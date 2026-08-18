#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
DeepSeek exm3 批处理脚本

功能：
- 读取 prompt_additional_testcase.txt 作为 system prompt
- 读取每个 <project>/<id>/input.java 作为 user content
- 调用 DeepSeek API (streaming)，保存原始响应到 output.json
- 已存在 output.json 自动跳过（断点续跑）
- 支持 --project / --ids 过滤
- 并行请求
"""

import os
import re
import time
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
PROMPT_FILE = "prompt_additional_testcase.txt"

MAX_WORKERS = 5
print_lock = Lock()


# ======================
# 工具函数
# ======================
def read_file_content(file_path: Path) -> str:
    if not file_path.exists():
        raise FileNotFoundError(f"找不到文件: {file_path}")
    return file_path.read_text(encoding="utf-8", errors="ignore")


def parse_id_expr(expr: str) -> set:
    ids = set()
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


def find_all_input_java(root_dir: Path, project: Optional[str], id_filter: Optional[set]):
    """返回 [(java_path, rel_path_from_root), ...]"""
    results = []
    search_root = root_dir / project if project else root_dir
    for java_file in sorted(search_root.rglob("input.java")):
        parts = java_file.relative_to(root_dir).parts
        # 期望结构: <project>/<id>/input.java
        if len(parts) < 3:
            continue
        bug_id = parts[1]
        if id_filter and bug_id not in id_filter:
            continue
        rel_path = str(java_file.relative_to(root_dir))
        results.append((java_file, rel_path))
    return results


def is_already_done(root_dir: Path, rel_path: str) -> bool:
    output_path = root_dir / Path(rel_path).parent / "output.json"
    return output_path.exists() and output_path.stat().st_size > 0


# ======================
# DeepSeek API 调用
# ======================
def call_deepseek_api(system_prompt: str, user_content: str) -> str:
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

    with requests.post(BASE_URL, headers=headers, json=payload, stream=True, timeout=600) as resp:
        if resp.status_code != 200:
            try:
                err_text = resp.text
            except Exception:
                err_text = "<no response body>"
            raise RuntimeError(f"HTTP {resp.status_code}\n{err_text}")

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


def save_response(root_dir: Path, rel_path: str, response: str):
    output_path = root_dir / Path(rel_path).parent / "output.json"
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(response, encoding="utf-8")


# ======================
# 单任务
# ======================
def process_one(root_dir: Path, system_prompt: str, java_path: Path, rel_path: str) -> bool:
    try:
        if is_already_done(root_dir, rel_path):
            with print_lock:
                print(f"[跳过] {rel_path}")
            return True

        with print_lock:
            print(f"[START] {rel_path}")

        java_code = read_file_content(java_path)
        user_content = (
            "Below is the buggy Java function(s) and trigger testcase(s) for analysis.\n"
            f"[source_file]: {rel_path}\n\n"
            "===== BEGIN =====\n"
            f"{java_code}\n"
            "===== END =====\n"
        )

        response = call_deepseek_api(system_prompt, user_content)
        save_response(root_dir, rel_path, response)

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
    parser = argparse.ArgumentParser(description="DeepSeek exm3 批处理：生成 fix + additional testcases")
    parser.add_argument("--project", type=str, default=None,
                        help="只处理指定 project，例如 Chart")
    parser.add_argument("--ids", type=str, default=None,
                        help="只处理指定 bug id，例如 '1,3,5-7'")
    parser.add_argument("--workers", type=int, default=MAX_WORKERS,
                        help=f"并发数（默认 {MAX_WORKERS}）")
    args = parser.parse_args()

    root_dir = Path(__file__).parent.resolve()
    system_prompt = read_file_content(root_dir / PROMPT_FILE)

    id_filter = parse_id_expr(args.ids) if args.ids else None
    java_files = find_all_input_java(root_dir, args.project, id_filter)

    total = len(java_files)
    print(f"[模式] project={args.project or '全部'}  ids={args.ids or '全部'}")
    print(f"[OK] 共找到 {total} 个 input.java")
    print(f"[并发] max_workers = {args.workers}")
    print("-" * 60)

    success = fail = 0
    with ThreadPoolExecutor(max_workers=args.workers) as executor:
        futures = {
            executor.submit(process_one, root_dir, system_prompt, jp, rp): rp
            for jp, rp in java_files
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
