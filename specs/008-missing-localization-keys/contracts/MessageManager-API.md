# MessageManager API Contract

**Feature**: 008-missing-localization-keys  
**Component**: MessageManager  
**Date**: October 29, 2025  
**Status**: Existing API - No Changes Required

## Overview

MessageManager is the central localization service that provides translated messages to all plugin components. This contract documents the existing API to ensure all missing keys are added in a compatible manner.

## API Methods (Existing - Reference Only)

### getMessage(String path)

**Description**: Retrieves a translated message for the current plugin's primary language (Russian).

**Signature**:
```java
public String getMessage(String path)
```

**Parameters**:
- `path` (String): Dot-notation localization key (e.g., "chest.search.searching")

**Returns**:
- `String`: Translated message from messages_ru.yml
- Returns the technical key name if translation is missing
- Logs warning if key not found

**Behavior**:
```java
String message = messagesRu.getString(path);
if (message == null) {
    plugin.getLogger().warning("Missing localization key: " + path);
    return path;  // Return technical key name (per clarification)
}
return ChatColor.translateAlternateColorCodes('&', message);
```

**Examples**:
```java
// Simple message retrieval
String searching = messageManager.getMessage("chest.search.searching");
// Returns: "Поиск..." (if key exists) or "chest.search.searching" (if missing)

// Item name retrieval
String itemName = messageManager.getMessage("items.stabilization_core.name");
// Returns: "Ядро стабилизации" (if key exists)
```

---

### getMessage(String path, Map<String, String> replacements)

**Description**: Retrieves a translated message with placeholder replacement.

**Signature**:
```java
public String getMessage(String path, Map<String, String> replacements)
```

**Parameters**:
- `path` (String): Dot-notation localization key
- `replacements` (Map<String, String>): Key-value pairs for {placeholder} substitution

**Returns**:
- `String`: Translated message with placeholders replaced

**Placeholder Syntax**:
- Use `{name}` in YAML (curly braces, no spaces)
- Replacement map keys should match placeholder names exactly
- Unmatched placeholders remain as-is in output

**Examples**:
```yaml
# In messages_ru.yml
chest.search.searching_item: "Поиск {item}..."
```

```java
// Java usage
Map<String, String> replacements = Map.of("item", "Алмаз");
String message = messageManager.getMessage("chest.search.searching_item", replacements);
// Returns: "Поиск Алмаз..."
```

---

### getMessageList(String path)

**Description**: Retrieves a list of translated messages (for multi-line content like item lore).

**Signature**:
```java
public List<String> getMessageList(String path)
```

**Parameters**:
- `path` (String): Dot-notation localization key pointing to a YAML list

**Returns**:
- `List<String>`: List of translated strings
- Returns list containing the key name if not found
- Logs warning if key not found

**Examples**:
```yaml
# In messages_ru.yml
items.stabilization_core.lore:
  - "&7Древний артефакт"
  - "&7Стабилизирует пространство"
  - "&eРедкость: Легендарная"
```

```java
// Java usage
List<String> lore = messageManager.getMessageList("items.stabilization_core.lore");
// Returns: ["&7Древний артефакт", "&7Стабилизирует пространство", "&eРедкость: Легендарная"]
```

---

### sendMessage(Player player, String path)

**Description**: Sends a translated message directly to a player's chat.

**Signature**:
```java
public void sendMessage(Player player, String path)
```

**Parameters**:
- `player` (Player): Target player (language preference ignored - always uses Russian per deprecation)
- `path` (String): Dot-notation localization key

**Behavior**:
- Calls `getMessage(path)` internally
- Sends result to player via `player.sendMessage()`
- Color codes are automatically translated (&c → §c)

**Examples**:
```java
// Send search notification to player
messageManager.sendMessage(player, "chest.search.searching");
// Player sees: "Поиск..." in chat
```

---

### sendMessage(Player player, String path, Map<String, String> replacements)

**Description**: Sends a translated message with placeholders to a player.

**Signature**:
```java
public void sendMessage(Player player, String path, Map<String, String> replacements)
```

**Examples**:
```java
Map<String, String> replacements = Map.of("item", "Алмаз");
messageManager.sendMessage(player, "chest.search.searching_item", replacements);
// Player sees: "Поиск Алмаз..." in chat
```

---

## Deprecated Methods (Do Not Use)

The following methods are deprecated since v1.4.0 (Russian-only update):

```java
@Deprecated
public String getMessage(Player player, String path)
// Use: getMessage(String path) instead

@Deprecated
public String getMessage(String lang, String path)
// Use: getMessage(String path) instead

@Deprecated
public List<String> getMessageList(Player player, String path)
// Use: getMessageList(String path) instead

@Deprecated
public List<String> getMessageList(String lang, String path)
// Use: getMessageList(String path) instead
```

**Deprecation Rationale**: Plugin changed to Russian-only in feature 006. Per-player language selection was temporarily removed but will be restored in a future feature using LanguageManager.

---

## YAML File Structure Contract

### messages_ru.yml (Primary Language)

**Requirements**:
- All keys referenced in Java code MUST exist in this file
- Build fails if any keys are missing (enforced by validation script)
- UTF-8 encoding with BOM
- Hierarchical structure using dot notation

**Example Structure**:
```yaml
# Chest search feature
chest:
  search:
    searching: "Поиск..."
    searching_item: "Поиск {item}..."
    nothing: "Ничего не найдено"
    found: "Найдено: {count} предметов"

# Story items
items:
  stabilization_core:
    name: "Ядро стабилизации"
    lore:
      - "&7Древний артефакт"
      - "&7Стабилизирует пространство"
  
# Menu system
menu:
  story:
    title: "Сюжет"
    back_button: "Назад"
```

### messages_en.yml (Secondary Language)

**Requirements**:
- All keys SHOULD exist (warning if missing, not error)
- Same structure as messages_ru.yml
- Used for English-speaking testers and future internationalization

**Example Structure**:
```yaml
chest:
  search:
    searching: "Searching..."
    searching_item: "Searching for {item}..."
    nothing: "Nothing found"
    found: "Found: {count} items"

items:
  stabilization_core:
    name: "Stabilization Core"
    lore:
      - "&7Ancient artifact"
      - "&7Stabilizes space"

menu:
  story:
    title: "Story"
    back_button: "Back"
```

---

## Missing Key Behavior Contract

### Current Behavior (As-Is)

When a key is not found in messages_ru.yml:

1. **Log Warning**:
   ```
   [WARN]: [ServerStoryPlugin] Missing localization key: chest.search.searching
   ```

2. **Return Technical Key Name**:
   ```java
   String result = messageManager.getMessage("chest.search.searching");
   // Returns: "chest.search.searching" (NOT translated text)
   ```

3. **Display to Player**:
   - Player sees: `chest.search.searching` in chat/UI
   - Makes missing translations immediately obvious (per clarifications)

### Expected Behavior (After This Feature)

1. **No Warnings**: Build validation ensures all keys exist before deployment
2. **All Keys Translated**: Players always see proper Russian/English text
3. **Technical Keys Only for New Features**: Only during active development

---

## Validation Contract

### Build-Time Validation Script

**Input**:
- All `.java` files in `src/main/java/`
- `src/main/resources/messages_ru.yml`
- `src/main/resources/messages_en.yml`

**Process**:
1. Extract all `getMessage("key")` and `getMessageList("key")` calls from Java files
2. Parse messages_ru.yml to get list of defined keys
3. Compare lists to find missing keys

**Output**:
- Exit code 0 if all keys exist in messages_ru.yml
- Exit code 1 if any keys missing in messages_ru.yml (FAIL build)
- Warning message if keys missing in messages_en.yml (non-blocking)

**Example Output**:
```
Validating localization keys...
✓ Found 127 key references in Java code
✓ Found 123 keys in messages_ru.yml
✗ Found 125 keys in messages_en.yml

MISSING IN RUSSIAN (BUILD FAILS):
  - chest.search.searching
  - chest.search.searching_item
  - chest.search.nothing
  - items.stabilization_core.name

MISSING IN ENGLISH (WARNING ONLY):
  - menu.story.advanced_option

Build FAILED: 4 required Russian translation keys are missing.
```

---

## Integration Points

### 1. ChestSpawnManager

**Usage**:
```java
messageManager.sendMessage(player, "chest.search.searching");
messageManager.sendMessage(player, "chest.search.searching_item", Map.of("item", itemName));
messageManager.sendMessage(player, "chest.search.nothing");
```

**Required Keys**:
- `chest.search.searching`
- `chest.search.searching_item`
- `chest.search.nothing`
- `chest.search.found`

### 2. ItemManager

**Usage**:
```java
String itemName = messageManager.getMessage("items.stabilization_core.name");
List<String> lore = messageManager.getMessageList("items.stabilization_core.lore");
```

**Required Keys**:
- `items.{item_id}.name` (for all story items)
- `items.{item_id}.lore` (for all story items with descriptions)

### 3. MenuManager

**Usage**:
```java
String menuTitle = messageManager.getMessage("menu.story.title");
String backButton = messageManager.getMessage("menu.story.back_button");
```

**Required Keys**:
- All `menu.*` keys for unified menu system (from feature 006)

### 4. DialogManager

**Note**: DialogManager uses separate `dialogs_ru.yml` and `dialogs_en.yml` files, not messages files. Same API pattern applies.

---

## Testing Contract

### Unit Testing

No changes to MessageManager code required, but new keys must be tested:

```java
@Test
public void testChestSearchKeys() {
    String searching = messageManager.getMessage("chest.search.searching");
    assertNotNull(searching);
    assertNotEquals("chest.search.searching", searching); // Should be translated
}

@Test
public void testItemNameKeys() {
    String itemName = messageManager.getMessage("items.stabilization_core.name");
    assertNotNull(itemName);
    assertNotEquals("items.stabilization_core.name", itemName);
}
```

### Manual Testing Checklist

1. **Chest Search Flow**:
   - Trigger chest search → verify "Searching..." message in Russian
   - Search for specific item → verify "Searching for {item}..." message
   - Complete search with no results → verify "Nothing found" message

2. **Item Display**:
   - View stabilization core in inventory → verify Russian item name
   - Hover over item → verify lore is in Russian

3. **Language Switching** (if supported):
   - Switch player language to English → verify English translations
   - Switch back to Russian → verify Russian translations

4. **Console Validation**:
   - Check server console → no "Missing localization key" warnings during gameplay

---

## Breaking Changes

**None** - This feature only adds data (new YAML entries). No API changes to MessageManager.

---

## Future Compatibility

When per-player language selection is restored (future feature):
- MessageManager will need to accept Player parameter again (un-deprecate old methods)
- getMessage() will call LanguageManager to get player's preferred language
- This feature ensures both Russian and English translations exist, ready for that change
