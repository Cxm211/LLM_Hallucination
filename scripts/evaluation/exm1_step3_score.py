#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Classify every input slice of Task 1 and record whether it carried an oracle trigger.

A bug whose relevant test suite exceeds the 300-method limit is split into slices, each an
independent task instance with its own patch and its own log. This writes one row per slice.

Definitions, shared with exp1step3_check_trigger_pass.py:
  oracle trigger  a class::method listed in oracle_triggers.csv for that bug
  trigger_appear  yes when at least one oracle trigger appears in the slice's input.java,
                  matched strictly on the "// class::method" header
  status          not compilable  the log carries a compilation-failure marker
                  not pass        otherwise, if any oracle trigger appears in the log
                  pass            otherwise
  n_trigger_pred_success
                  how many of the appearing triggers the slice's prediction.json got right

trigger_appear is what supports the trigger-insensitive / trigger-dependent /
trigger-absent-only comparison of Table 7.

Output: one CSV per project -> <out-root>/<model>/<project>.csv (overwritten). The columns
match the published tables under results/evaluation/, so a re-run can be diffed against them
directly.

Example:
  python3 exm1_step3_score.py \
    --results-root results/data/exm1 \
    --out-root generated_evaluation/exm1 \
    --logs-root test-logs/exm1 \
    --model Claude --project Csv
"""

import argparse
import re
import csv
import json
from pathlib import Path
from typing import Dict, List, Optional, Set, Tuple


# --- helpers, inlined so this script stands alone ------------------------

_INPUT_METHOD_RE = re.compile(r"^\s*//\s*([\w.]+)::(\w+)\s*$")



DEFAULT_TRIGGER_CSV = Path(__file__).resolve().parent / "oracle_triggers.csv"


def read_text(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8")
    except UnicodeDecodeError:
        return path.read_text(encoding="utf-8", errors="ignore")


def parse_id_expr(expr: Optional[str]) -> Optional[Set[int]]:
    if not expr:
        return None
    ids: Set[int] = set()
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
    return ids or None


def load_oracle_triggers(csv_path: Path) -> Dict[Tuple[str, int], Set[str]]:
    """Read oracle_triggers.csv into {(project, bug_id): {"class::method", ...}}."""
    out: Dict[Tuple[str, int], Set[str]] = {}
    with csv_path.open(newline="", encoding="utf-8-sig") as f:
        for row in csv.DictReader(f):
            try:
                key = (row["project"], int(row["bug_id"]))
            except (KeyError, ValueError):
                continue
            cls, meth = row.get("class_fqn", "").strip(), row.get("method_name", "").strip()
            if cls and meth:
                out.setdefault(key, set()).add(f"{cls}::{meth}")
    return out


def find_result_branches(results_root: Path, model: str, project: str, bug_id: int) -> List[Path]:
    base = results_root / model / project
    if not base.exists():
        return []
    matched = []
    for child in base.iterdir():
        if child.is_dir() and re.match(rf"^{bug_id}(?:_\d+)?$", child.name):
            matched.append(child)
    return sorted(matched, key=lambda p: ("_" in p.name, p.name))


def extract_methods_from_input(path: Path) -> Set[str]:
    """Collect every method declared in an input.java header comment as class_fqn::method_name."""
    present: Set[str] = set()
    try:
        for line in path.read_text(encoding="utf-8", errors="ignore").splitlines():
            m = _INPUT_METHOD_RE.match(line)
            if m:
                present.add(f"{m.group(1)}::{m.group(2)}")
    except OSError:
        pass
    return present


def load_predictions(path: Path) -> Tuple[Set[str], Set[str]]:
    """Read a slice's prediction.json, the triggering testcases the model predicted.

    Returns (pred_full, pred_methods):
      pred_full     entries carrying a class name, normalised to class_fqn::method,
                    matched exactly
      pred_methods  entries that are a bare method name, kept so predictions written
                    without a class still match. Entries with a class never land here,
                    which would otherwise let a same-named method of another class count
                    as a hit
    """
    pred_full: Set[str] = set()
    pred_methods: Set[str] = set()
    try:
        arr = json.loads(path.read_text(encoding="utf-8", errors="ignore"))
    except (OSError, ValueError):
        return pred_full, pred_methods
    if not isinstance(arr, list):
        return pred_full, pred_methods
    for x in arr:
        s = str(x).strip().replace("#", "::")
        if not s:
            continue
        (pred_full if "::" in s else pred_methods).add(s)
    return pred_full, pred_methods


def normalize_testcase_name(name: str) -> List[str]:
    """
    Expand a testcase name into the several forms it may take, so it can be matched in
    input.java, in a log, or in the model output. For example org.foo.BarTest::testA
    -> {
         org.foo.BarTest::testA,
         org.foo.BarTest#testA,
         BarTest::testA,
         BarTest#testA,
         testA
       }
    """
    name = name.strip().strip('"').strip("'")
    name = re.sub(r"\s+", "", name)
    if not name:
        return []

    raw = name.replace("#", "::")
    parts = raw.split("::", 1)

    variants: List[str] = []
    variants.append(raw)
    variants.append(raw.replace("::", "#"))

    if len(parts) == 2:
        cls, method = parts
        short_cls = cls.split(".")[-1]
        variants.extend([
            f"{short_cls}::{method}",
            f"{short_cls}#{method}",
            method,
        ])
    else:
        variants.append(parts[0].split(".")[-1])

    # de-duplicate, preserving order
    seen = set()
    out = []
    for v in variants:
        if v and v not in seen:
            seen.add(v)
            out.append(v)
    return out


def testcase_in_text(testcase: str, text: str) -> bool:
    text_nospace = re.sub(r"\s+", "", text)
    for cand in normalize_testcase_name(testcase):
        cand_nospace = re.sub(r"\s+", "", cand)
        if cand_nospace and cand_nospace in text_nospace:
            return True
    return False


def resolve_log_file(logs_root: Path, model: str, project: str, branch: str) -> Optional[Path]:
    candidates = [
        logs_root / model / project / f"{project}-{branch}-buggy.log",
        logs_root / model / f"{project}-{branch}-buggy.log",
        logs_root / project / f"{project}-{branch}-buggy.log",
        logs_root / f"{project}-{branch}-buggy.log",
    ]
    for c in candidates:
        if c.exists():
            return c
    return None

# -------------------------------------------------------------------------



# the same compilation-failure markers classify_from_log uses
_NOT_COMPILABLE_MARKERS = [
    "not compilable", "compilation failed", "compile failed",
    "cannot find symbol", "build failed", "javac: compilation failed",
]


def variant_status(log_path, triggers):
    """Status of one slice, decided from a single read of its log."""
    if log_path is None:
        return "missing log"
    text = read_text(log_path)
    low = text.lower()
    if any(m in low for m in _NOT_COMPILABLE_MARKERS):
        return "not compilable"
    if any(testcase_in_text(t, text) for t in triggers):
        return "not pass"
    return "pass"


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--trigger-csv", default=str(DEFAULT_TRIGGER_CSV),
                    help="CSV of oracle triggering testcases "
                         "(default: <script dir>/oracle_triggers.csv)")
    ap.add_argument("--results-root", default=str(Path(__file__).resolve().parents[2] / "results/data/exm1"),
                    help="Root holding <model>/<project>/<variant>/")
    ap.add_argument("--out-root", default=None,
                    help="Where to write <model>/<project>.csv "
                         "(default: same as --results-root)")
    ap.add_argument("--logs-root", default="test-logs/exm1")
    ap.add_argument("--model", required=True)
    ap.add_argument("--project", action="append", help="Restrict to these projects; repeatable or comma separated")
    ap.add_argument("--ids", type=str, help="Bug ids such as 1,2,5-7")
    args = ap.parse_args()

    def parse_projects(values):
        if not values:
            return None
        s = set()
        for v in values:
            s.update(t.strip() for t in str(v).split(",") if t.strip())
        return s or None

    projects = parse_projects(args.project)
    ids = parse_id_expr(args.ids)
    trigger_csv = Path(args.trigger_csv)
    results_root = Path(args.results_root)
    logs_root = Path(args.logs_root)

    fieldnames = ["model", "project", "bug_id", "variant", "status", "trigger_appear",
                  "n_trigger_total", "n_trigger_appear", "n_trigger_pred_success",
                  "matched_triggers", "pred_success_triggers"]

    rows = []
    oracle = load_oracle_triggers(trigger_csv)
    for (project, bug_id) in sorted(oracle, key=lambda k: (k[0], k[1])):
        if projects and project not in projects:
            continue
        if ids and bug_id not in ids:
            continue
        triggers = sorted(oracle[(project, bug_id)])

        for branch_dir in find_result_branches(results_root, args.model, project, bug_id):
            variant = branch_dir.name

            present = extract_methods_from_input(branch_dir / "input.java")
            matched = [t for t in triggers if t in present]

            pred_full, pred_methods = load_predictions(branch_dir / "prediction.json")
            pred_success = [t for t in matched
                            if t in pred_full or t.rsplit("::", 1)[-1] in pred_methods]

            log_path = resolve_log_file(logs_root, args.model, project, variant)
            status = variant_status(log_path, triggers)

            rows.append({
                "model": args.model, "project": project, "bug_id": bug_id,
                "variant": variant, "status": status,
                "trigger_appear": "yes" if matched else "no",
                "n_trigger_total": len(triggers),
                "n_trigger_appear": len(matched),
                "n_trigger_pred_success": len(pred_success),
                "matched_triggers": "; ".join(matched),
                "pred_success_triggers": "; ".join(pred_success),
            })

    out_base = Path(args.out_root) if args.out_root else results_root
    out_root = out_base / args.model
    out_root.mkdir(parents=True, exist_ok=True)

    by_project = {}
    for r in rows:
        by_project.setdefault(r["project"], []).append(r)

    for proj, prows in sorted(by_project.items()):
        prows.sort(key=lambda r: (r["bug_id"], ("_" in r["variant"], r["variant"])))
        out_csv = out_root / f"{proj}.csv"
        with out_csv.open("w", newline="", encoding="utf-8") as f:
            w = csv.DictWriter(f, fieldnames=fieldnames)
            w.writeheader()
            for r in prows:
                w.writerow(r)
        print(f"csv -> {out_csv}  ({len(prows)} variants)")

    summary = {}
    for r in rows:
        summary[r["status"]] = summary.get(r["status"], 0) + 1
    print("\n=== status summary ===")
    for k in sorted(summary):
        print(f"{k:16s} -> {summary[k]}")
    print(f"total variants   -> {len(rows)}")


if __name__ == "__main__":
    main()
