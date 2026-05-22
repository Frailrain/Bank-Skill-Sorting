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
    "melee": [
        "Weapons", "Shields & defenders", "Head", "Body", "Legs",
        "Hands", "Feet", "Capes", "Neck", "Rings", "Ammunition",
        "Training & utility",
    ],
    "range": [
        "Weapons", "Ammunition", "Head", "Body", "Legs",
        "Hands", "Feet", "Capes", "Shields & off-hands", "Neck",
        "Rings", "Training & utility",
    ],
    "mage": [
        "Runes", "Weapons", "Off-hands, books & tomes", "Head", "Body",
        "Legs", "Hands", "Feet", "Capes", "Neck", "Rings",
        "Teleport tablets & spell utility", "Enchanting & skilling magic",
    ],
    "prayer": [
        # Brief #60: equipment first so the prayer tab leads with gear rather
        # than bones-pile chaff.
        "Prayer equipment & robes", "Bones & ashes", "Ensouled heads",
        "Prayer-restoring consumables", "Holy symbols, books & blessings",
        "Bone-processing utility",
    ],
    "cooking": [
        "Cooking tools & utensils", "Raw cookables", "Ingredients",
        "Cooked food", "Special & combo food", "Burnt food",
    ],
    # Brief #63: wc_fletching split. Fletching is now its own tab; the
    # woodcutting bits live alongside firemaking content in a combined tab.
    # Both tabs continue to overlap with the existing `firemaking` tab
    # (items can live in multiple tabs).
    "woodcutting_firemaking": [
        "Axes",
        "Logs",
        "Pyre logs",
        "Tinderboxes & firelighting tools",
        "Shade items",
        "Wintertodt & minigame items",
        "Woodcutting outfit",
        "Firemaking outfit",
        "Forestry items",
        "Misc utility",
    ],
    "fletching": [
        "Fletching tools",
        "Bow materials",
        "Unstrung bows",
        "Strung bows",
        "Crossbow stocks",
        "Crossbows",
        "Arrow & dart components",
        "Finished arrows",
        "Finished darts",
        "Finished bolts",
        "Finished javelins",
        "Fletching outfit & rewards",
    ],
    "fishing": [
        "Fishing tools", "Bait & consumables", "Fishing outfit",
        "Raw fish", "Fishing minigame items",
    ],
    # Brief #64: standalone firemaking tab removed. All firemaking content
    # lives in "woodcutting_firemaking" (Woodcutting + Firemaking) which
    # was created in Brief #63.
    "crafting": [
        "Crafting tools & moulds", "Gems", "Hides & leather",
        "Spinning materials", "Glassmaking", "Pottery & clay",
        "Jewellery materials", "Crafted jewellery",
        "Crafted armour & leather goods", "Crafting outfit & utility",
    ],
    "mining_smithing": [
        "Pickaxes", "Mining outfit & utility", "Ores", "Bars",
        "Smithing tools", "Smithed weapons", "Smithed armour",
        "Cannonballs & ammo outputs", "Giants' Foundry & minigame items",
    ],
    "herblore": [
        "Vials & herblore tools", "Herbs", "Secondaries",
        "Unfinished potions", "Finished potions", "Barbarian mixes",
        "Divine, extended & upgraded variants",
        "Herblore outfit & utility",
    ],
    "agility_thieving": [
        "Agility outfit & graceful", "Run-energy consumables",
        "Agility marks & tickets", "Thieving outfit & rogue set",
        "Thieving tools", "Thieving loot & artefacts",
    ],
    "slayer": [
        "Slayer assignment items", "Mandatory protection",
        "Core slayer gear", "Cannon & burst supplies",
        "Combat potions", "Prayer & restores", "Food",
        "Teleports", "Loot management", "Misc utility",
    ],
    "farming": [
        "Farming tools", "Compost & soil treatment",
        "Allotment seeds", "Hops seeds", "Flower seeds",
        "Herb seeds", "Bush seeds", "Tree seeds",
        "Fruit tree seeds", "Special seeds", "Saplings",
        "Farmer outfit & contracts",
    ],
    "runecraft": [
        "Essence", "Pouches & storage", "Talismans", "Tiaras",
        "Core runes", "Combination runes", "Runecraft outfit",
        "Guardians of the Rift items",
    ],
    "hunter": [
        "Hunter tools & traps", "Nets, jars & containers",
        "Hunter outfit", "Creature products", "Chinchompas",
        "Implings & impling jars", "Birdhouse items",
    ],
    "construction": [
        "Construction tools", "Planks", "Nails",
        "Building materials", "Teleport-to-house items",
        "Mahogany Homes & contract items",
        "Construction outfit & rewards",
    ],
    "misc": [
        "Currency & exchange tokens", "General teleports",
        "Jewellery teleports", "Utility containers",
        "Clue scroll items", "Minigame currencies & tickets",
        "General tools", "Uncategorized",
    ],
    "quests": [
        "Quest items", "Quest reward equipment",
        "Reclaimable quest utility",
    ],
    "sailing": [
        "Sailing tools & navigation", "Ship components",
        "Shipbuilding materials", "Cargo & contracts",
        "Sailing outfit & rewards",
    ],
    "cosmetics": [
        "Cosmetic sets", "Treasure trail cosmetics",
        "Holiday items", "Ornament kits",
        "Skill & event cosmetics", "Decorative weapons & armour",
    ],
    # Brief #62: Teleports tab. Starts empty; audit pass populates it.
    "teleports": [
        "Mounted & charged jewellery",
        "Spellbook tablets",
        "Skill destinations",
        "City teleports",
        "Boss & PvM destinations",
        "Minigame teleports",
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

# Cooked mainstream-PvM food (highest heal / common usage first).
COOKED_FOOD_NAMES = [
    "Anglerfish", "Cooked karambwan", "Dark crab", "Shark",
    "Manta ray", "Sea turtle", "Monkfish", "Tuna potato",
    "Swordfish", "Lobster", "Bass",
    "Salmon", "Tuna", "Trout", "Pike", "Cod", "Mackerel",
    "Herring", "Sardine", "Anchovies", "Shrimps",
    # Other cooked items
    "Cooked chicken", "Cooked meat", "Stew", "Curry",
    "Cooked rabbit", "Roast bird meat", "Roast beast meat",
    "Roast rabbit", "Roast chompy", "Roast jubbly", "Roast oomlie",
    "Roast frog legs", "Cooked sweetcorn", "Cooked oomlie wrap",
    "Spider on stick", "Spider on shaft",
    "Baked potato", "Egg potato", "Mushroom potato", "Potato with butter",
    "Potato with cheese", "Chilli potato",
    "Cave eel", "Cooked eel", "Slimy eel", "Rocktail",
]

# Pies / pizzas / cakes — "Special & combo" food (cooked but multi-step).
SPECIAL_FOOD_NAMES = [
    "Plain pizza", "Anchovy pizza", "Meat pizza", "Pineapple pizza",
    "Apple pie", "Redberry pie", "Meat pie", "Mud pie", "Garden pie",
    "Fish pie", "Admiral pie", "Wild pie", "Summer pie",
    "Steak and kidney pie", "Botanical pie", "Mushroom & onion pie",
    "Cake", "Chocolate cake", "Sliced cake",
    "Cheese+tom batta", "Toad batta", "Worm batta", "Vegetable batta",
    "Fruit batta", "Toad crunchies", "Spicy crunchies", "Worm crunchies",
    "Chocchip crunchies", "Toad gnomebowl", "Worm gnomebowl",
    "Salmon gnomebowl", "Tangled toads' legs gnomebowl",
    "Veg ball", "Worm hole", "Choc bomb",
    "Premade dirty blast", "Premade fr'y blurberry", "Premade fr't blast",
    "Premade veg ball", "Premade choc bomb",
    "Kebab", "Ugthanki kebab", "Super kebab",
    "Fishcake", "Cooked fishcake",
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
]
_CONSTRUCTION_EXCLUDE_TOKENS = [
    "ship paint", "sailing paint", "ship plank", "ship repair kit",
    "rope coil", "ship rope", "ship sail", "hull plate",
    "rudder", "mast", "anchor",
]


# ── Build-time ID set construction ─────────────────────────────────────────

_ID_SETS: dict[str, set[int]] = {}


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
    build("cooked_food", COOKED_FOOD_NAMES)
    build("special_food", SPECIAL_FOOD_NAMES)
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

    # Token-based exclude for construction (sailing pollution).
    construction_exclude = _ids_for_token_match(_CONSTRUCTION_EXCLUDE_TOKENS, items_by_id)
    _ID_SETS["construction_exclude"] = construction_exclude
    if verbose:
        print(f"  id_set[construction_exclude]: {len(construction_exclude):>4} items "
              f"(token match: {', '.join(_CONSTRUCTION_EXCLUDE_TOKENS[:3])}...)",
              file=_sys.stderr)


def _is(item: dict, set_name: str) -> bool:
    """ID-set membership check. False if init_id_sets hasn't been called."""
    iid = item.get("id")
    s = _ID_SETS.get(set_name)
    return s is not None and iid in s


# ── Tab exclude IDs (post-section-assignment hard filter) ──────────────────


def tab_exclude_ids(tab: str) -> set[int]:
    """Items hard-excluded from a tab regardless of LLM placement. Applied by
    llm_promote.build_synthetic_tabs as a post-filter."""
    if tab == "prayer":
        return _ID_SETS.get("prayer_exclude", set())
    if tab == "construction":
        return _ID_SETS.get("construction_exclude", set())
    return set()

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


def _section_combat(item: dict, tab: str) -> str:
    """Slot-based section assignment shared across melee/range/mage."""
    slot = _slot(item)
    name = _name(item)
    nlow = name.lower()

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
            return "Teleport tablets & spell utility"
        # Books / tomes / off-hand shields used for spellcasting.
        if slot == "shield" and any(k in nlow for k in (
                "book", "tome", "magic book", "mage's book", "merlin's crystal")):
            return "Off-hands, books & tomes"

    # Range-specific routing first.
    if tab == "range":
        if slot == "ammo":
            return "Ammunition"
        # Thrown weapons (darts, knives, javelins). These ARE weapons not ammo
        # in the slot system but mentally belong with ranged ammo.
        wt = _weapon_type(item)
        if wt in ("thrown", "throwing"):
            return "Ammunition"

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
        return "Ammunition"

    # Mage fallthrough — runic / enchanting items, training tools.
    if tab == "mage":
        if any(k in nlow for k in ("bones", "soul", "enchant", "wand")):
            return "Enchanting & skilling magic"
        return "Teleport tablets & spell utility"

    return "Training & utility"


def _section_prayer(item: dict) -> str:
    name = _name(item)
    nlow = name.lower()
    slot = _slot(item)
    # Equipment FIRST (Brief #60 reorder: prayer tab leads with gear).
    if slot in ("body", "legs", "head", "feet", "hands", "cape", "neck", "ring",
                "shield"):
        # But blessed-only / book-only shields belong with the holy items.
        if slot == "shield" and any(k in nlow for k in (
                "holy book", "book of", "unholy book", "blessed")):
            return "Holy symbols, books & blessings"
        return "Prayer equipment & robes"
    if nlow.endswith(" bones") or nlow == "bones" or "bones (" in nlow:
        return "Bones & ashes"
    if nlow.endswith(" ashes") or nlow == "ashes":
        return "Bones & ashes"
    if nlow.startswith("ensouled "):
        return "Ensouled heads"
    if any(k in nlow for k in (
        "prayer potion", "super restore", "sanfew serum", "prayer mix",
        "prayer renewal",
    )):
        return "Prayer-restoring consumables"
    if any(k in nlow for k in (
        "bonecrusher", "ash sanctifier", "dragonbone necklace",
    )):
        return "Bone-processing utility"
    if any(k in nlow for k in ("holy symbol", "unholy symbol", "holy book",
                               "blessed", "page", "blessing")):
        return "Holy symbols, books & blessings"
    return "Bone-processing utility"


def _section_cooking(item: dict) -> str:
    name = _name(item)
    nlow = name.lower()
    # Burnt detection works fine via name prefix; nothing else uses it.
    if nlow.startswith("burnt "):
        return "Burnt food"
    # ID-set lookups (Brief #60): authoritative for everything we curated.
    if _is(item, "cooked_food"):
        return "Cooked food"
    if _is(item, "special_food"):
        return "Special & combo food"
    if _is(item, "raw_cookables"):
        return "Raw cookables"
    if _is(item, "ingredients"):
        return "Ingredients"
    # Cooking tools — still name-driven because the set is small and the
    # tokens are distinctive.
    if any(k in nlow for k in ("knife", "rolling pin", "pestle and mortar",
                               "mixing bowl", "skewer", "cooking gauntlets",
                               "cook's outfit", "cook's hat", "cook's apron",
                               "cooks' assistant")):
        return "Cooking tools & utensils"
    # Name-pattern fallback for variants the canonical lists don't carry.
    if nlow.startswith("raw "):
        return "Raw cookables"
    if any(k in nlow for k in ("pie", "pizza", "stew", "cake",
                               "kebab", "curry", "gnomebowl", "gnome batta",
                               "gnome crunchies", "fishcake")):
        return "Special & combo food"
    # Default to Cooked food (most cross-tagged-into-cooking items are food).
    return "Cooked food"


def _section_woodcutting_firemaking(item: dict) -> str:
    """Brief #63: combined Woodcutting + Firemaking section assignment.
    The fletching-specific items that used to share this tab now live in
    the `fletching` tab and are routed by _section_fletching()."""
    name = _name(item)
    nlow = name.lower()
    slot = _slot(item)
    if "axe" in nlow and "pickaxe" not in nlow and slot in ("weapon", "2h", ""):
        return "Axes"
    if _is(item, "pyre_logs") or "pyre logs" in nlow:
        return "Pyre logs"
    if _is(item, "logs") or nlow.endswith(" logs") or nlow.endswith(" log"):
        return "Logs"
    if any(k in nlow for k in ("tinderbox", "firelighter", "bruma torch")):
        return "Tinderboxes & firelighting tools"
    if any(k in nlow for k in ("shade key", "fiyr remains", "loar remains",
                                "phrin remains", "riyl remains", "asyn remains",
                                "shade remains")):
        return "Shade items"
    if any(k in nlow for k in ("wintertodt", "bruma kindling",
                                "rejuvenation potion", "burnt page", "supply crate")):
        return "Wintertodt & minigame items"
    if any(k in nlow for k in ("pyromancer", "firemaking cape", "firemaking hood")):
        return "Firemaking outfit"
    if any(k in nlow for k in ("lumberjack", "forester ", "woodcutting cape",
                                "woodcutting hood")):
        return "Woodcutting outfit"
    if any(k in nlow for k in ("forestry", "secateurs", "anti-poaching",
                                "felling axe", "fox whistle", "log basket",
                                "pheasant", "bird egg")):
        return "Forestry items"
    return "Misc utility"


def _section_fletching(item: dict) -> str:
    """Brief #63: standalone Fletching tab section assignment. Pulled out
    of the old wc_fletching classifier; only fletching-specific routing
    lives here."""
    nlow = _name(item).lower()
    slot = _slot(item)
    # Tools first — knife/chisel may be shared with crafting/cooking but
    # land here when they're in the fletching tab.
    if any(k in nlow for k in ("chisel", "knife", "fletching kit")) and slot != "weapon":
        return "Fletching tools"
    # Materials (strings, feathers, dart/bolt tips, headless arrows, shafts).
    if "bow string" in nlow or "magic string" in nlow:
        return "Bow materials"
    if "unstrung" in nlow:
        return "Unstrung bows"
    if "crossbow stock" in nlow or nlow.endswith(" stock"):
        return "Crossbow stocks"
    if "crossbow" in nlow and not any(k in nlow for k in ("string", "stock", "limb")):
        return "Crossbows"
    if any(k in nlow for k in ("arrow shaft", "headless arrow", "dart tip",
                                "bolt tip", "feather", "javelin shaft",
                                "unfinished bolt", "unfinished broad")):
        return "Arrow & dart components"
    if nlow.endswith(" arrows") or nlow.endswith(" arrow"):
        return "Finished arrows"
    if nlow.endswith(" darts") or nlow.endswith(" dart"):
        return "Finished darts"
    if nlow.endswith(" bolts") or nlow.endswith(" bolt") \
            or nlow.endswith(" bolts (p)") or nlow.endswith(" bolts (p+)") \
            or nlow.endswith(" bolts (p++)"):
        return "Finished bolts"
    if nlow.endswith(" javelins") or nlow.endswith(" javelin") \
            or nlow.endswith(" javelin heads"):
        return "Finished javelins"
    if any(nlow.endswith(s) for s in (" bow", " shortbow", " longbow",
                                       " comp bow", " composite bow")):
        return "Strung bows"
    if any(k in nlow for k in ("fletching cape", "fletching hood",
                                "fletching outfit")):
        return "Fletching outfit & rewards"
    # Default — most uncategorised fletching items are component materials.
    return "Arrow & dart components"


def _section_fishing(item: dict) -> str:
    nlow = _name(item).lower()
    # Brief #60: ID-set lookups first.
    if _is(item, "raw_fish"):
        return "Raw fish"
    if _is(item, "fishing_tools"):
        return "Fishing tools"
    if _is(item, "fishing_bait"):
        return "Bait & consumables"
    if any(k in nlow for k in ("angler", "spirit angler", "fish barrel",
                               "fishing cape", "fishing hood")):
        return "Fishing outfit"
    if any(k in nlow for k in ("tempoross", "fishing rod-o-matic", "spirit flake",
                               "soaked page")):
        return "Fishing minigame items"
    # Name-pattern fallback ONLY for raw items not in the canonical fish list.
    # We deliberately do NOT use startswith("raw ") as a catch-all anymore —
    # raw fox furs etc. were being mis-classified as fish.
    return "Fishing minigame items"


# Brief #64 removed _section_firemaking — its logic moved into
# _section_woodcutting_firemaking() in Brief #63.


def _section_crafting(item: dict) -> str:
    name = _name(item)
    nlow = name.lower()
    if any(k in nlow for k in ("chisel", "needle", "shears")) and _slot(item) != "weapon":
        return "Crafting tools & moulds"
    if " mould" in nlow or nlow.endswith(" mould"):
        return "Crafting tools & moulds"
    for gem in GEM_TIER:
        if gem in nlow:
            return "Gems"
    if "dragonhide" in nlow and not any(k in nlow for k in (" body", " chaps", " vamb")):
        return "Hides & leather"
    if "leather" in nlow and _slot(item) == "":
        return "Hides & leather"
    if "wool" in nlow or "ball of wool" in nlow or "thread" in nlow:
        return "Spinning materials"
    if "molten glass" in nlow or "soda ash" in nlow or "seaweed" in nlow:
        return "Glassmaking"
    if "soft clay" in nlow or "pot lid" in nlow or "pottery" in nlow or "unfired" in nlow:
        return "Pottery & clay"
    if any(k in nlow for k in ("gold bar", "silver bar", "amulet of", "ring of",
                               "necklace of", "bracelet of")) and _slot(item) == "":
        return "Jewellery materials"
    if _slot(item) in ("neck", "amulet", "ring"):
        return "Crafted jewellery"
    if _slot(item) in ("body", "legs", "head", "feet", "hands", "cape", "shield"):
        return "Crafted armour & leather goods"
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
                               "varrock armour", "celestial ring")):
        return "Mining outfit & utility"
    if "cannonball" in nlow:
        return "Cannonballs & ammo outputs"
    if nlow.endswith(" ore") or nlow.endswith(" rocks"):
        return "Ores"
    if nlow.endswith(" bar"):
        return "Bars"
    if "hammer" in nlow and slot != "weapon":
        return "Smithing tools"
    if any(k in nlow for k in ("foundry", "giant's foundry", "ammo mould",
                               "smiths uniform", "smithing cape", "smithing hood")):
        return "Giants' Foundry & minigame items"
    if slot in ("weapon", "2h"):
        return "Smithed weapons"
    if slot in ("body", "legs", "head", "feet", "hands", "shield"):
        return "Smithed armour"
    return "Giants' Foundry & minigame items"


def _section_herblore(item: dict) -> str:
    name = _name(item)
    nlow = name.lower()
    if any(k in nlow for k in ("vial", "pestle and mortar", "herblore cape",
                               "herblore hood", "botanist")):
        return "Vials & herblore tools"
    # Brief #60: ID-set lookups for herbs (was the Brief #59 bug — clean
    # herbs in OSRS have no "Clean " prefix; startswith("clean ") matched 0).
    if _is(item, "grimy_herbs") or _is(item, "clean_herbs"):
        return "Herbs"
    # Name-pattern fallback for any "Grimy X" variant not in the canonical list.
    if nlow.startswith("grimy "):
        return "Herbs"
    if "(unf)" in nlow or " unf " in nlow or nlow.endswith(" unf"):
        return "Unfinished potions"
    # Dose suffix indicates a finished potion.
    if any(nlow.endswith(s) for s in ("(4)", "(3)", "(2)", "(1)")):
        # Special families come first.
        if "divine " in nlow or "extended " in nlow or "super antifire" in nlow:
            return "Divine, extended & upgraded variants"
        if "mix(" in nlow or "mix (" in nlow or "barbarian" in nlow:
            return "Barbarian mixes"
        return "Finished potions"
    # Curated secondaries set.
    if _is(item, "herblore_secondaries"):
        return "Secondaries"
    # Last-resort name-pattern fallback for the long tail of secondaries we
    # haven't enumerated. Anything that lands here is just "Secondaries" by
    # default since the LLM already routed it to the herblore tab.
    return "Secondaries"


def _section_agility_thieving(item: dict) -> str:
    nlow = _name(item).lower()
    if "graceful" in nlow or "agility cape" in nlow or "agility hood" in nlow:
        return "Agility outfit & graceful"
    if any(k in nlow for k in ("energy potion", "stamina potion", "agility potion",
                               "summer pie", "explorer's ring")):
        return "Run-energy consumables"
    if any(k in nlow for k in ("mark of grace", "agility ticket", "shayzien lap",
                               "wilderness agility ticket")):
        return "Agility marks & tickets"
    if "rogue" in nlow or "thieving cape" in nlow or "thieving hood" in nlow:
        return "Thieving outfit & rogue set"
    if any(k in nlow for k in ("lockpick", "blackjack", "stethoscope",
                               "key impressioning")):
        return "Thieving tools"
    return "Thieving loot & artefacts"


def _section_farming(item: dict) -> str:
    nlow = _name(item).lower()
    if any(k in nlow for k in ("spade", "rake", "watering can", "secateurs",
                               "trowel", "plant pot", "magic secateurs",
                               "farming cape", "farming hood", "farmer",
                               "gricoller", "bottomless compost")):
        return "Farming tools"
    if "compost" in nlow:
        return "Compost & soil treatment"
    if "sapling" in nlow:
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
    return "Farmer outfit & contracts"


def _section_runecraft(item: dict) -> str:
    name = _name(item)
    nlow = name.lower()
    if "essence" in nlow:
        return "Essence"
    if "pouch" in nlow and "rune" not in nlow:
        return "Pouches & storage"
    if "rune pouch" in nlow or "abyssal lantern" in nlow:
        return "Pouches & storage"
    if "talisman" in nlow:
        return "Talismans"
    if "tiara" in nlow:
        return "Tiaras"
    if name in _RUNE_NAMES or nlow.endswith(" rune") or nlow.endswith(" runes"):
        # Distinguish combination from core.
        if any(nlow.startswith(p) for p in ("steam ", "mist ", "smoke ",
                                            "lava ", "mud ", "dust ")):
            return "Combination runes"
        return "Core runes"
    if any(k in nlow for k in ("raiments of the eye", "raiments", "runecraft cape",
                               "runecraft hood", "wicked hood", "binding necklace",
                               "tome of fire", "tome of water")):
        return "Runecraft outfit"
    if any(k in nlow for k in ("guardian fragments", "abyssal pearls",
                               "intricate pouch", "outfit fragment",
                               "soaked fragment", "guardian essence",
                               "fragments")):
        return "Guardians of the Rift items"
    return "Runecraft outfit"


def _section_hunter(item: dict) -> str:
    nlow = _name(item).lower()
    if "trap" in nlow or "snare" in nlow or "noose wand" in nlow:
        return "Hunter tools & traps"
    if "impling" in nlow:
        return "Implings & impling jars"
    if any(k in nlow for k in ("butterfly jar", "butterfly net", "magic butterfly",
                               "magic box", "bird jar")) and "impling" not in nlow:
        return "Nets, jars & containers"
    if "chinchompa" in nlow:
        return "Chinchompas"
    if any(k in nlow for k in ("camo", "larupia", "graahk", "kyatt",
                               "spotted cape", "spottier cape", "hunter cape",
                               "hunter hood", "hunter outfit", "guild hunter")):
        return "Hunter outfit"
    if any(k in nlow for k in ("birdhouse", "bird nest", "raven egg")):
        return "Birdhouse items"
    return "Creature products"


def _section_construction(item: dict) -> str:
    nlow = _name(item).lower()
    if "plank" in nlow:
        return "Planks"
    if "nail" in nlow:
        return "Nails"
    if any(k in nlow for k in ("saw", "hammer", "construct cape", "construction cape",
                               "construction hood", "carpenter")):
        return "Construction tools"
    if any(k in nlow for k in ("clay", "gold leaf", "limestone", "marble block",
                               "silver bar", "cloth", "soft clay", "magic stone")):
        return "Building materials"
    if any(k in nlow for k in ("teleport to house", "house teleport tablet",
                               "house tablet")):
        return "Teleport-to-house items"
    if any(k in nlow for k in ("mahogany homes", "carpentry contract", "amys saw",
                               "plank sack", "mythical max cape")):
        return "Mahogany Homes & contract items"
    return "Construction outfit & rewards"


def _section_slayer(item: dict) -> str:
    name = _name(item)
    nlow = name.lower()
    if any(k in nlow for k in ("enchanted gem", "eternal gem", "slayer ring",
                               "slayer's enchantment", "mysterious emblem",
                               "ancient emblem", "totem", "ancient effigy",
                               "ancient relic")):
        return "Slayer assignment items"
    if any(k in nlow for k in ("nose peg", "earmuffs", "facemask", "mirror shield",
                               "spiny helmet", "witchwood icon", "brittle key",
                               "fungicide", "ice cooler", "bag of salt")):
        return "Mandatory protection"
    if any(k in nlow for k in ("slayer helmet", "broad arrows", "broad bolts",
                               "broad darts", "leaf-bladed", "darklight",
                               "rock hammer", "rock thrownhammer", "slayer staff",
                               "slayer's staff", "slayer gloves", "boots of stone",
                               "boots of brimstone", "granite gloves", "granite ring",
                               "granite hammer", "salve amulet", "black mask",
                               "smouldering stone", "hydra leather", "ferocious gloves")):
        return "Core slayer gear"
    if "cannon" in nlow:
        return "Cannon & burst supplies"
    if "cannonball" in nlow:
        return "Cannon & burst supplies"
    if any(nlow.endswith(s) for s in ("(4)", "(3)", "(2)", "(1)")):
        if any(k in nlow for k in ("prayer", "restore", "sanfew")):
            return "Prayer & restores"
        return "Combat potions"
    if name in _TOP_FOOD or nlow in {"shark", "manta ray", "anglerfish",
                                      "dark crab", "tuna potato", "monkfish",
                                      "cooked karambwan", "sea turtle"}:
        return "Food"
    if any(k in nlow for k in ("teleport", "drakan's medallion", "royal seed pod",
                               "ectophial", "skills necklace", "amulet of glory",
                               "ring of dueling", "xeric's talisman")):
        return "Teleports"
    if any(k in nlow for k in ("rune pouch", "herb sack", "seed box", "gem bag",
                               "gem pouch", "coal bag", "looting bag",
                               "bonecrusher", "ash sanctifier", "ava's", "avas")):
        return "Loot management"
    return "Misc utility"


_TOP_FOOD = {"Shark", "Manta ray", "Anglerfish", "Dark crab", "Tuna potato",
             "Monkfish", "Cooked karambwan", "Sea turtle"}


def _section_misc(item: dict) -> str:
    nlow = _name(item).lower()
    if nlow == "coins" or "platinum token" in nlow or "trading sticks" in nlow:
        return "Currency & exchange tokens"
    if any(k in nlow for k in ("amulet of glory", "ring of dueling",
                               "skills necklace", "games necklace", "combat bracelet",
                               "ring of wealth", "digsite pendant", "necklace of passage",
                               "burning amulet", "slayer ring")):
        return "Jewellery teleports"
    if any(k in nlow for k in ("teleport", "tablet", "scroll of redirection",
                               "ectophial", "enchanted lyre", "ardougne cloak",
                               "kharedst's memoirs", "drakan's medallion",
                               "royal seed pod", "xeric's talisman")):
        return "General teleports"
    if any(k in nlow for k in ("clue scroll", "casket", "reward casket",
                               "puzzle box", "challenge scroll", "key (",
                               "scroll box")):
        return "Clue scroll items"
    if any(k in nlow for k in ("rune pouch", "herb sack", "seed box", "gem bag",
                               "gem pouch", "coal bag", "looting bag",
                               "tool leprechaun", "tackle box", "fish barrel")):
        return "Utility containers"
    if any(k in nlow for k in ("castle wars ticket", "blast furnace token",
                               "mta token", "pizazz", "lms shard")):
        return "Minigame currencies & tickets"
    if any(k in nlow for k in ("chisel", "knife", "hammer", "saw",
                               "fishing", "spade", "rake", "tinderbox",
                               "shears", "bucket")):
        return "General tools"
    return "Uncategorized"


def _section_quests(item: dict) -> str:
    nlow = _name(item).lower()
    if _slot(item) and _slot(item) != "ammo":
        return "Quest reward equipment"
    if any(k in nlow for k in ("reclaim", "diary cape", "diary hood",
                               "ardougne cloak", "explorer's ring", "morytania legs",
                               "varrock armour", "kandarin headgear", "fremennik sea boots",
                               "wilderness sword", "western banner", "kourend headgear",
                               "kharedst's memoirs")):
        return "Reclaimable quest utility"
    return "Quest items"


def _section_sailing(item: dict) -> str:
    nlow = _name(item).lower()
    if any(k in nlow for k in ("compass", "spyglass", "crowbar", "captain's log",
                               "sextant", "telescope")):
        return "Sailing tools & navigation"
    if any(k in nlow for k in ("hull", "mast", "sail", "rudder", "anchor",
                               "ship component")):
        return "Ship components"
    if any(k in nlow for k in ("repair kit", "tar", "pitch", "rope coil",
                               "canvas", "ship plank")):
        return "Shipbuilding materials"
    if any(k in nlow for k in ("cargo", "crate of ", "shipment", "manifest",
                               "courier")):
        return "Cargo & contracts"
    return "Sailing outfit & rewards"


def _section_cosmetics(item: dict) -> str:
    nlow = _name(item).lower()
    if nlow.endswith(" ornament kit") or nlow.endswith(" colour kit") or "upgrade kit" in nlow:
        return "Ornament kits"
    if any(k in nlow for k in (
        "partyhat", "h'ween mask", "halloween mask", "santa", "easter",
        "midsummer", "diwali", "thanksgiving", "christmas", "scythe of vitur (",
        "festive", "valentine", "gingerbread", "trick", "jack lantern",
        "pumpkin", "bunny", "ham", "cracker", "snowman", "spectator",
    )):
        return "Holiday items"
    if any(k in nlow for k in (
        "trailblazer", "shattered relic", "twisted relics", "leagues",
    )):
        return "Skill & event cosmetics"
    if any(k in nlow for k in (
        "3rd age", "third age", "trimmed", "(g)", "(t)", "(h1)", "(h2)",
        "(h3)", "(h4)", "(h5)", "(sk)", "(lg)", "(or)", "(or 1)", "(or 2)",
        "elegant", "musketeer", "dragon mask", "leprechaun hat",
        "robin hood hat", "pith helmet", "deerstalker",
    )):
        return "Treasure trail cosmetics"
    if any(k in nlow for k in ("decorative sword", "decorative", "magic dye",
                               "white dye", "red dye", "blue dye", "yellow dye",
                               "purple dye", "orange dye", "pink dye")):
        return "Decorative weapons & armour"
    if "set" in nlow and "_(" not in nlow:
        return "Cosmetic sets"
    return "Treasure trail cosmetics"


def _section_teleports(item: dict) -> str:
    """Coarse routing for the Teleports tab. Brief #62 added the tab as an
    empty container; the audit pass owns final section assignment per item.
    Until then we make a low-effort guess from the item name so the smoke-
    test seed items land somewhere sensible rather than the fallback."""
    nlow = _name(item).lower()
    if any(k in nlow for k in (
        "amulet of glory", "skills necklace", "ring of dueling",
        "ring of wealth", "combat bracelet", "games necklace",
        "digsite pendant", "necklace of passage", "burning amulet",
        "slayer ring", "drakan's medallion", "ring of the elements",
    )):
        return "Mounted & charged jewellery"
    if "tablet" in nlow or " teleport (tab)" in nlow:
        return "Spellbook tablets"
    if any(k in nlow for k in (
        "ectophial", "royal seed pod", "kharedst's memoirs",
        "enchanted lyre", "pharaoh's sceptre",
    )):
        return "Special & one-time"
    if any(k in nlow for k in ("scroll of redirection",)):
        return "Special & one-time"
    if "wilderness" in nlow:
        return "Wilderness teleports"
    return "Special & one-time"


_TAB_TO_FN = {
    "melee": lambda it: _section_combat(it, "melee"),
    "range": lambda it: _section_combat(it, "range"),
    "mage":  lambda it: _section_combat(it, "mage"),
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


def assign_section(item: dict, tab_name: str) -> str:
    """Return the section name for this item in this tab. Falls back to the
    tab's last section if the rules don't classify it."""
    fn = _TAB_TO_FN.get(tab_name)
    sections = TAB_SECTIONS[tab_name]
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
        if section == "Ammunition":
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
