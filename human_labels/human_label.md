# Human Annotation Codebook

Case-level manual annotations for *"Better Understanding, Better Fixes? A Study of Hallucination in LLM-based Automated Program Repair."*

Two kinds of hallucination are annotated, each with its own label set:

- **Repair hallucination** — how a model-generated **patch** deviates from the developer-intended repair.
- **Understanding hallucination** — how a model-generated **intermediate artifact** deviates from the execution-grounded oracle.

## Layout

```
human_labels/
├── Repair_hallucination/
│   ├── Triggering_testcase_identification/    # 94 rows per model
│   ├── Line_coverage_prediction/              # 87 rows per model
│   └── Additional_testcase_generation/        # 92 / 90 / 87 rows
└── Understanding_hallucination/
    ├── Line_coverage_prediction/              # 21 / 6 / 4 rows
    └── Additional_testcase_generation/        # 92 / 90 / 87 rows
```

Every leaf folder holds one file per model — `Claude.csv`, `DeepSeek.csv`, `GPT5.csv` — and row counts above are listed in that order.

---

## `Repair_hallucination/<Task>/<Model>.csv`

One row per sampled case. The annotated object is the model-generated patch.

| Column | Type | Description |
|---|---|---|
| `project` | string | Defects4J project name. |
| `bug_id` | int | Numeric Defects4J bug identifier within that project. |
| `label` | enum | Exactly one value from the repair-hallucination taxonomy. |
| `overfitting` | `Yes` \| `No` | Whether the patch passes all developer-written testcases while deviating from the developer-intended repair semantics. Independent of `label`. |
| `label_if_others` | string | Finer-grained annotation for rows whose `label` is `Others`; empty otherwise. |


---

## `Understanding_hallucination/Line_coverage_prediction/<Model>.csv`

One row per bug. The annotated object is a low-scoring coverage prediction; the buggy-program and model-patched-program predictions are selected and labeled independently.

| Column | Type | Description |
|---|---|---|
| `project` | string | Defects4J project name. |
| `bug_id` | int | Numeric Defects4J bug identifier. |
| `buggy_label` | enum set | Code structures associated with the prediction on the **buggy** program. |
| `fixed_label` | enum set | Code structures associated with the prediction on the **model-patched** program. |


---

## `Understanding_hallucination/Additional_testcase_generation/<Model>.csv`

One row per generated testcase. Multiple testcases for the same bug share one model-generated patch but are annotated independently.

| Column | Type | Description |
|---|---|---|
| `project` | string | Defects4J project name. |
| `bug_id` | int | Numeric Defects4J bug identifier. |
| `add_test_file` | string | Which generated testcase the row refers to (`add_test.java`, `add_test1.java`, …), locating the file at `exm3/<Model>/<project>/<bug_id>/<add_test_file>`. |
| `label` | enum | Exactly one value from the understanding-hallucination taxonomy. |
| `label_if_others` | string | Finer-grained annotation for rows whose `label` is `Others`. Present only in `DeepSeek.csv`. |

