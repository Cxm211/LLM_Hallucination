#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
DeepSeek 批处理最终版脚本（可诊断稳定版）

功能：
- requests + streaming（正确处理 keep-alive）
- 并行 3 个请求
- 已存在 output.json 自动跳过（断点续跑）
- 支持 --project 指定单个 project
- 清晰日志（START / 成功 / 失败 / 跳过）
- ★ 400 Bad Request 时打印 DeepSeek 返回的真实原因
"""

import os
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
PROMPT_FILE = "prompt_line_coverage.txt"

MAX_WORKERS = 5
print_lock = Lock()

# ======================
# 工具函数
# ======================
def read_file_content(file_path: Path) -> str:
    if not file_path.exists():
        raise FileNotFoundError(f"找不到文件: {file_path}")
    return file_path.read_text(encoding="utf-8", errors="ignore")


def find_all_input_java(root_dir: Path) -> list[tuple[Path, str]]:
    """
    返回 [(java_path, rel_path_from_root), ...]
    """
    results = []
    for java_file in root_dir.rglob("input.java"):
        rel_path = java_file.relative_to(root_dir)
        results.append((java_file, str(rel_path)))
    return sorted(results)


def is_already_done(root_dir: Path, rel_path: str) -> bool:
    """
    Chart/10/input.java -> Chart/10/output.json
    """
    java_path = Path(rel_path)
    output_path = root_dir / java_path.parent / "output.json"
    return output_path.exists() and output_path.stat().st_size > 0


# ======================
# DeepSeek 调用（★可诊断版）
# ======================
def call_deepseek_api(
    system_prompt: str,
    user_content: str,
) -> Optional[str]:

    headers = {
        "Authorization": f"Bearer {DEEPSEEK_API_KEY}",
        "Content-Type": "application/json",
    }

    payload = {
        "model": MODEL,
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_content},
        ],
        "temperature": TEMPERATURE,
        "max_tokens": MAX_TOKENS,
        "stream": True,
    }

    with requests.post(
        BASE_URL,
        headers=headers,
        json=payload,
        stream=True,
        timeout=600,  # 允许长时间排队
    ) as resp:

        # ★ 关键：不要 raise_for_status，否则 body 会被吞
        if resp.status_code != 200:
            try:
                err_text = resp.text
            except Exception:
                err_text = "<no response body>"

            raise RuntimeError(
                f"HTTP {resp.status_code} Bad Request\n"
                f"Response body:\n{err_text}"
            )

        chunks = []

        for line in resp.iter_lines(decode_unicode=True):
            if not line:
                continue  # keep-alive

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

            text = None
            if "content" in delta:
                text = delta.get("content")
            elif "reasoning_content" in delta:
                text = delta.get("reasoning_content")

            if isinstance(text, str) and text:
                chunks.append(text)

        if not chunks:
            raise RuntimeError("服务器结束连接但未产生任何输出")

        return "".join(chunks)


def save_response(root_dir: Path, rel_path: str, response: str):
    java_path = Path(rel_path)
    output_path = root_dir / java_path.parent / "output.json"
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(response, encoding="utf-8")


# ======================
# 单任务
# ======================
def process_one(
    root_dir: Path,
    system_prompt: str,
    java_path: Path,
    rel_path: str,
) -> bool:
    try:
        if is_already_done(root_dir, rel_path):
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
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--project",
        type=str,
        help="只处理指定 project，例如 Chart / Closure / Lang",
    )
    args = parser.parse_args()

    if not DEEPSEEK_API_KEY:
        raise SystemExit("请设置 DEEPSEEK_API_KEY")

    root_dir = Path(__file__).parent.resolve()
    system_prompt = read_file_content(root_dir / PROMPT_FILE)

    # ===== 扫描 input.java =====
    if args.project:
        project_dir = root_dir / args.project
        if not project_dir.exists():
            raise SystemExit(f"[错误] project 不存在: {args.project}")

        java_files = []
        for java_file in project_dir.rglob("input.java"):
            rel_path = java_file.relative_to(root_dir)
            java_files.append((java_file, str(rel_path)))

        print(f"[模式] 仅处理 project: {args.project}")
    else:
        java_files = find_all_input_java(root_dir)
        print("[模式] 处理所有 project")

    total = len(java_files)

    print(f"[OK] 共找到 {total} 个 input.java")
    print(f"[并发] max_workers = {MAX_WORKERS}")
    print("-" * 60)

    success = 0
    fail = 0

    with ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
        futures = {
            executor.submit(
                process_one,
                root_dir,
                system_prompt,
                java_path,
                rel_path,
            ): rel_path
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
