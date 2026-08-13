#!/usr/bin/env python3
"""Validate two independent blind response files without exposing free text."""

from __future__ import annotations

import argparse
import csv
import json
from collections import Counter
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
CORPUS_PATH = ROOT / "corpus" / "m4_pairs_v1.jsonl"
ALLOWED_REASONS = {"", "ambiguous_wording"}


def _load_packet(path: Path, expected_ids: set[str]) -> dict[str, tuple[int, bool, str]]:
    with path.open(encoding="utf-8-sig", newline="") as handle:
        rows = list(csv.DictReader(handle))
    if len(rows) != len(expected_ids):
        raise ValueError(f"{path.name}: expected {len(expected_ids)} rows")
    result: dict[str, tuple[int, bool, str]] = {}
    for row in rows:
        pair_id = row.get("pair_id", "")
        if pair_id not in expected_ids or pair_id in result:
            raise ValueError(f"{path.name}: invalid or duplicate pair_id")
        try:
            rating = int(row.get("rating", ""))
        except ValueError as error:
            raise ValueError(f"{path.name}: rating must be 0..3") from error
        if rating not in {0, 1, 2, 3}:
            raise ValueError(f"{path.name}: rating must be 0..3")
        near_text = row.get("near_duplicate", "").strip().casefold()
        if near_text not in {"yes", "no"}:
            raise ValueError(f"{path.name}: near_duplicate must be yes or no")
        reason = row.get("reason_code", "").strip()
        if reason not in ALLOWED_REASONS:
            raise ValueError(f"{path.name}: reason_code is not allowlisted")
        result[pair_id] = (rating, near_text == "yes", reason)
    return result


def validate(left_path: Path, right_path: Path) -> dict:
    corpus = [json.loads(line) for line in CORPUS_PATH.read_text(encoding="utf-8").splitlines() if line]
    expected_ids = {record["pairId"] for record in corpus}
    left = _load_packet(left_path, expected_ids)
    right = _load_packet(right_path, expected_ids)
    exact = sum(left[pair_id][:2] == right[pair_id][:2] for pair_id in expected_ids)
    absolute_differences = Counter(abs(left[pair_id][0] - right[pair_id][0]) for pair_id in expected_ids)
    return {
        "schemaVersion": 1,
        "pairCount": len(expected_ids),
        "exactRatingAndDuplicateAgreement": exact,
        "absoluteRatingDifferenceCounts": dict(sorted(absolute_differences.items())),
        "ambiguousWordingCount": sum(
            left[pair_id][2] == "ambiguous_wording" or right[pair_id][2] == "ambiguous_wording"
            for pair_id in expected_ids
        ),
        "containsFreeText": False,
        "selectionAuthorized": False,
    }


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("annotator_a", type=Path)
    parser.add_argument("annotator_b", type=Path)
    args = parser.parse_args()
    print(json.dumps(validate(args.annotator_a, args.annotator_b), ensure_ascii=False))


if __name__ == "__main__":
    main()
