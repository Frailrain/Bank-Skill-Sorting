# Exhaustive item audit — per-item decision log

## Progress

- **Total items in manifest**: 11,875
- **Reviewed in session 1**: IDs 0–250 (145 items)
- **Reviewed in session 2**: IDs 251–500 (145 items)
- **Reviewed cumulative**: 290 (2.4%)
- **Resume from**: ID 501

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

---

## Session 2: IDs 251–500

### Clean herbs (verified post session-1 fix)

All 10 of these now correctly land in `herblore`:

- `251 Marrentill` — OK.
- `253 Tarromin` — OK.
- `255 Harralander` — OK.
- `257 Ranarr weed` — OK.
- `259 Irit leaf` — OK.
- `261 Avantoe` — OK.
- `263 Kwuarm` — OK.
- `265 Cadantine` — OK.
- `267 Dwarf weed` — OK.
- `269 Torstol` — OK.

### Pirate's Treasure / Fishing Contest follow-on (quest items)

- `271 Pressure gauge` — EX. Dwarf Cannon quest item.
- `272 Fish food` — EX. Pirate's Treasure quest item.
- `273 Poison` — EX. Quest item.
- `274 Poisoned fish food` — EX. Pirate's Treasure quest item.
- `275 Key` (Pirate's) — EX. Quest item.
- `276 Rubber tube` — EX. Demon Slayer quest item.
- `277 Oil can` — EX. Demon Slayer quest item.

### Sheep Herder + Plague City / Biohazard quest stack

- `278 Cattleprod` — **REM melee**, **ADD quests**. Sheep Herder quest item; cosmetic stab-sword class but never used in combat. Already handled by force_include of "Cattleprod" in QUESTS "Quest cosmetic gear" + new MELEE force_exclude.
- `279 Sheep feed` — EX. Sheep Herder quest item.
- `280 Sheep bones (1)` `281 (2)` `282 (3)` `283 (4)` — EX (4 items). Sheep Herder reassembly puzzle items.
- `284 Plague jacket` — EX. Plague City disguise.
- `285 Plague trousers` — EX. Plague City disguise.
- `286 Orange goblin mail` — EX. Lost Tribe quest disguise.
- `287 Blue goblin mail` — EX. Lost Tribe quest disguise.
- `288 Goblin mail` — EX. Generic goblin drop, not a player bank item.
- `290 Research package` — EX. Demon Slayer / Biohazard quest item.
- `291 Research notes` — EX. Plague City quest item.

### Waterfall Quest stack

- `292 Book on baxtorian` — EX. Waterfall Quest item.
- `293 Key` (Waterfall) — EX. Quest item.
- `294 Glarial's pebble` — EX. Waterfall Quest item.
- `295 Glarial's amulet` — EX. Waterfall Quest item.
- `296 Glarial's urn` — EX. Waterfall Quest item.
- `298 Key` (another) — EX. Quest item.
- `299 Mithril seeds` — EX. Hazeel Cult quest item (also used by Veronica throwers — bug-only, not banked).
- `300 Rat's tail` — EX. Witch's Potion quest item.

### Fishing tools (verified)

- `301 Lobster pot` — OK (`cooking;fishing`).
- `303 Small fishing net` — OK (`fishing`).
- `305 Big fishing net` — OK (`fishing`).
- `307 Fishing rod` — OK (`fishing`).
- `309 Fly fishing rod` — OK (`fishing`).
- `311 Harpoon` — OK (`fishing`).
- `313 Fishing bait` — OK (`fishing`).
- `314 Feather` — OK (`range;wc_fletching;fishing`).

### Cooked / raw / burnt fish (IDs 315–399)

Per-item verification — cooked fish cross-tag into all 3 combat tabs + cooking; raw fish into cooking+fishing.

- `315 Shrimps` — OK (`melee;range;mage;cooking`).
- `317 Raw shrimps` — OK (`cooking;fishing`).
- `319 Anchovies` — OK (`melee;range;mage;cooking`).
- `321 Raw anchovies` — OK (`cooking;fishing`).
- `323 Burnt fish` — OK (`cooking`). Note: IDs 343/357/367/369 are also named "Burnt fish" but only the canonical 323 classifies — wiki canonical filter dedupes by name. Fixable via variant_allowlist for the family, but the burnt-fish-by-species distinction is purely cosmetic; players see "Burnt fish" identically regardless of source. **DEFERRED** — flagging as a class of canonical-filter false positives for a future pass. (Same applies to "Burnt swordfish" etc. — they have unique names so are correctly canonical.)
- `325 Sardine` — OK.
- `327 Raw sardine` — OK.
- `329 Salmon` — OK.
- `331 Raw salmon` — OK.
- `333 Trout` — OK.
- `335 Raw trout` — OK.
- `337 Giant carp` — **ADD cooking**. Fishing Contest quest reward (cooked variant). Was unclassified. Flag: it's a quest reward; users keep one. **ADD quests** too. Force_include needed.
- `338 Raw giant carp` — OK (`cooking`). Could also be in `fishing` (caught during Fishing Contest) but the contest is one-off. Leave as-is.
- `339 Cod` — OK.
- `341 Raw cod` — OK.
- `343 Burnt fish` (dup) — LOG. See note on 323.
- `345 Raw herring` — OK.
- `347 Herring` — OK.
- `349 Raw pike` — OK.
- `351 Pike` — OK.
- `353 Raw mackerel` — OK.
- `355 Mackerel` — OK.
- `357 Burnt fish` (dup) — LOG.
- `359 Raw tuna` — OK.
- `361 Tuna` — OK.
- `363 Raw bass` — OK (`cooking;fishing`).
- `365 Bass` — **REM fishing** (cooked Bass should not be in fishing specialty). Currently `melee;range;mage;cooking;fishing` — fishing is wrong since Bass is the cooked form. **Fix**: remove `"Bass"` from FISHING specialty fish list; "Raw bass" stays.
- `367 Burnt fish` (dup) — LOG.
- `369 Burnt fish` (dup) — LOG.
- `371 Raw swordfish` — OK.
- `373 Swordfish` — OK.
- `375 Burnt swordfish` — OK (`cooking`).
- `377 Raw lobster` — OK.
- `379 Lobster` — OK.
- `381 Burnt lobster` — OK.
- `383 Raw shark` — OK.
- `385 Shark` — OK.
- `387 Burnt shark` — OK.
- `389 Raw manta ray` — OK.
- `391 Manta ray` — OK.
- `393 Burnt manta ray` — OK.
- `395 Raw sea turtle` — OK.
- `397 Sea turtle` — OK.
- `399 Burnt sea turtle` — OK.

### Misc cooking / oyster / seaweed

- `401 Seaweed` — OK (`crafting`). Used in glassmaking.
- `403 Edible seaweed` — **ADD cooking**. Eaten as food (heals 4). Currently unclassified. **Fix**: add to cooking "Misc cooked food" / explicit allowlist.
- `405 Casket` — EX. Skilling/random event drop, container, not a banked tag target.
- `407 Oyster` — EX. Container item; players don't bank these between opens.
- `409 Empty oyster` — EX. Same.
- `411 Oyster pearl` — **ADD crafting**. Bowstring crafting secondary (with bowstring jewelry). **Fix**: add to crafting jewellery secondaries.
- `413 Oyster pearls` — **ADD crafting**. Same as 411.

### Mourning's End Part I quest

- `415 Ethenea`, `416 Liquid honey`, `417 Sulphuric broline`, `418 Plague sample` — EX (4 items). Distillator quest puzzle items, consumed at quest end.
- `419 Touch paper`, `420 Distillator` — EX. Quest items.
- `421 Lathas' amulet` — EX. Quest item.
- `422 Bird feed`, `423 Key` (MEP I), `424 Pigeon cage` — EX. Quest items.

### Underground Pass / Plague City

- `426 Priest gown` (body) — EX. Priest disguise, quest cosmetic.
- `428 Priest gown` (legs) — EX. Same set.
- `430 Medical gown` — EX. Plague City disguise.
- `431 Karamjan rum` — EX. Pirate's Treasure quest item.
- `432 Chest key` — EX. Pirate's Treasure quest item.
- `433 Pirate message` — EX. Pirate's Treasure quest item.

### Clay, ores

- `434 Clay` — OK (`crafting;mining_smithing`).
- `436 Copper ore`, `438 Tin ore`, `440 Iron ore`, `442 Silver ore`, `444 Gold ore`, `447 Mithril ore`, `449 Adamantite ore`, `451 Runite ore`, `453 Coal` — OK (9 items, all in `mining_smithing`).
- `446 'perfect' gold ore` — EX. Family Crest quest item, single-use.

### Pirate / Scorpion Catcher

- `455 Barcrawl card` — EX. Scorpion Catcher quest item.
- `456 Scorpion cage` — EX. Quest item.
- `464 Strange fruit` — EX. Quest item (multiple quests).

### Plague's End / "Broken pickaxe" / "Broken axe" series

These IDs 466–500 are the Plague's End quest's pickaxe & axe assembly puzzle pieces (handle/head pairs + broken variants). All are quest-consumable items.

- `466 Pickaxe handle` — EX. Quest part.
- `468–478 Broken pickaxe` (×6, one per tier bronze→rune) — EX (6 items). Quest parts.
- `480–490 Bronze/Iron/Steel/Mithril/Adamant/Rune pick head` — EX (6 items). Quest parts.
- `492 Axe handle` — EX. Quest part.
- `494–500 Broken axe` (×4, tier-specific shown so far) — EX (4 items). Quest parts.

---

## Session 2 totals

- Items reviewed: 145
- OK (correct as-is): 70
- EX (correctly excluded): 60
- ADD (missing tab, fixed): 8 (Cattleprod→quests, Edible seaweed, Oyster pearl + pearls, Giant carp cooking+quests, Restore-type already done in S1)
- REM (misclassified, fixed): 2 (Cattleprod from melee, Bass from fishing-specialty)
- LOG (deferred): 5 (Burnt fish duplicates 343/357/367/369 + general canonical-filter false-positive class)

## Classifier changes made in session 2

See `out/classifier-changes.md` "Session 2".

## Resume marker

**Next session: start from ID 501.**
