## Context
The Enderman boss **replaces** the existing Boss #2 (Изверг Адских Глубин - Wither-based) in the 5-act story campaign. Positioned in Act 2, this replacement provides more engaging mechanics than the current Wither-based boss while maintaining the same narrative role. The new boss introduces complex mobility mechanics and deception tactics through teleportation and clone systems, requiring players to develop different strategies than the melee-focused Skeleton Lord in Act 3. The implementation must replace existing Wither mechanics in Act2Listener while integrating with the existing manager-based architecture.

## Goals / Non-Goals
**Goals:**
- Create a visually spectacular boss encounter with memorable mechanics
- Implement challenging but fair combat that rewards skill and observation
- Provide clear visual feedback for all abilities and states
- Maintain performance with multiple simultaneous entities
- Ensure compatibility with existing boss progression systems

**Non-Goals:**
- Implement AI pathfinding beyond basic teleportation
- Create persistent boss entities across server restarts
- Add complex loot systems (beyond existing boss progression)
- Implement multiplayer synchronization beyond basic entity management
- Maintain any existing Wither-based mechanics (complete replacement)

## Decisions

### Decision: Two-Phase Combat System
**What:** Divide boss encounter into Phase 1 (100%-50% HP) and Phase 2 (50%-0% HP) with different ability sets
**Why:** Creates progressive difficulty and maintains player engagement through evolving mechanics
**Alternatives considered:** Single-phase with all abilities (too complex), Three-phase (unnecessary complexity)

### Decision: Clone System with Shadow Death Effect
**What:** Spawn decoy Endermen that disappear after one hit with particle effects
**Why:** Creates deception gameplay without overwhelming performance or confusing players
**Alternatives considered:** Permanent clones (performance issues), Clones with full health (too difficult)

### Decision: Queue-Based Teleportation System
**What:** Implement scheduled queue for teleportation events to prevent conflicts
**Why:** Prevents teleportation bugs and ensures smooth ability execution
**Alternatives considered:** Immediate teleportation (conflict risk), Random timing (unpredictable)

### Decision: Water Immunity Through Damage Override
**What:** Override EntityDamageEvent for water damage instead of complex water physics
**Why:** Simpler implementation that achieves the core requirement without performance overhead
**Alternatives considered:** Water freezing mechanics (complex), Custom water blocks (maintenance overhead)

## Risks / Trade-offs

**Risk:** Performance degradation with multiple clones
- **Mitigation:** Implement entity pooling, limit particle effects, immediate cleanup

**Risk:** Players cannot distinguish real boss from clones
- **Mitigation:** Subtle visual differences (glow effects), different sound profiles, behavior patterns

**Risk:** Teleportation conflicts causing entity loss
- **Mitigation:** Queue system, safe location validation, teleport logging

**Risk:** Water immunity causing balance issues
- **Mitigation:** Limited duration abilities, clear visual indicators, counterplay options

**Trade-off:** Complexity vs Playability
- Chose simpler mechanics that are fun over overly complex systems that might confuse players

## Migration Plan
**Steps:**
1. Backup existing Boss #2 mechanics in Act2Listener
2. Remove current Wither-based boss implementation
3. Create EndermanBossManager with basic functionality
4. Implement Phase 1 mechanics (teleportation, basic clones)
5. Add Phase 2 mechanics (healing shield, clone waves)
6. Update summoning ritual to fit Enderman theme
7. Maintain existing boss name "Изверг Адских Глубин" in messages
8. Update configuration parameters for new mechanics
9. Add visual effects and polish
10. Performance testing and optimization

**Rollback:** Restore Wither-based boss from backup, disable new boss in configuration

## Resolved Questions
- **Clone Attack Patterns**: Clones should NOT have different attack patterns (Q1)
- **Shield Hit Visual Effects**: Beautiful particles when shield is hit (Q2)
- **Healing Success**: 3-second healing duration (Q3)
- **Additional Environmental Interactions**: Yes, possible to add more (Q4)

## Open Questions
- What specific particle effects should appear when shield is hit?
- How should the boss behave during extended periods without player contact?
- Should water freezing have additional effects beyond basic immunity?
- What additional environmental interactions could be implemented?