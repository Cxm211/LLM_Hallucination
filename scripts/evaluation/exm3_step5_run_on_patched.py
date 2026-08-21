#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
exm3_step5_run_on_patched.py

For each <defects-root>/<project>/<project>-<id>-buggy/ (after patching by step4):
  1. Run `ant clean` (output to console only)
  2. Run main command (default `defects4j test -r`) with timeout, write log to
     <logs-root>/<project>/<project>-<id>-buggy.log
  3. (Optional) Run JaCoCo combining trigger tests + add_test*.java test methods:
     - Clear old jacoco.exec / jacoco.xml / jacoco-html
     - Run each trigger test with jacoco agent (defects4j test -t), appending to jacoco.exec
     - Run each add_test*.java method (FQCN from first line) with jacoco agent, appending
     - Generate combined jacoco.xml + jacoco-html/
  4. After all tests, check each add_test*.java result and write
     <out-dir>/<Project>.csv  (Pass / Not Pass / Not Compilable)
  5. (If --jacoco) Parse jacoco.xml against <methods-dir>/*_methods.csv
     and write per-line coverage to <coverage-out-dir>/<Project>.csv

Usage:
  python exm3_step5_run_on_patched.py
  python exm3_step5_run_on_patched.py --project Chart
  python exm3_step5_run_on_patched.py --project Chart --ids 1,3,5-7
  python exm3_step5_run_on_patched.py -j 4 --jacoco

Requires JACOCO_HOME env var when --jacoco is used.
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

BUGGY_RE = re.compile(r"^([A-Za-z]+)-(\d+)-buggy$")
IND_BUGGY_RE = re.compile(r"^([A-Za-z]+)-(\d+)-buggy_ind(\d+)$")
EXIT_CODE_RE = re.compile(r"^----\s*exit code:\s*(\d+)", re.MULTILINE)
METHOD_RE = re.compile(r"\bpublic\s+void\s+(\w+)\s*\(")
TEST_RE = re.compile(r"^[A-Za-z0-9_$.]+::[A-Za-z0-9_]+$")
ADD_TEST_GLOB = "add_test*.java"
ADD_TEST_ID_FILE = ".add_test_id"

CSV_FIELDNAMES = ["project", "bug_id", "add_test_file", "class_fqcn", "method_name", "result"]

# Individual coverage CSV schema (extra add_test_file column)
FIELDNAMES_IND_COVERAGE = [
    "project", "bug_id", "folder", "add_test_file",
    "function_id", "class_fqcn", "file", "method_sig_snippet",
    "method_start", "method_end", "line", "line_in_method",
    "ci", "mi", "cb", "mb", "executed",
]

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
    """Return sorted list of (bug_id, buggy_dir) for combined (original) dirs only."""
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


def find_individual_dirs(defects_root: Path, project: str) -> List[Tuple[str, str, Path, str]]:
    """Return sorted list of (bug_id, ind_idx, buggy_dir, add_test_filename) for individual copy dirs."""
    proj_dir = defects_root / project
    if not proj_dir.is_dir():
        return []
    result = []
    for d in proj_dir.iterdir():
        if not d.is_dir():
            continue
        m = IND_BUGGY_RE.match(d.name)
        if m and m.group(1) == project:
            bug_id = m.group(2)
            ind_idx = m.group(3)
            add_test_id_path = d / ADD_TEST_ID_FILE
            add_test_name = (
                add_test_id_path.read_text(encoding="utf-8").strip()
                if add_test_id_path.exists() else ""
            )
            result.append((bug_id, ind_idx, d, add_test_name))
    result.sort(key=lambda x: (int(x[0]), int(x[1])))
    return result


def parse_first_line(first_line: str) -> Tuple[Optional[str], Optional[str]]:
    """
    '// org/jfree/chart/Foo.java::testBar' -> ('org/jfree/chart/Foo.java', 'testBar')
    """
    line = first_line.strip()
    if not line.startswith("//"):
        return None, None
    content = line[2:].strip()
    if "::" in content:
        path, method = content.split("::", 1)
        return path.strip() or None, method.strip() or None
    return content.strip() or None, None


def path_to_fqcn(rel_path: str) -> str:
    """'org/jfree/chart/Foo.java' -> 'org.jfree.chart.Foo'"""
    return rel_path.removesuffix(".java").replace("/", ".")


def get_add_test_ids(results_bug_dir: Path, only_file: Optional[str] = None) -> List[str]:
    """Read first line of each add_test*.java and return list of FQCN::method test IDs.
    only_file: if set, only parse this specific add_test filename."""
    test_ids: List[str] = []
    if only_file:
        candidates = [results_bug_dir / only_file]
    else:
        candidates = sorted(results_bug_dir.glob(ADD_TEST_GLOB))
    for af in candidates:
        if not af.exists():
            continue
        try:
            lines_af = af.read_text(encoding="utf-8", errors="replace").splitlines()
            if not lines_af:
                continue
            rel_path, method_name = parse_first_line(lines_af[0])
            if rel_path and not method_name:
                mm = METHOD_RE.search("\n".join(lines_af[1:]))
                method_name = mm.group(1) if mm else None
            if rel_path and method_name:
                test_ids.append(f"{path_to_fqcn(rel_path)}::{method_name}")
        except Exception:
            pass
    return test_ids


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


def run_all_tests_with_jacoco(
    cwd: Path,
    jacoco_home: Path,
    all_tests: List[str],
    log_write,
    timeout_seconds: int,
    trigger_count: int = 0,
) -> None:
    """
    Run all_tests sequentially with JaCoCo agent, appending to the same jacoco.exec.
    all_tests = trigger_tests + add_test ids.

    If trigger_count > 0, after running the first trigger_count tests, snapshot
    jacoco.exec -> jacoco_base.exec for later base XML generation.
    """
    exec_file = cwd / "jacoco.exec"
    base_env = os.environ.copy()
    base_env.pop("JAVA_TOOL_OPTIONS", None)

    start = time.time()
    for i, t in enumerate(all_tests):
        if time.time() - start > timeout_seconds:
            raise TimeoutError(f"JaCoCo stage timeout (> {timeout_seconds}s)")

        # After running all trigger tests, snapshot for base coverage
        if trigger_count > 0 and i == trigger_count and exec_file.exists():
            import shutil
            base_exec = cwd / "jacoco_base.exec"
            shutil.copy2(str(exec_file), str(base_exec))
            log_write(f"[JACOCO] base snapshot saved ({trigger_count} trigger tests)\n")

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
        if "Cannot compile test suite!" in (p.stdout or ""):
            raise RuntimeError("Not Compilable: test suite failed to compile")
        if p.returncode != 0:
            log_write(f"[JACOCO] WARNING: test -t returned {p.returncode} for {t}\n")

    if not exec_file.exists():
        raise RuntimeError("jacoco.exec not generated")


def generate_jacoco_report(
    cwd: Path,
    jacoco_home: Path,
    log_write,
    timeout_seconds: int,
    exec_file: Optional[Path] = None,
    xml_file: Optional[Path] = None,
) -> Path:
    if exec_file is None:
        exec_file = cwd / "jacoco.exec"
    if not exec_file.exists():
        raise RuntimeError(f"{exec_file.name} missing")

    bin_dir = resolve_exported_path(cwd, defects4j_export_value("dir.bin.classes", cwd))
    src_dir = resolve_exported_path(cwd, defects4j_export_value("dir.src.classes", cwd))
    if not bin_dir.exists():
        raise RuntimeError(f"bin classes not found: {bin_dir}")
    if not src_dir.exists():
        raise RuntimeError(f"src classes not found: {src_dir}")

    if xml_file is None:
        xml_file = cwd / "jacoco.xml"
    html_dir = cwd / "jacoco-html"
    html_dir.mkdir(parents=True, exist_ok=True)
    cli = jacoco_home / "lib/jacococli.jar"

    log_write(f"[JACOCO] generating report -> {xml_file.name}...\n")
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
    results_bug_dir: Path,
    cmd: str,
    log_dir: Path,
    timeout: int,
    run_jacoco: bool = False,
    jacoco_home: Optional[Path] = None,
    jacoco_timeout: int = DEFAULT_JACOCO_TIMEOUT,
    single_add_test_id: Optional[str] = None,
) -> Tuple[str, int, Dict[str, Any]]:
    """Run ant clean + main command. Optionally run JaCoCo on trigger + add tests.
    Returns (dir_name, rc, jacoco_row_dict).
    """
    log_dir.mkdir(parents=True, exist_ok=True)
    log_file = log_dir / f"{bug_dir.name}.log"

    jacoco_row: Dict[str, Any] = {
        "project": bug_dir.parent.name,
        "buggy_folder": bug_dir.name,
        "buggy_path": str(bug_dir),
        "trigger_tests_count": 0,
        "add_tests_count": 0,
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
        rc = 1

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
                for line in proc.stdout:
                    log_write(line)

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

        footer = f"---- exit code: {rc} | {bug_dir.name}\n\n"
        log_write(footer)
        print(footer, end="")

        # ---- JaCoCo (optional): trigger tests + add tests ----
        if run_jacoco:
            log_write("\n>>> [JACOCO] starting JaCoCo coverage collection\n")
            print(f">>> [JACOCO] starting JaCoCo for {bug_dir.name}")

            try:
                assert jacoco_home is not None
                clean_jacoco_artifacts(bug_dir, log_write)

                trigger_tests = get_trigger_tests(bug_dir)
                if single_add_test_id is not None:
                    add_tests = [single_add_test_id]
                else:
                    add_tests = get_add_test_ids(results_bug_dir)
                all_tests = trigger_tests + add_tests

                jacoco_row["trigger_tests_count"] = len(trigger_tests)
                jacoco_row["add_tests_count"] = len(add_tests)

                log_write(
                    f"[JACOCO] trigger tests: {len(trigger_tests)}, "
                    f"add tests: {len(add_tests)}, "
                    f"total: {len(all_tests)}\n"
                )

                if not all_tests:
                    jacoco_row["error"] = "No tests to run"
                    log_write(">>> [JACOCO] no tests found, skipping\n")
                else:
                    # Snapshot base after trigger tests whenever there are add_tests
                    tc = len(trigger_tests) if add_tests else 0
                    run_all_tests_with_jacoco(
                        bug_dir, jacoco_home, all_tests,
                        log_write=log_write,
                        timeout_seconds=jacoco_timeout,
                        trigger_count=tc,
                    )
                    # Generate base XML if snapshot was created
                    base_exec = bug_dir / "jacoco_base.exec"
                    if base_exec.exists():
                        generate_jacoco_report(
                            bug_dir, jacoco_home,
                            log_write=log_write,
                            timeout_seconds=jacoco_timeout,
                            exec_file=base_exec,
                            xml_file=bug_dir / "jacoco_base.xml",
                        )
                    # Generate combined XML
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

    return bug_dir.name, rc, jacoco_row


# -------------------- testcase result checking --------------------

def test_in_failing_list(log_text: str, fqcn: str, method_name: str) -> bool:
    short_class = fqcn.split(".")[-1]
    for pat in (f"{fqcn}::{method_name}", f"{short_class}::{method_name}"):
        if pat in log_text:
            return True
    return False


_TEST_SECTION_RE = re.compile(
    r"^>>> defects4j test -r\n(.*?)^---- exit code: (\d+)",
    re.MULTILINE | re.DOTALL,
)


def check_add_tests(
    results_bug_dir: Path,
    project: str,
    bug_id: str,
    log_file: Path,
    only_add_test_file: Optional[str] = None,
) -> List[Dict]:
    """Check each add_test*.java against the log. Returns list of result rows.
    only_add_test_file: if set, check only this specific add_test filename.

    Only the 'defects4j test -r' section of the log is used for judging results,
    ignoring 'ant clean' and JaCoCo sections to avoid false matches.
    """
    if only_add_test_file:
        candidate = results_bug_dir / only_add_test_file
        add_files = [candidate] if candidate.exists() else []
    else:
        add_files = sorted(results_bug_dir.glob(ADD_TEST_GLOB))
    if not add_files:
        return []

    full_log = log_file.read_text(encoding="utf-8", errors="replace") if log_file.exists() else ""

    # Extract only the 'defects4j test -r' section and its exit code
    test_section = ""
    exit_code: Optional[int] = None
    if full_log:
        m = _TEST_SECTION_RE.search(full_log)
        if m:
            test_section = m.group(1)
            exit_code = int(m.group(2))
        else:
            # Fallback: use the last exit code line (for older logs without section markers)
            em = None
            for em in EXIT_CODE_RE.finditer(full_log):
                pass  # iterate to get the last match
            if em:
                exit_code = int(em.group(1))
            test_section = full_log

    rows = []
    for af in add_files:
        text = af.read_text(encoding="utf-8", errors="replace")
        lines = text.splitlines()
        if not lines:
            continue

        rel_path, method_name = parse_first_line(lines[0])
        code = "\n".join(lines[1:])

        if not method_name:
            mm = METHOD_RE.search(code)
            method_name = mm.group(1) if mm else None

        fqcn = path_to_fqcn(rel_path) if rel_path else ""

        if not full_log:
            result = "No Log"
        elif "[TIMEOUT]" in full_log or exit_code == 124:
            result = "Timeout"
        elif exit_code is None:
            result = "Unknown"
        elif exit_code != 0:
            result = "Not Compilable"
        else:
            if method_name and fqcn and test_in_failing_list(test_section, fqcn, method_name):
                result = "Not Pass"
            else:
                result = "Pass"

        rows.append({
            "project": project,
            "bug_id": bug_id,
            "add_test_file": af.name,
            "class_fqcn": fqcn,
            "method_name": method_name or "",
            "result": result,
        })

    return rows


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
    """'org.jfree.chart.plot.CategoryPlot' -> 'org/jfree/chart/plot'"""
    class_fqcn = (class_fqcn or "").strip()
    if not class_fqcn or "." not in class_fqcn:
        return ""
    return "/".join(class_fqcn.split(".")[:-1])


_IND_FOLDER_RE = re.compile(r"_ind\d+$")


def parse_coverage_for_all_projects(
    defects_root: Path,
    methods_dir: Path,
    coverage_out_dir: Path,
    project_filter: Optional[str] = None,
    error_log: Optional[Path] = None,
    ind_coverage_out_dir: Optional[Path] = None,
    ind_base_coverage_out_dir: Optional[Path] = None,
    base_coverage_out_dir: Optional[Path] = None,
) -> None:
    """
    For each *_methods.csv in methods_dir, parse jacoco.xml in defects_root
    and write per-line coverage CSV to coverage_out_dir/<Project>.csv.
    """
    coverage_out_dir.mkdir(parents=True, exist_ok=True)
    if ind_coverage_out_dir:
        ind_coverage_out_dir.mkdir(parents=True, exist_ok=True)
    if ind_base_coverage_out_dir:
        ind_base_coverage_out_dir.mkdir(parents=True, exist_ok=True)
    if base_coverage_out_dir:
        base_coverage_out_dir.mkdir(parents=True, exist_ok=True)

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

    # Open per-project output files lazily
    combined_writers: Dict[str, Any] = {}
    combined_base_writers: Dict[str, Any] = {}
    ind_writers: Dict[str, Any] = {}
    ind_base_writers: Dict[str, Any] = {}
    combined_files: Dict[str, Any] = {}
    combined_base_files: Dict[str, Any] = {}
    ind_files: Dict[str, Any] = {}
    ind_base_files: Dict[str, Any] = {}

    def get_combined_writer(proj: str):
        if proj not in combined_writers:
            p = coverage_out_dir / f"{proj}.csv"
            f = open(p, "w", newline="", encoding="utf-8")
            w = csv.writer(f)
            w.writerow([
                "project", "bug_id", "folder", "function_id",
                "class_fqcn", "file", "method_sig_snippet",
                "method_start", "method_end", "line", "line_in_method",
                "ci", "mi", "cb", "mb", "executed",
            ])
            combined_files[proj] = f
            combined_writers[proj] = w
        return combined_writers[proj]

    def get_combined_base_writer(proj: str):
        if base_coverage_out_dir is None:
            return None
        if proj not in combined_base_writers:
            p = base_coverage_out_dir / f"{proj}.csv"
            f = open(p, "w", newline="", encoding="utf-8")
            w = csv.writer(f)
            w.writerow([
                "project", "bug_id", "folder", "function_id",
                "class_fqcn", "file", "method_sig_snippet",
                "method_start", "method_end", "line", "line_in_method",
                "ci", "mi", "cb", "mb", "executed",
            ])
            combined_base_files[proj] = f
            combined_base_writers[proj] = w
        return combined_base_writers[proj]

    def get_ind_writer(proj: str):
        if ind_coverage_out_dir is None:
            return None
        if proj not in ind_writers:
            p = ind_coverage_out_dir / f"{proj}.csv"
            f = open(p, "w", newline="", encoding="utf-8")
            w = csv.writer(f)
            w.writerow([
                "project", "bug_id", "folder", "add_test_file", "function_id",
                "class_fqcn", "file", "method_sig_snippet",
                "method_start", "method_end", "line", "line_in_method",
                "ci", "mi", "cb", "mb", "executed",
            ])
            ind_files[proj] = f
            ind_writers[proj] = w
        return ind_writers[proj]

    def get_ind_base_writer(proj: str):
        if ind_base_coverage_out_dir is None:
            return None
        if proj not in ind_base_writers:
            p = ind_base_coverage_out_dir / f"{proj}.csv"
            f = open(p, "w", newline="", encoding="utf-8")
            w = csv.writer(f)
            w.writerow([
                "project", "bug_id", "folder", "add_test_file", "function_id",
                "class_fqcn", "file", "method_sig_snippet",
                "method_start", "method_end", "line", "line_in_method",
                "ci", "mi", "cb", "mb", "executed",
            ])
            ind_base_files[proj] = f
            ind_base_writers[proj] = w
        return ind_base_writers[proj]

    try:
        for methods_csv in methods_csvs:
            base = methods_csv.stem.replace("_methods", "")
            project = base[0].upper() + base[1:] if base else "UNKNOWN"

            if project_filter and project.lower() != project_filter.lower():
                continue

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

            for r in method_rows:
                bug_id = (r.get("bug_id") or "").strip()
                folder = (r.get("folder") or "").strip()
                function_id = r.get("_function_id", "function_1")

                is_individual = bool(_IND_FOLDER_RE.search(folder))

                # For individual folders: get add_test_file from .add_test_id marker
                add_test_file_label = ""
                if is_individual:
                    add_test_id_path = defects_root / project / folder / ADD_TEST_ID_FILE
                    if add_test_id_path.exists():
                        add_test_file_label = add_test_id_path.read_text(encoding="utf-8").strip()

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

                writer = get_ind_writer(project) if is_individual else get_combined_writer(project)
                if writer is None:
                    continue

                if not os.path.isfile(jacoco_xml):
                    msg = "Missing jacoco.xml"
                    print(f"  [NOT_COMPILABLE] {msg} -> {jacoco_xml}", flush=True)
                    if err_w:
                        err_w.writerow([project, bug_id, folder, jacoco_xml, msg])
                    for line in range(start, end + 1):
                        if is_individual:
                            writer.writerow([
                                project, bug_id, folder, add_test_file_label, function_id,
                                class_fqcn, file_path, method_sig, start, end,
                                line, (line - start) + 1, "", "", "", "", "Not Compilable",
                            ])
                        else:
                            writer.writerow([
                                project, bug_id, folder, function_id,
                                class_fqcn, file_path, method_sig, start, end,
                                line, (line - start) + 1, "", "", "", "", "Not Compilable",
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

                    if is_individual:
                        if ci is None:
                            writer.writerow([
                                project, bug_id, folder, add_test_file_label, function_id,
                                class_fqcn, file_path, method_sig, start, end,
                                line, line_in_method, "", "", "", "", "",
                            ])
                        else:
                            writer.writerow([
                                project, bug_id, folder, add_test_file_label, function_id,
                                class_fqcn, file_path, method_sig, start, end,
                                line, line_in_method, ci, mi, cb, mb, bool(ci > 0 or cb > 0),
                            ])

                        # Write base (trigger-only) coverage from jacoco_base.xml
                        base_w = get_ind_base_writer(project)
                        if base_w is not None:
                            base_xml_path = str(defects_root / project / folder / "jacoco_base.xml")
                            if base_xml_path not in jacoco_cache:
                                bjmap, berr = safe_parse_jacoco_to_map(base_xml_path)
                                if berr is None and bjmap is not None:
                                    jacoco_cache[base_xml_path] = bjmap
                            base_map = jacoco_cache.get(base_xml_path)
                            bci = bmi = bcb = bmb = None
                            if base_map:
                                if pkg_guess:
                                    bv = base_map.get((pkg_guess, sf_name, line))
                                    if bv is not None:
                                        bci, bmi, bcb, bmb = bv
                                if bci is None:
                                    for (_, sf, nr), bv in base_map.items():
                                        if sf == sf_name and nr == line:
                                            bci, bmi, bcb, bmb = bv
                                            break
                            if bci is None:
                                base_w.writerow([
                                    project, bug_id, folder, add_test_file_label, function_id,
                                    class_fqcn, file_path, method_sig, start, end,
                                    line, line_in_method, "", "", "", "", "",
                                ])
                            else:
                                base_w.writerow([
                                    project, bug_id, folder, add_test_file_label, function_id,
                                    class_fqcn, file_path, method_sig, start, end,
                                    line, line_in_method, bci, bmi, bcb, bmb, bool(bci > 0 or bcb > 0),
                                ])
                    else:
                        if ci is None:
                            writer.writerow([
                                project, bug_id, folder, function_id,
                                class_fqcn, file_path, method_sig, start, end,
                                line, line_in_method, "", "", "", "", "",
                            ])
                        else:
                            writer.writerow([
                                project, bug_id, folder, function_id,
                                class_fqcn, file_path, method_sig, start, end,
                                line, line_in_method, ci, mi, cb, mb, bool(ci > 0 or cb > 0),
                            ])

                        # Write combined base (trigger-only) coverage from jacoco_base.xml
                        cbase_w = get_combined_base_writer(project)
                        if cbase_w is not None:
                            base_xml_path = str(defects_root / project / folder / "jacoco_base.xml")
                            if base_xml_path not in jacoco_cache:
                                bjmap, berr = safe_parse_jacoco_to_map(base_xml_path)
                                if berr is None and bjmap is not None:
                                    jacoco_cache[base_xml_path] = bjmap
                            base_map = jacoco_cache.get(base_xml_path)
                            bci = bmi = bcb = bmb = None
                            if base_map:
                                if pkg_guess:
                                    bv = base_map.get((pkg_guess, sf_name, line))
                                    if bv is not None:
                                        bci, bmi, bcb, bmb = bv
                                if bci is None:
                                    for (_, sf, nr), bv in base_map.items():
                                        if sf == sf_name and nr == line:
                                            bci, bmi, bcb, bmb = bv
                                            break
                            if bci is None:
                                cbase_w.writerow([
                                    project, bug_id, folder, function_id,
                                    class_fqcn, file_path, method_sig, start, end,
                                    line, line_in_method, "", "", "", "", "",
                                ])
                            else:
                                cbase_w.writerow([
                                    project, bug_id, folder, function_id,
                                    class_fqcn, file_path, method_sig, start, end,
                                    line, line_in_method, bci, bmi, bcb, bmb, bool(bci > 0 or bcb > 0),
                                ])

            for proj, f in combined_files.items():
                print(f"[OK] wrote {coverage_out_dir / proj}.csv", flush=True)
            if ind_coverage_out_dir:
                for proj, f in ind_files.items():
                    print(f"[OK-IND] wrote {ind_coverage_out_dir / proj}.csv", flush=True)

    finally:
        for f in combined_files.values():
            f.close()
        for f in combined_base_files.values():
            f.close()
        for f in ind_files.values():
            f.close()
        for f in ind_base_files.values():
            f.close()
        if err_f:
            err_f.close()
            if error_log:
                print(f"[OK] error log -> {error_log}", flush=True)


# -------------------- main --------------------

def main():
    ap = argparse.ArgumentParser(
        description="Run defects4j test -r on patched (fixed) *-buggy dirs, "
                    "optionally collect JaCoCo (trigger + add tests), "
                    "save logs and check testcase results."
    )
    ap.add_argument(
        "--defects-root", default=str(Path(__file__).resolve().parents[2] / "defects4j_checkouts"),
        help="Root containing <project>/<project>-<id>-buggy/ (default: defects4j_checkouts)",
    )
    ap.add_argument(
        "--results-root", default=str(Path(__file__).resolve().parents[2] / "results/data/exm3/Claude"),
        help="Root containing <project>/<id>/add_test*.java (default: results/data/exm3/Claude)",
    )
    ap.add_argument(
        "--logs-root", default="test-logs/exm3/fixed",
        help="Root for log output: <logs-root>/<project>/ (default: test-logs/exm3/fixed)",
    )
    ap.add_argument(
        "--out-dir", default=str(Path(__file__).resolve().parents[2] / "generated_evaluation/exm3_work/Claude/run_patched"),
        help="Output directory for combined per-project result CSVs "
             "(default: generated_evaluation/exm3_work/Claude/run_patched)",
    )
    ap.add_argument(
        "--ind-out-dir", default=str(Path(__file__).resolve().parents[2] / "generated_evaluation/exm3_work/Claude/run_patched_individual"),
        help="Output directory for individual per-project result CSVs "
             "(default: generated_evaluation/exm3_work/Claude/run_patched_individual)",
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

    # JaCoCo options
    ap.add_argument(
        "--jacoco", action=argparse.BooleanOptionalAction, default=True,
        help="Collect JaCoCo coverage: trigger tests + add_test*.java methods "
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
        "--coverage-out-dir", default=str(Path(__file__).resolve().parents[2] / "generated_evaluation/exm3_work/Claude/coverage_patched"),
        help="Output directory for combined per-project JaCoCo coverage CSVs "
             "(default: generated_evaluation/exm3_work/Claude/coverage_patched)",
    )
    ap.add_argument(
        "--ind-coverage-out-dir",
        default=str(Path(__file__).resolve().parents[2] / "generated_evaluation/exm3_work/Claude/coverage_patched_individual"),
        help="Output directory for individual per-project JaCoCo coverage CSVs "
             "(default: generated_evaluation/exm3_work/Claude/coverage_patched_individual)",
    )
    ap.add_argument(
        "--ind-base-coverage-out-dir",
        default=str(Path(__file__).resolve().parents[2] / "generated_evaluation/exm3_work/Claude/base_coverage_patched_individual"),
        help="Output directory for individual base (trigger-only) coverage CSVs "
             "(default: generated_evaluation/exm3_work/Claude/base_coverage_patched_individual)",
    )
    ap.add_argument(
        "--base-coverage-out-dir",
        default=str(Path(__file__).resolve().parents[2] / "generated_evaluation/exm3_work/Claude/base_coverage_patched"),
        help="Output directory for combined base (trigger-only) coverage CSVs "
             "(default: generated_evaluation/exm3_work/Claude/base_coverage_patched)",
    )

    args = ap.parse_args()
    defects_root = Path(args.defects_root).expanduser().resolve()
    results_root = Path(args.results_root).expanduser().resolve()
    logs_root = Path(args.logs_root).expanduser().resolve()
    out_dir = Path(args.out_dir).expanduser().resolve()
    ind_out_dir = Path(args.ind_out_dir).expanduser().resolve()
    methods_dir = Path(args.methods_dir).expanduser().resolve()
    coverage_out_dir = Path(args.coverage_out_dir).expanduser().resolve()
    ind_coverage_out_dir = Path(args.ind_coverage_out_dir).expanduser().resolve()
    ind_base_coverage_out_dir = Path(args.ind_base_coverage_out_dir).expanduser().resolve()
    base_coverage_out_dir = Path(args.base_coverage_out_dir).expanduser().resolve()

    if not defects_root.is_dir():
        sys.exit(f"[ERROR] defects-root not found: {defects_root}")
    if not results_root.is_dir():
        sys.exit(f"[ERROR] results-root not found: {results_root}")

    out_dir.mkdir(parents=True, exist_ok=True)
    ind_out_dir.mkdir(parents=True, exist_ok=True)
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

    # Collect combined tasks (original *-buggy dirs)
    tasks: List[Tuple[str, str, Path]] = []
    # Collect individual tasks: (project, bug_id, bug_dir, add_test_filename)
    ind_tasks: List[Tuple[str, str, Path, str]] = []
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
        for bug_id, _ind_idx, bug_dir, add_test_name in find_individual_dirs(defects_root, project):
            if id_filter and bug_id not in id_filter:
                continue
            if add_test_name:
                ind_tasks.append((project, bug_id, bug_dir, add_test_name))

    print(f"defects-root : {defects_root}")
    print(f"results-root : {results_root}")
    print(f"logs-root    : {logs_root}")
    print(f"out-dir      : {out_dir}")
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
    print()

    run_results: List[Tuple[str, str, int]] = []
    ind_run_results: List[Tuple[str, str, int]] = []
    jacoco_rows: List[Dict[str, Any]] = []

    def worker(
        project: str, bug_id: str, bug_dir: Path,
        add_test_name: Optional[str] = None,
    ) -> Tuple[str, str, int, Dict[str, Any]]:
        results_bug_dir = results_root / project / bug_id
        log_dir = logs_root / project
        single_id: Optional[str] = None
        if add_test_name:
            ids = get_add_test_ids(results_bug_dir, only_file=add_test_name)
            single_id = ids[0] if ids else None
        name, rc, jrow = run_one(
            bug_dir, results_bug_dir, args.cmd, log_dir, args.timeout,
            run_jacoco=args.jacoco,
            jacoco_home=jacoco_home,
            jacoco_timeout=args.jacoco_timeout,
            single_add_test_id=single_id,
        )
        return project, name, rc, jrow

    # --- Run combined tasks ---
    if args.jobs > 1:
        with ThreadPoolExecutor(max_workers=args.jobs) as ex:
            futs = {ex.submit(worker, proj, bid, bd): (proj, bid) for proj, bid, bd in tasks}
            for f in as_completed(futs):
                proj, name, rc, jrow = f.result()
                run_results.append((proj, name, rc))
                if args.jacoco:
                    jacoco_rows.append(jrow)
    else:
        for proj, bid, bd in tasks:
            proj, name, rc, jrow = worker(proj, bid, bd)
            run_results.append((proj, name, rc))
            if args.jacoco:
                jacoco_rows.append(jrow)

    # --- Run individual tasks ---
    if args.jobs > 1:
        with ThreadPoolExecutor(max_workers=args.jobs) as ex:
            futs = {ex.submit(worker, proj, bid, bd, atf): (proj, bid, atf)
                    for proj, bid, bd, atf in ind_tasks}
            for f in as_completed(futs):
                proj, name, rc, jrow = f.result()
                ind_run_results.append((proj, name, rc))
                if args.jacoco:
                    jacoco_rows.append(jrow)
    else:
        for proj, bid, bd, atf in ind_tasks:
            proj, name, rc, jrow = worker(proj, bid, bd, atf)
            ind_run_results.append((proj, name, rc))
            if args.jacoco:
                jacoco_rows.append(jrow)

    run_results.sort(key=lambda x: (x[0], x[1]))
    ind_run_results.sort(key=lambda x: (x[0], x[1]))
    fails = sum(1 for _, _, rc in run_results if rc != 0)
    timeouts = sum(1 for _, _, rc in run_results if rc == 124)
    ind_fails = sum(1 for _, _, rc in ind_run_results if rc != 0)

    print("\n=== Combined Test Run Summary ===")
    for proj, name, rc in run_results:
        status = "TIMEOUT" if rc == 124 else ("OK" if rc == 0 else f"FAIL({rc})")
        print(f"  {proj}/{name:40s} -> {status}")
    print(f"\nCombined total: {len(run_results)} | failed: {fails} | timed out: {timeouts}")

    print("\n=== Individual Test Run Summary ===")
    for proj, name, rc in ind_run_results:
        status = "TIMEOUT" if rc == 124 else ("OK" if rc == 0 else f"FAIL({rc})")
        print(f"  {proj}/{name:40s} -> {status}")
    print(f"\nIndividual total: {len(ind_run_results)} | failed: {ind_fails}")
    print(f"Logs : {logs_root}")

    # Write JaCoCo summary CSV
    if args.jacoco and jacoco_rows:
        jacoco_summary = logs_root / "jacoco_summary.csv"
        jacoco_summary.parent.mkdir(parents=True, exist_ok=True)
        with open(jacoco_summary, "w", newline="", encoding="utf-8") as f:
            w = csv.DictWriter(f, fieldnames=[
                "project", "buggy_folder", "buggy_path",
                "trigger_tests_count", "add_tests_count",
                "jacoco_xml", "success", "error",
            ])
            w.writeheader()
            w.writerows(sorted(jacoco_rows, key=lambda r: (r["project"], r["buggy_folder"])))
        print(f"JaCoCo summary: {jacoco_summary}")

    # ---- Check combined testcase results ----
    print("\n=== Checking Combined Testcase Results ===")
    project_rows: Dict[str, List[Dict]] = {}

    for project, bug_id, bug_dir in tasks:
        results_bug_dir = results_root / project / bug_id
        log_file = logs_root / project / f"{bug_dir.name}.log"
        rows = check_add_tests(results_bug_dir, project, bug_id, log_file)
        if rows:
            project_rows.setdefault(project, []).extend(rows)

    total = passed = not_pass = not_compilable = 0
    for project, rows in sorted(project_rows.items()):
        out_file = out_dir / f"{project}.csv"
        with open(out_file, "w", newline="", encoding="utf-8") as f:
            w = csv.DictWriter(f, fieldnames=CSV_FIELDNAMES)
            w.writeheader()
            w.writerows(rows)
        n_pass = sum(1 for r in rows if r["result"] == "Pass")
        n_fail = sum(1 for r in rows if r["result"] == "Not Pass")
        n_nc   = sum(1 for r in rows if r["result"] == "Not Compilable")
        total += len(rows)
        passed += n_pass
        not_pass += n_fail
        not_compilable += n_nc
        print(f"  [{project}] {len(rows)} tests | pass={n_pass} not_pass={n_fail} "
              f"not_compilable={n_nc} -> {out_file.name}")

    print(f"\n[DONE combined] total={total} pass={passed} not_pass={not_pass} "
          f"not_compilable={not_compilable}")
    print(f"CSVs : {out_dir}")

    # ---- Check individual testcase results ----
    print("\n=== Checking Individual Testcase Results ===")
    ind_project_rows: Dict[str, List[Dict]] = {}

    for project, bug_id, bug_dir, add_test_name in ind_tasks:
        results_bug_dir = results_root / project / bug_id
        log_file = logs_root / project / f"{bug_dir.name}.log"
        rows = check_add_tests(results_bug_dir, project, bug_id, log_file,
                               only_add_test_file=add_test_name)
        if rows:
            ind_project_rows.setdefault(project, []).extend(rows)

    ind_total = ind_passed = ind_not_pass = ind_not_compilable = 0
    for project, rows in sorted(ind_project_rows.items()):
        out_file = ind_out_dir / f"{project}.csv"
        with open(out_file, "w", newline="", encoding="utf-8") as f:
            w = csv.DictWriter(f, fieldnames=CSV_FIELDNAMES)
            w.writeheader()
            w.writerows(rows)
        n_pass = sum(1 for r in rows if r["result"] == "Pass")
        n_fail = sum(1 for r in rows if r["result"] == "Not Pass")
        n_nc   = sum(1 for r in rows if r["result"] == "Not Compilable")
        ind_total += len(rows)
        ind_passed += n_pass
        ind_not_pass += n_fail
        ind_not_compilable += n_nc
        print(f"  [{project}] {len(rows)} tests | pass={n_pass} not_pass={n_fail} "
              f"not_compilable={n_nc} -> {out_file.name}")

    print(f"\n[DONE individual] total={ind_total} pass={ind_passed} "
          f"not_pass={ind_not_pass} not_compilable={ind_not_compilable}")
    print(f"CSVs : {ind_out_dir}")

    # ---- Parse JaCoCo coverage (combined + individual) ----
    if args.jacoco:
        print("\n=== Parsing JaCoCo Coverage ===")
        parse_coverage_for_all_projects(
            defects_root=defects_root,
            methods_dir=methods_dir,
            coverage_out_dir=coverage_out_dir,
            project_filter=args.project,
            error_log=coverage_out_dir / "errors.csv",
            ind_coverage_out_dir=ind_coverage_out_dir,
            ind_base_coverage_out_dir=ind_base_coverage_out_dir,
            base_coverage_out_dir=base_coverage_out_dir,
        )
        print(f"Combined coverage CSVs: {coverage_out_dir}")
        print(f"Individual coverage CSVs: {ind_coverage_out_dir}")
        print(f"Individual base coverage CSVs: {ind_base_coverage_out_dir}")


if __name__ == "__main__":
    main()
