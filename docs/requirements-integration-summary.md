# Requirements integration summary

Brief #72: merged wiki-parsed requirements (Brief #71) into item-meta.json via llm_promote.emit_item_metadata.

- Items in item-meta.json: **11,819**
- Wiki HIGH-confidence merges: **1,093**
  - Wiki agrees with osrsbox exactly: **702**
  - Wiki overrides osrsbox (wiki wins, sample below): **197**
  - Wiki has data, osrsbox missing: **194**
- Confirmed no requirements ({} in rq): **2,431**
- osrsbox-kept (wiki medium, osrsbox had low-tier data): **560**
- osrsbox-only (item not in wiki equipable set): **16**
- No requirements data from any source: **7,719**

## Wiki overrides osrsbox (sample of 30)

| id | name | wiki (kept) | osrsbox (replaced) |
|---|---|---|---|
| 1339 | Steel warhammer | strength:5 | attack:5 |
| 1341 | Black warhammer | strength:10 | attack:10 |
| 1345 | Adamant warhammer | strength:30 | attack:30 |
| 1347 | Rune warhammer | strength:40 | attack:40 |
| 4153 | Granite maul | attack:50, strength:50 | attack:50 |
| 4503 | Decorative sword | attack:20 | attack:5 |
| 4504 | Decorative armour | defence:20 | defence:5 |
| 4505 | Decorative armour | defence:20 | defence:5 |
| 4506 | Decorative helm | defence:20 | defence:5 |
| 4507 | Decorative shield | defence:20 | defence:5 |
| 4508 | Decorative sword | attack:30 | attack:5 |
| 5574 | Initiate sallet | defence:20 | defence:20, prayer:10 |
| 5575 | Initiate hauberk | defence:20 | defence:20, prayer:10 |
| 5576 | Initiate cuisse | defence:20 | defence:10 |
| 6613 | White warhammer | strength:10 | attack:10 |
| 8905 | Black mask (8) | slayer:58, strength:20 | combat:40, defence:10, strength:20 |
| 8907 | Black mask (7) | slayer:58, strength:20 | combat:40, defence:10, strength:20 |
| 8909 | Black mask (6) | slayer:58, strength:20 | combat:40, defence:10, strength:20 |
| 8911 | Black mask (5) | slayer:58, strength:20 | combat:40, defence:10, strength:20 |
| 8915 | Black mask (3) | slayer:58, strength:20 | combat:40, defence:10, strength:20 |
| 8917 | Black mask (2) | slayer:58, strength:20 | combat:40, defence:10, strength:20 |
| 8919 | Black mask (1) | slayer:58, strength:20 | combat:40, defence:10, strength:20 |
| 8921 | Black mask | slayer:58, strength:20 | combat:40, defence:10, strength:20 |
| 9672 | Proselyte sallet | defence:30 | defence:30, prayer:20 |
| 9674 | Proselyte hauberk | defence:30 | defence:30, prayer:20 |
| 9676 | Proselyte cuisse | defence:30 | defence:30, prayer:20 |
| 9678 | Proselyte tasset | defence:30 | defence:30, prayer:20 |
| 10146 | Orange salamander | ranged:50 | attack:50, magic:50, ranged:50 |
| 10147 | Red salamander | ranged:60 | attack:60, magic:60, ranged:60 |
| 10589 | Granite helm | defence:50 | defence:50, strength:50 |
