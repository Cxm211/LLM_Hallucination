#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
exm3_step6_build_groundtruth.py

For each <defects-root>/<project>/<project>-<id>-buggy/ (after patching by step4),
WITHOUT relying on add_test*.java (no step1 needed):
  1. Run `ant clean` (output to console only)
  2. Run main command (default `defects4j test -r`) with timeout, write log to
     <logs-root>/<project>/<project>-<id>-buggy.log
  3. Run JaCoCo on trigger tests only:
     - Clear old jacoco.exec / jacoco.xml / jacoco-html
     - Run each trigger test with jacoco agent (defects4j test -t), appending to jacoco.exec
     - Generate jacoco.xml + jacoco-html/
  4. Parse jacoco.xml against <methods-dir>/*_methods.csv (fixed_methods)
     and write per-line coverage to <coverage-out-dir>/<Project>.csv (base_coverage_groundtruth)

Usage:
  python exm3_step6_build_groundtruth.py
  python exm3_step6_build_groundtruth.py --project Chart
  python exm3_step6_build_groundtruth.py --project Chart --ids 1,3,5-7
  python exm3_step6_build_groundtruth.py -j 4
  python exm3_step6_build_groundtruth.py --no-jacoco   # test run only, skip jacoco

Requires JACOCO_HOME env var (default: jacoco ON).
"""

import argparse
import csv
import os
import re
import shlex
import signal
import subprocess
import sys
import threading
import time
import xml.etree.ElementTree as ET
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path
from typing import Any, Dict, List, Optional, Set, Tuple

TIMEOUT_SECONDS = 600          # 10 min per bug (main command)
DEFAULT_JACOCO_TIMEOUT = 600  # JaCoCo stage total timeout

BUGGY_RE = re.compile(r"^([A-Za-z]+)-(\d+)-buggy$")  # matches only original dirs, not _ind* copies
TEST_RE = re.compile(r"^[A-Za-z0-9_$.]+::[A-Za-z0-9_]+$")

# JaCoCo map type: (pkg, sourcefile, line) -> (ci, mi, cb, mb)
JacocoMap = Dict[Tuple[str, str, int], Tuple[int, int, int, int]]


# -------------------- helpers --------------------

def parse_id_expr(expr: str) -> Set[str]:
    ids: Set[str] = set()
    for part in expr.split(","):
        part = part.strip()
        if not part:
            continue
        m = re.match(r"^(\d+)-(\d+)$", part)
        if m:
            a, b = int(m.group(1)), int(m.group(2))
            if a > b:
                a, b = b, a
            ids.update(str(i) for i in range(a, b + 1))
        else:
            ids.add(part)
    return ids


def find_buggy_dirs(defects_root: Path, project: str) -> List[Tuple[str, Path]]:
    """Return sorted list of (bug_id, buggy_dir) for one project."""
    proj_dir = defects_root / project
    if not proj_dir.is_dir():
        return []
    result = []
    for d in proj_dir.iterdir():
        if not d.is_dir():
            continue
        m = BUGGY_RE.match(d.name)
        if m and m.group(1) == project:
            result.append((m.group(2), d))
    result.sort(key=lambda x: int(x[0]))
    return result


# -------------------- JaCoCo helpers --------------------

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


def run_capture(
    cmd: List[str],
    cwd: Path,
    env: Optional[dict] = None,
    timeout: Optional[int] = None,
) -> subprocess.CompletedProcess:
    return subprocess.run(
        cmd, cwd=str(cwd), env=env,
        stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
        text=True, timeout=timeout,
    )


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
    seen: Set[str] = set()
    for t in tokens:
        if TEST_RE.match(t) and t not in seen:
            seen.add(t)
            tests.append(t)
    return tests


def build_java_tool_options(jacoco_home: Path, destfile: Path, append: bool) -> str:
    agent = jacoco_home / "lib/jacocoagent.jar"
    return (
        f"-javaagent:{agent}=destfile={destfile},"
        f"append={'true' if append else 'false'},output=file"
    )


def resolve_exported_path(cwd: Path, p: str) -> Path:
    pp = Path(p)
    return pp if pp.is_absolute() else (cwd / pp).resolve()


def restore_test_sources(cwd: Path, log_write) -> None:
    """
    Undo step1 insertions by restoring from *.java.orig backups created by step1.
    For each <file>.java.orig found under cwd, overwrites <file>.java with its content.
    """
    orig_files = list(cwd.rglob("*.java.orig"))
    if not orig_files:
        log_write("[RESTORE] no *.java.orig backups found, nothing to restore\n")
        return
    for orig in sorted(orig_files):
        target = orig.with_suffix("")  # strips .orig -> .java
        try:
            target.write_bytes(orig.read_bytes())
            log_write(f"[RESTORE] restored {target.relative_to(cwd)}\n")
        except Exception as e:
            log_write(f"[RESTORE] failed to restore {target.relative_to(cwd)}: {e}\n")


def clean_jacoco_artifacts(cwd: Path, log_write) -> None:
    for name in ("jacoco.exec", "jacoco.xml"):
        f = cwd / name
        if f.exists():
            f.unlink()
            log_write(f"[JACOCO] removed {name}\n")
    html_dir = cwd / "jacoco-html"
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
    exec_file = cwd / "jacoco.exec"
    base_env = os.environ.copy()
    base_env.pop("JAVA_TOOL_OPTIONS", None)

    start = time.time()
    for i, t in enumerate(trigger_tests):
        if time.time() - start > timeout_seconds:
            raise TimeoutError(f"JaCoCo stage timeout (> {timeout_seconds}s)")

        env = base_env.copy()
        env["JAVA_TOOL_OPTIONS"] = build_java_tool_options(
            jacoco_home, exec_file, append=(i > 0)
        )

        log_write(f"[JACOCO] defects4j test -t {t}\n")
        remaining = max(60, timeout_seconds - int(time.time() - start))
        try:
            p = run_capture(["defects4j", "test", "-t", t], cwd=cwd, env=env, timeout=remaining)
        except subprocess.TimeoutExpired:
            log_write(f"[JACOCO] single test timeout ({remaining}s): {t}\n")
            continue
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

    bin_dir = resolve_exported_path(cwd, defects4j_export_value("dir.bin.classes", cwd))
    src_dir = resolve_exported_path(cwd, defects4j_export_value("dir.src.classes", cwd))
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
        ["java", "-jar", str(cli), "report", str(exec_file),
         "--classfiles", str(bin_dir),
         "--sourcefiles", str(src_dir),
         "--xml", str(xml_file),
         "--html", str(html_dir)],
        cwd=cwd, timeout=timeout_seconds,
    )
    log_write(p.stdout or "")
    if p.returncode != 0:
        raise RuntimeError(f"jacococli report failed ({p.returncode})")

    return xml_file


# -------------------- test running --------------------

def run_one(
    bug_dir: Path,
    cmd: str,
    log_dir: Path,
    timeout: int,
    run_jacoco: bool = True,
    jacoco_home: Optional[Path] = None,
    jacoco_timeout: int = DEFAULT_JACOCO_TIMEOUT,
) -> Tuple[str, int, Dict[str, Any], str]:
    """Run ant clean + main command. Optionally run JaCoCo on trigger tests.
    Returns (dir_name, rc, jacoco_row_dict, test_result).
    test_result is one of: 'Pass', 'Not Pass', 'Not Compilable', 'Timeout'.
    """
    log_dir.mkdir(parents=True, exist_ok=True)
    log_file = log_dir / f"{bug_dir.name}.log"

    jacoco_row: Dict[str, Any] = {
        "project": bug_dir.parent.name,
        "buggy_folder": bug_dir.name,
        "buggy_path": str(bug_dir),
        "trigger_tests_count": 0,
        "jacoco_xml": str(bug_dir / "jacoco.xml"),
        "success": False,
        "error": "",
    }

    with open(log_file, "w", encoding="utf-8", buffering=1) as lf:

        def log_write(s: str):
            lf.write(s)
            lf.flush()

        header = f"==== {time.strftime('%F %T')} :: {bug_dir.name} ====\n"
        log_write(header)
        print(header, end="")

        # Restore test sources (undo step1 insertions)
        restore_test_sources(bug_dir, log_write)

        # ant clean
        try:
            clean_proc = subprocess.Popen(
                ["ant", "clean"],
                cwd=str(bug_dir),
                stdout=subprocess.DEVNULL,
                stderr=subprocess.DEVNULL,
            )
            clean_rc = clean_proc.wait()
            log_write(f">>> ant clean exit code: {clean_rc}\n")
        except Exception as e:
            log_write(f"[WARN] ant clean failed: {e}\n")

        # main command
        log_write(f"\n>>> {cmd}\n")
        print(f">>> {cmd} in {bug_dir.name}")

        start = time.time()
        timed_out = False
        has_failing_tests = False
        rc = 1

        _failing_re = re.compile(r"Failing tests:\s*([1-9]\d*)")

        try:
            proc = subprocess.Popen(
                shlex.split(cmd),
                cwd=str(bug_dir),
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                text=True,
                bufsize=1,
                preexec_fn=os.setsid,
            )
            assert proc.stdout is not None

            def _reader():
                nonlocal has_failing_tests
                for line in proc.stdout:
                    log_write(line)
                    if _failing_re.search(line):
                        has_failing_tests = True

            reader_thread = threading.Thread(target=_reader, daemon=True)
            reader_thread.start()

            try:
                proc.wait(timeout=timeout)
            except subprocess.TimeoutExpired:
                timed_out = True
                msg = f"\n[TIMEOUT] killed after {timeout}s\n"
                log_write(msg)
                print(msg, end="")
                try:
                    os.killpg(os.getpgid(proc.pid), signal.SIGKILL)
                except ProcessLookupError:
                    pass

            reader_thread.join(timeout=5)
            rc = 124 if timed_out else proc.returncode

        except FileNotFoundError as e:
            log_write(f"[ERROR] command not found: {e}\n")
            rc = 127
        except Exception as e:
            log_write(f"[ERROR] unexpected: {e}\n")
            rc = 1

        if rc != 0:
            test_result = "Not Compilable"
        elif has_failing_tests:
            test_result = "Not Pass"
        else:
            test_result = "Pass"

        footer = f"---- exit code: {rc} | test_result: {test_result} | {bug_dir.name}\n\n"
        log_write(footer)
        print(footer, end="")

        # ---- JaCoCo: trigger tests only ----
        if run_jacoco:
            log_write("\n>>> [JACOCO] starting JaCoCo coverage collection (trigger tests)\n")
            print(f">>> [JACOCO] starting JaCoCo for {bug_dir.name}")

            try:
                assert jacoco_home is not None
                clean_jacoco_artifacts(bug_dir, log_write)

                triggers = get_trigger_tests(bug_dir)
                jacoco_row["trigger_tests_count"] = len(triggers)

                if not triggers:
                    jacoco_row["error"] = "No trigger tests"
                    log_write(">>> [JACOCO] no trigger tests found, skipping\n")
                else:
                    run_trigger_tests_with_jacoco(
                        bug_dir, jacoco_home, triggers,
                        log_write=log_write,
                        timeout_seconds=jacoco_timeout,
                    )
                    xml = generate_jacoco_report(
                        bug_dir, jacoco_home,
                        log_write=log_write,
                        timeout_seconds=jacoco_timeout,
                    )
                    jacoco_row["success"] = bool(xml.exists() and xml.stat().st_size > 0)
                    if not jacoco_row["success"]:
                        jacoco_row["error"] = "jacoco.xml not generated"

            except TimeoutError as e:
                jacoco_row["error"] = f"TIMEOUT: {e}"
                log_write(f">>> [JACOCO] timeout: {e}\n")
            except Exception as e:
                jacoco_row["error"] = str(e).replace("\n", " | ")
                log_write(f">>> [JACOCO] failed: {jacoco_row['error']}\n")

            log_write(">>> [JACOCO] done\n")

    return bug_dir.name, rc, jacoco_row, test_result


# -------------------- JaCoCo parsing --------------------

def parse_jacoco_to_map(xml_path: str) -> JacocoMap:
    tree = ET.parse(xml_path)
    root = tree.getroot()
    m: JacocoMap = {}
    for pkg in root.findall(".//package"):
        pkg_name = pkg.get("name", "")
        for sf in pkg.findall("./sourcefile"):
            sf_name = sf.get("name", "")
            for ln in sf.findall("./line"):
                nr = int(ln.get("nr", "0"))
                ci = int(ln.get("ci", "0"))
                mi = int(ln.get("mi", "0"))
                cb = int(ln.get("cb", "0"))
                mb = int(ln.get("mb", "0"))
                m[(pkg_name, sf_name, nr)] = (ci, mi, cb, mb)
    return m


def safe_parse_jacoco_to_map(xml_path: str) -> Tuple[Optional[JacocoMap], Optional[str]]:
    try:
        return parse_jacoco_to_map(xml_path), None
    except ET.ParseError as e:
        return None, f"ParseError: {e}"
    except Exception as e:
        return None, f"{type(e).__name__}: {e}"


def norm_int(x: str, default: int = 0) -> int:
    try:
        return int(x)
    except Exception:
        return default


def guess_pkg_from_class_fqcn(class_fqcn: str) -> str:
    class_fqcn = (class_fqcn or "").strip()
    if not class_fqcn or "." not in class_fqcn:
        return ""
    return "/".join(class_fqcn.split(".")[:-1])


def parse_coverage_for_all_projects(
    defects_root: Path,
    methods_dir: Path,
    coverage_out_dir: Path,
    project_filter: Optional[str] = None,
    error_log: Optional[Path] = None,
) -> None:
    """
    For each *_methods.csv in methods_dir, parse jacoco.xml in defects_root
    and write per-line coverage CSV to coverage_out_dir/<Project>.csv.
    """
    coverage_out_dir.mkdir(parents=True, exist_ok=True)

    methods_csvs = sorted(methods_dir.glob("*_methods.csv"))
    if not methods_csvs:
        print(f"[WARN] No *_methods.csv found in {methods_dir}", flush=True)
        return

    err_f = None
    err_w = None
    if error_log:
        error_log.parent.mkdir(parents=True, exist_ok=True)
        err_f = open(error_log, "w", newline="", encoding="utf-8")
        err_w = csv.writer(err_f)
        err_w.writerow(["project", "bug_id", "folder", "jacoco_xml", "error"])

    jacoco_cache: Dict[str, JacocoMap] = {}

    try:
        for methods_csv in methods_csvs:
            base = methods_csv.stem.replace("_methods", "")
            project = base[0].upper() + base[1:] if base else "UNKNOWN"

            if project_filter and project.lower() != project_filter.lower():
                continue

            out_path = coverage_out_dir / f"{project}.csv"
            print(f"\n[COVERAGE] {project} <= {methods_csv.name}", flush=True)

            with open(methods_csv, "r", encoding="utf-8") as f_in:
                method_rows = list(csv.DictReader(f_in))

            # Assign function_id per (bug_id, folder) group
            groups: Dict[Tuple[str, str], List[dict]] = {}
            for r in method_rows:
                key = ((r.get("bug_id") or "").strip(), (r.get("folder") or "").strip())
                groups.setdefault(key, []).append(r)

            for (bug_id, folder), lst in groups.items():
                lst.sort(key=lambda r: (
                    r.get("file", ""),
                    norm_int(r.get("fixed_method_start_line", "0")),
                    norm_int(r.get("fixed_method_end_line", "0")),
                    r.get("method_sig_snippet", ""),
                ))
                for idx, r in enumerate(lst, start=1):
                    r["_function_id"] = f"function_{idx}"

            with open(out_path, "w", newline="", encoding="utf-8") as f_out:
                writer = csv.writer(f_out)
                writer.writerow([
                    "project", "bug_id", "folder",
                    "function_id",
                    "class_fqcn", "file", "method_sig_snippet",
                    "method_start", "method_end",
                    "line", "line_in_method",
                    "ci", "mi", "cb", "mb",
                    "executed",
                ])

                for r in method_rows:
                    bug_id = (r.get("bug_id") or "").strip()
                    folder = (r.get("folder") or "").strip()
                    function_id = r.get("_function_id", "function_1")

                    # Skip individual copy dirs — only process base buggy dirs
                    if re.search(r"_ind\d+$", folder):
                        continue

                    # support both 'class_fqcn' and 'class_entry' column names
                    class_fqcn = (r.get("class_fqcn") or r.get("class_entry") or "").strip()
                    file_path = (r.get("file") or "").strip()
                    method_sig = (r.get("method_sig_snippet") or "").strip()
                    start = norm_int(r.get("fixed_method_start_line", "0"))
                    end = norm_int(r.get("fixed_method_end_line", "0"))

                    jacoco_xml = str(defects_root / project / folder / "jacoco.xml")
                    print(
                        f"  [PROCESS] {bug_id} {function_id} folder={folder} lines={start}-{end}",
                        flush=True,
                    )

                    if not os.path.isfile(jacoco_xml):
                        msg = "Missing jacoco.xml"
                        print(f"  [NOT_COMPILABLE] {msg} -> {jacoco_xml}", flush=True)
                        if err_w:
                            err_w.writerow([project, bug_id, folder, jacoco_xml, msg])
                        for line in range(start, end + 1):
                            writer.writerow([
                                project, bug_id, folder, function_id,
                                class_fqcn, file_path, method_sig,
                                start, end,
                                line, (line - start) + 1,
                                "", "", "", "", "Not Compilable",
                            ])
                        continue

                    if jacoco_xml not in jacoco_cache:
                        jmap, err = safe_parse_jacoco_to_map(jacoco_xml)
                        if err is not None or jmap is None:
                            print(f"  [SKIP] {err} -> {jacoco_xml}", flush=True)
                            if err_w:
                                err_w.writerow([project, bug_id, folder, jacoco_xml, err])
                            continue
                        jacoco_cache[jacoco_xml] = jmap

                    jacoco_map = jacoco_cache[jacoco_xml]
                    sf_name = os.path.basename(file_path)
                    pkg_guess = guess_pkg_from_class_fqcn(class_fqcn)

                    for line in range(start, end + 1):
                        line_in_method = (line - start) + 1
                        ci = mi = cb = mb = None

                        if pkg_guess:
                            v = jacoco_map.get((pkg_guess, sf_name, line))
                            if v is not None:
                                ci, mi, cb, mb = v

                        if ci is None:
                            for (_, sf, nr), v in jacoco_map.items():
                                if sf == sf_name and nr == line:
                                    ci, mi, cb, mb = v
                                    break

                        if ci is None:
                            writer.writerow([
                                project, bug_id, folder, function_id,
                                class_fqcn, file_path, method_sig,
                                start, end, line, line_in_method,
                                "", "", "", "", "",
                            ])
                        else:
                            writer.writerow([
                                project, bug_id, folder, function_id,
                                class_fqcn, file_path, method_sig,
                                start, end, line, line_in_method,
                                ci, mi, cb, mb, bool(ci > 0 or cb > 0),
                            ])

            print(f"[OK] wrote {out_path}", flush=True)

    finally:
        if err_f:
            err_f.close()
            if error_log:
                print(f"[OK] error log -> {error_log}", flush=True)


# -------------------- main --------------------

def main():
    ap = argparse.ArgumentParser(
        description="Run defects4j test -r on patched (fixed) *-buggy dirs, "
                    "collect JaCoCo coverage (trigger tests only), "
                    "and parse to base_coverage_groundtruth. No add_test*.java needed."
    )
    ap.add_argument(
        "--defects-root", default=str(Path(__file__).resolve().parents[2] / "defects4j_checkouts"),
        help="Root containing <project>/<project>-<id>-buggy/ (default: defects4j_checkouts)",
    )
    ap.add_argument(
        "--logs-root", default="test-logs/exm3/fixed_jacoco",
        help="Root for log output (default: test-logs/exm3/fixed_jacoco)",
    )
    ap.add_argument("--cmd", default="defects4j test -r",
                    help="Main test command (default: defects4j test -r)")
    ap.add_argument("--project", default=None, help="Only process this project (e.g. Chart)")
    ap.add_argument("--ids", default=None, help="Only process these bug IDs, e.g. '1,3,5-7'")
    ap.add_argument("-j", "--jobs", type=int, default=1, help="Parallel workers (default: 1)")
    ap.add_argument(
        "--timeout", type=int, default=TIMEOUT_SECONDS,
        help=f"Timeout per bug in seconds (default: {TIMEOUT_SECONDS})",
    )

    # JaCoCo options (default ON)
    ap.add_argument(
        "--jacoco", action=argparse.BooleanOptionalAction, default=True,
        help="Collect JaCoCo coverage using trigger tests "
             "(default: ON; use --no-jacoco to disable)",
    )
    ap.add_argument(
        "--jacoco-timeout", type=int, default=DEFAULT_JACOCO_TIMEOUT,
        help=f"JaCoCo stage timeout per bug in seconds (default: {DEFAULT_JACOCO_TIMEOUT})",
    )
    ap.add_argument(
        "--methods-dir", default=str(Path(__file__).resolve().parents[2] / "generated_evaluation/exm3_work/Claude/fixed_methods"),
        help="Dir containing *_methods.csv for coverage parsing "
             "(default: generated_evaluation/exm3_work/Claude/fixed_methods)",
    )
    ap.add_argument(
        "--coverage-out-dir", default=str(Path(__file__).resolve().parents[2] / "generated_evaluation/exm3_work/Claude/base_coverage_groundtruth"),
        help="Output directory for per-project JaCoCo coverage CSVs "
             "(default: generated_evaluation/exm3_work/Claude/base_coverage_groundtruth)",
    )
    ap.add_argument(
        "--fixed-out-dir", default=str(Path(__file__).resolve().parents[2] / "generated_evaluation/exm3_work/Claude/groundtruth_checkouts"),
        help="Output directory for per-project test result CSVs (Pass/Not Pass/etc.) "
             "(default: generated_evaluation/exm3_work/Claude/groundtruth_checkouts)",
    )

    args = ap.parse_args()
    defects_root = Path(args.defects_root).expanduser().resolve()
    logs_root = Path(args.logs_root).expanduser().resolve()
    methods_dir = Path(args.methods_dir).expanduser().resolve()
    coverage_out_dir = Path(args.coverage_out_dir).expanduser().resolve()
    fixed_out_dir = Path(args.fixed_out_dir).expanduser().resolve()

    if not defects_root.is_dir():
        sys.exit(f"[ERROR] defects-root not found: {defects_root}")

    id_filter: Optional[Set[str]] = parse_id_expr(args.ids) if args.ids else None

    # Validate JaCoCo env early
    jacoco_home: Optional[Path] = None
    if args.jacoco:
        try:
            jacoco_home = ensure_jacoco_home()
        except Exception as e:
            sys.exit(f"[ERROR] JaCoCo not configured: {e}")
        if not methods_dir.is_dir():
            sys.exit(f"[ERROR] methods-dir not found: {methods_dir}")

    # Collect tasks
    tasks: List[Tuple[str, str, Path]] = []
    for proj_dir in sorted(defects_root.iterdir(), key=lambda p: p.name):
        if not proj_dir.is_dir():
            continue
        project = proj_dir.name
        if args.project and project.lower() != args.project.lower():
            continue
        for bug_id, bug_dir in find_buggy_dirs(defects_root, project):
            if id_filter and bug_id not in id_filter:
                continue
            tasks.append((project, bug_id, bug_dir))

    print(f"defects-root : {defects_root}")
    print(f"logs-root    : {logs_root}")
    print(f"cmd          : {args.cmd}")
    print(f"bugs to run  : {len(tasks)}")
    print(f"jobs         : {args.jobs}")
    print(f"timeout      : {args.timeout}s")
    print(f"jacoco       : {'ON' if args.jacoco else 'OFF'}")
    if args.jacoco:
        print(f"jacoco-home  : {jacoco_home}")
        print(f"jacoco-timeout: {args.jacoco_timeout}s")
        print(f"methods-dir  : {methods_dir}")
        print(f"coverage-out : {coverage_out_dir}")
    print(f"fixed-out    : {fixed_out_dir}")
    print()

    run_results: List[Tuple[str, str, int]] = []
    jacoco_rows: List[Dict[str, Any]] = []
    # (project, bug_id, folder, test_result)
    fixed_rows: List[Tuple[str, str, str, str]] = []

    def worker(project: str, bug_id: str, bug_dir: Path) -> Tuple[str, str, str, int, Dict[str, Any], str]:
        log_dir = logs_root / project
        name, rc, jrow, test_result = run_one(
            bug_dir, args.cmd, log_dir, args.timeout,
            run_jacoco=args.jacoco,
            jacoco_home=jacoco_home,
            jacoco_timeout=args.jacoco_timeout,
        )
        return project, bug_id, name, rc, jrow, test_result

    if args.jobs > 1:
        with ThreadPoolExecutor(max_workers=args.jobs) as ex:
            futs = {ex.submit(worker, proj, bid, bd): (proj, bid) for proj, bid, bd in tasks}
            for f in as_completed(futs):
                proj, bid, name, rc, jrow, test_result = f.result()
                run_results.append((proj, name, rc))
                fixed_rows.append((proj, bid, name, test_result))
                if args.jacoco:
                    jacoco_rows.append(jrow)
    else:
        for proj, bid, bd in tasks:
            proj, bid, name, rc, jrow, test_result = worker(proj, bid, bd)
            run_results.append((proj, name, rc))
            fixed_rows.append((proj, bid, name, test_result))
            if args.jacoco:
                jacoco_rows.append(jrow)

    run_results.sort(key=lambda x: (x[0], x[1]))
    fails = sum(1 for _, _, rc in run_results if rc != 0)
    timeouts = sum(1 for _, _, rc in run_results if rc == 124)

    print("\n=== Test Run Summary ===")
    for proj, name, rc in run_results:
        status = "TIMEOUT" if rc == 124 else ("OK" if rc == 0 else f"FAIL({rc})")
        print(f"  {proj}/{name:40s} -> {status}")
    print(f"\nTotal: {len(run_results)} | failed: {fails} | timed out: {timeouts}")
    print(f"Logs : {logs_root}")

    # Write groundtruth_checkouts per-project CSVs (Pass/Not Pass/Not Compilable/Timeout)
    if fixed_rows:
        fixed_out_dir.mkdir(parents=True, exist_ok=True)
        # Group by project
        from collections import defaultdict
        proj_fixed: Dict[str, List[Tuple[str, str, str, str]]] = defaultdict(list)
        for row in fixed_rows:
            proj_fixed[row[0]].append(row)
        for proj, rows in sorted(proj_fixed.items()):
            rows.sort(key=lambda r: (int(r[1]) if r[1].isdigit() else float("inf"), r[1]))
            out_path = fixed_out_dir / f"{proj}.csv"
            with open(out_path, "w", newline="", encoding="utf-8") as f:
                w = csv.writer(f)
                w.writerow(["project", "bug_id", "folder", "result"])
                for proj_, bid, folder, result in rows:
                    w.writerow([proj_, bid, folder, result])
            print(f"Fixed results: {out_path}")

    # Write JaCoCo summary CSV
    if args.jacoco and jacoco_rows:
        jacoco_summary = logs_root / "jacoco_summary.csv"
        jacoco_summary.parent.mkdir(parents=True, exist_ok=True)
        with open(jacoco_summary, "w", newline="", encoding="utf-8") as f:
            w = csv.DictWriter(f, fieldnames=[
                "project", "buggy_folder", "buggy_path",
                "trigger_tests_count", "jacoco_xml", "success", "error",
            ])
            w.writeheader()
            w.writerows(sorted(jacoco_rows, key=lambda r: (r["project"], r["buggy_folder"])))
        print(f"JaCoCo summary: {jacoco_summary}")

    # ---- Parse JaCoCo coverage -> base_coverage_groundtruth ----
    if args.jacoco:
        print("\n=== Parsing JaCoCo Coverage -> base_coverage_groundtruth ===")
        parse_coverage_for_all_projects(
            defects_root=defects_root,
            methods_dir=methods_dir,
            coverage_out_dir=coverage_out_dir,
            project_filter=args.project,
            error_log=coverage_out_dir / "errors.csv",
        )
        print(f"Coverage CSVs: {coverage_out_dir}")


if __name__ == "__main__":
    main()
