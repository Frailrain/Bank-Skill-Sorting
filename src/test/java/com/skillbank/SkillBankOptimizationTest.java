package com.skillbank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.runelite.client.plugins.banktags.tabs.Layout;
import org.junit.Assert;
import org.junit.Test;

public class SkillBankOptimizationTest
{
	@Test
	public void testDataTagsNotEmpty()
	{
		Assert.assertFalse(SkillBankData.tags().isEmpty());
		Assert.assertTrue(SkillBankData.tags().containsKey(SkillBankData.TAG_MELEE));
		Assert.assertTrue(SkillBankData.tags().containsKey(SkillBankData.TAG_COOKING));
		Assert.assertTrue(SkillBankData.tags().containsKey(SkillBankData.TAG_WOODCUTTING_FIREMAKING));
	}

	@Test
	public void testItemsForTab()
	{
		List<Integer> meleeItems = SkillBankData.itemsFor(SkillBankData.TAG_MELEE);
		Assert.assertNotNull(meleeItems);
		Assert.assertFalse(meleeItems.isEmpty());

		List<Integer> cookingItems = SkillBankData.itemsFor(SkillBankData.TAG_COOKING);
		Assert.assertNotNull(cookingItems);
		Assert.assertFalse(cookingItems.isEmpty());
	}

	@Test
	public void testLayoutPositioning()
	{
		Layout layout = new Layout("melee (auto)");
		layout.setItemAtPos(4151, 0); // Abyssal whip
		layout.setItemAtPos(11802, 1); // Armadyl godsword

		int[] arr = layout.getLayout();
		Assert.assertEquals(4151, arr[0]);
		Assert.assertEquals(11802, arr[1]);
	}
}
