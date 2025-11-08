# Spec Delta: Boss 1 Skull Behavior

This delta ensures that the wither skull projectiles from Boss #1's special attack do not cause environmental damage.

## ADDED Requirements

### Requirement: Prevent Skull Block Damage
-   The wither skull projectiles launched by Boss #1 during its special attack MUST NOT destroy any blocks upon impact.

#### Scenario: Skulls Impact Terrain
-   **Given** Boss #1 has launched its sphere of wither skull projectiles.
-   **When** a skull projectile impacts a block in the game world (e.g., stone, dirt, wood).
-   **Then** the skull projectile explodes with visual and sound effects.
-   **And** the impacted block and surrounding blocks are not broken or destroyed.
-   **And** players near the impact are still damaged if not in a safe zone.
