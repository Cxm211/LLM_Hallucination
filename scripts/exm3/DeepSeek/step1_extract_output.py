#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
从 <project>/<id>/output.json 提取派生文件：

  patch.java, patch1.java, ...   <- fixed_code 数组
  add_test.java, add_test1.java, ... <- additional_testcases 数组

add_test*.java 格式（与 Claude/GPT5 保持一致）：
  第1行: // <path>
  第2行起: testcase 代码

用法：
  python step1_extract_output.py
  python step1_extract_output.py --project Chart
  python step1_extract_output.py --project Chart --ids 1,3,5-7
  python step1_extract_output.py --quiet
"""

import argparse
import json
import re
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple


# -------------------- 鲁棒 JSON 解析（来自 Claude 版本）--------------------

FENCE_START_RE = re.compile(r"^\s*```(?:json)?\s*$", re.IGNORECASE)
FENCE_END_RE = re.compile(r"^\s*```\s*$")
INVISIBLE_PREFIX = "\ufeff\u200b\u200c\u200d"


def strip_outer_code_fence(text: str) -> str:
    text = text.lstrip(INVISIBLE_PREFIX)
    lines = text.splitlines()
    n = len(lines)
    start = None
    for i in range(n):
        if FENCE_START_RE.match(lines[i].lstrip(INVISIBLE_PREFIX)):
            start = i
            break
    if start is None:
        return text.strip()
    end = None
    for j in range(start + 1, n):
        if FENCE_END_RE.match(lines[j]):
            end = j
            break
    if end is None:
        return "\n".join(lines[start + 1:]).strip()
    return "\n".join(lines[start + 1:end]).strip()


def extract_first_complete_json(text: str) -> Optional[str]:
    s = text.strip()
    if not s:
        return None
    start = opening = None
    for i, ch in enumerate(s):
        if ch == "{":
            start, opening = i, "{"
            break
        if ch == "[":
            start, opening = i, "["
            break
    if start is None:
        return None
    closing = "}" if opening == "{" else "]"
    depth = 0
    in_str = False
    esc = False
    for j in range(start, len(s)):
        ch = s[j]
        if in_str:
            if esc:
                esc = False
            elif ch == "\\":
                esc = True
            elif ch == '"':
                in_str = False
            continue
        if ch == '"':
            in_str = True
            continue
        if ch == opening:
            depth += 1
        elif ch == closing:
            depth -= 1
            if depth == 0:
                return s[start:j + 1].strip()
    return None


def escape_control_chars_for_json(s: str) -> str:
    out = []
    for ch in s:
        o = ord(ch)
        if ch == "\n":
            out.append("\n")
        elif ch == "\r":
            out.append("\\r")
        elif ch == "\t":
            out.append("\\t")
        elif o < 0x20:
            out.append("\\u%04x" % o)
        else:
            out.append(ch)
    return "".join(out)


def extract_json_from_pos(text: str, start: int) -> Optional[str]:
    """从 text[start] 开始（必须是 '{' 或 '['），提取第一个完整 JSON 块。"""
    opening = text[start]
    closing = "}" if opening == "{" else "]"
    depth = 0
    in_str = False
    esc = False
    for j in range(start, len(text)):
        ch = text[j]
        if in_str:
            if esc:
                esc = False
            elif ch == "\\":
                esc = True
            elif ch == '"':
                in_str = False
            continue
        if ch == '"':
            in_str = True
            continue
        if ch == opening:
            depth += 1
        elif ch == closing:
            depth -= 1
            if depth == 0:
                return text[start:j + 1]
    return None


def try_parse_dict(s: str) -> Optional[Dict[str, Any]]:
    try:
        obj = json.loads(s)
        if isinstance(obj, dict):
            return obj
    except Exception as e:
        if "Invalid control character" in str(e):
            try:
                obj = json.loads(escape_control_chars_for_json(s))
                if isinstance(obj, dict):
                    return obj
            except Exception:
                pass
    return None


def parse_model_output_json(text: str) -> Tuple[Optional[Dict[str, Any]], Optional[str]]:
    if not text or not text.strip():
        return None, "empty_text"

    # 1. 先试整体（去掉 code fence）
    t = strip_outer_code_fence(text).lstrip(INVISIBLE_PREFIX).strip()
    obj = try_parse_dict(t)
    if obj is not None:
        return obj, None

    # 2. DeepSeek 把 JSON 放在末尾 —— 先找最后一个 "fixed_code" 或 "additional_testcases"
    #    的位置，然后从该位置往前找最近的 '{' 来提取完整的外层 JSON 对象
    anchor = -1
    for keyword in ['"fixed_code"', '"additional_testcases"']:
        pos = text.rfind(keyword)
        if pos > anchor:
            anchor = pos

    if anchor >= 0:
        # 从 anchor 往前找最近的 '{'
        for i in range(anchor - 1, -1, -1):
            if text[i] == "{":
                cand = extract_json_from_pos(text, i)
                if cand:
                    obj = try_parse_dict(cand)
                    if obj is not None:
                        return obj, None
                break  # 找到 '{' 但解析失败，不再继续

    # 3. 兜底：从最后一个 '{' 往前逐一尝试
    for i in range(len(text) - 1, -1, -1):
        if text[i] == "{":
            cand = extract_json_from_pos(text, i)
            if cand:
                obj = try_parse_dict(cand)
                if obj is not None and ("fixed_code" in obj or "additional_testcases" in obj):
                    return obj, None

    return None, "no_valid_json_dict_found"


# -------------------- patch 提取（同 exm2）--------------------

def write_patches(dest_dir: Path, fixed_codes: Any):
    """fixed_code 数组 -> patch.java, patch1.java, ..."""
    for old in dest_dir.glob("patch*.java"):
        try:
            old.unlink()
        except Exception:
            pass

    if fixed_codes is None:
        return
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


# -------------------- add_test 提取 --------------------

def normalize_path(raw_path: str) -> str:
    """
    把路径规范化成 org/foo/Bar.java 形式：
    - 去掉 ::method 后缀
    - 去掉开头的 / 或 ./
    """
    p = raw_path.strip()
    if "::" in p:
        p = p.split("::")[0].strip()
    p = p.lstrip("./")
    return p


def write_add_tests(dest_dir: Path, additional_testcases: Any):
    """
    additional_testcases 数组 -> add_test.java, add_test1.java, ...

    每个文件格式：
      // <normalized_path>
      <testcase code>
    """
    # 清理旧 add_test 文件
    for old in dest_dir.glob("add_test*.java"):
        try:
            old.unlink()
        except Exception:
            pass

    if not isinstance(additional_testcases, list) or not additional_testcases:
        return

    for idx, item in enumerate(additional_testcases):
        if not isinstance(item, dict):
            continue
        path = normalize_path(str(item.get("path", "") or ""))
        testcase = str(item.get("testcase", "") or "").strip()
        if not path or not testcase:
            continue

        content = f"// {path}\n{testcase}\n"
        fname = "add_test.java" if idx == 0 else f"add_test{idx}.java"
        (dest_dir / fname).write_text(content, encoding="utf-8")


# -------------------- 主处理逻辑 --------------------

def process_one(output_path: Path, quiet: bool = False) -> bool:
    try:
        text = output_path.read_text(encoding="utf-8", errors="ignore").strip()
        if not text:
            if not quiet:
                print(f"[SKIP] {output_path}: empty file")
            return False

        model_json, perr = parse_model_output_json(text)
        if model_json is None:
            if not quiet:
                print(f"[SKIP] {output_path}: cannot parse JSON ({perr})")
            return False

        if not isinstance(model_json, dict):
            if not quiet:
                print(f"[SKIP] {output_path}: JSON root is not an object")
            return False

        dest_dir = output_path.parent

        # patch*.java
        write_patches(dest_dir, model_json.get("fixed_code"))

        # add_test*.java
        write_add_tests(dest_dir, model_json.get("additional_testcases"))

        if not quiet:
            n_patches = len(list(dest_dir.glob("patch*.java")))
            n_tests   = len(list(dest_dir.glob("add_test*.java")))
            print(f"[OK] {output_path.parent.relative_to(output_path.parent.parent.parent)} "
                  f"-> {n_patches} patch(es), {n_tests} add_test(s)")
        return True

    except Exception as e:
        if not quiet:
            print(f"[FAIL] {output_path}: {e}")
        return False


# -------------------- id filter --------------------

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


def iter_output_json(root: Path, project: Optional[str], id_filter: Optional[set]):
    base = root / project if project else root
    for out in sorted(base.rglob("output.json")):
        parts = out.relative_to(root).parts
        if len(parts) < 3:
            continue
        bug_id = parts[1]
        if id_filter and bug_id not in id_filter:
            continue
        yield out


# -------------------- main --------------------

def main():
    ap = argparse.ArgumentParser(
        description="提取 output.json -> patch*.java + add_test*.java"
    )
    ap.add_argument("--root", default=".",
                    help="包含 <project>/<id>/output.json 的根目录（默认当前目录）")
    ap.add_argument("--project", default=None,
                    help="只处理指定 project（例如 Chart）")
    ap.add_argument("--ids", default=None,
                    help="只处理指定 bug id，例如 '1,3,5-7'")
    ap.add_argument("--quiet", action="store_true",
                    help="只输出失败/跳过信息")
    args = ap.parse_args()

    root = Path(args.root).expanduser().resolve()
    if not root.is_dir():
        raise SystemExit(f"[ERROR] root 不存在或不是目录: {root}")

    id_filter = parse_id_expr(args.ids) if args.ids else None

    total = ok = skipped = 0
    for out_json in iter_output_json(root, args.project, id_filter):
        total += 1
        if process_one(out_json, quiet=args.quiet):
            ok += 1
        else:
            skipped += 1

    print("\n[DONE]")
    print(f"  root    = {root}")
    if args.project:
        print(f"  project = {args.project}")
    if args.ids:
        print(f"  ids     = {args.ids}")
    print(f"  total   = {total}")
    print(f"  ok      = {ok}")
    print(f"  skipped = {skipped}")


if __name__ == "__main__":
    main()
