# Milestone 3 — Android local import engineering

Status: Engineering complete — participant pilot and personal-file use remain blocked
Owner: Project owner with Codex implementation support
Related milestone: Milestone 3 — Import and Time-only pilot
Relevant decisions: ADR-002, ADR-004, ADR-015, ADR-022, ADR-033–ADR-039

## Outcome

In the isolated validation environment, a person can select one bounded UTF-8
TXT/Markdown history, preview every proposed Entry/date/error/duplicate, commit
the valid set atomically, see imported Entries in Archive and Time eligibility,
cancel without changes, and later roll back only unchanged Entries created by
that batch. Original words and explicit dates survive export round-trip.

## Non-goals

- Participant pilot, personal files, real journal content, analytics, feedback,
  account, backend, sync, network, or remote source.
- ZIP/directory/media/HTML import, linked resources, OCR, Day One, Evernote, or
  a second source-specific importer.
- Date/structure inference from prose, fuzzy/semantic deduplication, rewriting,
  summarization, classification, or AI interpretation.
- Primary/release activation, Git, signing, publication, or release.

## Implementation sequence

1. Record accepted M3-R1 as the next ADR and keep activation validation-only.
2. Implement a pure streaming parser contract and typed preview errors/limits.
3. Parse versioned Fio export and conservative explicit generic Entry blocks;
   preserve exact UTF-8 content, timestamp, and timezone.
4. Add exact local fingerprinting and deduplication against active, deleted,
   and same-batch candidates without retaining plaintext fingerprints remotely.
5. Migrate Room `2 → 3` additively with ImportBatch/ImportBatchItem and minimal
   encrypted provenance; export schema 3 and test populated `1 → 2 → 3` paths.
6. Add staged preview state that performs no canonical write and survives safe
   cancellation/process recreation without persisting unnecessary plaintext.
7. Commit one ready batch transactionally; inject storage/database failures and
   prove zero partial canonical Entries.
8. Add confirmed rollback that tombstones only unchanged batch-created Entries,
   excludes later edits, cancels their pending Returns, and never touches native
   or unrelated Entries.
9. Add validation-only SAF selection, preview, confirmation, result, and batch
   management UI with quiet accessible copy and 48 dp controls.
10. Validate offline builds, dependency locks, Room migrations, malformed/
    oversized/adversarial fixtures, process death, API 26/API 36 matrix, M1/M2
    regressions, manifest/log/temp-file scans, and export round-trip.
11. Reconcile canonical docs and create dated M3 engineering evidence. Stop
    before participant use until every retained pre-pilot gate is closed.

## Required tests

- exact Fio Markdown/plain export round-trip, CRLF/LF, Unicode/emoji, empty and
  maximum-sized content, timezone offset, DST boundary, and stable ordering;
- malformed UTF-8, NUL/control-heavy content, active HTML/script, archive magic,
  unexpected delimiters, invalid/missing dates, 5 MiB/2,000 Entry/256 KiB/
  20,000 line/time limits;
- exact duplicate across active, recently deleted, and same batch; same words at
  a different explicit time remains distinct; no fuzzy matching;
- cancellation before/after parse, database full, injected transaction failure,
  process death, retry, and idempotent commit;
- rollback unchanged batch, edited imported Entry exclusion, native/unrelated
  preservation, pending Return cancellation, Recently Deleted recovery rules;
- schema 1→2→3 and 2→3 preservation, no destructive fallback/downgrade;
- API 26/API 36 UI/provider behavior, font 1.5, landscape, zero animations,
  privacy cover/app lock, and no plaintext/log/network leakage.

## Rollback/disable path

Set the immutable M3 capability false, clear private staging/cache, and leave
schema 3 readable. Existing M1/M2 data and behavior remain usable. A committed
batch is never automatically deleted by disabling the feature; the explicit
confirmed rollback rules still govern it.

## Gate

No application, dependency, schema, permission, or test fixture change for M3
may begin until the owner explicitly approves
`plans/2026-08-13-m3-decision-packet.md`. Approval still does not open the
participant pilot, physical personal-file use, Git, signing, publication, or
release.

## Progress log

- 2026-08-13 — The project owner explicitly approved Android M3-R1. The packet
  is recorded as ADR-039 and implementation is authorized only for the isolated
  validation variant with synthetic fixtures.
- 2026-08-13 — Implemented the pure bounded parser, exact deduplication, Room
  schema 3 encrypted provenance, atomic commit, protected rollback, pending
  Return cancellation, validation-only SAF/UI flow, and named-zone export
  round-trip. The final offline suite passes 51 JVM tests, lint, all build
  variants, and 23 instrumented tests on each API 26/API 36 endpoint. The M3
  SAF profile and retained adverse/credential profiles pass on both endpoints.
  See `evidence/2026-08-13-m3-local-import.md`.

## Final report

The bounded M3 engineering slice is complete in the isolated validation
variant. Release and debug-primary activation remain false. No personal file,
participant data, POCO installation, analytics, network path, Git operation,
signing, publication, or release was used. The Time-only participant pilot
remains blocked by the retained human, physical, provider, second-OEM, and
independent-review gates. The next permitted action is review of Proposed
M4-R1; semantic research cannot begin from this plan alone.
