#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成 Claude (Anthropic) Message Batches 的 batch 请求文件，并按 chunk 拆分：

- 统一 system instruction: prompt_select_testcases.txt
- 若指定 --root：只处理该 project
- 若不指定 --root：自动发现当前目录下所有包含 **/input.java 的一级子目录作为 project
- custom_id = <PROJECTNAME>_<子文件夹名>，如 Time_1、Time_1_1（含分片后缀）
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
from collections import defaultdict
from typing import List, Dict, Any, Tuple, Optional


INSTRUCTION_FILE = "prompt_select_testcases.txt"

MODEL = "claude-sonnet-4-5"

MAX_TOKENS = 40000

CHUNK_SIZE = 50


def read_text(p: Path) -> str:
    if not p.exists():
        raise FileNotFoundError(f"找不到文件: {p}")
    return p.read_text(encoding="utf-8", errors="ignore")


def split_filename(fname: str) -> Tuple[str, str]:
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
    projects: List[Path] = []
    for child in sorted([p for p in workspace.iterdir() if p.is_dir()]):
        if child.name.startswith("z_"):
            continue
        if any(child.rglob("input.java")):
            projects.append(child)
    return projects


def build_requests_for_project(
    root: Path,
    sys_inst: str,
    model: str,
    max_tokens: int,
) -> Tuple[List[Dict[str, Any]], int]:
    """
    为单个 project(root) 构建 Claude batch requests。
    custom_id = <ProjectName>_<subfolder>，subfolder 保留分片后缀（如 1_1, 1_2）。
    """
    java_files = sorted(root.rglob("input.java"))
    if not java_files:
        return [], 0

    root_name = root.name
    all_requests: List[Dict[str, Any]] = []

    # 按子文件夹名分组（保留分片后缀）
    by_subfolder: Dict[str, List[Path]] = defaultdict(list)
    for fp in java_files:
        rel = fp.relative_to(root)
        subfolder = rel.parts[0] if len(rel.parts) >= 2 else fp.parent.name
        by_subfolder[subfolder].append(fp)

    for subfolder, paths in sorted(by_subfolder.items(),
                                   key=lambda x: [int(p) if p.isdigit() else p
                                                  for p in x[0].replace("_", " ").split()]):
        for idx, fp in enumerate(sorted(paths)):
            code = read_text(fp)
            rel_str = fp.relative_to(root).as_posix()

            # 同一 subfolder 多份时加 -序号（通常不会出现，保险起见）
            custom_id = f"{root_name}_{subfolder}"
            if len(paths) > 1:
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

    return all_requests, len(all_requests)


def main() -> None:
    ap = argparse.ArgumentParser(
        description="生成 Claude Message Batches 请求文件（exm1）"
    )
    ap.add_argument("--root", default=None,
                    help="单个 project 根目录；不传则自动发现当前目录下所有 projects")
    ap.add_argument("--instruction", default=INSTRUCTION_FILE,
                    help=f"system 指令文件（默认 {INSTRUCTION_FILE}）")
    ap.add_argument("--out", default="batch.json",
                    help="基础输出文件名（默认 batch.json）")
    ap.add_argument("--model", default=MODEL, help="Claude 模型名")
    ap.add_argument("--max-tokens", type=int, default=MAX_TOKENS,
                    help="Messages API max_tokens")
    ap.add_argument("--chunk-size", type=int, default=CHUNK_SIZE,
                    help=f"每个 batch 文件包含的 requests 数量（默认 {CHUNK_SIZE}）")
    ap.add_argument("--anthropic-version", default="2023-06-01",
                    help="anthropic-version header（默认 2023-06-01）")
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
            raise SystemExit("[提示] 当前目录下未发现任何包含 **/input.java 的 project")

    grand_total = 0
    for root in roots:
        requests_list, total_inputs = build_requests_for_project(
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

    print(f"\n[总计] 共处理 {len(roots)} 个 project，共 {grand_total} 个 input.java")


if __name__ == "__main__":
    main()
