package com.mmmm.story.managers;

import org.bukkit.inventory.Inventory;
import java.util.Stack;
import java.util.UUID;

/**
 * Represents the state of a player's menu navigation.
 * Tracks menu history and current inventory for proper navigation.
 */
public class MenuState {
    private final UUID playerId;
    private Inventory currentInventory;
    private final Stack<MenuType> menuHistory;
    
    /**
     * Create a new menu state for a player.
     * @param playerId The player's UUID
     * @param initialMenu The initial menu type
     * @param inventory The initial inventory
     */
    public MenuState(UUID playerId, MenuType initialMenu, Inventory inventory) {
        this.playerId = playerId;
        this.currentInventory = inventory;
        this.menuHistory = new Stack<>();
        this.menuHistory.push(initialMenu);
    }
    
    /**
     * Get the player's UUID.
     */
    public UUID getPlayerId() {
        return playerId;
    }
    
    /**
     * Get the current inventory.
     */
    public Inventory getCurrentInventory() {
        return currentInventory;
    }
    
    /**
     * Set the current inventory.
     */
    public void setCurrentInventory(Inventory inventory) {
        this.currentInventory = inventory;
    }
    
    /**
     * Get the current menu type.
     */
    public MenuType getCurrentMenu() {
        return menuHistory.isEmpty() ? MenuType.MAIN : menuHistory.peek();
    }
    
    /**
     * Push a new menu onto the history stack.
     * @param menuType The menu type to push
     */
    public void pushMenu(MenuType menuType) {
        menuHistory.push(menuType);
    }
    
    /**
     * Pop the current menu from the history stack.
     * @return The previous menu type, or null if no previous menu
     */
    public MenuType popMenu() {
        if (menuHistory.size() <= 1) {
            return null; // Can't pop the main menu
        }
        return menuHistory.pop();
    }
    
    /**
     * Get the menu history stack.
     */
    public Stack<MenuType> getMenuHistory() {
        return new Stack<MenuType>() {{
            addAll(menuHistory);
        }};
    }
}