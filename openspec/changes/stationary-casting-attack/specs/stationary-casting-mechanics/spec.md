# Stationary Casting Attack Mechanics Specification

## ADDED Requirements

### Requirement: Stationary Boss Casting Animation
The boss SHALL remain stationary on the ground during the entire special attack sequence with a 3-second casting animation that includes arm raising, particle effects, and sound buildup.

#### Scenario:
When the Boss1 initiates the stationary special attack, the boss SHALL remain at its current position without rising into the air, perform a casting animation with raised arms, spawn SOUL_FIRE_FLAME particles during the first 0.75 seconds, intensify to DRAGON_BREATH and END_ROD particles during the middle 1.5 seconds, and culminate with a FLASH particle effect and high-pitched sound in the final 0.75 seconds.

### Requirement: Dynamic Safe Zone Generation
The system SHALL generate safe zones with 1.5-block radius positioned strategically within the 15-block attack radius, with the number of zones calculated as `Math.min(players + 1, 5)`.

#### Scenario:
When the stationary casting attack begins, the system SHALL calculate safe zone positions based on player count: 1 player gets 2 zones (boss proximity + opposite side), 2 players get 3 zones (triangle formation), 3 players get 4 zones (diamond formation), and 4+ players get 5 zones maximum (pentagon formation), with all zones appearing sequentially during the 3-second casting period.

### Requirement: Danger Zone Visualization
The system SHALL visualize the dangerous 15-block radius area around the boss using red particle effects that clearly indicate where damage will be applied, while leaving particle-free gaps at safe zone locations.

#### Scenario:
During the 3-second casting phase, the system SHALL spawn concentric red particle rings expanding from the boss position, create a grid of red DUST particles at player eye level (Y+1.5), pulse particle intensity based on casting progress, and automatically exclude particle spawning within 1.5 blocks of any safe zone location.

### Requirement: Evoker Fangs Area Attack
The boss SHALL execute an area-denial attack using minecraft:evoker_fangs distributed across the 15-block radius circle in 3 timed waves, with no fangs spawning within safe zones.

#### Scenario:
After the 3-second casting completes, the system SHALL calculate a 5x5 grid of potential fang positions covering the 15-block radius (25 total fangs), filter out positions within 1.5 blocks of safe zones, spawn the first wave of 8-9 fangs immediately, the second wave after 0.5 seconds (10 ticks), and the final wave after 1 second (20 ticks), with each wave creating visible ground crack effects and attack sounds.

### Requirement: Safe Zone Damage Immunity
Players located within 1.5-block radius safe zones SHALL receive no damage from evoker fangs while players outside safe zones SHALL receive full damage from fang attacks.

#### Scenario:
When evoker fangs execute their attack, the system SHALL check each player's position against safe zone locations, automatically apply damage immunity to players within safe zones, ensure fangs cannot spawn inside safe zones, and maintain visual distinction between safe (particle-free) and dangerous (red particle) areas throughout the attack.

## MODIFIED Requirements

### Requirement: Boss Attack State Management
The BossAttackState SHALL be modified to support stationary casting phases instead of airborne hemisphere phases.

#### Scenario:
The BossAttackState SHALL be enhanced with new stationary phases: CASTING_PREPARATION, SAFE_ZONES_APPEARING, DANGER_ZONE_VISUALIZATION, FANGS_ATTACK_EXECUTION, and COOLDOWN, with proper timing management for the 3-second casting period and 2-second attack execution, while maintaining compatibility with existing warrior summon tracking systems.

### Requirement: Visual Effects Integration
The visual effects system SHALL provide comprehensive feedback for the stationary casting attack including danger zone visualization and safe zone distinction.

#### Scenario:
During the stationary casting attack, the system SHALL display progressive particle effects from the boss (SOUL_FIRE_FLAME → DRAGON_BREATH/END_ROD → FLASH), create red danger zone particles with pulsing intensity, maintain particle-free areas at safe zone locations, and provide clear audio cues with escalating pitch and volume throughout the 3-second casting period.

### Requirement: Performance Optimization
The system SHALL maintain optimal performance during stationary casting attacks through efficient particle management and entity cleanup.

#### Scenario:
During the stationary casting attack with multiple players, the system SHALL limit particle spawning to 100 particles per tick maximum, use distance-based culling for particle effects (30-block radius), batch evoker fangs spawning into waves of 8-9 entities, and ensure proper cleanup of all particle effects and fang entities within 2 seconds of attack completion.

### Requirement: Configuration Flexibility
The stationary casting attack SHALL be fully configurable through configuration files for radius, timing, particle intensity, and difficulty scaling.

#### Scenario:
The configuration system SHALL support adjustable parameters for casting duration (default 60 ticks), attack radius (default 15.0 blocks), safe zone radius (default 1.5 blocks), maximum safe zones (default 5), evoker fangs count (default 25), wave timing intervals, particle density multipliers, and damage values, with all changes requiring only plugin reload to take effect.