# 08 — Privacy and security

Status: Mandatory product boundary for specification v0.1

Platform note: `ADR-033` makes Android the first client and `ADR-035` defines
the accepted M1 protection boundary. Android Keystore, AES-GCM, app-internal
storage, `BiometricPrompt`, `FLAG_SECURE`, backup exclusions, and the Storage
Access Framework replace the Superseded Apple-specific mechanisms.

This document defines requirements, not a certification. Fio must not claim
end-to-end encryption, “zero knowledge,” biometric protection, secure deletion,
or any equivalent property until the exact implementation and recovery paths
have been reviewed and tested.

## Privacy promise

Fio is designed so that a person's autobiographical content can be processed in
plaintext only on their authorized device. Future services may store and move
authenticated ciphertext, but must not read journal content or embeddings.

Privacy includes the right to:

- write and read offline;
- understand what leaves the device;
- decline optional analytics or research;
- pause returns and prevent an Entry from returning;
- export in durable human-readable formats;
- delete locally and, in a future sync system, propagate deletion;
- leave without retention pressure.

## Sensitive assets

Treat all of the following as **private content**:

- entry text and revision history;
- drafts, deleted items, and import source files;
- embeddings, semantic scores, clusters, and indexes;
- search queries and results;
- photos, audio, transcriptions, thumbnails, and EXIF;
- optional location and source provenance;
- export files and temporary decrypted files;
- encryption keys, recovery material, and authentication tokens;
- notification-to-Entry linkage;
- free-text research responses, if separately collected outside the product.

Treat these as **sensitive operational metadata** even when content-free:

- account/device identifiers;
- record counts, timestamps, byte sizes, and sync frequency;
- Return opened/rejected timestamps and algorithm assignment;
- app lock, pause, and consent settings;
- crash traces and database/migration error details.

## Threat model

Before each release, update a concrete threat model covering at least:

| Threat | Required posture |
|---|---|
| Lost or borrowed device | Rely on device protection; optional app lock and privacy cover |
| Notification seen by another person | Generic text only; no private preview |
| App switcher snapshot | Replace with neutral cover before background snapshot |
| Network observer | TLS plus authenticated application ciphertext for sync |
| Curious or compromised server operator | No server decryption key or plaintext semantic data |
| Analytics/crash vendor overcollection | Strict allowlist, redaction, no content SDK hooks |
| Malicious/corrupt import file | Local bounded parser, validation, no execution, transactional rollback |
| Tampered local/synced ciphertext | Authenticated encryption; fail closed without revealing content |
| Old device resurrecting deletion | Versioned tombstone/deletion protocol before V1 sync |
| Dependency or supply-chain compromise | Minimal dependencies, pinned review, data-flow inventory |
| Shoulder surfing/unlocked device | App lock option, sealed-entry reauthentication, rapid privacy exit |

No mobile app can protect plaintext from every compromise of an unlocked,
fully controlled device. Document residual risks honestly; do not compensate
with inflated marketing language.

## Data minimization

Collect and retain only what a defined product outcome requires.

- V0 can function without an account or production backend.
- Do not request Contacts, Calendar, Health, microphone, photo library, or
  location permissions in V0.
- Future media permissions are requested at the moment of explicit use, with a
  useful denial path.
- Do not derive or store mood, diagnosis, personality, relationship status, or
  named life-event categories.
- Do not persist rejected candidate lists or continuous behavioral trails.
- Coarsen dates and age into buckets for study telemetry where exact time is not
  necessary.

## Local storage protection

### Required layers

1. **Android app-internal storage** for the M1 database, sidecars, and
   Fio-owned temporary files; none may be placed in shared external storage.
2. **Authenticated app-level encryption boundary** for Entry content, semantic
   representations, revisions, and attachments.
3. **Android Keystore-backed key storage**, using a non-exportable key and no
   application-managed raw-key backup.
4. **Local authorization UI** for app lock and Sealed entries.

Use Android/JCA platform primitives. Do not implement cryptographic algorithms,
IV generation, key derivation, or authentication formats from scratch.

### Accepted M1 content-protection design

M1 uses the following bounded design:

- one non-exportable 256-bit AES content-encryption key generated and retained
  by Android Keystore;
- Entry and Draft private payloads sealed independently with
  `AES/GCM/NoPadding`;
- a fresh platform-generated 96-bit IV for every seal operation, stored only as
  part of the versioned envelope and never reused with the same key;
- authenticated associated data containing envelope version, record kind,
  opaque record ID, and schema version;
- a versioned sealed-box envelope owned by one Fio Crypto service;
- fail-closed handling for missing keys, malformed envelopes, and failed
  authentication.

Only this operational metadata may remain outside record ciphertext in M1:

- opaque Entry/Draft identifier;
- creation, original, update, deletion, and purge timestamps;
- original timezone/offset;
- content, envelope, and schema versions;
- source, access-policy, and Return-policy enum values.

The database and sidecar files remain private to the application sandbox and
must be excluded from Android backup and device-transfer extraction rules. Do
not add exposed metadata merely because it is operationally convenient.

### Accepted M1 recovery limitation

The Android Keystore key is not exported or copied by Fio. M1 excludes the
database, sidecars, private files, and key-related state from both legacy backup
and Android 12+ data-extraction/device-transfer rules. Clearing app data,
uninstalling, device reset, Keystore invalidation, or loss of the key can make
existing ciphertext unavailable and must produce an honest, non-destructive
failure state. M1 has no reinstall, cross-device restore, lost-key recovery,
key rotation, sync, or encrypted backup path.

Never generate a replacement key over existing data, reset the database, or
show an empty Archive as recovery. Human-readable export is the only M1
portability path. V1 key recovery, rotation, and multi-device bootstrap require
a new accepted design and migration plan.

Until the path is implemented, tested, and independently reviewed, user-facing
copy may say only that the entry is saved locally—not that a particular
encryption guarantee has been validated. M1 does not claim E2EE, zero knowledge,
secure deletion, Secure Enclave key storage, independent audit, or recovery.

## App lock and biometrics

App lock is an additional local access barrier. It is not encryption and must
not be described as such.

- Use AndroidX `BiometricPrompt`; M1 targets the stable `1.1.0` API unless the
  dependency-resolution gate records a newer stable compatible release.
- Privacy cover is enabled by default and contains no content preview.
- App lock is off until the user enables it; supported modes are immediate,
  one-minute, and five-minute where verified against Android lifecycle and
  process-recreation behavior on the supported API range.
- Use device-owner authentication so the platform may use biometrics or the
  device credential; Fio receives only the authorization result. The exact
  allowed authenticators must be tested across the minimum and target APIs.
- Provide a short PT-BR prompt that names the protected action without making a
  security claim.
- Re-entering from background after the grace boundary requires authorization.
- Sealed entries require authorization immediately before display, independent
  of the general app grace period if the accepted design says so.
- Do not create custom PIN recovery questions or store biometric information.
- Failed/cancelled authorization reveals no title, text, attachment, or preview.
- Do not assume one successful authorization approves later protected actions.
- When app lock is enabled, require fresh approved authorization before export
  and permanent deletion.
- Background tasks must not render decrypted content into snapshots.
- Apply `FLAG_SECURE` to content-bearing windows and place a neutral Compose
  privacy cover before the activity becomes non-visible. Verify both protections
  on physical devices because OEM Recents behavior can differ.

## Notification privacy

Allowed visible text:

> Algo seu voltou.

Payload rules:

- opaque local Return identifier only;
- no Entry ID meaningful outside the device, original date, text, title, topic,
  person, attachment, algorithm, or score;
- no remote push payload containing a decryption key;
- no repeated notification after the opportunity is ignored;
- no lock-screen rich preview or notification service extension that decrypts
  content.

Notification permission denial must not degrade writing, Archive access,
export, or deletion.

M2's Android notification adapter creates a low-importance, no-badge
`Devoluções` channel only after explicit Return consent. The visible payload is
the canonical sentence above; the intent carries only an opaque local Return
ID. WorkManager receives empty input/output/progress and no network constraint.
The merged release manifest has no `INTERNET`, storage, exact-alarm, or
foreground-service permission. M2 activation remains false in debug-primary and
release while the retained ADR-037 pre-pilot gates are open.

## App switcher, widgets, and extensions

- Cover the UI with a neutral Fio surface before Android Recents can capture a
  content-bearing state; keep `FLAG_SECURE` active on private surfaces.
- Do not ship widgets, Spotlight indexing, Siri suggestions, keyboard
  extensions, or share extensions that expose content in V0.
- Any future extension is a new trust boundary and requires its own data-access
  decision and tests.
- Do not place private content on the general pasteboard without explicit user
  action; use the narrowest platform protection available.

## Future end-to-end encrypted sync — V1 prerequisite

The desired property is:

```text
device encrypts → server stores opaque authenticated envelope → authorized device decrypts
```

The server must not possess keys that decrypt Entry text, embeddings, revisions,
attachments, search terms, or Return rationale.

Before sync is implemented, approve and test:

- account and device enrollment;
- new-device authorization;
- key backup/recovery or an explicit no-recovery posture;
- multi-device conflict resolution;
- revocation of a lost device;
- deletion propagation and tombstone expiry;
- rollback/replay protection;
- server metadata minimization;
- backup restore and disaster recovery without server plaintext;
- what happens when keys are lost.

TLS alone is transport security, not end-to-end encryption. Server-side database
encryption with server-held keys also does not meet this boundary.

## Embeddings and on-device models

Embeddings are private content-derived data.

- Generate and compare them on device.
- Encrypt persisted vectors and model-derived indexes.
- Never upload vectors to an analytics, model, vector database, or sync service
  as plaintext.
- Do not send journal text to a hosted model for embedding.
- Record model version locally so indexes can be invalidated/rebuilt.
- Deleting an Entry purges its vector and any cached candidate relationship.
- Model evaluation uses consented benchmark material, not production journals by
  default.

M4-R1 uses only purpose-written synthetic benchmark material. Scenario families
are separated between development and held-out partitions, annotation packets
contain closed response fields, and downloaded model artifacts plus human
responses stay outside version control. The research harness has no network
import and no path into Room, Returns, notifications, analytics, or application
source. Construction targets are not human semantic truth.

## Analytics and crash reporting

Telemetry is optional, minimal, and governed by
`09-ANALYTICS-EXPERIMENTS.md`.

Never collect:

- text, snippets, titles, drafts, search queries, file names, attachment data;
- embedding values, topic labels, sentiment, entities, people, places;
- exact original Entry dates when a bucket is sufficient;
- screen recordings, session replay, heatmaps, or keystrokes;
- arbitrary breadcrumbs containing UI labels or database values;
- advertising identifiers or cross-app tracking identifiers.

Implementation requirements:

- event constructors use closed schemas, not free-form dictionaries;
- a content guard rejects string fields not explicitly allowed;
- development logs use synthetic IDs/error codes and remain local;
- release logging defaults to the minimum necessary;
- crash reporting, if introduced, disables view capture and custom data by
  default and undergoes a data-flow review;
- analytics and crash vendors receive no decryption capability.

## Import security

- Parse locally in a staging directory protected as private data.
- Use size, count, nesting, and execution-time limits.
- Treat file content and metadata as data, never instructions or executable code.
- Normalize paths and reject archive traversal, symlinks outside staging,
  unexpected active content, and unsupported encodings safely.
- Preview counts/date ranges/errors before committing.
- Commit in one recoverable transaction; cancellation or failure leaves the
  canonical store unchanged.
- Remove temporary plaintext and source copies after the operation.
- Record parser version and minimal encrypted provenance.

M3 implements these controls only in the validation variant. It accepts one
SAF-selected `.txt`, `.md`, or `.markdown` UTF-8 document, never requests broad
storage access, and creates no temporary plaintext file. Limits are 5 MiB,
2,000 Entry blocks, 256 KiB per Entry, 20,000 lines, and five seconds of parser
work. Malformed UTF-8, container signatures, active HTML/script, control-heavy
input, missing/invalid dates, and unmatched explicit delimiters fail before a
canonical write. Exact SHA-256 deduplication is local; the content-derived
fingerprint and source filename are encrypted with record-specific AAD. Preview
is memory-only and is discarded on cancellation or process death.

## Export security

Export is always explicit and local.

- M1 offers one combined UTF-8 Markdown document or one combined UTF-8 plain-
  text document, with active Entries ordered oldest to newest.
- Include a format/version header and export date. For each Entry include opaque
  ID, original timestamp, known timezone/offset, and exact content inside a
  clear delimiter; do not add inferred metadata.
- Explain that exported plaintext is no longer protected by Fio once placed in
  another app/location.
- Require app authorization before exporting when app lock is enabled.
- Decrypt into the user-selected destination or a protected temporary location.
- Exclude deleted content by default; offer revisions/attachments only with
  clear choices when those features exist.
- Remove temporary files after success, cancellation, timeout, and failure.
- Use the Storage Access Framework with `ACTION_CREATE_DOCUMENT` and write
  directly to the user-selected content URI when possible. If a temporary file
  is unavoidable, keep it in app-internal cache, delete it on every exit path,
  and reconcile abandoned files on next launch.
- Do not upload an export to a Fio server merely to share it.
- Do not log, analyze, or retain a second export copy.

## Deletion

V0 supports Recently Deleted for 30 days and explicit immediate permanent
deletion.

Permanent purge removes:

- Entry ciphertext and drafts;
- revisions and attachments;
- embeddings, search indexes, thumbnails, and transcriptions;
- pending Return/notification references;
- import mappings and temporary files;
- future synchronized copies according to the V1 deletion protocol.

Flash storage and system backups can complicate physical byte erasure. Security
claims must describe actual platform behavior and the role of cryptographic key
destruction rather than promising impossible guarantees.

## Network policy

- V0 core works with networking disabled.
- Maintain an explicit allowlist of production endpoints and purposes.
- No third-party SDK may receive private content by default callbacks.
- Use TLS with current platform trust behavior; pinning requires a separate
  operational decision because it can create availability and rotation risks.
- Reject cleartext transport and unsafe fallback.
- Future authentication tokens, if introduced after a separate V1 decision,
  are protected with Android Keystore-backed storage and excluded from
  logs/backups as appropriate.

## Secure development requirements

Before a pilot or release:

- enable compiler/platform hardening defaults;
- run static analysis, dependency/license review, and secret scanning;
- inspect app entitlements and requested permissions;
- test locked-device, background, notification, screenshot, low-storage,
  corruption, tamper, import, export, and delete paths;
- use synthetic journal fixtures in automated tests and screenshots;
- verify release configuration contains no debug logging or test endpoint;
- document third-party data flows and retention;
- have an incident response and user-notification procedure;
- perform an independent security review before making strong cryptographic
  claims or launching multi-device sync.

## Incident posture

If a privacy/security defect is suspected:

1. preserve minimum technical evidence without copying user content;
2. disable the affected optional network/experiment path when possible;
3. determine which data classes, versions, and users may be affected;
4. rotate/revoke keys or credentials that can be safely rotated;
5. communicate facts, limitations, and user action plainly;
6. fix, test, and document the changed threat model;
7. never hide an incident to preserve the product's trust narrative.

## Release privacy checklist

- [ ] Core writing and Archive work offline.
- [ ] App switcher snapshot contains no private content.
- [ ] Notifications contain only canonical generic copy and opaque local ID.
- [ ] Logs and crash payloads contain no private content.
- [ ] Analytics event schemas reject unapproved/free-text fields.
- [ ] Stored Entry content and semantic data use the reviewed protection path.
- [ ] App lock and Sealed-entry claims match actual behavior.
- [ ] Import/export temporary plaintext is cleaned up on every exit path.
- [ ] Deletion removes derived data and cancels pending notifications.
- [ ] Network endpoints and third-party SDK data flows are documented.
- [ ] Security tests and migration tests pass on the current build.
- [ ] User-facing privacy copy makes no unverified guarantee.
