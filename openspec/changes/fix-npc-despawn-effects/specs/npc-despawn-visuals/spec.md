# Spec Delta: NPC Despawn Visuals

## MODIFIED Requirements

### Requirement: NPC Despawn Must Not Leave Residual Particles
- При исчезновении (despawn) NPC система ДОЛЖНА прекратить все связанные с ним фоновые эффекты (аура, idle-анимации) и НЕ ДОЛЖНА оставлять частицы после удаления NPC.

#### Scenario: Start Despawn
- Given запущена анимация исчезновения NPC
- When начинается процесс despawn
- Then все запланированные задачи ауры/idle отменены
- And частицы/звуки не продолжают появляться после удаления NPC

### Requirement: Despawn Visuals Occur At NPC Location Only
- Визуальные эффекты исчезновения должны происходить исключительно в координатах NPC и НЕ должны появляться у игроков.

#### Scenario: Player Nearby During Despawn
- Given рядом с NPC находятся игроки
- When NPC исчезает
- Then у игроков не спавнятся частицы и не проигрываются звуки, связанные с исчезновением NPC
- And эффекты видны только в районе NPC
