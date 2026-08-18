#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
DeepSeek 批处理脚本（exm1）

功能：
- requests + streaming（正确处理 keep-alive）
- 并行处理请求
- 已存在 output.json 自动跳过（断点续跑）
- --input-dir 指定 input.java 来源（默认 ../Claude）
- --project 指定单个 project
- 400 Bad Request 时打印 DeepSeek 返回的真实原因
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
# 配置
# ======================
DEEPSEEK_API_KEY = os.environ["DEEPSEEK_API_KEY"]  # set this in your environment
BASE_URL = "https://api.deepseek.com/chat/completions"
MODEL = "deepseek-reasoner"
TEMPERATURE = 0.0
MAX_TOKENS = 60000
PROMPT_FILE = "prompt.txt"

MAX_WORKERS = 5
print_lock = Lock()


# ======================
# 工具函数
# ======================
def read_file_content(file_path: Path) -> str:
    if not file_path.exists():
        raise FileNotFoundError(f"找不到文件: {file_path}")
    return file_path.read_text(encoding="utf-8", errors="ignore")


def find_all_input_java(input_dir: Path, project: Optional[str] = None) -> list[tuple[Path, str]]:
    """
    在 input_dir 下扫描所有 input.java，返回 [(java_path, rel_path), ...]
    rel_path 相对于 input_dir（如 Closure/4_4/input.java）
    """
    search_root = input_dir / project if project else input_dir
    results = []
    for java_file in search_root.rglob("input.java"):
        # 跳过 z_ 开头的目录
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
# DeepSeek 调用
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
            raise RuntimeError("服务器结束连接但未产生任何输出")
        return "".join(chunks)


# ======================
# 单任务
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
                print(f"[跳过] {rel_path}")
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
            print(f"[成功] {rel_path} (len={len(response)})")
        return True

    except Exception as e:
        with print_lock:
            print(f"[失败] {rel_path}:\n{e}")
        return False


# ======================
# main
# ======================
def main():
    parser = argparse.ArgumentParser(description="DeepSeek 批处理（exm1）")
    parser.add_argument(
        "--input-dir", type=str, default=None,
        help="input.java 所在根目录（默认：本脚本所在目录）",
    )
    parser.add_argument(
        "--project", type=str, default=None,
        help="只处理指定 project，例如 Chart / Closure / Lang",
    )
    parser.add_argument(
        "--workers", type=int, default=MAX_WORKERS,
        help=f"并发线程数（默认 {MAX_WORKERS}）",
    )
    args = parser.parse_args()

    if not DEEPSEEK_API_KEY:
        raise SystemExit("请设置 DEEPSEEK_API_KEY 环境变量")

    out_dir = Path(__file__).parent.resolve()
    system_prompt = read_file_content(out_dir / PROMPT_FILE)

    if args.input_dir:
        input_dir = Path(args.input_dir).expanduser().resolve()
    else:
        input_dir = out_dir

    if not input_dir.exists():
        raise SystemExit(f"[错误] input-dir 不存在: {input_dir}")

    if args.project and not (input_dir / args.project).exists():
        raise SystemExit(f"[错误] project 不存在: {input_dir / args.project}")

    java_files = find_all_input_java(input_dir, args.project)
    total = len(java_files)

    print(f"[input-dir] {input_dir}")
    print(f"[out-dir  ] {out_dir}")
    print(f"[模式] {'仅处理 project: ' + args.project if args.project else '处理所有 project'}")
    print(f"[OK] 共找到 {total} 个 input.java")
    print(f"[并发] max_workers = {args.workers}")
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
    print(f"[完成] 成功 {success} / 失败 {fail} / 总计 {total}")


if __name__ == "__main__":
    main()
