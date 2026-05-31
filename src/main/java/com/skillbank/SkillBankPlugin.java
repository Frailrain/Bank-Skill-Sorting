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
import net.runelite.api.events.GameTick;
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
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.RuneLiteProperties;
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
	name = "Auto Bank Sorter",
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

	@Inject
	private PluginManager pluginManager;

	private SkillBankPanel panel;
	private NavigationButton navButton;
	private boolean seedAttempted;
	private boolean setupCheckRunThisSession;
	/** Brief #81: tag waiting for a deferred rebuild. Set in
	 *  {@link #onItemContainerChanged}, consumed in {@link #onGameTick}.
	 *  null when no rebuild is pending. */
	private volatile String pendingRebuildTag;

	/** Brief #85: set true after seed-on-startup finishes; consumed when
	 *  the bank inventory container first changes (first bank open) so
	 *  the initial Skill Bank layout renders without requiring a
	 *  deposit/withdraw. */
	private volatile boolean needsInitialLayout;

	@Provides
	SkillBankConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SkillBankConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		seedAttempted = false;
		setupCheckRunThisSession = false;
		panel = new SkillBankPanel(this);
		// Dev-only Reset Setup Wizard button. RuneLiteProperties returns
		// null for getLauncherVersion() when the client is launched from
		// source (no production launcher); production users never see it.
		if (RuneLiteProperties.getLauncherVersion() == null)
		{
			panel.addResetWizardButton();
		}

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
		// First-run welcome + dependency check fire once per session and
		// persist their state via hidden config flags.
		if (!setupCheckRunThisSession)
		{
			setupCheckRunThisSession = true;
			clientThread.invokeLater(this::runFirstRunChecks);
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
			// Brief #85: prime the initial-layout flag once the seed
			// completes. The bank UI isn't loaded yet at this point
			// (login screen / game-world transition); the flag is
			// consumed when the bank-inventory container first
			// changes — i.e. the first bank open.
			needsInitialLayout = true;
		});
	}

	/**
	 * First-run welcome message + Bank Tag Layouts dependency check.
	 * <p>
	 * Welcome fires once (gated on {@code welcomeShown}). Dependency
	 * check fires up to 3 times (gated on {@code setupCheckCount}); once
	 * all dependencies are satisfied the {@code setupCheckDismissed}
	 * flag latches true and we stop checking forever.
	 */
	private void runFirstRunChecks()
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}

		// Welcome message — first login after installation.
		if (!config.welcomeShown())
		{
			postChatColored(
				"Your bank is being organized into 22 skill-sorted tabs. "
				+ "Open your bank to see them.");
			configManager.setConfiguration(SkillBankConfig.GROUP, "welcomeShown", true);
		}

		// Dependency check — skip if already dismissed or attempted 3+ times.
		if (config.setupCheckDismissed() || config.setupCheckCount() >= 3)
		{
			return;
		}

		SetupDependencyState state = getSetupDependencyState();
		if (state == SetupDependencyState.NEEDS_INSTALL)
		{
			postChatColored(
				"This plugin requires \"Bank Tag Layouts\" to display correctly. "
				+ "Install it from the Plugin Hub (plug icon &rarr; search "
				+ "\"Bank Tag Layouts\" &rarr; Install).");
			incrementSetupCount();
			return;
		}

		if (state == SetupDependencyState.NEEDS_CONFIG)
		{
			postChatColored(
				"Bank Tag Layouts is installed but needs to be configured. "
				+ "Open its settings and enable \"Enable layout by default\" &mdash; "
				+ "without this, your tabs won't sort correctly.");
			incrementSetupCount();
			return;
		}

		// Everything in place — latch dismissed so we never check again.
		configManager.setConfiguration(SkillBankConfig.GROUP, "setupCheckDismissed", true);
	}

	/** Tri-state used by both the chat check and the side-panel banner.
	 *  Brief #80: the panel banner stays visible while a dependency is
	 *  unmet, regardless of the chat-message dismissal counter. */
	enum SetupDependencyState
	{
		OK,
		NEEDS_INSTALL,
		NEEDS_CONFIG,
	}

	SetupDependencyState getSetupDependencyState()
	{
		if (!isPluginRunning(BANK_TAG_LAYOUTS_PLUGIN_NAME))
		{
			return SetupDependencyState.NEEDS_INSTALL;
		}
		if (!readBoolConfig(BANK_TAG_LAYOUTS_GROUP, BANK_TAG_LAYOUTS_DEFAULT_ON_KEY, false))
		{
			return SetupDependencyState.NEEDS_CONFIG;
		}
		return SetupDependencyState.OK;
	}

	private void incrementSetupCount()
	{
		configManager.setConfiguration(
			SkillBankConfig.GROUP, "setupCheckCount", config.setupCheckCount() + 1);
	}

	/** Dev-only: clear the first-run flags AND immediately re-run the
	 *  welcome + dependency checks so the messages appear in chat right
	 *  away. Used by the panel's Reset Setup Wizard button (visible only
	 *  when running from source). The previous version required the
	 *  player to log out, which kills the dev client. */
	void triggerResetSetupWizard()
	{
		configManager.setConfiguration(SkillBankConfig.GROUP, "welcomeShown", false);
		configManager.setConfiguration(SkillBankConfig.GROUP, "setupCheckDismissed", false);
		configManager.setConfiguration(SkillBankConfig.GROUP, "setupCheckCount", 0);
		setupCheckRunThisSession = true;
		clientThread.invokeLater(this::runFirstRunChecks);
	}

	/** Plugin Hub plugin detection. Iterates every plugin registered with
	 *  PluginManager and matches the @PluginDescriptor name. Returns true
	 *  only when the plugin is also currently enabled (not just present). */
	private boolean isPluginRunning(String pluginDescriptorName)
	{
		for (Plugin p : pluginManager.getPlugins())
		{
			PluginDescriptor d = p.getClass().getAnnotation(PluginDescriptor.class);
			if (d != null && pluginDescriptorName.equals(d.name())
				&& pluginManager.isPluginEnabled(p))
			{
				return true;
			}
		}
		return false;
	}

	/** Read a boolean from another plugin's config group. Returns {@code fallback}
	 *  when the setting has never been written (RuneLite returns null in that
	 *  case — the other plugin's @ConfigItem default applies). */
	private boolean readBoolConfig(String group, String key, boolean fallback)
	{
		String v = configManager.getConfiguration(group, key);
		if (v == null)
		{
			return fallback;
		}
		return "true".equalsIgnoreCase(v);
	}

	private void postChatColored(String body)
	{
		// Color the [Skill Bank Tabs] prefix in RuneLite's chat-message
		// green so the player can pick it out from the login noise.
		String message = "<col=00b000>[Skill Bank Tabs]</col> " + body;
		clientThread.invokeLater(() ->
		{
			if (client.getLocalPlayer() == null)
			{
				return;
			}
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
		});
	}

	private static final String BANK_TAG_LAYOUTS_PLUGIN_NAME = "Bank Tag Layouts";
	private static final String BANK_TAG_LAYOUTS_GROUP = "banktaglayouts";
	private static final String BANK_TAG_LAYOUTS_DEFAULT_ON_KEY = "layoutEnabledByDefault";

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
	 * <p>
	 * Brief #81: the rebuild + reload is deferred by one tick via
	 * {@link ClientThread#invokeLater}. {@code ItemContainerChanged} has
	 * multiple subscribers — bank-tags' own auto-tag handler is one of
	 * them — and event-bus dispatch order isn't guaranteed. If our
	 * subscriber runs first we save the layout and force a reload before
	 * bank-tags has had a chance to add the new item to the active tag's
	 * membership. When it auto-tags after our reload, the new item lands
	 * at slot 0 (the first free position) because the layout we just
	 * saved doesn't know about it yet. Deferring one tick lets every
	 * same-tick subscriber complete before we read the bank + rebuild.
	 */
	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.BANK)
		{
			return;
		}
		needsInitialLayout = false;
		// Brief #85: tabInterface.isTagTabActive() returns false even when
		// a tag tab IS active — verified in live diag (commit f10b6e8d
		// log: "tabActive=false activeTag=melee"). Gate on getActiveTag()
		// + the SkillBankData membership check only, matching bank-slot-
		// sync's working pattern.
		String activeTag = tabInterface.getActiveTag();
		if (activeTag == null || !SkillBankData.tags().containsKey(activeTag))
		{
			return;
		}
		pendingRebuildTag = activeTag;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (pendingRebuildTag == null)
		{
			return;
		}
		pendingRebuildTag = null;
		rebuildAndReloadActiveTab(null);
	}

	/** Brief #81: shared deferred rebuild + reload. Re-checks the active
	 *  tag at run time because the player may have switched tabs during
	 *  the delay. */
	private void rebuildAndReloadActiveTab(String expectedTag)
	{
		// Brief #85 (live diag): drop isTagTabActive() check — it returns
		// false even when a Skill Bank tag tab is actually active. Trust
		// getActiveTag() + the SkillBankData membership check instead.
		String currentTag = tabInterface.getActiveTag();
		if (currentTag == null || !SkillBankData.tags().containsKey(currentTag))
		{
			return;
		}
		// If the player switched tabs between event fire and this tick,
		// rebuild for whatever's actually visible now — not the snapshot
		// we captured at event time.
		log.debug("[SkillBank] Dynamic rebuild: tab={}, trigger=ItemContainerChanged", currentTag);
		buildAndSaveLayout(currentTag);
		// Brief #85: tabInterface.reloadActiveTab() is the canonical
		// re-render path — calls openBankTag → loadLayout (re-reads
		// config) → bankSearch.reset → layoutBank (re-renders grid).
		// bankSearch.layoutBank() alone would re-run the script against
		// a stale in-memory activeLayout.
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
