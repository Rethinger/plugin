# Stationary Casting Attack - Implementation Tasks

## Overview

Задачи для реализации новой атаки босса с неподвижным заклинанием, визуализацией опасных зон и атакой клинками заклинателя.

## Task Categories

1. **Core Components** - Новые классы для механики заклинания
2. **Visual Effects** - Система визуализации опасных зон
3. **Attack System** - Реализация атаки клинками заклинателя
4. **Integration** - Интеграция с существующим кодом
5. **Testing & Polish** - Тестирование и финальная доработка

---

## Phase 1: Core Components Tasks

### 1.1 Create StationaryCastingManager Class
**Priority**: High | **Estimated**: 6 hours | **Dependencies**: None

Создать основной класс для управления атакой с неподвижным заклинанием.

**Tasks:**
- [ ] Create `StationaryCastingManager.java` with basic structure
- [ ] Implement 3-second casting phase management
- [ ] Add boss animation control (arm raising, swaying)
- [ ] Implement phase transitions (casting → fangs attack → cooldown)
- [ ] Add integration with existing warrior summon tracking
- [ ] Add error handling and cleanup procedures

**Acceptance Criteria:**
- Boss remains stationary throughout entire attack sequence
- 3-second casting period with proper phase tracking
- Boss performs arm raising and swaying animations
- Seamless transition to fangs attack phase
- Proper cleanup and error recovery

### 1.2 Create DangerZoneVisualizer Class
**Priority**: High | **Estimated**: 4 hours | **Dependencies**: None

Создать класс для визуализации опасной зоны красными частицами.

**Tasks:**
- [ ] Create `DangerZoneVisualizer.java` class structure
- [ ] Implement concentric red ring particle effects
- [ ] Add grid-based red particle visualization at eye level
- [ ] Implement pulsing intensity based on casting progress
- [ ] Add safe zone exclusion (no particles in safe zones)
- [ ] Optimize particle spawning for performance

**Acceptance Criteria:**
- Red particles clearly define 15-block danger radius
- Concentric rings expand from boss position
- Grid particles appear at player eye level (Y+1.5)
- Particle intensity pulses with casting progress
- No particles spawn within safe zone areas
- Performance limited to 100 particles per tick maximum

### 1.3 Create EvokerFangsAttack Class
**Priority**: High | **Estimated**: 5 hours | **Dependencies**: None

Создать класс для управления атакой клинками заклинателя.

**Tasks:**
- [ ] Create `EvokerFangsAttack.java` class structure
- [ ] Implement 5x5 grid positioning algorithm
- [ ] Add safe zone filtering (no fangs in safe zones)
- [ ] Create 3-wave fang spawning system
- [ ] Implement timing between waves (0.5 seconds)
- [ ] Add fang entity cleanup and management

**Acceptance Criteria:**
- 25 evoker fangs distributed across 15-block radius
- No fangs spawn within 1.5 blocks of safe zones
- 3 waves: immediate, 0.5s delay, 1s delay
- All fangs despawn automatically after attack
- Proper error handling for fang spawning failures

### 1.4 Enhance SafeZoneManager Integration
**Priority**: High | **Estimated**: 3 hours | **Dependencies**: None

Интегрировать существующий `SafeZoneManager` с новой механикой.

**Tasks:**
- [ ] Modify safe zone radius to 1.5 blocks (smaller, more precise)
- [ ] Implement strategic safe zone positioning algorithm
- [ ] Add safe zone count formula: `Math.min(players + 1, 5)`
- [ ] Create positioning patterns for different player counts
- [ ] Add safe zone validation (minimum spacing)

**Acceptance Criteria:**
- Safe zones have 1.5-block radius (smaller than before)
- Strategic placement based on player count
- 1 player: 2 zones, 2 players: 3 zones, 3+ players: up to 5 zones
- Minimum 4-block spacing between zones
- All zones within 15-block attack radius

---

## Phase 2: Visual Effects Tasks

### 2.1 Implement Boss Casting Animation
**Priority**: High | **Estimated**: 3 hours | **Dependencies**: 1.1

Создать визуальные эффекты заклинания для босса.

**Tasks:**
- [ ] Implement boss arm raising animation
- [ ] Add boss swaying motion during casting
- [ ] Create particle effects timeline (SOUL_FIRE_FLAME → DRAGON_BREATH → FLASH)
- [ ] Add sound effects with escalating pitch
- [ ] Implement final casting pose freeze

**Acceptance Criteria:**
- Boss raises arms smoothly at start of casting
- Gentle swaying motion during middle phase
- Particle effects follow 3-phase timeline
- Sound pitch increases throughout casting
- Boss freezes in dramatic pose before fangs attack

### 2.2 Design Red Particle Danger Visualization
**Priority**: High | **Estimated**: 4 hours | **Dependencies**: 1.2

Создать систему красных частиц для визуализации опасности.

**Tasks:**
- [ ] Create concentric ring particle system
- [ ] Implement grid-based particle placement
- [ ] Add pulsing intensity algorithm
- [ ] Create particle exclusion zones for safe areas
- [ ] Optimize particle count for performance

**Acceptance Criteria:**
- Red rings expand from boss every 2 blocks
- Grid covers entire 15-block radius circle
- Particle intensity pulses smoothly
- Clear gaps where safe zones will appear
- Maximum 100 particles per tick

### 2.3 Create Safe Zone Visual Indicators
**Priority**: Medium | **Estimated**: 2 hours | **Dependencies**: 1.4

Создать визуальные индикаторы для безопасных зон.

**Tasks:**
- [ ] Design safe zone particle effects
- [ ] Create zone boundary visualization
- [ ] Add zone appearance animations
- [ ] Implement zone distinction from danger area
- [ ] Add player entry/exit indicators

**Acceptance Criteria:**
- Safe zones have distinct visual appearance
- Clear boundary indicators (blue/green particles)
- Zones appear sequentially during casting
- Obvious visual difference from red danger zone
- Feedback when players enter/exit zones

---

## Phase 3: Attack System Tasks

### 3.1 Implement Evoker Fangs Damage System
**Priority**: High | **Estimated**: 3 hours | **Dependencies**: 1.3

Создать систему урона от клинков заклинателя.

**Tasks:**
- [ ] Implement evoker fang damage application
- [ ] Add safe zone immunity checking
- [ ] Create damage timing for each wave
- [ ] Add visual effects for fang impacts
- [ ] Implement damage logging and debugging

**Acceptance Criteria:**
- Fangs apply standard evoker damage to players
- Players in safe zones receive no damage
- Damage applies correctly for each wave timing
- Visual effects appear on successful hits
- Proper logging for damage events

### 3.2 Create Fang Position Calculation Algorithm
**Priority**: High | **Estimated**: 3 hours | **Dependencies**: 1.3

Создать алгоритм расчёта позиций клинков.

**Tasks:**
- [ ] Implement 5x5 grid positioning system
- [ ] Add circular boundary filtering
- [ ] Create safe zone exclusion algorithm
- [ ] Add randomization within grid cells
- [ ] Validate fang positions before spawning

**Acceptance Criteria:**
- Grid covers entire 15-block radius effectively
- Positions outside circle are filtered out
- No fangs within 1.5 blocks of safe zones
- Small random offset for natural placement
- All positions validated before spawning

### 3.3 Implement Wave Timing System
**Priority**: High | **Estimated**: 2 hours | **Dependencies**: 1.3

Создать систему тайминга волн атаки.

**Tasks:**
- [ ] Create wave spawning scheduler
- [ ] Implement 0.5 second wave intervals
- [ ] Add wave completion tracking
- [ ] Create timing synchronization with visual effects
- [ ] Add wave timing configuration options

**Acceptance Criteria:**
- Wave 1 spawns immediately after casting
- Wave 2 spawns after 0.5 seconds (10 ticks)
- Wave 3 spawns after 1 second (20 ticks)
- Each wave spawns correct number of fangs
- Timing is configurable if needed

---

## Phase 4: Integration Tasks

### 4.1 Update BossAttackState Integration
**Priority**: High | **Estimated**: 2 hours | **Dependencies**: All core tasks

Интегрировать новую механику с существующим BossAttackState.

**Tasks:**
- [ ] Add new stationary casting phases to enum
- [ ] Update state transition methods
- [ ] Integrate with existing warrior summon tracking
- [ ] Add timeout handling for new phases
- [ ] Maintain backward compatibility

**Acceptance Criteria:**
- New phases added without breaking existing ones
- State transitions work correctly
- Warrior summon tracking remains functional
- Proper timeout handling for all phases
- Existing boss mechanics unaffected

### 4.2 Integrate with Act2Listener
**Priority**: High | **Estimated**: 3 hours | **Dependencies: 4.1

Интегрировать новую атаку с существующим Act2Listener.

**Tasks:**
- [ ] Add stationary casting trigger logic
- [ ] Integrate with existing attack timing systems
- [ ] Replace hemisphere attack calls
- [ ] Add configuration switching support
- [ ] Test with existing boss combat flow

**Acceptance Criteria:**
- New attack triggers correctly during boss fights
- Integration doesn't break existing mechanics
- Easy switching between attack types via config
- Proper timing with other boss actions
- All boss combat phases work correctly

### 4.3 Update Configuration System
**Priority**: Medium | **Estimated**: 2 hours | **Dependencies: All tasks**

Обновить систему конфигурации для новой атаки.

**Tasks:**
- [ ] Add stationary casting configuration options
- [ ] Create configurable parameters (radius, timing, etc.)
- [ ] Add attack type selection (hemisphere vs stationary)
- [ ] Implement configuration validation
- [ ] Update configuration loading/saving

**Acceptance Criteria:**
- All new parameters are configurable
- Attack type can be switched via config
- Configuration validation prevents invalid values
- Settings reload without plugin restart
- Backward compatibility maintained

---

## Phase 5: Testing & Polish Tasks

### 5.1 Create Unit Tests
**Priority**: Medium | **Estimated**: 4 hours | **Dependencies: All implementation tasks**

Создать модульные тесты для новой системы.

**Tasks:**
- [ ] Create unit tests for StationaryCastingManager
- [ ] Create unit tests for DangerZoneVisualizer
- [ ] Create unit tests for EvokerFangsAttack
- [ ] Test safe zone positioning algorithms
- [ ] Test configuration validation

**Acceptance Criteria:**
- All new classes have comprehensive unit tests
- Test coverage exceeds 80% for new code
- Edge cases are properly tested
- Tests pass consistently
- Performance benchmarks included

### 5.2 Integration Testing
**Priority**: High | **Estimated**: 3 hours | **Dependencies: 5.1

Провести интеграционное тестирование всей системы.

**Tasks:**
- [ ] Test complete stationary casting attack sequence
- [ ] Test with varying numbers of players (1-8)
- [ ] Test performance under different conditions
- [ ] Test configuration switching between attack types
- [ ] Test error recovery scenarios

**Acceptance Criteria:**
- Complete attack sequence works correctly
- Performance acceptable with multiple players
- Configuration switching works properly
- System recovers gracefully from errors
- No memory leaks or resource issues

### 5.3 Gameplay Testing and Balance
**Priority**: High | **Estimated**: 2 hours | **Dependencies: 5.2**

Провести геймплейное тестирование и балансировку.

**Tasks:**
- [ ] Test attack difficulty with different player counts
- [ ] Verify safe zone effectiveness and accessibility
- [ ] Test timing and visual clarity of attack phases
- [ ] Gather feedback on visual effects and readability
- [ ] Adjust parameters based on testing results

**Acceptance Criteria:**
- Attack is challenging but fair for all player counts
- Safe zones provide meaningful tactical choices
- Visual effects are clear and readable
- Attack timing creates good tension
- Player feedback is positive

### 5.4 Performance Optimization
**Priority**: Medium | **Estimated**: 2 hours | **Dependencies: 5.3

Оптимизировать производительность новой системы.

**Tasks:**
- [ ] Profile particle effect performance
- [ ] Optimize fang spawning and cleanup
- [ ] Reduce memory usage during attacks
- [ ] Add performance monitoring and logging
- [ ] Test with maximum player count

**Acceptance Criteria:**
- Attack maintains 60+ FPS with 8+ players
- Memory usage remains stable during attacks
- No significant lag spikes during sequence
- Performance metrics within acceptable limits
- System scales well with player count

### 5.5 Documentation and Final Polish
**Priority**: Low | **Estimated**: 1 hour | **Dependencies: 5.4

Завершить документацию и финальную доработку.

**Tasks:**
- [ ] Update code comments and documentation
- [ ] Create configuration documentation
- [ ] Add troubleshooting guide for new mechanics
- [ ] Final code review and cleanup
- [ ] Prepare release notes

**Acceptance Criteria:**
- Code is well-documented and maintainable
- Configuration is thoroughly documented
- Common issues have documented solutions
- Code review passes quality standards
- Release notes are comprehensive

---

## Total Estimated Time: **43-47 hours**

**Phased Timeline:**
- Phase 1 (Core Components): 18 hours
- Phase 2 (Visual Effects): 9 hours
- Phase 3 (Attack System): 8 hours
- Phase 4 (Integration): 7 hours
- Phase 5 (Testing & Polish): 8 hours

## Risk Assessment

### High Risk Items
- **Particle Performance**: Complex red particle visualization may cause lag
- **Fang Entity Management**: Multiple evoker fangs may impact performance
- **Safe Zone Algorithm**: Complex positioning calculations may have edge cases

### Mitigation Strategies
- Implement particle density scaling based on player count
- Use entity pooling and batch cleanup for fangs
- Add comprehensive testing and validation for safe zone positioning

### Rollback Plan
If critical issues arise:
1. Disable stationary casting via configuration
2. Revert to existing hemisphere attack implementation
3. Maintain compatibility with existing saves and configurations
4. Provide easy configuration switching between attack types