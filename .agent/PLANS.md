# Execution plans for Fio

Use an execution plan before work that spans multiple modules, changes stored
data, touches privacy or cryptography, modifies the returns engine, introduces
an external dependency, or lasts more than one focused implementation session.

Plans are living documents. Save them in `plans/YYYY-MM-DD-short-name.md` and
update them as evidence changes.

## Required structure

```md
# <Outcome>

Status: Draft | Approved | In progress | Complete | Blocked
Owner: <human or agent>
Related milestone: <roadmap milestone>
Relevant decisions: <ADR identifiers>

## Outcome

Describe what a user can do when the work is complete.

## Non-goals

List nearby work that is explicitly outside scope.

## Context read

List the specifications and current code paths inspected.

## Assumptions and open questions

Separate verified facts from assumptions. Mark questions that need human
approval before implementation.

## Implementation sequence

Use ordered, independently verifiable steps. Name expected files/modules but do
not invent paths that do not yet exist.

## Privacy and data impact

Describe new data, storage, logging, analytics, network, deletion, and migration
behavior. Write `None` only after checking.

## Tests and acceptance evidence

List automated checks and observable acceptance scenarios, including offline,
failure, accessibility, and privacy cases when relevant.

## Rollback or disable path

Explain how to reverse the change or disable an experiment without losing user
data.

## Progress log

- YYYY-MM-DD — factual progress note and evidence.

## Final report

Summarize files changed, tests run, limitations, migrations, flags, and any
follow-up decision still required.
```

## Planning rules

- A plan does not authorize a Frozen, Research, or Rejected feature.
- Do not hide product decisions inside implementation steps.
- Prefer milestones that leave the app usable and data safe.
- Every risky migration needs a backup/recovery story and failure tests.
- Every experiment needs a feature flag, a removal path, and a decision rule.
- Never claim security, privacy, or test properties without current evidence.
