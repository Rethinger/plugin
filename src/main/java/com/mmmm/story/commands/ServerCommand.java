package com.mmmm.story.commands;

import com.mmmm.story.MmmmStoryPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ServerCommand implements CommandExecutor {
    
    private final MmmmStoryPlugin plugin;
    
    public ServerCommand(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Использование: /server start").color(NamedTextColor.RED));
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "start":
                return handleStart(sender);
            default:
                sender.sendMessage(Component.text("Использование: /server start").color(NamedTextColor.RED));
                return true;
        }
    }
    
    private boolean handleStart(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Эту команду может использовать только игрок!").color(NamedTextColor.RED));
            return true;
        }
        
        Player player = (Player) sender;
        plugin.getMenuManager().openServerStartMenu(player);
        return true;
    }
}