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
    private FileConfiguration messagesRu;
    private FileConfiguration messagesEn;
    private final Map<String, FileConfiguration> configCache = new HashMap<>();
    
    public MessageManager(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }
    
    private void loadMessages() {
        // Load Russian messages
        messagesRu = loadMessageFile("messages.yml");
        
        // Load English messages
        messagesEn = loadMessageFile("messages_en.yml");
        
        // Cache for quick access
        configCache.put("ru", messagesRu);
        configCache.put("en", messagesEn);
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
        String lang = getPlayerLanguage(player);
        return getMessage(lang, path);
    }
    
    /**
     * Get message for specific language
     */
    public String getMessage(String lang, String path) {
        FileConfiguration config = configCache.getOrDefault(lang, messagesRu);
        String message = config.getString(path);
        
        // Fallback to English if not found in requested language
        if (message == null && !"en".equals(lang)) {
            message = messagesEn.getString(path);
        }
        
        // Fallback to Russian if not found in English
        if (message == null && !"ru".equals(lang)) {
            message = messagesRu.getString(path);
        }
        
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
    public String getMessage(Player player, String path, Map<String, String> replacements) {
        String message = getMessage(player, path);
        
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
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
        FileConfiguration config = configCache.getOrDefault(lang, messagesRu);
        java.util.List<String> list = config.getStringList(path);
        
        // Fallback to Russian if not found
        if ((list == null || list.isEmpty()) && !"ru".equals(lang)) {
            list = messagesRu.getStringList(path);
        }
        
        // Final fallback - return empty list
        if (list == null) {
            return new java.util.ArrayList<>();
        }
        
        return list;
    }
    
    /**
     * Get player's language based on client locale
     */
    private String getPlayerLanguage(Player player) {
        String locale = player.locale().toString().toLowerCase();
        
        // Russian locales
        if (locale.startsWith("ru")) {
            return "ru";
        }
        
        // Default to English
        return "en";
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
