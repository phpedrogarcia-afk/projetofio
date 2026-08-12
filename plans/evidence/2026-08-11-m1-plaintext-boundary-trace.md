# M1 plaintext boundary trace

Status: Prepared for independent review; sign-off remains a mandatory ADR-037
pre-pilot gate
Date: 2026-08-11

This document maps the current implementation. It is not an independent review
and makes no claim that memory plaintext can be erased deterministically by the
managed Android/Kotlin runtime.

## Boundary map

| Path | Plaintext exists | Persistent boundary | Failure behavior |
|---|---|---|---|
| Write/edit | Compose text field and `FioUiState` process memory | `RoomFioRepository` calls `ContentCipher.seal` before constructing the Room entity | UI retains/reports recoverable content; no false success |
| Draft autosave | UI state, `Draft`, cipher input | only `content_envelope` BLOB is stored | blank clears Draft; encryption/storage failure stays visible |
| Save | exact editor `String`, encrypted Draft, then `Entry` | encrypted Entry insert and Draft deletion share one Room transaction | forced failure retains the encrypted recoverable Draft |
| Read/Archive | cipher output, domain model, ViewModel/UI memory | Room still contains only the envelope BLOB | missing key, corrupt envelope, invalid UTF-8, or database failure produces a closed error, never an empty-archive claim |
| Export | decrypted Entry list and assembled Markdown/TXT `String` in process memory | UTF-8 is written directly through the user-selected `ContentResolver` URI | cancellation builds nothing; provider/write failure reports failure; Fio creates no temporary plaintext file |
| Delete | no new content copy | soft delete changes metadata; permanent/expired purge deletes the encrypted row | deletion is restricted to the expected record state |

## Stored fields

Entry/Draft content crosses Room only as `content_envelope` BLOB. Record IDs,
timestamps, timezone, source, format, Return mode, deletion deadline, app-lock
mode, and schema version are metadata and are not content-encrypted in M1.
Application backup/device-transfer is excluded; production has no journal
network endpoint or analytics transport.

## Cryptographic boundary

- AES-256-GCM through Android Keystore alias `fio_m1_content_v1`;
- platform-generated randomized IV for every seal;
- authenticated associated data contains envelope version, record kind, opaque
  record ID, and schema version;
- versioned `FIO1` binary envelope, strict length checks, 128-bit tag, and
  strict UTF-8 decoding;
- key is non-exportable through the application API;
- if ciphertext exists and the key is absent, no replacement key is generated.

## Export boundary

`FioService.export` builds the selected document in memory only after a
non-null destination exists. `AndroidDocumentWriter` opens that destination in
truncate/write mode, writes UTF-8 through a scoped stream, and closes it with
`use`. Fio retains neither a temporary file nor the selected URI. Once written,
the exported copy is intentionally outside Fio's encryption and lifecycle;
the Settings copy warns the user before export.

## Fail-closed evidence for reviewer

- `AesGcmContentCipherTest`: tamper, AAD, envelope/version, strict UTF-8;
- `AndroidKeystorePersistenceTest`: reopen lifecycle and ciphertext boundary;
- `AdverseConditionTest`: missing key, corrupt envelope, transaction rollback,
  SQLite full, and non-destructive corrupt-database open;
- `AndroidProviderFailureTest`: production writer through a refusing Android
  provider;
- `ExportCoordinatorTest`: cancel/failure/large-document outcomes;
- effective validation/release manifests: no Fio debug fixture, no Internet or
  broad storage permission, backup disabled, cleartext disabled.

## Independent reviewer checklist

- trace every call that supplies `Entry.content` or `Draft.content` to Room;
- confirm no alternate DAO/entity column stores content or snippets;
- inspect logging/crash strings for content-bearing interpolation;
- confirm export has no temporary-file or retained-URI path;
- verify key-loss/corruption paths cannot recreate an empty database or key;
- inspect the effective release manifest and backup extraction rules;
- record reviewer identity, date, scope, findings, and disposition in a new
  evidence file without editing this prepared trace.
