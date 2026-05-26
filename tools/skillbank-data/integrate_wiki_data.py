"""Brief #70 part 2 — cross-reference wiki equipment data against
src/main/resources/com/skillbank/item-meta.json, write a review report
to out/wiki-crossref-report.md, and emit out/item-meta-updated.json
with wiki-sourced fields applied where they differ.

This is NOT run on every build. It's a manual review step:
  1. python3 wiki_scraper.py    # fetch fresh wiki data
  2. python3 integrate_wiki_data.py    # build the diff + draft
  3. eyeball the report; if happy, replace item-meta.json with the draft

Only slot is currently updated from wiki (the bonuses/weapon-info fields
in item-meta.json are minimal — slot is the only field with a wiki
counterpart that integrate_wiki_data touches). Other fields stay as-is.
The diff report still highlights bonus-level discrepancies for awareness.

Requirements: NOT integrated (wiki bucket doesn't carry skill reqs — see
wiki_scraper.py file header). The existing osrsbox-derived rq field in
item-meta.json is left untouched.
"""
from __future__ import annotations

import json
import sys
from collections import Counter
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
REPO_ROOT = SCRIPT_DIR.parent.parent
WIKI_DATA = SCRIPT_DIR / "data" / "wiki-equipment-data.json"
META_PATH = REPO_ROOT / "src/main/resources/com/skillbank/item-meta.json"
REPORT_PATH = SCRIPT_DIR / "out" / "wiki-crossref-report.md"
DRAFT_META_PATH = SCRIPT_DIR / "out" / "item-meta-updated.json"


def integrate():
    if not WIKI_DATA.exists():
        print(f"ERROR: {WIKI_DATA} not found. Run wiki_scraper.py first.",
              file=sys.stderr)
        sys.exit(2)
    if not META_PATH.exists():
        print(f"ERROR: {META_PATH} not found.", file=sys.stderr)
        sys.exit(2)

    wiki = json.loads(WIKI_DATA.read_text())
    meta = json.loads(META_PATH.read_text())

    # wiki items keyed by item_id
    wiki_by_id: dict[int, dict] = {}
    wiki_no_id_count = 0
    for it in wiki["items"]:
        iid = it.get("item_id")
        if iid is None:
            wiki_no_id_count += 1
            continue
        # Multiple bonuses entries can share an ID (e.g. tradeable + bound,
        # though rare). Keep the first; surface a count.
        wiki_by_id.setdefault(int(iid), it)

    # meta items keyed by id (strings in JSON)
    meta_by_id: dict[int, dict] = {int(k): v for k, v in meta.items()}

    wiki_ids = set(wiki_by_id)
    meta_ids = set(meta_by_id)

    # Cross-reference buckets
    in_wiki_only = wiki_ids - meta_ids       # equipable per wiki, not in our meta
    in_meta_only_equip = []                  # in our meta with slot, not in wiki
    in_meta_only_unequip = []                # in our meta without slot, not in wiki
    for iid in (meta_ids - wiki_ids):
        if (meta_by_id[iid].get("sl")):
            in_meta_only_equip.append(iid)
        else:
            in_meta_only_unequip.append(iid)

    # Slot conflicts
    slot_conflicts: list[tuple[int, str, str | None, str | None]] = []
    members_conflicts: list[tuple[int, str, bool, bool]] = []
    for iid in (wiki_ids & meta_ids):
        w = wiki_by_id[iid]
        m = meta_by_id[iid]
        ws = (w.get("slot") or "").lower() or None
        ms = (m.get("sl") or "").lower() or None
        if ws != ms:
            slot_conflicts.append((iid, w.get("name") or "", ms, ws))

    # Build draft meta — copy and apply wiki slot where it differs
    draft = json.loads(META_PATH.read_text())  # fresh copy
    updates = 0
    for iid in (wiki_ids & meta_ids):
        w = wiki_by_id[iid]
        sk = str(iid)
        m = draft.get(sk)
        if m is None:
            continue
        ws = w.get("slot") or None
        ms = m.get("sl") or None
        if ws and ws != ms:
            m["sl"] = ws
            updates += 1

    DRAFT_META_PATH.parent.mkdir(parents=True, exist_ok=True)
    DRAFT_META_PATH.write_text(json.dumps(draft, separators=(",", ":")))

    # Write report
    REPORT_PATH.parent.mkdir(parents=True, exist_ok=True)
    lines: list[str] = []
    lines.append("# Wiki ⇄ item-meta cross-reference")
    lines.append("")
    lines.append(f"- Generated: {wiki.get('generated_at')}")
    lines.append(f"- Wiki items: **{len(wiki['items']):,}** "
                 f"({len(wiki_by_id):,} with item_id, {wiki_no_id_count:,} "
                 f"without — bonuses rows whose item page wasn't in the "
                 f"canonical-items fetch)")
    lines.append(f"- item-meta entries: **{len(meta_by_id):,}**")
    lines.append("")
    lines.append("## Caveat: requirements not in wiki bucket")
    lines.append("")
    lines.append("Wiki's `infobox_bonuses` bucket does NOT carry skill-level "
                 "requirements (Attack 70, Defence 60, etc.) — those live as "
                 "prose in each page's intro paragraph. This integration step "
                 "leaves the existing osrsbox-derived `rq` field on every item "
                 "untouched. To replace osrsbox requirements with wiki data, a "
                 "future brief will need to parse page wikitext.")
    lines.append("")
    lines.append("## Summary")
    lines.append("")
    lines.append(f"| Category | Count |")
    lines.append(f"|---|---|")
    lines.append(f"| Items in wiki + meta (matched) | {len(wiki_ids & meta_ids):,} |")
    lines.append(f"| Items in wiki only (potential additions) | {len(in_wiki_only):,} |")
    lines.append(f"| Items in meta only with slot (possible non-equipable) | {len(in_meta_only_equip):,} |")
    lines.append(f"| Items in meta only without slot (expected — non-equipables) | {len(in_meta_only_unequip):,} |")
    lines.append(f"| Slot conflicts (wiki ≠ meta) | {len(slot_conflicts):,} |")
    lines.append(f"| Slot updates applied to draft | {updates:,} |")
    lines.append("")
    if in_wiki_only:
        lines.append("## Items in wiki but missing from item-meta (sample of 30)")
        lines.append("")
        sample = sorted(in_wiki_only)[:30]
        for iid in sample:
            w = wiki_by_id[iid]
            lines.append(f"- `{iid:>6}` {w.get('name', '?')} "
                         f"(slot={w.get('slot')})")
        lines.append("")
    if in_meta_only_equip:
        lines.append("## Items in meta with slot but missing from wiki (sample of 30)")
        lines.append("")
        sample = sorted(in_meta_only_equip)[:30]
        for iid in sample:
            m = meta_by_id[iid]
            lines.append(f"- `{iid:>6}` (slot in meta: {m.get('sl')})")
        lines.append("")
    if slot_conflicts:
        lines.append("## Slot conflicts (sample of 50)")
        lines.append("")
        lines.append("| id | name | meta.slot | wiki.slot |")
        lines.append("|---|---|---|---|")
        for iid, name, ms, ws in slot_conflicts[:50]:
            lines.append(f"| {iid} | {name} | {ms} | {ws} |")
        lines.append("")

    lines.append("## Output")
    lines.append("")
    lines.append(f"Draft updated metadata written to `{DRAFT_META_PATH}`. "
                 "Review the slot conflicts above before replacing the live "
                 "file. The draft only changes the `sl` (slot) field; all "
                 "other fields including `rq` (requirements) are unchanged.")

    REPORT_PATH.write_text("\n".join(lines))
    print(f"\nWrote {REPORT_PATH}", file=sys.stderr)
    print(f"Wrote {DRAFT_META_PATH}", file=sys.stderr)
    print(f"\nSummary:", file=sys.stderr)
    print(f"  matched:           {len(wiki_ids & meta_ids):,}", file=sys.stderr)
    print(f"  wiki only:         {len(in_wiki_only):,}", file=sys.stderr)
    print(f"  meta only (equip): {len(in_meta_only_equip):,}", file=sys.stderr)
    print(f"  meta only (no slot): {len(in_meta_only_unequip):,}", file=sys.stderr)
    print(f"  slot conflicts:    {len(slot_conflicts):,}", file=sys.stderr)
    print(f"  draft updates:     {updates:,}", file=sys.stderr)


if __name__ == "__main__":
    integrate()
