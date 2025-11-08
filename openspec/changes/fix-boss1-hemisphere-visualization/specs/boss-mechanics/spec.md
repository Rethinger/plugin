# Boss 1 Hemisphere Attack Visualization and Recovery Fixes

## MODIFIED Requirements

### Requirement: HC-BUGFIX-001 Accurate Phase Timeout Calculation (MODIFIED)
The system SHALL calculate phase timeout durations based on tick counts multiplied by 50ms per tick to ensure timeout thresholds match actual execution time.

#### Scenario: Formation Phase Timeout Calculation
- **GIVEN** HEMISPHERE_FORMATION phase expects 75 ticks for skull formation.
- **WHEN** timeout threshold is calculated.
- **THEN** timeout SHALL be set to 3750ms (75 ticks × 50ms).
- **AND** timeout warnings SHALL only trigger if elapsed time exceeds 3750ms.
- **AND** phase SHALL NOT timeout prematurely at 3000ms.

**Bug Context**: Server logs (line 350) show timeout warning at 3749ms when configured timeout was 3000ms, causing false positive timeout warnings despite successful formation completing on schedule.

### Requirement: HC-BUGFIX-002 Recovery Logic Phase Advancement (MODIFIED)
The system SHALL advance to next phase when recovery detects viable formation instead of forcing immediate stop.

#### Scenario: Recovery with Viable Formation
- **GIVEN** Formation phase times out but skull count >= MIN_SKULL_THRESHOLD (8).
- **WHEN** recovery handler evaluates formation viability.
- **THEN** system SHALL call `advanceToNextPhase()` to proceed to SAFE_ZONES_APPEARING.
- **AND** system SHALL preserve existing display skulls and safe zones.
- **AND** system SHALL log recovery success with skull count and next phase.
- **AND** attack SHALL continue to completion.

#### Scenario: Recovery with Insufficient Formation
- **GIVEN** Formation phase times out and skull count < MIN_SKULL_THRESHOLD (8).
- **WHEN** recovery handler evaluates formation viability.
- **THEN** system SHALL call `forceStop()` to abort attack.
- **AND** system SHALL cleanup display skulls and safe zones.
- **AND** system SHALL log recovery failure with skull count and threshold.

**Bug Context**: Server logs (lines 350-356) show recovery confirmed viable formation (14 skulls > 8 threshold) but immediately called `forceStop()` instead of advancing to next phase, causing attack to abort prematurely.

### Requirement: HC-BUGFIX-003 Rising Animation Visual Effects (MODIFIED)
The system SHALL display particle effects and sounds during RISING_ANIMATION phase for dramatic visual feedback.

#### Scenario: Boss Rising Particle Effects
- **GIVEN** Boss enters RISING_ANIMATION phase.
- **WHEN** boss position updates during rise.
- **THEN** SOUL particles SHALL spawn around boss hitbox with upward velocity.
- **AND** PORTAL particles SHALL spawn at boss feet position.
- **AND** particle intensity SHALL increase progressively (low → high).
- **AND** particles SHALL be visible to all players within 64 blocks.

#### Scenario: Boss Rising Sound Effects
- **GIVEN** Boss enters RISING_ANIMATION phase.
- **WHEN** rise animation starts.
- **THEN** ENTITY_WITHER_SPAWN or ENTITY_ENDER_DRAGON_GROWL sound SHALL play at boss location.
- **AND** BLOCK_PORTAL_AMBIENT sound SHALL loop during entire rise duration.
- **AND** sounds SHALL be audible to all players within normal sound range.

**Bug Context**: Current implementation lacks visual/audio feedback during rising animation, making phase transition unclear to players.

### Requirement: HC-BUGFIX-004 Safe Zone Lifecycle Management (MODIFIED)
The system SHALL maintain safe zones through entire attack sequence without premature cleanup during recovery.

#### Scenario: Safe Zone Persistence Through Attack
- **GIVEN** Safe zones created during SAFE_ZONES_APPEARING phase.
- **WHEN** attack progresses to SKULLS_ATTACKING phase.
- **THEN** safe zones SHALL remain visible and active.
- **AND** safe zones SHALL only cleanup when attack completes or truly fails.
- **AND** safe zones SHALL NOT cleanup during successful recovery transitions.

#### Scenario: Display Skull Preservation During Recovery
- **GIVEN** Display skulls exist during formation phase recovery.
- **WHEN** recovery determines formation is viable.
- **THEN** display skulls SHALL be preserved for attack phase.
- **AND** display skulls SHALL NOT be removed until attack completes.
- **AND** display skull cleanup SHALL coordinate with safe zone cleanup.

**Bug Context**: Server logs (lines 357-374) show premature cleanup of all display skulls and safe zones immediately after recovery decision, preventing attack execution.

### Requirement: HC-BUGFIX-005 Phase Transition Integrity (MODIFIED)
The system SHALL enforce complete phase sequence: RISING_ANIMATION → HEMISPHERE_FORMATION → SAFE_ZONES_APPEARING → SKULLS_ATTACKING → COMPLETE.

#### Scenario: Complete Phase Flow Execution
- **GIVEN** Boss initiates special attack.
- **WHEN** attack progresses through phases.
- **THEN** all phases SHALL execute in order without skipping.
- **AND** recovery SHALL allow phase advancement when viable.
- **AND** attack SHALL complete with `completed=true` and `final_phase=COMPLETE`.
- **AND** cleanup SHALL only occur after COMPLETE phase or critical failure.

#### Scenario: Attack Completion Validation
- **GIVEN** Attack reaches SKULLS_ATTACKING phase.
- **WHEN** skulls finish attacking players outside safe zones.
- **THEN** attack SHALL transition to COMPLETE phase.
- **AND** attack state SHALL record `completed=true`.
- **AND** attack state SHALL record `final_phase=COMPLETE`.
- **AND** logs SHALL confirm successful attack execution.

**Bug Context**: Server logs show attack ending with `completed=false final_phase=NONE`, indicating phase sequence broke before attack could execute.

## Implementation Notes

### Affected Components
- `BossSpecialAttackManager.java` - Phase timeout calculations and recovery logic
- `BossRisingAnimation.java` - Particle and sound effects during rise
- `HemisphereFormation.java` - Display skull lifecycle coordination
- `SafeZoneManager.java` - Safe zone persistence through attack phases

### Timing Calculations
```java
// CORRECT: Tick-based timeout calculation
int FORMATION_TICKS = 75;
long FORMATION_TIMEOUT_MS = FORMATION_TICKS * 50L; // = 3750ms

// INCORRECT: Fixed millisecond value
long FORMATION_TIMEOUT_MS = 3000; // Does not match 75 ticks
```

### Recovery Logic Flow
```
Formation Phase Timeout Detected
    ↓
Check Skull Count
    ↓
    ├─→ skull_count >= 8 (VIABLE)
    │   └─→ advanceToNextPhase() → Continue to SAFE_ZONES_APPEARING
    │
    └─→ skull_count < 8 (INSUFFICIENT)
        └─→ forceStop() → Cleanup and abort attack
```

### Debug Logging Requirements
All fixes SHALL include debug logging with:
- Phase names (current and next)
- Elapsed time in milliseconds
- Skull counts and thresholds
- Recovery decisions and outcomes
- Cleanup coordination status
