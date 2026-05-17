# Brief: SkillBank data scraper

> **Heads up:** This file supersedes a draft titled "OSRS Wiki Item ID Scraper". The chosen approach is **osrsbox-db primary**, not the OSRS Wiki Cargo/Bucket API. See "Why osrsbox-db" below.

## Goal

Produce a one-shot Python tool that systematically expands `SkillBankData.java` by pulling item IDs and metadata from `osrsbox-db`. The tool produces a reviewable `SkillBankData.java.proposed` file plus a diff report. The human approves and merges by hand — the tool never touches the live plugin source automatically.

## Why osrsbox-db (not the wiki)

Empirical recon found:

1. **The wiki has no Cargo extension.** `action=cargoquery` returns `badvalue`. The original draft's recommended path doesn't exist.
2. **Wiki Bucket API exists but its schema is strict and undocumented.** `select('*')` is rejected. Some field names from the original draft (e.g. `release`) are not real (`Field release not found in bucket infobox_item`). Field discovery has to be empirical.
3. **Most categories the draft named don't exist.** Spot-check of 15 example categories (`Cooked_food`, `Daggers`, `Two-handed_swords`, `Combat_potions`, `Strength_potions`, `Smelted_bars`, `Hatchets`, `Bows` …) returned zero members. Only a handful exist (`Stab_weapons`, `Food`, `Potions`, `Logs`, `Axes`, `Arrows`, `Crossbows`).
4. **`osrsbox-db` (and its maintained fork `osrsreboxed-db`) covers every item with rich metadata** — id, name, members, equipable, weapon-type, equipment slot, full combat stats (stab/slash/crush/magic/range attack & defence, strength bonus, ranged strength bonus, magic damage, prayer bonus, attack speed, combat style), cost, low/high alch, weight, quest_item, release_date, examine. ~1 MB JSON, no rate limit, no API quirks. Fetch once, cache forever.

The wiki Bucket API remains useful for niche category-membership questions the JSON can't answer (e.g. "everything dropped by Vorkath" or "every item used in Recipe for Disaster"). It's a *supplement*, not the foundation.

## Data sources

- **Primary**: `osrsbox-db` complete item dump
  - Maintained fork: `https://raw.githubusercontent.com/0xNeffarion/osrsreboxed-db/master/docs/items-complete.json` (updates through 2024)
  - Original (older but well-known): `https://raw.githubusercontent.com/osrsbox/osrsbox-db/master/docs/items-complete.json`
- **Supplement (optional)**: OSRS Wiki Bucket API at `https://oldschool.runescape.wiki/api.php?action=bucket&format=json&query=...`. Used only for category-driven cross-tags where item metadata alone isn't enough.

## Approach

### Pipeline

1. Fetch `items-complete.json` once. Cache to `tools/skillbank-data/cache/`.
2. Load tab/subcategory mapping from `mapping.py`. Each tab declares:
   - **Classification rules** that match items from the osrsbox dump (predicates on equipment slot / combat stats / name patterns).
   - **Variant allowlist** — which non-canonical variants (e.g. `Rune scimitar (or)`, `Slayer helmet (i)` recolours) to include.
   - **Subcategory section order** and tier-sort hints.
3. For each item in the dump:
   - Skip `noted=true`, `placeholder=true`, `duplicate=true`.
   - Apply each tab's classifier. An item can belong to multiple tabs (intentional cross-tag).
4. Within each tab, group by subcategory, sort within tier (low → high), break ties deterministically by item ID ascending.
5. Parse the existing `SkillBankData.java` for current per-tag ID lists.
6. Emit:
   - `SkillBankData.java.proposed` (full re-generation, with section comments `// === <subcategory> ===` between groups).
   - A diff report (`diff.txt` and stdout summary) showing additions, removals, and items in current data the scraper couldn't classify.
7. Print first/last 20 items per tab so a human can sanity-check ordering.

### Variant handling

Default to **canonical only** (`duplicate=false` and the lowest-ID variant of a given name). The tab mapping can opt-in to specific named variants via an explicit allowlist, e.g.:

```python
variant_allowlist = {
    "slayer": ["Slayer helmet (i)", "Black slayer helmet (i)", ...],
    "agility_thieving": ["Graceful hood (Hallowed)", ...],
}
```

This is mid-effort, mid-output — keeps the bank usable while still surfacing cosmetic/imbued variants players actually want.

### Tier detection

Item names are parsed for tier keywords. Sort key (lower = earlier):

| Keyword | Sort |
|---|---|
| bronze | 1 |
| iron | 2 |
| steel | 3 |
| black | 4 |
| white | 4.1 |
| mithril | 5 |
| adamant / adamantite | 6 |
| rune / runite | 7 |
| dragon | 8 |
| crystal | 9 |
| barrows | 10 |
| infernal (tool variants) | 11 |
| BiS / boss-tier (Bandos, Armadyl, Ancestral, Inquisitor, Torva, Virtus, Scythe, …) | 12+ |

Woodcutting / Fletching wood tiers: regular (1), oak (2), willow (3), maple (4), yew (5), magic (6), redwood (7). Crystal/infernal tool variants get the metal tier (8/11).

Ambiguous tokens (`dragon` in "dragon bones" vs "dragon scimitar", `magic` in "magic logs" vs "magic shortbow") are disambiguated by the tab's classifier — `dragon bones` only matches the `prayer` classifier, not `melee`.

Items with no recognizable tier go to a `Miscellaneous` subcategory at the end of the tab, sorted alphabetically.

### Dose ordering for potions

Within a potion type: 4-dose → 3-dose → 2-dose → 1-dose → unfinished. Each family stays grouped. Same for sanfew, super combat, ranging potions, etc.

## Quality requirements

- **Rate limiting**: osrsbox is static GitHub raw — no rate limit. Wiki Bucket calls (if any): 1 req/sec, `User-Agent: SkillBankTabs/0.1 (https://github.com/Frailrain/Bank-Skill-Sorting)`.
- **Caching**: items-complete.json cached locally. Wiki Bucket responses cached in a sqlite or json file keyed by query string.
- **Idempotent**: running twice with the same inputs produces byte-identical output. Sort keys must be explicit; no API-order dependencies.
- **Graceful failures**: a classifier error for one item logs and continues.
- **Detailed logging**: per-tab counts (matched / new / dropped / unclassifiable).

## Output layout

```
tools/skillbank-data/
  README.md
  scraper.py
  mapping.py
  cache/             # gitignored
    items-complete.json
    bucket-cache.json
  out/               # gitignored
    SkillBankData.java.proposed
    diff.txt
    report.txt
```

## What this does NOT do

- Touch the live `SkillBankData.java` or any Java source.
- Decide which cosmetic variants to keep (you choose via allowlist).
- Pull level requirements (could be a v2 extension).
- Categorise quest-locked items beyond name pattern (deeper quest-link data would need wiki supplement).

## Verification flow when accepting output

1. Replace `SkillBankData.java` with `SkillBankData.java.proposed`.
2. `./gradlew build` inside the distrobox.
3. `./gradlew run`, scrape logs for `com.skillbank` exceptions (multi-line aware). Both clean.
4. Open the dev client, verify the panel shows 20 tags and the proposed items appear.
5. Commit and update the plugin-hub manifest.

## Phase boundaries

This brief covers **Phase 1** — data only. **Phase 2** (Layout integration via `LayoutManager` + `AutoLayout`) is documented separately in `ordering-research.md` and is the natural follow-on after Phase 1 lands.
