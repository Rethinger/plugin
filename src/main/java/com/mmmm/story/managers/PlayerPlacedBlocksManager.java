package com.mmmm.story.managers;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

public class PlayerPlacedBlocksManager {
    private static final Set<String> playerPlacedBlocks = new HashSet<>();
    
    /**
     * Отмечает блок как поставленный игроком
     */
    public static void markBlockAsPlayerPlaced(Block block) {
        playerPlacedBlocks.add(locationToString(block.getLocation()));
    }
    
    /**
     * Проверяет, был ли блок поставлен игроком
     */
    public static boolean isPlayerPlaced(Block block) {
        return playerPlacedBlocks.contains(locationToString(block.getLocation()));
    }
    
    /**
     * Удаляет блок из отслеживания
     */
    public static void removeBlock(Block block) {
        playerPlacedBlocks.remove(locationToString(block.getLocation()));
    }
    
    /**
     * Очищает все отслеживаемые блоки
     */
    public static void clearAll() {
        playerPlacedBlocks.clear();
    }
    
    /**
     * Получает количество отслеживаемых блоков
     */
    public static int getTrackedBlocksCount() {
        return playerPlacedBlocks.size();
    }
    
    /**
     * Конвертирует Location в строку для хранения
     */
    private static String locationToString(Location loc) {
        return loc.getWorld().getName() + ":" + 
               loc.getBlockX() + ":" + 
               loc.getBlockY() + ":" + 
               loc.getBlockZ();
    }
}
