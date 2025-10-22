package com.mmmm.story.listeners;

import com.mmmm.story.MmmmStoryPlugin;
import com.mmmm.story.managers.ItemManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Act1Listener implements Listener {
    
    private final MmmmStoryPlugin plugin;
    private final Map<UUID, Integer> playerWaveCount = new HashMap<>();
    private final Map<UUID, Boolean> activeWavePlayers = new HashMap<>();
    private final Map<UUID, Boolean> playerReceivedAchievement = new HashMap<>();
    private Location coreDropLocation = null; // Store location where core was dropped
    
    public Act1Listener(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
        startSkeletonWaveTask();
    }
    
    // Public method to get core location for portal ignition
    public Location getCoreDropLocation() {
        return coreDropLocation;
    }
    
    private void startSkeletonWaveTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Continue waves until Boss 1 is defeated (not just in Act 1)
                if (plugin.getDataManager().isBoss1Defeated()) {
                    return; // Stop waves after Boss 1 is defeated
                }
                
                for (World world : plugin.getServer().getWorlds()) {
                    if (world.getEnvironment() != World.Environment.NORMAL) {
                        continue;
                    }
                    
                    long day = world.getFullTime() / 24000;
                    long timeOfDay = world.getTime();
                    
                    // Check if it's every 3rd night (day 3, 6, 9, 12...)
                    // Day 0 = первый день, day 1 = второй день, day 2 = третий день
                    // Поэтому проверяем day >= 2 && (day % 3 == 2)
                    boolean isWaveDay = (day >= 2 && (day % 3 == 2));
                    
                    // Check if yesterday was wave day (for achievement)
                    boolean wasWaveDayYesterday = (day >= 3 && ((day - 1) % 3 == 2));
                    
                    if (!isWaveDay && !wasWaveDayYesterday) {
                        // Reset wave flags when not a wave day and not day after wave
                        for (Player player : world.getPlayers()) {
                            UUID uuid = player.getUniqueId();
                            activeWavePlayers.put(uuid, false);
                            playerReceivedAchievement.put(uuid, false);
                        }
                        continue;
                    }
                    
                    // Check if it's night (13000-23000)
                    if (isWaveDay && timeOfDay >= 13000 && timeOfDay <= 23000) {
                        // Spawn waves during night
                        for (Player player : world.getPlayers()) {
                            spawnSkeletonWavesForPlayer(player);
                        }
                    } else if (wasWaveDayYesterday && timeOfDay >= 0 && timeOfDay < 13000) {
                        // Day time after wave night - give achievement (only once per player)
                        for (Player player : world.getPlayers()) {
                            UUID uuid = player.getUniqueId();
                            // Check if player already has this achievement
                            List<String> achievements = plugin.getDataManager().getPlayerAchievements(uuid);
                            boolean hasAchievement = achievements.contains("survived_skeleton_wave");
                            
                            if (activeWavePlayers.getOrDefault(uuid, false) && !playerReceivedAchievement.getOrDefault(uuid, false) && !hasAchievement) {
                                // Player survived the night for the first time
                                player.sendMessage("§a§l✔ Достижение разблокировано: Выживший");
                                player.sendMessage("§7Вы пережили волну воинов-скелетов!");
                                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                                plugin.getDataManager().addPlayerAchievement(uuid, "survived_skeleton_wave");
                                playerReceivedAchievement.put(uuid, true);
                            }
                            // Don't reset activeWavePlayers here - only reset achievement flag
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 100L, 100L); // Check every 5 seconds
    }
    
    private Location getRandomLocationAround(Location center, int radius) {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            // Minimum distance 20 blocks, maximum radius blocks
            double distance = 20 + random.nextDouble() * (radius - 20);
            
            int x = center.getBlockX() + (int) (Math.cos(angle) * distance);
            int z = center.getBlockZ() + (int) (Math.sin(angle) * distance);
            int y = center.getWorld().getHighestBlockYAt(x, z);
            
            Location loc = new Location(center.getWorld(), x, y, z);
            if (loc.getBlock().getType().isSolid()) {
                return loc.add(0, 1, 0);
            }
        }
        return null;
    }
    private void spawnSkeletonWavesForPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Mark as active on first spawn
        if (!activeWavePlayers.getOrDefault(uuid, false)) {
            activeWavePlayers.put(uuid, true);
            playerReceivedAchievement.put(uuid, false);
            player.sendMessage("§c§lВолна скелетов началась! Защищайтесь до рассвета!");
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.0f);
        }
        
        // Count nearby skeletons for this player
        int nearbySkeletons = 0;
        int radius = plugin.getConfigManager().getConfig().getInt("acts.skeletonWaves.perPlayerRadius", 75);
        
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Skeleton skeleton) {
                if (skeleton.getCustomName() != null && skeleton.getCustomName().contains("Воин-Скелет")) {
                    nearbySkeletons++;
                }
            }
        }
        
        int maxPerPlayer = plugin.getConfigManager().getConfig().getInt("acts.skeletonWaves.maxPerPlayer", 15);
        
        // Spawn new skeletons if below limit
        if (nearbySkeletons < maxPerPlayer) {
            int toSpawn = Math.min(3, maxPerPlayer - nearbySkeletons); // Spawn up to 3 at a time
            
            for (int i = 0; i < toSpawn; i++) {
                Location spawnLoc = getRandomLocationAround(player.getLocation(), radius);
                if (spawnLoc != null && spawnLoc.getBlock().getLightLevel() <= 7) {
                    spawnWarriorSkeleton(spawnLoc, player);
                }
            }
        }
    }
    
    private void spawnWarriorSkeleton(Location location, Player targetPlayer) {
        Skeleton skeleton = (Skeleton) location.getWorld().spawnEntity(location, EntityType.SKELETON);
        skeleton.setCustomName("§6Воин-Скелет");
        skeleton.setCustomNameVisible(true);
        
        // Equipment: gold helmet, iron chestplate, stone sword
        skeleton.getEquipment().setHelmet(new ItemStack(Material.GOLDEN_HELMET));
        skeleton.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        skeleton.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SWORD));
        
        // Set equipment to not drop
        skeleton.getEquipment().setHelmetDropChance(0.0f);
        skeleton.getEquipment().setChestplateDropChance(0.0f);
        skeleton.getEquipment().setItemInMainHandDropChance(0.0f);
        
        // Set target to player
        skeleton.setTarget(targetPlayer);
        
        // Make skeleton aggressive to all nearby players
        new BukkitRunnable() {
            @Override
            public void run() {
                if (skeleton.isDead() || !skeleton.isValid()) {
                    cancel();
                    return;
                }
                
                // Find nearest player in 75 block radius
                Player nearest = null;
                double nearestDistance = 75.0;
                
                for (Player player : skeleton.getWorld().getPlayers()) {
                    double distance = player.getLocation().distance(skeleton.getLocation());
                    if (distance < nearestDistance) {
                        nearest = player;
                        nearestDistance = distance;
                    }
                }
                
                if (nearest != null && skeleton.getTarget() == null) {
                    skeleton.setTarget(nearest);
                }
            }
        }.runTaskTimer(plugin, 20L, 40L); // Check every 2 seconds
    }
    
    // NOTE: Forgotten Altar removed - now using vanilla Ruined Portal structures
    // Stabilization Core spawns in chests of Ruined Portals via ChestSpawnManager
    // See: ChestSpawnManager.java -> StoryStructureType.RUINED_PORTAL
    
    @EventHandler
    public void onItemSpawn(org.bukkit.event.entity.ItemSpawnEvent event) {
        if (plugin.getDataManager().getCurrentAct() != 1) {
            return;
        }
        
        org.bukkit.entity.Item droppedItem = event.getEntity();
        ItemStack itemStack = droppedItem.getItemStack();
        
        // Check if it's the Stabilization Core
        if (!plugin.getItemManager().isStoryItem(itemStack)) {
            return;
        }
        
        if (!ItemManager.STABILIZATION_CORE.equals(plugin.getItemManager().getStoryItemId(itemStack))) {
            return;
        }
        
        // Schedule a check for obsidian block below after a short delay
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!droppedItem.isValid() || droppedItem.isDead()) {
                    cancel();
                    return;
                }
                
                Location itemLoc = droppedItem.getLocation();
                Location blockBelow = itemLoc.subtract(0, 1, 0);
                
                // Check if item is on obsidian
                if (blockBelow.getBlock().getType() == Material.OBSIDIAN) {
                    // Activate the node!
                    activateCrossroadsNode(droppedItem);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 10L, 5L); // Check after 0.5s, then every 0.25s
    }
    
    private void activateCrossroadsNode(org.bukkit.entity.Item droppedItem) {
        Location location = droppedItem.getLocation();
        World world = location.getWorld();
        
        // Store core drop location for portal ignition
        coreDropLocation = location.clone();
        
        // Remove the item
        droppedItem.remove();
        
        // Enable nether portals
        plugin.getDataManager().setNetherEnabled(true);
        plugin.getActManager().progressToAct(2);
        
        // Play effects at the location
        world.spawnParticle(Particle.PORTAL, location, 200, 1, 1, 1, 0.5);
        world.spawnParticle(Particle.ENCHANT, location, 100, 1, 1, 1, 0.5);
        world.spawnParticle(Particle.END_ROD, location, 50, 0.5, 0.5, 0.5, 0.1);
        world.spawnParticle(Particle.FLAME, location, 80, 0.5, 0.2, 0.5, 0.05);
        world.playSound(location, Sound.BLOCK_END_PORTAL_SPAWN, 2.0f, 1.0f);
        world.playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 1.2f);
        
        // Fire will be set by ignitePortal flag in DialogManager when dialog plays
        
        // Create a pillar of light effect
        for (int y = 0; y < 20; y++) {
            Location particleLoc = location.clone().add(0, y, 0);
            world.spawnParticle(Particle.END_ROD, particleLoc, 3, 0.2, 0.2, 0.2, 0.01);
        }
        
        // Notify nearby players
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distance(location) < 100) {
                player.sendMessage(Component.text("§6§l⚡ Узел Перекрёстков активирован!").color(NamedTextColor.GOLD));
                player.sendMessage(Component.text("§aПорталы в Ад теперь работают!").color(NamedTextColor.GREEN));
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                plugin.getDataManager().addPlayerAchievement(player.getUniqueId(), "crossroads_linked");
                plugin.getDialogManager().playDialog(player, "node.crossroads");
            }
        }
    }
}
