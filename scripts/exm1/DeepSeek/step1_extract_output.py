#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
从已有的 <root>/<project>/<id>/output.json 生成派生文件：

  output.json 格式特殊：文件开头是模型的分析文本，末尾内嵌一个 JSON 块，
  JSON 块包含 trigger_method（列表）和 fixed_code（列表）字段。

派生文件：
  - trigger.json：trigger_method 字段（列表，直接写出）
  - patch.java, patch1.java, patch2.java...：来自 fixed_code 数组

用法：
  python step1_extract_output.py --root .
  python step1_extract_output.py --root . --project Chart
  python step1_extract_output.py --root . --quiet
  python step1_extract_output.py --root . --csv /path/to/fix_diff_orig_vs_new.csv
"""

import argparse
import csv
import json
import re
from pathlib import Path
from typing import Any, Dict, Iterable, Optional, Set, Tuple


# ---------- 从"文本+末尾JSON"格式中提取 JSON 块 ----------

def extract_first_complete_json(text: str) -> Optional[str]:
    """从任意文本中提取第一个完整 JSON 对象/数组（括号计数法）。"""
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
                return s[start: j + 1].strip()
    return None


def extract_last_complete_json(text: str) -> Optional[str]:
    """
    从后往前找文本中"像 JSON 对象入口的 {"，返回紧贴文本末尾的完整 JSON 对象。
    判断条件：位于文本开头，或前面紧跟换行符且其后紧跟空白+引号（JSON key 特征）。
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
        if s[i - 1] not in ("\n", ">", "<"):
            continue
        j = i + 1
        while j < len(s) and s[j] in " \t\r\n":
            j += 1
        if j < len(s) and s[j] == '"':
            positions.append(i)

    for pos in reversed(positions):
        candidate = extract_first_complete_json(s[pos:])
        if candidate is None:
            continue
        trailing = s[pos + len(candidate):].strip()
        if trailing == "" or trailing == ">" or trailing.startswith("</") or trailing.startswith("```"):
            return candidate
    return None


def escape_control_chars_for_json(s: str) -> str:
    out = []
    for ch in s:
        o = ord(ch)
        if ch in ("\n", "\r", "\t"):
            out.append(ch if ch == "\n" else ("\\r" if ch == "\r" else "\\t"))
            continue
        if o < 0x20:
            out.append("\\u%04x" % o)
            continue
        out.append(ch)
    return "".join(out)


# XML 字段兜底
_XML_TRIGGER_RE = re.compile(r"<trigger_method>(.*?)</trigger_method>", re.DOTALL | re.IGNORECASE)
_XML_CODE_RE = re.compile(r"<fixed_code>(.*?)</fixed_code>", re.DOTALL | re.IGNORECASE)

# Java 字符串拼接：`"...\n" +\n    "..."` → 合并为单一 JSON 字符串
_JAVA_CONCAT_RE = re.compile(r'"\s*\+\s*\n\s*"')


def _try_parse_dict(s: str) -> Tuple[Optional[Dict[str, Any]], Optional[str]]:
    def _load(text: str) -> Optional[Dict[str, Any]]:
        try:
            obj = json.loads(text)
            if isinstance(obj, dict):
                return obj
        except Exception:
            pass
        try:
            obj = json.loads(text, strict=False)
            if isinstance(obj, dict):
                return obj
        except Exception:
            pass
        return None

    obj = _load(s)
    if obj is not None:
        return obj, None

    err = "loads_failed"
    # 控制字符转义
    s_esc = escape_control_chars_for_json(s)
    if s_esc != s:
        obj = _load(s_esc)
        if obj is not None:
            return obj, None
        err = "sanitized_failed"

    # Java 字符串拼接（"...\n" +\n    "..."）
    s2 = _JAVA_CONCAT_RE.sub("", s)
    if s2 != s:
        obj = _load(s2)
        if obj is not None:
            return obj, None
        obj = _load(escape_control_chars_for_json(s2))
        if obj is not None:
            return obj, None
        err = f"{err}; java_concat_failed"

    return None, err


_LAST_JSON_ENTRY_RE = re.compile(
    r'(?:^|[.\s:>])(\{)\s*"(?:trigger_method|fixed_code)',
    re.DOTALL,
)


def find_embedded_json_block(text: str) -> Optional[str]:
    """
    定位文本末尾的嵌入 JSON 块（以 trigger_method / fixed_code 开头的对象）。
    DeepSeek 输出格式：分析文字在前，JSON 块紧跟在句末（如 "...output.{\\n  \\"trigger_method\\"..."）。
    """
    # 找所有满足条件的 { 位置（后面紧跟 trigger_method / fixed_code key）
    matches = list(re.finditer(r'\{\s*"(?:trigger_method|fixed_code)"', text))
    if not matches:
        return None
    # 从最后一个匹配往前尝试，取第一个能完整解析的
    for m in reversed(matches):
        candidate = extract_first_complete_json(text[m.start():])
        if candidate:
            return candidate
    return None


def parse_mixed_output(text: str) -> Tuple[Optional[Dict[str, Any]], Optional[str]]:
    """
    从"分析文字 + 末尾嵌入 JSON"的混合文本中提取 dict。
    解析顺序：
      1. 文本本身直接就是合法 JSON
      2. 定位包含 trigger_method/fixed_code key 的嵌入 JSON 块
      3. 通用末尾扫描
      4. <trigger_method>/<fixed_code> XML 标签兜底
    """
    if not text or not text.strip():
        return None, "empty_text"

    # 1. 直接是 JSON
    obj, _ = _try_parse_dict(text.strip())
    if obj is not None:
        return obj, None

    # 2. 定位嵌入 JSON 块（DeepSeek 特有格式：分析文字 + 末尾 JSON）
    embedded = find_embedded_json_block(text)
    if embedded:
        obj, err = _try_parse_dict(embedded)
        if obj is not None:
            return obj, None

    # 3. 通用末尾扫描（处理其他格式变体）
    last_cand = extract_last_complete_json(text)
    if last_cand and last_cand != embedded:
        obj, err = _try_parse_dict(last_cand)
        if obj is not None:
            return obj, None

    # 4. XML 标签兜底
    tm = _XML_TRIGGER_RE.search(text)
    fc = _XML_CODE_RE.search(text)
    if tm or fc:
        trigger_methods = [l.strip() for l in tm.group(1).splitlines() if l.strip()] if tm else []
        fixed_codes = [fc.group(1).strip()] if (fc and fc.group(1).strip()) else []
        return {"trigger_method": trigger_methods, "fixed_code": fixed_codes}, None

    return None, "all_fallbacks_failed"


# ---------- 业务逻辑 ----------

def write_patches(dest_dir: Path, fixed_codes: Any) -> None:
    """将 fixed_code 写入 patch.java, patch1.java...，运行前先清理旧文件。"""
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


def process_one(output_path: Path, quiet: bool = False) -> bool:
    """处理单个 output.json，生成 trigger.json 和 patch*.java。"""
    try:
        text = output_path.read_text(encoding="utf-8", errors="ignore").strip()
        if not text:
            if not quiet:
                print(f"[SKIP] {output_path}: empty file")
            return False

        model_json, err = parse_mixed_output(text)
        if model_json is None:
            if not quiet:
                print(f"[SKIP] {output_path}: parse error: {err}")
            return False

        dest_dir = output_path.parent

        # trigger.json
        trigger = model_json.get("trigger_method", [])
        (dest_dir / "trigger.json").write_text(
            json.dumps(trigger, ensure_ascii=False, indent=2),
            encoding="utf-8",
        )

        # patch*.java
        write_patches(dest_dir, model_json.get("fixed_code"))

        if not quiet:
            print(f"[OK] {output_path}")
        return True

    except Exception as e:
        if not quiet:
            print(f"[FAIL] {output_path}: {e}")
        return False


def iter_output_json_files(root: Path, project: Optional[str] = None):
    if project:
        base = root / project
        if base.is_dir():
            yield from base.rglob("output.json")
        return
    yield from root.rglob("output.json")


def parse_ids_expr(expr: str) -> Set[int]:
    """解析 --ids 表达式，支持 "1,2,5-9" 这种形式，返回整数集合。"""
    ids: Set[int] = set()
    for tok in expr.split(","):
        tok = tok.strip()
        if not tok:
            continue
        if "-" in tok:
            a, b = tok.split("-", 1)
            a, b = int(a.strip()), int(b.strip())
            if a > b:
                a, b = b, a
            ids.update(range(a, b + 1))
        else:
            ids.add(int(tok))
    return ids


def iter_output_json_by_ids(root: Path, project: str, ids: Set[int]):
    """按 --ids 过滤 <root>/<project>/<id>{,_*}/output.json。"""
    base = root / project
    if not base.is_dir():
        return
    for sub in sorted(base.iterdir()):
        if not sub.is_dir():
            continue
        name = sub.name
        main_id_str = name.split("_", 1)[0]
        try:
            main_id = int(main_id_str)
        except ValueError:
            continue
        if main_id in ids:
            out = sub / "output.json"
            if out.exists():
                yield out


def load_csv_pairs(csv_path: Path) -> Set[Tuple[str, str]]:
    """读取 (project, bug_id) 二元组集合。"""
    pairs: Set[Tuple[str, str]] = set()
    with open(csv_path, newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        if not reader.fieldnames or "project" not in reader.fieldnames or "bug_id" not in reader.fieldnames:
            raise SystemExit(f"[ERROR] CSV 缺少 project/bug_id 列：{csv_path}")
        for row in reader:
            proj = (row.get("project") or "").strip()
            bid = (row.get("bug_id") or "").strip()
            if proj and bid:
                pairs.add((proj, bid))
    return pairs


def iter_output_json_from_csv(root: Path, pairs: Iterable[Tuple[str, str]]):
    """按 CSV 列出的 (project, bug_id) 精确定位 <root>/<project>/<bug_id>/output.json。"""
    for proj, bid in pairs:
        yield root / proj / bid / "output.json"


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=".", help="包含 <project>/<id>/output.json 的根目录（默认当前目录）")
    ap.add_argument("--project", default=None, help="只处理指定 project（例如 Chart / Closure / Lang）")
    ap.add_argument("--csv", default=None,
                    help="只处理 CSV 文件里列出的 (project, bug_id)。CSV 必须包含 project/bug_id 列。"
                         "与 --project 互斥。")
    ap.add_argument("--ids", default=None,
                    help="只处理指定 bug id，逗号 / 区间，例如 '29' 或 '1,3,5-10'。"
                         "默认含 sub-variants（29 也会匹配 29_1, 29_2 等）。需配合 --project 使用。")
    ap.add_argument("--quiet", action="store_true", help="只输出失败/跳过信息")
    args = ap.parse_args()

    if args.csv and args.project:
        raise SystemExit("[ERROR] --csv 与 --project 不能同时使用")
    if args.ids and not args.project:
        raise SystemExit("[ERROR] --ids 需要配合 --project 使用")
    if args.ids and args.csv:
        raise SystemExit("[ERROR] --ids 与 --csv 不能同时使用")

    root = Path(args.root).expanduser().resolve()
    if not root.is_dir():
        raise SystemExit(f"[ERROR] root 不存在或不是目录：{root}")

    total = ok = skipped = missing = 0

    if args.csv:
        csv_path = Path(args.csv).expanduser().resolve()
        if not csv_path.is_file():
            raise SystemExit(f"[ERROR] CSV 不存在：{csv_path}")
        pairs = sorted(load_csv_pairs(csv_path))
        files = iter_output_json_from_csv(root, pairs)
    elif args.ids:
        ids = parse_ids_expr(args.ids)
        files = iter_output_json_by_ids(root, args.project, ids)
    else:
        files = iter_output_json_files(root, args.project)

    for out_json in files:
        if not out_json.exists():
            missing += 1
            if not args.quiet:
                print(f"[MISS] {out_json}")
            continue
        total += 1
        if process_one(out_json, quiet=args.quiet):
            ok += 1
        else:
            skipped += 1

    print("\n[DONE]")
    print(f"  root    = {root}")
    if args.csv:
        print(f"  csv     = {Path(args.csv).expanduser().resolve()}")
    if args.project:
        print(f"  project = {args.project}")
    if args.ids:
        print(f"  ids     = {args.ids}")
    print(f"  total   = {total}")
    print(f"  ok      = {ok}")
    print(f"  skipped = {skipped}")
    if args.csv:
        print(f"  missing = {missing}")


if __name__ == "__main__":
    main()
