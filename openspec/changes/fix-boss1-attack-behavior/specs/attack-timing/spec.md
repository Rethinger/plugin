# Spec Delta: Boss 1 Attack Timing

This delta modifies the timing of Boss #1's special attack to be more immediate and impactful.

## MODIFIED Requirements

### Requirement: FR-005 (from `012-boss1-attack-fixes`) - System MUST cast skull projectiles in a sphere pattern that targets players outside safe zones

#### Scenario: Attack at Peak of Rise
-   **Given** Boss #1 initiates its special attack and begins the rising animation.
-   **When** the boss reaches the highest point of its ascent.
-   **Then** the safe zones have already been displayed.
-   **And** the skull projectiles are launched approximately 1 second after the boss reaches its peak.
