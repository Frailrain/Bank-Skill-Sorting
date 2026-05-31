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
	 *  Grimy sorts before Clean. */
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
		"Antidote++", "Antidote+", "Antipoison", "Superantipoison",
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

	/** Canonical rune order. */
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
	 *    Zenyte, Uncut opal, Opal, Uncut jade, Jade, Uncut red topaz, Red topaz. */
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
		m.put("melee", List.of(
			"Weapons", "Shields & defenders", "Head", "Body", "Legs",
			"Hands", "Feet", "Capes", "Neck", "Rings", "Ammunition",
			"Food",
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
			"Training & utility"
		));
		// Brief #74: Runes back on top of the mage tab (reverts Brief #66's swap).
		m.put("mage", List.of(
			"Runes", "Weapons", "Off-hands, books & tomes", "Head", "Body",
			"Legs", "Hands", "Feet", "Capes", "Neck", "Rings",
			"Teleport tablets & spell utility",
			"Food",
			"Enchanting & skilling magic"
		));
		// Brief #66: simple-mode combat tab variants. The layout builder
		// picks these keys when the per-tab simple-mode config flag is on.
		m.put("melee_simple", List.of(
			"Weapons", "Shields & defenders", "Armor", "Capes", "Neck",
			"Rings", "Ammunition", "Food", "Training & utility"
		));
		m.put("range_simple", List.of(
			"Weapons", "Ammunition", "Armor", "Capes",
			"Shields & off-hands", "Neck", "Rings", "Food", "Training & utility"
		));
		m.put("mage_simple", List.of(
			"Runes", "Weapons", "Off-hands, books & tomes", "Armor",
			"Capes", "Neck", "Rings",
			"Teleport tablets & spell utility", "Food", "Enchanting & skilling magic"
		));
		m.put("prayer", List.of(
			// Brief #60: equipment leads the tab now.
			"Prayer equipment & robes", "Bones & ashes", "Ensouled heads",
			"Prayer-restoring consumables", "Holy symbols, books & blessings",
			"Bone-processing utility"
		));
		// Brief #76: raw paired with cooked, finished composites together,
		// then tools + outfit, then the ingredient dump, then burnt.
		m.put("cooking", List.of(
			"Raw fish", "Cooked fish",
			"Raw meat", "Cooked meat",
			"Combo food", "Baked & cooked goods",
			"Cooking tools & utensils",
			"Ingredients",
			"Burnt food"
		));
		// Brief #63: wc_fletching split into a combined Woodcutting + Firemaking
		// tab and a standalone Fletching tab. Display names live in the
		// Python TAB_DISPLAY_NAMES map; tag IDs stay snake_case so existing
		// banktags config keys keep parsing.
		// Brief #66: tools first, outfits second, materials after.
		m.put("woodcutting_firemaking", List.of(
			"Axes",
			"Tinderboxes & firelighting tools",
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
		m.put("fishing", List.of(
			"Fishing tools", "Bait & consumables", "Fishing outfit",
			"Raw fish", "Fishing minigame items"
		));
		// Brief #64: standalone firemaking tab removed. Its content lives
		// in "woodcutting_firemaking" (Woodcutting + Firemaking).
		// Brief #76: moulds split out from tools; new top of tab.
		m.put("crafting", List.of(
			"Moulds", "Crafting tools", "Gems", "Hides & leather",
			"Spinning materials", "Glassmaking", "Pottery & clay",
			"Jewellery materials", "Crafted jewellery",
			"Crafted armour & leather goods", "Crafting outfit & utility"
		));
		m.put("mining_smithing", List.of(
			"Pickaxes", "Mining outfit & utility", "Ores", "Bars",
			"Smithing tools", "Smithed weapons", "Smithed armour",
			"Cannonballs & ammo outputs", "Giants' Foundry & minigame items"
		));
		// Brief #76: flat 5-section layout. Barbarian mixes + Divine variants
		// roll into Finished potions; outfit + utility folds into Tools.
		m.put("herblore", List.of(
			"Tools", "Herbs", "Secondaries",
			"Unfinished potions", "Finished potions"
		));
		m.put("agility_thieving", List.of(
			"Agility outfit & graceful", "Run-energy consumables",
			"Agility marks & tickets", "Thieving outfit & rogue set",
			"Thieving tools", "Thieving loot & artefacts"
		));
		m.put("slayer", List.of(
			"Slayer assignment items", "Mandatory protection",
			"Core slayer gear", "Cannon & burst supplies",
			"Combat potions", "Prayer & restores", "Food",
			"Teleports", "Loot management", "Misc utility"
		));
		m.put("farming", List.of(
			"Farming tools", "Compost & soil treatment",
			"Allotment seeds", "Hops seeds", "Flower seeds",
			"Herb seeds", "Bush seeds", "Tree seeds",
			"Fruit tree seeds", "Special seeds", "Saplings",
			"Farmer outfit & contracts"
		));
		m.put("runecraft", List.of(
			"Essence", "Pouches & storage", "Talismans", "Tiaras",
			"Core runes", "Combination runes", "Runecraft outfit",
			"Guardians of the Rift items"
		));
		// Brief #76: Creature products split into furs / meats / tertiaries.
		m.put("hunter", List.of(
			"Hunter tools & traps", "Nets, jars & containers",
			"Hunter outfit",
			"Furs & hides", "Hunter meats", "Hunter tertiaries",
			"Chinchompas", "Implings & impling jars", "Birdhouse items"
		));
		m.put("construction", List.of(
			"Construction tools", "Planks", "Nails",
			"Building materials", "Teleport-to-house items",
			"Mahogany Homes & contract items",
			"Construction outfit & rewards"
		));
		m.put("misc", List.of(
			"Currency & exchange tokens", "General teleports",
			"Jewellery teleports", "Utility containers",
			"Clue scroll items", "Minigame currencies & tickets",
			"General tools", "Uncategorized"
		));
		m.put("quests", List.of(
			"Quest items", "Quest reward equipment",
			"Reclaimable quest utility"
		));
		m.put("sailing", List.of(
			"Sailing tools & navigation", "Ship components",
			"Shipbuilding materials", "Cargo & contracts",
			"Sailing outfit & rewards"
		));
		// Brief #75: set-per-row redesign. Within each section, items
		// sharing a set_name cluster on a single row, with row breaks
		// between sets. See SkillBankLayoutBuilder cosmetics path.
		m.put("cosmetics", List.of(
			"Treasure trail sets",
			"Minigame sets",
			"Holiday items",
			"Random event sets",
			"Ornament kits",
			"Miscellaneous cosmetics"
		));
		// Brief #62 / #66: Teleports tab. Brief #66 added "Teleport runes"
		// at the top — runes used in spellbook teleports.
		m.put("teleports", List.of(
			"Teleport runes",
			"Mounted & charged jewellery",
			"Spellbook tablets",
			"Skill destinations",
			"City teleports",
			"Boss & PvM destinations",
			"Minigame teleports",
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
