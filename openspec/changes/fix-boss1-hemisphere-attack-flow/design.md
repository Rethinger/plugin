## Context
Boss#1 hemisphere special attack intermittently fails: invisible/too-quick rise, inconsistent hemisphere shape, unreliable safe zones, premature global timeouts. Logs show phase timeouts and zero-player zone generation.

## Goals / Non-Goals
- Goals: visible rise/hover, stable hemisphere geometry, resilient phase timeouts, per-player safe zones on shared anchors, structured logs.
- Non-Goals: adding new projectile types, changing boss damage values.

## Decisions
- Timings (defaults; configurable):
  - rise_duration_ms = 1200
  - hover_min_ms = 800
  - formation_window_ms = 2500
  - zones_window_ms = 1500
  - final_prep_ms = 800
- Timeouts per phase = 1.5x the window; recovery prefers advancing if partial state is valid.
- Geometry: hemisphere centered at boss at hover height; radius = 6.0 ± 1.0 (min=5.0, max=7.0); polar angle θ in [20°, 90°]; azimuth φ uniform; count = 14 (configurable 10–18).
- Minimal viable formation threshold: ≥ 8 skulls positioned to proceed; otherwise cancel and resume AI.
- Safe zones: at least 1 if players present; count = min(players + 1, 4) with anchors in world; client visibility per-player.
- Logging: structured key=value lines at INFO with phase_from, phase_to, elapsed_ms, skull_count, zone_count, timeout_hit.

## Risks / Trade-offs
- Longer timings may slightly slow the fight; mitigated by config toggles.
- Per-player visuals require careful sync; anchor approach avoids desync.

## Migration Plan
- Gate behind config flags with defaults applied if missing.
- Implement per-phase timers and recovery paths.
- Update formation math and safe zone generation.
- Ship with verbose logs for one release; can reduce later.

## Open Questions
- Final skull count and radius tuning based on art direction.
- Whether hover should persist through safe zone appearance.

## Configuration Reference

All configuration values are defined in `SpecialAttackConfiguration.java` with the following defaults:

### Phase Timings
- `hemisphereFormationDurationTicks` = 50 (2500ms) - Time for skulls to form hemisphere
- `safeZonesWindowTicks` = 30 (1500ms) - Time window for safe zones to appear
- `finalPrepDurationTicks` = 16 (800ms) - Final pause before attack launch
- `risingDuration` = 60 (1200ms) - Boss rising animation duration
- `hoverDurationTicks` = 16 (800ms) - Hover at peak before formation
- `phaseTimeoutMultiplier` = 1.5 - Timeout multiplier (base_duration × 1.5)

### Hemisphere Geometry
- `hemisphereRadius` = 6.0 blocks - Base radius of hemisphere
- `hemisphereRadiusVariance` = 1.0 blocks - Randomization range (min=5.0, max=7.0)
- `hemisphereMinPolarAngleDegrees` = 20.0° - Minimum polar angle (prevents straight up)
- `hemisphereMaxPolarAngleDegrees` = 90.0° - Maximum polar angle (horizontal)
- `hemisphereSkullCount` = 14 - Number of skulls in formation (configurable 10-18)
- `minViableSkullThreshold` = 8 - Minimum skulls needed to proceed with attack

### Safe Zone Settings
- `minSafeZonesWhenPlayersPresent` = 1 - Guaranteed minimum if players exist
- `maxSafeZones` = 4 - Maximum safe zones: min(players + 1, 4)

### Implementation Notes
- All timing values are in ticks (20 ticks = 1 second)
- Geometry uses spherical coordinates: radius, polar angle θ, azimuth φ
- Safe zones use world-anchored locations with per-player visibility
- Phase recovery attempts to salvage partial state before aborting
- Structured logging uses key=value format for easy parsing
