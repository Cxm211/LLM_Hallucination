#!/usr/bin/env bash
set -uo pipefail

# evaluate_exm1.sh — evaluate the Task 1 (triggering testcase identification) patches for one model.
#
# Usage:
#   bash evaluate_exm1.sh Claude Chart
#   bash evaluate_exm1.sh Claude Chart Lang Math
#   bash evaluate_exm1.sh Claude            # no project -> every project of that model
#
# A bug whose relevant test suite exceeds the 300-method limit is split into slices, so a
# project directory holds entries like 14, 14_1, 14_2 — each with its own patch. Bugs run in
# batches of 30 and each checkout is deleted once its batch finishes.
#
# Set AFFECTED_CSV to a csv with project,bug_id columns to restrict the run to that subset;
# unset, every bug under the patch root is evaluated.
#
# Paths, all overridable through the environment:
#   in   RESULTS_ROOT    <repo>/results/data/exm1/<model>/<project>/<id>/patch.java
#   in   CSV_DIR         <repo>/scripts/evaluation/bug_methods/<project_lower>_methods.csv
#   tmp  PROJECTS_ROOT   <repo>/defects4j_checkouts/<Project>/<Project>-<id>-buggy
#   out  LOGS_ROOT       <repo>/test-logs/exm1/<model>/
#   out  OUT_ROOT        <repo>/generated_evaluation/exm1/<model>/z_final/<project>.csv
#
# Steps: exm1_step1_apply_patch.py -> step2_get_test_results -> step3_variant_status

MODEL="${1:-}"
if [[ -z "${MODEL}" ]]; then
  echo "[ERROR] missing model name"
  echo "Usage: bash evaluate_exm1.sh <MODEL> [PROJECT1 PROJECT2 ...]"
  echo "       If no projects given, every project under the patch root is used."
  exit 1
fi
shift || true

# --- repository-anchored paths -------------------------------------------
# This script lives at <repo>/scripts/evaluation/evaluate_exm1.sh
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

# inputs
RESULTS_ROOT="${RESULTS_ROOT:-${REPO_ROOT}/results/data/exm1}"        # patch.java per slice
CSV_DIR="${CSV_DIR:-${SCRIPT_DIR}/bug_methods}"                       # <project>_methods.csv

# scratch and outputs, created on demand
PROJECTS_ROOT="${PROJECTS_ROOT:-${REPO_ROOT}/defects4j_checkouts}"
LOGS_ROOT="${LOGS_ROOT:-${REPO_ROOT}/test-logs/exm1}"
OUT_ROOT="${OUT_ROOT:-${REPO_ROOT}/generated_evaluation/exm1}"
ARTIFACTS_DIR="${ARTIFACTS_DIR:-${OUT_ROOT}/step3_outputs}"
TRIGGER_CSV="${TRIGGER_CSV:-${SCRIPT_DIR}/oracle_triggers.csv}"        # oracle triggering testcases
AFFECTED_CSV="${AFFECTED_CSV:-}"                                      # optional filter, off by default
MODEL_ROOT="${RESULTS_ROOT}/${MODEL}"
# -------------------------------------------------------------------------

STEP1_SCRIPT="${SCRIPT_DIR}/exm1_step1_apply_patch.py"
STEP2_SCRIPT="${SCRIPT_DIR}/exm1_step2_run_tests.py"
STEP3_SCRIPT="${SCRIPT_DIR}/exm1_step3_score.py"

# Optional: restrict the run to these bug ids, comma separated or as ranges, e.g. ONLY_IDS=2,4,6
# Leave empty to evaluate every bug of the project
ONLY_IDS="${ONLY_IDS:-}"

# Set RERUN_FAILED_ONLY=1 to re-run only the bugs whose existing log contains
# FAILED_MARKER, i.e. those whose checkout was missing so the tests never ran.
# Off by default: every bug under the patch root is evaluated.
RERUN_FAILED_ONLY="${RERUN_FAILED_ONLY:-0}"
FAILED_MARKER="Cannot open config file"

BATCH_SIZE=30

mkdir -p "${PROJECTS_ROOT}" "${ARTIFACTS_DIR}"

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

    shopt -s nullglob
    local d
    for d in "${project_root}/${project}-${bug_id}"_*-buggy; do
      if [[ -d "${d}" ]]; then
        echo "[CLEANUP] removing ${d}"
        rm -rf "${d}"
      fi
    done
    shopt -u nullglob
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

merge_jsons() {
  local output="$1"
  shift
  python3 - "$output" "$@" <<'PY'
import json, sys, os
out = sys.argv[1]
files = sys.argv[2:]
merged = []
for fp in files:
    if os.path.exists(fp) and os.path.getsize(fp) > 0:
        with open(fp, "r", encoding="utf-8") as f:
            data = json.load(f)
        if isinstance(data, list):
            merged.extend(data)
with open(out, "w", encoding="utf-8") as f:
    json.dump(merged, f, ensure_ascii=False, indent=2)
print(f"[MERGED JSON] {out}")
PY
}

merge_csvs() {
  local output="$1"
  shift
  python3 - "$output" "$@" <<'PY'
import csv, sys, os
out = sys.argv[1]
files = sys.argv[2:]
fieldnames = None
rows = []
for fp in files:
    if os.path.exists(fp) and os.path.getsize(fp) > 0:
        with open(fp, "r", encoding="utf-8", newline="") as f:
            reader = csv.DictReader(f)
            if fieldnames is None:
                fieldnames = reader.fieldnames
            for row in reader:
                rows.append(row)
with open(out, "w", encoding="utf-8", newline="") as f:
    if fieldnames:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)
print(f"[MERGED CSV] {out}")
PY
}

if [[ -n "${AFFECTED_CSV}" && ! -f "${AFFECTED_CSV}" ]]; then
  echo "[ERROR] affected CSV not found: ${AFFECTED_CSV}"
  exit 1
fi

# Helper: get sorted affected bug ids for a project from CSV
# Without AFFECTED_CSV every bug present under the patch root is evaluated. Point
# AFFECTED_CSV at a csv with project,bug_id columns to restrict the run to that subset.
get_affected_ids() {
  local proj="$1"
  if [[ -z "${AFFECTED_CSV}" ]]; then
    local d="${MODEL_ROOT}/${proj}"
    [[ -d "$d" ]] || return 0
    ls -1 "$d" | sed 's/_.*$//' | grep -E '^[0-9]+$' | sort -n -u | tr '\n' ' '
    return 0
  fi
  python3 -c "
import csv, sys
proj = sys.argv[1]
ids = []
with open('${AFFECTED_CSV}') as f:
    for row in csv.DictReader(f):
        if row['project'] == proj:
            ids.append(int(row['bug_id']))
print(' '.join(str(i) for i in sorted(ids)))
" "${proj}"
}

# Helper: base bug ids whose log still carries FAILED_MARKER for this (model, project).
# The id is read back from the log name: <PROJECT>-<base>_<n>-buggy.log -> <base>
get_failed_ids() {
  local proj="$1"
  local logdir="${LOGS_ROOT}/${MODEL}/${proj}"
  [[ -d "${logdir}" ]] || { echo ""; return; }
  grep -rl "${FAILED_MARKER}" "${logdir}" 2>/dev/null \
    | sed 's#.*/##; s#-buggy.*##; s#_[0-9]*$##' \
    | sed "s#^${proj}-##" \
    | grep -E '^[0-9]+$' \
    | sort -n | uniq | tr '\n' ' '
}

# Get all affected projects from CSV if none specified
get_affected_projects() {
  if [[ -z "${AFFECTED_CSV}" ]]; then
    ls -1 "${MODEL_ROOT}" 2>/dev/null | grep -vE '^z' | tr '\n' ' '
    return 0
  fi
  python3 -c "
import csv
projects = set()
with open('${AFFECTED_CSV}') as f:
    for row in csv.DictReader(f):
        projects.add(row['project'])
print(' '.join(sorted(projects)))
"
}

# Determine which projects to run
if [[ "$#" -gt 0 ]]; then
  PROJECTS_TO_RUN=("$@")
else
  PROJECTS_TO_RUN=( $(get_affected_projects) )
fi

for PROJECT in "${PROJECTS_TO_RUN[@]}"; do
  echo
  echo "####################################################################################################"
  echo "[PROJECT] ${PROJECT}"
  echo "####################################################################################################"

  BUG_IDS=()
  while IFS= read -r bid; do
    [[ -n "$bid" ]] && BUG_IDS+=( "$bid" )
  done < <(get_affected_ids "${PROJECT}" | tr ' ' '\n')

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

  # Re-run only the previously failed bugs. The failing logs are the authoritative
  # source here, so the ids come from them directly rather than from AFFECTED_CSV.
  if [[ "${RERUN_FAILED_ONLY}" == "1" ]]; then
    FAILED_IDS=( $(get_failed_ids "${PROJECT}") )
    if [[ "${#FAILED_IDS[@]}" -eq 0 ]]; then
      echo "[INFO] RERUN_FAILED_ONLY active -> no failing log for ${PROJECT}, skipping"
      continue
    fi
    BUG_IDS=( "${FAILED_IDS[@]}" )
    echo "[INFO] RERUN_FAILED_ONLY active -> re-running failed bugs of ${PROJECT}: ${BUG_IDS[*]}"
  fi

  if [[ "${#BUG_IDS[@]}" -eq 0 ]]; then
    echo "[WARN] no affected bug ids for ${PROJECT}, skipping"
    continue
  fi

  echo "[INFO] ${PROJECT}: ${#BUG_IDS[@]} bugs: ${BUG_IDS[*]}"

  TOTAL="${#BUG_IDS[@]}"
  BATCHES=$(( (TOTAL + BATCH_SIZE - 1) / BATCH_SIZE ))
  echo "[INFO] ${PROJECT}: affected bugs=${TOTAL}, batches=${BATCHES}, batch_size=${BATCH_SIZE}"

  PROJECT_BATCH_CSVS=()
  PROJECT_BATCH_JSONS=()

  for ((batch_idx=0; batch_idx<TOTAL; batch_idx+=BATCH_SIZE)); do
    batch_num=$(( batch_idx / BATCH_SIZE + 1 ))
    BATCH_IDS=( "${BUG_IDS[@]:batch_idx:BATCH_SIZE}" )
    BATCH_EXPR="$(ids_to_expr "${BATCH_IDS[@]}")"

    echo
    echo "----------------------------------------------------------------------------------------------------"
    echo "[BATCH] ${PROJECT} batch ${batch_num}/${BATCHES} ids=${BATCH_EXPR}"
    echo "----------------------------------------------------------------------------------------------------"

    CSV_PATH="${CSV_DIR}/$(echo "${PROJECT}" | tr '[:upper:]' '[:lower:]')_methods.csv"
    if [[ ! -f "${CSV_PATH}" ]]; then
      CSV_PATH="${CSV_DIR}/${PROJECT}_methods.csv"
    fi
    if [[ ! -f "${CSV_PATH}" ]]; then
      echo "[ERROR] CSV not found: ${CSV_DIR}/<${PROJECT}>_methods.csv"
      echo "[WARN] skip batch ${batch_num}/${BATCHES} for ${PROJECT}"
      cleanup_batch "${PROJECT}" "${BATCH_IDS[@]}"
      continue
    fi

    PATCHES_ROOT="${RESULTS_ROOT}/${MODEL}/${PROJECT}"
    if [[ ! -d "${PATCHES_ROOT}" ]]; then
      echo "[ERROR] patches root not found: ${PATCHES_ROOT}"
      echo "[WARN] skip project ${PROJECT}"
      break
    fi

    mkdir -p "${PROJECTS_ROOT}/${PROJECT}"

    batch_ok=1
    checked_out_ids=()
    failed_checkout_ids=()

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
        failed_checkout_ids+=( "${bug_id}" )
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

    # Delete this batch of checkouts as soon as step1 and step2 are done; step3 runs once per project
    cleanup_batch "${PROJECT}" "${BATCH_IDS[@]}"
  done

  # Once every batch has finished, run step3 once for the whole project. No --ids is passed,
  # so it reads every log of that project and rewrites <project>.csv from scratch.
  echo
  echo "----------------------------------------------------------------------------------------------------"
  echo "[STEP3] ${PROJECT}: rebuilding ${PROJECT}.csv from the logs"
  echo "----------------------------------------------------------------------------------------------------"
  run_cmd python3 "${STEP3_SCRIPT}" \
    --results-root "${RESULTS_ROOT}" \
    --out-root "${OUT_ROOT}" \
    --logs-root "${LOGS_ROOT}" \
    --model "${MODEL}" \
    --project "${PROJECT}"

  echo "[DONE] ${PROJECT}"
  echo "  z_final csv: ${RESULTS_ROOT}/${MODEL}/z_final/${PROJECT}.csv"
done