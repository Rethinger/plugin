# Change: Add Enderman Boss (Boss #2)

## Why
The current story campaign needs a second boss to provide progression between Act 3 (Skeleton Lord) and Act 4 (Blaze Master). An Enderman-themed boss with complex teleportation mechanics and clone abilities will create a unique challenge that requires different player strategies than the existing melee-focused boss.

## What Changes
- Add new EndermanBossManager with teleportation and clone mechanics
- Implement two-phase combat system with different abilities per phase
- Create clone system that spawns decoy Endermen with shadow disintegration effects
- Add vampirism mechanic for boss healing during Phase 1
- Implement healing shield mechanic for Phase 2 with player interaction requirements
- Add epic entrance sequence with vertical rift visual effects
- Include water immunity and weather control capabilities
- Add anti-build mechanics (player dropping above boss, block breaking)
- Create comprehensive particle effects system for all abilities
- Add configuration parameters for all boss mechanics and timing

## Impact
- **Affected specs**: boss-mechanics (new capability)
- **Affected code**:
  - `src/main/java/com/mmmm/story/bosses/` (new EndermanBossManager.java)
  - `src/main/java/com/mmmm/story/listeners/Act3Listener.java` (boss integration)
  - `config.yml` (boss configuration parameters)
  - Existing boss system classes (extend BossAttackState enum)

## Technical Considerations
- Performance optimization for handling 10-20 simultaneous clones
- Water immunity implementation through custom damage handling
- Weather control integration with existing weather systems
- Particle effect optimization for multiplayer environments
- Teleportation queue system to prevent conflicts
- Clone identification system that balances deception with playability