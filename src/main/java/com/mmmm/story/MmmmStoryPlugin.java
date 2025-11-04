package com.mmmm.story;

import com.mmmm.story.commands.StoryCommand;
import com.mmmm.story.commands.ServerCommand;
import com.mmmm.story.listeners.*;
import com.mmmm.story.managers.*;
import de.eisi05.npc.api.NpcApi;
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
    private MenuManager menuManager;
    private MessageManager messageManager;
    private Act1Listener act1Listener;
    
    @Override
    public void onEnable() {
        instance = this;
        
        getLogger().info("=== Mmmm Story Plugin ===");
        getLogger().info("Initializing story campaign...");
        
        try {
            // Initialize NPC API
            NpcApi.createInstance(this);
            getLogger().info("NPC API initialized");
            
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
            messageManager = new MessageManager(this);
            menuManager = new MenuManager(this, messageManager, actManager, dialogManager);
            
            // Register commands
            getCommand("story").setExecutor(new StoryCommand(this));
            getCommand("server").setExecutor(new ServerCommand(this));
            
            // Register event listeners
            registerListeners();
            
            // Start auto-save task (every 5 minutes)
            getServer().getScheduler().runTaskTimer(this, () -> {
                dataManager.save();
            }, 6000L, 6000L);
            
            getLogger().info(getMessageManager().getMessage("log.plugin_enabled"));
            
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
        
        getLogger().info(getMessageManager().getMessage("log.plugin_disabled"));
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
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuClickListener(this), this);
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
    
    public NPCManager getNpcManager() {
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
    
    public MenuManager getMenuManager() {
        return menuManager;
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
}
