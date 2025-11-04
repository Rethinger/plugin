# Feature Specification: Command and Menu Rework

**Feature Branch**: `feat/009-command-menu-rework`  
**Created**: 2025-11-03  
**Status**: Draft  
**Input**: User feedback requesting command removal and menu simplification.

## 1. User Scenarios & Testing

### User Story 1 - Simplified Player Experience (Priority: P1)

As a server administrator, I want to provide a more focused and streamlined story experience by removing confusing or unnecessary commands and UI elements, so that players can engage with the core narrative without distraction.

**Why this priority**: The current set of commands and options clutters the experience and is not aligned with the desired simplified gameplay loop.

**Independent Test**:
1.  Verify that all specified admin/debug commands (`/story progress`, `/reload`, `/reset`, `/skip`, `/tp`) are no longer registered and do not appear in tab-completion.
2.  Verify that the `/story settings` command is removed and its functionality is merged into a simplified `/story menu`.
3.  Verify that the `/story start` command no longer presents a multi-step menu and directly starts the campaign.
4.  Verify that the menu opened by `/story menu` (and now also by `/story start` if needed) does not contain a language selection option.
5.  Verify that the large title text and chat announcements for the start of each act no longer appear.

**Acceptance Scenarios**:

1.  **Given** a player has `story.admin` permissions, **When** they type `/story ` and press Tab, **Then** the commands `progress`, `reload`, `reset`, `skip`, and `tp` do not appear in the suggestion list.
2.  **Given** a player has `story.admin` permissions, **When** they execute `/story reload`, **Then** they receive an "unknown command" message.
3.  **Given** any player, **When** they execute `/story settings`, **Then** they receive an "unknown command" message.
4.  **Given** a player has not configured their settings, **When** the `/story start` command is executed by an admin, **Then** a simplified menu opens directly, which does not contain a language option.
5.  **Given** the campaign progresses from Act 1 to Act 2, **When** the transition occurs, **Then** no large title text or multi-line chat message announcing "Act 2" is displayed to players.

## 2. Requirements

### Functional Requirements

-   **FR-001**: The plugin MUST completely remove the registration and execution logic for the following sub-commands: `progress`, `reload`, `reset`, `skip`, `tp`.
-   **FR-002**: The `/story settings` sub-command and its alias MUST be removed.
-   **FR-003**: The `/story menu` sub-command MUST be the sole entry point for players to access the settings GUI.
-   **FR-004**: The settings GUI, when opened, MUST NOT display an option for language selection.
-   **FR-005**: The `/story start` command logic MUST be simplified. It should no longer be responsible for a complex, multi-player waiting system. It can now directly open the simplified settings menu if a player has not configured their settings, or simply start the story.
-   **FR-006**: The underlying `PlayerSettings` data model and `DataManager` MUST be updated to remove the `language` field.
-   **FR-007**: The `showActTitle` functionality within the `ActManager` MUST be removed to prevent act transition titles and messages from being displayed.

### Non-Functional Requirements

-   **NFR-001**: The removal of these features should not negatively impact server performance.
-   **NFR-002**: The remaining commands and menu system must remain responsive and functional.

## 3. Key Entities

-   **`StoryCommand.java`**: The central point for command logic. Will be heavily modified.
-   **`SettingsManager.java`**: The old settings menu handler. Will be deprecated or removed.
-   **`MenuManager.java`**: The current menu handler. Will be adapted to become the primary, simplified menu.
-   **`PlayerSettings.java`**: The data object for player preferences. The `language` field will be removed.
-   **`DataManager.java`**: Handles persistence of `PlayerSettings`. Logic for saving/loading language will be removed.
-   **`ActManager.java`**: The `showActTitle` method will be removed.

## 4. Success Criteria

-   **SC-001**: All commands listed in FR-001 and FR-002 are fully non-functional and removed from user-facing help and tab-completion.
-   **SC-002**: The settings menu is accessible via `/story menu` and does not show a language option.
-   **SC-003**: Players no longer see the large, intrusive title cards when a new story act begins.
-   **SC-004**: The player data saved in `data/players/<uuid>.yml` no longer contains a `language` field.