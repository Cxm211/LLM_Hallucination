#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Classify every baseline bug from its defects4j test log.

Bug ids come from the patch directories under <baseline-root>/<model>/<project>/; the log for
each one is read from <logs-root>/<model>/<project>/<project>-<id>-buggy.log.

status is decided in this order:
  log contains a timeout marker, or the exit code is 124  -> timeout
  no exit code found                                      -> unknown
  exit code != 0                                          -> not compilable
  exit code == 0 and "Failing tests: N" with N > 0        -> not pass
  otherwise                                               -> pass
  log file missing                                        -> no log

Output: one CSV per project -> <out-root>/<model>/<project>.csv (overwritten). The columns
match the published tables under results/evaluation/, so a re-run can be diffed against them
directly.

Example:
  python3 baseline_step3_score.py \
    --baseline-root results/data/baseline \
    --out-root generated_evaluation/baseline \
    --logs-root test-logs/baseline \
    --model Claude --project Csv
"""

import argparse
import csv
import re
from pathlib import Path
from typing import Iterable, List, Optional, Set, Tuple


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


def read_text(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8")
    except UnicodeDecodeError:
        return path.read_text(encoding="utf-8", errors="ignore")


# -----------------------------
# bug ids, taken from the patch directories
# -----------------------------

_BUG_DIR_RE = re.compile(r"^(\d+)$")


def find_project_dirs(baseline_root: Path, model: str, projects: Optional[Set[str]]) -> List[Path]:
    base = baseline_root / model
    if not base.is_dir():
        return []
    out = []
    for child in sorted(base.iterdir()):
        if not child.is_dir():
            continue
        # skip aggregate directories such as z_output / z_requests / z_final
        if child.name.startswith("z"):
            continue
        if projects and child.name not in projects:
            continue
        out.append(child)
    return out


def find_bug_ids(project_dir: Path, ids: Optional[Set[int]]) -> List[int]:
    found: Set[int] = set()
    for child in project_dir.iterdir():
        if not child.is_dir():
            continue
        m = _BUG_DIR_RE.match(child.name)
        if not m:
            continue
        bid = int(m.group(1))
        if ids and bid not in ids:
            continue
        found.add(bid)
    return sorted(found)


# -----------------------------
# log lookup and classification
# -----------------------------

EXIT_RE = re.compile(r"^----\s*exit code:\s*(\d+)", re.MULTILINE)
FAILING_RE = re.compile(r"Failing tests:\s*([1-9]\d*)")


def resolve_log_file(logs_root: Path, model: str, project: str, bug_id: int) -> Optional[Path]:
    branch = f"{project}-{bug_id}-buggy"
    candidates = [
        logs_root / model / project / f"{branch}.log",
        logs_root / model / f"{branch}.log",
        logs_root / project / f"{branch}.log",
        logs_root / f"{branch}.log",
    ]
    for c in candidates:
        if c.exists():
            return c
    return None


def classify_log(log_path: Optional[Path]) -> str:
    """Return pass / not pass / not compilable / timeout / unknown / no log."""
    if log_path is None:
        return "no log"
    text = read_text(log_path)

    # only look at the main-command section, so the ant clean exit code is ignored
    idx = text.find(">>> running main command")
    main = text[idx:] if idx != -1 else text

    if "[timeout]" in main:
        return "timeout"

    m_exit = None
    for m_exit in EXIT_RE.finditer(main):
        pass  # keep the last exit code, the one in the footer
    exit_code = int(m_exit.group(1)) if m_exit else None

    if exit_code == 124:
        return "timeout"
    if exit_code is None:
        return "unknown"
    if exit_code != 0:
        return "not compilable"
    if FAILING_RE.search(main):
        return "not pass"
    return "pass"


# -----------------------------
# main
# -----------------------------

def main():
    parser = argparse.ArgumentParser(description="Classify every baseline bug from its test log")
    parser.add_argument("--baseline-root", default=str(Path(__file__).resolve().parents[2] / "results/data/baseline"),
                        help="Root holding <model>/<project>/<id>/patch.java")
    parser.add_argument("--out-root", default=None,
                        help="Where to write <model>/<project>.csv "
                             "(default: same as --baseline-root)")
    parser.add_argument("--logs-root", default="test-logs/baseline",
                        help="Root holding the defects4j test logs")
    parser.add_argument("--model", required=True)
    parser.add_argument("--project", action="append", help="Restrict to these projects; repeatable or comma separated")
    parser.add_argument("--ids", type=str, help="Bug ids such as 1,2,5-7")
    args = parser.parse_args()

    projects = parse_projects_arg(args.project)
    ids = parse_id_expr(args.ids)
    baseline_root = Path(args.baseline_root)
    logs_root = Path(args.logs_root)

    project_dirs = find_project_dirs(baseline_root, args.model, projects)
    if not project_dirs:
        print(f"[warn] no project directory under {baseline_root / args.model}")
        return

    out_root = Path(args.out_root) if args.out_root else baseline_root
    out_dir = out_root / args.model
    out_dir.mkdir(parents=True, exist_ok=True)

    fieldnames = ["model", "project", "bug_id", "status"]
    summary = {}
    total = 0

    for project_dir in project_dirs:
        project = project_dir.name
        bug_ids = find_bug_ids(project_dir, ids)
        if not bug_ids:
            continue

        rows = []
        for bug_id in bug_ids:
            log_path = resolve_log_file(logs_root, args.model, project, bug_id)
            status = classify_log(log_path)
            summary[status] = summary.get(status, 0) + 1
            total += 1
            rows.append({
                "model": args.model,
                "project": project,
                "bug_id": bug_id,
                "status": status,
            })

        out_csv = out_dir / f"{project}.csv"
        with out_csv.open("w", newline="", encoding="utf-8") as f:
            w = csv.DictWriter(f, fieldnames=fieldnames, lineterminator="\n")
            w.writeheader()
            for r in rows:
                w.writerow(r)
        print(f"csv -> {out_csv}  ({len(rows)} bugs)")

    print("\n=== status summary ===")
    for k in sorted(summary):
        print(f"{k:16s} -> {summary[k]}")
    print(f"total bugs       -> {total}")


if __name__ == "__main__":
    main()
