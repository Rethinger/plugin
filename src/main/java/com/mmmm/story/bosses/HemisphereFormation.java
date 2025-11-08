package com.mmmm.story.bosses;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Skeleton;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages the formation of a hemisphere of wither skulls that rise from the ground.
 * Handles position calculation, skull spawning, and rising animation.
 */
public class HemisphereFormation {

    private final Plugin plugin;
    private final Logger logger;
    private final Location center;
    private final double radius;
    private final int skullCount;
    private final Skeleton shooter;
    private final SpecialAttackConfiguration config;

    // Formation state
    private final List<Location> skullPositions;
    private final List<DisplaySkullProjectile> skulls;
    private boolean isForming;
    private boolean isComplete;
    private long formationStartTime;
    private BukkitRunnable formationTask;

    // Formation timing - now read from config
    private final int formationDurationTicks;

    /**
     * Create a new hemisphere formation
     * @param plugin Plugin instance
     * @param center Center location for the hemisphere (boss position)
     * @param radius Radius of the hemisphere
     * @param skullCount Number of skulls in the hemisphere
     * @param shooter Boss entity that is shooting the skulls
     * @param config Special attack configuration for geometry constraints
     */
    public HemisphereFormation(Plugin plugin, Location center, double radius, int skullCount, 
                              Skeleton shooter, SpecialAttackConfiguration config) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.center = center.clone();
        this.config = config;
        
        // Use config values for radius with variance
        double baseRadius = config.getHemisphereRadius();
        double variance = config.getHemisphereRadiusVariance();
        double minRadius = baseRadius - variance;
        double maxRadius = baseRadius + variance;
        this.radius = minRadius + (Math.random() * (maxRadius - minRadius));
        
        // Use config skull count
        this.skullCount = config.getHemisphereSkullCount();
        this.shooter = shooter;
        this.formationDurationTicks = config.getHemisphereFormationDurationTicks();

        this.skullPositions = calculateSkullPositions();
        this.skulls = new ArrayList<>();
        this.isForming = false;
        this.isComplete = false;
        this.formationStartTime = 0;
        this.formationTask = null;
        
        logger.info(String.format("[HemisphereFormation] Created with radius=%.2f skulls=%d duration_ticks=%d",
            this.radius, this.skullCount, this.formationDurationTicks));
    }
    
    /**
     * Create a new hemisphere formation (legacy constructor for compatibility)
     * @param plugin Plugin instance
     * @param center Center location for the hemisphere (boss position)
     * @param radius Radius of the hemisphere (will be overridden by config)
     * @param skullCount Number of skulls in the hemisphere (will be overridden by config)
     * @param shooter Boss entity that is shooting the skulls
     */
    @Deprecated
    public HemisphereFormation(Plugin plugin, Location center, double radius, int skullCount, Skeleton shooter) {
        this(plugin, center, radius, skullCount, shooter, new SpecialAttackConfiguration());
    }

    /**
     * Start the hemisphere formation process
     */
    public void startFormation() {
        if (isForming || isComplete) {
            logger.warning("[HemisphereFormation] Formation already started or completed");
            return;
        }

        isForming = true;
        formationStartTime = System.currentTimeMillis();

        logger.info("[HemisphereFormation] Starting hemisphere formation with " + skullCount + " skulls");

        formationTask = new BukkitRunnable() {
            private int ticksElapsed = 0;

            @Override
            public void run() {
                ticksElapsed++;

                // Spawn skulls gradually over the formation duration
                int skullsToSpawn = (skullCount * ticksElapsed) / formationDurationTicks;
                int skullsAlreadySpawned = skulls.size();

                // Spawn new skulls if needed
                while (skullsAlreadySpawned < skullsToSpawn && skullsAlreadySpawned < skullPositions.size()) {
                    spawnSkullAtPosition(skullPositions.get(skullsAlreadySpawned));
                    skullsAlreadySpawned++;
                }

                // Update formation progress
                updateFormation();

                // Check if formation is complete
                if (ticksElapsed >= formationDurationTicks) {
                    completeFormation();
                    cancel();
                }
            }
        };

        formationTask.runTaskTimer(plugin, 0L, 1L); // Every tick
    }

    /**
     * Calculate hemisphere positions for skulls
     * Uses config-defined polar angle constraints to ensure no skull goes straight up
     * @return List of positions forming a hemisphere
     */
    private List<Location> calculateSkullPositions() {
        List<Location> positions = new ArrayList<>();
        World world = center.getWorld();
        
        // Get polar angle constraints from config (in degrees)
        double minPolarDegrees = config.getHemisphereMinPolarAngleDegrees();
        double maxPolarDegrees = config.getHemisphereMaxPolarAngleDegrees();
        double minPolarRad = Math.toRadians(minPolarDegrees);
        double maxPolarRad = Math.toRadians(maxPolarDegrees);

        // Use Fibonacci sphere algorithm for even distribution
        double goldenRatio = (1 + Math.sqrt(5)) / 2;
        double angleIncrement = Math.PI * 2 * goldenRatio;

        for (int i = 0; i < skullCount; i++) {
            // Map i uniformly to the constrained polar angle range
            double t = (double) i / (skullCount - 1); // Normalized [0, 1]
            double inclination = minPolarRad + t * (maxPolarRad - minPolarRad);
            
            // Azimuth (φ) - full 360° rotation
            double azimuth = angleIncrement * i;

            // Calculate 3D position using spherical coordinates
            // x = r * sin(θ) * cos(φ)
            // y = r * cos(θ)
            // z = r * sin(θ) * sin(φ)
            double x = center.getX() + radius * Math.sin(inclination) * Math.cos(azimuth);
            double y = center.getY() + radius * Math.cos(inclination); // Height component
            double z = center.getZ() + radius * Math.sin(inclination) * Math.sin(azimuth);

            Location position = new Location(world, x, y, z);
            positions.add(position);
        }

        logger.info(String.format("[HemisphereFormation] Calculated %d positions radius=%.2f theta=[%.1f°, %.1f°]",
            positions.size(), radius, minPolarDegrees, maxPolarDegrees));
        return positions;
    }

    /**
     * Spawn a single skull at a ground position and start rising animation
     * @param hemispherePosition Target position in the hemisphere
     */
    private void spawnSkullAtPosition(Location hemispherePosition) {
        World world = hemispherePosition.getWorld();
        if (world == null) {
            return;
        }

        // Find ground position directly below the hemisphere position
        Location groundOrigin = findGroundPosition(hemispherePosition);
        if (groundOrigin == null) {
            logger.warning("[HemisphereFormation] Could not find ground position for skull");
            return;
        }

        // Create display skull projectile
        DisplaySkullProjectile skull = new DisplaySkullProjectile(
            plugin, groundOrigin, hemispherePosition, shooter
        );

        // Spawn the display skull
        if (skull.spawn(world)) {
            skulls.add(skull);

            // Spawn rising particles at ground position
            spawnRisingParticles(groundOrigin);

            logger.info("[HemisphereFormation] Spawned display skull at ground position " +
                       String.format("%.1f, %.1f, %.1f", groundOrigin.getX(), groundOrigin.getY(), groundOrigin.getZ()));
        } else {
            logger.warning("[HemisphereFormation] Failed to spawn display skull entity");
        }
    }

    /**
     * Find a safe ground position below the hemisphere position
     * @param targetPosition Target hemisphere position
     * @return Ground position for skull spawning, or null if not found
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
     * Spawn particle effects when skull rises from ground
     * @param location Ground location where skull appears
     */
    private void spawnRisingParticles(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }

        // Dust explosion effect at ground
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, location, 10, 0.5, 0.2, 0.5, 0.1);
        world.spawnParticle(Particle.END_ROD, location, 8, 0.3, 0.3, 0.3, 0.05);

        // Ground crack effect
        for (int i = 0; i < 6; i++) {
            double angle = (i / 6.0) * 2 * Math.PI;
            double x = location.getX() + 0.8 * Math.cos(angle);
            double z = location.getZ() + 0.8 * Math.sin(angle);
            Location crackLoc = new Location(world, x, location.getY(), z);
            world.spawnParticle(Particle.CRIT, crackLoc, 2, 0.1, 0.1, 0.1, 0.02);
        }

        // Sound effect
        world.playSound(location, org.bukkit.Sound.ENTITY_WITHER_SHOOT, 0.7f, 1.2f);
    }

    /**
     * Update formation progress and skull positions
     */
    public void updateFormation() {
        // Remove invalid skulls
        skulls.removeIf(skull -> !skull.isValid());

        // Display skulls are self-updating through their internal tasks

        // Spawn formation progress particles
        if (System.currentTimeMillis() % 5 == 0) { // Every 5 ticks to reduce particle spam
            spawnFormationParticles();
        }
    }

    /**
     * Spawn particle effects showing formation progress
     */
    private void spawnFormationParticles() {
        World world = center.getWorld();
        if (world == null || skulls.size() >= skullCount) {
            return;
        }

        // Show connecting lines between forming skulls
        for (int i = 0; i < skulls.size(); i++) {
            for (int j = i + 1; j < skulls.size(); j++) {
                Location pos1 = skulls.get(i).getLocation();
                Location pos2 = skulls.get(j).getLocation();

                if (pos1.distance(pos2) < radius * 0.7) { // Only connect nearby skulls
                    // Spawn particles along the line
                    int particles = (int) (pos1.distance(pos2) * 2);
                    for (int p = 0; p <= particles; p++) {
                        double t = (double) p / particles;
                        Location linePos = pos1.clone().add(pos2.clone().subtract(pos1).multiply(t));
                        world.spawnParticle(Particle.END_ROD, linePos, 1, 0, 0, 0, 0.01);
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

        logger.info("[HemisphereFormation] Formation completed with " + skulls.size() + " display skulls");

        // Play completion sound effect
        World world = center.getWorld();
        if (world != null) {
            world.playSound(center, org.bukkit.Sound.ENTITY_WITHER_AMBIENT, 1.0f, 0.8f);
            world.spawnParticle(Particle.GLOW, center, 20, 1, 1, 1, 0.2);
        }

        formationTask = null;
    }

    /**
     * Launch all skulls toward target locations
     * @param targetLocations List of target locations for skulls to attack
     */
    public void launchSkullsAtTargets(List<Location> targetLocations) {
        if (!isComplete) {
            logger.warning("[HemisphereFormation] Cannot launch skulls - formation not complete");
            return;
        }

        if (targetLocations.isEmpty()) {
            logger.warning("[HemisphereFormation] No target locations provided for skull launch");
            return;
        }

        // DEBUG: Log launch details
        logger.info(String.format("[DEBUG-LAUNCH] launchSkullsAtTargets: %d skulls, %d targets", 
            skulls.size(), targetLocations.size()));
        logger.info(String.format("[DEBUG-LAUNCH] Hemisphere center: %.2f, %.2f, %.2f",
            center.getX(), center.getY(), center.getZ()));

        logger.info("[HemisphereFormation] Launching " + skulls.size() + " display skulls at " + targetLocations.size() + " targets");

        // Assign targets to skulls
        for (int i = 0; i < skulls.size(); i++) {
            DisplaySkullProjectile skull = skulls.get(i);
            if (!skull.isValid()) {
                continue;
            }

            // Cycle through targets if more skulls than targets
            Location target = targetLocations.get(i % targetLocations.size());
            skull.setFinalTarget(target);

            // DEBUG: Log skull assignment
            Location skullLoc = skull.getLocation();
            logger.info(String.format("[DEBUG-LAUNCH] Skull[%d] at (%.2f, %.2f, %.2f) -> Target (%.2f, %.2f, %.2f) | dist=%.2f",
                i, skullLoc.getX(), skullLoc.getY(), skullLoc.getZ(),
                target.getX(), target.getY(), target.getZ(),
                skullLoc.distance(target)));

            // Spawn launch effect
            World world = skullLoc.getWorld();
            if (world != null) {
                world.spawnParticle(Particle.DRAGON_BREATH, skullLoc, 5, 0.2, 0.2, 0.2, 0.1);
                world.playSound(skullLoc, org.bukkit.Sound.ENTITY_WITHER_SHOOT, 1.2f, 1.0f);
            }
        }
    }

    /**
     * Check if formation is complete
     * @return True if formation is complete
     */
    public boolean isComplete() {
        return isComplete;
    }

    /**
     * Check if all skulls have completed their transformation to attack phase
     * @return True if all skulls are in attack phase
     */
    public boolean areAllSkullsInAttackPhase() {
        if (!isComplete || skulls.isEmpty()) {
            return false;
        }

        return skulls.stream().allMatch(DisplaySkullProjectile::isInAttackPhase);
    }

    /**
     * Check if formation is currently in progress
     * @return True if formation is in progress
     */
    public boolean isForming() {
        return isForming;
    }

    /**
     * Get the number of successfully spawned skulls
     * @return Number of valid skulls
     */
    public int getValidSkullCount() {
        return (int) skulls.stream().mapToInt(skull -> skull.isValid() ? 1 : 0).sum();
    }

    /**
     * Get the total number of skulls that should be in the formation
     * @return Total skull count
     */
    public int getSkullCount() {
        return skullCount;
    }

    /**
     * Get all skull projectiles in the formation
     * @return List of skull projectiles
     */
    public List<DisplaySkullProjectile> getSkulls() {
        return new ArrayList<>(skulls);
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
        long expectedDuration = formationDurationTicks * 50; // Convert ticks to milliseconds
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

        // Remove all display skull entities
        for (DisplaySkullProjectile skull : skulls) {
            skull.cleanup();
        }
        skulls.clear();

        isForming = false;
        isComplete = false;

        logger.info("[HemisphereFormation] Cleanup completed");
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