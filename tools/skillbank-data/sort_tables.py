"""Static sort tables, per-tab section structures, and deterministic
section assignment + composite sort-key generation.

Used by llm_promote.py to produce a SkillBankData.java where items within
each tab are grouped into ordered sections (Weapons, Head, Body, etc.)
and sorted within section by tier / skill level / functional rank.

Layered design (Brief #55):
  1. The static tables here (METAL_TIER, SLOT_RANK, etc.) provide raw
     numeric ranks for community-known orderings.
  2. assign_section() picks a section name for each (item, tab) pair
     using deterministic rules — slot data for combat tabs, name patterns
     for the rest. Unknown items fall through to the tab's last section
     (typically "Training & utility" or "Misc").
  3. composite_sort_key() returns a tuple the caller sorts on. The first
     element is (section_rank, group_rank) so the renderer can also use
     section_rank to bucket into the multi-section TabSpec.
"""
from __future__ import annotations

import re as _re

# ── Static tier lookups ─────────────────────────────────────────────────────

METAL_TIER: dict[str, int] = {
    "bronze": 10, "iron": 20, "steel": 30, "black": 40,
    "white": 45, "mithril": 50, "adamant": 60, "rune": 70,
    "granite": 75, "dragon": 80, "barrows": 90, "crystal": 95,
    "god_wars": 100, "raids": 110, "nex": 115, "mega_rare": 120,
}

SLOT_RANK: dict[str, int] = {
    "weapon": 10, "2h": 10, "shield": 20, "head": 30,
    "body": 40, "legs": 50, "hands": 60, "feet": 70,
    "cape": 80, "neck": 90, "ring": 100, "ammo": 110,
}

# Canonical order from runelite/community. Do not auto-sort.
RUNE_ORDER: list[str] = [
    "Air", "Mind", "Water", "Earth", "Fire", "Body",
    "Cosmic", "Chaos", "Nature", "Law", "Death",
    "Blood", "Soul", "Astral", "Wrath",
]
_RUNE_NAMES = {f"{r} rune" for r in RUNE_ORDER} | {f"{r}rune" for r in RUNE_ORDER}

# Brief #66: runes used in teleport spells. These appear in both the Mage
# tab (Runes section) and the Teleports tab (Teleport runes section). Order
# inside the section follows _RUNE_INDEX so it matches the canonical rune
# ordering players already see in their rune pouch.
# Section audit: Mind + Body runes removed — no teleport spell on any
# spellbook consumes them.
TELEPORT_RUNE_NAMES = [
    "Law rune",
    "Air rune", "Water rune", "Earth rune", "Fire rune",
    "Soul rune", "Astral rune",
]

GEM_TIER: dict[str, int] = {
    "opal": 10, "jade": 20, "red topaz": 30, "sapphire": 40,
    "emerald": 50, "ruby": 60, "diamond": 70, "dragonstone": 80,
    "onyx": 90, "zenyte": 100,
}

PROCESS_STAGE: dict[str, int] = {
    "tool": 10, "raw": 20, "cleaned": 30, "unfinished": 40,
    "finished": 50, "upgraded": 60, "cosmetic_variant": 70,
    "broken": 90,
}

# Set families — items in the same family cluster together (godwars/barrows).
SET_FAMILY: dict[str, int] = {
    "barrows": 10,  # Ahrim's, Dharok's, Guthan's, Karil's, Torag's, Verac's
    "god_wars": 20,  # Bandos, Armadyl, Saradomin, Zamorakian, Ancient
    "third_age": 30,
    "torva": 40, "virtus": 41, "masori": 42,  # DT2 bosses
    "ancestral": 50, "justiciar": 51, "scythe": 52,  # raids 2
    "torment_zaryte": 60,  # newer godwars adjacent
}

# ── Section structures per tab (display order matters) ──────────────────────

TAB_SECTIONS: dict[str, list[str]] = {
    # Brief #78: Food section added after Rings + before Training & utility
    # in each combat tab. ALWAYS_ZONE1 routes cooked food to the loadout zone.
    "melee": [
        # Section audit: "Ammunition" renamed — its only members are
        # ammo-SLOT worn items (Crystal blessing, Ghommal's lucky penny),
        # not ammunition.
        "Weapons", "Shields & defenders", "Head", "Body", "Legs",
        "Hands", "Feet", "Capes", "Neck", "Rings", "Ammo slot",
        "Food",
        "Potions",
        "Training & utility",
    ],
    # Brief #87: range weapon section split into nine fixed launcher→ammo
    # rows. Each section gets its own row break via the standard inter-
    # section row alignment; the dynamic top-N two-zone partition is
    # skipped for these sections (they're added to ALWAYS_ZONE1_SECTIONS
    # on the Java side so everything surfaces).
    "range": [
        "Bows", "Arrows",
        "Crossbows", "Bolts",
        "Ballistae & javelins",
        "Blowpipe & darts",
        "Knives",
        "Morrigan's javelins",
        "Other throwables",
        "Head", "Body", "Legs", "Hands", "Feet",
        "Capes", "Shields & off-hands", "Neck", "Rings",
        "Food",
        "Potions",
        "Training & utility",
    ],
    # Brief #74: Runes lead the mage tab (reverts Brief #66's Weapons-first swap).
    "mage": [
        # Section audit: "Teleport tablets & spell utility" renamed — the
        # teleport tablets migrated to the teleports tab; what remains is
        # rune packs, NZ runes, orbs, hearts and other cast supplies.
        "Runes", "Weapons", "Off-hands, books & tomes", "Head", "Body",
        "Legs", "Hands", "Feet", "Capes", "Neck", "Rings",
        "Spell utility & supplies",
        "Food",
        "Potions",
        "Enchanting & skilling magic",
    ],
    "prayer": [
        # Brief #60: equipment first so the prayer tab leads with gear rather
        # than bones-pile chaff.
        # Tab audit: the old "Bone-processing utility" fallback held 38% of
        # the tab (bonemeals, pyre logs, fossils, prayer potions). New rows
        # split those families out; the fallback keeps only true utility
        # (bonecrusher / ash sanctifier / one-offs).
        "Prayer equipment & robes", "Bones & ashes", "Ensouled heads",
        "Bonemeal & offerings", "Fossils & enriched bones",
        "Pyre logs & shade remains",
        "Prayer-restoring consumables", "Holy symbols, books & blessings",
        "Bone-processing utility",
    ],
    # Brief #90: level-ordered family rows. Fish raw → cooked → burnt,
    # then meat raw → cooked → burnt, then the ingredient dump (fruits,
    # veg, chocolate, cheese etc.), then multi-ingredient composites,
    # then tools; leftover burnt composites close the tab. Fruits and
    # vegetables stay folded into Ingredients (Brief #76).
    "cooking": [
        "Raw fish", "Cooked fish", "Burnt fish",
        "Raw meat", "Cooked meat", "Burnt meat",
        "Ingredients",
        "Combo food", "Baked & cooked goods",
        "Drinks & brews",
        "Cooking tools & utensils",
        "Burnt food",
    ],
    # Brief #63: wc_fletching split. Fletching is now its own tab; the
    # woodcutting bits live alongside firemaking content in a combined tab.
    # Both tabs continue to overlap with the existing `firemaking` tab
    # (items can live in multiple tabs).
    # Brief #66: tools first, outfits second, materials after.
    # Tab audit: "Misc utility" held a third of the tab. Light sources get
    # their own row (lanterns / candles / torches are the firemaking
    # product family); machetes join axes; oils + barbarian pyre bones
    # join Pyre logs.
    "woodcutting_firemaking": [
        "Axes & machetes",
        "Tinderboxes & firelighting tools",
        "Light sources & lamps",
        "Forestry items",
        "Woodcutting outfit",
        "Firemaking outfit",
        "Logs",
        "Pyre logs",
        "Shade items",
        "Wintertodt & minigame items",
        "Misc utility",
    ],
    # Brief #75 (revised): materials-first with per-type sections. Tools
    # at top, then Logs (cross-tag from WC) → universal consumables
    # (Feathers/strings) → Arrows (shafts/headless/finished bundled) →
    # Arrowheads as their own materials row → Bows (paired u+strung per
    # tier) → Crossbows (stock+unstrung+finished per tier) → Bolts
    # (tips+unf+finished) → Darts → Javelins → Misc safety net.
    "fletching": [
        "Tools",
        "Logs",
        "Feathers",
        "Arrows",
        "Arrowheads",
        "Bows",
        "Crossbows",
        "Bolts",
        "Darts",
        "Javelins",
        "Misc fletching",
    ],
    # Tab audit: the "Fishing minigame items" fallback held 69% of the tab.
    # Raw fish now catches every catchable (aerial / Camdozaal / CoX /
    # Sailing deep-sea), trophies get their own row, and boost potions /
    # worm baits roll into Bait & consumables.
    "fishing": [
        "Fishing tools", "Bait & consumables", "Fishing outfit",
        "Raw fish", "Trophies & big catches", "Fishing minigame items",
    ],
    # Brief #64: standalone firemaking tab removed. All firemaking content
    # lives in "woodcutting_firemaking" (Woodcutting + Firemaking) which
    # was created in Brief #63.
    # Brief #76: moulds split out from tools; new top of tab.
    # Tab audit: fallback was 51% — furs/textiles/lamps now route to their
    # material rows, battlestaves and monster-part chisel work get rows.
    "crafting": [
        "Moulds", "Crafting tools", "Gems", "Hides & leather",
        "Spinning materials", "Glassmaking", "Battlestaves & orbs",
        "Pottery & clay", "Jewellery materials", "Crafted jewellery",
        "Crafted armour & leather goods", "Monster parts & shells",
        "Crafting outfit & utility",
    ],
    # Tab audit: fallback was 60% — minigame minerals (MLM / Volcanic Mine /
    # Camdozaal / Zalcano / salts), smithed parts (limbs / unf bolts /
    # javelin heads / keel parts / visages) and the 30-piece Shayzien
    # supply armour family each get their own row.
    "mining_smithing": [
        "Pickaxes", "Mining outfit & utility", "Ores",
        "Special ores & minerals", "Bars",
        "Smithing tools", "Smithing outfit & gloves",
        "Shayzien supply armour", "Smithed parts & components",
        "Cannonballs & ammo outputs", "Giants' Foundry & minigame items",
    ],
    # Brief #76: flat layout. Barbarian mixes + Divine variants roll into
    # Finished potions. Tab audit: outfit/containers/packs split back out
    # of Tools ("Herblore outfit & utility") and the Mastering Mixology
    # reward+paste family gets a closing row.
    "herblore": [
        "Tools", "Herblore outfit & utility", "Herbs", "Secondaries",
        "Unfinished potions", "Finished potions",
        "Mastering Mixology items",
    ],
    "agility_thieving": [
        # Section audit: marks row renamed — Hallowed Sepulchre reward gear
        # dominates it, so "tickets" undersold the contents.
        "Agility outfit & graceful", "Run-energy consumables",
        "Agility marks & rewards", "Thieving outfit & rogue set",
        "Thieving tools", "Thieving loot & artefacts",
    ],
    # Section audit: "Mandatory protection" renamed (salt / fungicide /
    # ice cooler are task FINISHERS, and the Shayzien anti-shaman armour
    # joins the row); two big families promoted out of the 335-item Misc
    # utility fallback — task-monster heads/trophies and boss drops /
    # upgrade parts.
    "slayer": [
        "Slayer assignment items", "Mandatory task items",
        "Core slayer gear", "Cannon & burst supplies",
        "Combat potions", "Prayer & restores", "Food",
        "Teleports", "Loot management",
        "Monster heads & trophies", "Boss drops & upgrade parts",
        "Misc utility",
    ],
    # Tab audit: harvested crops (fruit/veg/hops/grimy herbs/produce sacks)
    # were 60% of the tab and sat in the fallback — they get a dedicated
    # row before the outfit/contract fallback. Seedlings roll into Saplings.
    "farming": [
        "Farming tools", "Compost & soil treatment",
        "Allotment seeds", "Hops seeds", "Flower seeds",
        "Herb seeds", "Bush seeds", "Tree seeds",
        "Fruit tree seeds", "Special seeds", "Saplings",
        "Harvested produce",
        "Farmer outfit & contracts",
    ],
    # Section audit: outfit row renamed — bloodbark/swampbark armour,
    # runescrolls, abyssal extracts and binding jewellery outnumber the
    # literal outfit pieces.
    "runecraft": [
        "Essence", "Pouches & storage", "Talismans", "Tiaras",
        "Core runes", "Combination runes", "Runecraft gear & utility",
        "Guardians of the Rift items",
    ],
    # Brief #76: Creature products split into furs/meats/tertiaries.
    # Tab audit: caught-creature families (salamanders + their tar fuel,
    # butterflies/moths + mixes) and the hunter weapon/potion rows split
    # out of the 58% "Hunter tertiaries" fallback.
    "hunter": [
        "Hunter tools & traps", "Hunter weapons & ammo",
        "Nets, jars & containers", "Hunter outfit",
        "Baits & potions",
        "Chinchompas", "Salamanders & lizards", "Butterflies & moths",
        "Furs & hides", "Hunter meats", "Hunter tertiaries",
        "Implings & impling jars", "Birdhouse items",
    ],
    # Tab audit: flatpacks (~110 items), bagged garden plants and POH
    # trophy-room decor were 89% of the tab in the fallback — each gets a
    # row family. Sailing hull parts / repair kits / paints evicted via
    # the construction exclude tokens.
    # Section audit: "Teleport-to-house items" deleted (empty — house
    # tablets live in the teleports tab); the contract row renamed since
    # blueprints and sawmill vouchers outnumber Mahogany Homes props.
    "construction": [
        "Construction tools", "Planks", "Nails",
        "Building materials",
        "Flatpacks & furniture", "Garden & bagged plants",
        "Mounted heads & decor",
        "Blueprints & contracts",
        "Construction outfit & rewards",
    ],
    # Misc audit: the old 8-section layout dumped ~1200 items into
    # "Uncategorized". New sections derived from the actual clump contents:
    # keys, sigils (Leagues), lamps, fossils, books/documents, minigame
    # rewards and consumables each get their own row family.
    "misc": [
        "Currency & exchange tokens", "General teleports",
        "Jewellery teleports", "Utility containers",
        "Clue scroll items", "Keys & access",
        "Books & documents", "Lamps & XP rewards",
        "Sigils & trinkets", "Fossils & museum",
        # Section audit: renamed — the " & " display cut left only the word
        # "Minigame" on the divider; "Minigame rewards" survives intact.
        "Minigame rewards", "Consumables & supplies",
        "General tools", "Uncategorized",
    ],
    # Quests row redesign: equipment routes by slot, the rest by name
    # family (keys/books/consumables/artefacts/remains/materials), with
    # the true one-off quest junk holding the tab-closing fallback.
    # Section audit: "Diary rewards" deleted (empty — diary teleports moved
    # to the teleports tab, diary jewellery to misc); "Boss pets &
    # followers" deleted (its lone member, Pet rock, is a Fremennik quest
    # keepsake → Quest items); "Reward" prefixes dropped — mid-quest
    # disguises and wieldable quest props dominate both equipment rows.
    "quests": [
        "Achievement capes",
        "Weapons & wieldables", "Armour & clothing",
        "Keys & access items",
        "Books & lore", "Quest consumables",
        "Artefacts & relics", "Remains & trophies",
        "Quest supplies & materials", "Quest items",
    ],
    # Brief #90: shipbuilding needs its construction kit on hand — tools,
    # nails and planks each get their own row between navigation gear and
    # the ship parts they build.
    # Tab audit: the 380-item "Sailing outfit & rewards" fallback split
    # into content-derived rows — cannon ammo, treasure keys/schematics,
    # deep-sea fish (raw/cooked), sea-monster parts, wreck salvage, island
    # resources, the ~70 boat cocktails, and pearls (reward currency).
    "sailing": [
        "Sailing tools & navigation",
        "Construction tools",
        "Nails",
        "Planks",
        "Shipbuilding materials",
        "Ship components",
        "Cannons & cannonballs",
        "Keys, charts & schematics",
        "Raw sailing fish",
        # Section audit: renamed — the row also holds the burnt stages and
        # the "I should get this stuffed!" trophy catches.
        "Cooked fish & trophies",
        "Sea creature parts",
        "Salvage",
        "Island resources",
        "Boat cocktails & brews",
        "Pearls",
        "Cargo & contracts",
        "Sailing outfit & rewards",
    ],
    # Brief #75: set-per-row redesign. Sections group by source/category;
    # within each section, items with the same set_name (sn meta field)
    # cluster on a single row.
    # Tab audit: the 78% "Miscellaneous cosmetics" clump gave up its big
    # families — skill/max capes, Leagues + speedrun rewards, recoloured
    # outfits (graceful / crystal), and quest/regional costume sets
    # (vyre / villager / fremennik / chompy hats).
    "cosmetics": [
        "Treasure trail sets",
        "Minigame sets",
        "Holiday items",
        "Random event sets",
        "Ornament kits",
        "Skill capes & max capes",
        "Leagues & speedrun rewards",
        "Recoloured outfits",
        "Quest & regional outfits",
        "Miscellaneous cosmetics",
    ],
    # Brief #66: simple-mode combat tab variants. The Java layout builder
    # picks _simple keys when the per-tab config flag is on. The "Armor"
    # section collapses Head / Body / Legs / Hands / Feet into one row
    # block; within-section sort routes by slot rank then tier.
    "melee_simple": [
        "Weapons", "Shields & defenders", "Armor", "Capes", "Neck",
        "Rings", "Ammo slot", "Food", "Potions", "Training & utility",
    ],
    "range_simple": [
        "Weapons", "Ammunition", "Armor", "Capes",
        "Shields & off-hands", "Neck", "Rings", "Food", "Potions",
        "Training & utility",
    ],
    # Brief #74 parity: Runes lead mage_simple too (matches the Java
    # TAB_SECTIONS, which already had Runes first).
    "mage_simple": [
        "Runes", "Weapons", "Off-hands, books & tomes", "Armor",
        "Capes", "Neck", "Rings",
        "Spell utility & supplies", "Food", "Potions",
        "Enchanting & skilling magic",
    ],
    # Brief #62: Teleports tab. Brief #66 added Teleport runes at the top
    # as the foundational requirement for any spellbook teleport.
    # Tab audit: rows are now grouped by teleport FORM — tablets, master
    # scroll book scrolls, diary/CA rewards, transport unlocks — instead
    # of the destination-based rows (City / Boss & PvM / Minigame) that
    # never attracted a single item.
    "teleports": [
        "Teleport runes",
        "Mounted & charged jewellery",
        "Spellbook tablets",
        "Teleport scrolls",
        "Diary & reward teleports",
        "Skill destinations",
        "Wilderness teleports",
        "Quest-locked teleports",
        "Special & one-time",
    ],
}


# Brief #63: display names. Tab IDs stay snake_case (so existing
# RuneLite banktags storage keeps working and config keys built from
# tag names like "icon_<tag>" don't pick up spaces or + chars). The
# UI / Cowork audit boundary translates through this map.
TAB_DISPLAY_NAMES: dict[str, str] = {
    "melee": "Melee",
    "range": "Range",
    "mage": "Mage",
    "prayer": "Prayer",
    "cooking": "Cooking",
    "woodcutting_firemaking": "Woodcutting + Firemaking",
    "fletching": "Fletching",
    "fishing": "Fishing",
    "crafting": "Crafting",
    "mining_smithing": "Mining + Smithing",
    "herblore": "Herblore",
    "agility_thieving": "Agility + Thieving",
    "slayer": "Slayer",
    "farming": "Farming",
    "runecraft": "Runecraft",
    "hunter": "Hunter",
    "construction": "Construction",
    "misc": "Misc",
    "quests": "Quests",
    "sailing": "Sailing",
    "cosmetics": "Cosmetics",
    "teleports": "Teleports",
}


def display_name(tab_id: str) -> str:
    """Return the human-facing label for a tab id. Falls back to the
    raw id when no mapping is defined (defensive — every known tab
    should be in TAB_DISPLAY_NAMES)."""
    return TAB_DISPLAY_NAMES.get(tab_id, tab_id)


# Reverse map for converting audit-supplied display names back to internal
# IDs. Used by the audit-decision ingest path.
_DISPLAY_TO_ID: dict[str, str] = {v: k for k, v in TAB_DISPLAY_NAMES.items()}


def tab_id_from_display(name: str) -> str:
    """Inverse of display_name(). Raises KeyError on an unknown label so a
    typo in audit output surfaces loudly rather than silently dropping the
    tab assignment."""
    if name in _DISPLAY_TO_ID:
        return _DISPLAY_TO_ID[name]
    # Fall through: if `name` is already an internal id, accept it. Cowork
    # may emit either form during the migration.
    if name in TAB_SECTIONS:
        return name
    raise KeyError(f"unknown tab label: {name!r}")



# ══ Brief #60: data-driven section classification ═══════════════════════════
#
# Pre-Brief-#60, sections were assigned by name-pattern matching. Brief #59
# diagnosed the herb scattering bug — clean herbs don't carry "Clean " in
# their canonical names, so startswith("clean ") matched zero items. Same
# class of bug existed for cooked food, logs, fishing, etc.
#
# Fix: canonical *names* are authored here; the actual ID sets are built at
# import time by intersecting those names with the merged item dataset
# (see init_id_sets). assign_section then prefers ID-set membership over
# name patterns. Name patterns remain as a last-resort fallback for items
# the canonical lists don't cover.

# Canonical herb base names (each maps to BOTH grimy and clean variants).
HERB_BASE_NAMES = [
    "Guam leaf", "Marrentill", "Tarromin", "Harralander", "Ranarr weed",
    "Toadflax", "Irit leaf", "Avantoe", "Kwuarm", "Snapdragon",
    "Cadantine", "Lantadyme", "Dwarf weed", "Torstol", "Huasca",
    # Quest / leagues herbs
    "Snake weed", "Ardrigal", "Sito foil", "Volencia moss",
    "Rogue's purse", "Noxifer", "Golpar", "Buchu leaf",
]

# Canonical log names.
LOG_NAMES = [
    "Logs", "Oak logs", "Willow logs", "Maple logs", "Yew logs",
    "Magic logs", "Mahogany logs", "Teak logs", "Redwood logs",
    "Arctic pine logs", "Achey tree logs", "Juniper logs",
    "Mystic logs", "Blisterwood logs",
]
PYRE_LOG_NAMES = [
    "Pyre logs", "Oak pyre logs", "Willow pyre logs", "Maple pyre logs",
    "Yew pyre logs", "Magic pyre logs", "Redwood pyre logs",
    "Mahogany pyre logs", "Teak pyre logs",
]

# Brief #66: granular cooked-food / ingredient buckets. The cooking tab
# now has separate sections for raw vs cooked fish, raw vs cooked meat,
# fruits, vegetables, combo food (fast-eat mechanic), and baked & cooked
# goods (composite recipes).

# Cooked fish — every cooked entry where the source is a fish.
COOKED_FISH_NAMES = [
    "Shrimps", "Sardine", "Anchovies", "Herring", "Mackerel",
    "Trout", "Cod", "Pike", "Salmon", "Tuna", "Lobster",
    "Bass", "Swordfish", "Monkfish", "Shark",
    "Sea turtle", "Manta ray", "Dark crab", "Anglerfish",
    "Cooked karambwan", "Karambwanji", "Cooked karambwanji",
    "Tuna potato", "Cave eel", "Cooked eel", "Slimy eel", "Rocktail",
    "Cooked oomlie wrap", "Cooked sweetcorn",
    "Fishcake", "Cooked fishcake", "Cooked oomlie", "Cooked octopus",
    "Cooked yak meat", "Cooked snake", "Halibut",
]

# Cooked meat & game.
COOKED_MEAT_NAMES = [
    "Cooked chicken", "Cooked meat", "Cooked rabbit",
    "Roast bird meat", "Roast beast meat", "Roast rabbit",
    "Roast chompy", "Roast jubbly", "Roast oomlie", "Roast frog legs",
    "Spider on stick", "Spider on shaft",
    "Cooked crab meat", "Roast bird thigh", "Cooked sandworm",
]

# Combo food — fast-eat / gnome cookery mechanic. Cooked karambwan and
# Guthix rest are the canonical examples.
COMBO_FOOD_NAMES = [
    "Cooked karambwan",
    "Guthix rest", "Guthix rest(1)", "Guthix rest(2)", "Guthix rest(3)",
    "Guthix rest(4)",
    "Halibut",
    # Pizzas
    "Plain pizza", "Meat pizza", "Anchovy pizza", "Pineapple pizza",
    # Gnome cookery — battas, crunchies, gnomebowls, etc.
    "Cheese+tom batta", "Toad batta", "Worm batta", "Vegetable batta",
    "Fruit batta",
    "Toad crunchies", "Spicy crunchies", "Worm crunchies", "Chocchip crunchies",
    "Toad gnomebowl", "Worm gnomebowl", "Salmon gnomebowl",
    "Tangled toad's legs gnomebowl", "Tangled toads' legs gnomebowl",
    "Tangled toad's legs", "Tangled toads' legs",
    "Veg ball", "Worm hole", "Choc bomb",
    "Premade dirty blast", "Premade fr'y blurberry", "Premade fr't blast",
    "Premade veg ball", "Premade choc bomb",
]

# Baked & cooked goods — composite recipes that aren't combo food. Pies,
# cakes, stews, kebabs, bread, fishcakes go here.
BAKED_GOODS_NAMES = [
    # Pies
    "Apple pie", "Redberry pie", "Meat pie", "Mud pie", "Garden pie",
    "Fish pie", "Admiral pie", "Wild pie", "Summer pie",
    "Steak and kidney pie", "Botanical pie", "Mushroom & onion pie",
    "Mushroom pie",
    "Half a pie", "Half a redberry pie", "Half a meat pie",
    "Half a fish pie", "Half a summer pie", "Half an apple pie",
    "Half a garden pie", "Half a wild pie", "Half an admiral pie",
    "Half a mushroom pie", "Half a botanical pie",
    # Cakes
    "Cake", "Chocolate cake", "Sliced cake", "Slice of cake",
    "Chocolate slice", "2/3 cake", "2/3 chocolate cake",
    # Stews & curries
    "Stew", "Spicy stew", "Curry",
    # Kebabs
    "Kebab", "Ugthanki kebab", "Super kebab",
    # Bread & rolls
    "Bread", "Bread roll",
]

# Fruits — raw fruit items for the cooking tab's fruits section.
FRUIT_NAMES = [
    "Banana", "Sliced banana", "Mashed banana",
    "Orange", "Sliced orange", "Orange chunks", "Orange slices",
    "Pineapple", "Pineapple chunks", "Pineapple ring",
    "Lemon", "Sliced lemon", "Lemon chunks", "Lemon slices",
    "Lime", "Sliced lime", "Lime chunks", "Lime slices",
    "Watermelon", "Watermelon slice",
    "Pear", "Pear half", "Apple", "Cooking apple", "Sliced apple",
    "Strawberry", "Strawberry half",
    "Papaya fruit", "Dragonfruit", "Coconut", "Half coconut",
    "Coconut shell", "Coconut milk",
]

# Vegetables — raw veg items.
VEGETABLE_NAMES = [
    "Tomato", "Chopped tomato", "Onion", "Chopped onion",
    "Cabbage", "Potato", "Baked potato", "Egg potato", "Mushroom potato",
    "Potato with butter", "Potato with cheese", "Chilli potato",
    "Sweetcorn", "Raw sweetcorn",
    "Garlic", "Spring onion", "Cooked spring onion",
    "Cooked mushroom", "Cooked white tree mushroom",
]

# Raw cookables (everything that becomes cooked food). Used to populate the
# Raw cookables section AND drive raw-fish detection in fishing.
RAW_COOKABLE_NAMES = [
    "Raw shrimps", "Raw sardine", "Raw anchovies", "Raw herring",
    "Raw mackerel", "Raw trout", "Raw cod", "Raw pike", "Raw salmon",
    "Raw tuna", "Raw lobster", "Raw bass", "Raw swordfish",
    "Raw monkfish", "Raw shark", "Raw sea turtle", "Raw manta ray",
    "Raw karambwan", "Raw karambwanji", "Raw dark crab", "Raw anglerfish",
    "Raw rocktail", "Raw cave eel", "Raw lava eel", "Raw slimy eel",
    "Raw beef", "Raw rat meat", "Raw bear meat", "Raw chicken",
    "Raw bird meat", "Raw rabbit", "Raw frog legs",
    "Raw chompy", "Raw jubbly", "Raw oomlie",
    # Some items in the fishing world are caught raw and DO NOT carry the
    # "Raw " prefix in OSRS — they're fish. Include here so they show in
    # Raw cookables too.
    "Karambwanji",
]
RAW_FISH_NAMES = [
    "Raw shrimps", "Raw sardine", "Raw anchovies", "Raw herring",
    "Raw mackerel", "Raw trout", "Raw cod", "Raw pike", "Raw salmon",
    "Raw tuna", "Raw lobster", "Raw bass", "Raw swordfish",
    "Raw monkfish", "Raw shark", "Raw sea turtle", "Raw manta ray",
    "Raw karambwan", "Raw karambwanji", "Raw dark crab", "Raw anglerfish",
    "Raw rocktail", "Raw cave eel", "Raw lava eel", "Raw slimy eel",
    "Karambwanji",
]

# Brief #90: burnt items split by family so each pairs with its raw/cooked
# rows. Anything burnt that isn't fish or meat (pies, pizzas, gnome
# cookery, breads...) stays in the tab-closing "Burnt food" section.
BURNT_FISH_NAMES = [
    "Burnt shrimp", "Burnt fish", "Burnt eel", "Burnt cave eel",
    "Burnt rainbow fish", "Burnt karambwan", "Burnt lobster",
    "Burnt swordfish", "Burnt monkfish", "Burnt shark",
    "Burnt sea turtle", "Burnt anglerfish", "Burnt dark crab",
    "Burnt manta ray",
]
BURNT_MEAT_NAMES = [
    "Burnt chicken", "Burnt meat", "Burnt rabbit", "Burnt bird meat",
    "Burnt beast meat", "Burnt bat", "Burnt spider", "Burnt snail",
    "Burnt crab meat", "Burnt kebbit", "Burnt fox meat", "Burnt antelope",
    "Burnt large beast", "Burnt chompy", "Burnt jubbly", "Burnt oomlie",
    "Burnt oomlie wrap",
]

# Cooking ingredients (non-cooked items consumed in cooking recipes).
INGREDIENT_NAMES = [
    "Pot of flour", "Flour", "Egg", "Bucket of milk", "Bucket of water",
    "Bucket of sand", "Bucket of compost", "Bucket of slime",
    "Bucket of wax", "Bucket", "Chocolate bar", "Chocolate dust",
    "Ashes", "Pat of butter", "Cooking apple", "Cake tin",
    "Wheat", "Yeast", "Pot of yeast", "Pot", "Empty pot",
    "Bowl", "Empty bowl", "Bowl of water",
    "Tomato", "Onion", "Cabbage", "Potato",
    "Banana", "Orange", "Pineapple", "Lemon", "Lime", "Watermelon",
    "Pear", "Apple", "Strawberry",
    "Pot of cream", "Pot of pulp",
    "Sliced banana", "Sliced lemon", "Sliced lime", "Sliced orange",
    "Pineapple chunks", "Pineapple ring",
    "Salt", "Sandstone (1kg)",
    "Garlic", "Spice", "Gnome spice", "Curry leaf", "Tomato",
    "Sweetcorn", "Raw sweetcorn",
    "Cheese", "Equa leaves", "Lime chunks", "Orange chunks",
    "Lemon chunks", "Pineapple chunks", "Pineapple ring",
]

# Fishing tools / bait / outfit.
FISHING_TOOL_NAMES = [
    "Small fishing net", "Fishing rod", "Fly fishing rod",
    "Oily fishing rod", "Barbarian rod", "Pearl rod",
    "Pearl fly rod", "Pearl barbarian rod",
    "Fishing net", "Big fishing net",
    "Harpoon", "Barbed harpoon", "Dragon harpoon", "Crystal harpoon",
    "Infernal harpoon", "Trailblazer harpoon",
    "Lobster pot", "Karambwan vessel", "Karambwan vessel (loaded)",
    "Fish sack", "Fish sack barrel",
    "Fishbowl", "Fishing explosive", "Cormorant's glove",
]
FISHING_BAIT_NAMES = [
    "Fishing bait", "Feather", "Fishing feather",
    "Stripy feather", "Yellow feather", "Red feather", "Blue feather",
    "Orange feather", "Raw karambwanji",  # Karambwanji is bait for karambwan
    "Fish food", "Fish offcuts", "Sashimi",
]

# Herblore secondaries (a chunk of these — not exhaustive but the common ones).
SECONDARY_NAMES = [
    "Eye of newt", "Limpwurt root", "Snape grass", "White berries",
    "Red spiders' eggs", "Chocolate dust", "Goat horn dust",
    "Wine of zamorak", "Jangerberries", "Potato cactus",
    "Kebbit teeth dust", "Mort myre fungus", "Pirate's hook",
    "Magic roots", "Mahogany roots", "Yew roots",
    "Phoenix feather", "Blue dragon scale",
    "Crushed bird's nest", "Bird nest", "Bird's nest",
    "Bird's egg", "Crushed bird egg",
    "Amylase crystal", "Morchella mushroom",
    "Crushed nest", "Crushed superior dragon bones",
    "Bee", "Anchovies", "Charcoal", "Olive oil",
    "Pyrophosphite", "Volcanic sulphur", "Cactus spine",
    "Swamp tar", "Frog spawn", "Toad's legs", "Cave nightshade",
    "Aremum",  # quest
    "Sliced red banana",  # gnome cooking
    "Snake's tongue",
]

# Seed sub-category canonical names.
SEED_NAMES_ALLOTMENT = [
    "Potato seed", "Onion seed", "Cabbage seed", "Tomato seed",
    "Sweetcorn seed", "Strawberry seed", "Watermelon seed",
    "Snape grass seed",
]
SEED_NAMES_HOPS = [
    "Barley seed", "Hammerstone seed", "Asgarnian seed", "Yanillian seed",
    "Krandorian seed", "Wildblood seed", "Jute seed",
]
SEED_NAMES_HERB = [
    "Guam seed", "Marrentill seed", "Tarromin seed", "Harralander seed",
    "Ranarr seed", "Toadflax seed", "Irit seed", "Avantoe seed",
    "Kwuarm seed", "Snapdragon seed", "Cadantine seed", "Lantadyme seed",
    "Dwarf weed seed", "Torstol seed", "Huasca seed",
]
SEED_NAMES_FLOWER = [
    "Marigold seed", "Rosemary seed", "Nasturtium seed", "Woad seed",
    "Limpwurt seed", "White lily seed",
]
SEED_NAMES_BUSH = [
    "Redberry seed", "Cadavaberry seed", "Dwellberry seed",
    "Jangerberry seed", "Whiteberry seed", "Poison ivy seed",
]
SEED_NAMES_TREE = [
    "Acorn", "Willow seed", "Maple seed", "Yew seed", "Magic seed",
    "Redwood tree seed", "Blisterwood seed",
]
SEED_NAMES_FRUIT_TREE = [
    "Apple tree seed", "Banana tree seed", "Orange tree seed",
    "Curry tree seed", "Pineapple seed", "Papaya tree seed",
    "Palm tree seed", "Calquat tree seed", "Dragonfruit tree seed",
    "Celastrus seed",
]
SEED_NAMES_SPECIAL = [
    "Mushroom spore", "Belladonna seed", "Cactus seed",
    "Potato cactus seed", "Hespori seed",
    "Mahogany seed", "Teak seed",
    "Crystal acorn", "Spirit seed",
    "Attas seed", "Kronos seed", "Iasor seed",
    "Anima seed",
]

# Items the LLM cross-tag classifier keeps misplacing. Brief #60 hard-removes
# these from the specified tabs after the build_synthetic_tabs assembly.
PRAYER_EXCLUDE_NAMES = [
    "Bird nest", "Bird's nest", "Bird's nest (empty)",
    "Bird's nest (red egg)", "Bird's nest (green egg)",
    "Bird's nest (blue egg)", "Bird's nest (seeds)",
    "Bird's nest (mushroom)", "Bird's nest (wyson)",
]
CONSTRUCTION_EXCLUDE_NAMES = [
    # Sailing repair kits, paints, and ship parts shouldn't be in construction.
    # These are caught by name pattern at filter time; the actual ID set is
    # built from items whose name contains any of these tokens.
    # Tab audit: the eleven Sailing ship paints — excluded by exact name
    # because a bare " paint" token would eat POH "...painting" decor.
    "Angler's paint", "Armadylean paint", "Barracuda paint",
    "Guthixian paint", "Inky paint", "Merchant's paint", "Salvor's paint",
    "Sandy paint", "Saradominist paint", "Shark paint", "Zamorakian paint",
    # Forestry beehive kit — lives in Woodcutting + Firemaking.
    "Sturdy beehive parts",
]
_CONSTRUCTION_EXCLUDE_TOKENS = [
    "ship paint", "sailing paint", "ship plank", "ship repair kit",
    "rope coil", "ship rope", "ship sail", "hull plate",
    # Tab audit: the actual Sailing item names are "<Wood> hull parts" and
    # "<Wood> repair kit" — the old tokens never matched anything.
    "hull parts", "repair kit",
    # NOTE: no "mast" token — substring matching eats "...master..." names
    # ("Grid master altar icon scroll"), and no item in the dump actually
    # carries a word-boundary "mast". If Sailing ever ships bankable masts,
    # exclude them by exact name in CONSTRUCTION_EXCLUDE_NAMES.
    "rudder", "anchor",
]


# ── Pass-3 strict audit: stat-carrying gear filters (misc + cosmetics) ─────
#
# The misc and cosmetics tabs must not hold real armour / weapons. These
# constants drive data-driven hard-excludes (see tab_exclude_ids) built in
# init_id_sets, so newly released wiki variant ids can never re-pollute
# either tab on regen.

_AUDIT_OFF_FIELDS = (
    "attack_stab", "attack_slash", "attack_crush", "attack_magic",
    "attack_ranged", "melee_strength", "ranged_strength", "magic_damage",
)
_AUDIT_DEF_FIELDS = (
    "defence_stab", "defence_slash", "defence_crush", "defence_magic",
    "defence_ranged",
)

# Misc keeps these deliberately (combat trophies — user-approved exception).
_MISC_TROPHY_KEEP_NAMES = {
    "Fire cape", "Fire max cape", "Infernal cape", "Infernal max cape",
    "Champion's cape", "Lightbearer",
}

# Cosmetics: items whose only positive bonuses are this trivial are
# fashionscape, not gear (team capes +4 total, coloured wardrobe hats +1s,
# GIM bracers +1 each). Anything above either bound is real equipment.
_COSMETIC_TRIVIAL_MAX_STAT = 3
_COSMETIC_TRIVIAL_TOTAL = 6

# Skill capes / max capes stay in cosmetics per Brief (they row together as
# one "Skill capes" set) — but only the defence-only ones. Max-cape variants
# with offensive bonuses (fire / infernal / assembler / imbued god capes)
# are real combat gear and get evicted.
_SKILLCAPE_PREFIXES = (
    "attack cape", "strength cape", "defence cape", "hitpoints cape",
    "ranging cape", "prayer cape", "magic cape", "runecraft cape",
    "construct. cape", "construction cape", "dungeoneering cape",
    "agility cape", "herblore cape", "thieving cape", "crafting cape",
    "fletching cape", "slayer cape", "hunter cape", "mining cape",
    "smithing cape", "fishing cape", "cooking cape", "firemaking cape",
    "woodcutting cape", "farming cape", "sailing cape",
    "attack hood", "strength hood", "defence hood", "hitpoints hood",
    "ranging hood", "prayer hood", "magic hood", "runecraft hood",
    "construct. hood", "construction hood",
    "agility hood", "herblore hood", "thieving hood", "crafting hood",
    "fletching hood", "slayer hood", "hunter hood", "mining hood",
    "smithing hood", "fishing hood", "cooking hood", "firemaking hood",
    "woodcutting hood", "farming hood", "sailing hood",
    "quest point cape", "quest point hood", "music cape", "music hood",
    "achievement diary cape", "achievement diary hood",
)

# God vestments / blessed d'hide / rune god armour name family (pass-3:
# real defence → hard-excluded from cosmetics; they live in prayer /
# range / melee instead).
_GOD_VESTMENT_PREFIXES = ("ancient ", "armadyl ", "bandos ", "guthix ",
                          "saradomin ", "zamorak ")
_GOD_VESTMENT_PIECES = ("mitre", "stole", "crozier", "cloak", "robe top",
                        "robe legs", "d'hide", "dragonhide", "coif", "chaps",
                        "bracers", "kiteshield", "full helm", "platebody",
                        "platelegs", "plateskirt")


def _is_skillcape_family(nlow: str) -> bool:
    """True for skill capes, their hoods and max cape/hood variants."""
    return ("max cape" in nlow or "max hood" in nlow
            or any(nlow.startswith(p) for p in _SKILLCAPE_PREFIXES))


def _is_god_vestment_family(nlow: str) -> bool:
    return (any(nlow.startswith(g) for g in _GOD_VESTMENT_PREFIXES)
            and any(p in nlow for p in _GOD_VESTMENT_PIECES))


def _positive_stat_profile(eq: dict) -> tuple[int, int, int]:
    """(total positive off+def, max single positive stat, positive off sum)
    over the equipment record. Negative bonuses (deliberately useless
    holiday weapons like flowers / banners) are ignored — they make an
    item LESS combat-capable, i.e. genuinely cosmetic."""
    pos_all: list[int] = []
    off_sum = 0
    for f in _AUDIT_OFF_FIELDS:
        v = eq.get(f) or 0
        if v > 0:
            pos_all.append(v)
            off_sum += v
    for f in _AUDIT_DEF_FIELDS:
        v = eq.get(f) or 0
        if v > 0:
            pos_all.append(v)
    return sum(pos_all), max(pos_all, default=0), off_sum


def _cosmetics_gear_violation(name: str, eq: dict) -> bool:
    """True when an item is too stat-y to live in the cosmetics tab."""
    total, mx, off_sum = _positive_stat_profile(eq)
    if total == 0:
        return False
    nlow = name.lower()
    # Skill capes / max capes stay unless they carry offensive bonuses.
    if _is_skillcape_family(nlow):
        return off_sum > 0
    # God vestments / blessed d'hide / rune god armour: always out, even
    # the low-defence pieces (mitres, stoles) — the set is real gear.
    if _is_god_vestment_family(nlow):
        return True
    # Trivial fashion stats (team capes, coloured wardrobe, GIM bracers).
    if mx <= _COSMETIC_TRIVIAL_MAX_STAT and total <= _COSMETIC_TRIVIAL_TOTAL:
        return False
    return True


def _misc_gear_violation(item: dict, eq: dict) -> bool:
    """True when an item carries any positive combat stat and is not one
    of the misc tab's sanctioned exceptions (teleport jewellery families
    sectioned as 'Jewellery teleports', the Fire/Infernal cape +
    Lightbearer combat trophies)."""
    total, _mx, _off = _positive_stat_profile(eq)
    if total == 0:
        return False
    if _name(item) in _MISC_TROPHY_KEEP_NAMES:
        return False
    if _section_misc(item) == "Jewellery teleports":
        return False
    return True


# ── Build-time ID set construction ─────────────────────────────────────────

_ID_SETS: dict[str, set[int]] = {}

# Pass-3 (Brief #75 extension): data-driven cosmetics set families derived
# from the item corpus — populated by init_id_sets, consumed by _set_name.
_DERIVED_SET_BY_NAME: dict[str, str] = {}


def _index_by_name(items_by_id: dict[int, dict]) -> dict[str, set[int]]:
    """name (lowercased) → set of IDs in the merged dataset that match exactly,
    excluding noted/placeholder/duplicate variants."""
    out: dict[str, set[int]] = {}
    for iid, it in items_by_id.items():
        if it.get("noted") or it.get("placeholder") or it.get("duplicate"):
            continue
        nm = (it.get("name") or "").strip().lower()
        if nm:
            out.setdefault(nm, set()).add(int(iid))
    return out


def _ids_for_names(names: list[str], by_name: dict[str, set[int]]) -> set[int]:
    """Resolve a list of canonical names to the union of all matching IDs."""
    out: set[int] = set()
    for n in names:
        out |= by_name.get(n.strip().lower(), set())
    return out


def _ids_for_token_match(
    tokens: list[str], items_by_id: dict[int, dict],
) -> set[int]:
    """Wider net: any item whose lowercased name contains any of the tokens.
    Used for exclude sets where the variant space is large (sailing items)."""
    out: set[int] = set()
    for iid, it in items_by_id.items():
        if it.get("noted") or it.get("placeholder") or it.get("duplicate"):
            continue
        nm = (it.get("name") or "").lower()
        for tok in tokens:
            if tok in nm:
                out.add(int(iid))
                break
    return out


def init_id_sets(items_by_id: dict[int, dict], *, verbose: bool = True) -> None:
    """Populate the module-level _ID_SETS from the merged item dataset.
    Must be called before assign_section() / classifier-driven downstream code.

    With verbose=True, prints per-set counts + samples to stderr — Brief #60
    requires this for build-log verification.
    """
    import sys as _sys
    by_name = _index_by_name(items_by_id)

    def build(name: str, canonical_names: list[str]) -> set[int]:
        s = _ids_for_names(canonical_names, by_name)
        _ID_SETS[name] = s
        if verbose:
            sample = list(s)[:3]
            sample_names = ", ".join(
                (items_by_id.get(i, {}).get("name") or str(i)) for i in sample
            )
            print(
                f"  id_set[{name}]: {len(s):>4} items "
                f"(e.g. {sample_names})",
                file=_sys.stderr,
            )
        return s

    if verbose:
        print("Building ID sets for section assignment...", file=_sys.stderr)

    # OSRS canonical grimy names lowercase the base name's first char
    # (e.g. "Guam leaf" → "Grimy guam leaf"). The "weed" / "leaf" suffix
    # stays as-is.
    grimy = ["Grimy " + n[0].lower() + n[1:] for n in HERB_BASE_NAMES]
    build("grimy_herbs", grimy)
    build("clean_herbs", HERB_BASE_NAMES)
    build("logs", LOG_NAMES)
    build("pyre_logs", PYRE_LOG_NAMES)
    # Brief #66: granular cooked-food buckets replace the old cooked_food /
    # special_food monolith. Combo food and baked goods are checked first
    # in _section_cooking() so an item that's both a cooked fish AND a
    # combo food (Cooked karambwan) lands in Combo food.
    build("cooked_fish", COOKED_FISH_NAMES)
    build("cooked_meat", COOKED_MEAT_NAMES)
    build("combo_food", COMBO_FOOD_NAMES)
    build("baked_goods", BAKED_GOODS_NAMES)
    build("fruits", FRUIT_NAMES)
    build("vegetables", VEGETABLE_NAMES)
    build("raw_cookables", RAW_COOKABLE_NAMES)
    build("raw_fish", RAW_FISH_NAMES)
    build("ingredients", INGREDIENT_NAMES)
    build("fishing_tools", FISHING_TOOL_NAMES)
    build("fishing_bait", FISHING_BAIT_NAMES)
    build("herblore_secondaries", SECONDARY_NAMES)
    build("seeds_allotment", SEED_NAMES_ALLOTMENT)
    build("seeds_hops", SEED_NAMES_HOPS)
    build("seeds_herb", SEED_NAMES_HERB)
    build("seeds_flower", SEED_NAMES_FLOWER)
    build("seeds_bush", SEED_NAMES_BUSH)
    build("seeds_tree", SEED_NAMES_TREE)
    build("seeds_fruit_tree", SEED_NAMES_FRUIT_TREE)
    build("seeds_special", SEED_NAMES_SPECIAL)
    build("prayer_exclude", PRAYER_EXCLUDE_NAMES)
    build("teleport_runes", TELEPORT_RUNE_NAMES)
    # Section audit: per-tab membership excludes (see tab_exclude_ids).
    for _tab, _names in SECTION_AUDIT_TAB_EXCLUDE_NAMES.items():
        build(f"section_audit_exclude_{_tab}", _names)

    # Token-based exclude for construction (sailing pollution) + the
    # exact-name excludes (ship paints etc. — tab audit).
    construction_exclude = _ids_for_token_match(_CONSTRUCTION_EXCLUDE_TOKENS, items_by_id)
    construction_exclude |= _ids_for_names(CONSTRUCTION_EXCLUDE_NAMES, by_name)
    _ID_SETS["construction_exclude"] = construction_exclude
    if verbose:
        print(f"  id_set[construction_exclude]: {len(construction_exclude):>4} items "
              f"(token match: {', '.join(_CONSTRUCTION_EXCLUDE_TOKENS[:3])}...)",
              file=_sys.stderr)

    # Pass-3 strict audit: stat-based hard-excludes for misc + cosmetics.
    # An item with a missing equipment record borrows the record of the
    # canonical item with the same name (catches wiki variant ids like the
    # Leagues trophy copies of real gear whose own record is incomplete).
    canon_eq_by_name: dict[str, dict] = {}
    for iid in sorted(items_by_id):
        it = items_by_id[iid]
        if it.get("noted") or it.get("placeholder") or it.get("duplicate"):
            continue
        eq = it.get("equipment")
        nm = it.get("name") or ""
        if eq and nm and nm not in canon_eq_by_name:
            canon_eq_by_name[nm] = eq

    misc_stat_exclude: set[int] = set()
    cosmetics_stat_exclude: set[int] = set()
    for iid, it in items_by_id.items():
        if it.get("noted") or it.get("placeholder") or it.get("duplicate"):
            continue
        nm = it.get("name") or ""
        eq = it.get("equipment") or canon_eq_by_name.get(nm) or {}
        if not eq:
            continue
        if _misc_gear_violation(it, eq):
            misc_stat_exclude.add(int(iid))
        if _cosmetics_gear_violation(nm, eq):
            cosmetics_stat_exclude.add(int(iid))
    _ID_SETS["misc_stat_exclude"] = misc_stat_exclude
    _ID_SETS["cosmetics_stat_exclude"] = cosmetics_stat_exclude
    if verbose:
        print(f"  id_set[misc_stat_exclude]: {len(misc_stat_exclude):>4} "
              f"stat-carrying ids barred from misc",
              file=_sys.stderr)
        print(f"  id_set[cosmetics_stat_exclude]: {len(cosmetics_stat_exclude):>4} "
              f"stat-carrying ids barred from cosmetics",
              file=_sys.stderr)

    # Pass-3 (Brief #75 extension): derive multi-piece outfit families for
    # the cosmetics row-per-set layout.
    _build_set_families(items_by_id)
    if verbose:
        fams = len(set(_DERIVED_SET_BY_NAME.values()))
        print(f"  set_families: {len(_DERIVED_SET_BY_NAME)} item names in "
              f"{fams} derived outfit families",
              file=_sys.stderr)


def _is(item: dict, set_name: str) -> bool:
    """ID-set membership check. False if init_id_sets hasn't been called."""
    iid = item.get("id")
    s = _ID_SETS.get(set_name)
    return s is not None and iid in s


# ── Tab exclude IDs (post-section-assignment hard filter) ──────────────────


# Section audit: cross-tab corrections — items whose TAB membership (not
# just section) was wrong. Name lists resolve to every matching id at
# init_id_sets time; the explicit id sets cover name-collision cases
# (e.g. the Lunar Diplomacy dream-puzzle copies of real runes).
SECTION_AUDIT_TAB_EXCLUDE_NAMES: dict[str, list[str]] = {
    # Rum has no teleport function (pirate-quest alcohol); mind/body runes
    # power no teleport spell on any spellbook.
    "teleports": ["Rum", "Mind rune", "Body rune"],
    # Soul shard is a Teomat libation offering (prayer keeps it).
    "runecraft": ["Soul shard"],
    # Huasca seed is a farming herb seed (farming keeps it); Elkhorn
    # coral is a Sailing island resource (sailing keeps it).
    "herblore": ["Huasca seed", "Elkhorn coral"],
    "farming": ["Elkhorn coral"],
    "hunter": ["Elkhorn coral"],
    # XP-reward lamps consolidated into misc "Lamps & XP rewards" (they
    # are force_included there via mapping.py).
    "quests": ["Antique lamp", "Lamp", "Magic lamp (strength)"],
    # Warhammer is melee gear (force_included there via mapping.py);
    # Frozen key is the quests-tab boss key — one home is enough.
    "misc": ["Warhammer", "Frozen key"],
}
SECTION_AUDIT_TAB_EXCLUDE_IDS: dict[str, set[int]] = {
    # Lunar Diplomacy dream-puzzle copies of Water/Air/Earth/Fire runes —
    # unusable for real teleports (the real elemental ids stay).
    "teleports": {9691, 9693, 9695, 9699},
}


def tab_exclude_ids(tab: str) -> set[int]:
    """Items hard-excluded from a tab regardless of LLM placement. Applied by
    llm_promote.build_synthetic_tabs as a post-filter."""
    out = set(_ID_SETS.get(f"section_audit_exclude_{tab}", set()))
    out |= SECTION_AUDIT_TAB_EXCLUDE_IDS.get(tab, set())
    if tab == "prayer":
        out |= _ID_SETS.get("prayer_exclude", set())
    if tab == "construction":
        out |= _ID_SETS.get("construction_exclude", set())
    # Pass-3 strict audit: no real armour / weapons in misc or cosmetics.
    if tab == "misc":
        out |= _ID_SETS.get("misc_stat_exclude", set())
    if tab == "cosmetics":
        out |= _ID_SETS.get("cosmetics_stat_exclude", set())
    return out

# ── Helpers ────────────────────────────────────────────────────────────────


def _name(item: dict) -> str:
    return (item.get("name") or "").strip()


def _slot(item: dict) -> str:
    eq = item.get("equipment") or {}
    return (eq.get("slot") or "").lower()


def _weapon_type(item: dict) -> str:
    wp = item.get("weapon") or {}
    return (wp.get("weapon_type") or "").lower()


def _req_max_level(item: dict) -> int:
    """Highest skill-level requirement on this item (Attack, Defence, etc.).
    Returns 0 if no requirement. Used as a sort-key component for combat gear
    and skill items."""
    eq = item.get("equipment") or {}
    reqs = eq.get("requirements") or {}
    if not isinstance(reqs, dict):
        return 0
    return max((int(v) for v in reqs.values() if isinstance(v, (int, float))), default=0)


def _name_tier(name: str) -> int:
    """Look up the most distinctive metal-tier token in the name. Returns
    METAL_TIER value, else 999."""
    n = name.lower()
    for key, val in METAL_TIER.items():
        if key in n:
            return val
    return 999


# ── Section assignment per tab ──────────────────────────────────────────────


def _section_range_weapon(item: dict):
    """Brief #87: route a range-tab weapon-or-ammo item into one of the
    nine fixed launcher→ammo rows. Returns None if the item isn't a
    range weapon at all (caller falls through to slot dispatch for
    armor / capes / etc.).

    Detection order matters — Morrigan's javelins are checked before
    the generic javelin gate (they need an isolated row), ballistae
    before crossbows (different row), blowpipe before darts (it leads
    the dart row).
    """
    nlow = _name(item).lower()
    slot = _slot(item)
    wc = (_weapon_type(item) or "").lower()

    # Morrigan's javelins — both thrown weapon AND ballista ammo. Pin
    # to their own row before either the javelin or thrown gates can
    # claim them.
    if "morrigan" in nlow and "javelin" in nlow:
        return "Morrigan's javelins"

    # Ammo-slot items: arrows, bolts, javelins. Check name family since
    # they all share slot=ammo and wc=None.
    if slot == "ammo":
        if "javelin" in nlow:
            return "Ballistae & javelins"
        if "bolt" in nlow:
            return "Bolts"
        # Atlatl darts are ammo for the Eclipse atlatl (which rows with the
        # Bows) — they're an arrow in all but name.
        if "arrow" in nlow or "atlatl dart" in nlow:
            return "Arrows"
        return None  # bolt rack / unfamiliar ammo — let slot dispatch decide

    # Two-handed launchers (slot=2h) and one-handed launchers (slot=weapon).
    if slot not in ("weapon", "2h"):
        return None

    # Ballistae are crossbow-class 2h but get their own row.
    if "ballista" in nlow:
        return "Ballistae & javelins"

    if wc == "crossbow" or "crossbow" in nlow:
        return "Crossbows"

    if wc == "bow" or "shortbow" in nlow or "longbow" in nlow:
        return "Bows"
    # Special bows by name that may not carry wc=bow.
    if any(k in nlow for k in (
            "twisted bow", "dark bow", "comp bow", "composite bow",
            "ogre bow", "crystal bow", "bow of faerdhinen",
            "craw's bow", "webweaver", "corrupted bow", "3rd age bow",
            "seercull", "eclipse atlatl")):
        return "Bows"

    # Blowpipe leads the dart row. Match all variants (toxic / blazing
    # / camphor / ironwood / rosewood) by the "blowpipe" token.
    if "blowpipe" in nlow:
        return "Blowpipe & darts"
    # Darts — wc='thrown' + name ending in dart / darts.
    if wc == "thrown" and (nlow.endswith(" dart") or nlow.endswith(" darts")):
        return "Blowpipe & darts"
    # Knives — wc='thrown' + name token.
    if wc == "thrown" and ("knife" in nlow or "knives" in nlow):
        return "Knives"

    # Other throwables: salamanders, chinchompas, obsidian rings,
    # thrownaxes, atlatl darts, hunter's spear, etc.
    if wc in ("salamander", "chinchompas", "blaster"):
        return "Other throwables"
    if any(k in nlow for k in (
            "salamander", "chinchompa", "toktz-xil-ul", "thrownaxe",
            "hunter's spear", "atlatl")):
        return "Other throwables"

    # Any remaining wc='thrown' that didn't match dart/knife above
    # (e.g. thrownaxes, prototype darts) bins as throwables too.
    if wc == "thrown":
        return "Other throwables"

    return None


def _section_combat(item: dict, tab: str) -> str:
    """Slot-based section assignment shared across melee/range/mage."""
    slot = _slot(item)
    name = _name(item)
    nlow = name.lower()

    # Section audit: Mud pie is a wieldable PvP throwing weapon that
    # "does not restore Hitpoints" — never food.
    if tab == "range" and nlow == "mud pie":
        return "Other throwables"

    # Brief #78: cooked food cross-tagged into combat tabs lives in the
    # Food section (always Zone 1 per ALWAYS_ZONE1_SECTIONS). Checked
    # before slot dispatch so non-equipped food doesn't fall through to
    # "Training & utility" / "Spell utility & supplies".
    # Section audit: blighted spell SACKS (rune charges) and blighted
    # overloads (potions) are not food — carve them out of the blighted
    # prefix match.
    if (_is(item, "cooked_fish") or _is(item, "cooked_meat")
            or _is(item, "combo_food") or _is(item, "baked_goods")
            or nlow in _EXTRA_COMBAT_FOOD_EXACT):
        return "Food"
    # Tab audit: food outside the canonical id sets — blighted (PvP-world)
    # food, CoX board food ("Kyren fish (6)" / "Psykk bat (6)"), Gauntlet
    # paddlefish, potato variants and gnome combo food.
    if ((nlow.startswith("blighted ") and not nlow.endswith(" sack")
            and "overload" not in nlow)
            or " fish (" in nlow or " bat (" in nlow
            or "paddlefish" in nlow
            or nlow in _EXTRA_COOKED_FISH_EXACT
            or nlow in _EXTRA_COOKED_MEAT_EXACT
            or any(k in nlow for k in (
                "potato with", "chilli potato", "egg potato",
                "mushroom potato", "tuna and corn", "chocolate bomb",
                "premade worm hole", "premade veg ball",
                "batta", "gnomebowl", "worm hole", "veg ball",
                "tangled toad"))):
        return "Food"

    # Potions row directly after Food (all combat tabs). Match by potion
    # family or dose suffix; the no-slot guard keeps charged jewellery
    # ("Amulet of glory(4)") out. Potions used to fall to "Training &
    # utility". Section audit: "rune pouch" guard stops the Divine rune
    # pouch matching the "divine " potion-family token.
    if not _slot(item) and "rune pouch" not in nlow and (
            _potion_family_rank(nlow) != 999
            or (any(nlow.endswith(d) for d in ("(1)", "(2)", "(3)", "(4)"))
                and "(unf)" not in nlow)):
        return "Potions"
    # Section audit: stat-boosting bar drinks that carry no dose suffix —
    # they row with the potions in every combat tab.
    if nlow in _COMBAT_STAT_DRINKS:
        return "Potions"

    # Mage-specific routing first.
    if tab == "mage":
        # Runes — canonical rune list match.
        if name in _RUNE_NAMES or " rune" in nlow and ("dust rune" not in nlow):
            # Be conservative: only call it a Rune if the name ends with " rune"
            # OR it's in the canonical rune set. Stops "Rune scimitar" matching.
            if (name in _RUNE_NAMES or nlow.endswith(" rune")
                    or nlow.endswith(" runes")):
                return "Runes"
        # Combination runes.
        if any(nlow.startswith(p) for p in ("steam rune", "mist rune", "smoke rune",
                                            "lava rune", "mud rune", "dust rune")):
            return "Runes"
        # Tablets / teleport scrolls.
        if (" tablet" in nlow or nlow.endswith(" teleport")
                or " teleport " in nlow or nlow.endswith(" scroll")):
            return "Spell utility & supplies"
        # Books / tomes / off-hand shields used for spellcasting.
        if slot == "shield" and any(k in nlow for k in (
                "book", "tome", "magic book", "mage's book", "merlin's crystal")):
            return "Off-hands, books & tomes"

    # Range-specific routing first.
    if tab == "range":
        s = _section_range_weapon(item)
        if s is not None:
            return s
        # Tab audit: no-slot ammo variants that fell to Training &
        # utility — single arrows, brutal arrows, shop arrow packs.
        if nlow.endswith(" arrow") or nlow.endswith(" arrows") \
                or "arrow pack" in nlow or nlow.endswith(" brutal"):
            return "Arrows"
        # Zulrah's scales charge the blowpipe.
        if "zulrah's scale" in nlow:
            return "Blowpipe & darts"
        # Pass-3 audit: grapples are agility-shortcut utility, not
        # crossbows — they row with the training/utility gear.
        if "grapple" in nlow:
            return "Training & utility"
        # Salamander tar fuel rows with the salamanders that fire it.
        if nlow.endswith(" tar"):
            return "Other throwables"
        # Section audit: wieldable variants whose ids lack equipment
        # records (Leagues replicas, crystal-quest copies) fell to the
        # fallback — pin them to their launcher rows by exact name.
        if nlow in ("new crystal bow", "venator bow"):
            return "Bows"
        if nlow == "dragon knife":
            return "Knives"
        if nlow == "studded body":
            return "Body"

    # Slot-based dispatch.
    if slot in ("weapon", "2h"):
        return "Weapons"
    if slot == "shield":
        if tab == "mage":
            return "Off-hands, books & tomes"
        if tab == "range":
            return "Shields & off-hands"
        return "Shields & defenders"
    if slot == "head":
        return "Head"
    if slot == "body":
        return "Body"
    if slot == "legs":
        return "Legs"
    if slot == "hands":
        return "Hands"
    if slot == "feet":
        return "Feet"
    if slot == "cape":
        return "Capes"
    if slot in ("neck", "amulet"):
        return "Neck"
    if slot == "ring":
        return "Rings"
    if slot == "ammo":
        # Section audit: melee's row holds ammo-SLOT worn items (blessings,
        # Ghommal's lucky penny), not ammunition — renamed accordingly.
        return "Ammo slot" if tab == "melee" else "Ammunition"

    # Section audit: slotless equipment-record gaps — dupe/variant ids of
    # real gear that fell through slot dispatch into the fallback.
    if tab == "melee":
        if nlow in ("armadyl godsword", "bandos godsword", "scythe of vitur",
                    "rune scimitar", "dinh's blazing bulwark",
                    # Section audit: the plain Warhammer (ex-misc "General
                    # tools") has no equipment record but is a weapon.
                    "warhammer"):
            return "Weapons"
        if nlow == "new crystal shield":
            return "Shields & defenders"
        if nlow == "verac's brassard":
            return "Body"

    # Mage fallthrough — runic / enchanting items, training tools.
    if tab == "mage":
        # Section audit: uncharged trident variants lack equipment records
        # but are weapons like their charged siblings.
        if "trident" in nlow:
            return "Weapons"
        if any(k in nlow for k in ("bones", "soul", "enchant", "wand")):
            return "Enchanting & skilling magic"
        return "Spell utility & supplies"

    return "Training & utility"


def _section_combat_simple(item: dict, tab: str) -> str:
    """Brief #66: simple-mode combat routing. Identical to _section_combat
    except Head / Body / Legs / Hands / Feet all collapse into "Armor"."""
    s = _section_combat(item, tab)
    if s in ("Head", "Body", "Legs", "Hands", "Feet"):
        return "Armor"
    return s


# Slot rank for ordering items inside the simple-mode Armor section.
_SIMPLE_ARMOR_SLOT_RANK = {
    "head": 0, "body": 1, "legs": 2, "hands": 3, "feet": 4,
}


def armor_slot_rank(slot: str) -> int:
    """Return the simple-mode Armor section's within-section slot rank.
    Used by the Java layout builder to keep head→body→legs→hands→feet
    visually adjacent inside the collapsed Armor block."""
    if slot is None:
        return 99
    return _SIMPLE_ARMOR_SLOT_RANK.get(slot.lower(), 99)


def _section_prayer(item: dict) -> str:
    name = _name(item)
    nlow = name.lower()
    slot = _slot(item)
    # Section audit: god-book precursors ("An incomplete book of...") and
    # the Ruinous powers tome row with the completed god books, whatever
    # their slot; shade coffins exist solely to carry shade remains; the
    # symbol family (holy/unholy/unblessed/unpowered) belongs to the row
    # literally named for it; the Blessed crystal scarab is a dosed prayer
    # restore that the "blessed" keyword would otherwise claim.
    if "damaged book" in nlow or "ruinous powers" in nlow:
        return "Holy symbols, books & blessings"
    if "coffin" in nlow:
        return "Pyre logs & shade remains"
    if nlow.endswith(" symbol"):
        return "Holy symbols, books & blessings"
    if "crystal scarab" in nlow:
        return "Prayer-restoring consumables"
    # Equipment FIRST (Brief #60 reorder: prayer tab leads with gear).
    # Tab audit: weapon-slot items too (croziers, white weapons, Verac's
    # flail, prayer-restoring godswords) — they were falling to the
    # utility fallback.
    if slot in ("body", "legs", "head", "feet", "hands", "cape", "neck", "ring",
                "shield", "weapon", "2h"):
        # But blessed-only / book-only shields belong with the holy items.
        if slot == "shield" and any(k in nlow for k in (
                "holy book", "book of", "unholy book", "blessed")):
            return "Holy symbols, books & blessings"
        return "Prayer equipment & robes"
    # Armour set boxes (Proselyte/Initiate harness, Sunfire fanatic set).
    if "harness" in nlow or nlow.endswith(" armour set"):
        return "Prayer equipment & robes"
    if nlow.endswith(" bones") or nlow == "bones" or "bones (" in nlow:
        return "Bones & ashes"
    if nlow.endswith(" ashes") or nlow == "ashes":
        return "Bones & ashes"
    if nlow.startswith("ensouled "):
        return "Ensouled heads"
    # Ectofuntus / altar offerings: bonemeal, slime, ecto-tokens, and the
    # Camdozaal sacrifice fish.
    if any(k in nlow for k in (
        "bonemeal", "bucket of slime", "ecto-token", "ectoplasmator",
        "bird's egg", "crushed bird egg",
        # Section audit: Teomat libation-bowl offerings.
        "soul shard", "tonameyo white",
    )) or nlow in ("guppy", "cavefish", "tetra", "catfish"):
        return "Bonemeal & offerings"
    # Fossil Island prayer training (mycelium pool / ecto).
    if any(k in nlow for k in (
        "fossilised", "fossilized", "enriched bone", "unidentified small fossil",
        "unidentified medium fossil", "unidentified large fossil",
        "unidentified rare fossil", "calcite", "pyrophosphite",
    )):
        return "Fossils & enriched bones"
    # Shade burning + barbarian pyres + the oils that make pyre logs.
    if any(k in nlow for k in (
        "pyre logs", "sacred oil", "olive oil",
        "loar remains", "phrin remains", "riyl remains", "asyn remains",
        "fiyr remains", "urium remains", "shade remains",
        "chewed bones", "mangled bones",
    )):
        return "Pyre logs & shade remains"
    if any(k in nlow for k in (
        "prayer potion", "super restore", "sanfew serum", "prayer mix",
        "prayer renewal", "moonlight moth",
        # Tab audit: the wider prayer-restoring family.
        "prayer enhance", "prayer regeneration", "revitalisation",
        "moonlight potion", "ancient brew", "ancient mix", "forgotten brew",
        "egniol potion", "ambrosia", "tears of elidinis",
        "jug of sunfire wine", "honey locust",
    )):
        return "Prayer-restoring consumables"
    if any(k in nlow for k in (
        "bonecrusher", "ash sanctifier", "dragonbone necklace",
    )):
        return "Bone-processing utility"
    if any(k in nlow for k in ("holy symbol", "unholy symbol", "holy book",
                               "blessed", "page", "blessing",
                               # Tab audit: raid prayer scrolls, spirit
                               # shield sigils/elixir, Leagues sigils and
                               # the wrench/book family read as holy kit.
                               "prayer scroll", "sigil", "holy elixir",
                               "holy wrench", "prayer book", "soul bearer")):
        return "Holy symbols, books & blessings"
    return "Bone-processing utility"


# Drinks & brews matching. Empty vessels stay in Ingredients (checked
# before the drink tokens fire); brewing inputs (hops / yeast / barley /
# unfermented wine) also stay in Ingredients by design.
_DRINK_EMPTY_VESSELS = {
    "beer glass", "cocktail glass", "empty cup", "cup", "tankard",
    "empty beer glass", "cocktail shaker", "calquat keg", "jug",
}
# Exact names — short tokens ("gin", "rum") are too substring-happy.
_DRINK_EXACT = {
    "gin", "vodka", "whisky", "brandy", "rum", "beer", "grog",
    "hot chocolate", "chocolatey milk", "ahab's beer", "beer tankard",
    "keg of beer", "bottle of wine", "jug of wine", "jug of bad wine",
    "cup of tea", "nettle tea", "tea flask", "damiana tea", "karamjan rum",
    "braindeath 'rum'", "bandit's brew", "poison chalice",
    "chilhuac red", "eclipse red", "ixcoztic white", "tonameyo white",
    "metztonalli white", "fortis ash white", "xochipaltic rosé",
    "moonrise wines", "elias white", "sunbeam ale", "asgoldian ale",
    "dragon inn tankard",
}
# Substring tokens for the brew families + gnome cocktails.
_DRINK_TOKENS = (
    "asgarnian ale", "axeman's folly", "chef's delight", "dragon bitter",
    "dwarven stout", "greenman's ale", "greenmans ale", "slayer's respite",
    "moonlight mead", "mind bomb", "mature wmb", "cider", "kelda stout",
    " stout", " mead", "cup of tea", "nettle tea",
    "blurberry special", "fruit blast", "dirty blast", "pineapple punch",
    "wizard blizzard", "short green guy", "drunk dragon", "choc saturday",
    "chocolate saturday", "blurberry barrel",
    "mixed blast", "mixed blizzard", "mixed dragon", "mixed punch",
    "mixed saturday", "mixed sgg", "mixed special",
    "odd cocktail", "unfinished cocktail",
    "premade blurb' sp.", "premade choc s'dy", "premade dr' dragon",
    "premade fr' blast", "premade fr't blast", "premade p' punch",
    "premade sgg", "premade wiz blz'd",
    "juice",
)


def _is_cooking_drink(nlow: str) -> bool:
    if nlow in _DRINK_EMPTY_VESSELS:
        return False
    if nlow in _DRINK_EXACT:
        return True
    if "unfermented" in nlow:  # wine-making intermediate → Ingredients
        return False
    if "wine" in nlow and "winema" not in nlow:
        return True
    if nlow.endswith(" ale") or " ale(" in nlow or " ale (" in nlow \
            or nlow.endswith(" beer") or " beer(" in nlow:
        return True
    return any(k in nlow for k in _DRINK_TOKENS)


# Chambers of Xeric / Gauntlet / newer fish and meats that the canonical
# id-set lists don't carry. Matched by name pattern in _section_cooking.
_EXTRA_COOKED_FISH_EXACT = {
    "catfish", "cavefish", "tetra", "guppy", "loach", "caviar",
    "eel sushi", "rainbow fish", "sacred eel", "lava eel", "harpoonfish",
    "corrupted paddlefish", "crystal paddlefish", "chopped tuna",
    # Sailing-era cooked catches (bare names = cooked form).
    "corrupted shark", "giant krill", "haddock", "yellowfin", "bluefin",
    "marlin", "yellow fin", "poison karambwan",
}
_EXTRA_COOKED_MEAT_EXACT = {
    "bat shish", "ugthanki meat", "locust meat", "fat snail",
    "lean snail", "thin snail", "roast frog", "giant frog legs",
    "coated frogs' legs", "seasoned legs", "spider on stick",
    "spicy minced meat", "minced meat",
}

# Section audit: edible one-offs stranded in the combat tabs' fallbacks —
# all wiki-verified as eatable food. Only consulted by _section_combat.
_EXTRA_COMBAT_FOOD_EXACT = {
    "abyssal potato", "cooked mystery meat", "honey locust",
    "varlamorian kebab", "stymphike tartare", "cabbage", "premade ttl",
    "peach",
}

# Section audit: stat-boosting ales/brews with no dose suffix that were
# stranded in combat-tab fallbacks (Blood pint boosts Att/Str, Lizardkicker
# boosts Ranged, the mind bomb family + Kraken ink stout boost Magic).
_COMBAT_STAT_DRINKS = {
    "blood pint", "lizardkicker", "wizard's mind bomb", "mature wmb",
    "mind bomb(m4)", "kraken ink stout", "steamforge brew",
}


def _section_cooking(item: dict) -> str:
    """Brief #66 (extended): granular cooking section routing.

    Priority is: burnt/ruined → cooking tools → drinks & brews →
    combo food → baked goods → cooked fish → cooked meat → raw fish →
    raw meat → name-pattern extensions → Ingredients (fallback).
    Combo food beats cooked-fish so Cooked karambwan routes to Combo
    food (its combo-eating role wins over its fish role).
    """
    name = _name(item)
    nlow = name.lower()

    # Burnt always wins — name prefix is reliable. Brief #90: fish and
    # meat burns pair with their raw/cooked rows; composite burns
    # (pies, pizzas, gnome cookery) keep the tab-closing Burnt food row.
    # "Ruined X" (Chambers of Xeric) and "over-cooked" join Burnt food.
    if nlow.startswith("burnt "):
        if name in BURNT_FISH_NAMES:
            return "Burnt fish"
        if name in BURNT_MEAT_NAMES:
            return "Burnt meat"
        return "Burnt food"
    if nlow.startswith("ruined ") or "over-cooked" in nlow \
            or nlow.startswith("rotten "):
        return "Burnt food"

    # Cooking tools + chef's outfit. "Cooking cape" / "Cooking hood"
    # belong here too — the outfit shouldn't scatter into ingredients.
    if any(k in nlow for k in ("rolling pin", "pestle and mortar",
                               "mixing bowl", "skewer", "cooking gauntlets",
                               "cook's outfit", "cook's hat", "cook's apron",
                               "cook's shirt", "cook's trousers", "cook's boots",
                               "chef's hat", "chef hat",
                               "cooking cape", "cooking hood",
                               "cooks' assistant",
                               "cooking pot", "iron spit", "kettle", "teapot",
                               "cocktail shaker", "batta tin", "crunchy tray",
                               "gnomebowl mould", "cake tin",
                               "cocktail guide", "gianne's cook book",
                               "cookery book", "cocktail book",
                               "sigil of the chef",
                               "sigil of the infernal chef")):
        return "Cooking tools & utensils"
    # Knife is a cooking tool but only when its slot isn't weapon (the
    # combat knife slots as weapon and lives in melee).
    if "knife" in nlow and not nlow.startswith("dagger") and _slot(item) != "weapon":
        if nlow == "knife" or " knife" in nlow:
            return "Cooking tools & utensils"

    # Drinks & brews — before the combo-food id set so gnome cocktails
    # (Premade fr't blast etc.) land here rather than in Combo food, and
    # before the ingredient check so ales/wines don't sink into the dump.
    if _is_cooking_drink(nlow):
        return "Drinks & brews"

    # Combo food before cooked_fish so Cooked karambwan lands here.
    if _is(item, "combo_food"):
        return "Combo food"
    if _is(item, "baked_goods"):
        return "Baked & cooked goods"
    if _is(item, "cooked_fish"):
        return "Cooked fish"
    if _is(item, "cooked_meat"):
        return "Cooked meat"

    # Raw items — raw_fish wins over raw_cookables because raw_cookables
    # is a superset.
    if _is(item, "raw_fish"):
        return "Raw fish"
    if _is(item, "raw_cookables"):
        return "Raw meat"

    # Brief #76: Fruits + Vegetables fold into Ingredients (separate
    # sections retired). The id-set hits still get routed here.
    if (_is(item, "fruits") or _is(item, "vegetables")
            or _is(item, "ingredients")):
        return "Ingredients"

    # Name-pattern fallbacks for items the canonical lists don't carry.
    if nlow.startswith("raw ") or nlow.endswith(" snail meat") \
            or nlow == "spider carcass" or nlow == "crab meat" \
            or nlow.endswith(" crab meat"):
        # Best guess: any unrecognized raw item is raw meat.
        return "Raw meat"
    # Sailing shellfish / squid catches (bare name = raw form).
    if nlow.endswith(" squid") or nlow.endswith(" crab"):
        return "Raw fish"
    # Chambers of Xeric board food: "Pysk fish (0)" … "Kyren fish (6)"
    # and "Guanic bat (0)" … "Psykk bat (6)".
    if " fish (" in nlow:
        return "Cooked fish"
    if " bat (" in nlow:
        return "Cooked meat"
    if nlow in _EXTRA_COOKED_FISH_EXACT or nlow.endswith("fish"):
        return "Cooked fish"
    if nlow in _EXTRA_COOKED_MEAT_EXACT:
        return "Cooked meat"
    # Generic cooked/roast prefixes — the fish id-set already claimed the
    # cooked fish, so what's left is meat/game (kebbits, antelope, etc.).
    if nlow.startswith("cooked ") or nlow.startswith("roast "):
        return "Cooked meat"
    if any(k in nlow for k in ("pie", "cake", "stew", "kebab", "curry",
                               "bread", "fishcake", "sandwich", "baguette",
                               "burger", "scrambled egg", "chilli con carne",
                               "fried ", "gumbo", "grubs à la mode",
                               "green gloop soup", "stuffed snake", "muffin",
                               "egg and tomato", "tuna and corn",
                               "spinach roll", "choc-ice", "field ration",
                               "wrapped oomlie")) or nlow == "roll":
        return "Baked & cooked goods"
    if any(k in nlow for k in ("pizza", "gnomebowl", "gnome batta",
                               "gnome crunchies", "guthix rest", "halibut",
                               "batta", "crunchies", "crunchy", "crunch",
                               "premade ttl",
                               "worm hole", "veg ball", "choc bomb",
                               "chocolate bomb", "tangled toad")):
        return "Combo food"
    # Default — everything else in the cooking tab is a recipe input
    # (produce sacks, spices, servery stock, brewing ingredients, ...).
    return "Ingredients"


def _section_woodcutting_firemaking(item: dict) -> str:
    """Brief #63: combined Woodcutting + Firemaking section assignment.
    The fletching-specific items that used to share this tab now live in
    the `fletching` tab and are routed by _section_fletching()."""
    name = _name(item)
    nlow = name.lower()
    slot = _slot(item)
    if ("axe" in nlow and "pickaxe" not in nlow and slot in ("weapon", "2h", "")) \
            or "machete" in nlow:
        return "Axes & machetes"
    if _is(item, "pyre_logs") or "pyre logs" in nlow:
        return "Pyre logs"
    # Tab audit: pyre-log oils + Ancient Caverns barbarian pyre bones pair
    # with the pyre logs they burn with.
    if any(k in nlow for k in ("sacred oil", "olive oil",
                               "chewed bones", "mangled bones")):
        return "Pyre logs"
    if _is(item, "logs") or nlow.endswith(" logs") or nlow.endswith(" log"):
        return "Logs"
    if any(k in nlow for k in ("tinderbox", "firelighter", "bruma torch")):
        return "Tinderboxes & firelighting tools"
    # Tab audit: light sources are the firemaking product family — they
    # were a third of the old Misc utility clump.
    if any(k in nlow for k in ("lantern", "candle", "oil lamp", "light orb",
                               "unlit torch", "lit torch", "mining helmet",
                               "torch")) and "bruma" not in nlow:
        return "Light sources & lamps"
    if any(k in nlow for k in ("shade key", "fiyr remains", "loar remains",
                                "phrin remains", "riyl remains", "asyn remains",
                                "shade remains", "urium remains")):
        return "Shade items"
    if any(k in nlow for k in ("wintertodt", "bruma kindling", "bruma herb",
                                "bruma root", "kindling", "warm gloves",
                                "rejuvenation potion", "burnt page", "supply crate")):
        return "Wintertodt & minigame items"
    if any(k in nlow for k in ("pyromancer", "firemaking cape", "firemaking hood")):
        return "Firemaking outfit"
    if any(k in nlow for k in ("lumberjack", "forester ", "woodcutting cape",
                                "woodcutting hood")):
        return "Woodcutting outfit"
    if any(k in nlow for k in ("forestry", "forester's ration", "secateurs",
                                "anti-poaching",
                                "felling axe", "fox whistle", "log basket",
                                "pheasant", "bird egg",
                                # Tab audit: Forestry-event kit and rewards.
                                "anima-infused bark", "cape pouch",
                                "clothes pouch", "log brace", "mulch",
                                "nature offerings", "petal garland",
                                "strange pollen", "beehive", "sturdy harness",
                                "twitcher's gloves", "sawmill voucher",
                                "sigil of woodcraft", "leprechaun charm",
                                "smoker canister")):
        return "Forestry items"
    return "Misc utility"


def _section_fletching(item: dict) -> str:
    """Brief #75 (revised): per-product-type sections so a player can scan
    the tab by intent.

    Routing order matters — checks proceed from most specific to least.

      Tools         knife + fletching cape/hood
      Logs          all log types (cross-tag from Woodcutting + Firemaking)
      Feathers      feathers + every kind of bowstring
      Arrows        arrow shafts, headless arrows, finished arrows (all tiers)
      Arrowheads    arrowhead/arrowtip materials only
      Bows          unstrung + strung shortbows/longbows (paired by tier in sort)
      Crossbows     crossbow stocks + unstrung + finished crossbows (paired by tier)
      Bolts         bolt tips + unfinished bolts + finished bolts (incl. gem-tipped)
      Darts         dart tips + finished darts
      Javelins      javelin heads + finished javelins
      Misc fletching   anything else (safety net)
    """
    nlow = _name(item).lower()
    slot = _slot(item)

    # 1. Tools — knife + fletching cape/hood. Chisel is a crafting tool.
    if "knife" in nlow and slot != "weapon":
        return "Tools"
    if "fletching cape" in nlow or "fletching hood" in nlow:
        return "Tools"

    # 2. Logs — cross-tagged from WC, used to fletch bows.
    if _is(item, "logs") or nlow.endswith(" logs") or nlow.endswith(" log") \
            or nlow == "logs":
        return "Logs"

    # 3. Feathers. Section audit: bowstrings/flax moved out — the divider
    # says "Feathers" and strings aren't feathers; they close the tab in
    # Misc fletching (no strings row in the fixed tier-row design).
    if "feather" in nlow:
        return "Feathers"
    if "bow string" in nlow or "magic string" in nlow \
            or "crossbow string" in nlow or "flax" in nlow:
        return "Misc fletching"

    # 4. Arrow chain — shafts → headless → finished arrows (incl. broad).
    if "arrow shaft" in nlow or "headless arrow" in nlow:
        return "Arrows"
    if nlow.endswith(" arrows") or nlow.endswith(" arrow"):
        return "Arrows"

    # 5. Arrowheads — material-only row before Bows.
    if "arrowhead" in nlow or "arrowtip" in nlow \
            or nlow.endswith(" arrow tip") or nlow.endswith(" arrow tips") \
            or nlow.endswith(" arrowtips"):
        return "Arrowheads"

    # 6. Bows — unstrung + strung shortbows/longbows, comp bows.
    if "shortbow" in nlow or "longbow" in nlow \
            or "comp bow" in nlow or "composite bow" in nlow:
        return "Bows"

    # 7. Crossbows — stocks + unstrung + finished. "Stock" check must
    # come before bolt checks because Wooden/Oak/etc stocks have no
    # "crossbow" token in their names.
    if "crossbow stock" in nlow or nlow.endswith(" stock"):
        return "Crossbows"
    if "crossbow" in nlow and not any(k in nlow for k in ("string", "limb")):
        return "Crossbows"
    # Section audit: Ballista limbs are a ballista part — they row with the
    # other ballista components in Javelins, not with crossbow limbs.
    if (" limb" in nlow or nlow.endswith(" limbs")) and "ballista" not in nlow:
        return "Crossbows"

    # 8. Bolts — tips, unfinished, finished (incl. gem-tipped + (p) poisons).
    # Note: merged names include "Barb bolttips" (no space) and "Bronze
    # bolts (unf)" / "Adamant bolts(unf)" — match both shapes. Grapples
    # are fletched crossbow ammo (tab audit).
    if "grapple" in nlow:
        return "Bolts"
    if ("bolt tip" in nlow or "bolttip" in nlow
            or nlow.endswith(" bolt tips")
            or "unfinished bolt" in nlow or "unfinished broad" in nlow
            or "bolts (unf)" in nlow or "bolts(unf)" in nlow):
        return "Bolts"
    if (nlow.endswith(" bolts") or nlow.endswith(" bolt")
            or nlow.endswith(" bolts (p)") or nlow.endswith(" bolts (p+)")
            or nlow.endswith(" bolts (p++)")):
        return "Bolts"

    # 9. Darts — tips + finished. Atlatl darts are Forestry-era fletching
    # ammo that share the dart workflow. Blowpipes are the fletched
    # dart-launchers (Sailing woods), so they lead this row (tab audit).
    if "blowpipe" in nlow:
        return "Darts"
    if "dart tip" in nlow or nlow.endswith(" dart tips") or "atlatl dart" in nlow:
        return "Darts"
    if nlow.endswith(" darts") or nlow.endswith(" dart"):
        return "Darts"

    # 10. Javelins — heads/tips/shafts + finished. Note: merged wiki names
    # use "javelin tips" where osrsbox said "javelin heads". Ballista
    # parts (frames, springs, monkey tail, incomplete/unstrung ballistae)
    # are fletched and fire javelins, so they row here (tab audit).
    if ("javelin head" in nlow or "javelin tip" in nlow
            or "javelin shaft" in nlow
            or nlow.endswith(" javelin tips")):
        return "Javelins"
    if nlow.endswith(" javelins") or nlow.endswith(" javelin"):
        return "Javelins"
    if any(k in nlow for k in ("ballista", "heavy frame", "light frame",
                               "monkey tail")):
        return "Javelins"

    return "Misc fletching"


def _section_fishing(item: dict) -> str:
    nlow = _name(item).lower()
    # Brief #60: ID-set lookups first.
    if _is(item, "raw_fish"):
        return "Raw fish"
    if _is(item, "fishing_tools"):
        return "Fishing tools"
    if _is(item, "fishing_bait"):
        return "Bait & consumables"
    # Tab audit: trophy fish + stuffed variants + big-net loot get their
    # own row (checked before the raw-fish patterns so "Big shark" doesn't
    # read as a catchable).
    if nlow.startswith("big ") or nlow.startswith("stuffed ") \
            or nlow in ("golden tench", "casket", "oyster", "oyster pearl",
                        "oyster pearls", "fishing trophy", "giant carp"):
        return "Trophies & big catches"
    if any(k in nlow for k in ("angler", "spirit angler", "fish barrel",
                               "fishing cape", "fishing hood",
                               "fish sack", "rada's blessing")):
        return "Fishing outfit"
    # Tab audit: harpoons/rods/nets that weren't in the canonical tool
    # list (ornamented + Sailing-era variants).
    if any(k in nlow for k in ("harpoon", "fishing rod", "fly fishing",
                               "drift net", "tiny net", "tackle box",
                               "fishbowl", "shark lure", "bailing bucket",
                               "barnacle blaster", "karambwan vessel",
                               "lobster pot", "fishing net")) \
            and "harpoonfish" not in nlow:
        return "Fishing tools"
    # Boost potions / pies + worm-family baits.
    if any(k in nlow for k in ("fishing potion", "fishing mix",
                               "super fishing", "admiral pie", "fish pie",
                               "sandworms", "cave worms", "diabolic worms",
                               "fish chunks", "fish offcuts", "bait",
                               "feather pack", "roe", "caviar",
                               "fish food")):
        return "Bait & consumables"
    # Every remaining catchable: raw fish outside the canonical list
    # (Sailing / CoX / Camdozaal), leaping fish, aerial-fishing creatures,
    # minnows and the eel family. Safe inside this tab — fur-bearing raw
    # items don't get tagged fishing.
    if nlow.startswith("raw ") or nlow.startswith("leaping ") \
            or nlow.endswith(" eel") or nlow.endswith("fish") \
            or nlow in ("minnow", "bluegill", "common tench", "greater siren",
                        "whitefish", "pufferfish", "fresh fish",
                        "fresh monkfish", "frog spawn", "karambwanji",
                        "sacred eel", "infernal eel", "slimy eel"):
        return "Raw fish"
    if any(k in nlow for k in ("tempoross", "fishing rod-o-matic", "spirit flake",
                               "soaked page")):
        return "Fishing minigame items"
    return "Fishing minigame items"


# Brief #64 removed _section_firemaking — its logic moved into
# _section_woodcutting_firemaking() in Brief #63.


# Brief #76 (revised): per-gem-family row layout for the Gems section.
# Each gem gets its own row: Uncut X → X → X ring → X necklace → X amulet
# → X bracelet. Precious gems (gold-bar jewellery) sort before
# semi-precious (silver-bar jewellery). Family rank also drives the
# row order. Dragonstone necklace uses the canonical wiki name
# "Dragon necklace"; topaz jewellery drops the "Red" prefix.
_GEM_FAMILY_TOKENS = (
    ("sapphire",     "Sapphire"),
    ("emerald",      "Emerald"),
    ("ruby",         "Ruby"),
    ("diamond",      "Diamond"),
    ("dragonstone",  "Dragonstone"),
    ("dragon necklace", "Dragonstone"),
    ("onyx",         "Onyx"),
    ("zenyte",       "Zenyte"),
    ("opal",         "Opal"),
    ("jade",         "Jade"),
    ("red topaz",    "Red topaz"),
)


def _gem_family(item: dict):
    """Return the gem family name for an uncut gem, cut gem, or piece of
    gem-set jewellery. Used by the crafting Gems section to cluster each
    gem's full workflow on a single row. Returns None for non-gem items.
    """
    nlow = _name(item).lower()
    # Topaz jewellery uses "Topaz" without "Red" but raw gem is "Red topaz".
    # Match the jewellery form first so it doesn't fall into a generic case.
    if any(k in nlow for k in (
            "topaz ring", "topaz necklace", "topaz amulet", "topaz bracelet")):
        return "Red topaz"
    for tok, fam in _GEM_FAMILY_TOKENS:
        if tok in nlow:
            return fam
    return None


def _section_crafting(item: dict) -> str:
    name = _name(item)
    nlow = name.lower()
    # Brief #76: Moulds split out from Crafting tools.
    if " mould" in nlow or nlow.endswith(" mould") or nlow.endswith(" moulds"):
        return "Moulds"
    # Brief #76 (revised): any gem or gem-set jewellery routes to Gems.
    # The Java layout sorts by family + sub-rank to produce one row
    # per gem (uncut → cut → ring → necklace → amulet → bracelet).
    # Tab audit: amethyst is chisel-cut too.
    if _gem_family(item) is not None or "amethyst" in nlow:
        return "Gems"
    # Glassmaking inputs/outputs — checked before Crafting tools so Bucket
    # of sand / Seaweed / Giant seaweed route to glassmaking instead of
    # the tools dump. Bronze wire / Woad leaf / Knife no longer in this
    # branch — they're force_excluded from the tab via mapping.py.
    # Tab audit: blown-glass lamp/lantern products join the row.
    if any(k in nlow for k in (
            "molten glass", "soda ash", "seaweed", "bucket of sand",
            "beer glass", "lantern lens", "unpowered orb", "light orb",
            "lantern", "oil lamp", "fishbowl", "empty candle")):
        return "Glassmaking"
    # Battlestaff crafting — elemental orbs + the staves they're attached
    # to (tab audit; was falling to the outfit fallback). Checked after
    # Glassmaking so "Unpowered orb" / "Light orb" stay with the glass.
    if nlow == "battlestaff" or nlow.endswith(" orb"):
        return "Battlestaves & orbs"
    # Brief #76: Crafting tools per the OSRS Wiki — chisel, needle,
    # glassblowing pipe, spinning wheel. Knife removed (it's fletching/
    # cooking). Thread lives in Spinning materials.
    if any(k in nlow for k in ("chisel", "needle", "shears",
                               "glassblowing pipe", "spinning wheel",
                               "lyre", "enchanted lyre",
                               "gem pouch", "gem sack", "gem satchel",
                               "gem tote", "bag full of gems")) \
            and _slot(item) != "weapon":
        return "Crafting tools"
    # GEM_TIER fallthrough removed — _gem_family above already catches
    # every gem and gem-set jewellery item earlier in the dispatch.
    if "dragonhide" in nlow and not any(k in nlow for k in (" body", " chaps", " vamb")):
        return "Hides & leather"
    if "leather" in nlow and _slot(item) == "":
        return "Hides & leather"
    # Tab audit: furs / hides / skins are tanning-and-needle inputs.
    if any(k in nlow for k in (" fur", "fur ", "hide", "skin", "cowhide")) \
            and _slot(item) == "":
        return "Hides & leather"
    # Tab audit: textile chain — flax→bowstring, wool, hemp/cotton/linen
    # yarns and cloth bolts, sinew, hair.
    if any(k in nlow for k in ("wool", "thread", "flax", "sinew",
                               "hemp", "cotton", "linen", "yarn",
                               "bolt of cloth", "bolt of canvas",
                               "fine cloth", "xerician fabric",
                               "jute fibre")) or nlow == "hair":
        return "Spinning materials"
    if "soft clay" in nlow or "pot lid" in nlow or "pottery" in nlow \
            or "unfired" in nlow or nlow == "clay":
        return "Pottery & clay"
    if any(k in nlow for k in ("gold bar", "silver bar", "amulet of", "ring of",
                               "necklace of", "bracelet of")) and _slot(item) == "":
        return "Jewellery materials"
    # Tab audit: furnace-jewellery inputs — DT2 vestige/icon ring
    # components and unstrung gold/silver pieces.
    if any(k in nlow for k in ("unstrung", "(u)", "vestige")) \
            or nlow.endswith(" icon") or nlow == "silver sickle" \
            or "silver bolts" in nlow:
        return "Jewellery materials"
    if _slot(item) in ("neck", "amulet", "ring"):
        return "Crafted jewellery"
    if _slot(item) in ("body", "legs", "head", "feet", "hands", "cape", "shield"):
        return "Crafted armour & leather goods"
    # Tab audit: chisel-worked monster parts (fangs, visages, shells,
    # kebbit claws, Fremennik rock-shell pieces).
    # Section audit: "eternal gem" removed — it IS a gem (crafted with a
    # gold bar into the eternal slayer ring); pinned to Gems by id.
    if any(k in nlow for k in ("fang", "visage", "shell", "kebbit claws",
                               "crab claw", "rock-shell", "skull piece",
                               "ribcage piece", "fibula piece",
                               "tormented synapse", "scurrius' spine",
                               "bryophyta's essence",
                               "serpentine", "narwhal horn", "antler",
                               "rabbit foot")):
        return "Monster parts & shells"
    if any(k in nlow for k in ("crafting cape", "crafting hood", "artisan", "tan")):
        return "Crafting outfit & utility"
    return "Crafting outfit & utility"


def _section_mining_smithing(item: dict) -> str:
    name = _name(item)
    nlow = name.lower()
    slot = _slot(item)
    if "pickaxe" in nlow and slot in ("weapon", "2h", ""):
        return "Pickaxes"
    if any(k in nlow for k in ("prospector", "mining cape", "mining hood",
                               "varrock armour", "celestial ring",
                               # Tab audit: outfit/utility additions — gem
                               # containers, boost drinks, ore packs, the
                               # smithing skillcape and misc utility.
                               "smithing cape", "smithing hood",
                               "gem pouch", "gem sack", "gem satchel",
                               "gem tote", "ore pack", "dwarven stout",
                               "kovac's grog", "steamforge brew",
                               "smithing catalyst", "ring of forging",
                               # Section audit: worn mining utility that used
                               # to mislabel as "Smithed armour" — the glove
                               # tiers, the lamp helmet and the soft-clay
                               # bracelet are mining aids, not smithed output.
                               "mining gloves", "mining helmet",
                               "bracelet of clay",
                               # Between a Rock: worn to mine the Arzinian
                               # gold mine, so it's mining gear in practice.
                               "gold helmet",
                               "mining tome")):
        return "Mining outfit & utility"
    if "cannonball" in nlow:
        return "Cannonballs & ammo outputs"
    # Tab audit: Shayzien supply armour is a 30-piece smith-for-favour
    # family — one row keeps it out of every other section.
    if nlow.startswith("shayzien supply"):
        return "Shayzien supply armour"
    # Tab audit: minigame / special-deposit minerals (MLM, Volcanic Mine,
    # Camdozaal, Zalcano, Shooting Stars, Mount Karuulm salts, Blast Mine,
    # the Varlamore infernal-smithing chain).
    if any(k in nlow for k in (
            "ore fragment", "pay-dirt", "nugget", "unidentified minerals",
            "barronite deposit", "barronite shards", "daeyalt",
            "tephra", "zalcano shard", "stardust", "star fragment",
            "basalt", "efh salt", "te salt", "urt salt", "salax salt",
            "dynamite", "juniper charcoal", "volcanic ash",
            "volcanic sulphur", "granite dust", "granite (",
            "sandstone (", "large rock", "calcified deposit",
            "infernal shale", "infernal chunk", "infernal nugget",
            "infernal blend", "infernal plate", "rubium",
            "spadeful of coke", "ancient essence")):
        return "Special ores & minerals"
    if nlow.endswith(" ore") or nlow.endswith(" rocks") \
            or nlow in ("coal", "amethyst", "coal fragment", "elemental metal",
                        "lovakite ore"):
        return "Ores"
    if nlow.endswith(" bar") or "ingot" in nlow:
        return "Bars"
    # Section audit: the Imcando hammer is wielded (slot=weapon) but it is
    # a smithing tool, not a smithed weapon — route every hammer here.
    if "hammer" in nlow or "ammo mould" in nlow:
        return "Smithing tools"
    # Section audit: minigame gear that carries a combat slot. Colossal
    # blade + preform are Giants' Foundry (reward / work-in-progress);
    # the heat-proof vessel is the Volcanic Mine water carrier ("Prevents
    # the water from evaporating!") that happens to sit in the weapon slot.
    if nlow in ("colossal blade", "preform", "heat-proof vessel"):
        return "Giants' Foundry & minigame items"
    # Tab audit: anvil-smithed parts + boss materials finished at an anvil
    # (limbs, unf bolts, javelin heads, pick heads, Sailing keel parts,
    # godsword shards, visages, dragon metal).
    # NOTE: merged wiki names use "javelin tips" where osrsbox said
    # "javelin heads" — match both shapes.
    if nlow == "chain" or any(k in nlow for k in (
            "bolts (unf)", "bolts(unf)", "javelin heads", "javelin tips",
            " limbs",
            "pick head", "keel parts", "wire", "iron sheet",
            "godsword blade", "godsword shard", "shield left half",
            "shield right half", "dragon metal", "visage",
            "grapple tip", "oathplate shards",
            "lantern (unf)", "lantern frame", "tormented synapse",
            "scurrius' spine", "aquanite tendon", "broken armour",
            "rusty sword", "broken zombie")):
        return "Smithed parts & components"
    if any(k in nlow for k in ("foundry", "giant's foundry",
                               "smiths uniform")):
        return "Giants' Foundry & minigame items"
    # Section audit: the old "Smithed armour" label lied — every slot item
    # left here is worn smithing gear (Smiths' outfit, goldsmith gauntlets,
    # ice gloves, blacksmith's helm, the Between a Rock gold helmet), not
    # anvil output. "Smithed weapons" (4 items, zero smithed weapons) was
    # dissolved into Smithing tools / Giants' Foundry above.
    if slot in ("weapon", "2h", "body", "legs", "head", "feet", "hands",
                "shield"):
        return "Smithing outfit & gloves"
    return "Giants' Foundry & minigame items"


def _section_herblore(item: dict) -> str:
    """Brief #76: flat layout (tab audit added two rows).

      Tools         pestle + vials + grinder + amulet of chemistry
      Herblore outfit & utility  cape/hood, alchemist set, sacks/boxes,
                    shop packs and potion sets
      Herbs         grimy + clean herbs (HERB_ORDER sort in Java)
      Secondaries   all secondary ingredients
      Unfinished potions  (unf) doses
      Finished potions    all doses (incl. barbarian mixes + divine ext.)
      Mastering Mixology items   pastes + rewards from the minigame
    """
    name = _name(item)
    nlow = name.lower()

    # Tools — pestle, vials, grinder, etc. Section audit: the chemistry/
    # alchemy amulets are WORN gear → moved to the outfit row.
    if any(k in nlow for k in ("vial", "pestle and mortar",
                               "grinder", "herb bowl", "sample bottle",
                               "chugging barrel")):
        return "Tools"

    # Tab audit: outfit + containers + shop packs + boxed potion sets.
    # Section audit adds the worn amulets, the Herblore-boosting pie and
    # the Greenman's ale family (a Herblore-boost drink, never an
    # ingredient).
    if any(k in nlow for k in ("herblore cape", "herblore hood", "botanist",
                               "herb sack", "herb box", "forager's pouch",
                               "reagent pouch", "amylase pack",
                               "eye of newt pack", "olive oil pack",
                               "potion pack", "potion set",
                               "amulet of chemistry", "amulet of alchemy",
                               "botanical pie", "greenman's ale",
                               "sigil of the alchemist",
                               "sigil of the potion master")):
        return "Herblore outfit & utility"

    # Mastering Mixology — pastes, lab liquids and reward gear. Checked
    # before the dose-suffix branches so "Mixalot" style rewards don't
    # leak (tab audit).
    if any(k in nlow for k in ("mox paste", "aga paste", "lye paste",
                               "aldarium", "alco-augmentator",
                               "aqualux amalgam", "azure aura mix",
                               "liplack liquor", "mammoth-might mix",
                               "marley's moonlight", "megalite liquid",
                               "mixalot", "mystic mana amalgam",
                               "mixology", "alchemist's amulet",
                               "alchemist's signet", "alchemist pants",
                               "alchemist top", "alchemist hood",
                               "prescription goggles")):
        return "Mastering Mixology items"

    # Herbs — grimy + clean.
    if _is(item, "grimy_herbs") or _is(item, "clean_herbs"):
        return "Herbs"
    if nlow.startswith("grimy "):
        return "Herbs"

    # Unfinished potions — "(unf)" suffix in any spacing. Section audit:
    # the "Unfinished potion/serum" quest intermediates and the
    # herb-and-water mixtures ("Needs another ingredient") join the row.
    if "(unf)" in nlow or " unf " in nlow or nlow.endswith(" unf") \
            or nlow.startswith("unfinished ") \
            or nlow in ("snakeweed mixture", "ardrigal mixture",
                        "herb tea mix"):
        return "Unfinished potions"

    # Weapon poison ("Weapon poison", "Weapon poison(+)", "Weapon
    # poison(++)") carries no dose suffix, so route it explicitly.
    if nlow.startswith("weapon poison"):
        return "Finished potions"

    # Finished potions — any dose suffix (1)-(4) goes here, including
    # the previously-separate barbarian mixes and divine/extended variants.
    if any(nlow.endswith(s) for s in ("(4)", "(3)", "(2)", "(1)")):
        return "Finished potions"

    # Curated secondaries set.
    if _is(item, "herblore_secondaries"):
        return "Secondaries"

    # Default — anything the LLM routed here that doesn't match above is
    # a secondary ingredient.
    return "Secondaries"


def _section_agility_thieving(item: dict) -> str:
    nlow = _name(item).lower()
    if "graceful" in nlow or "agility cape" in nlow or "agility hood" in nlow \
            or any(k in nlow for k in (
                # Tab audit: weight-reducers, agility pets/tomes and the
                # shortcut kit (grapples, ropes).
                "boots of lightness", "spotted cape", "spottier cape",
                "penance gloves", "agility tome", "ring of endurance",
                "grapple", "long rope", "sage's greaves",
                # Section audit: graceful recolour dye rows with the
                # graceful gear it recolours.
                "dark dye",
                "nimbleness charm")):
        return "Agility outfit & graceful"
    if any(k in nlow for k in ("energy potion", "stamina potion", "agility potion",
                               "summer pie", "explorer's ring",
                               # Tab audit: the full run-restore family.
                               "super energy", "energy mix", "stamina mix",
                               "agility mix", "sq'irk", "amylase",
                               "egniol potion")):
        return "Run-energy consumables"
    if any(k in nlow for k in ("mark of grace", "agility ticket", "shayzien lap",
                               "wilderness agility ticket",
                               # Tab audit: course/minigame currencies +
                               # Hallowed Sepulchre rewards (hence the
                               # section-audit rename to "& rewards").
                               "agility arena ticket", "brimhaven voucher",
                               "hallowed", "dark acorn",
                               "dark key", "calcified acorn",
                               "glistening tear")):
        return "Agility marks & rewards"
    if "rogue" in nlow or "thieving cape" in nlow or "thieving hood" in nlow \
            or nlow.startswith("ham ") \
            or "gloves of silence" in nlow or "thieving tome" in nlow:
        return "Thieving outfit & rogue set"
    # Section audit: Coin pouch removed (pickpocket LOOT, not a tool);
    # Flash powder added (Rogues' Den blinding tool you deploy).
    if any(k in nlow for k in ("lockpick", "blackjack", "stethoscope",
                               "key impressioning", "dodgy necklace",
                               "flash powder", "bandit's brew")):
        return "Thieving tools"
    return "Thieving loot & artefacts"


def _section_farming(item: dict) -> str:
    nlow = _name(item).lower()
    if any(k in nlow for k in ("spade", "rake", "watering can", "secateurs",
                               "trowel", "plant pot", "magic secateurs",
                               "farming cape", "farming hood", "farmer",
                               "gricoller", "bottomless compost",
                               # Tab audit: harvest containers + patch kit.
                               "basket", "empty sack", "sack pack",
                               "seed box", "seed dibber", "scarecrow",
                               "hay sack", "leprechaun")):
        return "Farming tools"
    if "compost" in nlow or any(k in nlow for k in (
            # Tab audit: the wider soil-treatment family.
            "plant cure", "saltpetre", "sulphurous fertiliser",
            "demon dung", "volcanic ash", "ritual mulch")):
        return "Compost & soil treatment"
    if "sapling" in nlow or "seedling" in nlow:
        return "Saplings"
    # Brief #60: data-driven seed sub-categorization. Each set was built from
    # canonical OSRS seed names; the test happens BEFORE name fallback so
    # mushroom spores (which don't end in "seed") still land correctly.
    if _is(item, "seeds_allotment"):
        return "Allotment seeds"
    if _is(item, "seeds_hops"):
        return "Hops seeds"
    if _is(item, "seeds_herb"):
        return "Herb seeds"
    if _is(item, "seeds_flower"):
        return "Flower seeds"
    if _is(item, "seeds_bush"):
        return "Bush seeds"
    if _is(item, "seeds_tree"):
        return "Tree seeds"
    if _is(item, "seeds_fruit_tree"):
        return "Fruit tree seeds"
    if _is(item, "seeds_special"):
        return "Special seeds"
    # Name-pattern fallback for any seed/sapling/spore not in the curated sets.
    if nlow.endswith(" seed") or nlow.endswith(" seeds") or nlow.endswith(" spore"):
        return "Special seeds"
    # Tab audit: harvested crops — fruit, veg, hops, grimy herbs, produce
    # sacks (Apples(5) etc.), and the leaves/roots cleared from tree
    # patches. This was 60% of the tab sitting in the fallback.
    if nlow.startswith("grimy ") or _is(item, "grimy_herbs") \
            or _is(item, "fruits") or _is(item, "vegetables"):
        return "Harvested produce"
    if any(nlow.startswith(p) for p in (
            "apples(", "bananas(", "cabbages(", "onions(", "oranges(",
            "potatoes(", "strawberries(", "tomatoes(")):
        return "Harvested produce"
    if nlow.endswith(" hops") or nlow.endswith(" leaves") \
            or nlow.endswith(" roots") or nlow.endswith(" berries") \
            or nlow.endswith(" fruit") or nlow.endswith(" branch"):
        return "Harvested produce"
    if nlow in ("banana", "orange", "pineapple", "papaya fruit", "coconut",
                "half coconut", "coconut shell", "watermelon", "strawberry",
                "apple", "cooking apple", "dragonfruit", "curry leaf",
                "potato", "onion", "cabbage", "tomato", "sweetcorn",
                "mushroom", "grapes", "zamorak's grapes", "grape barrel",
                "marigolds", "nasturtiums", "rosemary", "woad leaf",
                "limpwurt root", "white lily", "cactus spine",
                "potato cactus", "snape grass", "jangerberries",
                "dwellberries", "redberries", "white berries",
                "goutweed", "gout tuber", "barley", "barley malt",
                "grain", "wheat", "giant seaweed", "seaweed",
                "celastrus bark", "golpar", "buchu leaf", "noxifer",
                "huasca", "nightshade", "cave nightshade", "leaves",
                "weeds", "calquat fruit", "white tree fruit",
                "golovanova fruit", "bologano fruit", "logavano fruit",
                "mystery fruit", "jute fibre", "hemp", "cotton boll"):
        return "Harvested produce"
    return "Farmer outfit & contracts"


def _section_runecraft(item: dict) -> str:
    name = _name(item)
    nlow = name.lower()
    # Daeyalt shards become daeyalt essence; sunfire splinters are the
    # sunfire-rune crafting material (tab audit).
    if "essence" in nlow or "daeyalt" in nlow or "sunfire splinters" in nlow:
        return "Essence"
    if "pouch" in nlow and "rune" not in nlow:
        return "Pouches & storage"
    if "rune pouch" in nlow or "abyssal lantern" in nlow:
        return "Pouches & storage"
    if "talisman" in nlow:
        return "Talismans"
    if "tiara" in nlow:
        return "Tiaras"
    # Shop rune packs sit with the runes they contain (tab audit).
    if "rune pack" in nlow:
        return "Core runes"
    if name in _RUNE_NAMES or nlow.endswith(" rune") or nlow.endswith(" runes"):
        # Distinguish combination from core.
        if any(nlow.startswith(p) for p in ("steam ", "mist ", "smoke ",
                                            "lava ", "mud ", "dust ")):
            return "Combination runes"
        return "Core runes"
    # Tab audit: full GotR family — the "of the eye" outfit, guardian
    # stones, charged cells, abyssal dyes/needle/protector and lockbox
    # loot. Checked before the outfit branch so "Hat of the eye" etc.
    # cluster with their minigame.
    if any(k in nlow for k in ("of the eye", "guardian stone",
                               "guardian's eye", "abyssal red dye",
                               "abyssal green dye", "abyssal blue dye",
                               "abyssal needle", "abyssal protector",
                               "lost bag", "tarnished locket",
                               "atlax's diary", "rift guardian")) \
            or nlow.endswith(" cell"):
        return "Guardians of the Rift items"
    if any(k in nlow for k in ("raiments of the eye", "raiments", "runecraft cape",
                               "runecraft hood", "wicked hood", "binding necklace",
                               "tome of fire", "tome of water")):
        return "Runecraft gear & utility"
    if any(k in nlow for k in ("guardian fragments", "abyssal pearls",
                               "intricate pouch", "outfit fragment",
                               "soaked fragment", "guardian essence",
                               "fragments")):
        return "Guardians of the Rift items"
    # Section audit: renamed — bloodbark/swampbark armour, runescrolls and
    # binding jewellery outnumber the literal outfit pieces here.
    return "Runecraft gear & utility"


def _section_hunter(item: dict) -> str:
    nlow = _name(item).lower()
    # Brief #74 fix #11: Huntsman's pack belongs with Hunter tools so it
    # surfaces at the top of the tab next to traps/snares.
    if "huntsman" in nlow:
        return "Hunter tools & traps"
    if "trap" in nlow or "snare" in nlow or "noose wand" in nlow \
            or any(k in nlow for k in (
                # Tab audit: hand-tool additions.
                "teasing stick", "jar generator", "hunter kit",
                "ring of pursuit", "falconer's glove", "cormorant's glove",
                "imp repellent", "unlit torch", "butterfly net",
                "drift net", "quetzal whistle", "quetzal feed",
                "imp-in-a-box", "magic imp box")):
        return "Hunter tools & traps"
    # Tab audit: the fletched-from-hunter weapon family.
    if any(k in nlow for k in ("hunters' crossbow", "hunter's crossbow",
                               "kebbit bolts", "hunter's spear",
                               "hunter spear tips", "makeshift spear",
                               "hunters' sunlight crossbow",
                               "barb-tail harpoon")):
        return "Hunter weapons & ammo"
    if "impling" in nlow:
        return "Implings & impling jars"
    if any(k in nlow for k in ("butterfly jar", "magic butterfly",
                               "magic box", "bird jar",
                               # Tab audit: loot/meat carriers.
                               "loot sack", "meat pouch", "fur pouch")) \
            and "impling" not in nlow:
        return "Nets, jars & containers"
    # Tab audit: boost potions + tracking baits.
    if any(k in nlow for k in ("hunter potion", "super hunter",
                               "hunting mix", "spicy minced meat",
                               "spicy tomato", "termites")):
        return "Baits & potions"
    if "chinchompa" in nlow:
        return "Chinchompas"
    # Tab audit: caught salamanders/lizards + the tar fuel they fire.
    if "salamander" in nlow or "swamp lizard" in nlow \
            or nlow in ("guam tar", "marrentill tar", "tarromin tar",
                        "harralander tar", "irit tar", "swamp tar"):
        return "Salamanders & lizards"
    # Tab audit: caught butterflies/moths, their barbarian mixes and wing
    # tertiaries.
    if any(k in nlow for k in ("black warlock", "ruby harvest",
                               "sapphire glacialis", "snowy knight",
                               "moonlight moth", "sunlight moth",
                               "butterfly wing")):
        return "Butterflies & moths"
    if any(k in nlow for k in ("camo", "larupia", "graahk", "kyatt",
                               "spotted cape", "spottier cape", "hunter cape",
                               "hunter hood", "hunter outfit", "guild hunter",
                               "gloves of silence")):
        return "Hunter outfit"
    if any(k in nlow for k in ("birdhouse", "bird house", "bird nest",
                               "nest box", "raven egg")):
        return "Birdhouse items"

    # Brief #76: split former "Creature products" into furs / meats /
    # tertiaries so players can find fur runs / meat-only runs / tertiary
    # drops without scrolling a 60-item bucket.

    # Furs first — any item with "fur" in the name (kebbit furs, polar fur,
    # pyre fox fur, etc.) plus the named hide variants.
    if "fur" in nlow:
        return "Furs & hides"
    if any(k in nlow for k in ("antler", "hide ",)) and "outfit" not in nlow:
        return "Furs & hides"

    # Hunter meats — raw meat from hunter creatures, plus their cooked
    # forms (tab audit: cooked kebbit/antelope/fox).
    if nlow.startswith("raw ") or "bird meat" in nlow or "beast meat" in nlow \
            or "kebbit meat" in nlow \
            or (nlow.startswith("cooked ") and any(k in nlow for k in (
                "kebbit", "antelope", "fox", "larupia", "graahk", "kyatt",
                "barb-tailed"))):
        return "Hunter meats"

    # Everything else from hunter (tertiary drops): kebbit claws, kebbit
    # teeth, kebbit teeth dust, long kebbit spike, stripy feather, etc.
    return "Hunter tertiaries"


def _section_construction(item: dict) -> str:
    nlow = _name(item).lower()
    # Brief #74 fix #12: Sawmill coupons/vouchers have "plank" in their name
    # but are Forestry vouchers, not actual planks. Route them out of Planks.
    if "sawmill" in nlow or "voucher" in nlow or "coupon" in nlow:
        return "Blueprints & contracts"
    if "plank" in nlow:
        return "Planks"
    if "nail" in nlow:
        return "Nails"
    # Tab audit: Marlo's crate + supply crate are Mahogany Homes props.
    if "marlo" in nlow or "supply crate" in nlow:
        return "Blueprints & contracts"
    # Section audit: "carpenter" removed — the Carpenter's outfit is
    # wearable Mahogany Homes reward gear, not a tool (falls through to
    # the outfit & rewards fallback).
    if any(k in nlow for k in ("saw", "hammer", "construct cape", "construction cape",
                               "construction hood")):
        return "Construction tools"
    # Tab audit: POH garden — bagged trees/plants/flowers and hedges.
    if nlow.startswith("bagged ") or "hedge" in nlow or "topiary" in nlow:
        return "Garden & bagged plants"
    # Tab audit: trophy-room / wall decor — mounted heads, stuffed
    # trophies, paintings, portraits, maps and display jars.
    if nlow.startswith("stuffed ") or nlow.startswith("jar of ") \
            or any(k in nlow for k in ("painting", "portrait", " map",
                                       "kbd heads", "kq head", "basilisk head",
                                       "cockatrice head", "kurask head",
                                       "abyssal head", "hydra heads",
                                       "vorkath's stuffed head",
                                       "crawling hand", "big bass",
                                       "big swordfish", "big shark",
                                       "big harpoonfish")):
        return "Mounted heads & decor"
    # Tab audit: flatpack furniture — beds, benches, wardrobes, lecterns,
    # study/globes, kitchen barrels etc. The 85xx-96xx flatpack families
    # all match one of these tokens.
    if any(k in nlow for k in (
            "chair", " bench", " bed", "four-poster", "bookcase",
            "wardrobe", "dresser",
            "drawers", " table", "lectern", "cape rack", "armour case",
            "toy box", "treasure chest", "fancy dress box", "shaving stand",
            "clock", "globe", "orrery", "armillary sphere", "telescope",
            "crystal ball", "crystal of power", "elemental sphere",
            "demon", "eagle", "shoe box", "beer barrel", "cider barrel",
            "asgarnian ale", "greenman's ale", "dragon bitter",
            "chef's delight", "kitchen table", " rug", "curtains")):
        return "Flatpacks & furniture"
    if any(k in nlow for k in ("gold leaf", "limestone", "marble block",
                               "silver bar", "cloth", "soft clay", "clay",
                               "magic stone", "molten glass",
                               "bucket of sand", "thatch spar",
                               "mahogany logs", "teak logs",
                               "long bone", "curved bone")):
        return "Building materials"
    # Section audit: "Teleport-to-house items" deleted — house tablets
    # live in the teleports tab and the section sat empty. The contract
    # row is renamed "Blueprints & contracts" (blueprints + vouchers
    # outnumber the Mahogany Homes props).
    if any(k in nlow for k in ("mahogany homes", "carpentry contract", "amys saw",
                               "plank sack", "mythical max cape", "blueprint")):
        return "Blueprints & contracts"
    return "Construction outfit & rewards"


def _section_slayer(item: dict) -> str:
    name = _name(item)
    nlow = name.lower()
    if any(k in nlow for k in ("enchanted gem", "eternal gem", "slayer ring",
                               "slayer's enchantment", "mysterious emblem",
                               "ancient emblem", "totem", "ancient effigy",
                               "ancient relic")):
        return "Slayer assignment items"
    # Section audit: renamed from "Mandatory protection" — the row is
    # "items a task forces you to bring" (finishers included), and the
    # Shayzien armour (anti-shaman-spawn) joins it. Brittle key removed —
    # it's a Grotesque Guardians access key, not task kit.
    if any(k in nlow for k in ("nose peg", "earmuffs", "facemask", "mirror shield",
                               "spiny helmet", "witchwood icon",
                               "fungicide", "ice cooler", "bag of salt")) \
            or nlow.startswith("shayzien "):
        return "Mandatory task items"
    # Section audit: slayer-unlock / slayer-drop weapons fold in here —
    # they ARE core slayer gear (whips, bludgeon, dragon hunter weapons,
    # demonbane, blowpipe/tridents, keris, blisterwood, ivandis rods).
    if any(k in nlow for k in ("slayer helmet", "broad arrows", "broad bolts",
                               "broad darts", "leaf-bladed", "darklight",
                               "rock hammer", "rock thrownhammer", "slayer staff",
                               "slayer's staff", "slayer gloves", "boots of stone",
                               "boots of brimstone", "granite gloves", "granite ring",
                               "granite hammer", "salve amulet", "black mask",
                               "smouldering stone", "hydra leather", "ferocious gloves",
                               "abyssal whip", "abyssal tentacle", "abyssal bludgeon",
                               "abyssal dagger", "dragon hunter", "arclight",
                               "emberlight", "scorching bow", "purging staff",
                               "blowpipe", "trident", "keris partisan",
                               "chainmace", "blisterwood", "warped sceptre",
                               "rod of ivandis", "ivandis flail")):
        return "Core slayer gear"
    if "cannon" in nlow:
        return "Cannon & burst supplies"
    if "cannonball" in nlow:
        return "Cannon & burst supplies"
    # Section audit: rune packs are burst/barrage restock supplies.
    if nlow.endswith(" rune pack"):
        return "Cannon & burst supplies"
    # Section audit: teleport jewellery BEFORE the dose-suffix branch —
    # "Amulet of glory(4)" is a charge count, not a potion dose. Full
    # charge families included so (1)-(5) variants stop landing in
    # Combat potions / Misc utility.
    if any(k in nlow for k in ("teleport", "drakan's medallion", "royal seed pod",
                               "ectophial", "skills necklace", "amulet of glory",
                               "ring of dueling", "xeric's talisman",
                               "burning amulet", "combat bracelet",
                               "necklace of passage", "games necklace")):
        return "Teleports"
    # Section audit: Slayer's respite is a beer keg family, not a potion.
    if "respite" in nlow:
        return "Misc utility"
    if any(nlow.endswith(s) for s in ("(4)", "(3)", "(2)", "(1)")):
        if any(k in nlow for k in ("prayer", "restore", "sanfew")):
            return "Prayer & restores"
        return "Combat potions"
    # Section audit: route ALL cooked food via the shared id sets — the
    # old 8-name whitelist stranded ~22 cooked foods in Misc utility.
    if (name in _TOP_FOOD
            or _is(item, "cooked_fish") or _is(item, "cooked_meat")
            or _is(item, "combo_food") or _is(item, "baked_goods")
            or nlow in {"shark", "manta ray", "anglerfish",
                        "dark crab", "tuna potato", "monkfish",
                        "cooked karambwan", "sea turtle"}):
        return "Food"
    if any(k in nlow for k in ("rune pouch", "herb sack", "seed box", "gem bag",
                               "gem pouch", "coal bag", "looting bag",
                               "bonecrusher", "ash sanctifier", "ava's", "avas")):
        return "Loot management"
    # Section audit: task-monster heads, stuffed trophies and champion
    # scrolls — promoted out of the 335-item Misc utility fallback.
    if (nlow.startswith("ensouled ") or nlow.startswith("stuffed ")
            or nlow.endswith(" head") or nlow.endswith(" heads")
            or "champion scroll" in nlow):
        return "Monster heads & trophies"
    # Section audit: boss drops finished/attached elsewhere — visages,
    # hydra/drake/basilisk parts, bludgeon pieces, dragon metal, crystals.
    if any(k in nlow for k in ("visage", "hydra's", "hydra tail", "hydra claw",
                               "drake's", "basilisk jaw", "tanzanite fang",
                               "magic fang", "bludgeon spine", "bludgeon claw",
                               "bludgeon axon", "dragon metal", "nihil horn",
                               "nihil shard", "nihil dust", "chromium ingot",
                               "vestige", "eternal crystal", "pegasian crystal",
                               "primordial crystal", "black tourmaline core",
                               "dark claw", "imbued heart", "saturated heart",
                               "ancient hilt")):
        return "Boss drops & upgrade parts"
    return "Misc utility"


_TOP_FOOD = {"Shark", "Manta ray", "Anglerfish", "Dark crab", "Tuna potato",
             "Monkfish", "Cooked karambwan", "Sea turtle"}


_MISC_KEY_RE = _re.compile(r"\bkeys?\b")


def _section_misc(item: dict) -> str:
    """Misc audit redesign: sections derived from the actual contents of
    the old ~1200-item Uncategorized clump — keys, Leagues sigils, XP
    lamps, fossils, books/documents, minigame/PvM rewards, consumables."""
    nlow = _name(item).lower()
    if nlow == "coins" or any(k in nlow for k in (
            "platinum token", "trading sticks", "blood money", "tokkul",
            "numulite", "ecto-token", "hallowed mark", "old school bond",
            "pieces of eight", "temple coin", "old coin", "ancient coin")):
        return "Currency & exchange tokens"
    if any(k in nlow for k in ("amulet of glory", "ring of dueling",
                               "skills necklace", "games necklace", "combat bracelet",
                               "ring of wealth", "digsite pendant", "necklace of passage",
                               "burning amulet", "slayer ring", "ring of returning",
                               "karamja gloves", "desert amulet")):
        return "Jewellery teleports"
    if any(k in nlow for k in ("teleport", "tablet", "scroll of redirection",
                               "ectophial", "enchanted lyre", "ardougne cloak",
                               "kharedst's memoirs", "drakan's medallion",
                               "royal seed pod", "xeric's talisman",
                               # Section audit: Leagues relic — "Allows
                               # travel to all banks", it stores nothing.
                               "banker's briefcase",
                               "magic carpet")) \
            and nlow != "stone tablet":
        return "General teleports"
    # Section audit: Sailing buried-treasure keys ("A small key dug up
    # on...") matched the clue "key (" token — they're access keys.
    if nlow.startswith("small key"):
        return "Keys & access"
    # Section audit: "scroll case" added — the 2025 stackable-clues reward
    # cases were reading as Books & documents.
    if any(k in nlow for k in ("clue scroll", "clue bottle", "clue nest",
                               "clue geode", "clue box", "clue compass",
                               "casket", "reward casket",
                               "puzzle box", "challenge scroll", "key (",
                               "scroll box", "scroll case", "mimic kill count",
                               "sextant", "watch", "chart")) or nlow == "mimic":
        return "Clue scroll items"
    # Keys & access — word-boundary match so "monkey ..." doesn't hit "key".
    if (_MISC_KEY_RE.search(nlow) or "lockbox" in nlow
            or nlow.endswith(" locks") or "keyring" in nlow
            or "shantay pass" in nlow):
        return "Keys & access"
    # Leagues / Shattered Relics sigils and trinkets. Section audit:
    # "Unidentified fragment (combat/skilling/…)" are Shattered Relics
    # power items, not fossils.
    if nlow.startswith("sigil of") or nlow.startswith("trinket of") \
            or "unidentified fragment" in nlow:
        return "Sigils & trinkets"
    # Section audit: Emir's Arena / Soul Wars imbue scrolls are reward
    # consumables, not reading material (checked before Books).
    if "imbue scroll" in nlow or nlow == "scroll of imbuing":
        return "Minigame rewards"
    # XP lamps and knowledge books.
    if "lamp" in nlow or "book of knowledge" in nlow \
            or "book of arcane knowledge" in nlow:
        return "Lamps & XP rewards"
    # Fossil Island / Varrock Museum finds. Section audit: the Revenant
    # emblem-trader loot ("ancient statuette/totem/medallion") moved to
    # Minigame rewards with its sibling Ancient emblem.
    if any(k in nlow for k in ("fossil", "unidentified", "uncleaned find",
                               "cracked sample", "ceramic remains",
                               "old chipped vase", "ancient astroscope",
                               "ancient carcanet", "ancient globe",
                               "ancient symbol",
                               "museum map")):
        return "Fossils & museum"
    # Books, notes, letters, scrolls, maps and other paperwork.
    if any(k in nlow for k in ("book", "journal", "diary", "manual",
                               "guide", "note", "letter", "manuscript",
                               "ledger", "treatise", "writings", "poem",
                               "papyrus", "newspaper", "flyer", "map",
                               "census", "parchment", "scroll", "page",
                               "document", "report", "speech", "statement",
                               "incantation", "grimoire", "tome",
                               "scribbles", "message", "invitation",
                               "envoy", "manifesto", "translation",
                               "barcrawl card", "disclaimer",
                               "gielinor's flora", "ballad")):
        return "Books & documents"
    if any(k in nlow for k in ("rune pouch", "herb sack", "seed box", "gem bag",
                               "gem pouch", "coal bag", "looting bag",
                               "tool leprechaun", "tackle box", "fish barrel",
                               "loot sack", "fur pouch", "meat pouch",
                               "reagent pouch", "huntsman's kit", "plank sack",
                               "collection bag", "flamtaer bag", "druid pouch",
                               "hallowed sack", "extradimensional bag",
                               "forager's pouch",
                               "log basket", "coffer", "coffin")):
        return "Utility containers"
    # Minigame + PvM reward items: tickets/tokens/vouchers, Barbarian
    # Assault horns/icons/eggs, GotR cells, BH emblems + crates, PvM capes.
    if any(k in nlow for k in ("castle wars ticket", "blast furnace token",
                               "mta token", "pizazz", "lms shard",
                               "ticket", "token", "voucher", "coupon",
                               "spoils of war", "bounty crate", "supply crate",
                               "emblem", "starter pack", "starter kit",
                               "fresh start helper",
                               # Section audit: Revenant emblem-trader loot
                               # ("of substantial value to emblem traders")
                               # joins its sibling Ancient emblem here.
                               "ancient statuette", "ancient totem",
                               "ancient medallion",
                               "attacker horn", "defender horn", "healer horn",
                               "collector horn", "guardian horn",
                               "attacker icon", "defender icon", "healer icon",
                               "collector icon",
                               "poisoned egg", "poisoned tofu", "poisoned worms",
                               "spiked/pois. egg", "omega egg",
                               "fire cape", "fire max cape", "infernal cape",
                               "infernal max cape", "champion's cape",
                               "clan wars cape", "lightbearer")) \
            or nlow in ("blue egg", "red egg", "green egg", "yellow egg",
                        "weak cell", "medium cell", "strong cell",
                        "overcharged cell", "uncharged cell",
                        "blue icon", "red icon"):
        return "Minigame rewards"
    # Consumables & field supplies.
    if any(nlow.endswith(d) for d in ("(1)", "(2)", "(3)", "(4)")) \
            or any(k in nlow for k in ("potion", "vial", "waterskin",
                                       "bandages", "purple sweets",
                                       "mint cake", "sweets", "cup of tea",
                                       "elixir", "adrenaline",
                                       "shayzien medpack",
                                       "beer", "wine", " rum", " ale",
                                       "'rum'", "mead", "stout")):
        return "Consumables & supplies"
    if any(k in nlow for k in ("chisel", "knife", "hammer", "saw",
                               "fishing", "spade", "rake", "tinderbox",
                               "shears", "bucket", "rope", "toolkit",
                               "paintbrush", "sieve", "rock pick",
                               "monkey wrench", "grapple", "ogre bellows")):
        return "General tools"
    return "Uncategorized"


_QUEST_KEY_RE = _re.compile(r"\bkeys?\b")


def _section_quests(item: dict) -> str:
    """Quests row redesign: capes by name, then equipment by slot, then
    name-family routing (keys / books / consumables / artefacts / remains
    / materials). One-off quest junk closes the tab in the Quest items
    fallback.

    Section audit: the "Diary rewards" branch is gone (diary teleports
    live in the teleports tab, diary jewellery in misc — the section sat
    empty), and "Boss pets & followers" is gone (its only member was Pet
    rock, a quest keepsake pinned to Quest items by id)."""
    nlow = _name(item).lower()
    slot = _slot(item)

    if any(k in nlow for k in ("quest point cape", "quest point hood",
                               "achievement diary cape", "achievement diary hood",
                               "music cape", "music hood", "max cape", "max hood",
                               "completionist cape", "soul cape")):
        return "Achievement capes"
    # Greegrees are Ape Atoll access items despite their weapon slot.
    if "greegree" in nlow:
        return "Keys & access items"
    # Section audit: quest ammunition (fire arrows etc.) rows with the
    # wieldables, not with clothing.
    if slot in ("weapon", "2h", "ammo"):
        return "Weapons & wieldables"
    if slot:
        return "Armour & clothing"
    if (_QUEST_KEY_RE.search(nlow) or "keystone" in nlow
            or _re.search(r"\bemblem\b", nlow) or "crest" in nlow
            or _re.search(r"\bseal\b", nlow) or "medallion" in nlow
            or "talisman" in nlow or _re.search(r"\bpass\b", nlow)
            or "permit" in nlow):
        return "Keys & access items"
    if any(k in nlow for k in ("book", "journal", "diary", "notes", "note",
                               "letter", "scroll", "manuscript", "map",
                               "page", "parchment", "translation", "ledger",
                               "treatise", "poem", "papyrus", "plans",
                               "schematic", "blueprint", "instructions",
                               "manual", "guide", "memoirs", "prophecy",
                               "anthem", "ballad", "portrait", "message",
                               "invoice", "receipt", "census", "folder",
                               "document", "report", "invitation",
                               "sketch", "dossier", "case file",
                               "communiqué", "transcription", "etchings",
                               "declaration", "certificate", "directions",
                               " print", "papers", "recipe", "treaty",
                               "petition", "history", "histories", "volume",
                               "picture", "music sheet")) \
            or nlow in ("iou", "list", "item list") or nlow.endswith(" list") \
            or nlow.endswith(" form"):
        return "Books & lore"
    if any(nlow.endswith(d) for d in ("(1)", "(2)", "(3)", "(4)")) \
            or any(k in nlow for k in ("potion", "brew", " tea", "mixture",
                                       "elixir", "serum", "tonic", "cure",
                                       "balm", "salve", "antipoison",
                                       "pie", "stew", "cake", "wine", " ale",
                                       "beer", " rum", "mead", "cider",
                                       "curry", "kebab", "soup", "broth",
                                       "poison", "keg", "stout", "honey",
                                       "rations", "poultice")) \
            or nlow.startswith("tea ") or nlow == "guthix rest":
        return "Quest consumables"
    # Section audit: "lamp" removed — XP-reward lamps are not artefacts
    # (Antique lamp / Lamp / Magic lamp now live in misc "Lamps & XP
    # rewards" via force lists; the storyline quest lamps fall to Quest
    # items).
    if any(k in nlow for k in ("statuette", "statue", "artefact", "relic",
                               "idol", "totem", "sceptre", "orb", "crystal",
                               "gem", "jewel", "amulet", "necklace",
                               "pendant", "carving", "sculpture", "bust",
                               "figurine", "dolmen", "tablet", "sigil",
                               "effigy", "chalice", "candlestick",
                               "casket", "urn", "heirloom", "prism")):
        return "Artefacts & relics"
    if any(k in nlow for k in ("bone", "skull", "ribs", "remains", "corpse",
                               "carcass", "ashes", "hide", " fur", "feather",
                               "wing", "claw", "tooth", "fang", "tail",
                               "heart", "eye of", "pelt", "beak", "hoof",
                               "scale", "horn")):
        return "Remains & trophies"
    if any(k in nlow for k in ("bucket", "jug", "bowl", "jar", "barrel",
                               "flask", "bottle", "sack", "chest", "box",
                               "crate", "sample", "powder", "dust", "ore",
                               "ingot", "rock", "shard", "fragment", "root",
                               "bark", "branch", "log", "plank", "cloth",
                               "thread", "wool", "dye", "oil", "tar",
                               "resin", "mushroom", "berries", "herb",
                               "leaf", "seed", "egg", "grain", "flour",
                               "nail", "pipe", "cog", "gear", "lever",
                               "wire", "magnet", "candle", "torch", "rope",
                               "vial", "acid", "compound", "solution",
                               "fluid", "extract", "nerve", "pot of",
                               "block", "paste", "glue", "quicklime",
                               "gypsum", "linen", "kelp", "mould", "brush",
                               "pulley", "lens", "sheet", "salt",
                               "charcoal", "water", "tax bag", "clay",
                               "sulphur", "naphtha", "chemical", "fibre",
                               "reed", "vine", "hops", "kindling",
                               "beam")) \
            or nlow.startswith("pot ") or nlow.startswith("ground ") \
            or nlow.startswith("grimy ") \
            or _re.search(r"\bbars?\b", nlow) or _re.search(r"\brunes?\b", nlow):
        return "Quest supplies & materials"
    return "Quest items"


def _section_sailing(item: dict) -> str:
    nlow = _name(item).lower()
    # Cargo crates first — "Crate of cannonballs" / "Crate of coral" etc.
    # would otherwise match the content-family tokens below (tab audit).
    if any(k in nlow for k in ("cargo", "crate", "shipment", "manifest",
                               "courier")):
        return "Cargo & contracts"
    if any(k in nlow for k in ("compass", "spyglass", "crowbar", "captain's log",
                               "sextant", "telescope",
                               # Tab audit: on-deck utility kit.
                               "bailing bucket", "portable weather station",
                               "vacuum pump", "summon boat",
                               "teleport to boat", "barnacle blaster",
                               "list of repairs", "medallion of the deep",
                               "pirate medallion", "boat bottle",
                               "facility bottle")):
        return "Sailing tools & navigation"
    # Brief #90: the construction kit shipwrights carry. Checked before the
    # generic gates so "Hammer" / "Saw" never leak into outfit/rewards.
    if nlow in ("hammer", "imcando hammer", "saw", "crystal saw", "amy's saw"):
        return "Construction tools"
    if nlow.endswith(" nails"):
        return "Nails"
    if nlow == "plank" or nlow.endswith(" plank") or "ship plank" in nlow:
        return "Planks"
    # Tab audit: treasure-hunting keys, lockbox directions, station
    # schematics and the medallion-fragment chain. Checked before the
    # cannon family so "Dragon cannon schematic" reads as a schematic.
    if any(k in nlow for k in ("schematic", "lockbox directions",
                               "small key (", "medallion fragment",
                               "charred key", "fetid key", "serrated key",
                               "stormy key", "waterlogged journal")):
        return "Keys, charts & schematics"
    # Tab audit: ship cannon ammo family (regular / chainshot / incendiary
    # per metal + granite).
    if "cannonball" in nlow or "cannon barrel" in nlow \
            or "dragon cannon" in nlow:
        return "Cannons & cannonballs"
    # Tab audit: deep-sea catches — raw first, then cooked/burnt/big.
    _SAILING_FISH = ("bluefin", "giant krill", "giant blue krill", "haddock",
                     "halibut", "marlin", "yellowfin", "yellow fin",
                     "jumbo squid", "swordtip squid", "crab meat",
                     "orangefin", "purplefin", "whitefish",
                     "rare black lobster")
    if nlow.startswith("raw ") and any(f in nlow for f in _SAILING_FISH):
        return "Raw sailing fish"
    # Section audit: renamed — burnt stages and the stuffable trophy
    # catches share this row.
    if any(f in nlow for f in _SAILING_FISH):
        return "Cooked fish & trophies"
    # Tab audit: sea-monster / seabird parts brought back from encounters.
    if any(k in nlow for k in ("shark jaw", "shark liver", "blubber",
                               "tusk", "orca teeth", "ink sac", "tentacle",
                               "ray fin", "ray skin", "ray barbs",
                               "squid beak", "squid paste", "reef snake",
                               "beak", "feather", "narwhal horn",
                               "marlin scales", "haddock eye",
                               "aquanite tendon", "mogre")):
        return "Sea creature parts"
    # Tab audit: wreck salvage + the junk it decodes into.
    if "salvag" in nlow \
            or any(k in nlow for k in ("mouldy block", "mouldy doll",
                                       "smashed mirror", "rusty coin",
                                       "rusty locket", "dull knife",
                                       "broken hasta", "broken dragon hook",
                                       "sea shell", "fossilised skull",
                                       "tortugan scute", "tarnished")):
        return "Salvage"
    # Tab audit: island-grown resources — Sailing-era woods, fibre crops,
    # reef corals and the new ores.
    if any(k in nlow for k in ("camphor", "ironwood", "rosewood", "jatoba",
                               "hemp", "cotton", "linen", "coral",
                               "elkhorn", "pillar frag", "umbral frag",
                               "lead ore", "nickel ore", "rubium",
                               "mystery fruit", "ent branch", "ent seed")) \
            and "keel" not in nlow and "hull" not in nlow \
            and "repair kit" not in nlow \
            and "blowpipe" not in nlow and "paint" not in nlow \
            and "bottle of" not in nlow:
        return "Island resources"
    # Tab audit: the ~70 "Bottle of ..." boat cocktails + named brews.
    if nlow.startswith("bottle of ") or nlow.startswith("bottled ") \
            or nlow in ("kraken colada", "kraken ink stout",
                        "perildance bitter", "sunken rum",
                        "whirlpool surprise", "the melted rocks",
                        "chuck up's 'stew'", "blackbird red",
                        "trawler's trust", "horizon's lure",
                        "daddy's special water's special water"):
        return "Boat cocktails & brews"
    # Tab audit: pearls — Sailing reward currency, tiny → enormous.
    if nlow.endswith(" pearl") or nlow == "big pearl":
        return "Pearls"
    if any(k in nlow for k in ("hull", "mast", "sail", "rudder", "anchor",
                               "ship component", "keel parts",
                               "dragon metal sheet")) \
            and "sailors'" not in nlow and "sailing" not in nlow:
        return "Ship components"
    if nlow == "chain" or nlow == "tar" or nlow.endswith(" tar") \
            or any(k in nlow for k in (
                               "repair kit", "pitch", "rope coil",
                               "canvas",
                               # Tab audit: sail fabric + ship paints.
                               "fabric roll", "holy sheet", "paint",
                               "cupronickel", "lead bar")):
        return "Shipbuilding materials"
    return "Sailing outfit & rewards"


def _section_cosmetics(item: dict) -> str:
    """Brief #75: route by source/category. Sets (Treasure trails / minigames /
    random events / holidays) get their own sections so each set can claim
    its own row in the layout builder. Ornament kits are upgrade items.
    Anything that doesn't fit a known set source falls to Miscellaneous.
    """
    nlow = _name(item).lower()

    # Ornament/upgrade kits — distinct row family, never part of a set.
    if (nlow.endswith(" ornament kit") or nlow.endswith(" colour kit")
            or "ornament kit" in nlow or "upgrade kit" in nlow):
        return "Ornament kits"

    # Holiday items — fixed event keyword list (tab audit added the
    # cookout event props, marionettes and snowballs).
    if any(k in nlow for k in (
        "partyhat", "h'ween mask", "halloween mask", "santa", "easter",
        "midsummer", "diwali", "thanksgiving", "christmas", "festive",
        "valentine", "gingerbread", "jack lantern", "pumpkin", "bunny",
        "ham joint", "cracker", "snowman", "spectator",
        "yo-yo", "rubber chicken", "rubber duck",
        "(cookout)", "marionette", "snowball", "spooky", "grim reaper",
        "anti-panties", "wintumber",
    )):
        return "Holiday items"

    # Tab audit: skill capes + max capes (with hoods) — checked before the
    # treasure-trail (t) suffix branch so "Cooking cape(t)" stays here.
    if _is_skillcape_family(nlow):
        return "Skill capes & max capes"

    # Tab audit: Leagues / seasonal-event / speedrun rewards — the single
    # biggest family in the old Miscellaneous clump.
    if any(k in nlow for k in (
        "trailblazer", "shattered relic", "twisted relic", "twisted slayer",
        "twisted ancestral", "leagues", "raging echoes", "demonic pacts",
        "annihilation", "armageddon", "grid master", "speedrun",
        "adventurer's", "(t1)", "(t2)", "(t3)", "(cr)", "echo ",
        "deadman", "sigil of", "trinket of", "swift blade", "banker's note",
    )) or nlow.startswith("twisted ") or nlow.startswith("shattered "):
        return "Leagues & speedrun rewards"

    # Tab audit: recoloured skilling/PvP outfit variants.
    if "graceful" in nlow or "slayer helmet" in nlow \
            or any(k in nlow for k in (
        "crystal crown", "crystal body", "crystal helm", "crystal legs",
        "crystal boots", "crystal gloves",
    )):
        return "Recoloured outfits"

    # Tab audit: quest / regional costume families.
    if nlow.startswith("fremennik") or nlow.startswith("ham ") \
            or any(k in nlow for k in (
        "vyre noble", "villager ", "chompy bird hat",
        "pirate bandana", "pirate shirt", "pirate leggings", "pirate boots",
        "pirate's hat", "stripy pirate shirt", "desert shirt", "desert robe",
        "desert skirt", "desert top", "desert legs", "desert boots",
        "menap", "tribal top", "moonclan", "lunar isle",
    )):
        return "Quest & regional outfits"

    # Random event sets — characters/themes from random events.
    if any(k in nlow for k in (
        "mime ", "frog ", "zombie ", "gravedigger", "lederhosen",
        "camo top", "camo legs", "camo helmet", "mime mask", "mime top",
        "mime legs", "mime boots", "mime gloves",
        "zombie head", "zombie hands", "zombie shirt", "zombie trousers",
        "zombie boots",
        "frog mask",
    )):
        return "Random event sets"

    # Minigame sets — keyword anchors per minigame. Section audit: the
    # Trouble Brewing naval wardrobe (naval shirts / navy slacks /
    # tricorn hats, bought with Pieces of eight) moved here from the
    # regional-costumes row — it filled the previously empty section.
    if any(k in nlow for k in (
        "castle wars ", "barbarian assault ",
        "fight pits", "fight arena",
        "soul wars", "stealing creation",
        "trouble brewing", "shades of mort'ton",
        "naval shirt", "navy slacks", "tricorn hat",
        "lms ", "last man standing",
    )):
        return "Minigame sets"

    # Treasure trail sets — Gilded, 3rd age, (g)/(t)/(h1-5) suffixes,
    # elegant, musketeer, themed clue rewards.
    if any(k in nlow for k in (
        "gilded ", "3rd age", "third age", "trimmed",
        "(g)", "(t)", "(h1)", "(h2)", "(h3)", "(h4)", "(h5)",
        "(sk)", "(lg)", "(or)", "(or 1)", "(or 2)",
        "elegant", "musketeer", "dragon mask",
        "robin hood hat", "ranger boots", "ranger gloves",
        "pith helmet", "deerstalker", "katana",
        "wizard boots", "splitbark", "lunar ", "ahrim",
        "leprechaun hat", "samurai ", "highwayman",
        "carnillean", "khazard", "bearhead",
        # Tab audit: clue-reward hat/cane families. Section audit: "staff
        # of collection" removed — the tiered staves are Collection Log
        # milestone rewards, not Treasure Trails items.
        "beret", "boater", "cavalier", "headband", " cane",
        "heraldic helm",
    )):
        return "Treasure trail sets"
    # Tab audit: god vestments (mitre/stole/crozier/cloak/robes), blessed
    # d'hide and rune god armour are all Treasure Trails rewards. NOTE
    # (pass-3 audit): these carry real defence and are hard-excluded from
    # the cosmetics tab via tab_exclude_ids — this branch only fires for
    # any residual zero-stat pieces (e.g. blessings).
    if any(nlow.startswith(g) for g in _GOD_VESTMENT_PREFIXES) \
            and any(p in nlow for p in _GOD_VESTMENT_PIECES + ("blessing",)):
        return "Treasure trail sets"

    # Tab audit: Castle Wars decorative armour is a minigame set (the old
    # branch binned it into Miscellaneous).
    if "decorative " in nlow:
        return "Minigame sets"

    # Dyes — bin into Miscellaneous now that the dedicated section is gone.
    if any(k in nlow for k in ("magic dye",
                               "white dye", "red dye", "blue dye", "yellow dye",
                               "purple dye", "orange dye", "pink dye")):
        return "Miscellaneous cosmetics"

    return "Miscellaneous cosmetics"


# ── Brief #75: set_name resolver for cosmetics row-per-set layout ──────────
#
# Items with the same set_name cluster together with a row break between
# different sets. Items returning None sort normally (no row break).
#
# Pass-3 audit: coverage extended aggressively. Explicit keyword families
# (holiday / leagues / regional / recolours) come first; anything left is
# matched against the data-driven outfit families derived by
# _build_set_families() at init_id_sets time (strip colours + ordinal
# suffixes, group by shared prefix over clothing-piece suffixes, keep
# families with >= 2 distinct pieces).

_HOLIDAY_KEYWORDS = (
    ("partyhat",   "Partyhats"),
    ("marionette", "Marionettes"),
    ("christmas",  "Christmas"),
    ("santa",      "Christmas"),
    ("snowman",    "Christmas"),
    ("gingerbread", "Christmas"),
    ("cracker",    "Christmas"),
    ("h'ween",     "Halloween"),
    ("halloween",  "Halloween"),
    ("jack lantern", "Halloween"),
    ("pumpkin",    "Halloween"),
    ("easter",     "Easter"),
    ("bunny",      "Easter"),
    ("midsummer",  "Midsummer"),
    ("diwali",     "Diwali"),
    ("thanksgiving", "Thanksgiving"),
    ("valentine",  "Valentine"),
    ("festive",    "Festive"),
)

_RANDOM_EVENT_KEYWORDS = (
    ("mime ",       "Mime"),
    ("mime mask",   "Mime"),
    ("mime top",    "Mime"),
    ("mime legs",   "Mime"),
    ("mime boots",  "Mime"),
    ("mime gloves", "Mime"),
    ("frog ",       "Frog"),
    ("frog mask",   "Frog"),
    ("zombie ",     "Zombie"),
    ("zombie head", "Zombie"),
    ("zombie hands","Zombie"),
    ("zombie shirt","Zombie"),
    ("zombie trousers", "Zombie"),
    ("zombie boots","Zombie"),
    ("gravedigger", "Gravedigger"),
    ("lederhosen",  "Lederhosen"),
    ("camo",        "Camo"),
)

_MINIGAME_KEYWORDS = (
    ("castle wars",      "Castle Wars"),
    ("castlewars",       "Castle Wars"),
    ("decorative ",      "Castle Wars"),
    ("barbarian assault","Barbarian Assault"),
    ("fight pits",       "Fight Pits"),
    ("fight arena",      "Fight Arena"),
    ("soul wars",        "Soul Wars"),
    ("stealing creation","Stealing Creation"),
    ("trouble brewing",  "Trouble Brewing"),
    ("shades of mort",   "Shades of Mort'ton"),
    ("last man standing","LMS"),
    ("lms ",             "LMS"),
)

# Pass-3: Leagues / seasonal-event / speedrun reward families.
_LEAGUES_SET_KEYWORDS = (
    ("trailblazer",    "Trailblazer"),
    ("shattered",      "Shattered Relics"),
    ("twisted",        "Twisted League"),
    ("raging echoes",  "Raging Echoes"),
    ("echo ",          "Raging Echoes"),
    ("annihilation",   "Deadman"),
    ("armageddon",     "Deadman"),
    ("deadman",        "Deadman"),
    ("speedrun",       "Speedrunning"),
    ("grid master",    "Grid Master"),
    ("adventurer's",   "Adventurer's"),
    ("league",         "Leagues"),
)

# Pass-3: regional / quest costume families.
_REGIONAL_SET_KEYWORDS = (
    ("vyre noble",      "Vyre noble"),
    ("vyrewatch",       "Vyrewatch"),
    ("villager ",       "Villager"),
    ("fremennik",       "Fremennik casual"),
    ("menap",           "Menaphite"),
    ("desert ",         "Desert clothing"),
    ("tricorn",         "Pirate"),
    ("pirate",          "Pirate"),
    ("naval ",          "Naval"),
    ("navy slacks",     "Naval"),
    ("moonclan",        "Moonclan"),
    ("chompy bird hat", "Chompy bird hats"),
    ("mourner",         "Mourner"),
    ("ham shirt",       "H.A.M."),
    ("ham robe",        "H.A.M."),
    ("ham hood",        "H.A.M."),
    ("ham cloak",       "H.A.M."),
    ("ham boots",       "H.A.M."),
    ("ham gloves",      "H.A.M."),
    ("ham logo",        "H.A.M."),
    ("bomber",          "Bomber"),
)

# Pass-3: coloured-wardrobe clothes ("Pink hat", "Teal robe bottoms", team
# capes …) group as single recolour families.
_WARDROBE_COLOURS = (
    "red", "blue", "green", "yellow", "purple", "pink", "orange",
    "cream", "turquoise", "teal", "grey", "gray", "black", "white",
    "brown",
)
_WARDROBE_PIECES = (
    "hat", "robe top", "robe bottoms", "boots", "gloves", "cape",
    "skirt", "shirt", "blouse",
)

# ── Pass-3: data-driven outfit family derivation ────────────────────────────

# Clothing-piece suffixes recognised when deriving families. Two-word
# suffixes are tried before single-word ones.
_SET_PIECE_SUFFIXES_2 = ("robe top", "robe bottoms", "robe bottom",
                         "robe legs")
_SET_PIECE_SUFFIXES_1 = (
    "hat", "hood", "mask", "top", "shirt", "blouse", "torso", "trousers",
    "legs", "bottoms", "skirt", "robe", "boots", "shoes", "sandals",
    "gloves", "mitts", "hands", "cape", "cloak", "head", "tunic",
    "leggings", "slacks", "bandana", "shorts", "scarf", "apron",
    "jacket", "vest", "socks", "wig", "cap", "body", "outfit",
)
_SET_ORDINAL_SUFFIX_RE = _re.compile(r"\s*\((?:t?\d+|[a-z]{1,3}\d*)\)\s*$")


def _build_set_families(items_by_id: dict[int, dict]) -> None:
    """Populate _DERIVED_SET_BY_NAME: lowercased item name → derived
    outfit set name. Families are formed by stripping colour adjectives
    and ordinal/parenthetical suffixes, then grouping names that share a
    base prefix ahead of a clothing-piece suffix. Only families with two
    or more DISTINCT pieces survive — singletons keep set_name None."""
    _DERIVED_SET_BY_NAME.clear()
    colour_set = set(_WARDROBE_COLOURS)
    families: dict[str, set[str]] = {}
    members: dict[str, list[str]] = {}
    seen_names: set[str] = set()
    for iid, it in items_by_id.items():
        if it.get("noted") or it.get("placeholder") or it.get("duplicate"):
            continue
        raw = (it.get("name") or "").strip().lower()
        if not raw or raw in seen_names:
            continue
        seen_names.add(raw)
        stripped = _SET_ORDINAL_SUFFIX_RE.sub("", raw)
        tokens = [t for t in stripped.split() if t not in colour_set]
        if len(tokens) < 2:
            continue
        piece = None
        if len(tokens) >= 3 and " ".join(tokens[-2:]) in _SET_PIECE_SUFFIXES_2:
            piece = " ".join(tokens[-2:])
            base_tokens = tokens[:-2]
        elif tokens[-1] in _SET_PIECE_SUFFIXES_1:
            piece = tokens[-1]
            base_tokens = tokens[:-1]
        if not piece:
            continue
        base = " ".join(base_tokens)
        if not base or base in colour_set:
            continue
        families.setdefault(base, set()).add(piece)
        members.setdefault(base, []).append(raw)
    for base, pieces in families.items():
        if len(pieces) < 2:
            continue
        label = base[0].upper() + base[1:] + " outfit"
        for raw in members[base]:
            _DERIVED_SET_BY_NAME[raw] = label


def _set_name(item: dict) -> str | None:
    """Brief #75: return a set_name string for cosmetic items that
    visibly cluster as a set, or None otherwise. Used by the Java
    layout builder to insert a row break between different sets and
    keep set members adjacent on the same row.
    """
    name = _name(item)
    nlow = name.lower()

    # Pass-3: ALL skill capes (trimmed + untrimmed + hoods + max capes)
    # form one set so they row together, trimmed next to untrimmed.
    if _is_skillcape_family(nlow):
        return "Skill capes"

    # Treasure trail "Gilded" set.
    if nlow.startswith("gilded "):
        return "Gilded"

    # 3rd age — grouped by combat style based on item keywords.
    if "3rd age" in nlow or "third age" in nlow:
        if any(k in nlow for k in ("range", "bow", "vamb", "coif", "leather")):
            return "3rd age range"
        if any(k in nlow for k in ("mage", "robe", "wand", "hat", "amulet")):
            return "3rd age mage"
        if "druid" in nlow:
            return "3rd age druidic"
        # Default 3rd age bucket — melee plate / longsword / kiteshield.
        return "3rd age melee"

    # (g) / (t) trim sets — group by armor metal prefix.
    if nlow.endswith("(g)") or nlow.endswith(" (g)"):
        base = name.rsplit("(g)", 1)[0].strip()
        first = base.split()[0] if base else ""
        if first:
            return f"{first.capitalize()} (g)"
        return "(g)"
    if nlow.endswith("(t)") or nlow.endswith(" (t)"):
        base = name.rsplit("(t)", 1)[0].strip()
        first = base.split()[0] if base else ""
        if first:
            return f"{first.capitalize()} (t)"
        return "(t)"

    # Heraldic sets — (h1) through (h5).
    for n in range(1, 6):
        marker = f"(h{n})"
        if marker in nlow:
            return f"Heraldic {n}"

    # Elegant set (colour suffix groups each as their own set).
    if "elegant" in nlow:
        # "Black elegant shirt", "Black elegant legs", "Pink elegant blouse" ...
        for color in ("black", "white", "red", "purple", "pink", "green",
                      "gold", "blue"):
            if nlow.startswith(color + " elegant") or f" {color} elegant" in nlow:
                return f"Elegant {color}"
        return "Elegant"

    # Musketeer set.
    if "musketeer" in nlow:
        return "Musketeer"

    # Pith helmet + explorer set.
    if "pith helmet" in nlow or "explorer" in nlow:
        return "Explorer"

    # Robin hood hat lives alone — no set, return None.

    # Pass-3: recolour families.
    if "graceful" in nlow:
        return "Graceful"
    if "slayer helmet" in nlow:
        return "Slayer helmets"
    if "crystal crown" in nlow:
        return "Crystal crowns"
    if nlow.startswith("team-") or nlow.startswith("team cape"):
        return "Team capes"

    # Pass-3: Leagues / seasonal / speedrun reward families.
    for kw, label in _LEAGUES_SET_KEYWORDS:
        if kw in nlow:
            return label

    # Pass-3: regional / quest costume families.
    for kw, label in _REGIONAL_SET_KEYWORDS:
        if kw in nlow:
            return label

    # Holiday items.
    for kw, label in _HOLIDAY_KEYWORDS:
        if kw in nlow:
            return label

    # Random events.
    for kw, label in _RANDOM_EVENT_KEYWORDS:
        if kw in nlow:
            return label

    # Minigames.
    for kw, label in _MINIGAME_KEYWORDS:
        if kw in nlow:
            return label

    # Pass-3: coloured-wardrobe clothes — one recolour family.
    for colour in _WARDROBE_COLOURS:
        if nlow.startswith(colour + " "):
            rest = nlow[len(colour) + 1:]
            if rest in _WARDROBE_PIECES:
                return "Coloured wardrobe"
            break

    # Pass-3: data-driven outfit families (>= 2 distinct pieces sharing a
    # base prefix after colour/ordinal stripping).
    derived = _DERIVED_SET_BY_NAME.get(nlow)
    if derived:
        return derived

    # No pattern matched — item sorts on its own with no row break.
    return None


# Tab audit: standard/Ancient/Lunar/Arceuus teleport tablets carry plain
# "<Place> teleport" names — enumerate them so the Spellbook tablets row
# actually fills.
_TELEPORT_TABLET_NAMES = {
    # Standard spellbook
    "varrock teleport", "lumbridge teleport", "falador teleport",
    "camelot teleport", "ardougne teleport", "watchtower teleport",
    "teleport to house", "kourend castle teleport", "civitas illa fortis teleport",
    # Ancient magicks
    "annakarl teleport", "carrallanger teleport", "dareeyak teleport",
    "ghorrock teleport", "kharyrll teleport", "lassar teleport",
    "paddewwa teleport", "senntisten teleport",
    # Lunar
    "moonclan teleport", "ourania teleport", "waterbirth teleport",
    "barbarian teleport", "khazard teleport", "fishing guild teleport",
    "catherby teleport", "ice plateau teleport",
    # Arceuus
    "arceuus library teleport", "draynor manor teleport", "mind altar teleport",
    "salve graveyard teleport", "fenkenstrain's castle teleport",
    "west ardougne teleport", "cemetery teleport", "barrows teleport",
    "harmony island teleport", "battlefront teleport", "ape atoll teleport",
    "guthixian temple teleport",
}

# Master-scroll-book teleport scrolls (plus point-shop scroll teleports).
_TELEPORT_SCROLL_NAMES = {
    "nardah teleport", "digsite teleport", "feldip hills teleport",
    "lunar isle teleport", "mort'ton teleport", "pest control teleport",
    "piscatoris teleport", "tai bwo wannai teleport", "iorwerth camp teleport",
    "mos le'harmless teleport", "lumberyard teleport", "zul-andra teleport",
    "key master teleport", "revenant cave teleport", "watson teleport",
    "spider cave teleport", "brimhaven teleport", "pollnivneach teleport",
    "rimmington teleport", "taverley teleport", "rellekka teleport",
    "trollheim teleport", "yanille teleport", "prifddinas teleport",
    "hosidius teleport", "aldarin teleport", "volcanic mine teleport",
    "target teleport",
}

# Achievement-diary / combat-achievement reward teleports (Brief: the 30
# diary-reward items were force_included; give them a shared row).
_TELEPORT_DIARY_PREFIXES = (
    "ardougne cloak", "explorer's ring", "karamja gloves",
    "wilderness sword", "fremennik sea boots", "kandarin headgear",
    "desert amulet", "morytania legs", "western banner", "rada's blessing",
    "ghommal's hilt", "ghommal's avernic defender", "achievement diary cape",
    "music cape", "ardougne max cape",
)


def _section_teleports(item: dict) -> str:
    """Tab audit: rows grouped by teleport form — runes, jewellery,
    tablets, master-scroll-book scrolls, diary rewards, skilling
    transport unlocks, wilderness access, quest unlocks — with true
    one-offs (spheres, memoir pages, sceptres) in the fallback.

    Brief #66 added the Teleport runes section at the top — checked first
    via the canonical TELEPORT_RUNE_NAMES id set.
    """
    if _is(item, "teleport_runes"):
        return "Teleport runes"
    nlow = _name(item).lower()
    if any(k in nlow for k in (
        "amulet of glory", "amulet of eternal glory", "skills necklace",
        "ring of dueling", "ring of wealth", "combat bracelet",
        "games necklace", "digsite pendant", "necklace of passage",
        "burning amulet", "slayer ring", "drakan's medallion",
        "ring of the elements", "ring of returning", "giantsoul amulet",
        "sailors' amulet", "pendant of ates", "globetrotter pendant",
        "amulet of the eye", "xeric's talisman", "ring of shadows",
        # Section audit: the fang's only teleport role is charging
        # Xeric's talisman — it rows with the talisman it charges.
        "lizardman fang",
    )):
        return "Mounted & charged jewellery"
    # Section audit: one-time unlock tablets (Slepey → Drakan's medallion;
    # the DT2 lore tablets) are not breakable spellbook teleports.
    if nlow in ("slepey tablet", "strangled tablet", "sirenic tablet",
                "scarred tablet", "frozen tablet", "ancient tablet"):
        return "Special & one-time"
    # Section audit: wilderness-destination teleports grouped BEFORE the
    # tablet form-check — the danger row was authored in the tab design
    # but sat after the tablet branch, so it only ever caught one item.
    # ("Wilderness sword" is exempt: it's a diary reward.)
    if ("wilderness" in nlow and "wilderness sword" not in nlow) \
            or "revenant cave" in nlow \
            or "ice plateau" in nlow or "annakarl" in nlow \
            or "carrallanger" in nlow or "dareeyak" in nlow \
            or "ghorrock" in nlow or "cemetery" in nlow:
        return "Wilderness teleports"
    if nlow in _TELEPORT_TABLET_NAMES or "tablet" in nlow \
            or " teleport (tab)" in nlow:
        return "Spellbook tablets"
    # Section audit: the Chronicle is charged by Teleport cards (already
    # in this row) and needs no quest — they belong together.
    if nlow in _TELEPORT_SCROLL_NAMES or "teleport scroll" in nlow \
            or "master scroll book" in nlow or nlow == "teleport card" \
            or nlow == "chronicle" \
            or nlow == "scroll of redirection":
        return "Teleport scrolls"
    if any(nlow.startswith(p) for p in _TELEPORT_DIARY_PREFIXES):
        return "Diary & reward teleports"
    # Skilling transport unlocks — Varlamore quetzal whistles, Weiss/Troll
    # Stronghold basalt, gnome seed pods, Fossil Island mushtrees etc.
    if any(k in nlow for k in (
        "quetzal whistle", "icy basalt", "stony basalt", "basalt",
        "grand seed pod", "royal seed pod", "calcified moth",
        "magic mushtree", "spirit tree",
    )):
        return "Skill destinations"
    # Quest-unlock teleports: Kharedst's memoirs + its pages, crystals,
    # sceptres, the ectophial and friends.
    if any(k in nlow for k in (
        "kharedst's memoirs", "a dark disposition", "history and hearsay",
        "jewellery of jubilation", "lunch by the lancalliums",
        "the fisher's flute", "secret page",
        "ectophial", "teleport crystal", "crystal teleport seed",
        "pharaoh's sceptre", "skull sceptre", "dramen staff",
        "mokhaiotl waystone",
    )):
        return "Quest-locked teleports"
    return "Special & one-time"


_TAB_TO_FN = {
    "melee": lambda it: _section_combat(it, "melee"),
    "range": lambda it: _section_combat(it, "range"),
    "mage":  lambda it: _section_combat(it, "mage"),
    # Brief #66: simple-mode variants share the same item universe but
    # collapse armor slots into "Armor". Looked up by tab id when the
    # per-tab simple-mode config flag is on.
    "melee_simple": lambda it: _section_combat_simple(it, "melee"),
    "range_simple": lambda it: _section_combat_simple(it, "range"),
    "mage_simple":  lambda it: _section_combat_simple(it, "mage"),
    "prayer": _section_prayer,
    "cooking": _section_cooking,
    "woodcutting_firemaking": _section_woodcutting_firemaking,
    "fletching": _section_fletching,
    "fishing": _section_fishing,
    "crafting": _section_crafting,
    "mining_smithing": _section_mining_smithing,
    "herblore": _section_herblore,
    "agility_thieving": _section_agility_thieving,
    "slayer": _section_slayer,
    "farming": _section_farming,
    "runecraft": _section_runecraft,
    "hunter": _section_hunter,
    "construction": _section_construction,
    "misc": _section_misc,
    "quests": _section_quests,
    "sailing": _section_sailing,
    "cosmetics": _section_cosmetics,
    "teleports": _section_teleports,
}


# ── Section audit: per-item section overrides ──────────────────────────────
#
# One-off corrections found by the per-item section audit (every item in
# sections <25 checked against its examine text). Family-wide fixes live in
# the _section_* routing functions; this table pins the single items whose
# names/slots defeat keyword routing. Keyed by tab → item id → section name
# (must be a member of TAB_SECTIONS[tab]). Consulted FIRST by
# assign_section(), so entries here beat every keyword branch.
SECTION_ID_OVERRIDES: dict[str, dict[int, str]] = {
    "cooking": {
        # Sailing / Molch / Camdozaal / misc fish that fell into meat rows.
        32341: "Raw fish", 32309: "Raw fish", 32317: "Raw fish",
        32333: "Raw fish", 32349: "Raw fish", 32325: "Raw fish",
        31561: "Raw fish", 31553: "Raw fish",
        20855: "Raw fish", 20857: "Raw fish", 20859: "Raw fish",
        20861: "Raw fish", 20863: "Raw fish", 20865: "Raw fish",
        20867: "Raw fish",
        25652: "Raw fish", 25658: "Raw fish", 25664: "Raw fish",
        25670: "Raw fish",
        23872: "Raw fish", 10138: "Raw fish", 7529: "Raw fish",
        29217: "Cooked fish", 3381: "Cooked fish",
        # (Cooked karambwan deliberately stays in Combo food — Brief #66.)
        32336: "Cooked fish",  # Halibut — caught by a combo-food keyword
        # Raw/cooked/burnt stage swaps ("succulently slimy slice" = cooked;
        # "slimy corpse" = raw).
        3369: "Cooked meat", 3371: "Cooked meat", 3373: "Cooked meat",
        3363: "Raw meat", 3365: "Raw meat", 3367: "Raw meat",
        7521: "Cooked meat",
        31689: "Cooked meat", 31695: "Cooked meat", 31703: "Cooked meat",
        6293: "Raw meat", 6295: "Raw meat",
        6303: "Burnt meat",
        2343: "Cooked meat",   # Cooked oomlie wrap — meat, not fish
        2341: "Raw meat",      # Wrapped oomlie — "It just needs to be cooked"
        7579: "Cooked meat",   # Stuffed snake — cooked stage
        9984: "Raw meat", 9992: "Raw meat", 7224: "Raw meat",
        7230: "Raw meat",      # skewered raw meats — "ready to be used on a fire"
        # Uncooked pies join the part-pies in Baked & cooked goods.
        7196: "Baked & cooked goods", 7186: "Baked & cooked goods",
        7176: "Baked & cooked goods", 7168: "Baked & cooked goods",
        7216: "Baked & cooked goods", 7206: "Baked & cooked goods",
        # Gnome-cookery line intermediates and raws → Combo food.
        2250: "Combo food", 2202: "Combo food", 2178: "Combo food",
        2177: "Combo food", 9558: "Combo food", 9559: "Combo food",
        9561: "Combo food", 9563: "Combo food", 2179: "Combo food",
        2189: "Combo food", 2193: "Combo food", 9560: "Combo food",
        9562: "Combo food", 9564: "Combo food",
        2237: "Combo food",    # Premade w'm crun'
        31811: "Drinks & brews",  # Whirlpool surprise — a rum cocktail
        1940: "Ingredients",   # Raw swamp paste
        7086: "Ingredients",   # Chopped tuna — a topping bowl
        5988: "Ingredients",   # Cooked sweetcorn — vegetable topping
        7060: "Ingredients",   # Tuna potato — rows with the topped-potato chain
        7162: "Cooking tools & utensils",  # Pie recipe book
        10965: "Cooked meat",  # Fingers — "Wall beast fingers in a white fern sauce"
        10969: "Cooked meat",  # Fillets — "Chunky cave-crawler fillets"
        # Burnt fish/meat that fell into the composite Burnt food row.
        32347: "Burnt fish", 32315: "Burnt fish", 32323: "Burnt fish",
        32339: "Burnt fish", 31567: "Burnt fish", 32355: "Burnt fish",
        31559: "Burnt fish", 32331: "Burnt fish",
        25656: "Burnt fish", 25662: "Burnt fish", 25668: "Burnt fish",
        25674: "Burnt fish",
        7531: "Burnt fish",
        7520: "Burnt meat", 2880: "Burnt meat", 7580: "Burnt meat",
    },
    "woodcutting_firemaking": {
        # Axeman's folly is a WC-boost ale, not an axe; no drinks row
        # exists so the catch-all is the honest home.
        5751: "Misc utility", 5825: "Misc utility",
        5753: "Misc utility", 5905: "Misc utility",
    },
    "fishing": {
        31255: "Bait & consumables",   # Barnacle blaster — a cocktail, not a tool
        25582: "Fishing tools",        # Fish barrel — container, rows with tackle box
        25569: "Fishing minigame items",  # Spirit anglers research notes — Tempoross lore
        31408: "Trophies & big catches", 31412: "Trophies & big catches",
        31416: "Trophies & big catches", 31420: "Trophies & big catches",
        31424: "Trophies & big catches", 31428: "Trophies & big catches",
        31820: "Bait & consumables",   # Trawler's trust — a drink
    },
    "crafting": {
        9780: "Crafting outfit & utility",  # Crafting cape
        9782: "Crafting outfit & utility",  # Crafting hood
        6038: "Jewellery materials",   # Magic string — "use this to make jewellery"
        21270: "Gems",                 # Eternal gem — crafted into the eternal ring
        29299: "Crafting tools", 29301: "Crafting tools",
        29303: "Crafting tools",       # fur pouches — storage like the gem pouches
        2370: "Hides & leather",       # Steel studs — leather-armour material
        3678: "Crafting tools",        # Flamtaer hammer — temple-building tool
    },
    "herblore": {
        29531: "Finished potions",     # Super truth serum — completed serum
        19662: "Herblore outfit & utility",  # Botanical pie — herblore-boost food
    },
    "agility_thieving": {
        20659: "Agility marks & rewards",  # Giant squirrel — the Agility pet
    },
    "farming": {
        # Farmer's outfit + skillcape are wearables, not tools.
        13646: "Farmer outfit & contracts", 13643: "Farmer outfit & contracts",
        13642: "Farmer outfit & contracts", 13640: "Farmer outfit & contracts",
        13644: "Farmer outfit & contracts",
        9810: "Farmer outfit & contracts", 9812: "Farmer outfit & contracts",
        # "plant in a hops patch" — fibre crops grow in hops patches.
        31545: "Hops seeds", 31541: "Hops seeds", 31543: "Hops seeds",
        30037: "Farming tools",        # Grape barrel — collection container
        33135: "Farming tools",        # Silklined herb sack — harvest storage
    },
    "runecraft": {
        30843: "Combination runes",    # Aether rune — "A combined cosmic and soul rune"
        25696: "Essence", 25698: "Essence", 25700: "Essence",  # cores = essence substitutes
        26822: "Guardians of the Rift items",  # Abyssal lantern — GotR reward
        26908: "Guardians of the Rift items",  # Intricate pouch — GotR reward container
        26850: "Runecraft gear & utility", 26852: "Runecraft gear & utility",
        26854: "Runecraft gear & utility", 26856: "Runecraft gear & utility",
    },
    "hunter": {
        29119: "Hunter meats", 29122: "Hunter meats", 29125: "Hunter meats",
        29146: "Hunter meats", 29149: "Hunter meats", 29152: "Hunter meats",
        10095: "Furs & hides", 10099: "Furs & hides", 10103: "Furs & hides",
        10093: "Furs & hides", 10097: "Furs & hides", 10101: "Furs & hides",
        29226: "Hunter tertiaries", 29229: "Hunter tertiaries",
        29232: "Hunter tertiaries",    # Hunters' Rumours rare creature parts
        29280: "Hunter outfit", 29283: "Hunter outfit",
        29286: "Hunter outfit", 29289: "Hunter outfit",  # Mixed hide armour set
        29277: "Baits & potions",      # Trapper's tipple — Hunter-boost ale
        10025: "Hunter tools & traps", # Magic box — deployable imp trap
        33625: "Baits & potions",      # Letvek — stymphike bait
        31261: "Baits & potions",      # Sailor's mirage — Hunter-boost drink
    },
    "construction": {
        24977: "Garden & bagged plants",   # Magical pumpkin — plantable
        31027: "Garden & bagged plants",   # Greenman statue — garden hotspot
        31024: "Mounted heads & decor",    # Greenman carving — mountable
    },
    "misc": {
        22991: "Books & documents",    # Stone tablet — a readable record
        24585: "Utility containers", 24587: "Utility containers",  # bag/pouch notes
        22422: "Books & documents",    # Elixir of everlasting — "An ancient scroll"
        30035: "Books & documents",    # Moonrise wines — a book on wines
        9468: "Uncategorized",         # Sawdust — byproduct, not a tool
        2751: "Clue scroll items", 3624: "Clue scroll items",
        3650: "Clue scroll items",     # Sliding pieces — puzzle-box parts
        10561: "Minigame rewards", 10563: "Minigame rewards",  # BA spikes / egg hopper
        22305: "Minigame rewards",     # Ancient relic — top Revenant emblem
        24053: "Books & documents", 24073: "Books & documents",
        13535: "Books & documents",    # On leprechauns / Spurned demon / Tristessa
        30002: "Utility containers",   # Chugging barrel (disassembled)
        30359: "Utility containers",   # Leprechaun's vault
        31212: "Lamps & XP rewards", 31214: "Lamps & XP rewards",
        31216: "Lamps & XP rewards",   # Combatant/Craftsman/Gatherer XP scrolls
    },
    "quests": {
        3695: "Quest items",           # Pet rock — Fremennik quest keepsake
        29912: "Quest items",          # Butler's tray — wieldable prop
        32807: "Quest supplies & materials", 32808: "Quest supplies & materials",
        8871: "Quest items",           # Crate with zanik
        30970: "Quest items",          # Egg
        5608: "Quest items",           # Fox
        3689: "Quest items",           # Lyre
        4086: "Quest items",           # Trollweiss — rare flowers
        4023: "Quest supplies & materials",  # Monkey talisman — greegree input
        27010: "Quest supplies & materials", # Preform — untempered metal
        4591: "Armour & clothing",     # Kharidian headpiece ("pie" substring trap)
        24030: "Artefacts & relics", 24031: "Artefacts & relics",
        24032: "Artefacts & relics", 24033: "Artefacts & relics",  # Memoriam crystals
        30950: "Quest items",          # Canvas piece
        1486: "Quest items",           # Piece of railing
        10885: "Quest supplies & materials", 10898: "Quest supplies & materials",  # gunpowder kegs
        4082: "Quest supplies & materials",  # Salve shard — unstrung crystal
        29928: "Quest supplies & materials", # Wine labels — stationery
        280: "Remains & trophies", 281: "Remains & trophies",
        282: "Remains & trophies", 283: "Remains & trophies",  # Sheep bones
        6946: "Remains & trophies",    # Beer soaked hand — a severed hand
        3707: "Quest consumables",     # Legendary cocktail — a drink
        4075: "Quest supplies & materials",  # Glowing fungus
        33126: "Books & lore",         # The groats principles
        28807: "Artefacts & relics",   # Shield of arrav — the canonical relic
        11279: "Remains & trophies",   # Elvarg's head — a trophy
    },
    "sailing": {
        31255: "Boat cocktails & brews",   # Barnacle blaster
        31261: "Boat cocktails & brews",   # Sailor's mirage
        32866: "Salvage", 32868: "Salvage",  # long-broken compass / sextant relics
        32405: "Keys, charts & schematics",  # Rosewood cargo hold schematic
        32357: "Sea creature parts", 32362: "Sea creature parts",
        32360: "Sea creature parts",   # haddock eye / marlin scales / yellow fin
        31879: "Boat cocktails & brews",   # Rare black lobster — from the ale bottle
        31395: "Island resources", 31393: "Island resources",  # shell / scute
    },
    "cosmetics": {
        23288: "Treasure trail sets",      # Frog slippers — beginner clue reward
        24863: "Miscellaneous cosmetics",  # Zombie monkey — Monkey Backpacks item
        6655: "Random event sets", 6658: "Random event sets",  # Camo bottoms
        # Shattered Relics (or) ornament gear — Leagues, not Treasure Trails.
        26494: "Leagues & speedrun rewards", 26520: "Leagues & speedrun rewards",
        26522: "Leagues & speedrun rewards", 26524: "Leagues & speedrun rewards",
        26526: "Leagues & speedrun rewards", 26537: "Leagues & speedrun rewards",
        26539: "Leagues & speedrun rewards",
        31202: "Miscellaneous cosmetics",  # Swords and emblem (g) — sits with its siblings
        30613: "Miscellaneous cosmetics",  # Gilded staff of collection — Collection Log reward
    },
}


def assign_section(item: dict, tab_name: str) -> str:
    """Return the section name for this item in this tab. Falls back to the
    tab's last section if the rules don't classify it."""
    sections = TAB_SECTIONS[tab_name]
    override = SECTION_ID_OVERRIDES.get(tab_name, {}).get(item.get("id"))
    if override is not None and override in sections:
        return override
    fn = _TAB_TO_FN.get(tab_name)
    if fn is None:
        return sections[-1]
    try:
        section = fn(item)
    except Exception:
        section = sections[-1]
    if section not in sections:
        section = sections[-1]
    return section


# ── Composite sort key generation ───────────────────────────────────────────

_POTION_FAMILY_ORDER = [
    "attack potion", "antipoison", "strength potion", "defence potion",
    "combat potion", "energy potion", "stamina potion", "agility potion",
    "fishing potion", "ranging potion", "magic potion", "magic essence",
    "prayer potion", "summoning potion", "super attack", "superantipoison",
    "super strength", "super defence", "super combat",
    "super ranging", "super magic", "super restore",
    "saradomin brew", "sanfew serum", "zamorak brew", "guthix rest",
    "antifire potion", "extended antifire", "super antifire", "extended super antifire",
    "antidote+", "antidote++", "anti-venom", "anti-venom+",
    "weapon poison",
    "bastion potion", "battlemage potion", "divine ",
]


def _potion_family_rank(name_lower: str) -> int:
    for i, fam in enumerate(_POTION_FAMILY_ORDER):
        if fam in name_lower:
            return i
    return 999


def _potion_dose(name_lower: str) -> int:
    for d in ("(4)", "(3)", "(2)", "(1)"):
        if name_lower.endswith(d):
            return int(d[1])
    return 0


_RUNE_INDEX = {name.lower(): i for i, name in enumerate(RUNE_ORDER)}


def _rune_rank(name_lower: str) -> int:
    for r, idx in _RUNE_INDEX.items():
        if name_lower.startswith(r + " rune"):
            return idx
    # Combination runes get higher rank (after core).
    return 50


_COMBAT_TABS = {"melee", "range", "mage"}
_SKILL_PRODUCTION_TABS = {"cooking", "woodcutting_firemaking", "fletching",
                          "fishing",
                          "crafting", "mining_smithing", "herblore", "runecraft",
                          "farming", "hunter", "construction"}


def composite_sort_key(
    item: dict, tab_name: str, section: str,
) -> tuple:
    """Return a sortable tuple positioning the item within its section.
    The caller has already bucketed by section_rank, so this key only
    needs to handle within-section ordering.

    Brief #62: the primary/cross/override origin head was dropped along with
    the flat tabs schema. Items now sort purely by their item attributes
    (tier, weapon class, name).
    """
    name = _name(item)
    nlow = name.lower()

    # Tab-specific within-section sort.
    if tab_name in _COMBAT_TABS:
        if tab_name == "mage" and section == "Runes":
            return (_rune_rank(nlow), nlow, item.get("id", 0))
        if section in ("Weapons", "Shields & defenders", "Shields & off-hands",
                       "Off-hands, books & tomes",
                       "Head", "Body", "Legs", "Hands", "Feet", "Capes",
                       "Neck", "Rings"):
            # tier-first because osrsbox req data is missing for many
            # ornament-kitted / minigame / quest variants. tier comes from the
            # name token ("Bronze sword" → tier 10) so it's reliable; req is
            # used as a tiebreaker for same-tier items.
            tier = _name_tier(name)
            req = _req_max_level(item)
            return (tier, req, nlow, item.get("id", 0))
        if section in ("Ammunition", "Ammo slot"):
            return (_name_tier(name), nlow, item.get("id", 0))
        return (nlow, item.get("id", 0))

    if tab_name == "herblore" and section in (
        "Finished potions", "Barbarian mixes",
        "Divine, extended & upgraded variants", "Unfinished potions",
    ):
        return (_potion_family_rank(nlow), -_potion_dose(nlow),
                nlow, item.get("id", 0))

    if tab_name in _SKILL_PRODUCTION_TABS:
        # Same tier-first reasoning as combat tabs.
        tier = _name_tier(name)
        req = _req_max_level(item)
        return (tier, req, nlow, item.get("id", 0))

    if tab_name == "slayer":
        # Packing-priority via section_rank (caller); within section alpha.
        return (nlow, item.get("id", 0))

    # Default: alpha.
    return (nlow, item.get("id", 0))
