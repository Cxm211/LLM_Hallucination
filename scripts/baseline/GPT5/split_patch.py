#!/usr/bin/env python3
"""
Split patch.java into per-function files.

Usage:
    python split_patch.py <project> <bugid>
    python split_patch.py Closure 98_1     # single variant
    python split_patch.py Closure 79       # processes 79_1, 79_2, ... automatically

If <bugid> has no underscore and no matching directory exists, the script
looks for all directories named <bugid>_<N> (N = 1, 2, 3, ...) and processes
each one in numeric order.

Output per directory:
    patch.java  -> first function
    patch1.java -> second function
    patch2.java -> third function
    ...
"""

import sys
import os
import re


def strip_line_comment(line):
    """Remove // line comment, respecting string literals."""
    in_string = False
    i = 0
    while i < len(line):
        c = line[i]
        if c == '"' and (i == 0 or line[i-1] != '\\'):
            in_string = not in_string
        elif not in_string and c == '/' and i + 1 < len(line) and line[i+1] == '/':
            return line[:i]
        i += 1
    return line


def count_braces(line):
    """Count net brace depth change in a line, ignoring strings and comments."""
    line = strip_line_comment(line)
    depth = 0
    in_string = False
    in_char = False
    i = 0
    while i < len(line):
        c = line[i]
        if in_string:
            if c == '\\':
                i += 2
                continue
            if c == '"':
                in_string = False
        elif in_char:
            if c == '\\':
                i += 2
                continue
            if c == "'":
                in_char = False
        else:
            if c == '"':
                in_string = True
            elif c == "'":
                in_char = True
            elif c == '{':
                depth += 1
            elif c == '}':
                depth -= 1
        i += 1
    return depth


def split_functions(content):
    """Split Java content into a list of function strings."""
    functions = []
    current_lines = []
    brace_depth = 0
    in_block_comment = False

    for raw_line in content.splitlines():
        stripped = raw_line.strip()

        # Track block comments to avoid miscounting braces inside /* ... */
        if in_block_comment:
            current_lines.append(raw_line)
            if '*/' in raw_line:
                in_block_comment = False
            continue

        if '/*' in raw_line and '*/' not in raw_line:
            in_block_comment = True

        # Blank line at depth 0 -> function boundary
        if brace_depth == 0 and not stripped:
            if current_lines:
                # Trim trailing blank lines from the accumulated block
                while current_lines and not current_lines[-1].strip():
                    current_lines.pop()
                if current_lines:
                    functions.append('\n'.join(current_lines))
                current_lines = []
            continue

        current_lines.append(raw_line)
        brace_depth += count_braces(raw_line)

    # Flush any remaining content
    while current_lines and not current_lines[-1].strip():
        current_lines.pop()
    if current_lines:
        functions.append('\n'.join(current_lines))

    return functions


def output_filename(index):
    """patch.java for index 0, patch1.java for index 1, etc."""
    if index == 0:
        return 'patch.java'
    return f'patch{index}.java'


def find_variant_dirs(project_dir, base_id):
    """
    Return all directories matching <base_id>_<N> sorted by N numerically.
    E.g. base_id='79' -> ['79_1', '79_2', ..., '79_18']
    """
    pattern = re.compile(r'^' + re.escape(base_id) + r'_(\d+)$')
    matches = []
    for name in os.listdir(project_dir):
        m = pattern.match(name)
        if m and os.path.isdir(os.path.join(project_dir, name)):
            matches.append((int(m.group(1)), name))
    matches.sort()
    return [name for _, name in matches]


def process_bug_dir(bug_dir):
    """Split patch.java in bug_dir into per-function files. Returns True on success."""
    patch_path = os.path.join(bug_dir, 'patch.java')

    if not os.path.isfile(patch_path):
        print(f'  SKIP: patch.java not found in {bug_dir}')
        return False

    with open(patch_path, 'r', encoding='utf-8') as f:
        content = f.read()

    functions = split_functions(content)

    if not functions:
        print(f'  SKIP: no functions found in {patch_path}')
        return False

    if len(functions) == 1:
        return True

    print(f'  Found {len(functions)} function(s) -> splitting {patch_path}')
    for idx, func_content in enumerate(functions):
        out_path = os.path.join(bug_dir, output_filename(idx))
        with open(out_path, 'w', encoding='utf-8') as f:
            f.write(func_content + '\n')
        print(f'    Written: {output_filename(idx)}')
    return True


def main():
    if len(sys.argv) != 3:
        print(f'Usage: {sys.argv[0]} <project> <bugid>')
        print(f'  Single variant:   {sys.argv[0]} Closure 98_1')
        print(f'  All variants:     {sys.argv[0]} Closure 79   (runs 79_1, 79_2, ...)')
        sys.exit(1)

    project = sys.argv[1]
    bugid = sys.argv[2]

    base_dir = os.path.dirname(os.path.abspath(__file__))
    project_dir = os.path.join(base_dir, project)

    if not os.path.isdir(project_dir):
        print(f'Error: project directory not found: {project_dir}')
        sys.exit(1)

    bug_dir = os.path.join(project_dir, bugid)

    # bugid with underscore (e.g. 98_1) -> process that single directory
    if '_' in bugid:
        if not os.path.isdir(bug_dir):
            print(f'Error: directory not found: {bug_dir}')
            sys.exit(1)
        print(f'Processing {project}/{bugid}')
        process_bug_dir(bug_dir)
        print('Done.')
        return

    # bugid without underscore (e.g. 79) -> process base dir + all <bugid>_N variants
    variants = find_variant_dirs(project_dir, bugid)
    all_ids = ([bugid] if os.path.isdir(bug_dir) else []) + variants

    if not all_ids:
        print(f'Error: no directory "{bugid}" or "{bugid}_N" found under {project_dir}')
        sys.exit(1)

    print(f'Found {len(all_ids)} target(s) for {project}/{bugid}: {", ".join(all_ids)}')
    for v in all_ids:
        print(f'\n[{project}/{v}]')
        process_bug_dir(os.path.join(project_dir, v))

    print('\nAll done.')


if __name__ == '__main__':
    main()
