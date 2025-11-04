# Analysis Report: Localization and Gameplay Fixes

**Feature**: 010-localization-and-gameplay-fixes

## 1. Affected Components

Based on the implementation plan, the following components will be affected:

*   **Managers**:
    *   `DialogManager.java`: Will be modified to handle the NPC disappearance effect.
    *   `MenuManager.java`: Will be modified to create the new menu layouts.
    *   `MessageManager.java`: Will be used to load and serve the new Russian translations.
*   **Commands**:
    *   `StoryCommand.java`: Will be modified to handle the new menu logic for `/story menu` and potentially `/server start`.
*   **Listeners**:
    *   A new `PlayerJoinListener.java` will be created to show the `/server start` menu to players when they join.
*   **Configuration Files**:
    *   `messages.yml`: Will be updated with Russian translations for chest search, items, and system messages.
    *   `dialogs.yml`: Will be created and populated with Russian translations for boss phrases.
    *   `plugin.yml`: May need to be updated to include the `/server start` command.

## 2. Proposed Solution

The proposed solution will be implemented in three main phases, as outlined in the `plan.md`:

1.  **Localization**: This will be the most straightforward phase. It involves copying the English localization keys from `messages_en.yml` and `dialogs_en.yml` into their Russian counterparts (`messages.yml` and `dialogs.yml`) and translating them. This will be a data-only change and should not require any code modifications in the `MessageManager` itself, as it's already designed to handle multiple languages.

2.  **NPC Visual Effect**: This will require a modification to the `DialogManager`. The plan is to add a mechanism to trigger a `Runnable` at a specific point in a dialog sequence. This is a clean and extensible solution that avoids hardcoding the effect into the dialog player. The `Runnable` will be responsible for creating the particle effect and removing the NPC.

3.  **Menu Rework**: This is the most complex part of the feature. It will involve significant changes to the `MenuManager` and `StoryCommand`, as well as the creation of a new listener. The redesigned `/story menu` will require fetching player data to display the progress tracker and completed quests. The `/server start` menu will introduce a new "ready-up" mechanic, which will require a system to track the state of all online players.

## 3. Solution Validation

The proposed solution aligns with the project's constitution and is technically sound.

*   **Modularity**: The changes are well-contained within their respective managers, following the manager-based service layer principle.
*   **Data-Driven**: The use of YAML files for localization and the proposed mechanism for triggering the NPC effect from the dialog file adhere to the data-driven design philosophy of the plugin.
*   **Extensibility**: The `Runnable` mechanism in the `DialogManager` can be reused for other in-dialog events in the future.

By following the detailed task breakdown in `tasks.md`, the implementation should be straightforward and result in a high-quality, maintainable solution.