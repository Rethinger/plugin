# Implementation Plan: Command and Menu Rework

**Branch**: `feat/009-command-menu-rework` | **Date**: 2025-11-03 | **Spec**: [specs/009-command-and-menu-rework/spec.md](specs/009-command-and-menu-rework/spec.md)

## Summary

This plan outlines the technical steps required to remove several administrative and player-facing commands, simplify the player settings menu, and streamline the overall user experience. The goal is to focus the plugin on its core narrative delivery by reducing unnecessary complexity.

## Technical Context

-   **Language/Version**: Java 21
-   **Primary Dependencies**: Paper API 1.21.x
-   **Project Type**: Single project (Maven)
-   **Target Platform**: PaperMC Server

## Constitution Check

All changes proposed in this plan are aligned with the newly updated constitution (v1.1.0). The removal of commands and simplification of player settings are the driving factors for this implementation.

## Project Structure

The changes will be focused within the existing `com.mmmm.story` package structure. No new files are anticipated; this is primarily a refactoring and removal effort.

### Key Files for Modification:

1.  `src/main/java/com/mmmm/story/commands/StoryCommand.java`: The central hub for all command logic. Will be significantly simplified.
2.  `src/main/java/com/mmmm/story/data/PlayerSettings.java`: The data model for player settings. The `language` property will be removed.
3.  `src/main/java/com/mmmm/story/managers/DataManager.java`: Responsible for loading/saving `PlayerSettings`. Will be updated to no longer handle the `language` property.
4.  `src/main/java/com/mmmm/story/managers/SettingsManager.java`: The old settings GUI. This will be deprecated and its functionality either removed or merged into `MenuManager`.
5.  `src/main/java/com/mmmm/story/managers/MenuManager.java`: The current primary menu system. This will be updated to handle the simplified settings menu.
6.  `src/main/java/com/mmmm/story/managers/ActManager.java`: Contains the act transition UI logic that needs to be removed.
7.  `src/main/resources/plugin.yml`: Command definitions will be removed from here.

## Implementation Strategy

The implementation will be phased to ensure a clean and logical workflow.

### Phase 1: Data Model & Persistence Layer

1.  **Modify `PlayerSettings.java`**:
    *   Remove the `language` field.
    *   Remove the `getLanguage()`, `setLanguage()`, and `toggleLanguage()` methods.
    *   Update the constructor to reflect the removal of the `language` parameter.

2.  **Modify `DataManager.java`**:
    *   In `getPlayerSettings()`, remove the logic that reads `settings.language` from the player's YAML file.
    *   In `savePlayerSettings()`, remove the logic that writes `settings.language`.

### Phase 2: Command Layer Refactoring

1.  **Modify `StoryCommand.java`**:
    *   In the `onCommand` `switch` statement, remove the `case` blocks for: `progress`, `reload`, `reset`, `skip`, `tp`, and `settings`.
    *   Delete the corresponding handler methods: `handleProgress`, `showProgress`, `handleReload`, `handleReset`, `handleSkip`, `handleTeleport`, and `handleSettings`.
    *   Update the `handleStart` method to call the new simplified menu logic in `MenuManager` or `SettingsManager` instead of the complex waiting system.
    *   Update the `sendHelp` method to remove the deleted commands.
    *   In the `onTabComplete` method, remove all references to the deleted commands.

2.  **Modify `plugin.yml`**:
    *   Remove the command definitions for all the subcommands that are being deleted. This will ensure they are no longer registered with the server.

### Phase 3: UI and Menu Layer Rework

1.  **Modify `ActManager.java`**:
    *   Delete the `showActTitle` method entirely.
    *   In the `progressToAct` method, remove the call to `showActTitle(act)`.

2.  **Deprecate/Refactor `SettingsManager.java`**:
    *   The primary settings GUI logic will be moved to `MenuManager`.
    *   Remove the `createLanguageItem` method.
    *   In `openSettingsMenu`, remove the call to `createLanguageItem`. Adjust the layout slots accordingly.
    *   In `onInventoryClick`, remove the `case` for `LANGUAGE_SLOT`.
    *   The complex player waiting system (`startCampaignWithSettings`, `startWaitingTask`, `checkAllPlayersReady`) will be removed.

3.  **Enhance `MenuManager.java`**:
    *   This will become the definitive menu system.
    *   In `openSettingsSubmenu`, remove any logic related to language. The layout should be simplified to only show "Dialog Speed" and "Dialog Display".
    *   In `handleSettingsMenuClick`, remove any logic for handling clicks on a language item.

## Complexity Tracking

This task primarily involves code removal and simplification. The complexity is low-to-medium, with the main risk being incomplete removal of all references to the deleted features. A thorough search after the initial changes will be necessary to catch any lingering references.