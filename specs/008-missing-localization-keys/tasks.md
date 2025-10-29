# Tasks: Missing Localization Keys Throughout Plugin

**Input**: Design documents from `/specs/008-missing-localization-keys/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅

**Tests**: Not explicitly requested in specification - manual testing only

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- Java plugin project: `src/main/java/`, `src/main/resources/`
- Build scripts: `.specify/scripts/powershell/`
- Configuration: `pom.xml`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and validation infrastructure

- [ ] T001 Create validation script directory at .specify/scripts/powershell/
- [ ] T002 [P] Create translation glossary at specs/008-missing-localization-keys/glossary.md
- [ ] T003 [P] Backup existing localization files (messages_ru.yml, messages_en.yml, dialogs_ru.yml, dialogs_en.yml)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core validation infrastructure that MUST be complete before ANY user story translation work can begin

**⚠️ CRITICAL**: No translation work can begin until this phase is complete

- [ ] T004 Create validate-localization-keys.ps1 script at .specify/scripts/powershell/validate-localization-keys.ps1
- [ ] T005 Implement Java source code scanning in validate-localization-keys.ps1 (extract getMessage/getMessageList calls)
- [ ] T006 Implement YAML parsing in validate-localization-keys.ps1 (extract keys from messages_ru.yml)
- [ ] T007 Implement key comparison logic in validate-localization-keys.ps1 (find missing keys)
- [ ] T008 Add build integration to pom.xml (Maven exec plugin to run validation in validate phase)
- [ ] T009 Run validation script to generate baseline missing keys report
- [ ] T010 Verify validation script correctly identifies known missing keys (chest.search.*, items.stabilization_core.name)

**Checkpoint**: Foundation ready - validation script works and identifies all missing keys. Translation work can now begin.

---

## Phase 3: User Story 1 - Player Searches Chests (Priority: P1) 🎯 MVP

**Goal**: Add complete translations for all chest search functionality so players see proper Russian/English messages instead of technical key names

**Independent Test**: 
1. Player initiates chest search → sees "Поиск..." in Russian (not "chest.search.searching")
2. Player searches for specific item → sees "Поиск {item}..." with item name in selected language
3. Player completes search with no results → sees "Ничего не найдено" in Russian
4. Server console shows zero "Missing localization key" warnings for chest.search.* keys

### Implementation for User Story 1

- [ ] T011 [P] [US1] Add chest.search.searching key to src/main/resources/messages_ru.yml with value "Поиск..."
- [ ] T012 [P] [US1] Add chest.search.searching_item key to src/main/resources/messages_ru.yml with value "Поиск {item}..."
- [ ] T013 [P] [US1] Add chest.search.nothing key to src/main/resources/messages_ru.yml with value "Ничего не найдено"
- [ ] T014 [P] [US1] Add chest.search.found key to src/main/resources/messages_ru.yml with value "Найдено: {count} предметов" (if referenced in code)
- [ ] T015 [P] [US1] Add chest.search.searching key to src/main/resources/messages_en.yml with value "Searching..."
- [ ] T016 [P] [US1] Add chest.search.searching_item key to src/main/resources/messages_en.yml with value "Searching for {item}..."
- [ ] T017 [P] [US1] Add chest.search.nothing key to src/main/resources/messages_en.yml with value "Nothing found"
- [ ] T018 [P] [US1] Add chest.search.found key to src/main/resources/messages_en.yml with value "Found: {count} items" (if referenced in code)
- [ ] T019 [US1] Run validation script to verify chest.search.* keys are no longer missing
- [ ] T020 [US1] Compile plugin with mvn clean package to verify build passes
- [ ] T021 [US1] Deploy to test server and trigger chest search feature to verify messages display correctly
- [ ] T022 [US1] Check server console logs to confirm zero "Missing localization key" warnings for chest.search.* keys

**Checkpoint**: At this point, User Story 1 should be fully functional - all chest search messages display in proper Russian/English

---

## Phase 4: User Story 2 - Player Views Item Names (Priority: P1)

**Goal**: Add complete translations for all custom item names and lore so players see proper item descriptions instead of technical key names

**Independent Test**:
1. Player views stabilization core in inventory → sees "Ядро стабилизации" in Russian (not "items.stabilization_core.name")
2. Player hovers over item → sees lore in selected language with proper color codes
3. Server console shows zero "Missing localization key" warnings for items.* keys

### Implementation for User Story 2

- [ ] T023 [P] [US2] Identify all custom story items that need translations (scan code for items.*.name pattern)
- [ ] T024 [P] [US2] Add items.stabilization_core.name key to src/main/resources/messages_ru.yml with value "Ядро стабилизации"
- [ ] T025 [P] [US2] Add items.stabilization_core.lore key to src/main/resources/messages_ru.yml with multi-line lore in Russian
- [ ] T026 [P] [US2] Add any other items.*.name keys found in T023 to src/main/resources/messages_ru.yml
- [ ] T027 [P] [US2] Add items.stabilization_core.name key to src/main/resources/messages_en.yml with value "Stabilization Core"
- [ ] T028 [P] [US2] Add items.stabilization_core.lore key to src/main/resources/messages_en.yml with multi-line lore in English
- [ ] T029 [P] [US2] Add any other items.*.name keys found in T023 to src/main/resources/messages_en.yml
- [ ] T030 [US2] Run validation script to verify items.* keys are no longer missing
- [ ] T031 [US2] Compile plugin with mvn clean package to verify build passes
- [ ] T032 [US2] Deploy to test server and obtain stabilization core item (/give command)
- [ ] T033 [US2] Verify item name displays "Ядро стабилизации" in Russian inventory
- [ ] T034 [US2] Verify item lore displays correctly with color codes
- [ ] T035 [US2] Check server console logs to confirm zero "Missing localization key" warnings for items.* keys

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently - chest search messages AND item names display correctly

---

## Phase 5: User Story 3 - Comprehensive Localization Coverage (Priority: P2)

**Goal**: Identify and translate ALL remaining missing keys across the entire plugin (menus, commands, NPCs, dialogs, notifications)

**Independent Test**:
1. Complete walkthrough of all plugin features in both Russian and English
2. Zero "Missing localization key" warnings in server logs during full gameplay session
3. All user-facing text displays as translated content (no technical key names visible)

### Implementation for User Story 3

- [ ] T036 [US3] Run validation script to generate comprehensive missing keys report for all remaining keys
- [ ] T037 [US3] Review missing keys report and categorize by functional area (menu.*, dialog.*, npc.*, command.*, etc.)
- [ ] T038 [P] [US3] Add all missing menu.* keys to src/main/resources/messages_ru.yml with Russian translations
- [ ] T039 [P] [US3] Add all missing dialog.* keys to src/main/resources/dialogs_ru.yml with Russian translations
- [ ] T040 [P] [US3] Add all missing npc.* keys to src/main/resources/messages_ru.yml with Russian translations
- [ ] T041 [P] [US3] Add all missing command.* keys to src/main/resources/messages_ru.yml with Russian translations
- [ ] T042 [P] [US3] Add any other missing keys to appropriate Russian YAML files
- [ ] T043 [P] [US3] Add all missing menu.* keys to src/main/resources/messages_en.yml with English translations
- [ ] T044 [P] [US3] Add all missing dialog.* keys to src/main/resources/dialogs_en.yml with English translations
- [ ] T045 [P] [US3] Add all missing npc.* keys to src/main/resources/messages_en.yml with English translations
- [ ] T046 [P] [US3] Add all missing command.* keys to src/main/resources/messages_en.yml with English translations
- [ ] T047 [P] [US3] Add any other missing keys to appropriate English YAML files
- [ ] T048 [US3] Run validation script to verify ALL keys are now present (zero missing keys)
- [ ] T049 [US3] Compile plugin with mvn clean package to verify build passes
- [ ] T050 [US3] Deploy to test server and perform comprehensive feature walkthrough
- [ ] T051 [US3] Test menu system (/story command) to verify all menu text is translated
- [ ] T052 [US3] Test all story dialogs to verify translations and sound effects
- [ ] T053 [US3] Test NPC interactions to verify all NPC messages are translated
- [ ] T054 [US3] Test all commands to verify help text and feedback messages are translated
- [ ] T055 [US3] Monitor server console during full playthrough - verify zero "Missing localization key" warnings
- [ ] T056 [US3] Cross-reference with translation glossary to ensure terminology consistency

**Checkpoint**: All user stories should now be independently functional - complete localization coverage achieved

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements and validation that affect multiple user stories

- [ ] T057 [P] Update TRANSLATION_TEST_CHECKLIST.md with feature 008 completion status
- [ ] T058 [P] Create translation glossary with standard terms for future reference at specs/008-missing-localization-keys/glossary.md
- [ ] T059 [P] Document validation script usage in README or development guide
- [ ] T060 Perform final validation run to generate clean report (should show zero missing keys)
- [ ] T061 Test hot-reload of YAML files (modify a translation, reload plugin, verify change)
- [ ] T062 Verify UTF-8 encoding with BOM for all YAML files (Windows compatibility)
- [ ] T063 Check YAML file sizes (should be under 1MB as per research.md)
- [ ] T064 Review code for any hardcoded strings that should use MessageManager
- [ ] T065 Run full regression test following quickstart.md validation section
- [ ] T066 Update agent context if needed (already done in plan phase)
- [ ] T067 Create PR with all changes and request review from Russian native speaker

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-5)**: All depend on Foundational phase completion
  - User Story 1 can proceed independently after Phase 2
  - User Story 2 can proceed independently after Phase 2 (parallel with US1)
  - User Story 3 depends on US1 and US2 for context but can technically run in parallel
- **Polish (Phase 6)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1) - Chest Search**: Can start after Foundational (Phase 2) - No dependencies on other stories ✅ INDEPENDENT
- **User Story 2 (P1) - Item Names**: Can start after Foundational (Phase 2) - No dependencies on other stories ✅ INDEPENDENT
- **User Story 3 (P2) - Comprehensive Coverage**: Can start after Foundational (Phase 2) - Includes remaining keys not in US1/US2 ✅ MOSTLY INDEPENDENT

### Within Each User Story

- User Story 1: All Russian translations [P] in parallel → All English translations [P] in parallel → Validation → Testing
- User Story 2: Item identification → Russian translations [P] in parallel → English translations [P] in parallel → Validation → Testing
- User Story 3: Missing keys identification → Russian translations [P] in parallel → English translations [P] in parallel → Validation → Testing

### Parallel Opportunities

- Phase 1: T002 and T003 can run in parallel
- Phase 2: T004-T007 are sequential (script development), T010 can run after T009
- User Story 1: T011-T014 (Russian translations) can all run in parallel, T015-T018 (English translations) can all run in parallel
- User Story 2: T024-T026 (Russian translations) can run in parallel, T027-T029 (English translations) can run in parallel
- User Story 3: T038-T042 (Russian translations) can run in parallel, T043-T047 (English translations) can run in parallel
- Phase 6: T057, T058, T059 can all run in parallel
- **Cross-Story**: User Story 1, 2, and 3 can be worked on by different developers simultaneously after Phase 2 completes

---

## Parallel Example: User Story 1 (Chest Search)

```bash
# All Russian chest.search.* keys can be added simultaneously:
Task: "Add chest.search.searching key to messages_ru.yml"
Task: "Add chest.search.searching_item key to messages_ru.yml"
Task: "Add chest.search.nothing key to messages_ru.yml"
Task: "Add chest.search.found key to messages_ru.yml"

# All English chest.search.* keys can be added simultaneously:
Task: "Add chest.search.searching key to messages_en.yml"
Task: "Add chest.search.searching_item key to messages_en.yml"
Task: "Add chest.search.nothing key to messages_en.yml"
Task: "Add chest.search.found key to messages_en.yml"
```

---

## Parallel Example: User Story 2 (Item Names)

```bash
# All Russian items.* keys can be added simultaneously:
Task: "Add items.stabilization_core.name key to messages_ru.yml"
Task: "Add items.stabilization_core.lore key to messages_ru.yml"
Task: "Add items.{other_item}.name keys to messages_ru.yml"

# All English items.* keys can be added simultaneously:
Task: "Add items.stabilization_core.name key to messages_en.yml"
Task: "Add items.stabilization_core.lore key to messages_en.yml"
Task: "Add items.{other_item}.name keys to messages_en.yml"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup → Validation infrastructure ready
2. Complete Phase 2: Foundational (CRITICAL) → Validation script working
3. Complete Phase 3: User Story 1 → Chest search fully translated
4. **STOP and VALIDATE**: Test chest search independently
5. Deploy/demo if ready → Players can use chest search without seeing technical keys

**MVP delivers**: Complete chest search localization (most visible missing keys based on server logs)

### Incremental Delivery

1. Complete Setup + Foundational → Validation script operational ✅
2. Add User Story 1 → Test independently → Deploy/Demo (MVP - chest search works!) ✅
3. Add User Story 2 → Test independently → Deploy/Demo (Item names work!) ✅
4. Add User Story 3 → Test independently → Deploy/Demo (Complete coverage!) ✅
5. Polish phase → Professional quality assurance ✅

Each story adds value without breaking previous stories. Can ship after any completed user story.

### Parallel Team Strategy

With multiple developers after Foundational phase completes:

1. **Developer A**: User Story 1 (Chest Search) - Add all chest.search.* translations
2. **Developer B**: User Story 2 (Item Names) - Add all items.* translations
3. **Developer C**: User Story 3 (Comprehensive) - Identify and add remaining translations
4. All merge independently without conflicts (different key prefixes in same files)

Or with single developer:

1. Week 1: Setup + Foundational (2-3 hours)
2. Week 1: User Story 1 complete (2 hours) → Ship MVP!
3. Week 2: User Story 2 complete (2 hours) → Ship update!
4. Week 2: User Story 3 complete (3-4 hours) → Ship final version!

---

## Summary

- **Total Tasks**: 67
- **User Story 1**: 12 tasks (T011-T022)
- **User Story 2**: 13 tasks (T023-T035)
- **User Story 3**: 21 tasks (T036-T056)
- **Parallel Opportunities**: 35 tasks marked [P] (52% parallelizable)
- **MVP Scope**: Phase 1 + Phase 2 + User Story 1 = T001-T022 (22 tasks, ~4-5 hours)
- **Estimated Total Time**: 15-20 hours for complete coverage (all 3 user stories + polish)

---

## Notes

- [P] tasks = different YAML key prefixes or different files, no conflicts
- [Story] label maps task to specific user story (US1=Chest Search, US2=Item Names, US3=Comprehensive)
- Each user story is independently completable and testable
- Russian translations are CRITICAL (build fails if missing) - prioritize these
- English translations are RECOMMENDED (warnings only) - can be added in parallel
- Validation script (Phase 2) is critical blocker - must work before translations begin
- All YAML edits are low-risk (backwards compatible, no code changes)
- Can ship after any completed user story - incremental value delivery
- Glossary should be built incrementally during translation work for consistency
