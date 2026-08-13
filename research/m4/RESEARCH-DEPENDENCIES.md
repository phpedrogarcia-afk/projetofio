# M4 research dependency boundary

Snapshot date: 2026-08-13
Status: Approved research-only resolution; absent from Android product builds

The MiniLM development adapter uses an ignored local Python 3.14 virtual
environment. It never enters `app/`, Gradle, an APK, Room, Returns, or release
dependencies. Wheels are downloaded once into ignored `artifacts/wheels/`,
hashed, then installed with `--no-index --no-deps` for offline execution.

## Direct runtime set

| Package | Version | Purpose | License | Removal |
|---|---:|---|---|---|
| `onnxruntime` | 1.28.0 | Local CPU execution of the reviewed ONNX model | MIT | Delete `.venv/` and `artifacts/`; adapter becomes unavailable |
| `tokenizers` | 0.23.1 | Load the pinned local tokenizer JSON | Apache-2.0 | Same; no product tokenizer contract exists |
| `numpy` | 2.5.2 | Tensor arrays and mean pooling | BSD-3-Clause | Removed with research environment |
| `flatbuffers` | 25.12.19 | ONNX Runtime declared support dependency | Apache-2.0 | Removed with research environment |
| `packaging` | 26.3 | ONNX Runtime declared version support | Apache-2.0/BSD | Removed with research environment |
| `protobuf` | 7.35.1 | ONNX Runtime declared model support | BSD-3-Clause | Removed with research environment |

Primary project/package sources:

- https://pypi.org/project/onnxruntime/1.28.0/
- https://github.com/microsoft/onnxruntime
- https://pypi.org/project/tokenizers/0.23.1/
- https://github.com/huggingface/tokenizers
- https://pypi.org/project/numpy/2.5.2/
- https://github.com/google/flatbuffers
- https://github.com/pypa/packaging
- https://github.com/protocolbuffers/protobuf

## Deliberate dependency reduction

`tokenizers` declares `huggingface-hub` for online/repository convenience APIs.
M4 uses only `Tokenizer.from_file()` on a hash-verified local JSON, so the wheel
is installed with no dependencies. The isolated environment must prove that
`huggingface_hub`, `requests`, and `httpx` are absent. This prevents a local
benchmark from silently acquiring a model/network client path.

The environment also omits `sentence-transformers`, PyTorch, Transformers,
MediaPipe, ONNX Runtime Android, analytics, and any hosted-model SDK. If the
six-wheel bounded environment cannot import and run the adapter offline, the
spike fails closed rather than expanding dependencies automatically.

## Artifact authentication

`prepare_minilm.ps1` verifies the model/tokenizer against the hashes already in
`CANDIDATES.md`, creates a wheel lock with exact filenames/sizes/SHA-256, and
tests the offline imports. Model, tokenizer, wheels, and virtual environment are
ignored local research material. Only the manifest and aggregate synthetic
results may remain as repository evidence.
