# Dynamic Refresh Research — bank-slot-sync vs SkillBankPlugin

Brief #82. Research-only — no code changes.

Source: `https://raw.githubusercontent.com/kitten-lissy/bank-slot-sync/master/src/main/java/com/bankslotsync/BankSlotSyncPlugin.java` (530 lines, fetched into `/tmp/bank-slot-sync.java`).

## bank-slot-sync answers

### 1. What event(s) does it subscribe to for detecting bank changes?

Two events:

- `ItemContainerChanged` for **detection** (line 91)
- `GameTick` for **execution** (line 131)

```java
// Line 91-129
@Subscribe
public void onItemContainerChanged(ItemContainerChanged event)
{
    if (event.getContainerId() != InventoryID.BANK.getId())
    {
        return;
    }

    ItemContainer bankContainer = event.getItemContainer();
    if (bankContainer == null) { return; }

    Set<Integer> currentBankItems = new HashSet<>();
    for (Item item : bankContainer.getItems())
    {
        if (item.getId() > 0) { currentBankItems.add(item.getId()); }
    }

    Set<Integer> newItems = new HashSet<>(currentBankItems);
    newItems.removeAll(previousBankItems);

    if (!newItems.isEmpty())
    {
        pendingNewItems.addAll(newItems);
        pendingAllBankItems = new HashSet<>(currentBankItems);
        processPending = true;
    }
    previousBankItems = currentBankItems;
}
```

### 2. Is there a tick delay before modifying the layout? How many ticks? How is it implemented?

**Yes — 1 GameTick delay.** Implementation uses a `processPending` boolean flag set in `onItemContainerChanged` and consumed in `onGameTick`:

```java
// Line 69-71
private Set<Integer> pendingNewItems = new HashSet<>();
private Set<Integer> pendingAllBankItems = new HashSet<>();
private boolean processPending = false;

// Line 131-150
@Subscribe
public void onGameTick(GameTick event)
{
    if (!processPending) { return; }
    processPending = false;

    processNewItems(new HashSet<>(pendingNewItems), pendingAllBankItems);
    pendingNewItems.clear();
    pendingAllBankItems.clear();

    if (tabInterface.getActiveTag() != null)
    {
        tabInterface.reloadActiveTab();
    }
}
```

The comment on line 68 calls this out explicitly: `// Pending items to process (1-tick delay to let Bank Tags finish first)`.

### 3. What exact sequence of API calls does it make to update the layout?

```java
// Line 477-494 — READ
String getLayoutConfig(String tagName)
{
    String standardizedTag = Text.standardize(tagName);
    // First try the external Bank Tag Layouts plugin
    String layout = configManager.getConfiguration(
        "banktaglayouts", "layout_" + standardizedTag);
    if (layout != null && !layout.isEmpty()) { return layout; }
    // Fall back to built-in Bank Tags
    return configManager.getConfiguration(
        "banktags", "layout_" + standardizedTag);
}

// Line 500-518 — WRITE
void saveLayoutConfig(String tagName, String layoutStr)
{
    String standardizedTag = Text.standardize(tagName);
    String externalLayout = configManager.getConfiguration(
        "banktaglayouts", "layout_" + standardizedTag);
    if (externalLayout != null && !externalLayout.isEmpty())
    {
        configManager.setConfiguration(
            "banktaglayouts", "layout_" + standardizedTag, layoutStr);
    }
    else
    {
        configManager.setConfiguration(
            "banktags", "layout_" + standardizedTag, layoutStr);
    }
}
```

**Per-tag flow:**

1. `Text.standardize(tagName)` — bank-tags expects lowercase + trimmed
2. Probe `banktaglayouts.layout_<tag>` to see if Bank Tag Layouts owns it
3. Read existing layout via `getLayoutConfig` (external first, built-in fallback)
4. `LayoutParser.parseLayout(layoutStr)` — parse into mutable form
5. Mutate (insert/move items)
6. `layout.serialize()` — back to string
7. **`configManager.setConfiguration(group, "layout_" + tag, layoutStr)`** — write to the SAME group the layout was read from
8. After the per-tag loop finishes (still inside `onGameTick`), call `tabInterface.reloadActiveTab()` ONCE if there's an active tag

### 4. What guard conditions does it check before running?

In `onItemContainerChanged`:
- `event.getContainerId() != InventoryID.BANK.getId()` — bank container only
- `bankContainer == null` — defensive null check
- `newItems.isEmpty()` — only flags pending if there are actually new items (not deletions)

In `onGameTick`:
- `!processPending` — early return when no work pending
- `tabInterface.getActiveTag() != null` — reload only if a tag is active

In `processNewItems`:
- `tagTabs == null || tagTabs.length == 0`
- `tagName == null || tagName.isEmpty()`
- `layoutStr == null || layoutStr.isEmpty()` (skip tags without a layout)

### 5. Does it run on the client thread? How?

**Implicitly yes** — `@Subscribe` handlers for game events (`ItemContainerChanged`, `GameTick`) are dispatched by RuneLite's event bus on the client thread. No `clientThread.invokeLater` calls anywhere in the file. No injection of `ClientThread`.

Imports (line 1-23) — notably **no `import net.runelite.client.callback.ClientThread`**.

---

## SkillBankPlugin answers

### 1. What event(s) does it subscribe to for detecting bank changes?

Same two: `ItemContainerChanged` ([SkillBankPlugin.java:584](src/main/java/com/skillbank/SkillBankPlugin.java#L584)) for detection + `GameTick` ([SkillBankPlugin.java:607](src/main/java/com/skillbank/SkillBankPlugin.java#L607)) for execution.

```java
@Subscribe
public void onItemContainerChanged(ItemContainerChanged event)
{
    if (event.getContainerId() != InventoryID.BANK) { return; }
    if (!tabInterface.isTagTabActive()) { return; }
    String activeTag = tabInterface.getActiveTag();
    if (activeTag == null || !SkillBankData.tags().containsKey(activeTag)) { return; }
    pendingRebuildTag = activeTag;
}
```

### 2. Is there a tick delay before modifying the layout? How many ticks? How is it implemented?

**Yes — 1 GameTick delay.** Identical pattern: volatile string field set in detection handler, consumed in GameTick handler.

```java
private volatile String pendingRebuildTag;

@Subscribe
public void onGameTick(GameTick event)
{
    if (pendingRebuildTag == null) { return; }
    pendingRebuildTag = null;
    rebuildAndReloadActiveTab(null);
}
```

### 3. What exact sequence of API calls does it make to update the layout?

```java
// Line 528-552
private void buildAndSaveLayout(String tagName)
{
    ItemContainer bank = client.getItemContainer(InventoryID.BANK);
    if (bank == null) { return; }

    Set<Integer> ownedCanonical = new HashSet<>();
    for (Item it : bank.getItems())
    {
        if (it == null) { continue; }
        int id = it.getId();
        if (id > 0) { ownedCanonical.add(itemManager.canonicalize(id)); }
    }

    Layout layout = layoutBuilder.buildLayout(tagName, ownedCanonical);
    layoutManager.saveLayout(layout);
}

// Line 620-625
private void rebuildAndReloadActiveTab(String expectedTag)
{
    // ... guards ...
    buildAndSaveLayout(currentTag);
    tabInterface.reloadActiveTab();
}
```

**Per-tag flow:**

1. Read bank container, canonicalize item IDs
2. Build a `Layout` object via our own `layoutBuilder.buildLayout(...)`
3. **`layoutManager.saveLayout(layout)`** — call into `net.runelite.client.plugins.banktags.tabs.LayoutManager`
4. `tabInterface.reloadActiveTab()`

We rely on `LayoutManager.saveLayout(Layout)`. We never touch `configManager.setConfiguration("banktaglayouts", ...)` or even `configManager.setConfiguration("banktags", "layout_X", ...)`.

### 4. What guard conditions does it check before running?

In `onItemContainerChanged`:
- `event.getContainerId() != InventoryID.BANK`
- `!tabInterface.isTagTabActive()`
- `activeTag == null || !SkillBankData.tags().containsKey(activeTag)` (must be one of our 22 tabs)

In `onGameTick`:
- `pendingRebuildTag == null`

In `rebuildAndReloadActiveTab`:
- `!tabInterface.isTagTabActive()`
- `currentTag == null || !SkillBankData.tags().containsKey(currentTag)`

### 5. Does it run on the client thread? How?

Same as bank-slot-sync — implicit via `@Subscribe` on game events. No explicit `clientThread.invokeLater` wrapping the body of either handler.

---

## Comparison — what we do differently

| # | Aspect | bank-slot-sync | SkillBankPlugin | Significance |
|---|---|---|---|---|
| 1 | **Layout write API** | `configManager.setConfiguration(group, "layout_" + tag, str)` — direct config write | `layoutManager.saveLayout(Layout)` — bank-tags high-level API | **Likely the bug.** See below. |
| 2 | **Config group routing** | Probes `banktaglayouts` first, falls back to `banktags`. Writes to whichever group already has the layout. | Single-call into `LayoutManager` — destination opaque (almost certainly `banktags`, never `banktaglayouts`). | **Likely the bug.** See below. |
| 3 | **Layout source** | Reads + mutates existing layout string in place via `LayoutParser` | Rebuilds layout from scratch every tick (full sort) | Cosmetic — both write a complete layout |
| 4 | **Tag name normalization** | `Text.standardize(tagName)` before reading/writing | None (our tags are already lowercase) | Probably harmless since our tags are pre-normalized |
| 5 | **Per-tab vs active-tab** | Iterates ALL tagged tabs every tick when pending | Only rebuilds the currently-visible tab | Different scope — ours is narrower |
| 6 | **`InventoryID.BANK` API** | `InventoryID.BANK.getId()` (old enum) | `net.runelite.api.gameval.InventoryID.BANK` (new gameval int) | Just a different API surface for the same constant |
| 7 | **`reloadActiveTab` invocation** | Once at end of `onGameTick` if a tag is active | Inside `rebuildAndReloadActiveTab` | Equivalent — we only rebuild the active tab |

## The likely bug

**`LayoutManager.saveLayout()` writes to `banktags` config; Bank Tag Layouts reads from `banktaglayouts` config.**

When the player has Bank Tag Layouts (plugin hub) installed AND "Enable layout by default" turned on (the configuration we explicitly check for in the setup wizard), Bank Tag Layouts stores its layout in the `banktaglayouts.layout_<tag>` config key — not in bank-tags' built-in `banktags.layout_<tag>`. When the bank renders, Bank Tag Layouts intercepts and uses ITS layout, not bank-tags'.

`LayoutManager` is from `net.runelite.client.plugins.banktags.tabs` — built-in bank-tags. There's no reason to believe `saveLayout()` writes anywhere except `banktags.layout_<tag>`. So our `saveLayout` call updates a config key that Bank Tag Layouts never reads.

Bank Tag Layouts then renders its OWN stored layout (missing the new item) and places the newly-deposited item at the first free slot (slot 0) — which matches the symptom Matt is reporting.

bank-slot-sync's `saveLayoutConfig` (line 500-518) explicitly probes both groups and writes to whichever already owns the layout. That's why it works with Bank Tag Layouts installed and we don't.

## Recommended fix direction (NOT applied — research-only brief)

Replace `layoutManager.saveLayout(layout)` with a direct config write that mirrors bank-slot-sync's probe-and-write pattern:

```java
String standardizedTag = Text.standardize(tagName);
String layoutStr = layout.toArray()...serializeSomehow();  // need to figure out serialization
String externalLayout = configManager.getConfiguration(
    "banktaglayouts", "layout_" + standardizedTag);
String group = (externalLayout != null && !externalLayout.isEmpty())
    ? "banktaglayouts" : "banktags";
configManager.setConfiguration(group, "layout_" + standardizedTag, layoutStr);
```

Open question: how to convert our `Layout` object back to the wire string format. bank-slot-sync uses its own `LayoutParser.Layout.serialize()` because it always works with strings — we work with the typed `Layout`. Two paths:

1. Reflect / inspect the bank-tags `Layout` class for a serialize hook
2. Build the layout string ourselves from `(itemId, position)` pairs (the format is fixed: comma-separated `pos:itemId` per bank-slot-sync's parser)

The latter is cleaner because we already know each item's intended position. The wire format per inspection of `LayoutParser` would need to be confirmed.
