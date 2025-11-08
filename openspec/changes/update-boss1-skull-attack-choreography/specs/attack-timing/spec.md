# Spec Delta: Boss 1 Attack Timing

## MODIFIED Requirements

### Requirement: FR-005 (from `012-boss1-attack-fixes`) - System MUST cast skull projectiles in a sphere pattern that targets players outside safe zones

#### Scenario: Immediate Attack at Peak (No Delay)
- **GIVEN** Boss #1 initiates its special attack and begins the rising animation.
- **WHEN** the boss reaches the highest point of its ascent.
- **THEN** the safe zones have already been displayed.
- **AND** the skull projectiles are launched with no additional delay (<= 0.2s).

#### Scenario: Extended Hover Variant (If Configured)
- **GIVEN** rising animation completes.
- **WHEN** `specialAttack.hoverDurationTicks > 0`.
- **THEN** the boss remains airborne for that duration before launch, with sustained warning effects.
- **AND** launch occurs immediately after hover ends.
