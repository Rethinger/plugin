# Implementation Plan: Boss 1 Attack Loop and Localization Fix

**Branch**: `013-boss1-attack-loop-fix` | **Date**: 2025-11-04 | **Spec**: [specs/013-boss1-attack-loop-fix/spec.md](specs/013-boss1-attack-loop-fix/spec.md)

**Input**: Feature specification from `/specs/[013-boss1-attack-loop-fix]/spec.md`

## Summary

Fix the Boss 1 special attack loop issue where the boss gets stuck in an infinite loop and flies away uncontrollably on the Y-axis, while also adding the missing localization key `boss1.special_attack.warning` to resolve console warnings and provide appropriate player warnings.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: PaperMC API 1.21.x, Bukkit API, NpcApi-Paper library, Maven  
**Storage**: YAML files for configuration and messages  
**Testing**: JUnit for unit tests, existing test infrastructure  
**Target Platform**: Minecraft Paper server 1.21.x  
**Project Type**: Minecraft plugin  
**Performance Goals**: Maintain 20 TPS during boss combat, keep boss Y-axis position within reasonable bounds (not exceeding 200 blocks above spawn)  
**Constraints**: <200ms p95 for boss state updates, maintain existing visual effects during special attack  
**Scale/Scope**: Single boss entity fix affecting Boss 1 special attack mechanics

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Based on the MmmmStoryPlugin Constitution:
1. **Technology Stack Compliance**: Uses Java 21 and PaperMC API 1.21.x - COMPLIANT
2. **Manager-Based Architecture**: Changes will be made within existing boss management classes - COMPLIANT
3. **Data Management**: Localization fix will follow YAML format standards - COMPLIANT
4. **Event-Driven Listener**: Boss attack logic is handled in boss-specific classes - COMPLIANT

All constitution checks pass. No violations identified.

## Project Structure

### Documentation (this feature)

```text
specs/013-boss1-attack-loop-fix/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/main/java/com/mmmm/story/bosses/
├── BossAttackState.java         # Boss state management - may need updates for loop fix
├── BossRisingAnimation.java     # Boss rising animation - primary target for loop fix
├── SpecialAttackConfiguration.java # Special attack configuration - may need bounds checking
└── WithesSkullProjectile.java   # Projectile handling - related to special attack

src/main/resources/
├── messages_en.yml              # English messages - needs boss1.special_attack.warning key
├── messages_ru.yml              # Russian messages - needs boss1.special_attack.warning key
└── dialogs_en.yml, dialogs_ru.yml # May need special attack warning messages

src/main/java/com/mmmm/story/managers/
└── MessageManager.java          # Message localization - may need to handle new key
```

**Structure Decision**: Single project structure following existing manager-based architecture pattern with updates to boss-specific classes and localization files.

## Phase 0: Outline & Research

### Research Tasks

1. **Boss Attack Loop Analysis**: Research the current Boss 1 special attack implementation to identify the root cause of the infinite loop and Y-axis flight issue
   - Focus on BossRisingAnimation.java and related boss attack state management
   - Identify where the loop termination condition is missing or incorrect

2. **Y-Axis Boundary Implementation**: Research best practices for implementing Y-axis bounds during boss special attacks
   - Determine appropriate maximum Y-axis displacement (mentioned as 10-15 blocks in requirements)
   - Investigate existing position tracking and boundary enforcement mechanisms

3. **Localization Key Integration**: Research the existing localization system to properly implement the boss1.special_attack.warning key
   - Understand how MessageManager handles localization keys
   - Identify where the warning should be triggered during special attack

### Expected Research Outcomes

- Identification of specific code causing the infinite loop in BossRisingAnimation
- Understanding of current position tracking and how to implement bounds
- Clear understanding of localization system for proper message integration

## Phase 1: Design & Contracts

### Data Model Updates

1. **Boss Attack State Enhancement**: Update BossAttackState to include proper loop termination conditions and Y-axis boundary tracking
2. **Special Attack Configuration**: Enhance SpecialAttackConfiguration with Y-axis boundary parameters

### API Contracts

1. **Message Manager API**: Define contract for the new boss1.special_attack.warning localization key
2. **Boss Attack API**: Define any new methods needed for boundary checking and loop termination

### Quickstart Guide

1. **Setup Instructions**: Document how to test the fixed boss attack behavior
2. **Verification Steps**: Provide steps to confirm the loop fix and proper localization

## Phase 1: Agent Context Update

Run `.specify/scripts/powershell/update-agent-context.ps1 -AgentType kilocode` to update agent-specific context with new boss attack implementation details.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| N/A | N/A | N/A |