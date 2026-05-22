#!/usr/bin/env python3
"""Brief #63 data migration: split wc_fletching → woodcutting_firemaking +
fletching across the live data + in-flight Cowork artifacts.

Operations:

  1. out/llm-classifications.json  (in-place)
       items[i].tabs: replace "wc_fletching" with one of the two new tab IDs
       chosen by _section_fletching() / _section_woodcutting_firemaking()
       routing. Internal IDs stay snake_case.

  2. audit/decisions.jsonl  (in-place)
       items[i].tabs: same wc_fletching split, then translate all snake_case
       tab IDs to display names. Inline mentions in rationale text get
       updated too so Cowork reads a coherent decision log.

  3. audit/log-batch-NNN.csv  (in-place, for every batch)
       old_tabs / new_tabs columns: same translation as decisions.jsonl.
       Inline mentions in rationale column updated.

Prints a per-file count of rewrites so Matt can see the scope.
"""
from __future__ import annotations

import csv
import io
import json
import re
import sys
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
sys.path.insert(0, str(SCRIPT_DIR))

import sort_tables  # type: ignore

OUT_DIR = SCRIPT_DIR / "out"
AUDIT_DIR = SCRIPT_DIR / "audit"


# ── wc_fletching split ────────────────────────────────────────────────────

# Positive woodcutting+firemaking name signals. An item from wc_fletching
# that matches any of these stays in the combined Woodcutting + Firemaking
# tab. Everything else routes to the new Fletching tab. (Both classifiers
# in sort_tables.py fall through to permissive defaults, so we can't just
# delegate — we need an explicit positive test for W+F here.)
_WF_NAME_PATTERNS = (
    " logs", " log",
    "pyre log",
    "tinderbox", "firelighter", "bruma torch", "bruma kindling",
    "wintertodt", "rejuvenation potion", "burnt page", "supply crate",
    "pyromancer", "firemaking cape", "firemaking hood",
    "lumberjack", "forester",
    "woodcutting cape", "woodcutting hood",
    "forestry",
    "felling axe",
    "fox whistle", "log basket", "pheasant", "bird egg",
    "shade key", "shade remains",
    "fiyr remains", "loar remains", "phrin remains", "riyl remains",
    "asyn remains",
)
# `axe` is its own check because we have to exclude pickaxe + ornament-only
# variants that aren't really woodcutting axes.
_WF_AXE_RE = re.compile(r"\baxe\b", re.IGNORECASE)


def _split_wc_fletching(item_name: str) -> str:
    """Return either 'fletching' or 'woodcutting_firemaking' for an item that
    was previously in wc_fletching. Name-pattern based: positive signals for
    W+F (axes, logs, firemaking tools, outfits) keep the item in W+F;
    everything else routes to Fletching (which is dominated by bows, arrows,
    bolts, darts, javelins, strings, tips, shafts, the chisel, the knife)."""
    n = item_name.lower()
    if any(pat in n for pat in _WF_NAME_PATTERNS):
        return "woodcutting_firemaking"
    if _WF_AXE_RE.search(n) and "pickaxe" not in n:
        return "woodcutting_firemaking"
    return "fletching"


def _migrate_tabs_list(tabs: list[str], item_name: str) -> list[str]:
    """Replace `wc_fletching` in a tabs list with the mechanical split target.
    Preserves alphabetical order + de-dupes. Other tab IDs pass through."""
    out: set[str] = set()
    for t in tabs:
        if t == "wc_fletching":
            out.add(_split_wc_fletching(item_name))
        else:
            out.add(t)
    return sorted(out)


# ── Snake-case ID → display-name translation ──────────────────────────────

# Translate a single tab token to its display name. Used in the audit
# boundary files (decisions.jsonl, log-batch CSVs, cowork prompt).
def _to_display(tab_id: str) -> str:
    return sort_tables.display_name(tab_id)


def _translate_tab_token(tok: str) -> str:
    """Translate a tab token taken from a free-form string. The token may
    already be in display-name form (because Cowork's earlier decision was
    written by hand) or in snake_case form. Returns display-name form."""
    # If it's a known internal ID, translate.
    if tok in sort_tables.TAB_SECTIONS:
        return _to_display(tok)
    # If it's already a known display name, accept verbatim.
    try:
        sort_tables.tab_id_from_display(tok)
        return tok
    except KeyError:
        return tok  # not a tab token at all


# Inline-mention rewrite: catch snake_case tab tokens embedded in prose. We
# only target the legacy combined-tab IDs explicitly so we don't accidentally
# rewrite words that happen to match generic tabs like "range" or "fishing"
# in unrelated contexts.
_INLINE_REWRITES: list[tuple[str, str]] = [
    ("wc_fletching", "Woodcutting + Firemaking / Fletching"),
    ("mining_smithing", "Mining + Smithing"),
    ("agility_thieving", "Agility + Thieving"),
]


def _rewrite_inline_mentions(text: str) -> tuple[str, int]:
    """Replace bare snake_case tokens in free-form text. Returns
    (new_text, total_replacements)."""
    total = 0
    out = text
    for old, new in _INLINE_REWRITES:
        # word boundary on both sides so we don't touch substrings.
        new_text, n = re.subn(rf"\b{re.escape(old)}\b", new, out)
        if n:
            out = new_text
            total += n
    return out, total


# ── Step 1: llm-classifications.json ──────────────────────────────────────


def migrate_llm_classifications() -> tuple[int, int]:
    """Returns (items_touched, total_wc_replacements).

    Handles both forward migration (wc_fletching → split) and idempotent
    fix-up of an already-migrated file where every old wc_fletching item
    landed in `fletching`. The first pass converts wc_fletching → mechanical
    split; the second pass re-evaluates items in `fletching` against the
    W+F name patterns and moves over the woodcutting / firemaking ones."""
    path = OUT_DIR / "llm-classifications.json"
    data = json.loads(path.read_text())
    n_touched = 0
    n_wc = 0
    for it in data["items"]:
        # Forward migration: wc_fletching → split.
        if "wc_fletching" in it["tabs"]:
            n_wc += 1
            new_tabs = _migrate_tabs_list(it["tabs"], it["name"])
            if new_tabs != it["tabs"]:
                it["tabs"] = new_tabs
                n_touched += 1
        # Fix-up: items already in `fletching` whose names match a W+F
        # pattern get re-routed. Only applies when woodcutting_firemaking
        # isn't already alongside (don't duplicate the original wc_fletching
        # appearing in both).
        if "fletching" in it["tabs"] and "woodcutting_firemaking" not in it["tabs"]:
            target = _split_wc_fletching(it["name"])
            if target == "woodcutting_firemaking":
                tabs_set = set(it["tabs"])
                tabs_set.discard("fletching")
                tabs_set.add("woodcutting_firemaking")
                new_tabs = sorted(tabs_set)
                if new_tabs != it["tabs"]:
                    it["tabs"] = new_tabs
                    n_touched += 1
    path.write_text(json.dumps(data, indent=2))
    print(f"  llm-classifications.json: touched {n_touched} items "
          f"({n_wc} still had wc_fletching at start)", file=sys.stderr)
    return n_touched, n_wc


# ── Step 2: audit/decisions.jsonl ─────────────────────────────────────────


_DISPLAY_FLETCHING = sort_tables.display_name("fletching")
_DISPLAY_WF = sort_tables.display_name("woodcutting_firemaking")


def _fixup_tab_list(tabs: list[str], item_name: str) -> list[str]:
    """Apply the wc_fletching split + display-name translation in one pass.
    Idempotent — if a tab list already contains the display-name "Fletching"
    from a botched earlier migration, re-evaluates against W+F patterns."""
    # Normalize each tab token to a snake_case id we can route on.
    ids: list[str] = []
    for tok in tabs:
        if tok == "wc_fletching":
            ids.append(_split_wc_fletching(item_name))
            continue
        if tok in sort_tables.TAB_SECTIONS:
            ids.append(tok)
            continue
        try:
            ids.append(sort_tables.tab_id_from_display(tok))
        except KeyError:
            ids.append(tok)  # unrecognized — pass through
    id_set = set(ids)
    # Fix-up: re-route an item currently in `fletching` to `woodcutting_firemaking`
    # if its name screams W+F. Only triggers when not already alongside.
    if "fletching" in id_set and "woodcutting_firemaking" not in id_set:
        if _split_wc_fletching(item_name) == "woodcutting_firemaking":
            id_set.discard("fletching")
            id_set.add("woodcutting_firemaking")
    # Translate to display names for the audit boundary.
    return sorted({_to_display(t) for t in id_set})


def migrate_decisions_jsonl() -> int:
    """Returns count of rewritten decisions (any change at all)."""
    path = AUDIT_DIR / "decisions.jsonl"
    if not path.exists():
        print(f"  {path}: not present, skipping", file=sys.stderr)
        return 0
    rewritten = 0
    out_lines: list[str] = []
    for line in path.read_text().splitlines():
        line = line.strip()
        if not line:
            out_lines.append("")
            continue
        rec = json.loads(line)
        changed = False
        original_tabs = list(rec.get("tabs") or [])

        new_tabs = _fixup_tab_list(original_tabs, rec.get("name") or "")
        if new_tabs != original_tabs:
            changed = True
        rec["tabs"] = new_tabs

        # Inline mentions in rationale text.
        if rec.get("rationale"):
            new_rat, n = _rewrite_inline_mentions(rec["rationale"])
            if n:
                rec["rationale"] = new_rat
                changed = True

        if changed:
            rewritten += 1
        out_lines.append(json.dumps(rec, ensure_ascii=False))
    path.write_text("\n".join(out_lines) + ("\n" if out_lines else ""))
    print(f"  decisions.jsonl: rewrote {rewritten} decisions", file=sys.stderr)
    return rewritten


# ── Step 3: audit/log-batch-NNN.csv ───────────────────────────────────────


def _migrate_csv_tabs_cell(cell: str, item_name: str) -> tuple[str, bool]:
    """A cell holds a comma-separated list of tab tokens (snake_case ids or
    display names depending on migration state). Returns
    (translated_cell, changed)."""
    if not cell:
        return cell, False
    tokens = [t.strip() for t in cell.split(",") if t.strip()]
    if not tokens:
        return cell, False
    display_tabs = _fixup_tab_list(tokens, item_name)
    new_cell = ",".join(display_tabs)
    return new_cell, new_cell != cell


def migrate_log_batch_csvs() -> int:
    """Returns count of CSV rows where any column was rewritten."""
    csvs = sorted(AUDIT_DIR.glob("log-batch-*.csv"))
    if not csvs:
        print("  log-batch CSVs: none found, skipping", file=sys.stderr)
        return 0

    total_rewrites = 0
    for csv_path in csvs:
        text = csv_path.read_text()
        reader = csv.DictReader(io.StringIO(text))
        if reader.fieldnames is None:
            continue
        rows = list(reader)
        n_changes = 0
        for row in rows:
            name = row.get("name") or ""
            changed = False
            if "old_tabs" in row:
                new_old, ch = _migrate_csv_tabs_cell(row["old_tabs"], name)
                if ch:
                    row["old_tabs"] = new_old
                    changed = True
            if "new_tabs" in row:
                new_new, ch = _migrate_csv_tabs_cell(row["new_tabs"], name)
                if ch:
                    row["new_tabs"] = new_new
                    changed = True
            if row.get("rationale"):
                new_rat, n = _rewrite_inline_mentions(row["rationale"])
                if n:
                    row["rationale"] = new_rat
                    changed = True
            if changed:
                n_changes += 1

        # Rewrite the CSV.
        buf = io.StringIO()
        writer = csv.DictWriter(buf, fieldnames=reader.fieldnames,
                                 quoting=csv.QUOTE_MINIMAL)
        writer.writeheader()
        writer.writerows(rows)
        csv_path.write_text(buf.getvalue())
        print(f"  {csv_path.name}: rewrote {n_changes} rows", file=sys.stderr)
        total_rewrites += n_changes
    return total_rewrites


# ── Driver ────────────────────────────────────────────────────────────────


def main() -> int:
    print("Brief #63 migration starting...", file=sys.stderr)

    n_touched, n_wc = migrate_llm_classifications()
    n_decisions = migrate_decisions_jsonl()
    n_csv_rows = migrate_log_batch_csvs()

    print(file=sys.stderr)
    print(f"Summary:", file=sys.stderr)
    print(f"  llm-classifications.json items touched: {n_touched}", file=sys.stderr)
    print(f"  decisions.jsonl decisions rewritten:    {n_decisions}", file=sys.stderr)
    print(f"  log-batch CSV rows rewritten:           {n_csv_rows}", file=sys.stderr)
    print("Now run make_audit_input.py to regenerate out/audit-input.jsonl",
          file=sys.stderr)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
