# M4/M6 semantic early-stop evidence

Date: 2026-08-13
Decision: ADR-041 — no V0 model selected

## Owner direction

The sole project owner asked Codex to automate the confirmation work and stated
that the owner will test the application when it is ready. Codex cannot
truthfully impersonate two independent human annotators, so no human-rating
claim was created.

## Evidence used

- Frozen synthetic corpus/rubric v1: 300 pairs; SHA-256
  `72a702510cc0ed3e9f72595a8e0df9ed379278b64e223dd1b1a36b30ed73ccef`.
- Lexical construction-target rehearsal: Spearman `0.135344`, mean nDCG@5
  `0.498154`, unsafe top-1 `1.0`.
- MiniLM development-only rehearsal: Spearman `0.345850`, mean nDCG@5
  `0.483447`, unsafe top-1 `1.0`.
- MiniLM local research profile: approximately 118 MB model, 127.5 MB including
  tokenizer, and about 521 MB additional Windows working set in the recorded run.
- Held-out evaluated: no. Human gold used: no. Android product linked: no.
- M4 offline contract suite: 13 tests passing before this decision.

## Outcome

MiniLM is rejected before held-out annotation or Android integration. V0 keeps
Time Returns and introduces no embedding model, semantic index, dependency,
network inference, or M5 participant pilot. The M6 semantic branch is
`Remove / do not introduce`.

If semantic selection is reopened, ADR-040 still requires two independent human
ratings per held-out pair plus Android/physical, license, provenance, privacy,
and resource evidence. Codex-generated labels cannot satisfy that gate.
