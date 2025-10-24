# Feature Specification: Per-Player Dynamic Language System

**Feature Branch**: `005-per-player-i18n`  
**Created**: October 24, 2025  
**Status**: Draft  
**Input**: User description: "Проанализировать весь код и сделать систему перевода что бы когда игроки выбирали в /story menu русский или английский предметы переводились для каждого отдельного игрока (если игрок решит поменять язык на русский с английского сообщения об обыске, названия предметов и их описание и впринцепе все что у нас есть вплане диалогов и т.д)"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Language Selection Menu (Priority: P1)

A player opens `/story menu` and sees a language selection interface where they can choose between Russian and English. After selecting their preferred language, all future interactions (messages, dialogs, item names) appear in that language.

**Why this priority**: This is the core feature - without the ability to select and persist language preference, the entire feature is non-functional. This must be implemented first as all other stories depend on it.

**Independent Test**: Can be fully tested by running `/story menu`, selecting a language, and verifying the menu itself displays in the selected language. Delivers immediate value by giving players control over their experience.

**Acceptance Scenarios**:

1. **Given** a player joins the server for the first time, **When** they type `/story menu`, **Then** they see a language selection menu with options for Russian (ru_RU) and English (en_US)
2. **Given** a player selects Russian from the menu, **When** the selection is confirmed, **Then** their language preference is saved and all future messages appear in Russian
3. **Given** a player has previously selected English, **When** they open `/story menu` again, **Then** they can see their current selection highlighted and can change to Russian
4. **Given** a player changes language from English to Russian mid-session, **When** they close the menu, **Then** all subsequent interactions immediately use Russian without requiring reconnection

---

### User Story 2 - Dynamic Item Name Translation (Priority: P1)

When a player receives or views a story item (Stabilization Core, Boss Summon Keys, artifacts), the item's display name and lore automatically appear in their selected language. If they change their language preference, existing items in their inventory update to show the new language.

**Why this priority**: Item localization is critical because items are the primary tangible reward in the story campaign. Players need to understand what they're collecting. This is co-P1 with language selection because they're both essential for MVP.

**Independent Test**: Give player a Boss1 Summon Key → verify name shows in player's language → change language in menu → verify same item updates to new language in inventory. Delivers immediate value as items are core to gameplay.

**Acceptance Scenarios**:

1. **Given** a Russian-speaking player receives "Ключ Призыва Повелителя", **When** they view it in inventory, **Then** the display name is "§6§lКлюч Призыва Повелителя" and lore is in Russian
2. **Given** an English-speaking player receives the same item, **When** they view it in inventory, **Then** the display name is "§6§lBoss Summon Key" and lore is in English
3. **Given** a player has items in inventory with Russian names, **When** they change language to English in `/story menu`, **Then** all story items in their inventory immediately update to English names and lore
4. **Given** a player holds a story item, **When** another player with different language looks at it (in trade window or on ground), **Then** each player sees the item name in their own language

---

### User Story 3 - Chat Message Localization (Priority: P2)

All chat messages, including chest search results, boss warnings, quest notifications, and system messages, appear in the player's selected language. Multiple players with different language preferences see the same event in their own language.

**Why this priority**: While important for user experience, chat messages are less critical than items because they're temporary. A player can still complete the story with English messages if items are localized. This can be implemented after P1 stories.

**Independent Test**: Two players (one Russian, one English) search same chest → Russian player sees "🔍 Обыскиваю сундук...", English player sees "🔍 Searching chest..." → both understand the action. Delivers value for multilingual servers.

**Acceptance Scenarios**:

1. **Given** a Russian player searches a chest, **When** the search begins, **Then** they see "🔍 Обыскиваю сундук..." while English players see "🔍 Searching chest..."
2. **Given** a boss spawns and multiple players are nearby, **When** the boss sends a warning, **Then** Russian players see "§5§l⚡ Повелитель готовит разрушительную атаку!" and English players see "§5§l⚡ The Lord prepares a devastating attack!"
3. **Given** a player changes language mid-quest, **When** they receive the next quest message, **Then** it appears in their newly selected language
4. **Given** a player with Russian language completes an objective, **When** the completion message is sent, **Then** it uses Russian templates with proper grammar and formatting

---

### User Story 4 - Dialog Localization (Priority: P2)

NPC dialogs shown via titles, action bars, and boss bars automatically display in the player's selected language. Each player in a multiplayer scenario sees dialogs in their own language without affecting others.

**Why this priority**: Dialogs are crucial for story immersion but not for core gameplay mechanics. Players can understand story objectives through items and actions even if dialogs are in another language. This enhances experience but isn't blocking.

**Independent Test**: Trigger NPC interaction → verify dialog title appears in player's language → another player with different language sees same NPC → verifies they see different language. Delivers story immersion value.

**Acceptance Scenarios**:

1. **Given** a Russian player interacts with the messenger NPC, **When** dialog triggers, **Then** the title displays "§6§lТаинственный посланник" and subtitle in Russian
2. **Given** multiple players with different languages watch the same NPC dialog, **When** the dialog plays, **Then** each player sees titles and subtitles in their own language simultaneously
3. **Given** a player is in the middle of a multi-step dialog, **When** they change language, **Then** subsequent dialog steps appear in the new language
4. **Given** a timed boss bar message appears, **When** Russian and English players see it, **Then** the boss bar title shows "Время до атаки: 10с" for Russian and "Time until attack: 10s" for English with synchronized countdown

---

### User Story 5 - Language Persistence (Priority: P3)

A player's language preference persists across server restarts and reconnections. The preference is stored per-player and does not rely on Minecraft client locale.

**Why this priority**: While persistence improves user experience, the feature is still functional without it if players can reselect language each session. This is a polish feature that can be added after core functionality works.

**Independent Test**: Set language to Russian → disconnect → reconnect → verify language is still Russian without reopening menu. Delivers convenience value but not core functionality.

**Acceptance Scenarios**:

1. **Given** a player sets language to Russian, **When** they disconnect and reconnect, **Then** all messages and items still appear in Russian
2. **Given** a server restarts, **When** players rejoin, **Then** each player's language preference is restored from persistent storage
3. **Given** a player's Minecraft client locale is English, **When** they manually set plugin language to Russian, **Then** Russian is used regardless of client locale
4. **Given** multiple players on the same computer with different accounts, **When** each logs in, **Then** each has their own independent language preference

---

### Edge Cases

- What happens when a player with Russian language joins a team with English players? → Each sees messages in own language; team coordination works via universal icons/coords
- How does system handle partial translations (key missing in selected language)? → Falls back to English, then Russian, then shows raw key with warning log (already implemented in MessageManager)
- What if player changes language while holding 36 story items in inventory? → All items update within one inventory refresh cycle (<1 second) using batch update
- What if item is dropped on ground and player changes language? → Item entity updates when picked up; items on ground maintain neutral appearance
- What happens to items in offline player's inventory when language files are updated? → Items update on next login using latest translations
- How does system handle language selection for players in combat? → Menu access is allowed but warned that language switch takes effect immediately
- What if two players trade items with different language preferences? → Each player sees item in their own language; trade still works as item identity is language-independent

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide a `/story menu` command that opens a graphical interface showing available languages (Russian and English)
- **FR-002**: System MUST allow players to select their preferred language from the menu using clickable options
- **FR-003**: System MUST store each player's language preference in persistent storage that survives server restarts
- **FR-004**: System MUST immediately apply language changes to all active content (messages, items, dialogs) without requiring reconnection
- **FR-005**: System MUST translate item display names based on player's selected language when items are viewed or received
- **FR-006**: System MUST translate item lore (descriptions) based on player's selected language
- **FR-007**: System MUST update all story items in player's inventory when language preference changes
- **FR-008**: System MUST translate all chat messages (chest search, boss warnings, quest notifications) per player's language
- **FR-009**: System MUST translate NPC dialog titles, subtitles, and action bar messages per player's language
- **FR-010**: System MUST translate boss bar titles and messages per player's language
- **FR-011**: System MUST support simultaneous different languages for multiple players viewing the same event
- **FR-012**: System MUST fall back to English if translation key is missing in selected language, then to Russian, then show raw key
- **FR-013**: System MUST default new players to Russian language if no preference is set
- **FR-014**: System MUST preserve item functionality when language changes (same item, different display)
- **FR-015**: System MUST log language changes for debugging purposes

### Key Entities

- **Language Preference**: Player UUID → selected language code (ru/en), stored persistently, defaults to "ru"
- **Localized Item**: Story item with PDC identifier → display name and lore that dynamically resolve based on viewing player's language
- **Translation Key**: String path (e.g., "items.boss1_summon_key.name") → Russian text and English text in separate YAML files
- **Player Session**: Active connection → cached language preference for performance, invalidated on preference change

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Players can select and change their language preference in under 10 seconds using `/story menu`
- **SC-002**: Item names and descriptions update in player's inventory within 1 second of changing language preference
- **SC-003**: Multiple players with different language preferences can interact with the same NPC/boss/chest and each sees content in their own language
- **SC-004**: 100% of existing story content (items, messages, dialogs) has translations available in both Russian and English
- **SC-005**: Language preference persists across server restarts with 100% reliability (no language resets)
- **SC-006**: System handles language switching for 50 concurrent players without performance degradation
- **SC-007**: Players report understanding story objectives in their native language with 95%+ comprehension
- **SC-008**: Support tickets related to language confusion reduce by 80% after implementation
- **SC-009**: New Russian-speaking players can complete the story without needing English knowledge
- **SC-010**: New English-speaking players can complete the story without needing Russian knowledge

## Scope & Boundaries

### In Scope

- Language selection menu accessible via `/story menu`
- Persistent storage of player language preferences
- Dynamic translation of item names and lore
- Dynamic translation of chat messages
- Dynamic translation of NPC dialogs (titles, subtitles, action bars, boss bars)
- Support for Russian (ru) and English (en) languages
- Real-time language switching without reconnection
- Inventory update system for existing items
- Fallback mechanism for missing translations

### Out of Scope

- Translation of non-story vanilla Minecraft content
- Automatic translation (all translations are manually authored in YAML files)
- Support for languages other than Russian and English (can be added later)
- Translation of player-entered text or custom names
- Voice-over or audio localization
- Translation of server-wide announcements from other plugins
- Right-to-left language support (Arabic, Hebrew)
- Translation of coordinates, numbers, or player names

## Assumptions

1. **Assumption**: Players understand basic Minecraft controls (clicking menus, navigating GUIs)
   - **Validation**: Target audience has played Minecraft before
   
2. **Assumption**: Russian and English translations are culturally appropriate and grammatically correct
   - **Validation**: Native speakers will review translation files before release
   
3. **Assumption**: Server has sufficient storage for per-player language preferences (8 bytes per player)
   - **Validation**: Standard servers handle millions of UUID-based records
   
4. **Assumption**: Most players will set their language once and rarely change it
   - **Validation**: Industry standard shows <5% of users change language preferences frequently
   
5. **Assumption**: Item translation happens client-side (no server packet modifications needed)
   - **Validation**: Minecraft allows server to modify ItemStack display names and lore freely
   
6. **Assumption**: The MessageManager.getPlayerLanguage() fallback to client locale is acceptable as default
   - **Validation**: Current system already uses this logic; enhancement adds explicit override

## Dependencies

- **Dependency**: Existing MessageManager with Russian and English message files (`messages.yml`, `messages_en.yml`)
- **Dependency**: Existing ItemManager capable of creating items with language parameter
- **Dependency**: Paper API inventory event system for item translation updates
- **Dependency**: Bukkit persistent data container for unique item identification
- **Dependency**: Player data storage system for language preferences (file-based or database)

## Open Questions

None - all critical decisions have reasonable defaults based on existing system architecture and industry standards.

## Notes

- Current system already has foundation: MessageManager supports language parameter and fallback chain
- ItemManager already has language-aware item creation methods (createStabilizationCore(lang))
- Implementation can leverage existing infrastructure rather than building from scratch
- Main technical challenge: inventory update mechanism when player changes language (likely requires InventoryOpenEvent + metadata scanning)
- Performance consideration: Cache translated item names per language to avoid repeated YAML lookups
