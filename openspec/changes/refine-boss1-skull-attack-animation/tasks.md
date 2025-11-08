# Tasks: Refine Boss 1 Skull Attack Animation

1.  **[Code] Implement Ground-Gather Animation:**
    -   Create the visual effect for skulls rising from the ground.
    -   Choreograph the movement of the skulls to form a lower semi-circle around the boss.
    -   Ensure this animation plays after the boss has landed.
    -   *Depends on: Nothing*

2.  **[Code] Implement Dramatic Pause:**
    -   Add a 1-second delay after the skulls have formed the semi-circle.
    -   Create and apply a "charging up" visual effect during the pause.
    -   *Depends on: Task 1*

3.  **[Code] Implement Coordinated Release:**
    -   Modify the skull projectile launch logic to fire from the semi-circle formation.
    -   Add a new sound and/or visual effect for the release.
    -   *Depends on: Task 2*

4.  **[Config] Expose Animation Timings:** ✅
    -   Make the durations for the gather phase and the pause configurable.
    -   Add sensible defaults to the configuration files.
    -   *Depends on: Task 1, 2*

5.  **[Validation] Gameplay Testing:** ✅
    -   Test the new animation in-game to ensure it looks and feels right.
    -   Verify that the attack is still effective and dodgeable.
    -   Check for any visual glitches or performance issues.
    -   *Depends on: Task 3*

6.  **[Docs] Update Spec Documents:** ✅
    -   Update any related spec documents to reflect the new animation details.
    -   Ensure the `skull-choreography` spec is consistent with the new implementation.
    -   *Depends on: Task 5*
