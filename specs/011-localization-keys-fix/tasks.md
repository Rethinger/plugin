# Implementation Tasks: Localization Keys Fix

## Task Overview

This document breaks down the localization fix into specific, actionable tasks for implementation based on the selected approach (Option 2: Update Code References).

## User Stories

### US1: Add Missing Chest Search Message
**Priority**: P1
**Description**: As a player, I want to see proper Russian messages when searching chests instead of raw localization keys

### US2: Update Item Name References
**Priority**: P1
**Description**: As a player, I want to see proper Russian item names in my inventory instead of raw keys like "items.stabilization_core.name"

## Task List

### Phase 1: Setup Tasks

#### T001: Create Backup
**Priority**: High
**Story**: US1, US2
**Description**: Create backup of original files before making changes
**File**: N/A (manual backup)
**Estimated Time**: 5 minutes

**Implementation Tasks**:
- [ ] Create backup of `src/main/resources/messages.yml`
- [ ] Create backup of `src/main/java/com/mmmm/story/managers/ItemManager.java`
- [ ] Create backup of `src/main/java/com/mmmm/story/managers/ChestSpawnManager.java`

**Acceptance Criteria**:
- [ ] All backup files created successfully
- [ ] Backup files stored in safe location

---

#### T002: Add Missing Key to messages.yml
**Priority**: High
**Story**: US1
**Description**: Add the missing chest.search.material_found key to display proper success message
**File**: `src/main/resources/messages.yml`
**Estimated Time**: 10 minutes

**Implementation Tasks**:
- [ ] Navigate to `chest.search` section (around line 74)
- [ ] Add missing `material_found` key with Russian message: "✓ Материал найден в этом сундуке!"
- [ ] Validate YAML syntax

**Acceptance Criteria**:
- [ ] Key is properly indented under `chest.search`
- [ ] Message text is in Russian and makes sense contextually
- [ ] YAML syntax is valid

---

### Phase 2: Foundational Tasks

#### T003: Update ItemManager.java References
**Priority**: High
**Story**: US2
**Description**: Update all item key references to use chest.items.* instead of items.*
**File**: `src/main/java/com/mmmm/story/managers/ItemManager.java`
**Estimated Time**: 20 minutes

**Implementation Tasks**:
- [ ] Locate all `items.*` key references (approximately 24 locations)
- [ ] Replace each with `chest.items.*` equivalent
- [ ] Validate syntax and imports

**Key Changes Required**:
- [ ] `items.stabilization_core.name` → `chest.items.stabilization_core.name`
- [ ] `items.stabilization_core.lore.0-5` → `chest.items.stabilization_core.lore.0-5`
- [ ] `items.act1_skeleton_key.name` → `chest.items.act1_skeleton_key.name`
- [ ] `items.act1_skeleton_key.lore.0-3` → `chest.items.act1_skeleton_key.lore.0-3`
- [ ] `items.boss1_material.name` → `chest.items.boss1_material.name`
- [ ] `items.boss1_material.lore.0-4` → `chest.items.boss1_material.lore.0-4`
- [ ] `items.boss1_catalyst.name` → `chest.items.boss1_catalyst.name`
- [ ] `items.boss1_catalyst.lore.0-4` → `chest.items.boss1_catalyst.lore.0-4`
- [ ] `items.boss1_summon_key.name` → `chest.items.boss1_summon_key.name`
- [ ] `items.boss1_summon_key.lore.0-2` → `chest.items.boss1_summon_key.lore.0-2`
- [ ] `items.boss2_structure_key.name` → `chest.items.boss2_structure_key.name`
- [ ] `items.boss2_structure_key.lore.0-2` → `chest.items.boss2_structure_key.lore.0-2`
- [ ] `items.overworld_portal_key.name` → `chest.items.overworld_portal_key.name`
- [ ] `items.overworld_portal_key.lore.0-3` → `chest.items.overworld_portal_key.lore.0-3`
- [ ] `items.end_artifact.name` → `chest.items.end_artifact.name`
- [ ] `items.end_artifact.description` → `chest.items.end_artifact.description`

**Acceptance Criteria**:
- [ ] All item key references updated to use `chest.items.*`
- [ ] No syntax errors introduced
- [ ] All lore line references preserved (0, 1, 2, etc.)

---

#### T004: Update ChestSpawnManager.java References
**Priority**: High
**Story**: US2
**Description**: Update all item key references to use chest.items.* instead of items.*
**File**: `src/main/java/com/mmmm/story/managers/ChestSpawnManager.java`
**Estimated Time**: 15 minutes

**Implementation Tasks**:
- [ ] Locate all `items.*` key references (approximately 4 locations)
- [ ] Replace each with `chest.items.*` equivalent
- [ ] Validate syntax

**Key Changes Required**:
- [ ] `items.stabilization_core.name` → `chest.items.stabilization_core.name`
- [ ] `items.boss1_summon_key.name` → `chest.items.boss1_summon_key.name`
- [ ] `items.boss1_catalyst.name` → `chest.items.boss1_catalyst.name`
- [ ] `items.end_artifact.name` → `chest.items.end_artifact.name`

**Acceptance Criteria**:
- [ ] All item key references updated to use `chest.items.*`
- [ ] No syntax errors introduced
- [ ] All lore line references preserved

---

### Phase 3: Testing Tasks

#### T005: Functional Testing
**Priority**: High
**Story**: US1, US2
**Description**: Test that chest search shows proper messages and items display correctly
**Estimated Time**: 30 minutes

**Implementation Tasks**:
- [ ] Compile project to ensure no syntax errors
- [ ] Start server and test chest search functionality
- [ ] Verify success message displays properly instead of raw key
- [ ] Test item names display correctly in inventory
- [ ] Check for console warnings about missing keys

**Acceptance Criteria**:
- [ ] "✓ Материал найден в этом сундуке!" displays instead of raw key
- [ ] No error messages in console
- [ ] Search mechanics work as expected

---

#### T006: Regression Testing
**Priority**: High
**Story**: US1, US2
**Description**: Test that other game systems still work correctly after changes
**Estimated Time**: 15 minutes

**Implementation Tasks**:
- [ ] Test NPC dialogs work
- [ ] Check menu systems
- [ ] Test other chat messages
- [ ] Verify other game systems that use messages.yml

**Acceptance Criteria**:
- [ ] No other systems broken by changes
- [ ] All other messages display correctly
- [ ] No console warnings about missing keys

---

### Phase 4: Polish Tasks

#### T007: Documentation
**Priority**: Medium
**Story**: US1, US2
**Description**: Document changes for future reference
**Estimated Time**: 10 minutes

**Implementation Tasks**:
- [ ] Create summary of all changes made
- [ ] Document any unexpected issues
- [ ] Update technical documentation

**Acceptance Criteria**:
- [ ] Summary document created
- [ ] All changes documented
- [ ] Lessons learned recorded

## Dependencies

### Task Dependencies
- **T001** must be completed before **T003**
- **T002** must be completed before **T003**
- **T003** must be completed before **T004**
- **T004** must be completed before **T005**
- **T005** must be completed before **T006**
- **T006** must be completed before **T007**

### File Dependencies
- **T002**: Requires `src/main/resources/messages.yml`
- **T003**: Requires `src/main/java/com/mmmm/story/managers/ItemManager.java`
- **T004**: Requires `src/main/java/com/mmmm/story/managers/ChestSpawnManager.java`

## Risk Mitigation

### Backup Strategy
- Create backup of original files before starting
- Test each task incrementally
- Keep rollback plan ready

### Testing Strategy
- Validate YAML syntax after each major change
- Test functionality after completing all tasks
- Verify no regressions in other systems

## Success Criteria

### Task Completion
- [ ] All 7 tasks completed successfully
- [ ] No raw localization keys visible to players
- [ ] All item names and descriptions display correctly
- [ ] Chest search shows proper success messages

### Quality Assurance
- [ ] YAML syntax is valid
- [ ] No console warnings about missing keys
- [ ] All game systems function correctly
- [ ] Player experience is improved

## Notes

- This is a code-only fix, no YAML file changes required
- Focus on maintaining all existing functionality while fixing display issues
- Test thoroughly to ensure no regressions
- Document any unexpected issues for future reference