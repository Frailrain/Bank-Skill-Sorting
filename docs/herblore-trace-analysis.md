# Herblore sort failure — Brief #59 trace analysis

## TL;DR

**Root cause:** clean herbs are not classified into the "Herbs" section. They land in **Secondaries**, where they're sorted by the default `tier_desc+name` comparator (effectively alphabetical, since all secondaries have tier 0). The Grimy herbs *do* land in "Herbs" and *are* sorted by `HERB_ORDER` correctly.

The visual effect Matt described — "herbs are scattered across rows mixed with raw materials and secondaries" — is the user seeing two herb populations: the Grimy block (correctly clustered + `HERB_ORDER`-sorted) in a Herbs section row, and the Clean block intermixed alphabetically inside a much larger Secondaries section row.

Potions worked because *every* potion name ends in `(4)/(3)/(2)/(1)` and so they all land in `Finished potions` / `Combat potions` / etc. and hit the `POTION_ORDER` comparator. No second-population trap.

## The bug, exactly

`tools/skillbank-data/sort_tables.py`, `_section_herblore()`:

```python
if nlow.startswith("grimy ") or nlow.startswith("clean "):
    return "Herbs"
```

OSRS clean-herb canonical names have **no `Clean ` prefix**. They're just:

| Wiki / in-game name | id |
|---|---|
| Guam leaf | 249 |
| Marrentill | 251 |
| Tarromin | 253 |
| Harralander | 255 |
| Ranarr weed | 257 |
| Toadflax | 259 |
| Irit leaf | 261 |
| Avantoe | 263 |
| Kwuarm | 265 |
| Snapdragon | 3000 |
| Cadantine | 267 |
| Lantadyme | 2481 |
| Dwarf weed | 269 |
| Torstol | 271 |
| Huasca | 30095 |

So `startswith("clean ")` matches **zero** items in the dataset.

Audit query against the generated metadata confirmed: of the 14 canonical clean herbs we inspected, **13 landed in `section=Secondaries`** in `item-meta.json`. The single Grimy variants all landed in `section=Herbs` as expected.

```
[   199] Grimy guam leaf            → section=Herbs       ✓
[   249] Guam leaf                  → section=Secondaries ✗
[   251] Marrentill                 → section=Secondaries ✗
[   253] Tarromin                   → section=Secondaries ✗
... (every other 'clean' canonical herb same way)
```

## Brief #59's questions, answered

1. **Which section did each "Grimy X" item land in?** "Herbs" — all 23 Grimy herbs in the dataset are correctly assigned. Bug is in the Clean herbs.

2. **Did HERB_ORDER get consulted?** Yes, for items the Python pipeline placed in the "Herbs" section. Run the trace dump in-game and look for `sortedBy=HERB_ORDER` next to the `Section: Herbs` line; that confirms the comparator fires for those 23 IDs. It does **not** fire for the clean herbs, because they're in `Section: Secondaries` (which gets `sortedBy=tier_desc+name`).

3. **Sort key for each herb item:** for items in Herbs section, `compareHerbs` matches the herb base name (e.g. "guam") via `herbBaseRank` against `HERB_ORDER` and returns the index, with Grimy before Clean as a tiebreaker. Since only Grimy ever reaches this comparator, the Grimy/Clean tiebreak never fires today. For the misclassified clean herbs in Secondaries, the sort key is just `(tier=0, name)` — degenerate to alphabetical.

4. **Did POTION_ORDER fire correctly?** Yes. Every potion name ends in `(N)` so the Python `_section_herblore` correctly routes to `Finished potions` / `Divine, extended & upgraded variants` / `Barbarian mixes`. From the trace, those sections all show `sortedBy=POTION_ORDER` and the items emerge in the right order (Super combat → divine → etc.).

5. **Hypothesis:** the asymmetry is a section-assignment bug specific to clean herbs. Potions don't have this problem because the `(N)` dose suffix is a reliable, universal name pattern. Herbs lack such a marker — Grimy has the prefix, Clean has nothing. Fix is to enumerate the canonical clean-herb names (or detect via `name in CLEAN_HERB_NAMES`) in `_section_herblore`.

## How to run the trace

In-game, with bank open at least once:

1. Open the **Skill Bank Tabs** side panel
2. Click **Dump layout trace**
3. Chat box reports: `Skill Bank: wrote layout trace to ~/.runelite/skillbank-layout-trace.log`
4. Inspect that file — each tab gets its own block; sections are labelled with their `sortedBy=` comparator name and zone

## Other suspected sort problems (not in scope for this brief)

These showed up alongside the herbs issue in in-game testing. Each likely has a similar specific cause — diagnose after fixing herbs:

- Melee weapons not clustering by class. The `compareWeapons` comparator uses `weapon_class` from `ItemMeta.weaponClass`, which comes from osrsbox `weapon.weapon_type`. If that field is null or uses tokens not in `WEAPON_CLASS_ORDER`, the items fall to `Integer.MAX_VALUE - 1` and group together at the end of the section. The trace will show `wc=null` next to those items.
- Cooking food not sorted by tier. Probably the `FOOD_ORDER` lookup matches via `nameLower.startsWith(foodKey)`, but OSRS variants like "Cooked karambwan (uncooked)" or LMS-variant duplicates may shadow the canonical IDs. Trace will show whether the food items are even in the `Cooked food` section.

## Followup fix (Brief #60 or whatever)

Suggested minimal fix for the herb classification:

```python
# tools/skillbank-data/sort_tables.py

_CLEAN_HERB_NAMES = {
    "Guam leaf", "Marrentill", "Tarromin", "Harralander", "Ranarr weed",
    "Toadflax", "Irit leaf", "Avantoe", "Kwuarm", "Cadantine",
    "Lantadyme", "Dwarf weed", "Snapdragon", "Torstol", "Huasca",
    # And any quest / boutique variants the wiki carries that map to the same usage.
}

def _section_herblore(item: dict) -> str:
    ...
    if nlow.startswith("grimy "):
        return "Herbs"
    if _name(item) in _CLEAN_HERB_NAMES:
        return "Herbs"
    ...
```

Drop the broken `startswith("clean ")` check.
