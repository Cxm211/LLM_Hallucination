import os, csv

base = "/Users/grace/Documents/Paper/BugFixing/Interpretation/results/exm3/GPT5"
jacoco_dir = os.path.join(base, "z_buggy_jacoco")

proj_to_csv = {
    "JacksonCore": "Jacksoncore.csv",
    "JacksonDatabind": "Jacksondatabind.csv",
    "JacksonXml": "Jacksonxml.csv",
    "JxPath": "Jxpath.csv",
}

projs = ["Chart","Cli","Closure","Codec","Collections","Compress","Csv","Gson",
         "JacksonCore","JacksonDatabind","JacksonXml","Jsoup","JxPath","Lang","Math","Mockito","Time"]

no_test = []
for proj in projs:
    proj_path = os.path.join(base, proj)
    if not os.path.isdir(proj_path): continue
    for bug_id in sorted(os.listdir(proj_path), key=lambda x: int(x) if x.isdigit() else 0):
        id_path = os.path.join(proj_path, bug_id)
        if not os.path.isdir(id_path): continue
        add_tests = [f for f in os.listdir(id_path) if f.startswith('add_test') and f.endswith('.java')]
        if not add_tests:
            no_test.append((proj, bug_id))

hundred, not_hundred, no_data = [], [], []
for proj, bug_id in no_test:
    csv_name = proj_to_csv.get(proj, f"{proj}.csv")
    csv_file = os.path.join(jacoco_dir, csv_name)
    if not os.path.exists(csv_file):
        no_data.append((proj, bug_id, "no csv")); continue
    rows = []
    with open(csv_file) as f:
        for row in csv.DictReader(f):
            if row['bug_id'] == bug_id:
                rows.append(row)
    if not rows:
        no_data.append((proj, bug_id, "not in jacoco")); continue
    has_missed = any(row.get('mi','').isdigit() and int(row['mi']) > 0 for row in rows)
    (not_hundred if has_missed else hundred).append((proj, bug_id))

print(f"Total no-add_test: {len(no_test)}")
print(f"100% buggy coverage (mi all 0): {len(hundred)}")
print(f"Not 100% (has mi>0):            {len(not_hundred)}")
print(f"No jacoco data:                 {len(no_data)}")
print()
print("=== 100% coverage cases ===")
for p,b in hundred: print(f"  {p}-{b}")
print()
print("=== No jacoco data ===")
for p,b,r in no_data: print(f"  {p}-{b}: {r}")
