# Change: Update Boss 1 Skull Attack Choreography

## Why
Players report the current 1s delay at peak feels sluggish, and the boss can trigger special attacks twice in a row without a warrior-skeleton summon window in between. Intended design: the boss rises, skulls gather from ground to the boss, then he releases them downward in a hemisphere, and the skulls form a sphere while flying.

## What Changes
- Adjust peak launch timing: immediate launch at peak (<=0.2s) or a configurable, explicit hover duration.
- Add skull choreography: ground-to-boss gather, then downward hemisphere release; maintain spherical formation during flight.
- Enforce cadence: require at least one warrior-summon window between specials and a minimal spacing since last special.
- Add configuration toggles and sensible defaults for timing and cadence.

## Impact
- Affected specs: attack-timing, skull-choreography, special-attack-cadence
- Affected code: src/main/java/com/mmmm/story/listeners/Act2Listener.java, src/main/java/com/mmmm/story/bosses/BossAttackState.java, src/main/java/com/mmmm/story/bosses/SpecialAttackConfiguration.java, src/main/java/com/mmmm/story/bosses/BossRisingAnimation.java, src/main/java/com/mmmm/story/bosses/WitherSkullProjectile.java, src/main/java/com/mmmm/story/managers/SafeZoneManager.java
