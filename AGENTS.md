# Fio — repository instructions

Fio is a private autobiographical memory application. It lets people write in
their own words and rediscover those words after time has created distance.

Fio is not a productivity tool, a social network, a therapy product, or an AI
companion. Its central promise is:

> Fio does not interpret your story. It returns it to you.

## Read before changing anything

Always read:

- `docs/01-PRODUCT.md`
- `docs/02-PRINCIPLES.md`
- `docs/DECISIONS.md`

Then read the documents relevant to the task:

| Work area | Required context |
|---|---|
| Interface, copy, notifications, accessibility | `docs/03-UX.md` |
| Scope or feature proposals | `docs/04-FEATURES.md`, `docs/10-ROADMAP.md` |
| Long-term direction, time trust, correspondence, Legacy, or feature removal | `docs/11-FOUNDER-VISION.md` |
| Modules, storage, sync, platform choices | `docs/05-ARCHITECTURE.md` |
| Entities, fields, migrations, retention | `docs/06-DATA-MODEL.md` |
| Eligibility, selection, scheduling, feedback | `docs/07-RETURNS-ENGINE.md` |
| Personal data, encryption, logging, export | `docs/08-PRIVACY-SECURITY.md` |
| Telemetry, pilots, feature flags, thresholds | `docs/09-ANALYTICS-EXPERIMENTS.md` |

For work that spans several modules or changes a product invariant, create an
execution plan in `plans/` using `.agent/PLANS.md` before implementation.

## Source-of-truth order

When documents appear to conflict, apply this order:

1. `docs/DECISIONS.md` entries with status **Accepted**
2. `docs/02-PRINCIPLES.md`
3. `docs/01-PRODUCT.md`
4. The domain specification for the affected area
5. `docs/10-ROADMAP.md`
6. `docs/11-FOUNDER-VISION.md`

The founder vision preserves long-term intent but is not implementation
authorization. A Frozen capability remains blocked until a new Accepted
decision and roadmap gate permit it.

Do not resolve a real contradiction by silently choosing one side. Stop,
identify the conflict, and propose the smallest compliant option. Record a new
decision only after explicit human approval.

## Non-negotiable product rules

- The user's words are the product. Do not rewrite, summarize, diagnose, grade,
  or explain their life.
- AI may select, rank, cluster, or retrieve entries. It must not tell the user
  what an entry means.
- Prefer local-first and offline-first behavior. A failed network request must
  not prevent writing or reading local entries.
- Treat text, revisions, embeddings, photos, audio, locations, imports, and
  search queries as private content.
- Never send plaintext journal content or content-derived labels to analytics.
- Returns are invitations, never obligations. No repeated notification after a
  return is ignored.
- Preserve explicit controls: pause returns, never return an entry, let an
  entry rest, and delete/export data.
- Fio should feel smaller than the user's life. When choosing between making
  the app more interesting and preserving a quiet autobiographical experience,
  choose the quiet experience.

Current semantic boundary: ADR-041 ends the V0 semantic path with no model
selected. The M4-R2 corpus/rubric stays frozen, but no annotation is required
to reject the failed MiniLM candidate. Do not fabricate human labels, run M5,
or connect semantic research to Android production. Reopening model selection
requires a new decision and the human/device gates retained by ADR-040.

Search boundary (Manus Mission 4, branch `integration/manus-search-20260819`):
search belongs to the canonical **Encontrar** capability and must never merge
with **Reencontrar** (the returns engine: returns do not use search as a
selection source, search does not produce returns). Search retrieves the
user's exact original words plus factual context (date, snippet); it never
answers autobiographical questions, generates text, or interprets for the
person. Queries are never persisted by default, never logged, and never sent
to analytics. Any analytics for search stays local-first (only
`search_executed`, `result_count_bucket`, `search_mode`, `latency_bucket`);
remote analytics is DECISION REQUIRED. Semantic search, if prototyped, is
Experimental: behind a feature flag, removable, and never connected to Room or
Returns in production (ADR-040). Sealed notes may enter search scope only
after `docs/search/SEARCH-SEALED-THREAT-MODEL.md` exists; if more than one
defensible solution, that is DECISION REQUIRED.

## Never add without an approved decision

- streaks, points, badges, goals, or habit pressure;
- feeds, followers, likes, sharing, or public profiles;
- advertising or monetization of personal data;
- AI chat, generated advice, psychological profiling, or sentiment diagnoses;
- engagement notifications, urgency, red badges, or retention guilt;
- tasks, calendars, reminders disguised as productivity features;
- server-side access to journal plaintext or embeddings;
- the frozen Book or Legacy concepts;
- a new production dependency, external SDK, or network data flow involving
  private content.

## Implementation workflow

1. Inspect the current implementation and relevant tests before editing.
2. State assumptions and check feature status in `docs/04-FEATURES.md`.
3. Make the smallest change that satisfies the approved scope.
4. Keep selection logic, storage, UI, analytics, and sync behind explicit
   boundaries. Do not leak content into logs or event payloads.
5. Add or update tests for product invariants, privacy boundaries, migrations,
   and edge cases.
6. Run the checks documented by the repository. If checks do not exist yet,
   report that honestly; do not invent passing results.
7. Update specifications when behavior changes. Propose a decision record for
   any change to an accepted principle or architecture boundary.

## Decision discipline

Feature labels have precise meanings:

- **Approved**: may be implemented in the listed milestone.
- **Experimental**: must be isolated behind a feature flag and removable.
- **Planned**: direction is accepted, but implementation waits for its phase.
- **Research**: investigate only; do not ship.
- **Deferred**: intentionally postponed; reopening requires a new approved
  decision.
- **Frozen**: retain the idea, do not design or implement now.
- **Rejected**: do not implement.

When a request conflicts with Fio's philosophy, do not silently implement it.
Return:

1. the conflicting principle or accepted decision;
2. the concrete risk;
3. the smallest compliant alternative.

## Current implementation state

Version `v0.1` contains the canonical specification package, an Android M1
engineering baseline accepted by `ADR-037`, and validation-only M2 Time/M3
local-import engineering checkpoints. The UI migrated to the Sage Green
design system v1 (branch `feature/design-ux-v1`) with the ADR-043 temporal
picker and ADR-044 first-capsule copy live on Home. Four Manus missions
progressed the integration branch: Mission 1
(`integration/manus-rehearsal-20260817` — 17 hardening campaigns, 99 unit
tests green, one P0 plaintext-corruption finding fixed in
`AesGcmContentCipher.seal()`, report `MANUS-HARDENING-FINAL.md`), Mission 2
(`integration/manus-pre-codex-20260817` — product red team, pilot package
under `pilot/`, documentation drift repair, Codex handoff; report
`MANUS-PRE-CODEX-FINAL.md`), Mission 3 (`integration/manus-ux-refinement-20260819`
— UX observatory, 3 code fixes G1/G2/G9, 101 unit tests green, report
`MANUS-UX-REFINEMENT-FINAL.md`), and Mission 4 **completed** on
`integration/manus-search-20260819` (final report `MANUS-SEARCH-FINAL.md`,
commit 06307ca; 134 unit tests green) — the first search architecture is in
production: lexical baseline (PT-BR tokenizer, scan-on-demand Option A, sealed
notes content-invisible, soft-deleted excluded, factual "Já voltou" counts,
queries never persisted) plus a removable, isolated hybrid semantic research
prototype (RRF k=60, `SemanticSearchPrototype.kt`, zero shipped weights,
vector arm inherits the full lexical filter contract, never connected to Room
or Returns). Design debt D12 (draft survives a failed entry insert) is
closed, and `loadReturnsForEntry` (read-only) serves the search lens without
schema migration. `docs/search/` holds the architecture, sealed threat model,
and privacy documents; `research/search/` holds the PT-BR benchmark and the
on-device embedding research log. Semantic production shipping stays Deferred
(ADR-024) behind a documented kill criterion: recall@10 ≥ +10pp or MRR ≥
+0.08 vs lexical, RAM ≤ +200MB, latency ≤ 500ms on a real NPU device. Open physical, accessibility, interoperability,
second-OEM, notification-provider, and independent-review checks remain
mandatory before a participant pilot, production release, or
security/audit claim; they must not be reported as passed. No sync service,
remote analytics transport, participant pilot, or product semantic experiment
exists. ADR-040 authorizes only isolated M4 synthetic benchmark research;
ADR-041 records the early-stop `no model selected` outcome. ADR-024 remains
Deferred and no research model may connect to Room or Returns.
`ADR-033` makes Android the current initial-client direction; do not execute the
Superseded iOS/Apple plan or its dependencies. Follow `docs/10-ROADMAP.md` in
order.
