package com.mmmm.story.bosses;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Represents a wither skull projectile used in Boss 1's special attack.
 * Handles rising animation, sphere pattern targeting, and visual effects.
 */
public class WitherSkullProjectile {
    
    private final UUID id;
    private final Plugin plugin;
    private final SpecialAttackConfiguration config;
    
    // Projectile state
    private Location origin;
    private Location target;
    private Location finalTarget;
    private boolean isRisingPhase;
    private boolean isAttackPhase;
    private double damage;
    private Vector velocity;
    private long creationTime;

    // Enhanced ground rising support for hemisphere attack
    private Location groundOrigin;
    private Location hemispherePosition;
    private double risingProgress;
    private boolean isRisingFromGround;
    
    // Entity reference
    private WitherSkull skullEntity;
    private Skeleton shooter;
    
    // Visual effects
    private BukkitRunnable particleTask;
    
    /**
     * Create a new wither skull projectile
     * @param plugin Plugin instance
     * @param config Special attack configuration
     * @param origin Starting position at ground level
     * @param target Target position (boss location during rising phase)
     * @param damage Damage dealt on impact
     * @param shooter Boss entity that shot this projectile
     */
    public WitherSkullProjectile(Plugin plugin, SpecialAttackConfiguration config, 
                                Location origin, Location target, double damage, Skeleton shooter) {
        this.id = UUID.randomUUID();
        this.plugin = plugin;
        this.config = config;
        this.origin = origin.clone();
        this.target = target.clone();
        this.finalTarget = null; // Set later during attack phase
        this.isRisingPhase = true;
        this.isAttackPhase = false;
        this.damage = damage;
        this.velocity = new Vector(0, 0, 0); // Will be calculated
        this.creationTime = System.currentTimeMillis();
        this.skullEntity = null;
        this.particleTask = null;
        this.shooter = shooter;

        // Initialize enhanced ground rising support
        this.groundOrigin = origin.clone();
        this.hemispherePosition = target.clone();
        this.risingProgress = 0.0;
        this.isRisingFromGround = true; // Enhanced hemisphere support
    }
    
    /**
     * Spawn the wither skull entity and start rising animation
     * @param world World to spawn in
     * @return True if successfully spawned
     */
    public boolean spawn(World world) {
        if (world == null || origin == null) {
            return false;
        }
        
        // Spawn the wither skull at origin
        skullEntity = world.spawn(origin, WitherSkull.class);
        if (skullEntity == null) {
            return false;
        }
        
        // Set skull properties
        skullEntity.setYield(0f); // No terrain damage
        skullEntity.setIsIncendiary(false); // No fire
        skullEntity.setCharged(false); // Not blue skull
        skullEntity.setShooter(shooter); // Set boss as shooter
        
        // Make skull harmless during gather phase
        skullEntity.setSilent(true); // Silent during gather
        
        // Calculate initial velocity toward target (rising phase)
        Vector direction = target.toVector().subtract(origin.toVector()).normalize();
        double speed = 0.5; // Rising speed
        velocity = direction.multiply(speed);
        skullEntity.setVelocity(velocity);
        
        // Start particle effects
        startParticleEffects();
        
        return true;
    }
    
    /**
     * Start particle effects for the projectile
     */
    private void startParticleEffects() {
        if (particleTask != null) {
            particleTask.cancel();
        }
        
        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (skullEntity == null || !skullEntity.isValid()) {
                    cancel();
                    return;
                }
                
                Location loc = skullEntity.getLocation();
                
                // T021: Enhanced particle effects during rising phase
                if (isRisingPhase && config.hasSkullTrailParticles()) {
                    // Enhanced soul fire trail with swirling pattern
                    World world = loc.getWorld();
                    double time = System.currentTimeMillis() * 0.01;
                    
                    // Create swirling soul fire pattern
                    for (int i = 0; i < 3; i++) {
                        double angle = time + (i * 2 * Math.PI / 3);
                        double radius = 0.3;
                        
                        double x = loc.getX() + radius * Math.cos(angle);
                        double y = loc.getY() + 0.2 * Math.sin(time * 2);
                        double z = loc.getZ() + radius * Math.sin(angle);
                        
                        Location particleLoc = new Location(world, x, y, z);
                        world.spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 1, 0, 0.1, 0, 0.02);
                    }
                    
                    // Enhanced end rod trail with pulsing effect
                    double pulse = Math.sin(time * 3) * 0.5 + 0.5;
                    world.spawnParticle(Particle.END_ROD, loc, 1, 0.05, 0.05, 0.05, 0.02 + pulse * 0.01);
                    
                    // Add occasional sparkles for visual enhancement
                    if (Math.random() < 0.3) {
                        world.spawnParticle(Particle.ENCHANT, loc, 1, 0.1, 0.1, 0.1, 0.01);
                    }
                }
                
                // T022/T023: Enhanced visual effects for attack phase
                if (isAttackPhase) {
                    World world = loc.getWorld();
                    
                    // Enhanced dragon breath trail
                    world.spawnParticle(Particle.DRAGON_BREATH, loc, 2, 0.1, 0.1, 0.1, 0.05);
                    
                    // Add glowing effect during attack phase
                    world.spawnParticle(Particle.GLOW, loc, 1, 0.05, 0.05, 0.05, 0);
                    
                    // Add critical hit particles for impact anticipation
                    if (Math.random() < 0.2) {
                        world.spawnParticle(Particle.CRIT, loc, 1, 0.1, 0.1, 0.1, 0.01);
                    }
                }
            }
        };
        
        particleTask.runTaskTimer(plugin, 0L, 1L); // Every tick
    }
    
    /**
     * Update projectile state
     * @return True if projectile is still active
     */
    public boolean update() {
        if (skullEntity == null || !skullEntity.isValid()) {
            cleanup();
            return false;
        }

        Location currentLoc = skullEntity.getLocation();

        if (isRisingPhase) {
            // Enhanced ground rising support
            if (isRisingFromGround) {
                updateRisingFromGround(currentLoc);
            } else {
                // Legacy rising logic
                if (currentLoc.distance(target) <= 1.0) {
                    // Transition to attack phase
                    isRisingPhase = false;
                    isAttackPhase = true;
                }
            }
        }

        if (isAttackPhase && finalTarget != null) {
            // Make skull dangerous during attack phase
            if (skullEntity != null) {
                skullEntity.setSilent(false); // Enable sound for attack phase
            }

            // Calculate velocity toward final target
            Vector direction = finalTarget.toVector().subtract(skullEntity.getLocation().toVector()).normalize();
            double speed = 1.5; // Attack speed (faster than rising)
            velocity = direction.multiply(speed);
            skullEntity.setVelocity(velocity);
        }

        return true;
    }

    /**
     * Enhanced rising from ground animation for hemisphere attack
     * @param currentLoc Current location of the skull
     */
    private void updateRisingFromGround(Location currentLoc) {
        // Calculate rising progress
        double totalDistance = groundOrigin.distance(hemispherePosition);
        double currentDistance = groundOrigin.distance(currentLoc);
        risingProgress = Math.min(1.0, currentDistance / totalDistance);

        // Check if reached hemisphere position
        if (currentLoc.distance(hemispherePosition) <= 1.0) {
            // Transition to attack phase
            isRisingPhase = false;
            isAttackPhase = true;
            isRisingFromGround = false; // Rising complete

            // Spawn arrival particles
            spawnHemisphereArrivalParticles();
            return;
        }

        // Enhanced rising animation with ground-to-air movement
        Vector direction = hemispherePosition.toVector().subtract(currentLoc.toVector()).normalize();
        double speed = 0.4 + (risingProgress * 0.3); // Accelerating rise
        velocity = direction.multiply(speed);
        skullEntity.setVelocity(velocity);

        // Spawn enhanced rising particles
        if (System.currentTimeMillis() % 3 == 0) { // Every 3 ticks
            spawnEnhancedRisingParticles(currentLoc);
        }
    }

    /**
     * Spawn enhanced particles for ground-to-air rising
     * @param currentLoc Current location of the skull
     */
    private void spawnEnhancedRisingParticles(Location currentLoc) {
        World world = currentLoc.getWorld();
        if (world == null) return;

        // Ground lifting effect
        for (int i = 0; i < 3; i++) {
            double angle = (i / 3.0) * 2 * Math.PI;
            double radius = 0.8;
            double x = currentLoc.getX() + radius * Math.cos(angle);
            double z = currentLoc.getZ() + radius * Math.sin(angle);
            Location particleLoc = new Location(world, x, currentLoc.getY(), z);

            world.spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 1, 0, 0.2, 0, 0.01);
            world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0.1, 0, 0.005);
        }

        // Vertical trail effect
        world.spawnParticle(Particle.DRAGON_BREATH, currentLoc, 1, 0.1, 0.2, 0.1, 0.02);
    }

    /**
     * Spawn arrival particles when skull reaches hemisphere position
     */
    private void spawnHemisphereArrivalParticles() {
        World world = hemispherePosition.getWorld();
        if (world == null) return;

        // Dramatic arrival effect
        world.spawnParticle(Particle.EXPLOSION, hemispherePosition, 3, 0.2, 0.2, 0.2, 0.1);
        world.spawnParticle(Particle.DRAGON_BREATH, hemispherePosition, 8, 0.3, 0.3, 0.3, 0.15);
        world.spawnParticle(Particle.GLOW, hemispherePosition, 5, 0.2, 0.2, 0.2, 0.05);

        // Sound effect
        world.playSound(hemispherePosition, org.bukkit.Sound.ENTITY_WITHER_AMBIENT, 0.8f, 1.3f);
    }
    
    /**
     * Set final target for attack phase (sphere pattern)
     * @param finalTarget Final target position (player location)
     */
    public void setFinalTarget(Location finalTarget) {
        this.finalTarget = finalTarget.clone();
        
        if (isAttackPhase && skullEntity != null) {
            // Calculate velocity toward final target
            Vector direction = finalTarget.toVector().subtract(skullEntity.getLocation().toVector()).normalize();
            double speed = 1.5; // Attack speed (faster than rising)
            velocity = direction.multiply(speed);
            skullEntity.setVelocity(velocity);
        }
    }
    
    /**
     * Trigger explosion at impact location
     */
    private void explode() {
        if (skullEntity == null) {
            return;
        }
        
        Location impactLoc = skullEntity.getLocation();
        World world = impactLoc.getWorld();
        
        // T023: Enhanced visual effects for sphere pattern projectile launch
        // Visual explosion effects
        world.spawnParticle(Particle.EXPLOSION, impactLoc, 5, 0.2, 0.2, 0.2, 0);
        world.spawnParticle(Particle.DRAGON_BREATH, impactLoc, 15, 0.4, 0.4, 0.4, 0.15);
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, impactLoc, 20, 0.4, 0.4, 0.4, 0.08);
        
        // Add sphere pattern impact effects
        for (int i = 0; i < 8; i++) {
            double angle = (i / 8.0) * 2 * Math.PI;
            double radius = 1.5;
            
            double x = impactLoc.getX() + radius * Math.cos(angle);
            double z = impactLoc.getZ() + radius * Math.sin(angle);
            
            Location particleLoc = new Location(world, x, impactLoc.getY(), z);
            world.spawnParticle(Particle.END_ROD, particleLoc, 3, 0.2, 0.2, 0.2, 0.05);
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 2, 0.1, 0.1, 0.1, 0.03);
        }
        
        // Add dramatic flash effect
        world.spawnParticle(Particle.FLASH, impactLoc, 2, 0, 0, 0, 0);
        
        // Enhanced sound effects
        world.playSound(impactLoc, org.bukkit.Sound.ENTITY_WITHER_SHOOT, 1.5f, 1.0f);
        world.playSound(impactLoc, org.bukkit.Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.0f, 0.8f);
        
        // Apply damage to nearby players (handled by Act2Listener)
        // The actual damage application is done in the explosion event handler
    }
    
    /**
     * Clean up resources
     */
    private void cleanup() {
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }
        
        if (skullEntity != null && skullEntity.isValid()) {
            skullEntity.remove();
            skullEntity = null;
        }
    }
    
    /**
     * Generate sphere pattern targets for projectiles
     * @param center Center of sphere (boss position) - hemisphere ALWAYS forms around this
     * @param targets List of player targets outside safe zones - determines launch DIRECTION
     * @param projectileCount Number of projectiles to generate
     * @return List of target locations in sphere pattern
     */
    public static List<Location> generateSpherePatternTargets(Location center, List<Player> targets, int projectileCount) {
        List<Location> sphereTargets = new ArrayList<>();
        
        // DEBUG: Log method entry
        Logger logger = Logger.getLogger("Minecraft");
        logger.info(String.format("[DEBUG-SPHERE] generateSpherePatternTargets called: center=(%.2f, %.2f, %.2f), targets=%d, count=%d",
            center.getX(), center.getY(), center.getZ(), targets.size(), projectileCount));
        
        if (targets.isEmpty()) {
            // No valid targets, distribute evenly around boss
            logger.info("[DEBUG-SPHERE] No valid targets - hemisphere forms around boss, targets random directions");
            
            for (int i = 0; i < projectileCount; i++) {
                double phi = Math.acos(1 - 2 * (double) i / projectileCount);
                double theta = Math.sqrt(projectileCount * Math.PI) * phi;
                
                double x = center.getX() + 10 * Math.cos(theta) * Math.sin(phi);
                double y = center.getY() + 10 * Math.cos(phi);
                double z = center.getZ() + 10 * Math.sin(theta) * Math.sin(phi);
                
                sphereTargets.add(new Location(center.getWorld(), x, y, z));
            }
        } else {
            // CRITICAL FIX: Hemisphere forms around BOSS (center), but targets PLAYERS
            // The hemisphere forms around the boss, then skulls launch toward players
            logger.info(String.format("[DEBUG-SPHERE] Found %d targets - hemisphere forms around BOSS, skulls target PLAYERS", targets.size()));
            
            int projectilesPerTarget = projectileCount / targets.size();
            int remaining = projectileCount % targets.size();
            
            for (int i = 0; i < targets.size(); i++) {
                Player target = targets.get(i);
                Location targetLoc = target.getLocation();
                
                // Calculate direction from boss to player
                Vector directionToPlayer = targetLoc.toVector().subtract(center.toVector()).normalize();
                
                // Generate projectiles around this direction (on hemisphere around BOSS)
                int count = projectilesPerTarget + (i < remaining ? 1 : 0);
                
                logger.info(String.format("[DEBUG-SPHERE] Player '%s' at (%.2f, %.2f, %.2f) - generating %d skull targets in hemisphere around boss",
                    target.getName(), targetLoc.getX(), targetLoc.getY(), targetLoc.getZ(), count));
                
                for (int j = 0; j < count; j++) {
                    // Generate target locations in a cone/spread toward the player
                    // Start from boss center, project outward toward player with some spread
                    double spread = 0.3; // Spread angle in radians
                    double angleOffset = (j - count / 2.0) * spread / count;
                    
                    // Calculate perpendicular vector for spread
                    Vector perpendicular = new Vector(-directionToPlayer.getZ(), 0, directionToPlayer.getX()).normalize();
                    
                    // Apply spread
                    Vector spreadDirection = directionToPlayer.clone()
                        .add(perpendicular.multiply(Math.sin(angleOffset)))
                        .normalize();
                    
                    // Project to target distance (10-15 blocks from boss)
                    double targetDistance = 12.0 + (j % 3) * 1.5; // Vary distance slightly
                    
                    double x = center.getX() + spreadDirection.getX() * targetDistance;
                    double y = center.getY() + spreadDirection.getY() * targetDistance;
                    double z = center.getZ() + spreadDirection.getZ() * targetDistance;
                    
                    sphereTargets.add(new Location(center.getWorld(), x, y, z));
                    
                    logger.info(String.format("[DEBUG-SPHERE]   Skull target %d: offset from boss = (%.2f, %.2f, %.2f), distance=%.2f",
                        j, x - center.getX(), y - center.getY(), z - center.getZ(), targetDistance));
                }
            }
        }
        
        return sphereTargets;
    }
    
    /**
     * Get projectile unique ID
     */
    public UUID getId() {
        return id;
    }
    
    /**
     * Get current location of projectile
     */
    public Location getLocation() {
        if (skullEntity != null) {
            return skullEntity.getLocation();
        }
        return origin;
    }
    
    /**
     * Get damage value
     */
    public double getDamage() {
        return damage;
    }
    
    /**
     * Check if projectile is in rising phase
     */
    public boolean isRisingPhase() {
        return isRisingPhase;
    }
    
    /**
     * Check if projectile is in attack phase
     */
    public boolean isAttackPhase() {
        return isAttackPhase;
    }
    
    /**
     * Get skull entity
     */
    public WitherSkull getSkullEntity() {
        return skullEntity;
    }
    
    /**
     * Get shooter entity
     */
    public Skeleton getShooter() {
        return shooter;
    }
    
    /**
     * Check if projectile is still valid
     */
    public boolean isValid() {
        return skullEntity != null && skullEntity.isValid();
    }
}