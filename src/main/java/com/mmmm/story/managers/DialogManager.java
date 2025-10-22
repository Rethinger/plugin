package com.mmmm.story.managers;

import com.mmmm.story.MmmmStoryPlugin;
import net.kyori.adventure.text.Component;
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
import java.util.List;
import java.util.Map;

public class DialogManager {
    
    private final MmmmStoryPlugin plugin;
    
    public DialogManager(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void playDialog(Player player, String dialogKey) {
        ConfigurationSection dialog = plugin.getConfigManager().getDialogs().getConfigurationSection(dialogKey);
        if (dialog == null) {
            plugin.getLogger().warning("Dialog not found: " + dialogKey);
            return;
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lines = (List<Map<String, Object>>) dialog.getList("lines");
        
        if (lines == null) return;
        
        // Check for darkness effect (applied at start)
        String effectType = dialog.getString("effect", "");
        if (effectType.equalsIgnoreCase("DARKNESS")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 140, 0, false, false)); // 7 seconds (до строки delay:7)
        }
        
        // Check for clear weather flag
        boolean clearWeather = dialog.getBoolean("clearWeather", false);
        
        for (Map<String, Object> line : lines) {
            int delay = ((Number) line.getOrDefault("delay", 0)).intValue();
            String text = (String) line.get("text");
            String soundName = String.valueOf(line.getOrDefault("sound", ""));
            boolean removeEffect = (boolean) line.getOrDefault("removeEffect", false);
            boolean ignitePortal = (boolean) line.getOrDefault("ignitePortal", false);
            
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
            }.runTaskLater(plugin, delay * 20L);
        }
    }
    
    public void playDialogForAll(String dialogKey) {
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
            plugin.getLogger().warning("Core location not found for portal ignition!");
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
            plugin.getLogger().info("Portal ignited at core location: " + coreLocation);
        } else {
            plugin.getLogger().warning("Cannot ignite portal - block is " + blockToIgnite.getType());
        }
    }
}

