# Pipeline scripts

```
scripts/
├── generation/     query the models — build payloads, submit, collect raw responses
│   ├── build_requests.py     build batch payloads   (Claude, GPT5)
│   ├── submit_Claude.sh      Anthropic Message Batches
│   ├── submit_GPT5.sh        OpenAI Batch API
│   └── submit_DeepSeek.py    DeepSeek synchronous chat completions
└── evaluation/     run the tests — apply patches, execute, collect coverage, score
    ├── evaluate_baseline.sh  evaluate_exm1.sh  evaluate_exm2.sh  evaluate_exm3.sh
    └── <setting>step<N>_*.py
```

`generation/` produces what lands in `results/data/`; `evaluation/` produces what lands in
`results/evaluation/`.

# `generation/`


Every script takes `--setting baseline|exm1|exm2|exm3` and an optional `--project <name>`.
Inputs come from `results/data/` and [`prompt/`](../prompt/); output goes to
`generated_requests/` and `generated_outputs/`, so nothing under `results/` is overwritten.
Paths are anchored to the repository, so the scripts run from any directory.

## Requirements

- `bash`, `curl`, `jq` — for the two `.sh` scripts
- Python 3 with `requests` (`pip install requests`) — for `submit_DeepSeek.py`
- An API key in the environment:

```bash
export ANTHROPIC_API_KEY='...'
export OPENAI_API_KEY='...'
export DEEPSEEK_API_KEY='...'
```

## Running

Claude and GPT5 use batch endpoints, so they run in two stages; DeepSeek is called directly.

```bash
python scripts/generation/build_requests.py  --setting exm3 --model Claude
bash   scripts/generation/submit_Claude.sh   --setting exm3
bash   scripts/generation/submit_GPT5.sh     --setting exm3 --project Lang
python scripts/generation/submit_DeepSeek.py --setting exm3 --workers 5
```

The submit scripts read the payloads `build_requests.py` wrote, so the two stages chain
without any extra configuration. Point `INPUT_ROOT` elsewhere to submit a different set.

Progress is written to `generated_outputs/<setting>/<Model>/batches.csv`, so an interrupted
run resumes where it stopped.

---

### `build_requests.py`

```bash
# one project of one (setting, model)
python scripts/generation/build_requests.py --setting exm3 --model Claude --project Lang
# everything, without writing anything
python scripts/generation/build_requests.py --all --dry-run
```

| Argument | Description |
|---|---|
| `--setting {baseline,exm1,exm2,exm3}` | Required unless `--all` |
| `--model {Claude,GPT5}` | Required unless `--all` |
| `--project PROJECT` | One Defects4J project, e.g. `Lang`; default all 17 |
| `--all` | Every setting and every model |
| `--out-root OUT_ROOT` | Output root, default `generated_requests/` |
| `--dry-run` | Report what would be built without writing files |
| `-h`, `--help` | Show help |

### `submit_Claude.sh`

```bash
export ANTHROPIC_API_KEY='...'
bash scripts/generation/submit_Claude.sh --setting exm3 --project Lang
```

| Argument | Description |
|---|---|
| `--setting`, `-s` | `baseline` \| `exm1` \| `exm2` \| `exm3`, required |
| `--project`, `-p` | One project, or several comma separated; default all |
| `-h`, `--help` | Show help |

| Environment variable | Default |
|---|---|
| `ANTHROPIC_API_KEY` | required |
| `INPUT_ROOT` | `generated_requests/<setting>/Claude` |
| `OUTPUT_ROOT` | `generated_outputs/<setting>/Claude` |
| `CSV` | `<OUTPUT_ROOT>/batches.csv` |
| `API_BASE` | `https://api.anthropic.com` |
| `ANTHROPIC_VERSION` | `2023-06-01` |
| `ANTHROPIC_BETA` | `message-batches-2024-09-24` |
| `POLL_SECONDS` | `20` |
| `MAX_WAIT_SECONDS` | `86400` |
| `TMP_RESP` | `.tmp_resp.json` |

### `submit_GPT5.sh`

```bash
export OPENAI_API_KEY='...'
# MAX_ACTIVE caps how many batches stay in OpenAI's queue at once; 0 means no local cap
MAX_ACTIVE=4 bash scripts/generation/submit_GPT5.sh --setting exm3
```

| Argument | Description |
|---|---|
| `--setting`, `-s` | `baseline` \| `exm1` \| `exm2` \| `exm3`, required |
| `--project`, `-p` | One project, or several comma separated; default all |
| `-h`, `--help` | Show help |

| Environment variable | Default |
|---|---|
| `OPENAI_API_KEY` | required |
| `INPUT_ROOT` | `generated_requests/<setting>/GPT5` |
| `OUTPUT_ROOT` | `generated_outputs/<setting>/GPT5` |
| `CSV` | `<OUTPUT_ROOT>/batches.csv` |
| `ENDPOINT` | `/v1/responses` |
| `COMPLETION_WINDOW` | `24h` |
| `MAX_ACTIVE` | `0` — no local cap; submit until OpenAI reports a queue or rate limit |
| `POLL_SECONDS` | `20` |
| `MAX_WAIT_SECONDS` | `86400` |
| `TMP_RESP` | `.tmp_resp.json` |

### `submit_DeepSeek.py`

```bash
export DEEPSEEK_API_KEY='...'
python scripts/generation/submit_DeepSeek.py --setting exm3 --project Lang --workers 5
```

| Argument | Description |
|---|---|
| `--setting {baseline,exm1,exm2,exm3}` | Required |
| `--project PROJECT` | One Defects4J project, e.g. `Lang`; default all |
| `--workers WORKERS` | Worker threads, default `5` |
| `--input-dir INPUT_DIR` | Root holding `input.java`, default `results/data/<setting>/DeepSeek` |
| `--out-dir OUT_DIR` | Output root, default `generated_outputs/<setting>/DeepSeek` |
| `-h`, `--help` | Show help |

Requires `DEEPSEEK_API_KEY`. A bug whose `output.json` already exists is skipped, so an
interrupted run resumes.

## Request parameters

| | Claude | GPT5 | DeepSeek |
|---|---|---|---|
| Model | `claude-sonnet-4-5` | `gpt-5` | `deepseek-reasoner` |
| `max_tokens` | 40000 | 40000 | 60000 |
| Requests per file | 50 | 20 (15 for `exm3`) | not chunked |
| Payload | `batch.json`, `batch1.json`, … | `requests.jsonl`, `requests1.jsonl`, … | none |
| Raw results | `<name>_output.jsonl` | `<name>_output.jsonl` | `output.json` per bug |

## Reading the raw responses

The model's text sits at:

| Provider | Path within a result record |
|---|---|
| Claude | `result.message.content[0].text` |
| GPT5 | `response.body.output[type=="message"].content[0].text` |
| DeepSeek | written directly to `output.json` |

It is the JSON object the prompt specifies: `fixed_code`, plus `trigger_method` for `exm1`,
the per-function `buggy`/`fixed` line sets for `exm2`, or `additional_testcases` for `exm3`.
A model may wrap it in a ``` fence or precede it with analysis. Parsed out, these become the
`output.json`, `patch*.java`, `prediction.json`, and `add_test*.java` published under
`results/data/`.

---

# `evaluation/`

Applies each generated patch to a fresh Defects4J checkout, runs the developer-written test
suite, collects JaCoCo coverage, and scores the result.

## Requirements

- A local **Defects4J** installation with `defects4j` on `PATH`
- A **JDK** and the build tool each Defects4J project expects (Maven, Gradle, or Ant)
- **`JACOCO_HOME`** pointing at an unpacked JaCoCo release — a directory holding
  `lib/jacocoagent.jar` and `lib/jacococli.jar`. Needed by `evaluate_exm2.sh` and `evaluate_exm3.sh`
- Python 3 with **`pandas`** (`pip install pandas`) — needed by `exm2_step6_score.py`

```bash
export JACOCO_HOME=/path/to/jacoco
```

Checkouts are made under `defects4j_checkouts/` and deleted once each batch finishes.
The per-bug metadata is bundled: `bug_methods/<project>_methods.csv` gives the line range of
every developer-modified method, and `oracle_triggers.csv` lists the triggering testcases of
every bug. Everything else comes from Defects4J itself.

## Paths

Every driver reads the model output from `results/data/<setting>/<Model>/` and never writes
there. Intermediate tables go to `generated_evaluation/<setting>_work/`, and the final
per-project CSVs land in `generated_evaluation/<setting>/<Model>/<Project>.csv` with the same
columns as `results/evaluation/`, so a re-run can be diffed against the published tables
directly. Each path can be overridden through the environment: `RESULTS_ROOT`, `OUT_ROOT`,
`WORK`, `LOGS_ROOT`, `PROJECTS_ROOT`, `CSV_DIR`.

---

## Running

### `evaluate_baseline.sh`

```bash
bash scripts/evaluation/evaluate_baseline.sh Claude Chart      # one project
bash scripts/evaluation/evaluate_baseline.sh Claude            # every project
ONLY_IDS=1,6 bash scripts/evaluation/evaluate_baseline.sh Claude Csv
```

| Argument | Description |
|---|---|
| `<Model>` | `Claude`, `DeepSeek`, or `GPT5`. Required, first positional |
| `[Project ...]` | Defects4J projects; omit for all of them |
| `ONLY_IDS` | Environment variable restricting the run to these bug ids |

### `evaluate_exm1.sh`

```bash
bash scripts/evaluation/evaluate_exm1.sh Claude Chart Lang Math
AFFECTED_CSV=rerun.csv bash scripts/evaluation/evaluate_exm1.sh Claude
```

Same arguments as `evaluate_baseline.sh`. `AFFECTED_CSV` optionally restricts the run to the
`project,bug_id` pairs listed in a CSV; unset, every bug under the patch root is evaluated.

### `evaluate_exm2.sh`

```bash
bash scripts/evaluation/evaluate_exm2.sh --model GPT5
bash scripts/evaluation/evaluate_exm2.sh --model GPT5 --project Chart --ids 1,3,5-7
bash scripts/evaluation/evaluate_exm2.sh --model GPT5 --from-step 4
```

| Argument | Description |
|---|---|
| `--model` | Required |
| `--project` | One project or a comma-separated list; omit for all |
| `--ids` | Bug ids such as `1,3,5-7`; omit for all |
| `--from-step N` | Resume from step N instead of step 1 |

Step 0 collects coverage of the unpatched program. It does not depend on the model, so it is
cached in `generated_evaluation/exm2_work/coverage_buggy/` and skipped on later runs; delete
that directory or set `BUGGY_COVERAGE` to force a rebuild.

### `evaluate_exm3.sh`

```bash
bash scripts/evaluation/evaluate_exm3.sh --model Claude --project JacksonDatabind
bash scripts/evaluation/evaluate_exm3.sh --model Claude --project Csv --ids 1 --from-step 5
```

| Argument | Description |
|---|---|
| `--model` | Defaults to `GPT5` |
| `--project` | Required |
| `--ids` | Bug ids such as `1,3,5-7`; omit for all |
| `--from-step N` | Resume from step N instead of step 1 |
| `--testlog-root` | Where the per-stage test logs go |
| `--run-log-dir` | Where this run's own log goes |

---

<!-- ## Step scripts

The drivers pass every path explicitly, so these are only run directly when re-doing one
stage. Each takes `--help`.

### baseline

```bash
python3 scripts/evaluation/baseline_step1_apply_patch.py \
    --csv scripts/evaluation/bug_methods/csv_methods.csv \
    --patches-root results/data/baseline/Claude/Csv \
    --projects-root defects4j_checkouts --project Csv --ids 1,3,5-7

python3 scripts/evaluation/baseline_step2_run_tests.py \
    --bug-root defects4j_checkouts/Csv \
    --logs-root test-logs/baseline/Claude --ids 1,3,5-7

python3 scripts/evaluation/baseline_step3_score.py \
    --baseline-root results/data/baseline \
    --out-root generated_evaluation/baseline \
    --logs-root test-logs/baseline --model Claude --project Csv
```

`step1` writes each `patch*.java` over the line range of its buggy method, keeping a `.bak`
that `--restore` puts back. `step2` runs `ant clean` then `defects4j test -r`, logging both.
`step3` classifies every bug from its log as `pass` / `not pass` / `not compilable` /
`timeout` / `unknown` / `no log`.

### exm1 — triggering testcase identification

```bash
python3 scripts/evaluation/exm1_step1_apply_patch.py \
    --csv scripts/evaluation/bug_methods/csv_methods.csv \
    --patches-root results/data/exm1/Claude/Csv \
    --projects-root defects4j_checkouts --project Csv --ids 1

python3 scripts/evaluation/exm1_step2_run_tests.py \
    --bug-root defects4j_checkouts/Csv \
    --logs-root test-logs/exm1/Claude --ids 1

python3 scripts/evaluation/exm1_step3_score.py \
    --results-root results/data/exm1 \
    --out-root generated_evaluation/exm1 \
    --logs-root test-logs/exm1 --model Claude --project Csv
```

`step1` and `step2` handle the slice variants — a bug split by the 300-method limit has
directories `14`, `14_1`, `14_2`, each patched and tested on its own. `step3` writes one row
per slice, recording `trigger_appear` and how many oracle triggers the model predicted, read
from `--trigger-csv` (default `oracle_triggers.csv`).

### exm2 — line coverage prediction

```bash
python3 scripts/evaluation/exm2_step1_apply_patch.py \
    --csv scripts/evaluation/bug_methods/csv_methods.csv \
    --patches-root results/data/exm2/Claude/Csv \
    --projects-root defects4j_checkouts --project Csv \
    --fixed-out-dir generated_evaluation/exm2_work/Claude/fixed_methods

python3 scripts/evaluation/exm2_step2_run_tests_with_coverage.py \
    --bug-root defects4j_checkouts/Csv \
    --logs-root test-logs/exm2/Claude --jacoco

python3 scripts/evaluation/exm2_step3_parse_coverage.py defects4j_checkouts \
    --methods-dir generated_evaluation/exm2_work/Claude/fixed_methods \
    -o generated_evaluation/exm2_work/Claude/coverage_patched --project Csv

python3 scripts/evaluation/exm2_step4_align_prediction.py \
    --buggy-dir generated_evaluation/exm2_work/coverage_buggy \
    --fixed-dir generated_evaluation/exm2_work/Claude/coverage_patched \
    --expn-root results/data/exm2/Claude \
    -o generated_evaluation/exm2_work/Claude/prediction_vs_actual --project Csv

python3 scripts/evaluation/exm2_step5_collect_patch_results.py \
    --root test-logs/exm2/Claude \
    --out generated_evaluation/exm2_work/Claude/patch_results

python3 scripts/evaluation/exm2_step6_score.py \
    --prediction_root generated_evaluation/exm2_work/Claude/prediction_vs_actual \
    --test_results_root generated_evaluation/exm2_work/Claude/patch_results \
    --out_dir generated_evaluation/exm2/Claude
```

`step3` parses `jacoco.xml` against a `*_methods.csv`; pass `bug_methods/` for the unpatched
program and the `fixed_methods/` written by `step1` for the patched one. `step4` joins the
observed coverage with the model's `prediction.json`, one row per source line. `step6` scores
precision, recall, and F1 over the informative lines.

### exm3 — additional testcase generation

```bash
python3 scripts/evaluation/exm3_step1_insert_testcases.py \
    --project Csv --ids 1 --defects-root defects4j_checkouts \
    --results-root results/data/exm3/Claude

python3 scripts/evaluation/exm3_step2_run_on_buggy.py \
    --project Csv --ids 1 --defects-root defects4j_checkouts \
    --results-root results/data/exm3/Claude \
    --out-dir generated_evaluation/exm3_work/Claude/run_buggy \
    --ind-out-dir generated_evaluation/exm3_work/Claude/run_buggy_individual \
    --logs-root test-logs/exm3/Claude/buggy

python3 scripts/evaluation/exm3_step8_score.py \
    --project Csv \
    --work-root generated_evaluation/exm3_work/Claude \
    --out-dir generated_evaluation/exm3_work/Claude/final_combined \
    --out-dir-individual generated_evaluation/exm3/Claude
```

Steps 1–7 run each generated testcase on three program versions — the buggy program, the
model-patched program, and the developer-written fixed program — and collect coverage for
each. Every stage writes both a combined table, all of a bug's testcases inserted together,
and an `_individual` table, each testcase alone. `step8` joins them; the paper reports the
individual scoring, which is what lands in `generated_evaluation/exm3/`.

Run any of them with `--help` for the full argument list. -->
