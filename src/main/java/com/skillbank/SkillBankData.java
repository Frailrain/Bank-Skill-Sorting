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
		// MELEE — 1481 items
		//   Combat utility (6), Weapons (285), Helmets (224), Body armour (137),
		//   Legs (124), Boots (68), Gloves (68), Shields (111), Capes (50),
		//   Amulets (37), Rings (17), Combat potions (56), Restores (cross-tag)
		//   (14), Combat food (23), Legacy (261)
		m.put(TAG_MELEE, Arrays.asList(
			// === Combat utility ===
			2, 6, 8, 10, 12, 21728,

			// === Weapons ===
			1205, 27871, 1321, 1291, 1277, 1307, 1422, 1375,
			1337, 11367, 1237, 3190, 3095, 1203, 1323, 1293,
			1279, 1309, 1420, 1363, 1335, 11369, 1239, 3192,
			3096, 1207, 1325, 1295, 1281, 1311, 1424, 1365,
			1339, 11371, 1241, 3194, 3097, 1217, 1327, 1297,
			1283, 1313, 1426, 1367, 1341, 4580, 3196, 3098,
			12375, 6591, 6611, 6607, 6605, 6609, 6601, 6589,
			6613, 6599, 6587, 1209, 1329, 1299, 1285, 1315,
			1428, 1369, 1343, 11373, 1243, 3198, 3099, 1211,
			1331, 1301, 1287, 1317, 1430, 1371, 1345, 11375,
			1245, 3200, 3100, 12377, 1213, 1333, 1303, 1289,
			1319, 1432, 1373, 1347, 11377, 1247, 3202, 3101,
			12379, 1215, 4587, 1305, 27859, 21009, 7158, 1434,
			27857, 1377, 13576, 22731, 1249, 3204, 28534, 13652,
			21028, 22978, 11920, 30340, 23987, 23896, 23895, 23897,
			23762, 23861, 4747, 4726, 4755, 4718, 21031, 21033,
			33243, 24417, 11802, 29605, 28537, 25739, 25741, 28543,
			28545, 25736, 25738, 1419, 22325, 22486, 13265, 27861,
			8872, 12389, 12426, 7806, 667, 4068, 21646, 11902,
			11838, 12809, 10858, 22331, 30367, 9703, 24617, 22613,
			27904, 13108, 13109, 13110, 13111, 7675, 20155, 11061,
			7808, 25641, 28792, 27660, 27657, 22542, 7807, 20727,
			7668, 21742, 22622, 27908, 7809, 28988, 5016, 20161,
			20158, 4158, 20397, 30369, 22610, 27900, 11889, 11824,
			23850, 23849, 23851, 29796, 29577, 4151, 26233, 11804,
			12774, 11806, 12773, 11808, 23995, 24551, 28682, 21015,
			22324, 25734, 7142, 13263, 12006, 24880, 19675, 30955,
			10129, 10887, 27855, 11705, 31248, 24697, 5018, 11037,
			7451, 27021, 23823, 23820, 28531, 6746, 772, 28997,
			23206, 30957, 7435, 21003, 29589, 35, 33249, 3757,
			7441, 29889, 4153, 7141, 19941, 20756, 12357, 10581,
			25979, 30891, 25981, 27287, 27291, 7447, 7140, 7409,
			11711, 7449, 21649, 19918, 26219, 31049, 7445, 23528,
			2402, 7443, 30759, 7439, 7437, 24144, 22296, 11791,
			29084, 30388, 6523, 6525, 33036, 12902, 12904, 6527,
			6528, 27690, 29607, 2952, 7433,

			// === Helmets ===
			1139, 1155, 1137, 1153, 1141, 1157, 8682, 1151,
			1165, 8905, 8907, 8909, 8911, 8915, 8917, 8919,
			8921, 10306, 10308, 10310, 10312, 10314, 11776, 11777,
			11778, 11779, 11781, 11782, 11783, 11784, 19639, 19641,
			25276, 26781, 29560, 6621, 6623, 1143, 1159, 1145,
			1161, 10296, 10298, 10300, 10302, 10304, 22159, 1147,
			1163, 8464, 10286, 10288, 10290, 10292, 10294, 1149,
			11335, 23886, 23887, 23888, 4716, 4724, 4745, 4753,
			30445, 11826, 12476, 12512, 21018, 24192, 24419, 24664,
			26382, 28254, 1167, 1169, 2657, 2665, 2673, 3327,
			3329, 3331, 3333, 3335, 3337, 3339, 3341, 3343,
			3486, 3748, 3749, 3751, 3753, 3755, 4071, 4302,
			4513, 4551, 4567, 5014, 5574, 6128, 7539, 7917,
			9068, 9069, 9096, 9629, 9672, 10039, 10045, 10051,
			10334, 10350, 10374, 10382, 10390, 10547, 10548, 10549,
			10550, 10828, 11200, 11663, 11664, 11665, 11864, 11865,
			12466, 12486, 12496, 12504, 12637, 12638, 12639, 12810,
			12813, 12931, 13137, 13138, 13139, 13140, 13197, 13199,
			13359, 13364, 13369, 13374, 13379, 19643, 19645, 19647,
			19649, 19687, 19988, 20035, 20146, 20792, 20838, 21264,
			21266, 21298, 21838, 21888, 21890, 22650, 23073, 23075,
			23101, 23258, 23785, 23840, 23841, 23842, 24034, 24195,
			24198, 24201, 24204, 24271, 24370, 24444, 25165, 25398,
			25413, 25898, 25900, 25904, 25906, 25910, 25912, 26156,
			26170, 26241, 26731, 26733, 26735, 26743, 26745, 26747,
			27042, 27235, 27836, 27839, 27844, 27847, 27850, 28933,
			29010, 29019, 29028, 29816, 29818, 30073, 30111, 30321,
			30437, 30750, 30777, 31172, 33066, 33068, 33247, 33338,

			// === Body armour ===
			1103, 1117, 1101, 1115, 1105, 1119, 1107, 1125,
			23366, 23369, 23372, 23375, 23378, 29562, 6615, 6617,
			1109, 1121, 1111, 1123, 23392, 23395, 23398, 23401,
			23404, 1113, 1127, 23209, 23212, 23215, 23218, 23221,
			3140, 21892, 23889, 23890, 23891, 4720, 4728, 4749,
			4757, 30447, 12470, 12508, 21021, 24420, 24666, 1129,
			1131, 2653, 2661, 2669, 3481, 3767, 3769, 3771,
			3773, 3775, 3793, 4069, 4298, 5575, 6129, 8839,
			9070, 9097, 9634, 9640, 9674, 10037, 10043, 10049,
			10053, 10057, 10061, 10065, 10330, 10348, 10370, 10378,
			10386, 10551, 10822, 12460, 12480, 12492, 12500, 12596,
			12811, 12814, 13072, 13104, 13105, 13106, 13107, 13361,
			13366, 13371, 13376, 13381, 19689, 20038, 20149, 20794,
			20840, 21301, 22653, 23097, 23264, 23787, 23843, 23844,
			23845, 24037, 25389, 25404, 26158, 26172, 26243, 26721,
			26751, 26753, 27048, 27229, 27840, 27842, 27845, 27851,
			28936, 29004, 29013, 29022, 29280, 30076, 30439, 30753,
			30779,

			// === Legs ===
			1075, 1087, 1067, 1081, 1069, 1083, 1077, 1089,
			29564, 6625, 6627, 1071, 1085, 1073, 1091, 1079,
			1093, 4087, 4585, 23892, 23893, 23894, 4722, 4730,
			4751, 4759, 30449, 12472, 12474, 12510, 21024, 24421,
			24668, 1095, 2655, 2663, 2671, 3478, 3479, 3480,
			3483, 3485, 3795, 4300, 5576, 6130, 6809, 8840,
			9071, 9098, 9636, 9642, 9676, 9678, 10035, 10041,
			10047, 10055, 10059, 10063, 10067, 10332, 10346, 10372,
			10380, 10388, 10555, 10956, 12462, 12464, 12482, 12484,
			12494, 12502, 12812, 12815, 13073, 13112, 13113, 13114,
			13115, 13360, 13365, 13370, 13375, 13380, 19693, 20044,
			20796, 20842, 20844, 21304, 22656, 23095, 23242, 23246,
			23267, 23789, 23846, 23847, 23848, 24040, 25401, 25416,
			26166, 26180, 26245, 26755, 26757, 26759, 27044, 27232,
			27841, 27843, 27846, 27852, 28939, 29007, 29025, 29283,
			30079, 30441, 30756, 30781,

			// === Boots ===
			4119, 4121, 4123, 4125, 6619, 4127, 4129, 4131,
			11840, 19930, 1061, 3105, 3107, 3791, 4310, 6145,
			7159, 9005, 9006, 9073, 9100, 9638, 9644, 10552,
			10958, 11836, 12391, 13129, 13130, 13131, 13132, 13237,
			13239, 13358, 13363, 13368, 13373, 13378, 19695, 19921,
			19924, 19927, 19933, 19936, 20047, 21643, 21733, 22951,
			23037, 23093, 23389, 24043, 25163, 25395, 25410, 25557,
			28672, 28945, 29286, 29806, 31088, 31091, 31092, 31093,
			31094, 31095, 31096, 31097,

			// === Gloves ===
			7454, 7455, 778, 7456, 7457, 6629, 7458, 27110,
			7459, 7460, 27111, 7461, 1059, 1580, 2997, 3799,
			4308, 6151, 7453, 7462, 7537, 8842, 8929, 9072,
			9099, 10075, 10077, 10079, 10081, 10083, 11118, 11120,
			11122, 11124, 11126, 11133, 11136, 11138, 11140, 11972,
			11974, 13103, 13357, 13362, 13367, 13372, 13377, 19691,
			19994, 20041, 21736, 22981, 23091, 23261, 24046, 25392,
			25407, 26168, 26182, 26235, 26723, 26727, 27046, 27112,
			28630, 30380, 30386, 31106,

			// === Shields ===
			1173, 1189, 8844, 1175, 1191, 8845, 1177, 1193,
			8846, 1179, 1195, 7332, 7338, 7344, 7350, 7356,
			8847, 22284, 6631, 6633, 1181, 1197, 8848, 1183,
			1199, 7334, 7340, 7346, 7352, 7358, 8849, 1185,
			1201, 7336, 7342, 7348, 7354, 7360, 8850, 1187,
			1540, 11710, 12954, 21895, 30382, 12478, 12817, 23200,
			1171, 2659, 2667, 2675, 3122, 3488, 3758, 3840,
			3842, 3844, 4072, 4156, 6219, 6221, 6223, 6225,
			6229, 6231, 6233, 6235, 8856, 9704, 10352, 11283,
			11924, 12468, 12488, 12608, 12821, 12825, 12829, 12831,
			13117, 13118, 13119, 13120, 20152, 20272, 20846, 21633,
			22251, 22254, 22257, 22260, 22263, 22266, 22272, 22275,
			22278, 22281, 22322, 23188, 23191, 23194, 23197, 23203,
			24266, 25985, 27251, 27550, 27552, 31081, 33101,

			// === Capes ===
			21285, 21295, 12261, 6568, 6570, 7918, 9747, 9750,
			9753, 9756, 9759, 9762, 9765, 9768, 9771, 9774,
			9777, 9780, 9783, 9786, 9789, 9792, 9795, 9798,
			9801, 9804, 9807, 9810, 9813, 9948, 10448, 12197,
			12273, 12437, 13121, 13122, 13123, 13124, 13221, 13329,
			19476, 20050, 20760, 22114, 23345, 23351, 24855, 31288,
			31398, 33063,

			// === Amulets ===
			1478, 1704, 1706, 1708, 1710, 1712, 1718, 1724,
			1725, 1729, 1731, 4081, 6585, 10354, 10588, 11128,
			11666, 11667, 11668, 11669, 11671, 11672, 11673, 11976,
			11978, 12017, 12018, 12851, 19553, 19707, 22111, 22557,
			22986, 24780, 29801, 29804, 30376,

			// === Rings ===
			30895, 6735, 6737, 11772, 11773, 12601, 12603, 12605,
			12691, 12692, 13202, 19550, 19710, 28307, 28316, 28329,
			30378,

			// === Combat potions ===
			2428, 121, 123, 125, 9739, 9741, 9743, 9745,
			2432, 133, 135, 137, 23697, 23700, 23703, 23706,
			23685, 23688, 23691, 23694, 23721, 23724, 23727, 23730,
			23709, 23712, 23715, 23718, 6685, 6687, 6689, 6691,
			113, 115, 117, 119, 2436, 145, 147, 149,
			12695, 12697, 12699, 12701, 2442, 163, 165, 167,
			2440, 157, 159, 161, 2450, 189, 191, 193,

			// === Restores (cross-tag) ===
			24598, 24601, 24603, 24605, 10925, 10927, 10929, 10931,
			3024, 3026, 3028, 3030, 11493, 11495,

			// === Combat food ===
			319, 315, 325, 347, 3381, 355, 333, 339,
			351, 5003, 329, 361, 379, 365, 373, 2149,
			7946, 3144, 385, 397, 391, 11936, 13441,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			74, 75, 88, 278, 589, 616, 626, 628,
			630, 632, 634, 636, 638, 640, 642, 644,
			646, 648, 650, 652, 654, 746, 747, 975,
			1052, 1063, 1065, 1097, 1099, 1133, 1231, 1361,
			1495, 1727, 2405, 2493, 2495, 2497, 2572, 2577,
			2581, 2894, 2896, 2898, 2902, 2904, 2906, 2908,
			2912, 2914, 2916, 2918, 2922, 2924, 2926, 2928,
			2932, 2934, 2936, 2938, 2942, 3385, 3387, 3389,
			3391, 3393, 4502, 4708, 4712, 4714, 4732, 5013,
			5553, 5554, 5555, 5556, 5557, 5680, 5698, 6068,
			6069, 6131, 6133, 6135, 6137, 6139, 6141, 6143,
			6147, 6149, 6153, 6215, 6217, 6227, 6313, 6315,
			6317, 6324, 6326, 6328, 6330, 6408, 6412, 6418,
			6522, 6524, 6526, 6666, 6739, 6773, 8901, 8903,
			8913, 9748, 9751, 9754, 9769, 10356, 10358, 10360,
			10446, 10450, 10553, 11090, 11284, 11446, 11670, 11774,
			11775, 11780, 11798, 11799, 11800, 11810, 11814, 11816,
			11818, 11820, 11832, 11834, 11964, 11966, 12808, 12853,
			13080, 13081, 13082, 13083, 13084, 13085, 13086, 13087,
			13088, 13089, 13090, 13091, 13092, 13093, 13094, 13095,
			13096, 13097, 13098, 13099, 13100, 13101, 13241, 13280,
			13317, 13318, 19711, 20011, 20655, 20657, 20724, 22002,
			22003, 22109, 22545, 22616, 22638, 22956, 22975, 23673,
			23764, 23989, 24133, 24134, 24136, 24137, 24138, 24139,
			24140, 24141, 24142, 24143, 24157, 24158, 24160, 24161,
			24169, 24170, 24171, 24172, 24173, 24174, 24175, 24176,
			24177, 24178, 24179, 24180, 24182, 24183, 24184, 24185,
			24186, 24194, 24197, 24200, 24203, 24206, 24223, 24224,
			24533, 24589, 25173, 25176, 25177, 25643, 25975, 26376,
			26384, 26386, 26674, 26722, 26724, 26725, 26726, 26728,
			26729, 26730, 26732, 26734, 26736, 26752, 26754, 26756,
			26758, 26760, 27275, 27551, 27553, 28199, 28626, 30305,
			32312, 32328, 32336, 32344, 32352
		));
	}

	private static void addRange(Map<String, List<Integer>> m)
	{
		// RANGE — 817 items
		//   Cannon parts (cross-tag) (6), Ammunition (104), Bows (53), Crossbows
		//   (21), Thrown (41), D'hide armour (55), Raw dragonhide (cross-tag with
		//   crafting) (21), Helmets (29), Body (30), Legs (33), Boots (14), Gloves
		//   (34), Shields (15), Capes (76), Amulets (27), Rings (10), Ranging
		//   potions (8), Bait / feathers (cross-tag) (6), Combat food (cross-tag)
		//   (23), Legacy (211)
		m.put(TAG_RANGE, Arrays.asList(
			// === Cannon parts (cross-tag) ===
			2, 6, 8, 10, 12, 21728,

			// === Ammunition ===
			598, 825, 877, 882, 4773, 826, 884, 2532,
			4778, 9140, 827, 886, 2534, 4783, 9141, 4788,
			828, 888, 2536, 4793, 9142, 829, 890, 2538,
			4798, 9143, 830, 892, 2540, 4803, 9144, 11212,
			11217, 19484, 21905, 21932, 21934, 21936, 21938, 21940,
			21942, 21944, 21946, 21948, 21950, 21955, 21957, 21959,
			21961, 21963, 21965, 21967, 21969, 21971, 21973, 78,
			879, 880, 881, 2866, 4160, 4740, 8882, 9139,
			9145, 9236, 9237, 9238, 9239, 9240, 9241, 9242,
			9243, 9244, 9245, 9335, 9336, 9337, 9338, 9339,
			9340, 9341, 9342, 9706, 10142, 10143, 10144, 10145,
			10158, 10159, 11875, 21316, 21318, 21326, 21328, 22227,
			22228, 22229, 22230, 28837, 28872, 28878, 30694, 30696,

			// === Bows ===
			4212, 11748, 23901, 23902, 23903, 23983, 839, 841,
			843, 845, 847, 849, 851, 853, 855, 857,
			859, 861, 2883, 4236, 4827, 6724, 9705, 10280,
			10282, 10284, 11235, 11708, 12424, 12788, 20997, 22333,
			22547, 23357, 23855, 23856, 23857, 25865, 25867, 27610,
			27612, 27652, 27655, 27853, 28540, 28794, 29000, 29591,
			29599, 29611, 30434, 30436, 33245,

			// === Crossbows ===
			9174, 9177, 9179, 9181, 9183, 9185, 21012, 21902,
			25918, 4734, 11785, 767, 837, 8880, 9176, 10156,
			19478, 19481, 26374, 28869, 33251,

			// === Thrown ===
			800, 806, 864, 801, 807, 863, 802, 808,
			865, 869, 3093, 803, 809, 866, 804, 810,
			867, 805, 811, 868, 11230, 20849, 22804, 732,
			6522, 7170, 12926, 22634, 22636, 25849, 27912, 27916,
			28688, 28773, 28919, 28922, 29305, 30374, 31575, 31579,
			31583,

			// === D'hide armour ===
			2491, 2497, 2503, 12381, 12383, 12385, 12387, 22284,
			25493, 25494, 12508, 19930, 23200, 1065, 1099, 1135,
			2487, 2489, 2493, 2495, 2499, 2501, 7370, 7372,
			7374, 7376, 7378, 7380, 7382, 7384, 10370, 10378,
			10386, 12327, 12329, 12331, 12333, 12492, 12500, 19921,
			19924, 19927, 19933, 19936, 22275, 22278, 22281, 23188,
			23191, 23194, 23197, 23203, 23261, 23264, 23267,

			// === Raw dragonhide (cross-tag with crafting) ===
			1747, 2509, 12871, 1745, 2505, 2507, 13169, 1749,
			1751, 1753, 12865, 12867, 12869, 13161, 13163, 13165,
			13167, 13171, 23124, 27897, 32795,

			// === Helmets ===
			23886, 23887, 23888, 23971, 4732, 2581, 6131, 6326,
			10589, 11663, 11664, 11665, 13359, 22326, 22625, 22638,
			23840, 23841, 23842, 26743, 26745, 26747, 27226, 27235,
			27833, 27836, 27847, 31172, 33247,

			// === Body ===
			23889, 23890, 23891, 23975, 25496, 4736, 26384, 28256,
			1133, 6133, 6322, 8839, 10564, 10822, 10954, 11832,
			13072, 22327, 22616, 22628, 22641, 23843, 23844, 23845,
			27229, 27238, 27831, 27834, 27837, 27848,

			// === Legs ===
			23892, 23893, 23894, 23979, 4738, 26386, 28258, 6135,
			6324, 8840, 10956, 11834, 13073, 13360, 13365, 13370,
			13375, 13380, 22328, 22619, 22631, 22644, 23846, 23847,
			23848, 26755, 26759, 27232, 27241, 27832, 27835, 27838,
			27849,

			// === Boots ===
			2577, 6143, 6328, 7159, 10958, 13237, 31088, 31091,
			31092, 31093, 31094, 31095, 31096, 31097,

			// === Gloves ===
			7454, 7455, 7456, 7457, 7458, 7459, 7460, 7461,
			6149, 6330, 7453, 7462, 8842, 11118, 11120, 11122,
			11124, 11126, 11133, 11136, 11138, 11140, 11972, 11974,
			13103, 20041, 26168, 26182, 26235, 26723, 26727, 27046,
			30380, 30386,

			// === Shields ===
			4224, 11759, 23991, 3122, 3840, 3844, 6524, 10826,
			11926, 21000, 22002, 22269, 22272, 31081, 32879,

			// === Capes ===
			3789, 21285, 21295, 12261, 3759, 3761, 3763, 3765,
			3777, 3779, 3781, 3783, 3785, 3787, 4304, 4514,
			6568, 6570, 7918, 9101, 9747, 9750, 9753, 9756,
			9759, 9762, 9765, 9768, 9771, 9774, 9777, 9780,
			9783, 9786, 9789, 9792, 9795, 9798, 9801, 9804,
			9807, 9810, 9813, 9948, 10448, 12197, 12273, 12437,
			13221, 13329, 13679, 19476, 19697, 20050, 20211, 20214,
			20217, 21428, 21898, 22109, 22114, 22838, 23099, 23345,
			23351, 24855, 25502, 25585, 27363, 27374, 28902, 28947,
			28951, 28955, 31288, 31398,

			// === Amulets ===
			1704, 1706, 1708, 1710, 1712, 1718, 1729, 1731,
			6585, 10354, 11666, 11667, 11668, 11669, 11671, 11672,
			11673, 11976, 11978, 12851, 19547, 19707, 22111, 22557,
			22986, 24780, 30376,

			// === Rings ===
			6733, 11771, 12601, 13202, 19550, 19710, 21739, 21752,
			28310, 30378,

			// === Ranging potions ===
			23733, 23736, 23739, 23742, 2444, 169, 171, 173,

			// === Bait / feathers (cross-tag) ===
			314, 10087, 10088, 10089, 10090, 10091,

			// === Combat food (cross-tag) ===
			319, 315, 325, 347, 3381, 355, 333, 339,
			351, 5003, 329, 361, 379, 365, 373, 2149,
			7946, 3144, 385, 397, 391, 11936, 13441,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			616, 942, 1007, 1019, 1021, 1023, 1027, 1029,
			1031, 1052, 1063, 1095, 1097, 1129, 1169, 2533,
			2535, 2537, 2539, 2541, 2572, 4214, 4215, 4216,
			4217, 4218, 4219, 4220, 4221, 4222, 4223, 4225,
			4226, 4227, 4228, 4229, 4230, 4231, 4232, 4233,
			4234, 4315, 4317, 4319, 4321, 4323, 4325, 4327,
			4329, 4331, 4333, 4335, 4337, 4339, 4341, 4343,
			4345, 4347, 4349, 4351, 4353, 4355, 4357, 4359,
			4361, 4363, 4365, 4367, 4369, 4371, 4373, 4375,
			4377, 4379, 4381, 4383, 4385, 4387, 4389, 4391,
			4393, 4395, 4397, 4399, 4401, 4403, 4405, 4407,
			4409, 4411, 4413, 4716, 4720, 4722, 4724, 4728,
			4730, 4745, 4749, 4751, 5553, 5554, 5555, 5556,
			5557, 6809, 6959, 9757, 9977, 10033, 10069, 10071,
			10356, 10358, 10360, 10362, 10364, 10366, 10446, 10450,
			10498, 10499, 11222, 11670, 11749, 11750, 11751, 11752,
			11753, 11754, 11755, 11756, 11757, 11758, 11760, 11761,
			11762, 11763, 11764, 11765, 11766, 11767, 11768, 11769,
			11770, 11826, 11828, 11830, 11864, 11865, 11959, 11964,
			11966, 12853, 12924, 13279, 13280, 13319, 19639, 19643,
			20655, 20657, 21264, 21330, 21557, 21888, 22246, 22247,
			22248, 22249, 22550, 23985, 24133, 24134, 24135, 24177,
			24178, 24179, 24180, 24182, 24183, 24184, 24185, 24222,
			24223, 24224, 24370, 24444, 25487, 25587, 25862, 25900,
			26378, 26380, 26724, 26725, 26726, 26728, 26729, 26730,
			26744, 26746, 26748, 26756, 26760, 27365, 27376, 27544,
			27843, 28906, 28957
		));
	}

	private static void addMage(Map<String, List<Integer>> m)
	{
		// MAGE — 809 items
		//   Basic runes (16), Combo runes (6), Essence (4), Staves (115), Tomes
		//   (11), Helmets (84), Body (59), Legs (57), Boots (35), Gloves (61),
		//   Shields (28), Capes (77), Amulets (41), Rings (12), Magic potions
		//   (28), Orbs (cross-tag with crafting) (7), God cloaks (2), Combat food
		//   (cross-tag) (23), Legacy (143)
		m.put(TAG_MAGE, Arrays.asList(
			// === Basic runes ===
			554, 555, 556, 557, 558, 559, 560, 561,
			562, 563, 564, 565, 566, 9075, 21880, 28929,

			// === Combo runes ===
			4694, 4695, 4696, 4697, 4698, 4699,

			// === Essence ===
			1436, 7936, 24704, 26390,

			// === Staves ===
			6603, 12373, 30070, 4710, 30568, 12263, 1379, 1381,
			1383, 1385, 1387, 1389, 1391, 1393, 1395, 1397,
			1399, 1401, 1403, 1405, 1407, 1409, 2415, 2416,
			2417, 3053, 3054, 4170, 4675, 6526, 6562, 6563,
			6760, 6762, 6764, 6908, 6910, 6912, 6914, 7639,
			7641, 7642, 7643, 7644, 7646, 7647, 7648, 8841,
			9013, 9084, 9091, 9092, 9093, 10440, 10442, 10444,
			11709, 11787, 11789, 11998, 12000, 12199, 12275, 12422,
			12439, 12658, 13141, 13142, 13143, 13144, 20251, 20254,
			20257, 20260, 20263, 20730, 20733, 20736, 20739, 21006,
			21255, 21276, 22368, 22398, 22647, 23342, 23363, 24422,
			24423, 24424, 24425, 24699, 26945, 27624, 27676, 27679,
			27785, 27788, 27920, 29594, 29602, 29609, 30634, 31113,
			31115, 33253, 33255, 33257, 33314, 33316, 33318, 33323,
			33326, 33328, 33332,

			// === Tomes ===
			4707, 6749, 7779, 7782, 7785, 7788, 7791, 7794,
			7797, 21697, 24761,

			// === Helmets ===
			19641, 23886, 23887, 23888, 25495, 4708, 30445, 11826,
			12259, 21018, 24664, 579, 1017, 3385, 3755, 3797,
			4089, 4099, 4109, 6109, 6131, 6137, 6918, 7400,
			9068, 9069, 9096, 9729, 9733, 10342, 10452, 10454,
			10456, 10547, 10836, 11663, 11664, 11665, 11865, 12203,
			12271, 12419, 12457, 13385, 19645, 19649, 20128, 20595,
			21266, 21890, 22638, 22650, 23047, 23075, 23522, 23840,
			23841, 23842, 24288, 24444, 25398, 25413, 25900, 25906,
			25912, 26241, 26731, 26733, 26735, 26737, 26739, 26741,
			27226, 27235, 27839, 27850, 29019, 29566, 29818, 30111,
			30437, 31172, 33068, 33247,

			// === Body ===
			581, 23889, 23890, 23891, 4712, 30447, 11828, 12253,
			21021, 24666, 577, 1035, 3387, 4091, 4101, 4111,
			6107, 6139, 6916, 7399, 8839, 9070, 9097, 10330,
			10338, 10458, 10460, 10462, 10837, 12193, 12265, 12420,
			12458, 13072, 13387, 20131, 20517, 22641, 22653, 23050,
			23843, 23844, 23845, 24291, 25389, 25404, 26243, 26749,
			26751, 26753, 27167, 27238, 27837, 27840, 27848, 27851,
			29013, 29568, 30439,

			// === Legs ===
			23892, 23893, 23894, 25497, 4714, 30449, 11830, 12255,
			21024, 24668, 1033, 3389, 4093, 4103, 4113, 6108,
			6141, 6924, 7398, 8840, 9071, 9098, 10340, 10464,
			10466, 10468, 10838, 12195, 12267, 12421, 12459, 13073,
			13389, 20137, 20520, 22644, 22656, 23053, 23846, 23847,
			23848, 24294, 25401, 25416, 26245, 26755, 26757, 26759,
			27168, 27241, 27838, 27841, 27849, 27852, 29016, 29570,
			30441,

			// === Boots ===
			19930, 2579, 3393, 4097, 4107, 4117, 6106, 6147,
			6920, 9073, 9100, 10552, 10839, 13132, 13235, 13237,
			19921, 19924, 19927, 19933, 19936, 20140, 22951, 23059,
			25395, 25410, 29806, 31088, 31091, 31092, 31093, 31094,
			31095, 31096, 31097,

			// === Gloves ===
			7454, 7455, 7456, 7457, 10085, 25494, 7458, 27110,
			7459, 7460, 27111, 7461, 12506, 777, 3391, 4095,
			4105, 4115, 6110, 6153, 6922, 7453, 7462, 9072,
			9099, 10083, 10336, 10368, 10376, 10384, 11118, 11120,
			11122, 11124, 11126, 11133, 11136, 11138, 11140, 11972,
			11974, 12490, 12498, 13103, 19544, 19994, 20041, 20134,
			23056, 25392, 25407, 26168, 26182, 26723, 26727, 27046,
			27112, 30082, 30380, 30386, 31106,

			// === Shields ===
			30382, 2890, 3840, 3842, 3844, 6219, 6221, 6223,
			6225, 6229, 6231, 6233, 6235, 6889, 9731, 11924,
			12612, 12825, 20714, 21633, 25574, 25818, 25985, 26551,
			27251, 30064, 30371, 31081,

			// === Capes ===
			21285, 21295, 12261, 2412, 2413, 2414, 6111, 6568,
			6570, 7918, 9074, 9747, 9750, 9753, 9756, 9759,
			9762, 9765, 9768, 9771, 9774, 9777, 9780, 9783,
			9786, 9789, 9792, 9795, 9798, 9801, 9804, 9807,
			9810, 9813, 9948, 10448, 10498, 10499, 12197, 12273,
			12437, 13121, 13122, 13123, 13124, 13221, 13329, 13331,
			13333, 13335, 13337, 19476, 20050, 20760, 21776, 21780,
			21784, 21791, 21793, 21795, 21898, 22109, 22114, 23345,
			23351, 23603, 23605, 23607, 24855, 27363, 27374, 29289,
			29613, 29615, 29617, 31288, 33063,

			// === Amulets ===
			12257, 1478, 1704, 1706, 1708, 1710, 1712, 1718,
			1724, 1727, 1729, 1731, 6585, 8923, 9102, 10344,
			10354, 10470, 10472, 10474, 11666, 11667, 11668, 11669,
			11671, 11672, 11673, 11976, 11978, 12002, 12201, 12269,
			12851, 13393, 19707, 22111, 22557, 22986, 24780, 29893,
			30376,

			// === Rings ===
			6731, 9104, 11014, 11770, 12601, 13202, 19550, 19710,
			22975, 28313, 28329, 30378,

			// === Magic potions ===
			26340, 26342, 26344, 26346, 22449, 22452, 22455, 22458,
			24623, 24626, 24629, 24632, 23745, 23748, 23751, 23754,
			27629, 27632, 27635, 27638, 3040, 3042, 3044, 3046,
			11726, 11727, 11728, 11729,

			// === Orbs (cross-tag with crafting) ===
			567, 569, 571, 573, 575, 10973, 10980,

			// === God cloaks ===
			4041, 4042,

			// === Combat food (cross-tag) ===
			319, 315, 325, 347, 3381, 355, 333, 339,
			351, 5003, 329, 361, 379, 365, 373, 2149,
			7946, 3144, 385, 397, 391, 11936, 13441,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			616, 656, 658, 660, 662, 664, 729, 1052,
			1065, 2487, 2489, 2491, 2572, 2900, 2910, 2920,
			2930, 2940, 4736, 4738, 5553, 5554, 5555, 5556,
			5557, 6099, 6215, 6217, 6227, 7640, 7645, 8842,
			9763, 10356, 10358, 10360, 10446, 10450, 11090, 11670,
			11791, 11864, 11964, 11966, 12791, 12853, 12899, 12900,
			12902, 12904, 13102, 13280, 13392, 19639, 19643, 20655,
			20657, 20716, 20724, 21256, 21264, 21888, 22288, 22323,
			22370, 23727, 23730, 23916, 23959, 24133, 24134, 24135,
			24172, 24177, 24178, 24179, 24180, 24181, 24183, 24184,
			24185, 24222, 24223, 24224, 24232, 24233, 24234, 24248,
			24249, 24250, 24370, 24511, 25000, 25002, 25006, 25009,
			25011, 25024, 25026, 25030, 25033, 25035, 25385, 25387,
			25391, 25486, 25489, 25490, 25491, 25492, 25576, 25731,
			25987, 26724, 26725, 26726, 26728, 26729, 26730, 26732,
			26734, 26736, 26738, 26740, 26742, 26750, 26752, 26754,
			26756, 26758, 26760, 27275, 27281, 27365, 27376, 27626,
			28327, 28473, 28474, 28475, 28476, 29892, 30066
		));
	}

	private static void addPrayer(Map<String, List<Integer>> m)
	{
		// PRAYER — 338 items
		//   Bones (93), Ashes (9), Ensouled heads (23), Prayer potions (4), Super
		//   restores (10), Sanfew (4), Saradomin brews (4), Holy symbols (33),
		//   Robes (monk/proselyte/initiate/druid) (19), Bone secondaries (34),
		//   Shades remains (9), Quest-related prayer items (4), God pages
		//   (Treasure trail) (24), Prayer accessories (5), Pyre log oils (8),
		//   Legacy (55)
		m.put(TAG_PRAYER, Arrays.asList(
			// === Bones ===
			526, 528, 530, 532, 534, 536, 2391, 2859,
			3123, 3125, 3127, 3128, 3130, 3179, 3180, 3181,
			3182, 3183, 3185, 3186, 4812, 4830, 4832, 4834,
			6729, 6812, 6904, 7821, 7824, 7830, 7836, 7839,
			7845, 7848, 7851, 7863, 7866, 7869, 7872, 7881,
			7884, 7893, 7896, 7899, 7902, 7914, 10976, 10977,
			11337, 11338, 11943, 12839, 21547, 21549, 21551, 21553,
			21975, 22124, 22780, 22783, 22786, 25422, 26589, 26590,
			26591, 26592, 26593, 28899, 29344, 29346, 29348, 29350,
			29352, 29354, 29356, 29358, 29360, 29362, 29364, 29366,
			29368, 29370, 29372, 29374, 29378, 30898, 30973, 31075,
			31264, 31266, 31726, 31729, 33115,

			// === Ashes ===
			25778, 592, 1502, 8865, 25766, 25769, 25772, 25775,
			27223,

			// === Ensouled heads ===
			13511, 13448, 13451, 13454, 13457, 13460, 13463, 13466,
			13469, 13472, 13475, 13478, 13481, 13484, 13487, 13490,
			13493, 13496, 13499, 13502, 13505, 13508, 26997,

			// === Prayer potions ===
			2434, 139, 141, 143,

			// === Super restores ===
			24598, 24601, 24603, 24605, 3024, 3026, 3028, 3030,
			11493, 11495,

			// === Sanfew ===
			10925, 10927, 10929, 10931,

			// === Saradomin brews ===
			6685, 6687, 6689, 6691,

			// === Holy symbols ===
			1599, 1714, 1716, 1718, 1720, 1722, 1724, 2961,
			2963, 3840, 3842, 3844, 5508, 11640, 12608, 12610,
			12612, 13149, 13151, 13153, 13155, 13157, 13159, 13513,
			19515, 25818, 26488, 26490, 26492, 26494, 26496, 26498,
			29433,

			// === Robes (monk/proselyte/initiate/druid) ===
			538, 540, 542, 544, 5574, 5575, 5576, 9666,
			9668, 9670, 9672, 9674, 9676, 9678, 20199, 20202,
			22954, 23303, 23306,

			// === Bone secondaries ===
			4260, 4261, 11922, 22116, 31335, 2970, 4255, 4256,
			4257, 4258, 4259, 4262, 4263, 4264, 4265, 4266,
			4267, 4268, 4269, 4270, 4271, 4286, 4852, 4853,
			4854, 4855, 5615, 6728, 6810, 22754, 22756, 22758,
			30975, 31333,

			// === Shades remains ===
			0, 694, 3396, 3398, 3400, 3402, 3404, 25419,
			32445,

			// === Quest-related prayer items ===
			3839, 4850, 12601, 13202,

			// === God pages (Treasure trail) ===
			12617, 12618, 12619, 12620, 3827, 3828, 3829, 3830,
			3831, 3832, 3833, 3834, 3835, 3836, 3837, 3838,
			12613, 12614, 12615, 12616, 12621, 12622, 12623, 12624,

			// === Prayer accessories ===
			6714, 9759, 9761, 13116, 22986,

			// === Pyre log oils ===
			3422, 3424, 3426, 3428, 3430, 3432, 3434, 3436,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			1704, 2408, 2967, 3843, 3845, 3846, 3847, 4248,
			4817, 4829, 9760, 13447, 13450, 13453, 13456, 13459,
			13462, 13465, 13468, 13471, 13474, 13477, 13480, 13483,
			13486, 13489, 13492, 13495, 13498, 13501, 13504, 13507,
			13510, 18337, 19621, 19622, 19710, 20264, 20266, 20268,
			21047, 21643, 22111, 22374, 22378, 22405, 22446, 22774,
			24187, 25130, 25768, 25770, 25774, 26996, 28323
		));
	}

	private static void addCooking(Map<String, List<Integer>> m)
	{
		// COOKING — 601 items
		//   Cooking tools (10), Raw fish (25), Cooked fish (23), Pies (10), Pizzas
		//   (4), Stews & curries (3), Cakes (2), Breads (4), Gnome food /
		//   cocktails (91), Beverages (54), Burnt food (65), Containers
		//   (water/milk/etc) (18), Raw meat & ingredients (104), Misc cooked food
		//   (58), Pies (extended) (19), Harvest produce (cross-tag with farming)
		//   (55), Cooking pet & misc (4), Legacy (52)
		m.put(TAG_COOKING, Arrays.asList(
			// === Cooking tools ===
			775, 1550, 1887, 1923, 1935, 1949, 2007, 7445,
			9801, 9803,

			// === Raw fish ===
			317, 327, 3150, 345, 321, 353, 335, 341,
			349, 3379, 331, 359, 5001, 10138, 377, 363,
			371, 2148, 7944, 3142, 383, 395, 389, 13439,
			11934,

			// === Cooked fish ===
			319, 315, 325, 347, 3381, 355, 333, 339,
			351, 5003, 329, 361, 379, 365, 373, 2149,
			7946, 3144, 385, 397, 391, 11936, 13441,

			// === Pies ===
			2323, 2325, 2327, 7178, 7188, 7198, 7208, 7218,
			21690, 22795,

			// === Pizzas ===
			2289, 2293, 2297, 2301,

			// === Stews & curries ===
			2003, 2011, 7479,

			// === Cakes ===
			1891, 1897,

			// === Breads ===
			1865, 2307, 2309, 5986,

			// === Gnome food / cocktails ===
			2032, 2092, 2015, 2017, 2019, 2021, 2023, 2025,
			2026, 2028, 2030, 2034, 2036, 2038, 2040, 2042,
			2048, 2054, 2064, 2074, 2080, 2084, 2104, 2106,
			2110, 2112, 2116, 2118, 2120, 2122, 2124, 2126,
			2128, 2150, 2152, 2154, 2156, 2158, 2160, 2162,
			2164, 2165, 2166, 2167, 2169, 2171, 2173, 2175,
			2177, 2178, 2179, 2185, 2187, 2191, 2195, 2197,
			2199, 2201, 2205, 2207, 2209, 2213, 2217, 2219,
			2221, 2223, 2225, 2227, 2229, 2231, 2233, 2235,
			2237, 2239, 2241, 2243, 2245, 2249, 2251, 2253,
			2255, 2259, 2277, 2281, 2283, 2285, 2287, 2313,
			2341, 5004, 6794,

			// === Beverages ===
			1911, 5745, 5809, 5889, 712, 1905, 1907, 1909,
			1913, 1915, 1917, 1919, 2955, 3711, 4627, 5504,
			5739, 5741, 5743, 5747, 5749, 5751, 5753, 5755,
			5757, 5759, 5761, 5763, 5765, 5767, 5769, 5777,
			5785, 5793, 5801, 5817, 5825, 5833, 5841, 5849,
			5857, 5865, 5873, 5881, 5897, 5905, 5913, 5921,
			5929, 5935, 5992, 6118, 6561, 7157,

			// === Burnt food ===
			323, 375, 381, 387, 393, 399, 528, 1867,
			1903, 2005, 2013, 2144, 2146, 2247, 2305, 2311,
			2329, 2345, 2426, 3127, 3148, 3375, 3383, 4258,
			4259, 5002, 5990, 6301, 6699, 7090, 7092, 7094,
			7222, 7226, 7520, 7531, 7570, 7948, 7954, 7961,
			9982, 9990, 10140, 11938, 13443, 20718, 20869, 26637,
			26643, 26647, 29155, 29157, 29159, 29161, 31559, 31567,
			31698, 31706, 32315, 32323, 32331, 32339, 32347, 32355,
			33112,

			// === Containers (water/milk/etc) ===
			227, 229, 1783, 1823, 1825, 1827, 1829, 1921,
			1925, 1927, 1929, 1931, 1933, 1937, 1993, 2130,
			9085, 22405,

			// === Raw meat & ingredients ===
			301, 338, 1859, 1940, 1942, 1944, 1946, 1947,
			1951, 1953, 1955, 1957, 1963, 1965, 1973, 1975,
			1977, 1980, 1982, 1985, 1987, 1989, 1991, 1995,
			1997, 2001, 2009, 2102, 2108, 2114, 2132, 2134,
			2136, 2138, 2140, 2142, 2202, 2250, 2337, 2876,
			3226, 3228, 6178, 6200, 6697, 7168, 7176, 7186,
			7196, 7206, 7216, 7529, 7543, 7566, 7577, 9978,
			9986, 10087, 10816, 20855, 20857, 20859, 20861, 20863,
			20865, 20867, 20870, 20872, 20874, 20876, 20878, 20880,
			20882, 23872, 24782, 25564, 25652, 25658, 25664, 25670,
			25833, 29076, 29101, 29104, 29107, 29110, 29113, 29116,
			29119, 29122, 29125, 29216, 31553, 31561, 31686, 31692,
			31700, 32309, 32317, 32325, 32333, 32341, 32349, 33106,

			// === Misc cooked food ===
			7225, 337, 403, 1861, 1863, 1869, 1871, 1873,
			1875, 1877, 1879, 1881, 1883, 1889, 2343, 2878,
			5988, 6202, 6701, 6703, 6705, 6883, 7054, 7056,
			7058, 7060, 7062, 7064, 7066, 7068, 7070, 7072,
			7074, 7076, 7078, 7080, 7082, 7084, 7086, 7223,
			7224, 7230, 7521, 7530, 7568, 24785, 29077, 29128,
			29131, 29134, 29137, 29140, 29143, 29146, 29149, 29152,
			29217, 33109,

			// === Pies (extended) ===
			2315, 2317, 2319, 2321, 7164, 7170, 7172, 7182,
			7192, 7202, 7212, 10841, 13402, 13403, 19656, 19662,
			21684, 22789, 26247,

			// === Harvest produce (cross-tag with farming) ===
			5378, 5380, 5382, 5386, 5388, 5390, 5392, 5396,
			5398, 5400, 5402, 5406, 5408, 5410, 5412, 5416,
			5420, 5422, 5424, 5428, 5430, 5432, 5434, 5438,
			5440, 5442, 5444, 5448, 5450, 5452, 5454, 5458,
			5460, 5462, 5464, 5468, 5470, 5472, 5474, 5478,
			5960, 5962, 5964, 5968, 5970, 5972, 5974, 5976,
			5978, 5980, 5982, 5984, 6004, 6006, 6008,

			// === Cooking pet & misc ===
			1555, 13320, 13322, 20663,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			343, 357, 367, 590, 1895, 1899, 1901, 1971,
			1979, 2168, 2170, 2172, 2174, 2176, 2180, 2182,
			2184, 2880, 2881, 2883, 2884, 2885, 3151, 3326,
			3328, 4293, 4297, 4299, 6707, 6709, 6711, 6713,
			7050, 7052, 7174, 7190, 7210, 7220, 7947, 9802,
			9992, 11140, 22777, 24843, 24845, 24847, 24849, 32312,
			32328, 32336, 32344, 32352
		));
	}

	private static void addWcFletching(Map<String, List<Integer>> m)
	{
		// WC_FLETCHING — 433 items
		//   Axes (28), Logs (42), Bowstrings (3), Unstrung bows (41), Bows &
		//   shortbows (strung) (51), Crossbow parts (14), Bolt tips (12), Bolts
		//   (unfinished) (8), Bolts (finished) (39), Arrow shafts (3), Arrowtips
		//   (11), Arrows (27), Darts (20), Javelins (cross-tag with range) (9),
		//   Bird nests (1), Feathers (cross-tag with fishing) (6), Flax &
		//   secondary fletching materials (2), Tools (2), Forestry items (post-
		//   Sept 2023) (11), Capes & pet (6), Legacy (97)
		m.put(TAG_WC_FLETCHING, Arrays.asList(
			// === Axes ===
			1351, 28196, 1349, 28199, 1353, 28202, 1361, 28205,
			1355, 28208, 1357, 28211, 1359, 28214, 6739, 28217,
			23673, 28220, 13241, 13242, 10491, 20011, 23279, 23821,
			25110, 28226, 28810, 33335,

			// === Logs ===
			1511, 2862, 3438, 1521, 3440, 1519, 3442, 1517,
			3444, 6211, 6333, 10810, 1515, 3446, 6213, 6332,
			1513, 3448, 19669, 19672, 7404, 7405, 7406, 8934,
			10328, 10329, 10808, 11035, 13355, 24691, 31383, 31386,
			31389, 32467, 32539, 32541, 32671, 32744, 32902, 32904,
			32907, 32910,

			// === Bowstrings ===
			1777, 6038, 9438,

			// === Unstrung bows ===
			9454, 9457, 9459, 9461, 9463, 9465, 21921, 48,
			50, 54, 56, 58, 60, 62, 64, 66,
			68, 70, 72, 1673, 1675, 1677, 1679, 1681,
			1683, 3688, 4825, 6579, 9456, 12658, 19501, 21099,
			21102, 21105, 22542, 22547, 22552, 27652, 27657, 27662,
			33332,

			// === Bows & shortbows (strung) ===
			4212, 11748, 23901, 23902, 23903, 23983, 839, 841,
			843, 845, 847, 849, 851, 853, 855, 857,
			859, 861, 2883, 4236, 4827, 6724, 9705, 10280,
			10282, 10284, 11235, 11708, 12424, 12788, 20997, 22333,
			23357, 23855, 23856, 23857, 25865, 25867, 27610, 27612,
			27655, 27853, 28540, 28794, 29000, 29591, 29599, 29611,
			30434, 30436, 33245,

			// === Crossbow parts ===
			9420, 9423, 9425, 9427, 9429, 9431, 21918, 9440,
			9442, 9444, 9446, 9448, 9450, 9452,

			// === Bolt tips ===
			45, 46, 47, 9187, 9188, 9189, 9190, 9191,
			9192, 9193, 9194, 21338,

			// === Bolts (unfinished) ===
			9375, 9377, 9378, 9379, 9381, 21930, 9376, 9382,

			// === Bolts (finished) ===
			877, 9140, 9141, 9142, 9143, 9144, 21905, 21955,
			21957, 21959, 21961, 21963, 21965, 21967, 21969, 21971,
			21973, 879, 880, 881, 8882, 9139, 9145, 9335,
			9336, 9337, 9338, 9339, 9340, 9341, 9342, 10158,
			10159, 11875, 11876, 21316, 28872, 28878, 30696,

			// === Arrow shafts ===
			52, 53, 2864,

			// === Arrowtips ===
			39, 40, 41, 42, 43, 44, 11237, 2861,
			21350, 31047, 32448,

			// === Arrows ===
			598, 882, 884, 2532, 886, 2534, 888, 2536,
			890, 2538, 892, 2540, 11212, 11217, 78, 2865,
			2866, 4160, 7686, 9706, 21326, 21328, 22227, 22228,
			22229, 22230, 30694,

			// === Darts ===
			806, 819, 807, 820, 808, 821, 3093, 809,
			822, 810, 823, 811, 824, 11230, 11232, 25849,
			25853, 28991, 30998, 31010,

			// === Javelins (cross-tag with range) ===
			825, 826, 827, 828, 829, 830, 19484, 21318,
			22636,

			// === Bird nests ===
			5070,

			// === Feathers (cross-tag with fishing) ===
			314, 10087, 10088, 10089, 10090, 10091,

			// === Flax & secondary fletching materials ===
			1779, 9436,

			// === Tools ===
			946, 20720,

			// === Forestry items (post-Sept 2023) ===
			3700, 28136, 28143, 28149, 28169, 28171, 28173, 28175,
			28369, 28375, 28674,

			// === Capes & pet ===
			9783, 9785, 9807, 9809, 13322, 25644,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			671, 687, 1363, 1365, 1367, 1369, 1371, 1373,
			1375, 1377, 1849, 1853, 3094, 4214, 4215, 4216,
			4217, 4218, 4219, 4220, 4221, 4222, 4223, 4428,
			4718, 5071, 5072, 5073, 5074, 5075, 6589, 6904,
			7807, 9174, 9380, 9422, 9455, 9774, 9775, 9784,
			11749, 11750, 11751, 11752, 11753, 11754, 11755, 11756,
			11757, 11758, 19570, 19572, 19574, 19576, 19578, 19580,
			19581, 19582, 20727, 21352, 21964, 22550, 23185, 23675,
			23985, 25484, 25862, 28134, 28164, 28166, 28168, 28170,
			28172, 28174, 28186, 28188, 28190, 28192, 28194, 28198,
			28200, 28204, 28206, 28210, 28212, 28216, 28218, 28222,
			28223, 28279, 28280, 28281, 28282, 28283, 28284, 28285,
			28420
		));
	}

	private static void addFishing(Map<String, List<Integer>> m)
	{
		// FISHING — 103 items
		//   Rods & tools (15), Bait (8), Raw fish (25), Specialty fish (5), Angler
		//   outfit (4), Spirit angler outfit (4), Tempoross rewards (5), Fishing
		//   potions (cross-tag) (8), Cape & pet (3), Legacy (26)
		m.put(TAG_FISHING, Arrays.asList(
			// === Rods & tools ===
			21028, 23762, 21031, 301, 303, 305, 307, 309,
			311, 1585, 3157, 10129, 11323, 22844, 22846,

			// === Bait ===
			313, 314, 10087, 10088, 10089, 10090, 10091, 11940,

			// === Raw fish ===
			317, 327, 3150, 345, 321, 353, 335, 341,
			349, 3379, 331, 359, 5001, 10138, 377, 363,
			371, 2148, 7944, 3142, 383, 395, 389, 13439,
			11934,

			// === Specialty fish ===
			21293, 2149, 5003, 5004, 13339,

			// === Angler outfit ===
			13258, 13259, 13260, 13261,

			// === Spirit angler outfit ===
			25592, 25594, 25596, 25598,

			// === Tempoross rewards ===
			25574, 25578, 25580, 25582, 25588,

			// === Fishing potions (cross-tag) ===
			2438, 151, 153, 155, 31602, 31605, 31608, 31611,

			// === Cape & pet ===
			9798, 9800, 13320,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			365, 952, 3159, 6205, 9799, 13321, 21819, 22842,
			23526, 23528, 23534, 23537, 23539, 23612, 23677, 25576,
			25600, 25602, 25604, 25611, 25617, 32309, 32325, 32333,
			32341, 32349
		));
	}

	private static void addFiremaking(Map<String, List<Integer>> m)
	{
		// FIREMAKING — 104 items
		//   Tinderbox (2), Logs (42), Firelighters (6), Lanterns (17), Wintertodt
		//   (9), Cape (2), Legacy (26)
		m.put(TAG_FIREMAKING, Arrays.asList(
			// === Tinderbox ===
			590, 20720,

			// === Logs ===
			1511, 2862, 3438, 1521, 3440, 1519, 3442, 1517,
			3444, 6211, 6333, 10810, 1515, 3446, 6213, 6332,
			1513, 3448, 19669, 19672, 7404, 7405, 7406, 8934,
			10328, 10329, 10808, 11035, 13355, 24691, 31383, 31386,
			31389, 32467, 32539, 32541, 32671, 32744, 32902, 32904,
			32907, 32910,

			// === Firelighters ===
			10327, 7329, 7330, 7331, 10326, 20275,

			// === Lanterns ===
			595, 596, 4522, 4525, 4527, 4529, 4535, 4537,
			4540, 4542, 4544, 4546, 4548, 4700, 5014, 7053,
			20722,

			// === Wintertodt ===
			20693, 20695, 20696, 20704, 20706, 20708, 20710, 20712,
			20718,

			// === Cape ===
			9804, 9806,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			33, 36, 1831, 1833, 1835, 3432, 3433, 3434,
			3435, 4524, 4530, 4539, 5013, 6215, 6217, 8766,
			9064, 9805, 19671, 20697, 20699, 20701, 20703, 24361,
			24363, 25742
		));
	}

	private static void addCrafting(Map<String, List<Integer>> m)
	{
		// CRAFTING — 255 items
		//   Crafting tools (17), Thread & dyes (10), Leather (raw → tanned) (10),
		//   D'hide (27), Glass (11), Misc crafting materials (11), Pottery (10),
		//   Uncut gems (10), Cut gems (12), Jewellery (silver) (1), Jewellery
		//   (gold) (5), Jewellery (gem-set) (43), Battlestaves (10), Crafting cape
		//   & pet (2), Wool/dyes/cloth (extended) (2), Leathers (extended) (14),
		//   Pottery & wood crafting outputs (3), Legacy (57)
		m.put(TAG_CRAFTING, Arrays.asList(
			// === Crafting tools ===
			1794, 1592, 1594, 1595, 1597, 1733, 1735, 1755,
			1783, 1785, 1793, 2347, 2976, 3689, 3690, 5523,
			11065,

			// === Thread & dyes ===
			1734, 1737, 1759, 1763, 1765, 1767, 1769, 1771,
			1773, 6955,

			// === Leather (raw → tanned) ===
			948, 950, 958, 1131, 1739, 1741, 1743, 6287,
			6289, 10818,

			// === D'hide ===
			1747, 2491, 2497, 2503, 12508, 1065, 1099, 1135,
			1749, 1751, 1753, 2487, 2489, 2493, 2495, 2499,
			2501, 10370, 10378, 10386, 12492, 12500, 23261, 23264,
			23267, 27897, 32795,

			// === Glass ===
			227, 229, 401, 1775, 1781, 6667, 6958, 9085,
			10973, 10980, 21504,

			// === Misc crafting materials ===
			973, 1940, 1941, 3213, 3239, 6157, 6159, 6161,
			6163, 6165, 6167,

			// === Pottery ===
			434, 1761, 1787, 1789, 1791, 1923, 1931, 2313,
			5352, 5356,

			// === Uncut gems ===
			1617, 1619, 1621, 1623, 1625, 1627, 1629, 1631,
			6571, 19496,

			// === Cut gems ===
			411, 413, 1601, 1603, 1605, 1607, 1609, 1611,
			1613, 1615, 6573, 19493,

			// === Jewellery (silver) ===
			1796,

			// === Jewellery (gold) ===
			1635, 1654, 1673, 1692, 11069,

			// === Jewellery (gem-set) ===
			1647, 1666, 11067, 1664, 1637, 1639, 1641, 1643,
			1645, 1656, 1658, 1660, 1662, 1694, 1696, 1698,
			1700, 1702, 6575, 6577, 6581, 11072, 11076, 11085,
			11092, 11115, 11130, 19532, 19535, 19538, 19541, 21081,
			21084, 21087, 21090, 21093, 21096, 21108, 21111, 21114,
			21117, 21120, 21123,

			// === Battlestaves ===
			1393, 1395, 1397, 1399, 3053, 6562, 11787, 11998,
			20730, 20736,

			// === Crafting cape & pet ===
			9780, 9782,

			// === Wool/dyes/cloth (extended) ===
			8790, 23836,

			// === Leathers (extended) ===
			2509, 1745, 2505, 2507, 6155, 6169, 6171, 6173,
			7532, 9080, 9081, 22983, 30085, 32785,

			// === Pottery & wood crafting outputs ===
			2315, 4438, 28193,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			567, 569, 571, 573, 575, 960, 1391, 1469,
			1675, 1677, 1679, 1681, 1683, 1714, 1716, 1718,
			1722, 1724, 1795, 1797, 1799, 2349, 2351, 2353,
			2355, 2357, 2359, 2361, 2363, 5350, 5418, 5525,
			7801, 8778, 8780, 8782, 8794, 9625, 9781, 11068,
			11980, 19501, 20354, 20356, 20358, 20360, 20362, 20364,
			20384, 20386, 20388, 20390, 20392, 21039, 21041, 21043,
			21046
		));
	}

	private static void addMiningSmithing(Map<String, List<Integer>> m)
	{
		// MINING_SMITHING — 299 items
		//   Pickaxes (18), Mining tools & bags (6), Ores (21), Bars (10), Smithing
		//   outputs (8), Mining outfit (Prospector) (4), Mining/Smithing capes &
		//   pets (5), Crystal-tool/Tier-fallback pickaxes (1), Smithing armour
		//   outputs (extended) (167), Gem cutting/polishing inputs (10), Legacy
		//   (49)
		m.put(TAG_MINING_SMITHING, Arrays.asList(
			// === Pickaxes ===
			1265, 1267, 11721, 1269, 12297, 1273, 11720, 1271,
			1275, 11719, 11920, 23680, 13243, 13244, 20014, 23276,
			23822, 25112,

			// === Mining tools & bags ===
			776, 1755, 2347, 2568, 25644, 27019,

			// === Ores ===
			434, 436, 438, 1761, 6971, 6979, 6985, 6986,
			21622, 440, 668, 2892, 442, 453, 444, 13356,
			447, 9632, 449, 451, 21347,

			// === Bars ===
			2349, 2351, 9467, 2353, 2355, 2357, 13354, 2359,
			2361, 2363,

			// === Smithing outputs ===
			4819, 4820, 1539, 4821, 4822, 4823, 4824, 21728,

			// === Mining outfit (Prospector) ===
			12013, 12014, 12015, 12016,

			// === Mining/Smithing capes & pets ===
			9792, 9794, 9795, 9797, 13321,

			// === Crystal-tool/Tier-fallback pickaxes ===
			23677,

			// === Smithing armour outputs (extended) ===
			1075, 1087, 1103, 1117, 1139, 1155, 1173, 1189,
			1067, 1081, 1101, 1115, 1137, 1153, 1175, 1191,
			1069, 1083, 1105, 1119, 1141, 1157, 1177, 1193,
			1077, 1089, 1107, 1125, 1151, 1165, 1179, 1195,
			29560, 29562, 29564, 6615, 6617, 6621, 6623, 6625,
			6627, 6631, 6633, 1071, 1085, 1109, 1121, 1143,
			1159, 1181, 1197, 1073, 1091, 1111, 1123, 1145,
			1161, 1183, 1199, 1079, 1093, 1113, 1127, 1147,
			1163, 1185, 1201, 1149, 1187, 3140, 4087, 4585,
			11335, 21892, 21895, 4720, 4722, 4728, 4749, 4751,
			4759, 12470, 12472, 12474, 12476, 12478, 24421, 26382,
			26384, 26386, 28254, 28256, 28258, 2653, 2655, 2657,
			2659, 2661, 2663, 2665, 2667, 2669, 2671, 2673,
			2675, 3478, 3479, 3480, 3481, 3483, 3485, 3486,
			3488, 10346, 10348, 10352, 12460, 12462, 12464, 12466,
			12468, 12480, 12482, 12484, 12486, 12488, 12811, 12812,
			12814, 12815, 20146, 20149, 20152, 20794, 20796, 20840,
			20842, 20844, 20846, 21301, 21304, 22351, 22353, 22616,
			22619, 22625, 22628, 22631, 23242, 23787, 23789, 24034,
			24037, 24040, 25165, 26158, 26166, 26172, 26180, 26280,
			26282, 26284, 26286, 26288, 26290, 26743, 26745,

			// === Gem cutting/polishing inputs ===
			1617, 1619, 1621, 1623, 1625, 1627, 1629, 1631,
			6571, 19496,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			2, 764, 766, 1913, 3211, 5751, 6981, 9375,
			9377, 9378, 9379, 9380, 9381, 9382, 9793, 9796,
			10386, 10387, 10388, 10389, 10390, 11941, 11942, 12011,
			12012, 12019, 12797, 21343, 21345, 21390, 21539, 23682,
			24475, 24481, 24489, 25376, 25627, 27010, 27012, 27014,
			27016, 27018, 27020, 27021, 27023, 27024, 27026, 27028,
			27030
		));
	}

	private static void addHerblore(Map<String, List<Integer>> m)
	{
		// HERBLORE — 372 items
		//   Tools (4), Grimy herbs (14), Clean herbs (14), Unfinished potion
		//   variants (extended) (4), Spirits of Elid secondaries (2), Vials &
		//   secondaries (32), Unfinished potions (22), Attack potions (8),
		//   Strength potions (8), Defence potions (8), Super
		//   attack/strength/defence (12), Super combat (8), Ranging & magic (20),
		//   Prayer & restores (26), Antifire & anti-poison (68), Energy & stamina
		//   (22), Other potions (60), Cape & pet (3), Legacy (37)
		m.put(TAG_HERBLORE, Arrays.asList(
			// === Tools ===
			233, 13226, 21163, 29988,

			// === Grimy herbs ===
			199, 201, 203, 205, 207, 3049, 209, 211,
			213, 3051, 215, 2485, 217, 219,

			// === Clean herbs ===
			249, 251, 253, 255, 257, 2998, 259, 261,
			263, 3000, 265, 2481, 267, 269,

			// === Unfinished potion variants (extended) ===
			5936, 5939, 5942, 5951,

			// === Spirits of Elid secondaries ===
			6681, 6683,

			// === Vials & secondaries ===
			239, 241, 243, 187, 221, 223, 225, 227,
			229, 231, 235, 237, 245, 247, 1633, 1975,
			2398, 2970, 2972, 2974, 3138, 5937, 5940, 6016,
			6018, 6693, 9085, 9736, 10111, 11326, 11994, 22405,

			// === Unfinished potions ===
			91, 93, 95, 97, 99, 101, 103, 105,
			107, 109, 111, 2483, 3002, 3004, 20697, 22443,
			23800, 23881, 30100, 31662, 31665, 31668,

			// === Attack potions ===
			2428, 121, 123, 125, 23697, 23700, 23703, 23706,

			// === Strength potions ===
			23709, 23712, 23715, 23718, 113, 115, 117, 119,

			// === Defence potions ===
			2432, 133, 135, 137, 23721, 23724, 23727, 23730,

			// === Super attack/strength/defence ===
			2436, 145, 147, 149, 2442, 163, 165, 167,
			2440, 157, 159, 161,

			// === Super combat ===
			23685, 23688, 23691, 23694, 12695, 12697, 12699, 12701,

			// === Ranging & magic ===
			23745, 23748, 23751, 23754, 23733, 23736, 23739, 23742,
			3040, 3042, 3044, 3046, 2444, 169, 171, 173,
			11726, 11727, 11728, 11729,

			// === Prayer & restores ===
			24598, 24601, 24603, 24605, 2434, 139, 141, 143,
			2430, 127, 129, 131, 10925, 10927, 10929, 10931,
			6685, 6687, 6689, 6691, 3024, 3026, 3028, 3030,
			11493, 11495,

			// === Antifire & anti-poison ===
			12905, 12907, 12909, 12911, 12913, 12915, 12917, 12919,
			5943, 5945, 5947, 5949, 11501, 11503, 5952, 5954,
			5956, 5958, 11505, 11507, 2452, 2454, 2456, 2458,
			2446, 175, 177, 179, 25765, 25764, 25763, 25762,
			25757, 25756, 25755, 25754, 11433, 11435, 25761, 25760,
			25759, 25758, 29824, 29827, 29830, 29833, 11951, 11953,
			11955, 11957, 11960, 11962, 22209, 22212, 22215, 22218,
			22221, 22224, 21994, 21997, 21978, 21981, 21984, 21987,
			2448, 181, 183, 185,

			// === Energy & stamina ===
			3008, 3010, 3012, 3014, 31638, 31641, 31644, 31647,
			31614, 31617, 31620, 31623, 12625, 12627, 12629, 12631,
			3016, 3018, 3020, 3022, 11481, 11483,

			// === Other potions ===
			3032, 3034, 3036, 3038, 26340, 26342, 26344, 26346,
			22461, 22464, 22467, 22470, 22449, 22452, 22455, 22458,
			6470, 6472, 6474, 6476, 24635, 24638, 24641, 24644,
			24623, 24626, 24629, 24632, 2438, 151, 153, 155,
			27629, 27632, 27635, 27638, 4417, 4419, 4421, 4423,
			9998, 10000, 10002, 10004, 4842, 4844, 4846, 4848,
			31602, 31605, 31608, 31611, 31626, 31629, 31632, 31635,
			2450, 189, 191, 193,

			// === Cape & pet ===
			9774, 9776, 21509,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			3391, 6036, 9735, 9739, 9741, 9743, 9745, 9775,
			12623, 12934, 19558, 19730, 22446, 22997, 23673, 23676,
			23679, 23682, 23743, 23746, 23749, 23752, 23755, 23758,
			23761, 23764, 23767, 23770, 23773, 23776, 24478, 25387,
			25389, 25391, 25477, 27003, 27390
		));
	}

	private static void addAgilityThieving(Map<String, List<Integer>> m)
	{
		// AGILITY_THIEVING — 146 items
		//   Marks & tickets (3), Graceful set (7), Agility shortcut tools (4),
		//   Rogue equipment (6), Thieving accessories (9), Blackjacks (10),
		//   Pyramid plunder (1), Capes & pets (6), Energy / stamina potions
		//   (cross-tag) (26), Legacy (74)
		m.put(TAG_AGILITY_THIEVING, Arrays.asList(
			// === Marks & tickets ===
			2996, 11849, 29482,

			// === Graceful set ===
			11850, 11852, 11854, 11856, 11858, 11860, 30044,

			// === Agility shortcut tools ===
			88, 2203, 3105, 3107,

			// === Rogue equipment ===
			5553, 5554, 5555, 5556, 5557, 5558,

			// === Thieving accessories ===
			1523, 5560, 10075, 13121, 13122, 13123, 13124, 21143,
			22521,

			// === Blackjacks ===
			4599, 4600, 6408, 6410, 6412, 6414, 6416, 6418,
			6420, 30944,

			// === Pyramid plunder ===
			9044,

			// === Capes & pets ===
			9771, 9773, 9777, 9779, 20659, 20663,

			// === Energy / stamina potions (cross-tag) ===
			3032, 3034, 3036, 3038, 3008, 3010, 3012, 3014,
			31638, 31641, 31644, 31647, 31614, 31617, 31620, 31623,
			12625, 12627, 12629, 12631, 3016, 3018, 3020, 3022,
			11481, 11483,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			2572, 3672, 6404, 6406, 8872, 8880, 9046, 9048,
			9050, 9772, 9778, 12623, 21387, 22257, 24375, 24377,
			24379, 24381, 24383, 24385, 24709, 24711, 24713, 24719,
			24731, 24733, 24735, 24737, 24739, 24741, 25069, 25071,
			25073, 25075, 25077, 25079, 25081, 25083, 25085, 25087,
			25089, 25091, 25093, 25095, 25097, 25099, 25101, 25103,
			25105, 25107, 25109, 25111, 25113, 25115, 25117, 25119,
			25121, 25123, 25125, 25127, 25448, 27019, 27021, 27023,
			27025, 27027, 27029, 27433, 27435, 27437, 27439, 27441,
			27443, 29485
		));
	}

	private static void addSlayer(Map<String, List<Integer>> m)
	{
		// SLAYER — 184 items
		//   Slayer master items (5), Slayer rings (7), Slayer helmets (27), Black
		//   masks (18), Task-specific gear (27), Cannon (6), Cape & pet (4),
		//   Champion's Challenge scrolls (11), Legacy (79)
		m.put(TAG_SLAYER, Arrays.asList(
			// === Slayer master items ===
			4155, 5520, 21257, 21268, 21270,

			// === Slayer rings ===
			11866, 11867, 11868, 11869, 11871, 11872, 11873,

			// === Slayer helmets ===
			19639, 19641, 11864, 11865, 19643, 19645, 19647, 19649,
			21264, 21266, 21888, 21890, 23073, 23075, 24370, 24444,
			25898, 25900, 25904, 25906, 25910, 25912, 29816, 29818,
			33066, 33068, 33338,

			// === Black masks ===
			8905, 8907, 8909, 8911, 8915, 8917, 8919, 8921,
			11776, 11777, 11778, 11779, 11781, 11782, 11783, 11784,
			25276, 26781,

			// === Task-specific gear ===
			4153, 4156, 4161, 4162, 4164, 4166, 4168, 4170,
			4551, 6696, 7409, 7421, 7432, 8923, 13116, 13233,
			21255, 21724, 21736, 21739, 21742, 21752, 21754, 22951,
			22983, 22986, 23037,

			// === Cannon ===
			2, 6, 8, 10, 12, 21728,

			// === Cape & pet ===
			9786, 9788, 12647, 13262,

			// === Champion's Challenge scrolls ===
			6798, 6799, 6800, 6801, 6802, 6803, 6804, 6805,
			6806, 6807, 6808,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			985, 987, 989, 4081, 4151, 6106, 6171, 6570,
			6660, 6663, 8901, 8903, 8913, 9787, 10588, 10884,
			11135, 11732, 11736, 11774, 11775, 11780, 11870, 11874,
			12004, 12436, 12899, 12922, 12924, 12926, 12927, 18337,
			19550, 19710, 19712, 21015, 21047, 21256, 21271, 21272,
			21273, 21285, 21295, 22006, 22104, 22345, 22461, 23490,
			23491, 24187, 24495, 24498, 24502, 24551, 24553, 25177,
			25179, 25181, 25183, 25185, 25187, 25189, 25191, 25731,
			25902, 25908, 25914, 26674, 26675, 26676, 26677, 26678,
			26679, 26680, 26681, 26682, 26683, 26684, 28626
		));
	}

	private static void addFarming(Map<String, List<Integer>> m)
	{
		// FARMING — 324 items
		//   Tools (18), Compost (9), Seeds (158), Harvest produce (47), Farm
		//   outputs / materials (42), Farmer outfit (6), Cape & pet (3), Legacy
		//   (41)
		m.put(TAG_FARMING, Arrays.asList(
			// === Tools ===
			952, 5325, 5329, 5331, 5333, 5334, 5335, 5337,
			5338, 5339, 5340, 5341, 5343, 5345, 5350, 5356,
			7409, 13353,

			// === Compost ===
			6032, 6034, 6036, 6470, 6472, 6474, 6476, 21483,
			22994,

			// === Seeds ===
			4486, 6453, 6464, 22887, 4207, 6103, 9626, 23655,
			23659, 23808, 23953, 23956, 23959, 25859, 28575, 4205,
			5096, 5097, 5098, 5099, 5100, 5101, 5102, 5103,
			5104, 5105, 5106, 5280, 5281, 5282, 5283, 5284,
			5285, 5286, 5287, 5288, 5289, 5290, 5291, 5292,
			5293, 5294, 5295, 5296, 5297, 5298, 5299, 5300,
			5301, 5302, 5303, 5304, 5305, 5306, 5307, 5308,
			5309, 5310, 5311, 5312, 5313, 5314, 5315, 5316,
			5317, 5318, 5319, 5320, 5321, 5322, 5323, 5324,
			5358, 5359, 5360, 5361, 5362, 5363, 5370, 5371,
			5372, 5373, 5374, 5375, 5480, 5481, 5482, 5483,
			5484, 5485, 5486, 5487, 5496, 5497, 5498, 5499,
			5500, 5501, 5502, 5503, 6112, 6454, 6455, 6456,
			6457, 6458, 6460, 6710, 9932, 10178, 13423, 13424,
			13425, 13657, 20903, 20906, 20909, 21469, 21471, 21477,
			21480, 21486, 21488, 22848, 22850, 22856, 22859, 22862,
			22866, 22869, 22871, 22873, 22875, 22877, 22879, 22881,
			22883, 22885, 27037, 27038, 27039, 27040, 27041, 29538,
			30088, 31018, 31490, 31492, 31494, 31502, 31505, 31508,
			31541, 31543, 31545, 31547, 31549, 31551,

			// === Harvest produce ===
			5354, 5376, 5378, 5380, 5382, 5386, 5388, 5390,
			5392, 5396, 5398, 5400, 5402, 5406, 5408, 5410,
			5412, 5416, 5418, 5420, 5422, 5424, 5428, 5430,
			5432, 5434, 5438, 5440, 5442, 5444, 5448, 5450,
			5452, 5454, 5458, 5460, 5462, 5464, 5468, 5470,
			5472, 5474, 5478, 5960, 5962, 5964, 5968,

			// === Farm outputs / materials ===
			6461, 6469, 5931, 5933, 5970, 5972, 5974, 5976,
			5978, 5980, 5982, 5984, 5994, 5996, 5998, 6000,
			6002, 6004, 6006, 6008, 6010, 6012, 6014, 6020,
			6022, 6024, 6026, 6028, 6030, 6040, 6041, 6043,
			6045, 6047, 6049, 6051, 6053, 6055, 6057, 6059,
			6113, 6311,

			// === Farmer outfit ===
			1411, 13640, 13642, 13643, 13644, 13646,

			// === Cape & pet ===
			9810, 9812, 20661,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			676, 1942, 1957, 1963, 1965, 1982, 1985, 5336,
			5377, 5504, 5955, 5986, 9811, 11850, 11852, 11854,
			11856, 11858, 11860, 12623, 12625, 12627, 12629, 12631,
			13138, 13226, 13647, 13648, 13649, 13650, 14589, 22116,
			22118, 22123, 22586, 22997, 24840, 24842, 24856, 24858,
			24860
		));
	}

	private static void addRunecraft(Map<String, List<Integer>> m)
	{
		// RUNECRAFT — 124 items
		//   Talismans (24), Tiaras (20), Essence pouches (7), RC accessories (1),
		//   Essence (4), Basic runes (16), Combo runes (6), Raiments of the eye
		//   (4), Cape & pet (3), Legacy (39)
		m.put(TAG_RUNECRAFT, Arrays.asList(
			// === Talismans ===
			681, 1438, 1440, 1442, 1444, 1446, 1448, 1450,
			1452, 1454, 1456, 1458, 1460, 1462, 3696, 4023,
			5516, 13393, 22118, 26798, 28964, 28965, 28966, 28967,

			// === Tiaras ===
			5525, 5527, 5529, 5531, 5533, 5535, 5537, 5539,
			5541, 5543, 5545, 5547, 5549, 9103, 9106, 20008,
			22121, 26788, 26801, 26804,

			// === Essence pouches ===
			12791, 27281, 5509, 5510, 5512, 5514, 26784,

			// === RC accessories ===
			5521,

			// === Essence ===
			1436, 7936, 24704, 26390,

			// === Basic runes ===
			554, 555, 556, 557, 558, 559, 560, 561,
			562, 563, 564, 565, 566, 9075, 21880, 28929,

			// === Combo runes ===
			4694, 4695, 4696, 4697, 4698, 4699,

			// === Raiments of the eye ===
			26850, 26852, 26854, 26856,

			// === Cape & pet ===
			9765, 9767, 20665,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			1755, 5517, 5518, 5519, 5520, 5528, 9084, 9766,
			11095, 11097, 11099, 11101, 11103, 19627, 21538, 22332,
			22340, 22708, 24493, 24543, 26858, 26860, 26862, 26864,
			26866, 26868, 26870, 26872, 26874, 27610, 27612, 27614,
			27616, 27618, 27620, 27622, 27624, 27626, 27628
		));
	}

	private static void addHunter(Map<String, List<Integer>> m)
	{
		// HUNTER — 127 items
		//   Traps (5), Salamanders (10), Bait (8), Impling jars (12), Polar camo
		//   (2), Desert camo (2), Jungle camo (2), Generic camo outfit (3),
		//   Larupia hunter (5), Graahk hunter (5), Kyatt hunter (5),
		//   Spotted/spottier (3), Cape & pet (4), Legacy (61)
		m.put(TAG_HUNTER, Arrays.asList(
			// === Traps ===
			10006, 10008, 10010, 10025, 28626,

			// === Salamanders ===
			10148, 1939, 10142, 10143, 10144, 10145, 10146, 10147,
			10149, 28834,

			// === Bait ===
			25, 943, 2134, 2876, 3226, 5076, 9978, 9986,

			// === Impling jars ===
			11238, 11240, 11242, 11244, 11246, 11248, 11250, 11252,
			11254, 23768, 11256, 19732,

			// === Polar camo ===
			10065, 10067,

			// === Desert camo ===
			10061, 10063,

			// === Jungle camo ===
			10057, 10059,

			// === Generic camo outfit ===
			6654, 6655, 6656,

			// === Larupia hunter ===
			10041, 10043, 10045, 10095, 29226,

			// === Graahk hunter ===
			10047, 10049, 10051, 10099, 29229,

			// === Kyatt hunter ===
			10035, 10037, 10039, 10103, 29232,

			// === Spotted/spottier ===
			10069, 10071, 10075,

			// === Cape & pet ===
			9948, 9950, 13322, 13324,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			5070, 5071, 5072, 5073, 5074, 5075, 9813, 9814,
			9949, 9977, 9988, 10012, 10020, 10023, 10024, 10033,
			10042, 10053, 10054, 10055, 10056, 10066, 10068, 10073,
			10087, 10088, 10089, 10090, 10091, 10092, 10093, 10107,
			10109, 10111, 10113, 10115, 10117, 10119, 10121, 10123,
			10125, 10127, 10150, 11258, 11959, 13323, 13325, 13326,
			13327, 21306, 21337, 21338, 21339, 21344, 22446, 23783,
			25821, 28247, 28249, 28251, 28253
		));
	}

	private static void addConstruction(Map<String, List<Integer>> m)
	{
		// CONSTRUCTION — 115 items
		//   Tools (4), Planks (4), Nails (7), Construction materials (15), POH
		//   portals & telescopes (2), Bench/altar (pattern) (37), Mahogany Homes
		//   (7), POH teleports (2), Legacy (37)
		m.put(TAG_CONSTRUCTION, Arrays.asList(
			// === Tools ===
			9625, 2347, 8794, 25644,

			// === Planks ===
			960, 8778, 8780, 8782,

			// === Nails ===
			4819, 4820, 1539, 4821, 4822, 4823, 4824,

			// === Construction materials ===
			2353, 1761, 3211, 3420, 3470, 3678, 4692, 4703,
			6281, 6283, 6285, 8558, 8786, 8790, 8792,

			// === POH portals & telescopes ===
			8013, 31443,

			// === Bench/altar (pattern) ===
			8502, 8504, 8506, 8508, 8512, 8514, 8530, 8532,
			8550, 8552, 8554, 8556, 8564, 8566, 8568, 8570,
			8572, 8578, 8580, 8582, 8584, 8612, 8614, 8616,
			8618, 8620, 9843, 9844, 9845, 9852, 9853, 9854,
			9855, 9856, 9859, 9860, 9861,

			// === Mahogany Homes ===
			24872, 24874, 24876, 24878, 24880, 24882, 24885,

			// === POH teleports ===
			13117, 19476,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			1601, 1603, 1605, 1607, 1615, 1775, 2349, 2351,
			2355, 2357, 2359, 2361, 2363, 4626, 4627, 4826,
			4828, 7396, 7730, 8120, 8784, 8788, 8791, 9789,
			9790, 11815, 21388, 23321, 23322, 25614, 29155, 29156,
			29157, 29158, 29159, 29160, 29161
		));
	}

	private static void addMisc(Map<String, List<Integer>> m)
	{
		// MISC — 421 items
		//   Teleport jewellery (52), Teleport tabs (68), Boss & quest jewellery
		//   (10), Cosmetic outfits / random events (52), Clue scrolls (8), Clue
		//   tools (25), Keys (12), Storage bags (6), Utility / banked supplies
		//   (2), Holiday rares & cosmetics (56), Team capes (Castle Wars) (50),
		//   Currency (8), Legacy (72)
		m.put(TAG_MISC, Arrays.asList(
			// === Teleport jewellery ===
			1704, 1706, 1708, 1710, 1712, 2550, 2552, 2554,
			2556, 2558, 2562, 2564, 2566, 2570, 2572, 3853,
			3855, 3857, 3859, 3863, 3865, 3867, 11107, 11109,
			11111, 11113, 11118, 11120, 11122, 11124, 11126, 11190,
			11191, 11192, 11194, 11970, 11974, 11978, 11980, 11984,
			11986, 11988, 12785, 13393, 21146, 21151, 21153, 21155,
			21166, 21171, 21173, 21175,

			// === Teleport tabs ===
			8007, 8008, 8009, 8010, 8011, 8012, 8013, 11741,
			11742, 11743, 11744, 11745, 11746, 11747, 12402, 12403,
			12404, 12405, 12406, 12407, 12408, 12409, 12410, 12411,
			12642, 12775, 12776, 12777, 12778, 12779, 12780, 12781,
			12782, 12938, 13249, 19613, 19615, 19617, 19619, 19621,
			19623, 19625, 19627, 19629, 19631, 19651, 21541, 21802,
			22949, 23387, 23771, 24251, 24336, 24441, 24949, 24951,
			24953, 24955, 24957, 24959, 24961, 24963, 28790, 28824,
			29684, 29782, 30149, 31443,

			// === Boss & quest jewellery ===
			552, 1478, 1725, 1727, 1729, 11090, 12601, 13202,
			19550, 19710,

			// === Cosmetic outfits / random events ===
			626, 628, 630, 632, 634, 636, 638, 640,
			642, 644, 646, 648, 650, 652, 654, 656,
			658, 660, 662, 664, 2894, 2896, 2898, 2900,
			2902, 2904, 2906, 2908, 2910, 2912, 2914, 2916,
			2918, 2920, 2922, 2924, 2926, 2928, 2930, 2932,
			2934, 2936, 2938, 2940, 2942, 2978, 3006, 3057,
			3058, 3059, 3060, 3061,

			// === Clue scrolls ===
			713, 2677, 2722, 2801, 12073, 19835, 23182, 27427,

			// === Clue tools ===
			3450, 3451, 3452, 3453, 3454, 3455, 3456, 3457,
			3458, 3459, 3460, 3461, 3462, 3463, 3464, 3468,
			2574, 2575, 2576, 3465, 3466, 3467, 3469, 11014,
			20355,

			// === Keys ===
			989, 985, 987, 991, 993, 11942, 19677, 20756,
			21724, 22374, 23083, 23490,

			// === Storage bags ===
			5356, 11941, 13226, 13639, 24882, 25582,

			// === Utility / banked supplies ===
			954, 13660,

			// === Holiday rares & cosmetics ===
			1048, 4560, 6100, 6101, 6102, 962, 979, 981,
			1037, 1038, 1040, 1042, 1044, 1046, 1050, 1053,
			1055, 1057, 1959, 1961, 4558, 4559, 4562, 4563,
			4564, 4565, 4566, 6180, 6181, 6182, 6183, 6184,
			6185, 6186, 6187, 6188, 6822, 6828, 6834, 6840,
			6846, 6852, 6853, 6856, 6857, 6858, 6859, 6860,
			6861, 6862, 6863, 6864, 6865, 6866, 6867, 10476,

			// === Team capes (Castle Wars) ===
			4315, 4317, 4319, 4321, 4323, 4325, 4327, 4329,
			4331, 4333, 4335, 4337, 4339, 4341, 4343, 4345,
			4347, 4349, 4351, 4353, 4355, 4357, 4359, 4361,
			4363, 4365, 4367, 4369, 4371, 4373, 4375, 4377,
			4379, 4381, 4383, 4385, 4387, 4389, 4391, 4393,
			4395, 4397, 4399, 4401, 4403, 4405, 4407, 4409,
			4411, 4413,

			// === Currency ===
			617, 1464, 4278, 6306, 6529, 13204, 13307, 29482,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			563, 565, 566, 764, 766, 772, 1923, 1925,
			1935, 1937, 3861, 4447, 4670, 9084, 9469, 11105,
			11193, 11238, 11240, 11242, 11244, 11246, 11248, 11250,
			11252, 11254, 11256, 11258, 11866, 11867, 11868, 11869,
			11870, 11871, 11872, 11873, 11874, 11982, 12019, 19043,
			19564, 20372, 20374, 20376, 20378, 20380, 20786, 21149,
			21168, 21169, 21170, 21172, 21174, 21383, 21385, 21395,
			21396, 21397, 21398, 21880, 22461, 23491, 23783, 24478,
			24480, 24481, 25541, 25580, 25627, 25742, 25821, 29463
		));
	}

	private static void addQuests(Map<String, List<Integer>> m)
	{
		// QUESTS — 231 items
		//   Quest & achievement capes (8), Diary - Kandarin (4), Diary - Karamja
		//   (4), Diary - Ardougne (4), Diary - Falador (4), Diary - Fremennik (4),
		//   Diary - Wilderness (4), Diary - Morytania (4), Diary - Desert (4),
		//   Diary - Varrock (4), Diary - Western (4), Diary consumables (4), Quest
		//   unlock weapons (11), Quest cosmetic gear (20), Void Knight set (21),
		//   Fighter Torso et al. (6), Defenders (9), Boss pets (9), Legacy (103)
		m.put(TAG_QUESTS, Arrays.asList(
			// === Quest & achievement capes ===
			9813, 9814, 13070, 13221, 13223, 13280, 13281, 19476,

			// === Diary - Kandarin ===
			13137, 13138, 13139, 13140,

			// === Diary - Karamja ===
			11136, 11138, 11140, 13103,

			// === Diary - Ardougne ===
			13121, 13122, 13123, 13124,

			// === Diary - Falador ===
			13117, 13118, 13119, 13120,

			// === Diary - Fremennik ===
			13129, 13130, 13131, 13132,

			// === Diary - Wilderness ===
			13108, 13109, 13110, 13111,

			// === Diary - Morytania ===
			13112, 13113, 13114, 13115,

			// === Diary - Desert ===
			13133, 13134, 13135, 13136,

			// === Diary - Varrock ===
			13104, 13105, 13106, 13107,

			// === Diary - Western ===
			13141, 13142, 13143, 13144,

			// === Diary consumables ===
			22941, 22943, 22945, 22947,

			// === Quest unlock weapons ===
			35, 772, 2402, 2952, 4170, 4251, 9084, 10581,
			22398, 24699, 25979,

			// === Quest cosmetic gear ===
			26, 74, 75, 278, 337, 546, 548, 589,
			616, 746, 747, 1052, 2405, 4502, 6065, 6067,
			6068, 6069, 6070, 6773,

			// === Void Knight set ===
			8839, 8840, 8841, 8842, 11663, 11664, 11665, 11666,
			11667, 11668, 11669, 11671, 11672, 11673, 26463, 26465,
			26467, 26473, 26475, 26477, 32433,

			// === Fighter Torso et al. ===
			10547, 10548, 10549, 10551, 10553, 10555,

			// === Defenders ===
			8844, 8845, 8846, 8847, 8848, 8849, 8850, 12954,
			22322,

			// === Boss pets ===
			13225, 19730, 20851, 21273, 21291, 21992, 22473, 23760,
			25348,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			552, 762, 770, 1053, 1686, 1854, 2572, 3981,
			3983, 4024, 4025, 4026, 4027, 4028, 4029, 4202,
			4539, 4540, 4541, 4856, 4858, 4860, 4862, 5087,
			5093, 5525, 5573, 6450, 6570, 6583, 6704, 6737,
			9105, 9106, 9107, 9108, 9110, 9112, 9114, 9117,
			10510, 11142, 11670, 12018, 12019, 12020, 12021, 12655,
			13069, 13072, 13073, 13178, 13222, 13342, 13343, 13560,
			13561, 13562, 13563, 19477, 19478, 19479, 19820, 19825,
			19830, 20465, 20469, 20473, 20475, 20477, 20479, 20481,
			20655, 21285, 21295, 22151, 22152, 22153, 22154, 22246,
			22247, 22248, 22249, 22399, 22803, 24177, 24179, 24181,
			24182, 24183, 24184, 24185, 24443, 24670, 24671, 24672,
			25527, 27000, 27001, 27002, 27005, 27006, 27007
		));
	}

	private static void addSailing(Map<String, List<Integer>> m)
	{
		// SAILING — 49 items
		//   Navigation tools (20), Raw sailing fish (5), Cooked sailing fish (20),
		//   Cape & pet (2), Legacy (2)
		m.put(TAG_SAILING, Arrays.asList(
			// === Navigation tools ===
			32404, 31803, 31820, 31964, 31967, 31970, 31973, 31976,
			31979, 31982, 31985, 32401, 32847, 32849, 32851, 32853,
			32855, 32857, 32859, 32861,

			// === Raw sailing fish ===
			32309, 32325, 32333, 32341, 32349,

			// === Cooked sailing fish ===
			31420, 31422, 31428, 31430, 32312, 32315, 32328, 32331,
			32336, 32339, 32344, 32347, 32352, 32355, 32362, 32368,
			32374, 32377, 32380, 32383,

			// === Cape & pet ===
			31288, 31292,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			31290, 31807
		));
	}
}
