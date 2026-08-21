#!/usr/bin/env bash
set -uo pipefail

# evaluate_baseline.sh — evaluate the baseline patches for one model.
#
# Usage:
#   bash evaluate_baseline.sh Claude Chart
#   bash evaluate_baseline.sh Claude Chart Lang Math
#   bash evaluate_baseline.sh Claude            # no project -> every project of that model
#
# For each project it reads the bug ids from the patch directories, checks out each buggy
# version, applies patch.java, runs the developer-written test suite, and finally classifies
# every bug from its log. Bugs run in batches of 30 and each checkout is deleted once its
# batch finishes; a failing checkout or step skips that bug or batch and continues.
#
# Paths, all overridable through the environment:
#   in   BASELINE_ROOT   <repo>/results/data/baseline/<model>/<project>/<id>/patch.java
#   in   CSV_DIR         <repo>/scripts/evaluation/bug_methods/<project_lower>_methods.csv
#   tmp  PROJECTS_ROOT   <repo>/defects4j_checkouts/<Project>/<Project>-<id>-buggy
#   out  LOGS_ROOT       <repo>/test-logs/baseline/<model>/
#   out  OUT_ROOT        <repo>/generated_evaluation/baseline/<model>/<project>.csv
#                        same layout and columns as results/evaluation/, so the two diff directly
#
# Steps: baseline_step1_apply_patch.py -> step2_get_test_results -> step3_status

MODEL="${1:-}"
if [[ -z "${MODEL}" ]]; then
  echo "[ERROR] missing model name"
  echo "Usage: bash evaluate_baseline.sh <MODEL> [PROJECT1 PROJECT2 ...]"
  exit 1
fi
shift || true

# --- repository-anchored paths -------------------------------------------
# This script lives at <repo>/scripts/evaluation/evaluate_baseline.sh
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

# inputs
BASELINE_ROOT="${BASELINE_ROOT:-${REPO_ROOT}/results/data/baseline}"   # patch.java per bug
CSV_DIR="${CSV_DIR:-${SCRIPT_DIR}/bug_methods}"                       # <project>_methods.csv

# scratch and outputs, created on demand
PROJECTS_ROOT="${PROJECTS_ROOT:-${REPO_ROOT}/defects4j_checkouts}"    # Defects4J checkouts
LOGS_ROOT="${LOGS_ROOT:-${REPO_ROOT}/test-logs/baseline}"             # per-bug test logs
OUT_ROOT="${OUT_ROOT:-${REPO_ROOT}/generated_evaluation/baseline}"    # <model>/<project>.csv
# -------------------------------------------------------------------------

STEP1_SCRIPT="${SCRIPT_DIR}/baseline_step1_apply_patch.py"
STEP2_SCRIPT="${SCRIPT_DIR}/baseline_step2_run_tests.py"
STEP3_SCRIPT="${SCRIPT_DIR}/baseline_step3_score.py"

MODEL_ROOT="${BASELINE_ROOT}/${MODEL}"
if [[ ! -d "${MODEL_ROOT}" ]]; then
  echo "[ERROR] baseline model root not found: ${MODEL_ROOT}"
  exit 1
fi

# Optional: restrict the run to these bug ids, comma separated or as ranges, e.g. ONLY_IDS=2,4,6
ONLY_IDS="${ONLY_IDS:-}"

BATCH_SIZE=30

mkdir -p "${PROJECTS_ROOT}"

run_cmd() {
  echo
  echo "===================================================================================================="
  echo "RUN: $*"
  echo "===================================================================================================="
  "$@"
}

cleanup_batch() {
  local project="$1"
  shift
  local ids=("$@")
  local project_root="${PROJECTS_ROOT}/${project}"

  local bug_id
  for bug_id in "${ids[@]}"; do
    local base_dir="${project_root}/${project}-${bug_id}-buggy"
    if [[ -d "${base_dir}" ]]; then
      echo "[CLEANUP] removing ${base_dir}"
      rm -rf "${base_dir}"
    fi
  done
}

join_by_comma() {
  local IFS=","
  echo "$*"
}

is_consecutive() {
  local arr=("$@")
  local n="${#arr[@]}"
  if [[ "${n}" -le 1 ]]; then
    return 0
  fi
  local i
  for ((i=1; i<n; i++)); do
    if [[ $(( ${arr[i-1]} + 1 )) -ne "${arr[i]}" ]]; then
      return 1
    fi
  done
  return 0
}

ids_to_expr() {
  local arr=("$@")
  local n="${#arr[@]}"
  if [[ "${n}" -eq 0 ]]; then
    echo ""
    return
  fi
  if is_consecutive "${arr[@]}"; then
    if [[ "${n}" -gt 1 ]]; then
      echo "${arr[0]}-${arr[n-1]}"
    else
      echo "${arr[0]}"
    fi
  else
    join_by_comma "${arr[@]}"
  fi
}

# Bug ids for this project: the numerically named subdirectories of the patch root, ascending
get_baseline_ids() {
  local proj="$1"
  python3 -c "
import sys, re
from pathlib import Path
base = Path('${MODEL_ROOT}') / sys.argv[1]
ids = []
if base.is_dir():
    for c in base.iterdir():
        if c.is_dir() and re.match(r'^\d+\$', c.name):
            ids.append(int(c.name))
print(' '.join(str(i) for i in sorted(set(ids))))
" "${proj}"
}

# Every project of this model, skipping aggregate directories such as z_*
get_baseline_projects() {
  python3 -c "
from pathlib import Path
base = Path('${MODEL_ROOT}')
projs = []
for c in sorted(base.iterdir()):
    if c.is_dir() and not c.name.startswith('z'):
        projs.append(c.name)
print(' '.join(projs))
"
}

# Determine which projects to run
if [[ "$#" -gt 0 ]]; then
  PROJECTS_TO_RUN=("$@")
else
  PROJECTS_TO_RUN=( $(get_baseline_projects) )
fi

for PROJECT in "${PROJECTS_TO_RUN[@]}"; do
  echo
  echo "####################################################################################################"
  echo "[PROJECT] ${PROJECT}"
  echo "####################################################################################################"

  PATCHES_ROOT="${MODEL_ROOT}/${PROJECT}"
  if [[ ! -d "${PATCHES_ROOT}" ]]; then
    echo "[ERROR] patches root not found: ${PATCHES_ROOT}; skip project"
    continue
  fi

  BUG_IDS=()
  while IFS= read -r bid; do
    [[ -n "$bid" ]] && BUG_IDS+=( "$bid" )
  done < <(get_baseline_ids "${PROJECT}" | tr ' ' '\n')

  # When ONLY_IDS is set, keep only those bug ids
  if [[ -n "${ONLY_IDS}" ]]; then
    ONLY_SET=" $(echo "${ONLY_IDS}" | tr ',' ' ') "
    FILTERED=()
    for bid in "${BUG_IDS[@]}"; do
      if [[ "${ONLY_SET}" == *" ${bid} "* ]]; then
        FILTERED+=( "${bid}" )
      fi
    done
    BUG_IDS=( "${FILTERED[@]}" )
    echo "[INFO] ONLY_IDS active -> ${PROJECT} restricted to: ${BUG_IDS[*]}"
  fi

  if [[ "${#BUG_IDS[@]}" -eq 0 ]]; then
    echo "[WARN] no patch bug ids for ${PROJECT}, skipping"
    continue
  fi

  CSV_PATH="${CSV_DIR}/$(echo "${PROJECT}" | tr '[:upper:]' '[:lower:]')_methods.csv"
  if [[ ! -f "${CSV_PATH}" ]]; then
    CSV_PATH="${CSV_DIR}/${PROJECT}_methods.csv"
  fi
  if [[ ! -f "${CSV_PATH}" ]]; then
    echo "[ERROR] CSV not found: ${CSV_DIR}/<${PROJECT}>_methods.csv; skip project"
    continue
  fi

  TOTAL="${#BUG_IDS[@]}"
  BATCHES=$(( (TOTAL + BATCH_SIZE - 1) / BATCH_SIZE ))
  echo "[INFO] ${PROJECT}: bugs=${TOTAL}: ${BUG_IDS[*]}"
  echo "[INFO] ${PROJECT}: batches=${BATCHES}, batch_size=${BATCH_SIZE}"

  mkdir -p "${PROJECTS_ROOT}/${PROJECT}"

  for ((batch_idx=0; batch_idx<TOTAL; batch_idx+=BATCH_SIZE)); do
    batch_num=$(( batch_idx / BATCH_SIZE + 1 ))
    BATCH_IDS=( "${BUG_IDS[@]:batch_idx:BATCH_SIZE}" )
    BATCH_EXPR="$(ids_to_expr "${BATCH_IDS[@]}")"

    echo
    echo "----------------------------------------------------------------------------------------------------"
    echo "[BATCH] ${PROJECT} batch ${batch_num}/${BATCHES} ids=${BATCH_EXPR}"
    echo "----------------------------------------------------------------------------------------------------"

    batch_ok=1
    checked_out_ids=()

    bug_id=""
    for bug_id in "${BATCH_IDS[@]}"; do
      workdir="${PROJECTS_ROOT}/${PROJECT}/${PROJECT}-${bug_id}-buggy"
      rm -rf "${workdir}"

      echo
      echo "===================================================================================================="
      echo "RUN: defects4j checkout -p ${PROJECT} -v ${bug_id}b -w ${workdir}"
      echo "===================================================================================================="

      if defects4j checkout -p "${PROJECT}" -v "${bug_id}b" -w "${workdir}"; then
        checked_out_ids+=( "${bug_id}" )
      else
        echo "[WARN] checkout failed for ${PROJECT}-${bug_id}; skip this bug and continue current batch"
      fi
    done

    if [[ "${#checked_out_ids[@]}" -eq 0 ]]; then
      echo "[WARN] batch ${batch_num}/${BATCHES} of ${PROJECT}: no successful checkout; skip to next batch"
      cleanup_batch "${PROJECT}" "${BATCH_IDS[@]}"
      continue
    fi

    RUN_IDS_EXPR="$(ids_to_expr "${checked_out_ids[@]}")"
    echo "[INFO] ${PROJECT} batch ${batch_num}: runnable ids=${RUN_IDS_EXPR}"

    if ! run_cmd python3 "${STEP1_SCRIPT}" \
      --csv "${CSV_PATH}" \
      --patches-root "${PATCHES_ROOT}" \
      --projects-root "${PROJECTS_ROOT}" \
      --project "${PROJECT}" \
      --ids "${RUN_IDS_EXPR}"; then
      echo "[WARN] step1 failed for ${PROJECT} batch ${batch_num}; skip to next batch"
      batch_ok=0
    fi

    if [[ "${batch_ok}" -eq 1 ]]; then
      if ! run_cmd python3 "${STEP2_SCRIPT}" \
        --bug-root "${PROJECTS_ROOT}/${PROJECT}" \
        --logs-root "${LOGS_ROOT}/${MODEL}" \
        --ids "${RUN_IDS_EXPR}"; then
        echo "[WARN] step2 failed for ${PROJECT} batch ${batch_num}; skip to next batch"
        batch_ok=0
      fi
    fi

    # Delete this batch of checkouts as soon as step1 and step2 are done
    cleanup_batch "${PROJECT}" "${BATCH_IDS[@]}"
  done

  # Run step3 once per project: read every log of that project and rewrite <project>.csv
  echo
  echo "----------------------------------------------------------------------------------------------------"
  echo "[STEP3] ${PROJECT}: rebuilding ${PROJECT}.csv from the logs"
  echo "----------------------------------------------------------------------------------------------------"
  run_cmd python3 "${STEP3_SCRIPT}" \
    --baseline-root "${BASELINE_ROOT}" \
    --out-root "${OUT_ROOT}" \
    --logs-root "${LOGS_ROOT}" \
    --model "${MODEL}" \
    --project "${PROJECT}"

  echo "[DONE] ${PROJECT}"
  echo "  z_final csv: ${MODEL_ROOT}/z_final/${PROJECT}.csv"
done
