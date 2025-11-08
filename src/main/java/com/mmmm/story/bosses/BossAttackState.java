package com.mmmm.story.bosses;

import java.util.UUID;
import org.bukkit.Location;

/**
 * Tracks the attack state of a boss, including timing and cooldowns.
 * Used to manage boss attack patterns and prevent rapid-fire attacks.
 */
public class BossAttackState {
    private UUID bossId;
    private final int phase;
    private long lastAttackTime;
    private long attackStartTime;
    private boolean isAttacking;
    private int attackCount;
    
    // NEW: Special attack state tracking
    private SpecialAttackPhase specialAttackPhase;
    
    // NEW: Boundary and timeout tracking for loop prevention
    private Location originalPosition;
    private long specialAttackStartTime;
    private long maxSpecialAttackDuration;
    private double yAxisBoundaryOffset;
    
    // NEW: Cadence tracking for special attack timing
    private long lastSpecialAttackTime;
    private boolean lastSummonWasSkippedBySpecial;
    private boolean hasHadWarriorSummonSinceLastSpecial;

    // NEW: Warrior summon wave management for hemisphere attack
    private int warriorSummonWaveCount;
    private long lastWarriorSummonWaveTime;
    private long secondWaveTime; // Time when the second wave occurred (for hemisphere timing)
    private boolean warriorSummonTimerPaused;
    
    /**
     * Enum for special attack phases
     */
    public enum SpecialAttackPhase {
        NONE,                           // Not in special attack
        RISING_ANIMATION,              // Boss rising with visual effects
        GROUND_SKULL_GATHER,           // Skulls gathering on the ground (legacy)
        CASTING_SKULLS,                // Casting skull projectiles upward (legacy)
        SPHERE_ATTACK,                 // Skull projectiles in sphere pattern (legacy)
        HEMISPHERE_FORMATION,          // Hemisphere formation (legacy - replaced)
        SAFE_ZONES_APPEARING,          // Safe zones appearing sequentially (legacy)
        FINAL_PREPARATION,             // Final preparation before attack (legacy)
        HEMISPHERE_ATTACK,             // Hemisphere attack launching (legacy - replaced)
        POST_ATTACK_WAIT,              // Post-attack hover (legacy - replaced)
        GROUND_TOUCHDOWN,              // Boss descending to ground (legacy - replaced)

        // NEW: Stationary casting attack phases
        STATIONARY_CASTING_PREPARATION,  // Boss starts casting animation (3 seconds)
        STATIONARY_SAFE_ZONES_APPEARING, // Safe zones appear during casting
        STATIONARY_FANGS_ATTACK,         // Evoker fangs attack execution (3 seconds)
        STATIONARY_COOLDOWN,             // Attack cooldown phase
        COOLDOWN                        // Legacy cooldown (for compatibility)
    }
    
    /**
     * Create a new boss attack state.
     * @param bossId Unique identifier for the boss
     * @param phase The phase of the boss (1, 2, etc.)
     */
    public BossAttackState(UUID bossId, int phase) {
        this.bossId = bossId;
        this.phase = phase;
        this.lastAttackTime = 0;
        this.attackStartTime = 0;
        this.isAttacking = false;
        this.attackCount = 0;
        this.specialAttackPhase = SpecialAttackPhase.NONE;
        
        // Initialize boundary and timeout tracking
        this.originalPosition = null;
        this.specialAttackStartTime = 0;
        this.maxSpecialAttackDuration = 15000; // 15 seconds max
        this.yAxisBoundaryOffset = 15.0; // 15 blocks max
        
        // Initialize cadence tracking
        this.lastSpecialAttackTime = 0;
        this.lastSummonWasSkippedBySpecial = false;
        this.hasHadWarriorSummonSinceLastSpecial = false;

        // Initialize warrior summon wave management
        this.warriorSummonWaveCount = 0;
        this.lastWarriorSummonWaveTime = 0;
        this.secondWaveTime = 0;
        this.warriorSummonTimerPaused = false;
    }
    
    /**
     * Check if the boss can attack based on cooldown period.
     * @param intervalSeconds Minimum seconds between attacks
     * @return True if boss can attack
     */
    public boolean canAttack(int intervalSeconds) {
        long currentTime = System.currentTimeMillis();
        long intervalMillis = intervalSeconds * 1000L;
        
        // Check if enough time has passed since last attack
        return (currentTime - lastAttackTime) >= intervalMillis;
    }
    
    /**
     * Start an attack sequence.
     * Marks the boss as attacking and records start time.
     */
    public void startAttack() {
        this.attackStartTime = System.currentTimeMillis();
        this.isAttacking = true;
    }
    
    /**
     * Start a special attack sequence.
     * @param phase The special attack phase to start
     */
    public void startSpecialAttack(SpecialAttackPhase phase) {
        this.specialAttackPhase = phase;
        this.attackStartTime = System.currentTimeMillis();
        this.specialAttackStartTime = System.currentTimeMillis();
        this.isAttacking = true;
    }
    
    /**
     * Start a special attack sequence with position tracking.
     * @param phase The special attack phase to start
     * @param originalPos Original position before special attack
     */
    public void startSpecialAttack(SpecialAttackPhase phase, Location originalPos) {
        this.specialAttackPhase = phase;
        this.attackStartTime = System.currentTimeMillis();
        this.specialAttackStartTime = System.currentTimeMillis();
        this.originalPosition = originalPos != null ? originalPos.clone() : null;
        this.isAttacking = true;
    }
    
    /**
     * Set the current special attack phase.
     * @param phase The special attack phase
     */
    public void setSpecialAttackPhase(SpecialAttackPhase phase) {
        this.specialAttackPhase = phase;
    }
    
    /**
     * Get the current special attack phase.
     * @return Current special attack phase
     */
    public SpecialAttackPhase getSpecialAttackPhase() {
        return specialAttackPhase;
    }
    
    /**
     * Check if boss is in special attack.
     * @return True if in any special attack phase
     */
    public boolean isInSpecialAttack() {
        return specialAttackPhase != SpecialAttackPhase.NONE && specialAttackPhase != SpecialAttackPhase.COOLDOWN;
    }
    
    /**
     * Check if boss is in rising animation phase.
     * @return True if in rising animation
     */
    public boolean isRisingAnimation() {
        return specialAttackPhase == SpecialAttackPhase.RISING_ANIMATION;
    }
    
    /**
     * Check if boss is casting skulls.
     * @return True if casting skull projectiles
     */
    public boolean isCastingSkulls() {
        return specialAttackPhase == SpecialAttackPhase.CASTING_SKULLS;
    }

    /**
     * Check if boss is gathering skulls on the ground.
     * @return True if gathering skulls on the ground
     */
    public boolean isGatheringSkullsOnGround() {
        return specialAttackPhase == SpecialAttackPhase.GROUND_SKULL_GATHER;
    }
    
    /**
     * Check if boss is in sphere attack phase.
     * @return True if in sphere attack
     */
    public boolean isInSphereAttack() {
        return specialAttackPhase == SpecialAttackPhase.SPHERE_ATTACK;
    }

    /**
     * Check if boss is in hemisphere formation phase.
     * @return True if in hemisphere formation
     */
    public boolean isInHemisphereFormation() {
        return specialAttackPhase == SpecialAttackPhase.HEMISPHERE_FORMATION;
    }

    /**
     * Check if boss is in safe zones appearing phase.
     * @return True if safe zones are appearing
     */
    public boolean isSafeZonesAppearing() {
        return specialAttackPhase == SpecialAttackPhase.SAFE_ZONES_APPEARING;
    }

    /**
     * Check if boss is in final preparation phase.
     * @return True if in final preparation
     */
    public boolean isInFinalPreparation() {
        return specialAttackPhase == SpecialAttackPhase.FINAL_PREPARATION;
    }

    /**
     * Check if boss is in hemisphere attack phase.
     * @return True if in hemisphere attack
     */
    public boolean isInHemisphereAttack() {
        return specialAttackPhase == SpecialAttackPhase.HEMISPHERE_ATTACK;
    }

    /**
     * Check if boss is in post-attack wait phase.
     * @return True if in post-attack wait
     */
    public boolean isInPostAttackWait() {
        return specialAttackPhase == SpecialAttackPhase.POST_ATTACK_WAIT;
    }

    /**
     * Check if boss is in ground touchdown phase.
     * @return True if in ground touchdown
     */
    public boolean isInGroundTouchdown() {
        return specialAttackPhase == SpecialAttackPhase.GROUND_TOUCHDOWN;
    }

    /**
     * Check if boss should remain airborne (in any flight phase)
     * @return True if boss should stay airborne
     */
    public boolean shouldBeAirborne() {
        return specialAttackPhase == SpecialAttackPhase.RISING_ANIMATION ||
               specialAttackPhase == SpecialAttackPhase.HEMISPHERE_FORMATION ||
               specialAttackPhase == SpecialAttackPhase.SAFE_ZONES_APPEARING ||
               specialAttackPhase == SpecialAttackPhase.FINAL_PREPARATION ||
               specialAttackPhase == SpecialAttackPhase.HEMISPHERE_ATTACK ||
               specialAttackPhase == SpecialAttackPhase.POST_ATTACK_WAIT;
    }

    /**
     * Check if boss is in stationary casting preparation phase.
     * @return True if in stationary casting preparation
     */
    public boolean isInStationaryCastingPreparation() {
        return specialAttackPhase == SpecialAttackPhase.STATIONARY_CASTING_PREPARATION;
    }

    /**
     * Check if boss is in stationary safe zones appearing phase.
     * @return True if stationary safe zones are appearing
     */
    public boolean isInStationarySafeZonesAppearing() {
        return specialAttackPhase == SpecialAttackPhase.STATIONARY_SAFE_ZONES_APPEARING;
    }

    /**
     * Check if boss is in stationary fangs attack phase.
     * @return True if in stationary fangs attack
     */
    public boolean isInStationaryFangsAttack() {
        return specialAttackPhase == SpecialAttackPhase.STATIONARY_FANGS_ATTACK;
    }

    /**
     * Check if boss is in stationary cooldown phase.
     * @return True if in stationary cooldown
     */
    public boolean isInStationaryCooldown() {
        return specialAttackPhase == SpecialAttackPhase.STATIONARY_COOLDOWN;
    }

    /**
     * Check if boss should be stationary (grounded) during stationary casting phases
     * @return True if boss should stay on ground
     */
    public boolean shouldBeStationary() {
        return specialAttackPhase == SpecialAttackPhase.STATIONARY_CASTING_PREPARATION ||
               specialAttackPhase == SpecialAttackPhase.STATIONARY_SAFE_ZONES_APPEARING ||
               specialAttackPhase == SpecialAttackPhase.STATIONARY_FANGS_ATTACK ||
               specialAttackPhase == SpecialAttackPhase.STATIONARY_COOLDOWN;
    }
    
    /**
     * End an attack sequence.
     * Updates last attack time and attack count.
     */
    public void endAttack() {
        this.lastAttackTime = System.currentTimeMillis();
        this.isAttacking = false;
        this.attackCount++;
        this.specialAttackPhase = SpecialAttackPhase.NONE;
    }
    
    /**
     * End a special attack sequence.
     * @param transitionToCooldown Whether to transition to cooldown phase
     */
    public void endSpecialAttack(boolean transitionToCooldown) {
        this.lastAttackTime = System.currentTimeMillis();
        this.attackCount++;
        
        if (transitionToCooldown) {
            this.specialAttackPhase = SpecialAttackPhase.COOLDOWN;
        } else {
            this.specialAttackPhase = SpecialAttackPhase.NONE;
        }
        
        // Check if this was the last attack sequence
        if (specialAttackPhase == SpecialAttackPhase.NONE) {
            this.isAttacking = false;
        }
    }
    
    /**
     * Get the boss's unique identifier.
     * @return Boss UUID
     */
    public UUID getBossId() {
        return bossId;
    }
    
    /**
     * Get the current phase of the boss.
     * @return Boss phase number
     */
    public int getPhase() {
        return phase;
    }
    
    /**
     * Check if the boss is currently attacking.
     * @return True if attack is in progress
     */
    public boolean isAttacking() {
        return isAttacking;
    }
    
    /**
     * Get the timestamp when the current attack started.
     * @return Attack start time in milliseconds
     */
    public long getAttackStartTime() {
        return attackStartTime;
    }
    
    /**
     * Get the timestamp of the last completed attack.
     * @return Last attack time in milliseconds
     */
    public long getLastAttackTime() {
        return lastAttackTime;
    }
    
    /**
     * Get the total number of attacks performed.
     * @return Attack count
     */
    public int getAttackCount() {
        return attackCount;
    }
    
    /**
     * Get the duration of the current attack in milliseconds.
     * @return Attack duration, or 0 if not attacking
     */
    public long getCurrentAttackDuration() {
        if (!isAttacking) {
            return 0;
        }
        return System.currentTimeMillis() - attackStartTime;
    }
    
    /**
     * Reset the attack state.
     * Clears all timing and count information.
     */
    public void reset() {
        this.lastAttackTime = 0;
        this.attackStartTime = 0;
        this.isAttacking = false;
        this.attackCount = 0;
        this.specialAttackPhase = SpecialAttackPhase.NONE;
    }
    
    /**
     * Update the boss ID for this attack state.
     * Useful when the initial UUID was placeholder.
     * @param bossId The new boss UUID
     */
    public void setBossId(UUID bossId) {
        this.bossId = bossId;
    }
    
    /**
     * Check if special attack has exceeded maximum duration.
     * @return True if special attack should be terminated
     */
    public boolean hasSpecialAttackTimedOut() {
        if (specialAttackStartTime <= 0) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        return (currentTime - specialAttackStartTime) > maxSpecialAttackDuration;
    }
    
    /**
     * Check if boss position exceeds Y-axis boundary.
     * @param currentPos Current boss position
     * @return True if position exceeds boundary
     */
    public boolean exceedsYAxisBoundary(Location currentPos) {
        if (originalPosition == null || currentPos == null) {
            return false;
        }
        double currentY = currentPos.getY();
        double originalY = originalPosition.getY();
        double displacement = Math.abs(currentY - originalY);
        return displacement > yAxisBoundaryOffset;
    }
    
    /**
     * Get original position before special attack started.
     * @return Original position or null if not set
     */
    public Location getOriginalPosition() {
        return originalPosition != null ? originalPosition.clone() : null;
    }
    
    /**
     * Set original position for boundary tracking.
     * @param position Original position before special attack
     */
    public void setOriginalPosition(Location position) {
        this.originalPosition = position != null ? position.clone() : null;
    }
    
    /**
     * Get maximum Y-axis boundary offset.
     * @return Maximum Y displacement in blocks
     */
    public double getYAxisBoundaryOffset() {
        return yAxisBoundaryOffset;
    }
    
    /**
     * Set maximum Y-axis boundary offset.
     * @param offset Maximum Y displacement in blocks
     */
    public void setYAxisBoundaryOffset(double offset) {
        this.yAxisBoundaryOffset = Math.max(5.0, Math.min(20.0, offset)); // Clamp between 5-20
    }
    
    /**
     * Get maximum special attack duration in milliseconds.
     * @return Maximum duration in ms
     */
    public long getMaxSpecialAttackDuration() {
        return maxSpecialAttackDuration;
    }
    
    /**
     * Set maximum special attack duration.
     * @param duration Maximum duration in milliseconds
     */
    public void setMaxSpecialAttackDuration(long duration) {
        this.maxSpecialAttackDuration = Math.max(5000, Math.min(30000, duration)); // Clamp between 5-30 seconds
    }
    
    /**
     * Get special attack duration in milliseconds.
     * @return Duration since special attack started, or 0 if not in special attack
     */
    public long getSpecialAttackDuration() {
        if (specialAttackStartTime <= 0 || !isInSpecialAttack()) {
            return 0;
        }
        return System.currentTimeMillis() - specialAttackStartTime;
    }
    
    /**
     * Check if enough time has passed since last special attack
     * @param minSpacingSeconds Minimum required spacing in seconds
     * @return True if spacing requirement is met
     */
    public boolean hasMetSpecialAttackSpacing(int minSpacingSeconds) {
        if (lastSpecialAttackTime <= 0) {
            return true; // First special attack
        }
        long currentTime = System.currentTimeMillis();
        long requiredSpacingMillis = minSpacingSeconds * 1000L;
        return (currentTime - lastSpecialAttackTime) >= requiredSpacingMillis;
    }
    
    /**
     * Check if warrior summon requirement is met for next special attack
     * @param requireWarriorSummon Whether warrior summon is required
     * @return True if requirement is met
     */
    public boolean hasMetWarriorSummonRequirement(boolean requireWarriorSummon) {
        if (!requireWarriorSummon) {
            return true; // Requirement disabled
        }
        return hasHadWarriorSummonSinceLastSpecial;
    }
    
    /**
     * Check if special attack can be started based on cadence rules
     * @param minSpacingSeconds Minimum spacing between specials
     * @param requireWarriorSummon Whether warrior summon is required
     * @return True if special attack can start
     */
    public boolean canStartSpecialAttack(int minSpacingSeconds, boolean requireWarriorSummon) {
        return hasMetSpecialAttackSpacing(minSpacingSeconds) && 
               hasMetWarriorSummonRequirement(requireWarriorSummon);
    }
    
    /**
     * Record that a summon was skipped due to special attack
     */
    public void recordSummonSkippedBySpecial() {
        this.lastSummonWasSkippedBySpecial = true;
    }
    
    /**
     * Record that a successful warrior summon occurred
     */
    public void recordSuccessfulWarriorSummon() {
        this.lastSummonWasSkippedBySpecial = false;
        this.hasHadWarriorSummonSinceLastSpecial = true;
    }
    
    /**
     * Reset warrior summon tracking (called when special attack starts)
     */
    public void resetWarriorSummonTracking() {
        this.hasHadWarriorSummonSinceLastSpecial = false;
    }
    
    /**
     * Update last special attack time (called when special attack completes)
     */
    public void updateLastSpecialAttackTime() {
        this.lastSpecialAttackTime = System.currentTimeMillis();
    }
    
    /**
     * Check if the last summon was skipped by special attack
     * @return True if last summon was skipped
     */
    public boolean wasLastSummonSkippedBySpecial() {
        return lastSummonWasSkippedBySpecial;
    }

    /**
     * Record that a complete warrior summon wave occurred
     * A wave consists of 2-3 warriors summoned together
     */
    public void recordWarriorSummonWave() {
        this.warriorSummonWaveCount++;
        this.lastWarriorSummonWaveTime = System.currentTimeMillis();
        this.lastSummonWasSkippedBySpecial = false;
        this.hasHadWarriorSummonSinceLastSpecial = true;

        // Record the time of the second wave for hemisphere attack timing
        if (this.warriorSummonWaveCount == 2) {
            this.secondWaveTime = this.lastWarriorSummonWaveTime;
        }
    }

    /**
     * Get the current warrior summon wave count
     * @return Number of complete warrior summon waves
     */
    public int getWarriorSummonWaveCount() {
        return warriorSummonWaveCount;
    }

    /**
     * Check if minimum warrior summon wave requirement is met
     * @param minimumWaves Minimum number of waves required
     * @return True if requirement is met
     */
    public boolean hasMinimumWarriorSummonWaves(int minimumWaves) {
        return warriorSummonWaveCount >= minimumWaves;
    }

    /**
     * Check if enough time has passed since the last warrior summon wave
     * @param waitTimeSeconds Required wait time in seconds
     * @return True if wait time requirement is met
     */
    public boolean hasWaitedSinceLastWarriorWave(int waitTimeSeconds) {
        if (secondWaveTime <= 0) {
            return false; // Second wave hasn't occurred yet
        }
        long currentTime = System.currentTimeMillis();
        long requiredWaitMillis = waitTimeSeconds * 1000L;
        long elapsedTime = currentTime - secondWaveTime;
        return elapsedTime >= requiredWaitMillis;
    }

    /**
     * Check if warrior summon timer is currently paused
     * @return True if timer is paused
     */
    public boolean isWarriorSummonTimerPaused() {
        return warriorSummonTimerPaused;
    }

    /**
     * Pause the warrior summon timer (used during special attacks)
     */
    public void pauseWarriorSummonTimer() {
        this.warriorSummonTimerPaused = true;
    }

    /**
     * Resume the warrior summon timer (used after special attacks)
     */
    public void resumeWarriorSummonTimer() {
        this.warriorSummonTimerPaused = false;
    }

    /**
     * Reset warrior summon wave counting
     * Used when starting fresh or for testing
     */
    public void resetWarriorSummonWaves() {
        this.warriorSummonWaveCount = 0;
        this.lastWarriorSummonWaveTime = 0;
        this.secondWaveTime = 0;
    }

    /**
     * Get time since last warrior summon wave in milliseconds
     * @return Time since last wave, or 0 if no waves yet
     */
    public long getTimeSinceLastWarriorWave() {
        if (lastWarriorSummonWaveTime <= 0) {
            return 0;
        }
        return System.currentTimeMillis() - lastWarriorSummonWaveTime;
    }

    /**
     * Check if hemisphere special attack requirements are met
     * @param minimumWaves Minimum warrior waves required (usually 2)
     * @param waitTimeSeconds Wait time after second wave (usually 15)
     * @return True if all requirements are met
     */
    public boolean canStartHemisphereSpecialAttack(int minimumWaves, int waitTimeSeconds) {
        // Must have minimum number of warrior summon waves
        if (!hasMinimumWarriorSummonWaves(minimumWaves)) {
            return false;
        }

        // Must have waited required time after the second wave
        if (warriorSummonWaveCount >= 2 && !hasWaitedSinceLastWarriorWave(waitTimeSeconds)) {
            return false;
        }

        // Must meet special attack spacing requirements
        return hasMetSpecialAttackSpacing(waitTimeSeconds);
    }

    /**
     * Enhanced warrior summon tracking that accounts for waves
     * Replaces the simple recordSuccessfulWarriorSummon for hemisphere compatibility
     */
    public void recordSuccessfulWarriorSummonWave() {
        // This should be called once per complete wave (2-3 warriors)
        recordWarriorSummonWave();
    }
}