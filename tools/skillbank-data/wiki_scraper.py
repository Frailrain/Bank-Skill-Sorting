"""Brief #70: build a clean equipment dataset from the OSRS Wiki Bucket API.

The wiki Bucket API is the live, structured replacement for the abandoned
osrsbox-db. This module uses the existing rate-limited / cached client in
wiki.py to:

  1. Fetch every entry in the `infobox_bonuses` bucket — that bucket exists
     once per equipable item version, so its presence is the definitive
     "this item is equipable" signal.
  2. Fetch matching entries from `infobox_item` (the canonical-items query
     in wiki.py covers `default_version=true` + dose/charge variants).
  3. Join by `page_name_sub` (item_name#version_anchor), since
     `infobox_bonuses` has no `item_id` field on its own.
  4. Emit a single clean JSON output at `data/wiki-equipment-data.json`
     that downstream code (integrate_wiki_data.py) reads.

Critical caveat about requirements
----------------------------------
The brief asks for skill-level requirements (Attack 70 for the abyssal
whip, etc.), but neither `infobox_bonuses` nor `infobox_item` carries
that field. Confirmed by reading the bucket schema dumps at
`/wiki/Bucket:Infobox_bonuses?action=raw` and
`/wiki/Bucket:Infobox_item?action=raw` on 2026-05-26. Skill requirements
on the OSRS wiki live as prose text in each page's intro paragraph (e.g.
"requires an [[Attack]] level of 70 to wield" on the abyssal whip page).

This scraper therefore emits `"requirements": null` for every item, with
an explicit note in the JSON header that wiki bucket cannot supply this
field. The existing osrsbox-derived requirements in item-meta.json's
`rq` field (Brief #69) remain our requirements source until we either
parse page wikitext for them or find another structured source.

Schema
------
Top-level: {"generated_at": ISO, "source": ..., "notes": [...], "items": [...]}
Per item:
  {
    "name":            str,
    "version_anchor":  str | null,  // e.g. "Default", "(4)", "Charged"
    "item_id":         int | null,
    "page_name":       str | null,
    "slot":            str | null,
    "members":         bool,
    "requirements":    null,        // see caveat above
    "attack_bonuses":  {stab, slash, crush, magic, ranged},
    "defence_bonuses": {stab, slash, crush, magic, ranged},
    "other_bonuses":   {melee_strength, ranged_strength, magic_damage, prayer},
    "weapon_info":     {attack_speed, attack_range, combat_style} | null
  }
"""
from __future__ import annotations

import json
import sys
import time
from pathlib import Path

import wiki  # type: ignore

SCRIPT_DIR = Path(__file__).resolve().parent
CACHE_DIR = SCRIPT_DIR / "cache" / "wiki"
OUT_PATH = SCRIPT_DIR / "data" / "wiki-equipment-data.json"


def _first(v):
    if isinstance(v, list):
        return v[0] if v else None
    return v


def _int_or_zero(v):
    v = _first(v)
    if v in (None, ""):
        return 0
    try:
        return int(v)
    except (TypeError, ValueError):
        return 0


def _bool(v):
    v = _first(v)
    if isinstance(v, bool):
        return v
    if isinstance(v, str):
        return v.strip().lower() in ("yes", "true", "1")
    return bool(v) if v is not None else False


def fetch(refresh: bool = False) -> dict:
    print("Fetching wiki infobox_item (canonical + dose/charge variants)...",
          file=sys.stderr)
    items = wiki.fetch_canonical_items(CACHE_DIR, use_cache=True, refresh=refresh)
    print(f"  {len(items):,} infobox_item rows", file=sys.stderr)

    print("Fetching wiki infobox_bonuses (every equipable item)...",
          file=sys.stderr)
    bonuses = wiki.fetch_all_bonuses(CACHE_DIR, use_cache=True, refresh=refresh)
    print(f"  {len(bonuses):,} infobox_bonuses rows", file=sys.stderr)

    # Index items by page_name_sub — bonuses joins on that exact string.
    # An item with no version_anchor uses just page_name; with a version
    # anchor it's "PageName#Anchor". Wiki returns either form, so we index
    # both.
    by_pns: dict[str, dict] = {}
    for it in items:
        pns = _first(it.get("page_name_sub"))
        if pns:
            by_pns[pns] = it

    out_items: list[dict] = []
    no_match = 0
    no_id = 0
    for b in bonuses:
        pns = _first(b.get("page_name_sub")) or ""
        item = by_pns.get(pns)
        # Try fallback: bonuses may carry an unversioned page name when the
        # item page has only one version. The canonical-items table stores
        # those as page_name_sub == page_name.
        if item is None and "#" not in pns and pns in by_pns:
            item = by_pns[pns]

        if item is None:
            no_match += 1
            name = pns.split("#", 1)[0] if pns else None
            version = pns.split("#", 1)[1] if "#" in pns else None
            iid = None
            page_name = None
            members = False
        else:
            name = _first(item.get("item_name")) or pns.split("#", 1)[0]
            version = _first(item.get("version_anchor"))
            iid = _first(item.get("item_id"))
            try:
                iid = int(iid) if iid not in (None, "") else None
            except (TypeError, ValueError):
                iid = None
            page_name = _first(item.get("page_name"))
            members = _bool(item.get("is_members_only"))
        if iid is None:
            no_id += 1

        slot = _first(b.get("equipment_slot")) or None

        attack_bonuses = {
            "stab":   _int_or_zero(b.get("stab_attack_bonus")),
            "slash":  _int_or_zero(b.get("slash_attack_bonus")),
            "crush":  _int_or_zero(b.get("crush_attack_bonus")),
            "magic":  _int_or_zero(b.get("magic_attack_bonus")),
            "ranged": _int_or_zero(b.get("range_attack_bonus")),
        }
        defence_bonuses = {
            "stab":   _int_or_zero(b.get("stab_defence_bonus")),
            "slash":  _int_or_zero(b.get("slash_defence_bonus")),
            "crush":  _int_or_zero(b.get("crush_defence_bonus")),
            "magic":  _int_or_zero(b.get("magic_defence_bonus")),
            "ranged": _int_or_zero(b.get("range_defence_bonus")),
        }
        other_bonuses = {
            "melee_strength":  _int_or_zero(b.get("strength_bonus")),
            "ranged_strength": _int_or_zero(b.get("ranged_strength_bonus")),
            "magic_damage":    _int_or_zero(b.get("magic_damage_bonus")),
            "prayer":          _int_or_zero(b.get("prayer_bonus")),
        }

        speed = _first(b.get("weapon_attack_speed"))
        rng = _first(b.get("weapon_attack_range"))
        style = _first(b.get("combat_style"))
        weapon_info: dict | None = None
        if speed not in (None, "") or rng not in (None, "") or style:
            weapon_info = {
                "attack_speed": _int_or_zero(b.get("weapon_attack_speed")),
                "attack_range": str(rng) if rng not in (None, "") else None,
                "combat_style": str(style) if style else None,
            }

        out_items.append({
            "name":            name,
            "version_anchor":  version if version else None,
            "item_id":         iid,
            "page_name":       page_name,
            "slot":            slot,
            "members":         members,
            "requirements":    None,  # Wiki bucket doesn't expose these; see file header.
            "attack_bonuses":  attack_bonuses,
            "defence_bonuses": defence_bonuses,
            "other_bonuses":   other_bonuses,
            "weapon_info":     weapon_info,
        })

    # Sort by id then name so the diff against future runs is stable.
    out_items.sort(key=lambda x: ((x["item_id"] is None, x["item_id"] or 0),
                                   (x["name"] or "").lower()))

    out = {
        "generated_at": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        "source": "OSRS Wiki Bucket API (infobox_bonuses ⨝ infobox_item)",
        "notes": [
            "Skill-level requirements (Attack/Defence/Ranged/Magic/etc.) are "
            "NOT in the wiki bucket schema — they live as prose in each "
            "page's intro paragraph. requirements is set to null for every "
            "entry. Use osrsbox-derived rq in item-meta.json until a "
            "structured wiki source surfaces.",
            "page_name_sub join can fail for bonuses entries whose item "
            "page is missing from the canonical-items query (rare; e.g. "
            "obscure removed items). Those entries have item_id=null.",
        ],
        "stats": {
            "items_count":    len(out_items),
            "bonuses_rows":   len(bonuses),
            "infobox_items":  len(items),
            "no_match":       no_match,
            "no_item_id":     no_id,
        },
        "items": out_items,
    }

    OUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    OUT_PATH.write_text(json.dumps(out, separators=(",", ":")))

    print(file=sys.stderr)
    print(f"Wrote {OUT_PATH}", file=sys.stderr)
    print(f"  items_count:   {len(out_items):,}", file=sys.stderr)
    print(f"  no item_id:    {no_id:,}  (bonuses rows whose "
          f"item page wasn't in the canonical fetch)", file=sys.stderr)
    print(f"  unmatched pns: {no_match:,}", file=sys.stderr)
    return out


if __name__ == "__main__":
    fetch(refresh="--refresh" in sys.argv)
