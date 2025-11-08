# Spec Delta: Boss 1 Naming and Loot

This delta corrects the name of Boss #1 and ensures its loot table is correctly applied upon defeat.

## MODIFIED Requirements

### Requirement: Key Entity `Boss 1` (from `012-boss1-attack-fixes`)
-   The Skeleton Lord boss entity that performs special attacks, now with modified behavior to remove stun and wolves' fear.
-   The boss entity MUST be named "Повелитель скелетов" (or its localized equivalent) upon spawning.

#### Scenario: Boss Spawns
-   **Given** the conditions to summon Boss #1 are met.
-   **When** the boss entity is spawned into the world.
-   **Then** its display name is "Повелитель скелетов" (with color codes).
-   **And** it is not named "skeleton".

## ADDED Requirements

### Requirement: Correct Loot Drop on Death
-   The system MUST correctly identify the defeat of Boss #1 and drop the intended loot.

#### Scenario: Boss is Defeated
-   **Given** Boss #1, named "Повелитель скелетов", is present in the world.
-   **When** players defeat the boss.
-   **Then** the `onBoss1Death` event is triggered.
-   **And** the boss's specific loot table is used to drop items.
