# Change: Refine Boss 1 Skull Attack Animation

## Why
Players have reported that the current skull attack animation for Boss #1, added in the last patch, feels underwhelming and happens too quickly after the boss lands. The user wants a more dramatic and "epic" animation sequence to improve the player experience.

## What Changes
- The skull attack animation will be modified to be more visually impressive and well-paced.
- After landing, the boss will initiate a skull-gathering sequence.
- Skulls will rise from the ground and form a lower semi-circle around the boss.
- The boss will pause for a moment with the skulls gathered.
- After the pause, the boss will release the skulls towards the player.
- The new animation will be accompanied by "epic" visual effects.

## Impact
- Affected specs: `skull-animation` (new), potentially modifies `skull-choreography`.
- Affected code: The implementation will likely touch the same files as the previous skull choreography change, such as `Act2Listener.java`, `BossAttackState.java`, and related animation/projectile classes.
