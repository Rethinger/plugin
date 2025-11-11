package com.mmmm.story.managers;

import com.mmmm.story.MmmmStoryPlugin;
import de.eisi05.npc.api.objects.NPC;
import de.eisi05.npc.api.objects.NpcOption;
import de.eisi05.npc.api.objects.Skin;
import de.eisi05.npc.api.wrapper.enums.ChatFormat;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NPCManager {
    
    // Animation timing constants (in ticks)
    private static final long TICKS_PER_SECOND = 20L;
    private static final long MESSENGER_SPAWN_DELAY = 10 * TICKS_PER_SECOND;  // 10 seconds
    private static final long MESSENGER_DESPAWN_START = 58 * TICKS_PER_SECOND; // 58 seconds
    private static final long MESSENGER_DESPAWN_DURATION = 5 * TICKS_PER_SECOND; // 5 seconds
    private static final long DIRECTION_MARKER_DURATION = 5 * 60 * TICKS_PER_SECOND; // 5 minutes
    
    // Animation frame rates
    private static final int ANIMATION_FPS = 20; // 20 ticks per second
    private static final long IDLE_CHECK_INTERVAL = 60L; // Check every 3 seconds
    private static final long IDLE_NOD_INTERVAL = 100L; // Nod every 5 seconds
    
    // Scale limits for breathing animation
    private static final double BREATHING_AMPLITUDE = 0.03; // ±3% scale change
    private static final double BASE_SCALE = 1.0;
    
    // Head movement limits (in degrees)
    private static final float IDLE_YAW_MAX = 8.0f;
    private static final float IDLE_PITCH_MAX = 5.0f;
    
    private final MmmmStoryPlugin plugin;
    private final Map<String, NPC> npcEntities = new HashMap<>();
    private final Map<String, BukkitRunnable> auraTask = new HashMap<>();
    private final Map<String, java.util.List<BukkitTask>> scheduledTasks = new HashMap<>();
    private final Map<String, Long> despawningNpcs = new HashMap<>(); // Track despawning NPCs for idempotency
    private String currentMessengerId = null; // Track the current messenger NPC
    private Location currentMessengerLocation = null; // Track messenger spawn location for despawn animation
    
    private void trackTask(String npcId, BukkitTask task) {
        if (npcId == null || task == null) return;
        scheduledTasks.computeIfAbsent(npcId, k -> new java.util.ArrayList<>()).add(task);
    }

    private void cancelScheduledTasks(String npcId) {
        java.util.List<BukkitTask> tasks = scheduledTasks.remove(npcId);
        if (tasks != null) {
            for (BukkitTask t : tasks) {
                if (t != null) {
                    t.cancel();
                }
            }
        }
    }

    public NPCManager(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void spawnMessenger(Location location) {
        final World world = location.getWorld();
        final String npcId = "messenger_" + UUID.randomUUID().toString().substring(0, 8);
        currentMessengerId = npcId; // Track this messenger for later despawn
        currentMessengerLocation = location.clone(); // Store location for despawn animation
        
        // PHASE 1: Pre-spawn effects (delay: 0-5s) - Darkness gathers
        world.spawnParticle(Particle.SMOKE, location, 100, 1, 1.5, 1, 0.05);
        world.spawnParticle(Particle.SQUID_INK, location, 50, 1, 1, 1, 0.02);
        world.playSound(location, Sound.AMBIENT_CAVE, 1.5f, 0.8f);
        
        // PHASE 2: Portal effect (delay: 5s) - "???: ...is someone there?"
        trackTask(npcId, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            world.spawnParticle(Particle.REVERSE_PORTAL, location, 200, 0.5, 1.5, 0.5, 0.3);
            world.spawnParticle(Particle.DRAGON_BREATH, location, 80, 0.8, 1, 0.8, 0.05);
            world.playSound(location, Sound.ENTITY_ENDERMAN_STARE, 1.0f, 0.7f);
        }, 100L)); // 5 seconds
        
        // PHASE 3: Materialization (delay: 10s) - "A figure materializes from the mist"
        // First fetch skin asynchronously, then create NPC
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            // Load skin from config
            String skinType = plugin.getConfig().getString("npc.skin.type", "player");
            Skin skin = null;
            
            if (skinType.equalsIgnoreCase("player")) {
                // Fetch skin by player name
                String playerName = plugin.getConfig().getString("npc.skin.playerName", "Notch");
                plugin.getLogger().info("[NPC] Загрузка скина игрока: " + playerName);
                skin = Skin.fetchSkin(playerName);
                
                if (skin == null) {
                    plugin.getLogger().warning("[NPC] Не удалось загрузить скин игрока '" + playerName + "', используется стандартный скин Steve");
                } else {
                    plugin.getLogger().info("[NPC] Скин успешно загружен для игрока: " + playerName);
                }
            } else if (skinType.equalsIgnoreCase("custom")) {
                // Use custom texture from config
                String value = plugin.getConfig().getString("npc.skin.texture.value", "");
                String signature = plugin.getConfig().getString("npc.skin.texture.signature", "");
                
                if (!value.isEmpty() && !signature.isEmpty()) {
                    skin = new Skin(null, value, signature);
                    plugin.getLogger().info("[NPC] Используется кастомный скин из конфига");
                } else {
                    plugin.getLogger().warning("[NPC] Настроен кастомный скин, но значения текстуры пусты, используется стандартный");
                }
            }
            
            final Skin finalSkin = skin;
            
            // Create NPC on main thread
            trackTask(npcId, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                Component npcName = Component.text("Посланник").color(NamedTextColor.GOLD);
                UUID npcUuid = UUID.randomUUID();
                
                // Create NPC
                NPC npc = new NPC(location, npcUuid, npcName);
                
                // Apply skin using NpcOption.SKIN (proper way)
                if (finalSkin != null) {
                    npc.setOption(NpcOption.SKIN, finalSkin);
                    plugin.getLogger().info("[NPC] Скин успешно применён к NPC");
                } else {
                    plugin.getLogger().info("[NPC] NPC создан со стандартным скином");
                }
                
                // Enable NPC and set options
                npc.setEnabled(true);
                npc.setOption(NpcOption.GLOWING, ChatFormat.GOLD); // Golden glow effect
                npc.setOption(NpcOption.LOOK_AT_PLAYER, 8.0); // Look at nearby players
                npc.setOption(NpcOption.SHOW_TAB_LIST, false); // Bug #7 Fix: Hide from TAB list
                
                // Show NPC to all players
                npc.showNpcToAllPlayers();
                
                npcEntities.put(npcId, npc);
                
                // Epic spawn particles
                world.spawnParticle(Particle.PORTAL, location, 300, 0.5, 1, 0.5, 1);
                world.spawnParticle(Particle.END_ROD, location, 100, 0.8, 1.5, 0.8, 0.1);
                world.spawnParticle(Particle.ENCHANT, location, 150, 1, 1.5, 1, 1);
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, location, 2, 0, 0, 0, 0);
                world.spawnParticle(Particle.GLOW, location, 50, 0.5, 1, 0.5, 0);
                
                world.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.0f);
                world.playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
                world.playSound(location, Sound.BLOCK_BELL_USE, 1.2f, 1.3f);
                
                // Continuous mystical aura around NPC
                startMessengerAura(npc, npcId, location);
                
                // Start continuous idle animations (breathing, micro-movements)
                startIdleAnimations(npcId, location);
                
                // Wait a bit for NPC to be fully loaded, then start dialog animations
                trackTask(npcId, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    startMessengerAnimations(npcId, location);
                }, 20L)); // Wait 1 second after spawn
                
                // Despawn timing now controlled by DialogManager via despawnMessenger() call
                // This ensures exact synchronization with the "*Посланник исчезает в тумане*" dialog line
            }, 200L)); // 10 seconds from async skin fetch
        }); // End of async task
        
        // PHASE 4: Mystical whispers (delay: 15s) - "Finally... You have arrived"
        trackTask(npcId, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            world.spawnParticle(Particle.ENCHANT, location, 80, 1.5, 1.5, 1.5, 0.5);
            world.spawnParticle(Particle.END_ROD, location, 40, 1, 1.5, 1, 0.05);
            world.playSound(location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
        }, 300L)); // 15 seconds
        
        // PHASE 5: Ancient power reveal (delay: 27s) - "Portals are crumbling"
        trackTask(npcId, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            world.spawnParticle(Particle.REVERSE_PORTAL, location, 100, 2, 1.5, 2, 0.2);
            world.spawnParticle(Particle.SMOKE, location, 60, 1.5, 1, 1.5, 0.05);
            world.playSound(location, Sound.BLOCK_GLASS_BREAK, 1.5f, 0.8f);
        }, 540L)); // 27 seconds
        
        // PHASE 6: Hope spark (delay: 35s) - "But you can stop this"
        trackTask(npcId, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            world.spawnParticle(Particle.GLOW, location, 100, 1, 1.5, 1, 0);
            world.spawnParticle(Particle.END_ROD, location, 50, 1, 1.5, 1, 0.1);
            world.playSound(location, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.5f);
        }, 700L)); // 35 seconds
        
        // PHASE 7: Ancient altar power (delay: 49s) - "Only it can restore the balance"
        trackTask(npcId, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            world.spawnParticle(Particle.ENCHANT, location, 120, 1.5, 1.5, 1.5, 1);
            world.spawnParticle(Particle.PORTAL, location, 80, 1, 1, 1, 0.5);
            world.playSound(location, Sound.BLOCK_BEACON_AMBIENT, 1.5f, 1.2f);
        }, 980L)); // 49 seconds
        
        // PHASE 8: Final mystical guidance (delay: 63s) - "The Messenger vanishes"
        trackTask(npcId, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            world.spawnParticle(Particle.REVERSE_PORTAL, location, 250, 0.5, 1.5, 0.5, 0.5);
            world.spawnParticle(Particle.END_ROD, location, 100, 0.8, 1.5, 0.8, 0.15);
            world.spawnParticle(Particle.ENCHANT, location, 150, 1, 1.5, 1, 1.5);
            world.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.2f);
            world.playSound(location, Sound.BLOCK_PORTAL_TRAVEL, 1.0f, 1.5f);
        }, 1260L)); // 63 seconds
        
        // Play dialog immediately
        plugin.getDialogManager().playDialogForAll("messenger.spawn");
    }
    
    // New method: Synchronized animations with dialog timing (head nods + particles)
    private void startMessengerAnimations(String npcId, Location location) {
        World world = location.getWorld();
        
        // Dialog at 0s: "???: ...Здесь есть кто-нибудь?" - Mysterious look around
        trackTask(npcId, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            NPC npc = npcEntities.get(npcId);
            if (npc == null) return;
            animateTalkingMovement(npc, location, 80, 3); // 4 seconds of talking motion
        }, 0L));
        
        // Dialog at 5s: "*Голос становится яснее*" - Head tilt
        trackTask(npcId, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            NPC npc = npcEntities.get(npcId);
            if (npc == null) return;
            animateHeadTilt(npc, location, 20, 15); // Slight tilt
        }, 100L));
        
        // Dialog at 10s: "*Из тумана материализуется фигура*" - Materializing (handled by spawn)
        
        // Animation at 15s: "Наконец-то... Вы пришли" - Gentle welcoming nod
        trackTask(npcId, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            NPC npc = npcEntities.get(npcId);
            if (npc == null) return;
            
            // Gentle head nod with squash/stretch + talking motion
            animateHeadNod(npc, location, 20, -15, 15);
            animateSquashStretch(npc, location, 20, 1.0, 1.15, 0.95, null);
            
            // Continue talking motion for 6 seconds (until next dialog)
            trackTask(npcId, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                NPC innerNpc = npcEntities.get(npcId);
                if (innerNpc == null) return;
                animateTalkingMovement(innerNpc, location, 100, 4); // 5 seconds
                }, 25L));
            
            world.spawnParticle(Particle.END_ROD, location.clone().add(0, 1.8, 0), 12, 0.4, 0.3, 0.4, 0.06);
            world.spawnParticle(Particle.GLOW, location.clone().add(0, 1.5, 0), 8, 0.3, 0.2, 0.3, 0.02);
            world.playSound(location, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.7f, 1.8f);
        }, 100L)); // 5s after NPC spawn (total 15s)
        
        // Animation at 21s: "Меня зовут Посланник" - Formal introduction with head turn
        trackTask(npcId, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            NPC npc = npcEntities.get(npcId);
            if (npc == null) return;
            
            // Slow head turn left-right with dramatic squash/stretch
            animateHeadTurn(npc, location, 30, -30, 30);
            animateSquashStretch(npc, location, 30, 1.0, 1.25, 0.85, null);
            
            // Talking motion during dialog
            trackTask(npcId, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                NPC innerNpc = npcEntities.get(npcId);
                if (innerNpc == null) return;
                animateTalkingMovement(innerNpc, location, 100, 4);
            }, 35L));
            
            world.spawnParticle(Particle.END_ROD, location.clone().add(0, 1.6, 0), 18, 0.5, 0.3, 0.5, 0.08);
            world.spawnParticle(Particle.ENCHANT, location.clone().add(0, 1.3, 0), 25, 0.7, 0.4, 0.7, 0.6);
            world.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 2.0f);
        }, 220L)); // 11s after NPC spawn (total 21s)
        
        // Animation at 27s: "Мир умирает, Странник" - Sad head shake
        trackTask(npcId, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            NPC npc = npcEntities.get(npcId);
            if (npc == null) return;
            
            // Sad head shake (down) with reverse squash/stretch (compressed)
            animateHeadNod(npc, location, 25, 20, -10);
            animateSquashStretch(npc, location, 25, 1.0, 0.85, 1.15, null);
            
            // Talking motion
            trackTask(npcId, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                NPC innerNpc = npcEntities.get(npcId);
                if (innerNpc == null) return;
                animateTalkingMovement(innerNpc, location, 140, 5); // 7 seconds
            }, 30L));
            
            world.spawnParticle(Particle.SMOKE, location.clone().add(0, 1.5, 0), 20, 0.6, 0.4, 0.6, 0.05);
            world.spawnParticle(Particle.REVERSE_PORTAL, location.clone().add(0, 1.2, 0), 15, 0.5, 0.3, 0.5, 0.3);
            world.playSound(location, Sound.BLOCK_GLASS_BREAK, 1.2f, 0.8f);
        }, 340L)); // 17s after spawn (total 27s)
        
        // Animation at 35s: "Но вы можете это остановить" - Hopeful encouraging nod
        trackTask(npcId, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            NPC npc = npcEntities.get(npcId);
            if (npc == null) return;
            
            // Encouraging head nod with big stretch
            animateHeadNod(npc, location, 24, -20, 10);
            animateSquashStretch(npc, location, 24, 1.0, 1.3, 0.8, null);
            
            // Talking motion
            trackTask(npcId, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                NPC innerNpc = npcEntities.get(npcId);
                if (innerNpc == null) return;
                animateTalkingMovement(innerNpc, location, 100, 4); // 5 seconds
            }, 28L));
            
            world.spawnParticle(Particle.GLOW, location.clone().add(0, 1.7, 0), 30, 0.8, 0.5, 0.8, 0.05);
            world.spawnParticle(Particle.END_ROD, location.clone().add(0, 1.4, 0), 20, 0.5, 0.3, 0.5, 0.1);
            world.playSound(location, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.5f);
        }, 500L)); // 25s after spawn (total 35s)
        
        // Dialog at 41s: "Найдите Первый Алтарь" - Emphasis with multiple nods
        trackTask(npcId, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            NPC npc = npcEntities.get(npcId);
            if (npc == null) return;
            animateTalkingMovement(npc, location, 140, 6); // 7 seconds of talking
        }, 620L));

        // Dialog at 49s: "Только он может восстановить равновесие" - Serious nod
        trackTask(npcId, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            NPC npc = npcEntities.get(npcId);
            if (npc == null) return;
            animateTalkingMovement(npc, location, 100, 4); // 5 seconds
        }, 780L));
        
        // Animation at 55s: "Ищите руины с фиолетовым свечением" - Point north
        trackTask(npcId, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            NPC npc = npcEntities.get(npcId);
            if (npc == null) return;
            
            // Turn head to face north and look up slightly with stretch
            animateHeadLookDirection(npc, location, 35, 180, -10);
            animateSquashStretch(npc, location, 35, 1.0, 1.2, 0.9, null);
            
            // Talking motion while pointing
            trackTask(npcId, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                NPC innerNpc = npcEntities.get(npcId);
                if (innerNpc == null) return;
                animateTalkingMovement(innerNpc, location, 140, 5); // 7 seconds
            }, 40L));
            
            // Directional particle line pointing north
            Location particleLoc = location.clone().add(0, 1.5, 0);
            for (int i = 0; i < 10; i++) {
                final int index = i;
                trackTask(npcId, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    world.spawnParticle(Particle.END_ROD,
                        particleLoc.clone().add(0, 0, -index * 0.3),
                        3, 0.1, 0.1, 0.1, 0.01);
                }, i * 2L));
            }

            world.spawnParticle(Particle.PORTAL, location.clone().add(0, 1.5, 0), 25, 0.6, 0.4, 0.6, 0.8);
            world.playSound(location, Sound.BLOCK_BEACON_AMBIENT, 1.2f, 1.3f);
        }, 900L)); // 45s after spawn (total 55s)
    }
    
    // Animate talking - constant micro head movements like a YouTuber
    private void animateTalkingMovement(NPC npc, Location baseLocation, int totalFrames, int nodCount) {
        new BukkitRunnable() {
            int frame = 0;
            float baseYaw = baseLocation.getYaw();
            float basePitch = baseLocation.getPitch();
            
            @Override
            public void run() {
                if (frame >= totalFrames) {
                    // Return to base position
                    Location resetLoc = baseLocation.clone();
                    npc.setLocation(resetLoc);
                    cancel();
                    return;
                }
                
                // Multiple sine waves for natural talking motion
                double progress = (double) frame / totalFrames;
                
                // Main nod cycle - nod down and up multiple times
                double nodCycle = Math.sin(progress * Math.PI * 2 * nodCount);
                float pitchOffset = (float) (nodCycle * 3); // ±3 degrees
                
                // Subtle side-to-side
                double sideCycle = Math.sin(progress * Math.PI * 4 * nodCount);
                float yawOffset = (float) (sideCycle * 2); // ±2 degrees
                
                // Random micro-jitter for naturalness
                float jitterYaw = (float) ((Math.random() - 0.5) * 1);
                float jitterPitch = (float) ((Math.random() - 0.5) * 1);
                
                Location newLoc = baseLocation.clone();
                newLoc.setYaw(baseYaw + yawOffset + jitterYaw);
                newLoc.setPitch(basePitch + pitchOffset + jitterPitch);
                npc.setLocation(newLoc);
                
                frame++;
            }
        }.runTaskTimer(plugin, 0L, 1L); // Run every tick for smooth 20fps
    }
    
    // Head tilt animation (subtle pitch change)
    private void animateHeadTilt(NPC npc, Location baseLocation, int frames, float tiltAngle) {
        new BukkitRunnable() {
            int frame = 0;
            float startPitch = baseLocation.getPitch();
            
            @Override
            public void run() {
                if (frame >= frames) {
                    cancel();
                    return;
                }
                
                float progress = (float) frame / frames;
                float pitch = startPitch + (tiltAngle * easeInOutSine(progress));
                
                Location newLoc = baseLocation.clone();
                newLoc.setPitch(pitch);
                npc.setLocation(newLoc);
                
                frame++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    // Head nod animation (pitch up/down)
    private void animateHeadNod(NPC npc, Location location, int frames, float startPitch, float endPitch) {
        new BukkitRunnable() {
            int frame = 0;
            float originalYaw = location.getYaw();
            
            @Override
            public void run() {
                if (frame >= frames) {
                    cancel();
                    return;
                }
                
                float progress = (float) frame / frames;
                float pitch = startPitch + (endPitch - startPitch) * easeInOutSine(progress);
                
                Location newLoc = location.clone();
                newLoc.setYaw(originalYaw);
                newLoc.setPitch(pitch);
                npc.setLocation(newLoc);
                
                frame++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    // Head turn animation (yaw left/right)
    private void animateHeadTurn(NPC npc, Location location, int frames, float startYawOffset, float endYawOffset) {
        new BukkitRunnable() {
            int frame = 0;
            float originalYaw = location.getYaw();
            float originalPitch = location.getPitch();
            
            @Override
            public void run() {
                if (frame >= frames) {
                    cancel();
                    return;
                }
                
                float progress = (float) frame / frames;
                float yawOffset = startYawOffset + (endYawOffset - startYawOffset) * easeInOutSine(progress);
                
                Location newLoc = location.clone();
                newLoc.setYaw(originalYaw + yawOffset);
                newLoc.setPitch(originalPitch);
                npc.setLocation(newLoc);
                
                frame++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    // Look in specific direction (for pointing north)
    private void animateHeadLookDirection(NPC npc, Location location, int frames, float targetYaw, float targetPitch) {
        new BukkitRunnable() {
            int frame = 0;
            float originalYaw = location.getYaw();
            float originalPitch = location.getPitch();
            
            @Override
            public void run() {
                if (frame >= frames) {
                    cancel();
                    return;
                }
                
                float progress = (float) frame / frames;
                float yaw = originalYaw + (targetYaw - originalYaw) * easeInOutSine(progress);
                float pitch = originalPitch + (targetPitch - originalPitch) * easeInOutSine(progress);
                
                Location newLoc = location.clone();
                newLoc.setYaw(yaw);
                newLoc.setPitch(pitch);
                npc.setLocation(newLoc);
                
                frame++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    // Smooth easing function for natural head movement
    private float easeInOutSine(float x) {
        return (float) (-(Math.cos(Math.PI * x) - 1) / 2);
    }
    
    // Improved aura method for NPC with task tracking
    private void startMessengerAura(NPC npc, String npcId, Location location) {
        BukkitRunnable task = new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (npc == null || ticks >= 1200) { // 60 seconds max
                    cancel();
                    return;
                }
                
                Location loc = location.clone().add(0, 1, 0);
                double angle = ticks * 0.1;
                double radius = 0.8;
                
                // Floating runes effect
                for (int i = 0; i < 3; i++) {
                    double x = Math.cos(angle + i * Math.PI * 2 / 3) * radius;
                    double z = Math.sin(angle + i * Math.PI * 2 / 3) * radius;
                    double y = Math.sin(ticks * 0.05 + i) * 0.3;
                    
                    loc.getWorld().spawnParticle(Particle.ENCHANT, 
                        loc.clone().add(x, y, z), 1, 0, 0, 0, 0);
                }
                
                // Occasional sparkle
                if (ticks % 40 == 0) {
                    loc.getWorld().spawnParticle(Particle.END_ROD, loc, 5, 0.3, 0.5, 0.3, 0.01);
                }
                
                // Mystical fog
                if (ticks % 20 == 0) {
                    loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc, 3, 0.5, 0.3, 0.5, 0.01);
                }
                
                ticks++;
            }
        };
        task.runTaskTimer(plugin, 0L, 2L);
        auraTask.put(npcId, task); // Store for later cancellation
    }
    
    // Continuous idle animations - NPC looks alive like a YouTuber
    private void startIdleAnimations(String npcId, Location baseLocation) {
        BukkitRunnable idleTask = new BukkitRunnable() {
            int cycle = 0;
            
            @Override
            public void run() {
                NPC npc = npcEntities.get(npcId);
                if (npc == null || cycle >= 1200) { // 60 seconds max
                    cancel();
                    return;
                }
                
                // Breathing effect - subtle scale changes (very subtle, 1.0 ± 0.03)
                double breathProgress = Math.sin(cycle * 0.05); // Slow sine wave
                double breathScale = 1.0 + (breathProgress * 0.03);
                npc.setOption(NpcOption.SCALE, breathScale);
                
                // Micro head movements every 2-4 seconds
                if (cycle % 60 == 0) { // Every 3 seconds
                    // Small random head tilt
                    float randomPitch = -5 + (float)(Math.random() * 10); // -5 to +5 degrees
                    float randomYaw = -8 + (float)(Math.random() * 16); // -8 to +8 degrees
                    
                    animateSmallHeadMovement(npc, baseLocation, 15, randomYaw, randomPitch);
                }
                
                // Occasional blink/nod every 5-7 seconds
                if (cycle % 100 == 0) { // Every 5 seconds
                    animateQuickNod(npc, baseLocation);
                }
                
                cycle++;
            }
        };
        idleTask.runTaskTimer(plugin, 20L, 2L); // Start after 1 second, run every 2 ticks
        auraTask.put(npcId + "_idle", idleTask); // Store with unique key
    }
    
    // Small natural head movement (like YouTuber micro-adjustments)
    private void animateSmallHeadMovement(NPC npc, Location baseLocation, int frames, float yawOffset, float pitchOffset) {
        new BukkitRunnable() {
            int frame = 0;
            float startYaw = baseLocation.getYaw();
            float startPitch = baseLocation.getPitch();
            
            @Override
            public void run() {
                if (frame >= frames) {
                    cancel();
                    return;
                }
                
                float progress = (float) frame / frames;
                float easedProgress = easeInOutSine(progress);
                
                // Move to offset and back
                float currentYawOffset = (float) (yawOffset * Math.sin(progress * Math.PI));
                float currentPitchOffset = (float) (pitchOffset * Math.sin(progress * Math.PI));
                
                Location newLoc = baseLocation.clone();
                newLoc.setYaw(startYaw + currentYawOffset);
                newLoc.setPitch(startPitch + currentPitchOffset);
                npc.setLocation(newLoc);
                
                frame++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    // Quick nod animation (like agreement or natural movement)
    private void animateQuickNod(NPC npc, Location baseLocation) {
        new BukkitRunnable() {
            int frame = 0;
            final int totalFrames = 12; // Fast nod
            float startPitch = baseLocation.getPitch();
            
            @Override
            public void run() {
                if (frame >= totalFrames) {
                    cancel();
                    return;
                }
                
                float progress = (float) frame / totalFrames;
                // Quick down and up: 0 -> -10 -> 0
                float pitch = startPitch + (float) (-10 * Math.sin(progress * Math.PI));
                
                Location newLoc = baseLocation.clone();
                newLoc.setPitch(pitch);
                npc.setLocation(newLoc);
                
                frame++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    // Despawn animation with scale fade out
    private void startMessengerDespawn(String npcId, Location location) {
        NPC npc = npcEntities.get(npcId);
        if (npc == null) {
            return;
        }
        
        // Cancel any scheduled one-off tasks for this NPC
        cancelScheduledTasks(npcId);
        
        // Cancel aura particles immediately
        BukkitRunnable aura = auraTask.remove(npcId);
        if (aura != null) {
            aura.cancel();
        }
        
        // Cancel idle animations
        BukkitRunnable idle = auraTask.remove(npcId + "_idle");
        if (idle != null) {
            idle.cancel();
        }
        
        World world = location.getWorld();
        
        // Immediate particle cleanup before starting despawn animation
        clearParticlesInRadius(location, 10.0);
        
        // Vanish effect over 5 seconds (100 ticks) with shrinking scale
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 100) {
                    // Final particle cleanup after despawn completes
                    clearParticlesInRadius(location, 10.0);
                    removeNPC(npcId);
                    cancel();
                    return;
                }
                
                NPC currentNpc = npcEntities.get(npcId);
                if (currentNpc == null) {
                    cancel();
                    return;
                }
                
                Location loc = location.clone().add(0, 1, 0);
                
                // Calculate shrinking scale (1.0 -> 0.0)
                double progress = (double) ticks / 100;
                double scale = 1.0 - progress;
                currentNpc.setOption(NpcOption.SCALE, Math.max(0.01, scale)); // Prevent 0 scale
                
                // Intensifying portal effect
                if (ticks % 5 == 0) {
                    world.spawnParticle(Particle.REVERSE_PORTAL, loc, 10 + ticks / 2, 0.3, 0.5, 0.3, 0.1);
                    world.spawnParticle(Particle.END_ROD, loc, 3 + ticks / 5, 0.2, 0.3, 0.2, 0.05);
                }
                
                // Fade out particles
                if (ticks > 80) {
                    world.spawnParticle(Particle.ENCHANT, loc, 5, 0.5, 0.5, 0.5, 0.5);
                }
                
                // Sound effects
                if (ticks % 20 == 0) {
                    float volume = 0.5f + ((float)progress * 0.5f);
                    float pitch = 1.5f - ((float)progress * 0.5f);
                    world.playSound(location, Sound.BLOCK_PORTAL_AMBIENT, volume, pitch);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    // Old methods removed/updated
    private void startMessengerAura(org.bukkit.entity.ArmorStand npc) {
        // Legacy method - kept for compatibility but not used
    }
    
    public void removeNPC(String npcId) {
        NPC npc = npcEntities.remove(npcId);
        if (npc != null) {
            npc.hideNpcFromAllPlayers();
        }
        
        // Enhanced task cancellation with logging
        int cancelledTasks = 0;
        
        // Cancel any scheduled one-off tasks for this NPC
        try {
            cancelScheduledTasks(npcId);
            cancelledTasks++;
        } catch (Exception e) {
            plugin.getLogger().warning("[NPC] Error cancelling scheduled tasks for " + npcId + ": " + e.getMessage());
        }
        
        // Cancel aura particles if still running with enhanced null checking
        try {
            BukkitRunnable aura = auraTask.remove(npcId);
            if (aura != null && !aura.isCancelled()) {
                aura.cancel();
                cancelledTasks++;
                plugin.getLogger().info("[NPC] Cancelled aura task for " + npcId);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[NPC] Error cancelling aura task for " + npcId + ": " + e.getMessage());
        }
        
        // Cancel idle animations if still running with enhanced null checking
        try {
            BukkitRunnable idle = auraTask.remove(npcId + "_idle");
            if (idle != null && !idle.isCancelled()) {
                idle.cancel();
                cancelledTasks++;
                plugin.getLogger().info("[NPC] Cancelled idle task for " + npcId);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[NPC] Error cancelling idle task for " + npcId + ": " + e.getMessage());
        }
        
        // Additional safety check: ensure no remaining tasks for this NPC
        try {
            java.util.List<BukkitTask> remainingTasks = scheduledTasks.get(npcId);
            if (remainingTasks != null) {
                for (BukkitTask task : remainingTasks) {
                    if (task != null && !task.isCancelled()) {
                        task.cancel();
                        cancelledTasks++;
                    }
                }
                scheduledTasks.remove(npcId);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[NPC] Error in final task cleanup for " + npcId + ": " + e.getMessage());
        }
        
        if (cancelledTasks > 0) {
            plugin.getLogger().info("[NPC] Successfully cancelled " + cancelledTasks + " tasks for NPC " + npcId);
        }
    }
    
    /**
     * Remove NPC by name (for messenger NPC)
     * @param player The player who triggered the removal
     * @param npcName The name of the NPC to remove
     */
    public void removeNpcByName(Player player, String npcName) {
        // Find the messenger NPC by checking if the name contains "Посланник"
        for (Map.Entry<String, NPC> entry : npcEntities.entrySet()) {
            if (entry.getKey().contains("messenger") || entry.getKey().contains("Messenger")) {
                NPC npc = entry.getValue();
                if (npc != null) {
                    // Play disappearance effect at NPC location
                    Location npcLocation = npc.getLocation();
                    World world = npcLocation.getWorld();
                    
                    // Create smoke effect
                    for (int i = 0; i < 20; i++) {
                        double offsetX = (Math.random() - 0.5) * 2;
                        double offsetY = Math.random() * 2;
                        double offsetZ = (Math.random() - 0.5) * 2;
                        
                        world.spawnParticle(Particle.SMOKE, npcLocation, 10, offsetX, offsetY, offsetZ, 0.1);
                    }
                    
                    // Play disappearance sound
                    world.playSound(npcLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    
                    // Remove the NPC via centralized removal to cancel tasks
                    removeNPC(entry.getKey());
                    
                    plugin.getLogger().info("[NPC] Removed messenger NPC: " + entry.getKey());
                    return;
                }
            }
        }
    }
    
    public void removeNPC(UUID npcId) {
        // Legacy UUID-based removal - redirect to string-based
        removeNPC(npcId.toString());
    }
    
    /**
     * Despawn the current messenger NPC (if one exists).
     * This should be called by DialogManager at the exact "*Посланник исчезает в тумане*" timing.
     * Triggers the animated 5-second shrinking despawn effect.
     */
    public void despawnMessenger() {
        if (currentMessengerId == null) {
            plugin.getLogger().warning("[NPC] despawnMessenger() called but no messenger is tracked");
            return;
        }

        if (currentMessengerLocation == null) {
            plugin.getLogger().warning("[NPC] despawnMessenger() called but messenger location is null, using immediate removal");
            removeNPC(currentMessengerId);
            currentMessengerId = null;
            return;
        }

        plugin.getLogger().info("[NPC] Starting enhanced despawn animation for messenger: " + currentMessengerId);

        // Use the enhanced despawn method with improved cleanup and idempotency
        enhancedDespawnMessenger();

        // Clear tracking (actual NPC removal happens in enhanced method)
        currentMessengerId = null;
        currentMessengerLocation = null;
    }
    
    public void cleanup() {
        plugin.getLogger().info("[NPC] Cleaning up all NPCs and animations...");
        
        // Cancel all aura tasks
        for (BukkitRunnable task : auraTask.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        auraTask.clear();
        
        // Remove all NPCs (including messenger if still present)
        int removedCount = 0;
        
        for (Map.Entry<String, NPC> entry : npcEntities.entrySet()) {
            NPC npc = entry.getValue();
            if (npc != null) {
                try {
                    // Hide NPC from all players
                    npc.hideNpcFromAllPlayers();
                    removedCount++;
                    
                    // Log if this is a potential tiny NPC (messenger-related)
                    if (entry.getKey().contains("messenger") || entry.getKey().contains("Messenger")) {
                        plugin.getLogger().info("[NPC] Removed messenger NPC: " + entry.getKey());
                    }
                    
                } catch (Exception e) {
                    plugin.getLogger().warning("[NPC] Error removing NPC '" + entry.getKey() + "' during cleanup: " + e.getMessage());
                }
            }
        }
        npcEntities.clear();
        
        // Clear messenger tracking
        currentMessengerId = null;
        currentMessengerLocation = null;
        
        // Cancel any remaining scheduled tasks for safety
        for (java.util.List<BukkitTask> tasks : scheduledTasks.values()) {
            if (tasks != null) {
                for (BukkitTask t : tasks) {
                    if (t != null) t.cancel();
                }
            }
        }
        scheduledTasks.clear();

        // Clear despawning NPCs tracking
        despawningNpcs.clear();

        plugin.getLogger().info("[NPC] Cleanup complete - removed " + removedCount + " NPCs");
    }
    
    // Squash & Stretch animation helper (24 FPS smooth animation)
    private void animateSquashStretch(NPC npc, Location location, int frames, 
                                      double startScale, double maxScale, double minScale, 
                                      Runnable onPeak) {
        new BukkitRunnable() {
            int frame = 0;
            
            @Override
            public void run() {
                if (frame >= frames || npc == null) {
                    // Reset to normal scale
                    if (npc != null) {
                        npc.setOption(NpcOption.SCALE, startScale);
                    }
                    cancel();
                    return;
                }
                
                // Calculate scale using smooth easing (sine wave for natural motion)
                double progress = (double) frame / frames;
                double angle = progress * Math.PI; // 0 to PI for one complete bounce
                
                // Squash & Stretch formula: oscillate between min and max scale
                double scale;
                if (progress < 0.5) {
                    // First half: stretch up (anticipation)
                    scale = startScale + (maxScale - startScale) * Math.sin(angle);
                } else {
                    // Second half: squash down then return (follow-through)
                    double reverseProgress = (progress - 0.5) * 2;
                    scale = startScale + (minScale - startScale) * Math.sin(reverseProgress * Math.PI * 0.5);
                }
                
                // Apply scale
                npc.setOption(NpcOption.SCALE, scale);
                
                // Trigger effect at peak (middle of animation)
                if (frame == frames / 2 && onPeak != null) {
                    onPeak.run();
                }
                
                frame++;
            }
        }.runTaskTimer(plugin, 0L, 1L); // 1 tick = 50ms ≈ 20 FPS (close to 24 FPS)
    }
    
    public void giveDirectionMarker(Player player, Location target) {
        if (player == null || target == null) {
            return;
        }
        
        // Set player's compass target
        player.setCompassTarget(target);
        player.sendMessage(Component.text("§6Вы получили метку направления! Следуйте за компасом (5 минут)"));
        
        // Reset compass after 5 minutes
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.setCompassTarget(player.getWorld().getSpawnLocation());
                player.sendMessage(Component.text("§7Метка направления исчезла"));
            }
        }, 6000L); // 5 minutes = 6000 ticks
    }
    
    /**
     * Clear all particles in a specified radius around a location
     * @param center Center location for particle cleanup
     * @param radius Radius in blocks to clear particles (default 15 blocks for better cleanup)
     */
    private void clearParticlesInRadius(Location center, double radius) {
        if (center == null || center.getWorld() == null) {
            return;
        }

        World world = center.getWorld();

        // Immediate visual override particle burst - more intensive for better cleanup
        for (int i = 0; i < 50; i++) {
            double offsetX = (Math.random() - 0.5) * radius * 2;
            double offsetY = Math.random() * radius * 1.5; // Increased vertical coverage
            double offsetZ = (Math.random() - 0.5) * radius * 2;

            world.spawnParticle(Particle.END_ROD, center.clone().add(offsetX, offsetY, offsetZ), 1, 0, 0, 0, 0);
        }

        // Additional cleanup wave with larger radius to catch residual effects
        world.spawnParticle(Particle.END_ROD, center, 100, radius * 1.2, radius * 1.5, radius * 1.2, 0.05);

        // Follow-up with reverse portal particles to override any portal effects
        world.spawnParticle(Particle.REVERSE_PORTAL, center, 30, radius * 0.8, radius, radius * 0.8, 0.1);

        plugin.getLogger().info("[NPC] Enhanced particle cleanup in radius " + radius + " around " + center);
    }

    /**
     * Enhanced particle cleanup with configurable method and timing
     */
    private void enhancedParticleCleanup(Location center, String cleanupMethod, double radius) {
        if (center == null || center.getWorld() == null) {
            return;
        }

        World world = center.getWorld();

        switch (cleanupMethod.toLowerCase()) {
            case "override":
                // Enhanced override method: Aggressive particle clearing with multiple particle types
                // First wave: Heavy END_ROD burst to override existing particles
                for (int i = 0; i < 60; i++) {
                    double offsetX = (Math.random() - 0.5) * radius * 2.5;
                    double offsetY = Math.random() * radius * 1.8;
                    double offsetZ = (Math.random() - 0.5) * radius * 2.5;

                    world.spawnParticle(Particle.END_ROD, center.clone().add(offsetX, offsetY, offsetZ), 1, 0, 0, 0, 0);
                }

                // Second wave: Wider radius ENCHANT particles to override magical effects
                world.spawnParticle(Particle.ENCHANT, center, 80, radius * 1.2, radius * 1.5, radius * 1.2, 0.02);

                // Third wave: REVERSE_PORTAL to override any portal effects
                world.spawnParticle(Particle.REVERSE_PORTAL, center, 40, radius * 1.0, radius * 1.3, radius * 1.0, 0.08);
                break;

            case "wait":
                // Wait method: Minimal intervention - just mark area
                world.spawnParticle(Particle.END_ROD, center, 10, radius * 0.3, radius * 0.3, radius * 0.3, 0.02);
                break;

            default:
                // Default to enhanced override method
                enhancedParticleCleanup(center, "override", radius);
                return;
        }

        plugin.getLogger().info("[NPC] Enhanced particle cleanup using method: " + cleanupMethod);
    }

    /**
     * Checks if NPC despawn effects are enabled in configuration
     */
    private boolean isDespawnEffectEnabled(String effectType) {
        try {
            String basePath = "npc.despawn.";
            return plugin.getConfigManager().getConfig().getBoolean(basePath + "visualEffects.enabled", true) &&
                   plugin.getConfigManager().getConfig().getBoolean(basePath + "visualEffects." + effectType, true);
        } catch (Exception e) {
            plugin.getLogger().warning("[NPC] Error checking despawn effect config for " + effectType + ": " + e.getMessage());
            return true; // Default to enabled
        }
    }

    /**
     * Gets despawn configuration value with fallback
     */
    private double getDespawnConfigValue(String path, double defaultValue) {
        try {
            return plugin.getConfigManager().getConfig().getDouble("npc.despawn." + path, defaultValue);
        } catch (Exception e) {
            plugin.getLogger().warning("[NPC] Error getting despawn config for " + path + ": " + e.getMessage());
            return defaultValue;
        }
    }

    /**
     * Ensures NPC operations are idempotent by checking if already processed
     */
    private boolean isNpcBeingDespawned(String npcId) {
        return despawningNpcs.containsKey(npcId);
    }

    /**
     * Marks NPC as being despawned to prevent duplicate operations
     */
    private void markNpcAsDespawning(String npcId, Location location) {
        despawningNpcs.put(npcId, System.currentTimeMillis());
        plugin.getLogger().info("[NPC] Marked " + npcId + " as despawning at " + location);
    }

    /**
     * Removes NPC from despawning tracking
     */
    private void unmarkNpcAsDespawning(String npcId) {
        despawningNpcs.remove(npcId);
    }

    /**
     * Enhanced despawn with improved cleanup and idempotency
     */
    public void enhancedDespawnMessenger() {
        String npcId = currentMessengerId != null ? currentMessengerId : "messenger";
        Location npcLocation = currentMessengerLocation;

        if (npcLocation == null) {
            plugin.getLogger().warning("[NPC] Cannot despawn messenger - location unknown");
            return;
        }

        // Check idempotency - prevent duplicate despawns
        if (isNpcBeingDespawned(npcId)) {
            plugin.getLogger().info("[NPC] Messenger already being despawned, skipping duplicate request");
            return;
        }

        markNpcAsDespawning(npcId, npcLocation);

        try {
            // Get configuration values
            boolean visualEffectsEnabled = isDespawnEffectEnabled("enabled");
            boolean particlesEnabled = isDespawnEffectEnabled("showParticles");
            boolean immediateCleanup = isDespawnEffectEnabled("particleCleanup.immediateCleanup");
            boolean finalCleanup = isDespawnEffectEnabled("particleCleanup.finalCleanup");
            double cleanupRadius = getDespawnConfigValue("visualEffects.cleanupRadius", 15.0);
            int animationDuration = (int) getDespawnConfigValue("visualEffects.animationDuration", 100);
            String cleanupMethod = plugin.getConfigManager().getConfig().getString("npc.despawn.particleCleanup.cleanupMethod", "override");

            if (!visualEffectsEnabled) {
                // Simple despawn without effects
                removeNPC(npcId);
                unmarkNpcAsDespawning(npcId);
                return;
            }

            // Immediate particle cleanup if configured
            if (immediateCleanup) {
                enhancedParticleCleanup(npcLocation, cleanupMethod, cleanupRadius);
            }

            // Start enhanced despawn animation
            startEnhancedMessengerDespawn(npcId, npcLocation, animationDuration, particlesEnabled, cleanupRadius, finalCleanup, cleanupMethod);

        } catch (Exception e) {
            plugin.getLogger().severe("[NPC] Error during enhanced despawn: " + e.getMessage());
            e.printStackTrace();

            // Fallback to simple removal
            removeNPC(npcId);
            unmarkNpcAsDespawning(npcId);
        }
    }

    /**
     * Enhanced despawn animation with configurable parameters
     */
    private void startEnhancedMessengerDespawn(String npcId, Location location, int durationTicks,
                                              boolean showParticles, double cleanupRadius,
                                              boolean finalCleanup, String cleanupMethod) {
        NPC npc = npcEntities.get(npcId);
        if (npc == null) {
            unmarkNpcAsDespawning(npcId);
            return;
        }

        // Cancel existing tasks first
        cancelScheduledTasks(npcId);

        // Cancel aura and idle animations
        BukkitRunnable aura = auraTask.remove(npcId);
        if (aura != null) {
            aura.cancel();
        }

        BukkitRunnable idle = auraTask.remove(npcId + "_idle");
        if (idle != null) {
            idle.cancel();
        }

        World world = location.getWorld();

        // Immediate particle cleanup at despawn start
        plugin.getLogger().info("[NPC] Starting immediate particle cleanup for messenger despawn");
        enhancedParticleCleanup(location, cleanupMethod, cleanupRadius);

        // Despawn animation with configurable effects
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= durationTicks) {
                    // Final cleanup and removal
                    plugin.getLogger().info("[NPC] Performing final cleanup for messenger despawn");
                    if (finalCleanup) {
                        enhancedParticleCleanup(location, cleanupMethod, cleanupRadius);
                    }

                    removeNPC(npcId);
                    unmarkNpcAsDespawning(npcId);
                    cancel();
                    return;
                }

                NPC currentNpc = npcEntities.get(npcId);
                if (currentNpc == null) {
                    unmarkNpcAsDespawning(npcId);
                    cancel();
                    return;
                }

                Location loc = location.clone().add(0, 1, 0);

                // Calculate shrinking scale
                double progress = (double) ticks / durationTicks;
                double scale = 1.0 - progress;
                currentNpc.setOption(NpcOption.SCALE, Math.max(0.01, scale));

                // Particle effects if enabled
                if (showParticles) {
                    // Intensifying portal effect
                    if (ticks % 5 == 0) {
                        world.spawnParticle(Particle.REVERSE_PORTAL, loc, 10 + ticks / 2, 0.3, 0.5, 0.3, 0.1);
                        world.spawnParticle(Particle.END_ROD, loc, 3 + ticks / 5, 0.2, 0.3, 0.2, 0.05);
                    }

                    // Fade out particles
                    if (ticks > durationTicks * 0.8) {
                        world.spawnParticle(Particle.ENCHANT, loc, 5, 0.5, 0.5, 0.5, 0.5);
                    }
                }

                // Sound effects if enabled
                if (isDespawnEffectEnabled("soundEffects.enabled")) {
                    boolean fadeOut = isDespawnEffectEnabled("soundEffects.fadeOut");
                    if (ticks % 20 == 0) {
                        float volume = 0.5f;
                        float pitch = 1.5f;

                        if (fadeOut) {
                            volume = 0.5f + ((float)progress * 0.5f);
                            pitch = 1.5f - ((float)progress * 0.5f);
                        }

                        world.playSound(location, Sound.BLOCK_PORTAL_AMBIENT, volume, pitch);
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
