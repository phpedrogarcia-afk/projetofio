# 02 — Principles

Status: Canonical for specification v0.1

These principles are design constraints, not aspirations to trade away for
growth. A feature that violates them requires an explicit decision before code.

## 1. The user's words dominate

Fio preserves what the person actually wrote. It does not improve the prose,
complete an unfinished thought, summarize a period, or translate a memory into
an AI-authored lesson.

When an earlier entry returns, original content, original date, and necessary
context take precedence over interface decoration.

## 2. Selection without interpretation

The machine may help decide which eligible entry to show. It may not decide or
announce what the entry means.

Never display claims such as:

- “You have grown.”
- “This shows a pattern of anxiety.”
- “You were happier then.”
- “Here is what your past self would advise.”

The person is allowed to notice change, contradiction, repetition, or meaning
without the product naming it.

## 3. Time is a feature

Fio does not promise the emotionally “right moment.” It creates restrained
conditions in which an old text can be met again. Windows, spacing, and silence
are preferred to rigid recurrence and artificial certainty.

## 4. Quiet over engagement

Fio can be useful without being frequently opened. It must not manufacture
urgency, guilt, completion pressure, or a habit loop.

Never introduce:

- streaks, goals, points, badges, levels, or completion rings;
- feeds, likes, followers, public profiles, or popularity;
- red badges or repeated reminders for an ignored return;
- “we miss you,” countdowns, or retention guilt;
- content teasers that sensationalize a private memory.

## 5. Autonomy before magic

An unexpected return must never remove user control. A person can pause all
returns, make an entry ineligible, let it rest, dismiss a return, delete data,
and export the archive.

Declining or ignoring a return must be accepted immediately. No survey is
required to exercise a privacy or safety control.

## 6. Private by architecture

Privacy is a system property, not a lock illustration or marketing phrase.
Content and semantic representations stay on device in plaintext form. Any
future server receives only encrypted content blobs and minimum operational
metadata.

Treat embeddings, search queries, imports, revisions, attachments, location,
and voice as private content even when they are not human-readable prose.

## 7. Local and offline first

Writing, reading, saving, editing, searching the local archive, and evaluating
local return eligibility must not depend on server availability. Sync is an
additional durability mechanism, not the authority that makes the diary work.

## 8. Beauty must not compete

Fio may be warm, crafted, and visually distinctive, but the interface must not
perform over the user's words.

- Use sage green, warm off-white, restrained botanical detail, and generous
  space.
- Use decorative or serif typography for identity only when readable.
- Do not use cursive for journal body text.
- Do not use a large lock or envelope animation after saving.
- Respect Reduce Motion and avoid emotional spectacle.

## 9. The Archive is complete but secondary

People retain access to their history. Fio must not simulate forgetting by
hiding or deleting content. At the same time, the Archive is not a feed and
should not become the dominant call to action.

## 10. Simplicity earns complexity

Start with time-based returns. Add semantic selection only behind a feature flag
and only if it provides a meaningful advantage without increasing rejection or
privacy risk.

Technical novelty is not a product benefit by itself. Prefer the smallest
architecture that preserves the promise.

## 11. Trust includes departure

Users can export in durable, human-readable formats and can permanently delete
their data. Fio does not hold personal history hostage to a subscription or a
retention flow.

## 12. Research must not contaminate the experience

Pilot measurement is allowed only when minimal, transparent, and content-free.
The return itself remains quiet. If a resonance question is needed for a pilot,
ask it after the return is closed, not as commentary beneath the memory.

## Product test for any proposal

Ask:

> If this feature disappeared, would autobiographical continuity become poorer,
> or would the application merely become less interesting?

If the answer is only “less interesting,” default to not building it.

Also ask:

1. Does it preserve the user's interpretive authority?
2. Does it work without creating pressure to return?
3. What intimate data does it create, move, expose, or retain?
4. Can it be disabled or removed without loss of the user's words?
5. Can the same outcome be achieved with less interface and infrastructure?
