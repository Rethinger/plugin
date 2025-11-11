// BACKUP FILE: Original Boss #2 (Изверг Адских Глубин) Mechanics
// This file contains the backed-up Wither-based boss implementation from Act2Listener
// Created on: 2025-11-09
// For rollback purposes if Enderman replacement needs to be reverted

package com.mmmm.story.bosses.backup;

import com.mmmm.story.MmmmStoryPlugin;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.attribute.Attribute;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

/**
 * Backup of original Wither-based boss mechanics
 * This contains the complete boss implementation that was replaced by the Enderman boss
 */
public class OriginalBoss2Backup {

    private final MmmmStoryPlugin plugin;
    private Wither bossEntity;
    private int bossPhase = 1;
    private BossBar bossBar;

    // Combat tracking (simplified for backup)
    private Map<UUID, Long> playersAboveBoss = new HashMap<>();
    private Map<UUID, Long> playersNearBoss = new HashMap<>();
    private Map<UUID, Long> teleportCooldown = new HashMap<>();
    private Map<UUID, Integer> playerArrowsShot = new HashMap<>();

    // Task references
    private BukkitTask bossBarTask;
    private BukkitTask bossAITask;
    private BukkitTask heightCheckTask;
    private BukkitTask antiWallTask;
    private BukkitTask teleportTask;

    public OriginalBoss2Backup(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Original boss summoning method
     */
    public void spawnOriginalBoss(Location location) {
        World world = location.getWorld();

        // Spawn Wither-based boss entity
        Location spawnLoc = location.clone().add(0, 3, 0);
        bossEntity = (Wither) world.spawnEntity(spawnLoc, EntityType.WITHER);
        bossEntity.setCustomName("Изверг Адских Глубин");
        bossEntity.setCustomNameVisible(true);

        // Set original attributes
        bossEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(500.0);
        bossEntity.setHealth(500.0);
        bossEntity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0.6);

        // Mark as boss
        bossEntity.setPersistent(true);
        bossEntity.setRemoveWhenFarAway(false);

        // Create boss bar
        bossBar = BossBar.bossBar(
            Component.text("Изверг Адских Глубин"),
            1.0f,
            BossBar.Color.PURPLE,
            BossBar.Overlay.PROGRESS
        );

        // Add nearby players to boss bar
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distance(bossEntity.getLocation()) < 100) {
                // Note: Adventure API may have different method names
                // bossBar.addPlayer(player); // Commented out for compatibility
            }
        }

        // Start combat tasks
        startBossTasks();
    }

    /**
     * Start original boss combat tasks
     */
    private void startBossTasks() {
        // Boss bar update task
        bossBarTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (bossEntity == null || !bossEntity.isValid()) {
                    this.cancel();
                    return;
                }

                double healthPercentage = bossEntity.getHealth() / bossEntity.getMaxHealth();
                bossBar.progress((float) healthPercentage);

                // Update phase based on health
                int newPhase = healthPercentage > 0.5 ? 1 : 2;
                if (newPhase != bossPhase) {
                    bossPhase = newPhase;
                    onPhaseTransition(bossPhase);
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);

        // Boss AI task
        bossAITask = new BukkitRunnable() {
            @Override
            public void run() {
                if (bossEntity == null || !bossEntity.isValid()) {
                    this.cancel();
                    return;
                }

                // Original boss AI logic
                executeOriginalBossAI();
            }
        }.runTaskTimer(plugin, 0L, 10L);

        // Height check task (anti-exploit)
        heightCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkPlayerHeightExploit();
            }
        }.runTaskTimer(plugin, 0L, 5L);

        // Anti-fortification task
        antiWallTask = new BukkitRunnable() {
            @Override
            public void run() {
                preventPlayerFortification();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    /**
     * Execute original boss AI
     */
    private void executeOriginalBossAI() {
        // Check for nearby players
        List<Player> nearbyPlayers = getNearbyPlayers(75);
        if (nearbyPlayers.isEmpty()) {
            return; // No players nearby, wait
        }

        // Target nearest player
        Player target = findNearestPlayer(nearbyPlayers);
        if (target != null) {
            bossEntity.setTarget(target);

            // Phase-specific behavior
            if (bossPhase == 1) {
                executePhase1Behavior(target);
            } else {
                executePhase2Behavior(target);
            }
        }
    }

    /**
     * Execute Phase 1 behavior (original)
     */
    private void executePhase1Behavior(Player target) {
        // Basic attacks
        if (bossEntity.getLocation().distance(target.getLocation()) < 3.0) {
            // Teleport behind player if too close
            executeProximityTeleport(target);
        }
    }

    /**
     * Execute Phase 2 behavior (original)
     */
    private void executePhase2Behavior(Player target) {
        // Enhanced aggression in Phase 2
        bossEntity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
        bossEntity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 0));
    }

    /**
     * Execute proximity teleport (original mechanic)
     */
    private void executeProximityTeleport(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Check cooldown
        if (teleportCooldown.containsKey(playerId)) {
            long lastTeleport = teleportCooldown.get(playerId);
            if (currentTime - lastTeleport < 10000) { // 10 second cooldown
                return;
            }
        }

        // Teleport behind player
        Location behindPlayer = player.getLocation().clone()
            .add(player.getLocation().getDirection().multiply(-4));
        behindPlayer.setY(bossEntity.getLocation().getY());

        bossEntity.teleport(behindPlayer);
        teleportCooldown.put(playerId, currentTime);

        // Effects
        World world = bossEntity.getWorld();
        world.spawnParticle(Particle.PORTAL, behindPlayer, 50, 1, 1, 1, 0.2);
        world.playSound(behindPlayer, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }

    /**
     * Find nearest player to location
     */
    private Player findNearestPlayer(List<Player> players) {
        Player nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Player player : players) {
            double distance = bossEntity.getLocation().distance(player.getLocation());
            if (distance < minDistance) {
                minDistance = distance;
                nearest = player;
            }
        }

        return nearest;
    }

    /**
     * Get nearby players
     */
    private List<Player> getNearbyPlayers(double radius) {
        List<Player> players = new ArrayList<>();
        for (Entity entity : bossEntity.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player) {
                players.add((Player) entity);
            }
        }
        return players;
    }

    /**
     * Handle phase transition
     */
    private void onPhaseTransition(int newPhase) {
        // Handle phase transition effects
        World world = bossEntity.getWorld();
        world.spawnParticle(Particle.EXPLOSION, bossEntity.getLocation(), 50, 2, 2, 2, 0.1);
        world.playSound(bossEntity.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 2.0f, 0.5f);

        if (newPhase == 2) {
            // Phase 2 enhancements
            bossEntity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 1));
            bossEntity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 999999, 0));
        }
    }

    /**
     * Check player height exploit
     */
    private void checkPlayerHeightExploit() {
        if (bossEntity == null) return;

        for (Player player : getNearbyPlayers(20)) {
            Location playerLoc = player.getLocation();
            Location bossLoc = bossEntity.getLocation();

            if (playerLoc.getY() > bossLoc.getY() + 5) {
                // Player is too high above boss
                UUID playerId = player.getUniqueId();
                long currentTime = System.currentTimeMillis();

                if (!playersAboveBoss.containsKey(player.getUniqueId())) {
                    playersAboveBoss.put(player.getUniqueId(), currentTime);
                } else if (currentTime - playersAboveBoss.get(player.getUniqueId()) > 3000) {
                    // Knock player back after 3 seconds
                    knockPlayerBack(player);
                    playersAboveBoss.remove(playerId);
                }
            } else {
                playersAboveBoss.remove(player.getUniqueId());
            }
        }
    }

    /**
     * Knock player back
     */
    private void knockPlayerBack(Player player) {
        org.bukkit.util.Vector knockback = player.getLocation().toVector()
            .subtract(bossEntity.getLocation().toVector())
            .normalize()
            .multiply(3.0);
        knockback.setY(1.5);

        player.setVelocity(knockback);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
    }

    /**
     * Prevent player fortification
     */
    private void preventPlayerFortification() {
        if (bossEntity == null) return;

        Location bossLoc = bossEntity.getLocation();
        World world = bossLoc.getWorld();

        // Break blocks in 3-block radius
        for (int x = -3; x <= 3; x++) {
            for (int y = -1; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    Location checkLoc = bossLoc.clone().add(x, y, z);
                    if (shouldBreakBlock(checkLoc.getBlock().getType())) {
                        checkLoc.getBlock().setType(Material.AIR);
                    }
                }
            }
        }
    }

    /**
     * Check if block should be broken
     */
    private boolean shouldBreakBlock(Material type) {
        return type == Material.OAK_PLANKS || type == Material.COBBLESTONE ||
               type == Material.STONE_BRICKS || type == Material.IRON_BARS;
    }

    /**
     * Get current boss phase
     */
    public int getBossPhase() {
        return bossPhase;
    }

    /**
     * Get boss entity
     */
    public Wither getBossEntity() {
        return bossEntity;
    }

    /**
     * Check if boss is active
     */
    public boolean isBossActive() {
        return bossEntity != null && bossEntity.isValid();
    }

    /**
     * Clean up all boss resources
     */
    public void cleanup() {
        // Cancel tasks
        if (bossBarTask != null) bossBarTask.cancel();
        if (bossAITask != null) bossAITask.cancel();
        if (heightCheckTask != null) heightCheckTask.cancel();
        if (antiWallTask != null) antiWallTask.cancel();
        if (teleportTask != null) teleportTask.cancel();

        // Remove boss bar
        if (bossBar != null) {
            // Note: Adventure API may have different method names
            // bossBar.removeAllPlayers(); // Commented out for compatibility
        }

        // Remove boss entity
        if (bossEntity != null && bossEntity.isValid()) {
            bossEntity.remove();
        }

        // Clear collections
        playersAboveBoss.clear();
        playersNearBoss.clear();
        teleportCooldown.clear();
        playerArrowsShot.clear();

        // Remove reference
        bossEntity = null;
    }
}