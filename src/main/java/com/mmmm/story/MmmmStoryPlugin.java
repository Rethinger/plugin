package com.mmmm.story;

import com.mmmm.story.commands.StoryCommand;
import com.mmmm.story.listeners.*;
import com.mmmm.story.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class MmmmStoryPlugin extends JavaPlugin {
    
    private static MmmmStoryPlugin instance;
    
    private ConfigManager configManager;
    private DataManager dataManager;
    private ItemManager itemManager;
    private NPCManager npcManager;
    private ActManager actManager;
    private DialogManager dialogManager;
    private StructureManager structureManager;
    private Act1Listener act1Listener;
    
    @Override
    public void onEnable() {
        instance = this;
        
        getLogger().info("=== Mmmm Story Plugin ===");
        getLogger().info("Initializing story campaign...");
        
        try {
            // Initialize managers
            configManager = new ConfigManager(this);
            configManager.loadAll();
            
            dataManager = new DataManager(this);
            dataManager.load();
            
            itemManager = new ItemManager(this);
            dialogManager = new DialogManager(this);
            structureManager = new StructureManager(this);
            npcManager = new NPCManager(this);
            actManager = new ActManager(this);
            
            // Register commands
            getCommand("story").setExecutor(new StoryCommand(this));
            
            // Register event listeners
            registerListeners();
            
            // Start auto-save task (every 5 minutes)
            getServer().getScheduler().runTaskTimer(this, () -> {
                dataManager.save();
            }, 6000L, 6000L);
            
            getLogger().info("Story plugin enabled successfully!");
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize plugin!", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Saving data...");
        
        if (dataManager != null) {
            dataManager.save();
        }
        
        if (npcManager != null) {
            npcManager.cleanup();
        }
        
        getLogger().info("Story plugin disabled.");
    }
    
    private void registerListeners() {
        act1Listener = new Act1Listener(this);
        getServer().getPluginManager().registerEvents(act1Listener, this);
        getServer().getPluginManager().registerEvents(new Act2Listener(this), this);
        getServer().getPluginManager().registerEvents(new Act3Listener(this), this);
        // Act4Listener disabled - artifacts only through chest search, not auto-spawn
        // getServer().getPluginManager().registerEvents(new Act4Listener(this), this);
        getServer().getPluginManager().registerEvents(new Act5Listener(this), this);
        getServer().getPluginManager().registerEvents(new PortalListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new MobListener(this), this);
        getServer().getPluginManager().registerEvents(new ChestSpawnManager(this), this);
        getServer().getPluginManager().registerEvents(new StoryItemProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockTrackingListener(), this);
    }
    
    public void reload() {
        configManager.loadAll();
        getLogger().info("Configuration reloaded.");
    }
    
    // Getters
    public static MmmmStoryPlugin getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }
    
    public ItemManager getItemManager() {
        return itemManager;
    }
    
    public NPCManager getNPCManager() {
        return npcManager;
    }
    
    public ActManager getActManager() {
        return actManager;
    }
    
    public DialogManager getDialogManager() {
        return dialogManager;
    }
    
    public StructureManager getStructureManager() {
        return structureManager;
    }
    
    public Act1Listener getAct1Listener() {
        return act1Listener;
    }
}
