#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
1) 读取 z_output/<project>/batch*_results.jsonl（Claude Message Batches results，每行一个 JSON）
2) 按 custom_id = <project>_<id> 把模型输出 JSON 写入 <dest_root>/<project>/<id>/output.json
   - 每次运行覆盖 output.json
3) 生成派生文件：
   - trigger.json：收集 trigger_method 字段（列表，直接写出）
   - patch.java, patch1.java, patch2.java...：来自 fixed_code 数组
4) 可选 debug：
   - 解析失败时写 raw.txt（--dump-raw-on-fail）

用法：
  python step1_extract_output.py --outputs-root z_output --dest-root .
"""

import argparse
import json
import re
from pathlib import Path
from typing import Optional, Tuple, Dict, Any, Iterable, Tuple as Tup


# ---------- Claude 文本输出 -> JSON 的鲁棒解析 ----------
FENCE_JSON_RE = re.compile(r"^\s*```json\s*$", re.IGNORECASE)
FENCE_ANY_RE = re.compile(r"^\s*```(?:\w+)?\s*$", re.IGNORECASE)
FENCE_END_RE = re.compile(r"^\s*```\s*$")
XML_JSON_RE = re.compile(
    r"<(?:json|answer|output|response)>\s*(.*?)\s*</(?:json|answer|output|response)>",
    re.DOTALL | re.IGNORECASE,
)
# <parameter name="...">JSON</parameter> —— 模型把 JSON 放进工具调用参数里
XML_PARAM_RE = re.compile(r"<parameter[^>]*>\s*(\{.*?\})\s*</parameter>", re.DOTALL)
# <trigger_method>...</trigger_method> + <fixed_code>...</fixed_code> 替代 JSON 格式
XML_FIELD_TRIGGER_RE = re.compile(r"<trigger_method>(.*?)</trigger_method>", re.DOTALL | re.IGNORECASE)
XML_FIELD_CODE_RE = re.compile(r"<fixed_code>(.*?)</fixed_code>", re.DOTALL | re.IGNORECASE)
INVISIBLE_PREFIX = "﻿​‌‍"


def _extract_fence_block(lines: list, start_re: re.Pattern) -> Optional[Tuple[int, int]]:
    """找到匹配 start_re 的第一个有闭合的 fence，返回 (start, end) 行号，未找到返回 None。"""
    n = len(lines)
    start = None
    for i in range(n):
        if start_re.match(lines[i].lstrip(INVISIBLE_PREFIX)):
            start = i
            break
    if start is None:
        return None
    for j in range(start + 1, n):
        if FENCE_END_RE.match(lines[j]):
            return start, j
    return start, None  # 未闭合


def strip_outer_code_fence(text: str) -> str:
    """
    优先提取 ```json fence 的内容；若没有，再退回第一个任意 fence。
    - 支持 fence 前后有说明文字
    - 若只有开头 fence 没有闭合（输出被截断），也会去掉开头 fence 行
    """
    text = text.lstrip(INVISIBLE_PREFIX)
    lines = text.splitlines()

    # 1) 优先寻找 ```json fence
    result = _extract_fence_block(lines, FENCE_JSON_RE)
    if result is None:
        # 2) 退回任意 fence（```、```java 等）
        result = _extract_fence_block(lines, FENCE_ANY_RE)
    if result is None:
        return text.strip()

    start, end = result
    if end is None:
        return "\n".join(lines[start + 1 :]).strip()
    return "\n".join(lines[start + 1 : end]).strip()


def extract_first_complete_json(text: str) -> Optional[str]:
    """
    从任意文本中提取"第一个完整 JSON 候选"（对象或数组），使用括号/方括号计数。
    能处理前面有分析文字、后面有补充文字的情况。
    """
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
    """
    将 JSON 文本中的控制字符转义，避免 json.loads 报 Invalid control character。
    """
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


def extract_last_complete_json(text: str) -> Optional[str]:
    """
    从后往前找文本中"像 JSON 对象入口的 { "位置，返回紧贴文本末尾的完整 JSON 对象。

    判断 { 是否为 JSON 入口的条件：
      1. 位于文本开头，或
      2. 前面紧跟换行符（\\n{），且该 { 后面紧跟空白+引号（即 JSON key 模式 {\\s*"）。
    条件 2 可跳过 Java 表达式里的大括号（如 if (...) {），因为那些后面不直接跟引号。
    """
    s = text.strip()
    if not s:
        return None

    positions = []
    if s[0] == "{":
        positions.append(0)

    for i, ch in enumerate(s):
        if ch != "{" or i == 0:
            continue
        # 允许 { 前面是换行符、> （XML 标签结尾）、或 < （模型用 <{JSON}> 格式）
        if s[i - 1] not in ("\n", ">", "<"):
            continue
        # { 后面（跳过空白含换行）紧跟 " ——JSON key 特征，用来排除 Java 代码中的大括号
        j = i + 1
        while j < len(s) and s[j] in " \t\r\n":
            j += 1
        if j < len(s) and s[j] == '"':
            positions.append(i)

    for pos in reversed(positions):
        candidate = extract_first_complete_json(s[pos:])
        if candidate is None:
            continue
        trailing = s[pos + len(candidate) :].strip()
        # 允许 trailing 为空、XML 闭合标签、代码 fence、或单个 > （模型误加的残余字符）
        if trailing == "" or trailing == ">" or trailing.startswith("</") or trailing.startswith("```"):
            return candidate
    return None


def _try_parse_dict(s: str) -> Tup[Optional[Dict[str, Any]], Optional[str]]:
    """尝试将字符串解析为 dict，自动重试控制字符转义。返回 (obj, errstr)。"""
    try:
        obj = json.loads(s)
        if isinstance(obj, dict):
            return obj, None
        return None, f"json_is_{type(obj).__name__}_not_dict"
    except Exception as e:
        err = f"loads_failed: {e}"
    if "Invalid control character" in err:
        try:
            obj = json.loads(escape_control_chars_for_json(s))
            if isinstance(obj, dict):
                return obj, None
        except Exception as e2:
            err = f"{err}; sanitized_failed: {e2}"
    return None, err


def parse_model_output_json(text: str) -> Tup[Optional[Dict[str, Any]], Optional[str]]:
    """
    将模型输出（可能包含 fences/解释文本/控制字符/<json>标签）解析为 dict，返回 (obj, errstr)。
    解析顺序：
      1. <json|answer|output|response>...</...> XML 标签
      2. fence 剥离后直接解析（原有逻辑）
      3. fence 剥离后提取第一个完整 JSON
      4. 原始文本末尾向前扫描找完整 JSON（处理"分析文字+末尾裸JSON"）
      5. <parameter>...</parameter> 标签（模型误用工具调用格式）
    """
    if not text or not text.strip():
        return None, "empty_text"

    # 1. <json>/<answer>/<output>/<response> 标签
    m = XML_JSON_RE.search(text)
    if m:
        obj, _ = _try_parse_dict(m.group(1).strip())
        if obj is not None:
            return obj, None

    # 2. fence 剥离后直接解析
    t = strip_outer_code_fence(text)
    t = t.lstrip(INVISIBLE_PREFIX).strip()
    obj, err1 = _try_parse_dict(t)
    if obj is not None:
        return obj, None

    # 3. fence 剥离后提取第一个完整 JSON
    cand = extract_first_complete_json(t)
    if cand:
        obj, _ = _try_parse_dict(cand)
        if obj is not None:
            return obj, None

    # 4. 从原始文本末尾向前扫描（处理分析文字开头 + 末尾裸 JSON，或 JSON 后紧跟 XML 闭合标签）
    raw = text.lstrip(INVISIBLE_PREFIX)
    last_cand = extract_last_complete_json(raw)
    if last_cand and last_cand != cand:
        obj, _ = _try_parse_dict(last_cand)
        if obj is not None:
            return obj, None

    # 5. <parameter>JSON</parameter>（模型把结果放进工具调用参数里）
    for pm in XML_PARAM_RE.finditer(text):
        obj, _ = _try_parse_dict(pm.group(1).strip())
        if obj is not None:
            return obj, None

    # 6. <trigger_method>...</trigger_method> + <fixed_code>...</fixed_code>
    #    模型用独立 XML 标签代替 JSON 格式，重组为等价 dict
    tm = XML_FIELD_TRIGGER_RE.search(text)
    fc = XML_FIELD_CODE_RE.search(text)
    if tm or fc:
        trigger_methods = []
        if tm:
            trigger_methods = [l.strip() for l in tm.group(1).splitlines() if l.strip()]
        fixed_codes = []
        if fc:
            code = fc.group(1).strip()
            if code:
                fixed_codes = [code]
        return {"trigger_method": trigger_methods, "fixed_code": fixed_codes}, None

    return None, f"{err1}; all_fallbacks_failed"


# ---------- Claude results 行提取 ----------
def find_assistant_output_text(wrapper: dict) -> Optional[str]:
    """
    Claude results 行通常形如：
    {
      "custom_id": "...",
      "result": {
        "type": "succeeded",
        "message": {
          "role": "assistant",
          "content": [{"type":"text","text":"..."}]
        }
      }
    }
    取所有 text 段落拼接。
    """
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

        if not parts:
            return None

        return "\n".join(parts)
    except Exception:
        return None


# ---------- 业务逻辑：custom_id / 文件遍历 / 输出 ----------
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


def iter_output_files(outputs_root: Path) -> Iterable[Tuple[str, Path]]:
    """
    遍历 z_output/<project>/*_results.jsonl
    优先匹配 batch*_results.jsonl
    """
    for proj_dir in sorted(outputs_root.iterdir()):
        if not proj_dir.is_dir():
            continue

        patterns = [
            "batch*_results.jsonl",
            "*_results.jsonl",
        ]
        seen = set()
        for pat in patterns:
            for p in sorted(proj_dir.glob(pat)):
                if p in seen:
                    continue
                seen.add(p)
                yield proj_dir.name, p


def extract_trigger_methods(model_json: Dict[str, Any]) -> Any:
    """提取 trigger_method 字段（列表）"""
    return model_json.get("trigger_method", [])


def write_patches(dest_dir: Path, fixed_codes: Any) -> None:
    """
    将 fixed_code 数组写入 patch.java, patch1.java, patch2.java...
    运行前会先删除已有 patch*.java（避免残留）
    """
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


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--outputs-root", default="z_output", help="包含各 project 输出的目录（默认 z_output）")
    ap.add_argument("--dest-root", default=".", help="写入 <project>/<id>/output.json 的根目录（默认当前目录）")
    ap.add_argument("--quiet", action="store_true", help="只输出失败信息（可选）")
    ap.add_argument(
        "--dump-raw-on-fail",
        action="store_true",
        help="解析失败时把原始模型输出写到 <project>/<id>/raw.txt（便于定位）",
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

                # 写入 <dest_root>/<project>/<id>/output.json（覆盖）
                dest_dir = dest_root / project / sub_id
                dest_dir.mkdir(parents=True, exist_ok=True)

                (dest_dir / "output.json").write_text(
                    json.dumps(model_json, ensure_ascii=False, indent=2),
                    encoding="utf-8",
                )

                # 生成 trigger.json（trigger_method）
                trigger = extract_trigger_methods(model_json)
                (dest_dir / "trigger.json").write_text(
                    json.dumps(trigger, ensure_ascii=False, indent=2),
                    encoding="utf-8",
                )

                # 生成 patch*.java（fixed_code）
                write_patches(dest_dir, model_json.get("fixed_code", []))

                ok += 1

    if not args.quiet:
        print("\n[DONE]")
        print(f"  total   = {total}")
        print(f"  ok      = {ok}")
        print(f"  skipped = {skipped}")


if __name__ == "__main__":
    main()
