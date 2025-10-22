package com.mmmm.story.managers;

import com.mmmm.story.MmmmStoryPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ItemManager {
    
    private final MmmmStoryPlugin plugin;
    private final NamespacedKey storyItemKey;
    
    // Item IDs
    public static final String STABILIZATION_CORE = "stabilization_core";
    public static final String BOSS1_MATERIAL = "boss1_material";
    public static final String BOSS1_CATALYST = "boss1_catalyst";
    public static final String BOSS1_SUMMON_KEY = "boss1_summon_key";
    public static final String BOSS2_STRUCTURE_KEY = "boss2_structure_key";
    public static final String OVERWORLD_PORTAL_KEY = "overworld_portal_key";
    public static final String END_ARTIFACT_1 = "end_artifact_1";
    public static final String END_ARTIFACT_2 = "end_artifact_2";
    public static final String END_ARTIFACT_3 = "end_artifact_3";
    public static final String END_ARTIFACT_4 = "end_artifact_4";
    public static final String END_ARTIFACT_5 = "end_artifact_5";
    
    public ItemManager(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
        this.storyItemKey = new NamespacedKey(plugin, "storyItem");
    }
    
    public ItemStack createStoryItem(String itemId) {
        return switch (itemId) {
            case STABILIZATION_CORE -> createStabilizationCore();
            case BOSS1_MATERIAL -> createBoss1Material();
            case BOSS1_CATALYST -> createBoss1Catalyst();
            case BOSS1_SUMMON_KEY -> createBoss1SummonKey();
            case BOSS2_STRUCTURE_KEY -> createBoss2StructureKey();
            case OVERWORLD_PORTAL_KEY -> createOverworldPortalKey();
            case END_ARTIFACT_1 -> createEndArtifact(1);
            case END_ARTIFACT_2 -> createEndArtifact(2);
            case END_ARTIFACT_3 -> createEndArtifact(3);
            case END_ARTIFACT_4 -> createEndArtifact(4);
            case END_ARTIFACT_5 -> createEndArtifact(5);
            default -> null;
        };
    }
    
    private ItemStack createStabilizationCore() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("Ядро Стабилизации")
                .color(NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Древний артефакт, способный")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("стабилизировать портальные связи")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("✦ Появляется в сундуках")
                .color(NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("разрушенных порталов")
                .color(NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("▶ Выбросьте (Q) на блок Обсидиана")
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("для активации порталов в Ад")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        
        meta.getPersistentDataContainer().set(storyItemKey, PersistentDataType.STRING, STABILIZATION_CORE);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private ItemStack createBoss1Material() {
        ItemStack item = new ItemStack(Material.NETHERITE_SCRAP);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("Фрагмент Гнева")
                .color(NamedTextColor.DARK_RED)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Материал, пропитанный яростью")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("побеждённого Повелителя Скелетов")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("▶ Соедините с Катализатором Пустоты")
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("на блоке Древних Обломков")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("для призыва Стража Края")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        
        meta.getPersistentDataContainer().set(storyItemKey, PersistentDataType.STRING, BOSS1_MATERIAL);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private ItemStack createBoss1Catalyst() {
        ItemStack item = new ItemStack(Material.NETHERITE_INGOT);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("Катализатор Пустоты")
                .color(NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Загадочный катализатор из")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("сокровищниц Бастиона")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("▶ Соедините с Фрагментом Гнева")
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("на блоке Древних Обломков")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("для призыва Стража Края")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        
        meta.getPersistentDataContainer().set(storyItemKey, PersistentDataType.STRING, BOSS1_CATALYST);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private ItemStack createBoss1SummonKey() {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("Ключ Призыва")
                .color(NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Призывает Повелителя Скелетов")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("▶ Выбросьте (Q) на блок")
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Древних Обломков для призыва босса")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        
        meta.getPersistentDataContainer().set(storyItemKey, PersistentDataType.STRING, BOSS1_SUMMON_KEY);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private ItemStack createBoss2StructureKey() {
        ItemStack item = new ItemStack(Material.NETHERITE_INGOT);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("Ключ Призыва Босса II")
                .color(NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Призывает Стража Края")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("▶ Выбросьте (Q) на Яйцо Дракона")
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("в Арене Босса 2 для призыва")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        
        meta.getPersistentDataContainer().set(storyItemKey, PersistentDataType.STRING, BOSS2_STRUCTURE_KEY);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private ItemStack createOverworldPortalKey() {
        ItemStack item = new ItemStack(Material.END_CRYSTAL);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("Ключ Врат Эндера")
                .color(NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Этот ключ призывает")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("врата в Эндер Мир")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("▶ Выбросьте (Q) на Маяк")
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("для открытия портала")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        
        meta.getPersistentDataContainer().set(storyItemKey, PersistentDataType.STRING, OVERWORLD_PORTAL_KEY);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private ItemStack createEndArtifact(int number) {
        Material material = switch (number) {
            case 1 -> Material.ECHO_SHARD;
            case 2 -> Material.DRAGON_BREATH;
            case 3 -> Material.CHORUS_FRUIT;
            case 4 -> Material.ENDER_EYE;
            case 5 -> Material.ELYTRA;
            default -> Material.ENDER_PEARL;
        };
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        String[] names = {
            "Осколок Бездны",
            "Дыхание Времени",
            "Плод Пустоты",
            "Око Измерений",
            "Крылья Свободы"
        };
        
        meta.displayName(Component.text(names[number - 1])
                .color(NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Древний артефакт " + number + " из 5")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Необходим для финального ритуала")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("▶ Поместите все 5 артефактов")
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("в сундуки у центра Края (0, 0)")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("для активации финального ритуала")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        
        String artifactId = "end_artifact_" + number;
        meta.getPersistentDataContainer().set(storyItemKey, PersistentDataType.STRING, artifactId);
        item.setItemMeta(meta);
        
        return item;
    }
    
    public boolean isStoryItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        return item.getItemMeta().getPersistentDataContainer().has(storyItemKey, PersistentDataType.STRING);
    }
    
    public String getStoryItemId(ItemStack item) {
        if (!isStoryItem(item)) {
            return null;
        }
        
        return item.getItemMeta().getPersistentDataContainer().get(storyItemKey, PersistentDataType.STRING);
    }
    
    public boolean isEndArtifact(ItemStack item) {
        String id = getStoryItemId(item);
        return id != null && id.startsWith("end_artifact_");
    }
    
    public int getArtifactNumber(ItemStack item) {
        String id = getStoryItemId(item);
        if (id != null && id.startsWith("end_artifact_")) {
            try {
                return Integer.parseInt(id.substring(13));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
}
