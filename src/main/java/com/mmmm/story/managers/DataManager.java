package com.mmmm.story.managers;

import com.mmmm.story.MmmmStoryPlugin;
import com.mmmm.story.data.PlayerSettings;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class DataManager {
    
    private final MmmmStoryPlugin plugin;
    private File dataFolder;
    private File globalFile;
    private File playersFolder;
    
    private FileConfiguration globalData;
    private final Map<UUID, FileConfiguration> playerData = new HashMap<>();
    
    public DataManager(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
        setupFiles();
    }
    
    private void setupFiles() {
        dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        globalFile = new File(dataFolder, "global.yml");
        playersFolder = new File(dataFolder, "players");
        
        if (!playersFolder.exists()) {
            playersFolder.mkdirs();
        }
    }
    
    public void load() {
        // Load global data
        if (!globalFile.exists()) {
            globalData = new YamlConfiguration();
            initializeGlobalData();
            saveGlobal();
        } else {
            globalData = YamlConfiguration.loadConfiguration(globalFile);
        }
    }
    
    private void initializeGlobalData() {
        globalData.set("act.current", 1);
        globalData.set("portals.nether.enabled", false);
        globalData.set("portals.end.enabled", false);
        globalData.set("bosses.boss1.defeated", false);
        globalData.set("bosses.boss2.defeated", false);
        globalData.set("dragon.defeated", false);
        globalData.set("artifacts.collected", 0);
        globalData.set("initialized", true);
    }
    
    public void save() {
        saveGlobal();
        saveAllPlayers();
    }
    
    private void saveGlobal() {
        try {
            // Backup existing file
            if (globalFile.exists()) {
                File backup = new File(dataFolder, "global.yml.backup");
                Files.copy(globalFile.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            
            globalData.save(globalFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save global data: " + e.getMessage());
        }
    }
    
    public FileConfiguration getPlayerData(UUID uuid) {
        if (!playerData.containsKey(uuid)) {
            File playerFile = new File(playersFolder, uuid.toString() + ".yml");
            FileConfiguration cfg;
            
            if (playerFile.exists()) {
                cfg = YamlConfiguration.loadConfiguration(playerFile);
            } else {
                cfg = new YamlConfiguration();
                initializePlayerData(cfg);
            }
            
            playerData.put(uuid, cfg);
        }
        
        return playerData.get(uuid);
    }
    
    private void initializePlayerData(FileConfiguration cfg) {
        cfg.set("achievements", new ArrayList<String>());
        cfg.set("firstJoin", System.currentTimeMillis());
        cfg.set("currentAct", 1); // Default to act 1 for new players
    }
    
    public void savePlayerData(UUID uuid) {
        if (!playerData.containsKey(uuid)) {
            return;
        }
        
        File playerFile = new File(playersFolder, uuid.toString() + ".yml");
        try {
            playerData.get(uuid).save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player data for " + uuid + ": " + e.getMessage());
        }
    }
    
    private void saveAllPlayers() {
        for (UUID uuid : playerData.keySet()) {
            savePlayerData(uuid);
        }
    }
    
    // Global data getters/setters
    public int getCurrentAct() {
        return globalData.getInt("act.current", 1);
    }
    
    public void setCurrentAct(int act) {
        globalData.set("act.current", act);
        saveGlobal();
    }
    
    public boolean isNetherEnabled() {
        return globalData.getBoolean("portals.nether.enabled", false);
    }
    
    public void setNetherEnabled(boolean enabled) {
        globalData.set("portals.nether.enabled", enabled);
        saveGlobal();
    }
    
    public boolean isEndEnabled() {
        return globalData.getBoolean("portals.end.enabled", false);
    }
    
    public void setEndEnabled(boolean enabled) {
        globalData.set("portals.end.enabled", enabled);
        saveGlobal();
    }
    
    public boolean isBoss1Defeated() {
        return globalData.getBoolean("bosses.boss1.defeated", false);
    }
    
    public void setBoss1Defeated(boolean defeated) {
        globalData.set("bosses.boss1.defeated", defeated);
        saveGlobal();
    }
    
    public boolean isBoss2Defeated() {
        return globalData.getBoolean("bosses.boss2.defeated", false);
    }
    
    public void setBoss2Defeated(boolean defeated) {
        globalData.set("bosses.boss2.defeated", defeated);
        saveGlobal();
    }
    
    public boolean isDragonDefeated() {
        return globalData.getBoolean("dragon.defeated", false);
    }
    
    public void setDragonDefeated(boolean defeated) {
        globalData.set("dragon.defeated", defeated);
        saveGlobal();
    }
    
    public int getArtifactsCollected() {
        return globalData.getInt("artifacts.collected", 0);
    }
    
    public void setArtifactsCollected(int count) {
        globalData.set("artifacts.collected", count);
        saveGlobal();
    }
    
    public boolean isFinalRitualComplete() {
        return globalData.getBoolean("ritual.final.complete", false);
    }
    
    public void setFinalRitualComplete(boolean complete) {
        globalData.set("ritual.final.complete", complete);
        saveGlobal();
    }

    public boolean hasPlayedDialog(String dialogId) {
        return globalData.getBoolean("dialogs.played." + dialogId, false);
    }

    public void setDialogPlayed(String dialogId, boolean played) {
        globalData.set("dialogs.played." + dialogId, played);
        saveGlobal();
    }
    
    public void saveLocation(String key, Location location) {
        globalData.set(key + ".world", location.getWorld().getName());
        globalData.set(key + ".x", location.getX());
        globalData.set(key + ".y", location.getY());
        globalData.set(key + ".z", location.getZ());
        saveGlobal();
    }
    
    public Location getLocation(String key) {
        if (!globalData.contains(key + ".world")) {
            return null;
        }
        
        String worldName = globalData.getString(key + ".world");
        double x = globalData.getDouble(key + ".x");
        double y = globalData.getDouble(key + ".y");
        double z = globalData.getDouble(key + ".z");
        
        return new Location(plugin.getServer().getWorld(worldName), x, y, z);
    }
    
    public void addPlayerAchievement(UUID uuid, String achievement) {
        FileConfiguration cfg = getPlayerData(uuid);
        List<String> achievements = cfg.getStringList("achievements");
        if (!achievements.contains(achievement)) {
            achievements.add(achievement);
            cfg.set("achievements", achievements);
            savePlayerData(uuid);
        }
    }
    
    public List<String> getPlayerAchievements(UUID uuid) {
        return getPlayerData(uuid).getStringList("achievements");
    }
    
    public void resetAll() {
        initializeGlobalData();
        saveGlobal();
        
        // Clear player data
        for (UUID uuid : playerData.keySet()) {
            initializePlayerData(playerData.get(uuid));
            savePlayerData(uuid);
        }
    }
    
    public void resetPlayer(UUID uuid) {
        FileConfiguration cfg = getPlayerData(uuid);
        initializePlayerData(cfg);
        savePlayerData(uuid);
    }
    
    public FileConfiguration getGlobalData() {
        return globalData;
    }
    
    // Generic getters/setters for custom data
    public boolean getBoolean(String key, boolean defaultValue) {
        return globalData.getBoolean(key, defaultValue);
    }
    
    public void setBoolean(String key, boolean value) {
        globalData.set(key, value);
        saveGlobal();
    }
    
    public String getString(String key, String defaultValue) {
        return globalData.getString(key, defaultValue);
    }
    
    public void setString(String key, String value) {
        globalData.set(key, value);
        saveGlobal();
    }
    
    // ==========================================
    // PLAYER SETTINGS METHODS
    // ==========================================
    
    /**
     * Get player settings, creating default if not exists
     */
    public PlayerSettings getPlayerSettings(UUID uuid) {
        FileConfiguration cfg = getPlayerData(uuid);
        
        boolean showDialogs = cfg.getBoolean("settings.showDialogs", true);
        String speedStr = cfg.getString("settings.dialogSpeed", "NORMAL");
        PlayerSettings.DialogSpeed speed = PlayerSettings.DialogSpeed.fromString(speedStr);
        
        return new PlayerSettings(showDialogs, speed);
    }
    
    /**
     * Save player settings
     */
    public void savePlayerSettings(UUID uuid, PlayerSettings settings) {
        FileConfiguration cfg = getPlayerData(uuid);
        
        cfg.set("settings.showDialogs", settings.isShowDialogs());
        cfg.set("settings.dialogSpeed", settings.getDialogSpeed().name());
        
        savePlayerData(uuid);
    }
    
    /**
     * Check if player has configured settings (for first-time setup)
     */
    public boolean hasConfiguredSettings(UUID uuid) {
        FileConfiguration cfg = getPlayerData(uuid);
        return cfg.contains("settings.configured");
    }
    
    /**
     * Mark player settings as configured
     */
    public void markSettingsConfigured(UUID uuid) {
        FileConfiguration cfg = getPlayerData(uuid);
        cfg.set("settings.configured", true);
        savePlayerData(uuid);
    }
    
    // ==========================================
    // RITUAL AND END GAME METHODS
    // ==========================================
    
    /**
     * Complete the final ritual
     */
    public void completeRitual() {
        globalData.set("ritual.final.complete", true);
        saveGlobal();
    }
    
    /**
     * Get player's original spawn location before End trap
     */
    public Location getPlayerOriginalSpawn(UUID uuid) {
        FileConfiguration cfg = getPlayerData(uuid);
        if (!cfg.contains("originalSpawn.world")) {
            return null;
        }
        
        String worldName = cfg.getString("originalSpawn.world");
        double x = cfg.getDouble("originalSpawn.x");
        double y = cfg.getDouble("originalSpawn.y");
        double z = cfg.getDouble("originalSpawn.z");
        
        return new Location(plugin.getServer().getWorld(worldName), x, y, z);
    }
    
    /**
     * Set player's original spawn location
     */
    public void setPlayerOriginalSpawn(UUID uuid, Location location) {
        FileConfiguration cfg = getPlayerData(uuid);
        cfg.set("originalSpawn.world", location.getWorld().getName());
        cfg.set("originalSpawn.x", location.getX());
        cfg.set("originalSpawn.y", location.getY());
        cfg.set("originalSpawn.z", location.getZ());
        savePlayerData(uuid);
    }
    
    /**
     * Check if player is trapped in the End
     */
    public boolean isPlayerTrappedInEnd(UUID uuid) {
        FileConfiguration cfg = getPlayerData(uuid);
        return cfg.getBoolean("trappedInEnd", false);
    }
    
    /**
     * Set player trapped in End status
     */
    public void setPlayerTrappedInEnd(UUID uuid, boolean trapped) {
        FileConfiguration cfg = getPlayerData(uuid);
        cfg.set("trappedInEnd", trapped);
        savePlayerData(uuid);
    }
    
    /**
     * Clear player's original spawn location
     */
    public void clearPlayerOriginalSpawn(UUID uuid) {
        FileConfiguration cfg = getPlayerData(uuid);
        cfg.set("originalSpawn", null);
        savePlayerData(uuid);
    }
    
    // ==========================================
    // PLAYER READY STATUS METHODS
    // ==========================================
    
    /**
     * Set player ready status
     */
    public void setPlayerReady(UUID uuid, boolean ready) {
        FileConfiguration cfg = getPlayerData(uuid);
        cfg.set("ready", ready);
        savePlayerData(uuid);
    }
    
    /**
     * Get player ready status
     */
    public boolean isPlayerReady(UUID uuid) {
        FileConfiguration cfg = getPlayerData(uuid);
        return cfg.getBoolean("ready", false);
    }
    
    /**
     * Reset all players' ready status
     */
    public void resetAllReadyStatus() {
        for (UUID uuid : playerData.keySet()) {
            FileConfiguration cfg = getPlayerData(uuid);
            cfg.set("ready", false);
            savePlayerData(uuid);
        }
    }
    
    /**
     * Get current act for a specific player
     * @param uuid Player UUID
     * @return Current act number (1-5)
     */
    public int getPlayerCurrentAct(UUID uuid) {
        FileConfiguration cfg = getPlayerData(uuid);
        return cfg.getInt("currentAct", 1);
    }
    
    /**
     * Set current act for a specific player
     * @param uuid Player UUID
     * @param act Act number (1-5)
     */
    public void setPlayerCurrentAct(UUID uuid, int act) {
        FileConfiguration cfg = getPlayerData(uuid);
        cfg.set("currentAct", act);
        savePlayerData(uuid);
    }
}
