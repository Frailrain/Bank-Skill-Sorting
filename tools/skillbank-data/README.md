# skillbank-data scraper

One-shot Python tool that proposes an expanded `SkillBankData.java` driven by **live OSRS Wiki Bucket API queries** as the primary item registry, with **osrsboxed-db as supplementary metadata** for fields the wiki doesn't expose (e.g. `equipable_weapon`, `weapon_type`, `quest_item`). Produces `out/SkillBankData.java.proposed` plus diff reports. Use `--promote` to overwrite the live source file.

## How it works

1. Fetches the OSRS Wiki `infobox_item` bucket (canonical items only via `default_version=true`) and `infobox_bonuses` bucket. Paginated by `.offset()/.limit(5000)`, cached to `cache/wiki/` with 24h TTL.
2. Joins items + bonuses in Python by `page_name_sub` (Bucket has no working join syntax).
3. Downloads `items-complete.json` from the maintained `osrsreboxed-db` fork as a supplementary metadata source.
4. Merges the two: wiki wins for identity, osrsbox fills in fields wiki lacks. Records cross-source discrepancies to `cache/discrepancies.json`.
5. Loads tab classification rules from `mapping.py` and applies them to the merged items.
6. Parses the existing `src/main/java/com/skillbank/SkillBankData.java` for current per-tag IDs.
7. Emits `out/SkillBankData.java.proposed`, `out/diff.txt`, `out/report.txt`, and a top-level `diff-current-vs-wiki.txt` cross-validation report.

## Run it

```
cd tools/skillbank-data
python3 scraper.py
```

No pip dependencies. Standard library only.

Flags:
- `--no-fetch` — skip osrsbox download, reuse the local osrsbox cache.
- `--refresh-wiki` — invalidate all wiki cache files and re-fetch.
- `--no-cache` — bypass wiki cache entirely this run.
- `--probe-schema` — interrogate which fields actually exist in `infobox_item` / `infobox_bonuses`; writes `cache/wiki/schema.json` and exits.
- `--tab <name>` — restrict output to one tab (iterate quickly on one classifier).
- `--out <dir>` — change output dir (default `./out`).
- `--strict` — non-additive: items in current data the classifier missed are dropped (regresses tabs). Off by default until the cross-validation diff is clean.
- `--promote` — also overwrite `src/main/java/com/skillbank/SkillBankData.java`.

## Output

```
out/
  SkillBankData.java.proposed   # full file, ready to diff against the real one
  diff.txt                       # per-tab additions / removals / unclassifiable
  report.txt                     # summary stats + first/last 20 items per tab
```

To accept the proposal: review the diff, optionally edit `mapping.py` and re-run, then `cp out/SkillBankData.java.proposed ../../src/main/java/com/skillbank/SkillBankData.java` and run the standard verification flow (`./gradlew build` + `./gradlew run` log scrape) inside the distrobox.

## Editing the mapping

`mapping.py` is the heart of the tool. Each tab is a `TabSpec` with:

- `name`: matches `SkillBankData.TAG_*` value, lowercase.
- `sections`: ordered list of `(subcategory_label, classifier_fn)`. Classifier returns truthy if the item belongs.
- `variant_allowlist`: optional list of exact names that should bypass the canonical-only filter.
- `tier_overrides`: optional dict mapping item name → explicit tier int (for items where name parsing fails).

Adding a section is the typical edit: write a classifier, pick a label, place it in the right position. Re-run and inspect the diff.

## Known limitations

- Items with no recognisable tier go to a `Miscellaneous` subcategory at the end of the tab, sorted alphabetically.
- Quest-locked items aren't classified by quest membership (would need wiki Bucket supplement) — they currently fall to `Miscellaneous` unless explicitly mapped.
- Sailing items released since osrsreboxed's last update (Oct 2024) aren't in the dump and need manual additions in `mapping.py` or a wiki supplement.
- The osrsbox fork is paused on Sailing data — verify Sailing IDs against the wiki before merging.

## Wiki Bucket supplement

If a classifier needs category data beyond what osrsbox provides (e.g. "all items dropped by Vorkath"), use `bucket_query(query_str)` from `scraper.py`. Responses are cached. Stick to 1 req/sec.

## Monthly refresh of new items

`refresh_new_items.py` is the surgical counterpart to the full regen above. It
only looks for items the plugin does not know yet, classifies them with the
LLM classifier and **adds** them to the three data files; nothing existing is
touched. The GitHub Action `.github/workflows/refresh-item-classifications.yml`
runs it on the 1st of every month (and on demand via "Run workflow") and opens
a pull request on the `item-refresh` branch for review.

An item is "known" when its id appears in any of:

1. `src/main/resources/com/skillbank/item-meta.json`
2. any tab list in `src/main/java/com/skillbank/SkillBankData.java`
3. the ledger `data/auto-classified.json` (see below)

Everything else that is a real item (not noted / placeholder / duplicate) is
new. The run aborts without writing when more than `--max-new` (300) items
turn up, which almost certainly means the wiki query broke rather than Jagex
releasing 300 items.

### Run it locally

The classification runs on your Claude subscription (Max plan) through the
Claude Code CLI: the script shells out to `claude -p` with the same prompt the
API path uses and a JSON schema for the answer. You need the CLI installed
(`curl -fsSL https://claude.ai/install.sh | bash`) and signed in (`claude`,
then log in with the subscription account). No API key is involved;
`ANTHROPIC_API_KEY` is stripped from the CLI's environment so it can never
bill the API by accident.

```
# list what would be proposed (no CLI or key needed for the listing)
python3 tools/skillbank-data/refresh_new_items.py --dry-run --refresh-wiki

# classify the first five and write the three files
python3 tools/skillbank-data/refresh_new_items.py --limit 5 --summary-file out/summary.md
```

Flags: `--dry-run` (classify, print the summary, write nothing), `--limit N`
(first N new items only; `0` just lists), `--max-new N`, `--model <id>`,
`--backend claude-cli|api`, `--summary-file <path>`, `--refresh-wiki` /
`--no-cache` as in `scraper.py` (CI uses `--no-cache` so the wiki is always
fetched fresh; only the osrsbox dump is cached there). `--backend api` is the
fallback that posts to the Messages API with `ANTHROPIC_API_KEY` from the
environment or `tools/skillbank-data/.env`, like `scraper.py --classify`.
Both backends share the response cache in `cache/llm-classifications/`.

What gets written:

- `item-meta.json`: one new compact entry per placed item, appended at the end
  so the diff is additions only.
- `SkillBankData.java`: the ids go under a trailing
  `// === Auto-classified (monthly refresh) ===` group at the end of each tab's
  `Arrays.asList(...)`. The group is created on first use and extended after
  that; order inside the list does not matter at runtime (sections come from
  item-meta.json).
- `data/auto-classified.json`: the ledger, see next section.

`mapping.py` is never edited by the script, but its `force_include` /
`force_exclude` name lists are applied to the LLM output.

### The ledger

`data/auto-classified.json` records every item the monthly refresh classified:
`{id, name, release_date, tabs, rationale, model, run}`. It serves two
purposes:

- Items with `tabs: []` (the LLM put them nowhere, or you rejected them in the
  PR) are added to no file but stay in the ledger, so they are not proposed
  again next month.
- `llm_promote.build_synthetic_tabs` reads the ledger as id-based
  force-includes, so a future full regen (`scraper.py --classify
  --llm-promote`) keeps the placements that were reviewed in the monthly PRs.

Items that failed to classify (CLI or API error) are listed in the summary
but not written to the ledger, so they are retried next month.

### Triage the monthly PR

The PR body is the run summary: per-tab tables (id, name, release date,
section, LLM rationale), the "no tab assigned" list, failures, and token usage
(with the API-equivalent cost for reference; a subscription run is not billed
it).
Expect the first run to surface a backlog of older never-tagged items as well.
Edit the branch directly, then merge:

- **Move an item**: move its id between the tab lists in `SkillBankData.java`,
  change the `s` map in its `item-meta.json` entry, and update `tabs` in the
  ledger.
- **Reject an item**: delete its id from `SkillBankData.java` and its entry
  from `item-meta.json`, and set the ledger `tabs` to `[]` so it stays
  suppressed.
- **Make the decision permanent by name** (covers future variants too): add
  the name to the relevant `force_include` / `force_exclude` list in
  `mapping.py` as well.

Keep the `./gradlew build` green after hand edits; the Action already built
the plugin once against the generated patch. Plugin-hub submission is still a
manual step after merging.

Repository prerequisites for the Action:

- The `CLAUDE_CODE_OAUTH_TOKEN` repository secret. Generate it on your own
  machine with `claude setup-token` while signed in to the subscription; it is
  a one-year token tied to that subscription and can only make model
  requests. The Action installs the Claude Code CLI on the runner and the CLI
  reads this variable instead of a login. Renew the secret when it expires
  (the run fails at the "Check Claude Code CLI auth" step).
- Settings > Actions > General > "Allow GitHub Actions to create and approve
  pull requests".
- GitHub pauses scheduled workflows after 60 days without repository
  activity; re-enable it from the Actions tab.
