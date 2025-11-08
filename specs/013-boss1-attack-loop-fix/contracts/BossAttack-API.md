# Boss Attack API Contract: Boss 1 Attack Loop Fix

## Overview

This API contract defines the interfaces and methods for managing Boss 1 attack behavior, specifically addressing the infinite loop and Y-axis boundary issues. The contract ensures proper state management and boundary enforcement during special attacks.

## Message Manager API

### Interface: MessageManager

#### Method: sendMessageToPlayersNearBoss
```
void sendMessageToPlayersNearBoss(
  Location bossLocation,
  double radius,
  String localizationKey,
  Player... excludedPlayers
)
```

**Purpose**: Sends localized messages to players near the boss within a specified radius

**Parameters**:
- bossLocation: Location of the boss entity
- radius: Distance in blocks to search for nearby players
- localizationKey: Key for the localized message to send
- excludedPlayers: Optional list of players to exclude from the message

**Pre-conditions**:
- bossLocation must be valid
- radius must be positive
- localizationKey must exist in message files

**Post-conditions**:
- All players within radius receive the localized message
- No console warnings about missing localization keys

**Error handling**:
- Throws MissingLocalizationKeyException if localizationKey doesn't exist
- Logs warning if bossLocation is invalid

### Method: hasLocalizationKey
```
boolean hasLocalizationKey(String key)
```

**Purpose**: Checks if a localization key exists in the message files

**Parameters**:
- key: The localization key to check

**Returns**: True if the key exists, false otherwise

**Pre-conditions**: None

**Post-conditions**: Returns boolean result of key existence check

## Boss Attack State Management API

### Interface: BossAttackStateManager

#### Method: startSpecialAttack
```
boolean startSpecialAttack(
  UUID bossEntityId,
  Location originalPosition,
  SpecialAttackConfiguration config
)
```

**Purpose**: Initiates the special attack sequence for a boss with proper state tracking

**Parameters**:
- bossEntityId: Unique identifier of the boss entity
- originalPosition: Position where boss started the special attack
- config: Configuration for the special attack behavior

**Returns**: True if special attack started successfully, false if already in progress

**Pre-conditions**:
- Boss entity must exist and be valid
- originalPosition must be a valid location
- config must not be null

**Post-conditions**:
- Boss enters SPECIAL_ATTACK_RISING state
- Original position is recorded for boundary tracking
- Rising timer starts
- Warning message is scheduled to be sent

**Error handling**:
- Returns false if boss is already in special attack state
- Logs error if boss entity doesn't exist

#### Method: updateBossPosition
```
BossPositionStatus updateBossPosition(
 UUID bossEntityId,
  Location newPosition
)
```

**Purpose**: Updates the boss position during special attack and enforces Y-axis boundaries

**Parameters**:
- bossEntityId: Unique identifier of the boss entity
- newPosition: New location of the boss

**Returns**: Status indicating position validity and any required actions

**Pre-conditions**:
- bossEntityId must correspond to a boss in special attack state
- newPosition must be a valid location

**Post-conditions**:
- Boss position is updated in the system
- Boundary checks are performed
- State may be transitioned if boundaries are exceeded

**Return Values**:
- VALID_POSITION: Position is within acceptable bounds
- BOUNDARY_EXCEEDED: Position exceeds Y-axis boundary, force state transition
- INVALID_STATE: Boss is not in a state where position should be updated

#### Method: completeSpecialAttack
```
void completeSpecialAttack(UUID bossEntityId)
```

**Purpose**: Completes the special attack sequence and returns boss to normal combat state

**Parameters**:
- bossEntityId: Unique identifier of the boss entity

**Pre-conditions**:
- Boss must be in special attack state
- All special attack effects must be properly cleaned up

**Post-conditions**:
- Boss transitions to SPECIAL_ATTACK_COOLDOWN state
- Rising animation stops
- All special attack state is cleaned up

**Error handling**:
- Logs warning if boss is not in special attack state

## Special Attack Configuration API

### Interface: SpecialAttackConfiguration

#### Properties:
- **maxRisingTime**: Maximum time in seconds for the rising animation (default: 8 seconds)
- **yAxisBoundaryOffset**: Maximum Y-axis displacement from original position (default: 15 blocks)
- **warningMessageKey**: Localization key for the special attack warning (default: "boss1.special_attack.warning")
- **warningDistance**: Distance in blocks for warning message radius (default: 20 blocks)
- **attackCooldown**: Cooldown time after special attack completes (default: 30 seconds)

#### Method: validateConfiguration
```
List<ValidationError> validateConfiguration()
```

**Purpose**: Validates that the configuration values are within acceptable ranges

**Returns**: List of validation errors, empty if configuration is valid

**Validation Rules**:
- maxRisingTime must be between 5-15 seconds
- yAxisBoundaryOffset must be between 10-20 blocks
- warningMessageKey must exist in localization files
- warningDistance must be positive
- attackCooldown must be greater than 0

## Data Transfer Objects

### DTO: BossPositionStatus
```
class BossPositionStatus {
  PositionStatusType statusType;
  String message;
  Optional<Location> recommendedPosition;
}
```

**PositionStatusType Enum**:
- VALID_POSITION: Position is acceptable
- BOUNDARY_EXCEEDED: Y-axis boundary exceeded
- POSITION_ERROR: Other position-related error

### DTO: SpecialAttackConfiguration
```
class SpecialAttackConfiguration {
  int maxRisingTime;
  double yAxisBoundaryOffset;
  String warningMessageKey;
  double warningDistance;
  int attackCooldown;
}
```

## Error Handling

### Exceptions
- **MissingLocalizationKeyException**: Thrown when a localization key doesn't exist
- **InvalidBossStateException**: Thrown when attempting operations on a boss in an invalid state
- **BoundaryExceededException**: Thrown when boss position exceeds allowed boundaries

### Logging
All API methods must log:
- Entry and exit for debugging purposes
- Any validation errors
- State transitions for audit purposes
- Performance metrics for optimization

## Performance Requirements
- Position updates must complete within 5ms
- State transitions must complete within 10ms
- Message sending must complete within 20ms
- No blocking operations during combat

## Security Considerations
- Only authorized game systems may call these methods
- Input validation required for all parameters
- Prevent manipulation of boss states by unauthorized code