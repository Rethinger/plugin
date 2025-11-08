package com.mmmm.story.bosses;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Represents a display skull on armor stand that transforms into an attacking projectile.
 * Handles display phase, transformation effects, and attack phase.
 */
public class DisplaySkullProjectile {

    private final UUID id;
    private final Plugin plugin;
    private final Logger logger;

    // Projectile state
    private Location groundOrigin;
    private Location hemispherePosition;
    private Location finalTarget;
    private final Skeleton shooter;

    // Display entities
    private ArmorStand armorStand;
    private WitherSkull attackSkull;

    // State management
    private boolean isValid;
    private boolean isDisplayPhase;
    private boolean isAttackPhase;
    private long creationTime;
    private long transformationTime;

    // Timing configuration
    private static final int DISPLAY_DURATION_TICKS = 60; // 3 seconds display
    private static final int TRANSFORMATION_DURATION_TICKS = 20; // 1 second transformation
    private static final double RISE_SPEED = 0.15; // Rising speed per tick

    public DisplaySkullProjectile(Plugin plugin, Location groundOrigin, Location hemispherePosition, Skeleton shooter) {
        this.id = UUID.randomUUID();
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.groundOrigin = groundOrigin.clone();
        this.hemispherePosition = hemispherePosition.clone();
        this.shooter = shooter;

        this.isValid = false;
        this.isDisplayPhase = false;
        this.isAttackPhase = false;
        this.creationTime = System.currentTimeMillis();

        logger.info("[DisplaySkullProjectile] Created projectile from " +
                   String.format("%.1f,%.1f,%.1f to %.1f,%.1f,%.1f",
                   groundOrigin.getX(), groundOrigin.getY(), groundOrigin.getZ(),
                   hemispherePosition.getX(), hemispherePosition.getY(), hemispherePosition.getZ()));
    }

    /**
     * Spawn the display skull on armor stand
     */
    public boolean spawn(World world) {
        if (world == null) {
            return false;
        }

        try {
            // Create invisible armor stand
            armorStand = (ArmorStand) world.spawnEntity(groundOrigin.clone().add(0, 1.0, 0), EntityType.ARMOR_STAND);

            // Configure armor stand
            armorStand.setInvisible(true);
            armorStand.setInvulnerable(true);
            armorStand.setGravity(false);
            armorStand.setSilent(true);
            armorStand.setMarker(false); // Has hitbox

            // Place wither skull on armor stand head
            ItemStack skullItem = new ItemStack(Material.WITHER_SKELETON_SKULL);
            armorStand.getEquipment().setHelmet(skullItem);

            // Start display phase
            this.isValid = true;
            this.isDisplayPhase = true;

            // Start floating animation
            startDisplayAnimation();

            logger.info("[DisplaySkullProjectile] Successfully spawned display skull");
            return true;

        } catch (Exception e) {
            logger.warning("[DisplaySkullProjectile] Failed to spawn display skull: " + e.getMessage());
            return false;
        }
    }

    /**
     * Start the floating animation during display phase
     */
    private void startDisplayAnimation() {
        new BukkitRunnable() {
            private int ticksElapsed = 0;

            @Override
            public void run() {
                if (!isValid || armorStand == null || !armorStand.isValid()) {
                    cancel();
                    return;
                }

                ticksElapsed++;

                // Calculate floating position
                double progress = (double) ticksElapsed / DISPLAY_DURATION_TICKS;
                Location currentPos = groundOrigin.clone().add(0, 1.0 + (hemispherePosition.getY() - groundOrigin.getY()) * progress, 0);

                // Add gentle floating effect
                float floatOffset = (float) (Math.sin(ticksElapsed * 0.1) * 0.1);
                currentPos.add(0, floatOffset, 0);

                // Update armor stand position
                armorStand.teleport(currentPos);

                // Spawn particles
                if (ticksElapsed % 3 == 0) { // Every 3 ticks
                    spawnDisplayParticles(currentPos);
                }

                // Check if display phase is complete
                if (ticksElapsed >= DISPLAY_DURATION_TICKS) {
                    cancel();
                    startTransformation();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Start transformation into attack projectile
     */
    private void startTransformation() {
        this.transformationTime = System.currentTimeMillis();
        this.isDisplayPhase = false;

        Location transformPos = armorStand.getLocation().clone();

        logger.info("[DisplaySkullProjectile] Starting transformation at " +
                   String.format("%.1f,%.1f,%.1f", transformPos.getX(), transformPos.getY(), transformPos.getZ()));

        // Remove armor stand
        if (armorStand != null && armorStand.isValid()) {
            armorStand.remove();
            armorStand = null;
        }

        // Create transformation effect
        spawnTransformationEffect(transformPos);

        // Create attack skull after transformation delay
        new BukkitRunnable() {
            @Override
            public void run() {
                createAttackSkull(transformPos);
            }
        }.runTaskLater(plugin, TRANSFORMATION_DURATION_TICKS);
    }

    /**
     * Create the actual attacking wither skull
     */
    private void createAttackSkull(Location position) {
        World world = position.getWorld();
        if (world == null || !isValid) {
            return;
        }

        // Spawn wither skull
        attackSkull = (WitherSkull) world.spawnEntity(position, EntityType.WITHER_SKULL);

        // Configure skull
        attackSkull.setCharged(false);
        attackSkull.setDirection(new Vector(0, 1, 0)); // Initially point up

        this.isAttackPhase = true;

        // Start attack sequence
        startAttackSequence();

        logger.info("[DisplaySkullProjectile] Created attack skull");
    }

    /**
     * Start the attack sequence
     */
    private void startAttackSequence() {
        if (attackSkull == null || !attackSkull.isValid()) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isValid || attackSkull == null || !attackSkull.isValid()) {
                    cancel();
                    return;
                }

                // If we have a final target, move towards it
                if (finalTarget != null) {
                    Vector direction = finalTarget.toVector().subtract(attackSkull.getLocation().toVector()).normalize();
                    attackSkull.setDirection(direction);

                    // Apply velocity
                    attackSkull.setVelocity(direction.multiply(0.8));

                    // Spawn trail particles
                    spawnAttackTrailParticles();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Spawn particles during display phase
     */
    private void spawnDisplayParticles(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        // Gentle soul fire particles
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, location, 2, 0.2, 0.2, 0.2, 0.01);
        world.spawnParticle(Particle.END_ROD, location, 1, 0.1, 0.1, 0.1, 0.005);
    }

    /**
     * Spawn transformation effect
     */
    private void spawnTransformationEffect(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        // Explosive transformation effect
        world.spawnParticle(Particle.EXPLOSION, location, 10, 0.5, 0.5, 0.5, 0.1);
        world.spawnParticle(Particle.SMOKE, location, 15, 0.8, 0.8, 0.8, 0.15);
        world.spawnParticle(Particle.DRAGON_BREATH, location, 8, 0.4, 0.4, 0.4, 0.08);

        // Play sound
        world.playSound(location, org.bukkit.Sound.ENTITY_WITHER_SHOOT, 1.0f, 0.8f);
    }

    /**
     * Spawn trail particles during attack
     */
    private void spawnAttackTrailParticles() {
        if (attackSkull == null) return;

        World world = attackSkull.getWorld();
        if (world == null) return;

        Location loc = attackSkull.getLocation();
        world.spawnParticle(Particle.DRAGON_BREATH, loc, 1, 0.1, 0.1, 0.1, 0.02);
    }

    /**
     * Set the final target for the attack
     */
    public void setFinalTarget(Location target) {
        this.finalTarget = target.clone();

        // DEBUG: Enhanced logging with distance calculation
        logger.info(String.format("[DEBUG-SKULL] Set final target: (%.2f, %.2f, %.2f)", 
            target.getX(), target.getY(), target.getZ()));
        if (groundOrigin != null) {
            logger.info(String.format("[DEBUG-SKULL] Skull origin: (%.2f, %.2f, %.2f) | distance_to_target=%.2f",
                groundOrigin.getX(), groundOrigin.getY(), groundOrigin.getZ(),
                groundOrigin.distance(target)));
        }

        if (isAttackPhase && attackSkull != null && attackSkull.isValid()) {
            logger.info("[DisplaySkullProjectile] Set final target for attack");
        }
    }

    /**
     * Get current location
     */
    public Location getLocation() {
        if (isDisplayPhase && armorStand != null && armorStand.isValid()) {
            return armorStand.getLocation();
        } else if (isAttackPhase && attackSkull != null && attackSkull.isValid()) {
            return attackSkull.getLocation();
        }
        return groundOrigin;
    }

    /**
     * Check if projectile is valid
     */
    public boolean isValid() {
        if (isDisplayPhase && armorStand != null) {
            return armorStand.isValid();
        } else if (isAttackPhase && attackSkull != null) {
            return attackSkull.isValid();
        }
        return isValid;
    }

    /**
     * Check if display phase is complete
     */
    public boolean isDisplayComplete() {
        return !isDisplayPhase;
    }

    /**
     * Check if in attack phase
     */
    public boolean isInAttackPhase() {
        return isAttackPhase;
    }

    /**
     * Get projectile ID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Clean up entities
     */
    public void cleanup() {
        isValid = false;
        isDisplayPhase = false;
        isAttackPhase = false;

        if (armorStand != null && armorStand.isValid()) {
            armorStand.remove();
            armorStand = null;
        }

        if (attackSkull != null && attackSkull.isValid()) {
            attackSkull.remove();
            attackSkull = null;
        }

        logger.info("[DisplaySkullProjectile] Cleaned up projectile " + id.toString().substring(0, 8));
    }
}