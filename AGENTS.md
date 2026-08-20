# Fio — repository instructions

Fio is a private, local-first Android application for writing and later
re-encountering the user's exact words. It selects; it never interprets.

## Start and route

1. Read `AI-START-HERE.md`.
2. Read the current packet from `packets/EXECUTION-QUEUE.md`.
3. Read only the packet's `READ FIRST` files, then the named source files.
4. Do not scan or load the whole repository unless the packet is an audit or
   directed evidence is contradictory.

For source authority and document status, use `docs/INDEX.md`. Accepted ADRs
in `docs/DECISIONS.md` govern product/privacy intent. Code and passing tests
prove current behavior but never silently override an Accepted ADR.

## Critical invariants

- Preserve the user's exact words. Never rewrite, summarize, diagnose, grade,
  explain, or generate autobiographical meaning.
- Search and Returns stay separate: queries never select Returns; Returns never
  read queries.
- Plaintext, queries, embeddings and content-derived labels never enter logs,
  analytics, network payloads or unencrypted persistence.
- Writing, reading, Archive, local search and return eligibility remain
  offline-first.
- Explicit controls outrank algorithms: pause, never return, rest, delete and
  export must remain effective according to their approved phase.
- No streaks, goals, badges, feeds, social features, advertising, engagement
  pressure, AI companion, profiling or server-side plaintext.
- No new production dependency, SDK, network flow, schema, crypto behavior or
  product invariant without its required decision/packet.
- Never claim a human/device/security gate passed without current evidence.
- Never connect semantic research to Room or production Returns. ADR-041 keeps
  V0 Time-only with no model selected.
- FioOS is a separate repository/system and is outside ProjetoFio scope.

The complete numbered catalogue is `docs/atlas/INVARIANTS.md`.

## Stop conditions

Stop and report instead of inferring when:

- a request conflicts with an Accepted ADR or product principle;
- a packet is BLOCKED or requires an unapproved human/product decision;
- a SMALL task expands beyond its declared files/context budget;
- a migration, privacy boundary or destructive data operation lacks a plan;
- tests reveal an unrelated existing defect;
- user data, a physical-device gate or external reviewer is required;
- the current branch is `main`.

Use `SCOPE EXPANSION REQUIRED` when a SMALL packet cannot remain small.

## Implementation workflow

1. Confirm branch/status and preserve unrelated user changes.
2. State the smallest behavior boundary and inspect the relevant tests.
3. Make the smallest localized change; do not add architecture by default.
4. Run the packet's exact tests using `docs/TEST-LEVELS.md`.
5. Update only the canonical document whose behavior/status truly changed.
6. Stage explicit paths. Do not use `git add .` or `git add -A`.
7. Keep commit, push, PR, merge, publication and release as separate actions
   unless the user explicitly authorizes each relevant step.

For prolonged work, after roughly 30 minutes without a concrete diagnosis,
diff, test or reproducible blocker, stop and state what is known, unknown and
which exact file blocks progress.

## Decision and scope vocabulary

- **Approved/Accepted:** implementation may proceed in the listed phase.
- **Experimental:** removable and behind a flag.
- **Planned/Research/Deferred/Frozen:** no production implementation.
- **Rejected:** do not implement.

New product/privacy decisions are appended as new ADRs; never rewrite an
Accepted ADR to hide history. Long-term Legacy/Book intent is preserved in
`docs/11-FOUNDER-VISION.md` but remains Frozen.

## Response budget

Normal delivery reports stay short: `Changed`, `Tests`, `Risk`, `Next`. Do not
retell the project history. Link to evidence instead.
