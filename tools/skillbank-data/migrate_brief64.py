#!/usr/bin/env python3
"""Brief #64 data migration: remove standalone `firemaking` tab.

For every artifact that lists tab membership, any occurrence of the
`firemaking` tab (snake_case internal id) or `Firemaking` (display name)
is collapsed into `woodcutting_firemaking` / `Woodcutting + Firemaking`.
If the destination tab is already in the list, the firemaking entry is
just dropped. Final list is deduplicated + sorted.

Files touched (in place):
  - out/llm-classifications.json
  - out/audit-input.jsonl
  - audit/decisions.jsonl
  - audit/log-batch-*.csv
"""
from __future__ import annotations

import csv
import io
import json
import sys
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
OUT_DIR = SCRIPT_DIR / "out"
AUDIT_DIR = SCRIPT_DIR / "audit"

# Both forms get rewritten — internal ID and display name. Each entry maps
# a token that should be replaced to its replacement.
_REWRITES = {
    "firemaking": "woodcutting_firemaking",
    "Firemaking": "Woodcutting + Firemaking",
}


def _migrate_list(tabs: list[str]) -> tuple[list[str], bool]:
    """Replace any firemaking tokens, dedupe, sort. Returns (new_list, changed)."""
    new = []
    for t in tabs:
        new.append(_REWRITES.get(t, t))
    deduped = sorted(set(new))
    return deduped, deduped != list(tabs)


# ── out/llm-classifications.json ──────────────────────────────────────────


def migrate_llm_classifications() -> int:
    path = OUT_DIR / "llm-classifications.json"
    data = json.loads(path.read_text())
    n_touched = 0
    for it in data["items"]:
        new_tabs, changed = _migrate_list(list(it["tabs"]))
        if changed:
            it["tabs"] = new_tabs
            n_touched += 1
    path.write_text(json.dumps(data, indent=2))
    print(f"  llm-classifications.json: rewrote {n_touched} items", file=sys.stderr)
    return n_touched


# ── out/audit-input.jsonl ─────────────────────────────────────────────────


def migrate_audit_input() -> int:
    path = OUT_DIR / "audit-input.jsonl"
    if not path.exists():
        print("  audit-input.jsonl: not present, skipping", file=sys.stderr)
        return 0
    n_touched = 0
    out_lines: list[str] = []
    for line in path.read_text().splitlines():
        line = line.strip()
        if not line:
            out_lines.append("")
            continue
        rec = json.loads(line)
        new_tabs, changed = _migrate_list(list(rec.get("current_tabs") or []))
        if changed:
            rec["current_tabs"] = new_tabs
            n_touched += 1
        out_lines.append(json.dumps(rec, ensure_ascii=False, separators=(",", ":")))
    path.write_text("\n".join(out_lines) + ("\n" if out_lines else ""))
    print(f"  audit-input.jsonl: rewrote {n_touched} entries", file=sys.stderr)
    return n_touched


# ── audit/decisions.jsonl ─────────────────────────────────────────────────


def migrate_decisions() -> int:
    path = AUDIT_DIR / "decisions.jsonl"
    if not path.exists():
        print("  decisions.jsonl: not present, skipping", file=sys.stderr)
        return 0
    n_touched = 0
    out_lines: list[str] = []
    for line in path.read_text().splitlines():
        line = line.strip()
        if not line:
            out_lines.append("")
            continue
        rec = json.loads(line)
        new_tabs, changed = _migrate_list(list(rec.get("tabs") or []))
        if changed:
            rec["tabs"] = new_tabs
            n_touched += 1
        out_lines.append(json.dumps(rec, ensure_ascii=False))
    path.write_text("\n".join(out_lines) + ("\n" if out_lines else ""))
    print(f"  decisions.jsonl: rewrote {n_touched} decisions", file=sys.stderr)
    return n_touched


# ── audit/log-batch-*.csv ─────────────────────────────────────────────────


def _migrate_csv_cell(cell: str) -> tuple[str, bool]:
    if not cell:
        return cell, False
    tokens = [t.strip() for t in cell.split(",") if t.strip()]
    if not tokens:
        return cell, False
    new_tokens, changed = _migrate_list(tokens)
    new_cell = ",".join(new_tokens)
    return new_cell, new_cell != cell


def migrate_log_batches() -> int:
    csvs = sorted(AUDIT_DIR.glob("log-batch-*.csv"))
    if not csvs:
        print("  log-batch CSVs: none found, skipping", file=sys.stderr)
        return 0
    total = 0
    for csv_path in csvs:
        text = csv_path.read_text()
        reader = csv.DictReader(io.StringIO(text))
        if reader.fieldnames is None:
            continue
        rows = list(reader)
        n_changes = 0
        for row in rows:
            row_changed = False
            for col in ("old_tabs", "new_tabs"):
                if col in row:
                    new_val, ch = _migrate_csv_cell(row[col])
                    if ch:
                        row[col] = new_val
                        row_changed = True
            if row_changed:
                n_changes += 1
        buf = io.StringIO()
        writer = csv.DictWriter(buf, fieldnames=reader.fieldnames,
                                 quoting=csv.QUOTE_MINIMAL)
        writer.writeheader()
        writer.writerows(rows)
        csv_path.write_text(buf.getvalue())
        print(f"  {csv_path.name}: rewrote {n_changes} rows", file=sys.stderr)
        total += n_changes
    return total


def main() -> int:
    print("Brief #64 migration starting...", file=sys.stderr)
    n_llm = migrate_llm_classifications()
    n_audit = migrate_audit_input()
    n_dec = migrate_decisions()
    n_csv = migrate_log_batches()
    print(file=sys.stderr)
    print("Summary:", file=sys.stderr)
    print(f"  llm-classifications.json items rewritten: {n_llm}", file=sys.stderr)
    print(f"  audit-input.jsonl entries rewritten:      {n_audit}", file=sys.stderr)
    print(f"  decisions.jsonl decisions rewritten:      {n_dec}", file=sys.stderr)
    print(f"  log-batch CSV rows rewritten:             {n_csv}", file=sys.stderr)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
