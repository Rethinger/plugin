## ADDED Requirements
### Requirement: Clean NPC Despawn Visual State
NPC despawn SHALL leave no residual visual artifacts at the NPC's last location, and SHALL NOT trigger any player-centered visual effects unless explicitly configured by capability-specific rules.

#### Scenario: No particles remain at NPC location
- **WHEN** an NPC is despawned (natural or programmatic)
- **THEN** all particle emitters and scheduled particle tasks tied to that NPC are cancelled
- **AND** no new particles are emitted at that location after despawn completes

#### Scenario: No player FX on NPC despawn by default
- **GIVEN** default configuration
- **WHEN** an NPC is despawned near any player
- **THEN** no visual effects are spawned on or around the player as a result of the NPC despawn event

#### Scenario: Configurable exceptions (explicit opt-in)
- **GIVEN** a capability or feature explicitly configured to show player FX on NPC despawn
- **WHEN** an NPC belonging to that feature despawns
- **THEN** only the configured visual effect(s) are emitted
- **AND** effect lifetime is bounded and cleaned up according to config
