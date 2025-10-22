package com.mmmm.story.listeners;

import com.mmmm.story.MmmmStoryPlugin;
import com.mmmm.story.managers.ItemManager;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
                    player.sendMessage(Component.text("§aВаши сюжетные предметы сохранены!"));
                }
            }, 20L);
        }
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // In Act 4, if player dies in the End, respawn them in Overworld instead
        if (plugin.getDataManager().getCurrentAct() >= 4) {
            if (event.getRespawnLocation().getWorld().getEnvironment() == World.Environment.THE_END) {
                // Find the overworld
                World overworld = plugin.getServer().getWorlds().stream()
                        .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
                        .findFirst()
                        .orElse(null);
                
                if (overworld != null) {
                    Location spawnLoc = overworld.getSpawnLocation();
                    event.setRespawnLocation(spawnLoc);
                    plugin.getLogger().info("Player " + player.getName() + " respawned in Overworld (Act 4+)");
                }
            }
        }
    }
}

