package com.skillbank;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(SkillBankConfig.GROUP)
public interface SkillBankConfig extends Config
{
	String GROUP = "skillbank";

	@ConfigSection(
		name = "Tabs to seed",
		description = "Enable or disable individual skill tabs",
		position = 0
	)
	String tabsSection = "tabsSection";

	@ConfigSection(
		name = "Behavior",
		description = "How the plugin seeds tags",
		position = 1
	)
	String behaviorSection = "behaviorSection";

	@ConfigItem(
		keyName = "seedOnStartup",
		name = "Seed on startup",
		description = "Automatically seed missing skill tags when the plugin starts",
		section = behaviorSection,
		position = 0
	)
	default boolean seedOnStartup()
	{
		return true;
	}

	@ConfigItem(
		keyName = "announceInChat",
		name = "Announce in chat",
		description = "Post a chat message summarising seed results",
		section = behaviorSection,
		position = 1
	)
	default boolean announceInChat()
	{
		return true;
	}

	@ConfigItem(
		keyName = "reseedMissing",
		name = "Reseed missing tags",
		description = "Enable to re-run seeding for any skill tags that are currently missing. Auto-disables.",
		section = behaviorSection,
		position = 2
	)
	default boolean reseedMissing()
	{
		return false;
	}

	@ConfigItem(
		keyName = "resetConfirm",
		name = "Confirm reset",
		description = "Must be enabled before 'Reset all tags' will do anything. Auto-disables after a reset.",
		section = behaviorSection,
		position = 3
	)
	default boolean resetConfirm()
	{
		return false;
	}

	@ConfigItem(
		keyName = "resetAll",
		name = "Reset all tags",
		description = "Delete all skill tags created by this plugin. Requires 'Confirm reset' to be enabled. Auto-disables.",
		section = behaviorSection,
		position = 4
	)
	default boolean resetAll()
	{
		return false;
	}

	@ConfigItem(
		keyName = "tabMelee",
		name = "melee",
		description = "Seed the melee tag (weapons, armour, combat potions, HP food)",
		section = tabsSection,
		position = 0
	)
	default boolean tabMelee()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabRange",
		name = "range",
		description = "Seed the range tag (bows, crossbows, ammo, d'hide, blowpipe)",
		section = tabsSection,
		position = 1
	)
	default boolean tabRange()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabMage",
		name = "mage",
		description = "Seed the mage tag (runes, staves, magic armour, rune pouch)",
		section = tabsSection,
		position = 2
	)
	default boolean tabMage()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabPrayer",
		name = "prayer",
		description = "Seed the prayer tag (bones, ashes, prayer potions)",
		section = tabsSection,
		position = 3
	)
	default boolean tabPrayer()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabCooking",
		name = "cooking",
		description = "Seed the cooking tag (cooked food, ingredients, cook's outfit)",
		section = tabsSection,
		position = 4
	)
	default boolean tabCooking()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabWcFletching",
		name = "wc_fletching",
		description = "Seed the woodcutting/fletching tag (logs, axes, bows, bolts, darts)",
		section = tabsSection,
		position = 5
	)
	default boolean tabWcFletching()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabFishing",
		name = "fishing",
		description = "Seed the fishing tag (raw fish, rods, bait, angler outfit, heron)",
		section = tabsSection,
		position = 6
	)
	default boolean tabFishing()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabFiremaking",
		name = "firemaking",
		description = "Seed the firemaking tag (tinderbox, pyromancer outfit, firelighters)",
		section = tabsSection,
		position = 7
	)
	default boolean tabFiremaking()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabCrafting",
		name = "crafting",
		description = "Seed the crafting tag (gems, moulds, leather, dragonhide, glass)",
		section = tabsSection,
		position = 8
	)
	default boolean tabCrafting()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabMiningSmithing",
		name = "mining_smithing",
		description = "Seed the mining/smithing tag (ores, bars, pickaxes, prospector, foundry)",
		section = tabsSection,
		position = 9
	)
	default boolean tabMiningSmithing()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabHerblore",
		name = "herblore",
		description = "Seed the herblore tag (herbs, secondaries, all potion doses)",
		section = tabsSection,
		position = 10
	)
	default boolean tabHerblore()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabAgilityThieving",
		name = "agility_thieving",
		description = "Seed the agility/thieving tag (graceful, marks, rogue set, blackjacks)",
		section = tabsSection,
		position = 11
	)
	default boolean tabAgilityThieving()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabSlayer",
		name = "slayer",
		description = "Seed the slayer tag (task gear, slayer helms, enchanted gems, rings)",
		section = tabsSection,
		position = 12
	)
	default boolean tabSlayer()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabFarming",
		name = "farming",
		description = "Seed the farming tag (seeds, tools, compost, farmer outfit)",
		section = tabsSection,
		position = 13
	)
	default boolean tabFarming()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabRunecraft",
		name = "runecraft",
		description = "Seed the runecraft tag (essences, talismans, tiaras, pouches, wicked hood)",
		section = tabsSection,
		position = 14
	)
	default boolean tabRunecraft()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabHunter",
		name = "hunter",
		description = "Seed the hunter tag (traps, impling jars, camo outfits, salamanders)",
		section = tabsSection,
		position = 15
	)
	default boolean tabHunter()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabConstruction",
		name = "construction",
		description = "Seed the construction tag (planks, nails, saw, limestone, stones)",
		section = tabsSection,
		position = 16
	)
	default boolean tabConstruction()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabMisc",
		name = "misc",
		description = "Seed the misc tag (teleports, keys, clue scrolls, storage bags, utility)",
		section = tabsSection,
		position = 17
	)
	default boolean tabMisc()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabQuests",
		name = "quests",
		description = "Seed the quests tag (quest rewards, diary gear, minigame gear, boss uniques)",
		section = tabsSection,
		position = 18
	)
	default boolean tabQuests()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabSailing",
		name = "sailing",
		description = "Seed the sailing tag (Sailing skill — fish, navigation tools, ship parts, cape)",
		section = tabsSection,
		position = 19
	)
	default boolean tabSailing()
	{
		return true;
	}
}
