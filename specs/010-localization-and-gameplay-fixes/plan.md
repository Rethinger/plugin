# Implementation Plan: Localization and Gameplay Fixes

**Feature**: 010-localization-and-gameplay-fixes

## 1. Constitutional Alignment

| Principle | Alignment | Notes |
|---|---|---|
| **I. Java 21 & Paper API** | ✅ PASS | All new code will be Java 21 compliant and use Paper API. |
| **II. Singleton Plugin Pattern** | ✅ PASS | No changes to the singleton pattern. Will use `MmmmStoryPlugin.getInstance()` to access managers. |
| **III. Manager-Based Service Layer** | ✅ PASS | Logic will be implemented within the appropriate managers (`DialogManager`, `MessageManager`, `MenuManager`). |
| **IV. YAML-Only Data Storage** | ✅ PASS | All new localizations will be added to the existing YAML files. |
| **V. Localization-First Message Design**| ✅ **CORE FEATURE** | This feature is centered around completing the localization, directly supporting this principle. |
| **VI. Dialog System with Personalization** | ✅ PASS | Changes to the dialog system will be made through the `DialogManager`. |
| **VII. Protection of Story Items** | ✅ PASS | No changes to story item protection logic are required. |

**Gate Result**: ✅ **APPROVED** - The proposed changes are fully compliant with the project's constitution.

## 2. Project Structure

### Files to be Modified

*   `src/main/java/com/mmmm/story/managers/DialogManager.java`: To implement the NPC disappearance effect.
*   `src/main/java/com/mmmm/story/managers/MenuManager.java`: To implement the new menu designs.
*   `src/main/java/com/mmmm/story/commands/StoryCommand.java`: To handle the new menu logic.
*   `src/main/resources/messages.yml`: To add Russian translations for chest search, items, and system messages.
*   `src/main/resources/dialogs.yml`: To add Russian translations for boss phrases.
*   `src/main/resources/plugin.yml`: To handle the `/server start` command if it's not already implemented.

## 3. Implementation Phases

### Phase 1: Localization

**Goal**: Achieve 100% Russian localization for all specified game elements.

1.  **Translate Chest Search**:
    *   Copy the `chest.search` section from [`messages_en.yml`](src/main/resources/messages_en.yml:28) to `messages.yml`.
    *   Translate the values for `searching`, `searching_item`, `found`, `success`, `nothing`, `broke`, and `cooldown` into Russian.
2.  **Translate Items**:
    *   Copy the `items` section from [`messages_en.yml`](src/main/resources/messages_en.yml:118) to `messages.yml`.
    *   Translate all item names and lore into Russian.
3.  **Translate System Messages**:
    *   Add Russian translations for any missing system messages related to finding items.
4.  **Translate Boss Phrases**:
    *   Create a `dialogs.yml` file if it doesn't exist.
    *   Copy the boss dialogues from `dialogs_en.yml` to `dialogs.yml` and translate all boss phrases into Russian.

### Phase 2: NPC Visual Effect

**Goal**: Implement the visual effect for the disappearing NPC.

1.  **Locate the Dialog Trigger**: In [`DialogManager.java`](src/main/java/com/mmmm/story/managers/DialogManager.java), find the code that plays the `messenger.spawn` dialog.
2.  **Identify the Disappearance Line**: Find the part of the dialog sequence that corresponds to the line "Посланник исчезает в тумане".
3.  **Implement the Effect**:
    *   After the line is displayed, get the NPC entity.
    *   Play a particle effect (e.g., `Particle.SMOKE_NORMAL`) at the NPC's location.
    *   Schedule a task to remove the NPC entity after a short delay (e.g., 1 second) to allow the particle effect to be seen.

### Phase 3: Menu Rework

**Goal**: Implement the redesigned menus for `/story menu` and `/server start`.

1.  **Redesign `/story menu`**:
    *   In [`MenuManager.java`](src/main/java/com/mmmm/story/managers/MenuManager.java), create a new menu layout for the `/story menu`.
    *   This menu should include:
        *   A progress tracker (e.g., displaying the current act).
        *   A list of completed quests.
        *   A "Lore" section.
2.  **Implement `/server start` Menu**:
    *   In [`plugin.yml`](src/main/resources/plugin.yml), ensure there's a command for `/server start` that can be hooked into. If not, add it.
    *   In [`StoryCommand.java`](src/main/java/com/mmmm/story/commands/StoryCommand.java) (or a new command class), handle the `/server start` command.
    *   In [`MenuManager.java`](src/main/java/com/mmmm/story/managers/MenuManager.java), create the menu for `/server start`.
    *   This menu should include:
        *   Settings for dialog speed and display.
        *   A "Ready" button.
    *   Implement the logic to track the "Ready" status of all online players and start the story only when all are ready.

## 4. Testing Plan

*   **Localization**:
    *   Set the language to Russian and play through the game.
    *   Verify that all chest search messages, item names/lore, system messages, and boss phrases are in Russian.
*   **NPC Effect**:
    *   Trigger the `messenger.spawn` dialog and confirm that the NPC disappears with a smoke/mist particle effect.
*   **Menus**:
    *   Execute `/story menu` and verify the new menu design and functionality.
    *   Execute `/server start` and verify the menu with settings and the "Ready" button. Test the "Ready" functionality with multiple players.