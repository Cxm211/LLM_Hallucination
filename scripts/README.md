# Pipeline scripts

The scripts used to query the models and turn their responses into the per-bug artifacts analyzed in the paper. They are archived **as run**, one set per (setting, model), because the three providers expose different APIs and each setting was driven with its own parameters.

```
scripts/
├── baseline/  Claude/  DeepSeek/  GPT5/
├── exm1/      Claude/  DeepSeek/  GPT5/      # Task 1 — Triggering testcase identification
├── exm2/      Claude/  DeepSeek/  GPT5/      # Task 2 — Line coverage prediction
└── exm3/      Claude/  DeepSeek/  GPT5/      # Task 3 — Additional testcase generation
```

## API keys

Every script reads its credential from the environment and **fails fast if it is unset**:

```bash
export ANTHROPIC_API_KEY='...'   # Claude
export OPENAI_API_KEY='...'      # GPT5
export DEEPSEEK_API_KEY='...'    # DeepSeek
```

## The three providers

| | Claude (Anthropic) | GPT5 (OpenAI) | DeepSeek |
|---|---|---|---|
| Interface | Message Batches | Batch API, `/v1/responses` | Synchronous chat completions |
| Request payload | `z_requests/<project>/batch*.json`, one create body per file | `z_requests/<project>/requests*.jsonl`, one request per line | none — built in memory |
| Results | `z_output/<project>/batch*_results.jsonl` | `z_output/<project>/requests*_results.jsonl` | written straight to `output.json` |

Because DeepSeek has no batch endpoint, `submit.py` builds each request in memory and calls the synchronous API with a thread pool. That is why the DeepSeek result directories contain no `z_requests/` — it is not missing data.

## Per-file roles

| File | Role |
|---|---|
| `get_batch.py` | Walks `<project>/<bug_id>/input.java`, prepends the task prompt from [prompt/](../prompt/), and writes chunked batch payloads to `z_requests/`. Anthropic and OpenAI only. |
| `submit.sh` | Submits the payloads, polls until each batch completes, and downloads results into `z_output/`. Anthropic and OpenAI. |
| `submit.py` | DeepSeek: builds the request, calls the synchronous API concurrently, and writes each response directly. |
| `step1_extract_output.py` | Parses raw results back into per-bug `output.json`, then derives `patch*.java` from `fixed_code` and — for `exm3` — `add_test*.java` from `additional_testcases`. |
| `split_patch.py` | Splits a multi-function `fixed_code` array into individual `patch*.java` files. |
| `check_coverage*.py` | `exm3/GPT5` only: ad-hoc checks over the collected coverage tables. |

## Order

```
get_batch.py  →  submit.sh / submit.py  →  step1_extract_output.py
```

Chunk sizes and token limits differ per setting and provider — Anthropic batches 50 requests per file, OpenAI 15–20, and `max_tokens` is 20000 for `exm1/GPT5` and 40000 elsewhere. The values are constants at the top of each `get_batch.py`.
