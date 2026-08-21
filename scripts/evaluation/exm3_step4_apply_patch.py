#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Apply patches into the corresponding Defects4J buggy projects, and ALSO emit
per-project CSVs that report the FIXED method start/end lines after patching.

Key behaviors:
- Variants (e.g., 2_1, 2_2) are COPIED from the CLEAN baseline <project>-<id>-buggy
  BEFORE any patching; then each (baseline and every variant) receives ONLY ITS OWN patch.
- Range replacement is inclusive for both start and end lines.
- When multiple patches hit the same file, fixed ranges are computed by tracking
  line-count deltas in ascending start-line order (so offsets are correct).
- Per project: writes "<project_lower>_methods.csv" to --fixed-out-dir.

Expected input CSV columns:
  project, bug_id, folder, file, buggy_method_start_line, buggy_method_end_line
Optional columns (copied if present, otherwise blank):
  class_entry, method_sig_snippet

Output columns (fixed CSV):
  project, bug_id, folder, class_entry, file, method_sig_snippet,
  fixed_method_start_line, fixed_method_end_line

Example:
python exm3_step4_apply_patch.py \
  --patches-root results/data/exm3/Claude/Chart \
  --projects-root defects4j_checkouts \
  --project Chart \
  --dry-run
"""

import argparse
import csv
import re
import shutil
import textwrap
from dataclasses import dataclass
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
        m = re.match(r"^(\d+)\s*-\s*(\d+)$", part)
        if m:
            a, b = int(m.group(1)), int(m.group(2))
            if a > b:
                a, b = b, a
            result.update(range(a, b + 1))
        else:
            result.add(int(part))
    return result or None


# ================== Variant scan & copy ==================

_VARIANT_DIR_RE = re.compile(r"^(\d+)(?:_(\d+))?$")
_IND_BUGGY_RE = re.compile(r"^([A-Za-z]+)-(\d+)-buggy_ind(\d+)$")


@dataclass(frozen=True)
class Variant:
    bug_id: int
    suffix: Optional[str]  # None for base, otherwise "1","2",...
    key: str               # directory name in patches_root, e.g., "2" or "2_1"


def scan_variants(patches_root: Path) -> Dict[int, List[Variant]]:
    mapping: Dict[int, List[Variant]] = {}
    for child in patches_root.iterdir():
        if not child.is_dir():
            continue
        m = _VARIANT_DIR_RE.match(child.name)
        if not m:
            continue
        bid = int(m.group(1))
        suf = m.group(2)
        v = Variant(bug_id=bid, suffix=suf, key=child.name)
        mapping.setdefault(bid, [])
        if v not in mapping[bid]:
            mapping[bid].append(v)

    # base first, then 1,2,...
    for bid, variants in mapping.items():
        mapping[bid] = sorted(
            variants,
            key=lambda x: (x.suffix is not None, int(x.suffix) if x.suffix else -1),
        )
    return mapping


def ensure_variant_copy(projects_root: Path, project: str, bug_id: int, suffix: str) -> Tuple[bool, str, Path]:
    base = projects_root / project / f"{project}-{bug_id}-buggy"
    target = projects_root / project / f"{project}-{bug_id}_{suffix}-buggy"

    if not base.exists():
        return False, f"[ERROR] Base folder not found: {base}", target
    if target.exists():
        return False, f"[INFO] Variant folder already exists: {target}", target

    shutil.copytree(base, target)
    return True, f"[INFO] Copied CLEAN baseline to variant: {base.name} -> {target.name}", target


def rewrite_folder_for_variant(folder: str, project: str, bug_id: int, suffix: Optional[str]) -> str:
    if not suffix:
        return folder
    pattern = f"{project}-{bug_id}-buggy"
    repl = f"{project}-{bug_id}_{suffix}-buggy"
    return folder.replace(pattern, repl)


# ================== Patch I/O & replacement ==================

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


def replace_range_in_file(
    file_path: Path,
    start_line_1b: int,
    end_line_1b: int,
    new_block: str,
    dry_run: bool = False,
) -> Tuple[bool, str]:
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
    new_lines = lines[:start_idx] + new_block_reindented.split("\n") + lines[end_idx + 1 :]
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


# ================== Fixed-range computation ==================

def patch_line_count(text: str) -> int:
    text = text.replace("\r\n", "\n").replace("\r", "\n")
    dedented = textwrap.dedent(text).strip("\n")
    if dedented == "":
        return 1
    return len(dedented.split("\n"))


def compute_fixed_ranges(patches: List[Tuple[int, int, str, str, int]]) -> Dict[int, Tuple[int, int]]:
    """patches: [(start, end, patch_text, which, func_idx), ...] -> func_idx -> (fixed_start, fixed_end)"""
    items = []
    for start, end, text, which, func_idx in patches:
        old_len = end - start + 1
        new_len = patch_line_count(text)
        items.append((start, end, old_len, new_len, func_idx))
    items.sort(key=lambda x: x[0])

    delta = 0
    out: Dict[int, Tuple[int, int]] = {}
    for start, end, old_len, new_len, func_idx in items:
        fixed_start = start + delta
        fixed_end = fixed_start + new_len - 1
        out[func_idx] = (fixed_start, fixed_end)
        delta += (new_len - old_len)
    return out


def resolve_fixed_csv_path(
    project: str,
    fixed_out_dir: Optional[str],
    fixed_out_file: Optional[str],
    multi_project: bool,
) -> Path:
    if fixed_out_file:
        if multi_project:
            raise SystemExit("[error] --fixed-out-file only works for a single project. "
                         "Use --fixed-out-dir instead.")
        return Path(fixed_out_file).expanduser()
    if fixed_out_dir:
        return Path(fixed_out_dir).expanduser() / f"{project.lower()}_methods.csv"
    return Path(f"{project.lower()}_fixed_methods.csv")


# ================== Main ==================

def main():
    parser = argparse.ArgumentParser(
        description="Apply patches (variants copied from CLEAN baseline first), or RESTORE from backups."
    )
    parser.add_argument("--csv", default=None, help="Path to <project>_methods.csv. If omitted, auto-found from --methods-dir + --project.")
    parser.add_argument("--methods-dir", default=str(Path(__file__).resolve().parent / "bug_methods"), help="Dir containing *_methods.csv (default: <script dir>/bug_methods)")
    parser.add_argument("--patches-root", help="Root that contains patches; subdirs can be '2', '2_1', '4_2', ...")
    parser.add_argument("--projects-root", required=True, help="Root with Defects4J projects, e.g., defects4j_checkouts")
    parser.add_argument("--project", action="append", help="Project(s) to include (repeat or comma-separated). Omit = all.")
    parser.add_argument("--bugs", help="Bug id selection like '1-10' or '1,3,5-7'. Omit = all.")
    parser.add_argument("--ids", help="Bug ids selection like '1-10' or '1,3,5-7'. Omit = all.")
    parser.add_argument("--dry-run", action="store_true", help="Show what would change without writing")
    parser.add_argument("--restore", action="store_true", help="Restore from .bak instead of applying patches")
    parser.add_argument("--delete-backup", action="store_true", help="(restore) delete .bak after successful restore")
    parser.add_argument("--fixed-out-dir", default=str(Path(__file__).resolve().parents[2] / "generated_evaluation/exm3_work/Claude/fixed_methods"),
                        help="Directory to write per-project fixed-range CSVs (default: generated_evaluation/exm3_work/Claude/fixed_methods)")
    parser.add_argument("--fixed-out-file", default=None,
                        help="Single fixed CSV output path (only safe for single-project runs)")

    args = parser.parse_args()
    projects_root = Path(args.projects_root).expanduser()
    methods_dir = Path(args.methods_dir).expanduser().resolve()

    # Resolve CSV path
    if args.csv:
        csv_path = Path(args.csv).expanduser()
    else:
        selected_projects = parse_projects_arg(args.project)
        if not selected_projects:
            parser.error("--csv or --project must be specified so the methods CSV can be located")
        proj_name = next(iter(selected_projects))
        csv_path = methods_dir / f"{proj_name.lower()}_methods.csv"
        if not csv_path.exists():
            parser.error(f"Auto-derived CSV not found: {csv_path}. Use --csv to specify explicitly.")

    df = load_rows(csv_path)

    selected_projects = parse_projects_arg(args.project)
    selected_bugs = parse_bugs_arg(args.bugs)
    selected_ids = parse_bugs_arg(args.ids)

    if selected_projects:
        df = df[df["project"].isin(selected_projects)].copy()

    unique_projects = sorted({str(p) for p in df["project"].unique().tolist()})
    multi_project = len(unique_projects) > 1

    fixed_reports: Dict[str, List[Dict]] = {}
    total_patches = 0
    changed_patches = 0
    failures: List[Tuple[int, str, str]] = []

    # ========== RESTORE MODE ==========
    if args.restore:
        processed_files = 0
        restored_files = 0
        for _, row in df.iterrows():
            project = str(row["project"])
            bug_id = int(row["bug_id"])

            if selected_ids and bug_id not in selected_ids:
                continue
            if selected_bugs and bug_id not in selected_bugs:
                continue

            folder = str(row["folder"])
            file_rel = str(row["file"]).lstrip("/")

            target_file = resolve_target_file(projects_root, project, folder, file_rel)
            if target_file is None:
                failures.append((bug_id, f"{projects_root}/{project}/{folder}/{file_rel}", "Target file not found in common source roots"))
                print(f"[WARN] Could not resolve file for bug {bug_id}: {file_rel}")
                continue

            ok, msg = restore_from_backup(target_file, dry_run=args.dry_run, delete_backup=args.delete_backup)
            print(msg)
            processed_files += 1
            if ok:
                restored_files += 1
            else:
                failures.append((bug_id, str(target_file), msg))

        print(f"\n[RESTORE] Processed {processed_files} file(s). Restored {restored_files} file(s).")
        if failures:
            print("\nFailures / Skips:")
            for bug_id, path, reason in failures:
                print(f"  - Bug {bug_id}: {path}\n    Reason: {reason}")
        return

    # ========== PATCH MODE ==========
    if not args.patches_root:
        parser.error("--patches-root is required unless --restore is specified")
    patches_root = Path(args.patches_root).expanduser()

    variants_map = scan_variants(patches_root)
    if not variants_map:
        print(f"[WARN] No patch subfolders found under: {patches_root}")

    grouped = df.groupby(["project", "bug_id"], sort=False)

    for (project, bug_id), sub in grouped:
        project = str(project)
        bug_id = int(bug_id)

        if selected_ids and bug_id not in selected_ids:
            continue
        if selected_bugs and bug_id not in selected_bugs:
            continue

        bug_variants = variants_map.get(bug_id, [Variant(bug_id, None, str(bug_id))])

        # Prepare variant folders by copying from CLEAN baseline if needed
        for v in bug_variants:
            if v.suffix:
                _, msg, target_dir = ensure_variant_copy(projects_root, project, bug_id, v.suffix)
                print(msg)
                if not target_dir.exists():
                    failures.append((bug_id, str(target_dir), "Variant target folder not prepared"))

        def apply_for_variant(variant: Variant, folder_override: Optional[str] = None):
            """
            Apply patches for one variant.
            folder_override: if set, use this folder name instead of deriving from variant/row
                             (used for individual copy dirs like Chart-1-buggy_ind1).
            """
            nonlocal total_patches, changed_patches

            per_file: Dict[Path, List[Tuple[int, int, str, str, int, dict]]] = {}

            for _, row in sub.iterrows():
                folder_base = str(row["folder"])
                if folder_override:
                    folder_eff = folder_override
                else:
                    folder_eff = rewrite_folder_for_variant(folder_base, project, bug_id, variant.suffix)
                file_rel = str(row["file"]).lstrip("/")
                func_idx = int(row["func_index_in_bug"])

                target_file = resolve_target_file(projects_root, project, folder_eff, file_rel)
                if target_file is None:
                    failures.append((bug_id, f"{projects_root}/{project}/{folder_eff}/{file_rel}",
                                     "Target file not found in common source roots"))
                    print(f"[WARN] Could not resolve file for bug {bug_id} ({variant.key}): {file_rel}")
                    continue

                patch_text, which = find_patch_text_by_index(patches_root, variant.key, func_idx)
                if not patch_text:
                    failures.append((bug_id, str(target_file), f"{which} (variant {variant.key})"))
                    print(f"[WARN] {which}")
                    continue

                start_line = int(row["buggy_method_start_line"])
                end_line = int(row["buggy_method_end_line"])

                per_file.setdefault(target_file, []).append(
                    (start_line, end_line, patch_text, which, func_idx, row.to_dict())
                )

            for file_path, patches in per_file.items():
                patches_simple = [(a, b, t, w, i) for (a, b, t, w, i, _) in patches]
                fixed_map = compute_fixed_ranges(patches_simple)

                # Apply patches in descending order (safe)
                for start, end, text, which, idx, _rowdict in sorted(patches, key=lambda x: x[0], reverse=True):
                    changed, msg = replace_range_in_file(file_path, start, end, text, dry_run=args.dry_run)
                    total_patches += 1
                    if changed:
                        changed_patches += 1
                    print(f"{msg}  [patch: {which} | func_index={idx}]")

                # Emit fixed-range report rows
                for start, end, text, which, idx, rowdict in patches:
                    fixed_start, fixed_end = fixed_map.get(idx, ("", ""))
                    if folder_override:
                        eff_folder = folder_override
                    else:
                        eff_folder = rewrite_folder_for_variant(str(rowdict.get("folder", "")), project, bug_id, variant.suffix)
                    fixed_reports.setdefault(project, []).append({
                        "project": project,
                        "bug_id": bug_id,
                        "folder": eff_folder,
                        "class_entry": rowdict.get("class_entry", ""),
                        "file": str(rowdict.get("file", "")),
                        "method_sig_snippet": rowdict.get("method_sig_snippet", ""),
                        "fixed_method_start_line": fixed_start,
                        "fixed_method_end_line": fixed_end,
                    })

        # Apply base first, then variants
        base_variant = next((v for v in bug_variants if v.suffix is None), None)
        if base_variant:
            apply_for_variant(base_variant)
        for v in bug_variants:
            if v.suffix:
                apply_for_variant(v)

        # Also apply base patch to individual copy dirs (Chart-1-buggy_ind1, etc.)
        # Save and restore fixed_reports to avoid writing ind rows to methods CSV.
        if base_variant:
            proj_dir_path = projects_root / project
            if proj_dir_path.is_dir():
                saved_reports = dict(fixed_reports)
                for ind_dir in sorted(proj_dir_path.iterdir()):
                    m_ind = _IND_BUGGY_RE.match(ind_dir.name)
                    if m_ind and m_ind.group(1) == project and int(m_ind.group(2)) == bug_id and ind_dir.is_dir():
                        print(f"[IND-PATCH] applying base patch to {ind_dir.name}")
                        apply_for_variant(base_variant, folder_override=ind_dir.name)
                # Discard any ind rows that were appended
                fixed_reports.clear()
                fixed_reports.update(saved_reports)

    # Write per-project fixed-range CSVs
    if fixed_reports:
        for proj, rows in fixed_reports.items():
            out_path = resolve_fixed_csv_path(
                project=proj,
                fixed_out_dir=args.fixed_out_dir,
                fixed_out_file=args.fixed_out_file,
                multi_project=multi_project,
            ).resolve()
            out_path.parent.mkdir(parents=True, exist_ok=True)
            with out_path.open("w", encoding="utf-8", newline="") as f:
                writer = csv.DictWriter(f, fieldnames=[
                    "project", "bug_id", "folder", "class_entry", "file",
                    "method_sig_snippet", "fixed_method_start_line", "fixed_method_end_line",
                ])
                writer.writeheader()
                writer.writerows(rows)
            print(f"[OK] Wrote fixed methods CSV: {out_path} (rows={len(rows)})")

    print(f"\n[PATCH] Processed {total_patches} patch operation(s). Changed {changed_patches} patch(es).")
    if failures:
        print("\nFailures / Skips:")
        for bug_id, path, reason in failures:
            print(f"  - Bug {bug_id}: {path}\n    Reason: {reason}")


if __name__ == "__main__":
    main()
