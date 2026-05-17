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
