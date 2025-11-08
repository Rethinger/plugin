## ADDED Requirements
### Requirement: Phase-aware timeouts and recovery
The special attack flow SHALL enforce timeouts per phase (RISING_ANIMATION, HEMISPHERE_FORMATION, SAFE_ZONES_APPEARING, FINAL_PREPARATION, HEMISPHERE_ATTACK) with phase-specific limits and SHALL transition to a safe recovery state on timeout without aborting the entire encounter.

#### Scenario: Formation timeout recovers to launch
- **WHEN** HEMISPHERE_FORMATION exceeds its timeout
- **THEN** the system SHALL emit a recovery log and transition to SAFE_ZONES_APPEARING if skulls >= minimal threshold, otherwise gracefully cancel the special and resume normal AI

#### Scenario: Safe zones timeout falls back
- **WHEN** SAFE_ZONES_APPEARING exceeds its timeout
- **THEN** the system SHALL launch the attack with current skull formation and mark missing zones in logs

### Requirement: Deterministic phase timings
The system SHALL define configurable timing windows for rise, formation, safe zones appearance, final prep, and launch, with defaults that produce a clearly visible rise/hover and prompt transition between phases.

#### Scenario: Visible rise and hover
- **WHEN** the special attack starts
- **THEN** the boss SHALL remain visually elevated/hovering for at least the configured minimum hover duration before formation proceeds

### Requirement: Hemisphere geometry constraints
The formation SHALL generate skull positions on a hemisphere around the boss within configured radius and vertical arc, ensuring no initial trajectories go straight up or straight to players prior to the launch phase.

#### Scenario: Hemisphere distribution respects bounds
- **WHEN** positions are generated
- **THEN** each skull position SHALL lie within [minRadius, maxRadius] and polar angle within [minTheta, maxTheta] relative to boss center

### Requirement: Safe zones per-player with shared anchors
Safe zones SHALL be anchored in world coordinates and visible per-player, guaranteeing at least one safe zone when players are present.

#### Scenario: Zones reflect active players
- **WHEN** there is at least one active player in range
- **THEN** at least one safe zone SHALL appear; per-player visibility SHALL map to common anchors

### Requirement: Structured debug logging
Each phase transition and recovery path SHALL produce structured logs including phase name, elapsed durations, counts (skulls, zones), and decisions taken.

#### Scenario: Logs show transition metrics
- **WHEN** transitioning between phases
- **THEN** logs SHALL include: phase_from, phase_to, elapsed_ms, skull_count, zone_count, timeout_hit (bool)

## MODIFIED Requirements
### Requirement: Boss 1 Special Attack Flow
The Boss 1 special attack flow SHALL proceed through phases: RISING_ANIMATION → HEMISPHERE_FORMATION → SAFE_ZONES_APPEARING → FINAL_PREPARATION → HEMISPHERE_ATTACK, with enforced timing, geometry constraints, and reliable safe zone presentation as specified by the added requirements.

#### Scenario: End-to-end execution without premature aborts
- **WHEN** the special attack is triggered under normal conditions
- **THEN** the sequence SHALL complete in order without a global timeout, producing a visible rise, a clear hemisphere shape, safe zones, and a timely launch
