package com.skillbank;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Brief #91: static lookup for the per-task slayer dataset scraped from the
 * OSRS wiki (tools/skillbank-data/slayer_tasks.py), bundled as
 * {@code /com/skillbank/slayer-tasks.json}. Keyed by normalised task name —
 * the same string RuneLite's core Slayer plugin stores in its RSProfile
 * config ({@code slayer.taskName}) — with singular/plural tolerance because
 * wiki page titles and in-game task names disagree on pluralisation.
 */
@Slf4j
public final class SlayerTaskData
{
	private SlayerTaskData()
	{
	}

	public static class ItemRef
	{
		public String name;
		public Integer id;
	}

	public static class TaskLocation
	{
		public String name;
		public Boolean cannon;
		public Boolean multicombat;
	}

	public static class Weakness
	{
		public String monster;
		public String element;
		public int percent;
	}

	/** v3: a location where one specific variant is fought, with the
	 *  requirements that apply ONLY there (Boots of stone at Karuulm). */
	public static class VariantLocation
	{
		public String name;
		public Boolean cannon;
		@SerializedName("required_items")
		public List<ItemRef> requiredItems;
		public String notes;
	}

	/** v3: one monster variant a task page covers (Wyrms page → Wyrm and
	 *  Lava strykewyrm), each with its own weakness, requirements, and
	 *  locations. Extraction scoped these per the page text — never
	 *  promoted across variants or locations. */
	public static class Variant
	{
		public String name;
		public List<String> aliases;
		@SerializedName("elemental_weakness")
		public String elementalWeakness;
		@SerializedName("required_items")
		public List<ItemRef> requiredItems;
		@SerializedName("protection_items")
		public List<ItemRef> protectionItems;
		public List<VariantLocation> locations;
	}

	public static class Task
	{
		public String name;
		@SerializedName("task_item_id")
		public Integer taskItemId;
		@SerializedName("slayer_req")
		public int slayerReq;
		@SerializedName("combat_req")
		public int combatReq;
		@SerializedName("recommended_style")
		public String recommendedStyle;
		@SerializedName("protection_prayer")
		public String protectionPrayer;
		@SerializedName("required_items")
		public List<ItemRef> requiredItems;
		@SerializedName("protection_items")
		public List<ItemRef> protectionItems;
		@SerializedName("recommended_items")
		public List<ItemRef> recommendedItems;
		public List<String> attributes;
		@SerializedName("burst_viable")
		public boolean burstViable;
		@SerializedName("cannon_viable")
		public boolean cannonViable;
		public List<TaskLocation> locations;
		/** Wiki {{Recommended equipment}} data: style → slot → ranked list,
		 *  where each rank holds one or more alternatives ("A / B"). Parsed
		 *  deterministically from the task/monster page; empty for tasks
		 *  whose pages carry no template. */
		public Map<String, Map<String, List<List<ItemRef>>>> styles;
		@SerializedName("style_order")
		public List<String> styleOrder;
		@SerializedName("elemental_weaknesses")
		public List<Weakness> elementalWeaknesses;

		public List<Variant> variants;

		public boolean hasWikiLoadout()
		{
			return styles != null && !styles.isEmpty()
				&& styleOrder != null && !styleOrder.isEmpty();
		}

		/** The variant matching the in-game task name (by name or alias,
		 *  plural-tolerant); the page's primary variant when nothing
		 *  matches; null when the data carries no variants (legacy). */
		public Variant variantFor(String taskName)
		{
			if (variants == null || variants.isEmpty())
			{
				return null;
			}
			if (taskName != null)
			{
				String n = normalize(taskName);
				for (Variant v : variants)
				{
					if (v == null)
					{
						continue;
					}
					if (nameMatches(v.name, n))
					{
						return v;
					}
					if (v.aliases != null)
					{
						for (String alias : v.aliases)
						{
							if (nameMatches(alias, n))
							{
								return v;
							}
						}
					}
				}
			}
			return variants.get(0);
		}

		private static boolean nameMatches(String candidate, String normalizedTask)
		{
			if (candidate == null)
			{
				return false;
			}
			String c = normalize(candidate);
			return c.equals(normalizedTask)
				|| (c + "s").equals(normalizedTask)
				|| c.equals(normalizedTask + "s");
		}

		public boolean hasAttribute(String attr)
		{
			return attributes != null && attributes.contains(attr);
		}
	}

	private static class Root
	{
		List<Task> tasks;
	}

	private static volatile Map<String, Task> TASKS_BY_NAME;

	/** Idempotent lazy init using the runtime's injected Gson (plugin-hub
	 *  forbids direct Gson construction). */
	public static void init(Gson gson)
	{
		if (TASKS_BY_NAME != null)
		{
			return;
		}
		synchronized (SlayerTaskData.class)
		{
			if (TASKS_BY_NAME == null)
			{
				TASKS_BY_NAME = load(gson);
			}
		}
	}

	private static Map<String, Task> load(Gson gson)
	{
		try (InputStream in = SlayerTaskData.class.getResourceAsStream("/com/skillbank/slayer-tasks.json"))
		{
			if (in == null)
			{
				log.warn("slayer-tasks.json resource missing; dynamic slayer tab disabled");
				return Collections.emptyMap();
			}
			Root root = gson.fromJson(
				new InputStreamReader(in, StandardCharsets.UTF_8), Root.class);
			Map<String, Task> byName = new HashMap<>();
			if (root != null && root.tasks != null)
			{
				for (Task t : root.tasks)
				{
					if (t == null || t.name == null)
					{
						continue;
					}
					String key = normalize(t.name);
					byName.put(key, t);
					// Singular/plural aliases; putIfAbsent so an exact task
					// name always wins over another task's alias.
					if (key.endsWith("s"))
					{
						byName.putIfAbsent(key.substring(0, key.length() - 1), t);
					}
					else
					{
						byName.putIfAbsent(key + "s", t);
					}
				}
			}
			log.info("Loaded {} slayer tasks ({} lookup keys)",
				root != null && root.tasks != null ? root.tasks.size() : 0, byName.size());
			return Collections.unmodifiableMap(byName);
		}
		catch (Exception e)
		{
			log.warn("Failed to load slayer-tasks.json", e);
			return Collections.emptyMap();
		}
	}

	/** In-game task names that don't have their own wiki task page but map
	 *  cleanly onto a covered task's loadout (variant assignments). */
	private static final Map<String, String> TASK_ALIASES = Map.of(
		"lava strykewurm", "wyrms",
		"lava strykewurms", "wyrms",
		"lava strykewyrm", "wyrms",
		"lava strykewyrms", "wyrms"
	);

	/** The task record for an in-game task name (e.g. "Aberrant spectres"),
	 *  or null when unknown / data not loaded. */
	public static Task forTaskName(String taskName)
	{
		Map<String, Task> m = TASKS_BY_NAME;
		if (m == null || taskName == null || taskName.trim().isEmpty())
		{
			return null;
		}
		String n = normalize(taskName);
		n = TASK_ALIASES.getOrDefault(n, n);
		Task t = m.get(n);
		if (t != null)
		{
			return t;
		}
		if (n.endsWith("s"))
		{
			return m.get(n.substring(0, n.length() - 1));
		}
		return m.get(n + "s");
	}

	private static String normalize(String s)
	{
		return s.trim().toLowerCase(Locale.ROOT);
	}
}
