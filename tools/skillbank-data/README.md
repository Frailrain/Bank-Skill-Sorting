# skillbank-data scraper

One-shot Python tool that proposes an expanded `SkillBankData.java` driven by item metadata from `osrsbox-db`. Never touches live plugin source — produces `out/SkillBankData.java.proposed` plus a diff report for human review.

## How it works

1. Downloads `items-complete.json` from the maintained `osrsreboxed-db` fork (~1 MB). Caches it under `cache/`.
2. Loads tab classification rules from `mapping.py`. Each tab declares:
   - A classifier predicate matching osrsbox item records.
   - A subcategory mapping for ordering.
   - A variant allowlist for non-canonical IDs we want surfaced.
3. Classifies every item, groups by tab/subcategory, sorts by tier, breaks ties by item ID.
4. Parses the existing `src/main/java/com/skillbank/SkillBankData.java` to extract current per-tag IDs.
5. Emits `out/SkillBankData.java.proposed` plus `out/diff.txt` and `out/report.txt`.

## Run it

```
cd tools/skillbank-data
python3 scraper.py
```

No pip dependencies. Standard library only.

Optional flags:
- `--no-fetch` — skip osrsbox download, reuse cache.
- `--tab <name>` — restrict output to a single tab (handy for iterating on one classifier).
- `--out <dir>` — change output dir (default `./out`).

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
