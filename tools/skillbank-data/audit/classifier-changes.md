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
