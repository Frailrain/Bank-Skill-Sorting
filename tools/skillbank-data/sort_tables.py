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
        "Bones & ashes", "Ensouled heads", "Prayer-restoring consumables",
        "Prayer equipment & robes", "Holy symbols, books & blessings",
        "Bone-processing utility",
    ],
    "cooking": [
        "Cooking tools & utensils", "Raw cookables", "Ingredients",
        "Cooked food", "Special & combo food", "Burnt food",
    ],
    "wc_fletching": [
        "Axes", "Logs", "Fletching tools", "Bow & ammo materials",
        "Unstrung bows", "Strung bows", "Finished arrows, darts & bolts",
        "Woodcutting & Fletching outfits",
    ],
    "fishing": [
        "Fishing tools", "Bait & consumables", "Fishing outfit",
        "Raw fish", "Fishing minigame items",
    ],
    "firemaking": [
        "Tinderboxes & firelighting tools", "Logs", "Pyre logs",
        "Shade items", "Wintertodt & minigame items",
        "Firemaking outfit & rewards",
    ],
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
}

# Group-rank: primary items first within each section, then cross-tags, then overrides.
GROUP_RANK = {"primary": 0, "cross": 1, "override": 2}

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
    if any(k in nlow for k in (
        "holy", "unholy", "blessed", "saradomin", "zamorak", "guthix", "armadyl",
        "bandos", "ancient", "book of",
    )) and _slot(item) in ("body", "legs", "head", "feet", "hands", "shield"):
        return "Prayer equipment & robes"
    if any(k in nlow for k in ("holy symbol", "unholy symbol", "holy book",
                               "blessed", "page", "blessing")):
        return "Holy symbols, books & blessings"
    if _slot(item) in ("body", "legs", "head", "feet", "hands", "cape", "neck", "ring"):
        return "Prayer equipment & robes"
    return "Bone-processing utility"


def _section_cooking(item: dict) -> str:
    name = _name(item)
    nlow = name.lower()
    if nlow.startswith("burnt "):
        return "Burnt food"
    if nlow.startswith("raw "):
        return "Raw cookables"
    if nlow.startswith("uncooked "):
        return "Raw cookables"
    if any(k in nlow for k in ("knife", "rolling pin", "pestle and mortar",
                               "mixing bowl", "skewer", "cooking gauntlets",
                               "cook's outfit", "cook's hat", "cook's apron",
                               "cooks' assistant")):
        return "Cooking tools & utensils"
    if any(k in nlow for k in (
        "flour", "egg", "milk", "sugar", "pot of ", "bucket of ", "chocolate bar",
        "wheat", "potato seed", "barley", "yeast", "salt", "spice", "cooking apple",
        "tomato", "onion", "cabbage",
    )):
        return "Ingredients"
    if any(k in nlow for k in ("pie", "pizza", "stew", "cake",
                               "kebab", "curry", "gnomebowl", "gnome batta",
                               "gnome crunchies", "fishcake")):
        return "Special & combo food"
    return "Cooked food"


def _section_wc_fletching(item: dict) -> str:
    name = _name(item)
    nlow = name.lower()
    slot = _slot(item)
    if "axe" in nlow and "pickaxe" not in nlow and slot in ("weapon", "2h", ""):
        # Felling axes + standard axes
        return "Axes"
    if nlow.endswith(" logs") or nlow == "logs":
        return "Logs"
    if nlow.endswith(" log"):
        return "Logs"
    if any(k in nlow for k in ("lumberjack", "forester", "forestry",
                               "fletching cape", "fletching hood",
                               "woodcutting cape", "woodcutting hood",
                               "woodcutting outfit")):
        return "Woodcutting & Fletching outfits"
    if "unstrung" in nlow:
        return "Unstrung bows"
    if any(nlow.endswith(s) for s in (" bow", " shortbow", " longbow",
                                       " comp bow", " composite bow")):
        return "Strung bows"
    if any(nlow.endswith(s) for s in (" arrows", " bolts", " darts",
                                       " javelins", " javelin heads")):
        return "Finished arrows, darts & bolts"
    if any(k in nlow for k in (" feather", "bow string", "arrow shaft",
                               "headless arrow", "dart tip", "bolt tip",
                               "unfinished bolt", "unfinished broad")):
        return "Bow & ammo materials"
    if any(k in nlow for k in ("chisel", "knife")) and slot != "weapon":
        return "Fletching tools"
    # Forestry pet, secateurs, etc.
    return "Bow & ammo materials"


def _section_fishing(item: dict) -> str:
    nlow = _name(item).lower()
    if nlow.startswith("raw "):
        return "Raw fish"
    if any(k in nlow for k in ("fishing rod", "fly fishing rod", "barbarian rod",
                               "fishing net", "harpoon", "lobster pot",
                               "fishing explosive", "oily fishing rod",
                               "pearl rod")):
        return "Fishing tools"
    if any(k in nlow for k in ("fishing bait", "fishing feather", "feathers",
                               "bait", "stripy feather", "fish food")):
        return "Bait & consumables"
    if any(k in nlow for k in ("angler", "spirit angler", "fish barrel",
                               "fishing cape", "fishing hood")):
        return "Fishing outfit"
    if any(k in nlow for k in ("tempoross", "fishing rod-o-matic", "spirit flake",
                               "soaked page")):
        return "Fishing minigame items"
    # Fishbowl items, raw fish without "Raw" prefix
    return "Raw fish"


def _section_firemaking(item: dict) -> str:
    nlow = _name(item).lower()
    if "pyre logs" in nlow or nlow.endswith(" pyre logs"):
        return "Pyre logs"
    if "tinderbox" in nlow or "firelighter" in nlow or "bruma torch" in nlow:
        return "Tinderboxes & firelighting tools"
    if nlow.endswith(" logs"):
        return "Logs"
    if any(k in nlow for k in ("shade", "fiyr", "loar", "phrin", "riyl", "asyn")):
        return "Shade items"
    if any(k in nlow for k in ("wintertodt", "pyromancer", "bruma kindling",
                               "rejuvenation potion")):
        return "Wintertodt & minigame items"
    return "Firemaking outfit & rewards"


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
    if nlow.startswith("grimy ") or nlow.startswith("clean "):
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
    if any(k in nlow for k in (
        "limpwurt root", "white berries", "red spider", "wine of zamorak",
        "snape grass", "kebbit teeth", "newt", "potato cactus",
        "phoenix feather", "kebbit", "blue dragon scale", "swamp tar",
        "amylase crystal", "morchella mushroom",
    )):
        return "Secondaries"
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
    if nlow.endswith(" seed") or nlow.endswith(" seeds"):
        # Heuristic seed family routing — fall back to allotment.
        if any(k in nlow for k in ("potato seed", "onion seed", "cabbage seed",
                                   "tomato seed", "sweetcorn seed", "strawberry seed",
                                   "watermelon seed", "snape grass seed", "raw beetroot seed",
                                   "barley seed", "hammerstone seed", "asgarnian seed",
                                   "yanillian seed", "krandorian seed", "wildblood seed",
                                   "jute seed")):
            return "Allotment seeds" if "barley" not in nlow and "hammerstone" not in nlow and "asgarnian" not in nlow and "yanillian" not in nlow and "krandorian" not in nlow and "wildblood" not in nlow and "jute" not in nlow else "Hops seeds"
        if any(k in nlow for k in ("guam seed", "marrentill seed", "tarromin seed",
                                   "harralander seed", "ranarr seed", "toadflax seed",
                                   "irit seed", "avantoe seed", "kwuarm seed",
                                   "snapdragon seed", "cadantine seed", "lantadyme seed",
                                   "dwarf weed seed", "torstol seed", "huasca seed")):
            return "Herb seeds"
        if any(k in nlow for k in ("rosemary seed", "marigold seed", "nasturtium seed",
                                   "woad seed", "limpwurt seed", "white lily seed",
                                   "scarecrow")):
            return "Flower seeds"
        if any(k in nlow for k in ("redberry seed", "cadavaberry seed",
                                   "dwellberry seed", "jangerberry seed",
                                   "whiteberry seed", "poison ivy seed")):
            return "Bush seeds"
        if any(k in nlow for k in ("oak seed", "willow seed", "maple seed",
                                   "yew seed", "magic seed", "redwood tree seed",
                                   "acorn", "blisterwood seed")):
            return "Tree seeds"
        if any(k in nlow for k in ("apple tree seed", "banana tree seed",
                                   "orange tree seed", "curry tree seed",
                                   "pineapple seed", "papaya tree seed",
                                   "palm tree seed", "calquat tree seed",
                                   "dragonfruit tree seed", "celastrus seed",
                                   "spirit seed", "crystal acorn")):
            return "Fruit tree seeds"
        if any(k in nlow for k in ("mahogany seed", "teak seed", "hespori seed",
                                   "anima seed", "attas seed", "kronos seed",
                                   "iasor seed", "mushroom spore", "cactus seed",
                                   "potato cactus seed", "belladonna seed")):
            return "Special seeds"
        return "Allotment seeds"  # Fallback
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


_TAB_TO_FN = {
    "melee": lambda it: _section_combat(it, "melee"),
    "range": lambda it: _section_combat(it, "range"),
    "mage":  lambda it: _section_combat(it, "mage"),
    "prayer": _section_prayer,
    "cooking": _section_cooking,
    "wc_fletching": _section_wc_fletching,
    "fishing": _section_fishing,
    "firemaking": _section_firemaking,
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
_SKILL_PRODUCTION_TABS = {"cooking", "wc_fletching", "fishing", "firemaking",
                          "crafting", "mining_smithing", "herblore", "runecraft",
                          "farming", "hunter", "construction"}


def composite_sort_key(
    item: dict, tab_name: str, section: str, origin: str,
) -> tuple:
    """Return a sortable tuple positioning the item within its section.
    The caller has already bucketed by section_rank, so this key only
    needs to handle within-section ordering.

    Universal head: (group_rank, ...). primary < cross < override.
    """
    name = _name(item)
    nlow = name.lower()
    group = GROUP_RANK.get(origin, 2)

    # Tab-specific within-section sort.
    if tab_name in _COMBAT_TABS:
        if tab_name == "mage" and section == "Runes":
            return (group, _rune_rank(nlow), nlow, item.get("id", 0))
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
            return (group, tier, req, nlow, item.get("id", 0))
        if section == "Ammunition":
            return (group, _name_tier(name), nlow, item.get("id", 0))
        return (group, nlow, item.get("id", 0))

    if tab_name == "herblore" and section in (
        "Finished potions", "Barbarian mixes",
        "Divine, extended & upgraded variants", "Unfinished potions",
    ):
        return (group, _potion_family_rank(nlow), -_potion_dose(nlow),
                nlow, item.get("id", 0))

    if tab_name in _SKILL_PRODUCTION_TABS:
        # Same tier-first reasoning as combat tabs.
        tier = _name_tier(name)
        req = _req_max_level(item)
        return (group, tier, req, nlow, item.get("id", 0))

    if tab_name == "slayer":
        # Packing-priority via section_rank (caller); within section alpha.
        return (group, nlow, item.get("id", 0))

    # Default: alpha.
    return (group, nlow, item.get("id", 0))
