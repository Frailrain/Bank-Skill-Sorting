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
		// MELEE — 1433 items
		//   Combat utility (6), Weapons (298), Godsword construction (11), Avernic
		//   defender construction (1), Combat Achievement rewards (6), Helmets
		//   (216), Body armour (127), Legs (114), Boots (60), Gloves (66), Shields
		//   (101), Trim/gilded armour cosmetic variants (78), GE armour sets
		//   (melee) (61), Spirit shield construction (4), Crystal halberd (2),
		//   Shayzien supply armour (31), Capes (54), Amulets (44), Rings (17),
		//   Combat potions (56), Restores (cross-tag) (14), Legacy (66)
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
			12379, 1215, 4587, 20000, 1305, 27859, 21009, 7158,
			1434, 27857, 1377, 13576, 22731, 1249, 3204, 28534,
			13652, 28217, 21028, 22978, 11920, 30340, 23987, 23896,
			23895, 23897, 28220, 23762, 23861, 4747, 4726, 4755,
			4718, 21031, 21033, 33243, 24417, 11802, 29605, 20368,
			28537, 25739, 25741, 28543, 28545, 25736, 25738, 1419,
			22325, 22486, 13265, 27861, 8872, 12389, 12426, 7806,
			667, 4068, 21646, 11902, 11838, 12809, 10858, 22331,
			30367, 9703, 24617, 22613, 27904, 13108, 13109, 13110,
			13111, 7675, 20155, 11061, 7808, 25641, 28792, 27660,
			27657, 22542, 7807, 20727, 7668, 21742, 22622, 27908,
			7809, 28988, 5016, 20161, 20158, 4158, 20397, 30369,
			22610, 27900, 11889, 11824, 23850, 23849, 23851, 29796,
			29577, 4151, 26482, 26233, 11804, 20370, 12774, 11806,
			20372, 12773, 11808, 20374, 23995, 24551, 28682, 21015,
			22324, 25734, 7142, 33335, 28226, 13263, 12006, 26484,
			24880, 19675, 30955, 10129, 10887, 27855, 11705, 31248,
			24697, 5018, 11037, 7451, 27021, 23823, 23820, 28531,
			6746, 772, 28997, 23206, 30957, 7435, 21003, 29589,
			35, 33249, 3757, 7441, 29889, 4153, 7141, 19941,
			20756, 12357, 10581, 25979, 30891, 25981, 27287, 27291,
			7447, 7140, 7409, 11711, 7449, 21649, 19918, 26219,
			31049, 7445, 23528, 2402, 7443, 30759, 7439, 7437,
			24144, 22296, 11791, 29084, 30388, 6523, 6525, 33036,
			12902, 12904, 6527, 6528, 23235, 27690, 29607, 2952,
			7433, 28810,

			// === Godsword construction ===
			11810, 11794, 11796, 11798, 11800, 11812, 11814, 11816,
			11818, 11820, 11822,

			// === Avernic defender construction ===
			22477,

			// === Combat Achievement rewards ===
			25926, 25928, 25930, 25932, 25934, 25936,

			// === Helmets ===
			1139, 1155, 1137, 1153, 1141, 1157, 8682, 1151,
			1165, 8905, 8907, 8909, 8911, 8915, 8917, 8919,
			8921, 10306, 10308, 10310, 10312, 10314, 11776, 11777,
			11778, 11779, 11781, 11782, 11783, 11784, 19639, 19641,
			25276, 26781, 29560, 6621, 6623, 1143, 1159, 1145,
			1161, 10296, 10298, 10300, 10302, 10304, 22159, 1147,
			1163, 8464, 10286, 10288, 10290, 10292, 10294, 1149,
			11335, 23886, 23887, 23888, 4716, 4724, 4745, 4753,
			30445, 12476, 24192, 24419, 26382, 28254, 1167, 1169,
			2657, 2665, 2673, 3327, 3329, 3331, 3333, 3335,
			3337, 3339, 3341, 3343, 3486, 3748, 3749, 3751,
			3753, 3755, 4071, 4302, 4513, 4551, 4567, 5014,
			5574, 6128, 7539, 7917, 9068, 9069, 9096, 9629,
			9672, 10039, 10045, 10051, 10334, 10350, 10547, 10548,
			10549, 10550, 10589, 10828, 11200, 11663, 11664, 11665,
			11864, 11865, 12466, 12486, 12637, 12638, 12639, 12810,
			12813, 12929, 12931, 13137, 13138, 13139, 13140, 13196,
			13197, 13198, 13199, 13359, 13364, 13369, 13374, 13379,
			19643, 19645, 19647, 19649, 19687, 19988, 20035, 20146,
			20792, 20838, 21264, 21266, 21298, 21838, 21888, 21890,
			22625, 23073, 23075, 23101, 23258, 23785, 23840, 23841,
			23842, 24034, 24195, 24198, 24201, 24204, 24271, 24370,
			24444, 25165, 25898, 25900, 25904, 25906, 25910, 25912,
			26156, 26170, 26731, 26733, 26735, 26743, 26745, 26747,
			27042, 27235, 27836, 27839, 27844, 27847, 27850, 28933,
			29010, 29019, 29028, 29816, 29818, 30073, 30111, 30321,
			30437, 30750, 30777, 31172, 33066, 33068, 33247, 33338,

			// === Body armour ===
			1103, 1117, 1101, 1115, 1105, 1119, 1107, 1125,
			23366, 23369, 23372, 23375, 23378, 29562, 6615, 6617,
			1109, 1121, 1111, 1123, 23392, 23395, 23398, 23401,
			23404, 1113, 1127, 23209, 23212, 23215, 23218, 23221,
			3140, 21892, 23889, 23890, 23891, 4720, 4728, 4749,
			4757, 30447, 12470, 24420, 26384, 1129, 1131, 2653,
			2661, 2669, 3481, 3767, 3769, 3771, 3773, 3775,
			3793, 4069, 4298, 5575, 6129, 8839, 9070, 9097,
			9634, 9640, 9674, 10037, 10043, 10049, 10053, 10057,
			10061, 10065, 10348, 10551, 10564, 10822, 11832, 12460,
			12480, 12811, 12814, 13072, 13104, 13105, 13106, 13107,
			13361, 13366, 13371, 13376, 13381, 19689, 20038, 20149,
			20794, 20840, 21301, 22616, 22628, 23097, 23787, 23843,
			23844, 23845, 24037, 26158, 26172, 26721, 26751, 26753,
			27048, 27229, 27840, 27842, 27845, 27851, 28936, 29004,
			29013, 29022, 29280, 30076, 30439, 30753, 30779,

			// === Legs ===
			1075, 1087, 1067, 1081, 1069, 1083, 1077, 1089,
			29564, 6625, 6627, 1071, 1085, 1073, 1091, 1079,
			1093, 4087, 4585, 23892, 23893, 23894, 4722, 4730,
			4751, 4759, 30449, 12472, 12474, 24421, 26386, 1095,
			2655, 2663, 2671, 3478, 3479, 3480, 3483, 3485,
			3795, 4300, 5576, 6130, 6809, 8840, 9071, 9098,
			9636, 9642, 9676, 9678, 10035, 10041, 10047, 10055,
			10059, 10063, 10067, 10346, 10555, 10956, 11834, 12462,
			12464, 12482, 12484, 12812, 12815, 13073, 13112, 13113,
			13114, 13115, 13360, 13365, 13370, 13375, 13380, 19693,
			20044, 20796, 20842, 20844, 21304, 22619, 22631, 23095,
			23242, 23246, 23789, 23846, 23847, 23848, 24040, 26166,
			26180, 26755, 26757, 26759, 27044, 27232, 27841, 27843,
			27846, 27852, 28939, 29007, 29025, 29283, 30079, 30441,
			30756, 30781,

			// === Boots ===
			4119, 4121, 4123, 4125, 6619, 4127, 4129, 4131,
			11840, 1061, 3105, 3107, 3791, 4310, 6145, 7159,
			9005, 9006, 9073, 9100, 9638, 9644, 10552, 10958,
			11836, 12391, 13129, 13130, 13131, 13132, 13237, 13239,
			13358, 13363, 13368, 13373, 13378, 19695, 20047, 21643,
			21733, 22951, 23037, 23093, 23389, 24043, 25163, 25557,
			28672, 28945, 29286, 29806, 31088, 31091, 31092, 31093,
			31094, 31095, 31096, 31097,

			// === Gloves ===
			7454, 7455, 778, 7456, 7457, 10085, 6629, 7458,
			27110, 7459, 7460, 27111, 7461, 1059, 1580, 2997,
			3799, 4308, 6151, 7453, 7462, 7537, 8842, 8929,
			9072, 9099, 10075, 10077, 10079, 10081, 10083, 11118,
			11120, 11122, 11124, 11126, 11133, 11136, 11138, 11140,
			11972, 11974, 13103, 13357, 13362, 13367, 13372, 13377,
			19691, 19994, 20041, 21736, 22981, 23091, 24046, 26168,
			26182, 26235, 26723, 26727, 27046, 27112, 28630, 30380,
			30386, 31106,

			// === Shields ===
			1173, 1189, 8844, 1175, 1191, 8845, 1177, 1193,
			8846, 1179, 1195, 7332, 7338, 7344, 7350, 7356,
			8847, 6631, 6633, 1181, 1197, 8848, 1183, 1199,
			7334, 7340, 7346, 7352, 7358, 8849, 1185, 1201,
			7336, 7342, 7348, 7354, 7360, 8850, 1187, 1540,
			11710, 12954, 21895, 30382, 12478, 12817, 1171, 2659,
			2667, 2675, 3122, 3488, 3758, 3840, 3842, 3844,
			4072, 4156, 6219, 6221, 6223, 6225, 6229, 6231,
			6233, 6235, 8856, 9704, 10352, 11283, 11286, 11924,
			12468, 12488, 12608, 12821, 12825, 12829, 12831, 13117,
			13118, 13119, 13120, 20152, 20272, 20846, 21633, 22251,
			22254, 22257, 22260, 22263, 22266, 22322, 24266, 25985,
			27251, 27550, 27552, 31081, 33101,

			// === Trim/gilded armour cosmetic variants ===
			12205, 12207, 12209, 12211, 12213, 12215, 12217, 12219,
			12221, 12223, 12225, 12227, 12229, 12231, 12233, 12235,
			12237, 12239, 12241, 12243, 20169, 20172, 20175, 20178,
			20181, 20184, 20187, 20190, 20193, 20196, 2583, 2585,
			2587, 2589, 2591, 2593, 2595, 2597, 3472, 3473,
			12277, 12279, 12281, 12283, 12285, 12287, 12289, 12291,
			12293, 12295, 2599, 2601, 2603, 2605, 2607, 2609,
			2611, 2613, 3474, 3475, 2615, 2617, 2619, 2621,
			2623, 2625, 2627, 2629, 3476, 3477, 12414, 12415,
			12416, 12417, 12418, 22234, 22242, 22244,

			// === GE armour sets (melee) ===
			12960, 12962, 12964, 12966, 12968, 12970, 12972, 12974,
			12976, 12978, 12980, 12982, 12984, 12986, 20376, 20379,
			20382, 20385, 12988, 12990, 12992, 12994, 12996, 12998,
			13000, 13002, 13004, 13006, 13008, 13010, 13012, 13014,
			13016, 13018, 13020, 13022, 13024, 13026, 13028, 13030,
			13032, 13034, 13056, 13058, 13060, 13062, 21882, 21885,
			13052, 13054, 24488, 13036, 13038, 13040, 13042, 13044,
			13046, 13048, 13050, 21279, 23667,

			// === Spirit shield construction ===
			12819, 12823, 12827, 12833,

			// === Crystal halberd ===
			13080, 13091,

			// === Shayzien supply armour ===
			13538, 13539, 13540, 13541, 13542, 13543, 13544, 13545,
			13546, 13547, 13548, 13549, 13550, 13551, 13552, 13553,
			13554, 13555, 13556, 13557, 13558, 13559, 13560, 13561,
			13562, 13563, 13565, 13566, 13567, 13568, 13569,

			// === Capes ===
			21285, 21295, 12261, 6568, 6570, 7918, 9747, 9749,
			9750, 9752, 9753, 9755, 9756, 9759, 9762, 9765,
			9768, 9770, 9771, 9774, 9777, 9780, 9783, 9786,
			9789, 9792, 9795, 9798, 9801, 9804, 9807, 9810,
			9813, 9948, 10448, 12197, 12273, 12437, 13121, 13122,
			13123, 13124, 13221, 13329, 19476, 20050, 20760, 22114,
			23345, 23351, 24855, 31288, 31398, 33063,

			// === Amulets ===
			1478, 1704, 1706, 1708, 1710, 1712, 1718, 1724,
			1725, 1729, 1731, 4081, 6585, 10354, 10362, 10364,
			10588, 11128, 11666, 11667, 11668, 11669, 11671, 11672,
			11673, 11976, 11978, 12017, 12018, 12436, 12851, 19553,
			19707, 20366, 22111, 22557, 22986, 23240, 23309, 23354,
			24780, 29801, 29804, 30376,

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

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			315, 319, 325, 329, 333, 339, 347, 351,
			355, 361, 365, 373, 379, 385, 391, 397,
			2149, 3144, 3381, 5003, 7946, 11936, 12596, 13441,
			19921, 19924, 19927, 19930, 19933, 19936, 21018, 21021,
			21024, 22272, 22275, 22278, 22281, 22284, 22650, 22653,
			22656, 23188, 23191, 23194, 23197, 23200, 23203, 23261,
			23264, 23267, 24664, 24666, 24668, 25389, 25392, 25395,
			25398, 25401, 25404, 25407, 25410, 25413, 25416, 26241,
			26243, 26245
		));
	}

	private static void addRange(Map<String, List<Integer>> m)
	{
		// RANGE — 653 items
		//   Cannon parts (cross-tag) (6), Ward shards (construction intermediates)
		//   (3), Ammunition (104), Bows (55), Crossbows (23), Thrown (41), D'hide
		//   armour (55), Raw dragonhide (cross-tag with crafting) (21), Helmets
		//   (36), Body (32), Legs (42), Boots (14), Gloves (41), Shields (15),
		//   Capes (76), Amulets (30), Rings (10), Ranging potions (12), Bait /
		//   feathers (cross-tag) (6), Legacy (31)
		m.put(TAG_RANGE, Arrays.asList(
			// === Cannon parts (cross-tag) ===
			2, 6, 8, 10, 12, 21728,

			// === Ward shards (construction intermediates) ===
			11928, 11929, 11930,

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
			22547, 23357, 23855, 23856, 23857, 25865, 25867, 26237,
			26239, 27610, 27612, 27652, 27655, 27853, 28540, 28794,
			29000, 29591, 29599, 29611, 30434, 30436, 33245,

			// === Crossbows ===
			9174, 9177, 9179, 9181, 9183, 9185, 26486, 21012,
			21902, 25916, 25918, 4734, 11785, 767, 837, 8880,
			9176, 10156, 19478, 19481, 26374, 28869, 33251,

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
			23886, 23887, 23888, 23971, 4732, 11826, 12512, 2581,
			6131, 6326, 10334, 10374, 10382, 10390, 11663, 11664,
			11665, 12496, 12504, 13359, 22326, 22638, 23840, 23841,
			23842, 26743, 26745, 26747, 27226, 27235, 27833, 27836,
			27847, 29010, 31172, 33247,

			// === Body ===
			23889, 23890, 23891, 23975, 25496, 4736, 11828, 28256,
			1133, 6133, 6322, 7362, 7364, 8839, 10330, 10822,
			10954, 12596, 13072, 22327, 22641, 23381, 23843, 23844,
			23845, 27229, 27238, 27831, 27834, 27837, 27848, 29004,

			// === Legs ===
			23892, 23893, 23894, 23979, 4738, 11830, 12510, 28258,
			6135, 6324, 7366, 7368, 8840, 10332, 10372, 10380,
			10388, 10956, 12494, 12502, 13073, 13360, 13365, 13370,
			13375, 13380, 22328, 22644, 23249, 23384, 23846, 23847,
			23848, 26755, 26759, 27232, 27241, 27832, 27835, 27838,
			27849, 29007,

			// === Boots ===
			2577, 6143, 6328, 7159, 10958, 13237, 31088, 31091,
			31092, 31093, 31094, 31095, 31096, 31097,

			// === Gloves ===
			7454, 7455, 7456, 7457, 7458, 7459, 7460, 7461,
			12506, 6149, 6330, 7453, 7462, 8842, 10336, 10368,
			10376, 10384, 11118, 11120, 11122, 11124, 11126, 11133,
			11136, 11138, 11140, 11972, 11974, 12490, 12498, 13103,
			20041, 26168, 26182, 26235, 26723, 26727, 27046, 30380,
			30386,

			// === Shields ===
			4224, 11759, 23991, 3122, 3840, 3844, 6524, 10826,
			11926, 21000, 22002, 22269, 22272, 31081, 32879,

			// === Capes ===
			3789, 21285, 21295, 12261, 3759, 3761, 3763, 3765,
			3777, 3779, 3781, 3783, 3785, 3787, 4304, 4514,
			6568, 6570, 7918, 9747, 9750, 9753, 9756, 9758,
			9759, 9762, 9765, 9768, 9770, 9771, 9774, 9777,
			9780, 9783, 9786, 9789, 9792, 9795, 9798, 9801,
			9804, 9807, 9810, 9813, 9948, 10448, 12197, 12273,
			12437, 13221, 13329, 13679, 19476, 19697, 20050, 20211,
			20214, 20217, 21428, 21898, 22109, 22114, 22838, 23099,
			23345, 23351, 24855, 25502, 27363, 27374, 28902, 28947,
			28951, 28955, 31288, 31398,

			// === Amulets ===
			1704, 1706, 1708, 1710, 1712, 1718, 1729, 1731,
			6585, 10354, 10362, 11666, 11667, 11668, 11669, 11671,
			11672, 11673, 11976, 11978, 12436, 12851, 19547, 19707,
			22111, 22249, 22557, 22986, 24780, 30376,

			// === Rings ===
			6733, 11771, 12601, 13202, 19550, 19710, 21739, 21752,
			28310, 30378,

			// === Ranging potions ===
			23733, 23736, 23739, 23742, 2444, 169, 171, 173,
			11722, 11723, 11724, 11725,

			// === Bait / feathers (cross-tag) ===
			314, 10087, 10088, 10089, 10090, 10091,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			315, 319, 325, 329, 333, 339, 347, 351,
			355, 361, 365, 373, 379, 385, 391, 397,
			2149, 3144, 3381, 5003, 7946, 11936, 13441, 22616,
			22619, 22625, 22628, 22631, 25585, 26384, 26386
		));
	}

	private static void addMage(Map<String, List<Integer>> m)
	{
		// MAGE — 731 items
		//   Basic runes (16), Combo runes (6), Essence (4), Staves (134), Ward
		//   shards (construction intermediates) (3), Tomes (11), Hallowed
		//   Sepulchre tomes + pages (6), Helmets (86), Body (60), Legs (59), Boots
		//   (29), Gloves (42), Shields (28), Capes (75), Amulets (45), Rings (12),
		//   Magic potions (28), Orbs (cross-tag with crafting) (7), God cloaks
		//   (2), Spell tablets (POH lectern) (9), Magic level boost (1), CoX mage
		//   upgrades + Ancestral (2), Nightmare orbs + spell sacks (9),
		//   Runescrolls (spell scrolls) (2), Mage GE armour sets (7), Bracelet
		//   ornament variants (1), Legacy (47)
		m.put(TAG_MAGE, Arrays.asList(
			// === Basic runes ===
			554, 555, 556, 557, 558, 559, 560, 561,
			562, 563, 564, 565, 566, 9075, 21880, 28929,

			// === Combo runes ===
			4694, 4695, 4696, 4697, 4698, 4699,

			// === Essence ===
			1436, 7936, 24704, 26390,

			// === Staves ===
			6603, 12373, 30070, 23898, 23899, 23900, 4710, 30568,
			12263, 1379, 1381, 1383, 1385, 1387, 1389, 1391,
			1393, 1395, 1397, 1399, 1401, 1403, 1405, 1407,
			1409, 2415, 2416, 2417, 3053, 3054, 4170, 4675,
			6526, 6562, 6563, 6760, 6762, 6764, 6908, 6910,
			6912, 6914, 7639, 7641, 7642, 7643, 7644, 7646,
			7647, 7648, 8841, 9013, 9084, 9091, 9092, 9093,
			10440, 10442, 10444, 11709, 11787, 11789, 11905, 11908,
			11998, 12000, 12199, 12275, 12422, 12439, 12658, 12899,
			12900, 13141, 13142, 13143, 13144, 20251, 20254, 20257,
			20260, 20263, 20730, 20733, 20736, 20739, 21006, 21255,
			21276, 22288, 22290, 22292, 22294, 22323, 22368, 22398,
			22481, 22555, 22647, 23342, 23363, 23852, 23853, 23854,
			24422, 24423, 24424, 24425, 24699, 25731, 25733, 26945,
			27624, 27676, 27679, 27785, 27788, 27920, 29594, 29602,
			29609, 30634, 31113, 31115, 33253, 33255, 33257, 33314,
			33316, 33318, 33323, 33326, 33328, 33332,

			// === Ward shards (construction intermediates) ===
			11931, 11932, 11933,

			// === Tomes ===
			4707, 6749, 7779, 7782, 7785, 7788, 7791, 7794,
			7797, 21697, 24761,

			// === Hallowed Sepulchre tomes + pages ===
			23504, 23506, 23508, 23510, 23512, 23514,

			// === Helmets ===
			12453, 12455, 19641, 23886, 23887, 23888, 25495, 4708,
			30445, 12259, 21018, 24664, 579, 1017, 3385, 3755,
			3797, 4089, 4099, 4109, 6109, 6131, 6137, 6918,
			7394, 7396, 7400, 9068, 9069, 9096, 9729, 9733,
			10342, 10452, 10454, 10456, 10547, 10836, 11663, 11664,
			11665, 11865, 12203, 12271, 12419, 12457, 13385, 19645,
			19649, 20128, 20595, 21266, 21890, 22650, 23047, 23075,
			23522, 23840, 23841, 23842, 24288, 24444, 25398, 25413,
			25900, 25906, 25912, 26241, 26731, 26733, 26735, 26737,
			26739, 26741, 27226, 27235, 27839, 27850, 29019, 29566,
			29818, 30111, 30437, 31172, 33068, 33247,

			// === Body ===
			581, 12449, 12451, 23889, 23890, 23891, 4712, 30447,
			12253, 21021, 24666, 577, 1035, 3387, 4091, 4101,
			4111, 6107, 6139, 6916, 7390, 7392, 7399, 8839,
			9070, 9097, 10338, 10458, 10460, 10462, 10837, 12193,
			12265, 12420, 12458, 13072, 13387, 20131, 20517, 22653,
			23050, 23843, 23844, 23845, 24291, 25389, 25404, 26243,
			26749, 26751, 26753, 27167, 27238, 27837, 27840, 27848,
			27851, 29013, 29568, 30439,

			// === Legs ===
			12445, 12447, 23892, 23893, 23894, 25497, 4714, 30449,
			12255, 21024, 24668, 1033, 3389, 4093, 4103, 4113,
			6108, 6141, 6924, 7386, 7388, 7398, 8840, 9071,
			9098, 10340, 10464, 10466, 10468, 10838, 12195, 12267,
			12421, 12459, 13073, 13389, 20137, 20520, 22656, 23053,
			23846, 23847, 23848, 24294, 25401, 25416, 26245, 26755,
			26757, 26759, 27168, 27241, 27838, 27841, 27849, 27852,
			29016, 29570, 30441,

			// === Boots ===
			2579, 3393, 4097, 4107, 4117, 6106, 6147, 6920,
			9073, 9100, 10552, 10839, 13132, 13235, 13237, 20140,
			22951, 23059, 25395, 25410, 29806, 31088, 31091, 31092,
			31093, 31094, 31095, 31096, 31097,

			// === Gloves ===
			25494, 27110, 27111, 777, 3391, 4095, 4105, 4115,
			6110, 6153, 6922, 9072, 9099, 11118, 11120, 11122,
			11124, 11126, 11133, 11136, 11138, 11140, 11972, 11974,
			13103, 19544, 19994, 20041, 20134, 23056, 25392, 25407,
			26168, 26182, 26723, 26727, 27046, 27112, 30082, 30380,
			30386, 31106,

			// === Shields ===
			30382, 2890, 3840, 3842, 3844, 6219, 6221, 6223,
			6225, 6229, 6231, 6233, 6235, 6889, 9731, 11924,
			12612, 12825, 20714, 21633, 25574, 25818, 25985, 26551,
			27251, 30064, 30371, 31081,

			// === Capes ===
			21285, 21295, 12261, 2412, 2413, 2414, 6111, 6568,
			6570, 7918, 9074, 9101, 9747, 9750, 9753, 9759,
			9762, 9764, 9765, 9768, 9770, 9771, 9774, 9777,
			9780, 9783, 9786, 9789, 9792, 9795, 9798, 9801,
			9804, 9807, 9810, 9813, 10448, 12197, 12273, 12437,
			13121, 13122, 13123, 13124, 13221, 13329, 13331, 13333,
			13335, 13337, 19476, 20050, 20760, 21776, 21780, 21784,
			21791, 21793, 21795, 21898, 22114, 23345, 23351, 23603,
			23605, 23607, 24855, 27363, 27374, 29289, 29613, 29615,
			29617, 31288, 33063,

			// === Amulets ===
			12257, 1478, 1704, 1706, 1708, 1710, 1712, 1718,
			1724, 1727, 1729, 1731, 6585, 8923, 9102, 10344,
			10354, 10362, 10366, 10470, 10472, 10474, 11666, 11667,
			11668, 11669, 11671, 11672, 11673, 11976, 11978, 12002,
			12201, 12269, 12436, 12851, 13393, 19707, 19720, 22111,
			22557, 22986, 24780, 29893, 30376,

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

			// === Spell tablets (POH lectern) ===
			8014, 8015, 8016, 8017, 8018, 8019, 8020, 8021,
			8022,

			// === Magic level boost ===
			20724,

			// === CoX mage upgrades + Ancestral ===
			21049, 21043,

			// === Nightmare orbs + spell sacks ===
			24511, 24514, 24517, 24607, 24609, 24611, 24613, 24615,
			24621,

			// === Runescrolls (spell scrolls) ===
			25478, 25481,

			// === Mage GE armour sets ===
			23110, 23113, 23116, 23119, 23336, 23339, 24333,

			// === Bracelet ornament variants ===
			23444,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			315, 319, 325, 329, 333, 339, 347, 351,
			355, 361, 365, 373, 379, 385, 391, 397,
			2149, 3144, 3381, 5003, 7453, 7454, 7455, 7456,
			7457, 7458, 7459, 7460, 7461, 7462, 7946, 9756,
			9948, 10498, 10499, 11936, 13441, 19921, 19924, 19927,
			19930, 19933, 19936, 22109, 22638, 22641, 22644
		));
	}

	private static void addPrayer(Map<String, List<Integer>> m)
	{
		// PRAYER — 335 items
		//   Bones (93), Ashes (9), Ensouled heads (23), Prayer potions (4), Super
		//   restores (10), Sanfew (4), Saradomin brews (4), Holy symbols (37),
		//   Blessings (Treasure Trails) (8), Prayer scrolls + cosmetic (5), Fossil
		//   Island fossils (25), Robes (monk/proselyte/initiate/druid) (27), Bone
		//   secondaries (36), Shades remains (9), Quest-related prayer items (4),
		//   God pages (Treasure trail) (24), Prayer accessories (5), Pyre log oils
		//   (8)
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
			19515, 22433, 22435, 24693, 24695, 25818, 26488, 26490,
			26492, 26494, 26496, 26498, 29433,

			// === Blessings (Treasure Trails) ===
			30384, 20220, 20223, 20226, 20229, 20232, 20235, 20747,

			// === Prayer scrolls + cosmetic ===
			21034, 21047, 21079, 21157, 25781,

			// === Fossil Island fossils ===
			21562, 21564, 21566, 21568, 21570, 21572, 21574, 21576,
			21578, 21580, 21582, 21584, 21586, 21588, 21600, 21602,
			21604, 21606, 21608, 21610, 21612, 21614, 21616, 21618,
			21620,

			// === Robes (monk/proselyte/initiate/druid) ===
			538, 540, 542, 544, 4078, 5574, 5575, 5576,
			9666, 9668, 9670, 9672, 9674, 9676, 9678, 12598,
			12600, 19997, 20199, 20202, 22954, 23303, 23306, 25434,
			25436, 25438, 25440,

			// === Bone secondaries ===
			4260, 4261, 11922, 22116, 31335, 2970, 4255, 4256,
			4257, 4258, 4259, 4262, 4263, 4264, 4265, 4266,
			4267, 4268, 4269, 4270, 4271, 4286, 4852, 4853,
			4854, 4855, 5615, 6728, 6810, 21726, 22754, 22756,
			22758, 25139, 30975, 31333,

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
			3422, 3424, 3426, 3428, 3430, 3432, 3434, 3436
		));
	}

	private static void addCooking(Map<String, List<Integer>> m)
	{
		// COOKING — 639 items
		//   Cooking tools (18), Raw fish (25), Cooked fish (23), Pies (10), Pizzas
		//   (4), Stews & curries (3), Cakes (2), Breads (4), Gnome food /
		//   cocktails (91), Beverages (58), Burnt food (65), Containers
		//   (water/milk/etc) (18), Raw meat & ingredients (119), Misc cooked food
		//   (121), Pies (extended) (19), Harvest produce (cross-tag with farming)
		//   (55), Cooking pet & misc (1), Legacy (3)
		m.put(TAG_COOKING, Arrays.asList(
			// === Cooking tools ===
			775, 1550, 1887, 1923, 1935, 1949, 1980, 2007,
			4244, 7445, 7688, 7690, 7691, 7700, 7702, 7738,
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
			5929, 5935, 5992, 6118, 6561, 7157, 10848, 10849,
			10850, 10851,

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
			1977, 1982, 1985, 1987, 1989, 1991, 1995, 1997,
			2001, 2009, 2102, 2108, 2114, 2132, 2134, 2136,
			2138, 2140, 2142, 2202, 2250, 2337, 2876, 3226,
			3228, 6178, 6200, 6697, 7168, 7176, 7186, 7196,
			7206, 7216, 7480, 7481, 7482, 7483, 7484, 7485,
			7486, 7487, 7488, 7489, 7490, 7491, 7492, 7493,
			7494, 7495, 7529, 7543, 7566, 7577, 9978, 9986,
			10087, 10816, 20855, 20857, 20859, 20861, 20863, 20865,
			20867, 20870, 20872, 20874, 20876, 20878, 20880, 20882,
			23872, 24782, 25564, 25652, 25658, 25664, 25670, 25833,
			29076, 29101, 29104, 29107, 29110, 29113, 29116, 29119,
			29122, 29125, 29216, 31553, 31561, 31686, 31692, 31700,
			32309, 32317, 32325, 32333, 32341, 32349, 33106,

			// === Misc cooked food ===
			7225, 25960, 337, 403, 1861, 1863, 1869, 1871,
			1873, 1875, 1877, 1879, 1881, 1883, 1889, 2343,
			2878, 5988, 6202, 6701, 6703, 6705, 6883, 7054,
			7056, 7058, 7060, 7062, 7064, 7066, 7068, 7070,
			7072, 7074, 7076, 7078, 7080, 7082, 7084, 7086,
			7223, 7224, 7230, 7473, 7476, 7521, 7527, 7528,
			7530, 7544, 7545, 7546, 7568, 7572, 7573, 7574,
			7575, 7579, 7580, 9475, 9980, 9984, 9988, 9992,
			9994, 9996, 10960, 10961, 10962, 10963, 10964, 10965,
			10966, 10967, 10968, 10969, 10970, 10971, 13426, 13427,
			13428, 19653, 20856, 20858, 20860, 20862, 20864, 20866,
			20868, 20871, 20873, 20875, 20877, 20879, 20881, 20883,
			21693, 23874, 24589, 24592, 24595, 24785, 25565, 25566,
			25654, 25660, 25666, 25672, 25958, 29077, 29128, 29131,
			29134, 29137, 29140, 29143, 29146, 29149, 29152, 29217,
			33109,

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
			1555,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			13320, 13322, 20663
		));
	}

	private static void addWcFletching(Map<String, List<Integer>> m)
	{
		// WC_FLETCHING — 357 items
		//   Axes (23), Axe ornament variants (2), Logs (42), Bowstrings (3),
		//   Unstrung bows (41), Bows & shortbows (strung) (51), Crossbow parts
		//   (18), Ballista construction (MM2) (5), Bolt tips (12), Bolts
		//   (unfinished) (9), Bolts (finished) (39), Arrow shafts (3), Arrowtips
		//   (12), Arrows (27), Darts (20), Javelins (cross-tag with range) (9),
		//   Bird nests (3), Feathers (cross-tag with fishing) (6), Flax &
		//   secondary fletching materials (2), Tools (2), Forestry items (post-
		//   Sept 2023) (15), Capes & pet (6), Lumberjack outfit (4), Sulliuscep +
		//   exotic wood (1), Legacy (2)
		m.put(TAG_WC_FLETCHING, Arrays.asList(
			// === Axes ===
			1351, 28196, 1349, 28199, 1353, 28202, 1361, 28205,
			1355, 28208, 1357, 28211, 1359, 28214, 6739, 23673,
			13241, 13242, 10491, 20011, 23279, 23821, 25110,

			// === Axe ornament variants ===
			25378, 25066,

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
			9420, 9423, 9425, 9427, 9429, 9431, 21918, 9422,
			9433, 9434, 9440, 9442, 9444, 9446, 9448, 9450,
			9452, 21952,

			// === Ballista construction (MM2) ===
			19595, 19598, 19601, 19604, 19607,

			// === Bolt tips ===
			45, 46, 47, 9187, 9188, 9189, 9190, 9191,
			9192, 9193, 9194, 21338,

			// === Bolts (unfinished) ===
			9375, 9377, 9378, 9379, 9380, 9381, 21930, 9376,
			9382,

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
			11874, 21350, 31047, 32448,

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
			5070, 12792, 12793,

			// === Feathers (cross-tag with fishing) ===
			314, 10087, 10088, 10089, 10090, 10091,

			// === Flax & secondary fletching materials ===
			1779, 9436,

			// === Tools ===
			946, 20720,

			// === Forestry items (post-Sept 2023) ===
			28217, 28220, 3700, 28136, 28143, 28149, 28169, 28171,
			28173, 28175, 28177, 28226, 28369, 28375, 28674,

			// === Capes & pet ===
			9783, 9785, 9807, 9809, 13322, 25644,

			// === Lumberjack outfit ===
			10933, 10939, 10940, 10941,

			// === Sulliuscep + exotic wood ===
			21626,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			28810, 33335
		));
	}

	private static void addFishing(Map<String, List<Integer>> m)
	{
		// FISHING — 110 items
		//   Rods & tools (23), Aerial fishing catches + byproducts (7), Bait (11),
		//   Raw fish (25), Specialty fish (11), Angler outfit (4), Spirit angler
		//   outfit (4), Tempoross rewards (6), Fishing potions (cross-tag) (8),
		//   Trophy fish (POH mountable) (8), Cape & pet (3)
		m.put(TAG_FISHING, Arrays.asList(
			// === Rods & tools ===
			21028, 25373, 23762, 21031, 21033, 25059, 301, 303,
			305, 307, 309, 311, 1585, 3157, 10129, 11323,
			21652, 22816, 22842, 22844, 22846, 23122, 25114,

			// === Aerial fishing catches + byproducts ===
			22818, 22820, 22826, 22829, 22832, 22835, 22840,

			// === Bait ===
			313, 314, 10087, 10088, 10089, 10090, 10091, 11940,
			13430, 13431, 13432,

			// === Raw fish ===
			317, 327, 3150, 345, 321, 353, 335, 341,
			349, 3379, 331, 359, 5001, 10138, 377, 363,
			371, 2148, 7944, 3142, 383, 395, 389, 13439,
			11934,

			// === Specialty fish ===
			21293, 2149, 5003, 5004, 11324, 11328, 11330, 11332,
			11334, 13339, 21356,

			// === Angler outfit ===
			13258, 13259, 13260, 13261,

			// === Spirit angler outfit ===
			25592, 25594, 25596, 25598,

			// === Tempoross rewards ===
			25574, 25578, 25580, 25582, 25585, 25588,

			// === Fishing potions (cross-tag) ===
			2438, 151, 153, 155, 31602, 31605, 31608, 31611,

			// === Trophy fish (POH mountable) ===
			7989, 7990, 7991, 7992, 7993, 7994, 25559, 25561,

			// === Cape & pet ===
			9798, 9800, 13320
		));
	}

	private static void addFiremaking(Map<String, List<Integer>> m)
	{
		// FIREMAKING — 81 items
		//   Tinderbox (2), Logs (42), Firelighters (6), Lanterns (17), Wintertodt
		//   (11), Lovakengj charcoal (1), Cape (2)
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
			20693, 20695, 20696, 20698, 20704, 20706, 20708, 20710,
			20712, 20718, 24554,

			// === Lovakengj charcoal ===
			13570,

			// === Cape ===
			9804, 9806
		));
	}

	private static void addCrafting(Map<String, List<Integer>> m)
	{
		// CRAFTING — 256 items
		//   Crafting tools (17), Thread & dyes (10), Leather (raw → tanned) (11),
		//   D'hide (27), Glass (11), Misc crafting materials (13), Pottery (10),
		//   Uncut gems (10), Cut gems (13), Jewellery (silver) (1), Jewellery
		//   (gold) (5), Jewellery (gem-set) (43), Battlestaves (10), Crafting cape
		//   & pet (2), Wool/dyes/cloth (extended) (2), Leathers (extended) (14),
		//   Pottery & wood crafting outputs (3), Legacy (54)
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
			6289, 10818, 10820,

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
			6163, 6165, 6167, 13383, 22935,

			// === Pottery ===
			434, 1761, 1787, 1789, 1791, 1923, 1931, 2313,
			5352, 5356,

			// === Uncut gems ===
			1617, 1619, 1621, 1623, 1625, 1627, 1629, 1631,
			6571, 19496,

			// === Cut gems ===
			411, 413, 1601, 1603, 1605, 1607, 1609, 1611,
			1613, 1615, 6573, 19493, 19529,

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
			12526, 12528, 12530, 12532, 12534, 12536, 12538, 12798,
			12800, 20002, 20062, 20065, 20068, 20071, 20074, 20077,
			20143, 21202, 22231, 22236, 22239, 22246, 23227, 23232,
			23237, 23348, 24670, 25090, 25099, 25742, 25744, 26421,
			26479, 26528, 26541, 26707, 26709, 26711, 26713, 26717,
			27098, 27113, 27121, 27255, 28017, 28336, 28684, 28690,
			30432, 30443, 30451, 33305, 33308, 33311
		));
	}

	private static void addMiningSmithing(Map<String, List<Integer>> m)
	{
		// MINING_SMITHING — 272 items
		//   Pickaxes (18), Mining tools & bags (9), Varrock armour (Varrock Diary
		//   mining buff) (4), Ores (21), Bars (10), Smithing outputs (8), Lunar
		//   Diplomacy ores/bars (2), Daeyalt (Sins of the Father) (1), Motherlode
		//   Mine (3), Lovakengj mining (sulphur + dynamite) (4), Shooting Star
		//   miner (3), Golden prospector outfit (4), Camdozaal mining (Barronite)
		//   (4), Volcanic Mine (ore fragments) (7), Zalcano (tephra + shards) (4),
		//   Mining outfit (Prospector) (4), Mining/Smithing capes & pets (5),
		//   Crystal-tool/Tier-fallback pickaxes (2), Smithing armour outputs
		//   (extended) (115), Gem cutting/polishing inputs (10), Legacy (34)
		m.put(TAG_MINING_SMITHING, Arrays.asList(
			// === Pickaxes ===
			1265, 1267, 11721, 1269, 12297, 1273, 11720, 1271,
			1275, 11719, 11920, 23680, 13243, 13244, 20014, 23276,
			23822, 25112,

			// === Mining tools & bags ===
			776, 1755, 2347, 2568, 21343, 21345, 21392, 25644,
			27019,

			// === Varrock armour (Varrock Diary mining buff) ===
			13104, 13105, 13106, 13107,

			// === Ores ===
			434, 436, 438, 1761, 6971, 6979, 6985, 6986,
			21622, 440, 668, 2892, 442, 453, 444, 13356,
			447, 9632, 449, 451, 21347,

			// === Bars ===
			2349, 2351, 9467, 2353, 2355, 2357, 13354, 2359,
			2361, 2363,

			// === Smithing outputs ===
			4819, 4820, 1539, 4821, 4822, 4823, 4824, 21728,

			// === Lunar Diplomacy ores/bars ===
			9076, 9077,

			// === Daeyalt (Sins of the Father) ===
			24706,

			// === Motherlode Mine ===
			12009, 12011, 12012,

			// === Lovakengj mining (sulphur + dynamite) ===
			13571, 13572, 13573, 13575,

			// === Shooting Star miner ===
			25539, 25543, 25547,

			// === Golden prospector outfit ===
			25549, 25551, 25553, 25555,

			// === Camdozaal mining (Barronite) ===
			25635, 25637, 25639, 25676,

			// === Volcanic Mine (ore fragments) ===
			21532, 21536, 21537, 21538, 21533, 21535, 21539,

			// === Zalcano (tephra + shards) ===
			23905, 23906, 23907, 23908,

			// === Mining outfit (Prospector) ===
			12013, 12014, 12015, 12016,

			// === Mining/Smithing capes & pets ===
			9792, 9794, 9795, 9797, 13321,

			// === Crystal-tool/Tier-fallback pickaxes ===
			23677, 25063,

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
			4759, 28254, 28256, 28258, 2653, 2655, 2657, 2659,
			2661, 2663, 2665, 2667, 2669, 2671, 2673, 2675,
			3478, 3479, 3480, 3481, 3483, 3485, 3486, 3488,
			12811, 12812, 12814, 12815, 20794, 20796, 24034, 24037,
			24040, 26743, 26745,

			// === Gem cutting/polishing inputs ===
			1617, 1619, 1621, 1623, 1625, 1627, 1629, 1631,
			6571, 19496,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			20146, 20149, 20152, 20840, 20842, 20844, 20846, 21301,
			21304, 22351, 22353, 22616, 22619, 22625, 22628, 22631,
			23242, 23787, 23789, 24421, 25165, 26158, 26166, 26172,
			26180, 26280, 26282, 26284, 26286, 26288, 26290, 26382,
			26384, 26386
		));
	}

	private static void addHerblore(Map<String, List<Integer>> m)
	{
		// HERBLORE — 503 items
		//   Tools (4), Grimy herbs (14), Clean herbs (14), Unfinished potion
		//   variants (extended) (17), Spirits of Elid secondaries (2), Vials &
		//   secondaries (35), Unfinished potions (22), Barbarian mix potions (58),
		//   Nightmare Zone potions (21), Wintertodt Rejuvenation potion (4),
		//   Gauntlet Egniol potion (4), Chambers of Xeric herbs/secondaries (9),
		//   Chambers of Xeric raid potions (72), Attack potions (8), Strength
		//   potions (8), Defence potions (8), Super attack/strength/defence (12),
		//   Super combat (8), Ranging & magic (20), Prayer & restores (24),
		//   Antifire & anti-poison (56), Energy & stamina (20), Other potions
		//   (60), Cape & pet (3)
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
			5936, 5939, 5942, 5951, 9019, 9021, 9022, 9023,
			9024, 10909, 10911, 10913, 10915, 10917, 10919, 10921,
			10923,

			// === Spirits of Elid secondaries ===
			6681, 6683,

			// === Vials & secondaries ===
			239, 241, 243, 12640, 187, 221, 223, 225,
			227, 229, 231, 235, 237, 245, 247, 1633,
			1975, 2398, 2970, 2972, 2974, 3138, 5937, 5940,
			6016, 6018, 6693, 9085, 9736, 10111, 11326, 11992,
			11994, 12641, 22405,

			// === Unfinished potions ===
			91, 93, 95, 97, 99, 101, 103, 105,
			107, 109, 111, 2483, 3002, 3004, 20697, 22443,
			23800, 23881, 30100, 31662, 31665, 31668,

			// === Barbarian mix potions ===
			11429, 11431, 11433, 11435, 11437, 11439, 11441, 11443,
			11445, 11447, 11449, 11451, 11453, 11455, 11457, 11459,
			11461, 11463, 11465, 11467, 11469, 11471, 11473, 11475,
			11477, 11479, 11481, 11483, 11485, 11487, 11489, 11491,
			11493, 11495, 11497, 11499, 11501, 11503, 11505, 11507,
			11509, 11511, 11513, 11515, 11517, 11519, 11521, 11523,
			11960, 11962, 12633, 12635, 21994, 21997, 22221, 22224,
			26350, 26353,

			// === Nightmare Zone potions ===
			11722, 11723, 11724, 11725, 11730, 11731, 11732, 11733,
			11734, 11735, 11736, 11737, 11738, 20985, 20986, 20987,
			20988, 20993, 20994, 20995, 20996,

			// === Wintertodt Rejuvenation potion ===
			20699, 20700, 20701, 20702,

			// === Gauntlet Egniol potion ===
			23882, 23883, 23884, 23885,

			// === Chambers of Xeric herbs/secondaries ===
			20901, 20902, 20904, 20905, 20907, 20908, 20910, 20911,
			20912,

			// === Chambers of Xeric raid potions ===
			20913, 20914, 20915, 20916, 20917, 20918, 20919, 20920,
			20921, 20922, 20923, 20924, 20925, 20926, 20927, 20928,
			20929, 20930, 20931, 20932, 20933, 20934, 20935, 20936,
			20937, 20938, 20939, 20940, 20941, 20942, 20943, 20944,
			20945, 20946, 20947, 20948, 20949, 20950, 20951, 20952,
			20953, 20954, 20955, 20956, 20957, 20958, 20959, 20960,
			20961, 20962, 20963, 20964, 20965, 20966, 20967, 20968,
			20969, 20970, 20971, 20972, 20973, 20974, 20975, 20976,
			20977, 20978, 20979, 20980, 20981, 20982, 20983, 20984,

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

			// === Antifire & anti-poison ===
			12905, 12907, 12909, 12911, 12913, 12915, 12917, 12919,
			5943, 5945, 5947, 5949, 5952, 5954, 5956, 5958,
			2452, 2454, 2456, 2458, 2446, 175, 177, 179,
			25765, 25764, 25763, 25762, 25757, 25756, 25755, 25754,
			25761, 25760, 25759, 25758, 29824, 29827, 29830, 29833,
			11951, 11953, 11955, 11957, 22209, 22212, 22215, 22218,
			21978, 21981, 21984, 21987, 2448, 181, 183, 185,

			// === Energy & stamina ===
			3008, 3010, 3012, 3014, 31638, 31641, 31644, 31647,
			31614, 31617, 31620, 31623, 12625, 12627, 12629, 12631,
			3016, 3018, 3020, 3022,

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
			9774, 9776, 21509
		));
	}

	private static void addAgilityThieving(Map<String, List<Integer>> m)
	{
		// AGILITY_THIEVING — 87 items
		//   Marks & tickets (3), Graceful set (7), Agility shortcut tools (4),
		//   Hallowed Sepulchre tools + jewellery (6), Rogue equipment (6),
		//   Thieving accessories (10), Blackjacks (10), Pyramid plunder (9), Capes
		//   & pets (6), Energy / stamina potions (cross-tag) (26)
		m.put(TAG_AGILITY_THIEVING, Arrays.asList(
			// === Marks & tickets ===
			2996, 11849, 29482,

			// === Graceful set ===
			11850, 11852, 11854, 11856, 11858, 11860, 30044,

			// === Agility shortcut tools ===
			88, 2203, 3105, 3107,

			// === Hallowed Sepulchre tools + jewellery ===
			24721, 24723, 24725, 24727, 24731, 24740,

			// === Rogue equipment ===
			5553, 5554, 5555, 5556, 5557, 5558,

			// === Thieving accessories ===
			1523, 5560, 10075, 12856, 13121, 13122, 13123, 13124,
			21143, 22521,

			// === Blackjacks ===
			4599, 4600, 6408, 6410, 6412, 6414, 6416, 6418,
			6420, 30944,

			// === Pyramid plunder ===
			9028, 9030, 9032, 9034, 9036, 9038, 9040, 9042,
			9044,

			// === Capes & pets ===
			9771, 9773, 9777, 9779, 20659, 20663,

			// === Energy / stamina potions (cross-tag) ===
			3032, 3034, 3036, 3038, 3008, 3010, 3012, 3014,
			31638, 31641, 31644, 31647, 31614, 31617, 31620, 31623,
			12625, 12627, 12629, 12631, 3016, 3018, 3020, 3022,
			11481, 11483
		));
	}

	private static void addSlayer(Map<String, List<Integer>> m)
	{
		// SLAYER — 378 items
		//   Slayer master items (122), Slayer rings (7), Slayer helmets (27),
		//   Black masks (18), Task-specific gear (28), Cannon (6), Cape & pet (4),
		//   Champion's Challenge scrolls (11), Mounted heads (POH trophies) (15),
		//   Top food (cross-tag) (11), Combat potions (cross-tag) (90), Teleport
		//   utility (cross-tag) (30), Storage & utility (cross-tag) (9)
		m.put(TAG_SLAYER, Arrays.asList(
			// === Slayer master items ===
			21730, 20525, 12728, 12730, 12732, 12734, 12736, 12738,
			20523, 20524, 20607, 21698, 21701, 21704, 21707, 22097,
			22100, 22103, 13227, 13229, 13231, 21804, 26358, 25746,
			2425, 3741, 4155, 5520, 10952, 11877, 11879, 11881,
			11883, 11885, 11887, 12740, 12742, 12744, 12769, 12771,
			12857, 12859, 12922, 12927, 12932, 12934, 13193, 13200,
			13201, 13250, 13252, 13254, 13273, 13274, 13275, 13276,
			13391, 19679, 19681, 19683, 19685, 20742, 21177, 21183,
			21257, 21268, 21270, 21275, 21637, 21807, 21810, 21813,
			21817, 21820, 21909, 22006, 22302, 22305, 22372, 22386,
			22388, 22390, 22392, 22394, 22396, 22660, 22671, 22673,
			22957, 22960, 22966, 22969, 22971, 22973, 22988, 23077,
			23079, 24258, 24259, 24260, 24261, 24262, 24268, 24777,
			25280, 25340, 26221, 26223, 26225, 26227, 26229, 26231,
			26360, 26362, 26364, 26368, 26370, 26372, 26520, 26522,
			26524, 26526,

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
			4551, 6696, 7409, 7421, 7432, 8923, 12004, 13116,
			13233, 21255, 21724, 21736, 21739, 21742, 21752, 21754,
			22951, 22983, 22986, 23037,

			// === Cannon ===
			2, 6, 8, 10, 12, 21728,

			// === Cape & pet ===
			9786, 9788, 12647, 13262,

			// === Champion's Challenge scrolls ===
			6798, 6799, 6800, 6801, 6802, 6803, 6804, 6805,
			6806, 6807, 6808,

			// === Mounted heads (POH trophies) ===
			7975, 7976, 7977, 7978, 7979, 7980, 7981, 7982,
			7983, 7984, 7985, 7986, 7987, 7988, 11279,

			// === Top food (cross-tag) ===
			385, 391, 397, 6685, 6687, 6689, 6691, 7060,
			7946, 11936, 13441,

			// === Combat potions (cross-tag) ===
			12913, 12915, 12917, 12919, 5943, 5945, 5947, 5949,
			22461, 22464, 22467, 22470, 22449, 22452, 22455, 22458,
			24598, 24601, 24603, 24605, 23733, 23736, 23739, 23742,
			23697, 23700, 23703, 23706, 23685, 23688, 23691, 23694,
			23721, 23724, 23727, 23730, 23709, 23712, 23715, 23718,
			22209, 22212, 22215, 22218, 2434, 139, 141, 143,
			2444, 169, 171, 173, 10925, 10927, 10929, 10931,
			21978, 21981, 21984, 21987, 2436, 145, 147, 149,
			12695, 12697, 12699, 12701, 2442, 163, 165, 167,
			11726, 11727, 11728, 11729, 11722, 11723, 11724, 11725,
			3024, 3026, 3028, 3030, 11493, 11495, 2440, 157,
			159, 161,

			// === Teleport utility (cross-tag) ===
			772, 1706, 1708, 1710, 1712, 2552, 2554, 2556,
			2558, 2562, 2564, 2566, 8013, 9084, 11107, 11109,
			11111, 11113, 11968, 11970, 11978, 13393, 21146, 21151,
			21153, 21155, 21166, 21171, 21173, 21175,

			// === Storage & utility (cross-tag) ===
			12791, 27281, 10498, 10499, 11941, 13226, 20724, 22109,
			25781
		));
	}

	private static void addFarming(Map<String, List<Integer>> m)
	{
		// FARMING — 299 items
		//   Tools (20), Compost (16), Seeds (158), Harvest produce (51), Farm
		//   outputs / materials (42), Farming Guild produce + seed packs (3),
		//   Farmer outfit (6), Cape & pet (3)
		m.put(TAG_FARMING, Arrays.asList(
			// === Tools ===
			952, 5325, 5329, 5331, 5333, 5334, 5335, 5337,
			5338, 5339, 5340, 5341, 5343, 5345, 5350, 5356,
			7409, 7410, 13353, 21160,

			// === Compost ===
			6032, 6034, 6036, 6470, 6472, 6474, 6476, 13419,
			13420, 13421, 19704, 21483, 21490, 21543, 21545, 22994,

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
			5472, 5474, 5478, 5960, 5962, 5964, 5968, 10844,
			10845, 10846, 10847,

			// === Farm outputs / materials ===
			6461, 6469, 5931, 5933, 5970, 5972, 5974, 5976,
			5978, 5980, 5982, 5984, 5994, 5996, 5998, 6000,
			6002, 6004, 6006, 6008, 6010, 6012, 6014, 6020,
			6022, 6024, 6026, 6028, 6030, 6040, 6041, 6043,
			6045, 6047, 6049, 6051, 6053, 6055, 6057, 6059,
			6113, 6311,

			// === Farming Guild produce + seed packs ===
			22932, 22929, 22993,

			// === Farmer outfit ===
			1411, 13640, 13642, 13643, 13644, 13646,

			// === Cape & pet ===
			9810, 9812, 20661
		));
	}

	private static void addRunecraft(Map<String, List<Integer>> m)
	{
		// RUNECRAFT — 92 items
		//   Talismans (24), Tiaras (20), Essence pouches (7), RC accessories (2),
		//   Essence intermediates (3), Weiss salts (runecrafting secondaries) (3),
		//   Essence (4), Basic runes (16), Combo runes (6), Raiments of the eye
		//   (4), Cape & pet (3)
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
			5521, 26822,

			// === Essence intermediates ===
			7938, 13445, 13446,

			// === Weiss salts (runecrafting secondaries) ===
			22593, 22595, 22597,

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
			9765, 9767, 20665
		));
	}

	private static void addHunter(Map<String, List<Integer>> m)
	{
		// HUNTER — 107 items
		//   Traps (24), Caught butterflies (4), Chinchompas (3), Kebbit furs
		//   (cleaned) (7), Hunter furs (tatty) (3), Kebbit byproducts (4),
		//   Salamanders (10), Bait (8), Hunter rewards (1), Impling jars (12),
		//   Polar camo (2), Desert camo (2), Jungle camo (2), Generic camo outfit
		//   (3), Larupia hunter (5), Graahk hunter (5), Kyatt hunter (5),
		//   Spotted/spottier (3), Cape & pet (4)
		m.put(TAG_HUNTER, Arrays.asList(
			// === Traps ===
			10006, 10008, 10010, 10012, 10023, 10025, 10027, 10028,
			10029, 10031, 10150, 11258, 11259, 11260, 21512, 21515,
			21518, 21521, 22192, 22195, 22198, 22201, 22204, 28626,

			// === Caught butterflies ===
			10014, 10016, 10018, 10020,

			// === Chinchompas ===
			11959, 10033, 10034,

			// === Kebbit furs (cleaned) ===
			10115, 10117, 10119, 10121, 10123, 10125, 10127,

			// === Hunter furs (tatty) ===
			10093, 10097, 10101,

			// === Kebbit byproducts ===
			10105, 10107, 10109, 10113,

			// === Salamanders ===
			10148, 1939, 10142, 10143, 10144, 10145, 10146, 10147,
			10149, 28834,

			// === Bait ===
			25, 943, 2134, 2876, 3226, 5076, 9978, 9986,

			// === Hunter rewards ===
			12855,

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
			9948, 9950, 13322, 13324
		));
	}

	private static void addConstruction(Map<String, List<Integer>> m)
	{
		// CONSTRUCTION — 182 items
		//   Tools (5), Planks (4), Nails (7), Construction materials (15), POH
		//   portals & telescopes (2), Bench/altar (pattern) (37), Mahogany Homes
		//   (7), Portraits & paintings (POH decoration) (11), Fossilised plant
		//   displays (5), Bagged plants & trees (16), Hedges (7), Lecterns (POH
		//   study) (8), Globes, orreries, telescopes (POH study) (13), Basic
		//   furniture (non-Oak/Teak/Mahogany) (42), POH teleports (3)
		m.put(TAG_CONSTRUCTION, Arrays.asList(
			// === Tools ===
			9625, 2347, 8794, 21180, 25644,

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

			// === Portraits & paintings (POH decoration) ===
			4814, 4816, 7995, 7996, 7997, 7998, 7999, 8000,
			8001, 8002, 8003,

			// === Fossilised plant displays ===
			21590, 21592, 21594, 21596, 21598,

			// === Bagged plants & trees ===
			8417, 8419, 8421, 8423, 8425, 8427, 8429, 8431,
			8433, 8435, 8451, 8453, 8455, 8457, 8459, 8461,

			// === Hedges ===
			8437, 8439, 8441, 8443, 8445, 8447, 8449,

			// === Lecterns (POH study) ===
			8534, 8536, 8538, 8540, 8542, 8544, 8546, 22687,

			// === Globes, orreries, telescopes (POH study) ===
			8624, 8628, 8626, 8630, 8632, 8634, 8636, 8638,
			8640, 8642, 8644, 8646, 8648,

			// === Basic furniture (non-Oak/Teak/Mahogany) ===
			8463, 8496, 8498, 8500, 8510, 8516, 8518, 8528,
			8548, 8560, 8562, 8574, 8576, 8586, 8588, 8590,
			8592, 8594, 8596, 8598, 8600, 8602, 8604, 8606,
			8608, 8610, 8622, 9791, 9846, 9847, 9848, 9849,
			9850, 9851, 9857, 9858, 9862, 9863, 9864, 9865,
			9866, 9867,

			// === POH teleports ===
			11740, 13117, 19476
		));
	}

	private static void addMisc(Map<String, List<Integer>> m)
	{
		// MISC — 610 items
		//   Teleport jewellery (69), Teleport tabs (74), Boss & quest jewellery
		//   (10), Cosmetic outfits / random events (52), Clue scrolls (35), Clue
		//   tools (25), Keys (13), Storage bags (6), Utility / banked supplies
		//   (2), Imbue scrolls (3), Combat trophies (PvM rewards) (6), Team capes
		//   (Castle Wars) (50), Currency (12), Legacy (253)
		m.put(TAG_MISC, Arrays.asList(
			// === Teleport jewellery ===
			1704, 1706, 1708, 1710, 1712, 2550, 2552, 2554,
			2556, 2558, 2562, 2564, 2566, 2570, 2572, 3853,
			3855, 3857, 3859, 3863, 3865, 3867, 11079, 11081,
			11083, 11088, 11095, 11099, 11101, 11103, 11107, 11109,
			11111, 11113, 11118, 11120, 11122, 11124, 11126, 11190,
			11191, 11192, 11194, 11968, 11970, 11972, 11974, 11976,
			11978, 11980, 11984, 11986, 11988, 12785, 13393, 20786,
			21129, 21134, 21136, 21138, 21146, 21151, 21153, 21155,
			21166, 21171, 21173, 21175, 24735,

			// === Teleport tabs ===
			8007, 8008, 8009, 8010, 8011, 8012, 8013, 11741,
			11742, 11743, 11744, 11745, 11746, 11747, 12402, 12403,
			12404, 12405, 12406, 12407, 12408, 12409, 12410, 12411,
			12642, 12775, 12776, 12777, 12778, 12779, 12780, 12781,
			12782, 12938, 13249, 19613, 19615, 19617, 19619, 19621,
			19623, 19625, 19627, 19629, 19631, 19651, 20430, 21541,
			21802, 22400, 22599, 22601, 22603, 22710, 22949, 23387,
			23771, 24251, 24336, 24441, 24949, 24951, 24953, 24955,
			24957, 24959, 24961, 24963, 28790, 28824, 29684, 29782,
			30149, 31443,

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
			713, 2677, 2722, 2801, 12073, 13648, 13649, 13650,
			13651, 19712, 19714, 19716, 19718, 19835, 19836, 20358,
			20360, 20362, 20364, 20543, 20544, 20545, 20546, 23127,
			23129, 23182, 23245, 23442, 24361, 24362, 24363, 24364,
			24365, 24366, 27427,

			// === Clue tools ===
			3450, 3451, 3452, 3453, 3454, 3455, 3456, 3457,
			3458, 3459, 3460, 3461, 3462, 3463, 3464, 3468,
			2574, 2575, 2576, 3465, 3466, 3467, 3469, 11014,
			20355,

			// === Keys ===
			989, 985, 987, 991, 993, 11942, 13248, 19677,
			20756, 21724, 22374, 23083, 23490,

			// === Storage bags ===
			5356, 11941, 13226, 13639, 24882, 25582,

			// === Utility / banked supplies ===
			954, 13660,

			// === Imbue scrolls ===
			12783, 12786, 20238,

			// === Combat trophies (PvM rewards) ===
			21285, 21295, 6570, 21433, 21439, 25975,

			// === Team capes (Castle Wars) ===
			4315, 4317, 4319, 4321, 4323, 4325, 4327, 4329,
			4331, 4333, 4335, 4337, 4339, 4341, 4343, 4345,
			4347, 4349, 4351, 4353, 4355, 4357, 4359, 4361,
			4363, 4365, 4367, 4369, 4371, 4373, 4375, 4377,
			4379, 4381, 4383, 4385, 4387, 4389, 4391, 4393,
			4395, 4397, 4399, 4401, 4403, 4405, 4407, 4409,
			4411, 4413,

			// === Currency ===
			617, 1464, 4278, 6306, 6529, 8851, 13204, 13307,
			21555, 23497, 24711, 29482,

			// === Legacy carry-over (hand-curated items the new classifier missed) ===
			962, 979, 981, 1037, 1038, 1040, 1042, 1044,
			1046, 1048, 1050, 1053, 1055, 1057, 1959, 1961,
			4558, 4559, 4560, 4562, 4563, 4564, 4565, 4566,
			6100, 6101, 6102, 6180, 6181, 6182, 6183, 6184,
			6185, 6186, 6187, 6188, 6542, 6822, 6828, 6834,
			6840, 6846, 6852, 6853, 6856, 6857, 6858, 6859,
			6860, 6861, 6862, 6863, 6864, 6865, 6866, 6867,
			7592, 7593, 7594, 7595, 7596, 7927, 9920, 9921,
			9922, 9923, 9924, 9925, 10476, 11019, 11020, 11021,
			11022, 11023, 11024, 11025, 11026, 11847, 11862, 11863,
			12861, 12887, 12888, 12889, 12890, 12891, 12892, 12893,
			12894, 12895, 12896, 12897, 13173, 13175, 13182, 13183,
			13184, 13185, 13283, 13284, 13285, 13286, 13287, 13288,
			13343, 13344, 13345, 13663, 13664, 13665, 19958, 19961,
			19964, 19967, 19970, 19973, 19976, 19979, 19982, 19985,
			20080, 20083, 20086, 20089, 20092, 20095, 20098, 20101,
			20104, 20107, 20433, 20436, 20439, 20442, 20832, 20834,
			20836, 21208, 21209, 21211, 21214, 21216, 21217, 21218,
			21219, 21220, 21221, 21222, 21223, 21224, 21225, 21226,
			21227, 21228, 21229, 21230, 21231, 21232, 21233, 21234,
			21235, 21236, 21237, 21238, 21239, 21847, 21849, 21851,
			21853, 21855, 21857, 21859, 21866, 21867, 21868, 22351,
			22353, 22355, 22358, 22361, 22689, 22692, 22695, 22698,
			22701, 23946, 24030, 24031, 24032, 24033, 24305, 24307,
			24309, 24311, 24313, 24315, 24317, 24319, 24321, 24323,
			24325, 24327, 24428, 24430, 24431, 24432, 24433, 24434,
			24435, 24436, 24437, 24438, 24977, 24980, 24981, 24982,
			24983, 24984, 24985, 24986, 24987, 24988, 24989, 24990,
			25322, 25324, 25326, 25328, 25330, 25332, 25334, 26280,
			26282, 26284, 26286, 26288, 26290, 26292, 26294, 26298,
			26304, 26308, 26310, 26312, 26314, 26316, 26326, 26328,
			26330, 26332, 26334, 26336, 26338
		));
	}

	private static void addQuests(Map<String, List<Integer>> m)
	{
		// QUESTS — 149 items
		//   Quest & achievement capes (9), Diary - Kandarin (4), Diary - Karamja
		//   (4), Diary - Ardougne (4), Diary - Falador (4), Diary - Fremennik (4),
		//   Diary - Wilderness (4), Diary - Morytania (4), Diary - Desert (4),
		//   Diary - Varrock (4), Diary - Western (4), Diary - Lumbridge (4), Diary
		//   consumables (4), Max hood variants (12), Quest unlock weapons (11),
		//   Quest cosmetic gear (20), Void Knight set (23), Fighter Torso et al.
		//   (6), Defenders (11), Boss pets (9)
		m.put(TAG_QUESTS, Arrays.asList(
			// === Quest & achievement capes ===
			9813, 9814, 13070, 13221, 13223, 13280, 13281, 19476,
			25344,

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

			// === Diary - Lumbridge ===
			13125, 13126, 13127, 13128,

			// === Diary consumables ===
			22941, 22943, 22945, 22947,

			// === Max hood variants ===
			21282, 13330, 13332, 13334, 13336, 13338, 20764, 21778,
			21782, 21786, 21900, 24857,

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
			26467, 26469, 26471, 26473, 26475, 26477, 32433,

			// === Fighter Torso et al. ===
			10547, 10548, 10549, 10551, 10553, 10555,

			// === Defenders ===
			8844, 8845, 8846, 8847, 8848, 8849, 8850, 23230,
			12954, 19722, 22322,

			// === Boss pets ===
			13225, 19730, 20851, 21273, 21291, 21992, 22473, 23760,
			25348
		));
	}

	private static void addSailing(Map<String, List<Integer>> m)
	{
		// SAILING — 47 items
		//   Navigation tools (20), Raw sailing fish (5), Cooked sailing fish (20),
		//   Cape & pet (2)
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
			31288, 31292
		));
	}

	private static void addCosmetics(Map<String, List<Integer>> m)
	{
		// COSMETICS — 461 items
		//   Holiday rares & event cosmetics (253), Ornament kits (54), Treasure
		//   Trail fashion (no stats) (154)
		m.put(TAG_COSMETICS, Arrays.asList(
			// === Holiday rares & event cosmetics ===
			11847, 11862, 13343, 24986, 1048, 4560, 24982, 6100,
			6101, 6102, 23946, 24030, 24031, 24032, 24033, 962,
			979, 981, 1037, 1038, 1040, 1042, 1044, 1046,
			1050, 1053, 1055, 1057, 1959, 1961, 4558, 4559,
			4562, 4563, 4564, 4565, 4566, 6180, 6181, 6182,
			6183, 6184, 6185, 6186, 6187, 6188, 6542, 6822,
			6828, 6834, 6840, 6846, 6852, 6853, 6856, 6857,
			6858, 6859, 6860, 6861, 6862, 6863, 6864, 6865,
			6866, 6867, 7592, 7593, 7594, 7595, 7596, 7927,
			9920, 9921, 9922, 9923, 9924, 9925, 10476, 11019,
			11020, 11021, 11022, 11023, 11024, 11025, 11026, 11863,
			12861, 12887, 12888, 12889, 12890, 12891, 12892, 12893,
			12894, 12895, 12896, 12897, 13173, 13175, 13182, 13183,
			13184, 13185, 13283, 13284, 13285, 13286, 13287, 13288,
			13344, 13345, 13663, 13664, 13665, 19958, 19961, 19964,
			19967, 19970, 19973, 19976, 19979, 19982, 19985, 20080,
			20083, 20086, 20089, 20092, 20095, 20098, 20101, 20104,
			20107, 20433, 20436, 20439, 20442, 20832, 20834, 20836,
			21208, 21209, 21211, 21214, 21216, 21217, 21218, 21219,
			21220, 21221, 21222, 21223, 21224, 21225, 21226, 21227,
			21228, 21229, 21230, 21231, 21232, 21233, 21234, 21235,
			21236, 21237, 21238, 21239, 21847, 21849, 21851, 21853,
			21855, 21857, 21859, 21866, 21867, 21868, 22351, 22353,
			22355, 22358, 22361, 22689, 22692, 22695, 22698, 22701,
			24305, 24307, 24309, 24311, 24313, 24315, 24317, 24319,
			24321, 24323, 24325, 24327, 24428, 24430, 24431, 24432,
			24433, 24434, 24435, 24436, 24437, 24438, 24977, 24980,
			24981, 24983, 24984, 24985, 24987, 24988, 24989, 24990,
			25322, 25324, 25326, 25328, 25330, 25332, 25334, 26280,
			26282, 26284, 26286, 26288, 26290, 26292, 26294, 26298,
			26304, 26308, 26310, 26312, 26314, 26316, 26326, 26328,
			26330, 26332, 26334, 26336, 26338,

			// === Ornament kits ===
			23227, 12532, 12534, 12536, 12538, 12800, 20002, 20143,
			22231, 22236, 22239, 26707, 26709, 30451, 20068, 24670,
			26713, 25744, 12526, 12528, 12530, 12798, 20062, 20065,
			20071, 20074, 20077, 21202, 22246, 23232, 23237, 23348,
			25090, 25099, 25742, 26421, 26479, 26528, 26541, 26711,
			26717, 27098, 27113, 27121, 27255, 28017, 28336, 28684,
			28690, 30432, 30443, 33305, 33308, 33311,

			// === Treasure Trail fashion (no stats) ===
			12363, 12365, 12367, 7327, 8956, 8963, 8995, 10322,
			10400, 10402, 12524, 20246, 20266, 8924, 10420, 10422,
			12299, 12313, 12321, 20269, 12369, 23270, 23273, 12371,
			12518, 12520, 12522, 7112, 7319, 7321, 7323, 7325,
			8925, 8926, 8927, 8928, 8950, 8952, 8953, 8954,
			8955, 8957, 8958, 8959, 8960, 8961, 8962, 8964,
			8965, 8991, 8992, 8993, 8994, 8996, 8997, 10316,
			10318, 10320, 10324, 10404, 10406, 10408, 10410, 10412,
			10414, 10416, 10418, 10424, 10426, 10428, 10430, 10432,
			10434, 10436, 10438, 10862, 10863, 10864, 10865, 11280,
			11282, 12245, 12247, 12249, 12251, 12301, 12303, 12305,
			12307, 12309, 12311, 12315, 12317, 12319, 12323, 12325,
			12335, 12337, 12339, 12341, 12343, 12345, 12347, 12349,
			12351, 12353, 12355, 12359, 12361, 12393, 12395, 12397,
			12399, 12412, 12428, 12430, 12432, 12434, 12516, 12540,
			19699, 19943, 19946, 19949, 19952, 19955, 20053, 20056,
			20113, 20116, 20119, 20122, 20125, 20205, 20208, 20240,
			20243, 20773, 20775, 20777, 21861, 21862, 23285, 23288,
			23291, 23294, 23297, 23300, 23312, 23315, 23318, 23360,
			23407, 23410
		));
	}
}
