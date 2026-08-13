#!/usr/bin/env python3
"""Validate M4 corpus, split, privacy and blind-packet contracts."""

from __future__ import annotations

import csv
import hashlib
import json
import re
from collections import Counter, defaultdict
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
CORPUS_PATH = ROOT / "corpus" / "m4_pairs_v1.jsonl"
MANIFEST_PATH = ROOT / "corpus" / "manifest.json"
EXPECTED_CATEGORIES = {"indirect_relation", "near_duplicate", "lexical_trap", "negation", "unrelated"}
REQUIRED_KEYS = {
    "schemaVersion", "pairId", "split", "scenarioFamily", "groupId", "category",
    "anchor", "candidate", "authoringTargetRating", "expectedNearDuplicate",
    "humanRatings", "goldRating", "provenance",
}
FORBIDDEN_PATTERNS = (
    re.compile(r"https?://|content://|file://|www\.", re.IGNORECASE),
    re.compile(r"\b[A-Z]:\\"),
    re.compile(r"[\w.+-]+@[\w.-]+\.[A-Za-z]{2,}"),
)


def load_records(path: Path = CORPUS_PATH) -> list[dict]:
    return [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines() if line]


def validate(records: list[dict]) -> dict:
    errors: list[str] = []
    ids: set[str] = set()
    groups: dict[str, list[dict]] = defaultdict(list)
    family_splits: dict[str, set[str]] = defaultdict(set)

    if len(records) != 300:
        errors.append(f"expected 300 records, found {len(records)}")

    for record in records:
        pair_id = record.get("pairId", "<missing>")
        if set(record) != REQUIRED_KEYS:
            errors.append(f"{pair_id}: unexpected schema keys")
        if pair_id in ids:
            errors.append(f"{pair_id}: duplicate id")
        ids.add(pair_id)
        if record.get("schemaVersion") != 1:
            errors.append(f"{pair_id}: wrong schema version")
        if record.get("split") not in {"development", "heldout"}:
            errors.append(f"{pair_id}: invalid split")
        if record.get("category") not in EXPECTED_CATEGORIES:
            errors.append(f"{pair_id}: invalid category")
        if record.get("authoringTargetRating") not in {0, 1, 2, 3}:
            errors.append(f"{pair_id}: invalid construction target")
        if record.get("humanRatings") != [] or record.get("goldRating") is not None:
            errors.append(f"{pair_id}: human/gold label present before the human gate")
        if record.get("provenance") != "purpose-written-synthetic-m4-v1":
            errors.append(f"{pair_id}: invalid provenance")
        if record.get("expectedNearDuplicate") != (record.get("category") == "near_duplicate"):
            errors.append(f"{pair_id}: near-duplicate contract mismatch")
        for field in ("anchor", "candidate"):
            text = record.get(field, "")
            if not 20 <= len(text) <= 500:
                errors.append(f"{pair_id}: {field} length outside contract")
            if any(ord(char) < 32 and char not in "\t\n\r" for char in text):
                errors.append(f"{pair_id}: control character")
            if any(pattern.search(text) for pattern in FORBIDDEN_PATTERNS):
                errors.append(f"{pair_id}: path, URL or email-like private surface")
        groups[record.get("groupId", "")].append(record)
        family_splits[record.get("scenarioFamily", "")].add(record.get("split", ""))

    if len(groups) != 60:
        errors.append(f"expected 60 ranking groups, found {len(groups)}")
    for group_id, rows in groups.items():
        if len(rows) != 5 or {row["category"] for row in rows} != EXPECTED_CATEGORIES:
            errors.append(f"{group_id}: group must contain the five fixed categories")
        if len({row["anchor"] for row in rows}) != 1:
            errors.append(f"{group_id}: anchor changed within group")
    leaking_families = [family for family, splits in family_splits.items() if len(splits) != 1]
    if leaking_families:
        errors.append(f"scenario-family split leakage: {leaking_families}")

    split_counts = Counter(record["split"] for record in records)
    if split_counts != Counter({"development": 240, "heldout": 60}):
        errors.append(f"wrong split counts: {dict(split_counts)}")

    packet_orders: list[list[str]] = []
    for label in ("a", "b"):
        path = ROOT / "annotations" / f"annotator-{label}.csv"
        with path.open(encoding="utf-8-sig", newline="") as handle:
            rows = list(csv.DictReader(handle))
        if len(rows) != 300:
            errors.append(f"annotator-{label}: expected 300 rows")
        if set(rows[0]) != {"packet_row", "pair_id", "anchor", "candidate", "rating", "near_duplicate", "reason_code"}:
            errors.append(f"annotator-{label}: packet exposes wrong columns")
        if any(row["rating"] or row["near_duplicate"] or row["reason_code"] for row in rows):
            errors.append(f"annotator-{label}: response fields must start blank")
        packet_orders.append([row["pair_id"] for row in rows])
    if packet_orders[0] == packet_orders[1]:
        errors.append("blind packets must use different deterministic order")

    manifest = json.loads(MANIFEST_PATH.read_text(encoding="utf-8"))
    actual_hash = hashlib.sha256(CORPUS_PATH.read_bytes()).hexdigest()
    if manifest.get("sha256") != actual_hash:
        errors.append("corpus hash differs from manifest")
    if manifest.get("humanGate") != "open":
        errors.append("human gate must remain open")

    if errors:
        raise ValueError("\n".join(errors))
    return {
        "records": len(records),
        "groups": len(groups),
        "families": len(family_splits),
        "development": split_counts["development"],
        "heldout": split_counts["heldout"],
        "sha256": actual_hash,
        "humanGate": "open",
    }


def main() -> None:
    print(json.dumps(validate(load_records()), ensure_ascii=False))


if __name__ == "__main__":
    main()
