package com.mmmm.story.bosses;

import com.mmmm.story.managers.SafeZoneManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages the stationary casting boss attack with danger zone visualization and evoker fangs.
 * Boss remains stationary while casting, then attacks with evoker fangs across the area.
 */
public class StationaryCastingManager {

    private final Plugin plugin;
    private final Logger logger;
    private final Skeleton boss;
    private final SafeZoneManager safeZoneManager;

    // State management
    private boolean isActive;
    private Location bossPosition;
    private int castingProgress;
    private BukkitRunnable castingTask;
    private BukkitRunnable fangsTask;
    private AttackCompletionCallback completionCallback;

    // Configuration constants
    private static final int CASTING_DURATION_TICKS = 60; // 3 seconds
    private static final double ATTACK_RADIUS = 15.0; // Back to 15 blocks for performance
    private static final double SAFE_ZONE_RADIUS = 1.5;
    private static final int MAX_SAFE_ZONES = 5;
    private static final int FANGS_PER_WAVE = 8;

    // Damage constants
    private static final double DAMAGE_HEARTS = 4.0; // 4 hearts damage
    private static final double DAMAGE_AMOUNT = DAMAGE_HEARTS * 2.0; // 4 hearts = 8 damage points

    // Particle colors
    private static final DustOptions RED_DUST = new DustOptions(org.bukkit.Color.fromRGB(255, 50, 50), 1.5f);
    private static final DustOptions WHITE_DUST = new DustOptions(org.bukkit.Color.fromRGB(255, 255, 255), 2.0f);

    /**
     * Create a new stationary casting manager
     * @param plugin Plugin instance
     * @param boss Boss entity
     * @param safeZoneManager Safe zone manager for integration
     */
    public StationaryCastingManager(Plugin plugin, Skeleton boss, SafeZoneManager safeZoneManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.boss = boss;
        this.safeZoneManager = safeZoneManager;

        this.isActive = false;
        this.castingProgress = 0;
    }

    /**
     * Set completion callback for attack notifications
     * @param callback Callback to notify when attack completes
     */
    public void setCompletionCallback(AttackCompletionCallback callback) {
        this.completionCallback = callback;
    }

    /**
     * Start the stationary casting attack sequence
     * @return True if attack started successfully
     */
    public boolean startCastingAttack() {
        if (isActive || boss == null || !boss.isValid()) {
            logger.warning("[StationaryCastingManager] Cannot start attack - boss invalid or already active");
            return false;
        }

        isActive = true;
        bossPosition = boss.getLocation().clone();
        castingProgress = 0;

        logger.info("[StationaryCastingManager] Starting stationary casting attack");

        // Freeze boss movement during casting
        freezeBoss();

        // Start the main casting task
        startCastingTask();

        return true;
    }

    /**
     * Start the main casting task
     */
    private void startCastingTask() {
        castingTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive || boss == null || !boss.isValid()) {
                    stopCastingAttack(false);
                    return;
                }

                castingProgress++;

                // Update casting effects based on progress
                updateCastingEffects();

                // Create safe zones at specific intervals
                if (castingProgress == 20) { // After 1 second
                    createSafeZones();
                }

                // Complete casting and start fangs attack
                if (castingProgress >= CASTING_DURATION_TICKS) {
                    startFangsAttack();
                    cancel();
                }
            }
        };

        castingTask.runTaskTimer(plugin, 0L, 1L); // Every tick
    }

    /**
     * Update casting effects based on progress
     */
    private void updateCastingEffects() {
        Location bossLoc = boss.getLocation();
        World world = bossLoc.getWorld();
        if (world == null) return;

        // Boss animation (swaying)
        if (castingProgress % 10 == 0) {
            double swayAngle = Math.sin(castingProgress * 0.1) * Math.toRadians(15);
            Vector currentLook = bossLoc.getDirection();
            Vector swayed = rotateAroundY(currentLook, swayAngle);

            Location lookTarget = bossLoc.clone().add(swayed.multiply(10));
            boss.lookAt(lookTarget);
        }

        // Particle effects based on casting phase
        if (castingProgress <= 15) {
            // Phase 1: Initial charge (red particles)
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, bossLoc, 8, 0.5, 0.8, 0.5, 0.1);
        } else if (castingProgress <= 45) {
            // Phase 2: Active casting (intense effects)
            world.spawnParticle(Particle.DRAGON_BREATH, bossLoc, 15, 0.8, 0.8, 0.8, 0.15);
            world.spawnParticle(Particle.END_ROD, bossLoc, 10, 1.0, 1.0, 1.0, 0.1);
        } else {
            // Phase 3: Final preparation (white flash)
            world.spawnParticle(Particle.FLASH, bossLoc, 1, 0, 0, 0, 0);
            world.spawnParticle(Particle.GLOW, bossLoc, 20, 1.5, 1.5, 1.5, 0.2);
        }

        // Visualize danger zone with red particles
        visualizeDangerZone();

        // Sound effects
        if (castingProgress % 10 == 0) {
            float pitch = 0.5f + (castingProgress / (float) CASTING_DURATION_TICKS) * 1.0f;
            world.playSound(bossLoc, org.bukkit.Sound.ENTITY_EVOKER_PREPARE_SUMMON, 1.0f, pitch);
        }
    }

    /**
     * Visualize the danger zone with red particles
     */
    private void visualizeDangerZone() {
        World world = bossPosition.getWorld();
        if (world == null) return;

        double pulse = Math.sin(castingProgress * 0.2) * 0.5 + 0.5;

        // Create concentric rings
        for (double radius = ATTACK_RADIUS; radius > 0; radius -= 2.0) {
            int particles = (int) (2 * Math.PI * radius);
            for (int i = 0; i < particles; i++) {
                double angle = (i / (double) particles) * 2 * Math.PI;
                double x = bossPosition.getX() + radius * Math.cos(angle);
                double z = bossPosition.getZ() + radius * Math.sin(angle);

                // Find ground level for particle location
                double groundY = findGroundLevel(world, x, z, bossPosition.getY());
                Location particleLoc = new Location(world, x, groundY + 0.1, z); // Just above ground

                // Skip if location is in safe zone
                if (safeZoneManager != null && safeZoneManager.isInSafeZone(particleLoc)) {
                    continue;
                }

                // Spawn red particle with pulsing intensity
                if (Math.random() < pulse) {
                    world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, RED_DUST);
                }
            }
        }

        // Visualize safe zones with white circles
        visualizeSafeZones();
    }

    /**
     * Visualize safe zones with white circles
     */
    private void visualizeSafeZones() {
        if (safeZoneManager == null) return;

        World world = bossPosition.getWorld();
        if (world == null) return;

        // Get active safe zones
        List<com.mmmm.story.bosses.SafeZone> activeZones = safeZoneManager.getActiveSafeZones();

        for (com.mmmm.story.bosses.SafeZone zone : activeZones) {
            Location center = zone.getCenter();
            double radius = zone.getRadius();

            // Find ground level for safe zone center
            double groundY = findGroundLevel(world, center.getX(), center.getZ(), center.getY());
            center.setY(groundY);

            // Create white circle for safe zone
            int particles = (int) (2 * Math.PI * radius * 3); // 3 particles per block of circumference
            for (int i = 0; i < particles; i++) {
                double angle = (i / (double) particles) * 2 * Math.PI;
                double x = center.getX() + radius * Math.cos(angle);
                double z = center.getZ() + radius * Math.sin(angle);

                Location particleLoc = new Location(world, x, groundY + 0.1, z);

                // Spawn white particle
                world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, WHITE_DUST);
            }

            // Add some glow particles in the center
            for (int i = 0; i < 5; i++) {
                world.spawnParticle(Particle.GLOW, center, 1, radius * 0.5, 0.2, radius * 0.5, 0.1);
            }
        }
    }

    /**
     * Create safe zones based on player count
     */
    private void createSafeZones() {
        int playerCount = getNearbyPlayerCount();
        int safeZoneCount = Math.min(playerCount + 1, MAX_SAFE_ZONES);

        logger.info("[StationaryCastingManager] Creating " + safeZoneCount + " safe zones for " + playerCount + " players");
        logger.info("[StationaryCastingManager] Attack radius: " + ATTACK_RADIUS + ", Safe zone radius: " + SAFE_ZONE_RADIUS);

        // Create safe zones using hemisphere method (better for sequential appearance)
        List<com.mmmm.story.bosses.SafeZone> createdZones = safeZoneManager.generateHemisphereSafeZones(
            bossPosition, ATTACK_RADIUS, SAFE_ZONE_RADIUS, 30, playerCount, true, 10);

        logger.info("[StationaryCastingManager] Created " + createdZones.size() + " safe zones with sequential appearance");

        // Debug: log safe zone positions
        for (int i = 0; i < createdZones.size(); i++) {
            com.mmmm.story.bosses.SafeZone zone = createdZones.get(i);
            Location center = zone.getCenter();
            logger.info("[StationaryCastingManager] Safe zone " + (i+1) + " at " +
                String.format("(%.1f, %.1f, %.1f)", center.getX(), center.getY(), center.getZ()));
        }
    }

    /**
     * Calculate safe zone positions based on player count
     */
    private List<Location> calculateSafeZonePositions(int count) {
        List<Location> positions = new ArrayList<>();
        World world = bossPosition.getWorld();
        if (world == null) return positions;

        // Always create one safe zone near the boss
        Location nearBoss = bossPosition.clone().add(3, 0, 0);
        positions.add(nearBoss);

        // Create additional zones distributed around the circle
        for (int i = 1; i < count; i++) {
            double angle = (i / (double) count) * 2 * Math.PI;
            double distance = ATTACK_RADIUS * 0.7; // 70% of attack radius
            double x = bossPosition.getX() + distance * Math.cos(angle);
            double z = bossPosition.getZ() + distance * Math.sin(angle);

            Location pos = new Location(world, x, bossPosition.getY(), z);
            positions.add(pos);
        }

        return positions;
    }

    /**
     * Start the evoker fangs attack
     */
    private void startFangsAttack() {
        logger.info("[StationaryCastingManager] Starting evoker fangs attack");

        // Calculate fang positions
        List<Location> fangPositions = calculateFangPositions();

        logger.info("[StationaryCastingManager] Calculated " + fangPositions.size() + " fang positions");

        // Spawn triple fang attack instantly (3 waves at once)
        spawnTripleFangAttack(fangPositions);

        // Schedule attack completion
        fangsTask = new BukkitRunnable() {
            @Override
            public void run() {
                stopCastingAttack(true);
            }
        };
        fangsTask.runTaskLater(plugin, 60L); // 3 seconds for fangs attack
    }

    /**
     * Calculate positions for evoker fangs - spawn on every block in danger zone
     */
    private List<Location> calculateFangPositions() {
        List<Location> positions = new ArrayList<>();
        World world = bossPosition.getWorld();
        if (world == null) return positions;

        // Calculate ground level for the boss position
        double bossGroundY = findGroundLevel(world, bossPosition.getX(), bossPosition.getZ(), bossPosition.getY());

        // Spawn fangs on every block within the attack radius
        for (int x = (int) -ATTACK_RADIUS; x <= ATTACK_RADIUS; x++) {
            for (int z = (int) -ATTACK_RADIUS; z <= ATTACK_RADIUS; z++) {
                double offsetX = x;
                double offsetZ = z;

                Location fangLoc = bossPosition.clone().add(offsetX, 0, offsetZ);

                // Check if position is within attack radius (circular boundary)
                if (fangLoc.distance(bossPosition) <= ATTACK_RADIUS) {
                    // Find ground level for this specific position
                    double groundY = findGroundLevel(world, fangLoc.getX(), fangLoc.getZ(), bossGroundY);
                    fangLoc.setY(groundY);

                    // Check if not in safe zone
                    if (safeZoneManager == null || !safeZoneManager.isInSafeZone(fangLoc)) {
                        positions.add(fangLoc);
                    }
                }
            }
        }

        logger.info("[StationaryCastingManager] Calculated " + positions.size() + " fang positions covering every block");
        return positions;
    }

    /**
     * Spawn triple fang attack with staggered waves for performance
     */
    private void spawnTripleFangAttack(List<Location> positions) {
        if (positions.isEmpty()) return;

        logger.info("[StationaryCastingManager] Starting staggered triple fang attack - " + positions.size() + " fangs per wave");

        // Calculate positions for each of the 3 waves to distribute load
        int totalPositions = positions.size();
        int waveSize = Math.max(50, totalPositions / 3); // At least 50 fangs per wave for better performance

        // Wave 1 - immediate
        int endIdx1 = Math.min(waveSize, totalPositions);
        List<Location> wave1Positions = positions.subList(0, endIdx1);
        spawnFangs(wave1Positions, 1.0f);

        // Wave 2 - after 5 ticks (0.25 seconds) to reduce performance impact
        new BukkitRunnable() {
            @Override
            public void run() {
                int startIdx = waveSize;
                int endIdx = Math.min(startIdx + waveSize, totalPositions);
                if (startIdx < totalPositions) {
                    List<Location> wave2Positions = positions.subList(startIdx, endIdx);
                    spawnFangs(wave2Positions, 1.1f);
                }
            }
        }.runTaskLater(plugin, 5L);

        // Wave 3 - after 10 ticks (0.5 seconds)
        new BukkitRunnable() {
            @Override
            public void run() {
                int startIdx = waveSize * 2;
                if (startIdx < totalPositions) {
                    List<Location> wave3Positions = positions.subList(startIdx, totalPositions);
                    spawnFangs(wave3Positions, 1.2f);
                }
            }
        }.runTaskLater(plugin, 10L);

        logger.info("[StationaryCastingManager] Staggered triple fang attack initiated - " + totalPositions + " total fangs across 3 waves");
    }

    /**
     * Spawn evoker fangs at specified positions with custom damage
     */
    private void spawnFangs(List<Location> positions, float soundPitch) {
        World world = bossPosition.getWorld();
        if (world == null) return;

        for (Location pos : positions) {
            EvokerFangs fang = (EvokerFangs) world.spawnEntity(pos, EntityType.EVOKER_FANGS);

            // Set custom damage for the fangs
            setFangDamage(fang);

            // Play spawn sound with varying pitch
            world.playSound(pos, org.bukkit.Sound.ENTITY_EVOKER_FANGS_ATTACK, 0.8f, soundPitch);

            // Add spawn particles
            world.spawnParticle(Particle.CRIT, pos, 5, 0.2, 0.2, 0.2, 0.1);
        }

        logger.info("[StationaryCastingManager] Spawned " + positions.size() + " evoker fangs with " + DAMAGE_HEARTS + " hearts damage (pitch: " + soundPitch + ")");
    }

    /**
     * Set custom damage for evoker fangs that bypasses armor and protects skeleton warriors
     */
    private void setFangDamage(EvokerFangs fang) {
        // Schedule damage application shortly after fang spawns
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!fang.isValid()) return;

                Location fangLoc = fang.getLocation();
                World world = fangLoc.getWorld();
                if (world == null) return;

                // Find entities near the fang (1.5 block radius)
                for (org.bukkit.entity.Entity entity : world.getNearbyEntities(fangLoc, 1.5, 1.5, 1.5)) {
                    if (!(entity instanceof LivingEntity)) continue;

                    // Skip skeleton warriors (any skeleton-type mob)
                    if (entity instanceof Skeleton ||
                        (entity instanceof Mob && isWarriorSkeleton((Mob) entity))) {
                        continue;
                    }

                    // Apply custom damage to players (ignores armor)
                    if (entity instanceof Player) {
                        Player player = (Player) entity;

                        // Check if player is in safe zone
                        if (safeZoneManager != null && safeZoneManager.isInSafeZone(player.getLocation())) {
                            continue; // No damage in safe zones
                        }

                        // Apply damage that bypasses armor
                        boolean success = applyCustomDamage(player);
                        if (success) {
                            logger.info("[StationaryCastingManager] Applied " + DAMAGE_HEARTS + " hearts damage to player " + player.getName());
                        }
                    }
                }
            }
        }.runTaskLater(plugin, 5L); // Apply damage 5 ticks after spawn
    }

    /**
     * Apply custom damage that bypasses armor
     */
    private boolean applyCustomDamage(Player player) {
        if (!player.isValid() || player.isDead()) return false;

        // Store original health
        double originalHealth = player.getHealth();

        // Apply damage using EntityDamageEvent with IGNORE_ARMOR and IGNORE_INVULNERABILITY for custom calculation
        EntityDamageEvent damageEvent = new EntityDamageEvent(
            player,
            EntityDamageEvent.DamageCause.MAGIC,
            DAMAGE_AMOUNT
        );

        // Call the damage event
        if (!damageEvent.isCancelled()) {
            // Apply damage directly (bypasses armor)
            double newHealth = Math.max(0, originalHealth - DAMAGE_AMOUNT);
            player.setHealth(newHealth);

            // Send damage animation
            player.playEffect(org.bukkit.EntityEffect.HURT);

            // Play hurt sound
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);

            return true;
        }

        return false;
    }

    /**
     * Check if a mob is a warrior skeleton (custom skeleton used by the boss system)
     */
    private boolean isWarriorSkeleton(Mob mob) {
        // Check for specific characteristics of warrior skeletons
        // This could be based on custom name, tags, or other properties
        String customName = mob.getCustomName();
        if (customName != null) {
            return customName.toLowerCase().contains("warrior") ||
                   customName.toLowerCase().contains("скелет-воин") ||
                   customName.toLowerCase().contains("skeleton warrior");
        }

        // Additional check: if the skeleton has specific equipment or metadata
        if (mob instanceof Skeleton) {
            Skeleton skeleton = (Skeleton) mob;
            // You can add more specific checks here based on your warrior skeleton implementation
            // For example, specific armor, weapons, or metadata
        }

        return false;
    }

    /**
     * Count nearby players within attack range
     */
    private int getNearbyPlayerCount() {
        if (boss == null) return 0;

        int count = 0;
        World world = boss.getWorld();
        if (world == null) return count;

        for (Player player : world.getPlayers()) {
            if (player.getLocation().distance(boss.getLocation()) <= ATTACK_RADIUS + 5.0) {
                count++;
            }
        }
        return count;
    }

    /**
     * Stop the casting attack
     * @param completed Whether attack completed successfully
     */
    public void stopCastingAttack(boolean completed) {
        if (!isActive) {
            return;
        }

        isActive = false;

        // Cancel tasks
        if (castingTask != null) {
            castingTask.cancel();
            castingTask = null;
        }
        if (fangsTask != null) {
            fangsTask.cancel();
            fangsTask = null;
        }

        // Clean up safe zones
        if (safeZoneManager != null) {
            safeZoneManager.cleanup();
        }

        // Unfreeze boss movement
        unfreezeBoss();

        logger.info("[StationaryCastingManager] Casting attack stopped, completed=" + completed);

        // Notify callback of attack completion
        if (completionCallback != null) {
            completionCallback.onAttackCompleted(completed);
        }
    }

    /**
     * Check if casting attack is currently active
     * @return True if attack is active
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Get current casting progress (0-60 ticks)
     * @return Current progress
     */
    public int getCastingProgress() {
        return castingProgress;
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        stopCastingAttack(false);
    }

    /**
     * Freeze boss movement during casting
     */
    private void freezeBoss() {
        if (boss == null || !boss.isValid()) return;

        // Make boss invulnerable and unable to move
        boss.setInvulnerable(true);
        boss.setAI(false);

        // Store original location to keep boss in place
        boss.teleport(bossPosition);

        logger.info("[StationaryCastingManager] Boss frozen during casting");
    }

    /**
     * Unfreeze boss movement after casting
     */
    private void unfreezeBoss() {
        if (boss == null || !boss.isValid()) return;

        // Restore boss movement and vulnerability
        boss.setInvulnerable(false);
        boss.setAI(true);

        logger.info("[StationaryCastingManager] Boss unfrozen after casting");
    }

    /**
     * Find ground level at specific x,z coordinates
     */
    private double findGroundLevel(World world, double x, double z, double startY) {
        // Search downward from start position to find ground
        for (int y = (int) startY; y >= world.getMinHeight(); y--) {
            if (world.getBlockAt((int) x, y, (int) z).getType().isSolid()) {
                return y + 1; // Return position above solid block
            }
        }

        // Search upward if no ground found below
        for (int y = (int) startY; y <= world.getMaxHeight(); y++) {
            if (world.getBlockAt((int) x, y, (int) z).getType().isSolid()) {
                return y + 1;
            }
        }

        return startY; // Fallback to starting position
    }

    /**
     * Helper method to rotate vector around Y axis
     */
    private Vector rotateAroundY(Vector vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = vector.getX() * cos - vector.getZ() * sin;
        double z = vector.getX() * sin + vector.getZ() * cos;
        return new Vector(x, vector.getY(), z);
    }
}