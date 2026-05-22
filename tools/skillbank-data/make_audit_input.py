#!/usr/bin/env python3
"""Generate out/audit-input.jsonl — input file for the Brief #62 audit pass.

One JSON object per line, one item per line, containing:
  id, name, current_tabs, wiki_summary, osrsbox

`current_tabs` is the flattened tab set produced by llm_promote.build_synthetic_tabs
(LLM tabs + mapping.py force_include/force_exclude + extra_items + slayer
preservation + tab_exclude filter). Items missing from llm-classifications.json
but added through mapping.py are included.

`wiki_summary` is the first non-template paragraph of the item's cached wiki
page (cache/wiki-pages/page-<hash>.txt). Falls back to the `examine` field
when the wiki page is missing or empty.

`osrsbox` carries the structured fields the audit pass needs to reason about
each item (slot, weapon, requirements, members, tradeable, examine).

Run from tools/skillbank-data/:
    python3 make_audit_input.py
"""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
sys.path.insert(0, str(SCRIPT_DIR))

import mapping  # type: ignore
import wiki  # type: ignore
import wiki_page  # type: ignore
import llm_promote  # type: ignore
from scraper import (
    fetch_osrsbox, merge_wiki_osrsbox, parse_current_data,
    OUT_DIR, WIKI_CACHE_DIR, WIKI_PAGE_CACHE_DIR, REPO_ROOT,
)


# ── Wiki summary extraction ───────────────────────────────────────────────

_INFOBOX_RE = re.compile(r"\{\{Infobox[^{}]*?\}\}", re.IGNORECASE | re.DOTALL)
_TEMPLATE_RE = re.compile(r"\{\{[^{}]*?\}\}", re.DOTALL)
_REF_RE = re.compile(r"<ref[^/]*?>.*?</ref>", re.DOTALL | re.IGNORECASE)
_HTML_TAG_RE = re.compile(r"<[^>]+>")
_WIKILINK_RE = re.compile(r"\[\[([^|\]]+)(?:\|([^\]]+))?\]\]")
_BOLD_ITALIC_RE = re.compile(r"'{2,5}")


def _strip_templates(text: str) -> str:
    """Strip nested wiki templates iteratively until none remain."""
    prev = None
    out = text
    # Drop infoboxes specifically first (they often nest stat templates).
    out = _INFOBOX_RE.sub("", out)
    while out != prev:
        prev = out
        out = _TEMPLATE_RE.sub("", out)
    return out


def _extract_first_paragraph(wikitext: str) -> str:
    """Return the first non-empty paragraph of body prose from wikitext.

    The wiki page layout is: {{Infobox Item|...}} \n\n <intro paragraph> \n\n ...
    After templates and refs are stripped, the first non-empty paragraph is the
    intro prose."""
    if not wikitext:
        return ""
    text = _strip_templates(wikitext)
    text = _REF_RE.sub("", text)
    text = _HTML_TAG_RE.sub("", text)
    # Replace wiki links with their display text.
    text = _WIKILINK_RE.sub(lambda m: m.group(2) or m.group(1), text)
    text = _BOLD_ITALIC_RE.sub("", text)
    # Split into paragraphs by blank line; return the first that has content
    # and isn't a section header.
    for para in re.split(r"\n\s*\n", text):
        stripped = para.strip()
        if not stripped:
            continue
        if stripped.startswith("=") or stripped.startswith("*") or stripped.startswith("#"):
            continue
        if stripped.startswith("[[Category:"):
            continue
        # Collapse internal whitespace.
        return re.sub(r"\s+", " ", stripped)
    return ""


def _wiki_summary_for(item: dict, cache_dir: Path) -> str:
    """Return first-paragraph summary if cached; fall back to examine field."""
    page_name = item.get("page_name") or ""
    if not page_name and item.get("page_name_sub"):
        page_name = item["page_name_sub"].split("#", 1)[0]
    summary = ""
    if page_name:
        try:
            wt = wiki_page.fetch_page_wikitext(
                page_name, cache_dir, use_cache=True, refresh=False,
            )
        except Exception:
            wt = None
        summary = _extract_first_paragraph(wt or "")
    if not summary:
        summary = (item.get("examine") or "").strip()
    # Cap to ~1500 chars so the per-line JSON stays manageable.
    if len(summary) > 1500:
        summary = summary[:1500].rsplit(" ", 1)[0] + " […]"
    return summary


# ── osrsbox subset ────────────────────────────────────────────────────────

def _osrsbox_subset(item: dict) -> dict:
    """The structured fields the audit pass needs. We omit huge nested
    dictionaries to keep the JSONL line size reasonable."""
    eq = item.get("equipment") or {}
    wp = item.get("weapon") or {}
    out: dict = {
        "members": bool(item.get("members")),
        "tradeable": bool(item.get("tradeable")),
        "equipable": bool(item.get("equipable")),
        "equipable_weapon": bool(item.get("equipable_weapon")),
        "quest_item": bool(item.get("quest_item")),
        "release_date": item.get("release_date") or "",
        "examine": item.get("examine") or "",
    }
    if eq:
        eq_out = {k: v for k, v in eq.items() if v not in (None, "", 0, False, {}, [])}
        if eq_out:
            out["equipment"] = eq_out
    if wp:
        wp_out = {k: v for k, v in wp.items() if v not in (None, "", 0, False, {}, [])}
        if wp_out:
            out["weapon"] = wp_out
    return out


# ── Main ──────────────────────────────────────────────────────────────────

def main() -> int:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    print("Loading wiki cache...", file=sys.stderr)
    wiki_items = wiki.fetch_canonical_items(WIKI_CACHE_DIR, use_cache=True, refresh=False)
    wiki_bonuses = wiki.fetch_all_bonuses(WIKI_CACHE_DIR, use_cache=True, refresh=False)
    print("Loading osrsbox cache...", file=sys.stderr)
    osrsbox = fetch_osrsbox(force=False)
    print("Merging...", file=sys.stderr)
    items_str, _wo, _oo, _disc = merge_wiki_osrsbox(wiki_items, wiki_bonuses, osrsbox)
    items_by_id = {int(k): v for k, v in items_str.items()}
    print(f"  {len(items_by_id)} merged items", file=sys.stderr)

    # We need the current Java state too because the slayer preserve_tabs
    # branch in build_synthetic_tabs() consults it.
    data_java = REPO_ROOT / "src/main/java/com/skillbank/SkillBankData.java"
    java_src = data_java.read_text()
    current, _ = parse_current_data(java_src)

    llm_json = OUT_DIR / "llm-classifications.json"
    print(f"Building flattened by_tab via llm_promote...", file=sys.stderr)
    _syn, _classified, report = llm_promote.build_synthetic_tabs(
        llm_json, mapping.TABS, items_by_id,
        current_membership=current,
        preserve_tabs=["slayer"],
    )

    # Reverse map: id -> sorted list of tab names.
    item_tabs: dict[int, list[str]] = {}
    for tab_name, tab_items in report["_by_tab"].items():
        for iid in tab_items:
            item_tabs.setdefault(iid, []).append(tab_name)
    for iid in item_tabs:
        item_tabs[iid].sort()

    # Emit JSONL — one line per item that has at least one tab.
    out_path = OUT_DIR / "audit-input.jsonl"
    n_written = 0
    total_tab_count = 0
    tabs_distribution: dict[int, int] = {}
    with out_path.open("w") as f:
        for iid in sorted(item_tabs):
            it = items_by_id.get(iid)
            if it is None:
                continue
            tabs_list = item_tabs[iid]
            record = {
                "id": iid,
                "name": it.get("name") or "",
                "current_tabs": tabs_list,
                "wiki_summary": _wiki_summary_for(it, WIKI_PAGE_CACHE_DIR),
                "osrsbox": _osrsbox_subset(it),
            }
            f.write(json.dumps(record, ensure_ascii=False, separators=(",", ":")))
            f.write("\n")
            n_written += 1
            total_tab_count += len(tabs_list)
            tabs_distribution[len(tabs_list)] = tabs_distribution.get(len(tabs_list), 0) + 1
            if n_written % 1000 == 0:
                print(f"  wrote {n_written} entries...", file=sys.stderr)

    avg_tabs = total_tab_count / n_written if n_written else 0.0
    print(f"\nWrote {out_path}: {n_written} entries", file=sys.stderr)
    print(f"  Total (item, tab) pairs: {total_tab_count}", file=sys.stderr)
    print(f"  Average tabs per item: {avg_tabs:.2f}", file=sys.stderr)
    print("  Distribution (tabs-per-item):", file=sys.stderr)
    for k in sorted(tabs_distribution):
        print(f"    {k:>2} tab(s): {tabs_distribution[k]}", file=sys.stderr)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
