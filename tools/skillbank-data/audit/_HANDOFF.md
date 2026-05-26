# Skill Bank audit — handoff / runbook

Last updated: 2026-05-25 (**PROJECT COMPLETE — all 11,826/11,826 items classified.** BATCH 10 COMPLETE + a 50-item gap discovered and filled. Two audit slices PENDING Matt: `audit-slice-batch-010.csv` and `audit-slice-gapfill-001.csv`. Also still pending from earlier: `audit-slice-batch-007.csv` and `audit-slice-batch-009.csv`.). Written so a fresh thread (no memory of prior chats) can pick this up cold. Read this top to bottom, then read `questions-for-matt.md` for the full ruleset.

## ⭐ PROJECT COMPLETE (2026-05-25)

- `audit/decisions.jsonl` = **11,826 lines**, ids 0 → 33393, now **perfectly line-aligned** with `out/audit-input.jsonl` (the old "+50 line offset" is GONE — see the gap note below). Every input item has exactly one decision; no gaps, no duplicates. Verified line-for-line at both ends (line 11826 = id 33393 "Gem sack" in both files).
- **THE +50 GAP — FOUND & FIXED.** Final verification revealed decisions.jsonl had only 11,776 lines vs the input's 11,826: a contiguous block of **50 items (ids 7842–7961)** — Rag and Bone Man I/II quest bones, the Swan Song / Royal Trouble / Pirate's Treasure quest items, the monkfish line, a few skilling materials — had never been classified (and had never appeared in any batch log or audit slice). Matt's instruction: "use every rule we have to classify them yourself; defer to a wiki lookup if medium/low confidence." Done: all 50 classified and **spliced into decisions.jsonl in id-order** (right after id 7839), which is what closed the offset. They are logged in `audit/log-gapfill-001.csv` and surfaced for review in `audit/audit-slice-gapfill-001.csv` (ALL 50 rows, since they were never audited). 4 were corrections vs the input's current_tabs: Ogre ribs & Experiment bone (dropped spurious Prayer → Quests), Iron sheet (added Mining + Smithing, Swan Song smith), **Monkfish (was mis-tagged Melee,Slayer → Cooking,Mage,Melee,Range,Slayer per the high-heal-cooked-food convention — heals 16, ≥12 threshold; matches Swordfish/Lobster/Shark)**.
- Pure essence stayed Runecraft-only (matches Rune/Daeyalt/Guardian essence convention — mining tab omitted by convention). Bottle of wine kept Agility + Thieving,Cooking (wine family). Field ration stayed Cooking (heals 10, sub-12).
- **PENDING Matt's audit (4 slices):** `audit-slice-batch-007.csv`, `audit-slice-batch-009.csv`, `audit-slice-batch-010.csv` (71 rows — weighted to changes + every rule-level family: sea-charting bottles, cargo crates, Avernic treads, ornament kits, gem containers, deep-sea fish, sigils, all-style accessories, League teleporters, quetzal whistle, etc.), `audit-slice-gapfill-001.csv` (50 rows). When Matt returns each, apply corrections to decisions.jsonl + the matching log + a corrections-*.csv.

## What this project is

Classify every OSRS item in `out/audit-input.jsonl` into one or more of the 22 Skill Bank tabs, under the **inputs-only "Brief #63"** rules. An item belongs in the tab(s) of the skill(s) that *consume* it. Output is written to disk in a fully resumable way.

The 22 tabs: Melee, Range, Mage, Prayer, Cooking, Woodcutting + Firemaking, Fletching, Fishing, Crafting, Mining + Smithing, Herblore, Agility + Thieving, Slayer, Farming, Runecraft, Hunter, Construction, Misc, Quests, Sailing, Cosmetics, Teleports. (Note the combined tabs use the literal strings "Woodcutting + Firemaking", "Mining + Smithing", "Agility + Thieving".)

## Current state (updated 2026-05-25 — ALL BATCHES COMPLETE)

- `out/audit-input.jsonl` = **11,826 items total** (READ-ONLY master input).
- `audit/decisions.jsonl` = **11,826 entries** (ids 0 → 33393) — the cumulative source of truth, one JSON line per classified item: `{"id":N,"name":"...","tabs":[...],"rationale":"...","confidence":"high|medium|low"}`. Fully classified; line-aligned with the input.
- **Batches 1–10 COMPLETE.** Batch 10 = decisions 10743–11826 (1,084 items; `log-batch-010.csv` = 1,084 data rows + header, verified line-aligned with decisions). Plus the **gap-fill** of 50 items (ids 7842–7961) spliced in-order (`log-gapfill-001.csv`).
- **Audited:** batches 1–6, batch 8 (corrections applied). **PENDING Matt's audit:** batches 7, 9, 10, and the gap-fill (slices listed at the top of this file).

## RESUME POINT — none (classification done)

Classification is finished. The only remaining work is applying Matt's audit corrections as he returns each pending slice (batches 7, 9, 10, gap-fill). For each: apply to `decisions.jsonl` in-place (by id), flip the matching `log-*.csv` row(s) to `changed=yes` where needed, and append to a `corrections-*.csv` (header `id,name,from_tabs,to_tabs,reason,source`). Propagate any recurring-family ruling across the whole file.

### NOTE for whoever audits batch 10: the original handoff over-counted batch 10 as "1,134 items / decisions 10693–11826." That was the old +50-line-offset confusion. The TRUE figures are **1,084 items, decisions 10743–11826** (the offset is now gone after the gap-fill splice). The batch-10 flag families below are still accurate.

### Batch 10 — settled patterns + open flags (applied so far; PENDING Matt's batch-10 audit)
- **Sailing skill content has arrived** (decisions ~11043+). Sailing shipbuilding materials (planks, yarns, bolts of linen/canvas/cotton, dragon nails), boat-summon tablets, coral-nursery corals, gryphon items, Summer Shore lore -> mostly keep Sailing (+ Crafting/Farming/Construction where they're consumed/produced by those skills). FLAGS: gryphon feather Range tag; ironwood-pyre-log Sailing (dropped, ironwood source incidental); rare-fish family (unstuffed=Fishing,Sailing; stuffed=Construction,Fishing — normalised two outliers); yarn family (dropped stray Construction from hemp yarn); coral family (inconsistent Farming/Fishing/Hunter tags — left as source, flagged); "Teleport to boat" -> Sailing,Teleports; "Summon boat" kept Misc,Sailing (boat logistics, not a player teleport).
- **Yama / Doom of Mokhaiotl endgame:** oathplate (finished armour -> Melee; shards/plate chain -> Mining + Smithing); Yama contract tokens normalised -> Misc; purifying-sigil pieces -> Cosmetics (recolour applicators); avernic treads variants -> their style, dropped Slayer (Mokhaiotl not a slayer boss; tribrid Mage/Range flagged).
- **Leagues V (Raging Echoes) / Grid Master:** Relic-Hunter outfits/trophies/banners -> Cosmetics; echo ornament kits -> Cosmetics (drop the gear tab); recoloured echo gear keeps its style + Cosmetics; POH-recolour scrolls (nexus/portal/scrying-pool/spirit-tree) -> Cosmetics (drop Construction); rugs/curtains (built) keep Construction,Cosmetics; all-style accessories (battlehat, amulet of monarchs, etc.) left as source + flagged; tri-style weapons (nature's reprisal) -> Mage,Melee,Range,Misc.
- **Continued rules:** teleport tablets/scrolls/devices -> Teleports (Mokhaiotl waystone, chasm/quetzal teleports, giantsoul amulet, ancient teleporter); GE armour-set placeholders -> primary style (fixed blue-moon->Mage, eclipse-moon->Range); ToA jewels -> drop Quests; quest keys -> Quests; lore books -> Misc; JMod bot tools -> Misc; holiday/event cosmetics -> Cosmetics; XP lamps/perk scrolls -> Misc/Quests; skill-boost ales/wines/drinks -> kept source + flagged.

### Batch 9 — settled patterns + open flags (applied through all 1,188; PENDING Matt's batch-9 audit)
Extensions of prior rulings, applied consistently. The flagged ones are on `audit-slice-batch-009.csv`:
- **Teleport tablets/scrolls/devices -> Teleports** (Kourend/Civitas/Aldarin tablets, calcified moth, Guthixian-temple/spider-cave/colossal-wyrm scrolls, Globetrotter pendant, pendant of ates, ring-of-shadows tablets). **Quetzal whistles -> Teleports** (dropped Hunter/Misc) [FLAG]. Animation-override "teleport scrolls" stay Cosmetics.
- **Cosmetic recolour APPLICATORS -> Cosmetics only** (ornament kits, Armageddon cape fabric, graceful crafting kit, rejuvenation pool scroll — drop the gear's functional tab). Recoloured GEAR keeps its function + Cosmetics.
- **Multi-skill crafting INGREDIENTS get the production skills** (cf. broken-zombie-axe precedent): Scurrius' spine -> Crafting,Fletching,Mining + Smithing; Tormented synapse -> same. [KEY FLAG — finished spine/demonbane weapons kept combat-only; confirm direction.]
- **All-style combat POTIONS -> Mage,Melee,Range** (blighted overload). [FLAG: all-style ACCESSORIES — amulet of the monarchs/emperor ring/thousand-dragon ward/gloves of the damned — left as source Melee(,Misc); confirm whether to add Mage/Range.]
- **Source mis-tags corrected:** Forestry "Fletching" -> Woodcutting + Firemaking (continues from batch 8); cooked hunter-meats had stray Melee -> dropped (food, not melee); raw pyre fox/moonlight antelope had stray Fishing -> dropped; antlers had stray Range -> dropped (material, not ranged gear); LMS blue-moon helm/spear had Melee -> Mage (Blue Moon is the Magic set); anti-venom+(2) stray Melee dropped; rum Melee -> Misc.
- **Jar-of-X boss collectibles -> Construction** (jar of venom; per Matt's batch-8 ruling). Quest "canopic jar (oil)" stays Quests (not a display jar).
- **Skilling pets keep Cosmetics + skill** (Quetzin -> Cosmetics,Hunter). Combat-boss pets -> Cosmetics only.
- **Hunter-meat foods -> Cooking,Hunter; Hunter creature parts/Rumours items -> Hunter; Mixology potions/pastes/reagents -> Herblore.**
- **OPEN FLAGS for Matt (no ruling yet):** (a) skill-boost ales/wines (sunbeam/blackbird/etc.) — boosted-skill tab vs Misc?; (b) skilling XP-boost outfits (alchemist's/guild-hunter/etc.) — skill tab vs Cosmetics?; (c) all-style Deadman sigils tagged a single style (adroit/onslaught/restoration) vs Misc-only (cf. meticulousness=Misc); (d) tecu salamander tri-style; (e) hunter butterfly/moth heal & restore mixes carrying Melee; (f) aranea boots / araxyte slayer helm (i) tribrid bonuses; (g) finished smithed/fletched weapons combat-only vs production-skill (the Scurrius/synapse counterpoint). STILL OPEN from batch 8: soul bearer (19634) + bonecrusher necklace (22986) Slayer-tool confirm.

**Batch-8 settled patterns (applied through all 1,188; pending Matt's batch-8 audit):**
- Teleport items (tablets, teleport sceptres/rings/amulets, teleport spheres) -> **Teleports** tab (moved many out of Misc). Animation-override "teleport scrolls" are Cosmetics, not Teleports.
- Ornament/recolour KITS -> **Cosmetics only** (the kit is not the gear); the recoloured GEAR keeps its functional tab + Cosmetics.
- Dragon/infernal skilling TOOLS (axe/pickaxe/harpoon) -> skill tab + Cosmetics, **no Melee**.
- Holiday/seasonal EVENT items (Halloween/Christmas/Easter/Birthday/Pride cookout etc.) -> **Cosmetics** (dropped Misc/Quests/Cooking/Fishing on event-process items).
- Slayer tag REMOVED from Nex/GWD/event/boss drops that aren't slayer-task gear (ancient ceremonial set, nihil shard/dust/horn, ancient hilt, frozen key pieces, essence pack, ectoplasmator, ash sanctifier, sanguine dust). Rare slayer-monster drops stay DEFERRED (abyssal whip (or) -> dropped Slayer).
- Void body/robe/gloves -> Mage,Melee,Range (all styles); style helms -> their style. (Flagged: confirm vs base void set.)
- All-styles combat boost potions (Castlewars brew, Potion of power) -> Mage,Melee,Range.
- Skilling-activity/boss pets keep their skill tab (Tiny tempor->Fishing, Abyssal protector->Runecraft).
- Skilling-sourced rewards/outfits keep the skill tab (GotR->Runecraft, Giants' Foundry->Mining + Smithing, Tempoross->Fishing, Sepulchre->Agility + Thieving).
**Batch-8 audit RESOLVED (Matt's rulings — now BINDING for all batches; corrections-batch-008.csv applied):**
- **Jar-of-X boss collectibles -> Construction ONLY** (you display them in your POH after beating the boss). Swept all 13 jars to Construction (jar of dirt/sand/swamp/souls/miasma/darkness/stone/decay/chemicals/eyes/dreams/spirits/smoke).
- **Auto-bone-crush / ash-scatter Prayer devices ARE slayer tools -> Prayer + Slayer**: bonecrusher (13116), ectoplasmator (25340), ash sanctifier (25781) all get Slayer back. STILL OPEN (flagged in corrections-batch-008, not yet changed): soul bearer (19634) and bonecrusher necklace (22986) — same category, awaiting Matt's confirm.
- Deadman Mode sigils -> **Misc** (confirmed, keep). Cosmetic slayer helms + (i) variants -> **keep Slayer** (confirmed). Shades of Mort'ton coffins -> **Misc,Prayer** keep as-is (confirmed). Swampbark/bloodbark runescrolls -> Mage,Runecraft (confirmed, "reluctant yes"). All other batch-8 slice rows with blank approval = approved as-is.
- Still genuinely open (no ruling yet, low stakes): crystal of echoes (Misc vs Teleports); Emir's Arena Calamity hybrid armour (Melee,Misc vs all styles); ToA supply consumables (nectar/tears/salts/ambrosia - Herblore vs not, single vs all combat styles). Read in 50-line chunks. Line-mapping quirk: audit-input line = decisions line + 50. Batch 8 ends at decision 9504. Apply the binding batch-7 rules: skilling-sourced rewards/pets keep their skill tab; Slayer loadout is the curated slayer-loadout.md overlay (don't scatter Slayer tags). (Batch 7's slice is awaiting Matt; apply his corrections to decisions.jsonl + log-batch-007.csv + corrections-batch-007.csv when he returns it.) (Cowork note: the sandbox bash mount of the dev folder is stale/empty under the wrapped desktop app — use the Read/Edit/Glob/Grep file-tools, which reach the real folder.)

Per chunk: classify -> append to `decisions.jsonl` AND `log-batch-007.csv` (header `id,name,old_tabs,new_tabs,changed,rationale,confidence`; quote fields with commas) by anchoring `old_string` on the current last line -> update the resume marker. **File-tools only (bash cannot reach the dev folder).**

**At batch-7 end (decision 8316):** verify decisions.jsonl == 8,316 lines and log-batch-007.csv == 1,188 data rows + header; check ID continuity; then produce `audit-slice-batch-007.csv` (~60 rows; header `id,name,old_tabs,new_tabs,changed,confidence,rationale,approved,comments`; leave approved/comments blank; weight changed/medium-confidence rows + every flagged batch-7 family listed in the Task #16 marker).

Apply the full batch-5 ruleset + batch-6 RESOLVED rulings (below) + the **batch-7 settled patterns** (next section). The live Task #16 ("Classify batch 7") description holds the same resume point + the full cumulative batch-7 pattern list + the slice-flag list — read it via TaskList/TaskGet.

## Batch 7 — settled patterns (provisional, applied through ALL 1,188 items; pending Matt's batch-7 audit)

Extensions/applications of prior rulings, applied consistently so far in batch 7:
- **Silver (opal/jade/topaz) jewellery:** unenchanted -> Crafting; enchanted -> by FUNCTION (teleport rings/necklaces/amulets [ring of returning, necklace of passage, burning amulet] -> Teleports drop Misc/Slayer; ring of pursuit -> Hunter; dodgy necklace -> Agility + Thieving; necklace of faith -> Prayer; amulet of bounty -> Farming; amulet of chemistry -> Herblore; expeditious/slaughter bracelet -> Slayer; flamtaer bracelet -> Misc; Efaritay's aid anti-vampyre -> Melee drop Slayer).
- **Teleports:** teleport tablets/scrolls (volcanic mine, revenant cave, etc.), Chronicle + teleport card, royal seed pod, charge-dragonstone scroll, ring of wealth(i), ancient tablet, slayer ring (eternal), skull sceptre (i), **Kharedst's memoirs + its 6 teleport pages** -> Teleports.
- **Coloured slayer helms** (purple/turquoise + earlier black/green/red): non-(i) -> Cosmetics+Melee+Slayer; (i) -> Cosmetics+Melee+Range+Mage+Slayer.
- **Max capes/hoods:** max HOODS (no combat stats) -> Cosmetics only; max CAPES -> Cosmetics + their cape's style (infernal->Melee, imbued god->Mage, assembler->Range, ardougne->Teleports).
- **Imbued god capes -> Mage.** Mage Arena II imbue components (Justiciar's hand / ent's roots / demon's heart) -> Mage (re-imbue use), drop Quests; enchanted symbol (locator) -> Quests.
- **Crossbow tree:** FINISHED crossbows/bolts (incl gem-tipped + enchanted dragon bolts) -> Fletching+Range (enchanted (e) bolts -> Range); INTERMEDIATES (limbs, stock, unstrung (u), unfinished bolts) -> Fletching, drop stray Range.
- **Visages (skeletal/wyvern/draconic) -> Mining + Smithing** (smithing precursor for the shield), drop combat/Slayer. **Finished smithed gear** (dragon kiteshield) -> Melee, drop Mining + Smithing.
- **Antifire potions** (incl super antifire + barbarian antifire mix) -> **Herblore ONLY** (dragonfire resist is not a combat boost), drop Melee/Slayer.
- **Grotesque Guardians** granite gear (gloves/ring/hammer/boots, +imbued ring) -> Melee, drop spurious Slayer; granite dust -> Mining + Smithing; granite cannonball -> Mining + Smithing + Sailing; black tourmaline core -> Melee; jar of stone -> Misc; brittle key (slayer-boss roof access) -> Slayer. **Revenant** artifacts (ancient crystal[LOW CONF]/emblem/totem/statuette), bracelet of ethereum, revenant ether -> Misc, drop Slayer.
- **Vorkath's head -> Cosmetics+Range+Prayer** (multi-use), drop Slayer; **stuffed head -> Construction**. **Wrath/runes -> Mage+Runecraft.**
- **Event/holiday items** (2017 Halloween + Christmas activity items, incl event teleport-tablet/key/logs) -> Cosmetics. **Library lore books** (Myths' Guild, Arceuus) -> Misc, drop Quests. **DSII quest items** (map pieces, busts, dragon key, diaries, locator orbs) -> Quests, drop spurious Melee. **Pets -> Cosmetics. Unobtainable -> Misc.**
- **Fossil Island:** bone fossils -> Misc+Prayer; plant fossils -> Misc; rare bone fossils -> Prayer; enriched bones/calcite/pyrophosphite -> Prayer; numulite -> Misc; volcanic ash -> Mining + Smithing + Farming. **Amethyst ammo:** gem bolt/arrow tips -> Fletching+Range (drop Crafting); javelin tips -> Crafting+Fletching+Range; amethyst gem -> Mining + Smithing + Crafting + Fletching. **Mermaid's tear -> Agility + Thieving; merfolk trident -> Melee (drop Fishing); drift net -> Fishing+Hunter; karambwanji -> Cooking+Fishing.** Tzhaar/LMS rune packs -> Mage (drop Slayer); mining gloves/ore fragments/Volcanic Mine tools -> Mining + Smithing; bird houses -> Crafting+Hunter; tree seeds/saplings/compost -> Farming.

### Batch 7 audit — Matt's rulings (2026-05-25)

Matt reviewed `audit-slice-batch-007.csv` and marked only disagreements; everything else is APPROVED and now binding. Corrections applied to decisions.jsonl + log-batch-007.csv + corrections-batch-007.csv:
- **Fish sack, Golden tench -> keep Fishing** (now Cosmetics + Fishing). **Zalcano shard -> keep Mining + Smithing** (now Cosmetics + Mining + Smithing).
- **NEW PROVISIONAL PRINCIPLE (pending discussion 2):** a reward sourced from a SKILLING activity/boss keeps its associated skill tab even if cosmetic / no real stats. Revisit candidates: other skilling-boss cosmetic drops and skilling pets currently in Cosmetics-only.
- **Antifire family: DEFERRED** (Matt: No). Left as Herblore for now, pending the Slayer-tab decision (discussion 1).

**RESOLVED (Matt ruled 2026-05-25, now binding):**
1. **Slayer = HYBRID curated loadout tab.** It holds: task-mechanic items, the inventory loadout (top foods + worthwhile potions incl. antifires, prayer/combat/stamina/anti's), and task-enhancing gear (salve amulet, demonbane weapons, etc.). RARE monster drops are SKIPPED for now (revisit later). Membership is curated in `audit/slayer-loadout.md` and applied as a force-include overlay at build time, NOT scattered through decisions.jsonl. The classifier still tags genuinely slayer-locked items. Antifire family stays Herblore in decisions.jsonl and gains Slayer via the loadout overlay. Matt to trim the [SUBJECTIVE] food/potion picks in slayer-loadout.md.
2. **Skilling-activity / boss rewards keep their associated SKILLING tab even if cosmetic.** Combat-boss drops do NOT (no skill behind them). Retroactive sweep applied to 11 skilling pets across batches + fish sack/golden tench/Zalcano shard (see `retroactive-skilling-sweep.csv` and `corrections-batch-007.csv`). Skilling pets now carry Cosmetics + their skill; Baby mole (Giant Mole = combat boss) correctly stayed Cosmetics. Apply this rule going forward in batches 8+.

### Batch 7 items 401-1188 — newly applied patterns (cowork continuation, provisional; batch COMPLETE)
- **Adamant kiteshields + heraldic helms (every crest) -> Cosmetics + Melee.** Drop stray Construction/Crafting paint-step tags (decorative gear with real defence; same standard as the batch-4 heraldic family).
- **Myths' Guild dragon-metal smithing components** (dragon metal shard/slice/lump) -> **Mining + Smithing only**; drop Melee (not worn) and spurious Slayer.
- **Fletched wooden shields** (oak/willow/maple/yew/magic/redwood) -> **Fletching + Melee** (Fletching output, worn as melee defence); willow normalised to add Melee to match its family. [FLAG on slice]
- **Leather/d'hide shields** -> **Crafting + Range** (crafted ranged defence; no Fletching despite the fletched wooden base).
- **Standalone ornament KITS -> Cosmetics only** (dragon boots/platebody/kiteshield kits, anguish kit); assembled (g)/(or) pieces keep their functional tab + Cosmetics.
- **Extended super antifire (all doses) + barbarian mixes -> Herblore only** (antifire is not a combat boost); drop Melee/Slayer.
- **Barbarian Assault machine ammo/runes -> combat tab** (arrows -> Range, wrath rune -> Mage); drop Misc.
- **Revenant artefacts** (medallion/effigy/relic) -> Misc; drop spurious Slayer. **XP lamps** (Champion's lamp) -> Misc; drop spurious Slayer.
- **Bird houses (all woods) -> Crafting + Hunter.** Underwater Agility/Thieving currency (glistening tear) -> Agility + Thieving.
- **Powered staves / (e) tridents / Sanguinesti staff -> Mage.** **Staff of light -> Mage + Melee** (bladed staff with real melee stats; melee NOT incidental). [FLAG] **Dragonbone necklace -> Mage+Melee+Prayer+Range** (all-style +10 attack neck whose defining feature is the +12 Prayer bonus). [FLAG]
- **DS2 ship-defence quest items** (water container, swamp paste, revitalisation potion, keys) -> Quests. **Mythical cape -> Melee** (usable post-quest, drop Quests). **Jar of decay -> Misc** (jar family).
- **ToB gear** (Ghrazi rapier, scythe of vitur, Justiciar set) -> Melee (incidental prayer bonus ignored). **Deadman starter** sword/bow/staff -> Melee/Range/Mage; starter pack -> Misc.
- **CoX Challenge-Mode cosmetic capes** (Xeric's guard/warrior/sentinel/general/champion) + **metamorphic dust** (pet recolour) -> Cosmetics; drop Slayer.
- **Ivandis flail -> Melee + Slayer** (anti-vampyre, usable post-quest; drop Quests; autocast magic incidental). [FLAG] **Drakan's medallion -> Teleports.**
- **Bryophyta's essence -> Crafting** (drop Mage precursor + spurious Slayer); Bryophyta's staff -> Mage; mossy key -> Misc.
- **A Taste of Hope quest-internal item run -> Quests** (mysterious herb/meat/potions, vial of blood, notes, chain, tome of experience). **Lore books/scrolls/deeds -> Misc** (incl. bloody grimoire, despite "about potions"; turncloak, explosive discovery, elixir of everlasting).
- **Battlemage potion -> Herblore + Mage; bastion potion -> Herblore + Range** (combat potions; drop Slayer). **Vial of blood (ToB) -> Herblore** (potion base; drop Melee). **Cadantine blood potion (unf) -> Herblore.**
- **Theatre of Blood:** weapons/armour to their style (Ghrazi rapier/scythe/Justiciar -> Melee, Sang staff/Dawnbringer -> Mage), uncharged variants identical; **Sinhaza shrouds + Lil' zik pet -> Cosmetics**; ToB lore books (Serafina's diary, the butcher, etc.) -> Misc; **avernic defender hilt -> Melee** (direct-attach component, godsword-hilt precedent). Dawnbringer drops Misc.
- **Ancient Warriors sets:** Vesta/Statius -> Melee, Morrigan -> Range, Zuriel -> Mage (Deadman-exclusive but tradeable functional gear, classify by style).
- **Revenant weapons:** Viggora's chainmace -> Melee (drop Slayer), Craw's bow -> Range, Thammaron's sceptre -> Mage. **Amulet of avarice -> Melee+Range+Mage** (glory-style all-style amulet; anti-revenant boost is salve-like, no Slayer). [FLAG]
- **Weiss salts (te/efh/urt) -> Mining + Smithing + Woodcutting + Firemaking** (mined, burned as fire-pit fuel; efh drops anomalous Construction). [FLAG] **Basalt -> Mining + Smithing.** **Icy/stony basalt + escape crystal -> Teleports.**
- **Boss-head trophies (kq head, stuffed) -> Construction** (drop Slayer; examine confirms not a Slayer item). **Curator's medallion -> Construction** (POH mount).
- **2018 Halloween/Christmas event + clown/tree costumes -> Cosmetics; event keys -> Cosmetics** (crypt key). **JMod bot-busting tools (scythe/AGS) -> Misc.** **Generic supply packs (empty bucket pack) -> Misc.**
- **Coin pouch (pickpocket loot) -> Agility + Thieving.** **Ectofuntus bonemeal (wyrm/drake/superior dragon) -> Prayer.** **Silver sickle (b) emerald/enchanted-emerald variants -> Melee** (usable blessed sickle, drop Quests). [FLAG] **Bloody bracer (novelty ale) -> Misc.**
- **Forsaken Tower + Ascent of Arceuus quest-internal items + favour certificates -> Quests.** Unobtainable reward-scroll items (dark altar, cyan crystal) -> Misc.
- **Wyrm/drake/hydra bones + bonemeal -> Prayer.** **Seeds -> Farming only** (drop spurious Fletching from celastrus/redwood-tree seeds and Herblore from snape grass seed); seedlings/saplings -> Farming.
- **Aerial fishing:** cormorant's glove + catches (bluegill/common tench/mottled eel/greater siren) -> Fishing + Hunter; fish chunks/molch pearl -> Fishing. **Fishing-reward items with no fishing function -> Cosmetics** (fish sack, golden tench novelty). [FLAG] **Pearl rods (decorative functional) -> Fishing + Cosmetics.** [FLAG]
- **Dragon knife (thrown) -> Range** (drop Slayer); **animation-only dragon knife -> Misc.** **Dragonfruit pie (heals + reliable Fletching boost) -> Cooking + Fletching** (skill-boost food). [FLAG] **Seed bird nests -> Farming** (drop Fletching).
- **Rada's blessing (ammo-slot Kourend) -> Fishing** (double-catch chance) **+ Prayer** on tiers 2-4; drop Misc. (wiki-confirmed)
- **Karuulm slayer-boss combine-components -> Slayer** (drake's claw/tooth, hydra's claw/heart/fang/eye/tail, hydra leather; grouped under Slayer rather than inheriting the finished item's combat tab). [FLAG] Finished gear routes by style and DROPS the non-task-locked Slayer: dragon hunter lance + ferocious gloves -> Melee; **brimstone ring -> Melee+Range+Mage**; **bonecrusher necklace -> Melee+Range+Mage+Prayer** (matches dragonbone necklace). **Boots of brimstone -> Mage+Range+Slayer** (slayer-locked hybrid); **boots of stone + devout boots** stay Slayer / Prayer.
- **Reference/lore books -> Misc** (Gielinor's Flora vols 1-7, Karuulm "old notes"; drop Farming/Slayer per lore-book rule). [FLAG family]
- **Bottled dragonbreath (dragonfire-shield/ward charge consumable) -> Misc** (wiki-confirmed). [FLAG] **Battlefront teleport -> Teleports.** **Dragonfruit -> Cooking + Farming; celastrus bark -> Farming + Fletching** (harvested + fletched).
- **Hydra slayer helmet -> Cosmetics+Melee+Slayer; (i) -> Cosmetics+Melee+Range+Mage+Slayer** (coloured-slayer-helm rule). **Boss-head trophies (alchemical/stuffed hydra heads, kq head) -> Construction** (drop Slayer/Prayer). **Jar family (jar of chemicals) -> Misc.**
- **Brimstone key (Konar slayer-task-locked) -> Slayer** (drop Misc). [FLAG] **Ornate (Crack the Clue II) prestige armour kept Cosmetics-only** despite iron-tier defence. [FLAG]
- **GE boxed sets route by contents:** mystic sets -> Mage; gilded d'hide set -> Cosmetics + Range; Justiciar set -> Melee. **Oily/pearl fishing rods -> Fishing + Cosmetics.** **Clue containers (nests/bottles) + all clue scrolls -> Misc** (drop the gathering-skill source).
- **2019 Birthday event:** fetch items (shopping list, letter, wine, beer glass of water) -> Misc; novelty/wearable (birthday cake, brewer's folly) -> Cosmetics. (event fetch-vs-cosmetic split noted)
- **Treasure Trail rewards (large block, items ~860-960):** decorative armour WITH real stats -> Cosmetics + style (3rd age, heraldic platebodies h1-h5 black/adamant/rune, gilded d'hide pieces, Fremennik kilt, Rangers' tights, spiked manacles, climbing boots (g)); pure-cosmetic clue rewards (masks, slippers, capes, outfits, dragon masks) -> Cosmetics; novelty weapons with no real stats (ham joint, gilded spade) -> Cosmetics. **Standalone ornament KITS -> Cosmetics only** (berserker/rune-scimitar/rune-defender/tzhaar/tormented kits); assembled (or)/(t)/(g)/(god) -> Cosmetics + functional style.
- **God d'hide shields (+1 prayer) -> Cosmetics + Range + Prayer** (god-d'hide rule). [FLAG] **Amulet of power (t) / amulet of avarice-style all-style amulets -> all combat styles.** [FLAG] **Cape of skulls -> Cosmetics** (all-style defence, no offence). [FLAG]
- **Gilded pickaxe -> Cosmetics + Mining + Smithing; gilded axe -> Cosmetics + Woodcutting + Firemaking** (tool melee bash dropped, matches pickaxe). [FLAG]
- **3rd age druidic robes/cloak + monk's robes (t) -> Cosmetics + Prayer; 3rd age druidic staff -> Cosmetics + Mage + Prayer.**
- **Teleport scroll/items -> Teleports** (Watson teleport, enchanted lyre(i), battlefront teleport, basalts). **Slayer-task-locked chest keys -> Slayer** (brimstone key, Larran's key; drop Misc). [FLAG] Generic dungeon keys (grubby/temple/mossy) -> Misc.
- **Clue containers (nests/bottles/geodes) + all clue scrolls + reward caskets + puzzle-box sliding pieces -> Misc.** **Jar family (chemicals/eyes/decay) -> Misc.** **Boss-head trophies (hydra/kq heads, stuffed) -> Construction.**
- **LMS minigame items -> combat tab, drop Misc** (cooked karambwan heals 18 -> Melee+Range+Mage; super combat -> Melee; ranging potion -> Range).
- **Forthos In Search of Knowledge miniquest items (tomes, tattered pages, lamp of knowledge) + temple coin -> Quests.** **Wine of zamorak -> Herblore** (ranging/bastion secondary, wiki). **Giant egg sac -> Herblore** (red spiders' eggs). **Warm-clothing cosmetics (frog/bear/demon feet, wolf cloak) kept Cosmetics-only.** [FLAG]

## Batch 6 audit — RESOLVED rulings (Matt-approved, now binding for all future batches)

Matt approved every batch-6 slice call EXCEPT these two families (corrected):
- **Ensouled heads -> Prayer ONLY** (NOT Mage+Prayer). The reanimation Magic XP is incidental: "despite giving mage XP they are a prayer input. This same logic would put any item that can be alchemised in the magic tab and we can't have that." Strong restatement of "XP-given != skill input/output." Apply to anything whose only Magic link is XP-from-using (reanimation, alch, etc.).
- **Harpoons -> Fishing ONLY.** Nobody fights with a harpoon (drop Melee even though dragon/infernal harpoons have melee stats). For the infernal harpoon also drop Cooking: the auto-cook is a *passive function of fishing*, not cooking-with-it. GENERALISES to auto-processing skilling tools: **infernal axe -> Woodcutting + Firemaking only** (auto-burn is passive), **infernal pickaxe -> Mining + Smithing only** (auto-smelt is passive), and **barb-tail harpoon -> Fishing only** if a prior batch gave it Melee. Watch for these in batch 7+.

Everything else on the batch-6 slice was approved as-is, including: CoX raid food >=12 -> Cooking+Melee+Range+Mage (no Slayer); Xeric's aid -> Herblore+Melee+Range+Mage (Saradomin-brew-style); overload -> all combat; ammo-precursor split (javelin tips keep Range, ballista build parts drop it); ornament KITS -> Cosmetics only; demonbane/dragonbane/imbued-heart/battlestaff drop spurious Slayer; god d'hide boots -> Cosmetics+Range+Prayer; grimy CoX herbs -> Farming+Herblore; **Ardougne max cape + achievement diary cape -> Cosmetics+Teleports**; jar family -> Misc; Shayzien supply armour -> Mining + Smithing; graceful recolours -> Agility + Thieving + Cosmetics; dynamite -> Mining + Smithing.

## Batch 6 — settled patterns (provisional, applied; pending Matt's batch-6 audit)

These were applied consistently in batch 6. Most are extensions of existing rules; the flagged ones are on `audit-slice-batch-006.csv`.
- **Minigame/raid-locked FUNCTIONAL gear -> its combat tab, drop Misc** (Last Man Standing weapons/armour/potions; LMS prayer pot->Prayer, super energy->Agility + Thieving, runes->Mage, arrows->Range, high-heal food->Melee+Range+Mage). Supply packs drop spurious Slayer.
- **CoX (Chambers of Xeric):** raw fish->Cooking+Fishing; raw bats->Cooking+Hunter; cooked raid food >=12 heal -> Cooking+Melee+Range+Mage (raid-locked, NO Slayer), <12 -> Cooking; grimy + clean herbs -> Farming+Herblore, herb seeds->Farming; combat potions elder->Herblore+Melee, twisted->Herblore+Range, kodai->Herblore+Mage; Xeric's aid (defensive PvM, Saradomin-brew-style)->Herblore+Melee+Range+Mage; overload->Herblore+Melee+Range+Mage; revitalisation/prayer-enhance->Herblore+Prayer; lore books + fight-mechanic items (medivaemia, keystone, cavern grubs)->Misc; CoX kindling->WC+FM (chopped, no fletch); gourd vials->Herblore.
- **Cosmetic/clue armour WITH real stats -> Cosmetics + style** (gilded/trimmed steel, samurai, of-darkness=Mage, HCIM, corrupted, house banners, GE armour sets). Standalone ornament KITS -> Cosmetics only (drop combat); assembled (or)/(t) weapons keep functional tab + Cosmetics. SGS(or) -> +Prayer (SGS special).
- **Teleports:** teleport tablets, Chronicle + teleport card, royal seed pod, charge-dragonstone-jewellery scroll, ring of wealth(i), ancient tablet (Xeric's-talisman teleport unlock) -> Teleports.
- **Drop spurious Slayer from monster-DROP combat gear that isn't slayer-task-locked**: demonbane (Arclight), dragonbane (dragon hunter crossbow), dragon thrownaxe, imbued heart, elemental battlestaves. KEEP Slayer on genuinely slayer-locked (leaf-bladed battleaxe 55-Slayer-req; slayer helms; dark totem pieces from superior-slayer drops).
- **Unenchanted zenyte jewellery -> Crafting** (drop Mage); enchanted by function (tormented->Mage, anguish->Range, torture->Melee, suffering(+i)->Melee+Range+Mage all-style defensive ring).
- **Ballista BUILD parts (frames/limbs/springs/incomplete/unstrung/monkey tail) -> Fletching only** (drop Range, like crossbow parts). Javelin TIPS -> Fletching + Mining + Smithing + Range (ammo precursor keeps Range per bolt-tip precedent). Finished ballista + dragon javelin -> Range (drop Fletching).
- **Amulet of glory/eternal glory/power -> Melee+Range+Mage** (+Teleports if it teleports). God-blessed d'hide (Ancient/Bandos/Guthix/Armadyl/Sara/Zammy) -> Cosmetics+Range+Prayer.
- **Coloured slayer helm**: non-(i) -> Cosmetics+Melee+Slayer; (i) -> Cosmetics+Melee+Range+Mage+Slayer.
- **Pets (skilling + boss) -> Cosmetics** (drop the skill). **Jar family -> Misc.** **Shayzien SUPPLY armour (unequippable, smithed) -> Mining + Smithing.** **Graceful region/Kourend/Agility-Arena RECOLOURS -> Agility + Thieving + Cosmetics.** **Arceuus Library fetch-books + reward XP books -> Misc.** **Dynamite -> Mining + Smithing** (FM-XP-from-lighting incidental). **Clue containers (bottles/nests/geodes) -> Misc** (drop the gathering-skill drop-source). **Firelighters/bruma torch/pyromancer/warm gear -> WC+FM.** **POH altar signets -> Construction.** **Burnt page -> Mage** (tome of fire charge). **Soul bearer -> Prayer.** **Unobtainable items -> Misc** (combat scarred key, quest-decoration BGS).
- **Saltpetre -> Farming** (spade-dug fertiliser mineral, not pickaxe-mined). **Seed box -> Farming.** **Empty jug pack -> Cooking** (wine container). **Infernal harpoon -> Cooking+Fishing+Melee** (auto-cook + wieldable).
- **Achievement diary cape -> Cosmetics+Teleports. Ardougne max cape -> Cosmetics+Teleports** [both on slice — note the still-open base max cape question].

## god d'hide/vestment Cosmetics correction — APPLIED 2026-05-24 (kept for reference)

DONE: the items below had Cosmetics restored in decisions.jsonl + log-batch-005.csv and are logged in corrections-batch-005.csv. (Originally I wrongly dropped Cosmetics before resolving the "prestige gear keeps Cosmetics" rule.) Restored Cosmetics to these (also DROP Mage from the robe tops/legs to match the ancient/armadyl/bandos versions). Fix BOTH `decisions.jsonl` and `log-batch-005.csv` new_tabs:
- d'hide (-> Cosmetics,Range,Prayer): 10370, 10372, 10374, 10378, 10380, 10382, 10386, 10388, 10390.
- croziers (-> Cosmetics,Mage,Prayer): 10440, 10442, 10444.  mitres (-> Cosmetics,Mage,Prayer): 10452, 10454, 10456.
- cloaks (-> Cosmetics,Prayer): 10446, 10448, 10450.  stoles (-> Cosmetics,Prayer): 10470, 10472, 10474.
- robe tops (-> Cosmetics,Prayer; drop Mage): 10458, 10460, 10462.  robe legs (-> Cosmetics,Prayer; drop Mage): 10464, 10466, 10468.
- DO NOT touch 4041/4042 (Castle Wars team cloaks, correctly Misc).

## Batch 5 ruleset — RESOLVED (Matt-approved in the batch-5 audit, now binding for all future batches)

Matt approved every one of these in the batch-5 slice. Keep applying them. NEW settled exception from his audit:
- **Long bone + curved bone -> Construction ONLY (drop Prayer).** Though buriable for 15.1 Prayer XP, they're always sold to Barlak for Construction; the Prayer tag just confuses players. One-off exception to the "buriable bones get Prayer" rule (does NOT generalise to other bones, which still get Prayer).

The rest (all confirmed):

- **Enchanted jewellery -> classify by FUNCTION only.** Crafting stays on the UNENCHANTED gem base; the enchanted item gets its functional tab(s). Mage kept ONLY if it has real magic combat stats (not merely from being enchanted). amulet of glory / combat bracelet / regen bracelet -> Melee+Range+Mage(+Teleports if it teleports); abyssal bracelet -> Runecraft; skills necklace + digsite pendant + house teleport tablets -> Teleports; ring of wealth -> Teleports+Misc; castle wars / inoculation bracelet -> Misc; bracelet of clay -> Mining + Smithing.
- **Incidental prayer bonus does NOT add Prayer.** A small/passive +prayer on DPS gear (berserker necklace +3, godswords +8, spears +2, armadyl cbow +1) stays in its combat tab only. Prayer added ONLY when the prayer bonus/mechanic is defining (ancient mace prayer-drain special + 25 Prayer req -> Melee+Prayer; Saradomin godsword special restores prayer -> Melee+Prayer).
- **Imbued slayer-bonus headgear (black mask (i), slayer helm (i)) -> Melee+Range+Mage+Slayer** (the (i) imbue extends the on-task bonus to all 3 styles). Slayer ring -> Slayer+Teleports (teleports + checks task). Imbued combat rings -> their style tab.
- **Anti-undead gear (salve amulet (i)/(ei)) -> Melee+Range+Mage** (boosts all 3 styles vs undead; NOT a slayer-task item, so no Slayer).
- **Spurious "Slayer" tag dropped from non-slayer items** (generic vial/feather/bait packs, looting bag, tridents, skills necklace, occult necklace, abyssal/kraken tentacle, Bandos armour). Genuinely slayer-locked items KEEP Slayer (broad ammo, leaf-bladed sword, black/slayer helms, slayer ring).
- **High-heal cooked food (>=12 HP heal) -> Cooking + Melee + Range + Mage + Slayer** (e.g. dark crab heals 22). Sub-12 cooked food -> Cooking only. [CONFIRM exact tab set with Matt - first batch-5 example.]
- **Minigame-locked FUNCTIONAL combat consumables -> their combat tab, NOT Misc.** Barbarian Assault / Nightmare Zone runes -> Mage; BA arrows -> Range; NZ pickaxes -> Melee (used as weapons, "doesn't mine"). NZ-only potions are NOT Herblore-brewable -> functional tab: super ranging -> Range, super magic -> Mage, overload -> Melee+Range+Mage, absorption -> Misc (no skill boost). Pure minigame tokens/seals/utility (void seals, ecumenical key, looting bag) -> Misc.
- **Smithing precursors -> Mining + Smithing, NOT the downstream weapon's combat tab** (godsword shards 1/2/3, godsword blade; draconic visage). Direct-attach components with no production skill inherit the weapon's tab (godsword hilts -> Melee; odium/malediction ward shards -> the ward's style tab). Finished smithed weapons -> combat tab (hastae -> Melee).
- **Decorative gear with real combat stats -> Cosmetics + combat-style tab** (Castle Wars decorative armour; heraldic helms / painted kiteshields from batch 4).
- **Antifire potions (base / extended / barbarian mix) -> Herblore only** (dragonfire resistance is not a combat-stat boost). Barbarian mixes mirror their base potion's tabs + Herblore (defence potions -> Melee per convention; super restore -> Herblore+Prayer+Slayer).
- **Event/holiday items -> Cosmetics** (incl. "fun weapons" like cursed goblin hammer/bow/staff with no real stats; pets -> Cosmetics). JMod spawner tools (holiday tool, "Easter") -> Misc.
- **Other:** elemental staves / battlestaves / tridents -> Mage (melee bash incidental); soft clay -> Crafting (pottery); skilling-XP outfits (prospector, lumberjack, graceful) -> their skill tab; readable guide/lore books incl. glassblowing book -> Misc; clue scrolls -> Misc; chinchompas -> Hunter+Range; teleport tablets/spheres -> Teleports; XP lamps -> Misc or Quests (quest-reward lamps stay Quests).

## HIGH-LEVERAGE questions — ALL RESOLVED in the batch-5 audit (kept for reference)

Matt ruled on all five below in the batch-5 slice. Outcomes: (1) prestige/themed functional gear KEEPS Cosmetics (correction applied); (2) enchanted-jewellery-by-function CONFIRMED; (3) minigame-locked-functional->combat-tab CONFIRMED; (4) high-heal food = Cooking+Melee+Range+Mage+Slayer CONFIRMED; (5) anti-undead salve = Melee+Range+Mage (no Slayer) CONFIRMED. Original questions:

1. **god vestments / god d'hide** (and prestige themed FUNCTIONAL gear generally): I dropped Cosmetics (god d'hide -> Range+Prayer; god vestments -> Prayer/+Mage), which DIFFERS from skillcapes (which keep [skill]+Cosmetics). Should functional themed/prestige gear keep a Cosmetics tag too?
2. The **enchanted-jewellery-by-function** rule (drop Crafting/Mage from enchanted items).
3. The **minigame-locked-functional-consumables -> combat-tab** rule (vs treating them as Misc).
4. The exact **high-heal-food tab set** (I used Cooking+Melee+Range+Mage+Slayer).
5. **Anti-undead salve amulet** dropping Slayer (-> Melee+Range+Mage).

## How to write decisions (file-tools only — the sandbox bash CANNOT reach the dev folder)

- Append to `decisions.jsonl` with the **Edit** tool: `old_string` = the current last line, `new_string` = last line + all the new lines. (Do not Read the whole file; it's ~600KB. Read tails with offset/limit, or Grep.)
- Append to `log-batch-005.csv` the same way. Log header: `id,name,old_tabs,new_tabs,changed,rationale,confidence`. `changed` = `yes`/`no` (did tabs differ from the input's `current_tabs`). Quote fields containing commas.
- Work in ~50-item chunks; commit each chunk's decisions.jsonl + log rows before reading the next chunk.
- Wiki-check anything that lands at medium OR low confidence, using the approved web tools only (WebSearch / web_fetch on the OSRS wiki; no curl/wget/archive workarounds). If the wiki resolves it, classify and continue. If it stays unresolved: ask Matt only when the call is genuinely urgent or high-leverage; otherwise classify provisionally and add it to the audit slice. (Matt's directive, 2026-05-24.)

## The audit cadence (how Matt reviews)

1. Classify a full batch (~1,188).
2. Produce `audit-slice-batch-NNN.csv` = ~5% of the batch, **weighted to changed/low-confidence rows + EVERY rule-level judgment call**. Header: `id,name,old_tabs,new_tabs,changed,confidence,rationale,approved,comments` (leave `approved`/`comments` blank for Matt).
3. Matt fills `approved`/`comments` and hands the slice back.
4. Apply his corrections to `decisions.jsonl` **in-place**, flip the matching `log-batch-NNN.csv` rows to `changed=yes` where needed, and log every correction in `corrections-batch-NNN.csv` (header: `id,name,from_tabs,to_tabs,reason,source`).
5. Whatever he rules on a recurring family, propagate it to all future batches.

## Batch-4 audit: DONE (2026-05-23)

Matt reviewed `audit-slice-batch-004.csv` (64 approved, 5 corrected). Corrections applied to `decisions.jsonl`, `log-batch-004.csv`, and `corrections-batch-004.csv`. The 5 rulings (now binding for all future batches) are recorded in `questions-for-matt.md` under "Matt's batch-4 audit rulings (RESOLVED)". Summary:

1. **NEW PRINCIPLE: "XP given != skill input/output."** An item gets a skill tab only if it's that skill's input or produced output, NOT merely because using it grants XP. -> bagged garden decorations = Construction only (dropped Farming); grand seed pod = Teleports only (dropped Farming). (Contrast: Auguste's sapling stays Farming -- genuinely planted as a Farming input.)
2. **Unique Thieving/Agility outputs & currency DO go in Agility + Thieving** (Pyramid Plunder artefacts reverted Misc -> Agility + Thieving; also marks of grace, unique pickpocket keys/valuables). Test: unique output/currency of that skill with no role as another skill's input. Non-unique items that ARE another skill's input (stall gems -> Crafting, stall cakes -> food) do not qualify. (HAM keys stayed Misc -- generic, not unique currency.)
3. **Disguise outfits whose only real use is quest/area content -> Quests + Cosmetics** (Vyrewatch + Citizen). Contrast: bomber jacket stayed Cosmetics (real ongoing Wintertodt use).
4. **Sinew -> Crafting only** (a non-food cooking-skill product doesn't get Cooking; the Cooking exemption is for FOOD).
5. **Skillcapes -> [skill] + Cosmetics: CONFIRMED.** Keep applying to all 99 capes + quest cape. Salamanders verified Hunter+Melee+Range+Mage (multi-style, wiki-confirmed).

## Settled ruleset

The complete, current ruleset lives in `questions-for-matt.md` — read it. It accumulates a section per batch. Batches 1-3 rulings are RESOLVED (Matt-approved). The "Batch 4 COMPLETE" section lists the batch-4 **provisional** rules (applied but pending his audit). Key batch-4 provisional rules to keep applying in batch 5 until told otherwise:

- Skillcapes -> [skill] + Cosmetics; quest point cape -> Quests + Cosmetics.
- Heraldic helms + decorative kiteshields (rune/steel, painted or Treasure-Trail) -> Cosmetics + Melee. Banners/flags (no combat stats) -> Cosmetics only.
- POH furniture flatpacks (incl. kitchen "ale/cider/bitter" barrel flatpacks, globes, costume-room storage) -> Construction. Bagged garden decorations -> Construction + Farming.
- Void top/robe/gloves -> Melee+Range+Mage; void mace -> Melee+Mage+Prayer.
- Salamanders + swamp lizard -> Hunter+Melee+Range+Mage; salamander tars -> Herblore+Hunter+Range.
- Teleport-FUNCTION items -> Teleports (pharaoh's/skull sceptre [+Mage], grand seed pod [+Farming], minigame rum).
- Quest-locked-but-USABLE gear -> classify by function, drop Quests (lunar gear -> Mage; proselyte/initiate -> Melee+Prayer; magic essence -> Herblore[+Mage]; crystal saw -> Construction; suqah hide/leather -> Crafting; lunar ore/bar -> Mining+Smithing). Quest-INTERNAL-only items (no post-quest use) stay Quests (waking-sleep-vial chain, Slug Menace "special runes" that can't cast, timber beam).
- Minigame loot/tokens/utility + animation items -> Misc (Pyramid Plunder artefacts, Warriors' Guild tokens, HAM keys, shot/barrels, stick/bucket/torch). MTA coins -> Mage.
- Disguise/costume/holiday clothing -> Cosmetics (pirate, Vyrewatch/Citizen, bomber jacket/cap, event gear).
- Crossbow tree: unstrung (u) crossbows + stocks + limbs are Fletching/Mining+Smithing intermediates (drop stray Range); finished crossbows/bolts -> Fletching+Range; gem bolt tips -> Fletching+Range (chiselling cut gems is Fletching, drop Crafting precursor); bolt mould -> Crafting tool; sinew -> Cooking+Crafting; spiky vambraces (all variants) -> Range; mith grapple split by stage.
- Smaller: enchant tablets -> Mage; clockwork -> Crafting; bolt of cloth -> Construction+Crafting; readable guide/lore books -> Misc; sawdust (no use) -> Misc; sub-12-heal cooked food -> Cooking only; weight-reducing capes -> Agility+Hunter.

Plus all earlier settled rules: seeds -> Farming only; prayer-bonus combat gear -> combat+Prayer; cooked food 12+ heal -> all 4 combat tabs; universal heal/defence PvM potion (e.g. Saradomin brew) -> Herblore+Melee+Range+Mage+Slayer; finished arrows/bows -> Range; cut gems -> Crafting+Fletching; nails -> Construction+Fletching; runes -> Mage+Runecraft; tiaras/talismans -> Runecraft; mined-not-smelted rocks -> Mining+Smithing (precursors don't carry the downstream skill); raw armour materials -> Crafting, finished crafted armour -> combat tab; lore books -> Misc; quest/miniquest/quest-room-only -> Quests.

## Watch-for flags for batch 5+ (categories not yet hit, anticipate these)

culinaromancer's gloves (culi gloves) -> M+R+Mage; spices/spicy stew -> Cooking; diving set (fishbowl helmet/diving apparatus) -> Sailing (low conf); anti-vampyre items (rod of ivandis / Guthix balance) -> +Slayer; magic secateurs -> Farming; Barbarian Assault gear; Fremennik gear; god books; barrows sets.

## Matt's working preferences (carry these)

- **Keep grinding autonomously.** Chain many chunks per turn. Do NOT stop after each chunk, do NOT ask permission to resume. Only return to Matt for a genuine high-leverage question. He has said repeatedly: "PLEASE DON'T MAKE ME START YOU UP EVERY TIME." Decisions are applied provisionally and corrected later (like Saradomin brew was), so a wrong guess is cheap.
- Never offer breaks / sleep / "wrap up soon" — never steer toward closure. He signals when he's leaving.
- Vault grounding: at the start of a session read the four files under `/var/home/mhersee/vaults/personal/claude.d/` and `.../work/claude.d/` (life.md + claude.md each). They take precedence over defaults.

## Verification (do this at each batch end)

- `decisions.jsonl` entry count == 1,188 x (batches done). `log-batch-NNN.csv` == 1,188 data rows + header.
- Spot-check continuity (no skipped/duplicate IDs in the batch range) and that `changed` flags match `current_tabs` vs final `tabs`.
