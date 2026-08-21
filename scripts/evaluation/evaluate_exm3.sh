#!/usr/bin/env bash
# evaluate_exm3.sh — run exm3 step1-8 for one project
#
# Default test logs:
#   test-logs/exm3/${MODEL}/buggy/
#   test-logs/exm3/${MODEL}/buggy_jacoco/
#   test-logs/exm3/${MODEL}/fixed/
#   test-logs/exm3/${MODEL}/fixed_jacoco/
#   test-logs/exm3/${MODEL}/groundtruth/
#
# Usage:
#   ./evaluate_exm3.sh --project JacksonDatabind
#   ./evaluate_exm3.sh --project JacksonDatabind --ids 1,3,5-7
#   ./evaluate_exm3.sh --project JacksonDatabind --from-step 4
#   ./evaluate_exm3.sh --model Claude --project Closure --ids 150,168
#
# Override base testlog root:
#   ./evaluate_exm3.sh --model Claude --project Closure --testlog-root /tmp/exp3_logs
#   -> /tmp/exp3_logs/Claude/buggy/
#
# Override wrapper stdout/stderr log dir:
#   ./evaluate_exm3.sh --model Claude --project Closure --run-log-dir test-logs/exm3/Claude/wrapper

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Prefer conda base python, if available.
CONDA_BASE="${CONDA_BASE:-$HOME/opt/anaconda3}"
if [[ -d "$CONDA_BASE/bin" ]]; then
    export PATH="$CONDA_BASE/bin:$PATH"
fi

# -------------------- defaults --------------------
PROJECT=""
IDS=""
FROM_STEP=1
MODEL="GPT5"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
PROJECTS_ROOT="${PROJECTS_ROOT:-${REPO_ROOT}/defects4j_checkouts}"

# Base directory for per-step test logs.
# Actual logs go to: ${TESTLOG_ROOT}/${MODEL}/...
TESTLOG_ROOT="${TESTLOG_ROOT:-${REPO_ROOT}/test-logs/exm3}"

# Directory for this wrapper's stdout/stderr log.
# If empty, defaults to ./logs
RUN_LOG_DIR=""

# -------------------- parse args --------------------
while [[ $# -gt 0 ]]; do
    case "$1" in
        --project)
            PROJECT="$2"
            shift 2
            ;;
        --ids)
            IDS="$2"
            shift 2
            ;;
        --from-step)
            FROM_STEP="$2"
            shift 2
            ;;
        --model)
            MODEL="$2"
            shift 2
            ;;
        --testlog-root)
            TESTLOG_ROOT="$2"
            shift 2
            ;;
        --run-log-dir)
            RUN_LOG_DIR="$2"
            shift 2
            ;;
        *)
            echo "[ERROR] unknown argument: $1"
            exit 1
            ;;
    esac
done

if [[ -z "$PROJECT" ]]; then
    echo "Usage: $0 --project <PROJECT> [--ids <expr>] [--from-step N] [--model MODEL] [--testlog-root DIR]"
    exit 1
fi

# Inputs come from results/data/exm3/<MODEL>; results go to generated_evaluation/exm3/<MODEL>
MR="${RESULTS_ROOT:-${REPO_ROOT}/results/data/exm3}/${MODEL}"          # inputs, read only
OUT_ROOT="${OUT_ROOT:-${REPO_ROOT}/generated_evaluation/exm3}/${MODEL}"
# Steps 1-7 write their intermediates here rather than into the published results tree
WORK="${WORK:-${REPO_ROOT}/generated_evaluation/exm3_work}/${MODEL}"
mkdir -p "$WORK" "$OUT_ROOT"
PATCHES_ROOT="${MR}/${PROJECT}"

# -------------------- logging --------------------
if [[ -n "$RUN_LOG_DIR" ]]; then
    LOG_DIR="$RUN_LOG_DIR"
else
    LOG_DIR="$SCRIPT_DIR/logs"
fi

mkdir -p "$LOG_DIR"
LOG_FILE="$LOG_DIR/exp3_${PROJECT}_$(date +%Y%m%d_%H%M%S).log"
exec > >(tee -a "$LOG_FILE") 2>&1

# -------------------- optional args as strings --------------------
# Use strings instead of empty bash arrays to avoid macOS Bash 3.2 + set -u issues.
IDS_ARG=""
if [[ -n "$IDS" ]]; then
    IDS_ARG="--ids $IDS"
fi

# -------------------- testlog args --------------------
# TESTLOG_ROOT is a base root, e.g. test-logs/exm3.
# The actual model-specific root is:
#   test-logs/exm3/${MODEL}
#
# The step scripts use different option names:
#   step2/5/6/7: --logs-root
#   step3:       --log-dir
TESTLOG_MODEL_ROOT="${TESTLOG_ROOT%/}/${MODEL}"
mkdir -p "$TESTLOG_MODEL_ROOT"

STEP2_LOG_ARGS="--logs-root $TESTLOG_MODEL_ROOT/buggy"
STEP3_LOG_ARGS="--log-dir $TESTLOG_MODEL_ROOT/buggy_jacoco"
STEP5_LOG_ARGS="--logs-root $TESTLOG_MODEL_ROOT/fixed"
STEP6_LOG_ARGS="--logs-root $TESTLOG_MODEL_ROOT/fixed_jacoco"
STEP7_LOG_ARGS="--logs-root $TESTLOG_MODEL_ROOT/groundtruth"

# -------------------- helper --------------------
run_step() {
    local step="$1"
    shift
    local cmd=("$@")

    if [[ "$step" -lt "$FROM_STEP" ]]; then
        echo "=== [SKIP] step${step} (--from-step=${FROM_STEP}) ==="
        return
    fi

    echo ""
    echo "================================================================"
    echo "=== step${step}: ${cmd[*]} ==="
    echo "================================================================"

    time python -u "${cmd[@]}"
}

echo "log file      : $LOG_FILE"
echo "model         : $MODEL"
echo "project       : $PROJECT"
echo "ids           : ${IDS:-all}"
echo "from-step     : $FROM_STEP"
echo "results root  : $MR"
echo "patches root  : $PATCHES_ROOT"
echo "testlog root  : $TESTLOG_MODEL_ROOT"
echo ""

# -------------------- discover bug IDs for checkout/cleanup --------------------
# With --ids, only those ids are checked out and cleaned up, expanding ranges such as
# 1,3,5-7. Without it, every active bug is used.
expand_ids() {
    local expr="$1"
    local out=()
    local IFS=','

    for part in $expr; do
        if [[ "$part" == *-* ]]; then
            local a="${part%-*}"
            local b="${part#*-}"
            for ((i=a; i<=b; i++)); do
                out+=("$i")
            done
        elif [[ -n "$part" ]]; then
            out+=("$part")
        fi
    done

    echo "${out[@]}"
}

if [[ -n "$IDS" ]]; then
    ALL_BUG_IDS=$(expand_ids "$IDS")
else
    ALL_BUG_IDS=$(defects4j bids -p "$PROJECT" 2>/dev/null | tr '\n' ' ' | tr -s ' ')
fi

echo "checkout/cleanup IDs : ${ALL_BUG_IDS:-none}"
echo ""

# -------------------- pre-step: checkout ALL active buggy versions --------------------
# Runs before step1; skipped when resuming from step7+.
if [[ "$FROM_STEP" -le 6 ]]; then
    echo "================================================================"
    echo "=== [PRE] checkout buggy versions: $PROJECT ==="
    echo "================================================================"

    for BID in $ALL_BUG_IDS; do
        BUG_DIR="${PROJECTS_ROOT}/${PROJECT}/${PROJECT}-${BID}-buggy"

        if [[ -d "$BUG_DIR" ]]; then
            echo "  [SKIP] ${PROJECT}-${BID}-buggy already exists"
        else
            mkdir -p "${PROJECTS_ROOT}/${PROJECT}"
            echo "  [CHECKOUT] ${PROJECT}-${BID}-buggy ..."
            defects4j checkout -p "$PROJECT" -v "${BID}b" -w "$BUG_DIR"
        fi
    done

    echo ""
fi

# -------------------- step1-8 --------------------

# Step1: Insert add_test code into buggy dirs.
run_step 1 exm3_step1_insert_testcases.py \
    --project "$PROJECT" \
    --defects-root "$PROJECTS_ROOT" \
    $IDS_ARG \
    --results-root "$MR"

# Step2: Run tests on buggy version.
run_step 2 exm3_step2_run_on_buggy.py \
    --project "$PROJECT" \
    --defects-root "$PROJECTS_ROOT" \
    $IDS_ARG \
    --results-root "$MR" \
    --out-dir "$WORK/run_buggy" \
    --ind-out-dir "$WORK/run_buggy_individual" \
    $STEP2_LOG_ARGS

# Step3: JaCoCo coverage on buggy version.
run_step 3 exm3_step3_coverage_on_buggy.py \
    --project "$PROJECT" \
    --defects-root "$PROJECTS_ROOT" \
    $IDS_ARG \
    --results-root "$MR" \
    --out-dir "$WORK/coverage_buggy" \
    --ind-out-dir "$WORK/coverage_buggy_individual" \
    --ind-base-out-dir "$WORK/base_coverage_buggy_individual" \
    --base-out-dir "$WORK/base_coverage_buggy" \
    $STEP3_LOG_ARGS

# Step4: Apply patches to buggy dirs.
run_step 4 exm3_step4_apply_patch.py \
    --patches-root "$PATCHES_ROOT" \
    --projects-root "$PROJECTS_ROOT" \
    --project "$PROJECT" \
    $IDS_ARG \
    --fixed-out-dir "$WORK/fixed_methods"

# Step5: Run tests + JaCoCo coverage on patched version.
run_step 5 exm3_step5_run_on_patched.py \
    --project "$PROJECT" \
    --defects-root "$PROJECTS_ROOT" \
    $IDS_ARG \
    --jacoco \
    --results-root "$MR" \
    --out-dir "$WORK/run_patched" \
    --ind-out-dir "$WORK/run_patched_individual" \
    --methods-dir "$WORK/fixed_methods" \
    --coverage-out-dir "$WORK/coverage_patched" \
    --ind-coverage-out-dir "$WORK/coverage_patched_individual" \
    --ind-base-coverage-out-dir "$WORK/base_coverage_patched_individual" \
    --base-coverage-out-dir "$WORK/base_coverage_patched" \
    $STEP5_LOG_ARGS

# Step6: Check if patch passes trigger tests.
run_step 6 exm3_step6_build_groundtruth.py \
    --project "$PROJECT" \
    --defects-root "$PROJECTS_ROOT" \
    $IDS_ARG \
    --no-jacoco \
    --methods-dir "$WORK/fixed_methods" \
    --coverage-out-dir "$WORK/base_coverage_groundtruth" \
    --fixed-out-dir "$WORK/groundtruth_checkouts" \
    $STEP6_LOG_ARGS

# -------------------- post-step6: remove ALL checked-out buggy dirs --------------------
# Deletes base dirs + all variant copies.
if [[ "$FROM_STEP" -le 6 ]]; then
    echo ""
    echo "================================================================"
    echo "=== [POST-6] cleanup buggy dirs: $PROJECT ==="
    echo "================================================================"

    for BID in $ALL_BUG_IDS; do
        PROJ_DIR="${PROJECTS_ROOT}/${PROJECT}"

        for d in \
            "${PROJ_DIR}/${PROJECT}-${BID}-buggy" \
            "${PROJ_DIR}/${PROJECT}-${BID}-buggy_ind"* \
            "${PROJ_DIR}/${PROJECT}-${BID}_"*"-buggy"
        do
            if [[ -d "$d" ]]; then
                echo "  [RM] $d"
                rm -rf "$d"
            fi
        done
    done

    echo ""
fi

# Step7: Ground truth — checkout defects4j FIXED version, insert add_tests, run tests.
run_step 7 exm3_step7_run_on_groundtruth.py \
    --project "$PROJECT" \
    $IDS_ARG \
    --results-root "$MR" \
    --out-dir "$WORK/run_groundtruth" \
    --ind-out-dir "$WORK/run_groundtruth_individual" \
    $STEP7_LOG_ARGS

# Step8: Aggregate scores.
run_step 8 exm3_step8_score.py \
    --project "$PROJECT" \
    $IDS_ARG \
    --work-root "$WORK" \
    --out-dir "$WORK/final_combined" \
    --out-dir-individual "$OUT_ROOT"

# -------------------- post-step8: remove ALL checked-out fixed dirs --------------------
echo ""
echo "================================================================"
echo "=== [POST-8] cleanup fixed dirs: $PROJECT ==="
echo "================================================================"

for BID in $ALL_BUG_IDS; do
    PROJ_DIR="${PROJECTS_ROOT}/${PROJECT}"

    for d in \
        "${PROJ_DIR}/${PROJECT}-${BID}-fixed" \
        "${PROJ_DIR}/${PROJECT}-${BID}-fixed_ind"* \
        "${PROJ_DIR}/${PROJECT}-${BID}_"*"-fixed"
    do
        if [[ -d "$d" ]]; then
            echo "  [RM] $d"
            rm -rf "$d"
        fi
    done
done

echo ""
echo "================================================================"
echo "=== [DONE] $PROJECT ==="
echo "================================================================"
