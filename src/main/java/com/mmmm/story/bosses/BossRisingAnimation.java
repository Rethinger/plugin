package com.mmmm.story.bosses;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Skeleton;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;

/**
 * Handles the boss rising animation during special attack.
 * Manages vertical movement, particle effects, and timing.
 */
public class BossRisingAnimation {
    
    private final Plugin plugin;
    private final SpecialAttackConfiguration config;
    private final Skeleton boss;
    private final BossAttackState attackState;
    
    // Animation state
    private Location originalLocation;
    private Location targetLocation;
    private boolean isAnimating;
    private boolean hasCompleted; // NEW: Guard to prevent multiple completions
    private int currentTick;
    private int totalTicks;
    
    // Boundary enforcement
    private double yAxisBoundaryOffset;
    private long maxAnimationTime;
    private long animationStartTime;
    
    // Animation tasks
    private BukkitRunnable animationTask;
    private BukkitRunnable particleTask;
    private BukkitRunnable boundaryCheckTask;
    
    /**
     * @deprecated Use constructor with BossAttackState parameter for proper boundary tracking
     * Create a new boss rising animation
     * @param plugin Plugin instance
     * @param config Special attack configuration
     * @param boss Boss entity to animate
     */
    @Deprecated
    public BossRisingAnimation(Plugin plugin, SpecialAttackConfiguration config, Skeleton boss) {
        this.plugin = plugin;
        this.config = config;
        this.boss = boss;
        this.attackState = null;
        this.originalLocation = null;
        this.targetLocation = null;
        this.isAnimating = false;
        this.hasCompleted = false;
        this.currentTick = 0;
        this.totalTicks = 0;
        this.yAxisBoundaryOffset = 15.0; // Default 15 blocks
        this.maxAnimationTime = 15000; // Default 15 seconds
        this.animationStartTime = 0;
        this.animationTask = null;
        this.particleTask = null;
        this.boundaryCheckTask = null;
        
        plugin.getLogger().warning("[Boss Animation] DEPRECATED CONSTRUCTOR USED: BossRisingAnimation without BossAttackState - boundary checking disabled");
    }
    
    /**
     * Create a new boss rising animation with attack state tracking
     * @param plugin Plugin instance
     * @param config Special attack configuration
     * @param boss Boss entity to animate
     * @param attackState Boss attack state for boundary tracking
     */
    public BossRisingAnimation(Plugin plugin, SpecialAttackConfiguration config, Skeleton boss, BossAttackState attackState) {
        this.plugin = plugin;
        this.config = config;
        this.boss = boss;
        this.attackState = attackState;
        this.originalLocation = null;
        this.targetLocation = null;
        this.isAnimating = false;
        this.hasCompleted = false;
        this.currentTick = 0;
        this.totalTicks = 0;
        this.yAxisBoundaryOffset = attackState != null ? attackState.getYAxisBoundaryOffset() : 15.0;
        this.maxAnimationTime = attackState != null ? attackState.getMaxSpecialAttackDuration() : 15000;
        this.animationStartTime = 0;
        this.animationTask = null;
        this.particleTask = null;
        this.boundaryCheckTask = null;
    }
    
    /**
     * Start the rising animation
     * @return True if animation started successfully
     */
    public boolean startAnimation() {
        if (isAnimating || boss == null || !boss.isValid()) {
            return false;
        }
        
        // Store original location
        originalLocation = boss.getLocation().clone();
        
        // Set original position in attack state for boundary tracking
        if (attackState != null) {
            attackState.setOriginalPosition(originalLocation);
        }
        
        // Calculate target location with boundary enforcement
        double risingHeight = config.getRisingHeight();
        double maxRisingHeight = Math.min(risingHeight, yAxisBoundaryOffset);
        targetLocation = originalLocation.clone().add(0, maxRisingHeight, 0);
        
        // Get animation duration
        totalTicks = config.getRisingDuration();
        currentTick = 0;
        animationStartTime = System.currentTimeMillis();
        isAnimating = true;
        
        // BUGFIX: Play initial sound effects at animation start for dramatic feedback
        World world = boss.getWorld();
        if (world != null) {
            Location bossLoc = boss.getLocation();
            world.playSound(bossLoc, org.bukkit.Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.8f);
            world.playSound(bossLoc, org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.6f);
            plugin.getLogger().info("[Boss Animation] Starting rising animation with sound effects");
        }
        
        // Start animation tasks
        startMovementAnimation();
        startParticleEffects();
        startBoundaryChecking();
        
        return true;
    }
    
    /**
     * Start the vertical movement animation
     */
    private void startMovementAnimation() {
        if (animationTask != null) {
            animationTask.cancel();
        }
        
        animationTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isAnimating || boss == null || !boss.isValid()) {
                    cancel();
                    return;
                }
                
                currentTick++;
                
                // Calculate progress (0.0 to 1.0)
                double progress = (double) currentTick / totalTicks;
                
                // Use easing function for smooth animation
                double easedProgress = easeInOutCubic(progress);
                
                // Calculate current position
                Location currentPos = originalLocation.clone();
                double yOffset = (targetLocation.getY() - originalLocation.getY()) * easedProgress;
                currentPos.setY(originalLocation.getY() + yOffset);
                
                // Check Y-axis boundary before teleporting
                if (attackState != null && attackState.exceedsYAxisBoundary(currentPos)) {
                    plugin.getLogger().warning("[Boss Animation] Y-axis boundary exceeded during movement, forcing completion");
                    // Force completion if boundary exceeded
                    boss.teleport(currentPos);
                    completeAnimation();
                    return;
                }
                
                // Check timeout
                if (System.currentTimeMillis() - animationStartTime > maxAnimationTime) {
                    // Force completion if timeout exceeded
                    boss.teleport(currentPos);
                    completeAnimation();
                    return;
                }
                
                // Teleport boss to current position
                boss.teleport(currentPos);
                
                // Check if animation complete
                if (currentTick >= totalTicks) {
                    // Ensure boss reaches exact target
                    boss.teleport(targetLocation);
                    completeAnimation();
                }
            }
        };
        
        animationTask.runTaskTimer(plugin, 0L, 1L); // Every tick
    }
    
    /**
     * Start particle effects during rising animation
     */
    private void startParticleEffects() {
        if (particleTask != null) {
            particleTask.cancel();
        }
        
        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isAnimating || boss == null || !boss.isValid()) {
                    cancel();
                    return;
                }
                
                Location bossLoc = boss.getLocation();
                World world = bossLoc.getWorld();
                
                // Spawn configured particles
                Particle[] particles = config.getRisingParticles();
                for (Particle particleType : particles) {
                    spawnRisingParticles(world, bossLoc, particleType);
                }
                
                // Enhanced visual effects for User Story 2
                spawnEnhancedVisualEffects(world, bossLoc);
                
                // Add dramatic effects at key points
                if (currentTick == totalTicks / 2) {
                    // Mid-point effect
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, bossLoc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.EXPLOSION, bossLoc, 2, 0.5, 0.5, 0.5, 0);
                    world.playSound(bossLoc, org.bukkit.Sound.ENTITY_WITHER_AMBIENT, 1.5f, 0.5f);
                }
            }
        };
        
        particleTask.runTaskTimer(plugin, 0L, 3L); // Every 3 ticks for T024 performance optimization
    }
    
    /**
     * Start boundary checking task
     */
    private void startBoundaryChecking() {
        if (boundaryCheckTask != null) {
            boundaryCheckTask.cancel();
        }
        
        boundaryCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isAnimating || boss == null || !boss.isValid()) {
                    cancel();
                    return;
                }
                
                Location currentPos = boss.getLocation();
                
                // Check Y-axis boundary
                if (attackState != null && attackState.exceedsYAxisBoundary(currentPos)) {
                    plugin.getLogger().warning("[Boss Animation] Y-axis boundary exceeded during boundary check, forcing completion");
                    // Force state transition and stop animation
                    completeAnimation();
                    return;
                }
                
                // Check timeout
                if (System.currentTimeMillis() - animationStartTime > maxAnimationTime) {
                    // Force completion if timeout exceeded
                    completeAnimation();
                    return;
                }
            }
        };
        
        boundaryCheckTask.runTaskTimer(plugin, 5L, 5L); // Every 5 ticks for boundary checking
    }
    
    /**
     * Spawn enhanced visual effects for User Story 2
     * @param world World to spawn particles in
     * @param bossLoc Boss location
     */
    private void spawnEnhancedVisualEffects(World world, Location bossLoc) {
        // T019/T020: Enhanced soul fire and end rod particle effects
        if (config.hasSoulFireParticles()) {
            // Create swirling soul fire pattern around boss
            double angle = (currentTick * 0.1) % (2 * Math.PI);
            for (int i = 0; i < 6; i++) {
                double particleAngle = angle + (i * Math.PI / 3);
                double radius = 2.0 + Math.sin(currentTick * 0.05) * 0.5;
                
                double x = bossLoc.getX() + radius * Math.cos(particleAngle);
                double y = bossLoc.getY() + 1.0 + Math.sin(currentTick * 0.1) * 0.3;
                double z = bossLoc.getZ() + radius * Math.sin(particleAngle);
                
                Location particleLoc = new Location(world, x, y, z);
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 1, 0, 0.1, 0, 0.02);
            }
        }
        
        if (config.hasEndRodParticles()) {
            // Create vertical end rod columns around boss
            for (int i = 0; i < 4; i++) {
                double angle = (i * Math.PI / 2) + (currentTick * 0.05);
                double radius = 1.5;
                
                double x = bossLoc.getX() + radius * Math.cos(angle);
                double z = bossLoc.getZ() + radius * Math.sin(angle);
                
                // Create vertical column of end rod particles
                for (int y = 0; y < 4; y++) {
                    Location particleLoc = new Location(world, x, bossLoc.getY() + y * 0.5, z);
                    world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0.01);
                }
            }
        }
        
        // Add periodic burst effects for more visual impact
        if (currentTick % 10 == 0) {
            world.spawnParticle(Particle.ENCHANT, bossLoc, 5, 1, 1, 1, 0.5);
            world.spawnParticle(Particle.PORTAL, bossLoc, 3, 0.5, 0.5, 0.5, 0.1);
        }
    }
    
    /**
     * Spawn rising particles around the boss
     * @param world World to spawn particles in
     * @param location Boss location
     * @param particleType Type of particle to spawn
     */
    private void spawnRisingParticles(World world, Location location, Particle particleType) {
        // Create spiral pattern around boss
        int particleCount = 8;
        double radius = 1.5;
        
        for (int i = 0; i < particleCount; i++) {
            double angle = (i / (double) particleCount) * 2 * Math.PI;
            double x = location.getX() + radius * Math.cos(angle);
            double y = location.getY() + (Math.random() * 2.0); // Random Y variation
            double z = location.getZ() + radius * Math.sin(angle);
            
            Location particleLoc = new Location(world, x, y, z);
            
            // Add slight upward velocity
            double velocity = 0.1;
            
            world.spawnParticle(particleType, particleLoc, 1, 0, velocity, 0, 0.02);
        }
        
        // Add particles at boss feet
        Location feetLoc = location.clone().add(0, -0.5, 0);
        world.spawnParticle(particleType, feetLoc, 3, 0.3, 0.1, 0.3, 0.05);
    }
    
    /**
     * Easing function for smooth animation
     * @param t Progress value (0.0 to 1.0)
     * @return Eased progress value
     */
    private double easeInOutCubic(double t) {
        if (t < 0.5) {
            return 4 * t * t * t;
        } else {
            double p = 2 * t - 2;
            return 1 + p * p * p / 2;
        }
    }
    
    /**
     * Complete the animation and clean up
     */
    public void completeAnimation() {
        if (!isAnimating || hasCompleted) {
            return; // Already completed or in progress of completion
        }
        
        isAnimating = false;
        hasCompleted = true;
        
        plugin.getLogger().info("[Boss Animation] Completing rising animation");
        
        // Cancel all tasks
        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
        
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }
        
        if (boundaryCheckTask != null) {
            boundaryCheckTask.cancel();
            boundaryCheckTask = null;
        }
        
        // Update attack state to transition out of rising animation
        if (attackState != null && attackState.isRisingAnimation()) {
            attackState.setSpecialAttackPhase(BossAttackState.SpecialAttackPhase.CASTING_SKULLS);
            plugin.getLogger().info("[Boss Animation] Transitioned to CASTING_SKULLS phase");
        }
        
        // Final effects
        if (boss != null && boss.isValid()) {
            Location bossLoc = boss.getLocation();
            World world = bossLoc.getWorld();
            
            // Dramatic final effect
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, bossLoc, 2, 0, 0, 0, 0);
            world.spawnParticle(Particle.EXPLOSION, bossLoc, 5, 0.5, 0.5, 0.5, 0);
            world.spawnParticle(Particle.DRAGON_BREATH, bossLoc, 20, 1, 1, 1, 0.1);
            
            // Sound effect
            world.playSound(bossLoc, org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.7f);
            world.playSound(bossLoc, org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.2f);
        }
    }
    
    /**
     * Stop the animation immediately
     */
    public void stopAnimation() {
        if (!isAnimating) {
            return;
        }
        
        plugin.getLogger().info("[Boss Animation] Stopping rising animation");
        
        isAnimating = false;
        
        // Cancel all tasks
        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
        
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }
        
        if (boundaryCheckTask != null) {
            boundaryCheckTask.cancel();
            boundaryCheckTask = null;
        }
        
        // Teleport boss to target if still animating
        if (boss != null && boss.isValid() && targetLocation != null) {
            boss.teleport(targetLocation);
            plugin.getLogger().info("[Boss Animation] Teleported boss to target location: " + targetLocation.getBlockX() + "," + targetLocation.getBlockY() + "," + targetLocation.getBlockZ());
        }
        
        // Update attack state
        if (attackState != null && attackState.isRisingAnimation()) {
            attackState.setSpecialAttackPhase(BossAttackState.SpecialAttackPhase.CASTING_SKULLS);
            plugin.getLogger().info("[Boss Animation] Force transitioned to CASTING_SKULLS phase");
        }
    }
    
    /**
     * Check if animation is currently running
     */
    public boolean isAnimating() {
        return isAnimating;
    }
    
    /**
     * Get current animation progress (0.0 to 1.0)
     */
    public double getProgress() {
        if (totalTicks <= 0) {
            return 0.0;
        }
        return Math.min(1.0, (double) currentTick / totalTicks);
    }
    
    /**
     * Get original location before animation started
     */
    public Location getOriginalLocation() {
        return originalLocation != null ? originalLocation.clone() : null;
    }
    
    /**
     * Get target location at end of animation
     */
    public Location getTargetLocation() {
        return targetLocation != null ? targetLocation.clone() : null;
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        stopAnimation();
    }
    
    /**
     * Set Y-axis boundary offset for this animation
     * @param offset Maximum Y displacement in blocks
     */
    public void setYAxisBoundaryOffset(double offset) {
        this.yAxisBoundaryOffset = Math.max(5.0, Math.min(20.0, offset));
    }
    
    /**
     * Set maximum animation time in milliseconds
     * @param maxTime Maximum time in milliseconds
     */
    public void setMaxAnimationTime(long maxTime) {
        this.maxAnimationTime = Math.max(5000, Math.min(30000, maxTime));
    }
    
    /**
     * Check if animation has exceeded time limit
     * @return True if timeout exceeded
     */
    public boolean hasTimedOut() {
        return isAnimating && (System.currentTimeMillis() - animationStartTime) > maxAnimationTime;
    }
    
    /**
     * Get current Y-axis displacement from original position
     * @return Y displacement in blocks
     */
    public double getCurrentYDisplacement() {
        if (originalLocation == null || boss == null || !boss.isValid()) {
            return 0.0;
        }
        return Math.abs(boss.getLocation().getY() - originalLocation.getY());
    }
}