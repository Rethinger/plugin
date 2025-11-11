package com.mmmm.story.listeners;

import com.mmmm.story.MmmmStoryPlugin;
import com.mmmm.story.managers.ItemManager;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class PlayerListener implements Listener {
    
    private final MmmmStoryPlugin plugin;
    private BukkitTask autoStartTask = null;
    
    public PlayerListener(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Initialize player data if first join
        plugin.getDataManager().getPlayerData(player.getUniqueId());

        // CRITICAL FIX: Check if player should be in Overworld after ritual completion
        if (plugin.getDataManager().isFinalRitualComplete()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && player.getWorld().getEnvironment() == World.Environment.THE_END) {
                    // Player is in End but ritual is complete - move them to Overworld
                    World overworld = plugin.getServer().getWorlds().stream()
                            .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
                            .findFirst()
                            .orElse(null);

                    if (overworld != null) {
                        Location originalSpawn = plugin.getDataManager().getPlayerOriginalSpawn(player.getUniqueId());
                        Location spawnLoc = originalSpawn != null ? originalSpawn : overworld.getSpawnLocation();

                        player.teleport(spawnLoc);
                        player.setBedSpawnLocation(spawnLoc, true);

                        plugin.getLogger().info("JOIN FIX: Moved player " + player.getName() +
                            " from End to Overworld (ritual complete)");
                    }
                }
            }, 20L); // Check after 1 second
        }

        // Translate all story items in player's inventory to their language
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            translatePlayerItems(player);
        }, 20L); // Wait 1 second after join for client to fully load
        
        // Auto-start story if 2+ players online and story hasn't started
        int onlinePlayers = plugin.getServer().getOnlinePlayers().size();
        int currentAct = plugin.getDataManager().getCurrentAct();
        
        plugin.getLogger().info("Player joined. Online: " + onlinePlayers + ", Act: " + currentAct + ", Task active: " + (autoStartTask != null));
        
        if (onlinePlayers >= 2 && currentAct == 0) {
            // Cancel any existing task to reset timer
            if (autoStartTask != null) {
                plugin.getLogger().info("Cancelling existing auto-start task");
                autoStartTask.cancel();
            }
            
            // Start story in 10 seconds for testing (200 ticks)
            plugin.getLogger().info("Scheduling auto-start in 10 seconds...");
            autoStartTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                // Check again if conditions are still met
                int actNow = plugin.getDataManager().getCurrentAct();
                int playersNow = plugin.getServer().getOnlinePlayers().size();
                plugin.getLogger().info("Auto-start timer elapsed. Act: " + actNow + ", Players: " + playersNow);
                
                if (actNow == 0 && playersNow >= 2) {
                    plugin.getLogger().info("=== AUTO-STARTING STORY NOW ===");
                    try {
                        plugin.getActManager().startCampaign();
                        plugin.getLogger().info("=== STORY STARTED SUCCESSFULLY ===");
                    } catch (Exception e) {
                        plugin.getLogger().severe("=== ERROR STARTING STORY: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    plugin.getLogger().info("Auto-start cancelled - conditions not met (Act: " + actNow + ", Players: " + playersNow + ")");
                }
                autoStartTask = null;
            }, 200L); // 10 seconds for testing
            
            // Notify players
            plugin.getServer().broadcast(Component.text("§6§l⚡ Обнаружено 2+ игроков! Сюжет начнётся через 10 секунд (ТЕСТ)..."));
        }
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        boolean keepOnDeath = plugin.getConfigManager().getConfig().getBoolean("items.keepOnDeath", true);
        
        if (!keepOnDeath) {
            return;
        }
        
        // Keep story items on death
        List<ItemStack> itemsToKeep = new ArrayList<>();
        
        event.getDrops().removeIf(item -> {
            if (plugin.getItemManager().isStoryItem(item)) {
                itemsToKeep.add(item.clone());
                return true;
            }
            return false;
        });
        
        // Return items to player after respawn
        if (!itemsToKeep.isEmpty()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    for (ItemStack item : itemsToKeep) {
                        player.getInventory().addItem(item);
                    }
                    player.sendMessage(Component.text(plugin.getMessageManager().getMessage("items.kept_on_death")));
                }
            }, 20L);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)  // Set highest priority to override other plugins
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        int currentAct = plugin.getDataManager().getCurrentAct();
        boolean isRitualComplete = plugin.getDataManager().isFinalRitualComplete();

        // Debug logging
        plugin.getLogger().info("RESPAWN DEBUG: Player " + player.getName() + " - Act: " + currentAct +
            ", RitualComplete: " + isRitualComplete + ", Current respawn world: " +
            (event.getRespawnLocation() != null ? event.getRespawnLocation().getWorld().getEnvironment().name() : "null"));

        // CRITICAL FIX: If ritual is complete, ALWAYS respawn in Overworld regardless of other conditions
        if (isRitualComplete) {
            // Force respawn in Overworld regardless of current respawn location
            World overworld = plugin.getServer().getWorlds().stream()
                    .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
                    .findFirst()
                    .orElse(null);

            if (overworld != null) {
                // Try to use player's original spawn first
                Location originalSpawn = plugin.getDataManager().getPlayerOriginalSpawn(player.getUniqueId());
                Location spawnLoc = originalSpawn != null ? originalSpawn : overworld.getSpawnLocation();

                event.setRespawnLocation(spawnLoc);
                player.setBedSpawnLocation(spawnLoc, true);  // Also set bed spawn for future respawns

                plugin.getLogger().info("RESPAWN FIX: Player " + player.getName() +
                    " FORCED respawn in Overworld after ritual (Act " + currentAct +
                    ", Original spawn: " + (originalSpawn != null ? "yes" : "no") + ")");
                return;
            }
        }

        // Original logic for when ritual is NOT complete
        // Acts 3-4 OR Act 5 BEFORE ritual completion: Respawn in the End
        if ((currentAct >= 3 && currentAct <= 4) || (currentAct >= 5 && !isRitualComplete)) {
            if (event.getRespawnLocation().getWorld().getEnvironment() != World.Environment.THE_END) {
                // Find the End
                World end = plugin.getServer().getWorlds().stream()
                        .filter(w -> w.getEnvironment() == World.Environment.THE_END)
                        .findFirst()
                        .orElse(null);

                if (end != null) {
                    Location spawnLoc = end.getSpawnLocation();
                    event.setRespawnLocation(spawnLoc);
                    plugin.getLogger().info("Player " + player.getName() + " respawned in the End (Act " + currentAct + ", Ritual: " + isRitualComplete + ")");
                }
            }
        }
    }
    
    /**
     * Translate all story items in player's inventory to their language
     */
    private void translatePlayerItems(Player player) {
        String playerLang = plugin.getDialogManager().getPlayerLanguage(player);
        ItemStack[] inventory = player.getInventory().getContents();
        boolean modified = false;
        
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null && item.getType() != org.bukkit.Material.AIR) {
                String storyItemId = plugin.getItemManager().getStoryItemId(item);
                if (storyItemId != null) {
                    ItemStack translatedItem = plugin.getItemManager().createStoryItem(storyItemId, playerLang);
                    if (translatedItem != null) {
                        translatedItem.setAmount(item.getAmount());
                        inventory[i] = translatedItem;
                        modified = true;
                    }
                }
            }
        }
        
        if (modified) {
            player.getInventory().setContents(inventory);
            player.updateInventory();
        }
    }
}

