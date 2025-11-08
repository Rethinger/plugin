## ADDED Requirements
### Requirement: Dialog-Driven Messenger Despawn Timing
The system SHALL trigger the Messenger despawn exactly during the dialog line that communicates the disappearance (e.g., “*Посланник исчезает в тумане*”), independent of dialog speed and localization.

#### Scenario: Disappearance line triggers despawn
- **GIVEN** a dialog sequence that includes a disappearance line (e.g., “*Посланник исчезает в тумане*”)
- **WHEN** that line begins to display to players
- **THEN** `NPCManager.despawnMessenger()` is invoked on the same tick
- **AND** any previously scheduled despawn timers are canceled
