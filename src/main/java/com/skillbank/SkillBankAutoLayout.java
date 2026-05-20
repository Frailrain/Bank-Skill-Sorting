package com.skillbank;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.banktags.tabs.AutoLayout;
import net.runelite.client.plugins.banktags.tabs.Layout;

/**
 * Auto-layout that positions items in each Skill Bank tag tab in the order
 * declared by {@link SkillBankData}. The Python pipeline
 * (tools/skillbank-data/sort_tables.py) already sorts items into
 * sections / by tier / by skill level when it generates SkillBankData.java,
 * so this class just consumes that order and fills grid positions
 * row-major (8 items per row, matching Bank Tags' standard width).
 * <p>
 * Only items the player currently has in their bank are positioned, to
 * keep the layout compact. Items in the tab but not in the bank are
 * skipped — running the auto-layout again after acquiring new items
 * will position them.
 */
@Slf4j
@Singleton
public class SkillBankAutoLayout implements AutoLayout
{
	private final Client client;
	private final ItemManager itemManager;

	@Inject
	SkillBankAutoLayout(Client client, ItemManager itemManager)
	{
		this.client = client;
		this.itemManager = itemManager;
	}

	@Override
	public Layout generateLayout(Layout previous)
	{
		String tag = previous != null ? previous.getTag() : null;
		if (tag == null || tag.isEmpty())
		{
			return previous;
		}

		List<Integer> sorted = SkillBankData.itemsFor(tag);
		if (sorted == null || sorted.isEmpty())
		{
			// This tag isn't one of ours — return previous unchanged so the
			// user keeps whatever they had.
			return previous;
		}

		Set<Integer> ownedCanonical = collectOwnedCanonicalIds();

		Layout out = new Layout(tag);
		int pos = 0;
		for (Integer iid : sorted)
		{
			if (iid == null || iid < 0)
			{
				continue;
			}
			int canonical = itemManager.canonicalize(iid);
			if (!ownedCanonical.contains(canonical))
			{
				continue;
			}
			out.setItemAtPos(canonical, pos++);
		}
		log.debug("SkillBankAutoLayout: laid out {} items for tag '{}' ({} candidates)",
			pos, tag, sorted.size());
		return out;
	}

	/**
	 * Collect canonical item IDs the player currently has in their bank, so
	 * we can filter the SkillBankData list down to what should actually be
	 * laid out. canonicalize() merges placeholders / variants to a single id
	 * — same mapping LayoutManager uses on the other side when matching the
	 * layout to bank slots.
	 */
	private Set<Integer> collectOwnedCanonicalIds()
	{
		Set<Integer> out = new HashSet<>();
		ItemContainer bank = client.getItemContainer(InventoryID.BANK);
		if (bank == null)
		{
			return out;
		}
		Item[] items = bank.getItems();
		for (Item it : items)
		{
			if (it == null)
			{
				continue;
			}
			int id = it.getId();
			if (id <= 0)
			{
				continue;
			}
			out.add(itemManager.canonicalize(id));
		}
		return out;
	}
}
