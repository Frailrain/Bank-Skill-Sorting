"""Convert LLM classifications + mapping.py overrides into per-tab id lists.

Pipeline:
  out/llm-classifications.json  ──┐
                                  ├──▶ build_synthetic_tabs() ──▶ classified dict
  mapping.TABS overrides  ────────┘                                +
                                                                   synthetic TabSpecs
                                                                   │
                                                                   ▼
                                                          scraper.render_proposed()

Brief #62 flattened the LLM schema. Each item now carries a `tabs: [list]`
field — a single flat set of tab memberships. The old primary_tab /
cross_tags distinction is gone. mapping.py force_include / force_exclude
still apply as name-based add / remove operations on each item's tab set.

Brief #55 introduced section structures + composite sort keys. Brief #58
moved runtime sort to Java; Python's job here is now mostly to drive
section assignment and emit per-item metadata that the Java
SkillBankLayoutBuilder reads from /com/skillbank/item-meta.json. The
emit_item_metadata() function at the bottom of this file produces that
artifact.
"""
from __future__ import annotations

import json
from dataclasses import dataclass
from pathlib import Path

# Tabs that should ALWAYS exist in the output even if the LLM gave them
# zero items (keeps the SkillBankData.java structure stable across runs).
ALL_TAB_NAMES = [
    "melee", "range", "mage", "prayer", "cooking",
    "woodcutting_firemaking", "fletching",
    "fishing", "crafting", "mining_smithing",
    "herblore", "agility_thieving",
    "slayer", "farming", "runecraft", "hunter", "construction", "misc",
    "quests", "sailing", "cosmetics", "teleports",
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


# Combat-bleed safety net (Brief #56 + #62): a single item must not appear in
# more than one of melee/range/mage simultaneously. The flatten in Brief #62
# already de-bled the cached data; this filter catches anything that slips
# through future audit results. When triggered we keep the first combat tab in
# canonical order (melee → range → mage) and drop the others.
_COMBAT_TABS_ORDER = ("melee", "range", "mage")
_COMBAT_TABS_SET = set(_COMBAT_TABS_ORDER)


def _apply_combat_bleed_safety(tabs: set[str]) -> tuple[set[str], list[str]]:
    """If `tabs` contains more than one combat tab, keep the first per
    _COMBAT_TABS_ORDER and drop the rest. Returns (filtered_set, dropped_list)."""
    combat_present = [c for c in _COMBAT_TABS_ORDER if c in tabs]
    if len(combat_present) <= 1:
        return tabs, []
    keep = combat_present[0]
    drop = combat_present[1:]
    return tabs - set(drop), drop


def build_synthetic_tabs(
    llm_json_path: Path,
    mapping_tabs: list,
    items_by_id: dict[int, dict],
    *,
    current_membership: dict[str, list[int]] | None = None,
    preserve_tabs: list[str] | None = None,
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
    import sort_tables  # type: ignore

    # Brief #60: build the ID sets that drive data-driven section assignment.
    # Must run before any sort_tables.assign_section() call below.
    sort_tables.init_id_sets(items_by_id, verbose=True)

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
    combat_bleed_safety_hits = 0

    for r in llm_items:
        iid = r["id"]
        name = r["name"]
        tabs_for_item: set[str] = set(r.get("tabs") or [])

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

        # Safety net: an item cannot belong to more than one combat tab.
        tabs_for_item, dropped_combat = _apply_combat_bleed_safety(tabs_for_item)
        if dropped_combat:
            combat_bleed_safety_hits += 1

        for tab_name in tabs_for_item:
            if tab_name not in by_tab:
                # LLM emitted a tab name we don't recognize — should never
                # happen because llm_classifier validates against VALID_TABS,
                # but defend anyway.
                continue
            by_tab[tab_name][iid] = name

    # Apply mapping.py extra_items (items the wiki/osrsbox dump didn't carry).
    extra_count: dict[str, int] = {t: 0 for t in ALL_TAB_NAMES}
    for iid, name, tab_name in _collect_extra_items(mapping_tabs):
        if tab_name in by_tab and iid not in by_tab[tab_name]:
            by_tab[tab_name][iid] = name
            extra_count[tab_name] += 1

    # Preserve membership for designated tabs (additive): every item currently
    # in tab X stays in tab X regardless of LLM. Built for the Brief #50 slayer
    # loadout cross-tag design — the LLM doesn't know "this is a slayer task
    # food/potion/cannonball", so we keep manual curation as truth.
    preserved: dict[str, list[tuple[int, str]]] = {t: [] for t in ALL_TAB_NAMES}
    if current_membership and preserve_tabs:
        for tab_name in preserve_tabs:
            if tab_name not in by_tab:
                continue
            for iid in current_membership.get(tab_name, []):
                if iid in by_tab[tab_name]:
                    continue
                it = items_by_id.get(iid)
                if it is None or it.get("noted") or it.get("placeholder") or it.get("duplicate"):
                    continue
                name = it.get("name") or ""
                if not name:
                    continue
                by_tab[tab_name][iid] = name
                preserved[tab_name].append((iid, name))

    # Brief #60: hard tab-exclude pass — items the LLM cross-tagged into a tab
    # they don't belong in (bird nests in prayer, sailing pieces in
    # construction, etc.). Names + IDs come from sort_tables.tab_exclude_ids,
    # populated by init_id_sets above.
    excluded_counts: dict[str, int] = {t: 0 for t in ALL_TAB_NAMES}
    for tab_name in ALL_TAB_NAMES:
        excl = sort_tables.tab_exclude_ids(tab_name)
        if not excl:
            continue
        for iid in list(by_tab[tab_name].keys()):
            if iid in excl:
                del by_tab[tab_name][iid]
                excluded_counts[tab_name] += 1
        if excluded_counts[tab_name]:
            print(
                f"  tab_exclude[{tab_name}]: removed {excluded_counts[tab_name]} items",
                file=__import__("sys").stderr,
            )

    if combat_bleed_safety_hits:
        print(
            f"  combat-bleed safety: filtered {combat_bleed_safety_hits} items "
            f"that listed multiple combat tabs",
            file=__import__("sys").stderr,
        )

    # Build synthetic TabSpecs and the classified dict.
    classified: dict[str, list[list[tuple[object, int, str]]]] = {}
    synthetic_tabs: list = []
    const_name_lookup = {
        "melee":            "TAG_MELEE",
        "range":            "TAG_RANGE",
        "mage":             "TAG_MAGE",
        "prayer":           "TAG_PRAYER",
        "cooking":          "TAG_COOKING",
        "woodcutting_firemaking": "TAG_WOODCUTTING_FIREMAKING",
        "fletching":        "TAG_FLETCHING",
        "fishing":          "TAG_FISHING",
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
        "teleports":        "TAG_TELEPORTS",
    }

    # Brief #55: multi-section TabSpecs with deterministic section assignment
    # and composite within-section sort keys. Section structure + per-tab
    # assignment rules live in sort_tables.py.
    import sort_tables  # type: ignore

    for tab_name in ALL_TAB_NAMES:
        section_names = sort_tables.TAB_SECTIONS[tab_name]
        sec_objs = [
            scr.Section(label=s, classifier=lambda _it: False)
            for s in section_names
        ]
        tab_spec = scr.TabSpec(
            name=tab_name,
            const_name=const_name_lookup[tab_name],
            sections=sec_objs,
        )
        synthetic_tabs.append(tab_spec)

        # Bucket items into per-section lists with composite sort keys.
        items_by_section: list[list[tuple[object, int, str]]] = [[] for _ in section_names]
        for iid, name in by_tab[tab_name].items():
            it = items_by_id.get(iid, {"id": iid, "name": name})
            section = sort_tables.assign_section(it, tab_name)
            sec_idx = section_names.index(section)
            sort_key = sort_tables.composite_sort_key(it, tab_name, section)
            items_by_section[sec_idx].append((sort_key, iid, name))

        for s_items in items_by_section:
            s_items.sort(key=lambda t: t[0])

        classified[tab_name] = items_by_section

    report = {
        "tabs": {
            tab_name: {
                "count": len(by_tab[tab_name]),
                "override_added": len(override_adds[tab_name]),
                "override_dropped": len(override_drops[tab_name]),
                "extra_items_added": extra_count[tab_name],
                "preserved": len(preserved[tab_name]),
            }
            for tab_name in ALL_TAB_NAMES
        },
        "override_adds_sample": {
            tab: override_adds[tab][:5] for tab in ALL_TAB_NAMES if override_adds[tab]
        },
        "override_drops_sample": {
            tab: override_drops[tab][:5] for tab in ALL_TAB_NAMES if override_drops[tab]
        },
        "preserved_sample": {
            tab: preserved[tab][:5] for tab in ALL_TAB_NAMES if preserved[tab]
        },
        "combat_bleed_safety_hits": combat_bleed_safety_hits,
    }
    # Attach the by_tab + items map to the report so the caller can also
    # emit per-item metadata (item-meta.json) without re-walking everything.
    report["_by_tab"] = by_tab
    report["_items_by_id"] = items_by_id
    return synthetic_tabs, classified, report


# ── Brief #58: per-item metadata for the Java runtime layout builder ──────

# Extended tier table. Keys are LOWER-CASE substrings searched in item names.
# Order is longest-token-first so "dragon hunter" beats "dragon", "blue moon"
# beats "rune", etc. Endgame items get tier values above dragon so the
# two-zone "top N tiers" logic picks them up correctly.
_TIER_TOKENS: list[tuple[str, int]] = [
    # Newer endgame meta-defining items get the highest ranks.
    ("scythe of vitur", 115),
    ("tumeken's shadow", 115),
    ("ancestral", 110),
    ("masori", 110),
    ("justiciar", 110),
    ("torva ", 110), ("torva", 110),
    ("virtus ", 110), ("virtus", 110),
    ("twisted bow", 110),
    ("ghrazi rapier", 110),
    ("sanguine ", 110),
    ("dragon hunter crossbow", 108),
    ("dragon hunter lance", 108),
    ("inquisitor", 105),
    ("kodai", 105),
    ("elder maul", 100),
    # God Wars second-age.
    ("bandos", 95),
    ("armadyl", 95),
    ("zamorakian", 95),
    ("staff of the dead", 95),
    ("ancient godsword", 95),
    ("saradomin sword", 95),
    ("saradomin's tear", 95),
    # Third age cosmetic/PvM hybrid.
    ("3rd age", 92), ("third age", 92),
    # Abyssal-tier weapons.
    ("abyssal whip", 87),
    ("abyssal tentacle", 87),
    ("abyssal bludgeon", 87),
    ("abyssal dagger", 87),
    # Crystal & moons sit just below abyssal / above dragon.
    ("blue moon", 84),
    ("blood moon", 84),
    ("eclipse moon", 84),
    ("crystal", 86),
    # Barrows brothers.
    ("ahrim's", 85), ("dharok's", 85), ("guthan's", 85),
    ("karil's", 85), ("torag's", 85), ("verac's", 85),
    ("barrows", 85),
    # Vanilla metal tiers.
    ("dragon", 80),
    ("granite", 75),
    ("rune", 70),
    ("adamant", 60),
    ("mithril", 50),
    ("white", 45),
    ("black", 40),
    ("steel", 30),
    ("iron", 20),
    ("bronze", 10),
]


def _infer_item_tier(name: str) -> int:
    """Return a numeric tier rank (higher = stronger). Zero means 'unknown'
    — those items land in the chaff zone since the loadout zone picks
    items at the top N known tier values."""
    n = name.lower()
    for token, tier in _TIER_TOKENS:
        if token in n:
            return tier
    return 0


def _infer_category(item: dict, sample_tab: str) -> str:
    """Coarse role bucket. The Java layout builder uses this mostly for
    consumables vs gear within a section; section assignment itself is
    already decided by sort_tables.assign_section().

    `sample_tab` is any one of the tabs the item belongs to — used as a hint
    for the category heuristic. Items in multiple tabs get a deterministic
    pick from sorted(tabs) at the caller."""
    name = (item.get("name") or "").lower()
    eq = item.get("equipment") or {}
    slot = (eq.get("slot") or "").lower()
    if sample_tab in ("melee", "range", "mage") and slot:
        return "combat"
    if sample_tab in ("cooking",) or "potion" in name or " brew" in name \
            or " serum" in name or " mix(" in name or name.startswith("cooked "):
        return "consumable"
    if any(n in name for n in ("pickaxe", "axe", "fishing rod", "harpoon",
                                "tinderbox", "secateurs", "watering can",
                                "spade", "rake", "knife", "chisel", "needle",
                                "hammer", "saw")) and slot in ("weapon", "2h", ""):
        return "skill_tool"
    if sample_tab in ("cosmetics", "quests"):
        return "cosmetic"
    if sample_tab in ("herblore", "crafting", "mining_smithing",
                        "woodcutting_firemaking", "fletching",
                        "farming", "runecraft"):
        return "material"
    return "misc"


def emit_item_metadata(
    by_tab: dict[str, dict[int, str]],
    items_by_id: dict[int, dict],
    mapping_tabs: list,
    output_json_path: Path,
) -> dict:
    """Build per-item metadata + section map, write JSON to disk for the
    Java runtime layout builder. Returns the dict for inspection.

    Schema (per item id, keyed by id-as-string for JSON compatibility):
      {
        "t": int tier,
        "wc": str|null weapon_class,
        "sl": str|null slot,
        "ct": str category,
        "s": {tab_name: section_name, ...}
      }
    """
    import sort_tables  # type: ignore

    # Reverse by_tab: id -> set of tab names the item lives in (post-bleed-filter).
    item_tabs: dict[int, set[str]] = {}
    for tab_name, tab_items in by_tab.items():
        for iid in tab_items:
            item_tabs.setdefault(iid, set()).add(tab_name)

    meta: dict[str, dict] = {}
    for iid, tabs in item_tabs.items():
        it = items_by_id.get(iid)
        if not it:
            continue
        name = it.get("name") or ""
        eq = it.get("equipment") or {}
        wp = it.get("weapon") or {}

        tier = _infer_item_tier(name)
        slot = eq.get("slot") or None
        weapon_class = wp.get("weapon_type") or None
        # Deterministic category hint: pick the alphabetically first tab the
        # item belongs to. Category is a coarse role bucket so the exact
        # choice rarely matters — picking by sorted order keeps the artifact
        # stable across runs.
        sample_tab = sorted(tabs)[0] if tabs else "misc"
        category = _infer_category(it, sample_tab)

        sections: dict[str, str] = {}
        for tab in sorted(tabs):
            sections[tab] = sort_tables.assign_section(it, tab)

        entry: dict = {"t": tier, "ct": category, "s": sections}
        # Omit null weaponClass / slot from the JSON to keep file size down.
        if weapon_class:
            entry["wc"] = weapon_class
        if slot:
            entry["sl"] = slot
        meta[str(iid)] = entry

    output_json_path.parent.mkdir(parents=True, exist_ok=True)
    output_json_path.write_text(json.dumps(meta, separators=(",", ":")))
    return meta
