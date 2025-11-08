package com.mmmm.story.bosses;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Configuration for stationary casting boss attack parameters.
 */
public class StationaryAttackConfiguration {

    // Core timing parameters
    private final int castingDurationTicks;
    private final double attackRadius;
    private final double safeZoneRadius;
    private final int maxSafeZones;

    // Fang attack parameters
    private final int totalFangs;
    private final int fangsPerWave;
    private final int waveIntervalTicks;

    // Visual effects parameters
    private final boolean enableParticles;
    private final int maxParticlesPerTick;
    private final boolean enableSoundEffects;

    /**
     * Create configuration from config section
     * @param config Configuration section containing attack settings
     */
    public StationaryAttackConfiguration(ConfigurationSection config) {
        // Core parameters with defaults
        this.castingDurationTicks = config.getInt("castingDurationTicks", 60); // 3 seconds
        this.attackRadius = config.getDouble("attackRadius", 15.0);
        this.safeZoneRadius = config.getDouble("safeZoneRadius", 1.5);
        this.maxSafeZones = config.getInt("maxSafeZones", 5);

        // Fang attack parameters
        this.totalFangs = config.getInt("totalFangs", 25);
        this.fangsPerWave = config.getInt("fangsPerWave", 8);
        this.waveIntervalTicks = config.getInt("waveIntervalTicks", 10); // 0.5 seconds

        // Visual effects parameters
        this.enableParticles = config.getBoolean("enableParticles", true);
        this.maxParticlesPerTick = config.getInt("maxParticlesPerTick", 100);
        this.enableSoundEffects = config.getBoolean("enableSoundEffects", true);
    }

    /**
     * Create default configuration
     */
    public StationaryAttackConfiguration() {
        this.castingDurationTicks = 60; // 3 seconds
        this.attackRadius = 15.0;
        this.safeZoneRadius = 1.5;
        this.maxSafeZones = 5;

        this.totalFangs = 25;
        this.fangsPerWave = 8;
        this.waveIntervalTicks = 10; // 0.5 seconds

        this.enableParticles = true;
        this.maxParticlesPerTick = 100;
        this.enableSoundEffects = true;
    }

    // Getters for configuration parameters
    public int getCastingDurationTicks() { return castingDurationTicks; }
    public double getAttackRadius() { return attackRadius; }
    public double getSafeZoneRadius() { return safeZoneRadius; }
    public int getMaxSafeZones() { return maxSafeZones; }
    public int getTotalFangs() { return totalFangs; }
    public int getFangsPerWave() { return fangsPerWave; }
    public int getWaveIntervalTicks() { return waveIntervalTicks; }
    public boolean isParticlesEnabled() { return enableParticles; }
    public int getMaxParticlesPerTick() { return maxParticlesPerTick; }
    public boolean isSoundEnabled() { return enableSoundEffects; }
}