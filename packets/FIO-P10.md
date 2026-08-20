# FIO-P10 — ensaiar o processo de migration 3→4

**Status:** READY
**Type:** code/test-only
**Context:** MEDIUM (8 files)
**Branch:** `codex/migration-3-4-rehearsal`

## READ FIRST

- `docs/atlas/DATA-MAP.md`
- `docs/06-DATA-MODEL.md`
- `app/mobile/src/main/java/com/projetofio/app/persistence/FioDatabase.kt`
- `app/mobile/src/test/java/com/projetofio/app/persistence/Migration2To3Test.kt`
- Room schema 3 JSON

## OPTIONAL

- `RoomFioRepository.kt` and entities only to design representative fake data.

## DO NOT READ FOR THIS TASK

P19 choice implementation, UI, Search, Returns policy, crypto internals and Manus reports.

## FILES EXPECTED TO CHANGE

- test-only migration rehearsal files
- test fixtures/schemas that are explicitly non-production
- this packet evidence log

## TESTS TO RUN

```powershell
cd app
.\gradlew.bat :mobile:testDebugUnitTest --tests "com.projetofio.app.persistence.Migration2To3Test" --offline
.\gradlew.bat :mobile:testDebugUnitTest --offline
```

## Objective

Calibrate the migration workflow with a fake/test-only 3→4 path. Do not create
or ship production schema 4.

## Contract

Preserve I-01–I-07 and I-37. Production database version and exported schema
remain 3; no P19 field or product decision is implemented.

## Acceptance

The rehearsal documents byte preservation, rollback/failure behavior and exact
future checklist while the release artifact remains schema 3.

## Risks and stop conditions

Stop immediately if test code can enter the production source set or generated
schema 4 would be packaged.

## Evidence log

| Date | Evidence | Output |
|---|---|---|
| | | |
