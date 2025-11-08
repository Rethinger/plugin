package com.mmmm.story.bosses;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Manages the formation of a hemisphere of arrows that rise from the ground.
 * Arrows deal exactly 1 damage (2 HP) and ignore armor/enchantments.
 * Used for phase 2 of the enhanced boss special attack.
 */
public class ArrowHemisphereFormation implements Listener {

    private final Plugin plugin;
    private final Logger logger;
    private final Location center;
    private final double radius;
    private final int arrowCount;
    private final Skeleton shooter;

    // Formation state
    private final List<Location> arrowPositions;
    private final List<ArrowProjectile> arrows;
    private boolean isForming;
    private boolean isComplete;
    private long formationStartTime;
    private BukkitRunnable formationTask;

    // Formation timing (3 seconds = 60 ticks, same as skull formation)
    private static final int FORMATION_DURATION_TICKS = 60;

    /**
     * Create a new arrow hemisphere formation
     * @param plugin Plugin instance
     * @param center Center location for the hemisphere (boss position)
     * @param radius Radius of the hemisphere
     * @param arrowCount Number of arrows in the hemisphere
     * @param shooter Boss entity that is shooting the arrows
     */
    public ArrowHemisphereFormation(Plugin plugin, Location center, double radius, int arrowCount, Skeleton shooter) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.center = center.clone();
        this.radius = Math.max(5.0, Math.min(15.0, radius)); // Clamp between 5-15 blocks
        this.arrowCount = Math.max(12, Math.min(16, arrowCount)); // Clamp between 12-16 arrows
        this.shooter = shooter;

        this.arrowPositions = calculateArrowPositions();
        this.arrows = new ArrayList<>();
        this.isForming = false;
        this.isComplete = false;
        this.formationStartTime = 0;
        this.formationTask = null;

        // Register event listener for arrow damage handling
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Start the arrow hemisphere formation process
     */
    public void startFormation() {
        if (isForming || isComplete) {
            logger.warning("[ArrowHemisphereFormation] Formation already started or completed");
            return;
        }

        isForming = true;
        formationStartTime = System.currentTimeMillis();

        logger.info("[ArrowHemisphereFormation] Starting arrow hemisphere formation with " + arrowCount + " arrows");

        formationTask = new BukkitRunnable() {
            private int ticksElapsed = 0;

            @Override
            public void run() {
                ticksElapsed++;

                // Spawn arrows gradually over the formation duration
                int arrowsToSpawn = (arrowCount * ticksElapsed) / FORMATION_DURATION_TICKS;
                int arrowsAlreadySpawned = arrows.size();

                // Spawn new arrows if needed
                while (arrowsAlreadySpawned < arrowsToSpawn && arrowsAlreadySpawned < arrowPositions.size()) {
                    spawnArrowAtPosition(arrowPositions.get(arrowsAlreadySpawned));
                    arrowsAlreadySpawned++;
                }

                // Update formation progress
                updateFormation();

                // Check if formation is complete
                if (ticksElapsed >= FORMATION_DURATION_TICKS) {
                    completeFormation();
                    cancel();
                }
            }
        };

        formationTask.runTaskTimer(plugin, 0L, 1L); // Every tick
    }

    /**
     * Calculate hemisphere positions for arrows (same algorithm as skulls)
     * @return List of positions forming a hemisphere
     */
    private List<Location> calculateArrowPositions() {
        List<Location> positions = new ArrayList<>();
        World world = center.getWorld();

        // Use Fibonacci sphere algorithm for even distribution
        double goldenRatio = (1 + Math.sqrt(5)) / 2;
        double angleIncrement = Math.PI * 2 * goldenRatio;

        for (int i = 0; i < arrowCount; i++) {
            double t = (double) i / arrowCount;
            double inclination = Math.acos(1 - 2 * t);

            // Only use upper hemisphere (inclination from 0 to PI/2)
            if (inclination > Math.PI / 2) {
                inclination = Math.PI - inclination; // Mirror to upper hemisphere
            }

            double azimuth = angleIncrement * i;

            // Calculate 3D position
            double x = center.getX() + radius * Math.sin(inclination) * Math.cos(azimuth);
            double y = center.getY() + radius * Math.cos(inclination);
            double z = center.getZ() + radius * Math.sin(inclination) * Math.sin(azimuth);

            Location position = new Location(world, x, y, z);
            positions.add(position);
        }

        logger.info("[ArrowHemisphereFormation] Calculated " + positions.size() + " hemisphere positions");
        return positions;
    }

    /**
     * Spawn a single arrow at a ground position and start rising animation
     * @param hemispherePosition Target position in the hemisphere
     */
    private void spawnArrowAtPosition(Location hemispherePosition) {
        World world = hemispherePosition.getWorld();
        if (world == null) {
            return;
        }

        // Find ground position directly below the hemisphere position
        Location groundOrigin = findGroundPosition(hemispherePosition);
        if (groundOrigin == null) {
            logger.warning("[ArrowHemisphereFormation] Could not find ground position for arrow");
            return;
        }

        // Create arrow projectile with ground rising logic
        ArrowProjectile arrow = new ArrowProjectile(plugin, groundOrigin, hemispherePosition, shooter);

        // Spawn the arrow entity
        if (arrow.spawn(world)) {
            arrows.add(arrow);

            // Spawn rising particles at ground position (different from skull effect)
            spawnArrowRisingParticles(groundOrigin);

            logger.info("[ArrowHemisphereFormation] Spawned arrow at ground position " +
                       String.format("%.1f, %.1f, %.1f", groundOrigin.getX(), groundOrigin.getY(), groundOrigin.getZ()));
        } else {
            logger.warning("[ArrowHemisphereFormation] Failed to spawn arrow entity");
        }
    }

    /**
     * Find a safe ground position below the hemisphere position
     * @param targetPosition Target hemisphere position
     * @return Ground position for arrow spawning, or null if not found
     */
    private Location findGroundPosition(Location targetPosition) {
        World world = targetPosition.getWorld();
        if (world == null) {
            return null;
        }

        // Start from target position and scan downward to find ground
        Location checkPos = targetPosition.clone();

        for (int y = 0; y < 20; y++) { // Check up to 20 blocks down
            checkPos.setY(targetPosition.getY() - y);

            if (checkPos.getBlock().getType().isSolid()) {
                // Found solid ground, spawn slightly above it
                return checkPos.clone().add(0, 1.0, 0);
            }
        }

        // Fallback: use target position at ground level
        Location fallback = targetPosition.clone();
        fallback.setY(world.getHighestBlockYAt(targetPosition) + 2);
        return fallback;
    }

    /**
     * Spawn particle effects when arrow rises from ground (different from skull effect)
     * @param location Ground location where arrow appears
     */
    private void spawnArrowRisingParticles(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }

        // Arrow-specific rising particles (more subtle than skulls)
        world.spawnParticle(Particle.CRIT, location, 8, 0.4, 0.2, 0.4, 0.05);
        world.spawnParticle(Particle.SWEEP_ATTACK, location, 3, 0.2, 0.2, 0.2, 0.02);

        // Ground impact effect for arrow appearance
        for (int i = 0; i < 4; i++) {
            double angle = (i / 4.0) * 2 * Math.PI;
            double x = location.getX() + 0.6 * Math.cos(angle);
            double z = location.getZ() + 0.6 * Math.sin(angle);
            Location impactLoc = new Location(world, x, location.getY(), z);
            world.spawnParticle(Particle.BLOCK, impactLoc, 1, 0.1, 0.1, 0.1, 0,
                               Material.DIRT.createBlockData());
        }

        // Sound effect (quieter than skull)
        world.playSound(location, org.bukkit.Sound.ENTITY_ARROW_SHOOT, 0.5f, 1.3f);
    }

    /**
     * Update formation progress and arrow positions
     */
    public void updateFormation() {
        // Remove invalid arrows
        arrows.removeIf(arrow -> !arrow.isValid());

        // Update all arrows
        for (ArrowProjectile arrow : arrows) {
            arrow.update();
        }

        // Spawn formation progress particles (different from skull formation)
        if (System.currentTimeMillis() % 8 == 0) { // Every 8 ticks to reduce particle spam
            spawnArrowFormationParticles();
        }
    }

    /**
     * Spawn particle effects showing arrow formation progress
     */
    private void spawnArrowFormationParticles() {
        World world = center.getWorld();
        if (world == null || arrows.size() >= arrowCount) {
            return;
        }

        // Show subtle connecting lines between forming arrows
        for (int i = 0; i < arrows.size(); i++) {
            for (int j = i + 1; j < arrows.size(); j++) {
                Location pos1 = arrows.get(i).getLocation();
                Location pos2 = arrows.get(j).getLocation();

                if (pos1.distance(pos2) < radius * 0.7) { // Only connect nearby arrows
                    // Spawn particles along the line (more subtle than skull lines)
                    int particles = (int) (pos1.distance(pos2) * 1.5);
                    for (int p = 0; p <= particles; p++) {
                        double t = (double) p / particles;
                        Location linePos = pos1.clone().add(pos2.clone().subtract(pos1).multiply(t));
                        world.spawnParticle(Particle.CRIT, linePos, 1, 0, 0, 0, 0.005);
                    }
                }
            }
        }
    }

    /**
     * Complete the formation process
     */
    private void completeFormation() {
        isForming = false;
        isComplete = true;

        logger.info("[ArrowHemisphereFormation] Formation completed with " + arrows.size() + " arrows");

        // Play completion sound effect (different from skull completion)
        World world = center.getWorld();
        if (world != null) {
            world.playSound(center, org.bukkit.Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.7f);
            world.spawnParticle(Particle.CRIT, center, 15, 1, 1, 1, 0.1);
        }

        formationTask = null;
    }

    /**
     * Launch all arrows toward target locations
     * @param targetLocations List of target locations for arrows to attack
     */
    public void launchArrowsAtTargets(List<Location> targetLocations) {
        if (!isComplete) {
            logger.warning("[ArrowHemisphereFormation] Cannot launch arrows - formation not complete");
            return;
        }

        if (targetLocations.isEmpty()) {
            logger.warning("[ArrowHemisphereFormation] No target locations provided for arrow launch");
            return;
        }

        logger.info("[ArrowHemisphereFormation] Launching " + arrows.size() + " arrows at " + targetLocations.size() + " targets");

        // Assign targets to arrows
        for (int i = 0; i < arrows.size(); i++) {
            ArrowProjectile arrow = arrows.get(i);
            if (!arrow.isValid()) {
                continue;
            }

            // Cycle through targets if more arrows than targets
            Location target = targetLocations.get(i % targetLocations.size());
            arrow.setFinalTarget(target);

            // Spawn launch effect (different from skull launch)
            Location arrowLoc = arrow.getLocation();
            World world = arrowLoc.getWorld();
            if (world != null) {
                world.spawnParticle(Particle.SWEEP_ATTACK, arrowLoc, 3, 0.2, 0.2, 0.2, 0.05);
                world.playSound(arrowLoc, org.bukkit.Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.5f);
            }
        }
    }

    /**
     * Handle arrow hit events to apply instant damage
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow)) {
            return;
        }

        Arrow arrow = (Arrow) event.getEntity();

        // Check if this is one of our special arrows
        ArrowProjectile projectile = findProjectileByEntity(arrow);
        if (projectile == null) {
            return; // Not our arrow
        }

        // Apply instant damage to hit entity (if any)
        if (event.getHitEntity() instanceof Player) {
            Player player = (Player) event.getHitEntity();
            applyInstantArrowDamage(player, projectile);
        }

        // Clean up the projectile
        projectile.cleanup();
        arrows.remove(projectile);
    }

    /**
     * Handle direct arrow damage to ensure instant damage is applied
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArrowDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Arrow)) {
            return;
        }

        Player player = (Player) event.getEntity();
        Arrow arrow = (Arrow) event.getDamager();

        // Check if this is one of our special arrows
        ArrowProjectile projectile = findProjectileByEntity(arrow);
        if (projectile == null) {
            return; // Not our arrow
        }

        // Cancel the normal damage and apply our instant damage
        event.setCancelled(true);
        applyInstantArrowDamage(player, projectile);
    }

    /**
     * Apply instant damage from arrow (exactly 1 damage, ignoring armor)
     * @param player Player to damage
     * @param projectile Arrow projectile
     */
    private void applyInstantArrowDamage(Player player, ArrowProjectile projectile) {
        // Apply exactly 1 damage (2 HP) that ignores armor and enchantments
        player.damage(1.0);

        // Spawn hit effect
        Location hitLoc = player.getLocation();
        World world = hitLoc.getWorld();
        if (world != null) {
            world.spawnParticle(Particle.CRIT, hitLoc, 5, 0.3, 0.3, 0.3, 0.1);
            world.spawnParticle(Particle.BLOCK, hitLoc, 3, 0.2, 0.2, 0.2, 0,
                               Material.REDSTONE_BLOCK.createBlockData());
            world.playSound(hitLoc, org.bukkit.Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.8f);
        }

        logger.info("[ArrowHemisphereFormation] Applied instant 1 damage to " + player.getName());
    }

    /**
     * Find arrow projectile by arrow entity
     * @param arrow Arrow entity to find
     * @return ArrowProjectile or null if not found
     */
    private ArrowProjectile findProjectileByEntity(Arrow arrow) {
        for (ArrowProjectile projectile : arrows) {
            if (projectile.getArrowEntity().equals(arrow)) {
                return projectile;
            }
        }
        return null;
    }

    /**
     * Check if formation is complete
     * @return True if formation is complete
     */
    public boolean isComplete() {
        return isComplete;
    }

    /**
     * Check if formation is currently in progress
     * @return True if formation is in progress
     */
    public boolean isForming() {
        return isForming;
    }

    /**
     * Get the number of successfully spawned arrows
     * @return Number of valid arrows
     */
    public int getValidArrowCount() {
        return (int) arrows.stream().mapToInt(arrow -> arrow.isValid() ? 1 : 0).sum();
    }

    /**
     * Get the total number of arrows that should be in the formation
     * @return Total arrow count
     */
    public int getArrowCount() {
        return arrowCount;
    }

    /**
     * Get all arrow projectiles in the formation
     * @return List of arrow projectiles
     */
    public List<ArrowProjectile> getArrows() {
        return new ArrayList<>(arrows);
    }

    /**
     * Get formation progress (0.0 to 1.0)
     * @return Formation progress percentage
     */
    public double getFormationProgress() {
        if (isComplete) {
            return 1.0;
        }

        if (formationStartTime <= 0) {
            return 0.0;
        }

        long elapsed = System.currentTimeMillis() - formationStartTime;
        long expectedDuration = FORMATION_DURATION_TICKS * 50; // Convert ticks to milliseconds
        return Math.min(1.0, (double) elapsed / expectedDuration);
    }

    /**
     * Clean up resources and cancel tasks
     */
    public void cleanup() {
        if (formationTask != null) {
            formationTask.cancel();
            formationTask = null;
        }

        // Remove all arrow entities
        for (ArrowProjectile arrow : arrows) {
            arrow.cleanup();
        }
        arrows.clear();

        isForming = false;
        isComplete = false;

        logger.info("[ArrowHemisphereFormation] Cleanup completed");
    }

    /**
     * Get center location of the hemisphere
     * @return Center location
     */
    public Location getCenter() {
        return center.clone();
    }

    /**
     * Get radius of the hemisphere
     * @return Hemisphere radius
     */
    public double getRadius() {
        return radius;
    }
}