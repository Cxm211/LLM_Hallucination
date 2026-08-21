"""
Read:
  z_buggy_jacoco/<Project>.csv
  z_fixed_jacoco/<Project>.csv

Add prediction column using:
  exm2/DeepSeek/<Project>/<id>/prediction.json

Write:
  <out_dir>/buggy/<Project>.csv
  <out_dir>/fixed/<Project>.csv

prediction rule:
  buggy  -> expn[function_k]["buggy"]
  fixed  -> expn[function_k]["fixed"]

python exm2_step4_align_prediction.py \
  --buggy-dir results/exm2/DeepSeek/z_buggy_jacoco \
  --fixed-dir results/exm2/DeepSeek/z_fixed_jacoco \
  --expn-root  results/exm2/DeepSeek \
  -o results/exm2/DeepSeek/z_prediction

# only one project
python exm2_step4_align_prediction.py \
  --buggy-dir results/exm2/DeepSeek/z_buggy_jacoco \
  --fixed-dir results/exm2/DeepSeek/z_fixed_jacoco \
  --expn-root  results/exm2/DeepSeek \
  -o results/exm2/DeepSeek/z_prediction \
  --project Cli
"""

import argparse
import csv
import json
import re
from pathlib import Path
from typing import Dict, Any, Optional, Tuple

# Chart-14-buggy / Chart-14_1-buggy
FOLDER_RE = re.compile(r"^(?P<proj>[A-Za-z]+)-(?P<id>\d+)(?:_(?P<suf>\d+))?-buggy$")


def parse_folder(folder: str) -> Tuple[Optional[str], Optional[str], Optional[str]]:
    m = FOLDER_RE.match(folder)
    if not m:
        return None, None, None
    return m.group("proj"), m.group("id"), m.group("suf")


def load_expn(expn_root: Path, project: str, bug_id: str, suffix: Optional[str]) -> Optional[Dict[str, Any]]:
    paths = []
    if suffix:
        paths.append(expn_root / project / f"{bug_id}_{suffix}" / "prediction.json")
    paths.append(expn_root / project / bug_id / "prediction.json")

    for p in paths:
        if p.is_file():
            return json.loads(p.read_text(encoding="utf-8"))
    return None


def predict(expn: Dict[str, Any], function_id: str, mode: str, line_in_method: int) -> Optional[bool]:
    """
    mode: buggy / fixed
    """
    try:
        lst = expn[function_id][mode]
        return line_in_method in set(int(x) for x in lst)
    except Exception:
        return None


def process_one(
    in_csv: Path,
    out_csv: Path,
    expn_root: Path,
    mode: str,
):
    out_csv.parent.mkdir(parents=True, exist_ok=True)

    with in_csv.open("r", encoding="utf-8") as fin, out_csv.open("w", encoding="utf-8", newline="") as fout:
        r = csv.DictReader(fin)
        w = csv.writer(fout)
        w.writerow(["bug_id", "folder", "function_id", "line_in_method", "executed", "prediction"])

        expn_cache: Dict[Tuple[str, str, Optional[str]], Optional[Dict[str, Any]]] = {}

        for row in r:
            bug_id = row["bug_id"].strip()
            folder = row["folder"].strip()
            function_id = row["function_id"].strip()
            line_in_method = int(row["line_in_method"])
            executed = row["executed"]

            proj, bid, suf = parse_folder(folder)
            proj = proj or in_csv.stem
            bid = bid or bug_id

            key = (proj, bid, suf)
            if key not in expn_cache:
                expn_cache[key] = load_expn(expn_root, proj, bid, suf)

            expn = expn_cache[key]
            if expn is None:
                pred = ""
            else:
                hit = predict(expn, function_id, mode, line_in_method)
                pred = "TRUE" if hit else "FALSE"

            w.writerow([bug_id, folder, function_id, line_in_method, executed, pred])


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--buggy-dir", required=True)
    ap.add_argument("--fixed-dir", required=True)
    ap.add_argument("--expn-root", required=True)
    ap.add_argument("-o", "--out-dir", required=True)

    # several projects, comma separated
    ap.add_argument("--projects", default="ALL")

    # a single project
    ap.add_argument("--project", default=None, help="Only process one project (e.g. Cli). Overrides --projects.")

    args = ap.parse_args()

    buggy_dir = Path(args.buggy_dir)
    fixed_dir = Path(args.fixed_dir)
    expn_root = Path(args.expn_root)
    out_dir = Path(args.out_dir)

    buggy_out = out_dir / "buggy"
    fixed_out = out_dir / "fixed"

    wanted = None

    # --project wins when given
    if args.project:
        wanted = {args.project.strip()}
    else:
        # otherwise --projects, either ALL or a comma separated list
        if args.projects != "ALL":
            wanted = {p.strip() for p in args.projects.split(",")}

    wanted_lower = {w.lower() for w in wanted} if wanted else None

    for buggy_csv in sorted(buggy_dir.glob("*.csv")):
        project = buggy_csv.stem
        if wanted_lower and project.lower() not in wanted_lower:
            continue

        fixed_csv = fixed_dir / f"{project}.csv"
        if not fixed_csv.exists():
            print(f"[SKIP] missing fixed csv: {fixed_csv}")
            continue

        print(f"[PROJECT] {project}")

        process_one(
            buggy_csv,
            buggy_out / f"{project}.csv",
            expn_root,
            mode="buggy",
        )

        process_one(
            fixed_csv,
            fixed_out / f"{project}.csv",
            expn_root,
            mode="fixed",
        )

        print(f"  -> buggy/{project}.csv")
        print(f"  -> fixed/{project}.csv")

    print("Done.")


if __name__ == "__main__":
    main()
