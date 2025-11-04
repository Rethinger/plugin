package com.mmmm.story.managers;

import com.mmmm.story.MmmmStoryPlugin;
import com.mmmm.story.data.PlayerSettings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the unified menu system for the Mmmm Story Plugin.
 * Provides GUI-based navigation replacing multiple /story subcommands.
 * 
 * Thread-safe: All public methods can be called from any thread.
 * 
 * @since 1.4.0
 */
public class MenuManager {
    private final MmmmStoryPlugin plugin;
    private final MessageManager messageManager;
    private final ActManager actManager;
    private final DialogManager dialogManager;
    private final ConcurrentHashMap<UUID, MenuState> activeMenus;
    
    public MenuManager(MmmmStoryPlugin plugin, MessageManager messageManager, 
                      ActManager actManager, DialogManager dialogManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.actManager = actManager;
        this.dialogManager = dialogManager;
        this.activeMenus = new ConcurrentHashMap<>();
    }
    
    /**
     * Opens the main menu GUI for the specified player.
     * Performs blocking checks for boss fights and dialogs.
     */
    public void openMainMenu(Player player) {
        // Check blocking conditions
        if (isPlayerInBossFight(player.getUniqueId())) {
            player.sendMessage(messageManager.getMessage(player, "menu.blocked.combat"));
            return;
        }
        
        if (isPlayerInDialog(player.getUniqueId())) {
            player.sendMessage(messageManager.getMessage(player, "menu.blocked.dialog"));
            return;
        }
        
        // Create main menu
        String title = messageManager.getMessage(player, "menu.main.title");
        Inventory menu = Bukkit.createInventory(null, 27, title);
        
        // Add menu items
        menu.setItem(11, createMenuItem(Material.WRITABLE_BOOK, messageManager.getMessage(player, "menu.main.settings")));
        menu.setItem(13, createMenuItem(Material.BOOK, messageManager.getMessage(player, "menu.main.story")));
        menu.setItem(15, createMenuItem(Material.PAPER, messageManager.getMessage(player, "menu.main.info")));
        menu.setItem(26, createMenuItem(Material.BARRIER, messageManager.getMessage(player, "menu.main.close")));
        
        // Store state and open
        MenuState state = new MenuState(player.getUniqueId(), MenuType.MAIN, menu);
        activeMenus.put(player.getUniqueId(), state);
        player.openInventory(menu);
    }
    
    /**
     * Opens the story menu GUI for the specified player.
     * Shows current progress, active quests, and lore information.
     */
    public void openStoryMenu(Player player) {
        // Check blocking conditions
        if (isPlayerInBossFight(player.getUniqueId())) {
            player.sendMessage(messageManager.getMessage(player, "menu.blocked.combat"));
            return;
        }
        
        if (isPlayerInDialog(player.getUniqueId())) {
            player.sendMessage(messageManager.getMessage(player, "menu.blocked.dialog"));
            return;
        }
        
        // Create story menu
        String title = messageManager.getMessage(player, "menu.story.title");
        Inventory menu = Bukkit.createInventory(null, 27, title);
        
        // Get player progress
        int currentAct = plugin.getDataManager().getPlayerCurrentAct(player.getUniqueId());
        String actStatus = actManager.getActStatus(currentAct);
        
        // Add progress indicator
        menu.setItem(11, createMenuItem(Material.COMPASS,
            messageManager.getMessage(player, "menu.story.progress").replace("%act%", String.valueOf(currentAct))));
        
        // Add current act status
        menu.setItem(13, createMenuItem(Material.BOOK,
            messageManager.getMessage(player, "menu.story.act").replace("%status%", actStatus)));
        
        // Add active quests
        menu.setItem(15, createMenuItem(Material.PAPER,
            messageManager.getMessage(player, "menu.story.quests")));
        
        // Add lore information
        menu.setItem(22, createMenuItem(Material.WRITTEN_BOOK,
            messageManager.getMessage(player, "menu.story.lore")));
        
        // Add back button
        menu.setItem(26, createMenuItem(Material.BARRIER,
            messageManager.getMessage(player, "menu.main.close")));
        
        // Store state and open
        MenuState state = new MenuState(player.getUniqueId(), MenuType.STORY, menu);
        activeMenus.put(player.getUniqueId(), state);
        player.openInventory(menu);
    }
    
    /**
     * Opens the settings submenu for the specified player.
     */
    public void openSettingsSubmenu(Player player) {
        String title = messageManager.getMessage(player, "menu.settings.title");
        Inventory menu = Bukkit.createInventory(null, 9, title);
        
        // Get current settings from DataManager
        var settings = plugin.getDataManager().getPlayerSettings(player.getUniqueId());
        PlayerSettings.DialogSpeed dialogSpeed = settings.getDialogSpeed();
        String speed = dialogSpeed.name(); // SLOW, NORMAL, or FAST
        boolean showDialogs = settings.isShowDialogs();
        String display = showDialogs ? "Включены" : "Выключены";
        
        // Add settings items with current values (no language selection)
        menu.setItem(2, createMenuItem(Material.CLOCK,
            messageManager.getMessage(player, "menu.settings.speed").replace("%speed%", speed)));
        menu.setItem(6, createMenuItem(Material.ENDER_EYE,
            messageManager.getMessage(player, "menu.settings.display").replace("%mode%", display)));
        menu.setItem(8, createMenuItem(Material.ARROW,
            messageManager.getMessage(player, "menu.settings.back")));
        
        // Update state
        MenuState state = activeMenus.get(player.getUniqueId());
        if (state != null) {
            state.pushMenu(MenuType.SETTINGS);
            state.setCurrentInventory(menu);
            player.openInventory(menu);
        }
    }
    
    /**
     * Opens the information submenu for the specified player.
     */
    public void openInfoSubmenu(Player player) {
        String title = messageManager.getMessage(player, "menu.info.title");
        Inventory menu = Bukkit.createInventory(null, 9, title);
        
        menu.setItem(4, createMenuItem(Material.KNOWLEDGE_BOOK, 
            messageManager.getMessage(player, "menu.info.version")));
        menu.setItem(8, createMenuItem(Material.ARROW, 
            messageManager.getMessage(player, "menu.info.back")));
        
        MenuState state = activeMenus.get(player.getUniqueId());
        if (state != null) {
            state.pushMenu(MenuType.INFO);
            state.setCurrentInventory(menu);
            player.openInventory(menu);
        }
    }
    
    /**
     * Handles a click event in a menu GUI.
     */
    public void handleMenuClick(Player player, int slot, MenuType currentMenu) {
        switch (currentMenu) {
            case MAIN:
                handleMainMenuClick(player, slot);
                break;
            case SETTINGS:
                handleSettingsMenuClick(player, slot);
                break;
            case INFO:
                handleInfoMenuClick(player, slot);
                break;
            case STORY:
                handleStoryMenuClick(player, slot);
                break;
            case SERVER_START:
                handleServerStartMenuClick(player, slot);
                break;
        }
    }
    
    private void handleMainMenuClick(Player player, int slot) {
        switch (slot) {
            case 11: // Settings
                openSettingsSubmenu(player);
                break;
            case 13: // Story
                openStoryMenu(player);
                break;
            case 15: // Info
                openInfoSubmenu(player);
                break;
            case 26: // Close
                closeMenu(player);
                break;
        }
    }
    
    private void handleSettingsMenuClick(Player player, int slot) {
        if (slot == 8) {  // Back button
            MenuState state = activeMenus.get(player.getUniqueId());
            if (state != null && state.popMenu() != null) {
                openMainMenu(player);
            }
        } else if (slot == 2) {  // Dialog speed toggle
            var settings = plugin.getDataManager().getPlayerSettings(player.getUniqueId());
            // Cycle through speeds: SLOW -> NORMAL -> FAST -> SLOW
            PlayerSettings.DialogSpeed currentSpeed = settings.getDialogSpeed();
            PlayerSettings.DialogSpeed newSpeed = switch (currentSpeed) {
                case SLOW -> PlayerSettings.DialogSpeed.NORMAL;
                case NORMAL -> PlayerSettings.DialogSpeed.FAST;
                case FAST -> PlayerSettings.DialogSpeed.SLOW;
            };
            settings.setDialogSpeed(newSpeed);
            plugin.getDataManager().savePlayerSettings(player.getUniqueId(), settings);
            openSettingsSubmenu(player);  // Refresh menu to show new value
        } else if (slot == 6) {  // Display mode toggle (moved from slot 4)
            var settings = plugin.getDataManager().getPlayerSettings(player.getUniqueId());
            settings.setShowDialogs(!settings.isShowDialogs());
            plugin.getDataManager().savePlayerSettings(player.getUniqueId(), settings);
            openSettingsSubmenu(player);  // Refresh menu to show new value
        }
    }
    
    private void handleInfoMenuClick(Player player, int slot) {
        if (slot == 8) {  // Back button
            MenuState state = activeMenus.get(player.getUniqueId());
            if (state != null && state.popMenu() != null) {
                openMainMenu(player);
            }
        }
    }
    
    private void handleStoryMenuClick(Player player, int slot) {
        if (slot == 26) {  // Back button
            MenuState state = activeMenus.get(player.getUniqueId());
            if (state != null && state.popMenu() != null) {
                openMainMenu(player);
            }
        } else if (slot == 11) {  // Progress indicator
            // Show detailed progress information
            int currentAct = plugin.getDataManager().getPlayerCurrentAct(player.getUniqueId());
            String actStatus = actManager.getActStatus(currentAct);
            
            player.sendMessage(plugin.getMessageManager().getMessage("progress.title"));
            player.sendMessage(plugin.getMessageManager().getMessage("progress.act").replace("%act%", String.valueOf(currentAct)).replace("%status%", actStatus));
            
            // Show completed quests for current act
            switch (currentAct) {
                case 1:
                    player.sendMessage(plugin.getMessageManager().getMessage("progress.act1_complete"));
                    player.sendMessage(plugin.getMessageManager().getMessage("progress.act1_complete2"));
                    break;
                case 2:
                    player.sendMessage(plugin.getMessageManager().getMessage("progress.act2_complete"));
                    player.sendMessage(plugin.getMessageManager().getMessage("progress.act2_complete2"));
                    break;
                case 3:
                    player.sendMessage(plugin.getMessageManager().getMessage("progress.act3_complete"));
                    player.sendMessage(plugin.getMessageManager().getMessage("progress.act3_complete2"));
                    break;
                case 4:
                    player.sendMessage(plugin.getMessageManager().getMessage("progress.act4_complete"));
                    player.sendMessage(plugin.getMessageManager().getMessage("progress.act4_complete2"));
                    break;
                case 5:
                    player.sendMessage(plugin.getMessageManager().getMessage("progress.act5_complete"));
                    player.sendMessage(plugin.getMessageManager().getMessage("progress.act5_complete2"));
                    break;
            }
        } else if (slot == 13) {  // Act status
            // Show detailed act information
            int currentAct = plugin.getDataManager().getPlayerCurrentAct(player.getUniqueId());
            
            player.sendMessage("§6=== Информация об актах ===");
            
            for (int i = 1; i <= 5; i++) {
                String actStatus = actManager.getActStatus(i);
                String prefix = (i == currentAct) ? "§a" : "§7";
                player.sendMessage(prefix + plugin.getMessageManager().getMessage("progress.act").replace("%act%", String.valueOf(i)).replace("%status%", actStatus));
            }
        } else if (slot == 15) {  // Active quests
            // Show current active quests
            int currentAct = plugin.getDataManager().getPlayerCurrentAct(player.getUniqueId());
            
            player.sendMessage("§6=== Активные квесты ===");
            
            switch (currentAct) {
                case 1:
                    player.sendMessage(plugin.getMessageManager().getMessage("progress.quest1"));
                    player.sendMessage(plugin.getMessageManager().getMessage("progress.quest2"));
                    break;
                case 2:
                    player.sendMessage(plugin.getMessageManager().getMessage("progress.quest3"));
                    player.sendMessage(plugin.getMessageManager().getMessage("progress.quest4"));
                    break;
                case 3:
                    player.sendMessage(plugin.getMessageManager().getMessage("progress.quest5"));
                    player.sendMessage(plugin.getMessageManager().getMessage("progress.quest6"));
                    break;
                case 4:
                    player.sendMessage(plugin.getMessageManager().getMessage("progress.quest7"));
                    player.sendMessage(plugin.getMessageManager().getMessage("progress.quest8"));
                    break;
                case 5:
                    player.sendMessage(plugin.getMessageManager().getMessage("progress.quest9"));
                    player.sendMessage(plugin.getMessageManager().getMessage("progress.quest10"));
                    break;
            }
        } else if (slot == 22) {  // Lore information
            // Show story lore
            player.sendMessage("§6=== Лор истории ===");
            player.sendMessage("§7Давным-давно мир страдал от Тёмного Лорда.");
            player.sendMessage("§7Древние герои запечатали его, но печати ослабли.");
            player.sendMessage("§7Теперь Тёмный Лорд свободен и угрожает уничтожить все миры.");
            player.sendMessage("§7Только вы, избранные герои, можете остановить его.");
            player.sendMessage("§7Соберите 5 артефактов Края и выполните ритуал запечатывания.");
            player.sendMessage("§7Судьба миров в ваших руках.");
        }
    }
    
    /**
     * Opens the server start menu GUI for the specified player.
     * Shows dialog speed, display mode settings, and ready button.
     */
    public void openServerStartMenu(Player player) {
        // Create server start menu
        String title = plugin.getMessageManager().getMessage("server_start_menu.title");
        Inventory menu = Bukkit.createInventory(null, 27, title);
        
        // Get current settings from DataManager
        var settings = plugin.getDataManager().getPlayerSettings(player.getUniqueId());
        PlayerSettings.DialogSpeed dialogSpeed = settings.getDialogSpeed();
        String speed = dialogSpeed.name(); // SLOW, NORMAL, or FAST
        boolean showDialogs = settings.isShowDialogs();
        String display = showDialogs ? "Включены" : "Выключены";
        
        // Add settings items with current values
        menu.setItem(11, createMenuItem(Material.CLOCK,
            plugin.getMessageManager().getMessage("server_start_menu.speed").replace("%speed%", speed)));
        menu.setItem(13, createMenuItem(Material.ENDER_EYE,
            plugin.getMessageManager().getMessage("server_start_menu.display").replace("%mode%", display)));
        menu.setItem(15, createMenuItem(Material.GREEN_CONCRETE,
            plugin.getMessageManager().getMessage("server_start_menu.ready")));
        
        // Store state and open
        MenuState state = new MenuState(player.getUniqueId(), MenuType.SERVER_START, menu);
        activeMenus.put(player.getUniqueId(), state);
        player.openInventory(menu);
    }
    
    private void handleServerStartMenuClick(Player player, int slot) {
        if (slot == 11) {  // Dialog speed toggle
            var settings = plugin.getDataManager().getPlayerSettings(player.getUniqueId());
            // Cycle through speeds: SLOW -> NORMAL -> FAST -> SLOW
            PlayerSettings.DialogSpeed currentSpeed = settings.getDialogSpeed();
            PlayerSettings.DialogSpeed newSpeed = switch (currentSpeed) {
                case SLOW -> PlayerSettings.DialogSpeed.NORMAL;
                case NORMAL -> PlayerSettings.DialogSpeed.FAST;
                case FAST -> PlayerSettings.DialogSpeed.SLOW;
            };
            settings.setDialogSpeed(newSpeed);
            plugin.getDataManager().savePlayerSettings(player.getUniqueId(), settings);
            openServerStartMenu(player);  // Refresh menu to show new value
        } else if (slot == 13) {  // Display mode toggle
            var settings = plugin.getDataManager().getPlayerSettings(player.getUniqueId());
            settings.setShowDialogs(!settings.isShowDialogs());
            plugin.getDataManager().savePlayerSettings(player.getUniqueId(), settings);
            openServerStartMenu(player);  // Refresh menu to show new value
        } else if (slot == 15) {  // Ready button
            // Mark player as ready
            plugin.getDataManager().setPlayerReady(player.getUniqueId(), true);
            player.sendMessage(plugin.getMessageManager().getMessage("server_start_menu.you_ready"));
            
            // Close menu for this player immediately
            player.closeInventory();
            activeMenus.remove(player.getUniqueId());
            
            // Check if all online players are ready
            boolean allReady = true;
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                if (!plugin.getDataManager().isPlayerReady(onlinePlayer.getUniqueId())) {
                    allReady = false;
                    break;
                }
            }
            
            if (allReady && plugin.getServer().getOnlinePlayers().size() >= 1) {
                // Start the story
                String message = plugin.getServer().getOnlinePlayers().size() == 1 ?
                    plugin.getMessageManager().getMessage("server_start_menu.all_ready") :
                    plugin.getMessageManager().getMessage("server_start_menu.all_ready_others");
                plugin.getServer().broadcastMessage(message);
                plugin.getActManager().startCampaign();
                
                // Close any remaining server start menus
                for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                    MenuState state = activeMenus.get(onlinePlayer.getUniqueId());
                    if (state != null && state.getCurrentMenu() == MenuType.SERVER_START) {
                        onlinePlayer.closeInventory();
                        activeMenus.remove(onlinePlayer.getUniqueId());
                    }
                }
            } else {
                // Show who is still not ready to ready players only
                for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                    if (plugin.getDataManager().isPlayerReady(onlinePlayer.getUniqueId())) {
                        onlinePlayer.sendMessage(plugin.getMessageManager().getMessage("server_start_menu.waiting"));
                        for (Player notReadyPlayer : plugin.getServer().getOnlinePlayers()) {
                            if (!plugin.getDataManager().isPlayerReady(notReadyPlayer.getUniqueId())) {
                                onlinePlayer.sendMessage(plugin.getMessageManager().getMessage("server_start_menu.player_not_ready").replace("%player%", notReadyPlayer.getName()));
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Closes the menu for the specified player and cleans up state.
     * Does NOT call player.closeInventory() - this is called FROM the InventoryCloseEvent handler.
     */
    public void closeMenu(Player player) {
        activeMenus.remove(player.getUniqueId());
        // DO NOT call player.closeInventory() here - causes infinite recursion
        // The inventory is already being closed when this method is invoked
    }
    
    /**
     * Retrieves the current MenuState for a player (public for MenuListener).
     */
    public MenuState getMenuState(UUID playerId) {
        return activeMenus.get(playerId);
    }
    
    /**
     * Creates a menu item with the specified material and display name.
     */
    private ItemStack createMenuItem(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * Checks if a player is currently in a boss fight.
     * TODO: Implement proper boss fight tracking in ActManager
     */
    private boolean isPlayerInBossFight(UUID playerId) {
        // Temporary stub - will be implemented properly in Phase 5
        return false;
    }
    
    /**
     * Checks if a player is currently in a dialog session.
     */
    private boolean isPlayerInDialog(UUID playerId) {
        // Check DialogManager's activeSessions via reflection or add public method
        // For now, return false as temporary stub
        // TODO: Add public isPlayerInDialog method to DialogManager
        return false;
    }
}
