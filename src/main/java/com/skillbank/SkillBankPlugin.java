package com.skillbank;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Skill Bank Tabs",
	description = "Seeds Bank Tags tag groups with predefined skill-based item lists",
	tags = {"bank", "tags", "skilling", "organization"}
)
public class SkillBankPlugin extends Plugin
{
	static final String BANKTAGS_GROUP = "banktags";
	static final String TAGGED_ITEMS_PREFIX = "taggedItems_";

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

	private SkillBankPanel panel;
	private NavigationButton navButton;

	@Provides
	SkillBankConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SkillBankConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
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

		if (config.seedOnStartup())
		{
			// Run on the client thread so chat messages can be posted once logged in.
			SeedResult result = seedMissing();
			announce(result);
			SwingUtilities.invokeLater(() -> panel.setStatus(result.summary()));
		}
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
					SeedResult result = seedMissing();
					announce(result);
					if (panel != null)
					{
						SwingUtilities.invokeLater(() -> panel.setStatus(result.summary()));
					}
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
						int cleared = resetAll();
						postChat("Skill Bank: cleared " + cleared + " tag(s). Reseed via 'Reseed missing tags'.");
						if (panel != null)
						{
							final int c = cleared;
							SwingUtilities.invokeLater(() -> panel.setStatus("Cleared " + c + " tag(s)."));
						}
						configManager.setConfiguration(SkillBankConfig.GROUP, "resetConfirm", false);
					}
					configManager.setConfiguration(SkillBankConfig.GROUP, "resetAll", false);
				}
				break;
			default:
				break;
		}
	}

	/** Seed every enabled tag that does not yet exist. */
	SeedResult seedMissing()
	{
		Map<String, List<Integer>> tags = SkillBankData.tags();
		List<String> seeded = new ArrayList<>();
		List<String> skippedExisting = new ArrayList<>();
		List<String> skippedDisabled = new ArrayList<>();

		for (Map.Entry<String, List<Integer>> entry : tags.entrySet())
		{
			String tagName = entry.getKey();
			if (!isTabEnabled(tagName))
			{
				skippedDisabled.add(tagName);
				continue;
			}

			String key = TAGGED_ITEMS_PREFIX + tagName;
			String existing = configManager.getConfiguration(BANKTAGS_GROUP, key);
			if (existing != null && !existing.isEmpty())
			{
				skippedExisting.add(tagName);
				continue;
			}

			String value = entry.getValue().stream()
				.distinct()
				.map(String::valueOf)
				.collect(Collectors.joining(","));
			configManager.setConfiguration(BANKTAGS_GROUP, key, value);
			seeded.add(tagName);
		}

		return new SeedResult(seeded, skippedExisting, skippedDisabled);
	}

	/** Delete every skill tag this plugin knows about. Returns how many were cleared. */
	int resetAll()
	{
		int cleared = 0;
		for (String tagName : SkillBankData.tags().keySet())
		{
			String key = TAGGED_ITEMS_PREFIX + tagName;
			if (configManager.getConfiguration(BANKTAGS_GROUP, key) != null)
			{
				configManager.unsetConfiguration(BANKTAGS_GROUP, key);
				cleared++;
			}
		}
		return cleared;
	}

	/** Describe current on-disk state for the panel. */
	Map<String, Boolean> currentTagPresence()
	{
		Map<String, Boolean> out = new LinkedHashMap<>();
		for (String tagName : SkillBankData.tags().keySet())
		{
			String value = configManager.getConfiguration(BANKTAGS_GROUP, TAGGED_ITEMS_PREFIX + tagName);
			out.put(tagName, value != null && !value.isEmpty());
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

	private void announce(SeedResult result)
	{
		if (!config.announceInChat())
		{
			return;
		}
		postChat("Skill Bank: " + result.summary() + " Reopen your bank to see the new tabs.");
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
		final List<String> seeded;
		final List<String> skippedExisting;
		final List<String> skippedDisabled;

		SeedResult(List<String> seeded, List<String> skippedExisting, List<String> skippedDisabled)
		{
			this.seeded = seeded;
			this.skippedExisting = skippedExisting;
			this.skippedDisabled = skippedDisabled;
		}

		String summary()
		{
			return "seeded " + seeded.size()
				+ ", kept " + skippedExisting.size()
				+ ", disabled " + skippedDisabled.size() + ".";
		}
	}
}
