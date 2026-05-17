# Bank Tag Ordering Research

Consolidated research for ordering items inside Skill Bank tabs. Grounded in source-code reads of RuneLite's Bank Tags Layout API and empirical probes of OSRS data sources.

---

## TL;DR

Two coordinated changes get us proper ordering:

1. **Phase 1** — better data (`SkillBankData.java` expanded and properly sorted). See `docs/wiki-scraper-brief.md`. Data primary source is **osrsbox-db**, not the wiki Cargo extension (which doesn't exist on this wiki).
2. **Phase 2** — Bank Tags **Layout API**. Use `LayoutManager.saveLayout()` after seeding to fix items at specific grid positions, and register an `AutoLayout` so users can re-flow tabs from the right-click menu.

The Layout API is real and ships in mainline RuneLite. The wiki Bucket API also exists but is narrower than the original brief implied — see "Data sources" below.

---

## RuneLite Bank Tags Layout API (verified)

Package: `net.runelite.client.plugins.banktags.tabs`

**`AutoLayout`** (interface, single method):

```java
public interface AutoLayout
{
    Layout generateLayout(Layout previous);
}
```

`previous` may be null or empty — handle both.

**`Layout`** (class):
- `Layout(String tag)` — empty.
- `Layout(String tag, int[] layout)` — int array where index = grid position, value = item ID (or `-1` for empty).
- `Layout(Layout other)` — copy.
- `int[] getLayout()`, `void resize(int)`, `int size()`.
- `int getItemAtPos(int)`, `void setItemAtPos(int itemId, int pos)`.
- `void addItem(int itemId)`, `void addItemAfter(int itemId, int pos)`.
- `int count(int itemId)`.

**`LayoutManager`** (singleton, inject):
- `Layout loadLayout(String tag)` — may return null.
- `void saveLayout(Layout)` — persists to `banktags.layout_<tagname>`.
- `void removeLayout(String tag)`.
- `void registerAutoLayout(Plugin plugin, String name, AutoLayout al)` — appears in the Bank Tags context menu as "Auto Layout: <name>".
- `void unregisterAutoLayout(String name)` — call in `shutDown()`.

**Persistence**: Bank Tags handles the serialisation. We hand it `Layout`, it writes `banktags.layout_<tag>`. We don't need to know the wire format.

**Grid**: items wrap to a width of `BANK_ITEMS_PER_ROW` (currently 8). Use `-1` for empty slots — a full row of `-1` is a visual section break.

## Data sources (verified)

### Primary: osrsbox-db (recommended)

- `https://raw.githubusercontent.com/0xNeffarion/osrsreboxed-db/master/docs/items-complete.json` — maintained fork, updates through Oct 2024.
- ~1 MB single JSON. No rate limit, no API quirks, no auth.
- Per-item fields actually present (verified for id 1333):
  - **Identity**: `id`, `name`, `wiki_name`, `wiki_url`, `release_date`, `last_updated`, `incomplete`, `duplicate`.
  - **Inventory flags**: `members`, `tradeable`, `tradeable_on_ge`, `stackable`, `stacked`, `noted`, `noteable`, `linked_id_item`, `linked_id_noted`, `linked_id_placeholder`, `placeholder`.
  - **Equipment flags**: `equipable`, `equipable_by_player`, `equipable_weapon`.
  - **Economy**: `cost`, `lowalch`, `highalch`, `buy_limit`.
  - **Misc**: `weight`, `quest_item`, `examine`.
  - **Equipment block** (`equipment.*` when equipable): `attack_stab`, `attack_slash`, `attack_crush`, `attack_magic`, `attack_ranged`, `defence_stab`, `defence_slash`, `defence_crush`, `defence_magic`, `defence_ranged`, `melee_strength`, `ranged_strength`, `magic_damage`, `prayer`, `slot`, `requirements`.
  - **Weapon block** (`weapon.*` when weapon): `attack_speed`, `weapon_type`, `stances`.

### Supplement: OSRS Wiki Bucket API

`https://oldschool.runescape.wiki/api.php?action=bucket&format=json&query=<expr>`

Verified to work:

```
bucket('infobox_item')
  .select('item_id','item_name','version_anchor','default_version','is_members_only','weight','examine','tradeable','equipable','stackable')
  .where('item_name','Rune scimitar')
  .where('default_version', true)
  .run()
```

Returns rows with each field as a list (e.g. `item_id: [1333]`). `select('*')` is rejected — fields must be enumerated.

**Caveats vs the prior draft**:
- `release` is **not** a valid `infobox_item` field. Use `release_date` from osrsbox instead.
- `cargoquery` action is **not available** on this wiki. The wiki has Bucket only.
- `ask` (Semantic MediaWiki) action is also **not available**.

### Standard MediaWiki category membership

`action=query&list=categorymembers&cmtitle=Category:<Name>&cmlimit=500&format=json` works and returns page titles. Empirically many category names from the original draft do NOT exist (`Cooked_food`, `Daggers`, `Combat_potions`, …). Verify each category exists before relying on it.

---

## Tier taxonomy

### Combat equipment (metal / boss tiers)

| Tier | Sort |
|---|---|
| Bronze | 1 |
| Iron | 2 |
| Steel | 3 |
| Black | 4 |
| White | 4.1 |
| Mithril | 5 |
| Adamant / adamantite | 6 |
| Rune / runite | 7 |
| Dragon | 8 |
| Crystal | 9 |
| Barrows | 10 |
| Infernal (tool variants only — axe / pickaxe / hammer) | 11 |
| BiS (Bandos / Armadyl / Ancestral / Inquisitor) | 12 |
| ToA tier (Torva / Virtus / Sanguine) | 13 |
| Top-end PVM (Scythe / Tumeken's / Tonalztics) | 14 |

### Wood (axes, logs, bows, longbows)

Regular (1), Oak (2), Willow (3), Maple (4), Yew (5), Magic (6), Redwood (7). Crystal / 3a / infernal tool variants get the metal-tier sort key.

### Mining ore

Copper / Tin (1), Iron (2), Silver (3), Coal (4), Gold (5), Mithril (6), Adamantite (7), Runite (8), Amethyst (9).

### Fishing — by level required

Shrimps (1) → Sardine (5) → Herring (10) → Anchovies (15) → Mackerel (16) → Trout (20) → Cod (23) → Pike (25) → Salmon (30) → Tuna (35) → Lobster (40) → Bass (46) → Swordfish (50) → Monkfish (62) → Karambwan (65) → Shark (76) → Anglerfish (82) → Dark crab (85) → Sailing fish (post-release, exact levels still verifying).

### Cooking — by heal amount ascending

Shrimps (3) → Cod (7) → Salmon (9) → Tuna (10) → Lobster (12) → Bass (13) → Swordfish (14) → Monkfish (16) → Shark (20) → Anglerfish (22) → Dark crab (22) → Manta ray (22). Karambwan (18 combo) groups with high-tier.

### Potion doses

Within each potion family: 4-dose → 3-dose → 2-dose → 1-dose → unfinished.

### Slayer helmet variants

Regular → Black mask → Slayer helmet → Slayer helmet (i) → Twisted slayer helmet → Twisted slayer helmet (i) → recolour (i) variants in release order: Black (i) → Green (i) → Red (i) → Purple (i) → Turquoise (i) → Hydra (i) → Tztok (i) → Vampyric (i) → Tzkal (i).

---

## Within-tab ordering algorithm

Within each tab, items are placed in grid positions following this sequence:

1. **Sort by section** (top-level section order per tab — see "Per-tab section orders" below).
2. **Within each section, sort by subcategory** (e.g. melee weapons: daggers → scimitars → swords → maces → battleaxes → warhammers → spears → polearms → two-handed → specialty).
3. **Within each subcategory, sort by tier** (bronze → iron → … → BiS).
4. **Ties broken by item ID ascending** (deterministic fallback).
5. **Empty rows** (`-1`-filled rows) between sections for visual separation.

---

## Per-tab section orders

Each tab below lists sections top-to-bottom. Items within sections sort by subcategory then tier.

### `melee`
1. Combat utility (cannonballs, dueling rings)
2. Weapons (by class then tier)
3. Helmets (by tier)
4. Body armour (by tier)
5. Legs (by tier)
6. Boots (by tier)
7. Gloves (by tier)
8. Shields & defenders (by tier)
9. Capes (combat capes)
10. Amulets & rings (combat-relevant)
11. Combat potions (by family, 4-dose → 1-dose)
12. Combat food (cooked fish, heal ascending)

### `range`
1. Ammunition (arrows / bolts / darts / knives by tier)
2. Bows (shortbow → longbow → composite → magic shortbow → twisted bow)
3. Crossbows (bronze → dragon → armadyl → zaryte)
4. Thrown (darts, knives, chinchompas)
5. Special (blowpipe, spec weapons)
6. Ranged armour by slot (d'hide → Void → Karil's → Armadyl → Masori)
7. Capes (Ava's devices)
8. Amulets & rings (ranged-relevant)
9. Ranging potions

### `mage`
1. Runes (base → combo)
2. Staves (basic → battlestaff → mystic → ancient → kodai)
3. Wands & powered staves (sang, harm, trident)
4. Magic armour (mystic → dagon'hai → ahrim's → ancestral → virtus)
5. Capes (god capes, imbued)
6. Magic amulets & rings
7. Magic potions
8. Rune pouch, tomes

### `prayer`
1. Bones (regular → big → wyvern → dragon → ourg → superior dragon)
2. Ashes (fiyr → infernal sin'keth)
3. Ensouled heads (by combat level)
4. Prayer potions (4 → 1)
5. Super restores (4 → 1)
6. Sanfew serums (4 → 1)
7. Prayer accessories (holy wrench, prayer cape)

### `cooking`
1. Cooking tools (chef's hat, gauntlets, cooking cape, outfit)
2. Cooking unlocks
3. Raw food (raw meat, raw fish, raw veg, raw grain)
4. Processed ingredients (flour, dough, pastry)
5. Cooked food by heal ascending
6. Pies, stews, pizzas (by tier)
7. Beverages

### `wc_fletching`
1. Axes (bronze → infernal / crystal / 3a)
2. Logs (regular → redwood + special woods)
3. Bird nests, forestry items
4. Fletching tools (knife, chisel)
5. Bowstrings
6. Unstrung bows by tier → Strung bows by tier
7. Crossbow stocks / limbs by tier → Strung crossbows
8. Bolt parts → Finished bolts by tier
9. Arrow shafts → Arrowtips by tier → Finished arrows by tier
10. Darts, javelins, throwing knives
11. Cape, pet

### `fishing`
1. Fishing tools (rods, harpoons by tier, lobster pot, big net, karambwan vessel)
2. Bait (worms, feathers)
3. Raw fish by level
4. Specialty fish (karambwan, manta, dark crab, anglerfish, sailing)
5. Outfits (Angler, spirit angler)
6. Cape, pet
7. Tempoross rewards

### `firemaking`
1. Tinderbox + bruma torch
2. Logs by tier
3. Special pyre logs
4. Firelighters by colour
5. Lanterns
6. Wintertodt (warm clothes, bruma kindling, pyromancer outfit)
7. Cape, pet

### `crafting`
1. Crafting tools (chisel, needle, glassblowing pipe)
2. Cloth & dyes
3. Leather (raw → tanned)
4. Tanned hides (snakeskin, d'hide by tier, royal)
5. Glass (molten glass, glass items)
6. Pottery
7. Gems (uncut by tier → cut by tier)
8. Jewellery (silver/gold rings/amulets/necklaces by gem)
9. Battlestaves
10. Crafting outfit pieces
11. Cape

### `mining_smithing`
1. Pickaxes (by tier)
2. Smithing hammer + tools
3. Coal/gem/ore bags
4. Ores by tier
5. Bars by tier
6. Smithing outputs (knives, claws, nails)
7. Mining outfit (prospector, varrock armour)
8. Smithing outfit (Smith's uniform)
9. Cape, pet

### `herblore`
1. Herblore tools (pestle, herb sack, alchemist's amulet)
2. Grimy herbs by level
3. Clean herbs by level
4. Secondaries (vials, water/oil filled, ingredient secondaries)
5. Unfinished potions by base herb
6. Finished potions by family, dose 4 → 1
7. Special potions (Zamorak brew, divine, stamina, super energy)
8. Cape, pet

### `agility_thieving`
1. Agility shortcut items (grapple, climbing boots)
2. Tickets / marks of grace
3. Graceful set (full set, by region recolour)
4. Agility cape & pet
5. Thieving outfit (rogue equipment)
6. Thieving accessories (dodgy necklace, ardougne cloak, gloves of silence)
7. Thieving tools (blackjacks by tier, lockpick)
8. Thieving cape & pet

### `slayer`
1. Slayer master items (enchanted gem, eternal gem)
2. Slayer rings & teleports
3. Slayer helmets (variant order per taxonomy above)
4. Task-specific gear (rock hammer, fungicide, ice cooler, witchwood icon, mirror shield)
5. Cannon (multicannon + cannonballs)
6. Cape

### `farming`
1. Farming tools (rake, spade, dibber, secateurs + magic, watering can, plant cure)
2. Compost (regular → ultracompost), bottomless bucket
3. Seeds (allotments → flowers → herbs → bushes → trees → fruit trees → special)
4. Saplings
5. Farming outfit (farmer's set)
6. Cape, pet

### `runecraft`
1. Talismans by rune type
2. Tiaras by rune type
3. Pouches (small → colossal)
4. Essence (pure, daeyalt, blood)
5. Runes by type
6. Combo runes
7. Wrath rune
8. RC outfit (Raiments of the Eye)
9. Cape, pet

### `hunter`
1. Traps (box, snare, butterfly net, magic box, swamp lizards, salamanders by tier)
2. Bait
3. Impling jars by tier
4. Hunter outfit (camo, larupia, graahk, kyatt, spotted/spottier cape)
5. Special (quetzal whistles, fox whistle, butterfly jar)
6. Cape, pet

### `construction`
1. Construction tools (saw, crystal saw, hammer)
2. Planks by tier (+ sailing planks)
3. Nails by tier
4. Bolts of cloth, marble blocks, magic stones, limestone, gold leaf
5. Mahogany Homes contracts
6. Construction outfit
7. Cape

### `sailing`
1. Navigation tools (spyglass, sailing crowbar, captain's log, repair kits)
2. Salvaging hooks by tier (IDs verifying post-release)
3. Trawling nets, fish crates
4. Ship components by tier (IDs verifying)
5. Sailing hardwood logs & planks (cross-tag with wc/construction)
6. Sailing cannons & cannonballs (cross-tag with range/slayer)
7. Raw sailing fish (cross-tag with fishing)
8. Cooked sailing fish (cross-tag with cooking/melee)
9. Sailing outfit
10. Cape, pet

### `misc`
1. Teleport jewellery (glory, RoW, games necklace, skills necklace, combat bracelet, slayer ring, dueling ring, digsite pendant, xeric's talisman)
2. Teleport tablets (alphabetised)
3. Clue scrolls (easy → master + beginner)
4. Clue tools (sextant, watch, chart)
5. Keys (crystal, muddy, mossy, brimstone, larran's, ecumenical)
6. Storage bags
7. Currency
8. Fairy ring utility items

### `quests`
1. Quest cape & QPC(t)
2. Music cape, max cape, completionist
3. Diary rewards by region
4. Diary consumables (rada's blessing, ardougne cloak teleports)
5. Boss uniques (non-gear)
6. Boss pets
7. Minigame uniques (Void, fighter torso, defenders by tier)
8. Quest-locked cosmetics

---

## Implementation approach (Phase 2)

**Approach B** (static layout on seed) + **Approach C** (registered AutoLayout) combined:
- Seed writes baseline layouts via `LayoutManager.saveLayout(...)` after `TagManager.addTag(...)` calls.
- Plugin registers an `AutoLayout` in `startUp()` and unregisters in `shutDown()`.
- Re-seed = re-write baseline. AutoLayout menu entry lets users re-flow without going through the panel.

Approximate per-seed cost: 20 layouts × ~150 items each = 3000 grid placements. `LayoutManager.saveLayout` is a single config write per layout, so 20 writes total. Modest.

## Open questions / verify in-game

- Whether `AutoLayout.generateLayout(previous)` receives null or empty Layout on first run — defensive code handles both.
- Whether the auto-layout menu entry appears only on tabs we own or on every tab — needs live verification.
- Whether re-running seed should *replace* user-customised layouts or *merge* — current plan: replace, with chat warning. AutoLayout entry gives an explicit non-destructive opt-in.

## Workflow reminder

Per the audit:
1. `./gradlew build` clean.
2. `./gradlew run` (this repo's task name is `run`, not `runClient`) with multi-line-aware log scrape — no exceptions involving `com.skillbank`.
3. Functional changes touching seed/reset/layout: gate on `LOGGED_IN`, manually verify in a real account or test profile.
4. Plugin-hub fork: per-update branch off `upstream/master` (not master-to-master).
