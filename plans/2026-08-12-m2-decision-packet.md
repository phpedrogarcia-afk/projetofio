# M2-R1 — Android Time Returns decision packet

Status: Accepted — approved by project owner on 2026-08-13
Date: 2026-08-12
Related milestone: Milestone 2 — Time Returns
Recorded as: ADR-038

## Decision requested

Approve the following package as one bounded M2 implementation decision:

1. Add AndroidX WorkManager `2.11.2` as the only new M2 production dependency,
   using `work-runtime-ktx`; add matching `work-testing` only to tests. Use one
   uniquely named, replaceable, one-time work request for the next local Return
   opportunity. Do not use exact alarms, periodic alarms, a foreground service,
   network constraints, remote push, or an always-running process.
2. Create one local Android notification channel named `Devoluções`, with low
   importance, no badge, and canonical visible text `Algo seu voltou.`. Its
   intent carries only an opaque local Return ID. Ask for
   `POST_NOTIFICATIONS` only after explicit Return consent on API 33+; denial or
   dismissal causes no repeat prompt, warning badge, or impairment to writing,
   Archive, export, or deletion.
3. Use default quiet hours from 21:00 until 08:00 local time. The engine chooses
   a deterministic-testable random instant only inside the allowed 08:00–21:00
   window. M2 Settings lets the person change the quiet-hour boundaries; clock,
   timezone, daylight-saving, and delayed-work reconciliation cannot create a
   duplicate notification or a burst.
4. Migrate Room schema `1 → 2` non-destructively. Add the minimum M2 operational
   Entry fields, Return history table, and quiet-hour/notification-observation
   settings described in `docs/06-DATA-MODEL.md`. Existing installations remain
   `returnConsentState = notConfigured`; migration creates no Return and
   schedules no work.
5. Keep M2 engineering unavailable to the primary/release app while ADR-037
   pre-pilot gates remain open. Production code may be compiled in all variants,
   but onboarding, scheduling, and notifications are enabled only in the
   isolated `validation` environment and deterministic tests. No real journal
   content, analytics, pilot feedback, or network path enters M2 engineering.

## Why this package

WorkManager is Android's recommended API for persistent deferrable work and
survives process/app restarts without promising an exact delivery instant. The
stable AndroidX release recorded on 2026-08-12 is `2.11.2`; it supports the
project's API 26 minimum and compile SDK 36. Unique one-time work aligns with
Fio's sparse, recalculated opportunities more closely than a periodic job.

Official references:

- [WorkManager releases](https://developer.android.com/jetpack/androidx/releases/work)
- [Persistent work guidance](https://developer.android.com/develop/background-work/background-tasks/persistent)
- [Notification runtime permission](https://developer.android.com/develop/ui/compose/notifications/notification-permission)

The low-importance channel makes the Return an invitation rather than an
interruption. The allowed 08:00–21:00 window provides a conservative starting
default while retaining explicit user control. Validation-only activation
preserves ADR-037: implementation can advance without silently involving the
owner's primary journal data.

## Privacy and dependency impact

- WorkManager stores scheduling metadata in its app-internal database. Fio will
  pass only a constant unique-work name and opaque Return ID; no Entry ID, text,
  date, algorithm rationale, or score enters WorkManager input/progress/output.
- NotificationManager receives only the generic copy and opaque Return ID.
- No Internet permission, endpoint, analytics SDK, crash SDK, exact-alarm
  permission, foreground-service permission, advertising identifier, or remote
  service is added.
- AndroidX WorkManager uses the AndroidX Apache 2.0 license and remains behind a
  Fio-owned `ReturnOpportunityScheduler` interface. Removal cancels the unique
  work and leaves Time evaluation callable on app-open reconciliation.
- Version resolution, transitive graph, hashes, license inventory, offline
  build, manifest, backup rules, and removal path must be recorded before the
  dependency is accepted as implemented.

## Alternatives not selected

- **Exact `AlarmManager`:** rejects Fio's opportunistic timing, adds exact-alarm
  policy/permission complexity, and may imply a precision promise the product
  does not make.
- **Periodic WorkManager job:** encourages fixed cadence and complicates sparse
  randomized opportunities; one-time unique work is easier to reconcile.
- **Evaluate only when the app opens:** cannot deliver the approved local
  notification while the app remains closed.
- **Custom background service:** unnecessary lifecycle, power, and privacy
  surface.
- **Immediate M2 enablement in the primary app:** conflicts with the retained
  ADR-037 pre-pilot gate.

## Approval record

The project owner stated on 2026-08-13:

> Aprovo o pacote Android M2-R1 — Time Returns.

This approval authorizes ADR-038, the M2 execution plan, dependency resolution, Room
migration, validation-only implementation, and proportional automated tests. It
does not authorize a participant pilot, use with non-synthetic content, Git,
signing, publication, release, analytics transport, import, or semantic work.
