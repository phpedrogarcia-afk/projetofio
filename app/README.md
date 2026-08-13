# Fio Android — M1 development application

This directory contains the native Android implementation checkpoint for
Milestone 1 — Trusted Local Core. It is a development build, not a published or
accepted release.

## Open and run

- Open this `app/` directory in Android Studio.
- Use JDK 17 and the checked-in Gradle Wrapper.
- The application module is `mobile`.
- Application ID: `com.projetofio.app`.
- Supported baseline: Android 8.0/API 26; compile/target SDK 36.
- The local `validation` build uses `com.projetofio.app.validation`, a separate
  sandbox/key namespace, so manual fault and export checks never touch the
  primary development installation. It is not a release application ID.

Useful local validation:

```text
gradlew.bat :mobile:testDebugUnitTest :mobile:lintDebug :mobile:assembleDebug
gradlew.bat :mobile:assembleDebugAndroidTest :mobile:assembleRelease
powershell -ExecutionPolicy Bypass -File scripts\run-emulator-matrix.ps1
powershell -ExecutionPolicy Bypass -File scripts\run-emulator-adverse-profiles.ps1
powershell -ExecutionPolicy Bypass -File scripts\run-emulator-credential-profile.ps1
```

The matrix runner builds the isolated validation APK, runs JVM tests and lint,
then boots and tests the named API 36/API 26 AVDs sequentially. It sets an
explicit emulator serial, so a connected physical phone is never selected. By
default it stops only emulators that it started.

The adverse-profile runner uses a debug-only synthetic fixture receiver to
prove Draft recovery after an actual target-process force-stop. It then starts
the app with Android font scale 1.5, landscape rotation, and disabled animation
scales, checks the expected UI, restores every changed system setting, and
clears the emulator-only application data. The paired instrumented matrix reads
`FLAG_SECURE` directly from the Activity window on both API levels. The receiver
does not exist in validation or release builds.

The credential-profile runner temporarily configures the synthetic PIN `2468`
inside each disposable AVD, enables the debug-only Immediate app lock, proves
the real Android SystemUI prompt on API 26/API 36, plus cancellation, the
closed locked state, and PIN success on API 36, then clears the app data and
device credential. Android 8's legacy credential Activity does not accept
shell-injected interaction, so cancellation/success there remain manual. The
runner never targets a physical device and does not replace fingerprint,
lockout, or OEM checks.

The same build has also succeeded with Gradle offline mode. Dependency locks,
SHA-256 verification metadata, the Room schema v1 JSON, synthetic unit tests,
and synthetic instrumented tests are included. The current integrated checkpoint
passes 51 JVM unit tests and 25 instrumented tests on both API 26 and API 36,
plus the bounded physical validation recorded for the API 33 POCO. The emulator
suite now includes an exact Write-to-Archive recreation flow and verifies that
a corrupt database fails before Android's default corruption handler can
replace the file.

## Implemented M1 checkpoint

- Kotlin/Compose Write, Archive, Settings, and Recently Deleted surfaces;
- one encrypted recoverable Draft with debounced autosave;
- save plus Draft removal in one Room transaction;
- read, chronological list, exact edit, 30-day soft delete, recovery, and purge;
- combined UTF-8 Markdown/plain-text export through the Android system picker;
- Android Keystore AES-256-GCM versioned envelopes with authenticated metadata;
- backup/device-transfer exclusions, cleartext disabled, and no production
  network endpoint;
- `FLAG_SECURE`, neutral privacy cover, and optional device-owner app lock;
- Return consent fixed at `notConfigured`; M1 schedules no Return.

## Still required before M1 acceptance

Automated fault coverage now includes the pre-debounce save gap, transaction
rollback, missing-key refusal, corrupted encrypted envelopes, exact lock grace
boundaries, SQLite-full recovery, real process death, a refusing Android test
provider, direct `FLAG_SECURE`, 48 dp targets, and export cancellation/large-
document behavior. API 26/API 36 also pass the 1.5-font/landscape/animations-
off profile; the real SystemUI credential prompt appears on both, with cancel,
closed fallback, and PIN success automated on API 36.

Manual/independent evidence remains open for TalkBack and visual quality,
fingerprint success/lockout/grace/fresh authorization on the POCO, legacy API
26 credential interaction, physical Recents and third-party provider failure,
another physical OEM, and independent plaintext-boundary sign-off. Controlled
POCO evidence now passes screenshot blocking and authentication cancellation.

Git initialization, commit, push, publication, signing, and release remain
separately authorized actions.
