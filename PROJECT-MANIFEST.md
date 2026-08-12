# Project manifest

- Project: `Fio`
- Current specification: `0.1`
- Current milestone state: Android ADR-033–ADR-036 Accepted; prior iOS M1 plan
  Blocked; Android M1 implementation checkpoint built and automated matrix
  passing, with final manual/adverse-condition acceptance evidence pending
- Current execution host: Windows x64 with Android Studio Quail 3 `2026.1.3`,
  command-line tools 22.0/platform-tools 37.0.1, boot-validated API 26/API 36 AVDs, and
  an ADB-authorized Android 13/API 33 POCO
- Established as permanent project root: `2026-08-10`
- Language: English specifications with canonical PT-BR product copy
- Intended use: open this folder as the repository root and start with
  `AGENTS.md`

## Canonical source layout

```text
AGENTS.md                  Repository instructions for Codex
README.md                  Entry point and project map
CHANGELOG.md               Human-readable history of specification/releases
CONTRIBUTING.md            Change and review workflow
SECURITY.md                Security reporting and private-data handling
.agent/PLANS.md            Required execution-plan format
app/                       Application implementation boundary
docs/                      Canonical product and engineering specifications
plans/                     Living implementation plans
releases/                  Generated immutable local artifacts
```

## Canonical specifications

```text
docs/01-PRODUCT.md
docs/02-PRINCIPLES.md
docs/03-UX.md
docs/04-FEATURES.md
docs/05-ARCHITECTURE.md
docs/06-DATA-MODEL.md
docs/07-RETURNS-ENGINE.md
docs/08-PRIVACY-SECURITY.md
docs/09-ANALYTICS-EXPERIMENTS.md
docs/10-ROADMAP.md
docs/DECISIONS.md
```

## Current boundary

The repository contains canonical specifications and an active M1 Android
development implementation under `app/`.

It does not contain:

- an accepted, signed, or published Android release;
- server, sync, analytics, Return delivery, or semantic-model code;
- credentials, API keys, certificates, provisioning profiles, or secrets;
- real journal content, imported archives, research responses, or user data;
- a final embedding model;
- a promise that Proposed or Deferred decisions have been settled.

## Ready-state checks

- `AGENTS.md` is in the permanent project root.
- Accepted decisions have precedence; Proposed and Deferred entries are explicit.
- V0 remains useful with networking and Semantic Resonance disabled.
- Rejected and Frozen features are visible to Codex.
- Cross-cutting work has a plan template and approval boundary.
- Privacy covers text, embeddings, queries, imports, exports, attachments, logs,
  notifications, and analytics.
- Roadmap Milestone 1 has an Accepted Android architecture, Approved execution
  plan, built checkpoint, resolved dependencies, Room schema v1, and passing
  unit/instrumented matrix. Manual acceptance gates remain; Git, publication,
  release, and later milestones remain separate.

## Release policy

Release ZIPs under `releases/` are generated snapshots. They are not edited in
place and are not the canonical source. Every artifact receives a versioned
folder, a short release note, and a SHA-256 digest.
