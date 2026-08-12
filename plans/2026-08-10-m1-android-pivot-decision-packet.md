# Milestone 1 Android pivot — Recommendation R2

Status: Approved by project owner
Related plan: `2026-08-10-m1-trusted-local-core.md`
Prepared: 2026-08-10
Approved: 2026-08-10
Scope: Replace Apple-specific M1 decisions before any application generation

## Purpose

The project owner clarified that the intended smartphone and application
platform are Android, not iOS. `ADR-033` records that explicit platform
correction. This packet proposes the smallest native Android replacement for
the Apple-specific implementation choices that are now Superseded.

Approval of this packet would authorize documentation updates only. It would
not install Android Studio or SDK packages, generate a Gradle/Compose project,
download dependencies, initialize Git, connect a device, publish, or release.

## Current environment evidence

Inspection of the current Windows x64 host found:

| Component | Observed state |
|---|---|
| Operating system | Windows NT `10.0.26200.0`, x64 |
| Java runtime/compiler | Oracle JDK `17.0.12` |
| Android Studio | Quail 3 `2026.1.3`, stable; build `AI-261.26222.65.2613.15948027` |
| Android Studio runtime | JBR/OpenJDK `25.0.2` for the IDE |
| Android SDK platforms | `android-36`, `android-37.0` |
| Android Build Tools | `35.0.0`, `36.0.0` |
| ADB | `1.0.41`, platform-tools `37.0.1-15733141` |
| Emulator | `37.1.11.0` |
| Command-line tools | Stable `22.0`, official package `15859902`, checksum matched |
| Android virtual devices | API 26 and API 36 Fio AVDs created and boot-validated |
| Windows-connected Android device | POCO M3 Pro 5G recognized and ADB-authorized |
| Physical-device evidence | Android 13/API 33, ARM64, September 2023 patch; no identifier retained |
| Existing Android project files | `0` |

Android Studio Quail 3 supports AGP through 9.3. Current official release notes
identify platform-tools 37.0.1 as a stable July 2026 release. API 37 itself
remains preview and is not permission to compile or target M1 against it.

## Recommendation R2

### R2.1 — Native Android application boundary

Keep `ADR-033` Accepted:

- native Android is the initial client;
- Kotlin is the implementation language;
- Jetpack Compose is the UI toolkit;
- Gradle Kotlin DSL is the build configuration;
- the app remains local-first, offline-first, account-free, and server-free in
  M1;
- do not introduce Flutter, React Native, Kotlin Multiplatform, iOS targets, or
  a web wrapper in M1.

Reason: Compose is Android's native declarative UI toolkit and uses Kotlin. A
single-platform native client is the smallest path for the owner's real device.

### R2.2 — Stable build baseline, not the installed preview line

Approve this baseline policy:

- install or repair the official stable Android Studio before generation;
- use its bundled JDK when supported, while recording the exact JDK actually
  used;
- prefer the stable AGP/Gradle pair supported by that Android Studio;
- use `compileSdk = 36` and `targetSdk = 36` unless the repaired stable
  toolchain provides evidence for a newer non-preview requirement;
- do not use installed `android-37.0`, preview AGP,
  preview Compose, preview Room, or preview emulator images in the production
  M1 matrix;
- choose `minSdk` only after the intended phone's Android version is observed
  and the M1 security/accessibility APIs are checked;
- create at least a primary and oldest-supported AVD after `minSdk` is approved;
- record all exact versions and dependency resolution before generation.

Current official AGP 9.3 documentation pairs AGP 9.3.1 with Gradle 9.5.0, JDK
17, Build Tools 36.0.0, and support through API 37. The accepted M1 baseline
still uses stable `compileSdk = 36` and `targetSdk = 36`; installed API 37 and
API 37 does not enter M1 merely because it is installed. Post-approval matrix
work retained `compileSdk`/`targetSdk` 36 and validated exact stable tooling.

### R2.3 — Persistence: Room over system SQLite

Recommend accepting `ADR-034` with:

- AndroidX Room over the platform SQLite stack;
- KSP rather than KAPT for Room code generation;
- Room `2.8.4` as the current stable candidate, pinned only after the repaired
  toolchain resolves and tests it;
- exported Room schema JSON retained as migration evidence;
- `room-testing` migration fixtures from every supported schema version;
- explicit transactions for save/edit/delete/recover/purge and Draft-to-Entry;
- no `fallbackToDestructiveMigration` or equivalent destructive reset;
- Domain and Application depend only on Fio-owned repository interfaces;
- Room entities, DAOs, SQL, and migration types stay inside Persistence;
- no Paging, FTS, database observation-driven product behavior, SQLCipher, or
  additional database wrapper in M1 unless separately approved.

Reason: Android officially recommends Room over direct SQLite for non-trivial
structured data, compile-time query checking, and managed migration paths.

### R2.4 — Android record protection and no-recovery posture

Recommend accepting the protection portion of `ADR-035` with:

- store database, sidecars, settings, and temporary private files only in app-
  internal storage;
- generate a non-exportable 256-bit AES key in `AndroidKeyStore`;
- authorize that key only for `AES/GCM/NoPadding` encryption/decryption;
- encrypt each Entry and Draft private payload independently;
- let the platform cipher generate a fresh random IV for every encryption;
- authenticate envelope version, record kind, opaque record ID, and schema
  version as associated data;
- store a versioned envelope containing IV, ciphertext, authentication tag, and
  the minimum reconstruction/version fields;
- keep key lookup, seal/open, envelope parsing, and typed errors in one Fio
  Crypto service;
- do not bind the content key to biometrics in M1; optional app lock remains a
  separate authorization barrier;
- fail closed on missing key, invalid envelope, or authentication failure;
- never generate a replacement key over existing ciphertext or show a false
  empty Archive;
- exclude Fio databases/files/preferences from cloud backup and device-to-device
  transfer using both legacy and Android 12+ extraction rules;
- accept that uninstall, app-data clearing, Keystore loss, or unsupported device
  transfer makes local content unavailable; human-readable export is the only
  M1 portability path;
- do not claim hardware-backed/StrongBox storage unless device evidence proves
  it, and do not claim E2EE, zero knowledge, secure deletion, independent audit,
  or recovery.

The operational metadata boundary from the previous record format remains the
same: opaque IDs; timestamps/timezone; source, access, Return, deletion, and
version fields may remain outside record ciphertext inside the private database.

### R2.5 — Export through Android's system document picker

Preserve the accepted export format in `ADR-030`, replacing only its Apple
mechanism:

- use Android Storage Access Framework `ACTION_CREATE_DOCUMENT` through the
  activity-result API;
- create one combined UTF-8 Markdown or plain-text document;
- stream active Entries oldest-to-newest to the exact URI selected by the user;
- preserve opaque ID, original timestamp/timezone, and exact content;
- request no broad storage permission;
- avoid an intermediate plaintext file where possible;
- if a private temporary is unavoidable, keep it in internal cache and clean it
  after success, cancellation, failure, and next-launch reconciliation;
- warn that the selected document provider is outside Fio protection and may be
  local or cloud-backed;
- never upload, log, analyze, or retain a second export copy.

### R2.6 — Android privacy cover and optional app lock

Preserve the product defaults in `ADR-031`, replacing Apple APIs:

- privacy protection remains enabled by default;
- use `FLAG_SECURE` for private Fio windows in M1, accepting that this blocks
  screenshots and non-secure display/casting while active;
- also render a neutral Fio surface during background/lifecycle transitions;
- app lock remains optional and off until enabled;
- implement immediate, one-minute, and five-minute modes only after lifecycle
  tests on the supported Android matrix;
- use AndroidX `BiometricPrompt` with strong biometric or device credential
  according to capability checks;
- Fio receives an authorization result and stores no biometric material;
- reveal no Entry preview while locked or after cancellation/failure;
- require fresh authorization before export and permanent deletion when app
  lock is enabled;
- keep app lock distinct from Android Keystore record encryption.

Tradeoff: `FLAG_SECURE` provides a stronger simple M1 privacy boundary but also
prevents the user from taking screenshots of their own Fio screens. Changing
that behavior later requires an explicit privacy/UX review.

### R2.7 — Device and test matrix gate

Before any Android project generation:

1. Install/repair stable Android Studio and its command-line tools.
2. Record Android Studio build, JDK, AGP, Gradle, Kotlin, Compose BOM, KSP, Room,
   AndroidX Biometric, compile SDK, target SDK, and Build Tools versions.
3. Connect and authorize the intended phone with USB debugging, or record its
   model and Android version from Settings without device identifiers.
4. Choose `minSdk` from that phone and the intended pilot support boundary.
5. Create and boot primary plus oldest-supported stable AVDs.
6. Confirm the physical phone and both AVD configurations appear through ADB.
7. Run only empty toolchain diagnostics; do not create the Fio project until the
   completed matrix is reviewed and `ADR-034`–`ADR-036` are Accepted.

### R2.8 — Preserve operational boundaries

- The existing Android SDK, JDK, and ADB inspection does not authorize installs,
  downloads, environment-variable changes, or PATH edits.
- The ADB daemon started during inspection, found no device, and was then
  stopped; no device command ran.
- Git initialization, commit, remote, push, publication, and release remain
  separately authorized under `ADR-032`.
- The existing v0.1 ZIP remains an older specification snapshot and is not
  edited in place.

## Decisions deliberately deferred

- exact `minSdk` until the intended smartphone version is known;
- exact Android Studio/Kotlin/Compose/KSP/Biometric versions until stable Studio
  is installed and the compatibility matrix is resolved;
- signing key, application ID, Play Console, store distribution, CI, and release;
- notifications/Returns, imports, analytics, sync, backup recovery, and semantic
  model work from later milestones.

## Primary evidence consulted

- [Android Jetpack Compose setup](https://developer.android.com/develop/ui/compose/setup)
- [Android Room persistence guidance](https://developer.android.com/training/data-storage/room)
- [Room releases](https://developer.android.com/jetpack/androidx/releases/room)
- [Room migration testing](https://developer.android.com/training/data-storage/room/migrating-db-versions)
- [Android Keystore system](https://developer.android.com/privacy-and-security/keystore)
- [Android cryptography guidance](https://developer.android.com/privacy-and-security/cryptography)
- [Android app-internal storage](https://developer.android.com/training/data-storage/app-specific)
- [Android backup/device-transfer changes](https://developer.android.com/about/versions/12/behavior-changes-12)
- [Android Storage Access Framework](https://developer.android.com/training/data-storage/shared/documents-files)
- [AndroidX BiometricPrompt](https://developer.android.com/reference/androidx/biometric/BiometricPrompt)
- [Android `FLAG_SECURE`](https://developer.android.com/security/fraud-prevention/activities)
- [Android Studio installation](https://developer.android.com/studio/install)
- [AGP 9.3 compatibility](https://developer.android.com/build/releases/agp-9-3-0-release-notes)
- [Google Play target API requirement](https://developer.android.com/google/play/requirements/target-sdk)
- [SDK Platform Tools release notes](https://developer.android.com/tools/releases/platform-tools)

## Approval statement

The project owner approved Recommendation R2 without exceptions on 2026-08-10
using the following canonical statement:

> Aprovo o pacote Android M1-R2.

After approval, Codex may replace the Apple-specific canonical architecture and
plan with the accepted Android design. Tool installation and project generation
remain separate steps; generation waits for the completed Android matrix and a
re-approved executable M1 plan.

## Disposition

Recommendation R2 is incorporated as Accepted `ADR-034`–`ADR-036`. The
canonical specifications may now be rewritten for Android. The former iOS plan
remains Blocked until an Android replacement plan is complete and explicitly
reapproved.

Post-approval validation installed the verified stable command-line tools,
authorized and inspected the POCO without retaining its identifier, created and
booted API 26/API 36 AVDs, corrected platform-tools 37.0.1 to stable, and pinned
the exact dependency/test candidates in the Android M1 plan. Owner approval of
`minSdk 26`, application ID, and that plan remains pending. No application
project was generated.
