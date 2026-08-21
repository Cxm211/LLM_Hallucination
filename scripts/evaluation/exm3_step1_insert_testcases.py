#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
exm3_step1_insert_testcases.py

For each add_test*.java in <results-root>/<project>/<id>/:
  1. Read first line: // <path>::<method>  (strip :: part to get file path)
  2. Find target file in <project>-<id>-buggy/ under common test roots:
       tests/, src/test/java/, src/test/, test/, src/tests/
  3. Insert the testcase code (lines 2+) before the class's closing }

Usage:
  python exm3_step1_insert_testcases.py \
    --results-root results/data/exm3/Claude \
    --defects-root defects4j_checkouts \
    [--project Chart] \
    [--ids 1,3,5-7] \
    [--dry-run]
python exm3_step1_insert_testcases.py --project Chart --dry-run
"""

import argparse
import re
import shutil
import subprocess
import sys
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path
from typing import Optional, List, Tuple

ADD_TEST_GLOB = "add_test*.java"
ADD_TEST_ID_FILE = ".add_test_id"  # marker file in individual copy dirs

# Candidate test source roots, tried in order
TEST_ROOTS = [
    "tests",
    "src/test/java",
    "src/test",
    "test",
    "src/tests",
]


# -------------------- helpers --------------------

def parse_id_expr(expr: str) -> set:
    ids = set()
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


def parse_first_line(first_line: str) -> Optional[str]:
    """
    '// org/jfree/chart/plot/junit/XYPlotTests.java::testFoo'
    -> 'org/jfree/chart/plot/junit/XYPlotTests.java'
    Also handles lines without :: (plain path).
    """
    line = first_line.strip()
    if not line.startswith("//"):
        return None
    path = line[2:].strip()
    # strip ::method suffix if present
    if "::" in path:
        path = path.split("::")[0].strip()
    return path if path else None


TEST_RE = re.compile(r"^[A-Za-z0-9_$.]+::[A-Za-z0-9_]+$")


def fqcn_to_rel_path(fqcn: str) -> str:
    """'com.foo.BarTest' -> 'com/foo/BarTest.java'"""
    return fqcn.replace(".", "/") + ".java"


def get_trigger_test_file(bug_dir: Path) -> Optional[str]:
    """
    Run defects4j export -p tests.trigger and return the rel path of the first
    trigger test class, e.g. 'com/foo/BarTest.java'.
    Returns None on failure.
    """
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
            if TEST_RE.match(tok):
                fqcn = tok.split("::")[0]
                return fqcn_to_rel_path(fqcn)
    except Exception:
        pass
    return None


def find_class_closing_brace(lines: List[str]) -> Optional[int]:
    """
    Returns the index of the last line that is just '}' (the class closing brace).
    Searches from the end of the file.
    """
    for i in range(len(lines) - 1, -1, -1):
        if lines[i].strip() == "}":
            return i
    return None


def insert_before_class_close(original: str, new_code: str) -> str:
    """
    Insert new_code before the last '}' (class closing brace) in original.
    Adds a blank line separator before the inserted code.
    """
    lines = original.splitlines(keepends=True)
    idx = find_class_closing_brace(lines)
    if idx is None:
        # Fallback: just append
        return original.rstrip() + "\n\n" + new_code.strip() + "\n}\n"

    # Ensure new_code ends with newline
    code_block = "\n" + new_code.rstrip() + "\n"
    lines.insert(idx, code_block)
    return "".join(lines)


# -------------------- core --------------------

def process_add_test_file(
    add_test_path: Path,
    project: str,
    bug_id: str,
    defects_root: Path,
    dry_run: bool,
    buggy_dir_override: Optional[Path] = None,
) -> Tuple[bool, str]:
    """
    Process one add_test*.java file.
    Returns (success, message).
    buggy_dir_override: if set, use this dir instead of the default <project>-<id>-buggy.
    """
    text = add_test_path.read_text(encoding="utf-8", errors="replace")
    lines_raw = text.splitlines()
    if not lines_raw:
        return False, "empty file"

    first_line = lines_raw[0]
    rel_path = parse_first_line(first_line)
    if not rel_path:
        return False, f"cannot parse path from first line: {first_line!r}"

    code_to_insert = "\n".join(lines_raw[1:]).strip()
    if not code_to_insert:
        return False, "no code to insert (file has only one line)"

    # Locate target file by trying multiple common test source roots
    bug_dir = buggy_dir_override if buggy_dir_override is not None else (
        defects_root / project / f"{project}-{bug_id}-buggy"
    )
    target_file = None
    for root in TEST_ROOTS:
        candidate = bug_dir / root / rel_path
        if candidate.exists():
            target_file = candidate
            break
    if target_file is None:
        # Fallback: derive target from defects4j trigger test class
        fallback_path = get_trigger_test_file(bug_dir)
        if fallback_path:
            for root in TEST_ROOTS:
                candidate = bug_dir / root / fallback_path
                if candidate.exists():
                    target_file = candidate
                    print(f"    [FALLBACK] {rel_path!r} not found; using trigger test file: {target_file}")
                    break
    if target_file is None:
        # Last-resort: glob search anywhere under bug_dir
        filename = rel_path.split("/")[-1]
        matches = list(bug_dir.rglob(filename))
        if len(matches) == 1:
            target_file = matches[0]
            print(f"    [GLOB] {rel_path!r} not in standard roots; found via glob: {target_file}")
        elif len(matches) > 1:
            # Pick the one whose path ends with rel_path
            exact = [m for m in matches if str(m).replace("\\", "/").endswith(rel_path)]
            if exact:
                target_file = exact[0]
                print(f"    [GLOB] {rel_path!r} matched via glob: {target_file}")
    if target_file is None:
        tried = ", ".join(TEST_ROOTS)
        return False, f"target file not found: {rel_path} (tried roots: {tried}) in {bug_dir}"

    original = target_file.read_text(encoding="utf-8", errors="replace")
    updated = insert_before_class_close(original, code_to_insert)

    if dry_run:
        print(f"    [DRY-RUN] would insert {len(code_to_insert.splitlines())} lines into {target_file}")
        return True, "dry-run"

    # Backup original before first insertion (do not overwrite existing backup)
    backup_file = target_file.with_suffix(target_file.suffix + ".orig")
    if not backup_file.exists():
        backup_file.write_text(original, encoding="utf-8")

    target_file.write_text(updated, encoding="utf-8")
    return True, str(target_file)


def process_bug(
    bug_dir: Path,
    project: str,
    bug_id: str,
    defects_root: Path,
    dry_run: bool,
) -> Tuple[int, int, List[Tuple[str, str, str]]]:
    """
    Process all add_test*.java files in one bug results dir.
    If N > 1 add_tests: first create N individual buggy copies (one test each),
    then insert all tests into the original buggy dir (combined).
    Returns (ok, failed, fail_details).
    """
    add_files = sorted(bug_dir.glob(ADD_TEST_GLOB))
    if not add_files:
        return 0, 0, []

    ok = failed = 0
    fail_details: List[Tuple[str, str, str]] = []

    # --- Individual copies (only when N > 1) ---
    if len(add_files) > 1:
        buggy_dir = defects_root / project / f"{project}-{bug_id}-buggy"
        if buggy_dir.is_dir():
            for k, af in enumerate(add_files, start=1):
                copy_name = f"{project}-{bug_id}-buggy_ind{k}"
                copy_dir = buggy_dir.parent / copy_name
                if not copy_dir.exists():
                    if dry_run:
                        print(f"  [DRY-RUN] would copy {buggy_dir.name} -> {copy_name}")
                    else:
                        # Use APFS clone (cp -Rc) on macOS for near-zero disk usage;
                        # fall back to shutil.copytree on other platforms.
                        import platform
                        if platform.system() == "Darwin":
                            r = subprocess.run(
                                ["cp", "-Rc", str(buggy_dir), str(copy_dir)],
                                stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True,
                            )
                            if r.returncode != 0:
                                raise RuntimeError(f"cp -Rc failed: {r.stdout.strip()}")
                        else:
                            shutil.copytree(str(buggy_dir), str(copy_dir))
                        print(f"  [COPY] {buggy_dir.name} -> {copy_name}")
                else:
                    print(f"  [INFO] individual copy already exists: {copy_name}")
                # Write .add_test_id marker file
                if not dry_run and copy_dir.exists():
                    (copy_dir / ADD_TEST_ID_FILE).write_text(af.name, encoding="utf-8")
                # Insert only this add_test into the copy
                success, msg = process_add_test_file(
                    af, project, bug_id, defects_root, dry_run,
                    buggy_dir_override=copy_dir if copy_dir.exists() else None,
                )
                if success:
                    ok += 1
                    print(f"  [IND-OK] {copy_name}/{af.name} -> {msg}")
                else:
                    failed += 1
                    fail_details.append((project, bug_id, f"[ind{k}] {af.name}: {msg}"))
                    print(f"  [IND-FAIL] {copy_name}/{af.name}: {msg}")
        else:
            print(f"  [WARN] buggy dir not found for individual copies: {buggy_dir}")

    # --- Combined: insert all add_tests into original buggy dir ---
    for af in add_files:
        success, msg = process_add_test_file(af, project, bug_id, defects_root, dry_run)
        if success:
            ok += 1
            print(f"  [OK] {af.name} -> {msg}")
        else:
            failed += 1
            fail_details.append((project, bug_id, f"{af.name}: {msg}"))
            print(f"  [FAIL] {af.name}: {msg}")
    return ok, failed, fail_details


# -------------------- main --------------------

def main():
    ap = argparse.ArgumentParser(
        description="Insert add_test*.java testcases into defects4j_checkouts target files."
    )
    ap.add_argument(
        "--results-root", default=str(Path(__file__).resolve().parents[2] / "results/data/exm3/Claude"),
        help="Root containing <project>/<id>/add_test*.java (default: results/data/exm3/Claude)",
    )
    ap.add_argument(
        "--defects-root", default=str(Path(__file__).resolve().parents[2] / "defects4j_checkouts"),
        help="Root containing <project>/<project>-<id>-buggy/ (default: defects4j_checkouts)",
    )
    ap.add_argument("--project", default=None, help="Only process this project (e.g. Chart)")
    ap.add_argument("--ids", default=None, help="Only process these bug IDs, e.g. '1,3,5-7'")
    ap.add_argument("--dry-run", action="store_true", help="Preview only, do not write files")
    ap.add_argument("-j", "--jobs", type=int, default=4, help="Parallel workers per project (default: 4)")
    args = ap.parse_args()
    results_root = Path(args.results_root).expanduser().resolve()
    defects_root = Path(args.defects_root).expanduser().resolve()

    if not results_root.is_dir():
        sys.exit(f"[ERROR] results-root not found: {results_root}")
    if not defects_root.is_dir():
        sys.exit(f"[ERROR] defects-root not found: {defects_root}")

    id_filter = parse_id_expr(args.ids) if args.ids else None

    total_ok = total_failed = 0
    all_failures: List[Tuple[str, str, str]] = []

    for proj_dir in sorted(results_root.iterdir()):
        if not proj_dir.is_dir():
            continue
        project = proj_dir.name
        if args.project and project.lower() != args.project.lower():
            continue

        print(f"=== {project} ===")

        for bug_dir in sorted(proj_dir.iterdir(), key=lambda p: (int(p.name) if p.name.isdigit() else float('inf'), p.name)):
            if not bug_dir.is_dir():
                continue
            bug_id = bug_dir.name
            if id_filter and bug_id not in id_filter:
                continue

            ok, failed, fail_details = process_bug(bug_dir, project, bug_id, defects_root, args.dry_run)
            if ok + failed > 0:
                print(f"  [{project}-{bug_id}] ok={ok} failed={failed}")
            total_ok += ok
            total_failed += failed
            all_failures.extend(fail_details)

    print(f"\n[DONE] inserted={total_ok} failed={total_failed}")
    if all_failures:
        print(f"\n[FAILED LIST] ({total_failed} total):")
        for proj, bid, detail in all_failures:
            print(f"  {proj}-{bid}: {detail}")
    if args.dry_run:
        print("[DRY-RUN] no files were modified.")


if __name__ == "__main__":
    main()
