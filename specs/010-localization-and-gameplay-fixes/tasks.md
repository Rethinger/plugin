# Task Breakdown: Localization and Gameplay Fixes

**Feature**: 010-localization-and-gameplay-fixes

---

## Phase 1: Pre-computation and Foundation (Blocking)

**Purpose**: Set up the foundational elements required for all subsequent tasks.

- [ ] **T001**: Back up existing localization files (`messages.yml`, `messages_en.yml`, `dialogs_en.yml`).
- [ ] **T002**: Create a new file `dialogs.yml` for Russian dialog translations.

---

## Phase 2: User Story 1 - Immersive NPC Interactions (P2)

**Goal**: Implement the visual effect for the disappearing NPC.

- [ ] **T003**: In `DialogManager.java`, modify the `playDialog` method to accept an optional `Runnable` to be executed at a specific dialog line.
- [ ] **T004**: In the `messenger.spawn` dialog definition in `dialogs_en.yml`, add a parameter to specify the line at which the NPC should disappear.
- [ ] **T005**: In `DialogManager.java`, when playing the `messenger.spawn` dialog, pass a `Runnable` that:
    - Gets the "Посланник" NPC entity.
    - Spawns a particle effect (e.g., `Particle.SMOKE_NORMAL`) at the NPC's location.
    - Schedules a delayed task to remove the NPC entity from the world.
- [ ] **T006**: Test the NPC disappearance effect in-game to ensure it triggers correctly and looks as expected.

---

## Phase 3: User Story 2 - Complete Russian Localization (P1)

**Goal**: Achieve 100% Russian localization for all specified game elements.

- [ ] **T007**: Copy the `chest.search` section from `messages_en.yml` to `messages.yml`.
- [ ] **T008**: Translate all `chest.search` values in `messages.yml` to Russian.
- [ ] **T009**: Copy the `items` section from `messages_en.yml` to `messages.yml`.
- [ ] **T010**: Translate all item names and lore in the `items` section of `messages.yml` to Russian.
- [ ] **T011**: Identify any missing system messages related to finding items and add Russian translations to `messages.yml`.
- [ ] **T012**: Copy the boss dialogues from `dialogs_en.yml` to `dialogs.yml`.
- [ ] **T013**: Translate all boss phrases in `dialogs.yml` to Russian.
- [ ] **T014**: Perform a full playthrough with the language set to Russian to verify all translations.

---

## Phase 4: User Story 3 - Reworked Story Menu (P3)

**Goal**: Implement the redesigned menus for `/story menu` and `/server start`.

- [ ] **T015**: In `MenuManager.java`, create a new method `openStoryMenu(Player player)` that builds and displays the redesigned story menu.
- [ ] **T016**: The new story menu should include items for:
    - Progress tracker (displaying current act).
    - List of completed quests.
    - "Lore" section.
- [ ] **T017**: In `StoryCommand.java`, update the `/story menu` command to call `MenuManager.openStoryMenu()`.
- [ ] **T018**: In `plugin.yml`, add a command for `/server start` if it doesn't exist.
- [x] **T019**: Create a new listener `PlayerJoinListener.java` that listens for the `PlayerJoinEvent`.
- [x] **T020**: In `PlayerJoinListener.java`, on player join, open the `/server start` menu.
- [x] **T021**: In `MenuManager.java`, create a new method `openServerStartMenu(Player player)` that builds and displays the server start menu.
- [x] **T022**: The server start menu should include:
    - An item to adjust dialog speed.
    - An item to adjust dialog display mode.
    - A "Ready" button.
- [x] **T023**: Implement a system to track the "Ready" status of all online players.
- [x] **T024**: When all online players are "Ready", start the story.
- [ ] **T025**: Test the `/story menu` and `/server start` menus to ensure they function correctly.

---

## Phase 5: Finalization and Cleanup

**Purpose**: Ensure the feature is polished and ready for release.

- [ ] **T026**: Review all new code for adherence to the project's code style standards.
- [ ] **T027**: Remove any temporary or debug code.
- [ ] **T028**: Update the `CHANGELOG.md` with the new features and fixes.
- [ ] **T029**: Perform a final round of testing on all implemented features.