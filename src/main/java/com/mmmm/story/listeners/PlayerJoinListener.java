package com.mmmm.story.listeners;

import com.mmmm.story.MmmmStoryPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Listener for handling player join events.
 * Opens the server start menu when a player joins.
 */
public class PlayerJoinListener implements Listener {
    
    private final MmmmStoryPlugin plugin;
    
    public PlayerJoinListener(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Initialize player data if first join
        plugin.getDataManager().getPlayerData(player.getUniqueId());
        
        // Auto-open server start menu is now disabled
        // Menu will only be opened when admin executes /story start command
        /*
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                plugin.getMenuManager().openServerStartMenu(player);
            }
        }, 20L); // Wait 1 second after join for client to fully load
        */
    }
}