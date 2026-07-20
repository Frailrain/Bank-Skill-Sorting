# Skill Bank Tabs

A RuneLite plugin that automatically organises your bank into skill-based [Bank Tags](https://github.com/runelite/runelite/wiki/Bank-Tags) tabs — one tidy tab per skill from the moment you log in, with sectioned layouts, labelled dividers, and a slayer tab that rebuilds itself around your current task.

Items live in **every tab where you'd want them visible** — raw fish appear under both Fishing and Cooking, logs under both Woodcutting + Firemaking and Fletching, the rune pouch under both Mage and Runecraft.

## Features

- **22 skill-based tabs** covering all 23 OSRS skills (Woodcutting + Firemaking, Mining + Smithing, and Agility + Thieving share combined tabs), plus dedicated Teleports, Cosmetics, Misc, and Quests tabs.
- **`(auto)` tag naming** — every tab this plugin manages is suffixed `(auto)` (e.g. `melee (auto)`, `woodcutting + firemaking (auto)`), so it can never collide with or overwrite a tag you created yourself.
- **Sectioned layouts with labelled dividers** — each tab is organised into named sections (raw → cooked → burnt rows in Cooking, launcher → ammo rows in Ranged, seed types in Farming…) with a Quest Helper-style divider line and label above each section. Toggle the dividers off in settings for a compact layout.
- **Dynamic slayer tab** — when you have a slayer task, the slayer tab rebuilds itself: required and protection items for the task, the OSRS wiki's recommended gear per combat style *filtered to what you actually own* (slot-by-slot, ranked), elemental-weakness tomes, potion chains knocked down to what's in your bank, top food, cannon supplies where your task location allows one, and slayer utility. Variant-aware — a Konar task at a specific location gets that location's requirements, not another variant's. Falls back to a static slayer tab with no task; toggleable in settings.
- **Self-maintaining** — tabs reconcile on every login: items added in updates appear, items removed in updates disappear, layouts rebuild against your current bank. Note: the plugin owns the contents of its `(auto)` tags; manual edits to those tags are overwritten on the next seed. Your own tags are never touched.
- **Per-tab toggles**, a **reseed** button, and a confirm-gated **reset** that removes everything the plugin created.

## Requirements

- The **Bank Tags** plugin (built into RuneLite) must be enabled. Bank Tag Layouts is **not** required — layouts use RuneLite's native bank tag layout support.

## Usage

1. Enable **Bank Tags** in your RuneLite plugin list.
2. Install **Skill Bank Tabs** from the Plugin Hub.
3. Log in — tabs seed automatically (or use **Seed missing tags** in the side panel).
4. Get a slayer task and open the slayer `(auto)` tab to see the dynamic loadout.

## Tabs

| Tab | Contents |
| --- | --- |
| `melee` / `range` / `mage` | Gear by slot (strongest left), food, potions, then lower-tier gear |
| `prayer` | Equipment, bones & ashes, ensouled heads, restores, bonemeal & offerings |
| `cooking` | Raw → cooked → burnt rows per family, ingredients, combos, drinks & brews |
| `woodcutting + firemaking` | Axes, light sources, forestry kit, logs, pyre logs, Wintertodt |
| `fletching` | Materials-first: logs, feathers, arrows, bows, crossbows, bolts, darts |
| `fishing` | Tools, bait, outfit, raw fish, trophies, Tempoross |
| `crafting` | Moulds, tools, gems (family rows), hides, glass, battlestaves, jewellery |
| `mining + smithing` | Pickaxes, ores, special ores & minerals, bars, smithed parts, Foundry |
| `herblore` | Tools, herbs (level order), secondaries, unfinished & finished potions |
| `agility + thieving` | Graceful, run energy, Sepulchre rewards, rogue kit, thieving loot |
| `slayer` | Dynamic per-task loadout (see above); static task gear otherwise |
| `farming` | Tools, compost, seeds by patch type, saplings, harvested produce |
| `runecraft` | Essence, pouches, talismans, tiaras, runes, GotR |
| `hunter` | Traps, baits, salamanders, butterflies, furs, meats, implings |
| `construction` | Tools, planks, nails, materials, flatpacks, garden, mounted decor |
| `sailing` | Navigation, construction kit rows, ship parts, salvage, sea fish, cargo |
| `teleports` | Runes, jewellery, tablets, scrolls, diary rewards, wilderness |
| `cosmetics` | Purely visual items, one row per outfit set |
| `misc` | Currencies, keys, clue items, books, storage, lamps — the true junk drawer |
| `quests` | Quest-locked items: keys, books & lore, supplies, artefacts, remains |

## Notes & Contributing

- **Item data is generated** from the OSRS Wiki and osrsbox, then curated. If an item is in the wrong tab or section, open an issue with the item name and where it should live — most fixes are one-line overrides.
- **Duplicating items across tabs is intentional.** Bank Tags treats each tag as an independent set; cross-tagging is the point.
- The slayer task dataset is parsed from wiki task pages (recommended-equipment templates + variant/location requirements) and ships with the plugin — no network calls at runtime.
