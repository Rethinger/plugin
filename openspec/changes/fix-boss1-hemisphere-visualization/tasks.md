# Tasks: Fix Boss 1 Hemisphere Attack Visualization and Phase Recovery

## Planning
- [ ] Review server logs (lines 262-374) to understand exact failure sequence
- [ ] Review existing hemisphere attack implementation code
- [ ] Identify all phase transition points and timing dependencies
- [ ] Document current vs expected behavior for each bug

## Implementation

### Phase Timing Fix
- [ ] **BossSpecialAttackManager.java**: Update phase timeout calculation
  - Change `HEMISPHERE_FORMATION` timeout from 3000ms to 3750ms (75 ticks * 50ms)
  - OR: Use tick-based calculation instead of milliseconds to prevent future mismatches
  - Add debug logging for phase timing validation

### Recovery Logic Fix
- [ ] **BossSpecialAttackManager.java**: Fix recovery handler for formation phase
  - When `skull_count >= MIN_SKULL_THRESHOLD` (8), call `advanceToNextPhase()` instead of `forceStop()`
  - Only call `forceStop()` when `skull_count < MIN_SKULL_THRESHOLD`
  - Add logging to track recovery decisions and their outcomes
  - Ensure recovery preserves existing display skulls and safe zones

### Rising Animation Visual Effects
- [ ] **BossRisingAnimation.java**: Add particle effects during boss rise
  - Spawn SOUL particles around boss hitbox (rising effect)
  - Spawn PORTAL particles at boss feet (ground emergence effect)
  - Add particle intensity progression (low → high as boss rises)
  - Add sound effects: ENTITY_WITHER_SPAWN or ENTITY_ENDER_DRAGON_GROWL at start
  - Add sound effects: BLOCK_PORTAL_AMBIENT (looping) during rise
  - Ensure particles are visible to all nearby players

### Safe Zone Cleanup Coordination
- [ ] **HemisphereFormation.java**: Prevent premature cleanup
  - Remove or delay automatic cleanup of display skulls during recovery
  - Only cleanup display skulls after SKULLS_ATTACKING phase completes or attack truly fails
  - Coordinate with SafeZoneManager to maintain safe zones through attack phase

- [ ] **SafeZoneManager.java**: Extend safe zone lifetime
  - Ensure safe zones persist through SKULLS_ATTACKING phase
  - Only cleanup safe zones when attack completes (success or failure)
  - Add logging for safe zone lifecycle events

### Phase Flow Validation
- [ ] Verify phase transitions follow intended sequence:
  1. RISING_ANIMATION (2s with visual effects)
  2. HEMISPHERE_FORMATION (3.75s for 14 skulls)
  3. SAFE_ZONES_APPEARING (brief display)
  4. SKULLS_ATTACKING (actual damage phase)
  5. COMPLETE (cleanup)

## Testing
- [ ] Test normal execution: Boss rises → skulls form → safe zones appear → skulls attack
- [ ] Test recovery scenario: If formation is slow, recovery should allow continuation when viable
- [ ] Test failure scenario: If too few skulls (< 8), attack should properly fail and cleanup
- [ ] Verify visual effects: Particles and sounds play during rising animation
- [ ] Verify safe zones: Players inside safe zones take no damage
- [ ] Verify attack completion: Attack ends with `completed=true final_phase=COMPLETE`
- [ ] Check server logs: No more premature timeout warnings or forced stops

## Documentation
- [ ] Update CHANGELOG.md with bug fixes
- [ ] Document phase timing expectations in code comments
- [ ] Document recovery logic decision tree in code comments

## Validation
- [ ] Run `openspec validate fix-boss1-hemisphere-visualization --strict`
- [ ] Verify all spec deltas are properly formatted
- [ ] Code review for phase transition logic
- [ ] Test on live server with multiple players
