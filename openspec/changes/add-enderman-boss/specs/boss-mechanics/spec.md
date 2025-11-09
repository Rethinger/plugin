## ADDED Requirements

### Requirement: Enderman Boss Combat System
The plugin SHALL provide a second boss encounter featuring an Enderman with teleportation abilities, clone mechanics, and two-phase combat system.

#### Scenario: Boss Initialization
- **WHEN** Act 3 progression reaches the boss encounter trigger
- **THEN** the system SHALL spawn an Enderman boss with 500 HP, 1.25x scale, Resistance 2, and Strength 1 effects
- **AND** the boss SHALL appear with a vertical rift entrance sequence lasting 5 seconds
- **AND** the boss SHALL aggro on players within 75 blocks

#### Scenario: Phase 1 Combat Behavior
- **WHEN** boss health is above 50% during combat
- **THEN** the boss SHALL counter-attack every 4th player attack by teleporting behind the player
- **AND** the boss SHALL perform chaotic teleportation sequences every 25 seconds
- **AND** each teleportation sequence SHALL include 10-15 teleports with clone creation
- **AND** the boss SHALL heal 0.3 HP for each point of damage dealt to players (vampirism)

#### Scenario: Clone Mechanics
- **WHEN** the boss creates clones during teleportation
- **THEN** each clone SHALL inherit the boss's current health but without Resistance/Strength effects
- **AND** clones SHALL appear identical to the boss but without glow effects
- **AND** clones SHALL disappear after receiving one player hit with shadow disintegration effects
- **AND** clones SHALL target players within 75 blocks like the main boss

#### Scenario: Phase 2 Transition
- **WHEN** boss health drops to 50% or below
- **THEN** the boss SHALL transition to Phase 2 combat behavior
- **AND** chaotic teleportation with clone creation SHALL be disabled
- **AND** the boss SHALL gain new clone wave and healing abilities

#### Scenario: Phase 2 Clone Waves
- **WHEN** boss is in Phase 2
- **THEN** the boss SHALL spawn 10-20 clones every 15 seconds in a circular pattern
- **AND** each clone wave SHALL complete spawning within 3 seconds
- **AND** clones SHALL behave identically to Phase 1 clones

#### Scenario: Healing Shield Mechanic
- **WHEN** the boss attempts to heal in Phase 2 (every 35 seconds)
- **THEN** the boss SHALL stop movement for 2 seconds to prepare healing
- **AND** a purple particle shield SHALL form around the boss requiring 5 player hits to break
- **AND** if the shield is broken, the boss SHALL be stunned for 3 seconds and healing canceled
- **AND** if the shield is not broken, the boss SHALL heal for 3 seconds

#### Scenario: Water Immunity and Weather Control
- **WHEN** the boss or its clones contact water
- **THEN** they SHALL not receive water damage
- **AND** the boss SHALL freeze water blocks around players within 10 blocks
- **AND** if it starts raining, the weather SHALL immediately clear
- **AND** the boss SHALL maintain all combat abilities in water

#### Scenario: Anti-Build Mechanics
- **WHEN** a player is positioned above the boss within 5 blocks vertically
- **THEN** the boss SHALL knock the player back with significant force
- **AND** the boss SHALL break blocks within 3 blocks to prevent fortification
- **AND** the boss SHALL prevent block placement within 5 blocks during combat

#### Scenario: Visual Effects
- **WHEN** the boss teleports
- **THEN** purple and end rod particles SHALL mark both source and destination
- **AND** when clones die, shadow disintegration effects SHALL play with smoke and witch particles
- **AND** the healing shield SHALL display pulsing purple totem particles
- **AND** vampirism healing SHALL show red particle effects

#### Scenario: Configuration Parameters
- **WHEN** administrators modify boss configuration
- **THEN** all timing intervals, damage values, and particle effects SHALL be customizable
- **AND** health, scale, and effect levels SHALL be configurable
- **AND** visual effect intensity and performance settings SHALL be adjustable

#### Scenario: Performance Optimization
- **WHEN** multiple clones are active simultaneously
- **THEN** the system SHALL maintain 60+ server performance
- **AND** particle effects SHALL be optimized based on player distance
- **AND** clone entities SHALL be immediately cleaned up after use
- **AND** teleportation events SHALL be queued to prevent conflicts

#### Scenario: Combat Progression
- **WHEN** the boss is defeated
- **THEN** the story progression SHALL advance to the next act
- **AND** all clones and effects SHALL be properly cleaned up
- **AND** the boss area SHALL reset to normal state
- **AND** player achievements SHALL be awarded for boss completion