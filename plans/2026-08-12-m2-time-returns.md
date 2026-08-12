# Milestone 2 — Android Time Returns

Status: Draft — waits for M2-R1 owner approval
Owner: Project owner with Codex implementation support
Related milestone: Milestone 2 — Time Returns
Relevant decisions: ADR-003, ADR-006–ADR-008, ADR-010, ADR-012,
ADR-017, ADR-021, ADR-027, ADR-033–ADR-037; proposed M2-R1/ADR-038

## Outcome

In the isolated validation environment, a person can explicitly enable local
Time Returns, receive at most one quiet generic Android notification for an
eligible old synthetic Entry, open the exact original date and words, close it,
make that Entry ineligible, or pause all Returns. The same app remains fully
usable when offline, when notifications are denied, and when Android delays or
restarts background work.

## Non-goals

- Participant enrollment, a Time-only pilot, real journal data, analytics
  transport, or feedback collection.
- Import, Rest, Remember Later, Sealed entries, search, semantic candidates,
  embeddings, model dependencies, or experimental assignment.
- Exact delivery promises, repeated reminders, badges, engagement optimization,
  remote push, backend, account, sync, or network permission.
- Primary/release activation while ADR-037 pre-pilot gates remain open.
- Git initialization, commit, remote, push, signing, publication, or release.

## Context read

- `AGENTS.md`
- `.agent/PLANS.md`
- `docs/01-PRODUCT.md` through `docs/10-ROADMAP.md`
- `docs/DECISIONS.md`, including ADR-037
- `plans/2026-08-10-m1-android-trusted-local-core.md`
- `plans/2026-08-11-m1-m6-automation.md`
- `plans/evidence/2026-08-11-m1-emulator-automation.md`
- M1 domain models, repository interface, Room entities/DAO/database/repository,
  application service, graph, ViewModel, Compose UI, manifest, strings, Gradle
  version catalogue, module build file, and existing unit/instrumented tests.
- Official Android WorkManager `2.11.2`, persistent-work, unique-work, and API
  33+ notification-permission documentation checked on 2026-08-12.

## Assumptions and open questions

Verified:

- M1's engineering baseline is accepted under ADR-037; its external checks are
  retained as pre-pilot gates.
- Room schema 1 contains Entry, Draft, and singleton AppSettings; it contains no
  Return table and no migration.
- `returnConsentState` already defaults to `NOT_CONFIGURED`; M1 code cannot
  enable it.
- The project has no WorkManager dependency, notification permission/channel,
  notification scheduler, or Return UI.
- WorkManager `2.11.2` is the current stable official AndroidX release, supports
  the existing Android baseline, and is recommended for persistent deferrable
  work. Android 13+ requires runtime `POST_NOTIFICATIONS` permission.

Open approval gate:

- `plans/2026-08-12-m2-decision-packet.md` must be explicitly approved before
  recording ADR-038, changing Gradle dependencies, migrating the database, or
  implementing M2.

## Implementation sequence

1. **Record the accepted boundary.** After owner approval, add ADR-038, mark
   this plan Approved/In progress, correct the two stale iOS background wording
   references in `docs/07-RETURNS-ENGINE.md`, and record the exact resolved
   WorkManager graph/license/hash/removal path.
2. **Build the pure Time engine first.** Add Fio-owned clock/calendar/random and
   scheduling interfaces; model eligibility, age buckets, bootstrap windows,
   rolling seven-day cap, one pending Return, no repeat in V0, policy precedence,
   quiet hours, expiration, and typed content-free reason codes. Keep Android,
   Room, notifications, WorkManager, UI, and analytics out of pure tests.
3. **Migrate persistence `1 → 2`.** Add Entry return counters/timestamps,
   AppSettings quiet-hour and permission-observation fields, and a V0 one-entry
   Return table with explicit lifecycle fields and indexes. Migration defaults
   stay private: `NOT_CONFIGURED`, no pending Return, no scheduled work. Export
   schema `2.json`; test schema 1 fixture to 2 and failed/interrupted migration
   without destructive fallback.
4. **Add transactional Return use cases.** Evaluate and persist at most one
   selected Return, acknowledge scheduling without false success, reconcile a
   crash between persistence and scheduler acknowledgment, expire or cancel
   stale attempts, re-check policy before display, pause/resume globally, and
   set `Never Return` while cancelling all references for that Entry.
5. **Add the Android scheduling adapter.** Pin approved WorkManager artifacts;
   inject a WorkerFactory or bounded graph lookup; enqueue only uniquely named
   one-time work; use no network constraint or content-bearing WorkData. App
   launch, native save, explicit consent/resume, timezone/time change where
   supported, and worker completion reconcile the next opportunity. Cancellation
   removes only Fio's unique work.
6. **Add the notification adapter.** Declare `POST_NOTIFICATIONS`; create the
   low-importance, no-badge `Devoluções` channel only after Return consent;
   post `Algo seu voltou.` with one opaque Return ID and a safe immutable/update-
   current PendingIntent. Denial/dismissal remains quiet. Never include Entry ID,
   text, date, algorithm, score, preview, or rich lock-screen content.
7. **Add validation-only onboarding and controls.** Gate M2 activation by an
   immutable build capability that is true only for `validation` and tests.
   Present explicit consent before the API 33+ permission request; expose pause/
   resume and configurable quiet hours in Settings. Release/debug-primary builds
   show no active M2 onboarding while the pre-pilot gate is open.
8. **Add the Return screen.** Resolve only the opaque Return ID, re-check global
   and Entry policy, then decrypt locally. Show original date and exact content,
   a quiet close action, and `Não mostrar novamente`; show no selection rationale,
   other recommendation, feedback, or engagement surface. A missing, expired,
   cancelled, deleted, Never Return, paused, or undecryptable Return fails closed.
9. **Exercise adverse state and the emulator matrix.** Test process death,
   duplicate workers, clock/timezone/DST changes, delayed work, notification
   denial/revocation, permission-dialog dismissal, channel disabled, database
   and scheduler failure, policy changes, deleted/corrupt content, app lock/
   privacy cover, and API 26/API 36 notification opening.
10. **Reconcile documentation and evidence.** Update dependency inventory,
    schema ledger, privacy/data-flow map, plan progress, changelog, README,
    roadmap, and a dated M2 evidence record. Do not mark M2 complete until every
    M2 exit criterion has current evidence on both endpoint AVDs.

## Privacy and data impact

- New operational data: one Return history row per selected attempt; local
  quiet hours; observed notification-permission state; WorkManager's internal
  scheduling metadata.
- No new plaintext persistence: Entry content remains in the existing AES-GCM
  envelope. Return rows store only opaque references and content-free lifecycle
  metadata. No candidate lists or unselected reasons are persisted.
- No network, analytics, account, advertising ID, model, embedding, or telemetry
  path. WorkManager inputs/outputs/progress contain no journal-derived value.
- Notification surface contains only canonical generic text plus opaque local
  Return ID. Notification denial never changes consent or core app access.
- Deletion/Never Return/pause transactions cancel matching pending notification
  and work references. Permanent purge retains no Return-to-content access.
- All tests and screenshots use bounded synthetic entries. The primary app's
  database and Keystore namespace are outside the M2 engineering run.

## Tests and acceptance evidence

Pure tests:

- every hard/global eligibility and precedence combination;
- all six age buckets and exact boundary instants;
- imported-history and zero-history bootstrap tables, while M2 fixtures remain
  synthetic and import implementation remains absent;
- fixed clock/zone/random seed determinism;
- rolling seven-day cap, one pending Return, no V0 repeat, silence when empty;
- quiet-hour windows across midnight, timezone movement, DST gap/overlap;
- ignored/opened/expired/cancelled transitions and no reminder;
- pause/resume, Never Return, deletion, decryption failure, and stale candidate.

Repository/application tests:

- Room 1→2 migration preserves exact M1 Entries/Draft/settings and creates no
  Return or enabled consent;
- transactional single-pending/idempotency constraint and rollback failures;
- crash between Return persist and scheduler acknowledgment reconciles once;
- deleting/purging/Never Return/pause cancels correct Return references;
- schema downgrade/destructive fallback remains rejected.

Android tests on API 26 and API 36:

- unique WorkManager scheduling/replacement/cancellation and delayed execution;
- notification channel properties, exact generic payload, opaque-only intent,
  no badge, no repeated notification, and no content in dumps/manifest/logs;
- API 36 permission grant/deny/dismiss/revoke; API 26 no runtime permission;
- notification tap opens only the eligible Return after local re-check;
- disabled channel/permission leaves at most one quiet in-app pending affordance;
- validation-only activation is absent from debug-primary and release manifests/
  behavior;
- font 1.5, landscape, zero animations, 48 dp controls, privacy cover, app lock,
  cold process recreation, and offline operation.

Final checks:

- debug/validation/release assembly, lint, unit tests, Room migration tests,
  dependency verification/locking, offline rebuild, API 26/API 36 instrumented
  matrix, release manifest/permission/backup/network/log scan;
- observable synthetic scenario: enable Returns, create eligible fixture state,
  force opportunity, receive exactly `Algo seu voltou.`, open exact original
  words/date, close, set Never Return, and prove no second attempt;
- no claim of exact timing, pilot validation, broad OEM compatibility,
  accessibility sign-off, independent security review, or production readiness.

## Rollback or disable path

- Set the immutable engineering capability false and cancel the one unique Fio
  work plus pending Fio notifications. Writing, Archive, export, deletion, and
  M1 data remain usable.
- Keep Room schema 2 readable; disabling M2 does not downgrade or destructively
  rewrite the database. Return rows are inert local operational history.
- Remove the WorkManager adapter/dependency by replacing the scheduler interface
  with app-open reconciliation; pure Time engine and M1 content stay intact.
- If migration or notification privacy fails, stop M2 activation, preserve the
  existing database, and retain exact failure evidence without journal content.

## Progress log

- 2026-08-12 — Created the M2-R1 decision packet and this Draft after ADR-037
  accepted the M1 engineering baseline. Inspected all canonical product/domain
  documents, M1 persistence/application/UI code, Gradle dependencies, and
  official Android scheduling/notification guidance. No app code, dependency,
  schema, permission, Return, notification, or primary-device data changed.

## Final report

Pending owner approval of M2-R1. Implementation has not started.
