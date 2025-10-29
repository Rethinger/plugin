# Data Model: Missing Localization Keys

**Feature**: 008-missing-localization-keys  
**Date**: October 29, 2025  
**Phase**: 1 - Design & Contracts

## Overview

This feature does not introduce new data entities. It extends existing localization data structures by adding missing key-value pairs to YAML files. The data model describes the structure of localization files and the validation metadata.

## Core Entities

### LocalizationKey

**Description**: Unique identifier for a translatable text string used throughout the plugin code.

**Attributes**:
- `key` (String): Dot-notation hierarchical identifier (e.g., "chest.search.searching")
- `area` (String): Top-level functional area (chest, items, dialogs, menu, etc.)
- `feature` (String): Sub-feature within area (search, spawn, display)
- `action` (String): Specific action or element (searching, nothing, name, lore)

**Validation Rules**:
- Key MUST use dot notation (minimum 2 segments)
- Key MUST match pattern: `^[a-z_]+(\.[a-z_]+)+$`
- Key MUST be unique within a language file
- Key MUST NOT contain spaces or special characters except dots and underscores

**Example Keys**:
```
chest.search.searching
chest.search.searching_item
chest.search.nothing
items.stabilization_core.name
items.stabilization_core.lore
menu.story.title
menu.story.back_button
```

**Relationships**:
- Has one Translation per supported Language
- Referenced by zero or more JavaSourceReferences

---

### Translation

**Description**: Language-specific text value for a LocalizationKey.

**Attributes**:
- `key` (String): Reference to LocalizationKey
- `language` (Enum: ru, en): ISO 639-1 language code
- `value` (String): Translated text with optional placeholders
- `type` (Enum: STRING, STRING_LIST): Single string or list for multi-line content
- `context` (String, optional): Usage context notes for translators

**Validation Rules**:
- Value MUST NOT be empty string
- Value MUST NOT contain literal \\n (use YAML multi-line or list instead)
- Placeholders MUST use {name} syntax (curly braces)
- Color codes MUST use & prefix (e.g., &c for red)
- Russian translation (ru) is REQUIRED for all keys
- English translation (en) is RECOMMENDED but not blocking

**Example**:
```yaml
# Single string translation
chest.search.searching: "Поиск..."

# Translation with placeholder
chest.search.searching_item: "Поиск {item}..."

# Multi-line translation (list type)
items.stabilization_core.lore:
  - "&7Древний артефакт"
  - "&7Стабилизирует пространство"
```

**Relationships**:
- Belongs to one LocalizationKey
- Belongs to one Language
- Loaded by MessageManager or LanguageManager at runtime

---

### JavaSourceReference

**Description**: Location in Java source code where a LocalizationKey is referenced.

**Attributes**:
- `file` (String): Relative path to Java file (e.g., "src/main/java/com/mmmm/story/managers/ChestSpawnManager.java")
- `line` (Integer): Line number where key is used
- `method` (String): Method name containing the reference
- `key` (String): Referenced LocalizationKey
- `usage_type` (Enum: MESSAGE, MESSAGE_LIST): Whether key loads single string or list

**Validation Rules**:
- File path MUST exist in repository
- Line number MUST be positive integer
- Key MUST correspond to an actual LocalizationKey

**Example**:
```
File: src/main/java/com/mmmm/story/managers/ChestSpawnManager.java
Line: 145
Method: searchChests
Key: chest.search.searching
Usage: messageManager.getMessage("chest.search.searching")
```

**Relationships**:
- References one LocalizationKey
- Identified during code scanning phase

---

### LocalizationFile

**Description**: YAML resource file containing translation key-value pairs for a specific language.

**Attributes**:
- `path` (String): Relative path from resources root (e.g., "messages_ru.yml")
- `language` (Enum: ru, en): Language code
- `type` (Enum: MESSAGES, DIALOGS): File purpose category
- `encoding` (String): File character encoding (always UTF-8)
- `key_count` (Integer): Number of keys in file

**Validation Rules**:
- Path MUST follow naming convention: `{type}_{language}.yml`
- File MUST be valid YAML syntax
- File MUST be UTF-8 encoded with BOM
- File MUST contain at least one key
- Primary language file (messages_ru.yml) MUST have all keys referenced in code

**File Hierarchy**:
```
src/main/resources/
├── messages_ru.yml      # Primary language - all keys REQUIRED
├── messages_en.yml      # Secondary language - all keys RECOMMENDED
├── dialogs_ru.yml       # Story dialog translations (Russian)
└── dialogs_en.yml       # Story dialog translations (English)
```

**Relationships**:
- Contains many Translations
- Loaded by ConfigManager on plugin enable
- Validated by build script before compilation

---

## Validation Metadata

### KeyValidationReport

**Description**: Result of automated key validation scan (build-time or runtime).

**Attributes**:
- `scan_timestamp` (DateTime): When validation was performed
- `total_java_references` (Integer): Count of keys found in Java code
- `total_yaml_keys_ru` (Integer): Count of keys in messages_ru.yml
- `total_yaml_keys_en` (Integer): Count of keys in messages_en.yml
- `missing_in_russian` (List<String>): Keys referenced in code but missing from messages_ru.yml
- `missing_in_english` (List<String>): Keys referenced in code but missing from messages_en.yml
- `unused_keys_ru` (List<String>): Keys in messages_ru.yml not referenced in code
- `unused_keys_en` (List<String>): Keys in messages_en.yml not referenced in code
- `validation_status` (Enum: PASS, FAIL, WARNING): Overall result

**Validation Rules**:
- Status = FAIL if missing_in_russian is not empty
- Status = WARNING if missing_in_english is not empty AND missing_in_russian is empty
- Status = PASS if both lists are empty

**Example Output**:
```
Localization Key Validation Report
===================================
Scan Time: 2025-10-29 14:30:00
Java References Found: 127
Russian Keys Found: 123
English Keys Found: 125

MISSING IN RUSSIAN (CRITICAL):
  - chest.search.searching
  - chest.search.searching_item
  - chest.search.nothing
  - items.stabilization_core.name

MISSING IN ENGLISH (WARNING):
  - menu.story.new_option
  - menu.story.advanced_setting

Status: FAIL (4 critical missing keys in Russian)
```

---

## Data Flow

### 1. Build-Time Validation Flow

```
[Java Source Files]
        ↓
[validate-localization-keys.ps1 script]
        ↓ (extract keys via regex)
[List of JavaSourceReferences]
        ↓
[Compare with messages_ru.yml]
        ↓
[KeyValidationReport generated]
        ↓
[FAIL build if missing_in_russian not empty]
```

### 2. Runtime Localization Flow

```
[Player triggers action]
        ↓
[Listener/Manager calls messageManager.getMessage(key)]
        ↓
[MessageManager looks up key in messagesRu ConfigurationSection]
        ↓
[If found] → Return translated string
[If not found] → Log warning + return technical key name
        ↓
[Display to player]
```

### 3. Translation Addition Flow

```
[Identify missing key from validation report]
        ↓
[Translator adds entry to messages_ru.yml and messages_en.yml]
        ↓
[Commit changes]
        ↓
[Build script validates completeness]
        ↓
[Plugin reload loads new translations]
```

---

## State Transitions

### LocalizationKey States

- **MISSING**: Key referenced in code but not in YAML files (CRITICAL if missing in Russian)
- **PARTIALLY_TRANSLATED**: Key exists in Russian but missing in English (WARNING state)
- **FULLY_TRANSLATED**: Key exists in both Russian and English (IDEAL state)
- **UNUSED**: Key exists in YAML but not referenced in code (CLEANUP recommended)

### Translation File States

- **INCOMPLETE**: Missing keys that are referenced in code
- **COMPLETE**: All referenced keys have translations
- **OVERSIZED**: Contains unused keys (cleanup recommended)

---

## Implementation Notes

### Existing Systems (No Changes Required)

- **MessageManager**: Already handles key lookup, placeholder replacement, and missing key warnings
- **LanguageManager**: Already handles per-player language selection
- **ConfigManager**: Already loads YAML files on plugin enable

### New Components Required

- **validate-localization-keys.ps1**: PowerShell script for build-time validation
- **Maven exec plugin configuration**: Runs validation in build lifecycle
- **Translation glossary**: Reference document for terminology consistency

### Backwards Compatibility

- Adding new keys to YAML files is backwards compatible
- Existing keys and translations remain unchanged
- No API changes to MessageManager or LanguageManager required
- Hot-reload of YAML files already supported by existing code

---

## Performance Considerations

- **Memory**: Each key-value pair ~100 bytes, 200 keys = ~20KB in memory (negligible)
- **Load Time**: YAML parsing on plugin enable, <100ms for all files
- **Lookup Time**: HashMap lookup O(1), <1ms per getMessage() call
- **Validation Time**: Build script runs <1 second, no runtime impact
