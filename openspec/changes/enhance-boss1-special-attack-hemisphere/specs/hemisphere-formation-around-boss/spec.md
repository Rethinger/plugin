# Hemisphere Formation Around Boss Specification

## ADDED Requirements

### Requirement: Boss-Centered Hemisphere Formation
The system SHALL create a hemisphere of wither skulls that forms around the BOSS position (not player positions) with 14+ skulls dynamically positioned based on player count.

#### Scenario:
When the Boss1 initiates its special attack after meeting warrior summon requirements, the hemisphere formation algorithm shall calculate skull positions relative to the boss's current location, with skulls emerging from ground positions around the boss and rising to form a complete hemisphere structure centered on the boss.

### Requirement: Phased Attack Animation
The boss SHALL execute a complete phased animation sequence with precise timing for each phase.

#### Scenario:
When the hemisphere attack begins, the boss shall execute: rising animation (1.2s) → hovering wait (1s) → hemisphere formation (2s) → safe zones appearing (1.5s) → final preparation (1s) → attack execution → cooldown, with each phase triggering specific visual and mechanical effects.

### Requirement: Dynamic Safe Zone Scaling
The system SHALL generate safe zones with dynamic scaling based on player count and solo-player accommodations.

#### Scenario:
When safe zones are created during the special attack, the system shall generate `Math.min(players + 1, 6)` zones with 3.5-4 block radius (automatically increased to 4 blocks for solo players), distributed strategically around the boss area to provide meaningful tactical choices.

### Requirement: Warrior Summon Prerequisites
The special attack SHALL require completion of warrior summon prerequisites before activation.

#### Scenario:
When players engage the boss, the special attack shall only be available after the boss has successfully summoned minimum 2 warrior waves, with a 12-second waiting period after the second wave before attack initiation, creating a clear combat rhythm.

### Requirement: Summon Freeze During Special Attacks
The warrior summon system SHALL be suspended during all special attack phases.

#### Scenario:
During any special attack phase (hemisphere formation or arrow phase), the warrior summon system shall be completely frozen and resumed only after the boss completes descent animation, ensuring special attacks remain the focus during execution.

## MODIFIED Requirements

### Requirement: Special Attack State Management
The BossAttackState SHALL be enhanced with comprehensive phase tracking for special attacks.

#### Scenario:
The BossAttackState shall be enhanced with new SpecialAttackPhase enum including RISING_ANIMATION, HEMISPHERE_FORMATION, SAFE_ZONES_APPEARING, FINAL_PREPARATION, HEMISPHERE_ATTACK, and COOLDOWN states, with proper state transitions and timeout handling for each phase.

### Requirement: Visual Effects Integration
The visual effects system SHALL provide comprehensive feedback during hemisphere formation.

#### Scenario:
During hemisphere formation, the system shall display ground-cracking effects as skulls emerge from beneath the boss area, with soul flame particles and progressive hemisphere visualization that clearly indicates formation progress to players.

### Requirement: Player Damage and Safe Zone Logic
The damage system SHALL properly integrate with safe zone mechanics for fair gameplay.

#### Scenario:
When hemisphere attack executes, players outside safe zones shall receive damage from projectiles while players inside safe zones remain unharmed, with clear visual boundary indicators that allow players to understand safe vs danger areas.

### Requirement: Performance and Resource Management
The system SHALL maintain optimal performance during complex special attack sequences.

#### Scenario:
During special attacks with multiple players, the system shall maintain acceptable performance levels through proper particle effect throttling and projectile cleanup mechanisms, ensuring smooth gameplay even with complex visual effects.