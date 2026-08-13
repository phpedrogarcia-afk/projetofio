# 10 — Roadmap

Status: Canonical sequence for specification v0.1

This roadmap is ordered by evidence and risk, not calendar promises. A milestone
starts only when its prerequisites and decisions are satisfied. Nearby features
do not enter merely because Codex can build them quickly.

## Current state

The repository contains a specification package and an active Android M1
implementation checkpoint, not an accepted or published application. Milestone
0 is complete. The owner corrected the initial client
to Android in `ADR-033` before generation. Apple-specific ADR-005, ADR-022,
ADR-023, and ADR-028 are Superseded. Android Recommendation R2 and ADR-034–
ADR-037 are Accepted. The former iOS M1 plan is Blocked. Its Android replacement
has a validated technical matrix and is Approved with `minSdk 26` and
application ID `com.projetofio.app`. M1 automated emulator scope is complete.
ADR-037 accepts it as the engineering baseline for synthetic-data M2–M4
development while retaining physical, accessibility, interoperability,
second-OEM, and independent-review checks as a mandatory pre-pilot gate.

```text
M0 SPECIFICATION
      ↓
M1 TRUSTED LOCAL CORE
      ↓
M2 TIME RETURNS
      ↓
M3 IMPORT + TIME PILOT
      ↓
M4 SEMANTIC BENCHMARK
      ↓
M5 CONTROLLED RESONANCE PILOT
      ↓
M6 KEEP / REVISE / REMOVE DECISION
      ↓
V1 PRIVATE BETA HARDENING (conditional)
      ↓
V2 LONGITUDINAL MEDIA RESEARCH (conditional)
      ↓
LONG-TERM TIME TRUST + CORRESPONDENCE (separately gated)
```

## Milestone 0 — Repository understands Fio

Status: Complete in specification package v0.1

Outcome:

- Codex can discover `AGENTS.md` from the repository root;
- product, principles, UX, feature status, architecture, data, returns,
  privacy, experiments, roadmap, and decisions have explicit owners;
- contradictions route to `DECISIONS.md` instead of being resolved silently;
- complex work has a plan template;
- Frozen and Rejected features are visible.

Exit evidence:

- all documented relative links resolve;
- status vocabulary is consistent;
- V0 scope does not require semantic AI or a server;
- no document claims that unimplemented security has been validated;
- package can be opened directly as a repository root.

## Milestone 1 — Trusted local core

Status: Engineering baseline accepted; pre-pilot validation evidence pending

User outcome:

> A person can write, close Fio, reopen it offline, and find or export exactly
> what they wrote.

Scope:

- create a native Android project and test targets only after the Android
  rebaseline gates are Approved;
- define Domain, Application, Persistence, Crypto, and UI boundaries;
- implement plain-text Write, local draft protection, save, edit, and Archive;
- implement Recently Deleted, recovery, permanent delete, and Markdown/plain
  text export;
- implement privacy cover and optional app lock;
- create return-policy fields even though no Return is delivered yet;
- establish versioned schema/migration and synthetic test fixtures;
- implement the reviewed local data-protection design or limit claims until the
  design is approved.

Non-goals:

- notifications and return selection;
- embeddings or semantic search;
- import;
- account, server, sync, analytics vendor, monetization;
- rich text, photos, audio, tags, Book, or Legacy.

Decision state before implementation:

- **Accepted:** native Android, local-first/offline-first client (`ADR-033`).
- **Accepted:** explicit global Return consent defaults to `notConfigured`
  (`ADR-027`).
- **Accepted:** one encrypted active Draft, one-document export, and privacy-
  cover/app-lock product defaults (`ADR-029`–`ADR-031`); Apple mechanisms are
  Superseded.
- **Accepted:** Kotlin/Compose/Room/KSP Android stack (`ADR-034`).
- **Accepted:** Android Keystore/AES-GCM, BiometricPrompt, privacy, backup, and
  Storage Access Framework mechanisms (`ADR-035`).
- **Validated technical gate:** stable toolchain, POCO Android 13/API 33, API
  26/API 36 AVDs, and pinned dependency/test matrix (`ADR-036`).
- **Approved owner gate:** `minSdk 26`, `com.projetofio.app`, and the Android M1
  plan were explicitly approved on 2026-08-10.
- **Approved and executing:** `plans/2026-08-10-m1-android-trusted-local-core.md`
  replaces the former iOS execution sequence. Its generation gates were closed
  before source creation; current work is M1 validation and hardening.
- **Separate boundary:** Git initialization, commit, remote, push, and
  publication are not inferred (`ADR-032`).

Exit criteria:

- save/read/edit works in airplane mode and after relaunch;
- forced save failure preserves the draft and does not show false success;
- migration fixtures pass from every supported schema version;
- deletion removes local searchable/derived artifacts and cancels references;
- export is complete, readable, and cleans temporary plaintext on every path;
- app switcher and notifications contain no content;
- Android font scaling, TalkBack, keyboard, reduced-motion/animation behavior,
  and locked-device cases are tested on the supported matrix;
- privacy/security checklist applicable to M1 passes;
- independent reviewer can trace plaintext-to-ciphertext boundary.

Gate disposition under ADR-037:

- deterministic engineering evidence is accepted for continued local,
  synthetic-data development;
- open human, external-device, provider-interoperability, and independent-review
  criteria remain required before the M3 Time-only pilot, any production
  release, or any security/audit/broad-compatibility claim;
- this staged acceptance does not label unperformed checks as passed.

## Milestone 2 — Time returns

Status: Engineering checkpoint complete; validation-only; pre-pilot gates retained

User outcome:

> An eligible old Entry can quietly return once, and the person can close it,
> prevent it from returning, or pause all returns.

Scope:

- pure Returns engine with injected clock/calendar/randomness;
- bootstrap, age buckets, eligibility, frequency, and idempotency;
- local generic notification `Algo seu voltou.`;
- Return screen with original date/content;
- Never Return and global pause;
- notification-denied and background-delay behavior;
- local-only pilot feedback sheet behind a feature flag;
- no telemetry upload until study consent/event schemas are approved.

Non-goals:

- semantic candidates;
- Rest, Remember Later, Sealed entries;
- Recurrence/Bridge;
- remote push or server scheduling.

Exit criteria:

- all eligibility and precedence table tests pass;
- no duplicate notification after crash/re-evaluation/timezone change;
- ignored Return produces no reminder or badge;
- policy changes cancel pending notifications correctly;
- Time behavior is useful with semantic flags/code absent;
- Return screen exposes no algorithm rationale;
- notification and logs contain no private content.

Checkpoint evidence: ADR-038 and the approved M2 plan produced Room schema 2,
the pure Time engine, unique WorkManager scheduling, generic local
notifications, validation-only consent/settings/Return UI, 37 passing JVM
tests, 21 passing instrumented tests on each API 26/API 36 endpoint, an API 36
permission grant/revoke proof, and passing adverse/credential regressions. See
`plans/evidence/2026-08-13-m2-time-returns.md`. Physical M2 flow,
TalkBack/manual accessibility, notification-dialog dismissal/channel-disable,
second-OEM/provider interoperability, and independent review remain pre-pilot
evidence; no participant, release, or production claim follows.

## Milestone 3 — Import and Time-only pilot

Status: Engineering checkpoint complete; validation-only; participant pilot blocked

User outcome:

> A participant can preview and import an existing TXT/Markdown history, then
> experience the complete Time-based loop without risking the canonical archive.

Scope:

- bounded TXT and Markdown parsers;
- staging preview, validation, date mapping, deduplication, atomic commit;
- cancel/failure cleanup and committed-batch rollback;
- import provenance and original dates;
- consented content-free pilot events if approved;
- Stage A research with Time only.

One additional source importer may be selected only from participant evidence.

Exit criteria:

- malicious/corrupt/oversized fixtures fail safely;
- archive traversal and unsupported active content are rejected;
- cancellation/failure leaves canonical Entry count/content unchanged;
- original dates survive round-trip export;
- imported Entries obey Never Return/delete/pause controls;
- Time-only pilot confirms basic comprehension and trust before semantic work;
- event contract tests reject content and arbitrary strings.

Checkpoint evidence: ADR-039 produced the pure bounded TXT/Markdown parser,
exact local deduplication, Room schema 3, atomic commit/protected rollback,
encrypted minimal provenance, SAF preview UI, and export round-trip with named
timezone preservation. The final suite passes 51 JVM tests and 23 instrumented
tests on each API 26/API 36 endpoint; both endpoints also pass the validation-
only SAF profile and the retained adverse/credential regressions. See
`plans/evidence/2026-08-13-m3-local-import.md`. This completes import
engineering only. The Time-only participant pilot, content-free pilot events,
personal-file import, TalkBack/manual accessibility, second-OEM/provider
interoperability, physical authentication, and independent review remain
blocked gates.

## Milestone 4 — Portuguese semantic benchmark

Status: Complete — ADR-041 selected no V0 model after the development gate

Outcome:

> We know whether an on-device model can retrieve meaningful PT-BR relationships
> within acceptable privacy, latency, memory, energy, and package budgets.

Scope:

- build purpose-written/consented pair benchmark and rubric;
- compare an Android on-device baseline, compact multilingual encoder, and PT-
  focused option;
- measure ranking quality, near-duplicate failure, latency, memory, energy,
  binary size, license, OS/device support;
- define encrypted index versioning and rebuild behavior;
- create model adapter without connecting it to production Returns.

Exit criteria:

- held-out results and human disagreement are reported before selecting a model;
- all models run on device without journal network calls;
- device budgets and supported matrix are explicit;
- license/dependency/security review is complete;
- if no model clears thresholds, stop semantic work and retain Time.

Current checkpoint: M4-R1/ADR-040 created an isolated standard-library research
harness, a 300-pair synthetic PT-BR corpus with 20 scenario families/60 ranking
groups, a family-isolated 240-development/60-held-out split, two differently
ordered blind annotation packets, and a current-source candidate inventory. The
corpus hash is
`72a702510cc0ed3e9f72595a8e0df9ed379278b64e223dd1b1a36b30ed73ccef`.
Thirteen offline tests pass. The lexical control rehearsal records Spearman
`0.135344`, mean nDCG@5 `0.498154`, and unsafe top-1 rate `1.0`; these use only
construction targets and are not human/model selection evidence. M4-R2 froze
corpus/rubric v1. A hash-verified MiniLM ONNX
development rehearsal remained outside product/Gradle and did not evaluate the
held-out split: Spearman `0.345850`, mean nDCG@5 `0.483447`, unsafe top-1 `1.0`,
about 521 MB extra Windows working set, and about 41.5 ms per unique text on the
recorded PC run. ADR-041 rejects this candidate before held-out/Android work and
closes M4 with no model selected. Human packets remain mandatory only if model
selection is reopened. No semantic dependency or Android integration exists,
and ADR-024 remains Deferred.

## Milestone 5 — Controlled Resonance pilot

Status: Deferred for V0 by ADR-041; no M5 product code or participant pilot

User outcome:

> Some participants receive locally selected semantic Returns, but the visible
> experience and all autonomy controls remain identical to Time.

Scope:

- encrypted on-device embedding/index lifecycle;
- recent-entry anchor and qualified candidate rules;
- age-matched Time control;
- fixed counterbalanced within-subject assignment;
- content-free study events and separate skippable feedback;
- operational kill switch and Time fallback;
- 60–90 day exploratory pilot.

Exit criteria:

- no plaintext/vector network path exists;
- deletion purges semantic derivatives;
- assigned versus delivered algorithm is measurable without Entry identity;
- Time and Resonance UI/copy are indistinguishable;
- fallback and stop conditions work;
- study data, missingness, rejection, discomfort, and device cost are reported;
- experiment code/data removal path is tested.

## Milestone 6 — Semantic decision

Status: Complete for V0 — Remove / do not introduce Semantic Resonance

Use `09-ANALYTICS-EXPERIMENTS.md` thresholds to make one explicit decision:

### Remove

- disable/delete semantic selection and derived indexes;
- keep Time as core;
- retain anonymized approved study result only per consent/retention;
- document why complexity did not earn its place.

### Revise

- do not promote to core;
- state one bounded hypothesis and new stop condition;
- avoid indefinite experimentation or expanding telemetry.

### Keep investigating for V1

- requires at least a meaningful estimated advantage, no material rejection or
  trust cost, acceptable device performance, and qualitative Strong Resonance;
- document Accepted model/threshold/version decision;
- keep a user-visible/offline Time fallback and local disable path.

No result authorizes AI interpretation, server-side matching, or engagement
notifications.

## V1 — Private beta hardening

Status: Planned and conditional on core proof

Candidate scope, each requiring its own plan:

- Let Rest and Remember Later;
- Sealed entries;
- textual local search, then experimental semantic search;
- Entry revision history;
- broader import based on evidence;
- encrypted multi-device sync and backup only after key/recovery/deletion design;
- production accessibility, reliability, security review, support, and privacy
  documentation;
- simple monetization decision consistent with trust.

V1 exit gate:

- independent security review of cryptography and sync;
- recovery, revocation, conflict, deletion, backup, and lost-key tests;
- export and account deletion work without retention friction;
- incident and support procedures exist;
- no unclosed V0 privacy or data-loss issue.

## V2 — Longitudinal media research

Status: Planned/Research; not authorized for implementation now

Possible work:

- one optional photo per Entry;
- one optional audio recording;
- local transcription/OCR if justified;
- Bridge, Recurrence, or Long Thread displays without generated conclusions;
- optional location only with explicit consent and no psychological trigger.

Each expands the sensitive-data and return-safety surface and needs a separate
decision. Book and Legacy remain Frozen even after V2 begins, while their
long-term intent is preserved by ADR-042 and `11-FOUNDER-VISION.md`.

## Long-term — Time trust and private correspondence

Status: Founder direction preserved; no implementation authorized

Possible sequence after the earlier milestones and trust gates:

1. **Time trust** — `Guardar até…`, a future letter to oneself, and exact-word
   revisiting of decisions, questions, or promises.
2. **Private correspondence** — scheduled delivery of selected material to an
   authenticated, consenting living recipient.
3. **Legacy** — carefully governed posthumous delivery and, separately, a Book
   assembled from the person's own archive.

This sequence is not a release promise. Each step requires its own product,
data, encryption/key, recovery, consent, abuse, accessibility, and independent-
review decisions. ADR-019 still prohibits Legacy schemas, flows, APIs, and
implementation. ADR-042 records that Legacy is a desired destination rather
than a discarded concept; it does not supersede the freeze.

## Current Codex handoff

Current implementation handoff:

> Read `AGENTS.md`, ADR-033–ADR-041, the accepted M1 baseline, completed
> validation-only M2/M3 checkpoints, retained pre-pilot gates, and the isolated
> M4 foundation, M4-R2 evidence, and ADR-041 early-stop decision. Corpus/rubric
> v1 are frozen and V0 selects no semantic model. Do not add a product
> model/runtime, connect semantic research to Returns, start a
> participant pilot, or infer Git, publication, signing, or release.

After the Android decisions, matrix, and rewritten plan are Approved,
application work can proceed in small, verifiable slices. Planning and decision
approval do not authorize later-phase features, Git operations, publication, or
release.
