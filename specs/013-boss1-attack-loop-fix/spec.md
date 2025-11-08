# Feature Specification: Boss 1 Attack Loop and Localization Fix

**Feature Branch**: `013-boss1-attack-loop-fix`  
**Created**: 2025-11-04  
**Status**: Draft  
**Input**: User description: "дошло дело до убийства босса и он начал взлетать зациклилась специальная атака и он улетел по оси Y а так же в консоль вывелось это [17:41:07 INFO]: ... [17:48:36 WARN]: [ServerStoryPlugin] Missing localization key: boss1.special_attack.warning"

## User Scenarios & Testing *(mandatory)*

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

### User Story 1 - Fix Boss 1 Special Attack Loop (Priority: P1)

Players experience a properly functioning Boss 1 special attack without the boss getting stuck in a loop or flying away uncontrollably. The boss performs its special attack mechanics as intended without breaking the gameplay flow.

**Why this priority**: This is the core gameplay issue that breaks the boss fight experience, making it impossible for players to complete the encounter.

**Independent Test**: Can be fully tested by initiating Boss 1's special attack and verifying that the attack completes its sequence without getting stuck in a loop or causing the boss to fly away uncontrollably.

**Acceptance Scenarios**:

1. **Given** Boss 1 is in combat and triggers special attack, **When** special attack sequence begins, **Then** boss performs rising animation without getting stuck in a loop
2. **Given** Boss 1 is performing special attack, **When** attack sequence executes, **Then** boss remains within reasonable Y-axis bounds and returns to normal combat behavior
3. **Given** Boss 1 special attack completes, **When** sequence ends, **Then** boss returns to normal combat state without being stuck

---

### User Story 2 - Add Missing Special Attack Warning Localization (Priority: P1)

Players receive appropriate warning messages in their configured language when Boss 1 is about to perform its special attack, with no console warnings about missing localization keys.

**Why this priority**: This addresses the missing localization key that's causing console warnings and ensures players get proper warning messages during boss combat.

**Independent Test**: Can be tested by triggering Boss 1's special attack and verifying that appropriate warning messages are displayed in the player's language without console warnings.

**Acceptance Scenarios**:

1. **Given** Boss 1 is about to perform special attack, **When** warning should be displayed, **Then** localized message appears using the boss1.special_attack.warning key
2. **Given** server is running, **When** Boss 1 special attack triggers, **Then** no "Missing localization key" warnings appear in console
3. **Given** player has configured language, **When** special attack warning displays, **Then** message appears in player's preferred language

---

### User Story 3 - Enhanced Boss Combat Stability (Priority: P2)

Boss 1 combat mechanics operate reliably without unexpected behavior that disrupts the player experience, ensuring a consistent and enjoyable boss fight.

**Why this priority**: This ensures the overall stability of the boss encounter after fixing the specific loop issue.

**Independent Test**: Can be tested by engaging Boss 1 in combat multiple times and verifying consistent behavior without unexpected flight or stuck states.

**Acceptance Scenarios**:

1. **Given** players engage Boss 1 in combat, **When** various attack phases occur, **Then** boss behavior remains predictable and within expected parameters
2. **Given** special attack completes, **When** boss returns to normal combat, **Then** all combat mechanics function normally

---

### Edge Cases

- What happens when the boss reaches the maximum Y-axis boundary during special attack?
- How does the system handle the special attack if the boss takes damage during the rising animation?
- What if multiple players are in the arena during the special attack sequence?
- How does the system recover if the boss somehow gets into an invalid state during the attack?

## Requirements *(mandatory)*

<!--
  ACTION REQUIRED: The content in this section represents placeholders.
  Fill them out with the right functional requirements.
-->

### Functional Requirements

- **FR-01**: System MUST prevent Boss 1 from getting stuck in an infinite loop during special attack
- **FR-02**: System MUST ensure Boss 1 does not fly away uncontrollably on the Y-axis during special attack (limit to 10-15 blocks above original position)
- **FR-03**: System MUST add the missing localization key boss1.special_attack.warning to appropriate language files
- **FR-004**: System MUST display localized warning message when Boss 1 begins special attack
- **FR-005**: System MUST ensure console does not display missing localization key warnings for boss1.special_attack.warning
- **FR-006**: System MUST validate that Boss 1 returns to normal combat state after special attack completes
- **FR-007**: System MUST maintain existing special attack visual effects and mechanics while fixing the loop issue (preserve all existing visual effects including soul fire, end rod particles, etc.)
- **FR-008**: System MUST ensure boss Y-axis position remains within reasonable bounds during special attack (20 blocks maximum as mentioned in success criteria)
- **FR-009**: System MUST properly clean up any special attack state after completion to prevent stuck conditions

### Key Entities *(include if feature involves data)*

- **Boss 1**: The Skeleton Lord boss entity that performs special attacks, now with fixed loop and flight behavior
- **Special Attack State**: The state tracking system for boss special attacks, now with proper completion and cleanup
- **Localization Messages**: System messages including boss1.special_attack.warning, now properly implemented

## Success Criteria *(mandatory)*

<!--
  ACTION REQUIRED: Define measurable success criteria.
 These must be technology-agnostic and measurable.
-->

### Measurable Outcomes

- **SC-001**: Boss 1 special attack completes without getting stuck in infinite loop in 100% of instances
- **SC-002**: Boss 1 does not fly away uncontrollably on Y-axis during special attack in 100% of instances
- **SC-003**: Localization key boss1.special_attack.warning exists and displays appropriate message in 100% of cases
- **SC-004**: No "Missing localization key: boss1.special_attack.warning" warnings appear in console during boss special attack
- **SC-005**: Players receive appropriate warning message when Boss 1 begins special attack in 100% of instances
- **SC-006**: Boss 1 returns to normal combat state after special attack completes in 100% of instances
- **SC-007**: Boss Y-axis position remains within reasonable bounds (not exceeding 200 blocks above spawn) during special attack in 100% of instances
- **SC-008**: Special attack sequence properly cleans up state to prevent stuck conditions in 100% of instances