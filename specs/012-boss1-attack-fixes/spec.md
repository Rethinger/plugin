# Feature Specification: Boss 1 Attack Fixes

**Feature Branch**: `012-boss1-attack-fixes`  
**Created**: 2025-11-04  
**Status**: Draft  
**Input**: User description: "атака босса №1 дублируется кучу раз а так же ее надо переделать а именно: 1) убрать стан босса перед спец атакой. 2) сделать так что бы во время спец атаки босс эпично поднимался с красивыми визуальными эффектами и к нему поднимались черепа которые он потом будет кастовать в игроков он их будет кастовать сферой в огромном количестве что бы попасть по всем игрокам которые не находяться в безопасных зонах а из самих безопасных зон убрать шлейф который идет в верх. 3) убрать спавн скелетов воинов на время каста атаки. 4) убрать боссу и воинам которых он спавнит боязнь волков которая есть у обычных скелетов."
## Clarifications
### Session 2025-11-04
- Q: Should the boss physically move upward in the Y-axis during the special attack, or should it be a visual effect only? → A: Physical movement - boss should move upward in Y-axis
- Q: Should the skulls physically spawn at ground level and move upward to boss position, or should it be a visual effect? → A: Skulls should physically spawn at ground level and move upward to boss position


- Q: What specific visual effects should accompany the boss rising animation? → A: Soul fire and end rod particles for an epic dark magic effect


- Q: How many skull projectiles should be in the sphere pattern? → A: Make it more epic with 32-40 projectiles in full sphere pattern

## User Scenarios & Testing *(mandatory)*
- Q: How high should the boss rise during the special attack? → A: Rise to a specific height (e.g., 10-15 blocks total)


<!--
  IMPORTANT: User stories should be PRIORITIZED as user journeys ordered by importance.
  Each user story/journey must be INDEPENDENTLY TESTABLE - meaning if you implement just ONE of them,
  you should still have a viable MVP (Minimum Viable Product) that delivers value.
  
 Assign priorities (P1, P2, P3, etc.) to each story, where P1 is the most critical.
  Think of each story as a standalone slice of functionality that can be:
  - Developed independently
  - Tested independently
  - Deployed independently
  - Demonstrated to users independently
-->

### User Story 1 - Improved Boss 1 Special Attack (Priority: P1)

Players experience a more epic and visually engaging special attack from Boss 1 without the disruptive stuns and unwanted skeleton warrior spawning. The boss performs a dramatic rising animation with visual effects while casting skull projectiles in a sphere pattern that targets players outside safe zones.

**Why this priority**: This is the core gameplay improvement that addresses the main issue described by the user - making the boss fight more engaging and visually impressive while fixing the mechanical problems.

**Independent Test**: Can be fully tested by initiating Boss 1's special attack and verifying that the boss rises with visual effects, casts skull projectiles in a sphere pattern, and does not spawn skeleton warriors during the attack.

**Acceptance Scenarios**:

1. **Given** Boss 1 is in combat and ready to perform special attack, **When** special attack is triggered, **Then** boss rises with visual effects instead of being stunned, and no skeleton warriors spawn during the cast
2. **Given** Boss 1 is performing special attack, **When** skull projectiles are cast, **Then** they form a sphere pattern that targets players outside safe zones
3. **Given** players are in safe zones during boss special attack, **When** skull projectiles are cast, **Then** players in safe zones are protected from damage

---

### User Story 2 - Enhanced Visual Effects for Boss Attack (Priority: P1)

The boss special attack includes dramatic visual elements that make the encounter more epic and memorable, with rising effects and skull projectiles that create an immersive experience.

**Why this priority**: This directly addresses the user's request for "эпично поднимался с красивыми визуальными эффектами" (epic rise with beautiful visual effects).

**Independent Test**: Can be tested by observing the visual effects during boss special attack and confirming they match the epic, dramatic aesthetic described.

**Acceptance Scenarios**:

1. **Given** Boss 1 begins special attack, **When** attack sequence starts, **Then** boss performs rising animation with visual effects
2. **Given** Boss 1 is casting skull projectiles, **When** skulls emerge, **Then** they rise toward the boss with visual effects before being launched

---

### User Story 3 - Safe Zone Improvement (Priority: P2)

Safe zones during boss special attack no longer have the vertical beacon effect that was visually distracting, making them cleaner and more focused on protection.

**Why this priority**: This is a visual enhancement that improves the clarity of safe zones as requested by the user.

**Independent Test**: Can be tested by triggering the boss special attack and observing safe zones to confirm the vertical beacon effect is removed.

**Acceptance Scenarios**:

1. **Given** safe zones are active during boss special attack, **When** safe zones are displayed, **Then** no vertical beacon effect appears above them

---

### User Story 4 - Remove Wolves' Fear Mechanic (Priority: P2)

Boss 1 and its summoned skeleton warriors no longer have the default Minecraft mechanic that causes them to run away from wolves, making the encounter more consistent.

**Why this priority**: This fixes a gameplay inconsistency that affects boss behavior and summoned minions as requested.

**Independent Test**: Can be tested by having wolves near the boss and summoned warriors to confirm they don't flee from them.

**Acceptance Scenarios**:

1. **Given** wolves are near Boss 1, **When** wolves approach, **Then** boss does not flee from wolves
2. **Given** wolves are near skeleton warriors summoned by boss, **When** wolves approach, **Then** warriors do not flee from wolves

---

### Edge Cases

- What happens when the boss tries to rise but there's a ceiling above it?
- How does the system handle the sphere of skull projectiles when there are many players in the arena?
- What if all players are in safe zones during the special attack?

## Requirements *(mandatory)*

<!--
  ACTION REQUIRED: The content in this section represents placeholders.
  Fill them out with the right functional requirements.
-->

### Functional Requirements

- **FR-01**: System MUST remove the stun/freeze effect that currently occurs before Boss 1's special attack
- **FR-002**: System MUST make Boss 1 perform a rising animation with visual effects during special attack cast
- **FR-003**: System MUST prevent skeleton warrior spawning during Boss 1's special attack cast
- **FR-004**: System MUST make skull projectiles rise toward the boss with visual effects before being launched in sphere pattern
- **FR-005**: System MUST cast skull projectiles in a sphere pattern that targets players outside safe zones
- **FR-006**: System MUST remove the vertical beacon effect (END_ROD particles) from safe zones during special attack
- **FR-007**: System MUST remove wolves' fear mechanic from Boss 1, preventing it from fleeing when wolves are nearby
- **FR-008**: System MUST remove wolves' fear mechanic from skeleton warriors summoned by Boss 1
- **FR-009**: System MUST ensure that safe zones continue to provide protection during the modified special attack
- **FR-010**: System MUST maintain existing damage multipliers and safe zone protection mechanics

### Key Entities *(include if feature involves data)*

- **Boss 1**: The Skeleton Lord boss entity that performs special attacks, now with modified behavior to remove stun and wolves' fear
- **Safe Zone**: Temporary damage-immune area during boss special attack, now without vertical beacon effect
- **Skeleton Warrior**: Minions summoned by Boss 1, now without wolves' fear mechanic
- **Wither Skull Projectile**: Projectiles cast during special attack, now with rising visual effects and sphere pattern targeting

## Success Criteria *(mandatory)*

<!--
  ACTION REQUIRED: Define measurable success criteria.
 These must be technology-agnostic and measurable.
-->

### Measurable Outcomes

- **SC-001**: Players can experience Boss 1's special attack without the boss being stunned before casting
- **SC-002**: Boss 1 performs a rising animation with visual effects during special attack cast in 100% of special attack instances
- **SC-003**: No skeleton warriors spawn during Boss 1's special attack cast in 100% of special attack instances
- **SC-004**: Safe zones are displayed without vertical beacon effect during special attacks in 100% of instances
- **SC-005**: Boss 1 does not flee from wolves in 100% of encounters with wolves nearby
- **SC-006**: Skeleton warriors summoned by Boss 1 do not flee from wolves in 100% of encounters with wolves nearby
- **SC-007**: Players in safe zones remain protected during the modified special attack in 100% of cases
- **SC-008**: Skull projectiles are launched in sphere pattern targeting players outside safe zones in 100% of special attack instances