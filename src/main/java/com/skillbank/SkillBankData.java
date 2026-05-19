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
	public static final String TAG_WC_FLETCHING = "wc_fletching";
	public static final String TAG_FISHING = "fishing";
	public static final String TAG_FIREMAKING = "firemaking";
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

	private static final Map<String, List<Integer>> TAGS;
	private static final Map<String, Integer> TAG_ICONS;

	static
	{
		Map<String, Integer> i = new LinkedHashMap<>();
		i.put(TAG_MELEE, 1333);              // rune scimitar
		i.put(TAG_RANGE, 861);               // magic shortbow
		i.put(TAG_MAGE, 1387);               // staff of fire
		i.put(TAG_PRAYER, 1718);             // holy symbol
		i.put(TAG_COOKING, 1949);            // chef's hat
		i.put(TAG_WC_FLETCHING, 1513);       // magic logs
		i.put(TAG_FISHING, 307);             // fishing rod
		i.put(TAG_FIREMAKING, 590);          // tinderbox
		i.put(TAG_CRAFTING, 1733);           // needle
		i.put(TAG_MINING_SMITHING, 1275);    // rune pickaxe
		i.put(TAG_HERBLORE, 2428);           // attack potion (4)
		i.put(TAG_AGILITY_THIEVING, 11850);  // graceful hood
		i.put(TAG_SLAYER, 4155);             // enchanted gem
		i.put(TAG_FARMING, 5341);            // rake
		i.put(TAG_RUNECRAFT, 7936);          // pure essence
		i.put(TAG_HUNTER, 9977);             // grey chinchompa
		i.put(TAG_CONSTRUCTION, 2347);       // hammer
		i.put(TAG_MISC, 2572);               // ring of wealth
		i.put(TAG_QUESTS, 9813);             // quest point cape
		i.put(TAG_SAILING, 31288);           // sailing cape
		i.put(TAG_COSMETICS, 1037);          // bunny ears
		TAG_ICONS = Collections.unmodifiableMap(i);
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
		addWcFletching(m);

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

		// ---------------------------------------------------------------------
		// FIREMAKING — Firemaking skill.
		// Cross-tags: all logs (dup WC), tinderbox (dup cooking).
		// ---------------------------------------------------------------------
		// FIREMAKING — 82 items
		//   Tinderbox (2), Logs (30), Firelighters (6), Lanterns (7), Wintertodt
		//   (9), Cape (3), Legacy (25)
		// FIREMAKING — 96 items
		//   Tinderbox (2), Logs (42), Firelighters (6), Lanterns (7), Wintertodt
		//   (9), Cape (2), Legacy (28)
		addFiremaking(m);

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
		// MELEE — 1866 items
		//   LLM classified (1866)
		m.put(TAG_MELEE, Arrays.asList(
			// === LLM classified ===
			20011, 28226, 10350, 10352, 12426, 20014, 10348, 10346,
			23242, 11737, 11736, 11735, 11734, 13263, 13265, 27861,
			28080, 33118, 12006, 26484, 4151, 20405, 26482, 1317,
			1357, 1371, 4129, 12377, 1111, 3100, 1211, 8849,
			28211, 1161, 2613, 2605, 7459, 20582, 13020, 13022,
			3200, 11375, 10296, 10298, 10300, 10302, 10304, 22159,
			22161, 22163, 22165, 22167, 22169, 22171, 22173, 22175,
			22177, 22179, 22181, 22183, 22185, 22187, 22189, 1199,
			22127, 22129, 22131, 22133, 22135, 22137, 22139, 22141,
			22143, 22145, 22147, 22149, 22151, 22153, 22155, 22157,
			2611, 2603, 1301, 1430, 1145, 1271, 1123, 2607,
			23392, 23395, 23398, 23401, 23404, 2599, 1073, 2609,
			2601, 1091, 3475, 3474, 1331, 13012, 13014, 7334,
			7340, 7346, 7352, 7358, 1245, 1183, 1287, 13016,
			13018, 1345, 27349, 27347, 1478, 22557, 24780, 1729,
			23309, 19707, 6585, 23640, 12436, 1704, 20586, 10362,
			10354, 1706, 1708, 1710, 1712, 11976, 11978, 1731,
			20585, 23354, 29801, 29804, 1725, 12851, 30376, 19553,
			27173, 20366, 2297, 28336, 12466, 26233, 27184, 24201,
			26370, 12468, 11061, 7804, 12460, 12462, 12464, 13060,
			13062, 19677, 21633, 7807, 7808, 7806, 13441, 1540,
			11710, 12917, 12915, 12913, 11507, 2458, 2456, 2454,
			2452, 31081, 2323, 29806, 29799, 29816, 29818, 20251,
			19675, 23785, 23789, 30955, 12476, 11802, 20593, 29605,
			20368, 20068, 11810, 12478, 12470, 12472, 12474, 13052,
			13054, 5739, 9747, 9749, 11431, 11429, 125, 123,
			121, 2428, 22322, 22477, 31088, 31093, 31097, 33172,
			31092, 31096, 31091, 31095, 31094, 4627, 26717, 11836,
			26720, 11832, 26718, 12486, 11804, 20370, 20071, 24195,
			11812, 12488, 12613, 12614, 12616, 12480, 12482, 12484,
			13056, 13058, 11834, 23646, 26719, 26394, 10129, 10887,
			27855, 28074, 25639, 25641, 7462, 23593, 27112, 24268,
			31172, 3803, 28293, 28316, 28279, 31248, 3751, 27169,
			28295, 11128, 23240, 23237, 6737, 23595, 11773, 1313,
			1361, 1367, 4125, 12375, 1107, 3098, 1217, 8847,
			28205, 1165, 2595, 2587, 7457, 12996, 12998, 3196,
			10306, 10308, 10310, 10312, 10314, 1195, 2597, 2589,
			1297, 1426, 8921, 8919, 11783, 8917, 11782, 8915,
			11781, 8911, 11779, 8909, 11778, 8907, 11777, 8905,
			11776, 11784, 1151, 12297, 1125, 2591, 23366, 23369,
			23372, 23375, 23378, 2583, 1077, 2593, 2585, 1089,
			3473, 3472, 10148, 1327, 12988, 12990, 7332, 7338,
			7344, 7350, 7356, 19639, 19641, 4580, 10085, 1179,
			1283, 21730, 12992, 12994, 1341, 10014, 29207, 29189,
			29944, 23995, 24551, 25870, 25872, 25874, 25876, 25878,
			25880, 25882, 10491, 12831, 23642, 24592, 24589, 29640,
			29637, 29634, 29631, 24699, 24697, 31136, 29022, 29846,
			29028, 29848, 29025, 29847, 24774, 24777, 3329, 22430,
			13276, 13275, 13274, 31139, 29019, 29845, 28988, 29849,
			29016, 2064, 667, 5018, 8872, 28792, 5016, 22986,
			3844, 12608, 26494, 23037, 31886, 22999, 7671, 21817,
			2021, 27283, 2309, 22975, 11037, 3335, 22963, 28813,
			30324, 1307, 1351, 1375, 4119, 1103, 3095, 1205,
			8844, 1155, 12211, 12221, 7454, 12968, 12970, 3190,
			11367, 1189, 12213, 12223, 1291, 1422, 1139, 1117,
			12205, 12215, 1075, 12207, 12217, 1087, 12209, 12219,
			1321, 12960, 12962, 1237, 1173, 1277, 12964, 12966,
			1337, 6235, 6257, 6279, 6233, 6255, 6277, 6231,
			6253, 6275, 6229, 6251, 6273, 6225, 6247, 6269,
			6223, 6245, 6267, 6221, 6243, 6265, 6219, 6241,
			6263, 3333, 22368, 29574, 29577, 33200, 1967, 20272,
			1891, 26755, 26749, 25162, 25161, 25160, 25159, 5003,
			26721, 29963, 7054, 2209, 2185, 1897, 27667, 7451,
			3105, 20578, 23413, 27021, 11126, 11124, 11122, 11120,
			11118, 11974, 11972, 11447, 11445, 13064, 9745, 26153,
			9743, 26152, 9741, 26151, 9739, 26150, 30816, 30810,
			30825, 2140, 4291, 29134, 3144, 29146, 2142, 4293,
			29143, 24785, 2343, 29137, 3381, 29140, 5988, 33109,
			28537, 23821, 23844, 23843, 23845, 28534, 23850, 23849,
			23851, 23823, 23841, 23840, 23842, 23847, 23846, 23848,
			23820, 28543, 28545, 31174, 23831, 27844, 27845, 27846,
			27842, 27843, 28531, 7537, 7539, 8929, 23673, 30384,
			23890, 23889, 23891, 30340, 28220, 23987, 23896, 23895,
			23897, 23762, 23864, 23887, 23886, 23888, 23893, 23892,
			23894, 25960, 23861, 23991, 23868, 4207, 1978, 2011,
			27248, 12607, 11936, 6746, 4069, 4070, 4504, 4505,
			4509, 4510, 11893, 11894, 11895, 25163, 25167, 25171,
			25165, 25169, 25174, 4071, 4506, 4511, 4072, 4507,
			4512, 4068, 4503, 4508, 9753, 9755, 11459, 11457,
			137, 135, 133, 2432, 8856, 33305, 33338, 12877,
			4718, 25516, 4716, 23639, 4720, 25515, 4722, 23633,
			28682, 21015, 23706, 23703, 23700, 23697, 23694, 23691,
			23688, 23685, 23730, 23727, 23724, 23721, 23718, 23715,
			23712, 23709, 11286, 7158, 28051, 21882, 21885, 6739,
			25378, 1377, 28037, 1911, 5745, 11840, 28055, 22234,
			22231, 12373, 3140, 28065, 12414, 12534, 13652, 20784,
			28039, 26708, 26707, 1215, 20407, 28019, 12954, 23597,
			19722, 20143, 28217, 11335, 12417, 12538, 7461, 3204,
			28049, 21028, 25373, 22731, 22978, 21895, 22244, 12536,
			1305, 27859, 28033, 28078, 1434, 27857, 28027, 28076,
			1149, 28057, 22097, 11920, 12797, 21892, 22242, 22236,
			4087, 28061, 12415, 4585, 28063, 12416, 4587, 20406,
			28031, 20000, 20002, 1249, 28041, 1187, 28059, 12418,
			12532, 21009, 28029, 13576, 28035, 26710, 26709, 22111,
			11283, 11284, 33186, 23667, 24043, 24034, 24046, 24037,
			24040, 22957, 2092, 28997, 29850, 23206, 11200, 7509,
			30957, 28945, 28942, 29000, 31142, 29004, 10971, 21140,
			7056, 7435, 20921, 20922, 20923, 20924, 20913, 20914,
			20915, 20916, 21003, 21205, 27100, 27098, 20917, 20918,
			20919, 20920, 29560, 29562, 29564, 26759, 26753, 13073,
			13072, 26469, 23943, 12819, 12817, 29589, 22433, 30378,
			22435, 24695, 25859, 35, 28319, 29830, 11962, 11960,
			11957, 11955, 11953, 11951, 22218, 22215, 22212, 22209,
			27285, 28321, 22744, 13117, 13118, 13119, 13120, 9005,
			33249, 22981, 10548, 10551, 28067, 9006, 1393, 6570,
			13329, 13330, 3757, 3791, 3799, 3748, 23246, 27177,
			13129, 13130, 13131, 13132, 3758, 3795, 12774, 12769,
			7441, 7668, 27550, 27552, 25934, 25936, 27544, 22324,
			23628, 27871, 20155, 13036, 13038, 23279, 12391, 20149,
			3486, 20161, 3488, 20146, 3481, 3483, 3485, 12389,
			20158, 20152, 20875, 29889, 30380, 12727, 11798, 11818,
			11820, 11822, 11794, 11796, 11800, 4567, 10564, 21643,
			12849, 21736, 21742, 10589, 6809, 21646, 4153, 20557,
			24225, 21739, 21752, 3122, 19645, 1915, 26156, 27042,
			26158, 27048, 26166, 27044, 21733, 12873, 4730, 4724,
			23638, 4728, 4726, 13048, 13050, 2673, 12639, 2675,
			6760, 2669, 2671, 3480, 4423, 4421, 4419, 4417,
			32336, 23360, 26170, 26172, 26180, 20792, 20794, 20796,
			7453, 7141, 10828, 23591, 28070, 347, 20756, 9768,
			9770, 25734, 25736, 25738, 27351, 33066, 33068, 20254,
			22983, 23073, 23075, 22966, 22973, 22969, 29966, 25066,
			30347, 25371, 30348, 21295, 23622, 30342, 25367, 30343,
			21285, 21282, 30346, 33243, 5576, 9668, 5575, 5574,
			24488, 24419, 27195, 24420, 27196, 24417, 27198, 24421,
			27197, 1309, 1349, 1363, 4121, 1101, 3096, 1203,
			8845, 1153, 12241, 12231, 7455, 12980, 12982, 3192,
			11369, 1191, 12243, 12233, 1293, 1420, 1137, 1267,
			11721, 1115, 12235, 12225, 1067, 12237, 12227, 1081,
			12239, 12229, 1323, 12972, 12974, 1239, 1175, 1279,
			12976, 12978, 1335, 12811, 12812, 22398, 30893, 27289,
			1993, 22438, 22327, 22326, 22328, 13139, 13140, 11136,
			11138, 11140, 13103, 12357, 3801, 10581, 25979, 30891,
			25981, 27287, 27291, 7447, 12004, 20879, 20868, 20727,
			4158, 11902, 3371, 1059, 28325, 25975, 27870, 27341,
			27339, 379, 22081, 20257, 7140, 355, 11711, 22263,
			13199, 13198, 391, 26745, 26747, 26743, 6416, 6420,
			6418, 22257, 32352, 13280, 2327, 2293, 7449, 27211,
			27208, 27205, 27202, 21649, 4156, 1315, 1355, 1369,
			4127, 1109, 3099, 1209, 8848, 28208, 1159, 12283,
			12293, 7458, 20581, 27110, 13008, 13010, 3198, 11373,
			1197, 12281, 12291, 1299, 1428, 1143, 11720, 1121,
			12277, 12287, 1071, 12279, 12289, 1085, 12285, 12295,
			1329, 13000, 13002, 1243, 1181, 1285, 13004, 13006,
			1343, 7946, 33101, 29418, 29083, 29082, 29081, 29080,
			13112, 13113, 13114, 13115, 20881, 7058, 20864, 3327,
			22114, 30390, 30392, 27321, 27319, 27317, 27315, 24271,
			10826, 13091, 13080, 4224, 4235, 11759, 29792, 29796,
			33178, 29790, 29794, 19918, 4599, 6410, 6408, 22251,
			30744, 30753, 30750, 30756, 30765, 21279, 6568, 21298,
			21301, 21304, 3331, 10146, 24229, 26219, 33174, 27246,
			20994, 20995, 20985, 20986, 20987, 20988, 11733, 20989,
			11732, 20990, 11731, 20991, 11730, 20992, 13279, 10553,
			26948, 26945, 20877, 11090, 2301, 20260, 2289, 3339,
			3343, 3337, 3341, 1235, 6703, 6705, 25206, 25204,
			25203, 2028, 2032, 2038, 2235, 2219, 2040, 2233,
			13239, 13231, 9676, 9670, 9666, 9674, 9672, 9678,
			20883, 30793, 30791, 30787, 30785, 21264, 21266, 10476,
			30779, 30777, 30781, 7917, 7142, 10147, 19647, 19649,
			10083, 2325, 22266, 11133, 131, 21820, 2570, 2550,
			28329, 19550, 19710, 9988, 31151, 6145, 6157, 6151,
			6128, 6130, 6129, 6159, 6161, 7648, 7639, 7647,
			7646, 7644, 7643, 7642, 7641, 7445, 20866, 10020,
			29204, 29186, 24693, 28896, 1319, 13024, 13026, 1359,
			1373, 4131, 12379, 1113, 3101, 1213, 8850, 27185,
			23230, 23227, 28214, 1163, 2619, 2627, 7460, 27111,
			13032, 13034, 3202, 11377, 10286, 10288, 10290, 10292,
			10294, 8464, 8466, 8468, 8470, 8472, 8474, 8476,
			8478, 8480, 8482, 8484, 8486, 8488, 8490, 8492,
			8494, 1201, 8714, 8716, 8718, 8720, 8722, 8724,
			8726, 8728, 8730, 8732, 8734, 8736, 8738, 8740,
			8742, 8744, 2621, 2629, 1303, 1432, 1147, 11719,
			1127, 2615, 23209, 23212, 23215, 23218, 23221, 2623,
			1079, 20422, 2617, 2625, 1093, 3476, 3477, 1333,
			23330, 23332, 23334, 23321, 23327, 7336, 7342, 7348,
			7354, 7360, 1247, 1185, 1289, 13028, 13030, 1347,
			10549, 6721, 329, 4081, 10588, 12018, 12017, 25744,
			25739, 25741, 28254, 28256, 28258, 29198, 29180, 23528,
			13040, 13042, 6691, 23581, 6689, 23579, 6687, 23577,
			6685, 23575, 2665, 11806, 20372, 20074, 12637, 27165,
			11814, 2667, 6762, 2661, 2663, 3479, 11838, 12809,
			12804, 325, 22325, 22486, 397, 24198, 12931, 12929,
			10858, 21838, 385, 20390, 20263, 13358, 13363, 13368,
			13373, 13357, 13362, 13367, 13372, 13360, 13365, 13370,
			13375, 13359, 13364, 13369, 13374, 13379, 13361, 13366,
			13371, 13376, 2366, 2368, 2080, 26132, 26009, 26012,
			26126, 26006, 26135, 29670, 26060, 29664, 26075, 25997,
			28508, 29673, 27323, 2961, 2963, 2402, 28323, 22006,
			7443, 6163, 11864, 11865, 27343, 29583, 29585, 10016,
			29201, 29183, 30759, 33335, 7439, 20397, 12821, 23389,
			27178, 4551, 12829, 23599, 7437, 24144, 22296, 11791,
			33063, 22331, 22625, 27833, 22628, 27834, 22631, 27835,
			22622, 23620, 27908, 1311, 1353, 1365, 4123, 1105,
			3097, 1207, 8846, 1157, 20178, 20193, 778, 7456,
			20382, 3194, 11371, 8682, 8684, 8686, 8688, 8690,
			8692, 8694, 8696, 8698, 8700, 8702, 8704, 8706,
			8708, 8710, 8712, 1193, 8746, 8748, 8750, 8752,
			8754, 8756, 8758, 8760, 8762, 8764, 8766, 8768,
			8770, 8772, 8774, 8776, 20181, 20196, 1295, 1424,
			1141, 1269, 1119, 20169, 20184, 1069, 20172, 20187,
			1083, 20175, 20190, 30895, 1325, 12984, 12986, 1241,
			1177, 1281, 1339, 2003, 464, 10364, 9750, 9752,
			11441, 11443, 119, 117, 115, 113, 29084, 29421,
			29409, 29424, 28939, 28936, 28933, 29210, 29192, 30369,
			30386, 21997, 21994, 21987, 21984, 21981, 21978, 149,
			147, 145, 2436, 12701, 23549, 12699, 23547, 12697,
			23545, 12695, 23543, 11499, 11497, 167, 165, 163,
			2442, 4608, 13066, 11487, 11485, 161, 159, 157,
			2440, 11471, 11469, 26757, 26751, 20858, 30884, 30881,
			30878, 30875, 10149, 24219, 373, 13197, 13196, 27333,
			27331, 27329, 27327, 28834, 30367, 33038, 30382, 30388,
			33041, 2217, 6524, 6526, 6523, 6525, 12879, 4747,
			4745, 23637, 4749, 4751, 23634, 29580, 31145, 26382,
			30302, 26384, 30303, 26386, 30304, 33194, 33036, 12902,
			33035, 12904, 28684, 13396, 9704, 9703, 12605, 12692,
			333, 26149, 7068, 7060, 21888, 21890, 24444, 12603,
			12691, 9629, 6527, 6528, 23235, 23232, 25910, 25912,
			25900, 1885, 12813, 12814, 12815, 28287, 28307, 33182,
			28285, 3842, 26547, 27660, 27657, 33247, 24266, 25904,
			25906, 29900, 13104, 13105, 13106, 13107, 12875, 4757,
			27190, 4755, 27189, 4753, 23636, 4759, 23635, 24617,
			22616, 27831, 22613, 23615, 27904, 22619, 27832, 22610,
			27900, 22446, 22542, 8842, 8841, 8840, 8839, 11665,
			26477, 27690, 27869, 29607, 27684, 27687, 27681, 12773,
			12771, 8851, 3753, 28301, 24859, 6735, 11772, 187,
			5937, 5940, 13141, 13142, 13143, 13144, 2017, 6609,
			6589, 6619, 6615, 6587, 6591, 6623, 6629, 6599,
			6633, 6607, 6601, 6621, 6617, 6625, 6627, 6611,
			6631, 6605, 6613, 13108, 13109, 13110, 13111, 4600,
			6414, 6412, 2054, 2952, 1171, 7433, 7675, 2191,
			26723, 26727, 20973, 20974, 20975, 20976, 20980, 10822,
			32328, 22260, 13044, 13046, 193, 191, 189, 2450,
			2657, 11808, 20374, 20077, 12638, 11816, 2659, 11523,
			11521, 6764, 2653, 2655, 3478, 11889, 11824, 28810,
			19912, 30321
		));
	}

	private static void addRange(Map<String, List<Integer>> m)
	{
		// RANGE — 836 items
		//   LLM classified (836)
		m.put(TAG_RANGE, Arrays.asList(
			// === LLM classified ===
			12424, 10334, 27201, 10332, 27200, 10330, 27199, 10336,
			13337, 13338, 890, 20525, 43, 9143, 4798, 9183,
			9463, 810, 2538, 829, 19578, 867, 804, 21347,
			21326, 21350, 21338, 21316, 25849, 21328, 21318, 21352,
			19707, 6585, 12436, 10362, 1708, 1710, 1731, 12851,
			30376, 12490, 12494, 12496, 12492, 19921, 23197, 13171,
			22246, 31081, 32879, 29806, 33202, 3749, 28298, 6733,
			11771, 1464, 26713, 12506, 31659, 31656, 31653, 31650,
			11830, 26716, 12510, 11828, 26715, 12512, 11785, 23611,
			12508, 19930, 23200, 13169, 11826, 26714, 27269, 21898,
			21900, 28991, 29852, 31004, 30998, 10499, 22109, 10498,
			31088, 31093, 31097, 33172, 31092, 31096, 31091, 31095,
			31094, 19592, 19601, 12498, 12502, 12504, 12500, 19924,
			23203, 13167, 47, 22230, 881, 7462, 22470, 22467,
			22464, 22461, 4788, 11959, 2503, 20423, 12381, 12385,
			2497, 20424, 25493, 12383, 12387, 22284, 2491, 25494,
			3093, 12871, 869, 11783, 11782, 11781, 11779, 11778,
			11777, 11776, 11784, 10148, 10085, 28688, 28955, 29640,
			29637, 29634, 29631, 2499, 7374, 7376, 2493, 7382,
			7384, 22278, 2487, 12867, 10081, 22229, 9139, 9176,
			9434, 9433, 4740, 13193, 8882, 28794, 12610, 26492,
			22951, 25865, 25867, 25884, 25886, 25888, 25890, 25892,
			25894, 25896, 27187, 33021, 22975, 11885, 11874, 4160,
			11875, 687, 882, 11700, 39, 877, 4773, 9174,
			9454, 806, 598, 825, 19570, 864, 800, 22227,
			26755, 26749, 31575, 10, 26524, 6, 26520, 26526,
			26522, 30694, 30696, 25162, 25161, 25160, 25159, 10033,
			6169, 1169, 4827, 23856, 23855, 23857, 23832, 29599,
			23840, 27847, 27848, 27849, 28540, 22547, 837, 23956,
			23975, 27697, 27709, 27721, 27733, 27745, 27757, 27769,
			33023, 33166, 23890, 25496, 23983, 23902, 23901, 23903,
			23971, 27705, 27717, 27729, 27741, 27753, 27765, 27777,
			33031, 33170, 23887, 25495, 23888, 23979, 27701, 27713,
			27725, 27737, 27749, 27761, 27773, 33027, 33168, 23893,
			23892, 25497, 23991, 4207, 23869, 11708, 12609, 11235,
			20408, 27853, 29611, 28072, 30626, 11899, 11900, 11901,
			21034, 9192, 9340, 9243, 23649, 21969, 21946, 31047,
			24644, 24641, 24638, 24635, 23742, 23739, 23736, 23733,
			28902, 28904, 28951, 28947, 8880, 27012, 11212, 20389,
			11237, 21905, 21930, 21902, 28053, 21921, 11230, 11232,
			11217, 21012, 25918, 25916, 19484, 23648, 19582, 22804,
			22812, 27157, 21918, 20849, 22002, 22003, 9193, 9341,
			9244, 21971, 21948, 30374, 12863, 30434, 30436, 30432,
			29000, 29851, 29004, 29840, 29010, 29842, 29007, 29841,
			26759, 26753, 13073, 26471, 13072, 26469, 23943, 9190,
			9338, 9241, 21965, 21942, 30378, 731, 25859, 27670,
			11881, 6167, 22228, 6082, 2865, 10954, 10958, 10956,
			27544, 23258, 23264, 23267, 23261, 23124, 30380, 21728,
			21726, 21752, 1135, 7370, 7372, 1099, 7378, 7380,
			22275, 1065, 1745, 12865, 10079, 31235, 10142, 10376,
			10380, 27180, 10382, 10378, 19927, 23188, 13165, 22269,
			1131, 10145, 31010, 19481, 23630, 26712, 26711, 19589,
			9770, 732, 31169, 30076, 30079, 30073, 30082, 29305,
			10156, 28869, 23075, 78, 19598, 19595, 28837, 884,
			11701, 40, 9140, 4778, 9177, 9457, 807, 2532,
			826, 863, 9423, 801, 31579, 9187, 9335, 9237,
			21957, 21934, 19584, 12883, 4732, 4734, 4738, 4736,
			23632, 10158, 10113, 10105, 33251, 26741, 26739, 26737,
			1129, 23381, 1061, 1095, 23384, 1167, 1063, 19478,
			27188, 19586, 25975, 25826, 10159, 10107, 839, 10284,
			859, 70, 861, 12788, 72, 12786, 851, 62,
			853, 64, 32352, 10143, 27355, 27374, 27363, 27366,
			27229, 27238, 33190, 27232, 27241, 33192, 27372, 27226,
			27235, 13280, 9418, 9419, 9416, 888, 11703, 42,
			9142, 4793, 9181, 9461, 809, 822, 2536, 828,
			19576, 866, 803, 31166, 29286, 29289, 29283, 29280,
			19610, 29171, 28878, 22638, 27836, 22636, 23619, 27916,
			22641, 27837, 22644, 27838, 22634, 27912, 7170, 33245,
			30390, 30392, 19547, 27172, 22249, 4212, 4213, 11748,
			4224, 11759, 26372, 26231, 845, 56, 843, 54,
			11928, 11929, 11930, 11926, 12807, 2866, 2864, 2883,
			1485, 9194, 9342, 9245, 21973, 21950, 45, 879,
			9236, 21955, 21932, 27192, 10146, 20994, 20995, 20985,
			20986, 20987, 20988, 11733, 20989, 20990, 11731, 20991,
			20992, 46, 880, 9238, 21959, 21936, 13237, 13229,
			10555, 767, 11165, 11167, 818, 25206, 25204, 25203,
			23357, 2577, 19994, 10550, 24861, 23249, 12596, 27179,
			9756, 9758, 11511, 11509, 173, 23557, 171, 23555,
			169, 23553, 2444, 23551, 10034, 2501, 12327, 12331,
			2495, 12329, 12333, 22281, 2489, 2507, 12869, 10147,
			10083, 131, 21820, 6165, 2581, 31583, 9191, 9339,
			9242, 21967, 21944, 892, 20607, 44, 4803, 9185,
			23601, 26486, 811, 824, 2540, 830, 19580, 868,
			805, 9144, 9465, 9431, 28773, 12018, 12017, 9189,
			9337, 9240, 21963, 21940, 10384, 10388, 27182, 10390,
			10386, 19933, 23191, 13163, 29591, 6724, 26528, 13381,
			13378, 13377, 13380, 13379, 841, 26012, 29667, 26135,
			29664, 29676, 26000, 26072, 4236, 9145, 11865, 27343,
			29583, 6326, 6322, 6328, 6324, 22272, 6330, 10077,
			31157, 6133, 6143, 6135, 6149, 6131, 22333, 33063,
			886, 11702, 41, 9141, 4783, 2, 9179, 9459,
			808, 2534, 827, 19574, 865, 802, 6173, 1133,
			7362, 7364, 1097, 7366, 7368, 28924, 29168, 28872,
			30386, 21981, 11725, 11724, 11723, 11722, 26757, 26751,
			10149, 12922, 10144, 28834, 6522, 28922, 28919, 9188,
			9336, 9239, 21961, 21938, 29580, 12926, 28690, 9706,
			9705, 20933, 20934, 20935, 20936, 20925, 20926, 20927,
			20928, 20997, 21000, 33188, 20929, 20930, 20931, 20932,
			24444, 11887, 11876, 3842, 26547, 19607, 19604, 33247,
			27610, 27612, 28289, 28310, 27614, 28283, 8842, 8840,
			8839, 11664, 26475, 21907, 187, 5937, 5940, 27655,
			27652, 7208, 10280, 847, 58, 849, 60, 10824,
			10282, 855, 66, 857, 68, 9452, 10368, 10372,
			27181, 10374, 10370, 19936, 23194, 13161, 26239, 26237,
			26374, 27186, 26235, 12934
		));
	}

	private static void addMage(Map<String, List<Integer>> m)
	{
		// MAGE — 831 items
		//   LLM classified (831)
		m.put(TAG_MAGE, Arrays.asList(
			// === LLM classified ===
			10344, 23342, 10342, 27183, 10340, 20577, 10338, 20576,
			12422, 11101, 11099, 11095, 27665, 27679, 27676, 27662,
			6894, 6895, 30843, 12881, 4708, 4714, 20599, 4712,
			20598, 4710, 23653, 1397, 573, 556, 11688, 11715,
			12728, 19707, 6585, 12436, 10362, 1708, 1710, 1727,
			10366, 1731, 12851, 30376, 21018, 25518, 21024, 27194,
			21021, 27193, 21049, 26346, 26344, 26342, 26340, 12199,
			27616, 27627, 20430, 12203, 26353, 26350, 12621, 12623,
			27624, 4675, 20431, 21633, 21634, 6904, 6905, 6906,
			31081, 6910, 29806, 33202, 26551, 21079, 12827, 12825,
			6891, 12263, 12259, 12617, 12618, 12619, 12620, 29628,
			21697, 9075, 11699, 11156, 31088, 31093, 31097, 33172,
			31092, 31096, 31091, 31095, 31094, 12275, 12271, 3239,
			22458, 22455, 22452, 22449, 1391, 11014, 6908, 11783,
			11782, 11781, 11779, 11778, 11777, 11776, 11784, 581,
			10148, 12445, 12447, 12453, 12455, 12449, 12451, 24607,
			24609, 24613, 29640, 29637, 29634, 29631, 24611, 26705,
			24615, 24621, 26704, 28268, 565, 11697, 11714, 31163,
			25404, 25410, 25407, 25413, 25416, 29013, 29843, 29019,
			29845, 28988, 29849, 29016, 29844, 7386, 7388, 579,
			7394, 7396, 577, 7390, 7392, 559, 11691, 28796,
			8014, 8015, 13513, 12612, 26490, 13157, 25818, 22951,
			20140, 31881, 22975, 22372, 22368, 20718, 27293, 26755,
			26749, 25162, 25161, 25160, 25159, 20523, 777, 562,
			11694, 11712, 12738, 26967, 8890, 31106, 30813, 23840,
			23833, 23853, 23852, 23854, 28547, 28549, 29602, 27850,
			27852, 27851, 564, 11696, 30384, 23890, 23887, 23888,
			23893, 23892, 23870, 23899, 23898, 23900, 6899, 29486,
			11709, 6898, 24288, 27123, 24294, 27127, 24291, 27125,
			27121, 24333, 12459, 12528, 12457, 12458, 29566, 29570,
			29568, 22516, 560, 11692, 11713, 11896, 11897, 11898,
			21799, 33308, 33311, 30640, 30371, 24632, 24629, 24626,
			24623, 23754, 23751, 23748, 23745, 27281, 30070, 1664,
			6903, 1645, 20736, 4696, 1399, 575, 557, 11689,
			11717, 12732, 30445, 30451, 30449, 30447, 30568, 30437,
			30443, 30441, 30439, 21140, 20595, 27176, 27119, 20520,
			27175, 27117, 27113, 20517, 27174, 27115, 24425, 24517,
			9729, 20524, 2890, 25985, 27251, 33176, 27253, 26759,
			26753, 13073, 26471, 13072, 26469, 6896, 30378, 8019,
			8020, 8017, 8021, 8018, 8016, 7400, 7398, 21800,
			7399, 13505, 13463, 13472, 13502, 13469, 13511, 13481,
			13475, 13448, 13454, 13490, 13457, 13451, 13478, 13460,
			13484, 13499, 13466, 21798, 13235, 23644, 13227, 31113,
			31115, 3755, 3470, 1393, 30631, 569, 554, 11686,
			11718, 12734, 27638, 27635, 27632, 27629, 3797, 27544,
			6106, 6111, 6109, 27166, 6107, 6108, 27167, 27168,
			20134, 30380, 2413, 10442, 13335, 10454, 10462, 2416,
			24217, 24723, 24423, 24511, 10547, 30111, 9770, 25731,
			25733, 20128, 11159, 23075, 1409, 33330, 12658, 33332,
			30628, 28270, 6900, 21793, 23603, 29615, 21784, 21786,
			20724, 21791, 23607, 29617, 21776, 21778, 21795, 23605,
			29613, 21780, 21782, 6920, 27170, 6924, 6922, 6918,
			6916, 11088, 21797, 33251, 20945, 20946, 20947, 20948,
			20937, 20938, 20939, 20940, 21043, 20941, 20942, 20943,
			20944, 21006, 23626, 31814, 12004, 3053, 21198, 4699,
			21202, 563, 11695, 6893, 12421, 12530, 12419, 12420,
			25975, 33255, 33257, 9102, 9100, 9101, 9099, 9096,
			9098, 9104, 9084, 9091, 9092, 9093, 9097, 6889,
			23652, 9762, 11491, 11489, 9024, 9023, 9022, 9021,
			12932, 9764, 11515, 11513, 3046, 3044, 3042, 3040,
			1389, 28291, 28313, 33180, 28281, 11931, 11932, 11933,
			11924, 12806, 32352, 23522, 6914, 5741, 13280, 27255,
			5801, 5881, 9733, 558, 11690, 12736, 9731, 20730,
			4695, 31109, 9070, 9073, 9074, 9072, 9069, 9068,
			9071, 6562, 4698, 1405, 4097, 4107, 23059, 4117,
			26539, 20739, 1407, 1401, 4095, 4105, 23056, 4115,
			26537, 4089, 4099, 23047, 4109, 26531, 3054, 21200,
			20733, 6563, 4093, 20426, 4103, 27159, 23053, 4113,
			27161, 26535, 4091, 20425, 4101, 27158, 23050, 4111,
			27160, 26533, 23113, 23116, 23119, 23110, 12000, 11789,
			12796, 1403, 561, 11693, 30390, 30392, 24422, 12002,
			23654, 19720, 20065, 6575, 10146, 6902, 20994, 20995,
			20985, 20986, 20987, 20988, 11733, 20989, 20990, 11731,
			20991, 20992, 6883, 6901, 26948, 26945, 25206, 25204,
			25203, 6885, 29594, 33184, 10147, 131, 21820, 2550,
			28329, 6583, 30806, 20137, 20131, 7648, 7639, 7647,
			7646, 7644, 7643, 7642, 7641, 6897, 12791, 23650,
			27086, 30692, 24587, 25481, 25478, 26731, 26735, 26733,
			12018, 12017, 22323, 22481, 2412, 10440, 13331, 10452,
			2415, 13256, 27641, 28931, 28304, 6731, 11770, 23624,
			28272, 33253, 26541, 29679, 26066, 26012, 29667, 26135,
			29664, 28484, 28517, 33054, 26078, 26003, 10839, 31154,
			6147, 6141, 6153, 6137, 6139, 27673, 9013, 21276,
			11865, 21257, 4170, 21255, 27343, 11998, 28274, 4697,
			29583, 25578, 30068, 566, 11698, 30759, 12821, 2396,
			3387, 3393, 3391, 3385, 3389, 1379, 1381, 24144,
			23363, 1385, 1387, 22296, 11791, 23613, 1383, 33063,
			22335, 11787, 12795, 4694, 12798, 29412, 28929, 28924,
			21981, 11729, 11728, 11727, 11726, 26757, 26751, 10149,
			31160, 25389, 25395, 25392, 25398, 25401, 12846, 6912,
			28834, 8022, 22555, 27788, 27785, 22552, 27279, 6526,
			30064, 20714, 27358, 25574, 19544, 27171, 23444, 23348,
			29580, 33036, 12902, 33035, 12904, 22288, 33326, 11905,
			33323, 12899, 22292, 33318, 33314, 28570, 28561, 27275,
			27277, 30634, 24670, 24664, 24668, 24666, 24444, 21698,
			21704, 21707, 21701, 12900, 22294, 33320, 33316, 11908,
			22290, 33328, 3842, 26547, 567, 33247, 31148, 26241,
			26245, 33198, 26243, 33196, 8842, 8841, 8840, 8839,
			11663, 26473, 24424, 25517, 29609, 24514, 28585, 28583,
			1395, 571, 555, 11687, 11716, 12730, 6603, 2579,
			27162, 1017, 24860, 1907, 21880, 22208, 21637, 13393,
			13385, 13389, 13387, 2414, 10444, 13333, 10456, 1033,
			1035, 10460, 2417, 19532, 19538, 12934, 22650, 27839,
			22656, 27841, 22653, 27840, 22647, 23617, 27920
		));
	}

	private static void addPrayer(Map<String, List<Integer>> m)
	{
		// PRAYER — 566 items
		//   LLM classified (566)
		m.put(TAG_PRAYER, Arrays.asList(
			// === LLM classified ===
			12437, 23345, 23339, 23336, 23342, 25775, 7979, 1430,
			31075, 30975, 30973, 23077, 27349, 27347, 6585, 20235,
			26346, 26344, 26342, 26340, 26229, 26227, 26223, 26225,
			26221, 12197, 12199, 19921, 24201, 11061, 12203, 26353,
			26350, 12622, 12624, 12195, 12193, 12201, 31081, 21079,
			12827, 12825, 10808, 13121, 13122, 13123, 13124, 12261,
			12263, 19930, 24192, 12259, 12255, 12253, 12257, 25781,
			3402, 4260, 534, 12273, 12275, 19924, 24195, 12271,
			12267, 12265, 12269, 7977, 4256, 530, 4266, 3182,
			4257, 532, 5070, 5076, 25463, 1426, 25422, 29352,
			29354, 29346, 29348, 29381, 29338, 29344, 27335, 29356,
			29366, 29370, 31266, 29368, 29358, 29374, 29372, 12831,
			23642, 31264, 29362, 29364, 29360, 29350, 24605, 24603,
			24601, 24598, 13116, 22986, 4255, 526, 2530, 3187,
			8015, 3844, 26488, 12612, 26490, 13159, 12610, 26492,
			25818, 12608, 26494, 13155, 31886, 31861, 31844, 24204,
			25459, 4286, 33115, 4258, 528, 4259, 3127, 29088,
			21543, 31383, 25672, 25660, 11338, 7976, 7975, 10977,
			6729, 29376, 6728, 3839, 3841, 3843, 12607, 12609,
			12611, 30626, 22954, 21034, 4261, 536, 22111, 22756,
			22783, 22960, 538, 540, 28132, 28945, 28942, 4278,
			25340, 23882, 23883, 23884, 23885, 24425, 27251, 29562,
			12819, 12817, 13508, 13505, 13463, 13496, 13472, 13493,
			13502, 13469, 13511, 13481, 13475, 13448, 26997, 13487,
			13454, 13490, 13457, 13451, 13478, 13460, 13484, 13499,
			13466, 7893, 13126, 13127, 13117, 13118, 13119, 13120,
			4853, 4830, 25766, 3404, 27638, 27635, 27632, 27629,
			31335, 31729, 30898, 25467, 4265, 3181, 25654, 10448,
			10442, 19927, 12639, 27163, 10454, 3835, 3836, 3837,
			3838, 10466, 10462, 10472, 24725, 10828, 30111, 20220,
			3840, 26496, 13149, 12833, 33002, 12598, 1718, 19997,
			6714, 27351, 20229, 22758, 22786, 22988, 25778, 5576,
			9668, 5575, 5574, 24417, 31386, 4271, 3125, 29384,
			29386, 29382, 7980, 7981, 7978, 21551, 21600, 21606,
			21604, 21608, 21602, 4269, 3186, 11922, 11943, 3396,
			10976, 3448, 6213, 25772, 11337, 3444, 3130, 251,
			13280, 21549, 21580, 21586, 21584, 21588, 21582, 4264,
			3180, 542, 20202, 23306, 544, 20199, 23303, 4267,
			3183, 28893, 29213, 29195, 29083, 29082, 29081, 29080,
			13112, 26591, 30627, 21157, 3440, 4850, 7842, 3428,
			3426, 3424, 3422, 4855, 4834, 3128, 20226, 3398,
			10886, 10890, 9759, 20969, 20970, 20971, 20972, 20961,
			20962, 20963, 20964, 20965, 20966, 20967, 20968, 9761,
			11467, 11465, 143, 20396, 141, 20395, 139, 20394,
			2434, 20393, 30134, 30131, 30128, 30125, 426, 428,
			9676, 9670, 9666, 9674, 9672, 9678, 3438, 21545,
			22943, 22945, 22947, 21553, 21610, 21616, 21614, 21618,
			21612, 21620, 4854, 4832, 25670, 26592, 19672, 20957,
			20958, 20959, 20960, 20949, 20950, 20951, 20952, 20953,
			20954, 20955, 20956, 19550, 19710, 12601, 13202, 3400,
			31389, 28775, 33010, 1432, 3436, 3434, 3432, 3430,
			10931, 23565, 10929, 23563, 10927, 23561, 10925, 23559,
			10446, 10390, 10440, 10386, 19933, 11806, 12637, 27165,
			10452, 3827, 3828, 3829, 3830, 10464, 10458, 10470,
			24198, 548, 546, 5615, 3123, 26099, 33060, 28520,
			26129, 25465, 2963, 4270, 21547, 21570, 21576, 21574,
			21578, 21572, 4263, 3179, 4268, 3185, 29587, 26590,
			26589, 19634, 25344, 33231, 12823, 12821, 12829, 25461,
			26593, 31333, 31726, 29378, 29424, 28939, 28936, 28933,
			11495, 11493, 3030, 23573, 3028, 23571, 3026, 23569,
			3024, 23567, 22116, 22124, 6211, 27333, 27331, 27329,
			27327, 25666, 30367, 29958, 21047, 1716, 20223, 3842,
			27191, 26498, 13151, 1724, 21566, 21564, 21568, 21562,
			1722, 1714, 25419, 3325, 4757, 4755, 4753, 4759,
			25769, 8841, 21907, 20232, 6609, 6589, 6619, 6615,
			6587, 6623, 6629, 6599, 6633, 6607, 6601, 6603,
			6621, 6617, 6625, 6627, 6611, 6631, 6605, 6613,
			3442, 4262, 2859, 22754, 22780, 28899, 6810, 6812,
			3446, 10450, 10444, 19936, 12638, 27164, 10456, 1033,
			1035, 3831, 3832, 3833, 3834, 10468, 10460, 10474,
			25440, 25438, 25436, 25434, 4852, 4812
		));
	}

	private static void addCooking(Map<String, List<Integer>> m)
	{
		// COOKING — 739 items
		//   LLM classified (739)
		m.put(TAG_COOKING, Arrays.asList(
			// === LLM classified ===
			7198, 5767, 2297, 5992, 2323, 5378, 5380, 5382,
			5386, 1905, 7744, 5785, 5739, 5865, 5996, 5751,
			5825, 5753, 5905, 6961, 6701, 1963, 4016, 6006,
			6008, 5376, 10964, 2164, 1917, 7740, 10880, 24595,
			31674, 31695, 31128, 32344, 2064, 20747, 19662, 7919,
			33091, 1923, 8974, 4456, 8972, 1921, 2021, 20862,
			2309, 2307, 7491, 7490, 7489, 7488, 1925, 1927,
			1929, 13443, 29159, 26647, 20869, 2247, 9990, 9982,
			32347, 2311, 1903, 5002, 2144, 7226, 31698, 2199,
			2013, 11938, 3383, 7090, 323, 343, 357, 367,
			369, 20854, 23873, 7531, 29161, 7520, 32315, 2175,
			32323, 32339, 7570, 31567, 3148, 29155, 29157, 381,
			393, 32355, 2146, 7948, 7094, 7092, 2426, 2345,
			2329, 1867, 2305, 26637, 6699, 7222, 31706, 10140,
			399, 387, 7954, 3375, 6301, 2005, 5990, 375,
			31559, 33112, 32331, 1965, 5460, 5468, 5470, 5474,
			1891, 1887, 5769, 25672, 25660, 11326, 1985, 2259,
			5755, 7754, 5833, 5757, 5913, 1949, 29947, 7062,
			7054, 2074, 6794, 2209, 1973, 2185, 1897, 1975,
			1977, 7074, 1871, 1869, 7086, 1873, 5763, 5849,
			5929, 10963, 2026, 2023, 2025, 29131, 29217, 2140,
			4291, 2878, 7228, 29134, 7530, 7521, 29149, 7568,
			29152, 29146, 2142, 4293, 29143, 29077, 24785, 2343,
			29137, 3228, 29140, 5988, 33109, 29128, 1955, 9801,
			775, 9803, 977, 7463, 25958, 31174, 2165, 25960,
			4242, 4243, 7730, 7733, 7736, 30985, 30987, 4458,
			2011, 5970, 30977, 30981, 30983, 30979, 1573, 1911,
			7748, 5809, 5745, 5889, 22929, 22795, 2092, 1913,
			5777, 5747, 5857, 2126, 29415, 403, 10971, 1944,
			7064, 7056, 1980, 20742, 2128, 2154, 3367, 3373,
			7934, 10969, 10965, 7188, 1946, 7082, 7084, 5004,
			10962, 10961, 2277, 2084, 7178, 2171, 2167, 337,
			7518, 4517, 32312, 2019, 20875, 2169, 2166, 10881,
			19653, 1947, 30037, 1987, 10960, 10878, 1909, 5743,
			5793, 5873, 6683, 10966, 20871, 25654, 32320, 32357,
			2249, 2177, 2201, 9478, 9480, 9482, 9483, 9485,
			9558, 9559, 9561, 9563, 9577, 9579, 9581, 9583,
			32336, 5994, 25565, 7691, 2285, 1997, 21031, 21033,
			7225, 29952, 1935, 1991, 1937, 1993, 31564, 21394,
			1971, 1881, 2162, 946, 20879, 20868, 3365, 3371,
			20860, 2102, 2104, 2106, 2120, 2122, 2124, 10970,
			9052, 32352, 32362, 5765, 13280, 2327, 2293, 7070,
			5801, 5881, 9568, 9566, 9574, 9575, 9576, 9569,
			9571, 9572, 9573, 9567, 9570, 4014, 4012, 2955,
			7750, 5817, 5749, 5897, 7170, 20881, 6004, 7066,
			21690, 7058, 10968, 20864, 31903, 4239, 4240, 4237,
			4241, 2245, 2094, 2197, 2173, 1957, 1875, 5454,
			2108, 2110, 2112, 7487, 7486, 7485, 7484, 5388,
			5390, 5392, 5396, 23874, 2339, 5972, 7192, 7194,
			7182, 7184, 7172, 7174, 7164, 7166, 7212, 7214,
			7202, 7204, 1953, 6697, 24788, 6883, 20877, 2313,
			7162, 2315, 2114, 2116, 2301, 2048, 2118, 1865,
			1863, 2283, 2289, 10877, 3146, 1931, 7468, 2130,
			1933, 2516, 1942, 6703, 6705, 5420, 5438, 5422,
			5424, 5428, 5430, 5432, 5434, 20873, 2028, 2223,
			2239, 2229, 2030, 2032, 2034, 2225, 2036, 2241,
			2221, 2243, 2231, 2235, 2227, 2219, 2237, 2040,
			2233, 20883, 20856, 31677, 31703, 10136, 7196, 321,
			13439, 29101, 363, 2250, 2136, 9986, 2132, 4287,
			9978, 31692, 32341, 25833, 20861, 25670, 5001, 25658,
			2138, 4289, 2876, 341, 2202, 11934, 29107, 7186,
			7529, 7176, 32309, 20874, 2178, 29119, 20870, 7543,
			25652, 32317, 32333, 345, 7566, 31561, 3142, 20878,
			29125, 20867, 29122, 20859, 377, 353, 389, 32349,
			7944, 29113, 29076, 7168, 20880, 20863, 24782, 2337,
			23872, 6178, 20876, 349, 20872, 20882, 29110, 20855,
			3226, 31700, 10138, 2134, 31686, 20865, 331, 327,
			395, 383, 317, 2514, 3379, 7577, 7216, 29116,
			20857, 1940, 371, 31553, 33106, 25664, 335, 359,
			1859, 29104, 7206, 10816, 32325, 31671, 31689, 10879,
			7483, 7482, 7481, 7480, 1951, 2325, 9988, 9980,
			10967, 7223, 6963, 20866, 25674, 25662, 2880, 25656,
			25668, 10882, 13339, 7078, 1847, 2158, 401, 13407,
			13413, 13400, 13397, 13406, 13415, 13403, 13398, 13401,
			13410, 13411, 13412, 13404, 13409, 13414, 13399, 13418,
			13405, 13402, 13408, 13417, 2080, 26033, 28502, 6305,
			9992, 9984, 7230, 7224, 5759, 5841, 5921, 3162,
			7080, 7580, 2007, 2213, 9996, 7072, 7479, 2156,
			9994, 2160, 6291, 6295, 6299, 6303, 6293, 6297,
			1969, 6965, 25631, 2003, 5398, 5400, 5402, 5406,
			5504, 7579, 21626, 7218, 4608, 20858, 1941, 2150,
			10978, 5986, 7088, 31556, 2187, 10859, 7700, 25666,
			8988, 3363, 3369, 2255, 2217, 2152, 1982, 5960,
			5962, 5964, 5968, 29958, 6962, 7068, 7060, 1877,
			1879, 1883, 1885, 1861, 2317, 2321, 19656, 1889,
			2009, 22789, 7076, 2319, 21684, 2287, 2001, 1995,
			1996, 2251, 2257, 2261, 2279, 9479, 9481, 9484,
			9486, 2179, 2189, 2193, 9560, 9562, 9564, 2042,
			2050, 2056, 2066, 2076, 2082, 2086, 2207, 2211,
			2215, 9578, 9580, 9582, 9584, 29900, 2195, 2281,
			2015, 5982, 5984, 31811, 2017, 6469, 7208, 2054,
			2253, 2205, 2191, 2341, 7495, 7494, 7493, 7492,
			32328, 20749, 20752
		));
	}

	private static void addWcFletching(Map<String, List<Integer>> m)
	{
		// WC_FLETCHING — 404 items
		//   LLM classified (404)
		m.put(TAG_WC_FLETCHING, Arrays.asList(
			// === LLM classified ===
			20011, 28226, 2862, 890, 43, 1357, 518, 9143,
			9380, 4798, 9183, 9463, 810, 823, 28211, 829,
			19578, 9429, 21326, 21350, 21338, 21316, 25849, 25853,
			21318, 21352, 28134, 10810, 52, 28991, 31004, 30998,
			492, 5751, 5753, 31045, 19592, 19601, 47, 881,
			3239, 5070, 5073, 5074, 7413, 22798, 22800, 5076,
			1361, 514, 4788, 28205, 4821, 24691, 10089, 9139,
			9376, 9176, 9456, 9422, 1777, 31052, 11885, 11874,
			4160, 11875, 31086, 494, 496, 498, 500, 502,
			504, 506, 6741, 882, 39, 1351, 9375, 4773,
			9174, 9454, 819, 28196, 825, 19570, 9420, 20696,
			20695, 31575, 32904, 31547, 28613, 22935, 22869, 28163,
			28166, 23127, 19718, 4827, 9438, 23673, 23862, 28220,
			23953, 23869, 9192, 9340, 21969, 31047, 11212, 11237,
			6739, 25378, 6743, 21905, 21930, 21902, 21921, 11230,
			11232, 28217, 19484, 19582, 21918, 22795, 9193, 9341,
			21971, 25110, 29415, 9190, 9338, 21965, 31032, 31018,
			314, 11881, 28177, 1779, 9783, 9785, 31043, 2865,
			28157, 28143, 28175, 28173, 28136, 28171, 28169, 28670,
			28626, 28138, 23279, 28663, 7405, 31024, 31027, 53,
			31010, 19589, 29311, 29305, 28869, 19598, 19595, 13241,
			25066, 30347, 13242, 25371, 30348, 884, 40, 1349,
			510, 9140, 9377, 4778, 9177, 9457, 807, 820,
			28199, 826, 19572, 9423, 31579, 32907, 31549, 9187,
			9335, 21957, 32902, 19584, 13355, 13137, 10158, 10105,
			20799, 946, 6020, 19478, 19586, 28140, 28146, 1511,
			2511, 10159, 10107, 839, 48, 10933, 10941, 10940,
			10939, 975, 6030, 1513, 859, 70, 3448, 5316,
			22263, 861, 72, 21952, 6332, 21488, 9450, 6028,
			1517, 851, 62, 6047, 5314, 22257, 853, 64,
			9448, 13280, 9418, 9419, 888, 42, 1355, 516,
			9142, 9379, 4793, 9181, 9461, 809, 822, 28208,
			828, 19576, 9427, 19610, 29171, 28878, 28183, 31957,
			28152, 6022, 1521, 845, 56, 6043, 22251, 843,
			54, 9442, 2866, 2864, 9194, 9342, 21973, 45,
			879, 21955, 6313, 10091, 407, 411, 413, 46,
			880, 21959, 28655, 28616, 28661, 28620, 28622, 28624,
			23878, 10088, 31049, 19669, 19672, 22266, 22871, 28154,
			31583, 32910, 31551, 9191, 9339, 21967, 892, 44,
			1359, 520, 4803, 9185, 811, 824, 28214, 830,
			19580, 9144, 9381, 9465, 9431, 9189, 9337, 21963,
			28628, 8934, 28161, 28159, 50, 33062, 26030, 28529,
			9382, 9436, 6053, 10812, 886, 41, 1353, 512,
			9378, 4783, 9179, 9459, 808, 821, 28202, 827,
			19574, 9425, 28192, 10087, 10132, 28674, 28149, 21626,
			29168, 28872, 6333, 21486, 9446, 6285, 6281, 6283,
			9188, 9336, 21961, 28630, 11887, 11876, 4825, 19607,
			19604, 31054, 6024, 1519, 847, 58, 6045, 22254,
			60, 9444, 11035, 2859, 2861, 9807, 9809, 7797,
			9440, 10090, 6026, 1515, 855, 66, 3446, 5315,
			22260, 857, 68, 9452
		));
	}

	private static void addFishing(Map<String, List<Integer>> m)
	{
		// FISHING — 222 items
		//   LLM classified (222)
		m.put(TAG_FISHING, Arrays.asList(
			// === LLM classified ===
			7198, 13261, 13258, 13259, 13260, 583, 11883, 10129,
			11323, 31255, 25637, 7989, 305, 25559, 7993, 7991,
			1581, 22826, 31873, 21693, 6662, 13430, 405, 25590,
			20853, 11326, 23129, 13651, 13650, 13649, 22829, 22816,
			23823, 32612, 32451, 32753, 23762, 23864, 23953, 25566,
			11936, 11940, 26598, 30773, 7535, 21028, 25373, 21652,
			25114, 6667, 314, 11881, 32307, 25582, 22818, 32380,
			32366, 32368, 32371, 32377, 32383, 32374, 11334, 7188,
			22838, 25585, 6670, 6673, 313, 9798, 9800, 11479,
			11477, 155, 153, 151, 2438, 307, 7779, 6666,
			309, 13429, 7942, 5004, 31408, 337, 31412, 22840,
			22835, 311, 25565, 31420, 21293, 21031, 25059, 30342,
			21033, 25367, 30343, 3157, 3155, 2162, 2149, 11330,
			11332, 11328, 20860, 301, 13280, 21649, 21356, 22820,
			22832, 1585, 23122, 31416, 407, 411, 22842, 22846,
			22844, 21655, 31424, 20856, 22941, 22943, 22945, 22947,
			321, 13439, 363, 32341, 20861, 29216, 25670, 5001,
			25658, 2876, 341, 11934, 338, 32309, 25652, 32317,
			32333, 25564, 345, 31561, 3142, 3150, 20867, 2148,
			20859, 377, 353, 389, 32349, 7944, 29113, 20863,
			23872, 349, 29110, 20855, 10138, 31686, 20865, 331,
			327, 395, 383, 317, 2514, 3379, 20857, 371,
			31553, 25664, 335, 359, 32325, 25, 11324, 13339,
			13431, 13432, 30900, 33062, 28502, 303, 6209, 25578,
			25598, 25592, 31252, 25594, 25596, 25569, 25588, 10087,
			7990, 25561, 7994, 7992, 31410, 31414, 31422, 31418,
			31426, 31430, 31611, 31608, 31605, 31602, 10978, 31428,
			25580, 25567, 6674, 31820, 26579, 32328
		));
	}

	private static void addFiremaking(Map<String, List<Integer>> m)
	{
		// FIREMAKING — 113 items
		//   LLM classified (113)
		m.put(TAG_FIREMAKING, Arrays.asList(
			// === LLM classified ===
			26822, 2862, 10810, 10808, 592, 3402, 24691, 7331,
			7406, 20698, 20696, 20695, 20720, 29777, 4548, 4546,
			4544, 20718, 32904, 31383, 36, 4529, 29947, 4073,
			1468, 13573, 27426, 25110, 22595, 20722, 4527, 10980,
			4525, 4535, 20791, 13329, 9804, 9806, 7794, 3404,
			20275, 7330, 7405, 13241, 13242, 32907, 31386, 32902,
			20799, 10973, 7053, 3396, 1511, 2511, 21869, 1513,
			3448, 6332, 6213, 11337, 1517, 3444, 13280, 5014,
			1521, 3440, 4522, 4537, 4540, 3428, 3422, 9934,
			3398, 10326, 10329, 3438, 20710, 20704, 20708, 20706,
			24554, 7329, 7404, 19669, 19672, 20702, 20701, 20700,
			20699, 3400, 32910, 31389, 3436, 3434, 3432, 3430,
			4700, 6448, 22593, 6333, 6211, 590, 7156, 596,
			25419, 22597, 20712, 10327, 10328, 1519, 3442, 1515,
			3446
		));
	}

	private static void addCrafting(Map<String, List<Integer>> m)
	{
		// CRAFTING — 417 items
		//   LLM classified (417)
		m.put(TAG_CRAFTING, Arrays.asList(
			// === LLM classified ===
			2365, 774, 773, 11103, 11101, 11099, 11095, 26813,
			22133, 22135, 1397, 573, 21347, 21350, 21338, 25853,
			21352, 1595, 29799, 28298, 27269, 19473, 31045, 1759,
			8650, 8652, 8658, 8662, 8666, 8668, 8670, 8678,
			3239, 1391, 948, 1919, 28293, 28279, 28295, 21512,
			2503, 2497, 22284, 2491, 2509, 1747, 3353, 3351,
			3361, 3345, 3355, 3349, 3359, 3347, 3357, 29338,
			3329, 2499, 2493, 22278, 2487, 2505, 1751, 1767,
			10081, 9434, 31475, 31478, 31472, 1777, 1923, 11065,
			11074, 3335, 7540, 7541, 689, 1794, 6235, 6257,
			6279, 6233, 6255, 6277, 6231, 6253, 6275, 6229,
			6251, 6273, 6225, 6247, 6269, 6223, 6245, 6267,
			6221, 6243, 6265, 6219, 6241, 6263, 1757, 3333,
			22372, 1925, 1783, 4546, 4544, 688, 32364, 10981,
			1755, 6169, 434, 8792, 10595, 28166, 1169, 29920,
			31460, 31469, 1739, 7537, 7539, 9780, 9782, 1633,
			23956, 23962, 4207, 10820, 6155, 19665, 7938, 1601,
			1700, 1681, 11092, 1662, 1643, 1664, 1615, 1702,
			1683, 11115, 1645, 1399, 575, 1605, 1696, 1677,
			11076, 1658, 1639, 22660, 4527, 6667, 10980, 4525,
			4535, 5350, 8018, 23951, 25859, 21270, 6167, 3470,
			569, 6171, 1779, 29163, 7536, 7538, 33384, 33393,
			33387, 33390, 21504, 11656, 1785, 1692, 1673, 2357,
			11069, 1654, 1635, 26788, 1135, 1099, 22275, 1065,
			1745, 1753, 1771, 10079, 958, 10814, 1743, 22269,
			1131, 31457, 31466, 1599, 30085, 30076, 30079, 30073,
			30082, 22983, 33382, 1611, 21111, 21102, 21120, 21093,
			21084, 29218, 5931, 4542, 1741, 1129, 1061, 1095,
			1167, 1059, 22287, 1063, 10973, 3211, 3420, 31463,
			23836, 23876, 22201, 6051, 6038, 28291, 22195, 22192,
			13280, 32386, 29292, 29286, 29289, 29283, 29280, 1775,
			21690, 3327, 31954, 31957, 1597, 1733, 21515, 3331,
			4522, 4540, 6573, 6581, 6579, 11130, 6577, 6575,
			1609, 21108, 21099, 21117, 21090, 21081, 9934, 411,
			413, 10995, 10996, 3339, 3343, 3337, 3341, 1931,
			4440, 6041, 1773, 10134, 2501, 2495, 22281, 2507,
			1749, 12869, 1763, 10083, 1613, 1951, 22204, 6165,
			1592, 968, 6157, 6159, 1603, 1698, 1679, 9191,
			11085, 1660, 1641, 25481, 1607, 1694, 1675, 9189,
			11072, 1656, 1637, 27897, 401, 28304, 12927, 1735,
			2976, 26036, 950, 2355, 9382, 442, 2961, 9436,
			6163, 29299, 6287, 7801, 6289, 6326, 6322, 6328,
			6324, 22272, 6330, 1781, 1761, 12009, 10077, 3387,
			3385, 8710, 8752, 8754, 8760, 8764, 8772, 8774,
			2370, 6173, 3224, 10132, 29177, 9080, 21521, 1734,
			27279, 5525, 5523, 21114, 21105, 21123, 21096, 21087,
			7939, 7771, 7767, 7759, 28287, 1716, 1617, 1631,
			1621, 1627, 6571, 1625, 1629, 1619, 1623, 19496,
			1791, 28193, 1789, 5352, 1787, 4438, 1594, 1724,
			567, 1722, 1720, 1714, 28289, 28301, 1395, 571,
			21518, 5933, 1793, 10891, 1737, 13383, 10818, 10822,
			10824, 22198, 19493, 19541, 19501, 19532, 19535, 19538,
			19529
		));
	}

	private static void addMiningSmithing(Map<String, List<Integer>> m)
	{
		// MINING_SMITHING — 346 items
		//   LLM classified (346)
		m.put(TAG_MINING_SMITHING, Arrays.asList(
			// === LLM classified ===
			2365, 446, 20014, 43, 9380, 31912, 1111, 31926,
			823, 19578, 32011, 1145, 488, 1271, 1123, 2361,
			9429, 4823, 449, 21537, 21347, 4, 27616, 32879,
			32876, 19473, 25684, 25635, 25676, 22603, 31245, 12592,
			12297, 13575, 9467, 9376, 9422, 668, 31881, 11074,
			698, 468, 470, 472, 474, 476, 478, 11923,
			12594, 28813, 30324, 1307, 39, 1351, 2349, 9375,
			31906, 1103, 31918, 819, 31932, 19570, 31999, 9420,
			1139, 4819, 480, 1265, 1173, 1337, 1794, 4544,
			29088, 25539, 25543, 32886, 28276, 434, 23442, 20364,
			20362, 453, 21534, 27021, 30860, 436, 23837, 23822,
			30848, 23877, 23680, 23863, 23953, 32892, 9632, 24706,
			697, 27012, 11286, 32017, 21895, 22103, 22097, 22100,
			11920, 12797, 27695, 23677, 25376, 12800, 1913, 5777,
			5747, 5857, 13573, 13572, 25112, 22595, 2893, 2892,
			21392, 33393, 23276, 11798, 11818, 11820, 11822, 11794,
			11796, 11800, 2357, 721, 444, 21535, 12012, 25555,
			25549, 25551, 25553, 776, 6979, 21728, 21726, 2347,
			21539, 1580, 25644, 29775, 30854, 30862, 30858, 13243,
			25063, 30345, 13244, 25369, 30346, 30864, 30846, 1309,
			40, 1349, 2351, 9377, 31908, 31920, 820, 31934,
			19572, 32002, 1191, 9423, 1137, 4820, 440, 21532,
			482, 1267, 7225, 30808, 13570, 27014, 32032, 32023,
			32029, 21540, 32035, 32026, 32889, 31716, 3211, 13354,
			13356, 9077, 9076, 30856, 13280, 9792, 21343, 5014,
			9794, 7791, 9416, 1315, 42, 1355, 2359, 9379,
			31910, 1109, 31924, 822, 1159, 31938, 19576, 32008,
			1197, 9427, 1428, 1143, 4822, 447, 21536, 486,
			1273, 1121, 31719, 680, 30756, 30765, 4540, 27019,
			27693, 12011, 27010, 12016, 12013, 12014, 12015, 23906,
			2568, 31724, 31722, 44, 1359, 31914, 31928, 824,
			19580, 32014, 4824, 490, 1275, 1347, 2363, 9381,
			9431, 451, 21538, 686, 28349, 13421, 6971, 13539,
			13544, 13549, 13554, 13559, 13538, 13543, 13548, 13553,
			13558, 13541, 13546, 13551, 13556, 13561, 13540, 13545,
			13550, 13555, 13560, 13542, 13547, 13552, 13557, 13562,
			13565, 13566, 13567, 13568, 13569, 2366, 2368, 28505,
			26024, 2355, 442, 21533, 22006, 9795, 27017, 9797,
			27027, 27029, 27031, 27025, 27023, 12009, 12010, 6448,
			25547, 25527, 29412, 41, 1353, 2353, 9378, 2,
			1105, 31922, 821, 31936, 19574, 32005, 9425, 1539,
			484, 1269, 2370, 21345, 22593, 23905, 438, 1629,
			21341, 22597, 13104, 13105, 13106, 13107, 21622, 13571,
			21637, 23908
		));
	}

	private static void addHerblore(Map<String, List<Integer>> m)
	{
		// HERBLORE — 764 items
		//   LLM classified (764)
		m.put(TAG_HERBLORE, Arrays.asList(
			// === LLM classified ===
			29972, 30007, 11463, 11461, 3038, 3036, 3034, 3032,
			29982, 29988, 31084, 30014, 29993, 27349, 27347, 21163,
			12640, 12641, 26346, 26344, 26342, 26340, 27616, 26353,
			26350, 30018, 31712, 11475, 11473, 12911, 12909, 12907,
			12905, 12919, 12917, 12915, 12913, 5942, 11503, 11501,
			5949, 5947, 5945, 5943, 5951, 5958, 5956, 5954,
			5952, 11507, 11505, 2458, 2456, 2454, 2452, 25762,
			25763, 25764, 25765, 25754, 25755, 25756, 25757, 11435,
			11433, 25758, 25759, 25760, 25761, 179, 177, 175,
			2446, 29971, 30015, 29784, 28345, 28346, 28351, 1528,
			738, 31659, 31656, 31653, 31650, 592, 11431, 11429,
			125, 123, 121, 2428, 261, 103, 5298, 30016,
			6008, 30795, 22470, 22467, 22464, 22461, 22458, 22455,
			22452, 22449, 5075, 1582, 29640, 29637, 29634, 29631,
			22420, 31674, 243, 19662, 4456, 20698, 20908, 6016,
			265, 22443, 107, 5301, 30937, 2398, 11326, 29963,
			29643, 1973, 1975, 30002, 20912, 5974, 5935, 11447,
			11445, 13064, 9745, 26153, 9743, 26152, 9741, 26151,
			9739, 6476, 6474, 6472, 6470, 23830, 31708, 6693,
			21975, 23867, 23964, 23962, 4460, 30983, 11459, 11457,
			137, 135, 133, 2432, 30800, 9735, 30031, 24644,
			24641, 24638, 24635, 24632, 24629, 24626, 24623, 23754,
			23751, 23748, 23745, 23742, 23739, 23736, 23733, 23706,
			23703, 23700, 23697, 23694, 23691, 23688, 23685, 23730,
			23727, 23724, 23721, 23718, 23715, 23712, 23709, 241,
			11154, 267, 109, 5303, 23882, 23883, 23884, 23885,
			20921, 20922, 20923, 20924, 20913, 20914, 20915, 20916,
			23800, 20917, 20918, 20919, 20920, 31481, 31662, 1980,
			20800, 20742, 11877, 20911, 11455, 11453, 3014, 3012,
			3010, 3008, 29530, 29973, 29833, 29830, 29827, 29824,
			11962, 11960, 11957, 11955, 11953, 11951, 31647, 31644,
			31641, 31638, 22224, 22221, 22218, 22215, 22212, 22209,
			31623, 31620, 31617, 31614, 221, 12859, 3367, 11427,
			11428, 11479, 11477, 155, 153, 151, 2438, 30357,
			27638, 27635, 27632, 27629, 1550, 23517, 23017, 30146,
			30143, 30140, 30137, 9736, 26587, 26585, 26583, 26581,
			20905, 9018, 9016, 3261, 1909, 7746, 5743, 5793,
			5873, 1527, 211, 20907, 215, 217, 20904, 199,
			205, 30094, 209, 213, 2485, 201, 20901, 207,
			1533, 1529, 1525, 3051, 203, 3049, 219, 1531,
			798, 2391, 6681, 23835, 23875, 23881, 249, 91,
			5291, 10142, 9088, 9090, 7654, 7656, 7658, 7666,
			7664, 7662, 7660, 4423, 4421, 4419, 4417, 32357,
			31599, 31596, 31593, 31590, 31587, 5976, 255, 97,
			5294, 10145, 797, 11738, 13226, 4464, 9774, 9776,
			30097, 30100, 30088, 10004, 10002, 10000, 9998, 11519,
			11517, 11262, 23806, 259, 101, 5297, 28837, 247,
			5104, 3153, 10109, 10111, 20945, 20946, 20947, 20948,
			20937, 20938, 20939, 20940, 20941, 20942, 20943, 20944,
			263, 105, 5299, 2481, 2483, 5302, 11992, 11994,
			27272, 225, 5100, 30017, 30009, 9019, 11491, 11489,
			9024, 9023, 9022, 9021, 11515, 11513, 3046, 3044,
			3042, 3040, 6051, 30011, 30013, 32362, 9089, 251,
			93, 5292, 10143, 5741, 13280, 30019, 27211, 27208,
			27205, 27202, 30020, 705, 30032, 10915, 10913, 10911,
			10909, 10923, 10921, 10919, 10917, 29078, 29079, 29083,
			29082, 29081, 29080, 2970, 30005, 28341, 28342, 30012,
			10937, 27321, 27319, 27317, 27315, 27790, 26368, 26231,
			20902, 6043, 12857, 7487, 20993, 20994, 20995, 20996,
			20985, 20986, 20987, 20988, 20989, 11732, 20990, 20991,
			11730, 20992, 233, 26569, 31484, 31665, 6018, 3138,
			25205, 20969, 20970, 20971, 20972, 20961, 20962, 20963,
			20964, 20965, 20966, 20967, 20968, 11467, 11465, 143,
			141, 139, 2434, 30134, 30131, 30128, 30125, 29974,
			33133, 31677, 31710, 99, 5295, 257, 11511, 11509,
			173, 171, 169, 2444, 29996, 31671, 223, 20702,
			20701, 20700, 20699, 20697, 4848, 4846, 4844, 4842,
			11439, 11437, 11451, 11449, 131, 129, 127, 2430,
			20957, 20958, 20959, 20960, 20949, 20950, 20951, 20952,
			20953, 20954, 20955, 20956, 11324, 1534, 4462, 3377,
			10931, 23565, 10929, 23563, 10927, 23561, 10925, 23559,
			6691, 23581, 6689, 23579, 6687, 23577, 6685, 23575,
			3414, 3412, 3410, 3408, 3419, 3418, 3417, 3416,
			11204, 11205, 28478, 26027, 26045, 33135, 2355, 7650,
			993, 1530, 5841, 5761, 27343, 1526, 737, 3000,
			3004, 5300, 231, 22879, 31569, 12635, 12633, 12631,
			12629, 12627, 12625, 9017, 20910, 28388, 11441, 11443,
			119, 117, 115, 113, 28890, 21997, 21994, 21987,
			21984, 21981, 21978, 149, 147, 145, 2436, 12701,
			12699, 12697, 12695, 11499, 11497, 167, 165, 163,
			2442, 11483, 11481, 3022, 3020, 3018, 3016, 31611,
			31608, 31605, 31602, 31635, 31632, 31629, 31626, 13066,
			11724, 11723, 11722, 11495, 11493, 3030, 23573, 3028,
			23571, 3026, 23569, 3024, 23567, 11487, 11485, 161,
			159, 157, 2440, 29531, 185, 183, 181, 2448,
			11471, 11469, 22124, 30884, 30881, 30878, 30875, 1939,
			2150, 253, 95, 5293, 10144, 27333, 27331, 27329,
			27327, 2152, 2998, 3002, 5296, 269, 111, 5304,
			20933, 20934, 20935, 20936, 20925, 20926, 20927, 20928,
			20929, 20930, 20931, 20932, 31487, 31668, 3406, 4840,
			28386, 237, 235, 229, 2390, 22446, 227, 9086,
			21622, 1532, 20801, 23880, 11879, 187, 5937, 5940,
			5936, 5939, 239, 5105, 245, 23489, 20981, 20982,
			20983, 20984, 20973, 20974, 20975, 20976, 20977, 20978,
			20979, 20980, 32360, 6049, 193, 191, 189, 2450,
			11523, 11521, 20752, 12934
		));
	}

	private static void addAgilityThieving(Map<String, List<Integer>> m)
	{
		// AGILITY_THIEVING — 238 items
		//   LLM classified (238)
		m.put(TAG_AGILITY_THIEVING, Arrays.asList(
			// === LLM classified ===
			27410, 27404, 27406, 2996, 29480, 9771, 9773, 11463,
			11461, 3038, 3036, 3034, 3032, 7782, 12640, 12641,
			10846, 10850, 4627, 24777, 88, 7919, 29482, 30042,
			10989, 10981, 10983, 22521, 31764, 24729, 10115, 25244,
			21143, 23948, 3012, 3010, 3008, 13125, 31647, 31644,
			31641, 31638, 5559, 10985, 22207, 10075, 9040, 9028,
			9034, 19653, 11860, 13589, 13601, 13613, 13625, 13637,
			13677, 21076, 24758, 25084, 27459, 30060, 11852, 13581,
			13593, 13605, 13617, 13629, 13669, 21064, 24746, 25072,
			27447, 30048, 30044, 11858, 13587, 13599, 13611, 13623,
			13635, 13675, 21073, 24755, 25081, 27456, 30057, 11850,
			13579, 13591, 13603, 13615, 13627, 13667, 21061, 24743,
			25069, 27444, 30045, 11856, 13585, 13597, 13609, 13621,
			13633, 13673, 21070, 24752, 25078, 27453, 30054, 11854,
			13583, 13595, 13607, 13619, 13631, 13671, 21067, 24749,
			25075, 27450, 30051, 11682, 24723, 24721, 24727, 24711,
			24731, 24725, 24719, 4310, 4304, 4308, 4302, 4306,
			4300, 4298, 29325, 8869, 9026, 13138, 11138, 24862,
			24866, 10991, 1523, 24790, 24864, 6416, 6420, 6418,
			11849, 13280, 21656, 10987, 9418, 9419, 9416, 30676,
			4024, 4025, 4599, 6410, 6408, 10553, 2997, 9032,
			9036, 10993, 24867, 6970, 24735, 24844, 5557, 5556,
			5554, 5553, 5555, 21307, 28771, 28493, 29658, 29661,
			950, 8868, 24865, 10975, 10071, 10844, 10848, 12633,
			12631, 23589, 12629, 23587, 12627, 23585, 12625, 23583,
			8866, 5560, 4179, 13436, 13437, 13435, 13438, 13434,
			9030, 9038, 24740, 7218, 10845, 10849, 3022, 20551,
			3020, 20550, 3018, 20549, 3016, 20548, 30038, 9777,
			9779, 7785, 7763, 7767, 25099, 29332, 3325, 29460,
			4600, 6414, 6412, 10847, 10851, 24863
		));
	}

	private static void addSlayer(Map<String, List<Integer>> m)
	{
		// SLAYER — 575 items
		//   LLM classified (575)
		m.put(TAG_SLAYER, Arrays.asList(
			// === LLM classified ===
			13263, 5520, 13265, 7979, 13262, 12006, 4151, 26482,
			20525, 12728, 23077, 21338, 21316, 22557, 1706, 1708,
			1710, 1712, 11978, 29804, 26229, 26227, 26223, 26225,
			26221, 21804, 22302, 21807, 26370, 22305, 19677, 21813,
			21810, 21634, 13441, 12919, 12917, 12915, 12913, 5949,
			5947, 5945, 5943, 29806, 29788, 29816, 29818, 28298,
			19675, 25781, 10499, 22109, 10498, 31088, 31093, 31097,
			31092, 31096, 31095, 31094, 4161, 11883, 11832, 11834,
			7977, 24268, 13254, 22470, 22467, 22464, 22461, 31172,
			22458, 22455, 22452, 22449, 28316, 28279, 6737, 12740,
			8921, 8919, 11783, 8917, 11782, 8915, 11781, 8911,
			11779, 8909, 11778, 8907, 11777, 8905, 11776, 11784,
			19639, 19641, 21730, 24605, 24603, 24601, 24598, 24699,
			24697, 24777, 13276, 13275, 13274, 13193, 13116, 22986,
			22951, 23037, 12742, 21817, 21183, 27283, 23083, 22975,
			21724, 11885, 11874, 4160, 11875, 22372, 21175, 21173,
			21171, 21166, 10, 26524, 6, 26520, 12, 26526,
			8, 26522, 20523, 22320, 12738, 28276, 27667, 7976,
			11126, 11124, 11122, 11120, 11118, 11974, 11972, 7975,
			23975, 28577, 23987, 23979, 21275, 11936, 19685, 19679,
			19681, 19683, 6746, 33338, 23742, 23739, 23736, 23733,
			27281, 23706, 23703, 23700, 23697, 23694, 23691, 23688,
			23685, 23730, 23727, 23724, 23721, 23718, 23715, 23712,
			23709, 21012, 22978, 30070, 22804, 22103, 22097, 31996,
			22100, 20849, 22957, 22960, 772, 29596, 20736, 4166,
			12732, 6798, 25340, 21140, 20524, 2890, 11279, 29589,
			22660, 20742, 11877, 4155, 13508, 13496, 13487, 25280,
			13235, 13227, 21270, 21177, 22218, 22215, 22212, 22209,
			12859, 28321, 4164, 27670, 11881, 22981, 12734, 6666,
			3741, 26358, 26360, 26364, 26362, 12769, 7432, 7430,
			7421, 7429, 7428, 7426, 7425, 7424, 7423, 25926,
			25928, 25932, 25934, 25936, 6799, 6800, 30638, 30637,
			30146, 30143, 30140, 30137, 6801, 21643, 21728, 21726,
			21736, 21742, 4153, 21739, 21752, 19643, 19645, 7666,
			13226, 6802, 33066, 33068, 22983, 23073, 23075, 22988,
			22966, 22973, 22971, 22969, 6696, 20724, 6803, 29966,
			5576, 5575, 5574, 7159, 23064, 6804, 12647, 13103,
			7980, 10581, 25979, 30891, 25981, 27287, 27291, 7981,
			22671, 31258, 12004, 7978, 23490, 20727, 4158, 11902,
			6808, 6805, 7053, 13391, 11941, 24261, 9084, 12932,
			12744, 7409, 13198, 13201, 28281, 391, 13280, 22386,
			12736, 9731, 4156, 20730, 24260, 7946, 21155, 21153,
			21151, 21146, 24271, 11748, 13080, 26368, 26372, 26231,
			4168, 29792, 29790, 29794, 12002, 23023, 23025, 23027,
			23031, 23033, 23035, 12857, 13279, 13237, 13229, 13250,
			24262, 143, 141, 139, 2434, 13239, 13231, 29594,
			21264, 21266, 173, 171, 169, 2444, 19647, 19649,
			24942, 21820, 2566, 2564, 2562, 2558, 2556, 2554,
			2552, 13202, 4162, 21754, 20607, 9185, 12791, 13252,
			4081, 10588, 12018, 12017, 4082, 10931, 10929, 10927,
			10925, 25746, 6691, 6689, 6687, 6685, 29591, 397,
			6731, 12931, 12929, 12927, 385, 13381, 13358, 13363,
			13368, 13373, 13378, 13357, 13362, 13367, 13372, 13377,
			13360, 13365, 13370, 13375, 13380, 13359, 13364, 13369,
			13374, 13379, 13361, 13366, 13371, 13376, 26108, 22006,
			6806, 11113, 11111, 11109, 11107, 11970, 11968, 10952,
			9786, 11864, 11865, 9788, 11873, 11872, 11871, 11869,
			11868, 11867, 11866, 21268, 7788, 21257, 5759, 5841,
			5761, 5921, 4170, 21255, 13233, 4551, 2, 7986,
			7984, 7983, 7982, 23079, 7987, 7988, 22673, 7985,
			21987, 21984, 21981, 21978, 149, 147, 145, 2436,
			12701, 12699, 12697, 12695, 167, 165, 163, 2442,
			11729, 11728, 11727, 11726, 11725, 11724, 11723, 11722,
			11495, 11493, 3030, 3028, 3026, 3024, 161, 159,
			157, 2440, 12922, 13196, 13200, 10587, 8013, 21123,
			12926, 12692, 11905, 7060, 21888, 21890, 24466, 24370,
			24444, 21698, 21704, 21707, 21701, 25910, 25912, 25898,
			25900, 28285, 11908, 11887, 11876, 13273, 27660, 27657,
			24258, 24259, 33247, 24266, 25904, 25906, 28310, 28283,
			22542, 12771, 2425, 21907, 21909, 28585, 28583, 28301,
			6735, 12730, 11879, 27655, 7208, 8923, 21637, 22396,
			22394, 22388, 22392, 13393, 22390, 6807, 12934
		));
	}

	private static void addFarming(Map<String, List<Integer>> m)
	{
		// FARMING — 421 items
		//   LLM classified (421)
		m.put(TAG_FARMING, Arrays.asList(
			// === LLM classified ===
			5312, 21160, 6040, 5496, 5480, 5283, 5378, 5380,
			5382, 5386, 13122, 13123, 13124, 5996, 5308, 21697,
			22881, 9932, 10846, 261, 5298, 5825, 5905, 8455,
			8453, 8417, 8429, 8425, 8433, 8461, 8457, 8423,
			1963, 5497, 5481, 5284, 5408, 5410, 5412, 5416,
			6006, 6008, 5305, 5376, 13254, 5281, 5073, 7413,
			13653, 22798, 22800, 6710, 20747, 13427, 13424, 31834,
			22994, 20908, 20909, 1925, 1929, 1965, 5324, 5460,
			5478, 5462, 5464, 5468, 5470, 5472, 5474, 5280,
			6016, 265, 5301, 753, 5102, 30937, 5980, 5769,
			5503, 5487, 5290, 31502, 31547, 31490, 22935, 22856,
			22869, 22848, 5763, 7752, 5849, 5929, 5974, 5978,
			6032, 3271, 19704, 6476, 6474, 6472, 6470, 1955,
			31460, 31545, 23661, 23659, 23655, 5970, 5499, 5483,
			5286, 6457, 30747, 22929, 22866, 22862, 22877, 5303,
			2126, 5103, 31481, 31511, 22660, 5350, 5418, 29538,
			25102, 8445, 13644, 13640, 13642, 13643, 13646, 21253,
			9810, 9812, 9903, 5354, 31541, 7178, 5325, 21504,
			23015, 23011, 23009, 23021, 23017, 23013, 23019, 13426,
			13423, 20905, 20906, 6311, 1947, 9469, 30037, 13657,
			1987, 13353, 13420, 215, 217, 199, 30094, 213,
			2485, 207, 3051, 3049, 219, 9082, 249, 5291,
			5976, 5994, 5307, 4001, 255, 5294, 6057, 31457,
			31543, 22875, 30097, 30088, 22883, 22599, 5297, 31505,
			31549, 31492, 29952, 247, 5104, 5931, 5306, 6113,
			6112, 6000, 5310, 22885, 263, 5299, 2481, 5302,
			6020, 30359, 225, 5100, 13428, 13425, 6030, 6051,
			5374, 7409, 11711, 5316, 5362, 21480, 21488, 21471,
			6028, 6047, 5372, 5314, 5360, 5096, 6010, 5292,
			5765, 13280, 7416, 7418, 4014, 4012, 6004, 5282,
			31903, 5098, 6012, 12792, 12793, 13654, 27790, 20902,
			20903, 6022, 6043, 5370, 5358, 1957, 5319, 5440,
			5458, 5442, 5444, 5448, 5450, 5452, 5454, 2108,
			5498, 5482, 5285, 5388, 5390, 5392, 5396, 6458,
			6459, 5502, 5486, 5289, 5972, 5501, 5485, 5288,
			31484, 31513, 2114, 5500, 5287, 5484, 6955, 6036,
			5356, 13250, 6018, 5106, 1942, 3138, 22873, 5318,
			5420, 5438, 5422, 5424, 5428, 5430, 5432, 5434,
			6041, 6319, 5341, 5347, 5348, 5295, 257, 6454,
			1951, 5101, 22859, 22850, 22871, 28154, 6014, 5097,
			31508, 31551, 31494, 2518, 13252, 13421, 6059, 21490,
			5329, 13639, 5343, 22993, 33062, 26096, 33135, 8441,
			3000, 5300, 231, 22879, 6460, 952, 5328, 6053,
			5375, 5317, 5363, 10844, 5398, 5400, 5402, 5406,
			5504, 5323, 13419, 10845, 6034, 9079, 5986, 5320,
			8449, 20661, 253, 5293, 21477, 21486, 21469, 2998,
			5296, 1982, 5322, 5960, 5962, 5964, 5968, 8443,
			269, 5304, 21483, 31515, 5352, 6456, 21622, 5331,
			5333, 5334, 5335, 5337, 5338, 5339, 5340, 5982,
			5321, 6055, 239, 22932, 22887, 6453, 6469, 6464,
			6461, 5105, 6002, 5311, 5933, 6024, 6045, 5371,
			5313, 5359, 10847, 1793, 5099, 5998, 5309, 6026,
			6049, 5373, 5315, 5361, 20749
		));
	}

	private static void addRunecraft(Map<String, List<Integer>> m)
	{
		// RUNECRAFT — 158 items
		//   LLM classified (158)
		m.put(TAG_RUNECRAFT, Arrays.asList(
			// === LLM classified ===
			26809, 5520, 11103, 11101, 11099, 11095, 26807, 26822,
			26813, 26792, 26811, 30771, 30843, 556, 9693, 1438,
			5527, 26914, 9075, 9106, 26876, 5521, 26390, 565,
			1450, 5549, 25404, 25410, 25407, 25413, 25416, 25698,
			559, 1446, 5533, 13513, 26856, 26880, 26798, 26801,
			25700, 1452, 5543, 26784, 26906, 564, 1454, 5539,
			6653, 24704, 24706, 13446, 7938, 560, 1456, 5547,
			13445, 30640, 27281, 4696, 557, 9695, 1440, 5535,
			26881, 5516, 26804, 28965, 28964, 25280, 554, 9699,
			1442, 5537, 5514, 26788, 26879, 26878, 26820, 26850,
			23907, 26908, 5512, 4699, 563, 1458, 5545, 26912,
			28597, 13280, 26884, 5510, 29955, 25696, 558, 9697,
			1448, 5529, 4695, 4698, 561, 1462, 5541, 26886,
			30887, 26941, 26887, 26894, 26895, 26892, 26896, 26893,
			26889, 26890, 26898, 26891, 26897, 26888, 7936, 20665,
			26815, 26854, 26852, 1436, 9765, 9767, 28599, 5519,
			26039, 5509, 4697, 566, 33231, 1460, 4694, 26885,
			29087, 28929, 25389, 25392, 25398, 25401, 28591, 26910,
			5525, 5523, 13534, 28595, 26882, 28593, 555, 9691,
			1444, 5531, 26883, 21880, 22118, 22121
		));
	}

	private static void addHunter(Map<String, List<Integer>> m)
	{
		// HUNTER — 288 items
		//   LLM classified (288)
		m.put(TAG_HUNTER, Arrays.asList(
			// === LLM classified ===
			11264, 11266, 29236, 29241, 31712, 29323, 29321, 31079,
			13324, 11238, 5408, 5410, 5412, 5416, 10129, 29271,
			29251, 10173, 21512, 5075, 10006, 12740, 5076, 29230,
			11959, 10148, 10085, 10014, 29207, 29189, 29944, 29224,
			31674, 10089, 22826, 31901, 10008, 12742, 20720, 13430,
			10012, 10010, 6656, 10033, 29221, 2978, 2979, 2980,
			2981, 2982, 2983, 2984, 2985, 2986, 2987, 2988,
			2989, 2990, 2991, 2992, 2993, 2994, 10121, 22829,
			29131, 29134, 29149, 29152, 29143, 29140, 29128, 22816,
			31708, 23768, 19665, 10115, 10127, 10063, 10061, 10123,
			11256, 21652, 11244, 11248, 31481, 29273, 29253, 11246,
			10023, 10119, 10092, 9951, 29233, 29163, 10075, 11242,
			10099, 10051, 29229, 10047, 10049, 22835, 31235, 10142,
			29319, 29269, 29263, 29267, 29265, 10145, 29239, 31241,
			9948, 9950, 11159, 10004, 10002, 10000, 9998, 29311,
			29305, 10156, 29244, 29242, 29246, 29248, 30644, 28869,
			11519, 11517, 29309, 28831, 11262, 10028, 10027, 11260,
			11273, 11258, 29166, 10059, 10057, 10158, 10113, 10105,
			10109, 10111, 29223, 10103, 10039, 10035, 29232, 10037,
			29303, 29228, 29297, 29226, 10095, 10045, 10041, 10043,
			10159, 10107, 19732, 22201, 10025, 11259, 12744, 11252,
			22195, 22192, 10143, 13280, 29301, 29292, 29289, 29283,
			29280, 8942, 29171, 29174, 28893, 29213, 29195, 29240,
			22832, 11250, 11254, 10150, 21515, 2871, 31077, 10091,
			10146, 29231, 29275, 29256, 33120, 10067, 10065, 10117,
			20873, 29307, 10134, 10031, 31677, 29101, 9986, 9978,
			29107, 20874, 29119, 20870, 20878, 29125, 29122, 29113,
			20880, 20876, 20872, 20882, 29110, 3226, 10138, 29116,
			29104, 10034, 29235, 31671, 10088, 10147, 29234, 22204,
			21126, 10020, 29204, 29186, 31261, 29238, 10018, 29198,
			29180, 28487, 303, 29299, 29295, 10016, 29201, 29183,
			9996, 9994, 10069, 10125, 10071, 10087, 10132, 29168,
			29177, 28890, 29210, 29192, 29237, 31635, 31632, 31629,
			31626, 10149, 29225, 1939, 29222, 10144, 29317, 10097,
			10101, 10093, 21521, 10029, 28834, 29259, 29261, 29277,
			596, 29227, 21518, 10055, 10053, 10090, 22198, 11240
		));
	}

	private static void addConstruction(Map<String, List<Integer>> m)
	{
		// CONSTRUCTION — 393 items
		//   LLM classified (393)
		m.put(TAG_CONSTRUCTION, Arrays.asList(
			// === LLM classified ===
			7979, 22159, 22163, 22165, 22167, 22173, 22175, 22177,
			22181, 22187, 4823, 23077, 24880, 29774, 21804, 20611,
			33015, 33122, 20615, 32102, 29619, 8638, 7995, 8520,
			8455, 8453, 8417, 8451, 8429, 8425, 8459, 8419,
			8421, 8431, 8433, 8435, 8461, 8457, 8423, 8427,
			8650, 8652, 8654, 8656, 8658, 8660, 8662, 8664,
			8666, 8668, 8670, 8672, 8674, 8676, 8678, 8680,
			32087, 7977, 8516, 7742, 7989, 25559, 7993, 7991,
			4821, 8790, 8510, 4819, 1783, 32053, 24878, 24872,
			24874, 24876, 8566, 9853, 8552, 8570, 9855, 8556,
			8636, 8526, 7752, 8518, 434, 8792, 7976, 26266,
			9789, 9791, 8463, 7975, 8496, 8624, 8628, 9625,
			9626, 7730, 7733, 7736, 22710, 10977, 24287, 8538,
			33368, 33362, 7999, 7748, 8524, 31406, 8536, 22595,
			7996, 7728, 31032, 20609, 8445, 8604, 12854, 8586,
			7690, 7681, 8574, 9846, 8594, 8608, 8588, 9857,
			8622, 8630, 25316, 8784, 31412, 31024, 31027, 7746,
			8522, 31205, 31208, 32106, 2347, 31466, 24885, 7691,
			25644, 32093, 4820, 32056, 32907, 31979, 8000, 23064,
			22106, 12007, 23525, 25521, 12936, 8001, 7980, 7997,
			7688, 8528, 7981, 22671, 7978, 32074, 32077, 32071,
			8006, 8580, 32065, 8642, 32080, 8584, 32068, 32062,
			3211, 3420, 1511, 10976, 8002, 8634, 20613, 9864,
			9848, 8788, 24977, 8508, 9861, 8572, 8514, 9845,
			8546, 8606, 8544, 9867, 32050, 6332, 9856, 8782,
			31973, 8558, 8648, 9851, 8620, 21036, 8786, 9847,
			9858, 24940, 13280, 8005, 32110, 7998, 4822, 1775,
			7750, 8003, 8439, 8504, 9859, 8578, 8564, 8512,
			9843, 8502, 8590, 8550, 8612, 8600, 9865, 32044,
			8530, 8534, 1521, 9852, 8778, 31967, 8598, 8644,
			9849, 9862, 8614, 8560, 8632, 970, 6955, 960,
			24882, 7732, 7735, 7692, 7704, 7716, 7679, 30557,
			30459, 30461, 30554, 30560, 30563, 31964, 8500, 32059,
			32910, 31982, 8468, 8472, 8474, 8476, 8478, 8480,
			8482, 8486, 8492, 8494, 8720, 8736, 8742, 4824,
			7758, 25612, 32099, 32113, 32108, 8794, 9468, 32085,
			32083, 28628, 11740, 7974, 32090, 8596, 8610, 28529,
			964, 8441, 8004, 8640, 1761, 12009, 12010, 8682,
			8686, 8688, 8690, 8692, 8694, 8696, 8698, 8700,
			8702, 8708, 8752, 8756, 8760, 8762, 8764, 8772,
			8774, 8776, 1539, 7986, 7984, 7990, 25561, 7994,
			7992, 7983, 7982, 31410, 31414, 31422, 23079, 7987,
			7988, 22673, 7985, 31418, 31426, 31430, 28674, 24884,
			8449, 8447, 7738, 8506, 9860, 8582, 9844, 8592,
			8542, 8568, 8616, 8602, 8540, 9866, 32047, 8532,
			6333, 9854, 8780, 31970, 8554, 8646, 9850, 9863,
			8618, 7702, 7700, 6281, 8437, 8837, 8443, 7771,
			7763, 7767, 7759, 25093, 28708, 25096, 24463, 21562,
			21909, 10508, 8548, 8576, 8562, 8498, 32041, 7676,
			32104
		));
	}

	private static void addMisc(Map<String, List<Integer>> m)
	{
		// MISC — 2594 items
		//   LLM classified (2594)
		m.put(TAG_MISC, Arrays.asList(
			// === LLM classified ===
			8858, 8859, 27183, 27201, 27200, 27199, 20577, 20576,
			21770, 24051, 9596, 29408, 1507, 22039, 11737, 11736,
			11735, 11734, 27861, 28080, 20405, 30682, 19476, 26252,
			20525, 20582, 29458, 13012, 13014, 29972, 6561, 20599,
			20598, 23653, 11688, 11715, 7922, 31084, 30149, 9477,
			19707, 23640, 1704, 20586, 10354, 1706, 1708, 1710,
			1712, 11976, 11978, 20585, 26914, 30376, 27173, 24880,
			25518, 27194, 27193, 25690, 25694, 11180, 21631, 22302,
			21807, 25686, 27184, 24201, 27369, 25688, 20430, 22299,
			21668, 11341, 11342, 11343, 11345, 11346, 11347, 11348,
			12621, 12622, 12623, 12624, 22305, 19677, 20431, 21813,
			21046, 30966, 21810, 25692, 21670, 25938, 25941, 25944,
			25950, 25953, 12775, 11710, 25760, 24565, 11137, 11189,
			13145, 13146, 13147, 13148, 21640, 25920, 25921, 25922,
			25923, 25924, 25925, 28800, 32871, 29323, 19631, 27304,
			29971, 22508, 33202, 13578, 19613, 21629, 12746, 12747,
			1464, 13121, 13122, 13123, 13124, 20760, 8011, 1505,
			23611, 20593, 22665, 24192, 12617, 12618, 12619, 12620,
			13052, 13054, 11176, 30393, 11699, 29852, 10516, 10556,
			33172, 28334, 24442, 31454, 24263, 4049, 25202, 25730,
			24195, 12613, 12614, 12615, 12616, 13058, 23646, 20594,
			13302, 30361, 28767, 33221, 11340, 24955, 22230, 455,
			27855, 28074, 4053, 25209, 25637, 23593, 27112, 19629,
			29271, 22949, 1852, 23107, 684, 27169, 23595, 27221,
			8976, 11067, 25463, 20423, 20424, 25493, 25494, 3463,
			3461, 3462, 3464, 3460, 25448, 1666, 1647, 12988,
			12990, 23642, 24607, 24592, 24609, 24613, 24595, 24589,
			24611, 24605, 24603, 24601, 24598, 26705, 24615, 24621,
			13307, 29846, 29848, 29847, 11697, 11714, 20608, 24055,
			20526, 25476, 25195, 10533, 8936, 25212, 8943, 29843,
			29845, 29849, 29844, 21312, 22229, 11691, 9433, 25139,
			605, 25199, 8014, 8015, 6767, 13153, 13159, 5508,
			11640, 13157, 13155, 7464, 3667, 31875, 31890, 31841,
			31877, 22999, 9011, 33091, 24329, 28082, 28084, 28086,
			28088, 28090, 28092, 28094, 28096, 28098, 28017, 30576,
			30619, 30616, 7686, 27187, 8974, 8972, 7671, 24941,
			7157, 7773, 983, 24204, 8989, 8990, 8979, 11745,
			29482, 23083, 21724, 494, 498, 30688, 25457, 7540,
			7541, 4614, 4496, 11700, 508, 25459, 8867, 19566,
			3453, 3451, 3452, 3454, 3450, 25442, 12960, 1925,
			8986, 9660, 1929, 9659, 10835, 22227, 22424, 21175,
			21173, 21171, 21166, 33200, 33237, 13531, 22519, 27293,
			26755, 26749, 29090, 8010, 6707, 10, 12, 8,
			12776, 21716, 405, 25590, 2714, 2715, 30694, 30696,
			11083, 11081, 11079, 30690, 4067, 25162, 25161, 25160,
			25159, 4514, 4516, 4513, 4515, 4055, 20523, 24961,
			20885, 4184, 1840, 3136, 3137, 19627, 26721, 694,
			2842, 22320, 12694, 11694, 11712, 20238, 2576, 30775,
			432, 2404, 29923, 1755, 7004, 2884, 13660, 30002,
			26969, 28824, 25721, 12659, 3188, 20578, 4047, 23129,
			13648, 13651, 13650, 13649, 12789, 30363, 33235, 23442,
			20358, 20364, 20362, 20360, 23127, 19712, 19718, 19716,
			19714, 713, 23182, 2677, 2678, 2679, 2680, 2681,
			2682, 2683, 2684, 2685, 2686, 2687, 2688, 2689,
			2690, 2691, 2692, 2693, 2694, 2695, 2696, 2697,
			2698, 2699, 2700, 2701, 2702, 2703, 2704, 2705,
			2706, 2707, 2708, 2709, 2710, 2711, 2712, 2713,
			2716, 2719, 3490, 3491, 3492, 3493, 3494, 3495,
			3496, 3497, 3498, 3499, 3500, 3501, 3502, 3503,
			3504, 3505, 3506, 3507, 3508, 3509, 3510, 3512,
			3513, 3514, 3515, 3516, 3518, 7236, 7238, 10180,
			10182, 10184, 10186, 10188, 10190, 10192, 10194, 10196,
			10198, 10200, 10202, 10204, 10206, 10208, 10210, 10212,
			10214, 10216, 10218, 10220, 10222, 10224, 10226, 10228,
			10230, 10232, 12162, 12164, 12166, 12167, 12168, 12169,
			12170, 12172, 12173, 12174, 12175, 12176, 12177, 12178,
			12179, 12181, 12182, 12183, 12184, 12185, 12186, 12187,
			12188, 12189, 12190, 12191, 12192, 19814, 19816, 19817,
			19818, 19819, 19820, 19821, 19822, 19823, 19824, 19825,
			19826, 19828, 19829, 19830, 19831, 19833, 22001, 23149,
			23150, 23151, 23152, 23153, 23154, 23155, 23156, 23157,
			23158, 23159, 23160, 23161, 23162, 23163, 23164, 23165,
			23166, 25788, 25789, 28913, 28914, 29853, 29854, 30928,
			31268, 12073, 12074, 12075, 12076, 12077, 12078, 12079,
			12080, 12081, 12082, 12083, 12085, 12086, 12087, 12088,
			12089, 12090, 12091, 12092, 12093, 12094, 12095, 12096,
			12097, 12098, 12099, 12100, 12101, 12102, 12103, 12104,
			12105, 12106, 12107, 12108, 12109, 12110, 12111, 12127,
			12130, 12132, 12133, 12134, 12135, 12136, 12137, 12138,
			12140, 12141, 12142, 12143, 12144, 12145, 12146, 12147,
			12148, 12149, 12150, 12151, 12152, 12153, 12154, 12155,
			12156, 12157, 12158, 12159, 19782, 19783, 19784, 19785,
			19786, 19787, 19788, 19789, 19790, 19791, 19792, 19793,
			19794, 19795, 19796, 19797, 19798, 19799, 19800, 19801,
			19802, 19803, 19804, 19805, 19806, 19807, 19808, 19809,
			19810, 19811, 19813, 21524, 21525, 22000, 23144, 23145,
			23146, 23147, 23148, 23770, 24253, 24773, 25498, 25499,
			25786, 25787, 26943, 26944, 28910, 28911, 28912, 29855,
			29856, 30932, 31269, 31270, 31271, 2722, 2723, 2725,
			2727, 2729, 2731, 2733, 2735, 2737, 2739, 2741,
			2743, 2745, 2747, 2773, 2774, 2776, 2778, 2780,
			2782, 2783, 2785, 2786, 2788, 2790, 2792, 2793,
			2794, 2796, 2797, 2799, 3520, 3522, 3524, 3525,
			3526, 3528, 3530, 3532, 3534, 3536, 3538, 3540,
			3542, 3544, 3546, 3548, 3550, 3552, 3554, 3556,
			3558, 3560, 3562, 3564, 3566, 3568, 3570, 3572,
			3573, 3574, 3575, 3577, 3579, 3580, 7239, 7241,
			7243, 7245, 7247, 7248, 7249, 7250, 7251, 7252,
			7253, 7254, 7255, 7256, 7258, 7260, 7262, 7264,
			7266, 7268, 7270, 7272, 10234, 10236, 10238, 10240,
			10242, 10244, 10246, 10248, 10250, 10252, 12542, 12544,
			12546, 12548, 12550, 12552, 12554, 12556, 12558, 12560,
			12562, 12564, 12566, 12568, 12570, 12572, 12574, 12576,
			12578, 12581, 12584, 12587, 12590, 19840, 19842, 19844,
			19846, 19848, 19850, 19852, 19853, 19854, 19856, 19857,
			19858, 19860, 19862, 19864, 19866, 19868, 19870, 19872,
			19874, 19876, 19878, 19880, 19882, 19884, 19886, 19888,
			19890, 19892, 19894, 19896, 19898, 19900, 19902, 19904,
			19906, 19908, 19910, 21526, 21527, 23045, 23167, 23168,
			23169, 23170, 23172, 23174, 23175, 23176, 23177, 23178,
			23179, 23180, 23181, 24493, 25790, 25791, 25792, 26566,
			28915, 28916, 28918, 29859, 30929, 30931, 31272, 31273,
			19835, 2801, 2803, 2805, 2807, 2809, 2811, 2813,
			2815, 2817, 2819, 2821, 2823, 2825, 2827, 2829,
			2831, 2833, 2835, 2837, 2839, 2841, 2843, 2845,
			2847, 2848, 2849, 2851, 2853, 2855, 2856, 2857,
			2858, 3582, 3584, 3586, 3588, 3590, 3592, 3594,
			3596, 3598, 3599, 3601, 3602, 3604, 3605, 3607,
			3609, 3610, 3611, 3612, 3613, 3614, 3615, 3616,
			3617, 3618, 7274, 7276, 7278, 7280, 7282, 7284,
			7286, 7288, 7290, 7292, 7294, 7296, 7298, 7300,
			7301, 7303, 7304, 7305, 7307, 7309, 7311, 7313,
			7315, 7317, 10254, 10256, 10258, 10260, 10262, 10264,
			10266, 10268, 10270, 10272, 10274, 10276, 10278, 12021,
			12023, 12025, 12027, 12029, 12031, 12033, 12035, 12037,
			12039, 12041, 12043, 12045, 12047, 12049, 12051, 12053,
			12055, 12057, 12059, 12061, 12063, 12065, 12067, 12069,
			12071, 19734, 19736, 19738, 19740, 19742, 19744, 19746,
			19748, 19750, 19752, 19754, 19756, 19758, 19760, 19762,
			19764, 19766, 19768, 19770, 19772, 19774, 19776, 19778,
			19780, 23046, 23131, 23133, 23135, 23136, 23137, 23138,
			23139, 23140, 23141, 23142, 23143, 25783, 25784, 28907,
			28908, 28909, 29857, 29858, 30933, 30935, 31274, 31275,
			27427, 7587, 7588, 7589, 7591, 22521, 617, 995,
			6964, 8890, 10521, 22711, 10560, 10557, 30040, 25956,
			11126, 11124, 11122, 11120, 11118, 11974, 11972, 24130,
			24131, 19568, 31212, 30816, 30831, 30819, 30840, 30822,
			30810, 30837, 30825, 30813, 30828, 30834, 23104, 23103,
			23533, 23821, 23843, 28534, 23830, 25959, 23850, 23849,
			23823, 23846, 27847, 27848, 27849, 25958, 23822, 23820,
			23824, 23831, 27844, 27845, 27846, 23858, 27842, 27843,
			28531, 29602, 27850, 27852, 27851, 11696, 33103, 674,
			10513, 31214, 24049, 20886, 27308, 4707, 22677, 23862,
			30384, 33166, 25496, 30340, 23867, 23895, 23897, 23864,
			33170, 25495, 989, 33168, 25497, 25104, 8628, 23863,
			23861, 23962, 23866, 23868, 6103, 23953, 4077, 3063,
			712, 22366, 25571, 12777, 22778, 20408, 27853, 28072,
			20899, 25244, 13514, 13515, 13516, 13518, 13519, 13520,
			13521, 21027, 19685, 7684, 22516, 22330, 13666, 24190,
			33065, 21710, 11692, 11713, 24523, 4069, 4070, 4504,
			4505, 4509, 4510, 11893, 11894, 11895, 11896, 11897,
			11898, 11899, 11900, 11901, 25163, 25167, 25171, 24287,
			25165, 25169, 25174, 4071, 4506, 4511, 4072, 4507,
			4512, 4068, 4503, 4508, 22426, 10538, 10558, 31111,
			13134, 13135, 13136, 1837, 30394, 1835, 1833, 6384,
			6388, 25516, 23639, 25515, 23633, 23649, 11190, 11191,
			11192, 11194, 12403, 31132, 981, 10829, 9654, 10972,
			30803, 21885, 20389, 20784, 20407, 23597, 11256, 23648,
			27157, 27859, 28078, 27857, 28076, 20406, 33186, 22400,
			772, 19615, 29850, 32865, 24545, 30328, 1590, 5054,
			12863, 27426, 11689, 11717, 6798, 6075, 13526, 24065,
			25114, 25112, 29851, 31142, 29840, 29842, 29841, 4278,
			4251, 11942, 26388, 23882, 23883, 23884, 23885, 26571,
			25648, 27176, 27175, 27174, 21205, 20524, 8626, 33176,
			26759, 26753, 26471, 22422, 23943, 12819, 6896, 30378,
			7960, 22660, 6675, 409, 7921, 8019, 8020, 8021,
			8018, 8016, 3690, 3691, 6125, 6126, 13079, 23458,
			731, 23951, 23959, 25859, 29273, 11904, 22517, 25961,
			27997, 23644, 23946, 31211, 30997, 33227, 21672, 29973,
			11677, 13125, 13126, 13127, 13128, 22418, 4045, 25211,
			20791, 25106, 4008, 4009, 25102, 8009, 27219, 1411,
			11525, 12404, 19621, 31744, 22228, 4653, 10566, 11686,
			11718, 25582, 272, 24959, 6202, 8864, 12854, 21180,
			3678, 5559, 33239, 6666, 956, 30357, 30763, 6663,
			29961, 21594, 27216, 21596, 21598, 21590, 21592, 8863,
			12955, 30395, 27177, 13129, 13130, 13131, 13132, 27462,
			27622, 26356, 28333, 29895, 6814, 7681, 3867, 3865,
			3863, 3859, 3857, 3855, 3853, 13394, 31216, 5562,
			5563, 5564, 5566, 5567, 33384, 33393, 33387, 33390,
			27552, 25926, 25928, 25930, 25932, 25934, 25936, 27544,
			12778, 27166, 27167, 27168, 6799, 23628, 3901, 6800,
			20754, 30638, 30637, 28765, 751, 20275, 10999, 6801,
			601, 11060, 25467, 25430, 25426, 25428, 25432, 25424,
			4692, 25454, 10881, 9040, 9028, 9034, 24059, 27225,
			9469, 12849, 20557, 9415, 24418, 10531, 9057, 10878,
			13420, 8865, 27042, 23499, 23875, 6677, 6678, 23638,
			13050, 27180, 12639, 27163, 3835, 3836, 24217, 29684,
			1413, 24709, 24711, 24946, 24529, 19625, 21678, 9595,
			10526, 10559, 10546, 10545, 10544, 10543, 23630, 19941,
			23591, 11738, 13226, 27302, 6189, 979, 21766, 6802,
			11996, 12833, 7682, 21624, 13528, 19651, 20897, 29244,
			29246, 29248, 30644, 29309, 22971, 1580, 24963, 6900,
			22599, 20283, 20284, 20285, 20287, 20288, 20289, 20290,
			20307, 20308, 20309, 20311, 20312, 20313, 20314, 20331,
			20332, 20333, 20335, 20336, 20337, 20338, 23418, 23419,
			23420, 23422, 23423, 23424, 23425, 13532, 22043, 23603,
			23607, 23907, 23605, 25644, 6803, 10028, 10027, 21297,
			23622, 27170, 31345, 31347, 11088, 27195, 27196, 27198,
			27197, 5, 13395, 26908, 12410, 11701, 8869, 11721,
			12972, 12974, 3899, 9026, 23670, 1591, 1417, 11177,
			21768, 6804, 1992, 12656, 30396, 13139, 13140, 11140,
			13103, 3165, 23632, 275, 293, 298, 423, 1543,
			1544, 1545, 1546, 1547, 1548, 2411, 5010, 9722,
			11039, 30951, 2832, 13249, 13248, 20884, 21760, 4591,
			12779, 24957, 13527, 33218, 946, 11842, 23626, 26741,
			26739, 26737, 28790, 30400, 12693, 2528, 24528, 21600,
			21606, 21604, 21608, 21602, 29303, 29297, 9651, 21540,
			23490, 12780, 11695, 25117, 22287, 9008, 3177, 6808,
			30359, 6805, 31071, 31073, 27188, 20355, 25975, 27870,
			3848, 27341, 27339, 34, 13391, 28140, 7777, 987,
			30107, 26651, 11941, 24585, 19732, 8932, 12642, 8008,
			12405, 21762, 22045, 23652, 5614, 11711, 12786, 33180,
			33007, 30904, 30908, 30920, 30916, 30924, 30912, 5606,
			11932, 11933, 26745, 26747, 26743, 33233, 33190, 33192,
			21387, 13280, 26884, 21580, 21586, 21584, 21588, 21582,
			29301, 21656, 22475, 1839, 29955, 23184, 30926, 19617,
			11690, 19668, 5020, 28769, 30902, 30906, 30918, 30914,
			30922, 30910, 9475, 27296, 11703, 20581, 27110, 11720,
			299, 24534, 13000, 13002, 31099, 7416, 7418, 8942,
			4010, 4034, 4018, 30109, 9078, 24949, 30035, 27836,
			23619, 27916, 27837, 27838, 27912, 2974, 2972, 12406,
			30397, 13112, 13113, 13114, 13115, 12411, 22374, 32864,
			32869, 26154, 4181, 991, 11184, 21676, 11339, 33005,
			24277, 24763, 24765, 24767, 24771, 6199, 5561, 20426,
			27159, 27161, 20425, 27158, 27160, 24855, 12402, 30122,
			30113, 30390, 30392, 27172, 21155, 21153, 21151, 21146,
			27297, 6104, 550, 11169, 11171, 24061, 20888, 10563,
			29098, 31343, 33178, 796, 1685, 10860, 10861, 21555,
			30744, 23654, 22364, 13195, 11929, 4850, 12935, 685,
			11183, 11179, 22428, 31121, 5506, 25829, 23023, 23025,
			23027, 23031, 23033, 23035, 13190, 11182, 695, 21680,
			10537, 24053, 8860, 27192, 27693, 25650, 24229, 26602,
			33174, 24951, 26886, 20993, 20996, 11733, 11732, 11731,
			11730, 13187, 12781, 7756, 31399, 970, 972, 21674,
			619, 620, 21666, 29893, 6901, 10996, 29275, 33120,
			31817, 12407, 233, 23865, 9044, 9046, 9048, 9050,
			13074, 13075, 13076, 13077, 26948, 26945, 466, 8951,
			8930, 8949, 8950, 12408, 26629, 26577, 24882, 13204,
			30365, 197, 10535, 10541, 10539, 10540, 4495, 11743,
			26549, 26887, 26894, 26895, 26892, 26896, 26893, 26889,
			26890, 26898, 26891, 26897, 26888, 195, 25206, 25205,
			25204, 25203, 11178, 9032, 9036, 20396, 20395, 20394,
			20393, 23771, 6966, 33133, 7678, 6319, 6885, 4238,
			26247, 33184, 21313, 10476, 10946, 2795, 10562, 29307,
			22941, 22943, 22945, 22947, 13524, 13533, 30402, 24861,
			27179, 23557, 23555, 23553, 23551, 6200, 11704, 29996,
			25207, 10532, 7329, 8938, 9054, 25228, 8946, 10879,
			21826, 21832, 21829, 11744, 21802, 21820, 23245, 20546,
			20543, 20544, 19836, 20545, 7774, 9474, 10934, 13525,
			9007, 11741, 2566, 2564, 2562, 2558, 2556, 2554,
			2552, 24735, 2570, 21138, 21136, 21134, 21129, 28329,
			26815, 2572, 11988, 11986, 11984, 11980, 12785, 20786,
			12783, 1480, 4043, 675, 2203, 7636, 5558, 21307,
			954, 20587, 22321, 22437, 13205, 2959, 5733, 22541,
			2518, 30890, 19564, 33010, 8940, 8941, 10947, 13024,
			13026, 20607, 23601, 27185, 27111, 6897, 11719, 20422,
			23650, 27086, 30692, 24587, 10882, 9012, 25478, 10552,
			10549, 32867, 26960, 32863, 6721, 686, 31016, 28773,
			26731, 26735, 26733, 32399, 19619, 23565, 23563, 23561,
			23559, 4037, 23581, 23579, 23577, 23575, 27182, 4041,
			13163, 27165, 3827, 3828, 3829, 3830, 13256, 12804,
			27306, 24441, 27214, 28332, 8977, 8934, 28958, 21664,
			10512, 24361, 24362, 24365, 24364, 24366, 24363, 26706,
			11740, 28798, 22664, 12842, 28771, 6679, 6680, 21772,
			9003, 13639, 3745, 23624, 12782, 22047, 22504, 24198,
			31756, 32926, 2574, 1848, 1854, 20390, 25831, 13382,
			13563, 4186, 24299, 621, 3680, 21682, 7778, 8857,
			6097, 26132, 28493, 29679, 33058, 26009, 26066, 33056,
			25994, 26012, 26099, 33062, 26018, 26069, 33060, 26057,
			28520, 26126, 26006, 26015, 26105, 28481, 26144, 29649,
			26054, 26096, 29667, 26129, 28514, 26123, 26081, 26135,
			26141, 25991, 28490, 29670, 29652, 26108, 26060, 26042,
			26021, 26120, 28478, 26039, 29664, 28484, 28517, 26090,
			28511, 26036, 26084, 26087, 26048, 26075, 28496, 26102,
			25997, 26111, 26093, 33054, 26147, 29676, 28502, 28505,
			28508, 26078, 26003, 28526, 26063, 26045, 29655, 26000,
			26072, 26117, 26138, 29673, 26051, 28499, 28523, 26114,
			28529, 33135, 25465, 8868, 3468, 3466, 3467, 3469,
			3465, 25451, 25646, 993, 28331, 10948, 6806, 11113,
			11111, 11109, 11107, 11970, 11968, 965, 9013, 21276,
			21268, 25837, 2749, 2750, 2751, 2753, 2754, 2755,
			2756, 3619, 3620, 3621, 3623, 3624, 3625, 3626,
			3643, 3644, 3645, 3647, 3648, 3649, 3650, 21570,
			21576, 21574, 21578, 21572, 29299, 32416, 32418, 32420,
			32419, 32415, 32413, 32412, 32411, 32414, 29295, 31351,
			733, 20798, 13233, 7800, 24063, 19634, 25201, 19637,
			33231, 25197, 952, 5327, 20397, 27797, 21712, 12823,
			3273, 29782, 27178, 10536, 10561, 23599, 25342, 23613,
			24067, 23589, 12629, 23587, 12627, 23585, 12625, 23583,
			10949, 33063, 27833, 27834, 27835, 23620, 27908, 11702,
			25461, 8866, 3458, 3456, 3457, 3459, 3455, 4446,
			25445, 12984, 12986, 30679, 9030, 9042, 9038, 22991,
			30953, 22601, 3893, 5732, 31732, 5507, 3062, 23183,
			464, 24761, 9009, 29535, 28330, 26885, 9004, 31441,
			29409, 28924, 30386, 23549, 23547, 23545, 23543, 20551,
			20550, 20549, 20548, 11729, 11728, 11727, 11726, 23573,
			23571, 23569, 23567, 26757, 26751, 27314, 20703, 20527,
			1941, 8981, 24219, 3231, 25580, 12409, 3805, 24336,
			12846, 6083, 23510, 23514, 11742, 10859, 20890, 8022,
			29455, 13658, 23904, 6102, 6101, 6100, 13102, 31443,
			8013, 799, 23502, 6285, 6283, 25567, 24057, 21764,
			6079, 30123, 27312, 24071, 22510, 24069, 22416, 27310,
			22512, 30382, 8862, 945, 966, 5568, 30398, 10514,
			29388, 31123, 6529, 27358, 4051, 985, 30105, 9010,
			23637, 23634, 595, 8987, 27171, 29580, 19837, 33194,
			4185, 6306, 21718, 13396, 20893, 33229, 13537, 13536,
			26605, 26609, 26607, 7677, 25474, 30689, 28567, 33044,
			28564, 33047, 28570, 28561, 13535, 11747, 24187, 13530,
			33188, 8861, 33182, 26882, 11175, 27191, 13151, 26547,
			26544, 26548, 26545, 26546, 21566, 21564, 21568, 21562,
			13273, 22191, 9662, 31054, 20895, 30401, 28972, 8007,
			710, 26932, 21872, 29809, 27190, 27189, 23636, 23635,
			22514, 24617, 27831, 23615, 27904, 27832, 27900, 23839,
			23879, 13348, 33198, 33196, 8842, 26467, 26465, 8839,
			26463, 11673, 11672, 11671, 11669, 11668, 11667, 11666,
			27869, 27684, 27681, 25517, 21541, 30685, 23106, 1415,
			8851, 24859, 2575, 8012, 27295, 11687, 11716, 24953,
			1829, 1827, 1825, 1823, 23387, 26883, 23834, 23871,
			1465, 19623, 13142, 13143, 13144, 2885, 24251, 30399,
			13110, 13111, 10851, 13529, 21863, 27162, 943, 10515,
			6084, 22208, 26723, 26727, 1843, 20981, 20984, 20977,
			20979, 13393, 29969, 11746, 1765, 10534, 26576, 13046,
			4039, 27181, 4042, 12638, 27164, 3831, 3832, 3833,
			3834, 27186, 6807, 29449, 12938, 27839, 27841, 27840,
			23617, 27920
		));
	}

	private static void addQuests(Map<String, List<Integer>> m)
	{
		// QUESTS — 2141 items
		//   LLM classified (2141)
		m.put(TAG_QUESTS, Arrays.asList(
			// === LLM classified ===
			2365, 446, 774, 773, 3731, 3726, 4035, 3730,
			3725, 3724, 3723, 22764, 22763, 5589, 6122, 10495,
			21770, 9627, 10840, 6949, 10494, 718, 1508, 1507,
			9103, 2888, 24842, 2969, 26905, 28461, 33118, 4178,
			30989, 31365, 5578, 2862, 19476, 7459, 11680, 1858,
			29539, 29552, 28448, 9693, 4436, 22086, 22033, 27300,
			30973, 5588, 3270, 5767, 4, 701, 1478, 1498,
			1499, 1497, 11003, 1842, 6792, 24688, 23071, 4028,
			27607, 22093, 27369, 28409, 22775, 11061, 27597, 30963,
			20611, 11181, 681, 30966, 7807, 7808, 7809, 7806,
			28369, 28375, 671, 4428, 1816, 1540, 4447, 6543,
			7498, 11679, 21262, 23072, 23082, 25753, 25820, 27299,
			27543, 30960, 6094, 10841, 708, 28345, 28346, 28351,
			28355, 23785, 23787, 23789, 23791, 1505, 1528, 738,
			28385, 30955, 29517, 87, 11048, 4195, 30320, 11049,
			7995, 7508, 4489, 28456, 11156, 600, 29542, 9932,
			10499, 10498, 28334, 3894, 11050, 26958, 7839, 29551,
			2407, 6793, 9933, 7564, 25730, 29884, 21060, 12615,
			10489, 11323, 783, 1841, 3216, 3218, 3220, 3221,
			10887, 28390, 7462, 30311, 30310, 4574, 28364, 28378,
			7899, 7833, 2886, 2887, 11014, 616, 7973, 7815,
			4027, 4502, 9717, 1852, 4284, 6123, 6946, 10895,
			28295, 6947, 624, 29905, 3230, 7908, 730, 10870,
			10173, 422, 29931, 1474, 38, 21, 23782, 6752,
			6750, 4622, 7457, 9055, 11678, 4620, 4808, 10880,
			6650, 28357, 1582, 1581, 9692, 9694, 9698, 9696,
			9690, 10491, 722, 10889, 4659, 2954, 6711, 6710,
			24699, 24691, 24697, 2875, 4670, 28453, 22407, 9650,
			25147, 25799, 6064, 9613, 22, 6644, 23780, 6086,
			287, 740, 23792, 9616, 9615, 3719, 6089, 9614,
			25803, 4415, 668, 667, 1817, 9945, 9944, 25794,
			618, 21530, 7813, 605, 4272, 7950, 604, 13116,
			7918, 757, 1509, 29878, 7144, 4829, 3844, 12612,
			4248, 12610, 4817, 19515, 25818, 12608, 292, 711,
			5065, 4569, 4570, 4571, 28804, 26965, 28805, 6953,
			6818, 10894, 3692, 30945, 739, 7515, 28970, 4442,
			29519, 7146, 9591, 7540, 6081, 690, 1469, 25811,
			763, 4431, 10177, 7454, 2418, 5585, 19566, 27418,
			5602, 5008, 7491, 7490, 7489, 7488, 7473, 7474,
			7475, 7476, 19517, 7622, 4693, 4687, 6712, 30,
			10865, 10863, 10864, 28968, 716, 7961, 7531, 7520,
			3127, 7570, 29912, 29914, 29915, 688, 4209, 753,
			756, 7542, 7001, 33008, 22084, 6707, 7118, 7149,
			4579, 7119, 7147, 7145, 10, 6, 12, 8,
			4678, 30312, 30950, 1052, 2405, 1818, 29922, 7956,
			9646, 9647, 9648, 6766, 4677, 6544, 278, 7905,
			4184, 1840, 29534, 3136, 3137, 769, 3114, 21759,
			21775, 22365, 22367, 22760, 22777, 22414, 3706, 777,
			973, 28403, 19560, 29428, 707, 6759, 26955, 432,
			709, 2404, 4273, 28573, 5609, 4443, 90, 5601,
			28414, 6545, 741, 7472, 26969, 26967, 9644, 9640,
			9642, 25145, 11195, 26286, 23794, 7451, 3105, 10594,
			10595, 23814, 23815, 23816, 28425, 30316, 30941, 25804,
			26906, 6638, 10874, 19569, 29545, 10586, 19567, 19568,
			3102, 4426, 6635, 9681, 27532, 29883, 4201, 4200,
			31341, 4205, 30819, 30822, 29217, 7530, 7521, 7568,
			775, 21263, 7463, 7470, 4488, 23844, 23832, 23842,
			23848, 23833, 23837, 28455, 33103, 10593, 7537, 7519,
			3161, 1538, 9720, 9718, 10893, 7630, 32808, 32807,
			10899, 8871, 780, 1813, 1808, 28462, 26959, 10500,
			11032, 11033, 608, 25963, 28133, 2380, 23802, 23889,
			23891, 23983, 28577, 28575, 23804, 23894, 23870, 23877,
			3208, 9625, 9626, 23808, 4313, 6653, 4077, 25800,
			1978, 4245, 4838, 5584, 5577, 26962, 3702, 6643,
			23779, 11158, 793, 7857, 11001, 3839, 3841, 3843,
			12611, 2387, 19636, 25810, 11031, 1467, 28439, 746,
			25815, 29566, 29570, 29568, 6746, 1819, 27368, 2529,
			9683, 4197, 10842, 25814, 30317, 19562, 29544, 25796,
			6457, 31111, 21799, 25798, 6748, 6747, 6749, 4611,
			7902, 1835, 1833, 28393, 28419, 27783, 2408, 3395,
			3846, 22761, 4430, 6770, 7497, 27515, 3267, 29521,
			4617, 420, 7535, 6547, 1492, 1573, 2409, 9654,
			9682, 9589, 9590, 28410, 30968, 11217, 7461, 3204,
			4811, 22087, 22092, 22088, 6967, 4180, 1187, 7478,
			33006, 28420, 22400, 771, 772, 7408, 9067, 11154,
			11151, 11157, 3263, 29925, 2957, 2968, 3266, 1468,
			27600, 29596, 28458, 28132, 25706, 7629, 27595, 1501,
			0, 5056, 5060, 11200, 4568, 7509, 2126, 7514,
			10171, 10167, 29553, 28445, 9695, 6077, 4251, 6649,
			30970, 7435, 26571, 23798, 23800, 27223, 9729, 2893,
			2892, 2890, 7996, 28809, 30307, 29560, 29562, 29564,
			1820, 11279, 19559, 4686, 21054, 28417, 20722, 9066,
			22433, 29868, 29872, 29870, 29874, 30949, 3727, 3732,
			10486, 7496, 10831, 9085, 4007, 524, 522, 525,
			28965, 7544, 22435, 7546, 6754, 3690, 3691, 6125,
			6126, 13079, 7545, 21260, 523, 24695, 21259, 21800,
			28964, 22768, 29540, 7967, 29530, 29538, 21798, 4654,
			415, 28437, 7477, 35, 28319, 3698, 7893, 13128,
			23818, 5064, 5067, 4191, 26903, 27285, 10172, 4593,
			3268, 13117, 13118, 13119, 13120, 782, 24254, 27219,
			9903, 10179, 6718, 31379, 2384, 29554, 1583, 7872,
			28447, 9699, 3708, 6673, 7534, 29549, 27, 26,
			31357, 6082, 22413, 1554, 1811, 31328, 31327, 21662,
			5608, 28983, 6646, 23784, 9687, 9688, 9689, 1821,
			3699, 28551, 3758, 7538, 7942, 3728, 3733, 3741,
			26356, 26358, 26360, 26364, 26362, 3736, 7441, 3722,
			11007, 3729, 25146, 28981, 7109, 10884, 7668, 4190,
			4189, 1550, 4668, 1506, 22762, 553, 6106, 6111,
			6110, 6107, 6108, 552, 7881, 3901, 7827, 337,
			7518, 3897, 3898, 7824, 4674, 750, 295, 294,
			296, 785, 788, 747, 4075, 589, 4004, 22590,
			601, 288, 26587, 26585, 26583, 26581, 7812, 5009,
			11060, 721, 4567, 27422, 723, 2947, 2950, 10175,
			3693, 11210, 2949, 2944, 2951, 2948, 2946, 3694,
			776, 4624, 3895, 28468, 9018, 9016, 4026, 3261,
			4182, 9901, 17, 5607, 28817, 9609, 6642, 23778,
			23783, 6088, 9612, 23793, 9611, 6092, 9610, 1909,
			25608, 28424, 11196, 1527, 29558, 1533, 1529, 1525,
			1531, 1588, 11155, 2391, 704, 7528, 7527, 7517,
			9594, 3215, 3264, 9082, 28803, 26594, 23835, 9088,
			9090, 1856, 7108, 28442, 30940, 4421, 5579, 9652,
			24672, 4604, 11682, 28408, 4487, 11173, 6639, 19521,
			1504, 10862, 7453, 4001, 9902, 2403, 2406, 786,
			744, 19481, 7959, 10834, 10828, 11052, 4464, 4416,
			29427, 7951, 25968, 7634, 21766, 1494, 727, 728,
			28343, 28344, 28353, 3840, 748, 19, 1718, 4682,
			15, 732, 6714, 26575, 5610, 26957, 742, 29548,
			3696, 3697, 26573, 1502, 1496, 1500, 1409, 12658,
			78, 4671, 30628, 7875, 1580, 29887, 29897, 28361,
			27604, 27608, 1584, 3904, 3906, 3908, 3912, 3914,
			3916, 3918, 3952, 3954, 3956, 3960, 3962, 3964,
			3966, 28464, 28806, 22041, 26366, 28974, 9617, 9620,
			9619, 9618, 22079, 28967, 11013, 28966, 31377, 11088,
			28, 761, 794, 23806, 696, 3207, 3103, 7455,
			2945, 4427, 7941, 6648, 22398, 7914, 30969, 29932,
			22106, 12936, 30893, 27289, 27605, 21768, 4590, 7845,
			2967, 3845, 4203, 6755, 24686, 7810, 19519, 21797,
			6996, 13137, 13138, 3153, 3157, 21394, 3155, 11136,
			11138, 4031, 431, 3164, 6716, 4595, 27511, 10885,
			10898, 3711, 6113, 6112, 6118, 7997, 7516, 10581,
			25979, 275, 293, 298, 423, 1543, 1544, 1545,
			1546, 1547, 1548, 2411, 5010, 9722, 11039, 30951,
			2423, 4589, 30961, 10600, 77, 21760, 4591, 33004,
			75, 76, 74, 21059, 1815, 9094, 3206, 7447,
			1495, 5605, 28413, 30965, 28977, 28384, 25801, 19525,
			19523, 30962, 9655, 6796, 23516, 9724, 30967, 9651,
			6819, 421, 2149, 28460, 28454, 5062, 3707, 24075,
			4196, 602, 4204, 4615, 6121, 6756, 6757, 7966,
			21774, 11009, 691, 692, 693, 83, 27603, 9719,
			28325, 28428, 7972, 10832, 3848, 26961, 4684, 416,
			6479, 28808, 31397, 29573, 3714, 25704, 25809, 611,
			612, 613, 614, 22081, 28415, 7970, 7971, 28986,
			3712, 787, 743, 9102, 9077, 9100, 9101, 24261,
			9099, 9096, 9098, 9076, 9104, 9084, 9091, 9092,
			9093, 9097, 21762, 22045, 3689, 6994, 4020, 4021,
			4022, 11058, 6645, 23781, 11211, 29550, 9019, 9024,
			9023, 9022, 9021, 12932, 9592, 18, 33117, 28587,
			28426, 2395, 7409, 4703, 16, 6950, 2410, 3718,
			5604, 33007, 30318, 27298, 31392, 30944, 28980, 22037,
			21052, 3847, 1535, 1536, 1537, 22009, 22010, 22011,
			22013, 22014, 22015, 22016, 4274, 4275, 4276, 4187,
			3130, 24940, 9089, 30308, 31330, 1542, 7449, 430,
			9725, 3180, 20892, 5066, 24030, 24031, 24032, 24033,
			755, 9633, 9649, 26942, 10876, 10174, 1839, 10873,
			5586, 11197, 33128, 7471, 9733, 30939, 28449, 9697,
			7958, 2966, 7998, 1586, 10597, 10598, 10599, 7458,
			705, 4253, 7635, 7851, 24260, 4033, 19556, 3166,
			4006, 7854, 3167, 4023, 29078, 29079, 13113, 13114,
			13115, 7869, 28969, 26591, 6069, 6070, 6068, 6071,
			6065, 6067, 6541, 29525, 29526, 28401, 4490, 4492,
			7532, 2953, 28341, 28342, 28350, 28352, 6769, 11198,
			33005, 22404, 22402, 22403, 19505, 21261, 23069, 4247,
			22114, 24857, 6095, 3222, 4005, 26572, 28451, 4837,
			25152, 4239, 4237, 4241, 4212, 4213, 4235, 6104,
			6651, 19558, 4024, 4025, 703, 5581, 10833, 4597,
			4598, 21056, 21057, 21058, 29524, 9025, 30946, 31337,
			3, 27601, 6548, 603, 4188, 10178, 28430, 26963,
			7578, 23796, 31400, 2866, 4818, 21837, 2871, 2883,
			4839, 2372, 7842, 2377, 277, 1485, 1585, 31338,
			22411, 1493, 22588, 24682, 22051, 22410, 22774, 9947,
			2385, 28438, 3426, 3424, 28421, 9601, 1769, 286,
			9604, 7487, 7486, 7485, 7484, 9603, 9602, 6821,
			1481, 1482, 1483, 23812, 587, 588, 6458, 6459,
			25797, 28431, 9934, 4432, 23874, 9684, 9685, 9686,
			4572, 4573, 1488, 1489, 1490, 2339, 2340, 677,
			678, 970, 10585, 11036, 2424, 3128, 5012, 86,
			10592, 28366, 28380, 28405, 3695, 1577, 4283, 26569,
			767, 11165, 11167, 4621, 758, 4623, 23838, 23878,
			4199, 1510, 1486, 424, 4689, 6955, 9059, 6455,
			1013, 26284, 26280, 26282, 9723, 10871, 31363, 10872,
			31297, 433, 284, 418, 285, 26577, 10877, 6468,
			7628, 27424, 7143, 28987, 29879, 273, 6768, 274,
			4494, 4500, 10496, 24262, 4244, 666, 4440, 7468,
			3214, 7811, 6771, 30947, 2394, 22409, 28382, 21531,
			10886, 10890, 29547, 28434, 27010, 6542, 271, 426,
			428, 6073, 9727, 9728, 29867, 29950, 3135, 31353,
			31355, 28412, 3709, 29911, 9676, 9674, 9672, 9678,
			25822, 28436, 1849, 1853, 7969, 1812, 9058, 7410,
			9813, 9814, 28975, 3213, 31375, 6995, 7896, 28463,
			714, 14, 29929, 7818, 7917, 7120, 29546, 25964,
			609, 7836, 24, 6773, 11008, 300, 28422, 29216,
			7529, 338, 7543, 7566, 3150, 2148, 29076, 7577,
			4603, 25793, 6546, 33009, 7572, 1470, 9597, 23,
			6640, 23776, 1763, 6085, 3742, 4610, 4445, 9600,
			23795, 6454, 7483, 7482, 7481, 7480, 9599, 6090,
			9598, 25, 30103, 6954, 26592, 22589, 23906, 28432,
			24942, 6820, 2373, 2374, 2375, 7121, 7148, 28433,
			28423, 291, 10492, 25824, 290, 28347, 28348, 28354,
			29541, 11164, 22096, 28368, 28376, 5063, 4202, 28329,
			4657, 6066, 10866, 6786, 6787, 22083, 1480, 1855,
			7533, 9716, 2379, 7649, 7648, 7639, 7647, 7646,
			7644, 7643, 7642, 7641, 1534, 7445, 30964, 4498,
			7155, 11046, 25795, 25802, 25805, 6956, 1984, 6093,
			11044, 11045, 21758, 10830, 19564, 276, 21053, 28418,
			29523, 24693, 4810, 6467, 7460, 6466, 11199, 3849,
			26960, 4484, 9904, 29904, 28349, 4081, 4082, 6958,
			9943, 6985, 6986, 6988, 6987, 6945, 6948, 21055,
			28416, 4700, 26574, 19527, 28392, 21529, 32083, 21528,
			26956, 26952, 28443, 4575, 4578, 4576, 4577, 456,
			29880, 29881, 29882, 11681, 717, 19511, 23773, 6758,
			7968, 9721, 10485, 719, 5519, 3704, 31395, 1466,
			9680, 7887, 9083, 683, 9653, 3738, 2882, 1552,
			3168, 3104, 3745, 10488, 3414, 3412, 3410, 3408,
			3419, 3418, 3417, 3416, 27602, 10857, 28367, 28377,
			4673, 28370, 10858, 28819, 2397, 729, 6969, 4444,
			25816, 5603, 4186, 280, 281, 282, 283, 279,
			11054, 28807, 25813, 85, 29927, 10897, 6790, 11204,
			11202, 11205, 4236, 4816, 29572, 10839, 10836, 10838,
			10837, 1802, 1800, 1798, 10176, 1796, 1804, 1806,
			4658, 2963, 27420, 2402, 2399, 2400, 2401, 5011,
			7637, 10856, 11002, 4814, 1530, 2376, 720, 7443,
			10904, 10950, 9715, 28818, 1846, 1845, 1844, 4083,
			6817, 7574, 28441, 28465, 7511, 6715, 9726, 3179,
			3720, 4672, 28452, 28978, 6772, 26590, 4606, 4607,
			4605, 7576, 29933, 7580, 7860, 1526, 26589, 6460,
			9095, 5580, 7512, 6791, 6636, 28450, 24944, 4002,
			7439, 672, 670, 669, 2396, 4691, 7513, 7479,
			3107, 10812, 7437, 6119, 84, 1549, 4183, 9017,
			4618, 6785, 25631, 28459, 778, 7456, 4446, 28979,
			25967, 3746, 28471, 4425, 28976, 3109, 4704, 6989,
			7002, 6997, 6999, 6998, 7000, 699, 26954, 27519,
			28816, 29876, 30954, 606, 3269, 29906, 27598, 28130,
			28360, 4619, 27599, 3713, 4836, 28383, 28427, 25965,
			29535, 28388, 29543, 3224, 26904, 26593, 28574, 3169,
			7579, 3700, 28406, 3209, 25812, 25966, 417, 29531,
			28365, 28379, 9080, 9081, 9079, 11010, 29533, 11034,
			22095, 11056, 623, 7411, 7150, 10587, 6083, 26910,
			2388, 23510, 29903, 607, 23512, 23514, 23007, 24684,
			28394, 7573, 7575, 1850, 673, 6072, 29536, 4557,
			23497, 28389, 7632, 1851, 7878, 29898, 28435, 22506,
			21764, 33126, 7633, 24073, 22049, 1579, 29926, 8837,
			5592, 31359, 5583, 11203, 2393, 2378, 22415, 24690,
			9658, 9657, 9656, 23504, 23506, 23508, 1, 9665,
			4809, 21047, 6788, 6789, 4194, 31393, 31398, 1857,
			419, 4185, 29877, 6096, 6098, 3721, 3701, 19513,
			10493, 4655, 784, 4249, 4277, 23067, 3896, 28982,
			22085, 7884, 3265, 3262, 6478, 4086, 676, 6952,
			29532, 27596, 789, 4601, 4602, 28402, 11175, 29518,
			26633, 10487, 7890, 10490, 22406, 22408, 28386, 4438,
			25702, 3842, 4683, 7821, 1487, 22769, 22770, 22771,
			22773, 1822, 6719, 24256, 682, 3688, 2386, 3703,
			11006, 24258, 24259, 24266, 31373, 10875, 7465, 28404,
			28973, 21756, 4616, 22035, 3734, 3737, 3735, 24255,
			28363, 2389, 2390, 22405, 5582, 9086, 29556, 6456,
			9621, 9624, 9623, 9622, 1532, 2425, 7911, 24678,
			24674, 24680, 24675, 24676, 24673, 9087, 625, 11012,
			4656, 28429, 27518, 1503, 3710, 2964, 29522, 29555,
			22094, 28446, 9691, 4085, 24938, 24939, 759, 3705,
			4435, 4434, 29527, 29528, 29529, 9593, 22591, 7866,
			13141, 30936, 28407, 1005, 7957, 1476, 20, 26567,
			4485, 4486, 6453, 6611, 6469, 6464, 6461, 26579,
			2421, 13108, 13109, 11035, 29928, 1491, 28444, 6957,
			7830, 29930, 10896, 2952, 2861, 27523, 10891, 3744,
			7433, 27513, 6084, 2341, 28457, 6713, 1843, 1472,
			9605, 6641, 23777, 1765, 6087, 9056, 9608, 7495,
			7494, 7493, 7492, 9607, 6091, 9606, 28579, 749,
			735, 26576, 6993, 610, 11011, 8870, 8887, 4078,
			7848, 7863, 4029, 4030, 12934
		));
	}

	private static void addSailing(Map<String, List<Integer>> m)
	{
		// SAILING — 546 items
		//   LLM classified (546)
		m.put(TAG_SAILING, Arrays.asList(
			// === LLM classified ===
			31912, 31926, 31940, 32011, 32845, 32826, 32096, 32871,
			32876, 32102, 32836, 32817, 33143, 31255, 32087, 32851,
			31733, 31248, 31245, 31782, 31695, 32344, 31989, 31475,
			31478, 31472, 32431, 31889, 31831, 31888, 31838, 31860,
			31837, 31878, 31905, 31854, 31851, 31863, 31893, 31856,
			31858, 31849, 31840, 31881, 31886, 31875, 31833, 31892,
			31864, 31873, 31845, 31848, 31883, 31861, 31901, 31882,
			31839, 31843, 31900, 31899, 31902, 31847, 31844, 31885,
			31846, 31842, 31890, 31887, 31834, 31895, 31897, 31898,
			31859, 31891, 31855, 31835, 31832, 31852, 31862, 31836,
			31894, 31876, 31871, 31874, 31841, 31857, 31872, 31884,
			31896, 31877, 31850, 31853, 31949, 31779, 31797, 32866,
			31961, 31251, 32868, 31906, 31918, 31932, 31999, 32809,
			32828, 32347, 32315, 32339, 31567, 32355, 31559, 32331,
			32820, 32839, 32364, 32053, 32904, 31432, 31976, 31490,
			31985, 31745, 32886, 32432, 31869, 31460, 31545, 31469,
			32435, 32532, 32673, 32612, 32578, 32632, 32448, 32500,
			32483, 32449, 32548, 32766, 32464, 32465, 32589, 32579,
			32501, 32614, 32615, 32466, 32749, 32467, 32635, 32616,
			32694, 32711, 32580, 32590, 32486, 32468, 32502, 32436,
			32437, 32438, 32736, 32795, 32439, 32591, 32534, 32484,
			32451, 32753, 32565, 32504, 32440, 32470, 32640, 32480,
			32453, 32537, 32490, 32620, 32454, 32621, 32593, 32506,
			32594, 32622, 32471, 32553, 32491, 32441, 32493, 32683,
			32571, 32525, 32623, 32442, 32785, 32595, 32539, 32761,
			32473, 32541, 32572, 32701, 32443, 32444, 32685, 32555,
			32508, 32509, 32526, 32445, 32543, 32742, 32494, 32544,
			32446, 32456, 32495, 32586, 32730, 32457, 32458, 32744,
			32562, 32599, 32474, 32459, 32460, 32461, 32475, 32529,
			32775, 32481, 32574, 32496, 32705, 32514, 32807, 32462,
			32476, 32706, 32447, 32463, 32516, 32777, 32691, 32545,
			32630, 32531, 32671, 32517, 32518, 32499, 32478, 32546,
			32779, 32479, 32806, 32563, 32559, 31807, 31760, 31762,
			31766, 31764, 32892, 31805, 31865, 31300, 31403, 31298,
			31401, 32115, 32406, 31916, 31930, 32409, 31944, 32017,
			32410, 31996, 31406, 32404, 32865, 32819, 32838, 31946,
			31481, 31511, 31788, 32403, 31623, 33074, 31744, 32307,
			32380, 32366, 32368, 32371, 32377, 32383, 32374, 7534,
			32849, 31253, 32859, 32844, 32825, 32402, 31408, 32312,
			31794, 31412, 21728, 32812, 32831, 31235, 31746, 32106,
			31758, 32320, 32357, 31599, 31596, 31593, 31590, 32336,
			32810, 32829, 31757, 31457, 31543, 31466, 32434, 31823,
			31420, 31785, 32093, 31908, 31920, 31934, 32002, 31579,
			32056, 32907, 31435, 31386, 31979, 31505, 31492, 32921,
			32902, 31564, 31258, 31814, 32032, 32020, 32074, 32038,
			32023, 32077, 32071, 32029, 32065, 32080, 32035, 32853,
			32026, 32068, 32062, 32889, 31716, 31463, 31397, 32422,
			32428, 32423, 32421, 32426, 32430, 32424, 32427, 32429,
			32425, 8788, 32050, 31973, 32822, 32841, 32362, 32857,
			32388, 32389, 32390, 32392, 32393, 32394, 32395, 32386,
			32110, 31910, 31924, 31938, 32008, 32846, 32827, 32864,
			32869, 31903, 32813, 31954, 31957, 32832, 31719, 32044,
			31967, 32861, 31416, 32814, 32833, 32843, 32824, 31817,
			31484, 31513, 31297, 32855, 31809, 31424, 32834, 32815,
			31800, 31703, 31734, 31879, 31692, 32341, 32309, 32317,
			32333, 31561, 32349, 31700, 31553, 32325, 31959, 31689,
			32882, 31964, 32408, 32405, 32059, 32407, 32910, 31438,
			31982, 31494, 31724, 31722, 31914, 31928, 31942, 32014,
			32867, 32863, 31288, 31292, 31261, 32399, 32401, 32099,
			32113, 32108, 32085, 31395, 31756, 32090, 31791, 31776,
			32416, 32418, 32420, 32417, 32419, 32415, 32413, 32412,
			32411, 32414, 31773, 32847, 32870, 31283, 32835, 32816,
			31252, 31803, 31572, 31569, 31922, 31936, 32005, 32821,
			32840, 31738, 31736, 31742, 31740, 31732, 31422, 31441,
			31768, 31748, 31750, 31754, 31752, 31952, 31428, 31556,
			31254, 32047, 31970, 31443, 32842, 32823, 31867, 32811,
			32830, 31770, 31393, 31398, 31820, 31487, 31515, 31371,
			32837, 32818, 33145, 33144, 32433, 32396, 31811, 32041,
			32360, 32104
		));
	}

	private static void addCosmetics(Map<String, List<Integer>> m)
	{
		// COSMETICS — 2564 items
		//   LLM classified (2564)
		m.put(TAG_COSMETICS, Arrays.asList(
			// === LLM classified ===
			24539, 27820, 27812, 25328, 25326, 25334, 25330, 25322,
			25332, 25324, 33086, 33080, 33084, 33082, 10344, 20011,
			12424, 12437, 23345, 23339, 23336, 23342, 28226, 10350,
			10352, 12426, 10342, 27183, 20014, 10348, 10346, 23242,
			10334, 27201, 10332, 27200, 10330, 27199, 10340, 10338,
			10336, 12422, 21211, 26308, 10392, 26809, 26807, 13262,
			26901, 26811, 26484, 26482, 13337, 13338, 13070, 12377,
			23270, 2613, 2605, 13020, 13022, 10296, 10298, 10300,
			10302, 10304, 22159, 22161, 22163, 22165, 22167, 22169,
			22171, 22173, 22175, 22177, 22179, 22181, 22183, 22185,
			22187, 22189, 22127, 22129, 22131, 22135, 22137, 22139,
			22141, 22143, 22145, 22147, 22149, 22151, 22153, 22155,
			22157, 2611, 2603, 2607, 23392, 23395, 23398, 23401,
			23404, 2599, 2609, 2601, 3475, 3474, 7334, 7340,
			7346, 7352, 7358, 30607, 13016, 13018, 27394, 27402,
			27410, 27442, 27392, 27400, 27408, 27388, 27396, 27404,
			27390, 27398, 27406, 27412, 12430, 9773, 29986, 29978,
			29982, 20056, 23309, 12436, 10362, 10354, 10366, 23354,
			29804, 20366, 13218, 29774, 20235, 28336, 12490, 26229,
			26227, 26223, 26225, 26221, 12494, 12197, 12496, 12199,
			12492, 23197, 12466, 12468, 12203, 12460, 12462, 12464,
			27381, 12195, 12193, 13060, 13062, 12201, 32096, 22246,
			20101, 20095, 20107, 20098, 20104, 33018, 33012, 13288,
			13345, 12896, 12895, 12893, 12892, 12894, 12897, 29816,
			29818, 20251, 20113, 19943, 33149, 23787, 20760, 20764,
			26713, 12506, 26716, 12510, 26715, 12261, 12512, 12263,
			12508, 23200, 12476, 29605, 20368, 20068, 26714, 12478,
			12259, 12470, 12472, 12474, 12255, 12253, 12257, 29628,
			29619, 29622, 29625, 21898, 21900, 2460, 9749, 13324,
			12646, 26627, 25502, 25840, 26645, 8926, 8927, 8925,
			8924, 26717, 26720, 12498, 12502, 26718, 12273, 12504,
			12275, 12500, 23203, 13167, 12486, 20370, 20071, 12488,
			12271, 12480, 12482, 12484, 12267, 12265, 13056, 12269,
			26719, 8654, 8656, 8658, 8660, 8664, 8672, 8674,
			8676, 8680, 20773, 20777, 20775, 28250, 31172, 6853,
			11705, 12245, 23291, 27039, 13322, 33124, 21245, 25137,
			25135, 25129, 25133, 25131, 32930, 30162, 30242, 6846,
			11282, 23240, 23237, 26919, 26920, 26921, 12355, 26332,
			29931, 21209, 23108, 24331, 21230, 21218, 2635, 7327,
			24986, 12375, 1019, 2643, 12381, 12385, 12383, 12387,
			20026, 9918, 12524, 10402, 10400, 2476, 2595, 2587,
			12996, 12998, 11847, 2647, 10306, 10308, 10310, 10312,
			10314, 2597, 2589, 20246, 8956, 8995, 11862, 2591,
			23366, 23369, 23372, 23375, 23378, 2583, 2593, 2585,
			3473, 3472, 13343, 7332, 7338, 7344, 7350, 7356,
			1015, 12445, 12447, 19639, 19641, 30603, 2524, 8963,
			12992, 12994, 20266, 12453, 12455, 12449, 12451, 19988,
			25870, 25872, 25874, 25876, 25878, 25880, 25882, 28688,
			19730, 2633, 7325, 630, 24981, 1021, 7374, 7376,
			7382, 7384, 12757, 9915, 12520, 25846, 10428, 10410,
			10408, 10430, 7331, 2464, 24431, 1055, 660, 740,
			12301, 6865, 6868, 6875, 6876, 6877, 6878, 8952,
			8991, 1042, 2422, 650, 640, 1011, 7386, 7388,
			4558, 8959, 7394, 7396, 7390, 7392, 26929, 27806,
			10322, 10318, 10320, 10324, 10316, 6856, 6857, 9945,
			9944, 26488, 26490, 29433, 26492, 26494, 20140, 24338,
			24340, 24342, 24344, 24346, 24348, 28017, 25884, 25886,
			25888, 25890, 25892, 25894, 25896, 33021, 20110, 6828,
			11912, 30622, 1009, 26641, 26639, 23105, 12335, 29519,
			24544, 27555, 24547, 12363, 8968, 12211, 12221, 12968,
			12970, 12213, 12223, 12205, 12215, 12207, 12217, 12209,
			12219, 27418, 30597, 12964, 12966, 24980, 2649, 8955,
			8994, 2520, 8962, 27485, 33099, 26615, 19991, 20059,
			26617, 21243, 10865, 10863, 10864, 21874, 1037, 13182,
			13664, 13665, 13663, 23448, 26643, 28248, 29912, 29914,
			29915, 13679, 20272, 13680, 24549, 27804, 30042, 13178,
			7003, 6655, 6658, 6656, 6659, 6654, 6657, 22719,
			26524, 26520, 26526, 26522, 9946, 23351, 26613, 24546,
			30722, 30726, 30720, 30724, 30708, 24537, 24525, 12361,
			1575, 22680, 22681, 22682, 11280, 21439, 22361, 27643,
			26625, 1949, 11019, 11021, 11022, 11020, 26304, 11025,
			11026, 11910, 2978, 2979, 2980, 2981, 2982, 2983,
			2984, 2985, 2986, 2987, 2988, 2989, 2990, 2991,
			2992, 2993, 2994, 2995, 13071, 962, 32934, 27566,
			22687, 26935, 9642, 25712, 21396, 30648, 30646, 26290,
			26288, 23413, 27438, 22692, 22695, 22689, 22701, 22698,
			19695, 19697, 19689, 19691, 19693, 20249, 29781, 28601,
			30579, 30581, 30583, 30585, 30587, 30589, 30591, 30593,
			30595, 12835, 24543, 9791, 9803, 26925, 20838, 20846,
			20840, 20842, 20844, 22678, 12958, 11919, 12959, 33093,
			33096, 33097, 12956, 12957, 27794, 9782, 26939, 632,
			662, 652, 642, 20243, 20240, 12319, 13681, 21239,
			21227, 27697, 27709, 27721, 27733, 27745, 27757, 27769,
			33023, 23911, 23913, 23915, 23917, 23919, 23921, 23923,
			23925, 31760, 31762, 31766, 31764, 24000, 27705, 27717,
			27729, 27741, 27753, 27765, 27777, 33031, 27701, 27713,
			27725, 27737, 27749, 27761, 27773, 33027, 23941, 23933,
			23935, 23939, 23929, 23927, 23937, 23931, 25500, 11708,
			11707, 27248, 8966, 19915, 27123, 27127, 27125, 27121,
			24733, 29611, 19970, 2641, 24729, 25557, 30167, 30287,
			12459, 12528, 12457, 12458, 19964, 19961, 19958, 19967,
			22686, 24191, 24189, 24190, 4069, 4504, 4559, 12540,
			9755, 23294, 33305, 33269, 33281, 33293, 33260, 33272,
			33284, 33349, 33299, 33357, 33368, 33345, 33355, 33351,
			33347, 33353, 33362, 33266, 33278, 33290, 33263, 33275,
			33287, 33296, 33302, 33338, 33308, 33311, 13133, 6390,
			6386, 6834, 27783, 13188, 28682, 21723, 21722, 28902,
			28904, 29730, 29736, 29732, 29722, 29724, 29728, 29718,
			29726, 29734, 29720, 29716, 29740, 29764, 29760, 29744,
			29756, 29754, 29750, 29772, 29768, 29742, 29748, 29762,
			29766, 29738, 29758, 29746, 29752, 29770, 29714, 29712,
			29704, 29688, 29710, 29686, 29706, 29698, 29700, 29692,
			29708, 29694, 29696, 30491, 30487, 31130, 19727, 28051,
			25378, 28037, 28055, 22234, 22231, 27810, 12373, 28065,
			12414, 12534, 28039, 26708, 26707, 28053, 28019, 19722,
			20143, 12417, 12538, 28049, 25373, 25918, 25916, 22244,
			22239, 12536, 28033, 28027, 28057, 12797, 23677, 25376,
			12800, 22242, 22236, 28061, 12415, 28063, 12416, 28031,
			20000, 20002, 28041, 28059, 12418, 12532, 30611, 28029,
			28035, 26710, 26709, 23667, 24043, 24034, 24046, 24037,
			24040, 12600, 23206, 27801, 21231, 21219, 11997, 4565,
			13185, 1961, 7928, 11027, 26926, 21214, 26937, 7927,
			27873, 30445, 30451, 30449, 30447, 30568, 30453, 30434,
			30436, 30432, 30437, 30443, 30441, 30439, 22684, 30970,
			33147, 21252, 29443, 29441, 29437, 29439, 27563, 22351,
			22353, 27119, 27117, 27113, 27115, 27100, 27098, 27253,
			26471, 26469, 24003, 24006, 24024, 24012, 24018, 24009,
			24015, 24021, 24027, 29868, 29872, 29870, 29874, 13183,
			21873, 21861, 7400, 7398, 21862, 7399, 20433, 20439,
			20442, 20436, 9919, 12514, 28672, 20008, 9812, 11990,
			26270, 24432, 24434, 26312, 26310, 24436, 27588, 24433,
			26292, 26272, 26294, 27578, 27576, 27580, 27572, 27574,
			24435, 30489, 26274, 6382, 28067, 21865, 13329, 21186,
			13330, 9806, 3006, 6670, 9800, 21238, 21226, 10394,
			9785, 27035, 21236, 21224, 21248, 28175, 28173, 28171,
			28670, 28626, 3771, 3789, 3763, 3775, 3761, 3767,
			3759, 3765, 3779, 3769, 3797, 23246, 3787, 3785,
			3777, 3773, 3793, 3783, 3781, 21229, 21217, 6188,
			23288, 6183, 12774, 26936, 12769, 21228, 21216, 28138,
			12526, 5345, 23859, 27550, 9906, 6106, 6111, 6110,
			6109, 6107, 6108, 23252, 25314, 27871, 23446, 20836,
			20659, 27414, 20155, 13036, 13038, 23279, 12391, 20149,
			23258, 23264, 23267, 23261, 23124, 3486, 20161, 3488,
			20146, 23276, 3481, 3483, 3485, 12389, 8967, 23282,
			20158, 20152, 30613, 24443, 24437, 20134, 27434, 27799,
			27802, 13655, 25336, 27818, 9472, 9470, 27800, 25316,
			25290, 12251, 12727, 25289, 12343, 12349, 12347, 12345,
			21247, 12303, 1654, 27422, 20208, 20205, 28663, 25555,
			25549, 25551, 25553, 27560, 22840, 27038, 24758, 25084,
			27459, 30060, 24746, 25072, 27447, 30048, 30044, 24755,
			25081, 27456, 30057, 13615, 24743, 25069, 27444, 30045,
			24752, 25078, 27453, 30054, 24749, 25075, 27450, 30051,
			12849, 24225, 13286, 13287, 13285, 13283, 13284, 20023,
			13328, 7323, 628, 24985, 1027, 7370, 7372, 7378,
			7380, 12759, 9916, 12518, 10432, 10414, 10412, 10434,
			25284, 7330, 24428, 1053, 658, 12307, 6866, 6869,
			6879, 6880, 6881, 6882, 8953, 8992, 1044, 21311,
			648, 638, 19643, 19645, 4563, 8960, 31034, 25604,
			2894, 2902, 2900, 8958, 8997, 2898, 2896, 2526,
			8965, 31181, 31184, 31190, 31187, 31208, 31231, 31233,
			12845, 31229, 12836, 12837, 26168, 27046, 26156, 26158,
			27048, 26166, 27044, 26820, 10506, 31285, 13048, 10376,
			10380, 10448, 10382, 10442, 10378, 23188, 13165, 2673,
			2675, 13335, 13336, 10454, 3835, 3836, 3837, 3838,
			2669, 2671, 3480, 10466, 10462, 10472, 1989, 20053,
			13175, 30234, 27497, 4310, 4304, 4308, 4302, 23360,
			4306, 4300, 4298, 21354, 10862, 26182, 26170, 26172,
			26180, 20792, 20794, 20796, 8928, 26260, 24975, 26712,
			26711, 19941, 24527, 7583, 13247, 28070, 19687, 21509,
			9776, 13320, 22679, 2631, 9770, 20220, 26496, 25734,
			22355, 33002, 25742, 12598, 25731, 25733, 25736, 25738,
			19997, 20229, 20128, 27428, 33066, 33068, 19699, 20254,
			24885, 20116, 19946, 12841, 30152, 26336, 12839, 12843,
			26338, 9950, 12855, 20779, 23073, 33330, 33332, 26928,
			24438, 27267, 27257, 27259, 27261, 27263, 27265, 28786,
			22746, 29615, 21784, 21786, 29617, 21776, 21778, 29613,
			21782, 12249, 33359, 33365, 24993, 13184, 26631, 25066,
			30347, 25371, 30348, 25059, 30342, 25367, 30343, 21285,
			21282, 25063, 30345, 25369, 30346, 27440, 13344, 21208,
			30493, 12365, 12241, 12231, 12980, 12982, 12243, 12233,
			12235, 12225, 12237, 12227, 12239, 12229, 30599, 12976,
			12978, 12810, 12811, 12812, 9920, 32932, 27814, 27808,
			29932, 21291, 19701, 24495, 32921, 13277, 12885, 25524,
			13245, 25521, 21745, 29786, 26250, 23297, 6858, 6859,
			21720, 10059, 20032, 12647, 24862, 6717, 12357, 21875,
			24866, 26334, 20164, 21198, 12371, 21202, 6550, 23381,
			23384, 6182, 6181, 6180, 19724, 27037, 12359, 20020,
			27561, 19985, 12421, 12530, 12419, 12420, 19979, 19976,
			19973, 19982, 25348, 22473, 28252, 13216, 24491, 26326,
			26912, 20257, 20119, 19949, 28128, 32928, 8969, 11023,
			24535, 9764, 26278, 26924, 24977, 13199, 13201, 12806,
			24864, 6864, 13203, 23522, 27370, 27374, 27363, 27366,
			27372, 13281, 31331, 21233, 21221, 26927, 27255, 6392,
			6398, 6396, 6394, 6400, 6406, 6404, 6402, 22386,
			3061, 3060, 3059, 3057, 3058, 9794, 12369, 12283,
			12293, 13008, 13010, 12281, 12291, 12277, 12287, 12279,
			12289, 12285, 12295, 30605, 13004, 13006, 2472, 23285,
			20202, 23306, 20199, 23303, 19556, 12353, 25286, 30154,
			6665, 27562, 20083, 20092, 20086, 20080, 20089, 27590,
			21711, 13221, 13223, 12351, 12443, 12441, 26539, 27645,
			26537, 26531, 21200, 26535, 26533, 12796, 24855, 24857,
			12325, 22249, 27875, 26348, 29836, 21748, 26330, 21715,
			26276, 26596, 19918, 6548, 20050, 19720, 20065, 26963,
			12807, 20029, 27494, 27822, 20851, 31117, 7321, 24987,
			1031, 25844, 2470, 30165, 30269, 21309, 23093, 23099,
			23091, 23101, 23095, 23097, 27246, 7414, 24541, 10396,
			25838, 12399, 13173, 25609, 20226, 22358, 22842, 12428,
			27554, 1561, 1567, 11995, 12644, 12645, 12643, 12816,
			12650, 12652, 1555, 12655, 12649, 12703, 3695, 12648,
			12921, 12651, 28655, 8971, 28669, 28618, 28616, 28620,
			28622, 28624, 20693, 30160, 12309, 626, 24988, 6959,
			12339, 12317, 12315, 12341, 656, 12305, 646, 636,
			1013, 4564, 7112, 7124, 7130, 7136, 7114, 12412,
			7116, 7126, 7132, 7138, 2651, 2997, 20260, 20122,
			19952, 12516, 26635, 27424, 28126, 26619, 26621, 30947,
			30168, 30296, 9761, 13346, 13656, 30479, 12653, 24867,
			27795, 21870, 27796, 22316, 25606, 1959, 24325, 6852,
			30793, 30791, 30783, 30787, 30785, 30789, 12311, 2934,
			24983, 1029, 10436, 10418, 10416, 10438, 10326, 2468,
			2942, 2940, 8957, 8996, 1046, 2938, 2936, 21264,
			21266, 8964, 27041, 9814, 28962, 11024, 30779, 30777,
			30781, 30469, 30430, 30410, 30418, 30426, 30477, 30428,
			30557, 30455, 30465, 30404, 30412, 30420, 30475, 30471,
			30459, 30457, 30461, 30331, 30334, 30337, 30408, 30416,
			30424, 30554, 30467, 30560, 30563, 30473, 30406, 30414,
			30422, 23357, 29489, 29507, 28116, 11863, 21314, 30717,
			29929, 11848, 19994, 23249, 12596, 9758, 31879, 6773,
			30709, 12247, 7319, 2904, 24984, 1007, 12323, 12327,
			12331, 12329, 12333, 9914, 12522, 10424, 10406, 10404,
			10426, 25283, 2462, 24430, 2912, 1057, 2910, 2645,
			6867, 6870, 6871, 6872, 6873, 6874, 8954, 8993,
			1038, 30166, 30278, 21308, 2908, 2906, 19647, 19649,
			4562, 8961, 31049, 10507, 27377, 27378, 27379, 27380,
			21235, 21223, 24989, 24990, 20665, 1025, 23185, 20017,
			20005, 6583, 20137, 27432, 20131, 27430, 21250, 13321,
			20663, 12856, 25287, 13327, 12397, 6186, 6185, 6187,
			6184, 12395, 12393, 12439, 4566, 22666, 12379, 26486,
			23230, 23227, 23273, 2619, 2627, 13032, 13034, 10286,
			10288, 10290, 10292, 10294, 8464, 8466, 8468, 8470,
			8472, 8474, 8476, 8478, 8480, 8482, 8484, 8486,
			8488, 8490, 8492, 8494, 8714, 8716, 8718, 8722,
			8724, 8726, 8728, 8730, 8732, 8734, 8736, 8738,
			8740, 8744, 2621, 2629, 2615, 23209, 23212, 23215,
			23218, 23221, 2623, 2617, 2625, 3476, 3477, 23330,
			23332, 23334, 26262, 23321, 23324, 23327, 7336, 7342,
			7348, 7354, 7360, 30609, 13028, 13030, 9767, 21695,
			10552, 27570, 20834, 12337, 31292, 21234, 21222, 20047,
			20041, 20044, 20035, 20038, 23318, 23312, 23315, 25746,
			25744, 25739, 25741, 28254, 28256, 28258, 12891, 12890,
			1050, 12888, 12887, 12889, 21866, 21868, 21867, 27564,
			21871, 13040, 13042, 10384, 10388, 10446, 10440, 23191,
			2665, 20372, 20074, 2667, 13331, 13332, 10452, 3829,
			2661, 2663, 3479, 10464, 10458, 10470, 26254, 30232,
			13181, 33146, 22675, 28801, 1419, 21246, 26918, 26298,
			26316, 30161, 12840, 24792, 21838, 26424, 26436, 26448,
			26460, 26517, 26528, 27557, 26427, 26439, 26451, 26554,
			26557, 26560, 26511, 26503, 26515, 26505, 26509, 26541,
			26513, 26507, 26421, 26479, 26500, 26430, 26442, 26454,
			26433, 26445, 26457, 20263, 20125, 19955, 5030, 5032,
			5034, 5042, 5044, 5046, 23300, 27828, 27420, 22494,
			22496, 22498, 22500, 22502, 9921, 9922, 24327, 9923,
			9925, 24865, 9924, 5048, 5050, 5052, 26649, 21273,
			9788, 25282, 10398, 11916, 22350, 26328, 27488, 9797,
			24298, 21237, 21225, 28960, 23760, 29933, 20832, 27568,
			21849, 21857, 21855, 21847, 21851, 21853, 13217, 21864,
			10501, 10509, 27559, 28788, 26314, 27436, 33335, 31283,
			26916, 26917, 27416, 21232, 21220, 28603, 24992, 23389,
			4613, 24323, 24321, 24315, 24317, 24319, 24313, 31225,
			27491, 24311, 24305, 31222, 31227, 24307, 31224, 24309,
			31223, 23495, 23363, 20590, 25288, 6822, 25547, 22713,
			12795, 12798, 12367, 20178, 20193, 20382, 20385, 8682,
			8684, 8686, 8688, 8690, 8692, 8694, 8696, 8698,
			8700, 8702, 8704, 8706, 8708, 8710, 8712, 8746,
			8748, 8750, 8754, 8756, 8758, 8762, 8766, 8768,
			8770, 8776, 20181, 20196, 20169, 20184, 20172, 20187,
			20175, 20190, 30601, 20376, 20379, 9702, 25285, 31738,
			31736, 31742, 31740, 27816, 10364, 9752, 7110, 7122,
			7128, 7134, 26264, 7362, 7364, 7366, 7368, 31748,
			31750, 31754, 31752, 31193, 31196, 31202, 31199, 2639,
			20661, 26931, 13197, 13200, 21714, 2924, 2932, 2930,
			2928, 2926, 20217, 20214, 20211, 4315, 4333, 4335,
			4337, 4339, 4341, 4343, 4345, 4347, 4349, 4351,
			4317, 4353, 4355, 4357, 4359, 4361, 4363, 4365,
			4367, 4369, 4371, 4319, 4373, 4375, 4377, 4379,
			4381, 4383, 4385, 4387, 4389, 4391, 4321, 4393,
			4395, 4397, 4399, 4401, 4403, 4405, 4407, 4409,
			4411, 4323, 4413, 4325, 4327, 4329, 4331, 27040,
			27483, 12861, 25610, 23224, 9779, 13215, 21717, 26600,
			25602, 26623, 21713, 12432, 12434, 23444, 23348, 20062,
			30302, 30303, 30304, 33036, 7771, 25046, 25056, 25037,
			25025, 25010, 25054, 25013, 25042, 25099, 25028, 25016,
			25001, 25052, 25048, 25380, 25383, 25386, 28755, 28693,
			28702, 28690, 28721, 28733, 28745, 28763, 28684, 28699,
			28751, 28712, 28724, 28736, 28705, 28761, 28757, 28708,
			28777, 28780, 28783, 28753, 28759, 28715, 28727, 28739,
			28748, 28718, 28730, 28742, 28696, 25096, 25044, 25050,
			25087, 25090, 25031, 25019, 25004, 25034, 25022, 25007,
			8970, 27463, 6840, 22717, 22715, 6860, 6861, 6335,
			6337, 6339, 6341, 6351, 6361, 6371, 33326, 33323,
			33318, 33314, 5036, 5038, 5040, 27352, 634, 664,
			654, 644, 21888, 21890, 24376, 24670, 24664, 24668,
			24666, 24413, 24463, 24411, 24403, 24393, 24384, 24395,
			24407, 24399, 24389, 24372, 24405, 24397, 24387, 24466,
			24382, 24378, 24469, 24472, 24475, 24374, 24370, 24380,
			24460, 24409, 24401, 24391, 23235, 23232, 25910, 25912,
			13225, 25898, 25900, 26256, 12813, 12814, 12815, 33320,
			33316, 33328, 10487, 20223, 26498, 24542, 23255, 25904,
			25906, 27798, 26933, 31133, 13177, 27556, 13179, 13352,
			13349, 13350, 13351, 24207, 24209, 24213, 24520, 24211,
			24215, 6347, 6359, 6369, 6379, 6345, 6355, 6365,
			6375, 6343, 6353, 6363, 6373, 6349, 6357, 6367,
			6377, 12844, 26467, 26465, 26463, 26473, 26477, 26475,
			29607, 13186, 29609, 12773, 12771, 21992, 24794, 24810,
			24826, 24796, 24812, 24828, 24802, 24818, 24834, 24808,
			24824, 24840, 24806, 24822, 24838, 24678, 24800, 24816,
			24832, 24680, 24804, 24820, 24836, 24676, 24798, 24814,
			24830, 9636, 9638, 9634, 20232, 795, 12802, 26930,
			28671, 21240, 21251, 21249, 21242, 21241, 21244, 1005,
			24297, 2637, 12313, 24982, 12321, 12763, 9913, 10420,
			10422, 10327, 2474, 12299, 1048, 30163, 30251, 4560,
			2522, 20269, 21428, 21434, 21433, 12838, 10280, 6556,
			10508, 21859, 28246, 27479, 27481, 27473, 27477, 27475,
			24991, 2579, 23410, 23407, 29930, 9809, 26934, 20166,
			6862, 6863, 5024, 5026, 5028, 22396, 22394, 22388,
			22392, 22390, 30888, 2914, 1023, 12761, 9917, 2466,
			2922, 2920, 1040, 30164, 30260, 21310, 2918, 2916,
			7803, 4079, 23757, 23908, 13044, 10368, 10372, 10450,
			10374, 10444, 10370, 23194, 2657, 20374, 20077, 2659,
			13333, 13334, 10456, 3832, 3834, 2653, 2655, 3478,
			10468, 10460, 10474, 26611, 7596, 7595, 6722, 19912,
			7594, 24863, 7592, 7593
		));
	}
}
