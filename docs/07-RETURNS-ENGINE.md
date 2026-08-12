# 07 — Returns engine

Status: Canonical V0 behavior; Semantic Resonance remains experimental

## Purpose

The returns engine decides **whether to remain silent**, which eligible entry to
offer, and when to schedule the offer. It never decides what an entry means.

A successful engine is not the one that produces the most returns. It is the
one that preserves trust, respects explicit controls, and occasionally enables
a meaningful re-encounter.

## Terms

- **Entry** — original autobiographical content.
- **Unsolicited return** — Fio initiates the opportunity without a user-requested
  date.
- **Requested return** — a one-time Remember Later window explicitly selected
  by the user (V1).
- **Eligibility** — all hard rules allowing an Entry to be considered now.
- **Candidate** — an eligible Entry proposed by one algorithm.
- **Return** — the persisted selected attempt with a delivery window.
- **Time** — age-controlled random baseline.
- **Resonance** — experimental on-device semantic proximity to a recent entry.
- **Recurrence** — later experiment involving related entries across separated
  moments; not part of V0.

## Governing rules

1. Silence is always a valid result.
2. Explicit user policy beats algorithm score.
3. The engine operates locally on decrypted data in memory.
4. Notification content is generic and contains no Entry material.
5. The visible Return is identical across experimental algorithms.
6. An ignored Return is not notified again.
7. No algorithm uses sentiment, diagnosis, emotion classification, life-event
   inference, location triggers, or engagement prediction.
8. Time remains a complete production-capable fallback if semantic code is
   disabled, unavailable, or deleted.

## Inputs

The engine receives explicit dependencies:

- current clock, local calendar, locale, and timezone;
- all non-purged Entry metadata and decryptable candidate content as needed;
- Return history, including scheduled/expired/cancelled attempts;
- global settings and notification permission;
- typed experiment flags and assignment;
- deterministic random source;
- optional embedding/matching adapter;
- background opportunity constraints supplied by iOS.

It does not read analytics results during selection. Product experiments cannot
adapt per user to maximize opens.

## Hard eligibility

An Entry is eligible for an unsolicited return at instant `now` only when all
of the following are true:

```text
entry.deletedAt is null
AND entry.returnMode == eligible
AND (entry.restUntil is null OR entry.restUntil <= now)
AND entry.originalCreatedAt <= minimumAgeBoundary
AND no pending Return already references entry
AND entry passes per-entry repeat policy
AND content can be authenticated and decrypted locally
```

Global eligibility must also be true:

```text
returnConsentState == enabled
AND pilot/onboarding bootstrap threshold is met
AND global frequency cap has room
AND no other Return is pending for the same opportunity
AND delivery can fall within user/system constraints
```

Notification denial does not make content ineligible; it prevents notification
scheduling. The app may surface at most one quiet pending-return affordance when
opened, but must not create badges or a queue of missed returns.

Milestone 1 stores `returnConsentState = notConfigured` and delivers no
Returns. Only explicit Milestone 2 onboarding can transition that state to
`enabled`. A migration, per-entry `returnMode = eligible`, notification
permission, or feature flag cannot stand in for consent. `paused` is valid only
after Returns were configured and blocks all delivery until an explicit resume.

### Re-check before display

Eligibility is checked again when a Return is opened. If the Entry was deleted,
made Never Return, put to Rest, or cannot be authenticated, cancel safely and do
not reveal cached content.

## Policy precedence

From highest to lowest:

1. permanent deletion/purge;
2. `returnMode = never`;
3. global consent state (`notConfigured` or `paused` blocks delivery);
4. active Rest boundary;
5. explicit one-time Remember Later window;
6. existing pending Return;
7. frequency and repeat caps;
8. experiment assignment and algorithm selection.

If the user sets Never Return, clear any requested window and cancel pending
Returns for that Entry. If the user deletes an Entry, cancel all pending Returns.

V1 UI must not allow overlapping Rest and Remember Later windows without making
the user choose which intention should win.

## Bootstrap rules for V0 pilot

The benefit is delayed by design, but the pilot still needs a coherent first
opportunity. Use randomized windows, never exact recurring weekdays.

### Participant with imported history

If at least three eligible imported Entries are older than 30 days, and the
participant creates their first native Entry:

- first return opportunity: randomly within 24–72 hours after that native save;
- do not return immediately in the save session;
- imported and native Entries still pass all policy and experiment rules.

### Participant starting from zero

| Local history | First opportunity |
|---|---|
| At least 4 entries and oldest is at least 7 days | Randomly during days 7–14 |
| 2–3 entries | Randomly during days 14–21 |
| Exactly 1 entry | Randomly during days 30–45 |
| No entries | No return |

The day ranges are measured from the oldest eligible native Entry. A later Entry
does not reset the clock merely to drive engagement.

### After first return

During the V0 research build:

- at most one unsolicited notification attempt in any rolling seven-day period;
- at most one pending Return at a time;
- do not select the same Entry twice during the 60–90 day pilot;
- if no valid candidate exists, remain silent and try at a later background/app
  opportunity without notifying the user.

This is a research cadence, not a final promise. A public product will likely be
quieter and requires a separate Accepted decision.

## Candidate generation

### 1. Requested — V1

An Entry with an active user-requested window is a priority candidate inside
that window, subject to deletion, Never Return, authentication, and disclosed
global pause behavior.

It is one-time. After an attempt is opened or expires, clear the requested
window. Never turn it into a recurring reminder.

### 2. Time — Approved V0

Time chooses randomly among eligible Entries in a controlled age bucket. It is
both a real product mechanism and the experimental control.

V0 age buckets:

```text
7–29 days
30–89 days
90–179 days
180–364 days
365–729 days
730+ days
```

Buckets can be revised only with a version change and test updates. Selection
within a bucket uses the injected random source and must be reproducible in tests.

Time does not favor entries by length, inferred topic, sentiment, or prior open
behavior.

### 3. Resonance — Experimental V0

Resonance uses an on-device embedding adapter to find an older eligible Entry
that is semantically related to a recent user-authored Entry (the anchor).

Constraints:

- only consented pilot builds with the flag enabled;
- plaintext and embeddings remain on device;
- a missing/stale embedding schedules local reindexing but never blocks core
  product behavior;
- exclude the anchor itself and near-duplicate imports;
- require minimum temporal distance and sufficient semantic proximity;
- avoid selecting only the maximum cosine neighbor when it is a superficial
  duplicate; diversity rules may prefer “similar enough, older, and not nearly
  identical”;
- never attach an inferred label or explanation to the Return;
- if no qualified Resonance candidate exists, fall back to Time and record only
  a content-free fallback reason locally.

The exact model and thresholds are deliberately undecided until the Fio
Portuguese benchmark described in `09-ANALYTICS-EXPERIMENTS.md`.

### 4. Recurrence — Experimental V1

Recurrence may eventually select several related Entries separated in time. It
must display original fragments without naming a topic or producing a
conclusion. It is not implemented or measured in the first V0 experiment.

### Excluded: Contrast

There is no Contrast algorithm in V0. Detecting “before vs after” encourages
emotion/change classification and machine interpretation. Contrast may emerge
for the person naturally when two words are related; the system does not name it.

## Controlled Time versus Resonance experiment

The experiment is within-subject and counterbalanced: the same participant can
receive Time and Resonance Returns in an assigned order. The user-facing
experience is identical.

Age must be controlled. When comparing a Resonance candidate, Time selects from
the same age bucket. This prevents the experiment from confusing the effect of
age with the effect of semantic matching.

Suggested flow:

1. Experiment allocator selects the next arm according to a fixed,
   counterbalanced assignment; it does not respond to opens.
2. Determine an eligible age bucket available to both arms.
3. Resonance arm selects a qualified semantic candidate in that bucket.
4. Time arm selects randomly in that bucket.
5. If the assigned arm cannot produce a candidate, apply the documented
   fallback and preserve the original assignment for analysis.
6. Schedule with the same delivery rules and UI.

Do not create a separate participant group for each algorithm in a tiny pilot;
individual differences would dominate the result.

## Selection and scheduling pipeline

```text
function evaluateReturnOpportunity(now):
    if !globalPolicyAllows(now): return SILENT

    reconcilePendingReturns(now)
    if hasPendingReturn(): return SILENT

    eligible = entries.filter(hardEligibility(now))
    if eligible.isEmpty: return SILENT

    requested = requestedCandidate(eligible, now)
    candidate = requested ?? experimentCandidate(eligible, now)
    if candidate is null: return SILENT

    window = deliveryWindow(now, quietHours, systemConstraints)
    if window is null: return SILENT

    persist Return(selected, candidate, window) transactionally
    schedule generic local notification
    mark Return(scheduled) only after scheduler acknowledgment
    return SCHEDULED
```

Scheduling must be idempotent. Re-running after a crash cannot create duplicate
Return records or duplicate notifications.

## Delivery windows

- Use a randomized instant inside an allowed window, not a recurring weekday.
- Respect user quiet hours, notification settings, and platform limits.
- Timezone travel recalculates future local delivery while preserving the
  intended quiet-hour experience.
- Daylight-saving changes cannot create two notifications.
- An overdue window is reconciled; do not emit a burst after a long offline gap.
- Exact delivery time is not promised because iOS background execution is
  opportunistic.

## Notification and opening

Notification content:

> Algo seu voltou.

No subtitle is required. The payload contains only an opaque local Return
identifier. It contains no text, preview, topic, date, algorithm, or emotional
label.

If ignored:

- the Return expires at its window boundary;
- no follow-up notification is sent;
- it does not create a badge or missed-return inbox;
- the same Entry may become eligible again only after the normal product repeat
  policy; during the V0 pilot it is not selected again.

If opened:

- re-check policy and authenticate/decrypt locally;
- show original date and content;
- record `openedAt` locally;
- closing records `dismissedAt` without implying resonance;
- pilot feedback, if enabled, appears afterward and is skippable.

## User actions

### Never show again

Set `returnMode = never`, clear requested window, cancel pending Returns, and
remove the Entry from all candidate indexes. The Entry remains in Archive unless
deleted.

### Let rest — V1

Set `restUntil` and cancel pending unsolicited Returns for the Entry. At the
boundary it becomes eligible again; do not schedule it immediately merely
because Rest ended.

### Remember later — V1

Set one requested window. This expresses one return intention, not a task,
calendar event, or recurring reminder.

### Pause returns

After Returns have been configured, set `returnConsentState = paused` and
cancel pending unsolicited notifications. Writing, Archive, export, deletion,
and sync continue. Resume requires an explicit transition back to `enabled`.
V1 must explicitly disclose whether already requested Remember Later windows
are also paused; the safest default is to pause all delivery until the user
resumes.

### Seal — V1

A sealed Entry may remain eligible only if the user allowed returns. The generic
notification remains safe; opening requires local authentication before content
is decrypted/displayed. Failed authentication reveals nothing.

## Failure behavior

- **Embedding/model unavailable:** use Time or remain silent; never upload text.
- **Database unavailable/corrupt:** do not schedule; present recovery path when
  the app opens.
- **Notification permission denied:** do not nag; retain quiet in-app access.
- **Low storage:** preserve original content before derived indexes; skip model
  work and notify only through a local settings status if action is needed.
- **Clock moves backward/forward:** reconcile by stable Return IDs and windows;
  never duplicate.
- **Entry changed after selection:** re-check eligibility and use current
  original user-authored content; cancel if policy changed.
- **Decryption/authentication failure:** reveal nothing, cancel display, log only
  a content-free error code locally.

## Required tests

### Eligibility table

Test every combination that materially changes the result:

- active/deleted/purged;
- eligible/never;
- Rest before/at/after boundary;
- requested window before/inside/after;
- not-configured/enabled/paused global consent, including migration defaults;
- pending/no pending Return;
- frequency cap open/full;
- notification allowed/denied;
- decryptable/tampered content.

### Determinism and fairness

- fixed clock and random seed produce the same candidate/window;
- Time samples only from the chosen age bucket;
- experimental arms use matching buckets;
- user open behavior does not change arm assignment;
- Resonance disabled or removed leaves Time fully functional.

### Idempotency

- repeated background evaluation creates at most one Return/notification;
- crash between persist and schedule reconciles safely;
- policy change cancels the correct pending notification;
- ignored Return generates no reminder.

### Privacy

- notification contains only canonical generic copy and opaque local ID;
- reason codes and scores never reach UI or analytics;
- semantic processing makes no network request;
- logs contain no Entry text, embedding, query, or inferred label.

## Versioning

Persist `algorithmVersion` with each Return. Any change to eligibility, buckets,
thresholds, fallback, diversity, or frequency increments a documented version.
Historical experiment results must never be compared as if different versions
were identical.
