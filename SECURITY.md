# Security policy

Fio handles intimate autobiographical data. Security and privacy issues receive
priority over feature work and engagement goals.

The canonical design requirements are in
`docs/08-PRIVACY-SECURITY.md`. This file governs repository and reporting
behavior; it does not claim that the unimplemented application has been audited.

## Reporting a vulnerability

A public reporting address or private security channel has not yet been chosen.
That decision is required before the repository or application is published.

Until then:

- do not open a public issue containing a secret, exploit, real journal content,
  database, export, screenshot, key, token, certificate, or personal identifier;
- preserve the minimum technical evidence necessary;
- describe affected versions, conditions, and impact using synthetic data;
- notify the project owner through an agreed private channel before broader
  disclosure.

Do not invent a contact address in documentation or code.

## In-scope security surfaces

- local storage and migrations;
- cryptographic key and record handling;
- app lock, Sealed entries, snapshots, screenshots, and notifications;
- import/export and temporary plaintext;
- deletion, recovery, backup, and future sync;
- embeddings, search indexes, analytics, logs, and crash reporting;
- network endpoints, dependencies, entitlements, and permissions.

## Development data

- Use synthetic journal fixtures only.
- Store no private fixtures or local user database in the repository.
- Keep secrets in approved platform/local secret storage, never source files.
- Treat embeddings, search queries, import filenames, and derived labels as
  private content.
- Redact diagnostics and verify release logging independently.

## Response posture

When a credible issue is reported:

1. minimize further collection/exposure;
2. disable the affected optional path when possible;
3. identify data classes and versions affected without copying content;
4. fix and test the smallest safe change;
5. document residual risk and any required user action;
6. update the threat model and add a regression test;
7. communicate facts without overstating guarantees.
