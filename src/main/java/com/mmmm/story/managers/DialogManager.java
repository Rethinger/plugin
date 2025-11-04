package com.mmmm.story.managers;

import com.mmmm.story.MmmmStoryPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.*;

public class DialogManager {
    
    private final MmmmStoryPlugin plugin;
    private final Map<UUID, DialogSession> activeSessions = new HashMap<>();
    
    public DialogManager(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
    }
    
    // Class to track dialog sessions with pauses
    private static class DialogSession {
        List<Map<String, Object>> remainingLines;
        String dialogKey;
        boolean waitingForContinue = false;
        BukkitRunnable currentTask;
        
        DialogSession(List<Map<String, Object>> lines, String key) {
            this.remainingLines = new ArrayList<>(lines);
            this.dialogKey = key;
        }
    }
    
    // Handle continue command (called from a command or chat listener)
    public boolean continueDialog(Player player) {
        DialogSession session = activeSessions.get(player.getUniqueId());
        if (session == null || !session.waitingForContinue) {
            return false;
        }
        
        session.waitingForContinue = false;
        playNextDialogBatch(player, session);
        return true;
    }
    
    public void playDialog(Player player, String dialogKey) {
        // Always use automatic dialog playback
        playAutomaticDialog(player, dialogKey, null);
    }
    
    public void playDialog(Player player, String dialogKey, Runnable onComplete) {
        // Always use automatic dialog playback
        playAutomaticDialog(player, dialogKey, onComplete);
    }
    
    private void playInteractiveDialog(Player player, String dialogKey) {
        ConfigurationSection dialog = plugin.getConfigManager().getDialogs().getConfigurationSection(dialogKey);
        if (dialog == null) {
            plugin.getLogger().warning("Dialog not found: " + dialogKey);
            return;
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lines = (List<Map<String, Object>>) dialog.getList("lines");
        
        if (lines == null || lines.isEmpty()) return;
        
        // Create new session
        DialogSession session = new DialogSession(lines, dialogKey);
        activeSessions.put(player.getUniqueId(), session);
        
        // Apply initial effects
        String effectType = dialog.getString("effect", "");
        if (effectType.equalsIgnoreCase("DARKNESS")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 140, 0, false, false));
        }
        
        // Start playing dialog
        playNextDialogBatch(player, session);
    }
    
    private void playNextDialogBatch(Player player, DialogSession session) {
        if (session.remainingLines.isEmpty()) {
            // Dialog finished
            activeSessions.remove(player.getUniqueId());
            return;
        }
        
        // Play lines until we hit a pause point (every 3-5 lines or specific delay gaps)
        int linesToPlay = Math.min(4, session.remainingLines.size());
        int lastDelay = 0;
        
        for (int i = 0; i < linesToPlay; i++) {
            Map<String, Object> line = session.remainingLines.get(0);
            session.remainingLines.remove(0);
            
            int delay = ((Number) line.getOrDefault("delay", 0)).intValue();
            String text = (String) line.get("text");
            String soundName = String.valueOf(line.getOrDefault("sound", ""));
            boolean removeEffect = (boolean) line.getOrDefault("removeEffect", false);
            boolean ignitePortal = (boolean) line.getOrDefault("ignitePortal", false);
            
            int actualDelay = (delay - lastDelay) * 20; // Convert to ticks from last line
            lastDelay = delay;
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) {
                        activeSessions.remove(player.getUniqueId());
                        return;
                    }
                    
                    if (removeEffect) {
                        player.removePotionEffect(PotionEffectType.DARKNESS);
                    }
                    
                    if (ignitePortal) {
                        igniteNearbyNetherPortal(player);
                    }
                    
                    Component message = Component.text(text.replace("&", "§"));
                    player.sendMessage(message);
                    
                    if (!soundName.isEmpty() && !soundName.equals("null")) {
                        try {
                            Sound sound = Sound.valueOf(soundName.toUpperCase().replace("MINECRAFT:", ""));
                            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid sound: " + soundName);
                        }
                    }
                }
            }.runTaskLater(plugin, actualDelay);
        }
        
        // After this batch, show continue button if more lines remain
        if (!session.remainingLines.isEmpty()) {
            int finalDelay = (lastDelay + 2) * 20; // 2 seconds after last line
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) {
                        activeSessions.remove(player.getUniqueId());
                        return;
                    }
                    
                    session.waitingForContinue = true;
                    
                    // Send clickable "Continue" button
                    Component continueButton = Component.text("[")
                        .color(NamedTextColor.DARK_GRAY)
                        .append(Component.text(" ▶ Продолжить ")
                            .color(NamedTextColor.GREEN)
                            .decorate(TextDecoration.BOLD)
                            .hoverEvent(HoverEvent.showText(Component.text("Нажмите для продолжения диалога")))
                            .clickEvent(ClickEvent.runCommand("/story continue")))
                        .append(Component.text("]").color(NamedTextColor.DARK_GRAY));
                    
                    player.sendMessage(Component.empty());
                    player.sendMessage(continueButton);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.5f);
                }
            }.runTaskLater(plugin, finalDelay);
        } else {
            // No more lines, clean up
            new BukkitRunnable() {
                @Override
                public void run() {
                    activeSessions.remove(player.getUniqueId());
                }
            }.runTaskLater(plugin, (lastDelay + 1) * 20);
        }
    }
    
    private void playAutomaticDialog(Player player, String dialogKey, Runnable onComplete) {
        // Get player settings
        var playerSettings = plugin.getDataManager().getPlayerSettings(player.getUniqueId());
        
        // Check if player wants to see dialogs
        if (!playerSettings.isShowDialogs()) {
            return; // Skip dialog completely
        }
        
        // Get dialogs for default language (Russian)
        ConfigurationSection dialog = plugin.getConfigManager().getDialogs().getConfigurationSection(dialogKey);
        if (dialog == null) {
            plugin.getLogger().warning("Dialog not found: " + dialogKey);
            return;
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lines = (List<Map<String, Object>>) dialog.getList("lines");
        
        if (lines == null) return;
        
        // Get speed multiplier
        double speedMultiplier = playerSettings.getSpeedMultiplier();
        
        // Check for darkness effect (applied at start)
        String effectType = dialog.getString("effect", "");
        if (effectType.equalsIgnoreCase("DARKNESS")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 140, 0, false, false)); // 7 seconds (до строки delay:7)
        }
        
        // Check for clear weather flag
        boolean clearWeather = dialog.getBoolean("clearWeather", false);
        
        // Check for disappearance line parameter
        int disappearanceLine = dialog.getInt("disappearanceLine", -1);
        
        // Track the last line to execute onComplete after it
        int lastDelay = 0;
        
        for (Map<String, Object> line : lines) {
            int delay = ((Number) line.getOrDefault("delay", 0)).intValue();
            String text = (String) line.get("text");
            String soundName = String.valueOf(line.getOrDefault("sound", ""));
            boolean removeEffect = (boolean) line.getOrDefault("removeEffect", false);
            boolean ignitePortal = (boolean) line.getOrDefault("ignitePortal", false);
            
            // Apply speed multiplier to delay
            int adjustedDelay = (int) (delay * speedMultiplier);
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) {
                        return;
                    }
                    
                    // Remove darkness effect if specified
                    if (removeEffect) {
                        player.removePotionEffect(PotionEffectType.DARKNESS);
                    }
                    
                    // Ignite nether portal if specified
                    if (ignitePortal) {
                        igniteNearbyNetherPortal(player);
                    }
                    
                    // Parse color codes and formatting (включая &k для обфускации)
                    Component message = Component.text(text.replace("&", "§"));
                    
                    // Always send to chat only
                    player.sendMessage(message);
                    
                    // Apply clear weather if specified (after "солнце светит ярче")
                    if (clearWeather && text.contains("Солнце светит ярче")) {
                        World world = player.getWorld();
                        world.setStorm(false);
                        world.setThundering(false);
                        world.setClearWeatherDuration(999999);
                    }
                    
                    if (!soundName.isEmpty() && !soundName.equals("null")) {
                        try {
                            Sound sound = Sound.valueOf(soundName.toUpperCase().replace("MINECRAFT:", ""));
                            // Более разнообразные параметры звука для атмосферности
                            float pitch = 1.0f;
                            float volume = 1.0f;
                            
                            // Особые настройки для определённых звуков
                            if (soundName.contains("WITHER") || soundName.contains("DRAGON")) {
                                pitch = 0.8f;
                                volume = 1.5f;
                            } else if (soundName.contains("ENDERMAN")) {
                                pitch = 0.9f;
                            } else if (soundName.contains("VILLAGER")) {
                                pitch = 1.0f;
                            }
                            
                            player.playSound(player.getLocation(), sound, volume, pitch);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid sound: " + soundName);
                        }
                    }
                }
            }.runTaskLater(plugin, adjustedDelay * 20L);
            
            lastDelay = adjustedDelay;
        }
        
        // Execute onComplete callback after the last line
        if (onComplete != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    onComplete.run();
                }
            }.runTaskLater(plugin, (lastDelay + 1) * 20L);
        }
        
        // Handle NPC disappearance effect if specified
        if (disappearanceLine >= 0) {
            // Calculate when to trigger the disappearance effect
            int disappearanceDelay = (int) (disappearanceLine * speedMultiplier);
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) {
                        return;
                    }
                    
                    // Find and remove the "Посланник" NPC
                    plugin.getNpcManager().removeNpcByName(player, "Посланник");
                    
                    // Play particle effect at NPC location
                    Location npcLocation = player.getLocation().add(0, 1, 0); // Approximate NPC location
                    World world = npcLocation.getWorld();
                    
                    // Create smoke effect
                    for (int i = 0; i < 20; i++) {
                        double offsetX = (Math.random() - 0.5) * 2;
                        double offsetY = Math.random() * 2;
                        double offsetZ = (Math.random() - 0.5) * 2;
                        
                        world.spawnParticle(Particle.SMOKE, npcLocation, 10, offsetX, offsetY, offsetZ, 0.1);
                    }
                    
                    // Play disappearance sound
                    player.playSound(npcLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                }
            }.runTaskLater(plugin, disappearanceDelay * 20L);
        }
    }
    
    public void playDialogForAll(String dialogKey) {
        // Play dialog individually for each player with their settings
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            playDialog(player, dialogKey);
        }
    }
    
    public void sendSystemMessage(Player player, String messageKey) {
        String message = plugin.getConfigManager().getMessage(messageKey);
        player.sendMessage(Component.text(message));
    }
    
    private void igniteNearbyNetherPortal(Player player) {
        // Get core drop location from Act1Listener
        Location coreLocation = plugin.getAct1Listener().getCoreDropLocation();
        
        if (coreLocation == null) {
            plugin.getLogger().warning(plugin.getConfigManager().getMessage("dialog.core_location_not_found"));
            return;
        }
        
        World world = coreLocation.getWorld();
        
        // Simply ignite the block where the core was dropped
        Block blockToIgnite = coreLocation.getBlock();
        if (blockToIgnite.getType() == Material.AIR || blockToIgnite.getType() == Material.OBSIDIAN) {
            blockToIgnite.setType(Material.FIRE);
            world.playSound(coreLocation, Sound.ITEM_FIRECHARGE_USE, 1.5f, 0.8f);
            world.spawnParticle(Particle.FLAME, coreLocation, 100, 0.5, 0.5, 0.5, 0.1);
            world.spawnParticle(Particle.LAVA, coreLocation, 30, 0.3, 0.3, 0.3, 0.05);
            plugin.getLogger().info(plugin.getConfigManager().getMessage("dialog.portal_ignited_at").replace("%location%", coreLocation.toString()));
        } else {
            plugin.getLogger().warning(plugin.getConfigManager().getMessage("dialog.cannot_ignite_portal").replace("%block%", blockToIgnite.getType().toString()));
        }
    }
    
    /**
     * Get player's language based on client locale
     */
    public String getPlayerLanguage(Player player) {
        // Since only Russian is supported, always return "ru"
        return "ru";
    }
    
}

