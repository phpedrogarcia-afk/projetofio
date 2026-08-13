# Fio visual reference assets

This directory preserves visual evidence and review candidates. Images help
communicate atmosphere and hierarchy; they do not override Accepted decisions,
`03-UX.md`, privacy rules, accessibility requirements, or roadmap gates.

## Asset register

| Asset | Provenance | Status | Use |
|---|---|---|---|
| `fio-visual-reference-v1-recovered-2026-08-13.png` | Founder-supplied screenshot recovered from the earlier Fio design conversation on 2026-08-13 | Historical reference | Preserve the founding warm editorial, sage/off-white/charcoal, restrained botanical, and personal-object direction |
| `fio-visual-reference-v2-android-candidate-2026-08-13.png` | Generated on 2026-08-13 from the recovered image as a style/mood reference, with the active Android product rules supplied as constraints | Candidate pending founder review | Review the Android-first visual direction; do not treat it as shipped UI or milestone authorization |

The exact reproducibility record for the v2 candidate is
`fio-visual-reference-v2-prompt.md`.

## Review record

The recovered v1 image remains valuable, but it contains four known conflicts
with the current specification:

1. iPhone hardware and iOS visual language instead of Android;
2. the pilot resonance question inside the Return screen instead of a separate,
   skippable research sheet after the Return closes;
3. a notification subtitle beyond the canonical `Algo seu voltou.`;
4. a prominent Archive floating action button that makes the Archive feel more
   like a feed or productivity surface.

The v2 candidate keeps five quiet states: Write, Saved, Return, Archive, and
Settings. It uses neutral Android devices, removes feedback from the Return,
removes the Archive floating action button, and uses only the generic Return
notification. Text and proportions in the image are illustrative; production
copy and behavior must be implemented from the canonical specifications.

## Generation record for v2

- Mode: built-in image generation with the recovered local image supplied as a
  visual reference.
- Exact prompt: `fio-visual-reference-v2-prompt.md`.
- Intended use: Android UI mockup / visual direction board.
- Input role: composition atmosphere, palette, botanical restraint, paper,
  linen, and natural light only.
- Required corrections: Android hardware, standalone Return, chronological
  Archive, Settings, exact generic notification, legible non-cursive body copy,
  no lock/envelope/confetti/gamification/AI-chat/social-feed treatment.
- Output: 1659 × 948 PNG.
- SHA-256:
  `73B3CE4C1B1078E4B6D0E4583BCF7207DF6A5B5C518171120223BC4E61990FA7`.

Do not overwrite either asset. Future revisions receive a new versioned file
and a new row in this register.
