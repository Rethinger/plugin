package com.mmmm.story.listeners;

import com.mmmm.story.MmmmStoryPlugin;
import com.mmmm.story.managers.ItemManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class PortalListener implements Listener {
    
    private final MmmmStoryPlugin plugin;
    
    public PortalListener(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPortalKeyDrop(org.bukkit.event.entity.ItemSpawnEvent event) {
        if (plugin.getDataManager().getCurrentAct() < 1) {
            return;
        }
        
        org.bukkit.entity.Item droppedItem = event.getEntity();
        ItemStack itemStack = droppedItem.getItemStack();
        
        // Check if it's Portal Key
        if (!plugin.getItemManager().isStoryItem(itemStack)) {
            return;
        }
        
        if (!ItemManager.OVERWORLD_PORTAL_KEY.equals(plugin.getItemManager().getStoryItemId(itemStack))) {
            return;
        }
        
        // Schedule check for Beacon below
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!droppedItem.isValid() || droppedItem.isDead()) {
                    cancel();
                    return;
                }
                
                Location itemLoc = droppedItem.getLocation();
                Location blockBelow = itemLoc.clone().subtract(0, 1, 0);
                
                // Check if item is on Beacon
                if (blockBelow.getBlock().getType() == Material.BEACON) {
                    // Activate portal!
                    activateReturnPortal(droppedItem);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 10L, 5L);
    }
    
    private void activateReturnPortal(org.bukkit.entity.Item droppedItem) {
        Location location = droppedItem.getLocation();
        World world = location.getWorld();
        
        // Remove the item
        droppedItem.remove();
        
        // Destroy the beacon (consumed by ritual)
        Location beaconLoc = location.clone().subtract(0, 1, 0);
        if (beaconLoc.getBlock().getType() == Material.BEACON) {
            world.spawnParticle(Particle.EXPLOSION, beaconLoc.clone().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0);
            world.playSound(beaconLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);
            beaconLoc.getBlock().setType(Material.AIR);
        }
        
        // Effects
        world.spawnParticle(Particle.PORTAL, location, 400, 3, 3, 3, 0.3);
        world.spawnParticle(Particle.ENCHANT, location, 200, 2, 2, 2, 0.2);
        world.spawnParticle(Particle.END_ROD, location, 100, 1, 1, 1, 0.1);
        world.spawnParticle(Particle.FIREWORK, location, 50, 1.5, 1.5, 1.5, 0.15);
        world.playSound(location, Sound.BLOCK_END_PORTAL_SPAWN, 2.0f, 1.0f);
        world.playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 1.5f);
        world.playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.5f, 1.2f);
        
        // Enable overworld portal
        plugin.getDataManager().setEndEnabled(true);
        
        // Create portal structure
        createPortalStructure(location);
        
        // Light pillar
        for (int y = 0; y < 50; y++) {
            Location particleLoc = location.clone().add(0, y, 0);
            world.spawnParticle(Particle.END_ROD, particleLoc, 8, 0.4, 0.4, 0.4, 0.03);
            world.spawnParticle(Particle.PORTAL, particleLoc, 5, 0.3, 0.3, 0.3, 0.1);
        }
        
        // Notify all players
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distance(location) < 200) {
                String lang = plugin.getMessageManager().getPlayerLanguage(player);
                player.sendMessage(Component.text(plugin.getMessageManager().getMessage(lang, "portal.end_opened")).color(NamedTextColor.DARK_PURPLE));
                player.sendMessage(Component.text(plugin.getMessageManager().getMessage(lang, "portal.end_instruction")).color(NamedTextColor.GREEN));
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            }
        }
    }
    
    private void createPortalStructure(Location center) {
        World world = center.getWorld();
        
        // Create a 3x3 end portal
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location portalLoc = center.clone().add(x, 0, z);
                portalLoc.getBlock().setType(Material.END_PORTAL);
            }
        }
        
        // Add end portal frame around it
        for (int x = -2; x <= 2; x++) {
            world.getBlockAt(center.getBlockX() + x, center.getBlockY(), center.getBlockZ() - 2).setType(Material.END_PORTAL_FRAME);
            world.getBlockAt(center.getBlockX() + x, center.getBlockY(), center.getBlockZ() + 2).setType(Material.END_PORTAL_FRAME);
        }
        for (int z = -1; z <= 1; z++) {
            world.getBlockAt(center.getBlockX() - 2, center.getBlockY(), center.getBlockZ() + z).setType(Material.END_PORTAL_FRAME);
            world.getBlockAt(center.getBlockX() + 2, center.getBlockY(), center.getBlockZ() + z).setType(Material.END_PORTAL_FRAME);
        }
    }
    
    @EventHandler
    public void onNetherPortalCreate(PortalCreateEvent event) {
        if (event.getReason() != PortalCreateEvent.CreateReason.FIRE) {
            return;
        }
        
        // Check if nether is enabled
        if (!plugin.getDataManager().isNetherEnabled()) {
            event.setCancelled(true);
            
            // Play crumbling effect
            event.getBlocks().forEach(blockState -> {
                blockState.getLocation().getWorld().playSound(
                    blockState.getLocation(), 
                    Sound.BLOCK_GLASS_BREAK, 
                    1.0f, 
                    0.5f
                );
                blockState.getLocation().getWorld().spawnParticle(
                    Particle.BLOCK, 
                    blockState.getLocation(), 
                    20,
                    0.5, 0.5, 0.5, 0,
                    Material.OBSIDIAN.createBlockData()
                );
            });
            
            // Play dialog
            plugin.getDialogManager().playDialogForAll("portal.nether.blocked");
        }
    }
    
    @EventHandler
    public void onEndPortalCreate(PortalCreateEvent event) {
        // Block END portal creation until final ritual is complete
        if (event.getReason() == PortalCreateEvent.CreateReason.END_PLATFORM) {
            // Check if in The End
            if (event.getWorld().getEnvironment() == World.Environment.THE_END) {
                // Check if final ritual has been completed (Act 5)
                if (!plugin.getDataManager().isFinalRitualComplete()) {
                    event.setCancelled(true);
                    plugin.getLogger().info("Blocked END portal creation - final ritual not complete");
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerNetherPortal(PlayerPortalEvent event) {
        if (event.getCause() != PlayerPortalEvent.TeleportCause.NETHER_PORTAL) {
            return;
        }
        
        // Check if nether is enabled
        if (!plugin.getDataManager().isNetherEnabled()) {
            event.setCancelled(true);
            String lang = plugin.getMessageManager().getPlayerLanguage(event.getPlayer());
            event.getPlayer().sendMessage(Component.text(plugin.getMessageManager().getMessage(lang, "portal.unstable")));
            
            event.getPlayer().playSound(
                event.getPlayer().getLocation(),
                Sound.BLOCK_GLASS_BREAK,
                1.0f,
                0.5f
            );
        }
    }
    
    @EventHandler
    public void onPlayerEndPortal(PlayerPortalEvent event) {
        if (event.getCause() != PlayerPortalEvent.TeleportCause.END_PORTAL) {
            return;
        }
        
        // Check if end is enabled
        if (!plugin.getDataManager().isEndEnabled()) {
            event.setCancelled(true);
            String lang = plugin.getMessageManager().getPlayerLanguage(event.getPlayer());
            event.getPlayer().sendMessage(Component.text(plugin.getMessageManager().getMessage(lang, "portal.end_closed")));
            
            event.getPlayer().playSound(
                event.getPlayer().getLocation(),
                Sound.BLOCK_GLASS_BREAK,
                1.0f,
                0.5f
            );
        }
    }
    
    @EventHandler
    public void onEntityPortal(EntityPortalEvent event) {
        // Prevent mobs from using portals if not enabled
        if (event.getTo() != null && event.getTo().getWorld() != null) {
            switch (event.getTo().getWorld().getEnvironment()) {
                case NETHER:
                    if (!plugin.getDataManager().isNetherEnabled()) {
                        event.setCancelled(true);
                    }
                    break;
                case THE_END:
                    if (!plugin.getDataManager().isEndEnabled()) {
                        event.setCancelled(true);
                    }
                    break;
            }
        }
    }
}
