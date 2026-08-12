# Plans

Store approved execution plans here. Follow `.agent/PLANS.md`.

Do not treat a plan as an accepted product decision. Product invariants and
scope remain governed by `docs/DECISIONS.md`, `docs/02-PRINCIPLES.md`, and
`docs/04-FEATURES.md`.

## Active plans

| Plan | Status | Next action |
|---|---|---|
| `2026-08-10-m1-android-trusted-local-core.md` | Engineering baseline accepted | Retain physical/accessibility/external checks as the pre-pilot gate |
| `2026-08-11-m1-m6-automation.md` | In progress | Obtain M2-R1 approval; Git/publication remain separate |
| `2026-08-12-m2-decision-packet.md` | Proposed | Owner approval required before ADR-038/dependency/schema changes |
| `2026-08-12-m2-time-returns.md` | Draft | Wait for M2-R1 approval, then begin pure engine implementation |
| `2026-08-10-m1-trusted-local-core.md` | Blocked historical | Superseded iOS sequence |
| `2026-08-10-m1-decision-packet.md` | Approved historical | Apple portions Superseded by Android ADR-033 |
| `2026-08-10-m1-android-pivot-decision-packet.md` | Approved | Incorporated as Android ADR-033–ADR-036 |

Current evidence:

- `evidence/2026-08-11-m1-poco-manual-validation.md` — physical POCO session in
  progress; real Markdown and TXT exports passed, with biometric,
  accessibility, OEM, and cleanup checks still open.
- `evidence/2026-08-11-m1-emulator-automation.md` — completed API 26/API 36
  automated scope and exact residual physical/external gates.
- `evidence/2026-08-11-m1-plaintext-boundary-trace.md` — implementation trace
  prepared for the still-pending independent review.
