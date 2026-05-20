"""Convert LLM classifications + mapping.py overrides into per-tab id lists.

Pipeline:
  out/llm-classifications.json  ──┐
                                  ├──▶ build_synthetic_tabs() ──▶ classified dict
  mapping.TABS overrides  ────────┘                                +
                                                                   synthetic TabSpecs
                                                                   │
                                                                   ▼
                                                          scraper.render_proposed()

The LLM returns one primary_tab + zero-or-more cross_tags per item. We:
  1. Apply mapping.py's force_include / force_exclude as hard overrides
     (name-based, applies to every ID sharing that name).
  2. Dedup items per-tab (an item from cross_tags + a force_include that says
     the same thing is one entry).
  3. Build a single-section TabSpec per tab so the rest of the renderer
     (which expects multiple sections) still works.
  4. Sort each tab's items alphabetically by lowercase name, then by ID.
"""
from __future__ import annotations

import json
from dataclasses import dataclass
from pathlib import Path

# Tabs that should ALWAYS exist in the output even if the LLM gave them
# zero items (keeps the SkillBankData.java structure stable across runs).
ALL_TAB_NAMES = [
    "melee", "range", "mage", "prayer", "cooking", "wc_fletching", "fishing",
    "firemaking", "crafting", "mining_smithing", "herblore", "agility_thieving",
    "slayer", "farming", "runecraft", "hunter", "construction", "misc",
    "quests", "sailing", "cosmetics",
]


def _collect_overrides(tabs) -> tuple[dict[str, set[str]], dict[str, set[str]]]:
    """Walk all mapping.TabSpec sections and pull force_include / force_exclude
    into per-tab name sets, ignoring the section structure."""
    forced_into: dict[str, set[str]] = {t.name: set() for t in tabs}
    forbidden_from: dict[str, set[str]] = {t.name: set() for t in tabs}
    for t in tabs:
        for sec in t.sections:
            for n in getattr(sec, "force_include", ()) or ():
                forced_into[t.name].add(n)
            for n in getattr(sec, "force_exclude", ()) or ():
                forbidden_from[t.name].add(n)
    return forced_into, forbidden_from


def _collect_extra_items(tabs) -> list[tuple[int, str, str]]:
    """Pull (id, name, tab_name) for every extra_item across all mapping tabs.
    These are items not in the osrsbox/wiki dump (e.g. post-cutoff Sailing IDs)."""
    out: list[tuple[int, str, str]] = []
    for t in tabs:
        for iid, name, _section_label in getattr(t, "extra_items", ()) or ():
            out.append((iid, name, t.name))
    return out


def build_synthetic_tabs(
    llm_json_path: Path,
    mapping_tabs: list,
    items_by_id: dict[int, dict],
):
    """Return (synthetic_tabs, classified_dict, report_dict).

    `synthetic_tabs` is a list[scraper.TabSpec], one per ALL_TAB_NAMES, each
    with a single 'LLM classified' section. The TabSpec is only used by
    render_block for labels and constant names; classification predicates
    are unused.

    `classified_dict` is shaped like scraper.classify() output:
      {tab_name: [ [(sort_key, id, name), ...] ]}

    `report_dict` summarizes per-tab counts + override applications.
    """
    # Late import to avoid circular dep when scraper imports this module.
    import scraper as scr  # type: ignore

    llm_data = json.loads(llm_json_path.read_text())
    llm_items = llm_data["items"]

    forced_into, forbidden_from = _collect_overrides(mapping_tabs)

    # name -> set[id], so name-based overrides reach every variant.
    name_to_ids: dict[str, set[int]] = {}
    for iid, it in items_by_id.items():
        if it.get("noted") or it.get("placeholder") or it.get("duplicate"):
            continue
        n = it.get("name") or ""
        if n:
            name_to_ids.setdefault(n, set()).add(iid)

    # tab -> {id: name} so we can dedup and re-emit sorted.
    by_tab: dict[str, dict[int, str]] = {t: {} for t in ALL_TAB_NAMES}

    override_adds: dict[str, list[tuple[int, str]]] = {t: [] for t in ALL_TAB_NAMES}
    override_drops: dict[str, list[tuple[int, str]]] = {t: [] for t in ALL_TAB_NAMES}

    for r in llm_items:
        iid = r["id"]
        name = r["name"]
        primary = r["primary_tab"]
        # Brief #53: drop LLM cross-tags entirely — primary_tab is the only
        # automatic signal. Intentional cross-tab overlaps (slayer loadout,
        # etc.) are declared as explicit TabSpec.extra_items in mapping.py.
        tabs_for_item: set[str] = {primary}

        # Apply force_include: any tab where this name is forced in.
        for tab_name in ALL_TAB_NAMES:
            if name in forced_into.get(tab_name, ()):
                if tab_name not in tabs_for_item:
                    override_adds[tab_name].append((iid, name))
                tabs_for_item.add(tab_name)

        # Apply force_exclude: drop any tab where this name is forbidden.
        for tab_name in list(tabs_for_item):
            if name in forbidden_from.get(tab_name, ()):
                tabs_for_item.discard(tab_name)
                override_drops[tab_name].append((iid, name))

        for tab_name in tabs_for_item:
            if tab_name not in by_tab:
                # LLM emitted a tab name we don't recognize — should never
                # happen because llm_classifier validates against VALID_TABS,
                # but defend anyway.
                continue
            by_tab[tab_name][iid] = name

    # Apply mapping.py extra_items — explicit (id, name, tab) overrides for
    # items the LLM didn't auto-place where we want them (e.g. Brief #50 slayer
    # loadout: prayer pots, top food, cannonballs, etc.) plus items missing from
    # the wiki/osrsbox dump (e.g. post-cutoff Sailing IDs).
    extra_count: dict[str, int] = {t: 0 for t in ALL_TAB_NAMES}
    for iid, name, tab_name in _collect_extra_items(mapping_tabs):
        if tab_name in by_tab and iid not in by_tab[tab_name]:
            by_tab[tab_name][iid] = name
            extra_count[tab_name] += 1

    # Build synthetic TabSpecs and the classified dict.
    classified: dict[str, list[list[tuple[object, int, str]]]] = {}
    synthetic_tabs: list = []
    const_name_lookup = {
        "melee":            "TAG_MELEE",
        "range":            "TAG_RANGE",
        "mage":             "TAG_MAGE",
        "prayer":           "TAG_PRAYER",
        "cooking":          "TAG_COOKING",
        "wc_fletching":     "TAG_WC_FLETCHING",
        "fishing":          "TAG_FISHING",
        "firemaking":       "TAG_FIREMAKING",
        "crafting":         "TAG_CRAFTING",
        "mining_smithing":  "TAG_MINING_SMITHING",
        "herblore":         "TAG_HERBLORE",
        "agility_thieving": "TAG_AGILITY_THIEVING",
        "slayer":           "TAG_SLAYER",
        "farming":          "TAG_FARMING",
        "runecraft":        "TAG_RUNECRAFT",
        "hunter":           "TAG_HUNTER",
        "construction":     "TAG_CONSTRUCTION",
        "misc":             "TAG_MISC",
        "quests":           "TAG_QUESTS",
        "sailing":          "TAG_SAILING",
        "cosmetics":        "TAG_COSMETICS",
    }

    for tab_name in ALL_TAB_NAMES:
        sec = scr.Section(
            label="LLM classified",
            classifier=lambda _it: False,  # unused in render path
        )
        tab_spec = scr.TabSpec(
            name=tab_name,
            const_name=const_name_lookup[tab_name],
            sections=[sec],
        )
        synthetic_tabs.append(tab_spec)

        items = [
            (name.lower(), iid, name)
            for iid, name in by_tab[tab_name].items()
        ]
        items.sort(key=lambda t: (t[0], t[1]))
        classified[tab_name] = [items]

    report = {
        "tabs": {
            tab_name: {
                "count": len(by_tab[tab_name]),
                "override_added": len(override_adds[tab_name]),
                "override_dropped": len(override_drops[tab_name]),
                "extra_items_added": extra_count[tab_name],
            }
            for tab_name in ALL_TAB_NAMES
        },
        "override_adds_sample": {
            tab: override_adds[tab][:5] for tab in ALL_TAB_NAMES if override_adds[tab]
        },
        "override_drops_sample": {
            tab: override_drops[tab][:5] for tab in ALL_TAB_NAMES if override_drops[tab]
        },
    }
    return synthetic_tabs, classified, report
