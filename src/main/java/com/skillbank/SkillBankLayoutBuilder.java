package com.skillbank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.banktags.tabs.Layout;

/**
 * Brief #58 runtime sort engine.
 * <p>
 * Given a tag name and the canonical item IDs the player currently owns in
 * their bank, builds a {@link Layout} with items placed in the order a
 * knowledgeable player would arrange them by hand:
 * <ol>
 *   <li>Items are grouped by <b>section</b> using the per-tab section
 *       structure in {@link SkillBankSortData#TAB_SECTIONS}.</li>
 *   <li>Within combat / skilling tabs the items are partitioned into a
 *       <b>loadout zone</b> (top N tier values per weapon class or armour
 *       slot) and a <b>chaff zone</b> (everything else). Slayer / misc /
 *       quests / cosmetics / sailing skip the partition — they use single
 *       contiguous section order.</li>
 *   <li>Within each (section, group) the order is hand-authored (herbs,
 *       potions, food, runes, gems, weapon classes) where appropriate,
 *       otherwise tier descending then name.</li>
 * </ol>
 * No alphabetical fallback for sections with a known sort domain.
 */
@Slf4j
@Singleton
public class SkillBankLayoutBuilder
{
	/** OSRS bank grid width. Brief #65 aligns each section's first item to a
	 *  multiple of this value so sections start on fresh rows. RuneLite's
	 *  upstream Layout class doesn't expose the width as a constant, so it
	 *  lives here. */
	private static final int ITEMS_PER_ROW = 8;

	private final ItemManager itemManager;
	private final SkillBankConfig config;

	private final Map<String, Integer> herbOrderIndex;
	private final Map<String, Integer> potionOrderIndex;
	private final Map<String, Integer> foodOrderIndex;
	private final Map<String, Integer> runeOrderIndex;
	private final Map<String, Integer> comboRuneOrderIndex;
	private final Map<String, Integer> gemOrderIndex;

	@Inject
	SkillBankLayoutBuilder(ItemManager itemManager, SkillBankConfig config)
	{
		this.itemManager = itemManager;
		this.config = config;
		this.herbOrderIndex = lowercaseIndex(SkillBankSortData.HERB_ORDER);
		this.potionOrderIndex = lowercaseIndex(SkillBankSortData.POTION_ORDER);
		this.foodOrderIndex = lowercaseIndex(SkillBankSortData.FOOD_ORDER);
		this.runeOrderIndex = lowercaseIndex(SkillBankSortData.RUNE_ORDER);
		this.comboRuneOrderIndex = lowercaseIndex(SkillBankSortData.COMBO_RUNE_ORDER);
		this.gemOrderIndex = lowercaseIndex(SkillBankSortData.GEM_ORDER);
	}

	/** Brief #66: simple-mode is a per-combat-tab config toggle. Returns true
	 *  when the corresponding flag is on for melee / range / mage. */
	private boolean isSimpleMode(String tagName)
	{
		switch (tagName)
		{
			case "melee": return config.simpleMelee();
			case "range": return config.simpleRange();
			case "mage":  return config.simpleMage();
			default: return false;
		}
	}

	private static Map<String, Integer> lowercaseIndex(List<String> order)
	{
		Map<String, Integer> m = new HashMap<>();
		for (int i = 0; i < order.size(); i++)
		{
			m.put(order.get(i).toLowerCase(Locale.ROOT), i);
		}
		return m;
	}

	public Layout buildLayout(String tagName, Set<Integer> ownedCanonical)
	{
		return buildLayoutImpl(tagName, ownedCanonical, null);
	}

	/** Trace-producing variant. Same logic as {@link #buildLayout} but writes
	 *  a per-section / per-item dump into {@link LayoutTrace}. Used by the
	 *  side-panel "Dump layout trace" button (Brief #59). */
	public LayoutTrace traceLayout(String tagName, Set<Integer> ownedCanonical)
	{
		LayoutTrace trace = new LayoutTrace(tagName);
		buildLayoutImpl(tagName, ownedCanonical, trace);
		return trace;
	}

	private Layout buildLayoutImpl(String tagName, Set<Integer> ownedCanonical, LayoutTrace trace)
	{
		Layout layout = new Layout(tagName);

		List<Integer> tabItems = SkillBankData.itemsFor(tagName);
		if (tabItems == null || tabItems.isEmpty())
		{
			if (trace != null) trace.note("no items declared for tag");
			return layout;
		}

		// Brief #66: per-combat-tab simple-mode toggle picks an alternate
		// section structure where Head/Body/Legs/Hands/Feet collapse into
		// a single "Armor" section. Lookups go through the "<tag>_simple"
		// variant defined in SkillBankSortData.TAB_SECTIONS.
		boolean simpleMode = isSimpleMode(tagName);
		String sectionLookupKey = simpleMode ? tagName + "_simple" : tagName;
		List<String> sectionOrder = SkillBankSortData.TAB_SECTIONS.get(sectionLookupKey);
		Map<Integer, ItemMeta> meta = SkillBankSortData.itemMeta();

		// Canonicalize tab ids once and filter to ownership.
		List<Integer> ownedInTab = new ArrayList<>();
		Set<Integer> seen = new HashSet<>();
		for (Integer iid : tabItems)
		{
			if (iid == null || iid < 0)
			{
				continue;
			}
			int canonical = itemManager.canonicalize(iid);
			if (!ownedCanonical.contains(canonical) || !seen.add(canonical))
			{
				continue;
			}
			ownedInTab.add(canonical);
		}

		if (trace != null)
		{
			trace.header(ownedInTab.size());
		}

		if (ownedInTab.isEmpty())
		{
			return layout;
		}

		// Unknown tab → simple alpha-by-name fallback. Should never happen in
		// production since TAB_SECTIONS covers every tab in SkillBankData.tags().
		if (sectionOrder == null)
		{
			if (trace != null) trace.note("unknown tag — falling back to alpha sort");
			ownedInTab.sort(Comparator.comparing(this::nameOf));
			int pos = 0;
			for (Integer iid : ownedInTab)
			{
				layout.setItemAtPos(iid, pos++);
			}
			return layout;
		}

		// Bucket by section. Items whose meta-declared section isn't in the
		// tab's section order land in the last section (typically "Misc utility" /
		// "Uncategorized").
		Map<String, List<Integer>> bySection = new LinkedHashMap<>();
		for (String s : sectionOrder)
		{
			bySection.put(s, new ArrayList<>());
		}
		String fallbackSection = sectionOrder.get(sectionOrder.size() - 1);
		for (Integer iid : ownedInTab)
		{
			ItemMeta m = meta.get(iid);
			// ItemMeta carries the expanded-mode section name. Look up by
			// the tab's canonical id, not the _simple variant.
			String section = (m != null && m.sections != null) ? m.sections.get(tagName) : null;
			// Brief #66: in simple-mode, remap any armor-slot section
			// (Head/Body/Legs/Hands/Feet) into the collapsed "Armor"
			// section before the bucket lookup.
			if (simpleMode && section != null
				&& SkillBankSortData.SIMPLE_ARMOR_SECTIONS.contains(section))
			{
				section = "Armor";
			}
			if (section == null || !bySection.containsKey(section))
			{
				section = fallbackSection;
			}
			bySection.get(section).add(iid);
		}

		boolean twoZone = SkillBankSortData.TWO_ZONE_TABS.contains(tagName);

		// Per-section results so we can render the trace by zone after
		// partitioning, while still emitting the layout in zone1 → zone2 order.
		List<SectionResult> results = new ArrayList<>();
		for (String section : sectionOrder)
		{
			List<Integer> items = bySection.get(section);
			if (items.isEmpty())
			{
				continue;
			}
			String sortMethod = sortSection(items, section, tagName);
			List<Integer> sec1 = new ArrayList<>();
			List<Integer> sec2 = new ArrayList<>();
			if (twoZone)
			{
				// Brief #69: combat tabs use a 60+ requirement-level cutoff.
				// Skilling tabs keep the original top-N tiers heuristic.
				if (isCombatTab(tagName))
				{
					partitionByRequirement(items, section, tagName, sec1, sec2);
				}
				else
				{
					partitionTopTiers(items, section, tagName, sec1, sec2);
				}
			}
			else
			{
				sec1.addAll(items);
			}
			results.add(new SectionResult(section, sortMethod, sec1, sec2));
		}

		// Brief #65: each non-empty (section, zone) block starts on a fresh
		// bank row. Trailing slots in a partial row stay unset — Layout
		// auto-fills the gap with -1 in setItemAtPos, which renders as empty
		// in Bank Tags + Bank Tag Layouts. (Confirmed against the upstream
		// Layout.setItemAtPos implementation.) The `firstBlock` flag spans
		// both zone loops so the first zone-2 block also aligns to the next
		// row after zone 1 ends, giving an implicit zone separator.
		int pos = 0;
		boolean firstBlock = true;
		// Brief #75: cosmetics row-breaks by set_name. Brief #76 (revised):
		// crafting Gems section also row-breaks by set_name to give each
		// gem family its own row (Uncut → Cut → Ring → Necklace → Amulet
		// → Bracelet). The same ItemMeta.setName field carries both.
		boolean cosmeticsRowBreak = "cosmetics".equals(tagName);
		boolean craftingGemsRowBreak = "crafting".equals(tagName);
		// Zone 1 first, in section order.
		for (SectionResult r : results)
		{
			if (r.zone1.isEmpty()) continue;
			if (!firstBlock && pos % ITEMS_PER_ROW != 0)
			{
				pos = ((pos / ITEMS_PER_ROW) + 1) * ITEMS_PER_ROW;
			}
			if (trace != null) trace.section(r.section, 1, r.zone1.size(), r.sortMethod);
			String lastSetName = null;
			boolean rowBreakOnSet = cosmeticsRowBreak
				|| (craftingGemsRowBreak && "Gems".equals(r.section));
			for (Integer iid : r.zone1)
			{
				if (rowBreakOnSet)
				{
					ItemMeta im = meta.get(iid);
					String setName = im != null ? im.setName : null;
					if (lastSetName != null && !lastSetName.equals(setName))
					{
						if (pos % ITEMS_PER_ROW != 0)
						{
							pos = ((pos / ITEMS_PER_ROW) + 1) * ITEMS_PER_ROW;
						}
					}
					lastSetName = setName;
				}
				if (trace != null) trace.item(pos, iid, nameOf(iid), meta.get(iid), 1);
				layout.setItemAtPos(iid, pos++);
			}
			firstBlock = false;
		}
		// Zone 2 next.
		for (SectionResult r : results)
		{
			if (r.zone2.isEmpty()) continue;
			if (!firstBlock && pos % ITEMS_PER_ROW != 0)
			{
				pos = ((pos / ITEMS_PER_ROW) + 1) * ITEMS_PER_ROW;
			}
			if (trace != null) trace.section(r.section, 2, r.zone2.size(), r.sortMethod);
			for (Integer iid : r.zone2)
			{
				if (trace != null) trace.item(pos, iid, nameOf(iid), meta.get(iid), 2);
				layout.setItemAtPos(iid, pos++);
			}
			firstBlock = false;
		}

		int z1 = 0, z2 = 0;
		for (SectionResult r : results)
		{
			z1 += r.zone1.size();
			z2 += r.zone2.size();
		}
		log.debug("Layout for '{}': {} owned items → zone1={}, zone2={}",
			tagName, ownedInTab.size(), z1, z2);
		return layout;
	}

	// ── Sorting helpers ────────────────────────────────────────────────────

	/** Sorts items in place. Returns the name of the comparator used so the
	 *  trace can show which sort domain fired for each section. */
	private String sortSection(List<Integer> items, String section, String tagName)
	{
		// Brief #75 (revised): fletching uses a per-section comparator with
		// tier ASC + sub-rank groupings (shafts/tips/unstrung etc.).
		if ("fletching".equals(tagName))
		{
			items.sort((a, b) -> compareFletching(a, b, section));
			return "FLETCHING_PER_SECTION";
		}
		// Brief #75: cosmetics sorts by set rank → set name → slot rank →
		// item name so items in the same set cluster contiguously. The
		// layout builder then injects a row break between sets.
		if ("cosmetics".equals(tagName))
		{
			items.sort((a, b) -> compareCosmetics(a, b, section));
			return "COSMETICS_SET+SLOT";
		}
		if ("herblore".equals(tagName) && "Herbs".equals(section))
		{
			items.sort(this::compareHerbs);
			return "HERB_ORDER";
		}
		// Brief #66: simple-mode Armor section sorts head → body → legs →
		// hands → feet, then by tier within slot.
		if ("Armor".equals(section))
		{
			items.sort(this::compareArmorSlotThenTier);
			return "ARMOR_SLOT+tier_desc";
		}
		if ("Finished potions".equals(section)
			|| "Barbarian mixes".equals(section)
			|| "Divine, extended & upgraded variants".equals(section)
			|| "Combat potions".equals(section)
			|| "Prayer & restores".equals(section)
			|| "Prayer-restoring consumables".equals(section)
			|| "Run-energy consumables".equals(section))
		{
			items.sort(this::comparePotions);
			return "POTION_ORDER";
		}
		// Brief #66: cooking section names changed. "Cooked fish" / "Cooked
		// meat" / "Combo food" / "Baked & cooked goods" should all use the
		// FOOD_ORDER comparator so canonical heal order is preserved.
		if (("cooking".equals(tagName) &&
				("Cooked fish".equals(section)
					|| "Cooked meat".equals(section)
					|| "Combo food".equals(section)
					|| "Baked & cooked goods".equals(section)))
			|| "Food".equals(section))
		{
			items.sort(this::compareFood);
			return "FOOD_ORDER";
		}
		if ("Runes".equals(section) || "Core runes".equals(section))
		{
			items.sort(this::compareRunes);
			return "RUNE_ORDER";
		}
		if ("Combination runes".equals(section))
		{
			items.sort(this::compareComboRunes);
			return "COMBO_RUNE_ORDER";
		}
		if ("Gems".equals(section))
		{
			items.sort(this::compareGems);
			return "GEM_ORDER";
		}
		if ("Weapons".equals(section))
		{
			items.sort(this::compareWeapons);
			return "WEAPON_CLASS_ORDER+tier_desc";
		}
		items.sort(this::compareByTierDescThenName);
		return "tier_desc+name";
	}

	/** Per-section sort + zone partition output. Used by buildLayoutImpl to
	 *  emit zone 1 (all sections in order) then zone 2 (all sections in order). */
	private static final class SectionResult
	{
		final String section;
		final String sortMethod;
		final List<Integer> zone1;
		final List<Integer> zone2;

		SectionResult(String section, String sortMethod, List<Integer> zone1, List<Integer> zone2)
		{
			this.section = section;
			this.sortMethod = sortMethod;
			this.zone1 = zone1;
			this.zone2 = zone2;
		}
	}

	/** Compute the top-N tier set per group within a section and split items. */
	private void partitionTopTiers(
		List<Integer> sectionItems, String section, String tagName,
		List<Integer> zone1, List<Integer> zone2)
	{
		int n = topNForSection(section, tagName);
		Map<Integer, ItemMeta> meta = SkillBankSortData.itemMeta();

		// Group by weapon class for Weapons section; one logical group for
		// everything else (we still apply top-N to that one group).
		Map<String, List<Integer>> byGroup = new LinkedHashMap<>();
		if ("Weapons".equals(section))
		{
			for (Integer iid : sectionItems)
			{
				ItemMeta m = meta.get(iid);
				String g = (m != null && m.weaponClass != null) ? m.weaponClass : "_other";
				byGroup.computeIfAbsent(g, k -> new ArrayList<>()).add(iid);
			}
		}
		else
		{
			byGroup.put(section, sectionItems);
		}

		for (Map.Entry<String, List<Integer>> e : byGroup.entrySet())
		{
			List<Integer> group = e.getValue();
			Set<Integer> topTiers = computeTopTiers(group, n);
			for (Integer iid : group)
			{
				ItemMeta m = meta.get(iid);
				int t = m != null ? m.tier : 0;
				if (t > 0 && topTiers.contains(t))
				{
					zone1.add(iid);
				}
				else
				{
					zone2.add(iid);
				}
			}
		}
	}

	private Set<Integer> computeTopTiers(List<Integer> group, int n)
	{
		Map<Integer, ItemMeta> meta = SkillBankSortData.itemMeta();
		Set<Integer> allTiers = new TreeSet<>(Comparator.reverseOrder());
		for (Integer iid : group)
		{
			ItemMeta m = meta.get(iid);
			if (m != null && m.tier > 0)
			{
				allTiers.add(m.tier);
			}
		}
		Set<Integer> top = new HashSet<>();
		int taken = 0;
		for (Integer t : allTiers)
		{
			if (taken++ >= n)
			{
				break;
			}
			top.add(t);
		}
		return top;
	}

	private int topNForSection(String section, String tagName)
	{
		// Combat tabs surface more endgame than skilling tabs.
		if ("melee".equals(tagName) || "range".equals(tagName) || "mage".equals(tagName))
		{
			return 3;
		}
		return 2;
	}

	// ── Brief #73: top-N unique levels dynamic zone partition ──────────────

	/** Sections in any combat tab that always route to Zone 1, regardless
	 *  of requirement. Endgame items here mostly carry no level requirement
	 *  and the sections are small enough to surface everything. Brief #78
	 *  added Food — cooked food never has equipment requirements so the
	 *  level-based partitioner would dump it into Zone 2 by default. */
	private static final Set<String> ALWAYS_ZONE1_SECTIONS = Set.of(
		"Capes", "Neck", "Rings", "Food"
	);

	/** Force-Zone-1 for items with no requirement data but high tier. Catches
	 *  Fire cape / Barrows gloves / Void / Ava's family — quest- or
	 *  activity-gated endgame items that have no level requirement. */
	private static final int NO_REQ_TIER_FORCE_FLOOR = 80;

	private static boolean isCombatTab(String tag)
	{
		return "melee".equals(tag) || "range".equals(tag) || "mage".equals(tag);
	}

	/**
	 * Brief #73: dynamic zone split that scales with player progression.
	 * For each section (independently), collect the unique requirement
	 * levels owned, sort descending, take the top N. Items at those levels
	 * are Zone 1; everything else is Zone 2. N comes from
	 * {@link SkillBankConfig#zoneTierCount()} (default 3, range 1..6).
	 * <p>
	 * Capes/Neck/Rings always Zone 1. Mage Runes section: no zoning (all
	 * runes useful). Training & utility: always Zone 2. High-tier
	 * (tier ≥ 80) items with no requirement always Zone 1 (fire cape, etc.).
	 */
	private void partitionByRequirement(
		List<Integer> sectionItems, String section, String tagName,
		List<Integer> zone1, List<Integer> zone2)
	{
		Map<Integer, ItemMeta> meta = SkillBankSortData.itemMeta();

		// Capes / Neck / Rings → every item to Zone 1.
		if (ALWAYS_ZONE1_SECTIONS.contains(section))
		{
			zone1.addAll(sectionItems);
			return;
		}
		// Training & utility (or anything else with that name) → always chaff.
		if (section != null
			&& (section.contains("Training") || section.contains("utility")))
		{
			zone2.addAll(sectionItems);
			return;
		}
		// Mage Runes section → no zoning (all runes are loadout-relevant).
		if ("Runes".equals(section) && "mage".equals(tagName))
		{
			zone1.addAll(sectionItems);
			return;
		}

		int n = Math.max(1, config.zoneTierCount());

		// Collect unique requirement levels owned in this section, in descending
		// order. TreeSet with reverse-comparator does both the dedup and sort.
		java.util.TreeSet<Integer> uniqueLevels = new java.util.TreeSet<>(
			java.util.Comparator.reverseOrder());
		for (Integer iid : sectionItems)
		{
			ItemMeta m = meta.get(iid);
			if (m == null)
			{
				continue;
			}
			uniqueLevels.add(effectiveReq(m, section, tagName));
		}

		// Top N unique levels — anything else lives in Zone 2.
		Set<Integer> topLevels = new HashSet<>();
		int taken = 0;
		for (Integer lv : uniqueLevels)
		{
			if (taken++ >= n) break;
			topLevels.add(lv);
		}

		for (Integer iid : sectionItems)
		{
			ItemMeta m = meta.get(iid);
			if (m == null)
			{
				zone2.add(iid);
				continue;
			}
			int req = effectiveReq(m, section, tagName);
			// High-tier quest-gated / activity-gated items always surface.
			if (req == 0 && m.tier >= NO_REQ_TIER_FORCE_FLOOR)
			{
				zone1.add(iid);
				continue;
			}
			if (topLevels.contains(req))
			{
				zone1.add(iid);
			}
			else
			{
				zone2.add(iid);
			}
		}
	}

	/**
	 * Brief #73: effective requirement level for the zone partition.
	 *  - Melee Weapons: max(attack, strength) — covers warhammers (Strength-
	 *    gated) and normal weapons (Attack-gated) on the same scale.
	 *  - Range armour: max(defence, ranged) — mid-tier range gear gates on
	 *    Ranged, high-tier on Defence + Ranged.
	 *  - Mage armour: max(magic, defence) — mage robes gate on Magic, tankier
	 *    mage armour adds Defence.
	 *  - Weapons / Ammunition: the tab's offensive skill.
	 *  - Other (head/body/legs/hands/feet/shield): defence.
	 */
	private int effectiveReq(ItemMeta m, String section, String tagName)
	{
		if ("Weapons".equals(section))
		{
			if ("melee".equals(tagName))
			{
				return Math.max(m.requirement("attack"), m.requirement("strength"));
			}
			if ("range".equals(tagName)) return m.requirement("ranged");
			if ("mage".equals(tagName))  return m.requirement("magic");
			return m.requirement("attack");
		}
		if ("Ammunition".equals(section))
		{
			return "range".equals(tagName) ? m.requirement("ranged")
				: m.requirement("attack");
		}
		if (isArmourSection(section))
		{
			if ("range".equals(tagName))
			{
				return Math.max(m.requirement("defence"), m.requirement("ranged"));
			}
			if ("mage".equals(tagName))
			{
				return Math.max(m.requirement("magic"), m.requirement("defence"));
			}
		}
		return m.requirement("defence");
	}

	private static boolean isArmourSection(String section)
	{
		return "Head".equals(section) || "Body".equals(section)
			|| "Legs".equals(section) || "Hands".equals(section)
			|| "Feet".equals(section)
			|| "Shields & defenders".equals(section)
			|| "Shields & off-hands".equals(section)
			|| "Off-hands, books & tomes".equals(section);
	}

	/** Kept for the older Brief #69 callsites — returns the *primary* skill
	 *  for a combat (section, tab) pair (does NOT take the dual-skill max).
	 *  Currently unused at runtime; left in for trace-log inspection. */
	@SuppressWarnings("unused")
	private static String combatRequirementSkill(String section, String tagName)
	{
		if ("Weapons".equals(section) || "Ammunition".equals(section))
		{
			if ("range".equals(tagName)) return "ranged";
			if ("mage".equals(tagName)) return "magic";
			return "attack";
		}
		// Head, Body, Legs, Hands, Feet, Shields & defenders, Shields & off-hands,
		// Off-hands & books & tomes — all gate on defence.
		return "defence";
	}

	// ── Comparators ────────────────────────────────────────────────────────

	private int compareHerbs(int a, int b)
	{
		String na = nameOf(a).toLowerCase(Locale.ROOT);
		String nb = nameOf(b).toLowerCase(Locale.ROOT);
		int ia = herbBaseRank(na);
		int ib = herbBaseRank(nb);
		if (ia != ib)
		{
			return Integer.compare(ia, ib);
		}
		// Same herb → Grimy first, then Clean.
		boolean grimyA = na.startsWith("grimy ");
		boolean grimyB = nb.startsWith("grimy ");
		if (grimyA != grimyB)
		{
			return grimyA ? -1 : 1;
		}
		return na.compareTo(nb);
	}

	private int herbBaseRank(String nameLower)
	{
		for (Map.Entry<String, Integer> e : herbOrderIndex.entrySet())
		{
			// Match "guam", "marrentill", etc. anywhere in the name (handles
			// "Grimy guam", "Clean guam", "Guam potion (unf)", etc.).
			if (nameLower.contains(e.getKey()))
			{
				return e.getValue();
			}
		}
		return Integer.MAX_VALUE;
	}

	private int comparePotions(int a, int b)
	{
		String na = nameOf(a).toLowerCase(Locale.ROOT);
		String nb = nameOf(b).toLowerCase(Locale.ROOT);
		int ia = potionFamilyRank(na);
		int ib = potionFamilyRank(nb);
		if (ia != ib)
		{
			return Integer.compare(ia, ib);
		}
		// Same family → dose 4 → 1.
		int da = potionDose(na);
		int db = potionDose(nb);
		if (da != db)
		{
			return Integer.compare(db, da);
		}
		return na.compareTo(nb);
	}

	private int potionFamilyRank(String nameLower)
	{
		int best = Integer.MAX_VALUE;
		for (Map.Entry<String, Integer> e : potionOrderIndex.entrySet())
		{
			if (nameLower.contains(e.getKey()) && e.getValue() < best)
			{
				best = e.getValue();
			}
		}
		return best;
	}

	private int potionDose(String nameLower)
	{
		int len = nameLower.length();
		if (len >= 3 && nameLower.charAt(len - 1) == ')'
			&& nameLower.charAt(len - 3) == '(')
		{
			char c = nameLower.charAt(len - 2);
			if (c >= '1' && c <= '4')
			{
				return c - '0';
			}
		}
		return 0;
	}

	private int compareFood(int a, int b)
	{
		String na = nameOf(a).toLowerCase(Locale.ROOT);
		String nb = nameOf(b).toLowerCase(Locale.ROOT);
		int ia = foodRank(na);
		int ib = foodRank(nb);
		if (ia != ib)
		{
			return Integer.compare(ia, ib);
		}
		return na.compareTo(nb);
	}

	private int foodRank(String nameLower)
	{
		Integer r = foodOrderIndex.get(nameLower);
		if (r != null)
		{
			return r;
		}
		// Partial match for variants ("Cooked karambwan" vs "Cooked karambwan (uncooked)").
		for (Map.Entry<String, Integer> e : foodOrderIndex.entrySet())
		{
			if (nameLower.startsWith(e.getKey()))
			{
				return e.getValue();
			}
		}
		return Integer.MAX_VALUE;
	}

	private int compareRunes(int a, int b)
	{
		return Integer.compare(runeRank(nameOf(a)), runeRank(nameOf(b)));
	}

	private int runeRank(String name)
	{
		String n = name.toLowerCase(Locale.ROOT);
		for (Map.Entry<String, Integer> e : runeOrderIndex.entrySet())
		{
			if (n.startsWith(e.getKey() + " rune"))
			{
				return e.getValue();
			}
		}
		return Integer.MAX_VALUE;
	}

	private int compareComboRunes(int a, int b)
	{
		return Integer.compare(comboRuneRank(nameOf(a)), comboRuneRank(nameOf(b)));
	}

	private int comboRuneRank(String name)
	{
		String n = name.toLowerCase(Locale.ROOT);
		for (Map.Entry<String, Integer> e : comboRuneOrderIndex.entrySet())
		{
			if (n.startsWith(e.getKey() + " rune"))
			{
				return e.getValue();
			}
		}
		return Integer.MAX_VALUE;
	}

	// ── Brief #76 (revised): per-gem-family row layout for Gems section ──
	//
	// Each gem owns a row: Uncut X → X → X ring → X necklace → X amulet
	// → X bracelet. Precious gems (gold-bar jewellery) come first by
	// crafting level, then semi-precious. Family rank determines row
	// order; sub-rank determines within-row order.

	private static final Map<String, Integer> GEM_FAMILY_RANK = Map.ofEntries(
		// Precious (gold-bar) gems, ascending crafting level.
		Map.entry("Sapphire",     1),
		Map.entry("Emerald",      2),
		Map.entry("Ruby",         3),
		Map.entry("Diamond",      4),
		Map.entry("Dragonstone",  5),
		Map.entry("Onyx",         6),
		Map.entry("Zenyte",       7),
		// Semi-precious (silver-bar) gems.
		Map.entry("Opal",         8),
		Map.entry("Jade",         9),
		Map.entry("Red topaz",   10)
	);

	/** Returns family rank for a gem family name. Items without setName
	 *  (or with an unknown family) sort to the end. */
	private static int gemFamilyRank(String family)
	{
		if (family == null) return 99;
		Integer r = GEM_FAMILY_RANK.get(family);
		return r != null ? r : 50;
	}

	/** Within-family ordering: uncut → cut → ring → necklace → amulet →
	 *  bracelet. Anything else (jewellery variants, charged orbs, etc.)
	 *  sorts to the end of the row. */
	private static int gemSubRank(String name)
	{
		String n = name.toLowerCase(Locale.ROOT);
		if (n.startsWith("uncut ")) return 0;
		// Bare cut gem name: no slot keyword, no "uncut " prefix.
		if (!n.contains(" ring") && !n.contains(" necklace")
			&& !n.contains(" amulet") && !n.contains(" bracelet")
			&& !n.contains("dragon necklace"))
		{
			return 1;
		}
		if (n.contains(" ring"))     return 2;
		if (n.contains(" necklace") || n.contains("dragon necklace")) return 3;
		if (n.contains(" amulet"))   return 4;
		if (n.contains(" bracelet")) return 5;
		return 9;
	}

	/** Brief #76 (revised): comparator for the crafting Gems section.
	 *  Sort by gem family → within-family sub-rank → name. The layout
	 *  builder then injects a row break between families. */
	private int compareGems(int a, int b)
	{
		Map<Integer, ItemMeta> meta = SkillBankSortData.itemMeta();
		ItemMeta ma = meta.get(a);
		ItemMeta mb = meta.get(b);
		int fa = gemFamilyRank(ma != null ? ma.setName : null);
		int fb = gemFamilyRank(mb != null ? mb.setName : null);
		if (fa != fb) return Integer.compare(fa, fb);

		int sa = gemSubRank(nameOf(a));
		int sb = gemSubRank(nameOf(b));
		if (sa != sb) return Integer.compare(sa, sb);
		return nameOf(a).compareToIgnoreCase(nameOf(b));
	}

	private int compareWeapons(int a, int b)
	{
		Map<Integer, ItemMeta> meta = SkillBankSortData.itemMeta();
		ItemMeta ma = meta.get(a);
		ItemMeta mb = meta.get(b);
		int ra = ma != null ? SkillBankSortData.weaponClassRank(ma.weaponClass) : Integer.MAX_VALUE;
		int rb = mb != null ? SkillBankSortData.weaponClassRank(mb.weaponClass) : Integer.MAX_VALUE;
		if (ra != rb)
		{
			return Integer.compare(ra, rb);
		}
		// Same class → tier descending.
		int ta = ma != null ? ma.tier : 0;
		int tb = mb != null ? mb.tier : 0;
		if (ta != tb)
		{
			return Integer.compare(tb, ta);
		}
		return nameOf(a).compareTo(nameOf(b));
	}

	/** Brief #66: simple-mode Armor section ordering. Sort by slot rank
	 *  (head → body → legs → hands → feet), then by tier descending, then
	 *  by name as a final tiebreaker. Items without slot metadata bucket
	 *  at the end. */
	private int compareArmorSlotThenTier(int a, int b)
	{
		Map<Integer, ItemMeta> meta = SkillBankSortData.itemMeta();
		ItemMeta ma = meta.get(a);
		ItemMeta mb = meta.get(b);
		int sa = ma != null ? SkillBankSortData.armorSlotRank(ma.slot) : 99;
		int sb = mb != null ? SkillBankSortData.armorSlotRank(mb.slot) : 99;
		if (sa != sb)
		{
			return Integer.compare(sa, sb);
		}
		int ta = ma != null ? ma.tier : 0;
		int tb = mb != null ? mb.tier : 0;
		if (ta != tb)
		{
			return Integer.compare(tb, ta);
		}
		return nameOf(a).compareTo(nameOf(b));
	}

	/**
	 * Brief #68: universal default sort. Wearables lead by slot rank
	 * (head → body → legs → hands → feet → cape → neck → ring → weapon →
	 * shield → ammo) so any outfit/equipment section across all tabs ends
	 * up in head-to-toe order. Within the same slot, sort by tier
	 * descending, then alphabetically. Non-wearables (slot=null) get
	 * slot rank 999 and naturally sit after the wearables in the section.
	 */
	private int compareByTierDescThenName(int a, int b)
	{
		Map<Integer, ItemMeta> meta = SkillBankSortData.itemMeta();
		ItemMeta ma = meta.get(a);
		ItemMeta mb = meta.get(b);
		int sa = ma != null ? SkillBankSortData.slotRank(ma.slot) : 999;
		int sb = mb != null ? SkillBankSortData.slotRank(mb.slot) : 999;
		if (sa != sb)
		{
			return Integer.compare(sa, sb);
		}
		int ta = ma != null ? ma.tier : 0;
		int tb = mb != null ? mb.tier : 0;
		if (ta != tb)
		{
			return Integer.compare(tb, ta);
		}
		return nameOf(a).compareTo(nameOf(b));
	}

	private String nameOf(int itemId)
	{
		try
		{
			String n = itemManager.getItemComposition(itemId).getName();
			return n != null ? n : "";
		}
		catch (Exception e)
		{
			return "";
		}
	}

	// ── Brief #75: fletching per-section sort ──────────────────────────────

	/** Fletching tier resolver. Combines wood-tier ranks (for bows, stocks,
	 *  arrow shafts) and metal-tier ranks (for finished ammo, tips). Higher
	 *  number = higher tier. Returns 0 if no token matches — those items
	 *  sort to the front under the ASC ordering. */
	private static int fletchingTier(String name)
	{
		String n = name.toLowerCase(Locale.ROOT);
		// Wood tiers first — bow / stock / shaft names lead with wood token.
		if (n.contains("redwood")) return 60;
		if (n.contains("magic ") || n.startsWith("magic")) return 50;
		if (n.contains("yew ")   || n.startsWith("yew"))   return 40;
		if (n.contains("maple ") || n.startsWith("maple")) return 30;
		if (n.contains("willow ") || n.startsWith("willow")) return 20;
		if (n.contains("oak ")   || n.startsWith("oak"))   return 10;
		// Metal tiers for ammo, tips, etc. Specials get a sentinel above
		// dragon so they sort to the end of crossbow/dart progressions.
		if (n.contains("zaryte"))   return 95;
		if (n.contains("armadyl"))  return 90;
		if (n.contains("dragon "))  return 80;
		if (n.contains("amethyst")) return 75;
		if (n.contains("rune ") || n.contains("runite "))   return 70;
		if (n.contains("adamant ")) return 60;
		if (n.contains("mithril "))  return 50;
		if (n.contains("broad"))    return 45;
		if (n.contains("steel "))   return 30;
		if (n.contains("iron "))    return 20;
		if (n.contains("bronze "))  return 10;
		return 0;
	}

	/** Per-section sub-rank — keeps related items adjacent inside a
	 *  fletching section in the order players think about them. Lower
	 *  rank = earlier in the section. The combination of section + this
	 *  sub-rank + tier ASC produces the brief #75 layout. */
	private static int fletchingSubRank(String name, String section)
	{
		String n = name.toLowerCase(Locale.ROOT);
		switch (section)
		{
			case "Tools":
				if (n.contains("knife")) return 0;
				if (n.contains("fletching cape")) return 1;
				if (n.contains("fletching hood")) return 2;
				return 9;
			case "Feathers":
				if (n.contains("feather") && !n.contains("string")) return 0;
				if (n.contains("crossbow string")) return 3;
				if (n.contains("magic string")) return 2;
				if (n.contains("bow string")) return 1;
				return 9;
			case "Arrows":
				if (n.contains("arrow shaft")) return 0;
				if (n.contains("headless arrow")) return 1;
				return 2;   // finished arrows
			case "Bows":
				// Brief #75: within each wood tier, sort
				//   shortbow (u) → shortbow → longbow (u) → longbow
				{
					boolean lb = n.contains("longbow") || n.contains("comp bow")
						|| n.contains("composite bow");
					boolean u = n.contains("(u)") || n.contains("unstrung");
					int base = lb ? 2 : 0;
					return base + (u ? 0 : 1);
				}
			case "Crossbows":
				// Brief #75: within tier, stock → unstrung crossbow → finished.
				if (n.contains("stock")) return 0;
				if (n.contains("(u)") || n.contains("unstrung")) return 1;
				return 2;
			case "Bolts":
				// tip → unfinished → finished plain → gem-tipped variant.
				if (n.contains("bolt tip") || n.endsWith(" bolt tips")) return 0;
				if (n.contains("unfinished bolt") || n.contains("unfinished broad")) return 1;
				// Gem-tipped bolts contain a gem keyword (sapphire/ruby/etc.)
				// AND the word "bolts". Group them after plain finished bolts.
				if (n.contains("sapphire") || n.contains("emerald")
					|| n.contains("ruby") || n.contains("diamond")
					|| n.contains("dragonstone") || n.contains("onyx")
					|| n.contains("opal") || n.contains("jade")
					|| n.contains("pearl") || n.contains("topaz"))
				{
					return 3;
				}
				return 2;
			case "Darts":
				if (n.contains("dart tip")) return 0;
				return 1;
			case "Javelins":
				if (n.contains("javelin head") || n.contains("javelin tip")
					|| n.contains("javelin shaft")) return 0;
				return 1;
			default:
				return 0;
		}
	}

	/** Brief #75 (revised): fletching within-section comparator.
	 *
	 *  Bows: primary = wood tier ASC, secondary = sub-rank
	 *    (shortbow-u → shortbow → longbow-u → longbow), tertiary = name.
	 *  Crossbows: primary = tier ASC (specials at the end via tier sentinel),
	 *    secondary = sub-rank (stock → unstrung xbow → finished), name.
	 *  Arrows: primary = sub-rank (shaft → headless → finished), then within
	 *    finished by tier ASC, name.
	 *  Darts / Javelins: primary = tier ASC, secondary = sub-rank
	 *    (tip → finished / head → finished), name.
	 *  Bolts: primary = sub-rank (tip → unf → finished → gem-tipped),
	 *    secondary = tier ASC within sub-rank, name.
	 *  Feathers: primary = sub-rank (feathers → bowstring → magic string →
	 *    crossbow string), name.
	 *  Tools / Logs / Arrowheads / Misc: tier ASC, name.
	 */
	private int compareFletching(int a, int b, String section)
	{
		String na = nameOf(a);
		String nb = nameOf(b);

		int sra = fletchingSubRank(na, section);
		int srb = fletchingSubRank(nb, section);
		int ta = fletchingTier(na);
		int tb = fletchingTier(nb);

		// Bows + Crossbows + Darts + Javelins: tier-first within section.
		boolean tierFirst = "Bows".equals(section)
			|| "Crossbows".equals(section)
			|| "Darts".equals(section)
			|| "Javelins".equals(section);

		if (tierFirst)
		{
			if (ta != tb) return Integer.compare(ta, tb);
			if (sra != srb) return Integer.compare(sra, srb);
			return na.compareToIgnoreCase(nb);
		}

		// Arrows + Bolts + Feathers: sub-rank-first (shaft/tip/feather before
		// finished etc.), then tier ASC within sub-rank.
		boolean subRankFirst = "Arrows".equals(section)
			|| "Bolts".equals(section)
			|| "Feathers".equals(section);

		if (subRankFirst)
		{
			if (sra != srb) return Integer.compare(sra, srb);
			if (ta != tb) return Integer.compare(ta, tb);
			return na.compareToIgnoreCase(nb);
		}

		// Tools (3 items, fixed order via sub-rank), Logs / Arrowheads /
		// Misc fletching: sub-rank then tier ASC then name.
		if (sra != srb) return Integer.compare(sra, srb);
		if (ta != tb) return Integer.compare(ta, tb);
		return na.compareToIgnoreCase(nb);
	}

	// ── Brief #75: cosmetics set-then-slot sort ───────────────────────────

	private static final Map<String, Integer> SLOT_ORDER_COSMETICS = Map.ofEntries(
		Map.entry("head", 1),
		Map.entry("body", 2),
		Map.entry("legs", 3),
		Map.entry("hands", 4),
		Map.entry("feet", 5),
		Map.entry("cape", 6),
		Map.entry("neck", 7),
		Map.entry("ring", 8),
		Map.entry("weapon", 9),
		Map.entry("2h", 9),
		Map.entry("shield", 10),
		Map.entry("ammo", 11)
	);

	/** Treasure-trail clue-tier rank per set_name. Lower = earlier (easy
	 *  clues first → master clues last). Sets that don't appear here get a
	 *  default rank so they sort after the explicit list. */
	private static final Map<String, Integer> TREASURE_SET_TIER = Map.ofEntries(
		Map.entry("Explorer", 1),
		Map.entry("Black (g)", 2),  Map.entry("Black (t)", 2),
		Map.entry("Adamant (g)", 3), Map.entry("Adamant (t)", 3),
		Map.entry("Heraldic 1", 4),  Map.entry("Heraldic 2", 4),
		Map.entry("Heraldic 3", 4),  Map.entry("Heraldic 4", 4),
		Map.entry("Heraldic 5", 4),
		Map.entry("Rune (g)", 5),    Map.entry("Rune (t)", 5),
		Map.entry("Elegant", 5),
		Map.entry("Elegant black", 5), Map.entry("Elegant white", 5),
		Map.entry("Elegant red", 5), Map.entry("Elegant purple", 5),
		Map.entry("Elegant pink", 5), Map.entry("Elegant green", 5),
		Map.entry("Elegant gold", 5), Map.entry("Elegant blue", 5),
		Map.entry("Musketeer", 5),
		Map.entry("Gilded", 6),
		Map.entry("3rd age melee", 7),
		Map.entry("3rd age range", 7),
		Map.entry("3rd age mage", 7),
		Map.entry("3rd age druidic", 7)
	);

	private static final Map<String, Integer> HOLIDAY_SET_TIER = Map.ofEntries(
		Map.entry("Christmas", 1),
		Map.entry("Halloween", 2),
		Map.entry("Easter", 3),
		Map.entry("Midsummer", 4),
		Map.entry("Diwali", 5),
		Map.entry("Thanksgiving", 6),
		Map.entry("Valentine", 7),
		Map.entry("Festive", 8)
	);

	private static int cosmeticsSlotRank(String slot)
	{
		if (slot == null) return 99;
		Integer r = SLOT_ORDER_COSMETICS.get(slot.toLowerCase(Locale.ROOT));
		return r != null ? r : 99;
	}

	/** Set-rank for a (section, set_name) pair. Used as the primary cosmetics
	 *  sort key so items in the same set cluster contiguously. Items with
	 *  set_name == null get a sentinel rank that places them after all
	 *  recognised sets. */
	private static int cosmeticsSetRank(String section, String setName)
	{
		if (setName == null) return 9999;
		if ("Treasure trail sets".equals(section))
		{
			Integer r = TREASURE_SET_TIER.get(setName);
			return r != null ? r : 50;
		}
		if ("Holiday items".equals(section))
		{
			Integer r = HOLIDAY_SET_TIER.get(setName);
			return r != null ? r : 50;
		}
		// Minigame / Random event / Miscellaneous: alpha by set name
		// (the secondary key in compareCosmetics handles that).
		return 0;
	}

	/** Brief #75: cosmetics comparator. Primary key is the set's clue tier
	 *  / holiday rank, secondary is set_name alphabetical (groups by set),
	 *  tertiary is slot rank (head → body → legs → hands → feet → cape →
	 *  neck → ring → weapon → shield), final tiebreaker is name. Items
	 *  with no set_name sort after items with a set, in alpha order. */
	private int compareCosmetics(int a, int b, String section)
	{
		Map<Integer, ItemMeta> meta = SkillBankSortData.itemMeta();
		ItemMeta ma = meta.get(a);
		ItemMeta mb = meta.get(b);
		String sna = ma != null ? ma.setName : null;
		String snb = mb != null ? mb.setName : null;

		int ra = cosmeticsSetRank(section, sna);
		int rb = cosmeticsSetRank(section, snb);
		if (ra != rb)
		{
			return Integer.compare(ra, rb);
		}

		String keyA = sna != null ? sna : "";
		String keyB = snb != null ? snb : "";
		int byName = keyA.compareToIgnoreCase(keyB);
		if (byName != 0)
		{
			return byName;
		}

		int slA = cosmeticsSlotRank(ma != null ? ma.slot : null);
		int slB = cosmeticsSlotRank(mb != null ? mb.slot : null);
		if (slA != slB)
		{
			return Integer.compare(slA, slB);
		}
		return nameOf(a).compareToIgnoreCase(nameOf(b));
	}
}
