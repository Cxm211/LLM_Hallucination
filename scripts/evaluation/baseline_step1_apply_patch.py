#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Apply the baseline patches to their Defects4J checkouts.

For every bug it reads <patches-root>/<id>/patch*.java and writes each patch back over the
line range of the corresponding buggy method, taken from <project>_methods.csv. The original
file is saved as <file>.bak first, so --restore puts everything back.

Line ranges are inclusive at both ends. When one file holds several patched methods they are
replaced from the bottom up, so earlier replacements cannot shift the line numbers of later
ones.

Required CSV columns:
  project, bug_id, folder, file, buggy_method_start_line, buggy_method_end_line

Example:
  python3 baseline_step1_apply_patch.py \
    --csv bug_methods/csv_methods.csv \
    --patches-root results/data/baseline/Claude/Csv \
    --projects-root defects4j_checkouts \
    --project Csv --ids 1,3,5-7
"""

import argparse
import re
import textwrap
from pathlib import Path
from typing import Optional, Tuple, Iterable, Set, Dict, List

COMMON_SOURCE_ROOTS = [
    "source",
    "src/main/java",
    "src/java",
    "src",
    "gson/src/main/java",
    "core/src/main/java",
    "main/src/main/java",
]


def load_rows(csv_path: Path):
    import pandas as pd
    df = pd.read_csv(csv_path)
    df["func_index_in_bug"] = df.groupby(["project", "bug_id"]).cumcount()
    return df


def parse_projects_arg(values: Optional[Iterable[str]]) -> Optional[Set[str]]:
    if not values:
        return None
    s: Set[str] = set()
    for v in values:
        for token in str(v).split(","):
            token = token.strip()
            if token:
                s.add(token)
    return s or None


def parse_bugs_arg(expr: Optional[str]) -> Optional[Set[int]]:
    if not expr:
        return None
    result: Set[int] = set()
    for part in str(expr).split(","):
        part = part.strip()
        if not part:
            continue
        m = re.match(r'^(\d+)\s*-\s*(\d+)$', part)
        if m:
            a, b = int(m.group(1)), int(m.group(2))
            if a > b:
                a, b = b, a
            result.update(range(a, b + 1))
        else:
            result.add(int(part))
    return result or None


# ================== patch I/O and replacement ==================

def find_patch_text_by_index(patches_root: Path, bug_key: str, func_index: int) -> Tuple[Optional[str], str]:
    bug_dir = patches_root / bug_key
    if not bug_dir.exists():
        return None, f"Patch dir not found: {bug_dir}"

    candidates = ["patch.java", "patch0.java"] if func_index == 0 else [f"patch{func_index}.java"]
    for name in candidates:
        chosen = bug_dir / name
        if chosen.exists():
            try:
                return chosen.read_text(encoding="utf-8"), str(chosen)
            except UnicodeDecodeError:
                return chosen.read_text(errors="ignore"), str(chosen)
    tried = ", ".join(candidates)
    return None, f"Expected patch file not found in {bug_dir} (tried: {tried})"


def reindent_block(text: str, indent: str) -> str:
    text = text.replace("\r\n", "\n").replace("\r", "\n")
    dedented = textwrap.dedent(text).strip("\n")
    lines = dedented.split("\n")
    return "\n".join((indent + line if (line != "" or indent) else line) for line in lines)


def replace_range_in_file(file_path: Path, start_line_1b: int, end_line_1b: int,
                          new_block: str, dry_run: bool = False) -> Tuple[bool, str]:
    if not file_path.exists():
        return False, f"Target file not found: {file_path}"

    content = file_path.read_text(encoding="utf-8").replace("\r\n", "\n").replace("\r", "\n")
    lines = content.split("\n")
    n = len(lines)
    if start_line_1b < 1 or end_line_1b < start_line_1b or end_line_1b > n:
        return False, f"Invalid range {start_line_1b}-{end_line_1b} for file with {n} lines: {file_path}"

    start_idx = start_line_1b - 1
    end_idx = end_line_1b - 1

    first_line = lines[start_idx] if start_idx < n else ""
    indent = re.match(r"\s*", first_line).group(0) or ""

    new_block_reindented = reindent_block(new_block, indent)
    new_lines = lines[:start_idx] + new_block_reindented.split("\n") + lines[end_idx + 1:]
    new_content = "\n".join(new_lines)
    changed = (new_content != content)

    if changed and not dry_run:
        backup = file_path.with_suffix(file_path.suffix + ".bak")
        if not backup.exists():
            backup.write_text(content, encoding="utf-8")
        file_path.write_text(new_content, encoding="utf-8")

    msg = f"{'DRY-RUN would patch' if dry_run else 'Patched'} {file_path} lines {start_line_1b}-{end_line_1b} (inclusive)"
    return changed, msg


def resolve_target_file(projects_root: Path, project: str, folder: str, rel_file: str) -> Optional[Path]:
    base = projects_root / project / folder
    for root in [""] + COMMON_SOURCE_ROOTS:
        candidate = (base / root / rel_file) if root else (base / rel_file)
        if candidate.exists():
            return candidate
    return None


def restore_from_backup(file_path: Path, dry_run: bool = False, delete_backup: bool = False) -> Tuple[bool, str]:
    bak = file_path.with_suffix(file_path.suffix + ".bak")
    if not bak.exists():
        return False, f"No backup (.bak) next to file: {file_path}"
    if dry_run:
        return True, f"DRY-RUN would restore {file_path} from {bak.name}"
    content = bak.read_text(encoding="utf-8")
    file_path.write_text(content, encoding="utf-8")
    if delete_backup:
        try:
            bak.unlink()
        except Exception:
            pass
    return True, f"Restored {file_path} from {bak.name}"


# ================== main ==================

def main():
    parser = argparse.ArgumentParser(
        description="Apply baseline patches to their Defects4J checkouts, or restore from backup.")
    parser.add_argument("--csv", required=True, help="Path to <project>_methods.csv")
    parser.add_argument("--patches-root", help="Patch root; its subdirectories are numeric bug ids such as 1, 2, 16")
    parser.add_argument("--projects-root", required=True, help="Root holding the Defects4J checkouts, e.g. defects4j_checkouts")
    parser.add_argument("--project", action="append", help="Restrict to these projects; repeatable or comma separated. Omit for all.")
    parser.add_argument("--bugs", help="Bug ids such as '1-10' or '1,3,5-7'. Omit for all.")
    parser.add_argument("--ids", help="Bug ids; a synonym for --bugs, used by the run scripts. Omit for all.")
    parser.add_argument("--dry-run", action="store_true", help="Report what would change without writing any file")
    parser.add_argument("--restore", action="store_true", help="Restore from .bak instead of applying patches")
    parser.add_argument("--delete-backup", action="store_true", help="With --restore, delete the .bak once it has been restored")
    args = parser.parse_args()

    csv_path = Path(args.csv)
    projects_root = Path(args.projects_root)
    df = load_rows(csv_path)

    selected_projects = parse_projects_arg(args.project)
    selected_bugs = parse_bugs_arg(args.bugs)
    selected_ids = parse_bugs_arg(args.ids)

    if selected_projects:
        df = df[df["project"].isin(selected_projects)].copy()

    def bug_selected(bug_id: int) -> bool:
        if selected_ids and bug_id not in selected_ids:
            return False
        if selected_bugs and bug_id not in selected_bugs:
            return False
        return True

    total_methods = 0
    changed_methods = 0
    failures = []

    # ---------------- RESTORE ----------------
    if args.restore:
        for _, row in df.iterrows():
            project = str(row["project"])
            bug_id = int(row["bug_id"])
            if not bug_selected(bug_id):
                continue

            folder = str(row["folder"])
            file_rel = str(row["file"]).lstrip("/")

            target_file = resolve_target_file(projects_root, project, folder, file_rel)
            if target_file is None:
                failures.append((bug_id, f"{projects_root}/{project}/{folder}/{file_rel}",
                                 "Target file not found in common source roots"))
                print(f"[WARN] Could not resolve file for bug {bug_id}: {file_rel}")
                continue

            ok, msg = restore_from_backup(target_file, dry_run=args.dry_run, delete_backup=args.delete_backup)
            print(msg)
            total_methods += 1
            if ok:
                changed_methods += 1
            else:
                failures.append((bug_id, str(target_file), msg))

        print(f"\n[RESTORE] Processed {total_methods} file(s). Restored {changed_methods} file(s).")
        if failures:
            print("\nFailures / Skips:")
            for bug_id, path, reason in failures:
                print(f"  - Bug {bug_id}: {path}\n    Reason: {reason}")
        return

    # ---------------- PATCH ----------------
    if not args.patches_root:
        parser.error("--patches-root is required unless --restore is specified")
    patches_root = Path(args.patches_root)

    grouped = df.groupby(["project", "bug_id"], sort=False)

    for (project, bug_id), sub in grouped:
        bug_id = int(bug_id)
        if not bug_selected(bug_id):
            continue

        bug_key = str(bug_id)  # the directory name is the plain numeric bug id

        per_file: Dict[Path, List] = {}
        for _, row in sub.iterrows():
            folder = str(row["folder"])
            file_rel = str(row["file"]).lstrip("/")
            func_idx = int(row["func_index_in_bug"])

            target_file = resolve_target_file(projects_root, project, folder, file_rel)
            if target_file is None:
                failures.append((bug_id, f"{projects_root}/{project}/{folder}/{file_rel}",
                                 "Target file not found in common source roots"))
                print(f"[WARN] Could not resolve file for bug {bug_id}: {file_rel}")
                continue

            patch_text, which = find_patch_text_by_index(patches_root, bug_key, func_idx)
            if not patch_text:
                failures.append((bug_id, str(target_file), which))
                print(f"[WARN] {which}")
                continue

            start_line = int(row["buggy_method_start_line"])
            end_line = int(row["buggy_method_end_line"])

            per_file.setdefault(target_file, []).append(
                (start_line, end_line, patch_text, which, func_idx)
            )

        for file_path, patches in per_file.items():
            for start, end, text, which, idx in sorted(patches, reverse=True):
                total_methods += 1
                ok, msg = replace_range_in_file(file_path, start, end, text, dry_run=args.dry_run)
                print(f"{msg}  [patch: {which} | func_index={idx}]")
                if ok:
                    changed_methods += 1

    print(f"\n[PATCH] Processed {total_methods} method(s). Changed {changed_methods} patch(es) across file(s).")
    if failures:
        print("\nFailures / Skips:")
        for bug_id, path, reason in failures:
            print(f"  - Bug {bug_id}: {path}\n    Reason: {reason}")


if __name__ == "__main__":
    main()
