#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
exm3_step8_score.py

Computes per-bug-id scores by combining:

  1. Test discrimination (run_buggy vs run_patched):
       Ideal: add_test FAILS on buggy, PASSES on fixed -> 1.0
       One condition wrong -> 0.5; neither -> 0.0
       Not Compilable on either side -> "Not Compilable"
       No add_test*.java found for bug_id -> "Not Generated"
       Average across add_tests when bug_id has multiple.

  2. Coverage improvement (buggy side):
       coverage_buggy vs base_coverage_buggy
       score = delta / potential
         delta     = covered_rate_with_add - covered_rate_without_add
         potential = 1.0 - covered_rate_without_add  (headroom)
       Rewards large improvements AND improvements when base is already high.

  3. Coverage improvement (fixed side):
       coverage_patched vs base_coverage_groundtruth (same formula)

  Not Compilable propagates from coverage CSV (executed == "Not Compilable").

Output: <out-dir>/<Project>.csv  (one row per add_test per bug_id)
        If bug_id has no add_tests -> one row with "Not Generated".

Usage:
  python exm3_step8_score.py
  python exm3_step8_score.py --project Chart
  python exm3_step8_score.py --project Chart --ids 1,3,5-7
"""

import argparse
import csv
import re
import sys
from pathlib import Path
from typing import Any, Dict, List, Optional, Set, Tuple


# -------------------- scoring --------------------

def cov_score_from_counts(add_covered: int, base_covered: int, base_total: int) -> float:
    """Coverage improvement score [0, 1] based on instruction/branch counts.

    score = delta / potential
    where delta = add_covered - base_covered,
          potential = base_total - base_covered  (headroom).
    """
    delta = add_covered - base_covered
    potential = base_total - base_covered
    if potential <= 0:
        return 0.0 if delta <= 0 else 1.0
    return max(0.0, min(1.0, delta / potential))


def test_item_score(buggy_res: str, fixed_res: str) -> Any:
    """Per-test discrimination score: 1.0 / 0.5 / 0.0 / 'Not Compilable'."""
    NC = "Not Compilable"
    if buggy_res == NC or fixed_res == NC:
        return NC
    met = int(buggy_res == "Not Pass") + int(fixed_res == "Pass")
    return [0.0, 0.5, 1.0][met]


def aggregate_test_scores(scores: List[Any]) -> Any:
    """Aggregate per-test scores to a single bug-level value."""
    if any(s == "Not Compilable" for s in scores):
        return "Not Compilable"
    nums = [s for s in scores if isinstance(s, float)]
    if not nums:
        return ""
    return sum(nums) / len(nums)


# -------------------- data loaders --------------------

_IND_RE = re.compile(r"_ind\d+$")


def load_coverage(
    path: Path,
    by_add_test: bool = False,
    base_only: bool = False,
) -> Dict:
    """
    Aggregate line-level coverage CSV using instruction and branch counts.

    by_add_test=False: returns {bug_id: {folder, ci, mi, cb, mb, not_compilable}}
    by_add_test=True:  returns {(bug_id, add_test_file): {...}}
    base_only=True: skip rows whose folder contains '_ind' (individual copies).
    Lines with empty 'executed' (not in JaCoCo map) are skipped.
    """
    data: Dict = {}
    if not path.exists():
        return data
    with open(path, encoding="utf-8-sig") as f:
        nonblank = (line for line in f if line.strip())
        for row in csv.DictReader(nonblank):
            bid = row.get("bug_id", "").strip()
            if not bid:
                continue
            if base_only:
                folder = row.get("folder", "").strip()
                if _IND_RE.search(folder):
                    continue
            if by_add_test:
                atf = row.get("add_test_file", "").strip()
                key = (bid, atf)
            else:
                key = bid
            if key not in data:
                data[key] = {
                    "folder": row.get("folder", "").strip(),
                    "ci": 0, "mi": 0, "cb": 0, "mb": 0,
                    "not_compilable": False,
                }
            ex = row.get("executed", "").strip()
            if ex == "Not Compilable":
                data[key]["not_compilable"] = True
            elif ex in ("True", "False"):
                data[key]["ci"] += int(row.get("ci", 0) or 0)
                data[key]["mi"] += int(row.get("mi", 0) or 0)
                data[key]["cb"] += int(row.get("cb", 0) or 0)
                data[key]["mb"] += int(row.get("mb", 0) or 0)
    return data


def load_fixed_results(path: Path) -> Dict[str, str]:
    """
    Load groundtruth_checkouts/<Project>.csv.
    Returns {bug_id: result} where result is 'Pass', 'Not Pass', or 'Not Compilable'.
    """
    data: Dict[str, str] = {}
    if not path.exists():
        return data
    with open(path, encoding="utf-8-sig") as f:
        for row in csv.DictReader(line for line in f if line.strip()):
            bid = row.get("bug_id", "").strip()
            result = row.get("result", "").strip()
            if bid:
                data[bid] = result
    return data


def load_test_results(path: Path) -> Dict[Tuple[str, str], Dict[str, str]]:
    """
    Load a test result CSV (run_buggy / run_patched).
    Returns {(bug_id, add_test_file): {class_fqcn, method_name, result}}.
    """
    data: Dict[Tuple[str, str], Dict[str, str]] = {}
    if not path.exists():
        return data
    with open(path, encoding="utf-8-sig") as f:
        f = (line for line in f if line.strip())  # type: ignore[assignment]
        for row in csv.DictReader(f):
            bid = row.get("bug_id", "").strip()
            atf = row.get("add_test_file", "").strip()
            if bid and atf:
                data[(bid, atf)] = {
                    "class_fqcn":  row.get("class_fqcn", "").strip(),
                    "method_name": row.get("method_name", "").strip(),
                    "result":      row.get("result", "").strip(),
                }
    return data


def load_groundtruth_results(path: Path) -> Dict[Tuple[str, str], str]:
    """
    Load run_groundtruth/<Project>.csv.
    Returns {(bug_id, add_test_file): result}.
    """
    data: Dict[Tuple[str, str], str] = {}
    if not path.exists():
        return data
    with open(path, encoding="utf-8-sig") as f:
        for row in csv.DictReader(line for line in f if line.strip()):
            bid = row.get("bug_id", "").strip()
            atf = row.get("add_test_file", "").strip()
            if bid and atf:
                data[(bid, atf)] = row.get("result", "").strip()
    return data


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


# -------------------- coverage cell helpers --------------------

def _f(v: Any) -> str:
    if v is None:
        return ""
    if isinstance(v, float):
        return f"{v:.4f}"
    return str(v)


def cov_cells(
    base: Dict[str, Any],
    add: Dict[str, Any],
) -> Tuple[List, List, str, str, str, str]:
    """
    Returns (base_cells[4], add_cells[4], inst_delta, inst_score, branch_delta, branch_score).
    Cells = [ci, mi, cb, mb].
    Handles missing data and Not Compilable gracefully.
    """
    NC = "Not Compilable"

    def cells(d: Dict) -> Tuple[List, Any]:
        if not d:
            return ["", "", "", ""], None
        if d["not_compilable"]:
            return [NC, NC, NC, NC], NC
        return [d["ci"], d["mi"], d["cb"], d["mb"]], d

    base_cells, base_v = cells(base)
    add_cells,  add_v  = cells(add)

    if base_v == NC or add_v == NC:
        return base_cells, add_cells, NC, NC, NC, NC
    if base_v is None or add_v is None:
        return base_cells, add_cells, "", "", "", ""

    # instruction delta & score
    inst_delta = add_v["ci"] - base_v["ci"]
    inst_total = base_v["ci"] + base_v["mi"]
    inst_score = cov_score_from_counts(add_v["ci"], base_v["ci"], inst_total)

    # branch delta & score
    branch_delta = add_v["cb"] - base_v["cb"]
    branch_total = base_v["cb"] + base_v["mb"]
    branch_score = cov_score_from_counts(add_v["cb"], base_v["cb"], branch_total)

    return (base_cells, add_cells,
            str(inst_delta), _f(inst_score),
            str(branch_delta), _f(branch_score))


# -------------------- output schema --------------------

FIELDNAMES = [
    "project", "bug_id", "folder",
    # per-test columns
    "add_test_file", "class_fqcn", "method_name",
    "repair_result",
    "buggy_result", "fixed_result", "groundtruth_result",
    # instruction delta/score
    "buggy_inst_delta", "buggy_inst_score",
    "fixed_inst_delta", "fixed_inst_score",
    # branch delta/score
    "buggy_branch_delta", "buggy_branch_score",
    "fixed_branch_delta", "fixed_branch_score",
    # base counts (ci, mi, cb, mb)
    "buggy_base_ci", "buggy_base_mi", "buggy_base_cb", "buggy_base_mb",
    # add counts (ci, mi, cb, mb)
    "buggy_add_ci",  "buggy_add_mi",  "buggy_add_cb",  "buggy_add_mb",
    "fixed_base_ci", "fixed_base_mi", "fixed_base_cb", "fixed_base_mb",
    "fixed_add_ci",  "fixed_add_mi",  "fixed_add_cb",  "fixed_add_mb",
]


# -------------------- project processing --------------------

def process_project_combined(
    project: str,
    results_root: Path,
    id_filter: Optional[Set[str]],
    out_dir: Path,
) -> None:
    """Score using all add_tests together (combined mode)."""
    def cpath(sub: str) -> Path:
        return results_root / sub / f"{project}.csv"

    # Prefer the combined base from the same directory, else the shared base coverage
    buggy_base_combined = cpath("base_coverage_buggy")
    buggy_base_data = load_coverage(buggy_base_combined) if buggy_base_combined.exists() \
        else load_coverage(cpath("base_coverage_buggy"), base_only=True)
    buggy_add_data  = load_coverage(cpath("coverage_buggy"))

    fixed_base_combined = cpath("base_coverage_patched")
    fixed_base_data = load_coverage(fixed_base_combined) if fixed_base_combined.exists() \
        else load_coverage(cpath("base_coverage_groundtruth"), base_only=True)
    fixed_add_data  = load_coverage(cpath("coverage_patched"))
    buggy_test_data = load_test_results(cpath("run_buggy"))
    fixed_test_data = load_test_results(cpath("run_patched"))
    fixed_result_data = load_fixed_results(cpath("groundtruth_checkouts"))
    groundtruth_data = load_groundtruth_results(cpath("run_groundtruth"))

    # Collect all known bug_ids
    all_ids: Set[str] = set()
    for d in (buggy_base_data, buggy_add_data, fixed_base_data, fixed_add_data):
        all_ids.update(d.keys())
    for (bid, _) in list(buggy_test_data) + list(fixed_test_data):
        all_ids.add(bid)
    proj_res = results_root / project
    if proj_res.is_dir():
        for d in proj_res.iterdir():
            if d.is_dir() and re.match(r"^\d+$", d.name):
                all_ids.add(d.name)

    if id_filter:
        all_ids &= id_filter
    if not all_ids:
        print(f"  [SKIP] no bug_ids found for {project}")
        return

    out_path = out_dir / f"{project}.csv"
    rows_written = 0

    with open(out_path, "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(FIELDNAMES)

        for bug_id in sorted(all_ids, key=lambda x: int(x) if x.isdigit() else float("inf")):
            bb = buggy_base_data.get(bug_id, {})
            ba = buggy_add_data.get(bug_id, {})
            fb = fixed_base_data.get(bug_id, {})
            fa = fixed_add_data.get(bug_id, {})

            folder = (
                bb.get("folder") or ba.get("folder") or
                fb.get("folder") or fa.get("folder") or
                f"{project}-{bug_id}-buggy"
            )

            bb_c, ba_c, bi_d, bi_s, bb_d, bb_s = cov_cells(bb, ba)
            fb_c, fa_c, fi_d, fi_s, fb_d, fb_s = cov_cells(fb, fa)

            # Discover add_test files for this bug_id
            add_test_files: List[str] = []
            bug_res_dir = results_root / project / bug_id
            if bug_res_dir.is_dir():
                add_test_files = sorted(p.name for p in bug_res_dir.glob("add_test*.java"))
            if not add_test_files:
                add_test_files = sorted({
                    atf for (bid, atf) in list(buggy_test_data) + list(fixed_test_data)
                    if bid == bug_id
                })

            repair_result = fixed_result_data.get(bug_id, "")

            def _make_row(atf, class_fqcn, method_name, buggy_res, fixed_res, groundtruth_res=""):
                return [
                    project, bug_id, folder,
                    atf, class_fqcn, method_name,
                    repair_result,
                    buggy_res, fixed_res, groundtruth_res,
                    bi_d, bi_s, fi_d, fi_s,
                    bb_d, bb_s, fb_d, fb_s,
                    bb_c[0], bb_c[1], bb_c[2], bb_c[3],
                    ba_c[0], ba_c[1], ba_c[2], ba_c[3],
                    fb_c[0], fb_c[1], fb_c[2], fb_c[3],
                    fa_c[0], fa_c[1], fa_c[2], fa_c[3],
                ]

            # Not Generated
            if not add_test_files:
                w.writerow(_make_row(
                    "Not Generated", "Not Generated", "Not Generated",
                    "Not Generated", "Not Generated",
                ))
                rows_written += 1
                continue

            # Per-test rows
            for atf in add_test_files:
                br = buggy_test_data.get((bug_id, atf), {})
                fr = fixed_test_data.get((bug_id, atf), {})
                buggy_res      = br.get("result", "")
                fixed_res      = fr.get("result", "")
                class_fqcn     = br.get("class_fqcn") or fr.get("class_fqcn") or ""
                method_name    = br.get("method_name") or fr.get("method_name") or ""
                groundtruth_res = groundtruth_data.get((bug_id, atf), "")
                w.writerow(_make_row(atf, class_fqcn, method_name, buggy_res, fixed_res, groundtruth_res))
                rows_written += 1

    print(f"  [{project}] {rows_written} rows -> {out_path}")


# -------------------- individual project processing --------------------


def process_project_individual(
    project: str,
    results_root: Path,
    id_filter: Optional[Set[str]],
    out_dir: Path,
) -> None:
    """Score using each add_test in isolation (individual mode)."""
    def cpath(sub: str) -> Path:
        return results_root / sub / f"{project}.csv"

    # Prefer the per-testcase base, which shares the compilation state, else the shared one
    buggy_base_ind = cpath("base_coverage_buggy_individual")
    buggy_base_data = load_coverage(buggy_base_ind, by_add_test=True) if buggy_base_ind.exists() \
        else load_coverage(cpath("base_coverage_buggy"), base_only=True)
    _buggy_base_is_individual = buggy_base_ind.exists()

    fixed_base_ind = cpath("base_coverage_patched_individual")
    fixed_base_data = load_coverage(fixed_base_ind, by_add_test=True) if fixed_base_ind.exists() \
        else load_coverage(cpath("base_coverage_groundtruth"), base_only=True)
    _fixed_base_is_individual = fixed_base_ind.exists()

    buggy_add_data  = load_coverage(cpath("coverage_buggy_individual"), by_add_test=True)
    fixed_add_data  = load_coverage(cpath("coverage_patched_individual"), by_add_test=True)
    buggy_test_data = load_test_results(cpath("run_buggy_individual"))
    fixed_test_data = load_test_results(cpath("run_patched_individual"))
    fixed_result_data = load_fixed_results(cpath("groundtruth_checkouts"))
    groundtruth_data = load_groundtruth_results(cpath("run_groundtruth_individual"))

    # Combined fallbacks for N=1 bugs (coverage keyed by bug_id, not (bug_id, atf))
    buggy_add_data_comb  = load_coverage(cpath("coverage_buggy"))
    fixed_add_data_comb  = load_coverage(cpath("coverage_patched"))
    buggy_test_data_comb = load_test_results(cpath("run_buggy"))
    fixed_test_data_comb = load_test_results(cpath("run_patched"))
    groundtruth_data_comb = load_groundtruth_results(cpath("run_groundtruth"))

    # Collect all (bug_id, add_test_file) pairs from individual sources
    all_keys: Set[Tuple[str, str]] = set()
    for d in (buggy_add_data, fixed_add_data):
        all_keys.update(d.keys())
    all_keys.update(buggy_test_data.keys())
    all_keys.update(fixed_test_data.keys())

    # Also include N=1 bugs: they only have combined data (no _ind dirs were created)
    # Collect N=0 bugs (no add_test at all) for Not Generated rows
    not_generated_ids: Set[str] = set()
    proj_res = results_root / project
    if proj_res.is_dir():
        for d in proj_res.iterdir():
            if d.is_dir() and re.match(r"^\d+$", d.name):
                add_files = sorted(d.glob("add_test*.java"))
                if len(add_files) == 1:
                    all_keys.add((d.name, add_files[0].name))
                elif len(add_files) == 0 and d.name in fixed_result_data:
                    not_generated_ids.add(d.name)

    if id_filter:
        all_keys = {(bid, atf) for (bid, atf) in all_keys if bid in id_filter}
        not_generated_ids = {bid for bid in not_generated_ids if bid in id_filter}

    if not all_keys and not not_generated_ids:
        print(f"  [SKIP] no individual data found for {project}")
        return

    out_path = out_dir / f"{project}.csv"
    rows_written = 0

    with open(out_path, "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(FIELDNAMES)

        for bug_id, atf in sorted(
            all_keys,
            key=lambda x: (int(x[0]) if x[0].isdigit() else float("inf"), x[1]),
        ):
            # Base data: keyed by (bug_id, atf) if individual, or bug_id if shared
            bb = buggy_base_data.get((bug_id, atf), {}) if _buggy_base_is_individual \
                else buggy_base_data.get(bug_id, {})
            ba = buggy_add_data.get((bug_id, atf), {}) or buggy_add_data_comb.get(bug_id, {})
            fb = fixed_base_data.get((bug_id, atf), {}) if _fixed_base_is_individual \
                else fixed_base_data.get(bug_id, {})
            fa = fixed_add_data.get((bug_id, atf), {}) or fixed_add_data_comb.get(bug_id, {})

            folder = (
                bb.get("folder") or ba.get("folder") or
                fb.get("folder") or fa.get("folder") or
                f"{project}-{bug_id}-buggy"
            )

            bb_c, ba_c, bi_d, bi_s, bb_d, bb_s = cov_cells(bb, ba)
            fb_c, fa_c, fi_d, fi_s, fb_d, fb_s = cov_cells(fb, fa)

            br = buggy_test_data.get((bug_id, atf), {}) or buggy_test_data_comb.get((bug_id, atf), {})
            fr = fixed_test_data.get((bug_id, atf), {}) or fixed_test_data_comb.get((bug_id, atf), {})
            buggy_res       = br.get("result", "")
            fixed_res       = fr.get("result", "")
            class_fqcn      = br.get("class_fqcn") or fr.get("class_fqcn") or ""
            method_name     = br.get("method_name") or fr.get("method_name") or ""
            groundtruth_res = groundtruth_data.get((bug_id, atf), "") or groundtruth_data_comb.get((bug_id, atf), "")

            repair_result = fixed_result_data.get(bug_id, "")

            w.writerow([
                project, bug_id, folder,
                atf, class_fqcn, method_name,
                repair_result,
                buggy_res, fixed_res, groundtruth_res,
                bi_d, bi_s, fi_d, fi_s,
                bb_d, bb_s, fb_d, fb_s,
                bb_c[0], bb_c[1], bb_c[2], bb_c[3],
                ba_c[0], ba_c[1], ba_c[2], ba_c[3],
                fb_c[0], fb_c[1], fb_c[2], fb_c[3],
                fa_c[0], fa_c[1], fa_c[2], fa_c[3],
            ])
            rows_written += 1

        # Not Generated rows for bugs with no add_test files
        NG = "Not Generated"
        for bug_id in sorted(not_generated_ids, key=lambda x: int(x) if x.isdigit() else float("inf")):
            repair_result = fixed_result_data.get(bug_id, "")
            w.writerow([
                project, bug_id, f"{project}-{bug_id}-buggy",
                NG, NG, NG,
                repair_result,
                NG, NG, NG,
                "", "", "", "",
                "", "", "", "",
                "", "", "", "",
                "", "", "", "",
                "", "", "", "",
                "", "", "", "",
            ])
            rows_written += 1

    print(f"  [{project}] {rows_written} rows -> {out_path}")


# -------------------- main --------------------

def main():
    ap = argparse.ArgumentParser(
        description="Compute per-bug-id final scores from test results and JaCoCo coverage."
    )
    ap.add_argument(
        "--work-root", "--results-root", dest="work_root",
        default=str(Path(__file__).resolve().parents[2] / "generated_evaluation/exm3_work"),
        help="Root holding the intermediate tables: base_coverage_buggy/, run_buggy/, etc. "
             "(default: generated_evaluation/exm3_work)",
    )
    ap.add_argument(
        "--out-dir", default=str(Path(__file__).resolve().parents[2] / "generated_evaluation/exm3_work/final_combined"),
        help="Output directory for combined-mode score CSVs "
             "(default: generated_evaluation/exm3_work/final_combined)",
    )
    ap.add_argument(
        "--out-dir-individual", default=str(Path(__file__).resolve().parents[2] / "generated_evaluation/exm3/"),
        help="Output directory for individual-mode score CSVs "
             "(default: generated_evaluation/exm3/)",
    )
    ap.add_argument("--project", default=None, help="Only process this project (e.g. Chart)")
    ap.add_argument("--ids", default=None, help="Only process these bug IDs, e.g. '1,3,5-7'")
    args = ap.parse_args()
    results_root  = Path(args.work_root).expanduser().resolve()
    out_dir       = Path(args.out_dir).expanduser().resolve()
    out_dir_ind   = Path(args.out_dir_individual).expanduser().resolve()

    if not results_root.is_dir():
        sys.exit(f"[error] work root not found: {results_root}")
    out_dir.mkdir(parents=True, exist_ok=True)
    out_dir_ind.mkdir(parents=True, exist_ok=True)

    # Discover projects from all input subdirectories (combined + individual)
    INPUT_SUBDIRS = [
        "base_coverage_buggy",
        "coverage_buggy",
        "base_coverage_groundtruth",
        "coverage_patched",
        "run_buggy",
        "run_patched",
        "groundtruth_checkouts",
        "coverage_buggy_individual",
        "coverage_patched_individual",
        "run_buggy_individual",
        "run_patched_individual",
        "run_groundtruth",
        "run_groundtruth_individual",
    ]
    # Use a case-insensitive dedup dict: lower -> canonical name (prefer first seen)
    _proj_map: Dict[str, str] = {}
    for sub in INPUT_SUBDIRS:
        d = results_root / sub
        if d.is_dir():
            for f in d.glob("*.csv"):
                if f.stem.lower() == "errors":
                    continue
                key = f.stem.lower()
                if key not in _proj_map:
                    _proj_map[key] = f.stem
    # Also scan project dirs with add_test*.java
    for d in results_root.iterdir():
        if d.is_dir() and d.name not in set(INPUT_SUBDIRS):
            for sub in d.iterdir():
                if sub.is_dir() and sub.name.isdigit() and any(sub.glob("add_test*.java")):
                    key = d.name.lower()
                    if key not in _proj_map:
                        _proj_map[key] = d.name
                    break
    projects: Set[str] = set(_proj_map.values())

    if not projects:
        sys.exit("[ERROR] no project data found in input directories")

    id_filter = parse_id_expr(args.ids) if args.ids else None

    print(f"results-root     : {results_root}")
    print(f"out-dir (combined)  : {out_dir}")
    print(f"out-dir (individual): {out_dir_ind}")
    print(f"projects         : {', '.join(sorted(projects))}")
    print()

    for project in sorted(projects):
        if args.project and project.lower() != args.project.lower():
            continue
        print(f"=== {project} (combined) ===")
        process_project_combined(project, results_root, id_filter, out_dir)
        print(f"=== {project} (individual) ===")
        process_project_individual(project, results_root, id_filter, out_dir_ind)

    print(f"\n[DONE]  combined  CSVs: {out_dir}")
    print(f"[DONE]  individual CSVs: {out_dir_ind}")


if __name__ == "__main__":
    main()
