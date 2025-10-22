package com.mmmm.story.managers;

import com.mmmm.story.MmmmStoryPlugin;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NPCManager {
    
    private final MmmmStoryPlugin plugin;
    private final Map<UUID, ArmorStand> npcEntities = new HashMap<>();
    
    public NPCManager(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void spawnMessenger(Location location) {
        // Spawn NPC armor stand
        ArmorStand npc = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        npc.setCustomName("§6Посланник");
        npc.setCustomNameVisible(true);
        npc.setVisible(true);
        npc.setGravity(false);
        npc.setInvulnerable(true);
        npc.setBasePlate(false);
        npc.setArms(true);
        
        npcEntities.put(npc.getUniqueId(), npc);
        
        // Play dialog
        plugin.getDialogManager().playDialogForAll("messenger.spawn");
        
        // Schedule despawn
        int despawnDelay = plugin.getConfigManager().getConfig().getInt("npc.despawnAfterSeconds", 10);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            removeNPC(npc.getUniqueId());
        }, (despawnDelay + 15) * 20L); // 15 is the last dialog timing
    }
    
    public void removeNPC(UUID npcId) {
        ArmorStand npc = npcEntities.remove(npcId);
        if (npc != null && !npc.isDead()) {
            npc.remove();
        }
    }
    
    public void cleanup() {
        for (ArmorStand npc : npcEntities.values()) {
            if (!npc.isDead()) {
                npc.remove();
            }
        }
        npcEntities.clear();
    }
    
    public void giveDirectionMarker(Player player, Location target) {
        // Give compass pointing to target
        // For now, just send a message
        player.sendMessage(Component.text("§6Вы получили метку направления! (Действует 5 минут)"));
        
        // TODO: Implement boss bar or compass tracking
    }
}
