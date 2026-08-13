# M4-R2 — PT-BR corpus and rubric review packet

Status: Accepted — explicitly approved by the project owner on 2026-08-13
Date: 2026-08-13
Authority boundary: Accepted ADR-040 and M4-R1

## Frozen proposal

- schema version 1 with exact SHA-256
  `72a702510cc0ed3e9f72595a8e0df9ed379278b64e223dd1b1a36b30ed73ccef`;
- 300 purpose-written synthetic PT-BR pairs, no personal or participant text;
- 20 scenario families, 60 ranking groups, and five adversarial categories per
  group: indirect relation, near-duplicate, lexical trap, negation, unrelated;
- family-isolated split: 240 development pairs and 60 held-out pairs;
- rating rubric 0–3 plus a separate near-duplicate exclusion;
- two blind 300-row packets with different deterministic orders and no visible
  split, category, construction target, or expected exclusion;
- closed annotation fields only: rating, yes/no near-duplicate, and optional
  `ambiguous_wording` reason code;
- construction targets remain synthetic rehearsal labels and cannot substitute
  for two independent human ratings.

## Automated evidence

- corpus/schema/privacy/split/hash validation passed;
- 11 standard-library offline tests passed;
- no network import exists in the research Python tooling;
- no M4 runtime, model, dependency, or research link exists in Android product
  source/build configuration;
- lexical TF-IDF rehearsal: Spearman `0.135344`, mean nDCG@5 `0.498154`, unsafe
  top-1 `1.0`; this is a deliberately labeled control failure, not model
  evidence or a product metric.

## Approval effect

Approval freezes corpus wording/schema/split/rubric version 1 and permits the
already inventoried M4 candidate artifacts/adapters to proceed under ADR-040.
It does not count as an annotation, accept ADR-024, select a model, authorize
personal text, connect semantics to Returns, or authorize M5/M6, Git, signing,
publication, or release. Final held-out selection still requires two independent
completed blind packets and labeled Android/physical measurements.

Exact approval phrase:

> Aprovo o pacote Android M4-R2 — Corpus e rubrica PT-BR.

Approval received verbatim on 2026-08-13. Corpus/rubric v1 is frozen at the
recorded SHA-256. This approval is not an annotation or a model decision.
