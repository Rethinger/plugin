package com.mmmm.story.managers;

import com.mmmm.story.MmmmStoryPlugin;
import com.mmmm.story.data.PlayerSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Manages player settings GUI menu
 */
public class SettingsManager implements Listener {
    
    private final MmmmStoryPlugin plugin;
    private final Map<UUID, Inventory> openMenus = new HashMap<>();
    private boolean waitingForPlayers = false;
    private int checkTaskId = -1;
    
    // Slot positions
    private static final int DIALOGS_SLOT = 11;
    private static final int LANGUAGE_SLOT = 13;
    private static final int SPEED_SLOT = 15;
    private static final int CONFIRM_SLOT = 22;
    
    public SettingsManager(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Open settings menu for player
     */
    public void openSettingsMenu(Player player) {
        PlayerSettings settings = plugin.getDataManager().getPlayerSettings(player.getUniqueId());
        
        Inventory inv = Bukkit.createInventory(null, 27, 
            Component.text("⚙ Настройки Кампании ⚙").color(NamedTextColor.GOLD));
        
        // Dialog display setting
        inv.setItem(DIALOGS_SLOT, createDialogsItem(settings.isShowDialogs()));
        
        // Language setting
        inv.setItem(LANGUAGE_SLOT, createLanguageItem(settings.getLanguage()));
        
        // Speed setting
        inv.setItem(SPEED_SLOT, createSpeedItem(settings.getDialogSpeed()));
        
        // Confirm button
        inv.setItem(CONFIRM_SLOT, createConfirmItem());
        
        // Fill empty slots with glass pane
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.displayName(Component.text(" "));
        filler.setItemMeta(fillerMeta);
        
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }
        
        openMenus.put(player.getUniqueId(), inv);
        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }
    
    private ItemStack createDialogsItem(boolean showDialogs) {
        ItemStack item = new ItemStack(showDialogs ? Material.BOOK : Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        
        if (showDialogs) {
            meta.displayName(Component.text("💬 Показывать диалоги: ")
                .color(NamedTextColor.GREEN)
                .append(Component.text("ДА").color(NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        } else {
            meta.displayName(Component.text("💬 Показывать диалоги: ")
                .color(NamedTextColor.RED)
                .append(Component.text("НЕТ").color(NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        }
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Отображать сюжетные диалоги")
            .color(NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("и повествовательный текст")
            .color(NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("▶ Нажмите для переключения")
            .color(NamedTextColor.YELLOW)
            .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createLanguageItem(String language) {
        ItemStack item = new ItemStack(language.equals("ru") ? Material.PURPLE_BANNER : Material.LIGHT_BLUE_BANNER);
        ItemMeta meta = item.getItemMeta();
        
        String langName = language.equals("ru") ? "Русский 🇷🇺" : "English 🇬🇧";
        meta.displayName(Component.text("🌐 Язык: ")
            .color(NamedTextColor.AQUA)
            .append(Component.text(langName).color(NamedTextColor.WHITE))
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Язык диалогов кампании")
            .color(NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        if (language.equals("ru")) {
            lore.add(Component.text("Текущий: Русский")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text("Current: English")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.empty());
        lore.add(Component.text("▶ Нажмите для переключения")
            .color(NamedTextColor.YELLOW)
            .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createSpeedItem(PlayerSettings.DialogSpeed speed) {
        Material material = switch (speed) {
            case SLOW -> Material.TURTLE_EGG;
            case NORMAL -> Material.CLOCK;
            case FAST -> Material.FEATHER;
        };
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        String speedName = switch (speed) {
            case SLOW -> "Медленно";
            case NORMAL -> "Нормально";
            case FAST -> "Быстро";
        };
        
        NamedTextColor color = switch (speed) {
            case SLOW -> NamedTextColor.BLUE;
            case NORMAL -> NamedTextColor.GREEN;
            case FAST -> NamedTextColor.GOLD;
        };
        
        meta.displayName(Component.text("⏱ Скорость диалогов: ")
            .color(NamedTextColor.LIGHT_PURPLE)
            .append(Component.text(speedName).color(color))
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Скорость воспроизведения диалогов")
            .color(NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("❄ Медленно - для неспешного чтения")
            .color(speed == PlayerSettings.DialogSpeed.SLOW ? NamedTextColor.AQUA : NamedTextColor.DARK_GRAY)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("⚖ Нормально - сбалансированная скорость")
            .color(speed == PlayerSettings.DialogSpeed.NORMAL ? NamedTextColor.GREEN : NamedTextColor.DARK_GRAY)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("⚡ Быстро - для опытных игроков")
            .color(speed == PlayerSettings.DialogSpeed.FAST ? NamedTextColor.GOLD : NamedTextColor.DARK_GRAY)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("▶ Нажмите для переключения")
            .color(NamedTextColor.YELLOW)
            .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createConfirmItem() {
        ItemStack item = new ItemStack(Material.LIME_WOOL);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("✓ Сохранить и продолжить")
            .color(NamedTextColor.GREEN)
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Сохранить настройки и начать игру")
            .color(NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("▶ Нажмите для подтверждения")
            .color(NamedTextColor.YELLOW)
            .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        if (!openMenus.containsKey(playerId)) {
            return;
        }
        
        event.setCancelled(true);
        
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) {
            return;
        }
        
        PlayerSettings settings = plugin.getDataManager().getPlayerSettings(playerId);
        
        switch (slot) {
            case DIALOGS_SLOT -> {
                settings.toggleDialogs();
                event.getInventory().setItem(DIALOGS_SLOT, createDialogsItem(settings.isShowDialogs()));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            case LANGUAGE_SLOT -> {
                settings.toggleLanguage();
                event.getInventory().setItem(LANGUAGE_SLOT, createLanguageItem(settings.getLanguage()));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            case SPEED_SLOT -> {
                settings.cycleSpeed();
                event.getInventory().setItem(SPEED_SLOT, createSpeedItem(settings.getDialogSpeed()));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            case CONFIRM_SLOT -> {
                // Save settings
                plugin.getDataManager().savePlayerSettings(playerId, settings);
                plugin.getDataManager().markSettingsConfigured(playerId);
                
                openMenus.remove(playerId);
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                player.sendMessage(Component.text("✓ Настройки сохранены!").color(NamedTextColor.GREEN));
                
                // Check if all players are ready (if waiting for campaign start)
                if (waitingForPlayers) {
                    checkAllPlayersReady();
                }
            }
        }
        
        // Update settings in memory
        plugin.getDataManager().savePlayerSettings(playerId, settings);
    }
    
    /**
     * Clean up when inventory is closed
     */
    @EventHandler
    public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            openMenus.remove(player.getUniqueId());
        }
    }
    
    // ==========================================
    // CAMPAIGN START WITH PLAYER WAITING
    // ==========================================
    
    /**
     * Start campaign with settings menu for all players
     */
    public void startCampaignWithSettings() {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        
        if (onlinePlayers.isEmpty()) {
            plugin.getLogger().warning("No players online to start campaign!");
            return;
        }
        
        waitingForPlayers = true;
        
        // Open settings menu for all players who haven't configured yet
        for (Player player : onlinePlayers) {
            if (!plugin.getDataManager().hasConfiguredSettings(player.getUniqueId())) {
                openSettingsMenu(player);
                player.sendMessage(Component.text("═══════════════════════════════").color(NamedTextColor.GOLD));
                player.sendMessage(Component.text("").append(
                    Component.text("⚙ ", NamedTextColor.GOLD)
                    .append(Component.text("НАСТРОЙКА КАМПАНИИ", NamedTextColor.YELLOW, TextDecoration.BOLD))
                ));
                player.sendMessage(Component.empty());
                player.sendMessage(Component.text("Настройте свои предпочтения").color(NamedTextColor.GRAY));
                player.sendMessage(Component.text("перед началом сюжетной кампании").color(NamedTextColor.GRAY));
                player.sendMessage(Component.text("═══════════════════════════════").color(NamedTextColor.GOLD));
            }
        }
        
        // Start checking task
        startWaitingTask();
    }
    
    private void startWaitingTask() {
        // Cancel previous task if exists
        if (checkTaskId != -1) {
            Bukkit.getScheduler().cancelTask(checkTaskId);
        }
        
        // Check every second (20 ticks)
        checkTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            checkAllPlayersReady();
        }, 20L, 20L).getTaskId();
    }
    
    private void checkAllPlayersReady() {
        if (!waitingForPlayers) {
            return;
        }
        
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        List<Player> notReady = new ArrayList<>();
        
        for (Player player : onlinePlayers) {
            if (!plugin.getDataManager().hasConfiguredSettings(player.getUniqueId())) {
                notReady.add(player);
            }
        }
        
        if (notReady.isEmpty()) {
            // All players are ready!
            startCampaignNow();
        } else {
            // Still waiting - send periodic message
            Component waitingMsg = Component.text("⏳ Ожидание настройки игроков... (", NamedTextColor.YELLOW)
                .append(Component.text(notReady.size(), NamedTextColor.RED))
                .append(Component.text(" осталось)", NamedTextColor.YELLOW));
            
            for (Player player : onlinePlayers) {
                if (plugin.getDataManager().hasConfiguredSettings(player.getUniqueId())) {
                    player.sendActionBar(waitingMsg);
                }
            }
        }
    }
    
    private void startCampaignNow() {
        waitingForPlayers = false;
        
        // Cancel checking task
        if (checkTaskId != -1) {
            Bukkit.getScheduler().cancelTask(checkTaskId);
            checkTaskId = -1;
        }
        
        // Announce to all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("═══════════════════════════════").color(NamedTextColor.GOLD));
            player.sendMessage(Component.text("").append(
                Component.text("✦ ", NamedTextColor.LIGHT_PURPLE)
                .append(Component.text("ВСЕ ИГРОКИ ГОТОВЫ!", NamedTextColor.GREEN, TextDecoration.BOLD))
            ));
            player.sendMessage(Component.text("Сюжетная кампания начинается...").color(NamedTextColor.YELLOW));
            player.sendMessage(Component.text("═══════════════════════════════").color(NamedTextColor.GOLD));
            player.sendMessage(Component.empty());
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
        
        // Start campaign after short delay
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getActManager().startCampaign();
        }, 40L); // 2 seconds delay
    }
    
    /**
     * Cancel waiting for players
     */
    public void cancelWaiting() {
        if (waitingForPlayers) {
            waitingForPlayers = false;
            if (checkTaskId != -1) {
                Bukkit.getScheduler().cancelTask(checkTaskId);
                checkTaskId = -1;
            }
        }
    }
}
