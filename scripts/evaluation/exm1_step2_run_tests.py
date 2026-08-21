#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Run the developer-written test suite over the patched Defects4J checkouts.

For every <BASE>-*-buggy directory under --bug-root it runs `ant clean` (console only), then
`defects4j test -r`, and records the output in <logs-root>/<BASE>-<variant>-buggy.log. A bug
split into slices produces one directory, and one log, per slice.

A run exceeding TIMEOUT_SECONDS is killed and recorded with exit code 124, matching GNU
timeout; the next directory is then processed as usual.

Example:
  python3 exm1_step2_run_tests.py \
    --bug-root defects4j_checkouts/Csv \
    --logs-root test-logs/exm1/Claude \
    --ids 1,3,5-7
"""

import argparse
import os
import signal
import subprocess
import sys
import threading
import time
import shlex
import re
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor, as_completed

TIMEOUT_SECONDS = 600  # 10 minutes


def parse_id_expr(expr: str):
    """Expand an expression such as 1,3,5-7 into {1, 3, 5, 6, 7}."""
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
            ids.update(range(a, b + 1))
        else:
            ids.add(int(part))
    return ids


def find_buggy_dirs(bug_root: Path):
    """Find the <BASE>-<n>-buggy directories under bug_root."""
    if not bug_root.is_dir():
        print(f"[error] directory not found: {bug_root}", file=sys.stderr)
        sys.exit(1)

    base = bug_root.name
    candidates = [d for d in bug_root.iterdir() if d.is_dir()]

    def is_target(d: Path):
        parts = d.name.split("-")
        return len(parts) >= 3 and parts[0] == base and parts[-1] == "buggy"

    def key(d: Path):
        parts = d.name.split("-")
        try:
            n = int(parts[1].split("_")[0])
        except Exception:
            n = 10**9
        return (n, d.name)

    return sorted([d for d in candidates if is_target(d)], key=key)


def run_one(dir_path: Path, cmd: str, log_dir: Path) -> tuple[str, int]:
    """Run ant clean followed by defects4j test in one directory, with a timeout."""
    log_dir.mkdir(parents=True, exist_ok=True)
    log_file = log_dir / f"{dir_path.name}.log"

    header = f"==== {time.strftime('%F %T')} :: {dir_path.name} ====\n"
    with open(log_file, "w", encoding="utf-8", buffering=1) as lf:
        lf.write(header)
        print(header, end="")

        # ant clean, console only
        print(">>> pre-step: running `ant clean` (console only)")
        try:
            clean_proc = subprocess.Popen(
                ["ant", "clean"],
                cwd=dir_path,
                stdout=None,
                stderr=None,
                text=True,
            )
            clean_rc = clean_proc.wait()
            lf.write(f">>> ant clean exit code: {clean_rc}\n")
        except Exception as e:
            msg = f"[warn] ant clean failed: {e}\n"
            lf.write(msg)
            print(msg, end="")

        lf.write(f"\n>>> running main command: {cmd}\n")
        print(f"\n>>> running main command: {cmd}")

        timed_out = False
        rc = 1

        try:
            proc = subprocess.Popen(
                shlex.split(cmd),
                cwd=dir_path,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                text=True,
                bufsize=1,
                preexec_fn=os.setsid,
            )
            assert proc.stdout is not None

            def _reader():
                for line in proc.stdout:
                    lf.write(line)
                    print(line, end="")

            reader_thread = threading.Thread(target=_reader, daemon=True)
            reader_thread.start()

            try:
                proc.wait(timeout=TIMEOUT_SECONDS)
            except subprocess.TimeoutExpired:
                timed_out = True
                msg = f"\n[timeout] exceeded {TIMEOUT_SECONDS}s, process terminated.\n"
                lf.write(msg)
                print(msg, end="")
                try:
                    os.killpg(os.getpgid(proc.pid), signal.SIGKILL)
                except ProcessLookupError:
                    pass

            reader_thread.join(timeout=5)

            if not timed_out:
                rc = proc.returncode
            else:
                rc = 124  # same convention as GNU timeout

        except FileNotFoundError as e:
            msg = f"[error] command not found: {e}\n"
            lf.write(msg)
            print(msg, end="")
            rc = 127
        except Exception as e:
            msg = f"[error] unexpected exception: {e}\n"
            lf.write(msg)
            print(msg, end="")
            rc = 1

    footer = f"---- exit code: {rc} | {dir_path.name}\n\n"
    with open(log_file, "a", encoding="utf-8") as lf:
        lf.write(footer)
    print(footer, end="")
    return (dir_path.name, rc)


def main():
    parser = argparse.ArgumentParser(
        description="Run defects4j test -r in each *-buggy directory, with a timeout"
    )
    parser.add_argument("--bug-root", default="defects4j-GPT4o/Chart")
    parser.add_argument("--cmd", default="defects4j test -r")
    parser.add_argument("--logs-root", default="test-logs/exm1/GPT5")
    parser.add_argument("--log-dir", default=None)
    parser.add_argument("-j", "--jobs", type=int, default=1)
    parser.add_argument("--ids", type=str)

    args = parser.parse_args()

    script_dir = Path(__file__).resolve().parent
    bug_root = (script_dir / args.bug_root).resolve()

    if args.log_dir:
        log_dir = (script_dir / args.log_dir).resolve()
    else:
        log_dir = (script_dir / args.logs_root / bug_root.name).resolve()

    buggy_dirs = find_buggy_dirs(bug_root)

    if args.ids:
        selected = parse_id_expr(args.ids)
        buggy_dirs = [
            d for d in buggy_dirs
            if d.name.split("-")[1].split("_")[0] in {str(i) for i in selected}
        ]

    print(f"working dir : {bug_root}")
    print(f"log dir     : {log_dir}")
    print(f"directories : {len(buggy_dirs)}")
    print(f"timeout     : {TIMEOUT_SECONDS}s\n")

    results = []
    if args.jobs > 1:
        with ThreadPoolExecutor(max_workers=args.jobs) as ex:
            futs = {ex.submit(run_one, d, args.cmd, log_dir): d for d in buggy_dirs}
            for f in as_completed(futs):
                results.append(f.result())
    else:
        for d in buggy_dirs:
            results.append(run_one(d, args.cmd, log_dir))

    results.sort(key=lambda x: x[0])
    fails = sum(1 for _, rc in results if rc != 0)
    timeouts = sum(1 for _, rc in results if rc == 124)

    print("\n=== summary ===")
    for name, rc in results:
        print(f"{name:20s} -> {rc}")
    print(f"\ndone: {len(results)} directories | failed {fails} | timed out {timeouts}")
    print(f"log dir     : {log_dir}")


if __name__ == "__main__":
    main()
