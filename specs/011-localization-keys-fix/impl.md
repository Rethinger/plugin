# Implementation Plan: Localization Keys Fix

## Overview

This implementation plan addresses localization issues where players see raw message keys instead of proper Russian text. The selected approach is **Option 2: Update Code References** to use existing `chest.items.*` structure instead of restructuring YAML files.

## Technical Context

### Current State
- **Issue**: Players see raw keys like "chest.search.material_found" and "items.stabilization_core.name"
- **Root Cause**: Code expects `chest.items.*` but references `items.*` in multiple locations
- **Selected Solution**: Update code to use existing `chest.items.*` structure

### Files to Modify
1. **ItemManager.java** - Contains item creation logic with `items.*` references
2. **ChestSpawnManager.java** - Contains item name resolution with `items.*` references

## Implementation Phases

### Phase 1: Add Missing Key to messages.yml
**Duration**: 15 minutes
**Priority**: High

**Tasks**:
1. Navigate to `chest.search` section in `src/main/resources/messages.yml`
2. Add missing `material_found` key with Russian message
3. Validate YAML syntax

**Expected Outcome**: 
- Key `chest.search.material_found` resolves to proper Russian text
- No console warnings about missing keys

### Phase 2: Update ItemManager.java References
**Duration**: 20 minutes
**Priority**: High

**Tasks**:
1. Open `src/main/java/com/mmmm/story/managers/ItemManager.java`
2. Locate all `items.*` key references (approximately 24 locations)
3. Replace each with `chest.items.*` equivalent
4. Validate syntax and imports

**Key Changes Required**:
- `items.stabilization_core.name` → `chest.items.stabilization_core.name`
- `items.stabilization_core.lore.0-5` → `chest.items.stabilization_core.lore.0-5`
- `items.act1_skeleton_key.name` → `chest.items.act1_skeleton_key.name`
- `items.act1_skeleton_key.lore.0-3` → `chest.items.act1_skeleton_key.lore.0-3`
- `items.boss1_material.name` → `chest.items.boss1_material.name`
- `items.boss1_material.lore.0-4` → `chest.items.boss1_material.lore.0-4`
- `items.boss1_catalyst.name` → `chest.items.boss1_catalyst.name`
- `items.boss1_catalyst.lore.0-4` → `chest.items.boss1_catalyst.lore.0-4`
- `items.boss1_summon_key.name` → `chest.items.boss1_summon_key.name`
- `items.boss1_summon_key.lore.0-2` → `chest.items.boss1_summon_key.lore.0-2`
- `items.boss2_structure_key.name` → `chest.items.boss2_structure_key.name`
- `items.boss2_structure_key.lore.0-2` → `chest.items.boss2_structure_key.lore.0-2`
- `items.overworld_portal_key.name` → `chest.items.overworld_portal_key.name`
- `items.overworld_portal_key.lore.0-3` → `chest.items.overworld_portal_key.lore.0-3`
- `items.end_artifact.name` → `chest.items.end_artifact.name`
- `items.end_artifact.description` → `chest.items.end_artifact.description`

### Phase 3: Update ChestSpawnManager.java References
**Duration**: 15 minutes
**Priority**: High

**Tasks**:
1. Open `src/main/java/com/mmmm/story/managers/ChestSpawnManager.java`
2. Locate `items.*` key references (approximately 4 locations)
3. Replace each with `chest.items.*` equivalent
4. Validate syntax

**Key Changes Required**:
- `items.stabilization_core.name` → `chest.items.stabilization_core.name`
- `items.boss1_summon_key.name` → `chest.items.boss1_summon_key.name`
- `items.boss1_catalyst.name` → `chest.items.boss1_catalyst.name`
- `items.end_artifact.name` → `chest.items.end_artifact.name`

### Phase 4: Testing and Validation
**Duration**: 30 minutes
**Priority**: High

**Tasks**:
1. Compile project to ensure no syntax errors
2. Start server and test chest search functionality
3. Verify item names display correctly in inventory
4. Check for console warnings about missing keys
5. Test all affected item types

## Risk Assessment

### Low Risk
- **Code Changes Only**: No YAML file modifications required
- **Reversible**: Changes can be easily rolled back if issues arise
- **Isolated**: Only affects item name/lore resolution

### Mitigation Strategies
1. **Backup**: Create backup of both Java files before modification
2. **Incremental Testing**: Test after each file modification
3. **Validation**: Compile and run basic functionality tests
4. **Rollback Plan**: Keep original files ready for quick restoration

## Success Criteria

### Functional Requirements
- [ ] Chest search success message displays properly instead of raw key
- [ ] All item names display correctly in Russian
- [ ] All item lore lines display properly without showing raw keys
- [ ] No existing functionality is broken by changes

### Technical Requirements
- [ ] All item key references use `chest.items.*` pattern
- [ ] No compilation errors introduced
- [ ] MessageManager can successfully resolve all referenced keys
- [ ] No console warnings about missing localization keys

### User Experience Requirements
- [ ] Players see meaningful, localized messages when searching chests
- [ ] Items have proper Russian names and descriptions in inventory
- [ ] No raw localization keys are visible to players
- [ ] All text is properly formatted and readable

## Dependencies

### Required
- [`ItemManager.java`](src/main/java/com/mmmm/story/managers/ItemManager.java)
- [`ChestSpawnManager.java`](src/main/java/com/mmmm/story/managers/ChestSpawnManager.java)
- [`messages.yml`](src/main/resources/messages.yml) - For adding missing key

### Optional
- Development environment for testing
- Test server for validation
- Backup tools for rollback

## Timeline

| Phase | Duration | Start Time | End Time |
|--------|----------|------------|----------|
| Phase 1 | 15 minutes | T+0:15 | T+0:30 |
| Phase 2 | 20 minutes | T+0:30 | T+0:50 |
| Phase 3 | 15 minutes | T+0:50 | T+1:05 |
| Phase 4 | 30 minutes | T+1:05 | T+1:35 |

**Total Estimated Time**: 80 minutes

## Deliverables

1. Updated `ItemManager.java` with all `items.*` → `chest.items.*` changes
2. Updated `ChestSpawnManager.java` with all `items.*` → `chest.items.*` changes
3. Updated `messages.yml` with added `chest.search.material_found` key
4. Test report confirming all localization issues are resolved
5. Documentation of changes for future reference

## Notes

- This approach eliminates risk of YAML migration failures
- Maintains existing file organization and structure
- Requires careful testing to ensure all references are updated
- Focus on maintaining all existing functionality while fixing display issues