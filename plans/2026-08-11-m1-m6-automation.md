# M1 to M6 — evidence-driven Android automation

Status: Complete — maximum authorized automation reached
Owner: Project owner with Codex implementation support
Related milestone: Milestones 1–6
Relevant decisions: ADR-003, ADR-006–ADR-008, ADR-010–ADR-012,
ADR-015–ADR-018, ADR-020–ADR-021, ADR-024, ADR-027, ADR-032–ADR-041

## Outcome

Advance Fio from the current Android M1 checkpoint through the M6 semantic
decision with the maximum safe automation available on the recorded API 26 and
API 36 emulators and the owner's POCO. Every milestone remains evidence-gated:
automation may implement and test behavior, but it cannot manufacture physical
OEM coverage, participant consent, 60–90 day pilot observations, interviews, or
an independent review.

## Non-goals

- Git initialization, commit, remote, push, publication, signing, or release.
- Treating emulator behavior as evidence from a second physical manufacturer.
- Using real journal content in fixtures, screenshots, benchmarks, or logs.
- Enabling analytics transport, hosted models, server-side plaintext, or
  Semantic Resonance before its approved study and model gates.
- Completing M6 from synthetic outcomes presented as participant evidence.

## Context read

- `AGENTS.md`
- `docs/01-PRODUCT.md` through `docs/10-ROADMAP.md`
- `docs/DECISIONS.md`
- `.agent/PLANS.md`
- `plans/2026-08-10-m1-android-trusted-local-core.md`
- `plans/evidence/2026-08-11-m1-poco-manual-validation.md`
- Android Gradle configuration, main application boundaries, and all JVM and
  instrumented tests present on 2026-08-11.

## Assumptions and open questions

Verified:

- API 26 and API 36 AVDs are installed and boot successfully.
- The Android 13/API 33 POCO is currently ADB-authorized.
- On 2026-08-11, the current build, lint, 20 JVM tests, and all 17 instrumented
  tests passed on API 26 and API 36.
- ADR-037 accepts the M1 engineering baseline for synthetic-data M2–M4 work;
  the M3 pilot remains blocked by the retained pre-pilot gates. M5 requires
  real M3 pilot evidence, M4, and an approved study plan.

Open gates that automation cannot close honestly:

- A second physical Android manufacturer.
- Independent plaintext-boundary/security review.
- Owner-observed TalkBack quality, OEM Recents behavior, and real device-owner
  authentication outcomes where system prompts cannot be asserted in-process.
- Consented Time-only and Resonance pilot recruitment, elapsed study time,
  optional feedback, interviews, and participant-rights operations.
- M4 model choice and M5 study activation require the decisions and evidence
  named by ADR-024 and the canonical roadmap.

## Implementation sequence

1. **M1 automated closure** — keep one repeatable command for JVM tests, lint,
   isolated validation APK build, and the API 26/API 36 instrumented matrix.
   Add bounded UI, lifecycle, accessibility, storage, database, export, and
   security fault tests where Android permits deterministic automation.
2. **M1 residual physical checklist** — run only synthetic fixtures in the
   isolated validation application; record manual/OEM gates separately and do
   not downgrade them to automated proof.
3. **M1 staged acceptance report** — reconcile code, test totals, threat
   boundaries, limitations, and reviewer trace. ADR-037 accepts the engineering
   baseline without waiving or falsifying the retained pre-pilot gates.
4. **M2 Time Returns plan and implementation** — pure injected-clock engine,
   Room migration, WorkManager/notification boundary if separately approved,
   explicit consent onboarding, generic notification, pause, Never Return,
   idempotency, timezone/background reconciliation, and content-free tests.
5. **M3 bounded import and Time-only pilot tooling** — TXT/Markdown staging,
   preview, limits, deduplication, atomic commit/rollback, provenance, and local
   content-free study records. Human pilot outcomes remain external evidence.
6. **M4 semantic benchmark harness** — create the approved PT-BR benchmark
   format, scoring, held-out protocol, adapter boundary, privacy/performance
   measurements, and license report. Do not connect a model to production
   Returns or select one without the deferred decision.
7. **M5 controlled Resonance pilot build** — only after an approved study plan
   and M4 decision: removable local flag, encrypted index, age-matched Time
   control, fixed assignment, kill switch, Time fallback, closed event schemas,
   and tested removal path. Human 60–90 day results cannot be simulated.
8. **M6 decision package** — compute the canonical metrics and uncertainty from
   consented pilot data, report missingness/rejection/discomfort/device cost,
   and prepare exactly one Remove, Revise, or Keep-investigating decision for
   explicit owner acceptance.

## Privacy and data impact

The automation itself uses only synthetic content and local build/test output.
The emulator runner selects emulators explicitly and never targets the physical
phone. It creates no network endpoint and sends no analytics. Future M2–M5
stored entities, migrations, notifications, imports, benchmark assets, model
dependencies, and study events require milestone-specific plans and privacy
reviews before implementation.

## Tests and acceptance evidence

- `:mobile:testDebugUnitTest`
- `:mobile:lintDebug`
- `:mobile:assembleValidation`
- `:mobile:connectedDebugAndroidTest` on `Fio_API_26_Oldest`
- `:mobile:connectedDebugAndroidTest` on `Fio_API_36_Primary`
- Milestone-specific migration, deterministic engine, notification, import,
  analytics-contract, benchmark, kill-switch, and removal tests as introduced.
- Physical/manual evidence stays labeled by device, API, observer, and date.
- Research exit evidence reports real elapsed windows and participant data;
  synthetic rehearsals are labeled rehearsal only.

## Rollback or disable path

- The matrix runner changes no canonical user data and stops only emulators it
  started unless `-KeepEmulators` is supplied.
- M2 Time remains independent of semantic code.
- M3 imports retain batch identity and transactional rollback.
- M4 adapters do not connect to production Returns.
- M5 remains behind a local removable flag with a Time fallback, kill switch,
  encrypted-derived-data purge, and tested code/data removal path.

## Progress log

- 2026-08-11 — Created the continuous M1–M6 automation plan after the owner
  requested emulator-led progress. Revalidated build/lint, 20 JVM tests, and
  11/11 instrumented tests on both API 36 and API 26. Added
  `app/scripts/run-emulator-matrix.ps1` so the supported emulator matrix can be
  repeated without targeting the connected POCO.
- 2026-08-11 — Hardened the matrix runner with explicit emulator serials, cold
  boots, DocumentsUI readiness checks, and automatic cleanup. Recreated the
  synthetic API 26 AVD after detecting its invalid temporary `/data` mount;
  the repaired AVD mounts 10 GB and exposes the system document provider.
- 2026-08-11 — Added an exact UI Write/save/Archive/activity-recreation test,
  moved save/error feedback above the editor, and hid the keyboard on save.
  Added a non-destructive database preflight after proving that Android's
  default SQLite corruption handler could recreate an invalid database. The
  corrupt file now fails closed and retains its exact SHA-256. Build/lint, 20
  JVM tests, and 13/13 instrumented tests pass on API 26 and API 36.
- 2026-08-11 — Added production `ContentResolver` export writing with a
  refusing test provider, deterministic SQLite-full preservation, direct
  Activity `FLAG_SECURE`, and 48 dp touch-target tests. The suite now passes
  20 JVM and 17 instrumented tests on both supported endpoint AVDs.
- 2026-08-11 — Added repeatable adverse and credential profiles. Both AVDs
  recover a synthetic encrypted Draft after real process force-stop and cold
  launch under 1.5 font, landscape, and zero animations. The real credential
  prompt appears on both; API 36 also proves cancellation, closed fallback,
  and PIN success. The legacy API 26 credential Activity rejects shell
  interaction and remains explicitly manual.
- 2026-08-11 — Prepared a complete plaintext-boundary trace for independent
  review. Automated M1 scope is complete; TalkBack/visual/OEM/physical auth,
  a second physical manufacturer, and independent sign-off still block M1
  acceptance and therefore M2.
- 2026-08-11 — The accessibility contract caught Guardar at 40 dp. Its
  effective height was corrected to 48 dp, after which build/lint, 20 JVM
  tests, 17 instrumented tests on both AVDs, both adverse profiles, both
  credential profiles, unsigned release assembly, manifest isolation, and the
  no-main-source-logging scan passed. Automated M1 work is exhausted at the
  documented human/external acceptance gate.
- 2026-08-11 — Used only the isolated validation package on the reconnected
  POCO. A launcher capture control produced 2,204,931 bytes while the Fio
  foreground capture produced 0 bytes; no personal image was copied. The real
  SystemUI authentication prompt was cancelled and the locked Fio surface
  exposed zero unexpected text. Validation data was cleared and the primary
  package remained untouched. Physical screenshot blocking and cancellation
  are now closed; success/lockout/grace/Recents still require the owner/device.
- 2026-08-12 — The owner approved ADR-037. M1 is accepted as an engineering
  baseline for synthetic-data M2–M4 work. Human accessibility/authentication,
  provider interoperability, API 26 manual credential interaction, second-OEM,
  and independent plaintext review remain mandatory before any participant
  pilot, release, broad compatibility claim, or security/audit claim.
- 2026-08-12 — Inspected the M1 code and official Android background/
  notification guidance, then prepared `plans/2026-08-12-m2-decision-packet.md`
  and `plans/2026-08-12-m2-time-returns.md`. M2-R1 proposes stable WorkManager
  2.11.2, generic low-importance local notifications, 21:00–08:00 quiet hours,
  Room schema 2, and validation-only activation. No M2 dependency, schema,
  permission, or application code changed pending explicit owner approval.
- 2026-08-13 — The owner approved M2-R1/ADR-038. The validation-only Time
  Returns implementation and its API 26/API 36 verification are complete; see
  `plans/evidence/2026-08-13-m2-time-returns.md`. M1's retained pre-pilot gates
  remain unchanged. The next bounded engineering slice is the M3 import plan;
  Git, participant use, signing, publication, and release remain separate.
- 2026-08-13 — The owner approved M3-R1/ADR-039. The bounded import engineering
  checkpoint is complete with 51 JVM tests and 23 instrumented tests on each
  API 26/API 36 endpoint; see
  `plans/evidence/2026-08-13-m3-local-import.md`. Participant use remains
  blocked. Proposed M4-R1 and its Draft plan are the next human decision gate;
  no model or semantic dependency has been added.
- 2026-08-13 — The owner approved M4-R1/ADR-040. The isolated synthetic research
  foundation now has a hash-frozen 300-pair PT-BR corpus, family-separated
  240/60 split, two blind annotation packets, current candidate inventory, and
  11 passing offline tests. The lexical control is explicitly a failed control,
  not selection evidence. M4-R2 owner approval and two independent human
  ratings are the next real gates; no model or Android semantic dependency was
  added.
- 2026-08-13 — The owner approved M4-R2. A hash-locked MiniLM development-only
  rehearsal ran twice offline with stable aggregate values but unsafe top-1
  `1.0` and about 521 MB added Windows working set, so it was not advanced to
  Android or held-out. Two offline annotation pages and 13 passing M4 tests
  exhaust safe automation at the two-human-rating gate. ADR-024 remains
  Deferred and M5/M6 remain unavailable.
- 2026-08-13 — The sole project owner requested an automated path and reserved
  functional testing for the finished application. ADR-041 avoids false human
  evidence and uses the legitimate early-stop branch: no model selected, M5
  deferred, and M6 `Remove / do not introduce`. The frozen human packets remain
  available only if semantic selection is reopened later.
- 2026-08-13 — Revalidated the integrated Time-only V0 offline: 51 JVM tests,
  lint for debug/validation, debug/validation/unsigned-release assembly, 23/23
  instrumented tests on API 36 and API 26, both import-picker profiles, both
  process-death/font/rotation profiles, and the credential profiles passed.
  Updated only `com.projetofio.app.validation` on the API 33 POCO, cold-launched
  it, and confirmed Write, Time Returns, and local import surfaces without
  inserting personal content or touching `com.projetofio.app`. Created the
  immutable local owner-test APK/source package with SHA-256 evidence.
- 2026-08-13 — Reproduced and corrected the reported Devoluções static black
  screen. A transient notification-permission surface left the privacy cover
  active because Android resumed without restarting the Activity. The
  foreground access gate now covers transient resume and stopped-job recovery
  without exposing content early. The synthetic import → consent → permission
  → pending Return → original words flow passed on API 36; the expanded full
  suite passes 25/25 instrumented tests on both API 26 and API 36. See
  `plans/evidence/2026-08-13-return-privacy-cover-regression.md`.

## Final report

Automated M1 scope, bounded M2/M3 engineering, and the M4 synthetic research
foundation plus the bounded MiniLM development rehearsal are complete. ADR-041
records the safe M6 early-stop outcome without participant claims: Semantic
Resonance is not introduced in V0 and Time remains core. Participant pilots,
production readiness, publication, and retained device/accessibility/review
gates remain outside this automated result. The isolated validation APK is now
installed on the owner's POCO and ready for functional observation.
