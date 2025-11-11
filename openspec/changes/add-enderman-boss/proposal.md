# Change: Replace Boss #2 with Enderman

## Why
The current Boss #2 (Изверг Адских Глубин/Blaze Master - Wither-based) needs replacement with a more unique and mechanically interesting boss. An Enderman-themed boss with teleportation and clone mechanics will provide better gameplay variety and create a more memorable encounter that fits the progression between Skeleton Lord (melee) and the existing combat mechanics.

## What Changes
- **REPLACE** current Wither-based Boss #2 (Изверг Адских Глубин) with Enderman-based boss
- Add new EndermanBossManager with teleportation and clone mechanics
- Implement two-phase combat system with different abilities per phase
- Create clone system that spawns decoy Endermen with shadow disintegration effects
- Add vampirism mechanic for boss healing during Phase 1
- Implement healing shield mechanic for Phase 2 with player interaction requirements
- Add epic entrance sequence with vertical rift visual effects
- Include water immunity and weather control capabilities
- Add anti-build mechanics (player dropping above boss, block breaking)
- Create comprehensive particle effects system for all abilities
- Update boss name and messages to maintain "Изверг Адских Глубин" identity
- Modify summoning ritual to fit Enderman theme
- **BREAKING**: Remove existing Wither-based boss mechanics from Act2Listener

## Impact
- **Affected specs**: boss-mechanics (replacement of existing capability)
- **Affected code**:
  - `src/main/java/com/mmmm/story/listeners/Act2Listener.java` (replace Wither boss with Enderman)
  - `src/main/java/com/mmmm/story/bosses/` (new EndermanBossManager.java)
  - `config.yml` (replace boss #2 configuration parameters)
  - `messages.yml` (update boss messages while keeping name "Изверг Адских Глубин")
  - Existing boss system classes (extend BossAttackState enum)
- **Breaking change**: Existing Boss #2 encounter completely replaced

## Technical Considerations
- Performance optimization for handling 10-20 simultaneous clones
- Water immunity implementation through custom damage handling
- Weather control integration with existing weather systems
- Particle effect optimization for multiplayer environments
- Teleportation queue system to prevent conflicts
- Clone identification system that balances deception with playability