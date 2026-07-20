package com.skillbank;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Brief #91: draws small section descriptions above each loadout group's
 * first row on the dynamic slayer tab ("Task &amp; protection",
 * "Melee — wiki pick", "Potions", "Everything else"...). Core Bank Tags
 * layouts can't carry text, so the labels are rendered as an overlay,
 * anchored to the bank slot widgets of the laid-out positions.
 */
@Slf4j
@Singleton
public class SlayerTabOverlay extends Overlay
{
	private static final Color HEADER_COLOR = new Color(255, 255, 255, 230);
	private static final Color LINE_COLOR = new Color(160, 160, 160, 170);

	private final Client client;
	private final SkillBankPlugin plugin;
	private final SlayerLoadoutBuilder loadoutBuilder;
	private final SkillBankLayoutBuilder layoutBuilder;

	@Inject
	SlayerTabOverlay(Client client, SkillBankPlugin plugin,
		SlayerLoadoutBuilder loadoutBuilder, SkillBankLayoutBuilder layoutBuilder)
	{
		this.client = client;
		this.plugin = plugin;
		this.loadoutBuilder = loadoutBuilder;
		this.layoutBuilder = layoutBuilder;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	private String lastDebug;

	private void debugOnce(String state)
	{
		if (!state.equals(lastDebug))
		{
			lastDebug = state;
			log.debug("[SkillBank] TabOverlay state: {}", state);
		}
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		// Read the active tab THROUGH the plugin — the overlay's own
		// injected TabInterface observed null while the plugin's instance
		// tracked the live tab.
		String tabId = plugin.activeManagedTabId();
		if (tabId == null)
		{
			debugOnce("no-tab");
			return null;
		}
		String activeTag = SkillBankData.bankTagName(tabId);
		// Slayer prefers the dynamic loadout's headers when a task layout
		// is live; every other tab (and static slayer) uses the section
		// dividers from the generic layout builder.
		Map<Integer, String> headers =
			SkillBankData.TAG_SLAYER.equals(tabId) && !loadoutBuilder.headers().isEmpty()
				? loadoutBuilder.headers()
				: layoutBuilder.headersFor(activeTag);
		if (headers.isEmpty())
		{
			debugOnce("empty-headers tab=" + tabId + " tag=" + activeTag);
			return null;
		}
		Widget itemContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		if (itemContainer == null || itemContainer.isHidden())
		{
			debugOnce("no-container tab=" + tabId);
			return null;
		}
		Widget[] slots = itemContainer.getDynamicChildren();
		if (slots == null)
		{
			debugOnce("no-children tab=" + tabId);
			return null;
		}
		debugOnce("drawing tab=" + tabId + " headers=" + headers.size()
			+ " slots=" + slots.length);
		Rectangle clip = itemContainer.getBounds();
		graphics.setFont(FontManager.getRunescapeSmallFont());
		int rowHeight = 36; // bank grid row pitch (32px item + 4px gap)
		for (Map.Entry<Integer, String> e : headers.entrySet())
		{
			int pos = e.getKey();
			if (pos < 0 || pos >= slots.length)
			{
				continue;
			}
			Widget slot = slots[pos];
			if (slot == null || slot.isHidden())
			{
				continue;
			}
			Rectangle b = slot.getBounds();
			// The divider lives in the blank row ABOVE the group's first
			// item — Quest Helper style: label text, then a rule across the
			// remaining width. Text x aligns with the item grid; text and
			// line share one vertical centre so they can't drift apart.
			int baseline = b.y - rowHeight / 2 + 4;
			int lineY = baseline - 4;
			if (lineY < clip.y + 6 || lineY > clip.y + clip.height - 4)
			{
				continue;
			}
			String label = e.getValue();
			int textX = b.x;
			graphics.setColor(new Color(0, 0, 0, 170));
			graphics.drawString(label, textX + 1, baseline + 1);
			graphics.setColor(HEADER_COLOR);
			graphics.drawString(label, textX, baseline);
			int textWidth = graphics.getFontMetrics().stringWidth(label);
			int lineStart = textX + textWidth + 8;
			int lineEnd = clip.x + clip.width - 12;
			if (lineEnd > lineStart)
			{
				graphics.setColor(LINE_COLOR);
				graphics.drawLine(lineStart, lineY, lineEnd, lineY);
			}
		}
		return null;
	}
}
