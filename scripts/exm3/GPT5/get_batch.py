#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成 OpenAI Batch JSONL（/v1/responses），并按每 15 条拆分到多个文件：
- 统一 instruction: prompt_additional_testcase.txt
- 自动发现 <root>/**/input.java（默认 root=Cli）
- custom_id = <DEF_ROOT>_<子文件夹数字>，如 Cli_1
- 输出切分：requests.jsonl, requests1.jsonl, requests2.jsonl, ...

python get_batch.py --all --root ../../../results/exm3/GPT5

"""
import json
from pathlib import Path
import argparse
from collections import Counter, defaultdict
from typing import List, Dict, Any, Tuple

DEF_ROOT = "Time"
INSTRUCTION_FILE = "prompt_additional_testcase.txt"
MODEL = "gpt-5"
OUTPUT_JSONL = "requests.jsonl"
TEMPERATURE = 0          # 如报"不支持自定义温度"，可删掉该字段
MAX_TOKENS = 40000          # 对应 Responses 的 max_output_tokens
CHUNK_SIZE = 15             # 每个文件 15 条

def read_text(p: Path) -> str:
    if not p.exists():
        raise FileNotFoundError(f"找不到文件: {p}")
    return p.read_text(encoding="utf-8", errors="ignore")

def split_filename(fname: str) -> Tuple[str, str]:
    """
    将 'requests.jsonl' 拆成 ('requests', '.jsonl')
    """
    p = Path(fname)
    ext = "".join(p.suffixes) or ".jsonl"
    base = p.name[:-len(ext)] if ext else p.name
    # 不保留路径，只保留文件名。输出与当前工作目录同级。
    return base, ext

def write_chunk_files(
    records: List[Dict[str, Any]],
    out_name: str,
    chunk_size: int = CHUNK_SIZE,
    out_dir: Path = Path("."),
) -> List[str]:
    """
    将 records 按 chunk_size 拆分写入多个文件到 out_dir：
    """
    base, ext = split_filename(out_name)
    written = []

    if not records:
        return written

    # 确保目录存在
    out_dir.mkdir(parents=True, exist_ok=True)

    for i in range(0, len(records), chunk_size):
        chunk = records[i:i+chunk_size]
        idx = i // chunk_size

        if idx == 0:
            fname = f"{base}{ext}"
        else:
            fname = f"{base}{idx}{ext}"

        fpath = out_dir / fname

        with open(fpath, "w", encoding="utf-8") as f:
            for rec in chunk:
                f.write(json.dumps(rec, ensure_ascii=False) + "\n")

        written.append(str(fpath))

    return written


def process_one_root(
    root: Path,
    sys_inst: str,
    out_name: str,
    model: str,
    max_tokens: int,
    chunk_size: int,
) -> List[str]:
    """处理单个项目目录，返回写出的 JSONL 文件列表。"""
    root_name = root.name

    java_files = sorted(root.rglob("input.java"))
    if not java_files:
        print(f"[跳过] 在 {root} 下没有找到任何 input.java")
        return []

    ids = []
    by_id_paths = defaultdict(list)
    for fp in java_files:
        rel = fp.relative_to(root)
        parts = rel.parts
        subfolder_num = parts[0] if len(parts) >= 2 else fp.parent.name
        ids.append(subfolder_num)
        by_id_paths[subfolder_num].append(fp)

    dup_counts = Counter(ids)
    has_dup = any(c > 1 for c in dup_counts.values())

    all_records = []
    for sub_id, paths in by_id_paths.items():
        for idx, fp in enumerate(sorted(paths)):
            code = read_text(fp)
            rel_str = fp.relative_to(root).as_posix()

            custom_id = f"{root_name}_{sub_id}"
            if has_dup and dup_counts[sub_id] > 1:
                custom_id = f"{custom_id}-{idx+1}"

            body = {
                "model": model,
                "max_output_tokens": max_tokens,
                "reasoning": {"effort": "low"},
                "text": {"verbosity": "low"},
                "input": [
                    {"role": "system", "content": sys_inst},
                    {
                        "role": "user",
                        "content": (
                            "Below is the Java source code for analysis. "
                            "Please follow the system instructions strictly to complete the task.\n"
                            f"[source_file]: {rel_str}\n\n"
                            "===== BEGIN =====\n"
                            f"{code}\n"
                            "===== END =====\n"
                        ),
                    },
                ],
            }
            # body["temperature"] = args.temperature  # 若模型不支持请注释掉

            all_records.append({
                "custom_id": custom_id,
                "method": "POST",
                "url": "/v1/responses",
                "body": body,
            })

    out_dir = Path("z_requests") / root_name
    written_files = write_chunk_files(all_records, out_name, chunk_size, out_dir=out_dir)

    print(f"[{root_name}] {len(all_records)} 个请求 -> {len(written_files)} 个 JSONL")
    if has_dup:
        print(f"  [注意] 同一数字下存在多份 input.java，custom_id 已追加 -序号。")
    for fn in written_files:
        print(f"  {fn}")

    return written_files


def main():
    ap = argparse.ArgumentParser(description="生成 OpenAI Batch JSONL (/v1/responses)，并每 15 条拆分到多个文件")
    ap.add_argument("--root", default=DEF_ROOT,
                    help="单个项目目录（默认 Time）。与 --all 二选一。")
    ap.add_argument("--all", dest="all_projects", action="store_true",
                    help="自动发现 --root 的父目录下所有子目录并逐一处理。")
    ap.add_argument("--instruction", default=INSTRUCTION_FILE,
                    help="系统指令文件（默认 prompt_additional_testcase.txt）")
    ap.add_argument("--out", default=OUTPUT_JSONL,
                    help="基础输出 JSONL 文件名（默认 request.jsonl）")
    ap.add_argument("--model", default=MODEL)
    ap.add_argument("--temperature", type=float, default=TEMPERATURE)
    ap.add_argument("--max-tokens", type=int, default=MAX_TOKENS)
    ap.add_argument("--chunk-size", type=int, default=CHUNK_SIZE,
                    help="每个 JSONL 文件包含的请求数（默认 15）")
    args = ap.parse_args()

    sys_inst = read_text(Path(args.instruction))

    if args.all_projects:
        # 把 --root 当作父目录，遍历其下所有子目录
        parent = Path(args.root).resolve()
        roots = sorted(d for d in parent.iterdir() if d.is_dir())
        if not roots:
            raise SystemExit(f"[提示] 在 {parent} 下没有找到任何子目录")
        all_written: List[str] = []
        for root in roots:
            written = process_one_root(
                root, sys_inst, args.out, args.model, args.max_tokens, args.chunk_size
            )
            all_written.extend(written)
        print(f"\n[汇总] 共生成 {len(all_written)} 个 JSONL 文件。")
        if all_written:
            print(f"提交示例：openai batches create -f {all_written[0]}")
    else:
        root = Path(args.root).resolve()
        written = process_one_root(
            root, sys_inst, args.out, args.model, args.max_tokens, args.chunk_size
        )
        if written:
            print(f"提交示例：openai batches create -f {written[0]}")
        else:
            print("[警告] 没有写出任何 JSONL 文件。")


if __name__ == "__main__":
    main()
