#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
exm3_step3_coverage_on_buggy.py

For each <defects-root>/<project>/<project>-<id>-buggy/:
  1. Find new test cases from <results-root>/<project>/<id>/add_test*.java
     (first line: // path/to/File.java::methodName  -> FQCN::method)
  2. Run `defects4j test -t <FQCN>::<method>` with JaCoCo agent for each new test
  3. Generate jacoco.xml
  4. Parse per-line coverage for the buggy method range
     (buggy_method_start_line .. buggy_method_end_line from *_methods.csv)
  5. Write per-project CSV to <out-dir>/<Project>.csv

Requirements:
  - JACOCO_HOME env var (must contain lib/jacocoagent.jar and lib/jacococli.jar)
  - defects4j CLI on PATH

Usage:
  python exm3_step3_coverage_on_buggy.py
  python exm3_step3_coverage_on_buggy.py --project Chart
  python exm3_step3_coverage_on_buggy.py --project Chart --ids 1,3,5-7 -j 4
  python exm3_step3_coverage_on_buggy.py --skip-run        # parse existing jacoco.xml only
"""

import argparse
import csv
import os
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
from collections import defaultdict
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path
from typing import Dict, List, Optional, Set, Tuple

# -------------------- constants --------------------
JACOCO_TIMEOUT = 600   # seconds for JaCoCo phase per bug

JacocoMap = Dict[Tuple[str, str, int], Tuple[int, int, int, int]]

ADD_TEST_ID_FILE = ".add_test_id"
IND_BUGGY_RE = re.compile(r"^([A-Za-z]+)-(\d+)-buggy_ind(\d+)$")
_METHOD_RE = re.compile(r"\bpublic\s+void\s+(\w+)\s*\(")

FIELDNAMES = [
    "project", "bug_id", "folder", "function_id",
    "class_fqcn", "file", "method_sig_snippet",
    "method_start", "method_end",
    "line", "line_in_method",
    "ci", "mi", "cb", "mb", "executed",
]

# Individual coverage CSV has an extra add_test_file column
FIELDNAMES_INDIVIDUAL = [
    "project", "bug_id", "folder", "add_test_file", "function_id",
    "class_fqcn", "file", "method_sig_snippet",
    "method_start", "method_end",
    "line", "line_in_method",
    "ci", "mi", "cb", "mb", "executed",
]


# ===================== helpers: ID / path parsing =====================

def parse_id_expr(expr: str) -> set:
    ids: set = set()
    for part in expr.split(","):
        part = part.strip()
        if not part:
            continue
        m = re.match(r"^(\d+)-(\d+)$", part)
        if m:
            a, b = int(m.group(1)), int(m.group(2))
            if a > b:
                a, b = b, a
            ids.update(range(a, b + 1))
        else:
            ids.add(int(part))
    return ids


def norm_int(x: str, default: int = 0) -> int:
    try:
        return int(x)
    except Exception:
        return default


# ===================== helpers: JaCoCo execution =====================

def run_capture(cmd: List[str], cwd: Path, env=None, timeout=None) -> subprocess.CompletedProcess:
    return subprocess.run(
        cmd, cwd=str(cwd), env=env,
        stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
        text=True, timeout=timeout,
    )


def ensure_jacoco_home() -> Path:
    jh_str = os.environ.get("JACOCO_HOME", "").strip()
    if not jh_str:
        raise RuntimeError("JACOCO_HOME is not set")
    jh = Path(jh_str).expanduser().resolve()
    if not (jh / "lib/jacocoagent.jar").exists():
        raise RuntimeError(f"jacocoagent.jar not found under {jh}/lib/")
    if not (jh / "lib/jacococli.jar").exists():
        raise RuntimeError(f"jacococli.jar not found under {jh}/lib/")
    return jh


def defects4j_export_value(prop: str, cwd: Path) -> str:
    p = run_capture(["defects4j", "export", "-p", prop], cwd=cwd)
    lines = [ln.strip() for ln in (p.stdout or "").splitlines() if ln.strip()]
    if not lines:
        return ""
    for ln in reversed(lines):
        if "/" in ln or ln.startswith("."):
            return ln
    return lines[-1]


def resolve_path(cwd: Path, p: str) -> Path:
    pp = Path(p)
    return pp if pp.is_absolute() else (cwd / pp).resolve()


def clean_jacoco_artifacts(cwd: Path, log_write) -> None:
    for name in ("jacoco.exec", "jacoco.xml"):
        f = cwd / name
        if f.exists():
            f.unlink()
            log_write(f"[JACOCO] removed {name}\n")
    html_dir = cwd / "jacoco-html"
    if html_dir.exists():
        for p in sorted(html_dir.rglob("*"), reverse=True):
            if p.is_file():
                p.unlink()
        log_write("[JACOCO] cleaned jacoco-html/\n")


TEST_RE = re.compile(r"^[A-Za-z0-9_$.]+::[A-Za-z0-9_]+$")


def get_trigger_tests(cwd: Path) -> List[str]:
    out = defects4j_export_value("tests.trigger", cwd)
    # export_value returns last non-empty line; trigger tests may be multiline
    p = run_capture(["defects4j", "export", "-p", "tests.trigger"], cwd=cwd)
    tokens = re.split(r"[,\s]+", (p.stdout or "").strip())
    tests: List[str] = []
    seen: Set[str] = set()
    for t in tokens:
        if TEST_RE.match(t) and t not in seen:
            seen.add(t)
            tests.append(t)
    return tests


def get_add_test_ids(results_bug_dir: Path, only_file: Optional[str] = None) -> List[str]:
    """Read first line of each add_test*.java -> FQCN::method test IDs.
    only_file: if set, only parse this specific add_test filename."""
    ids: List[str] = []
    if only_file:
        candidates = [results_bug_dir / only_file]
    else:
        candidates = sorted(results_bug_dir.glob("add_test*.java"))
    for af in candidates:
        if not af.exists():
            continue
        try:
            text = af.read_text(encoding="utf-8", errors="replace")
            lines_af = text.splitlines()
            if not lines_af:
                continue
            first = lines_af[0].strip()
            if not first.startswith("//"):
                continue
            path_part = first[2:].strip()
            if "::" in path_part:
                file_part, method = path_part.split("::", 1)
                fqcn = file_part.strip().replace("/", ".").removesuffix(".java")
                method = method.strip()
            else:
                fqcn = path_part.replace("/", ".").removesuffix(".java")
                mm = _METHOD_RE.search("\n".join(lines_af[1:]))
                method = mm.group(1) if mm else ""
            if fqcn and method:
                ids.append(f"{fqcn}::{method}")
        except Exception:
            continue
    return ids


def build_java_tool_options(jacoco_home: Path, destfile: Path, append: bool) -> str:
    agent = jacoco_home / "lib/jacocoagent.jar"
    return (
        f"-javaagent:{agent}=destfile={destfile},"
        f"append={'true' if append else 'false'},output=file"
    )


def _generate_xml_report(
    buggy_dir: Path, jacoco_home: Path, exec_file: Path,
    xml_file: Path, log_write, timeout: int,
) -> None:
    """Generate jacoco.xml from jacoco.exec using jacococli."""
    bin_dir = resolve_path(buggy_dir, defects4j_export_value("dir.bin.classes", buggy_dir))
    src_dir = resolve_path(buggy_dir, defects4j_export_value("dir.src.classes", buggy_dir))
    if not bin_dir.exists():
        raise RuntimeError(f"bin classes not found: {bin_dir}")
    if not src_dir.exists():
        raise RuntimeError(f"src classes not found: {src_dir}")

    html_dir = buggy_dir / "jacoco-html"
    html_dir.mkdir(parents=True, exist_ok=True)
    cli = jacoco_home / "lib/jacococli.jar"

    log_write(f"[JACOCO] generating XML report -> {xml_file.name}...\n")
    p = run_capture(
        ["java", "-jar", str(cli), "report", str(exec_file),
         "--classfiles", str(bin_dir),
         "--sourcefiles", str(src_dir),
         "--xml", str(xml_file),
         "--html", str(html_dir)],
        cwd=buggy_dir, timeout=timeout,
    )
    log_write(p.stdout or "")
    if p.returncode != 0:
        raise RuntimeError(f"jacococli report failed (rc={p.returncode})")
    if not xml_file.exists() or xml_file.stat().st_size == 0:
        raise RuntimeError(f"{xml_file.name} not generated or empty")


def run_jacoco_for_dir(
    buggy_dir: Path,
    jacoco_home: Path,
    all_tests: List[str],
    log_write,
    timeout: int,
    trigger_count: int = 0,
) -> Tuple[Path, Optional[Path]]:
    """
    Run `defects4j test -t` for each test in all_tests with JaCoCo agent,
    accumulating coverage into a single jacoco.exec, then generate jacoco.xml.

    If trigger_count > 0, after running the first trigger_count tests, snapshot
    jacoco.exec and generate jacoco_base.xml (trigger-only coverage from the
    same compilation state). Then continue running the remaining add_tests and
    generate the combined jacoco.xml.

    Returns (combined_xml_path, base_xml_path_or_None).
    """
    clean_jacoco_artifacts(buggy_dir, log_write)

    exec_file = buggy_dir / "jacoco.exec"
    base_env = os.environ.copy()
    base_env.pop("JAVA_TOOL_OPTIONS", None)

    base_xml: Optional[Path] = None
    start_time = time.time()
    for i, t in enumerate(all_tests):
        if time.time() - start_time > timeout:
            raise TimeoutError(f"JaCoCo stage timeout (> {timeout}s)")

        # After running all trigger tests, snapshot for base coverage
        if trigger_count > 0 and i == trigger_count and exec_file.exists():
            import shutil
            base_exec = buggy_dir / "jacoco_base.exec"
            shutil.copy2(str(exec_file), str(base_exec))
            base_xml_path = buggy_dir / "jacoco_base.xml"
            _generate_xml_report(buggy_dir, jacoco_home, base_exec, base_xml_path, log_write, timeout)
            base_xml = base_xml_path
            log_write(f"[JACOCO] base snapshot saved ({trigger_count} trigger tests)\n")

        env = base_env.copy()
        env["JAVA_TOOL_OPTIONS"] = build_java_tool_options(jacoco_home, exec_file, append=(i > 0))
        log_write(f"[JACOCO] defects4j test -t {t}\n")
        remaining = max(60, timeout - int(time.time() - start_time))
        try:
            p = run_capture(["defects4j", "test", "-t", t], cwd=buggy_dir, env=env, timeout=remaining)
        except subprocess.TimeoutExpired:
            log_write(f"[JACOCO] single test timeout ({remaining}s): {t}\n")
            continue
        log_write(p.stdout or "")
        if "Cannot compile test suite!" in (p.stdout or ""):
            raise RuntimeError("Not Compilable: test suite failed to compile")

    if not exec_file.exists():
        raise RuntimeError("jacoco.exec not generated")

    # Generate combined XML report
    xml_file = buggy_dir / "jacoco.xml"
    _generate_xml_report(buggy_dir, jacoco_home, exec_file, xml_file, log_write, timeout)

    return xml_file, base_xml


# ===================== helpers: JaCoCo XML parsing =====================

def parse_jacoco_xml(xml_path: str) -> JacocoMap:
    tree = ET.parse(xml_path)
    root = tree.getroot()
    m: JacocoMap = {}
    for pkg in root.findall(".//package"):
        pkg_name = pkg.get("name", "")
        for sf in pkg.findall("./sourcefile"):
            sf_name = sf.get("name", "")
            for ln in sf.findall("./line"):
                nr = int(ln.get("nr", "0"))
                ci = int(ln.get("ci", "0"))
                mi = int(ln.get("mi", "0"))
                cb = int(ln.get("cb", "0"))
                mb = int(ln.get("mb", "0"))
                m[(pkg_name, sf_name, nr)] = (ci, mi, cb, mb)
    return m


def safe_parse_jacoco_xml(xml_path: str) -> Tuple[Optional[JacocoMap], Optional[str]]:
    try:
        return parse_jacoco_xml(xml_path), None
    except ET.ParseError as e:
        return None, f"ParseError: {e}"
    except Exception as e:
        return None, f"{type(e).__name__}: {e}"


def pkg_from_class_entry(class_entry: str) -> str:
    """'org.jfree.chart.plot.CategoryPlot' -> 'org/jfree/chart/plot'"""
    class_entry = (class_entry or "").strip()
    if not class_entry or "." not in class_entry:
        return ""
    return "/".join(class_entry.split(".")[:-1])


def lookup_line(jacoco_map: JacocoMap, sf_name: str, pkg_guess: str, line: int):
    if pkg_guess:
        v = jacoco_map.get((pkg_guess, sf_name, line))
        if v is not None:
            return v
    for (pkg, sf, nr), v in jacoco_map.items():
        if sf == sf_name and nr == line:
            return v
    return None


# ===================== per-bug processing =====================

def process_one_bug(
    buggy_dir: Path,
    bug_id: str,
    methods: List[dict],
    jacoco_home: Path,
    log_dir: Path,
    jacoco_timeout: int,
    jacoco_cache: dict,
    skip_run: bool,
    results_bug_dir: Optional[Path] = None,
    single_add_test_id: Optional[str] = None,
    add_test_file_label: str = "",
) -> Tuple[bool, str, List[dict]]:
    """
    single_add_test_id: if set, run JaCoCo with trigger + only this one test (individual mode).
    add_test_file_label: used as the 'add_test_file' column value in individual output rows.
    """
    log_dir.mkdir(parents=True, exist_ok=True)
    log_file = log_dir / f"{buggy_dir.name}.log"
    output_rows: List[dict] = []

    with open(log_file, "w", encoding="utf-8") as lf:
        def log_write(s: str):
            lf.write(s)
            lf.flush()

        log_write(f"==== {time.strftime('%F %T')} :: {buggy_dir.name} ====\n")

        # assign function_id within bug (needed for both normal and Not Compilable paths)
        sorted_methods = sorted(
            methods,
            key=lambda r: (
                r.get("file", ""),
                norm_int(r.get("buggy_method_start_line", "0")),
                norm_int(r.get("buggy_method_end_line", "0")),
                r.get("method_sig_snippet", ""),
            ),
        )
        for idx, r in enumerate(sorted_methods, start=1):
            r["_function_id"] = f"function_{idx}"

        def make_not_compilable_rows() -> List[dict]:
            rows = []
            for r in sorted_methods:
                project_    = r.get("project", "")
                class_entry = r.get("class_entry", "").strip()
                file_path   = r.get("file", "").strip()
                method_sig  = r.get("method_sig_snippet", "").strip()
                start       = norm_int(r.get("buggy_method_start_line", "0"))
                end         = norm_int(r.get("buggy_method_end_line", "0"))
                function_id = r.get("_function_id", "function_1")
                for line in range(start, end + 1):
                    row = {
                        "project": project_, "bug_id": bug_id,
                        "folder": buggy_dir.name,
                        "function_id": function_id,
                        "class_fqcn": class_entry, "file": file_path,
                        "method_sig_snippet": method_sig,
                        "method_start": start, "method_end": end,
                        "line": line, "line_in_method": (line - start) + 1,
                        "ci": "", "mi": "", "cb": "", "mb": "",
                        "executed": "Not Compilable",
                    }
                    if add_test_file_label:
                        row["add_test_file"] = add_test_file_label
                    rows.append(row)
            return rows

        xml_file = buggy_dir / "jacoco.xml"

        base_xml_file: Optional[Path] = None
        base_rows: List[dict] = []

        if skip_run and xml_file.exists():
            log_write(f"[SKIP] jacoco.xml already exists: {xml_file}\n")
        else:
            trigger_tests = get_trigger_tests(buggy_dir)
            if single_add_test_id is not None:
                add_tests = [single_add_test_id]
            else:
                add_tests = get_add_test_ids(results_bug_dir) if results_bug_dir and results_bug_dir.is_dir() else []
            all_tests = trigger_tests + add_tests
            log_write(f"[JACOCO] trigger={len(trigger_tests)} add={len(add_tests)} total={len(all_tests)}\n")
            if not all_tests:
                log_write("[JACOCO] no tests found, skipping\n")
                return False, "no tests", [], []
            log_write("[JACOCO] starting coverage collection (defects4j test -t per test)...\n")
            # Snapshot base after trigger tests whenever there are add_tests
            tc = len(trigger_tests) if add_tests else 0
            try:
                _, base_xml_file = run_jacoco_for_dir(
                    buggy_dir, jacoco_home, all_tests, log_write, jacoco_timeout,
                    trigger_count=tc,
                )
                log_write("[JACOCO] done.\n")
            except TimeoutError as e:
                msg = str(e).replace("\n", " | ")
                log_write(f"[JACOCO] TIMEOUT: {msg}\n")
                nc_rows = make_not_compilable_rows()
                log_write(f"[NOT_COMPILABLE] timeout -> writing {len(nc_rows)} rows.\n")
                return False, msg, nc_rows, []
            except Exception as e:
                msg = str(e).replace("\n", " | ")
                log_write(f"[JACOCO] FAILED: {msg}\n")
                if not xml_file.exists():
                    nc_rows = make_not_compilable_rows()
                    log_write(f"[NOT_COMPILABLE] writing {len(nc_rows)} rows.\n")
                    return False, msg, nc_rows, []

        # parse combined XML (add coverage)
        xml_path = str(xml_file)
        if xml_path not in jacoco_cache:
            m, err = safe_parse_jacoco_xml(xml_path)
            if err:
                log_write(f"[PARSE] error: {err}\n")
                nc_rows = make_not_compilable_rows()
                log_write(f"[NOT_COMPILABLE] writing {len(nc_rows)} rows.\n")
                return False, err, nc_rows, []
            jacoco_cache[xml_path] = m

        jacoco_map = jacoco_cache[xml_path]

        # parse base XML (trigger-only coverage from same dir) if available
        base_jacoco_map = None
        if base_xml_file and base_xml_file.exists():
            bm, berr = safe_parse_jacoco_xml(str(base_xml_file))
            if not berr:
                base_jacoco_map = bm
                log_write(f"[PARSE] base XML parsed: {base_xml_file.name}\n")
            else:
                log_write(f"[PARSE] base XML error: {berr}\n")

        for r in sorted_methods:
            project     = r.get("project", "")
            class_entry = r.get("class_entry", "").strip()
            file_path   = r.get("file", "").strip()
            method_sig  = r.get("method_sig_snippet", "").strip()
            start       = norm_int(r.get("buggy_method_start_line", "0"))
            end         = norm_int(r.get("buggy_method_end_line", "0"))
            function_id = r.get("_function_id", "function_1")

            sf_name   = os.path.basename(file_path)
            pkg_guess = pkg_from_class_entry(class_entry)

            for line in range(start, end + 1):
                line_in_method = (line - start) + 1
                v = lookup_line(jacoco_map, sf_name, pkg_guess, line)
                if v is None:
                    row = {
                        "project": project, "bug_id": bug_id,
                        "folder": buggy_dir.name,
                        "function_id": function_id,
                        "class_fqcn": class_entry, "file": file_path,
                        "method_sig_snippet": method_sig,
                        "method_start": start, "method_end": end,
                        "line": line, "line_in_method": line_in_method,
                        "ci": "", "mi": "", "cb": "", "mb": "", "executed": "",
                    }
                    if add_test_file_label:
                        row["add_test_file"] = add_test_file_label
                    output_rows.append(row)
                else:
                    ci, mi, cb, mb = v
                    row = {
                        "project": project, "bug_id": bug_id,
                        "folder": buggy_dir.name,
                        "function_id": function_id,
                        "class_fqcn": class_entry, "file": file_path,
                        "method_sig_snippet": method_sig,
                        "method_start": start, "method_end": end,
                        "line": line, "line_in_method": line_in_method,
                        "ci": ci, "mi": mi, "cb": cb, "mb": mb,
                        "executed": (ci > 0) or (cb > 0),
                    }
                    if add_test_file_label:
                        row["add_test_file"] = add_test_file_label
                    output_rows.append(row)

        log_write(f"[PARSE] {len(output_rows)} line rows.\n")

        # Generate base rows from base_jacoco_map (trigger-only, same dir)
        base_rows: List[dict] = []
        if base_jacoco_map is not None:
            for r in sorted_methods:
                project_    = r.get("project", "")
                class_entry = r.get("class_entry", "").strip()
                file_path   = r.get("file", "").strip()
                method_sig  = r.get("method_sig_snippet", "").strip()
                start       = norm_int(r.get("buggy_method_start_line", "0"))
                end         = norm_int(r.get("buggy_method_end_line", "0"))
                function_id = r.get("_function_id", "function_1")
                sf_name     = os.path.basename(file_path)
                pkg_guess   = pkg_from_class_entry(class_entry)

                for line in range(start, end + 1):
                    line_in_method = (line - start) + 1
                    v = lookup_line(base_jacoco_map, sf_name, pkg_guess, line)
                    if v is None:
                        brow = {
                            "project": project_, "bug_id": bug_id,
                            "folder": buggy_dir.name,
                            "function_id": function_id,
                            "class_fqcn": class_entry, "file": file_path,
                            "method_sig_snippet": method_sig,
                            "method_start": start, "method_end": end,
                            "line": line, "line_in_method": line_in_method,
                            "ci": "", "mi": "", "cb": "", "mb": "", "executed": "",
                        }
                    else:
                        ci, mi, cb, mb = v
                        brow = {
                            "project": project_, "bug_id": bug_id,
                            "folder": buggy_dir.name,
                            "function_id": function_id,
                            "class_fqcn": class_entry, "file": file_path,
                            "method_sig_snippet": method_sig,
                            "method_start": start, "method_end": end,
                            "line": line, "line_in_method": line_in_method,
                            "ci": ci, "mi": mi, "cb": cb, "mb": mb,
                            "executed": (ci > 0) or (cb > 0),
                        }
                    if add_test_file_label:
                        brow["add_test_file"] = add_test_file_label
                    base_rows.append(brow)
            log_write(f"[PARSE] {len(base_rows)} base line rows.\n")

    return True, "", output_rows, base_rows


# ===================== per-project processing =====================

def process_project(
    project: str,
    defects_root: Path,
    methods_csv: Path,
    out_dir: Path,
    log_dir: Path,
    jacoco_home: Path,
    ids_filter: Optional[set],
    jacoco_timeout: int,
    jobs: int,
    skip_run: bool,
    results_root: Optional[Path] = None,
    ind_out_dir: Optional[Path] = None,
    ind_base_out_dir: Optional[Path] = None,
    base_out_dir: Optional[Path] = None,
) -> None:
    # find project dir under defects_root (case-insensitive)
    buggy_proj_dir: Optional[Path] = None
    for d in defects_root.iterdir():
        if d.is_dir() and d.name.lower() == project.lower():
            buggy_proj_dir = d
            break
    if buggy_proj_dir is None:
        print(f"[WARN] no dir found for '{project}' under {defects_root}", flush=True)
        return

    # read methods CSV
    with open(methods_csv, newline="", encoding="utf-8") as f:
        all_rows = list(csv.DictReader(f))

    groups: Dict[str, List[dict]] = defaultdict(list)
    for r in all_rows:
        groups[r["bug_id"].strip()].append(r)

    if ids_filter is not None:
        groups = {bid: rows for bid, rows in groups.items() if int(bid) in ids_filter}

    out_path  = out_dir / f"{project}.csv"
    err_path  = out_dir / "errors.csv"

    with open(out_path, "w", newline="", encoding="utf-8") as f:
        csv.DictWriter(f, fieldnames=FIELDNAMES).writeheader()

    if base_out_dir:
        base_out_dir.mkdir(parents=True, exist_ok=True)
        base_path_init = base_out_dir / f"{project}.csv"
        with open(base_path_init, "w", newline="", encoding="utf-8") as f:
            csv.DictWriter(f, fieldnames=FIELDNAMES).writeheader()

    err_exists = err_path.exists()
    with open(err_path, "a", newline="", encoding="utf-8") as f:
        if not err_exists:
            csv.writer(f).writerow(["project", "bug_id", "folder", "error"])

    jacoco_cache: dict = {}

    def handle_one(bug_id: str, methods: List[dict]):
        sample_folder = methods[0].get("folder", "").strip()
        # folder field is already <Project>-<id>-buggy
        buggy_dir = buggy_proj_dir / sample_folder
        if not buggy_dir.is_dir():
            # fallback: construct from project name + bug_id
            alt = buggy_proj_dir / f"{buggy_proj_dir.name}-{bug_id}-buggy"
            if alt.is_dir():
                buggy_dir = alt
            else:
                print(f"  [SKIP] buggy dir not found: {buggy_dir}", flush=True)
                with open(err_path, "a", newline="", encoding="utf-8") as f:
                    csv.writer(f).writerow([project, bug_id, sample_folder, "buggy dir not found"])
                return

        print(f"  [{project}-{bug_id}] {buggy_dir.name}", flush=True)
        results_bug_dir = (results_root / project / bug_id) if results_root else None
        success, err_msg, rows, base_rows = process_one_bug(
            buggy_dir=buggy_dir,
            bug_id=bug_id,
            methods=methods,
            jacoco_home=jacoco_home,
            log_dir=log_dir / project,
            jacoco_timeout=jacoco_timeout,
            jacoco_cache=jacoco_cache,
            skip_run=skip_run,
            results_bug_dir=results_bug_dir,
        )

        if not success or err_msg:
            with open(err_path, "a", newline="", encoding="utf-8") as f:
                csv.writer(f).writerow([project, bug_id, buggy_dir.name, err_msg])
        if rows:
            with open(out_path, "a", newline="", encoding="utf-8") as f:
                csv.DictWriter(f, fieldnames=FIELDNAMES).writerows(rows)
        if base_rows and base_out_dir:
            base_path = base_out_dir / f"{project}.csv"
            with open(base_path, "a", newline="", encoding="utf-8") as f:
                csv.DictWriter(f, fieldnames=FIELDNAMES).writerows(base_rows)

    sorted_bug_ids = sorted(groups.keys(), key=lambda x: int(x))

    if jobs > 1:
        def handle_parallel(bug_id):
            local_cache: dict = {}
            methods = groups[bug_id]
            sample_folder = methods[0].get("folder", "").strip()
            buggy_dir = buggy_proj_dir / sample_folder
            if not buggy_dir.is_dir():
                alt = buggy_proj_dir / f"{buggy_proj_dir.name}-{bug_id}-buggy"
                buggy_dir = alt if alt.is_dir() else None
            if buggy_dir is None or not buggy_dir.is_dir():
                return bug_id, False, "buggy dir not found", []
            print(f"  [{project}-{bug_id}] {buggy_dir.name}", flush=True)
            results_bug_dir = (results_root / project / bug_id) if results_root else None
            success, err_msg, rows, base_rows = process_one_bug(
                buggy_dir=buggy_dir, bug_id=bug_id, methods=methods,
                jacoco_home=jacoco_home,
                log_dir=log_dir / project, jacoco_timeout=jacoco_timeout,
                jacoco_cache=local_cache, skip_run=skip_run,
                results_bug_dir=results_bug_dir,
            )
            return bug_id, success, err_msg, rows, base_rows

        with ThreadPoolExecutor(max_workers=jobs) as ex:
            futures = {ex.submit(handle_parallel, bid): bid for bid in sorted_bug_ids}
            for fut in as_completed(futures):
                bug_id, success, err_msg, rows, base_rows = fut.result()
                if not success or err_msg:
                    with open(err_path, "a", newline="", encoding="utf-8") as f:
                        csv.writer(f).writerow([project, bug_id, "", err_msg])
                if rows:
                    with open(out_path, "a", newline="", encoding="utf-8") as f:
                        csv.DictWriter(f, fieldnames=FIELDNAMES).writerows(rows)
                if base_rows and base_out_dir:
                    base_path = base_out_dir / f"{project}.csv"
                    with open(base_path, "a", newline="", encoding="utf-8") as f:
                        csv.DictWriter(f, fieldnames=FIELDNAMES).writerows(base_rows)
    else:
        for bug_id in sorted_bug_ids:
            handle_one(bug_id, groups[bug_id])

    print(f"[OK] {project} -> {out_path}", flush=True)

    # ---- Process individual copy dirs ----
    if ind_out_dir is None:
        return
    ind_out_dir.mkdir(parents=True, exist_ok=True)
    ind_out_path = ind_out_dir / f"{project}.csv"
    ind_err_path = ind_out_dir / "errors.csv"

    with open(ind_out_path, "w", newline="", encoding="utf-8") as f:
        csv.DictWriter(f, fieldnames=FIELDNAMES_INDIVIDUAL).writeheader()

    if ind_base_out_dir:
        ind_base_out_dir.mkdir(parents=True, exist_ok=True)
        ind_base_path_init = ind_base_out_dir / f"{project}.csv"
        with open(ind_base_path_init, "w", newline="", encoding="utf-8") as f:
            csv.DictWriter(f, fieldnames=FIELDNAMES_INDIVIDUAL).writeheader()

    ind_err_exists = ind_err_path.exists()
    with open(ind_err_path, "a", newline="", encoding="utf-8") as f:
        if not ind_err_exists:
            csv.writer(f).writerow(["project", "bug_id", "folder", "error"])

    # Scan for individual copy dirs: <Project>-<id>-buggy_ind<k>
    for ind_dir in sorted(buggy_proj_dir.iterdir()):
        m = IND_BUGGY_RE.match(ind_dir.name)
        if not m or m.group(1) != buggy_proj_dir.name or not ind_dir.is_dir():
            continue
        ind_bug_id = m.group(2)
        if ids_filter is not None and int(ind_bug_id) not in ids_filter:
            continue
        # Read which add_test this copy corresponds to
        add_test_id_path = ind_dir / ADD_TEST_ID_FILE
        if not add_test_id_path.exists():
            print(f"  [SKIP-IND] no {ADD_TEST_ID_FILE} in {ind_dir.name}", flush=True)
            continue
        add_test_name = add_test_id_path.read_text(encoding="utf-8").strip()
        # Get FQCN::method for this add_test
        results_bug_dir = (results_root / project / ind_bug_id) if results_root else None
        add_test_ids = get_add_test_ids(results_bug_dir, only_file=add_test_name) if results_bug_dir else []
        single_id = add_test_ids[0] if add_test_ids else None
        # Get methods for this bug_id
        methods = groups.get(str(ind_bug_id), [])
        if not methods:
            print(f"  [SKIP-IND] no methods found for bug_id={ind_bug_id}", flush=True)
            continue
        print(f"  [IND] {project}-{ind_bug_id} {ind_dir.name} add_test={add_test_name}", flush=True)
        ind_cache: dict = {}
        _success, _err_msg, ind_rows, ind_base_rows = process_one_bug(
            buggy_dir=ind_dir,
            bug_id=ind_bug_id,
            methods=[dict(r) for r in methods],
            jacoco_home=jacoco_home,
            log_dir=log_dir / project,
            jacoco_timeout=jacoco_timeout,
            jacoco_cache=ind_cache,
            skip_run=skip_run,
            results_bug_dir=results_bug_dir,
            single_add_test_id=single_id,
            add_test_file_label=add_test_name,
        )
        if not _success or _err_msg:
            with open(ind_err_path, "a", newline="", encoding="utf-8") as f:
                csv.writer(f).writerow([project, ind_bug_id, ind_dir.name, _err_msg])
        if ind_rows:
            with open(ind_out_path, "a", newline="", encoding="utf-8") as f:
                csv.DictWriter(f, fieldnames=FIELDNAMES_INDIVIDUAL).writerows(ind_rows)
        if ind_base_rows and ind_base_out_dir:
            ind_base_path = ind_base_out_dir / f"{project}.csv"
            with open(ind_base_path, "a", newline="", encoding="utf-8") as f:
                csv.DictWriter(f, fieldnames=FIELDNAMES_INDIVIDUAL).writerows(ind_base_rows)

    print(f"[OK-IND] {project} -> {ind_out_path}", flush=True)


# ===================== main =====================

def main():
    ap = argparse.ArgumentParser(
        description="Run JaCoCo on defects4j_checkouts buggy dirs using generated test cases "
                    "and parse per-line coverage for the buggy method."
    )
    ap.add_argument(
        "--defects-root", default=str(Path(__file__).resolve().parents[2] / "defects4j_checkouts"),
        help="Root containing <project>/<project>-<id>-buggy/ (default: defects4j_checkouts)",
    )
    ap.add_argument(
        "--results-root", default=str(Path(__file__).resolve().parents[2] / "results/data/exm3/Claude"),
        help="Root containing <project>/<id>/add_test*.java (default: results/data/exm3/Claude)",
    )
    ap.add_argument(
        "--methods-dir", default=str(Path(__file__).resolve().parent / "bug_methods"),
        help="Dir containing *_methods.csv (default: <script dir>/bug_methods)",
    )
    ap.add_argument(
        "--out-dir", default=str(Path(__file__).resolve().parents[2] / "generated_evaluation/exm3_work/Claude/coverage_buggy"),
        help="Output dir for combined per-project CSVs (default: generated_evaluation/exm3_work/Claude/coverage_buggy)",
    )
    ap.add_argument(
        "--ind-out-dir", default=str(Path(__file__).resolve().parents[2] / "generated_evaluation/exm3_work/Claude/coverage_buggy_individual"),
        help="Output dir for individual per-project CSVs with add_test_file column "
             "(default: generated_evaluation/exm3_work/Claude/coverage_buggy_individual)",
    )
    ap.add_argument(
        "--ind-base-out-dir", default=str(Path(__file__).resolve().parents[2] / "generated_evaluation/exm3_work/Claude/base_coverage_buggy_individual"),
        help="Output dir for individual base (trigger-only) coverage CSVs "
             "(default: generated_evaluation/exm3_work/Claude/base_coverage_buggy_individual)",
    )
    ap.add_argument(
        "--base-out-dir", default=str(Path(__file__).resolve().parents[2] / "generated_evaluation/exm3_work/Claude/base_coverage_buggy"),
        help="Output dir for combined base (trigger-only) coverage CSVs "
             "(default: generated_evaluation/exm3_work/Claude/base_coverage_buggy)",
    )
    ap.add_argument(
        "--log-dir", default="test-logs/exm3/buggy_jacoco",
        help="Dir for per-bug JaCoCo log files (default: test-logs/exm3/buggy_jacoco)",
    )
    ap.add_argument(
        "--project", default="all",
        help="Only process this project, e.g. Chart. Use 'all' for all (default).",
    )
    ap.add_argument(
        "--ids", type=str, default=None,
        help="Only process these bug IDs, e.g. '1,3,5-7'.",
    )
    ap.add_argument("-j", "--jobs", type=int, default=1, help="Parallel workers (default: 1).")
    ap.add_argument(
        "--jacoco-timeout", type=int, default=JACOCO_TIMEOUT,
        help=f"JaCoCo phase timeout in seconds per bug (default: {JACOCO_TIMEOUT}).",
    )
    ap.add_argument(
        "--skip-run", action="store_true",
        help="Skip JaCoCo execution if jacoco.xml already exists (parse only).",
    )
    args = ap.parse_args()
    defects_root = Path(args.defects_root).expanduser().resolve()
    methods_dir  = Path(args.methods_dir).expanduser().resolve()
    out_dir      = Path(args.out_dir).expanduser().resolve()
    ind_out_dir  = Path(args.ind_out_dir).expanduser().resolve()
    ind_base_out_dir = Path(args.ind_base_out_dir).expanduser().resolve()
    base_out_dir = Path(args.base_out_dir).expanduser().resolve()
    log_dir      = Path(args.log_dir).expanduser().resolve()
    results_root = Path(args.results_root).expanduser().resolve()

    if not defects_root.is_dir():
        sys.exit(f"[ERROR] defects-root not found: {defects_root}")

    out_dir.mkdir(parents=True, exist_ok=True)
    log_dir.mkdir(parents=True, exist_ok=True)

    jacoco_home = ensure_jacoco_home()

    ids_filter: Optional[set] = None
    if args.ids:
        ids_filter = parse_id_expr(args.ids)

    project_filter = (args.project or "all").strip().lower()

    methods_csvs = sorted(methods_dir.glob("*_methods.csv"))
    if not methods_csvs:
        sys.exit(f"No *_methods.csv found in: {methods_dir}")

    print(f"defects-root : {defects_root}")
    print(f"methods-dir  : {methods_dir}")
    print(f"out-dir      : {out_dir}")
    print(f"log-dir      : {log_dir}")
    print(f"project      : {args.project}")
    print(f"ids          : {args.ids or 'all'}")
    print(f"jobs         : {args.jobs}")
    print(f"jacoco-home  : {jacoco_home}")
    print(f"skip-run     : {args.skip_run}")
    print(f"results-root : {results_root}")
    print()

    for csv_path in methods_csvs:
        stem = csv_path.stem.replace("_methods", "")
        project = stem[0].upper() + stem[1:]

        if project_filter != "all" and project.lower() != project_filter:
            continue

        print(f"=== PROJECT: {project} ===", flush=True)
        process_project(
            project=project,
            defects_root=defects_root,
            methods_csv=csv_path,
            out_dir=out_dir,
            log_dir=log_dir,
            jacoco_home=jacoco_home,
            ids_filter=ids_filter,
            jacoco_timeout=args.jacoco_timeout,
            jobs=args.jobs,
            skip_run=args.skip_run,
            results_root=results_root,
            ind_out_dir=ind_out_dir,
            ind_base_out_dir=ind_base_out_dir,
            base_out_dir=base_out_dir,
        )

    print("\nDone.")


if __name__ == "__main__":
    main()
