# Change: Fix NPC despawn visual artifacts

## Why
After an NPC disappears, residual particles remain at its last position, and additional unintended visual effects appear around the player on NPC despawn. This is distracting and contradicts expected clean despawn behavior.

## What Changes
- Remove lingering particle emitters/effects at NPC location upon despawn.
- Prevent spawning of any player-centered visual effects on NPC despawn unless explicitly configured.
- Ensure effect cleanup runs once and is idempotent to avoid race conditions.

## Impact
- Affected specs: npc-visuals
- Affected code: NPC lifecycle/despawn handlers, effect/particle manager, any event listeners dispatching player FX on NPC despawn.
