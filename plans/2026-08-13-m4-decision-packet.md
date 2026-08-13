# M4-R1 — Portuguese semantic benchmark decision packet

Status: Accepted — explicitly approved by the project owner on 2026-08-13
Date: 2026-08-13
Authority boundary: ADR-015 and ADR-037 permit synthetic-data M4 research, but
ADR-024 defers every model choice until benchmark, device, license, and held-out
evidence exists.

## Recommendation

1. **Research only.** M4 lives in a removable research boundary and never
   connects to production Returns, Archive selection, notifications, analytics,
   or personal content.
2. **Corpus.** Author about 300 purpose-written PT-BR autobiographical pairs,
   with no production journal export. Freeze 60 pairs as a held-out set before
   scoring candidates. Include paraphrase, indirect relation, lexical traps,
   near-duplicates, negation, short/long text, and unrelated controls.
3. **Human labels.** Use the canonical 0–3 rubric. Automated fixture generation
   may prepare candidates, but it cannot claim human semantic truth. Final
   selection requires at least two independent ratings per held-out pair,
   disagreement reporting, and owner approval of the rubric/corpus boundary.
4. **Candidate freeze.** Before downloading or integrating anything, record for
   each candidate its exact artifact/version/hash, source, license, tokenizer,
   languages, quantization, input limit, binary/model size, minimum Android
   support, runtime/transitive dependencies, security provenance, and removal
   path. Compare: a non-semantic lexical control, one compact multilingual
   on-device encoder, and one PT-focused option only if its license/device
   profile passes review. No hosted inference is eligible.
5. **Adapter boundary.** Fio-owned inputs/outputs surround every runtime. No
   journal text, embedding, score, nearest-neighbor list, or model input leaves
   the device. Benchmark assets stay synthetic and separate from Room schema 3.
6. **Evaluation.** Report rank correlation, nDCG/top-k retrieval, lexical-trap,
   near-duplicate and negation failures, annotator disagreement, and held-out
   uncertainty. A leaderboard result alone cannot select a model.
7. **Device evidence.** Measure cold/warm latency, peak memory, model/package
   size, energy/thermal behavior, offline operation, and API 26/API 36 support.
   Emulator results prove compatibility only; real performance claims require
   labeled physical-device evidence. The POCO may use synthetic text only.
8. **Fail closed.** If no candidate has acceptable quality, license, provenance,
   offline coverage, or device cost, record `no model selected` and retain Time.
   A research winner remains an experiment adapter and does not authorize M5.

## Approval effect

Approval authorizes the Draft M4 plan, synthetic corpus/rubric tooling, a
candidate inventory, and offline benchmark adapters after each dependency and
license is recorded. It does not accept ADR-024, select a model, authorize
personal text, connect semantics to Returns, start M5, create telemetry, use a
hosted service, or authorize Git/signing/publication/release.

Exact approval phrase:

> Aprovo o pacote Android M4-R1 — Benchmark semântico PT-BR.

Approval received verbatim on 2026-08-13. Recorded as ADR-040. This approval
does not accept deferred ADR-024 or select an embedding model.
