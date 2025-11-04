package com.mmmm.story.bosses;

import java.util.UUID;

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
     * End an attack sequence.
     * Updates last attack time and attack count.
     */
    public void endAttack() {
        this.lastAttackTime = System.currentTimeMillis();
        this.isAttacking = false;
        this.attackCount++;
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
    }
    
    /**
     * Update the boss ID for this attack state.
     * Useful when the initial UUID was placeholder.
     * @param bossId The new boss UUID
     */
    public void setBossId(UUID bossId) {
        this.bossId = bossId;
    }
}