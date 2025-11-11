package com.mmmm.story.bosses;

import com.mmmm.story.MmmmStoryPlugin;
import com.mmmm.story.managers.ItemManager;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Manages the Enderman Boss (replacement for Boss #2 - Изверг Адских Глубин)
 * Features: teleportation, clone mechanics, two-phase combat, healing shield
 */
public class EndermanBossManager {

    private final MmmmStoryPlugin plugin;

    // Core boss entity
    private Enderman bossEntity;
    private String bossName = "Изверг Адских Глубин";

    // Combat state
    private int currentPhase = 1;
    private BossAttackState attackState;

    // Combat mechanics components
    private EndermanCloneSystem cloneSystem;
    private EndermanTeleportController teleportController;
    private EndermanHealingShield healingShield;
    private EndermanVFXManager vfxManager;

    // Combat tracking
    private final Map<UUID, Long> playersInAggroRange = new HashMap<>();
    private final Map<UUID, Integer> playerAttackCount = new HashMap<>();
    private final Set<UUID> nearbyPlayers = new HashSet<>();

    // Configuration
    private final double maxHealth = 500.0;
    private final double aggroRadius = 75.0;
    private final double teleportRadius = 10.0; // Уменьшен радиус телепортации для более ближних атак
    private final int counterAttackInterval = 4; // Every 4th attack
    private final int chaoticTeleportIntervalSeconds = 25;
    private final int cloneWaveIntervalSeconds = 15; // Phase 2
    private final int healingIntervalSeconds = 35; // Phase 2

    // Task management
    private BukkitTask combatTask;
    private BukkitTask teleportTask;
    private BukkitTask healingTask;
    private BukkitTask aggroTask;
    private BukkitTask shieldRepeatTask;

    // Shield cooldown system
    private long lastShieldTime = 0;
    private final long SHIELD_COOLDOWN_MS = 30000; // 30 seconds

    public EndermanBossManager(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
        initializeComponents();
    }

    /**
     * Initialize boss component systems
     */
    private void initializeComponents() {
        this.cloneSystem = new EndermanCloneSystem(plugin);
        this.teleportController = new EndermanTeleportController(plugin);
        this.healingShield = new EndermanHealingShield(plugin);
        this.vfxManager = new EndermanVFXManager(plugin);
    }

    /**
     * Spawn the Enderman boss at the specified location
     * Includes epic vertical rift entrance sequence
     */
    public void spawnBoss(Location location) {
        World world = location.getWorld();

        // Start epic entrance sequence
        vfxManager.createPortalRiftEntrance(location, () -> {
            // Spawn boss after entrance completes
            spawnBossEntity(location);
            initializeCombat();
        });
    }

    /**
     * Create the actual boss entity
     */
    private void spawnBossEntity(Location location) {
        World world = location.getWorld();
        Location spawnLoc = location.clone().add(0, 3, 0);

        // Spawn Enderman
        bossEntity = (Enderman) world.spawnEntity(spawnLoc, EntityType.ENDERMAN);
        bossEntity.setCustomName("§5" + bossName); // Фиолетовый цвет
        bossEntity.setCustomNameVisible(true);

        // Set attributes and effects
        configureBossAttributes();

        // Mark as boss
        bossEntity.setPersistent(true);
        bossEntity.setRemoveWhenFarAway(false);

        // Initialize attack state
        attackState = new BossAttackState(bossEntity.getUniqueId(), 1);

        // Boss bar управляется через Act2Listener

        // Visual effects for spawn
        vfxManager.createBossSpawnEffects(bossEntity.getLocation());

        // Notify nearby players
        notifyNearbyPlayers();
    }

    /**
     * Configure boss attributes and effects
     */
    private void configureBossAttributes() {
        // Health
        bossEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        bossEntity.setHealth(maxHealth);

        // Movement and combat
        bossEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.35);
        bossEntity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(7.5); // Уменьшен урон вдвое с 15.0
        bossEntity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0.8);

        // Boss is larger than regular Endermen (scaling API not available)

        // Permanent effects
        bossEntity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1));
        bossEntity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0));
    }

    
    /**
     * Notify nearby players of boss spawn
     */
    private void notifyNearbyPlayers() {
        World world = bossEntity.getWorld();
        Location bossLoc = bossEntity.getLocation();

        for (Player player : world.getPlayers()) {
            if (player.getLocation().distance(bossLoc) < 150) {
                // Send notification
                player.sendMessage(Component.text("§5§l⚔ " + bossName + " восстает из разрыва портала!").color(NamedTextColor.DARK_PURPLE));
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1.0f, 0.6f);
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.5f, 1.0f);
            }
        }
    }

    /**
     * Initialize combat systems and start tasks
     */
    private void initializeCombat() {
        // Start main combat task
        startCombatTask();

        // Start teleportation task
        startTeleportTask();

        // Start healing task (Phase 2 only)
        startHealingTask();

        // Start shield repeat task (Phase 2 only)
        startShieldRepeatTask();

        // Start aggro tracking task
        startAggroTask();

        // Start visual effects task
        vfxManager.startEffectsTask(bossEntity);
    }

    /**
     * Main combat AI task
     */
    private void startCombatTask() {
        combatTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isBossActive()) {
                    this.cancel();
                    return;
                }

                executeCombatLogic();
            }
        }.runTaskTimer(plugin, 0L, 10L); // Every 0.5 seconds
    }

    /**
     * Execute main combat logic
     */
    private void executeCombatLogic() {
        // Check phase transition
        checkPhaseTransition();

        // Get nearby players
        updateNearbyPlayers();

        if (nearbyPlayers.isEmpty()) {
            // No players nearby, teleport to nearest player
            teleportToNearestPlayer();
            bossEntity.setAI(false);
            return;
        }

        // Check if shield is active - freeze boss during shield phase
        if (healingShield != null && healingShield.isActive()) {
            bossEntity.setAI(false); // Freeze boss during shield
            return;
        }

        bossEntity.setAI(true);

        // Target nearest player
        Player target = findNearestPlayer();
        if (target != null) {
            bossEntity.setTarget(target);

            // Execute phase-specific behavior
            if (currentPhase == 1) {
                executePhase1Combat(target);
            } else {
                executePhase2Combat(target);
            }
        }
    }

    /**
     * Execute Phase 1 combat behavior
     */
    private void executePhase1Combat(Player target) {
        // Random teleportation and clone creation
        if (attackState.canAttack(chaoticTeleportIntervalSeconds)) {
            executeChaoticTeleportation();
        }

        // Basic attacks
        if (bossEntity.getLocation().distance(target.getLocation()) < 3.0) {
            // Melee range, allow natural attacks
            handlePlayerAttack(target);
        }
    }

    /**
     * Execute Phase 2 combat behavior
     */
    private void executePhase2Combat(Player target) {
        // Clone waves
        if (attackState.canAttack(cloneWaveIntervalSeconds)) {
            executeCloneWaveSpawn();
        }

        // Healing shield mechanics
        if (!healingShield.isActive() && attackState.canAttack(healingIntervalSeconds)) {
            executeHealingAttempt();
        }

        // Enhanced aggression (only when not frozen by shield)
        if (Math.random() < 0.1 && !(healingShield != null && healingShield.isActive())) { // 10% chance per tick
            teleportController.executeCounterTeleport(bossEntity, target);
        }
    }

    /**
     * Execute chaotic teleportation sequence with clones
     */
    private void executeChaoticTeleportation() {
        if (!attackState.canCreateClones()) {
            return;
        }

        attackState.startSpecialAttack(BossAttackState.SpecialAttackPhase.ENDERMAN_CHAOTIC_TELEPORT);

        // Perform 10-15 teleports with clone creation
        int teleportCount = 10 + (int)(Math.random() * 6); // 10-15 teleports

        new BukkitRunnable() {
            private int remainingTeleports = teleportCount;

            @Override
            public void run() {
                if (remainingTeleports <= 0 || !isBossActive()) {
                    attackState.endSpecialAttack(false);
                    this.cancel();
                    return;
                }

                // Teleport to random location (only if not frozen by shield)
                if (!(healingShield != null && healingShield.isActive())) {
                    Location randomLoc = teleportController.findRandomTeleportLocation(bossEntity.getLocation(), teleportRadius);
                    if (randomLoc != null) {
                        vfxManager.createTeleportEffects(bossEntity.getLocation(), randomLoc, EndermanTeleportController.TeleportType.NORMAL);
                        bossEntity.teleport(randomLoc);

                    // Create clone occasionally
                    if (Math.random() < 0.4) { // 40% chance to create clone
                        cloneSystem.createClone(bossEntity, randomLoc);
                    }
                }
                }

                remainingTeleports--;
            }
        }.runTaskTimer(plugin, 0L, 3L); // Rapid teleports
    }

    /**
     * Execute clone wave spawn (Phase 2)
     */
    private void executeCloneWaveSpawn() {
        if (!attackState.canCreateClones() || currentPhase != 2) {
            return;
        }

        attackState.startSpecialAttack(BossAttackState.SpecialAttackPhase.ENDERMAN_CLONE_WAVE_SPAWN);

        int cloneCount = 10 + (int)(Math.random() * 11); // 10-20 clones
        Location bossLoc = bossEntity.getLocation();

        // Spawn clones in circular pattern
        new BukkitRunnable() {
            private int spawnedClones = 0;
            private final int clonesPerTick = 3; // Spawn 3 clones per tick for 3-4 seconds

            @Override
            public void run() {
                if (spawnedClones >= cloneCount || !isBossActive()) {
                    attackState.endSpecialAttack(false);
                    this.cancel();
                    return;
                }

                for (int i = 0; i < clonesPerTick && spawnedClones < cloneCount; i++) {
                    double angle = (2 * Math.PI * spawnedClones) / cloneCount;
                    double radius = 8.0;

                    Location cloneLoc = bossLoc.clone().add(
                        Math.cos(angle) * radius,
                        0,
                        Math.sin(angle) * radius
                    );

                    cloneSystem.createClone(bossEntity, cloneLoc);
                    spawnedClones++;
                }
            }
        }.runTaskTimer(plugin, 0L, 5L); // Every 0.25 seconds
    }

    /**
     * Execute healing attempt (Phase 2)
     */
    private void executeHealingAttempt() {
        if (currentPhase != 2) {
            return;
        }

        attackState.startSpecialAttack(BossAttackState.SpecialAttackPhase.ENDERMAN_HEALING_PREPARATION);

        // 2-second preparation
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isBossActive()) {
                    this.cancel();
                    return;
                }

                // Start healing shield
                healingShield.activateShield(bossEntity, () -> {
                    // Shield broken callback
                    attackState.setSpecialAttackPhase(BossAttackState.SpecialAttackPhase.ENDERMAN_STUNNED);

                    // 3-second stun
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (isBossActive()) {
                                attackState.endSpecialAttack(false);
                            }
                        }
                    }.runTaskLater(plugin, 60L); // 3 seconds
                }, () -> {
                    // Healing success callback
                    attackState.setSpecialAttackPhase(BossAttackState.SpecialAttackPhase.ENDERMAN_HEALING_SUCCESS);

                    // Heal for 3 seconds
                    double healAmount = maxHealth * 0.3; // 30% of max health
                    new BukkitRunnable() {
                        private int healingTicks = 0;

                        @Override
                        public void run() {
                            if (!isBossActive() || healingTicks >= 60) { // 3 seconds
                                attackState.endSpecialAttack(false);
                                this.cancel();
                                return;
                            }

                            double currentHealth = bossEntity.getHealth();
                            double newHealth = Math.min(currentHealth + (healAmount / 60.0), maxHealth);
                            bossEntity.setHealth(newHealth);

                            healingTicks++;
                        }
                    }.runTaskTimer(plugin, 0L, 1L);
                });
            }
        }.runTaskLater(plugin, 40L); // 2 seconds
    }

    /**
     * Handle player attack on boss
     */
    public void handlePlayerAttack(Player attacker) {
        UUID playerId = attacker.getUniqueId();

        // Count attacks for counter-attack mechanic
        int currentAttacks = playerAttackCount.getOrDefault(playerId, 0) + 1;
        playerAttackCount.put(playerId, currentAttacks);

        // Every 4th attack triggers counter-teleport
        if (currentAttacks % counterAttackInterval == 0 && attackState.canTeleport()) {
            teleportController.executeCounterTeleport(bossEntity, attacker);
        }

        // Vampirism healing (Phase 1)
        if (currentPhase == 1) {
            double damageDealt = 15.0; // Approximate damage
            double healAmount = damageDealt * 0.05; // 5% vampirism
            double currentHealth = bossEntity.getHealth();
            double newHealth = Math.min(currentHealth + healAmount, maxHealth);
            bossEntity.setHealth(newHealth);

            vfxManager.createVampirismEffects(bossEntity.getLocation());
        }

        // Check healing shield hits (Phase 2)
        if (healingShield.isActive()) {
            healingShield.registerHit();
            vfxManager.createShieldHitEffects(attacker.getLocation());
        }
    }

    /**
     * Start teleportation task
     */
    private void startTeleportTask() {
        teleportTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isBossActive()) {
                    this.cancel();
                    return;
                }

                teleportController.processTeleportQueue();
            }
        }.runTaskTimer(plugin, 0L, 1L); // Every tick
    }

    /**
     * Start healing task
     */
    private void startHealingTask() {
        healingTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isBossActive()) {
                    this.cancel();
                    return;
                }

                healingShield.update();
            }
        }.runTaskTimer(plugin, 0L, 5L); // Every 0.25 seconds
    }

    /**
     * Start aggro tracking task
     */
    private void startAggroTask() {
        aggroTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isBossActive()) {
                    this.cancel();
                    return;
                }

                updateAggroTracking();
            }
        }.runTaskTimer(plugin, 0L, 20L); // Every second
    }

    /**
     * Update aggro tracking for nearby players
     */
    private void updateAggroTracking() {
        World world = bossEntity.getWorld();
        Location bossLoc = bossEntity.getLocation();

        // Clear old tracking
        playersInAggroRange.clear();

        // Find players in aggro range
        for (Entity entity : bossEntity.getNearbyEntities(aggroRadius, aggroRadius, aggroRadius)) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                playersInAggroRange.put(player.getUniqueId(), System.currentTimeMillis());
            }
        }
    }

    /**
     * Start shield repeat task for Phase 2
     */
    private void startShieldRepeatTask() {
        shieldRepeatTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isBossActive()) {
                    this.cancel();
                    return;
                }

                // Only activate shields in Phase 2 with cooldown
                if (currentPhase == 2 && !healingShield.isActive()) {
                    long currentTime = System.currentTimeMillis();

                    // Check if cooldown has passed
                    if (currentTime - lastShieldTime >= SHIELD_COOLDOWN_MS) {
                        // Random chance to activate shield (lower chance due to cooldown)
                        if (Math.random() < 0.05) { // 5% chance per tick (0.5s)
                            activateShieldWithRepeat();
                            lastShieldTime = currentTime; // Update last shield time
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L); // Every 0.5 seconds
    }

    /**
     * Update nearby players list
     */
    private void updateNearbyPlayers() {
        nearbyPlayers.clear();
        nearbyPlayers.addAll(playersInAggroRange.keySet());
    }

    /**
     * Activate shield that can repeat multiple times
     */
    private void activateShieldWithRepeat() {
        healingShield.activateShield(bossEntity, () -> {
            // Shield broken callback
            attackState.setSpecialAttackPhase(BossAttackState.SpecialAttackPhase.ENDERMAN_STUNNED);

            // 2-second stun
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (isBossActive()) {
                        attackState.endSpecialAttack(false);
                    }
                }
            }.runTaskLater(plugin, 40L); // 2 seconds
        }, () -> {
            // Healing success callback
            attackState.setSpecialAttackPhase(BossAttackState.SpecialAttackPhase.ENDERMAN_HEALING_SUCCESS);

            // Heal for 2 seconds
            double healAmount = maxHealth * 0.15; // 15% of max health
            double currentHealth = bossEntity.getHealth();
            double newHealth = Math.min(currentHealth + healAmount, maxHealth);
            bossEntity.setHealth(newHealth);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (isBossActive()) {
                        attackState.endSpecialAttack(false);
                    }
                }
            }.runTaskLater(plugin, 40L); // 2 seconds
        });
    }

    /**
     * Find nearest player
     */
    private Player findNearestPlayer() {
        Player nearest = null;
        double minDistance = Double.MAX_VALUE;
        Location bossLoc = bossEntity.getLocation();

        for (UUID playerId : nearbyPlayers) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                double distance = bossLoc.distance(player.getLocation());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = player;
                }
            }
        }

        return nearest;
    }

    /**
     * Teleport boss to nearest player (when no players nearby)
     */
    private void teleportToNearestPlayer() {
        World world = bossEntity.getWorld();
        Location bossLoc = bossEntity.getLocation();

        // Find any online player in the world
        Player targetPlayer = null;
        double minDistance = Double.MAX_VALUE;

        for (Player player : world.getPlayers()) {
            if (player.isOnline() && !player.isDead()) {
                double distance = bossLoc.distance(player.getLocation());
                if (distance < minDistance) {
                    minDistance = distance;
                    targetPlayer = player;
                }
            }
        }

        if (targetPlayer != null) {
            // Teleport to location near player (3-5 blocks away)
            Location targetLoc = targetPlayer.getLocation();
            double offsetX = (Math.random() - 0.5) * 8; // -4 to 4
            double offsetZ = (Math.random() - 0.5) * 8; // -4 to 4
            Location teleportLoc = targetLoc.clone().add(offsetX, 0, offsetZ);

            // Ensure safe landing location
            // Find safe Y location manually
            for (int y = teleportLoc.getBlockY(); y > Math.max(teleportLoc.getBlockY() - 30, world.getMinHeight()); y--) {
                Location checkLoc = teleportLoc.clone();
                checkLoc.setY(y);
                if (!checkLoc.getBlock().getType().isAir() &&
                    checkLoc.clone().add(0, 1, 0).getBlock().getType().isAir()) {
                    teleportLoc = checkLoc.clone().add(0, 1, 0);
                    break;
                }
            }

            vfxManager.createTeleportEffects(bossEntity.getLocation(), teleportLoc, EndermanTeleportController.TeleportType.NORMAL);
            bossEntity.teleport(teleportLoc);
        }
    }

    
    /**
     * Check for phase transition
     */
    private void checkPhaseTransition() {
        double healthPercentage = bossEntity.getHealth() / bossEntity.getMaxHealth();
        int newPhase = healthPercentage > 0.5 ? 1 : 2;

        if (newPhase != currentPhase) {
            currentPhase = newPhase;
            attackState = new BossAttackState(bossEntity.getUniqueId(), currentPhase);

            // Phase transition effects
            vfxManager.createPhaseTransitionEffects(bossEntity.getLocation(), currentPhase);

            // Notify players
            for (UUID playerId : nearbyPlayers) {
                Player player = Bukkit.getPlayer(playerId);
                if (player == null) continue;
                player.sendMessage(Component.text("§5§l" + bossName + " вступает в фазу " + currentPhase + "!").color(NamedTextColor.DARK_PURPLE));
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.8f);
            }
        }
    }

    /**
     * Handle entity damage
     */
    public void handleEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() != bossEntity) {
            return;
        }

        // Water immunity
        if (event.getCause() == EntityDamageEvent.DamageCause.DROWNING ||
            event.getCause() == EntityDamageEvent.DamageCause.DRYOUT) {
            event.setCancelled(true);
            return;
        }

        // Apply resistance effects during certain phases
        if (healingShield.isActive()) {
            event.setDamage(event.getDamage() * 0.5); // 50% damage reduction
        }
    }

    /**
     * Handle clone death - prevent drops and experience
     */
    public void handleEntityDeath(EntityDeathEvent event) {
        if (cloneSystem.isClone(event.getEntity())) {
            // Clear all drops
            event.getDrops().clear();
            event.setDroppedExp(0);

            // Remove the specific clone from the system
            EndermanCloneSystem.EndermanClone clone = cloneSystem.getClone(event.getEntity());
            if (clone != null) {
                clone.getEntity().remove();
            }
        }
    }

    /**
     * Handle water contact - freeze nearby water
     */
    public void handleWaterContact() {
        World world = bossEntity.getWorld();
        Location bossLoc = bossEntity.getLocation();

        // Freeze water blocks around nearby players
        for (UUID playerId : nearbyPlayers) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                Location playerLoc = player.getLocation();

                // Freeze water in 10-block radius around player
                for (int x = -10; x <= 10; x++) {
                    for (int z = -10; z <= 10; z++) {
                        for (int y = -2; y <= 2; y++) {
                            Location checkLoc = playerLoc.clone().add(x, y, z);
                            if (checkLoc.getBlock().getType() == Material.WATER) {
                                checkLoc.getBlock().setType(Material.ICE);
                            }
                        }
                    }
                }
            }
        }

        // Clear rain if raining
        if (world.hasStorm()) {
            world.setStorm(false);
        }
    }

    /**
     * Handle anti-build mechanics
     */
    public void handleAntiBuild() {
        if (bossEntity == null || !bossEntity.isValid()) {
            return;
        }

        Location bossLoc = bossEntity.getLocation();

        // Check for players above boss
        for (UUID playerId : nearbyPlayers) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                Location playerLoc = player.getLocation();

                // Player above boss within 5 blocks vertically
                if (playerLoc.getY() > bossLoc.getY() && playerLoc.getY() - bossLoc.getY() <= 5) {
                    double horizontalDistance = Math.sqrt(
                        Math.pow(playerLoc.getX() - bossLoc.getX(), 2) +
                        Math.pow(playerLoc.getZ() - bossLoc.getZ(), 2)
                    );

                    if (horizontalDistance <= 5) {
                        // Knock player back
                        Vector knockback = playerLoc.toVector()
                            .subtract(bossLoc.toVector())
                            .normalize()
                            .multiply(4.0)
                            .setY(2.0);
                        player.setVelocity(knockback);
                        player.playSound(playerLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
                    }
                }
            }
        }

        // Break blocks in 3-block radius
        for (int x = -3; x <= 3; x++) {
            for (int y = -1; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    Location checkLoc = bossLoc.clone().add(x, y, z);
                    if (shouldBreakBlock(checkLoc.getBlock().getType())) {
                        checkLoc.getBlock().setType(Material.AIR);
                        vfxManager.createBlockBreakEffects(checkLoc);
                    }
                }
            }
        }
    }

    /**
     * Check if block should be broken
     */
    private boolean shouldBreakBlock(Material type) {
        return type == Material.OAK_PLANKS || type == Material.COBBLESTONE ||
               type == Material.STONE_BRICKS || type == Material.IRON_BARS ||
               type == Material.NETHER_BRICKS || type == Material.OBSIDIAN;
    }

    /**
     * Check if boss is active and valid
     */
    public boolean isBossActive() {
        return bossEntity != null && bossEntity.isValid();
    }

    /**
     * Get current boss phase
     */
    public int getCurrentPhase() {
        return currentPhase;
    }

    /**
     * Get boss entity
     */
    public Enderman getBossEntity() {
        return bossEntity;
    }

    /**
     * Check if boss has active shield
     */
    public boolean hasActiveShield() {
        return healingShield != null && healingShield.isActive();
    }

    /**
     * Handle boss defeat
     */
    public void handleBossDefeat() {
        if (!isBossActive()) {
            return;
        }

        // Create defeat effects
        vfxManager.createDefeatEffects(bossEntity.getLocation());

        // Notify players
        for (UUID playerId : nearbyPlayers) {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null) continue;
            player.sendMessage(Component.text("§6§l⚔ " + bossName + " повержен!").color(NamedTextColor.GOLD));
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 2.0f, 1.0f);
        }

        // Trigger story progression
        plugin.getDataManager().setBoss2Defeated(true);
        plugin.getDataManager().setEndEnabled(true);
        plugin.getActManager().progressToAct(3);

        // Drop items at boss location
        dropBossRewards();

        // Play dialog for all players
        plugin.getDialogManager().playDialogForAll("boss2.defeated");

        // Create End portal in Overworld
        createOverworldPortal();

        // Cleanup
        cleanup();
    }

    /**
     * Drop boss rewards
     */
    private void dropBossRewards() {
        if (bossEntity == null) {
            return;
        }

        World world = bossEntity.getWorld();
        Location dropLoc = bossEntity.getLocation();

        // Drop Overworld Portal Key
        try {
            ItemStack portalKey = plugin.getItemManager().createStoryItem(ItemManager.OVERWORLD_PORTAL_KEY);
            world.dropItemNaturally(dropLoc, portalKey);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create Overworld Portal Key: " + e.getMessage());
        }

        // Drop Nether Stars
        world.dropItemNaturally(dropLoc, new ItemStack(Material.NETHER_STAR, 2));
    }

    /**
     * Create End portal in Overworld
     */
    private void createOverworldPortal() {
        World overworld = plugin.getServer().getWorlds().get(0);
        Location spawn = overworld.getSpawnLocation();
        int distance = plugin.getConfigManager().getConfig().getInt("structures.overworldPortalDistanceFromSpawn", 500);

        Location portalLoc = plugin.getStructureManager().findSafeLocation(overworld, spawn, distance);
        if (portalLoc != null) {
            plugin.getStructureManager().placeStructure("overworld_portal", portalLoc);
            plugin.getDataManager().saveLocation("structures.overworld_portal", portalLoc);

            // Announce portal creation
            String portalMsg = plugin.getMessageManager().getMessage("ru", "boss2.portal_created");
            if (portalMsg == null || portalMsg.equals("boss2.portal_created")) {
                portalMsg = "§5§l⚡ Портал в Край создан в Верхнем Мире!";
            }
            plugin.getServer().broadcast(Component.text(portalMsg).color(NamedTextColor.LIGHT_PURPLE));

            plugin.getLogger().info(plugin.getMessageManager().getMessage("log.overworld_portal_placed").replace("%location%", portalLoc.getBlockX() + ", " + portalLoc.getBlockY() + ", " + portalLoc.getBlockZ()));
        }
    }

    /**
     * Clean up all boss resources
     */
    public void cleanup() {
        // Cancel tasks
        if (combatTask != null) combatTask.cancel();
        if (teleportTask != null) teleportTask.cancel();
        if (healingTask != null) healingTask.cancel();
        if (aggroTask != null) aggroTask.cancel();
        if (shieldRepeatTask != null) shieldRepeatTask.cancel();

        // Cleanup components
        if (cloneSystem != null) cloneSystem.cleanup();
        if (teleportController != null) teleportController.cleanup();
        if (healingShield != null) healingShield.cleanup();
        if (vfxManager != null) vfxManager.cleanup();

        // Remove boss entity
        if (bossEntity != null && bossEntity.isValid()) {
            bossEntity.remove();
        }

        // Clear collections
        playersInAggroRange.clear();
        playerAttackCount.clear();
        nearbyPlayers.clear();

        // Remove reference
        bossEntity = null;
    }
}