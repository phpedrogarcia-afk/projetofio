# Releases

This directory stores generated local distribution artifacts. Canonical source
always lives elsewhere in the repository.

Rules:

- one folder per specification/application version;
- every artifact has a short README and SHA-256 digest;
- artifacts are immutable after recording their digest;
- generate a new version instead of editing a ZIP in place;
- ZIP files are ignored by Git by default to avoid repository bloat;
- never include credentials, real journal content, local databases, private
  fixtures, or build-signing material;
- release packaging does not authorize publication or deployment.

Current local artifacts:

- `v0.1/` — immutable original specification snapshot from 2026-08-10;
- `v0.1.0-validation/` — pre-regression 2026-08-13 local validation snapshot;
  retained for provenance, not the current owner-test build. The later privacy-
  cover correction is recorded in
  `plans/evidence/2026-08-13-return-privacy-cover-regression.md`.
