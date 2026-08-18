# Results

```
results/
├── evaluation/<setting>/<Model>/    outcome of running the tests: the tables reported in the paper
└── data/<setting>/<Model>/          what the models produced, one directory per bug
```

`<setting>` is `baseline`, `exm1`, `exm2`, or `exm3` — the baseline and Tasks 1–3 of the
paper. `<Model>` is `Claude`, `DeepSeek`, or `GPT5`. Every per-project table carries one CSV
per Defects4J project: `Chart, Cli, Closure, Codec, Collections, Compress, Csv, Gson,
JacksonCore, JacksonDatabind, JacksonXml, Jsoup, JxPath, Lang, Math, Mockito, Time`.

---

# `evaluation/`

## `baseline/<Model>/<Project>.csv`

Per-bug patch outcome. Columns: `model, project, bug_id, status`, with
`status ∈ {pass, not pass}`.

## `exm1/<Model>/<Project>.csv` — Triggering testcase identification

One row per input slice: a bug whose relevant test suite exceeds the 300-method limit is
split into several, each an independent task instance. Columns: `model, project, bug_id,
variant, status, trigger_appear, n_trigger_total, n_trigger_appear, n_trigger_pred_success,
matched_triggers, pred_success_triggers`.

`trigger_appear` records whether an oracle triggering testcase was present in that slice,
which is what supports the trigger-insensitive / trigger-dependent / trigger-absent-only
analysis of Table 7.

Two setting-level tables sit beside the per-model directories:

| File | Content |
|---|---|
| `relevant_testcase_counts.csv` | Relevant test methods per bug: `project, bug_id, n_relevant_testcase`. Underlies the left panel of Figure 7 |
| `trigger_understanding_summary.csv` | Each bug's predicted trigger set classified against the oracle set, split by repair outcome. Summing the Pass and Fail column of a model reproduces Table 6 |

`trigger_understanding_summary.csv` columns: `understanding_category, Claude_Pass,
Claude_Fail, DeepSeek_Pass, DeepSeek_Fail, GPT5_Pass, GPT5_Fail`. The category is one of
`Correct Trigger Identification`, `Partial Trigger Identification`, `Trigger Omission`,
`Spurious Trigger Identification`, or `Mixed Trigger Misidentification`, following the set
relations defined in Table 6.

## `exm2/<Model>/<Project>.csv` — Line coverage prediction

One row per bug. Columns: `bug_id, bug_precision, bug_recall, bug_f1, fix_precision,
fix_recall, fix_f1, bug_score, fix_score, code_status`.

`bug_*` scores the predicted executed lines against JaCoCo coverage on the buggy program,
`fix_*` does the same on the model-patched program. `code_status ∈ {pass, notpass,
not_compilable, timeout}` is the patch outcome on the developer-written test suite; counting
`pass` reproduces the line coverage prediction column of Table 2.

## `exm3/<Model>/<Project>.csv` — Additional testcase generation

One row per generated testcase, each evaluated on its own. Key columns: `project, bug_id,
folder, add_test_file, class_fqcn, method_name, repair_result, buggy_result, fixed_result,
groundtruth_result`, followed by instruction and branch coverage deltas.

`buggy_result`, `groundtruth_result`, and `fixed_result` are the testcase's outcome on the
buggy program, the developer-written fixed program, and the model-patched program. A testcase
is **valid** iff `buggy_result = Not Pass` and `groundtruth_result = Pass`. `add_test_file`
identifies which generated testcase the row describes and is the join key to
[`human_labels/`](../human_labels/).

Some responses contain no additional testcase at all. Those rows carry `Not Generated` in
`add_test_file` and in the three outcome columns; they are kept because `repair_result` still
records the patch outcome for that bug. **Exclude them before computing testcase statistics** —
counting all rows understates validity, since `Not Generated` rows can never be valid:

```python
generated = [r for r in rows if r["add_test_file"] != "Not Generated"]
valid = [r for r in generated
         if r["buggy_result"] == "Not Pass" and r["groundtruth_result"] == "Pass"]
```

Doing so gives 2010 / 1369 / 591 generated testcases for Claude / DeepSeek / GPT5, of which
30.4% / 34.6% / 48.4% are valid — the population sizes of Table 1 and the validity rates the
paper reports.

`repair_result` works the other way round: it belongs to the bug, not to the testcase, and it
is the only place the patch outcome is recorded for a bug whose response generated no
testcase. Count it over **all** rows, deduplicated by bug:

```python
passed = {(r["project"], r["bug_id"]) for r in rows if r["repair_result"] == "Pass"}
```

That gives 293 / 310 / 387 plausible patches, the additional-testcase-generation column of
Table 2. Filtering out `Not Generated` first would drop 104 passing GPT5 bugs.

---

# `data/`

Only the per-bug artifacts are published. The raw API traffic, the coverage and prediction
tables feeding the scores, and the submission manifests are not distributed — `evaluation/`
carries the results they produced.

## Per-bug files — `data/<setting>/<Model>/<Project>/<BugID>/`

| File | Present in | Content |
|---|---|---|
| `input.java` | all | The developer-modified buggy function(s) given to the model |
| `output.json` | all | The model's parsed JSON response |
| `patch.java`, `patch1.java`, … | all | Model-generated fixed function(s), from `fixed_code` |
| `fixed.java` | exm1, exm2, exm3 | The developer-written fixed function(s), i.e. ground truth |
| `trigger.json` | exm1 | Model-predicted set of bug-triggering test methods |
| `expn.json` | exm2 | Model-predicted executed lines per function, buggy and fixed |
| `add_test.java`, `add_test1.java`, … | exm3 | Generated additional testcase; line 1 is `// <target test file path>` |

Bug directories are named by Defects4J bug id. In `exm1`, a bug whose relevant test suite
exceeds the 300-method slice limit is split into variants (`14`, `14_1`, `14_2`, …), each an
independent task instance with its own patch.

---

## Regenerating

[`scripts/`](../scripts/) reads `input.java` from `data/` and the prompts from
[`prompt/`](../prompt/), and writes to its own output roots so nothing here is overwritten.
See [scripts/README.md](../scripts/README.md).

LLM APIs are not perfectly reproducible even at temperature 0, so re-running will not
reproduce these responses exactly.
