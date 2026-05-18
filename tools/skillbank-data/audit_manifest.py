#!/usr/bin/env python3
"""Generate the full per-item audit manifest.

Outputs out/full-audit.csv with one row per merged canonical item:
  id, name, slot, weapon_type, members, equipable_weapon, quest_item,
  classifier_tabs (semicolon-joined), source

The "classifier_tabs" column is the authoritative source of truth for what
the current classifier assigns; per-item audit decisions in audit-log.md
reference this manifest.
"""
from __future__ import annotations

import csv
import json
import sys
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
sys.path.insert(0, str(SCRIPT_DIR))

import mapping  # type: ignore
import wiki  # type: ignore
from scraper import (
    fetch_osrsbox, merge_wiki_osrsbox, classify, CACHE_DIR, OUT_DIR,
    WIKI_CACHE_DIR,
)


def main():
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    WIKI_CACHE_DIR.mkdir(parents=True, exist_ok=True)

    print("Loading wiki cache...", file=sys.stderr)
    wiki_items = wiki.fetch_canonical_items(WIKI_CACHE_DIR, use_cache=True, refresh=False)
    wiki_bonuses = wiki.fetch_all_bonuses(WIKI_CACHE_DIR, use_cache=True, refresh=False)

    print("Loading osrsbox cache...", file=sys.stderr)
    osrsbox = fetch_osrsbox(force=False)

    print("Merging...", file=sys.stderr)
    items, wiki_only, osrsbox_only, discrepancies = merge_wiki_osrsbox(
        wiki_items, wiki_bonuses, osrsbox,
    )
    print(f"  {len(items)} merged items", file=sys.stderr)

    print("Classifying against all 20 tabs...", file=sys.stderr)
    classified = classify(items, mapping.TABS)

    # Build reverse index: item_id -> list of (tab, section_label)
    item_assignments: dict[int, list[tuple[str, str]]] = {}
    for tab in mapping.TABS:
        for i, sec_items in enumerate(classified[tab.name]):
            for sk, iid, name in sec_items:
                item_assignments.setdefault(iid, []).append((tab.name, tab.sections[i].label))

    out_csv = OUT_DIR / "full-audit.csv"
    with out_csv.open("w", newline="") as f:
        w = csv.writer(f)
        w.writerow([
            "id", "name", "slot", "weapon_type", "members", "tradeable",
            "equipable", "equipable_weapon", "quest_item",
            "noted", "placeholder", "duplicate",
            "release_date", "source",
            "classifier_tab_count", "classifier_tabs", "classifier_sections",
        ])
        for iid_str, it in sorted(items.items(), key=lambda kv: int(kv[0])):
            iid = int(iid_str)
            eq = it.get("equipment") or {}
            wp = it.get("weapon") or {}
            assignments = item_assignments.get(iid, [])
            tabs_str = ";".join(a[0] for a in assignments)
            secs_str = ";".join(f"{a[0]}/{a[1]}" for a in assignments)
            w.writerow([
                iid,
                it.get("name", ""),
                eq.get("slot") or "",
                wp.get("weapon_type") or "",
                int(bool(it.get("members"))),
                int(bool(it.get("tradeable"))),
                int(bool(it.get("equipable"))),
                int(bool(it.get("equipable_weapon"))),
                int(bool(it.get("quest_item"))),
                int(bool(it.get("noted"))),
                int(bool(it.get("placeholder"))),
                int(bool(it.get("duplicate"))),
                it.get("release_date") or "",
                it.get("_source", "?"),
                len(assignments),
                tabs_str,
                secs_str,
            ])

    print(f"Wrote {out_csv}: {len(items)} rows", file=sys.stderr)
    print(f"  items in 0 tabs:  {sum(1 for it in items if int(it) not in item_assignments)}")
    print(f"  items in 1 tab:   {sum(1 for iid, a in item_assignments.items() if len(a) == 1)}")
    print(f"  items in 2+ tabs: {sum(1 for iid, a in item_assignments.items() if len(a) >= 2)}")


if __name__ == "__main__":
    main()
