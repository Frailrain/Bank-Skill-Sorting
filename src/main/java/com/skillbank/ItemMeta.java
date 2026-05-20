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

	public ItemMeta(int tier, String weaponClass, String slot, String category,
		Map<String, String> sections)
	{
		this.tier = tier;
		this.weaponClass = weaponClass;
		this.slot = slot;
		this.category = category;
		this.sections = sections;
	}
}
