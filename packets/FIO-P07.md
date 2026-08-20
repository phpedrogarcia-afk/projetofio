# FIO-P07 — endurecer importação contra arquivos grandes

**Status:** READY
**Type:** code
**Context:** MEDIUM (9 files)
**Branch:** `codex/import-large-file-hardening`

## READ FIRST

- `docs/08-PRIVACY-SECURITY.md` section Import security
- `docs/06-DATA-MODEL.md` import entities
- `app/mobile/src/main/java/com/projetofio/app/domain/LocalImportParser.kt`
- `app/mobile/src/main/java/com/projetofio/app/application/ImportService.kt`
- corresponding parser/service tests

## OPTIONAL

- Android SAF reader and `SafLaunchTest` only if the boundary changes before parsing.
- M3 plan only for an unresolved accepted limit.

## DO NOT READ FOR THIS TASK

Search, Returns, visual design, semantic research and Manus reports.

## FILES EXPECTED TO CHANGE

- `LocalImportParser.kt`
- parser/service tests
- Android document reader only with measured evidence
- this packet evidence log

## TESTS TO RUN

```powershell
cd app
.\gradlew.bat :mobile:testDebugUnitTest --tests "com.projetofio.app.domain.LocalImportParserTest" --offline
.\gradlew.bat :mobile:testDebugUnitTest --offline
```

## Objective

Prove bounded memory/failure behavior at the accepted 5 MiB/2,000-entry limits
without weakening atomic preview/commit/rollback.

## Contract

Preserve I-01, I-05, I-09–I-13 and the ADR-039 limits. No format expansion,
date inference, temporary plaintext file, dependency or schema change.

## Acceptance

Adversarial large fixtures fail safely or complete within documented memory and
time bounds; canonical data remains unchanged on cancellation/failure.

## Risks and stop conditions

Stop if streaming requires a new parser architecture, schema or dependency;
write a follow-up decision/packet instead.

## Evidence log

| Date | Evidence | Output |
|---|---|---|
| | | |
