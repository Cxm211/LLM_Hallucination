# Better Understanding, Better Fixes? A Study of Hallucination in LLM-based Automated Program Repair

Replication package for the paper *"Better Understanding, Better Fixes? A Study of Hallucination in LLM-based Automated Program Repair."*

Xuemeng Cai, Jiakun Liu, Linhan Yang, Wei Ma, Lingxiao Jiang

Singapore Management University · Harbin Institute of Technology · Blekinge Institute of Technology

📄 [Hallucination-3.pdf](Hallucination-3.pdf)

---

## Overview

Existing evaluations of LLM-based automated program repair (APR) are largely **result-centric**: they count how many patches pass the developer-written test suite. This tells us little about whether a repair is grounded in a faithful understanding of the bug.

This study performs a **multi-layered analysis of hallucination** across the APR process. We characterize hallucination as *the production of plausible patches or intermediate artifacts that are not faithfully grounded in the available repair evidence*, and split it into two forms:

- **Repair hallucination** — manifested in the final patch: uncompilable patches, patches that deviate from the developer-intended repair semantics, or patches that overfit the available test suite.
- **Understanding hallucination** — manifested in intermediate artifacts: misidentified bug-triggering testcases, inaccurate line-coverage predictions, and invalid additional testcases.

We probe understanding hallucination through three execution-grounded APR tasks, each of which *also* requires the model to produce a patch, so repair and understanding hallucination can be analyzed in pairs:

| Task | What the model must produce | Ground truth | Metric |
|---|---|---|---|
| **1. Triggering testcase identification** | The subset of relevant testcases that expose the bug, plus a patch | Test methods that actually fail on the buggy version | Precision / Recall / F1, Pass@1 |
| **2. Line coverage prediction** | Lines executed in the buggy functions and in its own patched functions under the triggering tests, plus a patch | JaCoCo line coverage on the buggy and model-patched programs | Precision / Recall / F1, Pass@1 |
| **3. Additional testcase generation** | New bug-triggering testcases, plus a patch | A testcase is *valid* iff it fails on the buggy program **and** passes on the developer-written fixed program | Validity@1, Pass@1 |

A **baseline** setting, in which the model receives only the buggy functions and is asked for a minimal fix, is included for comparison.

**Subjects:** 3 LLMs (GPT-5, DeepSeek-R1, Claude Sonnet 4.5) × 832 Defects4J bugs across 17 projects, queried zero-shot through provider APIs (November 2025).

## Key findings

- **Repair hallucination is prevalent.** Only **21.0%–55.9%** of generated patches pass the developer-written test suite. Baseline rates are lowest (21.0%–26.3%); line coverage prediction yields the highest plausible-patch rate in every model.
- **Manual analysis of 812 sampled repairs finds repair hallucinations in 590 cases (72.7%)**, including patches that pass all available tests. *Incorrect Causal Localization* (45.9%) and *Incorrect Repair Strategy* (18.5%) dominate.
- **Understanding hallucination is prevalent too.** Exact triggering-testcase identification succeeds for only **23.0%–40.9%** of bugs; mixed misidentification affects 33.3%–47.6%. Only **30.5%–48.4%** of generated additional testcases are valid.
- **Better intermediate artifacts correlate with successful repair, but not deterministically.** Some failing repairs have accurate artifacts, and 45.7%–67.7% of testcases attached to *passing* repairs are still invalid.
- **Branching control flow is the main source of coverage-prediction errors** — branch-related structures appear in 39 of 44 manually inspected low-scoring predictions (88.6%).
- **Three contributing factors:** insufficient project-context grounding, imprecise causal localization, and unstable execution-path reasoning.

### Plausible patches (Table 2 in the paper)

| Model | Baseline | Task 1: Trigger ID | Task 2: Line Coverage | Task 3: Additional Test |
|---|---|---|---|---|
| Claude Sonnet 4.5 | 178 (21.4%) | 222 (26.7%) | 308 (37.0%) | 293 (35.2%) |
| DeepSeek-R1 | 175 (21.0%) | 314 (37.7%) | 403 (48.4%) | 310 (37.3%) |
| GPT-5 | 219 (26.3%) | 378 (45.4%) | 465 (55.9%) | 387 (46.5%) |

*(out of 832 bugs each)*

---

## Repository layout

```
.
├── Hallucination-3.pdf       # The paper
├── prompt/                   # The exact system prompt for each of the four settings
├── baseline/                 # Baseline repair setting (patch only, no intermediate artifact)
├── exm1/                     # Task 1 — Triggering testcase identification
├── exm2/                     # Task 2 — Line coverage prediction
├── exm3/                     # Task 3 — Additional testcase generation
├── test-logs/                # Raw Defects4J test-execution logs for every run
│   ├── baseline/ exp1/ exp2/ exp3/
└── human_labels/             # Manual annotation results and codebook
```

Every experiment directory follows the same three-level shape:

```
<experiment>/<Model>/<Project>/<BugID>/       # Model ∈ {Claude, DeepSeek, GPT5}
```

with 17 Defects4J projects: `Chart, Cli, Closure, Codec, Collections, Compress, Csv, Gson, JacksonCore, JacksonDatabind, JacksonXml, Jsoup, JxPath, Lang, Math, Mockito, Time`.

Directories prefixed `z_` hold aggregated pipeline state (API requests, raw responses, coverage tables, scores) rather than per-bug inputs/outputs.

### Per-bug files

| File | Present in | Content |
|---|---|---|
| `input.java` | all | The developer-modified buggy function(s) given to the model |
| `output.json` | all | The model's parsed JSON response |
| `patch.java`, `patch1.java`, … | all | Model-generated fixed function(s), extracted from `output.json`'s `fixed_code` |
| `fixed.java` | exm1, exm2, exm3 | The developer-written fixed function(s) (ground truth) |
| `trigger.json` | exm1 | Model-predicted set of bug-triggering test methods |
| `expn.json` | exm2 | Model-predicted executed lines, per function, for the buggy and fixed versions |
| `add_test.java`, `add_test1.java`, … | exm3 | Generated additional testcase; line 1 is `// <target test file path>` |

Bug directories are named by Defects4J bug id. In `exm1`, bugs whose relevant test suite exceeds the 300-method slice limit are split into variants (`14`, `14_1`, `14_2`, …), each an independent task instance.

### Shared per-model files

| File | Content |
|---|---|
| `prompt_*.txt` | A copy of that task's system prompt; the canonical, task-named versions live in [prompt/](prompt/) |
| `z_requests/` | Generated batch-API request payloads, chunked per project |
| `z_output/` | Raw batch-API results (`batch*_results.jsonl`) |
| `report.csv`, `report.txt` | Extraction/parse diagnostics — bugs skipped or responses that failed to parse |
| `sample_*.csv` | The randomly sampled cases sent to human annotation |

---

## Experiment directories in detail

### `baseline/` — Baseline repair

The model receives only the buggy functions and is asked for a minimal correct fix; no intermediate artifact is requested.

- `z_final/<Project>.csv` — per-bug patch outcome.
  Columns: `model, project, bug_id, status, log`, where `status ∈ {pass, not pass}` and `log` points into `test-logs/baseline/`.
- `sample_87.csv` — sampled cases for annotation.

### `exm1/` — Task 1: Triggering testcase identification

- `z_final/<Project>.csv` — per-instance evaluation.
  Columns: `model, project, bug_id, variant, status, trigger_appear, n_trigger_total, n_trigger_appear, n_trigger_pred_success, matched_triggers, pred_success_triggers`.
  `trigger_appear` records whether an oracle triggering testcase was present in that slice — this is what supports the *trigger-insensitive / trigger-dependent / trigger-absent-only success* analysis (Table 7).
- `sample_94.csv` — 94 sampled instances per model.
- `make_sample.py` — regenerates `sample_94.csv` for all three models from `z_final/` with a fixed seed (42).

### `exm2/` — Task 2: Line coverage prediction

- `z_prediction/{buggy,fixed}/<Project>.csv` — model prediction aligned to ground truth, one row per source line.
  Columns: `bug_id, folder, function_id, line_in_method, executed, prediction`.
- `z_buggy_jacoco/<Project>.csv`, `z_fixed_jacoco/<Project>.csv` — JaCoCo-collected ground-truth coverage mapped back onto the extracted functions.
  Columns include `class_fqcn, file, method_sig_snippet, method_start, method_end, line, line_in_method, ci, mi, cb, mb, executed` (`ci`/`mi` = covered/missed instructions, `cb`/`mb` = covered/missed branches).
- `z_fixed_info/<project>_methods.csv` — line ranges of each developer-fixed method, used to map coverage back to functions.
- `z_scores/<Project>_scores.csv` — per-bug precision/recall/F1 aggregates (`bug_score`, `fix_score`, `code_score`, `total_score`).
- `z_test_results/<Project>_results.csv` — patch outcome per bug: `bug, status, exit_code, failing_tests, log_file`, with `status ∈ {Pass, Not Pass, Uncompilable}`.

### `exm3/` — Task 3: Additional testcase generation

Each generated testcase is executed on **three** program versions — the buggy program, the developer-written fixed program (the behavioral oracle), and the model-generated patched program — hence the parallel directory families:

| Directory | Meaning |
|---|---|
| `z_add_testcase_buggy/` | Generated testcase run on the **buggy** program |
| `z_add_testcase_fixed/` | Generated testcase run on the **model-patched** program |
| `z_add_groundtruth/` | Generated testcase run on the **developer-written fixed** program |
| `z_add_testcase_{buggy,fixed}_coverage/` | Coverage collected while running the generated testcase |
| `z_buggy_jacoco_*`, `z_fixed_jacoco_*` | Baseline coverage without the generated testcase, used to compute the coverage delta |
| `*_individual/` vs. `*_combined/` | Each generated testcase evaluated alone vs. all testcases for a bug inserted together |
| `z_fixed/`, `z_fixed_info/` | Developer-written fixed program and its method line ranges |

- `z_final_score/<Project>.csv` — the joined per-testcase result table. Key columns:
  `project, bug_id, folder, add_test_file, class_fqcn, method_name, repair_result, buggy_result, fixed_result, groundtruth_result`, followed by coverage deltas (`buggy_inst_delta`, `buggy_branch_score`, …).
  A testcase is **valid** iff `buggy_result = Not Pass` and `groundtruth_result = Pass`.
- `z_final_score.csv` — the outcome-tuple contingency table (`repair_result, buggy_result, fixed_result, groundtruth_result, count, ratio`) that underlies Figure 11.
- `z_pass_notpass_notpass.csv`, `z_notpass_pass_pass.csv`, … — slices of the contingency table isolating specific outcome combinations for inspection.
- `sample_92.csv` — sampled cases for annotation (Claude; DeepSeek and GPT-5 use 90 and 87).
- `get_batch.py` — builds the batch-API request payloads from `input.java` + the task prompt.
- `step1_extract_output.py` — parses `z_output/*/batch*_results.jsonl` back into per-bug `output.json`, `patch*.java`, and `add_test*.java`.

### `test-logs/`

Raw Defects4J test-run logs for every patched program, organized as `test-logs/<setting>/<Model>/<Project>/<Project>-<BugID>-buggy.log`. `exp1`, `exp2`, `exp3` correspond to `exm1`, `exm2`, `exm3`. The `log_file` / `log` columns in the score CSVs point here.

### `human_labels/`

Case-level manual annotations behind Tables 5, 8, and 9 of the paper, produced by the procedure in Section 3.6 (independent open coding by two annotators, codebook construction, conflict resolution with two additional authors; initial Cohen's κ = 0.82 for repair hallucination and 0.49 for understanding hallucination, all conflicts resolved before final labeling).

**[human_label.md](human_labels/human_label.md) is the codebook** — directory layout, per-file column schemas, the value domain of every label column, and join instructions. Read it before using the CSVs.

- `Repair_hallucination/<Task>/<Model>.csv` — how the model-generated **patch** deviates from the developer-intended repair.
  Columns: `project, bug_id, label, overfitting, label_if_others`.
  `label` takes one value from a 12-value taxonomy; `overfitting` is an **independent** binary attribute and must not be inferred from `label`.
- `Understanding_hallucination/Line_coverage_prediction/<Model>.csv` — code structures associated with low-scoring coverage predictions (F1 ≤ 0.5), annotated separately for the buggy and model-patched programs.
  Columns: `project, bug_id, buggy_label, fixed_label`. Labels are **not mutually exclusive** (multiple values joined with `; `), and an empty cell means *not selected for annotation*, not *no hallucination*.
- `Understanding_hallucination/Additional_testcase_generation/<Model>.csv` — how each generated **testcase** fails to capture the bug-triggering behavior, over a 14-category taxonomy keyed to execution outcomes on the buggy, developer-fixed, and model-patched programs.
  Columns: `project, bug_id, add_test_file, label` (`label_if_others` in `DeepSeek.csv`).

Repair hallucination is annotated for all nine task-model groups. Understanding hallucination covers line coverage prediction and additional testcase generation only: for triggering testcase identification the discrepancy between the predicted and oracle trigger sets is fully characterized by set relations and derived automatically (Table 6), so it is not manually annotated.

Sample sizes (Table 1), each meeting a 95% confidence level with a 10% margin of error over its task-model population:

| Task | Population *N* | Sample *n* (Claude / DeepSeek / GPT-5) |
|---|---|---|
| Triggering testcase identification | 1716 (sliced instances) | 94 / 94 / 94 |
| Line coverage prediction | 832 (bugs) | 87 / 87 / 87 |
| Additional testcase generation | 2102 / 1369 / 591 (generated testcases) | 92 / 90 / 87 |

---

## Experimental setup

| | |
|---|---|
| **Benchmark** | [Defects4J](https://github.com/rjust/defects4j) — 832 bugs after filtering (checkout/compile/test succeeds; ≥1 triggering testcase; every developer-modified location lies inside an identifiable function, and the patch is not solely a new function) |
| **Models** | GPT-5, DeepSeek-R1, Claude Sonnet 4.5 (`claude-sonnet-4-5`), zero-shot, one response per instance, no fine-tuning or tool use |
| **Decoding** | temperature 0 for Claude Sonnet 4.5; provider defaults for GPT-5 and DeepSeek-R1, where effective temperature control is unavailable |
| **Querying** | Provider batch APIs; requests chunked at 50 per file (`get_batch.py`), `max_tokens = 40000` |
| **Context** | Buggy functions only — never full source files. Task 1 additionally slices relevant test methods into chunks of ≤300 methods |
| **Coverage** | [JaCoCo](https://www.jacoco.org/jacoco/) line-level coverage, mapped back onto the extracted buggy / model-patched functions. Blank lines, comment-only lines, and brace-only lines are excluded |
| **Execution** | Isolated environments; 300 s timeout per build / test / coverage run, timeouts treated as failures. Java version and build tool (Maven, Gradle, Ant) configured per Defects4J project |

---

## Reproducing

The pipeline runs in four stages. Stage 1 and 2 helper scripts are included; the Defects4J orchestration and scoring code is not part of this package, since it is tightly coupled to the local Defects4J checkout and JVM configuration — the resulting artifacts are provided instead.

**1. Build requests.** `get_batch.py` walks `<Project>/<BugID>/input.java`, prepends the task prompt, and writes chunked batch payloads to `z_requests/<Project>/batch*.json`:

```bash
cd exm3/Claude
python get_batch.py                 # all projects
python get_batch.py --root Lang     # one project
```

**2. Submit and extract.** Submit the payloads to the provider batch API, place the results under `z_output/<Project>/batch*_results.jsonl`, then parse them back into per-bug files:

```bash
python step1_extract_output.py                     # writes output.json, patch*.java, add_test*.java
python step1_extract_output.py --project Lang
```

**3. Evaluate.** Apply each `patch*.java` to a fresh Defects4J checkout, run the developer-written test suite (300 s timeout), and classify the outcome as `Pass` / `Not Pass` / `Uncompilable`. Collect JaCoCo coverage for Task 2, and run generated testcases against the buggy, developer-fixed, and model-patched programs for Task 3. Logs land in `test-logs/`; scores land in `z_scores/`, `z_test_results/`, and `z_final_score/`.

**4. Sample for annotation.**

```bash
cd exm1 && python make_sample.py    # regenerates sample_94.csv for all three models (seed 42)
```

Only Python 3 with the standard library is needed for the included scripts. Reproducing stage 3 additionally requires Defects4J, JaCoCo, and the JDK/build-tool versions each Defects4J project expects.

---

## Notes and known gaps

- Model outputs and evaluation artifacts are provided as-is from the original runs. Coverage of the auxiliary `z_*` directories is uneven across models — for instance, `exm2` JaCoCo and score tables were produced under `exm2/Claude/` and are shared across models, and `z_requests/` / `z_output/` were retained only where the batch API was driven from this working tree.
- The full working tree is ~4 GB, dominated by `exm1/*/z_requests/` (raw API request payloads, which embed up to 300 test methods per request). Consider a shallow clone if you only need the aggregated result tables.
- LLM APIs are not perfectly reproducible even at temperature 0; exact re-runs may differ from the recorded outputs.

## Citation

```bibtex
@article{cai2026hallucination,
  title   = {Better Understanding, Better Fixes? A Study of Hallucination in LLM-based Automated Program Repair},
  author  = {Cai, Xuemeng and Liu, Jiakun and Yang, Linhan and Ma, Wei and Jiang, Lingxiao},
  year    = {2026}
}
```
