# Better Understanding, Better Fixes? A Study of Hallucination in LLM-based Automated Program Repair

Replication package for the paper *"Better Understanding, Better Fixes? A Study of Hallucination in LLM-based Automated Program Repair."*

Xuemeng Cai, Jiakun Liu, Linhan Yang, Wei Ma, Lingxiao Jiang

Singapore Management University · Harbin Institute of Technology · Blekinge Institute of Technology

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
├── prompt/               # The exact system prompt for each of the four settings
├── scripts/              # The pipeline that queried the models
├── results/              # Task inputs, model outputs, and evaluation artifacts
└── human_labels/         # Case-level manual annotations behind Tables 5, 8, and 9
```

Throughout this repository and the scripts, the four settings are named `baseline`, `exm1`, `exm2`, and `exm3`, corresponding to the baseline and to Tasks 1–3 respectively. Models are named `Claude`, `DeepSeek`, and `GPT5`.

### [`prompt/`](prompt/)

The four system prompts, one per setting, exactly as sent to the models. [prompt/README.md](prompt/README.md) documents what each prompt is given and the JSON schema it must return.

| File | Setting |
|---|---|
| `baseline.txt` | Baseline repair |
| `triggering_testcase_identification.txt` | Task 1 |
| `line_coverage_prediction.txt` | Task 2 |
| `additional_testcase_generation.txt` | Task 3 |

### [`scripts/`](scripts/)

One submit script per model, each covering all four settings through `--setting`. These are the scripts that produced this study's results, merged from the twelve per-(setting, model) originals. See [scripts/README.md](scripts/README.md).

```
scripts/
├── build_requests.py     build batch payloads   (Claude, GPT5)
├── submit_Claude.sh      Anthropic Message Batches
├── submit_GPT5.sh        OpenAI Batch API
└── submit_DeepSeek.py    DeepSeek synchronous chat completions
```

Inputs come from `results/data/` and `prompt/`; output goes to `generated_requests/` and `generated_outputs/`, so nothing under `results/` is overwritten. Credentials come from the environment and each script fails fast if unset: `ANTHROPIC_API_KEY`, `OPENAI_API_KEY`, or `DEEPSEEK_API_KEY`.

DeepSeek has no batch endpoint, so `submit_DeepSeek.py` reads `input.java` and the prompt directly and issues the requests concurrently — which is why `build_requests.py` covers only Claude and GPT5.

The pipeline stops at the raw API responses; `scripts/README.md` documents where the model's text sits in each provider's result record.

### [`results/`](results/)

```
results/
├── data/<setting>/<Model>/<Project>/<BugID>/   what the models produced
└── evaluation/<setting>/<Model>/<Project>.csv  what running the tests produced
```

`data/` holds the `input.java` given to each model, its parsed `output.json`, the `patch*.java` extracted from it, the setting's intermediate artifact (`trigger.json`, `expn.json`, or `add_test*.java`), and the developer-written `groundtruth.java`. `evaluation/` holds the patch outcomes, coverage precision/recall/F1, and generated-testcase validity tables reported in the paper.

Every setting covers the same 832 Defects4J bugs; `exm1` additionally splits a bug into several slices when its relevant test suite exceeds the 300-method limit, giving 3586 instances. [results/README.md](results/README.md) documents every column.

### [`human_labels/`](human_labels/)

Case-level manual annotations produced by the procedure in Section 3.6 of the paper: independent open coding by two annotators, codebook construction, then conflict resolution with two additional authors. Initial Cohen's κ = 0.82 for repair hallucination and 0.49 for understanding hallucination; every label released here is post-resolution.

**[human_label.md](human_labels/human_label.md) is the codebook** — directory layout, per-file column schemas, and the value domain of every label column. Read it before using the CSVs.

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
| **Querying** | Provider batch APIs where available; requests chunked at 50 per file for Anthropic and 15–20 for OpenAI, with `max_tokens` of 40000 |
| **Context** | Buggy functions only — never full source files. Task 1 additionally slices relevant test methods into chunks of ≤300 methods, each slice an independent instance |
| **Coverage** | [JaCoCo](https://www.jacoco.org/jacoco/) line-level coverage, mapped back onto the extracted buggy / model-patched functions. Blank lines, comment-only lines, and brace-only lines are excluded before scoring |
| **Execution** | Isolated environments; 300 s timeout per build / test / coverage run, timeouts treated as failures. Java version and build tool (Maven, Gradle, Ant) configured per Defects4J project |

---

## Reproducing

Stages 1 and 2 are scripted here. Stage 3 depends on a local Defects4J checkout and per-project JVM configuration, so its orchestration code is not part of this package.

**1. Query the models.** Claude and GPT5 go through their batch endpoints, so build the payloads first; DeepSeek is called directly.

```bash
export ANTHROPIC_API_KEY='...'      # or OPENAI_API_KEY / DEEPSEEK_API_KEY

python scripts/build_requests.py --setting exm3 --model Claude
bash   scripts/submit_Claude.sh   --setting exm3
bash   scripts/submit_GPT5.sh     --setting exm3 --project Lang
python scripts/submit_DeepSeek.py --setting exm3 --workers 5
```

The submit scripts read the payloads `build_requests.py` wrote, so the two stages chain without extra configuration. Raw responses land in `generated_outputs/`.

**2. Read the responses.** Each result record carries the model's text at `result.message.content[0].text` (Claude), `response.body.output[type=="message"].content[0].text` (GPT5), or `response.choices[0].message.content` (DeepSeek). That text is the JSON object the prompt specifies — `fixed_code` plus the setting's intermediate artifact. The artifacts derived that way for this study are published under `results/data/<setting>/<Model>/<Project>/<BugID>/`.

**3. Evaluate.** Apply each patched function to a fresh Defects4J checkout, run the developer-written test suite (300 s timeout), and classify the outcome as `Pass` / `Not Pass` / `Uncompilable`. Collect JaCoCo coverage for Task 2, and run each generated testcase against the buggy, developer-fixed, and model-patched programs for Task 3.

Only Python 3 with the standard library is needed for the included scripts. Stage 3 additionally requires Defects4J, JaCoCo, and the JDK and build tool each Defects4J project expects.

LLM APIs are not perfectly reproducible even at temperature 0, so re-runs will not match the recorded outputs exactly.

## Citation

```bibtex
@article{cai2026hallucination,
  title   = {Better Understanding, Better Fixes? A Study of Hallucination in LLM-based Automated Program Repair},
  author  = {Cai, Xuemeng and Liu, Jiakun and Yang, Linhan and Ma, Wei and Jiang, Lingxiao},
  year    = {2026}
}
```
