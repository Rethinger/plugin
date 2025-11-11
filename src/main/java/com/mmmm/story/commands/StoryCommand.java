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
            case "give":
                return handleGive(sender, args);
            case "debug":
                return handleDebug(sender, args);
            case "continue":
                return handleContinue(sender);
            case "menu":
                return handleSettings(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    private boolean handleContinue(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text(plugin.getConfigManager().getMessage("command.player_only")).color(NamedTextColor.RED));
            return true;
        }
        
        Player player = (Player) sender;
        boolean success = plugin.getDialogManager().continueDialog(player);
        
        if (!success) {
            player.sendMessage(Component.text(plugin.getConfigManager().getMessage("command.no_active_dialog")).color(NamedTextColor.YELLOW));
        }
        
        return true;
    }
    
    private boolean handleSettings(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text(plugin.getConfigManager().getMessage("command.player_only")).color(NamedTextColor.RED));
            return true;
        }
        
        Player player = (Player) sender;
        plugin.getMenuManager().openStoryMenu(player);
        return true;
    }
    
    private boolean handleStart(CommandSender sender) {
        if (!sender.hasPermission("story.admin")) {
            sender.sendMessage(Component.text(plugin.getConfigManager().getMessage("command.insufficient_permissions")).color(NamedTextColor.RED));
            return true;
        }
        
        // Check if campaign is already started
        if (plugin.getDataManager().getCurrentAct() > 1) {
            sender.sendMessage(Component.text(plugin.getConfigManager().getMessage("command.campaign_already_started")).color(NamedTextColor.RED));
            return true;
        }
        
        // Reset all players' ready status
        plugin.getDataManager().resetAllReadyStatus();
        
        // Open server start menu for all online players
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            plugin.getMenuManager().openServerStartMenu(player);
        }
        
        sender.sendMessage(Component.text(plugin.getConfigManager().getMessage("command.readiness_menu_opened")).color(NamedTextColor.GREEN));
        return true;
    }
    
    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("story.admin")) {
            sender.sendMessage(Component.text(plugin.getConfigManager().getMessage("command.insufficient_permissions")).color(NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(Component.text(plugin.getConfigManager().getMessage("command.give_usage")).color(NamedTextColor.RED));
            sender.sendMessage(Component.text(plugin.getConfigManager().getMessage("command.available_items")).color(NamedTextColor.YELLOW));
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
            sender.sendMessage(Component.text(plugin.getConfigManager().getMessage("command.player_not_found")).color(NamedTextColor.RED));
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
            sender.sendMessage(Component.text(plugin.getMessageManager().getMessage("command.unknown_item").replace("%item%", itemId)).color(NamedTextColor.RED));
            return true;
        }
        
        ItemStack item = plugin.getItemManager().createStoryItem(fullItemId);
        if (item == null) {
            sender.sendMessage(Component.text(plugin.getConfigManager().getMessage("command.item_creation_error")).color(NamedTextColor.RED));
            return true;
        }
        
        target.getInventory().addItem(item);
        sender.sendMessage(Component.text(plugin.getMessageManager().getMessage("command.item_give_success").replace("%item%", itemId).replace("%player%", target.getName())).color(NamedTextColor.GREEN));
        target.sendMessage(Component.text(plugin.getMessageManager().getMessage("command.item_give_receiver")).color(NamedTextColor.GOLD));
        
        return true;
    }
    
    private boolean handleDebug(CommandSender sender, String[] args) {
        if (!sender.hasPermission("story.admin")) {
            sender.sendMessage(Component.text(plugin.getConfigManager().getMessage("command.insufficient_permissions")).color(NamedTextColor.RED));
            return true;
        }

        // Handle force phase 2 command
        if (args.length > 1 && args[1].equalsIgnoreCase("forcephase2")) {
            return handleForcePhase2(sender);
        }

        sender.sendMessage(Component.text("=== Debug Information ===").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Tracked blocks: " + com.mmmm.story.managers.PlayerPlacedBlocksManager.getTrackedBlocksCount()).color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Debug mode is enabled - check logs for boss damage and phase transition info").color(NamedTextColor.GREEN));

        return true;
    }

    private boolean handleForcePhase2(CommandSender sender) {
        sender.sendMessage(Component.text("Force Phase 2 command disabled - please reduce boss health to 100 HP naturally").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Arrow Rain system will trigger automatically when boss reaches Phase 2").color(NamedTextColor.GREEN));
        return true;
    }

        
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text(plugin.getConfigManager().getMessage("command.help_header")).color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text(plugin.getConfigManager().getMessage("command.help_start")).color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text(plugin.getConfigManager().getMessage("command.help_menu")).color(NamedTextColor.GREEN));
        sender.sendMessage(Component.text(plugin.getConfigManager().getMessage("command.help_give")).color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text(plugin.getConfigManager().getMessage("command.help_debug")).color(NamedTextColor.YELLOW));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("start", "menu", "give", "debug"));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
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
