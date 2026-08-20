# Decisions

Status: Decision authority for specification v0.1

This is a lightweight architecture/product decision record. Accepted entries
override lower-level specifications when a genuine conflict exists. Proposed or
Deferred entries do not authorize implementation.

## Status meanings

- **Accepted** — current binding decision.
- **Proposed** — needs explicit human approval.
- **Deferred** — intentionally postponed; do not settle implicitly.
- **Superseded** — retained for history; a newer ADR governs.
- **Rejected** — considered and not adopted.

## ADR-001 — Use “Fio” as the working product name

- Date: 2026-08-10
- Status: Accepted
- Decision: Use **Fio** across the v0.1 specification and prototype.
- Reason: It suggests continuity, the thread of memory, losing and recovering a
  thread, without sounding like therapy, productivity, or technology.
- Consequence: Naming/legal availability remains a later validation; code and
  data should not make renaming unnecessarily expensive.

## ADR-002 — Define Fio as autobiographical continuity, not journaling habit

- Date: 2026-08-10
- Status: Accepted
- Decision: The primary outcome is re-encountering one's own earlier words after
  time, not maintaining a daily writing habit.
- Reason: This is the differentiated experience identified in the product
  analysis.
- Consequence: No streaks, goals, engagement loops, productivity features, or
  diary-completion pressure.

## ADR-003 — AI may select but must not interpret

- Date: 2026-08-10
- Status: Accepted
- Decision: Machine intelligence may retrieve, rank, cluster, or match Entries;
  it may not tell users what their life, emotions, or change mean.
- Reason: Interpretive authority belongs to the person; silence is part of the
  experience.
- Consequence: No AI chat, generated advice, summaries, diagnoses, sentiment
  narratives, or “why this returned” semantic explanation.

## ADR-004 — Keep a complete but secondary Archive

- Date: 2026-08-10
- Status: Accepted
- Decision: Users can access their complete chronological history, while Write
  remains the primary surface and Archive never becomes an algorithmic feed.
- Reason: Product silence must not remove control or access to personal history.
- Consequence: Archive is discoverable but visually secondary; no popularity,
  engagement ranking, or forced forgetting.

## ADR-005 — Start iOS-first, local-first, and offline-first

- Date: 2026-08-10
- Status: Superseded
- Decision: Initial client direction is native iOS with SwiftUI, SQLite behind a
  repository boundary, and local authority for core behavior.
- Reason: Platform-native privacy/notification behavior and on-device processing
  fit the product; networking must not block writing.
- Consequence: V0 does not require Android, web, or a production backend. GRDB
  over system SQLite is governed by ADR-022; the exact supported Apple
  toolchain/device baseline is recorded under ADR-028 before app generation.
- Superseded by: ADR-033. The local-first and offline-first invariants continue
  under the Android-first replacement.

## ADR-006 — No production server is required for the first proof

- Date: 2026-08-10
- Status: Accepted
- Decision: Milestones 1–3 can run entirely on device. A future server is limited
  to account/device operations and opaque authenticated ciphertext sync.
- Reason: The smallest complete loop is local, safer, and sufficient to test the
  product thesis.
- Consequence: No server-side plaintext, hosted embedding, vector database, or
  remote return scheduling. V1 sync needs a separate approved design.

## ADR-007 — Treat embeddings as private content

- Date: 2026-08-10
- Status: Accepted
- Decision: Generate and compare embeddings on device; encrypt them at rest and
  in any future sync envelope.
- Reason: Vectors carry semantic information even when not readable as prose.
- Consequence: No plaintext vector upload, hosted model call with journal text,
  server matching, or analytics scores.

## ADR-008 — Time is core; Semantic Resonance is removable

- Date: 2026-08-10
- Status: Accepted
- Decision: Time-based return is the complete baseline. Semantic Resonance is an
  experiment behind a feature flag and adapter.
- Reason: Semantic complexity must demonstrate a material advantage beyond the
  effect of time itself.
- Consequence: Removing model code cannot break writing, Archive, or Time
  returns. Time and Resonance comparisons are age-matched.

## ADR-009 — Exclude Contrast from V0

- Date: 2026-08-10
- Status: Accepted
- Decision: Do not implement a “Contrast” algorithm that labels before/after or
  emotional change.
- Reason: It encourages sentiment and psychological interpretation that the
  product does not need.
- Consequence: A person may notice contrast in related entries, but Fio does not
  classify or announce it.

## ADR-010 — Explicit return controls outrank algorithms

- Date: 2026-08-10
- Status: Accepted
- Decision: Support Never Return and global pause in V0; add Rest and one-time
  Remember Later in V1; keep Seal independent from return policy.
- Reason: Unexpected resurfacing can be powerful or unwanted, and autonomy is a
  safety property.
- Consequence: Policy changes cancel incompatible pending Returns. No control
  requires an explanation or retention flow.

## ADR-011 — One generic notification, no reminder

- Date: 2026-08-10
- Status: Accepted
- Decision: Canonical notification is `Algo seu voltou.` with no content preview;
  an ignored notification is not repeated.
- Reason: Private content must not leak and Returns must not become obligations.
- Consequence: No teaser, exact age, topic, red badge, remote content push, or
  “still waiting” follow-up.

## ADR-012 — Keep pilot feedback outside the Return screen

- Date: 2026-08-10
- Status: Accepted
- Decision: In research builds, ask `Isso significou algo para você?` only in a
  separate, skippable sheet after the Return closes.
- Reason: Measurement should not narrate or contaminate the encounter it is
  trying to measure.
- Consequence: Production can remove the sheet completely; feedback never gates
  controls or dismissal.

## ADR-013 — No cursive journal body text

- Date: 2026-08-10
- Status: Accepted
- Decision: Use a highly readable body/editor typeface; reserve expressive type
  for identity or short headings.
- Reason: Decorative typography competes with long and emotionally sensitive
  user content.
- Consequence: Dynamic Type/readability tests govern journal text even when a
  visual mockup suggests cursive.

## ADR-014 — Remove the large lock/save animation

- Date: 2026-08-10
- Status: Accepted
- Decision: Saving uses immediate, restrained confirmation with no large lock or
  cinematic envelope/trapping animation.
- Reason: A reward ritual attracts attention to the app and can turn security
  into theater.
- Consequence: `Guardado.` and a small check are sufficient; security claims
  must come from architecture and evidence.

## ADR-015 — Import is part of V0 proof, but bounded

- Date: 2026-08-10
- Status: Accepted
- Decision: Implement safe TXT/Markdown import for the Time-only pilot; add at
  most one source-specific importer based on participant evidence.
- Reason: Existing histories reduce the bootstrap wait and allow real temporal
  testing without building a migration platform.
- Consequence: Import requires staging, preview, original-date preservation,
  deduplication, transactional commit, cleanup, and rollback.

## ADR-016 — Pilot compares Time and Resonance within participant

- Date: 2026-08-10
- Status: Accepted
- Decision: Use a counterbalanced within-subject design with age-matched Time
  control and identical visible UX.
- Reason: A small between-group pilot would be dominated by individual
  differences and Entry age.
- Consequence: Assignment is fixed independently of opens/content; telemetry
  records assigned and delivered algorithms without Entry identity.

## ADR-017 — Analytics is content-free and closed-schema

- Date: 2026-08-10
- Status: Accepted
- Decision: Pilot telemetry may collect only consented, allowlisted product
  events/properties defined in `09-ANALYTICS-EXPERIMENTS.md`.
- Reason: Fio can measure return outcomes without observing journal content.
- Consequence: No text, query, filename, vector, score, inferred label, replay,
  heatmap, keystroke, advertising identifier, or arbitrary metadata field.

## ADR-018 — Users can export and permanently delete

- Date: 2026-08-10
- Status: Accepted
- Decision: Provide human-readable export and a 30-day Recently Deleted window
  with an explicit immediate permanent-delete option.
- Reason: Trust includes portability and the right to leave or destroy data.
- Consequence: Export is not hidden behind retention. Purge includes derived
  indexes, pending notifications, revisions/attachments when they exist, and
  future synchronized copies under the V1 protocol.

## ADR-019 — Freeze Book and Legacy

- Date: 2026-08-10
- Status: Accepted
- Decision: Retain Book and Legacy/“after me” as Frozen concepts only.
- Reason: They introduce separate product, legal, identity, key-management,
  consent, and emotional-safety systems before the core thesis is proven.
- Consequence: Do not design schemas, flows, APIs, or implementation for them.

## ADR-020 — Product philosophy cannot change silently

- Date: 2026-08-10
- Status: Accepted
- Decision: A change that conflicts with an Accepted decision or Principle must
  stop, name the conflict, and seek explicit human approval for a new/superseding
  ADR.
- Reason: Codex should not infer authority to trade away durable product choices.
- Consequence: Plans and code cannot hide product decisions. Accepted ADRs have
  highest project-document precedence.

## ADR-021 — Use execution plans for cross-cutting work

- Date: 2026-08-10
- Status: Accepted
- Decision: Work involving multiple modules, stored-data migrations, privacy,
  cryptography, returns, external dependencies, or extended implementation uses
  a plan based on `.agent/PLANS.md`.
- Reason: Long work needs explicit scope, evidence, risks, tests, and rollback.
- Consequence: A plan is required but does not itself authorize Experimental,
  Research, Frozen, or Rejected scope.

## ADR-022 — Select the local persistence layer

- Date: 2026-08-10
- Status: Superseded
- Decision: Use GRDB through Swift Package Manager over the SQLite library
  shipped by iOS. Pin one reviewed GRDB release compatible with the recorded
  Apple toolchain when the project is generated. Domain and Application depend
  only on Fio-owned repository protocols; GRDB and raw SQL remain inside
  Persistence. Multi-step state changes use explicit transactions.
- Reason: Milestone 1 needs reliable migrations, transactions, concurrency, and
  fixture testing while preserving direct SQLite control. The repository
  boundary contains dependency lock-in.
- Consequence: GRDB is the one approved M1 production dependency. Do not add
  GRDBQuery, SQLCipher, full-text search, database observation, or companion
  features without a separate decision. Record the selected version, resolved
  checksum, MIT license, transitive dependencies, update policy, and removal
  path on the approved Mac/Xcode host.
- Superseded by: ADR-033 for the platform pivot and Accepted ADR-034 for Android
  persistence.

## ADR-023 — Approve the local cryptographic record design

- Date: 2026-08-10
- Status: Superseded
- Decision: In M1, protect the database and Fio-owned temporary files with
  Apple Data Protection `.complete`. Generate one random 256-bit content key
  with CryptoKit, store it in Keychain as
  `kSecAttrAccessibleWhenUnlockedThisDeviceOnly`, and encrypt each Entry and
  Draft private payload independently with AES-GCM and a fresh CryptoKit nonce.
  Authenticate envelope version, record kind, opaque record ID, and schema
  version as associated data. One Fio-owned Crypto service owns key access,
  sealing/opening, envelope parsing, and error mapping.
- Reason: This uses platform primitives, keeps record tampering detectable, and
  matches M1's foreground-only local scope without inventing cryptography.
- Consequence: The following operational fields may remain outside record
  ciphertext, while still protected by the database file: opaque ID,
  timestamps and original timezone/offset, content/envelope/schema version,
  source enum, access and Return-policy enums, `deletedAt`, and `purgeAfter`.
  Missing keys, malformed envelopes, and failed authentication fail closed and
  never trigger a reset, replacement key over existing data, or false empty
  Archive. The key does not migrate to another device; M1 has no reinstall,
  cross-device, or lost-key recovery. Export is its only portability path. Do
  not claim E2EE, zero knowledge, secure deletion, Secure Enclave key storage,
  independent audit, or recoverability not actually implemented.
- Superseded by: ADR-033 for Apple-specific primitives and Accepted ADR-035 for
  Android protection.

## ADR-024 — Select an on-device embedding model

- Date: 2026-08-10
- Status: Deferred
- Decision needed: Choose only after the PT-BR benchmark, device measurements,
  license review, and held-out evaluation in Milestone 4.
- Blocking: Milestone 5 Resonance pilot, not Time-based product work.
- Constraint: No hosted journal processing or plaintext/vector upload.

## ADR-025 — Design V1 sync, recovery, and deletion propagation

- Date: 2026-08-10
- Status: Deferred
- Decision needed: Backend, authentication, device enrollment/revocation, key
  recovery, conflict policy, replay protection, backup, deletion propagation,
  metadata, and lost-key behavior.
- Blocking: V1 multi-device sync and any E2EE/zero-knowledge claim.
- Constraint: Server cannot decrypt content or embeddings.

## ADR-026 — Choose monetization

- Date: 2026-08-10
- Status: Deferred
- Decision needed: Evaluate simple annual subscription, purchase, or another
  trust-compatible model after core proof and cost evidence.
- Blocking: Commercial launch, not V0 product research.
- Constraint: No advertising, personal-data sale, aggressive freemium pressure,
  or export hostage.

## ADR-027 — Require explicit global Return consent

- Date: 2026-08-10
- Status: Accepted
- Decision: Store `returnConsentState` in `AppSettings` as `notConfigured`,
  `enabled`, or `paused`. Every M1 installation and migration defaults to
  `notConfigured`. Per-entry `returnMode = eligible` is subordinate and cannot
  authorize delivery until explicit M2 onboarding changes the global state to
  `enabled`. `paused` is available only after Returns were configured.
- Reason: Creating future policy fields in M1 must not silently opt a person's
  existing words into unsolicited Returns.
- Consequence: M1 schedules and displays no Return. M1-to-M2 migration cannot
  infer consent. The Returns engine must require `enabled`; pause and resume are
  explicit state transitions, and policy tests cover all three states.

## ADR-028 — Validate iOS work only on a recorded Apple toolchain

- Date: 2026-08-10
- Status: Superseded
- Decision: Generate the app, resolve dependencies, compile, sign, and run
  simulator/device tests only on an approved macOS host with a recorded Xcode,
  Swift, minimum-iOS, simulator, and physical-device matrix selected before
  generation.
- Reason: The current Windows host cannot compile or validate SwiftUI/iOS
  behavior, and guessed “latest” versions would create false evidence.
- Consequence: Documentation may advance on Windows, but no build or test is
  called passed without evidence from the recorded Apple environment. The
  exact platform baseline remains a pre-generation gate, not an implicit code
  choice.
- Superseded by: ADR-033; the Android environment/device gate is Proposed in
  ADR-036.

## ADR-029 — Keep exactly one encrypted active draft in M1

- Date: 2026-08-10
- Status: Accepted
- Decision: M1 maintains at most one local active draft. Meaningful text changes
  autosave after a debounce through the same authenticated-encryption path as
  Entries. Blank drafts are not persisted. `Guardar` creates an Entry
  transactionally and removes the draft only after commit succeeds.
- Reason: One draft prevents accidental loss without creating hidden history or
  a second Archive.
- Consequence: Save, encryption, or database failure preserves the draft and
  never displays `Guardado.`. Explicitly clearing the editor and confirming
  departure removes it. Draft history, multiple drafts, sync, and draft
  analytics are outside M1.

## ADR-030 — Export one explicit UTF-8 document in M1

- Date: 2026-08-10
- Status: Accepted
- Decision: Export active Entries, oldest to newest, as either one combined
  Markdown document or one combined plain-text document. The versioned export
  includes an export date and, for each Entry, opaque ID, original timestamp,
  known timezone/offset, and exact user content in a clear delimiter. Use the
  system file-export flow and an in-memory `FileDocument` snapshot where the
  selected platform supports it.
- Reason: A single readable document is portable and avoids a package/ZIP
  dependency before evidence requires a richer archival format.
- Consequence: Use UTF-8 with documented LF normalization; add no inferred
  title, topic, summary, sentiment, or label. Warn that the destination is
  outside Fio protection. If a temporary plaintext file is unavoidable, apply
  `.complete` protection and clean it on success, cancellation, failure, and
  next-launch reconciliation. Never upload, log, analyze, or retain another
  export copy. When app lock is enabled, authorize before creating plaintext.

## ADR-031 — Default to privacy cover and optional app lock

- Date: 2026-08-10
- Status: Accepted
- Decision: Enable the neutral privacy cover by default. App lock is optional
  and off until enabled, with immediate, one-minute, and five-minute modes
  subject to verified iOS lifecycle behavior. Use LocalAuthentication device-
  owner authentication so the platform may use biometrics or device passcode.
- Reason: The cover prevents casual snapshot exposure without setup; optional
  platform authorization adds a local barrier without pretending to be
  encryption or collecting biometric data.
- Consequence: Provide the localized Face ID usage description when applicable,
  reveal no Entry content while locked or after cancellation/failure, and do
  not treat one success as indefinite authorization. When app lock is enabled,
  require fresh approved authorization before export and permanent deletion.
  App lock remains separate from Keychain accessibility and record encryption.

## ADR-032 — Keep Git initialization and publication separately authorized

- Date: 2026-08-10
- Status: Accepted
- Decision: Do not infer Git initialization, commit, remote creation, push, or
  publication from product planning or app generation. A future local Git
  initialization requires explicit authorization and creates no remote by
  default; any commit is another separately authorized operation with explicit
  path staging.
- Reason: Version history is appropriate, but repository creation and external
  publication change project state and have distinct review boundaries.
- Consequence: M1 documentation and later app generation may proceed without
  Git. Release ZIPs remain ignored by the current package policy unless a later
  artifact decision says otherwise.

## ADR-033 — Pivot the initial client from iOS to Android

- Date: 2026-08-10
- Status: Accepted
- Decision: Build the initial Fio client as a native Android application for the
  project owner's Android smartphone. Preserve local-first, offline-first, no-
  server M1 behavior and the platform-independent product boundaries.
- Reason: The project owner explicitly corrected the target device/platform
  before any application scaffold or user-data schema was created.
- Consequence: Do not generate or validate an iOS/SwiftUI project. Apple-
  specific choices in ADR-005, ADR-022, ADR-023, and ADR-028 are Superseded.
  The `FileDocument`/Data Protection details in ADR-030 and the
  LocalAuthentication/Face ID/Keychain details in ADR-031 are non-binding;
  their platform-independent export, privacy-cover, app-lock, and fresh-
  authorization outcomes remain Accepted. Android UI/build, persistence,
  cryptography/authentication, backup, export mechanism, and environment/device
  matrix require ADR-034–ADR-036 before a project is generated.
- Supersedes: ADR-005, ADR-022, ADR-023, ADR-028, and the Apple-specific
  implementation portions of ADR-030 and ADR-031.

## ADR-034 — Select the native Android stack and persistence layer

- Date: 2026-08-10
- Status: Accepted
- Decision: Use a native Android application with Kotlin, Jetpack Compose,
  Gradle Kotlin DSL, AndroidX Room over SQLite, KSP, and structured coroutines.
  UI, Domain, and Application depend on Fio-owned interfaces/models; Room
  entities, DAOs, SQL, and migrations remain inside Persistence. Export Room
  schema JSON and test every supported migration with `room-testing`.
- Reason: This is the official native Android path, preserves compile-time SQL
  checks and migration tooling, and contains platform dependencies behind Fio's
  boundaries.
- Consequence: Do not use Flutter, React Native, KMP, KAPT, direct-SQLite as a
  second store, Paging, FTS, SQLCipher, or destructive migration fallback in
  M1. Exact stable versions are pinned by ADR-036 before generation. Room
  `2.8.4`, KSP `2.3.9`, and Compose BOM `2026.06.00` are the approved stable
  candidates subject to completed local resolution evidence.

## ADR-035 — Approve Android local protection, app lock, backup, and export

- Date: 2026-08-10
- Status: Accepted
- Decision: Keep private files in app-internal storage. Generate a non-exportable
  256-bit AES key in Android Keystore, authorized only for
  `AES/GCM/NoPadding`, and encrypt each Entry/Draft payload independently with a
  fresh platform-generated IV and the accepted version/record/ID/schema AAD.
  Exclude Fio data from cloud backup and device transfer through legacy and
  Android 12+ rules. Use `FLAG_SECURE` plus a neutral lifecycle cover, AndroidX
  BiometricPrompt with strong biometric/device credential for optional app lock,
  and Storage Access Framework `ACTION_CREATE_DOCUMENT` for explicit export.
- Reason: These are Android platform boundaries for private local data,
  authenticated encryption, system authorization, Recents/screenshot
  protection, and user-controlled document creation.
- Consequence: App lock remains separate from the content key. Missing key,
  invalid envelope, or authentication failure fails closed without reset or a
  false empty Archive. Uninstall/app-data clearing/key loss has no M1 recovery;
  export is the portability path. Do not claim StrongBox/hardware backing
  without device evidence, E2EE, zero knowledge, secure deletion, audit, or
  recovery. `FLAG_SECURE` blocks screenshots/casting on protected Fio windows.
- Implementation evidence: The 2026-08-11 M1 checkpoint implements the
  versioned AES-GCM envelope, Keystore key provider, backup exclusions,
  `FLAG_SECURE`, neutral cover, BiometricPrompt boundary, and SAF export. API
  26/API 36 ten-test instrumented suites and the seven-test POCO subset pass,
  including missing-key refusal, rollback, and corrupted-envelope preservation;
  open manual gates and claim limitations remain recorded in the Approved M1
  plan.

## ADR-036 — Record and validate the Android execution/device matrix

- Date: 2026-08-10
- Status: Accepted
- Decision: Before project generation, record a functioning stable Android
  Studio and command-line toolchain; intended physical phone model/Android/API;
  broad `minSdk`, stable `compileSdk`/`targetSdk`; primary and oldest-supported
  AVDs; and exact JDK, AGP, Gradle, Compose BOM, Room, KSP, Biometric, and test
  versions. The owner's POCO M3 Pro 5G is the primary physical device, not the
  only supported model or automatic minimum boundary.
- Reason: Android compatibility spans vendors, OS levels, hardware, lifecycle,
  biometrics, and storage behavior; one connected phone cannot prove the product
  matrix.
- Consequence: Android Studio Quail 3 `2026.1.3` is the recorded stable IDE.
  Leading stable build candidates are JDK toolchain 17, AGP `9.3.1`, Gradle
  `9.5.0`, Build Tools `36.0.0`, `compileSdk = 36`, and `targetSdk = 36`.
  `minSdk`, phone Android/API, command-line tools, stable system images/AVDs,
  dependency resolution, and test evidence remain open. Do not use API 37/
  preview APIs accidentally, expose device identifiers, or generate
  the app until the matrix and rewritten plan are complete and reapproved.
- Validation evidence: On 2026-08-10, stable command-line tools 22.0 and
  platform-tools 37.0.1 were validated; the authorized POCO ran Android 13/API
  33 on ARM64; API 26 and API 36 AVDs completed clean boots. The exact matrix is
  recorded in the Android M1 plan. The owner then explicitly approved `minSdk
  26`, application ID `com.projetofio.app`, and the complete Android M1 plan.
  All pre-generation gates are closed.
- Implementation evidence: The generated project retains the approved matrix;
  debug/release/lint/offline checks and 20 unit tests pass, the full ten-test
  instrumented suite passes on API 26 and API 36, and seven safe non-background
  tests pass on the POCO API 33 plus explicit cold launch with `FLAG_SECURE`.
  This evidence does not imply all Android models, another OEM, signing,
  publication, or M1 acceptance.

## ADR-037 — Separate M1 engineering acceptance from external field validation

- Date: 2026-08-12
- Status: Accepted
- Decision: Accept the implemented M1 local core as an **engineering baseline**
  for continued synthetic-data development after its complete automated API
  26/API 36 matrix and bounded POCO validation. The remaining owner-observed
  accessibility/authentication/Recents checks, Android 8 credential interaction,
  second-OEM coverage, real third-party-provider failure, and independent
  plaintext-boundary sign-off remain mandatory **pre-pilot validation gates**.
  They no longer block implementation and automated testing of M2–M4, but they
  must close before any participant pilot, production release, security/audit
  claim, or activation of M2+ behavior with non-synthetic journal data.
- Reason: The implementing agent and available emulator/POCO environment have
  exhausted the deterministic evidence they can produce. Treating unavailable
  people or hardware as if tested would be false; indefinitely blocking isolated
  engineering work would not improve that evidence. A staged gate preserves
  the limitation while allowing reversible, local, synthetic development.
- Consequence: M2 engineering may begin under a milestone-specific approved
  plan. M3 import implementation and M4 research tooling may also be built and
  tested with synthetic fixtures when their normal prerequisites pass, but the
  M3 Time-only pilot cannot start until the pre-pilot gates close. M5 still
  requires real M3 pilot evidence, an approved M4 model decision, and an
  approved study plan; M6 still requires real consented 60–90 day pilot data.
  No second-OEM, accessibility, independent-review, audit, broad compatibility,
  signing, publication, or release claim is created by this decision. Open
  evidence must remain visible in plans and reports.

## ADR-038 — Use bounded Android scheduling and validation-only M2 activation

- Date: 2026-08-13
- Status: Accepted
- Decision: Use AndroidX WorkManager `2.11.2` through
  `work-runtime-ktx` as M2's only new production dependency, behind a Fio-owned
  scheduling interface. Schedule one uniquely named, replaceable, one-time
  local Return opportunity; do not use exact alarms, periodic alarms, remote
  push, foreground services, or network constraints. Create one low-importance,
  no-badge `Lembranças do Fio` notification channel using only `Algo seu voltou.` and
  an opaque local Return ID. On API 33+, request `POST_NOTIFICATIONS` only after
  explicit Return consent. Default quiet hours are 21:00–08:00 local time.
  Migrate Room schema 1→2 non-destructively. Until ADR-037 pre-pilot gates
  close, M2 activation is available only in the isolated validation build and
  deterministic tests using synthetic data.
- Reason: WorkManager is the official persistent deferrable-work mechanism for
  the supported Android range and matches Fio's opportunistic, non-exact
  delivery model. Unique one-time work is quieter and easier to reconcile than
  periodic work. Validation-only activation allows evidence-driven engineering
  without involving the owner's primary journal data or pretending that open
  field gates have passed.
- Consequence: WorkManager input/progress/output, PendingIntent, and notification
  payloads contain no Entry text, date, meaningful Entry identifier, algorithm
  rationale, or score. No Internet, exact-alarm, foreground-service, analytics,
  account, or remote-push capability is added. Permission denial or dismissal
  causes no nag, badge, repeat prompt, or loss of core functions. Existing M1
  installations remain `notConfigured`; migration schedules nothing. The
  dependency graph, hashes, licenses, offline build, Room migration, API 26/API
  36 behavior, and removal path require recorded evidence. This decision does
  not authorize a participant pilot, primary/release activation, real-content
  testing, Git, signing, publication, release, import, or semantic work.

## ADR-039 — Bound Android M3 import to explicit local formats and atomic rollback

- Date: 2026-08-13
- Status: Accepted
- Decision: Import one UTF-8 TXT or Markdown document selected through the
  Storage Access Framework. Accept lossless Fio v1 export blocks and a
  conservative explicit Entry-block format; preserve explicit dates/timezones,
  enforce 5 MiB/2,000 Entry/256 KiB/20,000 line/five-second limits, and reject
  malformed encoding, active content, archives, and implicit date inference.
  Deduplicate only exact local SHA-256 fingerprints. Migrate Room `2 → 3` with
  encrypted minimal provenance and atomic batch/item records. Rollback
  tombstones only unchanged Entries created by that batch. M3 activation is
  immutable and true only in the isolated validation variant.
- Reason: Existing history is necessary for the Time-only proof, but guessing
  structure/dates, fuzzy deduplication, partial commits, or broad file support
  can silently change or lose autobiographical material. Exact, bounded,
  previewed local parsing is explainable and reversible.
- Consequence: Cancellation or failure writes no canonical Entry; edited
  imported Entries survive batch rollback; native/unrelated Entries are never
  touched. No archive/media/HTML/source-specific importer, network, analytics,
  AI interpretation, primary/release activation, personal-file test,
  participant pilot, Git, signing, publication, or release is authorized.

## ADR-040 — Isolate M4 as offline synthetic benchmark research

- Date: 2026-08-13
- Status: Accepted
- Decision: Implement M4 only as a removable research boundary using about 300
  purpose-written synthetic PT-BR pairs, a frozen held-out family split, a
  deterministic non-semantic lexical control, Fio-owned model adapters, closed
  quality/error/resource measures, and current license/provenance/hash records.
  Final model selection requires two independent human ratings per held-out
  pair and explicit approval of the corpus/rubric boundary. Candidate model
  text, embeddings, scores, and artifacts remain offline and outside Room and
  production Returns.
- Reason: A general leaderboard or model reputation cannot establish whether
  semantic similarity is useful for restrained PT-BR autobiographical returns.
  Synthetic tooling can make the evaluation reproducible without exposing a
  journal, while human disagreement and real device measurements cannot be
  fabricated by automation.
- Consequence: M4 may create synthetic corpus/rubric tooling, an offline
  benchmark harness, candidate inventory, and reviewed research-only adapters.
  It may not use personal/participant content, hosted inference, analytics,
  production dependencies, semantic indexes in the app database, Return
  integration, or user-visible AI behavior. ADR-024 remains Deferred until
  held-out quality, license, provenance, Android compatibility, and physical
  device evidence support an explicit later decision. M5/M6, Git, signing,
  publication, and release remain unauthorized.

## ADR-041 — End the V0 semantic path with no model selected

- Date: 2026-08-13
- Status: Accepted
- Decision: Treat the failed MiniLM development safety/resource gate as a valid
  early-stop outcome for M4 and select no embedding model for V0. Do not
  fabricate two human annotators or use Codex ratings as human gold. Retain
  Time Returns as the complete V0 mechanism, defer the M5 Resonance pilot, and
  record the M6 semantic decision as **Remove / do not introduce**.
- Reason: The project currently has one human owner. MiniLM produced unsafe
  top-1 rate `1.0`, lower mean nDCG@5 than the lexical control, an approximately
  118 MB model, and about 521 MB extra Windows working set before any Android
  integration. Human disagreement cannot be manufactured by automation, and a
  weak candidate does not justify recruiting annotators or increasing product
  complexity. The owner requested that Codex automate what can be automated and
  that the owner test the finished application.
- Consequence: The frozen corpus, rubric, packets, and research evidence remain
  available, but held-out evaluation and human annotation are not required to
  reject the current candidate. ADR-024 remains Deferred; no semantic model,
  index, dependency, participant study, or M5 product code is added. Reopening
  semantic selection later requires a new approved decision, a materially
  better candidate, and the two-independent-human/physical-device evidence in
  ADR-040. This decision does not waive outstanding accessibility, physical-
  device, provider, independent-review, signing, publication, or release gates.

## ADR-042 — Preserve the complete long-term time and Legacy vision

- Date: 2026-08-13
- Status: Accepted
- Decision: Make `11-FOUNDER-VISION.md` the durable record of the founder's
  long-term direction. Preserve the intent to reach the coherent Fio capability
  family over time: free writing and Archive, Time Returns, time-bound keeping,
  future-self letters, exact-word revisiting of decisions/questions/promises,
  private future delivery to another living person, Book, and posthumous
  messages/Legacy. The founder may later explicitly remove, narrow, or reorder
  any Planned, Research, or Frozen item. Recording the direction does not
  authorize implementation or expand the current milestone.
- Reason: The founder explicitly wants all coherent time/correspondence ideas,
  including messages after death, retained so a narrow current build or future
  agent does not optimize them out. Durable separation between vision and
  authorization preserves ambition without bypassing safety gates.
- Consequence: Agents must consult the founder vision for long-term product
  proposals and must not treat Frozen as forgotten. ADR-019 remains fully in
  force: no Legacy or Book schema, flow, API, prototype involving real people,
  or implementation may be created until a new Accepted decision closes the
  relevant product, legal, identity, consent, cryptographic-key, recovery,
  abuse, privacy, emotional-safety, and independent-review gates. Rejected
  productivity, social, engagement, advertising, AI-companion, and AI-
  interpretation features remain Rejected. The earlier generated visual mockup
  is recorded as historical evidence, but `03-UX.md` remains canonical because
  the image asset was not recovered.

## How to add a decision

Use the next ADR number and include:

```md
## ADR-NNN — Short decision title

- Date: YYYY-MM-DD
- Status: Proposed | Accepted | Deferred | Superseded | Rejected
- Decision: Concrete choice.
- Reason: Evidence and tradeoff.
- Consequence: Scope, tests, migration, privacy, and what is no longer allowed.
- Supersedes: ADR-NNN (when applicable)
```

Do not rewrite an Accepted ADR to hide its history. Add a superseding entry.

## ADR-043 — Absolute date as anchor; temporal UI mapped to canonical vocabulary
- Date: 2026-08-16
- Status: Accepted
- Decision: The Home temporal surface offers return policy choices per entry
  ("Algum dia" / explicit period / "Escolher uma data" / "Nunca") mapped to
  the canonical vocabulary: Algum dia → `ReturnMode.ELIGIBLE` for organic
  return; explicit period or date → an explicit return request
  (canonical `Remember later`); Nunca → `ReturnMode.NEVER`. No operational
  times, no recurrence, no agenda behavior. "Selar" (biometric seal,
  privacy) and "Deixar descansar" (temporary ineligibility on an existing
  entry) remain distinct and are never merged with the temporal picker.
- Reason: Founder-approved temporal home system and calendar-anchor return
  (S-5); the write → wait → return loop stays the single primary action of
  the Home while keeping every control of `07-RETURNS-ENGINE.md` intact.
- Consequence: UI work adds a policy layer on top of `ReturnMode`; the
  engine must honor explicit request windows as first-class candidates
  alongside organic age-bucket selection. Anchored-date returns are
  candidates, never forced deliveries, keeping the frequency cap, rest
  rules, never-return, and consent.
- Supersedes: none (extends ADR-010 and the returns engine spec)

## ADR-044 — First Capsule onboarding (S-3)
- Date: 2026-08-16
- Status: Accepted
- Decision: No tutorial, no profile capture, no early permission requests.
  The product teaches itself through the first save: the first entry may
  carry the temporal choice, and the first confirmation reads
  "Guardado. O tempo cuida do resto." Subsequent saves confirm only with
  "Guardado." The first real return opportunity (days 30–45 with a single
  entry) becomes the product's self-explanation moment.
- Reason: Founder-approved S-3; principle 5 (autonomy before magic) and the
  canonical promise favor letting the loop speak for itself.
- Consequence: The UI tracks a one-time first-save confirmation state
  persisted locally; the bootstrap table of the returns engine is the only
  onboarding logic needed. No name, profile, or notification request at
  first launch.
- Supersedes: none

## ADR-045 — Temporal Patina for the home botanical motif (S-4 revised)
- Date: 2026-08-16
- Status: Accepted
- Decision: The decorative botanical motif of the Home may mature very
  slowly as a function of time since the first entry only (internal states:
  início, jovem, intermediário, maduro — never presented as levels).
  Entry count, frequency, streaks, consecutive days, sessions, engagement,
  and return count are explicitly excluded as inputs. No progress,
  percentages, unlockables, goals, "continue writing", or rewards. No
  regression during absence: the motif keeps maturing while the user does
  not write. The motif is purely decorative: TalkBack ignores it, Reduce
  Motion is respected, and any per-user deterministic variation stays on
  device and is never social or comparable.
- Reason: Founder-approved S-4 with these constraints; principle 4 forbids
  completion pressure and guilt, and the user must never be able to feed
  the plant by writing more.
- Consequence: Implementation is a small, deterministic, removable
  UI-only module (vector assets or simple interpolation); no engine, no
  persistence beyond the first-entry date, no network, no analytics
  involvement.
- Supersedes: none

## ADR-046 — Longevity promise for the export format (S-7)
- Date: 2026-08-16
- Status: Accepted
- Decision: Declare export format version `1.0`, documented and
  application-independent: combined Markdown + plain text, simple directory
  structure, documented metadata (original timestamp, timezone, opaque id),
  a format version marker, and an integrity checksum where appropriate. The
  promise is readability without Fio, not compatibility with any future
  technology.
- Reason: Founder-approved S-7; principle 11 (trust includes departure)
  and the existing export behavior already satisfy most of the shape; this
  formalizes it.
- Consequence: A canonical export-format document is added; version marker
  and checksum go into export output; any incompatible format change
  requires an explicit decision and migration documentation. No proprietary
  format is introduced.
- Supersedes: none

## ADR-047 — Ritual do Fio (S-1) is Planned, gated on archive and engine maturity
- Date: 2026-08-16
- Status: Planned
- Decision: The annual self-review surface is approved in spirit but waits
  for a stable Archive, a proven time-return engine, and observable
  long-history behavior. When implemented it is factual only ("Um ano do
  seu Fio. Quer reler aquele período?"), chronological, silent, with no
  summary, statistics, generated cover, or growth narrative; AI must not
  state what the user wrote about.
- Reason: Founder-approved S-1 conditioned on temporal dependencies; time
  since first entry must exist meaningfully before the ritual is offered.
- Consequence: No implementation now; the idea is retained and re-evaluated
  when the gated dependencies are met.
- Supersedes: none

## ADR-048 — Guardar, Encontrar e Arquivo formam a navegação principal calma

- Date: 2026-08-20
- Status: Accepted
- Decision: O aplicativo abre em Guardar e apresenta uma navegação inferior
  persistente com três destinos nomeados: `Guardar`, `Encontrar` e
  `Arquivo`. Encontrar deixa de ser um campo enterrado no Arquivo e recebe
  superfície própria. Arquivo continua cronológico e não algorítmico.
  Configurações e ações administrativas permanecem secundárias. Editar e
  excluir deixam a lista e passam ao detalhe da nota.
- Reason: O fundador identificou que Arquivo e Search estavam escondidos e sem
  hierarquia compreensível. Duas referências fornecidas mostraram o valor de
  navegação persistente, criação sempre acessível e coleções legíveis; sua
  estética escura, IA, tarefas, tags, cadernos e recursos de produtividade não
  são adotados.
- Consequence: `MainSurface` separa WRITE/FIND/ARCHIVE/SETTINGS; a barra
  inferior existe somente nos três destinos principais; trocar de destino
  limpa foco/teclado, mas não persiste queries. A Home continua início e centro
  do loop write-wait-return. Search continua read-only e não influencia
  Returns. Testes de acessibilidade e fluxo devem tratar os três destinos como
  alvos de no mínimo 48dp.
- Supersedes: amplia ADR-004: “visualmente secundário” não pode significar
  escondido em overflow; o Arquivo permanece secundário no conteúdo e no fato
  de a Home ser a abertura, não na sua descobribilidade.

## ADR-049 — Ajustes usa visão geral por intenção e divulgação progressiva

- Date: 2026-08-20
- Status: Accepted
- Decision: `Ajustes` deixa de ser uma lista longa de controles técnicos. A
  primeira tela mostra destinos curtos por intenção humana: Proteção ao abrir,
  Lembranças que voltam, Importar notas, Exportar uma cópia e Excluídos
  recentemente.
  Cada destino abre uma página focada que explica primeiro o efeito e depois
  oferece o controle. Termos de milestone, engenharia e validação não aparecem
  como cópia de produto. Funcionalidade desabilitada por build não é descrita
  como ativa.
- Reason: O fundador não compreendeu a tela existente. Uma configuração que
  exige conhecer M1/M2/M3 ou interpretar rótulos internos falha mesmo quando o
  mecanismo técnico está correto.
- Consequence: A navegação de Ajustes ganha estado somente de UI, sem schema ou
  analytics. Ações sensíveis preservam autenticação e confirmação. Testes de
  interface cobrem destinos e rótulos. `docs/NOTES-BASELINE-AUDIT.md` passa a
  distinguir função existente de função encontrável/compreensível. “Devolução”
  permanece termo de domínio; a interface usa “Lembranças que voltam”, explica
  a diferença entre TXT/Markdown e evita “lote” ou linguagem de armazenamento
  como conhecimento pressuposto.
- Supersedes: none
