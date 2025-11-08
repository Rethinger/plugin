---
description: "Task list for Boss 1 Attack Fixes feature implementation"
---

# Tasks: Boss 1 Attack Fixes

**Input**: Design documents from `/specs/[012-boss1-attack-fixes]/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: The examples below include test tasks. Tests are OPTIONAL - only include them if explicitly requested in the feature specification.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Minecraft Plugin**: `src/main/java/com/mmmm/story/` at repository root
- **Boss mechanics**: `src/main/java/com/mmmm/story/bosses/`
- **Event listeners**: `src/main/java/com/mmmm/story/listeners/`
- **Configuration**: `src/main/java/com/mmmm/story/managers/`

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Create project structure per implementation plan
- [X] T002 [P] Update project dependencies in pom.xml for new boss mechanics
- [X] T003 [P] Create basic configuration structure for Boss 1 special attack settings

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

Examples of foundational tasks (adjust based on your project):

- [X] T004 Create BossAttackState enum with new states for rising animation
- [X] T005 [P] Create SpecialAttackConfiguration class in src/main/java/com/mmmm/story/bosses/SpecialAttackConfiguration.java
- [X] T006 [P] Create WitherSkullProjectile class in src/main/java/com/mmmm/story/bosses/WitherSkullProjectile.java
- [X] T007 Create BossRisingAnimation class in src/main/java/com/mmmm/story/bosses/BossRisingAnimation.java
- [X] T008 Update BossAttackState.java to include SPECIAL_ATTACK_CASTING and SPECIAL_ATTACK_ACTIVE states
- [X] T009 Create SafeZoneManager modifications in src/main/java/com/mmmm/story/managers/SafeZoneManager.java
- [X] T010 Configure special attack parameters in configuration files

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Improved Boss 1 Special Attack (Priority: P1) 🎯 MVP

**Goal**: Players experience a more epic and visually engaging special attack from Boss 1 without the disruptive stuns and unwanted skeleton warrior spawning. The boss performs a dramatic rising animation with visual effects while casting skull projectiles in a sphere pattern that targets players outside safe zones.

**Independent Test**: Can be fully tested by initiating Boss 1's special attack and verifying that the boss rises with visual effects, casts skull projectiles in a sphere pattern, and does not spawn skeleton warriors during the attack.

### Implementation for User Story 1

- [X] T011 [P] [US1] Remove stun effect from Boss 1 special attack in src/main/java/com/mmmm/story/listeners/Act2Listener.java
- [X] T012 [P] [US1] Prevent skeleton warrior spawning during special attack casting in src/main/java/com/mmmm/story/listeners/Act2Listener.java
- [X] T013 [US1] Implement boss rising animation logic in src/main/java/com/mmmm/story/bosses/BossRisingAnimation.java
- [X] T014 [US1] Create skull projectile spawning system in src/main/java/com/mmmm/story/bosses/WitherSkullProjectile.java
- [X] T015 [US1] Implement sphere pattern targeting for skull projectiles in src/main/java/com/mmmm/story/bosses/WitherSkullProjectile.java
- [X] T016 [US1] Integrate rising animation with special attack sequence in src/main/java/com/mmmm/story/listeners/Act2Listener.java
- [X] T017 [US1] Add validation and error handling for special attack mechanics in src/main/java/com/mmmm/story/listeners/Act2Listener.java
- [X] T018 [US1] Add logging for special attack operations in src/main/java/com/mmmm/story/listeners/Act2Listener.java

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Enhanced Visual Effects for Boss Attack (Priority: P1)

**Goal**: The boss special attack includes dramatic visual elements that make the encounter more epic and memorable, with rising effects and skull projectiles that create an immersive experience.

**Independent Test**: Can be tested by observing the visual effects during boss special attack and confirming they match the epic, dramatic aesthetic described.

### Implementation for User Story 2

- [X] T019 [P] [US2] Add soul fire particle effects to boss rising animation in src/main/java/com/mmmm/story/bosses/BossRisingAnimation.java
- [X] T020 [P] [US2] Add end rod particle effects to boss rising animation in src/main/java/com/mmmm/story/bosses/BossRisingAnimation.java
- [X] T021 [US2] Add particle effects to skull projectiles during rising phase in src/main/java/com/mmmm/story/bosses/WitherSkullProjectile.java
- [X] T022 [US2] Implement visual effects for skull projectiles moving to boss position in src/main/java/com/mmmm/story/bosses/WitherSkullProjectile.java
- [X] T023 [US2] Add visual effects for sphere pattern projectile launch in src/main/java/com/mmmm/story/bosses/WitherSkullProjectile.java
- [X] T024 [US2] Optimize particle effects to maintain server performance in src/main/java/com/mmmm/story/bosses/BossRisingAnimation.java

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - Safe Zone Improvement (Priority: P2)

**Goal**: Safe zones during boss special attack no longer have the vertical beacon effect that was visually distracting, making them cleaner and more focused on protection.

**Independent Test**: Can be tested by triggering the boss special attack and observing safe zones to confirm the vertical beacon effect is removed.

### Implementation for User Story 3

- [X] T025 [US3] Remove vertical beacon effect from safe zones during special attack in src/main/java/com/mmmm/story/managers/SafeZoneManager.java
- [X] T026 [US3] Maintain safe zone protection functionality while removing visual effect in src/main/java/com/mmmm/story/managers/SafeZoneManager.java
- [X] T027 [US3] Update safe zone state transitions to exclude beacon effect during special attacks in src/main/java/com/mmmm/story/managers/SafeZoneManager.java

**Checkpoint**: At this point, User Stories 1, 2 AND 3 should all work independently

---

## Phase 6: User Story 4 - Remove Wolves' Fear Mechanic (Priority: P2)

**Goal**: Boss 1 and its summoned skeleton warriors no longer have the default Minecraft mechanic that causes them to run away from wolves, making the encounter more consistent.

**Independent Test**: Can be tested by having wolves near the boss and summoned warriors to confirm they don't flee from them.

### Implementation for User Story 4

- [X] T028 [P] [US4] Remove wolves' fear mechanic from Boss 1 in src/main/java/com/mmmm/story/listeners/Act2Listener.java
- [X] T029 [P] [US4] Remove wolves' fear mechanic from skeleton warriors summoned by Boss 1 in src/main/java/com/mmmm/story/listeners/Act2Listener.java
- [X] T030 [US4] Modify Boss 1 AI to ignore wolves during combat in src/main/java/com/mmmm/story/bosses/BossAttackState.java
- [X] T031 [US4] Modify summoned skeleton warriors to ignore wolves during combat in src/main/java/com/mmmm/story/listeners/Act2Listener.java

**Checkpoint**: All user stories should now be independently functional

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [X] T032 [P] Update documentation for new boss mechanics in docs/boss_mechanics.md
- [X] T033 Code cleanup and refactoring for new boss mechanics in src/main/java/com/mmmm/story/listeners/Act2Listener.java
- [X] T034 Performance optimization for particle effects and projectile systems in src/main/java/com/mmmm/story/bosses/BossRisingAnimation.java
- [X] T035 [P] Add unit tests for new boss mechanics in src/test/java/com/mmmm/story/bosses/BossAttackTest.java
- [X] T036 Security hardening for boss combat mechanics in src/main/java/com/mmmm/story/listeners/Act2Listener.java
- [X] T037 Run quickstart.md validation for Boss 1 Attack Fixes feature in specs/012-boss1-attack-fixes/quickstart.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
 - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P1)**: Can start after Foundational (Phase 2) - May integrate with US1 but should be independently testable
- **User Story 3 (P2)**: Can start after Foundational (Phase 2) - May integrate with US1/US2 but should be independently testable
- **User Story 4 (P2)**: Can start after Foundational (Phase 2) - May integrate with US1/US2 but should be independently testable

### Within Each User Story

- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, all user stories can start in parallel (if team capacity allows)
- All tasks within a user story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members

---

## Parallel Example: User Story 1

```bash
# Launch all parallel tasks for User Story 1 together:
Task: "Remove stun effect from Boss 1 special attack in src/main/java/com/mmmm/story/listeners/Act2Listener.java"
Task: "Prevent skeleton warrior spawning during special attack casting in src/main/java/com/mmmm/story/listeners/Act2Listener.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo
5. Add User Story 4 → Test independently → Deploy/Demo
6. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1
   - Developer B: User Story 2
   - Developer C: User Story 3
   - Developer D: User Story 4
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence