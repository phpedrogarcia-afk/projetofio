# M4 MiniLM — development-only evidence

Status: Development rehearsal complete; candidate not advanced to Android/held-out
Date: 2026-08-13
Authority: Accepted M4-R2, ADR-040, frozen corpus/rubric v1

## Claim boundary

This record supports one repeatable Windows CPU rehearsal against synthetic
construction targets in the 240-pair development split. It is not human gold,
held-out evidence, Android/on-device evidence, a semantic model selection,
ADR-024 acceptance, participant research, product integration, M5/M6, or a
release claim.

## Authenticated artifacts

| Item | Evidence |
|---|---|
| Model | `sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2` |
| Revision | `e8f8c211226b894fcb81acc59f3b34ba3efd5f42` |
| License | Apache-2.0 |
| Quantized ONNX | 118,412,398 bytes; SHA-256 `783fea82d71a58179b830a4dbd2d58447e640609e98eedf9ffa12622d375a672` |
| Tokenizer JSON | 9,081,518 bytes; SHA-256 `2c3387be76557bd40970cec13153b3bbf80407865484b209e655e5e4729076b8` |
| Runtime | ONNX Runtime 1.28.0 CPU; Tokenizers 0.23.1; NumPy 2.5.2; Python 3.14.2/Windows AMD64 |
| Dependency mode | six pinned/hash-locked wheels; `--no-index --no-deps`; ignored virtual environment |

`research/m4/wheel-lock.json` contains every wheel filename, size, and SHA-256.
The isolated environment proves `huggingface_hub`, `requests`, and `httpx` are
absent. Model, tokenizer, wheels, and virtual environment are ignored local
artifacts with a complete deletion path.

## Stable development results

Two executions produced identical aggregate quality values:

| Measure | Result |
|---|---:|
| Development pairs/groups | 240 / 48 |
| Unique texts / dimensions | 288 / 384 |
| Spearman vs construction targets | 0.345850 |
| Mean nDCG@5 vs construction targets | 0.483447 |
| Unsafe top-1 rate | 1.0 |
| Mean score — indirect relation | 0.374546 |
| Mean score — near duplicate | 0.770407 |
| Mean score — lexical trap | 0.581415 |
| Mean score — negation | 0.584683 |
| Mean score — unrelated | 0.345099 |

The second recorded resource run loaded in 850.115 ms, encoded 288 unique texts
in 11,956.245 ms (41.515 ms/text), and increased the Windows process working set
from 56,340,480 to 576,991,232 bytes, a 520,650,752-byte delta. These are one-PC
research measurements, not Android performance claims.

## Disposition

MiniLM improves rank correlation over the lexical control but ranks
near-duplicates, lexical traps, and negations far above the intended indirect
relationship. Its unsafe top-1 rate remains 100%, its nDCG@5 does not improve
the lexical rehearsal, and its observed memory/artifact cost is material.
Therefore this artifact is not advanced to Android packaging or held-out
evaluation now. This is a bounded candidate stop, not a final `no model`
decision.

## Verification and residual gate

- 13 M4 offline tests pass, including corpus hash/split/privacy, HTML packet
  isolation, absence of model linkage in Android product code, and explicit
  false held-out/human/product flags in the aggregate result.
- The corpus hash remains
  `72a702510cc0ed3e9f72595a8e0df9ed379278b64e223dd1b1a36b30ed73ccef`.
- Two self-contained offline annotation pages now make the 300-row independent
  ratings usable without a spreadsheet or network.
- Two different human packets and validation are mandatory before any held-out
  comparison or ADR-024 decision.
