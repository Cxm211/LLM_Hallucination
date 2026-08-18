#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成 Claude (Anthropic) Message Batches 的 batch 请求文件，并按 chunk 拆分：

- 统一 system instruction: prompt_additional_testcase.txt
- 若指定 --root：只处理该 project
- 若不指定 --root：自动发现当前目录下所有包含 **/input.java 的一级子目录作为 project
- custom_id = <PROJECTNAME>_<子文件夹数字>，如 Time_1
- 若同数字下有多份 input.java，则追加 -序号 避免 custom_id 重复
- 输出为多个 JSON 文件（每个文件一个 batch create body）：
  z_requests/<project>/batch.json, batch1.json, batch2.json, ...

提交示例：
curl https://api.anthropic.com/v1/messages/batches \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "content-type: application/json" \
  --data @z_requests/Time/batch.json
"""

import json
from pathlib import Path
import argparse
from collections import Counter, defaultdict
from typing import List, Dict, Any, Tuple


INSTRUCTION_FILE = "prompt_additional_testcase.txt"

# 这里填你实际可用的 Claude 模型名
MODEL = "claude-sonnet-4-5"

# Claude Messages API: max_tokens
MAX_TOKENS = 40000

# 每个 batch 文件里包含的 requests 数量
CHUNK_SIZE = 50


def read_text(p: Path) -> str:
    if not p.exists():
        raise FileNotFoundError(f"找不到文件: {p}")
    return p.read_text(encoding="utf-8", errors="ignore")


def split_filename(fname: str) -> Tuple[str, str]:
    """将 'batch.json' 拆成 ('batch', '.json')"""
    p = Path(fname)
    ext = "".join(p.suffixes) or ".json"
    base = p.name[:-len(ext)] if ext else p.name
    return base, ext


def write_chunk_files_as_json(
    requests_list: List[Dict[str, Any]],
    out_name: str,
    chunk_size: int,
    out_dir: Path,
) -> List[str]:
    """
    将 requests_list 按 chunk_size 拆分写入多个 JSON 文件：
    每个文件结构：{ "requests": [ ... ] }
    """
    base, ext = split_filename(out_name)
    written: List[str] = []
    if not requests_list:
        return written

    out_dir.mkdir(parents=True, exist_ok=True)

    for i in range(0, len(requests_list), chunk_size):
        chunk = requests_list[i : i + chunk_size]
        idx = i // chunk_size
        fname = f"{base}{ext}" if idx == 0 else f"{base}{idx}{ext}"
        fpath = out_dir / fname

        payload = {"requests": chunk}
        fpath.write_text(
            json.dumps(payload, ensure_ascii=False, indent=2),
            encoding="utf-8",
        )
        written.append(str(fpath))

    return written


def discover_projects(workspace: Path) -> List[Path]:
    """
    自动发现 project：
    - 当前目录下的一级子目录
    - 目录内存在至少一个 **/input.java
    """
    projects: List[Path] = []
    for child in sorted([p for p in workspace.iterdir() if p.is_dir()]):
        if any(child.rglob("input.java")):
            projects.append(child)
    return projects


def build_requests_for_project(
    root: Path,
    sys_inst: str,
    model: str,
    max_tokens: int,
) -> Tuple[List[Dict[str, Any]], int, bool]:
    """
    为单个 project(root) 构建 Claude batch requests：
    returns: (requests_list, total_inputs, has_dup)
    """
    java_files = sorted(root.rglob("input.java"))
    if not java_files:
        return [], 0, False

    root_name = root.name  # custom_id 前缀

    ids: List[str] = []
    by_id_paths: Dict[str, List[Path]] = defaultdict(list)

    for fp in java_files:
        rel = fp.relative_to(root)
        parts = rel.parts
        subfolder_num = parts[0] if len(parts) >= 2 else fp.parent.name
        ids.append(subfolder_num)
        by_id_paths[subfolder_num].append(fp)

    dup_counts = Counter(ids)
    has_dup = any(c > 1 for c in dup_counts.values())

    all_requests: List[Dict[str, Any]] = []
    total_inputs = 0

    for sub_id, paths in sorted(by_id_paths.items(), key=lambda x: x[0]):
        for idx, fp in enumerate(sorted(paths)):
            code = read_text(fp)
            rel_str = fp.relative_to(root).as_posix()

            custom_id = f"{root_name}_{sub_id}"
            if has_dup and dup_counts[sub_id] > 1:
                custom_id = f"{custom_id}-{idx+1}"

            params: Dict[str, Any] = {
                "model": model,
                "max_tokens": max_tokens,
                "system": sys_inst,
                "messages": [
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
                    }
                ],
            }

            all_requests.append({"custom_id": custom_id, "params": params})
            total_inputs += 1

    return all_requests, total_inputs, has_dup


def main() -> None:
    ap = argparse.ArgumentParser(
        description="生成 Claude Message Batches 请求文件：指定 root 或自动发现全部 projects"
    )
    ap.add_argument(
        "--root",
        default=None,
        help="单个 project 根目录；不传则自动发现当前目录下所有 projects",
    )
    ap.add_argument(
        "--instruction",
        default=INSTRUCTION_FILE,
        help=f"system 指令文件（默认 {INSTRUCTION_FILE}）",
    )
    ap.add_argument(
        "--out",
        default="batch.json",
        help="基础输出文件名（默认 batch.json），会自动追加 1,2,...",
    )
    ap.add_argument("--model", default=MODEL, help="Claude 模型名")
    ap.add_argument("--max-tokens", type=int, default=MAX_TOKENS, help="Messages API max_tokens")
    ap.add_argument(
        "--chunk-size",
        type=int,
        default=CHUNK_SIZE,
        help=f"每个 batch 文件包含的 requests 数量（默认 {CHUNK_SIZE}）",
    )
    ap.add_argument(
        "--anthropic-version",
        default="2023-06-01",
        help="仅用于打印提交示例 header（默认 2023-06-01）",
    )
    args = ap.parse_args()

    workspace = Path(".").resolve()
    sys_inst = read_text(Path(args.instruction))

    if args.root:
        roots = [Path(args.root).resolve()]
        if not roots[0].exists() or not roots[0].is_dir():
            raise SystemExit(f"[错误] --root 不是有效目录: {roots[0]}")
    else:
        roots = discover_projects(workspace)
        if not roots:
            raise SystemExit("[提示] 当前目录下未发现任何包含 **/input.java 的 project（一级子目录）")

    grand_total = 0
    for root in roots:
        requests_list, total_inputs, has_dup = build_requests_for_project(
            root=root,
            sys_inst=sys_inst,
            model=args.model,
            max_tokens=args.max_tokens,
        )

        if total_inputs == 0:
            print(f"[跳过] {root.name}: 未找到 input.java")
            continue

        out_dir = Path("z_requests") / root.name
        written_files = write_chunk_files_as_json(
            requests_list,
            args.out,
            chunk_size=args.chunk_size,
            out_dir=out_dir,
        )

        grand_total += total_inputs 

        print(f"\n[OK] Project = {root.name}")
        print(f"     发现 {total_inputs} 个 input.java")
        if has_dup:
            print("     [注意] 同一数字下存在多份 input.java，custom_id 已追加 -序号 保证唯一。")
        print(f"     输出目录: {out_dir}")
        print(f"     生成 {len(written_files)} 个 batch JSON（每 {args.chunk_size} 条）:")
        for i, fn in enumerate(written_files):
            print(f"       [{i}] {fn}")

        print("     提交示例（第一个文件）：")
        print("     curl https://api.anthropic.com/v1/messages/batches \\")
        print('       --header "x-api-key: $ANTHROPIC_API_KEY" \\')
        print(f'       --header "anthropic-version: {args.anthropic_version}" \\')
        print('       --header "content-type: application/json" \\')
        print(f"       --data @{written_files[0]}")

    print(f"\n[总计] 共处理 {len(roots)} 个 project（含跳过），共发现 {grand_total} 个 input.java")


if __name__ == "__main__":
    main()
