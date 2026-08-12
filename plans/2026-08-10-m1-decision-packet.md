# Milestone 1 decision packet — Recommendation R1

Status: Approved by project owner
Related plan: `2026-08-10-m1-trusted-local-core.md`
Prepared: 2026-08-10
Approved: 2026-08-10
Scope: Decisions required before Xcode project generation

## Purpose

This packet converts the open gates in the Milestone 1 plan into one coherent,
reviewable recommendation. It does not change any ADR to Accepted, authorize a
dependency, generate an app, initialize Git, or make a release.

The owner may approve the package as written or list exceptions. After explicit
approval, the next documentation-only step is to update the affected ADRs and
canonical specifications. Application generation remains blocked until the
Apple execution environment is available and recorded.

## Current evidence

- The current host is Windows and has no `xcodebuild`; it cannot compile or
  validate an iOS/SwiftUI app.
- `app/` still contains only `README.md`.
- `Fio/` is not currently a Git repository and has no discoverable parent Git
  repository from the current workspace.
- The accepted architecture requires iOS/SwiftUI, SQLite, local/offline-first
  behavior, Apple Data Protection, app-level authenticated encryption, Keychain
  keys, and optional platform authentication.

## Recommendation R1

### R1.1 — Apple environment and platform baseline

Approve this policy now:

- app generation, dependency resolution, compilation, simulator/UI tests,
  entitlements, signing checks, and device validation run only on an approved
  macOS host with Xcode;
- the exact Xcode, Swift, minimum iOS version, simulator, and physical-device
  matrix are selected from that host and intended pilot devices immediately
  before generation;
- no “latest version” or deployment target is guessed from Windows;
- no milestone test is marked passed without evidence from the recorded Apple
  toolchain.

Effect: documentation and ADR preparation can continue here, but application
generation remains blocked until the Apple host exists.

### R1.2 — Persistence: GRDB over system SQLite

Recommend accepting `ADR-022` with this decision:

- use GRDB through Swift Package Manager as the concrete SQLite access and
  migration layer;
- use the SQLite library shipped by the target operating system;
- pin one exact reviewed GRDB release compatible with the selected Xcode/Swift
  toolchain when the project is generated;
- keep Domain and Application dependent only on Fio-owned repository protocols;
- allow raw SQL inside Persistence where it improves migration clarity;
- use explicit transactions for multi-step state changes;
- do not adopt GRDBQuery, SQLCipher, full-text search, database observation, or
  other companion features in Milestone 1 unless separately approved;
- record the MIT license, selected version, checksum/resolution file, transitive
  dependency inventory, update policy, and removal path.

Why this is recommended:

- Fio needs reliable transaction, migration, concurrency, error, and fixture
  behavior more than it needs to minimize every line of dependency code.
- GRDB exposes SQLite directly while providing tested migration and transaction
  tools.
- The repository boundary prevents GRDB types from becoming the domain model.
- SQLite itself permits concurrent reads but only one simultaneous writer, so a
  deliberate writer/transaction policy is required regardless of wrapper.

Risk accepted: one production open-source dependency and its maintenance/supply-
chain surface.

Risk not accepted: letting the wrapper choose Fio's schema, encryption, Return
policy, or UI architecture.

### R1.3 — Local content protection: versioned CryptoKit AES-GCM records

Recommend accepting `ADR-023` for Milestone 1 with this bounded design:

- protect the database and Fio-owned temporary files with Apple Data Protection
  `.complete` because all M1 content work is foreground-only;
- generate one random 256-bit content-encryption key using CryptoKit;
- store that key as a Keychain item with
  `kSecAttrAccessibleWhenUnlockedThisDeviceOnly`;
- encrypt Entry and Draft private payloads with CryptoKit AES-GCM;
- let CryptoKit generate a fresh nonce for every seal operation;
- authenticate stable associated data containing envelope version, record kind,
  opaque record ID, and schema version;
- store a versioned envelope containing only the fields required to reconstruct
  and verify the AES-GCM sealed box;
- keep key access, sealing/opening, envelope parsing, and error mapping inside one
  Fio-owned Crypto service;
- fail closed on missing key, invalid envelope, or authentication failure;
- never reset the database, generate a replacement key over existing data, or
  show an empty Archive as recovery;
- do not claim end-to-end encryption, zero knowledge, secure deletion, Secure
  Enclave key storage, independent audit, or cross-device recovery.

Approved local plaintext boundary:

- editor/view memory while authorized;
- transient values inside Domain/Application use cases;
- explicit user-selected export destination;
- protected temporary export data for the minimum controllable lifetime.

Approved operational metadata outside record ciphertext, still protected by the
database file's Data Protection class:

- opaque Entry ID;
- `createdAt`, `originalCreatedAt`, original timezone/offset, and `updatedAt`;
- content/envelope/schema version;
- source enum;
- access and Return-policy enum values;
- `deletedAt` and `purgeAfter`.

Key and recovery consequence accepted for M1:

- `ThisDeviceOnly` key material does not migrate to another device;
- M1 provides no reinstall, cross-device restore, or lost-key recovery;
- loss of the key makes encrypted content unavailable and must produce an honest
  non-destructive failure state;
- human-readable export is the only M1 portability path;
- V1 sync/backup requires a new approved key hierarchy and migration plan.

Reason: Apple's Keychain documentation states that this accessibility class is
available only while the device is unlocked and does not migrate to a new
device. The restriction matches foreground-only M1 but must be accepted as a
real recoverability tradeoff, not hidden in implementation.

### R1.4 — Return consent before Milestone 2

Recommend adding and accepting a new ADR with this decision:

- add `returnConsentState` to AppSettings with
  `notConfigured | enabled | paused`;
- default every Milestone 1 installation and migration to `notConfigured`;
- retain per-entry `returnMode = eligible | never` as a subordinate policy;
- `eligible` never authorizes delivery while global consent is `notConfigured`;
- only explicit Milestone 2 onboarding can transition the global state to
  `enabled`;
- `paused` is available only after Returns have been configured;
- upgrading an M1 database cannot schedule or display a Return without the
  explicit transition;
- update Data Model, Returns Engine, UX, and decision records before schema v1
  is frozen.

Reason: this resolves the current conflict between required Return-policy fields
and the rule against silently inferring future Return consent.

### R1.5 — One encrypted active draft

Approve this Milestone 1 behavior:

- support exactly one active local draft;
- do not persist a blank draft;
- debounce autosave after meaningful text changes;
- protect draft content with the same AES-GCM/Keychain path as Entries;
- `Guardar` creates the Entry transactionally and removes the draft only after
  commit succeeds;
- save/encryption/database failure preserves the draft and never displays
  `Guardado.`;
- clearing the editor and confirming departure removes the draft;
- no draft history, sync, analytics, or multiple-draft list.

Reason: one draft protects against loss without creating a hidden second Archive
or revision feature.

### R1.6 — Export: one explicit UTF-8 document per chosen format

Approve a deliberately simple M1 export:

- offer one combined Markdown document or one combined plain-text document;
- export active Entries only, oldest-to-newest for reading continuity;
- use UTF-8 and documented LF newline normalization;
- include a format/version header and export date;
- for each Entry, include opaque ID, original timestamp, timezone/offset when
  known, then the exact user content in a clearly delimited section;
- do not add topics, titles, summaries, sentiment, or inferred metadata;
- use SwiftUI's system file-export flow with an in-memory `FileDocument` snapshot
  where the selected platform version supports the approved API;
- if a temporary file is required by the final API path, apply `.complete`
  protection and clean it after success, cancellation, failure, and next-launch
  reconciliation;
- warn that the destination is outside Fio's protection;
- require local authorization before export when app lock is enabled;
- never upload, log, analyze, or retain a second export copy.

Reason: one document is human-readable, easy to inspect, and avoids a ZIP/package
dependency in Milestone 1. A later version may add a multi-file archival package
through a new format version without invalidating the original export.

### R1.7 — Privacy cover and optional app lock defaults

Approve this behavior:

- privacy cover is enabled by default and has no content preview;
- app lock is optional and off until the user enables it;
- modes are immediate, one minute, and five minutes, subject to verified iOS
  lifecycle behavior;
- use LocalAuthentication with the device-owner authentication policy so the
  platform may use biometrics or device passcode;
- include the required localized Face ID usage description when biometric
  authentication is available;
- use a short PT-BR reason that names the protected action, not a security claim;
- do not assume one successful authentication authorizes future operations;
- require fresh approved authorization before export and permanent deletion when
  app lock is on;
- reveal no Entry content while locked or after cancellation/failure;
- keep app lock separate from Keychain accessibility and record encryption.

Reason: Apple LocalAuthentication returns only success/failure and keeps
biometric data outside the app. The recommended default avoids claiming Fio
stores or validates biometric information itself.

### R1.8 — Git boundary

Recommend the following separate operation before app generation:

- initialize `Fio/` as a local Git repository only after explicit authorization;
- create no remote and perform no push/publication;
- inspect the complete initial diff and `.gitignore` before any commit;
- if a baseline commit is desired, request separate authorization for that
  commit and stage explicit paths only;
- keep release ZIPs ignored unless the owner later chooses another artifact
  publication mechanism.

Reason: the current workspace exposes no Git repository for `Fio`. Local version
history is appropriate for a high-level project, but Git initialization, commit,
remote creation, and publication are distinct actions.

## Decisions deliberately deferred

This package does not decide:

- exact Xcode/Swift/minimum iOS versions before the Apple host is inspected;
- application bundle identifier, signing team, App Store identity, or product
  name availability;
- V1 key recovery, cross-device migration, sync, backup, or E2EE;
- notifications, Return scheduling, imports, search, analytics, crash vendor,
  embeddings, or monetization;
- CI provider, Git hosting, branch policy, remote repository, publication, or
  deployment.

## Primary evidence consulted

- [Apple CryptoKit AES-GCM](https://developer.apple.com/documentation/cryptokit/aes/gcm)
- [Apple Keychain item accessibility](https://developer.apple.com/documentation/security/restricting-keychain-item-accessibility)
- [Apple `kSecAttrAccessibleWhenUnlockedThisDeviceOnly`](https://developer.apple.com/documentation/security/ksecattraccessiblewhenunlockedthisdeviceonly)
- [Apple LocalAuthentication](https://developer.apple.com/documentation/localauthentication)
- [Apple File Protection types](https://developer.apple.com/documentation/foundation/fileprotectiontype)
- [Apple SwiftUI `FileDocument`](https://developer.apple.com/documentation/swiftui/filedocument)
- [Apple SwiftUI `fileExporter`](https://developer.apple.com/documentation/swiftui/view/fileexporter(ispresented:document:contenttypes:defaultfilename:oncompletion:oncancellation:))
- [SQLite transaction documentation](https://www.sqlite.org/lang_transaction.html)
- [SQLite schema alteration documentation](https://sqlite.org/lang_altertable.html)
- [GRDB official repository and documentation](https://github.com/groue/GRDB.swift)

These sources establish current API/capability direction. Exact versions and
runtime behavior still require verification on the approved Apple toolchain.

## Approval statement

To approve this package without exceptions, the project owner can state:

> Aprovo o pacote de decisões M1-R1.

To modify it, list the affected item numbers, for example:

> Aprovo M1-R1, exceto R1.6; prefiro um arquivo por entrada.

After approval, Codex may update ADRs and canonical specifications only. Xcode
project generation still waits for the recorded macOS/Xcode environment and any
separately authorized Git operation.

## Disposition

The project owner approved Recommendation R1 without exceptions on 2026-08-10
using the canonical approval statement above. The approved choices are recorded
as `ADR-022`, `ADR-023`, and `ADR-027`–`ADR-032` in `docs/DECISIONS.md` and are
being incorporated into the affected canonical specifications.

This approval does not approve the full M1 execution plan, select the Apple
toolchain matrix, generate an Xcode project, install GRDB, initialize Git,
commit, create a remote, publish, or release anything.

Subsequent state: the project owner separately approved the full M1 execution
plan on 2026-08-10. The Apple toolchain matrix remains open, and no application,
dependency, Git, publication, or release action has occurred.

Later correction: before generation, the owner clarified that the real target
is Android. `ADR-033` Supersedes the Apple-specific R1 decisions, and the former
iOS plan is Blocked pending Android Recommendation R2. No application or
dependency was generated from R1.
