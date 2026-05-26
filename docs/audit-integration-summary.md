# Audit Integration Summary — Brief #67

Cowork's exhaustive 64-session item audit (10 batches + a 50-item gap-fill) is now integrated as the source of truth for tab membership.

## Validation results

```
Input items     : 11,826  (out/audit-input.jsonl)
Decision items  : 11,826  (audit/decisions.jsonl)
Missing decisions: 0
Extra decisions  : 0
Duplicate ids    : 0
Items with empty tabs list: 0
Unknown tab names: 0
```

Confidence distribution: **high 8,798 · medium 2,976 · low 52** (0.4 % low-confidence).

Multi-tag distribution on the audit input:
| Tabs per item | Count |
|---|---|
| 1 | 8,760 |
| 2 | 2,629 |
| 3 | 293 |
| 4 | 89 |
| 5 | 55 |

## Per-tab counts (before → after, delta)

| Tab | Before | After | Δ |
|---|---|---|---|
| melee | 1,840 | 1,692 | **−148** |
| range | 812 | 862 | +50 |
| mage | 800 | 872 | +72 |
| prayer | 565 | 537 | −28 |
| cooking | 739 | 754 | +15 |
| woodcutting_firemaking | 489 | 209 | **−280** |
| fletching | 5 | 232 | **+227** |
| fishing | 222 | 197 | −25 |
| crafting | 417 | 317 | −100 |
| mining_smithing | 346 | 294 | −52 |
| herblore | 764 | 675 | −89 |
| agility_thieving | 238 | 234 | −4 |
| slayer | 575 | 623 | +48 |
| farming | 421 | 364 | −57 |
| runecraft | 158 | 155 | −3 |
| hunter | 288 | 256 | −32 |
| construction | 391 | 315 | −76 |
| misc | 2,594 | 1,938 | **−656** |
| quests | 2,141 | 1,816 | **−325** |
| sailing | 546 | 551 | +5 |
| cosmetics | 2,564 | 2,729 | +165 |
| teleports | 5 | 250 | **+245** |

Largest **gains**: `teleports +245`, `fletching +227`, `cosmetics +165` — those three are downstream of the audit applying the "Teleports" tab consistently (teleport tablets, jewellery teleports, scrolls), the Fletching/Woodcutting+Firemaking split deflating wc_fletching into Fletching, and the consolidated "cosmetic recolour applicator" rule routing more ornament kits to Cosmetics.

Largest **losses**: `misc −656`, `quests −325`, `woodcutting_firemaking −280`. Misc shrank because the audit's "inputs-only" rule strips misc-as-fallback from many items that have a real skill home; quests shrank because the "items used outside the quest stay in their functional tab" rule moved gear back to combat/skill tabs; woodcutting_firemaking shrank because fletching items split out.

## Item-level changes

- **9,285 items unchanged** (78.5 %)
- **2,541 items with changed tab membership** (21.5 %)
- **7 items lost** in the metadata pipeline (11,826 → 11,819) — these are items present in `audit-input.jsonl` but missing from the merged wiki+osrsbox dataset, so they have no metadata entry. They still appear in `SkillBankData.java` via the tab membership lists; only `item-meta.json` is missing them.

## Average tabs per item

| | Before | After |
|---|---|---|
| Average | 1.431 | 1.343 |
| 1-tab items | 7,476 | 8,467 |
| 2-tab items | 3,713 | 2,855 |
| 3-tab items | 548 | 347 |
| 4-tab items | 83 | 96 |
| 5-tab items | 5 | 54 |

The shift toward more 1-tab and more 5-tab items reflects the audit's two big patterns: (a) the inputs-only rule strips spurious cross-tags from common items (so more land in one tab only), and (b) the audit explicitly classifies all-style consumables / accessories into all 4–5 of {Mage, Melee, Range, Slayer, Cooking} where appropriate (so more land in many tabs).

## What changed in the pipeline

- **`tools/skillbank-data/out/llm-classifications.json`** — replaced. Pre-audit backup at `out/llm-classifications-pre-audit.json` (3.4 MB).
- **`tools/skillbank-data/llm_promote.py`** — the Brief #56 combat-bleed safety filter is now **disabled when the upstream source is `cowork-audit`**. The audit explicitly classifies all-style items into all of Mage+Melee+Range; the filter was silently collapsing 228 of those decisions back to a single combat tab. The filter remains active for any future raw-LLM source.
- **`src/main/java/com/skillbank/SkillBankData.java`** — regenerated.
- **`src/main/resources/com/skillbank/item-meta.json`** — regenerated (11,819 entries).

## Uncertain items

There is no `audit/uncertain.jsonl`. Cowork resolved every uncertain item in-line (the 52 low-confidence decisions carry rationale and went into `decisions.jsonl` directly). Per the handoff doc, the project is marked **PROJECT COMPLETE** as of 2026-05-25.

## Audit slices still awaiting Matt

Per the handoff, the following slices are still pending Matt's review. They do not block this integration — corrections are applied as `corrections-batch-NNN.csv` files that get spliced into `decisions.jsonl` and re-promoted later:

- `audit/audit-slice-batch-007.csv` — batch 7 (Treasure Trail rewards block, others)
- `audit/audit-slice-batch-009.csv` — Sailing onset + ornament-kit families
- `audit/audit-slice-batch-010.csv` — final batch (~71 weighted rows)
- `audit/audit-slice-gapfill-001.csv` — the 50-item gap that was found + filled (all 50 surfaced for review since none had been audited)

Each is logged in the same row format as earlier audits. When Matt returns each slice, the corrections apply in-place to `decisions.jsonl` by id, then re-run `--llm-promote`.

## Open design questions (per `audit/questions-for-matt.md`)

Most design questions were resolved in-flight (batches 5/6/7 audits applied as binding rulings). The handoff doc lists these still-open flags from later batches:

- **Skill-boost ales/wines** (sunbeam, blackbird, etc.) — boosted-skill tab vs Misc?
- **Skilling XP-boost outfits** (alchemist's, guild-hunter, etc.) — skill tab vs Cosmetics?
- **All-style Deadman sigils** tagged a single style vs Misc-only?
- **Tecu salamander** tri-style placement?
- **Hunter butterfly/moth heal & restore mixes** carrying spurious Melee?
- **Aranea boots / araxyte slayer helm (i)** tribrid-bonus handling?
- **Finished smithed/fletched weapons** combat-only vs production-skill (the Scurrius spine / synapse counterpoint)?
- **Soul bearer (19634) + bonecrusher necklace (22986)** — slayer-tool confirm (provisional Prayer-only since batch 8)?
- **Antifire family** — Herblore + Slayer overlay vs Herblore-only?

These affect a small number of items each. They can be addressed by future correction CSVs without re-running the whole audit.

## Pre-existing mapping.py overrides that conflict with the audit

A few stale `force_exclude` entries in `mapping.py` now contradict explicit audit decisions and may need cleanup in a follow-up brief:

- `cooking` force_excludes raw-fish-style names that the audit classifies as cooked food in cooking + combat (`Sardine`, `Salmon`, `Trout`, `Herring`, `Mackerel`). The audit puts these in Cooking + Melee + Range + Mage + Slayer; mapping.py removes Cooking, so they end up in 4 combat tabs without the cooking home they were meant to have. Suggested fix: audit the mapping.py force_exclude lists against the audit data and remove entries that contradict it. Not in scope for #67.

## Slayer-loadout overlay (per Cowork's batch-7 ruling)

`audit/slayer-loadout.md` defines the curated Slayer-tab overlay (task-mechanic items, loadout consumables, task-enhancing gear). The current integration does NOT yet apply this overlay as a force-include — slayer = 623 items entirely from audit `decisions.jsonl` for items that are slayer-task-locked. The overlay is a separate workstream Matt is expected to trim and apply via mapping.py force_include. The 347 "preserved" items in the slayer per-tab printout come from the legacy preserve_tabs mechanism, not the new loadout overlay.

## Build status

- `./gradlew build` → BUILD SUCCESSFUL
- Startup check: plugin loads, all subscribers registered, 0 exceptions

## Reproduce

```sh
# Snapshot before any changes
cp src/main/resources/com/skillbank/item-meta.json /tmp/item-meta-before.json
cp tools/skillbank-data/out/llm-classifications.json \
   tools/skillbank-data/out/llm-classifications-pre-audit.json

# Convert decisions.jsonl → llm-classifications.json (using TAB_DISPLAY_NAMES reverse map)
python3 -c "
import json, sys
sys.path.insert(0, 'tools/skillbank-data')
import sort_tables
decisions = [json.loads(l) for l in open('tools/skillbank-data/audit/decisions.jsonl')]
items = [{'id': d['id'], 'name': d['name'], 'rationale': d.get('rationale',''),
          'tabs': sorted(sort_tables.tab_id_from_display(t) for t in d['tabs'])}
         for d in decisions]
json.dump({'model': 'cowork-audit', 'count': len(items),
           'cached': 0, 'fresh': 0, 'errors': 0,
           'usage_totals': {'input_tokens':0,'output_tokens':0,
                            'cache_read_input_tokens':0,'cache_creation_input_tokens':0},
           'items': items}, open('tools/skillbank-data/out/llm-classifications.json','w'), indent=2)
"

# Regenerate Java + item-meta
python3 tools/skillbank-data/scraper.py --no-fetch --llm-promote
```
