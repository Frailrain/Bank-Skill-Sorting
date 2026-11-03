#!/usr/bin/env python3
"""Monthly refresh: classify OSRS items the plugin does not know yet.

Finds items in the merged wiki + osrsbox dataset that appear in none of

  1. src/main/resources/com/skillbank/item-meta.json
  2. src/main/java/com/skillbank/SkillBankData.java (any tab)
  3. tools/skillbank-data/data/auto-classified.json (the ledger, see below)

classifies them with llm_classifier (same prompt + wikitext context as
`scraper.py --classify`), applies the mapping.py force_include /
force_exclude overrides by name, and patches the results into all three
files:

  * item-meta.json       one new entry per item; existing entries untouched
  * SkillBankData.java   ids appended under a trailing
                         `// === Auto-classified (monthly refresh) ===` group
                         inside each tab's Arrays.asList(...) block
  * auto-classified.json the ledger: every item this script classified,
                         including ones the LLM put nowhere (tabs == []), so
                         it is not proposed again next month. llm_promote
                         reads it as id-based force-includes during a full
                         regen, so the reviewed placements survive.

A Markdown summary (per-tab tables, no-tab list, failures, token usage) is
printed and optionally written with --summary-file; the GitHub Action
(.github/workflows/refresh-item-classifications.yml) uses it as the PR body.

Why not `scraper.py --new-only --llm-promote`? That path rewrites the whole
Java file from out/llm-classifications.json, which only exists on the
maintainer's machine. This script is strictly additive and only touches the
three files above. mapping.py is never edited by the script.

Usage:
  python3 tools/skillbank-data/refresh_new_items.py --dry-run --refresh-wiki
  python3 tools/skillbank-data/refresh_new_items.py --limit 5
  python3 tools/skillbank-data/refresh_new_items.py --no-cache --summary-file out/summary.md

Billing: by default (--backend claude-cli) each item is classified through
the Claude Code CLI (`claude -p`), so the calls run on the maintainer's Claude
subscription (Max plan): the local `claude` login, or in GitHub Actions the
CLAUDE_CODE_OAUTH_TOKEN secret minted with `claude setup-token`. No API key is
involved; ANTHROPIC_API_KEY is removed from the CLI's environment so it can
never bill the API by accident. `--backend api` is the fallback that posts to
the Messages API with ANTHROPIC_API_KEY (environment or .env). With --dry-run
and no usable backend the script stops after listing the new items.
"""
from __future__ import annotations

import argparse
import datetime as _dt
import json
import os
import re
import sys
from dataclasses import dataclass, field
from pathlib import Path
from typing import Callable

SCRIPT_DIR = Path(__file__).resolve().parent
if str(SCRIPT_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPT_DIR))

import llm_promote  # type: ignore  # noqa: E402
import scraper  # type: ignore  # noqa: E402

AUTO_GROUP_MARKER = "// === Auto-classified (monthly refresh) ==="
IDS_PER_LINE = 8
DEFAULT_MAX_NEW = 300
EXCLUDED_NAME_PREFIX = "Placeholder"

# Same note as scraper.run_llm_classification.
PRICING_NOTE = (
    "Sonnet 4.6 pricing as of 2026-01: $3/MTok input, $15/MTok output, "
    "$0.30/MTok cache read, $3.75/MTok cache write; may vary"
)


def _err(*parts: object) -> None:
    print(*parts, file=sys.stderr)


@dataclass
class Paths:
    """Every file the script reads or writes. Tests swap these for temp copies."""
    java: Path = scraper.DATA_JAVA
    meta: Path = scraper.REPO_ROOT / "src/main/resources/com/skillbank/item-meta.json"
    ledger: Path = llm_promote.DEFAULT_LEDGER_PATH
    wiki_requirements: Path = SCRIPT_DIR / "data" / "wiki-requirements.json"
    wiki_page_cache: Path = scraper.WIKI_PAGE_CACHE_DIR
    llm_cache: Path = scraper.LLM_CACHE_DIR


# ── Detection ──────────────────────────────────────────────────────────────

_MONTHS = {
    m: i for i, m in enumerate(
        ["january", "february", "march", "april", "may", "june", "july",
         "august", "september", "october", "november", "december"], 1,
    )
}
_DATE_RE = re.compile(r"^(?:(\d{1,2})\s+)?([A-Za-z]+)\s+(\d{4})$")


def parse_release_date(raw) -> str:
    """Wiki release dates look like '29 June 2004'. Return ISO 'YYYY-MM-DD'
    (day defaults to 01 when missing) or '' when unknown / unparseable.
    Locale-independent on purpose; CI runners are not guaranteed en_US."""
    if isinstance(raw, list):
        raw = raw[0] if raw else ""
    s = str(raw or "").strip()
    m = _DATE_RE.match(s)
    if not m:
        return ""
    day, month, year = m.groups()
    mi = _MONTHS.get(month.lower())
    if not mi:
        return ""
    return f"{int(year):04d}-{mi:02d}-{int(day or 1):02d}"


def is_candidate(item: dict) -> bool:
    """Items the plugin could ever tag: real, non-noted, non-placeholder."""
    if item.get("noted") or item.get("placeholder") or item.get("duplicate"):
        return False
    name = item.get("name") or ""
    if not name or name.startswith(EXCLUDED_NAME_PREFIX):
        return False
    return True


def known_ids(
    meta: dict,
    java_membership: dict[str, list[int]],
    ledger: list[dict],
) -> set[int]:
    """An item is known if it appears in item-meta.json, in any tab of
    SkillBankData.java, or in the ledger (even with tabs == [])."""
    known = {int(k) for k in meta}
    for ids in java_membership.values():
        known.update(int(i) for i in ids)
    known.update(int(e["id"]) for e in ledger)
    return known


def find_new_items(items_by_id: dict[int, dict], known: set[int]) -> list[dict]:
    new = [it for iid, it in items_by_id.items()
           if int(iid) not in known and is_candidate(it)]
    new.sort(key=lambda it: (parse_release_date(it.get("release_date")) or "9999-99-99",
                             int(it["id"])))
    return new


# ── Classification ─────────────────────────────────────────────────────────

@dataclass
class Usage:
    fresh: int = 0
    cached: int = 0
    input_tokens: int = 0
    output_tokens: int = 0
    cache_read_tokens: int = 0
    cache_write_tokens: int = 0
    # Sum of the CLI's own per-call `total_cost_usd` (API-rate estimate; a
    # subscription run is not billed this). None until a call reports one.
    reported_cost_usd: float | None = None

    def add(self, result: dict) -> None:
        if result.get("_cached"):
            self.cached += 1
            return
        self.fresh += 1
        u = result.get("_usage") or {}
        self.input_tokens += u.get("input_tokens") or 0
        self.output_tokens += u.get("output_tokens") or 0
        self.cache_read_tokens += u.get("cache_read_input_tokens") or 0
        self.cache_write_tokens += u.get("cache_creation_input_tokens") or 0
        reported = result.get("_cost_usd")
        if isinstance(reported, (int, float)):
            self.reported_cost_usd = (self.reported_cost_usd or 0.0) + float(reported)

    def cost(self) -> dict[str, float]:
        c = {
            "input": self.input_tokens * 3 / 1_000_000,
            "output": self.output_tokens * 15 / 1_000_000,
            "cache_read": self.cache_read_tokens * 0.30 / 1_000_000,
            "cache_write": self.cache_write_tokens * 3.75 / 1_000_000,
        }
        c["total"] = sum(c.values())
        return c


@dataclass
class Classified:
    item: dict
    tabs: list[str]                 # final placement after overrides
    rationale: str
    llm_tabs: list[str]             # raw LLM output, for the summary
    override_added: list[str] = field(default_factory=list)
    override_dropped: list[str] = field(default_factory=list)
    bleed_dropped: list[str] = field(default_factory=list)

    @property
    def id(self) -> int:
        return int(self.item["id"])

    @property
    def name(self) -> str:
        return self.item.get("name") or f"id={self.id}"


def page_name_for(item: dict) -> str:
    """Same fallback chain as scraper.run_llm_classification."""
    page_name = item.get("page_name") or ""
    if not page_name and item.get("page_name_sub"):
        page_name = str(item["page_name_sub"]).split("#", 1)[0]
    return page_name or item.get("name") or f"id={item['id']}"


def apply_overrides(
    name: str,
    tabs,
    forced_into: dict[str, set[str]],
    forbidden_from: dict[str, set[str]],
) -> tuple[set[str], list[str], list[str]]:
    """mapping.py force_include adds a tab, force_exclude removes one.
    Exclude wins when a name is listed in both, matching
    llm_promote.build_synthetic_tabs."""
    tabs = set(tabs)
    added: list[str] = []
    dropped: list[str] = []
    for tab in llm_promote.ALL_TAB_NAMES:
        if name in forced_into.get(tab, ()) and tab not in tabs:
            tabs.add(tab)
            added.append(tab)
    for tab in sorted(tabs):
        if name in forbidden_from.get(tab, ()):
            tabs.discard(tab)
            dropped.append(tab)
    return tabs, added, dropped


def _default_fetch_wikitext(page_name: str, paths: Paths, use_cache: bool) -> str | None:
    import wiki_page  # type: ignore
    return wiki_page.fetch_page_wikitext(
        page_name, paths.wiki_page_cache, use_cache=use_cache, refresh=False,
    )


def _default_classify(item: dict, wikitext: str, *, api_key: str, model: str,
                      paths: Paths, use_cache: bool, backend: str) -> dict:
    import llm_classifier  # type: ignore
    return llm_classifier.classify_item(
        item, wikitext, paths.llm_cache, api_key,
        model=model, use_cache=use_cache, refresh=not use_cache, backend=backend,
    )


def check_backend(backend: str, api_key: str) -> str | None:
    """Why `backend` cannot run right now, or None when it can."""
    import llm_classifier  # type: ignore
    if backend == "api":
        if not api_key:
            return ("ANTHROPIC_API_KEY not set. Create tools/skillbank-data/.env from "
                    ".env.example or export the variable directly (--backend api).")
        return None
    if backend != "claude-cli":
        return f"unknown backend {backend!r}"
    if not llm_classifier.cli_available():
        return (f"Claude Code CLI ({llm_classifier.CLI_BIN!r}) not found on PATH. Install it "
                "(curl -fsSL https://claude.ai/install.sh | bash), then either sign in with "
                "`claude` or set CLAUDE_CODE_OAUTH_TOKEN (from `claude setup-token`); "
                "or pass --backend api.")
    status = llm_classifier.cli_auth_status()
    if status is not None and not status.get("loggedIn"):
        return ("Claude Code CLI is not signed in. Run `claude` and log in with your "
                "subscription, or set CLAUDE_CODE_OAUTH_TOKEN (from `claude setup-token`); "
                "or pass --backend api.")
    return None


def classify_items(
    items: list[dict],
    *,
    api_key: str,
    model: str,
    use_cache: bool,
    paths: Paths,
    forced_into: dict[str, set[str]],
    forbidden_from: dict[str, set[str]],
    fetch_wikitext: Callable[[str], str | None] | None = None,
    classify: Callable[[dict, str], dict] | None = None,
    backend: str = "claude-cli",
) -> tuple[list[Classified], list[dict], Usage]:
    """Classify every item; per-item failures are collected, not raised.
    Returns (classified, failures, usage). `failures` entries are
    {id, name, release_date, error}."""
    import wiki_page  # type: ignore

    if fetch_wikitext is None:
        fetch_wikitext = lambda pn: _default_fetch_wikitext(pn, paths, use_cache)  # noqa: E731
    if classify is None:
        classify = lambda it, wt: _default_classify(  # noqa: E731
            it, wt, api_key=api_key, model=model, paths=paths, use_cache=use_cache,
            backend=backend,
        )

    classified: list[Classified] = []
    failures: list[dict] = []
    usage = Usage()
    total = len(items)
    for i, it in enumerate(items, 1):
        name = it.get("name") or f"id={it['id']}"
        page_name = page_name_for(it)
        try:
            wt = fetch_wikitext(page_name)
        except Exception as e:  # network hiccup: classify from item data alone
            _err(f"  [{i}/{total}] {name}: wiki page fetch failed: {e}")
            wt = None
        wt_trimmed = wiki_page.trim_wikitext(wt or "")

        try:
            result = classify(it, wt_trimmed)
        except Exception as e:
            _err(f"  [{i}/{total}] {name}: classify failed: {e}")
            failures.append({
                "id": int(it["id"]), "name": name,
                "release_date": it.get("release_date") or "",
                "error": str(e),
            })
            continue

        usage.add(result)
        llm_tabs = sorted(result.get("tabs") or [])
        tabs, added, dropped = apply_overrides(name, llm_tabs, forced_into, forbidden_from)
        # Raw LLM output gets the same combat-bleed safety net a full regen
        # applies (llm_promote.build_synthetic_tabs, Brief #56).
        tabs, bleed_dropped = llm_promote._apply_combat_bleed_safety(tabs)
        classified.append(Classified(
            item=it, tabs=sorted(tabs), rationale=str(result.get("rationale") or ""),
            llm_tabs=llm_tabs, override_added=added, override_dropped=dropped,
            bleed_dropped=bleed_dropped,
        ))
        _err(f"  [{i}/{total}] {name}: {', '.join(sorted(tabs)) or '(no tab)'}"
             + (" [cached]" if result.get("_cached") else ""))
    return classified, failures, usage


# ── SkillBankData.java patching ────────────────────────────────────────────

def _ids_in(text: str) -> set[int]:
    return {int(x) for x in scraper.INT_LITERAL_RE.findall(scraper.LINE_COMMENT_RE.sub("", text))}


def _id_indent(body: str) -> str:
    for line in body.split("\n"):
        if scraper.INT_LITERAL_RE.search(scraper.LINE_COMMENT_RE.sub("", line)):
            return line[: len(line) - len(line.lstrip())]
    return "\t\t\t"


def _closing_indent(body: str) -> str:
    tail = body[body.rfind("\n") + 1:]
    return tail if tail.strip() == "" else "\t\t"


def _ensure_trailing_comma(head: str) -> str:
    """Make sure the last id in `head` is followed by a comma so the group we
    append after it stays valid Java. Comment-only lines are skipped."""
    lines = head.split("\n")
    for i in range(len(lines) - 1, -1, -1):
        line = lines[i]
        code = scraper.LINE_COMMENT_RE.sub("", line)
        if not code.strip():
            continue
        stripped = code.rstrip()
        if stripped.endswith(",") or stripped.endswith("("):
            return head
        comment = line[len(code):]
        lines[i] = stripped + "," + code[len(stripped):] + comment
        return "\n".join(lines)
    return head


def append_ids_to_block(body: str, new_ids) -> str:
    """Return the Arrays.asList(...) body with `new_ids` added under the
    trailing auto-classified group (created when absent, extended when
    present). Ids already anywhere in the block are not duplicated."""
    existing = _ids_in(body)
    idx = body.find(AUTO_GROUP_MARKER)
    if idx >= 0:
        head = body[:idx]
        group_ids = _ids_in(body[idx + len(AUTO_GROUP_MARKER):])
    else:
        head = body
        group_ids = set()
    group_ids |= {int(i) for i in new_ids} - existing
    if not group_ids:
        return body

    id_indent = _id_indent(body)
    close_indent = _closing_indent(body)
    head = _ensure_trailing_comma(head).rstrip()

    lines = [f"{id_indent}{AUTO_GROUP_MARKER}"]
    ordered = sorted(group_ids)
    for i in range(0, len(ordered), IDS_PER_LINE):
        chunk = ordered[i:i + IDS_PER_LINE]
        line = id_indent + ", ".join(str(x) for x in chunk)
        if i + IDS_PER_LINE < len(ordered):
            line += ","
        lines.append(line)
    sep = "\n\n" if head.strip() else "\n"
    return head + sep + "\n".join(lines) + "\n" + close_indent


def patch_java(java_src: str, additions: dict[str, list[int]]) -> str:
    """`additions` maps tab name (e.g. 'melee') to the ids to append."""
    _membership, consts = scraper.parse_current_data(java_src)
    const_for_tab = {tag: const for const, tag in consts.items()}
    out = java_src
    for tab, ids in sorted(additions.items()):
        if not ids:
            continue
        const = const_for_tab.get(tab)
        if not const:
            raise KeyError(f"no TAG constant for tab {tab!r} in SkillBankData.java")
        match = next((m for m in scraper.TAG_PUT_RE.finditer(out) if m.group(1) == const), None)
        if match is None:
            raise KeyError(f"no m.put({const}, Arrays.asList(...)) block in SkillBankData.java")
        out = out[: match.start(2)] + append_ids_to_block(match.group(2), ids) + out[match.end(2):]
    return out


def verify_java_roundtrip(
    before: dict[str, list[int]],
    after: dict[str, list[int]],
    additions: dict[str, list[int]],
) -> list[str]:
    """Return a list of problems (empty when the patched file parses back to
    exactly old ids + new ids per tab, and untouched tabs are identical)."""
    problems: list[str] = []
    for tab, old_ids in before.items():
        new_ids = after.get(tab)
        if new_ids is None:
            problems.append(f"tab {tab!r} disappeared")
            continue
        expected = set(old_ids) | set(additions.get(tab, []))
        if set(new_ids) != expected:
            problems.append(f"tab {tab!r}: parsed ids differ from old + new")
        if tab not in additions and new_ids != old_ids:
            problems.append(f"tab {tab!r} changed although nothing was added")
    for tab in after:
        if tab not in before:
            problems.append(f"unexpected new tab {tab!r}")
    return problems


# ── item-meta.json + ledger ────────────────────────────────────────────────

def merge_meta(existing_text: str, new_entries: dict[str, dict]) -> tuple[str, int]:
    """Append new entries to the compact single-line JSON. Existing entries
    are never modified, so the new text starts with the old text minus its
    closing brace."""
    meta = json.loads(existing_text)
    added = 0
    for key, entry in new_entries.items():
        if key not in meta:
            meta[key] = entry
            added += 1
    return json.dumps(meta, separators=(",", ":")), added


def update_ledger(ledger: list[dict], new_entries: list[dict]) -> list[dict]:
    by_id = {int(e["id"]): e for e in ledger}
    for e in new_entries:
        by_id[int(e["id"])] = e
    return [by_id[k] for k in sorted(by_id)]


def ledger_text(entries: list[dict]) -> str:
    return json.dumps(entries, indent=2, ensure_ascii=False) + "\n"


# ── Summary ────────────────────────────────────────────────────────────────

def _cell(s: object) -> str:
    return " ".join(str(s or "").split()).replace("|", "\\|")


def render_summary(
    *,
    run_date: str,
    model: str,
    total_new: int,
    limit: int | None,
    classified: list[Classified],
    failures: list[dict],
    meta_entries: dict[str, dict],
    usage: Usage,
    dry_run: bool,
    backend: str = "claude-cli",
) -> str:
    by_tab: dict[str, list[Classified]] = {}
    no_tab: list[Classified] = []
    for c in classified:
        if not c.tabs:
            no_tab.append(c)
        for tab in c.tabs:
            by_tab.setdefault(tab, []).append(c)

    placed = len(classified) - len(no_tab)
    lines: list[str] = []
    lines.append(f"## New OSRS items classified ({run_date})")
    lines.append("")
    scope = f"{total_new} new item(s) found on the OSRS Wiki that the plugin does not classify yet"
    if limit is not None and limit < total_new:
        scope += f"; only the first {limit} were classified (`--limit`)"
    billing = ("Claude subscription via the Claude Code CLI" if backend == "claude-cli"
               else "Anthropic API (`ANTHROPIC_API_KEY`)")
    lines.append(scope + f". Model: `{model}`, billed to: {billing}.")
    lines.append("")
    lines.append(f"- Placed in at least one tab: **{placed}**")
    lines.append(f"- No tab assigned (kept in the ledger only): **{len(no_tab)}**")
    lines.append(f"- Failed to classify (will retry next month): **{len(failures)}**")
    if dry_run:
        lines.append("- **Dry run: no files were written.**")
    lines.append("")

    for tab in llm_promote.ALL_TAB_NAMES:
        rows = by_tab.get(tab)
        if not rows:
            continue
        lines.append(f"### {tab} ({len(rows)})")
        lines.append("")
        lines.append("| id | name | released | section | rationale |")
        lines.append("|---|---|---|---|---|")
        for c in sorted(rows, key=lambda c: c.id):
            section = (meta_entries.get(str(c.id), {}).get("s") or {}).get(tab, "")
            notes = []
            if tab in c.override_added:
                notes.append("added by mapping.py force_include")
            if c.bleed_dropped:
                notes.append("combat-bleed safety dropped " + ", ".join(c.bleed_dropped))
            if c.override_dropped:
                notes.append("force_exclude dropped " + ", ".join(c.override_dropped))
            rationale = _cell(c.rationale)
            if notes:
                rationale += " _(" + "; ".join(notes) + ")_"
            lines.append(
                f"| {c.id} | {_cell(c.name)} | {_cell(c.item.get('release_date'))} "
                f"| {_cell(section)} | {rationale} |"
            )
        lines.append("")

    if no_tab:
        lines.append(f"### No tab assigned ({len(no_tab)})")
        lines.append("")
        lines.append("Recorded in the ledger with `tabs: []` so they are not proposed again; "
                     "edit the ledger entry and add the ids by hand if one belongs somewhere.")
        lines.append("")
        for c in sorted(no_tab, key=lambda c: c.id):
            extra = ""
            if c.override_dropped:
                extra = f" (LLM said {', '.join(c.llm_tabs) or 'nothing'}; force_exclude dropped {', '.join(c.override_dropped)})"
            lines.append(f"- {c.id} {_cell(c.name)} ({_cell(c.item.get('release_date'))}): "
                         f"{_cell(c.rationale)}{extra}")
        lines.append("")

    if failures:
        lines.append(f"### Failed to classify ({len(failures)})")
        lines.append("")
        lines.append("Not written anywhere; they will be retried next month.")
        lines.append("")
        for f in sorted(failures, key=lambda f: f["id"]):
            lines.append(f"- {f['id']} {_cell(f['name'])} ({_cell(f['release_date'])}): {_cell(f['error'])}")
        lines.append("")

    cost = usage.cost()
    lines.append("### Usage")
    lines.append("")
    lines.append(f"- Fresh LLM calls: {usage.fresh}, served from local cache: {usage.cached}")
    lines.append(
        f"- Tokens: input {usage.input_tokens:,}, cache read {usage.cache_read_tokens:,}, "
        f"cache write {usage.cache_write_tokens:,}, output {usage.output_tokens:,}"
    )
    cost_line = (
        f"${cost['total']:.4f} "
        f"(input ${cost['input']:.4f} + cache read ${cost['cache_read']:.4f} "
        f"+ cache write ${cost['cache_write']:.4f} + output ${cost['output']:.4f}; "
        f"{PRICING_NOTE})"
    )
    if backend == "claude-cli":
        reported = (f"; the CLI's own estimate was ${usage.reported_cost_usd:.4f}"
                    if usage.reported_cost_usd is not None else "")
        lines.append(
            "- Not billed: this run used the Claude subscription through the Claude Code "
            f"CLI. API-equivalent cost would have been {cost_line}{reported}."
        )
    else:
        lines.append(f"- Estimated cost: {cost_line}")
    lines.append("")
    lines.append("### How to adjust before merging")
    lines.append("")
    lines.append("Edit the three data files directly on this branch:")
    lines.append("")
    lines.append("- `src/main/java/com/skillbank/SkillBankData.java`: ids sit under "
                 f"`{AUTO_GROUP_MARKER}` at the end of each tab's list.")
    lines.append("- `src/main/resources/com/skillbank/item-meta.json`: one entry per id; "
                 "`s` maps tab to section.")
    lines.append("- `tools/skillbank-data/data/auto-classified.json`: the ledger; `tabs` is what a "
                 "future full regen will force-include.")
    lines.append("")
    lines.append("To move an item: move its id between the Java tab lists and update the `s` key in "
                 "item-meta.json and `tabs` in the ledger. "
                 "To reject an item: remove its id from the Java file and item-meta.json and set its "
                 "ledger `tabs` to `[]` so it is not proposed again. "
                 "Add the name to mapping.py force_include/force_exclude only when you want a "
                 "name-based rule that also covers future variants.")
    return "\n".join(lines) + "\n"


# ── Orchestration ──────────────────────────────────────────────────────────

def build_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(
        description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    p.add_argument("--dry-run", action="store_true",
                   help="detect and classify but write none of the three data files "
                        "(the --summary-file is still written)")
    p.add_argument("--limit", type=int, default=None, metavar="N",
                   help="classify only the first N new items (0 = just list them)")
    p.add_argument("--max-new", type=int, default=DEFAULT_MAX_NEW, metavar="N",
                   help=f"abort when more than N new items are found (default {DEFAULT_MAX_NEW}); "
                        "guards against a broken wiki query classifying the whole game")
    p.add_argument("--model", default=None,
                   help="override the LLM model (default: llm_classifier.DEFAULT_MODEL)")
    p.add_argument("--backend", choices=("claude-cli", "api"), default="claude-cli",
                   help="claude-cli (default): run the prompt through the Claude Code CLI on "
                        "the Claude subscription (local `claude` login or CLAUDE_CODE_OAUTH_TOKEN); "
                        "api: post to the Messages API with ANTHROPIC_API_KEY")
    p.add_argument("--summary-file", default=None, metavar="PATH",
                   help="also write the Markdown summary here (the Action uses it as PR body)")
    p.add_argument("--refresh-wiki", action="store_true",
                   help="invalidate the wiki cache and re-fetch (same as scraper.py)")
    p.add_argument("--no-cache", action="store_true",
                   help="bypass the wiki, wiki-page and LLM response caches for this run "
                        "(what CI uses so the wiki is always fetched fresh)")
    return p


def _default_load_items(*, use_cache: bool, refresh: bool):
    # The osrsbox dump is static; reuse the cached copy (downloaded once when
    # absent). The wiki is the only source that gains new items.
    return scraper.load_merged_items(
        use_cache, refresh, refetch_osrsbox=False, write_discrepancies=False,
    )


def _write_summary(path: str | None, text: str) -> None:
    if path:
        out = Path(path)
        out.parent.mkdir(parents=True, exist_ok=True)
        out.write_text(text)
        _err(f"Wrote {out}")


def run(
    args: argparse.Namespace,
    *,
    paths: Paths | None = None,
    load_items=None,
    fetch_wikitext: Callable[[str], str | None] | None = None,
    classify: Callable[[dict, str], dict] | None = None,
    run_date: str | None = None,
) -> int:
    """Returns the process exit code. Writes nothing on a non-zero return."""
    import llm_classifier  # type: ignore
    import sort_tables  # type: ignore

    paths = paths or Paths()
    run_date = run_date or _dt.date.today().isoformat()
    scraper._load_env()
    api_key = os.environ.get("ANTHROPIC_API_KEY") or ""
    model = args.model or llm_classifier.DEFAULT_MODEL
    backend = args.backend
    use_cache = not args.no_cache

    # 1. Merged wiki + osrsbox item set.
    try:
        _items, items_by_id = (load_items or _default_load_items)(
            use_cache=use_cache, refresh=args.refresh_wiki,
        )
    except Exception as e:  # noqa: BLE001 - any fetch failure must abort cleanly
        _err(f"ERROR: fetching item data failed: {e}")
        return 1
    if not items_by_id:
        _err("ERROR: the wiki returned no items; refusing to continue")
        return 1

    # 2. Known ids from the three sources.
    meta_text = paths.meta.read_text()
    meta = json.loads(meta_text)
    java_src = paths.java.read_text()
    membership, _consts = scraper.parse_current_data(java_src)
    ledger = llm_promote.load_ledger(paths.ledger)
    known = known_ids(meta, membership, ledger)
    new_items = find_new_items(items_by_id, known)
    _err(
        f"known ids: item-meta={len(meta)} java={sum(len(v) for v in membership.values())} "
        f"ledger={len(ledger)} union={len(known)}; merged items={len(items_by_id)}; "
        f"new={len(new_items)}"
    )

    if not new_items:
        print("No new items")
        _write_summary(args.summary_file,
                       f"## New OSRS items classified ({run_date})\n\nNo new items.\n")
        return 0
    if len(new_items) > args.max_new:
        _err(
            f"ERROR: {len(new_items)} new items exceed --max-new {args.max_new}. "
            "Either the wiki query is broken or the backlog is genuinely large; "
            "re-run with a higher --max-new after checking the list below."
        )
        for it in new_items[: min(len(new_items), 50)]:
            _err(f"  {it['id']}\t{it.get('name')}\t{it.get('release_date') or ''}")
        return 1

    print(f"{len(new_items)} new item(s):")
    for it in new_items:
        print(f"  {it['id']}\t{it.get('name')}\t{it.get('release_date') or ''}")

    to_classify = new_items if args.limit is None else new_items[: max(args.limit, 0)]
    if args.limit == 0:
        _err("--limit 0: listing only, nothing classified")
        return 0
    problem = check_backend(backend, api_key)
    if problem:
        if args.dry_run:
            _err(f"{problem}\nDry run stops after listing the new items.")
            return 0
        _err(f"ERROR: {problem}")
        return 2

    # 3. Classify + overrides.
    import mapping  # type: ignore
    forced_into, forbidden_from = llm_promote._collect_overrides(mapping.TABS)
    _err(f"Classifying {len(to_classify)} item(s) via LLM (model={model}, backend={backend}, "
         f"cache={'on' if use_cache else 'off'})...")
    classified, failures, usage = classify_items(
        to_classify, api_key=api_key, model=model, use_cache=use_cache, paths=paths,
        forced_into=forced_into, forbidden_from=forbidden_from,
        fetch_wikitext=fetch_wikitext, classify=classify, backend=backend,
    )
    if not classified:
        _err(f"ERROR: every item failed to classify ({len(failures)} failure(s)); nothing written")
        return 1

    # 4. Build the per-item outputs.
    sort_tables.init_id_sets(items_by_id, verbose=False)
    wiki_reqs = llm_promote._load_wiki_requirements(paths.wiki_requirements)
    meta_additions: dict[str, dict] = {}
    java_additions: dict[str, list[int]] = {}
    ledger_additions: list[dict] = []
    for c in classified:
        if c.tabs:
            entry, _status = llm_promote.build_meta_entry(c.item, c.tabs, wiki_reqs)
            meta_additions[str(c.id)] = entry
            for tab in c.tabs:
                java_additions.setdefault(tab, []).append(c.id)
        ledger_additions.append({
            "id": c.id,
            "name": c.name,
            "release_date": c.item.get("release_date") or "",
            "tabs": list(c.tabs),
            "rationale": c.rationale,
            "model": model,
            "run": run_date,
        })

    summary = render_summary(
        run_date=run_date, model=model, total_new=len(new_items), limit=args.limit,
        classified=classified, failures=failures, meta_entries=meta_additions,
        usage=usage, dry_run=args.dry_run, backend=backend,
    )
    print(summary)
    _write_summary(args.summary_file, summary)
    if args.dry_run:
        _err("Dry run: no data files written.")
        return 0

    # 5. Patch, verify, write.
    try:
        new_java = patch_java(java_src, java_additions)
    except KeyError as e:
        _err(f"ERROR: cannot patch SkillBankData.java: {e}; nothing written")
        return 1
    after, _ = scraper.parse_current_data(new_java)
    problems = verify_java_roundtrip(membership, after, java_additions)
    if problems:
        _err("ERROR: patched SkillBankData.java does not round-trip; nothing written:")
        for pr in problems:
            _err(f"  - {pr}")
        return 1
    new_meta_text, n_meta = merge_meta(meta_text, meta_additions)
    new_ledger = update_ledger(ledger, ledger_additions)

    paths.java.write_text(new_java)
    paths.meta.write_text(new_meta_text)
    paths.ledger.write_text(ledger_text(new_ledger))
    _err(
        f"Wrote {paths.java.name} (+{sum(len(v) for v in java_additions.values())} ids), "
        f"{paths.meta.name} (+{n_meta} entries), {paths.ledger.name} (+{len(ledger_additions)} entries)"
    )
    return 0


def main(argv: list[str] | None = None) -> int:
    args = build_parser().parse_args(argv)
    return run(args)


if __name__ == "__main__":
    sys.exit(main())
