# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

<!-- OPENSPEC:START -->
# OpenSpec Instructions

These instructions are for AI assistants working in this project.

Always open `@/openspec/AGENTS.md` when the request:
- Mentions planning or proposals (words like proposal, spec, change, plan)
- Introduces new capabilities, breaking changes, architecture shifts, or big performance/security work
- Sounds ambiguous and you need the authoritative spec before coding

Use `@/openspec/AGENTS.md` to learn:
- How to create and apply change proposals
- Spec format and conventions
- Project structure and guidelines

Keep this managed block so 'openspec update' can refresh the instructions.

<!-- OPENSPEC:END -->

## Project Overview

This is a sophisticated Minecraft 1.21.x story campaign plugin using Paper API, written entirely in Russian. It implements a complex 5-act story with NPCs, dialog systems, boss fights, and progression mechanics.

## Development Commands

### Build & Compilation
```bash
# Clean compile with Maven
mvn clean package

# Output: target/story-plugin-1.0.jar
```

### Testing
```bash
# Run specific tests (if implemented)
mvn test

# Run plugin in test server
# Copy target/story-plugin-1.0.jar to server/plugins/
```

### Debug Commands (In-Game)
```bash
/story start          # Start story campaign (requires story.admin)
/story settings       # Open player settings menu (all players)
/story menu          # Alias for settings
/story debug         # Show debug information
/story reload        # Reload configuration files
/story give <player> <item>  # Give story items to players
```

## Architecture Overview

### Core Plugin Structure
- **Main Class**: `MmmmStoryPlugin.java` - Singleton pattern with manager initialization
- **Manager-Based Architecture**: Clear separation of concerns across different systems
- **Event-Driven**: Complex event listeners for each story act
- **Configuration-Driven**: All story content externalized in YAML files

### Key Managers (Initialization Order)

1. **ConfigManager** - Configuration & localization hub
   - Manages `config.yml`, `dialogs.yml`, `sounds.yml`, `messages.yml`
   - Multi-language support (Russian primary, English fallback)
   - Dynamic configuration reloading

2. **DataManager** - Player data persistence
   - JSON-based player progress tracking
   - Auto-save every 5 minutes
   - Achievement and quest state management

3. **NPCManager** - Advanced NPC system (1,015 lines)
   - Complex animation system with frame-by-frame timing
   - Multi-phase spawning with particle effects
   - Behavioral AI with idle animations
   - Advanced despawning with cleanup

4. **DialogManager** - Interactive story system
   - Automatic dialog playback with speed control
   - Sound synchronization with dialog timing
   - Visual effects (darkness, weather, portal ignition)
   - Session management with pause/resume

5. **ActManager** - Story progression controller
   - 5-act campaign structure
   - Silent transitions between acts
   - World state management

6. **StructureManager** - World structure management
   - Custom structure detection and placement
   - Integration with WorldEdit for schematics

7. **ItemManager** - Custom item system
   - Story-specific item creation and management
   - Item identification and properties

8. **MenuManager** - Unified GUI system
   - Stackable menu navigation
   - Player state integration
   - Settings management

9. **MessageManager** - Communication layer
   - Multi-language message retrieval
   - Dynamic placeholder replacement

### Event Listeners
- **Act1Listener**: Complex skeleton wave spawning, cinematic effects
- **Act2Listener**: Nether exploration mechanics
- **Act3Listener**: Boss fight mechanics (Skeleton Lord)
- **Act4Listener**: Blaze Master boss mechanics
- **Act5Listener**: End portal and artifact collection

## Story Structure

### 5-Act Campaign
1. **Act 1**: Overworld exploration, skeleton waves, stabilization core
2. **Act 2**: Nether access, catalyst acquisition
3. **Act 3**: Skeleton Lord boss fight
4. **Act 4**: Blaze Master boss fight
5. **Act 5**: End portal opening, artifact collection, final ritual

### Key Features
- **NPC System**: Advanced animations with squash/stretch effects
- **Dialog System**: Multi-language, speed-controlled, synchronized with effects
- **Boss Combat**: Complex attack patterns with special phases
- **Progressive World Building**: Gradual world revelation
- **Achievement System**: Player progression tracking

## Configuration Files

### Core Configuration
- `config.yml` - Main plugin settings, act configurations
- `dialogs.yml` - Story dialogues with timing and effects
- `sounds.yml` - Sound effect configurations
- `messages.yml` - Multi-language messages (Russian)

### Player Data
- `data/players/<uuid>.yml` - Individual player progress and settings
- Auto-saves every 5 minutes
- Stores act progress, achievements, personal settings

## Development Patterns

### Manager Access Pattern
```java
// All managers receive plugin instance in constructor
public class SomeManager {
    private final MmmmStoryPlugin plugin;

    public SomeManager(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
    }

    // Access other managers through plugin instance
    public void someMethod() {
        ConfigManager config = plugin.getConfigManager();
        // ...
    }
}
```

### Configuration Access
```java
// Always use ConfigManager for configuration values
String message = plugin.getConfigManager().getMessage("some.key");
int value = plugin.getConfigManager().getInt("some.setting", defaultValue);
```

### Event Registration
```java
// Register listeners in main plugin class
@Override
public void onEnable() {
    getServer().getPluginManager().registerEvents(new Act1Listener(this), this);
}
```

### NPC Pattern
```java
// NPC operations require careful null checks
if (npc != null) {
    npc.animate(NPC.Animation.TALKING);
    // Always check if NPC still exists after operations
}
```

## Important Conventions

### Naming
- **Russian Comments**: Primary documentation language
- **Manager Suffix**: All system classes end with "Manager"
- **Snake Case**: Configuration keys and data fields
- **Pascal Case**: Class names and method names

### Error Handling
- Comprehensive null checks for NPC operations
- Graceful degradation when data is missing
- Detailed logging for debugging
- Exception handling for plugin lifecycle

### Performance
- Proper task cleanup in disable() method
- Particle effects with radius optimization
- Efficient data loading/saving
- Memory management for NPCs

## Working with This Codebase

### Adding New Features
1. **Start with Manager**: Determine which manager handles the feature
2. **Configuration First**: Add configuration keys before implementation
3. **Event-Driven**: Use event listeners for game world interactions
4. **Test Thoroughly**: NPC timing is critical - test animations carefully

### Modifying Dialog System
1. **Edit YAML First**: All dialog content is in `dialogs.yml`
2. **Timing Matters**: Dialog timing affects NPC animations
3. **Multi-Language**: Consider both Russian and English versions
4. **Effects Integration**: Dialogs trigger visual and sound effects

### Adding New Acts
1. **Create Listener**: Each act has its own event listener
2. **Update ActManager**: Add act progression logic
3. **Configuration**: Add act-specific settings to config.yml
4. **Testing**: Test progression from previous acts

### Boss Mechanics
1. **Attack States**: Use the BossAttackState system
2. **Special Attacks**: Implement special attack phases with animations
3. **Boundary Enforcement**: Prevent movement during special attacks
4. **Player Feedback**: Provide clear visual and audio feedback

## Dependencies

### Required
- **PaperMC API 1.21.1**: For latest Minecraft features
- **Java 21**: Runtime requirement
- **NpcApi-Paper**: Advanced NPC functionality (version 1.21.x-4)

### Optional
- **WorldEdit**: For structure import/export
- **Citizens2**: Enhanced NPC functionality (fallback available)

## Testing Checklist

### Basic Functionality
- [ ] Plugin loads without errors
- [ ] Commands work correctly
- [ ] Configuration files generate
- [ ] NPCs spawn and animate properly

### Story Progression
- [ ] Act transitions work
- [ ] Dialog timing is correct
- [ ] Boss mechanics function
- [ ] Player progress saves

### Performance
- [ ] No memory leaks from NPCs
- [ ] Scheduled tasks cleanup properly
- [ ] Particle effects are optimized
- [ ] Data persistence is reliable

## Localization

### Current Languages
- **Russian**: Primary language (complete)
- **English**: Secondary language (partial)

### Adding New Language
1. Create `messages_<lang>.yml`
2. Add language option to player settings
3. Update MessageManager for new language
4. Test all message displays

## Troubleshooting

### Common Issues
1. **NPC Not Appearing**: Check NPC API initialization and skin loading
2. **Dialog Not Playing**: Verify configuration file syntax and timing
3. **Progress Not Saving**: Check file permissions and data directory
4. **Performance Issues**: Review task cleanup and particle optimization

### Debug Commands
```bash
/story debug          # Show current plugin state
/log level <level>    # Set logging level for detailed output
```

### Log Analysis
- Look for NPC initialization errors
- Check configuration loading warnings
- Monitor data persistence operations
- Review task scheduling logs