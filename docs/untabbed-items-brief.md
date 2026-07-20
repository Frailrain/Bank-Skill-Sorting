# Brief #92 — Zero-tab items: audit + coverage plan

## The numbers (measured 2026-07-09, post-Brief-#90 regen)

| Metric | Count |
|---|---|
| Canonical items (non-noted / non-placeholder / non-duplicate) | 13,667 |
| In at least one tab | 10,603 |
| **In no tab at all** | **3,064** |
| … of which GE-tradeable | 274 |
| … of which never went through the LLM classifier | 3,056 |

Policy (per Matt): every item belongs somewhere, even if it's just `misc`.

## Why they're missing

1. **Item universe moved after the LLM run.** The Brief #51 full
   classification ran against an older `items-complete.json`; the
   Sailing-era wiki refresh added/re-canonicalised thousands of IDs
   (`Shrimps`, `Cod`, `Tuna`, `Bass` literally have no classification).
   `llm_promote` only places items present in `out/llm-classifications.json`,
   so post-run items silently get nothing.
2. **force_exclude flattening footgun** *(fixed in Brief #90)*: `llm_promote`
   flattens section-scoped `force_exclude` lists tab-wide. A cooking section
   exclude of the standard cooked-fish list was banishing Lobster/Swordfish/
   Shark etc. from the whole cooking tab. Removed; fish restored. **Action
   remaining**: audit the other ~30 `force_exclude` lists in `mapping.py`
   for the same pattern (any exclude meant to be section-local, not
   tab-global).
3. **Genuine long tail**: quest-only clutter (lit candles, scorpion cages,
   Glarial's urn), holiday leftovers, minigame internals. Still wanted in
   `quests`/`misc` per policy.

## Plan

### Phase 1 — classify the gap (cheap, no Cowork needed)

The pipeline already supports exactly this:

```
cd tools/skillbank-data
python3 scraper.py --no-fetch --classify --new-only
```

- `--new-only` filters the classify pool to items not currently in
  `SkillBankData.java` → the 3,064 exactly.
- Cost estimate: full run was 11,836 items / $42 → this is ≈ $11–12, 1–2 h.
- **Before running**: snapshot `out/llm-classifications.json` (it's the
  canonical full-run artifact). The per-item response cache in
  `cache/llm-classifications` means a follow-up full `--llm-render` can
  merge old + new without re-querying — verify the new-only run *merges
  into* rather than *overwrites* the JSON before promoting (if it
  overwrites, combine the snapshot + new output with a 10-line script,
  then `--llm-promote`).
- Prompt already has the "route to misc/quests when nothing fits" fallback,
  which matches the policy.

### Phase 2 — Cowork review (where Matt's time goes)

- Generate a review sheet from the new classifications:
  `id, name, tabs, rationale, GE-tradeable, release_date` — sorted with the
  274 tradeables first (those are items real players actually bank), long
  tail after.
- Existing audit tooling (`make_audit_input.py`, `out/full-audit.csv`
  format from Briefs #48–50) is the template.
- Matt reviews in Cowork; corrections land as `force_include` /
  `force_exclude` in `mapping.py` (respecting the flattening rule above),
  then regen.

### Phase 3 — regression guard

- Make `--llm-promote` print (and write into `out/report.txt`) the count of
  canonical items with zero tabs, split tradeable / untradeable, and list
  any *newly* orphaned names vs the previous run.
- Rule of thumb going forward: tradeable orphans should be ~0; a jump in
  the counter means the item universe refreshed and Phase 1 needs a re-run
  (`--classify --new-only` is incremental and cheap by design).

### Sequencing with the slayer rework (Brief #91)

Independent — can run before/parallel. Phase 1 here is worth doing first so
the slayer "best owned gear" ranking sees a complete item universe.
