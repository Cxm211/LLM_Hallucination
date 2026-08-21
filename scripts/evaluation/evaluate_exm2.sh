#!/usr/bin/env bash
# evaluate_exm2.sh — run exm2 step1-6 for one model
#
# Usage:
#   ./evaluate_exm2.sh --model GPT5
#   ./evaluate_exm2.sh --model GPT5 --project Chart
#   ./evaluate_exm2.sh --model GPT5 --project Chart --ids 1,3,5-7
#   ./evaluate_exm2.sh --model GPT5 --from-step 4
#   ./evaluate_exm2.sh --model GPT5 --from-step 4 --project Chart

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# -------------------- defaults --------------------
MODEL=""
PROJECT=""
IDS=""
FROM_STEP=1
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
DEFECTS_ROOT="${DEFECTS_ROOT:-${REPO_ROOT}/defects4j_checkouts}"
METHODS_DIR="${SCRIPT_DIR}/bug_methods"

# -------------------- parse args --------------------
while [[ $# -gt 0 ]]; do
    case "$1" in
        --model)     MODEL="$2";     shift 2 ;;
        --project)   PROJECT="$2";   shift 2 ;;
        --ids)       IDS="$2";       shift 2 ;;
        --from-step) FROM_STEP="$2"; shift 2 ;;
        *) echo "[ERROR] unknown argument: $1"; exit 1 ;;
    esac
done

if [[ -z "$MODEL" ]]; then
    echo "Usage: $0 --model <MODEL> [--project <PROJECT>] [--ids <expr>] [--from-step N]"
    echo "  MODEL: Claude, DeepSeek, GPT5"
    exit 1
fi

RESULTS_ROOT="${RESULTS_ROOT:-${REPO_ROOT}/results/data/exm2}/$MODEL"
LOGS_ROOT="${LOGS_ROOT:-${REPO_ROOT}/test-logs/exm2}/$MODEL"
OUT_ROOT="${OUT_ROOT:-${REPO_ROOT}/generated_evaluation/exm2}/$MODEL"
# Steps 1-5 write their intermediates here rather than into the published results tree
WORK_ROOT="${WORK_ROOT:-${REPO_ROOT}/generated_evaluation/exm2_work}/$MODEL"

if [[ ! -d "$RESULTS_ROOT" ]]; then
    echo "[ERROR] results root not found: $RESULTS_ROOT"
    exit 1
fi

# Discover projects: directories in RESULTS_ROOT that don't start with z_
if [[ -n "$PROJECT" ]]; then
    # --project accepts a single project or a comma-separated list (e.g. JacksonCore,JxPath)
    IFS=',' read -r -a PROJECTS <<< "$PROJECT"
else
    PROJECTS=()
    for d in "$RESULTS_ROOT"/*/; do
        name="$(basename "$d")"
        [[ "$name" == z_* ]] && continue
        [[ "$name" == __* ]] && continue
        PROJECTS+=("$name")
    done
fi

# Map project name to lowercase for methods CSV lookup
to_lower() { echo "$1" | tr '[:upper:]' '[:lower:]'; }

IDS_ARG=""
[[ -n "$IDS" ]] && IDS_ARG="--ids $IDS"

# Expand an --ids expression like "40,72" or "5-7,10" into a space-separated list
expand_ids() {
    local expr="$1" out="" part lo hi i
    local -a parts
    IFS=',' read -r -a parts <<< "$expr"
    for part in "${parts[@]}"; do
        if [[ "$part" == *-* ]]; then
            lo="${part%-*}"; hi="${part#*-}"
            for ((i=lo; i<=hi; i++)); do out="$out $i"; done
        else
            out="$out $part"
        fi
    done
    echo "$out"
}

# -------------------- helper --------------------
run_step() {
    local step="$1"; shift
    local cmd=("$@")
    if [[ "$step" -lt "$FROM_STEP" ]]; then
        echo "=== [SKIP] step${step} (--from-step=${FROM_STEP}) ==="
        return
    fi
    echo ""
    echo "================================================================"
    echo "=== step${step}: ${cmd[*]} ==="
    echo "================================================================"
    time python3 "${cmd[@]}"
}

echo "model      : $MODEL"
echo "projects   : ${PROJECTS[*]}"
echo "ids        : ${IDS:-all}"
echo "from-step  : $FROM_STEP"
echo ""

# -------------------- pre-step: checkout ALL active buggy versions --------------------
if [[ "$FROM_STEP" -le 2 ]]; then
    echo "================================================================"
    echo "=== [PRE] checkout buggy versions ==="
    echo "================================================================"
    for proj in "${PROJECTS[@]}"; do
        if [[ -n "$IDS" ]]; then
            ALL_BUG_IDS=$(expand_ids "$IDS" | tr -s ' ')
        else
            # Only the bugs this model actually produced a patch for. Using `defects4j bids`
            # here would pull in bugs outside the study and add spurious rows to the output.
            ALL_BUG_IDS=$(ls -1 "$RESULTS_ROOT/$proj" 2>/dev/null \
                | grep -E '^[0-9]+$' | sort -n | tr '\n' ' ' | tr -s ' ')
        fi
        echo "  $proj active IDs: ${ALL_BUG_IDS:-none}"
        for BID in $ALL_BUG_IDS; do
            BUG_DIR="${DEFECTS_ROOT}/${proj}/${proj}-${BID}-buggy"
            if [[ -d "$BUG_DIR" ]]; then
                echo "  [SKIP] ${proj}-${BID}-buggy already exists"
            else
                mkdir -p "${DEFECTS_ROOT}/${proj}"
                echo "  [CHECKOUT] ${proj}-${BID}-buggy ..."
                defects4j checkout -p "$proj" -v "${BID}b" -w "$BUG_DIR"
            fi
        done
    done
    echo ""
fi

# -------------------- step0: coverage of the unpatched buggy program --------------------
# The checkouts are still unmodified at this point, so this is the coverage the model's
# line prediction for the buggy version is scored against. It does not depend on the model,
# so it is computed once and cached; delete BUGGY_COVERAGE to force a rebuild.
BUGGY_COVERAGE="${BUGGY_COVERAGE:-${REPO_ROOT}/generated_evaluation/exm2_work/coverage_buggy}"
if [[ "$FROM_STEP" -le 1 ]]; then
    for proj in "${PROJECTS[@]}"; do
        if [[ -f "$BUGGY_COVERAGE/${proj}.csv" ]]; then
            echo "--- step0: $proj already covered, skipping ---"
            continue
        fi
        bug_root="$DEFECTS_ROOT/$proj"
        [[ -d "$bug_root" ]] || continue
        echo "--- step0: $proj ---"
        time python3 "$SCRIPT_DIR/exm2_step2_run_tests_with_coverage.py" \
            --bug-root "$bug_root" \
            --logs-root "$WORK_ROOT/logs_buggy" \
            --jacoco \
            $IDS_ARG
        time python3 "$SCRIPT_DIR/exm2_step3_parse_coverage.py" "$DEFECTS_ROOT" \
            --methods-dir "$SCRIPT_DIR/bug_methods" \
            -o "$BUGGY_COVERAGE" \
            --error-log "$BUGGY_COVERAGE/errors.csv" \
            --project "$proj"
    done
fi

# -------------------- step1: apply patches --------------------
# Per project: needs --csv, --patches-root, --projects-root, --project
if [[ "$FROM_STEP" -le 1 ]]; then
    echo ""
    echo "================================================================"
    echo "=== step1: exm2_step1_apply_patch.py ==="
    echo "================================================================"
    for proj in "${PROJECTS[@]}"; do
        proj_lower="$(to_lower "$proj")"
        csv_file="$METHODS_DIR/${proj_lower}_methods.csv"
        if [[ ! -f "$csv_file" ]]; then
            echo "  [SKIP] $proj: methods CSV not found: $csv_file"
            continue
        fi
        echo "--- step1: $proj ---"
        time python3 exm2_step1_apply_patch.py \
            --csv "$csv_file" \
            --patches-root "$RESULTS_ROOT/$proj" \
            --projects-root "$DEFECTS_ROOT" \
            --project "$proj" \
            --fixed-out-dir "$WORK_ROOT/fixed_methods" \
            $IDS_ARG
    done
fi

# -------------------- step2: run tests + jacoco --------------------
# Per project: needs --bug-root, --logs-root, --jacoco
if [[ "$FROM_STEP" -le 2 ]]; then
    echo ""
    echo "================================================================"
    echo "=== step2: exm2_step2_run_tests_with_coverage.py ==="
    echo "================================================================"
    for proj in "${PROJECTS[@]}"; do
        bug_root="$DEFECTS_ROOT/$proj"
        if [[ ! -d "$bug_root" ]]; then
            echo "  [SKIP] $proj: bug root not found: $bug_root"
            continue
        fi
        echo "--- step2: $proj ---"
        time python3 exm2_step2_run_tests_with_coverage.py \
            --bug-root "$bug_root" \
            --logs-root "$LOGS_ROOT" \
            --jacoco \
            $IDS_ARG
    done
fi

# -------------------- step3: parse jacoco coverage --------------------
# Runs on all projects at once (or filtered by --project)
if [[ "$FROM_STEP" -le 3 ]]; then
    echo ""
    echo "================================================================"
    echo "=== step3: exm2_step3_parse_coverage.py ==="
    echo "================================================================"
    if [[ -n "$PROJECT" ]]; then
        for proj in "${PROJECTS[@]}"; do
            echo "--- step3: $proj ---"
            time python3 exm2_step3_parse_coverage.py "$DEFECTS_ROOT" \
                --methods-dir "$WORK_ROOT/fixed_methods" \
                -o "$WORK_ROOT/coverage_patched" \
                --error-log "$WORK_ROOT/coverage_patched/errors.csv" \
                --project "$proj"
        done
    else
        time python3 exm2_step3_parse_coverage.py "$DEFECTS_ROOT" \
            --methods-dir "$WORK_ROOT/fixed_methods" \
            -o "$WORK_ROOT/coverage_patched" \
            --error-log "$WORK_ROOT/coverage_patched/errors.csv"
    fi
fi

# -------------------- step4: add prediction --------------------
if [[ "$FROM_STEP" -le 4 ]]; then
    echo ""
    echo "================================================================"
    echo "=== step4: exm2_step4_align_prediction.py ==="
    echo "================================================================"
    if [[ -n "$PROJECT" ]]; then
        for proj in "${PROJECTS[@]}"; do
            echo "--- step4: $proj ---"
            time python3 exm2_step4_align_prediction.py \
                --buggy-dir "$BUGGY_COVERAGE" \
                --fixed-dir "$WORK_ROOT/coverage_patched" \
                --expn-root "$RESULTS_ROOT" \
                -o "$WORK_ROOT/prediction_vs_actual" \
                --project "$proj"
        done
    else
        time python3 exm2_step4_align_prediction.py \
            --buggy-dir "$BUGGY_COVERAGE" \
            --fixed-dir "$WORK_ROOT/coverage_patched" \
            --expn-root "$RESULTS_ROOT" \
            -o "$WORK_ROOT/prediction_vs_actual"
    fi
fi

# -------------------- step5: parse test results --------------------
if [[ "$FROM_STEP" -le 5 ]]; then
    echo ""
    echo "================================================================"
    echo "=== step5: exm2_step5_collect_patch_results.py ==="
    echo "================================================================"
    time python3 exm2_step5_collect_patch_results.py \
        --root "$LOGS_ROOT" \
        --out "$WORK_ROOT/patch_results"
fi

# -------------------- step6: final score --------------------
if [[ "$FROM_STEP" -le 6 ]]; then
    echo ""
    echo "================================================================"
    echo "=== step6: exm2_step6_score.py ==="
    echo "================================================================"
    time python3 "$SCRIPT_DIR/exm2_step6_score.py" \
        --prediction_root "$WORK_ROOT/prediction_vs_actual" \
        --test_results_root "$WORK_ROOT/patch_results" \
        --out_dir "$OUT_ROOT"
fi

# -------------------- post-all: remove ALL checked-out buggy dirs --------------------
if [[ "$FROM_STEP" -le 2 ]]; then
    echo ""
    echo "================================================================"
    echo "=== [POST-ALL] cleanup buggy dirs ==="
    echo "================================================================"
    for proj in "${PROJECTS[@]}"; do
        if [[ -n "$IDS" ]]; then
            ALL_BUG_IDS=$(expand_ids "$IDS" | tr -s ' ')
        else
            # Only the bugs this model actually produced a patch for. Using `defects4j bids`
            # here would pull in bugs outside the study and add spurious rows to the output.
            ALL_BUG_IDS=$(ls -1 "$RESULTS_ROOT/$proj" 2>/dev/null \
                | grep -E '^[0-9]+$' | sort -n | tr '\n' ' ' | tr -s ' ')
        fi
        for BID in $ALL_BUG_IDS; do
            BUG_DIR="${DEFECTS_ROOT}/${proj}/${proj}-${BID}-buggy"
            if [[ -d "$BUG_DIR" ]]; then
                echo "  [RM] $BUG_DIR"
                rm -rf "$BUG_DIR"
            fi
        done
    done
    echo ""
fi

echo ""
echo "================================================================"
echo "=== [DONE] $MODEL ==="
echo "================================================================"
