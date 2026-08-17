# Fio

Fio is a private autobiographical memory application: write in your own words,
let time pass, and occasionally meet those words again.

Specification version: `0.1`

This repository is the permanent product and engineering home for building Fio
with Codex. It contains specifications and an Android M1 development checkpoint,
not a finished or published application.
Documentation is written in English for compact technical use; canonical
user-facing copy is in PT-BR.

## Start here

1. Open this folder as the repository root.
2. Ask Codex to read `AGENTS.md`.
3. Choose a milestone from `docs/10-ROADMAP.md`.
4. For substantial work, create a plan in `plans/` using `.agent/PLANS.md`.
5. Implement only features whose status permits that work.

Current next action:

> Continue the open manual/adverse-condition validation in
> `plans/2026-08-10-m1-android-trusted-local-core.md`. Automated M1 checks pass
> on API 26/API 36 and the approved POCO subset. Do not infer M2, Git,
> publication, signing, or release.

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
- Book and Legacy: frozen future concepts
- M1-R1 technical/product decisions: Accepted
- Milestone 1 Android engineering baseline: Accepted under ADR-037; automated
  M1 scope passes 20 JVM and 17 instrumented tests on API 26/API 36. Physical,
  accessibility, interoperability, second-OEM, and independent-review evidence
  remains an explicit pre-pilot gate while M2 engineering may proceed
- Current host: Windows x64 with Android Studio Quail 3 `2026.1.3`, stable
  command-line tools 22.0/platform-tools 37.0.1, and booted API 26/API 36 AVDs;
  POCO M3 Pro 5G is ADB-authorized on Android 13/API 33
- Milestone 2: decision packet M2-R1 and Draft execution plan prepared; no M2
  dependency, permission, schema, or application behavior changes before the
  packet receives explicit owner approval

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
