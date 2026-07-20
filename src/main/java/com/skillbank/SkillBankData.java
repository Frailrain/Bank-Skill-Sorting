package com.skillbank;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Predefined item ID lists for each skill tag group.
 *
 * Design philosophy:
 *   - Items appear in EVERY tab where the player would want them visible.
 *   - Raw inputs live in the producing skill tab (raw fish -> Fishing) AND the
 *     consuming skill tab (raw fish -> Cooking).
 *   - Outputs live in their consumption tab (cooked fish -> Cooking AND Melee
 *     for combat eating).
 *   - Multi-use tools appear in every skill that uses them (hammer -> Mining/
 *     Smithing AND Construction; chisel -> Crafting AND Mining/Smithing AND
 *     Runecraft; saw -> Construction AND Crafting).
 *   - Herblore OWNS every finished potion; skill-specific potions also live in
 *     their combat/skilling tab so they surface during training.
 *   - Skill capes and pets go in their home skill.
 *   - misc tab: teleports, keys, clue scrolls, utility storage — things banked
 *     long-term that don't belong to one skill.
 *   - quests tab: quest rewards, diary rewards, boss uniques, minigame gear
 *     that players keep but don't easily bucket into a skill tab.
 *
 * Tag keys correspond to the `banktags` config values the plugin writes, e.g.
 *   `taggedItems_melee` -> "1333,1127,1079,..."
 *
 * IDs come from net.runelite.api.ItemID / the OSRS Wiki. A few cosmetic variant
 * IDs (graceful recolours, slayer helm recolours) may drift; verify against
 * ItemID before shipping.
 */
public final class SkillBankData
{
	public static final String TAG_MELEE = "melee";
	public static final String TAG_RANGE = "range";
	public static final String TAG_MAGE = "mage";
	public static final String TAG_PRAYER = "prayer";
	public static final String TAG_COOKING = "cooking";
	public static final String TAG_WOODCUTTING_FIREMAKING = "woodcutting_firemaking";
	public static final String TAG_FLETCHING = "fletching";
	public static final String TAG_FISHING = "fishing";
	public static final String TAG_CRAFTING = "crafting";
	public static final String TAG_MINING_SMITHING = "mining_smithing";
	public static final String TAG_HERBLORE = "herblore";
	public static final String TAG_AGILITY_THIEVING = "agility_thieving";
	public static final String TAG_SLAYER = "slayer";
	public static final String TAG_FARMING = "farming";
	public static final String TAG_RUNECRAFT = "runecraft";
	public static final String TAG_HUNTER = "hunter";
	public static final String TAG_CONSTRUCTION = "construction";
	public static final String TAG_MISC = "misc";
	public static final String TAG_QUESTS = "quests";
	public static final String TAG_SAILING = "sailing";
	public static final String TAG_COSMETICS = "cosmetics";
	public static final String TAG_TELEPORTS = "teleports";

	/**
	 * Suffix appended to every bank tag name this plugin manages. Internal
	 * tab ids stay bare ("melee", "slayer" — config keys, section lookups,
	 * panel labels); ONLY the Bank Tags boundary (tagManager calls, icon
	 * config keys, the tagtabs CSV, saved Layout names) uses the suffixed
	 * form ("melee (auto)"). This is what stops the plugin from hijacking
	 * a tag the user created themselves.
	 */
	public static final String TAG_SUFFIX = " (auto)";

	/**
	 * Brief #90: internal tag id -> tab display name. Bank Tags standardizes
	 * every tab name to lower case on load ({@code TabManager.loadAllTabNames}
	 * -> {@code Text.standardize}), so title case is not achievable — tab names
	 * always render lower case. The only display win available is replacing the
	 * underscore in compound ids with " + " ("woodcutting_firemaking" ->
	 * "woodcutting + firemaking"). Simple ids display as-is.
	 */
	public static String displayName(String internalTag)
	{
		return internalTag.replace("_", " + ");
	}

	/** The "(auto)"-scheme Bank Tags name for one of our managed tabs:
	 *  {@link #displayName} plus {@link #TAG_SUFFIX}, e.g.
	 *  "woodcutting + firemaking (auto)". Note a migrated legacy install may
	 *  instead manage the bare {@code displayName} form — resolve active tags
	 *  through the plugin's op-key lookup, not by assuming this suffix. */
	public static String bankTagName(String tabId)
	{
		return displayName(tabId) + TAG_SUFFIX;
	}

	private static final Map<String, List<Integer>> TAGS;
	private static final Map<String, Integer> TAG_ICONS;

	static
	{
		Map<String, Integer> i = new LinkedHashMap<>();
		i.put(TAG_MELEE, 4151);              // abyssal whip
		i.put(TAG_RANGE, 20997);             // twisted bow
		i.put(TAG_MAGE, 1393);               // fire battlestaff
		i.put(TAG_PRAYER, 1718);             // holy symbol
		i.put(TAG_COOKING, 1949);            // chef's hat
		i.put(TAG_WOODCUTTING_FIREMAKING, 1515); // yew logs (no tree item exists)
		i.put(TAG_FLETCHING, 1777);          // bow string
		i.put(TAG_FISHING, 307);             // fishing rod
		i.put(TAG_CRAFTING, 1755);           // chisel
		i.put(TAG_MINING_SMITHING, 1275);    // rune pickaxe
		i.put(TAG_HERBLORE, 2428);           // attack potion (4)
		i.put(TAG_AGILITY_THIEVING, 11850);  // graceful hood
		i.put(TAG_SLAYER, 4155);             // enchanted gem
		i.put(TAG_FARMING, 5341);            // rake
		i.put(TAG_RUNECRAFT, 7936);          // pure essence
		i.put(TAG_HUNTER, 9977);             // grey chinchompa
		i.put(TAG_CONSTRUCTION, 8794);       // saw
		i.put(TAG_MISC, 2572);               // ring of wealth
		i.put(TAG_QUESTS, 9813);             // quest point cape
		i.put(TAG_SAILING, 10887);           // barrelchest anchor
		i.put(TAG_COSMETICS, 1037);          // bunny ears
		i.put(TAG_TELEPORTS, 8013);          // teleport to house (tablet)
		TAG_ICONS = Collections.unmodifiableMap(i);
	}

	/** Prior default icons this plugin has shipped, so seeding can upgrade
	 *  an icon WE set without ever clobbering one the user picked. */
	private static final Map<String, Integer> LEGACY_TAG_ICONS;

	static
	{
		Map<String, Integer> i = new LinkedHashMap<>();
		i.put(TAG_MELEE, 1333);              // rune scimitar
		i.put(TAG_RANGE, 861);               // magic shortbow
		i.put(TAG_MAGE, 1387);               // staff of fire
		i.put(TAG_WOODCUTTING_FIREMAKING, 1513); // magic logs
		i.put(TAG_CRAFTING, 1733);           // needle
		i.put(TAG_CONSTRUCTION, 2347);       // hammer
		i.put(TAG_SAILING, 31288);           // sailing cape
		i.put(TAG_TELEPORTS, 1712);          // amulet of glory(4)
		LEGACY_TAG_ICONS = Collections.unmodifiableMap(i);
	}

	/** True if {@code configuredIcon} is a default this plugin previously
	 *  wrote for {@code tag} — i.e. safe to upgrade to the current default. */
	public static boolean isLegacyIcon(String tag, String configuredIcon)
	{
		Integer legacy = LEGACY_TAG_ICONS.get(tag);
		return legacy != null && String.valueOf(legacy).equals(configuredIcon);
	}

	static
	{
		Map<String, List<Integer>> m = new LinkedHashMap<>();

		// ---------------------------------------------------------------------
		// MELEE — Attack, Strength, Defence, Hitpoints.
		// Cross-tags: cooked food (combat eating), dragon/crystal/infernal axe
		// (spec weapons), slayer helm variants, cannon parts, combat potions.
		// ---------------------------------------------------------------------
		// MELEE — 1401 items
		//   Combat utility (6), Weapons (304), Helmets (249), Body armour (157),
		//   Legs (144), Boots (78), Gloves (84), Shields (119), Capes (55),
		//   Amulets (39), Rings (15), Combat potions (52), Combat food (23),
		//   Legacy (76)
		// MELEE — 1478 items
		//   Combat utility (6), Weapons (296), Helmets (234), Body armour (155),
		//   Legs (144), Boots (86), Gloves (81), Shields (110), Capes (51),
		//   Amulets (39), Rings (17), Combat potions (56), Restores (cross-tag)
		//   (14), Combat food (23), Legacy (166)
		addMelee(m);

		// ---------------------------------------------------------------------
		// RANGE — Ranged skill.
		// Cross-tags: chinchompas (hunter), cannon parts (dwarf cannon), slayer
		// helm range variants, dragonhide armour (crafting source), feathers.
		// ---------------------------------------------------------------------
		// RANGE — 703 items
		//   Ammunition (110), Bows (73), Crossbows (20), Thrown (38), Helmets
		//   (32), Body (36), Legs (43), Boots (10), Gloves (40), Shields (33),
		//   Capes (147), Amulets (35), Rings (12), Ranging potions (8), Legacy
		//   (66)
		// RANGE — 807 items
		//   Cannon parts (cross-tag) (6), Ammunition (104), Bows (53), Crossbows
		//   (21), Thrown (41), D'hide armour (55), Raw dragonhide (cross-tag with
		//   crafting) (21), Helmets (29), Body (32), Legs (36), Boots (12), Gloves
		//   (33), Shields (15), Capes (135), Amulets (28), Rings (10), Ranging
		//   potions (8), Bait / feathers (cross-tag) (6), Combat food (cross-tag)
		//   (23), Legacy (139)
		addRange(m);

		// ---------------------------------------------------------------------
		// MAGE — Magic skill.
		// Cross-tags: rune pouch (RC), runes (RC), slayer helm mage variants,
		// law runes (misc teleports), combat food.
		// ---------------------------------------------------------------------
		// MAGE — 742 items
		//   Basic runes (16), Combo runes (6), Essence (4), Staves (114), Tomes
		//   (12), Helmets (98), Body (63), Legs (59), Boots (27), Gloves (68),
		//   Shields (33), Capes (93), Amulets (51), Rings (15), Magic potions
		//   (28), Legacy (55)
		// MAGE — 803 items
		//   Basic runes (16), Combo runes (6), Essence (4), Staves (115), Tomes
		//   (12), Helmets (95), Body (60), Legs (57), Boots (35), Gloves (64),
		//   Shields (28), Capes (78), Amulets (42), Rings (12), Magic potions
		//   (28), Orbs (cross-tag with crafting) (7), God cloaks (2), Combat food
		//   (cross-tag) (23), Legacy (119)
		addMage(m);

		// ---------------------------------------------------------------------
		// PRAYER — Prayer skill.
		// Cross-tags: prayer potions/super restores (dup herblore). Mostly stays
		// skill-pure (bones, altar items, holy books).
		// ---------------------------------------------------------------------
		// PRAYER — 192 items
		//   Bones (85), Ashes (9), Ensouled heads (23), Prayer potions (4), Super
		//   restores (10), Sanfew (4), Saradomin brews (4), Holy symbols (4),
		//   Prayer accessories (6), Proselyte (6), Legacy (37)
		// PRAYER — 266 items
		//   Bones (93), Ashes (9), Ensouled heads (23), Prayer potions (4), Super
		//   restores (10), Sanfew (4), Saradomin brews (4), Holy symbols (36),
		//   Robes (monk/proselyte/initiate) (17), Bone secondaries (5), Quest-
		//   related prayer items (7), Prayer accessories (5), Legacy (49)
		addPrayer(m);

		// Teleports sits between prayer and cooking in the bank-tab row.
		addTeleports(m);

		// ---------------------------------------------------------------------
		// COOKING — Cooking skill.
		// Cross-tags: raw fish (dup fishing), tinderbox (lighting fires),
		// cooked meat/poultry ingredients.
		// ---------------------------------------------------------------------
		// COOKING — 253 items
		//   Cooking tools (11), Raw fish (25), Cooked fish (23), Pies (10), Pizzas
		//   (4), Stews & curries (3), Cakes (4), Breads (4), Gnome food (24),
		//   Beverages (8), Raw meat & ingredients (21), Legacy (116)
		// COOKING — 416 items
		//   Cooking tools (10), Raw fish (25), Cooked fish (23), Pies (10), Pizzas
		//   (4), Stews & curries (3), Cakes (2), Breads (4), Gnome food (24),
		//   Beverages (11), Burnt food (67), Containers (water/milk/etc) (14), Raw
		//   meat & ingredients (94), Misc cooked food (19), Pies (extended) (19),
		//   Cooking pet & misc (4), Legacy (83)
		addCooking(m);

		// ---------------------------------------------------------------------
		// WC_FLETCHING — Woodcutting + Fletching.
		// Cross-tags: logs (dup firemaking), feathers (dup fishing/range),
		// axes (dup melee).
		// ---------------------------------------------------------------------
		// WC_FLETCHING — 372 items
		//   Axes (42), Logs (30), Bowstrings (3), Unstrung bows (38), Bows &
		//   shortbows (strung) (71), Crossbow parts (14), Bolt tips (11), Bolts
		//   (unfinished) (8), Bolts (finished) (38), Arrow shafts (2), Arrowtips
		//   (9), Arrows (27), Darts (20), Javelin parts (8), Bird nests (1), Tools
		//   (2), Legacy (48)
		// WC_FLETCHING — 420 items
		//   Axes (40), Logs (42), Bowstrings (3), Unstrung bows (39), Bows &
		//   shortbows (strung) (51), Crossbow parts (14), Bolt tips (11), Bolts
		//   (unfinished) (8), Bolts (finished) (39), Arrow shafts (2), Arrowtips
		//   (11), Arrows (28), Darts (22), Bird nests (1), Feathers (cross-tag
		//   with fishing) (6), Flax & secondary fletching materials (2), Tools
		//   (2), Forestry items (post-Sept 2023) (15), Capes & pet (6), Legacy
		//   (78)
		// Brief #63: wc_fletching split. The existing item list lives in
		// addWoodcuttingFiremaking() unchanged — the audit pass will
		// re-evaluate each id and migrate the fletching subset into the new
		// Fletching tab.
		addWoodcuttingFiremaking(m);
		addFletching(m);

		// ---------------------------------------------------------------------
		// FISHING — Fishing skill.
		// Cross-tags: raw fish (dup cooking), feathers (dup fletching).
		// ---------------------------------------------------------------------
		// FISHING — 95 items
		//   Rods & tools (15), Bait (8), Raw fish (25), Specialty fish (6), Angler
		//   outfit (4), Spirit angler outfit (4), Tempoross rewards (6), Cape &
		//   pet (4), Legacy (23)
		// FISHING — 95 items
		//   Rods & tools (15), Bait (8), Raw fish (25), Specialty fish (6), Angler
		//   outfit (4), Spirit angler outfit (4), Tempoross rewards (5), Cape &
		//   pet (3), Legacy (25)
		addFishing(m);

		// Brief #64: standalone firemaking tab removed. Its items were merged
		// into addWoodcuttingFiremaking() (see new === Merged from firemaking
		// (Brief #64) === block in that method).

		// ---------------------------------------------------------------------
		// CRAFTING — Crafting skill.
		// Cross-tags: saw (dup construction), planks (dup construction),
		// chisel (dup mining_smithing, runecraft), all bars (dup mining_smithing,
		// construction), dragonhides (dup range), thread, needle.
		// ---------------------------------------------------------------------
		// CRAFTING — 178 items
		//   Crafting tools (6), Thread & dyes (10), Leather (raw → tanned) (7),
		//   D'hide (26), Glass (11), Pottery (10), Uncut gems (10), Cut gems (10),
		//   Jewellery (silver) (1), Jewellery (gold) (5), Battlestaves (10),
		//   Crafting cape & pet (3), Legacy (69)
		// CRAFTING — 193 items
		//   Crafting tools (6), Thread & dyes (10), Leather (raw → tanned) (7),
		//   D'hide (27), Glass (11), Pottery (10), Uncut gems (10), Cut gems (10),
		//   Jewellery (silver) (1), Jewellery (gold) (5), Battlestaves (10),
		//   Crafting cape & pet (2), Wool/dyes/cloth (extended) (2), Leathers
		//   (extended) (14), Pottery & wood crafting outputs (3), Legacy (65)
		addCrafting(m);

		// ---------------------------------------------------------------------
		// MINING_SMITHING — Mining + Smithing.
		// Cross-tags: chisel (gem cutting), hammer (dup construction), coal bag,
		// gem bag, all bars stay primary here.
		// ---------------------------------------------------------------------
		// MINING_SMITHING — 118 items
		//   Pickaxes (19), Mining tools & bags (7), Ores (19), Bars (10), Smithing
		//   outputs (9), Mining outfit (Prospector) (4), Mining/Smithing capes &
		//   pets (7), Legacy (43)
		// MINING_SMITHING — 296 items
		//   Pickaxes (18), Mining tools & bags (4), Ores (19), Bars (10), Smithing
		//   outputs (8), Mining outfit (Prospector) (4), Mining/Smithing capes &
		//   pets (5), Crystal-tool/Tier-fallback pickaxes (1), Smithing armour
		//   outputs (extended) (167), Gem cutting/polishing inputs (10), Legacy
		//   (50)
		addMiningSmithing(m);

		// ---------------------------------------------------------------------
		// HERBLORE — Herblore skill (owns all finished potions).
		// Cross-tags: herb sack (dup farming), compost potions (dup farming),
		// combat potions (dup melee/range/mage), plant cure (dup farming),
		// bottomless compost bucket (dup farming).
		// ---------------------------------------------------------------------
		// HERBLORE — 342 items
		//   Tools (5), Grimy herbs (14), Clean herbs (11), Vials & secondaries
		//   (24), Unfinished potions (19), Attack potions (8), Strength potions
		//   (8), Defence potions (8), Super attack/strength/defence (12), Super
		//   combat (8), Ranging & magic (20), Prayer & restores (22), Antifire &
		//   anti-poison (68), Energy & stamina (14), Other potions (48), Cape &
		//   pet (4), Legacy (49)
		// HERBLORE — 361 items
		//   Tools (4), Grimy herbs (14), Clean herbs (11), Vials & secondaries
		//   (24), Unfinished potions (22), Attack potions (8), Strength potions
		//   (8), Defence potions (8), Super attack/strength/defence (12), Super
		//   combat (8), Ranging & magic (20), Prayer & restores (22), Antifire &
		//   anti-poison (68), Energy & stamina (22), Other potions (56), Cape &
		//   pet (3), Legacy (51)
		addHerblore(m);

		// ---------------------------------------------------------------------
		// AGILITY_THIEVING — Agility + Thieving.
		// Cross-tags: stamina potions (dup herblore), super energy (dup herblore).
		// ---------------------------------------------------------------------
		// AGILITY_THIEVING — 124 items
		//   Marks & tickets (3), Graceful set (8), Agility shortcut tools (1),
		//   Rogue equipment (6), Thieving accessories (9), Blackjacks (9), Pyramid
		//   plunder (1), Capes & pets (8), Legacy (79)
		// AGILITY_THIEVING — 126 items
		//   Marks & tickets (3), Graceful set (7), Agility shortcut tools (2),
		//   Rogue equipment (6), Thieving accessories (9), Blackjacks (10),
		//   Pyramid plunder (1), Capes & pets (6), Legacy (82)
		addAgilityThieving(m);

		// ---------------------------------------------------------------------
		// SLAYER — Slayer skill.
		// Cross-tags: slayer helm (dup melee/range/mage), black mask, cannon
		// parts (dup melee/range — multicannon is core slayer tool),
		// enchanted gem (dup crafting).
		// ---------------------------------------------------------------------
		// SLAYER — 168 items
		//   Slayer master items (4), Slayer rings (8), Slayer helmets (46), Black
		//   masks (24), Task-specific gear (25), Cannon (6), Cape & pet (5),
		//   Legacy (50)
		// SLAYER — 171 items
		//   Slayer master items (4), Slayer rings (7), Slayer helmets (27), Black
		//   masks (18), Task-specific gear (25), Cannon (5), Cape & pet (4),
		//   Legacy (81)
		addSlayer(m);

		// ---------------------------------------------------------------------
		// FARMING — Farming skill.
		// Cross-tags: graceful (run energy for farming runs), herb sack (harvest
		// storage), compost potions (dup herblore), plant cure (dup herblore),
		// bottomless compost bucket, spade.
		// ---------------------------------------------------------------------
		// FARMING — 228 items
		//   Tools (18), Compost (9), Seeds (142), Farmer outfit (6), Cape & pet
		//   (4), Legacy (49)
		// FARMING — 241 items
		//   Tools (17), Compost (9), Seeds (155), Farmer outfit (6), Cape & pet
		//   (3), Legacy (51)
		addFarming(m);

		// ---------------------------------------------------------------------
		// RUNECRAFT — Runecraft skill.
		// Cross-tags: runes (dup mage), rune pouch (dup mage), chisel (tiara
		// making, dup crafting/mining_smithing).
		// ---------------------------------------------------------------------
		// RUNECRAFT — 124 items
		//   Talismans (24), Tiaras (20), Essence pouches (7), Essence (4), Basic
		//   runes (16), Combo runes (6), Raiments of the eye (13), Cape & pet (4),
		//   Legacy (30)
		// RUNECRAFT — 124 items
		//   Talismans (24), Tiaras (20), Essence pouches (7), Essence (4), Basic
		//   runes (16), Combo runes (6), Raiments of the eye (4), Cape & pet (3),
		//   Legacy (40)
		addRunecraft(m);

		// ---------------------------------------------------------------------
		// HUNTER — Hunter skill.
		// Cross-tags: chinchompas (dup range), impling jars (dup misc).
		// ---------------------------------------------------------------------
		// HUNTER — 120 items
		//   Traps (5), Salamanders (9), Bait (5), Impling jars (12), Polar camo
		//   (2), Desert camo (2), Jungle camo (2), Larupia hunter (5), Graahk
		//   hunter (5), Kyatt hunter (5), Spotted/spottier (3), Cape & pet (5),
		//   Legacy (60)
		// HUNTER — 120 items
		//   Traps (5), Salamanders (9), Bait (5), Impling jars (12), Polar camo
		//   (2), Desert camo (2), Jungle camo (2), Larupia hunter (5), Graahk
		//   hunter (5), Kyatt hunter (5), Spotted/spottier (3), Cape & pet (4),
		//   Legacy (61)
		addHunter(m);

		// ---------------------------------------------------------------------
		// CONSTRUCTION — Construction skill.
		// Cross-tags: planks (dup crafting), saw (dup crafting), hammer (dup
		// mining_smithing), bars (dup mining_smithing, crafting),
		// bolts of cloth (dup crafting), soft clay (dup mining_smithing).
		// ---------------------------------------------------------------------
		// CONSTRUCTION — 71 items
		//   Tools (4), Planks (4), Nails (7), Construction materials (9), Mahogany
		//   Homes (7), POH teleports (3), Legacy (37)
		// CONSTRUCTION — 110 items
		//   Tools (4), Planks (4), Nails (7), Construction materials (10), POH
		//   portals & telescopes (2), Bench/altar (pattern) (37), Mahogany Homes
		//   (7), POH teleports (2), Legacy (37)
		addConstruction(m);

		// ---------------------------------------------------------------------
		// SAILING — Sailing skill (released 2025-11-19).
		// Many Sailing items lack stable IDs in net.runelite.api.ItemID at the
		// time of writing; flagged entries inferred from the wiki and adjacent
		// confirmed IDs in the 31000-32400 release range. Cross-tags for fish
		// live in cooking/fishing/melee buckets above.
		// ---------------------------------------------------------------------
		// SAILING — 16 items
		//   Navigation tools (4), Raw sailing fish (5), Cooked sailing fish (5),
		//   Cape & pet (2)
		// SAILING — 49 items
		//   Navigation tools (20), Raw sailing fish (5), Cooked sailing fish (20),
		//   Cape & pet (2), Legacy (2)
		addSailing(m);

		// ---------------------------------------------------------------------
		// QUESTS — quest & minigame & diary items kept long-term in-bank.
		// Includes: quest point cape, notable quest-locked gear, diary rewards,
		// boss uniques/cosmetics, minigame gear that doesn't fit skill tabs.
		// ---------------------------------------------------------------------
		// QUESTS — 211 items
		//   Quest & achievement capes (9), Diary - Kandarin (5), Diary - Karamja
		//   (5), Diary - Ardougne (5), Diary - Falador (5), Diary - Fremennik (5),
		//   Diary - Wilderness (5), Diary - Morytania (5), Diary - Desert (5),
		//   Diary - Varrock (5), Diary - Western (5), Diary consumables (5), Quest
		//   unlock weapons (11), Void Knight set (41), Fighter Torso et al. (6),
		//   Defenders (9), Boss pets (9), Legacy (71)
		// QUESTS — 212 items
		//   Quest & achievement capes (8), Diary - Kandarin (4), Diary - Karamja
		//   (4), Diary - Ardougne (4), Diary - Falador (4), Diary - Fremennik (4),
		//   Diary - Wilderness (4), Diary - Morytania (4), Diary - Desert (4),
		//   Diary - Varrock (4), Diary - Western (4), Diary consumables (4), Quest
		//   unlock weapons (11), Void Knight set (21), Fighter Torso et al. (6),
		//   Defenders (9), Boss pets (9), Legacy (104)
		addQuests(m);

		// ---------------------------------------------------------------------
		// MISC — long-term banked utility items.
		// Keys, teleports (jewellery / tablets / items), clue scrolls + tools,
		// impling jars, storage bags, chronicle, fairy ring utils, misc jugs,
		// strange/golden rocks, seed pods.
		// ---------------------------------------------------------------------
		// MISC — 218 items
		//   Teleport jewellery (49), Teleport tabs (66), Clue scrolls (8), Clue
		//   tools (5), Keys (12), Storage bags (11), Currency (5), Legacy (62)
		// MISC — 228 items
		//   Teleport jewellery (43), Teleport tabs (68), Boss & quest jewellery
		//   (9), Clue scrolls (8), Clue tools (5), Keys (12), Storage bags (6),
		//   Currency (5), Legacy (72)
		addMisc(m);

		addCosmetics(m);

		// Freeze lists to prevent accidental mutation.
		Map<String, List<Integer>> locked = new LinkedHashMap<>();
		for (Map.Entry<String, List<Integer>> e : m.entrySet())
		{
			locked.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
		}
		TAGS = Collections.unmodifiableMap(locked);
	}

	private SkillBankData()
	{
	}

	public static Map<String, List<Integer>> tags()
	{
		return TAGS;
	}

	public static List<Integer> itemsFor(String tag)
	{
		return TAGS.getOrDefault(tag, Collections.emptyList());
	}

	/** Returns the item ID to use as this tag's tab icon, or 0 if none mapped. */
	public static int iconFor(String tag)
	{
		Integer id = TAG_ICONS.get(tag);
		return id != null ? id : 0;
	}

	private static void addMelee(Map<String, List<Integer>> m)
	{
		// MELEE — 1683 items
		//   Weapons (396), Shields & defenders (175), Head (253), Body (129), Legs
		//   (142), Hands (47), Feet (49), Capes (15), Neck (40), Rings (25), Ammo
		//   slot (2), Food (71), Potions (183), Training & utility (156)
		m.put(TAG_MELEE, Arrays.asList(
			// === Weapons ===
			27871, 1307, 1375, 3095, 1205, 3190, 11367, 1291,
			1422, 1321, 1237, 1277, 1337, 3899, 1309, 1363,
			3096, 1203, 3192, 11369, 1293, 1420, 11721, 1323,
			1239, 1279, 1335, 1311, 1365, 3097, 1207, 3194,
			11371, 1295, 1424, 1325, 1241, 1281, 1339, 6416,
			6420, 6418, 4599, 6410, 6408, 4600, 6414, 6412,
			1313, 1367, 12375, 3098, 1217, 3196, 1297, 1426,
			12297, 1327, 4580, 1283, 1341, 10148, 6609, 6589,
			6587, 6591, 6599, 6607, 6601, 6611, 6605, 6613,
			1315, 1369, 3099, 1209, 3198, 11373, 1299, 1428,
			11720, 1329, 1243, 1285, 1343, 1317, 1371, 12377,
			3100, 1211, 3200, 11375, 1301, 1430, 1331, 1245,
			1287, 1345, 26262, 1319, 1373, 12379, 3101, 1213,
			3202, 11377, 1303, 1432, 11719, 1333, 23330, 23332,
			23334, 1247, 1289, 1347, 21742, 21646, 4153, 20557,
			24225, 28534, 28051, 28037, 28039, 26708, 28019, 28049,
			27859, 28033, 27857, 28027, 28031, 28041, 28029, 28035,
			26710, 7158, 1377, 12373, 13652, 20784, 1215, 20407,
			3204, 22731, 1305, 1434, 11920, 12797, 4587, 20406,
			20000, 1249, 21009, 13576, 22978, 30340, 23896, 23895,
			23897, 23861, 23987, 13091, 13080, 27861, 26484, 26482,
			26233, 27184, 20251, 30955, 22665, 29605, 21060, 27855,
			31248, 25870, 25872, 25874, 25876, 25878, 25880, 25882,
			24697, 33713, 5018, 8872, 28792, 5016, 7671, 29577,
			33200, 7451, 27021, 28537, 23850, 23849, 23851, 23820,
			28543, 28545, 28531, 33631, 6746, 25516, 28682, 31132,
			28997, 29850, 23206, 30957, 7435, 27100, 29589, 22433,
			22435, 24695, 33249, 7441, 29889, 6760, 33718, 19941,
			25734, 25736, 25738, 20254, 30347, 30348, 30342, 30343,
			30345, 30346, 33243, 27198, 12357, 25979, 30891, 25981,
			27287, 27291, 20257, 11711, 7449, 21649, 30390, 30392,
			29796, 33178, 19918, 26219, 33174, 27246, 20260, 7648,
			7639, 7647, 7646, 7644, 7643, 7642, 7641, 7445,
			24693, 25739, 25741, 6762, 1419, 22664, 10858, 20263,
			2402, 7443, 30759, 33335, 7439, 20397, 7437, 22331,
			27908, 29084, 30369, 33722, 24219, 30367, 33038, 30388,
			33041, 9703, 27660, 27657, 27189, 27904, 27900, 27690,
			27869, 29607, 1415, 13141, 13142, 13143, 13144, 13108,
			13109, 13110, 13111, 2952, 7433, 7675, 6764, 28810,
			19912, 4068, 4503, 4508, 10491, 7141, 11061, 3757,
			7668, 7140, 10149, 25641, 11037, 20155, 20161, 12389,
			20158, 20756, 22398, 7447, 7142, 23235, 8841, 24699,
			10581, 10146, 4158, 11902, 10887, 10147, 6523, 6525,
			6527, 6528, 22542, 20011, 12426, 20014, 20727, 23528,
			13263, 13265, 4151, 20405, 4718, 12774, 4726, 11838,
			4747, 4755, 12773, 11889, 11824, 12006, 19675, 11802,
			20593, 20368, 11804, 20370, 23995, 24551, 21015, 21003,
			21205, 22324, 23628, 24417, 11806, 20372, 12809, 22325,
			22486, 24144, 22296, 23613, 24617, 11808, 20374, 22622,
			23620, 22613, 23615, 22610,

			// === Shields & defenders ===
			8844, 1189, 12213, 12223, 1173, 8845, 1191, 12243,
			12233, 1175, 8846, 1193, 8746, 8748, 8750, 8752,
			8754, 8756, 8758, 8760, 8762, 8764, 8766, 8768,
			8770, 8772, 8774, 8776, 20181, 20196, 1177, 8847,
			1195, 2597, 2589, 7332, 7338, 7344, 7350, 7356,
			1179, 6633, 6631, 8848, 1197, 12281, 12291, 1181,
			8849, 1199, 22127, 22129, 22131, 22133, 22135, 22137,
			22139, 22141, 22143, 22145, 22147, 22149, 22151, 22153,
			22155, 22157, 2611, 2603, 7334, 7340, 7346, 7352,
			7358, 1183, 27185, 8850, 23230, 1201, 8714, 8716,
			8718, 8720, 8722, 8724, 8726, 8728, 8730, 8732,
			8734, 8736, 8738, 8740, 8742, 8744, 2621, 2629,
			7336, 7342, 7348, 7354, 7360, 1185, 3122, 1540,
			11710, 28059, 33186, 30382, 12954, 23597, 19722, 21895,
			22244, 1187, 12418, 11283, 11284, 4235, 23991, 4224,
			11759, 31081, 3844, 12608, 26494, 20846, 3839, 3841,
			3843, 12607, 13117, 13118, 13119, 13120, 27550, 27552,
			25934, 25936, 3840, 33101, 9704, 3842, 1171, 20166,
			4072, 4507, 22251, 4156, 20272, 4512, 3758, 10826,
			22254, 12468, 12478, 12488, 3488, 20152, 2675, 22263,
			22257, 22266, 2667, 24266, 22260, 2659, 12829, 23599,
			6524, 10352, 22322, 12831, 23642, 12817, 12821,

			// === Head ===
			1155, 12211, 12221, 1139, 26156, 27042, 26170, 20792,
			12810, 12813, 1153, 12241, 12231, 1137, 1157, 20178,
			20193, 8682, 8684, 8686, 8688, 8690, 8692, 8694,
			8696, 8698, 8700, 8702, 8704, 8706, 8708, 8710,
			8712, 1141, 29560, 1165, 2595, 2587, 10306, 10308,
			10310, 10312, 10314, 1151, 19639, 19641, 8921, 8919,
			11783, 8917, 11782, 8915, 11781, 8911, 11779, 8909,
			11778, 8907, 11777, 8905, 11776, 11784, 6623, 6621,
			1159, 12283, 12293, 1143, 1161, 2613, 2605, 10296,
			10298, 10300, 10302, 10304, 22159, 22161, 22163, 22165,
			22167, 22169, 22171, 22173, 22175, 22177, 22179, 22181,
			22183, 22185, 22187, 22189, 1145, 1163, 2619, 2627,
			10286, 10288, 10290, 10292, 10294, 8464, 8466, 8468,
			8470, 8472, 8474, 8476, 8478, 8480, 8482, 8484,
			8486, 8488, 8490, 8492, 8494, 1147, 10589, 28057,
			24034, 11335, 12417, 1149, 23887, 23886, 23888, 29816,
			29818, 31172, 27169, 29028, 29848, 3329, 3335, 3333,
			20838, 23841, 23840, 23842, 27844, 7539, 25165, 25169,
			25174, 28070, 33066, 33068, 27195, 13137, 13138, 13139,
			13140, 13198, 26745, 26747, 26743, 3327, 30750, 33462,
			33338, 33439, 3331, 23101, 3339, 3343, 3337, 3341,
			30777, 33340, 33445, 28254, 12929, 21838, 27833, 28933,
			13196, 26382, 30302, 25910, 25912, 25900, 33247, 25904,
			25906, 26477, 30321, 4071, 4506, 4551, 9629, 19643,
			19645, 21264, 21266, 19647, 19649, 11864, 11865, 21888,
			21890, 24444, 5574, 13359, 13364, 13369, 13374, 4511,
			3748, 9672, 7917, 20035, 12466, 12476, 12486, 3486,
			20146, 2673, 6128, 2665, 2657, 11665, 3751, 10548,
			10549, 3753, 11200, 10828, 23591, 23073, 23075, 21298,
			24370, 10350, 4716, 23639, 4724, 23638, 24419, 24271,
			4745, 23637, 4753, 23636, 22326, 13199, 12931, 13197,
			22625, 9749, 9755, 9770, 9752,

			// === Body ===
			1103, 1117, 12205, 12215, 26158, 27048, 26172, 20794,
			12811, 12814, 1101, 1115, 12235, 12225, 1105, 1119,
			20169, 20184, 29562, 1107, 1125, 2591, 23366, 23369,
			23372, 23375, 23378, 2583, 6615, 6617, 1109, 1121,
			12277, 12287, 1111, 1123, 2607, 23392, 23395, 23398,
			23401, 23404, 2599, 1113, 1127, 2615, 23209, 23212,
			23215, 23218, 23221, 2623, 10564, 28065, 24037, 3140,
			12414, 21892, 22242, 23890, 23889, 23891, 26715, 26718,
			29022, 29846, 26749, 26721, 23844, 23843, 23845, 20840,
			27845, 27842, 25515, 26753, 26469, 28067, 27196, 30753,
			23097, 30779, 28256, 27834, 28936, 26751, 26384, 30303,
			13104, 13105, 13106, 13107, 27190, 27831, 26463, 4069,
			4504, 5575, 13361, 13366, 13371, 13376, 10822, 4509,
			9674, 20038, 12460, 12470, 12480, 10551, 20149, 3481,
			2669, 6129, 2661, 2653, 13072, 8839, 21301, 10348,
			11832, 4720, 4728, 24420, 4749, 4757, 22327, 22628,
			22616,

			// === Legs ===
			1075, 12207, 12217, 1087, 12209, 12219, 26166, 27044,
			26180, 20796, 12812, 12815, 1067, 12237, 12227, 1081,
			12239, 12229, 1069, 20172, 20187, 1083, 20175, 20190,
			29564, 1077, 2593, 2585, 1089, 3473, 3472, 6625,
			6627, 1071, 12279, 12289, 1085, 12285, 12295, 1073,
			2609, 2601, 1091, 3475, 3474, 1079, 20422, 2617,
			2625, 1093, 3476, 3477, 6809, 28061, 28063, 24040,
			4087, 12415, 4585, 12416, 23893, 23892, 23894, 26719,
			29025, 29847, 26755, 23847, 23846, 23848, 20842, 20844,
			27846, 27843, 26759, 26471, 23246, 27177, 27197, 13112,
			13113, 13114, 13115, 30756, 23095, 30781, 28258, 27835,
			28939, 26757, 26386, 30304, 33194, 27832, 26465, 4070,
			4505, 11893, 11894, 11895, 5576, 13360, 13365, 13370,
			13375, 4510, 9676, 9678, 20044, 12462, 12464, 12472,
			12474, 12482, 12484, 3483, 3485, 2671, 3480, 6130,
			2663, 3479, 2655, 3478, 13073, 8840, 21304, 10346,
			23242, 11834, 23646, 4722, 23633, 4730, 24421, 4751,
			23634, 4759, 23635, 22328, 22631, 22619,

			// === Hands ===
			7454, 7455, 778, 7456, 7457, 10085, 6629, 7458,
			20581, 27110, 7459, 20582, 7460, 27111, 21736, 7461,
			24046, 7462, 23593, 27112, 11126, 11124, 11122, 11120,
			11118, 11974, 11972, 7537, 8929, 30380, 7453, 2997,
			11133, 6151, 26467, 26723, 26727, 1059, 13357, 13362,
			13367, 13372, 20041, 10553, 8842, 10083, 22981,

			// === Feet ===
			4119, 4121, 4123, 4125, 6619, 4127, 4129, 4131,
			21643, 28055, 24043, 11840, 22234, 31088, 31093, 31097,
			33172, 31096, 31091, 31095, 31094, 26720, 3105, 20578,
			23413, 25163, 25167, 25171, 28945, 9005, 9006, 13129,
			13130, 13131, 13132, 6145, 23389, 27178, 1061, 13358,
			13363, 13368, 13373, 20047, 12391, 10552, 11836, 21733,
			13239,

			// === Capes ===
			1052, 23351, 6570, 21295, 23622, 22114, 6568, 20050,
			33063, 9747, 9753, 13329, 9768, 21285, 9750,

			// === Neck ===
			22111, 1478, 22557, 24780, 1729, 23309, 19707, 6585,
			23640, 12436, 1704, 20586, 10362, 10354, 1706, 1708,
			1710, 1712, 11976, 11978, 1731, 20585, 23354, 29801,
			29804, 1725, 12851, 30376, 27173, 11128, 23240, 11090,
			4081, 10588, 12018, 12017, 10364, 19553, 20366, 22986,

			// === Rings ===
			30895, 21739, 21752, 28316, 6737, 11773, 23595, 22975,
			21140, 30378, 25975, 27870, 2570, 2550, 28329, 12605,
			12692, 12603, 12691, 28307, 33182, 6735, 11772, 19550,
			19710,

			// === Ammo slot ===
			30384, 27544,

			// === Food ===
			33118, 2297, 13441, 2323, 24592, 24595, 24589, 20862,
			1967, 7054, 2185, 339, 3144, 23533, 24785, 2343,
			25958, 31174, 25960, 2011, 11936, 7056, 4423, 4421,
			4419, 4417, 347, 27351, 20879, 20868, 379, 355,
			391, 2327, 2293, 7946, 20881, 7058, 20864, 23874,
			20877, 2301, 2289, 6703, 6705, 2229, 2221, 2231,
			2235, 2219, 2233, 20883, 20866, 329, 325, 397,
			385, 20390, 33628, 373, 2187, 2255, 333, 26149,
			7068, 7060, 1885, 29900, 2195, 2253, 2191,

			// === Potions ===
			27349, 27347, 2458, 2456, 2454, 2452, 5785, 11431,
			11429, 125, 123, 121, 2428, 29207, 29189, 29640,
			29637, 29634, 29631, 24774, 25162, 25161, 25160, 25159,
			11447, 11445, 13064, 9745, 26153, 9743, 26152, 9741,
			26151, 9739, 26150, 11459, 11457, 137, 135, 133,
			2432, 23706, 23703, 23700, 23697, 23694, 23691, 23688,
			23685, 23730, 23727, 23724, 23721, 23718, 23715, 23712,
			23709, 5809, 20921, 20922, 20923, 20924, 20913, 20914,
			20915, 20916, 20917, 20918, 20919, 20920, 27341, 27339,
			27211, 27208, 27205, 27202, 29083, 29082, 29081, 29080,
			27321, 27319, 27317, 27315, 20993, 20994, 20995, 20996,
			20985, 20986, 20987, 20988, 11733, 20989, 11732, 20990,
			11731, 20991, 11730, 20992, 25206, 25205, 25204, 25203,
			29204, 29186, 29198, 29180, 6691, 23581, 6689, 23579,
			6687, 23577, 6685, 23575, 27323, 27343, 29201, 29183,
			11441, 11443, 119, 117, 115, 113, 29210, 29192,
			149, 147, 145, 2436, 12701, 23549, 12699, 23547,
			12697, 23545, 12695, 23543, 11499, 11497, 167, 165,
			163, 2442, 11487, 11485, 161, 159, 157, 2440,
			11471, 11469, 30884, 30881, 30878, 30875, 27333, 27331,
			27329, 27327, 187, 5937, 5940, 20981, 20982, 20983,
			20984, 20973, 20974, 20975, 20976, 20977, 20978, 20979,
			20980, 193, 191, 189, 2450, 11523, 11521,

			// === Training & utility ===
			28080, 13020, 13022, 13012, 13014, 13016, 13018, 26370,
			7804, 13060, 13062, 19677, 29799, 11810, 13052, 13054,
			22477, 11812, 13056, 13058, 26394, 28074, 25639, 28293,
			28279, 28295, 12996, 12998, 12988, 12990, 21730, 12992,
			12994, 10014, 31136, 24777, 13276, 13275, 13274, 27283,
			22963, 28813, 30324, 12968, 12970, 12960, 12962, 12964,
			12966, 29574, 27667, 23831, 23868, 12877, 11286, 21882,
			21885, 28078, 28076, 23667, 33627, 7509, 28942, 12819,
			25859, 33534, 28319, 27285, 28321, 22744, 13036, 13038,
			12873, 13048, 13050, 21287, 9668, 24488, 12980, 12982,
			12972, 12974, 12976, 12978, 30893, 27289, 22438, 12004,
			28325, 13008, 13010, 13000, 13002, 13004, 13006, 29792,
			29790, 29794, 30744, 21279, 24229, 13231, 9670, 9666,
			31151, 10020, 13024, 13026, 13032, 13034, 13028, 13030,
			13040, 13042, 11814, 12804, 26132, 26009, 26012, 26126,
			26006, 26135, 29670, 26060, 29664, 26075, 25997, 28508,
			29673, 28323, 29583, 29585, 10016, 12823, 20382, 20385,
			12984, 12986, 20376, 20379, 29424, 13066, 12879, 31145,
			28287, 28285, 12875, 27684, 27687, 27681, 28301, 24859,
			13044, 13046, 11816, 6722,
			// Separate-ID variants (degradation / charges / trims) + bird
			// nests, ported from the PR #2/#4 era data (pre-regen)
			4880, 4881, 4882, 4883, 4884, 4886, 4887, 4888,
			4889, 4890, 4892, 4893, 4894, 4895, 4896, 4898,
			4899, 4900, 4901, 4902, 4904, 4905, 4906, 4907,
			4908, 4910, 4911, 4912, 4913, 4914, 4916, 4917,
			4918, 4919, 4920, 4922, 4923, 4924, 4925, 4926,
			4952, 4953, 4954, 4955, 4956, 4958, 4959, 4960,
			4961, 4962, 4964, 4965, 4966, 4967, 4968, 4970,
			4971, 4972, 4973, 4974, 4976, 4977, 4978, 4979,
			4980, 4982, 4983, 4984, 4985, 4986, 4988, 4989,
			4990, 4991, 4992, 4994, 4995, 4996, 4997, 4998,
			4225, 4226, 4227, 4228, 4229, 4230, 4231, 4232,
			4233, 4234, 7640, 7645, 8901, 8903, 8913, 9748,
			9751, 9754, 9769, 11760, 11761, 11762, 11763, 11764,
			11765, 11766, 11767, 11768, 11769, 13092, 13093, 13094,
			13095, 13096, 13097, 13098, 13099, 13100, 13101, 20655,
			20657, 22545, 23989, 23993, 23997, 24125, 24127, 26376,
			26378, 26380, 27902, 27906, 27910, 27925, 27928, 27931,
			27934, 27937, 28327, 29043, 29045, 29047, 29067, 29070,
			29073, 30305
		));
	}

	private static void addRange(Map<String, List<Integer>> m)
	{
		// RANGE — 854 items
		//   Bows (66), Arrows (43), Crossbows (26), Bolts (61), Ballistae &
		//   javelins (15), Blowpipe & darts (16), Knives (10), Morrigan's javelins
		//   (3), Other throwables (31), Head (71), Body (72), Legs (74), Hands
		//   (47), Feet (17), Capes (16), Shields & off-hands (29), Neck (30),
		//   Rings (9), Food (57), Potions (95), Training & utility (66)
		m.put(TAG_RANGE, Arrays.asList(
			// === Bows ===
			12424, 28794, 25865, 25867, 25884, 25886, 25888, 25890,
			25892, 25894, 25896, 27187, 33021, 4827, 23856, 23855,
			23857, 29599, 28540, 22547, 23983, 23902, 23901, 23903,
			11235, 20408, 27853, 29611, 30434, 30436, 29000, 29851,
			839, 10284, 859, 861, 12788, 851, 853, 33245,
			4212, 4213, 11748, 845, 843, 2883, 23357, 29591,
			6724, 841, 22333, 9705, 20997, 27610, 31133, 27612,
			27655, 27652, 10280, 847, 849, 10282, 855, 857,
			26237, 26239,

			// === Arrows ===
			890, 20525, 4798, 21326, 21328, 28991, 29852, 22230,
			4788, 22229, 4160, 882, 11700, 4773, 22227, 30694,
			11212, 20389, 11217, 22228, 884, 11701, 4778, 888,
			11703, 4793, 2866, 892, 20607, 4803, 33577, 33589,
			33601, 33553, 33595, 33559, 33571, 33583, 33565, 886,
			11702, 4783, 9706,

			// === Crossbows ===
			9183, 11785, 23611, 9176, 9174, 837, 8880, 21902,
			33460, 28053, 21012, 25918, 25916, 10156, 28869, 9177,
			4734, 33251, 9181, 767, 9185, 23601, 26486, 9179,
			26374, 27186,

			// === Bolts ===
			9143, 21316, 881, 9139, 4740, 8882, 11875, 877,
			30696, 9340, 9243, 23649, 21969, 21946, 21905, 9341,
			9244, 21971, 21948, 9338, 9241, 21965, 21942, 9140,
			9335, 9237, 21957, 21934, 10158, 10159, 9142, 28878,
			9342, 9245, 21973, 21950, 879, 9236, 21955, 21932,
			27192, 880, 9238, 21959, 21936, 9339, 9242, 21967,
			21944, 9144, 9337, 9240, 21963, 21940, 9145, 9141,
			28872, 9336, 9239, 21961, 21938,

			// === Ballistae & javelins ===
			829, 21318, 825, 19484, 23648, 19481, 23630, 26712,
			826, 19478, 27188, 828, 830, 33801, 827,

			// === Blowpipe & darts ===
			810, 25849, 3093, 28688, 806, 31575, 11230, 30374,
			807, 31579, 809, 31583, 811, 808, 12926, 12934,

			// === Knives ===
			867, 869, 864, 22804, 22812, 27157, 863, 866,
			868, 865,

			// === Morrigan's javelins ===
			22636, 23619, 27916,

			// === Other throwables ===
			804, 11959, 10148, 33716, 800, 10033, 20849, 10142,
			10145, 732, 29305, 28837, 801, 10143, 803, 22634,
			27912, 7170, 10146, 10034, 10147, 805, 28773, 33790,
			802, 10149, 10144, 28834, 6522, 28922, 28919,

			// === Head ===
			19641, 11783, 11782, 11781, 11779, 11778, 11777, 11776,
			11784, 27705, 27717, 27729, 27741, 27753, 27765, 27777,
			33031, 33170, 23887, 23886, 25495, 23888, 23971, 27201,
			26714, 23841, 23840, 23842, 27847, 28904, 29010, 29842,
			23258, 19687, 30073, 26741, 26739, 26737, 27366, 27226,
			27235, 27836, 26475, 1167, 19645, 21266, 19649, 11865,
			21890, 24444, 1169, 13379, 6326, 2581, 6131, 11664,
			3749, 10550, 23075, 10334, 12496, 12512, 11826, 12504,
			10382, 4732, 10390, 10374, 22638, 9770, 9758,

			// === Body ===
			2503, 20423, 12381, 12385, 27697, 27709, 27721, 27733,
			27745, 27757, 27769, 33023, 33166, 23890, 23889, 25496,
			23891, 23975, 27199, 26715, 19689, 23844, 23843, 23845,
			27848, 29004, 29840, 26469, 30076, 27229, 27238, 33190,
			29280, 27837, 27179, 26264, 26463, 1129, 23381, 11899,
			1131, 13381, 1133, 7362, 7364, 10954, 6322, 23264,
			1135, 7370, 7372, 12596, 6133, 13072, 8839, 2499,
			7374, 7376, 2501, 12327, 12331, 10330, 12492, 11828,
			12508, 12500, 10378, 4736, 23632, 10386, 10370, 22641,

			// === Legs ===
			25493, 2497, 20424, 12383, 12387, 27701, 27713, 27725,
			27737, 27749, 27761, 27773, 33027, 33168, 23893, 23892,
			25497, 23894, 23979, 27200, 26716, 19693, 23847, 23846,
			23848, 27849, 29007, 29841, 26471, 27180, 30079, 23384,
			27232, 27241, 33192, 29283, 27838, 27182, 26465, 27181,
			1095, 11900, 13380, 1097, 7366, 7368, 10824, 10956,
			6324, 23267, 1099, 7378, 7380, 23249, 6135, 13073,
			8840, 2493, 7382, 7384, 10555, 2495, 12329, 12333,
			10332, 12494, 11830, 12510, 12502, 10380, 4738, 10388,
			10372, 22644,

			// === Hands ===
			7454, 7455, 7456, 25494, 7457, 2491, 10085, 7458,
			7459, 7460, 7461, 7462, 23593, 11126, 11124, 11122,
			11120, 11118, 11974, 11972, 7453, 30082, 11133, 6149,
			30386, 26467, 26235, 1063, 10077, 13377, 6330, 23261,
			1065, 10079, 19994, 8842, 2487, 10081, 2489, 10083,
			10336, 12490, 12506, 12498, 10376, 10384, 10368,

			// === Feet ===
			29806, 33202, 31092, 29286, 13378, 10958, 6328, 2577,
			6143, 19921, 19930, 19924, 22951, 19927, 19933, 19936,
			13237,

			// === Capes ===
			10498, 28955, 1052, 28902, 28951, 28947, 27374, 27363,
			29289, 6568, 11901, 10499, 22109, 13337, 21898, 9756,

			// === Shields & off-hands ===
			22284, 1540, 22002, 22003, 32879, 3844, 12610, 26492,
			3839, 3841, 3843, 12609, 3840, 33188, 3842, 22269,
			22272, 22275, 22278, 11926, 12807, 22281, 23197, 23200,
			23203, 23188, 23191, 23194, 21000,

			// === Neck ===
			22111, 1478, 22557, 1729, 19707, 6585, 23640, 12436,
			1704, 20586, 10362, 10354, 1706, 1708, 1710, 1712,
			11976, 11978, 1731, 20585, 23354, 12851, 27172, 33639,
			11090, 12018, 12017, 19547, 22249, 22986,

			// === Rings ===
			6733, 11771, 22975, 2570, 2550, 28329, 28310, 19550,
			19710,

			// === Food ===
			2297, 13441, 2323, 24592, 24595, 24589, 20862, 7054,
			2185, 3144, 23533, 2343, 2011, 11936, 7056, 347,
			20879, 20868, 379, 355, 391, 2327, 2293, 7946,
			20881, 7058, 20864, 23874, 20877, 2301, 2289, 6703,
			6705, 2229, 2221, 2231, 2235, 2219, 2233, 20883,
			20866, 329, 325, 397, 385, 20390, 373, 2187,
			2255, 333, 7068, 7060, 1885, 2195, 7208, 2253,
			2191,

			// === Potions ===
			2458, 2456, 2454, 2452, 31659, 31656, 31653, 31650,
			22470, 22467, 22464, 22461, 29640, 29637, 29634, 29631,
			25162, 25161, 25160, 25159, 24644, 24641, 24638, 24635,
			23742, 23739, 23736, 23733, 25826, 20993, 20994, 20995,
			20996, 20985, 20986, 20987, 20988, 11733, 20989, 11732,
			20990, 11731, 20991, 11730, 20992, 25206, 25205, 25204,
			25203, 11511, 11509, 173, 23557, 171, 23555, 169,
			23553, 2444, 23551, 6691, 23581, 6689, 23579, 6687,
			23577, 6685, 23575, 11725, 11724, 11723, 11722, 20933,
			20934, 20935, 20936, 20925, 20926, 20927, 20928, 20929,
			20930, 20931, 20932, 20981, 20982, 20983, 20984, 20973,
			20974, 20975, 20976, 20977, 20978, 20979, 20980,

			// === Training & utility ===
			19578, 21352, 13171, 28298, 13169, 27269, 13167, 12871,
			12867, 9433, 13193, 19570, 10, 26524, 6, 26520,
			12, 26526, 8, 26522, 23956, 28072, 30626, 21034,
			28826, 27012, 19582, 12863, 31142, 25859, 27670, 23124,
			12865, 31235, 13165, 31169, 19572, 12883, 27355, 9419,
			19576, 31166, 30390, 30392, 26372, 26231, 11928, 11929,
			11930, 13229, 24861, 12869, 19580, 13163, 29667, 29676,
			26000, 26072, 31157, 2, 19574, 28289, 27614, 28283,
			21907, 13161,
			// Separate-ID variants (degradation / charges / trims) + bird
			// nests, ported from the PR #2/#4 era data (pre-regen)
			4928, 4929, 4930, 4931, 4932, 4934, 4935, 4936,
			4937, 4938, 4940, 4941, 4942, 4943, 4944, 4946,
			4947, 4948, 4949, 4950, 4214, 4215, 4216, 4217,
			4218, 4219, 4220, 4221, 4222, 4223, 9757, 12924,
			20655, 20657, 22550, 23973, 23977, 23981, 23985, 24123,
			25862, 27699, 27703, 27707, 27711, 27715, 27719, 27723,
			27727, 27731, 27735, 27739, 27743, 27747, 27751, 27755,
			27759, 27763, 27767, 27771, 27775, 27779, 27914, 27918,
			27940, 27943, 27946, 28327, 28687, 28949, 28953, 29031,
			29033, 29035, 29049, 29052, 29055, 30373, 33025, 33029,
			33033
		));
	}

	private static void addMage(Map<String, List<Integer>> m)
	{
		// MAGE — 875 items
		//   Runes (39), Weapons (150), Off-hands, books & tomes (54), Head (90),
		//   Body (62), Legs (60), Hands (29), Feet (21), Capes (24), Neck (34),
		//   Rings (15), Spell utility & supplies (119), Food (57), Potions (108),
		//   Enchanting & skilling magic (13)
		m.put(TAG_MAGE, Arrays.asList(
			// === Runes ===
			556, 11688, 558, 11690, 555, 11687, 557, 11689,
			554, 11686, 559, 11691, 564, 11696, 562, 11694,
			561, 11693, 563, 11695, 560, 11692, 565, 11697,
			566, 11698, 9075, 11699, 21880, 22208, 30843, 27293,
			4696, 4699, 4695, 4698, 4697, 4694, 28929,

			// === Weapons ===
			10148, 6603, 30070, 23899, 23898, 23900, 27665, 27679,
			27676, 27662, 27624, 1391, 28988, 29849, 28796, 23853,
			23852, 23854, 28547, 28549, 29602, 22516, 30568, 31113,
			31115, 25731, 25733, 33330, 33332, 33255, 33257, 9091,
			9092, 9093, 1389, 30390, 30392, 29594, 33184, 33253,
			9013, 21276, 1379, 23363, 22335, 27788, 27785, 33036,
			33035, 33326, 33323, 33322, 33318, 33314, 27275, 27277,
			30634, 33320, 33316, 33328, 33434, 25517, 29609, 28585,
			28583, 27920, 1397, 1399, 1393, 1381, 1385, 1387,
			1383, 1395, 22368, 20736, 3053, 21198, 20730, 6562,
			11998, 11787, 12795, 10149, 1405, 20739, 1407, 1401,
			3054, 21200, 20733, 6563, 12000, 11789, 12796, 1403,
			8841, 6908, 4675, 20431, 6910, 1409, 12658, 10146,
			6912, 4170, 21255, 12199, 12263, 12275, 10442, 2416,
			6914, 10147, 10440, 2415, 22555, 22552, 6526, 10444,
			2417, 23342, 12422, 9084, 24422, 4710, 23653, 24425,
			24423, 21006, 23626, 22323, 22481, 24144, 22296, 11791,
			23613, 12902, 12904, 22288, 11905, 12899, 22292, 12900,
			22294, 11908, 22290, 24424, 22647, 23617,

			// === Off-hands, books & tomes ===
			1540, 26551, 3844, 12612, 26490, 25818, 3839, 3841,
			3843, 12611, 30371, 2890, 25985, 27251, 33176, 27253,
			3840, 9731, 30064, 27358, 25574, 3842, 6235, 6257,
			6279, 6233, 6255, 6277, 6231, 6253, 6275, 6229,
			6251, 6273, 6225, 6247, 6269, 6223, 6245, 6267,
			6221, 6243, 6265, 6219, 6241, 6263, 20714, 6889,
			23652, 11924, 12806, 21633, 21634, 12825,

			// === Head ===
			12453, 12455, 19641, 11783, 11782, 11781, 11779, 11778,
			11777, 11776, 11784, 23887, 23886, 23888, 27183, 25518,
			29019, 29845, 579, 7394, 7396, 23841, 23840, 23842,
			27850, 27123, 29566, 30445, 30437, 27176, 27119, 9729,
			3797, 6109, 27166, 30111, 23522, 9733, 9069, 9068,
			26531, 6885, 26731, 26735, 26733, 26241, 26473, 1017,
			27839, 11898, 19645, 21266, 19649, 11865, 21890, 24444,
			13385, 12203, 12259, 12271, 20595, 7400, 10454, 20128,
			4089, 4099, 23047, 4109, 10452, 6137, 3385, 10456,
			11663, 3755, 10547, 12457, 6918, 12419, 25398, 25413,
			23075, 10342, 9096, 4708, 24288, 21018, 24664, 22650,
			9770, 9764,

			// === Body ===
			581, 12449, 12451, 23890, 23889, 23891, 27193, 29013,
			29843, 577, 7390, 7392, 23844, 23843, 23845, 27851,
			27125, 29568, 30447, 30439, 27174, 27115, 26469, 6107,
			27167, 9070, 27158, 27160, 26533, 26243, 33196, 26463,
			1035, 27840, 11896, 13387, 20517, 7399, 4091, 20425,
			4101, 23050, 4111, 20131, 6139, 3387, 13072, 8839,
			12458, 6916, 12420, 25389, 25404, 10338, 20576, 9097,
			4712, 20598, 24291, 21021, 24666, 22653,

			// === Legs ===
			12445, 12447, 23893, 23892, 23894, 27194, 29016, 29844,
			7386, 7388, 23847, 23846, 23848, 27852, 27127, 29570,
			30449, 30441, 27175, 27117, 26471, 6108, 27168, 9071,
			27159, 27161, 26535, 26245, 33198, 26465, 1033, 27841,
			11897, 13389, 20520, 7398, 4093, 20426, 4103, 23053,
			4113, 20137, 6141, 3389, 13073, 8840, 12459, 6924,
			12421, 25401, 25416, 10340, 20577, 9098, 4714, 20599,
			24294, 21024, 24668, 22656,

			// === Hands ===
			777, 11126, 11124, 11122, 11120, 11118, 11974, 11972,
			31106, 6110, 9072, 26537, 11133, 6153, 27171, 26467,
			20134, 4095, 4105, 23056, 4115, 3391, 8842, 6922,
			25392, 25407, 9099, 19544, 23444,

			// === Feet ===
			6106, 27170, 9073, 26539, 10839, 6147, 27162, 2579,
			20140, 4097, 4107, 23059, 4117, 3393, 6920, 25395,
			25410, 9100, 22951, 13235, 23644,

			// === Capes ===
			1052, 6111, 29615, 29617, 29613, 9074, 6568, 21793,
			23603, 21791, 23607, 21795, 23605, 2413, 2412, 2414,
			9101, 13335, 21784, 21776, 21780, 9762, 13331, 13333,

			// === Neck ===
			22111, 1478, 22557, 1729, 19707, 6585, 23640, 12436,
			1704, 20586, 10362, 10354, 1706, 1708, 1710, 1712,
			11976, 11978, 1727, 10366, 1731, 20585, 23354, 12851,
			29486, 9102, 11090, 12018, 12017, 10344, 12002, 23654,
			19720, 22986,

			// === Rings ===
			22975, 13126, 13127, 13128, 9104, 28313, 33180, 2570,
			2550, 28329, 6731, 11770, 23624, 19550, 19710,

			// === Spell utility & supplies ===
			6894, 6895, 12881, 11715, 12728, 21049, 27616, 27627,
			20430, 12621, 12622, 12623, 12624, 21079, 12827, 21697,
			24607, 24609, 24613, 24611, 26705, 24615, 24621, 26704,
			28268, 11714, 31163, 31139, 13159, 20718, 20523, 11712,
			12738, 8890, 23833, 23870, 6899, 6898, 24333, 11713,
			21799, 30640, 27281, 6903, 11717, 12732, 24517, 20524,
			6896, 21798, 13227, 30631, 11718, 12734, 24217, 24511,
			30628, 28270, 6900, 20724, 21797, 21043, 12004, 6893,
			28291, 28281, 11931, 11932, 11933, 12736, 31109, 23113,
			23116, 23119, 23110, 30627, 6902, 6901, 30806, 6897,
			12791, 23650, 27086, 30692, 25481, 25478, 27641, 28931,
			28304, 28272, 29679, 26066, 28484, 28517, 33054, 26078,
			26003, 31154, 27673, 28274, 25578, 30068, 2396, 31160,
			12846, 8022, 27279, 28570, 28561, 21698, 21704, 21707,
			21701, 567, 31148, 24514, 11716, 12730, 24860,

			// === Food ===
			2297, 13441, 2323, 24592, 24595, 24589, 20862, 7054,
			2185, 3144, 23533, 2343, 2011, 11936, 7056, 347,
			20879, 20868, 379, 355, 391, 2327, 2293, 7946,
			20881, 7058, 20864, 23874, 6883, 20877, 2301, 2289,
			6703, 6705, 2229, 2221, 2231, 2235, 2219, 2233,
			20883, 20866, 329, 325, 397, 385, 20390, 373,
			2187, 2255, 333, 7068, 7060, 1885, 2195, 2253,
			2191,

			// === Potions ===
			26346, 26344, 26342, 26340, 26353, 26350, 2458, 2456,
			2454, 2452, 22458, 22455, 22452, 22449, 29640, 29637,
			29634, 29631, 25162, 25161, 25160, 25159, 24632, 24629,
			24626, 24623, 23754, 23751, 23748, 23745, 27638, 27635,
			27632, 27629, 20945, 20946, 20947, 20948, 20937, 20938,
			20939, 20940, 20941, 20942, 20943, 20944, 31814, 11491,
			11489, 9024, 9023, 9022, 9021, 11515, 11513, 3046,
			3044, 3042, 3040, 5741, 5801, 5881, 20993, 20994,
			20995, 20996, 20985, 20986, 20987, 20988, 11733, 20989,
			11732, 20990, 11731, 20991, 11730, 20992, 25206, 25205,
			25204, 25203, 6691, 23581, 6689, 23579, 6687, 23577,
			6685, 23575, 29412, 11729, 11728, 11727, 11726, 1907,
			20981, 20982, 20983, 20984, 20973, 20974, 20975, 20976,
			20977, 20978, 20979, 20980,

			// === Enchanting & skilling magic ===
			6904, 6905, 6906, 8014, 8015, 30384, 8019, 8020,
			8017, 8021, 8018, 8016, 21257,
			// Separate-ID variants (degradation / charges / trims) + bird
			// nests, ported from the PR #2/#4 era data (pre-regen)
			4856, 4857, 4858, 4859, 4860, 4862, 4863, 4864,
			4865, 4866, 4868, 4869, 4870, 4871, 4872, 4874,
			4875, 4876, 4877, 4878, 1410, 6215, 6217, 6227,
			6237, 6239, 6249, 6259, 6261, 6271, 9763, 11907,
			20655, 20657, 20716, 22370, 25576, 27922, 27949, 27952,
			27955, 28327, 29037, 29039, 29041, 29058, 29061, 29064,
			30066, 30519, 30521, 30523, 30525, 30527, 30529, 30531,
			30533, 30535, 30537, 30539, 30541, 30543, 30545, 30547,
			30570, 30571, 30572, 30573, 30574
		));
	}

	private static void addPrayer(Map<String, List<Integer>> m)
	{
		// PRAYER — 538 items
		//   Prayer equipment & robes (176), Bones & ashes (67), Ensouled heads
		//   (23), Bonemeal & offerings (42), Fossils & enriched bones (31), Pyre
		//   logs & shade remains (29), Prayer-restoring consumables (90), Holy
		//   symbols, books & blessings (71), Bone-processing utility (9)
		m.put(TAG_PRAYER, Arrays.asList(
			// === Prayer equipment & robes ===
			12437, 23345, 23339, 23336, 23342, 6585, 26229, 26227,
			26223, 26225, 26221, 12197, 12199, 19921, 23197, 24201,
			11061, 12203, 12195, 12193, 12201, 31081, 12825, 13121,
			13122, 13123, 13124, 12261, 12263, 19930, 23200, 24192,
			12259, 12255, 12253, 12257, 12273, 12275, 19924, 23203,
			24195, 12271, 12267, 12265, 12269, 22986, 24204, 22954,
			22111, 538, 540, 28945, 24425, 27251, 27253, 29562,
			12817, 13117, 13118, 13119, 13120, 10376, 10380, 10448,
			10382, 10442, 10378, 19927, 23188, 12639, 27163, 10454,
			10466, 10462, 10472, 10828, 30111, 33002, 12598, 19997,
			5576, 9668, 5575, 5574, 542, 20202, 23306, 544,
			20199, 23303, 21157, 9759, 9761, 426, 428, 9676,
			9670, 9666, 9674, 9672, 9678, 12601, 13202, 10384,
			10388, 10446, 10390, 10440, 10386, 19933, 23191, 11806,
			20372, 12637, 27165, 10452, 10464, 10458, 10470, 24198,
			548, 546, 25344, 12821, 12829, 29424, 28939, 28936,
			28933, 30367, 4757, 4755, 4753, 4759, 8841, 6609,
			6589, 6619, 6615, 6587, 6591, 6623, 6629, 6599,
			6633, 6607, 6601, 6603, 6621, 6617, 6625, 6627,
			6611, 6631, 6605, 6613, 10368, 10372, 10450, 10374,
			10444, 10370, 19936, 23194, 12638, 27164, 10456, 1033,
			1035, 10468, 10460, 10474, 25440, 25438, 25436, 25434,

			// === Bones & ashes ===
			25775, 31075, 30973, 534, 530, 3182, 532, 25422,
			29352, 29354, 29346, 29348, 29344, 29356, 29366, 29370,
			31266, 29368, 29358, 29374, 29372, 31264, 29362, 29364,
			29360, 29350, 526, 2530, 3187, 33115, 528, 3127,
			11338, 6729, 29376, 536, 22783, 4830, 25766, 31729,
			30898, 3181, 22786, 25778, 3125, 3186, 11943, 25772,
			11337, 3130, 3180, 3183, 4834, 3128, 4832, 3123,
			3179, 3185, 31726, 29378, 22124, 25769, 2859, 22780,
			28899, 6812, 4812,

			// === Ensouled heads ===
			13508, 13505, 13463, 13496, 13472, 13493, 13502, 13469,
			13511, 13481, 13475, 13448, 26997, 13487, 13454, 13490,
			13457, 13451, 13478, 13460, 13484, 13499, 13466,

			// === Bonemeal & offerings ===
			30975, 4260, 4256, 4266, 4257, 5076, 4255, 4286,
			4258, 4259, 25672, 25660, 6728, 4261, 22756, 4278,
			25340, 4853, 31335, 4265, 25654, 22758, 4271, 4269,
			11922, 4264, 4267, 4855, 4854, 5615, 4270, 4263,
			4268, 33231, 31333, 22116, 25666, 29958, 4262, 22754,
			6810, 4852,

			// === Fossils & enriched bones ===
			21543, 21551, 21600, 21606, 21604, 21608, 21602, 21549,
			21580, 21586, 21584, 21588, 21582, 21545, 21553, 21610,
			21616, 21614, 21618, 21612, 21620, 21547, 21570, 21576,
			21574, 21578, 21572, 21566, 21564, 21568, 21562,

			// === Pyre logs & shade remains ===
			3402, 25463, 25459, 31383, 3404, 25467, 31386, 3396,
			3448, 3444, 3440, 3428, 3426, 3424, 3422, 3398,
			3438, 19672, 3400, 31389, 3436, 3434, 3432, 3430,
			25465, 25461, 25419, 3442, 3446,

			// === Prayer-restoring consumables ===
			27349, 27347, 26346, 26344, 26342, 26340, 26353, 26350,
			27335, 24605, 24603, 24601, 24598, 23882, 23883, 23884,
			23885, 27638, 27635, 27632, 27629, 27351, 29382, 28893,
			29213, 29195, 29083, 29082, 29081, 29080, 20969, 20970,
			20971, 20972, 20961, 20962, 20963, 20964, 20965, 20966,
			20967, 20968, 11467, 11465, 143, 20396, 141, 20395,
			139, 20394, 2434, 20393, 30134, 30131, 30128, 30125,
			20957, 20958, 20959, 20960, 20949, 20950, 20951, 20952,
			20953, 20954, 20955, 20956, 10931, 23565, 10929, 23563,
			10927, 23561, 10925, 23559, 11495, 11493, 3030, 23573,
			3028, 23571, 3026, 23569, 3024, 23567, 27333, 27331,
			27329, 27327,

			// === Holy symbols, books & blessings ===
			20235, 21079, 12827, 29381, 29338, 12831, 3844, 26488,
			13153, 12612, 26490, 12610, 26492, 25818, 12608, 26494,
			3839, 3841, 3843, 12607, 12609, 12611, 30626, 21034,
			12819, 3835, 3836, 3837, 3838, 20220, 3840, 26496,
			13149, 12833, 1718, 6714, 20229, 29384, 29386, 30627,
			20226, 10890, 22943, 22945, 22947, 28775, 33010, 3827,
			3828, 3829, 3830, 26099, 33060, 28520, 26129, 19634,
			12823, 21047, 1716, 20223, 3842, 27191, 26498, 13151,
			1724, 1722, 20232, 3831, 3832, 3833, 3834,

			// === Bone-processing utility ===
			25781, 13116, 29088, 33627, 28132, 28942, 29587, 33695,
			21907,
			// Separate-ID variants (degradation / charges / trims) + bird
			// nests, ported from the PR #2/#4 era data (pre-regen)
			4976, 4977, 4978, 4979, 4980, 4982, 4983, 4984,
			4985, 4986, 4988, 4989, 4990, 4991, 4992, 4994,
			4995, 4996, 4997, 4998, 9760
		));
	}

	private static void addTeleports(Map<String, List<Integer>> m)
	{
		// TELEPORTS — 268 items
		//   Teleport runes (14), Mounted & charged jewellery (77), Spellbook
		//   tablets (31), Teleport scrolls (34), Diary & reward teleports (41),
		//   Skill destinations (9), Wilderness teleports (8), Quest-locked
		//   teleports (31), Special & one-time (23)
		m.put(TAG_TELEPORTS, Arrays.asList(
			// === Teleport runes ===
			556, 11688, 9075, 11699, 557, 11689, 554, 11686,
			563, 11695, 566, 11698, 555, 11687,

			// === Mounted & charged jewellery ===
			19707, 10362, 10354, 1706, 1708, 1710, 1712, 11976,
			11978, 26914, 21175, 21173, 21171, 21166, 11126, 11124,
			11122, 11120, 11118, 11974, 11972, 11190, 11191, 11192,
			11194, 22400, 3867, 3865, 3863, 3859, 3857, 3855,
			3853, 30638, 30637, 28765, 13391, 21155, 21153, 21151,
			21146, 29893, 2566, 2564, 2562, 2558, 2556, 2554,
			2552, 21138, 21136, 21134, 21129, 28329, 26815, 11988,
			11986, 11984, 11980, 12785, 20786, 32399, 11113, 11111,
			11109, 11107, 11970, 11968, 11873, 11872, 11871, 11869,
			11868, 11867, 11866, 21268, 13393,

			// === Spellbook tablets ===
			19631, 19613, 8011, 24955, 19629, 22949, 8010, 24961,
			28824, 19615, 8009, 19621, 24959, 29684, 19625, 12779,
			24957, 28790, 12780, 8008, 19617, 24949, 24951, 12781,
			19619, 12782, 8013, 8007, 8012, 24953, 19623,

			// === Teleport scrolls ===
			30149, 1505, 11745, 30775, 13660, 30040, 12403, 12404,
			19651, 12410, 13249, 12642, 12405, 21387, 12406, 12411,
			12402, 12407, 12408, 11743, 23771, 11744, 11741, 11740,
			29782, 12409, 24336, 11742, 13658, 11747, 21541, 23387,
			11746, 12938,

			// === Diary & reward teleports ===
			19476, 13121, 13122, 13123, 13124, 20760, 13134, 13135,
			13136, 13126, 13127, 13128, 13129, 13130, 13131, 13132,
			27550, 27552, 25926, 25928, 25930, 25932, 25934, 25936,
			13139, 13140, 11140, 13103, 13112, 13113, 13114, 13115,
			13221, 22941, 22943, 22945, 22947, 13143, 13144, 13110,
			13111,

			// === Skill destinations ===
			29271, 29090, 29273, 9469, 22599, 29275, 33120, 19564,
			22601,

			// === Wilderness teleports ===
			12775, 12776, 19627, 12777, 12778, 24963, 21802, 24251,

			// === Quest-locked teleports ===
			21770, 23858, 6103, 772, 4251, 23959, 23946, 21766,
			21768, 21760, 21762, 31099, 9044, 9046, 9048, 9050,
			13074, 13075, 13076, 13077, 26948, 26945, 21772, 9013,
			21276, 23904, 6102, 6101, 6100, 13102, 21764,

			// === Special & one-time ===
			19677, 21046, 30966, 20238, 981, 10972, 3690, 3691,
			6125, 6126, 13079, 23458, 22517, 28333, 11060, 24709,
			26577, 26549, 28332, 28331, 25837, 28330, 31443,
			// Separate-ID variants (degradation / charges / trims) + bird
			// nests, ported from the PR #2/#4 era data (pre-regen)
			2560, 3861, 6099, 6127, 11105, 11193, 11870, 13222,
			13392, 20787, 20788, 20789, 20790, 21132, 21149, 21169,
			26818, 26950, 28327, 29892, 32398
		));
	}

	private static void addCooking(Map<String, List<Integer>> m)
	{
		// COOKING — 780 items
		//   Raw fish (51), Cooked fish (54), Burnt fish (33), Raw meat (53),
		//   Cooked meat (52), Burnt meat (21), Ingredients (207), Combo food (82),
		//   Baked & cooked goods (82), Drinks & brews (108), Cooking tools &
		//   utensils (20), Burnt food (17)
		m.put(TAG_COOKING, Arrays.asList(
			// === Raw fish ===
			31674, 31564, 31677, 321, 13439, 363, 32341, 20861,
			25670, 5001, 25658, 341, 11934, 7529, 32309, 25652,
			32317, 32333, 345, 31561, 3142, 20867, 2148, 20859,
			377, 353, 389, 32349, 7944, 20863, 23872, 349,
			20855, 10138, 20865, 331, 327, 395, 383, 317,
			2514, 3379, 20857, 371, 31553, 25664, 335, 359,
			32325, 31671, 31556,

			// === Cooked fish ===
			25960, 319, 13441, 365, 32344, 20862, 25672, 5003,
			25660, 11326, 339, 29217, 7530, 3381, 25958, 31174,
			11936, 10971, 32312, 25654, 32320, 32336, 25565, 347,
			21394, 20868, 2149, 20860, 10970, 379, 355, 391,
			32352, 7946, 20864, 23874, 351, 3146, 20856, 10136,
			20866, 13339, 329, 325, 397, 385, 315, 20858,
			373, 25666, 333, 361, 32360, 32328,

			// === Burnt fish ===
			13443, 32347, 5002, 11938, 3383, 323, 343, 357,
			367, 369, 20854, 23873, 7531, 32315, 32323, 32339,
			31567, 3148, 381, 393, 32355, 7948, 10140, 399,
			387, 7954, 375, 31559, 32331, 25674, 25662, 25656,
			25668,

			// === Raw meat ===
			3367, 7518, 3365, 29101, 2136, 9986, 2132, 4287,
			9978, 31692, 25833, 2138, 4289, 2876, 29107, 20874,
			29119, 20870, 7543, 33821, 7566, 20878, 29125, 29122,
			29113, 29076, 20880, 24782, 2337, 6178, 20876, 20872,
			20882, 29110, 3226, 31700, 2134, 31686, 7577, 29116,
			33106, 1859, 29104, 10816, 9992, 9984, 7230, 7224,
			6291, 6295, 6293, 3363, 2341,

			// === Cooked meat ===
			10964, 31695, 10963, 29131, 2140, 4291, 2878, 7228,
			29134, 7521, 29149, 7568, 29152, 29146, 2142, 4293,
			29143, 29077, 24785, 2343, 29137, 3228, 29140, 33109,
			29128, 3373, 10969, 10965, 4517, 20875, 20871, 20879,
			3371, 9052, 7070, 20881, 20877, 20873, 20883, 31703,
			31689, 9988, 9980, 10967, 7223, 2158, 9996, 6299,
			6297, 7579, 3369, 1861,

			// === Burnt meat ===
			29159, 20869, 9990, 9982, 2144, 7226, 31698, 29161,
			7520, 7570, 29155, 29157, 2146, 2426, 2345, 7222,
			3375, 6301, 2880, 7580, 6303,

			// === Ingredients ===
			6469, 22929, 5767, 5992, 5378, 5380, 5382, 5386,
			5996, 6701, 1963, 5408, 5410, 5412, 5416, 6006,
			6008, 7742, 33623, 20747, 33091, 1923, 8974, 4456,
			8972, 1921, 7491, 7490, 7489, 7488, 1925, 1927,
			1929, 1965, 5460, 5478, 5462, 5464, 5468, 5470,
			5472, 5474, 5769, 1985, 7054, 1973, 1975, 7074,
			1871, 1869, 7086, 1873, 2026, 5988, 1955, 4458,
			5970, 30977, 30979, 1573, 2126, 403, 1944, 7056,
			1980, 20742, 2128, 2154, 1946, 1550, 2171, 2169,
			19653, 1947, 30037, 1987, 33659, 6683, 32357, 5994,
			1935, 1937, 2162, 6000, 2102, 2104, 2106, 2120,
			2122, 2124, 32362, 4014, 4012, 6004, 7066, 7058,
			10968, 31903, 4237, 4241, 6043, 1957, 1875, 5440,
			5458, 5442, 5444, 5448, 5450, 5452, 5454, 2108,
			2110, 2112, 7487, 7486, 7485, 7484, 5388, 5390,
			5392, 5396, 2339, 5972, 1953, 6697, 24788, 6883,
			2114, 2116, 2118, 1863, 1931, 2130, 1933, 2516,
			1942, 6703, 6705, 5420, 5438, 5422, 5424, 5428,
			5430, 5432, 5434, 1940, 7483, 7482, 7481, 7480,
			1951, 8977, 1847, 401, 13407, 13413, 13400, 13397,
			13398, 13410, 13411, 13414, 13399, 13405, 3162, 7080,
			2007, 7072, 2156, 9994, 2160, 5398, 5400, 5402,
			5406, 5504, 21626, 1941, 2150, 10978, 5986, 7088,
			8988, 2152, 1982, 5960, 5962, 5964, 5968, 7060,
			1877, 1879, 7076, 1995, 1996, 5982, 5984, 6002,
			5998, 7495, 7494, 7493, 7492, 20749, 20752,

			// === Combo food ===
			2297, 2259, 2209, 2185, 3144, 2277, 2249, 2177,
			2201, 9478, 9480, 9482, 9483, 9485, 9558, 9559,
			9561, 9563, 9577, 9579, 9581, 9583, 2285, 2293,
			2245, 2197, 2173, 2301, 2283, 2289, 2223, 2239,
			2229, 2225, 2241, 2221, 2243, 2231, 2235, 2227,
			2219, 2237, 2233, 2250, 2202, 2178, 13406, 13412,
			13404, 13409, 13408, 2213, 2187, 2255, 2217, 2287,
			2251, 2257, 2261, 2279, 9479, 9481, 9484, 9486,
			2179, 2189, 2193, 9560, 9562, 9564, 2207, 2211,
			2215, 9578, 9580, 9582, 9584, 2195, 2281, 2253,
			2205, 2191,

			// === Baked & cooked goods ===
			22795, 22789, 7198, 2323, 6961, 4016, 31128, 19662,
			2309, 2307, 1891, 7062, 6794, 1897, 2011, 7064,
			7934, 7188, 7082, 7084, 10962, 10961, 7178, 10960,
			10966, 1997, 1971, 1881, 2327, 7170, 21690, 7192,
			7194, 7182, 7184, 7172, 7174, 7164, 7166, 7212,
			7214, 7202, 7204, 2313, 2315, 1865, 7196, 7186,
			7176, 7168, 7216, 7206, 2325, 6963, 7078, 13415,
			13403, 13401, 13418, 13402, 13417, 7479, 1969, 6965,
			25631, 2003, 7218, 4608, 6962, 7068, 1883, 1885,
			2317, 2321, 19656, 1889, 2009, 2319, 21684, 2001,
			29900, 7208,

			// === Drinks & brews ===
			29952, 29958, 1911, 7748, 5809, 5745, 5889, 2092,
			9574, 9575, 9576, 2032, 1905, 7744, 5785, 5739,
			5865, 5751, 5825, 5753, 5905, 1917, 7740, 3803,
			2064, 7919, 2021, 5755, 7754, 5833, 5757, 5913,
			29947, 2074, 1977, 5763, 7752, 5849, 5929, 4242,
			4243, 7730, 7733, 7736, 30985, 30987, 30981, 30983,
			1913, 5777, 5747, 5857, 29415, 2084, 2019, 1909,
			7746, 5743, 5793, 5873, 1991, 1993, 3801, 5765,
			5741, 5801, 5881, 9568, 9566, 9569, 9571, 9572,
			9573, 9567, 9570, 2955, 7750, 5817, 5749, 5897,
			4239, 4240, 2094, 2048, 2028, 2030, 2034, 2036,
			2038, 2040, 2080, 5759, 5841, 5761, 5921, 10859,
			2042, 2050, 2056, 2066, 2076, 2082, 2086, 2015,
			31811, 2017, 2054, 1907,

			// === Cooking tools & utensils ===
			7225, 2164, 1887, 1949, 2023, 2025, 775, 977,
			2165, 2167, 2166, 7691, 946, 7162, 26033, 28502,
			6305, 7700, 9801, 9803,

			// === Burnt food ===
			2247, 2311, 1903, 2199, 2013, 7090, 2175, 7094,
			7092, 2329, 1867, 2305, 6699, 31706, 2005, 5990,
			33112,
			// Separate-ID variants (degradation / charges / trims) + bird
			// nests, ported from the PR #2/#4 era data (pre-regen)
			9802
		));
	}

	private static void addWoodcuttingFiremaking(Map<String, List<Integer>> m)
	{
		// WOODCUTTING_FIREMAKING — 206 items
		//   Axes & machetes (54), Tinderboxes & firelighting tools (9), Light
		//   sources & lamps (17), Forestry items (34), Woodcutting outfit (6),
		//   Firemaking outfit (7), Logs (25), Pyre logs (23), Shade items (6),
		//   Wintertodt & minigame items (9), Misc utility (16)
		m.put(TAG_WOODCUTTING_FIREMAKING, Arrays.asList(
			// === Axes & machetes ===
			508, 28196, 1351, 510, 28199, 1349, 512, 28202,
			1353, 514, 28205, 1361, 516, 28208, 1355, 518,
			28211, 1357, 520, 28214, 1359, 25378, 6743, 28217,
			6739, 28220, 23673, 23862, 28226, 492, 494, 496,
			498, 500, 502, 504, 506, 6741, 23821, 25110,
			28177, 25066, 30347, 25371, 30348, 6315, 975, 6313,
			6317, 10491, 23279, 13241, 13242, 20011,

			// === Tinderboxes & firelighting tools ===
			10327, 7331, 20720, 29777, 20275, 7330, 10326, 7329,
			590,

			// === Light sources & lamps ===
			26822, 4548, 4546, 4544, 36, 4529, 4527, 4525,
			4535, 10973, 5014, 4522, 4537, 4540, 4700, 596,
			7053,

			// === Forestry items ===
			28134, 28613, 28163, 28166, 28157, 28143, 28175, 28173,
			28136, 28171, 28169, 28626, 28663, 28140, 28146, 28183,
			28152, 28655, 28669, 28618, 28616, 28661, 28620, 28622,
			28624, 28154, 28628, 28161, 28159, 28529, 28192, 28674,
			28149, 28630,

			// === Woodcutting outfit ===
			10933, 10941, 10940, 10939, 9807, 9809,

			// === Firemaking outfit ===
			20710, 20704, 20708, 20706, 24554, 9804, 9806,

			// === Logs ===
			32907, 10328, 2862, 10810, 24691, 7406, 32904, 28138,
			7405, 32902, 13355, 1511, 2511, 1513, 6332, 1517,
			1521, 10329, 7404, 19669, 32910, 10812, 6333, 1519,
			1515,

			// === Pyre logs ===
			31386, 10808, 31383, 11338, 3448, 6213, 11337, 3444,
			3440, 3428, 3426, 3424, 3422, 3438, 19672, 31389,
			3436, 3434, 3432, 3430, 6211, 3442, 3446,

			// === Shade items ===
			3402, 3404, 3396, 3398, 3400, 25419,

			// === Wintertodt & minigame items ===
			20698, 20696, 20695, 20799, 20702, 20701, 20700, 20699,
			20712,

			// === Misc utility ===
			23953, 5751, 5825, 5753, 5905, 13322, 5070, 1468,
			7794, 28670, 20693, 23838, 23878, 6319, 33062, 7797,
			// Separate-ID variants (degradation / charges / trims) + bird
			// nests, ported from the PR #2/#4 era data (pre-regen)
			5071, 5072, 5074, 5075, 22798, 5076, 5077, 5078,
			22800, 9805, 9808, 23675, 28223
		));
	}

	private static void addFletching(Map<String, List<Integer>> m)
	{
		// FLETCHING — 249 items
		//   Tools (4), Logs (16), Feathers (7), Arrows (28), Arrowheads (12), Bows
		//   (25), Crossbows (33), Bolts (58), Darts (23), Javelins (27), Misc
		//   fletching (16)
		m.put(TAG_FLETCHING, Arrays.asList(
			// === Tools ===
			31043, 946, 9783, 9785,

			// === Logs ===
			2862, 1511, 2511, 1513, 1517, 1521, 33607, 33609,
			33613, 33617, 33619, 33615, 33611, 8934, 1519, 1515,

			// === Feathers ===
			10089, 314, 11881, 10091, 10088, 10087, 10090,

			// === Arrows ===
			11700, 33553, 882, 11701, 33559, 884, 33565, 11702,
			886, 11703, 33571, 888, 33577, 890, 33583, 892,
			33595, 11212, 52, 2865, 53, 2864, 33589, 33541,
			33601, 33547, 21326, 4160,

			// === Arrowheads ===
			39, 40, 41, 42, 43, 44, 11237, 21350,
			11885, 11874, 31047, 2861,

			// === Bows ===
			48, 70, 72, 62, 64, 56, 54, 50,
			4825, 58, 60, 66, 68, 839, 841, 845,
			843, 847, 849, 851, 853, 855, 857, 859,
			861,

			// === Crossbows ===
			9454, 9420, 9174, 9457, 9423, 9177, 9459, 9425,
			9179, 9461, 9427, 9181, 9463, 9429, 9183, 9185,
			21921, 21918, 21902, 9456, 9422, 28869, 21952, 9450,
			9448, 9442, 9465, 9431, 9446, 9444, 9440, 9452,
			9176,

			// === Bolts ===
			9375, 877, 9377, 9140, 9378, 9141, 9379, 9142,
			9380, 9143, 21930, 9193, 9341, 21969, 21905, 21971,
			21965, 21957, 21973, 21955, 21959, 21967, 21963, 21961,
			21338, 47, 9376, 9192, 9190, 9187, 9418, 9416,
			28878, 9194, 45, 46, 9191, 9381, 9189, 9382,
			28872, 9188, 11887, 11876, 9139, 9335, 9145, 9336,
			9337, 9338, 9339, 10158, 10159, 11875, 9340, 9342,
			9144, 21316,

			// === Darts ===
			819, 806, 820, 31579, 807, 821, 808, 822,
			809, 823, 810, 824, 811, 11232, 11230, 25849,
			25853, 28991, 31004, 30998, 31575, 31010, 31583,

			// === Javelins ===
			19570, 825, 19572, 826, 19574, 827, 19576, 828,
			19578, 829, 19580, 830, 19582, 19484, 23648, 21352,
			19592, 19601, 19589, 19598, 19595, 19584, 19586, 19610,
			19607, 19604, 21318,

			// === Misc fletching ===
			31045, 33716, 1777, 31052, 31086, 33539, 9438, 31032,
			31018, 31024, 31027, 411, 413, 31049, 31572, 31054,
			// Separate-ID variants (degradation / charges / trims) + bird
			// nests, ported from the PR #2/#4 era data (pre-regen)
			9784
		));
	}

	private static void addFishing(Map<String, List<Integer>> m)
	{
		// FISHING — 198 items
		//   Fishing tools (40), Bait & consumables (30), Fishing outfit (15), Raw
		//   fish (67), Trophies & big catches (25), Fishing minigame items (21)
		m.put(TAG_FISHING, Arrays.asList(
			// === Fishing tools ===
			25373, 21028, 23762, 23864, 583, 10129, 11323, 305,
			6662, 23823, 21652, 25114, 6667, 25582, 22838, 25585,
			6670, 6673, 307, 309, 311, 25059, 30342, 25367,
			30343, 3157, 301, 1585, 23122, 22842, 22846, 22844,
			30900, 303, 6209, 25580, 6674, 22816, 21031, 21033,

			// === Bait & consumables ===
			7198, 11883, 31255, 13430, 20853, 11326, 11940, 30773,
			314, 11881, 32307, 22818, 11334, 7188, 313, 11479,
			11477, 155, 153, 151, 2438, 11324, 13431, 13432,
			10087, 31611, 31608, 31605, 31602, 31820,

			// === Fishing outfit ===
			13261, 13258, 13259, 13260, 22941, 22943, 22945, 22947,
			25598, 25592, 31252, 25594, 25596, 9798, 9800,

			// === Raw fish ===
			26579, 25566, 22826, 21693, 22829, 13429, 7942, 5004,
			22835, 25565, 21293, 21394, 11330, 11332, 11328, 21356,
			22832, 21655, 321, 13439, 363, 32341, 20861, 29216,
			25670, 5001, 25658, 341, 11934, 32309, 25652, 32317,
			32333, 25564, 345, 31561, 3142, 3150, 20867, 2148,
			20859, 377, 353, 389, 32349, 7944, 20863, 23872,
			349, 20855, 10138, 20865, 331, 327, 395, 383,
			317, 2514, 3379, 20857, 371, 31553, 25664, 335,
			359, 32325, 13339,

			// === Trophies & big catches ===
			7989, 25559, 7993, 7991, 405, 25590, 31408, 31412,
			22840, 31420, 31416, 407, 411, 31424, 7990, 25561,
			7994, 7992, 31410, 31414, 31422, 31418, 31426, 31430,
			31428,

			// === Fishing minigame items ===
			23953, 21865, 32380, 32366, 32368, 32371, 32377, 32383,
			32374, 7779, 13320, 33621, 22820, 33062, 28502, 25578,
			25569, 25588, 10978, 25567, 25602,
			// Separate-ID variants (degradation / charges / trims) + bird
			// nests, ported from the PR #2/#4 era data (pre-regen)
			9799, 23764
		));
	}

	private static void addCrafting(Map<String, List<Integer>> m)
	{
		// CRAFTING — 311 items
		//   Moulds (9), Crafting tools (15), Gems (75), Hides & leather (32),
		//   Spinning materials (20), Glassmaking (18), Battlestaves & orbs (5),
		//   Pottery & clay (11), Jewellery materials (18), Crafted jewellery (6),
		//   Crafted armour & leather goods (25), Monster parts & shells (33),
		//   Crafting outfit & utility (44)
		m.put(TAG_CRAFTING, Arrays.asList(
			// === Moulds ===
			1595, 9434, 11065, 1599, 1597, 1592, 2976, 5523,
			1594,

			// === Crafting tools ===
			26813, 19473, 1755, 29920, 3678, 33384, 33393, 33387,
			33390, 1785, 29303, 29301, 1733, 1735, 29299,

			// === Gems ===
			1664, 1615, 1702, 1683, 11115, 1645, 1631, 21347,
			25853, 21352, 1601, 1700, 1681, 11092, 1662, 1643,
			1605, 1696, 1677, 11076, 1658, 1639, 21270, 1611,
			21111, 21102, 21120, 21093, 21084, 6573, 6581, 6579,
			11130, 6577, 6575, 1609, 21108, 21099, 21117, 21090,
			21081, 1613, 1603, 1698, 1679, 11085, 1660, 1641,
			1607, 1694, 1675, 11072, 1656, 1637, 21114, 21105,
			21123, 21096, 21087, 1617, 1621, 1627, 6571, 1625,
			1629, 1619, 1623, 19496, 19493, 19541, 19501, 19532,
			19535, 19538, 19529,

			// === Hides & leather ===
			2370, 2509, 1747, 2505, 1751, 1745, 1753, 2507,
			1749, 27897, 948, 6169, 1739, 10820, 6155, 6171,
			29163, 958, 1743, 30085, 33382, 29218, 1741, 29292,
			6287, 7801, 6289, 6173, 29177, 9080, 9081, 10818,

			// === Spinning materials ===
			31045, 1759, 31475, 8790, 31478, 31472, 31460, 31469,
			3470, 1779, 10814, 31457, 31466, 5931, 31463, 9436,
			1734, 27279, 1737, 13383,

			// === Glassmaking ===
			1919, 1783, 4546, 4544, 403, 4527, 6667, 10980,
			4525, 4535, 21504, 4542, 10973, 1775, 4522, 4540,
			401, 1781,

			// === Battlestaves & orbs ===
			573, 1391, 575, 569, 571,

			// === Pottery & clay ===
			434, 4440, 1761, 12009, 12010, 1791, 28193, 1789,
			5352, 1787, 4438,

			// === Jewellery materials ===
			2365, 28298, 28293, 28279, 28295, 1673, 2357, 6038,
			28291, 28304, 2355, 9382, 2961, 28287, 1720, 1714,
			28289, 28301,

			// === Crafted jewellery ===
			1692, 1654, 1635, 32386, 6041, 10132,

			// === Crafted armour & leather goods ===
			22284, 2491, 1757, 11069, 26788, 30076, 30079, 30073,
			30082, 29286, 29289, 29283, 29280, 5525, 1129, 1095,
			1063, 22269, 22272, 22275, 1065, 22278, 2487, 22281,
			2489,

			// === Monster parts & shells ===
			29799, 3353, 3351, 3361, 3345, 3355, 3349, 3359,
			3347, 3357, 22372, 33634, 33534, 33636, 6167, 7536,
			7538, 10113, 12932, 31954, 31957, 10995, 10996, 10134,
			6165, 6157, 6159, 6161, 28798, 12927, 6163, 29580,
			7939,

			// === Crafting outfit & utility ===
			4207, 25859, 27269, 3239, 29338, 1767, 1923, 689,
			1925, 32364, 10981, 8792, 771, 1771, 25644, 29297,
			3211, 23836, 23876, 21690, 1769, 1931, 1773, 1763,
			1951, 26036, 29295, 33681, 33693, 33683, 33689, 33685,
			33677, 33691, 33687, 33679, 7771, 7767, 7759, 5933,
			10891, 1765, 9780, 9782,
			// Separate-ID variants (degradation / charges / trims) + bird
			// nests, ported from the PR #2/#4 era data (pre-regen)
			9781
		));
	}

	private static void addMiningSmithing(Map<String, List<Integer>> m)
	{
		// MINING_SMITHING — 297 items
		//   Pickaxes (32), Mining outfit & utility (38), Ores (21), Special ores &
		//   minerals (47), Bars (15), Smithing tools (5), Smithing outfit & gloves
		//   (8), Shayzien supply armour (30), Smithed parts & components (70),
		//   Cannonballs & ammo outputs (18), Giants' Foundry & minigame items (13)
		m.put(TAG_MINING_SMITHING, Arrays.asList(
			// === Pickaxes ===
			1265, 1267, 1269, 12297, 1273, 1271, 1275, 27695,
			25376, 11920, 12797, 23677, 23680, 23863, 468, 470,
			472, 474, 476, 478, 11923, 12594, 23822, 25112,
			25063, 30345, 25369, 30346, 23276, 13243, 13244, 20014,

			// === Mining outfit & utility ===
			11074, 25539, 1913, 5777, 5747, 5857, 21392, 33384,
			33393, 33387, 33390, 25555, 25549, 25551, 25553, 27014,
			21343, 5014, 7791, 27019, 27693, 12016, 12013, 12014,
			12015, 2568, 27017, 29412, 21345, 13104, 13105, 13106,
			13107, 4567, 9792, 9794, 9795, 9797,

			// === Ores ===
			440, 447, 449, 23877, 21347, 13575, 668, 453,
			21534, 436, 23837, 2893, 2892, 444, 31716, 13356,
			9076, 31719, 451, 442, 438,

			// === Special ores & minerals ===
			21532, 21536, 21537, 6979, 21726, 27616, 25684, 25676,
			22603, 29088, 30860, 30848, 9632, 24706, 13573, 13572,
			22595, 21535, 12012, 23907, 30854, 30862, 30858, 30864,
			30846, 13570, 21540, 30856, 680, 12011, 23906, 31724,
			31722, 21538, 28349, 6971, 21533, 6448, 25547, 25527,
			22593, 23905, 21341, 22597, 21622, 13571, 23908,

			// === Bars ===
			2349, 2351, 2353, 2359, 2361, 2365, 9467, 28276,
			32892, 2357, 32889, 13354, 9077, 2363, 2355,

			// === Smithing tools ===
			4, 27012, 2347, 25644, 29775,

			// === Smithing outfit & gloves ===
			19988, 776, 1580, 27027, 27029, 27031, 27025, 27023,

			// === Shayzien supply armour ===
			13539, 13544, 13549, 13554, 13559, 13538, 13543, 13548,
			13553, 13558, 13541, 13546, 13551, 13556, 13561, 13540,
			13545, 13550, 13555, 13560, 13542, 13547, 13552, 13557,
			13562, 13565, 13566, 13567, 13568, 13569,

			// === Smithed parts & components ===
			9375, 19570, 31999, 9420, 480, 1794, 32020, 9377,
			19572, 32002, 9423, 482, 7941, 32023, 32026, 9378,
			19574, 32005, 9425, 484, 12592, 32029, 9379, 19576,
			32008, 9427, 486, 9380, 19578, 32011, 488, 9429,
			32032, 32035, 19580, 32014, 490, 32017, 22103, 22097,
			22100, 32038, 32876, 9376, 9422, 698, 28813, 30324,
			4544, 32886, 11286, 11798, 11818, 11820, 11822, 11794,
			11796, 11800, 9416, 30765, 4540, 9381, 9431, 686,
			28798, 2366, 2368, 22006, 29580, 21637,

			// === Cannonballs & ammo outputs ===
			31906, 31918, 31932, 31908, 31920, 31934, 31922, 31936,
			31910, 31924, 31938, 31912, 31926, 31940, 31914, 31928,
			31942, 21728,

			// === Giants' Foundry & minigame items ===
			23953, 25635, 31245, 25543, 27021, 21539, 30808, 33657,
			27010, 13321, 28505, 26024, 23760,
			// Separate-ID variants (degradation / charges / trims) + bird
			// nests, ported from the PR #2/#4 era data (pre-regen)
			9793, 9796, 23682, 25541, 25545
		));
	}

	private static void addHerblore(Map<String, List<Integer>> m)
	{
		// HERBLORE — 675 items
		//   Tools (15), Herblore outfit & utility (22), Herbs (40), Secondaries
		//   (107), Unfinished potions (35), Finished potions (438), Mastering
		//   Mixology items (18)
		m.put(TAG_HERBLORE, Arrays.asList(
			// === Tools ===
			30002, 20800, 11877, 11427, 11428, 798, 797, 233,
			3377, 229, 22446, 227, 20801, 23880, 11879,

			// === Herblore outfit & utility ===
			29972, 21163, 12641, 29971, 19662, 13064, 29973, 12859,
			30357, 1909, 7746, 5743, 11738, 13226, 12857, 29996,
			26027, 26045, 33135, 13066, 9774, 9776,

			// === Herbs ===
			3051, 3000, 261, 20908, 265, 267, 20905, 211,
			20907, 215, 217, 20904, 199, 205, 30094, 209,
			213, 2485, 201, 20901, 207, 1533, 1525, 203,
			3049, 219, 249, 255, 30097, 259, 263, 2481,
			251, 20902, 257, 1534, 1526, 253, 2998, 269,

			// === Secondaries ===
			239, 243, 21975, 241, 29530, 22124, 12640, 23867,
			23964, 27616, 30018, 31712, 29784, 28345, 28346, 28351,
			592, 30795, 5075, 1582, 1581, 33623, 31674, 20698,
			6016, 30937, 2398, 11326, 29963, 29643, 20912, 5935,
			23830, 31708, 6693, 4460, 30800, 9735, 30031, 20911,
			221, 1550, 23517, 9736, 9018, 9016, 3261, 33659,
			5873, 6681, 23835, 23875, 10142, 32357, 31587, 10145,
			11262, 28837, 247, 10109, 10111, 11992, 11994, 27272,
			225, 6051, 32362, 10143, 29078, 29079, 2970, 28341,
			28342, 10937, 27790, 26368, 26569, 31484, 6018, 3138,
			33133, 31677, 31710, 31671, 223, 11324, 4462, 7650,
			231, 31569, 9017, 20910, 28388, 28890, 1939, 2150,
			10144, 2152, 31487, 237, 235, 245, 23489, 32360,
			6049, 20752, 12934,

			// === Unfinished potions ===
			9019, 5942, 5951, 5936, 5939, 738, 103, 22443,
			107, 109, 31662, 23881, 91, 7654, 7656, 7658,
			97, 4464, 30100, 101, 105, 2483, 93, 31665,
			99, 20697, 737, 3004, 95, 3002, 111, 31668,
			3406, 4840, 28386,

			// === Finished potions ===
			2428, 23697, 121, 23700, 123, 23703, 125, 23706,
			25765, 25757, 25761, 2446, 2448, 25764, 25756, 25760,
			175, 181, 25763, 25755, 11433, 25759, 177, 183,
			25762, 25754, 11435, 25758, 179, 185, 23709, 113,
			23712, 115, 23715, 117, 23718, 119, 2432, 23721,
			133, 23724, 135, 23727, 137, 23730, 9739, 23685,
			12695, 9741, 23688, 12697, 9743, 23691, 12699, 9745,
			23694, 12701, 3008, 31614, 3010, 31617, 3012, 31620,
			3014, 31623, 31638, 12625, 31641, 12627, 31644, 12629,
			31647, 12631, 3032, 3034, 3036, 3038, 2438, 31602,
			151, 31605, 153, 31608, 155, 31611, 23733, 2444,
			23736, 169, 23739, 171, 23742, 173, 23745, 3040,
			23748, 3042, 23751, 3044, 23754, 3046, 9021, 9022,
			11489, 9023, 11491, 9024, 2434, 139, 141, 143,
			2436, 145, 147, 149, 2440, 157, 159, 161,
			2442, 163, 165, 167, 3024, 3026, 11493, 3028,
			11495, 3030, 6685, 6687, 6689, 6691, 10925, 10927,
			10929, 10931, 2450, 189, 191, 193, 4417, 4419,
			4421, 4423, 2452, 21978, 2454, 21981, 2456, 21984,
			2458, 21987, 11951, 11953, 11960, 11955, 11962, 11957,
			22209, 22212, 22221, 22215, 21994, 22224, 22218, 21997,
			5943, 5952, 5945, 5954, 11501, 5947, 5956, 11503,
			5949, 5958, 12905, 12913, 29824, 12907, 12915, 29827,
			12909, 12917, 29830, 12911, 12919, 29833, 187, 5937,
			5940, 22461, 24635, 22464, 24638, 22467, 24641, 22470,
			24644, 22449, 24623, 22452, 24626, 22455, 24629, 22458,
			24632, 26340, 31650, 29631, 6470, 20924, 20916, 20920,
			27629, 30137, 26581, 5793, 7660, 31590, 9998, 20948,
			20940, 20944, 27202, 10909, 10917, 29080, 27315, 20996,
			20988, 20992, 20972, 20964, 20968, 30125, 20699, 4842,
			2430, 20960, 20952, 20956, 3408, 3016, 31626, 30875,
			27327, 20936, 20928, 20932, 20984, 20976, 20980, 26342,
			31653, 29634, 6472, 20923, 20915, 20919, 27632, 30140,
			26583, 7662, 31593, 10000, 20947, 20939, 20943, 27205,
			10911, 10919, 29081, 27317, 20995, 20987, 20991, 20971,
			20963, 20967, 30128, 20700, 4844, 127, 20959, 20951,
			20955, 3410, 3018, 31629, 30878, 27329, 20935, 20927,
			20931, 20983, 20975, 20979, 11461, 27347, 26344, 26350,
			11473, 11505, 31656, 11429, 29637, 11445, 6474, 11457,
			20922, 20914, 20918, 11453, 11477, 27635, 30143, 26585,
			7664, 31596, 10002, 11517, 20946, 20938, 20942, 11513,
			27208, 10913, 10921, 29082, 27319, 20994, 20986, 20990,
			20970, 20962, 20966, 11465, 30131, 11509, 20701, 4846,
			11437, 11449, 129, 20958, 20950, 20954, 3412, 27343,
			12633, 11443, 11497, 11481, 3020, 31632, 11485, 11469,
			30881, 27331, 20934, 20926, 20930, 20982, 20974, 20978,
			11521, 11463, 27349, 26346, 26353, 11475, 11507, 31659,
			11431, 29640, 11447, 6476, 11459, 20921, 20913, 20917,
			11455, 11479, 27638, 30146, 26587, 7666, 31599, 10004,
			11519, 20945, 20937, 20941, 11515, 27211, 10915, 10923,
			29083, 27321, 20993, 20985, 20989, 20969, 20961, 20965,
			11467, 30134, 11511, 20702, 4848, 11439, 11451, 131,
			20957, 20949, 20953, 3414, 12635, 11441, 11499, 11483,
			3022, 31635, 11487, 11471, 30884, 27333, 20933, 20925,
			20929, 20981, 20973, 20977, 11523, 29531,

			// === Mastering Mixology items ===
			30007, 29982, 29988, 31084, 30014, 29993, 30015, 30016,
			30017, 30009, 30011, 30013, 30019, 30020, 30032, 30005,
			30012, 29974,
			// Separate-ID variants (degradation / charges / trims) + bird
			// nests, ported from the PR #2/#4 era data (pre-regen)
			9775, 29990
		));
	}

	private static void addAgilityThieving(Map<String, List<Integer>> m)
	{
		// AGILITY_THIEVING — 231 items
		//   Agility outfit & graceful (87), Run-energy consumables (55), Agility
		//   marks & rewards (16), Thieving outfit & rogue set (17), Thieving tools
		//   (15), Thieving loot & artefacts (41)
		m.put(TAG_AGILITY_THIEVING, Arrays.asList(
			// === Agility outfit & graceful ===
			9771, 9773, 7782, 88, 24729, 11860, 13589, 13601,
			13613, 13625, 13637, 13677, 21076, 24758, 25084, 27459,
			30060, 11852, 13581, 13593, 13605, 13617, 13629, 13669,
			21064, 24746, 25072, 27447, 30048, 11858, 13587, 13599,
			13611, 13623, 13635, 13675, 21073, 24755, 25081, 27456,
			30057, 11850, 13579, 13591, 13603, 13615, 13627, 13667,
			21061, 24743, 25069, 27444, 30045, 11856, 13585, 13597,
			13609, 13621, 13633, 13673, 21070, 24752, 25078, 27453,
			30054, 11854, 13583, 13595, 13607, 13619, 13631, 13671,
			21067, 24749, 25075, 27450, 30051, 24721, 24790, 9419,
			30676, 10553, 24735, 24844, 10069, 10071, 25099,

			// === Run-energy consumables ===
			11463, 11461, 3038, 3036, 3034, 3032, 12640, 12641,
			10846, 10850, 23882, 23883, 23884, 23885, 3014, 3012,
			3010, 3008, 13125, 13126, 13127, 13128, 31647, 31644,
			31641, 31638, 31623, 31620, 31617, 31614, 10844, 10848,
			12635, 12633, 12631, 23589, 12629, 23587, 12627, 23585,
			12625, 23583, 7218, 10845, 10849, 3022, 20551, 3020,
			20550, 3018, 20549, 3016, 20548, 10847, 10851,

			// === Agility marks & rewards ===
			29480, 29482, 30042, 24733, 25244, 20659, 22207, 24723,
			24727, 24711, 24731, 24946, 24725, 24719, 11849, 29460,

			// === Thieving outfit & rogue set ===
			10075, 4310, 4304, 4308, 4302, 4306, 4300, 4298,
			5557, 5556, 5554, 5553, 5555, 21307, 9777, 9779,
			7785,

			// === Thieving tools ===
			4627, 21143, 5559, 1523, 6416, 6420, 6418, 4599,
			6410, 6408, 5560, 24740, 4600, 6414, 6412,

			// === Thieving loot & artefacts ===
			24777, 7919, 10989, 10983, 22521, 23948, 10985, 9040,
			9028, 9034, 29325, 9026, 10991, 21656, 10987, 8942,
			4024, 4025, 26948, 26945, 9032, 9036, 10993, 6970,
			20663, 28771, 28493, 29658, 29661, 10975, 4179, 13436,
			13437, 13435, 13438, 13434, 9030, 9042, 9038, 29332,
			3325,
			// Separate-ID variants (degradation / charges / trims) + bird
			// nests, ported from the PR #2/#4 era data (pre-regen)
			9772, 9778, 24736, 26950
		));
	}

	private static void addSlayer(Map<String, List<Integer>> m)
	{
		// SLAYER — 628 items
		//   Slayer assignment items (19), Mandatory task items (42), Core slayer
		//   gear (108), Cannon & burst supplies (22), Combat potions (84), Prayer
		//   & restores (18), Food (32), Teleports (35), Loot management (10),
		//   Monster heads & trophies (35), Boss drops & upgrade parts (35), Misc
		//   utility (188)
		m.put(TAG_SLAYER, Arrays.asList(
			// === Slayer assignment items ===
			22302, 21807, 22305, 21810, 19685, 19679, 19681, 19683,
			4155, 21270, 11873, 11872, 11871, 11869, 11868, 11867,
			11866, 21268, 21257,

			// === Mandatory task items ===
			4161, 4166, 4164, 7432, 7430, 7421, 7429, 7428,
			7426, 7425, 7424, 7423, 6696, 4156, 4168, 13381,
			13358, 13363, 13368, 13373, 13378, 13357, 13362, 13367,
			13372, 13377, 13360, 13365, 13370, 13375, 13380, 13359,
			13364, 13369, 13374, 13379, 13361, 13366, 13371, 13376,
			4551, 8923,

			// === Core slayer gear ===
			13263, 13265, 12006, 4151, 26482, 21316, 29816, 29818,
			19675, 8921, 8919, 11783, 8917, 11782, 8915, 11781,
			8911, 11779, 8909, 11778, 8907, 11777, 8905, 11776,
			11784, 19639, 19641, 24699, 24697, 22951, 23037, 4160,
			11875, 6746, 21012, 22978, 30070, 29589, 22981, 21736,
			21742, 21739, 21752, 19643, 19645, 33066, 33068, 22983,
			23073, 23075, 22398, 25979, 30891, 25981, 27287, 27291,
			20727, 4158, 11902, 33338, 33439, 29594, 21264, 21266,
			33340, 33445, 19647, 19649, 4162, 21754, 7648, 7639,
			7647, 7646, 7644, 7643, 7642, 7641, 4081, 10588,
			12018, 12017, 29591, 33601, 11864, 11865, 4170, 21255,
			13233, 12926, 11905, 21888, 21890, 24370, 24444, 25910,
			25912, 25898, 25900, 11908, 11876, 27660, 27657, 25904,
			25906, 22542, 28585, 28583,

			// === Cannon & burst supplies ===
			12728, 10, 26524, 6, 26520, 12, 26526, 8,
			26522, 20523, 12738, 12732, 20524, 12734, 21728, 12736,
			2, 21698, 21704, 21707, 21701, 12730,

			// === Combat potions ===
			12919, 12917, 12915, 12913, 5949, 5947, 5945, 5943,
			22470, 22467, 22464, 22461, 22458, 22455, 22452, 22449,
			23742, 23739, 23736, 23733, 23706, 23703, 23700, 23697,
			23694, 23691, 23688, 23685, 23730, 23727, 23724, 23721,
			23718, 23715, 23712, 23709, 22218, 22215, 22212, 22209,
			30146, 30143, 30140, 30137, 7666, 7664, 7662, 7660,
			173, 171, 169, 2444, 6691, 6689, 6687, 6685,
			21987, 21984, 21981, 21978, 149, 147, 145, 2436,
			12701, 12699, 12697, 12695, 167, 165, 163, 2442,
			11729, 11728, 11727, 11726, 11725, 11724, 11723, 11722,
			161, 159, 157, 2440,

			// === Prayer & restores ===
			24605, 24603, 24601, 24598, 143, 141, 139, 2434,
			10931, 10929, 10927, 10925, 11495, 11493, 3030, 3028,
			3026, 3024,

			// === Food ===
			2297, 13441, 2323, 3144, 2343, 2011, 11936, 347,
			379, 355, 391, 2327, 2293, 7946, 2301, 2289,
			2229, 2235, 329, 325, 397, 385, 373, 2187,
			2255, 333, 7060, 1885, 2195, 7208, 2253, 2191,

			// === Teleports ===
			1706, 1708, 1710, 1712, 11978, 21175, 21173, 21171,
			21166, 11126, 11124, 11122, 11120, 11118, 11974, 11972,
			21155, 21153, 21151, 21146, 2566, 2564, 2562, 2558,
			2556, 2554, 2552, 11113, 11111, 11109, 11107, 11970,
			11968, 8013, 13393,

			// === Loot management ===
			25781, 10499, 22109, 10498, 13116, 22986, 27281, 13226,
			11941, 12791,

			// === Monster heads & trophies ===
			7979, 23077, 29788, 7977, 7976, 6798, 11279, 13508,
			13496, 13487, 6799, 6800, 6801, 6802, 6803, 6804,
			7980, 7981, 7978, 6808, 6805, 6806, 7986, 7984,
			7983, 7982, 23079, 7987, 7988, 22673, 7985, 2425,
			21907, 21909, 6807,

			// === Boss drops & upgrade parts ===
			26370, 24268, 28279, 21730, 13276, 13275, 13274, 28276,
			21275, 22103, 22097, 31996, 22100, 22957, 22960, 13227,
			22988, 22966, 22973, 22971, 22969, 20724, 12932, 28281,
			26368, 26372, 26231, 13229, 13231, 12927, 22006, 12922,
			28285, 28283, 21637,

			// === Misc utility ===
			5520, 13262, 20525, 21338, 22557, 29804, 26229, 26227,
			26223, 26225, 26221, 21804, 19677, 21813, 21634, 29806,
			28298, 6733, 31088, 31093, 31097, 31092, 31096, 31095,
			31094, 11883, 11832, 11834, 13254, 31172, 28316, 6737,
			12740, 24777, 13193, 12742, 21817, 21183, 27283, 23083,
			22975, 21724, 11885, 11874, 22372, 22320, 7054, 2185,
			27667, 7975, 23975, 28577, 23987, 23979, 22804, 20849,
			772, 29596, 20736, 25340, 21140, 7056, 2890, 22660,
			20742, 11877, 25280, 13235, 21177, 12859, 28321, 27670,
			11881, 6666, 3741, 26358, 26360, 26364, 26362, 12769,
			25926, 25928, 25932, 25934, 25936, 30638, 30637, 21643,
			21726, 4153, 29966, 5576, 5575, 5574, 7159, 23064,
			12647, 13103, 10581, 22671, 31258, 12004, 23490, 7053,
			13391, 24261, 9084, 12744, 7409, 13198, 13201, 13280,
			22386, 9731, 20730, 24260, 7058, 24271, 11748, 13080,
			29792, 29790, 29794, 12002, 23023, 23025, 23027, 23031,
			23033, 23035, 12857, 13279, 13237, 13250, 24262, 6703,
			6705, 2221, 2231, 2219, 2233, 13239, 24942, 21820,
			13202, 20607, 9185, 13252, 4082, 25746, 6731, 12931,
			12929, 26108, 10952, 9786, 9788, 7788, 5759, 5841,
			5761, 5921, 33651, 13196, 13200, 10587, 21123, 12692,
			7068, 24466, 11887, 13273, 24258, 24259, 33247, 24266,
			28310, 12771, 28301, 6735, 11879, 27655, 22396, 22394,
			22388, 22392, 22390, 12934,
			// Separate-ID variants (degradation / charges / trims) + bird
			// nests, ported from the PR #2/#4 era data (pre-regen)
			2560, 7422, 7427, 7431, 7640, 7645, 8901, 8903,
			8913, 9787, 11105, 11870, 11907, 12924, 13392, 21149,
			21169, 21816, 22545, 23977, 23981, 23989, 24125, 30305
		));
	}

	private static void addFarming(Map<String, List<Integer>> m)
	{
		// FARMING — 362 items
		//   Farming tools (34), Compost & soil treatment (14), Allotment seeds
		//   (8), Hops seeds (10), Flower seeds (6), Herb seeds (15), Bush seeds
		//   (6), Tree seeds (6), Fruit tree seeds (10), Special seeds (24),
		//   Saplings (47), Harvested produce (136), Farmer outfit & contracts (46)
		m.put(TAG_FARMING, Arrays.asList(
			// === Farming tools ===
			5376, 13254, 22994, 5350, 5418, 5354, 5325, 30037,
			13353, 13420, 6057, 30359, 7409, 5356, 13250, 5341,
			5347, 5348, 13252, 6059, 5329, 13639, 5343, 33135,
			952, 5328, 5331, 5333, 5334, 5335, 5337, 5338,
			5339, 5340,

			// === Compost & soil treatment ===
			6032, 19704, 6476, 6474, 6472, 6470, 30747, 6036,
			28154, 13421, 13419, 6034, 21483, 21622,

			// === Allotment seeds ===
			5324, 5319, 5318, 22879, 5323, 5320, 5322, 5321,

			// === Hops seeds ===
			5308, 5305, 31545, 31541, 5307, 31543, 5306, 5310,
			5311, 5309,

			// === Flower seeds ===
			22887, 5100, 5096, 5098, 5097, 5099,

			// === Herb seeds ===
			5300, 5298, 5301, 5303, 5291, 5294, 30088, 5297,
			5299, 5302, 5292, 5295, 5293, 5296, 5304,

			// === Bush seeds ===
			5105, 5102, 5103, 5104, 5106, 5101,

			// === Tree seeds ===
			5312, 5316, 5314, 22871, 5313, 5315,

			// === Fruit tree seeds ===
			22877, 5283, 5284, 5290, 22869, 5286, 5285, 5289,
			5288, 5287,

			// === Special seeds ===
			31549, 29538, 23661, 22881, 5281, 13424, 20909, 5280,
			31547, 13423, 20906, 13657, 22875, 22883, 22885, 13425,
			21488, 5282, 20903, 22873, 31551, 21490, 5317, 21486,

			// === Saplings ===
			31505, 31492, 22866, 22862, 23659, 23655, 5496, 5480,
			9932, 5497, 5481, 5503, 5487, 31502, 31490, 22856,
			22848, 5499, 5483, 5374, 5362, 21480, 21471, 5372,
			5360, 5370, 5358, 5498, 5482, 5502, 5486, 5501,
			5485, 5500, 5484, 22859, 22850, 31508, 31494, 5375,
			5363, 21477, 21469, 5371, 5359, 5373, 5361,

			// === Harvested produce ===
			239, 22932, 6469, 22929, 3051, 5378, 5380, 5382,
			5386, 5996, 1963, 5408, 5410, 5412, 5416, 6006,
			6008, 13427, 20908, 1965, 5460, 5478, 5462, 5464,
			5468, 5470, 5472, 5474, 6016, 30937, 5980, 22935,
			5974, 5978, 1955, 31460, 5970, 2126, 21504, 13426,
			20905, 6311, 1947, 1987, 211, 20907, 215, 217,
			20904, 199, 205, 30094, 209, 213, 2485, 201,
			20901, 207, 203, 3049, 219, 5976, 5994, 31457,
			30097, 247, 5931, 6000, 6020, 225, 13428, 6030,
			6051, 6028, 6047, 6010, 6004, 31903, 6012, 27790,
			20902, 6022, 6043, 1957, 5440, 5458, 5442, 5444,
			5448, 5450, 5452, 5454, 2108, 5388, 5390, 5392,
			5396, 5972, 2114, 6018, 1942, 3138, 5420, 5438,
			5422, 5424, 5428, 5430, 5432, 5434, 1951, 6014,
			231, 6053, 5398, 5400, 5402, 5406, 5504, 5986,
			1982, 5960, 5962, 5964, 5968, 5982, 6055, 6002,
			5933, 6024, 6045, 1793, 5998, 6026, 6049, 20749,

			// === Farmer outfit & contracts ===
			29952, 21160, 6040, 13122, 13123, 13124, 21697, 5073,
			7413, 13653, 22798, 22800, 20747, 1925, 1929, 5769,
			5763, 7752, 5849, 5929, 31511, 13644, 13640, 13642,
			13643, 13646, 7178, 9082, 5765, 7416, 7418, 4014,
			4012, 12792, 12793, 13654, 31484, 31513, 22993, 33062,
			26096, 9079, 20661, 31515, 9810, 9812,
			// Separate-ID variants (degradation / charges / trims) + bird
			// nests, ported from the PR #2/#4 era data (pre-regen)
			9811, 22997
		));
	}

	private static void addRunecraft(Map<String, List<Integer>> m)
	{
		// RUNECRAFT — 153 items
		//   Essence (16), Pouches & storage (6), Talismans (28), Tiaras (18), Core
		//   runes (22), Combination runes (7), Runecraft gear & utility (32),
		//   Guardians of the Rift items (24)
		m.put(TAG_RUNECRAFT, Arrays.asList(
			// === Essence ===
			1436, 26390, 25698, 25700, 24704, 24706, 13446, 7938,
			13445, 25280, 26879, 25696, 7936, 29087, 28924, 28591,

			// === Pouches & storage ===
			26784, 26906, 5514, 5512, 5510, 5509,

			// === Talismans ===
			1438, 1450, 1446, 26798, 1452, 1454, 1456, 1440,
			5516, 1442, 1458, 1448, 1462, 26887, 26894, 26895,
			26892, 26896, 26893, 26889, 26890, 26898, 26891, 26897,
			26888, 1460, 1444, 22118,

			// === Tiaras ===
			5527, 9106, 5549, 5533, 26801, 5543, 5539, 5547,
			5535, 26804, 5537, 26788, 5545, 5529, 5541, 5525,
			5531, 22121,

			// === Core runes ===
			556, 12728, 9075, 565, 559, 562, 12738, 564,
			560, 557, 12732, 554, 12734, 563, 558, 12736,
			561, 566, 28929, 555, 12730, 21880,

			// === Combination runes ===
			30843, 4696, 4699, 4695, 4698, 4697, 4694,

			// === Runecraft gear & utility ===
			25481, 25478, 9765, 9767, 11103, 11101, 11099, 11095,
			30771, 5521, 26856, 30640, 26850, 28597, 26815, 26854,
			26852, 28599, 26039, 25578, 28595, 28593, 25389, 25395,
			25392, 25398, 25401, 25404, 25410, 25407, 25413, 25416,

			// === Guardians of the Rift items ===
			26809, 26807, 26822, 26813, 26792, 26901, 26811, 26914,
			26876, 26880, 26881, 26878, 26820, 26908, 26912, 26884,
			26886, 30887, 26941, 20665, 26885, 26910, 26882, 26883,
			// Separate-ID variants (degradation / charges / trims) + bird
			// nests, ported from the PR #2/#4 era data (pre-regen)
			9766, 26818
		));
	}

	private static void addHunter(Map<String, List<Integer>> m)
	{
		// HUNTER — 251 items
		//   Hunter tools & traps (28), Hunter weapons & ammo (8), Nets, jars &
		//   containers (11), Hunter outfit (30), Baits & potions (15), Chinchompas
		//   (6), Salamanders & lizards (15), Butterflies & moths (23), Furs &
		//   hides (19), Hunter meats (29), Hunter tertiaries (44), Implings &
		//   impling jars (14), Birdhouse items (9)
		m.put(TAG_HUNTER, Arrays.asList(
			// === Hunter tools & traps ===
			29251, 10006, 12740, 10008, 12742, 21652, 29253, 11159,
			29309, 11262, 10028, 10027, 11258, 10025, 12744, 10150,
			29256, 29307, 10031, 21126, 10029, 29259, 29261, 596,
			10010, 11259, 22816, 10023,

			// === Hunter weapons & ammo ===
			10129, 29311, 29305, 28869, 33655, 10156, 10158, 10159,

			// === Nets, jars & containers ===
			10012, 29244, 29242, 29246, 29248, 30644, 29303, 29297,
			29301, 29299, 29295,

			// === Hunter outfit ===
			10063, 10061, 29269, 29263, 29267, 29265, 10059, 10057,
			29286, 29289, 29283, 29280, 10067, 10065, 10055, 10053,
			10045, 10041, 10043, 10051, 10047, 10049, 10069, 10039,
			10035, 10037, 10075, 10071, 9948, 9950,

			// === Baits & potions ===
			10004, 10002, 10000, 9998, 11519, 11517, 33625, 31261,
			9996, 9994, 31635, 31632, 31629, 31626, 29277,

			// === Chinchompas ===
			11959, 13324, 29221, 29235, 10033, 10034,

			// === Salamanders & lizards ===
			10148, 10142, 10145, 28831, 10143, 29231, 29234, 29238,
			29225, 1939, 10144, 28834, 10149, 10146, 10147,

			// === Butterflies & moths ===
			29230, 10014, 29207, 29189, 29227, 29224, 28893, 29213,
			29195, 29240, 10020, 29204, 29186, 10018, 29198, 29180,
			10016, 29201, 29183, 28890, 29210, 29192, 29237,

			// === Furs & hides ===
			10121, 10115, 10127, 10123, 10119, 29163, 10099, 10103,
			10095, 29292, 29171, 29174, 10117, 10125, 29168, 29177,
			10097, 10101, 10093,

			// === Hunter meats ===
			29131, 29134, 29149, 29152, 29146, 29143, 29137, 29140,
			29128, 29101, 9986, 9978, 29107, 20874, 29119, 20870,
			20878, 29125, 29122, 29113, 20880, 20876, 20872, 20882,
			29110, 3226, 2134, 29116, 29104,

			// === Hunter tertiaries ===
			11264, 11266, 29236, 29241, 31712, 29323, 29321, 31674,
			10089, 31708, 19665, 10092, 9951, 29233, 29229, 31235,
			29319, 21509, 29239, 31241, 29166, 10113, 10105, 10109,
			10111, 29223, 29232, 29228, 29226, 10107, 8942, 10091,
			28962, 10134, 31677, 31671, 10088, 28487, 10087, 10132,
			33651, 29222, 29317, 10090,

			// === Implings & impling jars ===
			11256, 23768, 11238, 11244, 11248, 11246, 11242, 11260,
			11273, 19732, 11252, 11250, 11254, 11240,

			// === Birdhouse items ===
			21512, 22201, 22195, 22192, 21515, 22204, 21521, 21518,
			22198,
			// Separate-ID variants (degradation / charges / trims) + bird
			// nests, ported from the PR #2/#4 era data (pre-regen)
			9949, 31243
		));
	}

	private static void addConstruction(Map<String, List<Integer>> m)
	{
		// CONSTRUCTION — 283 items
		//   Construction tools (7), Planks (6), Nails (8), Building materials
		//   (17), Flatpacks & furniture (107), Garden & bagged plants (25),
		//   Mounted heads & decor (61), Blueprints & contracts (10), Construction
		//   outfit & rewards (42)
		m.put(TAG_CONSTRUCTION, Arrays.asList(
			// === Construction tools ===
			9625, 9626, 24880, 29774, 2347, 25644, 8794,

			// === Planks ===
			8782, 21036, 8778, 960, 24882, 8780,

			// === Nails ===
			4819, 4820, 1539, 4821, 4822, 4823, 4824, 31406,

			// === Building materials ===
			8790, 1783, 434, 10977, 8784, 3211, 3420, 10976,
			8788, 6332, 8786, 1775, 1761, 6333, 6285, 6281,
			6283,

			// === Flatpacks & furniture ===
			8524, 8624, 8628, 29619, 8638, 8520, 8516, 8510,
			8566, 9853, 8552, 8570, 9855, 8556, 8636, 8526,
			8518, 8496, 8538, 8536, 8626, 8604, 8586, 8574,
			9846, 8594, 8608, 8588, 9857, 8622, 8630, 8522,
			8528, 8580, 8642, 8584, 8634, 9864, 9848, 8508,
			9861, 8572, 8514, 9845, 8546, 8606, 8544, 9867,
			9856, 8558, 8648, 9851, 8620, 9847, 9858, 8504,
			9859, 8578, 8564, 8512, 9843, 8502, 8590, 8550,
			8612, 8600, 9865, 8530, 8534, 9852, 8598, 8644,
			9849, 9862, 8614, 8560, 8632, 30557, 30554, 8500,
			8596, 8610, 8640, 8506, 9860, 8582, 9844, 8592,
			8542, 8568, 8616, 8602, 8540, 9866, 8532, 9854,
			8554, 8646, 9850, 9863, 8618, 25093, 25096, 8548,
			8576, 8562, 8498,

			// === Garden & bagged plants ===
			8455, 8453, 8417, 8451, 8429, 8425, 8459, 8419,
			8421, 8431, 8433, 8435, 8461, 8457, 8423, 8427,
			8445, 31027, 24977, 8439, 8441, 8449, 8447, 8437,
			8443,

			// === Mounted heads & decor ===
			7979, 23077, 7995, 7977, 7989, 25559, 7993, 7991,
			7976, 7975, 7999, 7996, 31024, 8000, 23064, 19701,
			22106, 12007, 24495, 23525, 32921, 13277, 12885, 25524,
			13245, 25521, 21745, 12936, 29786, 8001, 7980, 7997,
			7981, 22671, 7978, 8006, 8002, 8005, 7998, 8003,
			8004, 7986, 7984, 7990, 25561, 7994, 7992, 7983,
			7982, 31410, 31414, 31422, 23079, 7987, 7988, 22673,
			7985, 31418, 31426, 31430, 21909,

			// === Blueprints & contracts ===
			33015, 33122, 24885, 24940, 25612, 32085, 32083, 28628,
			24884, 24463,

			// === Construction outfit & rewards ===
			20611, 20615, 24878, 24872, 24874, 24876, 26266, 7730,
			7733, 7736, 22710, 24287, 7728, 31032, 20609, 7690,
			25316, 31205, 7691, 7688, 20613, 970, 7732, 7735,
			7692, 7704, 7716, 7679, 7758, 7974, 28529, 964,
			7738, 7702, 7700, 7771, 7763, 7767, 7759, 7676,
			9789, 9791,
			// Separate-ID variants (degradation / charges / trims) + bird
			// nests, ported from the PR #2/#4 era data (pre-regen)
			9790
		));
	}

	private static void addMisc(Map<String, List<Integer>> m)
	{
		// MISC — 1790 items
		//   Currency & exchange tokens (14), General teleports (5), Jewellery
		//   teleports (14), Utility containers (40), Clue scroll items (705), Keys
		//   & access (87), Books & documents (150), Lamps & XP rewards (38),
		//   Sigils & trinkets (85), Fossils & museum (34), Minigame rewards (93),
		//   Consumables & supplies (48), General tools (20), Uncategorized (457)
		m.put(TAG_MISC, Arrays.asList(
			// === Currency & exchange tokens ===
			11180, 13307, 617, 995, 6964, 4278, 24711, 21555,
			11179, 13190, 8951, 13204, 6529, 6306,

			// === General teleports ===
			30361, 13666, 5614, 24441, 29455,

			// === Jewellery teleports ===
			13134, 13135, 11136, 11138, 11140, 13103, 2572, 11988,
			11986, 11984, 11980, 12785, 20786, 12783,

			// === Utility containers ===
			25463, 25457, 25459, 30002, 7587, 7588, 7589, 7591,
			10521, 24523, 2957, 25106, 25582, 12854, 30357, 33384,
			25467, 24946, 29244, 29246, 29248, 30644, 29309, 29303,
			29297, 30359, 28140, 11941, 24585, 29301, 29996, 27086,
			30692, 24587, 33135, 25465, 29299, 29295, 25461, 25580,

			// === Clue scroll items ===
			405, 25590, 2714, 2715, 2842, 2576, 23129, 13648,
			13651, 13650, 13649, 12789, 30363, 23442, 20358, 20364,
			20362, 20360, 23127, 19712, 19718, 19716, 19714, 713,
			23182, 2677, 2678, 2679, 2680, 2681, 2682, 2683,
			2684, 2685, 2686, 2687, 2688, 2689, 2690, 2691,
			2692, 2693, 2694, 2695, 2696, 2697, 2698, 2699,
			2700, 2701, 2702, 2703, 2704, 2705, 2706, 2707,
			2708, 2709, 2710, 2711, 2712, 2713, 2716, 2719,
			3490, 3491, 3492, 3493, 3494, 3495, 3496, 3497,
			3498, 3499, 3500, 3501, 3502, 3503, 3504, 3505,
			3506, 3507, 3508, 3509, 3510, 3512, 3513, 3514,
			3515, 3516, 3518, 7236, 7238, 10180, 10182, 10184,
			10186, 10188, 10190, 10192, 10194, 10196, 10198, 10200,
			10202, 10204, 10206, 10208, 10210, 10212, 10214, 10216,
			10218, 10220, 10222, 10224, 10226, 10228, 10230, 10232,
			12162, 12164, 12166, 12167, 12168, 12169, 12170, 12172,
			12173, 12174, 12175, 12176, 12177, 12178, 12179, 12181,
			12182, 12183, 12184, 12185, 12186, 12187, 12188, 12189,
			12190, 12191, 12192, 19814, 19816, 19817, 19818, 19819,
			19820, 19821, 19822, 19823, 19824, 19825, 19826, 19828,
			19829, 19830, 19831, 19833, 22001, 23149, 23150, 23151,
			23152, 23153, 23154, 23155, 23156, 23157, 23158, 23159,
			23160, 23161, 23162, 23163, 23164, 23165, 23166, 25788,
			25789, 28913, 28914, 29853, 29854, 30928, 31268, 12073,
			12074, 12075, 12076, 12077, 12078, 12079, 12080, 12081,
			12082, 12083, 12085, 12086, 12087, 12088, 12089, 12090,
			12091, 12092, 12093, 12094, 12095, 12096, 12097, 12098,
			12099, 12100, 12101, 12102, 12103, 12104, 12105, 12106,
			12107, 12108, 12109, 12110, 12111, 12127, 12130, 12132,
			12133, 12134, 12135, 12136, 12137, 12138, 12140, 12141,
			12142, 12143, 12144, 12145, 12146, 12147, 12148, 12149,
			12150, 12151, 12152, 12153, 12154, 12155, 12156, 12157,
			12158, 12159, 19782, 19783, 19784, 19785, 19786, 19787,
			19788, 19789, 19790, 19791, 19792, 19793, 19794, 19795,
			19796, 19797, 19798, 19799, 19800, 19801, 19802, 19803,
			19804, 19805, 19806, 19807, 19808, 19809, 19810, 19811,
			19813, 21524, 21525, 22000, 23144, 23145, 23146, 23147,
			23148, 23770, 24253, 24773, 25498, 25499, 25786, 25787,
			26943, 26944, 28910, 28911, 28912, 29855, 29856, 30932,
			31269, 31270, 31271, 2722, 2723, 2725, 2727, 2729,
			2731, 2733, 2735, 2737, 2739, 2741, 2743, 2745,
			2747, 2773, 2774, 2776, 2778, 2780, 2782, 2783,
			2785, 2786, 2788, 2790, 2792, 2793, 2794, 2796,
			2797, 2799, 3520, 3522, 3524, 3525, 3526, 3528,
			3530, 3532, 3534, 3536, 3538, 3540, 3542, 3544,
			3546, 3548, 3550, 3552, 3554, 3556, 3558, 3560,
			3562, 3564, 3566, 3568, 3570, 3572, 3573, 3574,
			3575, 3577, 3579, 3580, 7239, 7241, 7243, 7245,
			7247, 7248, 7249, 7250, 7251, 7252, 7253, 7254,
			7255, 7256, 7258, 7260, 7262, 7264, 7266, 7268,
			7270, 7272, 10234, 10236, 10238, 10240, 10242, 10244,
			10246, 10248, 10250, 10252, 12542, 12544, 12546, 12548,
			12550, 12552, 12554, 12556, 12558, 12560, 12562, 12564,
			12566, 12568, 12570, 12572, 12574, 12576, 12578, 12581,
			12584, 12587, 12590, 19840, 19842, 19844, 19846, 19848,
			19850, 19852, 19853, 19854, 19856, 19857, 19858, 19860,
			19862, 19864, 19866, 19868, 19870, 19872, 19874, 19876,
			19878, 19880, 19882, 19884, 19886, 19888, 19890, 19892,
			19894, 19896, 19898, 19900, 19902, 19904, 19906, 19908,
			19910, 21526, 21527, 23045, 23167, 23168, 23169, 23170,
			23172, 23174, 23175, 23176, 23177, 23178, 23179, 23180,
			23181, 24493, 25790, 25791, 25792, 26566, 28915, 28916,
			28918, 29859, 30929, 30931, 31272, 31273, 19835, 2801,
			2803, 2805, 2807, 2809, 2811, 2813, 2815, 2817,
			2819, 2821, 2823, 2825, 2827, 2829, 2831, 2833,
			2835, 2837, 2839, 2841, 2843, 2845, 2847, 2848,
			2849, 2851, 2853, 2855, 2856, 2857, 2858, 3582,
			3584, 3586, 3588, 3590, 3592, 3594, 3596, 3598,
			3599, 3601, 3602, 3604, 3605, 3607, 3609, 3610,
			3611, 3612, 3613, 3614, 3615, 3616, 3617, 3618,
			7274, 7276, 7278, 7280, 7282, 7284, 7286, 7288,
			7290, 7292, 7294, 7296, 7298, 7300, 7301, 7303,
			7304, 7305, 7307, 7309, 7311, 7313, 7315, 7317,
			10254, 10256, 10258, 10260, 10262, 10264, 10266, 10268,
			10270, 10272, 10274, 10276, 10278, 12021, 12023, 12025,
			12027, 12029, 12031, 12033, 12035, 12037, 12039, 12041,
			12043, 12045, 12047, 12049, 12051, 12053, 12055, 12057,
			12059, 12061, 12063, 12065, 12067, 12069, 12071, 19734,
			19736, 19738, 19740, 19742, 19744, 19746, 19748, 19750,
			19752, 19754, 19756, 19758, 19760, 19762, 19764, 19766,
			19768, 19770, 19772, 19774, 19776, 19778, 19780, 23046,
			23131, 23133, 23135, 23136, 23137, 23138, 23139, 23140,
			23141, 23142, 23143, 25783, 25784, 28907, 28908, 28909,
			29857, 29858, 30933, 30935, 31274, 31275, 27427, 2832,
			30904, 30908, 30920, 30916, 30924, 30912, 23184, 30926,
			30902, 30906, 30918, 30914, 30922, 30910, 2795, 23245,
			20546, 20543, 20544, 19836, 20545, 24361, 24362, 24365,
			24364, 24366, 24363, 2574, 2751, 3624, 3650, 19837,
			2575,

			// === Keys & access ===
			29408, 29323, 24442, 13302, 3463, 3461, 3462, 3464,
			3460, 25448, 20608, 20526, 983, 8867, 3453, 3451,
			3452, 3454, 3450, 25442, 29923, 19567, 989, 25244,
			1590, 11942, 26388, 25648, 23951, 31744, 30763, 20754,
			25430, 25426, 25428, 25432, 25424, 25454, 23499, 8869,
			1591, 13248, 987, 30107, 26651, 30109, 22374, 991,
			4850, 22428, 25650, 6966, 7678, 31756, 1854, 8868,
			3468, 3466, 3467, 3469, 3465, 25451, 25646, 993,
			32416, 32418, 32420, 32419, 32415, 32413, 32412, 32411,
			32414, 8866, 3458, 3456, 3457, 3459, 3455, 4446,
			25445, 31732, 23502, 985, 30105, 9662, 29449,

			// === Books & documents ===
			5520, 7922, 21631, 25688, 21668, 11341, 11342, 11343,
			11345, 11346, 11347, 11348, 25692, 21670, 21629, 6891,
			31079, 24263, 28767, 455, 24055, 22420, 25476, 6767,
			5508, 7464, 8989, 8990, 13531, 4055, 8463, 23104,
			24049, 20886, 4707, 20899, 13514, 13515, 13516, 13518,
			13519, 13520, 13521, 10829, 6798, 24065, 22422, 11904,
			21672, 11677, 9903, 956, 7681, 6799, 3901, 6800,
			23015, 23011, 23009, 23021, 23017, 23013, 23019, 11656,
			10999, 6801, 24059, 21678, 9595, 6802, 13528, 20897,
			6803, 5, 23670, 6804, 11842, 22287, 6808, 6805,
			31071, 31073, 12786, 33233, 22475, 19668, 9078, 30035,
			26154, 21676, 11339, 24763, 24765, 24767, 24771, 550,
			11169, 11171, 20888, 31343, 796, 1685, 10860, 10861,
			12935, 31121, 5506, 25829, 23023, 23025, 23027, 23031,
			23033, 23035, 24053, 13279, 972, 21666, 10562, 13524,
			13525, 28958, 21664, 10512, 9003, 22504, 1848, 21682,
			6806, 20798, 24063, 25197, 24067, 22991, 5507, 24761,
			9004, 20890, 24073, 13396, 20893, 33229, 13537, 25474,
			13535, 24187, 21756, 28972, 29809, 6807,

			// === Lamps & XP rewards ===
			4447, 6543, 7498, 11137, 11189, 11679, 13145, 13146,
			13147, 13148, 21262, 21640, 23072, 23082, 25753, 25820,
			25920, 25921, 25922, 25923, 25924, 25925, 27299, 27543,
			28800, 30960, 32871, 13513, 11640, 22320, 31212, 31214,
			31211, 31216, 2528, 6796, 24528, 28587,

			// === Sigils & trinkets ===
			26132, 29679, 33058, 26009, 26066, 33056, 25994, 26012,
			26099, 33062, 26018, 26069, 33060, 26057, 28520, 26126,
			26006, 26015, 26105, 28481, 26144, 29649, 26054, 26096,
			29667, 26129, 28514, 26123, 26081, 26135, 26141, 25991,
			28490, 29670, 29652, 26108, 26060, 26042, 26021, 26120,
			28478, 29664, 28517, 26090, 28511, 26084, 26087, 26048,
			26075, 26030, 28496, 26102, 25997, 26111, 26093, 33054,
			26147, 29676, 28508, 26078, 26003, 28526, 26063, 26045,
			29655, 26000, 26072, 26117, 26138, 29673, 26051, 28499,
			28523, 26114, 28567, 33044, 28564, 33047, 28570, 28561,
			26547, 26544, 26548, 26545, 26546,

			// === Fossils & museum ===
			25690, 25694, 25686, 11181, 694, 674, 21594, 27216,
			21596, 21598, 21590, 21592, 21600, 21606, 21604, 21608,
			21602, 21580, 21586, 21584, 21588, 21582, 11184, 11183,
			21570, 21576, 21574, 21578, 21572, 11175, 21566, 21564,
			21568, 21562,

			// === Minigame rewards ===
			28080, 2996, 21807, 22299, 22305, 21813, 21810, 24565,
			12746, 12747, 1464, 10516, 10556, 28074, 10533, 25212,
			24329, 28082, 28084, 28086, 28088, 28090, 28092, 28094,
			28096, 28098, 30576, 30619, 30616, 29482, 30690, 4067,
			12659, 21396, 10560, 10557, 24130, 24131, 28072, 22330,
			10538, 10558, 28078, 28076, 27997, 20791, 10566, 21186,
			12955, 27462, 10531, 10526, 10559, 21297, 25975, 27870,
			5606, 26884, 5020, 24277, 30113, 10563, 10537, 26886,
			619, 620, 10535, 10539, 10540, 30402, 24861, 10532,
			25228, 7774, 9474, 10934, 26706, 13563, 621, 10536,
			10561, 25342, 26885, 20703, 20527, 29388, 31123, 26882,
			8851, 24859, 26883, 24860, 10534,

			// === Consumables & supplies ===
			11737, 11736, 11735, 11734, 29972, 6561, 29971, 4049,
			25202, 25730, 23107, 7157, 11083, 11081, 11079, 712,
			7921, 29973, 4045, 25211, 10546, 10545, 10544, 10543,
			10028, 10027, 1992, 3165, 27341, 27339, 26278, 26924,
			9475, 21711, 27297, 195, 10476, 13382, 21712, 29409,
			23839, 23879, 12844, 23106, 1829, 1827, 1825, 1823,

			// === General tools ===
			33091, 8986, 9660, 9659, 7004, 4047, 32865, 22660,
			9415, 4018, 2871, 7756, 675, 954, 4498, 20587,
			6097, 5327, 945, 4051,

			// === Uncategorized ===
			3904, 3906, 3908, 3912, 3914, 3916, 3918, 3952,
			3954, 3956, 3960, 3962, 3964, 3966, 20283, 20284,
			20285, 20287, 20288, 20289, 20290, 20307, 20308, 20309,
			20311, 20312, 20313, 20314, 20331, 20332, 20333, 20335,
			20336, 20337, 20338, 23418, 23419, 23420, 23422, 23423,
			23424, 23425, 8858, 8859, 24051, 9596, 22039, 30682,
			29458, 31084, 9477, 21804, 22302, 25938, 25941, 25944,
			25950, 25953, 27304, 22508, 13578, 11176, 30393, 28334,
			31454, 20594, 33221, 11340, 4053, 25209, 684, 27221,
			5074, 8976, 11067, 1666, 1647, 2875, 22430, 25195,
			8936, 8943, 25139, 25199, 3667, 22999, 9011, 7686,
			24941, 21817, 7773, 8979, 687, 30688, 7540, 7541,
			4614, 4496, 10835, 22424, 33237, 22519, 27293, 20885,
			12694, 2884, 25721, 11195, 3188, 33235, 22711, 25956,
			12835, 3271, 30816, 30831, 30819, 30840, 30822, 30810,
			30837, 30825, 30813, 30828, 30834, 23103, 3147, 25959,
			23824, 33103, 10513, 27308, 1633, 30384, 25104, 23962,
			23866, 3063, 22366, 697, 25571, 22778, 21027, 7684,
			33065, 22426, 31111, 1837, 30394, 1835, 1833, 30803,
			33697, 24545, 33644, 30328, 5054, 12863, 27426, 11997,
			6075, 13526, 27223, 23943, 7960, 6675, 409, 7496,
			25961, 30997, 33227, 22418, 4008, 4009, 27219, 1411,
			21253, 11525, 4653, 272, 6202, 8864, 21180, 33239,
			6663, 29961, 8863, 30395, 27622, 29895, 6814, 13394,
			33537, 5562, 5563, 5564, 5566, 5567, 33393, 33387,
			33390, 27544, 751, 4692, 27225, 24418, 8865, 6677,
			6678, 1413, 24529, 27302, 6189, 979, 11996, 7682,
			21624, 13532, 22043, 22041, 31345, 31347, 11088, 13395,
			26908, 1417, 11177, 12656, 30396, 20884, 13527, 33218,
			30400, 12693, 25117, 9008, 3177, 24075, 20355, 34,
			7777, 8932, 22045, 33665, 20892, 29955, 28769, 27296,
			33424, 299, 24534, 7416, 7418, 4010, 4034, 2974,
			2972, 30397, 32864, 32869, 4181, 6199, 5561, 30122,
			24061, 29098, 30744, 22364, 13195, 685, 11182, 695,
			21680, 31077, 8860, 13187, 2340, 31399, 21674, 31817,
			23865, 466, 8930, 30365, 197, 1235, 818, 10541,
			4495, 4500, 26887, 26894, 26895, 26892, 26896, 26893,
			26889, 26890, 26898, 26891, 26897, 26888, 11178, 33133,
			4238, 10946, 29307, 13533, 6200, 11704, 25207, 8938,
			8946, 21826, 21832, 21829, 21820, 9007, 4202, 24735,
			968, 1480, 4043, 2203, 7636, 5558, 22321, 22437,
			13205, 2959, 13327, 5733, 22541, 33629, 2518, 30890,
			28896, 10947, 9012, 32867, 32863, 6721, 31016, 28773,
			4037, 4041, 13256, 9468, 27306, 27214, 28771, 6679,
			6680, 22047, 25831, 24299, 3680, 7778, 8857, 950,
			10948, 965, 2749, 2750, 2753, 2754, 2755, 2756,
			3619, 3620, 3621, 3623, 3625, 3626, 3643, 3644,
			3645, 3647, 3648, 3649, 31351, 733, 13233, 7800,
			25201, 19637, 33231, 3273, 10949, 9702, 30679, 30953,
			3893, 5732, 3062, 23183, 464, 9009, 33653, 31441,
			28924, 27314, 8981, 3231, 3805, 4557, 799, 22506,
			25567, 24057, 6079, 30123, 27312, 24071, 22510, 24069,
			22416, 27310, 22049, 22512, 8862, 966, 5568, 30398,
			10514, 9010, 595, 8987, 13534, 13536, 7677, 30689,
			13530, 8861, 13273, 22191, 31054, 20895, 30401, 710,
			33661, 33663, 22514, 2425, 30685, 27295, 23834, 23871,
			2885, 30399, 13529, 24991, 943, 10515, 29969, 4039,
			4042,
			// Separate-ID variants (degradation / charges / trims) + bird
			// nests, ported from the PR #2/#4 era data (pre-regen)
			11982, 20787, 20788, 20789, 20790, 21816, 27902, 27906,
			27910, 27914, 27918, 27922, 27925, 27928, 27931, 27934,
			27937, 27940, 27943, 27946, 27949, 27952, 27955, 33104
		));
	}

	private static void addQuests(Map<String, List<Integer>> m)
	{
		// QUESTS — 1926 items
		//   Achievement capes (4), Weapons & wieldables (43), Armour & clothing
		//   (123), Keys & access items (167), Books & lore (332), Quest
		//   consumables (108), Artefacts & relics (141), Remains & trophies (85),
		//   Quest supplies & materials (353), Quest items (570)
		m.put(TAG_QUESTS, Arrays.asList(
			// === Achievement capes ===
			24855, 24857, 9813, 9814,

			// === Weapons & wieldables ===
			2538, 7807, 7808, 7809, 7806, 24699, 24697, 33713,
			667, 598, 27418, 278, 746, 33709, 27783, 33711,
			24695, 35, 6082, 747, 27422, 33718, 78, 2532,
			25979, 21059, 2536, 6541, 27424, 29911, 25822, 6773,
			24693, 2540, 4236, 2963, 27420, 33800, 33801, 4083,
			33790, 2534, 33722,

			// === Armour & clothing ===
			774, 773, 4035, 23785, 23787, 23789, 87, 11014,
			616, 4502, 4284, 29931, 6752, 6750, 9055, 287,
			7918, 25818, 10865, 10863, 10864, 29914, 29915, 6707,
			2405, 4677, 6544, 5609, 26969, 26967, 9644, 9640,
			9642, 33103, 10500, 3208, 29566, 29570, 29568, 4611,
			6547, 10171, 29560, 29562, 29564, 29868, 29872, 29870,
			29874, 10172, 31357, 1506, 552, 295, 589, 288,
			4567, 5607, 9057, 10862, 2406, 29932, 4591, 75,
			74, 1495, 421, 4021, 28426, 430, 6069, 6070,
			6068, 6065, 6067, 6548, 26963, 286, 86, 9059,
			284, 285, 30947, 31353, 31355, 9058, 29929, 7917,
			9054, 24942, 4657, 6786, 6787, 9083, 6790, 10839,
			10836, 10838, 10837, 1796, 1846, 1845, 1844, 29933,
			3107, 26910, 31398, 11673, 11672, 11671, 11669, 11668,
			11667, 11666, 24678, 24680, 24676, 9636, 9638, 9634,
			26567, 29930, 9056,

			// === Keys & access items ===
			1507, 6792, 4028, 22093, 27369, 681, 26958, 30311,
			2887, 4027, 1852, 25803, 605, 4272, 2418, 5585,
			19566, 4184, 1840, 29534, 3136, 3137, 432, 709,
			2404, 4273, 28573, 30316, 25804, 19569, 19568, 33726,
			780, 26959, 25963, 4077, 25810, 28419, 4617, 2409,
			9654, 22087, 22092, 22088, 28420, 33731, 26571, 21054,
			28417, 28965, 6754, 28964, 782, 27, 3741, 26356,
			26358, 26360, 26364, 26362, 33736, 33724, 33727, 33729,
			788, 4004, 601, 2944, 4026, 28803, 33725, 26575,
			26957, 3696, 3697, 26573, 27608, 28967, 28966, 2945,
			33780, 4031, 275, 293, 298, 423, 1543, 1544,
			1545, 1546, 1547, 1548, 2411, 5010, 9722, 11039,
			30951, 2423, 4589, 30961, 76, 19525, 9651, 33734,
			3848, 33781, 21052, 30308, 1542, 1839, 1586, 33783,
			26572, 6104, 33728, 4024, 4025, 28430, 4839, 28421,
			28405, 758, 31297, 3135, 33735, 21053, 28418, 29523,
			26960, 21055, 28416, 26574, 26956, 3745, 28370, 33784,
			4186, 85, 2399, 2400, 2401, 28441, 33730, 3269,
			29906, 28574, 28406, 6083, 28389, 7632, 2378, 33782,
			4185, 29877, 33786, 28404, 28973, 33785, 4656, 759,
			28407, 6084, 1843, 26576, 4078, 4029, 4030,

			// === Books & lore ===
			9627, 6949, 718, 1508, 11680, 22033, 27300, 5588,
			11003, 22775, 27597, 28375, 4428, 1816, 29517, 7995,
			600, 3894, 6793, 30310, 4574, 28378, 2886, 9717,
			624, 29905, 3230, 730, 10173, 1817, 757, 1509,
			29878, 7144, 4829, 4248, 4817, 19515, 292, 711,
			5065, 4569, 4570, 4571, 33423, 7961, 1818, 29922,
			9646, 9647, 9648, 769, 3114, 21759, 21775, 22365,
			22367, 22760, 22777, 29428, 10594, 23814, 23815, 23816,
			29883, 3161, 1538, 9718, 608, 28133, 11001, 1819,
			25814, 2408, 3395, 3846, 6770, 27515, 9682, 9589,
			9590, 27600, 29596, 33789, 25706, 7629, 27595, 6649,
			7996, 30307, 1820, 4686, 30949, 21259, 33707, 4654,
			28437, 10179, 22413, 21662, 28983, 1821, 3699, 11007,
			33767, 785, 5009, 3895, 28468, 28424, 33763, 29558,
			1856, 9652, 24672, 11173, 2403, 786, 29427, 7951,
			7634, 1494, 1584, 26366, 28974, 761, 794, 696,
			3207, 3103, 6648, 2967, 3845, 4203, 6755, 24686,
			27511, 7997, 1815, 3206, 25801, 33773, 4204, 4615,
			6121, 6756, 6757, 7966, 21774, 11009, 691, 692,
			693, 9719, 28428, 7972, 6479, 25704, 28986, 30318,
			27298, 22037, 3847, 1535, 1536, 1537, 22009, 22010,
			22011, 22013, 22014, 22015, 22016, 4274, 4275, 4276,
			5066, 755, 9633, 9649, 26942, 7998, 10597, 10598,
			10599, 6071, 29525, 29526, 28401, 6769, 11198, 33764,
			19505, 4837, 25152, 4597, 4598, 21056, 21057, 21058,
			29524, 9025, 30946, 31337, 3, 27601, 22411, 1493,
			24682, 22051, 22410, 22774, 25797, 28431, 9684, 9685,
			9686, 4572, 4573, 10585, 11036, 5012, 28380, 4283,
			1510, 433, 29879, 33746, 666, 28382, 10886, 28434,
			6073, 28412, 3709, 28436, 714, 7543, 4603, 25793,
			6546, 33009, 28432, 28433, 28423, 291, 10492, 25824,
			28376, 25802, 25805, 33795, 9904, 33703, 4575, 4578,
			4576, 4577, 717, 19511, 23773, 6758, 7968, 9721,
			10485, 33704, 719, 3704, 683, 9653, 3104, 27602,
			28377, 25816, 11202, 4816, 1802, 10856, 11002, 4814,
			2376, 720, 9715, 33701, 25967, 27599, 28379, 11010,
			29533, 11034, 7411, 10587, 33794, 23510, 607, 23512,
			23514, 23007, 24684, 28394, 1850, 28435, 33126, 29926,
			11203, 4809, 3701, 19513, 10493, 4655, 784, 4249,
			4277, 23067, 3896, 27596, 25702, 1822, 24256, 682,
			11006, 33768, 4616, 22035, 28429, 27518, 4435, 22591,
			28444, 27513, 28579, 11011,

			// === Quest consumables ===
			5589, 33797, 10841, 28351, 28355, 7508, 6123, 22407,
			26965, 739, 7531, 756, 7542, 6766, 33769, 33772,
			7530, 7470, 4245, 4838, 26962, 11154, 1501, 23800,
			23818, 33810, 33809, 33808, 33807, 26587, 26585, 26583,
			26581, 4604, 1504, 28353, 23806, 431, 3164, 3711,
			6118, 77, 3707, 416, 3712, 2395, 28980, 7471,
			28350, 28352, 4239, 6468, 273, 6768, 274, 2394,
			22409, 21531, 33806, 33805, 33804, 33803, 33814, 33813,
			33812, 33811, 33818, 33817, 33816, 33815, 24, 28422,
			7529, 22589, 28354, 22096, 2379, 3414, 3412, 3410,
			3408, 3419, 3418, 3417, 3416, 25813, 33820, 4836,
			28383, 28388, 26904, 25812, 29531, 29898, 33771, 9658,
			9657, 9656, 3265, 6952, 29532, 22406, 22408, 28386,
			13348, 13349, 13350, 33770,

			// === Artefacts & relics ===
			29539, 22086, 1498, 1499, 1497, 23071, 27607, 11049,
			4489, 29542, 29551, 23782, 4808, 6650, 6064, 6644,
			23780, 7520, 3127, 7570, 22084, 7956, 741, 29545,
			6635, 9681, 11032, 11033, 2380, 23802, 28577, 28575,
			23804, 23808, 4313, 6653, 6643, 23779, 28439, 2529,
			29544, 6748, 6747, 29540, 26903, 33765, 33766, 29549,
			6646, 23784, 750, 296, 28817, 6642, 23778, 23783,
			744, 742, 29548, 22079, 27605, 4590, 6996, 28384,
			611, 612, 613, 614, 22081, 743, 6994, 4020,
			4022, 6645, 23781, 29550, 6950, 4187, 24030, 24031,
			24032, 24033, 11197, 21261, 23069, 6651, 4188, 4818,
			21837, 2372, 28438, 6821, 1481, 1482, 1483, 23812,
			587, 588, 1577, 29547, 6995, 29546, 6640, 23776,
			6820, 2373, 2374, 2375, 29541, 28368, 6066, 22083,
			3849, 5519, 28819, 28807, 28818, 28465, 4183, 4618,
			6785, 28976, 699, 26954, 27519, 28816, 29876, 30954,
			29535, 29543, 29536, 7878, 1857, 22085, 24258, 24259,
			27523, 6641, 23777, 749, 6993,

			// === Remains & trophies ===
			671, 7839, 7899, 7833, 7815, 6946, 25794, 618,
			21530, 7813, 7950, 604, 28970, 10177, 7905, 4488,
			9720, 7857, 7902, 7408, 0, 10167, 11279, 7893,
			24254, 1583, 7872, 553, 7881, 7827, 7824, 7812,
			2950, 10175, 11196, 2391, 1502, 7875, 7914, 7845,
			18, 3130, 3180, 10174, 7851, 3166, 7869, 28969,
			26591, 7532, 7842, 2377, 33741, 3128, 4621, 7896,
			7818, 609, 7836, 300, 26592, 7887, 280, 281,
			282, 283, 10176, 10904, 10950, 3179, 26590, 7576,
			26589, 26593, 28982, 7884, 7890, 7821, 1487, 7911,
			7866, 7830, 610, 7848, 7863,

			// === Quest supplies & materials ===
			446, 3731, 3726, 3730, 3725, 3724, 3723, 22764,
			22763, 2888, 5578, 29552, 28448, 9693, 1842, 30963,
			6094, 708, 28345, 28346, 28385, 11048, 28456, 11156,
			10489, 783, 1841, 3216, 3218, 3220, 3221, 28364,
			7973, 10895, 10870, 38, 21, 4622, 4620, 28357,
			9692, 9694, 9698, 9696, 9690, 722, 2954, 6710,
			24691, 28453, 22, 6086, 3719, 28804, 28805, 6953,
			3692, 30945, 4442, 33748, 5602, 19517, 7622, 4693,
			4687, 6712, 30, 753, 7001, 7145, 4678, 30312,
			973, 707, 33778, 6759, 26955, 6545, 25145, 4200,
			4205, 7463, 28455, 7630, 32808, 32807, 10899, 1808,
			28462, 5584, 11158, 793, 33777, 11031, 4073, 27368,
			6457, 30968, 9067, 11151, 29925, 28458, 28132, 4568,
			7514, 29553, 28445, 9695, 9066, 3727, 3732, 10486,
			10831, 9085, 4007, 7544, 7546, 731, 29538, 7477,
			33743, 5067, 4191, 2384, 29554, 28447, 9699, 6673,
			9687, 9688, 9689, 3728, 3733, 3722, 3729, 28981,
			4190, 4668, 4075, 721, 723, 2947, 2946, 3694,
			6088, 23793, 1527, 1529, 1531, 11155, 704, 7528,
			7527, 7517, 9594, 3215, 3264, 9088, 9090, 7108,
			28442, 5579, 4487, 7959, 10834, 11052, 4416, 25968,
			727, 728, 28343, 28344, 27604, 7941, 30969, 33819,
			7810, 19519, 3153, 3155, 10885, 10898, 6113, 6112,
			7516, 9094, 28977, 9724, 30967, 28460, 28454, 602,
			83, 27603, 10832, 4684, 25809, 33761, 7970, 7971,
			9592, 2410, 3718, 5604, 33007, 24940, 9089, 31330,
			9725, 10876, 10873, 33128, 28449, 9697, 705, 4023,
			29079, 4492, 2953, 28341, 28342, 22402, 6095, 3222,
			28451, 4237, 10833, 603, 10178, 277, 1485, 33779,
			6458, 6459, 2424, 28366, 26569, 4689, 6955, 6455,
			9723, 10871, 31363, 10872, 418, 7628, 24262, 4440,
			7468, 3214, 7811, 6771, 27010, 9727, 9728, 7969,
			3213, 28463, 23, 6085, 4445, 23795, 6454, 25,
			7121, 7148, 28347, 28348, 11164, 1855, 7533, 9716,
			7649, 30964, 7155, 11046, 6956, 11044, 11045, 6467,
			6466, 28349, 4082, 32083, 26952, 9680, 33792, 28367,
			11054, 11205, 1800, 1530, 9726, 28452, 28978, 6772,
			6460, 9095, 28450, 670, 669, 28459, 28979, 4704,
			25965, 3224, 3209, 25966, 417, 28365, 22095, 11056,
			33752, 7575, 8837, 5583, 7156, 9665, 6096, 6098,
			22769, 22770, 22771, 22773, 6719, 3735, 28363, 2389,
			2390, 22405, 33775, 5582, 9086, 33774, 29556, 6456,
			9087, 2964, 29555, 22094, 28446, 9691, 24938, 24939,
			3705, 20, 4486, 6453, 11035, 29928, 28457, 6087,
			735,

			// === Quest items ===
			6122, 10495, 10840, 10494, 9103, 24842, 2969, 26905,
			28461, 4178, 30989, 31365, 1858, 4436, 3270, 33796,
			701, 33762, 24688, 28409, 33738, 33737, 28369, 23791,
			1528, 4195, 30320, 11050, 2407, 9933, 7564, 29884,
			28390, 33759, 6947, 7908, 422, 1474, 11678, 10889,
			4659, 6711, 4670, 9650, 25147, 25799, 9613, 740,
			23792, 9616, 9615, 6089, 9614, 4415, 33787, 6818,
			10894, 7515, 29519, 7146, 9591, 6081, 690, 1469,
			25811, 763, 4431, 5008, 7473, 7474, 7475, 7476,
			28968, 716, 29912, 688, 4209, 33008, 7118, 7149,
			4579, 7119, 7147, 30950, 22414, 3706, 28403, 19560,
			4443, 90, 5601, 28414, 7472, 23794, 28425, 30941,
			26906, 6638, 10874, 10586, 3102, 4426, 27532, 4201,
			31341, 29217, 7521, 7568, 21263, 10593, 7519, 10893,
			33788, 8871, 1813, 25800, 5577, 3702, 2387, 19636,
			1467, 25815, 33791, 9683, 4197, 10842, 30317, 19562,
			25796, 25798, 6749, 28393, 22761, 4430, 7497, 3267,
			29521, 420, 1492, 28410, 4811, 4180, 7478, 33006,
			11157, 3263, 2968, 3266, 5056, 5060, 6077, 30970,
			23798, 28809, 19559, 20722, 33754, 524, 522, 525,
			7545, 21260, 523, 21800, 22768, 7967, 29530, 415,
			3698, 5064, 4593, 3268, 6718, 31379, 3708, 26,
			1554, 1811, 31328, 31327, 5608, 28551, 7942, 33702,
			3736, 33755, 25146, 7109, 10884, 4189, 22762, 337,
			7518, 3897, 3898, 4674, 294, 22590, 3693, 11210,
			2949, 2951, 2948, 4624, 3261, 4182, 9901, 17,
			9609, 9612, 9611, 6092, 9610, 1588, 26594, 30940,
			11682, 28408, 33798, 6639, 19521, 4001, 9902, 748,
			19, 4682, 15, 5610, 1496, 1500, 4671, 29887,
			29897, 28361, 28464, 28806, 9617, 9620, 9619, 9618,
			11013, 31377, 28, 4427, 33720, 33708, 6716, 4595,
			10600, 33004, 5605, 28413, 30965, 19523, 30962, 9655,
			23516, 33745, 6819, 5062, 4196, 26961, 28808, 31397,
			29573, 3714, 28415, 787, 24261, 3689, 11058, 33760,
			11211, 33117, 4703, 16, 31392, 30944, 5586, 30939,
			7958, 2966, 4253, 7635, 24260, 4033, 4006, 7854,
			3167, 29078, 4490, 33005, 22404, 33802, 22403, 4247,
			4005, 4241, 19558, 703, 5581, 7578, 23796, 31400,
			31338, 22588, 9947, 2385, 9601, 9604, 9603, 9602,
			9934, 4432, 33742, 33740, 1488, 1489, 1490, 677,
			678, 10592, 3695, 11165, 11167, 4623, 4199, 1486,
			424, 33705, 7143, 28987, 4494, 10496, 4244, 6542,
			271, 29867, 29950, 1849, 1853, 1812, 33776, 7410,
			28975, 31375, 14, 7120, 25964, 11008, 29216, 338,
			7566, 29076, 7577, 7572, 1470, 9597, 31405, 3742,
			4610, 9600, 9599, 6090, 9598, 30103, 6954, 290,
			5063, 10866, 25795, 1984, 6093, 21758, 10830, 276,
			4810, 11199, 4484, 29904, 6958, 9943, 6985, 6986,
			6988, 6987, 6945, 6948, 4700, 19527, 28392, 21529,
			21528, 28443, 456, 29880, 29881, 29882, 11681, 31395,
			1466, 3738, 2882, 1552, 3168, 10488, 10857, 4673,
			2397, 729, 6969, 33749, 4444, 5603, 279, 33739,
			29927, 10897, 11204, 29572, 1798, 1804, 1806, 4658,
			5011, 7637, 33747, 6817, 7574, 7511, 6715, 33744,
			3720, 4672, 4606, 4607, 4605, 7580, 7860, 5580,
			7512, 6791, 6636, 24944, 4002, 33758, 672, 4691,
			7513, 6119, 84, 1549, 25631, 3746, 28471, 4425,
			3109, 6989, 7002, 6997, 6999, 6998, 7000, 606,
			27598, 28130, 28360, 4619, 3713, 28427, 3169, 7579,
			3700, 33757, 623, 33751, 33753, 7150, 2388, 29903,
			7573, 673, 6072, 23497, 1851, 33706, 7633, 1579,
			33793, 5592, 31359, 2393, 22415, 24690, 33715, 23504,
			23506, 23508, 33750, 1, 6788, 6789, 4194, 31393,
			419, 3721, 3262, 6478, 4086, 676, 789, 4601,
			4602, 28402, 29518, 10490, 4438, 4683, 3688, 2386,
			3703, 31373, 10875, 7465, 3734, 3737, 33721, 33756,
			24255, 9621, 9624, 9623, 9622, 1532, 24674, 24675,
			24673, 625, 11012, 1503, 3710, 29522, 4085, 4434,
			29527, 29528, 29529, 9593, 30936, 7957, 1476, 4485,
			6464, 6461, 26579, 2421, 1491, 6957, 10896, 10891,
			3744, 6713, 1472, 9605, 9608, 9607, 6091, 9606,
			8870, 8887,
			// Separate-ID variants (degradation / charges / trims) + bird
			// nests, ported from the PR #2/#4 era data (pre-regen)
			13068, 33104
		));
	}

	private static void addSailing(Map<String, List<Integer>> m)
	{
		// SAILING — 569 items
		//   Sailing tools & navigation (12), Construction tools (5), Nails (8),
		//   Planks (7), Shipbuilding materials (26), Ship components (29), Cannons
		//   & cannonballs (22), Keys, charts & schematics (44), Raw sailing fish
		//   (11), Cooked fish & trophies (25), Sea creature parts (52), Salvage
		//   (20), Island resources (35), Boat cocktails & brews (79), Pearls (12),
		//   Cargo & contracts (142), Sailing outfit & rewards (40)
		m.put(TAG_SAILING, Arrays.asList(
			// === Sailing tools & navigation ===
			31989, 31985, 31807, 33074, 31397, 32386, 31297, 31809,
			31803, 31441, 31443, 31371,

			// === Construction tools ===
			24880, 9625, 2347, 25644, 8794,

			// === Nails ===
			4823, 4821, 4819, 31406, 4820, 4822, 4824, 1539,

			// === Planks ===
			31432, 31435, 8782, 8778, 960, 31438, 8780,

			// === Shipbuilding materials ===
			32096, 32102, 32087, 31475, 31976, 32886, 32892, 31746,
			32106, 31758, 32434, 32093, 31979, 32889, 31973, 32110,
			31967, 31734, 31964, 31982, 32099, 32113, 32108, 32090,
			31970, 32104,

			// === Ship components ===
			32011, 31999, 32053, 32017, 31996, 32002, 32056, 32032,
			32020, 32074, 32038, 32023, 32077, 32071, 32029, 32065,
			32080, 32035, 32026, 32068, 32062, 32050, 32008, 32044,
			32059, 32014, 32005, 32047, 32041,

			// === Cannons & cannonballs ===
			31912, 31926, 31940, 31906, 31918, 31932, 32115, 31916,
			31930, 31944, 21728, 31908, 31920, 31934, 31910, 31924,
			31938, 31914, 31928, 31942, 31922, 31936,

			// === Keys, charts & schematics ===
			33143, 33423, 32432, 32406, 32409, 32410, 32404, 32403,
			31744, 32402, 32422, 32428, 32423, 32421, 32426, 32430,
			32424, 32427, 32429, 32425, 32388, 32389, 32390, 32392,
			32393, 32394, 32395, 32408, 32405, 32407, 32401, 31756,
			32416, 32418, 32420, 32417, 32419, 32415, 32413, 32412,
			32411, 32414, 31732, 32396,

			// === Raw sailing fish ===
			31692, 32341, 32309, 32317, 32333, 31561, 32349, 31700,
			31686, 31553, 32325,

			// === Cooked fish & trophies ===
			31695, 32344, 32347, 32315, 32323, 32339, 31567, 32355,
			31559, 32331, 31408, 32312, 31412, 32320, 32336, 31420,
			31564, 32352, 31416, 31424, 31703, 31689, 31428, 31556,
			32328,

			// === Sea creature parts ===
			32845, 32826, 32876, 32836, 32817, 32809, 32828, 32820,
			32839, 32819, 32838, 32844, 32825, 32812, 32831, 31235,
			32357, 32810, 32829, 32822, 32841, 32362, 32846, 32827,
			32813, 31954, 31957, 32832, 32814, 32833, 32843, 32824,
			32834, 32815, 31959, 32882, 32835, 32816, 31572, 31569,
			32821, 32840, 31952, 32842, 32823, 32811, 32830, 32837,
			32818, 33145, 33144, 32360,

			// === Salvage ===
			32851, 31245, 32866, 31961, 31251, 32868, 32865, 32849,
			31253, 32859, 32853, 32857, 32864, 32869, 32861, 32855,
			32867, 32863, 32847, 32870,

			// === Island resources ===
			31478, 31472, 32904, 31502, 31547, 31490, 31460, 31545,
			31469, 31481, 31511, 31457, 31543, 31466, 32907, 31505,
			31549, 31492, 32902, 31716, 31463, 31903, 31719, 31484,
			31513, 32910, 31508, 31551, 31494, 31724, 31722, 31395,
			31393, 31487, 31515,

			// === Boat cocktails & brews ===
			31255, 31889, 31831, 31888, 31838, 31860, 31837, 31878,
			31905, 31854, 31851, 31863, 31893, 31856, 31858, 31849,
			31840, 31881, 31886, 31875, 31833, 31892, 31864, 31873,
			31845, 31848, 31883, 31861, 31901, 31882, 31839, 31843,
			31900, 31899, 31902, 31847, 31844, 31885, 31846, 31842,
			31890, 31887, 31834, 31895, 31897, 31898, 31859, 31891,
			31855, 31835, 31832, 31852, 31862, 31836, 31894, 31876,
			31871, 31874, 31841, 31857, 31872, 31884, 31896, 31877,
			31850, 31853, 31949, 31869, 31865, 31823, 31258, 31814,
			31817, 31879, 31261, 31768, 31867, 31820, 31811,

			// === Pearls ===
			31782, 31779, 31797, 31946, 31788, 31794, 31785, 31800,
			31791, 31776, 31773, 31770,

			// === Cargo & contracts ===
			32364, 32435, 32532, 32673, 32612, 32578, 32632, 32448,
			32500, 32483, 32449, 32548, 32766, 32464, 32465, 32589,
			32579, 32501, 32614, 32615, 32466, 32749, 32467, 32635,
			32616, 32694, 32711, 32580, 32590, 32486, 32468, 32502,
			32436, 32437, 32438, 32736, 32795, 32439, 32591, 32534,
			32484, 32451, 32753, 32565, 32504, 32440, 32470, 32640,
			32488, 32480, 32453, 32537, 32490, 32620, 32454, 32621,
			32593, 32506, 32594, 32622, 32471, 32553, 32491, 32441,
			32493, 32683, 32571, 32525, 32623, 32442, 32785, 32595,
			32539, 32761, 32473, 32541, 32572, 32701, 32443, 32444,
			32685, 32555, 32508, 32509, 32526, 32445, 32543, 32742,
			32494, 32544, 32446, 32456, 32495, 32586, 32730, 32457,
			32458, 32744, 32562, 32599, 32474, 32459, 32460, 32461,
			32475, 32529, 32775, 32481, 32574, 32496, 32705, 32514,
			32807, 32462, 32476, 32706, 32447, 32463, 32516, 32777,
			32691, 32545, 32630, 32531, 32671, 32517, 32518, 32499,
			32478, 32546, 32779, 32479, 32806, 32563, 32559, 32380,
			32366, 32368, 32371, 32377, 32383, 32374,

			// === Sailing outfit & rewards ===
			32871, 31733, 31248, 32431, 31745, 31760, 31762, 31766,
			31764, 31805, 31300, 31403, 31298, 31401, 7535, 32307,
			7534, 31599, 31596, 31593, 31590, 31757, 8788, 31288,
			31292, 32399, 32085, 31283, 31252, 31738, 31736, 31742,
			31740, 31748, 31750, 31754, 31752, 31254, 31398, 32433,
			// Separate-ID variants (degradation / charges / trims) + bird
			// nests, ported from the PR #2/#4 era data (pre-regen)
			31290, 32398
		));
	}

	private static void addCosmetics(Map<String, List<Integer>> m)
	{
		// COSMETICS — 1971 items
		//   Treasure trail sets (150), Minigame sets (21), Holiday items (192),
		//   Random event sets (30), Ornament kits (58), Skill capes & max capes
		//   (70), Leagues & speedrun rewards (206), Recoloured outfits (75), Quest
		//   & regional outfits (120), Miscellaneous cosmetics (1049)
		m.put(TAG_COSMETICS, Arrays.asList(
			// === Treasure trail sets ===
			23339, 23336, 23270, 13020, 13022, 13016, 13018, 20235,
			13171, 13060, 13062, 13169, 13052, 13054, 13167, 13056,
			13058, 11282, 2635, 7327, 2643, 12524, 10402, 10400,
			12996, 12998, 2647, 20246, 12445, 12447, 12992, 12994,
			12453, 12455, 12449, 12451, 2633, 7325, 12520, 10428,
			10410, 10408, 10430, 12301, 7386, 7388, 7394, 7396,
			7390, 7392, 12363, 12968, 12970, 12964, 12966, 2649,
			20059, 22719, 11280, 23413, 2641, 12540, 23288, 13036,
			13038, 23124, 8967, 23282, 12343, 12349, 12347, 12345,
			12303, 7323, 12518, 10432, 10414, 10412, 10434, 12307,
			13048, 13050, 13165, 2631, 12365, 12980, 12982, 12976,
			12978, 12371, 12359, 12369, 13008, 13010, 13004, 13006,
			20202, 23306, 20199, 23303, 12351, 12443, 12441, 12325,
			7321, 12309, 12339, 12317, 12315, 12341, 12305, 12516,
			12311, 10436, 10418, 10416, 10438, 12247, 7319, 12323,
			12522, 10424, 10406, 10404, 10426, 2645, 23185, 23273,
			13032, 13034, 13028, 13030, 13040, 13042, 13163, 12367,
			20382, 20385, 20376, 20379, 2639, 2637, 12313, 12321,
			10420, 10422, 12299, 13044, 13046, 13161,

			// === Minigame sets ===
			8956, 8995, 8963, 8952, 8991, 8959, 8955, 8994,
			8962, 8953, 8992, 8960, 8958, 8997, 8965, 8957,
			8996, 8964, 8954, 8993, 8961,

			// === Holiday items ===
			13288, 12896, 12895, 12893, 12892, 12894, 12897, 26627,
			26645, 30162, 30242, 26332, 21218, 11847, 11862, 13343,
			24431, 1055, 6865, 6868, 6875, 6876, 6877, 6878,
			1042, 2422, 26641, 26639, 27555, 26615, 26617, 1037,
			13182, 13664, 13665, 13663, 23448, 26647, 26643, 26637,
			26625, 962, 32934, 27566, 21227, 30167, 30287, 21219,
			4565, 13185, 1961, 7928, 11027, 26926, 21214, 26937,
			7927, 26270, 24432, 24434, 26312, 26310, 24436, 27588,
			24433, 26292, 26272, 26294, 27578, 27576, 27580, 27572,
			27574, 24435, 30489, 26274, 21226, 21224, 21217, 21216,
			23446, 24443, 24437, 27560, 24428, 1053, 6866, 6869,
			6879, 6880, 6881, 6882, 1044, 31231, 31233, 12845,
			31229, 12836, 13175, 23360, 26336, 26338, 26928, 24438,
			26631, 13344, 9920, 26334, 26326, 24977, 6864, 21221,
			26927, 26330, 30165, 30269, 12399, 13173, 27554, 26629,
			26635, 26619, 26621, 30168, 30296, 1959, 24325, 26247,
			1046, 11863, 30709, 24430, 1057, 6867, 6870, 6871,
			6872, 6873, 6874, 1038, 30166, 30278, 21223, 4566,
			22666, 21222, 12891, 12890, 1050, 12888, 12887, 12889,
			21866, 21868, 21867, 27564, 21871, 26298, 26316, 27557,
			27828, 26328, 21225, 10501, 10509, 27559, 26314, 21220,
			24313, 31225, 27491, 24311, 24305, 31222, 31227, 24307,
			31224, 24309, 31223, 12861, 26623, 26633, 27556, 1048,
			30163, 30251, 10508, 21859, 1040, 30164, 30260, 4079,

			// === Random event sets ===
			6655, 6658, 6656, 6659, 6654, 6657, 6188, 6183,
			13286, 13287, 13285, 13283, 13284, 6182, 6181, 6180,
			3061, 3060, 3059, 3057, 3058, 6186, 6185, 6187,
			6184, 7596, 7595, 7594, 7592, 7593,

			// === Ornament kits ===
			28336, 22246, 26713, 20068, 26717, 20071, 23237, 28017,
			27121, 12528, 33305, 33308, 33311, 22231, 12534, 26707,
			20143, 12538, 22239, 12536, 12800, 22236, 20002, 12532,
			26709, 30451, 30432, 30443, 27113, 27098, 12526, 26711,
			25742, 21202, 12530, 27255, 20065, 23227, 23321, 23324,
			23327, 25744, 20074, 26528, 26541, 26421, 26479, 12798,
			23348, 20062, 25099, 28690, 28684, 25090, 24670, 23232,
			12802, 20077,

			// === Skill capes & max capes ===
			13338, 19476, 13070, 9771, 9773, 20764, 21900, 9747,
			9749, 9789, 9791, 9801, 9803, 9780, 9782, 9753,
			9755, 28904, 9810, 9812, 13330, 9804, 9806, 9798,
			9800, 9783, 9785, 13336, 9774, 9776, 9768, 9770,
			9948, 9950, 21786, 21778, 21782, 21282, 9762, 9764,
			27366, 13280, 13281, 9792, 9794, 13221, 13223, 24857,
			9759, 9761, 9813, 9814, 9756, 9758, 9765, 9767,
			31288, 31292, 13332, 9786, 9788, 9795, 9797, 9750,
			9752, 9777, 9779, 9807, 9809, 13334,

			// === Leagues & speedrun rewards ===
			27394, 27402, 27410, 27442, 27392, 27400, 27408, 27388,
			27396, 27404, 27390, 27398, 27406, 27412, 33018, 33012,
			29628, 29619, 29622, 29625, 26494, 27418, 26524, 26520,
			26526, 26522, 24191, 24189, 24190, 33269, 33281, 33293,
			33260, 33272, 33284, 33349, 33299, 33357, 33368, 33345,
			33355, 33351, 33451, 33454, 33457, 33347, 33353, 33362,
			33266, 33278, 33290, 33263, 33275, 33287, 27783, 30453,
			27422, 31181, 31184, 31190, 31187, 31208, 26539, 26537,
			27424, 30469, 30430, 30410, 30418, 30426, 30477, 30428,
			30557, 30455, 30465, 30404, 30412, 30420, 30475, 30471,
			30459, 30457, 30461, 30331, 30334, 30337, 30408, 30416,
			30424, 30554, 30467, 30560, 30563, 30473, 30406, 30414,
			30422, 26424, 26436, 26448, 26460, 26517, 26427, 26439,
			26451, 26554, 26557, 26560, 26511, 26503, 26515, 26505,
			26509, 26513, 26507, 26500, 26430, 26442, 26454, 26433,
			26445, 26457, 27420, 25046, 25056, 25037, 25025, 25010,
			25054, 25013, 25042, 25093, 25028, 25016, 25001, 25052,
			25048, 25380, 25383, 25386, 28755, 28693, 28702, 28721,
			28733, 28745, 28763, 28699, 28751, 28712, 28724, 28736,
			28705, 28761, 28757, 28708, 28777, 28780, 28783, 28753,
			28759, 28715, 28727, 28739, 28748, 28718, 28730, 28742,
			28696, 25096, 25044, 25050, 25087, 25031, 25019, 25004,
			25034, 25022, 25007, 24376, 24413, 24463, 24411, 24403,
			24393, 24384, 24395, 24407, 24399, 24389, 24372, 24405,
			24397, 24387, 24466, 24382, 24378, 24469, 24472, 24475,
			24374, 24380, 24460, 24409, 24401, 24391,

			// === Recoloured outfits ===
			23911, 23913, 23915, 23917, 23919, 23921, 23923, 23925,
			13589, 13601, 13613, 13625, 13637, 13677, 21076, 24758,
			25084, 27459, 30060, 13581, 13593, 13605, 13617, 13629,
			13669, 21064, 24746, 25072, 27447, 30048, 30044, 13587,
			13599, 13611, 13623, 13635, 13675, 21073, 24755, 25081,
			27456, 30057, 13579, 13591, 13603, 13615, 13627, 13667,
			21061, 24743, 25069, 27444, 30045, 13585, 13597, 13609,
			13621, 13633, 13673, 21070, 24752, 25078, 27453, 30054,
			13583, 13595, 13607, 13619, 13631, 13671, 21067, 24749,
			25075, 27450, 30051,

			// === Quest & regional outfits ===
			2978, 2979, 2980, 2981, 2982, 2983, 2984, 2985,
			2986, 2987, 2988, 2989, 2990, 2991, 2992, 2993,
			2994, 2995, 6390, 6386, 6384, 6388, 3771, 3789,
			3763, 3775, 3791, 3761, 3767, 3759, 3799, 3765,
			3779, 3769, 3797, 3787, 3785, 3777, 3773, 3793,
			3795, 3783, 3781, 4310, 4304, 4308, 4302, 4306,
			4300, 4298, 6392, 6398, 6396, 6394, 6400, 6406,
			6404, 6402, 7112, 7124, 7130, 7136, 8949, 7114,
			7116, 7126, 7132, 7138, 2651, 7110, 7122, 7128,
			7134, 6341, 6351, 6361, 6371, 6347, 6359, 6369,
			6379, 6345, 6355, 6365, 6375, 6343, 6353, 6363,
			6373, 6349, 6357, 6367, 6377, 24794, 24810, 24826,
			24796, 24812, 24828, 24802, 24818, 24834, 24808, 24824,
			24840, 24806, 24822, 24838, 24678, 24800, 24816, 24832,
			24680, 24804, 24820, 24836, 24676, 24798, 24814, 24830,

			// === Miscellaneous cosmetics ===
			24539, 27820, 27812, 25328, 25326, 25334, 25330, 25322,
			25332, 25324, 33086, 33080, 33084, 33082, 21211, 26308,
			10392, 26809, 26807, 13262, 26901, 26811, 26252, 30607,
			12430, 29986, 29978, 29982, 20056, 13218, 29774, 26229,
			26227, 26223, 26225, 26221, 27381, 20101, 20095, 20107,
			20098, 20104, 13345, 20113, 19943, 33149, 2460, 13324,
			12646, 25502, 25840, 8926, 8927, 8925, 8924, 8650,
			8652, 8654, 8656, 8658, 8660, 8662, 8664, 8666,
			8668, 8670, 8672, 8674, 8676, 8678, 8680, 20773,
			20777, 20775, 28250, 6853, 11705, 12245, 23291, 27039,
			13322, 33124, 21245, 25137, 25135, 25129, 25133, 25131,
			32930, 6846, 26919, 26920, 26921, 12355, 29931, 21209,
			23108, 24331, 21230, 24986, 1019, 20026, 9918, 2476,
			10880, 1015, 30603, 2524, 20266, 19730, 630, 24981,
			1021, 12757, 9915, 25846, 7331, 2464, 660, 740,
			21312, 650, 640, 1011, 4558, 26929, 27806, 10322,
			10318, 10320, 10324, 10316, 6856, 6857, 9945, 9944,
			29433, 20140, 24338, 24340, 24342, 24344, 24346, 24348,
			20110, 6828, 11912, 30622, 1009, 23105, 12335, 29519,
			24544, 24547, 8968, 30597, 24980, 2520, 27485, 33099,
			19991, 21243, 10865, 10863, 10864, 21874, 28248, 29912,
			29914, 29915, 13679, 13680, 24549, 27804, 30042, 13178,
			7003, 9946, 26613, 24546, 30722, 30726, 30720, 30724,
			30708, 24537, 21716, 4514, 4516, 4513, 4515, 24525,
			12361, 1575, 22680, 22681, 22682, 21439, 22361, 27643,
			11019, 11021, 11022, 11020, 26304, 11025, 11026, 11910,
			13071, 22687, 26935, 9644, 9640, 9642, 25712, 30648,
			30646, 26290, 26286, 26288, 27438, 10595, 22692, 22695,
			22689, 22701, 22698, 19695, 19697, 19691, 20249, 29781,
			28601, 30579, 30581, 30583, 30585, 30587, 30589, 30591,
			30593, 30595, 24543, 26925, 22678, 12958, 11919, 12959,
			33093, 33096, 33097, 12956, 12957, 27794, 26939, 632,
			662, 652, 642, 20243, 20240, 12319, 13681, 21239,
			22677, 31760, 31762, 31766, 31764, 24000, 23941, 23933,
			23935, 23939, 23929, 23927, 23937, 23931, 25500, 11708,
			11707, 11709, 27248, 8966, 19915, 24733, 19970, 24729,
			25557, 19964, 19961, 19958, 19967, 22686, 21710, 4559,
			23294, 33296, 33302, 13133, 26598, 6834, 13188, 21723,
			21722, 29730, 29736, 29732, 29722, 29724, 29728, 29718,
			29726, 29734, 29720, 29716, 29740, 29764, 29760, 29744,
			29756, 29754, 29750, 29772, 29768, 29742, 29748, 29762,
			29766, 29738, 29758, 29746, 29752, 29770, 29714, 29712,
			29704, 29688, 29710, 29686, 29706, 29698, 29700, 29692,
			29708, 29694, 29696, 30491, 30487, 31130, 19727, 27810,
			30611, 23667, 12600, 27801, 21231, 27873, 22684, 30970,
			33147, 21252, 29443, 29441, 29437, 29439, 27563, 22351,
			22353, 24003, 24006, 24024, 24012, 24018, 24009, 24015,
			24021, 24027, 29868, 29872, 29870, 29874, 13183, 21873,
			21861, 21862, 20433, 20439, 20442, 20436, 9919, 12514,
			25102, 28672, 20008, 11990, 6382, 3006, 22838, 6670,
			21238, 10394, 27035, 21236, 21248, 28175, 28173, 28171,
			28169, 28670, 28626, 21229, 26936, 12769, 21228, 28138,
			5345, 23859, 9906, 6106, 6110, 6109, 23252, 25314,
			20836, 20659, 27414, 30613, 20134, 27434, 27799, 27802,
			13655, 25336, 27818, 9472, 9470, 27800, 25316, 25290,
			12251, 12727, 25289, 21247, 10881, 20208, 20205, 28663,
			25555, 25549, 25551, 25553, 22840, 27038, 12849, 20023,
			13328, 628, 24985, 1027, 12759, 9916, 25284, 7330,
			658, 21311, 648, 638, 10878, 4563, 31034, 25604,
			25608, 2894, 2902, 2900, 2898, 2896, 2526, 12837,
			26168, 27046, 26820, 10506, 31285, 1989, 20053, 30234,
			27497, 21354, 10862, 26182, 8928, 26260, 24975, 24527,
			7583, 13247, 21509, 13320, 22679, 20220, 22355, 33002,
			12598, 19997, 20229, 27428, 19699, 24885, 20116, 19946,
			12841, 30152, 12839, 12843, 12855, 20779, 27267, 27257,
			27259, 27261, 27263, 27265, 28786, 22746, 12249, 33359,
			33365, 24993, 13184, 27440, 21208, 30493, 30599, 32932,
			27814, 27808, 29932, 21291, 26250, 23297, 6858, 6859,
			21720, 20032, 12647, 24862, 6717, 21875, 24866, 20164,
			6550, 19724, 27037, 20020, 27561, 19985, 19979, 19976,
			19973, 19982, 25348, 22473, 28252, 13216, 24491, 21869,
			26912, 20119, 19949, 28128, 32928, 8969, 33642, 11023,
			24535, 13198, 13201, 24864, 13203, 23522, 27370, 27372,
			31331, 21233, 22386, 30605, 2472, 23285, 19556, 12353,
			25286, 30154, 6665, 27562, 20083, 20092, 20086, 20080,
			20089, 27590, 27645, 27875, 26348, 29836, 21748, 21715,
			26276, 26596, 26963, 20029, 27494, 27822, 20851, 31117,
			24987, 1031, 25844, 2470, 21309, 23093, 23099, 23091,
			26602, 7414, 24541, 10396, 25838, 25609, 20226, 22358,
			12428, 1561, 1567, 11995, 12644, 12645, 12643, 12816,
			12650, 12652, 1555, 12655, 12649, 12703, 3695, 12648,
			12921, 12651, 28655, 8971, 28669, 28618, 28616, 28620,
			28622, 28624, 20693, 30160, 626, 24988, 6959, 656,
			646, 636, 1013, 26284, 26280, 26282, 4564, 8950,
			12412, 20122, 19952, 10877, 28126, 30947, 13346, 13656,
			30479, 12653, 24867, 27795, 21870, 27796, 22316, 25606,
			6852, 30793, 30791, 30783, 30787, 30785, 30789, 2934,
			24983, 1029, 10326, 2468, 2942, 2940, 21313, 2938,
			2936, 27041, 28962, 11024, 29489, 29507, 28116, 21314,
			30717, 29929, 11848, 31879, 2904, 24984, 1007, 9914,
			25283, 7329, 2462, 2912, 2910, 21308, 2908, 2906,
			10879, 4562, 10507, 27377, 27378, 27379, 27380, 21235,
			24989, 24990, 20665, 1025, 20017, 20005, 6583, 27432,
			27430, 21250, 13321, 20663, 12856, 25287, 12397, 12395,
			12393, 12439, 10882, 30609, 21695, 27570, 20834, 12337,
			21234, 23318, 23312, 23315, 25746, 26254, 30232, 13181,
			33146, 22675, 28801, 12842, 21246, 26918, 30161, 12840,
			32926, 24792, 20125, 19955, 5030, 5032, 5034, 5042,
			5044, 5046, 23300, 22494, 22496, 22498, 22500, 22502,
			9921, 9922, 24327, 9923, 9925, 24865, 9924, 5048,
			5050, 5052, 26649, 21273, 25282, 10398, 11916, 22350,
			27488, 24298, 21237, 28960, 23760, 29933, 20832, 27568,
			21849, 21857, 21855, 21847, 21851, 21853, 13217, 21864,
			28788, 27436, 31283, 27797, 26916, 26917, 27416, 21232,
			28603, 24992, 4613, 24323, 24321, 24315, 24317, 24319,
			23495, 20590, 25288, 6822, 25547, 22713, 30601, 25285,
			31738, 31736, 31742, 31740, 27816, 31748, 31750, 31754,
			31752, 31193, 31196, 31202, 31199, 20661, 26931, 13196,
			13200, 21714, 2924, 2932, 2930, 2928, 2926, 20217,
			20214, 20211, 4315, 4333, 4335, 4337, 4339, 4341,
			4343, 4345, 4347, 4349, 4351, 4317, 4353, 4355,
			4357, 4359, 4361, 4363, 4365, 4367, 4369, 4371,
			4319, 4373, 4375, 4377, 4379, 4381, 4383, 4385,
			4387, 4389, 4391, 4321, 4393, 4395, 4397, 4399,
			4401, 4403, 4405, 4407, 4409, 4411, 4323, 4413,
			4325, 4327, 4329, 4331, 27040, 27483, 25610, 23224,
			13215, 21717, 26600, 25602, 21713, 12432, 12434, 7771,
			21718, 26605, 26609, 26607, 8970, 27463, 6840, 22717,
			22715, 6860, 6861, 6335, 6337, 6339, 33323, 5036,
			5038, 5040, 27352, 634, 664, 654, 644, 13225,
			26256, 33434, 10487, 20223, 24542, 23255, 27798, 26933,
			26932, 21872, 13177, 13179, 13352, 13351, 24207, 24209,
			24213, 24520, 24211, 24215, 13186, 12771, 21907, 21992,
			9636, 9638, 9634, 20232, 795, 26930, 28671, 21240,
			21251, 21249, 21242, 21241, 21244, 1005, 24297, 24982,
			12763, 9913, 10327, 2474, 4560, 2522, 20269, 21428,
			21434, 21433, 12838, 6556, 21863, 28246, 27479, 27481,
			27473, 27477, 27475, 23410, 23407, 29930, 26934, 6862,
			6863, 5024, 5026, 5028, 22396, 22394, 22388, 22392,
			22390, 30888, 2914, 1023, 12761, 9917, 2466, 2922,
			2920, 21310, 2918, 2916, 7803, 23757, 23908, 26611,
			24863,
			// Separate-ID variants (degradation / charges / trims) + bird
			// nests, ported from the PR #2/#4 era data (pre-regen)
			9748, 9751, 9754, 9757, 9760, 9763, 9766, 9769,
			9772, 9775, 9778, 9781, 9784, 9787, 9790, 9793,
			9796, 9799, 9802, 9805, 9808, 9811, 9949, 13068,
			13222, 13317, 13318, 13319, 27699, 27703, 27707, 27711,
			27715, 27719, 27723, 27727, 27731, 27735, 27739, 27743,
			27747, 27751, 27755, 27759, 27763, 27767, 27771, 27775,
			27779, 28687, 30519, 30521, 30523, 30525, 30527, 30529,
			30531, 30533, 30535, 30537, 30539, 30541, 30543, 30545,
			30547, 30570, 30571, 30572, 30573, 30574, 31290, 33025,
			33029, 33033
		));
	}
}
