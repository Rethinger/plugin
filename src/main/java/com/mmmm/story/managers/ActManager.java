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
        
        // Show Act 1 title to all online players
        showActTitle(1);
        
        // Spawn messenger NPC
        plugin.getServer().getWorlds().forEach(world -> {
            if (world.getEnvironment() == org.bukkit.World.Environment.NORMAL) {
                Location spawn = world.getSpawnLocation();
                plugin.getNPCManager().spawnMessenger(spawn);
            }
        });
        
        plugin.getLogger().info("Story campaign started!");
    }
    
    public void progressToAct(int act) {
        if (act < 1 || act > 5) {
            plugin.getLogger().warning("Invalid act number: " + act);
            return;
        }
        
        plugin.getDataManager().setCurrentAct(act);
        plugin.getLogger().info("Progressed to Act " + act);
        
        // Show title to all online players
        showActTitle(act);
        
        // Broadcast to players
        plugin.getDialogManager().playDialogForAll("act." + act + ".start");
    }
    
    private void showActTitle(int act) {
        String[] actNames = {
            "",
            "",
            "",
            "",
            "",
            ""
        };
        
        String[] actSubtitles = {
            "",
            "",
            "",
            "",
            "",
            ""
        };
        
        if (act < 1 || act > 5) return;
        
        Component title = Component.text(actNames[act]);
        Component subtitle = Component.text("§7" + actSubtitles[act]);
        
        Title actTitle = Title.title(
            title,
            subtitle,
            Title.Times.times(
                Duration.ofMillis(1000),
                Duration.ofMillis(5000),
                Duration.ofMillis(1000)
            )
        );
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(actTitle);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            player.sendMessage("§8§m                                                    ");
            player.sendMessage("");
            player.sendMessage("§8» " + actNames[act]);
            player.sendMessage("§8» §7" + actSubtitles[act]);
            player.sendMessage("");
            player.sendMessage("§8§m                                                    ");
        }
    }
    
    public void enableNetherPortals() {
        plugin.getDataManager().setNetherEnabled(true);
        plugin.getLogger().info("Nether portals enabled!");
    }
    
    public void enableEndPortals() {
        plugin.getDataManager().setEndEnabled(true);
        plugin.getLogger().info("End portals enabled!");
    }
}
