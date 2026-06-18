package com.skillbank;

import com.google.inject.Provides;
import java.awt.Window;
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
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Auto Bank Sorter",
	description = "Sorts your bank into 22 skill-based tabs using wiki-sourced item data, with two-zone combat layouts and dynamic re-sorting. Requires Bank Tag Layouts.",
	tags = {"bank", "tags", "skilling", "organization", "sort", "layout"}
)
@PluginDependency(BankTagsPlugin.class)
public class SkillBankPlugin extends Plugin
{
	static final String BANKTAGS_GROUP = "banktags";
	static final String ITEM_PREFIX = "item_";
	static final String ICON_PREFIX = "icon_";
	static final String TAG_TABS_KEY = "tagtabs";
	static final String LEGACY_TAGGED_ITEMS_PREFIX = "taggedItems_";

	// Brief #89/#90: collision-decision + naming config keys (in SkillBankConfig.GROUP).
	static final String SKIPPED_TABS_KEY = "skippedTabs";
	static final String RENAMED_TABS_KEY = "renamedTabs";
	static final String MANAGED_TABS_KEY = "managedTabs";
	static final String NAMING_MIGRATED_KEY = "namingMigrated";
	static final String AUTO_NAMING_KEY = "autoNaming";
	/** Suffix on a fresh install's bank tab, e.g. "herblore (auto)" — keeps the
	 *  default seed names collision-safe (Brief #89). Lower case because Bank
	 *  Tags renders all tab names standardized regardless. */
	static final String AUTO_DISPLAY_SUFFIX = " (auto)";

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
			.tooltip("Auto Bank Sorter")
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
						postChat("Auto Bank Sorter: enable 'Confirm reset' before resetting tags.");
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
							postChat("Auto Bank Sorter: removed" + cleared + " skill tag(s). Reseed via 'Reseed missing tags'.");
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
		clientThread.invokeLater(this::runStartupSeed);
	}

	/**
	 * Brief #90: route the once-per-session startup seed by install state.
	 * <ul>
	 *   <li><b>already on the new scheme</b> ({@code namingMigrated}) — normal
	 *       seed/refresh via {@link #doSeedMissing};</li>
	 *   <li><b>legacy install</b> (fingerprint tabs present) — one-time rename
	 *       of the old lowercase tabs to title case, no wizard;</li>
	 *   <li><b>new install</b> — seed with the "(Auto)" naming scheme; the
	 *       Brief #89 wizard handles any (unlikely) collisions.</li>
	 * </ul>
	 * Runs on the client thread.
	 */
	private void runStartupSeed()
	{
		if (!config.namingMigrated() && isLegacyInstall())
		{
			int migrated = doNamingMigration();
			configManager.setConfiguration(SkillBankConfig.GROUP, AUTO_NAMING_KEY, false);
			configManager.setConfiguration(SkillBankConfig.GROUP, NAMING_MIGRATED_KEY, true);
			needsInitialLayout = true;
			String summary = "renamed " + migrated + " tab(s) to title case.";
			if (config.announceInChat())
			{
				postChat("Auto Bank Sorter: " + summary);
			}
			if (panel != null)
			{
				SwingUtilities.invokeLater(() ->
				{
					panel.setStatus(summary);
					panel.refresh();
				});
			}
			return;
		}

		boolean newInstall = !config.namingMigrated();
		if (newInstall)
		{
			// Fresh install: seed under the "(Auto)" suffix scheme.
			configManager.setConfiguration(SkillBankConfig.GROUP, AUTO_NAMING_KEY, true);
		}
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
		if (newInstall)
		{
			configManager.setConfiguration(SkillBankConfig.GROUP, NAMING_MIGRATED_KEY, true);
		}
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
		// Color the [Auto Bank Sorter] prefix in RuneLite's chat-message
		// green so the player can pick it out from the login noise.
		String message = "<col=00b000>[Auto Bank Sorter]</col> " + body;
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
		clientThread.invokeLater(() ->
		{
			SeedResult result = doSeedMissing();
			callback.accept(result);
			// Brief #89: any first-time name collisions are surfaced in a
			// modal chooser on the EDT. Non-colliding tabs are already seeded
			// by doSeedMissing above, so the popup never blocks them.
			if (!result.collisions.isEmpty())
			{
				SwingUtilities.invokeLater(() -> showCollisionDialog(result.collisions));
			}
		});
	}

	/**
	 * Seed/refresh every enabled skill tab, honouring the player's stored
	 * collision decisions (Brief #89). Routing per base tag name:
	 * <ul>
	 *   <li><b>skipped</b> — left completely untouched (their tab, their data);</li>
	 *   <li><b>renamed</b> — the plugin's content is (re)seeded under the
	 *       "(Auto)" variant; the user's same-named tab is left alone;</li>
	 *   <li><b>managed</b> — the plugin owns it: synced to {@link SkillBankData};</li>
	 *   <li><b>unresolved</b> — if a tab/layout already exists under the name it
	 *       is a collision (returned for the chooser, never overwritten here);
	 *       on a genuine fresh install with nothing there, it is seeded and
	 *       adopted as managed.</li>
	 * </ul>
	 * On the very first pass, an upgrade of an install that has seeded before
	 * (welcome already shown) adopts its existing plugin tabs silently rather
	 * than flagging all 22 as collisions.
	 * <p>
	 * Per-tab sync is non-additive — {@code SkillBankData} is the sole source
	 * of truth. {@link TabInterface#reloadActiveTab()} is called once at the
	 * end so the bank UI does not redraw mid-sync.
	 */
	private SeedResult doSeedMissing()
	{
		Set<String> skipped = readDecisionSet(SKIPPED_TABS_KEY);
		Set<String> renamed = readDecisionSet(RENAMED_TABS_KEY);
		Set<String> managed = readDecisionSet(MANAGED_TABS_KEY);
		Set<String> managedBefore = new LinkedHashSet<>(managed);

		// Snapshot existing tabs before seeding so a tab we create this pass is
		// never mistaken for a pre-existing user tab later in the same pass.
		Set<String> existingTabs = readTagTabsStandardized();

		SeedAccumulator acc = new SeedAccumulator(readTagTabsList());
		List<String> tabsCsvBefore = new ArrayList<>(acc.tabsCsv);

		List<String> tagsDisabled = new ArrayList<>();
		List<String> collisions = new ArrayList<>();

		for (String internal : SkillBankData.tags().keySet())
		{
			if (!isTabEnabled(internal))
			{
				tagsDisabled.add(internal);
				continue;
			}
			if (skipped.contains(internal))
			{
				continue;
			}
			if (renamed.contains(internal))
			{
				seedTab(internal, autoOp(internal), autoDisplay(internal), acc);
				continue;
			}
			if (managed.contains(internal))
			{
				seedTab(internal, primaryOp(internal), primaryDisplay(internal), acc);
				continue;
			}
			// Unresolved name: collision only if this install's primary tab
			// name already exists (Brief #89). "(Auto)" names rarely collide.
			if (hasExistingTab(primaryOp(internal), existingTabs))
			{
				collisions.add(internal);
			}
			else
			{
				seedTab(internal, primaryOp(internal), primaryDisplay(internal), acc);
				managed.add(internal);
			}
		}

		if (!managed.equals(managedBefore))
		{
			writeDecisionSet(MANAGED_TABS_KEY, managed);
		}
		if (!acc.tabsCsv.equals(tabsCsvBefore))
		{
			configManager.setConfiguration(BANKTAGS_GROUP, TAG_TABS_KEY, String.join(",", acc.tabsCsv));
		}

		tabInterface.reloadActiveTab();

		return new SeedResult(acc.itemsTagged, acc.itemsRemoved, acc.itemsAlready,
			acc.tagsSeeded, tagsDisabled, collisions);
	}

	/**
	 * Sync one tab's membership to {@link SkillBankData} and (re)build its
	 * layout. Non-additive: items tagged but no longer in the bucket are
	 * removed, bucket items not yet tagged are added.
	 *
	 * @param dataTag SkillBankData key supplying the item set + sort order
	 * @param opTag   standardized tag name used for membership / icon / layout
	 *                storage — equals {@code dataTag} for a normal tab, the
	 *                "(auto)" key for an alongside tab
	 * @param display tab name written to the bank-tags tab list (cased label)
	 */
	private void seedTab(String dataTag, String opTag, String display, SeedAccumulator acc)
	{
		List<Integer> bucket = SkillBankData.tags().get(dataTag);
		if (bucket == null)
		{
			return;
		}
		Set<Integer> desired = new LinkedHashSet<>(bucket);
		Set<Integer> currentlyTagged = new HashSet<>(tagManager.getItemsForTag(opTag));
		boolean anyChange = false;

		for (Integer itemId : currentlyTagged)
		{
			if (!desired.contains(itemId))
			{
				tagManager.removeTag(itemId, opTag);
				acc.itemsRemoved++;
				anyChange = true;
			}
		}

		for (Integer itemId : desired)
		{
			if (currentlyTagged.contains(itemId))
			{
				acc.itemsAlready++;
				continue;
			}
			tagManager.addTag(itemId, opTag, false);
			acc.itemsTagged++;
			anyChange = true;
		}

		if (anyChange)
		{
			acc.tagsSeeded.add(display);
		}

		String iconKey = ICON_PREFIX + opTag;
		if (configManager.getConfiguration(BANKTAGS_GROUP, iconKey) == null)
		{
			int iconId = SkillBankData.iconFor(dataTag);
			if (iconId > 0)
			{
				configManager.setConfiguration(BANKTAGS_GROUP, iconKey, String.valueOf(iconId));
			}
		}

		ensureTabInList(acc.tabsCsv, display);

		// Brief #57: persist a Layout per tab so bank items render in the
		// sort order produced by sort_tables.py. No-ops if the bank isn't
		// loaded (seed-on-startup before bank-open); the bank subscribers
		// rebuild on the next bank open / change / close.
		buildAndSaveLayout(opTag);
	}

	/** Mutable per-pass seed counters + working copy of the tab-list (ordered). */
	private static final class SeedAccumulator
	{
		int itemsTagged;
		int itemsRemoved;
		int itemsAlready;
		final List<String> tagsSeeded = new ArrayList<>();
		final List<String> tabsCsv;

		SeedAccumulator(List<String> tabsCsv)
		{
			this.tabsCsv = tabsCsv;
		}
	}

	/**
	 * Ensure {@code display} is present in the ordered tab list exactly once,
	 * matching case-insensitively on the standardized form. If an entry already
	 * standardizes the same (e.g. a pre-existing "Melee" vs our "melee"), it is
	 * normalized in place — preserving tab order and never duplicating.
	 */
	private static void ensureTabInList(List<String> tabsCsv, String display)
	{
		String op = Text.standardize(display);
		for (int i = 0; i < tabsCsv.size(); i++)
		{
			if (Text.standardize(tabsCsv.get(i)).equals(op))
			{
				tabsCsv.set(i, display);
				return;
			}
		}
		tabsCsv.add(display);
	}

	/**
	 * Build and save a sorted {@link Layout} for {@code opTag}, containing only
	 * the canonical item IDs the player currently has in their bank. Sort is
	 * two-zone (loadout / chaff) for combat + skilling tabs, single-zone for
	 * the rest — see {@link SkillBankLayoutBuilder}. No-op if the bank isn't
	 * loaded yet or the tag is unknown. Must run on the client thread.
	 * <p>
	 * Brief #90: {@code opTag} may be a title-case or "(auto)" tab name whose
	 * standardized key differs from the internal {@link SkillBankData} id (e.g.
	 * "woodcutting + firemaking" -> internal "woodcutting_firemaking"). The
	 * layout content is built from the internal bucket, then re-tagged to
	 * {@code opTag} before saving so the bank renders it under the right name.
	 */
	private void buildAndSaveLayout(String opTag)
	{
		ItemContainer bank = client.getItemContainer(InventoryID.BANK);
		if (bank == null)
		{
			return;
		}

		String op = Text.standardize(opTag);
		String dataTag = internalForOp(op);
		if (dataTag == null)
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

		Layout layout = layoutBuilder.buildLayout(dataTag, ownedCanonical);
		if (!op.equals(dataTag))
		{
			// Display key differs from the internal id: keep the sorted
			// positions, re-tag to the display key so saveLayout writes under
			// the rendered tab's config.
			layout = new Layout(op, layout.getLayout());
		}
		layoutManager.saveLayout(layout);
	}

	/** Rebuild + save Layouts for every enabled Skill Bank tab the plugin
	 *  manages. Client thread only. Brief #89: skipped tabs and unresolved
	 *  collisions are left untouched; renamed tabs rebuild under their
	 *  "(auto)" key. */
	private void rebuildAllLayouts()
	{
		Set<String> skipped = readDecisionSet(SKIPPED_TABS_KEY);
		Set<String> renamed = readDecisionSet(RENAMED_TABS_KEY);
		Set<String> managed = readDecisionSet(MANAGED_TABS_KEY);
		for (String internal : SkillBankData.tags().keySet())
		{
			if (!isTabEnabled(internal) || skipped.contains(internal))
			{
				continue;
			}
			if (renamed.contains(internal))
			{
				buildAndSaveLayout(autoOp(internal));
			}
			else if (managed.contains(internal))
			{
				buildAndSaveLayout(primaryOp(internal));
			}
			// else: unresolved collision pending the chooser — don't touch it.
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
		if (activeTag == null || !isManagedActiveTag(activeTag))
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
		if (currentTag == null || !isManagedActiveTag(currentTag))
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
		// Standardized tab names removed below — used to filter the tab-list CSV.
		Set<String> namesToDrop = new HashSet<>();
		int cleared = 0;
		for (String internal : skillTags)
		{
			// Brief #90: clear every naming form — legacy lowercase id, bare
			// title case, and the "(auto)" variant.
			for (String op : new String[]{internal, titleOp(internal), autoOp(internal)})
			{
				cleared += clearOneTag(op);
				namesToDrop.add(op);
			}
		}

		Set<String> tabsCsv = readTagTabs();
		Set<String> tabsCsvKept = new LinkedHashSet<>();
		for (String t : tabsCsv)
		{
			if (!namesToDrop.contains(Text.standardize(t)))
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

		// Brief #89: wipe stored collision decisions so a later reseed
		// re-detects from a clean slate.
		clearStoredDecisions();

		tabInterface.reloadActiveTab();

		return cleared;
	}

	/** Remove a single tag's membership, icon and layout. Returns 1 if the tag
	 *  had any tagged items (so the caller can count cleared tabs), else 0. */
	private int clearOneTag(String opTag)
	{
		int cleared = 0;
		if (!tagManager.getItemsForTag(opTag).isEmpty())
		{
			tagManager.removeTag(opTag);
			cleared = 1;
		}
		configManager.unsetConfiguration(BANKTAGS_GROUP, ICON_PREFIX + opTag);
		// Brief #57: also wipe the saved Layout so a re-seed starts clean.
		layoutManager.removeLayout(opTag);
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

	/** Describe current on-disk state for the panel. Must run on the client thread.
	 *  Brief #91: check presence under the tab's actual op key, not the internal
	 *  id — after migration the compound tabs live under their " + " title key
	 *  (e.g. "woodcutting + firemaking"), and a new install under the "(auto)"
	 *  key, so checking the raw internal id falsely reports them empty. */
	private Map<String, Boolean> currentTagPresence()
	{
		Set<String> renamed = readDecisionSet(RENAMED_TABS_KEY);
		Map<String, Boolean> out = new LinkedHashMap<>();
		for (String internal : SkillBankData.tags().keySet())
		{
			String op = renamed.contains(internal) ? autoOp(internal) : primaryOp(internal);
			out.put(internal, !tagManager.getItemsForTag(op).isEmpty());
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

	// ---- Brief #89: collision detection, naming and decision persistence ----

	/** Existing tab names, standardized, for collision detection. */
	private Set<String> readTagTabsStandardized()
	{
		Set<String> out = new LinkedHashSet<>();
		for (String t : readTagTabs())
		{
			out.add(Text.standardize(t));
		}
		return out;
	}

	/** True if the player already has a tab, tagged items, or a saved layout
	 *  under {@code base} — i.e. seeding it now would collide. */
	private boolean hasExistingTab(String base, Set<String> existingStdTabs)
	{
		return existingStdTabs.contains(base)
			|| !tagManager.getItemsForTag(base).isEmpty()
			|| layoutManager.loadLayout(base) != null;
	}

	/** True if the standardized active tag is one of this plugin's managed
	 *  tabs — its primary (title or auto) form, or an "(auto)" alongside tab. */
	private boolean isManagedActiveTag(String activeTag)
	{
		String std = Text.standardize(activeTag);
		String internal = internalForOp(std);
		if (internal == null)
		{
			return false;
		}
		if (std.equals(autoOp(internal)) && readDecisionSet(RENAMED_TABS_KEY).contains(internal))
		{
			return true;
		}
		return readDecisionSet(MANAGED_TABS_KEY).contains(internal);
	}

	// ---- Brief #90: internal id <-> display / op-key naming ----

	/** Reverse lookup of any standardized op key (bare title case, the
	 *  "(auto)" form, or the legacy lowercase id) to the internal
	 *  SkillBankData id. Built once from {@link SkillBankData#displayName}. */
	private static final Map<String, String> OP_TO_INTERNAL;

	static
	{
		Map<String, String> m = new LinkedHashMap<>();
		for (String internal : SkillBankData.tags().keySet())
		{
			m.put(Text.standardize(SkillBankData.displayName(internal)), internal);
			m.put(Text.standardize(SkillBankData.displayName(internal) + AUTO_DISPLAY_SUFFIX), internal);
			m.put(internal, internal);
		}
		OP_TO_INTERNAL = java.util.Collections.unmodifiableMap(m);
	}

	/** Internal SkillBankData id for a standardized op key, or null. */
	private static String internalForOp(String std)
	{
		return OP_TO_INTERNAL.get(std);
	}

	/** Bare title-case label, e.g. "woodcutting_firemaking" -> "Woodcutting + Firemaking". */
	private static String titleDisplay(String internal)
	{
		return SkillBankData.displayName(internal);
	}

	/** Standardized key for the bare title-case tab. */
	private static String titleOp(String internal)
	{
		return Text.standardize(titleDisplay(internal));
	}

	/** Cased "(Auto)" label, e.g. "Herblore (Auto)". */
	private static String autoDisplay(String internal)
	{
		return titleDisplay(internal) + AUTO_DISPLAY_SUFFIX;
	}

	/** Standardized key for the "(auto)" tab. */
	private static String autoOp(String internal)
	{
		return Text.standardize(autoDisplay(internal));
	}

	/** This install's default seed label for a tab (scheme-dependent). */
	private String primaryDisplay(String internal)
	{
		return config.autoNaming() ? autoDisplay(internal) : titleDisplay(internal);
	}

	/** This install's default seed op key for a tab (scheme-dependent). */
	private String primaryOp(String internal)
	{
		return config.autoNaming() ? autoOp(internal) : titleOp(internal);
	}

	// ---- Brief #90: legacy lowercase -> title-case migration ----

	/** Fingerprint: all three underscore-named compound tabs present. Nobody
	 *  creates all three by hand, so this is an existing Auto Bank Sorter
	 *  install whose tabs predate the title-case naming. */
	private boolean isLegacyInstall()
	{
		Set<String> existing = readTagTabsStandardized();
		return hasExistingTab(SkillBankData.TAG_WOODCUTTING_FIREMAKING, existing)
			&& hasExistingTab(SkillBankData.TAG_MINING_SMITHING, existing)
			&& hasExistingTab(SkillBankData.TAG_AGILITY_THIEVING, existing);
	}

	/**
	 * One-time rename of a legacy install's tabs from the old lowercase ids to
	 * bare title case. Simple tabs (e.g. "melee" -> "Melee") keep the same
	 * standardized key, so only the tab-list label changes. Compound tabs
	 * ("woodcutting_firemaking" -> "Woodcutting + Firemaking") change key, so
	 * their tag data, layout and icon are moved (write-new-before-delete-old).
	 * All 22 are marked managed. Returns the number of tabs renamed.
	 */
	private int doNamingMigration()
	{
		Set<String> managed = readDecisionSet(MANAGED_TABS_KEY);
		Set<String> existing = readTagTabsStandardized();
		Map<String, String> tabtabsRenames = new LinkedHashMap<>();
		int renamed = 0;

		for (String internal : SkillBankData.tags().keySet())
		{
			// Mark managed even when disabled/absent, so a later enable seeds
			// it under the new (title-case) scheme rather than re-colliding.
			managed.add(internal);

			String oldOp = internal;
			if (!hasExistingTab(oldOp, existing))
			{
				continue;
			}

			String newDisplay = titleDisplay(internal);
			String newOp = Text.standardize(newDisplay);

			if (!newOp.equals(oldOp))
			{
				// Key changes (compound tabs). Guard: skip if a DIFFERENT tab
				// already owns the title-case name (extreme edge case).
				if (hasExistingTab(newOp, existing))
				{
					log.warn("Auto Bank Sorter: '{}' already exists; leaving '{}' unrenamed",
						newDisplay, oldOp);
					continue;
				}
				moveTagData(oldOp, newOp);
			}
			tabtabsRenames.put(oldOp, newDisplay);
			renamed++;
		}

		applyTabtabsRenames(tabtabsRenames);
		writeDecisionSet(MANAGED_TABS_KEY, managed);
		tabInterface.reloadActiveTab();
		log.info("Auto Bank Sorter: naming migration renamed {} tab(s)", renamed);
		return renamed;
	}

	/** Move a tab's membership, layout and icon from {@code oldOp} to
	 *  {@code newOp}. The new copy is written in full before the old is
	 *  deleted, so a mid-failure leaves both (harmless) — never data loss. */
	private void moveTagData(String oldOp, String newOp)
	{
		List<Integer> items = new ArrayList<>(tagManager.getItemsForTag(oldOp));
		Layout oldLayout = layoutManager.loadLayout(oldOp);
		String icon = configManager.getConfiguration(BANKTAGS_GROUP, ICON_PREFIX + oldOp);

		// Write new first.
		for (Integer id : items)
		{
			tagManager.addTag(id, newOp, false);
		}
		if (oldLayout != null)
		{
			layoutManager.saveLayout(new Layout(newOp, oldLayout.getLayout()));
		}
		if (icon != null)
		{
			configManager.setConfiguration(BANKTAGS_GROUP, ICON_PREFIX + newOp, icon);
		}

		// Then delete old.
		for (Integer id : items)
		{
			tagManager.removeTag(id, oldOp);
		}
		layoutManager.removeLayout(oldOp);
		configManager.unsetConfiguration(BANKTAGS_GROUP, ICON_PREFIX + oldOp);
	}

	/** Replace tab-list entries per {@code renames} (key = standardized old
	 *  name, value = new display), preserving order and any non-plugin tabs
	 *  such as a user's "clues" tab. */
	private void applyTabtabsRenames(Map<String, String> renames)
	{
		List<String> current = readTagTabsList();
		List<String> updated = new ArrayList<>(current.size());
		for (String entry : current)
		{
			updated.add(renames.getOrDefault(Text.standardize(entry), entry));
		}
		if (!updated.equals(current))
		{
			configManager.setConfiguration(BANKTAGS_GROUP, TAG_TABS_KEY, String.join(",", updated));
		}
	}

	/** Tab-list entries in order (ordered form of {@link #readTagTabs}). */
	private List<String> readTagTabsList()
	{
		return new ArrayList<>(readTagTabs());
	}

	/**
	 * Brief #91: rename every managed tab from its bare title-case key to the
	 * "(auto)" variant (e.g. "herblore" -> "herblore (auto)",
	 * "woodcutting + firemaking" -> "woodcutting + firemaking (auto)") via
	 * {@link #moveTagData}, rewrite the tab list, and set the auto-naming flag
	 * so future seeds use "(auto)" names. One-way. Client thread.
	 */
	private void doUpdateNamingScheme()
	{
		if (config.autoNaming())
		{
			return;
		}
		Set<String> managed = readDecisionSet(MANAGED_TABS_KEY);
		Map<String, String> tabtabsRenames = new LinkedHashMap<>();
		int renamed = 0;
		for (String internal : SkillBankData.tags().keySet())
		{
			if (!managed.contains(internal))
			{
				continue;
			}
			String oldOp = titleOp(internal);
			String newOp = autoOp(internal);
			if (oldOp.equals(newOp))
			{
				continue;
			}
			moveTagData(oldOp, newOp);
			tabtabsRenames.put(oldOp, autoDisplay(internal));
			renamed++;
		}
		applyTabtabsRenames(tabtabsRenames);
		configManager.setConfiguration(SkillBankConfig.GROUP, AUTO_NAMING_KEY, true);
		tabInterface.reloadActiveTab();

		String summary = "switched " + renamed + " tab(s) to \"(auto)\" naming.";
		log.info("Auto Bank Sorter: {}", summary);
		if (config.announceInChat())
		{
			postChat("Auto Bank Sorter: " + summary);
		}
		if (panel != null)
		{
			SwingUtilities.invokeLater(() ->
			{
				panel.setStatus(summary);
				panel.refreshNamingButton();
				panel.refresh();
			});
		}
	}

	private Set<String> readDecisionSet(String key)
	{
		String v = configManager.getConfiguration(SkillBankConfig.GROUP, key);
		Set<String> out = new LinkedHashSet<>();
		if (v == null || v.isEmpty())
		{
			return out;
		}
		for (String part : v.split(","))
		{
			String trimmed = Text.standardize(part);
			if (!trimmed.isEmpty())
			{
				out.add(trimmed);
			}
		}
		return out;
	}

	private void writeDecisionSet(String key, Set<String> values)
	{
		configManager.setConfiguration(SkillBankConfig.GROUP, key, String.join(",", values));
	}

	private void clearStoredDecisions()
	{
		configManager.unsetConfiguration(SkillBankConfig.GROUP, SKIPPED_TABS_KEY);
		configManager.unsetConfiguration(SkillBankConfig.GROUP, RENAMED_TABS_KEY);
		configManager.unsetConfiguration(SkillBankConfig.GROUP, MANAGED_TABS_KEY);
	}

	/**
	 * Show the collision chooser (EDT). The dialog only collects decisions;
	 * {@link #onCollisionDecisions} persists them and applies the seeding on
	 * the client thread.
	 */
	private void showCollisionDialog(List<String> collisions)
	{
		Window owner = panel != null ? SwingUtilities.getWindowAncestor(panel) : null;
		SkillBankCollisionDialog dialog = new SkillBankCollisionDialog(
			owner, collisions, SkillBankData::displayName, this::onCollisionDecisions);
		dialog.setVisible(true);
	}

	/** Persist the player's choices, then seed Overwrite/Alongside tabs. */
	private void onCollisionDecisions(Map<String, SkillBankCollisionDialog.Decision> decisions)
	{
		Set<String> skipped = readDecisionSet(SKIPPED_TABS_KEY);
		Set<String> renamed = readDecisionSet(RENAMED_TABS_KEY);
		Set<String> managed = readDecisionSet(MANAGED_TABS_KEY);
		for (Map.Entry<String, SkillBankCollisionDialog.Decision> e : decisions.entrySet())
		{
			switch (e.getValue())
			{
				case SKIP:
					skipped.add(e.getKey());
					break;
				case OVERWRITE:
					managed.add(e.getKey());
					break;
				case ALONGSIDE:
					renamed.add(e.getKey());
					break;
				default:
					break;
			}
		}
		writeDecisionSet(SKIPPED_TABS_KEY, skipped);
		writeDecisionSet(RENAMED_TABS_KEY, renamed);
		writeDecisionSet(MANAGED_TABS_KEY, managed);

		clientThread.invokeLater(() -> applyDecisionSeeds(decisions));
	}

	/** Client thread: seed the tabs the player chose to Overwrite / create
	 *  Alongside. Skip choices touch nothing. */
	private void applyDecisionSeeds(Map<String, SkillBankCollisionDialog.Decision> decisions)
	{
		SeedAccumulator acc = new SeedAccumulator(readTagTabsList());
		List<String> before = new ArrayList<>(acc.tabsCsv);
		for (Map.Entry<String, SkillBankCollisionDialog.Decision> e : decisions.entrySet())
		{
			String internal = e.getKey();
			switch (e.getValue())
			{
				case OVERWRITE:
					seedTab(internal, primaryOp(internal), primaryDisplay(internal), acc);
					break;
				case ALONGSIDE:
					seedTab(internal, autoOp(internal), autoDisplay(internal), acc);
					break;
				default:
					break;
			}
		}
		if (!acc.tabsCsv.equals(before))
		{
			configManager.setConfiguration(BANKTAGS_GROUP, TAG_TABS_KEY, String.join(",", acc.tabsCsv));
		}
		tabInterface.reloadActiveTab();
		if (config.announceInChat())
		{
			postChat("Auto Bank Sorter: applied your tab choices.");
		}
		if (panel != null)
		{
			SwingUtilities.invokeLater(() -> panel.refresh());
		}
	}

	/**
	 * Panel action: forget the player's Skip / Alongside choices so those tabs
	 * are re-evaluated (and re-prompted) on the next seed. Plugin-owned
	 * (managed) tabs are left in place so they don't all re-collide. Brief #89.
	 */
	void triggerResetDecisions()
	{
		configManager.unsetConfiguration(SkillBankConfig.GROUP, SKIPPED_TABS_KEY);
		configManager.unsetConfiguration(SkillBankConfig.GROUP, RENAMED_TABS_KEY);
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			seedMissing(result ->
			{
				if (panel != null)
				{
					SwingUtilities.invokeLater(() -> panel.setStatus(result.summary()));
				}
			});
		}
		else if (panel != null)
		{
			SwingUtilities.invokeLater(
				() -> panel.setStatus("Tab decisions cleared. Log in to re-check."));
		}
	}

	/** Brief #91: true while this install is on the legacy bare title-case
	 *  naming and can opt into the "(auto)" scheme. Drives the panel button. */
	boolean isLegacyNamingScheme()
	{
		return config.namingMigrated() && !config.autoNaming();
	}

	/**
	 * Brief #91: one-time opt-in upgrade. Renames every managed tab to its
	 * "(auto)" variant and flips the install onto the auto naming scheme. No
	 * confirmation, no reverse path. No-op if already on auto naming.
	 */
	void triggerUpdateNamingScheme()
	{
		if (config.autoNaming())
		{
			return;
		}
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			if (panel != null)
			{
				SwingUtilities.invokeLater(
					() -> panel.setStatus("Log in first to update the naming scheme."));
			}
			return;
		}
		clientThread.invokeLater(this::doUpdateNamingScheme);
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
		postChat("Auto Bank Sorter: " + result.summary() + " Reopen your bank if tabs aren't visible yet.");
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
		/** Brief #89: base tag names that already existed and were left
		 *  untouched, pending the player's choice in the collision chooser. */
		final List<String> collisions;

		SeedResult(int itemsTagged, int itemsRemoved, int itemsAlready,
			List<String> tagsSeeded, List<String> tagsDisabled, List<String> collisions)
		{
			this.itemsTagged = itemsTagged;
			this.itemsRemoved = itemsRemoved;
			this.itemsAlready = itemsAlready;
			this.tagsSeeded = tagsSeeded;
			this.tagsDisabled = tagsDisabled;
			this.collisions = collisions;
		}

		String summary()
		{
			return "synced " + tagsSeeded.size() + " tab(s): +" + itemsTagged + " added, -"
				+ itemsRemoved + " removed, " + itemsAlready + " unchanged; "
				+ tagsDisabled.size() + " disabled.";
		}
	}
}
