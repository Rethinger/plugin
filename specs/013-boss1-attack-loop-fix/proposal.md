# Proposal: Boss 1 Attack Loop and Loot Fixes

This document outlines the necessary fixes for the Boss 1 encounter to align its behavior with the intended design.

## Issues

Based on player feedback and server logs, the following issues have been identified:

1.  **Incorrect Wither Skull Attack:**
    - **Observed:** The boss does not consistently use the wither skull attack at the peak of its rising animation.
    - **Expected:** The boss should rise, pause at the apex, cast wither skulls, and then descend. The boss should be invulnerable during the descent.

2.  **Delayed Minion Summoning:**
    - **Observed:** There is a significant delay in the boss summoning its minions ('Воин Повелителя' and 'Лучник Повелителя').
    - **Expected:** Minions should be summoned at regular, predictable intervals during the fight.

3.  **Incorrect Teleportation Logic:**
    - **Observed:** The boss teleports in both phase one and phase two of the fight.
    - **Expected:** Teleportation should only occur during phase one.

4.  **Missing Loot Drops:**
    - **Observed:** The boss did not drop the expected loot upon defeat (diamonds, netherite scrap, and a story item).
    - **Expected:** The boss should drop a predefined set of items, including essential story progression items.

## Proposed Changes

To address these issues, the following changes will be implemented:

1.  **Refactor Boss Attack State Machine:**
    - Modify the boss's AI to ensure the `CASTING_SKULLS` state is triggered reliably at the peak of the `RISING` animation.
    - Add a new state or logic to make the boss invulnerable during its descent after the skull attack.

2.  **Adjust Minion Summoning Timer:**
    - Review and adjust the timer or conditions that trigger the summoning of minions to ensure they appear more frequently and consistently.

3.  **Phase-Specific Teleportation:**
    - Add a condition to the teleportation logic to check the boss's current phase. Teleportation should be disabled during phase two.

4.  **Fix Loot Table:**
    - Investigate the boss's death event handler and loot table configuration.
    - Ensure that the correct loot (diamonds, netherite scrap, and the specific story item) is always dropped upon the boss's defeat.

## Next Steps

1.  Create a detailed implementation plan.
2.  Switch to `code` mode to apply the fixes.
3.  Test the changes thoroughly on a development server.