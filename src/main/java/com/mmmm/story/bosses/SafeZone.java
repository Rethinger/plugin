package com.mmmm.story.bosses;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Represents a temporary damage-immune area during boss wither skull attack.
 * Safe zones are marked with red particle spheres and protect players from wither skull damage.
 */
public class SafeZone {
    
    private final Location center;
    private final double radius;
    private final long createdAt;
    private final long expiresAt;
    
    /**
     * Create a new safe zone
     * @param center Center location of the safe zone (ground level)
     * @param radius Protection radius in blocks
     * @param durationSeconds How long the safe zone lasts
     */
    public SafeZone(Location center, double radius, int durationSeconds) {
        this.center = center.clone();
        this.radius = radius;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = createdAt + (durationSeconds * 1000L);
    }
    
    /**
     * Check if a location is within this safe zone
     * Uses 2D distance (ignores Y coordinate) for ground-based protection
     * @param location Location to check
     * @return True if location is protected
     */
    public boolean contains(Location location) {
        if (!location.getWorld().equals(center.getWorld())) {
            return false;
        }
        
        // Calculate 2D distance (XZ plane only)
        double deltaX = location.getX() - center.getX();
        double deltaZ = location.getZ() - center.getZ();
        double distance2D = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        
        return distance2D <= radius;
    }
    
    /**
     * Check if this safe zone has expired
     */
    public boolean isExpired() {
        return System.currentTimeMillis() >= expiresAt;
    }
    
    /**
     * Get remaining duration in milliseconds
     */
    public long getRemainingMillis() {
        long remaining = expiresAt - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
    
    /**
     * Get the center location of this safe zone
     */
    public Location getCenter() {
        return center.clone();
    }
    
    /**
     * Get the protection radius
     */
    public double getRadius() {
        return radius;
    }
    
    /**
     * Spawn particle sphere at this safe zone with beacon effect
     * Should be called periodically (e.g., every 5 ticks) while active
     * @param particleCount Number of particles to spawn per call
     */
    public void spawnParticles(int particleCount) {
        if (isExpired()) {
            return;
        }
        
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        
        // Create a sphere of particles (not just a circle)
        // Using Fibonacci sphere algorithm for even distribution
        double goldenRatio = (1 + Math.sqrt(5)) / 2;
        double angleIncrement = Math.PI * 2 * goldenRatio;
        
        for (int i = 0; i < particleCount; i++) {
            double t = (double) i / particleCount;
            double inclination = Math.acos(1 - 2 * t);
            double azimuth = angleIncrement * i;
            
            double x = center.getX() + radius * Math.sin(inclination) * Math.cos(azimuth);
            double y = center.getY() + radius * Math.cos(inclination);
            double z = center.getZ() + radius * Math.sin(inclination) * Math.sin(azimuth);
            
            Location particleLocation = new Location(world, x, y, z);
            
            // Spawn red dust particle (main sphere)
            world.spawnParticle(
                Particle.DUST,
                particleLocation,
                1,
                0, 0, 0,
                0,
                new Particle.DustOptions(org.bukkit.Color.RED, 1.0f)
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
        }
        
        // Add vertical beacon effect (END_ROD particles)
        // Spawn from ground up through ceiling
        Location beaconStart = center.clone();
        beaconStart.setY(center.getY() - 1); // Start slightly below center
        
        for (int i = 0; i < 10; i++) { // 10 particles upward
            Location beaconParticle = beaconStart.clone().add(0, i * 0.5, 0);
            world.spawnParticle(
                Particle.END_ROD,
                beaconParticle,
                1,
                0.1, 0, 0.1, // Slight horizontal spread
                0.02 // Slight upward velocity
            );
        }
    }
    
    /**
     * Find the highest solid block at a location (proper ground detection)
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
     * Check if there's a ceiling above a location (within reasonable distance)
     * @param world World to check
     * @param x X coordinate
     * @param z Z coordinate
     * @param startY Starting Y coordinate
     * @return Y coordinate of ceiling block, or -1 if no ceiling within 10 blocks
     */
    private static int findCeiling(World world, double x, double z, int startY) {
        for (int y = startY; y < startY + 10; y++) {
            Block block = world.getBlockAt((int) x, y, (int) z);
            if (block.getType().isSolid() && block.getType() != Material.AIR) {
                return y;
            }
        }
        return -1; // No ceiling found
    }
    
    /**
     * Generate a safe zone at a random position within a radius of a center point
     * @param centerLocation Center to generate around
     * @param maxRadius Maximum distance from center
     * @param safeZoneRadius Radius of the safe zone itself
     * @param durationSeconds How long the safe zone lasts
     * @return New safe zone at random location
     */
    public static SafeZone generateRandom(Location centerLocation, double maxRadius, 
                                          double safeZoneRadius, int durationSeconds) {
        // Random angle
        double angle = Math.random() * 2 * Math.PI;
        
        // Random distance (weighted toward edges for better distribution)
        double distance = Math.sqrt(Math.random()) * maxRadius;
        
        // Calculate position
        double x = centerLocation.getX() + distance * Math.cos(angle);
        double z = centerLocation.getZ() + distance * Math.sin(angle);
        
        World world = centerLocation.getWorld();
        if (world == null) {
            return new SafeZone(centerLocation, safeZoneRadius, durationSeconds);
        }
        
        // Use improved ground detection
        int groundY = findHighestSolidBlock(world, x, z, (int) centerLocation.getY());
        
        // Check for ceiling
        int ceilingY = findCeiling(world, x, z, groundY);
        
        // If there's a ceiling close to ground, adjust Y to be visible above ceiling
        if (ceilingY != -1 && (ceilingY - groundY) < 5) {
            // Place safe zone above ceiling for visibility
            groundY = ceilingY + 2;
        }
        
        Location safeZoneLocation = new Location(world, x, groundY, z);
        
        return new SafeZone(safeZoneLocation, safeZoneRadius, durationSeconds);
    }
}
