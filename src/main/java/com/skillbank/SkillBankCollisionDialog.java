package com.skillbank;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import net.runelite.client.ui.ColorScheme;

/**
 * Brief #89: modal dialog shown when first-time seeding finds tabs whose names
 * already exist in the player's bank. Lists each colliding tab and lets the
 * player choose, per tab, whether to keep theirs ({@link Decision#SKIP}, the
 * pre-selected safe default), replace it with the auto-sorted version
 * ({@link Decision#OVERWRITE}), or seed the plugin's version alongside under a
 * "(Auto)" suffix ({@link Decision#ALONGSIDE}).
 * <p>
 * The dialog is purely a chooser: it collects decisions and hands them back via
 * the {@code onApply} callback (invoked on the EDT). All bank-tag mutation
 * happens in {@link SkillBankPlugin} on the client thread. Construct and show
 * on the Swing event-dispatch thread.
 */
class SkillBankCollisionDialog extends JDialog
{
	enum Decision
	{
		SKIP,
		OVERWRITE,
		ALONGSIDE,
	}

	private final List<String> bases;
	private final Map<String, JRadioButton> skipButtons = new LinkedHashMap<>();
	private final Map<String, JRadioButton> overwriteButtons = new LinkedHashMap<>();
	private final Map<String, JRadioButton> alongsideButtons = new LinkedHashMap<>();
	private final Consumer<Map<String, Decision>> onApply;

	/**
	 * @param owner     parent window for centring/modality (may be null)
	 * @param bases     standardized base tag names that collided, in display order
	 * @param displayFn maps a base name to its human-readable label
	 * @param onApply   receives the per-base decisions when the player confirms
	 */
	SkillBankCollisionDialog(Window owner, List<String> bases,
		Function<String, String> displayFn, Consumer<Map<String, Decision>> onApply)
	{
		super(owner, "Auto Bank Sorter — existing tabs found", ModalityType.APPLICATION_MODAL);
		this.bases = bases;
		this.onApply = onApply;
		// Closing via the window "X" records no decision — the unresolved
		// collisions are simply re-offered on a later seed. Safe: nothing is
		// changed without an explicit Apply / Skip All.
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		JPanel root = new JPanel(new BorderLayout(0, 8));
		root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		root.setBackground(ColorScheme.DARK_GRAY_COLOR);

		root.add(buildHeader(), BorderLayout.NORTH);
		root.add(buildRowsScroller(displayFn), BorderLayout.CENTER);
		root.add(buildButtonBar(), BorderLayout.SOUTH);

		setContentPane(root);
		// Cap the height so a config with many collisions stays usable; the
		// row list scrolls inside the cap.
		pack();
		Dimension pref = getPreferredSize();
		if (pref.height > 520)
		{
			setSize(new Dimension(Math.max(pref.width, 420), 520));
		}
		setLocationRelativeTo(owner);
	}

	private JLabel buildHeader()
	{
		JLabel header = new JLabel(
			"<html><body style='width:380px'>"
				+ "<b>" + bases.size() + " of your bank tabs already exist.</b><br>"
				+ "The plugin won't change them unless you say so. Choose what to do "
				+ "with each — <b>Skip</b> keeps your tab exactly as-is."
				+ "</body></html>");
		header.setForeground(Color.WHITE);
		return header;
	}

	private JScrollPane buildRowsScroller(Function<String, String> displayFn)
	{
		JPanel rows = new JPanel();
		rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
		rows.setBackground(ColorScheme.DARK_GRAY_COLOR);

		for (String base : bases)
		{
			rows.add(buildRow(base, displayFn.apply(base)));
			rows.add(javax.swing.Box.createVerticalStrut(4));
		}

		JScrollPane scroller = new JScrollPane(rows,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setBorder(BorderFactory.createEmptyBorder());
		scroller.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);
		scroller.getVerticalScrollBar().setUnitIncrement(16);
		return scroller;
	}

	private JPanel buildRow(String base, String display)
	{
		JPanel row = new JPanel(new BorderLayout(0, 2));
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
		row.setAlignmentX(Component.LEFT_ALIGNMENT);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));

		JLabel name = new JLabel(display);
		name.setFont(name.getFont().deriveFont(Font.BOLD));
		name.setForeground(Color.WHITE);
		row.add(name, BorderLayout.NORTH);

		JRadioButton skip = makeRadio("Skip (keep mine)", true);
		JRadioButton overwrite = makeRadio("Overwrite", false);
		JRadioButton alongside = makeRadio("Create alongside", false);

		ButtonGroup group = new ButtonGroup();
		group.add(skip);
		group.add(overwrite);
		group.add(alongside);

		skipButtons.put(base, skip);
		overwriteButtons.put(base, overwrite);
		alongsideButtons.put(base, alongside);

		JPanel choices = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		choices.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		choices.add(skip);
		choices.add(overwrite);
		choices.add(alongside);
		row.add(choices, BorderLayout.CENTER);

		return row;
	}

	private static JRadioButton makeRadio(String text, boolean selected)
	{
		JRadioButton b = new JRadioButton(text, selected);
		b.setForeground(Color.LIGHT_GRAY);
		b.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		b.setFocusPainted(false);
		return b;
	}

	private JPanel buildButtonBar()
	{
		JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		bar.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JButton skipAll = new JButton("Skip All");
		skipAll.setToolTipText("Keep every existing tab untouched");
		skipAll.addActionListener(e -> applyAllSkip());

		JButton apply = new JButton("Apply");
		apply.addActionListener(e -> applySelections());

		bar.add(skipAll);
		bar.add(apply);
		return bar;
	}

	private void applyAllSkip()
	{
		Map<String, Decision> decisions = new LinkedHashMap<>();
		for (String base : bases)
		{
			decisions.put(base, Decision.SKIP);
		}
		finish(decisions);
	}

	private void applySelections()
	{
		Map<String, Decision> decisions = new LinkedHashMap<>();
		for (String base : bases)
		{
			final Decision d;
			if (overwriteButtons.get(base).isSelected())
			{
				d = Decision.OVERWRITE;
			}
			else if (alongsideButtons.get(base).isSelected())
			{
				d = Decision.ALONGSIDE;
			}
			else
			{
				d = Decision.SKIP;
			}
			decisions.put(base, d);
		}
		finish(decisions);
	}

	private void finish(Map<String, Decision> decisions)
	{
		// Dispose before invoking the callback so the modal blockage is
		// already released when the plugin posts its follow-up work.
		dispose();
		onApply.accept(decisions);
	}
}
