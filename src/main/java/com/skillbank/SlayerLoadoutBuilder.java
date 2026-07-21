package com.skillbank;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemVariationMapping;
import net.runelite.client.plugins.banktags.tabs.Layout;

/**
 * Brief #91 Phase 3: builds the dynamic slayer tab for the player's CURRENT
 * task — required/protection items from the wiki task dataset, the strongest
 * owned gear for the task's recommended combat style (plus the strongest
 * non-degradable alternative when the top pick degrades), relevant potions,
 * top food, cannon supplies when the task is cannonable, and slayer utility.
 * <p>
 * Decisions (2026-07-09): style comes from the wiki recommendation;
 * ownership is bank-only; cannon rows only when task+location viable;
 * wilderness budget filtering is a later phase.
 */
@Slf4j
@Singleton
public class SlayerLoadoutBuilder
{
	private static final int ITEMS_PER_ROW = 8;

	/** Name tokens marking gear that degrades or consumes charges — the
	 *  "strongest non-degradable alternative" rule keys off these. Curated
	 *  here for v1; candidate for a pipeline-emitted flag later. */
	private static final String[] DEGRADABLE_TOKENS = {
		"dharok", "ahrim", "karil", "torag", "verac", "guthan",
		"crystal ", "bow of faerdhinen", "blade of saeldor",
		"toxic blowpipe", "blazing blowpipe", "trident", "sanguinesti",
		"scythe of vitur", "tumeken", "serpentine helm", "magma helm",
		"tanzanite helm", "abyssal tentacle", "craw's bow", "webweaver",
		"viggora", "ursine", "thammaron", "accursed sceptre",
		"venator bow", "warped sceptre", "barronite mace (l)",
	};

	/**
	 * Potion CHAINS per combat style: each chain covers one boost category
	 * and walks strongest → basic until the player owns something, so a
	 * melee loadout without super combats still surfaces attack/strength/
	 * defence potions. Names are FULL in-game base names (dose resolved at
	 * pick time, highest owned first). One pick per chain; an item covering
	 * several chains (super combat) dedupes via `taken`.
	 */
	private static final Map<String, String[][]> STYLE_POTION_CHAINS = Map.of(
		"melee", new String[][]{
			{"divine super combat potion", "super combat potion", "super attack",
				"combat potion", "attack potion"},
			{"divine super combat potion", "super combat potion", "super strength",
				"combat potion", "strength potion"},
			{"divine super combat potion", "super combat potion", "super defence",
				"defence potion"},
		},
		"range", new String[][]{
			{"divine ranging potion", "ranging potion", "bastion potion"},
		},
		"mage", new String[][]{
			{"divine magic potion", "magic potion", "battlemage potion"},
		}
	);

	/** Prayer restoration chain — moonlight moths are a legitimate prayer
	 *  alternative (Matt, 2026-07-10). */
	private static final String[] PRAYER_CHAIN = {
		"prayer potion", "super restore", "sanfew serum",
		"moonlight moth mix", "moonlight moth",
	};

	private static final String[] ANTIFIRE_CHAIN = {
		"extended super antifire", "super antifire potion",
		"extended antifire", "antifire potion",
	};

	/** Cannon parts + set + ammo (base ids). */
	private static final int[] CANNON_IDS = {6, 8, 10, 12, 11967, 2};

	/** Display order for wiki {{Recommended equipment}} slots. */
	private static final String[] WIKI_SLOT_ORDER = {
		"weapon", "special", "head", "cape", "neck", "body", "legs",
		"hands", "feet", "shield", "ammo", "ring",
	};

	/** Utility picks — EXACT in-game names only (lowercase), one pick per
	 *  row. No token/prefix matching anywhere in selection: fuzzy matching
	 *  produced "Shark paint" for "shark". */
	private static final String[][] UTILITY_EXACT = {
		{"expeditious bracelet"},
		{"bracelet of slaughter"},
		{"looting bag"},
		{"herb sack", "open herb sack"},
		{"bonecrusher necklace", "bonecrusher"},
		{"ash sanctifier"},
		{"slayer ring (eternal)", "slayer ring (8)", "slayer ring (7)",
			"slayer ring (6)", "slayer ring (5)", "slayer ring (4)",
			"slayer ring (3)", "slayer ring (2)", "slayer ring (1)"},
		{"eternal gem", "enchanted gem"},
	};

	/** Display labels for the loadout groups, used by the section-header
	 *  overlay. Keys match the internal group names below. */
	private static final Map<String, String> GROUP_LABELS = Map.of(
		"task", "Task & protection",
		"gear-melee", "Melee — recommended from OSRS wiki",
		"gear-range", "Ranged — recommended from OSRS wiki",
		"gear-mage", "Magic — recommended from OSRS wiki",
		"extras", "Also recommended from OSRS wiki",
		"potions", "Potions",
		"food", "Food",
		"cannon", "Cannon",
		"utility", "Utility"
	);

	/** First letter upper-cased for header display ("lava strykewyrms" ->
	 *  "Lava strykewyrms"); task names in the data are otherwise cased. */
	private static String capitalize(String s)
	{
		return s == null || s.isEmpty()
			? s
			: Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	/** pos → header label for the most recently built slayer layout; the
	 *  bank overlay reads this to draw section descriptions. */
	private final Map<Integer, String> headerByPos = new HashMap<>();

	public Map<Integer, String> headers()
	{
		return headerByPos;
	}

	private final ItemManager itemManager;
	private final SkillBankConfig config;

	@Inject
	SlayerLoadoutBuilder(ItemManager itemManager, SkillBankConfig config,
		com.google.gson.Gson gson)
	{
		this.itemManager = itemManager;
		this.config = config;
		SlayerTaskData.init(gson);
	}

	/** True when we have data + a resolvable task name. */
	public boolean hasTaskData(String taskName)
	{
		return SlayerTaskData.forTaskName(taskName) != null;
	}

	/**
	 * Item ids that should carry the slayer tag for this task IN ADDITION to
	 * the static slayer bucket: required + protection + recommended uniques.
	 * Tagging unowned items is harmless (they never render), and means the
	 * tab is already right when the player acquires them mid-task.
	 */
	public Set<Integer> taskMembership(String taskName)
	{
		SlayerTaskData.Task task = SlayerTaskData.forTaskName(taskName);
		Set<Integer> out = new LinkedHashSet<>();
		if (task == null)
		{
			return out;
		}
		addRefIds(out, task.requiredItems);
		addRefIds(out, task.protectionItems);
		addRefIds(out, task.recommendedItems);
		if (task.cannonViable)
		{
			for (int id : CANNON_IDS)
			{
				out.add(id);
			}
		}
		return out;
	}

	private static void addRefIds(Set<Integer> out, List<SlayerTaskData.ItemRef> refs)
	{
		if (refs == null)
		{
			return;
		}
		for (SlayerTaskData.ItemRef r : refs)
		{
			if (r != null && r.id != null && r.id > 0)
			{
				out.add(r.id);
			}
		}
	}

	/**
	 * Build the dynamic slayer Layout. Group order: Task & protection →
	 * Weapons → Armour → Recommended extras → Potions → Food → Cannon →
	 * Utility. Each group starts on a fresh bank row. Items tagged into the
	 * tab but not selected here auto-append after the layout (Bank Tags
	 * first-free-slot behaviour) — loadout on top, everything else below.
	 *
	 * @param bankTagName suffixed Bank Tags name the layout saves under
	 * @param ownedByBase variation base id → banked ids in that family
	 */
	public Layout buildLayout(String bankTagName, String taskName, String taskLocation,
		Map<Integer, List<Integer>> ownedByBase)
	{
		Layout layout = new Layout(bankTagName);
		headerByPos.clear();
		SlayerTaskData.Task task = SlayerTaskData.forTaskName(taskName);
		if (task == null)
		{
			return layout;
		}
		// v3: resolve WHICH monster variant this assignment is (Wyrms page
		// covers Wyrm and Lava strykewyrm) and, when Konar gave a location,
		// WHICH location record applies. Requirements, weakness, and cannon
		// viability all come from that scope — never from sibling variants.
		SlayerTaskData.Variant variant = task.variantFor(taskName);
		SlayerTaskData.VariantLocation variantLoc = null;
		if (variant != null && taskLocation != null && variant.locations != null)
		{
			String locLower = taskLocation.toLowerCase(Locale.ROOT);
			for (SlayerTaskData.VariantLocation loc : variant.locations)
			{
				if (loc == null || loc.name == null)
				{
					continue;
				}
				String wikiLoc = loc.name.toLowerCase(Locale.ROOT);
				if (wikiLoc.contains(locLower) || locLower.contains(wikiLoc))
				{
					variantLoc = loc;
					break;
				}
			}
		}

		// Cannon precedence: variant-location flag > task-location flag >
		// task-level "cannonable somewhere".
		boolean cannonHere = task.cannonViable;
		if (taskLocation != null && task.locations != null)
		{
			String locLower = taskLocation.toLowerCase(Locale.ROOT);
			for (SlayerTaskData.TaskLocation loc : task.locations)
			{
				if (loc == null || loc.name == null || loc.cannon == null)
				{
					continue;
				}
				String wikiLoc = loc.name.toLowerCase(Locale.ROOT);
				if (wikiLoc.contains(locLower) || locLower.contains(wikiLoc))
				{
					cannonHere = loc.cannon;
					break;
				}
			}
		}
		if (variantLoc != null && variantLoc.cannon != null)
		{
			cannonHere = variantLoc.cannon;
		}
		log.debug("[SkillBank] Slayer loadout: task='{}' variant='{}' location='{}' cannonHere={}",
			taskName, variant != null ? variant.name : null, taskLocation, cannonHere);

		Map<Integer, ItemMeta> meta = SkillBankSortData.itemMeta();
		String style = task.recommendedStyle != null
			? task.recommendedStyle.toLowerCase(Locale.ROOT) : "melee";
		if (!STYLE_POTION_CHAINS.containsKey(style))
		{
			style = "melee";
		}
		// Styles the loadout covers: every wiki-loadout style in page order,
		// else the single LLM-recommended style (heuristic fallback path).
		List<String> stylesToBuild = task.hasWikiLoadout()
			? task.styleOrder : java.util.Collections.singletonList(style);
		// Normalize any unknown style ids so potion-chain lookups can't
		// silently miss (wiki uses melee/range/mage after our mapping).
		List<String> potionStyles = new ArrayList<>();
		for (String st : stylesToBuild)
		{
			if (STYLE_POTION_CHAINS.containsKey(st))
			{
				potionStyles.add(st);
			}
		}
		if (potionStyles.isEmpty())
		{
			potionStyles.add("melee");
		}

		// Ordered groups; LinkedHashSet per group prevents dupes while
		// preserving insertion (strongest-first) order.
		Map<String, LinkedHashSet<Integer>> groups = new LinkedHashMap<>();
		groups.put("task", new LinkedHashSet<>());
		for (String st : stylesToBuild)
		{
			groups.put("gear-" + st, new LinkedHashSet<>());
		}
		for (String g : new String[]{"extras", "potions", "food", "cannon", "utility"})
		{
			groups.put(g, new LinkedHashSet<>());
		}
		Set<Integer> taken = new HashSet<>();

		// ── Task & protection: variant + location scoped ────────────────
		if (variant != null)
		{
			// v3 data: variant-wide requirements + this location's only.
			collectOwnedRefs(groups.get("task"), taken, variant.requiredItems, ownedByBase);
			if (variantLoc != null)
			{
				collectOwnedRefs(groups.get("task"), taken, variantLoc.requiredItems, ownedByBase);
			}
			// Task-level = "every variant, every location" under v3.
			collectOwnedRefs(groups.get("task"), taken, task.requiredItems, ownedByBase);
		}
		else
		{
			// Legacy data without variants: flat requirements.
			collectOwnedRefs(groups.get("task"), taken, task.requiredItems, ownedByBase);
		}
		// Any owned slayer helmet / black mask covers every sensory
		// protection — lead with it, then specific protection items.
		Integer helm = strongestOwnedByToken(ownedByBase, taken, "slayer helmet", "black mask");
		if (helm != null)
		{
			groups.get("task").add(helm);
			taken.add(helm);
		}
		if (variant != null)
		{
			collectOwnedRefs(groups.get("task"), taken, variant.protectionItems, ownedByBase);
		}
		collectOwnedRefs(groups.get("task"), taken, task.protectionItems, ownedByBase);

		// ── Gear rows ───────────────────────────────────────────────────
		if (task.hasWikiLoadout())
		{
			// The wiki's own slot-ranked loadout per style: for each slot,
			// walk the ranks and surface the FIRST item the player owns.
			for (String st : stylesToBuild)
			{
				Map<String, List<List<SlayerTaskData.ItemRef>>> slots = task.styles.get(st);
				if (slots == null)
				{
					continue;
				}
				LinkedHashSet<Integer> gear = groups.get("gear-" + st);
				List<String> chosenWeapons = new ArrayList<>();
				// Weapons first: collect owned picks ACROSS ALL RANKS (up
				// to 3) — owning a blowpipe must not hide a lower-ranked
				// atlatl the player also owns. If the wiki list yields
				// nothing owned, fall back to the player's highest-level
				// weapons for this style.
				List<Integer> weaponPicks = ownedAcrossRanks(
					slots.get("weapon"), ownedByBase, taken, 3);
				if (weaponPicks.isEmpty())
				{
					weaponPicks = bestOwnedForSlots(ownedByBase, meta,
						new HashSet<>(SkillBankData.itemsFor(st)), st, taken,
						new String[]{"weapon", "2h"}, 2);
				}
				for (int pick : weaponPicks)
				{
					gear.add(pick);
					taken.add(pick);
					chosenWeapons.add(nameOf(pick));
				}
				for (String slot : WIKI_SLOT_ORDER)
				{
					if ("weapon".equals(slot))
					{
						continue;
					}
					List<Integer> picks;
					if ("ammo".equals(slot))
					{
						// Ammo must match a chosen weapon — no atlatl darts
						// without the atlatl, no bolts without a crossbow.
						picks = ownedRankedAmmo(slots.get(slot), ownedByBase,
							taken, chosenWeapons);
					}
					else
					{
						picks = ownedInBestRank(slots.get(slot), ownedByBase, taken);
					}
					for (int pick : picks)
					{
						gear.add(pick);
						taken.add(pick);
					}
				}
				// Elemental weakness → surface the matching owned tome with
				// the mage gear ("mage" per wiki data, e.g. Tome of earth
				// for wyrms, Tome of water for lava strykewyrms).
				if ("mage".equals(st))
				{
					// The resolved variant's weakness only — a wyrm task
					// gets the earth tome, a lava strykewyrm the water
					// tome, never both. Legacy fallback: task-level list.
					List<String> elements = new ArrayList<>();
					if (variant != null && variant.elementalWeakness != null)
					{
						elements.add(variant.elementalWeakness);
					}
					else if (task.elementalWeaknesses != null)
					{
						for (SlayerTaskData.Weakness w : task.elementalWeaknesses)
						{
							if (w != null && w.element != null)
							{
								elements.add(w.element);
							}
						}
					}
					for (String element : elements)
					{
						Integer tome = ownedExact(ownedByBase, taken,
							"tome of " + element.toLowerCase(Locale.ROOT));
						if (tome != null)
						{
							gear.add(tome);
							taken.add(tome);
						}
					}
				}
			}
		}
		else
		{
			// Heuristic fallback: strongest owned by requirement/tier from
			// the style's tab bucket (+ non-degradable alternative).
			Set<Integer> styleBucket = new HashSet<>(SkillBankData.itemsFor(style));
			LinkedHashSet<Integer> gear = groups.get("gear-" + style);
			gear.addAll(bestOwnedForSlots(ownedByBase, meta, styleBucket,
				style, taken, new String[]{"weapon", "2h"}, 2));
			for (String slot : new String[]{"head", "body", "legs", "hands",
				"feet", "cape", "shield", "neck", "ring", "ammo"})
			{
				gear.addAll(bestOwnedForSlots(ownedByBase, meta,
					styleBucket, style, taken, new String[]{slot}, 1));
			}
		}

		// ── Recommended extras (owned, not already placed) ──────────────
		collectOwnedRefs(groups.get("extras"), taken, task.recommendedItems, ownedByBase);

		// ── Potions: one pick per chain, knocked down to whatever the
		//    player actually owns, highest dose first ────────────────────
		for (String st : potionStyles)
		{
			for (String[] chain : STYLE_POTION_CHAINS.get(st))
			{
				pickPotionChain(groups.get("potions"), taken, ownedByBase, chain);
			}
		}
		pickPotionChain(groups.get("potions"), taken, ownedByBase, PRAYER_CHAIN);
		if (task.hasAttribute("fiery") || task.hasAttribute("dragon"))
		{
			pickPotionChain(groups.get("potions"), taken, ownedByBase, ANTIFIRE_CHAIN);
		}

		// ── Food: top heal tiers owned + karambwan (EXACT names) ───────
		int foodFamilies = 0;
		for (String food : SkillBankSortData.FOOD_ORDER)
		{
			if (foodFamilies >= 3)
			{
				break;
			}
			Integer id = ownedExact(ownedByBase, taken, food.toLowerCase(Locale.ROOT));
			if (id != null && taken.add(id))
			{
				groups.get("food").add(id);
				foodFamilies++;
			}
		}
		Integer karam = ownedExact(ownedByBase, taken, "cooked karambwan");
		if (karam != null && taken.add(karam))
		{
			groups.get("food").add(karam);
		}

		// ── Cannon (gated per-location when the location is known) ─────
		if (cannonHere)
		{
			for (int id : CANNON_IDS)
			{
				Integer owned = ownedVariant(ownedByBase, id);
				if (owned != null && taken.add(owned))
				{
					groups.get("cannon").add(owned);
				}
			}
		}

		// ── Utility (exact names, first owned per row entry) ────────────
		for (String[] alternatives : UTILITY_EXACT)
		{
			Integer id = ownedExact(ownedByBase, taken, alternatives);
			if (id != null)
			{
				groups.get("utility").add(id);
				taken.add(id);
			}
		}

		// ── Emit: every group is preceded by a FULL BLANK ROW that the
		//    overlay fills with a divider line + description (Quest Helper
		//    style). headerByPos keys on the group's first ITEM position;
		//    the overlay draws in the empty row above it. ─────────────────
		int pos = 0;
		boolean dividers = config.sectionDividers();
		boolean firstHeader = true;
		for (Map.Entry<String, LinkedHashSet<Integer>> e : groups.entrySet())
		{
			LinkedHashSet<Integer> group = e.getValue();
			if (group.isEmpty())
			{
				continue;
			}
			if (pos % ITEMS_PER_ROW != 0)
			{
				pos = ((pos / ITEMS_PER_ROW) + 1) * ITEMS_PER_ROW;
			}
			if (dividers)
			{
				pos += ITEMS_PER_ROW; // blank divider row
				String label = GROUP_LABELS.get(e.getKey());
				if (label == null && e.getKey().startsWith("gear-"))
				{
					label = "Best owned gear (" + e.getKey().substring(5) + ")";
				}
				if (label == null)
				{
					label = e.getKey();
				}
				// The topmost divider names the actual assignment, so the tab
				// reads "Lava strykewyrms — task & protection" at a glance.
				if (firstHeader)
				{
					label = capitalize(task.name) + " — "
						+ Character.toLowerCase(label.charAt(0)) + label.substring(1);
					firstHeader = false;
				}
				headerByPos.put(pos, label);
			}
			for (int id : group)
			{
				layout.setItemAtPos(id, pos++);
			}
		}
		log.debug("[SkillBank] Slayer loadout for '{}' ({}): {} items placed",
			task.name, style, taken.size());
		return layout;
	}

	/**
	 * Merge the task loadout with the full static slayer layout: loadout
	 * groups keep their rows at the top; every remaining owned slayer item
	 * is appended below in static section order, starting on a fresh row.
	 * Without this, Bank Tags floods unpositioned tagged items into the
	 * loadout's row-padding gaps and the tab renders as an unstructured
	 * blob.
	 */
	public Layout mergeWithChaff(Layout loadout, Layout full)
	{
		int[] placedArr = loadout.getLayout();
		Set<Integer> placed = new HashSet<>();
		int maxPos = -1;
		for (int i = 0; i < placedArr.length; i++)
		{
			if (placedArr[i] > -1)
			{
				placed.add(placedArr[i]);
				maxPos = i;
			}
		}
		// Blank divider row, then the chaff under an "Everything else" rule
		// (plain fresh row when dividers are toggled off).
		boolean dividers = config.sectionDividers();
		int pos = (maxPos / ITEMS_PER_ROW + (dividers ? 2 : 1)) * ITEMS_PER_ROW;
		boolean any = false;
		int[] rest = full.getLayout();
		for (int id : rest)
		{
			if (id > -1 && placed.add(id))
			{
				if (!any)
				{
					if (dividers)
					{
						headerByPos.put(pos, "Everything else");
					}
					any = true;
				}
				loadout.setItemAtPos(id, pos++);
			}
		}
		return loadout;
	}

	// ── Selection helpers ───────────────────────────────────────────────

	private void collectOwnedRefs(Set<Integer> group, Set<Integer> taken,
		List<SlayerTaskData.ItemRef> refs, Map<Integer, List<Integer>> ownedByBase)
	{
		if (refs == null)
		{
			return;
		}
		for (SlayerTaskData.ItemRef r : refs)
		{
			if (r == null || r.id == null || r.id <= 0)
			{
				continue;
			}
			Integer owned = ownedVariant(ownedByBase, r.id);
			if (owned != null && taken.add(owned))
			{
				group.add(owned);
			}
		}
	}

	/** The banked id for {@code id}'s variation family, or null. */
	private static Integer ownedVariant(Map<Integer, List<Integer>> ownedByBase, int id)
	{
		List<Integer> family = ownedByBase.get(ItemVariationMapping.map(id));
		return family == null || family.isEmpty() ? null : family.get(0);
	}

	/** Collect owned items across ALL ranks in rank order (weapons: owning
	 *  a rank-1 blowpipe must not hide a rank-3 atlatl the player also
	 *  owns), capped at {@code limit}. */
	private static List<Integer> ownedAcrossRanks(List<List<SlayerTaskData.ItemRef>> ranks,
		Map<Integer, List<Integer>> ownedByBase, Set<Integer> taken, int limit)
	{
		List<Integer> out = new ArrayList<>();
		if (ranks == null)
		{
			return out;
		}
		for (List<SlayerTaskData.ItemRef> alternatives : ranks)
		{
			if (alternatives == null)
			{
				continue;
			}
			for (SlayerTaskData.ItemRef ref : alternatives)
			{
				if (out.size() >= limit)
				{
					return out;
				}
				if (ref == null || ref.id == null || ref.id <= 0)
				{
					continue;
				}
				Integer id = ownedVariant(ownedByBase, ref.id);
				if (id != null && !taken.contains(id) && !out.contains(id))
				{
					out.add(id);
				}
			}
		}
		return out;
	}

	/** Walk a wiki slot's ranks (each rank = equally-effective alternatives)
	 *  and return EVERY owned item from the best rank that has any owned —
	 *  the wiki calls them equal, so owning both bofa and the Eclipse atlatl
	 *  surfaces both. Wiki names are base items, so family expansion is safe
	 *  (degraded/ornamented variants inherit the recommendation). */
	private static List<Integer> ownedInBestRank(List<List<SlayerTaskData.ItemRef>> ranks,
		Map<Integer, List<Integer>> ownedByBase, Set<Integer> taken)
	{
		if (ranks == null)
		{
			return java.util.Collections.emptyList();
		}
		for (List<SlayerTaskData.ItemRef> alternatives : ranks)
		{
			if (alternatives == null)
			{
				continue;
			}
			List<Integer> owned = new ArrayList<>();
			for (SlayerTaskData.ItemRef ref : alternatives)
			{
				if (ref == null || ref.id == null || ref.id <= 0)
				{
					continue;
				}
				Integer id = ownedVariant(ownedByBase, ref.id);
				if (id != null && !taken.contains(id) && !owned.contains(id))
				{
					owned.add(id);
				}
			}
			if (!owned.isEmpty())
			{
				return owned;
			}
		}
		return java.util.Collections.emptyList();
	}

	/**
	 * Strongest owned items for the given equipment slots within the style's
	 * tab bucket, ranked by style requirement then tier. Returns up to
	 * {@code topN} picks, appending the strongest NON-degradable item when
	 * every top pick degrades.
	 */
	private List<Integer> bestOwnedForSlots(Map<Integer, List<Integer>> ownedByBase,
		Map<Integer, ItemMeta> meta, Set<Integer> styleBucket, String style,
		Set<Integer> taken, String[] slots, int topN)
	{
		String reqSkill = "range".equals(style) ? "ranged"
			: "mage".equals(style) ? "magic" : "attack";
		List<Integer> candidates = new ArrayList<>();
		for (Map.Entry<Integer, List<Integer>> e : ownedByBase.entrySet())
		{
			for (int owned : e.getValue())
			{
				if (taken.contains(owned) || !inBucket(styleBucket, owned))
				{
					continue;
				}
				ItemMeta m = meta.get(ItemVariationMapping.map(owned));
				if (m == null)
				{
					m = meta.get(owned);
				}
				if (m == null || m.slot == null)
				{
					continue;
				}
				for (String slot : slots)
				{
					if (slot.equals(m.slot))
					{
						candidates.add(owned);
						break;
					}
				}
			}
		}
		Comparator<Integer> strongest = Comparator
			.comparingInt((Integer id) -> score(meta, id, reqSkill)).reversed()
			.thenComparing(this::nameOf);
		candidates.sort(strongest);

		List<Integer> picks = new ArrayList<>();
		for (int id : candidates)
		{
			if (picks.size() >= topN)
			{
				break;
			}
			picks.add(id);
		}
		// Non-degradable alternative: if every pick degrades, surface the
		// strongest non-degradable candidate as well.
		if (!picks.isEmpty() && picks.stream().allMatch(this::isDegradable))
		{
			for (int id : candidates)
			{
				if (!picks.contains(id) && !isDegradable(id))
				{
					picks.add(id);
					break;
				}
			}
		}
		taken.addAll(picks);
		return picks;
	}

	private static boolean inBucket(Set<Integer> bucket, int owned)
	{
		if (bucket.contains(owned))
		{
			return true;
		}
		// Bucket holds base/canonical ids; owned may be a variant.
		return bucket.contains(ItemVariationMapping.map(owned));
	}

	private static int score(Map<Integer, ItemMeta> meta, int id, String reqSkill)
	{
		ItemMeta m = meta.get(ItemVariationMapping.map(id));
		if (m == null)
		{
			m = meta.get(id);
		}
		if (m == null)
		{
			return 0;
		}
		return m.requirement(reqSkill) * 100 + m.tier;
	}

	private boolean isDegradable(int id)
	{
		String n = nameOf(id);
		for (String tok : DEGRADABLE_TOKENS)
		{
			if (n.contains(tok))
			{
				return true;
			}
		}
		return false;
	}

	/** Walk a potion chain (strongest → basic) and add the first owned item
	 *  at its highest owned dose. Dose forms tried per name: "name(4..1)",
	 *  hunter-mix "name (2..1)", then the bare name (undosed items like
	 *  Moonlight moth). */
	private void pickPotionChain(Set<Integer> group, Set<Integer> taken,
		Map<Integer, List<Integer>> ownedByBase, String[] chain)
	{
		for (String base : chain)
		{
			Integer id = ownedBestDose(ownedByBase, taken, base);
			if (id != null)
			{
				taken.add(id);
				group.add(id);
				return;
			}
		}
	}

	private Integer ownedBestDose(Map<Integer, List<Integer>> ownedByBase,
		Set<Integer> taken, String base)
	{
		for (int d = 4; d >= 1; d--)
		{
			Integer id = ownedExact(ownedByBase, taken, base + "(" + d + ")");
			if (id != null)
			{
				return id;
			}
		}
		for (int d = 2; d >= 1; d--)
		{
			Integer id = ownedExact(ownedByBase, taken, base + " (" + d + ")");
			if (id != null)
			{
				return id;
			}
		}
		return ownedExact(ownedByBase, taken, base);
	}

	/** Owned id whose in-game name EXACTLY equals one of {@code names}
	 *  (lowercase), skipping already-taken ids. Names are tried in order. */
	private Integer ownedExact(Map<Integer, List<Integer>> ownedByBase,
		Set<Integer> taken, String... names)
	{
		for (String want : names)
		{
			for (List<Integer> fam : ownedByBase.values())
			{
				for (int id : fam)
				{
					if (!taken.contains(id) && nameOf(id).equals(want))
					{
						return id;
					}
				}
			}
		}
		return null;
	}

	/** Ammo compatibility with the chosen weapon — prevents surfacing
	 *  atlatl darts without the atlatl, bolts without a crossbow, etc. */
	private static boolean ammoCompatible(String ammoLower, String weaponLower)
	{
		if (ammoLower.contains("atlatl dart"))
		{
			return weaponLower != null && weaponLower.contains("atlatl");
		}
		if (ammoLower.contains("bolt"))
		{
			return weaponLower != null
				&& (weaponLower.contains("crossbow") || weaponLower.contains("ballista"));
		}
		if (ammoLower.contains("javelin"))
		{
			return weaponLower != null && weaponLower.contains("ballista");
		}
		if (ammoLower.endsWith(" dart") || ammoLower.endsWith(" darts"))
		{
			return weaponLower != null && weaponLower.contains("blowpipe");
		}
		if (ammoLower.contains("arrow"))
		{
			// Bows that fire arrows — not crossbows, not self-supplying
			// crystal/blowpipe/atlatl weapons.
			return weaponLower != null && weaponLower.contains("bow")
				&& !weaponLower.contains("crossbow")
				&& !weaponLower.contains("blowpipe")
				&& !weaponLower.contains("faerdhinen")
				&& !weaponLower.contains("crystal bow")
				&& !weaponLower.contains("craw");
		}
		return true;
	}

	/** ownedInBestRank, filtered to ammo compatible with ANY chosen weapon.
	 *  Walks ranks until one yields at least one compatible owned item. */
	private List<Integer> ownedRankedAmmo(List<List<SlayerTaskData.ItemRef>> ranks,
		Map<Integer, List<Integer>> ownedByBase, Set<Integer> taken,
		List<String> weaponsLower)
	{
		if (ranks == null)
		{
			return java.util.Collections.emptyList();
		}
		for (List<SlayerTaskData.ItemRef> alternatives : ranks)
		{
			if (alternatives == null)
			{
				continue;
			}
			List<Integer> owned = new ArrayList<>();
			for (SlayerTaskData.ItemRef ref : alternatives)
			{
				if (ref == null || ref.id == null || ref.id <= 0)
				{
					continue;
				}
				Integer id = ownedVariant(ownedByBase, ref.id);
				if (id == null || taken.contains(id) || owned.contains(id))
				{
					continue;
				}
				String ammoName = nameOf(id);
				boolean compatible = weaponsLower.isEmpty();
				for (String w : weaponsLower)
				{
					if (ammoCompatible(ammoName, w))
					{
						compatible = true;
						break;
					}
				}
				if (compatible)
				{
					owned.add(id);
				}
			}
			if (!owned.isEmpty())
			{
				return owned;
			}
		}
		return java.util.Collections.emptyList();
	}

	/** Strongest owned HEAD-slot item whose name contains any of the tokens
	 *  (slayer helmet family only — the slot guard keeps token matching from
	 *  grabbing non-gear lookalikes). */
	private Integer strongestOwnedByToken(Map<Integer, List<Integer>> ownedByBase,
		Set<Integer> taken, String... tokens)
	{
		Map<Integer, ItemMeta> meta = SkillBankSortData.itemMeta();
		Integer best = null;
		int bestScore = -1;
		for (List<Integer> fam : ownedByBase.values())
		{
			for (int id : fam)
			{
				if (taken.contains(id))
				{
					continue;
				}
				ItemMeta m = meta.get(ItemVariationMapping.map(id));
				if (m == null)
				{
					m = meta.get(id);
				}
				// Soft slot guard: veto only on KNOWN non-head metadata.
				// Black mask (i)'s variation base has no meta entry, and the
				// helm tokens below are specific enough on their own.
				if (m != null && m.slot != null && !"head".equals(m.slot))
				{
					continue;
				}
				String n = nameOf(id);
				for (String tok : tokens)
				{
					if (n.contains(tok))
					{
						int s = score(meta, id, "attack");
						if (s > bestScore)
						{
							bestScore = s;
							best = id;
						}
						break;
					}
				}
			}
		}
		return best;
	}

	private String nameOf(int id)
	{
		return itemManager.getItemComposition(id).getName().toLowerCase(Locale.ROOT);
	}
}
