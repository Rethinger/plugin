package com.mmmm.story.listeners;

import com.mmmm.story.MmmmStoryPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Act5Listener implements Listener {
    
    private final MmmmStoryPlugin plugin;
    private boolean portalActive = false;
    private Location endCenterLocation = null;
    private Map<UUID, Location> playerPortals = new HashMap<>(); // Track personal portals
    
    public Act5Listener(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onChestPlace(BlockPlaceEvent event) {
        // Mark chests placed by players in The End
        if (event.getBlock().getType() == Material.CHEST || 
            event.getBlock().getType() == Material.TRAPPED_CHEST) {
            if (event.getBlock().getWorld().getEnvironment() == World.Environment.THE_END) {
                event.getBlock().setMetadata("ritual_chest", 
                    new FixedMetadataValue(plugin, true));
            }
        }
    }
    
    @EventHandler
    public void onChestClose(InventoryCloseEvent event) {
        // Check if it's a chest first
        if (!(event.getInventory().getHolder() instanceof Chest)) {
            return;
        }
        
        Chest chest = (Chest) event.getInventory().getHolder();
        Location chestLoc = chest.getLocation();
        
        // Check if in The End
        if (chestLoc.getWorld().getEnvironment() != World.Environment.THE_END) {
            return;
        }
        
        // Check if chest was placed by player (has ritual_chest metadata)
        if (!chest.getBlock().hasMetadata("ritual_chest")) {
            return;
        }
        
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        
        if (portalActive) {
            return;
        }
        if (portalActive) {
            plugin.getLogger().info(">>> Portal already active, ignoring");
            return;
        }
        
        // Find End center (0, y, 0)
        if (endCenterLocation == null) {
            endCenterLocation = new Location(chestLoc.getWorld(), 0, chestLoc.getY(), 0);
        }
        
        // Check if chest is near End center (within 100 blocks)
        double distanceToCenter = chestLoc.distance(new Location(chestLoc.getWorld(), 0, chestLoc.getY(), 0));
        
        if (distanceToCenter > 100) {
            String tooFarMsg = plugin.getMessageManager().getMessage(player, "act5.too_far");
            tooFarMsg = tooFarMsg.replace("%distance%", String.valueOf((int)distanceToCenter));
            player.sendMessage(Component.text(tooFarMsg).color(NamedTextColor.RED));
            return;
        }
        
        // Check for artifacts in this chest and notify player
        checkAndNotifyArtifacts(player, chest);
        
        // Check all ritual chests for complete set
        checkArtifactsInChests(chestLoc);
    }
    
    private void checkAndNotifyArtifacts(Player player, Chest chest) {
        Inventory inv = chest.getInventory();
        boolean foundAnyArtifact = false;
        
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                if (item.hasItemMeta()) {
                    ItemMeta meta = item.getItemMeta();
                    NamespacedKey storyKey = new NamespacedKey(plugin, "storyItem");
                    if (meta.getPersistentDataContainer().has(storyKey, PersistentDataType.STRING)) {
                        String storyId = meta.getPersistentDataContainer().get(storyKey, PersistentDataType.STRING);
                        if (storyId != null && storyId.startsWith("end_artifact_")) {
                            foundAnyArtifact = true;
                            break;
                        }
                    }
                }
            }
        }
        
        if (foundAnyArtifact || countTotalArtifactsInRitualChests(chest.getWorld()) > 0) {
            int totalArtifacts = countTotalArtifactsInRitualChests(chest.getWorld());
            String artifactsMsg = plugin.getMessageManager().getMessage(player, "act5.artifacts_count");
            artifactsMsg = artifactsMsg.replace("%count%", String.valueOf(totalArtifacts));
            player.sendMessage(Component.text(artifactsMsg).color(NamedTextColor.LIGHT_PURPLE));
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.2f);
        }
    }
    
    private int countTotalArtifactsInRitualChests(World world) {
        Location center = new Location(world, 0, 65, 0);
        Set<Integer> foundArtifacts = new HashSet<>();
        
        // Search for ritual chests in 100 block radius from center
        for (int x = -100; x <= 100; x++) {
            for (int y = -20; y <= 20; y++) {
                for (int z = -100; z <= 100; z++) {
                    Location loc = center.clone().add(x, y, z);
                    Block block = loc.getBlock();
                    
                    if ((block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) &&
                        block.hasMetadata("ritual_chest")) {
                        
                        Chest chest = (Chest) block.getState();
                        Inventory inv = chest.getInventory();
                        
                        for (ItemStack item : inv.getContents()) {
                            if (item != null && plugin.getItemManager().isStoryItem(item)) {
                                String itemId = plugin.getItemManager().getStoryItemId(item);
                                
                                if (itemId != null && itemId.startsWith("end_artifact_")) {
                                    int artifactNum = plugin.getItemManager().getArtifactNumber(item);
                                    if (artifactNum != -1) {
                                        foundArtifacts.add(artifactNum);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return foundArtifacts.size();
    }
    
    private void checkArtifactsInChests(Location playerChestLoc) {
        World world = playerChestLoc.getWorld();
        Location center = new Location(world, 0, playerChestLoc.getY(), 0);
        
        Set<Integer> foundArtifacts = new HashSet<>();
        List<Location> chestLocations = new ArrayList<>();
        
        // Search for RITUAL chests in 100 block radius from center
        for (int x = -100; x <= 100; x++) {
            for (int y = -20; y <= 20; y++) {
                for (int z = -100; z <= 100; z++) {
                    Location loc = center.clone().add(x, y, z);
                    Block block = loc.getBlock();
                    
                    if ((block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) &&
                        block.hasMetadata("ritual_chest")) {
                        
                        Chest chest = (Chest) block.getState();
                        Inventory inv = chest.getInventory();
                        
                        // Check for artifacts in this chest
                        for (ItemStack item : inv.getContents()) {
                            if (item != null && plugin.getItemManager().isStoryItem(item)) {
                                String itemId = plugin.getItemManager().getStoryItemId(item);
                                
                                if (itemId != null && itemId.startsWith("end_artifact_")) {
                                    int artifactNum = plugin.getItemManager().getArtifactNumber(item);
                                    if (artifactNum != -1) {
                                        foundArtifacts.add(artifactNum);
                                        if (!chestLocations.contains(chest.getLocation())) {
                                            chestLocations.add(chest.getLocation());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Check if all 5 artifacts are in ritual chests (notification already sent by checkAndNotifyArtifacts)
        if (foundArtifacts.size() >= 5) {
            activateFinalRitual(center, chestLocations);
        }
    }
    
    private void activateFinalRitual(Location center, List<Location> chestLocations) {
        if (portalActive) {
            return;
        }
        
        portalActive = true;
        
        // Mark final ritual as complete (allows END portal to spawn)
        plugin.getDataManager().setFinalRitualComplete(true);
        
        // BUG #4 & #5 FIX: Complete ritual - unblock portal and restore spawn points
        plugin.getDataManager().completeRitual();
        
        // BUG #5 FIX: Restore original spawn points for all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            restorePlayerSpawn(player);
        }
        
        World world = center.getWorld();
        
        // Broadcast
        String allCollectedMsg = plugin.getMessageManager().getMessage("en", "act5.all_artifacts_collected");
        String ritualStartMsg = plugin.getMessageManager().getMessage("en", "act5.ritual_starting");
        
        plugin.getServer().broadcast(Component.text(allCollectedMsg).color(NamedTextColor.DARK_PURPLE));
        plugin.getServer().broadcast(Component.text(ritualStartMsg).color(NamedTextColor.LIGHT_PURPLE));
        plugin.getDialogManager().playDialogForAll("final.ritual_start");
        
        // Effects from each chest with artifact and DESTROY them
        for (Location chestLoc : chestLocations) {
            world.spawnParticle(Particle.ENCHANT, chestLoc.clone().add(0.5, 0.5, 0.5), 200, 0.5, 0.5, 0.5, 0.5);
            world.spawnParticle(Particle.PORTAL, chestLoc.clone().add(0.5, 0.5, 0.5), 100, 1, 1, 1, 0.3);
            world.spawnParticle(Particle.END_ROD, chestLoc.clone().add(0.5, 0.5, 0.5), 50, 0.3, 0.3, 0.3, 0.1);
            world.spawnParticle(Particle.FLAME, chestLoc.clone().add(0.5, 0.5, 0.5), 100, 0.3, 0.3, 0.3, 0.05);
            
            // Beam from chest to center
            createBeamEffect(chestLoc, center);
            
            // Destroy chest and its contents (artifacts consumed by ritual)
            Block chestBlock = chestLoc.getBlock();
            if (chestBlock.getState() instanceof Chest) {
                Chest chest = (Chest) chestBlock.getState();
                chest.getInventory().clear(); // Clear inventory (artifacts consumed)
                world.spawnParticle(Particle.EXPLOSION, chestLoc.clone().add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0);
                world.playSound(chestLoc, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.2f);
                chestBlock.setType(Material.AIR); // Destroy chest
            }
        }
        
        // Center effects
        world.spawnParticle(Particle.EXPLOSION, center, 10, 5, 5, 5, 0);
        world.spawnParticle(Particle.PORTAL, center, 500, 3, 3, 3, 0.5);
        world.spawnParticle(Particle.END_ROD, center, 300, 2, 2, 2, 0.2);
        world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.6f);
        world.playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 0.5f);
        world.playSound(center, Sound.BLOCK_END_PORTAL_SPAWN, 2.0f, 0.8f);
        
        // Apply effects to all players in area
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distance(center) <= 150) {
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 0.8f);
                
                // Apply DARKNESS effect - 50 seconds (46s ritual_start + 4s complete)
                player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 1000, 0)); // 50 seconds
                
                if (player.getLocation().distance(center) <= 50) {
                    // Slow ascension effect - multiple stages
                    startSlowAscension(player);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 1000, 0)); // 50s Glowing
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 1000, 0)); // 50s Safe descent
                }
            }
        }
        
        // Spawn END_ROD particles in ring around players
        spawnParticleRingAroundPlayers(center, world);
        
        // Don't create the large 100x100 portal - players use personal 3x3 portals
    }
    
    private void createBeamEffect(Location from, Location to) {
        World world = from.getWorld();
        double distance = from.distance(to);
        int particles = (int) (distance * 2);
        
        for (int i = 0; i < particles; i++) {
            double ratio = (double) i / particles;
            Location point = from.clone().add(
                (to.getX() - from.getX()) * ratio,
                (to.getY() - from.getY()) * ratio + 1,
                (to.getZ() - from.getZ()) * ratio
            );
            world.spawnParticle(Particle.END_ROD, point, 1, 0, 0, 0, 0);
            world.spawnParticle(Particle.PORTAL, point, 2, 0.1, 0.1, 0.1, 0);
        }
    }
    
    private void startSlowAscension(Player player) {
        Location startLoc = player.getLocation().clone();
        
        new BukkitRunnable() {
            int ticks = 0;
            final int totalDuration = 1000; // 50 seconds total (46s ritual_start + 4s complete)
            final double maxHeight = 40; // Rise 40 blocks
            boolean portalCreated = false;
            
            @Override
            public void run() {
                if (ticks >= totalDuration || !player.isOnline()) {
                    cancel();
                    return;
                }
                
                // Smooth easing function for slow start, fast middle, slow end
                double progress = (double) ticks / totalDuration;
                double easedProgress = easeInOutCubic(progress);
                
                // Calculate current height
                double currentHeight = maxHeight * easedProgress;
                Location targetLoc = startLoc.clone().add(0, currentHeight, 0);
                
                // Smoothly move player
                if (player.getLocation().getY() < targetLoc.getY()) {
                    player.setVelocity(player.getVelocity().setY(0.15)); // Gentle upward push
                }
                
                // Create portal at 47 seconds (1 second after "Портал домой открывается" at 46s)
                // 46s in ritual_start (delay:46)
                // 47 seconds = 940 ticks = 94% of 1000 ticks
                if (ticks >= 940 && !portalCreated) {
                    createPersonalPortal(player);
                    portalCreated = true;
                }
                
                // Spawn particles around player during ascension
                for (int i = 0; i < 3; i++) {
                    double angle = Math.random() * 2 * Math.PI;
                    double radius = 1.5;
                    double x = player.getLocation().getX() + radius * Math.cos(angle);
                    double z = player.getLocation().getZ() + radius * Math.sin(angle);
                    double y = player.getLocation().getY() + Math.random() * 2;
                    
                    Location particleLoc = new Location(player.getWorld(), x, y, z);
                    player.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
                    player.getWorld().spawnParticle(Particle.PORTAL, particleLoc, 2, 0.1, 0.1, 0.1, 0);
                }
                
                // Sound effects at intervals
                if (ticks % 40 == 0) {
                    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 0.5f, 1.5f);
                }
                
                if (ticks % 20 == 0) {
                    player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.3f, 1.8f);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    // Create 3x3 portal platform under player at final ascension height
    private void createPersonalPortal(Player player) {
        Location playerLoc = player.getLocation();
        World world = player.getWorld();
        
        // Create portal 3x3 platform 2 blocks below player
        Location portalCenter = playerLoc.clone().subtract(0, 2, 0);
        
        // Create 3x3 PURPLE_STAINED_GLASS platform (can be removed unlike END_PORTAL)
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location blockLoc = portalCenter.clone().add(x, 0, z);
                blockLoc.getBlock().setType(Material.PURPLE_STAINED_GLASS);
                
                // Add particle effect
                world.spawnParticle(Particle.PORTAL, blockLoc.clone().add(0.5, 0.5, 0.5), 20, 0.3, 0.3, 0.3, 0.1);
                world.spawnParticle(Particle.END_ROD, blockLoc.clone().add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0.05);
            }
        }
        
        // Sound effect
        world.playSound(portalCenter, Sound.BLOCK_PORTAL_TRIGGER, 1.0f, 1.2f);
        world.playSound(portalCenter, Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1.0f, 0.8f);
        
        // Save portal location for this player
        playerPortals.put(player.getUniqueId(), portalCenter);
        
        // Start continuous particle effects around portal
        startPortalParticles(portalCenter);
    }
    
    // Easing function for smooth animation
    private double easeInOutCubic(double t) {
        return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
    }
    
    // Create continuous portal particle effects
    private void startPortalParticles(Location portalCenter) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                // Stop after 60 seconds or if portal was removed
                if (ticks >= 1200 || !playerPortals.containsValue(portalCenter)) {
                    cancel();
                    return;
                }
                
                World world = portalCenter.getWorld();
                
                // Spawn particles in circle around portal
                for (int i = 0; i < 10; i++) {
                    double angle = (ticks + i * 36) * Math.PI / 180.0;
                    double radius = 2.0;
                    double x = portalCenter.getX() + radius * Math.cos(angle);
                    double z = portalCenter.getZ() + radius * Math.sin(angle);
                    double y = portalCenter.getY() + 0.5;
                    
                    Location particleLoc = new Location(world, x, y, z);
                    world.spawnParticle(Particle.PORTAL, particleLoc, 2, 0.1, 0.1, 0.1, 0);
                    world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
                }
                
                // Play ambient sound occasionally
                if (ticks % 40 == 0) {
                    world.playSound(portalCenter, Sound.BLOCK_PORTAL_AMBIENT, 0.3f, 1.5f);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    @EventHandler
    public void onPlayerMoveOnPortal(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location portalLoc = playerPortals.get(player.getUniqueId());
        
        if (portalLoc == null) {
            return;
        }
        
        Location playerLoc = player.getLocation();
        
        // Check if player is standing on their portal (within 3x3 area, Y±1)
        if (Math.abs(playerLoc.getX() - portalLoc.getX()) <= 1.5 &&
            Math.abs(playerLoc.getZ() - portalLoc.getZ()) <= 1.5 &&
            Math.abs(playerLoc.getY() - portalLoc.getY()) <= 2) {
            
            // Teleport player to Overworld
            World overworld = plugin.getServer().getWorlds().get(0);
            Location spawn = overworld.getSpawnLocation();
            
            player.teleport(spawn);
            String returnedMsg = plugin.getMessageManager().getMessage(player, "act5.returned_overworld");
            player.sendMessage(Component.text(returnedMsg));
            
            // Remove effects
            player.removePotionEffect(PotionEffectType.DARKNESS);
            player.removePotionEffect(PotionEffectType.GLOWING);
            player.removePotionEffect(PotionEffectType.SLOW_FALLING);
            
            // Destroy portal
            destroyPersonalPortal(player.getUniqueId(), portalLoc);
            
            // Cleanup all NPCs (including any leftover tiny messenger NPCs)
            plugin.getNPCManager().cleanup();
            
            // Play completion dialog
            plugin.getDialogManager().playDialogForAll("final.complete");
        }
    }
    
    private void destroyPersonalPortal(UUID playerId, Location portalLoc) {
        // Remove 3x3 portal blocks
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location blockLoc = portalLoc.clone().add(x, 0, z);
                if (blockLoc.getBlock().getType() == Material.PURPLE_STAINED_GLASS) {
                    blockLoc.getBlock().setType(Material.AIR);
                }
            }
        }
        
        // Effects at portal location
        World endWorld = portalLoc.getWorld();
        endWorld.playSound(portalLoc, Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 1.5f);
        endWorld.spawnParticle(Particle.PORTAL, portalLoc.clone().add(0, 0.5, 0), 50, 1, 0.5, 1, 0.1);
        
        // Remove from tracking
        playerPortals.remove(playerId);
    }
    
    private void spawnParticleRingAroundPlayers(Location center, World world) {
        // Spawn END_ROD particles in a ring around each player
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distance(center) <= 150) {
                Location playerLoc = player.getLocation();
                int radius = 3;
                int points = 24;
                
                // Create particle ring around player
                new BukkitRunnable() {
                    int ticks = 0;
                    
                    @Override
                    public void run() {
                        if (ticks >= 100) { // Run for 5 seconds
                            cancel();
                            return;
                        }
                        
                        for (int i = 0; i < points; i++) {
                            double angle = (2 * Math.PI / points) * i + (ticks * 0.1);
                            double x = playerLoc.getX() + radius * Math.cos(angle);
                            double z = playerLoc.getZ() + radius * Math.sin(angle);
                            double y = playerLoc.getY() + 1;
                            
                            Location particleLoc = new Location(world, x, y, z);
                            world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
                        }
                        
                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }
        }
        
        // Sound
        world.playSound(center, Sound.BLOCK_END_PORTAL_SPAWN, 3.0f, 0.5f);
    }
    
    // ==========================================
    // BUG #5 FIX: SPAWN POINT RESTORATION
    // ==========================================
    
    /**
     * Restore player's original spawn point after Act 5 ritual completion
     * @param player Player to restore spawn for
     */
    private void restorePlayerSpawn(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Get saved original spawn
        Location originalSpawn = plugin.getDataManager().getPlayerOriginalSpawn(playerId);
        
        if (originalSpawn != null) {
            // Restore bed spawn location
            player.setBedSpawnLocation(originalSpawn, true);
            
            // Clear trapped flag
            plugin.getDataManager().setPlayerTrappedInEnd(playerId, false);
            
            // Clear saved spawn data
            plugin.getDataManager().clearPlayerOriginalSpawn(playerId);
            
            // Send feedback message
            plugin.getMessageManager().sendMessage(player, "spawn.restored");
            
            if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
                plugin.getLogger().info("Restored original spawn for " + player.getName() + " at " + 
                    originalSpawn.getWorld().getName() + " " + originalSpawn.getBlockX() + "," + 
                    originalSpawn.getBlockY() + "," + originalSpawn.getBlockZ());
            }
        } else {
            if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
                plugin.getLogger().info("No saved spawn to restore for " + player.getName());
            }
        }
    }
}
