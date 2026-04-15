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

	SkillBankPanel(SkillBankPlugin plugin)
	{
		super(false);
		this.plugin = plugin;

		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel title = new JLabel("Skill Bank Tabs");
		title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
		title.setForeground(Color.WHITE);
		title.setAlignmentX(LEFT_ALIGNMENT);
		content.add(title);

		content.add(createSpacer(8));

		JLabel help = new JLabel(
			"<html>Seeds missing Bank Tags groups.<br>"
				+ "Existing tags are never overwritten.</html>");
		help.setForeground(Color.LIGHT_GRAY);
		help.setAlignmentX(LEFT_ALIGNMENT);
		content.add(help);

		content.add(createSpacer(10));

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

		refreshTagList();
	}

	void setStatus(String text)
	{
		SwingUtilities.invokeLater(() ->
		{
			statusLabel.setText(text);
			confirmResetBox.setSelected(plugin.isResetConfirmed());
			refreshTagList();
		});
	}

	private void refreshTagList()
	{
		tagListPanel.removeAll();
		Map<String, Boolean> presence = plugin.currentTagPresence();
		for (Map.Entry<String, Boolean> e : presence.entrySet())
		{
			JLabel row = new JLabel((e.getValue() ? "\u2713 " : "\u00B7 ") + e.getKey());
			row.setForeground(e.getValue() ? new Color(120, 200, 120) : Color.LIGHT_GRAY);
			tagListPanel.add(row);
		}
		tagListPanel.revalidate();
		tagListPanel.repaint();
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
