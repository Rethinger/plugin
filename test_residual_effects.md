# Test Plan: NPC Residual Effects Fix

## Overview
This test plan validates the implementation of the fix-npc-residual-effects change proposal, ensuring complete particle cleanup and proper effect isolation.

## Test Environment
- Server in creative mode for easy access
- Test area with clear space (at least 20x20 blocks)
- No other plugins running that might interfere with particles

## Test Cases

### Test Case 1: Particle Cleanup Effectiveness
**Objective**: Verify complete particle removal after NPC despawn

**Steps**:
1. Spawn a messenger NPC using `/story messenger`
2. Wait for NPC to appear with full particle effects
3. Trigger NPC despawn (wait for natural despawn or use removal command)
4. Observe particle cleanup during despawn animation
5. After despawn completes, check 10-block radius around NPC location
6. Verify no residual particles remain (END_ROD, ENCHANT, REVERSE_PORTAL, etc.)

**Expected Results**:
- Immediate particle cleanup at start of despawn animation
- Final particle cleanup after despawn completion
- No particles visible within 10 blocks after despawn
- Clean log messages confirming particle cleanup

### Test Case 2: Player Location Isolation
**Objective**: Ensure effects only appear at NPC location, not player location

**Steps**:
1. Spawn messenger NPC
2. Position player within 5 blocks of NPC
3. Trigger dialog with NPC
4. Observe where visual effects appear during dialog
5. Trigger NPC despawn while player is nearby
6. Monitor player location for any unintended effects

**Expected Results**:
- All NPC-related effects (particles, sounds) appear at NPC location
- Player-centered sounds only (dialog feedback) appear at player location
- No cross-contamination of effects between NPC and player positions
- Despawn effects remain centered on NPC, not player

### Test Case 3: Task Cancellation Validation
**Objective**: Verify complete cancellation of all NPC-related tasks

**Steps**:
1. Spawn messenger NPC with active aura and idle animations
2. Start multiple dialog sequences to create scheduled tasks
3. Trigger immediate NPC despawn
4. Monitor server logs for task cancellation messages
5. Verify no new tasks are created after despawn begins

**Expected Results**:
- All aura tasks cancelled immediately
- All scheduled tasks cancelled with proper logging
- No new tasks created after despawn initiation
- Clean task cleanup with count of cancelled tasks

### Test Case 4: Multiple Players Scenario
**Objective**: Test effect isolation with multiple players

**Steps**:
1. Have 2-3 players positioned around NPC
2. Trigger NPC actions and despawn
3. Each player reports what they see and where
4. Verify all players see effects at same location (NPC position)

**Expected Results**:
- All players see identical effects at NPC location
- No player-specific effect variations
- Consistent experience across all players

### Test Case 5: Performance Validation
**Objective**: Ensure particle cleanup doesn't impact performance

**Steps**:
1. Monitor server TPS before and during tests
2. Perform multiple rapid spawn/despawn cycles
3. Check for memory leaks or performance degradation
4. Verify cleanup operations are efficient

**Expected Results**:
- No significant TPS drops during particle cleanup
- Memory usage remains stable
- Cleanup operations complete quickly

## Validation Commands

### Debug Commands (if available)
```
/story messenger - Spawn test NPC
/npc list - Check active NPCs
/npc cleanup - Force cleanup (for testing)
```

### Log Monitoring
Watch for these log messages:
- `[NPC] Cleared particles in radius 10.0 around Location`
- `[NPC] Cancelled aura task for messenger`
- `[NPC] Cancelled idle task for messenger`
- `[NPC] Successfully cancelled X tasks for NPC messenger`

## Success Criteria
- All test cases pass without exceptions
- No residual particles detected after despawn
- Proper effect isolation maintained
- Clean task cancellation with logging
- No performance impact

## Regression Testing
After implementing fixes, verify existing functionality still works:
- Normal NPC spawning works correctly
- Dialog systems function properly
- Other visual effects remain unaffected
- No breaking changes to existing commands