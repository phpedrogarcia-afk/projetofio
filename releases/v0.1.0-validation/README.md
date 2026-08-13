# Fio v0.1.0 — pre-regression local validation snapshot

Prepared: 2026-08-13
Purpose: owner testing on Android; not a published or production release

## Supersession notice

These exact APK/source artifacts predate the later Devoluções privacy-cover
correction. They are retained locally for provenance and must not be described
as the current owner-test build. The corrected APK installed on the POCO has a
different hash, recorded in
`plans/evidence/2026-08-13-return-privacy-cover-regression.md`. The repository
working tree, not this ZIP, is the source of the post-regression checkpoint.

## Installable APK

`Fio-v0.1.0-validation.apk`

- Application ID: `com.projetofio.app.validation`
- Version: `0.1.0-dev-validation`
- Minimum Android: 8.0 / API 26
- Target Android: API 36
- Size: 13,582,460 bytes
- SHA-256:
  `94731568B9E234FD9C72FBB4244883A02828E8859C2A2DB68BC284843ABE8115`

This package is debug-signed for local validation. It uses a separate Android
application/data namespace and does not replace `com.projetofio.app`.

## Codex-ready source archive

`Fio-v0.1.0-Codex-ready.zip`

- Size: 497,742 bytes
- SHA-256:
  `AB6ACDA44F01881123963A0F779887369925F51E73AADBA54B2BD3AA4E989538`
- Root inside archive: `Fio-v0.1.0-validation/`

The source archive excludes local databases, journal exports, private fixtures,
build output, model downloads, credentials, signing material, and Git metadata.

## Verification at the time of this snapshot

- offline JVM tests and lint passed;
- debug, validation, and unsigned release APK assembly passed;
- 23/23 instrumented tests passed on API 36;
- 23/23 instrumented tests passed on API 26;
- import picker profile passed on API 36 and API 26;
- process-death/accessibility adverse profiles passed on API 36 and API 26;
- credential profile passed on API 36; API 26 system prompt presence passed with
  interaction retained as a manual legacy-platform gate;
- this exact validation APK was installed and cold-launched on the owner's POCO
  M3 Pro 5G, Android 13/API 33;
- POCO UI inspection confirmed the Time Returns and local import validation
  surfaces without inserting journal content.

Semantic Resonance is not included. ADR-041 selects no V0 semantic model; Time
Returns remains the complete return mechanism.

This superseded snapshot does not claim publication readiness, independent
security review, second-OEM coverage, production signing, or completed manual
accessibility and biometric observation.
