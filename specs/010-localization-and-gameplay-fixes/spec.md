# Feature Specification: Localization and Gameplay Fixes

**Feature Branch**: `010-localization-and-gameplay-fixes`
**Created**: 2025-11-03
**Status**: Draft
**Input**: User description: "1. npc не пропадает во время реплики “Посланник исчезает в тумане” а должен пропадать с красивыми визуальными эффектами 2. не переведен обыск сундука на русский 3. не переведены все предметы 4. не переведены системные сообщение принахождении предметов 5. не переведены фразы боссов 6. доработать само меню по команде “/story menu” (что бы открывалось меню при /server start но немного изменено)"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Immersive NPC Interactions (Priority: P2)

When an NPC is supposed to disappear, it does so with a visual effect, enhancing the player's immersion in the story.

**Why this priority**: Visual feedback is a key part of storytelling in a game. A missing visual effect can break immersion and make the game feel unpolished.

**Independent Test**: The NPC "Посланник" disappears with a noticeable visual effect (e.g., fades away, dissolves into particles) at the appropriate moment in the dialogue.

**Acceptance Scenarios**:

1.  **Given** the player is in a dialogue with the "Посланник" NPC, **When** the dialogue line "Посланник исчезает в тумане" is displayed, **Then** the NPC entity is removed from the world with a visual effect.

### User Story 2 - Complete Russian Localization (Priority: P1)

Players using the Russian language setting will see all in-game text, including system messages, item names, and UI elements, in Russian.

**Why this priority**: The user has indicated that Russian is the primary language for this plugin. Incomplete localization makes the game difficult or impossible to play for the target audience.

**Independent Test**: A player with Russian language selected can play through the entire story without encountering any un-translated text.

**Acceptance Scenarios**:

1.  **Given** a player with Russian language selected, **When** they search a chest, **Then** all related messages are displayed in Russian.
2.  **Given** a player with Russian language selected, **When** they acquire or view any custom item, **Then** the item's name and lore are displayed in Russian.
3.  **Given** a player with Russian language selected, **When** they receive a system message related to finding an item, **Then** the message is displayed in Russian.
4.  **Given** a player with Russian language selected, **When** they encounter a boss, **Then** all of the boss's phrases are displayed in Russian.

### User Story 3 - Reworked Story Menu (Priority: P3)

The `/story menu` command opens an improved menu, and a modified version of this menu is also shown on `/server start`.

**Why this priority**: This is a quality-of-life improvement that enhances the user experience but is not as critical as the localization and core gameplay fixes.

**Independent Test**: The `/story menu` command opens a redesigned menu. The `/server start` command also opens a menu, which is a modified version of the `/story menu`.

**Acceptance Scenarios**:

1.  **Given** a player executes the `/story menu` command, **When** the menu appears, **Then** it reflects the new, improved design.
2.  **Given** a player executes the `/server start` command, **When** the menu appears, **Then** it is a modified version of the main story menu.

### Edge Cases

*   What happens if the visual effect for the NPC disappearance fails to load?
*   How does the system handle a player switching languages mid-game?
*   What should the `/server start` menu show for a player who has already completed the story?

## Requirements *(mandatory)*

### Functional Requirements

*   **FR-001**: The "Посланник" NPC MUST be despawned with a visual effect when the corresponding dialog line is triggered.
*   **FR-002**: All text related to chest searches MUST be translated into Russian in the `messages.yml` file.
*   **FR-003**: All custom item names and lore MUST be translated into Russian in the `messages.yml` file.
*   **FR-004**: All system messages related to finding items MUST be translated into Russian in the `messages.yml` file.
*   **FR-005**: All boss phrases MUST be translated into Russian in the `dialogs.yml` file.
*   **FR-006**: The menu opened by `/story menu` MUST be redesigned.
*   **FR-007**: A modified version of the `/story menu` MUST be displayed on `/server start`.
*   **FR-008**: The NPC disappearance visual effect MUST be a particle effect (e.g., smoke, mist).
*   **FR-009**: The redesigned `/story menu` MUST include a progress tracker, a list of completed quests, and a "Lore" section.
*   **FR-010**: The menu on `/server start` MUST include settings for dialogue speed and display.
*   **FR-011**: The `/server start` menu MUST include a "Ready" button.
*   **FR-012**: The story MUST only begin after all players currently on the server have indicated they are "Ready".

### Key Entities *(include if feature involves data)*

*   **Localization Key**: Unique identifier for translatable text (e.g., "chest.search.searching").
*   **Localization File**: Language-specific resource file containing key-value pairs (`messages.yml`, `dialogs.yml`).
*   **NPC**: Non-player character entity in the game.
*   **Menu**: A graphical user interface presented to the player.

## Success Criteria *(mandatory)*

### Measurable Outcomes

*   **SC-001**: 100% of user-facing text is translated into Russian.
*   **SC-002**: The "Посланник" NPC disappears with a visual effect 100% of the time at the correct dialogue point.
*   **SC-003**: The improved `/story menu` is accessible and functional.
*   **SC-004**: The `/server start` menu is displayed correctly and functions as intended.