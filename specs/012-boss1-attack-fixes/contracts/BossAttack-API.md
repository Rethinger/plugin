# Boss Attack API Contract

## Overview
This document defines the API contract for Boss 1 attack mechanics, specifically the modified special attack behavior that includes rising animation, skull projectiles in sphere pattern, and removed unwanted behaviors.

## Endpoints

### Boss Special Attack Configuration
```
GET /api/boss/{bossId}/special-attack/config
```
Retrieve configuration parameters for special attack behavior.

**Response**:
```json
{
  "bossId": "string",
  "projectileCount": 32,
  "riseHeight": 12.0,
  "sphereRadius": 15.0,
  "risingDuration": 40,
  "attackDuration": 100,
  "hasStunBeforeCast": false,
  "spawnsSkeletonsDuringCast": false,
  "particleEffectType": "SOUL_FIRE_FLAME,END_ROD"
}
```

### Trigger Boss Special Attack
```
POST /api/boss/{bossId}/special-attack/trigger
```
Trigger the special attack sequence with new mechanics.

**Request Body**:
```json
{
  "bossId": "string",
  "arenaCenter": {
    "x": 0.0,
    "y": 0.0,
    "z": 0.0,
    "world": "world_name"
  },
  "playerPositions": [
    {
      "playerId": "uuid",
      "x": 0.0,
      "y": 0.0,
      "z": 0.0
    }
  ],
  "safeZones": [
    {
      "id": "string",
      "center": {
        "x": 0.0,
        "y": 0.0,
        "z": 0.0
      },
      "radius": 5.0
    }
  ]
}
```

**Response**:
```json
{
  "attackId": "string",
  "status": "STARTED",
  "bossRising": true,
  "projectilesSpawned": 32,
  "risingHeight": 12.0,
  "particlesEnabled": true
}
```

### Update Boss Special Attack Configuration
```
PUT /api/boss/{bossId}/special-attack/config
```
Update special attack configuration parameters.

**Request Body**:
```json
{
  "projectileCount": 36,
  "riseHeight": 15.0,
  "sphereRadius": 20.0,
  "risingDuration": 60,
  "hasStunBeforeCast": false,
  "spawnsSkeletonsDuringCast": false,
  "particleEffectType": "SOUL_FIRE_FLAME,END_ROD"
}
```

**Response**:
```json
{
  "success": true,
  "updatedConfig": {
    "bossId": "string",
    "projectileCount": 36,
    "riseHeight": 15.0,
    "sphereRadius": 20.0,
    "risingDuration": 60,
    "hasStunBeforeCast": false,
    "spawnsSkeletonsDuringCast": false,
    "particleEffectType": "SOUL_FIRE_FLAME,END_ROD"
  }
}
```

## Data Models

### BossAttackState
```json
{
  "stateId": "string",
  "bossId": "string",
  "currentState": "NORMAL_COMBAT|SPECIAL_ATTACK_CASTING|SPECIAL_ATTACK_ACTIVE|DEFEATED",
  "timestamp": "ISO8601",
  "specialAttackActive": false,
  "skeletonSpawningDisabled": true,
  "stunEffectDisabled": true
}
```

### ProjectileData
```json
{
  "id": "string",
  "type": "WITHER_SKULL",
  "origin": {
    "x": 0.0,
    "y": 0.0,
    "z": 0.0,
    "world": "world_name"
 },
  "target": {
    "x": 0.0,
    "y": 0.0,
    "z": 0.0,
    "world": "world_name"
  },
  "isRisingPhase": true,
  "isAttackPhase": false,
  "damage": 10.0,
  "velocity": {
    "x": 0.0,
    "y": 0.0,
    "z": 0.0
  }
}
```

### SafeZoneData
```json
{
  "id": "string",
  "center": {
    "x": 0.0,
    "y": 0.0,
    "z": 0.0
  },
  "radius": 5.0,
  "isActive": true,
  "hasVerticalBeacon": false,
  "playersProtected": ["player_uuid1", "player_uuid2"]
}
```

## Error Responses

### 400 Bad Request
```json
{
  "error": "INVALID_ATTACK_STATE",
  "message": "Boss is not in a valid state to perform special attack"
}
```

### 404 Not Found
```json
{
  "error": "BOSS_NOT_FOUND",
  "message": "Specified boss ID does not exist"
}
```

### 429 Too Many Requests
```json
{
  "error": "ATTACK_ON_COOLDOWN",
  "message": "Special attack is still on cooldown"
}
```

## Authentication
All endpoints require authentication with server operator privileges for configuration changes. Game mechanics endpoints are accessible during gameplay without additional authentication.

## Rate Limits
- Configuration endpoints: 10 requests per minute per admin
- Game mechanics endpoints: No rate limit during active combat