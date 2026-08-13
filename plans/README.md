# Plans

Store approved execution plans here. Follow `.agent/PLANS.md`.

Do not treat a plan as an accepted product decision. Product invariants and
scope remain governed by `docs/DECISIONS.md`, `docs/02-PRINCIPLES.md`, and
`docs/04-FEATURES.md`.

## Active plans

| Plan | Status | Next action |
|---|---|---|
| `2026-08-10-m1-android-trusted-local-core.md` | Engineering baseline accepted | Retain physical/accessibility/external checks as the pre-pilot gate |
| `2026-08-11-m1-m6-automation.md` | Automated scope complete | Owner-test validation APK installed; retained human/external gates remain |
| `2026-08-12-m2-decision-packet.md` | Accepted | Recorded as ADR-038 |
| `2026-08-12-m2-time-returns.md` | Engineering complete | Retain physical/accessibility/external checks as pre-pilot gates |
| `2026-08-13-m3-decision-packet.md` | Accepted | Recorded as ADR-039 |
| `2026-08-13-m3-local-import.md` | Engineering complete | Retain participant/personal-file gates; review M4-R1 separately |
| `2026-08-13-m4-decision-packet.md` | Accepted | Recorded as ADR-040; ADR-024 remains Deferred |
| `2026-08-13-m4-portuguese-semantic-benchmark.md` | Complete | ADR-041: no V0 model; M5 deferred; M6 remove/do not introduce |
| `2026-08-13-m4-corpus-rubric-review.md` | Accepted | Corpus/rubric v1 frozen; not an annotation/model decision |
| `2026-08-10-m1-trusted-local-core.md` | Blocked historical | Superseded iOS sequence |
| `2026-08-10-m1-decision-packet.md` | Approved historical | Apple portions Superseded by Android ADR-033 |
| `2026-08-10-m1-android-pivot-decision-packet.md` | Approved | Incorporated as Android ADR-033–ADR-036 |

Current evidence:

- `evidence/2026-08-13-v0-time-only-validation.md` — current integrated build,
  API 26/API 36 matrix, adverse profiles, and POCO installation/smoke evidence.
- `evidence/2026-08-13-m4-minilm-development.md` — hash-verified offline
  development rehearsal, dependency lock, resource result, and stop disposition.
- `evidence/2026-08-13-m4-research-foundation.md` — deterministic synthetic
  corpus/control/inventory evidence and the exact open human/model gates.
- `evidence/2026-08-13-m3-local-import.md` — completed validation-only local
  import engineering evidence and exact residual participant/personal-file gates.
- `evidence/2026-08-13-m2-time-returns.md` — completed validation-only Time
  Returns engineering evidence and exact residual pre-pilot gates.
- `evidence/2026-08-11-m1-poco-manual-validation.md` — physical POCO session in
  progress; real Markdown and TXT exports passed, with biometric,
  accessibility, OEM, and cleanup checks still open.
- `evidence/2026-08-11-m1-emulator-automation.md` — completed API 26/API 36
  automated scope and exact residual physical/external gates.
- `evidence/2026-08-11-m1-plaintext-boundary-trace.md` — implementation trace
  prepared for the still-pending independent review.
