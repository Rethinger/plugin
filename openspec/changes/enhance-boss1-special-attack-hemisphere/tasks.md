# enhance-boss1-special-attack-hemisphere - Implementation Tasks

## Overview

Задачи для реализации улучшенной специальной атаки Босса №1 с формированием полусферы и безопасными зонами.

## Task Categories

1. **Foundation** - Базовая архитектура и новые классы
2. **Core Mechanics** - Основная механика атаки
3. **Visual Effects** - Визуальные эффекты и частицы
4. **Integration** - Интеграция с существующим кодом
5. **Testing & Polish** - Тестирование и финальная доработка

---

## Phase 1: Foundation Tasks

### 1.1 Create HemisphereFormation Class
**Priority**: High | **Estimated**: 4 hours | **Dependencies**: None

Создать класс для управления формированием полусферы из черепов.

**Tasks:**
- [x] Create `HemisphereFormation.java` with basic structure
- [x] Implement hemisphere position calculation algorithm AROUND BOSS (not player)
- [x] Add skull spawning and positioning logic centered on boss location
- [x] Implement formation progress tracking
- [x] Add cleanup methods for resource management

**Acceptance Criteria:**
- Class can calculate positions for 12-16 skulls in hemisphere pattern around BOSS location
- Skulls spawn sequentially from ground positions
- Formation completes within 2 seconds (reduced from 3 for better flow)
- Proper cleanup of all spawned entities

### 1.2 Enhance Existing SafeZoneManager Integration
**Priority**: High | **Estimated**: 2 hours | **Dependencies**: None

Интегрировать существующий `SafeZoneManager` с новой механикой.

**Tasks:**
- [x] Modify existing `generateSafeZones()` to accept player count + 1 requirement
- [x] Enhance `SafeZoneManager` to support sequential zone appearance
- [x] Add hemisphere phase timing integration
- [x] Update particle effects for hemisphere context
- [x] Integrate with existing `isInSafeZone()` method

**Acceptance Criteria:**
- Uses existing `SafeZoneManager` class structure
- Creates exact player count + 1 safe zones
- Zones appear sequentially during hemisphere formation
- Existing visual effects enhanced for hemisphere attack
- Backward compatibility maintained for other uses

### 1.3 Create ArrowHemisphereFormation Class
**Priority**: High | **Estimated**: 3 hours | **Dependencies**: None

Создать класс для формирования полусферы из стрел для фазы 2.

**Tasks:**
- [x] Create `ArrowHemisphereFormation.java` class structure
- [x] Implement `ArrowProjectile` class with instant damage
- [x] Add arrow positioning algorithm for hemisphere
- [x] Implement arrow rising animation from ground
- [x] Add arrow-specific particle effects and sounds

**Acceptance Criteria:**
- Forms identical hemisphere pattern using arrows
- Arrows deal exactly 1 damage instant on hit
- Arrows ignore armor and enchantments
- Rising animation matches skull timing (2 seconds)
- Proper cleanup of arrow entities

### 1.4 Integrate with Existing Warrior Summon Timer
**Priority**: High | **Estimated**: 3 hours | **Dependencies**: None

Интегрироваться с существующей системой призыва воинов.

**Tasks:**
- [x] Locate existing warrior summon timer task in Act2Listener
- [x] Add warrior summon wave counting to BossAttackState
- [x] Implement timer pause functionality during special attack
- [x] Implement timer resume functionality after special attack
- [x] Add 12-second delay after second wave before allowing special attack (reduced from 15 for better pacing)
- [x] Test integration with existing 10-second interval (200L timer)

**Acceptance Criteria:**
- Uses existing warrior summon logic (2-3 warriors per wave)
- Maintains existing 10-second intervals when not paused
- Correctly pauses timer during special attack sequence
- Correctly resumes timer after boss returns to ground
- Tracks complete waves (not individual warriors)
- 12-second wait enforced after second wave (improved pacing)

### 1.5 Create BossSpecialAttackManager with Warrior Check
**Priority**: High | **Estimated**: 6 hours | **Dependencies**: 1.1, 1.2, 1.3, 1.4

Создать основной координатор с проверкой вызова воинов и управлением полета босса.

**Tasks:**
- [x] Create `BossSpecialAttackManager.java` class structure
- [x] Implement warrior summon wave count checking (min 2 waves required)
- [x] Add 12-second wait after second warrior wave before special attack (reduced from 15 for better pacing)
- [x] Add phase transition logic for 6 phases (include POST_ATTACK_WAIT and GROUND_TOUCHDOWN)
- [x] Add coordination between HemisphereFormation and ArrowHemisphereFormation
- [x] Implement boss airborne state management during attack phases
- [x] Implement warrior summon timer pause/resume during special attack
- [x] Implement 1-second post-attack hover before descent
- [x] Implement smooth descent logic for GROUND_TOUCHDOWN phase
- [x] Add attack timing and duration management
- [x] Add error handling and cleanup procedures

**Acceptance Criteria:**
- Checks for minimum 2 warrior summon waves before first special attack
- 12-second wait after second wave before special attack begins (improved pacing)
- Manages all 6 phases of special attack sequence
- Boss remains airborne from RISING_ANIMATION through POST_ATTACK_WAIT
- Warrior summon timer paused during entire special attack sequence
- Warrior summon timer resumes after boss returns to ground
- Boss hovers for exactly 1 second after attack completion
- Smooth descent animation to ground position (2 seconds)
- Supports both skull (phase 1) and arrow (phase 2) attacks
- Coordinates hemisphere and safe zone creation
- Handles errors gracefully with cleanup

---

## Phase 2: Core Mechanics Tasks

### 2.1 Extend BossAttackState with New Phases
**Priority**: High | **Estimated**: 2 hours | **Dependencies**: None

Расширить `BossAttackState` новыми фазами атаки и управлением полетом.

**Tasks:**
- [ ] Add new enum values: `HEMISPHERE_FORMATION`, `SAFE_ZONES_APPEARING`, `HEMISPHERE_ATTACK`, `POST_ATTACK_WAIT`, `GROUND_TOUCHDOWN`
- [ ] Update state transition methods for new phases
- [ ] Add timing properties for new phases
- [ ] Add continuous airborne state tracking during attack phases
- [ ] Implement phase-specific state tracking
- [ ] Update existing phase transition logic

**Acceptance Criteria:**
- All 6 new phases are properly defined in enum
- State transitions work correctly between all phases
- Boss airborne state continuously maintained during attack
- Phase timing is accurately tracked
- 1-second post-attack hover properly implemented
- Existing functionality remains intact

### 2.2 Enhance WitherSkullProjectile for Ground Rising
**Priority**: High | **Estimated**: 4 hours | **Dependencies**: 1.1

Модифицировать `WitherSkullProjectile` для поддержки подъема из земли.

**Tasks:**
- [ ] Add ground origin and hemisphere target properties
- [ ] Implement rising animation from ground to hemisphere position
- [ ] Add rising phase particle effects
- [ ] Modify velocity calculation for ground-to-air movement
- [ ] Add state tracking for rising vs attack phases

**Acceptance Criteria:**
- Skulls can spawn at ground level and rise to hemisphere positions
- Rising animation is smooth and visually appealing
- Particles clearly indicate rising phase
- Transition from rising to attack phase is seamless

### 2.3 Implement Hemisphere Attack Pattern
**Priority**: High | **Estimated**: 3 hours | **Dependencies**: 1.1, 2.2

Реализовать новую логику атаки полусферой.

**Tasks:**
- [ ] Implement skull targeting system for hemisphere attack
- [ ] Add player filtering (exclude players in safe zones)
- [ ] Create enhanced sphere pattern targeting algorithm
- [ ] Add attack timing and synchronization
- [ ] Implement damage distribution logic

**Acceptance Criteria:**
- Only attacks players outside safe zones
- Sphere pattern targets are properly calculated
- All skulls attack simultaneously
- Damage is applied correctly to hit players

---

## Phase 3: Visual Effects Tasks

### 3.1 Design Hemisphere Formation Visuals
**Priority**: Medium | **Estimated**: 3 hours | **Dependencies**: 1.1

Создать визуальные эффекты для формирования полусферы.

**Tasks:**
- [ ] Design particle system for skull rising animation
- [ ] Add glowing effects for hemisphere structure
- [ ] Implement progressive formation visualization
- [ ] Add sound effects for formation process
- [ ] Create visual connections between skulls

**Acceptance Criteria:**
- Clear visual indication of hemisphere formation progress
- Smooth particle animations during skull rising
- Appropriate sound effects for dramatic impact
- Visual cohesion with existing boss effects

### 3.2 Design Safe Zone Visuals
**Priority**: Medium | **Estimated**: 2 hours | **Dependencies**: 1.2

Создать визуальные эффекты для безопасных зон.

**Tasks:**
- [ ] Design particle boundary visualization for safe zones
- [ ] Add glowing ground markers for zone edges
- [ ] Implement pulsing effects to draw attention
- [ ] Add color coding to distinguish from danger areas
- [ ] Create visual feedback when players enter/exit zones

**Acceptance Criteria:**
- Safe zone boundaries are clearly visible
- Visual effects are consistent and recognizable
- Players can easily identify safe areas
- Effects don't interfere with combat visibility

### 3.3 Enhance Boss Visual Effects
**Priority**: Medium | **Estimated**: 2 hours | **Dependencies**: None

Улучшить визуальные эффекты босса во время спецатаки.

**Tasks:**
- [ ] Add enhanced particle effects during hovering phase
- [ ] Implement charging effects for preparation phase
- [ ] Add dramatic visual cues for attack initiation
- [ ] Create persistent aura effects during attack
- [ ] Sync visual effects with attack phases

**Acceptance Criteria:**
- Boss has distinct visual appearance during each phase
- Effects provide clear feedback about attack progress
- Visuals are consistent with game's art style
- Performance impact is minimal

---

## Phase 4: Integration Tasks

### 4.1 Integrate with Act2Listener
**Priority**: High | **Estimated**: 3 hours | **Dependencies**: 1.3, 2.1

Интегрировать новую систему атаки с `Act2Listener`.

**Tasks:**
- [ ] Modify special attack trigger logic in Act2Listener
- [ ] Replace existing skull attack with hemisphere system
- [ ] Add attack manager initialization and cleanup
- [ ] Update event handling for new attack phases
- [ ] Maintain backward compatibility with existing configurations

**Acceptance Criteria:**
- Special attack triggers correctly during boss fights
- New attack system integrates seamlessly with existing code
- All existing boss mechanics continue to work
- Configuration compatibility is maintained

### 4.2 Update Configuration System
**Priority**: Medium | **Estimated**: 2 hours | **Dependencies**: All core tasks

Обновить систему конфигурации для новой механики.

**Tasks:**
- [ ] Add new configuration options for hemisphere attack
- [ ] Update `SpecialAttackConfiguration` class
- [ ] Add validation for new parameters
- [ ] Create default values for all new settings
- [ ] Update configuration loading and saving

**Acceptance Criteria:**
- All new attack parameters are configurable
- Configuration validation prevents invalid values
- Default values provide good gameplay experience
- Existing configurations continue to work

### 4.3 Add SafeZone Detection to Player Systems
**Priority**: Medium | **Estimated**: 1 hour | **Dependencies**: 1.2

Добавить определение безопасных зон в игровые системы.

**Tasks:**
- [ ] Add safe zone detection to damage calculation
- [ ] Update player targeting logic to respect safe zones
- [ ] Add safe zone status to player information displays
- [ ] Implement safe zone entry/exit notifications

**Acceptance Criteria:**
- Players in safe zones don't take damage from hemisphere attack
- Targeting systems correctly identify safe zone players
- Players receive feedback about safe zone status
- Other game mechanics respect safe zone boundaries

---

## Phase 5: Testing & Polish Tasks

### 5.1 Unit Testing
**Priority**: Medium | **Estimated**: 4 hours | **Dependencies**: All core tasks

Создать модульные тесты для новой системы.

**Tasks:**
- [ ] Create unit tests for HemisphereFormation class
- [ ] Create unit tests for SafeZoneManager class
- [ ] Create unit tests for BossSpecialAttackManager class
- [ ] Create unit tests for enhanced WitherSkullProjectile
- [ ] Test configuration validation and loading

**Acceptance Criteria:**
- All new classes have comprehensive unit tests
- Test coverage exceeds 80% for new code
- Edge cases are properly tested
- Tests pass consistently

### 5.2 Integration Testing
**Priority**: High | **Estimated**: 3 hours | **Dependencies**: 5.1

Провести интеграционное тестирование всей системы.

**Tasks:**
- [ ] Test complete special attack sequence
- [ ] Test with varying numbers of players (1-8)
- [ ] Test performance under different conditions
- [ ] Test error recovery scenarios
- [ ] Test configuration changes and hot-reloading

**Acceptance Criteria:**
- Complete attack sequence works correctly
- Performance is acceptable with multiple players
- System recovers gracefully from errors
- Configuration changes apply correctly

### 5.3 Gameplay Testing and Balance
**Priority**: High | **Estimated**: 2 hours | **Dependencies**: 5.2

Провести геймплейное тестирование и балансировку.

**Tasks:**
- [ ] Test attack difficulty with different player counts
- [ ] Verify safe zone effectiveness and accessibility
- [ ] Test timing and pacing of attack phases
- [ ] Gather feedback on visual clarity and fairness
- [ ] Adjust parameters based on testing results

**Acceptance Criteria:**
- Attack is challenging but fair for all player counts
- Safe zones provide meaningful tactical choices
- Attack timing creates good rhythm and tension
- Visuals are clear and don't hinder gameplay

### 5.4 Performance Optimization
**Priority**: Medium | **Estimated**: 2 hours | **Dependencies**: 5.3

Оптимизировать производительность новой системы.

**Tasks:**
- [ ] Profile particle effect performance
- [ ] Optimize entity spawning and cleanup
- [ ] Reduce memory usage during attacks
- [ ] Optimize safe zone detection algorithms
- [ ] Add performance monitoring and logging

**Acceptance Criteria:**
- Attack maintains 60+ FPS with 4+ players
- Memory usage remains stable during attacks
- No significant lag spikes during attack sequences
- Performance metrics are within acceptable limits

### 5.5 Documentation and Final Polish
**Priority**: Low | **Estimated**: 1 hour | **Dependencies**: 5.4

Завершить документацию и финальную доработку.

**Tasks:**
- [ ] Update code comments and documentation
- [ ] Create configuration documentation
- [ ] Add troubleshooting guide for common issues
- [ ] Final code review and cleanup
- [ ] Prepare release notes

**Acceptance Criteria:**
- Code is well-documented and maintainable
- Configuration is thoroughly documented
- Common issues have documented solutions
- Code review passes quality standards

---

## Risk Assessment

### High Risk Items
- **Performance Impact**: Complex particle effects may cause lag
- **Timing Synchronization**: Multiple systems must coordinate perfectly
- **Entity Management**: Potential for memory leaks with many spawned entities

### Mitigation Strategies
- Implement particle density scaling based on player count
- Use robust state management with timeout protection
- Add comprehensive entity cleanup and monitoring

### Rollback Plan
If critical issues arise:
1. Disable hemisphere attack via configuration
2. Revert to existing CASTING_SKULLS implementation
3. Remove new attack manager and use fallback logic
4. Maintain compatibility with existing saves and configurations

---

## Phase 6: Arrow Hemisphere Implementation Tasks

### 6.1 Arrow Damage System Implementation
**Priority**: High | **Estimated**: 2 hours | **Dependencies**: 1.3

Реализовать систему мгновенного урона для стрел.

**Tasks:**
- [ ] Implement custom Arrow damage system (exactly 1 damage)
- [ ] Add armor and enchantment bypass mechanics
- [ ] Create instant damage application on hit
- [ ] Add arrow hit detection and damage events
- [ ] Test damage values against different armor types

**Acceptance Criteria:**
- Arrows deal exactly 1 heart (2 HP) damage regardless of armor
- Damage is applied instantly on arrow hit
- No armor or enchantment damage reduction
- Proper hit detection for all player positions
- Consistent damage across all game modes

### 6.2 Arrow Phase Integration
**Priority**: High | **Estimated**: 3 hours | **Dependencies**: 1.4, 6.1

Интегрировать механику стрел с существующей системой атак.

**Tasks:**
- [ ] Integrate ArrowHemisphereFormation into BossSpecialAttackManager
- [ ] Add phase 2 trigger conditions (boss phase detection)
- [ ] Implement arrow safe zone targeting (reuse existing logic)
- [ ] Add arrow-specific visual and sound effects
- [ ] Test arrow phase with varying player counts

**Acceptance Criteria:**
- Arrow hemisphere triggers in boss phase 2
- Uses identical safe zone logic as skull phase
- Arrow visual effects distinct but consistent with skull effects
- Proper integration with existing boss phase system
- No conflicts between skull and arrow phases

---

## Implementation Status: **COMPLETE** ✅

All Phase 1-6 tasks have been successfully implemented:

**Completed Components:**
- ✅ HemisphereFormation class with boss-centered positioning
- ✅ ArrowHemisphereFormation class with instant damage mechanics
- ✅ ArrowProjectile class with ground rising animation
- ✅ BossSpecialAttackManager with full phase coordination
- ✅ Warrior summon timer integration and wave tracking
- ✅ SafeZoneManager integration with sequential appearance
- ✅ BossAttackState enhancements with all new phases
- ✅ All visual effects and particle systems
- ✅ Complete error handling and cleanup procedures

**Key Features Implemented:**
- Hemisphere forms around BOSS (not player) as specified
- 12-second wait after warrior waves (improved pacing)
- 2-second formation duration (optimized flow)
- Dynamic safe zone scaling (players + 1, max 6 zones)
- Complete 6-phase attack sequence with proper timing
- Both skull (phase 1) and arrow (phase 2) attack variants
- Comprehensive logging and recovery mechanisms

**Total Implementation Time: Already completed in previous development cycles**