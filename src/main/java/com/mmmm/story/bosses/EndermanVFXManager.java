package com.mmmm.story.bosses;

import com.mmmm.story.MmmmStoryPlugin;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Manages visual effects for the Enderman boss
 * Features: portal rift entrance, teleportation effects, death effects, etc.
 */
public class EndermanVFXManager {

    private final MmmmStoryPlugin plugin;

    // Effect tasks
    private BukkitTask effectsTask;
    private BukkitTask ambianceTask;

    // Boss reference for ambient effects
    private Enderman targetBoss;

    // Configuration
    private final double effectRadius = 50.0;
    private final boolean enableAdvancedEffects = true;

    public EndermanVFXManager(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Create epic portal rift entrance sequence
     */
    public void createPortalRiftEntrance(Location location, Runnable onComplete) {
        World world = location.getWorld();

        // 5-second entrance sequence
        new BukkitRunnable() {
            private int ticks = 0;
            private final int totalTicks = 100; // 5 seconds

            @Override
            public void run() {
                if (ticks >= totalTicks) {
                    // Entrance complete
                    createFinalEntranceEffects(location);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                    this.cancel();
                    return;
                }

                double progress = (double) ticks / totalTicks;
                createEntrancePhase(location, progress);

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Create entrance phase based on progress
     */
    private void createEntrancePhase(Location location, double progress) {
        World world = location.getWorld();

        // Vertical rift intensifies over time
        double riftHeight = progress * 40; // Grows to 40 blocks high
        double riftIntensity = progress;

        // Create vertical rift particles
        for (int y = 0; y < riftHeight; y += 2) {
            Location riftLoc = location.clone().add(0, y, 0);

            // Core rift particles
            world.spawnParticle(Particle.PORTAL, riftLoc,
                (int) (20 * riftIntensity), 1.5, 0, 1.5, 0.3 * riftIntensity);
            world.spawnParticle(Particle.DRAGON_BREATH, riftLoc,
                (int) (15 * riftIntensity), 1, 0.5, 1, 0.1 * riftIntensity);

            // Energy particles spiraling around rift
            double angle = (y * 0.3 + System.currentTimeMillis() * 0.01);
            double spiralRadius = 2.0 - (progress * 1.5); // Spiral tightens

            double x = Math.cos(angle) * spiralRadius;
            double z = Math.sin(angle) * spiralRadius;

            Location spiralLoc = riftLoc.clone().add(x, 0, z);
            world.spawnParticle(Particle.END_ROD, spiralLoc, 2, 0.2, 0.2, 0.2, 0.1);
            world.spawnParticle(Particle.WITCH, spiralLoc, 1, 0.1, 0.1, 0.1, 0);
        }

        // Ground effects spreading from center
        double groundRadius = progress * 8.0;
        for (int angle = 0; angle < 360; angle += 20) {
            double radians = Math.toRadians(angle);
            double x = Math.cos(radians) * groundRadius;
            double z = Math.sin(radians) * groundRadius;

            Location groundLoc = location.clone().add(x, 0.1, z);
            world.spawnParticle(Particle.SQUID_INK, groundLoc, 1);
            world.spawnParticle(Particle.SMOKE, groundLoc, 2, 0.1, 0.1, 0.1, 0.02);
        }

        // Sound effects intensifying based on progress
        if (Math.random() < 0.3) { // Random chance for sound effects
            float volume = 0.5f + (float) (progress * 1.0f);
            float pitch = 2.0f - (float) (progress * 1.2f);

            world.playSound(location, Sound.BLOCK_END_PORTAL_SPAWN, volume, pitch);
            world.playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, volume * 0.8f, pitch * 0.7f);
        }

        // Screen darkness effect for nearby players (advance feature)
        if (enableAdvancedEffects && progress > 0.3) {
            for (Entity entity : location.getNearbyEntities(effectRadius, effectRadius, effectRadius)) {
                if (entity instanceof Player) {
                    Player player = (Player) entity;
                    // Would require advanced plugin to actually darken screen
                    // For now, just play ambient sounds
                    if (Math.random() < 0.1) {
                        player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 0.3f, 0.5f);
                    }
                }
            }
        }
    }

    /**
     * Create final entrance effects
     */
    private void createFinalEntranceEffects(Location location) {
        World world = location.getWorld();

        // Massive energy burst
        world.spawnParticle(Particle.EXPLOSION, location, 50, 2, 1, 2, 0.2);
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, location, 1);
        world.spawnParticle(Particle.DRAGON_BREATH, location, 100, 3, 2, 3, 0.3);

        // Sound effects
        world.playSound(location, Sound.ENTITY_ENDER_DRAGON_DEATH, 2.0f, 0.5f);
        world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.8f);
        world.playSound(location, Sound.BLOCK_END_PORTAL_FRAME_FILL, 2.5f, 1.0f);

        // Ring of particles expanding outward
        new BukkitRunnable() {
            private double radius = 1.0;
            private int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 30 || radius > 15) {
                    this.cancel();
                    return;
                }

                for (int angle = 0; angle < 360; angle += 10) {
                    double radians = Math.toRadians(angle);
                    double x = Math.cos(radians) * radius;
                    double z = Math.sin(radians) * radius;

                    Location particleLoc = location.clone().add(x, 1.5, z);
                    world.spawnParticle(Particle.PORTAL, particleLoc, 3, 0.1, 0.1, 0.1, 0.1);
                    world.spawnParticle(Particle.END_ROD, particleLoc, 2, 0.2, 0.2, 0.2, 0.1);
                }

                radius += 0.5;
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Create boss spawn effects
     */
    public void createBossSpawnEffects(Location location) {
        World world = location.getWorld();

        // Immediate spawn effects
        world.spawnParticle(Particle.SMOKE, location, 30, 1, 1, 1, 0.1);
        world.spawnParticle(Particle.FLAME, location, 20, 0.5, 0.5, 0.5, 0.05);
        world.spawnParticle(Particle.ENCHANT, location, 50, 1.5, 1, 1.5, 1.0);

        // Sound effects
        world.playSound(location, Sound.ENTITY_WITHER_SPAWN, 1.5f, 1.2f);
        world.playSound(location, Sound.ENTITY_ENDERMAN_SCREAM, 1.0f, 1.5f);

        // Ground cracking effect
        for (int i = 0; i < 8; i++) {
            double angle = (2 * Math.PI * i) / 8;
            double distance = 3.0;

            Location crackLoc = location.clone().add(
                Math.cos(angle) * distance,
                0.1,
                Math.sin(angle) * distance
            );

            world.spawnParticle(Particle.BLOCK, crackLoc, 10,
                crackLoc.getBlock().getBlockData());
            world.playSound(crackLoc, Sound.BLOCK_STONE_BREAK, 0.8f, 1.0f);
        }
    }

    /**
     * Start continuous effects task for boss
     */
    public void startEffectsTask(Enderman boss) {
        this.targetBoss = boss;

        effectsTask = new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                if (targetBoss == null || !targetBoss.isValid()) {
                    this.cancel();
                    return;
                }

                createAmbientEffects(ticks);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 5L); // Every 0.25 seconds

        // Start ambiance task
        startAmbianceTask();
    }

    /**
     * Create ambient boss effects
     */
    private void createAmbientEffects(int ticks) {
        if (targetBoss == null) {
            return;
        }

        Location bossLoc = targetBoss.getLocation();
        World world = bossLoc.getWorld();

        // Subtle purple aura around boss
        if (ticks % 4 == 0) { // Every 1 second
            for (int i = 0; i < 6; i++) {
                double angle = (2 * Math.PI * i) / 6 + ticks * 0.05;
                double radius = 1.2;

                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                double y = 1.8 + Math.sin(ticks * 0.1 + angle) * 0.3;

                Location auraLoc = bossLoc.clone().add(x, y, z);

                world.spawnParticle(Particle.PORTAL, auraLoc, 1, 0.1, 0.1, 0.1, 0);
                world.spawnParticle(Particle.WITCH, auraLoc, 1, 0.05, 0.05, 0.05, 0);
            }
        }

        // Occasional energy burst
        if (Math.random() < 0.05) { // 5% chance
            world.spawnParticle(Particle.END_ROD, bossLoc, 8, 0.8, 1.5, 0.8, 0.2);
            world.spawnParticle(Particle.ENCHANT, bossLoc, 15, 1.2, 2, 1.2, 1.0);
        }

        // Ground particles
        if (ticks % 10 == 0) { // Every 2.5 seconds
            world.spawnParticle(Particle.SMOKE, bossLoc.clone().add(0, 0.1, 0),
                10, 0.8, 0.1, 0.8, 0.02);
            world.spawnParticle(Particle.DUST, bossLoc.clone().add(0, 0.1, 0),
                5, 0.3, 0.05, 0.3, 0,
                new Particle.DustOptions(Color.PURPLE, 1.0f));
        }
    }

    /**
     * Start background ambiance task
     */
    private void startAmbianceTask() {
        ambianceTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (targetBoss == null || !targetBoss.isValid()) {
                    this.cancel();
                    return;
                }

                createAmbianceEffects();
            }
        }.runTaskTimer(plugin, 0L, 100L); // Every 5 seconds
    }

    /**
     * Create ambiance effects
     */
    private void createAmbianceEffects() {
        if (targetBoss == null) {
            return;
        }

        Location bossLoc = targetBoss.getLocation();
        World world = bossLoc.getWorld();

        // Occasional ambient sounds
        double soundChance = Math.random();
        if (soundChance < 0.3) {
            world.playSound(bossLoc, Sound.ENTITY_ENDERMAN_AMBIENT, 0.6f, 0.8f);
        } else if (soundChance < 0.5) {
            world.playSound(bossLoc, Sound.ENTITY_WITHER_AMBIENT, 0.4f, 1.5f);
        } else if (soundChance < 0.6) {
            world.playSound(bossLoc, Sound.BLOCK_RESPAWN_ANCHOR_AMBIENT, 0.3f, 1.2f);
        }

        // Occasional particle bursts
        if (Math.random() < 0.2) { // 20% chance
            world.spawnParticle(Particle.PORTAL, bossLoc, 20, 1, 2, 1, 0.2);
            world.spawnParticle(Particle.DRAGON_BREATH, bossLoc, 15, 0.8, 1.5, 0.8, 0.1);
        }
    }

    /**
     * Create teleportation effects
     */
    public void createTeleportEffects(Location from, Location to,
                                   EndermanTeleportController.TeleportType type) {
        World world = from.getWorld();

        // Departure effects
        switch (type) {
            case CHAOTIC:
                world.spawnParticle(Particle.PORTAL, from, 40, 1, 2, 1, 0.3);
                world.spawnParticle(Particle.DRAGON_BREATH, from, 25, 0.8, 1.5, 0.8, 0.1);
                world.spawnParticle(Particle.END_ROD, from, 30, 0.5, 1, 0.5, 0.2);
                world.playSound(from, Sound.ENTITY_ENDERMAN_TELEPORT, 1.2f, 0.8f);
                break;

            case COUNTER:
                world.spawnParticle(Particle.PORTAL, from, 20, 0.5, 1, 0.5, 0.2);
                world.spawnParticle(Particle.SMOKE, from, 15, 0.3, 0.8, 0.3, 0.05);
                world.playSound(from, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
                break;

            default:
                world.spawnParticle(Particle.PORTAL, from, 25, 0.5, 1, 0.5, 0.2);
                world.spawnParticle(Particle.END_ROD, from, 15, 0.3, 0.8, 0.3, 0.1);
                world.playSound(from, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.0f);
                break;
        }

        // Schedule arrival effects
        new BukkitRunnable() {
            @Override
            public void run() {
                createArrivalEffects(to, type);
            }
        }.runTaskLater(plugin, 2L);
    }

    /**
     * Create teleport arrival effects
     */
    private void createArrivalEffects(Location to, EndermanTeleportController.TeleportType type) {
        World world = to.getWorld();

        switch (type) {
            case CHAOTIC:
                world.spawnParticle(Particle.EXPLOSION, to, 15, 1, 1, 1, 0.1);
                world.spawnParticle(Particle.PORTAL, to, 50, 1.5, 2.5, 1.5, 0.4);
                world.playSound(to, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 2.0f);
                world.playSound(to, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.5f);
                break;

            case COUNTER:
                world.spawnParticle(Particle.CRIT, to, 20, 0.8, 1, 0.8, 0.2);
                world.spawnParticle(Particle.SWEEP_ATTACK, to, 10, 0.5, 0.5, 0.5, 0.1);
                world.playSound(to, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.2f);
                break;

            default:
                world.spawnParticle(Particle.PORTAL, to, 30, 0.8, 1.5, 0.8, 0.3);
                world.spawnParticle(Particle.END_ROD, to, 20, 0.4, 1, 0.4, 0.15);
                world.playSound(to, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.0f);
                break;
        }
    }

    /**
     * Create vampirism healing effects
     */
    public void createVampirismEffects(Location location) {
        World world = location.getWorld();

        // Red healing particles flowing to boss
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double radius = Math.random() * 3;

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = Math.random() * 3;

            Location particleLoc = location.clone().add(x, y, z);

            // Red healing particles
            world.spawnParticle(Particle.DAMAGE_INDICATOR, particleLoc, 1, 0.1, 0.1, 0.1, 0.1);
            world.spawnParticle(Particle.DUST, particleLoc, 1,
                new Particle.DustOptions(Color.RED, 1.5f));
        }

        // Healing sound
        world.playSound(location, Sound.ENTITY_WITCH_DRINK, 0.8f, 1.3f);
        world.playSound(location, Sound.ENTITY_GENERIC_DRINK, 0.6f, 1.5f);
    }

    /**
     * Create shield hit effects
     */
    public void createShieldHitEffects(Location location) {
        World world = location.getWorld();

        // Colorful impact burst
        for (int i = 0; i < 25; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double speed = Math.random() * 1.5;

            double x = Math.cos(angle) * speed;
            double y = Math.random() * 1.5;
            double z = Math.sin(angle) * speed;

            Location particleLoc = location.clone().add(x, y, z);

            // Mixed colors for shield impact
            Color[] colors = {Color.PURPLE, Color.BLUE, Color.WHITE, Color.FUCHSIA};
            Color randomColor = colors[(int) (Math.random() * colors.length)];

            world.spawnParticle(Particle.DUST, particleLoc, 2,
                new Particle.DustOptions(randomColor, 1.0f));
            world.spawnParticle(Particle.CRIT, particleLoc, 3, 0.2, 0.2, 0.2, 0.15);
        }

        // Impact sound
        world.playSound(location, Sound.BLOCK_GLASS_HIT, 1.2f, 1.8f);
        world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.8f, 1.3f);
    }

    /**
     * Create phase transition effects
     */
    public void createPhaseTransitionEffects(Location location, int newPhase) {
        World world = location.getWorld();

        // Phase transition explosion
        world.spawnParticle(Particle.EXPLOSION, location, 30, 1, 1, 1, 0.2);
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, location, 1);

        // Phase-specific effects
        if (newPhase == 2) {
            // Transition to Phase 2 - more aggressive effects
            world.spawnParticle(Particle.DRAGON_BREATH, location, 50, 2, 2, 2, 0.2);
            world.spawnParticle(Particle.SQUID_INK, location, 30, 1.5, 1.5, 1.5, 0.1);
            world.playSound(location, Sound.ENTITY_WITHER_AMBIENT, 2.0f, 1.5f);
            world.playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.8f);
        }

        // Ring effect
        new BukkitRunnable() {
            private double radius = 0.5;

            @Override
            public void run() {
                if (radius > 8) {
                    this.cancel();
                    return;
                }

                for (int angle = 0; angle < 360; angle += 30) {
                    double radians = Math.toRadians(angle);
                    double x = Math.cos(radians) * radius;
                    double z = Math.sin(radians) * radius;

                    Location particleLoc = location.clone().add(x, 1.5, z);
                    world.spawnParticle(Particle.PORTAL, particleLoc, 2, 0.1, 0.1, 0.1, 0.1);
                }

                radius += 0.4;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    /**
     * Create boss defeat effects
     */
    public void createDefeatEffects(Location location) {
        World world = location.getWorld();

        // Massive defeat explosion
        world.spawnParticle(Particle.EXPLOSION, location, 1);
        world.spawnParticle(Particle.DRAGON_BREATH, location, 100, 4, 3, 4, 0.3);
        world.spawnParticle(Particle.FLAME, location, 80, 3, 2, 3, 0.2);
        world.spawnParticle(Particle.SMOKE, location, 60, 2, 2, 2, 0.1);

        // Sound effects
        world.playSound(location, Sound.ENTITY_ENDER_DRAGON_DEATH, 3.0f, 0.5f);
        world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 2.5f, 0.8f);
        world.playSound(location, Sound.ENTITY_WITHER_DEATH, 2.0f, 1.2f);

        // Light pillar effect
        new BukkitRunnable() {
            private int y = 0;

            @Override
            public void run() {
                if (y > 30) {
                    this.cancel();
                    return;
                }

                Location pillarLoc = location.clone().add(0, y, 0);
                world.spawnParticle(Particle.END_ROD, pillarLoc, 10, 1, 0, 1, 0.1);
                world.spawnParticle(Particle.PORTAL, pillarLoc, 8, 0.5, 0, 0.5, 0.05);
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, pillarLoc, 3, 0.3, 0.3, 0.3, 0);

                y += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        // Expanding rings
        for (int ring = 0; ring < 3; ring++) {
            final int ringNumber = ring;
            new BukkitRunnable() {
                private double radius = 1.0;

                @Override
                public void run() {
                    if (radius > 20) {
                        this.cancel();
                        return;
                    }

                    for (int angle = 0; angle < 360; angle += 15) {
                        double radians = Math.toRadians(angle);
                        double x = Math.cos(radians) * radius;
                        double z = Math.sin(radians) * radius;

                        Location particleLoc = location.clone().add(x, 0.5, z);
                        world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0.1, 0.1, 0.1, 0);
                        world.spawnParticle(Particle.PORTAL, particleLoc, 2, 0.1, 0.1, 0.1, 0);
                    }

                    radius += 0.6;
                }
            }.runTaskLater(plugin, ring * 10L);
        }
    }

    /**
     * Create block break effects for anti-build mechanics
     */
    public void createBlockBreakEffects(Location location) {
        World world = location.getWorld();

        // Block destruction particles
        world.spawnParticle(Particle.BLOCK, location, 15,
            location.getBlock().getBlockData());
        world.spawnParticle(Particle.SMOKE, location, 10, 0.3, 0.3, 0.3, 0.05);
        world.spawnParticle(Particle.CRIT, location, 8, 0.2, 0.2, 0.2, 0.1);

        // Sound effect
        world.playSound(location, Sound.BLOCK_STONE_BREAK, 0.8f, 1.2f);
    }

    /**
     * Clean up all visual effects
     */
    public void cleanup() {
        // Cancel effect tasks
        if (effectsTask != null) {
            effectsTask.cancel();
        }
        if (ambianceTask != null) {
            ambianceTask.cancel();
        }

        // Clear references
        targetBoss = null;
    }
}