package com.mmmm.story.listeners;

import com.mmmm.story.MmmmStoryPlugin;
import com.mmmm.story.managers.MenuManager;
import com.mmmm.story.managers.MenuState;
import com.mmmm.story.managers.MenuType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class MenuClickListener implements Listener {
    
    private final MmmmStoryPlugin plugin;
    
    public MenuClickListener(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        Inventory playerInventory = event.getWhoClicked().getInventory();
        
        // Check if clicked inventory is a menu (not player inventory)
        if (clickedInventory == null || clickedInventory.equals(playerInventory)) {
            return;
        }
        
        // Check if player has an active menu
        MenuState state = plugin.getMenuManager().getMenuState(event.getWhoClicked().getUniqueId());
        if (state == null || !clickedInventory.equals(state.getCurrentInventory())) {
            return;
        }
        
        // Cancel the event to prevent item movement
        event.setCancelled(true);
        
        // Handle the menu click
        plugin.getMenuManager().handleMenuClick(
            (org.bukkit.entity.Player) event.getWhoClicked(), 
            event.getSlot(), 
            state.getCurrentMenu()
        );
    }
}