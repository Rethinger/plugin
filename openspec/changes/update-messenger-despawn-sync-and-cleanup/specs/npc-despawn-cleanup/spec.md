## ADDED Requirements
### Requirement: Particle Cleanup at Messenger Location
The system SHALL ensure that after the Messenger despawns, there are no lingering particles at the Messenger's last location.

#### Scenario: Cleanup after vanish animation
- **GIVEN** the Messenger is performing its vanish effect
- **WHEN** the vanish animation completes
- **THEN** particle emitters around the last location are stopped/canceled
- **AND** residual particles within a small radius are cleared so the spot is visually clean
