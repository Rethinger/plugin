# Project Context

## Purpose
Mmmm Story Plugin is a Minecraft plugin that implements a sequential story campaign with 5 acts featuring custom events, bosses, mob waves, and achievements. The plugin provides a structured narrative experience for players with interconnected story elements, boss battles, and progression mechanics. It includes features such as per-player localization, personal settings for dialog preferences, and synchronized world progress with individual player achievements.

## Tech Stack
- Java 21
- PaperMC API 1.21.1
- Maven (build system)
- Spigot API (for testing)
- NpcApi-Paper (NPC management)
- JUnit Jupiter (testing)
- Mockito (mocking framework)
- YAML (configuration and data storage)
- Bukkit API (Minecraft server interface)

## Project Conventions

### Code Style
- Java naming conventions (PascalCase for classes, camelCase for methods/variables)
- Descriptive method and variable names
- Proper encapsulation with private fields and public getter/setter methods
- Comprehensive JavaDoc comments for public classes and methods
- Enum-based configuration for type-safe options
- Consistent indentation and formatting following standard Java practices
- Use of try-catch blocks for exception handling in critical sections
- Logger usage for debugging and operational information

### Architecture Patterns
- Manager-based architecture (ConfigManager, DataManager, ItemManager, etc.)
- Singleton pattern (MmmmStoryPlugin.getInstance())
- Event-driven architecture (listeners for various game events)
- Dependency injection (managers passed to constructors)
- Data access layer (PlayerSettings, DataManager)
- Configuration management system with language support
- Separation of concerns (managers handle different aspects of the plugin)
- YAML-based data persistence
- Enum-based state management (for boss attacks, dialog speeds, etc.)

### Testing Strategy
- JUnit 5 for unit testing
- Mockito for mocking dependencies (World, Player, Location, etc.)
- Comprehensive test coverage for core mechanics (boss attacks, state management)
- Test-driven development approach for complex game mechanics
- Mock-based testing to avoid dependencies on Minecraft server environment
- Parameterized tests for configuration validation
- Boundary testing for limits and constraints
- Integration testing for plugin initialization and event handling

### Git Workflow
- Feature-based branching model
- Structured change proposals using OpenSpec methodology (openspec/changes/)
- Specification-driven development with requirement scenarios
- Version-controlled configuration and localization files
- Task-based development with detailed planning documents
- Change tracking with sequential IDs (005-per-player-i18n, 008-missing-localization-keys, etc.)
- Regular commits with descriptive messages
- Branch-based isolation of feature development

## Domain Context
This is a Minecraft story campaign plugin with 5 acts that take players through different dimensions (Overworld, Nether, End). The plugin includes custom boss mechanics, special attack patterns, and player progression systems. It features:

- A multi-act story with interconnected events
- Custom boss battles with complex attack patterns
- NPC interactions and dialog systems
- Structure placement and management
- Player progress synchronization (world-wide progress with individual achievements)
- Per-player localization and settings
- Custom items with special properties
- Portal and structure management
- Wave-based mob spawning systems

The plugin is designed for Minecraft 1.21.x servers running PaperMC or compatible implementations.

## Important Constraints
- Requires PaperMC 1.21.x or higher (Spigot API compatible)
- Java 21 or higher required
- Optional dependencies: WorldEdit (for structure import), Citizens2 (for enhanced NPCs)
- Performance considerations: mob spawn limits per player, throttling of wave spawning
- Thread safety: Bukkit API requires main thread execution for most operations
- Data persistence: automatic saving every 5 minutes to prevent data loss
- Memory management: efficient handling of player data and boss state
- Server restart resilience: all structures and states must be recoverable after restart

## External Dependencies
- PaperMC API (core Minecraft server functionality)
- NpcApi-Paper (NPC management and interactions)
- Spigot API (testing environment)
- Bukkit API (Minecraft server interface)
- WorldEdit (optional - structure import)
- Citizens2 (optional - enhanced NPCs)
- YAML libraries (configuration and data storage)
- JUnit/Mockito (testing framework)
