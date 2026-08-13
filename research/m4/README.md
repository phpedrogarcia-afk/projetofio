# M4 — PT-BR semantic benchmark research

Status: Isolated research under ADR-040; not product code

This directory contains only synthetic benchmark material and removable local
research tooling. It is not linked from the Android application, Room database,
Time Returns, notifications, analytics, import, or export.

## Fixed boundaries

- No production, participant, imported, or owner journal text.
- No hosted inference or model/vector upload.
- No network access is required to generate, validate, or score the corpus.
- `authoringTargetRating` is a synthetic construction target, not human truth.
- Final held-out selection requires two independent human ratings per pair;
  ADR-041 rejects MiniLM before selection and does not fabricate those labels.
- A model or runtime artifact cannot enter `artifacts/` until its exact source,
  revision, license, size, hash, compatibility, and removal path are recorded.
- Downloaded artifacts and annotation responses are ignored by Git.
- ADR-024 remains Deferred; a benchmark result cannot activate M5.

## Layout

```text
research/m4/
├── README.md
├── RUBRIC.md
├── CANDIDATES.md
├── RESEARCH-DEPENDENCIES.md
├── model-manifest.json               exact local model/tokenizer provenance
├── wheel-lock.json                   exact offline Python wheel provenance
├── corpus/
│   ├── schema.json
│   ├── generate_corpus.py
│   ├── validate_corpus.py
│   ├── m4_pairs_v1.jsonl             generated synthetic corpus
│   └── manifest.json                  generated hash/count ledger
├── annotations/
│   ├── README.md
│   ├── annotator-a.csv                generated blind packet
│   ├── annotator-b.csv                generated blind packet
│   ├── annotator-a.html               generated offline annotation interface
│   └── annotator-b.html               generated offline annotation interface
├── harness/
│   ├── benchmark.py
│   ├── minilm_adapter.py
│   └── test_m4.py
├── results/
│   ├── lexical-rehearsal.json         synthetic rehearsal only
│   └── minilm-development-rehearsal.json
├── prepare_minilm.ps1                 hash-locked removable environment setup
├── run_minilm_rehearsal.ps1           development split only
└── run_checks.ps1                     corpus/interface/privacy contract
```

## Reproduce offline

From the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File research/m4/run_checks.ps1
```

The command regenerates the corpus and blind packets deterministically,
validates the fixed split/privacy/schema contracts, runs the standard-library
tests, and writes a lexical rehearsal. It installs nothing and contacts no
network.

## Retained future human gate

M4-R2 froze corpus/rubric v1. ADR-041 closes V0 with no model selected because
MiniLM failed the development safety/resource gate. The owner does not need to
complete either packet for this rejection. If semantic selection is reopened,
two different people must independently use `annotator-a.html` and
`annotator-b.html`; responses belong under `annotations/responses/` and must
not be committed. Codex output never counts as independent human gold.
