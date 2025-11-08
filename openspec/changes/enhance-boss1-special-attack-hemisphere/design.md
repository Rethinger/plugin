# enhance-boss1-special-attack-hemisphere - Technical Design

## Overview

Этот документ описывает техническую реализацию улучшенной специальной атаки Босса №1 с формированием полусферы из черепов и безопасными зонами.

## Architecture

### Component Structure

```
BossSpecialAttackManager (NEW)
├── HemisphereFormation (NEW)
├── SafeZoneManager (EXISTING - Enhanced)
├── ArrowHemisphereFormation (NEW)
├── SkullOrchestrator (ENHANCED)
└── AttackPhaseController (NEW)
```

### Integration Points

- **BossAttackState**: Расширен новыми фазами и проверкой вызова воинов
- **BossRisingAnimation**: Интеграция с фазой формирования
- **WitherSkullProjectile**: Enhanced логика подъема
- **SafeZoneManager**: Использование существующего класса для безопасных зон
- **Act2Listener**: Координация всей атаки с проверкой воинов
- **SpecialAttackConfiguration**: Расширен настройками для фаз 1 и 2

## New Classes

### HemisphereFormation

Управляет процессом формирования полусферы из черепов.

```java
public class HemisphereFormation {
    private final Location center;
    private final double radius;
    private final int skullCount;
    private final List<Location> skullPositions;
    private final List<WitherSkullProjectile> skulls;

    public void startFormation();
    public void updateFormation();
    public boolean isComplete();
    public List<Location> calculateSkullPositions();
    public void spawnSkullAtPosition(Location position);
}
```

### SafeZoneManager (EXISTING - Enhanced Integration)

Использует существующий `SafeZoneManager` и `SafeZone` классы.

**Existing Integration:**
```java
// Используем существующий функционал
SafeZoneManager manager = new SafeZoneManager(plugin);

// Создаем зоны с учетом требований (игроки + 1)
List<SafeZone> zones = manager.generateSafeZones(
    boss.getLocation(),      // center
    15.0,                    // maxRadius
    3.0,                     // safeZoneRadius (существующий)
    10,                      // durationSeconds
    playerCount + 1,         // minCount (новое требование)
    playerCount + 2          // maxCount
);

// Проверка попадания в безопасную зону
boolean inSafeZone = manager.isInSafeZone(player.getLocation());
```

**Enhancements Required:**
- Изменить логику создания зон с "random" на "player count + 1"
- Интеграция с фазами формирования полусферы
- Кастомизация времени появления зон

### ArrowHemisphereFormation (NEW)

Управляет формированием полусферы из стрел для фазы 2.

```java
public class ArrowHemisphereFormation {
    private final Location center;
    private final double radius;
    private final int arrowCount;
    private final List<Location> arrowPositions;
    private final List<ArrowProjectile> arrows;

    public void startFormation();
    public void updateFormation();
    public boolean isComplete();
    public List<Location> calculateArrowPositions();
    public void spawnArrowAtPosition(Location position);
}

public class ArrowProjectile {
    private final Arrow arrowEntity;
    private Location groundOrigin;
    private Location hemispherePosition;
    private double risingProgress;
    private boolean isRisingFromGround;

    public void startRisingFromGround(Location groundOrigin, Location targetPosition);
    private void updateRisingAnimation();
    private void setInstantDamage(int damage);
}
```

### BossSpecialAttackManager

Координирует всю специальную атаку с проверкой вызова воинов и заморозкой таймера.

```java
public class BossSpecialAttackManager {
    private final Skeleton boss;
    private final BossAttackState attackState;
    private final HemisphereFormation hemisphere;
    private final ArrowHemisphereFormation arrowHemisphere; // NEW
    private final SafeZoneManager safeZoneManager;
    private final SpecialAttackConfiguration config;

    // NEW: Warrior summon timer management
    private BukkitRunnable warriorSummonTask;
    private boolean warriorSummonPaused;

    public void startSpecialAttack();
    public void updateAttack();
    public boolean isAttackComplete();
    private void transitionPhase(BossAttackState.SpecialAttackPhase newPhase);

    // NEW: Warrior summon checking and timer management
    private boolean canStartSpecialAttack();
    private int getWarriorSummonWaveCount();
    private void pauseWarriorSummonTimer();
    private void resumeWarriorSummonTimer();
}
```

#### Warrior Summon Timer Management

```java
private void pauseWarriorSummonTimer() {
    // Cancel existing warrior summon task
    if (warriorSummonTask != null) {
        warriorSummonTask.cancel();
        warriorSummonTask = null;
    }
    warriorSummonPaused = true;
}

private void resumeWarriorSummonTimer() {
    if (warriorSummonPaused) {
        // Resume warrior summoning every 10 seconds (200L)
        warriorSummonTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Use existing warrior summon logic
                summonWarriorWave();
                recordSuccessfulWarriorSummon();
            }
        };
        warriorSummonTask.runTaskTimer(plugin, 200L, 200L); // Every 10 seconds
        warriorSummonPaused = false;
    }
}
```

## Enhanced Classes

### BossAttackState - New Phases

```java
public enum SpecialAttackPhase {
    NONE,                           // Нет спецатаки
    RISING_ANIMATION,              // Босс поднимается в воздух
    HEMISPHERE_FORMATION,          // Формирование полусферы (3 сек)
    SAFE_ZONES_APPEARING,          // Появление безопасных зон
    HEMISPHERE_ATTACK,             // Атака полусферой (босс в воздухе)
    POST_ATTACK_WAIT,              // Ожидание 1 сек после атаки
    GROUND_TOUCHDOWN,              // Спуск на землю
    COOLDOWN                       // Откат спецатаки
}
```

### WitherSkullProjectile - Rising Logic

Новая логика подъема черепов из земли:

```java
public class WitherSkullProjectile {
    // NEW: Rising from ground properties
    private Location groundOrigin;
    private Location hemispherePosition;
    private double risingProgress;
    private boolean isRisingFromGround;

    public void startRisingFromGround(Location groundOrigin, Location targetPosition);
    private void updateRisingAnimation();
    private void spawnRisingParticles();
}
```

## Phase Implementation Details

### Phase 1: RISING_ANIMATION (existing)

Использует существующую логику из `BossRisingAnimation`.

**Duration**: Based on `config.getRisingDuration()`
**Transition**: `HOVERING_WAIT`

### Phase 2: HOVERING_WAIT (new)

Босс зависает в воздухе перед формированием полусферы.

```java
private void executeHoveringWait() {
    // Boss hovers in air
    // Visual effects: anticipation particles
    // Sound effects: charging sounds

    if (hoveringTimer >= HOVERING_DURATION) {
        transitionPhase(SpecialAttackPhase.HEMISPHERE_FORMATION);
        startHemisphereFormation();
    }
}
```

**Duration**: 20 ticks (1 секунда)
**Visual Effects**: Anticipation particles around boss
**Sound Effects**: Low humming, charging sounds

### Phase 3: HEMISPHERE_FORMATION (new)

Формирование полусферы из черепов.

```java
private void startHemisphereFormation() {
    hemisphereFormation = new HemisphereFormation(
        boss.getLocation(),
        config.getHemisphereRadius(),
        config.getSkullCount()
    );
    hemisphereFormation.startFormation();
}

private void updateHemisphereFormation() {
    if (hemisphereFormation.isComplete()) {
        transitionPhase(SpecialAttackPhase.SAFE_ZONES_APPEARING);
        startSafeZoneCreation();
    }
}
```

**Duration**: 60 ticks (3 секунды)
**Skull Count**: 12-16 черепов
**Radius**: 8-10 блоков от центра

### Phase 4: SAFE_ZONES_APPEARING (new)

Постепенное появление безопасных зон.

```java
private void startSafeZoneCreation() {
    int playerCount = getNearbyPlayerCount();
    int safeZoneCount = playerCount + 1; // +1 as per requirement

    safeZoneManager = new SafeZoneManager(boss.getLocation(), config.getHemisphereRadius());
    safeZoneManager.createSafeZones(safeZoneCount);
}
```

**Safe Zone Count**: `players + 1`
**Safe Zone Size**: 3x3 blocks radius
**Appearance**: Sequential, 1 zone per 10 ticks

### Phase 5: FINAL_PREPARATION (new)

Финальная подготовка перед атакой.

```java
private void executeFinalPreparation() {
    // Boss charges up for attack
    // Safe zones fully visible
    // Skulls position for attack

    if (preparationTimer >= PREPARATION_DURATION) {
        transitionPhase(SpecialAttackPhase.HEMISPHERE_ATTACK);
        launchHemisphereAttack();
    }
}
```

**Duration**: 20 ticks (1 секунда)
**Visual Effects**: Flash, charging particles
**Sound Effects**: Explosion charging sound

### Phase 4: HEMISPHERE_ATTACK (new)

Запуск черепов в игроков пока босс остается в воздухе.

```java
private void launchHemisphereAttack() {
    List<Player> targets = getPlayersOutsideSafeZones();
    List<Location> attackTargets = WitherSkullProjectile.generateSpherePatternTargets(
        boss.getLocation(),
        targets,
        hemisphereFormation.getSkullCount()
    );

    hemisphereFormation.launchSkullsAtTargets(attackTargets);

    // Boss remains airborne during entire attack
    keepBossAirborne();
}
```

**Targeting**: Only players outside safe zones
**Attack Pattern**: Enhanced sphere pattern
**Damage**: Standard skull damage
**Boss State**: Remains airborne at hover height

### Phase 5: POST_ATTACK_WAIT (new)

Ожидание 1 секунду после завершения атаки перед спуском.

```java
private void executePostAttackWait() {
    // Boss hovers for 1 second after attack completes
    keepBossAirborne();

    if (postAttackTimer >= POST_ATTACK_DURATION) {
        transitionPhase(BossAttackState.SpecialAttackPhase.GROUND_TOUCHDOWN);
        startDescentToGround();
    }
}
```

**Duration**: 20 ticks (1 секунда)
**Boss State**: Airborne at hover height during wait
**Visual Effects**: Post-attack glow particles
**Sound Effects**: Descending wind sounds

### Phase 6: GROUND_TOUCHDOWN (new)

Плавный спуск босса на землю.

```java
private void startDescentToGround() {
    // Smooth descent animation back to ground
    Location groundPosition = originalBossLocation.clone();

    new BukkitRunnable() {
        @Override
        public void run() {
            if (descentTimer >= DESCENT_DURATION) {
                boss.teleport(groundPosition);
                transitionPhase(BossAttackState.SpecialAttackPhase.COOLDOWN);
                resumeWarriorSummonTimer();
                cancel();
                return;
            }

            // Smooth interpolation to ground
            double progress = (double) descentTimer / DESCENT_DURATION;
            Location currentPos = interpolatePosition(boss.getLocation(), groundPosition, progress);
            boss.teleport(currentPos);

            descentTimer++;
        }
    }.runTaskTimer(plugin, 0L, 1L);
}
```

**Duration**: 40 ticks (2 секунды) for smooth descent
**Boss State**: Descending from hover height to ground
**Visual Effects**: Descending trail particles
**Descent**: Smooth interpolation to original position

## Performance Considerations

### Particle Optimization

- Ограничение частиц в зависимости от количества игроков
- Использование `Particle.DENSITY_OPTIONS` для оптимизации
- Пакетная обработка частиц

```java
private void spawnOptimizedParticles(Location location, int count) {
    int playerCount = getNearbyPlayerCount();
    int optimizedCount = Math.min(count, playerCount * 10);

    location.getWorld().spawnParticle(
        Particle.SOUL_FIRE_FLAME,
        location,
        optimizedCount,
        0.5, 0.5, 0.5, 0.1
    );
}
```

### Task Management

Все задачи используют `BukkitRunnable` с правильной очисткой:

```java
public void cleanup() {
    if (hemisphereFormation != null) {
        hemisphereFormation.cleanup();
    }
    if (safeZoneManager != null) {
        safeZoneManager.cleanup();
    }
    if (attackTask != null) {
        attackTask.cancel();
    }
}
```

### Memory Management

- Использование WeakReference для сущностей
- Регулярная очистка неиспользуемых объектов
- Проверка валидности сущностей перед использованием

## Configuration

### New Configuration Options

```yaml
boss1:
  special_attack:
    hemisphere:
      radius: 8.0
      skull_count: 14
      formation_duration: 60 # ticks
      safe_zone:
        radius: 1.5
        player_count_multiplier: 1.0
        appearance_interval: 10 # ticks
      phases:
        hovering_duration: 20 # ticks
        preparation_duration: 20 # ticks
```

## Error Handling

### Common Failure Scenarios

1. **Boss dies during attack**: Graceful cleanup of all entities
2. **Player disconnects**: Adjust safe zones and targeting
3. **Chunk unloading**: Pause attack until chunk loads
4. **Performance issues**: Automatic particle reduction

```java
private void handleAttackFailure(Exception e) {
    plugin.getLogger().warning("Boss special attack failed: " + e.getMessage());
    cleanup();
    attackState.setSpecialAttackPhase(SpecialAttackPhase.COOLDOWN);

    // Notify players of attack cancellation
    broadcastMessage("boss.attack.cancelled");
}
```

## Testing Strategy

### Unit Tests

- `HemisphereFormationTest` - Test hemisphere creation logic
- `SafeZoneManagerTest` - Test safe zone positioning and detection
- `BossSpecialAttackManagerTest` - Test phase transitions

### Integration Tests

- Full attack sequence with multiple players
- Performance tests with varying player counts
- Edge cases (player death, disconnect, etc.)

### Manual Testing Checklist

- [ ] Visual quality of hemisphere formation
- [ ] Safe zone visibility and accuracy
- [ ] Attack damage distribution
- [ ] Performance under load
- [ ] Recovery from error conditions

## Future Enhancements

### Potential Extensions

1. **Dynamic difficulty**: Adjust parameters based on player count/skill
2. **Environmental interaction**: Use terrain in attack patterns
3. **Multiple attack patterns**: Randomized attack sequences
4. **Player feedback system**: Score/rating based on performance

### Configuration Extensibility

```java
public interface SpecialAttackPattern {
    String getName();
    void execute(Skeleton boss, BossAttackState state);
    boolean isApplicable(int playerCount, Environment environment);
}
```

This design provides a robust foundation for implementing the enhanced boss special attack while maintaining performance and extensibility.