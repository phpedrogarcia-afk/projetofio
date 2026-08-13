# 11 — Founder vision

Status: Canonical long-term direction; not implementation authorization

Date recorded: 2026-08-13

## Why this document exists

This document preserves the founder's complete intended direction for Fio so
that a later human or Codex agent does not mistake a small current milestone for
the whole product.

The founder wants the coherent Fio capability family to remain reachable over
time, including delivery after death. A capability may later be explicitly
removed, revised, or narrowed, but it must not disappear from the project merely
because it is not safe or appropriate to implement now.

This is a direction record, not permission to design or build every item. The
status and phase in `04-FEATURES.md`, Accepted decisions in `DECISIONS.md`, and
the gates in `10-ROADMAP.md` continue to control implementation.

## Origin

Fio began with a simple autobiographical experience: something emotionally
important was written, forgotten, and later rediscovered by the same person
with enough distance to read it differently.

The product is therefore not primarily a note organizer. It is a place where a
person may entrust something to time.

Long-term external expressions worth preserving:

> Quando existir alguma coisa que você queira confiar ao tempo, coloque aqui.

> Fio é um lugar bonito e privado para guardar coisas que você não quer perder
> — e decidir o que o tempo deve fazer com elas.

The exact public copy may change through research. The underlying idea may not
be silently replaced by generic productivity or an AI companion.

## Core invariant across every phase

Fio may preserve, protect, wait, select, organize, return, and—after separately
approved safety work—deliver the person's own material.

Fio does not author the person's meaning. It does not rewrite a life, diagnose
it, explain it, or tell the person what a memory, decision, promise, or letter
means.

The durable relationship is:

```text
PERSON WRITES → FIO KEEPS → TIME ACTS → PERSON INTERPRETS
```

## The complete capability family

### 1. Keep

A person may write freely without first deciding whether the material is a
note, idea, phrase, list, letter, worry, draft, decision, promise, question, or
memory. The complete chronological Archive remains available. Organization is
optional and secondary.

### 2. Return

Fio may occasionally return the person's own earlier words after time has
created distance. Time-based returns remain a complete product even without a
semantic model. The person controls pause, dismissal, Never Return, Rest, and
deletion.

### 3. Keep until a chosen time

The intended future action `Guardar até…` allows a person to choose a boundary
before which an item is not normally available to open. This is a time-trust
feature, not a task reminder.

Before implementation, a separate decision must define whether the promise is
only an interface restriction or a stronger cryptographic release design. It
must also define recovery, clock rollback, reinstall, device replacement,
backup, lost-key, accessibility, emergency, and deletion behavior. Fio must not
promise technical impossibility that it cannot prove.

### 4. Send to oneself in the future

A person may intentionally write a future letter, question, decision rationale,
or promise and choose when it becomes available or returns. It is one
user-requested event, not a recurring calendar, habit system, or engagement
notification.

Examples include:

- a letter to a future self;
- the reasons behind a difficult decision, reopened later without judgment;
- a promise the person chose to revisit;
- a question deliberately left for a later version of the person.

Fio returns the original words and context. It does not score whether the
decision was correct or whether the promise was fulfilled.

### 5. Send to another living person in the future

The founder wants future correspondence to another person to remain part of the
long-term direction. This crosses Fio's current private, single-user boundary
and therefore remains Frozen.

It requires its own recipient identity, consent, revocation, delivery,
encryption/key-release, changed-relationship, abuse-prevention, deletion,
failed-delivery, and support design. No recipient data, schema, flow, or API is
authorized by this document.

### 6. Deliver after death — Legacy

Posthumous delivery is an explicit founder goal, not a rejected idea. A person
may eventually be able to prepare selected material or a message for selected
people after death.

Legacy remains Frozen under ADR-019. Before any implementation can be approved,
the project must close at least these gates:

1. prove the private local core and long-term data reliability;
2. obtain legal review for supported jurisdictions and digital inheritance;
3. define credible death verification, delay, dispute, and false-positive
   handling;
4. authenticate recipients and obtain appropriate consent;
5. support revocation and changes in relationships, recipients, and intent;
6. design end-to-end key custody or release without silently giving the service
   plaintext access;
7. define recovery, device loss, account compromise, and operator/company
   failure behavior;
8. address coercion, stalking, domestic abuse, minors, impersonation, and
   malicious content;
9. preserve export, deletion, auditability, and privacy without content
   telemetry;
10. complete independent security, privacy, legal, and emotional-safety review.

Until a new Accepted decision supersedes or narrows ADR-019, agents must not
create Legacy schemas, APIs, delivery flows, prototypes involving real people,
or marketing promises.

### 7. Preserve in other forms

Optional photographs, the person's voice, selected location context, and a
future Book may deepen autobiographical preservation. They remain subject to
their catalogue status and their own privacy, consent, storage, export, and
return-safety work. A Book must present the person's material, not an AI-authored
life interpretation.

## Product layers

The intended sequence is conceptual, not a release-date promise:

```text
MEMORY
write, keep, archive, import, export, return
        ↓
TIME CONTROL
rest, remember later, seal, keep until, future self
        ↓
PRIVATE CORRESPONDENCE
deliver selected material to another living person
        ↓
LEGACY
carefully governed delivery after death
```

Each layer must remain useful without the next. A later layer may not weaken
the privacy, autonomy, readability, or non-interpretation of the earlier one.

## What “reach all the ideas” means

It means retaining and, when the evidence and gates permit, exploring all ideas
that belong to Fio's relationship with memory, time, private correspondence,
and autobiographical continuity.

It does not reverse the Rejected list. Fio still does not become a generic
Evernote clone, task manager, calendar, social network, engagement game,
advertising surface, AI therapist, or AI-written life story. OCR, media, search,
sync, and imports are valuable only when they help preserve or recover the
person's own material without making organization the product.

## Visual direction to preserve

Fio should feel like a personal object rather than a technology dashboard. The
visual metaphor is restrained `glass + paper + light`:

- warm off-white background, sage green, and soft charcoal;
- subtle translucent surfaces, not heavy glassmorphism;
- restrained botanical details around empty space, never over the person's
  writing;
- editorial vertical entries rather than a dense card grid;
- simple navigation centered on writing, Archive, things entrusted to time, and
  settings;
- quiet confirmation such as `Guardado.`;
- subtle motion and `Algo seu voltou.` without spectacle or urgency;
- no cursive body text, large lock, confetti, or theatrical save ritual.

An earlier conversation generated a multi-screen visual reference containing
Write, a saving transition, Saved, Archive, Return feedback, and entry-reading
states. The founder recovered that reference on 2026-08-13; it is preserved as
`assets/fio-visual-reference-v1-recovered-2026-08-13.png`. Its approved visual
inheritance is the warm editorial identity, restrained botanical detail,
`Guardado.`, and the personal-object quality. Its iPhone shell, embedded Return
feedback, notification subtitle, and Archive floating action button are not
current product direction.

The Android-first reconstruction
`assets/fio-visual-reference-v2-android-candidate-2026-08-13.png` corrects those
known conflicts and adds the Settings surface. It is a review candidate, not a
new product decision or implementation authorization. `03-UX.md`, Accepted
decisions, privacy requirements, accessibility requirements, and roadmap gates
remain authoritative even after a visual candidate is approved.

## Removal and revision rule

The founder may later decide that any Planned, Research, or Frozen capability
should be removed. That change must be explicit and recorded in a new decision;
do not erase the earlier intent or rewrite decision history. Until then,
“Frozen” means preserved but not authorized—not forgotten and not secretly
implemented.
