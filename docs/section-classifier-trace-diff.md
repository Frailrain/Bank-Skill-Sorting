# Section classifier — Brief #60 trace diff

Compares section assignments in `src/main/resources/com/skillbank/item-meta.json` before vs after the Brief #60 data-driven classifier rewrite.

## Summary of section moves

| Tab | Moves | Headline change |
|---|---|---|
| herblore | 23 | All 23 clean herbs moved Secondaries → Herbs (Brief #59 bug fixed) |
| prayer | 40 | Slot-driven assignment: 21 Bone-processing utility → Prayer equipment & robes; 16 Prayer equipment & robes → Holy symbols, books & blessings (god-book detection split) |
| firemaking | 2 | "Logs" (no prefix) + 1 other moved Firemaking outfit → Logs |
| farming | 28 | 22 items misclassified as Allotment seeds → Special seeds; 3 outfit items + 2 herb seeds → Special seeds (mushroom spores etc.) |
| cooking | 98 | 45 Ingredients → Cooked food, 24 Cooked food → Ingredients, 13 Cooked food → Special & combo (multi-step foods) |
| fishing | 165 | 138 false-positive "raw \*" items (raw rat meat, raw fox furs etc.) moved Raw fish → Fishing minigame items chaff bucket |

Tab-exclude post-filter:
- **prayer**: removed 1 (bird nest)
- **construction**: removed 2 (sailing ship parts / paint)

## Built ID sets (counts + samples logged at build time)

```
id_set[grimy_herbs]:           23 items (Grimy guam leaf, Grimy marrentill, Grimy tarromin)
id_set[clean_herbs]:           23 items (Guam leaf, Ranarr weed, Irit leaf)
id_set[logs]:                  14 items (Logs, Oak logs, Yew logs, Magic logs)
id_set[pyre_logs]:              9 items (Pyre logs, Yew pyre logs, Magic pyre logs)
id_set[cooked_food]:           50 items (Anglerfish, Shark, Roast beast meat)
id_set[special_food]:          35 items (Mud pie, Garden pie, Super kebab)
id_set[raw_cookables]:         37 items (Raw beef, Raw chicken, Raw anchovies)
id_set[raw_fish]:              26 items (Raw anchovies, Raw manta ray, Raw karambwan)
id_set[ingredients]:           54 items (Strawberry, Bowl of water, Flour)
id_set[fishing_tools]:         19 items (Fishing rod, Crystal harpoon, Fishbowl)
id_set[fishing_bait]:          11 items (Feather, Stripy feather, Fish offcuts)
id_set[herblore_secondaries]:  40 items (Limpwurt root, Snape grass, Volcanic sulphur)
id_set[seeds_allotment]:        8 items (Potato seed, Sweetcorn seed, ...)
id_set[seeds_hops]:             7 items (Barley seed, Jute seed, Hammerstone seed)
id_set[seeds_herb]:            15 items (Guam seed, Ranarr seed, Torstol seed)
id_set[seeds_flower]:           6 items (Marigold seed, White lily seed, ...)
id_set[seeds_bush]:             6 items (Redberry seed, Jangerberry seed, ...)
id_set[seeds_tree]:             6 items (Acorn, Willow seed, Magic seed)
id_set[seeds_fruit_tree]:      10 items (Apple tree seed, Palm tree seed, ...)
id_set[seeds_special]:         12 items (Mushroom spore, Hespori seed, Crystal acorn)
id_set[prayer_exclude]:         8 items (Bird nest variants)
id_set[construction_exclude]: 24 items (ship paint / ship plank / hull plate / ...)
```

## Spot-check verification

```
[199]  Grimy guam leaf      → herblore: Herbs ✓
[249]  Guam leaf            → herblore: Herbs ✓   (was Secondaries — the Brief #59 bug)
[257]  Ranarr weed          → herblore: Herbs ✓   (was Secondaries)
[1511] Logs                 → firemaking: Logs ✓, wc_fletching: Logs ✓
[1521] Oak logs             → firemaking: Logs ✓
[3446] Yew pyre logs        → firemaking: Pyre logs ✓
[5295] Ranarr seed          → farming: Herb seeds ✓
```

## What changed in code

**`tools/skillbank-data/sort_tables.py`** — new section at the top of the file:

- 18 canonical name lists (`HERB_BASE_NAMES`, `LOG_NAMES`, `COOKED_FOOD_NAMES`, etc.) plus a token list for construction-exclude.
- `init_id_sets(items_by_id, verbose=True)` builds module-level `_ID_SETS` from those lists by intersecting against the merged item dataset. Verbose mode prints counts + samples to stderr (Brief #60 verification requirement).
- `_is(item, set_name)` membership test.
- `tab_exclude_ids(tab)` exposed for the post-filter step.

**Section assignment functions** (`_section_herblore`, `_section_cooking`, `_section_firemaking`, `_section_fishing`, `_section_farming`, `_section_wc_fletching`, `_section_prayer`) — each rewritten to call `_is(item, ...)` first and fall back to name patterns only for the long-tail items not in any canonical set.

**`TAB_SECTIONS["prayer"]`** — equipment leads (was last), bones moved to second.

**`tools/skillbank-data/llm_promote.py`** — calls `sort_tables.init_id_sets(items_by_id)` once at the start of `build_synthetic_tabs`, and runs a final hard-exclude pass that removes items in `tab_exclude_ids(tab)` from `by_tab[tab]` (the bird-nests-from-prayer + sailing-from-construction fix).

**`src/main/java/com/skillbank/SkillBankSortData.java`** — `TAB_SECTIONS["prayer"]` order updated to match the Python side. Java is the runtime source-of-truth for in-game section ordering, so both sides must agree.

## Out of scope (deferred follow-up briefs)

- **mining_smithing**: brief flagged it as "filled with random shit" and asked to diagnose via trace dump first. The trace tool exists from Brief #59; an in-game dump would surface what's actually misclassified. No changes here.
- **Shark / Anglerfish / Manta ray not in cooking tab**: LLM-side decision — the model classified them as primary=slayer / cross=melee (loadout food) without cross-tagging them to cooking. The Brief #60 classifier rewrite is downstream of the LLM placement; reclassifying or adding cooking as a forced cross-tag for top food is a separate concern.
- **Weapon-class clustering for range/mage/crafting**: explicitly out of scope per the brief.
- **Tab structure changes** (wc/fletching split, agility/thieving merge): explicitly out of scope per the brief.

## Reproduce

```sh
# Snapshot before any changes
cp src/main/resources/com/skillbank/item-meta.json /tmp/item-meta-before.json

# (make changes)

# Regenerate
cd tools/skillbank-data && python3 scraper.py --no-fetch --llm-promote

# Diff per-(item, tab) section assignments
python3 -c '
import json
b = json.load(open("/tmp/item-meta-before.json"))
a = json.load(open("src/main/resources/com/skillbank/item-meta.json"))
# ... (see this docs file or the Brief #60 commit body for full script)
'
```
