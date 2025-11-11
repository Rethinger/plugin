package com.mmmm.story.bosses;

import com.mmmm.story.MmmmStoryPlugin;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

/**
 * Manages Enderman boss clones (decoy entities)
 * Clones inherit boss health but die in one hit with shadow disintegration effects
 */
public class EndermanCloneSystem {

    private final MmmmStoryPlugin plugin;
    private final Map<UUID, EndermanClone> activeClones = new HashMap<>();
    private final Set<UUID> playersWhoKnowRealBoss = new HashSet<>();

    // Task for clone management
    private BukkitTask managementTask;

    // Clone behavior configuration
    private final double cloneHealthPercentage = 1.0; // Clones inherit full boss health
    private final int maxClones = 30; // Maximum simultaneous clones
    private final int cloneDespawnTime = 30000; // 30 seconds lifetime
    private final double cloneTeleportRadius = 12.0; // Clones teleport in smaller radius

    public EndermanCloneSystem(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
        startManagementTask();
    }

    /**
     * Start clone management task
     */
    private void startManagementTask() {
        managementTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateClones();
                cleanupExpiredClones();
            }
        }.runTaskTimer(plugin, 0L, 10L); // Every 0.5 seconds
    }

    /**
     * Create a new clone at specified location
     */
    public EndermanClone createClone(Enderman boss, Location location) {
        if (activeClones.size() >= maxClones) {
            return null; // Too many clones
        }

        World world = location.getWorld();
        Enderman clone = (Enderman) world.spawnEntity(location, EntityType.ENDERMAN);

        // Configure clone attributes
        configureClone(clone, boss);

        // Create clone wrapper
        EndermanClone cloneWrapper = new EndermanClone(clone, boss, location);
        activeClones.put(clone.getUniqueId(), cloneWrapper);

        // Spawn effects
        createSpawnEffects(location);

        return cloneWrapper;
    }

    /**
     * Configure clone attributes based on boss
     */
    private void configureClone(Enderman clone, Enderman boss) {
        // Inherit boss health percentage
        double bossHealthPercentage = boss.getHealth() / boss.getMaxHealth();
        double cloneHealth = boss.getMaxHealth() * cloneHealthPercentage * bossHealthPercentage;

        clone.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(cloneHealth);
        clone.setHealth(cloneHealth);

        // Clone is slightly smaller than boss for visual distinction (no scaling API available)

        // Movement speed slightly slower
        clone.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.30);

        // Attack damage heavily reduced
        clone.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(3.0); // Уменьшен урон в 4 раза с 12.0

        // No special effects (Resistance/Strength are boss-only)
        clone.setPersistent(false);
        clone.setRemoveWhenFarAway(true);

        // Disable drops and experience
        clone.getEquipment().clear();
        clone.setCanPickupItems(false);
        clone.setRemoveWhenFarAway(true);

        // Custom name with black color
        clone.setCustomName("§0Тень Изверга"); // Черный цвет
        clone.setCustomNameVisible(true);
    }

    /**
     * Create spawn visual effects
     */
    private void createSpawnEffects(Location location) {
        World world = location.getWorld();

        // Shadow particles
        world.spawnParticle(Particle.SMOKE, location, 30, 0.5, 0.5, 0.5, 0.05);
        world.spawnParticle(Particle.WITCH, location, 20, 0.3, 0.3, 0.3, 0.1);

        // Sound effect
        world.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);
    }

    /**
     * Handle clone being damaged
     */
    public boolean handleCloneDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Enderman)) {
            return false;
        }

        Enderman damaged = (Enderman) event.getEntity();
        UUID cloneId = damaged.getUniqueId();

        if (!activeClones.containsKey(cloneId)) {
            return false; // Not a managed clone
        }

        EndermanClone clone = activeClones.get(cloneId);

        // Clones die in one hit regardless of damage
        event.setDamage(damaged.getHealth());

        // Prevent drops and experience
        damaged.setCustomNameVisible(false); // Hide name to prevent identification

        // Create death effects immediately
        createDeathEffects(damaged.getLocation());

        // Handle player identification (subtle hint about real boss)
        if (event instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
            if (damager instanceof Player) {
                Player player = (Player) damager;
                // Give subtle hint about real boss
                if (!playersWhoKnowRealBoss.contains(player.getUniqueId())) {
                    playersWhoKnowRealBoss.add(player.getUniqueId());
                    player.sendActionBar(Component.text("§7Эта тень исчезла слишком легко...").color(NamedTextColor.GRAY));
                }
            }
        }

        return true;
    }

    /**
     * Create shadow disintegration death effects
     */
    private void createDeathEffects(Location location) {
        World world = location.getWorld();

        // Shadow disintegration particles
        world.spawnParticle(Particle.SMOKE, location, 50, 1, 2, 1, 0.1);
        world.spawnParticle(Particle.WITCH, location, 40, 0.8, 1.5, 0.8, 0.15);
        world.spawnParticle(Particle.SQUID_INK, location, 30, 0.5, 1, 0.5, 0.05);

        // Sound effects
        world.playSound(location, Sound.ENTITY_ENDERMAN_DEATH, 0.7f, 1.5f);
        world.playSound(location, Sound.ENTITY_WITCH_AMBIENT, 0.5f, 1.8f);
        world.playSound(location, Sound.ENTITY_ITEM_BREAK, 0.8f, 0.5f);

        // Visual effect: shrinking shadow
        new BukkitRunnable() {
            private int ticks = 0;
            private final int maxTicks = 20;

            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    this.cancel();
                    return;
                }

                double progress = (double) ticks / maxTicks;
                double scale = 1.0 - (progress * 0.8); // Shrink to 20% size

                Location effectLoc = location.clone().add(0, scale * 0.5, 0);
                world.spawnParticle(Particle.SMOKE, effectLoc,
                    (int) (10 * (1 - progress)), 0.2 * scale, 0.3 * scale, 0.2 * scale, 0.02);

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Update all active clones
     */
    private void updateClones() {
        Iterator<Map.Entry<UUID, EndermanClone>> iterator = activeClones.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, EndermanClone> entry = iterator.next();
            EndermanClone clone = entry.getValue();

            if (!clone.isValid()) {
                iterator.remove();
                continue;
            }

            // Update clone behavior
            updateCloneBehavior(clone);
        }
    }

    /**
     * Update individual clone behavior
     */
    private void updateCloneBehavior(EndermanClone clone) {
        Enderman cloneEntity = clone.getEntity();
        Enderman boss = clone.getBoss();

        // Target players like the boss
        Player target = findNearestPlayer(cloneEntity.getLocation());
        if (target != null) {
            cloneEntity.setTarget(target);
        }

        // Occasional teleportation (chaotic but less frequent than boss)
        if (Math.random() < 0.02 && clone.canTeleport()) { // 2% chance per tick
            executeCloneTeleport(clone);
        }

        // Remove boss-related glow effects periodically
        if (System.currentTimeMillis() - clone.getCreationTime() > 10000) { // After 10 seconds
            cloneEntity.setGlowing(false);
        }
    }

    /**
     * Execute clone teleportation
     */
    private void executeCloneTeleport(EndermanClone clone) {
        Enderman cloneEntity = clone.getEntity();
        Location currentLoc = cloneEntity.getLocation();

        // Find random teleport location
        Location newLoc = findRandomTeleportLocation(currentLoc, cloneTeleportRadius);
        if (newLoc == null) {
            return;
        }

        // Teleport effects
        World world = cloneEntity.getWorld();
        world.spawnParticle(Particle.PORTAL, currentLoc, 20, 0.3, 0.3, 0.3, 0.1);
        world.playSound(currentLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 0.6f, 1.3f);

        cloneEntity.teleport(newLoc);

        // Arrival effects
        world.spawnParticle(Particle.PORTAL, newLoc, 20, 0.3, 0.3, 0.3, 0.1);
        world.playSound(newLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 0.6f, 1.3f);

        // Update last teleport time
        clone.setLastTeleportTime(System.currentTimeMillis());
    }

    /**
     * Find random teleport location for clone
     */
    private Location findRandomTeleportLocation(Location center, double radius) {
        World world = center.getWorld();

        for (int attempts = 0; attempts < 10; attempts++) {
            double angle = Math.random() * 2 * Math.PI;
            double distance = Math.random() * radius;

            Location candidate = center.clone().add(
                Math.cos(angle) * distance,
                0,
                Math.sin(angle) * distance
            );

            // Find safe Y level
            candidate.setY(world.getHighestBlockYAt(candidate) + 1);

            // Check if location is safe
            if (isSafeLocation(candidate)) {
                return candidate;
            }
        }

        return null;
    }

    /**
     * Check if location is safe for clone
     */
    private boolean isSafeLocation(Location location) {
        // Check if location is solid ground
        if (location.clone().subtract(0, 1, 0).getBlock().getType().isAir()) {
            return false;
        }

        // Check if location has space
        if (!location.getBlock().getType().isAir() ||
            !location.clone().add(0, 1, 0).getBlock().getType().isAir()) {
            return false;
        }

        return true;
    }

    /**
     * Find nearest player to location
     */
    private Player findNearestPlayer(Location location) {
        Player nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Entity entity : location.getNearbyEntities(75, 75, 75)) {
            if (entity instanceof Player) {
                double distance = location.distance(entity.getLocation());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = (Player) entity;
                }
            }
        }

        return nearest;
    }

    /**
     * Clean up expired clones
     */
    private void cleanupExpiredClones() {
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, EndermanClone>> iterator = activeClones.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, EndermanClone> entry = iterator.next();
            EndermanClone clone = entry.getValue();

            if (currentTime - clone.getCreationTime() > cloneDespawnTime) {
                // Despawn clone
                createDespawnEffects(clone.getEntity().getLocation());
                clone.getEntity().remove();
                iterator.remove();
            }
        }
    }

    /**
     * Create despawn effects for expired clones
     */
    private void createDespawnEffects(Location location) {
        World world = location.getWorld();

        // Subtle fade-out particles
        world.spawnParticle(Particle.SMOKE, location, 15, 0.3, 0.3, 0.3, 0.02);
        world.spawnParticle(Particle.WITCH, location, 10, 0.2, 0.2, 0.2, 0.05);

        // Sound effect
        world.playSound(location, Sound.ENTITY_ENDERMAN_AMBIENT, 0.3f, 2.0f);
    }

    /**
     * Get number of active clones
     */
    public int getActiveCloneCount() {
        return activeClones.size();
    }

    /**
     * Get all active clones
     */
    public Collection<EndermanClone> getActiveClones() {
        return new ArrayList<>(activeClones.values());
    }

    /**
     * Check if entity is a managed clone
     */
    public boolean isClone(Entity entity) {
        return activeClones.containsKey(entity.getUniqueId());
    }

    /**
     * Get specific clone by entity
     */
    public EndermanClone getClone(Entity entity) {
        return activeClones.get(entity.getUniqueId());
    }

    /**
     * Clean up all clones
     */
    public void cleanup() {
        // Cancel management task
        if (managementTask != null) {
            managementTask.cancel();
        }

        // Remove all clones
        for (EndermanClone clone : activeClones.values()) {
            createDespawnEffects(clone.getEntity().getLocation());
            clone.getEntity().remove();
        }

        // Clear collections
        activeClones.clear();
        playersWhoKnowRealBoss.clear();
    }

    /**
     * Wrapper class for Enderman clones
     */
    public static class EndermanClone {
        private final Enderman entity;
        private final Enderman boss;
        private final Location spawnLocation;
        private final long creationTime;
        private long lastTeleportTime;

        public EndermanClone(Enderman entity, Enderman boss, Location spawnLocation) {
            this.entity = entity;
            this.boss = boss;
            this.spawnLocation = spawnLocation.clone();
            this.creationTime = System.currentTimeMillis();
            this.lastTeleportTime = 0;
        }

        public Enderman getEntity() {
            return entity;
        }

        public Enderman getBoss() {
            return boss;
        }

        public Location getSpawnLocation() {
            return spawnLocation.clone();
        }

        public long getCreationTime() {
            return creationTime;
        }

        public long getLastTeleportTime() {
            return lastTeleportTime;
        }

        public void setLastTeleportTime(long time) {
            this.lastTeleportTime = time;
        }

        public boolean canTeleport() {
            return System.currentTimeMillis() - lastTeleportTime > 2000; // 2 second cooldown
        }

        public boolean isValid() {
            return entity != null && entity.isValid();
        }

        public long getAge() {
            return System.currentTimeMillis() - creationTime;
        }
    }
}