#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
exm3_step7_run_on_groundtruth.py

For each bug in <results-root>/<project>/<id>/:
  1. Checkout the defects4j FIXED version into
       <defects-root>/<project>/<Project>-<id>-fixed/
     (skipped if dir already exists)
  2. Insert add_test*.java testcases (mirrors exm3_step1_insert_testcases):
       - If N == 1: insert into original fixed dir only
       - If N > 1:  create individual copies <Project>-<id>-fixed_ind1 ...
                    insert one test per copy; also insert all into original
  3. Run `defects4j test -r` on:
       - original <Project>-<id>-fixed  (combined)
       - each     <Project>-<id>-fixed_indK (individual)
  4. Record Pass / Not Pass / Not Compilable
  5. Write combined results   -> <out-dir>/<Project>.csv
     Write individual results -> <ind-out-dir>/<Project>.csv
  6. Delete checked-out fixed dirs (combined + individual) unless --no-cleanup

Usage:
  python exm3_step7_run_on_groundtruth.py
  python exm3_step7_run_on_groundtruth.py --project Chart
  python exm3_step7_run_on_groundtruth.py --project Chart --ids 1,3,5-7
  python exm3_step7_run_on_groundtruth.py -j 4
  python exm3_step7_run_on_groundtruth.py --no-cleanup
"""

import argparse
import csv
import os
import platform
import re
import shlex
import shutil
import signal
import subprocess
import sys
import threading
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path
from typing import Dict, List, Optional, Set, Tuple

TIMEOUT_SECONDS = 600  # 10 min per bug

FIXED_RE     = re.compile(r"^([A-Za-z]+)-(\d+)-fixed$")
FIXED_IND_RE = re.compile(r"^([A-Za-z]+)-(\d+)-fixed_ind(\d+)$")
EXIT_CODE_RE = re.compile(r"^----\s*exit code:\s*(\d+)", re.MULTILINE)
METHOD_RE    = re.compile(r"\bpublic\s+void\s+(\w+)\s*\(")
TEST_RE_TRIGGER = re.compile(r"^[A-Za-z0-9_$.]+::[A-Za-z0-9_]+$")
ADD_TEST_GLOB   = "add_test*.java"
ADD_TEST_ID_FILE = ".add_test_id"

CSV_FIELDNAMES = ["project", "bug_id", "add_test_file", "class_fqcn", "method_name", "result"]

TEST_ROOTS = [
    "tests",
    "src/test/java",
    "src/test",
    "test",
    "src/tests",
]

# NOTE: the first segment must be [^\n]* (match only the rest of the command
# line), NOT .* — with re.DOTALL a greedy .*\n swallows the whole test section
# (incl. the "Failing tests:" lines), leaving group(1) empty so every passing
# compile is scored "Pass". Keep [^\n]* so (.*?) captures the section correctly.
_TEST_SECTION_RE = re.compile(
    r"^>>> defects4j test [^\n]*\n(.*?)^---- exit code: (\d+)",
    re.MULTILINE | re.DOTALL,
)


# -------------------- id parsing --------------------

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


# -------------------- checkout --------------------

def checkout_fixed(
    project: str,
    bug_id: str,
    defects_root: Path,
    timeout: int = 300,
) -> Tuple[bool, str]:
    """
    Run `defects4j checkout -p <project> -v <id>f -w <dir>`.
    Skips if target directory already exists.
    Returns (success, message).
    """
    fixed_dir = defects_root / project / f"{project}-{bug_id}-fixed"
    if fixed_dir.exists():
        return True, f"[SKIP] already exists: {fixed_dir}"

    fixed_dir.parent.mkdir(parents=True, exist_ok=True)
    cmd = ["defects4j", "checkout", "-p", project, "-v", f"{bug_id}f", "-w", str(fixed_dir)]
    try:
        result = subprocess.run(
            cmd,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            timeout=timeout,
        )
        if result.returncode == 0:
            return True, f"[CHECKOUT] {project}-{bug_id}-fixed"
        else:
            return False, f"[CHECKOUT FAILED] {project}-{bug_id}: {result.stdout.strip()[-300:]}"
    except subprocess.TimeoutExpired:
        return False, f"[CHECKOUT TIMEOUT] {project}-{bug_id} after {timeout}s"
    except Exception as e:
        return False, f"[CHECKOUT ERROR] {project}-{bug_id}: {e}"


# -------------------- testcase insertion (mirrors step1) --------------------

def parse_first_line_path(first_line: str) -> Optional[str]:
    line = first_line.strip()
    if not line.startswith("//"):
        return None
    path = line[2:].strip()
    if "::" in path:
        path = path.split("::")[0].strip()
    return path if path else None


def fqcn_to_rel_path(fqcn: str) -> str:
    return fqcn.replace(".", "/") + ".java"


def get_trigger_test_file(bug_dir: Path) -> Optional[str]:
    try:
        p = subprocess.run(
            ["defects4j", "export", "-p", "tests.trigger"],
            cwd=str(bug_dir),
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            timeout=30,
        )
        for tok in re.split(r"[,\s]+", (p.stdout or "").strip()):
            tok = tok.strip()
            if TEST_RE_TRIGGER.match(tok):
                return fqcn_to_rel_path(tok.split("::")[0])
    except Exception:
        pass
    return None


def find_class_closing_brace(lines: List[str]) -> Optional[int]:
    for i in range(len(lines) - 1, -1, -1):
        if lines[i].strip() == "}":
            return i
    return None


def insert_before_class_close(original: str, new_code: str) -> str:
    lines = original.splitlines(keepends=True)
    idx = find_class_closing_brace(lines)
    if idx is None:
        return original.rstrip() + "\n\n" + new_code.strip() + "\n}\n"
    lines.insert(idx, "\n" + new_code.rstrip() + "\n")
    return "".join(lines)


def insert_one_add_test(add_test_path: Path, target_dir: Path) -> Tuple[bool, str]:
    """
    Insert one add_test*.java into target_dir.
    Returns (success, message).
    """
    text = add_test_path.read_text(encoding="utf-8", errors="replace")
    lines_raw = text.splitlines()
    if not lines_raw:
        return False, "empty file"

    rel_path = parse_first_line_path(lines_raw[0])
    if not rel_path:
        return False, f"cannot parse path from first line: {lines_raw[0]!r}"

    code_to_insert = "\n".join(lines_raw[1:]).strip()
    if not code_to_insert:
        return False, "no code to insert"

    target_file = None
    for root in TEST_ROOTS:
        candidate = target_dir / root / rel_path
        if candidate.exists():
            target_file = candidate
            break

    if target_file is None:
        fallback = get_trigger_test_file(target_dir)
        if fallback:
            for root in TEST_ROOTS:
                candidate = target_dir / root / fallback
                if candidate.exists():
                    target_file = candidate
                    print(f"    [FALLBACK] {rel_path!r} -> trigger: {target_file}")
                    break

    if target_file is None:
        filename = rel_path.split("/")[-1]
        matches = list(target_dir.rglob(filename))
        if len(matches) == 1:
            target_file = matches[0]
            print(f"    [GLOB] found: {target_file}")
        elif len(matches) > 1:
            exact = [m for m in matches if str(m).replace("\\", "/").endswith(rel_path)]
            if exact:
                target_file = exact[0]

    if target_file is None:
        return False, f"target file not found: {rel_path} in {target_dir}"

    original = target_file.read_text(encoding="utf-8", errors="replace")
    updated = insert_before_class_close(original, code_to_insert)

    backup_file = target_file.with_suffix(target_file.suffix + ".orig")
    if not backup_file.exists():
        backup_file.write_text(original, encoding="utf-8")

    target_file.write_text(updated, encoding="utf-8")
    return True, str(target_file)


def copy_fixed_dir(src: Path, dst: Path) -> None:
    """Clone src -> dst. Uses APFS copy-on-write on macOS, shutil.copytree elsewhere."""
    if platform.system() == "Darwin":
        r = subprocess.run(
            ["cp", "-Rc", str(src), str(dst)],
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True,
        )
        if r.returncode != 0:
            raise RuntimeError(f"cp -Rc failed: {r.stdout.strip()}")
    else:
        shutil.copytree(str(src), str(dst))


def insert_all_add_tests(
    results_bug_dir: Path,
    project: str,
    bug_id: str,
    defects_root: Path,
) -> Tuple[int, int]:
    """
    Insert add_test*.java files into the fixed dir(s).

    - If N == 1: insert into original <project>-<id>-fixed only.
    - If N > 1:
        * Create individual copies <project>-<id>-fixed_ind1 ... fixed_indN
        * Insert one test per copy (write .add_test_id marker)
        * Also insert all tests into the original (combined)
    Returns (ok_count, failed_count).
    """
    fixed_dir = defects_root / project / f"{project}-{bug_id}-fixed"
    if not fixed_dir.is_dir():
        print(f"  [WARN] fixed dir not found, skipping insert: {fixed_dir}")
        return 0, 0

    add_files = sorted(results_bug_dir.glob(ADD_TEST_GLOB))
    if not add_files:
        return 0, 0

    ok = failed = 0

    # --- Individual copies (only when N > 1) ---
    if len(add_files) > 1:
        for k, af in enumerate(add_files, start=1):
            copy_name = f"{project}-{bug_id}-fixed_ind{k}"
            copy_dir  = fixed_dir.parent / copy_name
            if not copy_dir.exists():
                try:
                    copy_fixed_dir(fixed_dir, copy_dir)
                    print(f"  [COPY] {fixed_dir.name} -> {copy_name}")
                except Exception as e:
                    print(f"  [COPY-FAIL] {copy_name}: {e}")
                    failed += 1
                    continue
            else:
                print(f"  [INFO] individual copy already exists: {copy_name}")
            # Write marker
            (copy_dir / ADD_TEST_ID_FILE).write_text(af.name, encoding="utf-8")
            # Insert only this add_test
            success, msg = insert_one_add_test(af, copy_dir)
            if success:
                ok += 1
                print(f"  [IND-OK] {copy_name}/{af.name} -> {msg}")
            else:
                failed += 1
                print(f"  [IND-FAIL] {copy_name}/{af.name}: {msg}")

    # --- Combined: insert all add_tests into original fixed dir ---
    for af in add_files:
        success, msg = insert_one_add_test(af, fixed_dir)
        if success:
            ok += 1
            print(f"  [OK] {af.name} -> {msg}")
        else:
            failed += 1
            print(f"  [FAIL] {af.name}: {msg}")

    return ok, failed


# -------------------- test running --------------------

def run_one(
    bug_dir: Path,
    cmd: str,
    log_dir: Path,
    timeout: int,
) -> Tuple[str, int]:
    """Run ant clean + main test command. Returns (dir_name, rc)."""
    log_dir.mkdir(parents=True, exist_ok=True)
    log_file = log_dir / f"{bug_dir.name}.log"

    with open(log_file, "w", encoding="utf-8", buffering=1) as lf:

        def log_write(s: str):
            lf.write(s)
            lf.flush()

        header = f"==== {time.strftime('%F %T')} :: {bug_dir.name} ====\n"
        log_write(header)
        print(header, end="")

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

    return bug_dir.name, rc


# -------------------- result checking --------------------

def parse_first_line_full(first_line: str) -> Tuple[Optional[str], Optional[str]]:
    line = first_line.strip()
    if not line.startswith("//"):
        return None, None
    content = line[2:].strip()
    if "::" in content:
        path, method = content.split("::", 1)
        return path.strip() or None, method.strip() or None
    return content.strip() or None, None


def path_to_fqcn(rel_path: str) -> str:
    return rel_path.removesuffix(".java").replace("/", ".")


def test_in_failing_list(log_text: str, fqcn: str, method_name: str) -> bool:
    short_class = fqcn.split(".")[-1]
    for pat in (f"{fqcn}::{method_name}", f"{short_class}::{method_name}"):
        if pat in log_text:
            return True
    return False


def get_test_target(add_test_path: Path) -> Tuple[str, str]:
    """Parse (fqcn, method_name) from an add_test*.java file for use with -t."""
    if not add_test_path.exists():
        return "", ""
    text = add_test_path.read_text(encoding="utf-8", errors="replace")
    lines = text.splitlines()
    if not lines:
        return "", ""
    rel_path, method_name = parse_first_line_full(lines[0])
    if not method_name:
        mm = METHOD_RE.search("\n".join(lines[1:]))
        method_name = mm.group(1) if mm else ""
    fqcn = path_to_fqcn(rel_path) if rel_path else ""
    return fqcn, method_name


def check_add_tests(
    results_bug_dir: Path,
    project: str,
    bug_id: str,
    log_file: Path,
    only_add_test_file: Optional[str] = None,
) -> List[Dict]:
    """
    Check add_test*.java files against the log.
    only_add_test_file: if set, check only that specific file (individual mode).
    Returns list of result rows.
    """
    if only_add_test_file:
        candidate = results_bug_dir / only_add_test_file
        add_files = [candidate] if candidate.exists() else []
    else:
        add_files = sorted(results_bug_dir.glob(ADD_TEST_GLOB))
    if not add_files:
        return []

    full_log = log_file.read_text(encoding="utf-8", errors="replace") if log_file.exists() else ""

    test_section = ""
    exit_code: Optional[int] = None
    if full_log:
        m = _TEST_SECTION_RE.search(full_log)
        if m:
            test_section = m.group(1)
            exit_code = int(m.group(2))
        else:
            em = None
            for em in EXIT_CODE_RE.finditer(full_log):
                pass
            if em:
                exit_code = int(em.group(1))
            test_section = full_log

    rows = []
    for af in add_files:
        text = af.read_text(encoding="utf-8", errors="replace")
        lines = text.splitlines()
        if not lines:
            continue

        rel_path, method_name = parse_first_line_full(lines[0])
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


# -------------------- directory discovery --------------------

def discover_bug_ids(results_root: Path, project: str) -> List[str]:
    proj_dir = results_root / project
    if not proj_dir.is_dir():
        return []
    return sorted(
        [d.name for d in proj_dir.iterdir() if d.is_dir() and d.name.isdigit()],
        key=int,
    )


def find_fixed_dirs(defects_root: Path, project: str) -> List[Tuple[str, Path]]:
    """Return sorted (bug_id, fixed_dir) for original <Project>-<id>-fixed dirs."""
    proj_dir = defects_root / project
    if not proj_dir.is_dir():
        return []
    result = []
    for d in proj_dir.iterdir():
        if not d.is_dir():
            continue
        m = FIXED_RE.match(d.name)
        if m and m.group(1) == project:
            result.append((m.group(2), d))
    result.sort(key=lambda x: int(x[0]))
    return result


def find_individual_fixed_dirs(
    defects_root: Path, project: str
) -> List[Tuple[str, str, Path, str]]:
    """
    Return sorted (bug_id, ind_idx, fixed_ind_dir, add_test_filename)
    for <Project>-<id>-fixed_indK dirs.
    """
    proj_dir = defects_root / project
    if not proj_dir.is_dir():
        return []
    result = []
    for d in proj_dir.iterdir():
        if not d.is_dir():
            continue
        m = FIXED_IND_RE.match(d.name)
        if m and m.group(1) == project:
            bug_id  = m.group(2)
            ind_idx = m.group(3)
            marker  = d / ADD_TEST_ID_FILE
            add_test_name = marker.read_text(encoding="utf-8").strip() if marker.exists() else ""
            result.append((bug_id, ind_idx, d, add_test_name))
    result.sort(key=lambda x: (int(x[0]), int(x[1])))
    return result


# -------------------- main --------------------

def main():
    ap = argparse.ArgumentParser(
        description=(
            "Checkout defects4j fixed versions, insert add_test*.java (combined + individual), "
            "run tests, record Pass/Not Pass/Not Compilable, then delete fixed dirs."
        )
    )
    ap.add_argument(
        "--results-root", default=str(Path(__file__).resolve().parents[2] / "results/data/exm3/Claude"),
        help="Root containing <project>/<id>/add_test*.java (default: results/data/exm3/Claude)",
    )
    ap.add_argument(
        "--defects-root", default=str(Path(__file__).resolve().parents[2] / "defects4j_checkouts"),
        help="Root for checked-out projects (default: defects4j_checkouts)",
    )
    ap.add_argument(
        "--logs-root", default="test-logs/exm3/groundtruth",
        help="Root for log files (default: test-logs/exm3/groundtruth)",
    )
    ap.add_argument(
        "--out-dir", default=str(Path(__file__).resolve().parents[2] / "generated_evaluation/exm3_work/Claude/run_groundtruth"),
        help="Output dir for combined results (default: generated_evaluation/exm3_work/Claude/run_groundtruth)",
    )
    ap.add_argument(
        "--ind-out-dir", default=str(Path(__file__).resolve().parents[2] / "generated_evaluation/exm3_work/Claude/run_groundtruth_individual"),
        help="Output dir for individual results "
             "(default: generated_evaluation/exm3_work/Claude/run_groundtruth_individual)",
    )
    ap.add_argument("--project", default=None, help="Only process this project (e.g. Chart)")
    ap.add_argument("--ids", default=None, help="Only process these bug IDs, e.g. '1,3,5-7'")
    ap.add_argument("--cmd", default="defects4j test -r",
                    help="Test command (default: defects4j test -r)")
    ap.add_argument("-j", "--jobs", type=int, default=1,
                    help="Parallel workers for test runs (default: 1)")
    ap.add_argument(
        "--timeout", type=int, default=TIMEOUT_SECONDS,
        help=f"Timeout per bug in seconds (default: {TIMEOUT_SECONDS})",
    )
    ap.add_argument(
        "--checkout-timeout", type=int, default=300,
        help="Timeout for defects4j checkout (default: 300)",
    )
    ap.add_argument("--skip-checkout", action="store_true",
                    help="Skip checkout phase (dirs must already exist)")
    ap.add_argument("--skip-insert", action="store_true",
                    help="Skip insertion phase (tests must already be inserted)")
    ap.add_argument("--no-cleanup", action="store_true",
                    help="Do NOT delete checked-out fixed dirs after testing")
    args = ap.parse_args()
    results_root = Path(args.results_root).expanduser().resolve()
    defects_root = Path(args.defects_root).expanduser().resolve()
    logs_root    = Path(args.logs_root).expanduser().resolve()
    out_dir      = Path(args.out_dir).expanduser().resolve()
    ind_out_dir  = Path(args.ind_out_dir).expanduser().resolve()

    if not results_root.is_dir():
        sys.exit(f"[ERROR] results-root not found: {results_root}")
    defects_root.mkdir(parents=True, exist_ok=True)
    out_dir.mkdir(parents=True, exist_ok=True)
    ind_out_dir.mkdir(parents=True, exist_ok=True)

    id_filter: Optional[Set[str]] = parse_id_expr(args.ids) if args.ids else None

    projects: List[str] = []
    for d in sorted(results_root.iterdir()):
        if not d.is_dir():
            continue
        project = d.name
        if args.project and project.lower() != args.project.lower():
            continue
        if not discover_bug_ids(results_root, project):
            continue
        projects.append(project)

    if not projects:
        sys.exit("[ERROR] no projects found in results-root")

    print(f"results-root : {results_root}")
    print(f"defects-root : {defects_root}")
    print(f"logs-root    : {logs_root}")
    print(f"out-dir      : {out_dir}")
    print(f"ind-out-dir  : {ind_out_dir}")
    print(f"cmd          : {args.cmd}")
    print(f"jobs         : {args.jobs}")
    print(f"timeout      : {args.timeout}s")
    print(f"projects     : {', '.join(projects)}")
    print()

    # ------------------------------------------------------------------ #
    # Phase 1: Checkout fixed versions
    # ------------------------------------------------------------------ #
    if not args.skip_checkout:
        print("=" * 60)
        print("PHASE 1: Checkout fixed versions")
        print("=" * 60)
        for project in projects:
            for bug_id in discover_bug_ids(results_root, project):
                if id_filter and bug_id not in id_filter:
                    continue
                success, msg = checkout_fixed(project, bug_id, defects_root, args.checkout_timeout)
                print(f"  {msg}")
        print()

    # ------------------------------------------------------------------ #
    # Phase 2: Insert add_test*.java (combined + individual copies)
    # ------------------------------------------------------------------ #
    if not args.skip_insert:
        print("=" * 60)
        print("PHASE 2: Insert add_test*.java into fixed dirs")
        print("=" * 60)
        total_ins_ok = total_ins_fail = 0
        for project in projects:
            print(f"=== {project} ===")
            for bug_id in discover_bug_ids(results_root, project):
                if id_filter and bug_id not in id_filter:
                    continue
                results_bug_dir = results_root / project / bug_id
                ok, failed = insert_all_add_tests(results_bug_dir, project, bug_id, defects_root)
                if ok + failed > 0:
                    print(f"  [{project}-{bug_id}] inserted={ok} failed={failed}")
                total_ins_ok  += ok
                total_ins_fail += failed
        print(f"\n[INSERT DONE] inserted={total_ins_ok} failed={total_ins_fail}\n")

    # ------------------------------------------------------------------ #
    # Phase 3: Run tests
    # ------------------------------------------------------------------ #
    print("=" * 60)
    print("PHASE 3: Run tests on fixed dirs")
    print("=" * 60)

    # Combined tasks: (project, bug_id, fixed_dir)
    tasks: List[Tuple[str, str, Path]] = []
    # Individual tasks: (project, bug_id, fixed_ind_dir, add_test_filename, fqcn, method_name)
    ind_tasks: List[Tuple[str, str, Path, str, str, str]] = []

    for project in projects:
        for bug_id, fixed_dir in find_fixed_dirs(defects_root, project):
            if id_filter and bug_id not in id_filter:
                continue
            results_bug_dir = results_root / project / bug_id
            if not results_bug_dir.is_dir():
                continue
            if not any(results_bug_dir.glob(ADD_TEST_GLOB)):
                continue
            tasks.append((project, bug_id, fixed_dir))

        for bug_id, _idx, ind_dir, add_test_name in find_individual_fixed_dirs(defects_root, project):
            if id_filter and bug_id not in id_filter:
                continue
            if add_test_name:
                results_bug_dir = results_root / project / bug_id
                fqcn, method_name = get_test_target(results_bug_dir / add_test_name)
                ind_tasks.append((project, bug_id, ind_dir, add_test_name, fqcn, method_name))

    print(f"combined bugs  : {len(tasks)}")
    print(f"individual bugs: {len(ind_tasks)}")
    print()

    def worker(project: str, bug_dir: Path) -> Tuple[str, str, int]:
        log_dir = logs_root / project
        name, rc = run_one(bug_dir, args.cmd, log_dir, args.timeout)
        return project, name, rc

    def ind_worker(project: str, bug_dir: Path, fqcn: str, method_name: str) -> Tuple[str, str, int]:
        log_dir = logs_root / project
        if fqcn and method_name:
            cmd = f"defects4j test -t {fqcn}::{method_name}"
        else:
            cmd = args.cmd
        name, rc = run_one(bug_dir, cmd, log_dir, args.timeout)
        return project, name, rc

    run_results:     List[Tuple[str, str, int]] = []
    ind_run_results: List[Tuple[str, str, int]] = []

    # --- Combined ---
    if args.jobs > 1:
        with ThreadPoolExecutor(max_workers=args.jobs) as ex:
            futs = {ex.submit(worker, proj, bd): (proj, bid) for proj, bid, bd in tasks}
            for f in as_completed(futs):
                run_results.append(f.result())
    else:
        for proj, bid, bd in tasks:
            run_results.append(worker(proj, bd))

    # --- Individual: use -t <fqcn>::<method> to avoid interference from other tests ---
    if args.jobs > 1:
        with ThreadPoolExecutor(max_workers=args.jobs) as ex:
            futs = {ex.submit(ind_worker, proj, bd, fqcn, meth): (proj, bid, atf)
                    for proj, bid, bd, atf, fqcn, meth in ind_tasks}
            for f in as_completed(futs):
                ind_run_results.append(f.result())
    else:
        for proj, bid, bd, _atf, fqcn, meth in ind_tasks:
            ind_run_results.append(ind_worker(proj, bd, fqcn, meth))

    run_results.sort(key=lambda x: (x[0], x[1]))
    ind_run_results.sort(key=lambda x: (x[0], x[1]))

    print("\n=== Combined Test Run Summary ===")
    for proj, name, rc in run_results:
        status = "TIMEOUT" if rc == 124 else ("OK" if rc == 0 else f"FAIL({rc})")
        print(f"  {proj}/{name:50s} -> {status}")
    print(f"Combined total: {len(run_results)} | "
          f"failed: {sum(1 for _,_,rc in run_results if rc != 0)} | "
          f"timed out: {sum(1 for _,_,rc in run_results if rc == 124)}")

    print("\n=== Individual Test Run Summary ===")
    for proj, name, rc in ind_run_results:
        status = "TIMEOUT" if rc == 124 else ("OK" if rc == 0 else f"FAIL({rc})")
        print(f"  {proj}/{name:50s} -> {status}")
    print(f"Individual total: {len(ind_run_results)} | "
          f"failed: {sum(1 for _,_,rc in ind_run_results if rc != 0)}")
    print(f"Logs : {logs_root}")

    # ------------------------------------------------------------------ #
    # Phase 4: Collect and write results
    # ------------------------------------------------------------------ #
    print("\n=== Checking Combined Testcase Results ===")
    project_rows: Dict[str, List[Dict]] = {}
    for project, bug_id, fixed_dir in tasks:
        results_bug_dir = results_root / project / bug_id
        log_file = logs_root / project / f"{fixed_dir.name}.log"
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
        total += len(rows); passed += n_pass; not_pass += n_fail; not_compilable += n_nc
        print(f"  [{project}] {len(rows)} | pass={n_pass} not_pass={n_fail} "
              f"not_compilable={n_nc} -> {out_file.name}")
    print(f"\n[DONE combined] total={total} pass={passed} not_pass={not_pass} "
          f"not_compilable={not_compilable}")
    print(f"CSVs : {out_dir}")

    print("\n=== Checking Individual Testcase Results ===")
    ind_project_rows: Dict[str, List[Dict]] = {}
    for project, bug_id, ind_dir, add_test_name, _fqcn, _method_name in ind_tasks:
        results_bug_dir = results_root / project / bug_id
        log_file = logs_root / project / f"{ind_dir.name}.log"
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
        ind_total += len(rows); ind_passed += n_pass
        ind_not_pass += n_fail; ind_not_compilable += n_nc
        print(f"  [{project}] {len(rows)} | pass={n_pass} not_pass={n_fail} "
              f"not_compilable={n_nc} -> {out_file.name}")
    print(f"\n[DONE individual] total={ind_total} pass={ind_passed} "
          f"not_pass={ind_not_pass} not_compilable={ind_not_compilable}")
    print(f"CSVs : {ind_out_dir}")

    # ------------------------------------------------------------------ #
    # Phase 5: Cleanup — delete checked-out fixed dirs (combined + individual)
    # ------------------------------------------------------------------ #
    if not args.no_cleanup:
        print("\n" + "=" * 60)
        print("PHASE 5: Cleanup — deleting fixed dirs")
        print("=" * 60)
        deleted = skipped = 0

        dirs_to_delete: List[Path] = (
            [bd for _, _, bd in tasks] +
            [bd for _, _, bd, _, _, _ in ind_tasks]
        )
        for d in dirs_to_delete:
            if d.exists():
                try:
                    shutil.rmtree(d)
                    print(f"  [DEL] {d}")
                    deleted += 1
                except Exception as e:
                    print(f"  [DEL-FAIL] {d}: {e}")
                    skipped += 1
            else:
                skipped += 1

        # Remove empty project dirs
        for project in projects:
            proj_dir = defects_root / project
            if proj_dir.is_dir() and not any(proj_dir.iterdir()):
                try:
                    proj_dir.rmdir()
                    print(f"  [DEL] empty project dir: {proj_dir}")
                except Exception:
                    pass

        print(f"\n[CLEANUP DONE] deleted={deleted} skipped={skipped}")


if __name__ == "__main__":
    main()
