package com.mmmm.story.listeners;

import com.mmmm.story.MmmmStoryPlugin;
import com.mmmm.story.managers.ItemManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Act4Listener implements Listener {
    
    private final MmmmStoryPlugin plugin;
    private final Set<String> processedCities = new HashSet<>();
    private int artifactsPlaced = 0;
    
    public Act4Listener(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
        loadPlacedArtifacts();
    }
    
    private void loadPlacedArtifacts() {
        if (plugin.getDataManager().getGlobalData().contains("artifacts.placed_cities")) {
            List<String> cities = plugin.getDataManager().getGlobalData().getStringList("artifacts.placed_cities");
            processedCities.addAll(cities);
            artifactsPlaced = processedCities.size();
        }
    }
    
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!plugin.getDataManager().isDragonDefeated()) {
            return;
        }
        
        if (artifactsPlaced >= 5) {
            return;
        }
        
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();
        
        if (world.getEnvironment() != World.Environment.THE_END) {
            return;
        }
        
        // Check if this chunk contains an End City
        // This is a simplified check - in production you'd scan for End City structures
        if (containsEndCity(chunk)) {
            String cityKey = world.getName() + "_" + chunk.getX() + "_" + chunk.getZ();
            
            if (!processedCities.contains(cityKey)) {
                placeArtifactInCity(chunk, cityKey);
            }
        }
    }
    
    private boolean containsEndCity(Chunk chunk) {
        // Simple heuristic: look for purpur blocks
        // In production, use structure detection or WorldEdit API
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 50; y < 100; y++) {
                    Material type = chunk.getBlock(x, y, z).getType();
                    if (type == Material.PURPUR_BLOCK || type == Material.PURPUR_PILLAR) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private void placeArtifactInCity(Chunk chunk, String cityKey) {
        if (artifactsPlaced >= 5) {
            return;
        }
        
        artifactsPlaced++;
        processedCities.add(cityKey);
        
        // Save to data
        plugin.getDataManager().getGlobalData().set("artifacts.placed_cities", new ArrayList<>(processedCities));
        plugin.getDataManager().save();
        
        // Find a chest or create one in the city
        Location chestLoc = findOrCreateChestInChunk(chunk);
        
        if (chestLoc != null) {
            Chest chest = (Chest) chestLoc.getBlock().getState();
            Inventory inv = chest.getInventory();
            
            // Place artifact
            ItemStack artifact = plugin.getItemManager().createStoryItem("end_artifact_" + artifactsPlaced);
            inv.addItem(artifact);
            
            plugin.getLogger().info("Placed artifact " + artifactsPlaced + "/5 in End City at " + 
                chestLoc.getBlockX() + ", " + chestLoc.getBlockY() + ", " + chestLoc.getBlockZ());
        }
    }
    
    private Location findOrCreateChestInChunk(Chunk chunk) {
        // Look for existing chest
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 50; y < 100; y++) {
                    Location loc = chunk.getBlock(x, y, z).getLocation();
                    if (loc.getBlock().getType() == Material.CHEST) {
                        return loc;
                    }
                }
            }
        }
        
        // Create new chest on solid ground
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 50; y < 100; y++) {
                    Location loc = chunk.getBlock(x, y, z).getLocation();
                    if (loc.getBlock().getType().isSolid() && 
                        loc.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) {
                        Location chestLoc = loc.clone().add(0, 1, 0);
                        chestLoc.getBlock().setType(Material.CHEST);
                        return chestLoc;
                    }
                }
            }
        }
        
        return null;
    }
}
