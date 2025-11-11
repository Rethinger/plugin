# Mmmm Story Plugin

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/Rethinger/plugin)
[![Version](https://img.shields.io/badge/version-1.0-blue.svg)](https://github.com/Rethinger/plugin/releases)
[![Java](https://img.shields.io/badge/java-21+-orange.svg)](https://openjdk.java.net/)
[![PaperMC](https://img.shields.io/badge/PaperMC-1.21.1-green.svg)](https://papermc.io/)

A sophisticated Minecraft story campaign plugin built for PaperMC 1.21.x, featuring a complete 5-act storyline with advanced NPC systems, boss mechanics, dialog management, and progressive world building.

## Features

### Core Systems
- **Advanced NPC System** - Frame-by-frame animations with squash/stretch effects and behavioral AI
- **Multi-Act Story Campaign** - 5-act progressive storyline with seamless transitions
- **Dynamic Dialog System** - Multi-language support with speed control and sound synchronization
- **Boss Battle Mechanics** - Complex attack patterns, special phases, and boundary enforcement
- **Custom Item System** - Story-specific items with NBT tagging and unique textures
- **Progressive World Building** - Dynamic structure placement and world state management
- **Player Achievement System** - Individual progress tracking with auto-save functionality
- **Multi-Language Support** - Russian (primary) and English localization

### Technical Features
- **Manager-Based Architecture** - Clean separation of concerns across 13 specialized managers
- **Event-Driven Design** - Complex event listeners for each story act with precise timing
- **Configuration-Driven** - All story content externalized in YAML files
- **Performance Optimized** - Particle effects with radius optimization, mob throttling, and async data saving
- **Persistent Data Storage** - JSON-based player progress with 5-minute auto-save intervals
- **Memory Management** - Proper task cleanup and NPC memory management
- **Debug Tools** - Comprehensive debugging commands and detailed logging

### User Experience
- **Personal Settings** - Individual player preferences for dialog language, speed, and display
- **GUI Menu System** - Stackable navigation with player state integration
- **Visual Effects** - Weather control, darkness effects, and portal ignition
- **Sound Integration** - Synchronized audio effects with dialog timing
- **Safe Zone Management** - Configurable protection zones with visual feedback

## Requirements

### Required
- **Java 21** or higher
- **PaperMC 1.21.1** or higher (Spigot API compatible)
- **Maven 3.6+** (for building from source)

### Included Dependencies
- **NpcApi-Paper 1.21.x-4** - Advanced NPC functionality (bundled)

## Installation

### Quick Install (Server Owners)

1. **Download the latest release** from the [Releases page](https://github.com/Rethinger/plugin/releases)
2. **Place the JAR file** in your server's `plugins/` directory
3. **Restart your server** to generate configuration files
4. **Configure settings** as needed (see Configuration section)
5. **Reload** with `/story reload` or restart again

### Build from Source (Developers)

```bash
# Clone the repository
git clone https://github.com/Rethinger/plugin.git
cd plugin

# Compile with Maven
mvn clean package

# The compiled JAR will be in: target/story-plugin-1.0.jar
```

## Configuration

The plugin uses multiple YAML configuration files for maximum flexibility:

### Core Configuration Files

- **`config.yml`** - Main plugin settings, act configurations, boss mechanics, and structure locations
- **`dialogs.yml`** - Story dialogues with timing controls, sound effects, and display methods
- **`sounds.yml`** - Audio effect configurations for various events
- **`messages.yml`** - Multi-language message templates (Russian primary)
- **`messages_en.yml`** - English localization support

### Data Storage

- **`data/global.yml`** - World progress, boss states, structure coordinates, artifact counts
- **`data/players/<uuid>.yml`** - Individual player progress, achievements, and personal settings

### Personal Player Settings

Each player can customize their experience through GUI menus:
- **Dialog Display** - Enable/disable story dialogues
- **Language Preference** - Russian or English dialogues
- **Dialog Speed** - Slow (+50%), Normal, or Fast (-25%)

## Commands

### Player Commands
```bash
/story settings    # Open personal settings menu (all players)
/story menu        # Alias for settings
```

### Administration Commands
```bash
/story start           # Start story campaign
/story skip <act>      # Skip to specific act (1-5)
/story reset <target>  # Reset progress (all|world|player <name>)
/story progress [player] # Show current progress
/story tp <location>   # Teleport to structures (debug)
/story reload          # Reload configuration files
/story give <player> <item> # Give story items
/story debug           # Show debug information
```

### Server Commands
```bash
/server start          # Initialize server start sequence
```

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `story.admin` | Full admin access to all commands | OP |
| `story.debug` | Debug and teleport commands | OP |
| `story.progress.view` | View story progress of any player | true |

## Architecture

### Manager System
The plugin uses a sophisticated manager-based architecture with clear separation of concerns:

#### Core Managers
1. **ConfigManager** - Configuration hub with multi-language support
2. **DataManager** - JSON-based player progress persistence
3. **NPCManager** - Advanced NPC animations and behavioral AI
4. **DialogManager** - Interactive story system with sound synchronization
5. **ActManager** - Story progression and world state control

#### Specialized Managers
6. **StructureManager** - Custom structure detection and placement
7. **ItemManager** - Story-specific item creation and management
8. **MenuManager** - Unified GUI system with stackable navigation
9. **MessageManager** - Multi-language communication layer
10. **SafeZoneManager** - Protection zone management
11. **ChestSpawnManager** - Loot chest generation and control

### Event Listeners
Each story act has dedicated event listeners:
- **Act1Listener** - Skeleton wave spawning, cinematic effects
- **Act2Listener** - Nether exploration mechanics
- **Act3Listener** - Boss fight mechanics (Skeleton Lord)
- **Act4Listener** - Blaze Master boss mechanics
- **Act5Listener** - End portal and artifact collection

### Boss Battle System
The plugin features an advanced boss mechanics system including:
- **BossAttackState** - Complex attack pattern management
- **Special Attack Phases** - Boundary-enforced animations
- **Damage Scaling** - Balanced combat mechanics
- **Visual Feedback** - Particle effects and sound cues

## Story Structure

The plugin implements a 5-act campaign structure:

1. **Act 1** - Overworld exploration with skeleton waves and stabilization core discovery
2. **Act 2** - Nether access and catalyst acquisition with first boss encounter
3. **Act 3** - Skeleton Lord boss battle mechanics
4. **Act 4** - Blaze Master boss confrontation
5. **Act 5** - End portal activation and artifact collection

Each act includes unique mechanics, custom structures, and progressive difficulty scaling.

## Development

### Project Structure
```
src/main/java/com/mmmm/story/
├── MmmmStoryPlugin.java          # Main plugin class
├── commands/                     # Command implementations
├── listeners/                    # Event listeners by act
├── managers/                     # Core system managers
├── bosses/                       # Boss mechanics and AI
└── utils/                        # Utility classes

src/main/resources/
├── plugin.yml                    # Plugin metadata
├── config.yml                    # Main configuration
├── dialogs.yml                   # Story dialogues
├── sounds.yml                    # Audio configurations
└── messages*.yml                 # Localization files
```

### Code Patterns

#### Manager Access
```java
public class SomeManager {
    private final MmmmStoryPlugin plugin;

    public SomeManager(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
    }

    public void someMethod() {
        ConfigManager config = plugin.getConfigManager();
        // Access other managers through plugin instance
    }
}
```

#### Configuration Access
```java
String message = plugin.getConfigManager().getMessage("some.key");
int value = plugin.getConfigManager().getInt("some.setting", defaultValue);
```

#### Event Registration
```java
@Override
public void onEnable() {
    getServer().getPluginManager().registerEvents(new Act1Listener(this), this);
}
```

### Testing

#### Unit Tests
```bash
mvn test
```

#### Integration Testing
1. Copy `target/story-plugin-1.0.jar` to test server
2. Test all commands and permissions
3. Verify NPC animations and dialog timing
4. Test boss mechanics and progression

#### Test Checklist
- [ ] Plugin loads without errors
- [ ] All commands execute properly
- [ ] Configuration files generate correctly
- [ ] NPCs spawn and animate as expected
- [ ] Act transitions function smoothly
- [ ] Boss mechanics work as designed
- [ ] Player progress saves reliably

## Contributing

We welcome contributions to improve the plugin! Please follow these guidelines:

### Pull Request Process
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Standards
- **Language**: All comments and documentation should be in English
- **Java Style**: Follow standard Java conventions
- **Manager Pattern**: Use existing manager access patterns
- **Configuration**: Externalize all configurable values in YAML files
- **Error Handling**: Include comprehensive null checks and exception handling
- **Testing**: Add unit tests for new functionality
- **Documentation**: Update relevant documentation sections

### Development Setup
```bash
# Clone your fork
git clone https://github.com/Rethinger/plugin.git
cd plugin

# Install dependencies
mvn clean install

# Run tests
mvn test

# Start development server
# Copy target/story-plugin-1.0.jar to server/plugins/
```

## Troubleshooting

### Common Issues

#### Plugin Won't Load
- **Check Java version**: Ensure Java 21+ is installed
- **Verify PaperMC version**: Must be 1.21.1 or higher
- **Check logs**: Look for dependency errors in server logs

#### NPCs Not Appearing
- **Verify NPC API**: Check if NpcApi-Paper is properly initialized
- **Check skins**: Ensure skin loading isn't blocked by firewall
- **Review logs**: Look for NPC initialization errors

#### Dialog System Issues
- **Validate YAML**: Check dialog configuration syntax
- **Check permissions**: Ensure players have necessary permissions
- **Verify timing**: Dialog timing affects NPC animations

#### Progress Not Saving
- **File permissions**: Ensure plugin has write access to data directory
- **Disk space**: Check available disk space
- **Review logs**: Monitor data persistence operations

### Debug Commands
```bash
/story debug          # Show current plugin state
/log level <level>    # Set detailed logging level
```

### Log Analysis
Check server logs (`logs/latest.log`) for:
- NPC initialization errors
- Configuration loading warnings
- Data persistence operations
- Task scheduling issues

### Performance Issues
- **Mob Limits**: Configure mob throttling in config.yml
- **Particle Effects**: Adjust radius settings for better performance
- **Task Cleanup**: Monitor for memory leaks from scheduled tasks

## License

This project is licensed under the [MIT License](LICENSE) - see the LICENSE file for details.

## Support

- **Issues**: Report bugs via [GitHub Issues](https://github.com/Rethinger/plugin/issues)
- **Discussions**: Join our [GitHub Discussions](https://github.com/Rethinger/plugin/discussions)

---

**Note**: This plugin is primarily designed for Russian-speaking servers, but includes full English localization support. All technical documentation is provided in English for developer accessibility.