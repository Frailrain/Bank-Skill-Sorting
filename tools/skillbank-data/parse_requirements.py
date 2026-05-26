"""Brief #71: parse skill-level requirements from OSRS Wiki wikitext.

Reads the equipable-item list from data/wiki-equipment-data.json (Brief #70
output), fetches each item's wikitext via wiki_page.fetch_page_wikitext
(Brief #51 — caches to cache/wiki-pages/, rate-limited 1 req/sec), then
regex-extracts (skill, level) pairs from the page text.

Output:
  data/wiki-requirements.json — per-item requirement records
  out/requirements-failures.txt — items where confidence is medium or low
  out/requirements-crossref.md — wiki ↔ osrsbox rq comparison

Phrasings handled (verified manually against 15+ representative pages):

  "requires an [[Attack]] level of 70 to wield"                 → P1
  "requires a [[Defence]] level of 55 to wear"                  → P1
  "requires level 65 [[Defence]] to wear"                       → P2
  "requiring level 60 [[Magic]] and 60 [[Defence]] to equip"    → P2 (gerund)
  "requires 75 [[Attack]] to wield"                             → P3
  "requiring 75 [[Ranged]] to wield"                            → P3 (gerund)
  "requires 70 [[Defence]] and completion of …"                 → P3 (quest tail ignored)
  "must have at least 42 [[Attack]], [[Strength]], … and 22 [[Prayer]]"  → P3 + compound spread
  "It has no requirements to wear"                              → NO_REQ
"""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
WIKI_DATA = SCRIPT_DIR / "data" / "wiki-equipment-data.json"
WIKI_PAGE_CACHE = SCRIPT_DIR / "cache" / "wiki-pages"
OUT_JSON = SCRIPT_DIR / "data" / "wiki-requirements.json"
FAILURES_TXT = SCRIPT_DIR / "out" / "requirements-failures.txt"
CROSSREF_MD = SCRIPT_DIR / "out" / "requirements-crossref.md"
META_PATH = SCRIPT_DIR.parent.parent / "src/main/resources/com/skillbank/item-meta.json"

# Skills that actually gate wearing / wielding equipable items. The full
# OSRS skill list includes Smithing/Crafting/Fletching/Runecraft/etc., but
# those gate item *creation*, not equipping — including them produces
# false positives like "Bronze sword requires level 4 Smithing" (creation
# recipe) or "Rune platebody requires level 99 Smithing" (max-tier
# smithing recipe). Slayer + Agility kept for slayer-task-gated gear and
# the agility-gated hallowed gear.
SKILLS = [
    "Attack", "Strength", "Defence", "Ranged", "Magic", "Prayer",
    "Hitpoints", "Slayer", "Agility",
]
SKILL_RE = r"(?:" + "|".join(SKILLS) + r")"
SKILL_LOOKUP = {s.lower(): s for s in SKILLS}

# Strip wiki link wrappers so [[Defence]] and [[Magic|Magic level]] both
# reduce to plain "Defence" / "Magic" before the regex sees them.
LINK_RE = re.compile(r"\[\[([^\]|]+)(?:\|[^\]]*)?\]\]")
TEMPLATE_RE = re.compile(r"\{\{[^{}]*\}\}")
BOLD_ITALIC_RE = re.compile(r"'{2,5}")
HTML_TAG_RE = re.compile(r"<[^>]+>")


def _clean(wikitext: str) -> str:
    """Reduce wikitext to plain-ish prose for regex matching."""
    text = wikitext
    # Strip nested templates {{ ... }} — apply repeatedly to handle nesting.
    for _ in range(8):
        new = TEMPLATE_RE.sub(" ", text)
        if new == text:
            break
        text = new
    text = LINK_RE.sub(r"\1", text)
    text = BOLD_ITALIC_RE.sub("", text)
    text = HTML_TAG_RE.sub(" ", text)
    return text


def _normalise_skill(token: str) -> str:
    return SKILL_LOOKUP.get(token.strip().lower(), token)


# Pattern 1: "requires a/an Skill level of N"
P1 = re.compile(
    rf"requir(?:es?|ing|ed)\s+(?:an?\s+)({SKILL_RE})\s+level\s+of\s+(\d+)",
    re.IGNORECASE,
)
# Pattern 2: "requir(?:es|ing|ed) (a )?level N Skill"
P2 = re.compile(
    rf"requir(?:es?|ing|ed)\s+(?:a\s+)?level\s+(\d+)\s+({SKILL_RE})",
    re.IGNORECASE,
)
# Pattern 3: "requir(?:es|ing|ed) (at least|only) N Skill" — most common
P3 = re.compile(
    rf"requir(?:es?|ing|ed)\s+(?:(?:at\s+least|only)\s+)?(\d+)\s+({SKILL_RE})",
    re.IGNORECASE,
)
# Pattern 4: "have (at least) N Skill" — Dragon scimitar pattern
P4 = re.compile(
    rf"\b(?:have|has)\s+(?:at\s+least\s+)?(\d+)\s+({SKILL_RE})\b",
    re.IGNORECASE,
)
# Pattern 5: "at least N Skill" — Void Knight prelude
P5 = re.compile(
    rf"at\s+least\s+(\d+)\s+({SKILL_RE})",
    re.IGNORECASE,
)
# Pattern 6: "Level N Skill" — only when paired with wield/wear/equip nearby
# (intentionally narrower than P2_BARE_LEVEL in earlier draft, which
# false-positively caught creation levels like "level 99 Smithing".)
P6_GATED = re.compile(
    rf"[Ll]evel\s+(\d+)\s+({SKILL_RE})\b[^.]{{0,80}}\bto\s+(?:wield|wear|equip|use)",
    re.IGNORECASE,
)
# Compound spread: a (N, Skill1) match where the tail "(?:, Skill)+(?:,? and Skill)?"
# propagates N to every additional skill name. Used for Void knight's
# "42 Attack, Strength, Defence, Hitpoints, Ranged, and Magic" list and
# Armadyl chainskirt's "70 Defence and Ranged" pair.
COMPOUND_SPREAD = re.compile(
    rf"(\d+)\s+{SKILL_RE}\b\s*((?:,\s*{SKILL_RE}\b\s*)*(?:,?\s*and\s+{SKILL_RE})?)",
    re.IGNORECASE,
)
# Tail-only "along with N Skill" / "plus N Skill" — picks up Void Knight's
# trailing Prayer 22 that the comma-list spread can't reach.
ALONG_WITH = re.compile(
    rf"(?:along\s+with|plus)\s+(\d+)\s+({SKILL_RE})",
    re.IGNORECASE,
)
# Skill token (with word boundary) for scanning compound tails.
SKILL_TOKEN = re.compile(rf"\b({SKILL_RE})\b", re.IGNORECASE)

# Explicit no-requirement (both indicative + descriptive phrasings).
NO_REQ_RE = re.compile(
    r"(?:has|have)\s+no\s+(?:level\s+)?requirements?\s+to\s+(?:wield|wear|equip|use)",
    re.IGNORECASE,
)

# Two-skill compound with separate numbers, e.g. "level 60 Magic and 60 Defence
# to equip" — Bloodbark body. Captures the second (N2, Skill2) pair.
TWO_NUM_PAIR = re.compile(
    rf"\b(\d+)\s+{SKILL_RE}\s+and\s+(\d+)\s+({SKILL_RE})\b",
    re.IGNORECASE,
)

# Keywords that indicate a wield/wear context. A (skill, level) match is
# only kept if one of these appears within EQUIP_CTX_DISTANCE chars of
# the match position — this kills false positives from spell mentions,
# enchant recipes, smithing recipes, and other non-wear contexts.
EQUIP_CTX_RE = re.compile(
    r"\b(?:wield|wear|equip|wielded|worn|equipped|wielding|wearing|equipping|"
    r"to\s+use|to\s+wield|to\s+wear|to\s+equip)\b",
    re.IGNORECASE,
)
EQUIP_CTX_DISTANCE = 100


def _in_equip_context(text: str, match_start: int, match_end: int) -> bool:
    """Return True if a wield/wear/equip keyword appears within
    EQUIP_CTX_DISTANCE chars of the match. Used to reject false positives
    where 'requires N Skill' refers to a spell, crafting recipe, or
    other non-wear context."""
    lo = max(0, match_start - EQUIP_CTX_DISTANCE)
    hi = min(len(text), match_end + EQUIP_CTX_DISTANCE)
    window = text[lo:hi]
    return bool(EQUIP_CTX_RE.search(window))


def parse_requirements(wikitext: str) -> tuple[dict[str, int], str | None]:
    """Return ({skill: level}, source_text) extracted from wikitext.

    {} means we confirmed no requirements (the page explicitly says so OR
    the lead paragraph mentions wielding/wearing with no level required).
    Empty {} with source_text=None means we found no requirement text in
    the lead — caller decides what confidence to assign.
    """
    text = _clean(wikitext)

    # Restrict matching to the lead section: text before the first "==" header.
    # This sidesteps the bulk of false positives (creation recipes, related-
    # item references, trivia sections that mention combat levels).
    lead, _, _ = text.partition("\n==")

    if NO_REQ_RE.search(lead):
        m = NO_REQ_RE.search(lead)
        start = max(0, m.start() - 60)
        end = min(len(lead), m.end() + 30)
        return {}, lead[start:end].strip()

    reqs: dict[str, int] = {}
    source_text: str | None = None

    def record(skill: str, level: int, ctx: str | None = None):
        nonlocal source_text
        skill = _normalise_skill(skill).lower()
        if skill in reqs:
            return
        # Drop trivially-weak "level 1 X" — these are descriptive ("can be
        # used with level 1 Defence"), not gating.
        if level <= 1:
            return
        reqs[skill] = level
        if source_text is None and ctx:
            source_text = ctx

    for pat, n_group, skill_group in (
        (P1, 2, 1),
        (P2, 1, 2),
        (P3, 1, 2),
        (P4, 1, 2),
        (P5, 1, 2),
        (P6_GATED, 1, 2),
    ):
        for m in pat.finditer(lead):
            if not _in_equip_context(lead, m.start(), m.end()):
                continue
            n = int(m.group(n_group))
            sk = m.group(skill_group)
            ctx_start = max(0, m.start() - 30)
            ctx_end = min(len(lead), m.end() + 60)
            record(sk, n, lead[ctx_start:ctx_end].strip())

    # Two-skill compound with separate numbers: "60 Magic and 60 Defence to
    # equip" — Bloodbark body. Captures the second pair when the first was
    # already picked up by one of the above patterns.
    for m in TWO_NUM_PAIR.finditer(lead):
        if not _in_equip_context(lead, m.start(), m.end()):
            continue
        record(m.group(3), int(m.group(2)))

    # Compound spread: any "N Skill1, Skill2, ..., and Skill4" — same N
    # applies to all listed skills. Also matches the simple two-skill
    # "N Skill1 and Skill2" form (Armadyl chainskirt: "70 Defence and
    # Ranged"). Context-checked.
    for m in COMPOUND_SPREAD.finditer(lead):
        if not _in_equip_context(lead, m.start(), m.end()):
            continue
        n = int(m.group(1))
        tail = m.group(2) or ""
        for sm in SKILL_TOKEN.finditer(tail):
            record(sm.group(1), n)

    # "along with N Skill" / "plus N Skill" — Void Knight's trailing 22
    # Prayer after the 42-X compound list. Context-checked.
    for m in ALONG_WITH.finditer(lead):
        if not _in_equip_context(lead, m.start(), m.end()):
            continue
        record(m.group(2), int(m.group(1)))

    return reqs, source_text


# ── Runner ──────────────────────────────────────────────────────────────────


def run():
    if not WIKI_DATA.exists():
        print(f"ERROR: {WIKI_DATA} not found. Run --wiki-fetch first.",
              file=sys.stderr)
        sys.exit(2)
    sys.path.insert(0, str(SCRIPT_DIR))
    import wiki_page  # type: ignore

    wiki = json.loads(WIKI_DATA.read_text())
    items = wiki["items"]

    out: list[dict] = []
    high = medium = low = 0

    # We classify per UNIQUE page_name to avoid refetching for multi-version
    # items that share a wiki page. The per-item record still pairs each
    # item_id with its derived requirements.
    cache_by_page: dict[str, tuple[dict[str, int], str | None, str]] = {}
    pages_fetched = 0
    pages_cached = 0

    for idx, it in enumerate(items, 1):
        name = it.get("name")
        iid = it.get("item_id")
        page_name = it.get("page_name") or name
        if not page_name:
            out.append({
                "name": name, "item_id": iid,
                "requirements": None, "source_text": None,
                "confidence": "low",
                "note": "missing page_name",
            })
            low += 1
            continue

        if page_name in cache_by_page:
            reqs, src, status = cache_by_page[page_name]
        else:
            try:
                wt = wiki_page.fetch_page_wikitext(
                    page_name, WIKI_PAGE_CACHE,
                    use_cache=True, refresh=False,
                )
                if wt is None or not wt.strip():
                    reqs, src, status = None, None, "missing-wikitext"
                    pages_fetched += 1
                else:
                    reqs, src = parse_requirements(wt)
                    status = "ok"
                    # Track whether we hit cache (rough heuristic: cache file existed).
                    pages_cached += 1
            except Exception as e:
                reqs, src, status = None, None, f"error: {e}"
            cache_by_page[page_name] = (reqs, src, status)

        if status == "missing-wikitext":
            confidence = "low"
            note = "wiki page returned empty / does not exist"
        elif status.startswith("error"):
            confidence = "low"
            note = status
        elif reqs is None:
            confidence = "low"
            note = "parse error"
        elif reqs:
            confidence = "high"
            note = None
        else:
            # Empty dict — either explicit no-req or no requirement text found.
            if src:  # NO_REQ_RE matched
                confidence = "high"
                note = "wiki explicitly states no requirements"
            else:
                confidence = "medium"
                note = "no requirement text matched in page"

        rec = {
            "name": name, "item_id": iid,
            "requirements": reqs,
            "source_text": src,
            "confidence": confidence,
        }
        if note:
            rec["note"] = note
        out.append(rec)

        if confidence == "high":
            high += 1
        elif confidence == "medium":
            medium += 1
        else:
            low += 1

        if idx % 500 == 0 or idx == len(items):
            print(f"  parsed {idx:>5}/{len(items)} "
                  f"(high={high} medium={medium} low={low})",
                  file=sys.stderr)

    OUT_JSON.parent.mkdir(parents=True, exist_ok=True)
    OUT_JSON.write_text(json.dumps(out, separators=(",", ":")))

    # Failures file
    FAILURES_TXT.parent.mkdir(parents=True, exist_ok=True)
    with FAILURES_TXT.open("w") as f:
        f.write("# Requirements parse failures (medium + low confidence)\n")
        f.write("# Columns: item_id\\tname\\tconfidence\\tnote\n")
        for rec in out:
            if rec["confidence"] in ("medium", "low"):
                f.write(f"{rec.get('item_id')}\t{rec['name']}\t"
                        f"{rec['confidence']}\t{rec.get('note', '')}\n")

    # Distribution + summary
    skill_dist: dict[str, int] = {}
    no_req = 0
    null_req = 0
    for rec in out:
        r = rec.get("requirements")
        if r is None:
            null_req += 1
        elif r == {}:
            no_req += 1
        else:
            for sk in r:
                skill_dist[sk] = skill_dist.get(sk, 0) + 1

    total = len(out)
    print(file=sys.stderr)
    print(f"=== Wiki Requirements Parser Results ===", file=sys.stderr)
    print(f"Total equipable items: {total:,}", file=sys.stderr)
    print(f"High confidence:   {high:>5} ({high*100//total}%)", file=sys.stderr)
    print(f"Medium confidence: {medium:>5} ({medium*100//total}%)", file=sys.stderr)
    print(f"Low confidence:    {low:>5} ({low*100//total}%)", file=sys.stderr)
    print(file=sys.stderr)
    print(f"Skill distribution (items with that requirement):", file=sys.stderr)
    for sk in sorted(skill_dist, key=lambda x: -skill_dist[x]):
        print(f"  {sk.capitalize():<14} {skill_dist[sk]:>5}", file=sys.stderr)
    print(f"  (No requirements):  {no_req:>5}", file=sys.stderr)
    print(f"  (Null / unknown):   {null_req:>5}", file=sys.stderr)
    print(file=sys.stderr)
    print(f"Wrote {OUT_JSON}", file=sys.stderr)
    print(f"Wrote {FAILURES_TXT}", file=sys.stderr)

    # Cross-reference
    crossref(out)


def crossref(records: list[dict]):
    if not META_PATH.exists():
        print(f"WARN: {META_PATH} not found — skipping cross-reference",
              file=sys.stderr)
        return
    meta = json.loads(META_PATH.read_text())
    # meta[str(iid)] = {... "rq": {"attack": 70}, ...}
    osrsbox_by_id: dict[int, dict] = {}
    for sid, m in meta.items():
        rq = m.get("rq")
        if rq is None:
            continue
        try:
            osrsbox_by_id[int(sid)] = rq
        except (TypeError, ValueError):
            continue

    both_agree = 0
    mismatches: list[tuple[int, str, dict, dict]] = []
    wiki_only = 0
    osrsbox_only = 0

    for rec in records:
        iid = rec.get("item_id")
        if iid is None:
            continue
        wreqs = rec.get("requirements")
        oreqs = osrsbox_by_id.get(iid)
        if wreqs is None:
            if oreqs:
                osrsbox_only += 1
            continue
        if oreqs is None:
            if wreqs:
                wiki_only += 1
            continue
        if wreqs == oreqs:
            both_agree += 1
        else:
            mismatches.append((iid, rec["name"], wreqs, oreqs))

    CROSSREF_MD.parent.mkdir(parents=True, exist_ok=True)
    lines: list[str] = []
    lines.append("# Wiki ⇄ osrsbox requirements cross-reference")
    lines.append("")
    lines.append(f"- Both sources agree: **{both_agree:,}**")
    lines.append(f"- Wiki has data, osrsbox missing: **{wiki_only:,}**")
    lines.append(f"- osrsbox has data, wiki missing: **{osrsbox_only:,}**")
    lines.append(f"- Mismatches: **{len(mismatches):,}**")
    lines.append("")
    if mismatches:
        lines.append("## Mismatches (sample of 60)")
        lines.append("")
        lines.append("| id | name | wiki | osrsbox |")
        lines.append("|---|---|---|---|")
        for iid, name, w, o in mismatches[:60]:
            wj = ", ".join(f"{k}:{v}" for k, v in sorted(w.items()))
            oj = ", ".join(f"{k}:{v}" for k, v in sorted(o.items()))
            lines.append(f"| {iid} | {name} | {wj or '∅'} | {oj or '∅'} |")
    CROSSREF_MD.write_text("\n".join(lines))
    print(f"Wrote {CROSSREF_MD}", file=sys.stderr)
    print(f"  agree={both_agree:,}  wiki-only={wiki_only:,}  "
          f"osrsbox-only={osrsbox_only:,}  mismatches={len(mismatches):,}",
          file=sys.stderr)


if __name__ == "__main__":
    run()
