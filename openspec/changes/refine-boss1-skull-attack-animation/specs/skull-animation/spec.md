# Spec Delta: Boss 1 Skull Attack Animation

## MODIFIED Requirements

### Requirement: Refined Skull Attack Choreography ✅ IMPLEMENTED
The special attack SHALL be visually choreographed with a more dramatic sequence after the boss lands.

#### Scenario: Ground Gather into Semi-Circle ✅ IMPLEMENTED
- **GIVEN** the boss has landed and initiates a skull attack.
- **WHEN** the skull gathering sequence begins.
- **THEN** skulls (or visual representations of them) shall rise from the ground towards the boss.
- **AND** the skulls shall arrange themselves into a lower semi-circle around the boss.
- **AND** this gathering phase shall last between 1.0 and 1.5 seconds.

#### Scenario: Dramatic Pause ✅ IMPLEMENTED
- **GIVEN** the skulls have formed a semi-circle around the boss.
- **WHEN** the boss pauses.
- **THEN** the boss shall hold the skulls in the semi-circle formation for approximately 1 second.
- **AND** during this pause, visual effects shall build up to indicate an impending attack.

#### Scenario: Coordinated Release ✅ IMPLEMENTED
- **GIVEN** the dramatic pause has ended.
- **WHEN** the boss releases the skulls.
- **THEN** the skulls shall be launched from their positions in the semi-circle towards their targets.
- **AND** the release shall be accompanied by a distinct visual and/or sound effect.

## Implementation Notes

### Configuration Options
The animation timings are now configurable through the following settings:
- `groundGatherDurationTicks`: Duration of ground gather phase (default: 30 ticks = 1.5 seconds)
- `groundGatherPauseTicks`: Duration of pause after gather (default: 20 ticks = 1 second)

### Technical Implementation
- Added `GROUND_SKULL_GATHER` state to `BossAttackState` enum
- Implemented `startGroundSkullGatherAnimation()` method in `Act2Listener`
- Updated `SpecialAttackConfiguration` class with new timing parameters
- Animation uses configurable particle effects and smooth transitions
