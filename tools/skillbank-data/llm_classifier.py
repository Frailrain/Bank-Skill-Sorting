"""LLM-based item-to-tab classifier using the Anthropic Messages API.

Replaces (eventually) the heuristic predicate stack in mapping.py. Each
item is presented with its osrsbox/wiki metadata + trimmed wikitext, plus
the 22 tab definitions, and the model returns a JSON object specifying a
flat list of every tab the item belongs in.

Brief #62 flattened the schema. Earlier versions split the answer into a
primary_tab + cross_tags pair so we could enforce primary-first sort
ordering. That distinction is gone; each item carries a flat set of tab
memberships and the audit pass is the single source of ground truth on
"should this item appear in this tab?".

Design notes:
- Uses urllib (no `anthropic` SDK dep needed). The Messages API is a thin
  HTTPS POST — keeping this dep-free means the scraper still runs in a
  bare Python install.
- API key read from os.environ['ANTHROPIC_API_KEY'] (loaded by env.py from
  tools/skillbank-data/.env which is gitignored).
- A second transport, backend="claude-cli", sends the same prompt through
  the Claude Code CLI (`claude -p --json-schema ...`) so the calls are
  billed to the maintainer's Claude subscription instead of an API key.
  Locally that is the `claude` login; in GitHub Actions it is the
  CLAUDE_CODE_OAUTH_TOKEN secret minted with `claude setup-token`. The
  monthly refresh (refresh_new_items.py) uses this backend by default;
  `scraper.py --classify` keeps the API transport.
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
import shutil
import subprocess
import sys
import tempfile
import time
import urllib.error
import urllib.request
from pathlib import Path
from typing import Callable

API_URL = "https://api.anthropic.com/v1/messages"
DEFAULT_MODEL = "claude-sonnet-4-6"
ANTHROPIC_VERSION = "2023-06-01"
MAX_TOKENS = 512
TIMEOUT = 60

BACKENDS = ("claude-cli", "api")

# ── Claude Code CLI transport ──────────────────────────────────────────────
# Override the binary with CLAUDE_CLI_BIN (e.g. an absolute path) if `claude`
# is not on PATH.
CLI_BIN = os.environ.get("CLAUDE_CLI_BIN") or "claude"
CLI_TIMEOUT = 300          # seconds per item; the CLI retries transient API errors itself
CLI_MAX_TURNS = 3          # structured output costs the CLI one internal tool turn
CLI_RETRIES = 3
RESPONSE_SCHEMA = {
    "type": "object",
    "properties": {
        "tabs": {"type": "array", "items": {"type": "string"}},
        "rationale": {"type": "string"},
    },
    "required": ["tabs", "rationale"],
    "additionalProperties": False,
}
# Removed from the child environment, by exact name. The nested-session
# markers make the CLI refuse to start from inside another Claude Code
# session; ANTHROPIC_API_KEY / ANTHROPIC_AUTH_TOKEN would silently take
# precedence over the subscription login and bill the API instead.
# CLAUDE_CODE_OAUTH_TOKEN is deliberately kept: it is how the subscription is
# reached in CI, which is why this is a name list and not a prefix match.
CLI_STRIP_ENV = (
    "ANTHROPIC_API_KEY",
    "ANTHROPIC_AUTH_TOKEN",
    "CLAUDECODE",
    "CLAUDE_CODE_CHILD_SESSION",
    "CLAUDE_CODE_SESSION_ID",
    "CLAUDE_CODE_MESSAGING_SOCKET",
    "CLAUDE_CODE_MESSAGING_TOKEN",
    "CLAUDE_CODE_ENTRYPOINT",
    "CLAUDE_CODE_SESSION_ATTENDED",
)
# Messages that are worth a retry: the subscription/API is momentarily busy.
_CLI_TRANSIENT_RE = re.compile(r"overloaded|rate.?limit|529|503|timed? ?out|ECONNRESET|ETIMEDOUT", re.I)

# Messages that mean the login itself was rejected. A retry cannot help and
# every later item would fail the same way, so these raise CliAuthError.
_CLI_AUTH_RE = re.compile(
    r"\b401\b|failed to authenticate|authentication_error"
    r"|invalid (api key|bearer token|auth token|authorization header)"
    r"|oauth (access )?token|not logged in|please run /login",
    re.I,
)

# The variable that carries the subscription login in CI.
OAUTH_TOKEN_ENV = "CLAUDE_CODE_OAUTH_TOKEN"
# Credential families by prefix, most specific first. Only the first one
# works in CLAUDE_CODE_OAUTH_TOKEN; it is what `claude setup-token` prints.
_TOKEN_KINDS = (
    ("sk-ant-oat", True, "a subscription OAuth token (the right kind)"),
    ("sk-ant-api", False, "an Anthropic API key"),
    ("sk-ant-ort", False, "an OAuth refresh token"),
    ("sk-ant-", False, "some other Anthropic credential"),
)
_TOKEN_KIND_UNKNOWN = "something other than an Anthropic token (no sk-ant- prefix)"
# Length of a complete token. `claude setup-token` prints it wrapped over two
# terminal lines, so storing only the first line (79 characters) is the
# classic way to end up with a secret the server rejects.
TOKEN_FULL_LEN = 108

# One tiny request used by cli_probe to prove the login works.
PROBE_SYSTEM = 'Connectivity check. Answer with tabs = [] and rationale = "ok".'
PROBE_USER = "ping"

# (returncode, stdout, stderr) for a command fed `stdin_text` under `env`.
CliRunner = Callable[[list[str], str, dict], tuple[int, str, str]]


class CliAuthError(RuntimeError):
    """The CLI started but its login was rejected: a bad, expired or revoked
    CLAUDE_CODE_OAUTH_TOKEN, or no usable local login. Never retried."""

# Canonical tab names + descriptions (mirrors SkillBankConfig).
TAB_DEFS: list[tuple[str, str]] = [
    ("melee",             "Weapons, armour, combat potions, HP food. Anything used primarily for melee combat including dragon/barrows/godwars/tbow-equivalent melee gear, defenders, slayer helms in melee context."),
    ("range",             "Bows, crossbows, ammunition (arrows/bolts/darts), d'hide armour, blowpipe, ranged void, ava's devices, ranging potion."),
    ("mage",              "Runes, staves, wands, magic armour (mystic/ahrim's/ancestral/virtus), rune pouch, magic potions, god capes, occult, tormented bracelet."),
    ("prayer",            "Bones, ashes, prayer potions, holy wrench, dragonbone necklace, prayer cape. Things that train prayer."),
    ("cooking",           "Cooked/burnt food, raw ingredients used in cooking (flour, eggs, milk, sugar), cook's outfit, cooking gauntlets, cooking cape."),
    ("woodcutting_firemaking", "Axes, logs, pyre logs, tinderbox, firelighters, bruma torch, shade keys/remains, Wintertodt items (bruma kindling, rejuvenation potion, supply crates), pyromancer/lumberjack/forester outfits, woodcutting/firemaking cape, Forestry items (sturdy/anima/log basket/secateurs), Beaver pet."),
    ("fletching",         "Knife (when used as a fletching tool), chisel (for stocks), bow strings, unstrung & strung bows, crossbow stocks, finished crossbows, arrow shafts, headless arrows, dart tips, bolt tips, feathers (cross-tag with fishing), finished arrows/darts/bolts/javelins, fletching cape/hood."),
    ("fishing",           "Raw fish, fishing rods/nets/cages/harpoons, bait, feathers (as fishing bait), angler outfit, fish barrel, spirit anglers, heron."),
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
    ("teleports",         "Teleport tablets, jewellery teleports (glory, skills necklace, ring of dueling), Ectophial, Royal seed pod, Kharedst's memoirs, Drakan's medallion, Pharaoh's sceptre, scrolls of redirection — items used primarily as a teleport. Items keep their other tab memberships (tablets stay in mage, jewellery stays in crafting); this tab is additive."),
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
1. **tabs** — the complete list of tabs this item belongs in. A player should
   expect to find this item in every listed tab. Must be a non-empty list of
   tab names drawn from the 22 below. Order is irrelevant; duplicates are not
   allowed. An item with no skill-specific use should land in a single utility
   tab (misc, quests, or cosmetics as appropriate).
2. **rationale** — one sentence explaining the choice (used for human review).

# Tabs

{tab_lines}

# Guidelines

- Prefer the **training/use context** over the source. A dragon mace belongs in melee (used for combat), not slayer (even if slayer-task-locked).
- For PVM gear at the top of the meta (Torva/Masori/Ancestral/Bandos/Armadyl etc.), put it in the combat style it is USED FOR, not the boss that drops it. Torva is melee, Masori is range, Ancestral is mage, Bandos is melee, Armadyl is range.
- Quest-reward gear belongs in **quests** ONLY if it's primarily defined by being a quest reward with no broader training/combat utility (e.g. quest cape, quest-only cosmetics). Quest-locked gear used in normal training/combat (e.g. Barrows gloves, Recipe-for-Disaster gloves, Verac's armor) goes to its functional tab AND optionally quests.
- Cosmetics tab is for **purely visual** items — third-age sets (full sets, not the offensive d'hide which is range), partyhats, h'ween masks, holiday rares. If an item has actual stats and is competitive, list its stat tab.
- **Ornament-kitted variants** (item names ending in `(or)`, `(or 1)`, `(or 2)`, `(or 3)`, `(cr)`, `(t)`, `(g)`, etc.) keep the **same stats** as their base item — list the **base item's tab** (e.g. Berserker necklace (or) → `melee`) together with `cosmetics`. Do NOT classify ornament-kitted variants as cosmetics-only.
- **LMS / Last Man Standing variants**: items obtainable only inside Last Man Standing (often named identically to their stat counterpart but with a separate item ID) should be classified as `misc` plus the functional stat tab, because they cannot be used outside the minigame. Apply the same rule to other minigame-exclusive item variants.
- Slayer items that are not slayer-mandatory but are part of slayer task loadouts (broad ammo, slayer helms, salve amulet, ferocious gloves) should list slayer alongside their primary functional tab.
- Misc is the fallback for utility items that don't fit a skill (keys, clue items, storage, minigame-exclusive variants). Don't dump items in misc if they have a clear skill tab AND can be used outside a minigame.
- Cosmetic/holiday items with no combat/training role go to **cosmetics** alone, NOT misc.
- **Pets** (boss/skilling pets like Rocky, Heron, Beaver, Olmlet, etc.) go to `cosmetics` since they have no in-bank training/combat utility.
- **Teleports** is additive: a teleport tablet keeps `mage`; a jewellery teleport keeps `crafting`. Add `teleports` whenever the item's primary purpose is to teleport the player.
- An item must not appear in more than one of `melee` / `range` / `mage` — pick the combat style it's actually used for.

# Output format

Respond with ONLY a JSON object, no preamble or trailing text:

{{
  "tabs": ["<one or more tab names>"],
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
    tabs = parsed.get("tabs")
    rationale = parsed.get("rationale") or ""
    if not isinstance(tabs, list) or not tabs:
        raise ValueError(f"{item_name}: tabs must be a non-empty list, got {tabs!r}")
    tabs = [t for t in tabs if isinstance(t, str)]
    bad = [t for t in tabs if t not in VALID_TABS]
    if bad:
        raise ValueError(f"{item_name}: invalid tabs {bad}")
    # Deduplicate while preserving alphabetical order (canonical form on disk).
    tabs = sorted(set(tabs))
    return {"tabs": tabs, "rationale": rationale}


def _is_adaptive_only(model: str) -> bool:
    """Models where sampling params are removed from the API and thinking is
    adaptive (Fable 5, Opus 4.7/4.8, Sonnet 5). Sending temperature to these
    returns a 400."""
    return any(t in model for t in ("fable", "opus-4-7", "opus-4-8", "sonnet-5"))


def _api_call(model: str, system: str, user: str, api_key: str) -> dict:
    payload: dict = {
        "model": model,
        "max_tokens": MAX_TOKENS,
        "temperature": 0,
        "system": [
            {"type": "text", "text": system, "cache_control": {"type": "ephemeral"}},
        ],
        "messages": [
            {"role": "user", "content": user},
        ],
    }
    if _is_adaptive_only(model):
        # No temperature (400 on these models), and thinking output counts
        # against max_tokens — give it room so the JSON isn't truncated.
        del payload["temperature"]
        payload["max_tokens"] = 8000
    body = json.dumps(payload).encode("utf-8")
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


def cli_env(base: dict | None = None) -> dict:
    """Environment for the `claude` child process: the current environment
    minus CLI_STRIP_ENV, with auto-updates and telemetry off."""
    env = dict(os.environ if base is None else base)
    for k in CLI_STRIP_ENV:
        env.pop(k, None)
    env.setdefault("DISABLE_AUTOUPDATER", "1")
    env.setdefault("CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC", "1")
    return env


def cli_command(model: str, system: str) -> list[str]:
    """One-shot, tool-less, non-persisted `claude -p` invocation that must
    answer with RESPONSE_SCHEMA. The user message goes on stdin."""
    return [
        CLI_BIN, "-p",
        "--output-format", "json",
        "--model", model,
        "--system-prompt", system,
        "--json-schema", json.dumps(RESPONSE_SCHEMA),
        "--tools", "",
        "--strict-mcp-config",
        "--no-session-persistence",
        "--max-turns", str(CLI_MAX_TURNS),
        "--disable-slash-commands",
        "--permission-prompts", "none",
    ]


def cli_available() -> bool:
    return shutil.which(CLI_BIN) is not None


def _default_cli_runner(cmd: list[str], stdin_text: str, env: dict) -> tuple[int, str, str]:
    """Run the CLI in an empty scratch directory so no CLAUDE.md, project
    settings or MCP config from the repo leak into the prompt."""
    with tempfile.TemporaryDirectory(prefix="skillbank-llm-") as cwd:
        proc = subprocess.run(
            cmd, input=stdin_text, capture_output=True, text=True,
            cwd=cwd, env=env, timeout=CLI_TIMEOUT, check=False,
        )
    return proc.returncode, proc.stdout, proc.stderr


def cli_auth_status(runner: CliRunner | None = None) -> dict | None:
    """`claude auth status --json` as a dict, or None when it cannot be read.
    With CLAUDE_CODE_OAUTH_TOKEN set this only confirms the token is present;
    it is not validated until the first real call."""
    runner = runner or _default_cli_runner
    try:
        _rc, out, _err = runner([CLI_BIN, "auth", "status", "--json"], "", cli_env())
        status = json.loads(out)
    except (OSError, ValueError, subprocess.SubprocessError):
        return None
    return status if isinstance(status, dict) else None


def oauth_token_shape(token: str | None) -> dict:
    """Describe a CLAUDE_CODE_OAUTH_TOKEN value without revealing any of it.

    Returns {"set", "length", "kind", "right_kind", "problems"}; every string
    is a fixed label, so the result is safe to print in a public CI log. The
    problems are hints for a human (a mis-pasted secret is the usual cause of
    a rejected login), not a verdict: only a real call proves a token."""
    raw = token or ""
    stripped = raw.strip()
    core = stripped.strip("\"'`")
    at = core.find("sk-ant-")
    body = core[at:] if at >= 0 else core
    kind, right_kind = _TOKEN_KIND_UNKNOWN, False
    for prefix, ok, label in _TOKEN_KINDS:
        if body.startswith(prefix):
            kind, right_kind = label, ok
            break

    problems: list[str] = []
    if raw != stripped:
        problems.append("it has spaces or line breaks before or after the token")
    if core != stripped:
        problems.append("it is wrapped in quotes; store the bare token")
    if at > 0:
        problems.append(
            "there is extra text before the token (for example `export NAME=`); "
            "store the bare token"
        )
    if any(c.isspace() for c in body):
        problems.append(
            "it has a space or line break in the middle "
            "(a token wrapped over two terminal lines was probably copied with the break)"
        )
    if raw and not right_kind:
        problems.append(
            f"it looks like {kind}, not the token `claude setup-token` prints "
            "(that one starts with sk-ant-oat)"
        )
    if right_kind and re.search(r"[^A-Za-z0-9_\-]", "".join(body.split())):
        problems.append("it contains characters that never appear in a token")
    if right_kind and len("".join(body.split())) < TOKEN_FULL_LEN:
        problems.append(
            f"it is shorter than a complete token, which has {TOKEN_FULL_LEN} characters: "
            "`claude setup-token` prints the token over two lines and only part of it was copied"
        )
    return {
        "set": bool(raw), "length": len(raw), "kind": kind,
        "right_kind": right_kind, "problems": problems,
    }


def cli_probe(model: str = DEFAULT_MODEL, *, runner: CliRunner | None = None) -> dict:
    """One tiny real request through the exact transport classify_item uses.

    `claude auth status` only reports that a login is configured; this is the
    check that it is accepted. Raises CliAuthError when the login is rejected
    and RuntimeError for any other failure; returns the CLI's result JSON."""
    return _cli_call(model, PROBE_SYSTEM, PROBE_USER, runner=runner)


def _cli_call(model: str, system: str, user: str, *, runner: CliRunner | None = None) -> dict:
    """Run one classification through the CLI and return its result JSON
    (`type: result`, with `structured_output`, `result`, `usage`,
    `total_cost_usd`, `is_error`). Raises CliAuthError, without retrying,
    when the CLI reports that its login was rejected."""
    runner = runner or _default_cli_runner
    cmd = cli_command(model, system)
    env = cli_env()
    backoff = 5
    last_err: Exception | None = None
    for attempt in range(CLI_RETRIES):
        try:
            rc, out, err = runner(cmd, user, env)
        except FileNotFoundError as e:
            raise RuntimeError(
                f"claude CLI not found ({CLI_BIN!r}); install Claude Code or use backend='api'"
            ) from e
        except subprocess.TimeoutExpired as e:
            last_err = RuntimeError(f"claude CLI timed out after {CLI_TIMEOUT}s")
            print(f"  claude CLI timeout, retrying in {backoff}s (attempt {attempt+1}/{CLI_RETRIES})", file=sys.stderr)
            time.sleep(backoff)
            backoff *= 2
            continue

        resp: dict | None = None
        if out.strip():
            try:
                loaded = json.loads(out)
                if isinstance(loaded, dict):
                    resp = loaded
            except ValueError:
                resp = None

        if rc == 0 and resp is not None and not resp.get("is_error"):
            return resp

        if resp is not None:
            detail = str(resp.get("result") or resp.get("error") or "")
        else:
            detail = (err or out or "").strip()
        detail = detail[:500]
        if _CLI_AUTH_RE.search(detail):
            raise CliAuthError(detail)
        last_err = RuntimeError(f"claude CLI exited {rc}: {detail or '(no output)'}")
        if _CLI_TRANSIENT_RE.search(detail):
            print(f"  claude CLI transient error, retrying in {backoff}s (attempt {attempt+1}/{CLI_RETRIES}): {detail[:200]}", file=sys.stderr)
            time.sleep(backoff)
            backoff *= 2
            continue
        raise last_err
    raise RuntimeError(f"repeated claude CLI errors: {last_err}")


def _usage_dict(usage: dict) -> dict:
    return {
        "input_tokens": usage.get("input_tokens"),
        "cache_creation_input_tokens": usage.get("cache_creation_input_tokens"),
        "cache_read_input_tokens": usage.get("cache_read_input_tokens"),
        "output_tokens": usage.get("output_tokens"),
    }


def classify_item(
    it: dict,
    wikitext: str | None,
    cache_dir: Path,
    api_key: str,
    *,
    model: str = DEFAULT_MODEL,
    use_cache: bool = True,
    refresh: bool = False,
    backend: str = "api",
    cli_runner: CliRunner | None = None,
) -> dict:
    """Classify a single item. Returns dict with tabs/rationale/_cached.

    backend="api" posts to the Messages API with `api_key`; backend="claude-cli"
    runs the Claude Code CLI on the subscription login (api_key unused). Both
    share the response cache: same model + prompt, same answer."""
    if backend not in BACKENDS:
        raise ValueError(f"unknown backend {backend!r}; expected one of {BACKENDS}")
    cache_dir.mkdir(parents=True, exist_ok=True)
    key = _key(model, it, wikitext or "")
    cache_file = cache_dir / f"{key}.json"

    if use_cache and not refresh and cache_file.exists():
        cached = json.loads(cache_file.read_text())
        cached["_cached"] = True
        return cached

    system = _build_system_prompt()
    user = _build_user_message(it, wikitext)
    item_name = it.get("name") or f"id={it.get('id')}"

    if backend == "claude-cli":
        resp = _cli_call(model, system, user, runner=cli_runner)
        parsed = resp.get("structured_output")
        if not isinstance(parsed, dict):
            parsed = _parse_response(str(resp.get("result") or ""))
        result = _validate(parsed, item_name)
        result["_usage"] = _usage_dict(resp.get("usage") or {})
        result["_cost_usd"] = resp.get("total_cost_usd")
    else:
        resp = _api_call(model, system, user, api_key)
        content = resp.get("content") or []
        text = "".join(b.get("text", "") for b in content if b.get("type") == "text")
        parsed = _parse_response(text)
        result = _validate(parsed, item_name)
        result["_usage"] = _usage_dict(resp.get("usage") or {})
    result["_model"] = model
    result["_backend"] = backend

    cache_file.write_text(json.dumps(result, indent=2))
    result["_cached"] = False
    return result
