# Spec Delta: Boss 1 Special Attack Cadence

## ADDED Requirements

### Requirement: Special Attack Cadence: Enforce Warrior Spawn Between Specials
The system MUST prevent back-to-back special attacks without a successful warrior-skeleton summon window between them.

#### Scenario: Cadence Gate by Summon Window
- **GIVEN** a special attack completes.
- **WHEN** the boss is eligible to use specials again.
- **THEN** the next special SHALL NOT start until at least one warrior-summon tick has been attempted and not skipped due to special state.
- **AND** a minimal spacing of N seconds (configurable, default 20s) since last special is enforced.

#### Scenario: Summon Attempt Tracking
- **GIVEN** summon scheduler runs every T seconds.
- **WHEN** a run occurs during any special phase.
- **THEN** mark it as "skipped by special".
- **AND** the cadence gate requires the next non-skipped summon run to occur before re-arming specials.
