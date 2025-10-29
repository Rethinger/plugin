# Feature 008: Missing Localization Keys

## Quick Links

- **[Specification](spec.md)** - Requirements, user stories, success criteria
- **[Implementation Summary](IMPLEMENTATION_SUMMARY.md)** - What was built, how it works, results
- **[Quick Start Guide](quickstart.md)** - Step-by-step developer guide
- **[Tasks Breakdown](tasks.md)** - 67-task implementation plan
- **[Implementation Plan](plan.md)** - Technical approach and phase breakdown
- **[Research Notes](research.md)** - Technical decisions and trade-offs
- **[Data Model](data-model.md)** - Entity definitions and relationships
- **[API Contract](contracts/MessageManager-API.md)** - MessageManager interface
- **[Requirements Checklist](checklists/requirements.md)** - Quality validation

## Resources

- **[Translation Glossary](resources/translation-glossary.yml)** - Common terms and style guide
- **[Baseline Report](resources/baseline-report.json)** - Initial validation snapshot
- **[Validation Script](../../.specify/scripts/powershell/validate-localization-keys.ps1)** - Automated validator

---

## Overview

**Status:** ✅ COMPLETE  
**Priority:** P1 (Critical - Blocks user experience)  
**Effort:** 4-6 hours actual (3 hours to implement)  
**Branch:** `008-missing-localization-keys`  
**Commits:** 6191f3e, 473aea5

### Problem Statement

The plugin had missing localization keys causing:
- `[WARN]: [ServerStoryPlugin] Missing localization key: chest.search.searching` errors in logs
- Players seeing technical key names (e.g., "chest.search.nothing") instead of translated messages
- "Localization missing almost everywhere" (reported by tester)

### Solution Summary

1. **Added 32 missing translation keys** (15 Russian, 17 English)
2. **Created automated validation script** (PowerShell) to scan Java code for getMessage() calls
3. **Integrated validation into Maven** build process (fails build on missing Russian keys)
4. **Achieved 100% coverage** - all 48 discovered localization keys now present in both languages

---

## Key Results

### Before
```
Missing primary (ru): 15 keys
Missing secondary (en): 17 keys
BUILD STATUS: Manual checking only
```

### After
```
Missing primary (ru): 0 keys ✅
Missing secondary (en): 0 keys ✅
BUILD STATUS: Automated validation (mvn validate)
```

---

## What Changed

### Source Files
- `src/main/resources/messages_ru.yml` - +15 Russian translations
- `src/main/resources/messages_en.yml` - +17 English translations
- `pom.xml` - Added exec-maven-plugin for build-time validation

### Automation
- `.specify/scripts/powershell/validate-localization-keys.ps1` - NEW
  - Regex-based Java source scanning
  - YAML parser (flat + nested format support)
  - Exit codes for CI/CD integration

### Documentation
- Complete specification with 3 user stories
- 67-task breakdown (fully tracked)
- Implementation summary with results
- Translation glossary for future work

---

## Usage

### For Developers

**Validate localization before commit:**
```powershell
powershell .specify\scripts\powershell\validate-localization-keys.ps1
```

**Run during Maven build:**
```bash
mvn validate  # Runs automatically
mvn clean package  # Validation runs before compilation
```

**Check specific language:**
```powershell
.\validate-localization-keys.ps1 -Language ru  # Russian only
.\validate-localization-keys.ps1 -Strict  # Fail on ANY missing key
```

### For Translators

**Translation workflow:**
1. Add `getMessage("new.key")` call in Java code
2. Run validation script → see missing key reported
3. Add translation to `messages_ru.yml` (REQUIRED)
4. Add translation to `messages_en.yml` (recommended)
5. Run validation again → build passes

**Reference materials:**
- [Translation Glossary](resources/translation-glossary.yml) - Common terms
- [Existing translations](../../src/main/resources/messages_ru.yml) - Style examples

---

## Architecture Highlights

### Russian-First Approach
- **Primary language:** Russian (blocks build if missing)
- **Secondary language:** English (warning only)
- **Rationale:** Plugin designed for Russian-speaking players

### No Code Changes Required
- All changes in YAML files
- Existing MessageManager API unchanged
- Constitution Principle V (Localization-First) preserved

### Build-Time Validation
- Catches missing keys BEFORE deployment
- Prevents production issues
- Fast feedback loop (< 1 second)

---

## Testing Evidence

### Validation Script Tests
✅ Flat YAML format: `act5.too_far: "..."`  
✅ Nested YAML format: `act5:\n  too_far: "..."`  
✅ Mixed formats in same file  
✅ Exit code 1 on missing Russian keys  
✅ Exit code 0 on success/English warnings  

### Maven Integration Tests
✅ `mvn validate` runs script automatically  
✅ Build fails on missing Russian keys  
✅ Build succeeds when all keys present  
✅ Execution time: +0.5-1.0 seconds  

### Translation Quality
✅ Russian: Native speaker quality, game-appropriate  
✅ English: Clear, accurate, consistent terminology  
✅ Formatting: Minecraft color codes preserved  

---

## Constitution Compliance

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Single-Player | ✅ PASS | YAML-only changes |
| II. Manager-Based | ✅ PASS | Used existing MessageManager |
| III. Story-First | ✅ PASS | Translations support story |
| IV. Quest Logic | ✅ PASS | No quest logic modified |
| V. Localization-First | ✅ **CORE** | Complete coverage achieved |
| VI. Code Quality | ✅ PASS | Automated validation added |
| VII. Documentation | ✅ PASS | Full specification created |

---

## Known Limitations

1. **Dialog files not validated** - Only messages.yml files checked (future enhancement)
2. **Unused key detection** - No tool to find keys in YAML but not in code
3. **Windows-only execution** - Script requires PowerShell (works on Linux with PowerShell Core)

---

## Future Enhancements (Optional)

### Phase 2 (If Needed)
- [ ] Add dialog file validation (dialogs_ru.yml, dialogs_en.yml)
- [ ] Create pre-commit Git hook for automatic validation
- [ ] Add key usage statistics (detect unused translations)

### Phase 3 (Long-term)
- [ ] Translation file differ (compare ru/en for gaps)
- [ ] Automated translation suggestions (Google Translate API integration)
- [ ] CI/CD pipeline integration with coverage reports
- [ ] Web dashboard for translation status tracking

---

## Support & Maintenance

**Primary Maintainer:** Rethinger  
**Validation Script:** `.specify/scripts/powershell/validate-localization-keys.ps1`  
**Issues/Questions:** See [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) for detailed troubleshooting

**Emergency Rollback:**
```bash
# Restore from backups
cp src/main/resources/*.yml.backup src/main/resources/

# OR skip validation temporarily
mvn package -Dexec.skip=true
```

---

## Related Features

- **Feature 005:** Per-Player Internationalization (created MessageManager/LanguageManager)
- **Feature 006:** Russian-Only Unified Menu (menu.* keys added in this feature)
- **Feature 007:** Missing Menu Localization Keys (menu system translations)

---

**Last Updated:** 2025-10-29  
**Version:** 1.4.0  
**Feature Branch:** `008-missing-localization-keys`  
**Status:** ✅ Complete and merged
