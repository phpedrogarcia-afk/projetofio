# 04 — Feature catalogue

Status: Scope authority for specification v0.1

This catalogue prevents implementation by inference. A feature's status and
phase must both permit the requested work.

## Status vocabulary

- **Approved** — may be implemented in the listed phase.
- **Experimental** — may be tested only behind a removable feature flag.
- **Planned** — accepted direction, but implementation waits for that phase and
  its prerequisites.
- **Research** — investigate and document; do not ship.
- **Frozen** — retain the concept but do not design or implement it now.
- **Rejected** — do not implement.

Phases are sequencing labels, not release dates:

- **V0** — local pilot and proof of the core return experience.
- **V1** — trusted private beta after V0 exit criteria.
- **V2** — later media and longitudinal experiences after trust is proven.
- **Future** — no committed phase.
- **Never** — outside Fio's product.

## Catalogue

| Capability | Status | Phase | Boundary |
|---|---|---:|---|
| Write plain-text entry | Approved | V0 | No required title, tag, mood, or template |
| Local autosave and offline save | Approved | V0 | Exactly one encrypted active draft; network cannot block writing |
| Edit an entry | Approved | V0 | Preserve timestamps and safe failure behavior |
| Chronological Archive | Approved | V0 | Complete but secondary; never a feed |
| Recently Deleted and permanent delete | Approved | V0 | Recovery window defined in data model |
| Human-readable export | Approved | V0 | One combined UTF-8 plain-text or Markdown document; never a retention hostage |
| Basic TXT/Markdown import | Approved | V0 | Explicit preview, deduplication, and rollback |
| Import one pilot-relevant source format | Experimental | V0 | Choose only from participant evidence |
| Local app lock | Approved | V0 | Platform authentication; not a substitute for encryption |
| Hide content in app switcher | Approved | V0 | Neutral privacy cover |
| Local data encryption boundary | Approved | V0 | Use platform primitives; security review before claims |
| Time-based return | Approved | V0 | Scientific and product baseline |
| Explicit Return consent onboarding | Approved | V0 | Starts `notConfigured`; no migration or per-entry default can enable delivery |
| Generic local return notification | Approved | V0 | “Algo seu voltou.”; no repeat when ignored |
| Pause all unsolicited returns | Approved | V0 | Does not disable writing or Archive |
| Never return this entry | Approved | V0 | Reversible only by explicit user action |
| Dismiss a return | Approved | V0 | No explanation required |
| Pilot resonance question | Experimental | V0 | Separate, skippable sheet after return closes |
| Anonymous content-free pilot telemetry | Experimental | V0 | Must follow `09-ANALYTICS-EXPERIMENTS.md` |
| Semantic Resonance return | Experimental | V0 | On-device, flagged, removable, compared with Time |
| On-device embedding model bake-off | Research | V0 | Do not choose a model by reputation |
| Let an entry rest until a boundary | Approved | V1 | Eligibility resumes later; no immediate guarantee |
| Remember later once | Approved | V1 | One requested window, never recurring habit logic |
| Sealed entry | Approved | V1 | Extra local authentication, independent of return policy |
| Search in my words | Approved | V1 | User-initiated; local textual search first |
| Semantic local search | Experimental | V1 | No server query or content telemetry |
| Entry revision history | Planned | V1 | Recovery-oriented, not an editing analytics surface |
| Encrypted multi-device sync | Planned | V1 | Server stores opaque ciphertext only |
| Encrypted backup and recovery | Planned | V1 | Key recovery requires a separately approved design |
| Day One or Evernote import | Planned | V1 | Prioritize only with real user evidence |
| Keep nearby | Research | V1 | Must not become ranking or affect returns silently |
| Recurrence return | Experimental | V1 | Several related entries across time, no conclusion |
| One optional photo per entry | Planned | V2 | Not albums, filters, or a photo feed |
| One optional audio recording | Planned | V2 | No autoplay; local transcription only if justified |
| Optional autobiographical location | Research | V2 | Explicit opt-in; never a psychological trigger |
| Bridge: two entries across time | Research | V2 | Show excerpts without claiming change or growth |
| Long Thread: several recurrences | Research | V2 | No generated story or inferred theme label |
| OCR for handwritten imports | Research | Future | Private/on-device feasibility first |
| Book generated from the archive | Frozen | Future | Separate product and consent problem |
| Legacy / “after me” transfer | Frozen | Future | Separate legal, identity, key, and consent system |
| AI chat or companion | Rejected | Never | Conflicts with selection-without-interpretation |
| AI-written insights or life summary | Rejected | Never | User retains interpretive authority |
| Psychological or mood profiling | Rejected | Never | Sensitive inference and product conflict |
| Streaks, goals, points, badges | Rejected | Never | Creates obligation and engagement pressure |
| Feed, likes, followers, public sharing | Rejected | Never | Fio is private and autobiographical |
| Advertising or sale of personal data | Rejected | Never | Violates trust model |
| Tasks and calendar productivity | Rejected | Never | Different user job |
| Engagement reminders and red badges | Rejected | Never | Ignored return ends the notification attempt |
| Server-side plaintext matching | Rejected | Never | Server must not read journal content or embeddings |

## V0 frozen scope

V0 proves the smallest complete loop:

```text
WRITE → SAVE LOCALLY → ARCHIVE → TIME RETURN → CONTROL → OPTIONAL PILOT FEEDBACK
```

Required V0 user outcomes:

1. A person can write, close the app, and recover the entry offline.
2. A person can find all of their entries chronologically.
3. A person can import a small existing history with a preview and safe rollback.
4. An eligible entry can return once through a quiet local notification.
5. The person can dismiss it, prevent it from returning, or pause all returns.
6. The person can export and delete their data.
7. A pilot build can compare Time and Resonance without changing the visible
   return experience or collecting content.

Not required for V0:

- account creation or a production backend;
- cross-device sync;
- photos, audio, location, OCR, rich text, or collaboration;
- a final embedding model;
- Recurrence, Bridge, Long Thread, Book, or Legacy;
- monetization.

## Change rule

To change a status, phase, or boundary:

1. cite the user evidence or technical constraint;
2. identify affected principles and privacy surfaces;
3. update or add an Accepted decision in `DECISIONS.md`;
4. update the roadmap and relevant domain specification;
5. only then implement.
