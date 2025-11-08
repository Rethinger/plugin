package com.mmmm.story.bosses;

import com.mmmm.story.managers.SafeZoneManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Main coordinator for the enhanced boss special attack with hemisphere formation.
 * Manages warrior summon wave checking, hemisphere formation, safe zones, and boss flight states.
 * Handles both skull phase (phase 1) and arrow phase (phase 2) of the special attack.
 */
public class BossSpecialAttackManager {

    private final Plugin plugin;
    private final Logger logger;
    private final Skeleton boss;
    private final BossAttackState attackState;
    private final SpecialAttackConfiguration config;

    // Formation components
    private HemisphereFormation hemisphereFormation;
    private ArrowHemisphereFormation arrowHemisphereFormation;
    private SafeZoneManager safeZoneManager;

    // State management
    private boolean isActive;
    private Location originalBossLocation;
    private Location airborneTargetLocation;
    private SpecialAttackPhase currentPhase;
    private int phaseTimer;
    private BukkitRunnable attackTask;
    private BukkitTask warriorSummonTimerTask;
    
    // Phase-specific timeout tracking
    private long phaseStartTime;
    private int currentPhaseTimeout;

    // Warrior summon timer management
    private boolean isWarriorSummonTimerPaused;
    private long warriorSummonTimerPauseStartTime;
    
    // Target capture for hemisphere attack (captured when safe zones appear)
    private List<Player> capturedTargets;

    // Configuration constants
    private static final int MINIMUM_WARRIOR_WAVES = 2;
    private static final int WAIT_AFTER_SECOND_WAVE_SECONDS = 12;
    // Note: Phase duration constants now read from SpecialAttackConfiguration
    // - Rising duration: config.getRisingDuration()
    // - Hover duration: config.getHoverDurationTicks()
    // - Formation duration: config.getHemisphereFormationDurationTicks()
    // - Safe zones window: config.getSafeZonesWindowTicks()
    // - Final prep: config.getFinalPrepDurationTicks()
    // - Phase timeout multiplier: config.getPhaseTimeoutMultiplier()
    private static final int POST_ATTACK_WAIT_DURATION_TICKS = 20; // 1 second
    private static final int GROUND_TOUCHDOWN_DURATION_TICKS = 40; // 2 seconds

    /**
     * Enhanced special attack phases for hemisphere attack
     */
    public enum SpecialAttackPhase {
        NONE,                           // No special attack active
        RISING_ANIMATION,              // Boss rising into the air
        HEMISPHERE_FORMATION,          // Hemisphere formation (3 seconds)
        SAFE_ZONES_APPEARING,          // Safe zones appearing sequentially
        FINAL_PREPARATION,             // Final preparation before attack (1 second)
        HEMISPHERE_ATTACK,             // Hemisphere attack launching
        POST_ATTACK_WAIT,              // Post-attack hover (1 second)
        GROUND_TOUCHDOWN,              // Boss descending to ground (2 seconds)
        COOLDOWN                       // Attack cooldown phase
    }

    /**
     * Create a new boss special attack manager
     * @param plugin Plugin instance
     * @param boss Boss entity
     * @param attackState Boss attack state for tracking
     * @param config Special attack configuration
     */
    public BossSpecialAttackManager(Plugin plugin, Skeleton boss, BossAttackState attackState,
                                  SpecialAttackConfiguration config) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.boss = boss;
        this.attackState = attackState;
        this.config = config;

        this.isActive = false;
        this.currentPhase = SpecialAttackPhase.NONE;
        this.phaseTimer = 0;
        this.isWarriorSummonTimerPaused = false;
        this.warriorSummonTimerPauseStartTime = 0;

        // Initialize components
        this.safeZoneManager = new SafeZoneManager(plugin);
    }

    /**
     * Check if special attack can be started based on warrior summon requirements
     * @return True if special attack requirements are met
     */
    public boolean canStartSpecialAttack() {
        // Check basic attack state requirements
        if (!attackState.canStartSpecialAttack(config.getMinSpecialAttackSpacingSeconds(),
                                              config.requiresWarriorSummonBetweenSpecials())) {
            return false;
        }

        // Check hemisphere-specific warrior summon requirements
        if (!attackState.canStartHemisphereSpecialAttack(MINIMUM_WARRIOR_WAVES,
                                                         WAIT_AFTER_SECOND_WAVE_SECONDS)) {
            return false;
        }

        // Check if boss is in a valid state
        if (boss == null || !boss.isValid() || boss.isDead()) {
            return false;
        }

        // Check if not already in special attack
        if (isActive || attackState.isInSpecialAttack()) {
            return false;
        }

        return true;
    }

    /**
     * Start the special attack sequence
     * @param isArrowPhase Whether to use arrows (phase 2) or skulls (phase 1)
     * @return True if special attack started successfully
     */
    public boolean startSpecialAttack(boolean isArrowPhase) {
        if (!canStartSpecialAttack()) {
            logger.warning("[BossSpecialAttackManager] Cannot start special attack - requirements not met");
            return false;
        }

        // Initialize attack state
        isActive = true;
        originalBossLocation = boss.getLocation().clone();
        currentPhase = SpecialAttackPhase.RISING_ANIMATION;
        phaseTimer = 0;
        phaseStartTime = System.currentTimeMillis();
        currentPhaseTimeout = calculatePhaseTimeout(SpecialAttackPhase.RISING_ANIMATION);

        // Calculate airborne target position
        airborneTargetLocation = originalBossLocation.clone().add(0, config.getRisingHeight(), 0);

        // Pause warrior summon timer
        pauseWarriorSummonTimer();

        // Update attack state
        attackState.startSpecialAttack(BossAttackState.SpecialAttackPhase.RISING_ANIMATION, originalBossLocation);
        attackState.pauseWarriorSummonTimer();

        logger.info(String.format("[BossSpecialAttackManager] phase_from=NONE phase_to=RISING_ANIMATION attack_type=%s",
            (isArrowPhase ? "arrow" : "skull")));

        // Start the main attack task
        attackTask = startAttackTask(isArrowPhase);

        return true;
    }

    /**
     * Start the main attack coordination task
     * @param isArrowPhase Whether this is arrow phase or skull phase
     * @return The created BukkitRunnable task
     */
    private BukkitRunnable startAttackTask(boolean isArrowPhase) {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive || boss == null || !boss.isValid()) {
                    stopSpecialAttack(false);
                    return;
                }

                updateAttackPhase(isArrowPhase);
                phaseTimer++;

                // Check for phase-specific timeout with recovery
                if (checkPhaseTimeout(isArrowPhase)) {
                    // Recovery failed, must abort
                    logger.warning("[BossSpecialAttackManager] Phase timeout recovery failed, forcing stop");
                    stopSpecialAttack(false);
                    return;
                }
                
                // Keep global timeout as fallback safety
                if (attackState.hasSpecialAttackTimedOut()) {
                    logger.warning("[BossSpecialAttackManager] Global timeout reached, forcing stop");
                    stopSpecialAttack(false);
                    return;
                }
            }
        };

        task.runTaskTimer(plugin, 0L, 1L); // Every tick
        return task;
    }

    /**
     * Update current attack phase
     * @param isArrowPhase Whether this is arrow phase or skull phase
     */
    private void updateAttackPhase(boolean isArrowPhase) {
        switch (currentPhase) {
            case RISING_ANIMATION:
                executeRisingAnimation(isArrowPhase);
                break;

            case HEMISPHERE_FORMATION:
                executeHemisphereFormation(isArrowPhase);
                break;

            case SAFE_ZONES_APPEARING:
                executeSafeZonesAppearing();
                break;

            case FINAL_PREPARATION:
                executeFinalPreparation();
                break;

            case HEMISPHERE_ATTACK:
                executeHemisphereAttack(isArrowPhase);
                break;

            case POST_ATTACK_WAIT:
                executePostAttackWait();
                break;

            case GROUND_TOUCHDOWN:
                executeGroundTouchdown();
                break;

            default:
                break;
        }
    }

    /**
     * Execute boss rising animation phase
     * @param isArrowPhase Whether this is arrow phase or skull phase
     */
    private void executeRisingAnimation(boolean isArrowPhase) {
        // Keep boss airborne during rising
        keepBossAirborne();

        // Spawn rising particles around boss
        if (phaseTimer % 5 == 0) { // Every 5 ticks
            spawnRisingParticles();
        }

        // Check if rising animation is complete
        if (phaseTimer >= config.getRisingDuration()) {
            transitionPhase(SpecialAttackPhase.HEMISPHERE_FORMATION);
            startHemisphereFormation(isArrowPhase);
        }
    }

    /**
     * Execute hemisphere formation phase
     * @param isArrowPhase Whether to use arrows or skulls
     */
    private void executeHemisphereFormation(boolean isArrowPhase) {
        // Keep boss airborne
        keepBossAirborne();

        // Update formation progress
        if (isArrowPhase) {
            if (arrowHemisphereFormation != null && !arrowHemisphereFormation.isForming()) {
                arrowHemisphereFormation.updateFormation();
            }
        } else {
            if (hemisphereFormation != null && !hemisphereFormation.isForming()) {
                hemisphereFormation.updateFormation();
            }
        }

        // Check if formation is complete and all skulls are in attack phase
        boolean formationComplete = isArrowPhase ?
            (arrowHemisphereFormation != null && arrowHemisphereFormation.isComplete()) :
            (hemisphereFormation != null && hemisphereFormation.isComplete());

        // For skull phase, also check that all skulls have transformed to attack phase
        boolean skullsReadyForAttack = isArrowPhase ? true :
            (hemisphereFormation != null && hemisphereFormation.areAllSkullsInAttackPhase());

        if (formationComplete && skullsReadyForAttack) {
            transitionPhase(SpecialAttackPhase.SAFE_ZONES_APPEARING);
            startSafeZoneCreation();
        }
    }

    /**
     * Execute safe zones appearing phase
     */
    private void executeSafeZonesAppearing() {
        // Keep boss airborne
        keepBossAirborne();

        // Check if safe zone appearance is complete
        if (!safeZoneManager.isSequentialAppearanceActive()) {
            transitionPhase(SpecialAttackPhase.FINAL_PREPARATION);
        }
    }

    /**
     * Execute final preparation phase
     */
    private void executeFinalPreparation() {
        // Keep boss airborne
        keepBossAirborne();

        // Spawn preparation particles
        if (phaseTimer % 3 == 0) {
            spawnPreparationParticles();
        }

        // Check if preparation is complete (use config value)
        if (phaseTimer >= config.getFinalPrepDurationTicks()) {
            transitionPhase(SpecialAttackPhase.HEMISPHERE_ATTACK);
            launchHemisphereAttack();
        }
    }

    /**
     * Execute hemisphere attack phase
     * @param isArrowPhase Whether to use arrows or skulls
     */
    private void executeHemisphereAttack(boolean isArrowPhase) {
        // Keep boss airborne during entire attack
        keepBossAirborne();

        // Check if attack is complete (all projectiles launched and hit)
        boolean attackComplete = isArrowPhase ?
            (arrowHemisphereFormation == null || arrowHemisphereFormation.getValidArrowCount() == 0) :
            (hemisphereFormation == null || hemisphereFormation.getValidSkullCount() == 0);

        if (attackComplete) {
            transitionPhase(SpecialAttackPhase.POST_ATTACK_WAIT);
        }
    }

    /**
     * Execute post-attack wait phase
     */
    private void executePostAttackWait() {
        // Keep boss airborne for post-attack hover
        keepBossAirborne();

        // Spawn post-attack particles
        if (phaseTimer % 4 == 0) {
            spawnPostAttackParticles();
        }

        // Check if post-attack wait is complete (1 second = 20 ticks)
        if (phaseTimer >= POST_ATTACK_WAIT_DURATION_TICKS) {
            transitionPhase(SpecialAttackPhase.GROUND_TOUCHDOWN);
        }
    }

    /**
     * Execute ground touchdown phase
     */
    private void executeGroundTouchdown() {
        // Smooth descent animation to ground
        double progress = (double) phaseTimer / GROUND_TOUCHDOWN_DURATION_TICKS;
        progress = Math.min(1.0, progress);

        // Interpolate position from airborne target to ground
        Location currentPos = airborneTargetLocation.clone().add(
            originalBossLocation.clone().subtract(airborneTargetLocation).multiply(progress)
        );

        boss.teleport(currentPos);

        // Spawn descent particles
        if (phaseTimer % 2 == 0) {
            spawnDescentParticles(currentPos);
        }

        // Check if touchdown is complete
        if (phaseTimer >= GROUND_TOUCHDOWN_DURATION_TICKS) {
            // Boss has landed, resume warrior summon timer
            resumeWarriorSummonTimer();
            stopSpecialAttack(true);
        }
    }

    /**
     * Transition to next phase
     * @param nextPhase Next phase to transition to
     */
    private void transitionPhase(SpecialAttackPhase nextPhase) {
        long elapsed = System.currentTimeMillis() - phaseStartTime;
        logger.info(String.format("[BossSpecialAttackManager] phase_from=%s phase_to=%s elapsed_ms=%d",
            currentPhase, nextPhase, elapsed));
        
        currentPhase = nextPhase;
        phaseTimer = 0;
        phaseStartTime = System.currentTimeMillis();
        currentPhaseTimeout = calculatePhaseTimeout(nextPhase);
    }
    
    /**
     * Calculate timeout for a specific phase based on configuration
     * BUGFIX: Returns milliseconds (not ticks) for comparison with System.currentTimeMillis()
     * @param phase The phase to calculate timeout for
     * @return Timeout duration in milliseconds
     */
    private int calculatePhaseTimeout(SpecialAttackPhase phase) {
        double multiplier = config.getPhaseTimeoutMultiplier();
        int ticks;
        
        switch (phase) {
            case RISING_ANIMATION:
                ticks = (int) (config.getRisingDuration() * multiplier);
                break;
                
            case HEMISPHERE_FORMATION:
                ticks = (int) (config.getHemisphereFormationDurationTicks() * multiplier);
                break;
                
            case SAFE_ZONES_APPEARING:
                ticks = (int) (config.getSafeZonesWindowTicks() * multiplier);
                break;
                
            case FINAL_PREPARATION:
                ticks = (int) (config.getFinalPrepDurationTicks() * multiplier);
                break;
                
            case POST_ATTACK_WAIT:
                ticks = (int) (POST_ATTACK_WAIT_DURATION_TICKS * multiplier);
                break;
                
            case GROUND_TOUCHDOWN:
                ticks = (int) (GROUND_TOUCHDOWN_DURATION_TICKS * multiplier);
                break;
                
            case HEMISPHERE_ATTACK:
                // Attack phase timeout is longer - allow projectiles to complete
                ticks = 200; // 10 seconds max for all projectiles
                break;
                
            default:
                ticks = 100; // Default 5 second timeout
                break;
        }
        
        // Convert ticks to milliseconds (1 tick = 50ms)
        return ticks * 50;
    }
    
    /**
     * Check if current phase has timed out and attempt recovery
     * BUGFIX: Compare elapsed milliseconds (not ticks) against timeout
     * @param isArrowPhase Whether this is arrow phase or skull phase
     * @return True if recovery failed and must abort, false if phase is on schedule or recovery succeeded
     */
    private boolean checkPhaseTimeout(boolean isArrowPhase) {
        long elapsed = System.currentTimeMillis() - phaseStartTime;
        
        if (elapsed <= currentPhaseTimeout) {
            return false; // Not timed out, continue normally
        }
        
        logger.warning(String.format(
            "[BossSpecialAttackManager] phase=%s timeout_hit=true elapsed_ms=%d expected_ms=%d actual_ticks=%d",
            currentPhase, elapsed, currentPhaseTimeout, phaseTimer));
        
        // Attempt phase-specific recovery
        boolean recoveryFailed = attemptPhaseRecovery(isArrowPhase);
        return recoveryFailed; // Return true only if recovery failed and must abort
    }
    
    /**
     * Attempt to recover from a phase timeout
     * BUGFIX: Return value indicates recovery FAILURE (true = must abort, false = recovered successfully)
     * @param isArrowPhase Whether this is arrow phase or skull phase
     * @return True if recovery FAILED and must abort, false if recovery succeeded
     */
    private boolean attemptPhaseRecovery(boolean isArrowPhase) {
        switch (currentPhase) {
            case RISING_ANIMATION:
                // If boss is partially risen, can proceed to formation
                double currentHeight = boss.getLocation().getY() - originalBossLocation.getY();
                if (currentHeight >= config.getRisingHeight() * 0.5) {
                    logger.info("[BossSpecialAttackManager] Recovery: Boss partially risen, advancing to formation");
                    transitionPhase(SpecialAttackPhase.HEMISPHERE_FORMATION);
                    startHemisphereFormation(isArrowPhase);
                    return false; // Recovery succeeded, continue attack
                }
                break;
                
            case HEMISPHERE_FORMATION:
                // BUGFIX: Check if minimal viable formation exists - if so, ADVANCE to next phase
                int skullCount = isArrowPhase ?
                    (arrowHemisphereFormation != null ? arrowHemisphereFormation.getValidArrowCount() : 0) :
                    (hemisphereFormation != null ? hemisphereFormation.getValidSkullCount() : 0);
                    
                if (skullCount >= config.getMinViableSkullThreshold()) {
                    logger.info(String.format(
                        "[BossSpecialAttackManager] Recovery: Viable formation skull_count=%d threshold=%d, advancing to safe zones",
                        skullCount, config.getMinViableSkullThreshold()));
                    transitionPhase(SpecialAttackPhase.SAFE_ZONES_APPEARING);
                    startSafeZoneCreation();
                    return false; // Recovery succeeded, continue attack
                } else {
                    logger.warning(String.format(
                        "[BossSpecialAttackManager] Recovery: Insufficient formation skull_count=%d threshold=%d, aborting",
                        skullCount, config.getMinViableSkullThreshold()));
                    return true; // Recovery failed, must abort
                }
                
            case SAFE_ZONES_APPEARING:
                // If any safe zones created, can proceed
                if (safeZoneManager != null && safeZoneManager.getActiveZoneCount() > 0) {
                    logger.info(String.format(
                        "[BossSpecialAttackManager] Recovery: Safe zones present zone_count=%d, advancing to final prep",
                        safeZoneManager.getActiveZoneCount()));
                    transitionPhase(SpecialAttackPhase.FINAL_PREPARATION);
                    return false; // Recovery succeeded, continue attack
                }
                break;
                
            case FINAL_PREPARATION:
            case POST_ATTACK_WAIT:
            case GROUND_TOUCHDOWN:
                // These phases can be fast-forwarded
                logger.info(String.format("[BossSpecialAttackManager] Recovery: Fast-forwarding phase=%s", currentPhase));
                if (currentPhase == SpecialAttackPhase.FINAL_PREPARATION) {
                    transitionPhase(SpecialAttackPhase.HEMISPHERE_ATTACK);
                    launchHemisphereAttack();
                } else if (currentPhase == SpecialAttackPhase.POST_ATTACK_WAIT) {
                    transitionPhase(SpecialAttackPhase.GROUND_TOUCHDOWN);
                } else {
                    resumeWarriorSummonTimer();
                    stopSpecialAttack(true);
                }
                return false; // Recovery succeeded, continue attack
                
            case HEMISPHERE_ATTACK:
                // If attack is stuck, force cleanup and proceed
                logger.info("[BossSpecialAttackManager] Recovery: Force completing hemisphere attack");
                transitionPhase(SpecialAttackPhase.POST_ATTACK_WAIT);
                return false; // Recovery succeeded, continue attack
        }
        
        // Recovery failed - must abort
        logger.severe(String.format(
            "[BossSpecialAttackManager] Recovery failed for phase=%s, aborting attack", currentPhase));
        return true; // Recovery failed, must abort
    }

    /**
     * Start hemisphere formation
     * @param isArrowPhase Whether this is arrow phase or skull phase
     */
    private void startHemisphereFormation(boolean isArrowPhase) {
        World world = boss.getWorld();
        if (world == null) {
            return;
        }

        if (isArrowPhase) {
            // Create arrow hemisphere formation
            arrowHemisphereFormation = new ArrowHemisphereFormation(
                plugin, boss.getLocation(), 8.0, 14, boss
            );
            arrowHemisphereFormation.startFormation();

            logger.info("[BossSpecialAttackManager] Started arrow hemisphere formation");
        } else {
            // Create skull hemisphere formation with config
            hemisphereFormation = new HemisphereFormation(
                plugin, boss.getLocation(), config.getHemisphereRadius(), 
                config.getHemisphereSkullCount(), boss, config
            );
            hemisphereFormation.startFormation();

            logger.info(String.format("[BossSpecialAttackManager] Started skull hemisphere formation skull_count=%d radius=%.1f radius_variance=%.1f polar_angle_min=%.1f polar_angle_max=%.1f formation_duration_ticks=%d",
                config.getHemisphereSkullCount(), config.getHemisphereRadius(), config.getHemisphereRadiusVariance(),
                config.getHemispherePolarAngleMin(), config.getHemispherePolarAngleMax(), config.getHemisphereFormationDurationTicks()));
        }
    }

    /**
     * Start safe zone creation with sequential appearance
     */
    private void startSafeZoneCreation() {
        // CRITICAL: Capture target players NOW before they can move into safe zones
        capturedTargets = getPlayersOutsideSafeZones();
        
        logger.info(String.format(
            "[BossSpecialAttackManager] Captured %d target players for hemisphere attack (before safe zones appear)",
            capturedTargets.size()));
        
        for (Player p : capturedTargets) {
            logger.info(String.format("[DEBUG-CAPTURE] Player '%s' captured at: %.2f, %.2f, %.2f", 
                p.getName(),
                p.getLocation().getX(), 
                p.getLocation().getY(), 
                p.getLocation().getZ()));
        }
        
        // Count nearby players
        int playerCount = countNearbyPlayers(20.0);
        
        // Calculate safe zone count according to design spec:
        // - At least minSafeZones if players present
        // - Formula: min(players + 1, maxSafeZones)
        int safeZoneCount;
        if (playerCount > 0) {
            safeZoneCount = Math.min(playerCount + 1, config.getMaxSafeZones());
            safeZoneCount = Math.max(safeZoneCount, config.getMinSafeZonesWhenPlayersPresent());
        } else {
            // No players - minimal zones for safety
            safeZoneCount = 1;
        }

        // Generate safe zones with shared world anchors
        safeZoneManager.generateHemisphereSafeZones(
            boss.getLocation(),           // center (shared anchor)
            15.0,                        // max radius
            3.0,                         // safe zone radius
            30,                          // duration seconds
            safeZoneCount - 1,           // pass count-1 since method adds +1
            true,                        // sequential appearance
            config.getSafeZonesWindowTicks() / Math.max(safeZoneCount, 1) // distribute evenly
        );

        logger.info(String.format(
            "[BossSpecialAttackManager] zone_count=%d player_count=%d min_zones=%d max_zones=%d",
            safeZoneCount, playerCount, config.getMinSafeZonesWhenPlayersPresent(), 
            config.getMaxSafeZones()));
    }

    /**
     * Launch hemisphere attack at targets
     */
    private void launchHemisphereAttack() {
        // Use captured targets instead of querying current safe zone status
        List<Player> targets = (capturedTargets != null) ? capturedTargets : new ArrayList<>();
        
        logger.info(String.format(
            "[BossSpecialAttackManager] Using %d captured targets for attack (captured before safe zones appeared)",
            targets.size()));

        // DEBUG: Log boss and player positions
        logger.info(String.format("[DEBUG-ATTACK] Boss location: %.2f, %.2f, %.2f", 
            boss.getLocation().getX(), 
            boss.getLocation().getY(), 
            boss.getLocation().getZ()));
        
        for (Player p : targets) {
            logger.info(String.format("[DEBUG-ATTACK] Player '%s' location: %.2f, %.2f, %.2f", 
                p.getName(),
                p.getLocation().getX(), 
                p.getLocation().getY(), 
                p.getLocation().getZ()));
        }

        // Generate sphere pattern targets
        List<Location> attackTargets = WitherSkullProjectile.generateSpherePatternTargets(
            boss.getLocation(), targets, 14
        );

        // DEBUG: Log generated attack targets with distances
        logger.info(String.format("[DEBUG-ATTACK] Generated %d attack targets:", attackTargets.size()));
        for (int i = 0; i < attackTargets.size(); i++) {
            Location loc = attackTargets.get(i);
            double distFromBoss = boss.getLocation().distance(loc);
            double distFromPlayer = targets.isEmpty() ? -1.0 : targets.get(0).getLocation().distance(loc);
            logger.info(String.format("[DEBUG-ATTACK] Target[%d]: %.2f, %.2f, %.2f | dist_from_boss=%.2f | dist_from_player=%.2f",
                i, loc.getX(), loc.getY(), loc.getZ(), distFromBoss, distFromPlayer));
        }

        // Launch skulls at targets
        if (hemisphereFormation != null) {
            hemisphereFormation.launchSkullsAtTargets(attackTargets);
        }

        logger.info(String.format("[BossSpecialAttackManager] Launched hemisphere attack target_count=%d players_outside_safe_zones=%d",
                   attackTargets.size(), targets.size()));
    }

    /**
     * Keep boss airborne at target position
     */
    private void keepBossAirborne() {
        if (boss == null || !boss.isValid()) {
            return;
        }

        // Ensure boss stays at airborne position
        Location currentPos = boss.getLocation();
        if (currentPos.distance(airborneTargetLocation) > 1.0) {
            boss.teleport(airborneTargetLocation);
        }
    }

    /**
     * Count nearby players within radius
     * @param radius Search radius
     * @return Number of nearby players
     */
    private int countNearbyPlayers(double radius) {
        if (boss == null) {
            return 0;
        }

        int count = 0;
        for (Player player : boss.getWorld().getPlayers()) {
            if (player.getLocation().distance(boss.getLocation()) <= radius) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get players who are not in safe zones
     * @return List of players outside safe zones
     */
    private List<Player> getPlayersOutsideSafeZones() {
        List<Player> players = new ArrayList<>();

        if (boss == null) {
            return players;
        }

        for (Player player : boss.getWorld().getPlayers()) {
            if (player.getLocation().distance(boss.getLocation()) <= 25.0) { // Within attack range
                if (!safeZoneManager.isInSafeZone(player.getLocation())) {
                    players.add(player);
                }
            }
        }

        return players;
    }

    /**
     * Pause warrior summon timer
     */
    private void pauseWarriorSummonTimer() {
        if (!isWarriorSummonTimerPaused) {
            isWarriorSummonTimerPaused = true;
            warriorSummonTimerPauseStartTime = System.currentTimeMillis();

            // Update attack state
            attackState.pauseWarriorSummonTimer();

            logger.info("[BossSpecialAttackManager] Warrior summon timer paused");
        }
    }

    /**
     * Resume warrior summon timer
     */
    private void resumeWarriorSummonTimer() {
        if (isWarriorSummonTimerPaused) {
            isWarriorSummonTimerPaused = false;
            long pauseDuration = System.currentTimeMillis() - warriorSummonTimerPauseStartTime;

            // Update attack state
            attackState.resumeWarriorSummonTimer();

            logger.info("[BossSpecialAttackManager] Warrior summon timer resumed after " +
                       (pauseDuration / 1000) + " seconds");
        }
    }

    /**
     * Stop the special attack
     * @param completed Whether attack completed successfully
     */
    public void stopSpecialAttack(boolean completed) {
        if (!isActive) {
            return;
        }

        isActive = false;
        currentPhase = SpecialAttackPhase.NONE;

        // Cancel attack task
        if (attackTask != null) {
            attackTask.cancel();
            attackTask = null;
        }

        // Clean up formations
        if (hemisphereFormation != null) {
            hemisphereFormation.cleanup();
            hemisphereFormation = null;
        }

        if (arrowHemisphereFormation != null) {
            arrowHemisphereFormation.cleanup();
            arrowHemisphereFormation = null;
        }

        // Clean up safe zones
        if (safeZoneManager != null) {
            safeZoneManager.cleanup();
        }
        
        // Clear captured targets
        capturedTargets = null;

        // Ensure boss is on ground
        if (boss != null && boss.isValid() && originalBossLocation != null) {
            boss.teleport(originalBossLocation);
        }

        // Update attack state
        attackState.endSpecialAttack(completed);

        // Make sure warrior summon timer is resumed
        resumeWarriorSummonTimer();

        logger.info(String.format("[BossSpecialAttackManager] Special attack stopped completed=%s final_phase=%s",
            completed, currentPhase));
    }

    /**
     * Check if special attack is currently active
     * @return True if attack is active
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Get current attack phase
     * @return Current phase
     */
    public SpecialAttackPhase getCurrentPhase() {
        return currentPhase;
    }

    /**
     * Check if warrior summon timer is paused
     * @return True if timer is paused
     */
    public boolean isWarriorSummonTimerPaused() {
        return isWarriorSummonTimerPaused;
    }

    // Particle effect methods
    private void spawnRisingParticles() {
        World world = boss.getWorld();
        if (world == null) return;

        Location loc = boss.getLocation();
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 3, 0.5, 0.2, 0.5, 0.1);
        world.spawnParticle(Particle.END_ROD, loc, 2, 0.3, 0.3, 0.3, 0.05);
    }

    private void spawnPreparationParticles() {
        World world = boss.getWorld();
        if (world == null) return;

        Location loc = boss.getLocation();
        world.spawnParticle(Particle.DRAGON_BREATH, loc, 5, 0.3, 0.3, 0.3, 0.1);
        world.spawnParticle(Particle.GLOW, loc, 3, 0.2, 0.2, 0.2, 0.02);
    }

    private void spawnPostAttackParticles() {
        World world = boss.getWorld();
        if (world == null) return;

        Location loc = boss.getLocation();
        world.spawnParticle(Particle.END_ROD, loc, 4, 0.4, 0.1, 0.4, 0.05);
        world.spawnParticle(Particle.CRIT, loc, 2, 0.2, 0.2, 0.2, 0.02);
    }

    private void spawnDescentParticles(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        world.spawnParticle(Particle.FALLING_DUST, location, 2, 0.3, 0.1, 0.3, 0.02);
        world.spawnParticle(Particle.SMOKE, location, 1, 0.2, 0.2, 0.2, 0.01);
    }

    /**
     * Clean up all resources
     */
    public void cleanup() {
        stopSpecialAttack(false);
    }
}