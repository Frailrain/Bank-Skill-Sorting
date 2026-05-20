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

Brief #55 added section structures + composite sort keys (Python-side).
Brief #58 moves the runtime sort to Java; Python's job here is now mostly
to drive section assignment and emit per-item metadata that the Java
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
    # tab -> {id: origin} where origin ∈ {"primary","cross","override"}.
    # Brief #54: drives primary-first sort order within each tab. "override"
    # covers force_include + extra_items + preserve_tabs.
    origin: dict[str, dict[int, str]] = {t: {} for t in ALL_TAB_NAMES}

    override_adds: dict[str, list[tuple[int, str]]] = {t: [] for t in ALL_TAB_NAMES}
    override_drops: dict[str, list[tuple[int, str]]] = {t: [] for t in ALL_TAB_NAMES}

    # Brief #56: combat cross-tag bleed filter. Combat gear should appear in
    # one combat tab only — Eclipse moon + Bandos chestplate + dragon bolts
    # kept cross-bleeding between melee/range/mage because the LLM lists the
    # adjacent styles as "useful for combat". Filter cross-tags between combat
    # tabs entirely; primary placement still governs which combat tab the item
    # lives in.
    _COMBAT_BLEED_BLOCK: dict[str, set[str]] = {
        "melee": {"range", "mage"},
        "range": {"melee", "mage"},
        "mage":  {"melee", "range"},
    }

    for r in llm_items:
        iid = r["id"]
        name = r["name"]
        primary = r["primary_tab"]
        cross = r.get("cross_tags") or []
        blocked = _COMBAT_BLEED_BLOCK.get(primary, set())
        if blocked:
            cross = [c for c in cross if c not in blocked]
        tabs_for_item: set[str] = {primary, *cross}

        # Per-item origin map; primary wins over cross when the LLM lists the
        # primary tab in its own cross_tags (shouldn't happen, but defend).
        item_origins: dict[str, str] = {primary: "primary"}
        for c in cross:
            if c != primary:
                item_origins[c] = "cross"

        # Apply force_include: any tab where this name is forced in.
        for tab_name in ALL_TAB_NAMES:
            if name in forced_into.get(tab_name, ()):
                if tab_name not in tabs_for_item:
                    override_adds[tab_name].append((iid, name))
                tabs_for_item.add(tab_name)
                # force_include only sets origin if not already primary/cross,
                # so a force_include that confirms the LLM choice doesn't
                # demote the item from "primary" to "override".
                item_origins.setdefault(tab_name, "override")

        # Apply force_exclude: drop any tab where this name is forbidden.
        for tab_name in list(tabs_for_item):
            if name in forbidden_from.get(tab_name, ()):
                tabs_for_item.discard(tab_name)
                item_origins.pop(tab_name, None)
                override_drops[tab_name].append((iid, name))

        for tab_name in tabs_for_item:
            if tab_name not in by_tab:
                # LLM emitted a tab name we don't recognize — should never
                # happen because llm_classifier validates against VALID_TABS,
                # but defend anyway.
                continue
            by_tab[tab_name][iid] = name
            origin[tab_name][iid] = item_origins.get(tab_name, "override")

    # Apply mapping.py extra_items (items the wiki/osrsbox dump didn't carry).
    extra_count: dict[str, int] = {t: 0 for t in ALL_TAB_NAMES}
    for iid, name, tab_name in _collect_extra_items(mapping_tabs):
        if tab_name in by_tab and iid not in by_tab[tab_name]:
            by_tab[tab_name][iid] = name
            origin[tab_name][iid] = "override"
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
                origin[tab_name][iid] = "override"
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
                origin[tab_name].pop(iid, None)
                excluded_counts[tab_name] += 1
        if excluded_counts[tab_name]:
            print(
                f"  tab_exclude[{tab_name}]: removed {excluded_counts[tab_name]} items",
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
            orig = origin[tab_name].get(iid, "override")
            sort_key = sort_tables.composite_sort_key(it, tab_name, section, orig)
            items_by_section[sec_idx].append((sort_key, iid, name))

        for s_items in items_by_section:
            s_items.sort(key=lambda t: t[0])

        classified[tab_name] = items_by_section

    # Per-tab group counts driven by `origin` map (Brief #54).
    group_counts: dict[str, dict[str, int]] = {}
    for tab_name in ALL_TAB_NAMES:
        counts = {"primary": 0, "cross": 0, "override": 0}
        for iid in by_tab[tab_name]:
            counts[origin[tab_name].get(iid, "override")] += 1
        group_counts[tab_name] = counts

    report = {
        "tabs": {
            tab_name: {
                "count": len(by_tab[tab_name]),
                "primary": group_counts[tab_name]["primary"],
                "cross": group_counts[tab_name]["cross"],
                "override": group_counts[tab_name]["override"],
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
    }
    # Attach the by_tab + items map to the report so the caller can also
    # emit per-item metadata (item-meta.json) without re-walking everything.
    report["_by_tab"] = by_tab
    report["_items_by_id"] = items_by_id
    report["_origin"] = origin
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


def _infer_category(item: dict, primary_tab: str) -> str:
    """Coarse role bucket. The Java layout builder uses this mostly for
    consumables vs gear within a section; section assignment itself is
    already decided by sort_tables.assign_section()."""
    name = (item.get("name") or "").lower()
    eq = item.get("equipment") or {}
    slot = (eq.get("slot") or "").lower()
    if primary_tab in ("melee", "range", "mage") and slot:
        return "combat"
    if primary_tab in ("cooking",) or "potion" in name or " brew" in name \
            or " serum" in name or " mix(" in name or name.startswith("cooked "):
        return "consumable"
    if any(n in name for n in ("pickaxe", "axe", "fishing rod", "harpoon",
                                "tinderbox", "secateurs", "watering can",
                                "spade", "rake", "knife", "chisel", "needle",
                                "hammer", "saw")) and slot in ("weapon", "2h", ""):
        return "skill_tool"
    if primary_tab in ("cosmetics", "quests"):
        return "cosmetic"
    if primary_tab in ("herblore", "crafting", "mining_smithing", "wc_fletching",
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
        # Pick the most reliable primary signal for category — actual primary
        # is in LLM data; fall back to any tab the item is in.
        primary_tab = next(iter(tabs), "misc")
        category = _infer_category(it, primary_tab)

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
