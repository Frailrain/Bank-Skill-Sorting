package com.skillbank;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

class SkillBankPanel extends PluginPanel
{
	private final SkillBankPlugin plugin;
	private final JLabel statusLabel;
	private final JPanel tagListPanel;
	private final JCheckBox confirmResetBox;
	private final JPanel content;
	private final JLabel dependencyBanner;
	private final JButton namingButton;

	SkillBankPanel(SkillBankPlugin plugin)
	{
		super(false);
		this.plugin = plugin;

		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel title = new JLabel("Auto Bank Sorter");
		title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
		title.setForeground(Color.WHITE);
		title.setAlignmentX(LEFT_ALIGNMENT);
		content.add(title);

		content.add(createSpacer(8));

		// Brief #80: dependency status banner. Lives at the top of the
		// panel so the player sees the warning every time the panel is
		// opened while Bank Tag Layouts is missing or unconfigured.
		// Hidden when state == OK.
		dependencyBanner = new JLabel();
		dependencyBanner.setAlignmentX(LEFT_ALIGNMENT);
		dependencyBanner.setForeground(new Color(255, 180, 90));
		dependencyBanner.setVisible(false);
		content.add(dependencyBanner);

		JLabel help = new JLabel(
			"<html>Seeds missing Bank Tags groups.<br>"
				+ "Existing tags are never overwritten.<br><br>"
				+ "<b>Recommended:</b> install <i>Bank Tag Layouts</i> from<br>"
				+ "the Plugin Hub and enable<br>"
				+ "<i>Enable layout by default</i>. Without it, tabs are<br>"
				+ "grouped by source bank with separator lines, not a<br>"
				+ "flat grid.</html>");
		help.setForeground(Color.LIGHT_GRAY);
		help.setAlignmentX(LEFT_ALIGNMENT);
		content.add(help);

		content.add(createSpacer(10));

		// Brief #91: one-time opt-in to the "(auto)" naming scheme. Visible
		// only while on the legacy (bare title-case) scheme; hides for good
		// once switched.
		namingButton = new JButton("Update naming scheme");
		namingButton.setToolTipText(
			"Rename your tabs to the current \"(auto)\" scheme (one-time, no undo)");
		namingButton.setAlignmentX(LEFT_ALIGNMENT);
		namingButton.addActionListener(e -> plugin.triggerUpdateNamingScheme());
		namingButton.setVisible(plugin.isLegacyNamingScheme());
		content.add(namingButton);

		content.add(createSpacer(6));

		JButton seedButton = new JButton("Seed missing tags");
		seedButton.setAlignmentX(LEFT_ALIGNMENT);
		seedButton.addActionListener(e -> plugin.triggerReseed());
		content.add(seedButton);

		content.add(createSpacer(6));

		confirmResetBox = new JCheckBox("Confirm reset");
		confirmResetBox.setForeground(Color.LIGHT_GRAY);
		confirmResetBox.setBackground(ColorScheme.DARK_GRAY_COLOR);
		confirmResetBox.setAlignmentX(LEFT_ALIGNMENT);
		confirmResetBox.addActionListener(e -> plugin.setResetConfirm(confirmResetBox.isSelected()));
		content.add(confirmResetBox);

		JButton resetButton = new JButton("Reset all skill tags");
		resetButton.setAlignmentX(LEFT_ALIGNMENT);
		resetButton.addActionListener(e -> plugin.triggerReset());
		content.add(resetButton);

		content.add(createSpacer(6));

		// Brief #89: forget Skip / Create-alongside choices so the plugin
		// re-asks about those tabs on the next seed. Does not delete the
		// plugin's own tabs.
		JButton resetDecisionsButton = new JButton("Reset tab decisions");
		resetDecisionsButton.setToolTipText(
			"Re-ask about tabs you chose to skip or create alongside");
		resetDecisionsButton.setAlignmentX(LEFT_ALIGNMENT);
		resetDecisionsButton.addActionListener(e -> plugin.triggerResetDecisions());
		content.add(resetDecisionsButton);

		content.add(createSpacer(12));

		statusLabel = new JLabel("Ready.");
		statusLabel.setForeground(Color.LIGHT_GRAY);
		statusLabel.setAlignmentX(LEFT_ALIGNMENT);
		content.add(statusLabel);

		content.add(createSpacer(10));

		JLabel listHeader = new JLabel("Current tags:");
		listHeader.setForeground(Color.WHITE);
		listHeader.setAlignmentX(LEFT_ALIGNMENT);
		content.add(listHeader);

		tagListPanel = new JPanel(new GridLayout(0, 1, 0, 2));
		tagListPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		tagListPanel.setAlignmentX(LEFT_ALIGNMENT);
		content.add(tagListPanel);

		add(content, BorderLayout.NORTH);

		JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
		footer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		add(footer, BorderLayout.SOUTH);

		renderEmptyTagList();
		refresh();
	}

	/** Brief #78b: dev-only button that clears the first-run setup flags
	 *  so the welcome + dependency check can be retested without
	 *  hand-editing the settings.properties. Added to the panel only
	 *  when the plugin detects a non-launcher (source) build. */
	void addResetWizardButton()
	{
		SwingUtilities.invokeLater(() ->
		{
			content.add(createSpacer(12));
			JLabel devLabel = new JLabel("Dev tools:");
			devLabel.setForeground(Color.LIGHT_GRAY);
			devLabel.setAlignmentX(LEFT_ALIGNMENT);
			content.add(devLabel);

			content.add(createSpacer(4));

			JButton resetWizard = new JButton("Reset Setup Wizard");
			resetWizard.setAlignmentX(LEFT_ALIGNMENT);
			resetWizard.addActionListener(e -> plugin.triggerResetSetupWizard());
			content.add(resetWizard);

			content.revalidate();
			content.repaint();
		});
	}

	void setStatus(String text)
	{
		SwingUtilities.invokeLater(() ->
		{
			statusLabel.setText(text);
			confirmResetBox.setSelected(plugin.isResetConfirmed());
		});
		refresh();
	}

	/**
	 * Paint a synchronous placeholder list using just the known tag names, so the
	 * panel has all rows immediately even before the client-thread presence
	 * check completes.
	 */
	private void renderEmptyTagList()
	{
		tagListPanel.removeAll();
		for (String tagName : plugin.knownTagNames())
		{
			JLabel row = new JLabel("\u00B7 " + SkillBankData.displayName(tagName));
			row.setForeground(Color.LIGHT_GRAY);
			tagListPanel.add(row);
		}
		tagListPanel.revalidate();
		tagListPanel.repaint();
	}

	/**
	 * Ask the plugin to read tag presence on the client thread, then update the
	 * panel on the EDT once results are back. Called by the panel on construction
	 * and after status updates, and by the plugin on every transition into
	 * GameState.LOGGED_IN so the panel populates once the item cache is ready.
	 */
	void refresh()
	{
		plugin.requestTagPresence(this::applyTagPresence);
		updateDependencyBanner();
		refreshNamingButton();
	}

	/** Brief #91: show "Update naming scheme" only while on the legacy scheme. */
	void refreshNamingButton()
	{
		SwingUtilities.invokeLater(() -> namingButton.setVisible(plugin.isLegacyNamingScheme()));
	}

	@Override
	public void onActivate()
	{
		// Re-evaluate the dependency banner each time the player opens
		// the side panel — covers the case where they fix Bank Tag
		// Layouts settings mid-session and come back to verify.
		updateDependencyBanner();
	}

	private void updateDependencyBanner()
	{
		SkillBankPlugin.SetupDependencyState state = plugin.getSetupDependencyState();
		final String text;
		switch (state)
		{
			case NEEDS_INSTALL:
				text = "<html><b>⚠ Required:</b> Install \"Bank Tag Layouts\" "
					+ "from the Plugin Hub. This plugin won't display "
					+ "correctly without it.</html>";
				break;
			case NEEDS_CONFIG:
				text = "<html><b>⚠ Required:</b> Open Bank Tag Layouts "
					+ "settings and enable \"Enable layout by default.\""
					+ "</html>";
				break;
			default:
				text = null;
				break;
		}
		SwingUtilities.invokeLater(() ->
		{
			if (text == null)
			{
				dependencyBanner.setVisible(false);
			}
			else
			{
				dependencyBanner.setText(text);
				dependencyBanner.setVisible(true);
			}
			dependencyBanner.revalidate();
			dependencyBanner.repaint();
		});
	}

	private void applyTagPresence(Map<String, Boolean> presence)
	{
		SwingUtilities.invokeLater(() ->
		{
			tagListPanel.removeAll();
			for (Map.Entry<String, Boolean> e : presence.entrySet())
			{
				JLabel row = new JLabel((e.getValue() ? "\u2713 " : "\u00B7 ") + SkillBankData.displayName(e.getKey()));
				row.setForeground(e.getValue() ? new Color(120, 200, 120) : Color.LIGHT_GRAY);
				tagListPanel.add(row);
			}
			tagListPanel.revalidate();
			tagListPanel.repaint();
		});
	}

	private static JPanel createSpacer(int height)
	{
		JPanel s = new JPanel();
		s.setOpaque(false);
		s.setPreferredSize(new Dimension(0, height));
		s.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
		return s;
	}
}
