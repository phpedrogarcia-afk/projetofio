# Blind annotation packets

`annotator-a.csv` and `annotator-b.csv` contain the same 300 synthetic pairs in
different deterministic orders. They deliberately omit split, category,
construction target, and expected exclusion.

For a simpler non-technical workflow, open the matching `annotator-a.html` or
`annotator-b.html`. Each is a self-contained offline page with a restrictive
content policy, local-only progress, 48 px controls, and CSV export. It contains
no external script, font, image, analytics, request, or model score.

Each annotator fills:

- `rating`: integer `0`, `1`, `2`, or `3` according to `../RUBRIC.md`;
- `near_duplicate`: `yes` or `no`;
- `reason_code`: blank or exactly `ambiguous_wording`.

Completed files go in `responses/` and are ignored by Git. Do not place names,
email addresses, personal journal text, comments, or any free-form explanation
in a packet. If semantic selection is reopened, the two annotators work
independently.

This gate is intentionally not automated: construction targets are not human
labels, and one person cannot supply two independent judgments. ADR-041 rejects
the current candidate before this gate; the sole owner does not need to fill a
packet for the V0 no-model decision.
