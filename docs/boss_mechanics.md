# Boss 1 Attack Mechanics Documentation

## Overview

This document describes the enhanced Boss 1 (Skeleton Lord) attack mechanics implemented in the Boss 1 Attack Fixes feature.

## Features

### 1. Enhanced Special Attack

The boss special attack has been completely redesigned to provide a more epic and engaging combat experience:

#### Rising Animation
- Boss rises 10-15 blocks above original position during special attack
- Smooth easing animation for natural movement
- Visual effects include soul fire and end rod particles
- Dramatic sound effects at key animation points

#### Skull Projectile System
- Skull projectiles spawn at ground level and rise to boss position
- Enhanced particle trails during rising phase
- Sphere pattern targeting for players outside safe zones
- 32-40 projectiles create an epic visual effect

#### Safe Zone Integration
- Safe zones provide protection during special attacks
- Enhanced visual effects without vertical beacon effect
- Pulsing red particles with ground-level circle indicators
- Maintains all protective functionality

### 2. Visual Effects

#### Particle Systems
- **Soul Fire Particles**: Swirling patterns around boss during rising animation
- **End Rod Particles**: Vertical columns and enhanced trail effects
- **Enhanced Explosion Effects**: Multi-layered particle effects for impacts
- **Performance Optimization**: Particle count scales based on nearby players

#### Sound Design
- **Rising Animation**: Wither ambient sounds with dramatic timing
- **Projectile Launch**: Enhanced wither shoot sounds
- **Impact Effects**: Combination of explosion and dragon fireball sounds
- **Phase Transitions**: Distinct audio cues for each attack phase

### 3. Combat Mechanics

#### Wolves' Fear Removal
- Boss 1 no longer flees from wolves during combat
- Summoned skeleton warriors ignore wolves completely
- Maintains aggressive targeting behavior
- Preserves existing damage multipliers

#### Skeleton Spawn Prevention
- Skeleton warrior spawning is prevented during special attack casting
- Maintains normal summoning behavior outside special attacks
- Configurable through special attack settings

### 4. Configuration

#### Special Attack Settings
```yaml
acts:
  boss1:
    witherSkullAttack:
      specialAttack:
        enabled: true
        risingAnimation: true
        risingHeight: 12.0
        risingDuration: 40
        projectileCount: 36
        spherePattern: true
        preventSkeletonSpawn: true
        removeStunEffect: true
        particleEffects:
          soulFire: true
          endRod: true
          skullTrail: true
```

#### Performance Settings
- Particle count automatically scales based on nearby players
- Configurable update intervals for particle effects
- Optimized sphere pattern calculations

## Implementation Details

### Classes

#### BossRisingAnimation
- Handles the boss rising animation during special attacks
- Manages particle effects and smooth movement
- Performance-optimized particle spawning

#### WitherSkullProjectile
- Manages individual skull projectiles
- Handles rising and attack phases
- Enhanced visual effects and impact handling

#### SpecialAttackConfiguration
- Centralized configuration for special attack mechanics
- Validates all configuration values
- Provides easy access to attack settings

#### SafeZoneManager
- Manages safe zones during special attacks
- Enhanced particle effects without beacon effect
- Maintains protection functionality

### Integration Points

#### Act2Listener
- Main integration point for boss combat mechanics
- Handles special attack triggering and timing
- Manages skeleton summoning with fear removal
- Coordinates all attack phases

#### BossAttackState
- Tracks attack state and phase transitions
- Manages special attack phase timing
- Provides state information for other systems

## Performance Considerations

### Particle Optimization
- Particle count scales based on nearby player count
- Configurable update intervals for different effects
- Efficient sphere pattern algorithms

### Memory Management
- Proper cleanup of animation tasks
- Automatic removal of expired projectiles
- Efficient safe zone tracking

### Server Impact
- Maintains 20 TPS during boss combat
- Optimized for up to 20 players simultaneously
- Configurable quality settings for different server specs

## Troubleshooting

### Common Issues

#### Boss Not Rising
- Check if special attack is enabled in configuration
- Verify rising animation setting is true
- Ensure boss has valid location and AI state

#### Projectiles Not Spawning
- Verify projectile count is within valid range (16-64)
- Check if sphere pattern is enabled
- Ensure boss has valid target location

#### Safe Zones Not Working
- Verify safe zone manager is properly initialized
- Check beacon effect setting if vertical effects are unwanted
- Ensure protection radius is configured correctly

#### Wolves Still Causing Fear
- Verify fear removal method is called on boss spawn
- Check if skeleton warriors are properly modified
- Ensure NMS compatibility for current server version

### Debug Mode

Enable debug mode in configuration for detailed logging:

```yaml
logging:
  debugMode: true
```

This provides:
- Special attack phase transitions
- Particle effect counts and optimization
- Safe zone generation details
- Wolves' fear mechanic removal status

## Future Enhancements

### Potential Improvements
- Configurable particle effect types
- Dynamic difficulty scaling
- Additional special attack patterns
- Enhanced AI behavior options

### Extensibility
- Plugin architecture supports new attack phases
- Modular particle effect system
- Configurable behavior modifiers
- API for custom mechanics integration