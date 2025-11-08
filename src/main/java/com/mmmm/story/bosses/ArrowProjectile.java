package com.mmmm.story.bosses;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Skeleton;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * Represents an arrow projectile used in the enhanced boss special attack phase 2.
 * Handles rising animation, instant damage application, and visual effects.
 * Arrows deal exactly 1 damage (2 HP) and ignore armor/enchantments.
 */
public class ArrowProjectile {

    private final UUID id;
    private final Plugin plugin;

    // Projectile state
    private Location origin;
    private Location target;
    private Location finalTarget;
    private boolean isRisingPhase;
    private boolean isAttackPhase;
    private Vector velocity;
    private long creationTime;

    // Entity reference
    private Arrow arrowEntity;
    private Skeleton shooter;

    // Visual effects
    private BukkitRunnable particleTask;

    /**
     * Create a new arrow projectile
     * @param plugin Plugin instance
     * @param origin Starting position at ground level
     * @param target Target position (boss location during rising phase)
     * @param shooter Boss entity that shot this projectile
     */
    public ArrowProjectile(Plugin plugin, Location origin, Location target, Skeleton shooter) {
        this.id = UUID.randomUUID();
        this.plugin = plugin;
        this.origin = origin.clone();
        this.target = target.clone();
        this.finalTarget = null; // Set later during attack phase
        this.isRisingPhase = true;
        this.isAttackPhase = false;
        this.velocity = new Vector(0, 0, 0); // Will be calculated
        this.creationTime = System.currentTimeMillis();
        this.arrowEntity = null;
        this.particleTask = null;
        this.shooter = shooter;
    }

    /**
     * Spawn the arrow entity and start rising animation
     * @param world World to spawn in
     * @return True if successfully spawned
     */
    public boolean spawn(World world) {
        if (world == null || origin == null) {
            return false;
        }

        // Spawn the arrow at origin
        arrowEntity = world.spawn(origin, Arrow.class);
        if (arrowEntity == null) {
            return false;
        }

        // Set arrow properties
        arrowEntity.setPickupStatus(Arrow.PickupStatus.CREATIVE_ONLY); // Prevent pickup
        arrowEntity.setCritical(false); // Not a critical hit by default
        arrowEntity.setDamage(0.0); // We'll handle damage ourselves
        arrowEntity.setKnockbackStrength(0); // No knockback from our arrows
        arrowEntity.setShooter(shooter); // Set boss as shooter

        // Make arrow silent during gather phase
        arrowEntity.setSilent(true);

        // Calculate initial velocity toward target (rising phase)
        Vector direction = target.toVector().subtract(origin.toVector()).normalize();
        double speed = 0.4; // Rising speed (slightly slower than skulls)
        velocity = direction.multiply(speed);
        arrowEntity.setVelocity(velocity);

        // Start particle effects
        startParticleEffects();

        return true;
    }

    /**
     * Start particle effects for the arrow projectile
     */
    private void startParticleEffects() {
        if (particleTask != null) {
            particleTask.cancel();
        }

        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (arrowEntity == null || !arrowEntity.isValid()) {
                    cancel();
                    return;
                }

                Location loc = arrowEntity.getLocation();

                // Arrow-specific particle effects during rising phase
                if (isRisingPhase) {
                    World world = loc.getWorld();
                    double time = System.currentTimeMillis() * 0.01;

                    // Swirling crit effect for rising arrow
                    for (int i = 0; i < 2; i++) {
                        double angle = time + (i * Math.PI);
                        double radius = 0.2;

                        double x = loc.getX() + radius * Math.cos(angle);
                        double y = loc.getY() + 0.1 * Math.sin(time * 3);
                        double z = loc.getZ() + radius * Math.sin(angle);

                        Location particleLoc = new Location(world, x, y, z);
                        world.spawnParticle(Particle.CRIT, particleLoc, 1, 0, 0, 0, 0.01);
                    }

                    // Subtle sweep attack effect
                    if (Math.random() < 0.3) {
                        world.spawnParticle(Particle.SWEEP_ATTACK, loc, 1, 0, 0, 0, 0.01);
                    }
                }

                // Attack phase particle effects
                if (isAttackPhase) {
                    World world = loc.getWorld();

                    // Enhanced crit trail during attack
                    world.spawnParticle(Particle.CRIT, loc, 2, 0.1, 0.1, 0.1, 0.02);

                    // Occasional sweep attack
                    if (Math.random() < 0.4) {
                        world.spawnParticle(Particle.SWEEP_ATTACK, loc, 1, 0, 0, 0, 0.01);
                    }
                }
            }
        };

        particleTask.runTaskTimer(plugin, 0L, 1L); // Every tick
    }

    /**
     * Update arrow projectile state
     * @return True if projectile is still active
     */
    public boolean update() {
        if (arrowEntity == null || !arrowEntity.isValid()) {
            cleanup();
            return false;
        }

        Location currentLoc = arrowEntity.getLocation();

        if (isRisingPhase) {
            // Check if reached target (boss position)
            if (currentLoc.distance(target) <= 1.0) {
                // Transition to attack phase
                isRisingPhase = false;
                isAttackPhase = true;

                // Will be set to final target when hemisphere pattern is calculated
                return true;
            }
        }

        if (isAttackPhase && finalTarget != null) {
            // Make arrow dangerous during attack phase
            if (arrowEntity != null) {
                arrowEntity.setSilent(false); // Enable sound for attack phase
                arrowEntity.setCritical(true); // Make it critical for visual effect
            }

            // Calculate velocity toward final target
            Vector direction = finalTarget.toVector().subtract(arrowEntity.getLocation().toVector()).normalize();
            double speed = 1.8; // Attack speed (faster than skulls)
            velocity = direction.multiply(speed);
            arrowEntity.setVelocity(velocity);
        }

        return true;
    }

    /**
     * Set final target for attack phase (hemisphere pattern)
     * @param finalTarget Final target position (player location)
     */
    public void setFinalTarget(Location finalTarget) {
        this.finalTarget = finalTarget.clone();

        if (isAttackPhase && arrowEntity != null) {
            // Calculate velocity toward final target
            Vector direction = finalTarget.toVector().subtract(arrowEntity.getLocation().toVector()).normalize();
            double speed = 1.8; // Attack speed (faster than skulls)
            velocity = direction.multiply(speed);
            arrowEntity.setVelocity(velocity);
        }
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }

        if (arrowEntity != null && arrowEntity.isValid()) {
            arrowEntity.remove();
            arrowEntity = null;
        }
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
        if (arrowEntity != null) {
            return arrowEntity.getLocation();
        }
        return origin;
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
     * Get arrow entity
     */
    public Arrow getArrowEntity() {
        return arrowEntity;
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
        return arrowEntity != null && arrowEntity.isValid();
    }
}