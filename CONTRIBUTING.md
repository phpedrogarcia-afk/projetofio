# Contributing to Fio

Fio is a privacy-sensitive autobiographical product. A small change can affect
user trust, return behavior, or intimate data, so scope and evidence matter as
much as code quality.

## Before proposing a change

1. Read `AGENTS.md`.
2. Read `docs/01-PRODUCT.md`, `docs/02-PRINCIPLES.md`, and
   `docs/DECISIONS.md`.
3. Check the capability status in `docs/04-FEATURES.md` and its roadmap phase.
4. Read the domain specification for the affected area.
5. If the work is cross-cutting, create a plan using `.agent/PLANS.md`.

## Scope rules

- Approved scope may be implemented in its listed phase.
- Experimental scope needs a removable flag, measurement contract, stop rule,
  and rollback path.
- Planned scope waits for its phase and prerequisites.
- Research is documentation/measurement only.
- Frozen and Rejected scope is not implemented.
- A conflict with an Accepted decision stops work until a new/superseding ADR is
  explicitly approved.

## Change quality

Every implementation change should include, as relevant:

- a narrow user outcome and explicit non-goals;
- tests for normal, offline, failure, migration, deletion, accessibility, and
  privacy behavior;
- a description of new stored data, logs, telemetry, network calls, permissions,
  and dependencies;
- a safe rollback/disable path;
- updated specifications when behavior changes;
- current evidence for every test or security claim.

Do not use real journal text in fixtures, screenshots, issue descriptions, logs,
or code review. Use clearly synthetic examples.

## Commits and review

- Keep commits focused and explain the user-visible or architectural outcome.
- Do not mix an unrelated refactor with a product change.
- Never commit secrets, certificates, provisioning profiles, local databases,
  exports, or imported user archives.
- Review the diff for accidental content/logging/network additions.
- Record a new ADR instead of rewriting history when an Accepted decision
  changes.
- Release artifacts are generated from reviewed source; they are not edited by
  hand.

## Pull-request evidence when Git hosting is introduced

Include:

- outcome and non-goals;
- related milestone and ADRs;
- files/modules changed;
- tests run with results;
- privacy/data/dependency impact;
- screenshots using synthetic data for visual changes;
- migration and rollback notes;
- known limitations and follow-up decisions.

Git hosting, branch policy, CI, code owners, and release automation are not yet
decided. Do not invent them silently.
