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
- **Reviewed cumulative**: 2113 (17.8%)
- **Resume from**: ID 4001

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

## Resume marker

**Next session: start from ID 4001.**
