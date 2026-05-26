package com.skillbank;

import java.util.Map;

/**
 * Per-item metadata used by {@link SkillBankLayoutBuilder} to decide
 * section placement and tier ranking at runtime. Produced offline by
 * tools/skillbank-data/llm_promote.py and loaded from the bundled
 * resource {@code /com/skillbank/item-meta.json}.
 * <p>
 * Field names are short to keep the JSON file compact (one entry per
 * item × ~12k items adds up). The Gson deserializer reads them via
 * {@code @com.google.gson.annotations.SerializedName}.
 */
public final class ItemMeta
{
	/** Metal tier (METAL_TIER lookup, e.g. bronze=10, dragon=80) or 0 if N/A. */
	@com.google.gson.annotations.SerializedName("t")
	public final int tier;

	/** osrsbox weapon.weapon_type, e.g. "scimitar", "whip", "longbow". Null for non-weapons. */
	@com.google.gson.annotations.SerializedName("wc")
	public final String weaponClass;

	/** osrsbox equipment.slot, e.g. "weapon", "head", "body", "ammo", "ring". Null for non-equipped. */
	@com.google.gson.annotations.SerializedName("sl")
	public final String slot;

	/** Coarse role bucket — {@code combat}, {@code skill_tool}, {@code consumable},
	 *  {@code material}, {@code cosmetic}, {@code misc}. */
	@com.google.gson.annotations.SerializedName("ct")
	public final String category;

	/** Per-tab section name. Key = tab name (e.g. "melee"), value = section label
	 *  (e.g. "Weapons"). An item appearing in N tabs has N entries here. */
	@com.google.gson.annotations.SerializedName("s")
	public final Map<String, String> sections;

	/** Brief #69: osrsbox equipment.requirements ({@code {"attack":70}} etc.).
	 *  Used by the combat-tab zone partitioner to apply a 60+ level cutoff
	 *  for Zone 1 (loadout) vs Zone 2 (chaff). Null when the item carries
	 *  no requirement data (most non-equipment + many quest items). */
	@com.google.gson.annotations.SerializedName("rq")
	public final Map<String, Integer> requirements;

	/** Brief #75: cosmetic set name. Items sharing the same {@code setName}
	 *  cluster on a single row in the cosmetics tab, with row breaks between
	 *  different sets. Null when the item doesn't belong to a recognised set
	 *  (or for non-cosmetics items, where the field isn't emitted). */
	@com.google.gson.annotations.SerializedName("sn")
	public final String setName;

	public ItemMeta(int tier, String weaponClass, String slot, String category,
		Map<String, String> sections, Map<String, Integer> requirements,
		String setName)
	{
		this.tier = tier;
		this.weaponClass = weaponClass;
		this.slot = slot;
		this.category = category;
		this.sections = sections;
		this.requirements = requirements;
		this.setName = setName;
	}

	/** Look up a skill requirement (e.g. "attack", "defence", "ranged",
	 *  "magic", "strength"). Returns 0 if the item has no requirements
	 *  map or the requested skill is absent. */
	public int requirement(String skill)
	{
		if (requirements == null || skill == null)
		{
			return 0;
		}
		Integer v = requirements.get(skill);
		return v != null ? v : 0;
	}

	/** Brief #72: bean-style alias for {@link #requirement(String)}.
	 *  Empty map means "confirmed: no level required"; null map means
	 *  "unknown — no data from either osrsbox or wiki". Both currently
	 *  return 0 here, which is the right behaviour for the runtime
	 *  zone partitioner (treats missing data as no-req). */
	public int getRequirement(String skill)
	{
		return requirement(skill);
	}

	public Map<String, Integer> getRequirements()
	{
		return requirements;
	}
}
