# Cowork audit prompt — v2 (post Brief #63)

This replaces v1. Two structural changes you need to absorb before resuming
the per-item audit:

1. **`wc_fletching` is gone.** The combined Woodcutting + Fletching tab has
   been split. Woodcutting items now live alongside Firemaking items in
   `Woodcutting + Firemaking`. Fletching items live in their own `Fletching`
   tab. Items can still belong to both — a chisel used for fletching stocks
   AND for crafting jewellery should list both `Fletching` and `Crafting`.

2. **Combined tabs renamed for consistency.** The three combined tabs now
   use the canonical "Word + Word" style:

   | Old (used in v1)      | New (use this in v2)           |
   |-----------------------|---------------------------------|
   | `wc_fletching`        | (split — see above)             |
   | `mining_smithing`     | `Mining + Smithing`             |
   | `agility_thieving`    | `Agility + Thieving`            |

   The tab count is now **23**.

Your earlier decisions (`audit/decisions.jsonl`, `audit/log-batch-001.csv`)
have been mechanically migrated to the new names. Items previously in
`wc_fletching` were split into `Fletching` vs `Woodcutting + Firemaking`
using a name-pattern rule (axes/logs/firemaking outfit → W+F; bows / arrows /
bolts / darts / strings / tips → Fletching). Audit those items again if the
split is wrong for any specific case.

## The 23 tabs

For each tab below: name, item categories, sample items.

| Tab | What goes here | Examples |
|---|---|---|
| Melee | Attack/Strength/Defence weapons & armour, combat food, melee combat potions | Abyssal whip, Bandos chestplate, Dragon scimitar, Saradomin brew |
| Range | Bows (when strung & wieldable), crossbows, ranged armour, ammo (when wieldable), ranged combat potions | Twisted bow, Magic shortbow (strung), Magic shortbow, Dragon arrow, Masori body |
| Mage | Runes, staves, wands, magic armour, magic combat potions, occult/tormented neck | Ahrim's robetop, Trident of the swamp, Tormented bracelet, Magic potion |
| Prayer | Bones, ashes, prayer-restore potions, holy symbols, prayer-training tools | Dragon bones, Holy wrench, Prayer potion(4), Dragonbone necklace |
| Cooking | Cooked + raw food, cooking ingredients (flour/eggs/milk/sugar), cooking outfit & tools | Anglerfish, Raw shark, Pot of flour, Cook's hat |
| Woodcutting + Firemaking | Axes, logs, pyre logs, tinderbox, firelighters, shade items, Wintertodt items, lumberjack/pyromancer outfits, Forestry items | Bronze axe, Magic logs, Tinderbox, Pyromancer top, Bruma kindling |
| Fletching | Bow strings, unstrung bows, strung bows (when not yet wielded), arrow shafts, headless arrows, dart tips, bolt tips, feathers (for fletching), finished arrows/darts/bolts/javelins, fletching cape | Bow string, Magic shortbow (u), Bronze arrowtips, Dragon arrow, Fletching cape |
| Fishing | Raw fish, fishing rods/nets/cages/harpoons, bait, fishing outfit | Raw shark, Fishing rod, Karambwan vessel, Angler hat |
| Firemaking | Tinderbox-and-log content (also appears in Woodcutting + Firemaking — items can be in both); for items where firemaking is the dominant use, list both tabs | Tinderbox, Logs, Pyromancer outfit, Firemaking cape |
| Crafting | Gems, moulds, leather, dragonhide pre-craft, glass, ball of wool, needle, chisel, gauntlets | Sapphire, Molten glass, Soft leather, Needle |
| Mining + Smithing | Ores, bars, pickaxes, prospector outfit, smiths/foundry outfit, hammer | Iron ore, Steel bar, Rune pickaxe, Prospector helm |
| Herblore | Herbs (clean + grimy), vials, secondaries, all potion doses, pestle and mortar | Ranarr weed, Grimy ranarr weed, Vial, Snape grass, Super combat potion (4) |
| Agility + Thieving | Graceful outfit, marks of grace, rogue's outfit, blackjacks, lockpicks | Graceful hood, Mark of grace, Rogue mask, Lockpick |
| Slayer | Task-mandatory gear (broad bolts, slayer helms, leaf-bladed sword), enchanted gem, eternal/pegasian/primordial rings, salve amulet, slayer ring | Slayer helmet, Enchanted gem, Broad arrows, Salve amulet (i) |
| Farming | Seeds (all kinds), saplings, secateurs, watering can, rake, spade, compost, farmer outfit | Ranarr seed, Magic sapling, Watering can(8), Farmer's strawhat |
| Runecraft | Essence (pure/daeyalt/guardian), talismans, tiaras, pouches, wicked hood, GotR items | Pure essence, Air talisman, Large pouch, Wicked hood |
| Hunter | Box traps, bird snares, butterfly nets, impling jars, camouflage outfits, salamanders | Box trap, Eagle feather, Larupia top, Black salamander |
| Construction | Planks (all kinds), nails, saw, limestone, soft clay, marble blocks, gold leaf | Mahogany plank, Steel nails, Saw, Marble block |
| Misc | Coins, GE-only items, storage bags (looting bag/seed box/coal bag), keys, clue scrolls, minigame-exclusive variants | Coins, Looting bag, Seed box, Reward casket (master) |
| Quests | Quest-only reward gear, diary-locked gear, boss uniques (DKS rings, Zulrah scales), minigame gear with no functional use outside | Quest point cape, Berserker ring, Zulrah's scales, Fighter torso |
| Sailing | Sailing skill content — navigation tools, ship parts, sailing-specific fish, sailing cape, dock items | Compass, Sailing cape, Raw arctic char, Ship's wheel |
| Cosmetics | Pure cosmetic outfits (Treasure Trail third-age, holiday rares, partyhats, h'ween masks), ornament kits, purely-visual items | Red partyhat, Halloween mask (green), Dragon scimitar ornament kit |
| Teleports | Teleport tablets, jewellery teleports, ectophial, royal seed pod, Kharedst's memoirs, Drakan's medallion, Pharaoh's sceptre — anything whose primary purpose is teleporting | Amulet of glory(4), Teleport to house, Ectophial, Skills necklace(5) |

## Audit rules (carry-over from v1)

These have not changed. Brief restatement:

- **Use-context wins over source.** A dragon mace is `Melee`, not `Slayer`,
  even if a slayer monster drops it. A skilling tool stays in its skill tab
  even when the quest gates its use.
- **Inputs-only rule.** An item belongs in the tab of the skill that
  consumes it as an input, not the skill that produces it as an output —
  unless the producing skill uses it again for another same-skill step. So
  arrowtips are `Mining + Smithing` produced but `Fletching` consumed
  with no further smithing step — file them in `Fletching` only. Bolt tips
  and unstrung bows already comply.
- **Combat-style exclusivity.** An item must not appear in more than one of
  `Melee` / `Range` / `Mage`. Pick the style it's actually used for.
- **Quest items.** Only items with no enduring skill or combat function
  stay in `Quests`. Permanent skill tools (e.g. Ammo mould) drop the
  `Quests` tag and live in the function-skill tab.
- **Ornament-kitted variants** keep their base item's stat tab AND
  `Cosmetics`.
- **LMS / minigame-exclusive variants** go to `Misc` + the functional stat
  tab.
- **Pets** (Beaver, Rocky, Heron, Olmlet, etc.) go to `Cosmetics`.
- **Teleports is additive.** A teleport tablet keeps `Mage` (or wherever
  else it natively lives) AND `Teleports`. A jewellery teleport keeps
  `Crafting` AND `Teleports`.

## Output format

Per-item decision JSON line in `audit/decisions.jsonl`:

```json
{"id": 4151, "name": "Abyssal whip", "tabs": ["Melee", "Slayer"], "rationale": "<one sentence>", "confidence": "high|medium|low"}
```

Tabs must be from the 23-name list above (exact spelling, exact case, `+`
with spaces in the combined names). Each item should appear in at least one
tab — if you truly believe it belongs in none, set `tabs: []` and explain
in the rationale.

## Resuming

You were at ID 7301 when the pause hit. The migrated `audit-input.jsonl`
has every prior decision's `current_tabs` already in v2 form, so the resumed
session reads the same data shape as your prior batches.

Items previously routed via the mechanical wc_fletching split (i.e.
currently tagged `Fletching` or `Woodcutting + Firemaking` solely as a
migration artifact) should be re-evaluated when you reach their IDs. The
split is best-effort; the audit is authoritative.
