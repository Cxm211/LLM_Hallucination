#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Run the test suite over each Defects4J checkout and optionally collect JaCoCo coverage.

For every <BASE>-*-buggy directory under --bug-root it:
  1. runs `ant clean` (console only)
  2. runs the main command, `defects4j test -r` by default, logging its output
  3. with --jacoco, collects coverage: stale jacoco.exec / jacoco.xml / jacoco-html are
     removed first, the agent is attached to one `defects4j test -t` run per triggering
     test, and jacoco.xml plus jacoco-html/ are produced

The main command is killed after TIMEOUT_SECONDS and recorded with exit code 124, matching
GNU timeout; the next directory is then processed as usual. Directories can be filtered with
--ids and processed in parallel with -j.

JaCoCo needs JACOCO_HOME pointing at an unpacked JaCoCo release, i.e. a directory holding
lib/jacocoagent.jar and lib/jacococli.jar.

Output:
  <log-dir>/<folder>.log          one log per directory
  <log-dir>/jacoco_summary.csv    one row per directory when --jacoco is used

Example:
  python3 exm2_step2_run_tests_with_coverage.py \
    --bug-root defects4j_checkouts/Csv \
    --logs-root test-logs/exm2/Claude \
    --jacoco --ids 1,3,5-7
"""

import argparse
import csv
import os
import signal
import re
import shlex
import subprocess
import sys
import time
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor, as_completed
from typing import Optional, List, Dict, Any, Tuple

# -------------------- timeouts --------------------
TIMEOUT_SECONDS = 600          # main command, i.e. defects4j test -r: 10 minutes
DEFAULT_JACOCO_TIMEOUT = 1200  # whole JaCoCo stage; overridable on the command line

# -------------------- JaCoCo / trigger tests --------------------
TEST_RE = re.compile(r"^[A-Za-z0-9_$.]+::[A-Za-z0-9_]+$")
# accepts Chart-7-buggy, Chart-7_1-buggy and 7-buggy
FOLDER_ID_RE = re.compile(r"^(?:[A-Za-z]+-)?(\d+)(?:_\d+)?-buggy$")


def parse_id_expr(expr: str) -> set[int]:
    """Expand an expression such as 1,3,5-7 into {1, 3, 5, 6, 7}."""
    ids: set[int] = set()
    for part in expr.split(","):
        part = part.strip()
        if not part:
            continue
        m = re.match(r"^(\d+)-(\d+)$", part)
        if m:
            a, b = int(m.group(1)), int(m.group(2))
            if a > b:
                a, b = b, a
            ids.update(range(a, b + 1))
        else:
            ids.add(int(part))
    return ids


def buggy_id_from_folder(folder_name: str) -> Optional[str]:
    m = FOLDER_ID_RE.match(folder_name)
    return m.group(1) if m else None


def find_buggy_dirs(bug_root: Path) -> List[Path]:
    """Find the <BASE>-<n>-buggy and <BASE>-<n>_<k>-buggy directories under bug_root."""
    if not bug_root.is_dir():
        print(f"[error] directory not found: {bug_root}", file=sys.stderr)
        sys.exit(1)

    base = bug_root.name
    candidates = [d for d in bug_root.iterdir() if d.is_dir()]

    def is_target(d: Path) -> bool:
        parts = d.name.split("-")
        return len(parts) >= 3 and parts[0] == base and parts[-1] == "buggy"

    def key(d: Path):
        parts = d.name.split("-")
        try:
            n = int(parts[1].split("_")[0])
        except Exception:
            n = 10**9
        return (n, d.name)

    return sorted([d for d in candidates if is_target(d)], key=key)


# ===================== JaCoCo helpers =====================

def run_capture(
    cmd: List[str],
    cwd: Path,
    env: Optional[dict] = None,
    timeout: Optional[int] = None,
) -> subprocess.CompletedProcess:
    """Run a command capturing stdout and stderr; the caller inspects the return code."""
    return subprocess.run(
        cmd,
        cwd=str(cwd),
        env=env,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        timeout=timeout,
    )


def ensure_jacoco_home() -> Path:
    jacoco_home = os.environ.get("JACOCO_HOME", "").strip()
    if not jacoco_home:
        raise RuntimeError("JACOCO_HOME is not set")
    jh = Path(jacoco_home).expanduser().resolve()
    if not (jh / "lib/jacocoagent.jar").exists():
        raise RuntimeError("jacocoagent.jar not found under JACOCO_HOME/lib/")
    if not (jh / "lib/jacococli.jar").exists():
        raise RuntimeError("jacococli.jar not found under JACOCO_HOME/lib/")
    return jh


def defects4j_export_raw(prop: str, cwd: Path) -> str:
    p = run_capture(["defects4j", "export", "-p", prop], cwd=cwd)
    return p.stdout or ""


def defects4j_export_value(prop: str, cwd: Path) -> str:
    out = defects4j_export_raw(prop, cwd)
    lines = [ln.strip() for ln in out.splitlines() if ln.strip()]
    if not lines:
        return ""
    for ln in reversed(lines):
        if "/" in ln or ln.startswith("."):
            return ln
    return lines[-1]


def get_trigger_tests(cwd: Path) -> List[str]:
    out = defects4j_export_raw("tests.trigger", cwd)
    tokens = re.split(r"[,\s]+", out.strip())
    tests: List[str] = []
    seen = set()
    for t in tokens:
        if TEST_RE.match(t) and t not in seen:
            seen.add(t)
            tests.append(t)
    return tests


def build_java_tool_options(jacoco_home: Path, destfile: Path, append: bool) -> str:
    agent = jacoco_home / "lib/jacocoagent.jar"
    return f"-javaagent:{agent}=destfile={destfile},append={'true' if append else 'false'},output=file"


def resolve_exported_path(cwd: Path, p: str) -> Path:
    pp = Path(p)
    return pp if pp.is_absolute() else (cwd / pp).resolve()


def clean_jacoco_artifacts(cwd: Path, log_write) -> None:
    """Remove stale artefacts before each JaCoCo run, so a jacoco.xml shipped with the
    project is never mistaken for freshly collected coverage."""
    exec_file = cwd / "jacoco.exec"
    xml_file = cwd / "jacoco.xml"
    html_dir = cwd / "jacoco-html"

    if exec_file.exists():
        exec_file.unlink()
        log_write("[JACOCO] removed jacoco.exec\n")
    if xml_file.exists():
        xml_file.unlink()
        log_write("[JACOCO] removed jacoco.xml\n")

    if html_dir.exists():
        for p in sorted(html_dir.rglob("*"), reverse=True):
            if p.is_file():
                p.unlink()
        log_write("[JACOCO] cleaned jacoco-html/\n")


def run_trigger_tests_with_jacoco(
    cwd: Path,
    jacoco_home: Path,
    trigger_tests: List[str],
    log_write,
    timeout_seconds: int,
) -> None:
    """
    Attach the JaCoCo agent to one `defects4j test -t` run per triggering test and
    produce cwd/jacoco.exec.
    """
    exec_file = cwd / "jacoco.exec"

    base_env = os.environ.copy()
    base_env.pop("JAVA_TOOL_OPTIONS", None)

    append = len(trigger_tests) > 1
    start = time.time()

    for t in trigger_tests:
        elapsed = time.time() - start
        if elapsed > timeout_seconds:
            raise TimeoutError(f"JaCoCo stage timeout (> {timeout_seconds}s)")

        remaining = max(30, int(timeout_seconds - elapsed))
        env = base_env.copy()
        env["JAVA_TOOL_OPTIONS"] = build_java_tool_options(jacoco_home, exec_file, append)

        log_write(f"[JACOCO] defects4j test -t {t}\n")
        try:
            p = run_capture(["defects4j", "test", "-t", t], cwd=cwd, env=env, timeout=remaining)
        except subprocess.TimeoutExpired:
            raise TimeoutError(f"JaCoCo test -t {t} timed out after {remaining}s")
        log_write(p.stdout or "")
        if p.returncode != 0:
            raise RuntimeError(f"defects4j test -t failed ({p.returncode}) for {t}")

    if not exec_file.exists():
        raise RuntimeError("jacoco.exec not generated")


def generate_jacoco_report(
    cwd: Path,
    jacoco_home: Path,
    log_write,
    timeout_seconds: int,
) -> Path:
    exec_file = cwd / "jacoco.exec"
    if not exec_file.exists():
        raise RuntimeError("jacoco.exec missing")

    bin_raw = defects4j_export_value("dir.bin.classes", cwd)
    src_raw = defects4j_export_value("dir.src.classes", cwd)

    bin_dir = resolve_exported_path(cwd, bin_raw)
    src_dir = resolve_exported_path(cwd, src_raw)

    if not bin_dir.exists():
        raise RuntimeError(f"bin classes not found: {bin_dir}")
    if not src_dir.exists():
        raise RuntimeError(f"src classes not found: {src_dir}")

    xml_file = cwd / "jacoco.xml"
    html_dir = cwd / "jacoco-html"
    html_dir.mkdir(parents=True, exist_ok=True)

    cli = jacoco_home / "lib/jacococli.jar"

    log_write("[JACOCO] java -jar jacococli.jar report ...\n")
    p = run_capture(
        [
            "java", "-jar", str(cli), "report", str(exec_file),
            "--classfiles", str(bin_dir),
            "--sourcefiles", str(src_dir),
            "--xml", str(xml_file),
            "--html", str(html_dir),
        ],
        cwd=cwd,
        timeout=timeout_seconds,
    )
    log_write(p.stdout or "")
    if p.returncode != 0:
        raise RuntimeError(f"jacococli report failed ({p.returncode})")

    return xml_file


# ===================== ant clean plus the main command, logged and timed out =====================

def run_one(
    dir_path: Path,
    cmd: str,
    log_dir: Path,
    run_jacoco: bool,
    jacoco_home: Optional[Path],
    jacoco_timeout: int,
) -> Tuple[str, int, Dict[str, Any]]:
    """
    Run ant clean and the main command in one directory, with a timeout, optionally
    followed by a JaCoCo pass that always recollects.

    Returns (dir_name, rc_main, jacoco_row_dict).
    """
    log_dir.mkdir(parents=True, exist_ok=True)
    log_file = log_dir / f"{dir_path.name}.log"

    # project name, taken from bug_root.name, e.g. Chart
    jacoco_row = {
        "project": dir_path.parent.name,
        "buggy_folder": dir_path.name,
        "buggy_path": str(dir_path),
        "trigger_tests_count": 0,
        "jacoco_xml": str(dir_path / "jacoco.xml"),
        "success": False,
        "error": "",
    }

    header = f"==== {time.strftime('%F %T')} :: {dir_path.name} ====\n"
    with open(log_file, "w", encoding="utf-8", buffering=1) as lf:

        def log_write(s: str):
            lf.write(s)
            lf.flush()

        log_write(header)
        print(header, end="")

        # ant clean, console only
        print(">>> pre-step: running `ant clean` (console only)")
        try:
            clean_proc = subprocess.Popen(
                ["ant", "clean"],
                cwd=dir_path,
                stdout=None,
                stderr=None,
                text=True,
            )
            clean_rc = clean_proc.wait()
            log_write(f">>> ant clean exit code: {clean_rc}\n")
        except Exception as e:
            msg = f"[warn] ant clean failed: {e}\n"
            log_write(msg)
            print(msg, end="")

        # main command
        log_write(f"\n>>> running main command: {cmd}\n")
        print(f"\n>>> running main command: {cmd}")

        start_time = time.time()
        timed_out = False
        rc_main = 1

        try:
            proc = subprocess.Popen(
                shlex.split(cmd),
                cwd=dir_path,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                text=True,
                bufsize=1,
                preexec_fn=os.setsid,
            )
            assert proc.stdout is not None

            def _reader():
                for line in proc.stdout:
                    log_write(line)
                    print(line, end="")

            import threading
            reader_thread = threading.Thread(target=_reader, daemon=True)
            reader_thread.start()

            try:
                proc.wait(timeout=TIMEOUT_SECONDS)
            except subprocess.TimeoutExpired:
                timed_out = True
                msg = f"\n[timeout] exceeded {TIMEOUT_SECONDS}s, process terminated.\n"
                log_write(msg)
                print(msg, end="")
                try:
                    os.killpg(os.getpgid(proc.pid), signal.SIGKILL)
                except ProcessLookupError:
                    pass

            reader_thread.join(timeout=5)

            if not timed_out:
                rc_main = proc.returncode
            else:
                rc_main = 124  # same convention as GNU timeout

        except FileNotFoundError as e:
            msg = f"[error] command not found: {e}\n"
            log_write(msg)
            print(msg, end="")
            rc_main = 127
        except Exception as e:
            msg = f"[error] unexpected exception: {e}\n"
            log_write(msg)
            print(msg, end="")
            rc_main = 1

        footer = f"---- main command exit code: {rc_main} | {dir_path.name}\n\n"
        log_write(footer)
        print(footer, end="")

        # ===================== JaCoCo, optional and always recollected =====================
        if run_jacoco:
            log_write("\n>>> [JACOCO] collecting coverage over the triggering tests\n")
            print(">>> [JACOCO] collecting coverage over the triggering tests")

            try:
                assert jacoco_home is not None

                # remove stale artefacts, including any jacoco.xml shipped with the project
                clean_jacoco_artifacts(dir_path, log_write)

                triggers = get_trigger_tests(dir_path)
                jacoco_row["trigger_tests_count"] = len(triggers)

                if not triggers:
                    jacoco_row["error"] = "No trigger tests"
                    log_write(">>> [JACOCO] no triggering test found, skipping\n")
                else:
                    run_trigger_tests_with_jacoco(
                        dir_path,
                        jacoco_home,
                        triggers,
                        log_write=log_write,
                        timeout_seconds=jacoco_timeout,
                    )
                    xml = generate_jacoco_report(
                        dir_path,
                        jacoco_home,
                        log_write=log_write,
                        timeout_seconds=jacoco_timeout,
                    )
                    jacoco_row["success"] = bool(xml.exists() and xml.stat().st_size > 0)
                    if not jacoco_row["success"]:
                        jacoco_row["error"] = "jacoco.xml not generated"

            except TimeoutError as e:
                jacoco_row["error"] = f"TIMEOUT: {e}"
                log_write(f">>> [JACOCO] timed out: {e}\n")
            except Exception as e:
                jacoco_row["error"] = str(e).replace("\n", " | ")
                log_write(f">>> [JACOCO] failed: {jacoco_row['error']}\n")

            log_write(">>> [JACOCO] done\n")

    return (dir_path.name, rc_main, jacoco_row)


def main():
    parser = argparse.ArgumentParser(
        description="Run defects4j test -r in each *-buggy directory, with a timeout, optionally collecting JaCoCo coverage"
    )
    parser.add_argument("--bug-root", default="defects4j-GPT4o/Chart")
    parser.add_argument("--cmd", default="defects4j test -r")
    parser.add_argument("--logs-root", default="test-logs/exm2/DeepSeek")
    parser.add_argument("--log-dir", default=None)
    parser.add_argument("-j", "--jobs", type=int, default=1)
    parser.add_argument("--ids", type=str)

    # JaCoCo switches
    parser.add_argument("--jacoco", action="store_true", help="Also collect JaCoCo coverage over the triggering tests of each directory")
    parser.add_argument("--jacoco-timeout", type=int, default=DEFAULT_JACOCO_TIMEOUT,
                        help=f"Timeout in seconds for the JaCoCo stage, default {DEFAULT_JACOCO_TIMEOUT}")

    args = parser.parse_args()

    script_dir = Path(__file__).resolve().parent
    bug_root = (script_dir / args.bug_root).resolve()

    if args.log_dir:
        log_dir = (script_dir / args.log_dir).resolve()
    else:
        log_dir = (script_dir / args.logs_root / bug_root.name).resolve()

    buggy_dirs = find_buggy_dirs(bug_root)

    # --ids filter
    if args.ids:
        selected = parse_id_expr(args.ids)
        selected_str = {str(i) for i in selected}
        filtered: List[Path] = []
        for d in buggy_dirs:
            bid = buggy_id_from_folder(d.name)
            if bid is not None and bid in selected_str:
                filtered.append(d)
        buggy_dirs = filtered

    jacoco_home: Optional[Path] = None
    if args.jacoco:
        try:
            jacoco_home = ensure_jacoco_home()
        except Exception as e:
            print(f"[error] JaCoCo is not configured: {e}", file=sys.stderr)
            sys.exit(2)

    print(f"working dir  : {bug_root}")
    print(f"log dir      : {log_dir}")
    print(f"directories  : {len(buggy_dirs)}")
    print(f"main timeout : {TIMEOUT_SECONDS}s")
    print(f"jacoco       : {'on' if args.jacoco else 'off'}")
    if args.jacoco:
        print(f"jacoco timeout: {args.jacoco_timeout}s")
    print("")

    results: List[Tuple[str, int]] = []
    jacoco_rows: List[Dict[str, Any]] = []

    if args.jobs > 1:
        with ThreadPoolExecutor(max_workers=args.jobs) as ex:
            futs = {
                ex.submit(
                    run_one, d, args.cmd, log_dir,
                    args.jacoco, jacoco_home, args.jacoco_timeout
                ): d
                for d in buggy_dirs
            }
            for f in as_completed(futs):
                name, rc, jrow = f.result()
                results.append((name, rc))
                if args.jacoco:
                    jacoco_rows.append(jrow)
    else:
        for d in buggy_dirs:
            name, rc, jrow = run_one(
                d, args.cmd, log_dir,
                args.jacoco, jacoco_home, args.jacoco_timeout
            )
            results.append((name, rc))
            if args.jacoco:
                jacoco_rows.append(jrow)

    results.sort(key=lambda x: x[0])
    fails = sum(1 for _, rc in results if rc != 0)
    timeouts = sum(1 for _, rc in results if rc == 124)

    print("\n=== summary, main command exit codes ===")
    for name, rc in results:
        print(f"{name:30s} -> {rc}")
    print(f"\ndone: {len(results)} directories | failed {fails} | timed out {timeouts}")
    print(f"log dir      : {log_dir}")

    # JaCoCo summary CSV
    if args.jacoco:
        out_csv = log_dir / "jacoco_summary.csv"
        with out_csv.open("w", newline="", encoding="utf-8") as f:
            w = csv.DictWriter(
                f,
                fieldnames=[
                    "project",
                    "buggy_folder",
                    "buggy_path",
                    "trigger_tests_count",
                    "jacoco_xml",
                    "success",
                    "error",
                ],
            )
            w.writeheader()
            w.writerows(sorted(jacoco_rows, key=lambda r: r["buggy_folder"]))
        print(f"jacoco summary: {out_csv}")


if __name__ == "__main__":
    main()
