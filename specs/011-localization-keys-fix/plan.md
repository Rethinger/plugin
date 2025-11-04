# Implementation Plan: Localization Keys Fix

## Overview

This plan addresses the localization issues where players see raw message keys instead of proper Russian text when searching chests and viewing items.

## Implementation Steps

### Step 1: Add Missing Chest Search Message
**File**: [`src/main/resources/messages.yml`](src/main/resources/messages.yml)
**Location**: Under `chest.search` section (around line 74-82)

**Action**: Add the missing `material_found` key:
```yaml
chest:
  search:
    # ... existing keys ...
    material_found: "✓ Материал найден в этом сундуке!"
```

### Step 2: Update Code References
**Files**: [`ItemManager.java`](src/main/java/com/mmmm/story/managers/ItemManager.java) and [`ChestSpawnManager.java`](src/main/java/com/mmmm/story/managers/ChestSpawnManager.java)
**Current Location**: Multiple locations referencing `items.*`
**Target Location**: Use existing `chest.items.*` structure

**Action**: Modify all item key references from `items.*` to `chest.items.*`:

**Before:**
```java
String coreName = plugin.getMessageManager().getMessage(lang, "items.stabilization_core.name");
```

**After:**
```java
String coreName = plugin.getMessageManager().getMessage(lang, "chest.items.stabilization_core.name");
```

### Step 3: Remove Old Item Section
**Action**: Remove the entire `chest.items` section after moving all items to the new `items` section

### Step 4: Verify All Item Keys
**Items to Move:**
1. `stabilization_core` (with lore.0 through lore.5)
2. `act1_skeleton_key` (with lore.0 through lore.3)
3. `boss1_material` (with lore.0 through lore.4)
4. `boss1_catalyst` (with lore.0 through lore.4)
5. `boss1_summon_key` (with lore.0 through lore.2)
6. `boss2_structure_key` (with lore.0 through lore.2)
7. `overworld_portal_key` (with lore.0 through lore.3)
8. `end_artifact` (with name and description)
9. `end_artifact_1` through `end_artifact_5` (with name and lore)

## Detailed Changes

### New `items` Section Structure
```yaml
items:
  # Core Items
  stabilization_core:
    name: "Ядро Стабилизации"
    lore:
      - "Древний артефакт, способный"
      - "стабилизировать портальные соединения"
      - ""
      - "✦ Найдено в сундуках"
      - "разрушенных порталов"
      - ""
      - "▶ Бросьте (Q) на блок обсидиана"
      - "чтобы активировать порталы в Ад"

  act1_skeleton_key:
    name: "Скелетный Ключ"
    lore:
      - "Древний ключ, полученный от"
      - "Повелителя Скелетов"
      - ""
      - "✦ Открывает путь к более"
      - "глубоким тайнам"
      - ""
      - "▶ Используйте на алтаре призыва"

  # ... (continue for all other items)
```

### Updated `chest.search` Section
```yaml
chest:
  search:
    searching: "🔍 Поиск сундука..."
    searching_item: "Поиск предмета: {0}"
    found: "✓ НАЙДЕНО: {0}!"
    success: "✓ УСПЕХ!"
    nothing: "✗ В этом сундуке ничего нет ({0}/{1})"
    broke: "✗ Сундук сломался после 3 неудачных попыток!"
    cooldown: "✗ Этот сундук уже недавно обыскивался ({0} сек)"
    material_found: "✓ Материал найден в этом сундуке!"
```

## Validation Steps

### 1. YAML Syntax Validation
- Ensure proper indentation
- Verify all quotes are matched
- Check for any syntax errors

### 2. Key Completeness Check
- Verify all referenced keys exist
- Ensure no duplicate keys
- Check all lore lines are numbered correctly

### 3. Functional Testing
- Test chest search in all structure types
- Verify item names display correctly
- Confirm item descriptions show properly

## Risk Mitigation

### Backup Strategy
1. Create backup of original `messages.yml`
2. Test changes in development environment first
3. Keep rollback plan ready

### Testing Strategy
1. **Unit Tests**: Verify MessageManager can resolve all keys
2. **Integration Tests**: Test chest search and item display
3. **User Acceptance**: Verify player experience

## Implementation Order

1. **Phase 1** (15 min): Add missing `chest.search.material_found` key
2. **Phase 2** (30 min): Move all item definitions to new `items` section
3. **Phase 3** (15 min): Remove old `chest.items` section
4. **Phase 4** (30 min): Test and validate all changes

## Success Criteria

### Immediate Success
- [ ] No raw localization keys visible to players
- [ ] Chest search shows proper success message
- [ ] All items display correct Russian names
- [ ] All item lore lines display properly

### Long-term Success
- [ ] No console warnings about missing keys
- [ ] All localization functions work correctly
- [ ] Player experience is seamless and professional

## Rollback Plan

If issues arise:
1. Restore original `messages.yml` from backup
2. Restart plugin/server
3. Verify functionality returns to previous state
4. Investigate root cause of failure

## Dependencies

- **MessageManager**: No changes required
- **ItemManager**: No changes required (uses existing key structure)
- **ChestSpawnManager**: No changes required (uses existing key structure)
- **messages.yml**: Primary file to modify

## Notes

- This fix addresses the root cause (key structure mismatch)
- No code changes required, only configuration changes
- Maintains all existing functionality while fixing display issues
- Improves overall user experience significantly