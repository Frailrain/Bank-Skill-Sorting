"""LLM-based item-to-tab classifier using the Anthropic Messages API.

Replaces (eventually) the heuristic predicate stack in mapping.py. Each
item is presented with its osrsbox/wiki metadata + trimmed wikitext, plus
the 21 tab definitions, and the model returns a JSON object specifying a
primary tab + optional cross-tag tabs.

Design notes:
- Uses urllib (no `anthropic` SDK dep needed). The Messages API is a thin
  HTTPS POST — keeping this dep-free means the scraper still runs in a
  bare Python install.
- API key read from os.environ['ANTHROPIC_API_KEY'] (loaded by env.py from
  tools/skillbank-data/.env which is gitignored).
- Per-item responses cached to cache/llm-classifications/<sha1>.json keyed
  by (model, item_id, name, examine, slot, wikitext_hash). Reruns are free
  unless the cache is purged or the input materially changes.
- The user kept existing force_include/force_exclude from mapping.py as
  hard overrides (applied by the caller, not here).
"""
from __future__ import annotations

import hashlib
import json
import os
import re
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

API_URL = "https://api.anthropic.com/v1/messages"
DEFAULT_MODEL = "claude-sonnet-4-6"
ANTHROPIC_VERSION = "2023-06-01"
MAX_TOKENS = 512
TIMEOUT = 60

# Canonical tab names + descriptions (mirrors SkillBankConfig).
TAB_DEFS: list[tuple[str, str]] = [
    ("melee",             "Weapons, armour, combat potions, HP food. Anything used primarily for melee combat including dragon/barrows/godwars/tbow-equivalent melee gear, defenders, slayer helms in melee context."),
    ("range",             "Bows, crossbows, ammunition (arrows/bolts/darts), d'hide armour, blowpipe, ranged void, ava's devices, ranging potion."),
    ("mage",              "Runes, staves, wands, magic armour (mystic/ahrim's/ancestral/virtus), rune pouch, magic potions, god capes, occult, tormented bracelet."),
    ("prayer",            "Bones, ashes, prayer potions, holy wrench, dragonbone necklace, prayer cape. Things that train prayer."),
    ("cooking",           "Cooked/burnt food, raw ingredients used in cooking (flour, eggs, milk, sugar), cook's outfit, cooking gauntlets, cooking cape."),
    ("wc_fletching",      "Logs, axes, bows (unstrung & strung), bowstring, bolts/darts (fletching products), pyrolings, forestry items, woodcutting/fletching outfits, lumberjack outfit."),
    ("fishing",           "Raw fish, fishing rods/nets/cages/harpoons, bait, feathers (as fishing bait), angler outfit, fish barrel, spirit anglers, heron."),
    ("firemaking",        "Tinderbox, pyromancer outfit, firelighters, bruma torch, bonfire/forester's items, firemaking cape."),
    ("crafting",          "Gems (cut + uncut), moulds, leather/tanned hide pre-craft, glass, ball of wool, threads, needle, chisel, crafting cape, dragonhide bodies/chaps/vamb (post-craft)."),
    ("mining_smithing",   "Ores, bars, pickaxes, prospector outfit, smiths/foundry outfit, hammer, mining/smithing cape, gold/silver bars when used as material, varrock armour."),
    ("herblore",          "Herbs (clean + grimy), vials, secondary ingredients, all potion doses (1/2/3/4), pestle and mortar, herblore cape."),
    ("agility_thieving",  "Graceful outfit, marks of grace, rogue's outfit (top/trousers/mask/gloves/boots), blackjacks, lockpicks, agility/thieving capes."),
    ("slayer",            "Slayer task gear (broad bolts, broad arrows, broad darts), slayer helms, enchanted gem, eternal/pegasian/primordial rings, slayer gloves, slayer ring, salve amulet."),
    ("farming",           "Seeds (all kinds), saplings, secateurs, watering can, rake, spade, compost, farmer's outfit, farming cape."),
    ("runecraft",         "Pure/Daeyalt/Guardian essence, talismans, tiaras, pouches (small/medium/large/giant/colossal), wicked hood, runecraft cape, abyssal lantern."),
    ("hunter",            "Box traps, bird snares, butterfly nets, impling jars, camouflage outfits, larupia/graahk/kyatt hunter gear, salamanders, hunter cape."),
    ("construction",      "Planks (all kinds), nails, saw, hammer (when carpentry), limestone, soft clay, marble blocks, gold leaf, construction cape."),
    ("misc",              "Teleports (tabs, jewellery, scrolls), keys, clue scrolls/caskets, storage bags (looting bag/seed box/coal bag/herb sack/etc.), coins, utility (chisel, pestle, knife) when not skill-specific."),
    ("quests",            "Quest-only reward gear, diary-locked gear (graceful colour kits aside), minigame gear (LMS/CW), boss uniques (DKS rings, Zulrah scales). If primarily obtained through a quest reward."),
    ("sailing",           "Sailing skill content — newer Sailing skill: navigation tools, ship parts, sailing-specific fish, sailing cape, dock items."),
    ("cosmetics",         "Pure cosmetic outfits (Treasure Trail third-age, holiday rares, partyhats, h'ween masks), ornament kits, purely-visual items with no stat or training utility."),
]
VALID_TABS = {n for n, _ in TAB_DEFS}


def _key(model: str, it: dict, wikitext: str) -> str:
    payload = json.dumps({
        "model": model,
        "id": it.get("id"),
        "name": it.get("name"),
        "examine": it.get("examine"),
        "slot": (it.get("equipment") or {}).get("slot"),
        "weapon_type": (it.get("weapon") or {}).get("weapon_type"),
        "wt_hash": hashlib.sha1((wikitext or "").encode("utf-8")).hexdigest()[:12],
        "prompt_hash": hashlib.sha1(
            _build_system_prompt().encode("utf-8"),
        ).hexdigest()[:12],
    }, sort_keys=True)
    return hashlib.sha1(payload.encode("utf-8")).hexdigest()


def _build_system_prompt() -> str:
    tab_lines = "\n".join(f"- **{n}**: {d}" for n, d in TAB_DEFS)
    return f"""You are an OSRS item classifier for a RuneLite bank-sorting plugin.

Given an item's name, metadata, and wiki page content, decide:
1. **primary_tab** — the single best tab for this item (the tab a player would expect to find it in when sorting their bank). Must be one of the 21 listed below.
2. **cross_tags** — additional tabs the item legitimately belongs in (e.g. a slayer helm goes primary=melee, cross=[slayer]; a salve amulet goes primary=slayer, cross=[melee, mage]). Empty list if there are no good cross-tags.
3. **rationale** — one sentence explaining the choice (used for human review).

# Tabs

{tab_lines}

# Guidelines

- Prefer the **training/use context** over the source. A dragon mace is melee (used for combat), not slayer (even if slayer-task-locked).
- For PVM gear at the top of the meta (Torva/Masori/Ancestral/Bandos/Armadyl etc.), assign to the combat style they are USED FOR, not the boss that drops them. Torva is melee, Masori is range, Ancestral is mage, Bandos is melee, Armadyl is range.
- Quest-reward gear should go to **quests** ONLY if it's primarily defined by being a quest reward with no broader training/combat utility (e.g. quest cape, quest-only cosmetics). Quest-locked gear used in normal training/combat (e.g. Barrows gloves, Recipe-for-Disaster gloves, Verac's armor) goes to its functional tab with cross-tag to quests if you want.
- Cosmetics tab is for **purely visual** items — third-age sets (full sets, not the offensive d'hide which goes to range), partyhats, h'ween masks, holiday rares. If an item has actual stats and is competitive, prefer its stat tab.
- **Ornament-kitted variants** (item names ending in `(or)`, `(or 1)`, `(or 2)`, `(or 3)`, `(cr)`, `(t)`, `(g)`, etc.) keep the **same stats** as their base item — classify them in their **base item's tab** (e.g. Berserker necklace (or) → `melee`), with `cosmetics` as a cross-tag. Do NOT make ornament-kitted variants primary cosmetics.
- **LMS / Last Man Standing variants**: items obtainable only inside the Last Man Standing minigame (often named identically to their stat counterpart but with a separate item ID) should be classified primary=`misc` cross=[functional stat tab], because they cannot be used outside the minigame. Apply the same rule to other minigame-exclusive item variants (Castle Wars-locked variants etc.).
- Slayer cross-tags: items that are not slayer-mandatory but are part of slayer task loadouts (broad ammo, slayer helms, salve amulet, ferocious gloves) should appear in slayer alongside their primary functional tab.
- Misc is the fallback for utility items that don't fit a skill (teleport tabs, keys, clue items, storage, minigame-exclusive variants). Don't dump items in misc if they have a clear skill tab AND can be used outside any minigame.
- Cosmetic/holiday items with no combat/training role go to **cosmetics** alone, NOT misc.
- **Pets** (boss/skilling pets like Rocky, Heron, Beaver, Olmlet, etc.) go to `cosmetics` since they have no in-bank training/combat utility.

# Output format

Respond with ONLY a JSON object, no preamble or trailing text:

{{
  "primary_tab": "<one of the 21 tab names>",
  "cross_tags": ["<zero or more other tab names>"],
  "rationale": "<one sentence>"
}}
"""


def _build_user_message(it: dict, wikitext: str | None) -> str:
    eq = it.get("equipment") or {}
    wp = it.get("weapon") or {}
    parts = [
        f"Item ID: {it.get('id')}",
        f"Name: {it.get('name')}",
        f"Examine: {it.get('examine') or '(none)'}",
        f"Members: {bool(it.get('members'))}",
        f"Equipable: {bool(it.get('equipable'))} (weapon={bool(it.get('equipable_weapon'))})",
        f"Quest item: {bool(it.get('quest_item'))}",
    ]
    if eq.get("slot"):
        parts.append(f"Slot: {eq['slot']}")
    if wp.get("weapon_type"):
        parts.append(f"Weapon type: {wp['weapon_type']}")
    stat_keys = [
        "attack_stab", "attack_slash", "attack_crush", "attack_magic", "attack_ranged",
        "defence_stab", "defence_slash", "defence_crush", "defence_magic", "defence_ranged",
        "melee_strength", "ranged_strength", "magic_damage", "prayer",
    ]
    stats = {k: eq[k] for k in stat_keys if eq.get(k)}
    if stats:
        parts.append("Equipment stats: " + ", ".join(f"{k}={v}" for k, v in stats.items()))

    wt = (wikitext or "").strip()
    if wt:
        parts.append("\n--- Wiki page content (excerpt) ---")
        parts.append(wt)
    else:
        parts.append("\n--- Wiki page: (no content available) ---")

    return "\n".join(parts)


_JSON_BLOCK_RE = re.compile(r"\{.*\}", re.DOTALL)


def _parse_response(text: str) -> dict:
    """Extract the JSON object from the response, tolerating leading fences."""
    text = text.strip()
    if text.startswith("```"):
        text = re.sub(r"^```[a-zA-Z]*\n?", "", text)
        text = re.sub(r"\n?```\s*$", "", text)
    m = _JSON_BLOCK_RE.search(text)
    if not m:
        raise ValueError(f"no JSON object found in response: {text!r}")
    return json.loads(m.group(0))


def _validate(parsed: dict, item_name: str) -> dict:
    primary = parsed.get("primary_tab")
    cross = parsed.get("cross_tags") or []
    rationale = parsed.get("rationale") or ""
    if primary not in VALID_TABS:
        raise ValueError(f"{item_name}: invalid primary_tab {primary!r}")
    if not isinstance(cross, list):
        raise ValueError(f"{item_name}: cross_tags must be a list, got {type(cross).__name__}")
    cross = [c for c in cross if isinstance(c, str)]
    bad = [c for c in cross if c not in VALID_TABS]
    if bad:
        raise ValueError(f"{item_name}: invalid cross_tags {bad}")
    # primary should never duplicate into cross
    cross = [c for c in cross if c != primary]
    return {"primary_tab": primary, "cross_tags": cross, "rationale": rationale}


def _api_call(model: str, system: str, user: str, api_key: str) -> dict:
    body = json.dumps({
        "model": model,
        "max_tokens": MAX_TOKENS,
        "temperature": 0,
        "system": [
            {"type": "text", "text": system, "cache_control": {"type": "ephemeral"}},
        ],
        "messages": [
            {"role": "user", "content": user},
        ],
    }).encode("utf-8")
    req = urllib.request.Request(
        API_URL,
        data=body,
        headers={
            "content-type": "application/json",
            "x-api-key": api_key,
            "anthropic-version": ANTHROPIC_VERSION,
        },
        method="POST",
    )

    backoff = 5
    last_err: Exception | None = None
    for attempt in range(5):
        try:
            with urllib.request.urlopen(req, timeout=TIMEOUT) as resp:
                return json.loads(resp.read())
        except urllib.error.HTTPError as e:
            err_body = ""
            try:
                err_body = e.read().decode("utf-8", errors="replace")
            except Exception:
                pass
            if e.code in (429, 529, 503):
                wait = int(e.headers.get("retry-after") or backoff)
                print(f"  HTTP {e.code}, sleeping {wait}s (attempt {attempt+1}/5): {err_body[:200]}", file=sys.stderr)
                time.sleep(wait)
                backoff *= 2
                last_err = e
                continue
            raise RuntimeError(f"HTTP {e.code}: {err_body[:500]}") from e
        except urllib.error.URLError as e:
            last_err = e
            print(f"  URLError, retrying in {backoff}s: {e}", file=sys.stderr)
            time.sleep(backoff)
            backoff *= 2
            continue
    raise RuntimeError(f"repeated API errors: {last_err}")


def classify_item(
    it: dict,
    wikitext: str | None,
    cache_dir: Path,
    api_key: str,
    *,
    model: str = DEFAULT_MODEL,
    use_cache: bool = True,
    refresh: bool = False,
) -> dict:
    """Classify a single item. Returns dict with primary_tab/cross_tags/rationale/_cached."""
    cache_dir.mkdir(parents=True, exist_ok=True)
    key = _key(model, it, wikitext or "")
    cache_file = cache_dir / f"{key}.json"

    if use_cache and not refresh and cache_file.exists():
        cached = json.loads(cache_file.read_text())
        cached["_cached"] = True
        return cached

    system = _build_system_prompt()
    user = _build_user_message(it, wikitext)
    resp = _api_call(model, system, user, api_key)

    content = resp.get("content") or []
    text = "".join(b.get("text", "") for b in content if b.get("type") == "text")
    parsed = _parse_response(text)
    result = _validate(parsed, it.get("name") or f"id={it.get('id')}")

    usage = resp.get("usage") or {}
    result["_usage"] = {
        "input_tokens": usage.get("input_tokens"),
        "cache_creation_input_tokens": usage.get("cache_creation_input_tokens"),
        "cache_read_input_tokens": usage.get("cache_read_input_tokens"),
        "output_tokens": usage.get("output_tokens"),
    }
    result["_model"] = model

    cache_file.write_text(json.dumps(result, indent=2))
    result["_cached"] = False
    return result
