# Fio

Fio is a private autobiographical memory application: write in your own words,
let time pass, and occasionally meet those words again.

Specification version: `0.1`

This repository is the permanent product and engineering home for building Fio
with Codex. It contains specifications, an accepted Android M1 engineering
baseline, and validation-only M2/M3 checkpoints—not a finished or published
app.
Documentation is written in English for compact technical use; canonical
user-facing copy is in PT-BR.

## Start here

1. Open this folder as the repository root.
2. Ask Codex to read `AGENTS.md`.
3. Choose a milestone from `docs/10-ROADMAP.md`.
4. For substantial work, create a plan in `plans/` using `.agent/PLANS.md`.
5. Implement only features whose status permits that work.

Current next action:

> M1 is the accepted engineering baseline. M2 Time Returns and M3 local import
> have completed validation-only engineering checkpoints. Retained physical,
> accessibility, provider, second-OEM, and independent-review gates still block
> participant use. M4-R1 is approved and its isolated corpus/control foundation
> is complete and M4-R2 froze corpus/rubric v1. MiniLM's development rehearsal
> did not justify Android advancement; ADR-041 closes V0 with no model selected;
> the isolated owner-test APK is installed on the POCO. The next action is owner
> functional observation; do not infer Git, publication, signing, release, or
> production readiness.

## Repository map

```text
Fio/
├── .agent/
│   └── PLANS.md
├── .gitignore
├── AGENTS.md
├── README.md
├── CHANGELOG.md
├── CONTRIBUTING.md
├── PROJECT-MANIFEST.md
├── SECURITY.md
├── app/                       Android/Gradle project root
│   ├── mobile/                Application module and tests
│   ├── gradle/                Versions, wrapper, verification metadata
│   ├── build.gradle.kts
│   └── settings.gradle.kts
├── docs/
│   ├── design/              UX/design reorganization package (source of truth
│   │   │                        for the upcoming UI migration; see its README)
│   ├── 01-PRODUCT.md
│   ├── 02-PRINCIPLES.md
│   ├── 03-UX.md
│   ├── 04-FEATURES.md
│   ├── 05-ARCHITECTURE.md
│   ├── 06-DATA-MODEL.md
│   ├── 07-RETURNS-ENGINE.md
│   ├── 08-PRIVACY-SECURITY.md
│   ├── 09-ANALYTICS-EXPERIMENTS.md
│   ├── 10-ROADMAP.md
│   ├── 11-FOUNDER-VISION.md
│   └── DECISIONS.md
├── plans/
│   ├── README.md
│   ├── 2026-08-10-m1-decision-packet.md
│   ├── 2026-08-10-m1-android-pivot-decision-packet.md
│   ├── 2026-08-10-m1-android-trusted-local-core.md
│   └── 2026-08-10-m1-trusted-local-core.md
└── releases/
    ├── README.md
    └── v0.1/
        ├── README.md
        └── Fio-v0.1-Codex-ready.zip
```

## Document roles

| Document | Question it answers |
|---|---|
| `AGENTS.md` | How must an agent behave in this repository? |
| `docs/01-PRODUCT.md` | What is Fio, for whom, and what is its core loop? |
| `docs/02-PRINCIPLES.md` | What must the product protect or refuse? |
| `docs/03-UX.md` | How should Fio look, sound, and behave? |
| `docs/design/` | The full UX/design reorganization packet: audit, new IA and
navigation, Sage Green design system, flows, microinteractions, wireframes,
mockups, and the staged (A–D) implementation plan. |
| `docs/04-FEATURES.md` | What is approved, experimental, deferred, or rejected? |
| `docs/05-ARCHITECTURE.md` | Where do responsibilities and trust boundaries live? |
| `docs/06-DATA-MODEL.md` | What entities and invariants exist? |
| `docs/07-RETURNS-ENGINE.md` | When and how can something return? |
| `docs/08-PRIVACY-SECURITY.md` | How is intimate personal data protected? |
| `docs/09-ANALYTICS-EXPERIMENTS.md` | How can we learn without observing the user's life? |
| `docs/10-ROADMAP.md` | In what order should the product be built and proven? |
| `docs/11-FOUNDER-VISION.md` | What complete long-term direction must not be forgotten? |
| `docs/DECISIONS.md` | Which important choices are accepted and why? |
| `CONTRIBUTING.md` | How should humans and agents propose and verify changes? |
| `SECURITY.md` | How should vulnerabilities and private test data be handled? |
| `releases/` | Where are immutable local distribution artifacts kept? |

## Current state

- Specification version: `0.1`
- Product name: `Fio` (working name, accepted for this package)
- Initial platform direction: native Android with Accepted Kotlin/Compose/
  Room/KSP local-first architecture
- First proof: writing, local archive, and time-based returns without AI
- Semantic returns: experimental and removable
- Book and Legacy: desired long-term concepts, still Frozen and not authorized
  for design or implementation
- M1-R1 technical/product decisions: Accepted
- Milestone 1 Android engineering baseline: Accepted under ADR-037; the current
  integrated suite passes 51 JVM and 25 instrumented tests on API 26/API 36,
  including the Devoluções privacy-cover regression.
  Physical,
  accessibility, interoperability, second-OEM, and independent-review evidence
  remains an explicit pre-pilot gate while M2 engineering may proceed
- Current host: Windows x64 with Android Studio Quail 3 `2026.1.3`, stable
  command-line tools 22.0/platform-tools 37.0.1, and booted API 26/API 36 AVDs;
  POCO M3 Pro 5G is ADB-authorized on Android 13/API 33
- Milestone 2: M2-R1/ADR-038 approved and implemented in the isolated validation
  variant; Room schema 2, pure Time engine, unique WorkManager scheduling,
  generic notifications, and local controls pass API 26/API 36 automation.
  Debug-primary and release cannot activate Time Returns; retained pre-pilot
  gates remain open
- Milestone 3: M3-R1/ADR-039 approved and implemented in the isolated validation
  variant; bounded local TXT/Markdown parsing, preview, exact deduplication,
  atomic Room schema 3 commit, protected rollback, and Fio export round-trip
  pass 51 JVM tests and 23 instrumented tests on each API 26/API 36 endpoint.
  Debug-primary and release cannot activate import; no personal file or
  participant pilot was authorized
- Milestone 4: M4-R1/ADR-040 approved; isolated synthetic corpus, blind human
  packets/interfaces, lexical control, and candidate inventory are complete.
  M4-R2 froze the corpus. A hash-verified MiniLM development-only rehearsal
  failed the safe top-1 check and used about 521 MB additional PC working set;
  it was not advanced to Android. Fourteen M4 tests pass. ADR-041 records the
  valid early-stop outcome: no V0 model, semantic index, M5 pilot, or product
  Return integration
- Owner-test package: `com.projetofio.app.validation` installed and cold-launched
  on the API 33 POCO. The corrected post-regression APK hash is recorded in
  `plans/evidence/2026-08-13-return-privacy-cover-regression.md`; the artifacts
  under `releases/v0.1.0-validation/` are the earlier pre-regression snapshot

The detailed status lives in `docs/04-FEATURES.md` and
`docs/10-ROADMAP.md`. No roadmap entry is a promise of a date.

## Repository boundaries

- `docs/` is canonical product and engineering context.
- `app/` contains the M1 Android development implementation and generated test
  evidence; it is not a release boundary.
- `plans/` contains living execution plans, not product authorization.
- `releases/` contains generated snapshots and is not a source of truth.
- Real journal content, credentials, certificates, and production secrets must
  never be committed.
