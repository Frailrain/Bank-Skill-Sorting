# Bank Skill Sorting

A RuneLite plugin that automatically seeds [Bank Tags](https://github.com/runelite/runelite/wiki/Bank-Tags) tag groups with skill-based item categories, so your bank is organised into one tidy tab per skill the moment you log in.

Items live in **every tab where you'd want them visible** — raw fish appear under both Fishing and Cooking, logs appear under both Woodcutting/Fletching and Firemaking, the rune pouch shows up under both Mage and Runecraft, and so on. The plugin never overwrites tags you've already created — it only fills in the ones that are missing.

## Features

- **20 skill-based tag groups** covering all 23 OSRS skills (the multi-skill tabs combine WC+Fletching, Mining+Smithing, and Agility+Thieving), plus a `misc` tab for teleports/keys/clues and a `quests` tab for quest, diary, and minigame rewards.
- **Thoughtful categorisation** — raw inputs sit in the skill that produces them, processed outputs sit in the skill that consumes them.
- **Multi-tagging** — cross-skill items (combat potions, slayer helms, hammers, planks, cannons, graceful, herb sack, etc.) are duplicated across every tab where they belong.
- **Per-tab toggles** in the config panel to enable or disable individual tabs.
- **Reseed missing tags** button — re-runs seeding for any tag that's currently empty without touching the rest.
- **Reset all tags** button (gated behind a confirm checkbox) clears every tag this plugin manages, so you can start clean.
- **Side panel** showing the current seeded state of every tag with green checkmarks.
- **Non-destructive** — existing tag contents are never overwritten. If a tag already has any contents, it's left alone.

## Requirements

- The **Bank Tags** plugin must be installed and active. This plugin writes to Bank Tags' configuration; without it, the seeded data has nothing to render.

## Usage

1. Make sure the **Bank Tags** plugin is enabled in your RuneLite plugin list.
2. Install **Bank Skill Sorting** from the Plugin Hub.
3. Open the side panel and click **Seed missing tags** (or just log in — seeding on startup is on by default).
4. Reopen your bank — the new skill tabs will be there.

To start fresh: tick **Confirm reset** in the config, then click **Reset all tags**. To rebuild a single missing tab, just delete it in Bank Tags and click **Seed missing tags** again.

## Tabs

| Tab | Contents |
| --- | --- |
| `melee` | Weapons, armour, combat potions, high-heal food, slayer helms, cannon parts |
| `range` | Bows, crossbows, ammo, d'hide, blowpipes, ranging potions, cannon parts |
| `mage` | Runes, staves, magic armour, rune pouch, teleport runes |
| `prayer` | Bones, ashes, prayer potions, ensouled heads |
| `cooking` | Cooked + raw food, ingredients, cook's outfit, gauntlets |
| `wc_fletching` | Logs, axes, bow strings, unstrung bows, dart tips, forestry items |
| `fishing` | Raw fish, rods, bait, angler outfit, fish barrel, Tempoross gear |
| `firemaking` | Tinderbox, all logs, pyromancer outfit, firelighters, pyre logs |
| `crafting` | Gems, moulds, leather, dragonhide, glass, jewellery bars |
| `mining_smithing` | Ores, bars, pickaxes, prospector, foundry uniform, coal/gem bag |
| `herblore` | Herbs, secondaries, every potion dose, vials, herb sack |
| `agility_thieving` | Graceful (all recolours), marks, rogue set, blackjacks, ardougne cloak |
| `slayer` | Task gear, slayer helms, enchanted gem, rings, blood essence |
| `farming` | Seeds, tools, compost, farmer outfit, herb sack, stamina pots |
| `runecraft` | Essence, talismans, tiaras, pouches, raiments of the eye |
| `hunter` | Traps, impling jars, camo outfits, salamanders, quetzals |
| `construction` | Planks, nails, saw, limestone, bars, mahogany homes packs |
| `sailing` | Sailing fish, navigation tools, repair kits, sailing cape *(provisional — see notes)* |
| `misc` | Teleport jewellery, tablets, clue scrolls, keys, storage bags, fairy ring tools |
| `quests` | Quest rewards, diary gear, minigame gear, boss uniques and pets |

## Notes & Contributing

- **Item IDs are community maintained.** They come from `net.runelite.api.ItemID` and the OSRS Wiki. PRs to expand, correct, or re-bucket items are welcome — open an issue or send a patch against [`SkillBankData.java`](src/main/java/com/skillbank/SkillBankData.java).
- **The Sailing tab is provisional.** Sailing was released on 2025-11-19 and many of its items don't have stable IDs in `ItemID` yet. The current bucket covers the cape, navigation tools (spyglass, crowbar, captain's log), repair kits, and the new fish — best-effort entries are flagged with `// verify ID` comments. Expect the Sailing tab to grow as the skill matures and IDs settle.
- **Duplicating IDs across tabs is intentional**, not a bug. Bank Tags treats each tag as an independent set, so an item being tagged `melee` *and* `cooking` is exactly what we want.
- If a tab has the wrong contents, the simplest fix is to edit the tag directly in Bank Tags — this plugin won't fight you.
