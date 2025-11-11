## 1. Legacy Boss Removal
- [x] 1.1 Backup existing Boss #2 (Wither) mechanics in Act2Listener
- [x] 1.2 Remove current Wither-based boss implementation from Act2Listener
- [ ] 1.3 Clean up Wither-specific configuration parameters
- [ ] 1.4 Document removed mechanics for potential rollback

## 2. Core Architecture Setup
- [x] 2.1 Create EndermanBossManager class with basic structure
- [x] 2.2 Implement EndermanCloneSystem for managing decoy entities
- [x] 2.3 Create EndermanPhaseManager for state transitions
- [x] 2.4 Implement EndermanTeleportController for movement abilities
- [x] 2.5 Create EndermanHealingShield for Phase 2 mechanics
- [x] 2.6 Implement EndermanVFXManager for visual effects
- [x] 2.7 Extend BossAttackState enum with Enderman attack types

## 3. Phase 1 Implementation
- [ ] 3.1 Implement counter-attack mechanic (every 4th player attack)
- [ ] 3.2 Create chaotic teleportation system (10-15 teleports every 25 seconds)
- [ ] 3.3 Implement clone creation during teleportation sequences
- [ ] 3.4 Add shadow disintegration effect for clone death
- [ ] 3.5 Implement vampirism healing (0.3 HP from damage dealt)
- [ ] 3.6 Add clone behavior and target acquisition

## 4. Phase 2 Implementation
- [ ] 4.1 Implement counter-attack mechanic for Phase 2
- [ ] 4.2 Create clone wave spawning (10-20 clones every 15 seconds)
- [ ] 4.3 Implement healing preparation sequence
- [ ] 4.4 Create shield visual effects and damage tracking
- [ ] 4.5 Add shield break mechanics and stun effect
- [ ] 4.6 Implement healing interruption when shield broken

## 5. Visual Effects Implementation
- [ ] 5.1 Create epic entrance sequence with vertical rift
- [ ] 5.2 Implement teleportation particle effects
- [ ] 5.3 Add shadow disintegration particles for clone death
- [ ] 5.4 Create healing shield particle system
- [ ] 5.5 Implement vampirism healing visual feedback
- [ ] 5.6 Add boss glow effects and visual identification

## 6. Combat Mechanics
- [ ] 6.1 Implement water immunity system
- [ ] 6.2 Add weather control (rain prevention)
- [ ] 6.3 Create player drop mechanic for players above boss
- [ ] 6.4 Implement block breaking to prevent fortification
- [ ] 6.5 Add aggro system with 75-block radius
- [ ] 6.6 Create boss damage scaling with player count

## 7. Integration with Act2Listener
- [x] 7.1 Integrate new Enderman boss with existing Act2Listener progression
- [x] 7.2 Update summoning ritual to fit Enderman theme
- [x] 7.3 Maintain existing boss name "Изверг Адских Глубин" in messages
- [x] 7.4 Implement boss health bar integration
- [x] 7.5 Add boss defeat triggers for story progression
- [x] 7.6 Ensure compatibility with existing player progress tracking

## 8. Configuration Integration
- [ ] 8.1 Replace boss #2 configuration parameters in config.yml
- [ ] 8.2 Create timing and balance configuration sections
- [ ] 8.3 Add visual effects customization options
- [ ] 8.4 Implement dynamic configuration reloading
- [ ] 8.5 Add debug configuration options

## 9. Testing and Validation
- [ ] 9.1 Create comprehensive testing scenarios
- [ ] 9.2 Performance optimization for clone management
- [ ] 9.3 Validate boss defeat progression works correctly
- [ ] 9.4 Test summoning ritual with new mechanics
- [ ] 9.5 Verify player progress tracking compatibility
- [ ] 9.6 Test rollback procedures if needed

## 10. Polish and Balance
- [ ] 10.1 Fine-tune damage values and timing
- [ ] 10.2 Optimize particle effect performance
- [ ] 10.3 Add sound effects for all abilities
- [ ] 10.4 Implement proper cleanup on boss defeat
- [ ] 10.5 Add comprehensive logging for debugging
- [ ] 10.6 Create player feedback for mechanics