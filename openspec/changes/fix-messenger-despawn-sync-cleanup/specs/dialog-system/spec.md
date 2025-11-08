## MODIFIED Requirements
### Requirement: Dialog-NPC Synchronization
The dialog system SHALL trigger NPC actions at precise timing relative to dialog display.

#### Scenario: Exact despawn trigger timing
- **WHEN** dialog system displays the "*Посланник исчезает в тумане*" line
- **THEN** messenger despawn is triggered immediately (same tick)
- **AND** despawn animation begins without delay
- **AND** dialog continues normally while NPC disappears

#### Scenario: Reliable despawn trigger detection
- **WHEN** dialog contains messenger disappearance trigger
- **THEN** trigger is detected via exact text matching
- **AND** trigger works regardless of dialog speed settings
- **AND** trigger fires only once per dialog sequence
- **AND** debug logging records trigger timing for verification