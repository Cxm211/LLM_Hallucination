#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
DeepSeek submission script, merged across all settings.

Features:
- requests + streaming (handles keep-alive correctly)
- concurrent requests
- skips a bug whose output.json already exists, so an interrupted run resumes
- --setting selects the prompt and the data directory
- --project restricts the run to one project
- prints the reason DeepSeek reports on a 400 Bad Request
"""

import os
import json
import requests
import argparse
from pathlib import Path
from typing import Optional
from concurrent.futures import ThreadPoolExecutor, as_completed
from threading import Lock

# ======================
# configuration
# ======================
DEEPSEEK_API_KEY = os.environ.get("DEEPSEEK_API_KEY", "")  # checked in main()
BASE_URL = "https://api.deepseek.com/chat/completions"
MODEL = "deepseek-reasoner"
TEMPERATURE = 0.0
MAX_TOKENS = 60000
# ---- merged from the four per-setting submit.py scripts ------------------
# This script lives at <repo>/scripts/submit_DeepSeek.py
REPO_ROOT = Path(__file__).resolve().parents[1]
SETTINGS = ["baseline", "exm1", "exm2", "exm3"]
PROMPT_FILE = {
    "baseline": "baseline.txt",
    "exm1": "triggering_testcase_identification.txt",
    "exm2": "line_coverage_prediction.txt",
    "exm3": "additional_testcase_generation.txt",
}
# -------------------------------------------------------------------------

MAX_WORKERS = 5
print_lock = Lock()


# ======================
# helpers
# ======================
def read_file_content(file_path: Path) -> str:
    if not file_path.exists():
        raise FileNotFoundError(f"file not found: {file_path}")
    return file_path.read_text(encoding="utf-8", errors="ignore")


def find_all_input_java(input_dir: Path, project: Optional[str] = None) -> list[tuple[Path, str]]:
    """
    Scan input_dir for every input.java and return [(java_path, rel_path), ...].
    rel_path is relative to input_dir, e.g. Closure/4_4/input.java
    """
    search_root = input_dir / project if project else input_dir
    results = []
    for java_file in search_root.rglob("input.java"):
        # skip directories whose name starts with z_
        parts = java_file.relative_to(input_dir).parts
        if any(p.startswith("z_") for p in parts):
            continue
        rel_path = java_file.relative_to(input_dir)
        results.append((java_file, str(rel_path)))
    return sorted(results)


def output_path_for(out_dir: Path, rel_path: str) -> Path:
    """rel_path: Closure/4_4/input.java -> out_dir/Closure/4_4/output.json"""
    return out_dir / Path(rel_path).parent / "output.json"


def is_already_done(out_dir: Path, rel_path: str) -> bool:
    p = output_path_for(out_dir, rel_path)
    return p.exists() and p.stat().st_size > 0


def save_response(out_dir: Path, rel_path: str, response: str):
    p = output_path_for(out_dir, rel_path)
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(response, encoding="utf-8")


# ======================
# DeepSeek API call
# ======================
def call_deepseek_api(system_prompt: str, user_content: str) -> Optional[str]:
    headers = {
        "Authorization": f"Bearer {DEEPSEEK_API_KEY}",
        "Content-Type": "application/json",
    }
    payload = {
        "model": MODEL,
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user",   "content": user_content},
        ],
        "temperature": TEMPERATURE,
        "max_tokens": MAX_TOKENS,
        "stream": True,
    }

    with requests.post(BASE_URL, headers=headers, json=payload,
                       stream=True, timeout=600) as resp:
        if resp.status_code != 200:
            try:
                err_text = resp.text
            except Exception:
                err_text = "<no response body>"
            raise RuntimeError(
                f"HTTP {resp.status_code}\nResponse body:\n{err_text}"
            )

        chunks = []
        for line in resp.iter_lines(decode_unicode=True):
            if not line:
                continue
            if not line.startswith("data:"):
                continue
            data = line[5:].strip()
            if data == "[DONE]":
                break
            try:
                obj = json.loads(data)
            except json.JSONDecodeError:
                continue
            delta = obj["choices"][0]["delta"]
            text = delta.get("content") or delta.get("reasoning_content")
            if isinstance(text, str) and text:
                chunks.append(text)

        if not chunks:
            raise RuntimeError("server closed the connection without producing any output")
        return "".join(chunks)


# ======================
# one task
# ======================
def process_one(
    out_dir: Path,
    system_prompt: str,
    java_path: Path,
    rel_path: str,
) -> bool:
    try:
        if is_already_done(out_dir, rel_path):
            with print_lock:
                print(f"[skip] {rel_path}")
            return True

        with print_lock:
            print(f"[START] {rel_path}")

        java_code = read_file_content(java_path)
        user_content = (
            "Below is the Java source code for analysis.\n"
            f"[source_file]: {rel_path}\n\n"
            "===== BEGIN =====\n"
            f"{java_code}\n"
            "===== END =====\n"
        )

        response = call_deepseek_api(system_prompt, user_content)
        save_response(out_dir, rel_path, response)

        with print_lock:
            print(f"[ok] {rel_path} (len={len(response)})")
        return True

    except Exception as e:
        with print_lock:
            print(f"[fail] {rel_path}:\n{e}")
        return False


# ======================
# main
# ======================
def main():
    parser = argparse.ArgumentParser(description="DeepSeek submission, merged across all settings")
    parser.add_argument(
        "--setting", type=str, required=True, choices=SETTINGS,
        help="baseline | exm1 | exm2 | exm3",
    )
    parser.add_argument(
        "--out-dir", type=str, default=None,
        help="output root (default <repo>/generated_outputs/<setting>/DeepSeek)",
    )
    parser.add_argument(
        "--input-dir", type=str, default=None,
        help="root holding input.java (default <repo>/results/data/<setting>/DeepSeek)",
    )
    parser.add_argument(
        "--project", type=str, default=None,
        help="restrict to one project, e.g. Chart / Closure / Lang",
    )
    parser.add_argument(
        "--workers", type=int, default=MAX_WORKERS,
        help=f"number of worker threads (default {MAX_WORKERS})",
    )
    args = parser.parse_args()

    if not DEEPSEEK_API_KEY:
        raise SystemExit("DEEPSEEK_API_KEY is not set")

    system_prompt = read_file_content(REPO_ROOT / "prompt" / PROMPT_FILE[args.setting])

    if args.input_dir:
        input_dir = Path(args.input_dir).expanduser().resolve()
    else:
        input_dir = REPO_ROOT / "results" / "data" / args.setting / "DeepSeek"

    if args.out_dir:
        out_dir = Path(args.out_dir).expanduser().resolve()
    else:
        out_dir = REPO_ROOT / "generated_outputs" / args.setting / "DeepSeek"
    out_dir.mkdir(parents=True, exist_ok=True)

    if not input_dir.exists():
        raise SystemExit(f"[error] input-dir does not exist: {input_dir}")

    if args.project and not (input_dir / args.project).exists():
        raise SystemExit(f"[error] project does not exist: {input_dir / args.project}")

    java_files = find_all_input_java(input_dir, args.project)
    total = len(java_files)

    print(f"[input-dir] {input_dir}")
    print(f"[out-dir  ] {out_dir}")
    print(f"[mode] {'single project: ' + args.project if args.project else 'all projects'}")
    print(f"[ok] found {total} input.java file(s)")
    print(f"[concurrency] max_workers = {args.workers}")
    print("-" * 60)

    success = fail = 0

    with ThreadPoolExecutor(max_workers=args.workers) as executor:
        futures = {
            executor.submit(process_one, out_dir, system_prompt, java_path, rel_path): rel_path
            for java_path, rel_path in java_files
        }
        for future in as_completed(futures):
            if future.result():
                success += 1
            else:
                fail += 1

    print("\n" + "=" * 60)
    print(f"[done] ok {success} / failed {fail} / total {total}")


if __name__ == "__main__":
    main()
