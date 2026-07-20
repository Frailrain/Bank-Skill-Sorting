"""Brief #91 Phase 1 — scrape OSRS wiki slayer task pages into slayer-tasks.json.

Pipeline:
  1. Enumerate canonical (non-redirect) wiki pages under "Slayer task/".
  2. Fetch each page's wikitext via wiki_page.fetch_page_wikitext (30-day cache).
  3. Structured parse of {{Infobox Slayer}}: task name, slayer/combat reqs,
     other reqs, per-master assignment ranges + weights, and the in-game task
     item id (|id = 4149 for Abyssal demons — the same item id the game's
     slayer task enum resolves to, so the plugin can match VarPlayer
     SLAYER_TASK_CREATURE straight against this field).
  4. LLM pass (same Messages-API plumbing as llm_classifier) normalises the
     free-text strategy prose into structured fields: recommended_style,
     required/protection/recommended items, attributes, cannon + burst
     viability, locations.
  5. Resolve item names to canonical item ids against cache/items-complete.json;
     unresolved names are kept (id=null) and listed in the report.

Outputs:
  out/slayer-tasks.json          — full artifact incl. rationale/unresolved
  out/slayer-tasks-report.md     — review sheet (Cowork-friendly)
  --write also copies a slim version to
  src/main/resources/com/skillbank/slayer-tasks.json

Run standalone (python3 slayer_tasks.py [--write] [--refresh-llm]) or via
scraper.py --slayer-tasks.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import sys
import urllib.parse
import urllib.request
from pathlib import Path

import llm_classifier
from wiki_page import fetch_page_wikitext

SCRIPT_DIR = Path(__file__).resolve().parent
REPO_ROOT = SCRIPT_DIR.parent.parent
PAGE_CACHE = SCRIPT_DIR / "cache" / "wiki-pages"
LLM_CACHE = SCRIPT_DIR / "cache" / "llm-slayer-tasks"
OUT_DIR = SCRIPT_DIR / "out"
RESOURCE_PATH = REPO_ROOT / "src/main/resources/com/skillbank/slayer-tasks.json"

WIKI_API = "https://oldschool.runescape.wiki/api.php"
USER_AGENT = "SkillBankTabs/1.0 (https://github.com/Frailrain/Bank-Skill-Sorting)"

# Brief #91 v3: variant-aware extraction is quality-critical — run it on
# Fable (the flat Sonnet extraction promoted Karuulm-only "Boots of stone"
# to a task-wide requirement).
MODEL = "claude-fable-5"
MAX_TOKENS = 4000
# Wikitext beyond this is drop tables / reward tables — no strategy signal.
WIKITEXT_CAP = 16000

MASTER_KEYS = (
    "turael", "spria", "aya", "mazchna", "vannaka", "chaeldar",
    "konar", "nieve", "steve", "duradel", "kuradal", "krystilia",
)

ATTRIBUTE_VOCAB = (
    "demon", "undead", "dragon", "fiery", "kalphite", "leafy",
    "spectral", "vampyre", "insect", "aquatic", "wilderness-only",
    "cave-dweller",
)

SYSTEM_PROMPT = """You are an OSRS slayer-task analyst for a RuneLite bank-organisation plugin. \
Given the wikitext of one "Slayer task/<Monster>" page, extract a machine-readable loadout \
recommendation. Respond with STRICT JSON only — no markdown fences, no prose.

Schema:
{
  "recommended_style": "melee" | "range" | "mage",   // the single style the page recommends as most effective for a typical task; if genuinely no preference, pick the most commonly used
  "protection_prayer": "melee" | "missiles" | "magic" | "none" | "varies",
  "required_items": ["..."],       // items REQUIRED to complete, damage, or safely attempt the task: finishing items (Rock hammer, Ice cooler, Bag of salt, Fungicide spray), light sources, mandatory quest items. Empty list if none.
  "protection_items": ["..."],     // sensory/special protection equipment the task demands (Earmuffs, Facemask, Nose peg, Mirror shield, Witchwood icon, Spiny helmet, Insulated boots, Reinforced goggles). Do NOT list slayer helmet here; the plugin adds it as the universal cover. Empty list if none.
  "recommended_items": ["..."],    // notable effectiveness boosters the page calls out: task-specific uniques (Arclight, Dragon hunter lance, Leaf-bladed battleaxe, Salve amulet(ei), Broad bolts), antifire/antivenom potions, Expeditious bracelet, Dinh's bulwark, Goading potion, etc. Max 12, most impactful first.
  "attributes": ["..."],           // subset of: demon, undead, dragon, fiery, kalphite, leafy, spectral, vampyre, insect, aquatic, wilderness-only, cave-dweller
  "burst_viable": true|false,      // does the page indicate AoE/burst/barrage (or chinning) is an effective strategy?
  "cannon_viable": true|false,     // can a dwarf multicannon be used effectively somewhere on this task?
  "locations": [{"name": "...", "cannon": true|false|null, "multicombat": true|false|null}],  // task locations named on the page; cannon/multicombat only when the page states it, else null
  "notes": "...",                  // <= 200 chars of anything loadout-critical not captured above
  "variants": [                    // EVERY distinct monster variant this task page covers (e.g. Wyrms page → Wyrm AND Lava strykewyrm). At least one entry.
    {
      "name": "...",               // the variant's in-game monster name
      "aliases": ["..."],          // other names players/task lists use for it
      "elemental_weakness": "earth"|"water"|"fire"|"air"|null,
      "required_items": ["..."],   // required for THIS VARIANT at every location
      "protection_items": ["..."],
      "locations": [               // where THIS variant is fought
        {
          "name": "...",
          "cannon": true|false|null,
          "required_items": ["..."],  // required ONLY at this location (e.g. Boots of stone at Karuulm)
          "notes": null|"..."         // <= 120 chars, location-specific tactics
        }
      ]
    }
  ]
}

Rules:
- Item names must be exact OSRS item names as they would appear in the bank \
(e.g. "Rock hammer", "Mirror shield", "Salve amulet(ei)", "Antidote++(4)"). \
Name potions as the (4)-dose variant.
- required_items vs recommended_items: required = the task is impossible or \
reckless without it; recommended = meaningfully better with it.
- Do not invent items or locations not supported by the page text.
- locations: 8 entries max, most task-relevant first.
- VARIANTS ARE THE TRUTH SOURCE for requirements. Read the ENTIRE page and \
scope every requirement to exactly the variant and location the page states. \
NEVER promote a location-specific requirement to the variant or task level: \
if boots of stone are needed because of the Karuulm dungeon floor, they \
belong in the Karuulm location's required_items ONLY — a player fighting the \
same task elsewhere must not be told to bring them. Likewise per-variant \
facts (a weakness, a mechanic) must not leak onto sibling variants. The \
top-level required_items field is legacy: put only requirements that apply \
to EVERY variant at EVERY location there."""


# ── Page enumeration ─────────────────────────────────────────────────────────

def list_task_pages() -> list[str]:
    """Canonical (non-redirect) 'Slayer task/…' page titles."""
    titles: list[str] = []
    cont: dict[str, str] = {}
    while True:
        params = {
            "action": "query", "list": "allpages",
            "apprefix": "Slayer task/", "aplimit": "500",
            "apfilterredir": "nonredirects",
            "format": "json", "formatversion": "2", **cont,
        }
        url = f"{WIKI_API}?{urllib.parse.urlencode(params)}"
        req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
        with urllib.request.urlopen(req, timeout=30) as resp:
            data = json.loads(resp.read())
        titles += [p["title"] for p in data["query"]["allpages"]]
        if "continue" in data:
            cont = {"apcontinue": data["continue"]["apcontinue"]}
        else:
            break
    # The bare "Slayer task/" root (if present) isn't a task.
    return [t for t in titles if t != "Slayer task/"]


# ── Infobox parsing ──────────────────────────────────────────────────────────

def _template_block(wikitext: str, name: str) -> str | None:
    """Return the full {{name ...}} block with balanced braces, or None."""
    start = wikitext.find("{{" + name)
    if start == -1:
        return None
    depth = 0
    i = start
    while i < len(wikitext) - 1:
        pair = wikitext[i:i + 2]
        if pair == "{{":
            depth += 1
            i += 2
        elif pair == "}}":
            depth -= 1
            i += 2
            if depth == 0:
                return wikitext[start:i]
        else:
            i += 1
    return None


def _split_params(block: str) -> dict[str, str]:
    """Split a template block into |key = value params (top-level pipes only)."""
    inner = block[2:-2]
    parts: list[str] = []
    depth = 0
    cur: list[str] = []
    for ch in inner:
        if ch == "{" or ch == "[":
            depth += 1
        elif ch == "}" or ch == "]":
            depth -= 1
        if ch == "|" and depth == 0:
            parts.append("".join(cur))
            cur = []
        else:
            cur.append(ch)
    parts.append("".join(cur))
    params: dict[str, str] = {}
    for p in parts[1:]:  # parts[0] is the template name
        if "=" not in p:
            continue
        k, v = p.split("=", 1)
        params[k.strip().lower()] = v.strip()
    return params


def _clean_wiki_value(v: str) -> str:
    """Strip [[links]], {{SCP|..}} decorations, refs, files from a value."""
    v = re.sub(r"\{\{Cite\w*[^}]*\}\}", "", v, flags=re.I | re.S)
    v = re.sub(r"\{\{SCP\|([^|}]+)\|([^|}]+)[^}]*\}\}", r"\1 \2", v)
    v = re.sub(r"\{\{SCP\|([^|}]+)[^}]*\}\}", r"\1", v)
    v = re.sub(r"\[\[File:[^\]]*\]\]", "", v)
    v = re.sub(r"\[\[(?:[^|\]]*\|)?([^\]]+)\]\]", r"\1", v)
    v = re.sub(r"\{\{[^}]*\}\}", "", v)
    v = re.sub(r"<ref[^>]*>.*?</ref>", "", v, flags=re.S)
    v = re.sub(r"<[^>]+>", "", v)
    return re.sub(r"\s+", " ", v).strip()


_RANGE_RE = re.compile(
    r"(\d+)\s*[-–]\s*(\d+)"                 # base range
    r"(?:\s*\((\d+)\s*[-–]\s*(\d+)\))?",    # extended range
    re.I | re.S,
)
_WEIGHT_RE = re.compile(r"Weighting\s+(\d+)", re.I)


def parse_infobox(wikitext: str) -> dict | None:
    block = _template_block(wikitext, "Infobox Slayer")
    if block is None:
        return None
    p = _split_params(block)

    def _level(key: str) -> int:
        m = re.search(r"(\d+)", p.get(key, ""))
        return int(m.group(1)) if m else 0

    masters: dict[str, dict] = {}
    for mk in MASTER_KEYS:
        raw = p.get(mk)
        if not raw:
            continue
        raw_clean = _clean_wiki_value(raw)
        m = _RANGE_RE.search(raw_clean)
        entry: dict = {"raw": raw_clean}
        if m:
            entry["amount"] = [int(m.group(1)), int(m.group(2))]
            if m.group(3):
                entry["extended"] = [int(m.group(3)), int(m.group(4))]
        w = _WEIGHT_RE.search(raw_clean)
        if w:
            entry["weight"] = int(w.group(1))
        masters[mk] = entry

    task_id = None
    m = re.search(r"(\d+)", p.get("id", ""))
    if m:
        task_id = int(m.group(1))

    return {
        "name": _clean_wiki_value(p.get("name", "")),
        "task_item_id": task_id,
        "slayer_req": _level("skillreq"),
        "combat_req": _level("combatreq"),
        "other_req": _clean_wiki_value(p.get("otherreq", "")) or None,
        "masters": masters,
    }


# ── Recommended-equipment template parsing (deterministic, no LLM) ─────────

_PLINK_RE = re.compile(r'\{\{[Pp]link\|([^|}]+)')
_EQUIP_PARAM_RE = re.compile(r'^\|\s*([a-z]+)(\d+)\s*=\s*(.*)$', re.M)
_STYLE_RE = re.compile(r'\|\s*style\s*=\s*(\w+)')
# "…[[wyrm]]s' 50% earth weakness…"
_WEAKNESS_RE = re.compile(
    r"\[\[([^\]|]+)(?:\|[^\]]*)?\]\]s?'?s?\s*(\d+)%\s*(earth|water|fire|air|wind)\s+weakness",
    re.I)
# "…50% weakness to [[earth]]…"
_WEAKNESS_RE2 = re.compile(
    r"(\d+)%\s*(?:elemental\s+)?weakness to\s*\[\[(earth|water|fire|air|wind)",
    re.I)

# Slots the runtime loadout builder understands, in display order.
EQUIPMENT_SLOTS = (
    "weapon", "special", "head", "cape", "neck", "body", "legs",
    "hands", "feet", "shield", "ammo", "ring",
)


def parse_recommended_equipment(wikitext: str) -> tuple[dict, list[str]]:
    """Parse every {{Recommended equipment}} template on a page into
    {style: {slot: [rank1_alternatives, rank2_alternatives, ...]}} where each
    rank is a list of item names ("A / B" alternatives). Returns
    (styles, style_order). Fully deterministic — no LLM involved."""
    styles: dict = {}
    order: list[str] = []
    i = 0
    while True:
        start = wikitext.find("{{Recommended equipment", i)
        if start == -1:
            break
        depth, j = 0, start
        while j < len(wikitext) - 1:
            pair = wikitext[j:j + 2]
            if pair == "{{":
                depth += 1
                j += 2
            elif pair == "}}":
                depth -= 1
                j += 2
                if depth == 0:
                    break
            else:
                j += 1
        block = wikitext[start:j]
        i = j
        sm = _STYLE_RE.search(block)
        style = sm.group(1).lower() if sm else "unknown"
        # normalize wiki style names to runtime ids
        style = {"ranged": "range", "magic": "mage"}.get(style, style)
        slots: dict[str, dict[int, list[str]]] = {}
        for pm in _EQUIP_PARAM_RE.finditer(block):
            slot, rank, val = pm.group(1), int(pm.group(2)), pm.group(3)
            if slot not in EQUIPMENT_SLOTS:
                continue
            names = [n.strip() for n in _PLINK_RE.findall(val)]
            if names:
                slots.setdefault(slot, {})[rank] = names
        if slots and style not in styles:
            styles[style] = {
                slot: [ranks[r] for r in sorted(ranks)]
                for slot, ranks in slots.items()
            }
            order.append(style)
    return styles, order


def parse_elemental_weaknesses(wikitext: str) -> list[dict]:
    out, seen = [], set()
    for m in _WEAKNESS_RE.finditer(wikitext):
        key = (m.group(1).lower(), m.group(3).lower())
        if key not in seen:
            seen.add(key)
            out.append({"monster": m.group(1), "element": m.group(3).lower(),
                        "percent": int(m.group(2))})
    for m in _WEAKNESS_RE2.finditer(wikitext):
        key = ("", m.group(2).lower())
        if key not in seen:
            seen.add(key)
            out.append({"monster": None, "element": m.group(2).lower(),
                        "percent": int(m.group(1))})
    return out


def _equipment_source_pages(task_page: str, task_name: str) -> list[str]:
    """Pages to try, in order, for {{Recommended equipment}} templates: the
    task page itself, then the monster's page and its /Strategies subpage
    (singular and plural forms)."""
    base = task_name.strip()
    singular = base[:-1] if base.endswith("s") else base
    candidates = [task_page]
    for n in (base, singular):
        candidates += [n, n + "/Strategies"]
    seen, out = set(), []
    for c in candidates:
        if c and c not in seen:
            seen.add(c)
            out.append(c)
    return out


# ── LLM extraction ───────────────────────────────────────────────────────────

_DROP_SECTION_RE = re.compile(
    r"^==\s*(Drops|Loot|Related slayer shop options|Changes|Gallery|References|Trivia)\s*==",
    re.I | re.M,
)


def trim_for_llm(wikitext: str) -> str:
    """Cut drop/reward/trivia sections; cap length."""
    m = _DROP_SECTION_RE.search(wikitext)
    if m:
        wikitext = wikitext[:m.start()]
    return wikitext[:WIKITEXT_CAP]


def _llm_key(model: str, page: str, trimmed: str) -> str:
    payload = json.dumps({
        "model": model,
        "page": page,
        "wt_hash": hashlib.sha1(trimmed.encode("utf-8")).hexdigest()[:12],
        "prompt_hash": hashlib.sha1(SYSTEM_PROMPT.encode("utf-8")).hexdigest()[:12],
    }, sort_keys=True)
    return hashlib.sha1(payload.encode("utf-8")).hexdigest()


def extract_strategy(
    page: str,
    wikitext: str,
    api_key: str,
    *,
    refresh: bool = False,
) -> dict:
    LLM_CACHE.mkdir(parents=True, exist_ok=True)
    trimmed = trim_for_llm(wikitext)
    key = _llm_key(MODEL, page, trimmed)
    cache_file = LLM_CACHE / f"{key}.json"
    if not refresh and cache_file.exists():
        cached = json.loads(cache_file.read_text())
        cached["_cached"] = True
        return cached

    user = f"Page: {page}\n\nWikitext:\n{trimmed}"
    # Reuse llm_classifier's Messages-API call (retries, prompt caching),
    # but with this module's token budget.
    orig_max = llm_classifier.MAX_TOKENS
    llm_classifier.MAX_TOKENS = MAX_TOKENS
    try:
        resp = llm_classifier._api_call(MODEL, SYSTEM_PROMPT, user, api_key)
    finally:
        llm_classifier.MAX_TOKENS = orig_max

    content = resp.get("content") or []
    text = "".join(b.get("text", "") for b in content if b.get("type") == "text")
    text = text.strip()
    if text.startswith("```"):
        text = re.sub(r"^```(?:json)?\s*|\s*```$", "", text, flags=re.S)
    parsed = json.loads(text)

    usage = resp.get("usage") or {}
    parsed["_usage"] = {
        "input_tokens": usage.get("input_tokens"),
        "cache_creation_input_tokens": usage.get("cache_creation_input_tokens"),
        "cache_read_input_tokens": usage.get("cache_read_input_tokens"),
        "output_tokens": usage.get("output_tokens"),
    }
    cache_file.write_text(json.dumps(parsed, indent=2))
    parsed["_cached"] = False
    return parsed


# ── Item name resolution ─────────────────────────────────────────────────────

# Wiki-prose shorthand → canonical bank item name(s). A value of None drops
# the name (category placeholders like "Zamorak item" aren't bankable items).
# Lowercase keys; matched after a direct canonical lookup fails.
NAME_ALIASES: dict[str, str | list[str] | None] = {
    "dwarf multicannon": ["Dwarf cannon set", "Cannonball"],
    "steel cannonball": "Cannonball",
    "goading potion": "Goading potion(4)",
    "antifire(4)": "Antifire potion(4)",
    "antifire shield": "Anti-dragon shield",
    "antidfire shield": "Anti-dragon shield",  # wiki typo
    "salve amulet(e)": "Salve amulet (e)",
    "slayer ring": "Slayer ring (8)",
    "slayer ring(8)": "Slayer ring (8)",
    "slayer staff": "Slayer's staff",
    "fire staff": "Staff of fire",
    "guthans set": "Guthan's armour set",
    "guthan's set": "Guthan's armour set",
    "dragonhide body": "Green d'hide body",
    "dragonhide chaps": "Green d'hide chaps",
    "brutal arrow": "Rune brutal",
    "super set(4)": "Super combat potion(4)",
    "shayzien legs (5)": "Shayzien greaves (5)",
    "shayzien gauntlets (5)": "Shayzien gloves (5)",
    "hallowed flail": "Blisterwood flail",
    "rat bone weapons": "Bone mace",
    "vyre noble clothing": ["Vyre noble top", "Vyre noble legs", "Vyre noble shoes"],
    "fungicide spray": "Fungicide spray 10",
    "wind bolt": None,       # spell, not an item
    "sunspear": None,        # RS3-only
    "zamorak item": None, "saradomin item": None, "bandos item": None,
    "armadyl item": None, "zaros item": None,  # god-protection categories
}


# Post-cache releases (Varlamore III / Sailing era) absent from
# items-complete.json; ids confirmed from the wiki item infoboxes.
EXTRA_ITEM_IDS: dict[str, int] = {
    "antler guard": 31081,
    "tortugan shield": 31398,
}


def build_canonical_name_map() -> dict[str, int]:
    items = json.loads((SCRIPT_DIR / "cache" / "items-complete.json").read_text())
    canonical: dict[str, int] = {}
    for iid in sorted(items, key=int):
        it = items[iid]
        if it.get("noted") or it.get("placeholder") or it.get("duplicate"):
            continue
        canonical.setdefault(it["name"].lower(), int(iid))
    for name, iid in EXTRA_ITEM_IDS.items():
        canonical.setdefault(name, iid)
    return canonical


def resolve_items(names: list[str], canon: dict[str, int]) -> tuple[list[dict], list[str]]:
    resolved: list[dict] = []
    unknown: list[str] = []
    for n in names or []:
        if not isinstance(n, str) or not n.strip():
            continue
        n = n.strip()
        expanded: list[str]
        if n.lower() in canon:
            expanded = [n]
        elif n.lower() in NAME_ALIASES:
            alias = NAME_ALIASES[n.lower()]
            if alias is None:
                continue
            expanded = alias if isinstance(alias, list) else [alias]
        else:
            expanded = [n]
        for name in expanded:
            iid = canon.get(name.lower())
            resolved.append({"name": name, "id": iid})
            if iid is None:
                unknown.append(name)
    return resolved, unknown


# ── Main ─────────────────────────────────────────────────────────────────────

def run(*, write_resource: bool = False, refresh_llm: bool = False,
        limit: int | None = None) -> None:
    api_key = os.environ.get("ANTHROPIC_API_KEY")
    if not api_key:
        print("ERROR: ANTHROPIC_API_KEY not set (tools/skillbank-data/.env)", file=sys.stderr)
        sys.exit(2)

    canon = build_canonical_name_map()
    pages = list_task_pages()
    if limit:
        pages = pages[:limit]
    print(f"{len(pages)} slayer task pages", file=sys.stderr)

    tasks: list[dict] = []
    all_unknown: dict[str, list[str]] = {}
    skipped: list[str] = []
    usage_in = usage_out = 0

    for i, page in enumerate(pages, 1):
        wt = fetch_page_wikitext(page, PAGE_CACHE)
        if not wt:
            skipped.append(f"{page} (missing page)")
            continue
        info = parse_infobox(wt)
        if info is None or not info["name"]:
            skipped.append(f"{page} (no Infobox Slayer)")
            continue

        strat = extract_strategy(page, wt, api_key, refresh=refresh_llm)
        cached = strat.pop("_cached", False)
        u = strat.pop("_usage", {}) or {}
        usage_in += (u.get("input_tokens") or 0) + (u.get("cache_creation_input_tokens") or 0)
        usage_out += u.get("output_tokens") or 0

        unknown: list[str] = []
        for field in ("required_items", "protection_items", "recommended_items"):
            resolved, unk = resolve_items(strat.get(field) or [], canon)
            strat[field] = resolved
            unknown += unk

        # v3: resolve item names inside the variant records too.
        for variant in strat.get("variants") or []:
            if not isinstance(variant, dict):
                continue
            for field in ("required_items", "protection_items"):
                resolved, unk = resolve_items(variant.get(field) or [], canon)
                variant[field] = resolved
                unknown += unk
            for loc in variant.get("locations") or []:
                if not isinstance(loc, dict):
                    continue
                resolved, unk = resolve_items(loc.get("required_items") or [], canon)
                loc["required_items"] = resolved
                unknown += unk

        # Deterministic per-style, slot-ranked wiki loadouts + elemental
        # weaknesses. Task page first, then the monster page / /Strategies
        # subpage (where most task pages keep their equipment templates).
        styles: dict = {}
        style_order: list[str] = []
        weaknesses: list[dict] = []
        equip_source = None
        for src_page in _equipment_source_pages(page, info["name"]):
            src_wt = wt if src_page == page else fetch_page_wikitext(src_page, PAGE_CACHE)
            if not src_wt:
                continue
            if not weaknesses:
                weaknesses = parse_elemental_weaknesses(src_wt)
            if not styles:
                styles, style_order = parse_recommended_equipment(src_wt)
                if styles:
                    equip_source = src_page
            if styles and weaknesses:
                break
        resolved_styles: dict = {}
        for st, slots in styles.items():
            resolved_styles[st] = {}
            for slot, ranks in slots.items():
                rank_refs = []
                for names in ranks:
                    refs, unk = resolve_items(names, canon)
                    unknown += unk
                    rank_refs.append(refs)
                resolved_styles[st][slot] = rank_refs

        if unknown:
            all_unknown[info["name"]] = sorted(set(unknown))

        attrs = [a for a in (strat.get("attributes") or []) if a in ATTRIBUTE_VOCAB]
        strat["attributes"] = attrs

        tasks.append({"page": page, **info, **strat,
                      "styles": resolved_styles,
                      "style_order": style_order,
                      "elemental_weaknesses": weaknesses,
                      "equipment_source": equip_source})
        print(f"  [{i}/{len(pages)}] {info['name']}{' (cached)' if cached else ''}",
              file=sys.stderr)

    tasks.sort(key=lambda t: t["name"].lower())

    OUT_DIR.mkdir(exist_ok=True)
    full = {
        "model": MODEL,
        "task_count": len(tasks),
        "tasks": tasks,
    }
    (OUT_DIR / "slayer-tasks.json").write_text(json.dumps(full, indent=1))
    print(f"Wrote {OUT_DIR / 'slayer-tasks.json'} ({len(tasks)} tasks)", file=sys.stderr)

    # Review sheet.
    lines = ["# Slayer task scrape — review sheet", ""]
    if skipped:
        lines += ["## Skipped pages", ""] + [f"- {s}" for s in skipped] + [""]
    if all_unknown:
        lines += ["## Unresolved item names (fix in wiki text or add aliases)", ""]
        for task, names in sorted(all_unknown.items()):
            lines.append(f"- **{task}**: {', '.join(names)}")
        lines.append("")
    lines += ["## Tasks", "",
              "| Task | id | Slay | Style | Pray | Required | Protection | Cannon | Burst |",
              "|---|---|---|---|---|---|---|---|---|"]
    for t in tasks:
        req = ", ".join(x["name"] for x in t.get("required_items") or []) or "—"
        prot = ", ".join(x["name"] for x in t.get("protection_items") or []) or "—"
        lines.append(
            f"| {t['name']} | {t.get('task_item_id') or '?'} | {t['slayer_req']} "
            f"| {t.get('recommended_style')} | {t.get('protection_prayer')} "
            f"| {req} | {prot} "
            f"| {'Y' if t.get('cannon_viable') else 'n'} "
            f"| {'Y' if t.get('burst_viable') else 'n'} |"
        )
    (OUT_DIR / "slayer-tasks-report.md").write_text("\n".join(lines) + "\n")
    print(f"Wrote {OUT_DIR / 'slayer-tasks-report.md'}", file=sys.stderr)

    in_cost = usage_in * 3 / 1_000_000
    out_cost = usage_out * 15 / 1_000_000
    print(f"LLM usage: {usage_in:,} in / {usage_out:,} out ≈ ${in_cost + out_cost:.2f}",
          file=sys.stderr)

    if write_resource:
        slim_tasks = []
        for t in tasks:
            s = {k: v for k, v in t.items() if k not in ("page", "notes", "equipment_source")}
            for f in ("required_items", "protection_items", "recommended_items"):
                s[f] = [x for x in s.get(f) or []]
            slim_tasks.append(s)
        RESOURCE_PATH.write_text(json.dumps({"tasks": slim_tasks}, separators=(",", ":")))
        print(f"Wrote {RESOURCE_PATH}", file=sys.stderr)


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--write", action="store_true",
                    help="also write src/main/resources/com/skillbank/slayer-tasks.json")
    ap.add_argument("--refresh-llm", action="store_true",
                    help="bypass the per-page LLM response cache")
    ap.add_argument("--limit", type=int, default=None,
                    help="only process the first N pages (pilot run)")
    args = ap.parse_args()

    # Standalone: load .env the same way scraper.py does.
    env_file = SCRIPT_DIR / ".env"
    if env_file.exists():
        for line in env_file.read_text().splitlines():
            line = line.strip()
            if line and not line.startswith("#") and "=" in line:
                k, v = line.split("=", 1)
                os.environ.setdefault(k.strip(), v.strip())

    run(write_resource=args.write, refresh_llm=args.refresh_llm, limit=args.limit)


if __name__ == "__main__":
    main()
