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
				partitionTopTiers(items, section, tagName, sec1, sec2);
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
		// Zone 1 first, in section order.
		for (SectionResult r : results)
		{
			if (r.zone1.isEmpty()) continue;
			if (!firstBlock && pos % ITEMS_PER_ROW != 0)
			{
				pos = ((pos / ITEMS_PER_ROW) + 1) * ITEMS_PER_ROW;
			}
			if (trace != null) trace.section(r.section, 1, r.zone1.size(), r.sortMethod);
			for (Integer iid : r.zone1)
			{
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

	private int compareGems(int a, int b)
	{
		return Integer.compare(gemRank(nameOf(a)), gemRank(nameOf(b)));
	}

	private int gemRank(String name)
	{
		String n = name.toLowerCase(Locale.ROOT);
		for (Map.Entry<String, Integer> e : gemOrderIndex.entrySet())
		{
			if (n.contains(e.getKey()))
			{
				return e.getValue();
			}
		}
		return Integer.MAX_VALUE;
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

	private int compareByTierDescThenName(int a, int b)
	{
		Map<Integer, ItemMeta> meta = SkillBankSortData.itemMeta();
		ItemMeta ma = meta.get(a);
		ItemMeta mb = meta.get(b);
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
}
