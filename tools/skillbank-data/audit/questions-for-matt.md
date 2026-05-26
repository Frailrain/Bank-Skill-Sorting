# Questions for Matt

This file collects design questions and taxonomy ambiguities that come up during the item classification audit. I add a section whenever something affects many items or needs your judgment. Answer in batches when it suits you. Unless you say otherwise, your answers apply to future decisions only, not to decisions already written.

---

## 2026-05-22: Initial approach (sanity-check pause after first 5 items)

This is the checkpoint the brief asked for before I commit to the full run. Here is how I am working so far.

### How I am reading the decision rule

For every (item, tab) pair I ask the single question from the brief: would a player opening this tab in their normal workflow expect to find this item there. If yes, the tab goes in the item's list. If no, it stays out. When a case is genuinely balanced, I take the tighter option and leave the tab out, since a player can hand-tag an edge case but cannot easily clean up a cluttered tab. I am treating the brief's decision rule as the authority, not the previous classifier's heuristics in mapping.py, because that heuristic pass is what produced the misplacements we are correcting.

### Sources I consulted for the first five

These first five items (IDs 0 to 4) are all Dwarf Cannon content and are well known, so I did not need web search for any of them. For context I read the project README, make_audit_input.py (to confirm what each input field means), sort_tables.py (the per-tab section structures), and out/classifier-reasoning.md (the prior pass's own trace of flagged misclassifications, which shows exactly how the loose rules failed). When an item is unfamiliar or its stats are ambiguous, I will use the OSRS Wiki first, then r/2007scape or wiki forum threads, capped at three searches per item before the item goes to uncertain.jsonl.

### Early observations about the data

The first item is ID 0 (Dwarf remains), not ID 1. The brief said to begin at item ID 1, but a hard rule says every input item gets a decision, so I am starting from the very first line and will process all of them in ascending ID order.

The input only contains items that the previous pass had placed in at least one tab. make_audit_input.py builds the file from the flattened LLM-plus-mapping result, so any item the prior pass left untagged is not in this file. The audit therefore covers the items that already carry tags, which is the set we want to clean up.

The current_tabs field on each line is the previous pass's classification. That is the baseline I compare against for the changed column in the batch CSV.

### A note on how I am running this

My code sandbox cannot see your project files (a mount quirk), so I am working entirely through direct file reads and writes rather than a Python script. That is fine at this data size. Practically it means I read the input in chunks and append to the output files as I go. Progress is fully resumable: on any restart I read PROGRESS.md and the tail of decisions.jsonl to find the last completed ID and continue from the next one. If a clean way to give the sandbox access to the files turns up, I can switch to a faster script-driven pass, but it is not required.

### Design question 1: the dwarf multicannon and its ammunition and parts

The cannon is a combat tool that is not tied to a single combat style. Players use it during melee, ranged, and magic training, and very heavily during slayer. Its ammunition (steel cannonball, and later granite cannonball) is made by Smithing. This affects a whole family of items: the cannonballs, the cannon parts (barrel, base, stand, furnace), and the coloured or gold cannon variants.

My interim rule, which I used on Steel cannonball (ID 2): cannonballs go in mining_smithing because they are a Smithing product, and in slayer because the cannon is a staple slayer-task accelerator. I removed cannonballs from range, because a cannon is not ranged-combat-style equipment the way a bow or bolts are, and I do not think a player browsing the range tab expects to find cannonballs there. For the cannon parts themselves I am leaning slayer plus misc.

Is that the split you want? The main judgment call is whether cannon ammo and parts belong in range at all, and whether the smithing tag on cannonballs is worth keeping or is clutter. Tell me and I will apply it to every cannon item from here forward.

### Design question 2: quest-obtained items that are also permanent tools or gear

Some items are obtained during a quest but have ongoing skill or combat use afterward. Ammo mould (ID 4) is the first example: you get it in Dwarf Cannon, but it is the permanent tool that casts cannonballs, and it is repurchasable. The previous pass tagged it both quests and mining_smithing.

My interim rule: if an item has enduring function, I classify it by that function and drop the quests tag, because a player opening the quests tab is looking for quest keepsakes and quest-locked gear, not a tool they use every smithing trip. Pure quest items with no ongoing use (Dwarf remains, Toolkit, Nulodion's notes) stay in quests only. So I put Ammo mould in mining_smithing alone and dropped quests.

Is that the policy you want for the whole audit? The alternative is to keep the quests tag on anything quest-obtained even when it has a functional home. This decision will touch a lot of items, so I want your call early.

---

## 2026-05-22: Answers received (cannon rule, quest-tool policy, run cadence)

You answered the first three questions. Recording the resulting rules so they apply to every item from here forward.

Cannon family: cannonballs go in mining_smithing, range, and slayer. Cannon parts (barrel, base, stand, furnace, and the coloured or gold variants) go in range and slayer. This reversed my initial call on Steel cannonball (ID 2), where I had dropped range; it is now back to mining_smithing, range, slayer, which happens to match its previous classification. The ammo mould stays in mining_smithing only, since it is a Smithing tool rather than a deployed cannon part.

Quest-obtained tools and gear: classify by function and drop the quests tag when the item has enduring skill or combat use. Pure quest items with no ongoing use stay in quests. This confirmed Ammo mould (ID 4) as mining_smithing only.

Run cadence: I process 10% of the dataset per batch, about 1,188 items. At the end of each batch I produce a 5% audit slice, about 59 items, weighted toward changed rows and lower-confidence calls, written to audit-slice-batch-NNN.csv with empty approved and comments columns. I then wait for your audit. I apply your corrections to decisions.jsonl and record them in corrections-batch-NNN.csv, and only then start the next batch. If your audit produces a major logic change, I re-scan already-processed items for that pattern and re-categorize where it applies, logging those revisions in the corrections file.

---

## 2026-05-22: Design question 3 (high impact): crafting intermediates and unfinished items in combat tabs

This one affects a large number of items, so please check it during your batch 1 audit. Early in the ID range there is a big cluster of intermediate and unfinished items: arrowtips (smithed from a bar, then fletched onto arrows), bolt tips (cut from gems), and unstrung bows (cut from logs, not wieldable until strung). The previous pass tagged many of these into range, and it did so inconsistently. Longbow (u) and Shortbow (u) had no range tag, but Oak shortbow (u) and every unstrung bow above it did. Arrowtips were all tagged range.

My interim rule: an item that cannot be equipped or fired in its current form is a skilling material, not combat equipment. So arrowtips go in mining_smithing and wc_fletching but not range. Bolt tips and unstrung bows go in wc_fletching only. The finished, usable item (the strung bow, the completed arrow or bolt) is what earns the range tag. This keeps the range tab to things a ranged player actually equips or fires, and it keeps the fletching and smithing tabs as the home for the materials.

The alternative, which is closer to what the previous pass did, is to also tag these intermediates into range so that everything in an ammo or bow production chain shows up under range. I think that clutters the range tab, but it is your call. If you want intermediates kept in range, tell me and I will re-scan the items already processed and add the tag back. The items affected so far are IDs 39 to 47 (arrowtips and bolt tips) and the unstrung bows at 54, 56, 58, 60, 62, 64, 66, plus 48 and 50 which were already range-free.

---

## 2026-05-22: Tab structure check and the inputs-only rule (needs your scope decision)

You asked whether the assembling Claude Code had moved fletching to its own tab and merged woodcutting with firemaking. It has not. The live code still defines wc_fletching (woodcutting and fletching combined) and a separate firemaking tab, and the section tables in sort_tables.py match. Restructuring tabs is a code change I am not permitted to make, so for now I am auditing against the 22 tabs that actually exist. If you later have CC split fletching out and merge woodcutting with firemaking, my fletching, woodcutting, and firemaking decisions remap mechanically and I can produce that remap.

You then gave the inputs-only rule: an item belongs in the tab of the skill that consumes it as an input, not the skill that produces it as an output, unless the producing skill uses it again for a further same-skill step. I applied the unambiguous part now: arrowtips (IDs 39 to 44) leave both range and mining_smithing and sit in wc_fletching only, because their only next step is fletching. Bolt tips and unstrung bows already complied, since their next step is also fletching.

The open question is scope, and it is large enough that I paused the run for it. The current plugin design and the brief both put finished outputs in skill tabs on purpose. sort_tables.py has sections named Smithed weapons, Smithed armour, Cooked food, Finished potions, Strung bows, and Finished arrows, darts and bolts, and the brief's tab definitions list smithed equipment, crafted equipment, cooked food, and finished potions. A strict inputs-only reading empties all of those sections and moves the finished items to wherever they are used. That is a real and defensible design, but it contradicts what is currently built, so I want your explicit call before I apply it to thousands of items. The three options I am weighing are recorded with you in chat.

---

## 2026-05-22: Governing ruleset for the restart (Brief #63 taxonomy)

This section supersedes the earlier interim rules. It is the single source of truth for the restarted run.

Taxonomy. After the restructure there are 23 tabs. My decisions use the display names exactly as the ingest expects them: Melee, Range, Mage, Prayer, Cooking, Woodcutting + Firemaking, Fletching, Fishing, Firemaking, Crafting, Mining + Smithing, Herblore, Agility + Thieving, Slayer, Farming, Runecraft, Hunter, Construction, Misc, Quests, Sailing, Cosmetics, Teleports.

Classification rule, your inputs-only principle. An item belongs in the tab of the skill or activity that uses it as an input: the materials, tools, and consumables for that skill, plus the equipment used in that combat style. A skill's own finished output does not stay in that skill's tab; it moves to where it is used. The one carve-out is a further same-skill step. A bar stays in Mining + Smithing because it is smithed again. An unstrung bow, arrowtips, shafts, and bolt tips stay in Fletching because they are fletched again. Dragonhide vambraces stay in Crafting because spikes can be added.

Exempt tabs that keep their own outputs: Cooking, Herblore, Farming, Hunter, and Runecraft. Crafting is a partial exemption, keeping inputs plus unenchanted jewellery, while enchanted jewellery leaves to its use tab.

Worked example. Iron ore goes to Mining + Smithing because Smithing smelts it. Iron bar stays in Mining + Smithing because of the further smithing step. Iron arrowtips leave Mining + Smithing and sit in Fletching, the consuming skill. The finished iron arrow leaves Fletching and sits in Range.

Note on stale sections. sort_tables.py still lists sections like Smithed weapons, Smithed armour, Strung bows, and Finished arrows under skill tabs. Under the inputs-only rule those become empty, since the finished items move to their use tabs. Reworking the section structure is a CC job; it does not change my tab-membership decisions.

Restart. Per Matt's note I am starting from ID 0 and rewriting decisions.jsonl and the batch CSV under this taxonomy and rule. The earlier 50 decisions are superseded.

Two items I need settled before the run, asked in chat:

A. Firemaking versus Woodcutting + Firemaking. The code kept a separate Firemaking tab next to the new merged Woodcutting + Firemaking, and the regenerated input still puts some items such as Candle in Firemaking alone. Since the request was to combine woodcutting and firemaking, should Firemaking be treated as deprecated with all of that content living in Woodcutting + Firemaking, or should both tabs be kept?

B. Cannonballs and other terminal ammo outputs. Under strict inputs-only a steel cannonball is a Mining + Smithing output with no further smithing step, so it would leave Mining + Smithing and sit in Range and Slayer only. But Mining + Smithing has a section named Cannonballs and ammo outputs, and the earlier cannon ruling placed cannonballs in Mining + Smithing. Do cannonballs stay in Mining + Smithing as an explicit ammo-output exception, or drop to Range and Slayer like other terminal outputs?

### Resolutions (2026-05-22)

A. Firemaking is being removed by Claude Code. The standalone Firemaking tab goes away and its content consolidates into Woodcutting + Firemaking. The taxonomy on resume will be 22 tabs with no Firemaking. I am holding the run until Matt confirms that change is in and the input is regenerated.

B. Cannonballs drop to Range and Slayer. Strict inputs-only applies: a steel or granite cannonball leaves Mining + Smithing and sits in Range and Slayer only. This generalises to other terminal ammo outputs that have no further same-skill step. The ammo mould stays in Mining + Smithing because it is a smithing tool, an input.

### Working sub-rule for combat potions (for your audit)

Potions recur constantly, so I am applying a consistent sub-rule. Unfinished potions stay in Herblore only. Finished boost potions go in Herblore plus the combat tab for the stat they boost: Attack, Strength, and Defence potions get Melee, and the super versions and other task staples also get Slayer. Prayer-restoring potions get Herblore, Prayer, and Slayer. Plain restore potions, which only restore reduced stats, stay in Herblore only rather than a combat tab, since they are a utility consumable rather than a style loadout item. Flagging so you can confirm or adjust the combat-tab spread during the batch 1 audit.

### Working sub-rule for herbs (for your audit)

Grimy and clean herbs split differently under inputs-only. A grimy herb is what you harvest from a Farming patch, so it sits in Farming, an exempt tab that keeps its outputs, and in Herblore, where it is cleaned. A clean herb is the product of the cleaning step and the input to making potions, both Herblore actions, and you never harvest clean herbs from a patch, so a clean herb sits in Herblore only. I standardised grimy herbs to Farming + Herblore and clean herbs to Herblore only. This flips several clean herbs the previous pass had tagged with Farming, so it is worth a look during your audit.

---

## 2026-05-22: Design questions 4 and 5 (high impact): fishing and cooked food

I paused the run at ID 305 because the fish and food block forces two decisions that affect hundreds of items.

4. Is Fishing an exempt tab? Fishing is not in your exempt list, so under strict inputs-only the caught fish, which is a Fishing output, leaves the Fishing tab. Raw fish would sit in Cooking, where it is cooked, and the Fishing tab would hold only rods, nets, bait, harpoons, and the fishing outfit. You exempted Hunter because it is basically all outputs, and Fishing is the same shape. Do you want Fishing exempted too, so caught fish stay in Fishing, or kept strict?

5. Where does cooked food live for its combat use? Cooked food stays in Cooking, which is exempt. It is also eaten as healing during combat. The previous pass scattered this, putting most cooked fish in Melee and a few high-tier ones in Melee and Slayer. The Slayer tab has an explicit Food section. Options: Cooking plus Slayer only; Cooking plus all four combat tabs; or Cooking only with no combat-tab listing. Your answer sets the rule for every food item.

### Resolutions (2026-05-22)

4. Fishing is exempt. It keeps its catches, the same as Hunter. Raw fish sits in Fishing and Cooking. The updated exempt set is Cooking, Herblore, Farming, Hunter, Runecraft, and Fishing, plus the partial Crafting exemption.

5. Cooked food goes in Cooking plus all four combat tabs. Every cooked, edible food sits in Cooking, Melee, Range, Mage, and Slayer. Burnt food, which is not edible, stays in Cooking only.

### Working sub-rule for mined and gathered materials (for your audit)

Under inputs-only, a raw material sits in the tab of the skill that consumes it, not the skill that gathered it, unless the gathering skill is exempt or there is a further same-skill step. Ores that get smelted stay in Mining + Smithing, because Smithing, the other half of that tab, consumes them. But clay is mined and then used in Crafting pottery, never smelted, so it sits in Crafting only. The same will apply to other mined or gathered materials that feed a different skill, for example a bucket of sand for glassmaking. Flagging since it shifts some materials out of Mining + Smithing.

---

## 2026-05-22: Batch 1 partial audit results and rule refinements

You spot-audited a 24-row, ~5% slice of the first 455 items. 22 were approved. Two corrections plus one extension, recorded in corrections-batch-001.csv and applied to decisions.jsonl and the batch CSV.

Corrections:
- Clay (434): also a Construction material, so Construction and Crafting. Still out of Mining + Smithing because it is not smelted.
- Blurite sword (667): a quest curiosity, so Quests, not by-function Melee.
- Bronze fire arrow (598): per your ice-arrows note, quest fire and ice arrows go to Quests, not Range.
- Excalibur (35): re-scanned from the blurite ruling to Quests. Flagged for you to confirm, since Excalibur has a special attack and is slightly more usable than the blurite sword.

Refined rule on quest-obtained equipment. A quest item that has genuine ongoing use is classified by function: Holy water goes to Range, the priest gown to Prayer, boots of lightness to Agility + Thieving, the ammo mould to Mining + Smithing. A quest item that is only a curiosity, with no real combat or skill use, stays in Quests even if it is equippable: the blurite sword, Excalibur, Khazard armour, the cattleprod, the gnome amulet. Quest fire and ice arrows fall on the curiosity side and go to Quests. I will apply this distinction for the rest of the run.

---

## 2026-05-22: Batch 1 complete (IDs 0 through 2199, 1,188 items)

The full batch-1 pass is done. decisions.jsonl and log-batch-001.csv hold all 1,188 entries. A second audit slice covering the 456-1,188 portion is in audit-slice-batch-001-part2.csv (weighted to changed and medium/low-confidence rows); the earlier audit-slice-batch-001.csv with your prior answers was left untouched.

Rules you approved mid-run, now standing for batch 2 and beyond:

- Skilling tools that are also equippable weapons go to their skill tab only, not Melee. Pickaxes to Mining + Smithing; woodcutting hatchets to Woodcutting + Firemaking. (Battleaxes and warhammers remain Melee, being true weapons.)
- Maces go to Melee only; their small prayer bonus is incidental, not Prayer gear.
- Quests is only for items used solely within a quest. Quest-locked but genuinely usable gear is classified by function (Dragon sq shield to Melee, Iban's staff to Mage, anti-dragon shield across the three combat styles).
- Capes and non-style-specific defensive jewellery span all three combat styles (Melee, Range, Mage). The basic coloured fashion capes with token defence stay in Cosmetics. Charged teleport jewellery also adds Teleports.
- Always check the OSRS Wiki "uses" section before classifying an uncertain item. This corrected bear fur and grey wolf fur back to Crafting (wooden cats / fur heads).

Rules I applied this batch that I'd like you to confirm during the part-2 audit:

- Cooked-food rule (your resolution #5) extended to all finished edible foods and pure-heal drinks: Cooking plus Melee, Range, Mage, and Slayer. Raw ingredients, prep intermediates, and burnt food stay Cooking only. This flooded the combat tabs with low-tier foods (a 3-HP cooked meat now sits in four combat tabs); tell me if you want a healing threshold instead.
- Brewed ales and stat-boost drinks go to Cooking plus the boosted skill's tab (Strength to Melee, Magic to Mage, Mining/Smithing to Mining + Smithing, Herblore to Herblore).
- Cut gems go to Crafting and Fletching (jewellery plus gem bolt tips); uncut gems stay Crafting.
- All logs go to Woodcutting + Firemaking and Fletching.
- The symbol chain splits at the blessing step: crafted/strung intermediates to Crafting, blessed holy/unholy symbols to Prayer.
- Trade goods and failure byproducts with no consuming skill go to Misc (silk, crushed gem, the failed-craft rock, archery ticket as a currency).

## 2026-05-22: Litmus refinement on quest herbs (from your part-2 audit)

You flagged snake weed. Verified on the wiki and refined the quest litmus: an ingredient counts as having genuine non-quest use only if its downstream product is itself usable outside quests. Snake weed and rogue's purse make relicym's balm and Sanfew serum (real, general potions), so they correctly sit in Herblore. Ardrigal, sito foil, and volencia moss only ever feed quest-specific potions (the Legends' bravery potion / Jungle Potion), so they are effectively quest-only and have been moved back to Quests (grimy and clean, six items, logged in corrections-batch-001.csv). The act of cleaning a herb for XP does not by itself count as a non-quest use.

## 2026-05-22: Separate-item decomposition (silver bar / silver dust)

When a material is processed into a distinct downstream item that the next skill consumes, classify the material by its direct consumers and let the separate downstream item carry the later skill. Silver bar is ground into silver dust (a separate item, around input line 3735) which is the Guthix balance ingredient, so the bar sits in Crafting + Mining + Smithing and Herblore belongs to silver dust. REMINDER: classify Silver dust as Herblore when reached (~line 3735). Logged in corrections-batch-002.csv.

## 2026-05-23: Batch 2 complete (IDs 2201-4507, 1,188 items) - audit slice ready

decisions.jsonl now holds 2,376 entries (batch 1 + batch 2); log-batch-002.csv has 1,188 rows. Audit slice for your review: audit-slice-batch-002.csv (65 rows, weighted to changed/low-confidence rows). Note: "Silver dust" did NOT appear in batch 2 - the ~3735 line estimate was off (input line 3735 is "Vase of water"). Silver dust will surface in a later batch; the Herblore reminder still stands.

Four judgment calls I applied consistently across batch 2 but want you to confirm before I treat them as settled rules (and before retro-applying to batch 1 if you agree):

1. COOKED-FOOD 5-TAB RULE - SCOPE. I put every cooked, edible (healing) food in Cooking + Melee + Range + Mage + Slayer, including low-value foods (cooked rabbit/5hp, snail meats, undead cooked chicken/1hp). This floods the combat tabs with trek/junk food. Options: (a) keep blanket rule; (b) restrict combat tabs to food that heals >=N hitpoints; (c) only "real" combat foods (a named list). Examples in slice: 3144, 3228, 3371, 3373, 3381, 4291, 4293.

2. GOD BOOKS -> all combat styles. I expanded holy/unholy/book of balance AND the incomplete "damaged book" bases to Prayer + Melee + Range + Mage (worn off-hand across all combat for the +5 prayer / attack / defence). Source had them as Prayer,Quests or Melee,Prayer,Quests. Question: do you want the incomplete damaged books treated the same as completed books, or kept simpler (Prayer only)? The torn god-book PAGES I standardized to Prayer only (source tags were a mess: Misc/Cosmetics/Prayer mixed). Slice: 3839, 3840, 3842, 3844, 3827, 3835.

3. FREMENNIK / TEAM FASHION CAPES vs the cape rule. Your batch-1 rule put basic capes in all three combat styles. The Fremennik fashion cloaks and team capes have identical cape-tier stats (def +1/+1/+2) but I kept them Cosmetics only, treating the cape rule as applying to genuine combat capes, not shop fashion/team-identifier capes. I also normalized the Fremennik fashion line (boots/skirt/gloves) from Melee to Cosmetics. Confirm the fashion-vs-combat-cape split. Slice: 3759, 3791, 3795, 3797, 3799.

4. SLAYER-DROP WEAPONS. I kept abyssal whip and granite maul as Melee + Slayer (they are general melee weapons but locked behind slayer-monster drops). If the Slayer tab should hold only slayer-specific equipment (mirror shield, nose peg, broad arrows, etc.), these should drop Slayer -> Melee only. Slice: 4151.

Other notable consistent moves this batch (all in the slice): teleport jewelry/items -> Teleports (games necklaces, ectophial, enchanted lyre); finished crafted armour drops Crafting to its worn tab while raw materials keep Crafting (snelms, splitbark, bark, fine cloth); limestone/limestone brick follow the Clay precedent out of Mining + Smithing; quest-locked-but-generally-usable gear -> by function (dragon halberd, climbing boots, Fremennik shield, crystal bow/shield, salve amulet, bearhead); quest-only keys drop Misc -> Quests; unobtainable interface/animation items -> Misc (sliding pieces/buttons, compost bin, mouth grip, rope/pole animation items); decorative Castle Wars armour -> Cosmetics + Melee.

## 2026-05-23: Matt's batch-2 audit rulings (RESOLVED)

You approved all 65 slice rows ("straight bangers") and ruled on the four open questions:

1. COOKED-FOOD COMBAT-TAB THRESHOLD = 12+ heal (lobster and up). NEW GLOBAL RULE: cooked, edible food only gets the combat tabs (Melee + Range + Mage + Slayer) in addition to Cooking if it heals 12 or more hitpoints; anything that heals under 12 is Cooking only. Raw/intermediate/burnt remain Cooking only as before. Batch-2 corrections applied (7 items: cooked rabbit, thin/lean/fat snail meat, cooked slimy eel, undead cooked chicken/meat; only cooked karambwan at 18 stayed) and logged in corrections-batch-002.csv. RETRO-APPLY to batch 1: any sub-12 food given combat tabs must drop to Cooking only (shrimps, sardine, herring, mackerel, trout, cod, pike, salmon, tuna, regular cooked chicken/meat, etc.); lobster(12)/bass(13)/swordfish(14)/monkfish(16)/karambwan(18)/shark(20)/sea turtle/manta and other 12+ foods keep the combat tabs.

2. GOD BOOKS across all combat styles: KEEP for now. You want to see how busy the combat tabs actually get before deciding whether to trim. No change; revisit after more data.

3. FASHION / TEAM CAPES kept Cosmetics (not combat) and Fremennik fashion line normalized to Cosmetics: APPROVED ("molto bene").

4. SLAYER-DROP WEAPONS (abyssal whip, granite maul) keep Melee + Slayer: APPROVED. High-desire slayer chase items belong in the Slayer tab. This generalizes: notable slayer-locked drop gear can carry Slayer alongside its combat tab.

## 2026-05-23: Batch 3 IN-FLIGHT decisions (pending your audit, applied provisionally)

Batch 3 is rolling (IDs 4508+). A few judgment calls below are applied consistently now; flagging the high-leverage ones early since they affect many future items. All are in the upcoming audit-slice-batch-003.csv. The big one is #1.

1. **SEEDS = Farming only (HIGH LEVERAGE, dropped Herblore/Fletching).** A seed's only direct consumer is Farming (you plant it). The downstream item carries the later skill, exactly per your herb sub-rule chain: seed (Farming) -> grimy herb (Farming + Herblore) -> clean herb (Herblore only). So herb seeds dropped Herblore, and tree seeds (maple/yew/magic) dropped Fletching; they are Farming only. This matches the plugin's own allotment-seed tagging (potato/onion/etc. were already Farming-only despite Cooking products) and your "Guam leaf = Herblore only, removed Farming" boundary precedent. It resolves the source data's internal inconsistency (limpwurt/herb seeds had a stray Herblore tag; poison ivy/belladonna seeds did not). SCOPE WARNING: if you'd rather seeds keep the downstream skill's tag, this reverts ~17 seeds so far plus every future seed (mechanical grep+correct). Examples in slice: 5100, 5291, 5295, 5314, 5316.

2. **BIRD NEST FAMILY + bird's egg re-tabbed (dropped bogus Fletching).** Source had Fletching on nests/eggs; the wiki shows no Fletching use at all. Wiki-confirmed: bird's eggs are offered at the Woodcutting Guild shrine for 100 Prayer XP, and empty nests crush into a Saradomin-brew secondary. So: egg nest -> Prayer, seed nest -> Farming, ring nest -> Misc (jewellery loot), empty nest -> Herblore, bird's egg -> Prayer. Low confidence on the full-nest containers; flagging the family. Slice: 5070, 5074, 5076.

3. **NAILS = Construction + Fletching (dropped Mining + Smithing).** Nails are a terminal smithed product (the bar carries Mining + Smithing), consumed by Construction AND by fletching brutal arrows (real Fletching XP). Re-scan: batch-1 Steel nails (1539) had been left Construction-only, missing the brutal-arrow Fletching use; added Fletching for consistency (logged corrections-batch-001.csv). If you consider the brutal-arrow use too niche, nails go Construction-only. Slice: 4819, 4824.

4. **FINISHED ARROWS/BOWS -> Range only.** Applied your finished-output rule to the ogre line: brutal arrows (bronze..rune) and the comp ogre bow dropped Fletching -> Range; the unstrung comp bow stays Fletching (intermediate). Consistent with batch-1 finished arrows/bows. Slice: 4803, 4827.

5. **PRAYER-BONUS COMBAT GEAR (Verac's) kept Melee + Prayer.** Pure prayer robes (priest gown, monk's robe) are Prayer-only, but Verac's is genuine melee armour where the prayer bonus is a defining feature, so dual-tagged. Confirm vs Melee-only. Slice: 4753.

6. **TERMINAL CRAFTING PRODUCT -> use-tab.** Empty plant pot (fired) dropped Crafting -> Farming (consumed by Farming); unfired plant pot stays Crafting (further fired). Same logic as nails. Slice: 5350, 5352.

7. Small calls: cave eel (5003) heals a variable 8-12, not a reliable 12+, so treated as sub-threshold -> Cooking only (added Cooking, dropped Melee) -- a borderline food, flag it. Frog spawn (5004) is fished and eaten raw (never cooked) -> Fishing only. Ogre coffin key (4850) dropped Prayer -> Misc (a loot-access key, not a Prayer input). Quest-only keys (5010) drop Misc -> Quests as before. Combination runes (steam/mist/etc.) stay Mage + Runecraft (Runecraft exempt).

## 2026-05-23: Batch 3 COMPLETE (IDs 4508-7358, 1,188 items) - audit slice ready

decisions.jsonl now holds 3,564 entries (batches 1+2+3); log-batch-003.csv has all 1,188 rows (234 changed, ~20%). Audit slice: audit-slice-batch-003.csv (57 rows, weighted to changed/low-confidence + every rule-level judgment call). "Silver dust" did NOT appear in batch 3 (max ID reached 7358); the Herblore reminder still carries forward.

The rule-level questions that most need your ruling before batch 4 (which will hit many more seeds/herbs/gear), in priority order:

1. SEEDS = Farming-only (the headline; see item #1 above). If you disagree, it reverts every seed project-wide.
2. PRAYER-BONUS combat gear -> combat + Prayer. This now spans Verac's, initiate (Temple Knight) armour, the entire White Knight set (~25 items), and the amulet of fury. One ruling settles all of them. Initiate also had Slayer dropped (prayer-conservation gear isn't slayer-specific).
3. DK / boss-drop Slayer: the four Fremennik rings (DK drops) kept/added Slayer; champion scrolls had Slayer dropped (minigame items). Confirm whether boss drops that aren't slayer-locked belong in Slayer.
4. Mined-but-not-smelted rocks (sandstone, granite): kept in Mining + Smithing, but the clay/bucket-of-sand precedent would push sandstone -> Crafting (ground into glass sand). Confirm.
5. Broodoo shields (~24 charge variants): low confidence. Made Mage (25 Magic req + magic attack), dropped Crafting (finished worn shield) + Melee. Confirm Mage vs Melee vs keep Crafting.
6. Saradomin brew: kept Herblore + Melee + Slayer; confirm whether a universal heal/defence PvM potion should span all three combat styles.

Other consistent batch-3 moves (all in the slice or log): cut gems -> Crafting + Fletching (onyx); non-style-specific defensive capes/jewellery -> Melee+Range+Mage (obsidian cape; fire cape kept Melee for its +str); teleport items -> Teleports (teleport crystals, enchanted lyre); demonbane (Darklight) -> Melee+Slayer; ghostly set -> Cosmetics+Mage; readable lore books -> Misc (Arena book, Abyssal book); quest-only/curiosity gear -> Quests (fixed device, Camulet); MTA items -> Mage; champion scrolls/holiday/pirate clothing -> Cosmetics/Misc; the source's systematic "Fletching" mis-tag on tree-derived items (seeds/leaves/roots) corrected throughout (roots -> Crafting via crossbow-string spinning, wiki-confirmed); 12+ heal foods normalized to all four combat tabs (potato-with-butter/cheese, chilli/egg/mushroom/tuna potato, tuna-and-corn); produce containers/hops normalized to Cooking+Farming (bananas dropped bogus Hunter); brewed-ale tab fixes (mature wmb Herblore->Cooking; axeman's folly Fletching/Farming->WC+FM); pyre logs dropped Prayer; iron spit -> Cooking; pink dye -> Quests. One re-scan correction logged in corrections-batch-001.csv: Steel nails (1539) +Fletching.

## 2026-05-23: Matt's batch-3 audit rulings (RESOLVED)

You reviewed all 57 audit-slice-batch-003.csv rows. 56 approved as-is; 1 correction. Key confirmations and the one change:

1. SEEDS = Farming-only: CONFIRMED (approved). Now a settled rule project-wide -- a seed sits in Farming only; the downstream grimy herb / logs carry the later skill. No reversal needed. Quest-only seeds (Kelda, Garden of Tranquillity flowers) still go to Quests.
2. PRAYER-BONUS combat gear = combat + Prayer: CONFIRMED (Verac's, initiate, the whole White Knight set, amulet of fury all approved). Initiate dropping Slayer (prayer-conservation gear isn't slayer-specific) approved.
3. DK / boss-drop Slayer: CONFIRMED. The four Fremennik rings carry their combat tab + Slayer. Champion scrolls dropping Slayer (minigame items, no Slayer skill) approved.
4. Mined-but-not-smelted rocks: KEEP in Mining + Smithing. Your ruling: "Don't add it to crafting as it's a precursor of an input, not the input itself." So sandstone/granite stay Mining + Smithing (the SAND is the Crafting input, not the rock). This also reinforces the decomposition principle (precursors don't carry the downstream skill -- validates coconut->Farming, pre-nature amulet->Crafting, etc.).
5. Broodoo shields = Mage: CONFIRMED (approved at low confidence).
6. SARADOMIN BREW = ALL 5 TABS (CORRECTION). Your ruling: "Span all combat styles, put it in all 5 tabs: Herblore, Melee, Mage, Range, Slayer." Applied to all 4 doses (6685/6687/6689/6691); logged in corrections-batch-003.csv. NEW SUB-RULE: a universal heal/defence PvM potion (heals a lot + boosts/affects defence, used across all combat) gets Herblore + Melee + Range + Mage + Slayer. Distinguish from: stat-boost potions (Herblore + the boosted style), prayer-restore (Herblore + Prayer + Slayer), and plain restore/antipoison utility (Herblore only). Apply to analogous potions in batch 4+.
7. Other confirmations: obsidian cape -> Melee+Range+Mage approved ("Yes, but could add Slayer if it fits other cape rules" -- left as-is; no cape->Slayer rule exists, defensive capes aren't slayer gear). Proboscis -> WC+FM approved ("not a great fit because niche, but it does fit"). Cave eel sub-12 -> Cooking approved ("good call"). All the Fletching-mis-tag fixes (roots/leaves/seeds), produce/hops/keg normalizations, teleport items, demonbane, ghostly set, lore-books->Misc, 12+ food combat-tab additions, and skilling-tool drops: approved.

---

## 2026-05-23: Batch 4 COMPLETE (IDs 7360-10306, 1,188 items) - audit slice ready

decisions.jsonl now 4,752 total (4 batches done). log-batch-004.csv has 1,188 rows, ~316 changed (~27%). audit-slice-batch-004.csv has 69 rows weighted to every rule-level judgment call below (batch 4 touched many brand-new item categories: POH furniture, the full crossbow tree, Hunter, skillcapes, Treasure Trail rewards, several quest lines). Decisions applied provisionally per your standing "keep grinding" instruction; the slice flags everything that needs your eyes.

NEW rule-level calls made this batch (please confirm or correct in the slice):

1. SKILLCAPES (BIG one): I added Cosmetics to every level-99 Cape of Accomplishment so each cape is [its skill] + Cosmetics, matching its hood (hoods were already Cosmetics+skill). Reasoning: a skillcape is a prestige/fashion cape (generic +9 defence), not a skilling supply, but it is strongly skill-associated. Quest point cape -> Quests+Cosmetics likewise. If you'd rather skillcapes live ONLY in their skill tab (or ONLY in Cosmetics), say so and I'll re-run the family.
2. HERALDIC HELMS + DECORATIVE KITESHIELDS (rune/steel, painted or Treasure-Trail) -> Cosmetics + Melee (decorative full helms/kites with real melee defence). Dropped the inconsistent stray Construction/Crafting tags throughout.
3. BANNERS and Trouble-Brewing FLAGS -> Cosmetics only (held weapon-slot decorations with zero combat stats). Distinct from the helms/kites which keep Melee for their defence.
4. POH FURNITURE FLATPACKS -> Construction (chairs, tables, beds, lecterns, globes, cape racks, costume-room storage, even the "ale/cider/bitter" KITCHEN-BARREL flatpacks which are furniture, NOT drinkable ale). A couple of globe flatpacks mistagged Misc were normalized to Construction.
5. BAGGED GARDEN DECORATIONS (trees/hedges/flowers) -> Construction + Farming (planting gives BOTH XP).
6. VOID set: top/robe/gloves -> Melee+Range+Mage (shared across all 3 styles); void mace -> Melee+Mage+Prayer (crush mace, autocasts, prayer bonus).
7. SALAMANDERS + swamp lizard -> Hunter + Melee + Range + Mage (Hunter-caught multi-style weapons: scorch/flare/blaze). Salamander tars -> Herblore+Hunter+Range (made via Herblore, ammo + bait).
8. TELEPORT-FUNCTION items -> Teleports: pharaoh's sceptre, skull sceptre (+Mage for its autocast), grand seed pod (+Farming for its launch XP), Trouble Brewing "rum".
9. QUEST-LOCKED-BUT-USABLE gear classified BY FUNCTION (drop Quests): lunar armour/staff/jewellery -> Mage; proselyte/initiate -> Melee+Prayer; magic essence line -> Herblore(+Mage); elemental/mind helmets & mind shield -> Mage; crystal saw/seed -> Construction; suqah hide/leather -> Crafting; lunar ore/bar -> Mining+Smithing. CONTRAST: quest-INTERNAL items with no post-quest use stay Quests -- the waking-sleep-vial chain (dropped Herblore), the Slug Menace "special runes" that cannot cast spells (dropped Runecraft), timber beam, origami balloon.
10. MINIGAME loot/tokens/utility -> Misc: Pyramid Plunder artefacts (dropped Agility+Thieving -- they're vendor loot, not inputs), Warriors' Guild token + defensive shield + shot/barrels/ashes, HAM store-room keys, animation items (stick/bucket/torch). MTA coins -> Mage (made via alchemy).
11. DISGUISE / COSTUME clothing -> Cosmetics: pirate bandana/hat, Meiyerditch Vyrewatch + Citizen outfits, bomber jacket/cap, holiday-event gear.
12. CROSSBOW TREE precision: unstrung (u) crossbows + stocks + limbs are Fletching/Mining+Smithing INTERMEDIATES (dropped stray Range); finished crossbows/bolts -> Fletching+Range; gem BOLT TIPS -> Fletching+Range (chiselling cut gems is a Fletching action, so dropped the Crafting precursor tag); bolt mould -> Crafting tool; sinew -> Cooking(exempt output)+Crafting(spun into string), the Fletching tag was wrong; spiky vambraces (all variants) -> Range (ranger dhide hand armour; dropped stray Crafting/Melee/Hunter); mith grapple split by stage (tip -> Fletching+Mining+Smithing; finished grapple -> Agility+Range).
13. Smaller fixes: enchant tablets -> Mage; clockwork -> Crafting; bolt of cloth -> +Crafting (banner input); Construction guide / Farming manual / readable guide books -> Misc; sawdust (no use) -> Misc; roast beast meat sub-12 -> Cooking only (dropped Melee); raw rainbow fish -> Cooking+Fishing (dropped Hunter, which was only via bait); weight-reducing capes (spotted/spottier) -> Agility+Hunter; goblin-mail dyes normalized to Quests; dark kebbit fur normalized to Hunter; Bird book -> Quests.

Hunter section sanity checks (kept as-is): traps/tools/furs/camo gear -> Hunter; raw kebbit/bird meats -> Cooking+Hunter; chinchompas/kebbit spikes+bolts -> Hunter+Range/Fletching; Hunter potion -> Herblore+Hunter; caught butterflies with combat-boost release effects (black warlock/snowy knight/ruby harvest) -> Hunter+Melee, sapphire glacialis (non-combat) -> Hunter; barb-tail harpoon -> Fishing+Hunter+Melee.

Open low-confidence items I'd most like a gut check on: the skillcape Cosmetics call (#1), Pyramid Plunder artefacts -> Misc vs Agility+Thieving (#10), and whether minigame "rum" really belongs in Teleports (#8).

---

## 2026-05-23: Matt's batch-4 audit rulings (RESOLVED)

You reviewed all 69 audit-slice-batch-004.csv rows. 64 approved as-is; 5 corrections (one is a new high-leverage general principle). Applied to decisions.jsonl in-place, log-batch-004.csv flipped to final state, all logged in corrections-batch-004.csv.

1. NEW GENERAL PRINCIPLE -- **"XP given != skill input/output."** An item belongs in a tab because it is consumed/produced as that skill's INPUT or OUTPUT, NOT merely because using it grants that skill's XP.
   - Bagged garden decorations (8417-8461, all 23) -> **Construction only** (your words: "This is a construction input, not a farming input, even though it gives farming XP"). Reverted the Construction+Farming rule.
   - Grand seed pod (9469) -> **Teleports only** (your words: "Teleport only, giving XP is not the same as being an actual input or output of the skill"). Dropped Farming.
   - APPLY GOING FORWARD: any item whose only link to a skill is "using it gives that skill XP" (but it isn't an input you skill with, nor a produced output of that skill) does NOT get that tab. Contrast: Auguste's sapling stays Farming because it is genuinely planted in a farming patch as a Farming input.
2. **Thieving/Agility unique outputs & currency DO go in Agility + Thieving** (Pyramid Plunder artefacts 9026-9042 reverted from Misc -> Agility + Thieving). Your reasoning: "There are very few inputs for thieving and agility, so having the outputs that are unique to these skills is okay." Examples you gave that BELONG in Agility+Thieving: marks of grace (currency), unique pickpocket keys/valuables, Pyramid Plunder artefacts. Examples that do NOT (because they are non-unique items that are another skill's input): gems from a gem stall (Crafting input), cakes from a baker's stall (food), etc. So the test is: is it a UNIQUE Thieving/Agility output or currency, with no role as another skill's input? If yes -> Agility + Thieving. (Note: HAM store-room keys stayed Misc -- you approved that row; they're generic dungeon keys, not unique Thieving currency.)
3. **Disguise outfits whose only real use is quest/area content -> Quests + Cosmetics** (Vyrewatch + Citizen Meiyerditch outfits, 9634-9644). Your words on Citizen top: "Keep this in quests and cosmetic, as its only real use is in quests though you can still wear it for cosmetic afterwards, that's not its intended use." Contrast: the bomber jacket stayed Cosmetics (approved) because it has a real ongoing non-quest use (Wintertodt warm clothing).
4. **Sinew (9436) -> Crafting only** (you removed Cooking: "this is likely a singular gut-feel exception"). So a non-food item made via the Cooking skill does NOT get Cooking just for that; it sits in the tab of the skill that consumes it. Reinforces the "exempt tab keeps its outputs" rule applies to FOOD, not arbitrary cooking-skill products.
5. **Skillcapes -> [skill] + Cosmetics: CONFIRMED** ("Good call on cosmetics for skill capes"). Quest point cape -> Quests + Cosmetics confirmed. Keep adding Cosmetics to every 99 cape + the quest cape across all future batches.
6. **Salamanders verified -> Hunter + Melee + Range + Mage** (you were Unsure, asked me to wiki-check). OSRS Wiki confirms salamanders are multi-style: Scorch (Slash/melee), Flare (Ranged), Blaze (Magic), each using its own attack bonus. So Hunter (caught) + all three combat tabs is correct. No change. (Source: oldschool.runescape.wiki/w/Salamander.)
7. Everything else in the slice approved as-is: enchant tablets->Mage; heraldic helms/kiteshields->Cosmetics+Melee; banners/flags->Cosmetics; POH flatpacks (incl. barrel/globe)->Construction; void gear; teleport-function items (pharaoh's/skull sceptre, rum)->Teleports; magic essence->Herblore(+Mage); waking-sleep chain & quest "runes"->Quests; lunar gear->Mage; proselyte->Melee+Prayer; crossbow-tree precision (intermediates drop stray Range, bolt tips->Fletching+Range, bolt mould->Crafting, mith grapple split); spiky vambraces->Range; combat potion->Herblore+Melee; sub-12 food->Cooking; readable books->Misc; sawdust->Misc; weight-reducing capes->Agility+Hunter; raw rainbow fish->Cooking+Fishing.
