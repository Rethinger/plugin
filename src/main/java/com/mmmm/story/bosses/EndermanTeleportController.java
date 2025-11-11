package com.mmmm.story.bosses;

import com.mmmm.story.MmmmStoryPlugin;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manages Enderman boss teleportation mechanics
 * Features: chaotic teleportation, counter-attack teleports, queued system
 */
public class EndermanTeleportController {

    private final MmmmStoryPlugin plugin;

    // Teleport queue to prevent conflicts
    private final Queue<TeleportRequest> teleportQueue = new ConcurrentLinkedQueue<>();
    private boolean isProcessingTeleports = false;

    // Teleport configuration
    private final double defaultTeleportRadius = 20.0;
    private final double counterTeleportDistance = 4.0;
    private final int maxTeleportAttempts = 20;
    private final long teleportCooldown = 500; // 0.5 seconds between teleports

    // Performance optimization
    private final Map<UUID, Long> lastTeleportTime = new HashMap<>();

    public EndermanTeleportController(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Queue a teleport request for processing
     */
    public void queueTeleport(Enderman entity, Location destination, TeleportType type, Runnable onComplete) {
        TeleportRequest request = new TeleportRequest(entity, destination, type, onComplete);
        teleportQueue.offer(request);

        // Start processing if not already running
        if (!isProcessingTeleports) {
            processNextTeleport();
        }
    }

    /**
     * Execute counter-attack teleportation behind player
     */
    public void executeCounterTeleport(Enderman boss, Player target) {
        // Calculate position behind player
        Location behindPlayer = calculateBehindPlayerLocation(target, counterTeleportDistance);

        // Add teleport effects
        Location currentLoc = boss.getLocation();
        createTeleportEffects(currentLoc, behindPlayer, TeleportType.COUNTER);

        // Execute teleport
        boss.teleport(behindPlayer);

        // Update last teleport time
        updateLastTeleportTime(boss.getUniqueId());

        // Post-teleport effects
        createPostTeleportEffects(behindPlayer, TeleportType.COUNTER);
    }

    /**
     * Calculate location behind player
     */
    private Location calculateBehindPlayerLocation(Player player, double distance) {
        Location playerLoc = player.getLocation();
        Vector playerDirection = playerLoc.getDirection().multiply(-1).normalize(); // Opposite direction

        Location behindPlayer = playerLoc.clone().add(playerDirection.multiply(distance));
        behindPlayer.setY(playerLoc.getY()); // Same Y level as player

        // Ensure safe location
        return findSafeTeleportLocation(behindPlayer, 2.0);
    }

    /**
     * Find random teleport location within radius
     */
    public Location findRandomTeleportLocation(Location center, double radius) {
        World world = center.getWorld();

        for (int attempts = 0; attempts < maxTeleportAttempts; attempts++) {
            // Random angle and distance
            double angle = Math.random() * 2 * Math.PI;
            double distance = Math.random() * radius;

            Location candidate = center.clone().add(
                Math.cos(angle) * distance,
                0,
                Math.sin(angle) * distance
            );

            // Find appropriate Y level
            candidate = findSafeYLocation(candidate);

            // Validate location
            if (isSafeTeleportLocation(candidate)) {
                return candidate;
            }
        }

        // Fallback: return slightly modified original location
        return center.clone().add(
            (Math.random() - 0.5) * 4,
            0,
            (Math.random() - 0.5) * 4
        );
    }

    /**
     * Find safe Y location for teleportation
     */
    private Location findSafeYLocation(Location location) {
        World world = location.getWorld();
        Location groundLoc = location.clone();

        // Find ground level (increase search range to 30 blocks)
        for (int y = Math.min(location.getBlockY(), world.getMaxHeight() - 5); y > Math.max(location.getBlockY() - 30, world.getMinHeight()); y--) {
            groundLoc.setY(y);
            if (!groundLoc.getBlock().getType().isAir() &&
                groundLoc.clone().add(0, 1, 0).getBlock().getType().isAir()) {
                // Found solid ground with air above
                return groundLoc.clone().add(0, 1, 0);
            }
        }

        // If no ground found below, try above
        for (int y = Math.max(location.getBlockY(), world.getMinHeight() + 5); y < Math.min(location.getBlockY() + 20, world.getMaxHeight()); y++) {
            groundLoc.setY(y);
            if (!groundLoc.getBlock().getType().isAir() &&
                groundLoc.clone().add(0, 1, 0).getBlock().getType().isAir()) {
                return groundLoc.clone().add(0, 1, 0);
            }
        }

        // Ultimate fallback: use a safe location near spawn
        return location.getWorld().getSpawnLocation().add(0, 3, 0);
    }

    /**
     * Check if location is safe for teleportation
     */
    private boolean isSafeTeleportLocation(Location location) {
        // Check if location has air space
        if (!location.getBlock().getType().isAir()) {
            return false;
        }

        if (!location.clone().add(0, 1, 0).getBlock().getType().isAir()) {
            return false;
        }

        // Check if not in water or lava
        Material groundType = location.clone().subtract(0, 1, 0).getBlock().getType();
        if (groundType == Material.WATER || groundType == Material.LAVA ||
            groundType == Material.MAGMA_BLOCK || groundType == Material.FIRE) {
            return false;
        }

        // Check for dangerous blocks nearby
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    Material block = location.clone().add(dx, dy, dz).getBlock().getType();
                    if (block == Material.LAVA || block == Material.FIRE ||
                        block == Material.MAGMA_BLOCK || block.name().contains("WALL") ||
                        block.name().contains("FENCE") || block.name().contains("DOOR") ||
                        block.name().contains("GATE")) {
                        return false;
                    }
                }
            }
        }

        // Check distance from players (avoid teleporting too close)
        for (Entity entity : location.getNearbyEntities(8, 8, 8)) {
            if (entity instanceof Player) {
                return false;
            }
        }

        return true;
    }

    /**
     * Find safe teleport location near target
     */
    private Location findSafeTeleportLocation(Location target, double radius) {
        for (int attempts = 0; attempts < 10; attempts++) {
            double angle = Math.random() * 2 * Math.PI;
            double distance = Math.random() * radius;

            Location candidate = target.clone().add(
                Math.cos(angle) * distance,
                0,
                Math.sin(angle) * distance
            );

            candidate = findSafeYLocation(candidate);

            if (isSafeTeleportLocation(candidate)) {
                return candidate;
            }
        }

        return target; // Fallback
    }

    /**
     * Process next teleport in queue
     */
    private void processNextTeleport() {
        TeleportRequest request = teleportQueue.poll();
        if (request == null) {
            isProcessingTeleports = false;
            return;
        }

        isProcessingTeleports = true;

        // Check cooldown
        UUID entityId = request.entity.getUniqueId();
        if (isOnCooldown(entityId)) {
            // Re-queue for later
            teleportQueue.offer(request);
            isProcessingTeleports = false;
            return;
        }

        // Execute teleport with delay for effect
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                executeTeleport(request);

                // Process next teleport
                processNextTeleport();
            }
        }.runTaskLater(plugin, 5L); // 0.25 second delay
    }

    /**
     * Execute teleport request
     */
    private void executeTeleport(TeleportRequest request) {
        Enderman entity = request.entity;
        Location from = entity.getLocation();
        Location to = request.destination;

        // Validate entity and destination
        if (!entity.isValid()) {
            return;
        }

        if (!isSafeTeleportLocation(to)) {
            // Find alternative safe location
            to = findSafeTeleportLocation(to, 3.0);
            if (to == null) {
                return; // No safe location found
            }
        }

        // Create teleport effects
        createTeleportEffects(from, to, request.type);

        // Execute teleport
        entity.teleport(to);

        // Update cooldown
        updateLastTeleportTime(entity.getUniqueId());

        // Post-teleport effects
        createPostTeleportEffects(to, request.type);

        // Execute completion callback
        if (request.onComplete != null) {
            request.onComplete.run();
        }
    }

    /**
     * Create teleport visual effects
     */
    private void createTeleportEffects(Location from, Location to, TeleportType type) {
        World world = from.getWorld();

        // Departure effects
        createDepartureEffects(from, type);

        // Arrival effects (will be created after teleport)
        // Note: These are created in executeTeleport after the actual teleport
    }

    /**
     * Create departure effects
     */
    private void createDepartureEffects(Location location, TeleportType type) {
        World world = location.getWorld();

        switch (type) {
            case CHAOTIC:
                // Intense chaotic particles
                world.spawnParticle(Particle.PORTAL, location, 40, 1, 2, 1, 0.3);
                world.spawnParticle(Particle.DRAGON_BREATH, location, 25, 0.8, 1.5, 0.8, 0.1);
                world.spawnParticle(Particle.END_ROD, location, 30, 0.5, 1, 0.5, 0.2);
                world.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.2f, 0.8f);
                world.playSound(location, Sound.BLOCK_END_PORTAL_FRAME_FILL, 0.8f, 1.5f);
                break;

            case COUNTER:
                // Quick counter-attack particles
                world.spawnParticle(Particle.PORTAL, location, 20, 0.5, 1, 0.5, 0.2);
                world.spawnParticle(Particle.SMOKE, location, 15, 0.3, 0.8, 0.3, 0.05);
                world.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
                break;

            case NORMAL:
            default:
                // Standard teleport effects
                world.spawnParticle(Particle.PORTAL, location, 25, 0.5, 1, 0.5, 0.2);
                world.spawnParticle(Particle.END_ROD, location, 15, 0.3, 0.8, 0.3, 0.1);
                world.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.0f);
                break;
        }
    }

    /**
     * Create post-teleport effects
     */
    private void createPostTeleportEffects(Location location, TeleportType type) {
        World world = location.getWorld();

        switch (type) {
            case CHAOTIC:
                // Dramatic arrival
                world.spawnParticle(Particle.EXPLOSION, location, 15, 1, 1, 1, 0.1);
                world.spawnParticle(Particle.PORTAL, location, 50, 1.5, 2.5, 1.5, 0.4);
                world.playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 2.0f);
                world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.5f);
                break;

            case COUNTER:
                // Quick, sharp arrival
                world.spawnParticle(Particle.CRIT, location, 20, 0.8, 1, 0.8, 0.2);
                world.spawnParticle(Particle.SWEEP_ATTACK, location, 10, 0.5, 0.5, 0.5, 0.1);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.2f);
                break;

            case NORMAL:
            default:
                // Standard arrival
                world.spawnParticle(Particle.PORTAL, location, 30, 0.8, 1.5, 0.8, 0.3);
                world.spawnParticle(Particle.END_ROD, location, 20, 0.4, 1, 0.4, 0.15);
                world.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.0f);
                break;
        }
    }

    /**
     * Check if entity is on cooldown
     */
    private boolean isOnCooldown(UUID entityId) {
        Long lastTime = lastTeleportTime.get(entityId);
        if (lastTime == null) {
            return false;
        }

        return (System.currentTimeMillis() - lastTime) < teleportCooldown;
    }

    /**
     * Update last teleport time for entity
     */
    private void updateLastTeleportTime(UUID entityId) {
        lastTeleportTime.put(entityId, System.currentTimeMillis());
    }

    /**
     * Process teleport queue (called from main boss task)
     */
    public void processTeleportQueue() {
        // This method is called regularly to ensure queue processing continues
        if (!isProcessingTeleports && !teleportQueue.isEmpty()) {
            processNextTeleport();
        }

        // Cleanup old cooldowns
        cleanupOldCooldowns();
    }

    /**
     * Clean up old cooldown entries
     */
    private void cleanupOldCooldowns() {
        long currentTime = System.currentTimeMillis();
        lastTeleportTime.entrySet().removeIf(entry ->
            currentTime - entry.getValue() > 5000 // Remove entries older than 5 seconds
        );
    }

    /**
     * Get queue size
     */
    public int getQueueSize() {
        return teleportQueue.size();
    }

    /**
     * Clear all pending teleports
     */
    public void clearQueue() {
        teleportQueue.clear();
    }

    /**
     * Clean up controller
     */
    public void cleanup() {
        clearQueue();
        lastTeleportTime.clear();
        isProcessingTeleports = false;
    }

    /**
     * Teleport types for different visual/audio effects
     */
    public enum TeleportType {
        NORMAL,     // Standard teleport
        CHAOTIC,    // Chaotic teleportation with clones
        COUNTER     // Counter-attack teleport behind player
    }

    /**
     * Teleport request data structure
     */
    private static class TeleportRequest {
        final Enderman entity;
        final Location destination;
        final TeleportType type;
        final Runnable onComplete;
        final long timestamp;

        TeleportRequest(Enderman entity, Location destination, TeleportType type, Runnable onComplete) {
            this.entity = entity;
            this.destination = destination;
            this.type = type;
            this.onComplete = onComplete;
            this.timestamp = System.currentTimeMillis();
        }
    }
}