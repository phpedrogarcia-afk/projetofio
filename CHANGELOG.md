# Changelog

This file records meaningful product-specification, architecture, and release
changes. Application changes will be added when implementation begins.

## Unreleased

- Added the Draft execution plan for Milestone 1 — Trusted Local Core.
- Added recommendation packet M1-R1 for persistence, cryptography, Return
  consent, draft, export, app-lock, Apple environment, and Git boundaries.
- Recorded project-owner approval of M1-R1 and accepted ADR-022, ADR-023, and
  ADR-027–ADR-032; aligned the affected canonical specifications without
  generating application code or changing Git state.
- Recorded project-owner approval of the complete Milestone 1 — Trusted Local
  Core execution plan; the Apple toolchain/device matrix remains the sole
  pre-generation gate.
- Inspected the current Windows x64 host and documented that no Xcode, Swift,
  iOS simulator, Apple device, or validated GRDB compatibility evidence is
  available; the Apple gate remains open without guessed versions.
- Corrected the initial platform to native Android in Accepted ADR-033,
  Superseded the Apple-specific M1 decisions, blocked the former iOS execution
  plan, inspected the partial Android environment, and added Android
  Recommendation R2 with Proposed ADR-034–ADR-036. No app was generated.
- Recorded project-owner approval of Android Recommendation R2 and accepted
  ADR-034–ADR-036. Verified stable Android Studio Quail 3 `2026.1.3`; Windows
  recognizes the POCO M3 Pro 5G through MTP, while ADB authorization, Android/API
  evidence, command-line tools, AVDs, `minSdk`, and resolved dependencies remain
  open. No app was generated.
- Rewrote the canonical Android privacy/data/UX mechanisms and added the Draft
  Android M1 Trusted Local Core plan with a multi-model API-level strategy. The
  POCO is the primary physical device, not the support boundary; application
  generation remains gated.
- Authorized the POCO through ADB without retaining its device identifier;
  recorded Android 13/API 33, ARM64, September 2023 patch, file encryption,
  fingerprint/secure-lock capability, and no advertised StrongBox feature.
- Installed checksum-verified Android command-line tools 22.0, created and
  boot-validated Android 8/API 26 and Android 16/API 36 AVDs, and corrected
  platform-tools 37.0.1 from the earlier Canary classification to the current
  official stable release.
- Pinned the pre-generation Android matrix and verified primary artifacts.
  The owner then explicitly approved `minSdk 26`, application ID
  `com.projetofio.app`, and the complete Android M1 plan, closing every
  pre-generation gate. Git and publication remain separate.
- Generated the approved Kotlin/Compose Android project with application ID
  `com.projetofio.app`, `minSdk 26`, compile/target SDK 36, Gradle wrapper,
  version catalog, dependency locks, and SHA-256 verification metadata.
- Implemented the M1 Domain/Application/Crypto/Persistence boundaries, Android
  Keystore AES-256-GCM envelopes, Room schema v1, encrypted Draft/Entry flows,
  Archive/edit, Recently Deleted/recovery/purge, local UTF-8 export, privacy
  cover, `FLAG_SECURE`, and optional device-owner app lock.
- Added synthetic unit and instrumented evidence for crypto tamper/AAD/version/
  missing-key behavior, transaction failure, Android Keystore database reopen,
  complete local lifecycle, Room schema fixture, safe privacy/error surfaces,
  and API 26/API 36 launch.
- Verified debug/release compilation, lint, unit tests, dependency locks,
  checksum metadata, and an offline build. The effective release manifest has
  no Internet or storage permission and disables backup and cleartext traffic.
- Validated the automated suite on Android 8/API 26 and Android 16/API 36; the
  final POCO Android 13/API 33 build passed the non-background full local
  lifecycle, ciphertext, and Room schema subset, explicit cold launch, and the
  real window reported `SECURE`. MIUI blocks the background Activity launch
  used by the Compose runner and may require confirmation for test APKs.
- Hardened `Guardar` so the exact editor state is encrypted as the recoverable
  Draft before the Entry transaction, closing the 700 ms debounce gap. Added
  fault tests for rollback, missing/non-exportable Keystore keys, corrupted
  envelopes, lock grace boundaries, and cancelled/failed/large exports. The
  current checkpoint passes 20 unit tests, 11 instrumented tests on API 26 and
  API 36, and seven safe non-background tests on the POCO API 33; the updated
  physical build cold-launched with `FLAG_SECURE` and the test APK was removed.
- Fixed the real SAF export-launch crash reported from the POCO. AndroidX
  Biometric 1.1.0 had selected obsolete Fragment 1.2.5, which rejected the
  modern Activity Result request code before the document picker opened. The
  graph now pins Fragment 1.8.9, refreshes dependency locks and SHA-256
  metadata, and adds a real picker-launch regression test. All 11 instrumented
  tests pass on API 26 and API 36.
- Fixed a second real SAF lifecycle failure found after the picker began
  opening. The Compose-scoped result launcher was disposed when `onPause`
  replaced the interface with the privacy cover, so DocumentsUI created an
  empty destination but Fio never received the URI to write. Export launchers,
  coordination, and result messages now live at the Activity boundary and
  survive the privacy-cover transition without weakening `FLAG_SECURE`. The
  20 unit tests, lint, all build variants, and 11 instrumented tests on API 26
  and API 36 pass after the correction. The corrected POCO export produced a
  333-byte TXT containing the exact synthetic Entry, showed the success
  message, preserved the process, and added no crash record.
- Added a repeatable API 26/API 36 emulator-matrix runner with explicit AVD
  targeting, document-provider readiness checks, cold boot, and automatic
  emulator shutdown. It cannot accidentally select the physical POCO.
- Added an exact Compose Write/save/Archive/activity-recreation regression and
  improved editor feedback visibility by moving status above the editor and
  dismissing the keyboard on save.
- Prevented Android's default SQLite corruption handler from silently replacing
  an invalid existing Fio database. A read-only non-destructive preflight now
  runs before Room and preserves the exact corrupt file on failure. The current
  checkpoint passes 20 JVM tests and 13 instrumented tests on API 26/API 36.
- At that checkpoint M1 remained in progress: SAF failure/cancel, biometric/grace behavior,
  accessibility/font/IME/reduced-motion checks, process-death/low-storage cases,
  and another physical OEM remain manual acceptance gates. No Git operation,
  signing, publication, or release was performed.
- Expanded M1 automation to 17 instrumented tests on API 26/API 36: real
  `ContentResolver` provider refusal, SQLite-full Draft preservation,
  non-destructive corrupt-database preflight, direct Activity `FLAG_SECURE`,
  and 48 dp primary touch targets.
- Added adverse-profile automation for real process force-stop/cold recovery,
  font scale 1.5, landscape, and disabled animations on both endpoint AVDs.
- Added a debug-only, DUMP-protected synthetic fixture and credential runner.
  The real SystemUI prompt is present on API 26/API 36; API 36 also passes
  cancel, closed locked fallback, and PIN success. The fixture is absent from
  validation/release manifests and all synthetic credentials/data are cleared.
- Prepared the M1 plaintext-boundary trace for independent review. M1 remains
  unaccepted because physical/TalkBack/OEM and independent-review gates remain;
  M2 was not started.
- Reused only the isolated validation package on the POCO to close two physical
  gates: controlled `screencap` produced 2,204,931 bytes on the launcher and
  zero bytes with Fio foregrounded, and cancellation of the real SystemUI
  credential prompt returned to a content-free locked surface. Validation data
  was cleared afterward; the primary package was untouched.
- Accepted ADR-037 after explicit owner approval. M1 is now the engineering
  baseline for synthetic-data M2–M4 development. Its unresolved human,
  provider, second-OEM, and independent-review evidence remains a mandatory
  pre-pilot/release gate and was not relabeled as passed.

## Specification 0.1 — 2026-08-10

### Added

- Permanent repository instructions in `AGENTS.md`.
- Canonical Product, Principles, and UX definitions.
- Feature catalogue with Approved, Experimental, Planned, Research, Frozen, and
  Rejected states.
- iOS-first local/offline architecture and explicit trust boundaries.
- Versioned data model and return-policy semantics.
- Time baseline and removable Semantic Resonance experiment specification.
- Privacy/security and content-free analytics boundaries.
- Evidence-gated roadmap and 26 product/architecture decisions.
- Execution-plan template, contribution workflow, security policy, and local
  release organization.

### Preserved

- Original v0.1 Codex-ready ZIP under `releases/v0.1/` with its recorded SHA-256.

### Not implemented

- iOS application, storage, cryptography, returns, import, analytics, backend,
  sync, or embedding model.
