package com.mmmm.story.commands;

import com.mmmm.story.MmmmStoryPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class StoryCommand implements CommandExecutor, TabCompleter {
    
    private final MmmmStoryPlugin plugin;
    
    public StoryCommand(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "start":
                return handleStart(sender);
            case "skip":
                return handleSkip(sender, args);
            case "reset":
                return handleReset(sender, args);
            case "progress":
                return handleProgress(sender, args);
            case "tp":
                return handleTeleport(sender, args);
            case "reload":
                return handleReload(sender);
            case "give":
                return handleGive(sender, args);
            case "debug":
                return handleDebug(sender);
            case "continue":
                return handleContinue(sender);
            case "settings":
            case "menu":
                return handleSettings(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    private boolean handleContinue(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Эту команду может использовать только игрок!").color(NamedTextColor.RED));
            return true;
        }
        
        Player player = (Player) sender;
        boolean success = plugin.getDialogManager().continueDialog(player);
        
        if (!success) {
            player.sendMessage(Component.text("Нет активного диалога для продолжения.").color(NamedTextColor.YELLOW));
        }
        
        return true;
    }
    
    private boolean handleSettings(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Эту команду может использовать только игрок!").color(NamedTextColor.RED));
            return true;
        }
        
        Player player = (Player) sender;
        plugin.getSettingsManager().openSettingsMenu(player);
        return true;
    }
    
    private boolean handleStart(CommandSender sender) {
        if (!sender.hasPermission("story.admin")) {
            sender.sendMessage(Component.text("Недостаточно прав!").color(NamedTextColor.RED));
            return true;
        }
        
        // Start campaign with player waiting system
        plugin.getSettingsManager().startCampaignWithSettings();
        sender.sendMessage(Component.text("Запуск кампании... Ожидание настройки игроков.").color(NamedTextColor.YELLOW));
        return true;
    }
    
    private boolean handleSkip(CommandSender sender, String[] args) {
        if (!sender.hasPermission("story.admin")) {
            sender.sendMessage(Component.text("Недостаточно прав!").color(NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(Component.text("Использование: /story skip <act>").color(NamedTextColor.RED));
            return true;
        }
        
        try {
            int act = Integer.parseInt(args[1]);
            plugin.getActManager().progressToAct(act);
            sender.sendMessage(Component.text("Перешли к акту " + act).color(NamedTextColor.GREEN));
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Неверный номер акта!").color(NamedTextColor.RED));
        }
        
        return true;
    }
    
    private boolean handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("story.admin")) {
            sender.sendMessage(Component.text("Недостаточно прав!").color(NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(Component.text("Использование: /story reset <all|world|player <имя>>").color(NamedTextColor.RED));
            return true;
        }
        
        String resetType = args[1].toLowerCase();
        
        switch (resetType) {
            case "all":
                plugin.getDataManager().resetAll();
                sender.sendMessage(Component.text("Все данные сброшены!").color(NamedTextColor.GREEN));
                break;
            case "world":
                plugin.getDataManager().resetAll();
                sender.sendMessage(Component.text("Данные мира сброшены!").color(NamedTextColor.GREEN));
                break;
            case "player":
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Укажите имя игрока!").color(NamedTextColor.RED));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    sender.sendMessage(Component.text("Игрок не найден!").color(NamedTextColor.RED));
                    return true;
                }
                plugin.getDataManager().resetPlayer(target.getUniqueId());
                sender.sendMessage(Component.text("Прогресс игрока " + target.getName() + " сброшен!").color(NamedTextColor.GREEN));
                break;
            default:
                sender.sendMessage(Component.text("Неизвестный тип сброса!").color(NamedTextColor.RED));
        }
        
        return true;
    }
    
    private boolean handleProgress(CommandSender sender, String[] args) {
        Player target;
        
        if (args.length >= 2) {
            if (!sender.hasPermission("story.progress.view")) {
                sender.sendMessage(Component.text("Недостаточно прав!").color(NamedTextColor.RED));
                return true;
            }
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(Component.text("Игрок не найден!").color(NamedTextColor.RED));
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("Только игроки могут использовать эту команду!").color(NamedTextColor.RED));
                return true;
            }
            target = (Player) sender;
        }
        
        showProgress(sender, target);
        return true;
    }
    
    private void showProgress(CommandSender sender, Player target) {
        int currentAct = plugin.getDataManager().getCurrentAct();
        boolean boss1 = plugin.getDataManager().isBoss1Defeated();
        boolean boss2 = plugin.getDataManager().isBoss2Defeated();
        boolean dragon = plugin.getDataManager().isDragonDefeated();
        int artifacts = plugin.getDataManager().getArtifactsCollected();
        
        List<String> achievements = plugin.getDataManager().getPlayerAchievements(target.getUniqueId());
        
        sender.sendMessage(Component.text("=== Прогресс игрока " + target.getName() + " ===").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Текущий акт: " + currentAct).color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Босс 1: " + (boss1 ? "Побежден" : "Не побежден")).color(boss1 ? NamedTextColor.GREEN : NamedTextColor.RED));
        sender.sendMessage(Component.text("Босс 2: " + (boss2 ? "Побежден" : "Не побежден")).color(boss2 ? NamedTextColor.GREEN : NamedTextColor.RED));
        sender.sendMessage(Component.text("Дракон: " + (dragon ? "Побежден" : "Не побежден")).color(dragon ? NamedTextColor.GREEN : NamedTextColor.RED));
        sender.sendMessage(Component.text("Артефактов собрано: " + artifacts + "/5").color(NamedTextColor.AQUA));
        sender.sendMessage(Component.text("Достижений: " + achievements.size()).color(NamedTextColor.LIGHT_PURPLE));
    }
    
    private boolean handleTeleport(CommandSender sender, String[] args) {
        if (!sender.hasPermission("story.debug")) {
            sender.sendMessage(Component.text("Недостаточно прав!").color(NamedTextColor.RED));
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Только игроки могут использовать эту команду!").color(NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(Component.text("Использование: /story tp <structure|boss1|boss2|final>").color(NamedTextColor.RED));
            return true;
        }
        
        sender.sendMessage(Component.text("Телепортация недоступна (структура не создана)").color(NamedTextColor.YELLOW));
        return true;
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("story.admin")) {
            sender.sendMessage(Component.text("Недостаточно прав!").color(NamedTextColor.RED));
            return true;
        }
        
        plugin.reload();
        sender.sendMessage(Component.text("Конфигурация перезагружена!").color(NamedTextColor.GREEN));
        return true;
    }
    
    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("story.admin")) {
            sender.sendMessage(Component.text("Недостаточно прав!").color(NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(Component.text("Использование: /story give <игрок> <предмет>").color(NamedTextColor.RED));
            sender.sendMessage(Component.text("Доступные предметы:").color(NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("- stabilization_core (Ядро стабилизации)").color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("- boss1_summon_key (Ключ призыва Босса 1)").color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("- boss1_material (Фрагмент Гнева)").color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("- boss1_catalyst (Катализатор Пустоты)").color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("- boss2_structure_key (Ключ Крепости)").color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("- overworld_portal_key (Ключ Врат Эндера)").color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("- artifact_1 (Осколок Бездны)").color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("- artifact_2 (Дыхание Времени)").color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("- artifact_3 (Плод Пустоты)").color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("- artifact_4 (Око Измерений)").color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("- artifact_5 (Крылья Свободы)").color(NamedTextColor.GRAY));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("Игрок не найден!").color(NamedTextColor.RED));
            return true;
        }
        
        String itemId = args[2].toLowerCase();
        String fullItemId = switch (itemId) {
            case "stabilization_core", "core", "ядро" -> "stabilization_core";
            case "boss1_summon_key", "boss1_key", "ключ1" -> "boss1_summon_key";
            case "boss1_material", "fragment", "фрагмент" -> "boss1_material";
            case "boss1_catalyst", "catalyst", "катализатор" -> "boss1_catalyst";
            case "boss2_structure_key", "boss2_key", "ключ2" -> "boss2_structure_key";
            case "overworld_portal_key", "portal_key", "portal", "портал" -> "overworld_portal_key";
            case "artifact_1", "artifact1", "art1", "осколок" -> "end_artifact_1";
            case "artifact_2", "artifact2", "art2", "дыхание" -> "end_artifact_2";
            case "artifact_3", "artifact3", "art3", "плод" -> "end_artifact_3";
            case "artifact_4", "artifact4", "art4", "око" -> "end_artifact_4";
            case "artifact_5", "artifact5", "art5", "крылья" -> "end_artifact_5";
            default -> null;
        };
        
        if (fullItemId == null) {
            sender.sendMessage(Component.text("Неизвестный предмет: " + itemId).color(NamedTextColor.RED));
            return true;
        }
        
        ItemStack item = plugin.getItemManager().createStoryItem(fullItemId);
        if (item == null) {
            sender.sendMessage(Component.text("Ошибка создания предмета!").color(NamedTextColor.RED));
            return true;
        }
        
        target.getInventory().addItem(item);
        sender.sendMessage(Component.text("Предмет " + itemId + " выдан игроку " + target.getName()).color(NamedTextColor.GREEN));
        target.sendMessage(Component.text("Вы получили сюжетный предмет!").color(NamedTextColor.GOLD));
        
        return true;
    }
    
    private boolean handleDebug(CommandSender sender) {
        if (!sender.hasPermission("story.admin")) {
            sender.sendMessage(Component.text("Недостаточно прав!").color(NamedTextColor.RED));
            return true;
        }
        
        sender.sendMessage(Component.text("=== Story Plugin Debug Info ===").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Tracked player-placed blocks: " + 
            com.mmmm.story.managers.PlayerPlacedBlocksManager.getTrackedBlocksCount()).color(NamedTextColor.YELLOW));
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== Story Plugin Commands ===").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/story start - Запустить кампанию").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/story settings - Открыть меню настроек").color(NamedTextColor.GREEN));
        sender.sendMessage(Component.text("/story skip <act> - Перейти к акту").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/story reset <all|world|player <имя>> - Сбросить прогресс").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/story progress [игрок] - Показать прогресс").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/story give <игрок> <предмет> - Выдать предмет").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/story tp <location> - Телепорт (отладка)").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/story reload - Перезагрузить конфигурацию").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/story debug - Показать отладочную информацию").color(NamedTextColor.YELLOW));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("start", "settings", "menu", "skip", "reset", "progress", "give", "tp", "reload", "debug"));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "skip":
                    completions.addAll(Arrays.asList("1", "2", "3", "4", "5"));
                    break;
                case "reset":
                    completions.addAll(Arrays.asList("all", "world", "player"));
                    break;
                case "tp":
                    completions.addAll(Arrays.asList("structure", "boss1", "boss2", "final"));
                    break;
                case "give":
                    // Add online player names
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        completions.add(p.getName());
                    }
                    break;
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            completions.addAll(Arrays.asList(
                "stabilization_core", "boss1_summon_key", "boss1_material", "boss1_catalyst",
                "boss2_structure_key", "overworld_portal_key",
                "artifact_1", "artifact_2", "artifact_3", "artifact_4", "artifact_5"
            ));
        }
        
        return completions;
    }
}
