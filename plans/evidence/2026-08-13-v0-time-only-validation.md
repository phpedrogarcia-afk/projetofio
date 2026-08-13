# V0 Time-only integrated validation

Date: 2026-08-13
Device owner: project owner
Implementation support: Codex

Historical note: the APK hash and 23-test emulator counts below describe the
pre-regression snapshot. The later Devoluções privacy-cover correction, its
25-test endpoint matrix, and the corrected installed APK hash are recorded in
`2026-08-13-return-privacy-cover-regression.md`.

## Outcome

The approved M1 local core, validation-only M2 Time Returns, and validation-only
M3 local import compile and run together without Semantic Resonance. ADR-041
closes the semantic path with no V0 model selected.

## Local build evidence

- JDK: 17.0.12
- Android compile/target: API 36; minimum: API 26
- offline `testDebugUnitTest`, `lintDebug`, and `lintValidation`: passed
- `assembleDebug`, `assembleValidation`, and unsigned `assembleRelease`: passed
- M4 isolated offline contract suite: 14 tests passed
- `git diff --check`: passed

## Emulator evidence

- `Fio_API_36_Primary`: 23/23 instrumented tests passed
- `Fio_API_26_Oldest`: 23/23 instrumented tests passed
- local import validation UI and Android document picker: passed on both
- real process-death Draft recovery, font scale 1.5, landscape, and animations
  disabled: passed on both
- system credential prompt, cancel/locked fallback/PIN success: passed on API 36
- legacy API 26 system credential prompt presence: passed; shell interaction
  remains a manual platform limitation
- emulators started by the scripts were stopped and their synthetic credentials
  removed; the scripts never selected the physical POCO

## Physical POCO evidence

- Device: POCO M3 Pro 5G (`M2103K19PG`, `camellian`)
- OS: Android 13 / API 33
- ADB targeted the physical POCO explicitly; its device identifier is omitted
  from repository evidence.
- Installed package: `com.projetofio.app.validation`
- Installation mode: update in place (`-r`), preserving validation data
- Primary `com.projetofio.app` package: not replaced or cleared
- Cold launch: succeeded in 1.752 seconds
- Visible Write surface: Fio heading, quiet prompt, editor, Guardar, Arquivo,
  and Configurações
- Visible Settings validation surfaces: Time Returns and local import with
  system file picker; no personal text was inserted

MIUI's `uiautomator` printed a missing `theme_compatibility.xml` stack trace but
still returned the complete Fio hierarchy; the Activity remained foreground and
responsive. This is a Xiaomi UI-automation warning, not an app crash.

## Test artifacts

- APK SHA-256:
  `94731568B9E234FD9C72FBB4244883A02828E8859C2A2DB68BC284843ABE8115`
- Local artifact folder: `releases/v0.1.0-validation/`
- The final source ZIP size/hash are recorded in that artifact folder's
  `README.md`, outside the archive, avoiding a circular self-hash.

## Honest remaining boundaries

The owner can now perform functional validation of this isolated build. This
evidence does not close owner-observed TalkBack/visual quality, fingerprint
success/lockout/grace, Recents behavior, third-party document-provider failure,
second physical OEM coverage, independent plaintext/security review, production
signing, publication, or release authorization.
