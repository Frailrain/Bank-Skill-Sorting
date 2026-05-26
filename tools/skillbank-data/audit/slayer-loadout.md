# Slayer tab — curated loadout list

Created 2026-05-25 from Matt's batch-7 audit ruling. The Slayer tab is a HYBRID, curated tab, not derived purely from the inputs-only classifier. It holds the things you actually take on or need for tasks. This is the source of truth for Slayer membership beyond the genuinely slayer-locked items the classifier already tags; apply it as a force-include overlay at build time (per the root-handoff `force_include` design).

Decisions:
- RARE MONSTER DROPS are deliberately SKIPPED for now (e.g. abyssal whip). No clean rule for which qualify; revisit later.
- Items marked [SUBJECTIVE] are Matt's call to keep or trim (personal preference on foods/potions).
- The classifier still tags genuinely slayer-locked items in decisions.jsonl (slayer helm/black mask family, broad ammo, dark totem, slayer ring, boots of stone/brimstone, task-mechanic items below). This list ADDS the loadout consumables and task-enhancing gear on top.

## 1. Task-mechanic items (required to damage/finish specific tasks)
- Bag of salt (rockslugs)
- Rock hammer / granite hammer (gargoyles, Grotesque Guardians)
- Nose peg, facemask, earmuffs, slayer helmet + (i)/coloured variants (already classified; task gas/sound/cockatrice protection)
- Witchwood icon (cave horrors)
- Fungicide spray (+ fungicide refills) (zygomites)
- Spiny helmet (wall beasts)
- Mirror shield / V's shield (cockatrice, basilisk)
- Reinforced goggles (cave kraken)
- Boots of stone / boots of brimstone / granite boots (Karuulm heat) [already Slayer-tagged]
- Insulated boots
- Leaf-bladed sword / spear / battleaxe, broad arrows / broad bolts / amethyst broad bolts (turoth, kurask) [broad ammo already Slayer]
- Ice cooler / slayer bell and similar task-utility items

## 2. Loadout consumables (the inventory you bring)
Potions:
- Prayer potion, Super restore, Sanfew serum (all doses)
- Super combat / Divine super combat, Super attack/strength/defence + divine variants
- Antifire, Super antifire, Extended (super) antifire + barbarian mixes (dragon tasks) <- this is the antifire ruling
- Stamina potion
- Antipoison / Superantipoison / Antivenom / Antivenom+
- Saradomin brew, Xeric's aid
Food [SUBJECTIVE - "top 3 from bank"]:
- Shark, Manta ray, Anglerfish, Dark crab, Karambwan (combo), Saradomin brew pairing
Ammo/other:
- Cannonball + dwarf multicannon set (already loadout per Brief #50)

## 3. Task-enhancing gear (greatly improves a specific task)
- Salve amulet / (e) / (i) / (ei) (undead tasks)
- Arclight, Emberlight, Darklight, Silverlight (demon tasks)
- Demonbane spells/weapons; Burning claws / Scorching bow (demons)
- Leaf-bladed weapons (turoth/kurask) [also in section 1]
- Slayer helm (i) / black mask (i) (on-task damage; already classified)
- Boots of brimstone, Brimstone ring (already classified)
- Wilderness / specific-monster gear that is the meta for a task (note as encountered)

## 4. Deferred (NOT included yet)
- Generic rare monster drops from slayer monsters (abyssal whip, dragon boots, etc.). Skip until a rule is agreed.

## Implementation note
Once Matt trims this, apply as a force-include overlay so these items appear in the Slayer tab regardless of their primary classification, while remaining in their functional tab too (e.g. antifire stays Herblore + gains Slayer; shark stays Cooking + gains Slayer). Do NOT scatter ~200 individual Slayer tags through decisions.jsonl.
