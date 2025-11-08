# Change: Fix Boss 1 Hemisphere Attack Visualization and Phase Recovery

## Why
The Boss 1 hemisphere special attack is not executing properly due to several critical bugs identified in server logs:

1. **Rising Animation has no visual effects** - Boss rises without particle effects or visual feedback
2. **Phase timeout miscalculation** - Formation phase expects 75 ticks (3.75s) but timeout is set to 3s, causing premature timeout warnings
3. **Premature recovery failure** - Recovery logic immediately forces stop instead of allowing the attack to continue when a viable formation exists (14 skulls > 8 threshold)
4. **Missing attack execution** - Safe zones appear briefly but attack never progresses to skull attack phase

Server log evidence (lines 350-374):
- Formation completes with 14 display skulls successfully
- Phase timeout hits at 3749ms (3.75s actual vs 3s expected)
- Recovery confirms viable formation (14 > 8 threshold)
- Instead of advancing to next phase, recovery immediately forces stop
- All display skulls and safe zones are cleaned up prematurely
- Attack ends with `completed=false final_phase=NONE`

This is a bug fix to restore the intended behavior from the existing `enhance-boss1-special-attack-hemisphere` proposal, not a new feature.

## What Changes
- Fix phase timing calculation to match actual tick duration (75 ticks = 3.75 seconds, not 3 seconds)
- Add visual effects (particles, sounds) to boss rising animation phase
- Fix recovery logic to allow attack continuation when viable formation exists
- Prevent premature cleanup of visual elements when recovery succeeds
- Ensure proper phase transitions: RISING_ANIMATION → HEMISPHERE_FORMATION → SAFE_ZONES_APPEARING → SKULLS_ATTACKING → COMPLETE

## Impact
- Affected specs: boss-mechanics
- Affected code:
  - `src/main/java/com/mmmm/story/bosses/BossSpecialAttackManager.java` - Phase timing and recovery logic
  - `src/main/java/com/mmmm/story/bosses/BossRisingAnimation.java` - Add visual effects
  - `src/main/java/com/mmmm/story/bosses/HemisphereFormation.java` - Ensure proper cleanup coordination
  - `src/main/java/com/mmmm/story/managers/SafeZoneManager.java` - Prevent premature cleanup
