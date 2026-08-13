# M3-R1 — Android local import decision packet

Status: Accepted — approved by project owner on 2026-08-13
Date: 2026-08-13
Related milestone: Milestone 3 — Import and Time-only pilot
Authority boundary: ADR-015 and ADR-037 permit synthetic-data M3 import
engineering after M2; the participant pilot remains blocked by pre-pilot gates

## Recommended package

Approve the following bounded implementation decisions as one package:

1. **Supported inputs.** M3 imports one UTF-8 `.txt` or `.md` document selected
   through Android's Storage Access Framework. The first implementation accepts
   Fio's versioned M1 export format losslessly and a conservative generic format
   made of explicit Entry blocks. It does not guess structure from prose, open
   archives, follow links, execute Markdown/HTML, or add a source-specific
   importer without participant evidence.
2. **Dates fail closed.** Every multi-Entry block must provide an explicit
   original timestamp; timezone/offset is preserved when present. Missing or
   invalid dates stay visible as preview errors and cannot be committed. A
   single undated document may be imported only after the person explicitly
   chooses its original date in preview; import time is never silently used as
   the autobiographical date.
3. **Bounded parser.** Limit one source document to 5 MiB, 2,000 candidate
   Entries, 256 KiB UTF-8 per Entry, 20,000 lines, and five seconds of parser
   work per attempt. Reject NUL/control-heavy input, malformed UTF-8, unsupported
   encodings, active HTML/script content, archive/container signatures, and any
   limit breach before canonical writes.
4. **Exact local deduplication.** Compute a local SHA-256 fingerprint over the
   exact UTF-8 content plus canonical original timestamp/timezone. Compare it
   with active, recently deleted, and items staged in the same batch. Preview
   labels exact duplicates and excludes them from commit. No fuzzy/semantic
   deduplication, fingerprint analytics, or network lookup enters M3.
5. **Schema `2 → 3`.** Add encrypted/minimal Entry import provenance,
   `ImportBatch`, and `ImportBatchItem`. Staging plaintext remains memory/private
   cache only and is removed on cancel, failure, success, and next launch.
   Migration defaults create no batch, Entry, consent, Return, or work.
6. **Atomic commit and bounded rollback.** Commit one ready batch in a Room
   transaction. Cancellation or failure changes zero canonical Entries. A later
   rollback tombstones only unchanged Entries created by that batch. Entries
   edited after import are excluded and reported for explicit individual action;
   unrelated/native Entries are never touched.
7. **Validation-only activation.** Import UI and receiver behavior are available
   only in the isolated validation variant while ADR-037 gates remain open.
   Debug-primary/release stay inactive. Tests use synthetic files and private
   test providers; the connected POCO and personal files remain outside the
   automated run.
8. **No pilot expansion.** This package authorizes parser/schema/UI engineering
   and emulator validation only. It does not authorize participant enrollment,
   real journal import, qualitative feedback, analytics, second source format,
   Git, signing, publication, release, or a Time-only pilot.

## Why this is recommended

- It preserves original words and dates without pretending an arbitrary text
  file has reliable structure.
- Exact deduplication is explainable and reversible; fuzzy matching would risk
  silently dropping distinct autobiographical entries.
- Bounded streaming and atomic writes reduce memory, corruption, traversal, and
  partial-import risk across the API 26/API 36 device range.
- Excluding post-import edits from bulk rollback prevents a historical batch
  action from deleting later user work.
- Validation-only activation preserves the accepted ADR-037 gate boundary.

## Rejected alternatives

- Guessing dates from headings, prose, file modification time, or import time.
- Treating arbitrary Markdown paragraphs as separate Entries without previewed
  explicit delimiters.
- ZIP/archive import, directory traversal, linked resources, images, HTML, or
  executable/active content.
- Fuzzy, semantic, or model-based deduplication.
- Partial commit followed by best-effort cleanup.
- Bulk rollback that deletes an imported Entry after the person edited it.
- Enabling import in primary/release or using the owner's personal files during
  engineering.

## Approval wording

> Aprovo o pacote Android M3-R1 — Importação local.

Approval authorizes the bounded M3 plan, ADR recording, Room schema 3,
validation-only code, synthetic fixtures, and proportional emulator tests. It
does not authorize the M3 participant pilot or any Git/release operation.
