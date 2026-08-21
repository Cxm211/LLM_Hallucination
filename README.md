# Better Understanding, Better Fixes? A Study of Hallucination in LLM-based Automated Program Repair

Replication package for the paper *"Better Understanding, Better Fixes? A Study of Hallucination in LLM-based Automated Program Repair."*

Xuemeng Cai, Jiakun Liu, Linhan Yang, Wei Ma, Lingxiao Jiang

Singapore Management University · Harbin Institute of Technology · Blekinge Institute of Technology

---

## Overview

Evaluations of LLM-based automated program repair are largely **result-centric**: they count how many patches pass the developer-written test suite, which says little about whether a repair is grounded in a faithful understanding of the bug.

This study analyses hallucination at two levels — **repair hallucination** in the final patch, and **understanding hallucination** in the intermediate artifacts that guide it — through three execution-grounded tasks, each of which also requires the model to produce a patch:

| Setting | Task |
|---|---|
| `baseline` | Baseline repair — the model sees only the buggy functions and returns a patch |
| `exm1` | Task 1 — Triggering testcase identification |
| `exm2` | Task 2 — Line coverage prediction |
| `exm3` | Task 3 — Additional testcase generation |

**Subjects:** GPT-5, DeepSeek-R1, and Claude Sonnet 4.5 × 832 Defects4J bugs across 17 projects, queried zero-shot through provider APIs in November 2025.

## Repository layout

```
.
├── prompt/         the exact system prompt sent for each setting
├── scripts/        the pipeline: query the models, then evaluate what they returned
├── results/        what the models produced, and what running the tests produced
└── human_labels/   the manual annotations behind the hallucination taxonomies
```

Each directory carries its own README with the details.

### [`prompt/`](prompt/)

Four `.txt` files, one per setting, byte-identical to what was sent as the system instruction. [prompt/README.md](prompt/README.md) documents what each prompt is given and the JSON schema it must return.

### [`scripts/`](scripts/)

Two stages, one directory each. [scripts/README.md](scripts/README.md) documents every argument and gives a runnable example per script.

- **`generation/`** builds the API requests from `results/data/` and `prompt/`, submits them, and stores the raw responses. One submit script per provider, since the three APIs differ.
- **`evaluation/`** applies each generated patch to a fresh Defects4J checkout, runs the developer-written test suite, collects JaCoCo coverage, and scores the outcome. One driver per setting, each chaining numbered step scripts.

Generation needs an API key; evaluation needs a local Defects4J installation and JaCoCo. Neither writes into `results/`.

### [`results/`](results/)

- **`data/`** — what the models produced: the input given to each model, its parsed response, the extracted patch, and the setting's intermediate artifact, one directory per bug.
- **`evaluation/`** — what running the tests produced: patch outcomes, coverage precision/recall/F1, and generated-testcase validity, one CSV per project.

[results/README.md](results/README.md) documents every file and every column.

### [`human_labels/`](human_labels/)

Case-level annotations produced by two annotators through independent open coding and conflict resolution, covering both repair hallucination and understanding hallucination. [human_labels/README.md](human_labels/README.md) is the codebook: the taxonomies, the column schemas, and the value domain of every label.
<!-- 
## Citation

```bibtex
@article{cai2026hallucination,
  title   = {Better Understanding, Better Fixes? A Study of Hallucination in LLM-based Automated Program Repair},
  author  = {Cai, Xuemeng and Liu, Jiakun and Yang, Linhan and Ma, Wei and Jiang, Lingxiao},
  year    = {2026}
}
``` -->
