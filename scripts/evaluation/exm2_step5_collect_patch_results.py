#!/usr/bin/env python3
# -*- coding: utf-8 -*-


# python exm2_step5_collect_patch_results.py --root test-logs/exm2/DeepSeek --out results/exm2/DeepSeek/z_test_results

import argparse
import csv
import os
import re
from dataclasses import dataclass
from typing import Optional, List


EXIT_LINE_RE = re.compile(r"----\s*main command exit code:\s*(\d+)\s*\|\s*([^\s]+)")
FAILING_TESTS_RE = re.compile(r"Failing tests:\s*(\d+)")


@dataclass
class LogResult:
    bug: str
    exit_code: Optional[int]
    failing_tests: Optional[int]
    status: str
    log_file: str


def parse_one_log(path: str) -> LogResult:
    """
    Parse a single log file.
    Rules:
      - exit_code == 1 => not compilable
      - exit_code == 0 => failing_tests > 0 => Not Pass, failing_tests == 0 => Pass
    """
    exit_code: Optional[int] = None
    bug: Optional[str] = None
    failing_tests: Optional[int] = None

    # read the whole file; logs are small enough that streaming is not worth it
    with open(path, "r", encoding="utf-8", errors="replace") as f:
        text = f.read()

    # 1) the main-command exit code, taking the last occurrence in case the log repeats
    exit_matches = EXIT_LINE_RE.findall(text)
    if exit_matches:
        exit_code = int(exit_matches[-1][0])
        bug = exit_matches[-1][1]

    # 2) the Failing tests count, again the last occurrence
    ft_matches = FAILING_TESTS_RE.findall(text)
    if ft_matches:
        failing_tests = int(ft_matches[-1])

    # 3) fall back to the file name when the exit-code line carried no bug name
    if not bug:
        bug = os.path.splitext(os.path.basename(path))[0]

    # 4) decide the status
    if exit_code is None:
        status = "Unknown (no exit code line)"
    elif exit_code == 1:
        status = "not compilable"
    elif exit_code == 0:
        if failing_tests is None:
            status = "Unknown (no failing tests line)"
        elif failing_tests > 0:
            status = "Not Pass"
        else:
            status = "Pass"
    else:
        status = f"Unknown (exit_code={exit_code})"

    return LogResult(
        bug=bug,
        exit_code=exit_code,
        failing_tests=failing_tests,
        status=status,
        log_file=os.path.basename(path),
    )


def write_project_csv(project_dir: str, out_dir: str, project_name: str, results: List[LogResult]) -> str:
    os.makedirs(out_dir, exist_ok=True)
    out_path = os.path.join(out_dir, f"{project_name}_results.csv")

    # sort by the number in the bug name when there is one, otherwise lexicographically
    def sort_key(r: LogResult):
        m = re.search(r"-(\d+)-buggy$", r.bug)
        return (int(m.group(1)) if m else 10**9, r.bug)

    results = sorted(results, key=sort_key)

    with open(out_path, "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(["bug", "status", "exit_code", "failing_tests", "log_file"])
        for r in results:
            w.writerow([r.bug, r.status, "" if r.exit_code is None else r.exit_code,
                        "" if r.failing_tests is None else r.failing_tests,
                        r.log_file])
    return out_path


def main():
    ap = argparse.ArgumentParser(
        description="Parse Defects4J test logs under exp2/DeepSeek/<Project> and output per-project CSV."
    )
    ap.add_argument("--root", default="test-logs/exm2/DeepSeek",
                    help="Root folder that contains per-project folders (default: test-logs/exm2/DeepSeek)")
    ap.add_argument("--out", default=None,
                    help="Output folder for CSVs (default: same as --root)")
    ap.add_argument("--pattern", default=r".*\.log$",
                    help="Regex for selecting log files (default: .*\\.log$)")
    args = ap.parse_args()

    root = args.root
    out_root = args.out or root
    file_pat = re.compile(args.pattern)

    if not os.path.isdir(root):
        raise SystemExit(f"❌ root not found or not a dir: {root}")

    # every subdirectory of root is treated as a project
    project_dirs = [d for d in os.listdir(root) if os.path.isdir(os.path.join(root, d))]
    if not project_dirs:
        raise SystemExit(f"❌ no project dirs under: {root}")

    for project in sorted(project_dirs):
        pdir = os.path.join(root, project)
        logs = [
            os.path.join(pdir, fn)
            for fn in os.listdir(pdir)
            if file_pat.match(fn) and os.path.isfile(os.path.join(pdir, fn))
        ]

        if not logs:
            print(f"⚠️  {project}: no log files matched.")
            continue

        results: List[LogResult] = []
        for lp in sorted(logs):
            try:
                results.append(parse_one_log(lp))
            except Exception as e:
                # one bad file must not stop the rest
                results.append(LogResult(
                    bug=os.path.splitext(os.path.basename(lp))[0],
                    exit_code=None,
                    failing_tests=None,
                    status=f"ParseError: {e}",
                    log_file=os.path.basename(lp),
                ))

        out_csv = write_project_csv(pdir, out_root, project, results)
        print(f"✅ {project}: wrote {out_csv} ({len(results)} rows)")


if __name__ == "__main__":
    main()
