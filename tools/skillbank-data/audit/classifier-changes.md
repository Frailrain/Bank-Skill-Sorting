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
