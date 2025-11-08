# Spec Delta: Boss 1 Skull Choreography

## ADDED Requirements

### Requirement: Skull Choreography: Ground Gather, Boss Converge, Hemisphere Release
The special attack SHALL visually choreograph skulls rising from ground towards the boss before release, then emit a downward hemisphere, and maintain a spherical formation in flight.

#### Scenario: Ground-To-Boss Gather
- **GIVEN** special attack enters CASTING_SKULLS.
- **WHEN** gather starts (<= 0.5s).
- **THEN** projectiles or visible traces travel upward from multiple ground points to the boss, without harming terrain or players.
- **AND** gather completes within 0.8–1.5s.

#### Scenario: Downward Hemisphere Release
- **GIVEN** gather completes.
- **WHEN** release triggers.
- **THEN** initial emission directions cover a downward hemisphere around the boss.
- **AND** safe zones remain valid during release.

#### Scenario: Maintain Spherical Pattern In Flight
- **GIVEN** skulls are released.
- **WHEN** they travel toward their targets.
- **THEN** relative positions approximate a sphere pattern (within tolerance) while homing.
- **AND** block damage remains disabled.
