# Mmmm Story Plugin: Project Constitution

This document outlines the key principles, architectural patterns, and technical guidelines for developing the Mmmm Story Plugin. All new code and contributions should adhere to these standards to maintain consistency, quality, and stability.

## 1. Core Technology Stack

*   **Language:** Java 21
*   **Platform:** Minecraft: Java Edition
*   **Server API:** [Paper API 1.21.1](https://docs.papermc.io/paper/dev/getting-started)
*   **Build Tool:** Apache Maven
*   **Key Dependencies:**
    *   **NPCs:** [NpcApi-Paper](https://github.com/eisi05/NpcApi-Paper) (Bundled in the final JAR)

## 2. Architecture and Structure

The plugin follows a classic manager-based architecture, which is standard for Bukkit/Paper plugins.

*   **Main Class:** [`MmmmStoryPlugin`](src/main/java/com/mmmm/story/MmmmStoryPlugin.java) is the central entry point and should only be used for initialization, shutdown, and providing access to managers.
*   **Singleton Access:** The plugin instance and its managers are accessed via the static `MmmmStoryPlugin.getInstance()` method.
*   **Managers:** All core logic is encapsulated within manager classes, each with a single responsibility. When adding a new major feature, create a new manager for it.
    *   **Location:** New manager classes should be placed in the `com/mmmm/story/managers` package.
*   **Event Listeners:** Gameplay logic is triggered by server events.
    *   **Location:** Listeners should be placed in the `com/mmmm/story/listeners` package.
    *   **Registration:** All listeners must be registered in the `registerListeners()` method within the main plugin class.
*   **Commands:** Player-facing commands are handled by classes that implement `CommandExecutor`.
    *   **Location:** Command classes should be in the `com/mmmm/story/commands` package.

## 3. Data Management and Configuration

*   **Configuration:** All configuration is handled through YAML files located in the `src/main/resources` directory. The primary configuration file is `config.yml`.
*   **Data Storage:** The plugin uses YAML files for storing data, such as NPC dialogs (`dialogs.yml`) and player messages (`messages.yml`). The [`DataManager`](src/main/java/com/mmmm/story/managers/DataManager.java) class is the entry point for accessing and modifying this data.
*   **Localization:** The plugin uses a localization system based on YAML files.
    *   All user-visible strings must be added to `messages.yml` (for general messages) or `dialogs.yml` (for NPC conversations).
    *   Do not hard-code strings in the Java source code. Use the [`MessageManager`](src/main/java/com/mmmm/story/managers/MessageManager.java) to retrieve localized strings.

## 4. Special Practices and Guidelines

*   **No Tests:** The project currently lacks a dedicated testing framework. Be extra diligent with manual testing of new features.
*   **No Linters:** There is no automated linter configured. Follow standard Java coding conventions and maintain a consistent style with the existing codebase.
*   **Immutability:** Where possible, use immutable objects to prevent unintended side effects.
*   **Defensive Programming:** Always check for null values and handle potential errors gracefully, especially when dealing with player input or external data.
*   **Documentation:** Add Javadoc comments to all new public methods and classes, explaining their purpose, parameters, and return values.

By following these guidelines, we can ensure that the Mmmm Story Plugin remains a high-quality, maintainable, and extensible project.