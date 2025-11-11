package com.mmmm.story.managers;

import com.mmmm.story.bosses.SafeZone;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Manages safe zones during boss special attacks.
 * Handles creation, updates, and removal of safe zones with configurable visual effects.
 * Enhanced for hemisphere attack with sequential zone appearance.
 */
public class SafeZoneManager {

    private final Plugin plugin;
    private final List<SafeZone> activeSafeZones;
    private final List<SafeZone> pendingSafeZones; // Zones waiting to be revealed
    private boolean beaconEffectEnabled;

    // Sequential appearance support
    private BukkitRunnable appearanceTask;
    private int appearanceIntervalTicks;
    private boolean isSequentialAppearance;

    /**
     * Create a new safe zone manager
     * @param plugin Plugin instance
     */
    public SafeZoneManager(Plugin plugin) {
        this.plugin = plugin;
        this.activeSafeZones = new ArrayList<>();
        this.pendingSafeZones = new ArrayList<>();
        this.beaconEffectEnabled = true; // Default enabled
        this.appearanceTask = null;
        this.appearanceIntervalTicks = 10; // Default: reveal every 10 ticks (0.5 seconds)
        this.isSequentialAppearance = false;
    }
    
    /**
     * Generate safe zones around a center location
     * @param centerLocation Center to generate around
     * @param maxRadius Maximum distance from center
     * @param safeZoneRadius Radius of each safe zone
     * @param durationSeconds How long safe zones last
     * @param minCount Minimum number of safe zones
     * @param maxCount Maximum number of safe zones
     * @return List of generated safe zones
     */
    public List<SafeZone> generateSafeZones(Location centerLocation, double maxRadius,
                                           double safeZoneRadius, int durationSeconds,
                                           int minCount, int maxCount) {
        List<SafeZone> safeZones = new ArrayList<>();

        // Calculate number of safe zones
        int safeZoneCount = minCount + (int)(Math.random() * (maxCount - minCount + 1));

        // Generate safe zones
        for (int i = 0; i < safeZoneCount; i++) {
            SafeZone zone = SafeZone.generateRandom(centerLocation, maxRadius, safeZoneRadius, durationSeconds);
            safeZones.add(zone);
        }

        // Add to active list
        activeSafeZones.addAll(safeZones);

        return safeZones;
    }

    /**
     * Generate safe zones for hemisphere attack with exact count
     * @param centerLocation Center to generate around (boss position)
     * @param maxRadius Maximum distance from center
     * @param safeZoneRadius Radius of each safe zone
     * @param durationSeconds How long safe zones last
     * @param playerCount Number of players (will create playerCount + 1 zones)
     * @param sequentialAppearance Whether to reveal zones sequentially
     * @param appearanceIntervalTicks Ticks between zone appearances
     * @return List of generated safe zones
     */
    public List<SafeZone> generateHemisphereSafeZones(Location centerLocation, double maxRadius,
                                                     double safeZoneRadius, int durationSeconds,
                                                     int playerCount, boolean sequentialAppearance,
                                                     int appearanceIntervalTicks) {
        // Calculate exact number of safe zones (playerCount + 1)
        int safeZoneCount = playerCount + 1;

        List<SafeZone> safeZones = new ArrayList<>();

        // Generate safe zones with improved distribution for hemisphere attack
        for (int i = 0; i < safeZoneCount; i++) {
            SafeZone zone = generateHemisphereSafeZone(centerLocation, maxRadius, safeZoneRadius,
                                                      durationSeconds, i, safeZoneCount);
            safeZones.add(zone);
        }

        if (sequentialAppearance) {
            // Start sequential appearance
            startSequentialAppearance(safeZones, appearanceIntervalTicks);
        } else {
            // Add all zones to active list immediately
            activeSafeZones.addAll(safeZones);
        }

        plugin.getLogger().info("[SafeZoneManager] Generated " + safeZoneCount + " safe zones for " +
                               playerCount + " players (sequential: " + sequentialAppearance + ")");

        return safeZones;
    }

    /**
     * Generate a single safe zone for hemisphere attack with improved positioning
     * @param centerLocation Center to generate around
     * @param maxRadius Maximum distance from center
     * @param safeZoneRadius Radius of the safe zone
     * @param durationSeconds How long safe zone lasts
     * @param index Index of this zone (for distribution)
     * @param totalZones Total number of zones
     * @return New safe zone
     */
    private SafeZone generateHemisphereSafeZone(Location centerLocation, double maxRadius,
                                               double safeZoneRadius, int durationSeconds,
                                               int index, int totalZones) {
        World world = centerLocation.getWorld();
        if (world == null) {
            return new SafeZone(centerLocation, safeZoneRadius, durationSeconds);
        }

        // Use even distribution around the boss for hemisphere attack
        double angle = (index / (double) totalZones) * 2 * Math.PI;
        double distance = maxRadius * 0.6; // Use 60% of max radius for better spacing

        // Calculate position
        double x = centerLocation.getX() + distance * Math.cos(angle);
        double z = centerLocation.getZ() + distance * Math.sin(angle);

        // Find ground level (duplicate logic from SafeZone since method is private)
        int groundY = findHighestSolidBlock(world, x, z, (int) centerLocation.getY());

        Location safeZoneLocation = new Location(world, x, groundY, z);

        return new SafeZone(safeZoneLocation, safeZoneRadius, durationSeconds);
    }

    /**
     * Start sequential appearance of safe zones
     * @param zones List of zones to reveal
     * @param intervalTicks Ticks between revelations
     */
    private void startSequentialAppearance(List<SafeZone> zones, int intervalTicks) {
        if (appearanceTask != null) {
            appearanceTask.cancel();
        }

        pendingSafeZones.clear();
        pendingSafeZones.addAll(zones);
        this.appearanceIntervalTicks = intervalTicks;
        this.isSequentialAppearance = true;

        plugin.getLogger().info("[SafeZoneManager] Starting sequential appearance of " +
                               zones.size() + " zones every " + intervalTicks + " ticks");

        appearanceTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (pendingSafeZones.isEmpty()) {
                    cancel();
                    isSequentialAppearance = false;
                    plugin.getLogger().info("[SafeZoneManager] Sequential appearance completed");
                    return;
                }

                // Reveal next safe zone
                SafeZone nextZone = pendingSafeZones.remove(0);
                activeSafeZones.add(nextZone);

                // Spawn appearance effect
                spawnSafeZoneAppearanceEffect(nextZone);
            }
        };

        appearanceTask.runTaskTimer(plugin, 0L, intervalTicks);
    }

    /**
     * Spawn visual effect when a safe zone appears
     * @param zone Safe zone that appeared
     */
    private void spawnSafeZoneAppearanceEffect(SafeZone zone) {
        Location center = zone.getCenter();
        World world = center.getWorld();
        if (world == null) {
            return;
        }

        // Dramatic appearance effect
        world.spawnParticle(Particle.END_ROD, center, 30, zone.getRadius(), zone.getRadius(), zone.getRadius(), 0.2);
        world.spawnParticle(Particle.GLOW, center, 15, zone.getRadius(), zone.getRadius(), zone.getRadius(), 0.1);

        // Flash effect
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, center, 1, 0, 0, 0, 0);

        // Sound effect
        world.playSound(center, org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
        world.playSound(center, org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.5f);

        plugin.getLogger().info("[SafeZoneManager] Safe zone appeared at " +
                               String.format("%.1f, %.1f, %.1f", center.getX(), center.getY(), center.getZ()));
    }
    
    /**
     * Update all active safe zones (spawn particles)
     * @param particleCount Number of particles per safe zone
     */
    public void updateSafeZones(int particleCount) {
        // Remove expired safe zones
        activeSafeZones.removeIf(SafeZone::isExpired);
        
        // Update particles for remaining safe zones
        for (SafeZone zone : activeSafeZones) {
            if (beaconEffectEnabled) {
                zone.spawnParticles(particleCount);
            } else {
                // Spawn particles without beacon effect
                spawnParticlesWithoutBeacon(zone, particleCount);
            }
        }
    }
    
    /**
     * Spawn particles for safe zone without beacon effect
     * @param zone Safe zone to update
     * @param particleCount Number of particles to spawn
     */
    public void spawnParticlesWithoutBeacon(SafeZone zone, int particleCount) {
        if (zone.isExpired()) {
            return;
        }
        
        Location center = zone.getCenter();
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        
        double radius = zone.getRadius();
        
        // T025/T026: Enhanced safe zone particles without vertical beacon effect
        // Create a sphere of particles (not just a circle)
        // Using Fibonacci sphere algorithm for even distribution
        double goldenRatio = (1 + Math.sqrt(5)) / 2;
        double angleIncrement = Math.PI * 2 * goldenRatio;
        
        // Add pulsing effect for better visibility
        double pulse = Math.sin(System.currentTimeMillis() * 0.003) * 0.3 + 0.7;
        
        for (int i = 0; i < particleCount; i++) {
            double t = (double) i / particleCount;
            double inclination = Math.acos(1 - 2 * t);
            double azimuth = angleIncrement * i;
            
            double x = center.getX() + radius * Math.sin(inclination) * Math.cos(azimuth);
            double y = center.getY() + radius * Math.cos(inclination);
            double z = center.getZ() + radius * Math.sin(inclination) * Math.sin(azimuth);
            
            Location particleLocation = new Location(world, x, y, z);
            
            // Enhanced red dust particle with pulsing intensity
            world.spawnParticle(
                Particle.DUST,
                particleLocation,
                1,
                0, 0, 0,
                0,
                new Particle.DustOptions(org.bukkit.Color.fromRGB(255, (int)(100 * pulse), 0), 1.0f)
            );
            
            // Add glowing particles for better visibility
            if (i % 3 == 0) { // Every 3rd particle
                world.spawnParticle(
                    Particle.GLOW,
                    particleLocation,
                    1,
                    0, 0, 0,
                    0
                );
            }
            
            // Add occasional enchantment particles for magical effect
            if (i % 5 == 0 && Math.random() < 0.3) {
                world.spawnParticle(
                    Particle.ENCHANT,
                    particleLocation,
                    1,
                    0.1, 0.1, 0.1,
                    0.1
                );
            }
        }
        
        // Add ground-level circle effect for better visibility
        for (double angle = 0; angle < 2 * Math.PI; angle += Math.PI / 8) {
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location groundLoc = new Location(world, x, center.getY(), z);
            
            world.spawnParticle(
                Particle.END_ROD,
                groundLoc,
                1,
                0, 0.1, 0,
                0.01
            );
        }
        
        // NO vertical beacon effect (END_ROD particles) - removed as requested
    }
    
    /**
     * Check if a location is within any active safe zone
     * @param location Location to check
     * @return True if location is protected
     */
    public boolean isInSafeZone(Location location) {
        for (SafeZone zone : activeSafeZones) {
            if (zone.contains(location)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get all active safe zones
     * @return List of active safe zones
     */
    public List<SafeZone> getActiveSafeZones() {
        return new ArrayList<>(activeSafeZones);
    }
    
    /**
     * Clear all active safe zones
     */
    public void clearSafeZones() {
        activeSafeZones.clear();
    }
    
    /**
     * Remove expired safe zones
     * @return Number of zones removed
     */
    public int removeExpiredZones() {
        int initialSize = activeSafeZones.size();
        activeSafeZones.removeIf(SafeZone::isExpired);
        return initialSize - activeSafeZones.size();
    }
    
    /**
     * Set whether beacon effect is enabled for safe zones
     * @param enabled True to enable beacon effect
     */
    public void setBeaconEffectEnabled(boolean enabled) {
        this.beaconEffectEnabled = enabled;
    }
    
    /**
     * Check if beacon effect is enabled
     * @return True if beacon effect is enabled
     */
    public boolean isBeaconEffectEnabled() {
        return beaconEffectEnabled;
    }
    
    /**
     * Get count of active safe zones
     * @return Number of active safe zones
     */
    public int getActiveSafeZoneCount() {
        return activeSafeZones.size();
    }
    
    /**
     * Find the highest solid block at a location (proper ground detection)
     * Duplicate of SafeZone method since it's private
     * @param world World to search in
     * @param x X coordinate
     * @param z Z coordinate
     * @param startY Starting Y coordinate
     * @return Y coordinate of highest solid block, or startY if none found
     */
    private static int findHighestSolidBlock(World world, double x, double z, int startY) {
        // Search downward from startY to find ground
        for (int y = startY; y > world.getMinHeight(); y--) {
            Block block = world.getBlockAt((int) x, y, (int) z);
            if (block.getType().isSolid() && block.getType() != Material.AIR) {
                return y + 1; // Return position above solid block
            }
        }

        // Search upward if no ground found below
        for (int y = startY; y < world.getMaxHeight(); y++) {
            Block block = world.getBlockAt((int) x, y, (int) z);
            if (block.getType().isSolid() && block.getType() != Material.AIR) {
                return y + 1;
            }
        }

        return startY; // Fallback to starting position
    }

    /**
     * Check if sequential appearance is currently active
     * @return True if zones are appearing sequentially
     */
    public boolean isSequentialAppearanceActive() {
        return isSequentialAppearance && appearanceTask != null;
    }

    /**
     * Get number of pending zones yet to be revealed
     * @return Number of pending zones
     */
    public int getPendingZoneCount() {
        return pendingSafeZones.size();
    }
    
    /**
     * Get number of active safe zones (excluding pending)
     * @return Number of active zones
     */
    public int getActiveZoneCount() {
        return activeSafeZones.size();
    }

    /**
     * Force stop sequential appearance
     */
    public void stopSequentialAppearance() {
        if (appearanceTask != null) {
            appearanceTask.cancel();
            appearanceTask = null;
        }

        // Move all pending zones to active immediately
        if (!pendingSafeZones.isEmpty()) {
            activeSafeZones.addAll(pendingSafeZones);
            pendingSafeZones.clear();
        }

        isSequentialAppearance = false;
        plugin.getLogger().info("[SafeZoneManager] Sequential appearance stopped");
    }

    /**
     * Clean up all safe zones
     */
    public void cleanup() {
        // Stop sequential appearance if active
        stopSequentialAppearance();

        clearSafeZones();
    }
}