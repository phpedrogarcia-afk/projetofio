# 05 — Architecture

Status: Accepted Android direction for V0; M1 implementation checkpoint present

## Binding platform-transition notice

`ADR-033`–`ADR-036` make Android the initial client and replace all Apple-
specific implementation choices. Kotlin/Compose, Room/KSP, Android Keystore,
BiometricPrompt, Storage Access Framework, and the Android matrix govern M1.
Superseded Apple details live only in decision/plan history and must not be
implemented.

## Architectural thesis

Fio starts **Android-first, local-first, offline-first, and server-minimal**.

The phone is the authority for writing, reading, decryption, eligibility,
selection, scheduling, and notification. A future server may authenticate
devices and store encrypted synchronization envelopes, but it must not read
journal content or semantic representations.

V0 does not require a production server. Prove the complete local loop first.

## Technology direction

| Layer | V0 direction | Notes |
|---|---|---|
| Client | Native Android with Kotlin and Jetpack Compose | Gradle Kotlin DSL; no cross-platform framework in M1 |
| Concurrency | Kotlin structured coroutines | Keep UI, storage, and background work isolated |
| Local store | Room/KSP over SQLite behind a repository boundary | Export schemas and test every migration |
| Content protection | App-internal storage plus AES-GCM record encryption | Versioned authenticated Entry/Draft envelopes |
| Keys | Non-exportable AES-256 key in Android Keystore | No silent replacement or M1 recovery |
| Search | Local textual search | Semantic search waits for V1 experiment |
| Returns | Local eligibility, selection, scheduling | Deterministic core with injectable clock/randomness |
| Embeddings | On-device adapter | No model is selected in v0.1 |
| Notifications | Local, generic content | App remains usable if permission is denied |
| Server | None required in initial V0 | Later server stores opaque sync envelopes only |
| Analytics | Off by default in development; minimal pilot events when approved | Separate from content and sync paths |

Do not add a production dependency only because it is common. Record why it is
needed, its data access, update policy, offline behavior, and removal path.

### M1 Android execution baseline

Android Studio Quail 3 `2026.1.3` is the recorded stable IDE. The technical
ADR-036 matrix is validated with JDK 17, AGP 9.3.1/Gradle 9.5.0, API 36,
command-line tools 22.0, stable platform-tools 37.0.1, an Android 13/API 33
POCO, and booted API 26/API 36 AVDs. Use `compileSdk = 36` and `targetSdk = 36`;
API 37 remains preview and is excluded from M1. The owner approved `minSdk 26`,
application ID `com.projetofio.app`, and the Android M1 plan on 2026-08-10.

### M1 persistence dependency boundary

AndroidX Room through Gradle is the approved M1 persistence dependency. KSP
generates Room integration; KAPT is excluded.

- Domain and Application expose Fio-owned interfaces and types only.
- Persistence owns Room entities, DAOs, raw SQL, schemas, and migrations.
- Multi-step state changes use explicit transactions.
- Export Room schema JSON and retain migration fixtures.
- Record exact resolved versions, licenses, transitive dependencies, update
  policy, and removal path.
- Destructive migration fallback, Paging, FTS, SQLCipher, and a second database
  wrapper remain outside M1.

## Trust boundaries

```text
┌───────────────────────── Android device ───────────────────────┐
│                                                                │
│  Native Android UI (toolkit pending ADR-034)                   │
│     │                                                          │
│  Application use cases                                        │
│     ├── Entry service ──────┐                                  │
│     ├── Archive/search      │                                  │
│     ├── Returns engine      ├── Domain models                  │
│     ├── Import/export       │                                  │
│     └── Privacy settings ───┘                                  │
│              │                         │                       │
│       encrypted repository     on-device model adapter         │
│              │                         │                       │
│        local SQLite             embeddings/matching            │
│                                                                │
└──────────────┬─────────────────────────┬────────────────────────┘
               │ ciphertext only         │ content-free events only
               ▼                         ▼
       Future sync service        Optional pilot analytics
       (opaque envelopes)         (strict allowlist)
```

Only the device-side trusted process may hold plaintext journal content or
decrypted embeddings. System backups, extensions, notification payloads, logs,
widgets, analytics SDKs, and servers are separate surfaces and must be reviewed
explicitly.

## Module boundaries

### UI

Renders state and sends user intent. It must not implement eligibility,
cryptography, event payload construction, or database queries directly.

Suggested feature groups:

- Write
- Archive
- Return
- ImportExport
- SettingsPrivacy

### Application use cases

Coordinate one outcome at a time, such as `SaveEntry`, `DeleteEntry`,
`SelectNextReturn`, or `ExportArchive`. They depend on protocols, not concrete
storage or network SDKs.

### Domain

Pure models and policies:

- entry lifecycle and return policy;
- return eligibility and candidate selection;
- scheduling windows and frequency caps;
- import validation and deduplication identifiers;
- privacy-safe analytics event definitions.

Domain logic must be testable without the UI toolkit, the clock, random number
generation, notifications, network, or a real database.

### Persistence

One repository boundary owns schema and migrations. It stores:

- encrypted content and encrypted semantic representations;
- local operational metadata required for ordering and eligibility;
- return history, settings, import provenance, tombstones, and revisions when
  that feature is introduced.

No UI feature creates its own parallel store.

M1 uses one Room-managed SQLite repository and a versioned exported-schema/
migration ledger. Save, edit, delete, recover, purge, and Draft-to-Entry
transitions require explicit transactions and tested rollback behavior.

### Cryptography boundary

All conversion between plaintext domain content and stored/synced ciphertext
passes through one reviewed service. Use platform cryptographic primitives; do
not implement ciphers, key derivation, nonce generation, or authentication
formats from scratch.

The architecture must distinguish:

- device file protection;
- app-level content encryption;
- app lock or biometric authorization;
- future end-to-end encrypted sync.

These are related controls, not synonyms.

- keep database, sidecars, settings, and private temporaries in app-internal
  storage;
- generate one non-exportable 256-bit AES key in Android Keystore;
- restrict that key to `AES/GCM/NoPadding` encrypt/decrypt operations;
- seal each Entry and Draft private payload independently with a fresh platform-
  generated IV;
- authenticate envelope version, record kind, opaque record ID, and schema
  version as associated data;
- keep envelope parsing, key access, seal/open, and typed errors in one
  Fio-owned Crypto service;
- fail closed on missing keys, malformed envelopes, or authentication failure.

The only approved M1 operational metadata outside record ciphertext is the
opaque ID; timestamps and original timezone/offset; content, envelope, and
schema versions; source, access, and Return-policy enums; and deletion/purge
fields. The private app sandbox protects the containing database from other
ordinary apps; record encryption remains an additional boundary.

The platform-independent failure rule remains binding: never recover by
replacing a missing key over existing data, resetting the
database, or presenting an empty Archive. Uninstall, app-data clearing, lost-key,
cross-device, and backup recovery remain explicit M1 limitations. Fio data is
excluded from cloud backup/device transfer; export is the M1 portability path.

### Returns engine

Owns eligibility, candidate generation, selection, frequency, and scheduling.
It exposes decisions and reason codes to tests/diagnostics, but the product UI
does not reveal semantic reasons to the user.

Dependencies are injected:

- clock and calendar;
- deterministic random source;
- entry repository;
- return history repository;
- embedding/matching adapter;
- feature flags;
- notification scheduler.

### Model adapter

Semantic behavior is an optional adapter. V0 research may compare Android/on-
device platform and packaged model alternatives using a private, consented
benchmark. The application never depends directly on a single model API.

Deleting or disabling the adapter must leave Time returns fully functional.

### Import/export

Import uses a staging area:

```text
select file → parse locally → preview → validate/deduplicate → commit transaction
```

A failed or cancelled import leaves the canonical store unchanged. Import
provenance is retained without storing unnecessary copies of source files.

Export decrypts locally into a user-selected destination only after explicit
action. Temporary plaintext files must be protected and removed after success,
cancellation, or failure.

M1 export still offers one combined UTF-8 Markdown document or one combined UTF-8
plain-text document, with active Entries ordered oldest to newest. It uses a
versioned header and clear Entry delimiters, preserves exact content and
original timestamp/timezone metadata, and adds no inferred labels. Use Android's
Storage Access Framework and stream directly to the user-selected URI. If a
private temporary is unavoidable, keep it in internal cache and reconcile
cleanup after success, cancellation, failure, and next launch.

## Main data flows

### Write

```text
Editor → SaveEntry → validate → encrypt → transaction → Saved locally
                                             └→ async indexing/embedding if enabled
```

The saved confirmation depends on the local transaction, not remote sync or
embedding completion.

M1 maintains exactly one encrypted active Draft. Meaningful changes autosave
after a debounce; blank drafts are not persisted. `Guardar` creates the Entry
and clears the Draft in one deliberate transaction, with draft removal becoming
effective only after commit. Failure preserves the Draft and produces no saved
confirmation.

### Return

```text
Background opportunity
  → evaluate global policy and eligible entries
  → generate candidates
  → select within caps/diversity rules
  → persist scheduled return
  → schedule generic local notification
  → authenticate if required
  → decrypt and display original entry
```

No notification contains the decrypted entry.

### Future sync

```text
Local change → encrypt/authenticate envelope → upload opaque blob
Remote blob  → download → authenticate/decrypt locally → merge by defined policy
```

The server may know minimum routing metadata such as account, device, opaque
record identifier, envelope version, byte size, and timestamps. It must not know
text, embedding, inferred topic, search query, or return rationale.

Sync conflict policy, account recovery, key recovery, and deletion propagation
must be decided before V1 implementation. “Encrypted sync” is not complete
until those cases are specified and tested.

### Pilot analytics

```text
Domain outcome → allowlisted event builder → content guard → queued event
             → explicit pilot transport (if enabled and consented)
```

The analytics path cannot accept arbitrary dictionaries or strings. Event types
and properties are closed enums/allowlists defined in
`09-ANALYTICS-EXPERIMENTS.md`.

## Background execution

Android background execution is constrained and opportunistic. The engine must
not promise an exact
delivery instant. It schedules within windows and remains correct if:

- background work is delayed;
- the device is offline;
- notifications are denied;
- the clock or timezone changes;
- the app was not launched for a long period;
- the operating system removes pending notification requests.

Opening the app can safely reconcile overdue or invalid scheduled states without
creating a burst of returns.

## Feature flags

Experiments use local, typed flags with safe defaults. Semantic Resonance must
be disabled by default outside an approved pilot cohort and removable without a
database rewrite. Flags cannot bypass privacy controls or Accepted decisions.

## Testing architecture

At minimum, support:

- pure unit tests for eligibility, selection, caps, and state transitions;
- repository tests for transactions, migrations, deletion, and import rollback;
- cryptography tests using documented test vectors and tamper cases;
- notification tests for generic payloads and ignored-return behavior;
- UI tests for write/save, privacy cover, Return controls, accessibility, and
  notification-denied paths;
- analytics contract tests that reject unapproved fields and free text;
- fault tests for low storage, corrupted data, interrupted import/export, and
  delayed background execution.

## Remaining technical choices

The following remain intentionally unsettled after M1-R2:

- real scaffold resolution/compile evidence for the pinned dependency matrix;
- final on-device embedding model;
- future backend, authentication, and sync transport;
- E2EE key recovery and new-device bootstrap;
- crash reporting vendor or whether one is necessary;
- subscription/purchase infrastructure.

Codex must surface these as decisions in a plan rather than choosing silently.
