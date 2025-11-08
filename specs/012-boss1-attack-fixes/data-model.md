# Data Model: Boss 1 Attack Fixes

## Entities

### Boss 1 (Skeleton Lord)
- **Type**: Game Entity (extends Minecraft Skeleton)
- **Fields**:
  - `id: String` - Unique identifier for the boss
  - `currentHealth: double` - Current health value
  - `maxHealth: double` - Maximum health value
  - `position: Location` - Current world position (x, y, z, world)
  - `attackState: BossAttackState` - Current combat state (normal, special-attack-casting, etc.)
  - `isFleeingFromWolves: boolean` - Whether boss currently avoids wolves (to be set to false)
  - `specialAttackCooldown: long` - Time until next special attack available
  - `risingHeight: double` - Height to rise during special attack (10-15 blocks)
  - `hasStunEffect: boolean` - Whether to apply stun before special attack (to be set to false)

### Safe Zone
- **Type**: Game Area (existing entity with modified behavior)
- **Fields**:
  - `id: String` - Unique identifier for the safe zone
  - `center: Location` - Center position of the safe zone
  - `radius: double` - Radius of the safe zone
  - `isActive: boolean` - Whether the safe zone is currently active
  - `hasVerticalBeacon: boolean` - Whether to display vertical beacon effect (to be set to false during special attack)
  - `playersProtected: List<Player>` - Players currently within safe zone boundaries

### Skeleton Warrior (Summoned Minion)
- **Type**: Game Entity (extends Minecraft Skeleton)
- **Fields**:
  - `id: String` - Unique identifier for the minion
  - `bossId: String` - Reference to summoning boss
  - `currentHealth: double` - Current health value
  - `position: Location` - Current world position (x, y, z, world)
 - `isFleeingFromWolves: boolean` - Whether minion currently avoids wolves (to be set to false)
  - `summonTime: long` - Time when minion was summoned
  - `despawnTime: long` - Time when minion should despawn

### Wither Skull Projectile
- **Type**: Game Projectile
- **Fields**:
  - `id: String` - Unique identifier for the projectile
  - `origin: Location` - Starting position at ground level
  - `target: Location` - Target position (boss location during rising phase)
  - `finalTarget: Location` - Final target position (player location during attack phase)
  - `isRisingPhase: boolean` - Whether currently moving to boss position
  - `isAttackPhase: boolean` - Whether currently moving to final target
  - `damage: double` - Damage dealt on impact
  - `velocity: Vector` - Current movement vector
  - `creationTime: long` - Time when projectile was created

### Special Attack Configuration
- **Type**: Configuration Object
- **Fields**:
 - `projectileCount: int` - Number of skull projectiles (32-40)
  - `riseHeight: double` - Height boss rises during attack (10-15 blocks)
  - `sphereRadius: double` - Radius of sphere pattern for projectiles
  - `risingDuration: int` - Duration of rising phase in ticks
  - `attackDuration: int` - Duration of attack phase in ticks
 - `hasStunBeforeCast: boolean` - Whether to apply stun before casting (false)
 - `spawnsSkeletonsDuringCast: boolean` - Whether to spawn skeletons during cast (false)
  - `particleEffectType: String` - Type of particles to use (SOUL_FIRE_FLAME, END_ROD)

## Relationships

### Boss 1 → Skeleton Warrior
- **Relationship**: One-to-Many (one boss can summon multiple warriors)
- **Description**: Boss 1 summons Skeleton Warrior minions during combat
- **Constraint**: Minions despawn when boss is defeated

### Safe Zone → Player
- **Relationship**: Many-to-Many (players can be in multiple zones over time, zones can contain multiple players)
- **Description**: Players can be within safe zone boundaries for protection
- **Constraint**: Players in safe zones are immune to special attack damage

### Boss 1 → Wither Skull Projectile
- **Relationship**: One-to-Many (one boss can create multiple projectiles during special attack)
- **Description**: Boss 1 creates and controls skull projectiles during special attack
- **Constraint**: Projectiles are destroyed if boss is defeated

### Special Attack Configuration → Boss 1
- **Relationship**: One-to-One (each boss has one special attack configuration)
- **Description**: Configuration parameters control boss special attack behavior
- **Constraint**: Configuration is specific to Boss 1

## State Transitions

### BossAttackState
- **NORMAL_COMBAT**: Standard combat behavior
  - Can transition to SPECIAL_ATTACK_CASTING
- **SPECIAL_ATTACK_CASTING**: Casting special attack (with rising animation)
  - Can transition to SPECIAL_ATTACK_ACTIVE
  - No longer includes stun phase
 - No longer spawns skeleton warriors during this phase
- **SPECIAL_ATTACK_ACTIVE**: Special attack projectiles active
  - Can transition back to NORMAL_COMBAT
- **DEFEATED**: Boss has been defeated
  - Final state, no transitions out

### SafeZoneState
- **INACTIVE**: Safe zone not currently active
  - Can transition to ACTIVE
- **ACTIVE**: Safe zone currently active, providing protection
  - No beacon visual effect during special attack
  - Can transition to INACTIVE