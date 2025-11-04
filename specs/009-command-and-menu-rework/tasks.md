# Tasks: Command and Menu Rework

**Input**: Design documents from `/specs/009-command-and-menu-rework/`

## Phase 1: Data Model and Persistence

- [ ] **T001**: In `src/main/java/com/mmmm/story/data/PlayerSettings.java`, remove the `language` field, its getter/setter, the `toggleLanguage()` method, and update the constructor.
- [ ] **T002**: In `src/main/java/com/mmmm/story/managers/DataManager.java`, remove all logic related to reading or writing the `language` field in the `getPlayerSettings` and `savePlayerSettings` methods.

## Phase 2: Command and UI Layer

- [ ] **T003**: In `src/main/java/com/mmmm/story/managers/ActManager.java`, delete the `showActTitle` method and remove the call to it from the `progressToAct` method.
- [ ] **T004**: In `src/main/java/com/mmmm/story/commands/StoryCommand.java`, remove the `case` blocks for `progress`, `reload`, `reset`, `skip`, `tp`, and `settings` from the `onCommand` method's `switch` statement.
- [ ] **T005**: In `src/main/java/com/mmmm/story/commands/StoryCommand.java`, delete the now-unused handler methods: `handleProgress`, `showProgress`, `handleReload`, `handleReset`, `handleSkip`, `handleTeleport`, and `handleSettings`.
- [ ] **T006**: In `src/main/java/com/mmmm/story/commands/StoryCommand.java`, update the `sendHelp` method to remove the deleted commands from the help text.
- [ ] **T007**: In `src/main/java/com/mmmm/story/commands/StoryCommand.java`, update the `onTabComplete` method to remove the deleted commands from the tab completion suggestions.
- [ ] **T008**: In `src/main/java/com/mmmm/story/commands/StoryCommand.java`, simplify the `handleStart` method. Remove the call to `plugin.getSettingsManager().startCampaignWithSettings()` and replace it with a simple call to `plugin.getActManager().startCampaign()`.
- [ ] **T009**: In `src/main/java/com/mmmm/story/commands/StoryCommand.java`, ensure the `menu` case calls `plugin.getMenuManager().openMainMenu(player)`.

## Phase 3: Menu System Refactoring

- [ ] **T010**: Delete the file `src/main/java/com/mmmm/story/managers/SettingsManager.java` as its functionality is now either removed or superseded by `MenuManager`.
- [ ] **T011**: In `src/main/java/com/mmmm/story/MmmmStoryPlugin.java`, remove the `SettingsManager` field, its initialization, and the `getSettingsManager()` method. Also remove the `reload()` method which was only used by a now-deleted command.
- [ ] **T012**: In `src/main/java/com/mmmm/story/managers/MenuManager.java`, modify `openSettingsSubmenu` to remove the language selection item and adjust the layout. The menu should now only contain options for "Dialog Speed" and "Dialog Display".
- [ ] **T013**: In `src/main/java/com/mmmm/story/managers/MenuManager.java`, update `handleSettingsMenuClick` to remove the logic for the language toggle.

## Phase 4: Final Cleanup

- [ ] **T014**: In `src/main/resources/plugin.yml`, remove the definitions for the deleted commands.
- [ ] **T015**: Search the entire project for any remaining usages of `SettingsManager` or the `language` field in `PlayerSettings` and remove them.