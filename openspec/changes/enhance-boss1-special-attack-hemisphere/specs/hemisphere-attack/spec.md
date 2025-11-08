# Hemisphere Attack Special Move

## ADDED Requirements

### Requirement: HC-SPEC-001 Hemisphere Formation Phase
The system SHALL create a hemisphere formation of skulls rising from ground around the boss during special attack.

#### Scenario: Hemisphere Formation
- **GIVEN** Boss triggers special attack and enters rising animation.
- **WHEN** rising completes and boss enters hovering phase.
- **THEN** 12-16 skulls spawn at ground level around boss location.
- **AND** skulls rise from ground to form hemisphere pattern over 3 seconds.
- **AND** hemisphere radius is 8-10 blocks from boss center.
- **AND** visual effects show skull rising with particle trails.

### Requirement: HC-SPEC-002 Safe Zone Generation
The system SHALL utilize existing SafeZoneManager to generate safe zones count equal to player count plus one during hemisphere formation.

#### Scenario: Safe Zone Creation
- **GIVEN** Hemisphere formation begins.
- **WHEN** hemisphere reaches 30% completion.
- **THEN** existing SafeZoneManager creates safe zones (player count + 1 total).
- **AND** each safe zone uses existing 3x3 block radius configuration.
- **AND** zones positioned using existing generateRandom() method to avoid overlap.
- **AND** zone boundaries use existing visual particle system.

### Requirement: HC-SPEC-003 Warrior Summon Prerequisite
The system SHALL require minimum 2 warrior summon waves before allowing first special attack.

#### Scenario: Warrior Summon Requirement
- **GIVEN** Boss starts combat phase.
- **WHEN** special attack conditions are met.
- **THEN** system checks warrior summon wave count in BossAttackState.
- **AND** special attack only triggers if 2+ warrior summon waves completed.
- **AND** each wave summons 2-3 warriors (existing logic).
- **AND** warrior waves occur every 10 seconds (200L timer).
- **AND** existing recordSuccessfulWarriorSummon() tracking is utilized.
- **AND** 15-second wait after second wave before special attack.

#### Scenario: Warrior Summon Freeze During Special Attack
- **GIVEN** Boss enters special attack sequence.
- **WHEN** special attack phases execute.
- **THEN** warrior summon timer (200L) is paused.
- **AND** no new warrior waves spawn during entire special attack sequence.
- **AND** summon timer resumes after boss returns to ground.
- **AND** normal 10-second interval resumes from pause point.

### Requirement: HC-SPEC-004 Multi-Phase Attack Sequence
The system SHALL execute special attack in 6 sequential phases with precise timing, keeping boss continuously airborne from lift to post-attack wait.

#### Scenario: Phase Transitions
- **GIVEN** Boss initiates special attack.
- **WHEN** attack sequence starts.
- **THEN** phases execute in order:
  1. RISING_ANIMATION - Boss rises to hover height.
  2. HEMISPHERE_FORMATION - Skulls form hemisphere over 3 seconds while boss hovers.
  3. SAFE_ZONES_APPEARING - Safe zones appear sequentially while boss continues hovering.
  4. HEMISPHERE_ATTACK - Skulls launch at targets while boss remains airborne.
  5. POST_ATTACK_WAIT - Boss hovers for 1 second after attack, then descends.
  6. GROUND_TOUCHDOWN - Boss descends smoothly to ground.

### Requirement: HC-SPEC-005 Player Safe Zone Mechanics
The system SHALL prevent damage to players within safe zones during hemisphere attack phase.

#### Scenario: Safe Zone Protection
- **GIVEN** Player stands within marked safe zone boundaries.
- **WHEN** hemisphere attack phase triggers skull launches.
- **THEN** skulls ignore players inside safe zones for targeting.
- **AND** players receive visual confirmation of safety status.
- **AND** players can move freely within safe zone without damage.
- **BUT** players take damage when exiting safe zone during attack.

### Requirement: HC-SPEC-006 Hemisphere Attack Targeting
The system SHALL target hemisphere skulls only at players outside safe zones.

#### Scenario: Selective Targeting
- **GIVEN** 2 players in safe zones, 2 players outside zones.
- **WHEN** hemisphere attack phase launches skulls.
- **THEN** all hemisphere skulls target only the 2 exposed players.
- **AND** skulls distribute evenly between available targets.
- **AND** players in safe zones receive zero damage from attack.

### Requirement: HC-SPEC-007 Phase 2 Arrow Hemisphere Attack
The system SHALL implement identical hemisphere mechanics for phase 2 using arrows with instant damage.

#### Scenario: Arrow Hemisphere Formation
- **GIVEN** Boss enters phase 2 special attack.
- **WHEN** hemisphere formation begins.
- **THEN** 12-16 arrows spawn at ground level around boss location.
- **AND** arrows rise from ground to form hemisphere pattern over 3 seconds.
- **AND** arrows deal exactly 1 damage instant on hit.
- **AND** arrows ignore player armor/protections.

#### Scenario: Arrow Safe Zone Integration
- **GIVEN** Arrow hemisphere attack activates.
- **WHEN** arrows launch at players.
- **THEN** existing SafeZoneManager protects players in zones.
- **AND** arrows only target players outside safe zones.
- **AND** safe zone detection uses existing isInSafeZone() method.

#### Scenario: Arrow Projectile Configuration
- **GIVEN** Arrow projectiles created for special attack.
- **WHEN** arrows spawn.
- **THEN** arrows use existing Arrow entity class with custom damage.
- **AND** arrow damage set to exactly 1 heart (2 health points).
- **AND** arrows set to bypass armor and enchantments.
- **AND** arrows use existing particle trail system for visual effects.

## MODIFIED Requirements

### Requirement: HC-SPEC-008 Boss Special Attack State Management (MODIFIED)
The system SHALL track 6 special attack phases instead of current 2-phase system, maintaining airborne state during attack with post-attack hover.

#### Scenario: Extended State Management
- **GIVEN** Boss begins special attack sequence.
- **WHEN** phase transitions occur.
- **THEN** system correctly manages all states:
  - RISING_ANIMATION (existing)
  - HEMISPHERE_FORMATION (new)
  - SAFE_ZONES_APPEARING (new)
  - HEMISPHERE_ATTACK (modified from CASTING_SKULLS)
  - POST_ATTACK_WAIT (new)
  - GROUND_TOUCHDOWN (new)
  - COOLDOWN (existing)

#### Scenario: Post-Attack Hover and Descent
- **GIVEN** Boss completes HEMISPHERE_ATTACK phase.
- **WHEN** transitioning to POST_ATTACK_WAIT.
- **THEN** boss hovers at airborne position for 1 second.
- **AND** then smoothly descends to ground position in GROUND_TOUCHDOWN phase.
- **AND** attack state transitions to COOLDOWN after descent complete.

### Requirement: HC-SPEC-009 Skull Projectile Behavior (MODIFIED)
The system SHALL modify skull projectiles to rise from ground to hemisphere before attacking.

#### Scenario: Ground Rising Behavior
- **GIVEN** Skull projectile created for special attack.
- **WHEN** projectile spawns.
- **THEN** skull appears at ground level beneath hemisphere position.
- **AND** skull rises smoothly to hemisphere position over 3 seconds.
- **AND** rising phase displays particle effects.
- **AND** after hemisphere formation, skull targets players outside safe zones.

### Requirement: HC-SPEC-010 Visual Effects Coordination (MODIFIED)
The system SHALL coordinate visual effects across all attack phases for cohesive experience.

#### Scenario: Phase-Appropriate Effects
- **GIVEN** Boss special attack starts.
- **WHEN** transitioning between phases.
- **THEN** visual effects match current phase:
  - **Hovering**: anticipation particles around boss
  - **Formation**: rising skull effects from ground
  - **Safe Zones**: boundary highlighting and glow effects
  - **Preparation**: charging and power-up effects
  - **Attack**: dramatic launch and impact effects

## REMOVED Requirements

### Requirement: HC-SPEC-011 Simple Ground Skull Attack (REMOVED)
The current simple ground-based skull spawning system SHALL BE REMOVED.

**Justification**: This basic mechanic is replaced by the more sophisticated hemisphere formation system with safe zones, providing enhanced visual spectacle and tactical depth for improved gameplay experience.