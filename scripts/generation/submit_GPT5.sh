#!/usr/bin/env bash
set -euo pipefail

# ---- merged from the four per-setting submit.sh scripts ------------------
# This script lives at <repo>/scripts/generation/submit_GPT5.sh
_SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$_SCRIPT_DIR/../.." && pwd)"
SETTING=""          # baseline | exm1 | exm2 | exm3, required via --setting
# -------------------------------------------------------------------------


ENDPOINT="${ENDPOINT:-/v1/responses}"
COMPLETION_WINDOW="${COMPLETION_WINDOW:-24h}"
# IMPORTANT:
# Do NOT hard-code OPENAI_API_KEY in this script.
# Run: export OPENAI_API_KEY='sk-...'
POLL_SECONDS="${POLL_SECONDS:-20}"
MAX_WAIT_SECONDS="${MAX_WAIT_SECONDS:-$((24*3600))}"

# 0 = auto-fill until OpenAI refuses more queued batch work.
# Set MAX_ACTIVE=100 if you still want a local safety cap.
MAX_ACTIVE="${MAX_ACTIVE:-0}"

TMP_RESP="${TMP_RESP:-.tmp_resp.json}"

PROJECT_FILTER="all"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --setting|-s)
      SETTING="${2:-}"
      [[ -n "$SETTING" ]] || { echo "Missing argument: --setting <baseline|exm1|exm2|exm3>" >&2; exit 1; }
      shift 2
      ;;
    --project|-p)
      PROJECT_FILTER="${2:-}"
      if [[ -z "$PROJECT_FILTER" ]]; then
        echo "Missing argument: --project <name|name1,name2|all>" >&2
        exit 1
      fi
      shift 2
      ;;
    --help|-h)
      cat >&2 <<EOF
Usage:
  bash $0 --setting exm3                   # all projects (default)
  bash $0 --setting exm3 --project Cli     # a single project
  bash $0 --setting exm3 --project Cli,Web # several, comma separated
  bash $0 -p Cli,Web

Environment variables:
  INPUT_ROOT       default <repo>/generated_requests/<setting>/GPT5
  OUTPUT_ROOT      default <repo>/generated_outputs/<setting>/GPT5
  OPENAI_API_KEY   required; never hard-code it in this script
  MAX_ACTIVE       default 0; 0 means no local cap, submit until OpenAI reports a queue/rate limit
  POLL_SECONDS     default 20
  MAX_WAIT_SECONDS default 86400
  ENDPOINT         default /v1/responses
  COMPLETION_WINDOW default 24h
  CSV              default <OUTPUT_ROOT>/batches.csv
  TMP_RESP         default .tmp_resp.json
Arguments:
  --setting, -s    baseline | exm1 | exm2 | exm3   (required)
  --project, -p    one project, or several comma separated (default all)
  --help, -h       show this message
EOF
      exit 0
      ;;
    *)
      echo "Unknown argument: $1 (see --help)" >&2
      exit 1
      ;;
  esac
done

# ---- resolve the setting-dependent paths --------------------------------
if [[ -z "$SETTING" ]]; then
  echo "Missing argument: --setting <baseline|exm1|exm2|exm3>" >&2
  exit 1
fi
case "$SETTING" in
  baseline|exm1|exm2|exm3) ;;
  *) echo "Unknown setting: $SETTING" >&2; exit 1 ;;
esac

INPUT_ROOT="${INPUT_ROOT:-$REPO_ROOT/generated_requests/$SETTING/GPT5}"
OUTPUT_ROOT="${OUTPUT_ROOT:-$REPO_ROOT/generated_outputs/$SETTING/GPT5}"
CSV="${CSV:-$OUTPUT_ROOT/batches.csv}"
mkdir -p "$OUTPUT_ROOT"
echo "🧭 setting=$SETTING  input=$INPUT_ROOT  output=$OUTPUT_ROOT" >&2
# -------------------------------------------------------------------------

# ===== dependency checks =====
if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required (mac: brew install jq / Ubuntu: sudo apt-get install jq)" >&2
  exit 1
fi

if [[ -z "${OPENAI_API_KEY:-}" ]]; then
  echo "OPENAI_API_KEY is not set. Run: export OPENAI_API_KEY='...'" >&2
  exit 1
fi

mkdir -p "$OUTPUT_ROOT"
[[ -f "$CSV" ]] || echo "file,file_id,batch_id,status" > "$CSV"

# ===== helpers =====
json_get () { jq -r "$2" <<<"$1"; }

is_capacity_error_body () {
  # OpenAI queue/rate-limit/body wording may vary. Keep this deliberately broad.
  jq -er '
    tostring
    | ascii_downcase
    | test("rate limit|rate_limit|429|enqueued|queue|quota|too many|tokens.*limit|limit.*tokens")
  ' >/dev/null 2>&1
}

curl_json () { # $1:METHOD $2:URL [$3:DATA]
  local method="$1"; shift
  local url="$1"; shift
  local data="${1-}"

  local attempt=0 max_attempts=3 backoff=2 http_code
  while :; do
    if [[ "$method" == "GET" ]]; then
      http_code=$(curl -sS -w "%{http_code}" "$url" \
        -H "Authorization: Bearer $OPENAI_API_KEY" -o "$TMP_RESP") || http_code=$?
    else
      http_code=$(curl -sS -w "%{http_code}" "$url" \
        -H "Authorization: Bearer $OPENAI_API_KEY" \
        -H "Content-Type: application/json" \
        -d "$data" -o "$TMP_RESP") || http_code=$?
    fi

    if [[ "$http_code" == "200" ]]; then
      cat "$TMP_RESP"
      return 0
    fi

    if [[ "$http_code" == "429" ]]; then
      echo "OpenAI returned 429; the batch queue or rate limit is likely full." >&2
      cat "$TMP_RESP" >&2
      return 75
    fi

    # Short retry for transient 5xx only.
    if [[ "$http_code" =~ ^5[0-9][0-9]$ && $attempt -lt $max_attempts ]]; then
      attempt=$((attempt+1))
      sleep "$backoff"
      backoff=$((backoff*2))
      continue
    fi

    echo "HTTP $http_code while calling: $url" >&2
    cat "$TMP_RESP" >&2
    return 1
  done
}

curl_upload_file () { # $1:file
  local f="$1"
  local http_code attempt=0 max_attempts=3 backoff=2

  while :; do
    http_code=$(curl -sS -w "%{http_code}" https://api.openai.com/v1/files \
      -H "Authorization: Bearer $OPENAI_API_KEY" \
      -F purpose="batch" \
      -F file="@$f" \
      -o "$TMP_RESP") || http_code=$?

    if [[ "$http_code" == "200" ]]; then
      cat "$TMP_RESP"
      return 0
    fi

    if [[ "$http_code" == "429" ]]; then
      echo "429 during file upload; pausing further uploads." >&2
      cat "$TMP_RESP" >&2
      return 75
    fi

    if [[ "$http_code" =~ ^5[0-9][0-9]$ && $attempt -lt $max_attempts ]]; then
      attempt=$((attempt+1))
      sleep "$backoff"
      backoff=$((backoff*2))
      continue
    fi

    echo "HTTP $http_code while uploading file: $f" >&2
    cat "$TMP_RESP" >&2
    return 1
  done
}

validate_jsonl () { # $1:file
  local f="$1"
  [[ -f "$f" ]] || { echo "Skip: $f not found" >&2; return 2; }
  if ! jq -c . < "$f" >/dev/null 2>&1; then
    echo "Invalid JSONL: $f (each line must be a standalone JSON object)" >&2
    return 3
  fi
  local url_in
  url_in=$(head -n1 "$f" | jq -r '.url? // empty')
  if [[ "$url_in" != "$ENDPOINT" ]]; then
    echo "URL mismatch: $f carries '${url_in:-<empty>}' but ENDPOINT is '$ENDPOINT'" >&2
    return 4
  fi
  return 0
}

lookup_csv () { # $1:file -> "file_id,batch_id,status" or empty
  [[ -f "$CSV" ]] || return 0
  awk -F, -v f="$1" 'NR>1 && $1==f {print $2","$3","$4}' "$CSV" | tail -n1
}

upsert_csv () { # $1:file $2:file_id $3:batch_id $4:status
  [[ -f "$CSV" ]] || echo "file,file_id,batch_id,status" > "$CSV"
  local tmp; tmp=$(mktemp)
  awk -F, -v f="$1" 'BEGIN{OFS=","} NR==1{print; next} $1!=f {print}' "$CSV" > "$tmp"
  mv "$tmp" "$CSV"
  echo "$1,$2,$3,$4" >> "$CSV"
}

create_batch_from_file_id () { # $1:local_file $2:file_id -> echo "batch_id file_id"
  local f="$1"
  local file_id="$2"

  local payload batch_json rc batch_id status
  payload=$(jq -nc --arg fid "${file_id}" --arg ep "$ENDPOINT" --arg cw "$COMPLETION_WINDOW" \
    '{input_file_id:$fid, endpoint:$ep, completion_window:$cw}')

  set +e
  batch_json=$(curl_json POST "https://api.openai.com/v1/batches" "$payload")
  rc=$?
  set -e

  if [[ $rc -eq 75 ]]; then
    upsert_csv "$f" "${file_id}" "NA" "queue_full_after_file_upload"
    return 75
  elif [[ $rc -ne 0 ]]; then
    echo "Batch creation failed: $f" >&2
    upsert_csv "$f" "${file_id}" "NA" "create_failed"
    return 0
  fi

  # Some non-429 errors may still encode queue/limit semantics.
  if is_capacity_error_body <<<"$batch_json"; then
    upsert_csv "$f" "${file_id}" "NA" "queue_full_after_file_upload"
    return 75
  fi

  batch_id=$(json_get "$batch_json" '.id')
  status=$(json_get "$batch_json" '.status')

  if [[ -z "${batch_id}" || "${batch_id}" == "null" ]]; then
    echo "Batch creation failed (no batch_id): $f" >&2
    echo "$batch_json" >&2
    upsert_csv "$f" "${file_id}" "NA" "create_failed"
    return 0
  fi

  echo "Created batch_id=${batch_id}  status=$status" >&2
  upsert_csv "$f" "${file_id}" "${batch_id}" "$status"
  echo "${batch_id} ${file_id}"
}

create_batch () { # $1:file [$2:existing_file_id] -> echo "batch_id file_id"; rc 75 means capacity reached
  local f="$1"
  local existing_file_id="${2:-}"
  local upload_json file_id rc

  if [[ -n "$existing_file_id" && "$existing_file_id" != "NA" && "$existing_file_id" != "null" ]]; then
    echo "Reusing uploaded file_id=$existing_file_id: $f" >&2
    create_batch_from_file_id "$f" "$existing_file_id"
    return $?
  fi

  echo "Uploading file: $f" >&2
  set +e
  upload_json=$(curl_upload_file "$f")
  rc=$?
  set -e

  if [[ $rc -eq 75 ]]; then
    return 75
  elif [[ $rc -ne 0 ]]; then
    echo "Upload failed: $f" >&2
    upsert_csv "$f" "" "NA" "upload_failed"
    return 0
  fi

  file_id=$(json_get "$upload_json" '.id')
  if [[ -z "${file_id}" || "${file_id}" == "null" ]]; then
    echo "Upload failed (no file_id): $f" >&2
    echo "$upload_json" >&2
    upsert_csv "$f" "" "NA" "upload_failed"
    return 0
  fi

  create_batch_from_file_id "$f" "${file_id}"
}

print_error_brief () { # $1:batch_id $2:file
  local meta err_id
  meta=$(curl_json GET "https://api.openai.com/v1/batches/$1") || return 0
  err_id=$(json_get "$meta" '.error_file_id')
  if [[ -n "$err_id" && "$err_id" != "null" ]]; then
    echo "Error summary ($2):" >&2
    curl -sS "https://api.openai.com/v1/files/$err_id/content" \
      -H "Authorization: Bearer $OPENAI_API_KEY" | head -n 100 >&2
  fi
}

download_outputs () { # $1:batch_id $2:file $3:output_dir
  local meta out_id err_id base output_dir
  output_dir="$3"
  mkdir -p "$output_dir"

  meta=$(curl_json GET "https://api.openai.com/v1/batches/$1") || return 0
  out_id=$(json_get "$meta" '.output_file_id')
  err_id=$(json_get "$meta" '.error_file_id')

  base="${2##*/}"
  base="${base%.*}"

  if [[ -n "$out_id" && "$out_id" != "null" ]]; then
    echo "Downloading output: $out_id -> $output_dir/${base}_output.jsonl" >&2
    curl -sS "https://api.openai.com/v1/files/$out_id/content" \
      -H "Authorization: Bearer $OPENAI_API_KEY" -o "$output_dir/${base}_output.jsonl"
  fi
  if [[ -n "$err_id" && "$err_id" != "null" ]]; then
    echo "Downloading error detail: $err_id -> $output_dir/${base}_errors.jsonl" >&2
    curl -sS "https://api.openai.com/v1/files/$err_id/content" \
      -H "Authorization: Bearer $OPENAI_API_KEY" -o "$output_dir/${base}_errors.jsonl"
  fi
}

submit_one_file () { # $1:file $2:output_dir -> prints "batch_id|file|output_dir"; rc 75 means queue full
  local file="$1" out_dir="$2"
  echo "========== submitting ${file} ==========" >&2

  if ! validate_jsonl "${file}"; then
    upsert_csv "${file}" "" "NA" "invalid_json_or_url"
    echo "Skip (pre-check failed): ${file}" >&2
    return 0
  fi

  local line="" file_id="" batch_id="" prev_status=""
  line="$(lookup_csv "${file}" || true)"
  if [[ -n "$line" ]]; then
    file_id="$(cut -d, -f1 <<<"$line")"
    batch_id="$(cut -d, -f2 <<<"$line")"
    prev_status="$(cut -d, -f3 <<<"$line")"
  fi

  case "${prev_status}" in
    failed|expired|cancelled|create_failed)
      echo "Previous result was ${prev_status}; resubmitting ${file}" >&2
      batch_id=""
      ;;
    completed)
      if [[ -n "${batch_id}" && "${batch_id}" != "NA" ]]; then
        echo "Reusing finished batch, will check and download later: ${batch_id}" >&2
        echo "${batch_id}|${file}|${out_dir}"
        return 0
      fi
      ;;
    queue_full_after_file_upload)
      echo "Creating a batch from the already uploaded file_id: ${file_id}" >&2
      ;;
    *)
      if [[ -n "${batch_id}" && "${batch_id}" != "NA" ]]; then
        echo "Reusing existing batch: ${batch_id}" >&2
        echo "${batch_id}|${file}|${out_dir}"
        return 0
      fi
      ;;
  esac

  local created rc
  set +e
  created="$(create_batch "${file}" "${file_id}")"
  rc=$?
  set -e

  if [[ $rc -eq 75 ]]; then
    return 75
  fi

  if [[ -n "$created" ]]; then
    read -r batch_id file_id <<< "$created"
    echo "${batch_id}|${file}|${out_dir}"
  fi
  return 0
}

poll_active_once () { # uses ACTIVE_* indexed arrays; updates DONE_COUNT
  local idx bid f od status_json status rc
  local -a NEW_BIDS=()
  local -a NEW_FILES=()
  local -a NEW_OUTDIRS=()

  if [[ ${#ACTIVE_BIDS[@]} -eq 0 ]]; then
    return 0
  fi

  echo "Polling ${#ACTIVE_BIDS[@]} active batch(es)..." >&2

  for idx in "${!ACTIVE_BIDS[@]}"; do
    bid="${ACTIVE_BIDS[$idx]}"
    f="${ACTIVE_FILES[$idx]}"
    od="${ACTIVE_OUTDIRS[$idx]}"

    set +e
    status_json=$(curl_json GET "https://api.openai.com/v1/batches/$bid")
    rc=$?
    set -e

    if [[ $rc -ne 0 ]]; then
      echo "Status query failed, keeping for now: $bid" >&2
      NEW_BIDS+=("$bid")
      NEW_FILES+=("$f")
      NEW_OUTDIRS+=("$od")
      continue
    fi

    status=$(json_get "$status_json" '.status')
    echo "  $bid -> $status" >&2

    case "$status" in
      completed|failed|expired|cancelled)
        # Preserve file_id if already known.
        local old_line old_file_id
        old_line="$(lookup_csv "$f" || true)"
        old_file_id="$(cut -d, -f1 <<<"$old_line")"
        upsert_csv "$f" "$old_file_id" "$bid" "$status"

        if [[ "$status" == "completed" ]]; then
          download_outputs "$bid" "$f" "$od"
        else
          print_error_brief "$bid" "$f"
        fi

        DONE_COUNT=$((DONE_COUNT + 1))
        echo "Completed ${DONE_COUNT}/${TOTAL}" >&2
        ;;
      *)
        NEW_BIDS+=("$bid")
        NEW_FILES+=("$f")
        NEW_OUTDIRS+=("$od")
        ;;
    esac
  done

  ACTIVE_BIDS=("${NEW_BIDS[@]}")
  ACTIVE_FILES=("${NEW_FILES[@]}")
  ACTIVE_OUTDIRS=("${NEW_OUTDIRS[@]}")
}

# ===== main =====
shopt -s nullglob

if [[ ! -d "$INPUT_ROOT" ]]; then
  echo "INPUT_ROOT does not exist or is not a directory: $INPUT_ROOT" >&2
  exit 1
fi

# collect the project list (macOS Bash 3.2 safe; avoids negative/empty array subscripts)
projects=()
if [[ "$PROJECT_FILTER" != "all" ]]; then
  # avoid read -a and ${!array[@]}: they trigger bad array subscript in some bash builds
  filter_tmp="$PROJECT_FILTER,"
  while [[ -n "$filter_tmp" ]]; do
    p="${filter_tmp%%,*}"
    filter_tmp="${filter_tmp#*,}"
    # trim all whitespace in project name
    p="${p//[[:space:]]/}"
    [[ -z "$p" ]] && continue
    if [[ ! -d "$INPUT_ROOT/$p" ]]; then
      echo "Project does not exist under $INPUT_ROOT/: $p" >&2
      exit 1
    fi
    projects+=("$p")
  done
else
  for proj_dir in "$INPUT_ROOT"/*/; do
    [[ -d "$proj_dir" ]] || continue
    project_name_tmp="${proj_dir%/}"
    project_name_tmp="${project_name_tmp##*/}"
    [[ -z "$project_name_tmp" ]] && continue
    projects+=("$project_name_tmp")
  done
fi

# collect every file to submit
declare -a QUEUE=()
for project_name in "${projects[@]}"; do
  [[ -z "$project_name" ]] && continue
  proj_dir="$INPUT_ROOT/$project_name"
  out_dir="$OUTPUT_ROOT/$project_name"
  mkdir -p "${out_dir}"

  files=( "$proj_dir"/requests*.jsonl "$proj_dir"/requests*.json )
  if ((${#files[@]} == 0)); then
    echo "Skip: no requests*.jsonl/json under $project_name" >&2
    continue
  fi

  echo "🧩 Project: $project_name (${#files[@]} files)" >&2
  for file in "${files[@]}"; do
    QUEUE+=("${file}|${out_dir}")
  done
done

TOTAL=${#QUEUE[@]}
echo "" >&2
if [[ "${MAX_ACTIVE}" == "0" ]]; then
  echo "${TOTAL} file(s), MAX_ACTIVE=0 (submit until the OpenAI queue limit), starting..." >&2
else
  echo "${TOTAL} file(s), MAX_ACTIVE=${MAX_ACTIVE}, starting sliding-window submission..." >&2
fi

ACTIVE_BIDS=()     # indexed arrays for macOS bash 3.2 compatibility
ACTIVE_FILES=()
ACTIVE_OUTDIRS=()
QUEUE_INDEX=0
DONE_COUNT=0
capacity_waited=0

while [[ ${QUEUE_INDEX} -lt ${TOTAL} || ${#ACTIVE_BIDS[@]} -gt 0 ]]; do
  capacity_reached=0
  submitted_this_round=0

  # 1. submit as many new batches as allowed:
  #    - MAX_ACTIVE=0: keep going until OpenAI answers 429 / queue limit
  #    - MAX_ACTIVE>0: stop at the local window size
  while [[ ${QUEUE_INDEX} -lt ${TOTAL} ]]; do
    if [[ "${MAX_ACTIVE}" != "0" && ${#ACTIVE_BIDS[@]} -ge ${MAX_ACTIVE} ]]; then
      break
    fi

    IFS='|' read -r file outdir <<< "${QUEUE[${QUEUE_INDEX}]}"

    set +e
    entry="$(submit_one_file "${file}" "$outdir")"
    rc=$?
    set -e

    if [[ $rc -eq 75 ]]; then
      capacity_reached=1
      echo "Submission limit reached; waiting for running batches to free capacity." >&2
      break
    fi

    # rc != 75 means this queue item has been processed: submitted/reused/skipped.
    QUEUE_INDEX=$((QUEUE_INDEX + 1))

    if [[ -n "${entry:-}" ]]; then
      IFS='|' read -r bid f od <<< "$entry"
      ACTIVE_BIDS+=("$bid")
      ACTIVE_FILES+=("$f")
      ACTIVE_OUTDIRS+=("$od")
      submitted_this_round=$((submitted_this_round + 1))
      if [[ "${MAX_ACTIVE}" == "0" ]]; then
        echo "Processed ${QUEUE_INDEX}/${TOTAL}, active ${#ACTIVE_BIDS[@]} (auto-fill mode)" >&2
      else
        echo "Processed ${QUEUE_INDEX}/${TOTAL}, active ${#ACTIVE_BIDS[@]}/${MAX_ACTIVE}" >&2
      fi
    fi
  done

  # 2. poll every active batch; download on completion to free capacity
  poll_active_once

  # 3. avoid spinning: wait before topping the queue up again
  if [[ ${QUEUE_INDEX} -lt ${TOTAL} || ${#ACTIVE_BIDS[@]} -gt 0 ]]; then
    if [[ $capacity_reached -eq 1 ]]; then
      capacity_waited=$((capacity_waited + POLL_SECONDS))
      if (( capacity_waited >= MAX_WAIT_SECONDS )); then
        echo "Waited longer than MAX_WAIT_SECONDS=$MAX_WAIT_SECONDS for queue capacity; stopping this run." >&2
        echo "   Re-run the script later; it reuses the file_id / batch_id recorded in $CSV." >&2
        break
      fi
    else
      capacity_waited=0
    fi
    sleep "$POLL_SECONDS"
  fi
done

echo "Finished. CSV: $CSV, results under $OUTPUT_ROOT/<project>/" >&2
