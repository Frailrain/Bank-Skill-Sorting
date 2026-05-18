"""Tab classification rules for the skillbank-data scraper.

Each TabSpec declares per-tab classification of items from the osrsbox-db dump
into ordered sections. The scraper applies these classifiers, sorts within
sections by tier (or a section-specific sort_key), and emits a proposed
SkillBankData.java.

Conventions:
- `_is_xxx(item)` — predicates returning bool.
- `_name_*` — name-matching helper factories.
- `_*_sort_key(item)` — custom sort keys for sections that need subdivision.
- `force_include` / `force_exclude` on a Section are name-based bypasses for
  judgment calls (e.g. Dragon pickaxe in melee weapons).
- Where osrsbox metadata is rich (equipment slot, weapon_type, combat stats),
  classifiers use it. Where the data isn't there (runes, consumables, quest
  items), classifiers fall back to name patterns or explicit name allowlists.
"""
from __future__ import annotations

import re
from scraper import Section, TabSpec, infer_tier


# ── Generic helpers ────────────────────────────────────────────────────────

_NOISE_SUFFIX_RE = re.compile(
    r"\((?:p\+?\+?|kp|or|g|t|deg|i\d|cr)\)|\s+(?:25|50|75|100)\s*$",
    re.IGNORECASE,
)


def _eq(it): return it.get("equipment") or {}
def _wp(it): return it.get("weapon") or {}
def _slot(it): return _eq(it).get("slot")
def _wtype(it): return (_wp(it).get("weapon_type") or "").lower()
def _max(*vals): return max(vals) if vals else 0
def _is_noise(it): return bool(_NOISE_SUFFIX_RE.search(it.get("name", "")))


def _name_contains(*toks):
    toks_lower = [t.lower() for t in toks]
    def pred(it):
        n = it.get("name", "").lower()
        return any(t in n for t in toks_lower)
    return pred


def _name_starts(*toks):
    toks_lower = [t.lower() for t in toks]
    def pred(it):
        n = it.get("name", "").lower()
        return any(n.startswith(t) for t in toks_lower)
    return pred


def _name_ends(*toks):
    toks_lower = [t.lower() for t in toks]
    def pred(it):
        n = it.get("name", "").lower()
        return any(n.endswith(t) for t in toks_lower)
    return pred


def _name_in(names):
    names_set = set(names)
    def pred(it):
        return it.get("name") in names_set
    return pred


def _and(*preds):
    def pred(it): return all(p(it) for p in preds)
    return pred


def _or(*preds):
    def pred(it): return any(p(it) for p in preds)
    return pred


def _not(pred):
    def inner(it): return not pred(it)
    return inner


def _never(it): return False


# ── Potion helpers ─────────────────────────────────────────────────────────

_POTION_DOSE_RE = re.compile(r"\((\d)\)\s*$")
_POTION_UNF_RE = re.compile(r"\bpotion\s*\(unf\)\s*$", re.IGNORECASE)
_POTION_FAMILY_STRIP_RE = re.compile(r"\((?:\d|unf)\)\s*$")


def _potion_dose(name: str) -> int:
    """Return dose 1..4, 0 for unfinished, -1 for non-dose-suffixed name."""
    if _POTION_UNF_RE.search(name):
        return 0
    m = _POTION_DOSE_RE.search(name)
    return int(m.group(1)) if m else -1


def _is_potion_family(*family_substrings):
    subs = [s.lower() for s in family_substrings]
    def pred(it):
        n = it.get("name", "").lower()
        if _potion_dose(it.get("name", "")) < 0:
            return False
        return any(s in n for s in subs)
    return pred


def _potion_sort_key(it):
    """Group by family, then dose 4 → 3 → 2 → 1 → unf."""
    name = it.get("name", "")
    family = _POTION_FAMILY_STRIP_RE.sub("", name).strip().lower()
    dose = _potion_dose(name)
    dose_order = {4: 0, 3: 1, 2: 2, 1: 3, 0: 4}.get(dose, 5)
    return (family, dose_order, name)


# ── Melee weapon class ordering ────────────────────────────────────────────

# Each tuple: (lower-case name substring, class score). Order matters — longer
# substrings checked first so "longsword" wins over "sword".
_MELEE_CLASS_PATTERNS = [
    ("godsword", 11),
    ("two-handed sword", 4), ("2h sword", 4),
    ("longsword", 3),
    ("battleaxe", 6),
    ("warhammer", 7),
    ("halberd", 9),
    ("dagger", 1),
    ("scimitar", 2),
    ("rapier", 12),
    ("saeldor", 12),
    ("soulreaper", 12),
    ("sword", 3),
    ("mace", 5),
    ("hammer", 7),
    ("spear", 8), ("hasta", 8),
    ("claw", 10),
    ("whip", 11),
    ("bulwark", 12),
    ("scythe", 13),
    ("flail", 12),
]


def _melee_weapon_class_score(name: str) -> int:
    lower = name.lower()
    for pattern, score in _MELEE_CLASS_PATTERNS:
        if pattern in lower:
            return score
    return 50


def _melee_weapon_sort_key(it):
    """tier primary, weapon-class secondary, id tertiary."""
    name = it["name"]
    tier = infer_tier(name, prefer="metal")
    klass = _melee_weapon_class_score(name)
    return (tier, klass, name)


# Weapon types we never want in melee even if stats suggest otherwise.
# Note: 'axe' is intentionally NOT here — osrsbox's weapon_type='axe' covers
# both woodcutting axes AND battleaxes/greataxes (e.g. Iron battleaxe). The
# `_is_melee_weapon` predicate disambiguates by name: items with weapon_type='axe'
# whose name contains 'battleaxe' / 'greataxe' / 'great axe' stay as melee
# weapons; pure woodcutting axes get excluded by the name check below.
_NOT_MELEE_WEAPON_TYPES = {
    "pickaxe", "staff", "powered staff", "wand", "bow", "crossbow",
    "thrown", "chinchompa", "salamander", "blaster", "polestaff",
}

# Gauntlets that pass the melee-gloves stat test but functionally aren't melee.
_SKILLING_GAUNTLETS = [
    "Cooking gauntlets", "Goldsmith gauntlets", "Smithing gauntlets",
    "Chaos gauntlets", "Family gauntlets", "Ice gauntlets",
    # Note: "Steel gauntlets" intentionally NOT here — they're defensive
    # melee gloves (Family Crest), not a skilling utility.
]

# Mime random event cosmetic outfit (IDs 626-664). 5 colours × 4 slots = 20 items.
_MIME_COLOURS = ("Pink", "Green", "Blue", "Cream", "Turquoise")
_MIME_PIECES = ("boots", "robe top", "robe bottoms", "hat")
_MIME_OUTFIT = [f"{c} {p}" for c in _MIME_COLOURS for p in _MIME_PIECES]

# Camouflage random event outfit (IDs 2894-2942). 5 colours × 5 slots = 25 items.
_CAMO_COLOURS = ("Grey", "Red", "Yellow", "Teal", "Purple")
_CAMO_PIECES = ("boots", "robe top", "robe bottoms", "hat", "gloves")
_CAMO_OUTFIT = [f"{c} {p}" for c in _CAMO_COLOURS for p in _CAMO_PIECES]

# Actual Mime random event outfit (IDs 3057-3061). 5 pieces.
_REAL_MIME_OUTFIT = ["Mime mask", "Mime top", "Mime legs", "Mime gloves", "Mime boots"]

# Chompy bird hat variants (IDs 2978-2995). 18 colour/style variants, all named identically
# "Chompy bird hat". Wiki canonical filter handles dedup; we just need to include the canonical.
_CHOMPY_HATS = ["Chompy bird hat"]

# Basic colour capes (cosmetic) — have tiny ranged defence (rd=2 vs md=1)
# which makes the range classifier capture them, but they're not real range gear.
_BASIC_COLOUR_CAPES = [
    "Red cape", "Black cape", "Blue cape", "Yellow cape",
    "Green cape", "Purple cape", "Orange cape",
    "Pink cape", "White cape",
] + [f"Team-{n} cape" for n in range(1, 51)]  # 50 team capes (Castle Wars cosmetic)

# D'hide armour names that should NEVER land in melee or mage even though
# basic stat checks might let them through (defensive numbers can spill).
_DHIDE_ALL_NAMES = [
    "Green d'hide vambraces", "Blue d'hide vambraces", "Red d'hide vambraces",
    "Black d'hide vambraces",
    "Green d'hide chaps", "Blue d'hide chaps", "Red d'hide chaps", "Black d'hide chaps",
    "Green d'hide body", "Blue d'hide body", "Red d'hide body", "Black d'hide body",
    "Leather vambraces",  # Leather vambraces are basic range gloves
    "Studded body", "Studded chaps",
]

# Cape of legends — quest cosmetic, not real combat gear.
_QUEST_COSMETIC_CAPES = ["Cape of legends"]

# Holiday rares + halloween mask variants.
_HOLIDAY_RARES = [
    "Christmas cracker", "Highwayman mask", "Easter egg", "Bunny ears",
    "Halloween mask", "Green halloween mask", "Blue halloween mask",
    "Red halloween mask", "Pumpkin", "Disk of returning",
    "Red partyhat", "Yellow partyhat", "Blue partyhat", "Green partyhat",
    "Purple partyhat", "White partyhat", "Santa hat",
]

# Quest cosmetic equipment with combat stats that should NOT land in melee.
_QUEST_COSMETIC_MELEE = [
    "Cattleprod", "Khazard helmet", "Khazard armour", "Khazard cell keys",
    "Dark dagger", "Glowing dagger",
    "Gnome amulet", "Beads of the dead",
    # Machetes are navigation tools (Karamja jungle), not combat weapons.
    "Machete", "Opal machete", "Jade machete", "Red topaz machete",
]

# Splitbark armour — mage hybrid set with minor melee defence but canonically
# magic gear. Force-exclude from melee armour sections.
_SPLITBARK_PIECES = [
    "Splitbark helm", "Splitbark body", "Splitbark legs",
    "Splitbark gauntlets", "Splitbark boots",
]

# Rogue equipment — agility_thieving only; should NOT be in combat tabs.
_ROGUE_PIECES = ["Rogue top", "Rogue mask", "Rogue trousers", "Rogue gloves", "Rogue boots"]

# Barrows armour — canonical OSRS combat affinity per set, but defence stats
# are split across multiple styles which makes the stat-dominance classifier
# put pieces in the wrong tab. Use these constants to surgical-exclude.
_AHRIM_PIECES = ["Ahrim's hood", "Ahrim's robetop", "Ahrim's robeskirt"]  # mage only
_KARIL_PIECES = ["Karil's coif", "Karil's leathertop", "Karil's leatherskirt"]  # range only
# Melee Barrows sets (Dharok/Guthan/Torag/Verac) body parts cross-tag to range
# wrongly via range_defence; weapons + Verac are correct.
_DHAROK_BODY = ["Dharok's helm", "Dharok's platebody", "Dharok's platelegs"]
_GUTHAN_BODY = ["Guthan's helm", "Guthan's platebody", "Guthan's chainskirt"]
_TORAG_BODY = ["Torag's helm", "Torag's platebody", "Torag's platelegs"]
_MELEE_BARROWS_BODY = _DHAROK_BODY + _GUTHAN_BODY + _TORAG_BODY


def _is_melee_weapon(it):
    if not it.get("equipable_weapon") or _is_noise(it):
        return False
    if _slot(it) not in ("weapon", "2h"):
        return False
    wt = _wtype(it)
    if wt in _NOT_MELEE_WEAPON_TYPES:
        return False
    if wt == "axe":
        # osrsbox conflates wc axes and battleaxes under weapon_type='axe'.
        n = (it.get("name") or "").lower()
        if not any(k in n for k in ("battleaxe", "greataxe", "great axe")):
            return False
    e = _eq(it)
    melee = _max(e.get("attack_slash", 0), e.get("attack_stab", 0), e.get("attack_crush", 0))
    other = _max(e.get("attack_ranged", 0), e.get("attack_magic", 0))
    return melee > 0 and melee >= other


def _slot_pred(slot):
    """Generic melee armour predicate by slot. Defence-leaning OR
    melee-offensive (strength bonus / attack bonus) when defence is zero —
    handles stat-only amulets like Amulet of strength which have
    melee_strength but no defence."""
    def pred(it):
        if _slot(it) != slot or _is_noise(it):
            return False
        e = _eq(it)
        md = _max(e.get("defence_stab", 0), e.get("defence_slash", 0), e.get("defence_crush", 0))
        if md > 0:
            rd = e.get("defence_ranged", 0)
            mgd = e.get("defence_magic", 0)
            return md >= rd and md >= mgd
        ms = e.get("melee_strength", 0)
        ma = _max(e.get("attack_slash", 0), e.get("attack_stab", 0), e.get("attack_crush", 0))
        if ms <= 0 and ma <= 0:
            return False
        rs = e.get("ranged_strength", 0)
        mdmg = e.get("magic_damage", 0)
        max_off = max(ms, ma)
        return max_off >= rs and max_off >= mdmg
    return pred


# ── Range helpers ──────────────────────────────────────────────────────────

def _is_range_weapon_type(*types):
    types_set = set(types)
    def pred(it):
        if not it.get("equipable_weapon") or _is_noise(it):
            return False
        return _wtype(it) in types_set
    return pred


def _is_range_ammo(it):
    if _slot(it) != "ammo" or _is_noise(it):
        return False
    e = _eq(it)
    return e.get("ranged_strength", 0) > 0 or e.get("attack_ranged", 0) > 0


def _is_range_armour_slot(slot):
    def pred(it):
        if _slot(it) != slot or _is_noise(it):
            return False
        e = _eq(it)
        if e.get("ranged_strength", 0) > 0:
            return True
        rd = e.get("defence_ranged", 0)
        md = _max(e.get("defence_stab", 0), e.get("defence_slash", 0), e.get("defence_crush", 0))
        mgd = e.get("defence_magic", 0)
        if rd <= 0:
            return False
        return rd >= md and rd >= mgd
    return pred


# ── Mage helpers ───────────────────────────────────────────────────────────

# Hand-maintained — covers every regular rune in OSRS plus combos.
_BASIC_RUNES = {
    "Air rune", "Water rune", "Earth rune", "Fire rune",
    "Mind rune", "Body rune", "Cosmic rune", "Chaos rune",
    "Nature rune", "Law rune", "Death rune", "Astral rune",
    "Blood rune", "Soul rune", "Wrath rune", "Sunfire rune",
}
_COMBO_RUNES = {
    "Mist rune", "Dust rune", "Mud rune",
    "Smoke rune", "Steam rune", "Lava rune",
}
_ESSENCE = {"Pure essence", "Rune essence", "Daeyalt essence", "Blood essence"}


def _is_mage_armour_slot(slot):
    def pred(it):
        if _slot(it) != slot or _is_noise(it):
            return False
        e = _eq(it)
        if e.get("magic_damage", 0) > 0 or e.get("attack_magic", 0) > 0:
            return True
        mgd = e.get("defence_magic", 0)
        md = _max(e.get("defence_stab", 0), e.get("defence_slash", 0), e.get("defence_crush", 0))
        rd = e.get("defence_ranged", 0)
        if mgd <= 0:
            return False
        return mgd >= md and mgd >= rd
    return pred


def _is_mage_weapon_type(*types):
    types_set = set(types)
    def pred(it):
        if not it.get("equipable_weapon") or _is_noise(it):
            return False
        return _wtype(it) in types_set
    return pred


# ── Prayer helpers ─────────────────────────────────────────────────────────

def _is_bones(it):
    n = it.get("name", "")
    return n == "Bones" or n.endswith(" bones") or n.endswith(" bone")


def _is_ashes(it):
    n = it.get("name", "")
    return n.endswith(" ashes") or n == "Ashes" or n.endswith("'s ashes")


# ── Fishing helpers ────────────────────────────────────────────────────────

_RAW_FISH_LEVEL = {
    "Raw shrimps": 1, "Raw sardine": 5, "Raw herring": 10, "Raw anchovies": 15,
    "Raw mackerel": 16, "Raw trout": 20, "Raw cod": 23, "Raw pike": 25,
    "Raw salmon": 30, "Raw tuna": 35, "Raw lobster": 40, "Raw bass": 46,
    "Raw swordfish": 50, "Raw monkfish": 62, "Raw karambwan": 65,
    "Raw karambwanji": 5, "Raw shark": 76, "Raw anglerfish": 82,
    "Raw dark crab": 85, "Raw manta ray": 81, "Raw sea turtle": 79,
    "Raw cave eel": 38, "Raw lava eel": 53, "Raw slimy eel": 28,
    "Raw infernal eel": 80, "Raw sacred eel": 87, "Raw rainbow fish": 38,
}
_COOKED_FISH_HEAL = {
    "Shrimps": 3, "Sardine": 4, "Herring": 5, "Anchovies": 1,
    "Mackerel": 6, "Trout": 7, "Cod": 7, "Pike": 8,
    "Salmon": 9, "Tuna": 10, "Lobster": 12, "Bass": 13,
    "Swordfish": 14, "Monkfish": 16, "Cooked karambwan": 18,
    "Shark": 20, "Anglerfish": 22, "Dark crab": 22,
    "Manta ray": 22, "Sea turtle": 21,
    "Cave eel": 8, "Lava eel": 14, "Cooked slimy eel": 5,
    "Cooked rainbow fish": 11,
}


def _is_raw_fish(it):
    return it.get("name") in _RAW_FISH_LEVEL


def _is_cooked_fish(it):
    return it.get("name") in _COOKED_FISH_HEAL


def _raw_fish_sort_key(it):
    return (_RAW_FISH_LEVEL.get(it["name"], 999), it["id"])


def _cooked_fish_sort_key(it):
    return (_COOKED_FISH_HEAL.get(it["name"], 999), it["id"])


# ── Cooking helpers ────────────────────────────────────────────────────────

_PIES = {
    "Redberry pie", "Meat pie", "Apple pie", "Garden pie", "Fish pie",
    "Admiral pie", "Wild pie", "Summer pie", "Mushroom pie", "Dragonfruit pie",
}
_PIZZAS = {"Plain pizza", "Meat pizza", "Anchovy pizza", "Pineapple pizza"}
_STEWS = {"Stew", "Curry", "Spicy stew"}
_CAKES = {"Cake", "Chocolate cake", "Slice of cake", "Chocolate slice"}
_BREADS = {"Bread", "Bread dough", "Sweetcorn", "Pitta bread"}
_GNOMEFOOD = {  # gnome cocktails / drinks / food
    # Crunchies + battas (canonical finished products)
    "Worm crunchies", "Chocchip crunchies", "Spicy crunchies", "Toad crunchies",
    "Choc-ice", "Frog spawn", "Worm hole", "Veg ball",
    "Tangled toad's legs", "Worm batta", "Toad batta", "Cheese+tom batta",
    "Fruit batta", "Vegetable batta",
    "Gnomebowl", "Chocolate bomb",
    # Cocktail finished products
    "Wizard blizzard", "Short green guy", "Drunk dragon",
    "Choc saturday", "Blurberry special", "Pineapple punch", "Fruit blast",
    # Premade variants (cooking inventory items)
    "Premade w'm batta", "Premade ch ball", "Premade fr blast",
    "Premade p punch", "Premade choc s'dy", "Premade w' blizz",
    "Premade dr' dragon", "Premade s g g", "Premade blurb' sp.",
    "Premade sgg", "Premade fr' blast", "Premade p' punch",
    # Bartending base spirits + tools
    "Vodka", "Whisky", "Gin", "Brandy",
    "Cocktail guide", "Cocktail shaker", "Cocktail glass",
    # Toad / worm / leg gnome ingredients
    "Swamp toad", "Toad's legs", "Equa toad's legs", "Spicy toad's legs",
    "Seasoned legs", "Spicy worm", "King worm",
    # Gnome cooking tools/intermediates
    "Batta tin", "Crunchy tray", "Gnomebowl mould",
    "Gianne's cook book", "Gnome spice", "Gianne dough",
    "Odd gnomebowl", "Burnt gnomebowl", "Half baked bowl", "Raw gnomebowl",
    "Unfinished bowl", "Odd crunchies", "Burnt crunchies",
    # Fruit chunks/slices/rings for cocktails
    "Lemon chunks", "Lemon slices",
    "Orange chunks", "Orange slices",
    "Pineapple chunks", "Pineapple ring",
    "Lime", "Lime chunks", "Lime slices",
    # Other gnome ingredients
    "Equa leaves", "Dwellberries",
}


# ── Firemaking helpers ─────────────────────────────────────────────────────

_LOG_TIERS = {
    "Logs": 1, "Achey tree logs": 1, "Oak logs": 2, "Willow logs": 3,
    "Teak logs": 4, "Maple logs": 4, "Mahogany logs": 5, "Arctic pine logs": 4,
    "Yew logs": 5, "Magic logs": 6, "Redwood logs": 7,
    "Pyre logs": 1, "Oak pyre logs": 2, "Willow pyre logs": 3,
    "Teak pyre logs": 4, "Maple pyre logs": 4, "Mahogany pyre logs": 5,
    "Yew pyre logs": 5, "Magic pyre logs": 6, "Redwood pyre logs": 7,
}


def _is_log(it):
    return it.get("name") in _LOG_TIERS or it.get("name", "").endswith(" logs") \
        or it.get("name", "").endswith(" pyre logs") or it.get("name") == "Logs"


def _log_sort_key(it):
    return (_LOG_TIERS.get(it["name"], 999), it["id"])


# ── Mining helpers ─────────────────────────────────────────────────────────

_ORES = {
    "Copper ore": 1, "Tin ore": 1, "Iron ore": 2, "Silver ore": 3,
    "Coal": 4, "Gold ore": 5, "Mithril ore": 6, "Adamantite ore": 7,
    "Runite ore": 8, "Amethyst": 9, "Volcanic ash": 1, "Soft clay": 1,
    "Clay": 1, "Sandstone (1kg)": 1, "Granite (500g)": 1,
    "Lovakite ore": 5, "Daeyalt ore": 6,
    "Blurite ore": 2, "Elemental ore": 2,
}
_BARS = {
    "Bronze bar": 1, "Iron bar": 2, "Steel bar": 3, "Silver bar": 4,
    "Gold bar": 5, "Mithril bar": 6, "Adamantite bar": 7, "Runite bar": 8,
    "Blurite bar": 2, "Lovakite bar": 5,
}


def _is_ore(it): return it.get("name") in _ORES
def _is_bar(it): return it.get("name") in _BARS
def _ore_sort_key(it): return (_ORES.get(it["name"], 999), it["id"])
def _bar_sort_key(it): return (_BARS.get(it["name"], 999), it["id"])


# ── Herb helpers ───────────────────────────────────────────────────────────

_HERBS = [
    "Guam leaf", "Marrentill", "Tarromin", "Harralander", "Ranarr weed",
    "Toadflax", "Spirit weed", "Irit leaf", "Avantoe", "Kwuarm",
    "Snapdragon", "Cadantine", "Lantadyme", "Dwarf weed", "Torstol",
]
_HERB_LEVELS = {name: i + 1 for i, name in enumerate(_HERBS)}


def _is_grimy_herb(it):
    n = it.get("name", "")
    return n.startswith("Grimy ") and any(h.lower() in n.lower() for h in _HERBS)


def _is_clean_herb(it):
    n = it.get("name", "")
    return n in _HERBS


def _herb_sort_key(it):
    n = it.get("name", "").replace("Grimy ", "")
    # Grimy items lower-case the herb word ("Grimy guam leaf"); canonical
    # clean herbs are capitalised ("Guam leaf"). Match either form.
    return (
        _HERB_LEVELS.get(n, _HERB_LEVELS.get(n[:1].upper() + n[1:], 999)),
        it["id"],
    )


# ── Farming helpers ────────────────────────────────────────────────────────

def _is_seed(it):
    n = it.get("name", "")
    if n.endswith(" seed") or n.endswith(" seedling") or n.endswith(" sapling"):
        return True
    # Non-suffix seeds
    return n in {"Mushroom spore", "Acorn"}


_HARVEST_FRUITS = ("Apples", "Oranges", "Strawberries", "Bananas", "Lemons", "Limes", "Pineapples")
_HARVEST_VEGS = ("Potatoes", "Onions", "Cabbages", "Tomatoes", "Sweetcorn")
_HARVEST_PRODUCE_RE = re.compile(
    r"^(?:" + "|".join(_HARVEST_FRUITS + _HARVEST_VEGS) + r")\(\d+\)$"
)


def _is_harvest_produce(it):
    return bool(_HARVEST_PRODUCE_RE.match(it.get("name", "")))


def _is_sapling(it):
    return it.get("name", "").endswith(" sapling")


# ── Runecraft helpers ──────────────────────────────────────────────────────

def _is_talisman(it):
    return it.get("name", "").endswith(" talisman")


def _is_tiara(it):
    n = it.get("name", "")
    return n.endswith(" tiara") or n == "Tiara"


# ── Construction helpers ───────────────────────────────────────────────────

_PLANKS = {"Plank": 1, "Oak plank": 2, "Teak plank": 3, "Mahogany plank": 4}
_NAILS = {
    "Bronze nails": 1, "Iron nails": 2, "Steel nails": 3,
    "Black nails": 4, "Mithril nails": 5, "Adamantite nails": 6, "Rune nails": 7,
}


def _is_plank(it): return it.get("name") in _PLANKS
def _is_nails(it): return it.get("name") in _NAILS
def _plank_sort_key(it): return (_PLANKS.get(it["name"], 999), it["id"])
def _nails_sort_key(it): return (_NAILS.get(it["name"], 999), it["id"])


# ── Hunter helpers ─────────────────────────────────────────────────────────

_IMPLING_TIERS = [
    "Baby impling jar", "Young impling jar", "Gourmet impling jar",
    "Earth impling jar", "Essence impling jar", "Eclectic impling jar",
    "Nature impling jar", "Magpie impling jar", "Ninja impling jar",
    "Crystal impling jar", "Dragon impling jar", "Lucky impling jar",
    "Zombie impling jar",
]


def _is_impling(it):
    return it.get("name") in _IMPLING_TIERS


def _impling_sort_key(it):
    n = it.get("name", "")
    try:
        return (_IMPLING_TIERS.index(n), it["id"])
    except ValueError:
        return (999, it["id"])


# ── Sailing helpers ────────────────────────────────────────────────────────

# Items released after the osrsbox/osrsreboxed dump's last update (Oct 2024) — the
# Sailing skill launched Nov 2025. These IDs are from in-game / wiki lookups and
# are force_included on the Sailing tab since they're not in the cache.
_SAILING_FORCE_IDS = {
    31288: "Sailing cape",
    31290: "Sailing cape(t)",
    31803: "Spyglass",
    31807: "Crowbar (Sailing)",  # speculative naming
    31964: "Repair kit",
    31985: "Captain's log",
    32309: "Raw giant krill", 32312: "Cooked giant krill",
    32325: "Raw yellowfin", 32328: "Cooked yellowfin",
    32333: "Raw halibut", 32336: "Cooked halibut",
    32341: "Raw bluefin", 32344: "Cooked bluefin",
    32349: "Raw marlin", 32352: "Cooked marlin",
}


# ── Per-tab specs ──────────────────────────────────────────────────────────

# === MELEE ===
_BARROWS_MELEE_PIECES = {
    # Dharok's / Guthan's / Torag's / Verac's (melee Barrows)
    "Dharok's helm", "Dharok's platebody", "Dharok's platelegs", "Dharok's greataxe",
    "Guthan's helm", "Guthan's platebody", "Guthan's chainskirt", "Guthan's warspear",
    "Torag's helm", "Torag's platebody", "Torag's platelegs", "Torag's hammers",
    "Verac's helm", "Verac's brassard", "Verac's plateskirt", "Verac's flail",
}
MELEE = TabSpec(
    name="melee", const_name="TAG_MELEE",
    sections=[
        Section("Combat utility", _name_in({
            "Cannon base", "Cannon stand", "Cannon barrels", "Cannon furnace",
            "Cannonball", "Granite cannonball", "Steel cannonball",
        })),
        Section("Weapons", _is_melee_weapon,
                sort_key=_melee_weapon_sort_key,
                force_include=["Dragon pickaxe"],
                force_exclude=_QUEST_COSMETIC_MELEE),
        Section("Helmets", _slot_pred("head"),
                force_include=list(n for n in _BARROWS_MELEE_PIECES if "helm" in n.lower()),
                force_exclude=["Khazard helmet", "Robin hood hat", "Mime mask", "Splitbark helm", "Bearhead",
                               "Ahrim's hood", "Karil's coif", "Rogue mask"]
                + [n for n in _CAMO_OUTFIT if "hat" in n.lower()]),
        Section("Body armour", _slot_pred("body"),
                force_include=list(n for n in _BARROWS_MELEE_PIECES if any(k in n.lower() for k in ("platebody","brassard"))),
                force_exclude=["Khazard armour", "Studded body", "Carnillean armour", "Mime top", "Splitbark body",
                               "Ahrim's robetop", "Karil's leathertop", "Rogue top"]
                + [n for n in _DHIDE_ALL_NAMES if "body" in n.lower()]
                + [n for n in _MIME_OUTFIT if "robe top" in n.lower()]
                + [n for n in _CAMO_OUTFIT if "robe top" in n.lower()]),
        Section("Legs", _slot_pred("legs"),
                force_include=list(n for n in _BARROWS_MELEE_PIECES if any(k in n.lower() for k in ("platelegs","chainskirt","plateskirt"))),
                force_exclude=["Mime legs", "Splitbark legs",
                               "Ahrim's robeskirt", "Karil's leatherskirt", "Rogue trousers"]
                + [n for n in _DHIDE_ALL_NAMES if "chaps" in n.lower()]
                + [n for n in _MIME_OUTFIT if "robe bottoms" in n.lower()]
                + [n for n in _CAMO_OUTFIT if "robe bottoms" in n.lower()]),
        Section("Boots", _slot_pred("feet"),
                force_exclude=["Ranger boots", "Mime boots", "Splitbark boots", "Rogue boots", "Mourner boots"]
                + [n for n in _MIME_OUTFIT if "boots" in n.lower()]
                + [n for n in _CAMO_OUTFIT if "boots" in n.lower()]),
        Section("Gloves", _slot_pred("hands"),
                force_exclude=_SKILLING_GAUNTLETS
                + [n for n in _DHIDE_ALL_NAMES if "vambrace" in n.lower()]
                + ["Leather vambraces", "Klank's gauntlets", "Mime gloves", "Splitbark gauntlets", "Rogue gloves", "Mourner gloves"]
                + [n for n in _CAMO_OUTFIT if "gloves" in n.lower()]),
        Section("Shields", _slot_pred("shield"),
                force_include=["Granite shield"]),
        Section("Capes", _slot_pred("cape"),
                force_exclude=_QUEST_COSMETIC_CAPES),
        Section("Amulets", _slot_pred("neck"),
                force_exclude=["Gnome amulet", "Beads of the dead"]),
        Section("Rings", _slot_pred("ring")),
        Section("Combat potions",
                _is_potion_family("super combat", "super strength", "super attack",
                                  "super defence", "strength potion", "attack potion",
                                  "defence potion", "combat potion", "divine super",
                                  "saradomin brew", "zamorak brew"),
                sort_key=_potion_sort_key),
        Section("Restores (cross-tag)",
                _is_potion_family("super restore", "sanfew"),
                sort_key=_potion_sort_key),
        Section("Combat food", _is_cooked_fish, sort_key=_cooked_fish_sort_key),
    ],
    variant_allowlist=[
        "Slayer helmet (i)", "Black mask (i)",
        "Imbued saradomin cape", "Imbued zamorak cape", "Imbued guthix cape",
    ],
)

# === RANGE ===
_DHIDE_PIECE_RE = re.compile(r"\bd'?hide\b", re.IGNORECASE)
_DRAGONHIDE_RAW_RE = re.compile(r"\bdragonhide\b|\bdragon leather\b", re.IGNORECASE)
RANGE = TabSpec(
    name="range", const_name="TAG_RANGE",
    sections=[
        Section("Cannon parts (cross-tag)", _name_in({
            "Cannon base", "Cannon stand", "Cannon barrels", "Cannon furnace",
            "Cannonball", "Granite cannonball", "Steel cannonball",
        })),
        Section("Ammunition", _is_range_ammo),
        Section("Bows", _is_range_weapon_type("bow")),
        Section("Crossbows", _is_range_weapon_type("crossbow")),
        Section("Thrown", _is_range_weapon_type("thrown")),
        Section("Special weapons", _is_range_weapon_type("chinchompa", "blaster")),
        Section("D'hide armour", lambda it: bool(_DHIDE_PIECE_RE.search(it.get("name") or ""))),
        Section("Raw dragonhide (cross-tag with crafting)",
                lambda it: bool(_DRAGONHIDE_RAW_RE.search(it.get("name") or ""))),
        Section("Helmets", _is_range_armour_slot("head"),
                force_include=["Robin hood hat", "Karil's coif"],
                force_exclude=["Dharok's helm", "Guthan's helm", "Torag's helm", "Rogue mask"]),
        Section("Body", _is_range_armour_slot("body"),
                force_include=["Karil's leathertop"],
                force_exclude=["Dharok's platebody", "Guthan's platebody", "Torag's platebody", "Rogue top"]),
        Section("Legs", _is_range_armour_slot("legs"),
                force_include=["Karil's leatherskirt"],
                force_exclude=["Dharok's platelegs", "Guthan's chainskirt", "Torag's platelegs", "Rogue trousers"]),
        Section("Boots", _is_range_armour_slot("feet"),
                force_include=["Ranger boots"],
                force_exclude=["Rogue boots"]),
        Section("Gloves", _is_range_armour_slot("hands"),
                force_exclude=["Rogue gloves"]),
        Section("Shields", _is_range_armour_slot("shield")),
        Section("Capes", _is_range_armour_slot("cape"),
                force_exclude=_BASIC_COLOUR_CAPES + _QUEST_COSMETIC_CAPES),
        # Team capes are caught by the section above via _is_range_armour_slot
        # because the cape has defence_ranged=2 like basic colour capes.
        # Mass-exclude via a pattern force_exclude below in classify-time.
        Section("Amulets", _is_range_armour_slot("neck"),
                force_exclude=["Beads of the dead"]),
        Section("Rings", _is_range_armour_slot("ring")),
        Section("Ranging potions",
                _is_potion_family("ranging potion", "divine ranging"),
                sort_key=_potion_sort_key),
        Section("Bait / feathers (cross-tag)", _name_in({
            "Feather", "Stripy feather", "Red feather", "Blue feather",
            "Yellow feather", "Orange feather", "Bone bolts",
        })),
        Section("Combat food (cross-tag)", _is_cooked_fish, sort_key=_cooked_fish_sort_key),
    ],
)

# === MAGE ===
MAGE = TabSpec(
    name="mage", const_name="TAG_MAGE",
    sections=[
        Section("Basic runes", _name_in(_BASIC_RUNES)),
        Section("Combo runes", _name_in(_COMBO_RUNES)),
        Section("Essence", _name_in(_ESSENCE)),
        Section("Staves", _is_mage_weapon_type("staff", "powered staff", "polestaff")),
        Section("Wands", _is_mage_weapon_type("wand")),
        Section("Tomes", _name_ends(" tome"),
                force_exclude=["Shaman's tome"]),
        Section("Helmets", _is_mage_armour_slot("head"),
                force_exclude=["Mime mask", "Rogue mask"]
                + [n for n in _MIME_OUTFIT if "hat" in n.lower()]
                + [n for n in _CAMO_OUTFIT if "hat" in n.lower()]),
        Section("Body", _is_mage_armour_slot("body"),
                force_exclude=["Karil's leathertop", "Rogue top"]),
        Section("Legs", _is_mage_armour_slot("legs"),
                force_exclude=["Karil's leatherskirt", "Rogue trousers"]),
        Section("Boots", _is_mage_armour_slot("feet"),
                force_exclude=["Rogue boots"]),
        Section("Gloves", _is_mage_armour_slot("hands"),
                force_include=["Chaos gauntlets"],
                force_exclude=[n for n in _DHIDE_ALL_NAMES if "vambrace" in n.lower()]
                + ["Leather vambraces", "Rogue gloves"]),
        Section("Shields", _is_mage_armour_slot("shield")),
        Section("Capes", _is_mage_armour_slot("cape"),
                force_exclude=_QUEST_COSMETIC_CAPES),
        Section("Amulets", _is_mage_armour_slot("neck"),
                force_exclude=["Beads of the dead"]),
        Section("Rings", _is_mage_armour_slot("ring")),
        Section("Magic potions",
                _is_potion_family("magic potion", "ancient brew", "forgotten brew", "battlemage"),
                sort_key=_potion_sort_key),
        Section("Orbs (cross-tag with crafting)", _name_in({
            "Unpowered orb", "Air orb", "Water orb", "Earth orb", "Fire orb",
            "Light orb", "Empty light orb",
        })),
        Section("God cloaks", _name_in({
            "Saradomin cloak", "Zamorak cloak", "Guthix cloak",
            "Saradomin cape", "Zamorak cape", "Guthix cape",
        })),
        Section("Combat food (cross-tag)", _is_cooked_fish, sort_key=_cooked_fish_sort_key),
    ],
    variant_allowlist=[
        "Imbued saradomin cape", "Imbued zamorak cape", "Imbued guthix cape",
    ],
)

# === PRAYER ===
PRAYER = TabSpec(
    name="prayer", const_name="TAG_PRAYER",
    sections=[
        Section("Bones", _is_bones),
        Section("Ashes", _is_ashes),
        Section("Ensouled heads", _name_starts("Ensouled ")),
        Section("Prayer potions", _is_potion_family("prayer potion"), sort_key=_potion_sort_key),
        Section("Super restores", _is_potion_family("super restore"), sort_key=_potion_sort_key),
        Section("Sanfew", _is_potion_family("sanfew"), sort_key=_potion_sort_key),
        Section("Saradomin brews", _is_potion_family("saradomin brew"), sort_key=_potion_sort_key),
        Section("Holy symbols", _or(
            _name_in({"Holy symbol", "Unholy symbol", "Holy book", "Unholy book",
                      "Unstrung symbol", "Unblessed symbol", "Unstrung emblem",
                      "Unpowered symbol", "Holy mould", "Silver sickle",
                      "Silver sickle (b)"}),
            _name_starts("Holy book"), _name_starts("Unholy book"),
            _name_starts("Book of "),
        ), force_exclude=["Book of haricanto", "Book of portraiture",
                          "Book of 'h.a.m'", "Book of HAM"]),
        Section("Robes (monk/proselyte/initiate/druid)", _or(
            _name_starts("Monk's "), _name_starts("Proselyte "),
            _name_starts("Initiate "), _name_starts("Devout "),
            _name_starts("Druid's "),
        )),
        Section("Bone secondaries", _or(
            _name_in({
                "Bonemeal pot", "Bucket of slime", "Vial of milk",
                "Mort myre fungus",
            }),
            _name_ends(" bonemeal"),  # Bonemeal, Bat bonemeal, Dragon bonemeal, etc.
            _name_in({"Bonemeal"}),
        )),
        Section("Shades remains", _name_ends(" remains")),
        Section("Quest-related prayer items", _name_in({
            "Ogre coffin key", "Ring of the gods", "Ring of the gods (i)",
            "Damaged book", "Book of balance",
            # Note: "Diary", "Journal", and "Manual" all removed — too generic
            # (Hazeel Cult diary, Nature Spirit journal, Fremennik manual aren't
            # prayer items; Holy/Unholy book + Book of starts catch the real ones).
        })),
        Section("God pages (Treasure trail)", _and(
            _or(_name_starts("Saradomin page "),
                _name_starts("Zamorak page "),
                _name_starts("Guthix page "),
                _name_starts("Armadyl page "),
                _name_starts("Bandos page "),
                _name_starts("Ancient page ")),
            _or(_name_ends(" 1"), _name_ends(" 2"),
                _name_ends(" 3"), _name_ends(" 4")),
        )),
        Section("Prayer accessories", _name_in({
            "Holy wrench", "Bonecrusher", "Bonecrusher necklace",
            "Hand of glory", "Prayer cape", "Prayer cape(t)", "Prayer hood",
        })),
        Section("Pyre log oils",
                _is_potion_family("olive oil", "sacred oil"),
                sort_key=_potion_sort_key),
        Section("Proselyte", _name_starts("Proselyte ")),
    ],
)

# === COOKING ===
COOKING = TabSpec(
    name="cooking", const_name="TAG_COOKING",
    sections=[
        Section("Cooking tools", _name_in({
            "Chef's hat", "Cooking cape", "Cooking cape(t)", "Cooking hood",
            "Cooking gauntlets", "Spice", "Garlic", "Rolling pin",
            "Bowl", "Empty pot", "Jug", "Cake tin",
        })),
        Section("Raw fish", _is_raw_fish, sort_key=_raw_fish_sort_key),
        Section("Cooked fish", _is_cooked_fish, sort_key=_cooked_fish_sort_key),
        Section("Pies", _name_in(_PIES)),
        Section("Pizzas", _name_in(_PIZZAS)),
        Section("Stews & curries", _name_in(_STEWS)),
        Section("Cakes", _name_in(_CAKES)),
        Section("Breads", _name_in(_BREADS)),
        Section("Gnome food / cocktails", _or(
            _name_in(_GNOMEFOOD),
            _name_starts("Premade "),
            _name_starts("Unfinished cocktail"),
            _name_starts("Unfinished batta"),
            _name_starts("Unfinished crunchy"),
            _name_starts("Half baked "),
            _name_in({
                "Odd batta", "Odd crunchies", "Odd gnomebowl",
                "Pizza base", "Incomplete pizza", "Uncooked pizza",
                "Wrapped oomlie", "Pie dish",
            }),
        )),
        Section("Beverages", _or(
            _name_in({
                "Beer", "Asgarnian ale", "Dwarven stout", "Wizard's mind bomb",
                "Bandit's brew", "Cider", "Mature cider",
                "Tea", "Cup of tea", "Tea (gnome)", "Grog", "Beer glass",
                "Keg of beer", "Mature dwarven stout",
                "Mature wmb", "Greenman's ale", "Dragon bitter",
                "Moonlight mead", "Axeman's folly", "Chef's delight",
                "Slayer's respite", "Ale yeast", "Calquat keg",
                "Strawberry", "Coconut milk", "Apple mush",
            }),
            # Matured beer variants (m) and (4)/(m4) charge variants
            _name_ends("(m)"),
            # Beer-family endswith (4) or (m4) — broad pattern catching all brews.
            _and(_or(_name_starts("Asgarnian ale"), _name_starts("Dwarven stout"),
                     _name_starts("Greenman"), _name_starts("Dragon bitter"),
                     _name_starts("Moonlight mead"), _name_starts("Mind bomb"),
                     _name_starts("Axeman's folly"), _name_starts("Chef's delight"),
                     _name_starts("Slayer's respite"), _name_starts("Cider"),
                     _name_starts("Wizard's mind bomb")),
                 _or(_name_ends("(4)"), _name_ends("(m4)"))),
        )),
        Section("Burnt food", _name_starts("Burnt ")),
        Section("Containers (water/milk/etc)", _name_in({
            "Vial", "Vial of water", "Empty vial", "Vial of blood",
            "Bowl", "Bowl of water", "Empty bowl",
            "Pot", "Empty pot", "Pot of flour", "Pot of cream",
            "Jug", "Jug of water", "Jug of wine",
            "Bucket", "Bucket of water", "Bucket of milk", "Bucket of sand",
            "Waterskin(4)", "Waterskin(3)", "Waterskin(2)", "Waterskin(1)", "Waterskin(0)",
        })),
        Section("Raw meat & ingredients", _or(
            _name_starts("Raw "),
            _name_in({
                "Pot of flour", "Pot of cream", "Bucket of milk", "Egg",
                "Onion", "Tomato", "Cabbage", "Potato", "Sweetcorn",
                "Cooking apple", "Banana", "Pineapple", "Lemon", "Orange",
                "Pastry dough", "Bread dough", "Pizza base",
                "Flour", "Cooked meat", "Cooked chicken", "Cooked rabbit",
                "Stripy feather", "Garlic", "Lobster pot", "Salt", "Spice",
                "Pat of butter", "Chocolate bar", "Chocolate dust",
                "Honeycomb", "Egg shell",
                # session 8 additions
                "Grain", "Redberries", "Cheese", "Grapes",
                "Chocolatey milk", "Empty cup",
                "Half full wine jug", "Jug of bad wine", "Unfermented wine",
                "Incomplete stew", "Uncooked stew", "Uncooked curry",
            })),
        ),
        Section("Misc cooked food", _or(
            _name_starts("Cooked "),
            _name_in({
                "Edible seaweed", "Giant carp", "Raw giant carp",
                "Ugthanki meat", "Uncooked cake",
                "Pitta dough", "Pitta bread",
                "Chopped tomato", "Chopped onion", "Chopped ugthanki",
                "Onion & tomato", "Ugthanki & onion", "Ugthanki & tomato",
                "Kebab mix", "Ugthanki kebab",
            }),
        ), force_exclude=list(_COOKED_FISH_HEAL.keys())),
        Section("Pies (extended)", _or(_name_ends(" pie"), _name_in({"Pie shell"}))),
        Section("Harvest produce (cross-tag with farming)", _or(
            _is_harvest_produce,
            _name_in({
                "Curry leaf", "Papaya fruit", "Coconut", "Half coconut",
                "Coconut shell", "Calquat fruit", "Watermelon",
                "Watermelon slice", "Mushroom", "Barley", "Barley malt",
            }),
        )),
        Section("Cooking pet & misc", _name_in({"Rocky", "Heron", "Beaver", "Pet hellpuppy", "Pet kitten"})),
    ],
)

# === WC + FLETCHING ===
def _is_wc_axe(it):
    """Woodcutting axe — weapon_type='axe' but exclude battleaxes/greataxes
    which osrsbox lumps under the same weapon_type."""
    if _wtype(it) != "axe" or _is_noise(it):
        return False
    if _slot(it) not in ("weapon", "2h"):
        return False
    n = (it.get("name") or "").lower()
    if any(k in n for k in ("battleaxe", "greataxe", "great axe")):
        return False
    return True


WC_FLETCHING = TabSpec(
    name="wc_fletching", const_name="TAG_WC_FLETCHING",
    sections=[
        Section("Axes", _is_wc_axe),
        Section("Logs", _is_log, sort_key=_log_sort_key),
        Section("Bowstrings", _name_in({"Bow string", "Crossbow string", "Magic string"})),
        Section("Unstrung bows", _or(
            _name_contains("(u)"),
            _name_in({"Unstrung comp bow", "Unstrung lyre"}),
        ), force_exclude=[]),
        Section("Bows & shortbows (strung)", _and(
            _is_range_weapon_type("bow"),
            _not(_name_contains("(u)")),
        )),
        Section("Crossbow parts", _name_in({
            "Wooden stock", "Oak stock", "Willow stock", "Teak stock",
            "Maple stock", "Mahogany stock", "Yew stock",
            "Bronze limbs", "Iron limbs", "Steel limbs", "Mithril limbs",
            "Adamantite limbs", "Runite limbs", "Dragon limbs",
        })),
        Section("Bolt tips", _or(
            _name_ends(" bolt tips"),
            _name_in({"Barb bolttips"}),  # historical no-space plural
        )),
        Section("Bolts (unfinished)", _name_ends(" bolts (unf)")),
        Section("Bolts (finished)", _and(_name_ends(" bolts"),
                                          _not(_name_contains("(unf)")))),
        Section("Arrow shafts", _or(
            _name_in({"Arrow shaft", "Headless arrow"}),
            _name_ends(" arrow shaft"),
        )),
        Section("Arrowtips", _name_ends(" arrowtips")),
        Section("Arrows", _and(_name_ends(" arrow", " arrows"),
                               _not(_name_contains("shaft"))),
                force_exclude=["Broken arrow"]),
        Section("Darts", _name_ends(" dart", " darts", " dart tip", " dart tips"),
                force_exclude=["Prototype dart", "Prototype dart tip"]),
        Section("Javelin parts", _name_ends(" javelin heads", " javelin shaft")),
        Section("Javelins (cross-tag with range)", _name_ends(" javelin")),
        Section("Bird nests", _or(_name_starts("Bird nest"), _name_starts("Bird's nest"))),
        Section("Feathers (cross-tag with fishing)", _name_in({
            "Feather", "Stripy feather", "Red feather", "Blue feather",
            "Yellow feather", "Orange feather",
        })),
        Section("Flax & secondary fletching materials", _name_in({
            "Flax", "Bow string", "Crossbow string", "Magic string",
            "Sinew",
        })),
        Section("Tools", _name_in({"Knife", "Hatchet", "Crystal hatchet", "Bruma torch"})),
        Section("Forestry items (post-Sept 2023)",
                lambda it: (
                    "forestry" in (it.get("name") or "").lower()
                    or "anima " in (it.get("name") or "").lower()  # "Anima " (Forestry items use Anima/anima as a prefix-word, not anywhere)
                    or (it.get("name") or "").startswith("Sturdy ")
                ),
                force_exclude=["Animal skull"]),
        Section("Capes & pet", _name_in({
            "Woodcutting cape", "Woodcutting cape(t)", "Woodcutting hood",
            "Fletching cape", "Fletching cape(t)", "Fletching hood",
            "Beaver", "Imcando hammer",
        })),
    ],
)

# === FISHING ===
FISHING = TabSpec(
    name="fishing", const_name="TAG_FISHING",
    sections=[
        Section("Rods & tools", _name_in({
            "Fishing rod", "Pearl fishing rod", "Fly fishing rod",
            "Pearl fly fishing rod", "Barbarian rod", "Oily fishing rod",
            "Big fishing net", "Small fishing net", "Lobster pot",
            "Harpoon", "Barb-tail harpoon", "Dragon harpoon",
            "Crystal harpoon", "Infernal harpoon", "Trailblazer harpoon",
            "Karambwan vessel", "Karambwanji bait",
        })),
        Section("Bait", _name_in({
            "Fishing bait", "Feather", "Dark fishing bait",
            "Stripy feather", "Red feather", "Yellow feather",
            "Blue feather", "Orange feather",
        })),
        Section("Raw fish", _is_raw_fish, sort_key=_raw_fish_sort_key),
        Section("Specialty fish", _name_in({
            "Sacred eel", "Infernal eel", "Cave eel", "Lava eel",
            "Slimy eel", "Frog spawn",
            # Bass removed — that's the cooked variant; raw bass is already in Raw fish.
        })),
        Section("Angler outfit", _name_starts("Angler ")),
        Section("Spirit angler outfit", _name_starts("Spirit angler ")),
        Section("Tempoross rewards", _name_in({
            "Spirit flakes", "Soaked page", "Tackle box", "Fish barrel",
            "Tome of water (empty)", "Tome of water",
        })),
        Section("Fishing potions (cross-tag)",
                _is_potion_family("fishing potion"),
                sort_key=_potion_sort_key),
        Section("Cape & pet", _name_in({"Fishing cape", "Fishing cape(t)", "Fishing hood", "Heron"})),
    ],
)

# === FIREMAKING ===
FIREMAKING = TabSpec(
    name="firemaking", const_name="TAG_FIREMAKING",
    sections=[
        Section("Tinderbox", _name_in({"Tinderbox", "Bruma torch"})),
        Section("Logs", _is_log, sort_key=_log_sort_key),
        Section("Firelighters", _name_ends(" firelighter")),
        Section("Lanterns", _name_in({
            "Bullseye lantern", "Bullseye lantern (unf)", "Bullseye lantern (empty)",
            "Mining helmet", "Oil lantern", "Empty oil lantern", "Oil lantern frame",
            "Sapphire lantern", "Emerald lantern", "Lit bug lantern",
            "Candle lantern", "Empty candle lantern",
            "Lantern lens", "Oil lamp", "Empty oil lamp",
            "Torch", "Unlit torch", "Lit torch",
        })),
        Section("Wintertodt", _name_in({
            "Bruma kindling", "Bruma root", "Burnt page",
            "Pyromancer hood", "Pyromancer garb",
            "Pyromancer robe", "Pyromancer boots",
            "Warm gloves", "Warm cloak",
            "Phoenix", "Phoenix pet",
        })),
        Section("Cape", _name_in({"Firemaking cape", "Firemaking cape(t)", "Firemaking hood"})),
    ],
)

# === CRAFTING ===
CRAFTING = TabSpec(
    name="crafting", const_name="TAG_CRAFTING",
    sections=[
        Section("Crafting tools", _name_in({
            "Chisel", "Needle", "Glassblowing pipe", "Hammer",
            "Spinning wheel", "Lyre", "Enchanted lyre", "Shears",
            "Ring mould", "Amulet mould", "Necklace mould",
            "Unholy mould", "Tiara mould", "Bracelet mould",
            "Sickle mould",
            "Bronze wire", "Bucket of sand", "Woad leaf",
        })),
        Section("Thread & dyes", _name_in({
            "Thread", "Wool", "Ball of wool",
            "Red dye", "Yellow dye", "Blue dye", "Green dye",
            "Orange dye", "Purple dye", "Pink dye",
        })),
        Section("Leather (raw → tanned)", _name_in({
            "Cowhide", "Leather", "Hard leather",
            "Snake hide", "Snakeskin", "Yak-hide", "Yak hide",
            "Soft leather", "Hardleather body",
            "Bear fur", "Grey wolf fur", "Silk",
        })),
        Section("D'hide", _or(
            _name_ends(" dragonhide"),
            _name_ends(" d'hide", " d'hide body", " d'hide chaps", " d'hide vambraces"),
        )),
        Section("Glass", _name_in({
            "Molten glass", "Empty light orb", "Light orb", "Empty fishbowl",
            "Empty vial", "Vial", "Vial of water", "Sand", "Soda ash",
            "Seaweed", "Giant seaweed",
        })),
        Section("Misc crafting materials", _name_in({
            "Charcoal", "Bones (Crafting)",
            "Raw swamp paste", "Swamp paste",
            "Bark", "Quicklime",
        })),
        Section("Pottery", _name_in({
            "Soft clay", "Clay", "Unfired bowl", "Unfired pie dish",
            "Unfired pot", "Unfired plant pot",
            "Pot", "Bowl", "Pie dish", "Plant pot",
        })),
        Section("Uncut gems", _or(
            _name_starts("Uncut "),
        )),
        Section("Cut gems", _name_in({
            "Opal", "Jade", "Red topaz", "Sapphire", "Emerald", "Ruby",
            "Diamond", "Dragonstone", "Onyx", "Zenyte",
            # bowstringing / jewellery secondaries
            "Oyster pearl", "Oyster pearls", "Pearl",
        })),
        Section("Jewellery (silver)", _and(
            _name_starts("Silver "),
            _or(_name_contains("amulet"), _name_contains("necklace"),
                _name_contains("bracelet"), _name_contains("ring")),
        )),
        Section("Jewellery (gold)", _and(
            _name_starts("Gold "),
            _or(_name_contains("amulet"), _name_contains("necklace"),
                _name_contains("bracelet"), _name_contains("ring")),
        )),
        Section("Jewellery (gem-set)", _and(
            _or(_name_ends(" ring"), _name_ends(" necklace"),
                _name_ends(" amulet"), _name_ends(" bracelet")),
            _or(_name_starts("Sapphire "), _name_starts("Emerald "),
                _name_starts("Ruby "), _name_starts("Diamond "),
                _name_starts("Dragonstone "), _name_starts("Onyx "),
                _name_starts("Zenyte "), _name_starts("Opal "),
                _name_starts("Jade "), _name_starts("Topaz "),
                _name_starts("Pearl "), _name_starts("Dragon "),
                _name_starts("Black ")),
        )),
        Section("Battlestaves", _and(_name_ends(" battlestaff"), _not(_name_contains("mystic")))),
        Section("Crafting cape & pet", _name_in({"Crafting cape", "Crafting cape(t)", "Crafting hood"})),
        Section("Wool/dyes/cloth (extended)", _name_in({
            "Wool", "Ball of wool", "Bolt of cloth",
            "Linum tirinum", "Spun flax",
        })),
        Section("Leathers (extended)",
                _or(_name_ends(" leather"), _name_ends(" hide"))),
        Section("Pottery & wood crafting outputs", _or(
            _name_starts("Unfired "),
            _name_in({"Pie shell", "Vial", "Vial of water", "Empty vial"}),
        )),
        Section("Spinning / weaving inputs", _name_in({
            "Spinning wheel", "Yarn",
        })),
    ],
)

# === MINING + SMITHING ===
MINING_SMITHING = TabSpec(
    name="mining_smithing", const_name="TAG_MINING_SMITHING",
    sections=[
        Section("Pickaxes", lambda it: _wtype(it) == "pickaxe" and not _is_noise(it)),
        Section("Mining tools & bags", _name_in({
            "Hammer", "Imcando hammer", "Chisel",
            "Coal bag", "Gem bag", "Open gem bag", "Ore pack",
            "Goldsmith gauntlets", "Ring of forging",
        })),
        Section("Ores", _is_ore, sort_key=_ore_sort_key),
        Section("Bars", _is_bar, sort_key=_bar_sort_key),
        Section("Smithing outputs", _name_in({
            "Bronze nails", "Iron nails", "Steel nails", "Black nails",
            "Mithril nails", "Adamantite nails", "Rune nails",
            "Cannonball", "Granite cannonball",
        })),
        Section("Mining outfit (Prospector)", _name_starts("Prospector ")),
        Section("Smithing outfit (Smiths')", _name_starts("Smiths' ")),
        Section("Mining/Smithing capes & pets", _name_in({
            "Mining cape", "Mining cape(t)", "Mining hood",
            "Smithing cape", "Smithing cape(t)", "Smithing hood",
            "Rock golem", "Pet smithing pet",
        })),
        Section("Crystal-tool/Tier-fallback pickaxes", _name_in({
            "Crystal pickaxe", "Crystal pickaxe (inactive)", "Crystal pickaxe full",
            "Dragon pickaxe (or)", "Dragon pickaxe (upgraded)",
            "3rd age pickaxe", "Infernal pickaxe", "Infernal pickaxe (uncharged)",
        })),
        Section("Smithing armour outputs (extended)", _or(
            _name_ends(" full helm"), _name_ends(" med helm"),
            _name_ends(" platebody"), _name_ends(" platelegs"),
            _name_ends(" plateskirt"), _name_ends(" chainbody"),
            _name_ends(" sq shield"), _name_ends(" kiteshield"),
        )),
        Section("Gem cutting/polishing inputs", _or(
            _name_starts("Uncut "),
            _name_in({"Chisel", "Crystal chisel"}),
        )),
    ],
)

# === HERBLORE ===
HERBLORE = TabSpec(
    name="herblore", const_name="TAG_HERBLORE",
    sections=[
        Section("Tools", _name_in({
            "Pestle and mortar", "Herb sack", "Open herb sack",
            "Alchemist's amulet", "Amulet of chemistry",
        })),
        Section("Grimy herbs", _is_grimy_herb, sort_key=_herb_sort_key),
        Section("Clean herbs", _is_clean_herb, sort_key=_herb_sort_key),
        Section("Unfinished potion variants (extended)", _name_in({
            "Weapon poison+ (unf)", "Weapon poison++ (unf)",
            "Antidote+ (unf)", "Antidote++ (unf)",
        })),
        Section("Vials & secondaries", _name_in({
            "Vial", "Vial of water", "Vial of blood", "Empty vial",
            "Eye of newt", "Limpwurt root", "Red spiders' eggs",
            "Chocolate dust", "Snape grass", "Mort myre fungus",
            "White berries", "Wine of zamorak", "Kebbit teeth dust",
            "Crushed nest", "Goat horn dust", "Cactus spine",
            "Jangerberries", "Potato cactus", "Crushed gem",
            "Lava scale shard", "Cave nightshade", "Poison ivy berries",
            "Caviar", "Dragon scale dust",
            # audit session 1 additions
            "Unicorn horn", "Unicorn horn dust",
            "Blue dragon scale", "Red dragon scale", "Green dragon scale",
            "Black dragon scale", "Mithril dragon scale",
            "Weapon poison", "Weapon poison(+)", "Weapon poison(++)",
            "Weapon poison (unf)", "Weapon poison(+) (unf)", "Weapon poison(++) (unf)",
            # audit session 11 additions
            "Mort myre stem", "Mort myre pear",
        })),
        Section("Unfinished potions", _name_ends(" potion (unf)")),
        Section("Attack potions", _is_potion_family("attack potion"), sort_key=_potion_sort_key),
        Section("Strength potions", _is_potion_family("strength potion"), sort_key=_potion_sort_key),
        Section("Defence potions", _is_potion_family("defence potion"), sort_key=_potion_sort_key),
        Section("Super attack/strength/defence",
                _is_potion_family("super attack", "super strength", "super defence"),
                sort_key=_potion_sort_key),
        Section("Super combat", _is_potion_family("super combat", "divine super"), sort_key=_potion_sort_key),
        Section("Ranging & magic", _is_potion_family("ranging potion", "magic potion", "divine ranging", "divine magic"), sort_key=_potion_sort_key),
        Section("Prayer & restores", _is_potion_family("prayer potion", "super restore", "restore potion", "sanfew", "saradomin brew"), sort_key=_potion_sort_key),
        Section("Antifire & anti-poison", _is_potion_family("antifire", "antipoison", "antidote", "anti-venom"), sort_key=_potion_sort_key),
        Section("Energy & stamina", _is_potion_family("energy potion", "super energy", "stamina potion"), sort_key=_potion_sort_key),
        Section("Other potions",
                _is_potion_family("zamorak brew", "guthix rest", "ancient brew", "forgotten brew", "battlemage", "bastion potion", "compost potion", "fishing potion", "hunter potion", "agility potion", "relicym's balm"),
                sort_key=_potion_sort_key),
        Section("Cape & pet", _name_in({
            "Herblore cape", "Herblore cape(t)", "Herblore hood", "Herbi",
        })),
    ],
)

# === AGILITY + THIEVING ===
def _is_blackjack(it):
    n = it.get("name", "")
    return "blackjack" in n.lower()


AGILITY_THIEVING = TabSpec(
    name="agility_thieving", const_name="TAG_AGILITY_THIEVING",
    sections=[
        Section("Marks & tickets", _name_in({
            "Mark of grace", "Agility arena ticket", "Brimhaven voucher",
        })),
        Section("Graceful set",
                _or(_name_starts("Graceful "), _name_starts("Graceful("))),
        Section("Agility shortcut tools", _name_in({
            "Mithril grapple", "Climbing boots", "Spiked climbing boots",
            "Crystal grappling hook", "Boots of lightness",
            "Rock-climbing boots", "Spiked boots",
        })),
        Section("Rogue equipment", _name_starts("Rogue ")),
        Section("Thieving accessories", _name_in({
            "Dodgy necklace", "Ardougne cloak 1", "Ardougne cloak 2",
            "Ardougne cloak 3", "Ardougne cloak 4",
            "Gloves of silence", "Coin pouch", "Coin pouch (cracked)",
            "Stethoscope", "Lockpick",
        })),
        Section("Blackjacks", _is_blackjack),
        Section("Pyramid plunder", _name_in({
            "Pharaoh's sceptre", "Pharaoh's sceptre (3)", "Pharaoh's sceptre (2)",
            "Pharaoh's sceptre (1)", "Black ibis mask", "Black ibis body",
            "Black ibis legs",
        })),
        Section("Capes & pets", _name_in({
            "Agility cape", "Agility cape(t)", "Agility hood",
            "Thieving cape", "Thieving cape(t)", "Thieving hood",
            "Giant squirrel", "Rocky",
        })),
        Section("Energy / stamina potions (cross-tag)",
                _is_potion_family("energy potion", "super energy", "stamina potion",
                                  "agility potion"),
                sort_key=_potion_sort_key),
    ],
)

# === SLAYER ===
SLAYER = TabSpec(
    name="slayer", const_name="TAG_SLAYER",
    sections=[
        Section("Slayer master items", _name_in({
            "Enchanted gem", "Eternal gem", "Slayer ring", "Slayer ring (eternal)",
            "Slayer's enchantment", "Mysterious emblem", "Abyssal book",
        })),
        Section("Slayer rings", _name_starts("Slayer ring")),
        Section("Slayer helmets", _name_contains("slayer helmet")),
        Section("Black masks", _name_contains("black mask")),
        Section("Task-specific gear", _name_in({
            "Rock hammer", "Rock thrownhammer", "Slayer's staff", "Bag of salt",
            "Slayer's staff (e)", "Brittle key", "Mirror shield",
            "Witchwood icon", "Earmuffs", "Spiny helmet", "Facemask",
            "Nose peg", "Fungicide spray 10", "Fungicide", "Magic secateurs",
            "Bonecrusher", "Bonecrusher necklace", "Smouldering stone",
            "Hydra leather", "Boots of stone", "Boots of brimstone",
            "Granite gloves", "Granite hammer", "Granite ring",
            "Granite ring (i)", "Granite maul",
        })),
        Section("Cannon", _name_in({
            "Cannon base", "Cannon stand", "Cannon barrels", "Cannon furnace",
            "Cannonball", "Granite cannonball", "Steel cannonball",
        })),
        Section("Cape & pet", _name_in({
            "Slayer cape", "Slayer cape(t)", "Slayer hood",
            "Abyssal orphan", "Kalphite princess",
        })),
    ],
    variant_allowlist=[
        "Slayer helmet (i)", "Black mask (i)",
        "Black slayer helmet", "Black slayer helmet (i)",
        "Green slayer helmet", "Green slayer helmet (i)",
        "Red slayer helmet", "Red slayer helmet (i)",
        "Purple slayer helmet", "Purple slayer helmet (i)",
        "Turquoise slayer helmet", "Turquoise slayer helmet (i)",
        "Hydra slayer helmet", "Hydra slayer helmet (i)",
        "Twisted slayer helmet", "Twisted slayer helmet (i)",
        "Tztok slayer helmet", "Tztok slayer helmet (i)",
        "Vampyric slayer helmet", "Vampyric slayer helmet (i)",
        "Tzkal slayer helmet", "Tzkal slayer helmet (i)",
    ],
)

# === FARMING ===
FARMING = TabSpec(
    name="farming", const_name="TAG_FARMING",
    sections=[
        Section("Tools", _name_in({
            "Rake", "Spade", "Seed dibber",
            "Gardening trowel", "Gardening boots",
            "Secateurs", "Magic secateurs", "Watering can",
            "Watering can(8)", "Watering can(7)", "Watering can(6)",
            "Watering can(5)", "Watering can(4)", "Watering can(3)",
            "Watering can(2)", "Watering can(1)",
            "Gricoller's can", "Plant pot", "Empty plant pot",
        })),
        Section("Compost", _name_in({
            "Compost", "Supercompost", "Ultracompost",
            "Compost potion(4)", "Compost potion(3)",
            "Compost potion(2)", "Compost potion(1)",
            "Bottomless compost bucket", "Plant cure",
        })),
        Section("Seeds", _is_seed),
        # Note: prior force_exclude for "Marigold seed" was removed in session 18
        # audit — marigold IS a real farming flower seed.
        Section("Harvest produce", _or(
            _is_harvest_produce,
            _name_in({"Basket", "Empty sack", "Filled plant pot"}),
        )),
        Section("Farm outputs / materials", _or(
            _name_in({
                "Jute fibre", "Willow branch",
                # Hops
                "Hammerstone hops", "Asgarnian hops", "Yanillian hops",
                "Krandorian hops", "Wildblood hops",
                # Crop outputs
                "Mushroom", "Barley", "Barley malt",
                "Marigolds", "Nasturtiums", "Rosemary",
                # Tree-grown fruit / palm fruits
                "Curry leaf", "Papaya fruit", "Coconut", "Half coconut",
                "Coconut shell", "Calquat fruit", "Watermelon", "Watermelon slice",
                # Tree drops
                "Leaves", "Oak leaves", "Willow leaves", "Yew leaves",
                "Maple leaves", "Magic leaves",
                "Oak roots", "Willow roots", "Maple roots", "Yew roots",
                "Magic roots", "Spirit roots",
                # Misc farming
                "Weeds", "Hay sack", "Scarecrow",
                "Amulet of nature", "Pre-nature amulet",
            }),
        )),
        Section("Saplings", _is_sapling),
        Section("Farmer outfit", _name_starts("Farmer's ")),
        Section("Cape & pet", _name_in({
            "Farming cape", "Farming cape(t)", "Farming hood", "Tangleroot",
        })),
    ],
)

# === RUNECRAFT ===
RUNECRAFT = TabSpec(
    name="runecraft", const_name="TAG_RUNECRAFT",
    sections=[
        Section("Talismans", _is_talisman),
        Section("Tiaras", _is_tiara),
        Section("Essence pouches", _name_in({
            "Small pouch", "Medium pouch", "Large pouch", "Giant pouch",
            "Colossal pouch", "Divine rune pouch", "Rune pouch",
        })),
        Section("RC accessories", _name_in({
            "Binding necklace",
        })),
        Section("Essence", _name_in(_ESSENCE)),
        Section("Basic runes", _name_in(_BASIC_RUNES)),
        Section("Combo runes", _name_in(_COMBO_RUNES)),
        Section("Raiments of the eye", _or(
            _name_starts("Hat of the eye"),
            _name_starts("Robe top of the eye"),
            _name_starts("Robe bottoms of the eye"),
            _name_starts("Boots of the eye"),
            _name_starts("Eye of the eye"),
        )),
        Section("Wicked", _name_starts("Wicked ")),
        Section("Cape & pet", _name_in({
            "Runecraft cape", "Runecraft cape(t)", "Runecraft hood", "Rift guardian",
        })),
    ],
)

# === HUNTER ===
HUNTER = TabSpec(
    name="hunter", const_name="TAG_HUNTER",
    sections=[
        Section("Traps", _name_in({
            "Box trap", "Bird snare", "Butterfly net", "Magic box",
            "Net trap", "Marasamaw plant", "Quetzal whistle",
            "Fox whistle", "Bone blowpipe",
        })),
        Section("Salamanders", _name_in({
            "Swamp lizard", "Orange salamander", "Red salamander",
            "Black salamander", "Tecu salamander",
            "Guam tar", "Marrentill tar", "Tarromin tar", "Harralander tar",
            "Swamp tar",
        })),
        Section("Bait", _name_in({
            "Raw chompy", "Raw bird meat", "Raw beast meat",
            "Raw rabbit", "Raw rat meat",
            "Worm", "Red vine worm",
            "Bird's egg",
        })),
        Section("Impling jars", _is_impling, sort_key=_impling_sort_key),
        Section("Polar camo", _name_in({"Polar camo top", "Polar camo legs"})),
        Section("Woodland camo", _name_in({"Woodland camo top", "Woodland camo legs"})),
        Section("Desert camo", _name_in({"Desert camo top", "Desert camo legs"})),
        Section("Jungle camo", _name_in({"Jungle camo top", "Jungle camo legs"})),
        Section("Larupia hunter", _name_starts("Larupia ")),
        Section("Graahk hunter", _name_starts("Graahk ")),
        Section("Kyatt hunter", _name_starts("Kyatt ")),
        Section("Spotted/spottier", _name_in({
            "Spotted cape", "Spottier cape", "Gloves of silence",
        })),
        Section("Cape & pet", _name_in({
            "Hunter cape", "Hunter cape(t)", "Hunter hood",
            "Baby chinchompa", "Beaver",  # beaver is wc but harmless if listed
        })),
    ],
)

# === CONSTRUCTION ===
CONSTRUCTION = TabSpec(
    name="construction", const_name="TAG_CONSTRUCTION",
    sections=[
        Section("Tools", _name_in({
            "Saw", "Crystal saw", "Hammer", "Imcando hammer",
        })),
        Section("Planks", _is_plank, sort_key=_plank_sort_key),
        Section("Nails", _is_nails, sort_key=_nails_sort_key),
        Section("Construction materials", _name_in({
            "Bolt of cloth", "Limestone", "Limestone brick", "Soft clay",
            "Steel bar", "Marble block", "Magic stone", "Gold leaf",
            "Mahogany table", "Servant's moneybag",
            "Clockwork", "Empty oak cape rack",
            "Fine cloth", "Flamtaer hammer",
        })),
        Section("POH portals & telescopes",
                _or(_name_starts("Teleport to "),
                    _name_in({"House teleport", "Construct. cape teleport"}))),
        Section("Bench/altar (pattern)",
                lambda it: any(k in (it.get("name") or "").lower() for k in (
                    "oak", "mahogany", "teak"
                )) and any(k in (it.get("name") or "").lower() for k in (
                    "chair", "table", "bench", "bed", "bookcase",
                    "fireplace", "wardrobe", "drawers", "cape rack", "armour case",
                ))),
        Section("Mahogany Homes", _name_in({
            "Carpenter's helmet", "Carpenter's shirt",
            "Carpenter's trousers", "Carpenter's boots",
            "Plank sack", "Amy's saw", "Hosidius blueprints",
        })),
        Section("POH teleports", _name_in({
            "Teleport to house", "Construct. cape teleport",
            "Achievement diary cape", "Falador shield 1",
        })),
        Section("Cape", _name_in({
            "Construction cape", "Construction cape(t)", "Construction hood",
        })),
    ],
)

# === MISC ===
MISC = TabSpec(
    name="misc", const_name="TAG_MISC",
    sections=[
        Section("Teleport jewellery", _name_in({
            "Amulet of glory", "Amulet of glory(t)",
            "Amulet of glory(1)", "Amulet of glory(2)",
            "Amulet of glory(3)", "Amulet of glory(4)", "Amulet of glory(6)",
            "Ring of wealth", "Ring of wealth (i)",
            "Ring of wealth (1)", "Ring of wealth (2)",
            "Ring of wealth (3)", "Ring of wealth (4)", "Ring of wealth (5)",
            "Games necklace(8)", "Games necklace(7)", "Games necklace(6)",
            "Games necklace(5)", "Games necklace(4)", "Games necklace(3)",
            "Games necklace(2)", "Games necklace(1)",
            "Combat bracelet", "Combat bracelet(1)", "Combat bracelet(2)",
            "Combat bracelet(3)", "Combat bracelet(4)", "Combat bracelet(5)",
            "Skills necklace", "Skills necklace(1)", "Skills necklace(2)",
            "Skills necklace(3)", "Skills necklace(4)", "Skills necklace(5)",
            "Digsite pendant (1)", "Digsite pendant (2)", "Digsite pendant (3)",
            "Digsite pendant (4)", "Digsite pendant (5)",
            "Necklace of passage(5)", "Necklace of passage(4)",
            "Necklace of passage(3)", "Necklace of passage(2)",
            "Necklace of passage(1)",
            "Burning amulet(5)", "Burning amulet(4)", "Burning amulet(3)",
            "Burning amulet(2)", "Burning amulet(1)",
            "Xeric's talisman",
            # Duel arena + utility rings
            "Ring of dueling(8)", "Ring of dueling(7)", "Ring of dueling(6)",
            "Ring of dueling(5)", "Ring of dueling(4)", "Ring of dueling(3)",
            "Ring of dueling(2)", "Ring of dueling(1)",
            "Ring of recoil", "Ring of life",
        })),
        Section("Teleport tabs", _or(
            _and(_name_ends(" teleport"), _not(_name_contains("scroll"))),
            _name_starts("Teleport to "),
        )),
        Section("Boss & quest jewellery", _name_in({
            "Ring of the gods", "Ring of the gods (i)",
            "Ring of suffering", "Ring of suffering (i)",
            "Ring of suffering (r)", "Ring of suffering (ri)",
            "Phoenix necklace", "Amulet of magic", "Amulet of strength",
            "Amulet of defence", "Amulet of accuracy",
            "Ghostspeak amulet",
        })),
        Section("Cosmetic outfits / random events", _name_in(
            set(_MIME_OUTFIT) | set(_CAMO_OUTFIT) | set(_REAL_MIME_OUTFIT)
            | {"Chompy bird hat", "Firework"}
        )),
        Section("Clue scrolls",
                _or(_name_starts("Clue scroll"), _name_starts("Master clue"))),
        Section("Clue tools", _or(
            _name_in({
                "Sextant", "Watch", "Chart", "Mimic kill count",
                "Puzzle box", "Light box", "Beacon ring",
            }),
            # Treasure-trail colour-coded keys
            _and(
                _or(_name_starts("Bronze key "), _name_starts("Steel key "),
                    _name_starts("Black key "), _name_starts("Silver key ")),
                _or(_name_ends(" red"), _name_ends(" brown"),
                    _name_ends(" crimson"), _name_ends(" black"),
                    _name_ends(" purple")),
            ),
        )),
        Section("Keys", _name_in({
            "Crystal key", "Loop half of key", "Tooth half of key",
            "Muddy key", "Mossy key", "Brimstone key", "Larran's key",
            "Ecumenical key", "Ancient shard", "Hill giant club",
            "Sinister key", "Brittle key",
        })),
        Section("Storage bags", _name_in({
            "Looting bag", "Open looting bag",
            "Gem bag", "Open gem bag",
            "Coal bag", "Open coal bag",
            "Herb sack", "Open herb sack",
            "Plank sack", "Plant pot",
            "Fish barrel", "Seed box", "Forester's bag",
        })),
        Section("Utility / banked supplies", _name_in({
            "Rope", "Chronicle",
        })),
        Section("Holiday rares & cosmetics", _or(
            _name_in(set(_HOLIDAY_RARES) | {
                # Easter event
                "Blue sweets", "Deep blue sweets", "White sweets",
                "Purple sweets", "Red sweets", "Green sweets", "Pink sweets",
                "Easter basket", "Rubber chicken",
            }),
            _name_ends(" sweets"),
        )),
        Section("Team capes (Castle Wars)",
                lambda it: (it.get("name") or "").startswith("Team-") and
                           (it.get("name") or "").endswith(" cape")),
        Section("Currency", _name_in({
            "Coins", "Platinum token", "Blood money", "Tokkul",
            "Marks of grace", "Brimhaven voucher", "Archery ticket",
            "Ecto-token",
        })),
    ],
)

# === QUESTS ===
def _is_quest_cape(it):
    n = it.get("name", "")
    return n in {"Quest point cape", "Quest point cape(t)", "Quest point hood",
                 "Music cape", "Music cape(t)", "Music hood",
                 "Max cape", "Max hood", "Completionist cape",
                 "Achievement diary cape", "Achievement diary cape(t)",
                 "Achievement diary hood"}


QUESTS = TabSpec(
    name="quests", const_name="TAG_QUESTS",
    sections=[
        Section("Quest & achievement capes", _is_quest_cape),
        Section("Diary - Kandarin", _name_starts("Kandarin headgear")),
        Section("Diary - Karamja", _name_starts("Karamja gloves")),
        Section("Diary - Ardougne", _name_starts("Ardougne cloak")),
        Section("Diary - Falador", _name_starts("Falador shield")),
        Section("Diary - Fremennik", _name_starts("Fremennik sea boots")),
        Section("Diary - Wilderness", _name_starts("Wilderness sword")),
        Section("Diary - Morytania", _name_starts("Morytania legs")),
        Section("Diary - Desert", _name_starts("Desert amulet")),
        Section("Diary - Varrock", _name_starts("Varrock armour")),
        Section("Diary - Western", _name_starts("Western banner")),
        Section("Diary consumables", _name_starts("Rada's blessing")),
        Section("Quest unlock weapons", _name_in({
            "Dramen staff", "Lunar staff", "Ivandis flail", "Blisterwood flail",
            "Slayer's staff", "Silverlight", "Excalibur", "Enhanced excalibur",
            "Wolfbane", "Keris", "Keris partisan", "Ectophial",
        })),
        Section("Quest cosmetic gear", _name_in({
            "Khazard helmet", "Khazard armour",
            "Fishing trophy",
            "Cattleprod",
            "Giant carp",
            "Gnome amulet", "Beads of the dead",
            "Dark dagger", "Glowing dagger",
            "Shade robe", "Shade robe top",
            "Cape of legends",
            "Carnillean armour",
            "Bearhead",
            "Mourner top", "Mourner trousers", "Mourner gloves",
            "Mourner boots", "Mourner cloak",
        })),
        Section("Void Knight set", _name_starts("Void ")),
        Section("Fighter Torso et al.", _name_in({
            "Fighter torso", "Fighter hat", "Runner hat", "Healer hat",
            "Penance gloves", "Penance skirt", "Penance shield",
        })),
        Section("Defenders", _name_ends(" defender")),
        Section("Boss pets", _name_in({
            "Tzrek-jad", "Olmlet", "Skotos", "Vorki", "Lil' creator",
            "Bloodhound", "Smolcano", "Lil' zik", "Jal-nib-rek",
        })),
        # NOTE: an "Other quest items" catch-all (quest_item=True) was tried and
        # matched ~1800 items, which is OSRS's entire quest catalogue. Far too
        # broad. Keep this tab to specific named items above; user adds more by
        # extending the diary / unlock sections.
    ],
)

# === SAILING ===
# Sailing launched 19 November 2025. With live wiki scraping (Phase 1.5),
# Sailing items now come straight from the wiki — no hardcoded ID injection
# needed. Classifier matches by release_date being post-Sailing-launch AND
# the item belonging to Sailing-themed name patterns (avoids sweeping in
# unrelated 2025/2026 releases like cosmetic capes).

def _is_sailing_release(it):
    rd = (it.get("release_date") or "")
    if not rd:
        return False
    # Wiki returns dates like "19 November 2025" or "2025-11-19".
    is_post = any(year in rd for year in ("2025", "2026"))
    if not is_post:
        return False
    n = (it.get("name") or "").lower()
    return any(t in n for t in (
        "sail", "krill", "yellowfin", "halibut", "bluefin", "marlin",
        "spyglass", "captain's log", "repair kit", "trawl", "salvag",
    ))


def _is_sailing_fish(raw_or_cooked):
    fishes = {"giant krill", "yellowfin", "halibut", "bluefin", "marlin"}
    def pred(it):
        n = (it.get("name") or "").lower()
        if raw_or_cooked == "raw" and not n.startswith("raw "):
            return False
        if raw_or_cooked == "cooked" and (n.startswith("raw ") or "uncooked" in n):
            return False
        if "2025" not in (it.get("release_date") or "") and "2026" not in (it.get("release_date") or ""):
            return False
        return any(f in n for f in fishes)
    return pred


SAILING = TabSpec(
    name="sailing", const_name="TAG_SAILING",
    sections=[
        Section("Navigation tools", lambda it: _is_sailing_release(it) and any(
            t in (it.get("name") or "").lower()
            for t in ("spyglass", "captain's log", "repair kit", "crowbar", "trawl", "salvag")
        )),
        Section("Raw sailing fish", _is_sailing_fish("raw")),
        Section("Cooked sailing fish", _is_sailing_fish("cooked")),
        Section("Cape & pet", lambda it: _is_sailing_release(it) and (
            "sailing cape" in (it.get("name") or "").lower()
            or "sailing hood" in (it.get("name") or "").lower()
        )),
    ],
    # extra_items intentionally empty — wiki provides these IDs directly now.
)


TABS: list[TabSpec] = [
    MELEE, RANGE, MAGE, PRAYER, COOKING, WC_FLETCHING, FISHING, FIREMAKING,
    CRAFTING, MINING_SMITHING, HERBLORE, AGILITY_THIEVING, SLAYER, FARMING,
    RUNECRAFT, HUNTER, CONSTRUCTION, MISC, QUESTS, SAILING,
]
