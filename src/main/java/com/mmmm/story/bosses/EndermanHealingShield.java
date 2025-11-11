package com.mmmm.story.bosses;

import com.mmmm.story.MmmmStoryPlugin;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Manages Enderman boss Phase 2 healing shield mechanics
 * Shield requires 5 player hits to break, with visual effects and player feedback
 */
public class EndermanHealingShield {

    private final MmmmStoryPlugin plugin;

    // Shield state
    private boolean isActive = false;
    private Enderman protectedBoss;
    private int currentHits = 0;
    private final int maxHits = 5;
    private long shieldActivationTime = 0;

    // Visual feedback
    private BossBar shieldHitCounter;
    private BukkitTask effectsTask;
    private BukkitTask timeoutTask;

    // Configuration
    private final long shieldDuration = 7000; // 7 seconds to break shield
    private final long preparationDuration = 2000; // 2 seconds preparation
    private final long stunDuration = 3000; // 3 seconds stun if broken
    private final long healingDuration = 3000; // 3 seconds healing if successful

    // Callbacks
    private Runnable onShieldBroken;
    private Runnable onHealingSuccess;

    public EndermanHealingShield(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
        createShieldHitCounter();
    }

    /**
     * Create shield hit counter boss bar
     */
    private void createShieldHitCounter() {
        shieldHitCounter = BossBar.bossBar(
            Component.text("Щит Исцеления: 5 ударов").color(NamedTextColor.AQUA),
            1.0f,
            BossBar.Color.BLUE,
            BossBar.Overlay.PROGRESS
        );
    }

    /**
     * Activate healing shield for specified boss
     */
    public void activateShield(Enderman boss, Runnable onBroken, Runnable onHealingComplete) {
        if (isActive) {
            return; // Shield already active
        }

        this.protectedBoss = boss;
        this.onShieldBroken = onBroken;
        this.onHealingSuccess = onHealingComplete;
        this.currentHits = 0;
        this.shieldActivationTime = System.currentTimeMillis();

        isActive = true;

        // Add nearby players to shield hit counter
        updateShieldBarPlayers();

        // Start shield effects
        startShieldEffects();

        // Set timeout for shield breaking
        startShieldTimeout();

        // Preparation effects
        createPreparationEffects();
    }

    /**
     * Start shield visual effects
     */
    private void startShieldEffects() {
        effectsTask = new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                if (!isActive || protectedBoss == null || !protectedBoss.isValid()) {
                    this.cancel();
                    return;
                }

                // Update visual effects
                updateShieldEffects(ticks);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L); // Every 0.1 seconds
    }

    /**
     * Update shield visual effects
     */
    private void updateShieldEffects(int ticks) {
        Location bossLoc = protectedBoss.getLocation();
        World world = bossLoc.getWorld();

        // Pulsing shield particles
        double pulseIntensity = 0.5 + 0.5 * Math.sin(ticks * 0.2); // Pulsing effect

        // Purple totem particles forming shield
        for (int i = 0; i < 8; i++) {
            double angle = (2 * Math.PI * i) / 8 + ticks * 0.05;
            double radius = 2.5 + Math.sin(ticks * 0.1) * 0.3; // Breathing effect

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = 1.5 + Math.sin(ticks * 0.15 + angle) * 0.5;

            Location particleLoc = bossLoc.clone().add(x, y, z);

            // Purple shield particles
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 1, 0, 0, 0, 0);
            world.spawnParticle(Particle.ENCHANT, particleLoc, 2, 0.1, 0.1, 0.1, 0);
            world.spawnParticle(Particle.PORTAL, particleLoc, 1, 0.05, 0.05, 0.05, 0);

            // Occasional sparkle
            if (Math.random() < 0.1) {
                world.spawnParticle(Particle.END_ROD, particleLoc, 3, 0.2, 0.2, 0.2, 0.1);
            }
        }

        // Ground circle effect
        for (int angle = 0; angle < 360; angle += 15) {
            double radians = Math.toRadians(angle);
            double radius = 2.0;

            Location groundLoc = bossLoc.clone().add(
                Math.cos(radians) * radius,
                0.1,
                Math.sin(radians) * radius
            );

            world.spawnParticle(Particle.DUST, groundLoc, 1,
                new Particle.DustOptions(Color.PURPLE, 1.0f));
        }
    }

    /**
     * Create shield preparation effects (2 seconds)
     */
    private void createPreparationEffects() {
        Location bossLoc = protectedBoss.getLocation();
        World world = bossLoc.getWorld();

        // 2-second preparation sequence
        new BukkitRunnable() {
            private int progress = 0;

            @Override
            public void run() {
                if (!isActive || protectedBoss == null || !protectedBoss.isValid()) {
                    this.cancel();
                    return;
                }

                progress++;

                // Growing preparation effect
                double size = (double) progress / 20; // 2 seconds = 40 ticks

                // Preparation particles
                world.spawnParticle(Particle.WITCH, bossLoc, 20, size, 1, size, 0.1);
                world.spawnParticle(Particle.ENCHANT, bossLoc, 15, size * 0.8, 1.5, size * 0.8, 1);

                // Preparation sounds
                if (progress % 10 == 0) { // Every 0.5 seconds
                    world.playSound(bossLoc, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.8f, 1.2f);
                }

                // Completion
                if (progress >= 40) {
                    world.playSound(bossLoc, Sound.BLOCK_BEACON_POWER_SELECT, 1.5f, 1.0f);
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, bossLoc, 1);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Start shield timeout (break if not broken in time)
     */
    private void startShieldTimeout() {
        timeoutTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (isActive) {
                    // Shield wasn't broken in time - heal boss
                    executeHealingSuccess();
                }
            }
        }.runTaskLater(plugin, shieldDuration / 50L); // Convert to ticks
    }

    /**
     * Register a hit on the shield
     */
    public void registerHit() {
        if (!isActive) {
            return;
        }

        currentHits++;

        // Update hit counter display
        updateHitCounter();

        // Create hit impact effects
        createHitImpactEffects();

        // Check if shield is broken
        if (currentHits >= maxHits) {
            breakShield();
        }
    }

    /**
     * Update shield hit counter display
     */
    private void updateHitCounter() {
        int remainingHits = Math.max(0, maxHits - currentHits);
        shieldHitCounter.name(Component.text("Щит Исцеления: " + remainingHits + " ударов").color(NamedTextColor.AQUA));

        // Update progress bar
        double progress = (double) (maxHits - currentHits) / maxHits;
        shieldHitCounter.progress((float) progress);

        // Color change based on hits
        if (currentHits >= 4) {
            shieldHitCounter.color(BossBar.Color.RED);
        } else if (currentHits >= 2) {
            shieldHitCounter.color(BossBar.Color.YELLOW);
        }
    }

    /**
     * Create hit impact visual effects
     */
    private void createHitImpactEffects() {
        if (protectedBoss == null) {
            return;
        }

        Location bossLoc = protectedBoss.getLocation();
        World world = bossLoc.getWorld();

        // Shield crack effect
        for (int i = 0; i < 15; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double radius = 2.0 + Math.random() * 0.5;

            double x = Math.cos(angle) * radius;
            double y = 1.5 + Math.random() * 1.0;
            double z = Math.sin(angle) * radius;

            Location particleLoc = bossLoc.clone().add(x, y, z);

            // Impact particles
            world.spawnParticle(Particle.CRIT, particleLoc, 5, 0.2, 0.2, 0.2, 0.1);
            world.spawnParticle(Particle.ENCHANT, particleLoc, 8, 0.3, 0.3, 0.3, 0.2);
            world.spawnParticle(Particle.DUST, particleLoc, 3,
                new Particle.DustOptions(Color.WHITE, 1.5f));
        }

        // Shield impact sound
        world.playSound(bossLoc, Sound.BLOCK_GLASS_BREAK, 1.2f, 1.5f);
        world.playSound(bossLoc, Sound.ENTITY_PLAYER_ATTACK_STRONG, 0.8f, 1.2f);

        // Screen shake effect for nearby players
        for (Entity entity : bossLoc.getNearbyEntities(20, 20, 20)) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                // Subtle screen shake (would need plugin extension for actual shake)
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.3f, 2.0f);
            }
        }
    }

    /**
     * Break the shield
     */
    private void breakShield() {
        if (!isActive) {
            return;
        }

        // Create shield break effects
        createShieldBreakEffects();

        // Shield broken - stun boss
        deactivateShield();

        // Trigger callback
        if (onShieldBroken != null) {
            onShieldBroken.run();
        }
    }

    /**
     * Create shield break visual effects
     */
    private void createShieldBreakEffects() {
        if (protectedBoss == null) {
            return;
        }

        Location bossLoc = protectedBoss.getLocation();
        World world = bossLoc.getWorld();

        // Violent shield shatter
        for (int i = 0; i < 50; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double speed = 0.5 + Math.random() * 1.5;

            double x = Math.cos(angle) * speed;
            double y = Math.random() * 1.5;
            double z = Math.sin(angle) * speed;

            Location particleLoc = bossLoc.clone().add(x, y + 1.5, z);

            // Shatter particles
            world.spawnParticle(Particle.CRIT, particleLoc, 10, 0.2, 0.2, 0.2, 0.3);
            world.spawnParticle(Particle.END_ROD, particleLoc, 8, 0.1, 0.1, 0.1, 0.2);
            world.spawnParticle(Particle.PORTAL, particleLoc, 6, 0.3, 0.3, 0.3, 0.1);
        }

        // Sound effects
        world.playSound(bossLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.2f);
        world.playSound(bossLoc, Sound.BLOCK_GLASS_BREAK, 2.0f, 0.8f);
        world.playSound(bossLoc, Sound.ENTITY_WITCH_CELEBRATE, 1.0f, 1.5f);
    }

    /**
     * Execute healing success (shield wasn't broken)
     */
    private void executeHealingSuccess() {
        if (!isActive || protectedBoss == null) {
            return;
        }

        // Create healing success effects
        createHealingSuccessEffects();

        // Heal boss
        double healAmount = protectedBoss.getMaxHealth() * 0.3; // 30% health
        double currentHealth = protectedBoss.getHealth();
        double newHealth = Math.min(currentHealth + healAmount, protectedBoss.getMaxHealth());
        protectedBoss.setHealth(newHealth);

        // Deactivate shield
        deactivateShield();

        // Trigger callback
        if (onHealingSuccess != null) {
            onHealingSuccess.run();
        }
    }

    /**
     * Create healing success visual effects
     */
    private void createHealingSuccessEffects() {
        if (protectedBoss == null) {
            return;
        }

        Location bossLoc = protectedBoss.getLocation();
        World world = bossLoc.getWorld();

        // Healing aura
        new BukkitRunnable() {
            private int ticks = 0;
            private final int maxTicks = 60; // 3 seconds

            @Override
            public void run() {
                if (ticks >= maxTicks || protectedBoss == null || !protectedBoss.isValid()) {
                    this.cancel();
                    return;
                }

                // Expanding healing particles
                double radius = (double) ticks / maxTicks * 5.0;

                for (int i = 0; i < 12; i++) {
                    double angle = (2 * Math.PI * i) / 12;

                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    double y = 1.0 + Math.sin(ticks * 0.2) * 0.5;

                    Location particleLoc = bossLoc.clone().add(x, y, z);

                    // Healing particles
                    world.spawnParticle(Particle.HEART, particleLoc, 2, 0.1, 0.1, 0.1, 0.1);
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.HAPPY_VILLAGER, particleLoc, 3, 0.2, 0.2, 0.2, 0.05);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // Healing sounds
        world.playSound(bossLoc, Sound.ENTITY_WITCH_DRINK, 1.5f, 1.2f);
        world.playSound(bossLoc, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 1.0f);

        // Visual feedback
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, bossLoc, 1);
        protectedBoss.setGlowing(true);

        // Remove glow after healing
        new BukkitRunnable() {
            @Override
            public void run() {
                if (protectedBoss != null && protectedBoss.isValid()) {
                    protectedBoss.setGlowing(false);
                }
            }
        }.runTaskLater(plugin, healingDuration / 50L);
    }

    /**
     * Update shield bar players
     */
    private void updateShieldBarPlayers() {
        if (protectedBoss == null) {
            return;
        }

        World world = protectedBoss.getWorld();
        Location bossLoc = protectedBoss.getLocation();

        // Add nearby players
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distance(bossLoc) < 100) {
                // BossBar will be shown to all nearby players automatically
            }
        }
    }

    /**
     * Check if shield is currently active
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Get current hit count
     */
    public int getCurrentHits() {
        return currentHits;
    }

    /**
     * Get remaining hits needed to break shield
     */
    public int getRemainingHits() {
        return Math.max(0, maxHits - currentHits);
    }

    /**
     * Update shield state (called from main task)
     */
    public void update() {
        if (!isActive) {
            return;
        }

        // Validate protected boss
        if (protectedBoss == null || !protectedBoss.isValid()) {
            deactivateShield();
            return;
        }

        // Update shield bar players
        updateShieldBarPlayers();

        // Check for timeout
        long timeSinceActivation = System.currentTimeMillis() - shieldActivationTime;
        if (timeSinceActivation >= shieldDuration) {
            executeHealingSuccess();
        }
    }

    /**
     * Deactivate shield and cleanup
     */
    private void deactivateShield() {
        isActive = false;

        // Cancel tasks
        if (effectsTask != null) {
            effectsTask.cancel();
        }
        if (timeoutTask != null) {
            timeoutTask.cancel();
        }

        // Remove all players from shield hit counter
        // BossBar cleanup handled by system

        // Reset state
        currentHits = 0;
        protectedBoss = null;
        onShieldBroken = null;
        onHealingSuccess = null;

        // Reset shield counter appearance
        shieldHitCounter.name(Component.text("Щит Исцеления: 5 ударов").color(NamedTextColor.AQUA));
        shieldHitCounter.progress(1.0f);
        shieldHitCounter.color(BossBar.Color.BLUE);
    }

    /**
     * Force deactivate shield (emergency cleanup)
     */
    public void forceDeactivate() {
        if (isActive) {
            // Create emergency break effects
            if (protectedBoss != null) {
                createShieldBreakEffects();
            }
        }

        deactivateShield();
    }

    /**
     * Clean up all resources
     */
    public void cleanup() {
        forceDeactivate();
    }
}