#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
exm3_step2_run_on_buggy.py

For each <defects-root>/<project>/<project>-<id>-buggy/:
  1. Run `ant clean` (output to console only)
  2. Run main command (default `defects4j test -r`) with timeout, write log to
     <logs-root>/<project>/<project>-<id>-buggy.log
  3. After all tests, check each add_test*.java result and write
     <out-dir>/<Project>.csv  (Pass / Not Pass / Not Compilable)

Usage:
  python exm3_step2_run_on_buggy.py
  python exm3_step2_run_on_buggy.py --project Chart
  python exm3_step2_run_on_buggy.py --project Chart --ids 1,3,5-7
  python exm3_step2_run_on_buggy.py -j 4
"""

import argparse
import csv
import os
import re
import shlex
import signal
import subprocess
import threading
import sys
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path
from typing import Dict, List, Optional, Set, Tuple

TIMEOUT_SECONDS = 600  # 10 min per bug

BUGGY_RE = re.compile(r"^([A-Za-z]+)-(\d+)-buggy$")
IND_BUGGY_RE = re.compile(r"^([A-Za-z]+)-(\d+)-buggy_ind(\d+)$")
EXIT_CODE_RE = re.compile(r"^----\s*exit code:\s*(\d+)", re.MULTILINE)
METHOD_RE = re.compile(r"\bpublic\s+void\s+(\w+)\s*\(")
ADD_TEST_GLOB = "add_test*.java"
ADD_TEST_ID_FILE = ".add_test_id"

CSV_FIELDNAMES = ["project", "bug_id", "add_test_file", "class_fqcn", "method_name", "result"]


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


# -------------------- test running --------------------

def run_one(
    bug_dir: Path,
    cmd: str,
    log_dir: Path,
    timeout: int,
) -> Tuple[str, int]:
    """Run ant clean + main command in bug_dir. Returns (dir_name, rc)."""
    log_dir.mkdir(parents=True, exist_ok=True)
    log_file = log_dir / f"{bug_dir.name}.log"

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
                preexec_fn=os.setsid,  # new process group → can kill whole tree
            )
            assert proc.stdout is not None

            # Read stdout in a background thread so the main thread can enforce timeout
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

    return bug_dir.name, rc


# -------------------- testcase result checking --------------------

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


def test_in_failing_list(log_text: str, fqcn: str, method_name: str) -> bool:
    short_class = fqcn.split(".")[-1]
    for pat in (f"{fqcn}::{method_name}", f"{short_class}::{method_name}"):
        if pat in log_text:
            return True
    return False


def check_add_tests(
    results_bug_dir: Path,
    project: str,
    bug_id: str,
    log_file: Path,
    only_add_test_file: Optional[str] = None,
) -> List[Dict]:
    """Check each add_test*.java against the log. Returns list of result rows.
    only_add_test_file: if set, check only this specific add_test filename."""
    if only_add_test_file:
        candidate = results_bug_dir / only_add_test_file
        add_files = [candidate] if candidate.exists() else []
    else:
        add_files = sorted(results_bug_dir.glob(ADD_TEST_GLOB))
    if not add_files:
        return []

    log_text = log_file.read_text(encoding="utf-8", errors="replace") if log_file.exists() else ""
    exit_code: Optional[int] = None
    if log_text:
        m = EXIT_CODE_RE.search(log_text)
        if m:
            exit_code = int(m.group(1))

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

        if not log_text:
            result = "No Log"
        elif "[TIMEOUT]" in log_text or exit_code == 124:
            result = "Timeout"
        elif exit_code is None:
            result = "Unknown"
        elif exit_code != 0:
            result = "Not Compilable"
        else:
            if method_name and fqcn and test_in_failing_list(log_text, fqcn, method_name):
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


# -------------------- main --------------------

def main():
    ap = argparse.ArgumentParser(
        description="Run defects4j test -r on *-buggy dirs, save logs and check testcase results."
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
        "--logs-root", default="test-logs/exm3/buggy",
        help="Root for log output: <logs-root>/<project>/ (default: test-logs/exm3/buggy)",
    )
    ap.add_argument(
        "--out-dir", default=str(Path(__file__).resolve().parents[2] / "generated_evaluation/exm3_work/Claude/run_buggy"),
        help="Output directory for combined per-project result CSVs "
             "(default: generated_evaluation/exm3_work/Claude/run_buggy)",
    )
    ap.add_argument(
        "--ind-out-dir", default=str(Path(__file__).resolve().parents[2] / "generated_evaluation/exm3_work/Claude/run_buggy_individual"),
        help="Output directory for individual per-project result CSVs "
             "(default: generated_evaluation/exm3_work/Claude/run_buggy_individual)",
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
    args = ap.parse_args()
    defects_root = Path(args.defects_root).expanduser().resolve()
    results_root = Path(args.results_root).expanduser().resolve()
    logs_root = Path(args.logs_root).expanduser().resolve()
    out_dir = Path(args.out_dir).expanduser().resolve()
    ind_out_dir = Path(args.ind_out_dir).expanduser().resolve()

    if not defects_root.is_dir():
        sys.exit(f"[ERROR] defects-root not found: {defects_root}")
    if not results_root.is_dir():
        sys.exit(f"[ERROR] results-root not found: {results_root}")

    out_dir.mkdir(parents=True, exist_ok=True)
    ind_out_dir.mkdir(parents=True, exist_ok=True)
    id_filter: Optional[Set[str]] = parse_id_expr(args.ids) if args.ids else None

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
    print(f"ind-out-dir  : {ind_out_dir}")
    print(f"cmd          : {args.cmd}")
    print(f"combined bugs: {len(tasks)}")
    print(f"individual bugs: {len(ind_tasks)}")
    print(f"jobs         : {args.jobs}")
    print(f"timeout      : {args.timeout}s")
    print()

    run_results: List[Tuple[str, str, int]] = []
    ind_run_results: List[Tuple[str, str, int]] = []

    def worker(project: str, bug_dir: Path) -> Tuple[str, str, int]:
        log_dir = logs_root / project
        name, rc = run_one(bug_dir, args.cmd, log_dir, args.timeout)
        return project, name, rc

    # --- Run combined tasks ---
    if args.jobs > 1:
        with ThreadPoolExecutor(max_workers=args.jobs) as ex:
            futs = {ex.submit(worker, proj, bd): (proj, bid) for proj, bid, bd in tasks}
            for f in as_completed(futs):
                proj, name, rc = f.result()
                run_results.append((proj, name, rc))
    else:
        for proj, bid, bd in tasks:
            proj, name, rc = worker(proj, bd)
            run_results.append((proj, name, rc))

    # --- Run individual tasks ---
    if args.jobs > 1:
        with ThreadPoolExecutor(max_workers=args.jobs) as ex:
            futs = {ex.submit(worker, proj, bd): (proj, bid, atf)
                    for proj, bid, bd, atf in ind_tasks}
            for f in as_completed(futs):
                proj, name, rc = f.result()
                ind_run_results.append((proj, name, rc))
    else:
        for proj, bid, bd, _atf in ind_tasks:
            proj, name, rc = worker(proj, bd)
            ind_run_results.append((proj, name, rc))

    run_results.sort(key=lambda x: (x[0], x[1]))
    ind_run_results.sort(key=lambda x: (x[0], x[1]))
    fails = sum(1 for _, _, rc in run_results if rc != 0)
    timeouts = sum(1 for _, _, rc in run_results if rc == 124)

    print("\n=== Combined Test Run Summary ===")
    for proj, name, rc in run_results:
        status = "TIMEOUT" if rc == 124 else ("OK" if rc == 0 else f"FAIL({rc})")
        print(f"  {proj}/{name:40s} -> {status}")
    print(f"\nCombined total: {len(run_results)} | failed: {fails} | timed out: {timeouts}")

    ind_fails = sum(1 for _, _, rc in ind_run_results if rc != 0)
    print(f"\n=== Individual Test Run Summary ===")
    for proj, name, rc in ind_run_results:
        status = "TIMEOUT" if rc == 124 else ("OK" if rc == 0 else f"FAIL({rc})")
        print(f"  {proj}/{name:40s} -> {status}")
    print(f"\nIndividual total: {len(ind_run_results)} | failed: {ind_fails}")
    print(f"Logs : {logs_root}")

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


if __name__ == "__main__":
    main()
