#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import argparse
import re
from pathlib import Path
from typing import Any, Optional

import pandas as pd


# ---------- helpers ----------

def is_blank(x: Any) -> bool:
    """Treat NaN, None, and whitespace-only strings alike as blank."""
    if x is None:
        return True
    try:
        if pd.isna(x):
            return True
    except Exception:
        pass
    return str(x).strip() == ""


def normalize_for_compare(x: Any) -> Optional[Any]:
    """
    Normalise a cell so executed and prediction compare robustly:
      blank -> None
    - true/false/1/0/yes/no -> bool
      anything else -> the stripped string
    """
    if is_blank(x):
        return None
    s = str(x).strip()
    sl = s.lower()
    if sl in {"true", "1", "yes", "y", "t"}:
        return True
    if sl in {"false", "0", "no", "n", "f"}:
        return False
    return s


# ---------- scoring ----------

def prf_from_counts(n_actual: int, n_pred: int, n_inter: int):
    """
    Derive (precision, recall, f1) from three counts:
      n_actual = |L_b(T_b)|        lines actually executed
      n_pred   = |L_hat_b(T_b)|    lines predicted to execute
      n_inter  = |L_b n L_hat_b|   lines in both

    Edge cases, matching the definition in the paper:
      both sets empty          -> (1, 1, 1), counted as fully correct
      L non-empty, L_hat empty -> (0, 0, 0)
      - precision + recall == 0 -> f1 = 0
    """
    if n_actual == 0 and n_pred == 0:
        return 1.0, 1.0, 1.0
    precision = (n_inter / n_pred) if n_pred > 0 else 0.0
    recall = (n_inter / n_actual) if n_actual > 0 else 0.0
    f1 = (2 * precision * recall / (precision + recall)) if (precision + recall) > 0 else 0.0
    return precision, recall, f1


def compute_exec_pred_score(
    df: pd.DataFrame,
    bug_id_col: str = "bug_id",
    executed_col: str = "executed",
    pred_col: str = "prediction",
    prefix: str = "",
) -> pd.DataFrame:
    """
    Score precision / recall / f1 per bug_id over the informative lines.

    Informative lines are those whose executed cell is not blank. A blank executed cell
    marks a line filtered out beforehand, i.e. blank, comment-only, or brace-only, and
    those lines are excluded entirely.
      L_b     = informative lines with executed True   (actually executed)
      L_hat_b = informative lines with prediction True (predicted to execute)
    precision = |L ∩ L_hat| / |L_hat|,  recall = |L ∩ L_hat| / |L|,
    f1 = 2PR/(P+R). See prf_from_counts for the empty-set cases.

    Returns: bug_id, {prefix}precision, {prefix}recall, {prefix}f1
    """
    for c in (bug_id_col, executed_col, pred_col):
        if c not in df.columns:
            raise ValueError(f"Missing required column '{c}'. Columns={list(df.columns)}")

    informative = ~df[executed_col].apply(is_blank)
    sub = df.loc[informative, [bug_id_col, executed_col, pred_col]].copy()
    sub["_actual"] = sub[executed_col].apply(normalize_for_compare) == True   # noqa: E712
    sub["_pred"] = sub[pred_col].apply(normalize_for_compare) == True         # noqa: E712

    rows = []
    for bid, g in sub.groupby(bug_id_col):
        n_actual = int(g["_actual"].sum())
        n_pred = int(g["_pred"].sum())
        n_inter = int((g["_actual"] & g["_pred"]).sum())
        p, r, f1 = prf_from_counts(n_actual, n_pred, n_inter)
        rows.append({"bug_id": int(bid), "precision": p, "recall": r, "f1": f1})

    out = pd.DataFrame(rows, columns=["bug_id", "precision", "recall", "f1"])
    if prefix:
        out = out.rename(columns={
            "precision": f"{prefix}precision",
            "recall": f"{prefix}recall",
            "f1": f"{prefix}f1",
        })
    return out


def extract_bug_id_from_bug_field(bug_field: Any) -> Optional[int]:
    """
    The bug column of test_results looks like 'Chart-12-buggy', where the number in the
    middle is the bug id. Taking the first integer in the string is the robust way to read it.
    """
    if is_blank(bug_field):
        return None
    m = re.search(r"(\d+)", str(bug_field))
    return int(m.group(1)) if m else None


def status_to_label(st: Any) -> str:
    """Map a raw status onto pass / notpass / not_compilable / timeout.

    Anything else, including unknown, folds into notpass."""
    s = str(st).strip().lower()
    if s == "pass":
        return "pass"
    if s == "not compilable":
        return "not_compilable"
    if "exit_code=124" in s:          # killed by the timeout, exit 124
        return "timeout"
    return "notpass"                  # not pass, and anything unknown


# precedence when one bug_id carries several rows
_STATUS_PRIORITY = ["pass", "notpass", "not_compilable", "timeout"]


def compute_code_score(
    test_df: pd.DataFrame,
    bug_col: str = "bug",
    status_col: str = "status",
) -> pd.DataFrame:
    """
    code_score:
      status == Pass -> 1
      anything else, i.e. Not Pass / not compilable / timeout -> 0
    code_status keeps the four states pass / notpass / not_compilable / timeout.
    When one bug_id has several rows, code_score is 1 if any of them passed, and
    code_status is picked by precedence: pass > notpass > not_compilable > timeout.

    Returns: bug_id, code_score, code_status
    """
    for c in (bug_col, status_col):
        if c not in test_df.columns:
            raise ValueError(f"Missing required column '{c}'. Columns={list(test_df.columns)}")

    tmp = test_df.copy()
    tmp["bug_id"] = tmp[bug_col].apply(extract_bug_id_from_bug_field)
    tmp = tmp.dropna(subset=["bug_id"])
    tmp["bug_id"] = tmp["bug_id"].astype(int)
    tmp["label"] = tmp[status_col].apply(status_to_label)

    rows = []
    for bid, g in tmp.groupby("bug_id"):
        labels = set(g["label"])
        status = next(l for l in _STATUS_PRIORITY if l in labels)
        rows.append({"bug_id": bid,
                     "code_score": 1 if status == "pass" else 0,
                     "code_status": status})
    return pd.DataFrame(rows, columns=["bug_id", "code_score", "code_status"])


def read_csv_if_exists(p: Path) -> Optional[pd.DataFrame]:
    return pd.read_csv(p) if (p.exists() and p.is_file()) else None


# ---------- main ----------

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--prediction_root", type=str, default="z_prediction")
    ap.add_argument("--buggy_subdir", type=str, default="buggy")
    ap.add_argument("--fixed_subdir", type=str, default="fixed")
    ap.add_argument("--test_results_root", type=str, default="z_test_results")
    ap.add_argument("--out_dir", type=str, default="project_scores")
    args = ap.parse_args()

    pred_root = Path(args.prediction_root)
    buggy_dir = pred_root / args.buggy_subdir
    fixed_dir = pred_root / args.fixed_subdir
    test_root = Path(args.test_results_root)

    out_dir = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    if not buggy_dir.exists():
        raise FileNotFoundError(f"Buggy folder not found: {buggy_dir}")

    buggy_files = sorted([p for p in buggy_dir.glob("*.csv") if p.is_file()])
    if not buggy_files:
        raise FileNotFoundError(f"No project CSV found under: {buggy_dir}")

    for buggy_path in buggy_files:
        project = buggy_path.stem  # Chart / Cli / Closure ...

        fixed_path = fixed_dir / f"{project}.csv"
        test_path = test_root / f"{project}_results.csv"

        # bug prediction: precision / recall / f1
        buggy_df = pd.read_csv(buggy_path)
        bug_score_df = compute_exec_pred_score(buggy_df, prefix="bug_")

        # fix prediction: precision / recall / f1
        fixed_df = read_csv_if_exists(fixed_path)
        if fixed_df is not None:
            fix_score_df = compute_exec_pred_score(fixed_df, prefix="fix_")
        else:
            fix_score_df = pd.DataFrame(columns=["bug_id", "fix_precision", "fix_recall", "fix_f1"])

        # code score
        test_df = read_csv_if_exists(test_path)
        if test_df is not None:
            code_score_df = compute_code_score(test_df)
        else:
            code_score_df = pd.DataFrame(columns=["bug_id", "code_score", "code_status"])

        # outer join on bug_id, missing values filled with 0
        merged = (
            bug_score_df.merge(fix_score_df, on="bug_id", how="outer")
            .merge(code_score_df, on="bug_id", how="outer")
        )

        prf_cols = [
            "bug_precision", "bug_recall", "bug_f1",
            "fix_precision", "fix_recall", "fix_f1",
            "code_score",
        ]
        for c in prf_cols:
            if c not in merged.columns:
                merged[c] = 0
        merged[prf_cols] = merged[prf_cols].fillna(0)
        # a bug with no test result keeps an empty code_status
        if "code_status" not in merged.columns:
            merged["code_status"] = ""
        merged["code_status"] = merged["code_status"].fillna("")

        # An uncompilable patch cannot be executed, so the model-patched program has no
        # coverage at all and every fix_ score is forced to 0.
        uncompilable = merged["code_status"] == "not_compilable"
        merged.loc[uncompilable, ["fix_precision", "fix_recall", "fix_f1"]] = 0.0

        # bug_score and fix_score are kept as aliases of the headline F1
        merged["bug_score"] = merged["bug_f1"].astype(float)
        merged["fix_score"] = merged["fix_f1"].astype(float)

        merged = merged.sort_values("bug_id").reset_index(drop=True)

        out_path = out_dir / f"{project}.csv"
        merged[[
            "bug_id",
            "bug_precision", "bug_recall", "bug_f1",
            "fix_precision", "fix_recall", "fix_f1",
            "bug_score", "fix_score", "code_status",
        ]].to_csv(out_path, index=False, lineterminator="\n")
        print(f"[OK] {project} -> {out_path}")

    print(f"Done. Output folder: {out_dir.resolve()}")


if __name__ == "__main__":
    main()
