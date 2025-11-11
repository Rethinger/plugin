package com.mmmm.story.managers;

import com.mmmm.story.MmmmStoryPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    
    private final MmmmStoryPlugin plugin;
    private FileConfiguration messages;
    private final Map<String, FileConfiguration> configCache = new HashMap<>();
    
    public MessageManager(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }
    
    private void loadMessages() {
        // Load messages
        messages = loadMessageFile("messages.yml");
        
        // Load English messages if available
        FileConfiguration messagesEn = loadMessageFile("messages_en.yml");
        if (messagesEn != null) {
            configCache.put("en", messagesEn);
        }
        
        // Cache for quick access
        configCache.put("ru", messages);
    }
    
    private FileConfiguration loadMessageFile(String filename) {
        File file = new File(plugin.getDataFolder(), filename);
        
        // Create file from resource if doesn't exist
        if (!file.exists()) {
            plugin.saveResource(filename, false);
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        // Load defaults from resource
        InputStream defConfigStream = plugin.getResource(filename);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)
            );
            config.setDefaults(defConfig);
        }
        
        return config;
    }
    
    /**
     * Get message for player's language
     */
    public String getMessage(Player player, String path) {
        // Get player's preferred language (for future use)
        String playerLang = getPlayerLanguage(player);
        
        // Try to get message in player's language first, fallback to Russian
        FileConfiguration langConfig = configCache.getOrDefault(playerLang, messages);
        String message = langConfig.getString(path);
        
        // Final fallback - return raw key if not found
        if (message == null) {
            plugin.getLogger().warning("Missing localization key: " + path + " (language: " + playerLang + ")");
            return path;
        }
        
        return message;
    }
    
    /**
     * Get message for specific language
     */
    public String getMessage(String lang, String path) {
        FileConfiguration config = configCache.getOrDefault(lang, messages);
        String message = config.getString(path);
        
        // Final fallback - return raw key with warning
        if (message == null) {
            plugin.getLogger().warning("Missing localization key: " + path);
            return path; // Return raw key instead of error message
        }
        
        return message;
    }
    
    /**
     * Get message with replacements
     */
    public String getMessage(Player player, String path, Map<String, String> placeholders) {
        String message = getMessage(player, path);
        if (message == null) return "Message not found: " + path;
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return message;
    }

    public String getMessage(String messageKey) {
        // Since only Russian is supported, always return Russian
        return messages.getString(messageKey);
    }

    public String getMessage(String lang, String messageKey, Map<String, String> placeholders) {
        // Since only Russian is supported, ignore lang and return Russian
        String message = messages.getString(messageKey);
        if (message == null) return "Message not found: " + messageKey;
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return message;
    }
    
    /**
     * Get message list (e.g., for item lore) for player's language
     */
    public java.util.List<String> getMessageList(Player player, String path) {
        String lang = getPlayerLanguage(player);
        return getMessageList(lang, path);
    }
    
    /**
     * Get message list (e.g., for item lore) for specific language
     */
    public java.util.List<String> getMessageList(String lang, String path) {
        FileConfiguration config = configCache.getOrDefault(lang, messages);
        java.util.List<String> list = config.getStringList(path);
        
        // Final fallback - return empty list
        if (list == null) {
            return new java.util.ArrayList<>();
        }
        
        return list;
    }
    
    /**
     * Get player's language based on client locale
     */
    public String getPlayerLanguage(Player player) {
        // Since only Russian is supported, always return "ru"
        return "ru";
    }
    
    /**
     * Send message to player in their language
     * @param player Player to send message to
     * @param path Message key path
     */
    public void sendMessage(Player player, String path) {
        String message = getMessage(player, path);
        player.sendMessage(message);
    }
    
    /**
     * Send message with replacements to player
     * @param player Player to send message to
     * @param path Message key path
     * @param replacements Placeholder replacements
     */
    public void sendMessage(Player player, String path, Map<String, String> replacements) {
        String message = getMessage(player, path, replacements);
        player.sendMessage(message);
    }
    
    /**
     * Reload all message files
     */
    public void reload() {
        configCache.clear();
        loadMessages();
    }
}
