# Tasks: Fix Boss 1 Attack Behavior

This list outlines the tasks required to implement the proposed changes to Boss #1.

1.  **Correct Boss Name Localization Key**:
    -   [x] In `Act2Listener.java`, modify the `getMessage` call to use the correct localization key `npc.entities.skeleton_lord` for the boss's name.
    -   [x] This change is expected to also fix the loot drop issue.

2.  **Prevent Skull Block Damage**:
    -   [x] Modify the `WitherSkullProjectile` class to accept the boss entity as a shooter.
    -   [x] Update the instantiation of `WitherSkullProjectile` in `Act2Listener.java` to pass the boss entity.
    -   [x] Ensure the `onWitherSkullExplode` event handler correctly identifies these skulls and cancels block damage.

3.  **Adjust Attack Timing**:
    -   [x] In `Act2Listener.java`, reduce the delay between the appearance of safe zones and the skull attack from 5 seconds to 1 second.

4.  **Validation**:
    -   [x] Start the server and summon Boss #1.
    -   [x] Verify that the boss's name is correctly displayed as "Повелитель скелетов".
    -   [x] Trigger the special attack and confirm that the skulls are launched approximately 1 second after the boss reaches its peak height.
    -   [x] Confirm that the wither skulls do not break any blocks upon impact.
    -   [x] Defeat the boss and verify that the correct loot is dropped.
