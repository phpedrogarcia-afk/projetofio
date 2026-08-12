# M1 emulator automation evidence — API 26 and API 36

Status: Automated scope complete; engineering baseline accepted by ADR-037;
pre-pilot external gates remain open
Date: 2026-08-11 (America/Sao_Paulo)
Content boundary: synthetic fixtures only

This record belongs to
`plans/2026-08-10-m1-android-trusted-local-core.md`. ADR-037 later accepted the
combined M1 evidence as an engineering baseline and authorized synthetic-data
M2–M4 work. This record still does not replace another physical OEM, constitute
an independent security review, or authorize a pilot, release, or security
claim.

## Repeatable commands

From `app/`:

```text
powershell -ExecutionPolicy Bypass -File scripts\run-emulator-matrix.ps1
powershell -ExecutionPolicy Bypass -File scripts\run-emulator-adverse-profiles.ps1 -SkipBuild
powershell -ExecutionPolicy Bypass -File scripts\run-emulator-credential-profile.ps1 -SkipBuild
```

All runners resolve AVD names to emulator serials and pass an explicit serial
to ADB/Gradle. A connected physical phone is never selected. Each runner stops
only AVDs it started, unless `-KeepEmulators` is supplied.

## Automated result

- build, isolated validation APK, and Android lint: passed; lint has no errors;
- JVM suite: 20 tests, 0 failures;
- instrumented suite: 17 tests, 0 failures on Android 16/API 36 and Android
  8.0/API 26;
- adverse profile: passed on both AVDs after a real target-process force-stop,
  cold start, 1.5 font scale, landscape rotation, and all animation scales at
  zero; the synthetic Draft remained visible and no new AndroidRuntime crash
  was detected;
- credential profile: real SystemUI credential prompt appeared on both AVDs;
  API 36 additionally passed cancellation, closed locked fallback, and PIN
  success. The Android 8 legacy credential Activity does not accept shell-
  injected interaction, so its cancel/success behavior remains manual;
- every temporary system setting, application data set, and synthetic PIN was
  cleared in runner cleanup.
- final unsigned release assembly and release lint-vital checks passed; a
  main-source scan found no Android logging/printing call, and the effective
  release manifest contained only biometric/fingerprint and Android's
  generated non-exported dynamic-receiver permission.

## Deterministic fault and privacy coverage

- exact Write/save/Archive path survives Activity recreation;
- process death recovers the encrypted Draft on both supported endpoints;
- a forced SQLite `FULL` failure preserves the last recoverable Draft and
  commits no Entry;
- Entry insert plus Draft deletion rolls back as one transaction;
- corrupt encrypted envelope and missing Keystore key fail closed;
- a corrupt existing database fails read-only preflight and retains its exact
  SHA-256 instead of invoking Android's destructive corruption recovery;
- a test-only Android `ContentProvider` refuses `openFile`; the production
  `ContentResolver` writer reports failed export without false success;
- cancellation and large UTF-8 export behavior remain covered at the
  application boundary; the real POCO SAF success evidence is recorded in the
  separate physical report;
- `FLAG_SECURE` is read directly from the real Activity window on API 26/API
  36;
- primary navigation and Guardar meet the 48 dp minimum touch-target contract;
- debug fixture receiver requires `android.permission.DUMP`, accepts only
  bounded synthetic actions, and is absent from validation and release
  manifests.

The first touch-target run found Guardar at 40 dp and stopped the matrix. The
button was corrected to a real 48 dp minimum; the complete 17-test matrix then
passed on both AVDs. The failed pre-fix run is not counted as final evidence.

## Residual M1 gates

Automation does not close:

1. owner-observed TalkBack order/announcements and visual clipping/contrast;
2. physical POCO fingerprint/authentication success and lockout, grace modes,
   fresh authorization, and OEM Recents behavior; physical cancellation and
   screenshot blocking now pass in the separate POCO evidence;
3. Android 8 legacy credential cancellation/success outside shell injection;
4. interoperability failure from a real third-party document provider and
   cleanup of any remaining physical synthetic export;
5. a second physical Android manufacturer;
6. an independent review/sign-off of the plaintext boundary.

The first four are manual/physical observations. Items 5 and 6 require an
external device/person and cannot be manufactured by the emulator or by the
implementing agent.
