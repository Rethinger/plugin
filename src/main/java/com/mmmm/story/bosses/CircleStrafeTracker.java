package com.mmmm.story.bosses;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks player movement patterns to detect circle-strafing behavior around bosses.
 * Used to prevent players from exploiting boss AI by circling around them.
 */
public class CircleStrafeTracker {
    private final Player player;
    private final double minDistance;
    private final double maxDistance;
    private final double angleThreshold;
    private final int minAngleChanges;
    private final long trackingDurationMillis;
    
    private final List<Location> positionHistory;
    private final List<Long> timestampHistory;
    private int angleChangeCount;
    private boolean confirmed;
    private long startTime;
    
    /**
     * Create a new circle strafe tracker for a player.
     * @param player The player to track
     * @param minDistance Minimum distance from boss to consider tracking
     * @param maxDistance Maximum distance from boss to consider tracking
     * @param angleThreshold Minimum angle change to count as direction change
     * @param minAngleChanges Minimum angle changes before confirming strafing
     * @param trackingDurationSeconds How long to track before resetting
     */
    public CircleStrafeTracker(Player player, double minDistance, double maxDistance, 
                             double angleThreshold, int minAngleChanges, int trackingDurationSeconds) {
        this.player = player;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.angleThreshold = angleThreshold;
        this.minAngleChanges = minAngleChanges;
        this.trackingDurationMillis = trackingDurationSeconds * 1000L;
        
        this.positionHistory = new ArrayList<>();
        this.timestampHistory = new ArrayList<>();
        this.angleChangeCount = 0;
        this.confirmed = false;
        this.startTime = System.currentTimeMillis();
    }
    
    /**
     * Update tracker with new player position.
     * @param playerLocation Current player location
     * @param bossLocation Current boss location
     */
    public void update(Location playerLocation, Location bossLocation) {
        // Check if tracking duration exceeded
        if (System.currentTimeMillis() - startTime > trackingDurationMillis) {
            reset();
            return;
        }
        
        // Check if player is in valid distance range
        double distance = playerLocation.distance(bossLocation);
        if (distance < minDistance || distance > maxDistance) {
            reset();
            return;
        }
        
        // Add position to history
        positionHistory.add(playerLocation.clone());
        timestampHistory.add(System.currentTimeMillis());
        
        // Keep only recent positions (last 2 seconds)
        long cutoffTime = System.currentTimeMillis() - 2000L;
        while (!timestampHistory.isEmpty() && timestampHistory.get(0) < cutoffTime) {
            positionHistory.remove(0);
            timestampHistory.remove(0);
        }
        
        // Check for circular movement pattern
        if (positionHistory.size() >= 3) {
            detectCircularMovement(bossLocation);
        }
    }
    
    /**
     * Detect if player is moving in a circular pattern around the boss.
     * @param bossLocation The boss's location
     */
    private void detectCircularMovement(Location bossLocation) {
        if (positionHistory.size() < 3) return;
        
        // Calculate angles from boss to player at different time points
        double angle1 = calculateAngle(bossLocation, positionHistory.get(0));
        double angle2 = calculateAngle(bossLocation, positionHistory.get(positionHistory.size() / 2));
        double angle3 = calculateAngle(bossLocation, positionHistory.get(positionHistory.size() - 1));
        
        // Check if player is consistently changing direction (circling)
        double angleChange1 = normalizeAngle(angle2 - angle1);
        double angleChange2 = normalizeAngle(angle3 - angle2);
        
        // If both angle changes are in the same direction and above threshold
        if (Math.abs(angleChange1) > angleThreshold && Math.abs(angleChange2) > angleThreshold) {
            // Check if signs are the same (same direction)
            if ((angleChange1 > 0 && angleChange2 > 0) || (angleChange1 < 0 && angleChange2 < 0)) {
                angleChangeCount++;
                
                if (angleChangeCount >= minAngleChanges) {
                    confirmed = true;
                }
            }
        }
    }
    
    /**
     * Calculate angle from center to target location.
     * @param center Center location
     * @param target Target location
     * @return Angle in radians
     */
    private double calculateAngle(Location center, Location target) {
        double deltaX = target.getX() - center.getX();
        double deltaZ = target.getZ() - center.getZ();
        return Math.atan2(deltaZ, deltaX);
    }
    
    /**
     * Normalize angle to [-π, π] range.
     * @param angle The angle to normalize
     * @return Normalized angle
     */
    private double normalizeAngle(double angle) {
        while (angle > Math.PI) {
            angle -= 2 * Math.PI;
        }
        while (angle < -Math.PI) {
            angle += 2 * Math.PI;
        }
        return angle;
    }
    
    /**
     * Reset tracker state.
     */
    public void reset() {
        positionHistory.clear();
        timestampHistory.clear();
        angleChangeCount = 0;
        confirmed = false;
        startTime = System.currentTimeMillis();
    }
    
    /**
     * Check if circle strafing has been confirmed.
     * @return True if player is confirmed to be circle strafing
     */
    public boolean isConfirmed() {
        return confirmed;
    }
    
    /**
     * Get the tracked player.
     * @return The player being tracked
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Get current angle change count.
     * @return Number of detected angle changes
     */
    public int getAngleChangeCount() {
        return angleChangeCount;
    }
    
    /**
     * Get tracking start time.
     * @return Timestamp when tracking started
     */
    public long getStartTime() {
        return startTime;
    }
}