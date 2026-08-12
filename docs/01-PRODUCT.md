# 01 — Product

Status: Canonical for specification v0.1

## Definition

Fio is a private autobiographical memory application. It gives a person a quiet
place to write freely and, after enough time has passed, occasionally returns
their own words without explaining them.

Internal definition:

> An instrument of autobiographical continuity.

Simple external expression:

> Escreva hoje. Encontre isso outra vez no futuro.

Canonical promise:

> O Fio não interpreta sua história. Ele a devolve.

Fio uses time as part of the product. Its value does not come primarily from
capturing more notes, but from enabling a later encounter with a version of the
person that would otherwise be forgotten.

## The problem

People forget not only events, but how they actually thought, felt, doubted,
and described their lives at the time. Conventional note apps preserve text but
usually depend on the person remembering to search for it. Habit-oriented
journals optimize regular writing. AI journals often add interpretations.

Fio focuses on a narrower problem: helping someone unexpectedly rediscover
their own words after temporal distance has changed the reading.

## Core loop

```text
WRITE → KEEP → TIME PASSES → RETURN → RE-ENCOUNTER → LIFE CONTINUES
```

The loop must remain complete even if semantic AI is removed. Time-based
returns are the baseline product; semantic selection is a testable hypothesis.

## User job

The user comes to Fio to:

- record what is passing through their mind without organizing it;
- trust that their words remain available and private;
- meet an earlier version of themselves after enough time has passed;
- keep control over what may return and when returns should pause;
- take all of their data with them or delete it.

The user does not come to Fio to:

- complete a daily habit;
- increase productivity;
- receive advice, diagnoses, summaries, or life lessons;
- talk to an AI;
- perform for an audience;
- build a public identity;
- maximize time in an app.

## Core product surfaces

### Write

One calm prompt, a readable editor, and a clear save action. Writing works
offline. No required tags, mood, title, category, goal, or schedule.

### Saved

Saving is immediate and quiet. Confirmation must not become a reward ritual.
No large lock animation and no theatrical security claim.

### Archive

The complete personal archive remains accessible because silence must not mean
loss of control. It is chronological and secondary to writing, not a feed or a
homepage to browse compulsively.

### Return

A return presents an earlier entry with its original date and words. It offers
control, not interpretation. Production UX contains no explanation of why the
algorithm selected it.

### Import and export

Import helps users bring an existing autobiographical history into Fio. Export
must be understandable outside Fio and must not be held behind retention
friction.

## Return vocabulary

These actions are distinct:

- **Never return**: the entry remains in the archive but is permanently
  ineligible for unsolicited returns unless the user explicitly reverses it.
- **Let rest**: the entry is ineligible until a chosen boundary. After that it
  becomes eligible; it does not have to appear immediately.
- **Remember later**: the user explicitly asks for a return around a future
  date/window. This is a priority request, not a recurring productivity
  reminder.
- **Seal**: require an additional local authentication step before content is
  shown. Sealing does not replace encryption and does not by itself change
  return eligibility.
- **Pause returns**: globally stop unsolicited returns without stopping writing,
  reading, import, export, or sync.

## Role of AI

AI may:

- generate local embeddings;
- rank eligible entries;
- find semantic proximity or recurrence;
- support user-initiated search;
- help evaluate candidate models using consented test data.

AI must not:

- write or rewrite journal content;
- assign psychological meaning, mood, diagnosis, progress, or personality;
- produce a narrative about the user's life;
- explain a return with scores, topics, or inferred emotions;
- optimize notifications for engagement;
- require plaintext content to leave the device.

The governing distinction is:

> Machine intelligence may help select. Human intelligence keeps the right to
> interpret.

## First product proof

Before building a broad platform, prove one thing:

> When a forgotten sentence returns after time has passed, does it create a
> meaningfully different experience from merely browsing an old note?

The first product must therefore work without advanced AI:

1. write and edit safely;
2. keep a complete local archive;
3. import a small existing history;
4. schedule a quiet time-based return;
5. let the user reject, rest, pause, delete, and export;
6. collect minimal pilot feedback without observing content.

## Success

Success is not daily active use or time on screen. The central pilot outcome is
whether a participant experiences at least one genuinely meaningful return
during the study period.

Useful product signals are defined in `09-ANALYTICS-EXPERIMENTS.md`. They are
subordinate to privacy and cannot justify engagement mechanics.

## Business posture

Fio rejects advertising and sale of personal data. A simple paid model such as
an annual subscription or purchase may fit the trust promise, but monetization
is not decided in v0.1. Export cannot be used as a retention hostage.

## Boundaries

The Book and Legacy concepts may be emotionally coherent future consequences,
but each introduces a separate product, legal, security, and consent problem.
They remain Frozen until the core return experience and trust model are proven.
