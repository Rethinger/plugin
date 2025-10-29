# Feature Specification: Missing Localization Keys Throughout Plugin

**Feature Branch**: `008-missing-localization-keys`  
**Created**: October 29, 2025  
**Status**: Draft  
**Input**: User description: "Missing localization keys throughout the plugin - log shows errors for chest.search.searching, chest.search.searching_item, chest.search.nothing, items.stabilization_core.name, and almost everywhere localization is missing"

## Clarifications

### Session 2025-10-29

- Q: When a localization key is missing in a player's selected language, what should the system display? → A: Show the key name (technical key like "chest.search.searching"). Russian is the main language for this project, so any missing Russian keys should be immediately obvious for fixing. Missing keys should not be masked by fallbacks.
- Q: When should the system validate that all localization keys used in code have corresponding translations? → A: Both build and runtime - Validation happens during build/compile time to prevent deployment with missing keys, plus runtime checks for dynamic content or edge cases.
- Q: How should you identify ALL missing localization keys throughout the plugin beyond the ones visible in the server logs? → A: Automated code scan - Parse source code to extract all key references and compare with translation files for comprehensive coverage.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Player Searches Chests (Priority: P1)

A player interacts with the chest search functionality and sees properly translated messages in their selected language for all search-related actions including searching state, searching for specific items, and empty search results.

**Why this priority**: Core gameplay feature that players interact with frequently. Missing translations create a broken user experience and make the feature unusable for non-English speakers.

**Independent Test**: Player can open chest search interface, initiate a search, search for specific items, and receive empty results - all messages appear in their selected language without any missing key warnings.

**Acceptance Scenarios**:

1. **Given** a player with Russian language selected, **When** they initiate a chest search, **Then** they see the "Searching..." message in Russian
2. **Given** a player searching for a specific item, **When** the search is in progress, **Then** they see "Searching for [item]..." message in their selected language
3. **Given** a player completes a search with no results, **When** the search finishes, **Then** they see "Nothing found" message in their selected language
4. **Given** server console is open, **When** any player uses chest search, **Then** no "Missing localization key" warnings appear for chest.search.* keys

---

### User Story 2 - Player Views Item Names (Priority: P1)

A player views custom items in their inventory, tooltips, or menus and sees properly translated item names in their selected language.

**Why this priority**: Item names are fundamental to gameplay. Players need to understand what items they have. Missing translations make items unidentifiable for non-English speakers.

**Independent Test**: Player can view the stabilization core item (and other custom items) in inventory, hover for tooltip, and see properly translated item name without fallback to key names.

**Acceptance Scenarios**:

1. **Given** a player with Russian language selected, **When** they view a stabilization core in their inventory, **Then** they see the item name in Russian (not "items.stabilization_core.name")
2. **Given** a player hovers over a custom item, **When** the tooltip appears, **Then** the item name is translated in their selected language
3. **Given** server console is open, **When** any player interacts with custom items, **Then** no "Missing localization key" warnings appear for items.* keys

---

### User Story 3 - Comprehensive Localization Coverage (Priority: P2)

Players and administrators can use all plugin features without encountering any missing localization keys, ensuring a complete multilingual experience across all plugin functionality.

**Why this priority**: While individual features may work, comprehensive coverage ensures professional quality and prevents users from encountering any untranslated content anywhere in the plugin.

**Independent Test**: Run through all plugin features (menus, commands, NPCs, dialogs, notifications) and verify no "Missing localization key" warnings appear in console for any language.

**Acceptance Scenarios**:

1. **Given** a player using any supported language, **When** they interact with any plugin feature, **Then** all text content is properly translated
2. **Given** an administrator reviews server logs, **When** checking for localization warnings, **Then** no "Missing localization key" warnings are present
3. **Given** a player switches languages, **When** they revisit previously used features, **Then** all content updates to the new language without missing keys

---

### Edge Cases

- What happens when a localization key is requested for a language that doesn't have that specific translation? → System displays the technical key name to make it immediately obvious
- How does the system handle newly added features that may not have translations yet? → Technical key names are displayed until translations are added
- What happens when a custom item is added by server admin without translations? → Item displays its technical key name until proper translations are provided
- Russian is the main language - all features must have complete Russian translations before release

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide complete Russian translations for all chest.search.* localization keys (searching, searching_item, nothing)
- **FR-002**: System MUST provide complete translations for all items.*.name localization keys including stabilization_core
- **FR-003**: System MUST NOT log any "Missing localization key" warnings during normal plugin operation
- **FR-004**: System MUST display translated text to players instead of localization key names when viewing UI elements
- **FR-005**: System MUST display the technical key name (e.g., "chest.search.searching") when a translation is missing, making missing keys immediately visible for correction
- **FR-006**: System MUST validate all localization files contain entries for all keys used in the plugin code at both build/compile time (preventing deployment with missing keys) and runtime (for dynamic content validation)
- **FR-007**: System MUST support both English and Russian languages for all identified missing keys, with Russian as the primary language
- **FR-008**: System MUST maintain consistency between messages.yml, messages_en.yml, and messages_ru.yml files
- **FR-009**: Missing localization keys MUST be identified through automated source code scanning that extracts all key references and compares them with translation files

### Key Entities *(include if feature involves data)*

- **Localization Key**: Unique identifier for translatable text (e.g., "chest.search.searching")
- **Localization File**: Language-specific resource file containing key-value pairs (messages_en.yml, messages_ru.yml)
- **Player Language Preference**: Per-player setting determining which language to display (from existing per-player-i18n feature)

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Zero "Missing localization key" warnings appear in server logs during standard gameplay testing
- **SC-002**: 100% of chest search interactions display translated messages in both English and Russian
- **SC-003**: 100% of custom item names display translated text in both English and Russian
- **SC-004**: Players can complete all plugin features without encountering any untranslated text (key names visible to users)
- **SC-005**: All identified missing keys from server logs are added to localization files with proper translations

## Scope & Boundaries *(mandatory)*

### In Scope

- Adding missing translations for chest.search.* keys
- Adding missing translations for items.*.name keys
- Automated source code scanning to identify all missing localization keys throughout the plugin
- Adding Russian and English translations for all identified missing keys
- Build-time and runtime validation of localization key completeness
- Testing localization coverage across all plugin features
- Updating localization files (messages_en.yml, messages_ru.yml)

### Out of Scope

- Adding support for additional languages beyond English and Russian
- Refactoring the localization system architecture
- Creating new features or functionality beyond translations
- Modifying the per-player language selection system (already implemented in 005-per-player-i18n)
- Translating external plugin messages or Minecraft vanilla messages

## Dependencies & Constraints *(mandatory)*

### Dependencies

- Feature 005-per-player-i18n must be implemented (provides per-player language system)
- Access to all plugin code to identify all localization key usage
- Native Russian speaker or professional translator for accurate translations

### Technical Constraints

- Must maintain compatibility with existing YAML localization file format
- Must not break existing translated content
- Must preserve existing localization key naming conventions
- Server must not require restart to apply new translations if hot-reload is supported

### Assumptions

- The plugin uses YAML files for localization (messages_en.yml, messages_ru.yml)
- The per-player i18n system from feature 005 is functioning correctly
- Missing keys are primarily due to incomplete translation files, not system bugs
- Standard web game translation quality is sufficient (professional but not literary)
- Russian is the primary/main language for this project - all features must have complete Russian translations
- Missing translation keys should be displayed as technical key names (no fallback) to ensure they are immediately identified and fixed

## Risks & Mitigation *(optional)*

### Risks

1. **Risk**: Additional missing keys may be discovered during testing beyond those visible in provided logs
   - **Mitigation**: Conduct comprehensive plugin walkthrough testing in both languages; implement automated key validation

2. **Risk**: Translation quality may be inconsistent or inaccurate
   - **Mitigation**: Use native Russian speaker for review; maintain translation glossary for terminology consistency

3. **Risk**: New features added in future may introduce missing keys again
   - **Mitigation**: Document process for adding new translations; consider automated testing to catch missing keys before release

## Out of Scope *(mandatory)*

- Translation memory or translation management system
- Automated translation services or machine translation
- Community translation contribution system
- Support for languages other than English and Russian
- Localization of date formats, number formats, or other locale-specific formatting
- Right-to-left (RTL) language support
