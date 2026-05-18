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
