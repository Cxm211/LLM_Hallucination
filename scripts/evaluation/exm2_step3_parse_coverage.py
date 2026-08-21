#!/usr/bin/env python3
# -*- coding: utf-8 -*-

'''
python exm2_step3_parse_coverage.py defects4j-GPT4o \
  --methods-dir results/exm2/DeepSeek/z_fixed_info \
  -o results/exm2/DeepSeek/z_fixed_jacoco \
  --error-log results/exm2/DeepSeek/z_fixed_jacoco/errors.csv \
  --project Cli
'''

import argparse
import csv
import os
import xml.etree.ElementTree as ET
from typing import Dict, List, Tuple, Optional

# key: (pkg, sourcefile, line) -> (ci, mi, cb, mb)
JacocoMap = Dict[Tuple[str, str, int], Tuple[int, int, int, int]]

def parse_jacoco_to_map(xml_path: str) -> JacocoMap:
    tree = ET.parse(xml_path)
    root = tree.getroot()

    m: JacocoMap = {}
    for pkg in root.findall(".//package"):
        pkg_name = pkg.get("name", "")  # e.g., org/jfree/chart
        for sf in pkg.findall("./sourcefile"):
            sf_name = sf.get("name", "")  # e.g., Foo.java
            for ln in sf.findall("./line"):
                nr = int(ln.get("nr", "0"))
                mi = int(ln.get("mi", "0"))
                ci = int(ln.get("ci", "0"))
                mb = int(ln.get("mb", "0"))
                cb = int(ln.get("cb", "0"))
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


def guess_sourcefile_name(file_path: str) -> str:
    return os.path.basename(file_path)


def guess_pkg_from_class_fqcn(class_fqcn: str) -> str:
    """
    Java FQCN: org.jfree.chart.plot.CategoryPlot
    JaCoCo pkg: org/jfree/chart/plot
    """
    class_fqcn = (class_fqcn or "").strip()
    if not class_fqcn or "." not in class_fqcn:
        return ""
    pkg_dot = ".".join(class_fqcn.split(".")[:-1])
    return pkg_dot.replace(".", "/")


def list_methods_csvs(methods_dir: str) -> List[str]:
    files = []
    for name in os.listdir(methods_dir):
        if name.endswith("_methods.csv"):
            files.append(os.path.join(methods_dir, name))
    return sorted(files)


def project_name_from_methods_csv(path: str) -> str:
    """
    bug_methods/chart_methods.csv -> Chart
    bug_methods/cli_methods.csv   -> Cli
    """
    base = os.path.basename(path)
    proj = base.replace("_methods.csv", "").strip()
    if not proj:
        return "UNKNOWN_PROJECT"
    return proj[0].upper() + proj[1:]


def main():
    ap = argparse.ArgumentParser(
        description="Extract JaCoCo per-line info for buggy method ranges; one CSV per project; add executed/line_in_method/function_id."
    )
    ap.add_argument("defects_root", help="Root dir, e.g. defects4j_buggy")
    ap.add_argument("--methods-dir", required=True, help="Dir containing *_methods.csv (e.g. scripts/evaluation/bug_methods)")
    ap.add_argument("-o", "--out-dir", default="jacoco_output", help="Output dir for per-project CSVs")
    ap.add_argument("--error-log", default=None, help="Optional error log CSV path")

    # only add: project filter
    ap.add_argument("--project", default="all",
                    help="Only process this project (e.g. Cli). Use 'all' to process everything (default).")

    args = ap.parse_args()

    os.makedirs(args.out_dir, exist_ok=True)

    # error log
    err_f = None
    err_w = None
    if args.error_log:
        os.makedirs(os.path.dirname(args.error_log) or ".", exist_ok=True)
        err_f = open(args.error_log, "w", newline="", encoding="utf-8")
        err_w = csv.writer(err_f)
        err_w.writerow(["project", "bug_id", "folder", "jacoco_xml", "error"])

    methods_csvs = list_methods_csvs(args.methods_dir)
    if not methods_csvs:
        raise SystemExit(f"No *_methods.csv found in: {args.methods_dir}")

    # Cache parsed jacoco maps per jacoco.xml (avoid reparsing repeatedly)
    jacoco_cache: Dict[str, JacocoMap] = {}

    project_filter = (args.project or "all").strip().lower()

    for methods_csv in methods_csvs:
        project = project_name_from_methods_csv(methods_csv)

        # project filter
        if project_filter != "all" and project.strip().lower() != project_filter:
            continue

        out_path = os.path.join(args.out_dir, f"{project}.csv")
        print(f"[PROJECT] {project} <= {methods_csv}", flush=True)

        # read methods
        with open(methods_csv, "r", encoding="utf-8") as f_in:
            reader = csv.DictReader(f_in)
            method_rows = [r for r in reader]

        # group by (bug_id, folder) assign function_id
        groups: Dict[Tuple[str, str], List[dict]] = {}
        for r in method_rows:
            bug_id = (r.get("bug_id", "") or "").strip()
            folder = (r.get("folder", "") or "").strip()
            groups.setdefault((bug_id, folder), []).append(r)

        for (bug_id, folder), lst in groups.items():
            for idx, r in enumerate(lst, start=1):
                r["_function_id"] = f"function_{idx}"

        # write project CSV
        with open(out_path, "w", newline="", encoding="utf-8") as f_out:
            writer = csv.writer(f_out)
            writer.writerow([
                "project", "bug_id", "folder",
                "function_id",
                "class_fqcn", "file", "method_sig_snippet",
                "method_start", "method_end",
                "line", "line_in_method",
                "ci", "mi", "cb", "mb",
                "executed"
            ])

            for r in method_rows:
                bug_id = (r.get("bug_id", "") or "").strip()
                folder = (r.get("folder", "") or "").strip()
                function_id = (r.get("_function_id", "") or "").strip() or "function_1"

                class_fqcn = (r.get("class_fqcn", "") or "").strip()
                file_path = (r.get("file", "") or "").strip()
                method_sig = (r.get("method_sig_snippet", "") or "").strip()
                # bug_methods/ carries buggy_method_*, the CSVs step1 writes carry fixed_method_*
                start = norm_int(r.get("fixed_method_start_line")
                                 or r.get("buggy_method_start_line", "0"))
                end = norm_int(r.get("fixed_method_end_line")
                               or r.get("buggy_method_end_line", "0"))

                jacoco_xml = os.path.join(args.defects_root, project, folder, "jacoco.xml")
                print(f"  [PROCESS] {bug_id} {function_id} folder={folder} lines={start}-{end}", flush=True)

                # ✅ CHANGED: if jacoco.xml missing, DO NOT SKIP
                if not os.path.isfile(jacoco_xml):
                    msg = "Missing jacoco.xml"
                    print(f"  [NOT_COMPILABLE] {msg} -> {jacoco_xml}", flush=True)
                    if err_w:
                        err_w.writerow([project, bug_id, folder, jacoco_xml, msg])

                    # write rows with executed = Not Compilable, ci/mi/cb/mb empty
                    for line in range(start, end + 1):
                        line_in_method = (line - start) + 1
                        writer.writerow([
                            project, bug_id, folder,
                            function_id,
                            class_fqcn, file_path, method_sig,
                            start, end,
                            line, line_in_method,
                            "", "", "", "",
                            "Not Compilable"
                        ])
                    continue

                # load jacoco map with caching
                if jacoco_xml not in jacoco_cache:
                    m, err = safe_parse_jacoco_to_map(jacoco_xml)
                    if err is not None or m is None:
                        print(f"  [SKIP] {err} -> {jacoco_xml}", flush=True)
                        if err_w:
                            err_w.writerow([project, bug_id, folder, jacoco_xml, err])
                        continue
                    jacoco_cache[jacoco_xml] = m

                jacoco_map = jacoco_cache[jacoco_xml]

                sf_name = guess_sourcefile_name(file_path)
                pkg_guess = guess_pkg_from_class_fqcn(class_fqcn)

                for line in range(start, end + 1):
                    line_in_method = (line - start) + 1

                    # default: missing
                    ci = mi = cb = mb = None

                    # 1) exact match (package + sourcefile + line)
                    if pkg_guess:
                        v = jacoco_map.get((pkg_guess, sf_name, line))
                        if v is not None:
                            ci, mi, cb, mb = v

                    # 2) fallback: any package with same sourcefile+line
                    if ci is None:
                        for (pkg, sf, nr), v in jacoco_map.items():
                            if sf == sf_name and nr == line:
                                ci, mi, cb, mb = v
                                break

                    # executed derived only when we have numbers
                    if ci is None:
                        executed = ""
                        writer.writerow([
                            project, bug_id, folder,
                            function_id,
                            class_fqcn, file_path, method_sig,
                            start, end,
                            line, line_in_method,
                            "", "", "", "",
                            executed
                        ])
                    else:
                        executed = (ci > 0) or (cb > 0)
                        writer.writerow([
                            project, bug_id, folder,
                            function_id,
                            class_fqcn, file_path, method_sig,
                            start, end,
                            line, line_in_method,
                            ci, mi, cb, mb,
                            executed
                        ])

        print(f"[OK] wrote {out_path}", flush=True)

    if err_f:
        err_f.close()
        print(f"[OK] error log -> {args.error_log}", flush=True)

    print("Done.", flush=True)


if __name__ == "__main__":
    main()
