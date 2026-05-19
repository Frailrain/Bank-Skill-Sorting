"""Fetch OSRS Wiki page wikitext via MediaWiki action=parse.

Companion to wiki.py (which uses the Bucket API). This is for the LLM
classifier: we need the prose/infobox content of an item's wiki page as
extra context for tab assignment.

Endpoint: https://oldschool.runescape.wiki/api.php?action=parse&page={name}&prop=wikitext&format=json
Disk cache: tools/skillbank-data/cache/wiki-pages/<sha1>.txt (raw wikitext)
Rate limit: 1 req/sec, mirroring wiki.py — shares the same _last_call clock
so concurrent use doesn't violate the wiki's limits.
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
RATE_LIMIT_SECONDS = 1.0
DEFAULT_TTL_SECONDS = 30 * 24 * 60 * 60  # 30 days — page content barely changes

_last_call = 0.0


def _rate_limit():
    global _last_call
    elapsed = time.monotonic() - _last_call
    if elapsed < RATE_LIMIT_SECONDS:
        time.sleep(RATE_LIMIT_SECONDS - elapsed)
    _last_call = time.monotonic()


def _cache_path(cache_dir: Path, page_name: str) -> Path:
    h = hashlib.sha1(page_name.encode("utf-8")).hexdigest()[:16]
    return cache_dir / f"page-{h}.txt"


def _meta_path(cache_dir: Path, page_name: str) -> Path:
    h = hashlib.sha1(page_name.encode("utf-8")).hexdigest()[:16]
    return cache_dir / f"page-{h}.meta.json"


def _cache_fresh(path: Path, ttl: int) -> bool:
    if not path.exists():
        return False
    return (time.time() - path.stat().st_mtime) < ttl


def fetch_page_wikitext(
    page_name: str,
    cache_dir: Path,
    *,
    ttl: int = DEFAULT_TTL_SECONDS,
    use_cache: bool = True,
    refresh: bool = False,
) -> str | None:
    """Return raw wikitext for a page, or None if the page is missing.

    A None result is cached as an empty file with a sentinel meta entry so
    we don't keep hammering the wiki for missing pages.
    """
    cache_dir.mkdir(parents=True, exist_ok=True)
    path = _cache_path(cache_dir, page_name)
    meta = _meta_path(cache_dir, page_name)

    if use_cache and not refresh and _cache_fresh(path, ttl):
        if meta.exists():
            try:
                m = json.loads(meta.read_text())
                if m.get("missing"):
                    return None
            except json.JSONDecodeError:
                pass
        return path.read_text()

    _rate_limit()
    params = urllib.parse.urlencode({
        "action": "parse",
        "page": page_name,
        "prop": "wikitext",
        "format": "json",
        "formatversion": "2",
        "redirects": "1",
    })
    url = f"{WIKI_API}?{params}"
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})

    backoff = 5
    for _ in range(4):
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
        raise RuntimeError(f"repeated 429 responses fetching {page_name!r}")

    # MediaWiki returns {"error": {...}} for missing pages (or {"parse": {...}} on success).
    if "error" in data:
        path.write_text("")
        meta.write_text(json.dumps({"missing": True, "error": data["error"]}))
        return None

    parse = data.get("parse") or {}
    wikitext = parse.get("wikitext")
    if wikitext is None:
        path.write_text("")
        meta.write_text(json.dumps({"missing": True, "error": "no wikitext field"}))
        return None
    if isinstance(wikitext, dict):  # formatversion=1 wraps in {"*": "..."}
        wikitext = wikitext.get("*", "")

    path.write_text(wikitext)
    if meta.exists():
        meta.unlink()
    return wikitext


def trim_wikitext(text: str, max_chars: int = 6000) -> str:
    """Drop the lowest-value tail content so we fit in a sensible LLM budget.

    Strategy: keep the head (which holds the {{Infobox Item}}, intro prose,
    and usually the first behaviour section). Cut at the next section boundary
    after max_chars if possible, else hard-truncate.
    """
    if len(text) <= max_chars:
        return text
    cut = text.find("\n==", max_chars)
    if cut == -1 or cut > max_chars + 2000:
        return text[:max_chars] + "\n\n[...truncated...]"
    return text[:cut] + "\n\n[...truncated...]"
