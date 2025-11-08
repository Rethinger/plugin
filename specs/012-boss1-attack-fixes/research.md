# Research: Boss 1 Attack Fixes

## Decision: Remove stun effect before special attack
- **Rationale**: The current boss behavior includes an unnecessary stun/freeze before special attack casting, which creates a poor gameplay experience
- **Implementation**: Modify the BossAttackState to skip the stun phase before special attack execution
- **Alternatives considered**: 
 - Keeping stun but reducing duration - rejected as user specifically requested removal
  - Replacing with different animation - rejected as the requirement is to remove the stun entirely

## Decision: Implement boss rising animation with visual effects
- **Rationale**: User requested "эпично поднимался с красивыми визуальными эффектами" (epic rise with beautiful visual effects)
- **Implementation**: 
  - Use Bukkit's entity teleportation to move boss upward during special attack cast
  - Add soul fire and end rod particles around the boss during the rising animation
  - Rise to 10-15 blocks above original position as specified
- **Alternatives considered**:
 - Using only visual effects without physical movement - rejected as user specified physical movement
  - Different particle effects - soul fire and end rod particles chosen for dark magic aesthetic

## Decision: Implement skull projectiles rising to boss before sphere casting
- **Rationale**: User requested skulls to rise toward boss with visual effects before being launched
- **Implementation**:
  - Spawn wither skull projectiles at ground level around the arena
  - Use Bukkit's projectile mechanics to move them upward to boss position
  - Apply visual effects (particles) during the rising phase
- **Alternatives considered**:
 - Pre-spawning skulls at boss position - rejected as user specified ground-level spawning with rising effect

## Decision: Sphere pattern targeting for skull projectiles
- **Rationale**: User requested 32-40 skull projectiles in a sphere pattern targeting players outside safe zones
- **Implementation**:
  - Generate spherical distribution of projectile vectors
  - Calculate targets based on player positions outside safe zones
  - Use 32-40 projectiles as specified for epic effect
- **Alternatives considered**:
 - Different projectile patterns - sphere chosen as specifically requested
  - Fixed number vs. dynamic scaling - fixed 32-40 chosen as specified

## Decision: Remove skeleton warrior spawning during special attack
- **Rationale**: User specifically requested removal of skeleton warrior spawning during boss special attack
- **Implementation**: Conditionally disable skeleton warrior spawning when boss enters special attack state
- **Alternatives considered**: 
  - Reducing spawn count - rejected as user requested complete removal during special attack

## Decision: Remove vertical beacon effect from safe zones
- **Rationale**: User requested removal of visual beacon effect (END_ROD particles) from safe zones during special attack
- **Implementation**: Modify safe zone particle effects to exclude vertical beacon effect while maintaining protective function
- **Alternatives considered**:
 - Different particle effects - removal chosen as specifically requested

## Decision: Remove wolves' fear mechanic from boss and summoned warriors
- **Rationale**: User requested removal of default Minecraft mechanic where skeletons flee from wolves
- **Implementation**: 
  - Modify boss AI to ignore wolves during combat
  - Modify summoned skeleton warriors to ignore wolves during combat
- **Alternatives considered**:
  - Custom fear resistance configuration - direct AI modification chosen for reliability

## Technology Research: Bukkit/PaperMC API for combat mechanics
- **Entity Movement**: Use `Entity#teleport(Location)` for boss rising animation
- **Particle Effects**: Use `World#spawnParticle()` with PARTICLE_SOUL_FIRE_FLAME and PARTICLE_END_ROD for visual effects
- **Projectile Mechanics**: Use Bukkit's projectile API to spawn and control wither skulls
- **Combat State Management**: Extend existing BossAttackState pattern to include new special attack behaviors
- **AI Modification**: Use Bukkit's entity targeting to override default skeleton fear behavior

## Performance Considerations
- Particle effects must be optimized to avoid server performance impact
- Limit projectile count to 32-40 as specified to balance epic effect with performance
- Use efficient algorithms for spherical distribution calculations