# Milestone 1 — Android Trusted Local Core

Status: Approved
Implementation state: Engineering baseline accepted — pre-pilot validation gates open
Owner: Project owner with Codex implementation support
Related milestone: Milestone 1 — Trusted Local Core
Relevant decisions: ADR-003, ADR-004, ADR-006, ADR-008, ADR-014,
ADR-027, ADR-029–ADR-037
Approved: 2026-08-10

## Outcome

A person can use Fio on a supported Android phone to write plain text, close
the app, reopen it offline, find exactly what they wrote, edit it, delete or
recover it, and export a readable copy. Entry and Draft private payloads cross
the persistence boundary only through the accepted Android Keystore/AES-GCM
envelope. The app exposes no journal content through networking, analytics,
logs, notifications, Recents, or backup.

The first physical validation device is the project owner's POCO M3 Pro 5G.
Compatibility is defined by an approved Android API range and tested device
matrix, not by that phone model or Xiaomi-specific behavior.

## Pre-generation gate

No Gradle project, package name, source set, dependency lock, Room schema, or
application file may be generated until every item below is complete and this
plan changes to `Approved` by an explicit owner statement.

| Gate item | Current evidence | Required closure |
|---|---|---|
| Android Studio | Quail 3 `2026.1.3`, build `AI-261.26222.65.2613.15948027` | Closed |
| IDE runtime | Bundled JBR/OpenJDK `25.0.2` | Record only; not the build-language target |
| Build JDK | Oracle JDK/Javac `17.0.12` validated | Closed; Gradle toolchain is JDK 17 |
| Android Gradle Plugin | Stable `9.3.1`; artifact and Studio compatibility verified | Closed |
| Gradle | `9.5.0`; official AGP default and distribution verified | Closed |
| Kotlin | AGP built-in KGP `2.2.10`; Compose compiler plugin `2.2.10` | Closed; do not add legacy Kotlin Android plugin |
| Annotation processing | KSP2 `2.3.11`; KAPT forbidden | Closed; artifact verified; full build proof occurs in scaffold step 1 |
| Compose | BOM `2026.06.01` (`ui 1.11.4`) | Closed; remains compatible with `compileSdk 36`; Compose 1.12/API 37 excluded |
| Room | `2.8.4` runtime/compiler/testing | Closed; artifacts verified |
| Coroutines | `kotlinx-coroutines-android 1.11.0` | Closed; artifact metadata verified |
| Biometric | Stable AndroidX Biometric `1.1.0` | Closed; physical capability recorded; behavior tests remain M1 work |
| Test baseline | AndroidX Test core/runner/rules `1.7.0`, ext JUnit `1.3.0`, Espresso `3.7.0`, JUnit `4.13.2`, Compose/Room tests at their pinned versions | Closed for generation; exact transitive graph follows scaffold sync |
| Compile/target SDK | `compileSdk 36`, `targetSdk 36` | Closed; stable Android 16 baseline and current Play requirement |
| Build Tools | `36.0.0` installed | Closed; official AGP 9.3 default |
| Platform tools | Stable ADB/platform-tools `37.0.1-15733141` | Closed; official July 2026 stable release |
| Command-line tools | Stable `22.0` installed from official package `15859902`; SHA-256 matched | Closed; Android CLI metrics disabled during use |
| Physical device | POCO M3 Pro 5G: Android 13/API 33, September 2023 patch, ARM64 plus 32-bit compatibility, 1080×2400/440 dpi; ADB authorized | Closed without storing serial or IMEI |
| Physical security capabilities | Fingerprint and secure lock screen present; file-based encryption; no StrongBox feature | Closed; make no StrongBox/hardware-backed claim |
| Minimum Android | Android 8.0/API 26 | Closed; explicitly approved by owner |
| Primary AVD | `Fio_API_36_Primary`: Android 16/API 36, x86_64, 1080×2400/420 dpi | Closed; clean boot and ADB verified |
| Oldest-supported AVD | `Fio_API_26_Oldest`: Android 8.0/API 26, x86_64, 1080×1920/420 dpi | Closed; canonical oldest-supported AVD |
| Device discovery | POCO and each AVD independently reached `device`/completed boot through ADB | Closed |
| Dependency evidence | Exact primary artifacts returned HTTP 200 from Google Maven/Maven Central; AGP/Gradle compatibility is official | Closed for generation; resolved transitive graph/licenses are mandatory scaffold evidence |
| Application ID | `com.projetofio.app` | Closed; explicitly approved; check store uniqueness before publication |
| Plan authorization | Approved by project owner on 2026-08-10 | Closed; generation and M1 implementation authorized |

Technical preparation and owner authorization are complete. SDK installation
authorization was used only for the command-line tools, API 26 platform, two
stable images, and two Fio AVDs; nothing was installed on or changed in the
POCO. Application generation and M1 implementation may now follow the ordered
sequence below. Git, publication, and release remain separately authorized.

## Non-goals

- notifications, Return selection, Return UI, Rest, Remember Later, or Sealed;
- embeddings, semantic search, hosted AI, or any journal network request;
- import, attachments, rich text, tags, titles, mood, or templates;
- account, backend, sync, cloud backup, reinstall recovery, or key recovery;
- analytics, crash-reporting vendor, ads, tracking, monetization, or research;
- widgets, share extensions, shortcuts, broad storage access, or contacts/media/
  location permissions;
- iOS, web, Flutter, React Native, or Kotlin Multiplatform clients;
- Play Console, signing/release keys, store publication, CI, Git initialization,
  commit, remote, push, or release ZIP generation.

## Context read

- `AGENTS.md`
- `docs/01-PRODUCT.md`
- `docs/02-PRINCIPLES.md`
- `docs/03-UX.md`
- `docs/04-FEATURES.md`
- `docs/05-ARCHITECTURE.md`
- `docs/06-DATA-MODEL.md`
- `docs/08-PRIVACY-SECURITY.md`
- `docs/10-ROADMAP.md`
- `docs/DECISIONS.md`
- `.agent/PLANS.md`
- `plans/2026-08-10-m1-decision-packet.md`
- `plans/2026-08-10-m1-android-pivot-decision-packet.md`
- Superseded `plans/2026-08-10-m1-trusted-local-core.md`

## Assumptions and open questions

### Verified facts

- The execution host is Windows x64.
- Android Studio Quail 3 `2026.1.3` stable is installed.
- Android platforms 36 and 37.0 and Build Tools 35.0.0/36.0.0 are present.
- Stable command-line tools 22.0, API 26 platform, and the two required Google
  APIs x86_64 system images/AVDs are installed and boot-validated.
- ADB authorizes the POCO M3 Pro 5G. It runs Android 13/API 33 with an ARM64
  primary ABI and a September 2023 security patch; no serial/IMEI is retained.
- The POCO exposes fingerprint and secure-lock-screen capabilities and uses
  file-based encryption; it does not advertise StrongBox Keystore.
- The repository contains specifications and an application placeholder only.
- ADR-034–ADR-036 and Android Recommendation R2 are Accepted.

### Assumptions to validate during scaffold step 1

- The pinned artifacts resolve together under the real Gradle build without a
  forced upgrade, version conflict, preview dependency, or unexpected license.
- Compose compiler `2.2.10` and KSP2 `2.3.11` operate correctly with AGP
  built-in KGP `2.2.10`; artifact and release evidence exists, but only a real
  KSP/Compose compile can prove the combination end to end.
- Lifecycle/app-lock behavior remains consistent across API 26, API 33/POCO,
  and API 36 after the application exists.

### Owner approvals resolved

On 2026-08-10 the project owner explicitly approved `minSdk 26`, application ID
`com.projetofio.app`, and this complete Android M1 plan for generation and
implementation. Store uniqueness, Git, publication, and release remain separate
future gates.

## Compatibility strategy

Support is defined by API and capability, not brand/model allowlists.

- **Physical primary:** POCO M3 Pro 5G, Android 13/API 33, ARM64, MIUI 14,
  1080×2400/440 dpi; technical identifiers are not retained.
- **Primary AVD:** `Fio_API_36_Primary`, Android 16/API 36, x86_64,
  1080×2400/420 dpi; PT-BR and English checks after the app exists.
- **Oldest AVD:** `Fio_API_26_Oldest`, Android 8.0/API 26, x86_64,
  1080×1920/420 dpi; canonical only after owner approval of `minSdk 26`.
- **Additional evidence:** test another physical Android/OEM before pilot when
  available; absence does not permit claiming universal Android compatibility.
- **Responsive UI:** Compose layouts must survive narrow phones, common aspect
  ratios, display-size changes, keyboard/IME changes, landscape where allowed,
  and large font scaling without content loss.
- **Capability fallbacks:** app lock explains unavailable/unenrolled device
  authentication without blocking core writing; export uses the system picker;
  no OEM-specific APIs become core dependencies.

The supported matrix will name API levels and capabilities. Marketing must not
say “all Android phones”; it may state the tested minimum Android version and
known requirements after validation.

## Implementation sequence

Each step begins only after the pre-generation gate and explicit plan approval.
Paths below describe intended module ownership; exact generated paths are
recorded when the scaffold exists.

### 1. Generate the smallest reproducible Android project

- Create one Android application under `app/` using Kotlin, Jetpack Compose,
  Gradle Kotlin DSL, version catalogs, and the approved application ID.
- Pin the approved Gradle wrapper, AGP, SDK values, Java/Kotlin toolchain,
  Compose BOM, KSP, Room, and Biometric versions.
- Add debug/unit/instrumented test source sets and lint; add no network,
  analytics, crash, navigation, DI, image, or serialization dependency unless
  a later plan proves it necessary.
- Configure deterministic build settings and dependency verification/locking
  to the supported degree, then record licenses and transitive dependencies.
- Create backup/data-extraction exclusion resources before storing any content.

Evidence: clean build, unit-test task, lint task, dependency report, and empty
instrumentation launch succeed on both AVDs and the authorized POCO.

### 2. Establish Fio-owned boundaries

- Create Domain models and repository interfaces independent of Android/Room.
- Create Application use cases for draft load/save/clear, Entry save/read/list/
  edit, delete/recover/purge, settings, authorization, and export.
- Keep Compose in UI, Room in Persistence, and Android Keystore/JCA in Crypto.
- Use explicit construction or a very small composition root; do not add a DI
  framework without a separate accepted decision.
- Inject clock and ID generation for deterministic tests.

Evidence: dependency-direction tests/review show Domain and Application do not
import Compose, Room, Android Keystore, or Android framework types.

### 3. Implement the cryptographic envelope and failure model

- Generate one non-exportable AES-256 key in `AndroidKeyStore`, authorized only
  for `AES/GCM/NoPadding`.
- Seal Entry and Draft payloads independently using a new platform-generated IV
  and authenticated associated data: envelope version, record kind, opaque ID,
  and schema version.
- Centralize versioned envelope parse/seal/open and typed failures.
- Never log plaintext, key material, IV/ciphertext dumps, or user identifiers.
- Missing key, malformed envelope, and failed authentication stop safely and do
  not create a replacement key, erase data, or present an empty Archive.
- Verify actual key properties on the device but make no StrongBox/hardware
  claim unless evidence supports it.

Evidence: round-trip, unique-IV, wrong-AAD, tamper, truncation, missing-key,
unsupported-version, and process-restart tests pass with synthetic content.

### 4. Implement Room schema version 1 and repositories

- Map only the M1 subset of Entry, Draft, AppSettings, and deletion state from
  `06-DATA-MODEL.md`.
- Store private payloads only as authenticated ciphertext envelopes; keep only
  the Accepted operational metadata outside ciphertext.
- Export Room schema JSON and add migration-test infrastructure from version 1.
- Use explicit transactions for Entry save plus Draft removal, edits, deletion,
  recovery, purge, and settings changes.
- Never enable destructive migration or silently reset on open/migration error.
- Ensure database, WAL/SHM sidecars, preferences, and private cache remain in
  app-internal storage and are excluded from backup/device transfer.

Evidence: DAO/query tests, transaction rollback, reopen, corruption handling,
schema export, backup-rule inspection, and first migration fixture pass.

### 5. Build Write and recoverable Draft

- Implement the canonical prompt and immediately usable plain-text editor.
- Persist at most one nonblank encrypted Draft after a quiet debounce.
- `Guardar` commits Entry and removes Draft in one transaction; show
  `Guardado.` only after success.
- Preserve the last recoverable Draft on crypto, database, storage, lifecycle,
  or forced-save failure.
- Work fully offline and request no permissions.

Evidence: Compose UI/unit/instrumented tests cover typing, rotation/configuration
change, process recreation, relaunch, blank draft, debounce, save, double tap,
low storage/forced failure, and no false success.

### 6. Build Archive and edit

- Show active Entries chronologically with original date and exact decrypted
  content only after successful open.
- Provide readable empty/error states that do not turn key or corruption
  failures into an empty Archive.
- Edit exact plain text and update ciphertext transactionally while preserving
  original creation information.
- Keep Archive secondary but discoverable; no feed, streak, topics, summaries,
  scores, or search in M1.

Evidence: exact Unicode/PT-BR round trips, oldest/newest ordering, relaunch,
edit failure rollback, large-entry behavior, and accessibility traversal pass.

### 7. Build Recently Deleted, recovery, and permanent purge

- Default delete creates a tombstone with a 30-day recovery boundary.
- Recovery restores exact content and policy metadata.
- Permanent delete requires a clear confirmation and fresh device-owner
  authorization if app lock is enabled.
- Purge removes Entry envelope, Draft/references/derived artifacts that exist,
  and any internal plaintext temporary; never claim physical secure erasure.

Evidence: delete/recover/purge, time-boundary, idempotency, transaction failure,
relaunch, and authorization cancellation tests pass.

### 8. Build local export through the Storage Access Framework

- Offer one combined UTF-8 Markdown or plain-text document.
- Stream active Entries oldest-to-newest to the URI chosen with
  `ACTION_CREATE_DOCUMENT`; request no broad storage permission.
- Include version/header, export time, opaque Entry ID, original time/timezone,
  and exact content with unambiguous delimiters; infer nothing.
- Explain that the selected provider is outside Fio protection.
- Avoid temporary plaintext; if unavoidable, use internal cache and clean it
  after success, cancellation, failure, timeout, and next launch.
- Require fresh authorization first when app lock is enabled.

Evidence: byte/Unicode round trip, empty and large archives, local/cloud-capable
picker cancellation, provider failure, authorization failure, and temporary-
file reconciliation pass without retaining a second copy.

### 9. Build privacy cover and optional app lock

- Keep privacy cover enabled by default and app lock off by default.
- Apply `FLAG_SECURE` to private windows and render a neutral Compose cover
  during background/Recents transitions.
- Use AndroidX `BiometricPrompt` and capability-aware device-owner
  authentication; store no biometric material and do not bind the content key
  to biometrics.
- Implement immediate, one-minute, and five-minute grace modes only after tests
  succeed across the approved API/device matrix.
- Reveal no content on locked, cancelled, failed, background, or recreated
  surfaces; require fresh auth for export/permanent deletion.

Evidence: Recents/screenshot, background/foreground, process death, clock/grace
boundary, biometric enrolled/unenrolled/unavailable, device credential,
cancellation, lockout, and OEM physical-device tests pass.

### 10. Harden, audit, and document M1

- Run unit, instrumentation, Compose UI, Room migration, lint, dependency,
  secret, release-manifest, permission, backup-rule, and offline checks.
- Manually test TalkBack order, font scaling, reduced/disabled animations,
  contrast, touch targets, keyboard/IME, narrow layout, and PT-BR copy.
- Inspect release configuration for network endpoints, debug logging, exported
  components, backup behavior, permissions, and content-bearing snapshots.
- Trace plaintext from editor/decryption to re-encryption/export and document
  every temporary boundary.
- Update canonical docs, this progress log, and final report using current
  evidence. Do not create a release, commit, push, or publish without separate
  approval.

## Privacy and data impact

M1 creates local encrypted Entry and Draft data plus minimum operational
metadata and settings. There is no account, server, analytics, crash vendor,
notification, or production network endpoint. The manifest requests no private
data permission and cleartext traffic is disabled. Dependencies must be
inventoried for hidden initialization or network behavior.

Plaintext exists transiently in application memory while editing/reading and at
the user-selected export destination. Fio-owned persistent storage contains
authenticated ciphertext for Entry/Draft private payloads. Database files,
sidecars, preferences, private cache, and key-related state are excluded from
backup and device transfer. Uninstall, clear-data, reset, or Keystore loss has
no M1 recovery; the UI must state this limitation and never repair by erasure.

Logs use closed technical error codes and synthetic test IDs only. Screenshots,
test fixtures, and automated UI content use synthetic journals. Exported
plaintext is outside Fio protection after the user chooses a provider.

## Tests and acceptance evidence

M1 is accepted only when current evidence covers all of the following on the
POCO and both approved AVD configurations where applicable:

- clean/debug build, release compilation, lint, unit tests, instrumented tests,
  Compose UI tests, and Room migration tests;
- save/read/edit/delete/recover/export after relaunch and in airplane mode;
- Draft recovery and no false `Guardado.` under forced crypto/database/storage
  failures;
- exact PT-BR/Unicode/whitespace round trips and stable ordering/timestamps;
- cryptographic tamper/AAD/version/missing-key failures that preserve data;
- no destructive database fallback and tested transaction rollback;
- no journal content in logs, manifest metadata, Recents, screenshot, backup,
  notification, analytics, network request, or temporary-file residue;
- SAF export success/cancel/failure and explicit external-provider warning;
- app lock/cover lifecycle cases and fresh authorization for protected actions;
- TalkBack, Android font scaling, reduced animations, keyboard/IME, contrast,
  touch targets, narrow device, and supported orientation behavior;
- cold start, process recreation, low storage, corrupted envelope, database-open
  failure, and unavailable device authentication;
- dependency/license inventory and independent review of the plaintext/
  ciphertext boundary.

Observable user acceptance:

1. Write `Hoje choveu — e eu fiquei em paz.`, save offline, force-close, and
   reopen; the exact text and date remain.
2. Interrupt typing before save, force-close, and reopen; the recoverable Draft
   returns without a false saved Entry.
3. Delete, recover, delete again, and permanently purge; each state is explicit
   and no Return/search artifact remains.
4. Export Markdown and plain text; each opens outside Fio with exact content and
   an understandable warning that it is now outside Fio protection.
5. Background or capture the app while content is visible; Recents and
   screenshots expose no journal content.
6. Repeat core scenarios on the POCO, primary AVD, and oldest-supported AVD.

## Rollback or disable path

- Before any stored-data release, the generated scaffold can be removed only
  through an explicitly reviewed file change; documentation remains canonical.
- Once schema version 1 stores data, never roll back through destructive
  migration or database deletion. Stop opening the affected version, preserve
  files, and ship a forward migration/fix after review.
- App lock can be disabled by the user after successful device-owner
  authorization; privacy cover/`FLAG_SECURE` changes require a new privacy
  decision and are not remote flags.
- Optional M1 UI slices may be withheld from navigation while incomplete, but
  encryption, backup exclusions, deletion, and migration safeguards cannot be
  disabled to make tests pass.
- Export is the only M1 portability path; it is not an automated rollback or
  backup mechanism.

## Progress log

- 2026-08-10 — M1-R1 and the former iOS plan were approved before the owner
  corrected the target platform to Android; Apple implementation decisions and
  plan were later Superseded/Blocked without generating an app.
- 2026-08-10 — Project owner approved Android Recommendation M1-R2; ADR-034–
  ADR-036 became Accepted.
- 2026-08-10 — Verified Android Studio Quail 3 `2026.1.3`, JBR `25.0.2`, JDK
  `17.0.12`, SDK platforms 36/37.0, Build Tools 35/36, emulator 37.1.11, and ADB
  37.0.1. Command-line tools, images, AVDs, and resolved dependency evidence are
  absent.
- 2026-08-10 — Windows recognized the owner's POCO M3 Pro 5G through MTP. ADB
  reported no authorized device, so Android/API/security patch/ABI evidence and
  `minSdk` remain open. No serial, IMEI, application, or user data was recorded.
- 2026-08-10 — Created this Android replacement plan as Draft. No Android
  project or application source was generated.
- 2026-08-10 — Owner authorized the Android preparation required to close the
  matrix. Installed official command-line tools 22.0 after matching SHA-256,
  with Android CLI metrics disabled; installed API 26 and stable Google APIs
  x86_64 images for API 26/36; created and boot-validated both Fio AVDs.
- 2026-08-10 — ADB validated the POCO on Android 13/API 33, ARM64, September
  2023 patch, file-based encryption, fingerprint/secure-lock capability, and no
  StrongBox feature. No persistent device identifier or personal data entered
  the project.
- 2026-08-10 — Pinned the pre-generation matrix to JDK 17.0.12, AGP 9.3.1,
  Gradle 9.5.0, built-in KGP/Compose compiler 2.2.10, KSP2 2.3.11, Compose BOM
  2026.06.01, Room 2.8.4, coroutines 1.11.0, Biometric 1.1.0, AndroidX Test
  1.7.0/ext 1.3.0/Espresso 3.7.0, Build Tools 36, `compileSdk`/`targetSdk` 36,
  and stable platform-tools 37.0.1. Primary artifacts and official
  compatibility were verified without generating the app.
- 2026-08-10 — Project owner explicitly approved `minSdk 26`, application ID
  `com.projetofio.app`, and this Android M1 plan. All pre-generation gates are
  closed; implementation is authorized without inferring Git or publication.
- 2026-08-10 — Generated the Kotlin/Compose/Room/KSP project under `app/` only
  after approval. Initial build exposed Lifecycle 2.11/API 37 incompatibility;
  pinned Lifecycle 2.10 to preserve the approved stable `compileSdk 36` and
  fixed the API-27 navigation-bar resource without raising `minSdk 26`.
- 2026-08-11 — Implemented Fio-owned Domain/Application/Crypto/Persistence/UI
  boundaries; schema v1 covers Entry, singleton Draft, and AppSettings. Added
  Android Keystore AES-256-GCM envelopes, transactional save/draft removal,
  edit, 30-day Recently Deleted, recovery/purge, SAF export, privacy cover,
  `FLAG_SECURE`, and optional device-owner app lock. M1 still schedules no
  Return and has no production endpoint, analytics, account, or sync.
- 2026-08-11 — Generated dependency locks, SHA-256 verification metadata, and
  Room schema JSON. Debug/release builds, 13 unit tests, lint, and an entirely
  offline build passed. The effective release manifest has no Internet or
  storage permission, `allowBackup=false`, cleartext disabled, normal AndroidX
  biometric/fingerprint permissions, and the AndroidX signature-level dynamic
  receiver permission.
- 2026-08-11 — Full six-test instrumented suite passed on Android 8/API 26 and
  Android 16/API 36. It covers Android Keystore database reopen and the full
  synthetic Draft/save/read/edit/export/delete/recover/purge lifecycle,
  ciphertext persistence, Room schema v1 fixture, privacy/error surfaces, and
  cold Activity launch. The final POCO Android 13/API 33 build passed the
  non-background full local lifecycle, ciphertext persistence, and Room schema
  v1 tests, plus an explicit cold launch; the real window reported `SECURE`.
  MIUI blocks the Compose runner's background Activity launch. No device
  identifier or personal journal content was retained.
- 2026-08-11 — Adverse-condition hardening closed the pre-debounce Draft gap:
  `Guardar` now persists the exact encrypted editor state before attempting the
  Entry transaction, while failure copy makes no unverified recovery claim.
  Added deterministic coverage for transaction rollback, missing Keystore key
  without replacement, non-exportable first-use key, corrupted envelope without
  row deletion, exact app-lock grace boundaries, and cancelled/failed/large
  export paths. The checkpoint now passes 20 unit tests and 10 instrumented
  tests on both API 26 and API 36. Seven safe non-background tests pass on the
  physical POCO API 33; the updated app cold-launched with `FLAG_SECURE`, and the
  temporary test package was removed.
- 2026-08-11 — Began the physical manual-validation session recorded in
  `plans/evidence/2026-08-11-m1-poco-manual-validation.md`. POCO process death,
  cold reopen, and `FLAG_SECURE` passed. The app also started without crashing
  under font scale 1.30, disabled system animations, and landscape; all system
  values were restored. A separate synthetic-data validation build compiled,
  but MIUI requires owner approval before installation. The owner has no second
  Android manufacturer available, so that compatibility gate remains an
  explicit external gap rather than inferred evidence.
- 2026-08-11 — The owner approved installation of the isolated validation build.
  It cold-launched with `FLAG_SECURE`, recovered an exact synthetic Draft after
  force-stop, saved it, and retained it in Archive after relaunch. Coordinate-
  driven external UI automation was stopped when MIUI foregrounded WhatsApp;
  no text was entered there and remaining SAF/biometric/accessibility checks
  require direct owner interaction.
- 2026-08-11 — Owner-driven real SAF Markdown cancellation returned to Fio
  silently with no false success or error. Successful destination writes and
  provider-failure behavior remain pending.
- 2026-08-11 — Owner-driven real SAF Markdown export through the `Arquivos`
  provider completed. The owner opened the resulting document outside Fio and
  confirmed the exact synthetic Entry content. Plain-text/provider-failure and
  export cleanup checks remain pending.
- 2026-08-11 — The first owner-driven plain-text attempt crashed before the SAF
  picker opened. Crash evidence identifies AndroidX Fragment 1.2.5, selected
  transitively by Biometric 1.1.0, rejecting the Activity Result API request
  code. No export document was created and no encrypted content was changed.
  Corrective work pins Fragment 1.8.9 and adds a real picker-launch regression
  test; this remains pending validation on API 26, API 36, and the POCO.
- 2026-08-11 — The corrected dependency graph now resolves and locks AndroidX
  Fragment 1.8.9; SHA-256 verification metadata was refreshed. Debug,
  validation, and release assembly, 20 unit tests, and lint pass offline. A
  real system-picker launch regression test raises the instrumented suite to
  11/11 passing on both API 26 and API 36. POCO confirmation remains pending.
- 2026-08-11 — A corrected validation-APK reinstall was prepared but did not
  start: after the emulator session ADB reported no connected physical device,
  and reconnecting ADB did not rediscover the POCO. No package or device data
  changed. Physical confirmation remains pending phone reconnection/unlock.
- 2026-08-11 — After reconnection, the Fragment-corrected validation build was
  installed. On the POCO, **Texto** opened the real DocumentsUI picker without
  a process restart or new crash. Saving created a 0-byte `fio-export.txt`,
  exposing a second defect: `onPause` replaced the Compose tree with the privacy
  cover and disposed the result launcher before the destination URI returned.
  The encrypted synthetic Entry remained visible and decryptable in Archive.
- 2026-08-11 — Export launchers, coordination, and result state moved to the
  Activity boundary so the privacy-cover transition cannot unregister the SAF
  result. `FLAG_SECURE` and the opaque cover remain. Debug/validation/release,
  20 unit tests, lint, and 11/11 instrumented tests on API 26 and API 36 pass;
  the second physical TXT write/read is pending.
- 2026-08-11 — The Activity-scoped correction passed the second POCO TXT
  write/read. The Downloads document was 333 bytes and contained the exact
  synthetic Entry; Fio showed success, retained the same process, and added no
  crash after the 21:32 install. Historical crash records end at 15:47. The
  zero-byte failed artifact and the later verified 333-byte synthetic TXT were
  each removed after exact path/size/content verification.
- 2026-08-11 — Added a repeatable emulator-matrix runner that explicitly binds
  Gradle to one AVD serial, waits for DocumentsUI, and never selects a connected
  physical phone. An exact Write/save/Archive/activity-recreation test exposed
  save-feedback positioning on API 36; feedback now remains above the editor
  and the keyboard is dismissed on save. A corruption test also exposed
  Android's default destructive SQLite recovery. Fio now performs a read-only
  `quick_check` with a non-destructive error handler before Room opens an
  existing database. The invalid file and SHA-256 remain unchanged on failure.
  Build/lint and 20 unit tests pass; 13/13 instrumented tests pass on both API
  26 and API 36. The extended automated checkpoint now passes 17/17 on both.

## Implementation checkpoint report — 2026-08-11

This checkpoint became the accepted M1 engineering baseline under ADR-037 on
2026-08-12. It is not final field, accessibility, compatibility, security-review,
release, or pilot acceptance.

- **Generated boundary:** one Gradle project at `app/`, one application module
  `mobile`, Kotlin/Compose UI, Room/KSP persistence, Android Keystore/JCA crypto,
  synthetic unit/instrumented tests, Room schema `1.json`, lockfile, and
  verification metadata.
- **Approved matrix retained:** application ID `com.projetofio.app`, `minSdk 26`,
  compile/target SDK 36, JDK 17, AGP 9.3.1, Gradle 9.5.0, Kotlin/Compose compiler
  2.2.10, KSP2 2.3.11, Compose BOM 2026.06.01, Room 2.8.4, Lifecycle 2.10.0,
  coroutines 1.11.0, Biometric 1.1.0, and the Fragment 1.8.9 compatibility pin.
  Serialization BOM 1.8.1 aligns an existing transitive SavedState runtime with
  Room's schema serializers; Fio exposes no serialization API.
- **Crypto/data:** non-exportable Android Keystore AES-256 key, AES-GCM fresh IV,
  versioned binary envelope, stable length-prefixed AAD, ciphertext-only private
  Room payloads, no replacement key over existing records, no destructive
  migration fallback, transactional save plus Draft removal, and schema v1
  fixture validation.
- **Privacy:** backup/device-transfer excluded, cleartext disabled, no Internet
  or storage permission, no production network endpoint/logging/analytics,
  generic safe failures instead of an empty Archive, `FLAG_SECURE`, neutral
  cover, and optional platform authentication. Export plaintext is streamed
  only to the user-selected SAF URI and is outside Fio protection.
- **Automated evidence:** debug/validation/release assembly, lint, 20 unit tests,
  offline build, and 17 instrumented tests pass. The full suite passes on API 26 and
  API 36. Seven safe non-background tests pass on the physical POCO API 33,
  covering Android Keystore lifecycle/missing-key behavior, transaction
  rollback, corrupted-envelope preservation, ciphertext persistence, and Room
  schema v1, plus an explicit cold launch with `FLAG_SECURE`. MIUI still blocks
  the runner's background Activity launch.
- **Compatibility statement:** current evidence supports Android 8.0/API 26
  through Android 16/API 36 at the tested API/capability level. It does not
  support a claim of every Android model. Only one physical OEM has been tested.
- **Automated hardening complete:** a refusing Android provider, SQLite-full
  save, process death, font 1.5, rotation, zero animations, direct
  `FLAG_SECURE`, 48 dp targets, and the real SystemUI credential prompt are now
  covered. API 36 also proves credential cancel, closed fallback, and success.
- **Open acceptance gates:** owner-observed TalkBack/visual quality; physical
  fingerprint/authentication success, lockout, grace/fresh authorization,
  Recents, and third-party provider failure; legacy API 26 credential
  interaction; a second
  physical Android manufacturer; and independent plaintext-boundary sign-off.
- **POCO follow-up:** physical SystemUI cancellation fails closed with no
  content, and controlled `screencap` comparison proves `FLAG_SECURE` blocks
  capture on the recorded MIUI build. Only the validation package was cleared.
- **Physical-device availability:** the owner currently has only the approved
  POCO. Another OEM cannot be tested in this session and remains a documented
  limitation; it is not replaced by emulator evidence.
- **Repository/release state:** no Git initialization, commit, remote, push,
  signing, publication, release ZIP update, or M2 work was performed or inferred.
- **2026-08-12 gate disposition:** the owner explicitly approved ADR-037. The
  deterministic M1 baseline now permits M2–M4 engineering with synthetic data.
  Every open acceptance item above is retained as a mandatory pre-pilot gate;
  none is converted into passing evidence.
