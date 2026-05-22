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
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.banktags.BankTagsPlugin;
import net.runelite.client.plugins.banktags.TagManager;
import net.runelite.client.plugins.banktags.tabs.Layout;
import net.runelite.client.plugins.banktags.tabs.LayoutManager;
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

	@Inject
	private LayoutManager layoutManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private SkillBankLayoutBuilder layoutBuilder;

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
					if (client.getGameState() != GameState.LOGGED_IN)
					{
						if (panel != null)
						{
							SwingUtilities.invokeLater(() -> panel.setStatus("Seeding requires being logged in."));
						}
					}
					else
					{
						seedMissing(result ->
						{
							announce(result);
							if (panel != null)
							{
								SwingUtilities.invokeLater(() -> panel.setStatus(result.summary()));
							}
						});
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
					else if (client.getGameState() != GameState.LOGGED_IN)
					{
						if (panel != null)
						{
							SwingUtilities.invokeLater(() -> panel.setStatus("Reset requires being logged in."));
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
	 * Defer all game-thread work until the player is fully logged in: the OSRS
	 * item cache isn't guaranteed populated before that point, and calling
	 * {@link TagManager} or {@link client#getItemDefinition} earlier throws
	 * {@link NullPointerException} from inside Bank Tags' canonicalization.
	 * On every transition into LOGGED_IN we refresh the panel's presence
	 * indicators; the seed-on-startup trigger fires at most once per session,
	 * gated on the config toggle.
	 */
	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		if (panel != null)
		{
			panel.refresh();
		}
		if (seedAttempted || !config.seedOnStartup())
		{
			return;
		}
		seedAttempted = true;
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
	 * Sync every enabled skill tab to match {@link SkillBankData} exactly:
	 * remove any items currently tagged but no longer in the bucket, then add
	 * any bucket items not yet tagged. Non-additive — {@code SkillBankData}
	 * is the sole source of truth, so legacy items left over from earlier
	 * heuristic builds are evicted. Disabled tabs are skipped entirely (the
	 * user's tab toggle is respected; their content is not modified).
	 * <p>
	 * Add/remove pairs are interleaved per-tab inside one client-thread
	 * invocation, with {@link TabInterface#reloadActiveTab()} called once at
	 * the end, so the bank UI does not redraw mid-sync.
	 */
	private SeedResult doSeedMissing()
	{
		Map<String, List<Integer>> tags = SkillBankData.tags();
		int itemsTagged = 0;
		int itemsRemoved = 0;
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

			Set<Integer> desired = new LinkedHashSet<>(entry.getValue());
			Set<Integer> currentlyTagged = new HashSet<>(tagManager.getItemsForTag(tagName));
			boolean anyChange = false;

			for (Integer itemId : currentlyTagged)
			{
				if (!desired.contains(itemId))
				{
					tagManager.removeTag(itemId, tagName);
					itemsRemoved++;
					anyChange = true;
				}
			}

			for (Integer itemId : desired)
			{
				if (currentlyTagged.contains(itemId))
				{
					itemsAlready++;
					continue;
				}
				tagManager.addTag(itemId, tagName, false);
				itemsTagged++;
				anyChange = true;
			}

			if (anyChange)
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

			// Brief #57: persist a Layout per tab so bank items render in the
			// sort order produced by sort_tables.py. No-ops if the bank isn't
			// loaded (seed-on-startup before bank-open); the bank-open /
			// bank-change / bank-close subscribers below will rebuild.
			buildAndSaveLayout(tagName);
		}

		if (!tabsCsvUpdated.equals(tabsCsv))
		{
			configManager.setConfiguration(BANKTAGS_GROUP, TAG_TABS_KEY, String.join(",", tabsCsvUpdated));
		}

		tabInterface.reloadActiveTab();

		return new SeedResult(itemsTagged, itemsRemoved, itemsAlready, tagsSeeded, tagsDisabled);
	}

	/**
	 * Build and save a sorted {@link Layout} for {@code tagName}, containing
	 * only the canonical item IDs the player currently has in their bank.
	 * Sort is two-zone (loadout / chaff) for combat + skilling tabs, single-
	 * zone for the rest — see {@link SkillBankLayoutBuilder}. No-op if the
	 * bank isn't loaded yet or the tag is unknown. Must run on the client
	 * thread.
	 */
	private void buildAndSaveLayout(String tagName)
	{
		ItemContainer bank = client.getItemContainer(InventoryID.BANK);
		if (bank == null)
		{
			return;
		}

		Set<Integer> ownedCanonical = new HashSet<>();
		for (Item it : bank.getItems())
		{
			if (it == null)
			{
				continue;
			}
			int id = it.getId();
			if (id > 0)
			{
				ownedCanonical.add(itemManager.canonicalize(id));
			}
		}

		Layout layout = layoutBuilder.buildLayout(tagName, ownedCanonical);
		layoutManager.saveLayout(layout);
	}

	/** Rebuild + save Layouts for every enabled Skill Bank tab. Client thread only. */
	private void rebuildAllLayouts()
	{
		for (String tagName : SkillBankData.tags().keySet())
		{
			if (!isTabEnabled(tagName))
			{
				continue;
			}
			buildAndSaveLayout(tagName);
		}
	}

	/**
	 * Watch for bank changes. When a Skill Bank tag tab is the active tab,
	 * rebuild its layout against the current bank contents and tell Bank Tags
	 * to redraw so newly-deposited items pop into their sorted position.
	 */
	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.BANK)
		{
			return;
		}
		if (!tabInterface.isTagTabActive())
		{
			return;
		}
		String activeTag = tabInterface.getActiveTag();
		if (activeTag == null || !SkillBankData.tags().containsKey(activeTag))
		{
			return;
		}
		buildAndSaveLayout(activeTag);
		tabInterface.reloadActiveTab();
	}

	/**
	 * When the bank closes, refresh layouts for every enabled Skill Bank tab.
	 * The active tab is already refreshed by {@link #onItemContainerChanged};
	 * this catches the other 21 so a tab switch on next bank-open uses the
	 * latest sorted order.
	 */
	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		if (event.getGroupId() != InterfaceID.BANKMAIN)
		{
			return;
		}
		rebuildAllLayouts();
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
			// Brief #57: also wipe the saved Layout so a re-seed starts clean.
			layoutManager.removeLayout(tagName);
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
	 * to the given callback (also invoked on the client thread). Silently
	 * no-ops when the client isn't yet at LOGGED_IN, since
	 * {@link TagManager#getItemsForTag} ultimately requires the OSRS item
	 * cache. The panel re-requests on the next LOGGED_IN transition via
	 * {@link SkillBankPanel#refresh()}.
	 */
	void requestTagPresence(Consumer<Map<String, Boolean>> callback)
	{
		clientThread.invokeLater(() ->
		{
			if (client.getGameState() != GameState.LOGGED_IN)
			{
				return;
			}
			callback.accept(currentTagPresence());
		});
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

	/**
	 * Brief #59 diagnostic: dump a per-tab layout trace to disk so we can
	 * compare the algorithm's intended ordering against what shows up in
	 * the bank UI. Writes ~/.runelite/skillbank-layout-trace.log. Must run
	 * on the client thread because it touches the bank container and
	 * ItemManager.
	 */
	void triggerLayoutTraceDump()
	{
		clientThread.invokeLater(this::doDumpLayoutTrace);
	}

	private void doDumpLayoutTrace()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			postChat("Skill Bank: trace dump requires being logged in.");
			return;
		}
		ItemContainer bank = client.getItemContainer(InventoryID.BANK);
		if (bank == null)
		{
			postChat("Skill Bank: open the bank at least once before dumping a trace.");
			return;
		}
		Set<Integer> owned = new HashSet<>();
		for (Item it : bank.getItems())
		{
			if (it == null) continue;
			int id = it.getId();
			if (id > 0) owned.add(itemManager.canonicalize(id));
		}

		StringBuilder out = new StringBuilder();
		out.append("Skill Bank layout trace\n")
			.append("Generated: ").append(java.time.Instant.now()).append("\n")
			.append("Bank owned (canonical) item count: ").append(owned.size()).append("\n\n");

		for (String tag : SkillBankData.tags().keySet())
		{
			LayoutTrace trace = layoutBuilder.traceLayout(tag, owned);
			out.append(trace.render());
			out.append("\n");
		}

		try
		{
			java.nio.file.Path target = java.nio.file.Paths.get(
				System.getProperty("user.home"),
				".runelite", "skillbank-layout-trace.log"
			);
			java.nio.file.Files.createDirectories(target.getParent());
			java.nio.file.Files.writeString(target, out.toString());
			postChat("Skill Bank: wrote layout trace to " + target);
			log.info("Wrote layout trace to {}", target);
		}
		catch (Exception e)
		{
			log.error("Failed to write layout trace", e);
			postChat("Skill Bank: failed to write trace — see console.");
		}
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
		toggles.put(SkillBankData.TAG_WOODCUTTING_FIREMAKING, config::tabWoodcuttingFiremaking);
		toggles.put(SkillBankData.TAG_FLETCHING, config::tabFletching);
		toggles.put(SkillBankData.TAG_FISHING, config::tabFishing);
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
		toggles.put(SkillBankData.TAG_COSMETICS, config::tabCosmetics);
		toggles.put(SkillBankData.TAG_TELEPORTS, config::tabTeleports);
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
		final int itemsRemoved;
		final int itemsAlready;
		final List<String> tagsSeeded;
		final List<String> tagsDisabled;

		SeedResult(int itemsTagged, int itemsRemoved, int itemsAlready,
			List<String> tagsSeeded, List<String> tagsDisabled)
		{
			this.itemsTagged = itemsTagged;
			this.itemsRemoved = itemsRemoved;
			this.itemsAlready = itemsAlready;
			this.tagsSeeded = tagsSeeded;
			this.tagsDisabled = tagsDisabled;
		}

		String summary()
		{
			return "synced " + tagsSeeded.size() + " tab(s): +" + itemsTagged + " added, -"
				+ itemsRemoved + " removed, " + itemsAlready + " unchanged; "
				+ tagsDisabled.size() + " disabled.";
		}
	}
}
