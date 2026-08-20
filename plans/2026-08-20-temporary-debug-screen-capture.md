# Temporary debug screen capture

Status: Implemented; revert on explicit founder request

## Outcome

Permit screenshots and screen recording during visual validation without
weakening the release privacy boundary.

## Build matrix

| Variant | Screen capture | `FLAG_SECURE` | `PrivacyCover` |
|---|---|---|---|
| `debug` | Allowed temporarily | Off | Normal, including access-gate cover |
| `validation` | Allowed temporarily | Off | Normal, including access-gate cover |
| `release` | Blocked | On | Normal |

## Safety boundary

- Use synthetic text in screenshots and recordings.
- Do not treat debug evidence as proof of release privacy behavior.
- Do not change encryption, app lock, backup, export, or notification privacy.
- Keep the default and release value of `SCREEN_CAPTURE_ALLOWED` false.

## Reversal trigger

When the founder asks to restore screenshot protection, set
`SCREEN_CAPTURE_ALLOWED` to false in `debug` and `validation`, update the
instrumented expectation, and add a decision that closes ADR-050. Release
requires no reversal because it remains protected throughout.

## Validation

- Build debug and release variants.
- Confirm generated build configuration is true only for debug/validation.
- Run unit tests and the debug instrumentation privacy-flag contract.
- On device/emulator, confirm a foreground debug screenshot contains the
  synthetic UI; never use personal journal content in captured evidence.
