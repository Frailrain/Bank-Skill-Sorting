# Brief #91 — Dynamic slayer tab: rebuild per assigned task

## Problem

The slayer tab today is a frozen snapshot of the Brief #50 "task loadout"
curation, preserved verbatim through every LLM regen. In-game review shows
both misses (Black mask (i) absent) and noise (Abyssal book and other
LLM-routed oddities). Patching item-by-item fights the real issue: a static
list can't express "what you should grab for THIS task with YOUR bank."

## Goal

When the player receives a slayer task, the slayer tab regenerates to show,
in order:

1. **Required & protection items** for the task, from wiki task data
   (rock hammer / ice cooler / bag of salt / fungicide spray, earmuffs,
   facemask, nose peg, mirror shield, witchwood icon — with any slayer helm
   variant satisfying all protection rows).
2. **Best owned gear** — strongest item per equipment slot the player owns
   for the task's recommended combat style, plus the strongest
   **non-degradable** alternative when the top pick degrades (blowpipe →
   also show best crossbow/bow; barrows → also show best regular plate).
   Task-specific uniques get priority boosts (salve (ei) vs undead,
   arclight vs demons, dragonhunter weapons vs dragons, leaf-bladed vs
   turoth/kurask, broad ammo).
3. **Relevant potions** — combat style pots, prayer/restore, antifire or
   antivenom when the task calls for it.
4. **A few food options** — top 2–3 heal tiers owned + karambwan.
5. **Teleports to the task location** (from task-location data) + utility
   (cannon set, expeditious/slaughter bracelets, looting bag, herb sack).

No task assigned (or feature toggled off) → fall back to the current
curated static loadout.

## Architecture

### Phase 1 — task dataset (pipeline, no client code) — **DONE 2026-07-09**

- Built `tools/skillbank-data/slayer_tasks.py` (also wired as
  `scraper.py --slayer-tasks [--slayer-tasks-write]`), reusing
  `wiki_page.py` fetching/caching and `llm_classifier`'s Messages-API
  plumbing.
- 79 canonical `Slayer task/*` wiki pages found → **78 tasks scraped**
  (the 79th, `Slayer task/Dragons`, is a disambiguation page with no
  infobox). LLM extraction cost $1.23, per-page responses cached under
  `cache/llm-slayer-tasks/`.
- Per task: slayer/combat/other reqs, per-master assignment ranges +
  weights, `task_item_id` (the game's task item, e.g. 4149 = Abyssal
  demon — 32/78 pages carry it; runtime matching is by normalised task
  NAME, ids are icon sugar), `recommended_style`, `protection_prayer`,
  required / protection / recommended items **resolved to canonical item
  ids** (0 unresolved — `NAME_ALIASES` maps wiki shorthand like "Dwarf
  multicannon" → cannon set + cannonballs, and `EXTRA_ITEM_IDS` covers
  post-cache items Antler guard 31081 / Tortugan shield 31398), attribute
  flags, cannon/burst viability, locations with per-location
  cannon/multicombat flags.
- Outputs: `out/slayer-tasks.json` (full artifact),
  `out/slayer-tasks-report.md` (Cowork review sheet), and the shipped
  resource `src/main/resources/com/skillbank/slayer-tasks.json` (78 KB).
- Remaining Phase 1 action: Matt eyeballs `out/slayer-tasks-report.md`
  (78-row table: style / prayer / required / protection / cannon / burst).

### Phases 2+3 — **IMPLEMENTED 2026-07-09** (v1)

- `SlayerTaskData.java` — lazy-Gson loader for the bundled
  `slayer-tasks.json`, keyed by normalised task name with singular/plural
  aliases (matches the core Slayer plugin's `slayer.taskName` RSProfile
  value).
- `SlayerLoadoutBuilder.java` — the loadout algorithm: Task & protection
  (required items, owned slayer helm/black mask leads, protection items) →
  Weapons (top-2 owned for the wiki-recommended style, ranked by style
  requirement then tier; strongest **non-degradable** appended when every
  top pick degrades — degradables are a curated token list in this class
  for v1 instead of the planned pipeline `nd` flag) → Armour (best owned
  per slot) → Recommended extras → Potions (style families + prayer
  restores + antifire when the task is fiery/dragon, best dose only) →
  Food (top-3 heal families + karambwan) → Cannon (when task-viable) →
  Utility (expeditious/slaughter, looting bag, herb sack, bonecrusher,
  slayer ring, gems). Each group starts a fresh bank row; non-loadout
  slayer-tagged items auto-append below the loadout.
- Plugin wiring: `dynamicSlayerTab` config toggle (default on); seed adds
  the task's required/protection/recommended ids to the slayer tag;
  `buildAndSaveLayout` routes slayer through the loadout builder when a
  task is active (static tab is the no-task fallback); `ConfigChanged` on
  `slayer.taskName` triggers a full re-reconcile + re-render.

**v2 (same day, after in-game testing):** the wiki task/monster pages carry
`{{Recommended equipment}}` templates — slot-by-slot RANKED loadouts per
combat style, plus elemental weaknesses in prose ("wyrms' 50% earth
weakness, lava strykewyrms' 50% water"). These are now parsed
DETERMINISTICALLY (no LLM, $0) from the task page or the monster
page//Strategies fallback: 19/78 tasks carry full wiki loadouts (7 with
all three styles). The loadout builder walks each slot's ranks and
surfaces the FIRST item the player owns — the wiki's recommended setup
personalized to the bank — one gear row-block per style in page order,
with the matching elemental tome added to the mage row. The single-style
requirement/tier heuristic remains the fallback for the 59 tasks without
templates. Konar-style `taskLocation` is read from the slayer config and
gates cannon rows per-location.

Remaining for later phases: teleports-to-task-location group, wilderness
budget-gear filter (decision #4), pipeline-emitted `nd`/value flags,
wiki-loadout coverage for the 59 template-less tasks (most monster pages
keep gear advice in prose — an LLM pass could structure those into the
same slot-ranked shape).

### Phase 2 (original plan) — degradable + style metadata (pipeline)

- Add `nd` (non-degradable) boolean to `item-meta.json`: hand-curated
  degradable-family list in `mapping.py` (barrows pieces, crystal gear,
  blowpipe, trident variants, serp helm, amulet of the damned, etc. —
  ~50 name families).
- item-meta already carries tier (`t`), slot (`sl`), weapon class (`wc`),
  and requirement data — enough to rank "strongest owned per slot per style".

### Phase 3 — runtime (Java)

- **Task detection**: `VarPlayer.SLAYER_TASK_CREATURE` (varp 395) + the
  slayer-task enum for the creature name, with the core Slayer plugin's
  RSProfile config (`slayer.taskName`) as fallback/cross-check. Subscribe
  to VarbitChanged; debounce; rebuild only when the task actually changes.
- **New `SlayerLoadoutBuilder`** (mirrors `SkillBankLayoutBuilder`
  patterns): pure function `(taskData, bankContents, itemMeta) → ordered
  id list + section breaks`. Unit-testable without a client.
- **Seeding change**: `slayer` becomes a *dynamic* tag — exempted from the
  static SkillBankData reconcile (the existing slayer-preserve mechanism
  already special-cases it; promote that to an explicit "runtime-managed
  tab" flag). Tag contents + layout rewritten atomically on the client
  thread on: task change, bank open, feature toggle.
- **Config**: `dynamicSlayerTab` toggle (default on), falls back to static
  loadout when off or when task data is missing.

### Phase 4 — polish

- Section headers in layout: Required / Gear / Alternatives / Potions /
  Food / Teleports / Utility.
- Edge cases: boss tasks (use the boss's normal recommendations), partner
  tasks.
- Wilderness tasks: budget-gear ranking (see Decisions) with a config
  toggle to disable.

## Sizing

- Phase 1: one focused session + a Cowork review of the generated table.
- Phase 2: small (one mapping.py list + emit change).
- Phase 3: the big one — builder + seeding exemption + detection, ~2–3
  sessions with in-game iteration.
- Phase 4: trailing polish.

## Decisions (Matt, 2026-07-09)

1. **Style choice** — use the wiki-recommended style for the task (burst
   for dust devils, melee for gargoyles, …). The scrape must therefore
   capture a single `recommended_style` per task. No per-style config in v1.
2. **Gear source** — bank-only. Layouts can only render banked items
   anyway; if the best item is equipped, the next-best banked item shows.
3. **Cannon & burst rows** — shown only when the task is cannon-viable AND
   at least one task location allows a cannon. The scrape must capture
   per-location cannonability, not just the monster flag.
4. **Wilderness tasks** — budget-gear ranking: prefer cheap/replaceable
   tiers, skip expensive and untradeable-on-death items. Config toggle to
   disable and rank normally. Needs a value signal in item-meta (GE/alch
   value is already in the item dump — emit a coarse `vt` value-tier flag,
   or derive from existing tier + a curated "never risk" list).
