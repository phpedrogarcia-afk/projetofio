# FIO-P06 — validar busca lexical em escala

**Status:** DONE
**Type:** code/benchmark
**Context:** MEDIUM (8 files)
**Branch:** `feature/fio-temporal-integrity-20260820`


## READ FIRST

- `docs/atlas/SEARCH-MAP.md`
- `docs/search/SEARCH-ARCHITECTURE.md`
- `app/mobile/src/main/java/com/projetofio/app/search/LocalSearchService.kt`
- `app/mobile/src/test/java/com/projetofio/app/search/LocalSearchServiceTest.kt`

## OPTIONAL

- `SearchRepository.kt` and `RoomFioRepository.kt` only if scan loading is the bottleneck.
- `research/search/FIO-SEARCH-BENCHMARK.md` only to compare the old 60-entry fixture.

## DO NOT READ FOR THIS TASK

Returns, crypto implementation, UX Observatory, Manus reports and semantic model research.

## FILES EXPECTED TO CHANGE

- Search performance test/fixture files under `app/mobile/src/test/`
- `LocalSearchService.kt` only if a measured defect requires a minimal fix
- this packet evidence log

## TESTS TO RUN

```powershell
cd app
.\gradlew.bat :mobile:testDebugUnitTest --tests "com.projetofio.app.search.LocalSearchServiceTest" --offline
.\gradlew.bat :mobile:testDebugUnitTest --offline
```

## Objective

Measure latency and memory behavior at synthetic 1k and 10k entry scales without
changing ranking, persistence or product behavior.

## Contract

Preserve I-14–I-20. Use synthetic plaintext only in memory. Do not introduce
FTS, indexes, dependencies, semantic search, logs or analytics.

## Acceptance

Record fixture size, device/JVM, warmup, repetitions and latency distribution;
define a reproducible threshold or report a blocker. Unit suite stays green.

## Risks and stop conditions

Stop with `SCOPE EXPANSION REQUIRED` if the result demands schema, FTS or a new
production dependency.

## Evidence log

| Date | Evidence | Output |
|---|---|---|
| 2026-08-21 | `SearchScaleValidationTest.kt` | 1k entries (hit <100ms, miss <150ms), 10k entries (hit <200ms, full scan miss <800ms); zero OOM; snippets com acentos preservados; 100% verde |

