# Change: Fix Messenger Despawn Synchronization and Particle Cleanup

## Why
The messenger NPC is not disappearing at the correct time during the "*Посланник исчезает в тумане*" dialog line, and residual particles remain after despawn, breaking immersion.

## What Changes
- Fix the timing synchronization between dialog trigger and messenger despawn
- Enhance particle cleanup to ensure all visual effects are cleared after messenger disappears
- Improve the despawn trigger mechanism to be more reliable

## Impact
- Affected specs: npc-system, dialog-system
- Affected code: DialogManager.java:258-268, NPCManager.java:847-871
- Player experience: More immersive and clean messenger disappearance