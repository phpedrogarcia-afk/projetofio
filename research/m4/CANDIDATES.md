# M4 runtime and model candidate inventory

Snapshot date: 2026-08-13
Status: MiniLM development rehearsal complete; no model selected or product-linked

This is a current-source snapshot, not a permanent claim. Every candidate must
be revalidated before download or measurement.

## Android runtime boundary

Google's Maven metadata lists `com.google.mediapipe:tasks-text:1.0.0` as the
current release, last updated 2026-07-27. Google's official Text Embedder Android
sample pins `tasks-text:1.0.0` and `minSdk 24`, which is compatible with Fio's
`minSdk 26`. The runtime still requires a compatible LiteRT/MediaPipe text model
and metadata; a generic Hugging Face ONNX checkpoint is not automatically a
drop-in MediaPipe asset.

Primary sources:

- https://dl.google.com/dl/android/maven2/com/google/mediapipe/tasks-text/maven-metadata.xml
- https://developers.google.com/edge/mediapipe/solutions/text/text_embedder/android
- https://raw.githubusercontent.com/google-ai-edge/mediapipe-samples/main/examples/text_embedder/android/app/build.gradle

Disposition: eligible runtime for a later Android compatibility spike, not yet
added as a dependency. A compatible reviewed model artifact is still missing.

## Compact multilingual candidates

### `sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2`

- source revision: `e8f8c211226b894fcb81acc59f3b34ba3efd5f42`
- license: Apache-2.0
- model card: multilingual including `pt`/`pt-br`; sentence similarity
- output: 384 dimensions; mean pooling; max sequence length 128
- published ARM64 qint8 ONNX size: 118,412,398 bytes
- published ARM64 qint8 SHA-256:
  `783fea82d71a58179b830a4dbd2d58447e640609e98eedf9ffa12622d375a672`
- tokenizer JSON size/hash: 9,081,518 bytes /
  `2c3387be76557bd40970cec13153b3bbf80407865484b209e655e5e4729076b8`

Primary source:

- https://huggingface.co/sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2

Disposition: M4-R2 authorized a development-only local download. The model and
tokenizer now match the recorded hashes and run through the bounded Windows
ONNX Runtime adapter. On 240 synthetic development pairs it produced Spearman
`0.345850`, mean nDCG@5 `0.483447`, and unsafe top-1 `1.0`; process working-set
delta was about 521 MB and the model/tokenizer total about 127.5 MB. These
construction-target results do not justify Android integration or held-out
evaluation. The artifact remains ignored and removable.

### `intfloat/multilingual-e5-small`

- source revision: `614241f622f53c4eeff9890bdc4f31cfecc418b3`
- license: MIT
- model card: multilingual including Portuguese; sentence similarity/retrieval
- architecture: 12 layers, 384 dimensions, maximum 512 tokens
- input contract requires task prefixes; score calibration differs from a plain
  symmetric sentence-transformer
- published quantized ONNX in the source snapshot targets AVX512/VNNI, not
  Android ARM64; size 118,346,824 bytes, SHA-256
  `dd476dd0c2514e9b9be83aeb3853fac0763e0bdf4a71645407587d77c48a2d88`

Primary source:

- https://huggingface.co/intfloat/multilingual-e5-small

Disposition: inventory comparison only. No source-provided ARM64 quantized
artifact was found in this snapshot; conversion, tokenizer, prefix semantics,
and Android runtime provenance must be resolved before download/integration.

## PT-focused option

### `neuralmind/bert-base-portuguese-cased` (BERTimbau Base)

- source revision: `94d69c95f98f7d5b2a8700c420230ae10def0baa`
- license: MIT
- language: Brazilian Portuguese; 12 layers/110M parameters
- published primary weight size: 438,235,074 bytes
- weight SHA-256:
  `cb1693767adef60abf23d9fde3996f0c1e6310afad103a2db94ad44854568955`
- source pipeline is `fill-mask`; the card exposes token embeddings but no
  reviewed sentence-level pooling/fine-tuning contract for this benchmark

Primary source:

- https://huggingface.co/neuralmind/bert-base-portuguese-cased

Disposition: rejected as a direct sentence-embedding candidate for M4-R1. Being
PT-BR-specific does not make a masked-language base model a valid similarity
encoder. A future PT-focused sentence model may enter only with a primary model
card, suitable license/provenance, sentence-level objective, reproducible
artifact, and acceptable Android profile.

## Current candidate freeze decision

- Lexical control: authorized; standard-library implementation only.
- MediaPipe Text Embedder runtime: inventoried, not added.
- MiniLM multilingual: hash-verified local development rehearsal; not advanced
  to Android/held-out and not selected.
- E5-small multilingual: inventoried, not downloaded.
- BERTimbau Base: excluded as-is.
- PT-focused sentence encoder: no suitable option frozen in this snapshot.

ADR-041 takes the safe early-stop outcome: MiniLM's development result is not
promising enough to justify human held-out annotation or Android product/runtime
integration. No model is selected. The two-human gate is retained if semantic
selection is ever reopened; Codex ratings cannot substitute for it. ADR-024
remains Deferred.
