# Milestone 1 — Trusted Local Core

Status: Blocked — replaced by the Draft Android M1 plan
Owner: Project owner for authorization; Codex for preparation and future execution
Related milestone: `M1 — Trusted Local Core`
Relevant decisions: `ADR-002`, `ADR-004`–`ADR-006`, `ADR-010`, `ADR-013`,
`ADR-014`, `ADR-018`, `ADR-020`–`ADR-023`, `ADR-027`–`ADR-036`
Created: 2026-08-10
Last updated: 2026-08-10

## Platform transition notice

The project owner clarified on 2026-08-10 that the real target smartphone and
application platform are Android, not iOS. `ADR-033` accepts that correction.

This plan was approved against an iOS/SwiftUI/GRDB/CryptoKit design and is no
longer executable. Its product outcome, non-goals, acceptance intent, and
privacy invariants remain useful, but Apple-specific gates and implementation
steps are retained only as history. Do not generate an iOS or Android project
from this version.

Replacement technical decisions are Accepted in `ADR-034`–`ADR-036` and
Recommendation R2 in `2026-08-10-m1-android-pivot-decision-packet.md`. The
replacement is `2026-08-10-m1-android-trusted-local-core.md`, which remains
Draft until its complete Android toolchain/device matrix and `minSdk` are
approved. Obtain explicit approval of that plan before app generation.

## Outcome

When this milestone is complete, a person can open Fio on a supported iPhone,
write a plain-text entry, save it locally, close the app, reopen it without a
network connection, and recover exactly what they wrote. They can browse a
complete chronological Archive, edit an entry, move it to Recently Deleted,
recover it during the 30-day window, permanently delete it, and export active
entries in a durable human-readable form.

The experience is quiet and accessible:

- Write is the primary surface.
- Archive is complete but visually secondary.
- Save confirmation appears only after a successful local transaction.
- Draft and saved content survive normal app termination and offline use.
- Private content is covered in the app switcher.
- Optional app lock uses the platform authorization path.
- No analytics, account, server, sync, AI, notification, or Return behavior is
  introduced.

This milestone establishes a trustworthy local foundation. It does not claim
that Fio is end-to-end encrypted, zero-knowledge, independently audited, or
ready for production distribution.

## Definition of success

The milestone is successful only when current evidence demonstrates all of the
following:

1. **Fidelity** — saved, edited, recovered, and exported text is byte/Unicode
   equivalent to the user's committed text after documented newline
   normalization.
2. **Durability** — a confirmed save survives relaunch, offline use, and tested
   failure/recovery paths.
3. **No false success** — storage or encryption failure preserves the draft and
   never displays `Guardado.`.
4. **Portability** — active entries export locally in a documented UTF-8 format
   readable without Fio.
5. **Autonomy** — delete, recover, and immediate permanent purge behave as
   documented without retention friction.
6. **Privacy boundary** — plaintext journal content exists only in the trusted
   app process and explicit export destination; it does not enter logs,
   analytics, notifications, screenshots, or network traffic.
7. **Accessibility** — the supported device matrix passes Dynamic Type,
   VoiceOver, contrast, touch-target, keyboard, and Reduce Motion checks.
8. **Traceability** — an independent reviewer can follow content from editor to
   encryption, transaction, decryption, export, and deletion.

## Non-goals

The following work is outside Milestone 1 even when a nearby schema field or UI
placeholder might make it appear convenient:

- Return eligibility evaluation, selection, scheduling, notification, display,
  feedback, or history;
- Time, Resonance, Recurrence, Bridge, or any embedding/model work;
- TXT, Markdown, Evernote, Day One, PDF, or other import;
- search, semantic search, tags, folders, favorites, or Keep Nearby;
- Rest, Remember Later, or Sealed-entry behavior;
- Entry revision history beyond the current committed value;
- photos, audio, location, OCR, rich text, attachments, or collaboration;
- account creation, backend, remote push, backup, sync, telemetry, crash vendor,
  subscription, payments, or monetization;
- widgets, Spotlight indexing, Siri suggestions, share extensions, or other app
  extensions;
- Book, Legacy, social features, AI chat, psychological inference, habit or
  engagement mechanics;
- public release, App Store submission, deployment, signing automation, CI,
  Git hosting, pull request, commit, or push unless separately authorized.

The data model may reserve only the minimum fields required by the accepted
specifications. It must not build inactive tables/services for later milestones
“just in case.”

## Context read

The plan was prepared after reading the current repository state and these
sources in full:

- `AGENTS.md`
- `.agent/PLANS.md`
- `README.md`
- `CONTRIBUTING.md`
- `SECURITY.md`
- `docs/01-PRODUCT.md`
- `docs/02-PRINCIPLES.md`
- `docs/03-UX.md`
- `docs/04-FEATURES.md`
- `docs/05-ARCHITECTURE.md`
- `docs/06-DATA-MODEL.md`
- `docs/07-RETURNS-ENGINE.md`
- `docs/08-PRIVACY-SECURITY.md`
- `docs/09-ANALYTICS-EXPERIMENTS.md`
- `docs/10-ROADMAP.md`
- `docs/DECISIONS.md`
- `app/README.md`

Current repository evidence:

- `app/` contains only `README.md`; no Xcode project, Swift source, schema,
  dependency, implementation, or test exists.
- The current execution host is Windows (`Win32NT`) and does not provide
  `xcodebuild`.
- `Fio/` is not currently a nested Git repository.
- No application implementation or test result is available to reuse.

Consequently, SwiftUI compilation, simulator tests, UI tests, entitlements, and
device validation must run on an approved macOS host with Xcode. The current
Windows host can maintain documentation but cannot supply final iOS evidence.

## Assumptions and open questions

### Verified assumptions

- Fio is iOS-first, native SwiftUI, local-first, and offline-first.
- GRDB over system SQLite is the accepted local persistence layer, behind
  Fio-owned repository protocols.
- Plain text is the only Entry content format in Milestone 1.
- M1 uses Data Protection `.complete`, one `ThisDeviceOnly` Keychain content
  key, and versioned CryptoKit AES-GCM Entry/Draft records.
- M1 supports exactly one encrypted active Draft.
- Global Return consent defaults to `notConfigured`; M1 delivers no Return.
- Privacy cover is on by default; app lock is optional and off until enabled.
- App lock is an additional local authorization barrier, not encryption, and
  requires fresh authorization before export/permanent deletion when enabled.
- Recently Deleted retains an Entry for 30 days unless the user permanently
  deletes it sooner.
- Export is one combined UTF-8 Markdown or plain-text document, excludes deleted
  content, and never uploads to a Fio server.
- Real journal content cannot be used in fixtures, screenshots, logs, issues, or
  review evidence.
- Networking, analytics, crash reporting, imports, and Returns are absent from
  this milestone.

### Superseded gate A — Apple execution environment and platform baseline

Still required before app generation:

- macOS host and Xcode installation used for implementation and verification;
- minimum supported iOS version;
- Swift/Xcode version recorded for reproducible builds;
- simulator/device matrix, including the oldest supported configuration and at
  least one physical device before milestone completion.

Recommended approach:

1. Inspect the approved Mac's installed Xcode/SDK versions and available test
   devices.
2. Select the lowest iOS deployment target that supports the accepted security,
   accessibility, and SwiftUI behavior on the intended pilot devices.
3. Pin the selected Xcode and Swift versions in project documentation; do not
   choose them from this Windows host or from a “latest” assumption.

Blocking effect: do not generate the Xcode project, resolve GRDB, or claim build
evidence until this matrix is recorded.

#### Apple environment inspection record — 2026-08-10

Current-host evidence:

| Item | Observed result |
|---|---|
| Operating-system platform | `Win32NT` |
| Operating-system version | `Microsoft Windows NT 10.0.26200.0` |
| Process architecture | `X64` |
| `xcodebuild` | Not found |
| `xcrun` | Not found |
| `simctl` | Not found |
| `swift` | Not found |
| `system_profiler` | Not found |
| `instruments` | Not found |
| Present Apple PnP devices | `0` on this Windows host |

Inspection conclusion: this is not an Apple implementation environment. It
cannot establish an Xcode or Swift version, installed iOS SDK, deployment
target, simulator inventory, physical iPhone test matrix, signing behavior, or
GRDB/Xcode compatibility. The absence of a connected Apple PnP device here says
nothing about devices that may be available on a future Mac.

Historical gate state: **Superseded by ADR-033**. The Apple matrix is no longer
required and must not be completed. The evidence below is retained to show why
no iOS project was generated.

Evidence required on the approved Mac before closing this gate:

1. Record macOS version and hardware architecture.
2. Record `xcodebuild -version` and `xcrun swift --version` output.
3. Record the installed iPhoneOS SDK version and choose the minimum iOS target
   from actual pilot-device and accepted-API support.
4. Record available simulator runtimes/devices and identify the oldest and
   primary test configurations.
5. Record at least one intended physical iPhone model and installed iOS version
   before milestone completion; redact serial numbers and device identifiers.
6. Review the current official GRDB release/package requirements against that
   exact Xcode/Swift matrix, select one compatible version, and record its MIT
   license, Swift Package Manager resolution/checksum, transitive dependency
   inventory, update policy, and removal path.
7. Update the matrix below with command evidence before any project generation:

| Matrix field | Required value | Current state |
|---|---|---|
| macOS host/version/architecture | Actual approved Mac | Pending |
| Xcode | Exact installed version/build | Pending |
| Swift | Exact `xcrun` toolchain version | Pending |
| iPhoneOS SDK | Exact installed SDK | Pending |
| Minimum iOS deployment target | Evidence-based target | Pending |
| Primary simulator | Runtime and device model | Pending |
| Oldest supported simulator/device | Runtime and model | Pending |
| Physical iPhone | Model and iOS version, no identifier | Pending |
| GRDB | Exact compatible pinned version | Pending |
| GRDB review | License, resolution, dependencies, update/removal | Pending |

### Resolved gate B — execution-plan approval

The project owner explicitly approved this execution plan on 2026-08-10 after
M1-R1 established its controlling technical and product decisions.

This gate is resolved. Application generation remains blocked only by open gate
A: the recorded Apple toolchain and supported platform/device matrix.

### Resolved gates — M1-R1

The project owner approved M1-R1 without exceptions on 2026-08-10:

- persistence: GRDB over system SQLite (`ADR-022`);
- local content protection and no-recovery posture (`ADR-023`);
- explicit global Return consent (`ADR-027`);
- Apple validation policy (`ADR-028`);
- one encrypted active Draft (`ADR-029`);
- one-document Markdown/plain-text export (`ADR-030`);
- privacy-cover and app-lock defaults (`ADR-031`);
- separate Git/change-control authorization (`ADR-032`).

These decisions are incorporated into the canonical specifications. GRDB is
now Superseded by the Android pivot and was never installed. Platform-independent
M1-R1 outcomes remain in force where ADR-033 preserves them.

### Git and publication boundary

`Fio/` remains outside Git. M1-R1 explicitly leaves Git initialization, commit,
remote creation, push, CI, publication, and release as separate actions. They
are not blockers if explicitly left out of application generation, and no such
operation is authorized by this plan.

## Implementation sequence

**Blocked:** the sequence below was approved for iOS and must not be executed.
It will be replaced with an Android/Kotlin/Compose sequence only after M1-R2,
`ADR-034`–`ADR-036`, the Android matrix, and plan reapproval.

Each slice ends with reviewable evidence and leaves private data safe. No slice
starts while a decision that controls its design remains Proposed.

### 0. Approve the plan and establish the Apple build environment

1. Review this plan with the project owner.
2. Obtain explicit approval changing this plan from Draft to Approved.
3. On the approved Mac, record the Xcode, Swift, minimum-iOS, simulator,
   physical-device, and compatible pinned-GRDB matrix.
4. Record the GRDB version/resolution, MIT license, transitive dependency
   inventory, update policy, and removal path.
5. Confirm build/test commands and ensure no M1-R1 decision remains
   contradictory in schema version 1.
6. Leave Git initialization, commit, remote, push, publication, and release out
   unless each is separately authorized.

Evidence: approved plan status, Accepted ADRs, documented Xcode/SDK/device
matrix, and no unresolved contradiction for schema version 1.

### 1. Generate the minimal iOS project and test targets

Expected location: under `app/`. Proposed conventional products are the Fio app
target, unit-test target, and UI-test target; exact generated paths are recorded
after the approved Xcode generator/template is used.

1. Generate a native SwiftUI app with the approved deployment target.
2. Add unit and UI test targets.
3. Enable strict compiler warnings and platform hardening defaults supported by
   the selected toolchain.
4. Add no production package, network entitlement, account capability,
   analytics SDK, push notification, widget, or extension.
5. Define top-level boundaries for Domain, Application, Persistence, Crypto,
   and UI without implementing later milestones.
6. Add a build/readme file documenting exact local build and test commands.

Evidence: clean build and empty smoke tests on the selected simulator; project
file diff shows only approved targets/capabilities; entitlement/permission
inventory is recorded.

### 2. Implement pure domain types and application contracts

1. Define the minimal Milestone 1 Entry, Draft, AppSettings, deletion state, and
   export value types.
2. Use opaque locally generated UUIDs and injected clock/timezone sources.
3. Preserve `createdAt`, `originalCreatedAt`, `originalTimeZone`, `updatedAt`,
   content format, access policy, Return policy, `deletedAt`, `purgeAfter`, and
   schema version according to the approved consent decision.
4. Define repository and service protocols for save, load, list, edit, delete,
   recover, purge, settings, draft, key access, encryption, authorization, and
   export.
5. Define typed domain errors that carry no user content.
6. Implement pure use-case tests with in-memory fakes; do not let SwiftUI or
   SQLite types enter Domain.

Evidence: domain/use-case test suite passes without database, network, keychain,
SwiftUI, notifications, or real clock.

### 3. Implement schema version 1, repository, and crypto boundary

Prerequisites: this plan and the Apple environment gate are approved;
`ADR-022`, `ADR-023`, `ADR-027`, and `ADR-029` govern the implementation.

1. Create one versioned SQLite schema and migration ledger.
2. Store private payloads as authenticated ciphertext; keep only explicitly
   approved operational metadata outside the encrypted envelope.
3. Implement the selected key hierarchy and versioned encryption envelope.
4. Store encrypted active draft separately from committed Entry state.
5. Make save/edit/delete/recover/purge operations transactional and idempotent.
6. Apply Apple Data Protection to database, sidecars, and protected temporary
   locations.
7. Ensure database errors, tamper, key-unavailable, and insufficient-storage
   cases fail closed without discarding recoverable drafts.
8. Build migration fixtures from schema 0/empty and schema 1; define behavior
   for a database newer than the running app.

Evidence: repository, migration, crypto-vector, tamper, transaction rollback,
concurrency, low-storage simulation, and corrupted-database tests pass. An
independent reviewer traces editor plaintext → Crypto → ciphertext repository
and back.

### 4. Build the Write, autosave, and Saved experience

1. Implement the canonical prompt
   `O que está passando pela sua cabeça hoje?`.
2. Provide a readable plain-text editor with no title, tags, mood, category, or
   template.
3. Restore the encrypted active draft after normal relaunch.
4. Implement approved autosave lifecycle and explicit `Guardar`.
5. Display `Guardado.` only after the encrypted local transaction commits.
6. On failure, preserve the draft and show a quiet, factual next action.
7. Use no cursive body text, large lock, envelope, reward animation, or unproven
   security claim.
8. Keep Write as the default/primary surface.

Evidence: UI tests cover create, autosave, relaunch, save, forced failure,
Unicode/emoji/line breaks, blank input, rapid repeated action, Dynamic Type,
VoiceOver, keyboard, and Reduce Motion.

### 5. Build Archive, read, and edit

1. List active Entries newest first, grouped by local year/date derived from the
   preserved autobiographical timestamp.
2. Keep Archive discoverable but secondary; do not add ranking, feed, search,
   statistics, Return suggestions, or engagement counters.
3. Display original content and date after local decryption.
4. Implement edit as an atomic replacement of the current committed content;
   Entry revision history remains out of scope.
5. Decide preview behavior according to privacy settings; default to the most
   private approved option.
6. Preserve current text if edit fails and avoid false success.

Evidence: ordering tests across years/timezones, exact-content round trip,
large-entry scrolling, edit failure, empty Archive copy, accessibility order,
privacy-cover transition, and offline relaunch pass.

### 6. Build Recently Deleted, recovery, and purge

1. Moving an Entry to Recently Deleted sets `deletedAt` and `purgeAfter`, hides
   it from active Archive, and clears any M1 derived preview/cache.
2. Recovery before `purgeAfter` restores the same Entry identity, content, and
   autobiographical timestamp.
3. Permanent delete requires clear confirmation and, when app lock is enabled,
   approved authorization behavior.
4. Automatic purge occurs at the next safe local maintenance/app opportunity
   after the 30-day boundary; do not promise exact background execution.
5. Purge removes ciphertext, drafts tied to the Entry, and all M1 derivatives.
6. Document physical-erasure limitations and avoid claiming impossible flash or
   system-backup guarantees.

Evidence: delete/recover boundary tests, immediate purge, clock/timezone change,
idempotent repeated purge, app restart, authorization failure, and no-active-
Archive leakage pass.

### 7. Build explicit human-readable export

Governing decision: `ADR-030`.

1. Present export from Settings/Archive management, not as retention marketing.
2. Offer one combined Markdown document or one combined plain-text document.
3. Export active Entries only by default, preserving exact content and original
   date/timezone metadata.
4. If app lock is enabled, authorize before plaintext is created.
5. Use the approved protected temporary/destination flow.
6. Clean temporary plaintext after success, cancellation, failure, and next-
   launch reconciliation of abandoned temporaries.
7. Warn that the chosen destination is outside Fio protection.
8. Never upload, analyze, or log the export.

Evidence: export fixtures independently parse; every Entry/date/content matches;
deleted content is absent; filename collisions are safe; cancellation/failure/
crash cleanup tests pass; airplane-mode export succeeds.

### 8. Build privacy cover and optional app lock

Governing decision: `ADR-031`.

1. Cover content before iOS captures the background/app-switcher snapshot.
2. Implement LocalAuthentication with approved grace periods and platform
   fallback.
3. Reveal no content while locked or after failed/cancelled authorization.
4. Reauthorize export/permanent deletion according to the approved policy.
5. Keep encryption/key access semantics separate from UI lock state.
6. Request no unrelated permission or entitlement.

Evidence: immediate/one-minute/five-minute modes, app background/foreground,
device lock, biometric unavailable/failed/cancelled, passcode fallback, app
switcher screenshots, and cold launch are tested on simulator where possible and
physical device where required.

### 9. Integrate failure, accessibility, privacy, and performance evidence

1. Run the full unit, repository, migration, crypto, UI, and accessibility
   suite on the approved toolchain.
2. Test offline/airplane mode and confirm no core code initiates network traffic.
3. Inspect logs, screenshots, crash output, database files, exports, app
   entitlements, and package dependencies for private content/unapproved flows.
4. Test low storage, interrupted transaction, corrupt database, tampered
   ciphertext, key unavailable, app termination during save/export, and newer-
   schema refusal.
5. Measure launch, save, Archive load/scroll, memory, and export on the oldest
   supported device with synthetic small and large archives. Approve thresholds
   before declaring the milestone complete; do not fabricate a pass target after
   seeing results.
6. Run the M1 subset of `docs/08-PRIVACY-SECURITY.md` release checklist.
7. Perform an independent review of plaintext boundaries and user-facing claims.

Evidence: dated command results, test reports, synthetic screenshots, dependency
and entitlement inventory, measured performance report, review notes, and
documented residual risks.

### 10. Reconcile documentation and close the milestone

1. Update this plan's Progress log and Final report with current evidence.
2. Update `CHANGELOG.md`, `PROJECT-MANIFEST.md`, architecture, data model,
   privacy, UX, and Accepted ADRs only where implemented behavior/evidence
   requires it.
3. Record exact supported toolchain/device matrix and build/test commands.
4. Confirm every non-goal remains absent.
5. Confirm no release/publication action has been inferred.
6. Change plan status to Complete only after every exit criterion has evidence
   and no required work remains.

## Privacy and data impact

### New private data in Milestone 1

- active draft plaintext in editor memory and encrypted draft at rest;
- Entry text plaintext in trusted process and authenticated ciphertext at rest;
- original and operational timestamps/timezone metadata;
- deletion/recovery timestamps and policy metadata;
- app-lock and privacy-cover settings;
- Keychain key material and version metadata;
- explicit export plaintext in the destination selected by the user;
- protected temporary export plaintext only for the minimum necessary lifetime.

### Data not created

- embeddings, semantic scores, topics, sentiment, tags, search queries;
- Return, candidate, notification, feedback, experiment, or analytics records;
- account, device-sync, remote identifier, payment, advertising, or telemetry
  data;
- photo, audio, location, contact, calendar, health, or attachment data;
- revision history or imported source material.

### Storage

- One canonical SQLite repository owns schema and transactions.
- Private payloads pass through one reviewed authenticated encryption service.
- Drafts and Entries receive Apple Data Protection and app-level encryption.
- Key material lives in Keychain under the approved accessibility/backup policy.
- No UI module or feature creates a parallel plaintext store.

### Logging and diagnostics

- No Entry/draft/export text, filename, database value, key, path containing
  personal data, or free-form error enters logs.
- Use typed content-free error codes and synthetic identifiers in development.
- No crash-reporting SDK is introduced in M1.
- Release logging is reviewed separately; debug output is not proof of safety.

### Analytics and network

- No analytics events, analytics SDK, remote config, account, backend, sync,
  remote push, or network endpoint.
- The core passes in airplane mode.
- Any unexpected network capability or request is a milestone failure.

### Deletion

- Soft delete makes content unavailable in the active Archive immediately.
- Recovery is available until the 30-day boundary.
- Permanent purge removes all M1 copies and derived data controlled by Fio.
- Claims acknowledge platform backup/flash limitations; no impossible physical-
  erasure promise.

### Migration

- Schema version 1 is created only after consent and crypto decisions are
  Accepted.
- Every future schema change requires a forward migration and fixtures.
- A newer unsupported schema fails safely and never resets the database.
- No migration can infer Return consent.

### Permissions and entitlements

- No Contacts, Calendar, Health, Photos, microphone, location, push, background
  fetch, or network-specific capability in M1.
- LocalAuthentication/app-lock usage is the only expected sensitive capability,
  subject to platform configuration review.

## Tests and acceptance evidence

The implementation must turn each row below into an automated test where the
platform permits it and a documented device/manual check otherwise.

| ID | Area | Scenario | Required result |
|---|---|---|---|
| M1-F01 | Write | Create/save offline | Entry commits and `Guardado.` appears after commit |
| M1-F02 | Write | Relaunch with active draft | Exact draft is restored without creating an Entry |
| M1-F03 | Failure | Forced save/encryption failure | Draft remains; no false saved state |
| M1-F04 | Fidelity | Unicode, emoji, combining marks, long lines | Save/read/export preserves documented canonical content |
| M1-F05 | Idempotency | Rapid repeated save action | At most one intended Entry is created |
| M1-A01 | Archive | Multiple years/timezones | Deterministic newest-first grouping with original date preserved |
| M1-A02 | Edit | Successful edit | Current Entry updates atomically; no hidden revision feature |
| M1-A03 | Edit failure | Transaction fails | Prior committed text and edited draft remain recoverable |
| M1-D01 | Delete | Move to Recently Deleted | Entry disappears from active Archive immediately |
| M1-D02 | Recover | Before 30-day boundary | Same ID, content, and original timestamp return |
| M1-D03 | Purge | Immediate permanent delete | Fio-controlled content/derivatives are removed idempotently |
| M1-D04 | Retention | Cross 30-day boundary | Purge occurs on next safe opportunity, without exact-time claim |
| M1-E01 | Export | Active archive in airplane mode | Approved UTF-8 export completes and independently parses |
| M1-E02 | Export privacy | Deleted content exists | Deleted content is excluded by default |
| M1-E03 | Export failure | Cancel/fail/terminate | Temporary plaintext is removed/reconciled |
| M1-C01 | Crypto | Known vector/round trip | Reviewed envelope decrypts only with correct key/context |
| M1-C02 | Tamper | Ciphertext/tag/associated data changed | Fail closed; reveal no plaintext; preserve recoverable state |
| M1-C03 | Key unavailable | Keychain access fails | No reset or false empty Archive; factual recovery state |
| M1-M01 | Migration | Empty/schema fixture → v1 | Transactional migration succeeds with recorded version |
| M1-M02 | Migration failure | Interrupted/insufficient storage | Original database remains recoverable; no partial claim |
| M1-M03 | Newer schema | App opens unsupported future schema | Refuses safely; never recreates/overwrites data |
| M1-P01 | Privacy | Background/app switcher | Neutral cover contains no private content |
| M1-P02 | Privacy | Inspect logs/test reports | No text, key, export filename, or sensitive database value |
| M1-P03 | Network | Run core flows with traffic inspection | No unapproved request or endpoint |
| M1-L01 | App lock | Lock/grace/failure/fallback | Behavior matches approved policy; no preview leaks |
| M1-X01 | Accessibility | Largest Dynamic Type | Content/actions remain readable and reachable |
| M1-X02 | Accessibility | VoiceOver | Logical order, labels, and actions work without hidden gestures |
| M1-X03 | Accessibility | Reduce Motion | Non-essential transitions are removed |
| M1-X04 | Accessibility | Contrast/touch target | Supported platform checks pass |
| M1-R01 | Reliability | App killed during save | Reconcile to committed Entry or preserved draft, never ambiguity |
| M1-R02 | Reliability | Corrupt database | No destructive reset; recovery/export guidance is factual |
| M1-R03 | Reliability | Low storage | Existing content preserved; new save does not claim success |

### Milestone evidence packet

Before completion, collect:

- selected toolchain and supported device matrix;
- approved ADRs and threat-model decisions;
- dependency/license and entitlement inventory;
- clean build output;
- unit, repository, migration, crypto, UI, and accessibility test results;
- synthetic screenshots for Write, Saved, Archive, Recently Deleted, export,
  app lock, privacy cover, and errors;
- network inspection showing no M1 traffic;
- performance measurements on the oldest supported device;
- independent plaintext-boundary/security review;
- exact limitations and untested device/platform cases.

No item is marked Passed without current evidence from the approved Apple host.

## Rollback or disable path

### Before any real user data

- Each implementation slice can be removed or regenerated from source after
  review.
- Simulator/test databases use synthetic data and may be reset by explicit test
  setup only.
- A persistence or crypto spike is never reused as production storage by
  assumption.

### After a schema can hold user data

- Never roll back by deleting the app database, resetting Keychain, or silently
  creating an empty store.
- Every schema/encryption change is forward-migrated transactionally with a
  preserved recovery copy or stops before mutation.
- The app detects a newer unsupported schema and refuses safely.
- If a UI feature must be disabled, underlying Entries remain readable/exportable
  through a safe path.
- App lock can be turned off only through approved authorization; privacy cover
  remains independently configurable according to Accepted policy.
- Export provides the departure path before any knowingly destructive migration.

### Slice rollback

- **UI:** revert presentation while retaining repository/domain compatibility.
- **Persistence:** retain previous schema reader/migration fixture; do not
  downgrade destructively.
- **Crypto:** version envelopes and keep old decryption during migration until
  every record verifies; never rotate/delete old key material first.
- **Export:** disable the action if plaintext cleanup is unsafe; stored Entries
  remain unchanged.
- **App lock:** fail closed for protected actions while preserving data and a
  documented platform recovery path.

No experiment or remote kill switch exists in M1. Disabling is local and cannot
depend on a server.

## Risks and mitigations

| Risk | Consequence | Mitigation |
|---|---|---|
| Windows host cannot build iOS | False confidence or unverified code | Require macOS/Xcode gate and device evidence |
| Crypto implementation drifts from ADR-023 | Data loss or misleading claims | Enforce versioned boundary, tamper tests, and independent review |
| GRDB is added without toolchain/supply-chain evidence | Compatibility or maintenance risk | Pin on approved Mac and record license, resolution, inventory, and removal path |
| Autosave/save race | Duplicate or lost Entries | One draft lifecycle, transactional save, idempotency tests |
| Return consent inferred in schema | Future unsolicited Return without consent | Add explicit global not-configured state before schema v1 |
| Delete/purge overclaims | User believes bytes/backups are gone when not proven | Test Fio-controlled removal and document platform limits |
| Export leaves plaintext | Sensitive data exposed outside intended destination | Protected temp, cleanup ledger, failure/relaunch tests |
| App lock marketed as encryption | Security theater | Separate authorization/crypto copy and tests |
| Archive becomes a feed | Product drifts toward engagement | Chronological-only, secondary navigation, no ranking |
| Scope expands into M2/V1 | Unreviewed privacy and complexity | Enforce non-goals and feature statuses at every slice review |
| Synthetic fixtures are replaced with real notes | Privacy breach in repo/test artifacts | Fixture policy and review/secret scans |

## Approval checklist before app generation

- [x] Project owner corrected the target platform to Android (`ADR-033`).
- [x] Current Windows/Android environment inspection is recorded without
      generating an app.
- [ ] Project owner approves Recommendation M1-R2.
- [ ] Android stack/persistence decision (`ADR-034`) is Accepted.
- [ ] Android protection/auth/export decision (`ADR-035`) is Accepted.
- [ ] Android execution/device matrix decision (`ADR-036`) is Accepted.
- [ ] A functioning stable Android Studio/JDK/AGP/Gradle/Kotlin/Compose/Room
      matrix is recorded.
- [ ] The intended Android phone model/version is recorded without identifiers
      and the `minSdk` boundary is approved.
- [ ] Primary and oldest-supported stable AVDs are created and visible to ADB.
- [ ] This plan is rewritten for Android and explicitly reapproved.
- [x] Git initialization/hosting remains separately authorized or explicitly
      left out of implementation.
- [x] No M2/V1 non-goal has entered the transition.

Until this checklist is complete, do not generate the Xcode project or write
application code.

## Progress log

- 2026-08-10 — Draft plan created after reading all canonical specifications,
  repository instructions, contribution rules, and security policy. Confirmed
  that `app/` contains no implementation, the current host is Windows without
  `xcodebuild`, and `Fio/` is not a nested Git repository. Identified the four
  roadmap technical decisions plus a Return-consent schema conflict, draft
  lifecycle, export format, app-lock defaults, and execution-environment gates.
  No application, dependency, Git operation, or publication action performed.
- 2026-08-10 — Researched current primary Apple, SQLite, and GRDB documentation
  and created companion recommendation packet
  `2026-08-10-m1-decision-packet.md`. At that point the recommendations remained
  unapproved; no ADR status, dependency, application file, or Git state changed.
- 2026-08-10 — Project owner explicitly approved M1-R1 without exceptions.
  Accepted `ADR-022`, `ADR-023`, and `ADR-027`–`ADR-032`; aligned Architecture,
  Data Model, Returns Engine, Privacy/Security, UX, Features, and Roadmap. The
  plan remained Draft at that stage and the Apple toolchain/device matrix
  remained open. No app, dependency installation, Git operation, publication,
  or release occurred.
- 2026-08-10 — Project owner explicitly approved the complete Milestone 1 —
  Trusted Local Core execution plan. Changed plan status to Approved. The Apple
  toolchain/device matrix remains the sole pre-generation gate; no app,
  dependency installation, Git operation, publication, or release occurred.
- 2026-08-10 — Inspected the current execution host. Recorded Windows
  `Win32NT 10.0.26200.0` on x64; `xcodebuild`, `xcrun`, `simctl`, Swift,
  `system_profiler`, and Instruments are unavailable, with no present Apple PnP
  device detected. Marked the Apple environment gate open and blocked on this
  host. No Xcode/Swift/iOS/simulator/physical-device or GRDB version was guessed;
  no app, dependency, Git operation, publication, or release occurred.
- 2026-08-10 — Project owner clarified that the intended smartphone and app are
  Android. Accepted `ADR-033`, Superseded the iOS platform/persistence/crypto/
  environment decisions, and created Android Recommendation R2 with Proposed
  `ADR-034`–`ADR-036`. Inspected the partial Android environment: JDK 17.0.12,
  SDK platforms 36/37.0, Build Tools 35/36, ADB 37.0.1, emulator 37.1.11, no
  working Android Studio installation, no command-line tools, no AVD, and no
  connected device. Marked this iOS plan Blocked pending Android decisions,
  matrix, rewrite, and reapproval. The temporary ADB daemon was stopped after
  inspection. No app or dependency was generated.

## Final report

Not started. Complete this section only after implementation and verification.

The final report must include:

- approved ADRs and any superseded assumptions;
- exact Xcode/Swift/iOS/device matrix;
- generated and changed files/modules;
- schema and encrypted-envelope versions;
- dependencies, entitlements, permissions, and network inventory;
- tests and checks run with results;
- export, deletion, app-lock, accessibility, offline, and failure evidence;
- migrations and rollback behavior;
- security review findings and residual limitations;
- Git/release/publication state;
- confirmation that M2/V1 features remain absent.
