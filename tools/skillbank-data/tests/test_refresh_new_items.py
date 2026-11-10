"""Unit tests for refresh_new_items.py (monthly refresh of new OSRS items).

Run from the repository root:
    python3 -m unittest discover -s tools/skillbank-data/tests

Stdlib only, small inline fixtures; the real data files are never touched.
"""
from __future__ import annotations

import argparse
import contextlib
import io
import json
import os
import sys
import tempfile
import unittest
from pathlib import Path
from unittest import mock

TOOLS_DIR = Path(__file__).resolve().parent.parent
if str(TOOLS_DIR) not in sys.path:
    sys.path.insert(0, str(TOOLS_DIR))

import llm_classifier  # noqa: E402
import llm_promote  # noqa: E402
import refresh_new_items as rni  # noqa: E402
import scraper  # noqa: E402

MARKER = rni.AUTO_GROUP_MARKER

JAVA_FIXTURE = """package com.skillbank;

public final class SkillBankData {
	public static final String TAG_MELEE = "melee";
	public static final String TAG_MAGE = "mage";
	public static final String TAG_COOKING = "cooking";

	static {
		m.put(TAG_MELEE, Arrays.asList(
			// === Weapons ===
			1, 2, 3,
			// === Body ===
			4, 5
		));
		m.put(TAG_MAGE, Arrays.asList(
			10, 11,

			// === Auto-classified (monthly refresh) ===
			12, 13
		));
		m.put(TAG_COOKING, Arrays.asList(
			20 // trailing comment with a number 999 that must not count
		));
	}
}
"""

META_FIXTURE = (
    '{"1":{"t":10,"ct":"combat","s":{"melee":"Weapons"},"sl":"weapon"},'
    '"100":{"t":0,"ct":"consumable","s":{"cooking":"Fish"}}}'
)


# Shaped like what `claude setup-token` prints (prefix + filler), not a real token.
FAKE_TOKEN_BODY = "Ab3_-" * 19
FAKE_TOKEN = "sk-ant-oat01-" + FAKE_TOKEN_BODY
REJECTED = "Failed to authenticate. API Error: 401 OAuth access token is invalid."


def item(iid: int, name: str, released: str = "1 October 2026", **extra) -> dict:
    d = {"id": iid, "name": name, "release_date": released}
    d.update(extra)
    return d


def make_args(**overrides) -> argparse.Namespace:
    base = dict(dry_run=False, limit=None, max_new=rni.DEFAULT_MAX_NEW, model=None,
                summary_file=None, refresh_wiki=False, no_cache=False, backend="claude-cli",
                check_auth=False)
    base.update(overrides)
    return argparse.Namespace(**base)


class ParseReleaseDateTest(unittest.TestCase):
    def test_formats(self):
        self.assertEqual(rni.parse_release_date("29 June 2004"), "2004-06-29")
        self.assertEqual(rni.parse_release_date(["1 October 2026"]), "2026-10-01")
        self.assertEqual(rni.parse_release_date("March 2005"), "2005-03-01")
        self.assertEqual(rni.parse_release_date(""), "")
        self.assertEqual(rni.parse_release_date(None), "")
        self.assertEqual(rni.parse_release_date("Unknown"), "")


class DetectionTest(unittest.TestCase):
    def setUp(self):
        self.meta = json.loads(META_FIXTURE)
        self.membership, _ = scraper.parse_current_data(JAVA_FIXTURE)
        self.ledger = [
            {"id": 200, "name": "Nowhere item", "tabs": []},
            {"id": 201, "name": "Placed item", "tabs": ["melee"]},
        ]
        self.known = rni.known_ids(self.meta, self.membership, self.ledger)

    def test_known_ids_union_of_three_sources(self):
        self.assertIn(1, self.known)      # meta + java
        self.assertIn(13, self.known)     # java only (inside auto group)
        self.assertIn(100, self.known)    # meta only
        self.assertIn(200, self.known)    # ledger, tabs == []
        self.assertIn(201, self.known)    # ledger, placed
        self.assertNotIn(999, self.known)  # number inside a Java comment

    def test_new_items_exclude_known_and_non_candidates(self):
        items_by_id = {
            1: item(1, "Known java"),
            100: item(100, "Known meta"),
            200: item(200, "Known ledger"),
            300: item(300, "Noted thing", noted=True),
            301: item(301, "Placeholder thing", placeholder=True),
            302: item(302, "Dup thing", duplicate=True),
            303: item(303, "Placeholder (unnamed)"),
            304: item(304, ""),
            400: item(400, "Newer", "5 October 2026"),
            401: item(401, "Older", "3 March 2010"),
            402: item(402, "Undated", ""),
            403: item(403, "Same day lower id", "5 October 2026"),
        }
        new = rni.find_new_items(items_by_id, self.known)
        self.assertEqual([it["id"] for it in new], [401, 400, 403, 402])


class JavaPatchTest(unittest.TestCase):
    def _block(self, src: str, const: str) -> str:
        m = next(m for m in scraper.TAG_PUT_RE.finditer(src) if m.group(1) == const)
        return m.group(0)

    def test_create_group_and_roundtrip(self):
        before, _ = scraper.parse_current_data(JAVA_FIXTURE)
        out = rni.patch_java(JAVA_FIXTURE, {"melee": [7, 6]})
        after, _ = scraper.parse_current_data(out)
        self.assertEqual(set(after["melee"]), set(before["melee"]) | {6, 7})
        self.assertEqual(after["melee"][:5], before["melee"])
        self.assertEqual(after["mage"], before["mage"])
        self.assertEqual(after["cooking"], before["cooking"])
        block = self._block(out, "TAG_MELEE")
        self.assertEqual(block.count(MARKER), 1)
        self.assertIn("\t\t\t4, 5,\n\n\t\t\t" + MARKER + "\n\t\t\t6, 7\n\t\t));", block)
        self.assertEqual(rni.verify_java_roundtrip(before, after, {"melee": [6, 7]}), [])

    def test_extend_existing_group(self):
        before, _ = scraper.parse_current_data(JAVA_FIXTURE)
        out = rni.patch_java(JAVA_FIXTURE, {"mage": [15, 14, 12]})
        after, _ = scraper.parse_current_data(out)
        self.assertEqual(after["mage"], [10, 11, 12, 13, 14, 15])
        block = self._block(out, "TAG_MAGE")
        self.assertEqual(block.count(MARKER), 1)
        self.assertIn("\t\t\t10, 11,\n\n\t\t\t" + MARKER + "\n\t\t\t12, 13, 14, 15\n\t\t));", block)

    def test_eight_ids_per_line_and_comment_handling(self):
        ids = list(range(1000, 1020))
        out = rni.patch_java(JAVA_FIXTURE, {"cooking": ids})
        after, _ = scraper.parse_current_data(out)
        self.assertEqual(after["cooking"], [20] + ids)
        block = self._block(out, "TAG_COOKING")
        # The comma lands before the trailing comment, not after it.
        self.assertIn("\t\t\t20, // trailing comment", block)
        group = block.split(MARKER, 1)[1]
        id_lines = [ln for ln in group.splitlines() if ln.strip() and not ln.strip().startswith("))")]
        self.assertEqual([len(ln.rstrip(",").split(",")) for ln in id_lines], [8, 8, 4])
        self.assertTrue(all(ln.startswith("\t\t\t") for ln in id_lines))
        self.assertTrue(id_lines[0].endswith(",") and not id_lines[-1].endswith(","))

    def test_other_tabs_textually_untouched(self):
        out = rni.patch_java(JAVA_FIXTURE, {"melee": [6]})
        for const in ("TAG_MAGE", "TAG_COOKING"):
            self.assertEqual(self._block(out, const), self._block(JAVA_FIXTURE, const))
        self.assertTrue(out.startswith("package com.skillbank;"))
        self.assertTrue(out.endswith("\t}\n}\n"))

    def test_patch_is_idempotent_for_known_ids(self):
        once = rni.patch_java(JAVA_FIXTURE, {"melee": [6]})
        twice = rni.patch_java(once, {"melee": [6]})
        self.assertEqual(once, twice)
        self.assertEqual(rni.patch_java(JAVA_FIXTURE, {"melee": [1]}), JAVA_FIXTURE)

    def test_unknown_tab_raises(self):
        with self.assertRaises(KeyError):
            rni.patch_java(JAVA_FIXTURE, {"sailing": [6]})

    def test_verify_roundtrip_reports_problems(self):
        before = {"melee": [1, 2], "mage": [10]}
        self.assertEqual(rni.verify_java_roundtrip(before, {"melee": [1, 2, 3], "mage": [10]}, {"melee": [3]}), [])
        self.assertTrue(rni.verify_java_roundtrip(before, {"melee": [1, 3], "mage": [10]}, {"melee": [3]}))
        self.assertTrue(rni.verify_java_roundtrip(before, {"melee": [1, 2, 3], "mage": [10, 11]}, {"melee": [3]}))
        self.assertTrue(rni.verify_java_roundtrip(before, {"melee": [1, 2, 3]}, {"melee": [3]}))


class MetaMergeTest(unittest.TestCase):
    def test_additions_only_keeps_existing_bytes(self):
        new = {"500": {"t": 0, "ct": "consumable", "s": {"cooking": "Fish"}}}
        text, added = rni.merge_meta(META_FIXTURE, new)
        self.assertEqual(added, 1)
        self.assertTrue(text.startswith(META_FIXTURE[:-1] + ","))
        self.assertEqual(json.loads(text)["500"], new["500"])
        self.assertNotIn("\n", text)
        self.assertNotIn(": ", text)

    def test_existing_entries_never_overwritten(self):
        text, added = rni.merge_meta(META_FIXTURE, {"1": {"t": 99, "ct": "x", "s": {}}})
        self.assertEqual(added, 0)
        self.assertEqual(text, META_FIXTURE)


class LedgerTest(unittest.TestCase):
    def test_update_sorted_and_roundtrip(self):
        ledger = [{"id": 30, "name": "c", "tabs": []}, {"id": 10, "name": "a", "tabs": ["melee"]}]
        merged = rni.update_ledger(ledger, [{"id": 20, "name": "b", "tabs": ["mage"]},
                                            {"id": 10, "name": "a2", "tabs": ["range"]}])
        self.assertEqual([e["id"] for e in merged], [10, 20, 30])
        self.assertEqual(merged[0]["name"], "a2")
        with tempfile.TemporaryDirectory() as td:
            p = Path(td) / "ledger.json"
            p.write_text(rni.ledger_text(merged))
            self.assertTrue(p.read_text().endswith("]\n"))
            self.assertEqual(llm_promote.load_ledger(p), merged)
        self.assertEqual(llm_promote.load_ledger(Path("/nonexistent/ledger.json")), [])

    def test_build_synthetic_tabs_applies_ledger_force_includes(self):
        items_by_id = {
            1: item(1, "Llm placed"),
            2: item(2, "Ledger placed"),
            3: item(3, "Ledger nowhere"),
            4: item(4, "Ledger noted", noted=True),
        }
        with tempfile.TemporaryDirectory() as td:
            llm_json = Path(td) / "llm.json"
            llm_json.write_text(json.dumps({"model": "test", "items": [
                {"id": 1, "name": "Llm placed", "tabs": ["melee"]},
            ]}))
            ledger = Path(td) / "ledger.json"
            ledger.write_text(json.dumps([
                {"id": 2, "name": "Ledger placed", "tabs": ["cooking"]},
                {"id": 3, "name": "Ledger nowhere", "tabs": []},
                {"id": 4, "name": "Ledger noted", "tabs": ["cooking"]},
            ]))
            with contextlib.redirect_stderr(io.StringIO()):
                _tabs, _classified, report = llm_promote.build_synthetic_tabs(
                    llm_json, [], items_by_id, ledger_path=ledger,
                )
        by_tab = report["_by_tab"]
        self.assertIn(1, by_tab["melee"])
        self.assertEqual(by_tab["cooking"], {2: "Ledger placed"})
        self.assertEqual(report["tabs"]["cooking"]["ledger_added"], 1)
        for tab, members in by_tab.items():
            self.assertNotIn(3, members, tab)
            self.assertNotIn(4, members, tab)


class OverrideTest(unittest.TestCase):
    def test_include_then_exclude_wins(self):
        forced = {"cooking": {"Grog"}}
        forbidden = {"melee": {"Grog"}, "cooking": {"Grog"}}
        tabs, added, dropped = rni.apply_overrides("Grog", ["melee", "misc"], forced, forbidden)
        self.assertEqual(tabs, {"misc"})
        self.assertEqual(added, ["cooking"])
        self.assertEqual(sorted(dropped), ["cooking", "melee"])

    def test_no_override_leaves_tabs(self):
        tabs, added, dropped = rni.apply_overrides("Other", ["mage"], {}, {})
        self.assertEqual((tabs, added, dropped), ({"mage"}, [], []))

    def test_classify_items_applies_overrides_and_bleed_safety(self):
        items = [item(1, "Forced in"), item(2, "Bleeds"), item(3, "Breaks")]
        results = {
            1: {"tabs": ["misc"], "rationale": "r1", "_usage": {"input_tokens": 10, "output_tokens": 5}},
            2: {"tabs": ["mage", "melee", "range"], "rationale": "r2", "_cached": True},
        }

        def fake_classify(it, wikitext):
            if it["id"] == 3:
                raise RuntimeError("boom")
            return results[it["id"]]

        with contextlib.redirect_stderr(io.StringIO()):
            classified, failures, usage = rni.classify_items(
                items, api_key="k", model="m", use_cache=True, paths=rni.Paths(),
                forced_into={"cooking": {"Forced in"}}, forbidden_from={"misc": {"Forced in"}},
                fetch_wikitext=lambda page: "wikitext", classify=fake_classify,
            )
        self.assertEqual([c.id for c in classified], [1, 2])
        self.assertEqual(classified[0].tabs, ["cooking"])
        self.assertEqual(classified[0].override_added, ["cooking"])
        self.assertEqual(classified[0].override_dropped, ["misc"])
        self.assertEqual(classified[1].tabs, ["melee"])
        self.assertEqual(sorted(classified[1].bleed_dropped), ["mage", "range"])
        self.assertEqual([f["id"] for f in failures], [3])
        self.assertEqual((usage.fresh, usage.cached, usage.input_tokens, usage.output_tokens), (1, 1, 10, 5))

    def test_page_name_fallback(self):
        self.assertEqual(rni.page_name_for({"id": 1, "page_name": "A", "name": "N"}), "A")
        self.assertEqual(rni.page_name_for({"id": 1, "page_name_sub": "B#2", "name": "N"}), "B")
        self.assertEqual(rni.page_name_for({"id": 1, "name": "N"}), "N")


class RunTest(unittest.TestCase):
    """Drive run() end to end with fake item loading and a fake classifier."""

    def setUp(self):
        self.td = tempfile.TemporaryDirectory()
        root = Path(self.td.name)
        (root / "SkillBankData.java").write_text(JAVA_FIXTURE)
        (root / "item-meta.json").write_text(META_FIXTURE)
        (root / "auto-classified.json").write_text("[]\n")
        self.paths = rni.Paths(
            java=root / "SkillBankData.java",
            meta=root / "item-meta.json",
            ledger=root / "auto-classified.json",
            wiki_requirements=root / "missing-wiki-requirements.json",
            wiki_page_cache=root / "wiki-pages",
            llm_cache=root / "llm",
        )
        self.summary = root / "summary.md"
        self.env = mock.patch.dict(os.environ, {"ANTHROPIC_API_KEY": "test-key"})
        self.env.start()
        # The unit tests must pass on a runner without the Claude Code CLI
        # (the Action runs them before installing it), so fake its presence.
        self.cli_present = mock.patch.object(llm_classifier, "cli_available", lambda: True)
        self.cli_login = mock.patch.object(
            llm_classifier, "cli_auth_status", lambda runner=None: {"loggedIn": True},
        )
        self.cli_present.start()
        self.cli_login.start()

    def tearDown(self):
        self.cli_login.stop()
        self.cli_present.stop()
        self.env.stop()
        self.td.cleanup()

    def _snapshot(self) -> dict[str, str]:
        return {p.name: p.read_text() for p in (self.paths.java, self.paths.meta, self.paths.ledger)}

    def _run(self, items_by_id, classify=None, **args):
        loader = lambda **kw: ({str(k): v for k, v in items_by_id.items()}, items_by_id)  # noqa: E731
        out, err = io.StringIO(), io.StringIO()
        with contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
            code = rni.run(make_args(**args), paths=self.paths, load_items=loader,
                           fetch_wikitext=lambda page: "", classify=classify, run_date="2026-11-01")
        return code, out.getvalue(), err.getvalue()

    def test_max_new_guard_writes_nothing(self):
        before = self._snapshot()
        items = {i: item(i, f"New {i}") for i in range(500, 505)}
        code, out, err = self._run(items, max_new=3, summary_file=str(self.summary))
        self.assertEqual(code, 1)
        self.assertIn("exceed --max-new", err)
        self.assertEqual(self._snapshot(), before)
        self.assertFalse(self.summary.exists())

    def test_no_new_items_exits_zero(self):
        before = self._snapshot()
        code, out, _ = self._run({1: item(1, "Known"), 100: item(100, "Known meta")},
                                 summary_file=str(self.summary))
        self.assertEqual(code, 0)
        self.assertIn("No new items", out)
        self.assertEqual(self._snapshot(), before)
        self.assertIn("No new items", self.summary.read_text())

    def test_empty_item_set_fails(self):
        code, _, err = self._run({})
        self.assertEqual(code, 1)
        self.assertIn("no items", err)

    def test_loader_failure_fails_cleanly(self):
        def boom(**kw):
            raise RuntimeError("wiki down")
        out, err = io.StringIO(), io.StringIO()
        with contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
            code = rni.run(make_args(), paths=self.paths, load_items=boom)
        self.assertEqual(code, 1)
        self.assertIn("wiki down", err.getvalue())

    def test_api_backend_requires_key(self):
        items = {500: item(500, "New")}
        with mock.patch.dict(os.environ, {"ANTHROPIC_API_KEY": ""}):
            with mock.patch.object(scraper, "_load_env", lambda: None):
                code, _, _ = self._run(items, dry_run=True, backend="api")
                self.assertEqual(code, 0)
                code, _, err = self._run(items, backend="api")
                self.assertEqual(code, 2)
                self.assertIn("ANTHROPIC_API_KEY not set", err)

    def test_cli_backend_ignores_api_key_and_requires_cli(self):
        items = {500: item(500, "New")}
        before = self._snapshot()
        # No API key is fine for the CLI backend...
        with mock.patch.dict(os.environ, {"ANTHROPIC_API_KEY": ""}):
            with mock.patch.object(scraper, "_load_env", lambda: None):
                code, _, err = self._run(items, classify=lambda it, wt: {"tabs": ["cooking"], "rationale": "r"})
                self.assertEqual(code, 0, err)
        # ...but a missing `claude` binary stops a real run.
        with mock.patch.object(llm_classifier, "cli_available", lambda: False):
            code, _, err = self._run({501: item(501, "Other")}, dry_run=True)
            self.assertEqual(code, 0)
            self.assertIn("Dry run stops", err)
            code, _, err = self._run({501: item(501, "Other")})
            self.assertEqual(code, 2)
            self.assertIn("claude setup-token", err)
            self.assertIn("--backend api", err)
        # Installed but signed out is also a hard stop.
        with mock.patch.object(llm_classifier, "cli_auth_status", lambda runner=None: {"loggedIn": False}):
            code, _, err = self._run({501: item(501, "Other")})
            self.assertEqual(code, 2)
            self.assertIn("not signed in", err)
        # An unreadable auth status is not fatal (the first real call decides).
        with mock.patch.object(llm_classifier, "cli_auth_status", lambda runner=None: None):
            code, _, err = self._run({501: item(501, "Other")},
                                     classify=lambda it, wt: {"tabs": ["cooking"], "rationale": "r"})
            self.assertEqual(code, 0, err)
        ledger = json.loads(self.paths.ledger.read_text())
        self.assertEqual([e["id"] for e in ledger], [500, 501])
        self.assertNotEqual(self._snapshot(), before)

    def test_summary_states_billing_per_backend(self):
        items, classify = self._items_and_classifier()
        code, out, _ = self._run(items, classify=classify, dry_run=True, summary_file=str(self.summary))
        self.assertEqual(code, 0)
        text = self.summary.read_text()
        self.assertIn("billed to: Claude subscription via the Claude Code CLI", text)
        self.assertIn("Not billed", text)
        self.assertNotIn("Estimated cost:", text)
        code, out, _ = self._run(items, classify=classify, dry_run=True, backend="api",
                                 summary_file=str(self.summary))
        self.assertEqual(code, 0)
        text = self.summary.read_text()
        self.assertIn("billed to: Anthropic API", text)
        self.assertIn("Estimated cost:", text)
        self.assertNotIn("Not billed", text)

    def _items_and_classifier(self):
        items = {
            500: item(500, "Test sword", "2 October 2026",
                      equipment={"slot": "weapon", "requirements": {"attack": 60}},
                      weapon={"weapon_type": "slash_sword"}),
            501: item(501, "Test cake", "1 October 2026"),
            502: item(502, "Test junk", "3 October 2026"),
            503: item(503, "Test broken", "4 October 2026"),
            1: item(1, "Known"),
        }
        responses = {
            500: {"tabs": ["melee", "mage"], "rationale": "a | sword", "_usage": {"input_tokens": 100, "output_tokens": 10}},
            501: {"tabs": ["cooking"], "rationale": "food", "_usage": {"input_tokens": 100, "output_tokens": 10}},
            502: {"tabs": [], "rationale": "nothing", "_usage": {}},
        }

        def classify(it, wikitext):
            if it["id"] == 503:
                raise RuntimeError("api exploded")
            return responses[it["id"]]
        return items, classify

    def test_end_to_end_writes_all_three_files(self):
        items, classify = self._items_and_classifier()
        before = self._snapshot()
        code, out, err = self._run(items, classify=classify, summary_file=str(self.summary))
        self.assertEqual(code, 0, err)

        after_java, _ = scraper.parse_current_data(self.paths.java.read_text())
        before_java, _ = scraper.parse_current_data(before["SkillBankData.java"])
        self.assertEqual(after_java["melee"], before_java["melee"] + [500])
        self.assertEqual(after_java["cooking"], before_java["cooking"] + [501])
        self.assertEqual(after_java["mage"], before_java["mage"])  # bleed safety dropped mage

        meta_text = self.paths.meta.read_text()
        self.assertTrue(meta_text.startswith(META_FIXTURE[:-1] + ","))
        meta = json.loads(meta_text)
        self.assertEqual(set(meta) - set(json.loads(META_FIXTURE)), {"500", "501"})
        self.assertEqual(meta["500"]["s"], {"melee": "Weapons"})
        self.assertEqual(meta["500"]["rq"], {"attack": 60})
        self.assertEqual(list(meta["501"]["s"]), ["cooking"])

        ledger = json.loads(self.paths.ledger.read_text())
        self.assertEqual([e["id"] for e in ledger], [500, 501, 502])
        self.assertEqual(ledger[2]["tabs"], [])
        self.assertEqual(ledger[0]["tabs"], ["melee"])
        self.assertEqual(ledger[0]["run"], "2026-11-01")
        import llm_classifier
        self.assertEqual(ledger[0]["model"], llm_classifier.DEFAULT_MODEL)
        self.assertTrue(all(e["id"] != 503 for e in ledger))

        summary = self.summary.read_text()
        self.assertIn("### melee (1)", summary)
        self.assertIn("| 500 | Test sword | 2 October 2026 | Weapons | a \\| sword", summary)
        self.assertIn("combat-bleed safety dropped mage", summary)
        self.assertIn("### No tab assigned (1)", summary)
        self.assertIn("502 Test junk", summary)
        self.assertIn("### Failed to classify (1)", summary)
        self.assertIn("api exploded", summary)
        self.assertIn("How to adjust before merging", summary)
        self.assertIn(summary.strip(), out)

        # Second run: everything is now known, including the no-tab item; only
        # the failed item is proposed again and succeeds this time.
        def classify_retry(it, wikitext):
            return {"tabs": ["cooking"], "rationale": "retry", "_usage": {}}
        code, out, _ = self._run(items, classify=classify_retry)
        self.assertEqual(code, 0)
        self.assertIn("1 new item(s):\n  503\tTest broken", out)
        self.assertNotIn("502\t", out)
        ledger = json.loads(self.paths.ledger.read_text())
        self.assertEqual([e["id"] for e in ledger], [500, 501, 502, 503])
        after_java, _ = scraper.parse_current_data(self.paths.java.read_text())
        self.assertEqual(after_java["melee"], before_java["melee"] + [500])

    def test_dry_run_writes_only_summary(self):
        items, classify = self._items_and_classifier()
        before = self._snapshot()
        code, out, _ = self._run(items, classify=classify, dry_run=True, summary_file=str(self.summary))
        self.assertEqual(code, 0)
        self.assertEqual(self._snapshot(), before)
        self.assertIn("Dry run", self.summary.read_text())

    def test_limit_classifies_first_n(self):
        items, classify = self._items_and_classifier()
        code, out, _ = self._run(items, classify=classify, limit=1)
        self.assertEqual(code, 0)
        ledger = json.loads(self.paths.ledger.read_text())
        self.assertEqual([e["id"] for e in ledger], [501])  # earliest release date first
        before = self._snapshot()
        code, out, _ = self._run(items, classify=classify, limit=0)
        self.assertEqual(code, 0)
        self.assertEqual(self._snapshot(), before)

    def test_all_failed_is_an_error(self):
        def classify(it, wikitext):
            raise RuntimeError("down")
        before = self._snapshot()
        code, _, err = self._run({500: item(500, "New")}, classify=classify)
        self.assertEqual(code, 1)
        self.assertIn("every item failed", err)
        self.assertEqual(self._snapshot(), before)

    def test_rejected_login_stops_at_the_first_item(self):
        items, _ = self._items_and_classifier()
        calls = []

        def classify(it, wikitext):
            calls.append(it["id"])
            raise llm_classifier.CliAuthError(REJECTED)
        before = self._snapshot()
        env = {"CLAUDE_CODE_OAUTH_TOKEN": FAKE_TOKEN, "GITHUB_ACTIONS": "true"}
        with mock.patch.dict(os.environ, env):
            code, out, err = self._run(items, classify=classify, summary_file=str(self.summary))
        self.assertEqual(code, 2)
        self.assertEqual(calls, [501])                      # one call, not one per item
        self.assertIn("not attempting the remaining 3 item(s)", err)
        self.assertIn(REJECTED, err)
        self.assertIn("claude setup-token", err)
        self.assertIn(f"{len(FAKE_TOKEN)} characters long", err)
        self.assertIn("Its shape and length are right", err)
        self.assertIn("::error title=Claude Code login was rejected::", out)
        self.assertNotIn(FAKE_TOKEN_BODY[:10], out + err)   # the token is never echoed
        self.assertEqual(self._snapshot(), before)
        self.assertFalse(self.summary.exists())

    def test_login_rejected_part_way_keeps_earlier_results(self):
        items, ok = self._items_and_classifier()
        calls = []

        def classify(it, wikitext):
            calls.append(it["id"])
            if it["id"] == 500:                             # second item in release order
                raise llm_classifier.CliAuthError(REJECTED)
            return ok(it, wikitext)
        code, out, err = self._run(items, classify=classify)
        self.assertEqual(code, 0)
        self.assertEqual(calls, [501, 500])
        self.assertIn("rejected part-way", err)
        self.assertEqual([e["id"] for e in json.loads(self.paths.ledger.read_text())], [501])
        self.assertIn("Failed to classify (3)", out)

    def _check_auth(self, probe, env=None, **args):
        loader = mock.Mock(side_effect=AssertionError("--check-auth must not load items"))
        before = self._snapshot()
        out, err = io.StringIO(), io.StringIO()
        with mock.patch.object(llm_classifier, "cli_probe", probe), \
                mock.patch.dict(os.environ, {"GITHUB_ACTIONS": "", **(env or {})}), \
                contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
            code = rni.run(make_args(check_auth=True, **args), paths=self.paths, load_items=loader)
        loader.assert_not_called()
        self.assertEqual(self._snapshot(), before)
        return code, out.getvalue(), err.getvalue()

    def test_check_auth_ok(self):
        probe = mock.Mock(return_value={})
        code, out, err = self._check_auth(probe, model="some-model")
        self.assertEqual(code, 0)
        probe.assert_called_once_with("some-model")
        self.assertIn("authentication OK", err)
        self.assertEqual(out, "")

    def test_check_auth_rejected_token_explains_the_fix(self):
        probe = mock.Mock(side_effect=llm_classifier.CliAuthError(REJECTED))
        broken = FAKE_TOKEN[:50] + "\n" + FAKE_TOKEN[50:]    # copied across a wrapped line
        code, out, err = self._check_auth(
            probe, env={"CLAUDE_CODE_OAUTH_TOKEN": broken, "GITHUB_ACTIONS": "true"})
        self.assertEqual(code, 2)
        self.assertIn("in the middle", err)
        self.assertIn("claude setup-token", err)
        self.assertNotIn(FAKE_TOKEN_BODY[:10], out + err)
        # One annotation line; its line breaks are %0A-encoded.
        annotations = [ln for ln in out.splitlines() if ln.startswith("::error ")]
        self.assertEqual(len(annotations), 1)
        self.assertIn("%0A", annotations[0])
        self.assertEqual(len(out.splitlines()), 1)

    def test_check_auth_rejected_local_login(self):
        probe = mock.Mock(side_effect=llm_classifier.CliAuthError("Not logged in"))
        with mock.patch.dict(os.environ):
            os.environ.pop("CLAUDE_CODE_OAUTH_TOKEN", None)
            code, out, err = self._check_auth(probe)
        self.assertEqual(code, 2)
        self.assertIn("/login", err)
        self.assertNotIn("repository secret", err)
        self.assertEqual(out, "")                           # no annotation outside Actions

    def test_check_auth_other_failure_is_not_blamed_on_the_token(self):
        probe = mock.Mock(side_effect=RuntimeError("claude CLI exited 1: model not found"))
        code, _, err = self._check_auth(probe)
        self.assertEqual(code, 2)
        self.assertIn("model not found", err)
        self.assertIn("not a token problem", err)

    def test_check_auth_static_problems_skip_the_probe(self):
        probe = mock.Mock()
        with mock.patch.object(llm_classifier, "cli_available", lambda: False):
            code, _, err = self._check_auth(probe)
        self.assertEqual(code, 2)
        self.assertIn("not found on PATH", err)
        code, _, err = self._check_auth(probe, backend="api")
        self.assertEqual(code, 0)
        self.assertIn("presence only", err)
        probe.assert_not_called()


class FakeCli:
    """Scripted stand-in for the `claude` process: a list of (rc, stdout,
    stderr) tuples or exceptions returned in order; records every call."""

    def __init__(self, *responses):
        self.responses = list(responses)
        self.calls: list[tuple[list[str], str, dict]] = []

    def __call__(self, cmd, stdin_text, env):
        self.calls.append((cmd, stdin_text, env))
        r = self.responses.pop(0)
        if isinstance(r, BaseException):
            raise r
        return r


def cli_result(**overrides) -> str:
    base = {
        "type": "result", "subtype": "success", "is_error": False, "num_turns": 2,
        "result": '{"tabs": ["misc", "melee"], "rationale": "from cli"}',
        "structured_output": {"tabs": ["misc", "melee"], "rationale": "from cli"},
        "usage": {"input_tokens": 1200, "cache_creation_input_tokens": 300,
                  "cache_read_input_tokens": 4000, "output_tokens": 40},
        "total_cost_usd": 0.0148,
    }
    base.update(overrides)
    return json.dumps(base)


class CliBackendTest(unittest.TestCase):
    """llm_classifier's Claude Code CLI transport, with the process faked."""

    def setUp(self):
        self.td = tempfile.TemporaryDirectory()
        self.cache = Path(self.td.name) / "llm"
        self.item = item(777, "Cli sword", equipment={"slot": "weapon"})
        self.sleep = mock.patch.object(llm_classifier.time, "sleep", lambda s: None)
        self.sleep.start()

    def tearDown(self):
        self.sleep.stop()
        self.td.cleanup()

    def _classify(self, runner, **kw):
        kw.setdefault("model", "test-model")
        return llm_classifier.classify_item(
            self.item, "some wikitext", self.cache, "unused-key",
            backend="claude-cli", cli_runner=runner, **kw,
        )

    def test_env_strips_api_key_and_session_but_keeps_oauth_token(self):
        base = {
            "PATH": "/usr/bin", "HOME": "/h",
            "ANTHROPIC_API_KEY": "sk-revoked", "ANTHROPIC_AUTH_TOKEN": "x",
            "CLAUDECODE": "1", "CLAUDE_CODE_SESSION_ID": "abc", "CLAUDE_CODE_ENTRYPOINT": "cli",
            "CLAUDE_CODE_OAUTH_TOKEN": "sk-ant-oat01-keep",
        }
        env = llm_classifier.cli_env(base)
        for k in ("ANTHROPIC_API_KEY", "ANTHROPIC_AUTH_TOKEN", "CLAUDECODE",
                  "CLAUDE_CODE_SESSION_ID", "CLAUDE_CODE_ENTRYPOINT"):
            self.assertNotIn(k, env)
        self.assertEqual(env["CLAUDE_CODE_OAUTH_TOKEN"], "sk-ant-oat01-keep")
        self.assertEqual(env["PATH"], "/usr/bin")
        self.assertEqual(env["DISABLE_AUTOUPDATER"], "1")
        self.assertEqual(base["ANTHROPIC_API_KEY"], "sk-revoked")  # input untouched

    def test_command_shape(self):
        cmd = llm_classifier.cli_command("claude-sonnet-4-6", "SYSTEM")
        self.assertEqual(cmd[:2], [llm_classifier.CLI_BIN, "-p"])
        self.assertEqual(cmd[cmd.index("--model") + 1], "claude-sonnet-4-6")
        self.assertEqual(cmd[cmd.index("--system-prompt") + 1], "SYSTEM")
        self.assertEqual(cmd[cmd.index("--output-format") + 1], "json")
        self.assertEqual(json.loads(cmd[cmd.index("--json-schema") + 1]), llm_classifier.RESPONSE_SCHEMA)
        self.assertEqual(cmd[cmd.index("--tools") + 1], "")
        self.assertIn("--no-session-persistence", cmd)
        self.assertNotIn("--bare", cmd)  # bare mode ignores the subscription login

    def test_structured_output_is_used_and_cached(self):
        fake = FakeCli((0, cli_result(), ""))
        with mock.patch.dict(os.environ, {"ANTHROPIC_API_KEY": "sk-revoked", "CLAUDECODE": "1"}):
            result = self._classify(fake)
        self.assertEqual(result["tabs"], ["melee", "misc"])
        self.assertEqual(result["rationale"], "from cli")
        self.assertFalse(result["_cached"])
        self.assertEqual(result["_backend"], "claude-cli")
        self.assertEqual(result["_model"], "test-model")
        self.assertEqual(result["_cost_usd"], 0.0148)
        self.assertEqual(result["_usage"], {
            "input_tokens": 1200, "cache_creation_input_tokens": 300,
            "cache_read_input_tokens": 4000, "output_tokens": 40,
        })
        cmd, stdin_text, env = fake.calls[0]
        self.assertIn("Name: Cli sword", stdin_text)
        self.assertIn("some wikitext", stdin_text)
        self.assertNotIn("ANTHROPIC_API_KEY", env)
        self.assertNotIn("CLAUDECODE", env)
        self.assertEqual(len(list(self.cache.glob("*.json"))), 1)
        # Second call is a cache hit: the CLI is not spawned again.
        again = self._classify(FakeCli())
        self.assertTrue(again["_cached"])
        self.assertEqual(again["tabs"], ["melee", "misc"])

    def test_text_fallback_when_no_structured_output(self):
        text = '```json\n{"tabs": ["cooking"], "rationale": "text"}\n```'
        fake = FakeCli((0, cli_result(result=text, structured_output=None), ""))
        result = self._classify(fake)
        self.assertEqual(result["tabs"], ["cooking"])

    def test_invalid_tabs_from_cli_are_rejected(self):
        fake = FakeCli((0, cli_result(structured_output={"tabs": ["nope"], "rationale": "x"}), ""))
        with self.assertRaises(ValueError):
            self._classify(fake)
        self.assertEqual(list(self.cache.glob("*.json")), [])

    def test_error_result_and_nonzero_exit_raise(self):
        with self.assertRaises(RuntimeError) as cm:
            self._classify(FakeCli((0, cli_result(is_error=True, result="Not logged in"), "")))
        self.assertIn("Not logged in", str(cm.exception))
        with self.assertRaises(RuntimeError) as cm:
            self._classify(FakeCli((1, "", "some fatal error")))
        self.assertIn("some fatal error", str(cm.exception))
        with self.assertRaises(RuntimeError) as cm:
            self._classify(FakeCli(FileNotFoundError("claude")))
        self.assertIn("not found", str(cm.exception))

    def test_transient_error_is_retried(self):
        fake = FakeCli((1, "", "API Error: 529 overloaded"), (0, cli_result(), ""))
        with contextlib.redirect_stderr(io.StringIO()):
            result = self._classify(fake)
        self.assertEqual(result["tabs"], ["melee", "misc"])
        self.assertEqual(len(fake.calls), 2)
        exhausted = FakeCli(*[(1, "", "rate limit exceeded")] * llm_classifier.CLI_RETRIES)
        with contextlib.redirect_stderr(io.StringIO()):
            with self.assertRaises(RuntimeError) as cm:
                self._classify(exhausted, use_cache=False)
        self.assertIn("repeated", str(cm.exception))
        self.assertEqual(len(exhausted.calls), llm_classifier.CLI_RETRIES)

    def test_rejected_login_raises_auth_error_without_retry(self):
        fake = FakeCli((1, cli_result(is_error=True, result=REJECTED), ""), (0, cli_result(), ""))
        with self.assertRaises(llm_classifier.CliAuthError) as cm:
            self._classify(fake)
        self.assertEqual(str(cm.exception), REJECTED)
        self.assertEqual(len(fake.calls), 1)
        # Wording the CLI really uses for other bad CLAUDE_CODE_OAUTH_TOKEN values.
        for text in ("Failed to authenticate. API Error: 401 Invalid bearer token",
                     "Not logged in · Please run /login",
                     "Invalid auth token · Fix external auth token · Invalid Authorization "
                     "header value from CLAUDE_CODE_OAUTH_TOKEN: it contains a line break",
                     '{"type":"error","error":{"type":"authentication_error"}}'):
            with self.assertRaises(llm_classifier.CliAuthError):
                self._classify(FakeCli((1, "", text)))
        # Anything else stays an ordinary, per-item failure.
        with self.assertRaises(RuntimeError) as cm:
            self._classify(FakeCli((1, "", "model not found")))
        self.assertNotIsInstance(cm.exception, llm_classifier.CliAuthError)

    def test_probe_goes_through_the_classify_transport(self):
        ok = FakeCli((0, cli_result(), ""))
        llm_classifier.cli_probe("test-model", runner=ok)
        cmd, stdin_text, _env = ok.calls[0]
        self.assertEqual(cmd, llm_classifier.cli_command("test-model", llm_classifier.PROBE_SYSTEM))
        self.assertEqual(stdin_text, llm_classifier.PROBE_USER)
        with self.assertRaises(llm_classifier.CliAuthError):
            llm_classifier.cli_probe("test-model", runner=FakeCli((1, "", REJECTED)))

    def test_auth_status(self):
        ok = FakeCli((0, '{"loggedIn": true, "authMethod": "oauth_token"}', ""))
        self.assertEqual(llm_classifier.cli_auth_status(ok)["authMethod"], "oauth_token")
        self.assertEqual(ok.calls[0][0][1:], ["auth", "status", "--json"])
        self.assertIsNone(llm_classifier.cli_auth_status(FakeCli((1, "garbage", ""))))
        self.assertIsNone(llm_classifier.cli_auth_status(FakeCli(FileNotFoundError("claude"))))

    def test_unknown_backend_rejected(self):
        with self.assertRaises(ValueError):
            llm_classifier.classify_item(self.item, "", self.cache, "k", backend="nope")

    def test_usage_accumulates_reported_cost(self):
        u = rni.Usage()
        u.add({"_usage": {"input_tokens": 1}, "_cost_usd": 0.01})
        u.add({"_usage": {"input_tokens": 1}})
        u.add({"_cached": True, "_cost_usd": 5.0})
        self.assertAlmostEqual(u.reported_cost_usd, 0.01)
        self.assertEqual((u.fresh, u.cached, u.input_tokens), (2, 1, 2))
        self.assertIsNone(rni.Usage().reported_cost_usd)


class TokenShapeTest(unittest.TestCase):
    """oauth_token_shape names mis-pastes and never echoes the value."""

    def test_well_formed_token_has_no_problems(self):
        shape = llm_classifier.oauth_token_shape(FAKE_TOKEN)
        self.assertEqual(
            (shape["set"], shape["length"], shape["right_kind"], shape["problems"]),
            (True, len(FAKE_TOKEN), True, []),
        )

    def test_unset_token(self):
        for value in (None, ""):
            shape = llm_classifier.oauth_token_shape(value)
            self.assertEqual((shape["set"], shape["length"], shape["problems"]), (False, 0, []))

    def test_mis_pastes_are_named_without_echoing_the_value(self):
        cases = [
            (FAKE_TOKEN + "\n", ["before or after"]),
            ("  " + FAKE_TOKEN, ["before or after"]),
            (FAKE_TOKEN[:60] + "\n" + FAKE_TOKEN[60:], ["in the middle"]),
            (FAKE_TOKEN[:60] + " " + FAKE_TOKEN[60:], ["in the middle"]),
            (f'"{FAKE_TOKEN}"', ["quotes"]),
            (f"export CLAUDE_CODE_OAUTH_TOKEN={FAKE_TOKEN}", ["extra text before"]),
            ("sk-ant-api03-" + "Qz7" * 30, ["an Anthropic API key"]),
            ("sk-ant-ort01-" + "Qz7" * 30, ["an OAuth refresh token"]),
            ("sk-ant-sid01-" + "Qz7" * 30, ["some other Anthropic credential"]),
            ("ghp_" + "Qz7" * 12, ["something other than an Anthropic token"]),
            (FAKE_TOKEN[:79], ["shorter than a complete token"]),   # first line only
            (FAKE_TOKEN[:-1], ["shorter than a complete token"]),
            (FAKE_TOKEN + "\u2026", ["never appear"]),
        ]
        for value, expected in cases:
            with self.subTest(expected=expected):
                shape = llm_classifier.oauth_token_shape(value)
                text = " | ".join(shape["problems"])
                self.assertEqual(len(shape["problems"]), len(expected), text)
                for needle in expected:
                    self.assertIn(needle, text)
                self.assertEqual(shape["length"], len(value))
                rendered = text + shape["kind"]
                for secret in (FAKE_TOKEN_BODY[:8], "Qz7Qz7"):
                    self.assertNotIn(secret, rendered)


class LoadEnvTest(unittest.TestCase):
    def test_empty_env_var_is_overridden_by_dotenv(self):
        with tempfile.TemporaryDirectory() as td:
            (Path(td) / ".env").write_text("# comment\nANTHROPIC_API_KEY='from-file'\nOTHER=x\n")
            with mock.patch.object(scraper, "SCRIPT_DIR", Path(td)):
                with mock.patch.dict(os.environ, {"ANTHROPIC_API_KEY": "", "OTHER": "keep"}):
                    scraper._load_env()
                    self.assertEqual(os.environ["ANTHROPIC_API_KEY"], "from-file")
                    self.assertEqual(os.environ["OTHER"], "keep")


class BuildMetaEntryTest(unittest.TestCase):
    def test_entry_shape(self):
        import sort_tables
        it = item(900001, "Test sword", equipment={"slot": "weapon", "requirements": {"attack": 60}},
                  weapon={"weapon_type": "slash_sword"})
        sort_tables.init_id_sets({900001: it}, verbose=False)
        entry, status = llm_promote.build_meta_entry(it, ["melee", "misc"], {})
        self.assertEqual(status, "osrsbox_only")
        self.assertEqual(set(entry), {"t", "ct", "s", "wc", "sl", "rq"})
        self.assertEqual(set(entry["s"]), {"melee", "misc"})
        self.assertEqual(entry["rq"], {"attack": 60})


if __name__ == "__main__":
    unittest.main()
