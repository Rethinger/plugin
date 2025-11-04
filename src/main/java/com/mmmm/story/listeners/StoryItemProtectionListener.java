package com.mmmm.story.listeners;

import com.mmmm.story.MmmmStoryPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StoryItemProtectionListener implements Listener {
    
    private final MmmmStoryPlugin plugin;
    
    public StoryItemProtectionListener(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        List<ItemStack> storyItems = new ArrayList<>();
        
        // Iterate through drops and remove story items
        Iterator<ItemStack> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (item != null && plugin.getItemManager().isStoryItem(item)) {
                storyItems.add(item.clone());
                iterator.remove(); // Remove from drops
            }
        }
        
        // Return story items to player inventory after respawn
        if (!storyItems.isEmpty()) {
            player.sendMessage(plugin.getMessageManager().getMessage("item_protection.saved_on_death"));
            
            // Schedule to return items after respawn
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    for (ItemStack item : storyItems) {
                        // Try to add to inventory, if full - drop at player location
                        if (player.getInventory().firstEmpty() != -1) {
                            player.getInventory().addItem(item);
                        } else {
                            player.getWorld().dropItem(player.getLocation(), item);
                        }
                    }
                    player.sendMessage(plugin.getMessageManager().getMessage("item_protection.restored"));
                }
            }, 20L); // Wait 1 second after respawn
        }
    }
}
