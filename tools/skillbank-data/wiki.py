"""OSRS Wiki Bucket API client with paginated query and on-disk caching.

Bucket API specifics (verified empirically):
- Endpoint: https://oldschool.runescape.wiki/api.php
- Action: `bucket`
- Query expression syntax: bucket('table').select('a','b').where('x','y').limit(N).offset(M).run()
- `select('*')` is REJECTED — fields must be enumerated.
- No join syntax works (`.join()`, `.join_on()`, multi-bucket-select all error). Join in Python.
- `infobox_bonuses` has NO `item_id` field; join with `infobox_item` via `page_name_sub`.
- Field values come back as JSON arrays (one item can have multiple values per attribute).
- 5000 rows is an effective per-page maximum.
"""
from __future__ import annotations

import hashlib
import json
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

WIKI_API = "https://oldschool.runescape.wiki/api.php"
USER_AGENT = "SkillBankTabs/1.0 (https://github.com/Frailrain/Bank-Skill-Sorting)"
DEFAULT_TTL_SECONDS = 24 * 60 * 60
PAGE_SIZE = 5000
RATE_LIMIT_SECONDS = 1.0

_last_call = 0.0


def _rate_limit():
    global _last_call
    elapsed = time.monotonic() - _last_call
    if elapsed < RATE_LIMIT_SECONDS:
        time.sleep(RATE_LIMIT_SECONDS - elapsed)
    _last_call = time.monotonic()


def _cache_path(cache_dir: Path, query: str) -> Path:
    h = hashlib.sha1(query.encode("utf-8")).hexdigest()[:16]
    return cache_dir / f"q-{h}.json"


def _cache_fresh(path: Path, ttl: int) -> bool:
    if not path.exists():
        return False
    return (time.time() - path.stat().st_mtime) < ttl


def bucket_query(
    query: str,
    cache_dir: Path,
    *,
    ttl: int = DEFAULT_TTL_SECONDS,
    use_cache: bool = True,
    refresh: bool = False,
) -> list[dict]:
    cache_dir.mkdir(parents=True, exist_ok=True)
    path = _cache_path(cache_dir, query)
    if use_cache and not refresh and _cache_fresh(path, ttl):
        return json.loads(path.read_text())

    _rate_limit()
    params = urllib.parse.urlencode({"action": "bucket", "format": "json", "query": query})
    url = f"{WIKI_API}?{params}"
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})

    backoff = 5
    for attempt in range(4):
        try:
            with urllib.request.urlopen(req, timeout=60) as resp:
                data = json.loads(resp.read())
            break
        except urllib.error.HTTPError as e:
            if e.code == 429:
                retry = int(e.headers.get("Retry-After") or backoff)
                print(f"  HTTP 429, sleeping {retry}s...", file=sys.stderr)
                time.sleep(retry)
                backoff *= 2
                continue
            raise
    else:
        raise RuntimeError("repeated 429 responses; aborting")

    if "error" in data:
        raise RuntimeError(f"Bucket error: {data['error']!r}  query={query}")
    rows = data.get("bucket", [])
    path.write_text(json.dumps(rows, indent=2))
    return rows


def paginated_query(
    bucket_name: str,
    fields: list[str],
    cache_dir: Path,
    *,
    where: dict | None = None,
    raw_where: str | None = None,
    ttl: int = DEFAULT_TTL_SECONDS,
    use_cache: bool = True,
    refresh: bool = False,
) -> list[dict]:
    """`where` is a {field: value} dict (each becomes .where(field, value) — AND).
    `raw_where` is a literal Lua expression appended via .where(<expr>); use
    this when you need bucket.Or, bucket.And, bucket.Not constructs that
    can't be expressed as plain key-value pairs."""
    select = ",".join(f"'{f}'" for f in fields)
    where_clauses = ""
    for k, v in (where or {}).items():
        if isinstance(v, bool):
            where_clauses += f".where('{k}',{'true' if v else 'false'})"
        elif isinstance(v, (int, float)):
            where_clauses += f".where('{k}',{v})"
        else:
            where_clauses += f".where('{k}','{v}')"
    if raw_where:
        where_clauses += f".where({raw_where})"

    all_rows: list[dict] = []
    offset = 0
    while True:
        query = (
            f"bucket('{bucket_name}').select({select})"
            f"{where_clauses}.offset({offset}).limit({PAGE_SIZE}).run()"
        )
        rows = bucket_query(query, cache_dir, ttl=ttl, use_cache=use_cache, refresh=refresh)
        all_rows.extend(rows)
        print(
            f"    {bucket_name} offset={offset}: {len(rows)} rows (running total: {len(all_rows)})",
            file=sys.stderr,
        )
        if len(rows) < PAGE_SIZE:
            break
        offset += PAGE_SIZE
    return all_rows


# ── High-level fetchers ────────────────────────────────────────────────────

ITEM_FIELDS = [
    "item_id", "item_name", "version_anchor", "default_version",
    "is_members_only", "weight", "examine", "tradeable", "release_date",
    "page_name", "page_name_sub",
]

BONUSES_FIELDS = [
    "page_name_sub", "equipment_slot",
    "stab_attack_bonus", "slash_attack_bonus", "crush_attack_bonus",
    "magic_attack_bonus", "range_attack_bonus",
    "stab_defence_bonus", "slash_defence_bonus", "crush_defence_bonus",
    "magic_defence_bonus", "range_defence_bonus",
    "strength_bonus", "ranged_strength_bonus",
    "magic_damage_bonus", "prayer_bonus",
    "weapon_attack_speed", "combat_style",
]


def fetch_canonical_items(cache_dir: Path, **kwargs) -> list[dict]:
    """Fetch canonical items + dose variants (3 dose / 2 dose / 1 dose).

    Wiki's `default_version=true` would pin each potion to its (4)-dose row;
    we need the lower doses too so the herblore tab classifier can pick them
    up. Bucket's `where()` is AND-only; multi-value OR uses `bucket.Or()`
    with `{field, value}` table conditions (lowercase `bucket`, not the
    `Bucket` shown in the official Lua docs — that global only exists in
    wiki modules, not via api.php).
    """
    # Pre-built OR clause: default_version OR known non-canonical variants.
    # Non-canonical variants we deliberately want included:
    #   - dose variants (potions: '3 dose' / '2 dose' / '1 dose')
    #   - integer charge variants (Amulet of glory(1..6), Skills necklace etc.)
    #   - parenthesised charge variants (Broodoo shield (5)/(6)/(7)/(8) format)
    #   - 'Uncharged' (Amulet of glory uncharged, Bracelet of ethereum, etc.)
    or_clauses = [
        "{'default_version',true}",
        # potion doses
        "{'version_anchor','3 dose'}", "{'version_anchor','2 dose'}", "{'version_anchor','1 dose'}",
        # integer charge variants
        "{'version_anchor','1'}", "{'version_anchor','2'}", "{'version_anchor','3'}",
        "{'version_anchor','5'}", "{'version_anchor','6'}", "{'version_anchor','7'}",
        "{'version_anchor','8'}",
        # parenthesised charges
        "{'version_anchor','(1)'}", "{'version_anchor','(2)'}", "{'version_anchor','(3)'}",
        "{'version_anchor','(5)'}", "{'version_anchor','(6)'}", "{'version_anchor','(7)'}",
        "{'version_anchor','(8)'}",
        # charge state
        "{'version_anchor','Uncharged'}",
    ]
    or_clause = "bucket.Or(" + ",".join(or_clauses) + ")"
    return paginated_query(
        "infobox_item", ITEM_FIELDS, cache_dir,
        raw_where=or_clause, **kwargs,
    )


def fetch_all_bonuses(cache_dir: Path, **kwargs) -> list[dict]:
    return paginated_query("infobox_bonuses", BONUSES_FIELDS, cache_dir, **kwargs)


# ── Schema probe ───────────────────────────────────────────────────────────

def probe_schema(cache_dir: Path, candidate_fields: list[str], bucket: str) -> dict:
    """Test each candidate field; return {field: 'OK' | 'ERR: <msg>'}."""
    result = {}
    for f in candidate_fields:
        query = f"bucket('{bucket}').select('{f}').limit(1).run()"
        _rate_limit()
        params = urllib.parse.urlencode({"action": "bucket", "format": "json", "query": query})
        url = f"{WIKI_API}?{params}"
        req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
        try:
            with urllib.request.urlopen(req, timeout=30) as resp:
                data = json.loads(resp.read())
            if "bucket" in data:
                result[f] = "OK"
            else:
                result[f] = f"ERR: {data.get('error', 'unknown')}"
        except Exception as e:
            result[f] = f"ERR: {e}"
    return result
