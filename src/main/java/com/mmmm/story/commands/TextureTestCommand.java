package com.mmmm.story.commands;

import com.mmmm.story.MmmmStoryPlugin;
import com.mmmm.story.managers.ItemManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TextureTestCommand implements CommandExecutor {

    private final MmmmStoryPlugin plugin;

    public TextureTestCommand(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        // Тестируем простое создание предмета через плагин
        ItemStack item = plugin.getItemManager().createStoryItem("stabilization_core");
        if (item != null) {
            player.getInventory().addItem(item);
            player.sendMessage("§aЯдро Стабилизации выдано! Проверьте текстуру.");
            player.sendMessage("§eMaterial: " + item.getType());
            player.sendMessage("§eCustomModelData: " + item.getItemMeta().getCustomModelData());
        } else {
            player.sendMessage("§cОшибка создания предмета!");
        }

        // Также тестируем другие предметы
        ItemStack keyItem = plugin.getItemManager().createStoryItem("act1_skeleton_key");
        if (keyItem != null) {
            player.getInventory().addItem(keyItem);
            player.sendMessage("§aСкелетный Ключ выдан!");
            player.sendMessage("§eMaterial: " + keyItem.getType());
            player.sendMessage("§eCustomModelData: " + keyItem.getItemMeta().getCustomModelData());
        }

        return true;
    }
}