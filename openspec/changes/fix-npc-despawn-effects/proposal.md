# Change: Fix NPC Despawn Visual Effects

## Why
Игроки сообщали: после исчезновения NPC оставались частицы; также при исчезновении у игрока появлялись эффекты. Это отвлекало и создавало ложные сигналы.

## What Changes
- Отключение/остановка всех фоновых аур и idle-анимаций при начале despawn.
- Удаление эффектов, возникающих у игрока при исчезновении NPC; визуальные эффекты только у позиции NPC.
- Централизация триггера деспауна через NPCManager.despawnMessenger(), синхронно с диалогом.
- Трекинг и отмена всех одноразовых отложенных задач, связанных с messenger.

## Status
- Implemented and validated locally per tasks.md.

## Impact
- Affected specs: npc-despawn-visuals, dialog-despawn-trigger
- Affected code: src/main/java/com/mmmm/story/managers/NPCManager.java, src/main/java/com/mmmm/story/managers/DialogManager.java

## Notes
- Компасный маркер (giveDirectionMarker) остаётся не-NPC-связанным и не трекается в scheduledTasks по дизайну.
