#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
1) 读取 z_output/<project>/batch*_results.jsonl（Claude Message Batches results，每行一个 JSON）
2) 按 custom_id = <project>_<id> 把模型输出 JSON 写入 <dest_root>/<project>/<id>/output.json
   - 每次运行覆盖 output.json
3) 生成派生文件：
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
# 模型现在的输出格式：先是分析文字（其中可能内嵌 ```java 代码块），
# 末尾才是包含 "fixed_code" 的 JSON 块（通常包在 ```json fence 里）。
# 因此不能用“第一个 fence / 第一个 { ”来定位，必须定位含 "fixed_code" 的对象。
_FIXED_CODE_OBJ_RE = re.compile(r'\{\s*"fixed_code"', re.DOTALL)
INVISIBLE_PREFIX = "\ufeff\u200b\u200c\u200d"


def extract_first_complete_json(text: str) -> Optional[str]:
    """
    从任意文本中提取“第一个完整 JSON 候选”（对象或数组），使用括号/方括号计数。
    能处理前面有分析文字、后面有补充文字的情况。
    """
    s = text.strip()
    if not s:
        return None

    # 找第一个 { 或 [
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

    # 没找到闭合：可能输出截断
    return None


def escape_control_chars_for_json(s: str) -> str:
    """
    将 JSON 文本中的控制字符转义，避免 json.loads 报 Invalid control character。
    - 保留结构性换行 '\n'（JSON 文本的换行是合法的）
    - 将 '\t' '\r' 以及其它 0x00-0x1F 转成可解析的转义序列
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


_JSON_VALID_ESCAPES = set('"\\/bfnrtu')


def repair_invalid_json_backslashes(s: str) -> str:
    """
    将 JSON 字符串值里非法的反斜杠转义（如 Java 源码中的 \\d、\\. 等）改成 \\\\，
    避免 json.loads 报 'Invalid \\escape'。只在字符串内部处理。
    """
    out = []
    i = 0
    n = len(s)
    in_str = False
    while i < n:
        ch = s[i]
        if not in_str:
            out.append(ch)
            if ch == '"':
                in_str = True
            i += 1
            continue
        if ch == "\\":
            nxt = s[i + 1] if i + 1 < n else ""
            if nxt in _JSON_VALID_ESCAPES:
                out.append(ch)
                out.append(nxt)
                i += 2
            else:
                # 非法转义：把单个反斜杠转义成字面反斜杠
                out.append("\\\\")
                i += 1
            continue
        if ch == '"':
            out.append(ch)
            in_str = False
            i += 1
            continue
        out.append(ch)
        i += 1
    return "".join(out)


def find_embedded_fixed_code_json(text: str) -> Optional[str]:
    """
    定位文本中包含 "fixed_code" key 的 JSON 对象（取最后一个能完整闭合的）。
    模型输出现在是：分析文字（内含 ```java 块）+ 末尾 ```json 块，
    因此必须按 key 定位，而不能取“第一个 fence / 第一个大括号”。
    """
    matches = list(_FIXED_CODE_OBJ_RE.finditer(text))
    for m in reversed(matches):
        cand = extract_first_complete_json(text[m.start():])
        if cand:
            return cand
    return None


def _try_loads_dict(cand: str) -> Tup[Optional[Dict[str, Any]], Optional[str]]:
    """对一个 JSON 候选串做多级兜底解析（控制字符、非法反斜杠转义）。"""
    last_err = None
    transforms = [
        lambda x: x,
        escape_control_chars_for_json,
        lambda x: repair_invalid_json_backslashes(escape_control_chars_for_json(x)),
        lambda x: escape_control_chars_for_json(repair_invalid_json_backslashes(x)),
    ]
    for fn in transforms:
        try:
            obj = json.loads(fn(cand))
        except Exception as e:
            last_err = str(e)
            continue
        if isinstance(obj, dict):
            return obj, None
        last_err = f"json_is_{type(obj).__name__}_not_dict"
    return None, last_err


def parse_model_output_json(text: str) -> Tup[Optional[Dict[str, Any]], Optional[str]]:
    """
    将模型输出（分析文字 + 末尾含 fixed_code 的 JSON 块）解析为 dict，返回 (obj, errstr)

    流程：
    1) 去掉 BOM/零宽字符
    2) 优先定位含 "fixed_code" 的 JSON 对象并解析（控制字符 / 非法反斜杠兜底）
    3) 兜底：抽取第一个完整 JSON 片段再解析
    """
    if not text or not text.strip():
        return None, "empty_text"

    t = text.lstrip(INVISIBLE_PREFIX).strip()

    # 1) 定位含 fixed_code 的 JSON 对象
    cand = find_embedded_fixed_code_json(t)
    if cand:
        obj, err = _try_loads_dict(cand)
        if obj is not None:
            return obj, None
        err1 = f"fixed_code_block_failed: {err}"
    else:
        err1 = "no_fixed_code_block_found"

    # 2) 兜底：第一个完整 JSON 片段
    cand2 = extract_first_complete_json(t)
    if cand2 and cand2 != cand:
        obj, err = _try_loads_dict(cand2)
        if obj is not None:
            return obj, None
        return None, f"{err1}; first_json_failed: {err}"

    return None, err1


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

                # 解析 results 行 JSON
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

                # Claude：只处理 succeeded；否则跳过并打印原因
                result = wrapper.get("result") or {}
                rtype = result.get("type") if isinstance(result, dict) else None
                if rtype != "succeeded":
                    skipped += 1
                    reason = None
                    if isinstance(result, dict):
                        reason = result.get("error") or result.get("message") or result.get("status")
                    print(f"[SKIP] {out_file.name}:{ln} {custom_id}: result.type={rtype} reason={reason}")
                    continue

                # 取 assistant 文本
                text = find_assistant_output_text(wrapper)
                if not text or not str(text).strip():
                    skipped += 1
                    print(f"[SKIP] {out_file.name}:{ln} {custom_id}: 未找到 assistant text content")
                    continue

                # 解析模型输出为 JSON（dict）
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

                # 写入 <dest_root>/<project>/<id>/output.json （覆盖）
                dest_dir = dest_root / project / sub_id
                dest_dir.mkdir(parents=True, exist_ok=True)

                (dest_dir / "output.json").write_text(
                    json.dumps(model_json, ensure_ascii=False, indent=2),
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