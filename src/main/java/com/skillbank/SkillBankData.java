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
		// MELEE — 1840 items
		//   Weapons (418), Shields & defenders (192), Head (254), Body (120), Legs
		//   (137), Hands (50), Feet (48), Capes (12), Neck (40), Rings (25),
		//   Ammunition (2), Training & utility (542)
		m.put(TAG_MELEE, Arrays.asList(
			// === Weapons ===
			1307, 1375, 3095, 1205, 3190, 11367, 1291, 1422,
			1321, 1237, 1277, 1337, 1309, 1363, 3096, 1203,
			3192, 11369, 1293, 1420, 1323, 1239, 1279, 1335,
			1311, 1365, 3097, 1207, 3194, 11371, 1295, 1424,
			1325, 1241, 1281, 1339, 1313, 1367, 3098, 1217,
			3196, 1297, 1426, 1327, 4580, 1283, 1341, 6609,
			6589, 6587, 6591, 6599, 6607, 6601, 6611, 6605,
			6613, 1315, 1369, 3099, 1209, 3198, 11373, 1299,
			1428, 1329, 1243, 1285, 1343, 1317, 1371, 3100,
			1211, 3200, 11375, 1301, 1430, 1331, 1245, 1287,
			1345, 1319, 1373, 3101, 1213, 3202, 11377, 1303,
			1432, 1333, 23330, 23332, 23334, 1247, 1289, 1347,
			21742, 21646, 4153, 24225, 28051, 28037, 28039, 26708,
			28019, 28049, 28033, 28027, 28031, 28041, 28029, 28035,
			26710, 7158, 1377, 12373, 13652, 1215, 3204, 22731,
			1305, 1434, 4587, 20000, 1249, 21009, 13576, 22978,
			23896, 23987, 26484, 26482, 26233, 30955, 29605, 31248,
			25870, 25872, 25874, 25876, 25878, 25880, 25882, 24697,
			5018, 8872, 28792, 5016, 29577, 7451, 27021, 28537,
			23851, 28543, 28545, 6746, 28682, 28997, 30957, 7435,
			27100, 29589, 33249, 7441, 29889, 6760, 25734, 25736,
			25738, 33243, 25979, 30891, 27287, 27291, 7449, 29796,
			26219, 27246, 7445, 25739, 25741, 6762, 10858, 7443,
			30759, 33335, 7439, 7437, 22331, 29084, 30369, 30367,
			33038, 30388, 33041, 9703, 27660, 27657, 27690, 29607,
			13108, 13109, 13110, 13111, 7433, 7675, 6764, 28810,
			667, 35, 7141, 11061, 3757, 7668, 7140, 25641,
			11037, 20155, 20161, 12389, 20158, 20756, 7447, 7142,
			23235, 24699, 10581, 10887, 6523, 6525, 6527, 6528,
			22542, 12426, 23528, 13263, 13265, 4151, 4718, 12774,
			4726, 11838, 4747, 4755, 12773, 11889, 11824, 12006,
			19675, 11802, 20368, 11804, 20370, 23995, 24551, 21015,
			21003, 22324, 24417, 11806, 20372, 12809, 22325, 22486,
			24617, 11808, 20374, 22622, 22613, 22610, 27871, 1351,
			1349, 1267, 11721, 1353, 1269, 28205, 6416, 6420,
			6418, 4599, 6410, 6408, 4600, 6414, 6412, 1361,
			12375, 12297, 10148, 28208, 1355, 11720, 28211, 1357,
			12377, 1271, 28214, 1359, 12379, 11719, 20557, 28534,
			25378, 28217, 25373, 27859, 27857, 20784, 20407, 21028,
			11920, 20406, 6739, 30340, 28220, 23895, 23897, 23861,
			23673, 23762, 23864, 28226, 27861, 27184, 7807, 7808,
			7806, 20251, 10129, 27855, 29849, 7671, 33200, 23821,
			23850, 23849, 23823, 23820, 28531, 25516, 29850, 23206,
			22433, 22435, 24695, 12727, 23360, 20254, 25066, 30347,
			25371, 30348, 30342, 25367, 30343, 30346, 27198, 12357,
			25981, 20257, 11711, 21649, 30390, 30392, 33178, 19918,
			33174, 26948, 26945, 20260, 7648, 7639, 7647, 7646,
			7644, 7643, 7642, 7641, 24693, 20263, 2961, 2963,
			2402, 20397, 27908, 24219, 27189, 27904, 27900, 27869,
			13141, 13142, 13143, 13144, 2952, 19912, 4068, 4503,
			4508, 10491, 22398, 23279, 10146, 4158, 11902, 10147,
			20011, 20014, 20727, 20405, 20593, 21205, 23628, 23620,
			23615, 12797,

			// === Shields & defenders ===
			8844, 1189, 1173, 8845, 1191, 1175, 8846, 1193,
			8746, 8748, 8750, 8754, 8756, 8758, 8760, 8762,
			8764, 8766, 8768, 8770, 8772, 8774, 8776, 20196,
			1177, 8847, 1195, 2589, 1179, 6633, 6631, 8848,
			1197, 12291, 1181, 8849, 1199, 22127, 22129, 22131,
			22133, 22135, 22137, 22139, 22141, 22143, 22145, 22149,
			22151, 22153, 22155, 22157, 1183, 8850, 23230, 1201,
			8714, 8716, 8718, 8720, 8722, 8724, 8726, 8728,
			8730, 8732, 8734, 8736, 8738, 8740, 8742, 8744,
			2621, 2629, 7336, 7342, 7348, 7354, 7360, 1185,
			3122, 1540, 28059, 12954, 19722, 21895, 22244, 1187,
			12418, 11283, 11284, 23991, 4224, 11759, 31081, 12608,
			26494, 8856, 27550, 27552, 33101, 9704, 3842, 1171,
			6235, 6257, 6279, 6233, 6255, 6277, 6231, 6253,
			6275, 6229, 6251, 6273, 6225, 6247, 6269, 6223,
			6245, 6267, 6221, 6243, 6265, 6219, 6241, 6263,
			3758, 10826, 2667, 12829, 6524, 10352, 22322, 12831,
			12817, 12213, 12223, 12243, 12233, 8752, 20181, 2597,
			7332, 7338, 7344, 7350, 7356, 12281, 22147, 2611,
			2603, 7334, 7340, 7346, 7352, 7358, 27185, 11710,
			33186, 30382, 23597, 3844, 12607, 13117, 13118, 13119,
			13120, 25934, 25936, 4072, 4507, 22251, 4156, 20272,
			4512, 12468, 12478, 12488, 3488, 20152, 2675, 22263,
			22257, 22266, 24266, 22260, 2659, 23599, 23642, 12821,

			// === Head ===
			1155, 12221, 1139, 1153, 12231, 1137, 1157, 20193,
			8686, 1141, 1165, 1151, 6623, 6621, 1159, 1143,
			1161, 2613, 2605, 1145, 1163, 2627, 10286, 10288,
			10290, 10292, 10294, 8480, 1147, 10589, 28057, 11335,
			12417, 1149, 23887, 23886, 23888, 29028, 3335, 23841,
			23840, 23842, 7539, 28070, 13198, 3327, 30750, 30777,
			28254, 21838, 13196, 26382, 26477, 30321, 9629, 13359,
			13364, 13369, 13374, 3748, 2673, 6128, 2665, 2657,
			11665, 3751, 10548, 3753, 11200, 10828, 21298, 4716,
			4724, 24419, 24271, 4745, 4753, 22326, 13199, 12931,
			13197, 22625, 12211, 26156, 27042, 26170, 20792, 12813,
			12241, 20178, 8682, 8684, 8688, 8690, 8692, 8694,
			8696, 8698, 8700, 8702, 8704, 8706, 8708, 8710,
			8712, 29560, 2595, 2587, 10306, 10308, 10310, 10312,
			10314, 19639, 19641, 8921, 8919, 11783, 8917, 11782,
			8915, 11781, 8911, 11779, 8909, 11778, 8907, 11777,
			8905, 11776, 11784, 12283, 12293, 10296, 10298, 10300,
			10302, 10304, 22159, 22161, 22163, 22165, 22167, 22169,
			22171, 22173, 22175, 22177, 22179, 22181, 22183, 22185,
			22187, 22189, 2619, 8464, 8466, 8468, 8470, 8472,
			8474, 8476, 8478, 8482, 8484, 8486, 8488, 8490,
			8492, 8494, 24034, 24201, 29816, 29818, 23785, 24195,
			31172, 27169, 29848, 3329, 29845, 3333, 27844, 25165,
			25169, 25174, 33338, 12639, 33066, 33068, 27195, 13139,
			13140, 26745, 26747, 26743, 3331, 3339, 3343, 3337,
			3341, 12637, 27165, 24198, 12929, 27833, 28933, 25910,
			25912, 25900, 33247, 25904, 25906, 12638, 4071, 4506,
			4551, 19645, 21264, 21266, 19647, 19649, 11864, 11865,
			21888, 21890, 24444, 5574, 13379, 4511, 4567, 9672,
			7917, 12466, 12476, 12486, 3486, 20146, 10549, 23591,
			23073, 23075, 10350, 23639, 23638, 23637, 23636, 13330,
			21282, 30302, 9749, 9755, 9770, 9752,

			// === Body ===
			1103, 1117, 12205, 26158, 27048, 26172, 1101, 1115,
			12225, 1105, 1119, 20169, 20184, 1107, 1125, 2591,
			2583, 6615, 6617, 1109, 1121, 12287, 1111, 1123,
			2607, 2599, 1113, 1127, 2615, 23209, 2623, 10564,
			28065, 3140, 12414, 21892, 22242, 23890, 23889, 23891,
			29022, 23844, 23845, 28067, 30753, 30779, 28256, 26384,
			13361, 13366, 13371, 13376, 10822, 10551, 6129, 2661,
			21301, 11832, 4720, 4728, 24420, 4749, 4757, 22327,
			22628, 22616, 12215, 20794, 12811, 12814, 12235, 29562,
			23366, 23369, 23372, 23375, 23378, 12277, 23392, 23395,
			23398, 23401, 23404, 23212, 23215, 23218, 23221, 24037,
			29846, 26749, 26721, 23843, 27845, 27842, 25515, 26753,
			27196, 27834, 28936, 26751, 13104, 13105, 13106, 13107,
			27831, 4069, 4504, 5575, 4509, 9674, 12460, 12470,
			12480, 20149, 3481, 2669, 2653, 8839, 10348, 30303,

			// === Legs ===
			1075, 12217, 1087, 1067, 1081, 1069, 20172, 20187,
			1083, 20175, 20190, 1077, 1089, 6625, 6627, 1071,
			12279, 12289, 1085, 1073, 1091, 3474, 1079, 2617,
			2625, 1093, 3476, 3477, 6809, 28061, 28063, 4087,
			12415, 4585, 12416, 23893, 23892, 23894, 26719, 29025,
			23847, 23848, 23246, 3795, 30756, 30781, 28258, 26386,
			13360, 13365, 13370, 13375, 3485, 2671, 3480, 6130,
			3479, 2655, 3478, 8840, 21304, 11834, 4722, 4730,
			24421, 4751, 4759, 22328, 22631, 22619, 12207, 12209,
			12219, 26166, 27044, 26180, 20796, 12812, 12815, 12237,
			12227, 12239, 12229, 29564, 2593, 2585, 3473, 3472,
			12285, 12295, 2609, 2601, 3475, 20422, 24040, 23789,
			29847, 26755, 23846, 27846, 27843, 26759, 27177, 27197,
			13112, 13113, 13114, 13115, 27835, 28939, 26757, 33194,
			27832, 4070, 4505, 11893, 11894, 11895, 5576, 4510,
			9676, 9678, 12462, 12464, 12472, 12474, 12482, 12484,
			3483, 2663, 10346, 23242, 23646, 23633, 23634, 23635,
			30304,

			// === Hands ===
			7454, 7455, 7456, 7457, 6629, 7458, 7459, 7460,
			21736, 7461, 24046, 7462, 7537, 8929, 3799, 30380,
			7453, 11133, 6151, 13357, 13362, 13367, 13372, 22981,
			778, 20581, 27110, 20582, 27111, 23593, 27112, 21817,
			11126, 11124, 11122, 11120, 11118, 11974, 11972, 11136,
			11138, 11140, 13103, 30386, 26723, 26727, 1059, 8842,
			10085, 10083,

			// === Feet ===
			4119, 4121, 4123, 4125, 6619, 4127, 4129, 4131,
			21643, 28055, 24043, 11840, 22234, 31088, 31097, 31091,
			31095, 31094, 26720, 3105, 28945, 9005, 9006, 3791,
			13131, 13132, 6145, 23389, 13358, 13363, 13368, 13373,
			12391, 11836, 21733, 13239, 31093, 33172, 31096, 20578,
			23413, 25163, 25167, 25171, 13129, 13130, 27178, 23037,

			// === Capes ===
			6570, 21295, 22114, 6568, 9747, 9753, 13329, 9768,
			21285, 9750, 23622, 33063,

			// === Neck ===
			24780, 1729, 23309, 6585, 12436, 10362, 10354, 1731,
			23354, 29801, 29804, 1725, 12851, 11128, 23240, 11090,
			10364, 19553, 20366, 22111, 1478, 22557, 19707, 23640,
			1704, 20586, 1706, 1708, 1710, 1712, 11976, 11978,
			20585, 30376, 27173, 4081, 10588, 12018, 12017, 22986,

			// === Rings ===
			30895, 28316, 6737, 11773, 25975, 2550, 12605, 12692,
			12603, 12691, 28307, 6735, 11772, 19550, 19710, 21739,
			23595, 22975, 21140, 23943, 30378, 27870, 2570, 28329,
			33182,

			// === Ammunition ===
			30384, 27544,

			// === Training & utility ===
			33118, 13012, 13014, 28336, 26370, 7804, 13060, 13062,
			19677, 29799, 11810, 13052, 13054, 125, 123, 121,
			2428, 22477, 26718, 20071, 11812, 13056, 26394, 25639,
			3803, 28293, 28295, 12988, 12990, 29189, 24589, 31136,
			24774, 24777, 22430, 13276, 13275, 13274, 31139, 27283,
			22963, 12960, 12962, 29574, 1967, 27667, 11447, 13064,
			9745, 26153, 9743, 26152, 9741, 26151, 9739, 26150,
			31174, 25960, 1978, 137, 135, 133, 2432, 33305,
			12877, 23706, 23703, 23700, 23697, 23694, 23691, 23688,
			23685, 23730, 23727, 23724, 23721, 23718, 23715, 23712,
			23709, 21882, 21885, 22231, 12534, 20143, 23667, 31142,
			27098, 28319, 27285, 28321, 22744, 12769, 1915, 12873,
			13048, 13050, 27351, 22966, 24488, 12972, 12974, 30893,
			22438, 3801, 28325, 13000, 29418, 13091, 13080, 29792,
			29790, 29794, 30744, 21279, 24229, 13279, 1235, 2038,
			2040, 31151, 6157, 6159, 6161, 29204, 29186, 28896,
			13024, 13026, 23321, 23327, 25744, 13040, 13042, 11814,
			12804, 2368, 25997, 27323, 28323, 22006, 29583, 29585,
			12984, 12986, 119, 117, 115, 113, 29421, 29424,
			149, 147, 145, 2436, 12701, 12699, 12697, 12695,
			165, 2442, 161, 159, 157, 2440, 12879, 31145,
			28684, 23232, 28287, 28285, 12875, 27684, 27687, 27681,
			13044, 13046, 11816, 11737, 11736, 11735, 11734, 28080,
			13020, 13022, 13016, 13018, 27349, 27347, 2297, 13441,
			12917, 12915, 12913, 11507, 2458, 2456, 2454, 2452,
			2323, 20068, 5739, 11431, 11429, 4627, 26717, 12613,
			12614, 12616, 13058, 28074, 24268, 28279, 23237, 12996,
			12998, 21730, 12992, 12994, 10014, 29207, 29944, 24592,
			29640, 29637, 29634, 29631, 2064, 31886, 22999, 2021,
			2309, 28813, 30324, 12968, 12970, 12964, 12966, 1891,
			25162, 25161, 25160, 25159, 5003, 29963, 7054, 2209,
			2185, 1897, 11445, 30816, 30810, 30825, 2140, 4291,
			29134, 3144, 29146, 2142, 4293, 29143, 24785, 2343,
			29137, 3381, 29140, 5988, 33109, 23831, 23868, 2011,
			27248, 11936, 11459, 11457, 11286, 1911, 5745, 26707,
			12538, 12536, 28078, 28076, 22097, 22236, 20002, 12532,
			26709, 22957, 2092, 7509, 28942, 10971, 7056, 20921,
			20922, 20923, 20924, 20913, 20914, 20915, 20916, 20917,
			20918, 20919, 20920, 12819, 25859, 29830, 11962, 11960,
			11957, 11955, 11953, 11951, 22218, 22215, 22212, 22209,
			13036, 13038, 20875, 11798, 11818, 11820, 11822, 11794,
			11796, 11800, 12849, 4423, 4421, 4419, 4417, 32336,
			347, 22983, 22973, 22969, 29966, 9668, 12980, 12982,
			12976, 12978, 27289, 1993, 12004, 20879, 20868, 3371,
			27341, 27339, 379, 22081, 355, 391, 32352, 13280,
			2327, 2293, 27211, 27208, 27205, 27202, 13008, 13010,
			13002, 13004, 13006, 7946, 29083, 29082, 29081, 29080,
			20881, 7058, 20864, 27321, 27319, 27317, 27315, 4235,
			30765, 20994, 20995, 20985, 20986, 20987, 20988, 11733,
			20989, 11732, 20990, 11731, 20991, 11730, 20992, 10553,
			20877, 2301, 2289, 6703, 6705, 25206, 25204, 25203,
			2028, 2032, 2235, 2219, 2233, 13231, 9670, 9666,
			20883, 30793, 30791, 30787, 30785, 10476, 2325, 131,
			21820, 9988, 20866, 10020, 23227, 13032, 13034, 13028,
			13030, 6721, 329, 29198, 29180, 6691, 23581, 6689,
			23579, 6687, 23577, 6685, 23575, 20074, 325, 397,
			385, 20390, 2366, 2080, 26132, 26009, 26012, 26126,
			26006, 26135, 29670, 26060, 29664, 26075, 28508, 29673,
			6163, 27343, 10016, 29201, 29183, 20382, 2003, 464,
			11441, 11443, 29409, 29210, 29192, 21997, 21994, 21987,
			21984, 21981, 21978, 23549, 23547, 23545, 23543, 11499,
			11497, 167, 163, 4608, 13066, 11487, 11485, 11471,
			11469, 20858, 30884, 30881, 30878, 30875, 373, 27333,
			27331, 27329, 27327, 2217, 29580, 13396, 333, 26149,
			7068, 7060, 1885, 26547, 29900, 27190, 22446, 12771,
			8851, 28301, 24859, 187, 5937, 5940, 2017, 2054,
			2191, 20973, 20974, 20975, 20976, 20980, 32328, 193,
			191, 189, 2450, 20077, 11523, 11521
		));
	}

	private static void addRange(Map<String, List<Integer>> m)
	{
		// RANGE — 812 items
		//   Weapons (106), Ammunition (156), Head (62), Body (65), Legs (68),
		//   Hands (27), Feet (21), Capes (15), Shields & off-hands (22), Neck
		//   (11), Rings (7), Training & utility (252)
		m.put(TAG_RANGE, Arrays.asList(
			// === Weapons ===
			9174, 9177, 9179, 11959, 9181, 9183, 26486, 9185,
			28053, 25918, 25916, 21902, 21012, 23902, 23901, 23903,
			23983, 4212, 11748, 28794, 25867, 25884, 25886, 25888,
			25890, 25892, 25894, 25896, 33021, 23856, 23855, 23857,
			29599, 28540, 29611, 30434, 30436, 29000, 26712, 28869,
			33251, 33245, 23357, 29591, 22333, 28834, 9705, 27610,
			27612, 27655, 27652, 26239, 26237, 26374, 837, 839,
			767, 841, 845, 843, 9176, 10280, 847, 849,
			8880, 4827, 851, 853, 2883, 10149, 10282, 855,
			857, 10033, 10156, 10284, 859, 861, 12788, 6724,
			10034, 22547, 11235, 12424, 19478, 11785, 4734, 25865,
			19481, 20997, 10148, 23601, 27187, 11708, 27853, 6082,
			27188, 30390, 30392, 27186, 4236, 10146, 20408, 10147,
			23611, 23630,

			// === Ammunition ===
			882, 877, 4773, 806, 598, 825, 864, 800,
			884, 9140, 4778, 807, 2532, 826, 863, 801,
			31579, 886, 9141, 4783, 808, 2534, 827, 865,
			802, 4788, 3093, 869, 888, 9142, 4793, 809,
			2536, 828, 866, 803, 890, 9143, 4798, 810,
			2538, 829, 867, 804, 892, 4803, 811, 2540,
			830, 868, 805, 21969, 21946, 11212, 21905, 11230,
			11217, 19484, 22804, 20849, 9341, 9244, 21971, 21948,
			21965, 21942, 21957, 21934, 21973, 21950, 21955, 21932,
			21959, 21936, 21967, 21944, 21963, 21940, 21961, 21938,
			21326, 25849, 21328, 21318, 28991, 881, 28688, 9139,
			4740, 8882, 31575, 9340, 9243, 30374, 9338, 9241,
			10145, 29305, 78, 28837, 9335, 9237, 10158, 10159,
			28878, 22636, 22634, 2866, 9342, 9245, 879, 9236,
			880, 9238, 31583, 9339, 9242, 9144, 9337, 9240,
			9145, 28872, 10144, 6522, 28922, 28919, 9336, 9239,
			12926, 9706, 20389, 23648, 27157, 27192, 21316, 29852,
			22230, 22229, 4160, 11875, 22227, 30694, 30696, 23649,
			29851, 22228, 27544, 10142, 732, 10143, 9419, 23619,
			27916, 27912, 7170, 28773,

			// === Head ===
			27705, 27717, 27729, 27741, 27753, 27765, 27777, 33031,
			25495, 23971, 26714, 29010, 23258, 30073, 27366, 27226,
			27235, 26475, 1167, 1169, 6326, 2581, 6131, 11664,
			3749, 10550, 10334, 12496, 12512, 11826, 12504, 10382,
			4732, 10390, 10374, 22638, 13338, 9758, 11783, 11782,
			11781, 11779, 11778, 11777, 11776, 11784, 33170, 27201,
			27847, 28904, 29842, 26741, 26739, 26737, 27836, 33247,
			11865, 24444, 13379, 23075, 21900, 9770,

			// === Body ===
			2503, 12381, 12385, 27697, 27709, 27721, 27733, 27745,
			27757, 27769, 33023, 23975, 29004, 26469, 30076, 27229,
			27238, 29280, 1129, 1131, 13381, 1133, 7362, 7364,
			10954, 6322, 23264, 1135, 7370, 7372, 12596, 6133,
			13072, 2499, 7374, 7376, 2501, 12327, 12331, 10330,
			12492, 11828, 12508, 12500, 10378, 4736, 10386, 10370,
			22641, 20423, 33166, 25496, 27199, 26749, 27848, 29840,
			26753, 33190, 27837, 27179, 26751, 23381, 11899, 8839,
			23632,

			// === Legs ===
			2497, 12383, 12387, 27701, 27713, 27725, 27737, 27749,
			27761, 27773, 33027, 25497, 23979, 26716, 29007, 30079,
			23384, 27232, 27241, 29283, 1095, 1097, 7366, 7368,
			10824, 10956, 6324, 23267, 1099, 7378, 7380, 23249,
			6135, 13073, 2493, 7382, 7384, 10555, 2495, 12329,
			12333, 10332, 12494, 11830, 12510, 12502, 10380, 4738,
			10388, 10372, 22644, 25493, 20424, 33168, 27200, 26755,
			27849, 29841, 26759, 26471, 27180, 33192, 27838, 27182,
			26757, 27181, 11900, 13380,

			// === Hands ===
			2491, 10085, 30082, 6149, 26235, 1063, 10077, 13377,
			6330, 23261, 1065, 10079, 19994, 2487, 10081, 2489,
			10083, 10336, 12490, 12506, 12498, 10376, 10384, 10368,
			25494, 30386, 8842,

			// === Feet ===
			29806, 31092, 29286, 13378, 10958, 6328, 2577, 6143,
			19921, 19930, 19924, 19927, 19933, 19936, 13237, 33202,
			31093, 33172, 31096, 1061, 22951,

			// === Capes ===
			10498, 28955, 28902, 28951, 28947, 27374, 27363, 29289,
			10499, 22109, 13337, 21898, 9756, 33063, 11901,

			// === Shields & off-hands ===
			22284, 22002, 22003, 32879, 12610, 26492, 22269, 22272,
			22275, 22278, 11926, 12807, 22281, 23197, 23200, 23203,
			23188, 23191, 23194, 21000, 12609, 33188,

			// === Neck ===
			19547, 22249, 19707, 1708, 1710, 30376, 27172, 12018,
			12017, 12436, 10362,

			// === Rings ===
			21752, 6733, 11771, 28310, 22975, 23943, 30378,

			// === Training & utility ===
			13171, 1464, 31659, 31656, 31653, 31650, 26715, 13169,
			27269, 19601, 13167, 22470, 22467, 22464, 22461, 12871,
			12867, 9433, 13193, 26524, 6, 26520, 26526, 26522,
			23956, 4207, 23869, 24644, 24641, 24638, 24635, 23742,
			23739, 23736, 23733, 22812, 30432, 27670, 21728, 21726,
			12865, 13165, 26711, 31169, 12883, 25826, 12786, 27355,
			27372, 31166, 4213, 26372, 11928, 11929, 11930, 818,
			24861, 11511, 11509, 173, 171, 169, 2444, 12869,
			13163, 26528, 26000, 31157, 2, 28924, 11725, 11724,
			11723, 11722, 12922, 28690, 20933, 20934, 20935, 20936,
			20925, 20926, 20927, 20928, 20929, 20930, 20931, 20932,
			28289, 27614, 28283, 21907, 13161, 20525, 43, 9463,
			19578, 21347, 21350, 21338, 21352, 22246, 28298, 26713,
			31004, 30998, 19592, 47, 29640, 29637, 29634, 29631,
			9434, 11885, 11874, 687, 11700, 39, 9454, 19570,
			10, 25162, 25161, 25160, 25159, 6169, 23832, 28072,
			30626, 21034, 9192, 31047, 27012, 11237, 21930, 21921,
			11232, 19582, 21918, 9193, 12863, 9190, 731, 25859,
			11881, 6167, 2865, 23124, 1745, 31235, 31010, 19589,
			19598, 19595, 11701, 40, 9457, 9423, 9187, 19584,
			10113, 10105, 19586, 10107, 70, 72, 62, 64,
			32352, 13280, 9418, 9416, 11703, 42, 9461, 822,
			19576, 19610, 29171, 26231, 56, 54, 2864, 1485,
			9194, 45, 20994, 20995, 20985, 20986, 20987, 20988,
			11733, 20989, 20990, 11731, 20991, 20992, 46, 13229,
			11165, 11167, 25206, 25204, 25203, 23557, 23555, 23553,
			23551, 2507, 131, 21820, 6165, 9191, 20607, 44,
			824, 19580, 9465, 9431, 9189, 26012, 29667, 26135,
			29664, 29676, 26072, 27343, 11702, 41, 9459, 19574,
			6173, 29168, 21981, 9188, 29580, 11887, 11876, 26547,
			19607, 19604, 187, 5937, 5940, 7208, 58, 60,
			66, 68, 9452, 12934
		));
	}

	private static void addMage(Map<String, List<Integer>> m)
	{
		// MAGE — 800 items
		//   Runes (39), Weapons (158), Off-hands, books & tomes (24), Head (84),
		//   Body (58), Legs (54), Hands (24), Feet (25), Capes (23), Neck (18),
		//   Rings (15), Teleport tablets & spell utility (245), Enchanting &
		//   skilling magic (33)
		m.put(TAG_MAGE, Arrays.asList(
			// === Runes ===
			556, 558, 555, 557, 554, 559, 564, 562,
			561, 11693, 563, 560, 565, 566, 11698, 9075,
			21880, 30843, 27293, 4696, 4699, 4695, 4698, 4697,
			4694, 28929, 11688, 11690, 11687, 11689, 11686, 11691,
			11696, 11694, 11695, 11692, 11697, 11699, 22208,

			// === Weapons ===
			6603, 30070, 23899, 23898, 23900, 27665, 27679, 27676,
			27662, 27624, 1391, 28988, 28796, 23853, 23852, 23854,
			28547, 28549, 11709, 22516, 30568, 31113, 31115, 25731,
			25733, 33330, 33332, 33255, 33257, 1389, 29594, 33253,
			21276, 1379, 22335, 27788, 27785, 33036, 33035, 33326,
			33323, 33318, 33314, 27275, 27277, 30634, 33320, 33316,
			33328, 29609, 28585, 28583, 1397, 1399, 1393, 1381,
			1385, 1387, 1383, 1395, 22368, 20736, 3053, 21198,
			20730, 6562, 11998, 11787, 12795, 1405, 20739, 1407,
			1401, 3054, 21200, 20733, 6563, 12000, 11789, 12796,
			1403, 8841, 6908, 4675, 6910, 1409, 12658, 6912,
			2416, 6914, 2415, 22555, 22552, 6526, 2417, 12422,
			9084, 24422, 4710, 24425, 24423, 21006, 22323, 22481,
			24144, 22296, 11791, 12902, 12904, 22288, 11905, 12899,
			22292, 12900, 22294, 11908, 22290, 24424, 22647, 10148,
			29849, 29602, 9091, 9092, 9093, 30390, 30392, 26948,
			26945, 33184, 7648, 7639, 7647, 7646, 7644, 7643,
			7642, 7641, 9013, 23363, 25517, 27920, 20431, 10146,
			4170, 21255, 12199, 12263, 12275, 10442, 10147, 10440,
			10444, 23342, 23653, 23626, 23613, 23617,

			// === Off-hands, books & tomes ===
			26551, 12612, 26490, 25818, 30371, 25985, 27251, 27253,
			9731, 30064, 25574, 20714, 6889, 11924, 12806, 21633,
			21634, 12825, 2890, 33176, 24723, 27358, 23652, 12821,

			// === Head ===
			12453, 12455, 29019, 579, 27123, 30445, 30437, 27119,
			3797, 6109, 23522, 9069, 9068, 26531, 6885, 26241,
			26473, 1017, 13385, 20595, 7400, 4089, 4099, 23047,
			4109, 6137, 3385, 11663, 3755, 10547, 12457, 6918,
			12419, 25398, 25413, 10342, 9096, 4708, 24288, 21018,
			24664, 22650, 11783, 11782, 11781, 11779, 11778, 11777,
			11776, 11784, 27183, 25518, 29845, 7394, 7396, 26967,
			27850, 29566, 27176, 9729, 27166, 30111, 9733, 26731,
			26735, 26733, 33247, 27839, 11898, 11865, 24444, 12203,
			12259, 12271, 10454, 20128, 10452, 10456, 23075, 21786,
			21778, 21782, 9764, 9770,

			// === Body ===
			581, 12449, 12451, 29013, 577, 7390, 7392, 27125,
			30447, 30439, 27115, 6107, 9070, 26533, 26243, 1035,
			13387, 20517, 7399, 4091, 4101, 23050, 4111, 20131,
			6139, 3387, 12458, 6916, 12420, 25389, 25404, 10338,
			9097, 4712, 24291, 21021, 24666, 22653, 27193, 29843,
			26749, 27851, 29568, 27174, 26753, 27167, 27158, 27160,
			26751, 33196, 27840, 11896, 10462, 10460, 20425, 8839,
			20576, 20598,

			// === Legs ===
			29016, 27127, 30449, 30441, 27117, 6108, 9071, 26535,
			26245, 1033, 13389, 20520, 7398, 4093, 4103, 23053,
			4113, 20137, 6141, 3389, 12459, 6924, 12421, 25401,
			25416, 10340, 9098, 4714, 24294, 21024, 24668, 22656,
			27194, 29844, 26755, 27852, 29570, 27175, 26759, 26471,
			27168, 27159, 27161, 26757, 33198, 27841, 11897, 20426,
			20577, 20599, 12445, 12447, 7386, 7388,

			// === Hands ===
			777, 31106, 9072, 26537, 6153, 20134, 4095, 4105,
			23056, 4115, 3391, 6922, 25392, 25407, 9099, 19544,
			23444, 11101, 11099, 11095, 11088, 27171, 19532, 8842,

			// === Feet ===
			6106, 9073, 26539, 6147, 2579, 20140, 4097, 4107,
			23059, 4117, 3393, 6920, 25395, 25410, 9100, 13235,
			33202, 31093, 33172, 31096, 27170, 10839, 27162, 22951,
			23644,

			// === Capes ===
			29615, 29617, 29613, 9074, 21793, 21791, 21795, 2413,
			2412, 2414, 9101, 21784, 21776, 21780, 9762, 13331,
			13333, 6111, 33063, 23603, 23607, 23605, 13335,

			// === Neck ===
			1727, 10366, 29486, 10344, 12002, 19720, 1664, 19707,
			1708, 1710, 30376, 9102, 12018, 12017, 13393, 23654,
			12436, 10362,

			// === Rings ===
			11014, 9104, 28313, 6731, 11770, 1645, 22975, 21140,
			30378, 33180, 6575, 28329, 6583, 23624, 19538,

			// === Teleport tablets & spell utility ===
			6894, 6895, 12881, 12728, 21049, 26346, 26344, 26342,
			26340, 27627, 26353, 26350, 12827, 6891, 22458, 22455,
			22452, 22449, 24607, 24613, 24611, 26705, 24615, 24621,
			26704, 28268, 31163, 13513, 22372, 20718, 12738, 23833,
			23870, 6899, 6898, 27121, 24333, 12528, 33308, 33311,
			24632, 24629, 24626, 24623, 23754, 23751, 23748, 23745,
			27281, 6903, 12732, 30451, 30443, 27113, 24517, 30631,
			12734, 27638, 27635, 27632, 27629, 24511, 30628, 28270,
			20724, 20945, 20946, 20947, 20948, 20939, 20940, 21043,
			20943, 20944, 6893, 12530, 12932, 11515, 11513, 3046,
			3044, 3042, 3040, 28291, 28281, 11931, 11932, 11933,
			5741, 27255, 12736, 31109, 23113, 23116, 23119, 23110,
			6902, 6883, 30806, 12791, 24587, 25481, 25478, 13256,
			27641, 28931, 28304, 28272, 26541, 26066, 28517, 26078,
			26003, 31154, 27673, 28274, 25578, 30068, 11729, 11728,
			11727, 11726, 31160, 23348, 28570, 24670, 21698, 21704,
			21707, 21701, 31148, 24514, 12730, 24860, 1907, 21637,
			573, 11715, 27616, 20430, 12621, 12623, 21079, 12617,
			12618, 12619, 12620, 29628, 21697, 11156, 3239, 24609,
			29640, 29637, 29634, 29631, 11714, 13157, 31881, 25162,
			25161, 25160, 25159, 20523, 11712, 8890, 30813, 11713,
			21799, 30640, 575, 11717, 20524, 6896, 21798, 13227,
			3470, 569, 11718, 24217, 11159, 6900, 21797, 20937,
			20938, 20941, 20942, 31814, 12004, 21202, 11491, 11489,
			9024, 9023, 9022, 9021, 32352, 13280, 5801, 5881,
			20065, 20994, 20995, 20985, 20986, 20987, 20988, 11733,
			20989, 20990, 11731, 20991, 20992, 6901, 25206, 25204,
			25203, 131, 21820, 6897, 23650, 27086, 30692, 29679,
			26012, 29667, 26135, 29664, 28484, 33054, 27343, 2396,
			12798, 29412, 21981, 12846, 8022, 27279, 29580, 28561,
			26547, 567, 571, 11716, 12934,

			// === Enchanting & skilling magic ===
			6904, 6905, 6906, 8017, 8014, 8015, 30384, 8019,
			8020, 8021, 8018, 8016, 21800, 13505, 13463, 13472,
			13502, 13469, 13511, 13481, 13475, 13448, 13454, 13490,
			13457, 13451, 13478, 13460, 13484, 13499, 13466, 27544,
			21257
		));
	}

	private static void addPrayer(Map<String, List<Integer>> m)
	{
		// PRAYER — 565 items
		//   Prayer equipment & robes (152), Bones & ashes (67), Ensouled heads
		//   (23), Prayer-restoring consumables (33), Holy symbols, books &
		//   blessings (48), Bone-processing utility (242)
		m.put(TAG_PRAYER, Arrays.asList(
			// === Prayer equipment & robes ===
			23339, 23336, 26229, 26223, 26221, 12203, 12195, 12193,
			12201, 12259, 12255, 12253, 12257, 12271, 12267, 12265,
			12269, 22986, 3839, 3841, 3843, 12607, 12609, 12611,
			22954, 22111, 538, 540, 10454, 10462, 10472, 30111,
			33002, 12598, 1718, 19997, 5576, 5575, 5574, 542,
			544, 20199, 23303, 21157, 9759, 9761, 426, 428,
			9676, 9674, 9672, 9678, 12601, 13202, 28775, 12637,
			10452, 10464, 10458, 10470, 548, 546, 25344, 12821,
			28939, 28936, 28933, 1724, 10456, 10468, 10460, 10474,
			25440, 25438, 25436, 25434, 12437, 23345, 6585, 26227,
			26225, 12197, 19921, 24201, 31081, 12825, 13121, 13122,
			13123, 13124, 12261, 19930, 24192, 12273, 19924, 24195,
			25463, 24204, 25459, 28945, 27251, 29562, 12817, 13126,
			13127, 13117, 13118, 13119, 13120, 25467, 10448, 19927,
			12639, 27163, 10466, 24725, 10828, 20202, 23306, 13112,
			19550, 19710, 10446, 10390, 10386, 19933, 27165, 24198,
			25465, 12829, 25461, 1716, 1722, 4757, 4753, 4759,
			6619, 6615, 6623, 6629, 6633, 6621, 6617, 6625,
			6627, 6631, 10450, 19936, 12638, 27164, 1033, 1035,

			// === Bones & ashes ===
			25775, 31075, 30973, 534, 530, 3182, 532, 25422,
			29352, 29354, 29346, 29348, 29344, 29356, 29366, 29370,
			31266, 29368, 29358, 29374, 29372, 31264, 29362, 29364,
			29360, 29350, 526, 2530, 3187, 33115, 528, 3127,
			11338, 6729, 29376, 536, 22783, 4830, 25766, 31729,
			30898, 3181, 22786, 25778, 3125, 3186, 11943, 25772,
			11337, 3180, 3183, 4834, 4832, 3123, 3179, 3185,
			31726, 29378, 22124, 25769, 2859, 22780, 28899, 6812,
			4812, 3130, 3128,

			// === Ensouled heads ===
			13508, 13505, 13463, 13496, 13472, 13493, 13502, 13469,
			13511, 13481, 13475, 13448, 26997, 13487, 13454, 13490,
			13457, 13451, 13478, 13460, 13484, 13499, 13466,

			// === Prayer-restoring consumables ===
			24605, 24603, 24601, 24598, 11467, 11465, 143, 141,
			139, 2434, 31844, 20396, 20395, 20394, 20393, 10931,
			23565, 10929, 23563, 10927, 23561, 10925, 23559, 11495,
			11493, 3030, 23573, 3028, 23571, 3026, 23569, 3024,
			23567,

			// === Holy symbols, books & blessings ===
			20235, 29381, 29338, 27335, 3844, 26488, 13155, 20220,
			3840, 26496, 13149, 20229, 29384, 29386, 20226, 22947,
			3827, 3828, 3830, 20223, 26498, 20232, 3833, 12622,
			12624, 12831, 23642, 12612, 26490, 13159, 12610, 26492,
			25818, 12608, 26494, 3835, 3836, 3837, 3838, 22943,
			22945, 3829, 3842, 27191, 13151, 3831, 3832, 3834,

			// === Bone-processing utility ===
			30975, 21079, 25781, 4260, 4256, 4266, 4257, 5076,
			13116, 4255, 4286, 4258, 4259, 29088, 21543, 25672,
			25660, 6728, 30626, 21034, 4261, 22756, 22960, 28942,
			4278, 25340, 4853, 3404, 31335, 4265, 25654, 12833,
			6714, 22758, 22988, 9668, 4271, 29382, 21551, 21600,
			21604, 21608, 21602, 4269, 11922, 3396, 21549, 21584,
			21582, 4264, 4267, 29213, 29195, 30627, 3428, 3426,
			3424, 3422, 4855, 3398, 10890, 20969, 20970, 20971,
			20972, 20961, 20962, 20963, 20964, 20965, 20966, 20967,
			20968, 30134, 30131, 30128, 30125, 9670, 9666, 21545,
			21553, 21610, 21616, 21614, 21618, 21612, 21620, 4854,
			3400, 33010, 5615, 26099, 33060, 28520, 26129, 4270,
			21547, 21572, 4263, 4268, 29587, 19634, 12823, 31333,
			22116, 27333, 27331, 27329, 27327, 25666, 29958, 21047,
			21566, 21564, 21568, 25419, 3325, 4262, 22754, 6810,
			4852, 23342, 7979, 1430, 23077, 27349, 27347, 26346,
			26344, 26342, 26340, 12199, 11061, 26353, 26350, 12827,
			10808, 12263, 3402, 12275, 7977, 1426, 8015, 31886,
			31861, 31383, 7976, 7975, 10977, 28132, 23882, 23883,
			23884, 23885, 24425, 12819, 7893, 27638, 27635, 27632,
			27629, 10442, 27351, 24417, 31386, 7980, 7981, 7978,
			21606, 10976, 3448, 6213, 3444, 251, 13280, 21580,
			21586, 21588, 28893, 29083, 29082, 29081, 29080, 26591,
			3440, 4850, 7842, 10886, 3438, 25670, 26592, 19672,
			20957, 20958, 20959, 20960, 20949, 20950, 20951, 20952,
			20953, 20954, 20955, 20956, 31389, 1432, 3436, 3434,
			3432, 3430, 10440, 11806, 2963, 21570, 21576, 21574,
			21578, 26590, 26589, 33231, 26593, 29424, 6211, 30367,
			21562, 1714, 4755, 8841, 21907, 6609, 6589, 6587,
			6599, 6607, 6601, 6603, 6611, 6605, 6613, 3442,
			3446, 10444
		));
	}

	private static void addCooking(Map<String, List<Integer>> m)
	{
		// COOKING — 739 items
		//   Cooking tools & utensils (7), Raw cookables (96), Ingredients (40),
		//   Cooked food (449), Special & combo food (81), Burnt food (66)
		m.put(TAG_COOKING, Arrays.asList(
			// === Cooking tools & utensils ===
			775, 6305, 9992, 9984, 7230, 7224, 946,

			// === Raw cookables ===
			7196, 2250, 2136, 2132, 4287, 9978, 31692, 25833,
			2138, 4289, 2876, 2202, 7186, 7176, 20874, 2178,
			29119, 7566, 29125, 29122, 29076, 7168, 24782, 2337,
			6178, 20876, 3226, 31700, 2134, 7216, 1940, 33106,
			1859, 29104, 7206, 10816, 21394, 321, 13439, 29101,
			363, 9986, 32341, 20861, 25670, 5001, 25658, 341,
			11934, 29107, 7529, 32309, 20870, 7543, 25652, 32317,
			32333, 345, 31561, 3142, 20878, 20867, 20859, 377,
			353, 389, 32349, 7944, 29113, 20880, 20863, 23872,
			349, 20872, 20882, 29110, 20855, 10138, 31686, 20865,
			331, 327, 395, 383, 317, 2514, 3379, 7577,
			29116, 20857, 371, 31553, 25664, 335, 359, 32325,

			// === Ingredients ===
			1963, 1923, 1921, 1927, 1965, 1887, 1985, 1973,
			1955, 5970, 1944, 2128, 1946, 2169, 2102, 2104,
			2120, 2122, 1957, 2110, 6697, 2114, 2116, 2118,
			1931, 2130, 1933, 2516, 1942, 3162, 2007, 7088,
			1982, 1925, 1929, 1975, 2108, 5504, 5986, 5982,

			// === Cooked food ===
			7225, 10880, 1911, 5809, 5745, 5889, 2092, 9574,
			9575, 9576, 2032, 5767, 5992, 1905, 7744, 5785,
			5739, 5865, 5825, 5905, 6961, 6701, 6008, 10964,
			2164, 1917, 7740, 24595, 31695, 31128, 32344, 2064,
			7919, 33091, 2021, 20862, 2309, 2307, 7491, 5769,
			5755, 7754, 5833, 5757, 5913, 1949, 7062, 7054,
			2074, 6794, 2185, 1977, 7074, 1871, 1869, 7086,
			1873, 5849, 5929, 10963, 2026, 2023, 2025, 29131,
			29217, 2140, 4291, 2878, 7228, 29134, 7521, 29149,
			7568, 29152, 29146, 2142, 4293, 29143, 29077, 24785,
			2343, 29137, 3228, 29140, 5988, 33109, 29128, 977,
			2165, 4242, 4243, 30985, 30987, 4458, 2011, 30977,
			30981, 30983, 30979, 5777, 5857, 29415, 403, 10971,
			7064, 7056, 1980, 20742, 2154, 3367, 3373, 7934,
			10969, 10965, 7082, 7084, 10962, 10961, 2084, 2171,
			2167, 4517, 32312, 2019, 20875, 10881, 19653, 1947,
			1987, 10960, 5793, 6683, 10966, 20871, 32320, 2249,
			2177, 2201, 9478, 9480, 9482, 9483, 9485, 9558,
			9559, 9561, 9563, 9577, 9579, 9581, 9583, 32336,
			1935, 1991, 1937, 1993, 31564, 2162, 20879, 20868,
			3365, 3371, 20860, 2106, 2124, 10970, 9052, 32352,
			7070, 5801, 5881, 9568, 9566, 9569, 9571, 9572,
			9573, 9567, 9570, 4014, 2955, 5817, 5749, 5897,
			20881, 7066, 7058, 10968, 20864, 4239, 4240, 4241,
			2245, 2094, 2197, 1875, 2112, 7487, 7485, 23874,
			2339, 1953, 24788, 20877, 2048, 1865, 1863, 10877,
			3146, 6703, 6705, 20873, 2028, 2223, 2239, 2030,
			2034, 2225, 2036, 2241, 2221, 2243, 2231, 2227,
			2219, 2237, 2233, 20883, 20856, 31703, 10136, 31689,
			10879, 7480, 1951, 9988, 9980, 10967, 7223, 6963,
			20866, 25674, 25662, 2880, 25656, 25668, 7078, 1847,
			2158, 13407, 13413, 13400, 13397, 13398, 13410, 13411,
			13414, 13399, 13405, 2080, 26033, 5921, 7080, 7072,
			2156, 2160, 6291, 6295, 6299, 6303, 6293, 6297,
			1969, 6965, 2003, 5398, 5402, 5406, 21626, 20858,
			31556, 2187, 8988, 3363, 3369, 6962, 7068, 7060,
			1877, 1879, 1861, 7076, 1995, 1996, 2251, 2257,
			2261, 2279, 9479, 9481, 9484, 9486, 2179, 2189,
			2193, 9560, 9562, 9564, 2042, 2050, 2056, 2066,
			2076, 2082, 2086, 2207, 2211, 2215, 9578, 9580,
			9582, 9584, 2015, 5984, 2017, 2054, 2341, 32328,
			20749, 9801, 9803, 29952, 29958, 6469, 10882, 7748,
			22929, 25960, 5378, 5380, 5382, 5386, 5996, 5751,
			5753, 6006, 5376, 31674, 20747, 8974, 4456, 8972,
			7490, 7489, 7488, 5460, 5468, 5470, 5474, 25672,
			25660, 11326, 29947, 5763, 7463, 25958, 31174, 7730,
			7733, 7736, 1573, 1913, 5747, 2126, 5004, 337,
			7518, 30037, 10878, 1909, 5743, 5873, 25654, 32357,
			5994, 25565, 7691, 32362, 5765, 4012, 7750, 6004,
			31903, 4237, 5454, 7486, 7484, 5388, 5390, 5392,
			5396, 5972, 6883, 7468, 5420, 5438, 5422, 5424,
			5428, 5430, 5432, 5434, 2040, 31677, 31671, 7483,
			7482, 7481, 13339, 401, 28502, 5759, 5841, 7580,
			9996, 9994, 25631, 5400, 7579, 1941, 2150, 10978,
			10859, 7700, 25666, 2152, 5960, 5962, 5964, 5968,
			31811, 7495, 7494, 7493, 7492, 20752, 21031, 21033,
			13280,

			// === Special & combo food ===
			22795, 22789, 7198, 2297, 2323, 4016, 1891, 2259,
			2209, 1897, 7188, 2277, 7178, 2166, 2285, 1997,
			1971, 1881, 2327, 2293, 7170, 21690, 2173, 7192,
			7194, 7182, 7184, 7172, 7174, 7164, 7166, 7212,
			7214, 7202, 7204, 2313, 7162, 2315, 2301, 2283,
			2289, 2229, 2235, 2325, 13406, 13415, 13403, 13401,
			13412, 13404, 13409, 13418, 13402, 13408, 13417, 2213,
			7479, 4608, 2255, 2217, 1883, 1885, 2317, 2321,
			19656, 1889, 2009, 2319, 21684, 2287, 2001, 29900,
			2195, 2281, 2253, 2205, 2191, 19662, 7530, 7218,
			7208,

			// === Burnt food ===
			13443, 29159, 26647, 20869, 2247, 9990, 9982, 32347,
			2311, 1903, 5002, 2144, 7226, 31698, 2199, 2013,
			11938, 3383, 7090, 323, 343, 357, 367, 369,
			20854, 23873, 7531, 29161, 7520, 32315, 2175, 32323,
			32339, 7570, 31567, 3148, 29155, 29157, 381, 393,
			32355, 2146, 7948, 7094, 7092, 2426, 2345, 2329,
			1867, 2305, 26637, 6699, 7222, 31706, 10140, 399,
			387, 7954, 3375, 6301, 2005, 5990, 375, 31559,
			33112, 32331
		));
	}

	private static void addWcFletching(Map<String, List<Integer>> m)
	{
		// WC_FLETCHING — 404 items
		//   Axes (49), Logs (26), Fletching tools (3), Bow & ammo materials (263),
		//   Unstrung bows (3), Strung bows (10), Finished arrows, darts & bolts
		//   (35), Woodcutting & Fletching outfits (15)
		m.put(TAG_WC_FLETCHING, Arrays.asList(
			// === Axes ===
			28196, 1351, 510, 28199, 1349, 512, 28202, 1353,
			514, 28205, 1361, 516, 28208, 1355, 518, 28211,
			1357, 520, 28214, 1359, 25378, 6743, 28217, 6739,
			28220, 23673, 28226, 492, 5751, 5753, 496, 500,
			502, 504, 506, 6741, 25110, 28177, 25066, 30347,
			25371, 30348, 23279, 13241, 13242, 20011, 23862, 494,
			498,

			// === Logs ===
			32907, 2862, 10810, 24691, 32904, 32902, 13355, 1511,
			2511, 1513, 6332, 1517, 1521, 19669, 32910, 10812,
			6333, 1519, 1515, 28138, 7405, 3448, 19672, 8934,
			11035, 3446,

			// === Fletching tools ===
			31043, 946, 31957,

			// === Bow & ammo materials ===
			39, 9375, 9454, 819, 19570, 9420, 40, 9377,
			9457, 820, 19572, 9423, 41, 9378, 9459, 821,
			19574, 9425, 42, 9379, 9461, 822, 9427, 43,
			9380, 9463, 823, 19578, 9429, 44, 824, 11237,
			21930, 21921, 11232, 19582, 21918, 9193, 21350, 25853,
			21352, 28134, 52, 31004, 30998, 19592, 47, 5070,
			5074, 9376, 9456, 9422, 1777, 31052, 31086, 28613,
			22935, 28163, 28166, 9438, 9192, 31047, 9190, 31032,
			31018, 2865, 53, 31010, 19589, 19598, 19595, 9187,
			19584, 19586, 28140, 28146, 48, 975, 6030, 70,
			72, 21952, 9450, 6028, 62, 64, 9448, 19610,
			28183, 28152, 6022, 56, 54, 9442, 2864, 9194,
			45, 46, 28661, 28624, 31049, 28154, 9191, 9381,
			9465, 9431, 9189, 28161, 28159, 50, 26030, 28529,
			9382, 9436, 28192, 28149, 9446, 6285, 6281, 6283,
			9188, 28630, 6024, 58, 60, 9444, 2861, 7797,
			9440, 6026, 66, 68, 9452, 22251, 22254, 22263,
			22257, 22266, 22260, 882, 4773, 9174, 825, 31579,
			31549, 884, 4778, 807, 826, 9177, 886, 4783,
			808, 827, 9179, 4821, 4788, 19576, 888, 4793,
			809, 828, 9181, 890, 4798, 810, 829, 9183,
			19580, 892, 4803, 811, 830, 9185, 22795, 11212,
			11230, 21902, 19484, 23953, 23869, 21338, 25849, 28991,
			31045, 19601, 3239, 5073, 7413, 22798, 22800, 5076,
			10089, 11885, 11874, 20696, 20695, 31575, 31547, 22869,
			23127, 19718, 29415, 314, 11881, 1779, 28670, 28626,
			28663, 31024, 31027, 29311, 29305, 28869, 13137, 10105,
			20799, 6020, 10107, 5316, 21488, 6047, 5314, 9418,
			29171, 6043, 6313, 10091, 407, 411, 413, 28655,
			28616, 28620, 28622, 23878, 10088, 22871, 31583, 31551,
			28628, 33062, 6053, 10087, 10132, 28674, 21626, 29168,
			21486, 11887, 31054, 6045, 2859, 10090, 5315, 839,
			9419, 9176, 2866, 21326, 21318, 19478, 13280,

			// === Unstrung bows ===
			4825, 19607, 19604,

			// === Strung bows ===
			845, 843, 847, 4827, 851, 853, 855, 857,
			859, 861,

			// === Finished arrows, darts & bolts ===
			11876, 9140, 9142, 9143, 9341, 21969, 21905, 21971,
			21965, 21957, 21973, 21955, 21959, 21967, 21963, 21961,
			28878, 28872, 881, 879, 880, 9139, 9335, 9336,
			9337, 9338, 9339, 10158, 10159, 4160, 11875, 9340,
			9342, 9144, 21316,

			// === Woodcutting & Fletching outfits ===
			28157, 28143, 28175, 28173, 28136, 28171, 28169, 10933,
			10941, 10940, 10939, 9783, 9785, 9807, 9809
		));
	}

	private static void addFishing(Map<String, List<Integer>> m)
	{
		// FISHING — 222 items
		//   Fishing tools (19), Bait & consumables (4), Fishing outfit (14), Raw
		//   fish (25), Fishing minigame items (160)
		m.put(TAG_FISHING, Arrays.asList(
			// === Fishing tools ===
			21028, 23762, 11323, 305, 22838, 25585, 307, 309,
			311, 3157, 301, 1585, 22842, 303, 6209, 22816,
			21031, 23864, 6670,

			// === Bait & consumables ===
			314, 11334, 313, 10087,

			// === Fishing outfit ===
			13261, 13258, 13259, 13260, 25582, 25598, 25592, 25594,
			25596, 25569, 9798, 9800, 32612, 31252,

			// === Raw fish ===
			321, 13439, 363, 5001, 341, 11934, 345, 3142,
			3150, 2148, 377, 353, 389, 7944, 349, 331,
			327, 395, 383, 317, 2514, 3379, 371, 335,
			359,

			// === Fishing minigame items ===
			25373, 25566, 583, 11883, 10129, 31255, 25637, 7989,
			25559, 7993, 7991, 22826, 21693, 6662, 13430, 25590,
			20853, 22829, 11940, 26598, 30773, 21652, 25114, 11881,
			32307, 22818, 11479, 11477, 155, 153, 151, 2438,
			7779, 6666, 13429, 5004, 22835, 25565, 21293, 25059,
			30342, 25367, 30343, 11330, 11332, 11328, 21649, 21356,
			22820, 22832, 23122, 31416, 407, 22846, 22844, 21655,
			32341, 20861, 29216, 25670, 25658, 32309, 25652, 32317,
			32333, 25564, 31561, 20867, 20859, 32349, 29113, 20863,
			23872, 29110, 20855, 10138, 31686, 20865, 20857, 31553,
			25664, 32325, 13339, 13431, 13432, 30900, 25588, 31611,
			31608, 31605, 31602, 10978, 25580, 6674, 21033, 26579,
			23953, 7198, 1581, 31873, 405, 11326, 23129, 13651,
			13650, 13649, 23823, 32451, 32753, 11936, 7535, 6667,
			32380, 32366, 32368, 32371, 32377, 32383, 32374, 7188,
			6673, 7942, 31408, 337, 31412, 22840, 31420, 3155,
			2162, 2149, 20860, 411, 31424, 20856, 22941, 22943,
			22945, 22947, 2876, 338, 25, 11324, 33062, 28502,
			25578, 7990, 25561, 7994, 7992, 31410, 31414, 31422,
			31418, 31426, 31430, 31428, 25567, 31820, 32328, 13280
		));
	}

	private static void addFiremaking(Map<String, List<Integer>> m)
	{
		// FIREMAKING — 113 items
		//   Tinderboxes & firelighting tools (11), Logs (22), Pyre logs (13),
		//   Shade items (5), Wintertodt & minigame items (10), Firemaking outfit &
		//   rewards (52)
		m.put(TAG_FIREMAKING, Arrays.asList(
			// === Tinderboxes & firelighting tools ===
			10327, 7331, 20720, 29777, 4073, 20275, 7330, 10326,
			7329, 590, 7156,

			// === Logs ===
			10328, 7406, 7405, 10329, 7404, 32907, 2862, 10810,
			24691, 32904, 32902, 1511, 2511, 1513, 6332, 1517,
			1521, 19669, 32910, 6333, 1519, 1515,

			// === Pyre logs ===
			31386, 10808, 31383, 3448, 6213, 3444, 3440, 3438,
			19672, 31389, 6211, 3442, 3446,

			// === Shade items ===
			3402, 3404, 3396, 3398, 3400,

			// === Wintertodt & minigame items ===
			20696, 20710, 20704, 20708, 20706, 24554, 20702, 20701,
			20700, 20699,

			// === Firemaking outfit & rewards ===
			20695, 4548, 4546, 36, 4529, 29947, 27426, 4527,
			4535, 20791, 7794, 20799, 21869, 4522, 4537, 3436,
			3434, 3432, 3430, 22593, 596, 22597, 20712, 9804,
			9806, 26822, 592, 20698, 4544, 20718, 1468, 13573,
			25110, 22595, 20722, 10980, 4525, 10973, 11337, 5014,
			4540, 3428, 3422, 9934, 4700, 6448, 25419, 7053,
			13241, 13242, 13329, 13280
		));
	}

	private static void addCrafting(Map<String, List<Integer>> m)
	{
		// CRAFTING — 417 items
		//   Crafting tools & moulds (14), Gems (68), Hides & leather (14),
		//   Spinning materials (4), Glassmaking (4), Pottery & clay (9), Jewellery
		//   materials (3), Crafted jewellery (15), Crafted armour & leather goods
		//   (103), Crafting outfit & utility (183)
		m.put(TAG_CRAFTING, Arrays.asList(
			// === Crafting tools & moulds ===
			1595, 9434, 11065, 1755, 29920, 1599, 1597, 1733,
			1592, 1735, 2976, 1594, 26813, 5523,

			// === Gems ===
			1615, 1702, 1683, 11115, 1645, 1631, 1601, 1700,
			1681, 11092, 1662, 1643, 1605, 1696, 1677, 11076,
			1658, 1639, 1611, 21111, 21102, 21120, 21093, 21084,
			6573, 6581, 6579, 11130, 6577, 6575, 1609, 21108,
			21099, 21117, 21090, 21081, 1613, 1603, 1698, 1679,
			11085, 1660, 1641, 1607, 1694, 1675, 11072, 1656,
			1637, 1617, 1621, 1627, 6571, 1625, 1629, 1619,
			1623, 19496, 19493, 19541, 19501, 19532, 19535, 19538,
			19529, 8018, 9191, 9189,

			// === Hides & leather ===
			2509, 1747, 2505, 1751, 1745, 1753, 2507, 1749,
			27897, 1743, 1741, 12869, 22983, 22287,

			// === Spinning materials ===
			1759, 1734, 27279, 1737,

			// === Glassmaking ===
			21504, 1775, 401, 1781,

			// === Pottery & clay ===
			1791, 28193, 1789, 5352, 1787, 4438, 4440, 1761,
			12009,

			// === Jewellery materials ===
			2355, 2365, 2357,

			// === Crafted jewellery ===
			1664, 1692, 1654, 1635, 6041, 21114, 21096, 21087,
			1716, 1722, 774, 773, 32386, 10132, 1724,

			// === Crafted armour & leather goods ===
			3329, 1757, 3333, 11069, 3331, 3339, 3343, 3337,
			3341, 21123, 1061, 1059, 9780, 9782, 8710, 8752,
			8754, 8760, 8764, 8772, 8774, 2503, 2497, 22284,
			2491, 22133, 22135, 11103, 11101, 11099, 11095, 11074,
			3335, 10595, 7537, 7539, 26788, 30076, 30079, 30073,
			30082, 29286, 29289, 29283, 29280, 3327, 5525, 1129,
			1095, 1167, 1063, 10077, 1131, 1169, 22269, 10822,
			10824, 6235, 6257, 6279, 6233, 6255, 6277, 6231,
			6253, 6275, 6229, 6251, 6273, 6225, 6247, 6269,
			6223, 6245, 6267, 6221, 6243, 6265, 6219, 6241,
			6263, 6326, 6322, 6328, 6324, 22272, 6330, 1135,
			1099, 22275, 1065, 10079, 3387, 3385, 2499, 2493,
			22278, 2487, 10081, 2501, 2495, 22281, 10083,

			// === Crafting outfit & utility ===
			1794, 2370, 573, 19473, 31045, 3239, 948, 1919,
			3353, 3351, 3361, 3345, 3355, 3349, 3359, 3347,
			3357, 1767, 689, 1783, 4544, 10981, 6169, 8792,
			1739, 1633, 10820, 6155, 575, 6667, 10980, 4525,
			6167, 3470, 569, 6171, 1779, 7536, 11656, 1785,
			1673, 1771, 958, 10814, 30085, 33382, 29218, 4542,
			10973, 23836, 23876, 6038, 411, 413, 10995, 10996,
			1773, 1763, 6165, 968, 26036, 950, 2961, 6163,
			6287, 7801, 6289, 6173, 3224, 21105, 7939, 7767,
			7759, 567, 1720, 1714, 571, 1793, 13383, 10818,
			25481, 23956, 23962, 4207, 23951, 25859, 21347, 21350,
			21338, 25853, 21352, 29799, 28298, 27269, 8650, 8652,
			8658, 8662, 8666, 8668, 8670, 8678, 1391, 28293,
			28279, 28295, 21512, 29338, 31475, 31478, 31472, 1777,
			1923, 7540, 7541, 22372, 1925, 4546, 688, 32364,
			434, 28166, 31460, 31469, 19665, 7938, 22660, 4527,
			4535, 5350, 21270, 29163, 7538, 33384, 33393, 33387,
			33390, 31457, 31466, 5931, 3211, 3420, 31463, 22201,
			6051, 28291, 22195, 22192, 29292, 21690, 31954, 31957,
			21515, 4522, 4540, 9934, 1931, 10134, 1951, 22204,
			6157, 6159, 28304, 12927, 9382, 442, 9436, 29299,
			29177, 9080, 21521, 7771, 28287, 28289, 28301, 21518,
			5933, 10891, 22198, 1397, 1399, 1395, 13280
		));
	}

	private static void addMiningSmithing(Map<String, List<Integer>> m)
	{
		// MINING_SMITHING — 346 items
		//   Pickaxes (33), Mining outfit & utility (15), Ores (19), Bars (14),
		//   Smithing tools (2), Smithed weapons (15), Smithed armour (29),
		//   Cannonballs & ammo outputs (17), Giants' Foundry & minigame items
		//   (202)
		m.put(TAG_MINING_SMITHING, Arrays.asList(
			// === Pickaxes ===
			1265, 1267, 1269, 12297, 1273, 1271, 1275, 27695,
			25376, 12800, 11920, 12797, 23677, 23680, 468, 470,
			472, 474, 476, 478, 11923, 12594, 25112, 25063,
			30345, 25369, 30346, 23276, 13243, 13244, 20014, 23863,
			23822,

			// === Mining outfit & utility ===
			25539, 25555, 25549, 25551, 25553, 12016, 12013, 12014,
			12015, 13104, 13105, 13106, 13107, 9792, 9794,

			// === Ores ===
			440, 447, 449, 23877, 13575, 668, 436, 23837,
			9632, 444, 31716, 13356, 31719, 451, 442, 438,
			446, 2892, 9076,

			// === Bars ===
			2349, 2351, 2353, 2359, 2361, 9467, 2357, 13354,
			2363, 2365, 32892, 32889, 9077, 2355,

			// === Smithing tools ===
			2347, 29775,

			// === Smithed weapons ===
			21539, 25644, 27010, 1307, 1351, 1337, 1309, 1349,
			1353, 1315, 1355, 1428, 1359, 1347, 27021,

			// === Smithed armour ===
			11074, 21392, 776, 21343, 5014, 27027, 27029, 27031,
			27025, 27023, 21345, 1103, 1139, 1173, 1191, 1137,
			1105, 1109, 1159, 1197, 1143, 1121, 1111, 1145,
			1123, 21895, 32879, 1580, 30756,

			// === Cannonballs & ammo outputs ===
			31906, 31918, 31932, 31908, 31920, 31934, 2, 31922,
			31936, 31910, 31924, 31938, 31912, 31926, 31914, 31928,
			21728,

			// === Giants' Foundry & minigame items ===
			480, 21532, 482, 484, 12592, 19576, 21536, 486,
			488, 21537, 19580, 490, 6979, 22103, 22097, 22100,
			21347, 4, 25684, 25635, 25676, 22603, 698, 28813,
			30324, 25543, 32886, 28276, 434, 453, 21534, 30860,
			30848, 697, 27012, 11286, 1913, 5747, 13573, 13572,
			22595, 11798, 11818, 11820, 11822, 11794, 11796, 11800,
			21535, 12012, 30854, 30862, 30858, 30864, 30846, 30808,
			13570, 27014, 30856, 7791, 680, 30765, 4540, 27019,
			27693, 12011, 23906, 2568, 21538, 686, 6971, 13539,
			13544, 13549, 13554, 13559, 13538, 13543, 13548, 13553,
			13558, 13541, 13546, 13551, 13556, 13561, 13540, 13545,
			13550, 13555, 13560, 13542, 13547, 13552, 13557, 13562,
			13565, 13566, 13567, 13568, 13569, 2366, 28505, 26024,
			21533, 27017, 6448, 25547, 25527, 29412, 23905, 21341,
			13571, 9795, 9797, 39, 9375, 819, 19570, 31999,
			9420, 4819, 1794, 40, 9377, 820, 19572, 32002,
			9423, 4820, 7225, 32023, 32026, 41, 9378, 821,
			19574, 32005, 9425, 1539, 2370, 32029, 42, 9379,
			822, 32008, 9427, 4822, 43, 9380, 823, 19578,
			32011, 9429, 4823, 32032, 32035, 44, 824, 32014,
			4824, 21726, 32017, 23953, 27616, 32876, 19473, 31245,
			9376, 9422, 31881, 4544, 29088, 23442, 20364, 20362,
			24706, 5777, 5857, 2893, 33393, 721, 21540, 3211,
			9416, 31724, 31722, 9381, 9431, 28349, 13421, 2368,
			22006, 12009, 12010, 22593, 1629, 22597, 21622, 21637,
			23908, 13280
		));
	}

	private static void addHerblore(Map<String, List<Integer>> m)
	{
		// HERBLORE — 764 items
		//   Vials & herblore tools (18), Herbs (46), Secondaries (205), Unfinished
		//   potions (30), Finished potions (355), Barbarian mixes (52), Divine,
		//   extended & upgraded variants (58)
		m.put(TAG_HERBLORE, Arrays.asList(
			// === Vials & herblore tools ===
			20800, 11877, 11427, 11428, 233, 229, 22446, 227,
			20801, 23880, 11879, 9774, 9776, 9088, 9090, 9089,
			2390, 9086,

			// === Herbs ===
			3051, 3000, 261, 20908, 265, 267, 20905, 211,
			20907, 215, 217, 20904, 199, 205, 30094, 209,
			213, 2485, 201, 20901, 207, 1533, 1525, 203,
			3049, 219, 1531, 249, 255, 30097, 259, 263,
			2481, 251, 20902, 257, 1534, 1526, 253, 2998,
			269, 1528, 1527, 1529, 1530, 1532,

			// === Secondaries ===
			239, 243, 21975, 241, 12640, 23964, 29972, 30007,
			29988, 31084, 30014, 29993, 21163, 27616, 30018, 29971,
			30015, 29784, 592, 30016, 30795, 5075, 22420, 19662,
			4456, 20698, 6016, 30937, 2398, 11326, 29963, 29643,
			1975, 30002, 20912, 5974, 5935, 31708, 6693, 4460,
			30800, 9735, 30031, 20911, 29973, 221, 12859, 1550,
			23517, 9736, 9018, 9016, 1909, 7746, 5743, 5873,
			798, 6681, 23835, 23875, 32357, 31587, 5976, 797,
			11738, 247, 10111, 11992, 11994, 27272, 225, 30017,
			30009, 30011, 30013, 32362, 30019, 30020, 30032, 2970,
			30005, 30012, 10937, 27790, 26368, 26231, 12857, 3138,
			29974, 31710, 29996, 223, 11324, 4462, 3377, 26027,
			7650, 737, 231, 9017, 20910, 13066, 2150, 2152,
			3406, 4840, 237, 235, 187, 5937, 5940, 245,
			23489, 32360, 20752, 5105, 29530, 5300, 22124, 23867,
			23962, 29982, 12641, 31712, 28345, 28346, 28351, 738,
			5298, 6008, 1582, 31674, 5301, 1973, 13064, 23830,
			30983, 11154, 5303, 31481, 1980, 20742, 3367, 30357,
			23017, 3261, 2391, 5291, 10142, 5294, 10145, 13226,
			4464, 30088, 11262, 23806, 5297, 28837, 5104, 3153,
			10109, 5299, 5302, 5100, 6051, 5292, 10143, 5741,
			705, 29078, 29079, 28341, 28342, 6043, 26569, 31484,
			6018, 33133, 31677, 5295, 31671, 11204, 11205, 28478,
			26045, 33135, 2355, 993, 5761, 22879, 31569, 28388,
			28890, 29531, 1939, 5293, 10144, 5296, 5304, 31487,
			28386, 21622, 6049, 12934, 13280,

			// === Unfinished potions ===
			9019, 5942, 5951, 103, 22443, 107, 109, 31662,
			23881, 91, 7654, 7656, 7658, 97, 30100, 101,
			105, 2483, 93, 31665, 99, 20697, 3004, 95,
			3002, 111, 31668, 5936, 5939, 23800,

			// === Finished potions ===
			25765, 25757, 25761, 2446, 2448, 25764, 25756, 25760,
			175, 181, 25763, 25755, 25759, 177, 183, 25762,
			25754, 25758, 179, 185, 3008, 31614, 3010, 31617,
			3012, 31620, 3014, 31623, 12625, 12627, 12629, 12631,
			3032, 9021, 9022, 9023, 9024, 163, 167, 3024,
			3026, 3028, 3030, 6685, 6687, 6689, 6691, 10925,
			10927, 10929, 10931, 2450, 189, 191, 193, 4417,
			4419, 4421, 4423, 2452, 2454, 2456, 2458, 5943,
			5952, 5945, 5954, 5947, 5956, 5949, 5958, 12905,
			12913, 12907, 12915, 12909, 12917, 12911, 12919, 29631,
			20924, 20916, 20920, 30137, 7660, 31590, 27202, 10909,
			10917, 29080, 27315, 20996, 20988, 20992, 20699, 4842,
			2430, 20960, 20952, 20956, 3408, 3416, 5841, 30875,
			20984, 20976, 20980, 29634, 20923, 20915, 20919, 30140,
			7662, 31593, 27205, 10911, 10919, 29081, 27317, 20995,
			20987, 20991, 20700, 4844, 127, 20959, 20951, 20955,
			3410, 3018, 30878, 20983, 20975, 20979, 27347, 29637,
			20922, 20914, 20918, 30143, 7664, 31596, 20938, 20942,
			27208, 10913, 10921, 29082, 27319, 20994, 20986, 20990,
			20701, 4846, 129, 20958, 20950, 20954, 3412, 3418,
			27343, 3020, 30881, 20982, 20974, 20978, 27349, 29640,
			20921, 20913, 20917, 30146, 7666, 31599, 20937, 20941,
			27211, 10915, 10923, 29083, 27321, 20993, 20985, 20989,
			20702, 4848, 131, 20957, 20949, 20953, 3414, 3419,
			3022, 30884, 20981, 20973, 20977, 2428, 121, 123,
			125, 113, 115, 117, 119, 2432, 133, 135,
			137, 9739, 12695, 9741, 26151, 12697, 9743, 26152,
			12699, 9745, 26153, 12701, 3034, 3036, 3038, 2438,
			31602, 151, 31605, 153, 31608, 155, 31611, 2444,
			169, 171, 173, 3040, 3042, 3044, 3046, 2434,
			139, 141, 143, 2436, 145, 147, 149, 2440,
			157, 159, 161, 2442, 165, 11722, 11723, 11724,
			23567, 23569, 23571, 23573, 23575, 23577, 23579, 23581,
			23559, 23561, 23563, 23565, 22461, 22464, 22467, 22470,
			22449, 22452, 22455, 22458, 26340, 31650, 6470, 23885,
			27629, 26581, 5793, 9998, 20948, 20940, 20944, 11730,
			20972, 20964, 20968, 30125, 3016, 31626, 27327, 20936,
			20928, 20932, 26342, 31653, 6472, 23884, 27632, 26583,
			10000, 20947, 20939, 20943, 20971, 20963, 20967, 30128,
			3417, 31629, 27329, 20935, 20927, 20931, 26344, 31656,
			6474, 23883, 27635, 26585, 10002, 20946, 11732, 25205,
			20970, 20962, 20966, 30131, 31632, 27331, 20934, 20926,
			20930, 26346, 31659, 6476, 23882, 27638, 26587, 10004,
			20945, 7487, 20969, 20961, 20965, 30134, 31635, 27333,
			20933, 20925, 20929,

			// === Barbarian mixes ===
			11433, 11435, 11489, 11491, 11493, 11495, 11501, 11503,
			11461, 11473, 11505, 11429, 11445, 11457, 11453, 11437,
			11449, 12633, 11443, 11497, 11481, 11485, 11469, 11521,
			11463, 11475, 11507, 11431, 11459, 11455, 11439, 11451,
			12635, 11441, 11499, 11483, 11487, 11471, 11523, 26350,
			11477, 11517, 11513, 11465, 11509, 26353, 11447, 11479,
			11519, 11515, 11467, 11511,

			// === Divine, extended & upgraded variants ===
			31638, 31641, 31644, 31647, 21978, 21981, 21984, 21987,
			11951, 11953, 11960, 11955, 11962, 11957, 22209, 22212,
			22221, 22215, 21994, 22224, 22218, 21997, 29824, 29827,
			29830, 29833, 23697, 23700, 23703, 23706, 23709, 23712,
			23715, 23718, 23721, 23724, 23727, 23730, 23685, 23688,
			23691, 23694, 23733, 23736, 23739, 23742, 23745, 23748,
			23751, 23754, 24635, 24638, 24641, 24644, 24623, 24626,
			24629, 24632
		));
	}

	private static void addAgilityThieving(Map<String, List<Integer>> m)
	{
		// AGILITY_THIEVING — 238 items
		//   Agility outfit & graceful (76), Run-energy consumables (21), Agility
		//   marks & tickets (2), Thieving outfit & rogue set (8), Thieving tools
		//   (12), Thieving loot & artefacts (119)
		m.put(TAG_AGILITY_THIEVING, Arrays.asList(
			// === Agility outfit & graceful ===
			9771, 9773, 11860, 13589, 13601, 13613, 13625, 13637,
			13677, 21076, 24758, 25084, 27459, 30060, 11852, 13581,
			13593, 13605, 13617, 13629, 13669, 21064, 24746, 25072,
			27447, 30048, 11858, 13587, 13599, 13611, 13623, 13635,
			13675, 21073, 24755, 25081, 27456, 30057, 11850, 13579,
			13591, 13603, 13615, 13627, 13667, 21061, 24743, 25069,
			27444, 30045, 11856, 13585, 13597, 13609, 13621, 13633,
			13673, 21070, 24752, 25078, 27453, 30054, 11854, 13583,
			13595, 13607, 13619, 13631, 13671, 21067, 24749, 25075,
			27450, 30051, 25099, 30044,

			// === Run-energy consumables ===
			3038, 3036, 3034, 7218, 3032, 3012, 3010, 3008,
			13125, 31647, 31644, 31641, 31638, 12631, 23589, 12629,
			23587, 12627, 23585, 12625, 23583,

			// === Agility marks & tickets ===
			11849, 29460,

			// === Thieving outfit & rogue set ===
			5557, 5556, 5554, 5553, 5555, 21307, 9777, 9779,

			// === Thieving tools ===
			1523, 6416, 6420, 6418, 4599, 6410, 6408, 5560,
			24740, 4600, 6414, 6412,

			// === Thieving loot & artefacts ===
			2996, 29480, 7782, 12641, 10846, 10850, 4627, 88,
			29482, 10989, 10983, 22521, 21143, 23948, 10985, 22207,
			10075, 9040, 9028, 9034, 24723, 24721, 24727, 24711,
			24731, 24725, 24719, 4310, 4304, 4308, 4302, 4306,
			4300, 4298, 29325, 10991, 24790, 10987, 9418, 9419,
			9416, 30676, 10553, 2997, 9032, 9036, 10993, 6970,
			24735, 24844, 28493, 29658, 29661, 10975, 10844, 10848,
			4179, 13436, 13437, 13435, 13438, 13434, 9038, 10845,
			10849, 3016, 30038, 7785, 29332, 10847, 10851, 27410,
			27404, 27406, 11463, 11461, 12640, 24777, 7919, 30042,
			10981, 31764, 24729, 10115, 25244, 5559, 19653, 11682,
			8869, 9026, 13138, 11138, 24862, 24866, 24864, 13280,
			21656, 4024, 4025, 24867, 28771, 950, 8868, 24865,
			10071, 12633, 8866, 9030, 3022, 20551, 3020, 20550,
			3018, 20549, 20548, 7763, 7767, 3325, 24863
		));
	}

	private static void addSlayer(Map<String, List<Integer>> m)
	{
		// SLAYER — 575 items
		//   Slayer assignment items (19), Mandatory protection (18), Core slayer
		//   gear (68), Cannon & burst supplies (10), Combat potions (122), Prayer
		//   & restores (18), Food (7), Teleports (10), Loot management (10), Misc
		//   utility (293)
		m.put(TAG_SLAYER, Arrays.asList(
			// === Slayer assignment items ===
			19679, 19681, 19683, 4155, 21270, 11873, 11872, 11871,
			11869, 11868, 11867, 11866, 21268, 21257, 19685, 22302,
			21807, 22305, 21810,

			// === Mandatory protection ===
			4161, 21724, 4166, 4164, 7432, 7430, 7421, 7429,
			7428, 7426, 7425, 7424, 7423, 6696, 4156, 4168,
			4551, 8923,

			// === Core slayer gear ===
			21316, 29816, 29818, 8921, 8919, 11783, 8917, 11782,
			8915, 11781, 8911, 11779, 8909, 11778, 8907, 11777,
			8905, 11776, 11784, 19639, 19641, 22951, 23037, 4160,
			11875, 33338, 21739, 19643, 19645, 33066, 33068, 22983,
			23073, 23075, 20727, 4158, 11902, 21264, 21266, 19647,
			19649, 4162, 21754, 4081, 10588, 12018, 12017, 11864,
			11865, 4170, 21255, 13233, 21888, 21890, 24370, 24444,
			25910, 25912, 25898, 25900, 25904, 25906, 6746, 22981,
			21742, 11876, 21736, 21752,

			// === Cannon & burst supplies ===
			10, 26524, 6, 26520, 12, 26526, 8, 26522,
			21728, 2,

			// === Combat potions ===
			11124, 11122, 11120, 11118, 30146, 30143, 30140, 30137,
			7666, 13358, 13363, 13368, 13373, 13357, 13362, 13367,
			13372, 13360, 13365, 13370, 13375, 13359, 13364, 13369,
			13374, 13361, 13366, 13371, 13376, 5841, 2436, 1706,
			1708, 1710, 1712, 12919, 12917, 12915, 12913, 5949,
			5947, 5945, 5943, 22470, 22467, 22464, 22461, 22458,
			22455, 22452, 22449, 21175, 21173, 21171, 23742, 23739,
			23736, 23733, 23706, 23703, 23700, 23697, 23694, 23691,
			23688, 23685, 23730, 23727, 23724, 23721, 23718, 23715,
			23712, 23709, 22218, 22215, 22212, 22209, 21155, 21153,
			21151, 173, 171, 169, 2444, 2566, 2564, 2562,
			6691, 6689, 6687, 6685, 11111, 11109, 11107, 21987,
			21984, 21981, 21978, 149, 147, 145, 12701, 12699,
			12697, 12695, 167, 165, 163, 2442, 11729, 11728,
			11727, 11726, 11725, 11724, 11723, 11722, 161, 159,
			157, 2440,

			// === Prayer & restores ===
			24605, 24603, 24601, 24598, 143, 141, 139, 2434,
			10931, 10929, 10927, 10925, 11495, 11493, 3030, 3028,
			3026, 3024,

			// === Food ===
			13441, 11936, 391, 7946, 397, 385, 7060,

			// === Teleports ===
			11978, 2558, 2556, 2554, 2552, 11113, 11970, 11968,
			8013, 13393,

			// === Loot management ===
			22986, 13226, 25781, 10499, 22109, 10498, 13116, 27281,
			11941, 12791,

			// === Misc utility ===
			7979, 23077, 21338, 22557, 29788, 28298, 31093, 31096,
			24268, 28279, 21730, 21817, 21183, 23083, 22975, 11885,
			11874, 28577, 21275, 22957, 21140, 2890, 13227, 21177,
			22973, 22971, 22969, 29966, 7159, 25981, 31258, 12004,
			7978, 7053, 13229, 13231, 24942, 12929, 12927, 13380,
			13379, 26108, 10952, 9786, 9788, 7788, 5759, 5761,
			10587, 11887, 33247, 24266, 28301, 7208, 13263, 13265,
			12006, 4151, 26482, 29804, 19677, 21634, 29806, 19675,
			31088, 31097, 31092, 31095, 31094, 11832, 11834, 7977,
			31172, 28316, 6737, 24699, 24697, 13276, 13275, 13274,
			27283, 22320, 28276, 27667, 7976, 11126, 11974, 11972,
			7975, 23975, 23987, 23979, 21012, 22978, 30070, 22804,
			31996, 20849, 22960, 29596, 20736, 25340, 29589, 13508,
			13496, 13487, 13235, 28321, 27670, 6666, 25926, 25928,
			25932, 25934, 25936, 30638, 30637, 21643, 21726, 4153,
			22988, 22966, 20724, 5576, 5575, 5574, 23064, 13103,
			7980, 10581, 25979, 30891, 27287, 27291, 7981, 23490,
			13198, 28281, 13280, 9731, 20730, 24271, 11748, 13080,
			29792, 29790, 29794, 12002, 23023, 23025, 23027, 23031,
			23033, 23035, 13279, 13237, 13239, 29594, 13202, 9185,
			4082, 29591, 6731, 12931, 13381, 13378, 13377, 5921,
			7986, 7984, 7983, 7982, 7985, 12922, 13196, 21123,
			12926, 12692, 11905, 24466, 28285, 11908, 13273, 27660,
			27657, 28310, 28283, 22542, 21907, 28585, 28583, 6735,
			27655, 5520, 13262, 20525, 12728, 26229, 26227, 26223,
			26225, 26221, 21804, 26370, 21813, 11883, 13254, 12740,
			24777, 13193, 12742, 22372, 21166, 20523, 12738, 22103,
			22097, 22100, 772, 12732, 6798, 20524, 11279, 22660,
			20742, 11877, 25280, 12859, 11881, 12734, 3741, 26358,
			26360, 26364, 26362, 12769, 6799, 6800, 6801, 6802,
			6803, 6804, 12647, 22671, 6808, 6805, 13391, 24261,
			9084, 12932, 12744, 7409, 13201, 22386, 12736, 24260,
			21146, 26368, 26372, 26231, 12857, 13250, 24262, 21820,
			20607, 13252, 25746, 22006, 6806, 23079, 7987, 7988,
			22673, 13200, 21698, 21704, 21707, 21701, 24258, 24259,
			12771, 2425, 21909, 12730, 11879, 21637, 22396, 22394,
			22388, 22392, 22390, 6807, 12934
		));
	}

	private static void addFarming(Map<String, List<Integer>> m)
	{
		// FARMING — 421 items
		//   Farming tools (33), Compost & soil treatment (9), Allotment seeds (8),
		//   Hops seeds (7), Flower seeds (6), Herb seeds (15), Bush seeds (6),
		//   Tree seeds (6), Fruit tree seeds (10), Special seeds (36), Saplings
		//   (25), Farmer outfit & contracts (260)
		m.put(TAG_FARMING, Arrays.asList(
			// === Farming tools ===
			22994, 5350, 13644, 13640, 13642, 13643, 13646, 21253,
			5354, 5325, 13353, 7409, 5356, 13250, 5341, 5347,
			5348, 5329, 952, 5328, 5331, 5333, 5334, 5335,
			5337, 5338, 5339, 5340, 9810, 9812, 13420, 11711,
			5352,

			// === Compost & soil treatment ===
			6032, 3271, 19704, 6476, 6474, 6472, 6470, 6034,
			21483,

			// === Allotment seeds ===
			5324, 5319, 5318, 22879, 5323, 5320, 5322, 5321,

			// === Hops seeds ===
			5308, 5305, 5307, 5306, 5310, 5311, 5309,

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
			31549, 23661, 22881, 5281, 13424, 20909, 5280, 31547,
			31545, 31541, 13423, 20906, 13657, 31543, 22875, 22883,
			22885, 13425, 21488, 5282, 20903, 22873, 31551, 21490,
			5317, 21486, 6453, 29538, 6710, 6457, 6112, 6458,
			6459, 6454, 6460, 6456,

			// === Saplings ===
			31505, 22866, 23659, 5496, 5497, 5503, 31502, 22856,
			5499, 5374, 21480, 5372, 5370, 5498, 5502, 5501,
			5500, 22859, 31508, 5375, 21477, 5371, 5373, 6464,
			9932,

			// === Farmer outfit & contracts ===
			31492, 29952, 22932, 22929, 22862, 23655, 21160, 6040,
			5480, 5378, 5380, 5382, 5386, 5996, 21697, 5481,
			5408, 5410, 5412, 5416, 6006, 5376, 13254, 5073,
			7413, 13653, 22798, 22800, 20747, 13427, 5460, 5478,
			5462, 5464, 5468, 5470, 5472, 5474, 753, 5980,
			5487, 31490, 22848, 5763, 7752, 5978, 31460, 5483,
			30747, 2126, 31481, 31511, 5418, 23015, 23011, 23009,
			23021, 23017, 23013, 23019, 13426, 6311, 30037, 5994,
			6057, 31457, 5931, 6000, 6020, 13428, 6051, 5362,
			21471, 6047, 5360, 6010, 5765, 4012, 6004, 6012,
			12792, 12793, 13654, 6043, 5358, 5440, 5458, 5442,
			5444, 5448, 5450, 5452, 5454, 2108, 5482, 5388,
			5390, 5392, 5396, 5486, 5972, 5485, 31513, 5484,
			6036, 6018, 5420, 5438, 5422, 5424, 5428, 5430,
			5432, 5434, 6319, 22850, 6014, 31494, 13252, 13421,
			6059, 5343, 22993, 26096, 6053, 5363, 5400, 5504,
			13419, 5986, 21469, 5960, 5962, 5964, 5968, 31515,
			21622, 5982, 6055, 6002, 5933, 6045, 5359, 5998,
			6049, 5361, 239, 6469, 6461, 3051, 3000, 13122,
			13123, 13124, 10846, 261, 5825, 5905, 8455, 8453,
			8417, 8429, 8425, 8433, 8461, 8457, 8423, 1963,
			6008, 31834, 20908, 1925, 1929, 1965, 6016, 265,
			30937, 5769, 22935, 5849, 5929, 5974, 1955, 5970,
			22660, 25102, 8445, 9903, 7178, 21504, 20905, 1947,
			9469, 1987, 215, 217, 199, 30094, 213, 2485,
			207, 3049, 219, 9082, 249, 5976, 4001, 255,
			30097, 22599, 247, 6113, 263, 2481, 30359, 225,
			6030, 6028, 7416, 7418, 4014, 31903, 27790, 20902,
			6022, 1957, 31484, 2114, 6955, 1942, 3138, 6041,
			257, 1951, 28154, 2518, 13639, 33062, 33135, 8441,
			231, 10844, 5398, 5402, 5406, 10845, 9079, 8449,
			20661, 253, 2998, 1982, 8443, 269, 6024, 10847,
			1793, 6026, 20749, 13280
		));
	}

	private static void addRunecraft(Map<String, List<Integer>> m)
	{
		// RUNECRAFT — 158 items
		//   Essence (11), Pouches & storage (9), Talismans (30), Tiaras (19), Core
		//   runes (21), Combination runes (6), Runecraft outfit (60), Guardians of
		//   the Rift items (2)
		m.put(TAG_RUNECRAFT, Arrays.asList(
			// === Essence ===
			1436, 26390, 24704, 13446, 7938, 13445, 25280, 26879,
			7936, 29087, 28591,

			// === Pouches & storage ===
			26822, 26784, 5514, 26908, 5512, 5510, 5509, 27281,
			26906,

			// === Talismans ===
			1438, 1450, 1446, 26798, 1452, 1454, 1456, 1440,
			5516, 1442, 1458, 1448, 1462, 26895, 26892, 26893,
			26890, 26898, 26891, 26897, 26888, 1460, 1444, 22118,
			28965, 28964, 26887, 26894, 26896, 26889,

			// === Tiaras ===
			5527, 9106, 5549, 5533, 26801, 5543, 5539, 5547,
			5535, 26804, 5537, 26788, 5545, 5529, 5541, 5525,
			5523, 5531, 22121,

			// === Core runes ===
			30843, 556, 9693, 9075, 565, 559, 564, 560,
			557, 9695, 554, 9699, 563, 558, 9697, 561,
			566, 28929, 555, 9691, 21880,

			// === Combination runes ===
			4696, 4699, 4695, 4698, 4697, 4694,

			// === Runecraft outfit ===
			29955, 9765, 9767, 26809, 5520, 11103, 11101, 11099,
			11095, 26807, 26813, 26811, 30771, 26914, 26876, 5521,
			25698, 26856, 26880, 25700, 24706, 30640, 26881, 26850,
			28597, 26884, 25696, 30887, 26941, 26815, 26854, 26852,
			28599, 26039, 26910, 13534, 28595, 28593, 6653, 13513,
			26820, 23907, 26912, 26886, 20665, 5519, 33231, 26885,
			26882, 26883, 25389, 25392, 25398, 25401, 25404, 25410,
			25407, 25413, 25416, 13280,

			// === Guardians of the Rift items ===
			26792, 26878
		));
	}

	private static void addHunter(Map<String, List<Integer>> m)
	{
		// HUNTER — 288 items
		//   Hunter tools & traps (7), Nets, jars & containers (4), Hunter outfit
		//   (40), Creature products (215), Chinchompas (6), Implings & impling
		//   jars (15), Birdhouse items (1)
		m.put(TAG_HUNTER, Arrays.asList(
			// === Hunter tools & traps ===
			10006, 12740, 10008, 12742, 10150, 10031, 29277,

			// === Nets, jars & containers ===
			10012, 10025, 10010, 11259,

			// === Hunter outfit ===
			6656, 10063, 10061, 10099, 29229, 29269, 29263, 29267,
			29265, 10059, 10057, 10103, 29232, 29226, 10095, 10067,
			10065, 10097, 10101, 10093, 10055, 10053, 10045, 10041,
			10043, 10051, 10047, 10049, 10069, 10039, 10035, 10037,
			10071, 9948, 9950, 29149, 29152, 29119, 29125, 29122,

			// === Creature products ===
			29230, 10014, 29207, 29944, 10148, 29227, 11264, 11266,
			29236, 29241, 31712, 29321, 31079, 29251, 21512, 29224,
			31674, 10089, 10121, 19665, 10115, 10127, 10123, 29253,
			10119, 10092, 9951, 29233, 29163, 31235, 10142, 29319,
			29239, 31241, 11159, 10004, 10002, 10000, 9998, 29311,
			29244, 29242, 29246, 29248, 30644, 11519, 11517, 29309,
			28831, 11262, 10028, 10027, 11258, 29166, 10113, 10105,
			10109, 29223, 29303, 29228, 29297, 10107, 22201, 12744,
			22195, 22192, 10143, 29301, 29292, 29171, 29174, 28893,
			29240, 21515, 2871, 31077, 10091, 29231, 29256, 10117,
			29307, 10134, 31677, 29101, 9986, 29107, 20870, 20878,
			20880, 20872, 20882, 29116, 31671, 10088, 29234, 22204,
			21126, 10020, 31261, 29238, 10018, 29198, 29180, 28487,
			29299, 29295, 10016, 29201, 29183, 9996, 9994, 10125,
			10087, 10132, 29168, 29177, 28890, 29210, 29192, 29237,
			31635, 31632, 31629, 31626, 29225, 1939, 29222, 29317,
			21521, 10029, 29259, 29261, 21518, 10090, 22198, 10023,
			10146, 10147, 29189, 10085, 29323, 5408, 5410, 5412,
			5416, 10129, 29271, 10173, 5076, 22826, 20720, 13430,
			2978, 2979, 2980, 2981, 2982, 2983, 2984, 2985,
			2986, 2987, 2988, 2989, 2990, 2991, 2992, 2993,
			2994, 22829, 29131, 29134, 29143, 29140, 29128, 31708,
			21652, 31481, 29273, 22835, 10145, 29305, 28869, 10111,
			29289, 29283, 29280, 8942, 29213, 29195, 22832, 29275,
			33120, 20873, 9978, 20874, 29113, 20876, 29110, 3226,
			10138, 29104, 29204, 29186, 303, 10144, 28834, 596,
			10149, 22816, 10156, 10158, 10159, 10075, 13280,

			// === Chinchompas ===
			29221, 29235, 11959, 13324, 10033, 10034,

			// === Implings & impling jars ===
			11256, 23768, 11238, 11244, 11248, 11246, 11242, 11260,
			11273, 19732, 11252, 11250, 11254, 11240, 31901,

			// === Birdhouse items ===
			5075
		));
	}

	private static void addConstruction(Map<String, List<Integer>> m)
	{
		// CONSTRUCTION — 391 items
		//   Construction tools (13), Planks (8), Nails (8), Building materials
		//   (10), Construction outfit & rewards (352)
		m.put(TAG_CONSTRUCTION, Arrays.asList(
			// === Construction tools ===
			9625, 9626, 24880, 29774, 24878, 24872, 24874, 24876,
			8794, 9468, 28628, 2347, 25644,

			// === Planks ===
			8782, 21036, 8778, 960, 24882, 32085, 32083, 8780,

			// === Nails ===
			4819, 4820, 1539, 4821, 4822, 4823, 4824, 31406,

			// === Building materials ===
			8790, 8784, 3211, 3420, 8788, 8786, 1761, 12009,
			12010, 434,

			// === Construction outfit & rewards ===
			8752, 7748, 8524, 21804, 8624, 30459, 20611, 33015,
			33122, 20615, 29619, 8638, 7995, 8520, 8455, 8453,
			8417, 8451, 8429, 8425, 8459, 8419, 8421, 8431,
			8433, 8435, 8461, 8457, 8423, 8427, 8650, 8652,
			8654, 8656, 8658, 8660, 8662, 8664, 8666, 8668,
			8670, 8672, 8674, 8676, 8678, 8680, 7977, 8516,
			7742, 8510, 8566, 9853, 8552, 8570, 9855, 8556,
			8636, 8526, 8518, 7976, 26266, 8463, 7975, 8496,
			7730, 7733, 7736, 22710, 10977, 24287, 8538, 7999,
			8536, 7996, 7728, 20609, 8445, 8604, 8586, 7690,
			8574, 9846, 8594, 8608, 8588, 9857, 8622, 8630,
			31024, 31027, 8522, 24885, 7691, 8000, 23064, 22106,
			12007, 23525, 25521, 8001, 7980, 7997, 7688, 8528,
			7981, 22671, 8006, 8580, 8642, 8584, 10976, 8002,
			8634, 20613, 9864, 9848, 8508, 9861, 8572, 8514,
			9845, 8546, 8606, 8544, 9867, 9856, 8558, 8648,
			9851, 8620, 9847, 9858, 8005, 7998, 7750, 8003,
			8439, 8504, 9859, 8578, 8564, 8512, 9843, 8502,
			8590, 8550, 8612, 8600, 9865, 8530, 8534, 9852,
			8598, 8644, 9849, 9862, 8614, 8560, 8632, 7732,
			7735, 7692, 7704, 7716, 7679, 30557, 30554, 8500,
			7758, 25612, 7974, 8596, 8610, 964, 8441, 8004,
			8640, 7986, 7984, 7990, 25561, 7994, 7992, 7983,
			7982, 31410, 31414, 31422, 23079, 7987, 7988, 22673,
			7985, 31418, 31426, 31430, 28674, 24884, 8449, 8447,
			7738, 8506, 9860, 8582, 9844, 8592, 8542, 8568,
			8616, 8602, 8540, 9866, 8532, 9854, 8554, 8646,
			9850, 9863, 8618, 7702, 7700, 8437, 8837, 8443,
			7763, 25093, 25096, 24463, 21909, 8548, 8576, 8562,
			8498, 7676, 9789, 9791, 32056, 32907, 31979, 32077,
			8682, 8686, 8688, 8690, 8692, 8694, 8696, 8698,
			8700, 8702, 8708, 8756, 8760, 8762, 8764, 8772,
			8774, 8776, 22159, 22163, 22165, 22167, 22173, 22175,
			22177, 22181, 22187, 8468, 8472, 8474, 8476, 8478,
			8480, 8482, 8486, 8492, 8494, 8720, 8736, 8742,
			8628, 7979, 23077, 32102, 32087, 7989, 25559, 7993,
			7991, 1783, 32053, 7752, 8792, 33368, 33362, 22595,
			31032, 12854, 7681, 25316, 31412, 7746, 32106, 31466,
			32093, 12936, 7978, 32074, 32071, 32065, 32080, 32068,
			32062, 1511, 24977, 32050, 6332, 31973, 24940, 32110,
			1775, 32044, 1521, 31967, 970, 6955, 30461, 30560,
			30563, 31964, 32059, 32910, 31982, 32099, 32113, 32108,
			11740, 32090, 28529, 32047, 6333, 31970, 6281, 7771,
			7767, 7759, 28708, 21562, 10508, 32041, 32104, 13280
		));
	}

	private static void addMisc(Map<String, List<Integer>> m)
	{
		// MISC — 2594 items
		//   Currency & exchange tokens (6), General teleports (112), Jewellery
		//   teleports (57), Utility containers (12), Clue scroll items (678),
		//   Minigame currencies & tickets (1), General tools (20), Uncategorized
		//   (1708)
		m.put(TAG_MISC, Arrays.asList(
			// === Currency & exchange tokens ===
			617, 995, 6964, 8890, 13204, 6306,

			// === General teleports ===
			30149, 20430, 21046, 30966, 12775, 19631, 19613, 13121,
			13122, 13123, 13124, 8011, 24955, 19629, 22949, 11745,
			8010, 12776, 24961, 19627, 30775, 28824, 30040, 23858,
			6103, 12777, 13666, 12403, 22400, 19615, 4251, 3690,
			3691, 6125, 6126, 13079, 23458, 23959, 23946, 8009,
			12404, 19621, 24959, 28333, 12778, 29684, 19625, 19651,
			24963, 12410, 13249, 12779, 24957, 28790, 12780, 12642,
			8008, 12405, 19617, 24949, 12406, 12411, 12402, 24951,
			12781, 12407, 12408, 11743, 23771, 11744, 21802, 11741,
			19564, 19619, 24441, 28332, 11740, 12782, 28331, 25837,
			29782, 22991, 30953, 28330, 12409, 24336, 12846, 11742,
			29455, 13658, 23904, 6102, 6101, 6100, 13102, 31443,
			8013, 11747, 8007, 21541, 8012, 24953, 23387, 19623,
			24251, 21863, 13393, 11746, 12938, 1505, 24615, 21760,

			// === Jewellery teleports ===
			1704, 20586, 1706, 1708, 1710, 1712, 11976, 11978,
			21175, 21173, 21171, 21166, 11126, 11124, 11122, 11120,
			11118, 11974, 11972, 11190, 11191, 11192, 11194, 3867,
			3865, 3863, 3859, 3857, 3855, 3853, 21155, 21153,
			21151, 21146, 2566, 2564, 2562, 2558, 2556, 2554,
			2552, 2572, 11988, 11986, 11984, 11980, 12785, 20786,
			12783, 11113, 11111, 11109, 11107, 11970, 11968, 10354,
			21268,

			// === Utility containers ===
			33384, 13226, 11941, 24585, 23650, 27086, 30692, 13639,
			33135, 25582, 24587, 25580,

			// === Clue scroll items ===
			405, 2714, 2715, 2842, 713, 23182, 2677, 2678,
			2679, 2680, 2681, 2682, 2683, 2684, 2685, 2686,
			2687, 2688, 2689, 2690, 2691, 2692, 2693, 2694,
			2695, 2696, 2697, 2698, 2699, 2700, 2701, 2702,
			2703, 2704, 2705, 2706, 2707, 2708, 2709, 2710,
			2711, 2712, 2713, 2716, 2719, 3490, 3491, 3492,
			3493, 3494, 3495, 3496, 3497, 3498, 3499, 3500,
			3501, 3502, 3503, 3504, 3505, 3506, 3507, 3508,
			3509, 3510, 3512, 3513, 3514, 3515, 3516, 3518,
			7236, 7238, 10180, 10182, 10184, 10186, 10188, 10190,
			10192, 10194, 10196, 10198, 10200, 10202, 10204, 10206,
			10208, 10210, 10212, 10214, 10216, 10218, 10220, 10222,
			10224, 10226, 10228, 10230, 10232, 12162, 12164, 12166,
			12167, 12168, 12169, 12170, 12172, 12173, 12174, 12175,
			12176, 12177, 12178, 12179, 12181, 12182, 12183, 12184,
			12185, 12186, 12187, 12188, 12189, 12190, 12191, 12192,
			19814, 19816, 19817, 19818, 19819, 19820, 19821, 19822,
			19823, 19824, 19825, 19826, 19828, 19829, 19830, 19831,
			19833, 22001, 23149, 23150, 23151, 23152, 23153, 23154,
			23155, 23156, 23157, 23158, 23159, 23160, 23161, 23162,
			23163, 23164, 23165, 23166, 25788, 25789, 28913, 28914,
			29853, 29854, 30928, 31268, 12073, 12074, 12075, 12076,
			12077, 12078, 12079, 12080, 12081, 12082, 12083, 12085,
			12086, 12087, 12088, 12089, 12090, 12091, 12092, 12093,
			12094, 12095, 12096, 12097, 12098, 12099, 12100, 12101,
			12102, 12103, 12104, 12105, 12106, 12107, 12108, 12109,
			12110, 12111, 12127, 12130, 12132, 12133, 12134, 12135,
			12136, 12137, 12138, 12140, 12141, 12142, 12143, 12144,
			12145, 12146, 12147, 12148, 12149, 12150, 12151, 12152,
			12153, 12154, 12155, 12156, 12157, 12158, 12159, 19782,
			19783, 19784, 19785, 19786, 19787, 19788, 19789, 19790,
			19791, 19792, 19793, 19794, 19795, 19796, 19797, 19798,
			19799, 19800, 19801, 19802, 19803, 19804, 19805, 19806,
			19807, 19808, 19809, 19810, 19811, 19813, 21524, 21525,
			22000, 23144, 23145, 23146, 23147, 23148, 23770, 24253,
			24773, 25498, 25499, 25786, 25787, 26943, 26944, 28910,
			28911, 28912, 29855, 29856, 30932, 31269, 31270, 31271,
			2722, 2723, 2725, 2727, 2729, 2731, 2733, 2735,
			2737, 2739, 2741, 2743, 2745, 2747, 2773, 2774,
			2776, 2778, 2780, 2782, 2783, 2785, 2786, 2788,
			2790, 2792, 2793, 2794, 2796, 2797, 2799, 3520,
			3522, 3524, 3525, 3526, 3528, 3530, 3532, 3534,
			3536, 3538, 3540, 3542, 3544, 3546, 3548, 3550,
			3552, 3554, 3556, 3558, 3560, 3562, 3564, 3566,
			3568, 3570, 3572, 3573, 3574, 3575, 3577, 3579,
			3580, 7239, 7241, 7243, 7245, 7247, 7248, 7249,
			7250, 7251, 7252, 7253, 7254, 7255, 7256, 7258,
			7260, 7262, 7264, 7266, 7268, 7270, 7272, 10234,
			10236, 10238, 10240, 10242, 10244, 10246, 10248, 10250,
			10252, 12542, 12544, 12546, 12548, 12550, 12552, 12554,
			12556, 12558, 12560, 12562, 12564, 12566, 12568, 12570,
			12572, 12574, 12576, 12578, 12581, 12584, 12587, 12590,
			19840, 19842, 19844, 19846, 19848, 19850, 19852, 19853,
			19854, 19856, 19857, 19858, 19860, 19862, 19864, 19866,
			19868, 19870, 19872, 19874, 19876, 19878, 19880, 19882,
			19884, 19886, 19888, 19890, 19892, 19894, 19896, 19898,
			19900, 19902, 19904, 19906, 19908, 19910, 21526, 21527,
			23045, 23167, 23168, 23169, 23170, 23172, 23174, 23175,
			23176, 23177, 23178, 23179, 23180, 23181, 24493, 25790,
			25791, 25792, 26566, 28915, 28916, 28918, 29859, 30929,
			30931, 31272, 31273, 19835, 2801, 2803, 2805, 2807,
			2809, 2811, 2813, 2815, 2817, 2819, 2821, 2823,
			2825, 2827, 2829, 2831, 2833, 2835, 2837, 2839,
			2841, 2843, 2845, 2847, 2848, 2849, 2851, 2853,
			2855, 2856, 2857, 2858, 3582, 3584, 3586, 3588,
			3590, 3592, 3594, 3596, 3598, 3599, 3601, 3602,
			3604, 3605, 3607, 3609, 3610, 3611, 3612, 3613,
			3614, 3615, 3616, 3617, 3618, 7274, 7276, 7278,
			7280, 7282, 7284, 7286, 7288, 7290, 7292, 7294,
			7296, 7298, 7300, 7301, 7303, 7304, 7305, 7307,
			7309, 7311, 7313, 7315, 7317, 10254, 10256, 10258,
			10260, 10262, 10264, 10266, 10268, 10270, 10272, 10274,
			10276, 10278, 12021, 12023, 12025, 12027, 12029, 12031,
			12033, 12035, 12037, 12039, 12041, 12043, 12045, 12047,
			12049, 12051, 12053, 12055, 12057, 12059, 12061, 12063,
			12065, 12067, 12069, 12071, 19734, 19736, 19738, 19740,
			19742, 19744, 19746, 19748, 19750, 19752, 19754, 19756,
			19758, 19760, 19762, 19764, 19766, 19768, 19770, 19772,
			19774, 19776, 19778, 19780, 23046, 23131, 23133, 23135,
			23136, 23137, 23138, 23139, 23140, 23141, 23142, 23143,
			25783, 25784, 28907, 28908, 28909, 29857, 29858, 30933,
			30935, 31274, 31275, 27427, 2832, 2795, 23245, 20546,
			20543, 20544, 19836, 20545, 24361, 24362, 24365, 24364,
			24366, 24363, 32414, 19837, 25590, 19941, 32416, 32418,
			32420, 32419, 32415, 32413, 32412, 32411,

			// === Minigame currencies & tickets ===
			4067,

			// === General tools ===
			1925, 8986, 9660, 1929, 9659, 7004, 27157, 22660,
			3678, 946, 5327, 23620, 27908, 1415, 24880, 33091,
			1755, 32865, 25644, 952,

			// === Uncategorized ===
			20283, 20284, 20285, 20287, 20288, 20289, 20290, 20307,
			20308, 20309, 20311, 20312, 20313, 20314, 20331, 20332,
			20333, 20335, 20336, 20337, 20338, 23418, 23419, 23420,
			23422, 23423, 23424, 23425, 8858, 8859, 27183, 27201,
			27200, 27199, 20577, 20576, 21770, 24051, 9596, 29408,
			1507, 22039, 11737, 11736, 11735, 11734, 27861, 28080,
			20405, 30682, 19476, 26252, 20525, 20582, 29458, 6561,
			20599, 20598, 23653, 11688, 11715, 7922, 9477, 19707,
			23640, 20585, 30376, 27173, 25518, 27194, 27193, 25690,
			25694, 11180, 21631, 22302, 21807, 25686, 27184, 24201,
			27369, 25688, 22299, 21668, 11341, 11342, 11343, 11345,
			11346, 11347, 11348, 12621, 12622, 12623, 12624, 22305,
			20431, 21813, 21810, 25692, 21670, 25938, 25941, 25944,
			25950, 25953, 11710, 24565, 11137, 11189, 13145, 13146,
			13147, 13148, 21640, 25920, 25921, 25922, 25923, 25924,
			25925, 28800, 29323, 27304, 22508, 33202, 13578, 21629,
			12746, 12747, 20760, 23611, 20593, 22665, 24192, 12617,
			12618, 12619, 12620, 11176, 30393, 11699, 29852, 10516,
			10556, 33172, 28334, 24442, 31454, 24263, 4049, 25202,
			25730, 24195, 12613, 12614, 12615, 12616, 13058, 23646,
			20594, 13302, 30361, 28767, 33221, 11340, 22230, 455,
			27855, 28074, 4053, 25209, 23593, 27112, 29271, 1852,
			23107, 684, 27169, 23595, 27221, 8976, 11067, 25463,
			20423, 20424, 25493, 25494, 3463, 3461, 3462, 3464,
			3460, 25448, 1666, 1647, 23642, 24592, 24609, 13307,
			29846, 29848, 29847, 11697, 11714, 20608, 24055, 20526,
			25476, 25195, 10533, 8936, 25212, 8943, 29843, 29845,
			29849, 29844, 21312, 22229, 11691, 25139, 605, 25199,
			8014, 8015, 6767, 13153, 13159, 5508, 11640, 13157,
			7464, 3667, 22999, 9011, 24329, 28082, 28084, 28086,
			28088, 28090, 28092, 28094, 28096, 28098, 30576, 30619,
			30616, 7686, 27187, 8974, 8972, 7671, 24941, 7157,
			7773, 983, 24204, 8989, 8990, 8979, 494, 498,
			30688, 25457, 7540, 7541, 4614, 4496, 11700, 508,
			25459, 8867, 19566, 3453, 3451, 3452, 3454, 3450,
			25442, 10835, 22227, 22424, 33200, 33237, 13531, 22519,
			26755, 26749, 29090, 6707, 10, 12, 8, 21716,
			30694, 30696, 11083, 11081, 11079, 30690, 25162, 25161,
			25160, 25159, 4514, 4516, 4513, 4515, 4055, 20523,
			20885, 4184, 1840, 3136, 3137, 26721, 694, 22320,
			12694, 11694, 11712, 20238, 2576, 432, 2404, 29923,
			2884, 13660, 25721, 12659, 3188, 20578, 4047, 23129,
			13648, 13651, 13650, 13649, 12789, 30363, 33235, 23442,
			20358, 20364, 20362, 20360, 23127, 19712, 19718, 19716,
			19714, 7587, 7588, 7589, 7591, 10521, 22711, 10560,
			10557, 25956, 24130, 24131, 19568, 31212, 30816, 30831,
			30819, 30840, 30822, 30810, 30837, 30825, 30813, 30828,
			30834, 23104, 23103, 23533, 23821, 23843, 28534, 23830,
			25959, 23850, 23849, 23823, 23846, 27847, 27848, 27849,
			25958, 23822, 23820, 23824, 23831, 27844, 27845, 27846,
			27842, 27843, 28531, 29602, 27850, 27852, 27851, 11696,
			33103, 674, 10513, 31214, 24049, 20886, 27308, 4707,
			22677, 23862, 30384, 33166, 25496, 30340, 23867, 23895,
			23897, 23864, 33170, 989, 33168, 25104, 8628, 23863,
			23861, 23962, 23866, 23868, 23953, 4077, 3063, 712,
			22366, 25571, 22778, 20408, 27853, 28072, 20899, 25244,
			13514, 13515, 13516, 13518, 13519, 13520, 13521, 21027,
			19685, 7684, 22330, 24190, 33065, 21710, 11692, 11713,
			24523, 4069, 4070, 4504, 4505, 4509, 4510, 11893,
			11894, 11895, 11896, 11897, 11898, 11899, 11900, 11901,
			25163, 25167, 25171, 25165, 25169, 25174, 4071, 4506,
			4511, 4072, 4507, 4512, 4068, 4503, 4508, 22426,
			10538, 10558, 13134, 13135, 13136, 1837, 30394, 1835,
			1833, 6384, 6388, 25516, 23639, 25515, 23633, 23649,
			31132, 981, 10829, 9654, 10972, 30803, 20389, 20784,
			20407, 23597, 23648, 27859, 28078, 27857, 28076, 20406,
			33186, 772, 29850, 24545, 30328, 1590, 5054, 12863,
			11689, 11717, 6798, 6075, 13526, 24065, 29851, 29840,
			29842, 29841, 11942, 26388, 23882, 23883, 23884, 23885,
			26571, 25648, 27176, 27175, 27174, 21205, 20524, 8626,
			33176, 26759, 26753, 26471, 22422, 23943, 12819, 6896,
			30378, 7960, 6675, 409, 7921, 8019, 8020, 8021,
			8018, 8016, 731, 23951, 25859, 29273, 11904, 22517,
			25961, 27997, 23644, 31211, 30997, 33227, 21672, 11677,
			13125, 13126, 13127, 13128, 22418, 4045, 25211, 25106,
			4008, 4009, 25102, 27219, 1411, 11525, 31744, 22228,
			4653, 10566, 11686, 11718, 272, 6202, 8864, 12854,
			21180, 5559, 33239, 956, 30357, 30763, 6663, 29961,
			21594, 27216, 21596, 21598, 21590, 21592, 8863, 12955,
			30395, 27177, 13129, 13130, 27462, 27622, 29895, 6814,
			7681, 13394, 31216, 5562, 5563, 5564, 5566, 5567,
			33393, 33387, 33390, 25926, 25928, 25930, 25932, 25934,
			25936, 27544, 27166, 27167, 27168, 6799, 23628, 3901,
			6800, 20754, 30638, 30637, 28765, 751, 10999, 6801,
			601, 11060, 25467, 25430, 25426, 25428, 25432, 25424,
			4692, 25454, 24059, 27225, 9469, 20557, 9415, 24418,
			10531, 9057, 10878, 13420, 8865, 27042, 23499, 6677,
			6678, 23638, 27180, 12639, 27163, 3835, 3836, 24217,
			1413, 24709, 24946, 24529, 21678, 9595, 10526, 10559,
			10546, 10545, 10544, 10543, 23630, 23591, 27302, 6189,
			979, 6802, 11996, 7682, 21624, 13528, 20897, 1580,
			6900, 22599, 13532, 22043, 23603, 23607, 23907, 23605,
			6803, 21297, 23622, 27170, 31345, 31347, 11088, 27195,
			27196, 27198, 27197, 5, 13395, 11701, 8869, 11721,
			3899, 9026, 23670, 1591, 1417, 11177, 6804, 1992,
			12656, 30396, 13139, 13140, 11140, 13103, 3165, 23632,
			275, 293, 298, 423, 1543, 1544, 1545, 1546,
			1547, 1548, 2411, 5010, 9722, 11039, 30951, 13248,
			20884, 4591, 13527, 33218, 11842, 23626, 26741, 26739,
			26737, 30400, 12693, 2528, 24528, 21606, 9651, 21540,
			23490, 11695, 25117, 22287, 9008, 3177, 6808, 30359,
			6805, 31071, 31073, 27188, 20355, 27870, 3848, 27341,
			27339, 34, 13391, 7777, 987, 30107, 26651, 8932,
			21762, 22045, 23652, 5614, 11711, 33180, 30904, 30908,
			30920, 30916, 30924, 30912, 5606, 26745, 26747, 26743,
			33233, 33190, 33192, 21387, 13280, 21580, 21586, 21588,
			21656, 22475, 1839, 23184, 30926, 11690, 19668, 5020,
			28769, 30902, 30906, 30918, 30914, 30922, 30910, 9475,
			27296, 11703, 20581, 27110, 11720, 299, 24534, 13002,
			31099, 7416, 7418, 8942, 4010, 4034, 4018, 30109,
			9078, 30035, 27836, 23619, 27916, 27837, 27838, 27912,
			2974, 2972, 30397, 13112, 13113, 13114, 13115, 22374,
			26154, 4181, 991, 11184, 21676, 11339, 24277, 24763,
			24765, 24767, 24771, 6199, 5561, 20426, 27159, 27161,
			20425, 27158, 27160, 30122, 30113, 30390, 30392, 27172,
			27297, 6104, 550, 11169, 11171, 24061, 20888, 10563,
			29098, 31343, 33178, 796, 1685, 10860, 10861, 21555,
			23654, 22364, 13195, 4850, 12935, 685, 11183, 11179,
			22428, 31121, 5506, 25829, 23023, 23025, 23027, 23031,
			23033, 23035, 13190, 11182, 695, 21680, 10537, 24053,
			8860, 27192, 25650, 26602, 33174, 26886, 11733, 11732,
			11731, 11730, 13187, 7756, 31399, 970, 972, 21674,
			619, 620, 21666, 29893, 6901, 29275, 33120, 23865,
			9044, 9046, 9048, 9050, 13074, 13075, 13076, 13077,
			26948, 26945, 466, 8951, 8930, 8949, 8950, 26629,
			26577, 30365, 197, 10535, 10541, 10539, 10540, 4495,
			26549, 26887, 26894, 26896, 26889, 195, 25206, 25205,
			25204, 25203, 11178, 20396, 20395, 20394, 20393, 6966,
			33133, 7678, 4238, 26247, 33184, 21313, 10476, 10946,
			10562, 22941, 22943, 22945, 13524, 13533, 30402, 27179,
			23557, 23555, 23553, 23551, 6200, 11704, 25207, 10532,
			8938, 9054, 25228, 8946, 21826, 21832, 21829, 21820,
			7774, 9474, 10934, 13525, 9007, 2570, 21138, 21136,
			21134, 21129, 1480, 4043, 675, 2203, 7636, 5558,
			954, 20587, 22321, 22437, 13205, 2959, 5733, 22541,
			2518, 30890, 8940, 8941, 10947, 20607, 23601, 27185,
			27111, 6897, 11719, 20422, 10882, 9012, 10549, 26960,
			6721, 31016, 28773, 26731, 26735, 26733, 23565, 23563,
			23561, 23559, 4037, 23581, 23579, 23577, 23575, 27182,
			4041, 27165, 3829, 27306, 27214, 8977, 8934, 28958,
			21664, 10512, 26706, 28798, 22664, 12842, 28771, 6679,
			6680, 21772, 9003, 3745, 23624, 22047, 22504, 24198,
			31756, 32926, 2574, 1848, 1854, 20390, 25831, 13382,
			13563, 4186, 24299, 621, 3680, 21682, 7778, 8857,
			6097, 26132, 29679, 33058, 26009, 33056, 25994, 26012,
			33062, 26018, 26069, 26057, 26126, 26006, 26015, 26105,
			28481, 26144, 29649, 26054, 29667, 28514, 26123, 26081,
			26135, 26141, 25991, 28490, 29670, 29652, 26060, 26042,
			26021, 26120, 28478, 29664, 28484, 26090, 28511, 26084,
			26087, 26048, 26075, 28496, 26102, 26111, 26093, 33054,
			26147, 29676, 28502, 28508, 28526, 26063, 26045, 29655,
			26072, 26117, 26138, 29673, 26051, 28499, 28523, 26114,
			25465, 8868, 3468, 3466, 3467, 3469, 3465, 25451,
			25646, 993, 10948, 6806, 965, 9013, 2749, 2750,
			2751, 2753, 2754, 2755, 2756, 3619, 3620, 3621,
			3623, 3624, 3625, 3626, 3643, 3644, 3645, 3647,
			3648, 3649, 3650, 21570, 21576, 21574, 21578, 31351,
			733, 20798, 7800, 24063, 25201, 19637, 33231, 25197,
			20397, 27797, 21712, 3273, 27178, 10536, 10561, 23599,
			25342, 23613, 24067, 23589, 23587, 23585, 23583, 10949,
			33063, 27833, 27834, 27835, 11702, 25461, 8866, 3458,
			3456, 3457, 3459, 3455, 4446, 25445, 30679, 9030,
			9042, 22601, 3893, 5732, 5507, 3062, 23183, 464,
			24761, 9009, 26885, 9004, 29409, 30386, 23549, 23547,
			23545, 23543, 20551, 20550, 20549, 20548, 23573, 23571,
			23569, 23567, 26757, 26751, 27314, 20703, 20527, 1941,
			8981, 24219, 3231, 3805, 6083, 23510, 23514, 10859,
			20890, 8022, 799, 23502, 25567, 24057, 6079, 30123,
			27312, 24071, 22510, 24069, 22416, 27310, 22512, 30382,
			8862, 945, 966, 5568, 30398, 10514, 29388, 31123,
			6529, 27358, 4051, 985, 30105, 9010, 23637, 23634,
			595, 8987, 27171, 29580, 33194, 4185, 21718, 13396,
			20893, 33229, 13537, 13536, 26605, 26609, 26607, 7677,
			25474, 30689, 28567, 33044, 28564, 33047, 28561, 13535,
			24187, 13530, 33188, 8861, 33182, 26882, 11175, 27191,
			13151, 26547, 26544, 26548, 26545, 26546, 21562, 13273,
			22191, 9662, 31054, 20895, 30401, 28972, 710, 26932,
			21872, 29809, 27190, 27189, 23636, 23635, 22514, 27831,
			23615, 27904, 27832, 27900, 23839, 23879, 13348, 33198,
			33196, 8842, 26467, 26465, 8839, 26463, 11673, 11672,
			11671, 11669, 11668, 11667, 11666, 27869, 25517, 30685,
			23106, 8851, 24859, 2575, 27295, 11687, 11716, 1829,
			1827, 1825, 1823, 26883, 23834, 23871, 1465, 13142,
			13143, 13144, 2885, 30399, 13529, 27162, 943, 10515,
			6084, 22208, 26723, 26727, 1843, 29969, 1765, 10534,
			26576, 4039, 27181, 4042, 12638, 27164, 3831, 3832,
			3834, 27186, 6807, 29449, 27839, 27841, 27840, 23617,
			27920, 13012, 13014, 29972, 31084, 26914, 19677, 25760,
			32871, 29971, 1464, 13052, 13054, 25637, 12988, 12990,
			24607, 24613, 24595, 24589, 24611, 24605, 24603, 24601,
			24598, 26705, 24621, 9433, 13155, 31875, 31890, 31841,
			31877, 28017, 29482, 23083, 21724, 12960, 27293, 30002,
			26969, 22521, 25495, 25497, 22516, 24287, 31111, 21885,
			11256, 27426, 25114, 25112, 31142, 4278, 29973, 20791,
			6666, 13131, 13132, 26356, 27552, 20275, 10881, 9040,
			9028, 9034, 12849, 23875, 13050, 24711, 11738, 21766,
			12833, 29244, 29246, 29248, 30644, 29309, 22971, 10028,
			10027, 26908, 12972, 12974, 21768, 21600, 21604, 21608,
			21602, 29303, 29297, 25975, 28140, 19732, 12786, 33007,
			11932, 11933, 26884, 21584, 21582, 29301, 29955, 13000,
			32864, 32869, 33005, 24855, 30744, 11929, 27693, 24229,
			20993, 20996, 10996, 31817, 233, 24882, 26895, 26892,
			26893, 26890, 26898, 26891, 26897, 26888, 9032, 9036,
			6319, 6885, 29307, 22947, 24861, 29996, 7329, 10879,
			24735, 28329, 26815, 21307, 33010, 13024, 13026, 25478,
			10552, 32867, 32863, 686, 32399, 13163, 3827, 3828,
			3830, 13256, 12804, 28493, 26066, 26099, 33060, 28520,
			26096, 26129, 26108, 26039, 28517, 26036, 25997, 28505,
			26078, 26003, 26000, 28529, 21276, 21572, 29299, 29295,
			13233, 19634, 12823, 12629, 12627, 12625, 12984, 12986,
			9038, 31732, 29535, 31441, 28924, 11729, 11728, 11727,
			11726, 6285, 6283, 21764, 28570, 21566, 21564, 21568,
			24617, 27684, 27681, 13110, 13111, 10851, 20981, 20984,
			20977, 20979, 13046, 3833
		));
	}

	private static void addQuests(Map<String, List<Integer>> m)
	{
		// QUESTS — 2141 items
		//   Quest items (1826), Quest reward equipment (315)
		m.put(TAG_QUESTS, Arrays.asList(
			// === Quest items ===
			3904, 3906, 3908, 3912, 3914, 3916, 3918, 3952,
			3954, 3956, 3960, 3962, 3964, 3966, 2365, 446,
			3731, 3726, 3730, 3725, 3724, 3723, 22764, 22763,
			5589, 6122, 10495, 9627, 10840, 6949, 10494, 718,
			1508, 9103, 2888, 24842, 2969, 26905, 28461, 4178,
			30989, 31365, 5578, 11680, 1858, 29539, 29552, 28448,
			9693, 4436, 22086, 22033, 27300, 5588, 3270, 701,
			1498, 1499, 1497, 11003, 1842, 6792, 24688, 23071,
			27607, 22093, 28409, 22775, 27597, 30963, 11181, 681,
			28369, 28375, 671, 4428, 1816, 4447, 6543, 7498,
			11679, 21262, 23072, 23082, 25753, 25820, 27299, 27543,
			30960, 6094, 10841, 708, 28345, 28346, 28351, 28355,
			23791, 1505, 1528, 738, 28385, 29517, 11048, 4195,
			30320, 11049, 7508, 4489, 28456, 11156, 600, 29542,
			9932, 3894, 11050, 26958, 7839, 29551, 2407, 6793,
			9933, 7564, 29884, 21060, 10489, 783, 1841, 3216,
			3218, 3220, 3221, 28390, 30311, 30310, 4574, 28364,
			28378, 7899, 7833, 2886, 2887, 7973, 7815, 9717,
			6123, 6946, 10895, 6947, 624, 29905, 3230, 7908,
			730, 10870, 10173, 422, 1474, 38, 21, 23782,
			4622, 11678, 4620, 4808, 6650, 28357, 1582, 1581,
			9692, 9694, 9698, 9696, 9690, 722, 10889, 4659,
			2954, 6711, 6710, 2875, 4670, 28453, 22407, 9650,
			25147, 25799, 6064, 9613, 22, 6644, 23780, 6086,
			740, 23792, 9616, 9615, 3719, 6089, 9614, 25803,
			4415, 1817, 25794, 618, 21530, 7813, 4272, 7950,
			604, 757, 1509, 29878, 7144, 4829, 4248, 4817,
			19515, 292, 711, 5065, 4569, 4570, 4571, 28804,
			26965, 28805, 6953, 6818, 10894, 3692, 30945, 739,
			7515, 28970, 4442, 29519, 7146, 9591, 6081, 690,
			1469, 25811, 763, 4431, 10177, 2418, 5585, 5602,
			5008, 7490, 7489, 7488, 7473, 7474, 7475, 7476,
			19517, 7622, 4693, 4687, 6712, 30, 28968, 716,
			7961, 688, 4209, 756, 7542, 7001, 33008, 22084,
			7118, 7149, 4579, 7119, 7147, 7145, 4678, 30312,
			30950, 1818, 29922, 7956, 9646, 9647, 9648, 6766,
			7905, 29534, 769, 3114, 21759, 21775, 22365, 22367,
			22760, 22777, 22414, 3706, 973, 28403, 19560, 29428,
			707, 6759, 26955, 709, 4273, 28573, 4443, 90,
			5601, 28414, 6545, 741, 7472, 25145, 26286, 23794,
			10594, 23814, 23815, 23816, 28425, 30316, 30941, 25804,
			26906, 6638, 10874, 19569, 29545, 10586, 19567, 3102,
			4426, 6635, 9681, 27532, 29883, 4201, 4200, 31341,
			4205, 7530, 21263, 7463, 7470, 4488, 23832, 28455,
			10593, 7519, 3161, 1538, 9720, 9718, 10893, 7630,
			10899, 780, 1813, 1808, 28462, 26959, 11032, 11033,
			608, 25963, 28133, 2380, 23802, 28575, 23804, 23808,
			4313, 6653, 25800, 4245, 4838, 5584, 5577, 26962,
			3702, 6643, 23779, 11158, 793, 7857, 11001, 2387,
			19636, 25810, 11031, 1467, 28439, 25815, 1819, 27368,
			2529, 9683, 4197, 10842, 25814, 30317, 19562, 29544,
			25796, 6457, 31111, 21799, 25798, 6748, 6747, 6749,
			7902, 28393, 28419, 2408, 3395, 3846, 22761, 4430,
			6770, 7497, 27515, 3267, 29521, 4617, 420, 1492,
			1573, 2409, 9682, 9589, 9590, 28410, 30968, 4811,
			22087, 22092, 22088, 6967, 4180, 7478, 33006, 28420,
			771, 7408, 9067, 11154, 11151, 11157, 3263, 29925,
			2957, 2968, 3266, 1468, 27600, 29596, 28458, 28132,
			25706, 7629, 27595, 1501, 0, 5056, 5060, 4568,
			7509, 7514, 10167, 29553, 28445, 9695, 6077, 6649,
			23798, 23800, 27223, 2893, 2892, 28809, 30307, 1820,
			11279, 19559, 4686, 21054, 28417, 20722, 9066, 30949,
			3727, 3732, 10486, 7496, 10831, 9085, 4007, 524,
			522, 525, 28965, 7544, 7546, 6754, 7545, 21260,
			523, 21259, 21800, 28964, 22768, 29540, 7967, 29530,
			29538, 21798, 4654, 415, 28437, 7477, 3698, 7893,
			23818, 5064, 5067, 4191, 26903, 4593, 3268, 782,
			24254, 9903, 10179, 6718, 31379, 2384, 29554, 1583,
			7872, 28447, 9699, 3708, 6673, 29549, 27, 26,
			22413, 1554, 1811, 31328, 31327, 21662, 28983, 6646,
			23784, 9687, 9688, 9689, 1821, 3699, 28551, 7538,
			7942, 3728, 3733, 3741, 26356, 26358, 26360, 26364,
			26362, 3736, 3722, 11007, 3729, 25146, 28981, 7109,
			10884, 4190, 4189, 4668, 22762, 553, 7881, 7827,
			337, 7518, 3897, 3898, 7824, 4674, 750, 294,
			296, 785, 788, 4075, 4004, 22590, 26587, 26585,
			26583, 26581, 7812, 5009, 721, 723, 2947, 2950,
			10175, 3693, 11210, 2949, 2944, 2951, 2948, 2946,
			3694, 4624, 3895, 28468, 3261, 4182, 9901, 17,
			28817, 9609, 6642, 23778, 23783, 6088, 9612, 23793,
			9611, 6092, 9610, 25608, 28424, 11196, 1527, 29558,
			1529, 1588, 11155, 2391, 704, 7528, 7527, 7517,
			9594, 3215, 3264, 9082, 28803, 26594, 9088, 9090,
			1856, 7108, 28442, 30940, 5579, 9652, 24672, 4604,
			11682, 28408, 4487, 11173, 6639, 19521, 1504, 4001,
			9902, 2403, 786, 744, 7959, 10834, 11052, 4464,
			4416, 29427, 7951, 25968, 7634, 21766, 1494, 727,
			728, 28343, 28344, 28353, 748, 19, 4682, 15,
			26575, 5610, 26957, 742, 29548, 3696, 3697, 26573,
			1502, 1496, 1500, 4671, 7875, 29887, 29897, 28361,
			27604, 27608, 1584, 28464, 28806, 22041, 26366, 28974,
			9617, 9620, 9619, 9618, 22079, 28967, 11013, 28966,
			31377, 28, 761, 794, 23806, 696, 3207, 3103,
			2945, 4427, 7941, 6648, 7914, 30969, 12936, 27289,
			27605, 21768, 4590, 7845, 2967, 3845, 4203, 6755,
			24686, 7810, 19519, 21797, 6996, 3153, 21394, 3155,
			431, 3164, 6716, 4595, 27511, 10885, 10898, 3711,
			6113, 6112, 6118, 7516, 2423, 4589, 30961, 10600,
			77, 33004, 76, 1815, 9094, 3206, 5605, 28413,
			30965, 28977, 28384, 25801, 19523, 30962, 9655, 6796,
			23516, 9724, 30967, 6819, 421, 2149, 28460, 28454,
			5062, 3707, 24075, 4196, 602, 4204, 4615, 6121,
			6756, 6757, 7966, 21774, 11009, 691, 692, 693,
			83, 27603, 9719, 28428, 7972, 10832, 26961, 4684,
			416, 6479, 28808, 31397, 29573, 3714, 25704, 25809,
			611, 612, 613, 614, 22081, 28415, 7970, 7971,
			28986, 3712, 787, 743, 9077, 24261, 9076, 6994,
			4020, 4022, 11058, 6645, 23781, 11211, 29550, 9592,
			18, 33117, 28587, 2395, 4703, 16, 6950, 2410,
			3718, 5604, 33007, 30318, 27298, 31392, 30944, 28980,
			22037, 21052, 3847, 1535, 1536, 1537, 22009, 22010,
			22011, 22013, 22014, 22015, 22016, 4274, 4275, 4276,
			4187, 3130, 24940, 9089, 30308, 31330, 1542, 9725,
			20892, 5066, 24030, 24031, 24032, 24033, 755, 9633,
			9649, 26942, 10876, 10174, 10873, 5586, 11197, 33128,
			7471, 30939, 28449, 9697, 7958, 2966, 1586, 10597,
			10598, 10599, 705, 4253, 7635, 7851, 24260, 4033,
			3166, 4006, 7854, 3167, 29078, 29079, 7869, 28969,
			26591, 6071, 29525, 29526, 28401, 4490, 4492, 7532,
			2953, 28341, 28342, 28350, 28352, 6769, 11198, 33005,
			22404, 22402, 22403, 19505, 21261, 23069, 4247, 6095,
			3222, 4005, 26572, 28451, 4837, 25152, 4237, 4235,
			6651, 19558, 703, 5581, 10833, 4597, 4598, 21056,
			21057, 21058, 29524, 9025, 30946, 31337, 3, 27601,
			603, 4188, 10178, 28430, 7578, 23796, 31400, 4818,
			21837, 4839, 2372, 7842, 2377, 277, 1485, 31338,
			22411, 1493, 22588, 24682, 22051, 22410, 22774, 9947,
			2385, 28438, 28421, 9601, 1769, 9604, 7486, 7484,
			9603, 9602, 6821, 1481, 1482, 1483, 23812, 587,
			588, 6458, 6459, 25797, 28431, 9934, 4432, 9684,
			9685, 9686, 4572, 4573, 1488, 1489, 1490, 2340,
			677, 678, 10585, 11036, 2424, 3128, 5012, 10592,
			28366, 28380, 28405, 1577, 4283, 26569, 11165, 11167,
			4621, 758, 4623, 23838, 23878, 4199, 1510, 1486,
			424, 4689, 6955, 6455, 26284, 26280, 26282, 9723,
			10871, 31363, 10872, 31297, 433, 418, 6468, 7628,
			7143, 28987, 29879, 273, 6768, 274, 4494, 4500,
			10496, 24262, 4244, 666, 4440, 7468, 3214, 7811,
			6771, 2394, 22409, 28382, 21531, 10886, 29547, 28434,
			6542, 271, 6073, 9727, 9728, 29867, 29950, 3135,
			28412, 3709, 28436, 1849, 1853, 7969, 1812, 7410,
			28975, 3213, 31375, 6995, 7896, 28463, 714, 14,
			7818, 7120, 29546, 25964, 609, 7836, 24, 11008,
			300, 28422, 7529, 338, 7543, 7577, 4603, 25793,
			6546, 33009, 7572, 1470, 9597, 23, 6640, 23776,
			6085, 3742, 4610, 4445, 9600, 23795, 6454, 7483,
			7482, 7481, 9599, 6090, 9598, 25, 30103, 6954,
			26592, 22589, 28432, 6820, 2373, 2374, 2375, 7121,
			7148, 28433, 28423, 291, 10492, 25824, 290, 28347,
			28348, 28354, 29541, 11164, 22096, 28368, 28376, 5063,
			6066, 10866, 22083, 1855, 7533, 9716, 2379, 7649,
			30964, 4498, 7155, 11046, 25795, 25802, 25805, 6956,
			1984, 6093, 11044, 11045, 21758, 10830, 276, 21053,
			28418, 29523, 4810, 6467, 6466, 11199, 3849, 4484,
			9904, 29904, 28349, 4082, 6958, 9943, 6985, 6986,
			6988, 6987, 6945, 6948, 21055, 28416, 4700, 26574,
			19527, 28392, 21529, 21528, 26956, 26952, 28443, 4575,
			4578, 4576, 4577, 456, 29880, 29881, 29882, 11681,
			717, 19511, 23773, 6758, 7968, 9721, 10485, 719,
			5519, 3704, 1466, 9680, 7887, 683, 9653, 3738,
			2882, 1552, 3168, 3104, 10488, 3417, 27602, 10857,
			28367, 28377, 4673, 28370, 28819, 2397, 729, 6969,
			4444, 25816, 5603, 280, 281, 282, 283, 279,
			11054, 28807, 25813, 85, 29927, 10897, 11204, 11202,
			11205, 4816, 29572, 1802, 1800, 1798, 10176, 1804,
			1806, 4658, 2399, 2400, 2401, 5011, 7637, 10856,
			11002, 4814, 1530, 2376, 720, 10904, 10950, 9715,
			28818, 6817, 7574, 28441, 28465, 7511, 6715, 9726,
			3720, 4672, 28452, 28978, 6772, 26590, 4606, 4607,
			4605, 7576, 7580, 7860, 26589, 6460, 9095, 5580,
			7512, 6791, 6636, 28450, 24944, 4002, 672, 670,
			669, 2396, 4691, 7513, 6119, 84, 1549, 4183,
			4618, 6785, 25631, 28459, 28979, 25967, 3746, 28471,
			4425, 28976, 3109, 4704, 6989, 7002, 6997, 6999,
			6998, 7000, 699, 26954, 27519, 28816, 29876, 30954,
			606, 3269, 29906, 27598, 28130, 28360, 4619, 27599,
			3713, 4836, 28383, 28427, 25965, 29535, 28388, 29543,
			26904, 26593, 28574, 3169, 7579, 3700, 28406, 3209,
			25812, 25966, 417, 29531, 28365, 28379, 9080, 9081,
			9079, 11010, 29533, 11034, 22095, 11056, 623, 7411,
			7150, 2388, 29903, 607, 23512, 23007, 24684, 28394,
			7573, 7575, 1850, 673, 6072, 29536, 4557, 23497,
			28389, 7632, 1851, 7878, 29898, 28435, 22506, 21764,
			33126, 7633, 24073, 22049, 1579, 29926, 5592, 31359,
			5583, 11203, 2393, 2378, 22415, 24690, 9658, 9657,
			9656, 23504, 23506, 23508, 1, 9665, 4809, 6788,
			6789, 4194, 31393, 1857, 419, 29877, 6096, 6098,
			3721, 3701, 19513, 10493, 4655, 784, 4249, 4277,
			23067, 3896, 28982, 22085, 7884, 3265, 3262, 6478,
			676, 6952, 29532, 27596, 789, 4601, 4602, 28402,
			29518, 26633, 7890, 10490, 22406, 22408, 28386, 25702,
			4683, 7821, 1487, 22769, 22770, 22771, 22773, 1822,
			6719, 24256, 682, 3688, 2386, 3703, 11006, 24258,
			24259, 31373, 10875, 7465, 28404, 28973, 21756, 4616,
			22035, 3734, 3737, 3735, 24255, 28363, 2389, 2390,
			22405, 5582, 9086, 29556, 6456, 9621, 9624, 9623,
			9622, 1532, 2425, 7911, 24674, 24675, 24673, 9087,
			625, 11012, 4656, 28429, 27518, 1503, 3710, 2964,
			29522, 29555, 22094, 28446, 9691, 4085, 24938, 24939,
			759, 3705, 4435, 4434, 29527, 29528, 29529, 9593,
			22591, 7866, 30936, 28407, 7957, 1476, 20, 4485,
			4486, 6453, 6469, 6464, 6461, 26579, 2421, 11035,
			29928, 1491, 28444, 6957, 7830, 10896, 27523, 10891,
			3744, 27513, 28457, 6713, 1472, 9605, 6641, 23777,
			6087, 9608, 7495, 7494, 7493, 7492, 9607, 6091,
			9606, 28579, 749, 735, 6993, 610, 11011, 8870,
			8887, 4078, 7848, 7863, 12934, 21770, 1507, 33118,
			2862, 30973, 5767, 4, 27369, 20611, 30966, 7995,
			28334, 25730, 12615, 11323, 1852, 28295, 24691, 668,
			605, 13116, 7540, 19566, 7491, 7531, 7520, 3127,
			7570, 753, 10, 6, 12, 8, 4184, 1840,
			3136, 3137, 432, 2404, 19568, 30819, 30822, 29217,
			7521, 7568, 23833, 23837, 28577, 23870, 23877, 9625,
			9626, 4077, 1978, 9654, 11217, 2126, 4251, 26571,
			7996, 28319, 27285, 27219, 1550, 3901, 601, 11060,
			9018, 9016, 1909, 1533, 1525, 1531, 23835, 4421,
			6714, 78, 30628, 22106, 30893, 3157, 7997, 275,
			293, 298, 423, 1543, 1544, 1545, 1546, 1547,
			1548, 2411, 5010, 9722, 11039, 30951, 4591, 9651,
			28325, 3848, 21762, 22045, 9019, 9024, 9023, 9022,
			9021, 12932, 3180, 1839, 7998, 4239, 4241, 4213,
			6104, 2866, 2871, 1585, 3426, 3424, 7487, 7485,
			23874, 2339, 970, 26577, 10890, 29216, 7566, 3150,
			2148, 29076, 1763, 7480, 23906, 1480, 1534, 19564,
			26960, 32083, 31395, 3745, 3414, 3412, 3410, 3408,
			3419, 3418, 3416, 4186, 3179, 1526, 7479, 10812,
			9017, 4446, 3224, 10587, 6083, 23510, 23514, 8837,
			21047, 4185, 11175, 4438, 2861, 6084, 2341, 1843,
			1765, 26576,

			// === Quest reward equipment ===
			774, 773, 4035, 1478, 4028, 7807, 7808, 7809,
			7806, 23785, 23787, 23789, 87, 616, 4027, 4502,
			4284, 6752, 6750, 9055, 10491, 287, 7918, 1052,
			2405, 4677, 6544, 278, 5609, 26969, 26967, 9644,
			9640, 11195, 10595, 32808, 32807, 8871, 10500, 3208,
			746, 29566, 29570, 29568, 4611, 7535, 6547, 10171,
			9729, 29560, 29562, 29564, 22433, 29872, 29870, 22435,
			24695, 10172, 13117, 13118, 13119, 13120, 7534, 31357,
			6082, 5608, 1506, 552, 295, 747, 589, 288,
			4567, 4026, 5607, 10862, 2406, 732, 22398, 13137,
			13138, 11136, 11138, 4031, 21760, 75, 74, 21059,
			1495, 19525, 9102, 9091, 9092, 9093, 3689, 4021,
			28426, 430, 9733, 4023, 6069, 6070, 6068, 6065,
			6067, 6541, 4024, 4025, 6548, 286, 86, 3695,
			9059, 284, 285, 31353, 31355, 29911, 25822, 9058,
			9813, 7917, 6773, 4202, 28329, 4657, 6786, 6787,
			7648, 7639, 7647, 7646, 7644, 7643, 7642, 7641,
			24693, 9083, 6790, 4236, 10839, 10836, 10838, 10837,
			1796, 2963, 2402, 1846, 1845, 1844, 4083, 3107,
			778, 4086, 24678, 24680, 13141, 26567, 2952, 9056,
			4029, 4030, 19476, 7459, 11061, 1540, 30955, 10499,
			10498, 10887, 7462, 11014, 29931, 7457, 10880, 24699,
			24697, 667, 9945, 9944, 3844, 12612, 12610, 25818,
			12608, 7454, 27418, 10865, 10863, 10864, 29912, 29914,
			29915, 6707, 777, 9642, 7451, 3105, 775, 23844,
			23842, 23848, 33103, 7537, 23889, 23891, 23983, 23894,
			3839, 3841, 3843, 12611, 6746, 1835, 1833, 27783,
			7461, 3204, 1187, 22400, 772, 11200, 30970, 7435,
			2890, 29868, 29874, 3690, 3691, 6125, 6126, 13079,
			35, 13128, 3758, 7441, 7668, 6106, 6111, 6110,
			6107, 6108, 27422, 776, 7453, 19481, 10828, 3840,
			1718, 1409, 12658, 1580, 11088, 7455, 29932, 10581,
			25979, 7447, 9100, 9101, 9099, 9096, 9098, 9104,
			9084, 9097, 7409, 7449, 7458, 19556, 13113, 13114,
			13115, 22114, 24857, 4212, 26963, 2883, 767, 1013,
			10877, 27424, 30947, 27010, 426, 428, 9676, 9674,
			9672, 9678, 9814, 29929, 24942, 7445, 7460, 4081,
			10858, 27420, 7443, 29933, 7439, 7437, 7456, 26910,
			31398, 10487, 3842, 24266, 24676, 1005, 6611, 13108,
			13109, 29930, 7433
		));
	}

	private static void addSailing(Map<String, List<Integer>> m)
	{
		// SAILING — 546 items
		//   Sailing tools & navigation (5), Ship components (20), Shipbuilding
		//   materials (10), Cargo & contracts (133), Sailing outfit & rewards
		//   (378)
		m.put(TAG_SAILING, Arrays.asList(
			// === Sailing tools & navigation ===
			32866, 32868, 31985, 31807, 31803,

			// === Ship components ===
			32053, 32056, 32074, 32077, 32071, 32065, 32080, 32068,
			32062, 32050, 32044, 32408, 32059, 32407, 31288, 31292,
			32399, 32047, 32041, 31261,

			// === Shipbuilding materials ===
			31245, 31475, 31976, 32529, 31979, 31973, 31967, 31964,
			31982, 31970,

			// === Cargo & contracts ===
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
			32562, 32599, 32474, 32459, 32460, 32461, 32475, 32775,
			32481, 32574, 32496, 32705, 32514, 32462, 32476, 32706,
			32447, 32463, 32516, 32777, 32691, 32545, 32630, 32531,
			32671, 32517, 32518, 32499, 32478, 32546, 32779, 32479,
			32806, 32563, 32559, 32405, 32807,

			// === Sailing outfit & rewards ===
			31912, 31926, 31940, 32011, 32845, 32826, 32096, 32871,
			32876, 32102, 32836, 32817, 33143, 32087, 32851, 31733,
			31782, 31989, 31478, 31472, 32431, 31889, 31831, 31888,
			31838, 31860, 31837, 31878, 31905, 31854, 31851, 31863,
			31893, 31856, 31858, 31849, 31840, 31881, 31886, 31875,
			31833, 31892, 31864, 31873, 31845, 31848, 31883, 31861,
			31901, 31882, 31839, 31843, 31900, 31899, 31902, 31847,
			31844, 31885, 31846, 31842, 31890, 31887, 31834, 31895,
			31897, 31898, 31859, 31891, 31855, 31835, 31832, 31852,
			31862, 31836, 31894, 31876, 31871, 31874, 31841, 31857,
			31872, 31884, 31896, 31877, 31850, 31853, 31949, 31779,
			31797, 31961, 31251, 31906, 31918, 31932, 31999, 32809,
			32828, 32820, 32839, 32364, 31432, 31745, 32432, 31869,
			31469, 32892, 31805, 31865, 31300, 31403, 31298, 31401,
			32115, 32406, 31916, 31930, 32409, 31944, 32017, 32410,
			31996, 31406, 32404, 32865, 32819, 32838, 31946, 31788,
			32403, 33074, 32380, 32366, 32368, 32371, 32377, 32383,
			32374, 32849, 31253, 32859, 32844, 32825, 32402, 31408,
			31794, 31412, 32812, 32831, 31746, 32106, 31758, 32810,
			32829, 31757, 31466, 32434, 31823, 31420, 31785, 32093,
			31908, 31920, 31934, 32002, 31435, 31814, 32032, 32020,
			32038, 32023, 32029, 32035, 32853, 32026, 32889, 31463,
			32422, 32428, 32423, 32421, 32426, 32430, 32424, 32427,
			32429, 32425, 32822, 32841, 32857, 32388, 32389, 32390,
			32392, 32393, 32394, 32395, 32386, 32110, 31910, 31924,
			31938, 32008, 32846, 32827, 32864, 32869, 31903, 32813,
			31954, 31957, 32832, 32861, 32814, 32833, 32843, 32824,
			31817, 31484, 32855, 31809, 31424, 32834, 32815, 31800,
			31734, 31879, 31959, 32882, 31438, 31724, 31722, 31914,
			31928, 31942, 32014, 32867, 32863, 32401, 32099, 32113,
			32108, 31395, 32090, 31791, 31776, 32416, 32418, 32420,
			32417, 32419, 32415, 32413, 32412, 32411, 31773, 32847,
			32870, 32835, 32816, 31252, 31572, 31569, 31922, 31936,
			32005, 32821, 32840, 31732, 31441, 31768, 31952, 31428,
			31254, 32842, 32823, 31867, 32811, 32830, 31770, 31398,
			31820, 31487, 31371, 32837, 32818, 33145, 33144, 32433,
			32396, 31811, 32104, 31255, 31248, 31695, 32344, 32347,
			32315, 32339, 31567, 32355, 31559, 32331, 32904, 31490,
			32886, 31460, 31545, 31760, 31762, 31766, 31764, 31481,
			31511, 31623, 31744, 32307, 7534, 32312, 21728, 31235,
			32320, 32357, 31599, 31596, 31593, 31590, 32336, 31457,
			31543, 31579, 32907, 31386, 31505, 31492, 32921, 32902,
			31564, 31258, 31716, 31397, 8788, 32362, 31719, 31416,
			31513, 31297, 31703, 31692, 32341, 32309, 32317, 32333,
			31561, 32349, 31700, 31553, 32325, 31689, 32910, 31494,
			32085, 31756, 32414, 31283, 31738, 31736, 31742, 31740,
			31422, 31748, 31750, 31754, 31752, 31556, 31443, 31393,
			31515, 32360
		));
	}

	private static void addCosmetics(Map<String, List<Integer>> m)
	{
		// COSMETICS — 2564 items
		//   Cosmetic sets (14), Treasure trail cosmetics (2284), Holiday items
		//   (142), Ornament kits (55), Skill & event cosmetics (65), Decorative
		//   weapons & armour (4)
		m.put(TAG_COSMETICS, Arrays.asList(
			// === Cosmetic sets ===
			29712, 23124, 30331, 30334, 30337, 24469, 24472, 24475,
			24802, 24818, 24834, 13167, 23667, 13165,

			// === Treasure trail cosmetics ===
			24539, 27820, 27812, 25328, 25326, 25334, 25330, 25322,
			25332, 25324, 33086, 33080, 33084, 33082, 12437, 23345,
			23342, 10350, 10348, 10346, 23242, 21211, 26308, 10392,
			13262, 26901, 13070, 12377, 23270, 13020, 13022, 10296,
			10298, 10300, 10302, 10304, 22159, 22161, 22163, 22165,
			22167, 22169, 22171, 22173, 22175, 22177, 22179, 22181,
			22183, 22185, 22187, 22189, 22147, 2611, 2603, 23392,
			23395, 23398, 23401, 23404, 2609, 2601, 3475, 7334,
			7340, 7346, 7352, 7358, 30607, 13016, 13018, 27394,
			27402, 27410, 27442, 27392, 27400, 27408, 27388, 27396,
			27404, 27390, 27398, 27406, 27412, 12430, 29986, 29978,
			29982, 20056, 13218, 26227, 26225, 12197, 12199, 12466,
			12468, 12460, 12462, 12464, 27381, 20101, 20095, 20107,
			20098, 20104, 33018, 33012, 13288, 13345, 20251, 20113,
			19943, 33149, 20764, 12261, 12263, 12476, 12478, 12470,
			12472, 12474, 29628, 29622, 29625, 21900, 2460, 9749,
			13324, 12646, 26627, 25502, 25840, 26645, 8926, 8927,
			8925, 8924, 12273, 12275, 12486, 12488, 12480, 12482,
			12484, 20773, 20777, 20775, 28250, 31172, 6853, 11705,
			12245, 23291, 27039, 13322, 33124, 21245, 25137, 25135,
			25129, 25133, 25131, 32930, 6846, 11282, 26919, 26920,
			26921, 12355, 26332, 29931, 21209, 23108, 24331, 21230,
			2635, 7327, 24986, 12375, 1019, 2643, 20026, 9918,
			12524, 10402, 10400, 2476, 2595, 2587, 12996, 12998,
			2647, 10306, 10308, 10310, 10312, 10314, 2597, 20246,
			8956, 8995, 23366, 23369, 23372, 23375, 23378, 2593,
			2585, 3473, 3472, 7332, 7338, 7344, 7350, 7356,
			1015, 12445, 12447, 30603, 2524, 8963, 12992, 12994,
			20266, 19988, 19730, 2633, 7325, 630, 24981, 1021,
			12757, 9915, 12520, 25846, 10428, 10410, 10408, 10430,
			2464, 12301, 6865, 6868, 6875, 6876, 6877, 6878,
			8952, 8991, 650, 640, 1011, 7386, 7388, 4558,
			8959, 7394, 7396, 26929, 27806, 10322, 10318, 10320,
			10324, 10316, 6856, 6857, 9945, 9944, 29433, 24338,
			24340, 24342, 24344, 24346, 24348, 20110, 6828, 11912,
			30622, 1009, 26641, 26639, 23105, 12335, 24544, 24547,
			12363, 8968, 12211, 12968, 12970, 12213, 12223, 12215,
			12207, 12209, 12219, 27418, 30597, 12964, 12966, 24980,
			2649, 8955, 8994, 2520, 8962, 27485, 33099, 26615,
			19991, 20059, 26617, 21243, 10865, 10863, 10864, 21874,
			26643, 28248, 29912, 29914, 29915, 13679, 20272, 13680,
			24549, 27804, 30042, 13178, 7003, 6655, 6658, 6659,
			6654, 6657, 22719, 9946, 23351, 26613, 24546, 30722,
			30726, 30720, 30724, 30708, 24537, 24525, 12361, 1575,
			22680, 22681, 22682, 11280, 22361, 27643, 26625, 11019,
			11021, 11022, 11020, 26304, 11025, 11026, 11910, 2978,
			2979, 2980, 2981, 2982, 2983, 2984, 2985, 2986,
			2987, 2988, 2989, 2990, 2991, 2992, 2993, 2994,
			2995, 13071, 22687, 26935, 9642, 25712, 21396, 30648,
			30646, 26290, 26288, 23413, 27438, 22692, 22695, 22689,
			22701, 22698, 19695, 19697, 19689, 19691, 19693, 20249,
			29781, 28601, 30579, 30581, 30583, 30585, 30587, 30589,
			30591, 30593, 30595, 24543, 26925, 20838, 20846, 20840,
			20842, 20844, 22678, 12958, 11919, 12959, 33093, 33096,
			33097, 12956, 12957, 27794, 26939, 632, 652, 642,
			20243, 20240, 12319, 13681, 21239, 23911, 23913, 23915,
			23917, 23919, 23921, 23923, 23925, 31760, 31762, 31766,
			31764, 24000, 23941, 23933, 23935, 23939, 23929, 23927,
			23937, 23931, 25500, 11708, 27248, 8966, 19915, 24733,
			19970, 2641, 24729, 25557, 19964, 19961, 19958, 19967,
			22686, 24191, 24189, 4559, 12540, 9755, 23294, 33269,
			33281, 33293, 33260, 33272, 33284, 33349, 33299, 33357,
			33368, 33345, 33355, 33351, 33347, 33353, 33362, 33266,
			33278, 33290, 33263, 33275, 33287, 33296, 33302, 13133,
			6390, 6386, 6834, 27783, 13188, 21723, 21722, 28904,
			29730, 29736, 29732, 29722, 29724, 29728, 29718, 29726,
			29734, 29720, 29716, 29740, 29764, 29760, 29744, 29756,
			29754, 29750, 29772, 29768, 29742, 29748, 29762, 29766,
			29738, 29758, 29746, 29752, 29770, 29714, 29704, 29688,
			29710, 29686, 29706, 29698, 29700, 29692, 29708, 29694,
			29696, 30491, 30487, 31130, 19727, 27810, 30611, 24034,
			24037, 24040, 12600, 23206, 27801, 21231, 27873, 30453,
			22684, 30970, 33147, 21252, 29443, 29441, 29437, 29439,
			27563, 22351, 22353, 24003, 24006, 24024, 24012, 24018,
			24009, 24015, 24021, 24027, 29868, 29874, 13183, 21873,
			21861, 21862, 20433, 20439, 20442, 20436, 9919, 12514,
			28672, 20008, 11990, 6382, 21865, 21186, 13330, 3006,
			6670, 21238, 10394, 27035, 21236, 21248, 28670, 28626,
			3771, 3789, 3763, 3775, 3761, 3767, 3759, 3765,
			3779, 3769, 3787, 3785, 3777, 3773, 3793, 3783,
			3781, 21229, 6188, 23288, 6183, 26936, 21228, 28138,
			5345, 23859, 9906, 6111, 6110, 23252, 25314, 27871,
			20836, 20659, 27414, 13036, 13038, 20149, 3486, 3488,
			20146, 3481, 3483, 8967, 23282, 20152, 30613, 27434,
			27799, 27802, 13655, 25336, 27818, 9472, 9470, 27800,
			25316, 25290, 12251, 12727, 25289, 12343, 12349, 12347,
			12345, 21247, 12303, 27422, 20208, 20205, 28663, 27560,
			22840, 27038, 30044, 12849, 13286, 13287, 13285, 13283,
			13284, 20023, 13328, 7323, 628, 24985, 1027, 12759,
			9916, 12518, 10432, 10414, 10412, 10434, 25284, 12307,
			6866, 6869, 6879, 6880, 6881, 6882, 8953, 8992,
			21311, 648, 638, 4563, 8960, 31034, 25604, 2894,
			2902, 2900, 8958, 8997, 2898, 2896, 2526, 8965,
			31181, 31184, 31190, 31187, 31208, 31231, 31233, 12845,
			31229, 12836, 12837, 26168, 27046, 26156, 26166, 27044,
			26820, 10506, 31285, 10448, 10442, 2675, 13335, 13336,
			3837, 3838, 2669, 10466, 1989, 20053, 30234, 27497,
			21354, 26182, 26170, 26180, 20792, 20794, 20796, 8928,
			26260, 24975, 19941, 24527, 7583, 13247, 19687, 21509,
			13320, 22679, 2631, 9770, 22355, 20128, 27428, 19699,
			20254, 20116, 19946, 12841, 30152, 26336, 12839, 12843,
			26338, 12855, 20779, 27267, 27257, 27259, 27261, 27263,
			27265, 28786, 22746, 21786, 21778, 21782, 12249, 33359,
			33365, 24993, 13184, 26631, 21282, 27440, 21208, 30493,
			12365, 12241, 12980, 12982, 12243, 12233, 12235, 12237,
			12227, 12239, 12229, 30599, 12976, 12978, 12810, 12811,
			12812, 32932, 27814, 27808, 29932, 21291, 19701, 24495,
			32921, 13277, 12885, 25524, 13245, 21745, 29786, 26250,
			23297, 6858, 6859, 21720, 20032, 12647, 24862, 6717,
			12357, 21875, 24866, 26334, 20164, 12371, 6550, 23381,
			6182, 6181, 6180, 19724, 27037, 12359, 20020, 27561,
			19985, 19979, 19976, 19973, 19982, 25348, 22473, 28252,
			13216, 24491, 26326, 26912, 20257, 20119, 19949, 28128,
			32928, 8969, 11023, 24535, 9764, 26278, 26924, 13201,
			24864, 6864, 13203, 27370, 13281, 31331, 21233, 6392,
			6398, 6396, 6394, 6400, 6406, 6404, 6402, 22386,
			3061, 3060, 3059, 3057, 3058, 12369, 12283, 12293,
			13008, 13010, 12281, 12277, 12285, 12295, 30605, 13004,
			13006, 2472, 23285, 20202, 23306, 19556, 12353, 25286,
			30154, 6665, 27562, 20083, 20092, 20086, 20080, 20089,
			27590, 21711, 13221, 13223, 12351, 12443, 12441, 27645,
			24855, 24857, 12325, 27875, 26348, 29836, 21748, 26330,
			21715, 26276, 26596, 19918, 20050, 26963, 20029, 27494,
			27822, 20851, 31117, 7321, 24987, 1031, 25844, 2470,
			21309, 23093, 23099, 23091, 23101, 23095, 23097, 7414,
			24541, 10396, 25838, 25609, 22358, 12428, 1561, 1567,
			11995, 12644, 12645, 12643, 12816, 12650, 12652, 1555,
			12655, 12649, 12703, 12648, 12921, 12651, 28655, 8971,
			28669, 28618, 28616, 28620, 28622, 20693, 30160, 12309,
			626, 24988, 6959, 12339, 12317, 12315, 12341, 12305,
			646, 636, 1013, 4564, 7112, 7124, 7130, 7136,
			7114, 12412, 7116, 7126, 7132, 7138, 2651, 20260,
			20122, 19952, 12516, 26635, 27424, 28126, 26619, 26621,
			30947, 13346, 13656, 30479, 12653, 24867, 27795, 21870,
			27796, 22316, 25606, 6852, 30793, 30791, 30783, 30787,
			30785, 30789, 12311, 2934, 24983, 1029, 10436, 10418,
			10416, 10438, 2468, 2942, 2940, 8957, 8996, 2938,
			2936, 8964, 27041, 9814, 28962, 11024, 30469, 30430,
			30410, 30418, 30426, 30477, 30428, 30455, 30465, 30404,
			30412, 30420, 30475, 30471, 30457, 30461, 30408, 30416,
			30424, 30467, 30560, 30563, 30473, 30406, 30414, 30422,
			29489, 29507, 28116, 21314, 30717, 29929, 11848, 12247,
			7319, 2904, 24984, 1007, 12323, 9914, 12522, 10424,
			10406, 10404, 10426, 25283, 2462, 2912, 2910, 2645,
			6867, 6870, 6871, 6872, 6873, 6874, 8954, 8993,
			21308, 2908, 2906, 4562, 8961, 10507, 27377, 27378,
			27379, 27380, 21235, 24989, 24990, 20665, 1025, 23185,
			20017, 20005, 6583, 27432, 27430, 21250, 13321, 20663,
			12856, 25287, 13327, 12397, 6186, 6185, 6187, 6184,
			12395, 12393, 12439, 4566, 22666, 12379, 23273, 2619,
			13032, 13034, 8464, 8466, 8468, 8470, 8472, 8474,
			8476, 8478, 8482, 8484, 8486, 8488, 8490, 8492,
			8494, 23212, 23215, 23218, 23221, 26262, 23324, 30609,
			13028, 13030, 21695, 10552, 27570, 20834, 12337, 21234,
			20047, 20041, 20044, 20035, 20038, 23318, 23312, 23315,
			25746, 10446, 10440, 13332, 2663, 26254, 30232, 13181,
			33146, 22675, 28801, 1419, 21246, 26918, 30161, 12840,
			24792, 26424, 26436, 26448, 26460, 26517, 26427, 26439,
			26451, 26500, 26430, 26442, 26454, 26433, 26445, 26457,
			20263, 20125, 19955, 5030, 5032, 5034, 5042, 5044,
			5046, 23300, 27420, 22494, 22496, 22498, 22500, 22502,
			9921, 9922, 24327, 9923, 9925, 24865, 9924, 5048,
			5050, 5052, 26649, 21273, 25282, 10398, 11916, 22350,
			26328, 27488, 24298, 21237, 28960, 23760, 29933, 20832,
			27568, 21849, 21857, 21855, 21847, 21851, 21853, 13217,
			21864, 10501, 10509, 27559, 28788, 27436, 31283, 26916,
			26917, 27416, 21232, 28603, 24992, 4613, 24323, 24321,
			24315, 24317, 24319, 24313, 31225, 27491, 24311, 24305,
			31222, 24307, 31224, 24309, 31223, 23495, 23363, 20590,
			25288, 6822, 22713, 12367, 20178, 20382, 20385, 8682,
			8684, 8688, 8690, 8692, 8694, 8696, 8698, 8700,
			8702, 8704, 8706, 8708, 8710, 8712, 20181, 30601,
			20376, 20379, 9702, 25285, 31738, 31736, 31742, 31740,
			27816, 9752, 7110, 7122, 7128, 7134, 26264, 31748,
			31750, 31754, 31752, 31193, 31196, 31202, 31199, 2639,
			20661, 26931, 13200, 21714, 2924, 2932, 2930, 2928,
			2926, 20217, 20214, 20211, 4315, 4333, 4335, 4337,
			4339, 4341, 4343, 4345, 4347, 4349, 4351, 4317,
			4353, 4355, 4357, 4359, 4361, 4363, 4365, 4367,
			4369, 4371, 4319, 4373, 4375, 4377, 4379, 4381,
			4383, 4385, 4387, 4389, 4391, 4321, 4393, 4395,
			4397, 4399, 4401, 4403, 4405, 4407, 4409, 4411,
			4323, 4413, 4325, 4327, 4329, 4331, 27040, 27483,
			25610, 23224, 13215, 21717, 26600, 25602, 26623, 21713,
			12432, 12434, 30302, 30303, 30304, 7771, 8970, 27463,
			6840, 22717, 22715, 6860, 6861, 6335, 6337, 6339,
			6341, 6351, 6361, 6371, 5036, 5038, 5040, 27352,
			634, 654, 644, 24376, 24413, 24411, 24403, 24393,
			24384, 24395, 24407, 24399, 24389, 24372, 24405, 24397,
			24387, 24466, 24382, 24378, 24374, 24380, 24460, 24409,
			24401, 24391, 13225, 26256, 12813, 12814, 12815, 10487,
			24542, 23255, 27798, 26933, 31133, 13177, 13179, 13352,
			13349, 13350, 13351, 24207, 24209, 24213, 24520, 24211,
			24215, 6347, 6359, 6369, 6379, 6345, 6355, 6365,
			6375, 6343, 6353, 6363, 6373, 6349, 6357, 6367,
			6377, 12844, 13186, 12771, 21992, 24794, 24810, 24826,
			24796, 24812, 24828, 24808, 24824, 24840, 24806, 24822,
			24838, 24800, 24816, 24832, 24804, 24820, 24836, 24676,
			24798, 24814, 24830, 9636, 9638, 9634, 795, 26930,
			28671, 21240, 21251, 21249, 21242, 21241, 21244, 1005,
			24297, 2637, 12313, 24982, 12321, 12763, 9913, 10420,
			10422, 2474, 12299, 4560, 2522, 20269, 21428, 21434,
			12838, 6556, 10508, 28246, 27479, 27481, 27473, 27477,
			27475, 24991, 23410, 23407, 29930, 26934, 20166, 6862,
			6863, 5024, 5026, 5028, 22394, 22388, 22392, 22390,
			30888, 2914, 1023, 12761, 9917, 2466, 2922, 2920,
			21310, 2918, 2916, 7803, 4079, 23757, 23908, 10450,
			10444, 2659, 13334, 2653, 26611, 7596, 7595, 6722,
			19912, 7594, 24863, 7592, 7593, 10344, 20011, 12424,
			23339, 23336, 28226, 10352, 12426, 10342, 27183, 20014,
			10334, 27201, 10332, 27200, 10330, 27199, 10340, 10338,
			10336, 12422, 26807, 26484, 26482, 13337, 13338, 2613,
			2605, 22127, 22129, 22131, 22135, 22137, 22139, 22141,
			22143, 22145, 22149, 22151, 22153, 22155, 22157, 2607,
			2599, 3474, 9773, 23309, 12436, 10362, 10354, 10366,
			23354, 29804, 20366, 29774, 20235, 12490, 26229, 26223,
			26221, 12494, 12496, 12492, 23197, 12203, 12195, 12193,
			13060, 13062, 12201, 32096, 29816, 29818, 23787, 20760,
			12506, 26716, 12510, 26715, 12512, 12508, 23200, 29605,
			20368, 26714, 12259, 12255, 12253, 12257, 29619, 21898,
			26720, 12498, 12502, 26718, 12504, 12500, 23203, 20370,
			12271, 12267, 12265, 13056, 12269, 26719, 8654, 8656,
			8658, 8660, 8664, 8672, 8674, 8676, 8680, 23240,
			12381, 12385, 12383, 12387, 2589, 2591, 2583, 19639,
			19641, 12453, 12455, 12449, 12451, 25870, 25872, 25874,
			25876, 25878, 25880, 25882, 28688, 7374, 7376, 7382,
			7384, 7331, 7390, 7392, 26488, 26490, 26492, 26494,
			20140, 25884, 25886, 25888, 25890, 25892, 25894, 25896,
			33021, 29519, 12221, 12205, 12217, 6656, 26524, 26520,
			26526, 26522, 1949, 9791, 9803, 9782, 27697, 27709,
			27721, 27733, 27745, 27757, 27769, 33023, 27705, 27717,
			27729, 27741, 27753, 27765, 27777, 33031, 27701, 27713,
			27725, 27737, 27749, 27761, 27773, 33027, 27123, 27127,
			27125, 29611, 12459, 12457, 12458, 24190, 33338, 28682,
			28902, 28051, 25378, 28037, 28055, 22234, 12373, 28065,
			12414, 28039, 26708, 28053, 28019, 19722, 12417, 28049,
			25373, 25918, 25916, 22244, 28033, 28027, 28057, 12797,
			23677, 25376, 22242, 28061, 12415, 28063, 12416, 28031,
			20000, 28041, 28059, 12418, 28029, 24043, 24046, 30445,
			30449, 30447, 30568, 30434, 30436, 30437, 30441, 30439,
			27119, 27117, 27115, 27100, 27253, 26471, 26469, 29872,
			29870, 7400, 7398, 7399, 9812, 28067, 13329, 9806,
			9800, 9785, 28175, 28173, 28171, 3797, 23246, 12774,
			12769, 27550, 6106, 6109, 6107, 6108, 20155, 23279,
			12391, 23258, 23264, 23267, 23261, 20161, 23276, 3485,
			12389, 20158, 20134, 1654, 25555, 25549, 25551, 25553,
			24758, 25084, 27459, 30060, 24746, 25072, 27447, 30048,
			24755, 25081, 27456, 30057, 13615, 24743, 25069, 27444,
			30045, 24752, 25078, 27453, 30054, 24749, 25075, 27450,
			30051, 24225, 7370, 7372, 7378, 7380, 7330, 658,
			19643, 19645, 26158, 27048, 13048, 10376, 10380, 10382,
			10378, 23188, 2673, 10454, 3835, 3836, 2671, 3480,
			10462, 10472, 10862, 26172, 26712, 28070, 9776, 20220,
			26496, 25734, 33002, 12598, 25731, 25733, 25736, 19997,
			20229, 33066, 33068, 24885, 9950, 23073, 33330, 33332,
			29615, 21784, 29617, 21776, 29613, 25066, 30347, 25371,
			30348, 25059, 30342, 25367, 30343, 21285, 25063, 30345,
			25369, 30346, 12231, 12225, 25521, 10059, 21198, 23384,
			12421, 12419, 12420, 13199, 12806, 23522, 27374, 27363,
			27366, 27372, 9794, 12291, 12287, 12279, 12289, 20199,
			23303, 26539, 26537, 26531, 21200, 26535, 26533, 12796,
			22249, 6548, 19720, 12807, 27246, 20226, 22842, 3695,
			28624, 2997, 9761, 10326, 21264, 21266, 30779, 30777,
			30781, 30557, 30459, 30554, 23357, 19994, 23249, 12596,
			9758, 31879, 6773, 12327, 12331, 12329, 12333, 19647,
			19649, 31049, 20137, 20131, 26486, 23230, 2627, 10286,
			10288, 10290, 10292, 10294, 8480, 8714, 8716, 8718,
			8722, 8724, 8726, 8728, 8730, 8732, 8734, 8736,
			8738, 8740, 8744, 2621, 2629, 2615, 23209, 2623,
			2617, 2625, 3476, 3477, 23330, 23332, 23334, 23321,
			23327, 7336, 7342, 7348, 7354, 7360, 9767, 31292,
			25739, 28254, 28256, 28258, 13040, 13042, 10384, 10388,
			23191, 2665, 20372, 2667, 13331, 10452, 3829, 2661,
			3479, 10464, 10458, 10470, 9788, 9797, 33335, 23389,
			25547, 12795, 20193, 8686, 8746, 8748, 8750, 8754,
			8756, 8758, 8762, 8766, 8768, 8770, 8776, 20196,
			20169, 20184, 20172, 20187, 20175, 20190, 10364, 7362,
			7364, 7366, 7368, 13197, 9779, 23444, 33036, 33326,
			33323, 33318, 33314, 21888, 21890, 24664, 24668, 24666,
			24463, 24370, 23235, 25910, 25912, 25898, 25900, 33320,
			33316, 33328, 20223, 26498, 25904, 25906, 26467, 26465,
			26463, 26473, 26477, 26475, 29607, 29609, 12773, 24678,
			24680, 20232, 10327, 10280, 2579, 9809, 13044, 10368,
			10372, 10374, 10370, 23194, 2657, 20374, 13333, 10456,
			3832, 3834, 2655, 3478, 10468, 10460, 10474, 660,
			740, 662, 656, 664,

			// === Holiday items ===
			12896, 12895, 12893, 12892, 12894, 12897, 30162, 30242,
			21218, 11847, 11862, 13343, 24431, 1055, 1042, 2422,
			27555, 1037, 13182, 13664, 13665, 13663, 23448, 21439,
			962, 32934, 27566, 12835, 21227, 11707, 30167, 30287,
			21219, 11997, 4565, 13185, 1961, 7928, 11027, 26926,
			21214, 26937, 7927, 26270, 24432, 24434, 26312, 26310,
			24436, 27588, 24433, 26292, 26272, 26294, 27578, 27576,
			27580, 27572, 27574, 24435, 30489, 26274, 21226, 21224,
			21217, 21216, 23446, 24443, 24437, 24428, 1053, 1044,
			13175, 23360, 26928, 24438, 13344, 9920, 24977, 21221,
			26927, 30165, 30269, 12399, 13173, 27554, 30168, 30296,
			1959, 24325, 1046, 11863, 30709, 24430, 1057, 1038,
			30166, 30278, 21223, 21222, 12891, 12890, 1050, 12888,
			12887, 12889, 21866, 21868, 21867, 27564, 21871, 26298,
			26316, 27557, 27828, 21225, 26314, 21220, 31227, 12861,
			27556, 1048, 30163, 30251, 21433, 21859, 22396, 1040,
			30164, 30260, 28035, 26710, 4310, 4304, 4308, 4302,
			4306, 4300, 4298, 25738, 25741, 21838,

			// === Ornament kits ===
			22246, 26713, 20068, 26717, 23237, 28017, 26707, 12538,
			22239, 12536, 22236, 20002, 12532, 26709, 12526, 25742,
			21202, 20065, 23227, 20074, 26421, 26479, 12798, 20062,
			25090, 12802, 20077, 28336, 20071, 27121, 12528, 33305,
			33308, 33311, 22231, 12534, 20143, 12800, 30451, 30432,
			30443, 27113, 27098, 26711, 12530, 27255, 25744, 26528,
			26541, 23348, 25099, 28690, 28684, 24670, 23232,

			// === Skill & event cosmetics ===
			26554, 26557, 26560, 26511, 26503, 26515, 26505, 26509,
			26513, 26507, 25046, 25056, 25037, 25025, 25010, 25054,
			25013, 25042, 25028, 25016, 25001, 25052, 25048, 25380,
			25383, 25386, 28755, 28693, 28702, 28721, 28733, 28745,
			28763, 28699, 28751, 28712, 28724, 28736, 28705, 28761,
			28757, 28708, 28777, 28780, 28783, 28753, 28759, 28715,
			28727, 28739, 28748, 28718, 28730, 28742, 28696, 25044,
			25050, 25087, 25031, 25019, 25004, 25034, 25022, 25007,
			25096,

			// === Decorative weapons & armour ===
			26809, 26811, 4069, 4504
		));
	}
}
