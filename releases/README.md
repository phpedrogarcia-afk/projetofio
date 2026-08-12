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
