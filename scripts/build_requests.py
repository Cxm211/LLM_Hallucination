#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Build the API request payloads for every (setting, model) combination in the study.

This merges the eight per-setting `get_batch.py` scripts into one. It reads the
system prompt from <repo>/prompt/ and the task inputs from <repo>/results/data/, and
writes the generated payloads to a separate output root so nothing under results/ is
overwritten.

    <repo>/
    ├── prompt/<task>.txt                              system instruction
    ├── results/data/<setting>/<model>/<Project>/<BugID>/input.java   task input
    └── generated_requests/<setting>/<model>/<Project>/         output (default)

Usage
    python scripts/build_requests.py --setting exm3 --model Claude
    python scripts/build_requests.py --setting exm3 --model Claude --project Lang
    python scripts/build_requests.py --all
    python scripts/build_requests.py --all --out-root /tmp/req

Providers
    Claude    Anthropic Message Batches  -> <Project>/batch.json, batch1.json, ...
    GPT5      OpenAI Batch (/v1/responses) -> <Project>/requests.jsonl, requests1.jsonl, ...

DeepSeek has no batch endpoint, so it has no payload stage at all: scripts/submit_DeepSeek.py
builds each request in memory and calls the synchronous API directly.

Submitting
    Anthropic  curl https://api.anthropic.com/v1/messages/batches \\
                 -H "x-api-key: $ANTHROPIC_API_KEY" \\
                 -H "anthropic-version: 2023-06-01" \\
                 -H "content-type: application/json" \\
                 --data @generated_requests/exm3/Claude/Lang/batch.json
    OpenAI     openai batches create -f generated_requests/exm3/GPT5/Lang/requests.jsonl
"""

import argparse
import json
from collections import Counter, defaultdict
from pathlib import Path
from typing import Any, Dict, List, Tuple

# --- repository-anchored paths -------------------------------------------
_HERE = Path(__file__).resolve()
REPO_ROOT = _HERE.parents[1]
PROMPT_DIR = REPO_ROOT / "prompt"
DATA_ROOT = REPO_ROOT / "results" / "data"
DEFAULT_OUT_ROOT = REPO_ROOT / "generated_requests"
# -------------------------------------------------------------------------

SETTINGS = ["baseline", "exm1", "exm2", "exm3"]
MODELS = ["Claude", "GPT5"]   # DeepSeek has no batch endpoint; see submit_DeepSeek.py

# setting -> the prompt file in <repo>/prompt/
PROMPT_FILE = {
    "baseline": "baseline.txt",
    "exm1": "triggering_testcase_identification.txt",
    "exm2": "line_coverage_prediction.txt",
    "exm3": "additional_testcase_generation.txt",
}

PROVIDER = {"Claude": "anthropic", "GPT5": "openai"}

# (model, setting) -> request parameters, as used in the original runs
PARAMS: Dict[Tuple[str, str], Dict[str, Any]] = {
    **{("Claude", s): dict(model="claude-sonnet-4-5", max_tokens=40000, chunk=50)
       for s in SETTINGS},
    **{("GPT5", s): dict(model="gpt-5", max_tokens=40000, chunk=20)
       for s in ("baseline", "exm1", "exm2")},
    ("GPT5", "exm3"): dict(model="gpt-5", max_tokens=40000, chunk=15),
}

USER_TEMPLATE = (
    "Below is the Java source code for analysis. "
    "Please follow the system instructions strictly to complete the task.\n"
    "[source_file]: {rel}\n\n"
    "===== BEGIN =====\n"
    "{code}\n"
    "===== END =====\n"
)



def read_text(p: Path) -> str:
    if not p.exists():
        raise FileNotFoundError(f"not found: {p}")
    return p.read_text(encoding="utf-8", errors="ignore")


def natural_key(s: str) -> List[Any]:
    """Sort '10' after '9' and '4_2' after '4'."""
    return [int(p) if p.isdigit() else p for p in s.replace("_", " ").split()]


def collect_inputs(project_dir: Path) -> Tuple[Dict[str, List[Path]], Counter]:
    """Group every input.java under a project by its first-level subfolder (the bug id)."""
    by_id: Dict[str, List[Path]] = defaultdict(list)
    for fp in sorted(project_dir.rglob("input.java")):
        parts = fp.relative_to(project_dir).parts
        bug_id = parts[0] if len(parts) >= 2 else fp.parent.name
        by_id[bug_id].append(fp)
    return by_id, Counter({k: len(v) for k, v in by_id.items()})


def build_records(project_dir: Path, sys_inst: str, model: str, setting: str) -> List[Dict[str, Any]]:
    """One request record per input.java, in the provider's payload format."""
    provider = PROVIDER[model]
    cfg = PARAMS[(model, setting)]
    by_id, counts = collect_inputs(project_dir)
    has_dup = any(c > 1 for c in counts.values())
    project = project_dir.name
    records: List[Dict[str, Any]] = []

    for bug_id in sorted(by_id, key=natural_key):
        for idx, fp in enumerate(sorted(by_id[bug_id])):
            rel = fp.relative_to(project_dir).as_posix()
            code = read_text(fp)
            custom_id = f"{project}_{bug_id}"
            if has_dup and counts[bug_id] > 1:
                custom_id = f"{custom_id}-{idx + 1}"

            user = USER_TEMPLATE.format(rel=rel, code=code)

            if provider == "anthropic":
                records.append({
                    "custom_id": custom_id,
                    "params": {
                        "model": cfg["model"],
                        "max_tokens": cfg["max_tokens"],
                        "system": sys_inst,
                        "messages": [{"role": "user", "content": user}],
                    },
                })
            else:  # openai
                records.append({
                    "custom_id": custom_id,
                    "method": "POST",
                    "url": "/v1/responses",
                    "body": {
                        "model": cfg["model"],
                        "max_output_tokens": cfg["max_tokens"],
                        "reasoning": {"effort": "low"},
                        "text": {"verbosity": "low"},
                        "input": [
                            {"role": "system", "content": sys_inst},
                            {"role": "user", "content": user},
                        ],
                    },
                })
    return records


def write_payloads(records: List[Dict[str, Any]], out_dir: Path,
                   model: str, setting: str) -> List[Path]:
    """Chunk and write, in the layout each provider's batch endpoint expects."""
    if not records:
        return []
    provider = PROVIDER[model]
    chunk = PARAMS[(model, setting)]["chunk"] or len(records)
    out_dir.mkdir(parents=True, exist_ok=True)
    written: List[Path] = []

    for i in range(0, len(records), chunk):
        part = records[i:i + chunk]
        n = i // chunk
        if provider == "anthropic":
            fp = out_dir / (f"batch{n}.json" if n else "batch.json")
            fp.write_text(json.dumps({"requests": part}, ensure_ascii=False, indent=2),
                          encoding="utf-8")
        else:
            fp = out_dir / (f"requests{n}.jsonl" if n else "requests.jsonl")
            fp.write_text("".join(json.dumps(r, ensure_ascii=False) + "\n" for r in part),
                          encoding="utf-8")
        written.append(fp)
    return written


def discover_projects(root: Path) -> List[Path]:
    return sorted(d for d in root.iterdir()
                  if d.is_dir()
                  and not d.name.startswith(("z_", "__", "."))
                  and any(d.rglob("input.java")))


def run(setting: str, model: str, project: str, out_root: Path, dry_run: bool) -> int:
    data_dir = DATA_ROOT / setting / model
    if not data_dir.is_dir():
        print(f"[skip] no input directory: {data_dir}")
        return 0

    sys_inst = read_text(PROMPT_DIR / PROMPT_FILE[setting])
    projects = [data_dir / project] if project else discover_projects(data_dir)
    if project and not projects[0].is_dir():
        raise SystemExit(f"[error] no such project: {projects[0]}")

    total = 0
    for pdir in projects:
        records = build_records(pdir, sys_inst, model, setting)
        if not records:
            continue
        out_dir = out_root / setting / model / pdir.name
        files = [] if dry_run else write_payloads(records, out_dir, model, setting)
        total += len(records)
        suffix = "(dry run)" if dry_run else f"-> {len(files)} file(s)"
        print(f"  {setting}/{model}/{pdir.name}: {len(records)} requests {suffix}")
    return total


def main() -> None:
    ap = argparse.ArgumentParser(
        description="Build API request payloads for every (setting, model) in the study.")
    ap.add_argument("--setting", choices=SETTINGS, help="baseline | exm1 | exm2 | exm3")
    ap.add_argument("--model", choices=MODELS, help="Claude | GPT5")
    ap.add_argument("--project", help="a single Defects4J project, e.g. Lang; default all")
    ap.add_argument("--all", action="store_true", help="every setting and every model")
    ap.add_argument("--out-root", type=Path, default=DEFAULT_OUT_ROOT,
                    help=f"output root (default {DEFAULT_OUT_ROOT.relative_to(REPO_ROOT)}/)")
    ap.add_argument("--dry-run", action="store_true",
                    help="report what would be built without writing files")
    args = ap.parse_args()

    if not args.all and not (args.setting and args.model):
        ap.error("give --setting and --model, or --all")

    combos = ([(s, m) for s in SETTINGS for m in MODELS] if args.all
              else [(args.setting, args.model)])

    grand = 0
    for setting, model in combos:
        grand += run(setting, model, args.project, args.out_root, args.dry_run)
    verb = "would be written" if args.dry_run else "written"
    print(f"\n[done] {grand} requests {verb} under {args.out_root}")


if __name__ == "__main__":
    main()
