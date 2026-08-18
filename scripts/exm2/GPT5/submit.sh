#!/usr/bin/env bash
set -euo pipefail

INPUT_ROOT="${INPUT_ROOT:-z_requests}"
OUTPUT_ROOT="${OUTPUT_ROOT:-z_output}"

ENDPOINT="/v1/responses"
COMPLETION_WINDOW="24h"
: "${OPENAI_API_KEY:?set OPENAI_API_KEY in your environment before running this script}"
POLL_SECONDS=20
MAX_WAIT_SECONDS=$((24*3600))

CSV="batches.csv"
TMP_RESP=".tmp_resp.json"

# ========== 参数解析 ==========
# 用法：
#   bash run_batches.sh                       # 跑全部项目
#   bash run_batches.sh --project Cli         # 只跑 Cli
#   bash run_batches.sh --project Cli,Web     # 跑多个项目（逗号分隔）
#   bash run_batches.sh -p Cli,Web
PROJECT_FILTER="all"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --project|-p)
      PROJECT_FILTER="${2:-}"
      if [[ -z "$PROJECT_FILTER" ]]; then
        echo "❌ 缺少参数：--project <name|name1,name2|all>" >&2
        exit 1
      fi
      shift 2
      ;;
    --help|-h)
      cat >&2 <<EOF
用法：
  bash $0                       # 跑全部项目（默认）
  bash $0 --project Cli         # 只跑某个项目
  bash $0 --project Cli,Web     # 跑多个项目（逗号分隔）
  bash $0 -p Cli,Web
环境变量：
  INPUT_ROOT  (默认 z_requests)
  OUTPUT_ROOT (默认 z_output)
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
if ! command -v jq >/dev/null 2>&1; then
  echo "❌ 需要 jq，请先安装（mac: brew install jq / Ubuntu: sudo apt-get install jq）" >&2
  exit 1
fi

if [[ -z "${OPENAI_API_KEY:-}" ]]; then
  echo "❌ 未设置 OPENAI_API_KEY。请先执行：export OPENAI_API_KEY='...'" >&2
  exit 1
fi

mkdir -p "$OUTPUT_ROOT"
[[ -f "$CSV" ]] || echo "file,file_id,batch_id,status" > "$CSV"

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
        -H "Authorization: Bearer $OPENAI_API_KEY" -o "$TMP_RESP") || http_code=$?
    else
      http_code=$(curl -sS -w "%{http_code}" "$url" \
        -H "Authorization: Bearer $OPENAI_API_KEY" \
        -H "Content-Type: application/json" \
        -d "$data" -o "$TMP_RESP") || http_code=$?
    fi
    if [[ "$http_code" == "200" ]]; then cat "$TMP_RESP"; return 0; fi
    if [[ "$http_code" == "429" && $attempt -lt $max_attempts ]]; then
      attempt=$((attempt+1)); sleep $backoff; backoff=$((backoff*2)); continue
    fi
    echo "HTTP $http_code while calling: $url" >&2
    cat "$TMP_RESP" >&2
    return 1
  done
}

validate_jsonl () { # $1:file
  local f="$1"
  [[ -f "$f" ]] || { echo "跳过：找不到 $f" >&2; return 2; }
  if ! jq -c . < "$f" >/dev/null 2>&1; then
    echo "❌ JSONL 非法：$f（确保每行是独立合法 JSON）" >&2; return 3
  fi
  local url_in
  url_in=$(head -n1 "$f" | jq -r '.url? // empty')
  if [[ "$url_in" != "$ENDPOINT" ]]; then
    echo "❌ URL 不一致：$f 行内是 '${url_in:-<空>}'，脚本 ENDPOINT 是 '$ENDPOINT'" >&2; return 4
  fi
  return 0
}

lookup_csv () { # $1:file -> "batch_id,status" 或空
  [[ -f "$CSV" ]] || return 0
  awk -F, -v f="$1" 'NR>1 && $1==f {print $3","$4}' "$CSV" | tail -n1
}

upsert_csv () { # $1:file $2:file_id $3:batch_id $4:status
  [[ -f "$CSV" ]] || echo "file,file_id,batch_id,status" > "$CSV"
  local tmp; tmp=$(mktemp)
  awk -F, -v f="$1" 'BEGIN{OFS=","} NR==1{print; next} $1!=f {print}' "$CSV" > "$tmp"
  mv "$tmp" "$CSV"
  echo "$1,$2,$3,$4" >> "$CSV"
}

create_batch () { # $1:file -> echo "batch_id file_id"
  local f="$1"
  echo "上传文件：$f" >&2
  local upload_json file_id
  upload_json=$(curl -sS https://api.openai.com/v1/files \
    -H "Authorization: Bearer $OPENAI_API_KEY" \
    -F purpose="batch" -F file="@$f") || { echo "❌ 上传失败：$f" >&2; return 1; }
  file_id=$(json_get "$upload_json" '.id')
  if [[ -z "$file_id" || "$file_id" == "null" ]]; then
    echo "❌ 上传失败（未拿到 file_id）：$f" >&2; echo "$upload_json" >&2; return 1
  fi

  local payload batch_json batch_id status
  payload=$(jq -nc --arg fid "$file_id" --arg ep "$ENDPOINT" --arg cw "$COMPLETION_WINDOW" \
    '{input_file_id:$fid, endpoint:$ep, completion_window:$cw}')
  batch_json=$(curl_json POST "https://api.openai.com/v1/batches" "$payload") || return 1
  batch_id=$(json_get "$batch_json" '.id'); status=$(json_get "$batch_json" '.status')
  if [[ -z "$batch_id" || "$batch_id" == "null" ]]; then
    echo "❌ 创建 Batch 失败：$f（未拿到 batch_id）" >&2; echo "$batch_json" >&2; return 1
  fi

  echo "✅ 新建 batch_id=$batch_id  status=$status" >&2
  upsert_csv "$f" "$file_id" "$batch_id" "$status"
  echo "$batch_id $file_id"
}

wait_terminal () { # $1:batch_id -> echo final_status
  local b_id="$1" waited=0 status status_json
  while :; do
    status_json=$(curl_json GET "https://api.openai.com/v1/batches/$b_id") || {
      echo "❌ 拉取状态失败：$b_id" >&2; return 1; }
    status=$(json_get "$status_json" '.status')
    echo "状态：$b_id -> $status" >&2
    case "$status" in
      completed|failed|expired|cancelled) echo "$status"; return 0 ;;
      *) sleep "$POLL_SECONDS"; waited=$((waited+POLL_SECONDS));
         (( waited >= MAX_WAIT_SECONDS )) && { echo "⚠️ 等待超时" >&2; echo "$status"; return 0; } ;;
    esac
  done
}

print_error_brief () { # $1:batch_id $2:file
  local meta err_id
  meta=$(curl_json GET "https://api.openai.com/v1/batches/$1") || return 0
  err_id=$(json_get "$meta" '.error_file_id')
  if [[ -n "$err_id" && "$err_id" != "null" ]]; then
    echo "🔎 错误摘要（$2）：" >&2
    curl -sS "https://api.openai.com/v1/files/$err_id/content" \
      -H "Authorization: Bearer $OPENAI_API_KEY" | head -n 100 >&2
  fi
}

download_outputs () { # $1:batch_id $2:file
  local meta out_id err_id base
  meta=$(curl_json GET "https://api.openai.com/v1/batches/$1") || return 0
  out_id=$(json_get "$meta" '.output_file_id')
  err_id=$(json_get "$meta" '.error_file_id')

  base="${2##*/}"; base="${base%.*}"

  if [[ -n "$out_id" && "$out_id" != "null" ]]; then
    echo "⬇️ 下载输出：$out_id -> $OUTPUT_DIR/${base}_output.jsonl" >&2
    curl -sS "https://api.openai.com/v1/files/$out_id/content" \
      -H "Authorization: Bearer $OPENAI_API_KEY" -o "$OUTPUT_DIR/${base}_output.jsonl"
  fi
  if [[ -n "$err_id" && "$err_id" != "null" ]]; then
    echo "⬇️ 下载错误明细：$err_id -> $OUTPUT_DIR/${base}_errors.jsonl" >&2
    curl -sS "https://api.openai.com/v1/files/$err_id/content" \
      -H "Authorization: Bearer $OPENAI_API_KEY" -o "$OUTPUT_DIR/${base}_errors.jsonl"
  fi
}

process_one_file () { # $1:file
  local file="$1"
  echo "========== 处理 $file ==========" >&2

  if ! validate_jsonl "$file"; then
    upsert_csv "$file" "" "NA" "invalid_json_or_url"
    echo "➡️ 跳过（预校验失败）：$file" >&2
    return 0
  fi

  local line batch_id file_id final_status
  line="$(lookup_csv "$file" || true)"
  batch_id=""
  if [[ -n "$line" ]]; then
    batch_id="${line%%,*}"
    [[ -n "$batch_id" && "$batch_id" != "NA" ]] && echo "复用已存在 batch：$batch_id" >&2
  fi

  file_id=""
  if [[ -z "$batch_id" || "$batch_id" == "NA" ]]; then
    if read -r batch_id file_id < <(create_batch "$file"); then :; else
      upsert_csv "$file" "$file_id" "NA" "create_failed"
      echo "➡️ 跳过（创建失败）：$file" >&2
      return 0
    fi
  fi

  if [[ -z "$batch_id" || "$batch_id" == "null" ]]; then
    upsert_csv "$file" "$file_id" "NA" "empty_batch_id"
    echo "➡️ 跳过（batch_id 为空）：$file" >&2
    return 0
  fi

  if final_status="$(wait_terminal "$batch_id")"; then
    echo "➡️ 终态：$final_status" >&2
  else
    upsert_csv "$file" "$file_id" "$batch_id" "poll_failed"
    echo "➡️ 跳过（轮询失败）：$file" >&2
    return 0
  fi

  upsert_csv "$file" "$file_id" "$batch_id" "$final_status"
  if [[ "$final_status" == "completed" ]]; then
    download_outputs "$batch_id" "$file"
  else
    print_error_brief "$batch_id" "$file"
    echo "➡️ 继续下一个(本文件未完成：$final_status)" >&2
  fi
}

# ===== 主流程 =====
shopt -s nullglob

if [[ ! -d "$INPUT_ROOT" ]]; then
  echo "❌ INPUT_ROOT 不存在或不是目录：$INPUT_ROOT" >&2
  exit 1
fi

# 只跑指定 project（支持逗号分隔多个）
if [[ "$PROJECT_FILTER" != "all" ]]; then
  IFS=',' read -r -a projects <<< "$PROJECT_FILTER"

  # 去掉可能的空白（例如 "Cli, Web"）
  for i in "${!projects[@]}"; do
    projects[$i]="${projects[$i]//[[:space:]]/}"
  done

  # 校验 project 是否存在
  missing=()
  for project_name in "${projects[@]}"; do
    [[ -z "$project_name" ]] && continue
    proj_dir="$INPUT_ROOT/$project_name"
    [[ -d "$proj_dir" ]] || missing+=("$project_name")
  done

  if ((${#missing[@]} > 0)); then
    echo "❌ 以下 project 不存在于 $INPUT_ROOT/ ：${missing[*]}" >&2
    echo "   可用项目如下：" >&2
    ls -1 "$INPUT_ROOT" >&2
    exit 1
  fi

  # 逐个 project 执行
  for project_name in "${projects[@]}"; do
    [[ -z "$project_name" ]] && continue

    proj_dir="$INPUT_ROOT/$project_name"
    OUTPUT_DIR="$OUTPUT_ROOT/$project_name"
    mkdir -p "$OUTPUT_DIR"

    echo "==============================" >&2
    echo "🧩 Project: $project_name" >&2
    echo "  输入:  $proj_dir/requests*.jsonl" >&2
    echo "  输出:  $OUTPUT_DIR/" >&2
    echo "==============================" >&2

    files=( "$proj_dir"/requests*.jsonl "$proj_dir"/requests*.json )
    if ((${#files[@]} == 0)); then
      echo "➡️ 跳过：$project_name 下没有 requests*.jsonl / requests*.json" >&2
      continue
    fi

    for file in "${files[@]}"; do
      process_one_file "$file"
    done
  done

  echo "DONE: ${projects[*]}. CSV: $CSV, OUT: $OUTPUT_ROOT/<project>/ 下。" >&2
  exit 0
fi

# 跑全部 project
echo "📂 扫描全部项目：$INPUT_ROOT/*" >&2
for proj_dir in "$INPUT_ROOT"/*; do
  [[ -d "$proj_dir" ]] || continue

  project_name="${proj_dir##*/}"
  OUTPUT_DIR="$OUTPUT_ROOT/$project_name"
  mkdir -p "$OUTPUT_DIR"

  echo "==============================" >&2
  echo "🧩 Project: $project_name" >&2
  echo "  输入:  $proj_dir/requests*.jsonl" >&2
  echo "  输出:  $OUTPUT_DIR/" >&2
  echo "==============================" >&2

  files=( "$proj_dir"/requests*.jsonl "$proj_dir"/requests*.json )
  if ((${#files[@]} == 0)); then
    echo "➡️ 跳过：$project_name 下没有 requests*.jsonl / requests*.json" >&2
    continue
  fi

  for file in "${files[@]}"; do
    process_one_file "$file"
  done
done

echo "✅ 全部项目处理完成。清单在 $CSV，结果在 $OUTPUT_ROOT/<project>/ 下。" >&2
