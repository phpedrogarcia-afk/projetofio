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
- **Frozen**: retain the idea, do not design or implement now.
- **Rejected**: do not implement.

When a request conflicts with Fio's philosophy, do not silently implement it.
Return:

1. the conflicting principle or accepted decision;
2. the concrete risk;
3. the smallest compliant alternative.

## Current implementation state

Version `v0.1` contains the canonical specification package and an Android M1
engineering baseline accepted by `ADR-037`. Its open physical, accessibility,
interoperability, second-OEM, and independent-review checks remain mandatory
before a participant pilot, production release, or security/audit claim; they
must not be reported as passed. M2 engineering may proceed with synthetic data
after its execution plan is approved. No sync service, Return delivery,
analytics transport, or semantic experiment exists. `ADR-033` makes Android
the current initial-client direction; do not execute the Superseded iOS/Apple
plan or its dependencies. Follow `docs/10-ROADMAP.md` in order.
