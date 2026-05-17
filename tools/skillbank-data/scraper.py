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
OUT_DIR = SCRIPT_DIR / "out"
DATA_JAVA = REPO_ROOT / "src/main/java/com/skillbank/SkillBankData.java"

OSRSBOX_URL = (
    "https://raw.githubusercontent.com/0xNeffarion/osrsreboxed-db/master/"
    "docs/items-complete.json"
)
USER_AGENT = "SkillBankTabs/0.1 (https://github.com/Frailrain/Bank-Skill-Sorting)"


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


def render_proposed(
    java_src: str,
    tabs: list[TabSpec],
    classified: dict,
    current: dict[str, list[int]] | None = None,
    additive: bool = True,
) -> str:
    """Substitute m.put blocks. If `additive` and `current` provided, append a
    Legacy section per tab for items in the current data that the new classifier
    didn't pick up — never silently regresses a tab."""
    out = java_src
    current = current or {}
    for tab in tabs:
        proposed_ids = {
            r[1] for sec_items in classified[tab.name] for r in sec_items
        }
        legacy_ids: list[int] = []
        if additive and tab.name in current:
            legacy_ids = sorted(set(current[tab.name]) - proposed_ids)
        block = render_block(tab, classified[tab.name], legacy_ids=legacy_ids)
        pattern = re.compile(
            rf'([ \t]*)m\.put\({re.escape(tab.const_name)},\s*Arrays\.asList\(.*?\)\s*\);',
            re.DOTALL,
        )

        def replace(match, _block=block):
            indent = match.group(1)
            return "\n".join(
                (indent + ln) if ln.strip() else "" for ln in _block.split("\n")
            )

        new_out, n = pattern.subn(replace, out, count=1)
        if n == 0:
            print(
                f"WARN: no existing m.put({tab.const_name}) block; "
                f"tab '{tab.name}' was not substituted",
                file=sys.stderr,
            )
        else:
            out = new_out
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


# ── Main ────────────────────────────────────────────────────────────────────

def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--no-fetch", action="store_true", help="reuse osrsbox cache")
    p.add_argument("--tab", action="append", help="restrict to one or more tab names")
    p.add_argument("--out", default=str(OUT_DIR), help="output directory for proposed/diff/report")
    p.add_argument("--strict", action="store_true",
                   help="non-additive: drop items from current data that the new classifier misses (regresses tabs).")
    p.add_argument("--promote", action="store_true",
                   help="also overwrite src/main/java/com/skillbank/SkillBankData.java with the result.")
    args = p.parse_args()

    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)

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

    print(f"Loading osrsbox dump (force_refetch={not args.no_fetch})...", file=sys.stderr)
    items = fetch_osrsbox(force=not args.no_fetch)
    print(f"  {len(items)} items in dump", file=sys.stderr)
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

    if args.promote:
        DATA_JAVA.write_text(proposed)
        print(f"\nPROMOTED → {DATA_JAVA}")


if __name__ == "__main__":
    main()
