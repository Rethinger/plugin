## Context
The Enderman boss serves as the second major boss encounter in the 5-act story campaign, positioned between the Skeleton Lord (melee-focused) and Blaze Master (ranged-focused). This boss introduces complex mobility mechanics and deception tactics, requiring players to develop new strategies beyond basic combat. The implementation must integrate with the existing manager-based architecture while introducing new systems for clone management and teleportation control.

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
1. Create EndermanBossManager with basic functionality
2. Implement Phase 1 mechanics (teleportation, basic clones)
3. Add Phase 2 mechanics (healing shield, clone waves)
4. Integrate with existing boss progression system
5. Add visual effects and polish
6. Performance testing and optimization

**Rollback:** Disable boss in configuration, remove from Act progression if critical issues arise

## Open Questions
- Should clones have different movement patterns than the boss?
- How should the boss behave when all players are out of range?
- Should the healing shield have visual damage indicators?
- How to handle boss behavior during server restart/combat interruption?
- Should the boss have any environmental interaction beyond water immunity?