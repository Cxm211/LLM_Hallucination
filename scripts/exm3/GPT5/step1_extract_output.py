#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
1) 读取 z_output/<project>/request*_output.jsonl（Batch wrapper 每行一个 JSON）
2) 按 custom_id = <project>_<id> 把模型输出 JSON 写入 <dest_root>/<project>/<id>/output.json
   - 每次运行覆盖 output.json
3) 生成派生文件：
   - expn.json：prediction 字段（buggy/fixed 行号预测）
   - patch.java, patch1.java, patch2.java...：来自 fixed_code 数组
   - reason.txt：reason 字段（仅当该字段存在且非空时生成）

用法：
  python step1_extract_output.py --outputs-root z_output --dest-root .
"""

import argparse
import json
from pathlib import Path
from typing import Optional, Tuple, Dict, Any


def find_assistant_output_text(wrapper: dict) -> Optional[str]:
    """从 Batch wrapper JSON 里取 assistant output_text.text（字符串）"""
    try:
        out = wrapper["response"]["body"]["output"]
        if not isinstance(out, list):
            return None
        for item in out:
            if not isinstance(item, dict):
                continue
            if item.get("type") != "message":
                continue
            content = item.get("content", [])
            if not isinstance(content, list):
                continue
            for c in content:
                if isinstance(c, dict) and c.get("type") == "output_text":
                    return c.get("text")
        return None
    except Exception:
        return None


def parse_custom_id(custom_id: str) -> Optional[Tuple[str, str]]:
    """
    解析 custom_id: <project>_<id> 或 <project>_<id>-k
    返回 (project, id)
    """
    if not custom_id or "_" not in custom_id:
        return None
    project, rest = custom_id.split("_", 1)
    project = project.strip()
    rest = rest.strip()
    if not project:
        return None
    sub_id = rest.split("-", 1)[0].strip()
    if not sub_id:
        return None
    return project, sub_id


def iter_output_files(outputs_root: Path):
    """遍历 z_output/<project>/requests*_output.jsonl"""
    for proj_dir in sorted(outputs_root.iterdir()):
        if not proj_dir.is_dir():
            continue
        for p in sorted(proj_dir.glob("requests*_output.jsonl")):
            yield proj_dir.name, p


def write_additional_testcases(dest_dir: Path, testcases: Any):
    """
    将 additional_testcases 数组写入 add_test.java, add_test1.java, add_test2.java...
    每个文件第一行是 // <path>，之后是 testcase 代码。
    运行前先删除已有 add_test*.java（避免残留）。
    """
    for old in dest_dir.glob("add_test*.java"):
        try:
            old.unlink()
        except Exception:
            pass

    if not isinstance(testcases, list):
        return

    for idx, entry in enumerate(testcases):
        if not isinstance(entry, dict):
            continue
        path = entry.get("path", "")
        code = entry.get("testcase", "")
        if not isinstance(code, str):
            code = "" if code is None else str(code)
        content = f"// {path}\n{code}"
        fname = "add_test.java" if idx == 0 else f"add_test{idx}.java"
        (dest_dir / fname).write_text(content, encoding="utf-8")


def write_patches(dest_dir: Path, fixed_codes: Any):
    """
    将 fixed_code 数组写入 patch.java, patch1.java, patch2.java...
    运行前会先删除已有 patch*.java（避免残留）
    """
    # 清理旧 patch 文件
    for old in dest_dir.glob("patch*.java"):
        try:
            old.unlink()
        except Exception:
            pass

    if not isinstance(fixed_codes, list):
        return

    for idx, code in enumerate(fixed_codes):
        if not isinstance(code, str):
            code = "" if code is None else str(code)
        fname = "patch.java" if idx == 0 else f"patch{idx}.java"
        (dest_dir / fname).write_text(code, encoding="utf-8")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--outputs-root", default="z_output", help="包含各 project 输出的目录（默认 z_output）")
    ap.add_argument("--dest-root", default=".", help="写入 <project>/<id>/output.json 的根目录（默认当前目录）")
    ap.add_argument(
        "--quiet",
        action="store_true",
        help="只输出失败信息（可选）"
    )
    args = ap.parse_args()

    outputs_root = Path(args.outputs_root).expanduser().resolve()
    dest_root = Path(args.dest_root).expanduser().resolve()

    if not outputs_root.is_dir():
        raise SystemExit(f"[ERROR] outputs_root 不存在或不是目录：{outputs_root}")

    total = 0
    ok = 0
    skipped = 0

    for _, out_file in iter_output_files(outputs_root):
        with out_file.open("r", encoding="utf-8", errors="ignore") as f:
            for ln, line in enumerate(f, start=1):
                line = line.strip()
                if not line:
                    continue
                total += 1

                # 解析 wrapper
                try:
                    wrapper = json.loads(line)
                except Exception as e:
                    skipped += 1
                    if not args.quiet:
                        print(f"[SKIP] {out_file.name}:{ln} wrapper JSON 解析失败: {e}")
                    continue

                custom_id = wrapper.get("custom_id", "")
                parsed = parse_custom_id(custom_id)
                if not parsed:
                    skipped += 1
                    print(f"[SKIP] {out_file.name}:{ln} custom_id 解析失败: {custom_id!r}")
                    continue

                project, sub_id = parsed

                # 处理 incomplete（没有 message/output_text 就没法产出 output.json）
                body = (wrapper.get("response") or {}).get("body") or {}
                status = body.get("status")
                if status != "completed":
                    # 你之前见过：incomplete + max_output_tokens
                    reason = ((body.get("incomplete_details") or {}).get("reason")) if isinstance(body.get("incomplete_details"), dict) else None
                    skipped += 1
                    print(f"[SKIP] {out_file.name}:{ln} {custom_id}: response.status={status} reason={reason}")
                    continue

                # 取模型输出文本
                text = find_assistant_output_text(wrapper)
                if not text or not str(text).strip():
                    skipped += 1
                    print(f"[SKIP] {out_file.name}:{ln} {custom_id}: 未找到 assistant output_text")
                    continue

                # 解析模型输出为 JSON
                try:
                    model_json = json.loads(text)
                    if not isinstance(model_json, dict):
                        # 允许输出是 list，但你的后续逻辑需要 dict（function_* / fixed_code）
                        # 这里直接跳过更安全
                        skipped += 1
                        print(f"[SKIP] {out_file.name}:{ln} {custom_id}: 模型输出 JSON 非对象(dict)，实际类型={type(model_json).__name__}")
                        continue
                except Exception as e:
                    skipped += 1
                    print(f"[SKIP] {out_file.name}:{ln} {custom_id}: 模型输出 JSON 解析失败：{e}")
                    continue

                # 写入 <dest_root>/<project>/<id>/output.json （覆盖）
                dest_dir = dest_root / project / sub_id
                dest_dir.mkdir(parents=True, exist_ok=True)

                output_path = dest_dir / "output.json"
                output_path.write_text(
                    json.dumps(model_json, ensure_ascii=False, indent=2),
                    encoding="utf-8"
                )

                # 生成 patch*.java（fixed_code）
                fixed_codes = model_json.get("fixed_code", [])
                write_patches(dest_dir, fixed_codes)

                # 生成 add_test*.java（additional_testcases）
                testcases = model_json.get("additional_testcases", [])
                write_additional_testcases(dest_dir, testcases)

                # 清理旧的 expn.json / reason.txt（新格式不再有这两个字段）
                for stale in ("expn.json", "reason.txt"):
                    p = dest_dir / stale
                    if p.exists():
                        p.unlink()

                ok += 1

    if not args.quiet:
        print("\n[DONE]")
        print(f"  total   = {total}")
        print(f"  ok      = {ok}")
        print(f"  skipped = {skipped}")


if __name__ == "__main__":
    main()
