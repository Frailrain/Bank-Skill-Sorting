package com.skillbank;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

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
		keyName = "sectionDividers",
		name = "Section dividers",
		description = "Add a labelled divider row above each section in every tab. "
			+ "Turning this off restores the compact layout (sections separated by row breaks only).",
		section = behaviorSection,
		position = 3
	)
	default boolean sectionDividers()
	{
		return true;
	}

	@ConfigItem(
		keyName = "dynamicSlayerTab",
		name = "Dynamic slayer tab",
		description = "Rebuild the slayer tab for your current slayer task: required and protection items, "
			+ "your strongest owned gear for the task's recommended style, potions, food, and cannon supplies. "
			+ "Falls back to the static slayer tab when you have no task.",
		section = behaviorSection,
		position = 10
	)
	default boolean dynamicSlayerTab()
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

	@Range(min = 1, max = 6)
	@ConfigItem(
		keyName = "zoneTierCount",
		name = "Loadout tier count",
		description = "How many unique equipment tiers to show in the loadout zone "
			+ "at the top of combat tabs. The plugin looks at every requirement level on items "
			+ "you own per armour slot / weapon class, sorts descending, and takes the top N. "
			+ "Higher values show more items. Default 3.",
		section = behaviorSection,
		position = 5
	)
	default int zoneTierCount()
	{
		return 3;
	}

	// First-run setup-check flags. Hidden from the user-visible settings
	// panel; the plugin manages them automatically based on dependency
	// state and the number of times the check has fired.

	@ConfigItem(
		keyName = "welcomeShown",
		name = "",
		description = "",
		hidden = true
	)
	default boolean welcomeShown()
	{
		return false;
	}

	@ConfigItem(
		keyName = "setupCheckDismissed",
		name = "",
		description = "",
		hidden = true
	)
	default boolean setupCheckDismissed()
	{
		return false;
	}

	@ConfigItem(
		keyName = "setupCheckCount",
		name = "",
		description = "",
		hidden = true
	)
	default int setupCheckCount()
	{
		return 0;
	}

	// Brief #89: per-tab collision decisions, persisted so the plugin does
	// not re-prompt every login. All three are comma-separated lists of
	// standardized base tag names. Hidden — managed by the collision dialog
	// and the seeding path, never edited directly by the user.

	/** Base tag names the user chose to keep (Skip). Never seeded, updated,
	 *  or refreshed by the plugin. */
	@ConfigItem(
		keyName = "skippedTabs",
		name = "",
		description = "",
		hidden = true
	)
	default String skippedTabs()
	{
		return "";
	}

	/** Base tag names seeded alongside the user's tab under a "(Auto)"
	 *  suffix. The user's same-named tab is left untouched; the plugin
	 *  manages the suffixed variant. */
	@ConfigItem(
		keyName = "renamedTabs",
		name = "",
		description = "",
		hidden = true
	)
	default String renamedTabs()
	{
		return "";
	}

	/** Base tag names the plugin owns and refreshes normally (seeded with no
	 *  collision, or chosen Overwrite). Lets the plugin tell its own tabs
	 *  apart from a user's hand-built same-named tab on later logins. */
	@ConfigItem(
		keyName = "managedTabs",
		name = "",
		description = "",
		hidden = true
	)
	default String managedTabs()
	{
		return "";
	}

	/** Brief #90: latches true once this install has been placed on the new
	 *  title-case naming scheme — via fresh-install seeding OR the one-time
	 *  legacy rename migration. Gates both so neither runs twice. */
	@ConfigItem(
		keyName = "namingMigrated",
		name = "",
		description = "",
		hidden = true
	)
	default boolean namingMigrated()
	{
		return false;
	}

	/** Brief #90: which naming this install's plugin tabs use. true = fresh
	 *  install, tabs seeded with the "(Auto)" suffix ("Herblore (Auto)").
	 *  false = legacy install migrated to bare title case ("Herblore"), which
	 *  it already owned. Selects the primary seed/refresh name per tab. */
	@ConfigItem(
		keyName = "autoNaming",
		name = "",
		description = "",
		hidden = true
	)
	default boolean autoNaming()
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
		position = 5
	)
	default boolean tabCooking()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabWoodcuttingFiremaking",
		name = "Woodcutting + Firemaking",
		description = "Seed the woodcutting/firemaking tag (logs, axes, tinderbox, pyre logs, shade items, Wintertodt, lumberjack/pyromancer outfits)",
		section = tabsSection,
		position = 6
	)
	default boolean tabWoodcuttingFiremaking()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabFletching",
		name = "Fletching",
		description = "Seed the fletching tag (bow strings, unstrung/strung bows, arrows, bolts, darts, javelins, fletching cape)",
		section = tabsSection,
		position = 7
	)
	default boolean tabFletching()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabFishing",
		name = "fishing",
		description = "Seed the fishing tag (raw fish, rods, bait, angler outfit, heron)",
		section = tabsSection,
		position = 8
	)
	default boolean tabFishing()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabCrafting",
		name = "crafting",
		description = "Seed the crafting tag (gems, moulds, leather, dragonhide, glass)",
		section = tabsSection,
		position = 9
	)
	default boolean tabCrafting()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabMiningSmithing",
		name = "Mining + Smithing",
		description = "Seed the mining/smithing tag (ores, bars, pickaxes, prospector, foundry)",
		section = tabsSection,
		position = 10
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
		position = 11
	)
	default boolean tabHerblore()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabAgilityThieving",
		name = "Agility + Thieving",
		description = "Seed the agility/thieving tag (graceful, marks, rogue set, blackjacks)",
		section = tabsSection,
		position = 12
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
		position = 13
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
		position = 14
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
		position = 15
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
		position = 16
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
		position = 17
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
		position = 18
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
		position = 19
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
		position = 20
	)
	default boolean tabSailing()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tabCosmetics",
		name = "cosmetics",
		description = "Seed the cosmetics tag (Treasure trail/holiday cosmetic outfits, ornament kits, purely-visual items)",
		section = tabsSection,
		position = 21
	)
	default boolean tabCosmetics()
	{
		return true;
	}

	// Brief #74 fix #15: Teleports tab promoted to position 3 (after mage).
	@ConfigItem(
		keyName = "tabTeleports",
		name = "teleports",
		description = "Seed the teleports tag (teleport tablets, jewellery teleports, ectophial, royal seed pod, etc.)",
		section = tabsSection,
		position = 4
	)
	default boolean tabTeleports()
	{
		return true;
	}

	// Brief #66: per-combat-tab simple-vs-expanded layout toggles. When on,
	// the tab's Head / Body / Legs / Hands / Feet sections collapse into a
	// single "Armor" section.

	@ConfigItem(
		keyName = "simpleMelee",
		name = "Simple melee layout",
		description = "Collapse Head/Body/Legs/Hands/Feet into one Armor section in the melee tab",
		section = tabsSection,
		position = 23
	)
	default boolean simpleMelee()
	{
		return false;
	}

	@ConfigItem(
		keyName = "simpleRange",
		name = "Simple range layout",
		description = "Collapse Head/Body/Legs/Hands/Feet into one Armor section in the range tab",
		section = tabsSection,
		position = 24
	)
	default boolean simpleRange()
	{
		return false;
	}

	@ConfigItem(
		keyName = "simpleMage",
		name = "Simple mage layout",
		description = "Collapse Head/Body/Legs/Hands/Feet into one Armor section in the mage tab",
		section = tabsSection,
		position = 25
	)
	default boolean simpleMage()
	{
		return false;
	}
}
