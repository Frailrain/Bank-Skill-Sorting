# Classifier changes — exhaustive item audit

Each change here was triggered by a per-item review in `out/audit-log.md`.

## Session 1 (audit IDs 0–250)

### `_HERBS` list — canonical OSRS names

**Before**: `["Guam", "Marrentill", ..., "Ranarr", ..., "Irit", ...]` — using shorthand names.

**After**: `["Guam leaf", "Marrentill", ..., "Ranarr weed", ..., "Irit leaf", ...]` — full canonical OSRS names.

**Affects**: clean herb classifier (`_is_clean_herb` exact-match against `_HERBS`).
- Previously: `Guam leaf` (id 249) did not classify because list had `"Guam"`.
- Now: clean herbs all classify correctly.

**Examples**: Guam leaf (249), Ranarr weed, Irit leaf.

Also updated `_herb_sort_key` to handle case insensitivity (Grimy items lower-case the herb word: "Grimy guam leaf").

### HERBLORE "Vials & secondaries" — added missing secondaries

**Before**: 24 names in the explicit allowlist.

**After**: 24 + 12 = 36 names. Added:
- `Unicorn horn`, `Unicorn horn dust` (antipoison secondary)
- `Blue/Red/Green/Black/Mithril dragon scale` (antifire secondaries)
- `Weapon poison`, `Weapon poison(+)`, `Weapon poison(++)`
- `Weapon poison (unf)` variants

**Affects**: ~12 items previously unclassified now in herblore.

### HERBLORE "Prayer & restores" — added plain `Restore` family

**Before**: `_is_potion_family("prayer potion", "super restore", "sanfew", "saradomin brew")`.

**After**: added `"restore potion"`.

**Affects**: Restore potion(3/2/1) (IDs 127, 129, 131) and the (4)-dose at 2434. Previously unclassified.

### FISHING — new "Fishing potions (cross-tag)" section

**Before**: Fishing tab had no potion section. Fishing potion (151-155, 2438) only classified in herblore.

**After**: added section with `_is_potion_family("fishing potion")` + `_potion_sort_key`.

**Affects**: 4 Fishing potion dose variants now cross-tagged into fishing.

### SLAYER Cannon section — added `Steel cannonball`

**Before**: cannon name list had `Cannonball`, `Granite cannonball` only.

**After**: added `Steel cannonball`.

**Affects**: ID 2 (Steel cannonball) now correctly in slayer (was only melee+range).

### WC_FLETCHING "Bolt tips" — also matches no-space plural

**Before**: `_name_ends(" bolt tips")` only.

**After**: OR with explicit `Barb bolttips` (historical naming without space, plural).

**Affects**: ID 47 (Barb bolttips) now classified.

### MELEE "Helmets" / "Body armour" — `Khazard helmet/armour` force_exclude

**Before**: both items matched `_slot_pred("head"/"body")` because they have stub defence stats.

**After**: `force_exclude=["Khazard helmet"]` / `["Khazard armour"]` on the respective sections.

**Affects**: 2 items removed from melee (quest disguise items with no real combat use).

### QUESTS — new "Quest cosmetic gear" section

**Before**: No catch-all for cosmetic quest-only equipment.

**After**: Added section with `Khazard helmet`, `Khazard armour`, `Fishing trophy`.

**Affects**: 3 items now correctly in quests.

---

## Session 2 (audit IDs 251–500)

### MELEE "Weapons" — `Cattleprod` force_exclude

**Before**: Cattleprod (ID 278) has `weapon_type='stab_sword'` so the classifier put it in melee Weapons.

**After**: `force_exclude=["Cattleprod"]` on MELEE Weapons.

**Affects**: ID 278 removed from melee.

### QUESTS "Quest cosmetic gear" — added `Cattleprod`, `Giant carp`

**Before**: 3 items (`Khazard helmet`, `Khazard armour`, `Fishing trophy`).

**After**: + `Cattleprod`, + `Giant carp`.

**Affects**: 2 new items in quests. Cattleprod is Sheep Herder's quest cosmetic weapon (paired with REM from melee above). Giant carp is the Fishing Contest reward kept as a trophy.

### FISHING "Specialty fish" — removed `Bass`

**Before**: `Bass` was in the specialty fish list alongside the eels.

**After**: removed. `Bass` is the cooked variant; "Raw bass" is the actual catchable, which is already in Raw fish.

**Affects**: ID 365 (Bass) no longer in fishing — correctly only in `melee;range;mage;cooking`.

### COOKING "Misc cooked food" — added `Edible seaweed`, `Giant carp`, `Raw giant carp`

**Before**: `_name_starts("Cooked ")` only; force-excluded canonical cooked-fish names.

**After**: OR with explicit set `{"Edible seaweed", "Giant carp", "Raw giant carp"}`.

**Affects**: 3 items now classify into cooking.

### CRAFTING "Cut gems" — added `Oyster pearl`, `Oyster pearls`, `Pearl`

**Before**: 10 gem-name allowlist.

**After**: + `Oyster pearl`, `Oyster pearls`, `Pearl`.

**Affects**: 3 items now in crafting. Pearls are bowstring + jewellery secondaries.

---

## Session 3 (audit IDs 501–750)

### New module-level constants

- `_MIME_OUTFIT` — list of 20 Mime random-event cosmetic clothing names (5 colours × 4 pieces).
- `_QUEST_COSMETIC_MELEE` — names that have melee weapon stats but are quest-only cosmetics (Cattleprod, Khazard sets, Dark/Glowing dagger, Gnome amulet, Beads of the dead, etc.).

### MELEE Weapons — broader force_exclude

**Before**: `force_exclude=["Cattleprod"]`.

**After**: `force_exclude=_QUEST_COSMETIC_MELEE` (8 names).

**Affects**: Dark dagger (746), Glowing dagger (747), Khazard cell keys, etc. all removed from melee Weapons.

### MELEE Body/Legs/Boots/MAGE Helmets — Mime outfit force_exclude

**Before**: no exclusions for mime.

**After**: each section excludes the matching mime pieces (boots → MELEE Boots, robe top → MELEE Body, robe bottoms → MELEE Legs, hat → MAGE Helmets).

**Affects**: 20 mime random-event cosmetic items no longer in combat tabs.

### MELEE/RANGE/MAGE Amulets — `Beads of the dead` + `Gnome amulet` force_exclude

**Before**: no neck-slot exclusions.

**After**: MELEE Amulets excludes `Gnome amulet`, `Beads of the dead`. RANGE and MAGE Amulets exclude `Beads of the dead`.

**Affects**: 2 quest neck items removed from 3 tabs each.

### MAGE Tomes — `Shaman's tome` force_exclude

**Before**: `_name_ends(" tome")` matched everything.

**After**: + `force_exclude=["Shaman's tome"]` (Big Chompy quest item).

**Affects**: ID 729 removed from mage.

### FIREMAKING Lanterns — `Torch`, `Unlit torch`, `Lit torch`

**Before**: 7 names.

**After**: + Torch / Unlit torch / Lit torch.

**Affects**: 3 firemaking items now classified.

### FARMING Tools — `Trowel` removed

**Before**: `Trowel` was in the tool allowlist.

**After**: removed. The Trowel ID (676) is the Dig Site quest tool, a different item from the Farming trowel (which doesn't exist by that name — farming uses Seed dibber + Spade).

**Affects**: ID 676 no longer in farming.

### WC_FLETCHING Arrows — `Broken arrow` force_exclude

**Before**: `_name_ends(" arrow", " arrows")` matched `Broken arrow` (ID 687), a random-event drop.

**After**: + `force_exclude=["Broken arrow"]`.

**Affects**: ID 687 removed from wc_fletching.

### WC_FLETCHING Forestry items — narrower predicate + `Animal skull` exclude

**Before**: `"anima" in name` (substring) — matched `Animal skull` since it contains "anima".

**After**: `"anima " in name` (with trailing space — Anima/anima as a Forestry word, not anywhere as substring) + `force_exclude=["Animal skull"]`.

**Affects**: ID 671 removed from wc_fletching.

### PRAYER Robes — added `Druid's `

**Before**: `Monk's / Proselyte / Initiate / Devout` startswith.

**After**: + `Druid's ` startswith.

**Affects**: Druid's robe (538), Druid's robe top (540) now in prayer.

### QUESTS Quest cosmetic gear — broader

**Before**: 5 names (Khazard helmet/armour, Fishing trophy, Cattleprod, Giant carp).

**After**: 10 names. Added Gnome amulet, Beads of the dead, Dark dagger, Glowing dagger, Shade robe, Shade robe top.

**Affects**: 5 new items in quests.

### MISC Boss & quest jewellery — added `Ghostspeak amulet`

**Before**: 11 names.

**After**: + Ghostspeak amulet (frequently kept for re-equipping post-Restless Ghost).

### MISC — new "Cosmetic outfits / random events" section

**Before**: no place for mime random-event outfit.

**After**: section with the 20 mime outfit names.

**Affects**: 20 items now in misc (paired with the REM-from-combat excludes above).

---

## Session 4 (audit IDs 751–1000)

### MELEE Weapons — Machete variants force_exclude

**Before**: `_QUEST_COSMETIC_MELEE` had 8 names.

**After**: + Machete, Opal machete, Jade machete, Red topaz machete (12 total). Machetes are Karamja jungle navigation tools, not combat weapons.

**Affects**: 4 items removed from melee.

### MELEE Gloves — `Steel gauntlets` no longer excluded

**Before**: `_SKILLING_GAUNTLETS` included "Steel gauntlets".

**After**: removed. Steel gauntlets are Family Crest melee defensive gloves (+6 stab/slash/crush defence), not a skilling utility like Cooking/Goldsmith/Chaos.

**Affects**: Steel gauntlets (778) now classifies into melee Gloves via _slot_pred.

### MAGE Gloves — `Chaos gauntlets` force_include

**Before**: no force_includes.

**After**: + force_include `["Chaos gauntlets"]`. Family Crest member glove that boosts chaos spell damage; canonical mage equipment.

**Affects**: Chaos gauntlets (777) now in mage.

### MINING_SMITHING Tools — added `Goldsmith gauntlets`

**Before**: hammer / chisel / coal bag / gem bag / ore pack.

**After**: + Goldsmith gauntlets (Family Crest XP-boost glove for goldsmithing).

**Affects**: 1 item.

### WC_FLETCHING — new "Javelins (cross-tag with range)" section

**Before**: javelins were only in the range tab (auto via `_is_range_weapon_type`).

**After**: new section `_name_ends(" javelin")` in wc_fletching. Javelins are fletched from heads + shaft, so the cross-tag is correct.

**Affects**: 6 javelin items (Bronze..Rune) now cross-tagged.

### HUNTER Bait — added `Worm`, `Red vine worm`

**Before**: 5 raw meat / bait names.

**After**: + Worm + Red vine worm (bird snare baits).

**Affects**: 2 items.

### CRAFTING Leather — added animal hides

**Before**: Cowhide / Hard leather / Snake / Yak / etc.

**After**: + Bear fur, Grey wolf fur, Silk.

**Affects**: 3 items.

### CRAFTING — new "Misc crafting materials" section

**Before**: no place for Charcoal.

**After**: section with Charcoal.

**Affects**: 1 item.

### MISC — new "Utility / banked supplies" and "Holiday rares & cosmetics" sections

**Before**: rope and holiday items unclassified.

**After**: Utility section (Rope, Chronicle) + Holiday section (Christmas cracker, Highwayman mask, Easter egg, Halloween mask, Pumpkin, Disk of returning).

**Affects**: 8 items potentially picked up (depends on which exist in wiki canonical set).

---

## Session 5 (audit IDs 1001–1300)

### New module-level constants

- `_BASIC_COLOUR_CAPES` — Red/Black/Blue/Yellow/Green/Purple/Orange/Pink/White cape.
- `_DHIDE_ALL_NAMES` — 14 d'hide piece names + Leather vambraces + Studded body/chaps.
- `_QUEST_COSMETIC_CAPES` — ["Cape of legends"].
- `_HOLIDAY_RARES` — 17 holiday items including partyhat colours, halloween mask variants, santa hat, bunny ears.

### MELEE Capes — `Cape of legends` force_exclude

Quest cosmetic, not real combat gear.

### MELEE Body/Legs/Gloves — d'hide piece excludes

**Before**: d'hide vambraces/chaps/body could land in melee Gloves/Legs/Body via stat-sniff (they have minimal melee defence).

**After**: explicit `_DHIDE_ALL_NAMES` filtered into the right slot's force_exclude.

**Affects**: 12+ d'hide pieces no longer in melee. They stay in range (correct).

### RANGE Capes — basic colour capes + Cape of legends excluded

**Before**: `_is_range_armour_slot("cape")` predicate captured Red/Black/Blue capes (defence_ranged=2 vs others=1).

**After**: + `force_exclude=_BASIC_COLOUR_CAPES + _QUEST_COSMETIC_CAPES`.

**Affects**: 9+ cosmetic capes removed from range.

### MAGE Capes — Cape of legends excluded

Same as above.

### MISC Holiday section — expanded

**Before**: 6 names.

**After**: 17 (full `_HOLIDAY_RARES`).

**Affects**: 7 partyhats + Santa hat + Bunny ears + 3 halloween mask colours now classify into misc.

### QUESTS Quest cosmetic gear — added Cape of legends.

---

## Session 6 (audit IDs 1301–1600)

### `_is_wc_axe` — exclude battleaxes/greataxes

**Before**: `weapon_type == 'axe'` was sufficient for WC_FLETCHING Axes section.

**After**: also requires name doesn't contain `battleaxe / greataxe / great axe`. Same disambiguation we already applied to MELEE Weapons (session 1).

**Affects**: 8 battleaxe items (Iron/Steel/Black/Mithril/Adamant/Rune/Bronze/Dragon) removed from wc_fletching; they remain in melee.

### MELEE Gloves — `Klank's gauntlets` force_exclude

**Before**: Klank's gauntlets (Underground Pass quest gloves) matched `_slot_pred("hands")`.

**After**: + force_exclude.

**Affects**: 1 item.

### CRAFTING Crafting tools — jewellery moulds

**Before**: Chisel/Needle/Glassblowing pipe/Hammer/Spinning wheel/Lyres.

**After**: + Ring mould, Amulet mould, Necklace mould, Unholy mould, Tiara mould, Bracelet mould.

**Affects**: 4-6 items now in crafting.

### MISC Currency — `Archery ticket`

**Before**: 6 currency names.

**After**: + Archery ticket (Ranging Guild reward currency).

**Affects**: 1 item.

---

## Session 7 (audit IDs 1601–1900)

### CRAFTING — new "Jewellery (gem-set)" section

**Before**: only "Jewellery (silver)" and "Jewellery (gold)" matched name-startswith those metals.

**After**: new section matching `(ring|necklace|amulet|bracelet) endswith` AND `Sapphire/Emerald/Ruby/Diamond/Dragonstone/Onyx/Zenyte/Opal/Jade/Topaz/Pearl/Dragon/Black startswith`.

**Affects**: 12+ gem jewellery items (Sapphire ring, Emerald necklace, Ruby amulet, etc.) now classify into crafting.

### CRAFTING Crafting tools — expanded materials

**Before**: chisel/needle/glassblowing/spinning wheel/lyres + 6 moulds.

**After**: + Shears, Bronze wire, Bucket of sand, Woad leaf.

**Affects**: 4 items.

### WC_FLETCHING Darts — `Prototype dart`/`Prototype dart tip` force_exclude

Tourist Trap quest items wrongly tagged via dart-name pattern.

### COOKING Containers — Waterskin family

**Before**: vial/bowl/pot/jug/bucket only.

**After**: + Waterskin(4..1) and (0).

**Affects**: 5 items.

### COOKING Misc cooked food — kebab + cake recipe items

**Before**: name_starts("Cooked ") + edible seaweed + giant carp.

**After**: also matches Ugthanki meat, Uncooked cake, Pitta dough/bread,
Chopped tomato/onion/ugthanki, Onion&tomato / Ugthanki&onion /
Ugthanki&tomato, Kebab mix, Ugthanki kebab.

**Affects**: ~10 items.

---

## Session 8 (audit IDs 1901–2200)

### COOKING — major gnome cocktail/food expansion

`_GNOMEFOOD` constant grew from ~30 to ~75 names. Now covers:
- All bartending base spirits (Vodka, Whisky, Gin, Brandy).
- Cocktail tools (Cocktail guide / shaker / glass).
- Toad/leg gnome ingredients (Swamp toad, Toad's legs variants, Spicy/King worm).
- Gnome cooking tools/intermediates (Batta tin, Crunchy tray, Gnomebowl mould, Gianne's cook book, Gnome spice, Gianne dough, Odd/Burnt/Half baked/Raw gnomebowl, Unfinished bowl, Odd/Burnt crunchies).
- Fruit chunks/slices/rings (Lemon/Orange/Pineapple/Lime).
- Lime, Equa leaves, Dwellberries.

### COOKING "Gnome food / cocktails" section — broader match

**Before**: `_name_in(_GNOMEFOOD)` only.

**After**: OR with `_name_starts("Premade ")` and `_name_starts("Unfinished cocktail")` to catch every Premade-X and Unfinished-cocktail variant without enumerating each.

### COOKING Raw meat & ingredients — additions

+ Grain, Redberries, Cheese, Grapes, Chocolatey milk, Empty cup, Half full wine jug, Jug of bad wine, Unfermented wine, Incomplete stew, Uncooked stew, Uncooked curry.

### HUNTER Salamanders — `Swamp tar`

Used to make hunter tar variants.

### CRAFTING Misc crafting materials — Raw swamp paste / Swamp paste

Crafting recipe ingredients.

---

## Session 9 (audit IDs 2201–2500)

### COOKING Gnome food / cocktails — broader intermediate patterns

**Before**: `_name_in(_GNOMEFOOD)` OR `_name_starts("Premade ")` OR `_name_starts("Unfinished cocktail")`.

**After**: also `_name_starts("Unfinished batta")`, `_name_starts("Unfinished crunchy")`, `_name_starts("Half baked ")` and explicit allowlist for Odd batta/crunchies/gnomebowl, Pizza base/Incomplete pizza/Uncooked pizza, Wrapped oomlie, Pie dish.

**Affects**: ~15 items.

### AGILITY_THIEVING Agility shortcut tools — `Rock-climbing boots`

Death Plateau quest reward used for agility shortcuts.

### MELEE Body armour — `Carnillean armour` force_exclude

Hazeel Cult quest cosmetic, not real combat gear.

### QUESTS Quest cosmetic gear — `Carnillean armour`

### PRAYER Quest-related prayer items — removed `Diary`

Too generic; Hazeel Cult quest diary etc. is not a prayer item. The `name_starts("Book of ")` and `Unholy/Holy book` patterns still catch real prayer books.

---

## Session 10 (audit IDs 2501–2800)

### MISC Teleport jewellery — Ring of dueling family + utility rings

+ `Ring of dueling(1..8)` (8 charge variants).
+ `Ring of recoil`, `Ring of life` (defensive teleport rings).

**Affects**: 10 items.

### MINING_SMITHING Mining tools & bags — `Ring of forging`

Used in iron smelting (doubles success rate).

### MELEE Boots — `Ranger boots` force_exclude

Treasure trail range-spec boots; classifier picked up via melee-leaning defence stats.

### MELEE Helmets — `Robin hood hat` force_exclude

Same pattern — clue range-spec hat.

### RANGE Boots / Helmets — force_include for ranger gear

+ Ranger boots in RANGE Boots.
+ Robin hood hat in RANGE Helmets.

**Affects**: 2 items REM from melee, 2 items ADD to range.

---

## Session 11 (audit IDs 2801–3100)

### New module constants

- `_CAMO_OUTFIT` — 25 names (5 colours × 5 pieces) for the Camouflage random event.
- `_REAL_MIME_OUTFIT` — 5 names for the actual Mime random event (different from the colour-prefixed mage-arena outfit in `_MIME_OUTFIT`).

### MELEE Helmets/Body/Legs/Boots/Gloves — Camo + Mime excludes

All five melee armour sections now also `force_exclude` the corresponding Camo piece + real Mime piece for that slot.

**Affects**: 25 Camo items + 5 real Mime items removed from melee.

### MAGE Helmets — Camo hats + Mime mask

Same pattern: + Camo hats and `Mime mask` to `force_exclude`.

### MISC Cosmetic outfits / random events — expanded

**Before**: `_MIME_OUTFIT` only (20 items).

**After**: `_MIME_OUTFIT | _CAMO_OUTFIT | _REAL_MIME_OUTFIT | {Chompy bird hat, Firework}` — 50 items.

**Affects**: 30+ new items in misc.

### HERBLORE Vials & secondaries — `Mort myre stem`, `Mort myre pear`

Nature Spirit secondary materials. + to allowlist.

### CRAFTING Crafting tools — `Sickle mould`

Silver sickle mould.

### AGILITY_THIEVING — new "Energy / stamina potions (cross-tag)" section

`_is_potion_family("energy potion", "super energy", "stamina potion", "agility potion")` + `_potion_sort_key`.

**Affects**: ~16 items now cross-tag into agility_thieving (4 doses × 4 families).

---

## Session 12 (audit IDs 3101–3400)

### New module constant `_SPLITBARK_PIECES`

5 Splitbark armour piece names (helm/body/legs/gauntlets/boots).

### AGILITY_THIEVING Agility shortcut tools — `Spiked boots`

Used for ice-shortcut transitions.

### MELEE Shields — `Granite shield` force_include

Stat-wise classified as range (defence_ranged dominates), but canonically used as a melee tank shield. Cross-tag.

### MELEE Helmets/Body/Legs/Gloves/Boots — Splitbark force_exclude

Splitbark is hybrid mage armour with minor melee defence; canonical mage gear.

### PRAYER — new "Shades remains" section

`_name_ends(" remains")` catches Loar / Phrin / Riyl / Asyn / Fiyr remains (Shades of Mort'ton prayer XP items).

### CRAFTING Misc crafting materials — `Bark`, `Quicklime`

Crafting recipe ingredients.

---

## Session 13 (audit IDs 3401–3700)

### PRAYER — new "Pyre log oils" section

`_is_potion_family("olive oil", "sacred oil")` with `_potion_sort_key`. Catches 8 dose variants (Olive oil(4..1), Sacred oil(4..1)).

### MISC Clue tools — expanded with colour-coded keys

Now matches `Bronze/Steel/Black/Silver key startswith` AND `red/brown/crimson/black/purple endswith`.

**Affects**: 20 colour-coded treasure-trail keys now in misc.

### CONSTRUCTION materials — `Fine cloth`, `Flamtaer hammer`

Mahogany Homes / Shades of Mort'ton temple repair items.

---

## Session 14 (audit IDs 3701–4000)

### PRAYER Quest-related items — removed `Manual`

Generic quest book name; not a prayer item. Fremennik manual etc. were wrongly tagged.

### PRAYER — new "God pages (Treasure trail)" section

Matches `Saradomin/Zamorak/Guthix/Armadyl/Bandos/Ancient page` startswith AND ` 1/2/3/4` endswith.

**Affects**: 12 god-page items (3 main gods × 4 pages each, plus Armadyl/Bandos/Ancient gods covered for future-proofing).

---

## Session 15 (audit IDs 4001–4300)

### PRAYER Bone secondaries — pattern-based

**Before**: `_name_in({"Bonemeal", "Bonemeal pot", "Bucket of slime", "Vial of milk", "Zogre bonemeal", "Jogre bonemeal", "Mort myre fungus"})`.

**After**: `_or(name_ends(" bonemeal"), _name_in({"Bonemeal", "Bonemeal pot", "Bucket of slime", "Vial of milk", "Mort myre fungus"}))`.

**Affects**: 15+ bonemeal variants (Bat / Big / Burnt / Burnt jogre / Baby dragon / Dragon / Wolf / Small ninja / Medium ninja / Gorilla / Bearded gorilla / Monkey / Small zombie monkey / Large zombie monkey / Skeleton / Jogre / Zogre) now in prayer.

### PRAYER Holy symbols — `Book of haricanto` force_exclude

Ghosts Ahoy guide book matched the broad `name_starts("Book of ")` pattern; not a prayer item.

### SLAYER Task-specific gear — `Bag of salt`

Used to finish off Rockslugs.

### MISC Currency — `Ecto-token`

Ghosts Ahoy currency.

---

## Session 17 (audit IDs 4601–4900)

### Barrows armour cross-tab fixes (major)

The 6 Barrows sets all had pieces leaking into wrong tabs via stat-dominance:
- Ahrim's (mage set): hood/robetop/robeskirt were classifying into melee.
- Dharok's/Guthan's/Torag's (melee sets): helm/body/legs were classifying into range.
- Karil's (range set): coif was in melee; leathertop/leatherskirt were in mage.

**Fix**: introduced `_AHRIM_PIECES`, `_KARIL_PIECES`, `_DHAROK_BODY`,
`_GUTHAN_BODY`, `_TORAG_BODY` constants, and added per-slot force_excludes
in the wrong tabs + force_includes in the correct ones.

- MELEE Helmets: + force_exclude `Ahrim's hood`, `Karil's coif`.
- MELEE Body armour: + force_exclude `Ahrim's robetop`, `Karil's leathertop`.
- MELEE Legs: + force_exclude `Ahrim's robeskirt`, `Karil's leatherskirt`.
- RANGE Helmets: + force_include `Karil's coif`, force_exclude `Dharok's/Guthan's/Torag's helm`.
- RANGE Body: + force_include `Karil's leathertop`, force_exclude `Dharok's/Guthan's/Torag's platebody`.
- RANGE Legs: + force_include `Karil's leatherskirt`, force_exclude `Dharok's platelegs / Guthan's chainskirt / Torag's platelegs`.
- MAGE Body/Legs: + force_exclude `Karil's leathertop` / `Karil's leatherskirt`.

**Affects**: 13 Barrows pieces now classify into their canonical combat tab only.

### PRAYER Holy symbols — more book excludes

Added `Book of portraiture`, `Book of 'h.a.m'`, `Book of HAM` to the existing `Book of haricanto` exclude (4 total). These are Zogre Flesh Eaters / HAM Hideout quest books, not prayer items.

### HERBLORE Other potions — `Relicym's balm` family

Anti-disease potion. 4 dose variants.

### WC_FLETCHING Unstrung bows — also matches `Unstrung comp bow`, `Unstrung lyre`

Explicit allowlist for items without the `(u)` suffix.

---

## Session 18 (audit IDs 4901–5200)

### FARMING Seeds — removed `Marigold seed` force_exclude

The prior force_exclude was wrong; Marigold is a real farming flower seed.

### HUNTER Bait — `Bird's egg`

Falconry / nest drop used as bait.

---

## Session 19 (audit IDs 5201–5500)

### FARMING Tools — `Gardening trowel`, `Gardening boots`

Tool + outfit additions.

### `_is_seed` — also matches `Mushroom spore` and `Acorn`

Non-suffix farming seeds previously missed by the " seed" / " seedling" / " sapling" endswith.

### New `_is_harvest_produce` helper + FARMING "Harvest produce" section

Regex pattern matches `(Apples|Oranges|Strawberries|Bananas|Lemons|Limes|Pineapples|Potatoes|Onions|Cabbages|Tomatoes|Sweetcorn)(N)` for N digits — the harvest basket/sack item names.

Also explicit allowlist for `Basket`, `Empty sack`, `Filled plant pot`.

**Affects**: ~50 farming items now classified.

### COOKING "Harvest produce (cross-tag with farming)" section

Same regex pattern, cross-tagged into cooking since the produce items are also cooking ingredients.

---

## Session 20 (audit IDs 5501–5800)

### New `_ROGUE_PIECES` constant + force_excludes from combat tabs

5 Rogue equipment names. MELEE/RANGE/MAGE Helmets/Body/Legs/Gloves/Boots all add Rogue piece excludes.

**Affects**: 15 cross-tab removals (5 pieces × 3 combat tabs).

### COOKING Beverages — major expansion

`_name_in` allowlist + `_name_ends("(m)")` for matured beer + pattern for `<beer>(4)` charge variants. Captures Mature wmb, Greenman's ale, Dragon bitter, Moonlight mead, Axeman's folly, Chef's delight, Slayer's respite, Ale yeast, Calquat keg, plus all (m) and (4) variants.

**Affects**: ~16 items.

### COOKING Beverages — `Strawberry`

Singular fruit ingredient.

### SLAYER Slayer master items — `Abyssal book`

Abyss teleport / strange object related; slayer-flavoured.

### RUNECRAFT — new "RC accessories" section

Currently with `Binding necklace`. Useful for combination rune crafting.

---

## Session 21 (audit IDs 5801–6100)

### COOKING Beverages — expanded brew pattern

Beer-family startswith now includes Mind bomb / Axeman's folly / Chef's delight / Slayer's respite / Cider / Wizard's mind bomb. Endswith pattern now matches (4) OR (m4). Captures ~14 charge/mature variants.

Allowlist also added: Coconut milk, Apple mush.

### HERBLORE — new "Unfinished potion variants (extended)" section

Catches Weapon poison+ (unf), Weapon poison++ (unf), Antidote+ (unf), Antidote++ (unf).

### FARMING — new "Farm outputs / materials" section

Comprehensive allowlist of ~25 names: hops varieties, mushroom/barley/marigolds/nasturtiums/rosemary, tree-grown fruits (curry leaf, papaya, coconut, calquat, watermelon), tree leaves/roots drops, weeds, hay sack, scarecrow, amulet of nature/pre-nature amulet, Jute fibre, Willow branch.

### COOKING "Harvest produce (cross-tag with farming)" — extended

Now also matches tree-fruits + barley/mushroom for cooking cross-tag.

### MELEE Boots/Gloves — `Mourner boots`/`Mourner gloves` force_exclude

Mourning's End II disguise gear, not real combat gear.

### QUESTS Quest cosmetic gear — Mourner set

+ Mourner top / trousers / gloves / boots / cloak.

---

## Session 22 (audit IDs 6101–6400)

### New constants

- `_SPINED_PIECES` (5) — Dagannoth Kings range armour.
- `_SKELETAL_PIECES` (5) — Dagannoth Kings mage armour.
- `_SNAKESKIN_PIECES` (5) — Tai Bwo Wannai range armour.

### MELEE armour sections — major exclude additions

All 5 melee armour slot sections now exclude Spined/Skeletal/Snakeskin piece for their slot.

**Affects**: 12+ items removed from melee.

### RANGE armour sections — force_include additions

Spined helm/body/chaps/boots/gloves + Snakeskin bandana/chaps/boots/vambraces added to corresponding range slots. (Snakeskin body was already in range.)

### MAGE Legs — `Ghostly robe` force_include

The legs variant of Ghostly robe (id 6108) wasn't classifying via the standard predicate.

### CRAFTING Misc materials — Dagannoth crafting inputs

+ Rock-shell chunk/shard/splinter, Skull/Ribcage/Fibula piece.

### MISC Holiday rares — major expansion

+ Lederhosen top/shorts/hat, Frog token/mask, Royal frog tunic/leggings/blouse/skirt, Teleport crystal (1)/(2)/(3).

### MISC Currency — `Trading sticks`

Karamja currency.

### FARMING Farm outputs — Kelda hops, Gout tuber

Brewing hops + rare tuber farming.

### COOKING Beverages — `Kelda stout`

Brewing output.

### COOKING Misc cooked food — `Raw fishlike thing`, `Fishlike thing`

Fishing Trawler reward.

### CONSTRUCTION materials — Thatch spar variants

Tai Bwo Wannai repair material.

---

## Session 23 (audit IDs 6401–6700)

### MELEE Weapons — `_is_melee_weapon_with_blackjack_check` wrapper

Wraps `_is_melee_weapon` to also exclude blackjacks via `_is_blackjack` substring check. Resolves the (o)/(d) blackjack variants leaking into melee.

### MELEE Boots — `Flippers` force_exclude

Fishing Trawler cosmetic boots.

### HUNTER — new "Generic camo outfit" section

`Camo top`, `Camo bottoms`, `Camo helmet` — Hunter camo gear.

### HERBLORE — new "Spirits of Elid secondaries" section

`Ground guam`, `Ground seaweed` — Spirits of the Elid serum ingredients.

### SLAYER Task-specific gear — `Ice cooler`

Used on Ice giants.

### COOKING Beverages — `Ahab's beer`

Wanted! quest beer.

### FARMING Farm outputs — `White tree shoot`, `White tree fruit`

Garden of Tranquillity White tree outputs.

---

## Session 24 (audit IDs 6701–7000)

### COOKING Misc cooked food — additions

+ Baked potato, Potato with butter, Potato with cheese, Peach, Choc-ice.

### SLAYER Task-specific gear — `Slayer gloves`

Slayer reward gloves.

### SLAYER — new "Champion's Challenge scrolls" section

`_name_ends(" champion scroll")` catches all 11 champion scrolls.

### MELEE Legs — `Granite legs` force_include + RANGE Legs force_exclude

Granite is melee armour with high ranged-defence; was leaking to range.

### MISC Holiday rares — major expansion

+ Christmas baubles (Star/Box/Diamond/Tree/Bell + boxes), Bobble hat/scarf, Jester hat/scarf, Tri-jester hat/scarf, Woolly hat/scarf, Marionette handle, Blue/Green/Red marionette + `name_ends(" marionette")` pattern.

**Affects**: ~20 holiday items.

### QUESTS Quest cosmetic gear — `Rat pole`

Rat-Catchers quest cosmetic weapon.

### `_ORES` — Sandstone/Granite variant weights

+ Sandstone (2/5/10/20/32 kg), Granite (2/5 kg) — Pyramid Plunder + standard mining.

## Session 16 (audit IDs 4301–4600)

### `_BASIC_COLOUR_CAPES` — added 50 Team-N capes

`Team-1 cape` through `Team-50 cape` are Castle Wars team identifier cosmetics; have defence_ranged=2 like the basic colour capes and were mass-classified into range.

### MISC — new "Team capes (Castle Wars)" section + holiday expansion

- New "Team capes" section matching `name_starts("Team-") and name_ends(" cape")`.
- "Holiday rares & cosmetics" now also matches `name_ends(" sweets")` and the explicit Easter event names (Easter basket, Rubber chicken).

**Affects**: 50 team capes + 9 Easter event items.

### QUESTS Quest cosmetic gear — `Bearhead`

Mountain Daughter quest cosmetic helmet.

### MELEE Helmets — `Bearhead` force_exclude

(pairs with the QUESTS add above)

### FIREMAKING Lanterns — expanded variants

Added Empty/unfinished/frame variants of oil lamp, oil lantern, candle lantern, bullseye lantern + lantern lens.

**Affects**: 7+ items.

## Session 25 (audit IDs 7001–7300)

### COOKING "Misc cooked food" — major expansion

Added Hosidius potato variants (Chilli/Egg/Mushroom/Tuna potato), the RFD/gnome cooking recipe chain (Chilli con carne, Egg and tomato, Mushroom & onion, Tuna and corn, Minced meat, Spicy sauce, Chopped garlic, Uncooked egg, Scrambled egg, Sliced mushrooms, Fried mushrooms, Fried onions, Chopped tuna), and Big Chompy skewered foods (Roast rabbit, Skewered rabbit, Iron spit, Skewered chompy).

**Affects**: ~21 items.

### COOKING Beverages — `Braindeath 'rum'`

Cabin Fever pirate beverage; pirate quest reward, drinkable.

**Affects**: 1 item.

## Session 26 (audit IDs 7301–7600)

### COOKING "Misc cooked food" — RFD/Awowogei/Pirate Pete recipe ingredients

Added Brulee × 4 (RFD Evil Dave dessert), Ground giant crab meat + Ground cod (Pirate Pete fishcake intermediates), Enchanted egg/milk/flour (Sir Amik Cake of guidance), Red banana/Tchiki monkey nuts/Sliced red banana/Tchiki nut paste/Stuffed snake/Snake over-cooked (Awowogei recipe chain).

**Affects**: ~13 items.

### COOKING "Raw meat & ingredients" — Spicy stew flavouring

New pattern matching `(Red|Orange|Brown|Yellow) spice` ending in `(1)`, `(2)`, `(3)`, `(4)` — the four-charge dosed flavouring vials used to alter Spicy stew effects.

**Affects**: 16 items.

### RANGE Body/Legs — trim/gilded Studded armour force_include

Studded body (g/t) and Studded chaps (g/t) are equipable=0 in the data (Treasure Trails cosmetic variants) but visually identical to and worn alongside standard range armour.

**Affects**: 4 items.

### MAGE Helmets/Body/Legs — Blue wizard hat/robe + skirt (g/t) force_include

Blue wizard hat (g/t), Blue wizard robe (g/t), Blue skirt (g/t) — trim/gilded variants of basic mage gear. Equipable=0 in data but mage cosmetic.

**Affects**: 6 items.

### FARMING Tools — `Queen's secateurs`

Mole quest reward, max-harvest farming tool.

**Affects**: 1 item.

### MISC Holiday rares & cosmetics — Halloween zombie outfit

Added Zombie shirt/trousers/mask/gloves/boots (Halloween 2009 event cosmetic outfit).

**Affects**: 5 items.

## Session 27 (audit IDs 7601–7900)

### COOKING "Cooking tools" — tea ceremony additions

Added Kettle, Full kettle, Hot kettle, Teapot, Teapot with leaves, Empty cup, Porcelain cup, Tea leaves — tea-making chain tools/ingredients used in Penguin Hide and Seek + miscellaneous cooking content.

**Affects**: 8 items.

## Session 28 (audit IDs 7901–8200; effective range to 8022)

### SLAYER — new "Mounted heads (POH trophies)" section

Added 14 items: Crawling hand, Cockatrice head, Basilisk head, Kurask head, Abyssal head, Kbd heads, Kq head + Stuffed variants. Slayer monster trophy heads mountable in POH.

### FISHING — new "Trophy fish (POH mountable)" section

Added Big bass/swordfish/shark + Stuffed variants. Random fishing drops mountable in POH.

**Affects**: 6 items.

### CONSTRUCTION — new "Portraits & paintings (POH decoration)" section

Pattern `name_ends(" portrait")` or `name_ends(" painting")` catches Arthur/Elena/Keldagrim/Misc. portrait and Desert/Isafdar/Karamja/Lumbridge/Morytania painting (POH decoration).

**Affects**: 9+ items.

### MAGE — new "Spell tablets (POH lectern)" section

Added Bones to bananas/peaches, Enchant sapphire-or-opal/emerald-or-jade/ruby-or-topaz/diamond/dragonstone/onyx, Telekinetic grab. Magic spell tablets crafted at POH lectern.

**Affects**: 9 items.

### MISC Holiday rares — `Easter ring`

Cosmetic Easter event ring; Easter egg was already in _HOLIDAY_RARES.

**Affects**: 1 item.

### RUNECRAFT — new "Essence intermediates" section

Added Dark essence fragments, Dark essence block — Blood/Soul rune chiselling intermediates in the Arceuus runecraft chain.

**Affects**: 2 items.

## Session 29 (audit IDs 8417–8716)

### CONSTRUCTION — major expansion (5 new sections)

POH garden + study + bar + basic furniture coverage:

- **"Bagged plants & trees"**: pattern `name_starts("Bagged ")` — bagged trees (dead/nice/oak/willow/maple/yew/magic), bagged plants 1-3, bagged flowers (daffodils/bluebells/sunflower/marigolds/roses).
- **"Hedges"**: pattern `name_ends(" hedge")` — thorny/nice/small box/topiary/fancy/tall fancy/tall box hedges.
- **"Lecterns (POH study)"**: pattern `name_ends(" lectern")` + Mahogany eagle/demon names — Oak/Eagle/Demon/Teak eagle/Teak demon/Mahogany eagle/Mahogany demon lecterns.
- **"Globes, orreries, telescopes (POH study)"**: named list — Crystal ball, Elemental sphere, Crystal of power, Globe variants, Armillary sphere, Small/Large orrery, Oak/Teak/Mahogany telescope.
- **"Basic furniture (non-Oak/Teak/Mahogany)"**: named list covering Crude/Wooden/Rocking chair, Bookcase, Kitchen/Wood dining/Opulent table, Wooden/Gilded bench, Wooden/Four-poster/Gilded four-poster bed, Oak/Teak/Gilded clock, Shaving stands, Oak/Teak/Fancy teak/Mahogany/Gilded dresser, Shoe box, Gilded wardrobe, Beer/Cider barrel, Construction guide.

**Affects**: ~65 items.

## Session 30 (audit IDs 8717–9000)

### MISC Currency — `Warrior guild token`

Minigame entry currency for Warriors' Guild.

**Affects**: 1 item.

## Session 31 (audit IDs 9001–9300)

### AGILITY_THIEVING "Pyramid plunder" — treasure loot additions

Added Golden/Stone/Pottery scarab, Golden/Pottery/Stone statuette, Gold seal, Stone seal — Pyramid Plunder treasure rewards.

**Affects**: 8 items.

### MINING_SMITHING — new "Lunar Diplomacy ores/bars" section

Added Lunar ore, Lunar bar (Astral Altar quest mining/smithing chain).

**Affects**: 2 items.

### HERBLORE "Unfinished potion variants" — Magic essence

Added Magic essence (unf), Magic essence (1-4) — Hazeel Cult Magic potion intermediate.

**Affects**: 5 items.

### MAGE Capes / RANGE Capes — `Lunar cape` reclassification

Moved Lunar cape from range to mage (Lunar set is mage-themed; cape has defence_ranged=2 spillover that miscategorized it).

**Affects**: 1 item.

## Session 32 (audit IDs 9301–9600)

### WC_FLETCHING Crossbow parts — Blurite limbs, Bolt pouch, Bolt mould

Added 3 items to crossbow parts named list. Blurite limbs was missing from the canonical limbs list; Bolt pouch and Bolt mould are crossbow construction tools.

**Affects**: 3 items.

### WC_FLETCHING "Bolts (unfinished)" — spacing variant pattern

Pattern now matches both `bolts (unf)` (canonical, space) and `bolts(unf)` (no-space variant — Adamant bolts(unf) item 9380 uses this form).

**Affects**: 1+ items.

### COOKING "Misc cooked food" — `Mint cake`

Gnome Restaurant food.

**Affects**: 1 item.

## Session 33 (audit IDs 9601–9900)

### MELEE/RANGE/MAGE Capes — combat hood force_include

Combat hoods (Attack/Strength/Defence/Ranging/Magic/Hitpoints hood) were sitting in 0 tabs because the `_slot_pred("cape")` predicate requires the underlying item to be the cape slot piece; hoods are head slot.

Added force_includes:
- MELEE Capes: Attack hood, Strength hood, Defence hood, Hitpoints hood
- RANGE Capes: Ranging hood, Hitpoints hood
- MAGE Capes: Magic hood, Hitpoints hood

**Affects**: 6 items (Hitpoints hood triple-classifies via 3 force_includes).

### CONSTRUCTION "Basic furniture" — POH furniture + Construct. hood

Added Gilded/Marble/Magic cape rack, Oak/Teak/Mahogany toy box, Gilded/Marble magic wardrobe, Oak/Teak/M. treasure chest, Oak/Teak/Mahogany fancy dress box, Construct. hood.

**Affects**: 15 items.

## Session 34 (audit IDs 9901–10200)

### HUNTER — 5 new sections + Traps extension

- **Traps** (extended): Rabbit snare, Noose wand, Teasing stick, Butterfly jar, Falconer's glove, Imp-in-a-box (1/2) (7 items).
- **"Caught butterflies"**: Black warlock, Snowy knight, Sapphire glacialis, Ruby harvest (4 items).
- **"Chinchompas"**: Chinchompa, Red chinchompa, Black chinchompa (3 items).
- **"Kebbit furs (cleaned)"**: Dark/Polar kebbit fur, Feldip weasel fur, Common kebbit fur, Desert devil fur, Spotted kebbit fur, Dashing kebbit fur (7 items).
- **"Hunter furs (tatty)"**: Tatty larupia/graahk/kyatt fur (3 items).
- **"Kebbit byproducts"**: Kebbit spike, Long kebbit spike, Kebbit teeth, Kebbit claws (4 items).

**Affects**: 28 items.

### COOKING "Misc cooked food" — Big Chompy / RFD Skrach

Added Roast bird meat, Skewered bird meat, Roast beast meat, Skewered beast, Spicy tomato, Spicy minced meat.

**Affects**: 6 items.

### MISC Holiday rares — Halloween skeleton outfit

Added Jack lantern mask, Skeleton boots/gloves/leggings/shirt/mask.

**Affects**: 6 items.

### MELEE Gloves / MAGE Gloves — Red/Black spiky vambraces reclassification

Red/Black spiky vambraces colour variants have defence_magic but no melee defence stats, so they were getting matched into MAGE Gloves. Forced into MELEE Gloves (force_include) and excluded from MAGE Gloves (force_exclude).

**Affects**: 2 items.

## Session 35 (audit IDs 10201–10500)

### New constants: `_GOD_DHIDE_NAMES`, `_3RD_AGE_RANGE_NAMES`, `_3RD_AGE_MELEE_NAMES`

- `_GOD_DHIDE_NAMES`: Saradomin/Guthix/Zamorak d'hide body/chaps/coif/bracers (12 items).
- `_3RD_AGE_RANGE_NAMES`: 3rd age range top/legs/coif, 3rd age vambraces (4 items).
- `_3RD_AGE_MELEE_NAMES`: 3rd age platelegs/platebody/full helmet/kiteshield (4 items).

### RANGE Helmets/Body/Legs/Gloves — God d'hide + 3rd age range force_include

Added 12 god d'hide pieces + 4 3rd age range pieces to appropriate range armour sections (these have mixed defence stats that the predicate doesn't pick up).

**Affects**: 16 items.

### MELEE Helmets/Body/Legs/Gloves — God d'hide + 3rd age range force_exclude

Excluded god d'hide + 3rd age range pieces from melee sections to prevent miscategorization via defence stat spillover.

**Affects**: 16 exclusions (no item loss to melee since they belong in range).

### MAGE Gloves/Helmets/Body/Legs — God d'hide bracers + 3rd age range force_exclude

Bracers had defence_magic spillover putting them in mage; excluded explicitly.

**Affects**: 7 exclusions.

### MELEE "Smithing armour outputs (extended)" — 3rd age melee force_exclude

3rd age platebody/legs/full helmet/kiteshield are not smithable (clue scroll rare drops), so excluded from the smithing pattern.

**Affects**: 4 items moved out of mining_smithing (still classify into melee via slot predicate).

### MELEE Amulets / MAGE Amulets — trim variant force_includes

- MELEE: Strength amulet (t), Amulet of glory (t).
- MAGE: Amulet of magic (t), Amulet of glory (t).

**Affects**: 3 items (Glory trim cross-tags).

## Session 36 (audit IDs 10501–10800 + 10808–10900)

### MELEE Body armour/Helmets — Granite body/Granite helm reclassification

Granite body and Granite helm are melee armour but defence_ranged spillover put them in range. Force_included in MELEE Body armour/Helmets and force_excluded from RANGE.

**Affects**: 2 items.

### CRAFTING "Leather (raw → tanned)" — `Cured yak-hide`

Tanned form of yak-hide used in Fremennik Isles armour crafting.

**Affects**: 1 item.

### FARMING "Harvest produce" — sq'irk fruits

Added Spring/Summer/Autumn/Winter sq'irk (Sorceress's Garden fruit).

**Affects**: 4 items.

### COOKING Beverages — sq'irkjuice

Added Spring/Summer/Autumn/Winter sq'irkjuice (Sorceress's Garden potion, grants Thieving boost).

**Affects**: 4 items.

### MISC — new "Combat trophies (PvM rewards)" section

Added Fire cape, Infernal cape, Champion's cape — iconic combat trophy capes that primarily represent achievement (also worn for stats; canonical Fire cape 6570 already cross-tags into melee/range/mage Capes).

**Affects**: 3 items.

## Session 37 (audit IDs 10875–11200)

### WC_FLETCHING — new "Lumberjack outfit" section

Added Lumberjack boots/top/legs/hat — Random event woodcutting XP outfit.

**Affects**: 4 items.

### COOKING "Misc cooked food" — Temple Trekking food

Added Green gloop soup, Frogspawn gumbo, Frogburger, Coated frogs' legs, Bat shish, Fingers, Grubs à la mode, Roast frog, Mushrooms, Fillets, Loach, Eel sushi.

**Affects**: 12 items.

### MISC Holiday rares — Easter 2007 chicken outfit + chocolate kebbit

Added Chicken feet/wings/head/legs, Magic egg, Rabbit mould, Chocolate chunks, Chocolate kebbit.

**Affects**: 8 items.

### MISC Teleport jewellery — Castle wars / Inoculation / Abyssal bracelet

Added Castle wars bracelet (1-3), Inoculation bracelet, Abyssal bracelet (1/2/3/5).

**Affects**: 8 items.

### HERBLORE "Unfinished potion variants" — Sanfew serum Mixture step intermediates

Added Mixture - step 1 (1-4), Mixture - step 2 (1-4) — Dream Mentor Sanfew serum production chain intermediates.

**Affects**: 8 items.

### SLAYER "Slayer master items" — `Slayer bell`

Slayer task tracking utility.

**Affects**: 1 item.

## Session 38 (audit IDs 11201–11500)

### HUNTER Traps — impling jar tools

Added Jar generator, Magic butterfly net, Impling jar.

**Affects**: 3 items.

### SLAYER "Mounted heads" — `Elvarg's head`

Dragon Slayer quest trophy, POH-mountable.

**Affects**: 1 item.

### MELEE Shields — `Draconic visage`

Dragonfire shield base material; valuable to group with combat gear.

**Affects**: 1 item.

### FISHING "Specialty fish" — Barbarian Training catches + byproducts

Added Leaping trout/salmon/sturgeon (3-tick barbarian fishing catches) + Roe, Fish offcuts (Caviar production byproducts).

**Affects**: 5 items.

### HERBLORE — new "Barbarian mix potions" section

Pattern `name_ends("mix(1)")` or `name_ends("mix(2)")` catches all Barbarian Training brewable mix potions (Attack/Strength/Combat/Restore/Energy/Defence/Agility/Prayer/Superattack/Anti-poison supermix/Fishing/Super energy/Super str./Magic essence/Super restore/Super def./Antidote+/Antifire/Ranging/Magic/Hunting/Zamorak mix + (1)/(2) charge variants).

**Affects**: ~40 items.

## Session 39 (audit IDs 11525–11900)

### New constants: `_ARMADYL_NAMES`, `_BANDOS_NAMES`

- `_ARMADYL_NAMES`: Armadyl helmet/chestplate/chainskirt (range set, defence_magic spillover misclassifies them).
- `_BANDOS_NAMES`: Bandos chestplate/tassets (melee set, defence_ranged spillover misclassifies them).

### RANGE Helmets/Body/Legs — Armadyl force_include + Bandos force_exclude

Armadyl pieces force_included into range; Bandos pieces force_excluded.

### MELEE Helmets/Body armour/Legs — Armadyl force_exclude + Bandos force_include

Symmetric to above — Bandos force_included into melee; Armadyl force_excluded.

### MAGE Helmets/Body/Legs — Armadyl force_exclude

Armadyl had defence_magic stats that miscategorized as mage.

### MELEE — new "Godsword construction" section

Added Godsword shards (1&2, 1&3, 2&3), Godsword blade, Godsword shard 1/2/3, Armadyl/Bandos/Saradomin/Zamorak hilt.

**Affects**: 11 items.

### HERBLORE — new "Nightmare Zone potions" section

Pattern catches Super ranging (1-4), Overload (1-4), Absorption (1-4) + Herb box.

**Affects**: 13 items.

### RANGE "Ranging potions" — `super ranging` family

Extended potion family list to include super ranging (cross-tag with herblore NZ section).

**Affects**: 4 Super ranging dose variants (cross-tag).

### CONSTRUCTION POH teleports — `Scroll of redirection`

Re-routes POH teleport tabs to different POH locations.

**Affects**: 1 item.

### MISC Holiday rares — Halloween + Christmas event partyhats

Added Black h'ween mask, Black partyhat, Rainbow partyhat.

**Affects**: 3 items.

### WC_FLETCHING Arrowtips — `Broad arrowheads`

Slayer broad arrow craft material.

**Affects**: 1 item.

### SLAYER Slayer master items — bulk purchase packs

Added Empty/Water-filled vial pack, Feather pack, Bait pack, Broad arrowhead pack, Unfinished broad bolt pack.

**Affects**: 6 items.

## Session 40 (audit IDs 11901–12200)

### MAGE Staves — Trident force_include

Trident of the seas (full) and Uncharged trident force_included into MAGE Staves (Kraken slayer mage weapon; `_is_mage_weapon_type` doesn't pick them up by type).

**Affects**: 2 items.

### MAGE / RANGE — new "Ward shards (construction intermediates)" sections

- MAGE: Malediction shard 1/2/3 (3 items, construction intermediates for Malediction ward).
- RANGE: Odium shard 1/2/3 (3 items, construction intermediates for Odium ward).

**Affects**: 6 items.

### MISC Teleport jewellery — higher-charge variants

Added Combat bracelet(6), Skills necklace(6), Amulet of glory(5/7/8).

**Affects**: 4+ items.

### HERBLORE "Vials & secondaries" — `Lava scale`

Lava dragon scale (herblore secondary; sister item to Lava scale shard already in list).

**Affects**: 1 item.

### MINING_SMITHING — new "Motherlode Mine" section

Added Pay-dirt, Golden nugget, Soft clay pack — Motherlode Mine raw output + reward currency.

**Affects**: 3 items.

### SLAYER Task-specific gear — `Kraken tentacle`

Slayer boss drop; converts to Abyssal tentacle when combined with Abyssal whip.

**Affects**: 1 item.

## Session 41 (audit IDs 12201–12500)

### Extended `_GOD_DHIDE_NAMES` + new `_TREASURE_TRAIL_MELEE_NAMES`

- `_GOD_DHIDE_NAMES`: added Ancient/Armadyl/Bandos d'hide body/chaps/coif/bracers (12 more items).
- `_TREASURE_TRAIL_MELEE_NAMES`: new constant for Ancient/Armadyl/Bandos platebody/platelegs/plateskirt/full helm/kiteshield (15 items).

### MELEE Smithing armour outputs (extended) — Treasure Trail set force_exclude

Ancient/Armadyl/Bandos plate gear is not smithable; excluded from the smithing pattern.

**Affects**: 15 items (REM mining_smithing).

### MELEE — new "Trim/gilded armour cosmetic variants" section

New predicate `_is_trim_gilded_melee_armour` matching `name_ends("(g)")` or `name_ends("(t)")` for items prefixed with melee metal types (Bronze/Iron/Steel/Black/Mithril/Adamant/Rune/Dragon/etc) and containing melee armour keywords. Catches all the trim/gilded smithable armour variants.

**Affects**: ~35 items.

### MAGE Helmets/Body/Legs — Black mage cosmetic force_include

Black skirt (g)/(t), Black wizard robe (g)/(t), Black wizard hat (g)/(t) — same family as Blue wizard cosmetics from session 28.

**Affects**: 6 items.

### MELEE/RANGE/MAGE Amulets — `Amulet of fury (or)`

Ornament variant of Amulet of fury (already cross-tags into all 3 combat tabs via Amulet of fury canonical).

**Affects**: 1 item.

## Session 42 (audit IDs 12501–12800)

### CRAFTING — new "Ornament kits" section

Pattern `name_ends(" ornament kit")` + named list for Dark/Light infinity colour kit, Steam staff upgrade kit, Dragon pickaxe upgrade kit. Catches Fury ornament kit, Dragon sq shield/chainbody/legs+skirt/full helm ornament kits, etc.

**Affects**: 8+ items.

### RANGE/MELEE Body — `Rangers' tunic` reclassification

REM melee → ADD range force_include.

**Affects**: 1 item.

### PRAYER Robes — `Holy sandals`, `Druidic wreath`

Clue trail cosmetic prayer-themed items.

**Affects**: 2 items.

### HERBLORE Vials & secondaries — `Amylase crystal`, `Amylase pack`

Stamina potion ingredient (Motherlode Mine reward).

**Affects**: 2 items.

### SLAYER Slayer master items — additional packs + whip mixes

Added Air/Water/Earth/Fire/Mind/Chaos rune pack, Bird snare pack, Box trap pack, Magic imp box pack, Frozen whip mix, Volcanic whip mix.

**Affects**: 11 items.

### MISC — new "Imbue scrolls" section

Ring of wealth scroll, Magic shortbow scroll.

**Affects**: 2 items.

### WC_FLETCHING Bird nests — Nest box variants

Added Nest box (empty), Nest box (seeds) to bird nests section.

**Affects**: 2 items.

## Session 43 (audit IDs 12801–13100)

### MELEE — new "GE armour sets (melee)" section

Pattern `name_ends(" set (lg)")` or `name_ends(" set (sk)")` (excluding dragonhide via _not contains, and Karil's/Ahrim's force_excluded). Catches all Grand Exchange convenience sets that auto-unpack into melee armour.

**Affects**: ~50 items.

### MELEE — new "Spirit shield construction" section

Added Elysian sigil, Spectral sigil, Arcane sigil, Holy elixir (spirit shield production materials).

**Affects**: 4 items.

### MELEE — new "Crystal halberd" section

Added New crystal halberd full, New crystal halberd full (i).

**Affects**: 2 items.

### MELEE Helmets — `Serpentine helm (uncharged)` force_include

**Affects**: 1 item.

### MAGE Staves — toxic trident force_include

Added Trident of the swamp, Uncharged toxic trident.

**Affects**: 2 items.

### SLAYER Slayer master items — more packs + Zulrah drops

Added Olive oil pack, Eye of newt pack, Tanzanite fang, Serpentine visage, Magic fang, Zulrah's scales.

**Affects**: 6 items.

### HUNTER — new "Hunter rewards" section

Added Hunter's honour (NPC drink reward).

**Affects**: 1 item.

### AGILITY_THIEVING Thieving accessories — `Rogue's revenge`

NPC drink reward.

**Affects**: 1 item.

### MISC Holiday rares — Santa/Antisanta + Thanksgiving

Added Santa mask/jacket/pantaloons/gloves/boots, Antisanta mask/jacket/pantaloons/gloves/boots/coal box, Thanksgiving dinner.

**Affects**: 12 items.

## Session 44 (audit IDs 13101–13400)

### QUESTS — new sections

- **"Diary - Lumbridge"**: pattern `name_starts("Explorer's ring")` (Lumbridge Diary reward, charges 1-4).
- **"Max hood variants"**: Max hood, Fire/Saradomin/Zamorak/Guthix/Accumulator max hood.

**Affects**: 9 items.

### MELEE Helmets — Tanzanite/Magma helm (uncharged) force_include

**Affects**: 2 items.

### SLAYER Slayer master items — major expansion

Added Tanzanite/Magma mutagen (Zulrah), Bone bolt pack, Plant pot pack, Sack pack, Basket pack, Eternal/Pegasian/Primordial crystal (Cerberus boots), Unsired/Bludgeon spine/claw/axon (Sire boss), Lizardman fang.

**Affects**: 14 items.

### MISC Keys — `Key master's key`

Cerberus access key.

**Affects**: 1 item.

### MISC Holiday rares — Partyhat/Halloween set + Easter blaster + Gravedigger + Christmas extras

Added Partyhat set, Halloween mask set, Bunny feet, Empty/Incomplete/Easter blaster, Gravedigger mask/top/leggings/boots/gloves, Anti-panties, Black santa hat, Inverted santa hat, Anti-present, Present.

**Affects**: 17 items.

### CRAFTING Misc crafting materials — `Xerician fabric`

Xerician robes material.

**Affects**: 1 item.

## Session 45 (audit IDs 13401–13700)

### FARMING Compost — Hosidius fertiliser

Added Sulphurous fertiliser, Gricoller's fertiliser, Saltpetre.

**Affects**: 3 items.

### COOKING "Misc cooked food" — Hosidius gnome fruits

Added Golovanova fruit, Bologano fruit, Logavano fruit (gnome cooking fruits from Hosidius).

**Affects**: 3 items.

### FISHING Bait — Fossil Island sandworms

Added Bucket of sandworms, Sandworms, Sandworms pack.

**Affects**: 3 items.

### RUNECRAFT "Essence intermediates" — `Dense essence block`

Added Dense essence block (Arceuus Dense runestone intermediate).

**Affects**: 1 item.

### MELEE — new "Shayzien supply armour" section

Pattern `name_starts("Shayzien supply ")` catches all Shayzien supply armour pieces + crate + set variants.

**Affects**: ~30 items.

### FIREMAKING — new "Lovakengj charcoal" section

Added Juniper charcoal (Lovakengj sulphurous charcoal).

**Affects**: 1 item.

### MINING_SMITHING — new "Lovakengj mining (sulphur + dynamite)" section

Added Volcanic sulphur, Dynamite pot, Dynamite, Blasted ore.

**Affects**: 4 items.

### MISC Clue scrolls — Clue bottle variants

Pattern extended to catch `Clue bottle (easy/medium/hard/elite)` (Wilderness clue scroll containers).

**Affects**: 4 items.

### MISC Holiday rares — Bunny Easter set

Added Bunny top, Bunny legs, Bunny paws.

**Affects**: 3 items.

## Session 46 (audit IDs 13701–19850, large ID gap 13682–19472)

### WC_FLETCHING — new "Ballista construction (MM2)" section

Added Incomplete light/heavy ballista, Ballista spring, Unstrung light/heavy ballista.

**Affects**: 5 items.

### CRAFTING Cut gems — `Zenyte shard`

Demonic Gorillas drop, fuses to form Zenyte.

**Affects**: 1 item.

### COOKING Misc cooked food — `Golovanova fruit top`

Botanical pie filling.

**Affects**: 1 item.

### SLAYER Slayer master items — Skotizo Dark totem

Added Dark totem base/middle/top + Dark totem (Catacombs of Kourend boss key components).

**Affects**: 4 items.

### FARMING Compost — `Compost pack`

Tithe Farm reward bulk compost.

**Affects**: 1 item.

### MISC Clue scrolls — Clue nest variants

Pattern extended to catch `Clue nest (easy/medium/hard/elite)`.

**Affects**: 4 items.

### MAGE Amulets — `Occult necklace (or)`

Ornament variant of Occult necklace.

**Affects**: 1 item.

### QUESTS Defenders — `Dragon defender (t)`

Trim variant of Dragon defender (Bounty Hunter reward).

**Affects**: 1 item.

## Session 47 (audit IDs 19851–20200)

### `_GOD_DHIDE_NAMES` extended — God d'hide boots

Added Ancient/Armadyl/Bandos/Saradomin/Guthix/Zamorak d'hide boots. MELEE/MAGE Boots now force_exclude them; RANGE Boots force_include them.

**Affects**: 6 items (REM melee+mage cross-tag, range only).

### `_TREASURE_TRAIL_MELEE_NAMES` extended — Gilded variants

Added Gilded med helm, Gilded chainbody, Gilded sq shield (clue trail cosmetic unique-shape items that match smithing extended pattern). REM mining_smithing.

**Affects**: 3 items.

### PRAYER Robes — `Holy wraps`

Cosmetic prayer wrap.

**Affects**: 1 item.

### MELEE Weapons — `Dragon scimitar (or)` force_include

Ornament variant of Dragon scimitar.

**Affects**: 1 item.

### MISC Holiday rares — Christmas 2017 Tuxedo + Halloween 2017 Mummy/Ankou

Added Dark/Light tuxedo jacket/cuffs/trousers/shoes/bow tie (10), Mummy's head/body/hands/legs/feet (5), Ankou mask/top/gloves/leggings/socks (5).

**Affects**: 20 items.

## Session 48 (audit IDs 20201–20700)

### PRAYER — new "Blessings (Treasure Trails)" section

Pattern `name_ends(" blessing")` catches Holy/Unholy/Peaceful/Honourable/War/Ancient blessing.

**Affects**: 6 items.

### MISC Clue scrolls — Clue geode + Reward casket

Pattern extended to catch `Clue geode (easy/medium/hard/elite)` and `Reward casket` variants.

**Affects**: 8 items.

### MISC Imbue scrolls — `Charge dragonstone jewellery scroll`

**Affects**: 1 item.

### MELEE Weapons — Godsword (or) ornaments

Force_include Armadyl/Bandos/Saradomin/Zamorak godsword (or) (ornament variants).

**Affects**: 4 items.

### MELEE Amulets — `Amulet of torture (or)`

Ornament variant.

**Affects**: 1 item.

### MISC Teleport tabs — `Ancient magicks tablet`

Teleport tablet for Ancient Magicks altar.

**Affects**: 1 item.

### MISC Holiday rares — Easter 2018 Evil chicken

Added Evil chicken feet/wings/head/legs.

**Affects**: 4 items.

### SLAYER Slayer master items — more bulk packs

Added Catalytic rune pack, Elemental rune pack, Adamant arrow pack, Rune arrow pack.

**Affects**: 4 items.

### FIREMAKING Wintertodt — `Bruma herb`

Wintertodt-side Bruma herb (Rejuvenation potion ingredient).

**Affects**: 1 item.

### HERBLORE — new "Wintertodt Rejuvenation potion" section

Pattern catches Rejuvenation potion (4-1) charge variants.

**Affects**: 4 items.

## Session 49 (audit IDs 20701–21000)

### HERBLORE — new "Chambers of Xeric herbs/secondaries" section

Added Grimy/clean noxifer, golpar, buchu leaf + Stinkhorn mushroom, Endarkened juice, Cicely (CoX-specific herblore secondaries).

**Affects**: 9 items.

### HERBLORE — new "Chambers of Xeric raid potions" section

Pattern combining CoX potion family names (Elder/Twisted/Kodai/Revitalisation/Prayer enhance/Xeric's aid/Overload) with charge endings (1)/(2)/(3)/(4). Catches all (-)/(+)/standard charge variants.

**Affects**: ~75 items.

### COOKING Misc cooked food — CoX cooked fish + bats

Added Pysk/Suphi/Leckish/Brawk/Mycil/Roqed/Kyren fish (0-6) (7 items) and Guanic/Prael/Giral/Phluxia/Kryket/Murng/Psykk bat (0-6) (7 items).

**Affects**: 14 items.

### MAGE — new "Magic level boost" section

Added Imbued heart (Magic level boost, mage rare drop).

**Affects**: 1 item.

### QUESTS Max hood variants — `Ardougne max hood`

**Affects**: 1 item.

### `_TREASURE_TRAIL_MELEE_NAMES` extended — Corrupted plates

Corrupted platebody/platelegs/plateskirt/kiteshield (Last Man Standing reward) — REM mining_smithing.

**Affects**: 4 items.

### MISC Teleport jewellery — `Ring of wealth (i5)`

**Affects**: 1 item.

### MISC Holiday rares — Christmas 2018 Snow globe set

Added Snow globe, Sack of presents, Giant present.

**Affects**: 3 items.

### SLAYER Slayer master items — `Empty jug pack`

**Affects**: 1 item.

## Session 50 (audit IDs 21001–21300)

### MELEE Helmets/Body armour/Legs — Ancestral force_exclude

REM melee for Ancestral hat/robe top/robe bottom (Ancestral set is pure mage).

**Affects**: 3 items.

### PRAYER — new "Prayer scrolls + cosmetic" section

Added Dexterous prayer scroll, Arcane prayer scroll, Torn prayer scroll, Necklace of faith.

**Affects**: 4 items.

### MAGE — new "CoX mage upgrades + Ancestral" section

Added Kodai insignia, Ancestral robes set.

**Affects**: 2 items.

### MISC Teleport jewellery — Ring of returning charge variants

Added Ring of returning (1-5).

**Affects**: 5 items.

### FARMING Tools — `Amulet of bounty`

**Affects**: 1 item.

### SLAYER Slayer master items — bracelets + Dark claw

Added Expeditious bracelet, Bracelet of slaughter, Dark claw (Skotizo drop).

**Affects**: 3 items.

### CONSTRUCTION Tools — `Flamtaer bracelet`

Mort'ton-area construction speed bracelet.

**Affects**: 1 item.

### QUESTS Max hood variants — `Infernal max hood`

**Affects**: 1 item.

### MISC Combat trophies — `Infernal max cape`

**Affects**: 1 item.

### MISC Holiday rares — Easter 2019 + 4th Birthday

Added 4th birthday hat + Birthday balloons + Invitation list (3), Easter egg helm (1), 12 Easter egg flavour variants, 12 Chocolate mix variants.

**Affects**: 28 items.

### MELEE GE armour sets — `Obsidian armour set`

Added explicit force_include via `name_in` (set name doesn't end with " set (lg)/(sk)" pattern).

**Affects**: 1 item.

## Session 51 (audit IDs 21301–21700)

### `_TREASURE_TRAIL_MELEE_NAMES` extended — Obsidian plate

Added Obsidian platebody, Obsidian platelegs (TzHaar drops, not smithable). REM mining_smithing.

**Affects**: 2 items.

### MINING_SMITHING expansions

- Mining tools & bags: Mining gloves, Superior mining gloves, Expert mining gloves.
- New "Volcanic Mine (ore fragments)" section: pattern `name_ends(" ore fragment")` + Heat-proof vessel.

**Affects**: 11 items.

### FISHING expansions

- Rods & tools: Drift net (Fossil Island underwater fishing).
- Specialty fish: Minnow (Tempoross precursor).

**Affects**: 2 items.

### FARMING Compost — Volcanic compost ingredients + Seaweed spore

Added Calcite, Pyrophosphite (Fossil Island Volcanic compost ingredients), Seaweed spore.

**Affects**: 3 items.

### HUNTER Traps — Fossil Island bird houses

Added Bird house, Oak/Willow/Teak bird house.

**Affects**: 4 items.

### MISC Currency — `Numulite`

Fossil Island currency.

**Affects**: 1 item.

### MISC Combat trophies — `Wilderness champion amulet`

**Affects**: 1 item.

### PRAYER — new "Fossil Island fossils" section

Pattern catches Unidentified small/medium/large/rare fossil + body part fossils (Small/Medium/Large/Rare fossilised limbs/spine/ribs/pelvis/skull/tusk).

**Affects**: ~26 items.

### CONSTRUCTION — new "Fossilised plant displays" section

Added Fossilised roots/stump/branch/leaf/mushroom (POH garden display).

**Affects**: 5 items.

### WC_FLETCHING — new "Sulliuscep + exotic wood" section

Added Sulliuscep cap (Fossil Island Sulliuscep mushroom tree).

**Affects**: 1 item.

### COOKING Misc cooked food — `Bowl of fish`

Volcanic Mine food.

**Affects**: 1 item.

### SLAYER Slayer master items — Wyvern visage + Tzhaar pack

Added Wyvern visage (Ancient wyvern shield material), Tzhaar air rune pack.

**Affects**: 2 items.

## Session 52 (audit IDs 21701–22000)

### QUESTS Max hood variants — Imbued max hoods + Assembler max hood

Added Imbued saradomin/zamorak/guthix max hood, Assembler max hood.

**Affects**: 4 items.

### SLAYER Slayer master items — major expansion

Added Tzhaar water/earth/fire rune pack (3), Black tourmaline core (1), Ancient crystal/emblem/totem/statuette (4, Wilderness slayer emblem progression), Bracelet of ethereum (uncharged) + Revenant ether (2), Vorkath's head + Vorkath's stuffed head (2).

**Affects**: 12 items.

### PRAYER Bone secondaries — `Granite dust`

Used in Crushed superior dragon bones recipe.

**Affects**: 1 item.

### WC_FLETCHING Crossbow parts — `Magic stock`

Crossbow stock made from magic logs.

**Affects**: 1 item.

### MISC Holiday rares — Christmas 2018 Wise Old Man

Added Snow imp costume (6 pieces: head/body/legs/tail/gloves/feet), Wise old man's santa hat, Santa suit + (wet)/(dry) (3 items).

**Affects**: 10 items.

## Session 53 (audit IDs 22001–22300)

### SLAYER Slayer master items — DS2 Vorkath drops

Added Skeletal visage, Dragon metal shard/slice/lump (Dragonfire ward ingredients).

**Affects**: 4 items.

### HUNTER Traps — Fossil Island higher-tier bird houses

Added Maple/Mahogany/Yew/Magic/Redwood bird house.

**Affects**: 5 items.

### RANGE Amulets — `Necklace of anguish (or)`

Ornament variant.

**Affects**: 1 item.

### MAGE Staves — enchanted Trident variants

Added Trident of the seas (e), Uncharged trident (e), Trident of the swamp (e), Uncharged toxic trident (e).

**Affects**: 4 items.

### MELEE Shields / RANGE Shields — d'hide shields reclassification

REM melee for Snakeskin/Hard leather/Green/Blue/Red/Black d'hide shield; ADD range force_include (these are range shields with defence_ranged dominance).

**Affects**: 6 items.

## Session 54 (audit IDs 22301–22700)

### New constants: `_VESTA_STATIUS_NAMES`, `_MORRIGAN_NAMES`, `_ZURIEL_NAMES`

PvP armour sets — Vesta/Statius are melee, Morrigan is range, Zuriel is mage. Stat spillover misclassifies them.

### MELEE Helmets/Body armour/Legs — Vesta/Statius force_include + Morrigan/Zuriel force_exclude

REM melee from Morrigan/Zuriel (mage/range gear); ADD melee for Vesta's chainbody/plateskirt, Statius's full helm/platebody/platelegs.

### RANGE Helmets/Body/Legs — Morrigan force_include + Vesta/Statius force_exclude

REM range from Vesta/Statius (melee gear); ADD range for Morrigan's coif/leather body/leather chaps.

### MAGE Helmets/Body/Legs — Zuriel force_include + Morrigan/Vesta/Statius force_exclude

REM mage from Morrigan/Vesta/Statius; ADD mage for Zuriel's hood/robe top/robe bottom.

### `_TREASURE_TRAIL_MELEE_NAMES` extended — Vesta/Statius plate

REM mining_smithing for Vesta's chainbody/plateskirt, Statius's full helm/platebody/platelegs.

**Affects**: PvP armor 11 items total (5 Vesta/Statius melee + 3 Morrigan range + 3 Zuriel mage).

### MELEE — new "Avernic defender construction" section

Added Avernic defender hilt.

**Affects**: 1 item.

### MAGE Staves — Sanguinesti staff + Thammaron's sceptre

Added Sanguinesti staff, Sanguinesti staff (uncharged), Thammaron's sceptre force_include.

**Affects**: 3 items.

### PRAYER Holy symbols — Emerald sickle (b)

Added Emerald sickle (b), Enchanted emerald sickle (b) (blessed sickles).

**Affects**: 2 items.

### SLAYER Slayer master items — major expansion

Added Ancient effigy/relic (Wilderness slayer), Bryophyta's essence, Metamorphic dust, Xeric's guard/warrior/sentinel/general/champion (Catacombs ranks), Empty bucket pack, Kq head (tattered) + Stuffed variant.

**Affects**: 12 items.

### MISC Teleport tabs — Drakan's medallion + Weiss basalts

Added Drakan's medallion (Sins of the Father quest tele), Icy basalt, Stony basalt, Basalt.

**Affects**: 4 items.

### MISC Holiday rares — Easter 2018 Eggshell + Handegg + Clown

Added Eggshell platebody/platelegs, Holy/Peaceful/Chaotic handegg (3), Clown mask/bow tie/gown/trousers.

**Affects**: 9 items.

### RUNECRAFT — new "Weiss salts (runecrafting secondaries)" section

Added Te salt, Efh salt, Urt salt.

**Affects**: 3 items.
