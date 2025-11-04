package com.mmmm.story.managers;

import com.mmmm.story.MmmmStoryPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;

public class StructureManager {
    
    private final MmmmStoryPlugin plugin;
    
    public StructureManager(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void placeStructure(String structureName, Location location) {
        plugin.getLogger().info("Placing structure: " + structureName + " at " + 
            location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
        
        switch (structureName.toLowerCase()) {
            case "forgotten_altar":
                buildForgottenAltar(location);
                break;
            case "crossroads_node":
                buildCrossroadsNode(location);
                break;
            case "boss1_arena":
                buildBoss1Arena(location);
                break;
            case "boss2_arena":
                buildBoss2Arena(location);
                break;
            case "end_portal_frame":
                buildEndPortalFrame(location);
                break;
            default:
                // Default marker
                location.getBlock().setType(Material.GOLD_BLOCK);
                break;
        }
    }
    
    private void buildForgottenAltar(Location center) {
        World world = center.getWorld();
        
        // Platform 7x7
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                world.getBlockAt(center.getBlockX() + x, center.getBlockY(), center.getBlockZ() + z)
                    .setType(Material.STONE_BRICKS);
            }
        }
        
        // Central altar 3x3x2
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                world.getBlockAt(center.getBlockX() + x, center.getBlockY() + 1, center.getBlockZ() + z)
                    .setType(Material.CHISELED_STONE_BRICKS);
            }
        }
        
        // Center block - Gold
        world.getBlockAt(center.getBlockX(), center.getBlockY() + 2, center.getBlockZ())
            .setType(Material.GOLD_BLOCK);
        
        // Corner pillars
        int[][] corners = {{-3, -3}, {-3, 3}, {3, -3}, {3, 3}};
        for (int[] corner : corners) {
            for (int y = 0; y < 3; y++) {
                world.getBlockAt(center.getBlockX() + corner[0], center.getBlockY() + y, center.getBlockZ() + corner[1])
                    .setType(Material.CRACKED_STONE_BRICKS);
            }
        }
        
        // Sign
        Block signBlock = world.getBlockAt(center.getBlockX(), center.getBlockY() + 3, center.getBlockZ());
        signBlock.setType(Material.OAK_WALL_SIGN);
        if (signBlock.getState() instanceof Sign sign) {
            sign.setLine(0, plugin.getMessageManager().getMessage("structures.forgotten_altar.line1"));
            sign.setLine(1, plugin.getMessageManager().getMessage("structures.forgotten_altar.line2"));
            sign.setLine(2, plugin.getMessageManager().getMessage("structures.forgotten_altar.line3"));
            sign.update();
        }
    }
    
    private void buildCrossroadsNode(Location center) {
        World world = center.getWorld();
        
        // Platform 5x5
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                world.getBlockAt(center.getBlockX() + x, center.getBlockY(), center.getBlockZ() + z)
                    .setType(Material.MOSSY_COBBLESTONE);
            }
        }
        
        // Center obsidian platform (for dropping the core)
        world.getBlockAt(center.getBlockX(), center.getBlockY(), center.getBlockZ())
            .setType(Material.OBSIDIAN);
        
        // Center pillar
        for (int y = 1; y <= 3; y++) {
            world.getBlockAt(center.getBlockX(), center.getBlockY() + y, center.getBlockZ())
                .setType(Material.OBSIDIAN);
        }
        
        // Diamond block on top
        world.getBlockAt(center.getBlockX(), center.getBlockY() + 4, center.getBlockZ())
            .setType(Material.DIAMOND_BLOCK);
        
        // 4 direction markers
        world.getBlockAt(center.getBlockX() + 2, center.getBlockY() + 1, center.getBlockZ())
            .setType(Material.GLOWSTONE);
        world.getBlockAt(center.getBlockX() - 2, center.getBlockY() + 1, center.getBlockZ())
            .setType(Material.GLOWSTONE);
        world.getBlockAt(center.getBlockX(), center.getBlockY() + 1, center.getBlockZ() + 2)
            .setType(Material.GLOWSTONE);
        world.getBlockAt(center.getBlockX(), center.getBlockY() + 1, center.getBlockZ() - 2)
            .setType(Material.GLOWSTONE);
        
        // Sign
        Block signBlock = world.getBlockAt(center.getBlockX(), center.getBlockY() + 5, center.getBlockZ());
        signBlock.setType(Material.OAK_WALL_SIGN);
        if (signBlock.getState() instanceof Sign sign) {
            sign.setLine(0, "§b§l[УЗЕЛ]");
            sign.setLine(1, "§7Перекрёсток");
            sign.setLine(2, "§5Бросьте Ядро");
            sign.setLine(3, "§5на Обсидиан");
            sign.update();
        }
    }
    
    private void buildBoss1Arena(Location center) {
        World world = center.getWorld();
        
        // Arena platform 15x15
        for (int x = -7; x <= 7; x++) {
            for (int z = -7; z <= 7; z++) {
                world.getBlockAt(center.getBlockX() + x, center.getBlockY(), center.getBlockZ() + z)
                    .setType(Material.BLACKSTONE);
            }
        }
        
        // Border walls
        for (int x = -7; x <= 7; x++) {
            for (int y = 1; y <= 3; y++) {
                world.getBlockAt(center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() - 7)
                    .setType(Material.POLISHED_BLACKSTONE_BRICKS);
                world.getBlockAt(center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() + 7)
                    .setType(Material.POLISHED_BLACKSTONE_BRICKS);
            }
        }
        for (int z = -7; z <= 7; z++) {
            for (int y = 1; y <= 3; y++) {
                world.getBlockAt(center.getBlockX() - 7, center.getBlockY() + y, center.getBlockZ() + z)
                    .setType(Material.POLISHED_BLACKSTONE_BRICKS);
                world.getBlockAt(center.getBlockX() + 7, center.getBlockY() + y, center.getBlockZ() + z)
                    .setType(Material.POLISHED_BLACKSTONE_BRICKS);
            }
        }
        
        // Boss summon pillar
        for (int y = 1; y <= 2; y++) {
            world.getBlockAt(center.getBlockX(), center.getBlockY() + y, center.getBlockZ())
                .setType(Material.NETHERITE_BLOCK);
        }
        world.getBlockAt(center.getBlockX(), center.getBlockY() + 3, center.getBlockZ())
            .setType(Material.REDSTONE_BLOCK);
        
        // Sign
        Block signBlock = world.getBlockAt(center.getBlockX(), center.getBlockY() + 4, center.getBlockZ());
        signBlock.setType(Material.OAK_WALL_SIGN);
        if (signBlock.getState() instanceof Sign sign) {
            sign.setLine(0, plugin.getMessageManager().getMessage("structures.boss1_arena.line1"));
            sign.setLine(1, plugin.getMessageManager().getMessage("structures.boss1_arena.line2"));
            sign.setLine(2, plugin.getMessageManager().getMessage("structures.boss1_arena.line3"));
            sign.update();
        }
    }
    
    private void buildBoss2Arena(Location center) {
        World world = center.getWorld();
        
        // End stone platform 20x20
        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                world.getBlockAt(center.getBlockX() + x, center.getBlockY(), center.getBlockZ() + z)
                    .setType(Material.END_STONE);
            }
        }
        
        // Center obsidian pillar
        for (int y = 1; y <= 4; y++) {
            world.getBlockAt(center.getBlockX(), center.getBlockY() + y, center.getBlockZ())
                .setType(Material.OBSIDIAN);
        }
        
        // Dragon egg on top
        world.getBlockAt(center.getBlockX(), center.getBlockY() + 5, center.getBlockZ())
            .setType(Material.DRAGON_EGG);
        
        // Corner pillars
        int[][] corners = {{-8, -8}, {-8, 8}, {8, -8}, {8, 8}};
        for (int[] corner : corners) {
            for (int y = 1; y <= 6; y++) {
                world.getBlockAt(center.getBlockX() + corner[0], center.getBlockY() + y, center.getBlockZ() + corner[1])
                    .setType(Material.PURPUR_PILLAR);
            }
            world.getBlockAt(center.getBlockX() + corner[0], center.getBlockY() + 7, center.getBlockZ() + corner[1])
                .setType(Material.END_ROD);
        }
        
        // Sign
        Block signBlock = world.getBlockAt(center.getBlockX(), center.getBlockY() + 6, center.getBlockZ());
        signBlock.setType(Material.OAK_WALL_SIGN);
        if (signBlock.getState() instanceof Sign sign) {
            sign.setLine(0, plugin.getMessageManager().getMessage("structures.boss2_arena.line1"));
            sign.setLine(1, plugin.getMessageManager().getMessage("structures.boss2_arena.line2"));
            sign.setLine(2, plugin.getMessageManager().getMessage("structures.boss2_arena.line3"));
            sign.update();
        }
    }
    
    private void buildEndPortalFrame(Location center) {
        World world = center.getWorld();
        
        // Platform
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                world.getBlockAt(center.getBlockX() + x, center.getBlockY(), center.getBlockZ() + z)
                    .setType(Material.END_STONE_BRICKS);
            }
        }
        
        // Portal frame 3x3
        world.getBlockAt(center.getBlockX() - 1, center.getBlockY() + 1, center.getBlockZ() - 1)
            .setType(Material.END_PORTAL_FRAME);
        world.getBlockAt(center.getBlockX(), center.getBlockY() + 1, center.getBlockZ() - 1)
            .setType(Material.END_PORTAL_FRAME);
        world.getBlockAt(center.getBlockX() + 1, center.getBlockY() + 1, center.getBlockZ() - 1)
            .setType(Material.END_PORTAL_FRAME);
        
        world.getBlockAt(center.getBlockX() - 1, center.getBlockY() + 1, center.getBlockZ() + 1)
            .setType(Material.END_PORTAL_FRAME);
        world.getBlockAt(center.getBlockX(), center.getBlockY() + 1, center.getBlockZ() + 1)
            .setType(Material.END_PORTAL_FRAME);
        world.getBlockAt(center.getBlockX() + 1, center.getBlockY() + 1, center.getBlockZ() + 1)
            .setType(Material.END_PORTAL_FRAME);
        
        world.getBlockAt(center.getBlockX() - 1, center.getBlockY() + 1, center.getBlockZ())
            .setType(Material.END_PORTAL_FRAME);
        world.getBlockAt(center.getBlockX() + 1, center.getBlockY() + 1, center.getBlockZ())
            .setType(Material.END_PORTAL_FRAME);
        
        // Beacon in center
        world.getBlockAt(center.getBlockX(), center.getBlockY() + 1, center.getBlockZ())
            .setType(Material.BEACON);
        
        // Sign
        Block signBlock = world.getBlockAt(center.getBlockX() + 2, center.getBlockY() + 2, center.getBlockZ());
        signBlock.setType(Material.OAK_WALL_SIGN);
        if (signBlock.getState() instanceof Sign sign) {
            sign.setLine(0, plugin.getMessageManager().getMessage("structures.end_portal_frame.line1"));
            sign.setLine(1, plugin.getMessageManager().getMessage("structures.end_portal_frame.line2"));
            sign.setLine(2, plugin.getMessageManager().getMessage("structures.end_portal_frame.line3"));
            sign.update();
        }
    }
    
    public Location findSafeLocation(World world, Location center, int radius) {
        // Find a safe ground location within radius
        for (int attempt = 0; attempt < 10; attempt++) {
            int x = center.getBlockX() + (int) ((Math.random() - 0.5) * radius * 2);
            int z = center.getBlockZ() + (int) ((Math.random() - 0.5) * radius * 2);
            
            // Find highest solid block
            int y = world.getHighestBlockYAt(x, z);
            Location loc = new Location(world, x, y, z);
            
            // Check if location is safe
            if (isSafeLocation(loc)) {
                return loc;
            }
        }
        
        return center;
    }
    
    private boolean isSafeLocation(Location location) {
        Material blockType = location.getBlock().getType();
        return blockType.isSolid() && 
               !blockType.equals(Material.LAVA) && 
               !blockType.equals(Material.WATER);
    }
    
    public void createPortalFrame(Location center, int width, int height, Material frameMaterial) {
        World world = center.getWorld();
        int startX = center.getBlockX() - width / 2;
        int startZ = center.getBlockZ() - width / 2;
        int startY = center.getBlockY();
        
        // Create frame outline
        for (int x = 0; x <= width; x++) {
            for (int z = 0; z <= width; z++) {
                for (int y = 0; y <= height; y++) {
                    Location loc = new Location(world, startX + x, startY + y, startZ + z);
                    
                    // Only place blocks on the edges
                    if (x == 0 || x == width || z == 0 || z == width || y == 0 || y == height) {
                        loc.getBlock().setType(frameMaterial);
                    } else if (y == 0) {
                        // Floor
                        loc.getBlock().setType(Material.END_STONE);
                    } else {
                        // Clear interior
                        loc.getBlock().setType(Material.AIR);
                    }
                }
            }
        }
    }
}
