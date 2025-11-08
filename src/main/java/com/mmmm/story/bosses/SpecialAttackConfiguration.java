package com.mmmm.story.bosses;

import org.bukkit.Particle;

/**
 * Configuration class for Boss 1 special attack mechanics.
 * Controls rising animation, skull projectiles, and visual effects.
 */
public class SpecialAttackConfiguration {
    
    // Core attack settings
    private final boolean enabled;
    private final boolean risingAnimation;
    private final double risingHeight;
    private final int risingDuration;
    private final int projectileCount;
    private final boolean spherePattern;
    private final boolean preventSkeletonSpawn;
    private final boolean removeStunEffect;
    
    // NEW: Timing and cadence settings
    private final int hoverDurationTicks; // Hover at peak before launch (0 = immediate launch)
    private final int minSpecialAttackSpacingSeconds; // Minimum time between special attacks
    private final boolean requireWarriorSummonBetweenSpecials; // Enforce warrior summon window
    
    // NEW: Ground skull gather animation settings
    private final int groundGatherDurationTicks; // Duration of ground gather phase
    private final int groundGatherPauseTicks; // Duration of pause after gather
    
    // Phase-specific timing settings (from fix-boss1-hemisphere-attack-flow)
    private final int hemisphereFormationDurationTicks; // Duration for skulls to form hemisphere
    private final int safeZonesWindowTicks; // Duration for safe zones to appear
    private final int finalPrepDurationTicks; // Final preparation before launch
    private final double phaseTimeoutMultiplier; // Multiplier for phase timeout (default 1.5x)
    
    // Hemisphere geometry settings
    private final double hemisphereRadius; // Base radius for hemisphere (blocks)
    private final double hemisphereRadiusVariance; // Variance for radius randomization
    private final double hemisphereMinPolarAngleDegrees; // Minimum polar angle (θ) in degrees
    private final double hemisphereMaxPolarAngleDegrees; // Maximum polar angle (θ) in degrees
    private final int hemisphereSkullCount; // Number of skulls in hemisphere formation
    private final int minViableSkullThreshold; // Minimum skulls needed to proceed
    
    // Safe zone settings
    private final int minSafeZonesWhenPlayersPresent; // Minimum safe zones when players exist
    private final int maxSafeZones; // Maximum safe zones to create
    
    // Visual effects settings
    private final boolean soulFireParticles;
    private final boolean endRodParticles;
    private final boolean skullTrailParticles;
    
    /**
     * Create a new special attack configuration
     */
    public SpecialAttackConfiguration() {
        this.enabled = true;
        this.risingAnimation = true;
        this.risingHeight = 12.0;
        this.risingDuration = 60; // 1200ms = 60 ticks (design spec: rise_duration_ms = 1200)
        this.projectileCount = 36;
        this.spherePattern = true;
        this.preventSkeletonSpawn = true;
        this.removeStunEffect = true;
        this.hoverDurationTicks = 16; // 800ms = 16 ticks (design spec: hover_min_ms = 800)
        this.minSpecialAttackSpacingSeconds = 20; // 20 seconds minimum between specials
        this.requireWarriorSummonBetweenSpecials = true; // Require warrior summon window
        this.soulFireParticles = true;
        this.endRodParticles = true;
        this.skullTrailParticles = true;
        this.groundGatherDurationTicks = 30; // 1.5 seconds gather
        this.groundGatherPauseTicks = 20; // 1 second pause
        
        // Phase-specific timings (design spec: fix-boss1-hemisphere-attack-flow)
        this.hemisphereFormationDurationTicks = 50; // 2500ms = 50 ticks (formation_window_ms = 2500)
        this.safeZonesWindowTicks = 30; // 1500ms = 30 ticks (zones_window_ms = 1500)
        this.finalPrepDurationTicks = 16; // 800ms = 16 ticks (final_prep_ms = 800)
        this.phaseTimeoutMultiplier = 1.5; // 1.5x window for timeout
        
        // Hemisphere geometry (design spec)
        this.hemisphereRadius = 6.0; // Base radius in blocks
        this.hemisphereRadiusVariance = 1.0; // ± 1.0 blocks (min=5.0, max=7.0)
        this.hemisphereMinPolarAngleDegrees = 20.0; // θ min = 20°
        this.hemisphereMaxPolarAngleDegrees = 90.0; // θ max = 90°
        this.hemisphereSkullCount = 14; // 14 skulls (configurable 10-18)
        this.minViableSkullThreshold = 8; // Minimum 8 skulls to proceed
        
        // Safe zone settings
        this.minSafeZonesWhenPlayersPresent = 1; // At least 1 zone if players present
        this.maxSafeZones = 4; // min(players + 1, 4)
    }
    
    /**
     * Create a new special attack configuration with custom values
     */
    public SpecialAttackConfiguration(boolean enabled, boolean risingAnimation, double risingHeight, 
                                int risingDuration, int projectileCount, boolean spherePattern,
                                boolean preventSkeletonSpawn, boolean removeStunEffect,
                                int hoverDurationTicks, int minSpecialAttackSpacingSeconds, 
                                boolean requireWarriorSummonBetweenSpecials,
                                boolean soulFireParticles, boolean endRodParticles, 
                                boolean skullTrailParticles) {
        this(enabled, risingAnimation, risingHeight, risingDuration, projectileCount, spherePattern,
             preventSkeletonSpawn, removeStunEffect, hoverDurationTicks, minSpecialAttackSpacingSeconds,
             requireWarriorSummonBetweenSpecials, soulFireParticles, endRodParticles, 
             skullTrailParticles, 30, 20);
    }
    
    /**
     * Create a new special attack configuration with custom values including ground gather settings
     */
    public SpecialAttackConfiguration(boolean enabled, boolean risingAnimation, double risingHeight, 
                                int risingDuration, int projectileCount, boolean spherePattern,
                                boolean preventSkeletonSpawn, boolean removeStunEffect,
                                int hoverDurationTicks, int minSpecialAttackSpacingSeconds, 
                                boolean requireWarriorSummonBetweenSpecials,
                                boolean soulFireParticles, boolean endRodParticles, 
                                boolean skullTrailParticles, int groundGatherDurationTicks, 
                                int groundGatherPauseTicks) {
        this.enabled = enabled;
        this.risingAnimation = risingAnimation;
        this.risingHeight = risingHeight;
        this.risingDuration = risingDuration;
        this.projectileCount = projectileCount;
        this.spherePattern = spherePattern;
        this.preventSkeletonSpawn = preventSkeletonSpawn;
        this.removeStunEffect = removeStunEffect;
        this.hoverDurationTicks = hoverDurationTicks;
        this.minSpecialAttackSpacingSeconds = minSpecialAttackSpacingSeconds;
        this.requireWarriorSummonBetweenSpecials = requireWarriorSummonBetweenSpecials;
        this.soulFireParticles = soulFireParticles;
        this.endRodParticles = endRodParticles;
        this.skullTrailParticles = skullTrailParticles;
        this.groundGatherDurationTicks = groundGatherDurationTicks;
        this.groundGatherPauseTicks = groundGatherPauseTicks;
        
        // Use defaults for new phase-specific settings
        this.hemisphereFormationDurationTicks = 50;
        this.safeZonesWindowTicks = 30;
        this.finalPrepDurationTicks = 16;
        this.phaseTimeoutMultiplier = 1.5;
        this.hemisphereRadius = 6.0;
        this.hemisphereRadiusVariance = 1.0;
        this.hemisphereMinPolarAngleDegrees = 20.0;
        this.hemisphereMaxPolarAngleDegrees = 90.0;
        this.hemisphereSkullCount = 14;
        this.minViableSkullThreshold = 8;
        this.minSafeZonesWhenPlayersPresent = 1;
        this.maxSafeZones = 4;
    }
    
    /**
     * Check if special attack is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Check if rising animation is enabled
     */
    public boolean hasRisingAnimation() {
        return risingAnimation;
    }
    
    /**
     * Get the height boss rises during special attack
     */
    public double getRisingHeight() {
        return risingHeight;
    }
    
    /**
     * Get the duration of rising animation in ticks
     */
    public int getRisingDuration() {
        return risingDuration;
    }
    
    /**
     * Get the number of skull projectiles to spawn
     */
    public int getProjectileCount() {
        return projectileCount;
    }
    
    /**
     * Check if sphere pattern should be used for projectiles
     */
    public boolean useSpherePattern() {
        return spherePattern;
    }
    
    /**
     * Check if skeleton warrior spawning should be prevented during special attack
     */
    public boolean shouldPreventSkeletonSpawn() {
        return preventSkeletonSpawn;
    }
    
    /**
     * Check if stun effect should be removed before special attack
     */
    public boolean shouldRemoveStunEffect() {
        return removeStunEffect;
    }
    
    /**
     * Check if soul fire particles should be used during rising animation
     */
    public boolean hasSoulFireParticles() {
        return soulFireParticles;
    }
    
    /**
     * Check if end rod particles should be used during rising animation
     */
    public boolean hasEndRodParticles() {
        return endRodParticles;
    }
    
    /**
     * Check if skull trail particles should be used
     */
    public boolean hasSkullTrailParticles() {
        return skullTrailParticles;
    }
    
    /**
     * Get hover duration at peak before launch (in ticks)
     * 0 = immediate launch
     */
    public int getHoverDurationTicks() {
        return hoverDurationTicks;
    }
    
    /**
     * Get minimum spacing between special attacks (in seconds)
     */
    public int getMinSpecialAttackSpacingSeconds() {
        return minSpecialAttackSpacingSeconds;
    }
    
    /**
     * Check if warrior summon window is required between special attacks
     */
    public boolean requiresWarriorSummonBetweenSpecials() {
        return requireWarriorSummonBetweenSpecials;
    }
    
    /**
     * Get duration of ground skull gather phase (in ticks)
     */
    public int getGroundGatherDurationTicks() {
        return groundGatherDurationTicks;
    }
    
    /**
     * Get duration of pause after ground gather (in ticks)
     */
    public int getGroundGatherPauseTicks() {
        return groundGatherPauseTicks;
    }
    
    /**
     * Get particle types for rising animation
     */
    public Particle[] getRisingParticles() {
        if (!soulFireParticles && !endRodParticles) {
            return new Particle[0];
        }
        
        if (soulFireParticles && endRodParticles) {
            return new Particle[]{Particle.SOUL_FIRE_FLAME, Particle.END_ROD};
        }
        
        if (soulFireParticles) {
            return new Particle[]{Particle.SOUL_FIRE_FLAME};
        }
        
        return new Particle[]{Particle.END_ROD};
    }
    
    /**
     * Validate configuration values
     */
    public boolean isValid() {
        return risingHeight >= 5.0 && risingHeight <= 20.0 &&
               risingDuration >= 20 && risingDuration <= 100 &&
               projectileCount >= 16 && projectileCount <= 64;
    }
    
    // Phase-specific timing getters
    
    /**
     * Get hemisphere formation duration (in ticks)
     * Time window for skulls to form hemisphere pattern
     */
    public int getHemisphereFormationDurationTicks() {
        return hemisphereFormationDurationTicks;
    }
    
    /**
     * Get safe zones appearance window (in ticks)
     * Time window for safe zones to appear and become visible
     */
    public int getSafeZonesWindowTicks() {
        return safeZonesWindowTicks;
    }
    
    /**
     * Get final preparation duration (in ticks)
     * Final pause before hemisphere attack launches
     */
    public int getFinalPrepDurationTicks() {
        return finalPrepDurationTicks;
    }
    
    /**
     * Get phase timeout multiplier
     * Used to calculate timeout = base_duration * multiplier
     */
    public double getPhaseTimeoutMultiplier() {
        return phaseTimeoutMultiplier;
    }
    
    // Hemisphere geometry getters
    
    /**
     * Get base hemisphere radius (in blocks)
     */
    public double getHemisphereRadius() {
        return hemisphereRadius;
    }
    
    /**
     * Get hemisphere radius variance (in blocks)
     * Actual radius will be: baseRadius ± variance
     */
    public double getHemisphereRadiusVariance() {
        return hemisphereRadiusVariance;
    }
    
    /**
     * Get minimum polar angle for hemisphere (in degrees)
     * Polar angle θ defines vertical spread (0° = straight up, 90° = horizontal)
     */
    public double getHemispherePolarAngleMin() {
        return hemisphereMinPolarAngleDegrees;
    }
    
    /**
     * Get maximum polar angle for hemisphere (in degrees)
     * Polar angle θ defines vertical spread (0° = straight up, 90° = horizontal)
     */
    public double getHemispherePolarAngleMax() {
        return hemisphereMaxPolarAngleDegrees;
    }
    
    /**
     * @deprecated Use {@link #getHemispherePolarAngleMin()} instead
     */
    @Deprecated
    public double getHemisphereMinPolarAngleDegrees() {
        return hemisphereMinPolarAngleDegrees;
    }
    
    /**
     * @deprecated Use {@link #getHemispherePolarAngleMax()} instead
     */
    @Deprecated
    public double getHemisphereMaxPolarAngleDegrees() {
        return hemisphereMaxPolarAngleDegrees;
    }
    
    /**
     * Get number of skulls in hemisphere formation
     */
    public int getHemisphereSkullCount() {
        return hemisphereSkullCount;
    }
    
    /**
     * Get minimum viable skull threshold
     * Minimum number of skulls that must be positioned to proceed with attack
     */
    public int getMinViableSkullThreshold() {
        return minViableSkullThreshold;
    }
    
    // Safe zone getters
    
    /**
     * Get minimum safe zones when players are present
     * Guarantees at least this many zones if any players are in range
     */
    public int getMinSafeZonesWhenPlayersPresent() {
        return minSafeZonesWhenPlayersPresent;
    }
    
    /**
     * Get maximum safe zones to create
     * Formula: min(playerCount + 1, maxSafeZones)
     */
    public int getMaxSafeZones() {
        return maxSafeZones;
    }
}