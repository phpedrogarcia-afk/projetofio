#!/usr/bin/env python3
"""Hash-verified MiniLM development-only adapter for the M4 research corpus."""

from __future__ import annotations

import argparse
import ctypes
import hashlib
import json
import math
import os
import sys
import time
from collections import defaultdict
from pathlib import Path

import numpy as np
import onnxruntime as ort
from tokenizers import Tokenizer


ROOT = Path(__file__).resolve().parents[1]
CORPUS_PATH = ROOT / "corpus" / "m4_pairs_v1.jsonl"
MANIFEST_PATH = ROOT / "model-manifest.json"
MODEL_ROOT = ROOT / "artifacts" / "minilm-multilingual"
DEFAULT_OUTPUT = ROOT / "results" / "minilm-development-rehearsal.json"


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for block in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def working_set_bytes() -> int | None:
    if sys.platform != "win32":
        return None

    class Counters(ctypes.Structure):
        _fields_ = [
            ("cb", ctypes.c_ulong), ("PageFaultCount", ctypes.c_ulong),
            ("PeakWorkingSetSize", ctypes.c_size_t), ("WorkingSetSize", ctypes.c_size_t),
            ("QuotaPeakPagedPoolUsage", ctypes.c_size_t), ("QuotaPagedPoolUsage", ctypes.c_size_t),
            ("QuotaPeakNonPagedPoolUsage", ctypes.c_size_t), ("QuotaNonPagedPoolUsage", ctypes.c_size_t),
            ("PagefileUsage", ctypes.c_size_t), ("PeakPagefileUsage", ctypes.c_size_t),
            ("PrivateUsage", ctypes.c_size_t),
        ]

    kernel32 = ctypes.WinDLL("kernel32", use_last_error=True)
    psapi = ctypes.WinDLL("psapi", use_last_error=True)
    kernel32.GetCurrentProcess.restype = ctypes.c_void_p
    psapi.GetProcessMemoryInfo.argtypes = [ctypes.c_void_p, ctypes.POINTER(Counters), ctypes.c_ulong]
    psapi.GetProcessMemoryInfo.restype = ctypes.c_int
    counters = Counters()
    counters.cb = ctypes.sizeof(counters)
    process = kernel32.GetCurrentProcess()
    if not psapi.GetProcessMemoryInfo(process, ctypes.byref(counters), counters.cb):
        return None
    return int(counters.WorkingSetSize)


def _ranks(values: list[float]) -> list[float]:
    order = sorted(range(len(values)), key=lambda index: values[index])
    ranks = [0.0] * len(values)
    start = 0
    while start < len(order):
        end = start + 1
        while end < len(order) and values[order[end]] == values[order[start]]:
            end += 1
        average = (start + 1 + end) / 2.0
        for index in order[start:end]:
            ranks[index] = average
        start = end
    return ranks


def _spearman(left: list[float], right: list[float]) -> float:
    left = _ranks(left)
    right = _ranks(right)
    left_mean = sum(left) / len(left)
    right_mean = sum(right) / len(right)
    numerator = sum((a - left_mean) * (b - right_mean) for a, b in zip(left, right))
    denominator = math.sqrt(
        sum((a - left_mean) ** 2 for a in left) * sum((b - right_mean) ** 2 for b in right)
    )
    return numerator / denominator if denominator else 0.0


def _ndcg(rows: list[dict]) -> float:
    def gain(row: dict) -> float:
        return 0.0 if row["expectedNearDuplicate"] else 2.0 ** row["authoringTargetRating"] - 1.0

    ranked = sorted(rows, key=lambda row: (-row["modelScore"], row["pairId"]))
    ideal = sorted(rows, key=lambda row: (-gain(row), row["pairId"]))
    dcg = sum(gain(row) / math.log2(index + 2) for index, row in enumerate(ranked))
    idcg = sum(gain(row) / math.log2(index + 2) for index, row in enumerate(ideal))
    return dcg / idcg if idcg else 0.0


class MiniLmAdapter:
    def __init__(self, manifest: dict):
        model_path = MODEL_ROOT / manifest["model"]["localFilename"]
        tokenizer_path = MODEL_ROOT / manifest["tokenizer"]["localFilename"]
        if sha256(model_path) != manifest["model"]["sha256"]:
            raise ValueError("model hash mismatch")
        if sha256(tokenizer_path) != manifest["tokenizer"]["sha256"]:
            raise ValueError("tokenizer hash mismatch")
        self.tokenizer = Tokenizer.from_file(str(tokenizer_path))
        self.tokenizer.enable_truncation(max_length=128)
        self.tokenizer.enable_padding(length=128)
        options = ort.SessionOptions()
        options.intra_op_num_threads = 1
        options.inter_op_num_threads = 1
        options.graph_optimization_level = ort.GraphOptimizationLevel.ORT_ENABLE_BASIC
        self.session = ort.InferenceSession(str(model_path), sess_options=options, providers=["CPUExecutionProvider"])
        self.input_names = {item.name for item in self.session.get_inputs()}

    def encode(self, texts: list[str], batch_size: int = 16) -> np.ndarray:
        batches: list[np.ndarray] = []
        for start in range(0, len(texts), batch_size):
            encodings = self.tokenizer.encode_batch(texts[start:start + batch_size])
            input_ids = np.asarray([encoding.ids for encoding in encodings], dtype=np.int64)
            attention = np.asarray([encoding.attention_mask for encoding in encodings], dtype=np.int64)
            token_types = np.asarray([encoding.type_ids for encoding in encodings], dtype=np.int64)
            values = {
                "input_ids": input_ids,
                "attention_mask": attention,
                "token_type_ids": token_types,
            }
            feeds = {name: values[name] for name in self.input_names}
            output = self.session.run(None, feeds)[0]
            if output.ndim == 3:
                mask = attention[:, :, None].astype(np.float32)
                output = (output * mask).sum(axis=1) / np.clip(mask.sum(axis=1), 1e-9, None)
            if output.ndim != 2:
                raise ValueError(f"unexpected model output rank: {output.ndim}")
            norms = np.linalg.norm(output, axis=1, keepdims=True)
            batches.append((output / np.clip(norms, 1e-12, None)).astype(np.float32))
        return np.concatenate(batches, axis=0)


def evaluate() -> dict:
    os.environ["HF_HUB_OFFLINE"] = "1"
    os.environ["TRANSFORMERS_OFFLINE"] = "1"
    manifest = json.loads(MANIFEST_PATH.read_text(encoding="utf-8-sig"))
    records = [
        json.loads(line) for line in CORPUS_PATH.read_text(encoding="utf-8").splitlines()
        if line and json.loads(line)["split"] == "development"
    ]
    if len(records) != 240 or any(record["goldRating"] is not None for record in records):
        raise ValueError("development-only unlabeled corpus contract failed")

    texts = sorted({text for record in records for text in (record["anchor"], record["candidate"])})
    before = working_set_bytes()
    load_start = time.perf_counter()
    adapter = MiniLmAdapter(manifest)
    load_ms = (time.perf_counter() - load_start) * 1000.0
    encode_start = time.perf_counter()
    embeddings = adapter.encode(texts)
    encode_ms = (time.perf_counter() - encode_start) * 1000.0
    after = working_set_bytes()
    vector_by_text = dict(zip(texts, embeddings))

    scored: list[dict] = []
    for record in records:
        row = dict(record)
        row["modelScore"] = float(np.dot(vector_by_text[row["anchor"]], vector_by_text[row["candidate"]]))
        scored.append(row)
    groups: dict[str, list[dict]] = defaultdict(list)
    categories: dict[str, list[float]] = defaultdict(list)
    for row in scored:
        groups[row["groupId"]].append(row)
        categories[row["category"]].append(row["modelScore"])
    unsafe_top = sum(
        sorted(rows, key=lambda row: (-row["modelScore"], row["pairId"]))[0]["category"]
        in {"near_duplicate", "lexical_trap", "negation"}
        for rows in groups.values()
    )
    return {
        "schemaVersion": 1,
        "status": "development-rehearsal-not-heldout-selection-evidence",
        "modelId": manifest["modelId"],
        "revision": manifest["revision"],
        "artifactSha256": manifest["model"]["sha256"],
        "runtime": {"onnxruntime": ort.__version__, "numpy": np.__version__},
        "provider": adapter.session.get_providers(),
        "inputNames": sorted(adapter.input_names),
        "developmentPairCount": len(scored),
        "developmentGroupCount": len(groups),
        "uniqueTextCount": len(texts),
        "embeddingDimensions": int(embeddings.shape[1]),
        "loadMs": round(load_ms, 3),
        "encodeMs": round(encode_ms, 3),
        "meanEncodeMsPerText": round(encode_ms / len(texts), 3),
        "workingSetBeforeBytes": before,
        "workingSetAfterBytes": after,
        "workingSetDeltaBytes": None if before is None or after is None else after - before,
        "spearmanDevelopmentTargets": round(_spearman(
            [float(row["authoringTargetRating"]) for row in scored],
            [row["modelScore"] for row in scored],
        ), 6),
        "meanNdcgAt5DevelopmentTargets": round(sum(_ndcg(rows) for rows in groups.values()) / len(groups), 6),
        "unsafeTop1Rate": round(unsafe_top / len(groups), 6),
        "meanScoreByCategory": {
            category: round(sum(values) / len(values), 6)
            for category, values in sorted(categories.items())
        },
        "labelSource": "authoringTargetRating-not-human-gold",
        "heldoutEvaluated": False,
        "humanGoldUsed": False,
        "adr024": "deferred",
        "productLinked": False,
    }


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    args = parser.parse_args()
    result = evaluate()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False))


if __name__ == "__main__":
    main()
