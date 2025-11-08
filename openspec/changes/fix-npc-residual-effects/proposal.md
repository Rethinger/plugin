# Change: Fix NPC Despawn Residual Effects

## Why
После исчезновения NPC остаются частицы и визуальные эффекты, а также при исчезновении вокруг игрока появляются нежелательные визуальные эффекты. Предыдущее исправление (fix-npc-despawn-effects) не полностью решило проблему с остаточными частицами и эффектами у игроков.

## What Changes
- Улучшение очистки частиц при деспауне NPC для полного удаления всех остаточных эффектов
- Предотвращение появления визуальных эффектов у позиции игрока во время исчезновения NPC
- Добавление механизма принудительной очистки всех частиц в радиусе NPC
- Усиление контроля за отменой всех запланированных задач, связанных с визуальными эффектами

## Status
- Proposal created, implementation pending

## Impact
- Affected specs: npc-despawn-visuals, dialog-despawn-trigger
- Affected code: src/main/java/com/mmmm/story/managers/NPCManager.java, src/main/java/com/mmmm/story/managers/DialogManager.java

## Notes
- Это изменение дополняет предыдущее fix-npc-despawn-effects для полного решения проблемы с остаточными эффектами.