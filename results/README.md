# Results

The study covers four settings, each run against all three models:

| Setting | Task |
|---|---|
| `baseline` | Baseline repair — the model sees only the buggy functions and returns a patch |
| `exm1` | Task 1 — Triggering testcase identification |
| `exm2` | Task 2 — Line coverage prediction |
| `exm3` | Task 3 — Additional testcase generation |

Each setting produced two things, kept apart here:

```
results/
├── data/<setting>/<Model>/        concrete model output 
└── evaluation/<setting>/<Model>/  test outcome
```

`<Model>` is `Claude`, `DeepSeek`, or `GPT5`. All four settings cover the same 832 Defects4J
bugs across 17 projects: `Chart, Cli, Closure, Codec, Collections, Compress, Csv, Gson,
JacksonCore, JacksonDatabind, JacksonXml, Jsoup, JxPath, Lang, Math, Mockito, Time`. 

---

# `evaluation/`

## `baseline/<Model>/<Project>.csv`


| Column | Description |
|---|---|
| `model` | `Claude`, `DeepSeek`, or `GPT5` |
| `project` | Defects4J project name |
| `bug_id` | Numeric Defects4J bug identifier within that project |
| `status` | Outcome of the model's patch on the developer-written test suite: `pass`, `not pass`, `not compilable`, or `timeout` |

## `exm1/<Model>/<Project>.csv` — Triggering testcase identification

One row per input slice. 

| Column | Description |
|---|---|
| `model` | `Claude`, `DeepSeek`, or `GPT5` |
| `project` | Defects4J project name |
| `bug_id` | Numeric Defects4J bug identifier |
| `variant` | Which slice of that bug this row describes, e.g. `14`, `14_1`, `14_2`, … |
| `status` | Patch outcome on the developer-written test suite: `pass`, `not pass`, or `not compilable` |
| `trigger_appear` | `yes` if this slice contained at least one oracle triggering testcase, else `no`. This is what supports the trigger-insensitive / trigger-dependent / trigger-absent-only analysis of Table 7 |
| `n_trigger_total` | Number of oracle triggering testcases the bug has |
| `n_trigger_appear` | Number of those that were present in this slice |
| `n_trigger_pred_success` | Number of those the model predicted correctly |
| `matched_triggers` | The oracle triggers present in this slice, `; ` separated, as `class::method` |
| `pred_success_triggers` | The subset the model predicted correctly |

Two setting-level tables sit beside the per-model directories, each covering all three models.

### `exm1/relevant_testcase_counts.csv`

| Column | Description |
|---|---|
| `project` | Defects4J project name |
| `bug_id` | Numeric Defects4J bug identifier |
| `n_relevant_testcase` | Number of test methods that load at least one class containing a developer-modified function. Underlies the left panel of Figure 7 |

### `exm1/trigger_understanding_summary.csv`

Each bug's predicted trigger set classified against the oracle set, split by repair outcome.
Summing a model's Pass and Fail column reproduces Table 6.

| Column | Description |
|---|---|
| `understanding_category` | `Correct Trigger Identification`, `Partial Trigger Identification`, `Trigger Omission`, `Spurious Trigger Identification`, or `Mixed Trigger Misidentification`, following the set relations defined in Table 6 |
| `Claude_Pass`, `Claude_Fail` | Number of bugs in that category whose Claude patch did / did not pass |
| `DeepSeek_Pass`, `DeepSeek_Fail` | The same for DeepSeek |
| `GPT5_Pass`, `GPT5_Fail` | The same for GPT-5 |

## `exm2/<Model>/<Project>.csv` — Line coverage prediction

One row per bug. `bug_*` scores the predicted executed lines against JaCoCo coverage on the
buggy program; `fix_*` does the same on the model-patched program.

| Column | Description |
|---|---|
| `bug_id` | Numeric Defects4J bug identifier |
| `bug_precision`, `bug_recall`, `bug_f1` | Prediction accuracy on the buggy program |
| `fix_precision`, `fix_recall`, `fix_f1` | Prediction accuracy on the model-patched program |
| `bug_score`, `fix_score` | Aggregated per-bug scores over the two program versions |
| `code_status` | Patch outcome on the developer-written test suite: `pass`, `notpass`, `not_compilable`, or `timeout`. Counting `pass` reproduces the line coverage prediction column of Table 2 |

## `exm3/<Model>/<Project>.csv` — Additional testcase generation

One row per generated testcase, each evaluated on its own.

| Column | Description |
|---|---|
| `project` | Defects4J project name |
| `bug_id` | Numeric Defects4J bug identifier |
| `folder` | The checked-out program directory the testcase was run against, e.g. `Lang-1-buggy` |
| `add_test_file` | Which generated testcase this row describes — `add_test.java`, `add_test1.java`, … The join key to [`human_labels/`](../human_labels/). `Not Generated` when the response produced no testcase |
| `class_fqcn` | Fully qualified test class the generated method was inserted into |
| `method_name` | Name of the generated test method |
| `repair_result` | The model's **patch** outcome on the developer-written test suite: `Pass`, `Not Pass`, or `Not Compilable`. A property of the bug, not of the testcase |
| `buggy_result` | The generated testcase run on the **buggy** program: `Pass`, `Not Pass`, `Not Compilable`, or `Not Generated` |
| `groundtruth_result` | The same on the **developer-written fixed** program, the behavioral oracle |
| `fixed_result` | The same on the **model-patched** program; may additionally be `Timeout` |
| `buggy_inst_delta`, `fixed_inst_delta` | Number of additional instructions covered when the generated testcase is added, on the buggy and model-patched programs |
| `buggy_branch_delta`, `fixed_branch_delta` | Number of additional branches covered, likewise |
| `buggy_inst_score`, `fixed_inst_score`, `buggy_branch_score`, `fixed_branch_score` | Those deltas as a fraction of the previously uncovered instructions or branches |
| `*_base_ci`, `*_base_mi`, `*_base_cb`, `*_base_mb` | Number of JaCoCo covered/missed instructions and branches **before** the generated testcase is inserted |
| `*_add_ci`, `*_add_mi`, `*_add_cb`, `*_add_mb` | The same counts **after** insertion |

A testcase is **valid** iff `buggy_result = Not Pass` and `groundtruth_result = Pass`.

Rows with `add_test_file = Not Generated` are responses that produced no testcase at all.
**Exclude them when computing testcase statistics** — including them inflates the denominator
and gives 33.7% validity for GPT5 instead of the 48.4% the paper reports. After excluding
them the table holds 2010 / 1369 / 591 generated testcases for Claude / DeepSeek / GPT5, the
population sizes of Table 1, of which 30.4% / 34.6% / 48.4% are valid.

Do **not** exclude them when counting patches. `repair_result` belongs to the bug rather than
the testcase, and for 258 GPT5 bugs these rows are its only record. Counting `Pass` over all
rows, deduplicated by bug, gives the 293 / 310 / 387 plausible patches of Table 2; filtering
first would drop 104 passing GPT5 bugs.

---

# `data/`

The final outputs are published. 

## `data/<setting>/<Model>/<Project>/<BugID>/`

| File | Present in | Content |
|---|---|---|
| `input.java` | all | The developer-modified buggy function(s) given to the model together with the prompt |
| `output.json` | all | The model's parsed JSON response |
| `patch.java`, `patch1.java`, … | all | Model-generated fixed function(s), extracted from `fixed_code` in the same order as the buggy functions appear in `input.java` |
| `groundtruth.java` | exm1, exm2, exm3 | The developer-written fixed function(s) — the ground truth the patch is compared against |
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
