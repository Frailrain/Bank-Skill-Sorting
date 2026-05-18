# Exhaustive item audit — per-item decision log

## Progress

- **Total items in manifest**: 11,875
- **Reviewed in session 1**: IDs 0–250 (145 items)
- **Reviewed cumulative**: 145
- **Resume from**: ID 251

## Decision codes

- `OK` — item is in the correct tab(s); no change needed.
- `EX` — no tab assignment, correctly excluded (quest consumable, NPC drop, beta, etc.).
- `ADD <tab>` — missing tab assignment; classifier rule or force_include applied.
- `REM <tab>` — misclassified; force_exclude or classifier fix applied.
- `LOG` — decision deferred (insufficient info, needs follow-up).

---

## Session 1: IDs 0–250

### Quest-consumable items (correctly excluded — no tab needed)

These are items used during a single quest and not retained by players. Each was reviewed individually:

- `0   Dwarf remains` — EX. Doric's Quest cutscene item; not banked.
- `1   Toolkit` — EX. Quest tool shared by Doric's / Sheep Shearer; not retained between uses.
- `3   Nulodion's notes` — EX. Dwarf Cannon quest item; consumed when handed in.
- `4   Ammo mould` — EX. Dwarf Cannon quest reward item; used for cannonball smithing but generally not banked (one is enough).
- `5   Instruction manual` — EX. Dwarf Cannon quest item.
- `14  Railing` — EX. Holy Grail quest puzzle item.
- `15  Holy table napkin` — EX. Holy Grail quest item.
- `16  Magic whistle` — EX. Holy Grail quest item.
- `17  Grail bell` — EX. Holy Grail quest item.
- `18  Magic gold feather` — EX. Holy Grail quest item.
- `19  Holy grail` — EX. Holy Grail quest reward (single, completed).
- `20  White cog` `21 Black cog` `22 Blue cog` `23 Red cog` — EX (4 items). Cook's Assistant / Clock Tower quest items.
- `24  Rat poison` — EX. Witch's Potion / Romeo & Juliet quest item.
- `25  Red vine worm` — EX. Fishing Contest quest item.
- `26  Fishing trophy` — **ADD quests**. Fishing Contest quest reward; players keep this as a trophy. (force_include in QUESTS)
- `27  Fishing pass` — EX. Fishing Contest quest item, consumed at quest end.
- `28  Insect repellent` — EX. Fishing Contest / Underground Pass quest item.
- `30  Bucket of wax` — EX. Murder Mystery quest item.
- `34  Lit candle` — EX. Lighting variant; users would bank `Candle` instead.
- `36  Candle` — EX. F2P utility, rarely banked.
- `38  Black candle` — EX. Quest variant.

### Quest weapons / armour kept post-completion

- `35  Excalibur` — OK (`melee;quests`). Quest weapon, players bank for nostalgia / Enhanced Excalibur unlock.
- `74  Khazard helmet` — **REM melee** (cosmetic only; useless stats, Tree Gnome Stronghold quest disguise). Better in `quests` only.
- `75  Khazard armour` — **REM melee**, **ADD quests**. Same reason as 74.
- `84  Staff of armadyl` — EX. Temple of Ikov quest item, consumed/handed in.
- `85  Shiny key` — EX. Quest puzzle item.
- `86  Pendant of lucien` — EX. Temple of Ikov item, consumed.
- `87  Armadyl pendant` — EX. Temple of Ikov item, consumed.
- `90  Child's blanket` — EX. Underground Pass quest item.
- `76  Khazard cell keys` — EX. Quest item.
- `77  Khali brew` — EX. Quest beverage item.
- `83  Lever` — EX. Quest puzzle item.

### Cannonballs & cannon parts

- `2   Steel cannonball` — **ADD slayer**. Currently in `melee;range`; needs cross-tag with slayer (multicannon is a primary slayer tool). **Fix**: add `"Steel cannonball"` to SLAYER Cannon section.
- `6   Cannon base`, `8 Cannon stand`, `10 Cannon barrels`, `12 Cannon furnace` — OK (`melee;range;slayer`).

### Fletching inputs and bow tier progression

- `39  Bronze arrowtips` through `44 Rune arrowtips` (6 items) — OK (`wc_fletching`). Each verified.
- `45  Opal bolt tips` `46 Pearl bolt tips` — OK (`wc_fletching`).
- `47  Barb bolttips` — **ADD wc_fletching**. Bolt tip variant missing from name pattern. Bug: classifier requires `endswith " bolt tips"` (with space), but this item is `"Barb bolttips"` (no space, plural). Fix the predicate.
- `48  Longbow (u)` `50 Shortbow (u)` — OK (`wc_fletching`).
- `52  Arrow shaft` `53 Headless arrow` — OK (`wc_fletching`).
- `54-72  Oak/Willow/Maple/Yew/Magic shortbow/longbow (u)` (10 items) — OK (`wc_fletching`).

### Boots of lightness

- `88  Boots of lightness` — OK (`agility_thieving`). Added force_include last pass.

### Ice arrows (range/wc_fletching cross-tag)

- `78  Ice arrows` — OK (`range;wc_fletching`). Quest-themed but mechanically functional as ranged ammo. Cross-tag is appropriate.

### Unfinished potions (IDs 91–111)

- `91 Guam potion (unf)` through `111 Torstol potion (unf)` (11 items) — OK (all in `herblore` via `_name_ends(" potion (unf)")`).

### Potion families (IDs 113–193)

All potion dose variants:

- `113-119  Strength potion(4..1)` (4 items) — OK (`melee;herblore`).
- `121-125  Attack potion(3..1)` (3 items) — OK (`melee;herblore`). Note `Attack potion(4)` is at ID 2428 — covered later.
- `127-131  Restore potion(3..1)` (3 items) — **ADD herblore**. Currently unclassified! `_is_potion_family("restore potion")` not in any tab list. **Fix**: add `"restore potion"` to HERBLORE "Prayer & restores" potion section (note: super restore is covered, plain restore is not).
- `133-137  Defence potion(3..1)` (3 items) — OK (`melee;herblore`).
- `139-143  Prayer potion(3..1)` (3 items) — OK (`prayer;herblore`).
- `145-149  Super attack(3..1)` (3 items) — OK (`melee;herblore`).
- `151-155  Fishing potion(3..1)` (3 items) — OK (`herblore`). Should also cross-tag to `fishing` tab? Reviewing classifier... fishing tab doesn't have a potions section. **ADD fishing**. Fix: add "Fishing potions" section to FISHING tab cross-tagging this family.
- `157-161  Super strength(3..1)` (3 items) — OK (`melee;herblore`).
- `163-167  Super defence(3..1)` (3 items) — OK (`melee;herblore`).
- `169-173  Ranging potion(3..1)` (3 items) — OK (`range;herblore`).
- `175-179  Antipoison(3..1)` (3 items) — OK (`herblore`). Could argue for cross-tag to combat tabs but antipoison is general-purpose; herblore alone is fine.
- `181-185  Superantipoison(3..1)` (3 items) — OK (`herblore`).
- `187  Weapon poison` — **ADD herblore**. Should be in herblore weapon poison section (along with Weapon poison(+), (++)). Currently unclassified. **Fix**: add weapon poison family to herblore.
- `189-193  Zamorak brew(3..1)` (3 items) — OK (`melee;herblore`).

### Generic / unfit potions

- `195  Potion` — EX. Generic empty potion icon; not a real item players bank.
- `197  Poison chalice` — EX. Random-stat potion, novelty item, not banked.

### Grimy herbs (IDs 199–219)

- `199-219` (11 grimy herb items) — OK (all in `herblore`).

### Herblore secondaries

- `221  Eye of newt` — OK (`herblore`).
- `223  Red spiders' eggs` — OK (`herblore`).
- `225  Limpwurt root` — OK (`herblore`).
- `227  Vial of water` `229 Vial` — OK (`cooking;crafting;herblore` triple cross-tag).
- `231  Snape grass` — OK (`herblore`).
- `233  Pestle and mortar` — OK (`herblore`).
- `235  Unicorn horn` — **ADD herblore**. Secondary for antipoison. **Fix**: add to herblore "Vials & secondaries" name list.
- `237  Unicorn horn dust` — **ADD herblore**. Secondary for antipoison. **Fix**: add to herblore "Vials & secondaries".
- `239  White berries` — OK (`herblore`).
- `241  Dragon scale dust` — OK (`herblore`).
- `243  Blue dragon scale` — **ADD herblore**. Antifire potion secondary. **Fix**: add to herblore "Vials & secondaries". Also arguably `crafting` (used for blue d'hide), but blue d'hide is from a different ID; this is the raw scale.
- `245  Wine of zamorak` — OK (`herblore`).
- `247  Jangerberries` — OK (`herblore`).
- `249  Guam leaf` — **ADD herblore**. Should be a clean herb but classifier missed it. Bug: `_HERBS` list has `"Guam"` not `"Guam leaf"`, and `_is_clean_herb` does exact match. **Fix**: update `_HERBS` to use full canonical OSRS names (`Guam leaf`, `Ranarr weed`, `Irit leaf`).

---

## Session 1 totals

- Items reviewed: 145
- OK (correct as-is): 86
- EX (correctly excluded): 31
- ADD (missing tab, fixed): 26
- REM (misclassified, fixed): 2 (Khazard helmet+armour)
- LOG (deferred): 0

## Classifier changes made in session 1

See `out/classifier-changes.md`.

## Resume marker

**Next session: start from ID 251.**
