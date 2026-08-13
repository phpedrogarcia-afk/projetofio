from __future__ import annotations

import ast
import csv
import hashlib
import json
import sys
import unittest
from collections import Counter, defaultdict
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "corpus"))
sys.path.insert(0, str(ROOT / "harness"))

from generate_corpus import build_records  # noqa: E402
from benchmark import evaluate, tokens  # noqa: E402
from validate_corpus import load_records, validate  # noqa: E402


class CorpusContractTest(unittest.TestCase):
    def test_exact_fixed_shape_and_family_split(self) -> None:
        records = build_records()
        self.assertEqual(300, len(records))
        self.assertEqual(Counter({"development": 240, "heldout": 60}), Counter(r["split"] for r in records))
        self.assertEqual(20, len({r["scenarioFamily"] for r in records}))
        self.assertEqual(60, len({r["groupId"] for r in records}))
        family_splits = defaultdict(set)
        for record in records:
            family_splits[record["scenarioFamily"]].add(record["split"])
        self.assertTrue(all(len(splits) == 1 for splits in family_splits.values()))

    def test_each_group_has_fixed_adversarial_categories(self) -> None:
        groups = defaultdict(list)
        for record in build_records():
            groups[record["groupId"]].append(record)
        expected = {"indirect_relation", "near_duplicate", "lexical_trap", "negation", "unrelated"}
        for rows in groups.values():
            self.assertEqual(5, len(rows))
            self.assertEqual(expected, {row["category"] for row in rows})
            self.assertEqual(1, sum(row["expectedNearDuplicate"] for row in rows))

    def test_generated_files_pass_full_contract(self) -> None:
        result = validate(load_records())
        self.assertEqual(300, result["records"])
        self.assertEqual("open", result["humanGate"])

    def test_blind_packets_hide_targets_and_use_distinct_orders(self) -> None:
        orders = []
        for label in ("a", "b"):
            with (ROOT / "annotations" / f"annotator-{label}.csv").open(encoding="utf-8-sig", newline="") as handle:
                rows = list(csv.DictReader(handle))
            self.assertNotIn("split", rows[0])
            self.assertNotIn("category", rows[0])
            self.assertNotIn("authoringTargetRating", rows[0])
            self.assertTrue(all(not row["rating"] and not row["near_duplicate"] for row in rows))
            orders.append([row["pair_id"] for row in rows])
        self.assertNotEqual(orders[0], orders[1])

    def test_offline_annotation_interfaces_expose_no_targets_or_network(self) -> None:
        for label in ("a", "b"):
            html = (ROOT / "annotations" / f"annotator-{label}.html").read_text(encoding="utf-8")
            self.assertNotIn("authoringTargetRating", html)
            self.assertNotIn('"split"', html)
            self.assertNotIn('"category"', html)
            self.assertNotIn("http://", html)
            self.assertNotIn("https://", html)
            self.assertIn("connect-src 'none'", html)
            self.assertEqual(300, html.count('"pair_id"'))

    def test_manifest_hash_matches_exact_utf8_corpus(self) -> None:
        manifest = json.loads((ROOT / "corpus" / "manifest.json").read_text(encoding="utf-8"))
        actual = hashlib.sha256((ROOT / "corpus" / "m4_pairs_v1.jsonl").read_bytes()).hexdigest()
        self.assertEqual(actual, manifest["sha256"])


class LexicalControlTest(unittest.TestCase):
    def test_tokenizer_is_deterministic_and_accent_insensitive(self) -> None:
        self.assertEqual(tokens("Café na manhã"), tokens("CAFE na manha"))

    def test_rehearsal_never_claims_human_or_selection_evidence(self) -> None:
        first = evaluate(build_records())
        second = evaluate(build_records())
        self.assertEqual(first, second)
        self.assertEqual("synthetic-rehearsal-not-model-selection-evidence", first["status"])
        self.assertEqual("open", first["humanGate"])
        self.assertEqual("deferred", first["adr024"])
        self.assertGreaterEqual(first["meanNdcgAt5"], 0.0)
        self.assertLessEqual(first["meanNdcgAt5"], 1.0)

    def test_research_python_has_no_network_import(self) -> None:
        forbidden = {"requests", "urllib", "socket", "http", "httpx", "aiohttp"}
        for path in ROOT.rglob("*.py"):
            relative_parts = path.relative_to(ROOT).parts
            if ".venv" in relative_parts or "artifacts" in relative_parts:
                continue
            tree = ast.parse(path.read_text(encoding="utf-8"), filename=str(path))
            imported = set()
            for node in ast.walk(tree):
                if isinstance(node, ast.Import):
                    imported.update(alias.name.split(".")[0] for alias in node.names)
                elif isinstance(node, ast.ImportFrom) and node.module:
                    imported.add(node.module.split(".")[0])
            self.assertFalse(imported & forbidden, f"network import in {path.name}: {imported & forbidden}")

    def test_candidate_inventory_keeps_models_unselected(self) -> None:
        inventory = (ROOT / "CANDIDATES.md").read_text(encoding="utf-8")
        self.assertIn("no model selected or product-linked", inventory)
        self.assertIn("BERTimbau Base: excluded as-is", inventory)
        self.assertIn("ADR-024", inventory)

    def test_solo_early_stop_does_not_claim_human_evidence(self) -> None:
        repository = ROOT.parents[1]
        decisions = (repository / "docs" / "DECISIONS.md").read_text(encoding="utf-8")
        evidence = (repository / "plans" / "evidence" / "2026-08-13-m4-no-model-selection.md").read_text(encoding="utf-8")
        self.assertIn("## ADR-041", decisions)
        self.assertIn("- Status: Accepted", decisions.split("## ADR-041", 1)[1])
        self.assertIn("no V0 model selected", evidence)
        self.assertIn("Human gold used: no", evidence)
        self.assertIn("Android product linked: no", evidence)

    def test_android_product_has_no_m4_model_or_research_link(self) -> None:
        repository = ROOT.parents[1]
        forbidden = ("mediapipe", "onnxruntime", "sentence-transformers", "research/m4")
        product_paths = [repository / "app" / "mobile" / "build.gradle.kts"]
        product_paths.extend((repository / "app" / "mobile" / "src" / "main").rglob("*"))
        for path in product_paths:
            if not path.is_file():
                continue
            text = path.read_text(encoding="utf-8", errors="ignore").casefold()
            self.assertFalse(any(token in text for token in forbidden), f"M4 leaked into product: {path}")

    def test_private_research_surfaces_are_gitignored(self) -> None:
        gitignore = (ROOT.parents[1] / ".gitignore").read_text(encoding="utf-8")
        self.assertIn("research/m4/artifacts/", gitignore)
        self.assertIn("research/m4/annotations/responses/", gitignore)

    def test_minilm_result_never_claims_heldout_or_product_evidence(self) -> None:
        result_path = ROOT / "results" / "minilm-development-rehearsal.json"
        if not result_path.exists():
            self.skipTest("optional ignored-model rehearsal has not run")
        result = json.loads(result_path.read_text(encoding="utf-8"))
        self.assertEqual("development-rehearsal-not-heldout-selection-evidence", result["status"])
        self.assertFalse(result["heldoutEvaluated"])
        self.assertFalse(result["humanGoldUsed"])
        self.assertFalse(result["productLinked"])
        self.assertEqual("deferred", result["adr024"])
        self.assertEqual(240, result["developmentPairCount"])
        self.assertEqual("783fea82d71a58179b830a4dbd2d58447e640609e98eedf9ffa12622d375a672", result["artifactSha256"])


if __name__ == "__main__":
    unittest.main()
