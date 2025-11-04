package com.mmmm.story.managers;

import com.mmmm.story.MmmmStoryPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;

public class ActManager {
    
    private final MmmmStoryPlugin plugin;
    
    public ActManager(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void startCampaign() {
        if (plugin.getDataManager().getCurrentAct() > 1) {
            plugin.getLogger().warning("Campaign already started!");
            return;
        }
        
        plugin.getDataManager().setCurrentAct(1);
        
        // Clean up any existing NPCs before spawning new ones
        plugin.getNPCManager().cleanup();
        
        // Spawn messenger NPC
        plugin.getServer().getWorlds().forEach(world -> {
            if (world.getEnvironment() == org.bukkit.World.Environment.NORMAL) {
                Location spawn = world.getSpawnLocation();
                plugin.getNPCManager().spawnMessenger(spawn);
            }
        });
        
        plugin.getLogger().info(plugin.getMessageManager().getMessage("log.campaign_started"));
    }
    
    public void progressToAct(int act) {
        if (act < 1 || act > 5) {
            plugin.getLogger().warning("Invalid act number: " + act);
            return;
        }
        
        plugin.getDataManager().setCurrentAct(act);
        plugin.getLogger().info(plugin.getMessageManager().getMessage("log.progressed_to_act").replace("%act%", String.valueOf(act)));
        
        // Act transition is now silent
        
        // Broadcast to players
        plugin.getDialogManager().playDialogForAll("act." + act + ".start");
    }
    
    
    public void enableNetherPortals() {
        plugin.getDataManager().setNetherEnabled(true);
        plugin.getLogger().info(plugin.getMessageManager().getMessage("log.nether_portals_enabled"));
    }
    
    public void enableEndPortals() {
        plugin.getDataManager().setEndEnabled(true);
        plugin.getLogger().info(plugin.getMessageManager().getMessage("log.end_portals_enabled"));
    }
    
    /**
     * Get the status description for a specific act
     * @param act The act number (1-5)
     * @return Status description string
     */
    public String getActStatus(int act) {
        switch (act) {
            case 1:
                return "Поиск Ядра Стабилизации";
            case 2:
                return "Поиск Катализатора Пустоты";
            case 3:
                return "Битва с Повелителем Скелетов";
            case 4:
                return "Битва с Извергом Адских Глубин";
            case 5:
                return "Сбор артефактов Края";
            default:
                return "Неизвестный акт";
        }
    }
}
