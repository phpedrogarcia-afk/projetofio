# Fio Export Format — v1.0

Status: Canonical for export v1.0 (ADR-046)

## Promise

A Fio export is **human-readable, documented, and independent of the Fio
application**. A person who exports their archive today can open and read it
in 2040 with only standard text tools, even if Fio no longer exists. This is a
promise about readability of the current version, not a guarantee of
compatibility with any future technology.

## Composition

An export is a directory (or zip of it) containing:

| Path | Content |
|---|---|
| `README.md` | Human introduction: what this export is, what each file contains, and where to read more |
| `manifest.json` | Machine-readable index: export version `1.0`, export timestamp (UTC), timezone of the device, count of entries, and per-entry checksums (SHA-256 of the Markdown file content) |
| `entries/YYYY/YYYY-MM-DD_<opaque-id>.md` | One Markdown file per entry, ordered oldest → newest |
| `entries/YYYY/YYYY-MM-DD_<opaque-id>.txt` | Plain-text mirror of the same content |
| `deleted/` (when applicable) | Entries held in Recently Deleted, clearly marked as such |

## Entry file structure (Markdown)

```markdown
<!-- Fio export format v1.0 | id: <opaque> | created: <ISO-8601 original> | timezone: <IANA> | updated: <ISO-8601> | source: native|import-md|import-txt -->

# <formatted original date in the entry's timezone>

<exact entry content, character-for-character>
```

The Markdown body **must be the exact original content**, unchanged, with no
summarizing, reformatting, or annotation inserted into the autobiographical
text itself. All metadata lives in the comment header, so the visible text is
what the person actually wrote.

## Versioning and integrity

The comment header and `manifest.json` both carry `format version 1.0`. A
reader program can verify each entry by recomputing SHA-256 of the Markdown
file and comparing it to the manifest. If Fio later needs an incompatible
format change, a new major version is declared in a new decision record;
v1.0 exports remain readable by any tool that understands Markdown.

## Invariants

The export must never: contain plaintext journal content inside analytics or
telemetry; depend on Fio to be opened; use a proprietary container as the
primary format; hide behind retention friction; or alter the original words.
