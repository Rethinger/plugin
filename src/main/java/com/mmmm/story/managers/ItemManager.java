package com.mmmm.story.managers;

import com.mmmm.story.MmmmStoryPlugin;
// import com.mmmm.story.utils.TextureOverrideManager; // Temporarily disabled due to NMS dependency issues
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
    // private final TextureOverrideManager textureOverrideManager; // Temporarily disabled
    
    // Item IDs
    public static final String STABILIZATION_CORE = "stabilization_core";
    public static final String ACT1_SKELETON_KEY = "act1_skeleton_key";
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
        // this.textureOverrideManager = new TextureOverrideManager(plugin); // Temporarily disabled
    }
    
    public ItemStack createStoryItem(String itemId) {
        return createStoryItem(itemId, "ru"); // Default to Russian
    }
    
    public ItemStack createStoryItem(String itemId, String language) {
        ItemStack item = switch (itemId) {
            case STABILIZATION_CORE -> createStabilizationCore(language);
            case ACT1_SKELETON_KEY -> createAct1SkeletonKey(language);
            case BOSS1_MATERIAL -> createBoss1Material(language);
            case BOSS1_CATALYST -> createBoss1Catalyst(language);
            case BOSS1_SUMMON_KEY -> createBoss1SummonKey(language);
            case BOSS2_STRUCTURE_KEY -> createBoss2StructureKey(language);
            case OVERWORLD_PORTAL_KEY -> createOverworldPortalKey(language);
            case END_ARTIFACT_1 -> createEndArtifact(1, language);
            case END_ARTIFACT_2 -> createEndArtifact(2, language);
            case END_ARTIFACT_3 -> createEndArtifact(3, language);
            case END_ARTIFACT_4 -> createEndArtifact(4, language);
            case END_ARTIFACT_5 -> createEndArtifact(5, language);
            default -> null;
        };
        
        if (item != null) {
            // textureOverrideManager.applyTextureOverride(item); // Temporarily disabled due to NMS dependency issues
        }
        
        return item;
    }
    
    private ItemStack createStabilizationCore() {
        return createStabilizationCore("ru");
    }
    
    private ItemStack createStabilizationCore(String lang) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        
        String coreName = plugin.getMessageManager().getMessage(lang, "chest.items.stabilization_core.name");
        meta.displayName(Component.text(coreName)
                .color(NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        java.util.List<String> loreLines = plugin.getMessageManager().getMessageList(lang, "chest.items.stabilization_core.lore");
        for (String line : loreLines) {
            if (line.isEmpty()) {
                lore.add(Component.empty());
            } else {
                lore.add(Component.text(line)
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }
        // Apply specific colors for certain lines
        if (loreLines.size() > 2) {
            lore.set(2, Component.text(loreLines.get(2))
                    .color(NamedTextColor.DARK_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));
        }
        if (loreLines.size() > 3) {
            lore.set(3, Component.text(loreLines.get(3))
                    .color(NamedTextColor.DARK_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));
        }
        if (loreLines.size() > 4) {
            lore.set(4, Component.text(loreLines.get(4))
                    .color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
        }
        if (loreLines.size() > 5) {
            lore.set(5, Component.text(loreLines.get(5))
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
        }
        
        meta.lore(lore);
        
        setCustomModelDataAsString(meta, "1001");
        meta.getPersistentDataContainer().set(storyItemKey, PersistentDataType.STRING, STABILIZATION_CORE);
        item.setItemMeta(meta);

        return item;
    }
    
    private ItemStack createAct1SkeletonKey() {
        return createAct1SkeletonKey("ru");
    }
    
    private ItemStack createAct1SkeletonKey(String lang) {
        ItemStack item = new ItemStack(Material.BONE);
        ItemMeta meta = item.getItemMeta();
        
        String keyName = plugin.getMessageManager().getMessage(lang, "chest.items.act1_skeleton_key.name");
        meta.displayName(Component.text(keyName)
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        java.util.List<String> loreLines = plugin.getMessageManager().getMessageList(lang, "chest.items.act1_skeleton_key.lore");
        for (String line : loreLines) {
            if (line.isEmpty()) {
                lore.add(Component.empty());
            } else {
                lore.add(Component.text(line)
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }
        // Apply specific colors for certain lines
        if (loreLines.size() > 2) {
            lore.set(2, Component.text(loreLines.get(2))
                    .color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
        }
        if (loreLines.size() > 3) {
            lore.set(3, Component.text(loreLines.get(3))
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
        }
        
        meta.lore(lore);
        
        setCustomModelDataAsString(meta, "1002");
        meta.getPersistentDataContainer().set(storyItemKey, PersistentDataType.STRING, ACT1_SKELETON_KEY);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private ItemStack createBoss1Material() {
        return createBoss1Material("ru");
    }
    
    private ItemStack createBoss1Material(String lang) {
        ItemStack item = new ItemStack(Material.NETHERITE_SCRAP);
        ItemMeta meta = item.getItemMeta();
        
        String itemName = plugin.getMessageManager().getMessage(lang, "chest.items.boss1_material.name");
        meta.displayName(Component.text(itemName)
                .color(NamedTextColor.DARK_RED)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        java.util.List<String> loreLines = plugin.getMessageManager().getMessageList(lang, "chest.items.boss1_material.lore");
        for (String line : loreLines) {
            if (line.isEmpty()) {
                lore.add(Component.empty());
            } else {
                lore.add(Component.text(line)
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }
        // Apply specific colors for certain lines
        if (loreLines.size() > 2) {
            lore.set(2, Component.text(loreLines.get(2))
                    .color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
        }
        if (loreLines.size() > 3) {
            lore.set(3, Component.text(loreLines.get(3))
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
        }
        if (loreLines.size() > 4) {
            lore.set(4, Component.text(loreLines.get(4))
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);
        
        setCustomModelDataAsString(meta, "1003");
        meta.getPersistentDataContainer().set(storyItemKey, PersistentDataType.STRING, BOSS1_MATERIAL);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private ItemStack createBoss1Catalyst() {
        return createBoss1Catalyst("ru");
    }
    
    private ItemStack createBoss1Catalyst(String lang) {
        ItemStack item = new ItemStack(Material.NETHERITE_INGOT);
        ItemMeta meta = item.getItemMeta();
        
        String catalystName = plugin.getMessageManager().getMessage(lang, "chest.items.boss1_catalyst.name");
        meta.displayName(Component.text(catalystName)
                .color(NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        java.util.List<String> loreLines = plugin.getMessageManager().getMessageList(lang, "chest.items.boss1_catalyst.lore");
        for (String line : loreLines) {
            if (line.isEmpty()) {
                lore.add(Component.empty());
            } else {
                lore.add(Component.text(line)
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }
        // Apply specific colors for certain lines
        if (loreLines.size() > 2) {
            lore.set(2, Component.text(loreLines.get(2))
                    .color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
        }
        if (loreLines.size() > 3) {
            lore.set(3, Component.text(loreLines.get(3))
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
        }
        if (loreLines.size() > 4) {
            lore.set(4, Component.text(loreLines.get(4))
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
        }
        
        meta.lore(lore);
        
        setCustomModelDataAsString(meta, "1004");
        meta.getPersistentDataContainer().set(storyItemKey, PersistentDataType.STRING, BOSS1_CATALYST);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private ItemStack createBoss1SummonKey() {
        return createBoss1SummonKey("ru");
    }
    
    private ItemStack createBoss1SummonKey(String lang) {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        
        String keyName = plugin.getMessageManager().getMessage(lang, "chest.items.boss1_summon_key.name");
        meta.displayName(Component.text(keyName)
                .color(NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        java.util.List<String> loreLines = plugin.getMessageManager().getMessageList(lang, "chest.items.boss1_summon_key.lore");
        for (String line : loreLines) {
            if (line.isEmpty()) {
                lore.add(Component.empty());
            } else {
                lore.add(Component.text(line)
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }
        // Apply specific colors for certain lines
        if (loreLines.size() > 1) {
            lore.set(1, Component.text(loreLines.get(1))
                    .color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
        }
        if (loreLines.size() > 2) {
            lore.set(2, Component.text(loreLines.get(2))
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
        }
        
        meta.lore(lore);
        
        setCustomModelDataAsString(meta, "1005");
        meta.getPersistentDataContainer().set(storyItemKey, PersistentDataType.STRING, BOSS1_SUMMON_KEY);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private ItemStack createBoss2StructureKey() {
        return createBoss2StructureKey("ru");
    }
    
    private ItemStack createBoss2StructureKey(String lang) {
        ItemStack item = new ItemStack(Material.NETHERITE_INGOT);
        ItemMeta meta = item.getItemMeta();
        
        String keyName = plugin.getMessageManager().getMessage(lang, "chest.items.boss2_structure_key.name");
        meta.displayName(Component.text(keyName)
                .color(NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        java.util.List<String> loreLines = plugin.getMessageManager().getMessageList(lang, "chest.items.boss2_structure_key.lore");
        for (String line : loreLines) {
            if (line.isEmpty()) {
                lore.add(Component.empty());
            } else {
                lore.add(Component.text(line)
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }
        // Apply specific colors for certain lines
        if (loreLines.size() > 1) {
            lore.set(1, Component.text(loreLines.get(1))
                    .color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
        }
        if (loreLines.size() > 2) {
            lore.set(2, Component.text(loreLines.get(2))
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
        }
        
        meta.lore(lore);
        
        setCustomModelDataAsString(meta, "1006");
        meta.getPersistentDataContainer().set(storyItemKey, PersistentDataType.STRING, BOSS2_STRUCTURE_KEY);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private ItemStack createOverworldPortalKey() {
        return createOverworldPortalKey("ru");
    }
    
    private ItemStack createOverworldPortalKey(String lang) {
        ItemStack item = new ItemStack(Material.END_CRYSTAL);
        ItemMeta meta = item.getItemMeta();
        
        String keyName = plugin.getMessageManager().getMessage(lang, "chest.items.overworld_portal_key.name");
        meta.displayName(Component.text(keyName)
                .color(NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        java.util.List<String> loreLines = plugin.getMessageManager().getMessageList(lang, "chest.items.overworld_portal_key.lore");
        for (String line : loreLines) {
            if (line.isEmpty()) {
                lore.add(Component.empty());
            } else {
                lore.add(Component.text(line)
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }
        // Apply specific colors for certain lines
        if (loreLines.size() > 2) {
            lore.set(2, Component.text(loreLines.get(2))
                    .color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
        }
        if (loreLines.size() > 3) {
            lore.set(3, Component.text(loreLines.get(3))
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
        }
        
        meta.lore(lore);
        
        setCustomModelDataAsString(meta, "1007");
        meta.getPersistentDataContainer().set(storyItemKey, PersistentDataType.STRING, OVERWORLD_PORTAL_KEY);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private ItemStack createEndArtifact(int number) {
        return createEndArtifact(number, "ru");
    }
    
    private ItemStack createEndArtifact(int number, String lang) {
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
        
        String name = plugin.getMessageManager().getMessage(lang, "chest.items.end_artifact_" + number + ".name");
        
        meta.displayName(Component.text(name)
                .color(NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        java.util.List<String> loreLines = plugin.getMessageManager().getMessageList(lang, "chest.items.end_artifact_" + number + ".lore");
        for (int i = 0; i < loreLines.size(); i++) {
            String line = loreLines.get(i);
            if (line.isEmpty()) {
                lore.add(Component.empty());
            } else {
                // First line is gray, line with ✦ is yellow, line with ▶ is gold
                NamedTextColor color = NamedTextColor.GRAY;
                if (line.startsWith("✦")) {
                    color = NamedTextColor.YELLOW;
                } else if (line.startsWith("▶")) {
                    color = NamedTextColor.GOLD;
                }
                lore.add(Component.text(line)
                        .color(color)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }
        
        meta.lore(lore);
        
        String modelData = switch (number) {
            case 1 -> "1008"; // echo_shard_of_void
            case 2 -> "1009"; // breath_of_time
            case 3 -> "1010"; // fruit_of_void
            case 4 -> "1011"; // eye_of_dimensions
            case 5 -> "1012"; // wings_of_freedom
            default -> "0";
        };
        setCustomModelDataAsString(meta, modelData);

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

    /**
     * Устанавливает custom_model_data с использованием нового Paper 1.21+ API
     * Поддерживает строки как в команде /give minecraft:netherite_scrap[minecraft:custom_model_data={strings:["1003"]}]
     */
    private void setCustomModelDataAsString(ItemMeta meta, String modelData) {
        try {
            // Пробуем установить как строки через новый API Paper 1.21+
            try {
                // Пробуем новый подход через reflection для совместимости
                java.lang.reflect.Method getComponentMethod = meta.getClass().getMethod("getCustomModelDataComponent");
                Object component = getComponentMethod.invoke(meta);

                if (component != null) {
                    java.lang.reflect.Method setStringsMethod = component.getClass().getMethod("setStrings", java.util.List.class);
                    setStringsMethod.invoke(component, java.util.List.of(modelData));

                    java.lang.reflect.Method setComponentMethod = meta.getClass().getMethod("setCustomModelDataComponent", component.getClass());
                    setComponentMethod.invoke(meta, component);

                    plugin.getLogger().info("Set custom_model_data strings via reflection: [" + modelData + "]");
                    return;
                }
            } catch (Exception reflectionEx) {
                plugin.getLogger().fine("New CustomModelDataComponent API not available: " + reflectionEx.getMessage());
            }

            // Fallback к старому методу с integer
            try {
                int intModelData = Integer.parseInt(modelData);
                meta.setCustomModelData(intModelData);
                plugin.getLogger().info("Set integer custom_model_data: " + intModelData);
            } catch (NumberFormatException ex) {
                plugin.getLogger().warning("Invalid model data: " + modelData + ", using 0 instead");
                meta.setCustomModelData(0);
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to set custom_model_data: " + e.getMessage());
        }
    }
}
