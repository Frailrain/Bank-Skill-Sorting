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

	static
	{
		Map<String, List<Integer>> m = new LinkedHashMap<>();

		// ---------------------------------------------------------------------
		// MELEE — Attack, Strength, Defence, Hitpoints.
		// Cross-tags: cooked food (combat eating), dragon/crystal/infernal axe
		// (spec weapons), slayer helm variants, cannon parts, combat potions.
		// ---------------------------------------------------------------------
		m.put(TAG_MELEE, Arrays.asList(
			// --- Scimitars bronze..dragon ---
			1321, 1323, 1325, 1327, 1329, 1331, 1333, 4587,
			// --- Longswords bronze..dragon ---
			1291, 1293, 1295, 1297, 1299, 1301, 1303, 1305,
			// --- 2h swords bronze..dragon ---
			1307, 1309, 1311, 1313, 1315, 1317, 1319, 7158,
			// --- Maces bronze..dragon ---
			1422, 1420, 1424, 1426, 1428, 1430, 1432, 1434,
			// --- Warhammers bronze..dragon ---
			1337, 1335, 1339, 1341, 1343, 1345, 1347, 13576,
			// --- Battleaxes bronze..dragon ---
			1375, 1363, 1365, 1361, 1367, 1369, 1371, 1377,
			// --- Daggers bronze..dragon + poisoned dragon dagger variants ---
			1205, 1203, 1207, 1211, 1209, 1213, 1215,
			1231, 5698, 5680,
			// --- Spears bronze..dragon + Zamorakian spear / hasta ---
			1237, 1239, 1241, 1243, 1245, 1247, 1249,
			11824, 11889,
			// --- Halberds bronze..dragon + crystal ---
			3190, 3192, 3194, 3196, 3198, 3200, 3202, 3204, 23987,
			// --- Claws bronze..dragon ---
			3095, 3096, 3097, 3098, 3099, 3100, 3101, 13652,
			// --- Godswords + hilts/shards ---
			11802, 11804, 11806, 11808,
			11818, 11820, 11814, 11816,
			11798, 11799, 11800, 11810,
			// --- Whips / tentacles ---
			4151, 12006, 23691,
			// --- Bulwark / granite / elder maul / inquisitor / scythe / rapier / saeldor / soulreaper ---
			21015, 4153, 21742, 21003, 24417, 22325, 22324, 23995, 27251,
			// --- Dragon dagger & defender-style offhand ---
			8844, 8845, 8846, 8847, 8848, 8849, 8850, 12954, 22322,
			// --- Rune armour ---
			1147, 1163, 1127, 1079, 1093, 1113, 1189, 1201,
			// --- Dragon armour ---
			1149, 11335, 21892, 3140, 4087, 4585, 1187,
			// --- Dragonfire shield / ward / wyvern shield ---
			11283, 11284, 22002, 22003, 22956,
			// --- Barrows melee sets: Dharok / Verac / Torag / Guthan ---
			4716, 4718, 4720, 4722,
			4753, 4755, 4757, 4759,
			4745, 4747, 4749, 4751,
			4724, 4726, 4728, 4730,
			// --- Bandos / Torva ---
			11832, 11834, 11836,
			26382, 26384, 26386,
			// --- Fighter torso / obby / berserker helm / neitiznot / warrior helm ---
			10551, 6568, 6522, 6523, 6524, 6525, 6526, 3749, 10828,
			// --- Amulets / necklaces ---
			1725, 1727, 1731, 1704, 1706, 1712, 6585, 11128, 19553, 24780,
			// --- Rings ---
			6737, 11773, 2572, 22975, 11090,
			// --- Capes ---
			6570, 21295, 6568, 9747, 9748, 9750, 9751, 9753, 9754, 9768, 9769, 22109, 21285,
			// --- Combat potions / boosts ---
			2436, 145, 147, 149,
			2440, 157, 159, 161,
			2442, 163, 165, 167,
			2428, 121, 123, 125,
			12695, 12697, 12699, 12701,
			23685, 23688, 23691, 23694,
			23709, 23712, 23715, 23718,
			11446, 189, 191, 193,
			6685, 6687, 6689, 6691,
			3024, 3026, 3028, 3030,
			20724,
			// --- Cooked combat food (duplicated from cooking — eaten in combat) ---
			315, 319, 325, 347, 333, 351, 329, 355, 339, 361, 379, 365, 373,
			7946, 385, 397, 391, 11936, 13441, 3144,
			// --- Dragon / crystal / infernal / 3rd age axe (spec weapons, dup WC) ---
			6739, 13241, 23673, 20011, 28199,
			// --- Cannon parts (dwarf multicannon — combat utility) ---
			6, 8, 10, 12, 2,
			// --- Slayer helm + variants (primary melee tasks; dup slayer) ---
			11864, 11865, 21888, 19639, 19643, 21264,
			24444, 24370, 25900, 26674, 28626,
			// --- Salve amulet / black mask (i) / slayer masks ---
			4081, 10588, 12017, 19710, 19711, 8901, 11774,
			// --- Spec / notable combat items ---
			24589, 27275, 25975,
			// --- Sailing cooked fish (dup sailing/cooking — high-heal combat food) ---
			32312, // cooked giant krill
			32328, // cooked yellowfin
			32336, // cooked halibut
			32344, // cooked bluefin
			32352  // cooked marlin
		));

		// ---------------------------------------------------------------------
		// RANGE — Ranged skill.
		// Cross-tags: chinchompas (hunter), cannon parts (dwarf cannon), slayer
		// helm range variants, dragonhide armour (crafting source), feathers.
		// ---------------------------------------------------------------------
		m.put(TAG_RANGE, Arrays.asList(
			// --- Bows ---
			841, 839, 843, 845, 847, 849, 851, 853, 855, 857, 859, 861,
			12788, 11235, 4212, 23983, 25865, 20997, 25862, 6724,
			// --- Crossbows ---
			837, 9174, 9176, 9177, 9179, 9181, 9183, 9185,
			8880, 21902, 21012, 11785, 26374,
			4734, 4732, 4736, 4738,
			// --- Blowpipes ---
			12924, 12926, 28688,
			// --- Arrows ---
			882, 884, 886, 888, 890, 892,
			11212, 21557, 78,
			// --- Bolts ---
			877, 9140, 9141, 9142, 9143, 9144, 21905,
			879, 9335, 880, 9336, 9337, 9338, 9339, 9340, 9341, 9342,
			21316, 13280, 13279, 4160,
			// --- Thrown (darts / knives) ---
			806, 807, 808, 3093, 809, 810, 811, 11230, 25849,
			864, 863, 865, 869, 866, 867, 868,
			// --- Javelins ---
			825, 826, 827, 828, 829, 830, 19484,
			// --- Ballistas ---
			19478, 19481,
			// --- Chinchompas (dup hunter) ---
			9977, 10033, 11959,
			// --- D'hide bodies / chaps / vambraces ---
			1135, 2499, 2501, 2503, 7372, 7376, 7378,
			1099, 2493, 2495, 2497, 7384, 7380, 7382,
			1065, 2487, 2489, 2491, 10366, 10364, 10362,
			// --- Coifs / studded ---
			1169, 1129, 1063, 1095, 1097,
			// --- Void range ---
			11664, 8839, 8840, 8842, 11663, 13072, 13073,
			// --- Armadyl / Masori / Crystal ---
			11826, 11828, 11830,
			27226, 27229, 27232, 27235, 27238, 27241,
			23971, 23975, 23979,
			// --- Ava's ---
			10498, 10499, 22109, 27544,
			// --- Jewellery ---
			19547, 6733, 11770, 2572,
			// --- Capes ---
			9756, 9757,
			// --- Ranging potions (dup herblore) ---
			2444, 169, 171, 173,
			23733, 23736, 23739, 23742,
			// --- Rada's blessing (diary ranged prayer bonus) ---
			22246, 22247, 22248, 22249,
			// --- Cannon parts (dwarf multicannon — primary ranged xp source on slayer) ---
			6, 8, 10, 12, 2,
			// --- Slayer helm variants (range tasks: kraken, wyverns) ---
			11864, 11865, 21888, 19639, 19643, 21264, 24444, 24370, 25900,
			// --- Dragonhides (raw, dup crafting — range gear source) ---
			1745, 1749, 1747,
			// --- Feathers (dup fletching/fishing) ---
			314,
			// --- Combat food (dup cooking — eaten while ranging) ---
			385, 391, 397, 7946, 13441, 3144
		));

		// ---------------------------------------------------------------------
		// MAGE — Magic skill.
		// Cross-tags: rune pouch (RC), runes (RC), slayer helm mage variants,
		// law runes (misc teleports), combat food.
		// ---------------------------------------------------------------------
		m.put(TAG_MAGE, Arrays.asList(
			// --- Basic runes ---
			556, 555, 557, 554, 558, 559, 562, 560, 563, 561, 564,
			565, 566, 21880, 9075,
			// --- Combination runes ---
			4694, 4695, 4696, 4697, 4698, 4699,
			// --- Essence ---
			1436, 7936,
			// --- Staves / battlestaves / mystic ---
			1379, 1381, 1383, 1385, 1387,
			1391, 1393, 1395, 1397, 1399,
			1401, 1403, 1405, 1407, 1409,
			// --- Combat / quest / boss staves ---
			3053, 6562, 11787, 11791, 11998, 11789,
			4675, 4170, 21256,
			12902, 12904, 22323,
			21006, 6914, 12422,
			24422, 24424, 24511,
			27275,
			12899, 12900, 22288,
			25731,
			// --- Wizard / mystic robes ---
			579, 577,
			4089, 4091, 4093, 4095, 4097,
			4099, 4101, 4103, 4105, 4107,
			4109, 4111, 4113, 4115, 4117,
			// --- Ahrim's / Infinity / Ancestral / Dagon'hai ---
			4708, 4710, 4712, 4714,
			6918, 6916, 6924, 6922, 6920,
			21018, 21021, 21024,
			24288, 24291, 24294,
			// --- Bark robes ---
			25024, 25026, 25030, 25033, 25035,
			25000, 25002, 25006, 25009, 25011,
			3385, 3387, 3389, 3391, 3393,
			// --- Jewellery ---
			12002, 11090, 1727, 2572,
			// --- Void mage ---
			11663, 8839, 8840, 8842,
			// --- Rune pouch / divine rune pouch (dup runecraft) ---
			12791, 27281,
			// --- Tomes ---
			20714, 20716, 25985, 25987,
			// --- Orbs ---
			567, 569, 571, 573, 575,
			// --- Magic / brews (dup herblore) ---
			3040, 3042, 3044, 3046,
			26340, 26342, 26344, 26346,
			25385, 25387, 25389, 25391,
			23727, 23730,
			20724,
			// --- Capes ---
			9762, 9763,
			// --- Teleport items (dup misc — primary mage context) ---
			6099, 13102, 13103, 23959,
			23916,
			// --- Slayer helm mage variants (dup slayer — kraken/smoke devils etc) ---
			11864, 11865, 21888, 19639, 19643, 21264, 24444, 24370, 25900,
			// --- Combat food (dup cooking — eaten during mage combat) ---
			385, 391, 397, 7946, 13441, 3144
		));

		// ---------------------------------------------------------------------
		// PRAYER — Prayer skill.
		// Cross-tags: prayer potions/super restores (dup herblore). Mostly stays
		// skill-pure (bones, altar items, holy books).
		// ---------------------------------------------------------------------
		m.put(TAG_PRAYER, Arrays.asList(
			// --- Bones ---
			526, 528, 3179, 530, 3183, 532, 3125, 4812, 534, 6812, 536,
			22378, 22124, 11943, 22774, 22780, 6729, 4834, 4850, 4852,
			25130, 22783,
			// --- Ashes ---
			20264, 20266, 20268,
			25766, 25768, 25770, 25772, 25774,
			// --- Ensouled heads ---
			13447, 13450, 13453, 13456, 13459, 13462, 13465, 13468, 13471, 13474,
			13477, 13480, 13483, 13486, 13489, 13492, 13495, 13498, 13501, 28323,
			13504, 13507, 13510,
			// --- Prayer potions ---
			2434, 139, 141, 143,
			// --- Super restores ---
			3024, 3026, 3028, 3030,
			// --- Sanfew ---
			10925, 10927, 10929, 10931,
			// --- Saradomin brew ---
			6685, 6687, 6689, 6691,
			// --- Altar utilities ---
			21643, 18337, 24187, 22111, 22374, 12601, 19710,
			// --- Holy symbols ---
			1716, 1718, 1720, 1722, 1724,
			// --- Holy / unholy books ---
			3840, 3842, 3844, 3846, 3839, 3843, 3845, 3847, 22405,
			// --- Prayer scrolls ---
			19621, 19622, 21047,
			// --- Proselyte set ---
			9666, 9668, 9670, 9672, 9674,
			// --- Monk robes ---
			542, 544,
			// --- Jewellery ---
			1704,
			// --- Vial of blood (chapel) ---
			22446,
			// --- Capes ---
			9759, 9760
		));

		// ---------------------------------------------------------------------
		// COOKING — Cooking skill.
		// Cross-tags: raw fish (dup fishing), tinderbox (lighting fires),
		// cooked meat/poultry ingredients.
		// ---------------------------------------------------------------------
		m.put(TAG_COOKING, Arrays.asList(
			// --- Cooked fish (primary home) ---
			315, 319, 325, 347, 333, 351, 329, 355, 339, 361, 379, 365, 373,
			7946, 385, 397, 391, 11936, 13441, 3144,
			// --- Raw fish (dup fishing — Cooking consumes them) ---
			317, 321, 327, 345, 335, 349, 331, 353, 341, 359, 377, 363, 371,
			7944, 383, 395, 389, 11934, 13439, 3142,
			// --- Burnt fish / misc ---
			323, 343, 357, 367, 375, 381, 387, 393, 7947,
			// --- Raw meat / poultry ---
			2132, 2138, 2136, 2134, 9986, 9978, 2880, 2881, 2884, 2885,
			// --- Cooked meat / poultry ---
			2142, 2140, 2878, 2883, 3228,
			// --- Veg / fruit ingredients ---
			1942, 1957, 1965, 1982, 1985, 1963, 1979, 1951,
			1550, 5986, 5982, 5980,
			2108, 2110, 2112, 2114,
			7054, 6703, 6701, 6705, 6707, 6709, 6711, 6713,
			// --- Baked potato variants ---
			7050, 7052, 7056, 7058, 7060, 7062, 7064,
			// --- Pies ---
			2321, 2325, 2327, 2323, 7168, 7170, 7178, 7188, 7208, 7218,
			7172, 7174, 7176, 7190, 7210, 7220,
			// --- Pizzas ---
			2287, 2289, 2293, 2297, 2301,
			// --- Breads ---
			2309, 2307, 1933, 1953, 1937,
			// --- Cakes ---
			1891, 1895, 1897, 1899, 1901, 1903,
			// --- Stews / curries ---
			2001, 2003, 2009, 2011,
			// --- Drinks ---
			1917, 1919, 1921, 1915, 1993, 1991, 1989, 1987,
			3326, 3328,
			// --- Eggs / dairy ---
			1944, 1927, 1925, 1923, 1931, 1935,
			// --- Gnome foods / sushi ---
			1971, 2150, 2152, 2154, 2156, 2158, 2160, 2162, 2164, 2166, 2168,
			2170, 2172, 2174, 2176, 2178, 2180, 2182, 2184, 9990, 9992,
			4293, 4297, 4299, 4627,
			// --- Outfits / gauntlets ---
			1949,
			24843, 24845, 24847, 24849,
			775,
			11140, 22777,
			// --- Capes ---
			9801, 9802,
			// --- Bowls / jugs / pots / sauces ---
			229, 227, 1929, 1973, 1975, 1977,
			// --- Karambwanji (bait / ingredient, dup fishing) ---
			3150, 3151,
			// --- Tinderbox (lighting fires to cook; dup firemaking) ---
			590,
			// --- Sailing cooked fish (dup sailing/fishing) — IDs from wiki ---
			32312, // cooked giant krill
			32328, // cooked yellowfin
			32336, // cooked halibut
			32344, // cooked bluefin
			32352, // cooked marlin
			// --- Sailing raw fish (dup sailing/fishing) — raw marlin confirmed; others verify ID ---
			32309, // raw giant krill (verify ID — inferred from cooked-3 pattern)
			32325, // raw yellowfin (verify ID — inferred)
			32333, // raw halibut (verify ID — inferred)
			32341, // raw bluefin (verify ID — inferred)
			32349  // raw marlin
		));

		// ---------------------------------------------------------------------
		// WC_FLETCHING — Woodcutting + Fletching.
		// Cross-tags: logs (dup firemaking), feathers (dup fishing/range),
		// axes (dup melee).
		// ---------------------------------------------------------------------
		m.put(TAG_WC_FLETCHING, Arrays.asList(
			// --- Logs ---
			1511, 2862, 1521, 1519, 6333, 1517, 6332, 10810, 1515, 1513, 19669,
			// --- Axes bronze..dragon + gilded/infernal/crystal/3rd age ---
			1351, 1349, 1353, 1361, 1355, 1357, 1359,
			6739, 23185, 13241, 23673, 20011, 28199,
			// --- Knife ---
			946,
			// --- Bow strings + flax ---
			1777, 1779,
			// --- Unstrung shortbows / longbows ---
			50, 54, 58, 62, 66, 70,
			48, 52, 56, 60, 64, 68,
			// --- Crossbow stocks / limbs / unstrung ---
			9440, 9442, 9444, 9446, 9448, 9450, 9452, 9454, 9456,
			9420, 9422, 9423, 9425, 9427, 9429, 9431, 21964,
			9174, 9455, 9457, 9459, 9461, 9463, 9465,
			// --- Arrow shafts / headless / unfinished bolts ---
			52, 53,
			9375, 9377, 9378, 9379, 9380, 9381, 9382, 21930,
			// --- Dart tips ---
			819, 820, 821, 3094, 822, 823, 824, 11232, 25853,
			// --- Javelin heads / shafts ---
			19581,
			// --- Feathers (dup fishing; primary fletching use) ---
			314,
			// --- Bird's nests / seeds ---
			5070, 5071, 5072, 5073, 5074, 5075,
			// --- Forestry items ---
			28164, 28166, 28168, 28170, 28172, 28174,
			28186, 28188, 28190, 28192, 28194, 28196, 28198,
			28200, 28202, 28204, 28206, 28208, 28210, 28212,
			28279, 28280, 28281, 28282, 28283, 28284, 28285,
			28214, 28216, 28218, 28220, 28222,
			// --- Imcando hatchet ---
			28420,
			// --- Capes ---
			9774, 9775, 9783, 9784,
			// --- Pet (beaver) ---
			13322
		));

		// ---------------------------------------------------------------------
		// FISHING — Fishing skill.
		// Cross-tags: raw fish (dup cooking), feathers (dup fletching).
		// ---------------------------------------------------------------------
		m.put(TAG_FISHING, Arrays.asList(
			// --- Raw fish ---
			317, 321, 327, 345, 335, 349, 331, 353, 341, 359, 377, 363, 371,
			7944, 383, 395, 389, 11934, 13439, 3142,
			// --- Raw karambwanji (bait) ---
			3150,
			// --- Fishing tools ---
			303, 305, 307, 309, 311, 10129, 21028, 23612, 23677, 301, 1585,
			// --- Karambwan vessels ---
			3157, 3159,
			// --- Fish barrel ---
			25580,
			// --- Bait ---
			313, 6205, 11323,
			// --- Feathers (dup fletching) ---
			314, 10087,
			// --- Angler outfit ---
			13258, 13259, 13260, 13261,
			// --- Spirit angler (Aerial Fishing) ---
			25596, 25598, 25600, 25602,
			// --- Molch pearl / chunks ---
			23526, 23528, 23534, 23537, 23539,
			// --- Sacred / infernal eel ---
			21819, 13321,
			// --- Pearl fishing rods ---
			22846, 22842, 22844,
			// --- Capes ---
			9798, 9799,
			// --- Pet (Heron) ---
			13320,
			// --- Tempoross / rewards ---
			25604, 25617, 25611,
			// --- Spade (dup farming — mort myre swamp fishing uses it) ---
			952,
			// --- Sailing raw fish (dup sailing/cooking) ---
			32309, // raw giant krill (verify ID)
			32325, // raw yellowfin (verify ID)
			32333, // raw halibut (verify ID)
			32341, // raw bluefin (verify ID)
			32349  // raw marlin
		));

		// ---------------------------------------------------------------------
		// FIREMAKING — Firemaking skill.
		// Cross-tags: all logs (dup WC), tinderbox (dup cooking).
		// ---------------------------------------------------------------------
		m.put(TAG_FIREMAKING, Arrays.asList(
			// --- Core tool ---
			590,
			// --- Logs (dup WC — training via Wintertodt/cape trick) ---
			1511, 2862, 1521, 1519, 6333, 1517, 6332, 10810, 1515, 1513, 19669,
			// --- Firelighters ---
			7329, 7330, 7331, 10326, 10327, 10328,
			// --- Pyromancer outfit (Wintertodt) ---
			20706, 20708, 20710, 20712,
			// --- Warm clothing / bruma kindling ---
			20718, 20720,
			1831, 1833, 1835,
			20695, 20697, 20699, 20701, 20703,
			// --- Lanterns / torches / candles ---
			4524, 4525, 4529, 4548, 4544, 24361, 24363,
			36, 33,
			4530, 4539,
			// --- Sacred oil / pyre logs ---
			3432, 3433, 3434, 3435,
			3438, 3440, 3442, 3444, 6211, 6213, 6215, 6217, 19671,
			// --- Brittle key / phoenix egg ---
			25742, 20693,
			// --- Capes ---
			9804, 9805,
			// --- Incense burner utilities ---
			8766
		));

		// ---------------------------------------------------------------------
		// CRAFTING — Crafting skill.
		// Cross-tags: saw (dup construction), planks (dup construction),
		// chisel (dup mining_smithing, runecraft), all bars (dup mining_smithing,
		// construction), dragonhides (dup range), thread, needle.
		// ---------------------------------------------------------------------
		m.put(TAG_CRAFTING, Arrays.asList(
			// --- Uncut gems ---
			1625, 1621, 1619, 1617, 1631, 6571, 19496, 1623,
			// --- Cut gems ---
			1607, 1605, 1603, 1601, 1615, 6573, 19501, 1609,
			// --- Semi-precious ---
			1627, 1629,
			// --- Zenyte / fossil / tiny placeholders ---
			21043, 21041, 21039, 21046,
			// --- Moulds ---
			1592, 1595, 1597, 11065, 5523, 1594, 11068,
			// --- Needle / thread ---
			1733, 1734,
			// --- Leathers ---
			1739, 1741, 1743,
			6287, 6289,
			7801,
			1131,
			// --- Dragonhides (raw; dup range) ---
			1745, 1749, 1747, 2505, 2507, 2509,
			// --- Dragonhides (tanned) ---
			1753, 1751, 1755, 2503, 2501, 2499,
			// --- Glass pipeline ---
			1781, 401, 21504, 1783, 1775,
			1785,
			// --- Pottery ---
			1761, 1787, 1789, 1791, 1793, 1795, 1797, 1799,
			// --- Battlestaff + orbs (for Crafting orbs) ---
			1391, 567, 569, 571, 573, 575,
			// --- Amulet stringing intermediates ---
			1673, 1675, 1677, 1679, 1681, 1683, 1714,
			// --- Urns ---
			20354, 20356, 20358, 20360, 20362, 20364,
			20384, 20386, 20388, 20390, 20392,
			// --- Wool / yarn / symbols ---
			1737, 1759, 1763, 1765,
			// --- Bolts of cloth (dup construction) ---
			8790,
			// --- Chisel (dup mining_smithing, runecraft) ---
			1755,
			// --- Silver bars (for jewellery) ---
			2355,
			// --- Capes ---
			9780, 9781,
			// --- Holy mould / unholy symbol ---
			5525, 1716, 1722,
			// --- Ring of wealth stringing ---
			11980,
			1718, 1724,
			// --- Basket / sack empty ---
			5350, 5418,
			// --- Saw (dup construction) ---
			8794, 9625,
			// --- Planks (dup construction) ---
			960, 8778, 8780, 8782,
			// --- All bars (dup mining_smithing — jewellery bars) ---
			2349, 2351, 2353, 2357, 2359, 2361, 2363,
			// --- Dragon leather-made range armour (stored where crafted) ---
			1135, 2499, 2501, 2503,
			// --- Molten glass outputs (crafted here) ---
			1469
		));

		// ---------------------------------------------------------------------
		// MINING_SMITHING — Mining + Smithing.
		// Cross-tags: chisel (gem cutting), hammer (dup construction), coal bag,
		// gem bag, all bars stay primary here.
		// ---------------------------------------------------------------------
		m.put(TAG_MINING_SMITHING, Arrays.asList(
			// --- Ores ---
			436, 438, 440, 442, 453, 444, 447, 449, 451,
			21347, 668, 11941, 21343,
			24475, 24489, 12011,
			3211, 6971, 6979, 6981,
			1761, 434,
			// --- Bars ---
			2349, 2351, 2355, 2353, 2357, 2359, 2361, 2363,
			9467, 11942,
			// --- Pickaxes ---
			1265, 1267, 1269, 12297, 1273, 1271, 1275,
			23276, 11920, 12797, 23680, 13243, 20014, 25376,
			// --- Hammer (dup construction) + imcando hammer ---
			2347, 25644,
			// --- Chisel (dup crafting, runecraft — gem cutting) ---
			1755,
			// --- Prospector kit ---
			12013, 12014, 12015, 12016,
			// --- Smiths' uniform (Giants' Foundry) ---
			27024, 27026, 27028, 27030,
			// --- Coal bag / gem bag / ore pack ---
			12019, 25627, 27019, 27020,
			// --- Mining gloves ---
			21345, 21539,
			// --- Varrock armour 1..4 ---
			10386, 10387, 10388, 10389, 10390,
			// --- Dragon mining helm ---
			21390,
			// --- Capes + pet ---
			9792, 9793, 9795, 9796,
			13321,
			// --- Motherlode / nuggets ---
			12012,
			// --- Dwarven stout ---
			1913, 5751,
			// --- Unfinished bolts (smithing training) ---
			9375, 9377, 9378, 9379, 9380, 9381, 9382,
			// --- Giants' Foundry moulds / outputs ---
			27010, 27012, 27014, 27016, 27018,
			27021, 27023,
			// --- Ring of forging ---
			2568
		));

		// ---------------------------------------------------------------------
		// HERBLORE — Herblore skill (owns all finished potions).
		// Cross-tags: herb sack (dup farming), compost potions (dup farming),
		// combat potions (dup melee/range/mage), plant cure (dup farming),
		// bottomless compost bucket (dup farming).
		// ---------------------------------------------------------------------
		m.put(TAG_HERBLORE, Arrays.asList(
			// --- Grimy herbs ---
			199, 201, 203, 205, 207, 3049, 209, 211, 213, 3051, 215, 2485, 217, 219,
			// --- Clean herbs ---
			249, 251, 253, 255, 257, 2998, 259, 261, 263, 3000, 265, 2481, 267, 269,
			// --- Secondaries ---
			221, 231, 235, 237, 239, 241, 243, 225, 223, 245, 247,
			1975, 3391, 2970, 6693, 12934, 6018,
			9736, 9735, 27390, 22446, 27003,
			// --- Vials ---
			227, 229, 25477,
			// --- Pestle / herb sack / amulet of chemistry ---
			233, 13226, 19558, 21163,
			// --- Unfinished potions (1 per herb) ---
			91, 93, 95, 97, 99, 101, 103, 105, 107, 109, 111, 3002, 3004, 2483,
			// --- 4-dose finished potions (classic anchors) ---
			2428, 2430, 2432, 2434, 2436, 2438, 2440, 2442, 2444, 2446, 2448, 2450, 2452,
			// --- Attack potion doses 3..1 ---
			121, 123, 125,
			// --- Antipoison doses 3..1 ---
			175, 177, 179,
			// --- Strength potion doses 3..1 ---
			113, 115, 117,
			// --- Restore potion doses 3..1 ---
			127, 129, 131,
			// --- Energy potion doses (4..1) ---
			3008, 3010, 3012, 3014,
			// --- Defence potion doses 3..1 ---
			133, 135, 137,
			// --- Prayer potion doses 3..1 ---
			139, 141, 143,
			// --- Super attack 3..1 ---
			145, 147, 149,
			// --- Fishing potion 3..1 ---
			151, 153, 155,
			// --- Super strength 3..1 ---
			157, 159, 161,
			// --- Super defence 3..1 ---
			163, 165, 167,
			// --- Ranging potion 3..1 ---
			169, 171, 173,
			// --- Antipoison + / super / antidote ---
			181, 183, 185, 187,
			// --- Zamorak brew 3..1 ---
			189, 191, 193,
			// --- Super restore / sanfew / saradomin brew ---
			3026, 3028, 3030, 10927, 10929, 10931, 6687, 6689, 6691,
			// --- Super combat / divine super combat ---
			12697, 12699, 12701, 23688, 23691, 23694,
			// --- Stamina (4..1) ---
			12623, 12625, 12627, 12629, 12631,
			// --- Super energy (4..1) ---
			3016, 3018, 3020, 3022,
			// --- Ancient / forgotten brew ---
			26342, 26344, 26346, 25387, 25389, 25391,
			// --- Divine potions (full family) ---
			23709, 23712, 23715, 23718,
			23727, 23730, 23733, 23736, 23739, 23742,
			23673, 23676, 23679, 23682,
			23743, 23746, 23749, 23752,
			23755, 23758, 23761, 23764,
			23767, 23770, 23773, 23776,
			// --- Anti-venom / Anti-venom+ ---
			12905, 12907, 12909, 12911, 12913, 12915, 12917, 12919,
			// --- Guthix rest / combat potion / compost potion (dup farming) ---
			4417, 4419, 4421, 4423,
			9739, 9741, 9743, 9745,
			6470, 6472, 6474, 6476,
			// --- Weapon poison variants ---
			5937, 5939, 5940,
			// --- Capes ---
			9774, 9775,
			// --- Pet (Herbi) ---
			19730,
			// --- Plant cure (dup farming) ---
			6036,
			// --- Bottomless compost bucket (dup farming) ---
			22997
		));

		// ---------------------------------------------------------------------
		// AGILITY_THIEVING — Agility + Thieving.
		// Cross-tags: stamina potions (dup herblore), super energy (dup herblore).
		// ---------------------------------------------------------------------
		m.put(TAG_AGILITY_THIEVING, Arrays.asList(
			// --- Graceful (standard + recolours) ---
			11850, 11852, 11854, 11856, 11858, 11860,
			25069, 25071, 25073, 25075, 25077, 25079,
			25081, 25083, 25085, 25087, 25089, 25091,
			25093, 25095, 25097, 25099, 25101, 25103,
			25105, 25107, 25109, 25111, 25113, 25115,
			25117, 25119, 25121, 25123, 25125, 25127,
			27019, 27021, 27023, 27025, 27027, 27029,
			27433, 27435, 27437, 27439, 27441, 27443,
			24375, 24377, 24379, 24381, 24383, 24385,
			24731, 24733, 24735, 24737, 24739, 24741,
			// --- Marks of grace ---
			11849,
			// --- Agility arena ticket / squirrel pet ---
			2996, 20659,
			// --- Rogue equipment ---
			5553, 5554, 5555, 5556, 5557,
			// --- Dodgy necklace / coin pouch / lockpick ---
			21143, 22521, 25448, 1523,
			// --- Blackjacks ---
			6404, 6406, 6408, 6410, 6412, 6414,
			// --- Pyramid / pharaoh's sceptre ---
			9044, 9050, 9046, 9048,
			// --- Hallowed Sepulchre ---
			24709, 24711, 24713, 24719,
			// --- Master scroll book ---
			21387,
			// --- Ardougne cloak ---
			13121, 13122, 13123, 22257,
			// --- Ring of wealth ---
			2572,
			// --- Stamina potions 4..1 (dup herblore — primary agility use) ---
			12623, 12625, 12627, 12629, 12631,
			// --- Super energy 4..1 (dup herblore) ---
			3016, 3018, 3020, 3022,
			// --- Capes ---
			9771, 9772, 9777, 9778,
			// --- Pets: squirrel / rocky ---
			20659, 20663,
			// --- Dorgeshuun thieving weapons ---
			8872, 8880,
			// --- Eye patch ---
			3672
		));

		// ---------------------------------------------------------------------
		// SLAYER — Slayer skill.
		// Cross-tags: slayer helm (dup melee/range/mage), black mask, cannon
		// parts (dup melee/range — multicannon is core slayer tool),
		// enchanted gem (dup crafting).
		// ---------------------------------------------------------------------
		m.put(TAG_SLAYER, Arrays.asList(
			// --- Task-required gear ---
			4168, 4166, 4551, 4162, 8901,
			6171, 6106, 11736, 4156, 4161, 6660, 6663,
			// --- Slayer's staff / dust battlestaff ---
			4170, 21256, 25731,
			// --- Black mask + (i) ---
			8901, 11774,
			// --- Slayer helmet + recolours / (i) variants ---
			11864, 11865, 21888, 19639, 19643, 21264,
			24444, 24370,
			25900, 26674, 28626,
			// --- Enchanted gem (dup crafting) ---
			4155, 21268,
			// --- Slayer rings ---
			11866, 11867, 11868, 11869, 11870, 11871, 11872, 11873, 11874,
			21268,
			// --- Bonecrusher + necklace ---
			18337, 24187, 25731,
			// --- Brimstone / mysterious emblem / larran's ---
			22461, 22345, 23490, 23491,
			// --- Trident / blowpipe (slayer primary use) ---
			12899, 12924, 12926,
			// --- Imbue scrolls ---
			11135, 21047,
			// --- Ring of suffering / imbued ---
			19550, 19710,
			// --- Smouldering stone / bulwark imbue ---
			13233, 21015,
			// --- Salve amulet (e/ei) ---
			4081, 10588, 19710, 19712,
			// --- Fire / infernal cape (Jad/Inferno slayer) ---
			6570, 21295, 21285,
			// --- Dragon boots / granite boots ---
			11732, 10884,
			// --- Capes ---
			9786, 9787,
			// --- Crystal key halves ---
			985, 987, 989,
			// --- Amulet of forsaken / fury ornament ---
			12436,
			// --- Blood essence ---
			24495, 24498, 24502,
			// --- Slayer task drops core ---
			4151, 12004, 22006, 22104, 12922, 12927,
			// --- Slayer unlock scrolls ---
			21270, 21271, 21272, 21273,
			// --- Konar drops ---
			24551, 24553,
			// --- Cannon parts (dwarf multicannon — core slayer tool) ---
			6, 8, 10, 12, 2
		));

		// ---------------------------------------------------------------------
		// FARMING — Farming skill.
		// Cross-tags: graceful (run energy for farming runs), herb sack (harvest
		// storage), compost potions (dup herblore), plant cure (dup herblore),
		// bottomless compost bucket, spade.
		// ---------------------------------------------------------------------
		m.put(TAG_FARMING, Arrays.asList(
			// --- Allotment seeds ---
			5318, 5319, 5324, 5322, 5320, 5323, 5321,
			// --- Flower seeds ---
			5096, 5097, 5098, 5099, 5100,
			// --- Herb seeds ---
			5291, 5292, 5293, 5294, 5295, 5296, 5297, 5298,
			5299, 5300, 5301, 5302, 5303, 5304,
			// --- Hop seeds ---
			5305, 5306, 5307, 5308, 5309, 5310, 5311,
			// --- Bush seeds ---
			5101, 5102, 5103, 5104, 5105, 5106,
			// --- Tree seeds ---
			5312, 5313, 5314, 5315, 5316,
			// --- Fruit tree seeds ---
			5283, 5284, 5285, 5286, 5287, 5288, 5289, 5290,
			// --- Special seeds ---
			22869, 22877, 22879, 22881, 5317,
			14589, 5280, 5281, 5282,
			21486, 21488, 22871, 22873,
			24856, 24858, 24860,
			// --- Tools ---
			952, 5341, 5343,
			5331, 5333, 5334, 5335, 5336, 5337, 5338, 5339, 5340,
			22586, 7409, 13138, 5350, 5418, 5955,
			// --- Compost ---
			6032, 6034, 21483, 22875, 6036,
			// --- Bottomless compost bucket (dup herblore) ---
			22997,
			// --- Compost potions 4..1 (dup herblore) ---
			6470, 6472, 6474, 6476,
			// --- Farmer's outfits ---
			13640, 13642, 13644, 13646,
			13647, 13648, 13649, 13650,
			// --- Amulet of nature ---
			6040,
			// --- Harvest produce ---
			1942, 1957, 1965, 1982, 1985, 5986, 5504, 5982,
			// --- Capes ---
			9810, 9811,
			// --- Pet (Tangleroot) ---
			20661,
			// --- Bologa's blessing / hespori root ---
			22123, 22118, 22116,
			// --- Leaves / saplings / weeds ---
			1963, 5972, 5978, 5984,
			5370, 5371, 5372, 5373, 5374, 5375, 5376, 5377,
			6055,
			// --- Attas / iasor / kronos seeds ---
			24840, 24842,
			// --- Herb sack (dup herblore — harvesting herbs) ---
			13226,
			// --- Graceful (dup agility — farm-run energy) ---
			11850, 11852, 11854, 11856, 11858, 11860,
			// --- Stamina potions (dup herblore — farm runs) ---
			12623, 12625, 12627, 12629, 12631
		));

		// ---------------------------------------------------------------------
		// RUNECRAFT — Runecraft skill.
		// Cross-tags: runes (dup mage), rune pouch (dup mage), chisel (tiara
		// making, dup crafting/mining_smithing).
		// ---------------------------------------------------------------------
		m.put(TAG_RUNECRAFT, Arrays.asList(
			// --- Essence ---
			1436, 7936, 24493,
			// --- All runes (dup mage) ---
			556, 555, 557, 554, 558, 559, 562, 560, 563, 561, 564, 565, 566, 21880, 9075,
			// --- Combination runes ---
			4694, 4695, 4696, 4697, 4698, 4699,
			// --- Talismans ---
			1438, 1440, 1442, 1444, 1446, 1448, 1450, 1452, 1454, 1456, 1458, 1460, 1462, 9084, 21538,
			// --- Combination talismans ---
			5516, 5517, 5518, 5519, 5520,
			// --- Tiaras ---
			5527, 5529, 5531, 5533, 5535, 5537, 5539, 5541, 5543, 5545, 5547,
			// --- RC pouches ---
			5509, 5510, 5512, 5514, 26784,
			// --- Rune pouch / divine rune pouch (dup mage) ---
			12791, 27281,
			// --- Wicked hood / cape ---
			22332, 22340,
			// --- Raiments of the eye ---
			27622, 27624, 27626, 27628,
			27616, 27618, 27620,
			27610, 27612, 27614,
			// --- Binding necklace ---
			5521,
			// --- Abyssal bracelet ---
			11095, 11097, 11099, 11101, 11103,
			// --- Chisel (tiara making; dup crafting/mining_smithing) ---
			1755,
			// --- Tiara / talisman combo mould ---
			5525,
			// --- Ourania teleport tabs ---
			19627,
			// --- Dark essence fragments (Arceuus RC) ---
			24543,
			// --- Abyss teleport ---
			5528,
			// --- Capes ---
			9765, 9766,
			// --- Pet (Rift guardian) ---
			20665
		));

		// ---------------------------------------------------------------------
		// HUNTER — Hunter skill.
		// Cross-tags: chinchompas (dup range), impling jars (dup misc).
		// ---------------------------------------------------------------------
		m.put(TAG_HUNTER, Arrays.asList(
			// --- Traps ---
			10006, 10008, 10010, 10012, 10020, 10150, 10024,
			// --- Impling jars ---
			11238, 11240, 11242, 11244, 11246, 11248, 11250, 11252, 11254, 11256, 11258, 23783, 25821,
			// --- Raw hunter foods (bait) ---
			9978, 9986, 9988, 10109, 10111, 10113,
			// --- Chinchompas (dup range) ---
			9977, 10033, 11959,
			// --- Salamanders + skins ---
			10146, 10147, 10148, 10149,
			// --- Camo outfits (polar/woodland/desert/jungle) ---
			10068, 10069, 10066, 10067, 10056, 10057, 10054, 10055, 10042, 10043,
			// --- Larupia / Kyatt / Graahk ---
			10045, 10047,
			10049, 10051, 10053,
			10059, 10061, 10063,
			// --- Spotted / spottier cape ---
			10073, 10071,
			// --- Falconer glove ---
			10023,
			// --- Tracking skins / furs ---
			10099, 10103, 10107, 10115, 10117, 10119, 10121, 10123, 10125, 10127,
			// --- Feathers variants ---
			10087, 10088, 10089, 10090, 10091, 10092, 10093,
			// --- Tars ---
			10142, 10143, 10144,
			// --- Bird's nests ---
			5070, 5071, 5072, 5073, 5074, 5075,
			// --- Herbiboar / numulite ---
			21306, 21339, 21344, 21337, 21338,
			// --- Capes ---
			9813, 9814,
			// --- Baby chinchompas (pets) ---
			13324, 13325, 13326, 13327,
			// --- Varlamore hunter (quetzals, etc) ---
			28247, 28249, 28251, 28253,
			// --- Vial of blood (Varlamore sanguine) ---
			22446
		));

		// ---------------------------------------------------------------------
		// CONSTRUCTION — Construction skill.
		// Cross-tags: planks (dup crafting), saw (dup crafting), hammer (dup
		// mining_smithing), bars (dup mining_smithing, crafting),
		// bolts of cloth (dup crafting), soft clay (dup mining_smithing).
		// ---------------------------------------------------------------------
		m.put(TAG_CONSTRUCTION, Arrays.asList(
			// --- Planks (dup crafting) ---
			960, 8778, 8780, 8782,
			// --- Nails bronze..rune ---
			4819, 4820, 4822, 4824, 4826, 4828,
			// --- Saw + crystal saw (dup crafting) ---
			8794, 9625,
			// --- Hammer (dup mining_smithing) + imcando ---
			2347, 25644,
			// --- Bolts of cloth (dup crafting) ---
			8790,
			// --- Limestone + brick ---
			3211, 3420,
			// --- Soft clay / molten glass / bars ---
			1761, 1775,
			2349, 2351, 2353, 2355, 2357, 2359, 2361, 2363,
			// --- Gold leaf / marble / magic stone ---
			8784, 8786, 8788,
			// --- House teleport tab ---
			8013,
			// --- Portals / clockwork / spring cleaner ---
			8791, 25614,
			23321, 23322,
			// --- Servant's moneybag / tea / wine ---
			7396, 7730, 4626, 4627,
			// --- POH teleport scroll ---
			21388,
			// --- Rumours / contracts ---
			8792,
			// --- Costume storage / armour case ---
			11815,
			// --- Capes ---
			9789, 9790,
			// --- Mahogany Homes (packs / plates / clockwork / rewards) ---
			29155, 29156, 29157, 29158,
			29159, 29160, 29161,
			// --- Gems for mounted trophies ---
			1601, 1603, 1605, 1607, 1615,
			// --- Plank sack (dup misc) ---
			24882
		));

		// ---------------------------------------------------------------------
		// MISC — long-term banked utility items.
		// Keys, teleports (jewellery / tablets / items), clue scrolls + tools,
		// impling jars, storage bags, chronicle, fairy ring utils, misc jugs,
		// strange/golden rocks, seed pods.
		// ---------------------------------------------------------------------
		m.put(TAG_MISC, Arrays.asList(
			// --- Keys ---
			987, 989, 991, 22374, 22461, 23490, 23491, 11942,
			29463, // ancient key (placeholder)
			25742, // brittle key (dup firemaking)
			4670,  // mossy key placeholder
			// --- Amulet of glory (all doses + t) ---
			1704, 1706, 1708, 1710, 1712,
			// --- Ring of wealth + imbued ---
			2572, 11980, 11982, 11984, 11986, 11988, 20786,
			// --- Games necklace 8..0 ---
			3853, 3855, 3857, 3859, 3861, 3863, 3865, 3867,
			// --- Skills necklace 1..5 ---
			11105, 11107, 11109, 11111, 11113,
			// --- Combat bracelet 1..5 ---
			11118, 11120, 11122, 11124, 11126,
			// --- Necklace of passage ---
			21146, 21149, 21151, 21153, 21155,
			// --- Burning amulet ---
			21166, 21168, 21170, 21172, 21174,
			// --- Slayer rings (dup slayer) ---
			11866, 11867, 11868, 11869, 11870, 11871, 11872, 11873, 11874,
			// --- Digsite pendant 1..5 ---
			11190, 11191, 11192, 11193, 11194,
			// --- Xeric's talisman ---
			13393,
			// --- Teleport tabs (Varrock..POH) ---
			8007, 8008, 8009, 8010, 8011, 8012, 8013,
			// --- Law / soul / wrath / blood runes (dup mage/runecraft for tele) ---
			563, 565, 21880, 566,
			// --- Clue scrolls (starters for each tier) ---
			23182, 2677, 2801, 2722, 19043, 19835,
			// --- Clue utility items ---
			2574, 2575, 2576,
			// --- Impling jars (dup hunter) ---
			11238, 11240, 11242, 11244, 11246, 11248, 11250, 11252, 11254, 11256, 11258, 23783, 25821,
			// --- Storage bags ---
			11941, // looting bag
			12019, 25627, 13226, 25580,
			24882, // plank sack
			// --- Strange / golden rocks (museum display) ---
			20372, 20374, 20376, 20378, 20380,
			// --- Royal seed pod / seed pod ---
			9469, 19564,
			// --- Chronicle ---
			11194,
			// --- Eagle / essence pouch ---
			// --- Fairy ring items (utility) ---
			772, 9084, 11194,
			// --- Jugs / bowls (common banked misc) ---
			1935, 1937, 1923, 1925,
			// --- Antique lamp / tome of experience (common quest/diary rewards) ---
			4447,
			// --- Ardougne / Kandarin / Kourend / Fremennik / Desert / Varrock / Wilderness diary teleports ---
			21395, 21396, 21397, 21398,
			// --- Fire cape / infernal cape duplicates intentionally NOT here (stay in melee) ---
			// --- Barrows teleport / ferox teleport tabs ---
			21385, 21383,
			// --- Grand Exchange tab ---
			25541
		));

		// ---------------------------------------------------------------------
		// QUESTS — quest & minigame & diary items kept long-term in-bank.
		// Includes: quest point cape, notable quest-locked gear, diary rewards,
		// boss uniques/cosmetics, minigame gear that doesn't fit skill tabs.
		// ---------------------------------------------------------------------
		m.put(TAG_QUESTS, Arrays.asList(
			// --- Quest / achievement / max / completionist capes ---
			9813, // quest point cape (NOTE: may overlap with a hunter cape id)
			13069, 13070, // achievement diary cape (t)
			13280, 13281, // max cape (t)
			13342, 13343, // music cape (t)
			// --- Fire cape / infernal cape (dup melee — notable quest/miniquest rewards) ---
			6570, 21295, 21285,
			// --- Dramen / lunar / ivandis / blisterwood staves (quest-locked) ---
			772, 9084, 4170, 24443,
			// --- Ectophial ---
			4251,
			// --- Ring of charos / charos(a) ---
			4202, 5525,
			// --- Diary rewards ---
			13103, // ardougne cloak 4 already covered; kandarin headgear
			11136, 11138, 11140, 11142, // kandarin headgear 1..4
			13560, 13561, 13562, 13563, // explorer's ring 1..4
			12018, 12019, 12020, 12021, // karamja gloves 1..4
			22246, 22247, 22248, 22249, // rada's blessing 1..4 (dup range)
			13070, // fremennik sea boots placeholder
			19476, 19477, 19478, 19479, // desert amulet / morytania legs placeholder
			22151, 22152, 22153, 22154, // wilderness sword placeholder
			// --- Void knight equipment ---
			8839, 8840, 8842, 11663, 11664, 11665, 13072, 13073,
			// --- Fighter torso (Barbarian Assault) ---
			10551,
			// --- Defenders (Warriors' Guild) ---
			8844, 8845, 8846, 8847, 8848, 8849, 8850, 12954, 22322,
			// --- Boss uniques / pets (kept long-term) ---
			25527, // unsired
			20851, // olmlet
			13178, // KBD heads mount (lol) placeholder
			12655, // zamorakian spear remnant
			20655, // general graardor pet? (check)
			// --- Raids dust / twisted rewards ---
			24670, 24671, 24672,
			// --- Recipe for Disaster gauntlets (cooking) already cooking tab, skip ---
			// --- Monkey greegrees (kept post-RFD) ---
			4024, 4025, 4026, 4027, 4028, 4029,
			// --- Barrows teleport scrolls / quest consumables ---
			22399,
			// --- Light sources (quest items kept for Pyramid Plunder etc) ---
			4539, 4540, 4541,
			// --- Barrows items (tarnished placeholder) ---
			4856, 4858, 4860, 4862,
			// --- Shantay pass / explorer's ring (quest starters) ---
			1854,
			// --- Camulet / amulet of the damned / necklace of binding ---
			6704,
			// --- Ghostspeak amulet / ring of visibility / ring of stone ---
			552, 2572, 6583, 6737,
			// --- Nature rune stash / clue nail (completionist-adjacent) ---
			// --- Lunar / moon clan gear (quest locked) ---
			9105, 9106, 9107, 9108, 9110, 9112, 9114, 9117,
			// --- Ava's attractor / accumulator already in range ---
			// --- Master clue puzzle items ---
			19820, 19825, 19830,
			// --- Collection log (virtual; no ID) — skip ---
			// --- Rocky / beaver / squirrel pets already in skill tabs ---
			// --- Quest point cape trim / hood ---
			9814,
			// --- Legends cape / task master cape ---
			1052, 1053
		));

		// ---------------------------------------------------------------------
		// SAILING — Sailing skill (released 2025-11-19).
		// Many Sailing items lack stable IDs in net.runelite.api.ItemID at the
		// time of writing; flagged entries inferred from the wiki and adjacent
		// confirmed IDs in the 31000-32400 release range. Cross-tags for fish
		// live in cooking/fishing/melee buckets above.
		// ---------------------------------------------------------------------
		m.put(TAG_SAILING, Arrays.asList(
			// --- Skill cape + hood ---
			31288, // sailing cape
			31290, // sailing cape(t)
			// 31289, // sailing hood (verify ID — cape+1 pattern not confirmed)
			// --- Navigation tools (quest-obtained) ---
			31803, // spyglass
			31807, // crowbar (Sailing crowbar — distinct from vanilla 954)
			31985, // captain's log
			// 31986, // current duck (verify ID — adjacent to captain's log)
			// --- Repair / maintenance ---
			31964, // repair kit
			// --- Sailing raw fish (dup fishing/cooking) ---
			32309, // raw giant krill (verify ID — inferred from cooked-3 pattern)
			32325, // raw yellowfin (verify ID)
			32333, // raw halibut (verify ID)
			32341, // raw bluefin (verify ID)
			32349, // raw marlin
			// --- Sailing cooked fish (dup cooking/melee) ---
			32312, // cooked giant krill
			32328, // cooked yellowfin
			32336, // cooked halibut
			32344, // cooked bluefin
			32352  // cooked marlin

			// === ITEMS BELOW LACK CONFIRMED IDS — FLAGGED FOR FUTURE PASS ===
			// Salvaging hooks (bronze/iron/steel/mithril/adamant/rune/dragon)
			// Trawling nets (rope/cotton/hemp/linen)
			// Fish crates (empty + filled)
			// Ship components: hull, helm, mast+sails, keel (raft/skiff/sloop tiers)
			// Sailing hardwood logs + planks (would dup wc_fletching/firemaking +
			//   construction/crafting once IDs land)
			// Sailing cannons + Sailing-specific cannonballs (would dup range/slayer)
			// Sailing outfit pieces
			// Pet "Soup"
			// Fish offcuts (plain + fine — dup herblore/farming as secondaries)
			// Port cargo crates (platebodies/spices/logs/seeds/etc)
			// Bounty rewards: bull shark liver/jaw, osprey beak, mogre mace,
			//   tern feather, pygmy kraken ink sac, tiger shark jaw
			// Kegs / ales (passive-effect consumables)
			// Salvaging station schematic, crystal extractor
		));

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
}
