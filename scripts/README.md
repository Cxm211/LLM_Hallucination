# Pipeline scripts

```
scripts/
├── build_requests.py     build batch payloads   (Claude, GPT5)
├── submit_Claude.sh      Anthropic Message Batches
├── submit_GPT5.sh        OpenAI Batch API
└── submit_DeepSeek.py    DeepSeek synchronous chat completions
```

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
python scripts/build_requests.py  --setting exm3 --model Claude
bash   scripts/submit_Claude.sh   --setting exm3
bash   scripts/submit_GPT5.sh     --setting exm3 --project Lang
python scripts/submit_DeepSeek.py --setting exm3 --workers 5
```

The submit scripts read the payloads `build_requests.py` wrote, so the two stages chain
without any extra configuration. Point `INPUT_ROOT` elsewhere to submit a different set.

Progress is written to `generated_outputs/<setting>/<Model>/batches.csv`, so an interrupted
run resumes where it stopped.

---

### `build_requests.py`

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
A model may wrap it in a ``` fence or precede it with analysis.
