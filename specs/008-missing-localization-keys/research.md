# Research: Missing Localization Keys Implementation

**Feature**: 008-missing-localization-keys  
**Date**: October 29, 2025  
**Phase**: 0 - Research & Technical Decisions

## Research Questions

### 1. How to Extract All Localization Keys from Java Source Code?

**Decision**: Use grep-based pattern matching with PowerShell for simplicity and maintainability

**Rationale**:
- MessageManager uses `getMessage(String path)` and `getMessageList(String path)` patterns
- Keys are always string literals in the codebase (no dynamic key generation)
- PowerShell Select-String provides regex matching with file/line reporting
- No external dependencies required (vs AST parsing libraries)
- Fast execution (<1 second for entire codebase)

**Implementation Pattern**:
```powershell
# Extract all getMessage calls with string literal arguments
Select-String -Path "src\main\java\**\*.java" -Pattern 'getMessage\("([^"]+)"\)|getMessageList\("([^"]+)"\)'
```

**Alternatives Considered**:
- **JavaParser AST analysis**: More accurate but requires Maven dependency, slower, overkill for string literal extraction
- **Manual code review**: Time-consuming, error-prone, not repeatable
- **IDE refactoring tools**: Not scriptable, not CI-friendly

### 2. When Should Localization Validation Occur?

**Decision**: Both build-time (Maven phase) and runtime (existing warning logs)

**Rationale**:
- **Build-time**: Prevents deployment with missing keys (fail-fast principle)
- **Runtime**: Catches edge cases and dynamic scenarios during testing
- **Per clarifications**: Both approaches complement each other for comprehensive coverage

**Build-Time Implementation**:
- Maven exec plugin runs validation script during `validate` phase (before compile)
- Script exits with non-zero code if missing keys detected
- CI/CD pipeline fails before artifact creation

**Runtime Implementation** (already exists):
- MessageManager.getMessage() logs warnings when keys are missing
- Displayed key name makes issues immediately visible to testers

**Alternatives Considered**:
- **Runtime only**: Allows bugs to reach production
- **Build-time only**: Misses dynamic/conditional key usage

### 3. How to Structure YAML Files for Maintainability?

**Decision**: Maintain existing hierarchical structure with consistent prefixing

**Current Structure** (from existing files):
```yaml
# Functional area prefix
chest:
  search:
    searching: "Поиск..."
    searching_item: "Поиск {item}..."
    nothing: "Ничего не найдено"

items:
  stabilization_core:
    name: "Ядро стабилизации"
    lore:
      - "Древний артефакт"
```

**Rationale**:
- Groups related keys by feature (chest, items, dialogs, menu)
- Easy to locate keys during development
- Natural mapping to Java package structure
- Supports IDE YAML navigation

**Anti-Pattern** (rejected):
```yaml
# Flat structure - harder to maintain
chest_search_searching: "..."
chest_search_searching_item: "..."
```

### 4. What Translation Quality Standards to Apply?

**Decision**: Professional game translation quality with context-aware terminology

**Quality Requirements**:
- **Consistency**: Use same term for same concept across all translations
- **Context**: Item/UI translations differ from narrative dialog translations
- **Tone**: Match existing plugin style (semi-formal adventure game)
- **Technical accuracy**: Game mechanics terms must be precise

**Translation Workflow**:
1. Extract all English keys → create translation template
2. Native Russian speaker translates with context notes
3. Cross-reference with existing translations for terminology consistency
4. In-game testing for length/formatting issues
5. Glossary update for future translations

**Tools**:
- Manual translation by native speaker (no machine translation for quality)
- Glossary file: `specs/008-missing-localization-keys/glossary.md`
- Context screenshots for UI elements

**Alternatives Considered**:
- **Machine translation**: Fast but poor quality, inconsistent terminology
- **Community contributions**: Slow, quality varies, requires heavy review

### 5. How to Prevent Future Missing Keys?

**Decision**: Multi-layered prevention strategy

**Layer 1 - Build-Time Validation**:
- PowerShell script scans all Java files for `getMessage()` calls
- Compares extracted keys against messages_ru.yml (primary language)
- Fails build if any keys are missing in Russian
- Warning (non-blocking) if keys missing in English

**Layer 2 - Development Guidelines**:
- Document process in `TRANSLATION_TEST_CHECKLIST.md` (already exists)
- Require translation entries before PR approval
- Code review checklist includes localization completeness

**Layer 3 - Runtime Safety Net**:
- Keep existing warning logs for edge cases
- Display technical key name when translation missing (per clarifications)

**Layer 4 - Testing Protocol**:
- Manual testing checklist includes language switching
- Both Russian and English playthroughs required before release

**Implementation Details**:
```powershell
# validate-localization-keys.ps1
$javaKeys = Extract-Keys-From-Java-Code
$yamlKeys = Extract-Keys-From-YAML "messages_ru.yml"
$missing = $javaKeys | Where-Object { $_ -notin $yamlKeys }

if ($missing.Count -gt 0) {
    Write-Error "Missing Russian translations: $($missing -join ', ')"
    exit 1
}
```

## Best Practices Summary

### From Minecraft Plugin Development
- Use Paper's native YAML ConfigurationSection API (no custom parsers)
- Pre-load all translations on plugin enable (cache in memory)
- Support hot-reload for rapid translation iteration during development
- UTF-8 encoding for Cyrillic characters

### From Game Localization Industry
- Context matters: "Open" (verb) vs "Open" (adjective) require different translations
- String interpolation: Use {placeholder} syntax for dynamic values
- Pluralization: Russian has complex plural rules (1, 2-4, 5+ different forms)
- Length constraints: UI elements have space limits (test long translations)

### From Constitution Requirements
- Russian is PRIMARY language - feature incomplete if Russian missing
- English is SECONDARY - warnings only if English missing
- Never show English fallback to Russian players (shows technical key instead)
- All user-facing text must route through MessageManager (no hardcoded strings)

## Technical Constraints

- YAML file size: Keep under 1MB for fast loading (current ~50KB, plenty of headroom)
- Key naming: Use dot notation (area.feature.action) for hierarchy
- Special characters: Escape quotes, use \\n for newlines
- Color codes: Use & syntax (e.g., &c for red) not § (handled by MessageManager)
- File encoding: UTF-8 with BOM for Windows compatibility

## Risks & Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Incomplete key extraction | Medium | High | Manual code review + comprehensive testing |
| Translation errors | Medium | Medium | Native speaker review + glossary |
| Build performance hit | Low | Low | Script runs in <1s, negligible overhead |
| YAML syntax errors | Medium | High | Validation in build script before parsing |
| Merge conflicts in YAML | High | Low | Clear area-based organization, frequent commits |

## References

- **Constitution**: `.specify/memory/constitution.md` - Principle V (Localization-First)
- **Existing Checklist**: `TRANSLATION_TEST_CHECKLIST.md` - Testing protocols
- **Feature 005**: Per-player i18n system documentation
- **MessageManager**: `src/main/java/com/mmmm/story/managers/MessageManager.java` - API reference
- **LanguageManager**: `src/main/java/com/mmmm/story/managers/LanguageManager.java` - Language selection logic
