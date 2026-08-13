# Milestone 4 — Portuguese semantic benchmark research

Status: Complete — early stop with no model selected under ADR-041
Owner: Project owner with Codex implementation support
Related milestone: Milestone 4 — Portuguese semantic benchmark
Relevant decisions: ADR-003, ADR-015, ADR-020, ADR-024, ADR-033, ADR-037,
ADR-040, ADR-041

## Outcome

Produce reproducible, offline evidence about whether any reviewed on-device
encoder merits consideration for a later controlled experiment. Report quality,
human disagreement, privacy, license, provenance, device cost, and support; a
valid result may be `no model selected`.

## Non-goals

- Product Returns integration, semantic Archive/search, personal content,
  participant pilot, telemetry, hosted inference, server, sync, or model upload.
- Accepting ADR-024 or choosing a winner before held-out and physical evidence.
- M5/M6 implementation, Git, signing, publication, release, or production claim.

## Implementation sequence after approval

1. Freeze benchmark schema, 0–3 rubric, split/seed, authoring rules, license,
   consent, and exclusion checks.
2. Build a removable research harness with a deterministic lexical control and
   Fio-owned candidate adapter contract; keep it outside product Returns.
3. Author/review about 300 synthetic PT-BR pairs and freeze 60 held-out pairs
   before candidate scoring. Record human rating/disagreement as a real gate.
4. Inspect current official runtimes/model cards and write the candidate
   inventory. Stop before download when license, provenance, Android coverage,
   tokenizer, hash, or removal path is unclear.
5. After inventory review, pin and authenticate only approved artifacts, run
   quality/error analysis, and keep held-out results separate.
6. Run offline API 26/API 36 compatibility and resource measurements. Use only
   synthetic text on any physical device and label emulator versus physical
   evidence explicitly.
7. Produce a comparison report with uncertainty and one recommendation: no
   model, continue measuring one bounded candidate, or propose ADR-024 for
   explicit acceptance. Remove downloaded candidates not retained for review.

## Tests and acceptance evidence

- deterministic schema/split/hash and no held-out leakage;
- closed synthetic corpus with no production Entry or arbitrary path payload;
- adapter equivalence, offline/no-network enforcement, cancellation, malformed
  artifact, unsupported API, and removal tests;
- rank/top-k/error metrics plus annotator disagreement and uncertainty;
- model/runtime license, provenance, exact hash, dependency and removal ledger;
- API 26/API 36 compatibility and labeled physical latency/memory/energy/thermal
  measurements before any device-performance claim.

## Rollback/disable path

Delete the removable research adapters/artifacts/results that are not required
as approved aggregate evidence. No Room migration or canonical Entry mutation
is permitted. Time Returns and M1–M3 remain independently functional.

## Gate

M4-R2 was approved and corpus/rubric version 1 is frozen. MiniLM's hash-verified
development-only rehearsal failed the safety/resource gate and did not advance
to held-out or Android. ADR-041 closes this M4 run with `no model selected`;
therefore the two-human gate need not be completed merely to reject MiniLM.
It remains mandatory if semantic selection is reopened. M5 is deferred and the
M6 semantic outcome is `Remove / do not introduce`; Time remains core.

## Progress log

- 2026-08-13 — The project owner explicitly approved Android M4-R1. ADR-040
  records an isolated, offline, synthetic research boundary. ADR-024 remains
  Deferred; no model, runtime dependency, semantic index, personal content, or
  product Return integration is accepted by this approval.
- 2026-08-13 — Created `research/m4/` outside product source with schema/rubric,
  300 purpose-written synthetic PT-BR pairs, 20 isolated scenario families, 60
  ranking groups, a 240/60 development/held-out split, two blind packets, strict
  validation, a deterministic TF-IDF control, and response validation tooling.
  Corpus SHA-256 is
  `72a702510cc0ed3e9f72595a8e0df9ed379278b64e223dd1b1a36b30ed73ccef`.
- 2026-08-13 — Eleven offline tests pass. The synthetic-target lexical rehearsal
  reports Spearman `0.135344`, mean nDCG@5 `0.498154`, and unsafe top-1 `1.0`;
  it is explicitly not human/model evidence. Product source has no M4 runtime,
  dependency, research path, or model link. The current-source inventory records
  MediaPipe tasks-text 1.0.0, two multilingual candidates, and excludes
  BERTimbau Base as a direct sentence encoder. M4-R2 is the next human gate.
- 2026-08-13 — The owner approved M4-R2 verbatim, freezing corpus/rubric v1.
  Downloaded MiniLM revision `e8f8c211...` and its tokenizer into ignored local
  research storage, verified their recorded SHA-256 values, resolved six pinned
  wheels, installed them offline/no-deps in an ignored Python 3.14 environment,
  and proved that hub/requests/httpx clients are absent.
- 2026-08-13 — Two development-only CPU runs produced identical aggregate
  quality values: Spearman `0.345850`, mean nDCG@5 `0.483447`, and unsafe top-1
  `1.0`. The second run loaded in 850 ms, encoded 288 unique texts in 11.96 s
  (41.5 ms/text), and added about 521 MB to the Windows process working set.
  Held-out/human-gold use remained false and product linkage false. MiniLM is
  not advanced to Android. Added two self-contained offline annotation pages;
  the suite now passes 13 tests. Two human packets are the next real gate.
- 2026-08-13 — The sole project owner asked Codex to automate the confirmations
  and reserved personal testing for the finished application. ADR-041 records
  the truthful bounded outcome: Codex does not impersonate two human raters;
  MiniLM is rejected at the development gate, no model is selected, M5 is
  deferred, and M6 records `Remove / do not introduce` for V0.

## Final report

The isolated M4 research boundary, frozen 300-pair corpus/rubric, candidate
inventory, lexical control, hash-locked MiniLM adapter, aggregate development
evidence, and 13 offline contract tests are complete. MiniLM failed before
held-out/Android work. No personal text, product dependency, semantic index,
Room migration, Return integration, network inference, or human-label claim
was introduced. The retained research tree can be removed without affecting
M1–M3 or Time Returns.
