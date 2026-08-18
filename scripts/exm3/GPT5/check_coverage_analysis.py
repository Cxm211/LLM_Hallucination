import os
import csv

base = "/Users/grace/Documents/Paper/BugFixing/Interpretation/results/exm3/GPT5"
projects = ["Chart","Cli","Closure","Codec","Collections","Compress","Csv",
            "Gson","JacksonCore","JacksonDatabind","JacksonXml","Jsoup",
            "JxPath","Lang","Math","Mockito","Time"]
csv_name_map = {
    "JacksonCore": "Jacksoncore.csv",
    "JacksonDatabind": "Jacksondatabind.csv",
    "JacksonXml": "Jacksonxml.csv",
    "JxPath": "Jxpath.csv",
}

# Step 1: Find all project/bug_id combos with NO add_test*.java files
no_add_test = []
all_dirs = []

for project in projects:
    proj_dir = os.path.join(base, project)
    if not os.path.isdir(proj_dir):
        continue
    for bug_id in sorted(os.listdir(proj_dir)):
        bug_dir = os.path.join(proj_dir, bug_id)
        if not os.path.isdir(bug_dir):
            continue
        all_dirs.append((project, bug_id))
        files = os.listdir(bug_dir)
        has_add = any(f.startswith("add_test") and f.endswith(".java") for f in files)
        if not has_add:
            no_add_test.append((project, bug_id))

print(f"Total bug dirs: {len(all_dirs)}")
print(f"Combos WITH add_test*.java: {len(all_dirs) - len(no_add_test)}")
print(f"Combos WITHOUT add_test*.java: {len(no_add_test)}")

# Step 2: Load jacoco data
jacoco_coverage = {}

for project in projects:
    csv_file = csv_name_map.get(project, f"{project}.csv")
    csv_path = os.path.join(base, "z_buggy_jacoco", csv_file)
    if not os.path.exists(csv_path):
        print(f"WARNING: CSV not found: {csv_path}")
        continue
    with open(csv_path, newline='', encoding='utf-8') as f:
        reader = csv.reader(f)
        header = next(reader)
        exec_idx = header.index('executed') if 'executed' in header else -1
        bid_idx = header.index('bug_id')
        for row in reader:
            if not row:
                continue
            try:
                bug_id = row[bid_idx].strip()
            except IndexError:
                continue
            key = (project, bug_id)
            try:
                ev = row[exec_idx].strip() if exec_idx != -1 else row[-1].strip()
            except IndexError:
                ev = row[-1].strip()
            if ev == 'False':
                jacoco_coverage[key] = False
            elif ev == 'True':
                if key not in jacoco_coverage:
                    jacoco_coverage[key] = True

# Step 3: Analyze
all_100pct = []
not_100pct = []
no_jacoco = []

for project, bug_id in no_add_test:
    key = (project, bug_id)
    if key not in jacoco_coverage:
        no_jacoco.append(key)
    elif jacoco_coverage[key] is True:
        all_100pct.append(key)
    else:
        not_100pct.append(key)

print(f"\nOf the {len(no_add_test)} combos with no add_test*.java:")
print(f"  100% coverage (all executed=True):  {len(all_100pct)}")
print(f"  NOT 100% (some executed=False):     {len(not_100pct)}")
print(f"  No jacoco data:                     {len(no_jacoco)}")

print(f"\n=== 100% buggy coverage ({len(all_100pct)}) ===")
for p, b in sorted(all_100pct, key=lambda x: (x[0], int(x[1]) if x[1].isdigit() else x[1])):
    print(f"  {p}/{b}")

print(f"\n=== No jacoco data ({len(no_jacoco)}) ===")
for p, b in sorted(no_jacoco, key=lambda x: (x[0], int(x[1]) if x[1].isdigit() else x[1])):
    print(f"  {p}/{b}")

print(f"\n=== NOT 100% coverage ({len(not_100pct)}) ===")
for p, b in sorted(not_100pct, key=lambda x: (x[0], int(x[1]) if x[1].isdigit() else x[1])):
    print(f"  {p}/{b}")
