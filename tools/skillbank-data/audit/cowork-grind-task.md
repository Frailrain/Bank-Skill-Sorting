---
name: skillbank-batch-grind
description: Autonomously continue the OSRS Skill Bank item classification from the live resume point, in 50-item chunks, until all 11,826 items are done.
suggested_cadence: every 15 minutes (cron "*/15 * * * *"); tighten if runs reliably finish faster
notify_on_completion: false
---

# Skill Bank grind — scheduled-task / fresh-thread handoff prompt

Paste this as the prompt when creating the scheduled task (taskId `skillbank-batch-grind`), or hand it to any fresh thread to continue the grind. It is fully self-contained.

---

You are continuing an OSRS Skill Bank item-classification project for Matt. This is an autonomous handoff: a fresh agent each run with no memory of prior runs, so everything you need is on disk. Matt's standing instruction is to KEEP GRINDING without asking permission and without steering toward stopping. Just do the work and commit it.

CRITICAL ENVIRONMENT NOTE: use the file-tools (Read, Edit, Glob, Grep) for ALL file access. The sandbox bash mount of /var/home/mhersee/dev is stale/empty under Matt's wrapped desktop app, so bash CANNOT see these files. The file-tools reach the real folder. Do not try to "fix" access via bash; just use Read/Edit/Glob/Grep.

Project folder: /var/home/mhersee/dev/skillbank-plugin/tools/skillbank-data/

STEP 1 — Ground yourself in the ruleset (read both):
- /var/home/mhersee/dev/skillbank-plugin/tools/skillbank-data/audit/_HANDOFF.md (the runbook: current state, resume point, the full settled ruleset across batches 1-7, and Matt's working preferences).
- /var/home/mhersee/dev/skillbank-plugin/tools/skillbank-data/audit/questions-for-matt.md (the complete accumulated ruleset and Matt's resolved rulings).

STEP 2 — Compute the live resume point (do NOT trust any hardcoded number):
- Count lines in audit/decisions.jsonl with Grep output_mode=count, pattern ^\{"id". Call it N.
- The next item to classify is at line N+51 of out/audit-input.jsonl. (Line mapping: audit-input line = decisions line + 50; the next decision is line N+1.)
- Alignment check: read audit-input.jsonl line N+50 and confirm its id equals the id on the last line of decisions.jsonl. If they do NOT match, STOP and append a clearly-labelled mismatch note to _HANDOFF.md instead of guessing.
- Current batch B = ceil(N/1188); batch B spans decisions (B-1)*1188+1 .. B*1188 (batch 7 ends at decision 8316). Use the matching log file audit/log-batch-00B.csv.

STEP 3 — Classify in 50-item chunks; do up to 4 chunks this run, then stop:
- Read the next 50 input lines. Classify each item into one or more of the 22 Skill Bank tabs under the inputs-only "Brief #63" rule: an item belongs in the tab(s) of the skill(s) that CONSUME it (its input or its produced output), NOT merely because using it grants XP. Apply every binding ruling in _HANDOFF.md and questions-for-matt.md.
- The 22 tabs: Melee, Range, Mage, Prayer, Cooking, Woodcutting + Firemaking, Fletching, Fishing, Crafting, Mining + Smithing, Herblore, Agility + Thieving, Slayer, Farming, Runecraft, Hunter, Construction, Misc, Quests, Sailing, Cosmetics, Teleports. Combined tabs use the literal strings with spaces around the plus.
- Append the 50 new lines to BOTH decisions.jsonl AND the current log-batch file using the Edit tool: old_string = the current exact last line of the file, new_string = that same line plus a newline plus the 50 new lines. Work one chunk at a time and commit before reading the next chunk.
- Formats. decisions.jsonl line: {"id":N,"name":"...","tabs":[...],"rationale":"...","confidence":"high|medium|low"}. Tabs alphabetical. log line: id,"name","old_tabs","new_tabs",changed,"rationale",confidence — where old_tabs is the input's current_tabs and new_tabs is your result, both comma-joined with NO space and alphabetical (e.g. "Cosmetics,Melee"); changed is yes/no (did tabs differ from current_tabs); name, old_tabs, new_tabs, rationale are quoted; changed and confidence are unquoted.
- After each chunk re-verify (Grep count) that decisions.jsonl and the log file have matching counts and end on the same id.
- For genuinely low-confidence items, you may wiki-check via the approved web tools only (WebSearch / web_fetch). Never use curl/wget/archives.

STEP 4 — At a BATCH BOUNDARY (decisions.jsonl reaches a multiple of 1188, e.g. 8316 ends batch 7):
- Verify decisions.jsonl == 1188*B lines, log-batch-00B.csv == 1188 data rows + header, and ID continuity (no gaps/dupes in the batch range).
- Produce audit/audit-slice-batch-00B.csv: roughly 60 rows weighted to changed and medium/low-confidence rows plus EVERY rule-level judgment call you made in the batch. Header: id,name,old_tabs,new_tabs,changed,confidence,rationale,approved,comments — leave approved and comments blank for Matt.
- On the next chunk, create log-batch-00(B+1).csv with the header line id,name,old_tabs,new_tabs,changed,rationale,confidence and continue.

STEP 5 — Update the marker every run:
- Update _HANDOFF.md "Current state" and "RESUME POINT" sections with the new decisions count, last id, batch progress, and next audit-input line. Record any new rule-level families under the batch's settled-patterns section, and explicitly FLAG genuine judgment calls so Matt catches them in his audit.

STOP CONDITION: when decisions.jsonl reaches 11,826 lines the whole project is classified — verify final counts, mark completion in _HANDOFF.md, and do nothing further.

Style for any notes you write: prose over bullets where reasonable, no em dashes, no AI-flavored phrasing. Never suggest breaks or wrap-ups; Matt signals when he is done.
