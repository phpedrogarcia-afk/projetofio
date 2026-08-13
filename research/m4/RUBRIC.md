# M4 PT-BR relationship rubric

Status: Frozen v1; retained only if semantic selection is reopened after ADR-041

Question for each pair:

> Quanto o segundo trecho se relaciona ao primeiro sem depender apenas das
> mesmas palavras?

Use only the text shown. Do not diagnose emotion, infer a hidden biography, or
reward eloquence.

| Rating | Meaning | Guidance |
|---:|---|---|
| 0 | Unrelated or contradicted | No useful autobiographical relationship; shared words alone do not count |
| 1 | Weak/contextual | A broad setting or object overlaps, but the pair would be a fragile Return candidate |
| 2 | Meaningful indirect relation | A person could reasonably notice a shared experience without the texts saying the same thing |
| 3 | Strong relation/paraphrase | The same experience or a very close relation is present |

Separately, mark `near_duplicate = yes` only when the second text mostly repeats
the first. Near-duplicates can receive rating 3 but are excluded from a useful
Resonance Return because repetition is not discovery.

## Annotation rules

- Work independently; do not discuss ratings before both packets are complete.
- Do not use search, external context, AI assistance, or a model score.
- Rate every row; allowed ratings are integers 0–3 only.
- The packet order is randomized and hides split, category, construction target,
  and exclusion expectation.
- If wording is confusing, still rate the visible relationship and add only the
  closed reason code `ambiguous_wording`; do not enter free-text commentary.
- A disagreement is evidence to report, not an error to erase.

## Freeze rule

After either annotator sees a held-out row, changing its wording invalidates that
held-out version. Corrections require a new corpus version, new hash, and fresh
blind ratings. Development rows may be revised only before candidate scoring.
