# 09 — Analytics and experiments

Status: Canonical research boundary for specification v0.1

## Research principle

Fio needs evidence about the return experience without observing the contents
of a person's life. Research is allowed to measure product outcomes, not to
profile users or optimize engagement.

The return itself remains quiet. Measurement occurs after the experience and is
optional. No experiment can weaken deletion, pause, Never Return, encryption,
or content boundaries.

## Questions V0 must answer

1. Can people trust and understand the write → wait → return loop?
2. Does at least one return feel meaningfully different from browsing an old
   note during a 60–90 day pilot?
3. Does semantic Resonance improve that outcome beyond an age-matched Time
   return by enough to justify model, battery, privacy, and maintenance cost?
4. Does Resonance increase rejection, discomfort, or “creepy” reactions?
5. Can all useful pilot measurement remain free of journal content and
   content-derived labels?

## What success is not

Do not optimize or report the pilot primarily by:

- daily/monthly active users;
- time in app or session length;
- number of entries per person;
- notification click-through maximization;
- retention loops or streak completion;
- volume of generated returns;
- number of semantic clusters/topics.

These metrics can push Fio directly against its purpose.

## Core outcome metrics

### Return Open Rate

```text
opened Returns / valid notification attempts
```

This is operational context, not the product's north star. A lower rate is not a
license to send more notifications.

### Feedback Response Rate

```text
Returns with yes/no answer / opened Returns that showed the pilot question
```

Report this so missing/skipped feedback is not silently treated as “No.”

### Resonant Return Rate

Primary conservative definition:

```text
“Yes” answers / opened Returns that showed the pilot question
```

Also report `Yes / answered Returns` as a secondary view. Never substitute the
secondary denominator without labeling it.

### Rejection Rate

```text
Returns followed by Never Return for that Entry / opened Returns
```

Rest, pause, delete, and qualitative discomfort are reported separately; they
must not be collapsed into a positive engagement metric.

### Meaningful User Rate 60

```text
eligible participants with at least one “Yes” in first 60 days
────────────────────────────────────────────────────────────────
eligible participants observed through the 60-day window
```

This is the closest quantitative proxy for the core thesis in V0. Qualitative
interviews determine whether “Yes” meant mild novelty or genuine re-encounter.

### Strong Resonance — qualitative

In a separately consented interview, researchers may code reactions on a
predefined rubric, for example:

- 0 — no significance;
- 1 — mild recognition or novelty;
- 2 — meaningful autobiographical connection;
- 3 — strong, specific change in how a past moment was understood.

Interview notes are not product analytics, are not stored in Fio, and require a
separate research retention/access policy. Do not ask an AI to diagnose them.

## V0 pilot design

### Stage A — Time-only feasibility

Before semantic comparison, validate:

- save and Archive reliability;
- import preview/rollback;
- first-return bootstrap timing;
- generic notification comprehension;
- pause/Never Return/export/delete controls;
- feedback sheet does not contaminate the Return screen.

Time alone must form a coherent product.

### Stage B — Time versus Resonance

- Exploratory sample: approximately 20 participants.
- Duration: 60–90 days.
- Design: within-subject, counterbalanced algorithm order.
- Blinding: participant sees identical UI/copy for both arms.
- Control: Time candidate comes from the same age bucket as the Resonance
  candidate/comparable opportunity.
- Cadence: maximum one unsolicited notification attempt per rolling seven days
  in the pilot.
- Feedback: separate skippable Yes/No sheet after Return closes.
- Interviews: scheduled outside the return moment and separately consented.

With a small sample, treat results as directional. Do not repeatedly change
thresholds until a favorable `p` value appears. Record versions and examine
uncertainty.

## Assignment

Generate a study-scoped random participant identifier and a counterbalanced arm
sequence. The assignment is fixed independently of opens, answers, writing
frequency, or content.

Examples:

```text
Sequence A: Time → Resonance → Time → Resonance
Sequence B: Resonance → Time → Resonance → Time
```

If the assigned arm cannot produce a qualified candidate:

- fall back according to the versioned engine policy;
- record assigned arm and delivered algorithm separately;
- never search for a more “engaging” alternative based on prior behavior;
- keep user-facing experience unchanged.

## Decision rule for Semantic Resonance

Semantic selection must beat Time by enough to pay for its existence.

Evaluate the estimated difference in Resonant Return Rate alongside uncertainty,
Strong Resonance interviews, Rejection Rate, operational cost, and privacy.

| Estimated Resonance advantage over age-matched Time | Default decision |
|---|---|
| 5 percentage points or less | Remove/disable Semantic Resonance |
| More than 5 but less than 10 points | Inconclusive; do not promote to core |
| At least 10 points | Continue investigation if safety metrics do not worsen |
| At least 10 points plus more Strong Resonance | Serious candidate for V1 |
| Any lift with materially higher rejection/discomfort | Stop and investigate |

Prefer a Bayesian expression such as “probability the true advantage is at
least 10 points,” but do not let a statistical method hide small samples or
missing feedback.

### Immediate stop conditions

Disable the semantic experiment regardless of lift if:

- plaintext or embeddings leave the device unexpectedly;
- an analytics payload contains content or a content-derived label;
- the model requires an unapproved network call;
- participants report repeated invasive or unsafe-feeling selection;
- battery, memory, thermal, or app stability impact exceeds approved budgets;
- deletion fails to purge semantic derivatives;
- the Time fallback is not independently functional.

## Fio Portuguese embedding benchmark

Do not choose an embedding model from a general leaderboard alone. Create a
small benchmark for real PT-BR autobiographical phrasing before enabling
Resonance broadly.

Suggested design:

- about 300 purpose-written or explicitly consented text pairs;
- no production journal export by default;
- human rating from 0 (unrelated) to 3 (strongly related);
- include paraphrase, indirect relation, superficial lexical overlap,
  near-duplicate, negation, short/long entry, and unrelated controls;
- document annotator instructions and disagreement;
- keep a held-out set for final selection.

Compare at least:

- a deterministic non-semantic lexical control that works offline and makes
  superficial-overlap/near-duplicate failures visible;
- a compact multilingual encoder packaged for on-device use;
- a Portuguese-focused encoder if its license and device profile are suitable.

Under Android ADR-040, corpus construction targets are rehearsal labels only.
Freeze scenario families across development/held-out splits, require two
independent human ratings per held-out pair, and report disagreement. A
PT-focused base language model without a reviewed sentence-level objective is
not an eligible embedding candidate merely because it was trained in PT-BR.
ADR-041 uses the permitted early-stop path: no V0 model is selected, so the
held-out human gate is retained for future reopening rather than fabricated or
completed merely to reject the current candidate.

Evaluate:

- ranking agreement with human judgments;
- top-k retrieval quality and near-duplicate errors;
- model/download size;
- on-device latency, memory, energy, and thermal behavior;
- offline availability, OS/device coverage, and license;
- index migration/rebuild behavior.

The winning benchmark model is still an experiment adapter, not permission to
interpret user content.

## Telemetry architecture

Pilot telemetry is opt-in/consented and uses a study-scoped identity. It must
not reuse advertising identifiers or create cross-product profiles.

Common allowed fields:

```text
eventSchemaVersion
studyVersion
studyParticipantId       // random, study-scoped
studyReturnId?           // random, not Entry ID
appVersion
osMajorVersion
occurredDay              // coarsened; exact time stays local unless essential
```

Every event has a closed property schema. There is no arbitrary `metadata`,
`context`, `label`, `message`, or free-text field.

## Allowed V0 pilot events

### `pilot_enrolled`

Allowed properties:

```text
consentVersion
assignmentSequence
historyMode: imported | new
```

Do not send exact imported-entry count or source file name.

### `return_scheduled`

Allowed properties:

```text
studyReturnId
assignedArm: time | resonance
deliveredAlgorithm: time | resonance
fallbackReason: none | noQualifiedCandidate | modelUnavailable
algorithmVersion
ageBucket
daysSinceEnrollmentBucket
```

### `return_opened`

Allowed properties:

```text
studyReturnId
assignedArm
deliveredAlgorithm
algorithmVersion
ageBucket
openLatencyBucket
```

### `return_feedback_submitted`

Allowed properties:

```text
studyReturnId
answer: yes | no
questionVersion
```

### `return_rejected`

Allowed properties:

```text
studyReturnId
control: neverReturn
```

### `pilot_withdrawn`

Allowed properties:

```text
consentVersion
```

Do not require or transmit a withdrawal reason.

Events such as Entry saved/edited text length, search performed, Archive browsed,
screen time, keystrokes, topics, or location are not needed for the V0 question
and are not allowed.

## Never collect

- Entry text, title, snippet, draft, or revision;
- search query or import/export filename;
- embedding, similarity score, nearest-neighbor list, or model input;
- inferred topic, sentiment, emotion, diagnosis, person, or place;
- attachment, transcription, EXIF, or location;
- exact autobiographical date or exact age when a bucket is sufficient;
- screen recording, session replay, heatmap, touch path, clipboard, or keyboard
  content;
- Contacts, Calendar, Health, Photos, microphone, or device content unrelated to
  an explicit approved feature;
- advertising/cross-app tracking identifiers;
- arbitrary error messages containing database values.

## Qualitative research

Interview participants after enough distance from an individual Return. Use
open questions such as:

- What, if anything, was it like to encounter that entry again?
- Did any return feel intrusive or poorly timed?
- Did the controls make sense?
- Did knowing about the experiment change how you wrote?
- What would make you stop trusting Fio?

Do not prompt participants toward “growth,” healing, or a desired emotional
reaction. Keep interview data separate from production content and participant
identity wherever practical.

## Analysis requirements

- Pre-register event schemas, metric definitions, engine version, buckets, and
  decision thresholds before the comparative stage.
- Report missing data, skipped questions, withdrawals, fallback rate, and
  notification-denied cases.
- Analyze assigned arm (intention-to-treat view) and delivered algorithm
  separately.
- Pair results within participant where appropriate.
- Show uncertainty, not only point estimates.
- Never inspect journal text to explain an unfavorable result.
- Do not change notification frequency to rescue open rate.

## Retention and participant rights

Before enrollment, define:

- study purpose and exact event list;
- who can access study telemetry;
- retention period and deletion/withdrawal behavior;
- whether already aggregated results can be removed;
- contact and incident process.

Withdrawal stops future upload without disabling Fio. Where feasible, delete
study-linked raw events using the study-scoped identifier. Product Archive and
study telemetry remain separate systems.

## Experiment lifecycle

Every experiment has:

1. a named owner and question;
2. approved event schema and consent version;
3. fixed feature flag and cohort rule;
4. engine/model version;
5. operational and privacy budgets;
6. stop conditions;
7. analysis and decision date/window;
8. removal plan for flags, code, events, and derived data;
9. a final Accepted decision documenting keep, revise, or remove.

An experiment that ends must not leave dormant data collection behind.
