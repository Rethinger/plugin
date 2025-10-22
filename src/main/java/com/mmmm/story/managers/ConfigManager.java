package com.mmmm.story.managers;

import com.mmmm.story.MmmmStoryPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    
    private final MmmmStoryPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration dialogs;
    private FileConfiguration sounds;
    private FileConfiguration messages;
    
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    
    public ConfigManager(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void loadAll() {
        // Save default configs if they don't exist
        plugin.saveDefaultConfig();
        saveResourceIfNotExists("dialogs.yml");
        saveResourceIfNotExists("sounds.yml");
        saveResourceIfNotExists("messages_ru.yml");
        
        // Load configurations
        config = plugin.getConfig();
        dialogs = loadConfig("dialogs.yml");
        sounds = loadConfig("sounds.yml");
        messages = loadConfig("messages_ru.yml");
        
        configs.put("config", config);
        configs.put("dialogs", dialogs);
        configs.put("sounds", sounds);
        configs.put("messages", messages);
    }
    
    private void saveResourceIfNotExists(String resourcePath) {
        File file = new File(plugin.getDataFolder(), resourcePath);
        if (!file.exists()) {
            plugin.saveResource(resourcePath, false);
        }
    }
    
    private FileConfiguration loadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.getLogger().warning("Configuration file " + fileName + " not found!");
            return new YamlConfiguration();
        }
        return YamlConfiguration.loadConfiguration(file);
    }
    
    public void reloadConfig(String configName) {
        if (configName.equals("config")) {
            plugin.reloadConfig();
            config = plugin.getConfig();
        } else {
            FileConfiguration cfg = loadConfig(configName + ".yml");
            configs.put(configName, cfg);
            
            switch (configName) {
                case "dialogs" -> dialogs = cfg;
                case "sounds" -> sounds = cfg;
                case "messages" -> messages = cfg;
            }
        }
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
    
    public FileConfiguration getDialogs() {
        return dialogs;
    }
    
    public FileConfiguration getSounds() {
        return sounds;
    }
    
    public FileConfiguration getMessages() {
        return messages;
    }
    
    public String getMessage(String key, Map<String, String> placeholders) {
        String message = messages.getString(key, key);
        
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        
        return message.replace("&", "§");
    }
    
    public String getMessage(String key) {
        return getMessage(key, null);
    }
}
