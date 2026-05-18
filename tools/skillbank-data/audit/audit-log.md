# Exhaustive item audit — per-item decision log

## Progress

- **Total items in manifest**: 11,875
- **Reviewed in session 1**: IDs 0–250 (145 items)
- **Reviewed in session 2**: IDs 251–500 (145 items)
- **Reviewed in session 3**: IDs 501–750 (177 items)
- **Reviewed in session 4**: IDs 751–1000 (119 items)
- **Reviewed in session 5**: IDs 1001–1300 (134 items)
- **Reviewed in session 6**: IDs 1301–1600 (179 items)
- **Reviewed in session 7**: IDs 1601–1900 (155 items)
- **Reviewed in session 8**: IDs 1901–2200 (140 items)
- **Reviewed in session 9**: IDs 2201–2500 (159 items)
- **Reviewed in session 10**: IDs 2501–2800 (166 items)
- **Reviewed in session 11**: IDs 2801–3100 (168 items)
- **Reviewed in session 12**: IDs 3101–3400 (124 items)
- **Reviewed in session 13**: IDs 3401–3700 (177 items)
- **Reviewed in session 14**: IDs 3701–4000 (125 items)
- **Reviewed in session 15**: IDs 4001–4300 (169 items)
- **Reviewed in session 16**: IDs 4301–4600 (161 items)
- **Reviewed in session 17**: IDs 4601–4900 (125 items)
- **Reviewed in session 18**: IDs 4901–5200 (53 items)
- **Reviewed in session 19**: IDs 5201–5500 (133 items)
- **Reviewed in session 20**: IDs 5501–5800 (93 items)
- **Reviewed in session 21**: IDs 5801–6100 (116 items)
- **Reviewed in session 22**: IDs 6101–6400 (144 items)
- **Reviewed in session 23**: IDs 6401–6700 (129 items)
- **Reviewed in session 24**: IDs 6701–7000 (180 items)
- **Reviewed in session 25**: IDs 7001–7300 (131 items)
- **Reviewed cumulative**: 3547 (29.9%)
- **Resume from**: ID 7301

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

---

## Session 3: IDs 501–750

### Plague's End axe parts (continued from session 2)

- `502-506 Broken axe` (×3, tier-specific) — EX (3 items). Quest parts.
- `508-520 Bronze/Iron/Steel/Black/Mithril/Adamant/Rune axe head` — EX (7 items). Quest parts.

### Witch's Potion enchanted-food

- `522 Enchanted beef`, `523 Enchanted rat`, `524 Enchanted bear`, `525 Enchanted chicken` — EX (4 items). Witch's Potion quest items consumed at quest end.

### Bones (prayer)

- `526 Bones`, `528 Burnt bones`, `530 Bat bones`, `532 Big bones`, `534 Babydragon bones`, `536 Dragon bones` — OK (6 items). All in prayer. Burnt bones cross-tags into cooking (cooked-bones via firemaking sometimes).

### Druid's robe / Shade robe

- `538 Druid's robe` — **ADD prayer**. Currently unclassified; should be in prayer (worn for prayer flavour, also kept as cosmetic). Add to prayer Robes section.
- `540 Druid's robe top` — **ADD prayer**. Same.
- `542 Monk's robe` — OK (`prayer`).
- `544 Monk's robe top` — OK (`prayer`).
- `546 Shade robe top` — **ADD quests**. Shades of Mort'ton quest cosmetic robe. Currently unclassified.
- `548 Shade robe` — **ADD quests**. Same.

### Ghosts Ahoy / Newcomer / Pirate's Treasure follow-on

- `550 Newcomer map` — EX. Misthalin Mystery / starter map; not banked.
- `552 Ghostspeak amulet` — **ADD misc**. F2P quest reward, frequently kept and reused for ghost-related content. Add to misc Boss & quest jewellery.
- `553 Ghost's skull` — EX. Restless Ghost quest item.

### Runes (verified)

- `554 Fire rune` `555 Water rune` `556 Air rune` `557 Earth rune` `558 Mind rune` `559 Body rune` — OK (6 F2P runes in `mage;runecraft`).
- `560 Death rune` `561 Nature rune` `562 Chaos rune` `563 Law rune` `564 Cosmic rune` `565 Blood rune` `566 Soul rune` — OK (7 members runes).

### Orbs (mage materials)

- `567 Unpowered orb` `569 Fire orb` `571 Water orb` `573 Air orb` `575 Earth orb` — OK (5 items, all in `mage`). Session 1 fix added the section.

### Wizard robes

- `577 Blue wizard robe` `579 Blue wizard hat` `581 Black robe` — OK (3 items, `mage`).

### Pirate's Treasure / Quest misc

- `583 Bailing bucket` — EX. Pirate's Treasure / Tempoross item (used during minigame, but Tempoross-specific items are already in fishing).
- `587 Orb of protection` `588 Orbs of protection` — EX. Witch's House / Family Crest quest items.
- `589 Gnome amulet` — **REM melee**. Tree Gnome Stronghold quest item with neck slot but no real combat use. Classifier picked up due to slot. **ADD quests**.

### Lighting / fire

- `590 Tinderbox` — OK (`firemaking`).
- `592 Ashes` — OK (`prayer`).
- `595 Torch` — **ADD firemaking**. Unlit/lit torch are firemaking items. Add to Lanterns section.
- `596 Unlit torch` — **ADD firemaking**. Same.

### Ammunition (quest variant)

- `598 Bronze fire arrow` — OK (`range;wc_fletching`).

### Observatory Quest

- `600 Astronomy book` — EX. Quest item.

### Various early-2002 quests (Goblin Diplomacy → Shilo Village)

- `601-625` (~20 quest items: Goblin kitchen key, Lens mould, Observatory lens, Bone shard, Bone key, Stone-plaque, Tattered scroll, Crumpled scroll, Rashiliyia corpse, Zadimus corpse, Locating crystal ×4, Bone beads, Paramaya tickets, Ship ticket, Sword pommel, Bervirius notes, Wampum belt) — EX. All quest consumables.

### Coins

- `617 Coins` — OK (`misc`).

### Beads of the dead

- `616 Beads of the dead` — **REM melee, range, mage**. Currently in all 3 combat tabs because it's a neck slot item with offensive bonuses, but it's a quest cosmetic (Shilo Village reward). No real combat use. **ADD quests**.

### Mime random-event outfit (IDs 626–664)

5 colours (Pink, Green, Blue, Cream, Turquoise) × 4 pieces (boots / robe top / robe bottoms / hat) = 20 items. All currently classified in melee or mage based on slot — but they're cosmetic random event rewards.

- `626 Pink boots`, `628 Green boots`, `630 Blue boots`, `632 Cream boots`, `634 Turquoise boots` — **REM melee** (5 items). **ADD misc**. Mime random event cosmetic.
- `636-644 Pink/Green/Blue/Cream/Turquoise robe top` — **REM melee** (5 items). **ADD misc**.
- `646-654 Pink/Green/Blue/Cream/Turquoise robe bottoms` — **REM melee** (5 items). **ADD misc**.
- `656-664 Pink/Green/Blue/Cream/Turquoise hat` — **REM mage** (5 items). **ADD misc**.

(Fix: name-pattern force_exclude for the 5 colours; new misc section "Cosmetic outfits / random event".)

### Knight's Sword

- `666 Portrait` — EX. Quest item.
- `667 Blurite sword` — OK (`melee`). Quest reward weapon kept by some players.
- `668 Blurite ore` — OK (`mining_smithing`).

### Digsite + random events

- `669 Specimen jar`, `670 Specimen brush`, `672 Special cup`, `673 Teddy`, `674 Cracked sample`, `675 Rock pick`, `677-680 Panning tray/Nuggets`, `682-686 Unstamped letter/Sealed letter/Belt buckle/Old boot/Rusty sword` — EX (many items). Dig Site quest puzzle items + random-event drops.
- `671 Animal skull` — currently in wc_fletching. **REM wc_fletching**. Random event drop, not a fletching item. EX.
- `676 Trowel` — **REM farming**. Dig Site quest item; separate from farming trowel (different ID). EX.
- `681 Ancient talisman` — OK (`runecraft`). Used to enter the Abyss for RC training.
- `687 Broken arrow` — **REM wc_fletching**. Random event / Dig Site drop, not fletching material. EX.
- `688-696` (Buttons, Broken staff, Broken glass, Level 1-3 certificates, Ceramic remains, Old tooth, Invitation letter, Damaged armour, Broken armour) — EX (9 items). Dig Site / random event items.

### Murder Mystery quest

- `697 Damaged armour` `698 Broken armour` `699 Stone tablet` — EX. Quest items.
- `701-711` (Ammonium nitrate, Nitroglycerin, Ground charcoal, Mixed chemicals, Chemical compound, Arcenia root, Chest key, Vase, Book on chemicals) — EX (9 items). Murder Mystery + Witch's House quest items.

### Cup of tea + Clue scroll

- `712 Cup of tea` — OK (`cooking`).
- `713 Clue scroll` — OK (`misc`).

### Legends' Quest

- `714 Radimus notes`, `716 Bullroarer`, `717-720` (Scrawled/scribbled/scrumpled note, Sketch), `721-723` (Gold bowl, Blessed gold bowl, Golden bowl), `727-728 Hollow reed` (×2), `730 Binding book`, `731 Enchanted vial`, `733 Smashed glass`, `735 Yommi tree seeds`, `737-739` (Snakeweed mixture, Ardrigal mixture, Bravery potion), `741-744` (Chunk/Hunk/Lump/Heart crystal), `748-750` (Holy force, Yommi totem, Gilded totem) — EX (~25 items). All Legends' Quest consumables.
- `729 Shaman's tome` — **REM mage**. Quest book, not a magic tome. Classifier matched via `_name_ends(" tome")`. **Fix**: force_exclude `Shaman's tome` on MAGE Tomes section.
- `732 Holy water` — OK (`range`). Thrown holy water vials (anti-demon weapon).
- `740 Blue hat` — EX. Legends' Quest disguise.
- `746 Dark dagger` — **REM melee**. Heroes' Quest weapon, quest cosmetic with stab_sword stats but no combat use. **ADD quests**.
- `747 Glowing dagger` — **REM melee**. Same as 746 (it's the activated form). **ADD quests**.

---

## Session 3 totals

- Items reviewed: 177
- OK (correct as-is): 32
- EX (correctly excluded): 110
- ADD (missing tab, fixed): 31 (Druid's robes ×2, Shade robes ×2, Ghostspeak amulet, Torch ×2, Gnome amulet, Beads of the dead, Mime outfit ×20, Dark dagger + Glowing dagger)
- REM (misclassified, fixed): 24 (Mime outfit ×20, Beads of the dead from 3 tabs, Gnome amulet, Trowel, Broken arrow, Animal skull, Shaman's tome, Dark/Glowing dagger ×2)
- LOG (deferred): 0

## Classifier changes made in session 3

See `audit/classifier-changes.md` "Session 3".

---

## Session 4: IDs 751–1000

### Gnome Restaurant / Heroes' Quest

- `751 Gnomeball` — EX. Gnome Ball minigame item.
- `753-769` (Cadava berries, Message, Cadava potion, Book, Phoenix hq key, Weapon store key, Intel report, Broken shield, Certificate) — EX (~9). Heroes' Quest items.
- `767 Phoenix crossbow` — OK (`range`). Heroes' Quest crossbow; players keep it.

### Lost City + Family Crest

- `771 Dramen branch` — EX. Lost City quest item.
- `772 Dramen staff` — OK (`melee;quests`). Lost City reward; required for fairy ring travel and used by some pures.
- `773 'perfect' ring` — EX. Family Crest quest item, single-use.
- `774 'perfect' necklace` — EX. Family Crest quest item.
- `775 Cooking gauntlets` — OK (`cooking`).
- `776 Goldsmith gauntlets` — **ADD mining_smithing**. Doubles XP for goldsmithing; banked by players training. **Fix**: add to mining_smithing tools/bags section.
- `777 Chaos gauntlets` — **ADD mage**. Boosts chaos spell damage. Currently unclassified. **Fix**: add to mage Gloves section as force_include.
- `778 Steel gauntlets` — **ADD melee**. Family Crest quest reward; provides melee bonus. **Fix**: add to melee Gloves force_include.

### Tree Gnome Village / Watchtower / Throne of Miscellania bits

- `780-799` (Crest part, Family crest, Bark sample, Translation book, Glough's journal, Hazelmere's scroll, Lumber order, Glough's key, Twigs, Daconia rock, Invasion plans, War ship, null sup, Herb bowl, Grinder, Template for cert) — EX (~15 items, all quest items).

### Thrown weapons (range)

- `800-805 Bronze/Iron/Steel/Mithril/Adamant/Rune thrownaxe` — OK (`range`). 6 items.
- `806-811 Bronze/Iron/Steel/Mithril/Adamant/Rune dart` — OK (`range;wc_fletching`). 6 items.
- `818 Poisoned dart(p)` — EX. Noise-filter-excluded as poison variant (correct per design).

### Dart tips, javelins

- `819-824 Bronze/Iron/Steel/Mithril/Adamant/Rune dart tip` — OK (`wc_fletching`). 6 items.
- `825-830 Bronze/Iron/Steel/Mithril/Adamant/Rune javelin` — **ADD wc_fletching**. 6 items currently in range only; javelins ARE fletched (from javelin shaft + javelin head). Add to wc_fletching as cross-tag.

### Bows + crossbow

- `837 Crossbow` — OK (`range`).
- `839-861 Longbow / Shortbow + Oak/Willow/Maple/Yew/Magic variants` — OK (12 items, all `range;wc_fletching`).

### Knives (thrown)

- `863-869 Iron/Bronze/Steel/Mithril/Adamant/Rune/Black knife` — OK (`range`). 7 items, thrown weapons, smithed not fletched.

### Bolts + arrows

- `877-881 Bronze/Opal/Pearl/Barbed bolts` — OK (`range;wc_fletching`). 4 items.
- `882-892 Bronze/Iron/Steel/Mithril/Adamant/Rune arrow` — OK (`range;wc_fletching`). 6 items.

### Skilling/utility (gap from 892 to 943)

The manifest jumps — many IDs are not canonical wiki entries (skipped).

### Skilling / drops / utility

- `943 Worm` — **ADD hunter**. Bird snare bait. Currently unclassified.
- `945 Throwing rope` — EX. Heroes' Quest item.
- `946 Knife` — OK (`wc_fletching`).
- `948 Bear fur` — **ADD crafting**. Drop used in cape crafting / some quests. Currently unclassified.
- `950 Silk` — **ADD crafting**. Thieving drop, used in spinning wheel + leather crafting. Currently unclassified.
- `952 Spade` — OK (`farming`).
- `954 Rope` — **ADD misc**. Universal quest/clue tool, banked long-term. Currently unclassified.
- `956 Flyer` — EX. Heroes' Quest item.
- `958 Grey wolf fur` — **ADD crafting**. Drop used in some quests / crafting recipes. Currently unclassified.
- `960 Plank` — OK (`construction`).
- `962 Christmas cracker` — **ADD misc**. Holiday rare. Currently unclassified.
- `964 Skull` `965 Skull` — EX (2 items). Quest / random event items.
- `966 Tile` — EX. Construction interior tile.
- `968 Rock` — EX. Random event drop.
- `970 Papyrus` `972 Papyrus` — EX. Crafting paper (Mahjarrat memories); but the canonical version is in Black Knights' Fortress quest; LOG for now.
- `973 Charcoal` — **ADD crafting**. Used for some smithing / herblore recipes. Currently unclassified.
- `975 Machete` — **REM melee**. Karamja jungle navigation tool, not a combat weapon. **Fix**: force_exclude Machete + opal/jade/red-topaz variants on melee Weapons.
- `977 Cooking pot` — EX. Random event tool / camping prop.
- `979 Highwayman mask` — **ADD misc**. Holiday event cosmetic. Currently unclassified.
- `981 Disk of returning` — EX. Random event teleport.
- `983 Brass key` — EX. Murder Mystery key.
- `985 Tooth half of key` `987 Loop half of key` — OK (`misc`).
- `989 Crystal key` `991 Muddy key` `993 Sinister key` — OK (`misc`).
- `995 Coins` — LOG. Duplicate of ID 617 ("Coins"). Wiki canonical filter dedupes; 617 is in misc, 995 isn't because not canonical. This is the same canonical-filter false-positive class flagged in session 2 (burnt fish).

---

## Session 4 totals

- Items reviewed: 119
- OK (correct as-is): 55
- EX (correctly excluded): 38
- ADD (missing tab, fixed): 16 (Goldsmith gauntlets, Chaos gauntlets, Steel gauntlets, Worm, Bear fur, Silk, Rope, Grey wolf fur, Christmas cracker, Charcoal, Highwayman mask, Javelins ×6)
- REM (misclassified, fixed): 1 (Machete from melee)
- LOG (deferred): 3 (Papyrus ×2, Coins duplicate)

## Classifier changes made in session 4

See `audit/classifier-changes.md` "Session 4".

---

## Session 5: IDs 1001–1300

### Cosmetic / starter clothing

- `1005 White apron` — EX. Cook's Assistant starter; cosmetic only.
- `1009 Brass necklace` — **ADD crafting** (F2P jewellery crafting output) and **ADD misc** (cheap teleport-jewellery progenitor). Currently unclassified.
- `1011 Blue skirt`, `1013 Pink skirt`, `1015 Black skirt` — EX (3 items). F2P cosmetic legs.
- `1025 Right eye patch` — EX. Pirate's Treasure cosmetic.
- `1037 Bunny ears` — **ADD misc** (Holiday rares section).

### Basic colour capes (cosmetic)

- `1007 Red cape`, `1019 Black cape`, `1021 Blue cape`, `1023 Yellow cape`, `1027 Green cape`, `1029 Purple cape`, `1031 Orange cape` — **REM range** (7 items). Cosmetic capes with defence_ranged=2, defence_stab=1 by stat-sniff which is why they land in range. Tiny defensive bonuses; no real combat use. **Fix**: force_exclude the basic-color cape names from RANGE Capes.

### Wizard / monk

- `1017 Wizard hat` — OK (`mage`).
- `1033 Zamorak monk bottom`, `1035 Zamorak monk top` — OK (`mage`).

### Holiday rares

- `1038 Red partyhat`, `1040 Yellow partyhat`, `1042 Blue partyhat`, `1044 Green partyhat`, `1046 Purple partyhat`, `1048 White partyhat`, `1050 Santa hat` — **ADD misc**. 7 partyhat/santa items currently unclassified; should join Holiday rares section.
- `1053 Green halloween mask`, `1055 Blue halloween mask`, `1057 Red halloween mask` — **ADD misc**. 3 halloween masks. Currently unclassified. Add to Holiday rares (need name variants beyond plain "Halloween mask").

### Cape of legends

- `1052 Cape of legends` — **REM melee/range/mage** (all 3), **ADD quests**. Legends' Quest cape with all-style defence bonuses but tagged as quest cosmetic — players bank it for completionism, not combat.

### Leather / d'hide

- `1059 Leather gloves`, `1061 Leather boots`, `1063 Leather vambraces` — OK (`melee`). Basic leather armour.
- `1065 Green d'hide vambraces` — **REM melee, mage**. Currently in `melee;range;mage;crafting`. Should only be range + crafting (range armour with ranged_strength). **Fix**: force_exclude all d'hide names from MELEE Gloves and MAGE Gloves.
- `1095 Leather chaps`, `1097 Studded chaps` — OK (`melee`).
- `1099 Green d'hide chaps` — **REM melee**. Currently `melee;range;crafting`. Should be range+crafting only.
- `1131 Hardleather body` — OK (`melee;crafting`). Hardleather is melee gear.
- `1133 Studded body` — **REM melee**. Studded is range gear. Currently `melee;range`. Remove melee.
- `1135 Green d'hide body` — OK (`range;crafting`). Already excluded from melee.

### Bronze→Rune armour pieces (mining_smithing + melee)

All correctly classified `melee;mining_smithing`:

- `1067-1093 Iron/Steel/Mithril/Adamant/Bronze/Black/Rune platelegs and plateskirts` — OK (~14 items).
- `1101-1127 Iron/Bronze/Steel/Mithril/Adamant/Black/Rune chainbody and platebody` — OK (~14 items).
- `1129 Leather body` — OK (`melee`).
- `1137-1165 Iron/Bronze/Steel/Mithril/Adamant/Rune/Dragon/Black med helm and full helm` — OK (~14 items).
- `1167 Leather cowl`, `1169 Coif` — OK (`melee`). Note: Coif is canonically a range item too — `1169 Coif` currently only in `melee`. **ADD range** (cross-tag). But the classifier puts it in melee head because it has defence_slash etc. The range armour predicate would only catch it if ranged_strength > 0 or ranged_defence dominant. Coif likely has only minimal magic_defence. **LOG** for now — needs a range head force_include.
- `1171 Wooden shield` — OK (`melee`).
- `1173-1201 Bronze/Iron/Steel/Black/Mithril/Adamant/Rune/Dragon sq shield + Bronze/Iron/Steel/Black/Mithril/Adamant/Rune kiteshield` — OK (~15 items).

### Daggers (melee)

- `1203-1217 Iron/Bronze/Steel/Mithril/Adamant/Rune/Dragon/Black dagger` — OK (8 items).
- `1235 Poisoned dagger(p)` — EX. Noise-filter (poison variant). Correct.

### Spears (2h melee)

- `1237-1249 Bronze/Iron/Steel/Mithril/Adamant/Rune/Dragon spear` — OK (7 items).

### Pickaxes

- `1265-1275 Bronze/Iron/Steel/Adamant/Mithril/Rune pickaxe` — OK (`mining_smithing`). 6 items.

### Swords / Longswords

- `1277-1289 Bronze/Iron/Steel/Black/Mithril/Adamant/Rune sword` — OK (7 items, all `melee`).
- `1291-1299 Bronze/Iron/Steel/Black/Mithril longsword` — OK (5 items shown so far in batch; remainder in next batch).

---

## Session 5 totals

- Items reviewed: 134
- OK (correct as-is): 95
- EX (correctly excluded): 5
- ADD (missing tab, fixed): 19 (Bunny ears, 7 partyhats, 3 halloween masks, Brass necklace ×2 tabs, Cape of legends to quests, 4 d'hide REMs)
- REM (misclassified, fixed): 14 (7 colour capes from range, Cape of legends from 3 tabs, Green d'hide vambraces from melee+mage, Green d'hide chaps from melee, Studded body from melee)
- LOG (deferred): 1 (Coif range cross-tag)

## Classifier changes made in session 5

See `audit/classifier-changes.md` "Session 5".

---

## Session 6: IDs 1301–1600

### Combat weapons (continued melee tier coverage)

- `1301-1305 Adamant/Rune/Dragon longsword` — OK (`melee`).
- `1307-1319 Bronze/Iron/Steel/Black/Mithril/Adamant/Rune 2h sword` — OK (7 items).
- `1321-1333 Bronze/Iron/Steel/Black/Mithril/Adamant/Rune scimitar` — OK (7 items).
- `1335-1347 Iron/Bronze/Steel/Black/Mithril/Adamant/Rune warhammer` — OK (7 items).

### Woodcutting axes

- `1349-1359 Iron/Bronze/Steel/Mithril/Adamant/Rune axe` — OK (`wc_fletching`). 6 items, weapon_type='axe' correctly handled by my session 1 fix (excluded from melee since no "battleaxe" in name).
- `1361 Black axe` — OK (`wc_fletching`). Same.

### Battleaxes — bad wc cross-tag

- `1363-1377 Iron/Steel/Black/Mithril/Adamant/Rune/Bronze/Dragon battleaxe` — **REM wc_fletching** (8 items). Battleaxes are NOT wc tools; they share weapon_type='axe' with wc axes but are combat-only. My WC_FLETCHING Axes section matches anything with weapon_type='axe'. **Fix**: exclude battleaxe names from WC_FLETCHING Axes section.

### Staves (mage)

- `1379-1409 Staff / Staff of air/water/earth/fire / Magic staff / Battlestaff / element battlestaves / Mystic element staves / Iban's staff` — OK (16 items). All weapon_type='staff', correctly in mage. Element battlestaves cross-tag into crafting (orbs section result).

### Misc weapon variants

- `1411 Farmer's fork` — OK (`farming`). Farming tool.
- `1413 Halberd`, `1415 Warhammer`, `1417 Javelin` — EX (3 items). Disambiguation/icon-only items, no real stats.
- `1419 Scythe` — **LOG**. Easter 2014 holiday weapon (the original Scythe, not Scythe of Vitur). Currently in melee due to weapon_type='scythe'. Probably should be in misc Holiday rares, but breaking the scythe weapon_type sort key is a risk. Defer.

### Maces

- `1420-1434 Iron/Bronze/Steel/Black/Mithril/Adamant/Rune/Dragon mace` — OK (`melee`). 8 items.

### Rune essence + talismans

- `1436 Rune essence` — OK (`mage;runecraft`).
- `1438-1462` (14 talismans: Air/Earth/Fire/Water/Body/Mind/Blood/Chaos/Cosmic/Death/Law/Soul/Nature) — OK. All in runecraft.

### Misc items

- `1464 Archery ticket` — **ADD misc**. Ranging Guild reward currency. Currently unclassified. Add to Currency section.
- `1465 Weapon poison` — LOG (duplicate of 187 — canonical-filter dedupe; 187 is in herblore).

### Underground Pass + Witch's House + Dragon Slayer quest items

- `1466-1510` (~40 quest items): Sea slug, Damp sticks, Dry sticks, Broken glass, beads (Red/Yellow/Black/White), Rock, Orb of light ×3, Oily cloth, Piece of railing, Unicorn horn (Witch's Potion variant), Paladin's badge ×3, Witch's cat, Doll of iban, Old journal, History of iban, Iban's dove, Amulet of othanian/doomion/holthion, Iban's shadow, Dwarf brew, Iban's ashes (in prayer ✓), Warrant, Hangover cure, Ardougne teleport scroll, Gas mask, A small key, A scruffy note, Book, Picture, etc.) — EX (all quest consumables).
- `1478 Amulet of accuracy` — OK (`melee;mage;misc`). Cheap accuracy amulet, kept in multiple tabs.
- `1495 Klank's gauntlets` — **REM melee**. Underground Pass quest gloves. **Fix**: force_exclude on melee Gloves.
- `1502 Iban's ashes` — OK (`prayer`).

### Logs (wc + firemaking dual-tag)

- `1511-1521 Logs/Magic/Yew/Maple/Willow/Oak logs` — OK (`wc_fletching;firemaking`). 6 items.

### Thieving + skilling

- `1523 Lockpick` — OK (`agility_thieving`).

### Karamja Volcano quest herbs

- `1525-1534` (Snake weed, Grimy snake weed, Ardrigal, Grimy ardrigal, Sito foil, Volencia moss, Rogue's purse + grimy variants) — EX (10 items). Tai Bwo Wannai Trio quest items, consumed for cures.

### Dragon Slayer

- `1535-1538 Map part ×3 + Crandor map` — EX (4 items).
- `1539 Steel nails` — OK (`mining_smithing;construction`).
- `1540 Anti-dragon shield` — OK (`melee`).
- `1542-1548 Maze key + Keys ×6` — EX (7 items).

### Misc

- `1549 Stake` — EX. Witch's House quest item.
- `1550 Garlic` — OK (`cooking`).
- `1552 Seasoned sardine` — EX. Catlike quest item.
- `1554 Fluffs' kitten` — EX. Gertrude's Cat quest item.
- `1555 Pet kitten` — OK (`cooking`) - it's in my cooking pet section though arguably it should be misc. Cooking is fine.
- `1561 Pet cat`, `1567 Pet cat` — EX (2). Variant pets, canonical may differ.
- `1573 Doogle leaves` — EX. Pet cat quest item.
- `1575 Cat training medal` — EX. Quest reward, cosmetic.
- `1577 Pete's candlestick` — EX. Big Chompy quest item.
- `1579 Thieves' armband` — EX. Pirate's Treasure / Thieves' Guild item.
- `1580 Ice gloves` — OK (`melee`). Heroes' Quest reward, functional gloves.
- `1581-1584 Blamish snail slime / Blamish oil / Fire feather / Id papers` — EX. Tai Bwo Wannai / Heroes' / Murder Mystery items.
- `1585 Oily fishing rod` — OK (`fishing`).
- `1586-1591` (Miscellaneous key, Grip's keyring, Dusty key, Jail key) — EX (4 items). Quest keys.

### Jewellery moulds

- `1592 Ring mould`, `1594 Unholy mould`, `1595 Amulet mould`, `1597 Necklace mould` — **ADD crafting**. 4 items currently unclassified; jewellery crafting tools. **Fix**: add to crafting Tools section.
- `1599 Holy mould` — OK (`prayer`).

---

## Session 6 totals

- Items reviewed: 179
- OK (correct as-is): 79
- EX (correctly excluded): 80
- ADD (missing tab, fixed): 5 (Archery ticket, Ring/Unholy/Amulet/Necklace mould)
- REM (misclassified, fixed): 9 (8 battleaxes from wc_fletching, Klank's gauntlets from melee)
- LOG (deferred): 2 (Scythe holiday weapon, Weapon poison duplicate of 187)

## Classifier changes made in session 6

See `audit/classifier-changes.md` "Session 6".

---

## Session 7: IDs 1601–1900

### Gems (crafting + mining_smithing cross-tag)

- `1601-1615` cut gems (Diamond/Ruby/Emerald/Sapphire/Opal/Jade/Red topaz/Dragonstone) — OK (8 items, all `crafting`).
- `1617-1631` uncut gem versions — OK (8 items, all `crafting;mining_smithing`).
- `1633 Crushed gem` — OK (`herblore`). Antipoison++ secondary.

### Gold + gem jewellery

- `1635 Gold ring` — OK (`crafting`). Gold-startswith matched.
- `1637-1647 Sapphire/Emerald/Ruby/Diamond/Dragonstone/Black ring` — **ADD crafting** (6 items). My jewellery sections only matched "Gold/Silver " prefix. Need a gem-jewellery section.
- `1654 Gold necklace` — OK (`crafting`).
- `1656-1666 Sapphire/Emerald/Ruby/Diamond/Dragon/Black necklace` — **ADD crafting** (6 items). Same gap.
- `1673-1683 Gold/Sapphire/Emerald/Ruby/Diamond/Dragonstone amulet (u)` — OK (mostly in `wc_fletching` for amulet-stringing cross-tag).
- `1692-1702 Gold/Sapphire/Emerald/Ruby/Diamond/Dragonstone amulet` — partial OK (Gold) + **ADD crafting** for the 5 gem amulets.

### Glory + holy symbols

- `1704-1712 Amulet of glory (uncharged + 1..4)` — OK (5 items, `melee;range;mage;misc`).
- `1714-1724 Symbol family` (Unstrung/Unblessed/Holy/Unstrung emblem/Unpowered/Unholy) — OK (6 items). Already in prayer + combat tabs for the holy/unholy variants.

### Amulet of strength/magic/defence/power

- `1725 Amulet of strength` — OK (`melee;misc`).
- `1727 Amulet of magic` — OK (`mage;misc`).
- `1729 Amulet of defence` — OK (`melee;range;mage;misc`).
- `1731 Amulet of power` — OK (`melee;range;mage`).

### Crafting basics

- `1733 Needle`, `1734 Thread`, `1737 Wool`, `1739 Cowhide`, `1741 Leather`, `1743 Hard leather` — OK (6 items, `crafting`).
- `1735 Shears` — **ADD farming** (used to shear sheep for wool, also a farming flavour tool) — actually wool/sheep shearing is for crafting prep. Just **ADD crafting**.
- `1745-1753 Green/Black/Red/Blue/Green dragon leather variants` — OK (5 items, `range;crafting`).
- `1755 Chisel` — OK (`crafting;mining_smithing`).
- `1757 Brown apron` — EX. Cook's Assistant cosmetic variant.
- `1759 Ball of wool` — OK.
- `1761 Soft clay` — OK (`crafting;mining_smithing;construction`).
- `1763-1773 Red/Yellow/Blue/Orange/Green/Purple dye` — OK (6 items, `crafting`).
- `1775 Molten glass` — OK.
- `1777 Bow string` — OK (`wc_fletching`).
- `1779 Flax` — OK (`wc_fletching`).
- `1781 Soda ash` — OK (`crafting`).
- `1783 Bucket of sand` — **ADD crafting**. Currently `cooking` only; should also cross-tag crafting (glass-making input).
- `1785 Glassblowing pipe` — OK (`crafting`).
- `1787 Unfired pot`, `1789 Unfired pie dish`, `1791 Unfired bowl` — OK (3 items, `crafting`).
- `1793 Woad leaf` — **ADD crafting** (used to make blue dye). Currently unclassified.
- `1794 Bronze wire` — **ADD crafting**. Used in some jewellery / mahogany homes recipes.

### Murder Mystery items

- `1796 Silver necklace` — OK (`crafting`). Silver-starts.
- `1798-1822` (~20 items: Silver cup/bottle/book/needle/pot, Criminal's thread/dagger, Flypaper, Pungent pot, Killer's/Anna's/Bob's/Carol's/David's/Elizabeth's/Frank's/Unknown print) — EX.

### Tourist Trap

- `1823-1829 Waterskin(4..1)` — **ADD cooking**. Desert water containers; players bank for desert trips. Currently unclassified.
- `1833-1837 Desert shirt/robe/boots` — EX (cosmetic heat protection, players reuse but tiny audience).
- `1839-1843 Metal key, Cell door key, Barrel, Ana in a barrel, Wrought iron key` — EX (5 items).
- `1844-1846 Slave shirt/robe/boots` — EX. Quest disguise.
- `1847 Scrumpled paper`, `1848 Shantay disclaimer`, `1849 Prototype dart`, `1850 Technical plans`, `1851 Tenti pineapple`, `1852 Bedabin key`, `1853 Prototype dart tip`, `1854 Shantay pass`, `1855 Rock`, `1856 Guide book`, `1857 Totem`, `1858 Address label` — EX (~12 items).
- `1849 Prototype dart` — **REM wc_fletching**. Quest item, not a real dart.
- `1853 Prototype dart tip` — **REM wc_fletching**. Same.

### Gertrude's Cat / Tourist Trap food

- `1859 Raw ugthanki meat` — OK (`cooking`). Raw startswith.
- `1861 Ugthanki meat` — **ADD cooking**. Cooked variant; not picked up by "Cooked " startswith.
- `1863 Pitta dough` — **ADD cooking**. Bread variant.
- `1865 Pitta bread` — OK (`cooking`). Bread allowlist matched.
- `1867 Burnt pitta bread` — OK (`cooking`). Burnt startswith.
- `1869-1885` (Chopped tomato/onion/ugthanki, mixed kebab ingredients, Ugthanki kebab ×2) — **ADD cooking**. ~10 items currently unclassified; all are kebab-recipe items.
- `1887 Cake tin`, `1891 Cake`, `1897 Chocolate cake` — OK (`cooking`).
- `1889 Uncooked cake` — **ADD cooking**.

---

## Session 7 totals

- Items reviewed: 155
- OK (correct as-is): 73
- EX (correctly excluded): 47
- ADD (missing tab, fixed): 33 (12 gem jewellery, Bucket of sand, Woad leaf, Bronze wire, Shears, Waterskins ×4, kebab items ×10, Uncooked cake, Ugthanki meat, Pitta dough)
- REM (misclassified, fixed): 2 (Prototype dart, Prototype dart tip from wc_fletching)
- LOG (deferred): 0

## Classifier changes made in session 7

See `audit/classifier-changes.md` "Session 7".

---

## Session 8: IDs 1901–2200

This batch is overwhelmingly cooking food / ingredient items. Many were unclassified because finite name lists didn't cover them.

### Burnt cake + basic cooking items

- `1903 Burnt cake` — OK (`cooking`).
- `1905 Asgarnian ale`, `1907 Wizard's mind bomb`, `1913 Dwarven stout`, `1915 Grog`, `1917 Beer`, `1919 Beer glass` — OK (6 items, `cooking`).
- `1909 Greenman's ale` — EX. RFD quest variant.
- `1911 Dragon bitter` — EX. Dragon Slayer quest beverage.

### Container utilities

- `1921 Bowl of water`, `1923 Bowl`, `1925 Bucket`, `1927 Bucket of milk`, `1929 Bucket of water`, `1931 Pot`, `1933 Pot of flour`, `1935 Jug`, `1937 Jug of water` — OK (9 items).
- `1939 Swamp tar` — **ADD hunter** (used in hunter tars: Guam/Marrentill/Tarromin/Harralander tar). Currently unclassified. Add to hunter Salamanders section.
- `1940 Raw swamp paste`, `1941 Swamp paste` — **ADD crafting** (used in crafting recipes). 2 items unclassified.

### Basic ingredients

- `1942 Potato`, `1944 Egg`, `1946 Flour`, `1949 Chef's hat`, `1953 Pastry dough`, `1955 Cooking apple`, `1957 Onion`, `1963 Banana`, `1965 Cabbage`, `1973 Chocolate bar`, `1975 Chocolate dust`, `1982 Tomato`, `1993 Jug of wine`, `2003 Stew`, `2005 Burnt stew`, `2011 Curry`, `2013 Burnt curry`, `2007 Spice` — OK (~18 items, all in `cooking`).
- `1947 Grain` — **ADD cooking** (milled into flour). Currently unclassified.
- `1951 Redberries` — **ADD cooking** (used in pies). Currently unclassified.
- `1959 Pumpkin` — OK (`misc`). Holiday rare.
- `1961 Easter egg` — OK (`misc`).
- `1967 Cabbage` (dup) — LOG (canonical at 1965).
- `1969 Spinach roll` — **ADD cooking**.
- `1971 Kebab` — **ADD cooking**.
- `1977 Chocolatey milk` — **ADD cooking**.
- `1978 Cup of tea` (dup of 712) — LOG.
- `1980 Empty cup` — **ADD cooking** (container).
- `1984 Rotten apple` — EX.
- `1985 Cheese` — **ADD cooking**.
- `1987 Grapes` — **ADD cooking**.
- `1989 Half full wine jug` — **ADD cooking**.
- `1991-1992 Jug of bad wine` — **ADD cooking** (the failed cooking-XP product).
- `1995-1996 Unfermented wine` — **ADD cooking**.
- `1997 Incomplete stew` — **ADD cooking**.
- `2001 Uncooked stew` — **ADD cooking**.
- `2009 Uncooked curry` — **ADD cooking**.

### Gnome cocktails & food

A huge family. ~40 items in this batch.

- `2015-2021 Vodka, Whisky, Gin, Brandy` — **ADD cooking** (gnome bartending base spirits). Currently unclassified.
- `2023 Cocktail guide`, `2025 Cocktail shaker`, `2026 Cocktail glass` — **ADD cooking**. Bartending utensils.
- `2028-2040 Premade <cocktail>` (×7) — partial OK (some matched gnome food, others not). All should be cooking. **Fix**: tighten gnome cocktail pattern.
- `2042-2086 Unfinished cocktail` variants (×7 distinct IDs) — **ADD cooking** (intermediate states; players don't bank but the IDs exist).
- `2048 Pineapple punch`, `2054 Wizard blizzard`, `2064 Blurberry special`, `2074 Choc saturday`, `2080 Short green guy`, `2084 Fruit blast`, `2092 Drunk dragon` — OK (7 items, in cooking gnome food list).
- `2094 Odd cocktail` — EX. Random gnome quest outcome.

### Fruit chunks/slices/rings

- `2102 Lemon`, `2108 Orange`, `2114 Pineapple` — OK (`cooking`).
- `2104-2124 Lemon/Orange/Pineapple/Lime chunks/slices/rings` (~10 items) — **ADD cooking**. Gnome bartending ingredients.
- `2120 Lime` — **ADD cooking**.

### Meat (raw/cooked/burnt)

- `2132-2146 Raw beef/rat/bear/chicken, Cooked chicken/meat, Burnt chicken/meat` — OK (8 items, `cooking`).
- `2134 Raw rat meat` — OK (`cooking;hunter`).
- `2126 Dwellberries` — **ADD cooking** (used in pies/Underground Pass).
- `2128 Equa leaves` — **ADD cooking** (gnome ingredient).
- `2130 Pot of cream` — OK (`cooking`).

### Karamja Lava eel + gnome food output

- `2148 Raw lava eel` — OK (`cooking;fishing`).
- `2149 Lava eel` — OK (`melee;range;mage;cooking;fishing`). Combat food cross-tag.

### Gnome cooking ingredients (toads, worms)

- `2150 Swamp toad` — **ADD cooking** (gnome ingredient).
- `2152 Toad's legs`, `2154 Equa toad's legs`, `2156 Spicy toad's legs`, `2158 Seasoned legs` — **ADD cooking** (4 items).
- `2160 Spicy worm`, `2162 King worm` — **ADD cooking** (2 items).

### Gnome cooking tools/intermediates

- `2164 Batta tin`, `2165 Crunchy tray`, `2166 Gnomebowl mould`, `2167 Gianne's cook book`, `2169 Gnome spice` — **ADD cooking** (5 items).
- `2171 Gianne dough`, `2173 Odd gnomebowl`, `2175 Burnt gnomebowl`, `2177 Half baked bowl`, `2178 Raw gnomebowl`, `2179 Unfinished bowl`, `2185 Chocolate bomb`, `2187 Tangled toad's legs`, `2189 Unfinished bowl`, `2191 Worm hole`, `2193 Unfinished bowl`, `2195 Veg ball`, `2197 Odd crunchies`, `2199 Burnt crunchies` — **ADD cooking** for the unclassified ones.

---

## Session 8 totals

- Items reviewed: 140
- OK (correct as-is): 56
- EX (correctly excluded): 6
- ADD (missing tab, fixed): 76 (massive expansion of cooking — gnome bartending/cocktails, fruit slices/chunks, intermediate states, toad/worm gnome ingredients, etc.)
- REM (misclassified, fixed): 0
- LOG (deferred): 2 (Cabbage dup, Cup of tea dup — canonical-filter dupes)

## Classifier changes made in session 8

See `audit/classifier-changes.md` "Session 8".

---

## Session 9: IDs 2201–2500

### Gnome crunchies + battas (intermediates)

- `2201 Half baked crunchy`, `2207 Unfinished crunchy` ×3, `2245 Odd batta`, `2249 Half baked batta`, `2251/2257/2261/2279 Unfinished batta` ×4 — **ADD cooking**. Gnome cooking intermediate states. **Fix**: add name patterns to gnome food OR add to existing list.
- `2202 Raw crunchies`, `2205 Worm crunchies`, `2209 Chocchip crunchies`, `2213 Spicy crunchies`, `2217 Toad crunchies` — OK (5 items, `cooking`).
- `2219-2243 Premade <batta/crunchies>` (~13 items) — OK (all `cooking` via Premade pattern).
- `2247 Burnt batta`, `2250 Raw batta`, `2253 Worm batta`, `2255 Toad batta`, `2259 Cheese+tom batta`, `2277 Fruit batta`, `2281 Vegetable batta` — OK (7 items).

### Rock-climbing boots

- `2203 Rock-climbing boots` — **ADD agility_thieving**. Death Plateau quest reward; banked for agility shortcuts. Currently unclassified.

### Pizza chain

- `2283 Pizza base` — OK.
- `2285 Incomplete pizza`, `2287 Uncooked pizza` — **ADD cooking** (2 items).
- `2289 Plain pizza`, `2293 Meat pizza`, `2297 Anchovy pizza`, `2301 Pineapple pizza`, `2305 Burnt pizza` — OK (5 items, `cooking`).

### Bread chain

- `2307 Bread dough`, `2309 Bread`, `2311 Burnt bread` — OK (3 items).

### Pie chain

- `2313 Pie dish` — OK (`crafting`) but also belongs in cooking. **ADD cooking**.
- `2315 Pie shell` — OK (`cooking;crafting`).
- `2317-2321 Uncooked apple/meat/berry pie` — OK (3 items, `cooking`).
- `2323-2327 Apple pie, Redberry pie, Meat pie` — OK (3 items, `cooking`).
- `2329 Burnt pie` — OK.

### Karamja / Oomlie

- `2337 Raw oomlie` — OK.
- `2339-2340 Palm leaf` — EX (Tai Bwo Wannai quest item, 2 dups).
- `2341 Wrapped oomlie` — **ADD cooking**.
- `2343 Cooked oomlie wrap`, `2345 Burnt oomlie wrap`, `2426 Burnt oomlie` — OK (3 items, `cooking`).

### Hammer + bars

- `2347 Hammer` — OK (`crafting;mining_smithing;construction`).
- `2349-2363 Bronze/Iron/Steel/Silver/Gold/Mithril/Adamantite/Runite bar` — OK (8 items, `mining_smithing`). Steel bar also has `construction` cross-tag.

### Watchtower / Family Crest follow-on

- `2365 'perfect' gold bar` — EX. Family Crest variant.
- `2366 Shield left half`, `2368 Shield right half`, `2370 Steel studs` — EX. Watchtower quest items.
- `2372-2398` (Ogre relic, Relic part 1/2/3, Skavid map, Ogre tooth, Toban's key, Rock cake, Crystal, Fingernails, Old robe, Unusual armour, Damaged dagger, Tattered eye patch, Vial ×2, Ground bat bones (in prayer ✓), Toban's gold, Potion (quest), Magic ogre potion, Spell scroll, Shaman robe, Cave nightshade) — EX (mostly).
- `2391 Ground bat bones` — OK (`prayer`).
- `2398 Cave nightshade` — OK (`herblore`).

### Demon Slayer + Hazeel Cult

- `2399-2401 Silverlight key ×3` — EX.
- `2402 Silverlight` — OK (`melee;quests`).
- `2403-2411` (Hazeel scroll, Chest key, Carnillean armour, Hazeel's mark, Ball, Diary in prayer, Door key, Magnet, Key) — EX (~9 items).
- `2405 Carnillean armour` — **REM melee**. Hazeel Cult quest cosmetic body armour. **ADD quests cosmetic**.
- `2408 Diary` — LOG. Currently in `prayer` via my "Robes/Holy symbols" pattern (Book of /Diary). Hazeel quest diary, not a holy book. **REM prayer**.

### God capes + god staves

- `2412-2414 Saradomin/Guthix/Zamorak cape` — OK (`mage`). Session 3 fix.
- `2415-2417 Saradomin/Guthix/Zamorak staff` — OK (`mage`).

### Murder Mystery (more) + holiday

- `2418-2425 Bronze key, Wig, Blue partyhat (dup), Key print, Paste, Vorkath's head` — EX (Murder Mystery + Vorkath's Head quest reward).
- `2422 Blue partyhat` — LOG (dup of 1042 canonical).

### Potions (4-dose)

- `2428 Attack potion(4)`, `2430 Restore potion(4)`, `2432 Defence potion(4)`, `2434 Prayer potion(4)`, `2436 Super attack(4)`, `2438 Fishing potion(4)`, `2440 Super strength(4)`, `2442 Super defence(4)`, `2444 Ranging potion(4)`, `2446 Antipoison(4)`, `2448 Superantipoison(4)`, `2450 Zamorak brew(4)` — OK (12 items, all classify correctly).
- `2452-2458 Antifire potion(4..1)` — OK (`herblore`). Could cross-tag to melee for dragon-fighting but herblore is canonical.

### Flower bouquets

- `2460-2476 Assorted/Red/Blue/Yellow/Purple/Orange/Mixed/White/Black flowers` (~9 items) — EX. Wedding event / Holy Grail flowers; cosmetic with stub blunt stats. Currently unclassified (which is correct).

### Lantadyme herb

- `2481 Lantadyme`, `2483 Lantadyme potion (unf)`, `2485 Grimy lantadyme` — OK (`herblore`).

### D'hide variants (range armour)

- `2487-2499 Blue/Red/Black d'hide vambraces, Blue/Red/Black d'hide chaps, Blue d'hide body` — OK (7 items, `range;crafting`).

---

## Session 9 totals

- Items reviewed: 159
- OK (correct as-is): 86
- EX (correctly excluded): 51
- ADD (missing tab, fixed): 20 (gnome intermediates, Rock-climbing boots, Pizza chain intermediates, Pie dish to cooking, Wrapped oomlie, Carnillean armour to quests)
- REM (misclassified, fixed): 2 (Carnillean armour from melee, Diary from prayer)
- LOG (deferred): 2 (Blue partyhat dup, Diary classifier loose match)

## Classifier changes made in session 9

See `audit/classifier-changes.md` "Session 9".

---

## Session 10: IDs 2501–2800

### D'hide armour variants + leather

- `2501 Red d'hide body`, `2503 Black d'hide body`, `2505 Blue dragon leather`, `2507 Red dragon leather`, `2509 Black dragon leather` — OK (5 items).

### Misc dups + clue prep

- `2511 Logs`, `2513 Dragon chainbody`, `2514 Raw shrimps`, `2516 Pot of flour`, `2518 Rotten tomato` — EX (dups / quest variants).
- `2520-2526 Brown/White/Black/Grey toy horsey` — EX (4 items, holiday cosmetic toys).
- `2528 Lamp`, `2529 Dead orb` — EX.
- `2530 Bones` — LOG (canonical dup of 526).

### Fire arrows

- `2532-2540 Iron/Steel/Mithril/Adamant/Rune fire arrow` — OK (5 items, `range;wc_fletching`).

### Rings (utility/teleport)

- `2550 Ring of recoil` — **ADD misc**. Defensive ring kept for PvM.
- `2552-2566 Ring of dueling(1..8)` — **ADD misc**. 7 charge variants of the duel arena teleport ring. Currently all unclassified. **Fix**: add to misc Teleport jewellery.
- `2568 Ring of forging` — **ADD mining_smithing** (used in iron smithing). Currently unclassified.
- `2570 Ring of life` — **ADD misc**. Defensive teleport ring.
- `2572 Ring of wealth` — OK (`misc`).

### Clue tools (verified)

- `2574 Sextant`, `2575 Watch`, `2576 Chart` — OK (`misc`).

### Treasure trail reward gear

- `2577 Ranger boots` — **REM melee**, **ADD range**. Range boots with melee-defence stats; classifier picked up via slot_pred. **Fix**: force_exclude on MELEE Boots + add to RANGE Boots force_include.
- `2579 Wizard boots` — OK (`mage`).
- `2581 Robin hood hat` — **REM melee**, **ADD range**. Same pattern as Ranger boots.
- `2583-2629 (t)/(g) trim/gold armour variants` (~24 items) — EX. Noise-filter excludes `(t)` and `(g)` suffixes by design. Treasure trail cosmetic; players who want these can re-tag.

### Clue scroll cosmetics

- `2631 Highwayman mask` — OK (`misc`).
- `2633-2643 Blue/Black/White beret, Tan/Dark/Black cavalier` — EX (6 items). Clue cosmetic hats; not banked long-term.
- `2645-2649 Red/Black/Brown headband` — EX (3 items). Same.
- `2651 Pirate's hat` — EX. Clue cosmetic.

### God armour (Saradomin/Guthix/Zamorak rune-tier)

- `2653-2675 Zamorak/Saradomin/Guthix platebody/platelegs/full helm/kiteshield` — OK (12 items, `melee;mining_smithing`). Smithing reward + melee gear.

### Clue scrolls (canonical dedup)

- `2677 Clue scroll (easy)` — OK (`misc`).
- `2722 Clue scroll (hard)` — OK (`misc`).
- `2678-2716, 2723-2799 Clue scroll (easy/hard) ×~50 dups` — LOG. Wiki canonical filter dedupes by name; all variants of "Clue scroll (easy)" with different IDs are treated as one. This is correct behaviour — players see them as identical items.
- `2714-2715 Casket (easy) ×2`, `2749-2756 Sliding piece ×7`, `2795 Puzzle box (hard)` — EX. Clue puzzle intermediates.

---

## Session 10 totals

- Items reviewed: 166
- OK (correct as-is): 33
- EX (correctly excluded): 50 (mostly clue cosmetic + canonical-filter dedup non-canonicals)
- ADD (missing tab, fixed): 12 (Ring of recoil, Ring of dueling ×7, Ring of forging, Ring of life, Ranger boots, Robin hood hat to range)
- REM (misclassified, fixed): 2 (Ranger boots, Robin hood hat from melee)
- LOG (deferred): 70+ (massive clue-scroll canonical dups; bones dup)

## Classifier changes made in session 10

See `audit/classifier-changes.md` "Session 10".

---

## Session 11: IDs 2801–3100

### Medium clue scrolls (canonical dedup)

- `2801 Clue scroll (medium)` — OK (`misc`).
- `2803–2858 Clue scroll (medium) variants` (~30 items) — LOG (canonical dups).
- `2832 Key (medium)`, `2842 Challenge scroll (medium)` — EX. Clue intermediates.

### Big Chompy / ogre arrows

- `2859 Wolf bones` — OK (`prayer`).
- `2861 Wolfbone arrowtips` — OK (`wc_fletching`).
- `2862 Achey tree logs` — OK (`wc_fletching;firemaking`).
- `2864 Ogre arrow shaft` — **ADD wc_fletching**. Currently unclassified.
- `2865 Flighted ogre arrow` — OK (`wc_fletching`).
- `2866 Ogre arrow` — OK (`range;wc_fletching`).
- `2871-2885` (Ogre bellows, Bloated toad, Raw chompy in cooking;hunter ✓, Cooked chompy in cooking ✓, Ruined chompy, Seasoned chompy, Ogre bow ✓, Chompy bird obj, Whoopsie) — EX (most) / OK (chompy meats).

### Elemental Workshop I

- `2886-2888 Battered book/key, A stone bowl` — EX.
- `2890 Elemental shield` — OK (`mage`).
- `2892 Elemental ore` — OK (`mining_smithing`).
- `2893 Elemental metal` — EX.

### Camouflage random event outfit (IDs 2894–2942)

5 colours (Grey/Red/Yellow/Teal/Purple) × 5 pieces (boots/robe top/robe bottoms/hat/gloves) = 25 items. Like the Mime outfit (sessions 3), all wrongly classified in combat tabs.

- `2894–2942` — **REM melee/mage** (25 items, slot-dependent). **ADD misc**. **Fix**: add `_CAMO_OUTFIT` constant analogous to `_MIME_OUTFIT`; force_exclude from each combat slot section; new misc "Camouflage outfit" section.

### Holy Grail follow-on

- `2944-2951 Golden key/tinderbox/candle/pot/hammer/feather/needle + Iron key` — EX (8 items, Holy Grail quest cosmetics).
- `2952 Wolfbane` — OK (`melee;quests`). Hard-coded combat weapon vs vampyres.

### Nature Spirit / Mort'ton

- `2953-2959 Murky water, Blessed water, Moonlight mead, Druid pouch, Rotten food` — EX (5 items, quest consumables).
- `2961 Silver sickle`, `2963 Silver sickle (b)` — OK (`prayer`).
- `2964-2974` (Washing bowl, Mirror, Journal, Druidic spell, A used spell, Mort myre fungus, Mort myre stem, Mort myre pear) — partial OK + EX.
- `2967 Journal` — **REM prayer**. Quest item, not a prayer book. Fixed by session 9's removal of "Diary"/"Journal" from prayer list.
- `2970 Mort myre fungus` — OK (`prayer;herblore`).
- `2972 Mort myre stem` — **ADD herblore** (used in Super Saradomin brew recipes? Actually used in Pray-related Mort'ton flavour). Currently unclassified.
- `2974 Mort myre pear` — **ADD herblore** (used in Nature Spirit serum).
- `2976 Sickle mould` — **ADD crafting**. Silversmith mould for sickles.

### Chompy bird hat variants

- `2978-2995 Chompy bird hat ×18` — **ADD misc**. 18 colour/style variants kept as Big Chompy kill-count trophy. Currently unclassified. **Fix**: misc Cosmetic outfit section.

### Agility / herblore continued

- `2996 Agility arena ticket` — OK (`agility_thieving`).
- `2997 Pirate's hook` — OK (`melee`). Cabin Fever quest cosmetic gloves with combat stats; players bank for cosmetic.
- `2998 Toadflax`, `3000 Snapdragon`, `3002 Toadflax potion (unf)`, `3004 Snapdragon potion (unf)`, `3049 Grimy toadflax`, `3051 Grimy snapdragon` — OK (6 items, `herblore`).

### Firework

- `3006 Firework` — **ADD misc** (Holiday rares). Holiday event item.

### Stamina / Energy / Agility / Magic potions

- `3008-3014 Energy potion(4..1)` — OK (`herblore`). Cross-tag to agility_thieving missing. **ADD agility_thieving** to all dose variants of "energy potion" family.
- `3016-3022 Super energy(4..1)` — Same. **ADD agility_thieving** cross-tag.
- `3024-3030 Super restore(4..1)` — OK (`melee;prayer;herblore`).
- `3032-3038 Agility potion(4..1)` — OK (`herblore`). **ADD agility_thieving** cross-tag.
- `3040-3046 Magic potion(4..1)` — OK (`mage;herblore`).

### Lava battlestaves

- `3053 Lava battlestaff` — OK (`mage;crafting`).
- `3054 Mystic lava staff` — OK (`mage`).

### Mime random event (actual)

- `3057-3061 Mime mask/top/legs/gloves/boots` — **ADD misc**. 5 items separate from the colour-prefixed `_MIME_OUTFIT` (those were the Mage Training Arena cosmetics; THIS is the real Mime random event). Currently unclassified. **Fix**: add a new `_REAL_MIME_OUTFIT` allowlist and Misc section entry.

### Stronghold of Security puzzle

- `3062 Strange box`, `3063 Cube part` — EX. SoS puzzle items.

### Range + claws

- `3093 Black dart` — OK (`range;wc_fletching`).
- `3095-3100 Bronze/Iron/Steel/Black/Mithril/Adamant claws` — OK (6 items, `melee`).

---

## Session 11 totals

- Items reviewed: 168
- OK (correct as-is): 50
- EX (correctly excluded): 20
- ADD (missing tab, fixed): 65 (Ogre arrow shaft, Mort myre stem/pear, Sickle mould, Camo outfit ×25, Chompy bird hats ×18, Firework, Mime outfit ×5, Energy/Super energy/Agility potions cross-tag ×12)
- REM (misclassified, fixed): 26 (Camo outfit ×25 from combat tabs, Journal from prayer)
- LOG (deferred): 30+ (clue medium dups)

## Classifier changes made in session 11

See `audit/classifier-changes.md` "Session 11".

---

## Session 12: IDs 3101–3400

### Claws + climbing boots

- `3101 Rune claws` — OK (`melee`).
- `3102-3114` (Combination, IOU, Secret way map, Stone ball, Certificate) — EX (5 items, Hazeel/dwarf quest items).
- `3105 Climbing boots` — OK (`melee;agility_thieving`). Has both tags.
- `3107 Spiked boots` — **ADD agility_thieving**. Currently only in melee.

### Granite shield + bones

- `3122 Granite shield` — **ADD melee** (cross-tag). Currently only `range` due to defence_ranged dominance. Players use it as a melee tank shield.
- `3123-3130 Shaikahan/Jogre/Burnt jogre/Pasty jogre/Marinated j' bones` — OK (5 items, all `prayer`). Burnt jogre also has cooking cross-tag.

### Tribal Totem follow-on

- `3135-3137 Prison key, Cell key 1, Cell key 2` — EX (3 items).

### Potato cactus

- `3138 Potato cactus` — OK (`herblore`).

### Dragon chainbody + karambwan

- `3140 Dragon chainbody` — OK (`melee;mining_smithing`).
- `3142 Raw karambwan` — OK (`cooking;fishing`).
- `3144 Cooked karambwan` — OK (`melee;range;mage;cooking`).
- `3146-3148` (Poison karambwan, Cooked karambwan dup, Burnt karambwan) — partial. Burnt karambwan in cooking ✓. Poison karambwan unclassified — could ADD cooking (food that poisons player). Dup is LOG.
- `3150 Raw karambwanji` — OK (`cooking;fishing`).
- `3153 Karambwan paste`, `3155 Karambwanji paste` — EX (Tai Bwo Wannai consumables).
- `3157 Karambwan vessel` — OK (`fishing`).

### Monkey Madness + Monkey bones

- `3161-3169` (Crafting manual, Sliced banana, Karamjan rum ×2, Monkey corpse/skin, Seaweed sandwich, Stuffed monkey) — EX (8 items, most are quest).
- `3179-3186 Small ninja monkey bones, Medium ninja monkey bones, Gorilla bones, Bearded gorilla bones, Monkey bones, Small zombie monkey bones, Large zombie monkey bones` — OK (7 items, all `prayer`).
- `3177 Left-handed banana` — EX. Monkey Madness quest item.
- `3187 Bones` — LOG. Canonical dup of 526.
- `3188 Cleaning cloth` — EX.

### Halberds

- `3190-3204 Bronze/Iron/Steel/Black/Mithril/Adamant/Rune/Dragon halberd` — OK (8 items, `melee`).

### Regicide / Underground Pass II

- `3206-3214` (King's message, Iorwerth's message, Crystal pendant, Sulphur, Pot of quicklime) — EX (5 items).
- `3211 Limestone` — OK (`construction`).
- `3213 Quicklime` — **ADD crafting** (used in construction recipes too — already covered as Limestone).
- `3215-3224` (Ground sulphur, Barrel, Barrel bomb, Barrel of coal tar/naphtha, Naphtha mix, Strip of cloth) — EX.

### Raw rabbit + cooking

- `3226 Raw rabbit` — OK (`cooking;hunter`).
- `3228 Cooked rabbit` — OK (`cooking`).
- `3230 Big book of bangs` — EX.

### Misc + Mountain Daughter / Troll Stronghold

- `3231 Symbol` — EX (cosmetic/quest variant).
- `3239 Bark` — **ADD crafting** (Splitbark material). Currently unclassified.
- `3261-3273` (Goutweed, Troll thistle, Dried/Ground thistle, Troll potion, Drunk parrot, Dirty robe, Fake man, Storeroom key, Alco-chunks, Compost bin, Spell) — EX (~12 items, all quest items).

### Vampyric quest content

- `3325 Vampyre dust` — EX. Vampyre Slayer quest residue.

### Snelms (snail helmets)

- `3327-3343 Myre/Blood'n'tar/Ochre/Bruise blue/Broken bark snelm + pointed variants` — OK (9 items, all `melee`). Tai Bwo Wannai helmets with defence stats.

### Snail materials

- `3345-3373 Blamish shells + Thin/Lean/Fat snail/snail meat` — EX (~13 items). Tai Bwo Wannai snail intermediates / Slayer drops, not banked long-term.
- `3375 Burnt snail` — OK (`cooking`).
- `3377 Sample bottle` — EX.

### Slimy eels (Mort'ton)

- `3379 Raw slimy eel` — OK (`cooking;fishing`).
- `3381 Cooked slimy eel` — OK (`melee;range;mage;cooking`).
- `3383 Burnt eel` — OK (`cooking`).

### Splitbark armour

- `3385-3393 Splitbark helm/body/legs/gauntlets/boots` — **REM melee** (5 items). Splitbark is canonical mage armour (gives prayer bonus, magic defence + minor melee defence). Currently `melee;mage` — should be `mage` only. **Fix**: force_exclude Splitbark pieces from MELEE Helmets/Body/Legs/Gloves/Boots.

### Diary + Shades remains

- `3395 Diary` — EX (Hazeel-style quest diary; session 9's fix removed Diary from prayer).
- `3396 Loar remains`, `3398 Phrin remains`, `3400 Riyl remains` — **ADD prayer**. Shades of Mort'ton remains used for prayer XP. Currently unclassified.

---

## Session 12 totals

- Items reviewed: 124
- OK (correct as-is): 47
- EX (correctly excluded): 50
- ADD (missing tab, fixed): 12 (Spiked boots agility, Granite shield melee, Bark crafting, Quicklime crafting, Loar/Phrin/Riyl remains prayer ×3, Poison karambwan cooking)
- REM (misclassified, fixed): 5 (Splitbark armour ×5 from melee)
- LOG (deferred): 2 (Bones 3187 dup, Cooked karambwan 3147 dup)

## Classifier changes made in session 12

See `audit/classifier-changes.md` "Session 12".

---

## Session 13: IDs 3401–3700

### Shades remains + Nature Spirit Serum

- `3402 Asyn remains`, `3404 Fiyr remains` — OK (`prayer`). Post-session 12 pattern fix.
- `3406-3419 Unfinished potion + Serum 207/208 dose variants ×8` — EX. Nature Spirit quest items.

### Construction materials

- `3420 Limestone brick` — OK (`construction`).
- `3422-3428 Olive oil(4..1)` — **ADD prayer** (used for blessing pyre logs / Saradomin's blessing). 4 items currently unclassified.
- `3430-3436 Sacred oil(4..1)` — **ADD prayer**. Same family; blessed pyre log input. 4 items.

### Pyre logs

- `3438-3448 Pyre logs / Oak/Willow/Maple/Yew/Magic pyre logs` — OK (6 items, `wc_fletching;firemaking`).

### Treasure trail clue keys (colour-coded)

- `3450-3469 Bronze/Steel/Black/Silver key × red/brown/crimson/black/purple` — **ADD misc** (20 items). Treasure trail clue puzzle keys. **Fix**: add to misc Clue tools section using pattern.

### Treasure trail rewards

- `3470 Fine cloth` — **ADD construction** (Mahogany Homes / Carpenter unlocks). Could also be crafting. Add to construction materials.
- `3472-3477 Black/Adamant/Rune plateskirt (t)/(g) variants` — EX (6 items, noise-filter excluded).
- `3478-3480 Zamorak/Saradomin/Guthix plateskirt` — OK (`melee;mining_smithing`).
- `3481-3488 Gilded platebody/platelegs/plateskirt/full helm/kiteshield` — OK (5 items, `melee;mining_smithing`).

### Clue scroll IDs en masse

- `3490-3618 Clue scroll (easy/hard/medium) ×100+` — LOG (canonical dedupe; all the duplicates fold into the canonical 2677/2722/2801).
- `3619-3650 Sliding piece ×~14` — EX (clue puzzle intermediates).

### Misc / Shades of Mort'ton tools

- `3667 Boss helper tool` — EX. Beta / minigame helper.
- `3678 Flamtaer hammer` — **ADD construction**. Shades of Mort'ton temple repair hammer.
- `3680 Shoe` — EX. Random event drop.

### Fremennik Trials

- `3688 Unstrung lyre` — EX. Quest intermediate.
- `3689 Lyre`, `3690 Enchanted lyre` — OK (`crafting`).
- `3691 Enchanted lyre(1)` — LOG (charge variant of 3690).
- `3692-3699 Branch, Golden fleece, Golden wool, Pet rock, Hunters' talisman ×2, Exotic flower, Fremennik ballad` — EX (~8 items, mostly Fremennik quest items).
- `3696 Hunters' talisman` — OK (`runecraft`).

### Forestry

- `3700 Sturdy boots` — OK (`wc_fletching`).

---

## Session 13 totals

- Items reviewed: 177
- OK (correct as-is): 16
- EX (correctly excluded): 25
- ADD (missing tab, fixed): 29 (Olive oil + Sacred oil × 4 each = 8 prayer; 20 clue keys to misc; Fine cloth to construction; Flamtaer hammer to construction)
- REM (misclassified, fixed): 0
- LOG (deferred): 110+ (the massive clue-scroll dedupe block + sliding piece dups + Enchanted lyre(1))

## Classifier changes made in session 13

See `audit/classifier-changes.md` "Session 13".

---

## Session 14: IDs 3701–4000

### Fremennik Trials + Mountain Daughter quest items

- `3701-3746` (~30 items: Tracking map, Custom bow string, Unusual fish, Sea fishing map, Weather forecast, Champions token, Legendary cocktail, Fiscal statement, Promissory note, Warriors' contract, Low alcohol keg, Strange object ×2, Magnet, Blue thread, Small pick, Toy ship, Full bucket variants ×5, Frozen bucket, Full jug variants ×3, Frozen jug, Vase variants ×4, Frozen key, Red herring, Red disk, Wooden disk, Seer's key, Sticky red goop) — EX. All quest consumables.
- `3711 Keg of beer` — OK (`cooking`).

### Fremennik gear (quest reward)

- `3748 Fremennik helm`, `3749 Archer helm`, `3751 Berserker helm`, `3753 Warrior helm` — OK (4 items, `melee`).
- `3755 Farseer helm` — OK (`melee;mage`). Hybrid helm with magic and melee defence.
- `3757 Fremennik blade`, `3758 Fremennik shield` — OK (`melee`).
- `3759-3789 Fremennik cyan/brown/blue/green/red/grey/yellow/teal/purple/pink/black cloak` — LOG. 11 cosmetic cloaks landing in range via tiny defence_ranged stat dominance; same pattern as colour capes (1007-1031). Could add Fremennik cloak names to `_BASIC_COLOUR_CAPES`. Deferring — they're at least themed.
- `3767-3775 Fremennik brown/grey/beige/red/blue shirt` — OK (5 items, `melee`). Quest cosmetic body armour with stub stats.
- `3791 Fremennik boots`, `3793 Fremennik robe`, `3795 Fremennik skirt`, `3797 Fremennik hat`, `3799 Fremennik gloves` — OK (5 items, various combat tabs).

### Beer duplicates

- `3801 Keg of beer` (dup of 3711), `3803 Beer tankard`, `3805 Tankard` — EX / LOG dup.

### God pages (Treasure trail rewards)

- `3827-3830 Saradomin page 1..4` — **ADD prayer** (used to make Saradomin book + book(g)). Currently unclassified. 4 items.
- `3831-3834 Zamorak page 1..4` — **ADD prayer**. 4 items.
- `3835-3838 Guthix page 1..4` — **ADD prayer**. 4 items.

### Prayer books

- `3839 Damaged book` — OK (`prayer`, dup variant matched by force_include).
- `3840 Holy book` — OK (`melee;range;mage;prayer`).
- `3841 Damaged book` (dup) — EX (canonical dup).
- `3842 Unholy book` — OK (`melee;mage;prayer`).
- `3843 Damaged book` (dup) — EX.
- `3844 Book of balance` — OK (4 tabs).
- `3845 Journal`, `3846 Diary` — EX (Lunar Diplomacy / quest, both removed from prayer in sessions 9/11).
- `3847 Manual` — **REM prayer**. Generic quest manual, not a prayer item. **Fix**: remove from prayer Quest-related items.

### Misc Lunar Diplomacy items

- `3848 Lighthouse key`, `3849 Rusty casket` — EX.

### Games necklace (canonical + dose)

- `3853-3867 Games necklace(8..1)` — OK (7 items, `misc`).

### Other quest items

- `3881-3901` (Board game piece, Stool, Awful/Good anthem, Treaty, Giant nib, Giant pen, Iron sickle, Ghrim's book) — EX (~9 items, mostly Fremennik / Lunar Diplomacy).

### Empty-name entries

- `3904-3966` (~14 empty-name rows in CSV) — EX. Wiki canonical entries with stub/template names; not real items.

---

## Session 14 totals

- Items reviewed: 125
- OK (correct as-is): 35
- EX (correctly excluded): 70
- ADD (missing tab, fixed): 12 (12 god pages to prayer)
- REM (misclassified, fixed): 1 (Manual from prayer)
- LOG (deferred): 14 (Fremennik cloaks in range, Beer dup, empty-name canonical rows)

## Classifier changes made in session 14

See `audit/classifier-changes.md` "Session 14".

---

## Session 15: IDs 4001–4300

### Monkey Madness (Tree Gnome Stronghold)

- `4001-4034` (~30 items: Hardy gout tuber, Spare controls, Gnome royal seal, Narnode's orders, Monkey dentures, Enchanted bar, Eye of gnome ×2, Monkey magic, Monkey nuts, Monkey bar, Banana stew, Monkey wrench, M'amulet mould, M'speak amulet ×2, Monkey talisman, greegrees ×7, Monkey, Monkey skull) — EX. Quest consumables / monkey-transformation items.

### Castle Wars

- `4035 10th squad sigil`, `4037-4039 Banners (Saradomin/Zamorak)` — EX (3 items).
- `4041 Saradomin cloak`, `4042 Zamorak cloak` — OK (`mage`).
- `4043-4055 Rock, Explosive potion, Climbing rope, Bandages, Toolkit, Barricade, Castlewars manual` — EX (7 items).
- `4067 Castle wars ticket` — EX. Could add to misc but minigame token; LOG.
- `4068-4072 Decorative sword/armour/helm/shield` — OK (5 items, `melee`). Castle wars decorative armour.

### Haunted Mine (Salve amulet)

- `4073-4082 Damp tinderbox, Glowing fungus, Crystal-mine key, Zealot's key, Yo-yo, Salve amulet, Salve shard` — partial OK / EX.
- `4081 Salve amulet` — OK (`melee`). Anti-undead damage bonus.

### Trollweiss / Mountain Daughter

- `4083-4086 Sled, Wax, Trollweiss` — EX. Quest items.

### Dragon platelegs + Mystic robes

- `4087 Dragon platelegs` — OK (`melee;mining_smithing`).
- `4089-4117 Mystic hat/robe top/robe bottom/gloves/boots × Light/Standard/Dark` — OK (15 items, all `mage`). Three colour variants × 5 pieces.

### Metal boots family

- `4119-4131 Bronze/Iron/Steel/Black/Mithril/Adamant/Rune boots` — OK (7 items, `melee`).

### Abyssal whip / Slayer task gear

- `4151 Abyssal whip` — OK (`melee`).
- `4153 Granite maul` — OK (`melee;slayer`).
- `4155 Enchanted gem` — OK (`slayer`).
- `4156 Mirror shield` — OK (`melee;slayer`).
- `4158 Leaf-bladed spear` — OK (`melee`).
- `4160 Broad arrows` — OK (`range;wc_fletching`).
- `4161 Bag of salt` — **ADD slayer**. Used to defeat Rockslugs. Currently unclassified.
- `4162 Rock hammer`, `4164 Facemask`, `4166 Earmuffs`, `4168 Nose peg` — OK (4 items, `slayer`).
- `4170 Slayer's staff` — OK (`mage;slayer;quests`).

### Various Slayer-related dups + quest items

- `4178-4209` (~30 items: Abyssal whip dup, Stick, Dragon platelegs dup, Mouth grip, Goutweed dup, Star amulet, Cavern/Tower/Shed keys, Marble amulet, Obsidian amulet, Garden cane/brush/Extended brush, Torso/Arms/Legs/Decapitated head, Pickled brain, Conductor mould, Conductor, Ring of charos, Journal/Letter, Consecration seed, Crystal weapon seed, Cadarn lineage) — mostly EX (quest items).
- `4205 Consecration seed`, `4207 Crystal weapon seed` — OK (`farming`).

### Crystal bow + crystal shield

- `4212 New crystal bow` — OK (`range;wc_fletching`).
- `4224 New crystal shield` — OK (`range`). Crystal Elf armour.
- `4213, 4235 Bow/Shield dups` — EX (canonical dups).
- `4236 Signed oak bow` — OK (`range;wc_fletching`). Roving Elves reward.

### Ghosts Ahoy

- `4237-4253` (~14 items: Nettle-water, Puddle of slime, Nettle tea ×2, Nettles, Cup of tea ×3 dups, Porcelain cup, Mystical robes, Book of haricanto, Translation manual, Ectophial, Model ship) — EX (most).
- `4248 Book of haricanto` — currently in prayer (via _name_starts("Book of ")). It's not a prayer item — Ghosts Ahoy guide book. **REM prayer**. **Fix**: force_exclude "Book of haricanto" from PRAYER Holy symbols.
- `4251 Ectophial` — OK (`quests`).

### Bonemeal family

- `4255-4271 Bonemeal / Bat / Big / Burnt / Burnt jogre / Baby dragon / Dragon / Wolf / Small ninja / Medium ninja / Gorilla / Bearded gorilla / Monkey / Small zombie monkey / Large zombie monkey / Skeleton / Jogre bonemeal` (17 items) — **ADD prayer** for the 15 currently unclassified ones. **Fix**: change prayer Bone secondaries to use pattern `_name_ends(" bonemeal")` to catch all variants automatically.
- `4258 Burnt bonemeal`, `4259 Burnt jogre bonemeal` — OK (`cooking`) plus need prayer cross-tag added via the pattern fix above.
- `4286 Bucket of slime` — OK (`prayer`).

### Misc quest items

- `4272-4277` (Bone key, Chest key, Map scraps ×3, Treasure map) — EX.
- `4278 Ecto-token` — **ADD misc**. Ghosts Ahoy currency. Currently unclassified.
- `4283 Petition form`, `4284 Bedsheet` — EX.

### Raw/cooked food dupes

- `4287-4293 Raw beef / Raw chicken / Cooked chicken / Cooked meat dups` — LOG (canonical dups).

### Ham (Lowlands of Ardougne) gear

- `4298 Ham shirt`, `4300 Ham robe` — OK (`melee`). Hand in the Sand cosmetic gear.

---

## Session 15 totals

- Items reviewed: 169
- OK (correct as-is): 60
- EX (correctly excluded): 75
- ADD (missing tab, fixed): 17 (Bag of salt slayer, 15 bonemeal variants prayer, Ecto-token misc)
- REM (misclassified, fixed): 1 (Book of haricanto from prayer)
- LOG (deferred): 7 (Castle wars ticket, raw/cooked food dups, Trollweiss weapon)

## Classifier changes made in session 15

See `audit/classifier-changes.md` "Session 15".

---

## Session 16: IDs 4301–4600

### H.A.M. set

- `4302 Ham hood`, `4304 Ham cloak`, `4306 Ham logo`, `4308 Ham gloves`, `4310 Ham boots` — partial OK. Ham set is thieving disguise; classifier puts pieces in melee/range based on minimal stats. Leave for now.
- `4313 Crystal singing for beginners` — EX. Roving Elves quest item.

### Team capes (Castle Wars cosmetic, IDs 4315–4413)

50 team capes. All currently in range due to defence_ranged stat dominance. **Mass REM range, ADD misc**. **Fix**: pattern force_exclude `Team-N cape` from RANGE Capes + new misc section.

### Death Plateau / Eadgar's Ruse

- `4415-4435` (Blunt axe, Herbal tincture, Guthix rest dose variants ✓, Stodgy/Comfy mattress, Iron oxide, Animate rock scroll, Directionals, Broken vane part, Ornament, Weathervane pillar, Weather report) — EX (mostly).
- `4417-4423 Guthix rest(4..1)` — OK (`herblore`).

### Pottery / Mort'ton tea

- `4436 Airtight pot`, `4438 Unfired pot lid` (in crafting ✓), `4440 Pot lid` — partial.
- `4442 Breathing salts`, `4443 Chicken cage`, `4444 Sharpened axe`, `4445 Red mahogany log`, `4446 Steel key ring`, `4447 Antique lamp`, `4456-4464` (Bowl of hot water, Cup of water, Cup of hot water, Ruined herb tea, Herb tea mix) — EX.

### Fremennik Isles + misc

- `4484-4500` (Safety guarantee, White pearl, White pearl seed in farming ✓, Half a rock, Corpse of woman, Asleif's necklace, Mud, Muddy rock, Pole ×4, Broken pole, Rope) — EX.
- `4502 Bearhead` — **REM melee**, **ADD quests**. Mountain Daughter quest cosmetic helmet.

### Castle Wars decorative armour + cloak/hood

- `4503-4512 Decorative sword/armour/helm/shield dups` — LOG (canonical dups).
- `4513-4516 Castlewars hood/cloak ×2 dups` — partial OK (melee/range), dups LOG.

### Lanterns + Spiny helmet

- `4517 Giant frog legs` — EX.
- `4522-4548` (Oil lamp, Empty oil lamp, Empty candle lantern, Candle lantern ✓, Empty oil lantern, Oil lantern ✓, Oil lantern frame, Lantern lens, Bullseye lantern (unf), Bullseye lantern (empty), Bullseye lantern ✓) — **ADD firemaking** for the unclassified empty/unf variants (Empty oil lamp, Empty candle lantern, Empty oil lantern, Oil lantern frame, Lantern lens, Bullseye lantern (unf), Bullseye lantern (empty)). 7 items.
- `4551 Spiny helmet` — OK (`melee;slayer`).

### Easter event

- `4557 Telescope dummy` — EX.
- `4558-4564 Blue/Deep blue/White/Purple/Red/Green/Pink sweets` — **ADD misc** (7 items). Holiday cosmetics. Add to Holiday rares.
- `4565 Easter basket` — **ADD misc**. Easter event weapon.
- `4566 Rubber chicken` — **ADD misc**. April Fools event weapon.

### Heroes' Quest

- `4567 Gold helmet` — LOG. Heroes' Quest gold cosmetic; currently melee but it's a quest cosmetic. Could be quests.

### Hand in the Sand / Dwarf Cannon repeat

- `4568-4579` (Dwarven lore, Book page 1/2/3, Pages ×2, Base schematics, Schematic, Schematics ×2, Cannon ball) — EX.

### Black spear + Dragon platelegs / scimitar

- `4580 Black spear` — OK (`melee`).
- `4585 Dragon plateskirt` — OK (`melee;mining_smithing`).
- `4587 Dragon scimitar` — OK (`melee`).

### Sorceress's Garden + RFD opener

- `4589-4598` (Keys, Jewels, Kharidian headpiece, Fake beard, Karidian disguise, Note ×2) — EX.

### Blackjacks (Thieving)

- `4599 Oak blackjack`, `4600 Willow blackjack` — OK (`agility_thieving`).

---

## Session 16 totals

- Items reviewed: 161
- OK (correct as-is): 30
- EX (correctly excluded): 60
- ADD (missing tab, fixed): 60 (50 team capes to misc, 7 sweets to misc, Easter basket/Rubber chicken/Bearhead, 7 lantern variants to firemaking)
- REM (misclassified, fixed): 51 (50 team capes from range, Bearhead from melee)
- LOG (deferred): 12 (Decorative armour dups, Castle wars hood/cloak dups, Gold helmet, Pole variants)

## Classifier changes made in session 16

See `audit/classifier-changes.md` "Session 16".

---

## Session 17: IDs 4601–4900

### Desert / Sphinx / Spirits of the Elid quest items

- `4601-4691` (~50 items: Ugthanki dung ×2, Receipt, Hag's poison, Snake charm, Snake basket variants, Super kebab, Red hot sauce, Desert disguise, Spinning plate, Broken plate, Letter, Varmen's notes, Display cabinet key, Statuette, Strange implement, Black mushroom, Phoenix feather, Black dye, Phoenix quill pen, Golem program, Etchings, Translation, Warm key, Ring of visibility, Silver pot, Blessed pot, Garlic powder, Blood/Ice/Smoke/Shadow diamond, Gilded cross, Canopic jar, Holy symbol dup, Unholy symbol dup, Linen, Embalming manual, Bucket of sap, Pile of salt, Sphinx's token, Catspeak amulet) — EX (most). Sphinx Eye of the Sphinx tablets are quest consumables.
- `4627 Bandit's brew` — OK (`cooking`).
- `4675 Ancient staff` — OK (`mage`).
- `4692 Gold leaf` — OK (`construction`).
- `4694-4699 Steam/Mist/Dust/Smoke/Mud/Lava rune` — OK (6 items, `mage;runecraft`).
- `4700 Sapphire lantern` — OK (`firemaking`).
- `4703 Magic stone` — OK (`construction`).

### Barrows armour (huge classifier fix needed)

The 6 Barrows sets all have defence on multiple combat styles, which makes my naive stat-dominance classifier put pieces in the wrong tab. Canonical OSRS combat affinity:

- **Ahrim's** (4708/4710/4712/4714) — MAGE armour only. Currently `melee;mage` for hood/robetop/robeskirt. **REM melee** all 4.
- **Dharok's** (4716/4718/4720/4722) — MELEE only. Helm/platebody/platelegs land in `melee;range` due to range defence. **REM range** all body parts. Greataxe stays melee.
- **Guthan's** (4724/4726/4728/4730) — MELEE only. Same issue: helm/platebody/chainskirt in `melee;range`. **REM range** all body parts.
- **Karil's** (4732/4734/4736/4738/4740) — RANGE only. Karil's coif in `melee` (wrong!), leathertop/leatherskirt in `mage` (also wrong!), Karil's crossbow + Bolt rack ✓. **REM melee+mage**, **ADD range** for coif/leathertop/leatherskirt.
- **Torag's** (4745/4747/4749/4751) — MELEE only. Helm/platebody/platelegs in `melee;range`. **REM range** all body parts.
- **Verac's** (4753/4755/4757/4759) — MELEE only. Already correct.

**Fix**: introduce `_AHRIM_PIECES`, `_KARIL_PIECES`, `_DHAROK_PIECES`, `_GUTHAN_PIECES`, `_TORAG_PIECES` constants; force_exclude in the wrong combat tabs.

### Brutal arrows

- `4773-4803 Bronze/Iron/Steel/Black/Mithril/Adamant/Rune brutal` — OK (7 items, `range`).

### Zogre Flesh Eaters

- `4808-4818` (Black prism, Torn page, Ruined backpack, Dragon inn tankard, Zogre bones ✓, Sithik portrait, Signed portrait, Book of portraiture, Ogre artefact) — EX (most).
- `4812 Zogre bones` — OK (`prayer`).
- `4817 Book of portraiture` — **REM prayer**. Quest book, not prayer. **Fix**: force_exclude on PRAYER Holy symbols.

### Nails (smithing + construction)

- `4819-4824 Bronze/Iron/Black/Mithril/Adamantite/Rune nails` — OK (6 items, `mining_smithing;construction`).

### Ogre composite bow + Book of HAM

- `4825 Unstrung comp bow` — **ADD wc_fletching**. Currently unclassified.
- `4827 Comp ogre bow` — OK (`range;wc_fletching`).
- `4829 Book of 'h.a.m'` — **REM prayer**. HAM book (Thieving), not prayer. **Fix**: force_exclude.

### Ogre bones (Zogre Flesh Eaters / Garden of Tranquillity)

- `4830 Fayrg bones`, `4832 Raurg bones`, `4834 Ourg bones` — OK (3 items, `prayer`).

### Remaining quest items + Relicym's balm

- `4836-4839` (Strange potion, Necromancy book, Cup of tea dup, Ogre gate key) — EX.
- `4840 Unfinished potion` — LOG (generic dup).
- `4842-4848 Relicym's balm(4..1)` — **ADD herblore**. Anti-disease potion, currently unclassified.
- `4850 Ogre coffin key` — OK (`prayer`).
- `4852-4855 Zogre/Fayrg/Raurg/Ourg bonemeal` — OK (4 items, `prayer` via bonemeal pattern).

---

## Session 17 totals

- Items reviewed: 125
- OK (correct as-is): 40
- EX (correctly excluded): 55
- ADD (missing tab, fixed): 7 (Unstrung comp bow + 4 Relicym's balm doses, Karil's leather pieces to range ×2 -- pre-fix; actual ADDs after fix more)
- REM (misclassified, fixed): 16 (Ahrim's 3 pieces from melee; Dharok's/Guthan's/Torag's 9 pieces from range; Karil's 3 pieces wrong-tabbed; Book of portraiture + Book of 'h.a.m' from prayer)
- LOG (deferred): 7 (canonical dups)

## Classifier changes made in session 17

See `audit/classifier-changes.md` "Session 17".

---

## Session 18: IDs 4901–5200

(Sparse ID range — only 53 canonical items in this window.)

### Cave eel + Frog spawn

- `5001 Raw cave eel` — OK (`cooking;fishing`).
- `5002 Burnt cave eel` — OK (`cooking`).
- `5003 Cave eel` — OK (`melee;range;mage;cooking;fishing`).
- `5004 Frog spawn` — OK (`cooking;fishing`).

### Lost Tribe quest

- `5008-5012 Brooch, Goblin symbol book, Key, Silverware, Peace treaty` — EX (5 items).

### Mining helmet + Bone weapons

- `5014 Mining helmet` — OK (`melee;firemaking`). Light source + head slot defence.
- `5016 Bone spear`, `5018 Bone club` — OK (2 items, `melee`). Big Chompy quest weapons.
- `5020 Minecart ticket` — EX.

### Lost Tribe / Goblin Diplomacy cosmetics

- `5024-5052 Woven top ×3 / Shirt ×3 / Trousers ×3 / Shorts ×3 / Skirt ×3` — EX (15 items). Lost Tribe / NPC cosmetics; players don't bank these.

### Dwarven Cannon / Forgettable Tale

- `5054-5067` (Dwarf, Dwarven battleaxe ×2 dups, Left/Right boot, Exquisite boots, Book on costumes, Meeting notes, Exquisite clothes) — EX (~9 items).

### Bird nests / Bird's egg

- `5070 Bird nest` — OK (`wc_fletching`).
- `5073-5075 Bird nest` (3 dups) — LOG (canonical dedupe).
- `5076 Bird's egg` — **ADD hunter** (Falconry bird egg drop). Currently unclassified.

### Seeds

- `5096 Marigold seed` — **ADD farming**. Currently blocked by force_exclude from a prior session — that was wrong; Marigold IS a real farming seed (flower). **Fix**: remove the force_exclude.
- `5097-5106 Rosemary/Nasturtium/Woad/Limpwurt/Redberry/Cadavaberry/Dwellberry/Jangerberry/Whiteberry/Poison ivy seed` — OK (10 items, `farming`).

---

## Session 18 totals

- Items reviewed: 53
- OK (correct as-is): 18
- EX (correctly excluded): 28 (Lost Tribe / Forgettable Tale quest items + cosmetics)
- ADD (missing tab, fixed): 2 (Bird's egg hunter, Marigold seed farming)
- REM (misclassified, fixed): 0
- LOG (deferred): 5 (Bird nest dups, Dwarven battleaxe dup, canonical dedupe)

## Classifier changes made in session 18

See `audit/classifier-changes.md` "Session 18".

---

## Session 19: IDs 5201–5500

Heavy farming batch. Most items correctly classified; gaps are mostly harvest produce and tools.

### Tree / herb / seed family (5280–5324, 5358–5500)

- `5280-5324` (~45 seeds: Cactus/Belladonna, all tree seeds, all clean herb seeds, all hops seeds, all allotment seeds) — OK. Already in farming.
- `5282 Mushroom spore` — **ADD farming**. Bittercap mushroom seed equivalent.
- `5312 Acorn` — **ADD farming**. Oak tree seedling.
- `5358-5363 Oak/Willow/Maple/Yew/Magic/Spirit seedling` — OK (6 items, farming).
- `5370-5375 Tree saplings` — OK (6 items, farming).
- `5480-5500 Fruit tree seedlings + saplings` — OK (~14 items).

### Farming tools / outfit

- `5325 Gardening trowel` — **ADD farming**. Currently unclassified.
- `5327 Spade handle`, `5328 Spade head` — EX (quest items, Family Crest).
- `5329 Secateurs`, `5331-5340 Watering can (0..8)`, `5341 Rake`, `5343 Seed dibber` — OK (~10 items, farming).
- `5345 Gardening boots` — **ADD farming**. Cosmetic farming outfit boots.
- `5347 Rake handle`, `5348 Rake head` — EX (quest items).

### Plant pot variants

- `5350 Empty plant pot` — OK (farming).
- `5352 Unfired plant pot` — OK (crafting).
- `5354 Filled plant pot` — **ADD farming**. Currently unclassified.
- `5356 Plant pot` — OK (crafting;farming;misc).

### Fruit / vegetable harvest produce

- `5378-5416 Apples(1..5), Oranges(1..5), Strawberries(1..5), Bananas(1..5)` (~20 items) — **ADD farming + cooking** (harvest baskets). Cross-tag pattern.
- `5418 Empty sack` — **ADD farming**.
- `5420-5478 Potatoes(1..10), Onions(1..10), Cabbages(1..10)` (~30 items) — **ADD farming + cooking** (harvest sacks).
- `5376 Basket` — **ADD farming** (harvest container).

---

## Session 19 totals

- Items reviewed: 133
- OK (correct as-is): 80
- EX (correctly excluded): 6 (quest part items: Spade handle/head, Rake handle/head)
- ADD (missing tab, fixed): 47 (Mushroom spore, Acorn, Gardening trowel/boots, Filled plant pot, Basket, Empty sack, ~20 fruit baskets to farming+cooking, ~30 vegetable sacks to farming+cooking)
- REM (misclassified, fixed): 0
- LOG (deferred): 0

## Classifier changes made in session 19

See `audit/classifier-changes.md` "Session 19".

---

## Session 20: IDs 5501–5800

### Farming + Strawberry

- `5501-5503 Papaya/Palm/Calquat sapling` — OK (`farming`).
- `5504 Strawberry` — **ADD cooking** (singular fruit; Strawberry pie input).

### Books / runecraft

- `5506-5508 Old man's message / Strange book / Book of folklore` — Book of folklore in prayer ✓; others EX.
- `5509-5514 Small/Medium/Large/Giant pouch` — OK (4 items, `runecraft`).
- `5516 Elemental talisman` — OK.
- `5519 Scrying orb`, `5520 Abyssal book` — partial. **ADD slayer** for Abyssal book.
- `5521 Binding necklace` — **ADD runecraft**. RC neck slot teleport.
- `5523 Tiara mould` — OK (`crafting`).
- `5525-5549 Tiara + Air/Mind/Water/Body/Earth/Fire/Cosmic/Nature/Chaos/Law/Death/Blood tiara` — OK (13 items, `runecraft`).

### Rogue equipment (Stealing Creation cosmetic)

- `5553-5557 Rogue top / mask / trousers / gloves / boots` — **REM melee, range, mage** (5 items). Currently in all 4 combat tabs + agility_thieving. Rogue set is thieving-only. **Fix**: force_exclude on all 5 melee/range/mage armour slot sections.
- `5558 Rogue kit` — OK (`agility_thieving`).
- `5559 Flash powder` — EX.
- `5560 Stethoscope` — OK (`agility_thieving`).

### Misc quest items

- `5561-5568` (Mystic jewel, Gear ×5, Tile) — EX.

### Initiate set (Recruitment Drive)

- `5574-5576 Initiate sallet/hauberk/cuisse` — OK (3 items, `melee;prayer`). Prayer-themed melee armour.

### Devious Minds + alchemy items

- `5577-5605` (~25 items: Cupric sulfate, Acetic acid, Gypsum, Sodium chloride, Nitrous oxide, Vial of liquid, ore powders, Bronze key, Metal spade, Alchemical notes, ??? mixture, Tin, Chisel/Bronze wire/Shears/Magnet/Knife dups) — EX (all quest items / dups).
- `5606 Makeover voucher` — EX.

### RFD transformations + Hourglass + Magic carpet

- `5607 Grain` (cape slot — transformation), `5608 Fox`, `5609 Chicken`, `5610 Hourglass`, `5614 Magic carpet` — EX.
- `5615 Shaikahan bonemeal` — OK (`prayer`).

### Beer / brewing

- `5732 Stool`, `5733 Rotten potato` — EX.
- `5739-5761` (Asgarnian ale(m), Mature wmb, Greenman's ale(m), Dragon bitter(m), Dwarven stout(m), Moonlight mead(m), Axeman's folly + (m), Chef's delight + (m), Slayer's respite + (m)) — **ADD cooking** (~12 items). Matured beer brewing outputs.
- `5763 Cider`, `5765 Mature cider` — OK (`cooking`).
- `5767 Ale yeast` — **ADD cooking**. Brewing secondary.
- `5769 Calquat keg` — **ADD cooking**. Empty brewing keg.
- `5777, 5785, 5793 Dwarven stout(4), Asgarnian ale(4), Greenmans ale(4)` — **ADD cooking** (3 items, charge variants).

---

## Session 20 totals

- Items reviewed: 93
- OK (correct as-is): 30
- EX (correctly excluded): 35
- ADD (missing tab, fixed): 23 (Strawberry, Abyssal book, Binding necklace, ~16 beer variants, Ale yeast, Calquat keg)
- REM (misclassified, fixed): 15 (5 Rogue pieces × 3 combat tabs)
- LOG (deferred): 0

## Classifier changes made in session 20

See `audit/classifier-changes.md` "Session 20".

---

## Session 21: IDs 5801–6100

### Beer / brew (4) and (m4) variants

- `5801 Mind bomb(4)`, `5825 Axeman's folly(4)`, `5833 Chef's delight(4)`, `5841 Slayer's respite(4)`, `5849 Cider(4)` — **ADD cooking** (5 items). Beer charge variants not matched by my prior (4) pattern (only beer-startswith subset was covered).
- `5809 Dragon bitter(4)`, `5817 Moonlight mead(4)` — OK (`cooking`).
- `5857-5929 (m4) variants` (~9 items: Dwarven stout/Asgarnian ale/Greenmans ale/Mind bomb/Dragon bitter/Moonlight mead/Axeman's folly/Chef's delight/Slayer's respite/Cider) — **ADD cooking**. Mature 4-charge variants. **Fix**: expand pattern to also match `(m4)` suffix.

### Farming materials

- `5931 Jute fibre`, `5933 Willow branch` — **ADD farming** (2 items).
- `5935 Coconut milk` — **ADD cooking** (gnome cocktail ingredient).

### Weapon poison family

- `5936 Weapon poison+ (unf)`, `5939 Weapon poison++ (unf)` — **ADD herblore** (2 items).
- `5937 Weapon poison(+)`, `5940 Weapon poison(++)` — OK (`herblore`).

### Antidote+ / Antidote++ family

- `5942 Antidote+ (unf)`, `5951 Antidote++ (unf)` — **ADD herblore** (2 items).
- `5943-5949 Antidote+(4..1)`, `5952-5958 Antidote++(4..1)` — OK (8 items, herblore).

### Tomatoes harvest sacks

- `5960-5968 Tomatoes(1..3, 5)` — OK (4 items, `cooking;farming`).

### Harvest fruits / brew secondaries

- `5970 Curry leaf`, `5972 Papaya fruit`, `5974 Coconut`, `5976 Half coconut`, `5978 Coconut shell`, `5980 Calquat fruit`, `5982 Watermelon`, `5984 Watermelon slice` — **ADD cooking + farming** (~8 items). Tree-grown produce.
- `5986 Sweetcorn`, `5988 Cooked sweetcorn`, `5990 Burnt sweetcorn` — OK (3 items, `cooking`).
- `5992 Apple mush` — **ADD cooking** (brewing ingredient).
- `5994-6002 Hammerstone/Asgarnian/Yanillian/Krandorian/Wildblood hops` — **ADD farming** (5 items, hop seed outputs).
- `6004 Mushroom`, `6006 Barley`, `6008 Barley malt` — **ADD farming + cooking** (3 items).
- `6010 Marigolds`, `6012 Nasturtiums`, `6014 Rosemary` — **ADD farming** (3 items, flower outputs).

### Herblore secondaries

- `6016 Cactus spine`, `6018 Poison ivy berries` — OK (2 items, `herblore`).

### Tree drops + farming materials

- `6020-6030 Leaves/Oak/Willow/Yew/Maple/Magic leaves` — **ADD farming** (6 items, tree leaves drops).
- `6032 Compost`, `6034 Supercompost`, `6036 Plant cure` — OK (`farming`).
- `6038 Magic string` — OK (`wc_fletching`).
- `6040 Amulet of nature`, `6041 Pre-nature amulet` — **ADD farming** (2 items, farming patch notification).
- `6043-6053 Tree roots (Oak/Willow/Maple/Yew/Magic/Spirit)` — **ADD farming** (6 items, tree root drops).
- `6055 Weeds`, `6057 Hay sack`, `6059 Scarecrow` — **ADD farming** (3 items).

### Mourner set (Mourning's End II disguise)

- `6064-6070 Bloody mourner top, Mourner top/trousers/gloves/boots/cloak + Ripped mourner trousers` — **REM melee** for gloves/boots. **ADD quests cosmetic** (or EX). Mourning's End disguise.
- `6068 Mourner gloves`, `6069 Mourner boots` — currently in melee, REM.

### Remaining Mourning's End / Cabin Fever quest items

- `6071-6100` (~30 items: Mourner letter, Tegid's soap, Prifddinas' history, Eastern discovery, Eastern settlement, The great divide, Broken device, Fixed device, Tarnished/Worn key, Red/Blue/Yellow/Green dye bellows ×8, Blue/Red/Yellow/Green toad ×8, Rotten apples, Apple barrel, Naphtha apple mix, Toxic naphtha, Sieve, Toxic powder, Teleport crystal (3)) — EX.

---

## Session 21 totals

- Items reviewed: 116
- OK (correct as-is): 25
- EX (correctly excluded): 35
- ADD (missing tab, fixed): 54 (Beer (4)/(m4) variants ~14, fruits/farming materials ~30, herblore unfinished, tree drops, etc.)
- REM (misclassified, fixed): 2 (Mourner gloves/boots from melee)
- LOG (deferred): 0

## Classifier changes made in session 21

See `audit/classifier-changes.md` "Session 21".

---

## Session 22: IDs 6101–6400

### Teleport crystals + Ghostly robes

- `6100-6102 Teleport crystal (3/2/1)` — **ADD misc** (3 items, Roving Elves elf teleport).
- `6103 Crystal teleport seed` — OK (`farming`).
- `6106-6111 Ghostly boots/robe (body)/robe (legs)/hood/gloves/cloak` — OK except `6108 Ghostly robe` (legs slot) currently unclassified. **ADD mage** to legs variant via force_include.

### Kelda brewing chain (Eadgar's Ruse follow-on)

- `6112 Kelda seed` — OK (`farming`).
- `6113 Kelda hops` — **ADD farming**.
- `6118 Kelda stout` — **ADD cooking** (brewing output).
- `6119-6126` (Square stone, Letter, A chair, Beer glass dup, Enchanted lyre(2)/(3) charge variants) — EX/LOG.

### Dagannoth Kings armour drops

- `6128-6130 Rock-shell helm/plate/legs` — OK (melee).
- `6131 Spined helm` — **REM melee + mage**, **ADD range**. Spined is RANGE armour.
- `6133 Spined body`, `6135 Spined chaps` — **REM melee**, **ADD range** (2 items).
- `6137-6141 Skeletal helm/top/bottoms` — **REM melee** (3 items). Skeletal is MAGE armour; currently melee;mage. Remove melee.
- `6143 Spined boots` — **REM melee**, **ADD range**.
- `6145 Rock-shell boots` — OK (melee).
- `6147 Skeletal boots` — **REM melee**.
- `6149 Spined gloves` — **REM melee**, **ADD range**.
- `6151 Rock-shell gloves` — OK (melee).
- `6153 Skeletal gloves` — **REM melee**.

### Crafting materials (Dagannoth)

- `6155 Dagannoth hide` — OK (`crafting`).
- `6157-6161 Rock-shell chunk/shard/splinter` — **ADD crafting** (3 items, crafting recipe inputs).
- `6163-6167 Skull/Ribcage/Fibula piece` — **ADD crafting** (3 items, Skeletal armour materials).
- `6169-6173 Circular/Flattened/Stretched hide` — OK (`crafting`).

### Cooking / Hunter

- `6178 Raw pheasant` — OK (`cooking`).

### Easter holiday cosmetics

- `6180-6182 Lederhosen top/shorts/hat` — **ADD misc** (3 items, Easter event).
- `6183 Frog token` — **ADD misc**.
- `6184-6188 Royal frog tunic/leggings/blouse/skirt + Frog mask` — **ADD misc** (5 items, Easter event cosmetic).
- `6189 Hex edit detected` — EX (debug item).
- `6199 Mystery box` — EX.

### Fishing Trawler

- `6200 Raw fishlike thing` — OK (`cooking`). Wait — actually it shows cooking. But it should also be fishing. **ADD fishing**.
- `6202 Fishlike thing` — **ADD cooking + fishing**.

### Pyre logs (Mort'ton)

- `6209 Small fishing net` — LOG (canonical dup of 303).
- `6211 Teak pyre logs`, `6213 Mahogany pyre logs` — OK (`wc_fletching;firemaking`).

### Broodoo shields (Tai Bwo Wannai)

- `6219-6279 Broodoo shield + charge variants ×3 set colours` (~24 items) — Most in melee;mage from defence stats. Multiple dups via canonical filter. LOG.

### Tai Bwo Wannai materials

- `6281-6285 Thatch spar light/med/dense` — **ADD construction** (3 items, Karamja repair material).
- `6287 Snake hide`, `6289 Snakeskin` — OK (`crafting`).

### Spider / hunter Tai Bwo Wannai

- `6291-6299 Spider carcass / Spider on stick ×2 / Spider on shaft ×2` — EX (Big Chompy hunting variants).
- `6301 Burnt spider` — OK (`cooking`).
- `6303 Spider on shaft` (dup), `6305 Skewer stick` — EX/LOG.
- `6306 Trading sticks` — **ADD misc Currency** (Karamja currency).
- `6311 Gout tuber` — **ADD farming** (rare farming seed).
- `6313-6317 Opal/Jade/Red topaz machete` — EX (correctly excluded from melee by noise filter; not real melee).
- `6319 Proboscis` — EX.

### Snakeskin armour (range)

- `6322 Snakeskin body` — OK (`range`).
- `6324 Snakeskin chaps` — **REM melee** (currently melee;range).
- `6326 Snakeskin bandana` — **REM melee, ADD range**.
- `6328 Snakeskin boots` — **REM melee, ADD range**.
- `6330 Snakeskin vambraces` — **REM melee, ADD range**.

### Hardwood logs

- `6332 Mahogany logs`, `6333 Teak logs` — OK (`wc_fletching;firemaking`).

### Tai Bwo Wannai tribal/villager cosmetic (6335-6379)

- `6335-6379 Tribal masks ×3 + Tribal tops + Villager robes/hats/sandals/armbands ×~4 variants` (~40 items) — EX. Tai Bwo Wannai cosmetic costumes.

### Desert / Menaphite cosmetic

- `6382-6400 Fez, Desert top/robes/legs, Menaphite purple hat/top/robe/kilt + Menaphite red hat` (~10 items) — EX. Desert cosmetic gear from Sorceress's Garden / Contact!.

---

## Session 22 totals

- Items reviewed: 144
- OK (correct as-is): 35
- EX (correctly excluded): 75 (Tai Bwo Wannai tribal/villager cosmetics, Menaphite desert, spider/skewer hunting intermediates).
- ADD (missing tab, fixed): 28 (Teleport crystals, Kelda hops/stout, Ghostly legs, Spined/Snakeskin range pieces, Skeletal mage pieces tag-cleanup, crafting materials, Lederhosen + Frog event, Trading sticks, Gout tuber, Thatch spars).
- REM (misclassified, fixed): 16 (Spined ×5 + Skeletal ×4 from melee, Snakeskin chaps/bandana/boots/vambraces from melee, Broodoo dups).
- LOG (deferred): 10+ (Broodoo dups, canonical dedup).

## Classifier changes made in session 22

See `audit/classifier-changes.md` "Session 22".

---

## Session 23: IDs 6401–6700

### Menaphite continuing + Blackjacks

- `6402-6406 Menaphite red top/robe/kilt` — EX (3 items).
- `6408-6420 Oak/Willow/Maple blackjack + (o)/(d) variants` (~9 items) — REM melee. Currently (o) variants cross-tag melee + agility_thieving; should be agility_thieving only. Plain Maple blackjack already correct. **Fix**: force_exclude blackjack pattern from melee Weapons.

### Garden of Tranquillity

- `6448 Spadeful of coke` — EX.
- `6453-6460 White/Red/Pink rose seed, Vine, Delphinium, Orchid (×2), Snowdrop seed` — OK (8 items, `farming`).
- `6461 White tree shoot` — **ADD farming**.
- `6464 White tree sapling` — OK (`farming`).
- `6466-6469 Rune shards, Rune dust, Plant cure dup, White tree fruit` — EX (last 3 are quest variants); **ADD farming** for White tree fruit if needed.

### Compost potion (cross-tag)

- `6470-6476 Compost potion(4..1)` — OK (4 items, `herblore;farming`).
- `6478 Trolley`, `6479 List` — EX.

### TzHaar weapons

- `6522 Toktz-xil-ul` — OK (`range`). Obsidian rings (thrown).
- `6523 Toktz-xil-ak` — OK (`melee`). Obsidian short sword.
- `6524 Toktz-ket-xil` — OK (`range`). Obsidian shield.
- `6525 Toktz-xil-ek` — OK (`melee`). Obsidian dagger.
- `6526 Toktz-mej-tal` — OK (`mage`). Obsidian staff.
- `6527 Tzhaar-ket-em`, `6528 Tzhaar-ket-om` — OK (`melee`).
- `6529 Tokkul` — OK (`misc`).

### Recipe for Disaster + holiday

- `6541-6556` (Mouse toy, Present, Antique lamp dup, Catspeak amulet(e), Chores, Recipe, Doctor's hat, Nurse hat, Lazy cat, Wily cat) — EX (~10 items).

### Wanted! / Dwarven items

- `6561 Ahab's beer` — **ADD cooking**.
- `6562 Mud battlestaff` — OK (`mage;crafting`).
- `6563 Mystic mud staff` — OK (`mage`).

### Obsidian/Fire capes

- `6568 Obsidian cape` — OK (`melee;range;mage`).
- `6570 Fire cape` — OK (`melee;range;mage`).

### Onyx

- `6571 Uncut onyx` — OK (`crafting;mining_smithing`).
- `6573 Onyx`, `6575 Onyx ring`, `6577 Onyx necklace`, `6579 Onyx amulet (u)` (in wc_fletching ✓), `6581 Onyx amulet` — OK (5 items).
- `6583 Ring of stone` — EX. Quest item.
- `6585 Amulet of fury` — OK (`melee;range;mage`).

### White (Wanted!) armour set

- `6587-6633 White claws / White battleaxe / White dagger / White halberd / White mace / White magic staff / White sword / White longsword / White 2h sword / White scimitar / White warhammer / White chainbody / White platebody / White boots / White med helm / White full helm / White platelegs / White plateskirt / White gloves / White sq shield / White kiteshield` — OK (~21 items, `melee;mining_smithing` for body parts, `melee` for weapons, `mage` for the magic staff).

### Sorceress's Garden / Mosquito hunting

- `6635-6653` (~18 items: Commorb, Solus's hat, Colour wheel, Hand mirror, Red/Yellow/Green/Cyan/Blue/Magenta/Fractured/Blackened/Newly made crystal, Item list, Edern's journal, Crystal trinket) — EX.

### Camo outfit (Hunter)

- `6654-6656 Camo top/bottoms/helmet` — **ADD hunter** (3 items, Hunter camo).
- `6657-6659 Camo top/bottoms/helmet` dups — LOG (canonical dedupe).

### Aerial Fishing / Fishing Trawler

- `6662-6664 Broken fishing rod, Forlorn boot, Fishing explosive` — EX.
- `6665 Mudskipper hat` — LOG. Currently unclassified, range/quest cosmetic.
- `6666 Flippers` — REM melee. Cosmetic feet, not real combat gear.
- `6667 Empty fishbowl` — OK (`crafting`).
- `6670-6675` (Fishbowl, Fishbowl and net, Tiny net, Empty fish food box) — EX (4 items).

### Spirits of the Elid

- `6677-6680 Guam in a box / Seaweed in a box ×2 dups` — EX.
- `6681 Ground guam` — **ADD herblore** (secondary).
- `6683 Ground seaweed` — **ADD herblore** (secondary).

### Saradomin brew + Slayer

- `6685-6691 Saradomin brew(4..1)` — OK (4 items, `melee;prayer;herblore`).
- `6693 Crushed nest` — OK (`herblore`).
- `6696 Ice cooler` — **ADD slayer** (Ice giant slayer task tool).
- `6697 Pat of butter` — OK (`cooking`).
- `6699 Burnt potato` — OK (`cooking`).

---

## Session 23 totals

- Items reviewed: 129
- OK (correct as-is): 50
- EX (correctly excluded): 50
- ADD (missing tab, fixed): 17 (White tree shoot/fruit farming, Ahab's beer cooking, Camo outfit ×3 hunter, Ground guam/seaweed herblore, Ice cooler slayer)
- REM (misclassified, fixed): 4 (3 Oak/Willow/Maple blackjack(o) variants from melee, Flippers from melee)
- LOG (deferred): 5 (Camo dups, Mudskipper hat)

## Classifier changes made in session 23

See `audit/classifier-changes.md` "Session 23".

---

## Session 24: IDs 6701–7000

### Baked potato family

- `6701 Baked potato`, `6703 Potato with butter`, `6705 Potato with cheese` — **ADD cooking** (3 items).

### Enakhra's Lament / Camel + quest items

- `6707 Camulet` — EX (quest amulet).
- `6710 Blindweed seed` — OK (`farming`).
- `6711-6722` (Blindweed, Bucket of water dup, Wrench, Holy wrench ✓, Sluglings, Karamthulhu ×2, Fever spider body, Unsanitary swill, Slayer gloves, Rusty scimitar, Zombie head) — partial OK, partial EX.
- `6720 Slayer gloves` — **ADD slayer**.

### Seercull + DKS rings

- `6724 Seercull` — OK (`range;wc_fletching`).
- `6728 Dagannoth-king bonemeal`, `6729 Dagannoth bones` — OK (`prayer`).
- `6731 Seers ring` — OK (`mage`).
- `6733 Archers ring` — OK (`range`).
- `6735 Warrior ring`, `6737 Berserker ring` — OK (`melee`).

### Forestry / dwarf-axe content

- `6739 Dragon axe` — OK (`wc_fletching`).
- `6741 Broken axe`, `6743 Dragon axe head` — EX.

### Devious Minds + Shadow of the Storm

- `6746 Darklight` — OK (`melee`).
- `6747-6754 Demonic sigil mould/sigil/tome (mage ✓), Black desert shirt/robe, Enchanted key` — partial.
- `6749 Demonic tome` — OK (`mage`).

### Lunar Diplomacy / Mjolnir

- `6760-6764 Guthix/Saradomin/Zamorak mjolnir` — OK (3 items, `mage`).

### RFD Awowogei / Rat Catchers

- `6766-6772` (Cat antipoison, Book, Poisoned cheese, Music scroll, Directions, Pot of weeds, Smouldering pot) — EX.
- `6773 Rat pole` — REM melee. RFD weapon (Rat-Catchers quest), cosmetic only. **ADD quests**.

### Various quest items (Tourist Trap follow-on, Elidinis)

- `6785-6791` (Statuette, Robe of elidinis ×2, Torn robe ×2, Shoes, Sole) — EX.
- `6792 Ancestral key`, `6793 Ballad` — EX.

### Choc-ice + Champion Challenge scrolls

- `6794 Choc-ice` — OK (`cooking`).
- `6796 Lamp` — EX.
- `6798-6808 Champion scrolls ×11` (Earth warrior/Ghoul/Giant/Goblin/Hobgoblin/Imp/Jogre/Lesser demon/Skeleton/Zombie/Leon's) — **ADD slayer** (Champion's Challenge collection — slayer-flavored).

### Granite legs

- `6809 Granite legs` — **REM range, ADD melee**. Granite is a melee armour set; legs were misclassified into range via stat dominance.

### Wyvern bones

- `6810 Wyvern bonemeal`, `6812 Wyvern bones` — OK (`prayer`).

### Misc + Christmas baubles

- `6814 Fur`, `6817-6821` (Slender blade, Bow-sword, Large pouch dup, Relic, Orb) — EX.
- `6822-6853 Star/Box/Diamond/Tree/Bell bauble ×5 + Puppet box + Bauble box` — **ADD misc Holiday rares** (~7 items). Christmas event.
- `6856-6863 Bobble hat/scarf, Jester hat/scarf, Tri-jester hat/scarf, Woolly hat/scarf` (8 items) — **ADD misc Holiday rares**.
- `6864-6882 Marionette handle + Blue/Green/Red marionette × many dups` — **ADD misc Holiday rares** (canonical) + LOG (dups).
- `6883 Peach` — **ADD cooking**.

### MTA (Mage Training Arena) items

- `6885 Progress hat` — EX.
- `6889 Mage's book` — OK (`mage`).
- `6891-6903` (Arena book, Leather boots dup, Adamant kiteshield dup, Adamant med helm dup, Emerald dup, Rune longsword dup, Cylinder, Cube, Icosahedron, Pentamid, Orb, Dragonstone dup) — partial. The geometric shapes are MTA items. **ADD mage** for Cylinder/Cube/Icosahedron/Pentamid/Orb (when context is MTA).
- `6904-6906 Animals' bones ×3` — canonical OK, dups LOG.

### Wands + Infinity robes

- `6908-6914 Beginner/Apprentice/Teacher/Master wand` — OK (4 items, `mage`).
- `6916-6924 Infinity top/hat/boots/gloves/bottoms` — OK (5 items, `mage`).

### Rat Catchers (more)

- `6945-6957` (~13 items: Sandy hand, Beer soaked hand, Bert's/Sandy's rota, A magic scroll, Magical orb, Truth serum, Bottled water, Redberry juice, Pink dye ✓, Rose-tinted lens, Wizard's head) — EX.
- `6958 Sand` — OK (`crafting`).
- `6959 Pink cape` — EX (cosmetic).

### Mahjarrat Memories + Pyramid Plunder + Senntisten

- `6961-7000` (~30 items: Baguette, Triangle sandwich, Roll, Coins dup, Square sandwich, Prison key, Dragon med helm dup, Shark dup, Pyramid top, Sandstone variants ×4, Sandstone body/base, Stone head/arms/legs, Z/M/R/K sigil) — most EX.
- `6971 Sandstone (1kg)`, `6979 Granite (500g)` — OK (`mining_smithing`).
- `6985-6986 Sandstone (20kg)/(32kg)` — **ADD mining_smithing**.

---

## Session 24 totals

- Items reviewed: 180
- OK (correct as-is): 55
- EX (correctly excluded): 90 (heavy quest content, RFD subquests, Sandstone variants, Senntisten sigils).
- ADD (missing tab, fixed): 35 (Baked potato ×3, Slayer gloves, Champion scrolls ×11, Christmas baubles + bobble hats + marionettes ~20, MTA items, Peach, Sandstone variants)
- REM (misclassified, fixed): 2 (Rat pole from melee, Granite legs from range)
- LOG (deferred): 15+ (marionette colour dups, sandstone body/base dups)

## Classifier changes made in session 24

See `audit/classifier-changes.md` "Session 24".

---

## Session 25: IDs 7001–7300

### Camel + Bug lantern

- `7001-7003 Camel mould (p), Stone head, Camel mask` — EX.
- `7004 Chisel` — LOG (canonical dup).
- `7053 Lit bug lantern` — OK (`firemaking`).

### Potato variants (Cooks Assistant 2 / Hosidius)

- `7054-7060 Chilli/Egg/Mushroom/Tuna potato` — **ADD cooking** (4 items).

### Gnome Cooking continued

- `7062-7088` (Chilli con carne, Egg and tomato, Mushroom & onion, Tuna and corn, Minced meat, Spicy sauce, Chopped garlic, Uncooked egg, Scrambled egg, Sliced mushrooms, Fried mushrooms, Fried onions, Chopped tuna, Sweetcorn dup) — **ADD cooking** (~14 items, gnome cooking recipe chain).
- `7090-7094 Burnt egg/onion/mushroom` — OK (3 items, `cooking`).

### Cabin Fever / Pirate's Treasure

- `7108-7156` (~40 items: Gunpowder, Fuse, Stripy pirate shirt/bandana/boots/leggings × 4 colour variants, Canister, Cannon ball, Ramrod, Repair plank, Lucky cutlass, Harry's cutlass, Rapier ✓ for the cutlasses, Plunder, Book o' piracy, Cannon barrel, Broken cannon, Cannon balls, Tacks, Rope, Tinderbox dup) — mostly EX.
- `7140-7142 Lucky cutlass / Harry's cutlass / Rapier` — OK (3 items, `melee`).
- `7157 Braindeath 'rum'` — **ADD cooking** (pirate beverage).

### Dragon 2h + Insulated boots

- `7158 Dragon 2h sword` — OK (`melee`).
- `7159 Insulated boots` — LOG (Heroes' Quest cosmetic, currently in melee;range).

### Pie chain (multiple flavours)

- `7162 Pie recipe book` — EX.
- `7164-7218 Part/Raw/finished + Mud/Garden/Fish/Admiral/Wild/Summer pie` (~30 items): mostly cooking ✓ with some non-canonical dups (Part X dups not classified). Cooking canonical fine.
- `7170 Mud pie` — OK (`range;cooking`). Used as throwable in Burgh de Rott.

### Roast / Skewered food (Big Chompy + RFD)

- `7222 Burnt rabbit` — OK (`cooking`).
- `7223 Roast rabbit`, `7224 Skewered rabbit` — **ADD cooking** (2 items).
- `7225 Iron spit` — **ADD cooking** (rabbit skewering tool).
- `7226 Burnt chompy` — OK (`cooking`).
- `7228 Cooked chompy` — LOG (canonical dup of 2878).
- `7230 Skewered chompy` — **ADD cooking**.

### Clue scrolls (canonical dedupe)

- `7236-7300 Clue scroll (easy/hard/medium) × ~30 dups` — LOG.

---

## Session 25 totals

- Items reviewed: 131
- OK (correct as-is): 25
- EX (correctly excluded): 60 (Camel quest, Cabin Fever pirate quest, pie recipe books, etc.)
- ADD (missing tab, fixed): 22 (4 potato variants, ~14 gnome cooking, Roast/Skewered rabbit, Iron spit, Skewered chompy, Braindeath rum)
- REM (misclassified, fixed): 0
- LOG (deferred): 50+ (clue scroll canonical dups, pie part dups, pirate cosmetic dups, Insulated boots)

## Classifier changes made in session 25

See `audit/classifier-changes.md` "Session 25".

---

## Session 26: IDs 7301–7600

### Clue scroll medium dups + Boater hats

- `7301-7317 Clue scroll (medium) × ~10 dups` — LOG.
- `7319-7327 Red/Orange/Green/Blue/Black boater` (5 items) — LOG (Treasure Trails cosmetic; head slot but equipable=0; not in misc fashion section).

### Firelighters + Heraldic shields

- `7329-7331 Red/Green/Blue firelighter` — OK (3 items, `firemaking`).
- `7332-7360 Black/Adamant/Rune shield (h1-h5)` (15 items) — OK (`melee`).

### Trim/gilded cosmetic armour (Treasure Trails)

- `7362-7368 Studded body/chaps (g/t)` — **ADD range** (4 items). Equipable=0 in data, but visual variants of the standard studded body/chaps range armour.
- `7370-7384 Green/Blue d'hide body/chaps (g/t)` — OK (`range` via d'hide regex).
- `7386-7388 Blue skirt (g/t)` — **ADD mage** (2 items).
- `7390-7396 Blue wizard robe/hat (g/t)` — **ADD mage** (4 items).
- `7398-7400 Enchanted robe/top/hat` — OK (3 items, `mage`).

### Mole / Falador subquest

- `7404-7406 Red/Green/Blue logs` — OK (3 items, `wc_fletching;firemaking`).
- `7408 Draynor skull` — EX.
- `7409 Magic secateurs` — OK (`melee;slayer;farming`).
- `7410 Queen's secateurs` — **ADD farming**. Max-harvest farming tool.
- `7411-7418 Symptoms list, Bird nest, Paddle, Mole claw, Mole skin` — EX.

### Mort'ton (Shade of) — Fungicide

- `7421 Fungicide spray 10` — OK (`slayer`).
- `7423-7430 Fungicide spray (1-8) variants` — LOG (charge-dose dups).
- `7432 Fungicide` — OK (`slayer`).

### Joke weapons (Mort'ton)

- `7433-7451 Wooden spoon, Egg whisk, Spork, Spatula, Frying pan, Skewer, Rolling pin, Kitchen knife, Meat tenderiser, Cleaver` — OK (10 items, `melee`).

### Standard gloves (RFD)

- `7453-7462 Hardleather/Bronze/Iron/Steel/Black/Mithril/Adamant/Rune/Dragon/Barrows gloves` — OK (10 items, `melee;range;mage`).

### RFD Evil Dave / Pyrefiend cooking

- `7463-7472 Cornflour, Book on chickens, Vanilla pod, Pot of cornflour, Cornflour mixture, Milky mixture, Cinnamon` — EX.
- `7473-7476 Brulee × 3 + Brulee supreme` — **ADD cooking** (4 items, RFD dessert recipe).
- `7477 Evil chicken's egg`, `7478 Dragon token` — EX.
- `7479 Spicy stew` — OK (`cooking`).
- `7480-7495 Red/Orange/Brown/Yellow spice (1-4)` — **ADD cooking** (16 items, Spicy stew flavouring).
- `7496-7498 Empty spice shaker, Dirty blast, Antique lamp` — EX.

### Forgettable Tale + Fremennik isles

- `7508-7515 Asgoldian ale, Dwarven rock cake, Slop of compromise, Soggy bread, Spicy maggots, Dyed orange, Breadcrumbs` — EX (Forgettable Tale quest, mostly non-edible cosmetic).

### Sea Slug / RFD Pirate Pete (crab + fishcake)

- `7516-7519 Kelp, Ground kelp, Giant crab meat, Crab meat` — EX (raw materials, intermediate items).
- `7520 Burnt giant crab meat`, `7521 Cooked giant crab meat` — OK (2 items, `cooking`).
- `7527 Ground giant crab meat`, `7528 Ground cod` — **ADD cooking** (2 items, fishcake ingredients).
- `7529 Raw fishcake`, `7530 Cooked fishcake`, `7531 Burnt fishcake` — OK (3 items, `cooking`).
- `7532 Mudskipper hide` — OK (`crafting`).
- `7533-7536 Rock, Fishbowl helmet, Diving apparatus, Fresh crab claw` — EX (Pirate Pete cosmetic).
- `7537 Crab claw` — OK (`melee`).
- `7538-7541 Fresh crab shell, Broken crab claw, Broken crab shell` — EX.
- `7539 Crab helmet` — OK (`melee`).

### RFD Sir Amik Varze (Cake of guidance)

- `7542 Cake of guidance` — EX (quest item, single-use).
- `7543 Raw guide cake` — OK (`cooking`).
- `7544-7546 Enchanted egg, Enchanted milk, Enchanted flour` — **ADD cooking** (3 items, cake of guidance ingredients).

### RFD Awowogei (monkey subquest)

- `7564 Balloon toad` — EX (quest tool).
- `7566 Raw jubbly`, `7568 Cooked jubbly`, `7570 Burnt jubbly` — OK (3 items, `cooking`).
- `7572 Red banana`, `7573 Tchiki monkey nuts`, `7574 Sliced red banana`, `7575 Tchiki nut paste` — **ADD cooking** (4 items, Awowogei recipe ingredients).
- `7576 Snake corpse` — EX (raw material).
- `7577 Raw stuffed snake` — OK (`cooking`).
- `7578 Odd stuffed snake` — EX (wrong-recipe version).
- `7579 Stuffed snake`, `7580 Snake over-cooked` — **ADD cooking** (2 items, Awowogei final dish).

### Hell-kitten + Mort'ton coffins

- `7583 Hell-kitten` — EX (quest pet, not in standard cooking pets).
- `7587-7591 Coffin variants` — EX (Shades of Mort'ton).

### Halloween 2009 zombie outfit

- `7592-7596 Zombie shirt, trousers, mask, gloves, boots` — **ADD misc Holiday rares** (5 items, Halloween event cosmetic).

---

## Session 26 totals

- Items reviewed: ~150
- OK (correct as-is): 56
- EX (correctly excluded): 50 (Mole quest, Mort'ton joke quest, RFD intermediate ingredients)
- ADD (missing tab, fixed): 40 (Studded/Blue trim variants ×6, Queen's secateurs, Brulee ×4, Spice variants ×16, Crab meat ground ×2, Enchanted ×3, Awowogei recipe ×6, Zombie outfit ×5)
- REM (misclassified, fixed): 0
- LOG (deferred): 20+ (clue scroll medium dups, boater hat cosmetics, fungicide spray charge variants)

## Classifier changes made in session 26

See `audit/classifier-changes.md` "Session 26".

---

## Session 27: IDs 7601–7900

### Shades of Mort'ton / Darkness of Hallowvale (early)

- `7622-7635 Bucket of rubble, Plaster fragment, Dusty scroll, Crate, Temple library key, The sleeping seven, Histories of the hallowland, Modern day morytania` — EX (Hallowvale lore/quest).

### Rod of ivandis

- `7636-7637 Rod dust, Silvthrill rod` — EX (intermediate quest material).
- `7639-7648 Rod of ivandis (1-10)` — OK (8 items, `mage`).
- `7649-7650 Rod mould, Silver dust` — EX.

### Guthix balance potions (Darkness of Hallowvale)

- `7654-7666 Guthix balance (unf) × 3, Guthix balance (4-1)` — LOG (vyrewatch poison; charge-dose dups; classifier should pick up as herblore — see below).

### Joke weapons (Stronghold of Security)

- `7668 Gadderhammer` — OK (`melee`).
- `7671 Boxing gloves` — LOG (Stronghold training cosmetic; equipable=0).
- `7675 Wooden sword` — OK (`melee`).
- `7676 Wooden shield` — LOG (Stronghold training; equipable=0).

### Stronghold of Security rewards + misc

- `7677-7686 Treasure stone, Prize key, Pugel, Game book, Hoop, Dart, Bow and arrow` — mostly LOG (cosmetic prize items).
- `7686 Bow and arrow` — OK (`wc_fletching` from cosmetic catch).

### Tea ceremony chain (Penguin Hide and Seek / various)

- `7688 Kettle` — **ADD cooking** (tea-making tool).
- `7690 Full kettle`, `7691 Hot kettle` — **ADD cooking** (2 items).
- `7692-7716 Pot of tea (4) × 4 dups` — LOG (only `Pot of tea (4)` canonical needs cooking; non-canonical dups left as-is).
- `7700 Teapot with leaves`, `7702 Teapot` — **ADD cooking** (2 items).
- `7728 Empty cup` (dup of 1980), `7732/7735 Porcelain cup` (dups of 4244) — LOG (canonical already in cooking).
- `7730/7733/7736 Cup of tea` — already in COOKING Beverages allowlist; LOG dups.
- `7738 Tea leaves` — **ADD cooking** (1 item).

### Beer collection (Brewery / dups of canonical brews)

- `7740-7754 Beer/Asgarnian ale/Greenman's ale/Dragon bitter/Moonlight mead/Cider/Chef's delight` — LOG (~8 dups of the canonical brews already in COOKING Beverages).

### Toy + reward + vine items (Tower of Life)

- `7756-7778 Paintbrush, Rusty sword, Toy soldier/doll/mouse/cat, Branch, Reward token, Long/Short vine` — EX (Tower of Life quest cosmetic).

### Tomes (Treasure Trails Hard reward)

- `7779-7797 Fishing/Agility/Thieving/Slayer/Mining/Firemaking/Woodcutting tome` — OK (7 items, `mage` via tome regex).

### Animal Magnetism / Strange creature

- `7800-7804 Snail shell, Snake hide, Yin yang amulet, Ancient mjolnir` — EX (quest items).
- `7806-7809 Anger sword, Anger battleaxe, Anger mace, Anger spear` — OK (4 items, `melee`; Recipe for Disaster final boss reward weapons).
- `7810-7818 Jug of vinegar, Pot of vinegar, Goblin skull, Bone in vinegar, Bear ribs, Ram skull` — EX (Lunar Diplomacy / Animal Magnetism quest items, not buryable).

### Buryable bone collection (canonical + quest variants)

- `7821-7896 various-bone/bone-ribs/wing items` — mixed classification:
  - Prayer-classified (`Unicorn bone, Giant rat bone, Wolf bone, Rat bone, Baby dragon bone, Jogre/Zogre/Mogre bone, Zombie bone, Werewolf bone, Moss giant/Fire giant bone, Ghoul bone, Troll bone, Experiment bone, Rabbit bone, Basilisk bone`) — OK (~18 items, `prayer`).
  - Unclassified (`Giant bat wing, Bat wing, Ogre ribs, Monkey paw, Dagannoth ribs, Snake spine, Ice giant ribs, Terrorbird wing, Seagull wing, Undead cow ribs`) — LOG (~10 items; quest-only, not buryable for XP).
- `7899 Basilisk bone` — OK (`prayer`).

---

## Session 27 totals

- Items reviewed: ~120
- OK (correct as-is): 50 (Rod of ivandis, tomes, anger weapons, prayer bones, etc.)
- EX (correctly excluded): 35 (Hallowvale lore, Tower of Life cosmetic, quest material)
- ADD (missing tab, fixed): 6 (Kettle/Full/Hot kettle, Teapot/Teapot with leaves, Tea leaves)
- REM (misclassified, fixed): 0
- LOG (deferred): 30+ (Stronghold cosmetics, beer dups, non-buryable quest bones, Pot of tea charge variants)

## Classifier changes made in session 27

See `audit/classifier-changes.md` "Session 27".

---

## Session 28: IDs 7901–8200 (effective range 7902–8022; ID gap 8023–8416)

### Quest/random event bones + wings

- `7902 Desert lizard bone`, `7914 Jackal bone` — OK (2 items, `prayer`).
- `7905 Cave goblin skull`, `7908 Big frog leg`, `7911 Vulture wing` — EX (quest items, non-buryable).

### Slayer cosmetic helmets

- `7917 Ram skull helm` — OK (`melee`).
- `7918 Bonesack` — OK (`melee;range;mage` cape). Hard clue cosmetic.

### Random event + Easter event

- `7919 Bottle of wine`, `7921 Empty wine bottle` — EX.
- `7922 Al kharid flyer` — EX.
- `7927 Easter ring`, `7928 Easter egg` — **ADD misc Holiday rares** (2 items).

### Field rations + runecraft fragments

- `7934 Field ration` — EX.
- `7936 Pure essence` — OK (`mage;runecraft`).
- `7938 Dark essence fragments` — **ADD runecraft** (Blood/Dark altar intermediate).
- `7939 Tortoise shell`, `7941 Iron sheet` — EX.

### Monkfish chain (Swan Song)

- `7942 Fresh monkfish` — LOG (intermediate non-canonical).
- `7944 Raw monkfish` — OK (`cooking;fishing`).
- `7946 Monkfish` — OK (`melee;range;mage;cooking`).
- `7948 Burnt monkfish` — OK (`cooking`).
- `7950 Bone seeds`, `7951 Herman's book` — EX.
- `7954 Burnt shrimp` — OK (`cooking`).

### Eadgar's Ruse / Tower of Life misc

- `7956-7974 Casket, White apron, Mining prop, Heavy/Empty box, Burnt diary, Letter, Engine, Scroll, Pulley beam, Long/Longer pulley beam, Lift manual, Beam, Servant bell` — EX.

### Slayer monster heads (mounted trophies for POH)

- `7975-7981 Crawling hand, Cockatrice head, Basilisk head, Kurask head, Abyssal head, Kbd heads, Kq head` — **ADD slayer** (7 items).
- `7982-7988 Stuffed crawling hand, Stuffed cockatrice head, Stuffed basilisk head, Stuffed kurask head, Stuffed abyssal head, Stuffed kbd heads, Stuffed kq head` — **ADD slayer** (7 items, POH-mountable variants).

### Fishing trophies (POH mounted big fish)

- `7989-7994 Big bass, Stuffed big bass, Big swordfish, Stuffed big swordfish, Big shark, Stuffed big shark` — **ADD fishing** (6 items).

### POH portraits and paintings

- `7995-7998 Arthur portrait, Elena portrait, Keldagrim portrait, Misc. portrait` — **ADD construction** (4 items, hangable in POH).
- `7999-8003 Desert/Isafdar/Karamja/Lumbridge/Morytania painting` — **ADD construction** (5 items).

### Clue scroll maps

- `8004-8006 Small/Medium/Large map` — LOG (clue scroll material; not equipment).

### Teleport tabs (POH spellbook)

- `8007-8012 Varrock/Lumbridge/Falador/Camelot/Ardougne/Watchtower teleport` — OK (6 items, `misc/Teleport tabs`).
- `8013 Teleport to house` — OK (`construction;misc`).

### Magic spell tablets

- `8014-8021 Bones to bananas, Bones to peaches, Enchant sapphire or opal, Enchant emerald or jade, Enchant ruby or topaz, Enchant diamond, Enchant dragonstone, Enchant onyx` — **ADD mage** (8 items, spell tablets crafted at POH lectern).
- `8022 Telekinetic grab` — **ADD mage** (1 item, spell tablet).

---

## Session 28 totals

- Items reviewed: ~50 (range had ID gap 8023–8416)
- OK (correct as-is): 17
- EX (correctly excluded): 20 (random event, quest material)
- ADD (missing tab, fixed): 41 (2 Easter, 1 runecraft fragment, 14 slayer heads, 6 fishing trophies, 9 POH portraits/paintings, 9 magic tablets)
- REM: 0
- LOG: ~5 (clue maps, intermediate non-canonical)

## Classifier changes made in session 28

See `audit/classifier-changes.md` "Session 28".

---

## Session 29: IDs 8417–8716 (POH construction dump)

### Bagged plants & trees (POH garden)

- `8417-8429 Bagged dead/nice/oak/willow/maple/yew/magic tree` (7 items) — **ADD construction**.
- `8431-8461 Bagged plant 1/2/3, Bagged flower/daffodils/bluebells/sunflower/marigolds/roses` (9 items) — **ADD construction**.

### Hedges (POH garden)

- `8437-8449 Thorny/Nice/Small box/Topiary/Fancy/Tall fancy/Tall box hedge` (7 items) — **ADD construction**.

### Construction guide

- `8463 Construction guide` — **ADD construction**.

### Heraldic helm + kiteshield dups

- `8464 Rune heraldic helm` (canonical) — OK (`melee`).
- `8466-8494 Rune heraldic helm × 15 dups` — LOG.
- `8682 Steel heraldic helm` (canonical) — OK (`melee`).
- `8684-8712 Steel heraldic helm × 15 dups` — LOG.
- `8714-8716 Rune kiteshield dups` — LOG.

### Chairs, benches, beds (POH dining/bedroom)

- `8496 Crude chair`, `8498 Wooden chair`, `8500 Rocking chair` — **ADD construction** (3).
- `8502-8508 Oak/Teak/Mahogany armchair` — OK (`construction`; matched by existing pattern).
- `8510 Bookcase` — **ADD construction**.
- `8512-8514 Oak/Mahogany bookcase` — OK (`construction`).
- `8528 Kitchen table` — **ADD construction**.
- `8548 Wood dining table`, `8560 Opulent table` — **ADD construction** (2).
- `8562 Wooden bench` — **ADD construction**.
- `8564-8572 Oak/Carved oak/Teak/Carved teak/Mahogany bench` — OK (`construction`).
- `8574 Gilded bench` — **ADD construction**.
- `8576 Wooden bed` — **ADD construction**.
- `8578-8584 Oak/Large oak/Teak/Large teak bed` — OK (`construction`).
- `8586 Four-poster bed`, `8588 Gilded four-poster` — **ADD construction** (2).

### Lecterns (POH study)

- `8534-8546 Oak/Eagle/Demon/Teak eagle/Teak demon/Mahogany eagle/Mahogany demon lectern` (7 items) — **ADD construction**.

### POH bar (kitchen barrels)

- `8516 Beer barrel`, `8518 Cider barrel` — **ADD construction** (2 items, POH bar barrels).
- `8520-8526 Asgarnian ale, Greenman's ale, Dragon bitter, Chef's delight` (4 dups of canonical brews already in COOKING) — LOG.

### Clocks, shaving stand, dressers, drawers, wardrobes

- `8590-8594 Oak/Teak/Gilded clock` (3 items) — **ADD construction**.
- `8596-8598 Shaving stand, Oak shaving stand` (2 items) — **ADD construction**.
- `8600-8608 Oak/Teak/Fancy teak/Mahogany/Gilded dresser` (5 items) — **ADD construction**.
- `8610 Shoe box` — **ADD construction**.
- `8612-8620 Oak/Teak drawers/wardrobe, Mahogany wardrobe` — OK (`construction`).
- `8622 Gilded wardrobe` — **ADD construction**.

### Study furniture (globes, orreries, telescopes)

- `8624-8628 Crystal ball, Elemental sphere, Crystal of power` (3 items) — **ADD construction**.
- `8630-8642 Globe/Ornamental globe/Lunar globe/Celestial globe/Armillary sphere/Small orrery/Large orrery` (7 items) — **ADD construction**.
- `8644-8648 Oak/Teak/Mahogany telescope` (3 items) — **ADD construction**.

### POH banners (Castle Wars / heraldic)

- `8650-8680 Banner × 16 dups` — LOG.

---

## Session 29 totals

- Items reviewed: ~150
- OK (correct as-is): 40
- EX: 0
- ADD (missing tab, fixed): 65 (16 bagged plants/trees, 7 hedges, 1 guide, 3 basic chairs, 1 Bookcase, 1 Kitchen table, 2 dining tables, 4 bench/bed basics, 2 four-poster, 7 lecterns, 2 barrels, 3 clocks, 2 shaving stands, 6 dresser/shoe box, 1 Gilded wardrobe, 10 study globes/orreries/telescopes, 3 crystal items)
- REM: 0
- LOG: 50+ (heraldic helm dups, kiteshield dups, banner dups, ale dups)

## Classifier changes made in session 29

See `audit/classifier-changes.md` "Session 29".

---

## Session 30: IDs 8717–9000

### Kiteshield dups continue

- `8718-8744 Rune kiteshield × 14 dups` — LOG.
- `8746-8776 Steel kiteshield × 16 dups` — LOG.

### Construction materials (canonical already classified)

- `8778-8782 Oak/Teak/Mahogany plank` — OK (`construction`).
- `8784 Gold leaf`, `8788 Magic stone` — LOG (dups of canonical 4692/4703 already in construction).
- `8786 Marble block`, `8790 Bolt of cloth`, `8792 Clockwork`, `8794 Saw` — OK.
- `8837 Timber beam` — EX (Cabin Fever quest item, not actual construction material).

### Pest Control + Warriors Guild

- `8839-8842 Void knight top/robe/mace/gloves` — OK (`melee;range;mage;quests`).
- `8844-8850 Bronze/Iron/Steel/Black/Mithril/Adamant/Rune defender` — OK (7 items, `melee;quests`).
- `8851 Warrior guild token` — **ADD misc Currency** (Warriors' Guild minigame entry token).
- `8856 Defensive shield` — OK (`melee`).
- `8857-8864 Shot/18lb shot/22lb shot/One-Five barrels` — EX (Dwarf Cannon quest).

### Dorgeshuun (Death to the Dorgeshuun + Lost Tribe)

- `8865 Ground ashes` — OK (`prayer`).
- `8866-8869 Steel/Bronze/Silver/Iron key` — LOG (quest keys, dups).
- `8870-8871 Zanik, Crate with zanik` — EX (NPC follower mechanic).
- `8872 Bone dagger` — OK (`melee`).
- `8880 Dorgeshuun crossbow` — OK (`range`).
- `8882 Bone bolts` — OK (`range;wc_fletching`).
- `8887 Zanik`, `8890 Coins` — EX.

### Slayer Black mask charge variants + Cabin Fever (Trouble Brewing)

- `8905-8921 Black mask (1-8 charges) variants` — OK (`melee;slayer`).
- `8923 Witchwood icon` — OK (`mage;slayer`).
- `8924-8928 Bandana eyepatch (white/red/blue/brown), Hat eyepatch` (5 items) — LOG (Cabin Fever cosmetic).
- `8929 Crabclaw hook` — OK (`melee`).

### Rocking the Boat + Cabin Fever quest material

- `8930-8946 Pipe section, Lumber patch, Scrapey tree logs (OK), Blue/Red flowers, Rum × 2, Monkey/Blue/Red monkey` — mostly EX (quest material).
- `8934 Scrapey tree logs` — OK (`wc_fletching;firemaking`).

### Trouble Brewing minigame cosmetics

- `8949-8950 Pirate bandana, Pirate hat` — LOG (TB cosmetic).
- `8951 Pieces of eight` — LOG (TB minigame currency; minigame-specific).
- `8952-8965 Naval shirts × 7 colours + Tricorn hats × 7 colours` (14 items) — LOG (TB cosmetic).
- `8966-8971 Cutthroat flag, Gilded smile flag, Bronze fist flag, Lucky shot flag, Treasure flag, Phasmatys flag` (6 items) — LOG (TB cosmetic).
- `8972-8974 Bowl of red water, Bowl of blue water` — EX (TB quest material).
- `8976-8997 Bitternut, Scrapey bark, Bridge section, Sweetgrubs, Bucket dup, Torch dup, The stuff, Brewin' guide × 2, Navy slacks × 7 colours` — EX/LOG (TB material + cosmetic).

---

## Session 30 totals

- Items reviewed: ~120
- OK (correct as-is): 32
- EX (correctly excluded): 15 (Cabin Fever quest material, Dwarf Cannon ammo)
- ADD (missing tab, fixed): 1 (Warrior guild token → misc Currency)
- REM: 0
- LOG: 70+ (Rune/Steel kiteshield × 30 dups, Trouble Brewing cosmetic × ~30, Black mask charge variants OK)

## Classifier changes made in session 30

See `audit/classifier-changes.md` "Session 30".

---

## Session 31: IDs 9001–9300

### Stronghold of Security + Skull sceptre

- `9003-9004 Security book, Stronghold notes` — EX.
- `9005 Fancy boots`, `9006 Fighting boots` — OK (`melee`).
- `9007-9012 Right/Left skull half, Strange skull, Top/Bottom of sceptre, Runed sceptre` — EX (Stronghold reward chain components).
- `9013 Skull sceptre` — OK (`mage`).

### Hazeel Cult + Magic essence

- `9016-9018 Gorak claws, Star flower, Gorak claw powder` — EX (Hazeel Cult quest material).
- `9019 Magic essence (unf)`, `9021-9024 Magic essence (1-4)` — **ADD herblore** (5 items, used to make Magic potion via Magic potion(unf) chain).
- `9025 Nuff's certificate` — EX.

### Pyramid Plunder loot (Icthlarin's Little Helper)

- `9026 Ivory comb` — EX.
- `9028-9032 Golden/Stone/Pottery scarab` (3 items) — **ADD agility_thieving** (Pyramid Plunder treasure).
- `9034-9038 Golden/Pottery/Stone statuette` (3 items) — **ADD agility_thieving**.
- `9040-9042 Gold seal, Stone seal` (2 items) — **ADD agility_thieving**.
- `9044 Pharaoh's sceptre` (canonical) — OK (`agility_thieving`).
- `9046-9050 Pharaoh's sceptre × 3 dups` — LOG.
- `9052 Locust meat` — EX (Contact! quest food).

### Goblin Diplomacy

- `9054-9059 Red/Black/Yellow/Green/Purple/Pink goblin mail` (6 items) — LOG (cosmetic recolour).

### Lunar Diplomacy (heavy)

- `9066-9067 Emerald lens, Dream log` — EX.
- `9068-9073 Moonclan helm/hat/armour/skirt/gloves/boots` — OK (6 items, `melee;mage`).
- `9074 Moonclan cape` — OK (`mage`).
- `9075 Astral rune` — OK (`mage;runecraft`).
- `9076 Lunar ore`, `9077 Lunar bar` — **ADD mining_smithing** (2 items, Astral Altar quest mining/smithing chain).
- `9078 Moonclan manual` — EX.
- `9079 Suqah tooth`, `9082 Ground tooth` — EX.
- `9080-9081 Suqah hide, Suqah leather` — OK (2 items, `crafting`).
- `9083 Seal of passage` — EX.
- `9084 Lunar staff` — OK (`mage;quests`).
- `9085 Empty vial` — OK (`cooking;crafting;herblore`).
- `9086 Vial of water` (dup of canonical 227) — LOG.
- `9087-9090 Waking sleep vial, Guam/Marr/Guam-marr vial` (4 items) — LOG (Lunar Diplomacy quest-only potions).
- `9091-9093 Lunar staff - pt1/pt2/pt3` — OK (3 items, `mage`).
- `9094-9095 Kindling, Soaked kindling` — EX (quest-only campfire fuel).
- `9096-9100 Lunar helm/torso/legs/gloves/boots` — OK (5 items, `melee;mage`).
- `9101 Lunar cape` — **REM range, ADD mage**. Lunar set is mage; cape has defence_ranged=2 spillover.
- `9102 Lunar amulet`, `9104 Lunar ring` — OK (2 items, `mage`).
- `9103 A special tiara`, `9106 Astral tiara` — OK (`runecraft`).

### Bolts + crossbows (Defender of Varrock)

- `9139-9145 Blurite/Iron/Steel/Mithril/Adamant/Runite/Silver bolts` — OK (7 items, `range;wc_fletching`).
- `9174-9185 Bronze/Blurite/Iron/Steel/Mithril/Adamant/Rune crossbow` — OK (7 items, `range`).
- `9187-9194 Jade/Topaz/Sapphire/Emerald/Ruby/Diamond/Dragonstone/Onyx bolt tips` — OK (8 items, `wc_fletching`).
- `9236-9245 Opal/Jade/Pearl/Topaz/Sapphire/Emerald/Ruby/Diamond/Dragonstone/Onyx bolts (e)` — OK (10 items, `range`).

---

## Session 31 totals

- Items reviewed: ~100
- OK (correct as-is): 60
- EX (correctly excluded): 18
- ADD (missing tab, fixed): 10 (5 Magic essence variants → herblore, 8 PP loot → agility_thieving, 2 Lunar ore/bar → mining_smithing — actually 15 if we count separately, but several PP items are loot subgroups)
- REM (misclassified, fixed): 1 (Lunar cape range → mage)
- LOG: 15+ (Lunar quest vials, Lunar cape REMfix incidental, Pharaoh's sceptre dups, goblin mail recolour, Vial of water dup)

## Classifier changes made in session 31

See `audit/classifier-changes.md` "Session 31".

---

## Session 32: IDs 9301–9600

### Gem bolts (Defender of Varrock fletching chain)

- `9335-9342 Jade/Topaz/Sapphire/Emerald/Ruby/Diamond/Dragonstone/Onyx bolts` — OK (8 items, `range;wc_fletching`).

### Unfinished bolts (canonical fletching chain)

- `9375-9379, 9381-9382 Bronze/Blurite/Iron/Steel/Mithril/Runite/Silver bolts (unf)` — OK (7 items, `wc_fletching`).
- `9380 Adamant bolts(unf)` — **ADD wc_fletching**. Spacing-variant `bolts(unf)` (no space before `(unf)`) wasn't matched by existing `name_ends(" bolts (unf)")` pattern.

### Death Plateau grapples

- `9415-9419 Grapple, Mith grapple tip, Mith grapple × 2` — LOG (Death Plateau / agility shortcut tool).

### Crossbow limbs + crossbow parts

- `9420-9431 Bronze/Iron/Steel/Mithril/Adamantite/Runite limbs` — OK (6 items, `wc_fletching`).
- `9422 Blurite limbs` — **ADD wc_fletching** (missing from limbs list).
- `9433 Bolt pouch`, `9434 Bolt mould` — **ADD wc_fletching** (2 items, crossbow construction tools).
- `9436-9438 Sinew, Crossbow string` — OK (`wc_fletching`).
- `9440-9452 Wooden/Oak/Willow/Teak/Maple/Mahogany/Yew stock` — OK (7 items).
- `9454-9465 Unfinished crossbow variants` — OK (7 items).
- `9467 Blurite bar` — OK (`mining_smithing`).

### Gnome Restaurant + Aluft Aloft

- `9468 Sawdust`, `9469 Grand seed pod`, `9470-9472 Gnome scarf/Gnome goggles`, `9474 Reward token`, `9477 Aluft aloft box` — LOG/EX.
- `9475 Mint cake` — **ADD cooking** (Gnome Restaurant food).
- `9478-9486 Half made batta, Unfinished batta × many dups` — LOG (gnome cooking intermediate dups).
- `9558-9564 Half made bowl, Unfinished bowl dups` — LOG.
- `9566-9576 Mixed blizzard/sgg/blast/punch/special/saturday/dragon × dups` (11 items) — LOG (gnome cocktail intermediate).
- `9577-9584 Half made crunchy, Unfinished crunchy dups` — LOG.

### Quest / Tower of Life misc

- `9589-9596 Dossier × 2, Broken cauldron, Magic glue, Weird gloop, Ground mud runes, Hazelmere's book, A eyeglo null` — EX (Hazeel Cult / Watchtower / Tower of Life quest material).
- `9597-9600 Red circle/triangle/square/pentagon` — EX (Tower of Life lock pattern).

---

## Session 32 totals

- Items reviewed: ~80
- OK (correct as-is): 40
- EX (correctly excluded): 12 (gnome quest cosmetic, Tower of Life puzzle, Hazeel Cult material)
- ADD (missing tab, fixed): 5 (Adamant bolts(unf), Blurite limbs, Bolt pouch, Bolt mould, Mint cake)
- REM: 0
- LOG: 25+ (gnome cooking intermediate variants, Death Plateau grapples)

## Classifier changes made in session 32

See `audit/classifier-changes.md` "Session 32".

---

## Session 33: IDs 9601–9900

### Tower of Life puzzle (continues)

- `9601-9624 Orange/Yellow/Green/Blue/Indigo/Violet × circle/triangle/square/pentagon` (24 items) — EX (Tower of Life puzzle).

### Underground Pass / Tirannwn

- `9625 Crystal saw` — OK (`construction`).
- `9626 Crystal saw seed` — OK (`farming`).
- `9627 A handwritten book` — EX.
- `9629 Tyras helm` — OK (`melee`).

### Darkness of Hallowvale (Vyrewatch / citizen disguise)

- `9632 Daeyalt ore` — OK (`mining_smithing`).
- `9633 Message` — EX.
- `9634-9638 Vyrewatch top/legs/shoes` — OK (3 items, `melee`).
- `9640-9644 Citizen top/trousers/shoes` — OK (3 items, `melee`).
- `9646-9649 Castle sketch × 3, Message` — EX.
- `9650-9655 Blood tithe pouch, Large ornate key, Haemalchemy volume 1, Sealed message, Door key, Ladder top` — EX (quest material).
- `9656-9658 Tome of experience (3/2/1)` (3 items) — LOG (post-quest XP tome).
- `9659-9665 Bucket of water/Bucket dup, Useless key, Torch dup` — EX/LOG.

### Proselyte armour (Slayer / Slug Menace)

- `9666-9678 Proselyte/Initiate harness m/f, Proselyte sallet/hauberk/cuisse/tasset` — OK (7 items, `prayer` or `melee;prayer`).

### Sea Slug + Slug Menace

- `9680-9683 Sea slug glue, Commorb v2, Door transcription, Dead sea slug` — EX.
- `9684-9689 Page 1-3, Fragment 1-3` — EX.

### Misthalin Mystery + rune dups

- `9690-9699 Blank water/air/earth/mind/fire rune + Water/Air/Earth/Mind/Fire rune dups` (10 items) — LOG (quest-only blank rune variants; canonical runes already in mage).

### Stronghold of Security training items

- `9702 Stick` — EX.
- `9703-9706 Training sword, Training shield, Training bow, Training arrows` — OK (4 items).

### Elemental Workshop II + intermediate bars

- `9715-9726 Slashed book, Rock, Beaten book, Crane schematic, Lever schematic, Crane claw, Scroll, Key, Pipe, Large/Medium/Small cog` — EX (Elemental Workshop II / Tower of Life material).
- `9727-9728 Primed bar, Primed mind bar` — LOG (Elemental Workshop intermediate; could ADD mining_smithing but EW-specific).
- `9729-9733 Elemental helmet, Mind shield, Mind helmet` — OK (3 items, `mage`).

### Goat horn (Olaf's Quest)

- `9735 Desert goat horn` — LOG (noted variant of standard goat horn, herblore secondary in canonical 9734).
- `9736 Goat horn dust` — OK (`herblore`).

### Combat potion

- `9739-9745 Combat potion (4-1)` — OK (4 items, `melee`).

### Skill cape hoods (canonical Champion's cape hood line)

- `9747-9814 Skill capes + hoods` — heavy review:
  - **Combat capes (cross-tag)**: Attack/Strength/Defence/Ranging/Magic/Hitpoints cape — OK (`melee;range;mage`).
  - **Combat hoods (currently 0 tabs)**:
    - `9749 Attack hood` — **ADD melee**.
    - `9752 Strength hood` — **ADD melee**.
    - `9755 Defence hood` — **ADD melee**.
    - `9758 Ranging hood` — **ADD range**.
    - `9764 Magic hood` — **ADD mage**.
    - `9770 Hitpoints hood` — **ADD melee;range;mage**.
    - `9791 Construct. hood` — **ADD construction**.
  - Skill capes/hoods that classify correctly: Prayer/Runecraft/Agility/Herblore/Thieving/Crafting/Fletching/Slayer/Mining/Smithing/Fishing/Cooking/Firemaking/Woodcutting/Farming/Quest point cape + hood — OK.

### POH wardrobe / cape rack / armour case extensions

- `9843-9845 Oak/Teak/Mahogany cape rack` — OK (`construction`).
- `9846-9848 Gilded/Marble/Magic cape rack` — **ADD construction** (3 items).
- `9849-9851 Oak/Teak/Mahogany toy box` — **ADD construction** (3 items).
- `9852-9856 Oak/Carved oak/Teak/Carved teak/Mahogany magic wardrobe` — OK (`construction`).
- `9857-9858 Gilded magic wardrobe, Marble magic wardrobe` — **ADD construction** (2 items).
- `9859-9861 Oak/Teak/Mahogany armour case` — OK (`construction`).
- `9862-9864 Oak/Teak/M. treasure chest` — **ADD construction** (3 items).
- `9865-9867 Oak/Teak/Mahogany fancy dress box` — **ADD construction** (3 items).

---

## Session 33 totals

- Items reviewed: ~200
- OK (correct as-is): 70
- EX (correctly excluded): 60 (Tower of Life puzzle ×24, quest dialogs, slug/sketches/cogs)
- ADD (missing tab, fixed): 21 (7 combat hoods, 14 POH furniture extensions)
- REM: 0
- LOG: 25+ (blank rune dups, primed bar, tome XP, message dups)

## Classifier changes made in session 33

See `audit/classifier-changes.md` "Session 33".

---

## Session 34: IDs 9901–10200

### Eyes of Glouphrie / Hardy gout / various quest

- `9901 Goutweedy lump`, `9903 Farming manual`, `9904 Sailing book`, `9906 Ghost buster 500` — EX.
- `9902 Hardy gout tubers` — LOG (single-use seed; standard Gout tuber already in farming).
- `9913-9918 White/Red/Blue/Green/Yellow/Black destabiliser` (6 items) — LOG (Halloween 2013 event item).
- `9919 Evil root` — EX.

### Halloween skeleton outfit (cosmetic)

- `9920 Jack lantern mask` — **ADD misc Holiday rares**.
- `9921-9925 Skeleton boots, gloves, leggings, shirt, mask` (5 items) — **ADD misc Holiday rares**.

### Auguste's Excursion + Eagles Peak

- `9932 Auguste's sapling` — OK (`farming`).
- `9933-9947 Balloon structure, Origami balloon, Sandbag, Bomber jacket/cap, Cap and goggles, Old red disk` — EX (quest cosmetic/material).
- `9948 Hunter cape`, `9950 Hunter hood` — OK (`hunter` group).
- `9951 Footprint` — EX.

### Hunter cooked-meat chain (Big Chompy / RFD Skrach)

- `9978 Raw bird meat` — OK (`cooking;hunter`).
- `9980 Roast bird meat` — **ADD cooking**.
- `9982 Burnt bird meat` — OK (`cooking`).
- `9984 Skewered bird meat` — **ADD cooking**.
- `9986 Raw beast meat` — OK (`cooking;hunter`).
- `9988 Roast beast meat` — **ADD cooking**.
- `9990 Burnt beast meat` — OK (`cooking`).
- `9992 Skewered beast` — **ADD cooking**.
- `9994 Spicy tomato`, `9996 Spicy minced meat` — **ADD cooking** (2 items, RFD Skrach kebab ingredients).

### Hunter potions

- `9998-10004 Hunter potion (4-1)` — OK (4 items, `herblore`).

### Hunter traps + butterflies

- `10006-10010 Bird snare, Box trap, Butterfly net` — OK (3 items, `hunter`).
- `10012 Butterfly jar` — **ADD hunter**.
- `10014-10020 Black warlock, Snowy knight, Sapphire glacialis, Ruby harvest` (4 items) — **ADD hunter** (caught butterflies, can be released for buff).
- `10023 Falconer's glove` — **ADD hunter**.
- `10025 Magic box` — OK (`hunter`).
- `10027-10028 Imp-in-a-box (2/1)` (2 items) — **ADD hunter**.
- `10029 Teasing stick` — **ADD hunter**.
- `10031 Rabbit snare` — **ADD hunter**.
- `10033 Chinchompa`, `10034 Red chinchompa` — **ADD hunter** (caught chinchompas; also valid ranged weapons but missing from hunter tab).

### Hunter camo + Kyatt/Larupia/Graahk

- `10035-10051 Kyatt/Larupia/Graahk top/legs/hat/headdress` — OK (9 items, `melee;hunter`).
- `10053-10067 Wood/Jungle/Desert/Polar camo top/legs` — OK (8 items, `melee[;hunter]`).
- `10069-10071 Spotted cape, Spottier cape` — OK (`hunter`).

### Spiky vambraces colour variants

- `10075 Gloves of silence` — OK (`melee;agility_thieving;hunter`).
- `10077 Spiky vambraces`, `10079 Green spiky vambraces`, `10081 Blue spiky vambraces` — OK (`melee`).
- `10083 Red spiky vambraces` — **REM mage** (clue scroll cosmetic colour variant; should be melee only).
- `10085 Black spiky vambraces` — **REM mage, ADD melee** (same).

### Feathers + Hunter furs

- `10087-10091 Stripy/Red/Blue/Yellow/Orange feather` — OK (5 items, multi-tag).
- `10092 Ferret` — EX.
- `10093 Tatty larupia fur`, `10097 Tatty graahk fur`, `10101 Tatty kyatt fur` (3 items) — **ADD hunter** (Tatty furs need cleaning at Fancy clothes shop).
- `10095 Larupia fur`, `10099 Graahk fur`, `10103 Kyatt fur` — OK (`hunter`).
- `10105 Kebbit spike`, `10107 Long kebbit spike`, `10109 Kebbit teeth`, `10113 Kebbit claws` (4 items) — **ADD hunter** (Kebbit byproducts).
- `10111 Kebbit teeth dust` — OK (`herblore`).
- `10115-10127 Dark kebbit/Polar kebbit/Feldip weasel/Common kebbit/Desert devil/Spotted kebbit/Dashing kebbit fur` (7 items) — **ADD hunter**.

### Misc hunter / fishing / clue

- `10129 Barb-tail harpoon` — OK (`melee;fishing`).
- `10132-10134 Strung rabbit foot, Rabbit foot` — LOG (cosmetic/utility).
- `10136-10140 Rainbow fish, Raw rainbow fish, Burnt rainbow fish` — OK (3 items, `cooking[;fishing]`).
- `10142-10145 Guam/Marrentill/Tarromin/Harralander tar` — OK (4 items, `range;hunter`).
- `10146-10149 Orange/Red/Black salamander, Swamp lizard` — OK (4 items, `hunter`).
- `10150 Noose wand` — **ADD hunter**.
- `10156 Hunters' crossbow` — OK (`range`).
- `10158-10159 Kebbit bolts, Long kebbit bolts` — OK (`range;wc_fletching`).
- `10167-10177 Eagle feather, Eagle cape, Fake beak, Bird book, Metal/Golden/Silver/Bronze feather` (~7 items) — EX (Eagles Peak quest material).
- `10178 Odd bird seed` — OK (`farming`).
- `10179 Feathered journal` — EX.
- `10180-10200 Clue scroll (easy) × 11 dups` — LOG.

---

## Session 34 totals

- Items reviewed: ~180
- OK (correct as-is): 70
- EX (correctly excluded): 18 (Eyes of Glouphrie quest material, Eagles Peak quest item)
- ADD (missing tab, fixed): 38 (6 cooked meat chain, 4 Halloween skeleton, 1 Jack lantern, 18 hunter traps/butterflies/furs, 11 hunter kebbit byproducts, 2 spiky vambraces ADD melee)
- REM: 2 (Red/Black spiky vambraces from mage)
- LOG: 30+ (clue easy dups, destabilisers, tatty fur dups, gout)

## Classifier changes made in session 34

See `audit/classifier-changes.md` "Session 34".

---

## Session 35: IDs 10201–10500

### Clue scroll dups

- `10202-10232 Clue scroll (easy) × 16` — LOG.
- `10234-10252 Clue scroll (hard) × 10` — LOG.
- `10254-10278 Clue scroll (medium) × 13` — LOG.

### Composite bows + heraldic helms (Treasure Trails)

- `10280-10284 Willow/Yew/Magic comp bow` — OK (3 items, `range;wc_fletching`).
- `10286-10314 Rune/Adamant/Black helm (h1-h5)` (15 items) — OK (`melee`).

### Bob's shirts (Wise Old Man random event cosmetic)

- `10316-10324 Bob's red/blue/green/black/purple shirt` (5 items) — LOG (cosmetic chest-piece).

### Firelighters + colour logs

- `10326-10327 Purple/White firelighter` — OK (`firemaking`).
- `10328-10329 White/Purple logs` — OK (`wc_fletching;firemaking`).

### 3rd age armour set (clue scroll rare)

- `10330 3rd age range top`, `10332 3rd age range legs`, `10334 3rd age range coif`, `10336 3rd age vambraces` — **REM melee/mage, ADD range** (4 items).
- `10338-10344 3rd age robe top/robe/mage hat/amulet` — OK (4 items, `mage`).
- `10346-10352 3rd age platelegs/platebody/full helmet/kiteshield` — **REM mining_smithing** (4 items; 3rd age armour is not smithable).

### Glory / Strength / Magic amulet trim variants

- `10354 Amulet of glory (t4)` — OK (`melee;range;mage`).
- `10362 Amulet of glory (t)` — **ADD melee;range;mage** (1 item, trim variant of canonical 1704).
- `10364 Strength amulet (t)` — **ADD melee** (trim variant of Amulet of strength).
- `10366 Amulet of magic (t)` — **ADD mage** (trim variant).

### God d'hide armour (Treasure Trails set)

- `10368 Zamorak bracers`, `10376 Guthix bracers`, `10384 Saradomin bracers` — **REM mage, ADD range** (3 items; bracers are range gloves/vambraces).
- `10370 Zamorak d'hide body`, `10378 Guthix d'hide body`, `10386 Saradomin d'hide body` — **REM melee, ADD range only** (3 items; d'hide body cross-tag with crafting OK retained).
- `10372 Zamorak chaps`, `10380 Guthix chaps`, `10388 Saradomin chaps` — **REM melee, ADD range** (3 items; range d'hide legs equivalent).
- `10374 Zamorak coif`, `10382 Guthix coif`, `10390 Saradomin coif` — **REM melee, ADD range** (3 items).

### Treasure Trails fashion cosmetic

- `10392 A powdered wig`, `10394 Flared trousers`, `10396 Pantaloons`, `10398 Sleeping cap` (4 items) — LOG (fashion-only cosmetic).
- `10400-10438 Black/Red/Blue/Green/Purple elegant shirt/legs + White/Red/Blue/Green/Purple elegant blouse/skirt` (~20 items) — LOG (Treasure Trails elegant cosmetic outfits).

### God robes (priest/druid sets — Treasure Trails Hard rewards)

- `10440-10444 Saradomin/Guthix/Zamorak crozier` — OK (3 items, `mage`).
- `10446 Saradomin cloak` (dup of 4041), `10450 Zamorak cloak` (dup of 4042) — LOG.
- `10448 Guthix cloak` (newer variant) — OK (`melee;range;mage` cross-tag, plus `mage` God cloaks).
- `10452-10456 Saradomin/Guthix/Zamorak mitre` — OK (3 items, `mage`).
- `10458-10468 Saradomin/Zamorak/Guthix robe top/legs` — OK (6 items, `mage`).
- `10470-10474 Saradomin/Guthix/Zamorak stole` — OK (3 items, `mage`).

### Misc Halloween + clue scroll items

- `10476 Purple sweets` — OK (`misc`).
- `10485-10495 Scroll, Empty sack, Undead chicken, Selected iron, Bar magnet, Undead twigs, Blessed axe, Research notes, Translated notes, A pattern, A container` — EX/LOG (Animal Magnetism quest material).
- `10491 Blessed axe` — OK (`wc_fletching`).
- `10496 Polished buttons` — EX.
- `10498-10499 Ava's attractor, Ava's accumulator` — OK (`mage`). Note: Avas are cross-tag with range too, but classifier currently puts them in mage only.
- `10500 Crone-made amulet` — LOG (Olaf's Quest reward, single-use teleport).

---

## Session 35 totals

- Items reviewed: ~150
- OK (correct as-is): 70
- EX (correctly excluded): 13 (Animal Magnetism quest material)
- ADD (missing tab, fixed): 15 (4 3rd age range to range, 3 god bracers to range, 3 god d'hide body REM melee, 3 god chaps REM melee, 3 god coif REM melee, 3 amulet trim variants)
- REM (misclassified, fixed): 26 (3rd age range pieces × 4 from melee/mage, 3rd age plate × 4 from mining_smithing, 3 god bracers from mage, 9 god d'hide pieces from melee)
- LOG: 70+ (clue scroll dups, elegant fashion ×20, Bob's shirts ×5, fashion cosmetic ×4, cloak dups)

## Classifier changes made in session 35

See `audit/classifier-changes.md` "Session 35".

---

## Session 36: IDs 10501–10800 + 10808–10900 (ID gap 10601–10807)

### Christmas 2006 event

- `10501 Snowball`, `10506 Gublinch shards`, `10507 Reindeer hat`, `10508 Wintumber tree`, `10509 Snowball` (dup) — LOG (Christmas 2006 cosmetic + reward).

### Barbarian Assault (BA)

- `10512-10515 Scroll, Crackers, Tofu, Worms` — EX/LOG (BA quest material / training food).
- `10516-10560 BA attacker/healer/defender/collector horns, Various eggs (Green/Red/Blue/Yellow + Poisoned/Spiked/Omega), Poisoned tofu/worms/meat, Healing vials, Penance hat/torso/boots/gloves/skirt, BA icons, Spikes, Queen help book, No eggs` — heavy review:
  - Most BA gear classifies (Healer hat melee;mage;quests, Fighter torso melee;quests, etc.). OK.
  - Eggs, horns, icons, scrolls in 0 tabs are minigame-specific tools. LOG.

### Granite armour (Mountain Daughter)

- `10564 Granite body` — **REM range, ADD melee** (granite armour is melee gear; defence_ranged spillover misclassified it).
- `10589 Granite helm` — **REM range, ADD melee** (same).

### Inferno reward

- `10566 Fire cape` — **ADD misc Combat trophies** (TzHaar Fight Caves reward; combat completion cosmetic; can be wielded but no offensive bonus).

### Misc weapons + amulet + quest material

- `10581 Keris` — OK (`melee;quests`).
- `10585-10587 Parchment, Combat lamp, Tarn's diary` — EX.
- `10588 Salve amulet (e)` — OK (`melee`).
- `10592-10600 Penguin bongos, Cowbells, Clockwork book/suit, Mission reports, KGP id card` — EX (Cold War / Hunt for Surok quest material).

### Fremennik Isles

- `10808-10810 Arctic pyre logs, Arctic pine logs` — OK (`wc_fletching;firemaking`).
- `10812-10814 Split log, Hair` — EX.
- `10816 Raw yak meat` — OK (`cooking`).
- `10818 Yak-hide` — OK (`crafting`).
- `10820 Cured yak-hide` — **ADD crafting**.
- `10822 Yak-hide armour (top)`, `10824 Yak-hide armour (legs)` (1 dup) — OK (`melee;range`).
- `10826 Neitiznot shield` — OK (`range`).
- `10828 Helm of neitiznot` — OK (`melee`).

### Royal Trouble + Tax bags

- `10829-10835 Documents, Royal decree, Empty/Light/Normal/Hefty/Bulging taxbag` (7 items) — EX (Royal Trouble quest material).

### Silly jester (Recipe for Disaster)

- `10836-10839 Silly jester hat/top/tights/boots` — OK (4 items, `mage`).
- `10840 A jester stick` — LOG (jester cosmetic).

### Apricot cream pie + Sorceress's Garden

- `10841 Apricot cream pie` — OK (`cooking`).
- `10842 Decapitated head` — EX.
- `10844-10847 Spring/Summer/Autumn/Winter sq'irk` — **ADD farming** (4 items, Sorceress's Garden fruit).
- `10848-10851 Spring/Summer/Autumn/Winter sq'irkjuice` — **ADD cooking** (4 items, Sorceress's Garden potion).

### Tarn / Shadow sword

- `10856-10857 Sin seer's note, Severed leg` — EX.
- `10858 Shadow sword` — OK (`melee`).
- `10859 Tea flask` — LOG (Sorceress's Garden / Lunar Diplomacy minigame container).

### Garbage entries

- `10860-10861 "null <sup..." entries` — EX (data garbage / wiki HTML scraping artifact).

### The Slug Menace builder kit

- `10862-10866 Hard hat, Builder's shirt/trousers/boots, Rivets` (5 items) — LOG (Slug Menace cosmetic).
- `10870-10874 Binding fluid, Pipe, Pipe ring, Metal sheet, Coloured ball` — EX (Slug Menace quest material).

---

## Session 36 totals

- Items reviewed: ~110
- OK (correct as-is): 35
- EX (correctly excluded): 30 (heavy quest content: Royal Trouble, Slug Menace, Mission reports, Christmas event garbage)
- ADD (missing tab, fixed): 11 (Fire cape misc, Cured yak-hide crafting, 4 sq'irk farming, 4 sq'irkjuice cooking, Granite body/helm to melee = 2 net ADD-melee)
- REM (misclassified, fixed): 2 (Granite body, Granite helm from range)
- LOG: 35+ (BA minigame eggs/icons/spikes, Christmas dups, Builder cosmetic, Tea flask, jester stick)

## Classifier changes made in session 36

See `audit/classifier-changes.md` "Session 36".

---

## Session 37: IDs 10875–11200

### Slug Menace quest material

- `10875-10905 Valve wheel, Metal bar, Plain/Green/Red/Black/Gold/Rune satchel, Fuse, Keg × 2, Prayer book × 2, Wooden cat, Cranial clamp, Brain tongs, Bell jar, Wolf whistle, Shipping order, Crate part, Skull staple` (~20 items) — EX/LOG (Slug Menace quest material).
- `10887 Barrelchest anchor` — OK (`melee`).

### Sanfew serum (Dream Mentor herblore chain)

- `10909-10923 Mixture - step 1 (4-1), Mixture - step 2 (4-1)` (8 items) — **ADD herblore** (Sanfew serum intermediate dose variants).
- `10925-10931 Sanfew serum (4-1)` — OK (`melee;prayer;herblore`).

### Lumberjack outfit (Random event / Wintertodt-era cosmetic)

- `10933 Lumberjack boots`, `10939 Lumberjack top`, `10940 Lumberjack legs`, `10941 Lumberjack hat` (4 items) — **ADD wc_fletching** (Lumberjack outfit, woodcutting XP boost set).

### Random events + misc

- `10934 Reward token`, `10937 Nail beast nails`, `10946-10949 Pushup/Run/Situp/Starjump` — EX.
- `10950 Skull staples` — LOG.
- `10952 Slayer bell` — **ADD slayer** (Slayer-task tracking utility).

### Frog-leather + Temple Trekking food (Roving Elves)

- `10954-10958 Frog-leather body/chaps/boots` — OK (3 items, `range[+melee]`).
- `10960-10970 Green gloop soup, Frogspawn gumbo, Frogburger, Coated frogs' legs, Bat shish, Fingers, Grubs à la mode, Roast frog, Mushrooms, Fillets, Loach` (11 items) — **ADD cooking** (Temple Trekking food).
- `10971 Eel sushi` — **ADD cooking**.

### Dorgesh-Kaan / Power station

- `10972-10995 Dorgesh-kaan sphere, Spanner, Long/Curved bone (prayer ✓), Swamp weed, Light orb, Empty light orb, Cave goblin wire, Cog, Fuse, Meter, Capacitor, Lever, Powerbox, Perfect shell/snail shell` — EX/LOG.
- `10976-10977 Long bone, Curved bone` — OK (`prayer`).

### What Lies Below

- `10999-11013 Goblin book, Dagon'hai history, Sin'keth's diary, Empty/Used/Full folder, Rat's paper, Surok's letter dups, Zaff's instructions, Wand × 2` — EX.
- `11014 Beacon ring` — OK (`mage;misc`).

### Easter 2007 chicken outfit + chocolate kebbit

- `11019-11023 Chicken feet/wings/head/legs, Magic egg` (5 items) — **ADD misc Holiday rares** (Easter 2007 chicken event cosmetic).
- `11024-11026 Rabbit mould, Chocolate chunks, Chocolate kebbit` (3 items) — **ADD misc Holiday rares**.
- `11027 Easter egg` — OK (`misc`).

### Olaf's Quest

- `11031-11037 Damp planks, Crude/Cruder carving, Sven's last map, Windswept logs ✓, Parchment, Brine sabre ✓` — partial.

### Misthalin Mystery / King's Ransom quest material

- `11039-11058 Key, Rotten barrel × 2, Rope, Armour shard, Artefact, Axe head, Helmet/Shield/Sword fragment, Mace` — EX (King's Ransom + Defender of Varrock material).

### Goblin Village + Ancient mace

- `11060 Goblin village sphere` — EX.
- `11061 Ancient mace` — OK (`melee`).

### Jewellery — bracelets

- `11065 Bracelet mould`, `11067-11103 various crafted bracelets` — most ✓ crafting.
- `11079-11083 Castle wars bracelet (3-1)` (3 items) — **ADD misc Teleport jewellery** (Combat bracelet variant).
- `11088 Inoculation bracelet` — **ADD misc Teleport jewellery**.
- `11095-11103 Abyssal bracelet (5/3/2/1)` (4 items) — **ADD misc Teleport jewellery** (Castle Wars + Wilderness teleport bracelet).
- `11118-11126 Combat bracelet (4-0)` — OK (5 items, `melee;range;mage;misc`).
- `11128 Berserker necklace` — OK (`melee`).
- `11130 Onyx bracelet` — OK (`crafting`).
- `11133 Regen bracelet` — OK (`melee;range;mage`).
- `11136-11140 Karamja gloves 1/2/3` — OK (`melee;range;mage;quests`).
- `11107-11115 Skills necklace, Dragonstone bracelet` etc. — OK.

### While Guthix Sleeps + dream chain

- `11151-11159 Dream vial, Dream potion, Ground astral rune, Astral rune shards, Dreamy lamp, Cyrisus's chest, Hunter kit` — EX.

### Defender of Varrock + Phoenix gang

- `11164-11173 Restored shield, Phoenix crossbow × 2 dups, Newspaper × 2, Half certificate` — LOG/EX.

### Varrock Museum / Digsite

- `11175-11184 Uncleaned find, Arrowheads, Jewellery, Pottery, Old/Ancient coin, Ancient/Old symbol, Old chipped vase, Museum map` (10 items) — LOG (Varrock Museum archaeology kits, single-use cleanup items).
- `11189 Antique lamp` — EX.
- `11190-11194 Digsite pendant (1-5)` — OK (4 items, `misc`).
- `11195-11199 Clean necklace, Griffin feather, Miazrqa's pendant, Music sheet, Rupert's helmet` — EX.
- `11200 Dwarven helmet` — OK (`melee`).

---

## Session 37 totals

- Items reviewed: ~200
- OK (correct as-is): 50
- EX (correctly excluded): 60 (Slug Menace, What Lies Below, King's Ransom, Defender of Varrock, While Guthix Sleeps)
- ADD (missing tab, fixed): 36 (4 Lumberjack outfit, 12 Temple Trekking food, 1 Eel sushi, 1 Slayer bell, 8 Mixture step herblore, 5 Easter chicken, 3 Easter chocolate, 3 Castle wars bracelet, 1 Inoculation, 4 Abyssal bracelet)
- REM: 0
- LOG: 50+ (Varrock Museum dig kits, Phoenix dups, Slug Menace satchels, random event)

## Classifier changes made in session 37

See `audit/classifier-changes.md` "Session 37".

---

## Session 38: IDs 11201–11500

### Eyes of Glouphrie material

- `11202-11210 Shrinking recipe, To-do list, Shrink-me-quick, Shrunk ogleroot, Golden goblin, Magic beans` — EX.

### Dragon range ammo (Mourning's End Part II)

- `11212-11237 Dragon arrow, Dragon fire arrow, Dragon dart, Dragon dart tip, Dark bow, Dragon arrowtips` — OK (`range[+wc_fletching]`).

### Impling jars + butterfly net

- `11238-11256 Baby/Young/Gourmet/Earth/Essence/Eclectic/Nature/Magpie/Ninja/Dragon impling jar` — OK (10 items, `hunter`).
- `11258 Jar generator`, `11259 Magic butterfly net`, `11260 Impling jar` (3 items) — **ADD hunter**.
- `11262 Imp repellent`, `11264-11266 Anchovy oil/paste`, `11273 Impling scroll` — EX/LOG (Imp Catcher / Eyes of Glouphrie material).

### Mounted heads + cosmetic masks

- `11279 Elvarg's head` — **ADD slayer** (Dragon Slayer trophy head, mountable in POH).
- `11280 Cavalier mask`, `11282 Beret mask` — LOG (Treasure Trails fashion).

### Dragonfire shield + Draconic visage

- `11283 Dragonfire shield` — OK (`melee`).
- `11284 Dragonfire shield` (dup) — LOG.
- `11286 Draconic visage` — **ADD melee** (raw uncrafted shield base; combat tab valuable for grouping).

### Barbarian Training fishing chain

- `11323 Barbarian rod` — OK (`fishing`).
- `11324 Roe` — **ADD fishing** (barbarian fishing byproduct, also herblore secondary at 11326 Caviar).
- `11326 Caviar` — OK (`herblore`).
- `11328-11332 Leaping trout, Leaping salmon, Leaping sturgeon` (3 items) — **ADD fishing**.
- `11334 Fish offcuts` — **ADD fishing**.

### Dragon full helm + bones

- `11335 Dragon full helm` — OK (`melee;mining_smithing`).
- `11337-11338 Mangled bones, Chewed bones` — OK (`prayer`).
- `11339-11340 My notes, Barbarian skills` — EX.
- `11341-11348 Ancient page × 7` — LOG (Memoirs of a Barbarian).

### Hasta weapons

- `11367-11377 Bronze/Iron/Steel/Mithril/Adamant/Rune hasta` — OK (6 items, `melee`).

### Barbarian Training mix potions

- `11427-11428 Fish vial × 2` — LOG (Barbarian Training caviar prep vial).
- `11429-11523 various mix(1)/mix(2) potions` — heavy review:
  - Already classified (Antipoison mix, Super energy mix, Super restore mix, Antidote+ mix, Antifire mix) — OK.
  - Currently 0 tabs (Attack mix, Strength mix, Combat mix, Restore mix, Energy mix, Defence mix, Agility mix, Prayer mix, Superattack mix, Anti-poison supermix, Fishing mix, Super str. mix, Magic essence mix, Super def. mix, Relicym's mix, Ranging mix, Magic mix, Hunting mix, Zamorak mix) — **ADD herblore** via new pattern-based section catching `name_ends("mix(1)")` and `name_ends("mix(2)")`.

**Affects**: ~40 mix variants (all current + new pattern coverage).

---

## Session 38 totals

- Items reviewed: ~120
- OK (correct as-is): 40
- EX (correctly excluded): 15
- ADD (missing tab, fixed): 50 (3 hunter jars, 5 barbarian fishing, 1 Elvarg's head, 1 Draconic visage, ~40 mix potion variants)
- REM: 0
- LOG: 15+ (Ancient pages, fashion masks, Imp Catcher material)

## Classifier changes made in session 38

See `audit/classifier-changes.md` "Session 38".

---

## Session 39: IDs 11525–11900

### Pest Control Void + quest

- `11663-11673 Void mage/ranger/melee helm + Void seal (1-8)` — OK (8 items, `melee;range;mage;quests`).
- `11677-11682 Explorer's notes, Black knight helm, Antique lamp, Address form, Scrap paper, Hair clip` — EX.

### Treasure trails ammo cache + raw pheasant

- `11686-11699 Various rune dups, 11700-11703 arrow dups` — LOG (clue scroll reward stacks).
- `11704 Raw pheasant` — EX (Hunt for Surok quest).

### Nightmare Zone (NZ) variants

- `11705 Beach boxing gloves` — OK (`melee`).
- `11707-11709 Cursed goblin hammer/bow/staff` — OK (Goblin Diplomacy).
- `11710-11721 Anti-dragon shield (nz), Magic secateurs (nz), Chaos/Death/Blood/Air/Water/Earth/Fire rune (nz), Rune/Mithril/Iron pickaxe (nz)` (12 items) — LOG (NZ-instance variants of existing items, not bankable).

### NZ reward potions

- `11722-11725 Super ranging (4-1)` (4 items) — **ADD herblore;range** (NZ-reward super ranging potion).
- `11726-11729 Super magic potion (4-1)` — OK (`mage;herblore`).
- `11730-11733 Overload (4-1)` (4 items) — **ADD herblore** (NZ-only super combat potion).
- `11734-11737 Absorption (4-1)` (4 items) — **ADD herblore** (NZ damage absorber).
- `11738 Herb box` — **ADD herblore** (NZ bulk grimy herb reward).

### POH Scroll of redirection + teleport tabs

- `11740 Scroll of redirection` — **ADD construction;misc** (re-routes POH teleport tabs).
- `11741-11747 Rimmington/Taverley/Pollnivneach/Rellekka/Brimhaven/Yanille/Trollheim teleport` — OK (7 items, `misc`).

### Imbued crystal + rings + black masks

- `11748 New crystal bow (i)`, `11759 New crystal shield (i)` — OK.
- `11770-11773 Seers/Archers/Warrior/Berserker ring (i)` — OK (4 items).
- `11776-11784 Black mask (i) charge variants` — OK (`melee;slayer`).

### Godswords (Nomad's Requiem + GWD)

- `11785 Armadyl crossbow` — OK (`range`).
- `11787 Steam battlestaff`, `11789 Mystic steam staff`, `11791 Staff of the dead` — OK.
- `11794-11800 Godsword shards 1&2, 1&3, 2&3, Godsword blade` (4 items) — **ADD melee** (godsword construction intermediates).
- `11802-11808 Armadyl/Bandos/Saradomin/Zamorak godsword` — OK (4 items, `melee`).
- `11810-11816 Armadyl/Bandos/Saradomin/Zamorak hilt` (4 items) — **ADD melee**.
- `11818-11822 Godsword shard 1/2/3` (3 items) — **ADD melee**.

### God Wars armour reclassification

- `11824 Zamorakian spear` — OK (`melee`).
- `11826 Armadyl helmet` — **REM melee/mage, ADD range** (Armadyl is range set).
- `11828-11830 Armadyl chestplate, Armadyl chainskirt` — **REM mage, ADD range** (2 items).
- `11832-11834 Bandos chestplate, Bandos tassets` — **REM range, ADD melee** (2 items, Bandos is melee set despite defence_ranged stats).
- `11836 Bandos boots` — OK (`melee`).
- `11838 Saradomin sword` — OK (`melee`).
- `11840 Dragon boots` — OK (`melee`).
- `11842 Knight's notes` — EX.

### Halloween rares + Graceful

- `11847 Black h'ween mask` — **ADD misc Holiday rares**.
- `11848 Rancid turkey` — EX (Thanksgiving event quest food).
- `11849-11860 Mark of grace, Graceful hood/cape/top/legs/gloves/boots` — OK (7 items, `agility_thieving`).
- `11862 Black partyhat`, `11863 Rainbow partyhat` — **ADD misc Holiday rares** (2 items, Christmas event partyhats).

### Slayer

- `11864-11873 Slayer helmet (i), Slayer ring (1-8)` — OK.
- `11874 Broad arrowheads` — **ADD wc_fletching** (slayer broad arrow craft material).
- `11875-11876 Broad bolts, Unfinished broad bolts` — OK.
- `11877-11887 Empty/Water-filled vial pack, Feather/Bait/Broad arrowhead/Unfinished broad bolt pack` (6 items) — **ADD slayer** (Slayer master bulk-purchase packs).

### Zamorakian hasta + Castle Wars cosmetic

- `11889 Zamorakian hasta` — OK (`melee`).
- `11893-11900 Decorative armour × 8 dups` — LOG (Castle Wars cosmetic).

---

## Session 39 totals

- Items reviewed: ~150
- OK (correct as-is): 60
- EX (correctly excluded): 12 (Nomad's Requiem dialog, Hair clip)
- ADD (missing tab, fixed): 38 (4 Super ranging, 4 Overload, 4 Absorption, 1 Herb box, 1 Scroll of redirection, 11 godsword intermediates, 3 Armadyl range, 2 Bandos melee, 3 holiday rares, 1 Broad arrowheads, 6 slayer packs)
- REM (misclassified, fixed): 7 (Armadyl helm/chestplate/chainskirt × 3 from melee/mage; Bandos chestplate/tassets × 2 from range)
- LOG: 30+ (Decorative armour ×8, NZ-instance variants ×12, rune dups, partyhat dups)

## Classifier changes made in session 39

See `audit/classifier-changes.md` "Session 39".

---

## Session 40: IDs 11901–12200

### Castle Wars decorative dup

- `11901 Decorative armour` — LOG (Castle Wars cosmetic dup).

### Trident of the seas (Kraken slayer chain)

- `11902 Leaf-bladed sword` — OK (`melee`).
- `11904 Entomologist's diary` — EX.
- `11905 Trident of the seas (full)`, `11908 Uncharged trident` — **ADD mage** (2 items, Kraken-slot mage weapon).

### Misc + Random events

- `11910-11916 Chocolate strawberry, Box of chocolate strawberries, Slice of birthday cake` — LOG (event food).
- `11919 Cow mask` — LOG (cosmetic event head).
- `11920 Dragon pickaxe` — OK (`melee;mining_smithing`).
- `11922 Lava dragon bonemeal` — OK (`prayer`).
- `11923 Broken pickaxe` — EX.

### Ward (KQ chest rewards) + ward shards

- `11924 Malediction ward` — OK (`melee;mage`).
- `11926 Odium ward` — OK (`range`).
- `11928-11930 Odium shard 1/2/3` (3 items) — **ADD range** (construction intermediate to Odium ward).
- `11931-11933 Malediction shard 1/2/3` (3 items) — **ADD mage** (construction intermediate to Malediction ward).

### Wilderness Dark crab + Looting

- `11934-11940 Raw dark crab, Dark crab, Burnt dark crab, Dark fishing bait` — OK (`cooking[+fishing]`).
- `11941 Looting bag` — OK (`misc`).
- `11942 Ecumenical key` — OK (`misc`).
- `11943 Lava dragon bones` — OK (`prayer`).

### Extended antifire

- `11951-11957 Extended antifire (4-1)` — OK (4 items, `herblore`).
- `11959 Black chinchompa` — OK (`hunter`).
- `11960-11962 Extended antifire mix (2/1)` — OK (`herblore`).

### Charged jewellery higher-charge variants

- `11968 Skills necklace(6)` — **ADD misc Teleport jewellery** (6-charge variant).
- `11970 Skills necklace(5)` — OK (`misc`).
- `11972 Combat bracelet(6)` — **ADD misc Teleport jewellery**.
- `11974 Combat bracelet(5)` — OK (`melee;range;mage;misc`).
- `11976 Amulet of glory(5)` — **ADD misc Teleport jewellery** (currently `melee;range;mage` only).
- `11978 Amulet of glory(6)` — OK (`melee;range;mage;misc`).
- `11980-11988 Ring of wealth (1-5)` — OK (`misc`).

### Misc + slayer-related

- `11990 Fedora` — LOG (cosmetic random event).
- `11992 Lava scale` — **ADD herblore** (Lava dragon scale, herblore secondary).
- `11994 Lava scale shard` — OK (`herblore`).
- `11995 Pet chaos elemental`, `11996-11997 Holiday tool, Easter` — LOG/EX.

### Mage gear + Kraken

- `11998 Smoke battlestaff`, `12000 Mystic smoke staff`, `12002 Occult necklace` — OK (3 items, `mage[;crafting]`).
- `12004 Kraken tentacle` — **ADD slayer** (slayer boss drop, converts to Abyssal tentacle).
- `12006 Abyssal tentacle` — OK (`melee`).
- `12007 Jar of dirt` — LOG (Cerberus boss pet jar).

### Motherlode Mine

- `12009-12010 Soft clay pack × 2` — **ADD mining_smithing** (Motherlode reward bulk pack).
- `12011 Pay-dirt` — **ADD mining_smithing** (Motherlode mining raw output).
- `12012 Golden nugget` — **ADD mining_smithing** (Motherlode currency).
- `12013-12016 Prospector helmet/jacket/legs/boots` — OK (4 items, `mining_smithing`).

### Salve amulet + bags + clue scrolls

- `12017 Salve amulet(i)`, `12018 Salve amulet(ei)` — OK (`melee`).
- `12019-12020 Coal bag, Gem bag` — LOG (dups of canonicals).
- `12021-12071 Clue scroll (medium) × 26 dups` — LOG.
- `12073-12159 Clue scroll (elite) (12073 canonical OK; rest ×~60 dups)` — LOG.
- `12162-12192 Clue scroll (easy) × 27 dups` — LOG.

### Ancient set (Treasure Trails Hard)

- `12193 Ancient robe top`, `12195 Ancient robe legs`, `12199 Ancient crozier` — OK (3 items, `mage`).
- `12197 Ancient cloak` — OK (`melee;range;mage` via cape slot predicate; reasonable cross-tag).

---

## Session 40 totals

- Items reviewed: ~250
- OK (correct as-is): 50
- EX (correctly excluded): 8
- ADD (missing tab, fixed): 15 (2 trident, 3 odium shards, 3 malediction shards, 3 charged jewellery higher charges, 1 Lava scale, 4 Motherlode items, 1 Kraken tentacle, but some are double-counted in tab terms)
- REM: 0
- LOG: 130+ (clue scroll dups massive, Decorative armour dup, random event cosmetics, Cerberus pet jar)

## Classifier changes made in session 40

See `audit/classifier-changes.md` "Session 40".

---

## Session 41: IDs 12201–12500

### Ancient/Armadyl/Bandos god robes (Treasure Trails Hard)

- `12201-12203 Ancient stole, Ancient mitre` — OK (`mage`).
- `12253-12275 Armadyl/Bandos robes top/legs/stole/mitre/cloak/crozier` (12 items) — OK (`mage[+cape cross-tag]`).

### Bronze/Iron/Mithril trim & gilded armour variants

- `12205-12243 Bronze (g)/(t) and Iron (g)/(t) platebody/platelegs/plateskirt/full helm/kiteshield` (20 items) — **ADD melee** via pattern (cosmetic equipable=0 trim variants of standard smithed melee armour).
- `12277-12295 Mithril (g)/(t) platebody/platelegs/kiteshield/full helm/plateskirt` (10 items) — **ADD melee** (same).
- `12414-12418 Dragon (g) chainbody/platelegs/plateskirt/full helm/sq shield` (5 items) — **ADD melee**.

### Treasure Trails cosmetic head + clothing (mostly LOG)

- `12245-12251 Beanie, Red beret, Imp mask, Goblin mask` — LOG.
- `12297 Black pickaxe` — OK (`mining_smithing`).
- `12299-12307 White/Blue/Gold/Pink/Green headband` (5 items) — LOG.
- `12309-12313 Pink/Purple/White boater` — LOG.
- `12315-12349 Pink/Gold elegant shirt/legs/blouse/skirt + Crier hat + Cavalier (white/red/navy) + Briefcase + Sagacious spectacles` (~12 items) — LOG (Treasure Trails cosmetic).
- `12327-12333 Red d'hide body/chaps (g)/(t)` — OK (4 items, `range`).
- `12351-12399 Musketeer hat + Monocle + Big pirate hat + Katana ✓ + Leprechaun hat + Cat mask + 5 dragon masks + Canes (4) + Black d'hide (g)/(t) ✓ + Gilded scimitar ✓ + Gilded boots ✓ + Royal gown/crown + Partyhat & specs` (~28 items) — LOG/OK.

### Teleport tabs

- `12402-12411 Nardah/Digsite/Feldip hills/Lunar isle/Mort'ton/Pest control/Piscatoris/Tai bwo wannai/Iorwerth camp/Mos le'harmless teleport` — OK (10 items, `misc`).

### Infinity sets (Treasure Trails)

- `12419-12421 Light infinity hat/top/bottoms` — OK (3 items, `mage`).
- `12422 3rd age wand`, `12424 3rd age bow`, `12426 3rd age longsword` — OK.
- `12428-12434 Penguin mask, Afro, Top hat, Top hat & monocle` — LOG.
- `12436 Amulet of fury (or)` — **ADD melee;range;mage** (ornament variant of canonical Amulet of fury).
- `12437 3rd age cloak`, `12439 Royal sceptre` — OK.

### Black skirt + Black wizard set (g)/(t)

- `12441-12443 Musketeer tabard/pants` — LOG.
- `12445-12455 Black skirt (g)/(t), Black wizard robe (g)/(t), Black wizard hat (g)/(t)` (6 items) — **ADD mage** (same family as Blue wizard variants in session 28).
- `12457-12459 Dark infinity hat/top/bottoms` — OK (`mage`).

### Ancient/Armadyl/Bandos plate armour (Treasure Trails set)

- `12460-12488 Ancient/Armadyl/Bandos platebody/platelegs/plateskirt/full helm/kiteshield` (15 items, currently `melee;mining_smithing`) — **REM mining_smithing** (Treasure Trails set, not smithable).

### Ancient/Bandos d'hide armour (Treasure Trails range set)

- `12490 Ancient bracers` — **REM mage, ADD range**.
- `12492 Ancient d'hide body` — **REM melee, OK range (cross-tag crafting retained)**.
- `12494 Ancient chaps`, `12496 Ancient coif` — **REM melee, ADD range** (2 items).
- `12498 Bandos bracers` — **REM mage, ADD range**.
- `12500 Bandos d'hide body` — **REM melee, ADD range/crafting**.

---

## Session 41 totals

- Items reviewed: ~200
- OK (correct as-is): 50
- EX (correctly excluded): 5
- ADD (missing tab, fixed): 42 (35 trim/gilded melee armour, 6 Black mage cosmetic, 1 Amulet of fury (or))
- REM (misclassified, fixed): 22 (15 plate set from mining_smithing, 7 god d'hide pieces from melee/mage)
- LOG: 60+ (Treasure Trails fashion: headbands, boaters, elegant, cavalier, masks)

## Classifier changes made in session 41

See `audit/classifier-changes.md` "Session 41".

---

## Session 42: IDs 12501–12800

### Ancient/Armadyl/Bandos d'hide (session 41 fixes confirmed)

- `12502-12512 Bandos chaps/coif, Armadyl bracers/d'hide body/chaps/coif` (6 items) — OK (`range`, fixed in session 41).

### Treasure Trails cosmetic + ornament kits

- `12514 Explorer backpack`, `12516 Pith helmet` — LOG.
- `12518-12524 Green/Blue/Red/Black dragon mask` — LOG.
- `12526-12538 Fury ornament kit, Dark/Light infinity colour kit, Dragon sq shield/chainbody/legs/full helm/legs+skirt ornament kit` (8 items) — **ADD crafting** (ornament kits applied via crafting menu).
- `12540 Deerstalker` — LOG.

### Clue scroll (hard) dups

- `12542-12590 Clue scroll (hard) × 21 dups` — LOG.

### Treasure Trails Hard + Wilderness PvP rings

- `12592-12594 Black pick head, Broken pickaxe` — EX.
- `12596 Rangers' tunic` — **REM melee, ADD range** (clue scroll cosmetic range top).
- `12598 Holy sandals` — **ADD prayer** (clue trail cosmetic, prayer aesthetic).
- `12600 Druidic wreath` — **ADD prayer**.
- `12601 Ring of the gods` — OK.
- `12603-12605 Tyrannical ring, Treasonous ring` — OK (`melee`).

### God books (Treasure Trails Hard set rewards)

- `12607-12612 Damaged book × 3 + Book of war/law/darkness` — OK (`prayer[;mage]`).
- `12613-12624 Bandos/Armadyl/Ancient page 1-4` — OK (12 items, `prayer`).

### Stamina potion + halos + amylase

- `12625-12631 Stamina potion (4-1)` — OK (`herblore;agility_thieving`).
- `12633-12635 Stamina mix (2/1)` — OK (`herblore`).
- `12637-12639 Saradomin/Zamorak/Guthix halo` — OK (`melee`; Castle Wars reward head, defensive stats).
- `12640 Amylase crystal`, `12641 Amylase pack` — **ADD herblore** (2 items, Stamina potion ingredient).

### Pets (boss pets — LOG)

- `12642 Lumberyard teleport` — OK (`misc`).
- `12643-12655 Pet dagannoth supreme/prime/rex, Baby mole, Kalphite princess (✓ slayer), Pet smoke devil/kree'arra/general graardor/zilyana/k'ril tsutsaroth/Prince black dragon/Pet kraken` (~10 boss pets) — LOG (not in canonical pet sections).
- `12656 Junk` — EX.

### Mage / Clan wars / PvP

- `12658 Iban's staff (u)` — OK.
- `12659, 12675 Clan wars cape × 2` — LOG.
- `12691-12692 Tyrannical/Treasonous ring (i)` — OK (`melee`).
- `12693-12694 Kree'arra, Chaos elemental` — LOG (NPC entries).

### Super combat + boss minigame packs

- `12695-12701 Super combat potion (4-1)` — OK (`melee;herblore`).
- `12703 Pet penance queen`, `12727 Goblin paint cannon` — LOG.
- `12728-12738 Air/Water/Earth/Fire/Mind/Chaos rune pack` (6 items) — **ADD slayer** (Slayer master bulk packs like session 39).
- `12740-12744 Bird snare pack, Box trap pack, Magic imp box pack` (3 items) — **ADD slayer**.

### Wilderness Slayer + Whip variants

- `12746-12747 Archaic emblem (tier 1) × 2` — LOG.
- `12757-12763 Blue/Green/Yellow/White dark bow paint` — LOG (cosmetic).
- `12769-12771 Frozen whip mix, Volcanic whip mix` (2 items) — **ADD slayer** (Wilderness Slayer reward, modifies Abyssal whip).
- `12773-12774 Volcanic abyssal whip, Frozen abyssal whip` — OK (`melee`).

### Ancient Magicks teleports

- `12775-12782 Annakarl/Carrallanger/Dareeyak/Ghorrock/Kharyrll/Lassar/Paddewwa/Senntisten teleport` — OK (8 items, `misc`).

### Imbued + scrolls

- `12783 Ring of wealth scroll` — **ADD misc** (imbue scroll, single-use).
- `12785 Ring of wealth (i)` — OK.
- `12786 Magic shortbow scroll` — **ADD misc** (imbue scroll).
- `12788 Magic shortbow (i)` — OK.
- `12789 Clue box` — LOG.
- `12791 Rune pouch` — OK (`runecraft`).
- `12792-12793 Nest box (empty), Nest box (seeds)` (2 items) — **ADD wc_fletching** (Wintertodt-era bird nest containers).

### Dragon pickaxe + upgrade kits

- `12795-12797 Steam battlestaff, Mystic steam staff, Dragon pickaxe dups` — LOG.
- `12798 Steam staff upgrade kit`, `12800 Dragon pickaxe upgrade kit` — LOG (single-use cosmetic).

---

## Session 42 totals

- Items reviewed: ~150
- OK (correct as-is): 50
- EX (correctly excluded): 8
- ADD (missing tab, fixed): 27 (8 ornament kits, 2 prayer cosmetics, 2 amylase, 6 rune packs, 3 trap packs, 2 whip mix, 2 imbue scrolls, 2 nest box)
- REM (misclassified, fixed): 1 (Rangers' tunic)
- LOG: 50+ (Clue hard dups ×21, boss pets ×10, dragon mask cosmetic, paint variants)

## Classifier changes made in session 42

See `audit/classifier-changes.md` "Session 42".

---

## Session 43: IDs 12801–13100

### Ward + Ironman + Spirit shields

- `12802 Ward upgrade kit`, `12804 Saradomin's tear` — LOG (single-use upgrade).
- `12806-12807 Malediction/Odium ward dups` — LOG.
- `12809 Saradomin's blessed sword` — OK (`melee`).
- `12810-12815 Ironman/Ultimate ironman helm/platebody/platelegs` (6 items) — OK (`melee[;mining_smithing]`).
- `12816 Pet dark core` — LOG.

### Spirit shields + sigils

- `12817-12831 Elysian/Spectral/Arcane spirit shield + Spirit shield + Blessed spirit shield` — OK (5 items).
- `12819 Elysian sigil`, `12823 Spectral sigil`, `12827 Arcane sigil` (3 items) — **ADD melee** (Spirit shield construction materials).
- `12833 Holy elixir` — **ADD melee** (Blessed spirit shield material).

### Halloween 2015 Grim Reaper event

- `12835-12848 Community pumpkin, Grim reaper's diary/hood, Grim robe, Will and testament, Servant's skull, Hourglass, Scythe sharpener, Human eye, Voice potion, Target teleport scroll, Granite clamp` (~12 items) — EX/LOG (Halloween 2015 event).
- `12839 Human bones` — OK (`prayer`).
- `12851 Amulet of the damned (full)` — OK (`melee;range;mage`).

### Misc bulk packs + drink rewards

- `12854 Flamtaer bag` — LOG (Mort'ton container).
- `12855 Hunter's honour` — **ADD hunter** (NPC drink reward).
- `12856 Rogue's revenge` — **ADD agility_thieving** (NPC drink reward).
- `12857 Olive oil pack`, `12859 Eye of newt pack` (2 items) — **ADD slayer** (Slayer master bulk packs).

### Thanksgiving + Dwarf cannon set

- `12861 Thanksgiving dinner` — **ADD misc Holiday rares**.
- `12863 Dwarf cannon set` — LOG (GE convenience set).

### Grand Exchange armour sets

- `12865-12871 Green/Blue/Red/Black dragonhide set` — OK (4 items, `range` via dragonhide pattern).
- `12873-12883 Guthan's/Verac's/Dharok's/Torag's/Ahrim's/Karil's armour set` (6 items) — LOG (Barrows convenience sets; canonical pieces already classify).
- `12960-13062 Bronze/Iron/Steel/Black/Mithril/Adamant/Rune/Gilded/Saradomin/Zamorak/Guthix/Armadyl/Bandos/Ancient armour set (lg)/(sk)` (~50 items) — **ADD melee** (GE convenience sets unpack into canonical melee armour).

### Zulrah + toxic equipment

- `12885 Jar of sand` — LOG.
- `12887-12891 Santa mask/jacket/pantaloons/gloves/boots` (5 items) — **ADD misc Holiday rares**.
- `12892-12897 Antisanta mask/jacket/pantaloons/gloves/boots + coal box` (6 items) — **ADD misc Holiday rares**.
- `12899-12900 Trident of the swamp, Uncharged toxic trident` (2 items) — **ADD mage**.
- `12902-12904 Toxic staff (uncharged), Toxic staff of the dead` — OK (`melee`).
- `12905-12919 Anti-venom (4-1), Anti-venom+ (4-1)` — OK (8 items, `herblore`).
- `12921 Pet snakeling` — LOG.
- `12922 Tanzanite fang` — **ADD slayer** (Zulrah drop, blowpipe ingredient).
- `12926 Toxic blowpipe` — OK (`range`).
- `12927 Serpentine visage` — **ADD slayer** (Zulrah drop, Serpentine helm base).
- `12929 Serpentine helm (uncharged)` — **ADD melee**.
- `12931 Serpentine helm` — OK (`melee`).
- `12932 Magic fang` — **ADD slayer** (Zulrah drop, upgrades Trident).
- `12934 Zulrah's scales` — **ADD slayer** (Zulrah currency/serum).
- `12935-12936 Ohn's diary, Jar of swamp` — LOG.
- `12938 Zul-andra teleport` — OK (`misc`).

### Dragon defender + Cow-stume

- `12954 Dragon defender` — OK.
- `12955 Free to play starter pack` — LOG.
- `12956-12959 Cow top/trousers/gloves/shoes` (4 items) — LOG (Easter Cow-stume).

### Combat/Super potion sets + Achievement diary

- `13064-13066 Combat potion set, Super potion set` (2 items) — LOG (GE convenience sets).
- `13070 Achievement diary hood` — OK (`quests`).
- `13071 Chompy chick` — LOG.
- `13072-13073 Elite void top, Elite void robe` — OK (2 items, `melee;range;mage`).
- `13074-13077 Pharaoh's sceptre × 4 dups` — LOG.

### Crystal halberd + lyre

- `13079 Enchanted lyre(5)` — **ADD fishing** (5-charge variant of lyre family).
- `13080 New crystal halberd full (i)`, `13091 New crystal halberd full` (2 items) — **ADD melee** (post-Roving Elves crystal halberd).

---

## Session 43 totals

- Items reviewed: ~200
- OK (correct as-is): 55
- EX (correctly excluded): 25 (Halloween 2015 event content)
- ADD (missing tab, fixed): ~85 (4 spirit shield materials, 2 NPC drinks, 2 slayer packs, 12 Santa/Antisanta, 1 Thanksgiving, 2 toxic trident, 4 Zulrah drops, 1 Serpentine helm uncharged, 1 Enchanted lyre, 2 crystal halberd, ~50 GE armour sets melee)
- REM: 0
- LOG: 60+ (Barrows convenience sets, Halloween event, boss pets, dups, GE potion sets, Cow-stume)

## Classifier changes made in session 43

See `audit/classifier-changes.md` "Session 43".

---

## Session 44: IDs 13101–13400

### Achievement Diary rewards

- `13102 Teleport crystal (5)` — LOG (charge variant).
- `13103-13144 Karamja gloves 4, Varrock armour 1-4, Wilderness sword 1-4, Morytania legs 1-4, Falador shield 1-4, Ardougne cloak 1-4, Explorer's ring 1-4 (0 tabs!), Fremennik sea boots 1-4, Desert amulet 1-4, Kandarin headgear 1-4, Western banner 1-4` (43 items) — mostly OK.
- `13125-13128 Explorer's ring 1-4` (4 items) — **ADD quests** (Lumbridge Diary reward; quest cape teleport jewellery).

### Antique lamps + God book page sets + God dragonhide sets

- `13145-13148 Antique lamp × 4` — EX.
- `13149-13159 Holy/Unholy/Balance/War/Law/Darkness book page set` (6 items) — OK (`prayer`).
- `13161-13171 Zamorak/Saradomin/Guthix/Bandos/Armadyl/Ancient dragonhide set` (6 items) — OK (`range`).
- `13173-13175 Partyhat set, Halloween mask set` — **ADD misc Holiday rares** (2 items).

### Wilderness boss pets + Easter 2014 event

- `13177-13181 Venenatis spiderling, Callisto cub, Vet'ion jr., Scorpia's offspring` — LOG (boss pets).
- `13182-13185 Bunny feet, Empty/Incomplete/Easter blaster` (4 items) — **ADD misc Holiday rares**.
- `13186-13188 Volatile mineral, Package, Diango's claws` — EX/LOG.

### Bond + slayer packs

- `13190 Old school bond` — LOG.
- `13193 Bone bolt pack` — **ADD slayer**.
- `13195 Oddskull` — LOG.

### Zulrah helm variants + mutagens

- `13196 Tanzanite helm (uncharged)` — **ADD melee**.
- `13197 Tanzanite helm` — OK (`melee`).
- `13198 Magma helm (uncharged)` — **ADD melee**.
- `13199 Magma helm` — OK.
- `13200-13201 Tanzanite mutagen, Magma mutagen` (2 items) — **ADD slayer**.
- `13202 Ring of the gods (i)`, `13204 Platinum token` — OK.
- `13203 Mask of balance` — LOG.
- `13205 Rotten egg` — EX.

### Cosmetic + skill pets

- `13215-13218 Tiger/Lion/Snow leopard/Amur leopard toy` (4 items) — LOG (charity cosmetic).
- `13221-13223 Music cape, Music hood` — OK (`quests`).
- `13225 Tzrek-jad` — OK (`quests`).
- `13226 Herb sack` — OK (`herblore;misc`).

### Cerberus boots + crystals + Slayer key

- `13227-13231 Eternal/Pegasian/Primordial crystal` (3 items) — **ADD slayer** (Cerberus boss drop, boot ingredient).
- `13233 Smouldering stone` — OK (`slayer`).
- `13235-13239 Eternal boots (mage), Pegasian boots (multi), Primordial boots (melee)` — OK.
- `13241-13244 Infernal axe/pickaxe + uncharged variants` — OK.
- `13245 Jar of souls`, `13247 Hellpuppy` — LOG (Cerberus pet).
- `13248 Key master's key` — **ADD misc** (Cerberus access key).
- `13249 Key master teleport` — OK (`misc`).

### Slayer packs + Angler

- `13250-13254 Plant pot pack, Sack pack, Basket pack` (3 items) — **ADD slayer**.
- `13256 Saradomin's light` — LOG.
- `13258-13261 Angler hat/top/waders/boots` — OK (4 items, `fishing`).

### Abyssal Sire boss drops

- `13262 Abyssal orphan` — OK (`slayer`).
- `13263-13265 Abyssal bludgeon, Abyssal dagger` — OK (`melee`).
- `13273-13276 Unsired, Bludgeon spine/claw/axon` (4 items) — **ADD slayer** (Sire boss drops; Bludgeon production materials).
- `13277-13279 Jar of miasma, Overseer's book` — LOG.

### Max cape + Halloween 2016 Gravedigger

- `13280-13281 Max cape, Max hood` — OK (`quests`).
- `13283-13288 Gravedigger mask/top/leggings/boots/gloves, Anti-panties` (6 items) — **ADD misc Holiday rares** (Halloween 2016 event).
- `13302 Bank key` — LOG.
- `13307 Blood money` — OK.

### Skill pets + Misc + Max cape variants

- `13320-13324 Heron/Rock golem/Beaver/Baby chinchompa` — OK (4 items, skill pets).
- `13327 Rotten onion` — EX.
- `13328 Green banner` — LOG.
- `13329-13338 Fire/Saradomin/Zamorak/Guthix/Accumulator max cape + corresponding hoods` — heavy review:
  - Capes: Fire max cape `melee;range;mage`, Saradomin/Zamorak/Guthix max cape `mage`, Accumulator max cape `mage` — OK.
  - Hoods (0 tabs): Fire max hood, Saradomin/Zamorak/Guthix/Accumulator max hood (5 items) — **ADD quests** (Max hood family).

### Misc

- `13339 Sacred eel` — OK (`fishing`).
- `13340 Agility cape` — LOG (dup of canonical).
- `13343-13346 Black santa hat, Inverted santa hat, Anti-present, Present` (4 items) — **ADD misc Holiday rares**.
- `13348-13352 Vial of tears (1/2/3/full), Vial of sorrow` — EX (Drakan quest material).

### Lovakite + Juniper + Shayzien

- `13353 Gricoller's can` — OK (`farming`).
- `13354-13356 Lovakite bar, Juniper logs, Lovakite ore` — OK.
- `13357-13381 Shayzien gloves/boots/helm/greaves/platebody/body × 5 tiers` (~25 items) — OK (`melee[;range]`).
- `13382 Shayzien medpack` — LOG.

### Xerician + Servery

- `13383 Xerician fabric` — **ADD crafting** (Xerician robes material).
- `13385-13389 Xerician hat/top/robe` — OK (3 items, `mage`).
- `13391 Lizardman fang` — **ADD slayer** (lizardman drop, Xeric's talisman ingredient).
- `13393 Xeric's talisman` — OK (`mage;runecraft;misc`).
- `13394-13400 Gang meeting info, Intelligence, Training manual, Servery flour/pastry dough/raw meat/dish` — EX (Recipe for Disaster Servery + quest material).

---

## Session 44 totals

- Items reviewed: ~250
- OK (correct as-is): 100
- EX (correctly excluded): 20
- ADD (missing tab, fixed): 41 (4 Explorer's ring, 2 Holiday set, 4 Easter blaster, 1 Bone bolt pack, 2 Tanzanite/Magma uncharged, 2 mutagens, 3 boot crystals, 1 Key master's key, 3 plant pot packs, 4 Sire drops, 6 Gravedigger, 5 max hoods, 4 Christmas+Anti-present, 1 Xerician fabric, 1 Lizardman fang)
- REM: 0
- LOG: 60+ (boss pets, charity toy, mask of balance, banner, agility dup)

## Classifier changes made in session 44

See `audit/classifier-changes.md` "Session 44".

---

## Session 45: IDs 13401–13700

### RFD Servery + Servery quest material

- `13401-13418 Servery pie shell/Uncooked pie/Meat pie/Pizza base/Tomato/Incomplete pizza/Cheese/Uncooked pizza/Plain pizza/Pineapple/Pineapple chunks/Pineapple pizza/Cooked meat/Potato/Incomplete stew/Uncooked stew/Stew` (~17 items) — mostly EX/LOG; some cooking classified.

### Hosidius farming + Sorceress's Garden

- `13419 Sulphurous fertiliser`, `13420 Gricoller's fertiliser`, `13421 Saltpetre` (3 items) — **ADD farming** (Hosidius compost/fertiliser).
- `13423-13428 Golovanova/Bologano/Logavano seed ✓ farming + Golovanova/Bologano/Logavano fruit` — partial.
- `13426-13428 Golovanova fruit, Bologano fruit, Logavano fruit` (3 items) — **ADD cooking** (gnome-cooking fruits).

### Fossil Island fishing + Anglerfish

- `13429 Fresh fish` — LOG.
- `13430-13432 Bucket of sandworms, Sandworms, Sandworms pack` (3 items) — **ADD fishing** (Fossil Island bait).
- `13434-13438 Stolen pendant/garnet ring/circlet/family heirloom/jewelry box` — EX (random event).
- `13439-13443 Raw/cooked/Burnt anglerfish` — OK (3 items, `cooking[+fishing]`).

### Runecraft

- `13445 Dense essence block` — **ADD runecraft** (Arceuus Dense runestone intermediate).
- `13446 Dark essence block` — OK.

### Ensouled heads (Arceuus reanimation)

- `13448-13511 Ensouled goblin/monkey/imp/minotaur/scorpion/bear/unicorn/dog/chaos druid/giant/ogre/elf/troll/horror/kalphite/dagannoth/bloodveld/tzhaar/demon/aviansie/abyssal/dragon head` (22 items) — OK (`prayer` via `name_starts("Ensouled ")`).

### Arceuus library

- `13513 Book of arcane knowledge` — OK (`prayer`).
- `13514-13521 Dark manuscript × 7` — LOG (Arceuus library quest dialogue/lore books).
- `13524-13537 Rada's census, Ricktor's diary (7), Eathram & rada extract, Killing of a king, Hosidius letter, Wintertodt parable, Twill accord, Byrne's coronation speech, Ideology of darkness, Rada's journey, Transvergence theory, Tristessa's tragedy, Treachery of royalty, Transportation incantations` (~14 items) — LOG (Arceuus library cosmetic lore books).

### Shayzien supply armour (Hosidius favor)

- `13538-13562 Shayzien supply gloves/boots/helm/greaves/platebody × 5 tiers` (25 items) — **ADD melee** (Shayzien Combat XP gear; canonical Shayzien armour already classifies melee).
- `13563 Shayzien supply crate`, `13565-13569 Shayzien supply set (1-5)` (6 items) — LOG (containers/sets).

### Lovakengj mining + dynamite

- `13570 Juniper charcoal` — **ADD firemaking** (Lovakengj sulphurous charcoal).
- `13571 Volcanic sulphur` — **ADD mining_smithing**.
- `13572-13573 Dynamite pot, Dynamite` (2 items) — **ADD mining_smithing**.
- `13575 Blasted ore` — **ADD mining_smithing**.
- `13576 Dragon warhammer` — OK (`melee`).
- `13578 Arceuus icon` — LOG.

### Graceful set Kourend region colour variants

- `13579-13637 Graceful hood/cape/top/legs/gloves/boots × 5 Kourend region colour sets` (~36 items, 6 pieces × 6 regional sets including the original) — LOG (all have the same display name "Graceful hood/cape/..." as canonical 11850 etc.; classifier deduplicates by name).
- `13667-13677 More Graceful variants` (6 items) — LOG (same name dups).

### Misc + Farmer's outfit + clue bottles

- `13639 Seed box` — OK (`misc`).
- `13640-13646 Farmer's boro trousers/jacket/shirt/boots/strawhat` — OK (5 items, `farming`).
- `13648-13651 Clue bottle (easy/medium/hard/elite)` (4 items) — **ADD misc** (Wilderness clue scroll containers).

### Dragon claws + cosmetic + Bunny set

- `13652 Dragon claws` — OK (`melee`).
- `13653-13654 Bird nest, Nest box (seeds)` — LOG (dups).
- `13655-13656 Gnome child hat, Present` — LOG/Holiday.
- `13657 Grape seed` — OK (`farming`).
- `13658 Teleport card` — LOG.
- `13660 Chronicle` — OK (`misc`).
- `13663-13666 Bunny top, Bunny legs, Bunny paws, Deadman teleport tablet` — partial; `13663-13665 Bunny top/legs/paws` (3 items) — **ADD misc Holiday rares**.
- `13666 Deadman teleport tablet` — LOG.

### Cabbage event

- `13679 Cabbage cape` — OK (`range`).
- `13680-13681 Cabbage rune, Cruciferous codex` — LOG (Cabbage Facepunch Bonanza event cosmetic).

---

## Session 45 totals

- Items reviewed: ~300
- OK (correct as-is): 80
- EX (correctly excluded): 25 (Servery quest material, random event)
- ADD (missing tab, fixed): 44 (3 farming fertiliser, 3 cooking fruits, 3 fishing sandworm, 1 runecraft dense block, 25 Shayzien supply, 1 firemaking, 4 mining_smithing Lovakengj, 4 clue bottle, 3 Bunny holiday)
- REM: 0
- LOG: 70+ (Arceuus library lore ×20, Servery material, Bunny variants alternative)

## Classifier changes made in session 45

See `audit/classifier-changes.md` "Session 45".

---

## Session 46: IDs 13701–19850 (large ID gap 13682–19472)

### Random event + Achievement Diary

- `19473 Bag full of gems` — LOG.
- `19476 Achievement diary cape` — OK.

### Ballista weapons + parts (Tirannwn / Monkey Madness II)

- `19478 Light ballista`, `19481 Heavy ballista`, `19484 Dragon javelin` — OK (`range`).
- `19570-19592 Bronze/Iron/Steel/Mithril/Adamant/Rune/Dragon javelin tips, Javelin shaft, Light/Heavy frame, Ballista limbs` — OK.
- `19595-19607 Incomplete light/heavy ballista, Ballista spring, Unstrung light/heavy ballista` (5 items) — **ADD wc_fletching** (ballista construction intermediates).

### Zenyte gem chain

- `19493 Zenyte`, `19496 Uncut zenyte` — OK (`crafting[+mining_smithing]`).
- `19501 Zenyte amulet (u)` — OK (`wc_fletching`).
- `19529 Zenyte shard` — **ADD crafting** (zenyte gem raw material).
- `19532-19541 Zenyte bracelet/necklace/ring/amulet` — OK (4 items, `crafting`).
- `19544-19553 Tormented bracelet, Necklace of anguish, Ring of suffering, Amulet of torture` — OK (4 items, multi-tag).

### Monkey Madness II quest material

- `19505-19527 Mysterious note, Scrawled note, Translated note, Book of spyology ✓, Brush, Juice-coated brush, Handkerchief, Kruk's paw, Kruk monkey greegree, Satchel` — EX/LOG.
- `19556 Monkey`, `19558 Nieve` — LOG (NPC entries).
- `19559-19562 Elysian spirit shield dup, Charged onyx, Deconstructed onyx` — LOG.
- `19564 Royal seed pod` — LOG (Tree gnome stronghold tele).
- `19566-19569 Bronze key, Combat scarred/scratched/damaged key` — EX (Kruk's dungeon).

### Kourend teleports + Slayer helmets

- `19613-19631 Arceuus library/Draynor manor/Mind altar/Salve graveyard/Fenkenstrain's castle/West ardougne/Harmony island/Cemetery/Barrows/Ape atoll teleport` (10 items) — OK (`misc`).
- `19634-19637 Soul bearer, Damaged soul bearer, Soul journey` — LOG.
- `19639-19649 Black/Green/Red slayer helmet + (i)` — OK (6 items, `melee[;mage];slayer`).
- `19651 Hosidius teleport` — OK (`misc`).

### Botanical pie + Redwood logs

- `19653 Golovanova fruit top` — **ADD cooking** (Botanical pie filling).
- `19656 Uncooked botanical pie`, `19662 Botanical pie` — OK (`cooking`).
- `19665-19668 Damaged monkey tail, Minecart control scroll` — EX.
- `19669-19672 Redwood logs, Redwood pyre logs` — OK (`wc_fletching;firemaking`).

### Skotizo / Catacombs of Kourend

- `19675 Arclight` — OK (`melee`).
- `19677 Ancient shard` — OK (`misc`).
- `19679-19685 Dark totem base/middle/top, Dark totem` (4 items) — **ADD slayer** (Catacombs of Kourend boss key components).
- `19687 Helm of raedwald` — OK (`melee`).
- `19689-19697 Clue hunter garb/gloves/trousers/boots/cloak` — OK (5 items, `melee[;range]`).
- `19699 Hornwood helm` — LOG (cosmetic).
- `19701 Jar of darkness` — LOG.

### Compost + Clue nest + cosmetic

- `19704 Compost pack` — **ADD farming**.
- `19707 Amulet of eternal glory`, `19710 Ring of suffering (i)` — OK.
- `19712-19718 Clue nest (easy/medium/hard/elite)` (4 items) — **ADD misc** (Clue scroll bird nests).
- `19720 Occult necklace (or)` — **ADD mage** (Occult necklace ornament variant).
- `19722 Dragon defender (t)` — **ADD melee;quests** (Dragon defender trim variant).
- `19724-19727 Left eye patch, Double eye patch` — LOG (cosmetic).
- `19730 Bloodhound` — OK (`quests`).
- `19732 Lucky impling jar` — OK (`hunter`).

### Clue scroll massive dups

- `19734-19833 Clue scroll (medium/elite/easy/hard) × ~85 dups` — LOG.
- `19835 Clue scroll (master)` — OK (`misc`).
- `19836-19837 Reward casket (master), Torn clue scroll (part 1)` — LOG.
- `19840-19850 Clue scroll (hard) × 6 dups` — LOG.

---

## Session 46 totals

- Items reviewed: ~200
- OK (correct as-is): 50
- EX (correctly excluded): 20 (MM2 quest material, Skotizo combat key dups)
- ADD (missing tab, fixed): 19 (5 ballista construction, 1 Zenyte shard, 1 Golovanova fruit top, 4 Dark totem, 1 Compost pack, 4 Clue nest, 1 Occult necklace (or), 1 Dragon defender (t), 1 misc... actually count is right)
- REM: 0
- LOG: 110+ (massive clue scroll dups ~100, MM2 NPCs, soul bearer line)

## Classifier changes made in session 46

See `audit/classifier-changes.md` "Session 46".

---

## Session 47: IDs 19851–20200

### Clue scroll dups

- `19852-19910 Clue scroll (hard) × ~30 dups` — LOG.

### Mounted heads + Nunchaku

- `19912-19915 Zombie head, Cyclops head` — LOG (POH mounted trophy).
- `19918 Nunchaku` — OK (`melee`).

### God d'hide boots (Treasure Trails Hard)

- `19921-19936 Ancient/Bandos/Guthix/Armadyl/Saradomin/Zamorak d'hide boots` (6 items, currently `melee;range;mage`) — **REM melee/mage, keep range** (range armour set; defence stat spillover misclassifies into all 3 combat tabs).

### Heavy casket + Kourend faction scarves

- `19941 Heavy casket` — OK (`melee`).
- `19943-19955 Arceuus/Hosidius/Lovakengj/Piscarilius/Shayzien scarf` (5 items) — LOG (Kourend faction cosmetic scarves).

### Christmas 2017 Tuxedo event

- `19958-19985 Dark/Light tuxedo jacket/cuffs/trousers/shoes/bow tie` (10 items) — **ADD misc Holiday rares**.

### Blacksmith + Ranger + Holy wraps

- `19988 Blacksmith's helm` — OK (`melee`).
- `19991 Bucket helm` — LOG (cosmetic head).
- `19994 Ranger gloves` — OK.
- `19997 Holy wraps` — **ADD prayer** (cosmetic prayer wrap).

### Dragon scimitar (or) + ornament kits

- `20000 Dragon scimitar (or)` — **ADD melee** (ornament variant).
- `20002 Dragon scimitar ornament kit` — OK (`crafting`).
- `20005 Ring of nature`, `20017 Ring of coins` — LOG.
- `20008 Fancy tiara`, `20011 3rd age axe`, `20014 3rd age pickaxe` — OK.

### Demon masks + Samurai outfit

- `20020-20032 Lesser/Greater/Black/Old/Jungle demon mask` (5 items) — LOG (Treasure Trails cosmetic head).
- `20035-20047 Samurai kasa/shirt/gloves/greaves/boots` — OK (5 items, `melee[+range+mage]`).
- `20050 Obsidian cape (r)` — OK.
- `20053-20056 Half moon spectacles, Ale of the gods` — LOG.
- `20059 Bucket helm (g)` — LOG.

### Ornament kits (more)

- `20062-20077 Torture/Occult/Armadyl/Bandos/Saradomin/Zamorak godsword ornament kit` — OK (6 items, `crafting` via pattern).

### Halloween 2017 Mummy + Ankou

- `20080-20092 Mummy's head/body/hands/legs/feet` (5 items) — **ADD misc Holiday rares**.
- `20095-20107 Ankou mask/top/gloves/leggings/socks` (5 items) — **ADD misc Holiday rares**.
- `20110 Bowl wig` — LOG (cosmetic).

### Kourend hoods + Robe of darkness

- `20113-20125 Arceuus/Hosidius/Lovakengj/Piscarilius/Shayzien hood` (5 items) — LOG (Kourend faction cosmetic hood).
- `20128-20140 Hood/Robe top/Gloves/Robe bottom/Boots of darkness` — OK (5 items, `mage`).
- `20143 Dragon defender ornament kit` — OK.

### Gilded weapons + Steel (g)/(t) variants

- `20146-20152 Gilded med helm/chainbody/sq shield` (3 items, currently `melee;mining_smithing`) — **REM mining_smithing** (cosmetic clue trail variants, not smithable).
- `20155-20161 Gilded 2h sword, Gilded spear, Gilded hasta` — OK.
- `20164 Large spade`, `20166 Wooden shield (g)` — LOG.
- `20169-20196 Steel platebody/platelegs/plateskirt/full helm/kiteshield (g)/(t)` — OK (10 items, `melee` via trim/gilded pattern).
- `20199 Monk's robe top (g)` — OK (`prayer`).

---

## Session 47 totals

- Items reviewed: ~150
- OK (correct as-is): 40
- EX (correctly excluded): 0
- ADD (missing tab, fixed): 22 (10 tuxedo, 1 Holy wraps, 1 Dragon scimitar (or), 5 Mummy, 5 Ankou)
- REM (misclassified, fixed): 18 (12 god d'hide boots REM melee+mage, 3 Gilded helm/chain/shield REM mining_smithing)
- LOG: 60+ (massive clue hard dups ~30, faction scarves/hoods, demon masks, cosmetic items)

## Classifier changes made in session 47

See `audit/classifier-changes.md` "Session 47".

---

## Session 48: IDs 20201–20700

### Prayer robes + Team capes

- `20202 Monk's robe (g)` — OK (`prayer`).
- `20205-20208 Golden chef's hat, Golden apron` — LOG.
- `20211-20217 Team cape zero/x/i` — OK (3 items, `range`).

### Treasure Trails blessings

- `20220-20235 Holy/Unholy/Peaceful/Honourable/War/Ancient blessing` (6 items) — **ADD prayer** (clue scroll prayer-aesthetic items).

### Misc scrolls + cosmetic

- `20238 Charge dragonstone jewellery scroll` — **ADD misc Imbue scrolls**.
- `20240-20249 Crier coat, Crier bell, Black leprechaun hat, Clueless scroll` — LOG.

### Kourend banners

- `20251-20263 Arceuus/Hosidius/Lovakengj/Piscarilius/Shayzien banner` — OK (5 items, `mage`).

### Cosmetic + Cabbage event

- `20266-20269 Black/White unicorn mask` — LOG.
- `20272 Cabbage round shield` — OK (`melee`).
- `20275 Gnomish firelighter` — OK (`firemaking`).
- `20283-20338 (empty entries × ~17)` — EX (data placeholders).
- `20355 Light box` — OK (`misc`).

### Clue geodes + ornament kits

- `20358-20364 Clue geode (easy/medium/hard/elite)` (4 items) — **ADD misc Clue scrolls** (clue scroll container, like Clue nest).
- `20366 Amulet of torture (or)` — **ADD melee** (ornament variant).
- `20368-20374 Armadyl/Bandos/Saradomin/Zamorak godsword (or)` (4 items) — **ADD melee** (ornament variants).

### Steel trim/gold-trim sets

- `20376-20385 Steel trimmed set, Steel gold-trimmed set (lg)/(sk)` — OK (4 items, `melee` via GE pattern).

### Reward Chest dups + Ancient magicks tablet

- `20389-20408 Various dups (Dragon arrow, Shark, Prayer potion, Spear ✓, Abyssal whip, Dragon scimitar, Dragon dagger, Dark bow)` — LOG.
- `20422-20426 Rune platelegs, Black d'hide body/chaps, Mystic robe top/bottom dups` — LOG.
- `20430 Ancient magicks tablet` — **ADD mage** (Ancient teleport tablet).
- `20431 Ancient staff` — LOG (dup).

### Easter 2018 Evil chicken outfit

- `20433-20442 Evil chicken feet/wings/head/legs` (4 items) — **ADD misc Holiday rares**.

### Elder chaos + Slayer packs

- `20517-20520 Elder chaos top/robe` — OK (`mage`).
- `20523-20525 Catalytic rune pack, Elemental rune pack, Adamant arrow pack` (3 items) — **ADD slayer**.
- `20526-20527 Bloody key, Survival token` — LOG.

### Reward casket + Wintertodt

- `20543-20546 Reward casket (elite/hard/medium/easy)` (4 items) — **ADD misc Clue scrolls** (clue completion containers).
- `20548-20551 Super energy(4-1) dups` — LOG.
- `20557 Granite maul`, `20576-20599 various clue reward dups (3rd age robe top/robe, Climbing boots, Mithril gloves, Adamant gloves, Amulet of power, Amulet of glory, Rope, Stale baguette, Armadyl godsword, Bank filler, Ahrim's dups)` — LOG.
- `20595 Elder chaos hood` — OK (`mage`).

### Rune arrow pack + signets + PvP

- `20607 Rune arrow pack` — **ADD slayer**.
- `20608 Bloodier key` — LOG.
- `20609 Fairy enchantment`, `20611-20615 Ancient/Lunar/Arceuus signet` (4 items) — LOG (quest material).

### Skill pets + Wintertodt potions

- `20659-20693 Giant squirrel, Tangleroot, Rocky, Rift guardian, Phoenix` — OK (5 skill pets).
- `20695-20696 Bruma root, Bruma kindling` — OK (`firemaking`).
- `20697 Rejuvenation potion (unf)` — OK (`herblore`).
- `20698 Bruma herb` — **ADD firemaking** (Wintertodt herb).
- `20699-20700 Rejuvenation potion (4/3)` (2 items) — **ADD herblore** (Wintertodt-only potion charge variants).

---

## Session 48 totals

- Items reviewed: ~200
- OK (correct as-is): 45
- EX (correctly excluded): 20 (empty entries, signet quest material)
- ADD (missing tab, fixed): 28 (6 blessings, 1 scroll, 4 Clue geode, 5 ornament weapons, 1 Ancient tablet, 4 Evil chicken, 4 slayer packs, 4 Reward casket, 1 Bruma herb, 2 Rejuvenation)
- REM: 0
- LOG: 50+ (Various clue reward dups, signets, cosmetic crier/leprechaun, unicorn masks)

## Classifier changes made in session 48

See `audit/classifier-changes.md` "Session 48".

---

## Session 49: IDs 20701–21000

### Wintertodt continued

- `20701-20702 Rejuvenation potion (2)/(1)` — OK (`herblore`).
- `20703-20722 Supply crate, Pyromancer set, Warm gloves, Tome of fire, Burnt page, Bruma torch, Emerald lantern` — OK.
- `20724 Imbued heart` — **ADD mage** (Magic level boost).

### Misc weapons + slayer pack

- `20727 Leaf-bladed battleaxe` — OK (`melee`).
- `20730-20739 Mist/Dust battlestaff + Mystic mist/dust staff` — OK (`mage;crafting`).
- `20742 Empty jug pack` — **ADD slayer**.

### Bologa's blessing (already caught) + Ardougne Diary

- `20747 Bologa's blessing` — OK (`prayer` via blessing pattern, session 48).
- `20749-20752 Zamorak's grapes, Zamorak's unfermented wine` — EX.
- `20754 Giant key` — LOG.
- `20756 Hill giant club` — OK.
- `20760 Ardougne max cape` — OK (`melee;mage`).
- `20764 Ardougne max hood` — **ADD quests** (Max hood variant).

### Banshee (Slayer Tower cosmetic) + Hardcore ironman

- `20773-20779 Banshee mask/top/robe, Hunting knife` — LOG (Slayer cosmetic Treasure Trails outfit).
- `20784 Dragon claws` — LOG (dup).
- `20786 Ring of wealth (i5)` — **ADD misc** (Imbued + 5-charge Ring of wealth).
- `20791 Extra supply crate` — LOG.
- `20792-20796 Hardcore ironman helm/platebody/platelegs` — OK.

### Misc events + Corrupted armour

- `20798-20801 Smelly journal, Kindling, Empty/Water-filled gourd vial` — EX/LOG.
- `20832-20836 Snow globe, Sack of presents, Giant present` (3 items) — **ADD misc Holiday rares**.
- `20838 Corrupted helm` — OK (`melee`).
- `20840-20846 Corrupted platebody/platelegs/plateskirt/kiteshield` (4 items, `melee;mining_smithing`) — **REM mining_smithing** (Last Man Standing reward, not smithable).
- `20849 Dragon thrownaxe` — OK (`range`).
- `20851 Olmlet` — OK (`quests`).

### Chambers of Xeric (CoX) — fish + bats

- `20853-20854 Cave worms, Burnt fish` — LOG.
- `20855-20868 Raw pysk fish (0), Pysk fish (0), Raw suphi fish (1), Suphi fish (1), Raw leckish fish (2), Leckish fish (2), Raw brawk fish (3), Brawk fish (3), Raw mycil fish (4), Mycil fish (4), Raw roqed fish (5), Roqed fish (5), Raw kyren fish (6), Kyren fish (6)` — partial:
  - Raw variants (7 items) ✓ cooking.
  - Cooked variants (`Pysk/Suphi/Leckish/Brawk/Mycil/Roqed/Kyren fish (0-6)`, 7 items) — **ADD cooking**.
- `20869 Burnt bat` — OK (`cooking`).
- `20870-20883 Raw guanic/prael/giral/phluxia/kryket/murng/psykk bat + cooked variants` — partial:
  - Raw variants ✓ cooking.
  - Cooked variants (7 items) — **ADD cooking**.

### CoX dungeon material

- `20884-20899 Keystone crystal, Cavern grubs, Creature keeper's journal, Nistirio's manifesto, Tekton's journal, Medivaemia blossom, Transdimensional notes, Vanguard judgement, Houndmaster's diary, Dark journal` — LOG (CoX dungeon-specific quest/lore material).

### CoX herbs (Chambers herbs)

- `20901-20902 Grimy noxifer, Noxifer` — **ADD herblore** (2 items, CoX herb).
- `20903 Noxifer seed` — OK (`farming`).
- `20904-20905 Grimy golpar, Golpar` — **ADD herblore** (2 items).
- `20906 Golpar seed` — OK.
- `20907-20908 Grimy buchu leaf, Buchu leaf` — **ADD herblore** (2 items).
- `20909 Buchu seed` — OK.
- `20910-20912 Stinkhorn mushroom, Endarkened juice, Cicely` — **ADD herblore** (3 items, CoX herblore secondary).

### CoX raid potions (massive — pattern-based)

- `20913-20984 Elder/Twisted/Kodai/Revitalisation/Prayer enhance/Xeric's aid` × 3 phases (-)(1-4), (1-4), (+)(1-4) (72 items) — **ADD herblore** via new pattern catching CoX potion family names.
- `20985-20996 Overload (-)(1-4), Overload (1-4) (dups), Overload (+)(1-4)` (12 items, 4 CoX dups of NZ Overload + 8 (-)/(+) variants):
  - Overload (-)(1-4), Overload (+)(1-4) — OK (`herblore` via NZ pattern session 39).
  - Overload (1-4) (4 items) — LOG (dups of NZ canonical at 11730-11733).

### CoX rewards

- `20997 Twisted bow`, `21000 Twisted buckler` — OK (`range`).

---

## Session 49 totals

- Items reviewed: ~300
- OK (correct as-is): 60
- EX (correctly excluded): 10
- ADD (missing tab, fixed): ~100 (Imbued heart, Ardougne max hood, Empty jug pack, Ring of wealth (i5), 3 Christmas presents, 14 CoX cooked fish+bats, 9 CoX herbs, 72 CoX raid potions)
- REM (misclassified, fixed): 4 (Corrupted plate × 4 from mining_smithing)
- LOG: 50+ (Banshee cosmetic, dragon claws dup, CoX dungeon material, Overload CoX dups, Stale baguette/dups)

## Classifier changes made in session 49

See `audit/classifier-changes.md` "Session 49".

---

## Session 50: IDs 21001–21300

### CoX boss drops + Ancestral

- `21003 Elder maul`, `21006 Kodai wand`, `21009 Dragon sword`, `21012 Dragon hunter crossbow`, `21015 Dinh's bulwark` — OK.
- `21018-21024 Ancestral hat, robe top, robe bottom` (3 items, `melee;mage`) — **REM melee, keep mage** (Ancestral is pure mage gear).
- `21027 Dark relic` — LOG.
- `21028-21033 Dragon harpoon, Infernal harpoon + uncharged` — OK.
- `21034 Dexterous prayer scroll` — **ADD prayer** (CoX learnable prayer scroll).
- `21036 Mallignum root plank` — EX (CoX raid material).
- `21043 Kodai insignia` — **ADD mage** (Kodai wand upgrade).
- `21046 Ancient tablet` — **ADD mage** (Ancient teleport tablet).
- `21047 Torn prayer scroll` — LOG.
- `21049 Ancestral robes set` — **ADD mage** (GE convenience set).

### Tale of the Righteous quest material

- `21052-21059 Manor key, Ruby/Emerald/Sapphire key, Notes × 3, Killer's knife` — EX.
- `21060 Bandos godsword` — LOG (dup).
- `21061-21076 Graceful set Kourend dups × 6` — LOG.

### Arcane prayer scroll + jewellery

- `21079 Arcane prayer scroll` — **ADD prayer**.
- `21081-21123 Opal/Jade/Topaz ring/necklace/amulet (u)/amulet/bracelet` — OK (~15 items, `crafting[;wc_fletching]`).

### Ring of returning + various necklaces

- `21126 Ring of pursuit`, `21140 Efaritay's aid` — LOG.
- `21129-21138 Ring of returning (1/2/3/5)` (4 items) — **ADD misc Teleport jewellery**.
- `21143 Dodgy necklace`, `21146-21155 Necklace of passage (1-5)` — OK.
- `21157 Necklace of faith` — **ADD prayer** (Prayer-restoring necklace).
- `21160 Amulet of bounty` — **ADD farming** (Farming patch yield boost).
- `21163 Amulet of chemistry` — OK (`herblore`).
- `21166-21175 Burning amulet (1-5)` — OK (`misc`).

### Slayer bracelets

- `21177 Expeditious bracelet` — **ADD slayer** (Slayer task XP speed).
- `21180 Flamtaer bracelet` — **ADD construction** (Mort'ton speed bracelet).
- `21183 Bracelet of slaughter` — **ADD slayer** (Slayer reward).

### Fire max cape + Lava staff variants

- `21186 Fire max cape` — LOG (dup).
- `21198 Lava battlestaff`, `21200 Mystic lava staff` — LOG (dups of canonical 3053/3054).
- `21202 Lava staff upgrade kit` — **ADD crafting** (already caught by Ornament kits section name_in).
- `21205 Elder maul` — LOG (dup).

### Birthday + Easter 2019 event

- `21208 Invitation list`, `21209 Birthday balloons`, `21211 4th birthday hat` — **ADD misc Holiday rares** (3 items, RuneScape 4th birthday).
- `21214 Easter egg helm` — **ADD misc Holiday rares**.
- `21216-21227 12 Easter egg flavour variants` — **ADD misc Holiday rares** (12 items).
- `21228-21239 12 Chocolate mix variants` — **ADD misc Holiday rares** (12 items).
- `21240-21252 Wester banana/papaya/lemon/sand/spices/Beef fillet/Sea salt/Gold fragment/Fluffy feathers/Wester fish/Rock/Wester chocolate/Egg mould` — EX (Easter 2019 quest material).

### Slayer rewards + Skotizo

- `21253 Farmer's strawhat` — LOG (dup).
- `21255 Slayer's staff (e)`, `21257 Slayer's enchantment` — OK.
- `21259-21263 Enchanted scroll, Enchanted quill, Mysterious orb, Antique lamp, Copper's crimson collar` — EX/LOG.
- `21264-21266 Purple slayer helmet + (i)` — OK.
- `21268 Slayer ring (eternal)`, `21270 Eternal gem` — OK.
- `21273 Skotos` — OK (`quests`).
- `21275 Dark claw` — **ADD slayer** (Skotizo boss drop, Arclight upgrade material).
- `21276 Skull sceptre (i)` — OK.

### Infernal max + Obsidian set

- `21279 Obsidian armour set` — **ADD melee** (GE convenience set; obsidian gear is melee).
- `21282 Infernal max hood` — **ADD quests** (Max hood variant).
- `21284 Infernal max cape` — **ADD melee;range;mage** (combat trophy max cape; canonical at 21285 already has these tags).
- `21285 Infernal max cape` — OK (`melee;range;mage`).
- `21291 Jal-nib-rek` — OK (`quests`).
- `21293 Infernal eel` — OK (`fishing`).
- `21295 Infernal cape` — OK (`melee;range;mage;misc`).
- `21297 Infernal cape` — LOG (dup).
- `21298 Obsidian helmet` — OK (`melee`).

---

## Session 50 totals

- Items reviewed: ~280
- OK (correct as-is): 70
- EX (correctly excluded): 30 (Tale of the Righteous quest, CoX raid material, Easter 2019 quest)
- ADD (missing tab, fixed): 60 (3 Ancestral REM melee + keep mage, 2 prayer scrolls, 3 mage CoX upgrades, 4 Ring of returning, 1 Necklace of faith, 1 Amulet of bounty, 3 slayer bracelets, 1 Flamtaer construction, 1 Lava upgrade kit, 28 Easter+Birthday holiday, 1 Dark claw, 1 Obsidian set, 1 Infernal max hood, 1 Infernal max cape)
- REM (misclassified, fixed): 3 (Ancestral hat/robe top/robe bottom from melee)
- LOG: 60+ (CoX dups, Graceful dups, jewellery family)

## Classifier changes made in session 50

See `audit/classifier-changes.md` "Session 50".

---

## Session 51: IDs 21301–21700

### Obsidian plate + Rogue's crate + Rainbow

- `21301-21304 Obsidian platebody, Obsidian platelegs` (2 items, `melee;mining_smithing`) — **REM mining_smithing** (TzHaar drops, not smithable).
- `21307 Rogue's equipment crate` — LOG (GE convenience set).
- `21308-21314 Red/Orange/Yellow/Green/Blue/Purple rainbow strand, Rainbow scarf` — LOG (Easter cosmetic).

### Amethyst + mining gloves

- `21316-21338 Amethyst broad bolts/javelin/arrow/fire arrow/bolt tips` — OK (`range;wc_fletching`).
- `21341 Unidentified minerals` — LOG (Motherlode random output).
- `21343-21345 Mining gloves, Superior mining gloves` (2 items) — **ADD mining_smithing**.
- `21347 Amethyst`, `21350 Amethyst arrowtips`, `21352 Amethyst javelin tips` — OK.

### Misc + Wilderness + Tempoross

- `21354 Hand fan` — LOG.
- `21356 Minnow` — **ADD fishing** (Tempoross fishing).
- `21387 Master scroll book (empty)` — LOG.
- `21392 Expert mining gloves` — **ADD mining_smithing**.
- `21394 Karambwanji` — LOG (dup).
- `21396-21412 Clan wars cape × 2 dups` — LOG.
- `21428 Wilderness cape` — OK (`range`).
- `21433 Wilderness champion amulet` — **ADD misc**.
- `21434 Wilderness cape` (dup) — LOG.
- `21439 Champion's cape` — OK (`misc`).

### Tithe Farm + Seaweed + Bird houses

- `21469-21488 Teak/Mahogany seedling/sapling, Ultracompost, Teak/Mahogany seed` — OK (`farming`).
- `21490 Seaweed spore` — **ADD farming** (Seaweed underwater farming).
- `21504 Giant seaweed` — OK (`crafting`).
- `21509 Herbi` — OK (`herblore`).
- `21512-21521 Bird house, Oak/Willow/Teak bird house` (4 items) — **ADD hunter** (Fossil Island bird house traps).

### Fossil Island quest + Volcanic mine

- `21524-21527 Clue scroll dups` — LOG.
- `21528-21529 Sawmill proposal, Sawmill agreement` — EX.
- `21530-21531 Bone charm, Potion of sealegs` — LOG.
- `21532-21538 Iron/Silver/Coal/Gold/Mithril/Adamantite/Runite ore fragment` (7 items) — **ADD mining_smithing** (Volcanic Mine raw fragments).
- `21539 Heat-proof vessel` — **ADD mining_smithing** (Volcanic Mine tool).
- `21540 Large rock` — LOG.
- `21541 Volcanic mine teleport` — OK (`misc`).

### Fossil Island compost + enriched bones

- `21543-21545 Calcite, Pyrophosphite` (2 items) — **ADD farming** (Fossil Island Volcanic compost ingredient).
- `21547-21553 Small/Medium/Large/Rare enriched bone` — OK (4 items, `prayer`).

### Numulite + fossils (Fossil Island museum)

- `21555 Numulite` — **ADD misc** (Fossil Island currency).
- `21562-21568 Unidentified small/medium/large/rare fossil` (4 items) — **ADD prayer** (Fossil Island museum identification system).
- `21570-21620 Small/Medium/Large/Rare fossilised limbs/spine/ribs/pelvis/skull/tusk` (~22 items) — **ADD prayer** (POH prayer altar fossils).
- `21590-21598 Fossilised roots/stump/branch/leaf/mushroom` (5 items) — **ADD construction** (POH garden display fossils).

### Volcanic ash + Sulliuscep + Wyvern shield

- `21622 Volcanic ash` — OK (`mining_smithing`).
- `21624 Hoop snake`, `21626 Sulliuscep cap` — partial.
- `21626 Sulliuscep cap` — **ADD wc_fletching** (Sulliuscep mushroom tree wood).
- `21629-21640 Archaeologist's diary, Ancient diary, Antique lamp` — EX.
- `21633 Ancient wyvern shield` — OK (`melee;mage`).
- `21634 Ancient wyvern shield` (dup) — LOG.
- `21637 Wyvern visage` — **ADD slayer** (Ancient wyvern shield craft material).
- `21643 Granite boots`, `21646 Granite longsword`, `21649 Merfolk trident` — OK (`melee`).

### Drift net + Fossil Island fishing

- `21652 Drift net` — **ADD fishing** (Drift Net underwater fishing).
- `21655 Pufferfish` — LOG (Volcanic Mine fish, minor).
- `21656 Mermaid's tear` — LOG.
- `21662-21682 Various Fossil Island lore notes (scribbled, ancient, partial, experimental, paragraph, musty, hastily scrawled, old writing, short note)` — EX (~10 items, lore-only).

### Mushroom pie + Tzhaar pack

- `21684 Uncooked mushroom pie`, `21690 Mushroom pie` — OK (`cooking`).
- `21693 Bowl of fish` — **ADD cooking** (Volcanic Mine food).
- `21695 Runefest shield` — LOG (event reward).
- `21697 Ash covered tome` — OK (`mage`).
- `21698 Tzhaar air rune pack` — **ADD slayer** (Slayer master rune pack).

---

## Session 51 totals

- Items reviewed: ~250
- OK (correct as-is): 50
- EX (correctly excluded): 15 (Fossil Island lore notes, quest material)
- ADD (missing tab, fixed): ~60 (3 mining gloves, 1 Minnow, 1 Wilderness amulet, 1 Seaweed spore, 4 Bird houses, 7 ore fragments, 1 Heat-proof vessel, 2 Calcite/Pyrophosphite, 1 Numulite, 4 Unidentified fossil, 22 Fossilised body parts, 5 Fossilised plant parts, 1 Sulliuscep, 1 Wyvern visage, 1 Drift net, 1 Bowl of fish, 1 Tzhaar pack, 1 Expert mining gloves)
- REM (misclassified, fixed): 2 (Obsidian platebody/platelegs from mining_smithing)
- LOG: 50+ (Rainbow strands, clue dups, Karambwanji dup, Pufferfish, Hoop snake, lore notes, Sawmill proposal)

## Classifier changes made in session 51

See `audit/classifier-changes.md` "Session 51".

## Resume marker

**Next session: start from ID 21701.**
