# Change: Fix Boss#1 hemisphere special attack flow (rise → hemisphere → safe zones → launch)

## Why
Recent playtests show Boss#1's hemisphere special attack intermittently fails its intended choreography: the boss rise animation is imperceptible, skulls do not form a clear hemisphere arc (many travel straight up or toward the player prematurely), safe zones sometimes generate for zero players, and the sequence often times out mid‑flow. See serverlogs.txt for multiple timeouts and zero‑player zone generation.

## What Changes
- Clarify and tighten the state machine for the hemisphere special attack.
- Define deterministic timing for rise animation, formation, safe zone appearance, and launch.
- Specify hemisphere geometry constraints (radius, vertical arc, count) to prevent vertical/linear launches.
- Require safe zones to appear regardless of dynamic player counts, with per‑player visibility and shared world anchors.
- Make timeout semantics phase‑aware and pause‑resilient to avoid premature sequence aborts.
- Require structured debug logs for each phase and for recovery paths.

## Impact
- Affected specs: boss1-hemisphere-attack
- Affected code:
  - src/main/java/com/mmmm/story/bosses/BossSpecialAttackManager.java: rise/phase transitions, timeout handling, player targeting
  - src/main/java/com/mmmm/story/bosses/HemisphereFormation.java: position generation, projectile choreography
  - src/main/java/com/mmmm/story/managers/SafeZoneManager.java: safe zone generation, per‑player targeting and visibility
  - src/main/java/com/mmmm/story/bosses/DisplaySkullProjectile.java: formation vs attack transformation timing
  - Configuration entries for counts/timings if applicable
