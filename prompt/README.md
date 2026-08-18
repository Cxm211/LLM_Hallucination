# Prompts

The exact system prompts used for the four settings in *"Better Understanding, Better Fixes? A Study of Hallucination in LLM-based Automated Program Repair."*

Every model is queried zero-shot: the file below is sent as the system instruction, and the task instance — the buggy functions, plus the relevant or triggering testcases where the task requires them — is sent as the user content. One response is collected per instance, and the same prompt text is used for all three models (GPT-5, DeepSeek-R1, Claude Sonnet 4.5).

## Files

| Prompt | Setting |
|---|---|
| [baseline.txt](baseline.txt) | Baseline repair |
| [triggering_testcase_identification.txt](triggering_testcase_identification.txt) | Task 1 — Triggering testcase identification |
| [line_coverage_prediction.txt](line_coverage_prediction.txt) | Task 2 — Line coverage prediction |
| [additional_testcase_generation.txt](additional_testcase_generation.txt) | Task 3 — Additional testcase generation |

All four follow the same layout — persona, `Task` / `Given`, `Your job is to`, `Your Workflow`, `Rules`, the output format, and `Hard constraints`.

---

## `baseline.txt` — Baseline repair

**Given:** the complete function(s) where the error occurs.

**Output:**

```json
{ "fixed_code": ["full fixed_function1 as a JSON string", "full fixed_function2 as a JSON string"] }
```

---

## `triggering_testcase_identification.txt` — Task 1

**Given:** the buggy function(s), and all relevant testcases.

**Output:**

```json
{
  "trigger_method": ["full_trigger_method_path1", "full_trigger_method_path2"],
  "fixed_code":     ["full fixed_function1 as a JSON string", "full fixed_function2 as a JSON string"]
}
```

---

## `line_coverage_prediction.txt` — Task 2

**Given:** the buggy function(s) and all triggering testcases.

**Output:**

```json
{
  "function_1": { "buggy": [1, 4, 5, 6], "fixed": [1, 3, 4, 7] },
  "function_2": { "buggy": [2, 3],       "fixed": [2, 3] },
  "fixed_code": ["fixed function_1", "fixed function_2"]
}
```

---

## `additional_testcase_generation.txt` — Task 3

**Given:** the buggy function(s) and all triggering testcases.

**Output:**

```json
{
  "fixed_code": ["fixed function_1", "fixed function_2"],
  "additional_testcases": [
    { "path": "testcase_1_path", "testcase": "testcase_1_code" },
    { "path": "testcase_2_path", "testcase": "testcase_2_code" }
  ]
}
```
