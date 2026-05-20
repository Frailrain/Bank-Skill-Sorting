#!/usr/bin/env python3
"""Generate a proposed SkillBankData.java from osrsbox-db item metadata.

See ./README.md for usage and design.
"""
from __future__ import annotations

import argparse
import json
import os
import re
import sys
import time
import textwrap
import urllib.parse
import urllib.request
from dataclasses import dataclass, field
from pathlib import Path
from typing import Callable

SCRIPT_DIR = Path(__file__).resolve().parent
REPO_ROOT = SCRIPT_DIR.parent.parent
CACHE_DIR = SCRIPT_DIR / "cache"
WIKI_CACHE_DIR = CACHE_DIR / "wiki"
WIKI_PAGE_CACHE_DIR = CACHE_DIR / "wiki-pages"
LLM_CACHE_DIR = CACHE_DIR / "llm-classifications"
OUT_DIR = SCRIPT_DIR / "out"
DATA_JAVA = REPO_ROOT / "src/main/java/com/skillbank/SkillBankData.java"
DIFF_REPORT_FILE = SCRIPT_DIR / "diff-current-vs-wiki.txt"


def _load_env() -> None:
    """Load KEY=VALUE pairs from tools/skillbank-data/.env into os.environ.

    Existing env vars take precedence. Silently does nothing if .env is missing
    (the scraper has many modes that don't need it). Lines starting with # are
    comments; blank lines ignored. Quoted values (single or double) are unquoted.
    """
    env_file = SCRIPT_DIR / ".env"
    if not env_file.exists():
        return
    for line in env_file.read_text().splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        k, v = line.split("=", 1)
        k = k.strip()
        v = v.strip()
        if len(v) >= 2 and v[0] == v[-1] and v[0] in ("'", '"'):
            v = v[1:-1]
        os.environ.setdefault(k, v)

OSRSBOX_URL = (
    "https://raw.githubusercontent.com/0xNeffarion/osrsreboxed-db/master/"
    "docs/items-complete.json"
)
USER_AGENT = "SkillBankTabs/1.0 (https://github.com/Frailrain/Bank-Skill-Sorting)"


# ── osrsbox client ──────────────────────────────────────────────────────────

def fetch_osrsbox(force: bool = False) -> dict:
    CACHE_DIR.mkdir(parents=True, exist_ok=True)
    cache = CACHE_DIR / "items-complete.json"
    if cache.exists() and not force:
        with cache.open() as f:
            return json.load(f)
    print(f"Fetching {OSRSBOX_URL} (~1 MB)...", file=sys.stderr)
    req = urllib.request.Request(OSRSBOX_URL, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(req, timeout=60) as resp:
        raw = resp.read()
    cache.write_bytes(raw)
    return json.loads(raw)


# ── Wiki/osrsbox merge ─────────────────────────────────────────────────────

# Map wiki infobox_bonuses field names → osrsbox equipment.* field names.
_WIKI_BONUS_TO_OSRSBOX = {
    "stab_attack_bonus":    "attack_stab",
    "slash_attack_bonus":   "attack_slash",
    "crush_attack_bonus":   "attack_crush",
    "magic_attack_bonus":   "attack_magic",
    "range_attack_bonus":   "attack_ranged",
    "stab_defence_bonus":   "defence_stab",
    "slash_defence_bonus":  "defence_slash",
    "crush_defence_bonus":  "defence_crush",
    "magic_defence_bonus":  "defence_magic",
    "range_defence_bonus":  "defence_ranged",
    "strength_bonus":       "melee_strength",
    "ranged_strength_bonus": "ranged_strength",
    "magic_damage_bonus":   "magic_damage",
    "prayer_bonus":         "prayer",
}


def _first(v):
    """Wiki rows return values as lists; unwrap the first element."""
    if isinstance(v, list):
        return v[0] if v else None
    return v


def _int_or_zero(v):
    try:
        return int(_first(v))
    except (TypeError, ValueError):
        return 0


def _truthy(v):
    """Wiki returns booleans inconsistently — '' / 'true' / True / etc."""
    v = _first(v)
    if v is True or v is False:
        return v
    if isinstance(v, str):
        return v.lower() in ("true", "yes", "1") or (v == "")  # tradeable=true often "" on items
    return bool(v)


def merge_wiki_osrsbox(
    wiki_items: list[dict],
    wiki_bonuses: list[dict],
    osrsbox: dict,
) -> tuple[dict, list[int], list[int], list[dict]]:
    """Produce {str(id): osrsbox-shaped item} merging wiki (primary) + osrsbox.

    Returns:
        merged      — dict keyed by str(item_id)
        wiki_only   — IDs present in wiki but absent from osrsbox cache
        osrsbox_only — IDs in osrsbox but absent from wiki (likely deprecated)
        discrepancies — name/slot mismatches between sources
    """
    bonuses_by_pns: dict[str, dict] = {}
    for b in wiki_bonuses:
        pns = _first(b.get("page_name_sub"))
        if pns:
            bonuses_by_pns.setdefault(pns, b)

    merged: dict[str, dict] = {}
    wiki_only: list[int] = []
    discrepancies: list[dict] = []
    seen_ids: set[int] = set()

    for it in wiki_items:
        raw_id = _first(it.get("item_id"))
        if raw_id in (None, ""):
            continue
        try:
            iid = int(raw_id)
        except (TypeError, ValueError):
            continue
        if iid in seen_ids:
            continue
        seen_ids.add(iid)

        name = _first(it.get("item_name")) or ""
        pns = _first(it.get("page_name_sub")) or ""
        page_name = _first(it.get("page_name")) or ""
        members = _truthy(it.get("is_members_only"))

        bonuses = bonuses_by_pns.get(pns, {})
        if not bonuses and "#" in pns:
            # Items canonical to "Foo#Inventory" often have bonuses at plain "Foo"
            # (or another anchor). Fall back to the un-suffixed page name.
            bonuses = bonuses_by_pns.get(pns.split("#", 1)[0], {})
        equipment: dict = {}
        weapon: dict = {}
        if bonuses:
            slot = _first(bonuses.get("equipment_slot"))
            if slot:
                equipment["slot"] = slot
            for wkey, okey in _WIKI_BONUS_TO_OSRSBOX.items():
                v = _first(bonuses.get(wkey))
                if v not in (None, ""):
                    try:
                        equipment[okey] = int(v)
                    except (TypeError, ValueError):
                        pass
            wa = _first(bonuses.get("weapon_attack_speed"))
            if wa not in (None, ""):
                try:
                    weapon["attack_speed"] = int(wa)
                except (TypeError, ValueError):
                    pass
            cs = _first(bonuses.get("combat_style"))
            if cs:
                # combat_style is a rough proxy; osrsbox weapon_type wins below if present.
                weapon["weapon_type"] = cs

        osb = osrsbox.get(str(iid)) or {}
        if osb:
            if osb.get("name") and osb["name"] != name:
                discrepancies.append({
                    "id": iid, "wiki_name": name, "osrsbox_name": osb["name"],
                })
            equipable = osb.get("equipable", bool(equipment))
            equipable_weapon = osb.get("equipable_weapon", False)
            osb_weapon = osb.get("weapon") or {}
            if osb_weapon.get("weapon_type"):
                weapon["weapon_type"] = osb_weapon["weapon_type"]
            # Pull skill-level requirements through for the sort engine (Brief #55).
            osb_eq = osb.get("equipment") or {}
            if osb_eq.get("requirements"):
                equipment["requirements"] = osb_eq["requirements"]
            quest_item = osb.get("quest_item", False)
            noted = osb.get("noted", False)
            placeholder = osb.get("placeholder", False)
            duplicate = osb.get("duplicate", False)
        else:
            wiki_only.append(iid)
            equipable = bool(equipment)
            equipable_weapon = equipable and equipment.get("slot") in ("weapon", "2h")
            quest_item = False
            noted = placeholder = duplicate = False

        merged[str(iid)] = {
            "id": iid,
            "name": name,
            "page_name": page_name,
            "page_name_sub": pns,
            "members": members,
            "tradeable": _truthy(it.get("tradeable")),
            "weight": _first(it.get("weight")),
            "release_date": _first(it.get("release_date")),
            "examine": _first(it.get("examine")),
            "equipable": equipable,
            "equipable_weapon": equipable_weapon,
            "noted": noted,
            "placeholder": placeholder,
            "duplicate": duplicate,
            "quest_item": quest_item,
            "equipment": equipment,
            "weapon": weapon,
            "_source": "wiki+osrsbox" if osb else "wiki-only",
        }

    osrsbox_only = [int(k) for k in osrsbox if k not in merged]
    return merged, wiki_only, osrsbox_only, discrepancies


# ── Wiki Bucket supplement (rate-limited, cached) ───────────────────────────

_bucket_cache: dict | None = None
_last_bucket_call = 0.0


def bucket_query(query: str) -> list[dict]:
    """Run a wiki Bucket query. Results cached to disk; 1 req/sec rate limit."""
    global _bucket_cache, _last_bucket_call
    cache_file = CACHE_DIR / "bucket-cache.json"
    if _bucket_cache is None:
        if cache_file.exists():
            _bucket_cache = json.loads(cache_file.read_text())
        else:
            _bucket_cache = {}
    if query in _bucket_cache:
        return _bucket_cache[query]
    elapsed = time.monotonic() - _last_bucket_call
    if elapsed < 1.0:
        time.sleep(1.0 - elapsed)
    params = urllib.parse.urlencode({"action": "bucket", "format": "json", "query": query})
    url = f"https://oldschool.runescape.wiki/api.php?{params}"
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(req, timeout=30) as resp:
        data = json.loads(resp.read())
    _last_bucket_call = time.monotonic()
    rows = data.get("bucket", [])
    _bucket_cache[query] = rows
    CACHE_DIR.mkdir(parents=True, exist_ok=True)
    cache_file.write_text(json.dumps(_bucket_cache, indent=2))
    return rows


# ── Tier inference ──────────────────────────────────────────────────────────

METAL_TIERS = {
    "bronze": 1, "iron": 2, "steel": 3, "black": 4, "white": 4.1,
    "mithril": 5, "adamant": 6, "adamantite": 6, "rune": 7, "runite": 7,
    "dragon": 8, "crystal": 9, "infernal": 11,
}
WOOD_TIERS = {
    "oak": 2, "willow": 3, "maple": 4, "yew": 5, "magic": 6, "redwood": 7,
}
BARROWS_NAMES = {"ahrim", "dharok", "guthan", "karil", "torag", "verac"}
BIS_TIERS = {
    "bandos": 12, "armadyl": 12, "ancestral": 12, "inquisitor": 12,
    "torva": 13, "virtus": 13, "sanguine": 13,
    "scythe": 14, "tumeken": 14, "tonalztics": 14, "elysian": 12,
}

_TOKEN_RE = re.compile(r"\b([A-Za-z']+)\b")


def infer_tier(name: str, *, prefer: str = "metal") -> float:
    """Return a numeric sort key for `name`. prefer='wood' biases logs/bows/axes."""
    tokens = [t.lower().rstrip("'s") for t in _TOKEN_RE.findall(name)]
    for t in tokens:
        if t in BIS_TIERS:
            return BIS_TIERS[t]
        if t in BARROWS_NAMES:
            return 10
    if prefer == "wood":
        for t in tokens:
            if t in WOOD_TIERS:
                return WOOD_TIERS[t]
        for t in tokens:
            if t == "regular":
                return 1
    for t in tokens:
        if t in METAL_TIERS:
            if t == "dragon" and any(o in tokens for o in ("bones", "bone")):
                continue
            return METAL_TIERS[t]
    if prefer == "wood":
        return 1.0  # no tier token = vanilla wood
    return 999.0


# ── Existing SkillBankData.java parsing ────────────────────────────────────

TAG_CONST_RE = re.compile(
    r'public\s+static\s+final\s+String\s+(TAG_\w+)\s*=\s*"([^"]+)"\s*;'
)
TAG_PUT_RE = re.compile(
    r'm\.put\((TAG_\w+),\s*Arrays\.asList\((.*?)\)\s*\);',
    re.DOTALL,
)
INT_LITERAL_RE = re.compile(r"\b(\d+)\b")
LINE_COMMENT_RE = re.compile(r"//[^\n]*")


def parse_current_data(java_src: str) -> tuple[dict[str, list[int]], dict[str, str]]:
    """Return (tag_name -> id list, const_name -> tag_name)."""
    consts = dict(TAG_CONST_RE.findall(java_src))
    out: dict[str, list[int]] = {}
    for m in TAG_PUT_RE.finditer(java_src):
        const_name, body = m.group(1), m.group(2)
        body = LINE_COMMENT_RE.sub("", body)
        ids = [int(x) for x in INT_LITERAL_RE.findall(body)]
        tag_name = consts.get(const_name)
        if tag_name:
            out[tag_name] = ids
    return out, consts


# ── Classification ──────────────────────────────────────────────────────────

@dataclass
class Section:
    label: str
    classifier: Callable[[dict], bool]
    tier_pref: str = "metal"
    # Names that bypass `classifier` (judgment-call adds).
    force_include: list[str] = field(default_factory=list)
    # Names that match `classifier` but should NOT land in this section.
    force_exclude: list[str] = field(default_factory=list)
    # Optional custom sort key — return any comparable value.
    sort_key: Callable[[dict], object] | None = None


@dataclass
class TabSpec:
    name: str
    const_name: str
    sections: list[Section] = field(default_factory=list)
    variant_allowlist: list[str] = field(default_factory=list)
    tier_overrides: dict[str, float] = field(default_factory=dict)
    # Items to inject regardless of osrsbox presence — (id, name, section_label).
    # Use for items not in the osrsbox dump (e.g. post-cutoff releases like Sailing).
    extra_items: list[tuple[int, str, str]] = field(default_factory=list)


def classify(items: dict, tabs: list[TabSpec]) -> dict[str, list[list[tuple[object, int, str]]]]:
    by_id: dict[int, dict] = {int(k): v for k, v in items.items()}
    canonical_for_name: dict[str, int] = {}
    for iid in sorted(by_id):
        it = by_id[iid]
        if it.get("noted") or it.get("placeholder") or it.get("duplicate"):
            continue
        canonical_for_name.setdefault(it["name"], iid)

    result: dict[str, list[list[tuple[object, int, str]]]] = {
        t.name: [[] for _ in t.sections] for t in tabs
    }

    for tab in tabs:
        allow = set(tab.variant_allowlist)
        for iid, it in by_id.items():
            if it.get("noted") or it.get("placeholder"):
                continue
            nm = it["name"]
            if canonical_for_name.get(nm) != iid and nm not in allow:
                continue
            for i, sec in enumerate(tab.sections):
                if nm in sec.force_exclude:
                    continue
                matched = nm in sec.force_include
                if not matched:
                    try:
                        matched = sec.classifier(it)
                    except Exception as e:
                        print(
                            f"WARN: classifier error on '{nm}' (id={iid}) "
                            f"in {tab.name}/{sec.label}: {e}",
                            file=sys.stderr,
                        )
                        matched = False
                if matched:
                    if sec.sort_key is not None:
                        try:
                            sk: object = sec.sort_key(it)
                        except Exception as e:
                            print(
                                f"WARN: sort_key error on '{nm}' (id={iid}) "
                                f"in {tab.name}/{sec.label}: {e}",
                                file=sys.stderr,
                            )
                            sk = 999.0
                    else:
                        sk = tab.tier_overrides.get(nm)
                        if sk is None:
                            sk = infer_tier(nm, prefer=sec.tier_pref)
                    result[tab.name][i].append((sk, iid, nm))
                    break

    # Inject extra_items (e.g. post-cutoff Sailing IDs not in osrsbox cache).
    for tab in tabs:
        for iid, name, sec_label in tab.extra_items:
            for i, sec in enumerate(tab.sections):
                if sec.label == sec_label:
                    result[tab.name][i].append((999.0, iid, name))
                    break
            else:
                print(
                    f"WARN: extra_items for tab '{tab.name}' references section "
                    f"'{sec_label}' which doesn't exist",
                    file=sys.stderr,
                )

    for tab in tabs:
        for sec_items in result[tab.name]:
            sec_items.sort(key=lambda r: (_sort_safe(r[0]), r[1]))
    return result


def _sort_safe(v):
    """Coerce mixed sort-key types so float comparisons never crash."""
    if isinstance(v, tuple):
        return v
    if isinstance(v, (int, float)):
        return (float(v),)
    return (str(v),)


# ── Java rendering ──────────────────────────────────────────────────────────

def render_block(
    tab: TabSpec,
    sections: list[list[tuple[object, int, str]]],
    legacy_ids: list[int] | None = None,
) -> str:
    """Render the inner content of one m.put block (no leading indent).

    `legacy_ids` (if provided and non-empty) become a trailing 'Legacy carry-over'
    section so we never silently drop items the hand-curated data had.
    """
    legacy_ids = legacy_ids or []
    total = sum(len(s) for s in sections) + len(legacy_ids)
    summary_parts = [
        f"{tab.sections[i].label} ({len(sections[i])})"
        for i in range(len(sections)) if sections[i]
    ]
    if legacy_ids:
        summary_parts.append(f"Legacy ({len(legacy_ids)})")
    lines: list[str] = []
    lines.append(f"// {tab.name.upper()} — {total} items")
    if summary_parts:
        wrapped = textwrap.wrap(", ".join(summary_parts), 70)
        for w in wrapped:
            lines.append(f"//   {w}")
    lines.append(f"m.put({tab.const_name}, Arrays.asList(")

    nonempty = [(i, sections[i]) for i in range(len(sections)) if sections[i]]
    has_any = bool(nonempty) or bool(legacy_ids)
    if not has_any:
        lines.append("\t// (no items matched — see classifiers in mapping.py)")

    # Total section count for trailing-comma logic.
    total_sections = len(nonempty) + (1 if legacy_ids else 0)

    for outer_idx, (i, items) in enumerate(nonempty):
        if outer_idx > 0:
            lines.append("")
        lines.append(f"\t// === {tab.sections[i].label} ===")
        is_last_section = outer_idx == total_sections - 1
        for inner_idx in range(0, len(items), 8):
            chunk = items[inner_idx:inner_idx + 8]
            ids = ", ".join(str(r[1]) for r in chunk)
            last_chunk = inner_idx + len(chunk) >= len(items)
            comma = "" if (is_last_section and last_chunk) else ","
            lines.append(f"\t{ids}{comma}")

    if legacy_ids:
        if nonempty:
            lines.append("")
        lines.append("\t// === Legacy carry-over (hand-curated items the new classifier missed) ===")
        for inner_idx in range(0, len(legacy_ids), 8):
            chunk = legacy_ids[inner_idx:inner_idx + 8]
            ids = ", ".join(str(i) for i in chunk)
            last_chunk = inner_idx + len(chunk) >= len(legacy_ids)
            comma = "" if last_chunk else ","
            lines.append(f"\t{ids}{comma}")

    lines.append("));")
    return "\n".join(lines)


def _camel(s: str) -> str:
    return "".join(part.capitalize() for part in s.split("_"))


def render_proposed(
    java_src: str,
    tabs: list[TabSpec],
    classified: dict,
    current: dict[str, list[int]] | None = None,
    additive: bool = True,
) -> str:
    """Replace each m.put(TAG_X, Arrays.asList(...)); with addX(m); and append
    per-tab helper methods before the closing class brace.

    Splitting per tab is necessary because the JVM <clinit> bytecode limit
    is 64 KB — with ~5700 IDs across 20 tabs the single static block
    overflows ('code too large' compile error). Each helper method has its
    own bytecode quota.
    """
    out = java_src
    current = current or {}
    helper_blocks: list[tuple[str, str]] = []

    # Idempotency: if a prior run already split tabs into addXxx() helpers,
    # strip them out before re-emitting. Otherwise re-running would replace
    # m.put inside a helper with a recursive call to that helper.
    existing_helper_re = re.compile(
        r"\n\tprivate static void add\w+\(Map<String, List<Integer>> m\)\s*\n\t\{.*?\n\t\}\n",
        re.DOTALL,
    )
    out = existing_helper_re.sub("", out)

    for tab in tabs:
        proposed_ids = {
            r[1] for sec_items in classified[tab.name] for r in sec_items
        }
        legacy_ids: list[int] = []
        if additive and tab.name in current:
            legacy_ids = sorted(set(current[tab.name]) - proposed_ids)
        block_body = render_block(tab, classified[tab.name], legacy_ids=legacy_ids)
        method_name = "add" + _camel(tab.name)
        helper_blocks.append((method_name, block_body))

        pattern = re.compile(
            rf'([ \t]*)m\.put\({re.escape(tab.const_name)},\s*Arrays\.asList\(.*?\)\s*\);',
            re.DOTALL,
        )

        def replace(match, _name=method_name):
            indent = match.group(1)
            return f"{indent}{_name}(m);"

        new_out, n = pattern.subn(replace, out, count=1)
        if n == 0:
            print(
                f"WARN: no existing m.put({tab.const_name}) block; "
                f"tab '{tab.name}' was not substituted",
                file=sys.stderr,
            )
        else:
            out = new_out

    # Build helper method bodies and inject before the final '}' of the class.
    methods_src = []
    for method_name, body in helper_blocks:
        indented_body = "\n".join(
            ("\t\t" + ln) if ln.strip() else "" for ln in body.split("\n")
        )
        methods_src.append(
            f"\tprivate static void {method_name}(Map<String, List<Integer>> m)\n"
            f"\t{{\n"
            f"{indented_body}\n"
            f"\t}}\n"
        )
    methods_block = "\n" + "\n".join(methods_src)

    # Replace the LAST closing brace of the file with methods + brace.
    if not out.rstrip().endswith("}"):
        print("WARN: rendered Java doesn't end with '}'; helper methods not injected",
              file=sys.stderr)
        return out
    out = re.sub(r"\}\s*\Z", methods_block + "}\n", out)
    return out


# ── Diff & report ──────────────────────────────────────────────────────────

def build_report(
    current: dict[str, list[int]],
    classified: dict[str, list[list[tuple[float, int, str]]]],
    tabs: list[TabSpec],
    items_by_id: dict[int, dict],
) -> tuple[str, str]:
    diff_lines: list[str] = []
    summary_lines: list[str] = ["Tab            current    proposed    +added    -dropped"]
    summary_lines.append("─" * 60)

    for tab in tabs:
        cur_ids = set(current.get(tab.name, []))
        prop_pairs: list[tuple[int, str, str]] = []  # (id, name, section_label)
        for i, sec_items in enumerate(classified[tab.name]):
            for tier, iid, name in sec_items:
                prop_pairs.append((iid, name, tab.sections[i].label))
        prop_ids = {p[0] for p in prop_pairs}
        added = prop_ids - cur_ids
        dropped = cur_ids - prop_ids
        kept = cur_ids & prop_ids

        summary_lines.append(
            f"{tab.name:<14} {len(cur_ids):>7}    {len(prop_ids):>8}    "
            f"{len(added):>6}    {len(dropped):>8}"
        )

        diff_lines.append("")
        diff_lines.append(f"=== {tab.name} ===")
        diff_lines.append(
            f"  kept {len(kept)} | added {len(added)} | dropped {len(dropped)}"
        )

        if added:
            diff_lines.append(f"  + Added ({len(added)}):")
            by_section: dict[str, list[tuple[int, str]]] = {}
            for iid, name, sec in prop_pairs:
                if iid in added:
                    by_section.setdefault(sec, []).append((iid, name))
            for sec_label in by_section:
                diff_lines.append(f"      [{sec_label}]")
                for iid, name in sorted(by_section[sec_label]):
                    diff_lines.append(f"        {iid:>6}  {name}")

        if dropped:
            diff_lines.append(f"  - Dropped ({len(dropped)}):")
            for iid in sorted(dropped):
                nm = items_by_id.get(iid, {}).get("name", "(not in osrsbox — possibly newer)")
                diff_lines.append(f"        {iid:>6}  {nm}")

        # First/last 20 sanity check
        flat = [(iid, name) for iid, name, _ in prop_pairs]
        if flat:
            diff_lines.append("  --- first 20 in proposed order ---")
            for iid, name in flat[:20]:
                diff_lines.append(f"        {iid:>6}  {name}")
            if len(flat) > 20:
                diff_lines.append("  --- last 20 in proposed order ---")
                for iid, name in flat[-20:]:
                    diff_lines.append(f"        {iid:>6}  {name}")

    return "\n".join(summary_lines), "\n".join(diff_lines)


# ── Trace mode (classifier diagnostic dump) ────────────────────────────────

def describe_predicate(pred) -> str:
    """Best-effort one-line description of a predicate by inspecting the
    factory's closure cells. Falls back to qualname or repr if the predicate
    isn't one of the mapping.py factories we know about."""
    if pred is None:
        return "(none)"
    try:
        qn = getattr(pred, "__qualname__", None) or repr(pred)
        factory = qn.split(".")[0] if "." in qn else qn
        freevars = pred.__code__.co_freevars if hasattr(pred, "__code__") else ()
        closure = pred.__closure__ or ()
        cells = {v: c.cell_contents for v, c in zip(freevars, closure)}

        if factory == "_name_in":
            names = cells.get("names_set")
            if names is not None:
                n = len(names)
                sample = list(sorted(names))[:5]
                return f"name_in (n={n}, e.g. {sample})"
        if factory in ("_name_starts", "_name_ends", "_name_contains"):
            toks = cells.get("toks_lower", ())
            return f"{factory.lstrip('_')}({list(toks)})"
        if factory == "_slot_pred":
            return f"slot_pred(slot={cells.get('slot')!r}) — melee defence/offence dominance"
        if factory == "_is_range_armour_slot":
            return f"is_range_armour_slot(slot={cells.get('slot')!r}) — ranged defence dominance"
        if factory == "_is_mage_armour_slot":
            return f"is_mage_armour_slot(slot={cells.get('slot')!r}) — magic defence dominance"
        if factory == "_is_range_weapon_type":
            return f"is_range_weapon_type({list(cells.get('types_set', ()))})"
        if factory == "_is_mage_weapon_type":
            return f"is_mage_weapon_type({list(cells.get('types_set', ()))})"
        if factory in ("_or", "_and"):
            preds = cells.get("preds", ())
            sub = [describe_predicate(p) for p in preds]
            return f"{factory.lstrip('_')}([{' | '.join(sub)}])"
        if factory == "_not":
            return f"not({describe_predicate(cells.get('pred'))})"
        # Functions defined at module level in mapping (e.g. _is_melee_weapon, _is_seed, etc.)
        return factory
    except Exception as e:
        return f"{repr(pred)} ({e})"


def trace_match(it: dict, sec) -> dict:
    """Why did this item match this section? Returns {type, detail}."""
    name = it.get("name", "")
    if name in getattr(sec, "force_exclude", ()):
        return {"type": "force_exclude_but_assigned", "detail": "<<bug — should not happen>>"}
    if name in getattr(sec, "force_include", ()):
        return {"type": "force_include", "detail": f"name in force_include list"}
    try:
        if sec.classifier(it):
            return {"type": "predicate", "detail": describe_predicate(sec.classifier)}
    except Exception as e:
        return {"type": "predicate_error", "detail": str(e)}
    return {"type": "unknown",
            "detail": "(assigned but no clear match — possibly variant_allowlist or extra_items injection)"}


def summarize_item_fields(it: dict) -> str:
    """One-line summary of relevant wiki fields for the diagnostic report."""
    fields = []
    if it.get("slot"):
        fields.append(f"slot={it['slot']}")
    if it.get("weapon_type"):
        fields.append(f"weapon_type={it['weapon_type']}")
    eq = it.get("equipment") or {}
    interesting = (
        "attack_stab", "attack_slash", "attack_crush", "attack_magic", "attack_ranged",
        "defence_stab", "defence_slash", "defence_crush", "defence_magic", "defence_ranged",
        "melee_strength", "ranged_strength", "magic_damage", "prayer",
    )
    for k in interesting:
        v = eq.get(k)
        if v:
            fields.append(f"{k}={v}")
    for flag in ("members", "equipable", "equipable_weapon", "quest_item", "noted", "placeholder", "duplicate"):
        if it.get(flag):
            fields.append(f"{flag}=true")
    return ", ".join(fields) if fields else "(no stat fields)"


def diagnose_item_in_tab(it: dict, tab) -> list[dict]:
    """For each section of tab, show whether this item matches or why not.
    Returns a list of {section, result, reason} dicts. Stops at first MATCHED."""
    results = []
    name = it.get("name", "")
    for sec in tab.sections:
        entry = {"section": sec.label}
        if name in getattr(sec, "force_exclude", ()):
            entry["result"] = "EXCLUDED"
            entry["reason"] = "name in force_exclude"
            results.append(entry)
            continue
        if name in getattr(sec, "force_include", ()):
            entry["result"] = "MATCHED"
            entry["reason"] = "force_include"
            results.append(entry)
            return results
        try:
            matched = sec.classifier(it)
        except Exception as e:
            entry["result"] = "ERROR"
            entry["reason"] = f"{describe_predicate(sec.classifier)}: {e}"
            results.append(entry)
            continue
        if matched:
            entry["result"] = "MATCHED"
            entry["reason"] = describe_predicate(sec.classifier)
            results.append(entry)
            return results
        else:
            entry["result"] = "no match"
            entry["reason"] = describe_predicate(sec.classifier)
            results.append(entry)
    return results


# Canonical flagged items from Brief #50 part 1.
# (item_name, optional_target_tab_for_why_not, prose_hint)
FLAGGED_ITEMS: list[tuple[str, str | None, str]] = [
    ("Rocky", None, "Thieving pet — should NOT be in cooking."),
    ("Zombie axe", None, "Quest cosmetic — should NOT be in woodcutting."),
    ("Graceful hood", "agility_thieving", "Should be in agility but isn't."),
    ("Graceful set", None, "Currently in hunter — why?"),
    ("Coins", "misc", "Should be misc, currently in hunter — why?"),
    ("Eclipse moon helm", "range", "Eclipse moon set: melee placement OK but range missing."),
    ("Eclipse moon chestplate", "range", "Eclipse moon set: range coverage check."),
    ("Eclipse moon tassets", "range", "Eclipse moon set: range coverage check."),
    ("Black gloves", None, "Currently in mage — why?"),
    ("Ava's assembler", None, "Currently in mage — why? (Should be range cross-tag)"),
    ("Green dragonhide", "crafting", "Untanned dragonhide — should be in crafting."),
    ("Blue dragonhide", "crafting", "Untanned dragonhide — should be in crafting."),
    ("Red dragonhide", "crafting", "Untanned dragonhide — should be in crafting."),
    ("Black dragonhide", "crafting", "Untanned dragonhide — should be in crafting."),
    ("Shark", None, "Top food — what rule puts food in mage? (Combat food cross-tag inspection)"),
    ("Monkfish", None, "Top food — what rule puts food in mage?"),
    ("Karambwan", None, "Top food — what rule puts food in mage?"),
]


def write_reasoning_report(
    classified: dict[str, list[list[tuple[float, int, str]]]],
    tabs: list,
    items_by_id: dict[int, dict],
    out_path: Path,
) -> None:
    """Generate the full per-item diagnostic dump."""
    from datetime import datetime, timezone

    # name -> canonical id (skip non-canonical IDs like the classifier does)
    name_to_canonical: dict[str, int] = {}
    for iid in sorted(items_by_id.keys()):
        it = items_by_id[iid]
        if it.get("noted") or it.get("placeholder") or it.get("duplicate"):
            continue
        nm = it.get("name") or ""
        name_to_canonical.setdefault(nm, iid)

    # Build assignments map: id -> [{tab, section, type, detail}]
    assignments: dict[int, list[dict]] = {}
    for tab in tabs:
        for i, sec_items in enumerate(classified[tab.name]):
            sec = tab.sections[i]
            for _, iid, _name in sec_items:
                it = items_by_id.get(iid, {})
                tm = trace_match(it, sec)
                assignments.setdefault(iid, []).append({
                    "tab": tab.name,
                    "section": sec.label,
                    **tm,
                })

    lines: list[str] = []
    lines.append("# Classifier reasoning dump")
    lines.append("")
    lines.append(f"Generated {datetime.now(timezone.utc).isoformat(timespec='seconds')}")
    lines.append("")
    lines.append("Per-item trace of which tab/section assigned each item, via which")
    lines.append("match path. Predicate descriptors are introspected from the factory's")
    lines.append("closure cells where possible; falls back to qualname for module-level")
    lines.append("predicates and to repr() for anything unrecognized.")
    lines.append("")

    # FLAGGED MISCLASSIFICATIONS at top
    lines.append("## FLAGGED MISCLASSIFICATIONS")
    lines.append("")
    lines.append("Specific items reported by in-game testing. Each block shows current")
    lines.append("assignment(s), the rule that placed the item there, and (if a target")
    lines.append("tab is given) why the item is or isn't matched by that tab's sections.")
    lines.append("")
    for name, target_tab, hint in FLAGGED_ITEMS:
        lines.append(f"### {name}")
        lines.append("")
        lines.append(f"_{hint}_")
        lines.append("")
        iid = name_to_canonical.get(name)
        if iid is None:
            lines.append(f"- **Not found** in wiki dataset (or only present as noted/placeholder/duplicate).")
            lines.append("")
            continue
        it = items_by_id[iid]
        lines.append(f"- **ID**: {iid}")
        lines.append(f"- **Wiki fields**: {summarize_item_fields(it)}")
        asg = assignments.get(iid, [])
        tabs_in = sorted({a["tab"] for a in asg})
        lines.append(f"- **Currently assigned to**: {', '.join(tabs_in) if tabs_in else '_(no tabs)_'}")
        if asg:
            lines.append(f"- **Rules matched**:")
            for a in asg:
                lines.append(f"  - `{a['tab']}/{a['section']}`: **{a['type']}** — {a['detail']}")
        if target_tab:
            target = next((t for t in tabs if t.name == target_tab), None)
            if target:
                lines.append(f"- **Diagnostic for `{target_tab}` tab**:")
                diag = diagnose_item_in_tab(it, target)
                matched_any = False
                for d in diag:
                    if d["result"] == "MATCHED":
                        lines.append(f"  - `{d['section']}`: ✓ **MATCHED** ({d['reason']})")
                        matched_any = True
                    elif d["result"] == "EXCLUDED":
                        lines.append(f"  - `{d['section']}`: ✗ excluded (force_exclude)")
                    elif d["result"] == "ERROR":
                        lines.append(f"  - `{d['section']}`: ⚠ error — {d['reason']}")
                    # Skip "no match" entries to keep report focused; readers can re-run
                    # the diagnostic to see every section if needed.
                if not matched_any:
                    lines.append(f"  - **No section matched** — item is not classified into `{target_tab}`.")
        lines.append("")

    # Full per-tab dump
    lines.append("## All assignments by tab")
    lines.append("")
    lines.append("Sorted alphabetically within each tab. The line shows section + match")
    lines.append("path + predicate descriptor + cross-tag tabs + key wiki fields.")
    lines.append("")
    for tab in tabs:
        items_in_tab: list[tuple[int, str, str]] = []
        for i, sec_items in enumerate(classified[tab.name]):
            for _, iid, name in sec_items:
                items_in_tab.append((iid, name, tab.sections[i].label))
        items_in_tab.sort(key=lambda x: (x[1].lower(), x[0]))

        lines.append(f"### {tab.name} ({len(items_in_tab)} items)")
        lines.append("")
        for iid, name, sec_label in items_in_tab:
            it = items_by_id.get(iid, {})
            this_asg = next(
                (a for a in assignments.get(iid, [])
                 if a["tab"] == tab.name and a["section"] == sec_label),
                None,
            )
            all_tabs = sorted({a["tab"] for a in assignments.get(iid, [])})
            cross = [t for t in all_tabs if t != tab.name]
            mt = this_asg["type"] if this_asg else "?"
            det = this_asg["detail"] if this_asg else "?"
            cross_str = f" · cross: {', '.join(cross)}" if cross else ""
            lines.append(
                f"- **{name}** (ID: {iid}) — `{sec_label}` · {mt} · {det}{cross_str}"
            )
            lines.append(f"  - {summarize_item_fields(it)}")
        lines.append("")

    out_path.write_text("\n".join(lines))


# ── Delta mode ─────────────────────────────────────────────────────────────

def compute_delta(
    current: dict[str, list[int]],
    classified: dict[str, list[list[tuple[float, int, str]]]],
    tabs: list[TabSpec],
    items_by_id: dict[int, dict],
) -> dict:
    """Diff the live SkillBankData.java tab assignments against the freshly
    classified wiki data. Returns three buckets: new, removed, reclassified.

    - new: items the classifier placed in 1+ tabs that aren't anywhere in
      the live data. Includes per-tab + section-label so the report can
      show which classifier rule matched.
    - removed: items in the live data that the wiki+osrsbox merge no
      longer contains at all (renamed/deleted/etc.). Never auto-removed.
    - reclassified: items that exist in both live and proposed but with
      different tab assignments. Flagged for manual review.
    - unclassified: items present in the merged wiki dataset but not
      placed in any tab by the classifier AND not in the live data.
      Carved out separately from "new" so the report can highlight them
      as needing manual rules.
    """
    # Build proposed: id -> {tab: [section_label, ...]}
    proposed: dict[int, dict[str, list[str]]] = {}
    for tab in tabs:
        for i, sec_items in enumerate(classified[tab.name]):
            for _, iid, _name in sec_items:
                proposed.setdefault(iid, {}).setdefault(tab.name, []).append(
                    tab.sections[i].label
                )

    # Build live: id -> {tab, ...}
    live: dict[int, set[str]] = {}
    for tab_name, ids in current.items():
        for iid in ids:
            live.setdefault(iid, set()).add(tab_name)

    # All IDs the wiki+osrsbox merge actually knows about right now.
    wiki_known_ids = set(items_by_id.keys())

    new_items: list[dict] = []
    unclassified_items: list[dict] = []
    removed_items: list[dict] = []
    reclassified_items: list[dict] = []

    # New / unclassified: in wiki dataset, not in live.
    for iid in sorted(wiki_known_ids - live.keys()):
        it = items_by_id.get(iid, {})
        # Skip non-canonical/noted/placeholder/dup — classifier ignores them too.
        if it.get("noted") or it.get("placeholder") or it.get("duplicate"):
            continue
        name = it.get("name", "?")
        if iid in proposed:
            new_items.append({
                "id": iid,
                "name": name,
                "tabs": sorted(proposed[iid].keys()),
                "sections": dict(proposed[iid]),
            })
        else:
            unclassified_items.append({
                "id": iid,
                "name": name,
                "release_date": it.get("release_date") or "",
            })

    # Removed: in live, not in wiki dataset at all.
    for iid in sorted(live.keys() - wiki_known_ids):
        removed_items.append({
            "id": iid,
            "name": "(not in wiki dataset)",
            "current_tabs": sorted(live[iid]),
        })

    # Reclassified: in both, tab set differs.
    # Compare normalized tab sets (sorted lists of tab names).
    for iid in sorted(live.keys() & proposed.keys()):
        old_tabs = live[iid]
        new_tabs = set(proposed[iid].keys())
        if old_tabs != new_tabs:
            reclassified_items.append({
                "id": iid,
                "name": items_by_id.get(iid, {}).get("name", "?"),
                "old_tabs": sorted(old_tabs),
                "new_tabs": sorted(new_tabs),
                "added_to": sorted(new_tabs - old_tabs),
                "removed_from": sorted(old_tabs - new_tabs),
                "sections": dict(proposed[iid]),
            })

    return {
        "new": new_items,
        "unclassified": unclassified_items,
        "removed": removed_items,
        "reclassified": reclassified_items,
    }


def write_delta_report(
    delta: dict,
    out_path: Path,
    wiki_cache_fresh: bool,
) -> None:
    """Render a human-readable markdown delta report."""
    from datetime import datetime, timezone

    lines: list[str] = []
    lines.append(f"# SkillBankData delta report")
    lines.append("")
    lines.append(f"- **Run date:** {datetime.now(timezone.utc).isoformat(timespec='seconds')}")
    lines.append(f"- **Wiki source:** {'live fetch (--refresh-wiki)' if wiki_cache_fresh else 'cached'}")
    lines.append("")
    lines.append("## Counts")
    lines.append("")
    lines.append(f"- New (auto-classified): **{len(delta['new'])}**")
    lines.append(f"- Unclassified (in wiki, no classifier match): **{len(delta['unclassified'])}**")
    lines.append(f"- Removed (in live data, no longer in wiki): **{len(delta['removed'])}**")
    lines.append(f"- Reclassified (tab assignment changed): **{len(delta['reclassified'])}**")
    lines.append("")

    # New items — group by primary tab so the reader can scan by skill.
    if delta["new"]:
        lines.append("## New items (auto-classified)")
        lines.append("")
        by_tab: dict[str, list[dict]] = {}
        for item in delta["new"]:
            primary = item["tabs"][0] if item["tabs"] else "(none)"
            by_tab.setdefault(primary, []).append(item)
        for tab in sorted(by_tab):
            lines.append(f"### {tab}")
            lines.append("")
            lines.append("| ID | Name | Tabs | Sections |")
            lines.append("|----|------|------|----------|")
            for item in by_tab[tab]:
                tabs_str = ";".join(item["tabs"])
                sec_str = " · ".join(
                    f"{t}/{s}"
                    for t, secs in sorted(item["sections"].items())
                    for s in secs
                )
                # Escape pipes in names defensively.
                nm = (item["name"] or "").replace("|", "\\|")
                lines.append(f"| {item['id']} | {nm} | {tabs_str} | {sec_str} |")
            lines.append("")

    # Unclassified — flag for manual review or new rules.
    if delta["unclassified"]:
        lines.append("## Unclassified items (no classifier match)")
        lines.append("")
        lines.append("These items exist in the wiki dataset but the classifier didn't")
        lines.append("place them in any tab. They need manual review or new rules.")
        lines.append("")
        lines.append("| ID | Name | Release |")
        lines.append("|----|------|---------|")
        for item in delta["unclassified"]:
            nm = (item["name"] or "").replace("|", "\\|")
            lines.append(f"| {item['id']} | {nm} | {item['release_date']} |")
        lines.append("")

    if delta["removed"]:
        lines.append("## Removed items (no longer in wiki dataset)")
        lines.append("")
        lines.append("These IDs are in the live SkillBankData but the wiki+osrsbox merge")
        lines.append("no longer knows about them (renamed/deleted/merged). They are NOT")
        lines.append("auto-removed — review and drop manually if appropriate.")
        lines.append("")
        lines.append("| ID | Current tabs |")
        lines.append("|----|--------------|")
        for item in delta["removed"]:
            lines.append(f"| {item['id']} | {';'.join(item['current_tabs'])} |")
        lines.append("")

    if delta["reclassified"]:
        lines.append("## Reclassified items (tab assignment changed)")
        lines.append("")
        lines.append("These items exist in both live and proposed data, but the classifier")
        lines.append("now assigns them to different tabs. Review whether the new")
        lines.append("classification is correct — never auto-promoted.")
        lines.append("")
        lines.append("| ID | Name | Old tabs | New tabs | Δ |")
        lines.append("|----|------|----------|----------|---|")
        for item in delta["reclassified"]:
            nm = (item["name"] or "").replace("|", "\\|")
            delta_parts = []
            if item["added_to"]:
                delta_parts.append("+" + ",".join(item["added_to"]))
            if item["removed_from"]:
                delta_parts.append("-" + ",".join(item["removed_from"]))
            lines.append(
                f"| {item['id']} | {nm} | {';'.join(item['old_tabs'])} | "
                f"{';'.join(item['new_tabs'])} | {' '.join(delta_parts)} |"
            )
        lines.append("")

    if not any(delta[k] for k in ("new", "unclassified", "removed", "reclassified")):
        lines.append("## No drift")
        lines.append("")
        lines.append("Live data matches classifier output exactly. Nothing to report.")
        lines.append("")

    out_path.write_text("\n".join(lines))


def filter_classified_to_ids(
    classified: dict[str, list[list[tuple[float, int, str]]]],
    keep_ids: set[int],
    tabs: list[TabSpec],
) -> dict[str, list[list[tuple[float, int, str]]]]:
    """Return a copy of `classified` keeping only items whose id is in keep_ids.
    Used by --auto-promote to render a tailored output containing only new
    auto-classified items, so legacy carry-over preserves everything else
    untouched."""
    out: dict[str, list[list[tuple[float, int, str]]]] = {
        t.name: [[] for _ in t.sections] for t in tabs
    }
    for tab in tabs:
        for i, sec_items in enumerate(classified[tab.name]):
            out[tab.name][i] = [r for r in sec_items if r[1] in keep_ids]
    return out


# ── Main ────────────────────────────────────────────────────────────────────

def run_llm_classification(
    items_by_id: dict[int, dict],
    current: dict[str, list[int]],
    out_dir: Path,
    *,
    pilot: int | None = None,
    reclassify: bool = False,
    new_only: bool = False,
    use_cache: bool = True,
) -> dict:
    """Run LLM classification for some/all items. Writes JSON + Markdown reports.

    Returns the assembled classification dict so callers can post-process.
    """
    import random
    sys.path.insert(0, str(SCRIPT_DIR))
    import wiki_page  # type: ignore
    import llm_classifier  # type: ignore

    api_key = os.environ.get("ANTHROPIC_API_KEY")
    if not api_key:
        print(
            "ERROR: ANTHROPIC_API_KEY not set. Create tools/skillbank-data/.env "
            "from .env.example or export the variable directly.",
            file=sys.stderr,
        )
        sys.exit(2)

    classifiable: list[dict] = []
    for iid in sorted(items_by_id):
        it = items_by_id[iid]
        if it.get("noted") or it.get("placeholder") or it.get("duplicate"):
            continue
        classifiable.append(it)

    # --new-only: drop items already present somewhere in SkillBankData.java
    if new_only:
        currently_classified: set[int] = set()
        for ids in current.values():
            currently_classified.update(ids)
        before = len(classifiable)
        classifiable = [it for it in classifiable if it["id"] not in currently_classified]
        print(
            f"--new-only: filtered {before} → {len(classifiable)} (dropped already-classified)",
            file=sys.stderr,
        )

    # --pilot N: sample. Always include the flagged items if they're in scope.
    if pilot is not None:
        flagged_names = {n for n, _, _ in FLAGGED_ITEMS}
        flagged_items = [it for it in classifiable if it.get("name") in flagged_names]
        flagged_ids = {it["id"] for it in flagged_items}
        remaining = [it for it in classifiable if it["id"] not in flagged_ids]
        n_random = max(0, pilot - len(flagged_items))
        rng = random.Random(0xBADC0DE)
        rng.shuffle(remaining)
        sample = flagged_items + remaining[:n_random]
        print(
            f"--pilot {pilot}: {len(flagged_items)} flagged + "
            f"{min(n_random, len(remaining))} random = {len(sample)} total items",
            file=sys.stderr,
        )
        classifiable = sample

    print(
        f"Classifying {len(classifiable)} item(s) via LLM "
        f"(model={llm_classifier.DEFAULT_MODEL}, cache={'off' if reclassify else 'on'})...",
        file=sys.stderr,
    )

    results: dict[int, dict] = {}
    n_cached = 0
    n_fresh = 0
    n_errors = 0
    total_in = total_out = total_cache_read = total_cache_write = 0

    for i, it in enumerate(classifiable, 1):
        name = it.get("name") or f"id={it['id']}"
        page_name = it.get("page_name") or ""
        if not page_name and it.get("page_name_sub"):
            page_name = it["page_name_sub"].split("#", 1)[0]
        if not page_name:
            page_name = name  # last resort

        try:
            wt = wiki_page.fetch_page_wikitext(
                page_name, WIKI_PAGE_CACHE_DIR,
                use_cache=use_cache, refresh=False,
            )
        except Exception as e:
            print(f"  [{i}/{len(classifiable)}] {name}: wiki fetch failed: {e}", file=sys.stderr)
            wt = None
        wt_trimmed = wiki_page.trim_wikitext(wt or "")

        try:
            result = llm_classifier.classify_item(
                it, wt_trimmed, LLM_CACHE_DIR, api_key,
                use_cache=use_cache and not reclassify,
                refresh=reclassify,
            )
        except Exception as e:
            print(f"  [{i}/{len(classifiable)}] {name}: classify failed: {e}", file=sys.stderr)
            n_errors += 1
            continue

        results[it["id"]] = {
            "id": it["id"],
            "name": name,
            "primary_tab": result["primary_tab"],
            "cross_tags": result["cross_tags"],
            "rationale": result["rationale"],
        }
        if result.get("_cached"):
            n_cached += 1
        else:
            n_fresh += 1
            u = result.get("_usage") or {}
            total_in += u.get("input_tokens") or 0
            total_out += u.get("output_tokens") or 0
            total_cache_read += u.get("cache_read_input_tokens") or 0
            total_cache_write += u.get("cache_creation_input_tokens") or 0

        if i % 25 == 0 or i == len(classifiable):
            print(
                f"  progress {i}/{len(classifiable)} "
                f"(cached={n_cached}, fresh={n_fresh}, errors={n_errors})",
                file=sys.stderr,
            )

    # Write outputs
    out_dir.mkdir(parents=True, exist_ok=True)
    json_path = out_dir / "llm-classifications.json"
    json_path.write_text(json.dumps({
        "model": llm_classifier.DEFAULT_MODEL,
        "count": len(results),
        "cached": n_cached,
        "fresh": n_fresh,
        "errors": n_errors,
        "usage_totals": {
            "input_tokens": total_in,
            "output_tokens": total_out,
            "cache_read_input_tokens": total_cache_read,
            "cache_creation_input_tokens": total_cache_write,
        },
        "items": list(results.values()),
    }, indent=2))
    print(f"Wrote {json_path}", file=sys.stderr)

    # Diff vs current data
    diff_path = out_dir / "classification-diff.md"
    write_classification_diff(results, current, items_by_id, diff_path)
    print(f"Wrote {diff_path}", file=sys.stderr)

    # Cost estimate (Sonnet 4.6 pricing as of 2026-01: $3/MTok input, $15/MTok output)
    in_cost = total_in * 3 / 1_000_000
    out_cost = total_out * 15 / 1_000_000
    cache_read_cost = total_cache_read * 0.30 / 1_000_000
    cache_write_cost = total_cache_write * 3.75 / 1_000_000
    print(
        f"\nFresh queries: {n_fresh}  cached: {n_cached}  errors: {n_errors}\n"
        f"Usage: input={total_in:,}  cache_read={total_cache_read:,}  "
        f"cache_write={total_cache_write:,}  output={total_out:,}\n"
        f"Estimated cost (Sonnet 4.6 pricing, may vary):\n"
        f"  input ${in_cost:.4f} + cache_read ${cache_read_cost:.4f} "
        f"+ cache_write ${cache_write_cost:.4f} + output ${out_cost:.4f} "
        f"= ${in_cost + out_cost + cache_read_cost + cache_write_cost:.4f}",
    )
    return results


def write_classification_diff(
    llm_results: dict[int, dict],
    current: dict[str, list[int]],
    items_by_id: dict[int, dict],
    out_path: Path,
) -> None:
    """Diff LLM-assigned primary tab vs live SkillBankData.java placement."""
    from datetime import datetime, timezone

    # Build current placement map: id -> set[tab]
    current_tabs: dict[int, set[str]] = {}
    for tab, ids in current.items():
        for iid in ids:
            current_tabs.setdefault(iid, set()).add(tab)

    agree: list[tuple[int, str, str, list[str]]] = []
    disagree: list[tuple[int, str, set[str], str, list[str]]] = []
    new_assignments: list[tuple[int, str, str, list[str], str]] = []

    for iid, r in sorted(llm_results.items()):
        name = r["name"]
        primary = r["primary_tab"]
        cross = r["cross_tags"]
        rationale = r["rationale"]
        cur = current_tabs.get(iid, set())
        if not cur:
            new_assignments.append((iid, name, primary, cross, rationale))
        elif primary in cur:
            agree.append((iid, name, primary, cross))
        else:
            disagree.append((iid, name, cur, primary, cross))

    lines: list[str] = []
    lines.append("# LLM classification diff vs live SkillBankData.java")
    lines.append("")
    lines.append(f"Generated {datetime.now(timezone.utc).isoformat(timespec='seconds')}")
    lines.append("")
    lines.append(
        f"- **Total classified**: {len(llm_results)}\n"
        f"- **Agreement** (LLM primary matches a current tab): {len(agree)}\n"
        f"- **Disagreement** (LLM primary not in current tabs): {len(disagree)}\n"
        f"- **New** (item not in any current tab): {len(new_assignments)}"
    )
    lines.append("")

    if disagree:
        lines.append("## Disagreements")
        lines.append("")
        lines.append("LLM proposes a different primary tab than where the item currently lives.")
        lines.append("Use this to decide whether the heuristic was wrong or the LLM is wrong.")
        lines.append("")
        for iid, name, cur, primary, cross in disagree:
            cross_s = f", cross={cross}" if cross else ""
            it = items_by_id.get(iid, {})
            ex = it.get("examine") or ""
            lines.append(f"- **{name}** (ID {iid}) — current: `{sorted(cur)}` → LLM: `{primary}`{cross_s}")
            if ex:
                lines.append(f"  - _examine: {ex}_")
        lines.append("")

    if new_assignments:
        lines.append("## New (not in any current tab)")
        lines.append("")
        for iid, name, primary, cross, rat in new_assignments:
            cross_s = f", cross={cross}" if cross else ""
            lines.append(f"- **{name}** (ID {iid}) → `{primary}`{cross_s}")
            lines.append(f"  - _{rat}_")
        lines.append("")

    if agree:
        lines.append("## Agreements (collapsed)")
        lines.append("")
        for iid, name, primary, cross in agree:
            cross_s = f", cross={cross}" if cross else ""
            lines.append(f"- {name} (ID {iid}): `{primary}`{cross_s}")
        lines.append("")

    out_path.write_text("\n".join(lines))


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--no-fetch", action="store_true",
                   help="reuse osrsbox cache (also implies wiki cache use)")
    p.add_argument("--refresh-wiki", action="store_true",
                   help="invalidate wiki cache and re-fetch all pages")
    p.add_argument("--no-cache", action="store_true",
                   help="bypass wiki cache entirely for this run")
    p.add_argument("--probe-schema", action="store_true",
                   help="probe wiki bucket schemas and write cache/wiki/schema.json, then exit")
    p.add_argument("--tab", action="append", help="restrict to one or more tab names")
    p.add_argument("--out", default=str(OUT_DIR),
                   help="output directory for proposed/diff/report")
    p.add_argument("--strict", action="store_true",
                   help="non-additive: drop items from current data that the new classifier misses.")
    p.add_argument("--promote", action="store_true",
                   help="also overwrite src/main/java/com/skillbank/SkillBankData.java with the result.")
    p.add_argument("--delta", action="store_true",
                   help="diff classifier output against live SkillBankData.java; write out/delta-report.md.")
    p.add_argument("--auto-promote", action="store_true",
                   help="with --delta, also write new auto-classified items back to SkillBankData.java. "
                        "Unclassified, removed, and reclassified items are never auto-promoted.")
    p.add_argument("--trace", action="store_true",
                   help="write out/classifier-reasoning.md — per-item diagnostic dump showing "
                        "which tab/section assigned each item and via which match path.")
    p.add_argument("--classify", action="store_true",
                   help="run LLM-based classification (Anthropic API). Requires ANTHROPIC_API_KEY "
                        "in env or tools/skillbank-data/.env. Writes out/llm-classifications.json "
                        "and out/classification-diff.md. Does NOT promote without --promote.")
    p.add_argument("--pilot", type=int, metavar="N",
                   help="with --classify: classify only N items (flagged items + random sample). "
                        "Recommended first run; use without --pilot only after reviewing pilot output.")
    p.add_argument("--reclassify", action="store_true",
                   help="with --classify: bypass LLM response cache and re-query every item.")
    p.add_argument("--new-only", action="store_true",
                   help="with --classify: only classify items not currently in SkillBankData.java "
                        "(combine with --auto-promote to additively grow coverage).")
    p.add_argument("--llm-render", action="store_true",
                   help="read out/llm-classifications.json and render an LLM-driven "
                        "SkillBankData.java.llm-proposed (with mapping.py force_include/exclude "
                        "as overrides). Does NOT overwrite the live file.")
    p.add_argument("--llm-promote", action="store_true",
                   help="like --llm-render but also overwrites the live "
                        "src/main/java/com/skillbank/SkillBankData.java.")
    args = p.parse_args()

    _load_env()

    import wiki  # type: ignore

    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)
    WIKI_CACHE_DIR.mkdir(parents=True, exist_ok=True)

    if args.probe_schema:
        print("Probing infobox_item schema...", file=sys.stderr)
        item_schema = wiki.probe_schema(WIKI_CACHE_DIR, wiki.ITEM_FIELDS + [
            "equipable", "stackable", "release", "slot"
        ], "infobox_item")
        print("Probing infobox_bonuses schema...", file=sys.stderr)
        bonus_schema = wiki.probe_schema(WIKI_CACHE_DIR, wiki.BONUSES_FIELDS + [
            "item_id", "slot"
        ], "infobox_bonuses")
        schema_out = WIKI_CACHE_DIR / "schema.json"
        schema_out.write_text(json.dumps({
            "infobox_item": item_schema,
            "infobox_bonuses": bonus_schema,
        }, indent=2))
        print(f"Wrote {schema_out}")
        for bkt, sch in [("infobox_item", item_schema), ("infobox_bonuses", bonus_schema)]:
            print(f"\n{bkt}:")
            for f, status in sch.items():
                print(f"  {f}: {status}")
        return

    # Late import so the script can be run without mapping.py being valid yet.
    sys.path.insert(0, str(SCRIPT_DIR))
    import mapping  # type: ignore

    tabs: list[TabSpec] = mapping.TABS
    if args.tab:
        wanted = set(args.tab)
        tabs = [t for t in tabs if t.name in wanted]
        if not tabs:
            print(f"No matching tabs: {args.tab}", file=sys.stderr)
            sys.exit(2)

    use_cache = not args.no_cache
    refresh = args.refresh_wiki

    print(f"Fetching wiki canonical items (use_cache={use_cache}, refresh={refresh})...", file=sys.stderr)
    wiki_items = wiki.fetch_canonical_items(
        WIKI_CACHE_DIR, use_cache=use_cache, refresh=refresh,
    )
    print(f"Fetching wiki equipment bonuses...", file=sys.stderr)
    wiki_bonuses = wiki.fetch_all_bonuses(
        WIKI_CACHE_DIR, use_cache=use_cache, refresh=refresh,
    )

    print(f"Loading osrsbox dump (force_refetch={not args.no_fetch})...", file=sys.stderr)
    osrsbox = fetch_osrsbox(force=not args.no_fetch)
    print(f"  osrsbox: {len(osrsbox)} items", file=sys.stderr)

    print("Merging wiki + osrsbox...", file=sys.stderr)
    items, wiki_only, osrsbox_only, discrepancies = merge_wiki_osrsbox(
        wiki_items, wiki_bonuses, osrsbox,
    )
    print(
        f"  merged: {len(items)} canonical items "
        f"(wiki_only={len(wiki_only)}, osrsbox_only={len(osrsbox_only)}, "
        f"discrepancies={len(discrepancies)})",
        file=sys.stderr,
    )
    (CACHE_DIR / "discrepancies.json").write_text(json.dumps({
        "wiki_only_ids": wiki_only[:200],
        "wiki_only_total": len(wiki_only),
        "osrsbox_only_ids": osrsbox_only[:200],
        "osrsbox_only_total": len(osrsbox_only),
        "name_discrepancies": discrepancies[:200],
        "discrepancies_total": len(discrepancies),
    }, indent=2))

    items_by_id: dict[int, dict] = {int(k): v for k, v in items.items()}

    print("Classifying...", file=sys.stderr)
    classified = classify(items, tabs)

    print("Parsing existing SkillBankData.java...", file=sys.stderr)
    java_src = DATA_JAVA.read_text()
    current, consts = parse_current_data(java_src)

    # Warn about TabSpecs whose const_name isn't actually in the Java source.
    java_consts = set(consts)
    for t in tabs:
        if t.const_name not in java_consts:
            print(
                f"WARN: TabSpec '{t.name}' references {t.const_name} which "
                f"isn't declared in SkillBankData.java — output will be incomplete",
                file=sys.stderr,
            )

    print(f"Rendering proposed file + diff (additive={not args.strict})...", file=sys.stderr)
    proposed = render_proposed(java_src, tabs, classified, current=current, additive=not args.strict)
    (out_dir / "SkillBankData.java.proposed").write_text(proposed)

    summary, diff = build_report(current, classified, tabs, items_by_id)
    (out_dir / "report.txt").write_text(summary + "\n")
    (out_dir / "diff.txt").write_text(diff + "\n")

    print()
    print(summary)
    print()
    print(f"Wrote {out_dir}/SkillBankData.java.proposed")
    print(f"Wrote {out_dir}/report.txt   ({sum(len(c) for c in current.values())} cur ids total)")
    print(f"Wrote {out_dir}/diff.txt     (per-tab adds/drops + first/last 20 sample)")

    # Cross-validation diff: items in current SkillBankData that the wiki+classifier
    # output doesn't pick up. Committed once for human review per the Phase 1.5 brief.
    cross_lines = ["Cross-validation: items in current SkillBankData.java but NOT in wiki-classified output.",
                   "Use this to decide whether each item is genuinely deprecated (drop), needs a classifier fix,",
                   "or should be added to force_include / variant_allowlist before flipping --additive=false.",
                   "", ""]
    for tab in tabs:
        cur_ids = set(current.get(tab.name, []))
        prop_ids = {r[1] for sec_items in classified[tab.name] for r in sec_items}
        only_in_current = sorted(cur_ids - prop_ids)
        if not only_in_current:
            continue
        cross_lines.append(f"=== {tab.name} ({len(only_in_current)} items not in wiki output) ===")
        for iid in only_in_current:
            nm = items_by_id.get(iid, {}).get("name") or "(not in wiki canonical set)"
            cross_lines.append(f"  {iid:>6}  {nm}")
        cross_lines.append("")
    DIFF_REPORT_FILE.write_text("\n".join(cross_lines))
    print(f"Wrote {DIFF_REPORT_FILE} (cross-validation report for --additive flip decision)")

    if args.promote:
        DATA_JAVA.write_text(proposed)
        print(f"\nPROMOTED → {DATA_JAVA}")

    if args.trace:
        print("Writing classifier reasoning dump...", file=sys.stderr)
        trace_path = out_dir / "classifier-reasoning.md"
        write_reasoning_report(classified, tabs, items_by_id, trace_path)
        print(f"Wrote {trace_path}")

    if args.classify:
        run_llm_classification(
            items_by_id, current, out_dir,
            pilot=args.pilot,
            reclassify=args.reclassify,
            new_only=args.new_only,
            use_cache=use_cache,
        )

    if args.llm_render or args.llm_promote:
        llm_json = out_dir / "llm-classifications.json"
        if not llm_json.exists():
            print(
                f"ERROR: {llm_json} not found. Run with --classify first to generate it.",
                file=sys.stderr,
            )
            sys.exit(2)
        import llm_promote  # type: ignore

        print(f"Building LLM-driven Java from {llm_json}...", file=sys.stderr)
        # Preserve the slayer cross-tag loadout from Brief #50: every item
        # currently in slayer (prayer pots, combat pots, top food, cannonballs,
        # salve amulet, etc.) stays in slayer regardless of LLM placement.
        syn_tabs, llm_classified, report = llm_promote.build_synthetic_tabs(
            llm_json, mapping.TABS, items_by_id,
            current_membership=current,
            preserve_tabs=["slayer"],
        )
        # Render without legacy carry-over: LLM run is the new source of truth.
        llm_proposed = render_proposed(
            java_src, syn_tabs, llm_classified, current=None, additive=False,
        )
        llm_proposed_path = out_dir / "SkillBankData.java.llm-proposed"
        llm_proposed_path.write_text(llm_proposed)
        print(f"Wrote {llm_proposed_path}")

        # Per-tab report
        print("\nLLM tab summary:")
        print(
            f"  {'tab':<18} {'count':>6}  {'primary':>7}  {'cross':>5}  "
            f"{'override':>8}  {'ovr_drop':>8}  {'preserved':>9}"
        )
        for tn in llm_promote.ALL_TAB_NAMES:
            r = report["tabs"][tn]
            print(
                f"  {tn:<18} {r['count']:>6}  {r['primary']:>7}  {r['cross']:>5}  "
                f"{r['override']:>8}  {r['override_dropped']:>8}  {r['preserved']:>9}"
            )

        # Show a few override examples so it's obvious what came from mapping.py.
        if report["override_adds_sample"]:
            print("\nForce-include overrides (sample, name-based from mapping.py):")
            for tab, ex in report["override_adds_sample"].items():
                names = ", ".join(f"{n}({i})" for i, n in ex)
                print(f"  +{tab}: {names}")
        if report["override_drops_sample"]:
            print("\nForce-exclude overrides (sample, name-based from mapping.py):")
            for tab, ex in report["override_drops_sample"].items():
                names = ", ".join(f"{n}({i})" for i, n in ex)
                print(f"  -{tab}: {names}")

        if args.llm_promote:
            DATA_JAVA.write_text(llm_proposed)
            print(f"\nPROMOTED → {DATA_JAVA}")

    if args.delta:
        print("Computing delta against live data...", file=sys.stderr)
        delta = compute_delta(current, classified, tabs, items_by_id)
        delta_path = out_dir / "delta-report.md"
        write_delta_report(delta, delta_path, wiki_cache_fresh=args.refresh_wiki)
        print(
            f"\nDelta: new={len(delta['new'])} "
            f"unclassified={len(delta['unclassified'])} "
            f"removed={len(delta['removed'])} "
            f"reclassified={len(delta['reclassified'])}"
        )
        print(f"Wrote {delta_path}")

        if args.auto_promote:
            new_ids = {item["id"] for item in delta["new"]}
            if not new_ids:
                print("--auto-promote: no new auto-classified items, nothing to promote.")
            else:
                # Filter classified to only the new auto-classified items, then
                # render additively so legacy carry-over preserves everything
                # else. This deliberately avoids promoting reclassifications.
                tailored = filter_classified_to_ids(classified, new_ids, tabs)
                promoted_src = render_proposed(
                    java_src, tabs, tailored, current=current, additive=True,
                )
                DATA_JAVA.write_text(promoted_src)
                print(
                    f"AUTO-PROMOTED {len(new_ids)} new item(s) → {DATA_JAVA}. "
                    f"Removed/reclassified items left untouched for manual review."
                )


if __name__ == "__main__":
    main()
