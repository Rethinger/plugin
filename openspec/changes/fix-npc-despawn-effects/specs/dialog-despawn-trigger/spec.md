# Spec Delta: Dialog-Triggered NPC Despawn

## MODIFIED Requirements

### Requirement: Dialog Despawn Must Not Emit Player-Centered Effects
- Триггер исчезновения NPC из диалога НЕ должен создавать частицы/звуки у позиции игрока. Разрешены только эффекты у позиции NPC, управляемые NPCManager.

#### Scenario: Disappearance Line in Dialog
- Given диалог содержит строку «Посланник исчезает в тумане» (или аналогичный триггер)
- When срабатывает триггер деспауна из диалога
- Then вызывается NPCManager.despawnMessenger()
- And не создаются дополнительные частицы у игрока
- And визуальные эффекты исчезновения (если есть) формируются NPCManager у позиции NPC
