#!/usr/bin/env bash
set -euo pipefail

INPUT_ROOT="${INPUT_ROOT:-z_requests}"
OUTPUT_ROOT="${OUTPUT_ROOT:-z_output}"
: "${ANTHROPIC_API_KEY:?set ANTHROPIC_API_KEY in your environment before running this script}"

# Claude Message Batches 目前通常需要 beta 参数与 header
API_BASE="${API_BASE:-https://api.anthropic.com}"
BETA_QUERY="?beta=true"
ANTHROPIC_VERSION="${ANTHROPIC_VERSION:-2023-06-01}"
ANTHROPIC_BETA="${ANTHROPIC_BETA:-message-batches-2024-09-24}"

POLL_SECONDS="${POLL_SECONDS:-20}"
MAX_WAIT_SECONDS="${MAX_WAIT_SECONDS:-86400}"   # 24h

CSV="${CSV:-batches_claude.csv}"
TMP_RESP="${TMP_RESP:-.tmp_resp.json}"

PROJECT_FILTER="all"

# ========== 参数解析 ==========
# 用法：
#   bash run_batches_claude.sh                       # 跑全部项目
#   bash run_batches_claude.sh --project Time        # 只跑 Time
#   bash run_batches_claude.sh --project Time,Web    # 跑多个项目（逗号分隔）
while [[ $# -gt 0 ]]; do
  case "$1" in
    --project|-p)
      PROJECT_FILTER="${2:-}"
      [[ -n "$PROJECT_FILTER" ]] || { echo "❌ 缺少参数：--project <name|name1,name2|all>" >&2; exit 1; }
      shift 2
      ;;
    --help|-h)
      cat >&2 <<EOF
用法：
  bash $0                       # 跑全部项目（默认）
  bash $0 --project Time        # 只跑某个项目
  bash $0 --project Time,Web    # 跑多个项目（逗号分隔）
环境变量：
  INPUT_ROOT        (默认 z_requests)
  OUTPUT_ROOT       (默认 z_output)
  API_BASE          (默认 https://api.anthropic.com)
  ANTHROPIC_VERSION (默认 2023-06-01)
  ANTHROPIC_BETA    (默认 message-batches-2024-09-24)
  POLL_SECONDS      (默认 20)
  MAX_WAIT_SECONDS  (默认 86400)
  CSV               (默认 batches_claude.csv)
EOF
      exit 0
      ;;
    *)
      echo "❌ 未知参数：$1（用 --help 查看用法）" >&2
      exit 1
      ;;
  esac
done

# ===== 依赖检查 =====
command -v jq >/dev/null 2>&1 || { echo "❌ 需要 jq，请先安装（mac: brew install jq / Ubuntu: sudo apt-get install jq）" >&2; exit 1; }

if [[ -z "${ANTHROPIC_API_KEY:-}" ]]; then
  echo "❌ 未设置 ANTHROPIC_API_KEY。请先执行：export ANTHROPIC_API_KEY='...'" >&2
  exit 1
fi
  
mkdir -p "$OUTPUT_ROOT"
[[ -f "$CSV" ]] || echo "file,batch_id,processing_status,results_url" > "$CSV"

# ===== 工具函数 =====
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

    # Claude 正常可能返回 200/201，这里都放行
    if [[ "$http_code" == "200" || "$http_code" == "201" ]]; then
      cat "$TMP_RESP"
      return 0
    fi

    # 限流重试
    if [[ "$http_code" == "429" && $attempt -lt $max_attempts ]]; then
      attempt=$((attempt+1)); sleep "$backoff"; backoff=$((backoff*2)); continue
    fi

    # 5xx 可重试（可选）
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

lookup_csv () { # $1:file -> "batch_id,processing_status,results_url" 或空
  [[ -f "$CSV" ]] || return 0
  awk -F, -v f="$1" 'NR>1 && $1==f {print $2","$3","$4}' "$CSV" | tail -n1
}

validate_batch_json () { # $1:file
  local f="$1"
  [[ -f "$f" ]] || { echo "跳过：找不到 $f" >&2; return 2; }

  # 必须是合法 JSON
  if ! jq -e . "$f" >/dev/null 2>&1; then
    echo "❌ JSON 非法：$f" >&2
    return 3
  fi

  # 必须有 requests 数组
  if ! jq -e '.requests and (.requests|type=="array") and (.requests|length>0)' "$f" >/dev/null 2>&1; then
    echo "❌ 格式不对：$f 需要包含非空的 requests 数组" >&2
    return 4
  fi

  # 每条必须有 custom_id 和 params（最小校验）
  if ! jq -e '.requests[] | has("custom_id") and has("params")' "$f" >/dev/null 2>&1; then
    echo "❌ requests 内元素缺字段：需要 custom_id 与 params：$f" >&2
    return 5
  fi

  return 0
}

create_batch () { # $1:file -> echo "batch_id"
  local f="$1"
  echo "🚀 创建 Message Batch：$f" >&2

  local payload resp batch_id status results_url
  payload="$(cat "$f")"

  resp="$(curl_json POST "$API_BASE/v1/messages/batches$BETA_QUERY" "$payload")" || return 1

  batch_id="$(json_get "$resp" '.id')"
  status="$(json_get "$resp" '.processing_status // empty')"
  results_url="$(json_get "$resp" '.results_url // empty')"

  if [[ -z "$batch_id" || "$batch_id" == "null" ]]; then
    echo "❌ 创建失败（未拿到 batch_id）：$f" >&2
    echo "$resp" >&2
    return 1
  fi

  echo "✅ 新建 batch_id=$batch_id  processing_status=${status:-<unknown>}" >&2
  upsert_csv "$f" "$batch_id" "${status:-created}" "${results_url:-}"
  echo "$batch_id"
}

wait_terminal () { # $1:batch_id -> echo "final_status results_url"
  local b_id="$1" waited=0 resp status results_url

  while :; do
    resp="$(curl_json GET "$API_BASE/v1/messages/batches/$b_id$BETA_QUERY")" || {
      echo "❌ 拉取状态失败：$b_id" >&2
      return 1
    }

    status="$(json_get "$resp" '.processing_status')"
    results_url="$(json_get "$resp" '.results_url // empty')"

    echo "状态：$b_id -> $status" >&2

    case "$status" in
      ended)
        echo "$status $results_url"
        return 0
        ;;
      in_progress|canceling|"")
        sleep "$POLL_SECONDS"
        waited=$((waited+POLL_SECONDS))
        if (( waited >= MAX_WAIT_SECONDS )); then
          echo "⚠️ 等待超时（$MAX_WAIT_SECONDS 秒）" >&2
          echo "$status $results_url"
          return 0
        fi
        ;;
      *)
        # 未知状态也当终态返回
        echo "$status $results_url"
        return 0
        ;;
    esac
  done
}

print_error_brief_from_results () { # $1:results_jsonl $2:file_label
  local results="$1" label="$2"
  [[ -f "$results" ]] || return 0

  # Claude results 每行一个 JSON 对象；失败行通常会带 error / 或 result.type=errored（以实际返回为准）
  # 这里做一个“尽力而为”的摘要：优先抓有 .error 的行，否则抓 result.type == "errored" 的行
  local n
  n="$(jq -c 'select(.error != null) | {custom_id, error}' "$results" 2>/dev/null | head -n 20 | wc -l | tr -d ' ')"
  if [[ "$n" != "0" ]]; then
    echo "🔎 错误摘要（$label）— 取前 20 条 .error：" >&2
    jq -c 'select(.error != null) | {custom_id, error}' "$results" 2>/dev/null | head -n 20 >&2
    return 0
  fi

  n="$(jq -c 'select(.result.type? == "errored") | {custom_id, result}' "$results" 2>/dev/null | head -n 20 | wc -l | tr -d ' ')"
  if [[ "$n" != "0" ]]; then
    echo "🔎 错误摘要（$label）— 取前 20 条 result.type==errored：" >&2
    jq -c 'select(.result.type? == "errored") | {custom_id, result}' "$results" 2>/dev/null | head -n 20 >&2
  fi
}

download_results () { # $1:batch_id $2:file
  local b_id="$1" file="$2"
  local meta results_url base out_results

  meta="$(curl_json GET "$API_BASE/v1/messages/batches/$b_id$BETA_QUERY")" || return 1
  results_url="$(json_get "$meta" '.results_url // empty')"

  if [[ -z "$results_url" || "$results_url" == "null" ]]; then
    # 兜底：直接打 results endpoint（如果 results_url 没给）
    results_url="$API_BASE/v1/messages/batches/$b_id/results$BETA_QUERY"
  fi

  base="${file##*/}"; base="${base%.*}"
  out_results="$OUTPUT_DIR/${base}_results.jsonl"

  echo "⬇️ 下载结果：$results_url -> $out_results" >&2
  curl_results_to_file "$results_url" "$out_results"

  # 如果有错误，给一个摘要
  print_error_brief_from_results "$out_results" "$file"
}

process_one_file () { # $1:file
  local file="$1"
  echo "========== 处理 $file ==========" >&2

  if ! validate_batch_json "$file"; then
    upsert_csv "$file" "NA" "invalid_json" ""
    echo "➡️ 跳过（预校验失败）：$file" >&2
    return 0
  fi

  local line batch_id status results_url
  line="$(lookup_csv "$file" || true)"
  batch_id=""

  if [[ -n "$line" ]]; then
    batch_id="${line%%,*}"
    [[ -n "$batch_id" && "$batch_id" != "NA" ]] && echo "复用已存在 batch：$batch_id" >&2
  fi

  if [[ -z "$batch_id" || "$batch_id" == "NA" ]]; then
    if batch_id="$(create_batch "$file")"; then :; else
      upsert_csv "$file" "NA" "create_failed" ""
      echo "➡️ 跳过（创建失败）：$file" >&2
      return 0
    fi
  fi

  if [[ -z "$batch_id" || "$batch_id" == "null" ]]; then
    upsert_csv "$file" "NA" "empty_batch_id" ""
    echo "➡️ 跳过（batch_id 为空）：$file" >&2
    return 0
  fi

  local wait_out final_status final_results_url
  if wait_out="$(wait_terminal "$batch_id")"; then
    final_status="${wait_out%% *}"
    final_results_url="${wait_out#* }"
    [[ "$final_results_url" == "$final_status" ]] && final_results_url=""
    echo "➡️ 终态：$final_status" >&2
  else
    upsert_csv "$file" "$batch_id" "poll_failed" ""
    echo "➡️ 跳过（轮询失败）：$file" >&2
    return 0
  fi

  upsert_csv "$file" "$batch_id" "$final_status" "$final_results_url"

  if [[ "$final_status" == "ended" ]]; then
    download_results "$batch_id" "$file"
  else
    echo "➡️ 本文件未结束（status=$final_status），继续下一个" >&2
  fi
}

# ===== 主流程 =====
shopt -s nullglob

if [[ ! -d "$INPUT_ROOT" ]]; then
  echo "❌ INPUT_ROOT 不存在或不是目录：$INPUT_ROOT" >&2
  exit 1
fi

process_project_dir () { # $1:proj_dir $2:project_name
  local proj_dir="$1" project_name="$2"
  OUTPUT_DIR="$OUTPUT_ROOT/$project_name"
  mkdir -p "$OUTPUT_DIR"

  echo "==============================" >&2
  echo "🧩 Project: $project_name" >&2
  echo "  输入:  $proj_dir/batch*.json" >&2
  echo "  输出:  $OUTPUT_DIR/" >&2
  echo "==============================" >&2

  local files
  files=( "$proj_dir"/batch*.json )
  if ((${#files[@]} == 0)); then
    # 兼容：如果你生成器命名不是 batch*.json，也可以在这里加别的模式
    echo "➡️ 跳过：$project_name 下没有 batch*.json" >&2
    return 0
  fi

  local f
  for f in "${files[@]}"; do
    process_one_file "$f"
  done
}

# 只跑指定 project（支持逗号分隔多个）
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
    echo "❌ 以下 project 不存在于 $INPUT_ROOT/ ：${local_missing[*]}" >&2
    echo "   可用项目如下：" >&2
    ls -1 "$INPUT_ROOT" >&2
    exit 1
  fi

  for project_name in "${projects[@]}"; do
    [[ -z "$project_name" ]] && continue
    process_project_dir "$INPUT_ROOT/$project_name" "$project_name"
  done

  echo "DONE: ${projects[*]}. CSV: $CSV, OUT: $OUTPUT_ROOT/<project>/ 下。" >&2
  exit 0
fi

# 跑全部 project
echo "📂 扫描全部项目：$INPUT_ROOT/*" >&2
for proj_dir in "$INPUT_ROOT"/*; do
  [[ -d "$proj_dir" ]] || continue
  project_name="${proj_dir##*/}"
  process_project_dir "$proj_dir" "$project_name"
done

echo "✅ 全部项目处理完成。清单在 $CSV，结果在 $OUTPUT_ROOT/<project>/ 下。" >&2