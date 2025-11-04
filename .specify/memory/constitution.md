<!--
Sync Impact Report - Constitution Update
=========================================
Version Change: 1.0.1 → 1.1.0
Modified Principles:
  - Command Development: Removed several admin and debug commands.
  - Player Settings: Removed language selection to simplify player experience.
Added Sections: N/A
Removed Sections: N/A
Templates Requiring Updates:
  ✅ Verified: .specify/templates/plan-template.md - No changes needed.
  ✅ Verified: .specify/templates/spec-template.md - No changes needed.
  ✅ Verified: .specify/templates/tasks-template.md - No changes needed.
Follow-up TODOs: None
=========================================
-->

# Mmmm Story Plugin Constitution

## Core Principles

### I. Java 21 & Paper API Exclusivity (NON-NEGOTIABLE)

**Rule**: All code MUST compile with Java 21 and target Paper API 1.21.x exclusively. No backward compatibility with Java 8/11/17 or Spigot-only APIs.

**Rationale**: Paper 1.21.x requires Java 21+ and provides superior performance optimizations over Spigot. Modern Java features (records, pattern matching, enhanced switch) improve code clarity and maintainability. Attempting backward compatibility dilutes code quality and increases technical debt.

**Enforcement**:
- Maven compiler target: `<release>21</release>`
- Paper API dependency scope: `provided`
- No deprecated Bukkit/Spigot methods allowed
- CI/CD must validate Java 21 compilation

### II. Singleton Plugin Pattern

**Rule**: `MmmmStoryPlugin` MUST be a singleton accessible via `getInstance()`. All managers MUST receive the plugin instance via constructor injection, never static imports.

**Rationale**: Centralized plugin access ensures:
- Testability (mock plugin instance in unit tests)
- Lifecycle management (proper initialization order)
- Clear dependency graph (explicit constructor dependencies)
- Thread safety (single point of truth)

**Anti-pattern**: Creating new plugin references or using static service locators.

### III. Manager-Based Service Layer

**Rule**: Business logic MUST be encapsulated in dedicated Manager classes. Listeners MUST delegate to managers, not contain business logic.

**Structure**:
```
MmmmStoryPlugin (singleton)
  ├── ConfigManager (loads YAML configurations)
  ├── DataManager (persistence layer)
  ├── DialogManager (dialog playback engine)
  ├── ItemManager (story item creation)
  ├── NPCManager (NPC lifecycle)
  ├── ActManager (act progression)
  ├── StructureManager (world structures)
  ├── SettingsManager (player preferences)
  ├── MessageManager (localization)
  ├── ChestSpawnManager (loot generation)
  └── PlayerPlacedBlocksManager (anti-exploit tracking)
```

**Rationale**: Separation of concerns. Listeners handle event routing; managers handle state mutations and complex logic. This enables:
- Unit testing managers without Bukkit events
- Reusable logic across multiple listeners
- Clear initialization order enforcement
- Easier debugging (single point of failure per domain)

### IV. YAML-Only Data Storage

**Rule**: All persistent data MUST use YAML format. SQLite/MySQL/JSON files are PROHIBITED.

**Files**:
- `config.yml` - Plugin configuration
- `data/global.yml` - World-wide campaign progress
- `data/players/<uuid>.yml` - Player settings
- `dialogs.yml` / `dialogs_en.yml` - Localized dialogs
- `messages_ru.yml` / `messages_en.yml` - Localized messages
- `sounds.yml` - Sound effect mappings

**Rationale**:
- Portability (no database setup required)
- Human-readable (admins can edit files directly)
- Simplicity (no SQL migrations or ORM complexity)
- Bukkit native (ConfigurationSection API well-tested)

**Performance safeguard**: Dirty flag pattern required for writes. Auto-save every 5 minutes + backup before save.

### V. Localization-First Message Design

**Rule**: ZERO hardcoded user-facing strings in Java code. All messages MUST route through `MessageManager`.

**Implementation**:
```java
// ✅ CORRECT
messageManager.sendMessage(player, "portal.nether.blocked");

// ❌ PROHIBITED
player.sendMessage("§cThe Nether Portal is sealed!");
```

**Rationale**:
- Centralized text management (non-programmers can translate)
- A/B testing message variants
- Consistency (no mixed formatting across codebase)

### VI. Dialog System with Personalization

**Rule**: All narrative sequences MUST use `DialogManager.playDialog()`. Dialogs MUST support per-player customization (speed, display method).

**Features**:
- Multi-line timed sequences with sound effects
- Configurable speed (50/100/150ms per character)
- Display modes: ActionBar, Title, Hologram (future)
- PotionEffect integration for ambiance

**Anti-pattern**: Sending raw chat messages for story events.

**Rationale**: Narrative consistency. Dialogs are the core experience delivery mechanism—they must be:
- Interruptible (player quits mid-dialog)
- Skippable (player preference)
- Testable (mock dialog keys)
- Translatable without code changes

### VII. Protection of Story Items & Anti-Exploit Design

**Rule**: All story-critical items MUST use `PersistentDataContainer` tags. Block placement tracking MUST prevent boss exploit strategies.

**Required Protections**:
1. **Item Tagging**: Every story item tagged with `NamespacedKey("story_item", itemId)`
2. **Event Cancellation**: Block drop/throw/move to other inventories
3. **Death Retention**: Items stay in inventory on death
4. **Block Tracking**: `PlayerPlacedBlocksManager` prevents boss teleport exploits

**Rationale**: Story items are irreplaceable progression gates. Loss due to bugs/exploits breaks the campaign. Boss mechanics must not be bypassable through terrain modification.

## Technology Stack Requirements

### Mandatory Dependencies

| Dependency | Version | Scope | Purpose |
|------------|---------|-------|---------|
| **Paper API** | 1.21.1-R0.1-SNAPSHOT | provided | Minecraft server API |
| **NPC API** (de.eisi05) | 1.21.x-4 | compile | Custom NPC creation |
| **Maven Shade Plugin** | 3.5.0 | build | Dependency bundling |

### Optional Dependencies (Soft-Depend)

- **WorldEdit**: Structure import (.schem files) - MUST gracefully degrade if absent
- **Citizens2**: Enhanced NPC features (future) - NOT USED in v1.x

### Build Configuration

```xml
<properties>
    <java.version>21</java.version>
    <maven.compiler.release>21</maven.compiler.release>
</properties>
```

**Rule**: Shaded dependencies MUST be relocated to avoid conflicts (e.g., `com.mmmm.libs.npcapi`).

## Architecture and Code Organization

### Package Structure (Immutable)

```
com.mmmm.story/
├── MmmmStoryPlugin.java          # Main class (singleton)
├── commands/                      # Command handlers
│   └── StoryCommand.java          
├── listeners/                     # Event listeners (one per act + global)
│   ├── Act1Listener.java          
│   ├── Act2Listener.java          
│   ├── Act3Listener.java          
│   ├── Act4Listener.java          
│   ├── Act5Listener.java          
│   ├── PortalListener.java        
│   ├── PlayerListener.java        
│   ├── MobListener.java           
│   ├── BlockTrackingListener.java 
│   └── StoryItemProtectionListener.java
├── managers/                      # Business logic layer
│   └── [12 manager classes]
├── data/                          # DTOs
│   └── PlayerSettings.java        
└── npc/                           # NPC-specific utilities
```

**Expansion Rules**:
- **New Act**: Add `ActXListener.java` + register in `MmmmStoryPlugin`
- **New Boss**: Logic in corresponding `ActXListener` (private methods)
- **New Manager**: Add field in `MmmmStoryPlugin` + initialize in correct order
- **New Command**: Add case in `StoryCommand` switch + update `plugin.yml`

### Initialization Order (Strict)

```java
// In MmmmStoryPlugin.onEnable() - ORDER MATTERS
1. NpcApi.createInstance(this)
2. ConfigManager
3. DataManager
4. ItemManager, DialogManager
5. StructureManager, NPCManager
6. ActManager, SettingsManager
7. MessageManager
8. registerListeners()
9. registerCommands()
```

**Rationale**: Dependency graph requires strict loading sequence. Managers with no dependencies load first; high-level orchestrators load last.

### Code Style Standards

**Naming**:
- Classes: `PascalCase`
- Methods: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Variables: `camelCase`

**Formatting**:
- Indentation: 4 spaces (NO TABS)
- Line length: 120 characters max
- Braces: Same-line opening (`if (x) {`)
- Javadoc: Required for public APIs

**Null Handling**:
- Use `Optional<T>` for potentially null returns
- Check `!= null` before usage in critical paths
- Document nullability with `@Nullable` / `@NotNull` annotations

## Data Management Standards

### Global Progress Data (`data/global.yml`)

**Schema**:
```yaml
act:
  current: 1  # 1-5

portals:
  nether:
    enabled: false
  end:
    enabled: false

bosses:
  boss1:
    defeated: false
    location: null

structures:
  forgotten_altar:
    activated: false

artifacts:
  collected: 0

nights_elapsed: 0
```

**Access Rule**: Only `DataManager` may read/write this file. No direct `FileConfiguration` access from listeners.

### Player Settings (`data/players/<uuid>.yml`)

**Schema**:
```yaml
settings:
  dialogSpeed: 100  # 50 (fast) | 100 (normal) | 150 (slow)
  showDialogs: true

stats: # Future expansion
  bossesKilled: 0
  artifactsFound: 0
```

**Access Rule**: Use Java `record PlayerSettings(int dialogSpeed, boolean showDialogs)`.

### Save Strategy

**Dirty Flag Optimization**:
```java
private boolean globalDirty = false;
private Set<UUID> dirtyPlayers = new HashSet<>();

public void setCurrentAct(int act) {
    globalData.set("act.current", act);
    globalDirty = true;  // Mark for save
}
```

**Auto-Save**: Every 5 minutes (6000 ticks) via `BukkitScheduler`

**Backup**: Copy `global.yml` to `global.yml.backup` before each write

### Configuration File Rules

**Principles**:
1. ALL numeric balance values in `config.yml` (no hardcoding)
2. Dialog keys pattern: `<context>.<event>` (e.g., `boss1.phase2`)
3. Version field for migration detection
4. Comments for admin guidance

**Example**:
```yaml
version: 1  # For future schema migrations

acts:
  boss1:
    health: 200  # NOT hardcoded in Java
    teleportDistance: 4.0
```

## Development Workflow

### Pre-Commit Checklist

- [ ] Code compiles: `mvn clean package`
- [ ] No TODOs in production code
- [ ] Public methods have Javadoc
- [ ] Config values extracted from hardcoded numbers
- [ ] Permissions added to `plugin.yml`
- [ ] Documentation updated (if API changed)
- [ ] Tested on local test server
- [ ] No hardcoded strings (use `MessageManager`)
- [ ] Resource cleanup in `onDisable()` (if created entities/tasks)

### Testing Strategy

**Current State**: Manual testing on development server

**Future (v2.0)**:
- JUnit 5 for manager unit tests
- MockBukkit for Bukkit API mocking
- Integration tests for act progression

**Debug Mode**:
```yaml
# config.yml
debug: true  # Enables verbose logging
```

```java
if (configManager.isDebug()) {
    plugin.getLogger().info("[DEBUG] Boss phase: 1 -> 2");
}
```

### Command Development

**Pattern**:
```java
@Override
public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    switch (args[0].toLowerCase()) {
        case "start" -> handleStart(sender);
        case "menu" -> handleMenu(sender);
        case "give" -> handleGive(sender, args);
        default -> sendHelp(sender);
    }
    return true;
}
```

**Permission Check**:
```java
if (!sender.hasPermission("story.admin")) {
    messageManager.sendMessage((Player) sender, "commands.no_permission");
    return;
}
```

### Prohibited Practices

**NEVER**:
- Use `Thread.sleep()` (blocks main thread) → use `BukkitScheduler`
- Hardcode balance values → use `config.yml`
- Direct `player.sendMessage()` for story content → use `DialogManager`
- Create entities without cleanup tracking → use manager cleanup methods
- Modify config files from code → configs are read-only at runtime
- Use deprecated Bukkit APIs → migrate to Paper equivalents

## Governance

### Amendment Process

1. **Proposal**: Document proposed change in GitHub issue
2. **Impact Analysis**: Identify affected managers/listeners/configs
3. **Version Bump Decision**:
   - **MAJOR**: Breaking changes (e.g., removing YAML-only rule)
   - **MINOR**: New principle or section added
   - **PATCH**: Clarifications, typo fixes, non-semantic updates
4. **Implementation**: Update constitution + propagate to templates
5. **Migration Plan**: If breaking, provide migration guide
6. **Approval**: Review by repository owner (Rethinger)

### Compliance Verification

**Mandatory Reviews**:
- All PRs must pass constitution checklist
- Code review must verify manager pattern adherence
- Manual test of dialog system for story-related changes

**Enforcement Tools**:
- Maven enforcer plugin (Java 21 validation)
- Checkstyle (future - code style automation)
- Architecture tests (future - verify package structure)

### Exceptions

Exceptions to constitutional rules require:
1. Documented justification in code comments
2. GitHub issue explaining exception rationale
3. Plan to remove exception in future version
4. Approval in PR review comments

**Example**:
```java
// CONSTITUTIONAL EXCEPTION: Directly modifying player inventory
// Rationale: Bukkit API does not provide event-safe alternative for this use case
// Tracked in: Issue #123
// Removal planned: v2.0 when NMS wrapper is implemented
player.getInventory().setItem(0, item);
```

### Related Documentation

- **Runtime Development**: `PROJECT_CONSTITUTION.md` (comprehensive guide)
- **Architecture Details**: `ARCHITECTURE.md`
- **Player Guide**: `PLAYER_GUIDE.md`
- **Deployment**: `DEPLOYMENT_GUIDE.md`
- **Development Setup**: `README_DEV.md`

**Version**: 1.1.0 | **Ratified**: 2025-10-24 | **Last Amended**: 2025-11-03
