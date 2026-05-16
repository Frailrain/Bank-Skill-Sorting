package com.skillbank;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.banktags.BankTagsPlugin;
import net.runelite.client.plugins.banktags.TagManager;
import net.runelite.client.plugins.banktags.tabs.TabInterface;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Skill Bank Tabs",
	description = "Seeds Bank Tags tag groups with predefined skill-based item lists",
	tags = {"bank", "tags", "skilling", "organization"}
)
@PluginDependency(BankTagsPlugin.class)
public class SkillBankPlugin extends Plugin
{
	static final String BANKTAGS_GROUP = "banktags";
	static final String ITEM_PREFIX = "item_";
	static final String ICON_PREFIX = "icon_";
	static final String TAG_TABS_KEY = "tagtabs";
	static final String LEGACY_TAGGED_ITEMS_PREFIX = "taggedItems_";

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private SkillBankConfig config;

	@Inject
	private TagManager tagManager;

	@Inject
	private TabInterface tabInterface;

	private SkillBankPanel panel;
	private NavigationButton navButton;
	private boolean seedAttempted;

	@Provides
	SkillBankConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SkillBankConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		seedAttempted = false;
		panel = new SkillBankPanel(this);

		BufferedImage icon;
		try
		{
			icon = ImageUtil.loadImageResource(getClass(), "/com/skillbank/icon.png");
		}
		catch (Exception e)
		{
			icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		}

		navButton = NavigationButton.builder()
			.tooltip("Skill Bank Tabs")
			.icon(icon)
			.priority(7)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);

		cleanupLegacyTaggedItemsKeys();
	}

	@Override
	protected void shutDown() throws Exception
	{
		if (navButton != null)
		{
			clientToolbar.removeNavigation(navButton);
			navButton = null;
		}
		panel = null;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!SkillBankConfig.GROUP.equals(event.getGroup()))
		{
			return;
		}

		switch (event.getKey())
		{
			case "reseedMissing":
				if (config.reseedMissing())
				{
					seedMissing(result ->
					{
						announce(result);
						if (panel != null)
						{
							SwingUtilities.invokeLater(() -> panel.setStatus(result.summary()));
						}
					});
					configManager.setConfiguration(SkillBankConfig.GROUP, "reseedMissing", false);
				}
				break;
			case "resetAll":
				if (config.resetAll())
				{
					if (!config.resetConfirm())
					{
						postChat("Skill Bank: enable 'Confirm reset' before resetting tags.");
						if (panel != null)
						{
							SwingUtilities.invokeLater(() -> panel.setStatus("Reset blocked: confirm first."));
						}
					}
					else
					{
						resetAll(cleared ->
						{
							postChat("Skill Bank: removed " + cleared + " skill tag(s). Reseed via 'Reseed missing tags'.");
							if (panel != null)
							{
								SwingUtilities.invokeLater(() -> panel.setStatus("Removed " + cleared + " skill tag(s)."));
							}
							configManager.setConfiguration(SkillBankConfig.GROUP, "resetConfirm", false);
						});
					}
					configManager.setConfiguration(SkillBankConfig.GROUP, "resetAll", false);
				}
				break;
			default:
				break;
		}
	}

	/**
	 * Defer seed-on-startup until the OSRS item cache is populated. Running it
	 * from {@link #startUp()} called {@link TagManager#addTag} before the cache
	 * was ready and threw {@link NullPointerException} from inside Bank Tags'
	 * canonicalization. The first state transition into LOGIN_SCREEN or
	 * LOGGED_IN is the earliest safe trigger; we latch so it fires once.
	 */
	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (seedAttempted)
		{
			return;
		}
		GameState state = event.getGameState();
		if (state != GameState.LOGIN_SCREEN && state != GameState.LOGGED_IN)
		{
			return;
		}
		seedAttempted = true;
		if (!config.seedOnStartup())
		{
			return;
		}
		seedMissing(result ->
		{
			announce(result);
			if (panel != null)
			{
				SwingUtilities.invokeLater(() -> panel.setStatus(result.summary()));
			}
		});
	}

	/**
	 * Schedule a seed pass on the client thread, then deliver the result to the
	 * given callback (also invoked on the client thread). The underlying work
	 * calls {@link TagManager} methods which require the client thread.
	 */
	void seedMissing(Consumer<SeedResult> callback)
	{
		clientThread.invokeLater(() -> callback.accept(doSeedMissing()));
	}

	/**
	 * Seed every enabled skill tab. For each item ID in each enabled tag bucket,
	 * adds our tag via {@link TagManager#addTag(int, String, boolean)} (idempotent
	 * — Bank Tags appends to the existing CSV without duplicating). Also writes
	 * the tab icon and registers the tag name in {@code banktags.tagtabs} so the
	 * tab appears in the bank UI strip. Must run on the client thread.
	 */
	private SeedResult doSeedMissing()
	{
		Map<String, List<Integer>> tags = SkillBankData.tags();
		int itemsTagged = 0;
		int itemsAlready = 0;
		List<String> tagsSeeded = new ArrayList<>();
		List<String> tagsDisabled = new ArrayList<>();

		Set<String> tabsCsv = readTagTabs();
		Set<String> tabsCsvUpdated = new LinkedHashSet<>(tabsCsv);

		for (Map.Entry<String, List<Integer>> entry : tags.entrySet())
		{
			String tagName = entry.getKey();
			if (!isTabEnabled(tagName))
			{
				tagsDisabled.add(tagName);
				continue;
			}

			Set<Integer> alreadyTagged = new HashSet<>(tagManager.getItemsForTag(tagName));
			int newForThisTag = 0;
			for (Integer itemId : entry.getValue())
			{
				if (alreadyTagged.contains(itemId))
				{
					itemsAlready++;
					continue;
				}
				tagManager.addTag(itemId, tagName, false);
				itemsTagged++;
				newForThisTag++;
			}
			if (newForThisTag > 0)
			{
				tagsSeeded.add(tagName);
			}

			String iconKey = ICON_PREFIX + tagName;
			if (configManager.getConfiguration(BANKTAGS_GROUP, iconKey) == null)
			{
				int iconId = SkillBankData.iconFor(tagName);
				if (iconId > 0)
				{
					configManager.setConfiguration(BANKTAGS_GROUP, iconKey, String.valueOf(iconId));
				}
			}

			tabsCsvUpdated.add(tagName);
		}

		if (!tabsCsvUpdated.equals(tabsCsv))
		{
			configManager.setConfiguration(BANKTAGS_GROUP, TAG_TABS_KEY, String.join(",", tabsCsvUpdated));
		}

		tabInterface.reloadActiveTab();

		return new SeedResult(itemsTagged, itemsAlready, tagsSeeded, tagsDisabled);
	}

	/**
	 * Schedule a reset pass on the client thread, then deliver the cleared count
	 * to the given callback (also invoked on the client thread). The underlying
	 * work calls {@link TagManager} methods which require the client thread.
	 */
	void resetAll(Consumer<Integer> callback)
	{
		clientThread.invokeLater(() -> callback.accept(doResetAll()));
	}

	/**
	 * Remove every skill tag this plugin knows about from every item, leaving any
	 * co-tags from other sources intact. Also unsets our tab icons and removes
	 * our tag names from {@code banktags.tagtabs}. Returns the number of skill
	 * tag names whose tabs were cleared. Must run on the client thread.
	 */
	private int doResetAll()
	{
		Set<String> skillTags = SkillBankData.tags().keySet();
		int cleared = 0;
		for (String tagName : skillTags)
		{
			if (!tagManager.getItemsForTag(tagName).isEmpty())
			{
				tagManager.removeTag(tagName);
				cleared++;
			}
			configManager.unsetConfiguration(BANKTAGS_GROUP, ICON_PREFIX + tagName);
		}

		Set<String> tabsCsv = readTagTabs();
		Set<String> tabsCsvKept = new LinkedHashSet<>();
		for (String t : tabsCsv)
		{
			if (!skillTags.contains(t.toLowerCase()))
			{
				tabsCsvKept.add(t);
			}
		}
		if (tabsCsvKept.size() != tabsCsv.size())
		{
			if (tabsCsvKept.isEmpty())
			{
				configManager.unsetConfiguration(BANKTAGS_GROUP, TAG_TABS_KEY);
			}
			else
			{
				configManager.setConfiguration(BANKTAGS_GROUP, TAG_TABS_KEY, String.join(",", tabsCsvKept));
			}
		}

		tabInterface.reloadActiveTab();

		return cleared;
	}

	/**
	 * Schedule a tag-presence read on the client thread, then deliver the result
	 * to the given callback (also invoked on the client thread). Used by the
	 * panel to populate its indicator list without blocking the EDT.
	 */
	void requestTagPresence(Consumer<Map<String, Boolean>> callback)
	{
		clientThread.invokeLater(() -> callback.accept(currentTagPresence()));
	}

	/** All tag names this plugin manages, in canonical declaration order. */
	Set<String> knownTagNames()
	{
		return SkillBankData.tags().keySet();
	}

	/** Describe current on-disk state for the panel. Must run on the client thread. */
	private Map<String, Boolean> currentTagPresence()
	{
		Map<String, Boolean> out = new LinkedHashMap<>();
		for (String tagName : SkillBankData.tags().keySet())
		{
			out.put(tagName, !tagManager.getItemsForTag(tagName).isEmpty());
		}
		return out;
	}

	void triggerReseed()
	{
		configManager.setConfiguration(SkillBankConfig.GROUP, "reseedMissing", true);
	}

	void triggerReset()
	{
		configManager.setConfiguration(SkillBankConfig.GROUP, "resetAll", true);
	}

	void setResetConfirm(boolean value)
	{
		configManager.setConfiguration(SkillBankConfig.GROUP, "resetConfirm", value);
	}

	boolean isResetConfirmed()
	{
		return config.resetConfirm();
	}

	private boolean isTabEnabled(String tagName)
	{
		Map<String, BooleanSupplier> toggles = new LinkedHashMap<>();
		toggles.put(SkillBankData.TAG_MELEE, config::tabMelee);
		toggles.put(SkillBankData.TAG_RANGE, config::tabRange);
		toggles.put(SkillBankData.TAG_MAGE, config::tabMage);
		toggles.put(SkillBankData.TAG_PRAYER, config::tabPrayer);
		toggles.put(SkillBankData.TAG_COOKING, config::tabCooking);
		toggles.put(SkillBankData.TAG_WC_FLETCHING, config::tabWcFletching);
		toggles.put(SkillBankData.TAG_FISHING, config::tabFishing);
		toggles.put(SkillBankData.TAG_FIREMAKING, config::tabFiremaking);
		toggles.put(SkillBankData.TAG_CRAFTING, config::tabCrafting);
		toggles.put(SkillBankData.TAG_MINING_SMITHING, config::tabMiningSmithing);
		toggles.put(SkillBankData.TAG_HERBLORE, config::tabHerblore);
		toggles.put(SkillBankData.TAG_AGILITY_THIEVING, config::tabAgilityThieving);
		toggles.put(SkillBankData.TAG_SLAYER, config::tabSlayer);
		toggles.put(SkillBankData.TAG_FARMING, config::tabFarming);
		toggles.put(SkillBankData.TAG_RUNECRAFT, config::tabRunecraft);
		toggles.put(SkillBankData.TAG_HUNTER, config::tabHunter);
		toggles.put(SkillBankData.TAG_CONSTRUCTION, config::tabConstruction);
		toggles.put(SkillBankData.TAG_MISC, config::tabMisc);
		toggles.put(SkillBankData.TAG_QUESTS, config::tabQuests);
		toggles.put(SkillBankData.TAG_SAILING, config::tabSailing);
		BooleanSupplier s = toggles.get(tagName);
		return s != null && s.getAsBoolean();
	}

	private Set<String> readTagTabs()
	{
		String v = configManager.getConfiguration(BANKTAGS_GROUP, TAG_TABS_KEY);
		Set<String> out = new LinkedHashSet<>();
		if (v == null || v.isEmpty())
		{
			return out;
		}
		for (String part : v.split(","))
		{
			String trimmed = part.trim();
			if (!trimmed.isEmpty())
			{
				out.add(trimmed);
			}
		}
		return out;
	}

	/**
	 * Earlier versions of this plugin wrote {@code banktags.taggedItems_<name>}
	 * keys that Bank Tags never read. Sweep them out on startup so users moving
	 * from the broken format aren't left with dead config entries.
	 */
	private void cleanupLegacyTaggedItemsKeys()
	{
		List<String> keys = configManager.getConfigurationKeys(BANKTAGS_GROUP + "." + LEGACY_TAGGED_ITEMS_PREFIX);
		if (keys == null || keys.isEmpty())
		{
			return;
		}
		Set<String> skillTags = SkillBankData.tags().keySet();
		int removed = 0;
		for (String fullKey : keys)
		{
			String[] split = fullKey.split("\\.", 2);
			if (split.length < 2)
			{
				continue;
			}
			String shortKey = split[1];
			String suffix = shortKey.substring(LEGACY_TAGGED_ITEMS_PREFIX.length());
			if (skillTags.contains(suffix.toLowerCase()))
			{
				configManager.unsetConfiguration(BANKTAGS_GROUP, shortKey);
				removed++;
			}
		}
		if (removed > 0)
		{
			log.info("Cleaned up {} legacy banktags.taggedItems_* entries from a previous Skill Bank version", removed);
		}
	}

	private void announce(SeedResult result)
	{
		if (!config.announceInChat())
		{
			return;
		}
		postChat("Skill Bank: " + result.summary() + " Reopen your bank if tabs aren't visible yet.");
	}

	private void postChat(String message)
	{
		clientThread.invokeLater(() ->
		{
			if (client.getLocalPlayer() == null)
			{
				return;
			}
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
		});
	}

	static final class SeedResult
	{
		final int itemsTagged;
		final int itemsAlready;
		final List<String> tagsSeeded;
		final List<String> tagsDisabled;

		SeedResult(int itemsTagged, int itemsAlready, List<String> tagsSeeded, List<String> tagsDisabled)
		{
			this.itemsTagged = itemsTagged;
			this.itemsAlready = itemsAlready;
			this.tagsSeeded = tagsSeeded;
			this.tagsDisabled = tagsDisabled;
		}

		String summary()
		{
			return "tagged " + itemsTagged + " new item(s) across " + tagsSeeded.size() + " tab(s); "
				+ itemsAlready + " already tagged; " + tagsDisabled.size() + " disabled.";
		}
	}
}
