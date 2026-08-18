#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
1) 读取 z_output/<project>/batch*_results.jsonl（Claude Message Batches results，每行一个 JSON）
2) 按 custom_id = <project>_<id> 把模型输出 JSON 写入 <dest_root>/<project>/<id>/output.json
   - 每次运行覆盖 output.json
3) 生成派生文件：
   - patch.java, patch1.java, patch2.java...：来自 fixed_code 数组
   - add_test.java, add_test1.java...：来自 additional_testcases 数组
     每个文件第一行是 // {path}，其余是 testcase 代码
4) 可选 debug：
   - 解析失败时写 raw.txt（--dump-raw-on-fail）

用法：
  python step1_extract_output.py
  python step1_extract_output.py --outputs-root z_output --dest-root .
  python step1_extract_output.py --project Chart
  python step1_extract_output.py --project Chart Lang Math
"""

import argparse
import json
import re
from pathlib import Path
from typing import Optional, Tuple, Dict, Any, Iterable, Tuple as Tup


# ---------- Claude 文本输出 -> JSON 的鲁棒解析 ----------
FENCE_START_RE = re.compile(r"^\s*```(?:json)?\s*$", re.IGNORECASE)
FENCE_END_RE = re.compile(r"^\s*```\s*$")
INVISIBLE_PREFIX = "\ufeff\u200b\u200c\u200d"


def strip_outer_code_fence(text: str) -> str:
    text = text.lstrip(INVISIBLE_PREFIX)
    lines = text.splitlines()
    n = len(lines)

    start = None
    for i in range(n):
        line = lines[i].lstrip(INVISIBLE_PREFIX)
        if FENCE_START_RE.match(line):
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
        return "\n".join(lines[start + 1 :]).strip()

    return "\n".join(lines[start + 1 : end]).strip()


def extract_first_complete_json(text: str) -> Optional[str]:
    s = text.strip()
    if not s:
        return None

    start = None
    opening = None
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
                return s[start : j + 1].strip()

    return None


def escape_control_chars_for_json(s: str) -> str:
    out = []
    for ch in s:
        o = ord(ch)
        if ch == "\n":
            out.append("\n")
            continue
        if ch == "\r":
            out.append("\\r")
            continue
        if ch == "\t":
            out.append("\\t")
            continue
        if o < 0x20:
            out.append("\\u%04x" % o)
            continue
        out.append(ch)
    return "".join(out)


def parse_model_output_json(text: str) -> Tup[Optional[Dict[str, Any]], Optional[str]]:
    if not text or not text.strip():
        return None, "empty_text"

    t = strip_outer_code_fence(text)
    t = t.lstrip(INVISIBLE_PREFIX).strip()

    try:
        obj = json.loads(t)
        if isinstance(obj, dict):
            return obj, None
        return None, f"json_is_{type(obj).__name__}_not_dict"
    except Exception as e1:
        err1 = f"direct_loads_failed: {e1}"

    if "Invalid control character" in err1:
        t2 = escape_control_chars_for_json(t)
        try:
            obj = json.loads(t2)
            if isinstance(obj, dict):
                return obj, None
            return None, f"{err1}; sanitized_json_is_{type(obj).__name__}_not_dict"
        except Exception as e1b:
            err1 = f"{err1}; sanitized_retry_failed: {e1b}"

    cand = extract_first_complete_json(t)
    if not cand:
        return None, f"{err1}; no_complete_json_found"

    try:
        obj = json.loads(cand)
        if isinstance(obj, dict):
            return obj, None
        return None, f"{err1}; extracted_json_is_{type(obj).__name__}_not_dict"
    except Exception as e2:
        if "Invalid control character" in str(e2):
            cand2 = escape_control_chars_for_json(cand)
            try:
                obj = json.loads(cand2)
                if isinstance(obj, dict):
                    return obj, None
                return None, f"{err1}; extracted_sanitized_json_is_{type(obj).__name__}_not_dict"
            except Exception as e2b:
                return None, f"{err1}; extracted_loads_failed: {e2}; extracted_sanitized_retry_failed: {e2b}"
        return None, f"{err1}; extracted_loads_failed: {e2}"


# ---------- Claude results 行提取 ----------
def find_assistant_output_text(wrapper: dict) -> Optional[str]:
    try:
        result = wrapper.get("result") or {}
        if not isinstance(result, dict) or result.get("type") != "succeeded":
            return None

        msg = result.get("message") or {}
        if not isinstance(msg, dict):
            return None

        content = msg.get("content", [])
        if not isinstance(content, list):
            return None

        parts = []
        for c in content:
            if isinstance(c, dict) and c.get("type") == "text":
                t = c.get("text")
                if isinstance(t, str) and t.strip():
                    parts.append(t)

        return "\n".join(parts) if parts else None
    except Exception:
        return None


# ---------- 业务逻辑 ----------
def parse_custom_id(custom_id: str) -> Optional[Tuple[str, str]]:
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


def iter_output_files(outputs_root: Path, projects: Optional[list] = None) -> Iterable[Tuple[str, Path]]:
    for proj_dir in sorted(outputs_root.iterdir()):
        if not proj_dir.is_dir():
            continue
        if projects and proj_dir.name not in projects:
            continue
        seen = set()
        for pat in ["batch*_results.jsonl", "*_results.jsonl"]:
            for p in sorted(proj_dir.glob(pat)):
                if p in seen:
                    continue
                seen.add(p)
                yield proj_dir.name, p


def write_patches(dest_dir: Path, fixed_codes: Any) -> None:
    """写 patch.java, patch1.java, ... （先清除旧文件）"""
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


def write_add_tests(dest_dir: Path, additional_testcases: Any) -> None:
    """
    写 add_test.java, add_test1.java, ...（先清除旧文件）
    每个文件：第一行 // {path}，其余为 testcase 代码
    """
    for old in dest_dir.glob("add_test*.java"):
        try:
            old.unlink()
        except Exception:
            pass

    if not isinstance(additional_testcases, list):
        return

    for idx, tc in enumerate(additional_testcases):
        if not isinstance(tc, dict):
            continue
        path = tc.get("path", "")
        code = tc.get("testcase", "")
        if not isinstance(path, str):
            path = str(path) if path else ""
        if not isinstance(code, str):
            code = str(code) if code else ""

        content = f"// {path}\n{code}"
        fname = "add_test.java" if idx == 0 else f"add_test{idx}.java"
        (dest_dir / fname).write_text(content, encoding="utf-8")


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--outputs-root", default="z_output",
                    help="包含各 project 输出的目录（默认 z_output）")
    ap.add_argument("--dest-root", default=".",
                    help="写入 <project>/<id>/output.json 的根目录（默认当前目录）")
    ap.add_argument("--project", nargs="+", metavar="PROJECT",
                    help="只处理指定的 project（可指定多个，默认处理全部）")
    ap.add_argument("--quiet", action="store_true", help="只输出失败信息")
    ap.add_argument("--dump-raw-on-fail", action="store_true",
                    help="解析失败时把原始模型输出写到 <project>/<id>/raw.txt")
    args = ap.parse_args()

    outputs_root = Path(args.outputs_root).expanduser().resolve()
    dest_root = Path(args.dest_root).expanduser().resolve()

    if not outputs_root.is_dir():
        raise SystemExit(f"[ERROR] outputs_root 不存在或不是目录：{outputs_root}")

    total = 0
    ok = 0
    skipped = 0

    for _, out_file in iter_output_files(outputs_root, args.project):
        with out_file.open("r", encoding="utf-8", errors="ignore") as f:
            for ln, line in enumerate(f, start=1):
                line = line.strip()
                if not line:
                    continue
                total += 1

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

                result = wrapper.get("result") or {}
                rtype = result.get("type") if isinstance(result, dict) else None
                if rtype != "succeeded":
                    skipped += 1
                    reason = None
                    if isinstance(result, dict):
                        reason = result.get("error") or result.get("message") or result.get("status")
                    print(f"[SKIP] {out_file.name}:{ln} {custom_id}: result.type={rtype} reason={reason}")
                    continue

                text = find_assistant_output_text(wrapper)
                if not text or not str(text).strip():
                    skipped += 1
                    print(f"[SKIP] {out_file.name}:{ln} {custom_id}: 未找到 assistant text content")
                    continue

                model_json, perr = parse_model_output_json(text)
                if not isinstance(model_json, dict):
                    skipped += 1
                    preview = (text.strip().replace("\n", "\\n"))[:200]
                    tail = (text.strip().replace("\n", "\\n"))[-200:]
                    print(
                        f"[SKIP] {out_file.name}:{ln} {custom_id}: 模型输出 JSON 解析失败：{perr}. "
                        f"preview={preview!r} tail={tail!r}"
                    )
                    if args.dump_raw_on_fail:
                        dest_dir = dest_root / project / sub_id
                        dest_dir.mkdir(parents=True, exist_ok=True)
                        (dest_dir / "raw.txt").write_text(text, encoding="utf-8")
                    continue

                dest_dir = dest_root / project / sub_id
                dest_dir.mkdir(parents=True, exist_ok=True)

                (dest_dir / "output.json").write_text(
                    json.dumps(model_json, ensure_ascii=False, indent=2),
                    encoding="utf-8",
                )

                write_patches(dest_dir, model_json.get("fixed_code", []))
                write_add_tests(dest_dir, model_json.get("additional_testcases", []))

                ok += 1

    if not args.quiet:
        print("\n[DONE]")
        print(f"  total   = {total}")
        print(f"  ok      = {ok}")
        print(f"  skipped = {skipped}")


if __name__ == "__main__":
    main()
