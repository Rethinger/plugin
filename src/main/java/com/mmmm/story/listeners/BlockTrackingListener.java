package com.mmmm.story.listeners;

import com.mmmm.story.MmmmStoryPlugin;
import com.mmmm.story.managers.PlayerPlacedBlocksManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockTrackingListener implements Listener {

    private final MmmmStoryPlugin plugin;

    public BlockTrackingListener(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // Отслеживаем блоки только во время боя с боссом
        if (!isBossActive()) {
            return;
        }

        // Отслеживаем только блоки в режиме выживания/приключения
        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL ||
            event.getPlayer().getGameMode() == GameMode.ADVENTURE) {
            PlayerPlacedBlocksManager.markBlockAsPlayerPlaced(event.getBlock());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Убираем блок из отслеживания при разрушении (даже если босс не активен)
        PlayerPlacedBlocksManager.removeBlock(event.getBlock());
    }

    /**
     * Проверяет, активен ли босс в данный момент
     */
    private boolean isBossActive() {
        // Проверяем, не побежден ли босс
        if (plugin.getDataManager().isBoss1Defeated()) {
            return false;
        }

        // Проверяем, есть ли активный Act2Listener с боссом
        // Для этого мы можем проверить, существует ли босс сущность
        // Это немного хак, но эффективный способ проверить состояние

        // Проверяем, есть ли босс в мире (простая проверка по кастомному имени)
        // В идеале мы бы хранили ссылку на Act2Listener, но для простоты используем этот подход
        return plugin.getServer().getWorlds().stream()
            .anyMatch(world -> world.getEntitiesByClass(Skeleton.class).stream()
                .anyMatch(skeleton -> "Босс Скелетов".equals(skeleton.getCustomName())));
    }
}
