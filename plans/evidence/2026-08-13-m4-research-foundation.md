# M4 PT-BR benchmark — research foundation evidence

Status: Automated foundation complete; corpus/rubric and human-rating gates open
Date: 2026-08-13
Authority: Approved M4-R1, ADR-040, and the M4 research plan

## Claim boundary

This evidence supports only deterministic synthetic benchmark preparation. It
does not support a semantic model choice, ADR-024 acceptance, Android model
compatibility/performance, participant use, personal content, product Returns,
M5/M6, signing, publication, release, or production readiness.

## Corpus and annotation contract

| Property | Evidence |
|---|---|
| Records | 300 purpose-written synthetic PT-BR pairs |
| Families/groups | 20 scenario families; 60 five-candidate ranking groups |
| Split | 240 development; 60 held-out; family isolation validated |
| Adversarial categories | indirect relation, near-duplicate, lexical trap, negation, unrelated |
| Corpus hash | `72a702510cc0ed3e9f72595a8e0df9ed379278b64e223dd1b1a36b30ed73ccef` |
| Human fields | empty; gold labels null; human gate open |
| Blind packets | two 300-row packets, distinct deterministic order, hidden split/category/target |
| Response schema | rating 0–3, near-duplicate yes/no, optional closed `ambiguous_wording` only |

Construction targets make automated validation and a control rehearsal possible;
they are not human semantic truth. Two independent completed packets are still
required before final held-out model selection.

## Offline checks

`research/m4/run_checks.ps1` regenerated the corpus/packets, validated exact
schema/split/privacy/hash contracts, ran 11 standard-library tests, and produced
the same lexical rehearsal. No package was installed and no network was used by
the harness.

| Rehearsal measure | Result |
|---|---:|
| Spearman against construction targets | 0.135344 |
| Mean nDCG@5 | 0.498154 |
| Unsafe top-1 rate | 1.0 |

The unsafe result is useful as a control: superficial lexical overlap and
near-duplication are not acceptable proxies for a meaningful Return. It is not
a model comparison or a product success metric.

## Boundary and candidate audit

- Research Python imports no network library.
- Android product build/source contains no MediaPipe, ONNX Runtime,
  sentence-transformer, or `research/m4` link.
- Downloaded model artifacts, raw measurements, and annotation responses are
  ignored by Git.
- No model artifact or runtime dependency was downloaded or added.
- Current primary-source inventory records MediaPipe `tasks-text:1.0.0` and its
  Android sample minSdk 24; Fio remains minSdk 26.
- MiniLM multilingual and multilingual-E5-small remain inventory candidates,
  not approved Android assets. Their published quantized files are about 118 MB
  and still need compatible tokenizer/runtime/provenance measurements.
- BERTimbau Base is MIT/PT-BR but is a fill-mask base model without a reviewed
  sentence-level contract; it is excluded as-is.

## Open gates

1. Owner approval of M4-R2 freezes corpus version 1 and the rubric.
2. Two different humans independently rate both blind packets.
3. Only then may reviewed artifacts/adapters be scored against the held-out set.
4. Android API 26/API 36 and labeled physical-device measurements remain absent.
5. ADR-024 stays Deferred; M5 additionally requires real M3 pilot evidence and
   a separately approved study plan.
