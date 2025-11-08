# Implementation Plan: Boss 1 Attack Fixes

**Branch**: `012-boss1-attack-fixes` | **Date**: 2025-11-04 | **Spec**: [specs/012-boss1-attack-fixes/spec.md](specs/012-boss1-attack-fixes/spec.md)
**Input**: Feature specification from `/specs/[012-boss1-attack-fixes]/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Fix Boss 1's special attack mechanics by removing the stun effect before casting, implementing an epic rising animation with visual effects, casting skull projectiles in a sphere pattern, removing skeleton warrior spawning during the attack, and eliminating the wolves' fear mechanic from both the boss and summoned warriors.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: PaperMC API 1.21.x, Bukkit API, NpcApi-Paper  
**Storage**: N/A (in-memory during combat)  
**Testing**: N/A (part of existing plugin)  
**Target Platform**: PaperMC 1.21.x Minecraft server  
**Project Type**: Server plugin  
**Performance Goals**: Maintain 20 TPS during boss combat with multiple players and visual effects  
**Constraints**: <200ms p95 response time for combat actions, avoid excessive particle load that could impact server performance  
**Scale/Scope**: Supports up to 20 players in boss arena simultaneously

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Based on the project constitution, the following gates need to be evaluated:

### Gate 1: Combat Mechanics Compliance
- **Requirement**: Changes to boss behavior must maintain existing damage multipliers and safe zone protection mechanics
- **Status**: PASS - Feature preserves existing safe zone protection while modifying visual/behavioral aspects only

### Gate 2: Performance Compliance
- **Requirement**: New visual effects must not significantly impact server TPS
- **Status**: CONDITIONAL PASS - Particle effects need to be optimized to avoid server performance degradation

### Gate 3: Architecture Compliance
- **Requirement**: Changes must work with existing boss combat system in Act2Listener
- **Status**: PASS - Modifications will extend existing BossAttackState and Act2Listener patterns

### Gate 4: Safety Compliance
- **Requirement**: Safe zone modifications must continue to provide protection during combat
- **Status**: PASS - Safe zone protection mechanics remain unchanged, only visual beacon effect removed

### Gate 5: Code Standards Compliance
- **Requirement**: Follow existing project patterns and conventions (PascalCase, camelCase, UPPER_SNAKE_CASE)
- **Status**: PASS - Will follow existing code style patterns

### Gate 6: Data Management Compliance
- **Requirement**: Proper data handling through existing managers
- **Status**: PASS - No new data persistence required, using existing in-memory combat mechanics

## Project Structure

### Documentation (this feature)

```text
specs/012-boss1-attack-fixes/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/main/java/com/mmmm/story/
├── bosses/              # Boss combat mechanics including BossAttackState
├── listeners/           # Event handlers, including Act2Listener for Boss 1
└── managers/            # Game managers
```

**Structure Decision**: Modifications to existing boss combat system in accordance with project architecture, following existing patterns in Act2Listener and BossAttackState classes.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |

## Re-evaluated Constitution Check (Post-Design)

*GATE: Confirm all requirements still met after design phase.*

### Gate 1: Combat Mechanics Compliance
- **Requirement**: Changes to boss behavior must maintain existing damage multipliers and safe zone protection mechanics
- **Status**: PASS - Design preserves existing safe zone protection while modifying visual/behavioral aspects only

### Gate 2: Performance Compliance
- **Requirement**: New visual effects must not significantly impact server TPS
- **Status**: CONDITIONAL PASS - Particle effects in design need to be optimized to avoid server performance degradation

### Gate 3: Architecture Compliance
- **Requirement**: Changes must work with existing boss combat system in Act2Listener
- **Status**: PASS - Design extends existing BossAttackState and Act2Listener patterns

### Gate 4: Safety Compliance
- **Requirement**: Safe zone modifications must continue to provide protection during combat
- **Status**: PASS - Safe zone protection mechanics remain unchanged, only visual beacon effect removed

### Gate 5: Code Standards Compliance
- **Requirement**: Follow existing project patterns and conventions (PascalCase, camelCase, UPPER_SNAKE_CASE)
- **Status**: PASS - Design follows existing code style patterns

### Gate 6: Data Management Compliance
- **Requirement**: Proper data handling through existing managers
- **Status**: PASS - No new data persistence required, using existing in-memory combat mechanics

### Gate 7: API Consistency Compliance
- **Requirement**: New functionality must be accessible through appropriate interfaces
- **Status**: PASS - API contracts defined for configuration and triggering of new mechanics

### Gate 8: Gameplay Consistency Compliance
- **Requirement**: Changes must not break existing gameplay flow
- **Status**: PASS - Only modifies specific boss behavior without affecting overall game flow

## Summary

This implementation plan outlines the complete approach for fixing Boss 1's attack mechanics as specified in the feature requirements. The plan includes:

- **Research**: Comprehensive analysis of implementation approaches for each requirement
- **Data Model**: Clear definition of entities and relationships for the new mechanics
- **API Contracts**: Well-defined interfaces for configuring and triggering the new boss behaviors
- **Quickstart Guide**: Step-by-step instructions for implementing the changes
- **Constitution Compliance**: Verification that all architectural requirements are met

The plan addresses all user requirements from the feature specification:
1. ✅ Remove the stun effect before special attack
2. ✅ Implement epic rising animation with visual effects
3. ✅ Create skull projectiles that rise to boss and launch in sphere pattern
4. ✅ Remove skeleton warrior spawning during special attack casting
5. ✅ Remove wolves' fear mechanic from boss and summoned warriors
6. ✅ Remove vertical beacon effect from safe zones during special attacks

The implementation will enhance the player experience during Boss 1 combat while maintaining all existing gameplay mechanics and performance standards.

## Agent Context Update

The following command needs to be executed to update the agent context with the new technology from the current plan:

```powershell
.specify/scripts/powershell/update-agent-context.ps1 -AgentType kilocode
```

This script will:
- Detect that the KiloCode agent is in use
- Update the appropriate agent-specific context file
- Add only new technology from the current plan
- Preserve manual additions between markers