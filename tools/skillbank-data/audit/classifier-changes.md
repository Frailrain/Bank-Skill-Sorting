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
