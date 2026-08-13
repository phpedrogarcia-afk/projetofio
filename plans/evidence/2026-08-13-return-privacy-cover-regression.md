# Return privacy-cover regression — validation evidence

Date: 2026-08-13

Scope: local validation build only; no participant, production, publication, or
security-audit claim

## Reported symptom

After entering the Devoluções flow, the app could remain on a static dark/black
surface with no usable content.

The API 36 emulator reproduced the underlying state after the Android
notification-permission dialog. `MainActivity` remained resumed and healthy,
but the accessibility hierarchy exposed only the centered neutral `Fio`
privacy-cover label. Controlled system screenshots were entirely black, as
expected while `FLAG_SECURE` protects the Fio window.

## Root cause

`MainActivity.onPause()` correctly enabled the privacy cover before a system
surface could obscure Fio. The cover was cleared only in `onStart()`. Android
permission and other transient system surfaces can execute `onPause()` followed
by `onResume()` without an intervening `onStop()`/`onStart()`, leaving the cover
stuck even though the encrypted database, Entry, and Return attempt were intact.

## Correction

- Track whether the current foreground access gate has resolved.
- Keep the cover enabled while settings/app-lock state is unresolved.
- On transient `onResume()`, remove the cover only when the gate is already
  resolved and no lock, authentication, or safe-open failure is active.
- If a vendor/test lifecycle resumes after a stopped gate job without the normal
  start sequence, resolve the gate again instead of exposing content or leaving
  the cover indefinitely.
- Cancel the foreground gate job and invalidate its result on `onStop()`.

This preserves the privacy invariant: no Entry content is exposed before the
current foreground lock decision completes.

## Manual emulator reproduction after the correction

Environment: `Fio_API_36_Primary`, Android 16/API 36, isolated
`com.projetofio.app.validation` data.

1. Imported one synthetic TXT Entry dated 2025-01-10 through the real Android
   document picker and Fio preview/commit flow.
2. Enabled Devoluções and accepted the Android notification permission.
3. Confirmed that the app returned from the system dialog instead of remaining
   on the privacy cover.
4. Opened `Abrir devolução disponível`.
5. Confirmed the Return surface exposed the original date, exact synthetic
   words, `Fechar`, and `Não mostrar novamente`.

No real journal text was used. The emulator application data was isolated from
the owner's POCO and primary package.

## Automated verification

- Offline JVM tests: passed.
- Debug lint: passed.
- Validation APK and debug instrumentation APK assembly: passed.
- New regression:
  `ScaffoldLaunchTest.transientPauseDoesNotLeavePrivacyCoverStuck` passed.
- API 36 full instrumentation: 25/25 passed.
- API 26 full instrumentation: 25/25 passed.

The new regression explicitly moves the Activity through a transient paused
state and requires the Write prompt to become visible again after resume. A
second regression stops the Activity, cancels the former foreground gate, then
requires a fresh gate resolution before content returns.

## POCO update

The corrected validation APK was installed with `adb install -r` on the
ADB-authorized POCO M3 Pro 5G, preserving the existing validation-app data. The
primary `com.projetofio.app` package was neither replaced nor cleared.

- APK size: `13,582,460` bytes
- APK SHA-256:
  `D1DBD311EA10656BB2AA8D523A8EB16AD24387910870B1DEEF51F14C2BAF619F`
- Cold launch: successful; Android displayed Fio's device-owner authentication
  prompt. Automation did not operate or bypass that prompt.

## Remaining human boundary

The owner must authenticate normally and observe the corrected Devolução flow on
the physical POCO. Passing emulators and a successful cold launch do not close
the retained manual accessibility, physical authentication, second-OEM,
provider, or independent-review gates.
