package com.skillbank;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * Static lookup tables for the runtime layout builder:
 *  - hand-authored display orders (herbs, potions, food, runes, gems, weapon classes)
 *  - section structures per tab
 *  - item metadata loaded lazily from the bundled item-meta.json resource
 * <p>
 * No alphabetical fallbacks. If a section doesn't have a deliberate sort
 * order defined here, that's a bug to fix in this file or in
 * tools/skillbank-data/llm_promote.py — not silently sort by name.
 */
@Slf4j
public final class SkillBankSortData
{
	private SkillBankSortData()
	{
	}

	// ── Hand-authored display orders ───────────────────────────────────────

	/** Standard herb tier order, lowest-level to highest. Within each name,
	 *  Grimy sorts before Clean.
	 *  <p>
	 *  Brief #88: intentionally low-tier-FIRST (i.e. weakest-left). Combat
	 *  tabs across the plugin standardise on strongest-left, but herblore
	 *  is a training row — players reach for guam / marrentill far more
	 *  often than torstol while levelling, so the low end stays closest to
	 *  the tab rail. The discrepancy with combat tabs is by design, not a
	 *  missed flip. */
	public static final List<String> HERB_ORDER = List.of(
		"Guam", "Marrentill", "Tarromin", "Harralander", "Ranarr",
		"Toadflax", "Irit", "Avantoe", "Kwuarm", "Snapdragon",
		"Cadantine", "Lantadyme", "Dwarf weed", "Torstol", "Huasca"
	);

	/** Potion display order: combat-meta potions, restores, debuff cures,
	 *  utility, lower-tier basics. Within each family, dose 4 → 1. */
	public static final List<String> POTION_ORDER = List.of(
		"Super combat potion", "Divine super combat potion",
		"Super attack", "Super strength", "Super defence",
		"Ranging potion", "Divine ranging potion",
		"Magic potion", "Divine magic potion",
		"Bastion potion", "Divine bastion potion",
		"Battlemage potion", "Divine battlemage potion",
		"Saradomin brew", "Super restore", "Prayer potion",
		"Sanfew serum", "Prayer mix", "Prayer renewal",
		"Antifire potion", "Extended antifire",
		"Super antifire potion", "Extended super antifire",
		"Anti-venom+", "Anti-venom",
		"Antidote++", "Antidote+",
		// Weapon poisons — most specific first: potionFamilyRank matches by
		// contains + lowest index, so "(++)" must precede "(+)" and plain.
		"Weapon poison(++)", "Weapon poison(+)", "Weapon poison",
		"Antipoison", "Superantipoison",
		"Stamina potion", "Energy potion", "Super energy",
		"Agility potion", "Fishing potion",
		"Combat potion", "Attack potion", "Strength potion", "Defence potion",
		"Zamorak brew", "Guthix rest", "Magic essence", "Summoning potion",
		"Compost potion", "Hunter potion", "Relicym's balm"
	);

	/** Cooked food display order — highest heal / mainstream-PVM first,
	 *  then everything else by tier desc. */
	public static final List<String> FOOD_ORDER = List.of(
		"Anglerfish", "Cooked karambwan", "Dark crab", "Shark",
		"Manta ray", "Sea turtle", "Monkfish", "Tuna potato",
		"Swordfish", "Lobster", "Bass",
		"Salmon", "Tuna", "Trout", "Pike", "Cod", "Mackerel",
		"Herring", "Sardine", "Anchovies", "Shrimps",
		// Combo / utility
		"Saradomin brew", "Pineapple pizza", "Anchovy pizza", "Plain pizza",
		"Meat pizza",
		"Summer pie", "Wild pie", "Fish pie", "Garden pie", "Apple pie",
		"Meat pie", "Redberry pie",
		"Curry", "Stew", "Pineapple punch",
		"Chocolate cake", "Cake", "Sliced bread",
		"Cooked chicken", "Cooked meat",
		// Misc
		"Dragonfruit", "Papaya fruit", "Pineapple", "Banana",
		"Apple", "Orange"
	);

	/** Brief #90: cooking-tab family rows sort by Cooking level, lowest
	 *  first — the training-tab weakest-left convention (same reasoning as
	 *  HERB_ORDER / GEM_ORDER). Entries are base-name tokens: the comparator
	 *  takes the LONGEST list entry contained in the item name, so generic
	 *  tokens ("eel", "fish", "meat") only catch items no compound token
	 *  ("cave eel", "rainbow fish", "bird meat") already claimed. Fish
	 *  first, then meats; one shared list is safe because a section only
	 *  ever contains one family. */
	public static final List<String> COOKING_LEVEL_ORDER = List.of(
		// Fish by Cooking level: shrimps(1) … manta ray(91).
		"shrimp", "karambwanji", "sardine", "anchovies", "herring",
		"mackerel", "trout", "cod", "pike", "salmon", "slimy eel",
		"tuna", "karambwan", "rainbow fish", "cave eel", "lobster",
		"bass", "swordfish", "lava eel", "eel", "monkfish", "shark",
		"sea turtle", "anglerfish", "dark crab", "manta ray", "fish",
		// Meats by Cooking level: chicken/beef(1) … oomlie(50).
		"chicken", "meat", "rabbit", "bat", "spider", "snail",
		"bird meat", "crab meat", "kebbit", "fox meat", "antelope",
		"large beast", "chompy", "jubbly", "oomlie"
	);

	/** Canonical rune-pouch order (Air → Mind → … → Astral → Wrath).
	 *  <p>
	 *  Brief #88: intentionally low-tier-FIRST. This matches the in-game
	 *  rune pouch UI and the layout players already have memorised. Air
	 *  / Mind / Water / Earth / Fire stay on the left because those are
	 *  the runes runecrafting trainees handle by the thousand — the high-
	 *  end Wrath / Soul / Blood are the rare ones. Combat-tab strongest-
	 *  left convention does not apply here. */
	public static final List<String> RUNE_ORDER = List.of(
		"Air", "Mind", "Water", "Earth", "Fire", "Body",
		"Cosmic", "Chaos", "Nature", "Law", "Death",
		"Blood", "Soul", "Astral", "Wrath"
	);

	/** Combination runes — appear after core runes. */
	public static final List<String> COMBO_RUNE_ORDER = List.of(
		"Mist", "Dust", "Mud", "Smoke", "Steam", "Lava"
	);

	/** Brief #76: gem display order — primary gems first by tier ascending
	 *  (uncut paired with cut per gem), then secondary gems same pattern.
	 *  Used by SkillBankLayoutBuilder.compareGems to produce
	 *    Uncut sapphire, Sapphire, Uncut emerald, Emerald, ..., Uncut zenyte,
	 *    Zenyte, Uncut opal, Opal, Uncut jade, Jade, Uncut red topaz, Red topaz.
	 *  <p>
	 *  Brief #88: intentionally low-tier-FIRST. Crafting is a training tab
	 *  — players cut bulk sapphires / emeralds far more than they touch
	 *  dragonstone / onyx / zenyte. Combat-tab strongest-left convention
	 *  does not apply here; the gem rows stay sapphire-led on purpose. */
	public static final List<String> GEM_ORDER = List.of(
		"Uncut sapphire", "Sapphire",
		"Uncut emerald",  "Emerald",
		"Uncut ruby",     "Ruby",
		"Uncut diamond",  "Diamond",
		"Uncut dragonstone", "Dragonstone",
		"Uncut onyx",     "Onyx",
		"Uncut zenyte",   "Zenyte",
		"Uncut opal",     "Opal",
		"Uncut jade",     "Jade",
		"Uncut red topaz", "Red topaz"
	);

	/** Weapon-class grouping within the Weapons section: stab → slash → crush → special. */
	public static final List<String> WEAPON_CLASS_ORDER = List.of(
		// Stab
		"stab_sword", "dagger", "spear", "hasta", "rapier",
		// Slash
		"slash_sword", "scimitar", "longsword", "2h_sword", "claw", "claws",
		"halberd", "axe", "battleaxe",
		// Crush
		"mace", "warhammer", "hammer", "flail", "bludgeon",
		// Special / unique
		"whip", "polearm",
		// Range
		"bow", "shortbow", "longbow", "crossbow", "thrown", "throwing",
		"blaster", "blowpipe", "chinchompa",
		// Mage
		"staff", "wand", "powered_staff"
	);
	private static final Map<String, Integer> WEAPON_CLASS_INDEX = buildIndex(WEAPON_CLASS_ORDER);

	private static Map<String, Integer> buildIndex(List<String> order)
	{
		Map<String, Integer> m = new HashMap<>();
		for (int i = 0; i < order.size(); i++)
		{
			m.put(order.get(i).toLowerCase(), i);
		}
		return m;
	}

	public static int weaponClassRank(String weaponClass)
	{
		if (weaponClass == null)
		{
			return Integer.MAX_VALUE;
		}
		Integer r = WEAPON_CLASS_INDEX.get(weaponClass.toLowerCase());
		return r != null ? r : Integer.MAX_VALUE - 1;
	}

	/** Brief #68: universal head-to-toe slot ordering for any section that
	 *  contains wearable items. Used as the primary sort term across all
	 *  outfit sections (Agility graceful, Hunter camos, Prospector, rogue
	 *  set, prayer robes, etc.) so a player browsing the section always
	 *  sees gear in head → body → legs → hands → feet → cape → neck → ring
	 *  → weapon → shield → ammo order. Non-wearables (slot=null) bucket at
	 *  the end of the section. */
	private static final Map<String, Integer> SLOT_ORDER = Map.ofEntries(
		Map.entry("head", 10),
		Map.entry("body", 20),
		Map.entry("legs", 30),
		Map.entry("hands", 40),
		Map.entry("feet", 50),
		Map.entry("cape", 60),
		Map.entry("neck", 70),
		Map.entry("ring", 80),
		Map.entry("weapon", 90),
		Map.entry("2h", 90),
		Map.entry("shield", 95),
		Map.entry("ammo", 100)
	);

	public static int slotRank(String slot)
	{
		if (slot == null)
		{
			return 999;
		}
		Integer r = SLOT_ORDER.get(slot.toLowerCase());
		return r != null ? r : 999;
	}

	/** Back-compat shim for the Brief #66 simple-mode Armor sort. The 5-slot
	 *  rank map it used is a subset of {@link #SLOT_ORDER}, so the universal
	 *  table works identically for that call site. */
	public static int armorSlotRank(String slot)
	{
		return slotRank(slot);
	}

	/** Section names that collapse into "Armor" under simple-mode combat. */
	public static final Set<String> SIMPLE_ARMOR_SECTIONS = Set.of(
		"Head", "Body", "Legs", "Hands", "Feet"
	);

	// ── Per-tab section structures (display order matters) ────────────────

	public static final Map<String, List<String>> TAB_SECTIONS;
	static
	{
		Map<String, List<String>> m = new LinkedHashMap<>();
		// Brief #78: Food section added after Rings + before Training & utility
		// in each combat tab. ALWAYS_ZONE1 routes cooked food to the loadout zone.
		// Section audit: "Ammunition" renamed — its members are ammo-SLOT
		// worn items (blessings, Ghommal's lucky penny), not ammunition.
		m.put("melee", List.of(
			"Weapons", "Shields & defenders", "Head", "Body", "Legs",
			"Hands", "Feet", "Capes", "Neck", "Rings", "Ammo slot",
			"Food",
			"Potions",
			"Training & utility"
		));
		// Brief #87: range weapon section split into nine fixed
		// launcher→ammo rows. Each is added to ALWAYS_ZONE1_SECTIONS so
		// the dynamic top-N partition doesn't fire on them.
		m.put("range", List.of(
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
			"Training & utility"
		));
		// Brief #74: Runes back on top of the mage tab (reverts Brief #66's swap).
		// Section audit: "Teleport tablets & spell utility" renamed — the
		// tablets live in the teleports tab; rune packs / orbs / hearts
		// and other cast supplies are what remain.
		m.put("mage", List.of(
			"Runes", "Weapons", "Off-hands, books & tomes", "Head", "Body",
			"Legs", "Hands", "Feet", "Capes", "Neck", "Rings",
			"Spell utility & supplies",
			"Food",
			"Potions",
			"Enchanting & skilling magic"
		));
		// Brief #66: simple-mode combat tab variants. The layout builder
		// picks these keys when the per-tab simple-mode config flag is on.
		m.put("melee_simple", List.of(
			"Weapons", "Shields & defenders", "Armor", "Capes", "Neck",
			"Rings", "Ammo slot", "Food", "Potions", "Training & utility"
		));
		m.put("range_simple", List.of(
			"Weapons", "Ammunition", "Armor", "Capes",
			"Shields & off-hands", "Neck", "Rings", "Food", "Potions",
			"Training & utility"
		));
		m.put("mage_simple", List.of(
			"Runes", "Weapons", "Off-hands, books & tomes", "Armor",
			"Capes", "Neck", "Rings",
			"Spell utility & supplies", "Food", "Potions",
			"Enchanting & skilling magic"
		));
		m.put("prayer", List.of(
			// Brief #60: equipment leads the tab now.
			// Tab audit: bonemeal/offerings, fossils and pyre/shade rows
			// split out of the old fallback. Keep in sync with the Python
			// TAB_SECTIONS in tools/skillbank-data/sort_tables.py.
			"Prayer equipment & robes", "Bones & ashes", "Ensouled heads",
			"Bonemeal & offerings", "Fossils & enriched bones",
			"Pyre logs & shade remains",
			"Prayer-restoring consumables", "Holy symbols, books & blessings",
			"Bone-processing utility"
		));
		// Brief #90: level-ordered family rows — fish raw → cooked → burnt,
		// meat raw → cooked → burnt, then ingredients, composites, tools,
		// and leftover burnt composites at the end.
		m.put("cooking", List.of(
			"Raw fish", "Cooked fish", "Burnt fish",
			"Raw meat", "Cooked meat", "Burnt meat",
			"Ingredients",
			"Combo food", "Baked & cooked goods",
			"Drinks & brews",
			"Cooking tools & utensils",
			"Burnt food"
		));
		// Brief #63: wc_fletching split into a combined Woodcutting + Firemaking
		// tab and a standalone Fletching tab. Display names live in the
		// Python TAB_DISPLAY_NAMES map; tag IDs stay snake_case so existing
		// banktags config keys keep parsing.
		// Brief #66: tools first, outfits second, materials after.
		// Tab audit: machetes join axes; light sources get their own row.
		m.put("woodcutting_firemaking", List.of(
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
			"Misc utility"
		));
		// Brief #75 (revised): materials-first with per-type sections.
		// Tools / Logs / Feathers / Arrows / Arrowheads / Bows / Crossbows
		// / Bolts / Darts / Javelins / Misc fletching.
		m.put("fletching", List.of(
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
			"Misc fletching"
		));
		// Tab audit: trophy fish row added; Raw fish now catches all
		// catchables (aerial / Camdozaal / CoX / Sailing).
		m.put("fishing", List.of(
			"Fishing tools", "Bait & consumables", "Fishing outfit",
			"Raw fish", "Trophies & big catches", "Fishing minigame items"
		));
		// Brief #64: standalone firemaking tab removed. Its content lives
		// in "woodcutting_firemaking" (Woodcutting + Firemaking).
		// Brief #76: moulds split out from tools; new top of tab.
		// Tab audit: Battlestaves & orbs + Monster parts & shells rows added.
		m.put("crafting", List.of(
			"Moulds", "Crafting tools", "Gems", "Hides & leather",
			"Spinning materials", "Glassmaking", "Battlestaves & orbs",
			"Pottery & clay", "Jewellery materials", "Crafted jewellery",
			"Crafted armour & leather goods", "Monster parts & shells",
			"Crafting outfit & utility"
		));
		// Tab audit: minigame minerals, Shayzien supply armour and smithed
		// parts (limbs / unf bolts / keel parts / visages) rows added.
		m.put("mining_smithing", List.of(
			"Pickaxes", "Mining outfit & utility", "Ores",
			"Special ores & minerals", "Bars",
			"Smithing tools", "Smithing outfit & gloves",
			"Shayzien supply armour", "Smithed parts & components",
			"Cannonballs & ammo outputs", "Giants' Foundry & minigame items"
		));
		// Brief #76: flat 5-section layout. Barbarian mixes + Divine variants
		// roll into Finished potions; outfit + utility folds into Tools.
		// Tab audit: outfit/containers split out of Tools; Mastering
		// Mixology closes the tab.
		m.put("herblore", List.of(
			"Tools", "Herblore outfit & utility", "Herbs", "Secondaries",
			"Unfinished potions", "Finished potions",
			"Mastering Mixology items"
		));
		// Section audit: marks row renamed — Hallowed Sepulchre reward gear
		// dominates it.
		m.put("agility_thieving", List.of(
			"Agility outfit & graceful", "Run-energy consumables",
			"Agility marks & rewards", "Thieving outfit & rogue set",
			"Thieving tools", "Thieving loot & artefacts"
		));
		// Section audit: "Mandatory protection" renamed (task finishers +
		// Shayzien anti-shaman armour join it); heads/trophies and boss
		// drops promoted out of the Misc utility fallback.
		m.put("slayer", List.of(
			"Slayer assignment items", "Mandatory task items",
			"Core slayer gear", "Cannon & burst supplies",
			"Combat potions", "Prayer & restores", "Food",
			"Teleports", "Loot management",
			"Monster heads & trophies", "Boss drops & upgrade parts",
			"Misc utility"
		));
		// Tab audit: Harvested produce row added before the fallback.
		m.put("farming", List.of(
			"Farming tools", "Compost & soil treatment",
			"Allotment seeds", "Hops seeds", "Flower seeds",
			"Herb seeds", "Bush seeds", "Tree seeds",
			"Fruit tree seeds", "Special seeds", "Saplings",
			"Harvested produce",
			"Farmer outfit & contracts"
		));
		// Section audit: outfit row renamed — bloodbark/swampbark armour,
		// runescrolls and binding jewellery outnumber the outfit pieces.
		m.put("runecraft", List.of(
			"Essence", "Pouches & storage", "Talismans", "Tiaras",
			"Core runes", "Combination runes", "Runecraft gear & utility",
			"Guardians of the Rift items"
		));
		// Brief #76: Creature products split into furs / meats / tertiaries.
		// Tab audit: weapons/ammo, baits & potions, salamanders and
		// butterflies/moths rows added.
		m.put("hunter", List.of(
			"Hunter tools & traps", "Hunter weapons & ammo",
			"Nets, jars & containers", "Hunter outfit",
			"Baits & potions",
			"Chinchompas", "Salamanders & lizards", "Butterflies & moths",
			"Furs & hides", "Hunter meats", "Hunter tertiaries",
			"Implings & impling jars", "Birdhouse items"
		));
		// Tab audit: flatpacks, garden plants and trophy decor rows added.
		// Section audit: "Teleport-to-house items" deleted (empty — house
		// tablets live in the teleports tab); contract row renamed.
		m.put("construction", List.of(
			"Construction tools", "Planks", "Nails",
			"Building materials",
			"Flatpacks & furniture", "Garden & bagged plants",
			"Mounted heads & decor",
			"Blueprints & contracts",
			"Construction outfit & rewards"
		));
		// Misc audit: new sections derived from the old Uncategorized clump
		// (keys, Leagues sigils, XP lamps, fossils, books, minigame/PvM
		// rewards, consumables). Must stay in sync with the Python
		// TAB_SECTIONS in tools/skillbank-data/sort_tables.py.
		m.put("misc", List.of(
			"Currency & exchange tokens", "General teleports",
			"Jewellery teleports", "Utility containers",
			"Clue scroll items", "Keys & access",
			"Books & documents", "Lamps & XP rewards",
			"Sigils & trinkets", "Fossils & museum",
			// Section audit: renamed — the " & " display cut left only
			// "Minigame" on the divider.
			"Minigame rewards", "Consumables & supplies",
			"General tools", "Uncategorized"
		));
		// Quests row redesign: equipment by slot, the rest by name family;
		// unique quest junk holds the tab-closing fallback.
		// Section audit: "Diary rewards" deleted (empty), "Boss pets &
		// followers" deleted (lone Pet rock → Quest items); "Reward"
		// prefixes dropped — disguises and wieldable props dominate.
		m.put("quests", List.of(
			"Achievement capes",
			"Weapons & wieldables", "Armour & clothing",
			"Keys & access items",
			"Books & lore", "Quest consumables",
			"Artefacts & relics", "Remains & trophies",
			"Quest supplies & materials", "Quest items"
		));
		// Brief #90: construction kit rows (tools / nails / planks) sit
		// between navigation gear and the ship parts they build.
		// Tab audit: the 380-item fallback split into content-derived
		// rows (cannon ammo, keys/schematics, deep-sea fish, monster
		// parts, salvage, island resources, cocktails, pearls).
		m.put("sailing", List.of(
			"Sailing tools & navigation",
			"Construction tools",
			"Nails",
			"Planks",
			"Shipbuilding materials",
			"Ship components",
			"Cannons & cannonballs",
			"Keys, charts & schematics",
			"Raw sailing fish",
			// Section audit: renamed — burnt stages + stuffable trophy
			// catches share the row.
			"Cooked fish & trophies",
			"Sea creature parts",
			"Salvage",
			"Island resources",
			"Boat cocktails & brews",
			"Pearls",
			"Cargo & contracts",
			"Sailing outfit & rewards"
		));
		// Brief #75: set-per-row redesign. Within each section, items
		// sharing a set_name cluster on a single row, with row breaks
		// between sets. See SkillBankLayoutBuilder cosmetics path.
		// Tab audit: skill capes, Leagues/speedrun rewards, recoloured
		// outfits and quest/regional costumes carved out of Miscellaneous.
		m.put("cosmetics", List.of(
			"Treasure trail sets",
			"Minigame sets",
			"Holiday items",
			"Random event sets",
			"Ornament kits",
			"Skill capes & max capes",
			"Leagues & speedrun rewards",
			"Recoloured outfits",
			"Quest & regional outfits",
			"Miscellaneous cosmetics"
		));
		// Brief #62 / #66: Teleports tab. Brief #66 added "Teleport runes"
		// at the top — runes used in spellbook teleports.
		// Tab audit: rows grouped by teleport FORM (tablets / scrolls /
		// diary rewards); the never-populated destination rows (City /
		// Boss & PvM / Minigame) removed.
		m.put("teleports", List.of(
			"Teleport runes",
			"Mounted & charged jewellery",
			"Spellbook tablets",
			"Teleport scrolls",
			"Diary & reward teleports",
			"Skill destinations",
			"Wilderness teleports",
			"Quest-locked teleports",
			"Special & one-time"
		));
		TAB_SECTIONS = Collections.unmodifiableMap(m);
	}

	/** Tabs that use the two-zone (loadout / chaff) split. */
	// Brief #74: two-zone partitioning restricted to combat tabs only.
	// Skill tabs use single-zone layout with section row breaks (Brief #65)
	// — the previous loadout/chaff split was producing unnecessary double
	// rows of pickaxes, axes, ores, etc.
	public static final Set<String> TWO_ZONE_TABS = Set.of(
		"melee", "range", "mage"
	);

	// ── Item metadata (lazy-loaded from bundled JSON) ──────────────────────

	private static volatile Map<Integer, ItemMeta> ITEM_META;

	/** Initialise the static item-meta cache using the runtime's injected
	 *  {@link Gson} instance. Idempotent — subsequent calls are no-ops.
	 *  Plugin-hub static analysis forbids direct Gson construction; the
	 *  injected instance is the only legal source. Called from
	 *  {@link SkillBankLayoutBuilder}'s constructor so every later
	 *  {@link #itemMeta()} caller sees a populated cache. */
	public static void initItemMeta(Gson gson)
	{
		if (ITEM_META != null)
		{
			return;
		}
		synchronized (SkillBankSortData.class)
		{
			if (ITEM_META == null)
			{
				ITEM_META = loadItemMeta(gson);
			}
		}
	}

	public static Map<Integer, ItemMeta> itemMeta()
	{
		Map<Integer, ItemMeta> m = ITEM_META;
		if (m == null)
		{
			log.warn("itemMeta() called before initItemMeta(Gson); returning empty map");
			return Collections.emptyMap();
		}
		return m;
	}

	private static Map<Integer, ItemMeta> loadItemMeta(Gson gson)
	{
		long t0 = System.nanoTime();
		try (InputStream in = SkillBankSortData.class.getResourceAsStream("/com/skillbank/item-meta.json"))
		{
			if (in == null)
			{
				log.warn("item-meta.json not found on classpath — runtime layout will be empty");
				return Collections.emptyMap();
			}
			Type type = new TypeToken<Map<String, ItemMeta>>() {}.getType();
			Map<String, ItemMeta> raw = gson.fromJson(
				new InputStreamReader(in, StandardCharsets.UTF_8),
				type
			);
			Map<Integer, ItemMeta> out = new HashMap<>(raw.size());
			for (Map.Entry<String, ItemMeta> e : raw.entrySet())
			{
				try
				{
					out.put(Integer.parseInt(e.getKey()), e.getValue());
				}
				catch (NumberFormatException nfe)
				{
					log.warn("Skipping non-numeric item-meta key: {}", e.getKey());
				}
			}
			long ms = (System.nanoTime() - t0) / 1_000_000;
			log.info("Loaded {} item metadata entries in {} ms", out.size(), ms);
			return out;
		}
		catch (Exception e)
		{
			log.error("Failed to load item-meta.json", e);
			return Collections.emptyMap();
		}
	}
}
