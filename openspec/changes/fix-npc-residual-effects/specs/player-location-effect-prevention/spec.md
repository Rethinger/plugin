# Spec Delta: Player Location Effect Prevention

## MODIFIED Requirements

### Requirement: Strict NPC-Centered Visual Effects
- Все визуальные эффекты, связанные с NPC (частицы, звуки, анимации), ДОЛЖНЫ создаваться исключительно в координатах NPC и SHALL строго избегать привязки к позиции игрока.

#### Scenario: Dialog-Triggered NPC Actions
- Given диалог содержит триггеры действий NPC
- When выполняются визуальные эффекты, связанные с NPC
- Then все частицы создаются в location NPC
- And все звуки проигрываются в location NPC
- And ни один эффект не создается в player.getLocation()

### Requirement: Isolation of Player and NPC Effect Systems
- Система визуальных эффектов ДОЛЖНА строго разделять эффекты, связанные с игроком, и эффекты, связанные с NPC, предотвращая перекрестное загрязнение, и SHALL обеспечивать полную изоляцию систем.

#### Scenario: Multiple Players Near NPC
- Given несколько игроков находятся рядом с NPC
- When NPC выполняет действия с визуальными эффектами
- Then эффекты создаются только в позиции NPC
- And эффекты видны всем игрокам в радиусе, но не привязаны к их позициям
- And каждый игрок видит одни и те же эффекты в одном месте (позиция NPC)