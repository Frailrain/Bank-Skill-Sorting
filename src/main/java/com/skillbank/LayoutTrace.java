package com.skillbank;

/**
 * Diagnostic trace produced by {@link SkillBankLayoutBuilder#traceLayout}.
 * Mirrors the structure of the real Layout build (sections, zones,
 * per-item placements) as plain text so we can dump it to a log file
 * and compare against the in-game bank visually.
 * <p>
 * Not used at runtime — only the side-panel "Dump layout trace" button
 * (Brief #59) writes one out.
 */
public final class LayoutTrace
{
	private final String tag;
	private final StringBuilder body = new StringBuilder();
	private int placedCount = 0;

	LayoutTrace(String tag)
	{
		this.tag = tag;
	}

	void header(int ownedInTab)
	{
		body.append("=== ").append(tag)
			.append(" (").append(ownedInTab).append(" owned items) ===\n");
	}

	void section(String name, int zone, int count, String sortMethod)
	{
		body.append("\nSection: ").append(name)
			.append(" (zone ").append(zone)
			.append(", ").append(count).append(" items, sortedBy=")
			.append(sortMethod).append(")\n");
	}

	void item(int pos, int id, String name, ItemMeta m, int zone)
	{
		placedCount = pos + 1;
		body.append("  pos ").append(pos).append(": ").append(name)
			.append(" (id=").append(id).append(")");
		if (m != null)
		{
			body.append(" tier=").append(m.tier)
				.append(" wc=").append(m.weaponClass)
				.append(" slot=").append(m.slot)
				.append(" cat=").append(m.category);
		}
		else
		{
			body.append(" [no metadata]");
		}
		body.append(" zone=").append(zone).append("\n");
	}

	void note(String msg)
	{
		body.append("[note] ").append(msg).append("\n");
	}

	public String getTag()
	{
		return tag;
	}

	public int getPlacedCount()
	{
		return placedCount;
	}

	public String render()
	{
		return body.toString();
	}
}
