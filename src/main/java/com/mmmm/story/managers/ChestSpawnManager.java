package com.mmmm.story.managers;

import com.mmmm.story.MmmmStoryPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.generator.structure.Structure;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.util.StructureSearchResult;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;

import java.util.*;

public class ChestSpawnManager implements Listener {
    
    private final MmmmStoryPlugin plugin;
    private final MessageManager messageManager;
    private final Set<String> processedChests = new HashSet<>();
    private final Map<String, Long> chestTimers = new HashMap<>();
    private final Map<UUID, String> searchingPlayers = new HashMap<>(); // Player UUID -> Structure Key
    private final Map<String, Set<UUID>> structureSearchers = new HashMap<>(); // Structure Key -> Players searching
    private final Map<String, Integer> chestFailCounts = new HashMap<>(); // Chest Key -> Fail count
    private final Map<UUID, Integer> activeSearchTasks = new HashMap<>(); // Player UUID -> Task ID
    
    // Chance for item spawn
    private static final double SPAWN_CHANCE_STABILIZATION_CORE = 0.15;  // 15% для ядра стабилизации
    private static final double SPAWN_CHANCE_BOSS1_KEY = 0.10;  // 10% для ключа босса №1
    private static final double SPAWN_CHANCE_BOSS1_CATALYST = 0.10;  // 10% для катализатора пустоты
    private static final double SPAWN_CHANCE_ARTIFACT = 0.20; // 20% для артефактов End
    
    // Delay range in ticks
    private static final int MIN_DELAY_NORMAL = 100; // 5 секунд
    private static final int MAX_DELAY_NORMAL = 200; // 10 секунд
    private static final int MIN_DELAY_ARTIFACT = 40; // 2 секунды
    private static final int MAX_DELAY_ARTIFACT = 100; // 5 секунд
    
    // Cooldown between chest searches (prevent abuse) - 5 seconds
    private static final long CHEST_COOLDOWN = 5000L;
    
    // Max failed attempts before chest breaks
    private static final int MAX_FAILED_ATTEMPTS = 3;
    
    public ChestSpawnManager(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        
        // Start cleanup task to remove old processed chests (every 10 minutes)
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupOldChests();
            }
        }.runTaskTimer(plugin, 12000L, 12000L); // Every 10 minutes
    }
    
    private void cleanupOldChests() {
        long currentTime = System.currentTimeMillis();
        Set<String> toRemove = new HashSet<>();
        
        for (Map.Entry<String, Long> entry : chestTimers.entrySet()) {
            // Remove chests that were processed more than 30 minutes ago
            if (currentTime - entry.getValue() > 30 * 60 * 1000) {
                toRemove.add(entry.getKey());
            }
        }
        
        for (String key : toRemove) {
            processedChests.remove(key);
            chestTimers.remove(key);
            chestFailCounts.remove(key);
        }
        
        if (!toRemove.isEmpty()) {
            plugin.getLogger().info("Cleaned up " + toRemove.size() + " old chest records");
        }
    }
    
    @EventHandler
    public void onChestOpen(InventoryOpenEvent event) {
        if (!(event.getInventory().getHolder() instanceof Chest)) {
            return;
        }
        
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        Chest chest = (Chest) event.getInventory().getHolder();
        Location chestLoc = chest.getLocation();
        String chestKey = getChestKey(chestLoc);
        
        // Check if this is a player-placed chest (metadata check)
        if (chest.getBlock().hasMetadata("player_placed")) {
            return; // Don't allow searching in player-placed chests
        }
        
        // Determine structure type and item
        StoryStructureType structureType = detectStructureType(chestLoc);
        
        if (structureType == StoryStructureType.NONE) {
            return;
        }
        
        // Get structure key (coordinates of structure center)
        String structureKey = getStructureKey(chestLoc, structureType);
        
        // Check if material was already found in this structure type (ONE PER SERVER)
        if (isMaterialFoundInStructure(structureType)) {
            // For END_CITY, check if all artifacts found
            if (structureType == StoryStructureType.END_CITY && !hasUnfoundArtifacts()) {
                return; // All artifacts found, no more searching
            } else if (structureType != StoryStructureType.END_CITY) {
                return; // No searching mechanic if material already found
            }
        }
        
        // Check if this chest was already processed recently (PREVENT ABUSE)
        if (processedChests.contains(chestKey)) {
            // Check if cooldown has passed
            Long lastProcessed = chestTimers.get(chestKey);
            if (lastProcessed != null) {
                long timeSince = System.currentTimeMillis() - lastProcessed;
                if (timeSince < CHEST_COOLDOWN) {
                    // Still on cooldown, don't show search mechanic
                    long secondsRemaining = (CHEST_COOLDOWN - timeSince) / 1000;
                    String message = messageManager.getMessage(player, "chest.search.cooldown")
                        .replace("{0}", String.valueOf(secondsRemaining));
                    Component msg = Component.text(message).color(NamedTextColor.RED);
                    player.sendMessage(msg);
                    player.sendActionBar(msg);
                    return;
                }
            }
        }
        
        // Check if correct act for this structure
        if (!canSpawnInStructure(structureType)) {
            return;
        }
        
        // Mark as processed IMMEDIATELY to prevent abuse
        processedChests.add(chestKey);
        chestTimers.put(chestKey, System.currentTimeMillis());
        
        // Start searching mechanic for this player AFTER marking as processed
        startSearchingMechanic(player, structureType, structureKey);
        
        // Schedule delayed spawn
        scheduleItemSpawn(chest, structureType, chestKey, structureKey, player);
    }
    
    @EventHandler
    public void onChestClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Stop searching mechanic
        if (searchingPlayers.containsKey(playerId)) {
            String structureKey = searchingPlayers.remove(playerId);
            
            if (structureSearchers.containsKey(structureKey)) {
                structureSearchers.get(structureKey).remove(playerId);
                if (structureSearchers.get(structureKey).isEmpty()) {
                    structureSearchers.remove(structureKey);
                }
            }
            
            // Stop searching task and clear action bar
            stopSearchingTask(player);
            player.sendActionBar(Component.empty());
        }
    }
    
    @EventHandler
    public void onChestPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() == Material.CHEST || 
            event.getBlock().getType() == Material.TRAPPED_CHEST) {
            // Mark this chest as player-placed
            event.getBlock().setMetadata("player_placed", 
                new FixedMetadataValue(plugin, true));
        }
    }
    
    /**
     * Translate story items when player opens ANY inventory containing them
     * This ensures each player sees items in their own language
     */
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        String playerLang = plugin.getDialogManager().getPlayerLanguage(player);
        
        // Translate all story items in the opened inventory
        Inventory inventory = event.getInventory();
        ItemStack[] contents = inventory.getContents();
        
        boolean modified = false;
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() != Material.AIR) {
                String storyItemId = plugin.getItemManager().getStoryItemId(item);
                if (storyItemId != null) {
                    // This is a story item - recreate it in player's language
                    ItemStack translatedItem = plugin.getItemManager().createStoryItem(storyItemId, playerLang);
                    if (translatedItem != null) {
                        // Preserve the amount
                        translatedItem.setAmount(item.getAmount());
                        contents[i] = translatedItem;
                        modified = true;
                    }
                }
            }
        }
        
        // Update inventory if any items were translated
        if (modified) {
            // Schedule update for next tick to avoid conflicts
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                inventory.setContents(contents);
            });
        }
        
        // Also translate items in player's own inventory
        translatePlayerInventory(player, playerLang);
    }
    
    /**
     * Helper method to translate all story items in player's inventory
     */
    private void translatePlayerInventory(Player player, String language) {
        ItemStack[] inventory = player.getInventory().getContents();
        boolean modified = false;
        
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null && item.getType() != Material.AIR) {
                String storyItemId = plugin.getItemManager().getStoryItemId(item);
                if (storyItemId != null) {
                    ItemStack translatedItem = plugin.getItemManager().createStoryItem(storyItemId, language);
                    if (translatedItem != null) {
                        translatedItem.setAmount(item.getAmount());
                        inventory[i] = translatedItem;
                        modified = true;
                    }
                }
            }
        }
        
        if (modified) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.getInventory().setContents(inventory);
            });
        }
    }
    
    private void startSearchingMechanic(Player player, StoryStructureType type, String structureKey) {
        UUID playerId = player.getUniqueId();
        String lang = plugin.getDialogManager().getPlayerLanguage(player);
        
        // Add player to searching list
        searchingPlayers.put(playerId, structureKey);
        structureSearchers.computeIfAbsent(structureKey, k -> new HashSet<>()).add(playerId);
        
        // Send initial message
        String itemName = getItemNameForStructure(type, lang);
        String searchText = messageManager.getMessage(player, "chest.search.searching");
        String itemText = messageManager.getMessage(player, "chest.search.searching_item")
            .replace("{0}", itemName);
        
        Component searchMsg = Component.text(searchText)
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true);
        Component itemMsg = Component.text(itemText)
                .color(NamedTextColor.YELLOW);
        
        player.sendMessage(searchMsg);
        player.sendMessage(itemMsg);
        player.sendActionBar(searchMsg);
        
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
        
        // Start periodic searching messages
        startSearchingTask(player, type, structureKey);
    }
    
    private void startSearchingTask(Player player, StoryStructureType type, String structureKey) {
        // Cancel previous task if exists
        UUID playerId = player.getUniqueId();
        if (activeSearchTasks.containsKey(playerId)) {
            plugin.getServer().getScheduler().cancelTask(activeSearchTasks.get(playerId));
        }
        
        String lang = plugin.getDialogManager().getPlayerLanguage(player);
        String searchingText = messageManager.getMessage(player, "chest.search.searching");
        
        BukkitRunnable task = new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                // Check if player is still searching
                if (!searchingPlayers.containsKey(player.getUniqueId()) || 
                    !searchingPlayers.get(player.getUniqueId()).equals(structureKey)) {
                    activeSearchTasks.remove(player.getUniqueId());
                    cancel();
                    return;
                }
                
                ticks++;

                // Play experience orb sound every 2 ticks
                if (ticks % 2 == 0) {
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
                }
                
                // Send searching messages every 2 seconds
                if (ticks % 40 == 0) {
                    player.sendActionBar(Component.text(searchingText)
                            .color(NamedTextColor.GRAY));
                }
            }
        };
        
        int taskId = task.runTaskTimer(plugin, 0L, 1L).getTaskId();
        activeSearchTasks.put(playerId, taskId);
    }
    
    private void stopSearchingTask(Player player) {
        UUID playerId = player.getUniqueId();
        if (activeSearchTasks.containsKey(playerId)) {
            plugin.getServer().getScheduler().cancelTask(activeSearchTasks.get(playerId));
            activeSearchTasks.remove(playerId);
        }
    }
    
    private String getItemNameForStructure(StoryStructureType type, String lang) {
        boolean isEnglish = lang.equals("en");
        return switch (type) {
            case RUINED_PORTAL -> isEnglish ? "Stabilization Core" : "Ядро Стабилизации";
            case NETHER_FORTRESS -> isEnglish ? "Summon Key" : "Ключ Призыва";
            case BASTION_REMNANT -> isEnglish ? "Void Catalyst" : "Катализатор Пустоты";
            case END_CITY -> isEnglish ? "End Artifact" : "Артефакт Края";
            default -> isEnglish ? "Unknown Item" : "Неизвестный предмет";
        };
    }
    
    private String getStructureKey(Location loc, StoryStructureType type) {
        // Round to nearest 50 blocks to group chests in same structure
        int roundX = (int) (Math.round(loc.getX() / 50.0) * 50);
        int roundZ = (int) (Math.round(loc.getZ() / 50.0) * 50);
        return loc.getWorld().getName() + "_" + type.name() + "_" + roundX + "_" + roundZ;
    }
    
    private boolean isMaterialFoundInStructure(StoryStructureType type) {
        return switch (type) {
            case RUINED_PORTAL -> plugin.getDataManager().getBoolean("materials.stabilization_core_found", false);
            case NETHER_FORTRESS -> plugin.getDataManager().getBoolean("materials.boss1_summon_key_found", false);
            case BASTION_REMNANT -> plugin.getDataManager().getBoolean("materials.boss1_catalyst_found", false);
            case END_CITY -> false; // Artifacts don't use this - checked per artifact
            default -> false;
        };
    }
    
    private void markMaterialFound(StoryStructureType type, String structureKey, ItemStack item) {
        switch (type) {
            case RUINED_PORTAL -> plugin.getDataManager().setBoolean("materials.stabilization_core_found", true);
            case NETHER_FORTRESS -> plugin.getDataManager().setBoolean("materials.boss1_summon_key_found", true);
            case BASTION_REMNANT -> plugin.getDataManager().setBoolean("materials.boss1_catalyst_found", true);
            case END_CITY -> {
                // Mark specific artifact as found
                String itemId = plugin.getItemManager().getStoryItemId(item);
                if (itemId != null && itemId.startsWith("end_artifact_")) {
                    String artifactNum = itemId.replace("end_artifact_", "");
                    plugin.getDataManager().setBoolean("artifacts.artifact_" + artifactNum + "_found", true);
                    plugin.getLogger().info("Artifact " + artifactNum + " marked as found!");
                }
            }
        }
        
        // Notify all players searching in this structure
        if (structureSearchers.containsKey(structureKey)) {
            for (UUID playerId : structureSearchers.get(structureKey)) {
                Player p = plugin.getServer().getPlayer(playerId);
                if (p != null && p.isOnline()) {
                    String lang = plugin.getDialogManager().getPlayerLanguage(p);
                    String notifyText = lang.equals("en") 
                        ? "✓ Material found in this structure!"
                        : "✓ Материал найден в этой структуре!";
                    
                    p.sendMessage(Component.text(notifyText)
                            .color(NamedTextColor.GREEN)
                            .decoration(TextDecoration.BOLD, true));
                    p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                }
            }
        }
    }
    
    private void scheduleItemSpawn(Chest chest, StoryStructureType structureType, String chestKey, String structureKey, Player player) {
        Random random = new Random();
        
        // Different settings for artifacts vs regular materials
        boolean isArtifact = structureType == StoryStructureType.END_CITY;
        int minDelay = isArtifact ? MIN_DELAY_ARTIFACT : MIN_DELAY_NORMAL;
        int maxDelay = isArtifact ? MAX_DELAY_ARTIFACT : MAX_DELAY_NORMAL;
        
        // Get spawn chance based on structure type
        double spawnChance = switch (structureType) {
            case RUINED_PORTAL -> SPAWN_CHANCE_STABILIZATION_CORE;  // 15%
            case NETHER_FORTRESS -> SPAWN_CHANCE_BOSS1_KEY;         // 10%
            case BASTION_REMNANT -> SPAWN_CHANCE_BOSS1_CATALYST;    // 10%
            case END_CITY -> SPAWN_CHANCE_ARTIFACT;                  // 20%
            default -> 0.05;
        };
        
        int delay = minDelay + random.nextInt(maxDelay - minDelay + 1);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if chest still exists
                if (!(chest.getBlock().getState() instanceof Chest)) {
                    return;
                }
                
                // Check spawn chance (ruined_portal 15%, fortress/bastion 10%, end_city 20%)
                if (random.nextDouble() > spawnChance) {
                    // Failed spawn - increment fail counter
                    int failCount = chestFailCounts.getOrDefault(chestKey, 0) + 1;
                    chestFailCounts.put(chestKey, failCount);
                    
                    // Stop searching task and clear action bar
                    if (player.isOnline()) {
                        stopSearchingTask(player);
                        
                        String failText = messageManager.getMessage(player, "chest.search.nothing")
                            .replace("{0}", String.valueOf(failCount))
                            .replace("{1}", String.valueOf(MAX_FAILED_ATTEMPTS));
                        
                        Component failMsg = Component.text(failText).color(NamedTextColor.GRAY);
                        player.sendMessage(failMsg);
                        player.sendActionBar(failMsg);
                        player.playSound(player.getLocation(), Sound.ENTITY_CAT_DEATH, 1.0f, 1.0f);
                        
                        // Check if chest should break after 3 failed attempts
                        if (failCount >= MAX_FAILED_ATTEMPTS) {
                            Location chestLoc = chest.getLocation();
                            World world = chestLoc.getWorld();
                            
                            // Drop all items from chest before breaking
                            Inventory chestInventory = chest.getInventory();
                            for (ItemStack item : chestInventory.getContents()) {
                                if (item != null && item.getType() != Material.AIR) {
                                    world.dropItemNaturally(chestLoc.clone().add(0.5, 0.5, 0.5), item);
                                }
                            }
                            chestInventory.clear();
                            
                            // Break chest with effects
                            chest.getBlock().setType(Material.AIR);
                            world.spawnParticle(Particle.BLOCK, chestLoc.add(0.5, 0.5, 0.5), 30, 0.3, 0.3, 0.3, 0.1, Material.CHEST.createBlockData());
                            world.playSound(chestLoc, Sound.BLOCK_CHEST_LOCKED, 1.0f, 0.8f);
                            
                            String breakText = messageManager.getMessage(player, "chest.search.broke");
                            
                            player.sendMessage(Component.text(breakText)
                                    .color(NamedTextColor.RED)
                                    .decoration(TextDecoration.BOLD, true));
                            
                            // Clean up
                            chestFailCounts.remove(chestKey);
                        }
                    }
                    return;
                }
                
                // Spawn item based on structure
                String lang = plugin.getDialogManager().getPlayerLanguage(player);
                ItemStack item = getItemForStructure(structureType, lang);
                if (item == null) {
                    processedChests.remove(chestKey);
                    return;
                }
                
                // Add item to chest
                Inventory inv = chest.getInventory();
                inv.addItem(item);
                
                // Mark material as found (globally for one-per-server)
                markMaterialFound(structureType, structureKey, item);
                
                // Visual effects
                Location loc = chest.getLocation().add(0.5, 1, 0.5);
                World world = loc.getWorld();
                
                world.spawnParticle(Particle.ENCHANT, loc, 50, 0.3, 0.3, 0.3, 0.5);
                world.spawnParticle(Particle.END_ROD, loc, 30, 0.2, 0.2, 0.2, 0.1);
                world.playSound(loc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);
                world.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
                
                // Notify player
                if (player.isOnline()) {
                    // Stop searching task
                    stopSearchingTask(player);
                    
                    String itemName = getItemNameForStructure(structureType, lang);
                    
                    String foundText = messageManager.getMessage(player, "chest.search.found")
                        .replace("{0}", itemName);
                    String successText = messageManager.getMessage(player, "chest.search.success");
                    
                    Component foundMsg = Component.text(foundText)
                            .color(NamedTextColor.GREEN)
                            .decoration(TextDecoration.BOLD, true);
                    player.sendMessage(foundMsg);
                    player.sendActionBar(foundMsg);
                    player.showTitle(net.kyori.adventure.title.Title.title(
                            Component.text(successText).color(NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true),
                            Component.text(itemName).color(NamedTextColor.YELLOW)
                    ));
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                    
                    // Play dialog based on structure type
                    String dialogKey = switch (structureType) {
                        case RUINED_PORTAL -> "altar.forgotten";
                        case NETHER_FORTRESS -> "nether.key_found";
                        case BASTION_REMNANT -> "bastion.catalyst_found";
                        case END_CITY -> {
                            // Get artifact number from item
                            String itemId = plugin.getItemManager().getStoryItemId(item);
                            if (itemId != null && itemId.startsWith("end_artifact_")) {
                                String artifactNum = itemId.replace("end_artifact_", "");
                                yield "artifact.found_" + artifactNum;
                            }
                            yield null;
                        }
                        default -> null;
                    };
                    if (dialogKey != null) {
                        plugin.getDialogManager().playDialog(player, dialogKey);
                    }
                    
                    // Check if this was the 5th artifact and trigger final quest
                    if (structureType == StoryStructureType.END_CITY) {
                        int artifactsFound = 0;
                        for (int i = 1; i <= 5; i++) {
                            if (plugin.getDataManager().getBoolean("artifacts.artifact_" + i + "_found", false)) {
                                artifactsFound++;
                            }
                        }
                        if (artifactsFound == 5) {
                            // All 5 artifacts found! Trigger final quest dialog
                            plugin.getDialogManager().playDialog(player, "artifact.void_found");
                        }
                    }
                    
                    // Reset fail counter on success
                    chestFailCounts.remove(chestKey);
                }
            }
        }.runTaskLater(plugin, delay);
    }
    
    private boolean canSpawnInStructure(StoryStructureType type) {
        int currentAct = plugin.getDataManager().getCurrentAct();
        
        return switch (type) {
            case RUINED_PORTAL -> currentAct >= 1 && !isMaterialFoundInStructure(type);
            case NETHER_FORTRESS -> currentAct >= 2 && !isMaterialFoundInStructure(type); // Ключ для призыва Boss 1 (можно искать всегда)
            case BASTION_REMNANT -> currentAct >= 2 && plugin.getDataManager().isBoss1Defeated() && !isMaterialFoundInStructure(type); // Катализатор ПОСЛЕ победы над Boss 1
            case END_CITY -> currentAct >= 4 && plugin.getDataManager().isEndEnabled() && hasUnfoundArtifacts();
            default -> false;
        };
    }
    
    private boolean hasUnfoundArtifacts() {
        // Check if there are any artifacts that haven't been found yet
        for (int i = 1; i <= 5; i++) {
            if (!plugin.getDataManager().getBoolean("artifacts.artifact_" + i + "_found", false)) {
                return true;
            }
        }
        return false;
    }
    
    private ItemStack getItemForStructure(StoryStructureType type, String language) {
        return switch (type) {
            case RUINED_PORTAL -> plugin.getItemManager().createStoryItem(ItemManager.STABILIZATION_CORE, language);
            case NETHER_FORTRESS -> plugin.getItemManager().createStoryItem("boss1_summon_key", language); // Призывалка для Boss 1
            case BASTION_REMNANT -> plugin.getItemManager().createStoryItem("boss1_catalyst", language); // Катализатор Пустоты
            case END_CITY -> getRandomUnfoundArtifact(language);
            default -> null;
        };
    }
    
    private ItemStack getRandomUnfoundArtifact(String language) {
        // Collect all unfound artifacts
        List<Integer> unfoundArtifacts = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            if (!plugin.getDataManager().getBoolean("artifacts.artifact_" + i + "_found", false)) {
                unfoundArtifacts.add(i);
            }
        }
        
        if (unfoundArtifacts.isEmpty()) {
            return null;
        }
        
        // Pick random unfound artifact
        Random random = new Random();
        int artifactNumber = unfoundArtifacts.get(random.nextInt(unfoundArtifacts.size()));
        
        return plugin.getItemManager().createStoryItem("end_artifact_" + artifactNumber, language);
    }
    
    private StoryStructureType detectStructureType(Location location) {
        World world = location.getWorld();
        var structureRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.STRUCTURE);
        
        // Check for minecraft:ruined_portal (Ядро Стабилизации) - Overworld only
        // Search through all ruined portal variants (desert, jungle, mountain, ocean, swamp, standard)
        if (world.getEnvironment() == World.Environment.NORMAL) {
            // Try all ruined portal structure variants
            String[] portalVariants = {
                "ruined_portal",
                "ruined_portal_desert",
                "ruined_portal_jungle", 
                "ruined_portal_mountain",
                "ruined_portal_ocean",
                "ruined_portal_swamp"
            };
            
            for (String variantName : portalVariants) {
                Structure ruinedPortal = structureRegistry.get(NamespacedKey.minecraft(variantName));
                
                if (ruinedPortal != null) {
                    StructureSearchResult portalResult = world.locateNearestStructure(
                        location, 
                        ruinedPortal,
                        50, // radius in chunks
                        false
                    );
                    
                    if (portalResult != null) {
                        double distance = portalResult.getLocation().distance(location);
                        
                        if (distance < 120) { // Увеличенный радиус для больших порталов
                            return StoryStructureType.RUINED_PORTAL;
                        }
                    }
                }
            }
        }
        
        // Check for minecraft:fortress (Ключ для Boss 1) - Nether only
        if (world.getEnvironment() == World.Environment.NETHER) {
            Structure fortress = structureRegistry.get(NamespacedKey.minecraft("fortress"));
            if (fortress != null) {
                StructureSearchResult fortressResult = world.locateNearestStructure(
                    location,
                    fortress,
                    50,
                    false
                );
                if (fortressResult != null && fortressResult.getLocation().distance(location) < 200) {
                    plugin.getLogger().info("Detected NETHER_FORTRESS at " + location.getBlockX() + ", " + location.getBlockZ());
                    return StoryStructureType.NETHER_FORTRESS;
                }
            }
            
            // Check for minecraft:bastion_remnant (Материал для Boss 2)
            Structure bastion = structureRegistry.get(NamespacedKey.minecraft("bastion_remnant"));
            if (bastion != null) {
                StructureSearchResult bastionResult = world.locateNearestStructure(
                    location,
                    bastion,
                    50,
                    false
                );
                if (bastionResult != null && bastionResult.getLocation().distance(location) < 200) {
                    plugin.getLogger().info("Detected BASTION_REMNANT at " + location.getBlockX() + ", " + location.getBlockZ());
                    return StoryStructureType.BASTION_REMNANT;
                }
            }
        }
        
        // Check for minecraft:end_city (Артефакты) - End only
        if (world.getEnvironment() == World.Environment.THE_END) {
            Structure endCity = structureRegistry.get(NamespacedKey.minecraft("end_city"));
            if (endCity != null) {
                StructureSearchResult endCityResult = world.locateNearestStructure(
                    location,
                    endCity,
                    50,
                    false
                );
                if (endCityResult != null && endCityResult.getLocation().distance(location) < 200) {
                    plugin.getLogger().info("Detected END_CITY at " + location.getBlockX() + ", " + location.getBlockZ());
                    return StoryStructureType.END_CITY;
                }
            }
        }
        
        return StoryStructureType.NONE;
    }
    
    private boolean hasNearbyBlocks(Location center, int radius, Material... materials) {
        World world = center.getWorld();
        int count = 0;
        int requiredCount = 5; // Increased from 3 to 5 for more strict detection
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = world.getBlockAt(
                        center.getBlockX() + x,
                        center.getBlockY() + y,
                        center.getBlockZ() + z
                    );
                    
                    for (Material mat : materials) {
                        if (block.getType() == mat) {
                            count++;
                            if (count >= requiredCount) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    private String getChestKey(Location loc) {
        return String.format("%s_%d_%d_%d", 
            loc.getWorld().getName(),
            loc.getBlockX(),
            loc.getBlockY(),
            loc.getBlockZ()
        );
    }
    
    public void reset() {
        processedChests.clear();
        chestTimers.clear();
        chestFailCounts.clear();
        
        // Cancel all active search tasks
        for (Integer taskId : activeSearchTasks.values()) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }
        activeSearchTasks.clear();
    }
    
    private enum StoryStructureType {
        NONE,
        RUINED_PORTAL,
        NETHER_FORTRESS,
        BASTION_REMNANT,
        END_CITY  // For End artifacts
    }
}
