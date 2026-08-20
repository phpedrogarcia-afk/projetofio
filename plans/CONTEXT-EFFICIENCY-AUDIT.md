# Context Efficiency Audit

Status: ACTIVE — baseline for `codex/context-efficiency-v1`

Date: 2026-08-20

## Outcome

Small changes are expensive because routing exists but is not the default boot
path, status is implicit in most documents, packets do not yet carry bounded
context, and all major Compose surfaces share one 2,088-line source file.
The remedy is compression and routing, not another parallel Atlas.

## Measured baseline

| Signal | Current value |
|---|---:|
| Markdown files | 152 |
| `plans/` Markdown files | 50 |
| `docs/` Markdown files | 45 |
| `research/` Markdown files | 25 |
| `packets/` Markdown files | 10 |
| Root Markdown files | 12 |
| `FioApp.kt` | 2,088 lines / 89,850 bytes |
| `docs/DECISIONS.md` | 858 lines |
| Plans without a `Status:` line | 29 / 50 |
| Docs without a `Status:` line | 31 / 45 |
| Packets without a `Status:` line | 10 / 10 |
| Current unit baseline | 134 passed, 0 failed/error/skipped |

The counts are discovery metrics, not a request to add status headers to every
historical file. A compact index should classify groups without touching dozens
of stable documents.

## Problems found

### Boot path is wider than necessary

Root `AGENTS.md` is 180 lines and mixes durable rules with a long historical
implementation narrative. It requires three product documents for every task,
then adds domain documents by topic. The Atlas already has a 53-line master
map, but there is no short root entrypoint that routes by task.

### Source-of-truth precedence conflicts

`AGENTS.md` says Accepted ADRs outrank principles and product documents.
`docs/atlas/SOURCE-OF-TRUTH.md` says production code always wins over every
document. Code is evidence of current behavior, but it cannot silently override
an Accepted product/privacy decision. This ambiguity makes agents read both
documents and still reason about precedence.

### History remains on the execution path

The root contains five `MANUS-*-FINAL.md` reports plus mission work notes.
`plans/` contains completed milestone plans, campaign reports, evidence, old
queues, handoffs, and current state together. The material is valuable, but a
fresh agent cannot reliably distinguish ACTIVE from HISTORICAL by path alone.

### Packet factory is incomplete

The queue lists FIO-P06, FIO-P07 and FIO-P10 as READY, but corresponding packet
files do not exist. Existing packets lack a uniform `READ FIRST`, `OPTIONAL`,
`DO NOT READ`, expected-file, exact-test, and context-budget header. A packet
therefore does not yet bound discovery.

### UI has multiple independent responsibilities

`FioApp.kt` contains orchestration plus Home/time picker, Search, Archive,
entry detail/editing, Settings and its five subpages, Return, privacy/lock
surfaces, botanical drawing, helpers, and shared components. Archive work
currently requires opening Home, Search, Return and Settings code. Natural
surface boundaries and existing instrumentation tests make conservative
extraction worthwhile.

### Atlas drift increases verification cost

Examples found in the current boot map:

- `FioApp.kt` is described as approximately 1,914 lines; it is 2,088.
- target SDK is described as preview 37; build configuration uses 36.
- the master map says the future engine is not in main-era UI commentary even
  though the integrated branch contains the Time engine checkpoint.
- live project state retains an older APK hash after the temporary debug-capture
  build installed on the POCO.

The Atlas remains a router/lens. Volatile counts and device artifacts should
not be mandatory boot facts.

## Duplication hotspots

- Product prohibitions repeat across `AGENTS.md`, Principles, Master Map,
  Invariants, packets, project state, and next-work summaries.
- Search/Returns separation repeats in AGENTS, Search architecture, Atlas maps,
  project state and queues.
- Test counts and milestone history repeat in AGENTS, Master Map, project state,
  next work, packet evidence and final mission reports.
- Current-work order appears in `PROJECT-STATE`, `NEXT-WORK`, multiple queues
  and packet index; only `packets/EXECUTION-QUEUE.md` should own order.

## Proposed compression

1. Add one root `AI-START-HERE.md` (100–150 lines maximum) that points rather
   than copies.
2. Reduce root `AGENTS.md` to invariants, stop conditions, routing and workflow;
   remove historical narrative.
3. Add `docs/INDEX.md` to classify documentation by area and status groups.
4. Add one compact `docs/TEST-LEVELS.md` as the only test router.
5. Use officially supported nested `AGENTS.md` only for genuinely different
   local rules (`ui/`, `search/`, `crypto/`/`persistence/` if justified).
6. Upgrade the packet template and READY packets into bounded context packs.
7. Extract cohesive UI surfaces without new architecture, framework, Gradle
   module, navigation system, DI or behavior.
8. Keep historical material in place unless a move has a clear routing benefit;
   classify it from the index to avoid link churn.

## Risks and controls

| Risk | Control |
|---|---|
| Mechanical UI extraction changes behavior | Move code unchanged; compile and targeted tests after each extraction |
| Too many tiny files | One file per cohesive surface/group, not per composable |
| Nested instructions increase prompt size | Keep them short and domain-local; root remains authoritative |
| Status work creates documentation churn | Classify families in one index; do not mass-edit history |
| New router becomes another source of truth | Router owns paths only; ADR/spec/code remain authoritative |
| Refactor hides the P19 product gap | Preserve behavior and keep P19 explicitly decision-required |

## Success measure

For Home copy, Search behavior and Returns rule simulations, measure the
minimum routed documents/files before and after. A fresh-agent test must reach
the correct source without a repository-wide scan. UI success additionally
requires Archive work to avoid reading Home, Return and Settings implementations.
