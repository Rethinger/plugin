package com.mmmm.story.listeners;

import com.mmmm.story.managers.PlayerPlacedBlocksManager;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockTrackingListener implements Listener {
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // Отслеживаем только блоки в режиме выживания/приключения
        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL || 
            event.getPlayer().getGameMode() == GameMode.ADVENTURE) {
            PlayerPlacedBlocksManager.markBlockAsPlayerPlaced(event.getBlock());
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Убираем блок из отслеживания при разрушении
        PlayerPlacedBlocksManager.removeBlock(event.getBlock());
    }
}
