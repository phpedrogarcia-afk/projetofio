# 06 — Data model

Status: Canonical domain model for specification v0.1; M1 subset mapped to Room
schema version 1

Platform note: `ADR-033` changes the client to Android. Entity and consent
invariants remain canonical. The Android Keystore/AES-GCM envelope boundary is
Accepted through `ADR-035`; the M1 Entry/Draft/AppSettings subset is implemented
as exported Room schema version 1 under the Approved Android M1 execution plan.

## Goals

The model must preserve original user content, support quiet returns, remain
usable offline, and allow future encrypted sync without letting each feature
invent its own representation.

The names below describe domain concepts. Concrete Kotlin and SQL names may vary
only if the mapping is documented and invariants remain intact.

## Conventions

- Identifiers are locally generated opaque UUIDs.
- Persist instants in UTC; retain source timezone/offset when it matters for the
  original autobiographical date.
- Use half-open windows: `[windowStart, windowEnd)`.
- Store enum values explicitly and version serialized formats.
- User-authored content is never placed in logs, analytics, notification
  payloads, or server routing metadata.
- “Encrypted” fields below are plaintext only inside the trusted device process
  and ciphertext at the persistence/sync boundary.
- A row marked deleted is a tombstone until its recovery/purge policy completes.
- Derived indexes can be rebuilt. The original entry and import provenance
  cannot depend on a derived index for recovery.

## M1 encrypted record envelope

Entry and Draft private payloads must cross the Persistence boundary as a
versioned authenticated-encryption envelope using the Accepted Android Keystore
AES-256-GCM design from `ADR-035`. The stable associated data is not encrypted
and contains:

```text
envelopeVersion
recordKind             // entry | draft
opaqueRecordId
schemaVersion
```

Every seal operation must use a fresh platform-generated nonce/IV. The stored
envelope contains only the version and values required to reconstruct and
authenticate the sealed value. Envelope parsing, sealing, and opening belong to
the Fio Crypto service, not domain entities or UI code. Authentication failure,
an invalid envelope, or a missing key is a non-destructive error; it never
authorizes a reset or replacement key over existing records.

## Entity map

```text
Entry ──< EntryRevision          (revision history, V1)
  │
  ├──< Attachment                (V2)
  ├──< Return >── ReturnFeedback
  │       │
  │       └── experiment metadata (content-free, local)
  └── ImportBatchItem >── ImportBatch

Device ──< SyncEnvelope          (V1)
Draft                            (single active encrypted draft, M1)
AppSettings                      (singleton per local profile)
AnalyticsEvent                   (strict allowlist, pilot only)
```

## `Entry`

One piece of autobiographical material created by or imported for the user.

| Field | Type | Required | Meaning |
|---|---|---:|---|
| `id` | UUID | yes | Stable local identity |
| `createdAt` | instant | yes | When Fio first stored the record |
| `originalCreatedAt` | instant | yes | Autobiographical date; for native entries equals first save time |
| `originalTimeZone` | string? | no | Source timezone/offset if available |
| `updatedAt` | instant | yes | Last user-visible content or policy change |
| `source` | enum | yes | `native`, `importMarkdown`, `importText`, or approved importer |
| `sourceExternalId` | encrypted string? | no | Local deduplication/provenance; never analytics/server metadata |
| `contentCiphertext` | bytes | yes | Authenticated encrypted original text/content |
| `contentFormat` | enum | yes | `plainText` in V0; versioned for later formats |
| `contentLocale` | string? | no | User/source locale, not an inferred topic |
| `accessPolicy` | enum | yes | `standard` in V0; `sealed` in V1 |
| `returnMode` | enum | yes | `eligible` or `never` |
| `restUntil` | instant? | no | Temporary ineligibility boundary |
| `requestedWindowStart` | instant? | no | Start of explicit Remember Later window |
| `requestedWindowEnd` | instant? | no | End of explicit Remember Later window |
| `lastReturnedAt` | instant? | no | Cached most recent opened/attempted policy timestamp as specified by engine |
| `returnCount` | integer | yes | Cached number of completed/opened returns; default `0` |
| `embeddingCiphertext` | bytes? | no | Encrypted local semantic vector/index artifact |
| `embeddingModelVersion` | string? | no | Adapter/model identifier needed for reindexing |
| `embeddingUpdatedAt` | instant? | no | Last successful semantic index build |
| `deletedAt` | instant? | no | Tombstone start |
| `purgeAfter` | instant? | no | Expected automatic purge boundary |
| `schemaVersion` | integer | yes | Record serialization version |

### Entry invariants

1. `originalCreatedAt` is never silently replaced by import time.
2. `requestedWindowStart` and `requestedWindowEnd` are both null or both set,
   and start is earlier than end.
3. `returnMode = never` clears or suppresses any requested return window.
4. `restUntil` cannot coexist with an overlapping requested return window
   without an explicit user resolution in the UI.
5. A deleted entry is excluded from search, indexing, and return eligibility.
6. Sealing changes access authorization, not encryption status or return policy.
7. Embedding absence or failure never prevents writing, reading, exporting, or
   Time returns.
8. Cached `lastReturnedAt` and `returnCount` must be reconcilable from Return
   history; they are not the sole record of history.
9. `returnMode = eligible` is subordinate to
   `AppSettings.returnConsentState`; it never authorizes a Return while the
   global state is `notConfigured` or `paused`.

## `Draft` — Milestone 1

One recoverable editor state. M1 supports at most one active Draft and no draft
history.

| Field | Type | Required | Meaning |
|---|---|---:|---|
| `id` | UUID | yes | Stable singleton draft identity while the draft exists |
| `updatedAt` | instant | yes | Last successful encrypted autosave |
| `contentCiphertext` | bytes | yes | Authenticated encrypted draft text |
| `contentFormat` | enum | yes | `plainText` in M1 |
| `schemaVersion` | integer | yes | Draft payload/envelope mapping version |

Draft invariants:

1. Blank content is not persisted.
2. Meaningful changes autosave after a debounce through the same Crypto service
   and platform-keystore path as Entries.
3. `Guardar` inserts the Entry and removes the Draft transactionally; the Draft
   remains recoverable unless and until the Entry commit succeeds.
4. Encryption, database, or save failure preserves the last recoverable Draft
   and cannot produce `Guardado.`.
5. Explicitly clearing the editor and confirming departure removes the Draft.
6. Multiple drafts, revisions, sync, analytics, and a draft browser are outside
   M1.

## `EntryRevision` — planned V1

An immutable encrypted snapshot created before a user-visible edit.

| Field | Type | Meaning |
|---|---|---|
| `id` | UUID | Revision identity |
| `entryId` | UUID | Parent entry |
| `createdAt` | instant | Snapshot time |
| `contentCiphertext` | bytes | Previous authenticated encrypted content |
| `contentFormat` | enum | Format at snapshot time |
| `reason` | enum | `edit`, `importCorrection`, `restore` |
| `schemaVersion` | integer | Serialized revision format |

Revisions are private content and follow export, deletion, encryption, and sync
rules. Analytics never records revision text or diff size.

## `Return`

A persisted attempt to present one or more entries. V0 supports exactly one
entry per Return; later multi-entry experiences require a separate Accepted
decision and compatible schema migration.

| Field | Type | Required | Meaning |
|---|---|---:|---|
| `id` | UUID | yes | Return identity |
| `entryIds` | ordered UUID list | yes | One item in V0 |
| `algorithm` | enum | yes | `time`, `resonance`, `requested`, later `recurrence`/`bridge` |
| `algorithmVersion` | string | yes | Reproducible local policy version |
| `state` | enum | yes | Lifecycle state below |
| `createdAt` | instant | yes | Candidate selected |
| `windowStart` | instant | yes | Earliest valid delivery |
| `windowEnd` | instant | yes | Expiry/re-evaluation boundary |
| `scheduledAt` | instant? | no | Intended local notification time |
| `notifiedAt` | instant? | no | Best known notification delivery/request time |
| `openedAt` | instant? | no | User opened this return |
| `dismissedAt` | instant? | no | User closed/dismissed it |
| `expiredAt` | instant? | no | Window ended unopened |
| `cancelledAt` | instant? | no | Policy change invalidated it |
| `cancelReason` | enum? | no | Content-free reason such as `paused`, `entryDeleted`, `entryNever`, `superseded` |
| `candidateScore` | encrypted/local number? | no | Diagnostic only; never UI, analytics, or server routing |
| `ageBucket` | enum | yes | Coarse controlled experiment bucket |
| `experimentId` | opaque string? | no | Approved pilot assignment |
| `experimentArm` | enum? | no | `time` or `resonance`; content-free |
| `schemaVersion` | integer | yes | Record format |

### Return lifecycle

```text
selected → scheduled → notified → opened → dismissed
    │          │           │         └→ rejected/rested through Entry policy
    │          │           └→ expired
    │          └→ cancelled
    └→ cancelled
```

States are monotonic except a safe reconciliation may derive a terminal state
from evidence. A terminal Return is not rescheduled as the same record.

### Return invariants

1. Every referenced entry exists and is eligible at selection time.
2. Eligibility is checked again before display; deletion, Never Return, pause,
   or Rest can cancel a scheduled Return.
3. `openedAt` is never inferred from notification scheduling.
4. Ignoring a notification does not create a second notification record.
5. Algorithm and experiment identifiers are internal; they are not shown on the
   Return screen.
6. Rejection changes the entry policy and cancels other pending returns for it.
7. Return history survives entry recovery from Recently Deleted, but content is
   inaccessible while the entry is deleted.

## `ReturnCandidate`

An ephemeral engine value, not a general persistence entity.

```text
ReturnCandidate
- entryId
- algorithm
- ageBucket
- score?                 // local only
- eligibilitySnapshot
- exclusionReasons[]
```

Only the selected candidate becomes a Return. Persisting every candidate would
create sensitive behavioral exhaust and is forbidden unless a future approved
experiment demonstrates necessity.

## `ReturnFeedback`

Optional, skippable pilot response associated with an opened Return.

| Field | Type | Meaning |
|---|---|---|
| `id` | UUID | Feedback identity |
| `returnId` | UUID | Parent Return |
| `answer` | enum | `yes` or `no` |
| `answeredAt` | instant | Response time |
| `studyVersion` | string | Exact approved question/version |
| `uploadState` | enum | `localOnly`, `queued`, `uploaded` |

There is no free-text response in product telemetry. Qualitative interview notes
live outside the app under separately consented research handling.

## `ImportBatch`

Tracks a user-authorized local import transaction.

| Field | Type | Meaning |
|---|---|---|
| `id` | UUID | Batch identity |
| `source` | enum | Parser/source type |
| `startedAt` | instant | Parse start |
| `committedAt` | instant? | Canonical transaction completed |
| `status` | enum | `staging`, `ready`, `committed`, `cancelled`, `failed`, `rolledBack` |
| `sourceFileNameCiphertext` | bytes? | Optional encrypted local provenance |
| `parsedCount` | integer | Candidate count |
| `importedCount` | integer | Successfully inserted entries |
| `duplicateCount` | integer | Explicitly skipped duplicates |
| `failedCount` | integer | Validation failures |
| `parserVersion` | string | Reproducibility |

`ImportBatchItem` maps each staged source item to its result without retaining
unnecessary plaintext. A committed batch can be rolled back by tombstoning only
the entries it created, subject to user confirmation and later edits.

## `Attachment` — planned V2

Attachments are not part of V0/V1 implementation. Reserved shape:

```text
Attachment
- id
- entryId
- kind: photo | audio
- contentCiphertext
- mediaType
- byteCount
- createdAt
- duration? / pixel metadata?
- deletedAt
```

EXIF/location, thumbnails, transcriptions, and waveforms are private derived
content. Do not add them implicitly.

## `AppSettings`

One local settings aggregate.

| Field | Type | Meaning |
|---|---|---|
| `returnConsentState` | enum | `notConfigured`, `enabled`, or `paused`; default `notConfigured` |
| `returnsPausedAt` | instant? | Audit/reconciliation boundary |
| `quietHoursStart` / `quietHoursEnd` | local time? | User delivery preference |
| `notificationPermissionObserved` | enum | Cached system state, not authority |
| `appLockMode` | enum | `off`, `immediate`, `oneMinute`, `fiveMinutes` |
| `privacyCoverEnabled` | boolean | App switcher cover |
| `pilotConsentVersion` | string? | Approved study consent |
| `analyticsEnabled` | boolean | Pilot-only telemetry switch |
| `featureFlags` | typed versioned map | Approved removable experiments only |
| `schemaVersion` | integer | Settings format |

Settings that weaken privacy require explicit UI action and must not be changed
remotely by analytics or experimentation.

`returnConsentState` rules:

- every M1 installation and migration starts as `notConfigured`;
- M1 has no action that changes it to `enabled` and schedules no Returns;
- only explicit M2 onboarding can change `notConfigured → enabled`;
- `paused` is valid only after Returns have been configured;
- resume is an explicit `paused → enabled` action;
- per-entry `eligible` never overrides a non-enabled global state.

## `Device` and `SyncEnvelope` — planned V1

These entities remain provisional until the V1 sync and key-management decision.

The server-visible envelope may contain only minimum routing data:

```text
SyncEnvelope
- opaqueEnvelopeId
- opaqueAccountId
- opaqueDeviceId
- recordKindVersion
- encryptedPayload
- authenticationTag / authenticated envelope data
- createdAt / updatedAt
- byteCount
- tombstone marker if design requires it
```

Entry text, embedding, source filename, search terms, algorithm scores, and
content-derived labels cannot be server routing fields.

## `AnalyticsEvent` — experimental V0 pilot

Events are typed values defined in `09-ANALYTICS-EXPERIMENTS.md`, not an open
key-value table. The local queue stores:

```text
AnalyticsEvent
- id
- eventName             // strict enum
- occurredAt            // coarsened where possible
- approvedProperties    // event-specific closed schema
- consentVersion
- studyVersion
- uploadState
```

No API accepts an arbitrary `metadata`, `context`, `label`, or free-text field.

## Deletion and retention

V0 default:

- deleting an Entry moves it to Recently Deleted;
- recovery window: 30 days;
- `purgeAfter` is set at deletion;
- user may choose permanent deletion immediately after clear confirmation;
- purge removes entry ciphertext, embeddings, import item mapping, pending
  Returns, revisions, attachments, and local searchable derivatives;
- aggregate content-free study events may remain only under the consented study
  retention policy and cannot be traced back to content.

Future sync must propagate deletion and prevent an old device from resurrecting
purged content. That protocol is a V1 prerequisite, not an implementation detail.

## Migration rules

- Every schema change has a versioned, tested migration.
- Destructive migration requires a verified backup/recovery path or must stop.
- Never silently infer new return consent from old data.
- Existing M1 databases migrate to
  `returnConsentState = notConfigured`; migration cannot schedule or display a
  Return.
- New fields default to the most private and least intrusive behavior.
- Semantic indexes may be discarded and rebuilt; original content may not.
- Migration tests include old fixtures, interrupted migration, insufficient
  storage, and downgrade/rollback expectations where supported.
