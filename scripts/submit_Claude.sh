#!/usr/bin/env bash
set -euo pipefail

# ---- merged from the four per-setting submit.sh scripts ------------------
# This script lives at <repo>/scripts/submit_Claude.sh
_SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$_SCRIPT_DIR/.." && pwd)"
SETTING=""          # baseline | exm1 | exm2 | exm3, required via --setting
# -------------------------------------------------------------------------


# Claude Message Batches generally requires the beta query parameter and header
API_BASE="${API_BASE:-https://api.anthropic.com}"
BETA_QUERY="?beta=true"
ANTHROPIC_VERSION="${ANTHROPIC_VERSION:-2023-06-01}"
ANTHROPIC_BETA="${ANTHROPIC_BETA:-message-batches-2024-09-24}"

POLL_SECONDS="${POLL_SECONDS:-20}"
MAX_WAIT_SECONDS="${MAX_WAIT_SECONDS:-86400}"   # 24h

TMP_RESP="${TMP_RESP:-.tmp_resp.json}"

PROJECT_FILTER="all"

# ========== argument parsing ==========
# Usage:
#   bash submit_Claude.sh --setting exm3                    # all projects
#   bash submit_Claude.sh --setting exm3 --project Time     # only Time
#   bash submit_Claude.sh --setting exm3 --project Time,Web # several, comma separated
while [[ $# -gt 0 ]]; do
  case "$1" in
    --setting|-s)
      SETTING="${2:-}"
      [[ -n "$SETTING" ]] || { echo "Missing argument: --setting <baseline|exm1|exm2|exm3>" >&2; exit 1; }
      shift 2
      ;;
    --project|-p)
      PROJECT_FILTER="${2:-}"
      [[ -n "$PROJECT_FILTER" ]] || { echo "Missing argument: --project <name|name1,name2|all>" >&2; exit 1; }
      shift 2
      ;;
    --help|-h)
      cat >&2 <<EOF
Usage:
  bash $0 --setting exm3                    # all projects (default)
  bash $0 --setting exm3 --project Time     # a single project
  bash $0 --setting exm3 --project Time,Web # several, comma separated
Environment variables:
  INPUT_ROOT        (default <repo>/generated_requests/<setting>/Claude)
  OUTPUT_ROOT       (default <repo>/generated_outputs/<setting>/Claude)
  API_BASE          (default https://api.anthropic.com)
  ANTHROPIC_VERSION (default 2023-06-01)
  ANTHROPIC_BETA    (default message-batches-2024-09-24)
  POLL_SECONDS      (default 20)
  MAX_WAIT_SECONDS  (default 86400)
  CSV               (default <OUTPUT_ROOT>/batches.csv)
  TMP_RESP          (default .tmp_resp.json)
Arguments:
  --setting, -s     baseline | exm1 | exm2 | exm3   (required)
  --project, -p     one project, or several comma separated (default all)
  --help, -h        show this message
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

INPUT_ROOT="${INPUT_ROOT:-$REPO_ROOT/generated_requests/$SETTING/Claude}"
OUTPUT_ROOT="${OUTPUT_ROOT:-$REPO_ROOT/generated_outputs/$SETTING/Claude}"
CSV="${CSV:-$OUTPUT_ROOT/batches.csv}"
mkdir -p "$OUTPUT_ROOT"
echo "🧭 setting=$SETTING  input=$INPUT_ROOT  output=$OUTPUT_ROOT" >&2
# -------------------------------------------------------------------------

# ===== dependency checks =====
command -v jq >/dev/null 2>&1 || { echo "jq is required (mac: brew install jq / Ubuntu: sudo apt-get install jq)" >&2; exit 1; }

if [[ -z "${ANTHROPIC_API_KEY:-}" ]]; then
  echo "ANTHROPIC_API_KEY is not set. Run: export ANTHROPIC_API_KEY='...'" >&2
  exit 1
fi
  
mkdir -p "$OUTPUT_ROOT"
[[ -f "$CSV" ]] || echo "file,batch_id,processing_status,results_url" > "$CSV"

# ===== helpers =====
json_get () { jq -r "$2" <<<"$1"; }

curl_json () { # $1:METHOD $2:URL [$3:DATA]
  local method="$1"; shift
  local url="$1"; shift
  local data="${1-}"

  local attempt=0 max_attempts=5 backoff=2 http_code
  while :; do
    if [[ "$method" == "GET" ]]; then
      http_code=$(curl -sS -w "%{http_code}" "$url" \
        -H "x-api-key: $ANTHROPIC_API_KEY" \
        -H "anthropic-version: $ANTHROPIC_VERSION" \
        -H "anthropic-beta: $ANTHROPIC_BETA" \
        -o "$TMP_RESP") || http_code=$?
    else
      http_code=$(curl -sS -w "%{http_code}" "$url" \
        -H "x-api-key: $ANTHROPIC_API_KEY" \
        -H "anthropic-version: $ANTHROPIC_VERSION" \
        -H "anthropic-beta: $ANTHROPIC_BETA" \
        -H "content-type: application/json" \
        -d "$data" -o "$TMP_RESP") || http_code=$?
    fi

    # Claude may answer 200 or 201; accept both
    if [[ "$http_code" == "200" || "$http_code" == "201" ]]; then
      cat "$TMP_RESP"
      return 0
    fi

    # retry on rate limiting
    if [[ "$http_code" == "429" && $attempt -lt $max_attempts ]]; then
      attempt=$((attempt+1)); sleep "$backoff"; backoff=$((backoff*2)); continue
    fi

    # 5xx is retryable
    if [[ "$http_code" =~ ^5 && $attempt -lt $max_attempts ]]; then
      attempt=$((attempt+1)); sleep "$backoff"; backoff=$((backoff*2)); continue
    fi

    echo "HTTP $http_code while calling: $url" >&2
    cat "$TMP_RESP" >&2
    return 1
  done
}

curl_results_to_file () { # $1:RESULTS_URL $2:OUTFILE
  local url="$1" out="$2"
  curl -sS "$url" \
    -H "x-api-key: $ANTHROPIC_API_KEY" \
    -H "anthropic-version: $ANTHROPIC_VERSION" \
    -H "anthropic-beta: $ANTHROPIC_BETA" \
    -o "$out"
}

upsert_csv () { # $1:file $2:batch_id $3:status $4:results_url
  [[ -f "$CSV" ]] || echo "file,batch_id,processing_status,results_url" > "$CSV"
  local tmp; tmp=$(mktemp)
  awk -F, -v f="$1" 'BEGIN{OFS=","} NR==1{print; next} $1!=f {print}' "$CSV" > "$tmp"
  mv "$tmp" "$CSV"
  echo "$1,$2,$3,$4" >> "$CSV"
}

lookup_csv () { # $1:file -> "batch_id,processing_status,results_url" or empty
  [[ -f "$CSV" ]] || return 0
  awk -F, -v f="$1" 'NR>1 && $1==f {print $2","$3","$4}' "$CSV" | tail -n1
}

validate_batch_json () { # $1:file
  local f="$1"
  [[ -f "$f" ]] || { echo "Skip: $f not found" >&2; return 2; }

  # must be valid JSON
  if ! jq -e . "$f" >/dev/null 2>&1; then
    echo "Invalid JSON: $f" >&2
    return 3
  fi

  # must carry a requests array
  if ! jq -e '.requests and (.requests|type=="array") and (.requests|length>0)' "$f" >/dev/null 2>&1; then
    echo "Bad format: $f must contain a non-empty requests array" >&2
    return 4
  fi

  # every entry needs custom_id and params (minimal check)
  if ! jq -e '.requests[] | has("custom_id") and has("params")' "$f" >/dev/null 2>&1; then
    echo "Missing fields in requests: custom_id and params are required: $f" >&2
    return 5
  fi

  return 0
}

create_batch () { # $1:file -> echo "batch_id"
  local f="$1"
  echo "Creating Message Batch: $f" >&2

  local payload resp batch_id status results_url
  payload="$(cat "$f")"

  resp="$(curl_json POST "$API_BASE/v1/messages/batches$BETA_QUERY" "$payload")" || return 1

  batch_id="$(json_get "$resp" '.id')"
  status="$(json_get "$resp" '.processing_status // empty')"
  results_url="$(json_get "$resp" '.results_url // empty')"

  if [[ -z "$batch_id" || "$batch_id" == "null" ]]; then
    echo "Creation failed (no batch_id): $f" >&2
    echo "$resp" >&2
    return 1
  fi

  echo "Created batch_id=$batch_id  processing_status=${status:-<unknown>}" >&2
  upsert_csv "$f" "$batch_id" "${status:-created}" "${results_url:-}"
  echo "$batch_id"
}

wait_terminal () { # $1:batch_id -> echo "final_status results_url"
  local b_id="$1" waited=0 resp status results_url

  while :; do
    resp="$(curl_json GET "$API_BASE/v1/messages/batches/$b_id$BETA_QUERY")" || {
      echo "Failed to fetch status: $b_id" >&2
      return 1
    }

    status="$(json_get "$resp" '.processing_status')"
    results_url="$(json_get "$resp" '.results_url // empty')"

    echo "Status: $b_id -> $status" >&2

    case "$status" in
      ended)
        echo "$status $results_url"
        return 0
        ;;
      in_progress|canceling|"")
        sleep "$POLL_SECONDS"
        waited=$((waited+POLL_SECONDS))
        if (( waited >= MAX_WAIT_SECONDS )); then
          echo "Wait timed out after $MAX_WAIT_SECONDS s" >&2
          echo "$status $results_url"
          return 0
        fi
        ;;
      *)
        # treat an unknown status as terminal too
        echo "$status $results_url"
        return 0
        ;;
    esac
  done
}

print_error_brief_from_results () { # $1:results_jsonl $2:file_label
  local results="$1" label="$2"
  [[ -f "$results" ]] || return 0

  # Claude results carry one JSON object per line; failures show .error or result.type=errored
  # best-effort summary: prefer lines with .error, else lines with result.type == "errored"
  local n
  n="$(jq -c 'select(.error != null) | {custom_id, error}' "$results" 2>/dev/null | head -n 20 | wc -l | tr -d ' ')"
  if [[ "$n" != "0" ]]; then
    echo "Error summary ($label) - first 20 .error entries:" >&2
    jq -c 'select(.error != null) | {custom_id, error}' "$results" 2>/dev/null | head -n 20 >&2
    return 0
  fi

  n="$(jq -c 'select(.result.type? == "errored") | {custom_id, result}' "$results" 2>/dev/null | head -n 20 | wc -l | tr -d ' ')"
  if [[ "$n" != "0" ]]; then
    echo "Error summary ($label) - first 20 result.type==errored entries:" >&2
    jq -c 'select(.result.type? == "errored") | {custom_id, result}' "$results" 2>/dev/null | head -n 20 >&2
  fi
}

download_results () { # $1:batch_id $2:file
  local b_id="$1" file="$2"
  local meta results_url base out_results

  meta="$(curl_json GET "$API_BASE/v1/messages/batches/$b_id$BETA_QUERY")" || return 1
  results_url="$(json_get "$meta" '.results_url // empty')"

  if [[ -z "$results_url" || "$results_url" == "null" ]]; then
    # fallback: hit the results endpoint directly when results_url is absent
    results_url="$API_BASE/v1/messages/batches/$b_id/results$BETA_QUERY"
  fi

  base="${file##*/}"; base="${base%.*}"
  out_results="$OUTPUT_DIR/${base}_output.jsonl"

  echo "Downloading results: $results_url -> $out_results" >&2
  curl_results_to_file "$results_url" "$out_results"

  # summarise any errors
  print_error_brief_from_results "$out_results" "$file"
}

process_one_file () { # $1:file
  local file="$1"
  echo "========== processing $file ==========" >&2

  if ! validate_batch_json "$file"; then
    upsert_csv "$file" "NA" "invalid_json" ""
    echo "Skip (pre-check failed): $file" >&2
    return 0
  fi

  local line batch_id status results_url
  line="$(lookup_csv "$file" || true)"
  batch_id=""

  if [[ -n "$line" ]]; then
    batch_id="${line%%,*}"
    [[ -n "$batch_id" && "$batch_id" != "NA" ]] && echo "Reusing existing batch: $batch_id" >&2
  fi

  if [[ -z "$batch_id" || "$batch_id" == "NA" ]]; then
    if batch_id="$(create_batch "$file")"; then :; else
      upsert_csv "$file" "NA" "create_failed" ""
      echo "Skip (creation failed): $file" >&2
      return 0
    fi
  fi

  if [[ -z "$batch_id" || "$batch_id" == "null" ]]; then
    upsert_csv "$file" "NA" "empty_batch_id" ""
    echo "Skip (empty batch_id): $file" >&2
    return 0
  fi

  local wait_out final_status final_results_url
  if wait_out="$(wait_terminal "$batch_id")"; then
    final_status="${wait_out%% *}"
    final_results_url="${wait_out#* }"
    [[ "$final_results_url" == "$final_status" ]] && final_results_url=""
    echo "Terminal status: $final_status" >&2
  else
    upsert_csv "$file" "$batch_id" "poll_failed" ""
    echo "Skip (polling failed): $file" >&2
    return 0
  fi

  upsert_csv "$file" "$batch_id" "$final_status" "$final_results_url"

  if [[ "$final_status" == "ended" ]]; then
    download_results "$batch_id" "$file"
  else
    echo "Not finished (status=$final_status), moving on" >&2
  fi
}

# ===== main =====
shopt -s nullglob

if [[ ! -d "$INPUT_ROOT" ]]; then
  echo "INPUT_ROOT does not exist or is not a directory: $INPUT_ROOT" >&2
  exit 1
fi

process_project_dir () { # $1:proj_dir $2:project_name
  local proj_dir="$1" project_name="$2"
  OUTPUT_DIR="$OUTPUT_ROOT/$project_name"
  mkdir -p "$OUTPUT_DIR"

  echo "==============================" >&2
  echo "🧩 Project: $project_name" >&2
  echo "  input:  $proj_dir/batch*.json" >&2
  echo "  output: $OUTPUT_DIR/" >&2
  echo "==============================" >&2

  local files
  files=( "$proj_dir"/batch*.json )
  if ((${#files[@]} == 0)); then
    # add other patterns here if the generator names payloads differently
    echo "Skip: no batch*.json under $project_name" >&2
    return 0
  fi

  local f
  for f in "${files[@]}"; do
    process_one_file "$f"
  done
}

# run only the requested projects (comma separated)
if [[ "$PROJECT_FILTER" != "all" ]]; then
  IFS=',' read -r -a projects <<< "$PROJECT_FILTER"
  for i in "${!projects[@]}"; do projects[$i]="${projects[$i]//[[:space:]]/}"; done

  local_missing=()
  for project_name in "${projects[@]}"; do
    [[ -z "$project_name" ]] && continue
    proj_dir="$INPUT_ROOT/$project_name"
    [[ -d "$proj_dir" ]] || local_missing+=("$project_name")
  done

  if ((${#local_missing[@]} > 0)); then
    echo "These projects do not exist under $INPUT_ROOT/: ${local_missing[*]}" >&2
    echo "   available projects:" >&2
    ls -1 "$INPUT_ROOT" >&2
    exit 1
  fi

  for project_name in "${projects[@]}"; do
    [[ -z "$project_name" ]] && continue
    process_project_dir "$INPUT_ROOT/$project_name" "$project_name"
  done

  echo "DONE: ${projects[*]}. CSV: $CSV, output under $OUTPUT_ROOT/<project>/" >&2
  exit 0
fi

# run every project
echo "Scanning all projects: $INPUT_ROOT/*" >&2
for proj_dir in "$INPUT_ROOT"/*; do
  [[ -d "$proj_dir" ]] || continue
  project_name="${proj_dir##*/}"
  process_project_dir "$proj_dir" "$project_name"
done

echo "All projects processed. Manifest: $CSV, results under $OUTPUT_ROOT/<project>/" >&2