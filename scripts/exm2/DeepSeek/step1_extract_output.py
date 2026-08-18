#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
从已有的 <root>/<project>/<id>/output.json 生成派生文件：

- expn.json：收集 function_1/function_2/... 的内容（按 key 排序，输出为 JSON 对象）
- patch.java, patch1.java, patch2.java...：来自 output.json 里的 fixed_code 数组

用法：
  python step1_extract_output.py --root .
  python step1_extract_output.py --root . --project Chart
  python step1_extract_output.py --root . --quiet
"""

import argparse
import json
from pathlib import Path
from typing import Any, Dict, Optional


def extract_functions(model_json: Dict[str, Any]) -> Dict[str, Any]:
    """提取 function_1, function_2, ... 字段并排序。"""
    funcs = {}
    for k, v in model_json.items():
        if isinstance(k, str) and k.startswith("function_"):
            funcs[k] = v
    return dict(sorted(funcs.items(), key=lambda kv: kv[0]))


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

    if fixed_codes is None:
        return

    # 兼容 fixed_code 直接是字符串的情况
    if isinstance(fixed_codes, str):
        (dest_dir / "patch.java").write_text(fixed_codes, encoding="utf-8")
        return

    if not isinstance(fixed_codes, list):
        return

    for idx, code in enumerate(fixed_codes):
        if code is None:
            code = ""
        if not isinstance(code, str):
            code = str(code)

        fname = "patch.java" if idx == 0 else f"patch{idx}.java"
        (dest_dir / fname).write_text(code, encoding="utf-8")


def process_one_output_json(output_path: Path, quiet: bool = False) -> bool:
    """给定 <project>/<id>/output.json，生成 expn.json 和 patch*.java"""
    try:
        text = output_path.read_text(encoding="utf-8", errors="ignore").strip()
        if not text:
            if not quiet:
                print(f"[SKIP] {output_path}: empty file")
            return False

        try:
            model_json = json.loads(text)
        except Exception as e:
            if not quiet:
                print(f"[SKIP] {output_path}: JSON parse error: {e}")
            return False

        if not isinstance(model_json, dict):
            if not quiet:
                print(f"[SKIP] {output_path}: JSON is not an object(dict), got {type(model_json).__name__}")
            return False

        dest_dir = output_path.parent

        # expn.json
        expn = extract_functions(model_json)
        (dest_dir / "expn.json").write_text(
            json.dumps(expn, ensure_ascii=False, indent=2),
            encoding="utf-8",
        )

        # patch*.java from fixed_code
        fixed_codes = model_json.get("fixed_code", None)
        write_patches(dest_dir, fixed_codes)

        return True

    except Exception as e:
        if not quiet:
            print(f"[FAIL] {output_path}: {e}")
        return False


def iter_output_json_files(root: Path, project: Optional[str] = None):
    """
    遍历 root 下的 output.json：
    - 若指定 --project，则只搜 root/<project>/**/output.json
    - 否则搜 root/**/output.json
    """
    if project:
        base = root / project
        if base.is_dir():
            yield from base.rglob("output.json")
        return
    yield from root.rglob("output.json")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=".", help="包含 <project>/<id>/output.json 的根目录（默认当前目录）")
    ap.add_argument("--project", default=None, help="只处理指定 project（例如 Chart / Closure / Lang）")
    ap.add_argument("--quiet", action="store_true", help="只输出失败/跳过信息")
    args = ap.parse_args()

    root = Path(args.root).expanduser().resolve()
    if not root.is_dir():
        raise SystemExit(f"[ERROR] root 不存在或不是目录：{root}")

    total = 0
    ok = 0
    skipped = 0

    for out_json in iter_output_json_files(root, args.project):
        total += 1
        if process_one_output_json(out_json, quiet=args.quiet):
            ok += 1
        else:
            skipped += 1

    print("\n[DONE]")
    print(f" root    = {root}")
    if args.project:
        print(f" project = {args.project}")
    print(f" total   = {total}")
    print(f" ok      = {ok}")
    print(f" skipped = {skipped}")


if __name__ == "__main__":
    main()
