# Quickstart Guide: Boss 1 Attack Fixes

## Overview
This guide provides instructions for implementing the Boss 1 attack fixes, including the epic rising animation, sphere-pattern skull projectiles, and removal of unwanted behaviors.

## Prerequisites
- Java 21
- PaperMC 1.21.x server
- Bukkit API knowledge
- Understanding of Minecraft combat mechanics

## Implementation Steps

### 1. Update Boss Attack State Machine
Modify the existing `BossAttackState` enum and related logic to:
- Remove the stun/freeze state that occurs before special attack casting
- Add a new rising animation phase during special attack
- Prevent skeleton warrior spawning during special attack casting

### 2. Implement Boss Rising Animation
- Modify the boss's position using `Entity#teleport()` to move upward during special attack
- Add soul fire and end rod particle effects during the rising animation
- Rise to 10-15 blocks above the original position as specified

### 3. Create Skull Projectile System
- Implement a system to spawn wither skull projectiles at ground level around the arena
- Make projectiles rise toward the boss position with visual effects
- Implement sphere pattern targeting for projectiles to target players outside safe zones
- Configure 32-40 projectiles as specified for epic effect

### 4. Modify Safe Zone Visual Effects
- Remove the vertical beacon effect (END_ROD particles) from safe zones during special attacks
- Maintain all protective functionality of safe zones

### 5. Remove Wolves' Fear Mechanic
- Modify Boss 1's AI to ignore wolves during combat
- Modify summoned skeleton warriors to ignore wolves during combat
- Override default Minecraft skeleton behavior that causes fleeing from wolves

## Code Structure
The implementation will primarily modify:
- `src/main/java/com/mmmm/story/bosses/BossAttackState.java` - Update attack states
- `src/main/java/com/mmmm/story/listeners/Act2Listener.java` - Handle boss combat logic
- New/modified classes in `src/main/java/com/mmmm/story/bosses/` package for attack mechanics

## Configuration
The special attack behavior can be configured via:
- Projectile count (default: 32-40)
- Rising height (default: 10-15 blocks)
- Rising duration (default: 40 ticks)
- Particle effects (default: SOUL_FIRE_FLAME, END_ROD)

## Testing
1. Verify boss no longer stuns before special attack
2. Confirm boss rises with visual effects during special attack
3. Ensure skull projectiles follow sphere pattern targeting players outside safe zones
4. Verify skeleton warriors are not spawned during special attack casting
5. Confirm safe zones have no vertical beacon effect during attacks
6. Test that boss and summoned warriors no longer flee from wolves
7. Verify safe zone protection still functions properly

## Performance Considerations
- Monitor server TPS during boss combat with multiple players
- Optimize particle effects to avoid performance degradation
- Limit projectile count to maintain performance
- Use efficient algorithms for spherical distribution calculations

## Troubleshooting
- If boss doesn't rise: Check teleportation logic and permissions
- If projectiles don't follow sphere pattern: Verify vector calculations
- If wolves still scare boss: Ensure AI override is properly implemented
- If safe zones don't protect: Verify protection radius and collision detection