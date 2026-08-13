# M3 local import — engineering evidence

Status: Automated engineering scope complete; participant gates retained
Date: 2026-08-13
Authority: Approved M3-R1, ADR-039, and the M3 execution plan

## Claim boundary

This record supports only isolated, synthetic-data Android import engineering.
It does not support personal-file import, a participant pilot, broad provider or
OEM compatibility, manual accessibility sign-off, independent security review,
signing, publication, release, or production readiness. Debug-primary and
release have immutable local-import activation set to `false`; only the
validation variant has it set to `true`.

## Implemented slice

- one locally selected UTF-8 TXT/Markdown document through Android SAF;
- strict bounded parsing with typed failures for malformed UTF-8, container
  magic, active content, ambiguous structure/dates, and resource limits;
- versioned Fio plain/Markdown export plus conservative explicit generic blocks;
- exact SHA-256 local fingerprints over content, instant, and named zone;
- memory-only staged preview and exact deduplication across active, Recently
  Deleted, and same-batch candidates;
- additive Room schema `2 → 3`, encrypted source filename/fingerprint, atomic
  ImportBatch/Entry commit, and no plaintext staging file;
- confirmed rollback that preserves edited/native/unrelated Entries, moves only
  unchanged imported Entries to Recently Deleted, and cancels pending Returns;
- validation-only Settings preview/commit/cancel/batch UI and exact export
  round-trip with original named timezone preservation.

## Automated results

The final offline build passed unit tests, lint, debug/validation/release and
Android-test assembly.

| Evidence | Result |
|---|---|
| JVM/domain/application/contracts | 51 tests, 0 failures |
| API 36 instrumented matrix | 23 tests, 0 failures |
| API 26 instrumented matrix | 23 tests, 0 failures |
| Room migration | populated `1 → 2 → 3` and `2 → 3` fixtures preserved existing encrypted rows/settings; no import batch was invented |
| Import repository | atomic encrypted commit; unchanged rollback; edited exclusion; pending Return cancellation; no plaintext filename/fingerprint |
| Parser limits | 5 MiB file, 2,000 Entries, 256 KiB/Entry, 20,000 lines, and 5-second budget enforced |
| M3 validation profile | SAF import control and real DocumentsUI launch passed on API 26/API 36 |
| Retained profiles | adverse and credential profiles passed on both endpoints; API 26 shell credential interaction remains manual |
| Variants | validation `true`; debug-primary/release `false` |

The repeatable import profile is `app/scripts/run-m3-validation-profile.ps1`;
the complete matrix and retained adverse/credential profiles use the adjacent
scripts. They target named AVDs only. The connected POCO was not selected,
installed to, or modified during this checkpoint.

## Privacy and package audit

- M3 adds no production or test dependency, network endpoint, analytics path,
  storage permission, archive extraction, linked-resource fetch, or private
  temporary file.
- The merged release manifest contains no `INTERNET`, storage, exact-alarm, or
  foreground-service permission.
- Main-source scans found no logging/stack-trace output or app cache/files
  plaintext staging path.
- Import filename and exact fingerprint use the existing AES-GCM record
  boundary with record-specific authenticated context.
- Staging is process-memory only; cancel/read/validation failure produces no
  canonical Entry. Transaction failure is retryable with no partial commit.

## Retained gates

Before personal-file use or a participant pilot, retain manual TalkBack/visual
accessibility, complete physical-device authentication, provider
interoperability, a second physical Android manufacturer, API 26 legacy
credential interaction, and independent plaintext-boundary/security review.
Pilot consent, content-free event approval, participant rights, and real
elapsed Time-only evidence also remain absent. No gate was relabeled as passed.
