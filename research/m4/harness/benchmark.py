#!/usr/bin/env python3
"""Deterministic non-semantic lexical control for the M4 corpus."""

from __future__ import annotations

import argparse
import json
import math
import re
import unicodedata
from collections import Counter, defaultdict
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_CORPUS = ROOT / "corpus" / "m4_pairs_v1.jsonl"
DEFAULT_OUTPUT = ROOT / "results" / "lexical-rehearsal.json"
STOPWORDS = {
    "a", "ao", "aos", "as", "com", "da", "das", "de", "do", "dos", "e",
    "em", "eu", "foi", "me", "meu", "minha", "na", "nas", "no", "nos",
    "o", "os", "para", "por", "que", "se", "sem", "um", "uma",
}


def tokens(text: str) -> list[str]:
    folded = "".join(
        char for char in unicodedata.normalize("NFKD", text.casefold())
        if not unicodedata.combining(char)
    )
    return [token for token in re.findall(r"[a-z0-9]+", folded) if token not in STOPWORDS and len(token) > 1]


def _tfidf_vectors(texts: list[str]) -> list[dict[str, float]]:
    token_lists = [tokens(text) for text in texts]
    document_frequency = Counter(token for values in token_lists for token in set(values))
    count = len(texts)
    vectors: list[dict[str, float]] = []
    for values in token_lists:
        term_frequency = Counter(values)
        vector = {
            token: (1.0 + math.log(freq)) * (math.log((1 + count) / (1 + document_frequency[token])) + 1.0)
            for token, freq in term_frequency.items()
        }
        norm = math.sqrt(sum(weight * weight for weight in vector.values())) or 1.0
        vectors.append({token: weight / norm for token, weight in vector.items()})
    return vectors


def _cosine(left: dict[str, float], right: dict[str, float]) -> float:
    if len(left) > len(right):
        left, right = right, left
    return sum(weight * right.get(token, 0.0) for token, weight in left.items())


def _ranks(values: list[float]) -> list[float]:
    order = sorted(range(len(values)), key=lambda index: values[index])
    ranks = [0.0] * len(values)
    position = 0
    while position < len(order):
        end = position + 1
        while end < len(order) and values[order[end]] == values[order[position]]:
            end += 1
        average = (position + 1 + end) / 2.0
        for index in order[position:end]:
            ranks[index] = average
        position = end
    return ranks


def _pearson(left: list[float], right: list[float]) -> float:
    left_mean = sum(left) / len(left)
    right_mean = sum(right) / len(right)
    numerator = sum((a - left_mean) * (b - right_mean) for a, b in zip(left, right))
    denominator = math.sqrt(
        sum((a - left_mean) ** 2 for a in left) * sum((b - right_mean) ** 2 for b in right)
    )
    return numerator / denominator if denominator else 0.0


def _spearman(left: list[float], right: list[float]) -> float:
    return _pearson(_ranks(left), _ranks(right))


def _ndcg(rows: list[dict]) -> float:
    def gain(row: dict) -> float:
        return 0.0 if row["expectedNearDuplicate"] else (2.0 ** row["authoringTargetRating"] - 1.0)

    ranked = sorted(rows, key=lambda row: (-row["lexicalScore"], row["pairId"]))
    ideal = sorted(rows, key=lambda row: (-gain(row), row["pairId"]))
    dcg = sum(gain(row) / math.log2(index + 2) for index, row in enumerate(ranked))
    idcg = sum(gain(row) / math.log2(index + 2) for index, row in enumerate(ideal))
    return dcg / idcg if idcg else 0.0


def evaluate(records: list[dict]) -> dict:
    texts: list[str] = []
    for record in records:
        texts.extend((record["anchor"], record["candidate"]))
    vectors = _tfidf_vectors(texts)
    scored: list[dict] = []
    for index, record in enumerate(records):
        row = dict(record)
        row["lexicalScore"] = _cosine(vectors[index * 2], vectors[index * 2 + 1])
        scored.append(row)

    groups: dict[str, list[dict]] = defaultdict(list)
    by_category: dict[str, list[float]] = defaultdict(list)
    for row in scored:
        groups[row["groupId"]].append(row)
        by_category[row["category"]].append(row["lexicalScore"])

    unsafe_top = 0
    for rows in groups.values():
        top = sorted(rows, key=lambda row: (-row["lexicalScore"], row["pairId"]))[0]
        unsafe_top += top["category"] in {"near_duplicate", "lexical_trap", "negation"}

    return {
        "schemaVersion": 1,
        "status": "synthetic-rehearsal-not-model-selection-evidence",
        "control": "unicode-tfidf-cosine",
        "labelSource": "authoringTargetRating-not-human-gold",
        "recordCount": len(scored),
        "groupCount": len(groups),
        "spearmanAll": round(_spearman(
            [float(row["authoringTargetRating"]) for row in scored],
            [row["lexicalScore"] for row in scored],
        ), 6),
        "meanNdcgAt5": round(sum(_ndcg(rows) for rows in groups.values()) / len(groups), 6),
        "unsafeTop1Rate": round(unsafe_top / len(groups), 6),
        "meanScoreByCategory": {
            category: round(sum(values) / len(values), 6)
            for category, values in sorted(by_category.items())
        },
        "humanGate": "open",
        "adr024": "deferred",
    }


def load(path: Path) -> list[dict]:
    return [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines() if line]


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--corpus", type=Path, default=DEFAULT_CORPUS)
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    args = parser.parse_args()
    result = evaluate(load(args.corpus))
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False))


if __name__ == "__main__":
    main()
