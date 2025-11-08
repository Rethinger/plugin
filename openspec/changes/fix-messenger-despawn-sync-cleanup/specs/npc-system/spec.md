## MODIFIED Requirements
### Requirement: Messenger Despawn Animation
The system SHALL provide smooth messenger NPC despawn animation synchronized with dialog timing.

#### Scenario: Messenger disappears at dialog line
- **WHEN** dialog line "*Посланник исчезает в тумане*" is displayed
- **THEN** messenger NPC immediately starts despawn animation
- **AND** all particle effects around NPC are cleared
- **AND** NPC shrinks and fades over 5 seconds
- **AND** no residual particles remain after despawn completes

#### Scenario: Enhanced particle cleanup
- **WHEN** messenger despawn animation begins
- **THEN** immediate particle cleanup occurs in 15 block radius
- **AND** final particle cleanup occurs after animation completes
- **AND** all aura and idle animation tasks are cancelled
- **AND** visual artifacts are completely removed