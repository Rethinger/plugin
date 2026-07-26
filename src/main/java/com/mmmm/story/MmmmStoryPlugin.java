package com.mmmm.story;

import com.mmmm.story.commands.ServerCommand;
import com.mmmm.story.commands.StoryCommand;
import com.mmmm.story.commands.TextureTestCommand;
import com.mmmm.story.listeners.*;
import com.mmmm.story.managers.*;
import de.eisi05.npc.api.NpcApi;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class MmmmStoryPlugin extends JavaPlugin {

    private static MmmmStoryPlugin instance;

    /** Registered components that must release runtime state on shutdown. */
    private final List<Cleanable> cleanables = new ArrayList<>();

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
            registerCommand("story", new StoryCommand(this));
            registerCommand("server", new ServerCommand(this));
            registerCommand("testtexture", new TextureTestCommand(this));

            // Register event listeners
            registerListeners();

            startAutoSaveTask();

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

        // Reverse order so components tear down opposite to how they were built.
        // One failing component must not stop the rest from cleaning up.
        List<Cleanable> reversed = new ArrayList<>(cleanables);
        Collections.reverse(reversed);
        for (Cleanable cleanable : reversed) {
            try {
                cleanable.cleanup();
            } catch (Exception e) {
                getLogger().log(Level.WARNING,
                        "Cleanup failed for " + cleanable.getClass().getSimpleName(), e);
            }
        }
        cleanables.clear();

        if (npcManager != null) {
            npcManager.cleanup();
        }

        // Deliberately not localised: messageManager is null when onEnable() failed
        // early, and an NPE here would mask the original startup error.
        getLogger().info("Mmmm Story Plugin disabled");
    }

    /**
     * Persist story data every five minutes.
     *
     * <p>Serialisation happens on the main thread (Bukkit configuration objects are
     * not thread safe); only the file writes are pushed off it.
     */
    private void startAutoSaveTask() {
        long fiveMinutes = 20L * 60L * 5L;
        getServer().getScheduler().runTaskTimer(this, () -> dataManager.saveAsync(),
                fiveMinutes, fiveMinutes);
    }

    /**
     * Bind an executor to a command declared in plugin.yml, failing loudly when the
     * two drift apart instead of throwing a bare NPE.
     */
    private void registerCommand(String name, CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().severe("Command '" + name + "' is missing from plugin.yml - not registered");
            return;
        }
        command.setExecutor(executor);
    }

    private void registerListeners() {
        act1Listener = new Act1Listener(this);
        register(act1Listener);
        register(new Act2Listener(this));
        register(new Act3Listener(this));
        if (getConfig().getBoolean("act4.autoSpawnArtifacts", false)) {
            register(new Act4Listener(this));
        }
        register(new Act5Listener(this));
        register(new PortalListener(this));
        register(new PlayerListener(this));
        register(new MobListener(this));
        register(new ChestSpawnManager(this));
        register(new StoryItemProtectionListener(this));
        register(new BlockTrackingListener(this));
        register(new PlayerJoinListener(this));
        register(new MenuClickListener(this));
    }

    /**
     * Register a listener and, when it owns runtime state, remember it for shutdown.
     */
    private void register(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
        if (listener instanceof Cleanable cleanable) {
            cleanables.add(cleanable);
        }
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
    
    public MenuManager getMenuManager() {
        return menuManager;
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
}
