# M2 Time Returns — engineering evidence

Status: Automated engineering scope complete; pre-pilot gates retained
Date: 2026-08-13
Authority: Approved M2-R1, ADR-038, and the M2 execution plan

## Claim boundary

This record supports only the isolated, synthetic-data Android M2 engineering
checkpoint. It does not support a participant pilot, real journal data, broad
OEM compatibility, manual accessibility sign-off, independent security review,
signing, publication, release, or production readiness. Debug-primary and
release have immutable Time Returns activation set to `false`; only the
validation variant has it set to `true`.

## Implemented slice

- pure Time engine with injected clock, zone, and bounded random source;
- explicit consent, bootstrap thresholds, six age buckets, one pending Return,
  rolling seven-day cap, no V0 repeat, quiet hours, DST-safe local time, expiry,
  and content-free reason codes;
- additive Room schema `1 → 2`, exported schema 2, one-Entry Return lifecycle,
  and privacy-preserving migration defaults;
- transactional reconciliation, pause/resume, quiet-hour change, deletion,
  Never Return, policy recheck, and notification/work cancellation;
- WorkManager 2.11.2 unique one-time work with empty WorkData/progress/output,
  no network constraint, exact alarm, foreground work, server, or push;
- low-importance, no-badge `Devoluções` channel and exact visible text
  `Algo seu voltou.` with an opaque local Return ID;
- validation-only onboarding/settings/Return UI; debug-primary and release stay
  inactive.

## Automated results

The final local build command passed unit tests, lint, and debug, validation,
release, and Android-test assembly in offline dependency mode.

| Evidence | Result |
|---|---|
| JVM/domain/application/contracts | 37 tests, 0 failures |
| API 36 instrumented matrix | 21 tests, 0 failures |
| API 26 instrumented matrix | 21 tests, 0 failures |
| API 36 notification permission | grant observed; revoke observed externally; Android process-killing revocation accepted |
| Room migration | populated schema 1 fixture preserved exactly in schema 2; consent remains not configured; no Return created |
| Adverse profiles, both endpoints | process force-stop/cold recovery, font 1.5, landscape, zero animations passed |
| Credential profile | API 36 cancellation/closed fallback/PIN passed; API 26 prompt present and legacy interaction remains manual |
| Variants | validation `true`; debug-primary/release `false` |

The repeatable matrix is `app/scripts/run-emulator-matrix.ps1`; adverse and
credential regressions are the adjacent scripts. The runner targets named AVDs
only and did not install on or modify the connected POCO during this checkpoint.

## Privacy and package audit

- WorkManager runtime/testing artifacts resolve exactly to `2.11.2`, are pinned
  in `mobile/gradle.lockfile`, and are authenticated by SHA-256 verification
  metadata.
- The merged release manifest contains no `INTERNET`, storage, exact-alarm, or
  foreground-service permission. WorkManager's optional foreground permission
  and service are explicitly removed because Fio never uses long-running work.
- `WAKE_LOCK`, `ACCESS_NETWORK_STATE`, and `RECEIVE_BOOT_COMPLETED` are
  WorkManager scheduler surfaces; Fio defines no network constraint and lacks
  `INTERNET`.
- Main-source scans found no `Log`, `println`, stack-trace printing, content-
  bearing WorkData/output, or notification preview text.
- Entry content remains in the existing AES-GCM record envelope. Return rows,
  work metadata, and notification intents carry only opaque/content-free local
  operational values.

## Retained gates

Before any participant pilot or release claim, retain the ADR-037 physical and
external gates: manual TalkBack/visual accessibility, complete physical-device
authentication, notification prompt dismissal and disabled-channel behavior,
provider interoperability, a second physical Android manufacturer, API 26
legacy credential interaction, and independent plaintext-boundary/security
review. No gate was relabeled as passed by this automation.
