# Feature Specification: Fix Localization Keys for Chest Search and Item Names

## Problem Statement

Users are experiencing localization issues where:
1. When successfully searching a chest, the message shows "chest.search.material_found" instead of proper text
2. Items display raw localization keys like "items.stabilization_core.name" instead of actual item names
3. Item descriptions show as "items.stabilization_core.lore.0" through "items.stabilization_core.lore.5" instead of actual descriptions

## Root Cause Analysis

### Issue 1: Missing Localization Key
- **Location**: [`ChestSpawnManager.java:350`](src/main/java/com/mmmm/story/managers/ChestSpawnManager.java:350)
- **Problem**: Code references `chest.search.material_found` key which doesn't exist in [`messages.yml`](src/main/resources/messages.yml)
- **Impact**: Players see raw key instead of success message when materials are found

### Issue 2: Incorrect Key Paths
- **Location**: Multiple files in [`ItemManager.java`](src/main/java/com/mmmm/story/managers/ItemManager.java) and [`ChestSpawnManager.java`](src/main/java/com/mmmm/story/managers/ChestSpawnManager.java)
- **Problem**: Code expects item keys under `items.*` but they're defined under `chest.items.*` in messages.yml
- **Affected Items**:
  - `items.stabilization_core.name` and `items.stabilization_core.lore.0-5`
  - `items.act1_skeleton_key.name` and `items.act1_skeleton_key.lore.0-3`
  - `items.boss1_material.name` and `items.boss1_material.lore.0-4`
  - `items.boss1_catalyst.name` and `items.boss1_catalyst.lore.0-4`
  - `items.boss1_summon_key.name` and `items.boss1_summon_key.lore.0-2`
  - `items.boss2_structure_key.name` and `items.boss2_structure_key.lore.0-2`
  - `items.overworld_portal_key.name` and `items.overworld_portal_key.lore.0-3`
  - `items.end_artifact.name` and `items.end_artifact.description`

## Solution Approach

### Option 1: Update messages.yml Structure
Move all item definitions from `chest.items.*` to `items.*` to match what the code expects:

**Current Structure:**
```yaml
chest:
  items:
    stabilization_core:
      name: "Ядро Стабилизации"
      lore:
        - "Древний артефакт..."
```

**Target Structure:**
```yaml
items:
  stabilization_core:
    name: "Ядро Стабилизации"
    lore:
      - "Древний артефакт..."
```

### Option 2: Update Code References (Selected Approach)
Modify all code to use `chest.items.*` instead of `items.*`

**Decision**: Option 2 is selected because:
- Eliminates risk of YAML migration failures
- No structural changes to configuration files required
- Maintains existing file organization
- Safer approach with lower implementation risk

## Implementation Plan

### Phase 1: Fix Missing Key
1. Add missing `chest.search.material_found` key to messages.yml
2. Provide appropriate Russian message for material found notification

### Phase 2: Restructure Item Keys
1. Move all item definitions from `chest.items.*` to `items.*` in messages.yml
2. Ensure all lore lines are properly numbered (0, 1, 2, etc.)
3. Verify all item names and descriptions are preserved

### Phase 3: Validation
1. Test chest search functionality to ensure proper messages display
2. Verify all items show correct names and descriptions in inventory
3. Confirm no other localization keys are broken

## Success Criteria

### Functional Requirements
- [ ] Chest search success message displays properly instead of raw key
- [ ] All item names display correctly in Russian
- [ ] All item lore lines display properly without showing raw keys
- [ ] No existing functionality is broken by the changes

### Technical Requirements
- [ ] All item keys follow consistent `items.item_name.property` pattern
- [ ] All lore lines are properly indexed starting from 0
- [ ] MessageManager can successfully resolve all referenced keys
- [ ] No console warnings about missing localization keys

### User Experience Requirements
- [ ] Players see meaningful, localized messages when searching chests
- [ ] Items have proper Russian names and descriptions in inventory
- [ ] No raw localization keys are visible to players
- [ ] All text is properly formatted and readable

## Risk Assessment

### Low Risk
- Moving item keys in messages.yml only affects message resolution
- No game logic changes required
- Changes are reversible if issues arise

### Mitigation Strategies
- Backup current messages.yml before making changes
- Test with all item types to ensure nothing is broken
- Verify both chest search and inventory display work correctly

## Testing Strategy

### Manual Testing
1. **Chest Search Test**:
   - Open chests in ruined portals, fortresses, bastions, and end cities
   - Verify success messages display properly
   - Confirm no raw keys are shown

2. **Item Display Test**:
   - Acquire each type of story item
   - Check item names in inventory
   - Verify all lore lines display correctly
   - Test with different item quantities

3. **Regression Test**:
   - Verify existing chest search mechanics still work
   - Confirm item functionality remains intact
   - Check other game systems still display messages correctly

### Automated Validation
- Verify MessageManager can resolve all referenced keys
- Check for any console warnings about missing keys
- Validate YAML syntax after changes

## Dependencies

- [`MessageManager.java`](src/main/java/com/mmmm/story/managers/MessageManager.java) - No changes required
- [`messages.yml`](src/main/resources/messages.yml) - Primary file to modify
- [`ItemManager.java`](src/main/java/com/mmmm/story/managers/ItemManager.java) - No changes required with Option 1
- [`ChestSpawnManager.java`](src/main/java/com/mmmm/story/managers/ChestSpawnManager.java) - No changes required with Option 1

## Timeline Estimate

- **Phase 1** (Missing key): 15 minutes
- **Phase 2** (Restructure keys): 30 minutes
- **Phase 3** (Testing): 45 minutes
- **Total estimated time**: 90 minutes

## Clarifications

### Session 2025-11-04
- Q: What should happen if migration process fails partway through (e.g., YAML becomes invalid, some items get lost, or server won't start)? → A: Skip migration, use code fix instead

## Deliverables

1. Updated code files with:
   - Added `chest.search.material_found` key to messages.yml
   - Modified all item key references from `items.*` to `chest.items.*` in ItemManager.java and ChestSpawnManager.java
2. Test report confirming all localization issues are resolved
3. Documentation of changes for future reference