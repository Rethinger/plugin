# Quickstart Guide: Boss 1 Attack Loop and Localization Fix

## Overview

This guide provides step-by-step instructions for implementing and testing the Boss 1 attack loop fix and localization enhancement. This addresses the issue where Boss 1 gets stuck in an infinite loop during special attack and flies away uncontrollably on the Y-axis, while also adding the missing localization key.

## Prerequisites

- Java 21 or higher
- Maven 3.6.0 or higher
- PaperMC server 1.21.x
- MmmmStoryPlugin source code
- Basic understanding of Minecraft plugin development

## Setup Instructions

### 1. Clone and Prepare the Repository

```bash
git clone [your-repository-url]
cd [repository-directory]
```

### 2. Create the Feature Branch

```bash
git checkout -b 013-boss1-attack-loop-fix
```

### 3. Build the Plugin

```bash
mvn clean install
```

## Implementation Steps

### Step 1: Update Boss Attack State Management

1. Open `src/main/java/com/mmmm/story/bosses/BossAttackState.java`
2. Add or update state management logic to prevent infinite loops
3. Ensure proper transitions between states:
   - NORMAL_COMBAT → SPECIAL_ATTACK_PREPARATION → SPECIAL_ATTACK_RISING → SPECIAL_ATTACK_ACTIVE → SPECIAL_ATTACK_COOLDOWN → NORMAL_COMBAT

### Step 2: Fix Boss Rising Animation

1. Open `src/main/java/com/mmmm/story/bosses/BossRisingAnimation.java`
2. Implement Y-axis boundary checking:
   - Track original boss position before special attack
   - Limit Y-axis displacement to 10-15 blocks above original position
   - Force state transition if boundary is exceeded
3. Add timeout mechanism to prevent infinite rising

### Step 3: Add Missing Localization Key

1. Open `src/main/resources/messages_en.yml`
2. Add the missing key:
   ```yaml
   boss1:
     special_attack:
       warning: "Boss is preparing a special attack! Move away from the center!"
   ```
3. Open `src/main/resources/messages_ru.yml`
4. Add the Russian translation:
   ```yaml
   boss1:
     special_attack:
       warning: "Босс готовит специальную атаку! Отойдите от центра!"
   ```

### Step 4: Update Message Manager

1. Open `src/main/java/com/mmmm/story/managers/MessageManager.java`
2. Ensure the new localization key is properly handled
3. Verify that messages are sent to players in their configured language

### Step 5: Test the Implementation

1. Start your Paper server with the updated plugin
2. Trigger Boss 1 special attack
3. Verify:
   - Boss does not get stuck in infinite loop
   - Boss does not fly away uncontrollably on Y-axis
   - Warning message is displayed to players
   - No "Missing localization key" warnings in console

## Configuration Options

The special attack behavior can be configured in the plugin configuration:

```yaml
bosses:
  special_attacks:
    max_rising_time: 8  # Maximum seconds for rising animation
    y_axis_boundary: 15  # Maximum Y-axis displacement in blocks
    warning_distance: 20  # Distance in blocks for warning message
    attack_cooldown: 30  # Cooldown in seconds after special attack
```

## Testing Procedures

### 1. Unit Tests

Run the existing unit tests:
```bash
mvn test
```

### 2. Integration Tests

1. Start the server with the plugin installed
2. Engage Boss 1 in combat
3. Trigger the special attack multiple times
4. Verify the boss stays within Y-axis boundaries
5. Confirm warning messages appear in the correct language
6. Check that no console warnings about missing localization keys appear

### 3. Edge Case Testing

- Test with multiple players in the arena
- Test if boss takes damage during rising animation
- Test server restart during boss fight
- Test boundary conditions (boss at maximum Y-level of world)

## Verification Steps

### Before Implementation:
- [ ] Confirm Boss 1 exhibits infinite loop behavior
- [ ] Verify boss flies away uncontrollably on Y-axis
- [ ] Confirm "Missing localization key: boss1.special_attack.warning" in console

### After Implementation:
- [ ] Verify Boss 1 special attack completes without infinite loop
- [ ] Confirm boss stays within Y-axis boundaries (max 15 blocks above original)
- [ ] Verify warning message appears for players before special attack
- [ ] Confirm no "Missing localization key" warnings in console
- [ ] Test that boss returns to normal combat state after special attack

## Troubleshooting

### Issue: Boss still gets stuck in loop
**Solution**: Check that timeout mechanisms are properly implemented in BossRisingAnimation

### Issue: Y-axis boundary not enforced
**Solution**: Verify that original position is properly recorded and boundary calculations are correct

### Issue: Warning message not displayed
**Solution**: Confirm that the localization key is correctly added to message files and MessageManager properly handles the key

### Issue: Console warnings still appear
**Solution**: Verify that the localization key exists in all required language files (en, ru)

## Performance Considerations

- Position updates should complete within 5ms
- State transitions should complete within 10ms
- Message sending should complete within 20ms
- Monitor server TPS during boss fights to ensure performance impact is minimal

## Rollback Plan

If issues occur after deployment:
1. Revert to the previous stable version
2. Remove the feature branch changes
3. Deploy the stable version again