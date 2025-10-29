# Quick Start: Missing Localization Keys Fix

**Feature**: 008-missing-localization-keys  
**Estimated Time**: 4-6 hours  
**Difficulty**: Easy  
**Prerequisites**: Basic YAML knowledge, Russian language proficiency

## TL;DR

1. Run validation script to find missing keys
2. Add Russian translations to `messages_ru.yml`
3. Add English translations to `messages_en.yml`
4. Test in-game to verify all messages display correctly
5. Commit changes

## Step-by-Step Guide

### Phase 1: Identify Missing Keys (30 minutes)

#### 1.1 Run Build-Time Validation

```powershell
# From repository root
.\.specify\scripts\validate-localization-keys.ps1

# Expected output:
# Validating localization keys...
# ✓ Found 127 key references in Java code
# ✓ Found 123 keys in messages_ru.yml
# ✗ Missing keys found!
#
# MISSING IN RUSSIAN:
#   - chest.search.searching
#   - chest.search.searching_item
#   - chest.search.nothing
#   - items.stabilization_core.name
```

#### 1.2 Review Server Logs

Check `logs/latest.log` for runtime warnings:

```
[WARN]: [ServerStoryPlugin] Missing localization key: chest.search.searching
[WARN]: [ServerStoryPlugin] Missing localization key: items.stabilization_core.name
```

#### 1.3 Create Missing Keys List

Document all missing keys in a checklist:

```
Missing Keys Checklist:
☐ chest.search.searching
☐ chest.search.searching_item
☐ chest.search.nothing
☐ items.stabilization_core.name
```

---

### Phase 2: Add Russian Translations (2-3 hours)

#### 2.1 Open Primary Language File

```powershell
# Open in VS Code
code src\main\resources\messages_ru.yml
```

#### 2.2 Locate Correct Section

Find the functional area for each key:

```yaml
# Chest search keys go under 'chest.search' section
chest:
  search:
    # Add missing keys here

# Item keys go under 'items.{item_id}' section
items:
  stabilization_core:
    # Add missing keys here
```

#### 2.3 Add Missing Translations

For each missing key, add the Russian translation:

```yaml
chest:
  search:
    searching: "Поиск..."
    searching_item: "Поиск {item}..."
    nothing: "Ничего не найдено"
    found: "Найдено: {count} предметов"

items:
  stabilization_core:
    name: "Ядро стабилизации"
    lore:
      - "&7Древний артефакт"
      - "&7Стабилизирует пространственные разрывы"
      - "&eРедкость: Легендарная"
```

**Translation Tips**:
- Use `{placeholder}` for dynamic values (item names, counts, etc.)
- Use `&` prefix for color codes: `&c` = red, `&e` = yellow, `&7` = gray
- Keep UI messages concise (under 50 characters)
- Match tone of existing translations (semi-formal adventure style)
- Use "..." (ellipsis) for ongoing actions ("Поиск...")

#### 2.4 Check Glossary for Consistency

Reference `specs/008-missing-localization-keys/glossary.md` for standard translations of common terms:

| English | Russian | Context |
|---------|---------|---------|
| Search | Поиск | Action verb |
| Searching | Поиск... (with ellipsis) | Ongoing action |
| Nothing found | Ничего не найдено | Empty result |
| Item | Предмет | Game item |
| Core | Ядро | Item component |

---

### Phase 3: Add English Translations (1-2 hours)

#### 3.1 Open Secondary Language File

```powershell
code src\main\resources\messages_en.yml
```

#### 3.2 Mirror Russian Structure

Maintain same hierarchical structure as Russian file:

```yaml
chest:
  search:
    searching: "Searching..."
    searching_item: "Searching for {item}..."
    nothing: "Nothing found"
    found: "Found: {count} items"

items:
  stabilization_core:
    name: "Stabilization Core"
    lore:
      - "&7Ancient artifact"
      - "&7Stabilizes spatial rifts"
      - "&eRarity: Legendary"
```

**Translation Tips**:
- Keep placeholder names identical: `{item}`, `{count}`, `{player}`, etc.
- Use same color codes as Russian version
- Match punctuation style (ellipsis, periods, exclamation marks)

---

### Phase 4: Validate Changes (30 minutes)

#### 4.1 Re-run Build Validation

```powershell
.\.specify\scripts\validate-localization-keys.ps1

# Expected output:
# Validating localization keys...
# ✓ Found 127 key references in Java code
# ✓ Found 127 keys in messages_ru.yml
# ✓ Found 127 keys in messages_en.yml
# ✓ All localization keys are present!
```

#### 4.2 Compile Plugin

```powershell
mvn clean package

# Should compile without errors
# Validation runs automatically in Maven lifecycle
```

#### 4.3 Check YAML Syntax

Ensure no syntax errors:

```powershell
# PowerShell YAML validator (if available)
Test-Yaml -Path src\main\resources\messages_ru.yml

# Or use online validator: yamllint.com
```

---

### Phase 5: Test In-Game (1 hour)

#### 5.1 Deploy to Test Server

```powershell
# Copy built JAR to test server
Copy-Item target\ServerStoryPlugin-1.4.0.jar C:\TestServer\plugins\

# Restart server
cd C:\TestServer
.\start.bat
```

#### 5.2 Test Russian Language (Primary)

**Chest Search Test**:
1. Join server
2. Trigger chest search feature
3. Verify message: "Поиск..." appears (not "chest.search.searching")
4. Search for specific item
5. Verify message: "Поиск {item}..." with correct item name
6. Complete search with no results
7. Verify message: "Ничего не найдено"

**Item Display Test**:
1. Obtain stabilization core item (`/give @p story:stabilization_core`)
2. Check inventory → verify item name is "Ядро стабилизации"
3. Hover over item → verify lore displays in Russian with correct colors

**Console Check**:
```
# Check logs for any remaining warnings
tail -f logs/latest.log | grep "Missing localization key"
# Should show NO results during normal gameplay
```

#### 5.3 Test English Language (Secondary)

**Note**: If per-player language selection not yet implemented, skip this section.

1. Switch player language to English: `/story lang en`
2. Repeat all tests from 5.2
3. Verify English translations appear correctly

#### 5.4 Edge Case Testing

- **Invalid key test**: Temporarily reference non-existent key in code, verify warning logs
- **Placeholder test**: Verify {item}, {count}, {player} placeholders are replaced correctly
- **Color code test**: Verify `&c`, `&e`, `&7` render as colors, not literal text
- **Multi-line test**: Verify item lore displays all lines correctly

---

### Phase 6: Final Validation & Commit (30 minutes)

#### 6.1 Complete Checklist

Review feature checklist from spec:

```markdown
✅ All chest.search.* keys added to both language files
✅ All items.*.name keys added to both language files
✅ Build validation passes with zero missing keys
✅ In-game testing shows no "Missing localization key" warnings
✅ Russian translations are accurate and consistent
✅ English translations are accurate and consistent
✅ Placeholders work correctly in dynamic messages
✅ Color codes render properly
```

#### 6.2 Update Translation Checklist

Mark feature complete in `TRANSLATION_TEST_CHECKLIST.md`:

```markdown
## Feature 008: Missing Localization Keys
- [x] All keys identified via automated scan
- [x] Russian translations added
- [x] English translations added
- [x] In-game testing completed
- [x] Zero runtime warnings
```

#### 6.3 Commit Changes

```powershell
git add src\main\resources\messages_ru.yml
git add src\main\resources\messages_en.yml
git add specs\008-missing-localization-keys\
git commit -m "feat(localization): Add missing translations for chest search and items

- Added chest.search.* keys (searching, searching_item, nothing)
- Added items.stabilization_core.name and lore
- Both Russian (primary) and English (secondary) translations complete
- Build validation passes with zero missing keys
- Resolves #008 missing localization keys issue"

git push origin 008-missing-localization-keys
```

---

## Common Issues & Solutions

### Issue: YAML Syntax Error

**Symptom**: Build fails with "could not parse YAML" error

**Solution**:
```powershell
# Check for common YAML mistakes:
# - Incorrect indentation (use 2 spaces, not tabs)
# - Missing colon after key
# - Unquoted strings with special characters
# - Mixed quotes (use double quotes consistently)

# Example FIX:
# ❌ Wrong:
chest:
search:searching: "Поиск..."  # Missing space after colon

# ✅ Correct:
chest:
  search:
    searching: "Поиск..."
```

### Issue: Color Codes Not Working

**Symptom**: Player sees `&cRed text` instead of red colored text

**Solution**: Verify MessageManager calls `ChatColor.translateAlternateColorCodes('&', message)`. This is already implemented - check if message is being sent through MessageManager.

### Issue: Placeholders Not Replaced

**Symptom**: Player sees `Searching for {item}...` with literal `{item}`

**Solution**: Ensure code passes replacement map to `getMessage()`:

```java
// ❌ Wrong:
messageManager.sendMessage(player, "chest.search.searching_item");

// ✅ Correct:
Map<String, String> replacements = Map.of("item", itemName);
messageManager.sendMessage(player, "chest.search.searching_item", replacements);
```

### Issue: Validation Script Not Found

**Symptom**: PowerShell error "script not found"

**Solution**:
```powershell
# Script is created in Phase 0 - check if it exists
Test-Path .\.specify\scripts\validate-localization-keys.ps1

# If missing, refer to research.md for script implementation
# Or run from repo root: .\specs\008-missing-localization-keys\research.md
```

---

## Time Estimates

| Phase | Task | Estimated Time |
|-------|------|----------------|
| 1 | Identify missing keys | 30 min |
| 2 | Add Russian translations | 2-3 hours |
| 3 | Add English translations | 1-2 hours |
| 4 | Validate changes | 30 min |
| 5 | In-game testing | 1 hour |
| 6 | Final validation & commit | 30 min |
| **Total** | | **4-6 hours** |

**Variables affecting time**:
- Number of missing keys (estimate ~20-50 keys)
- Translation complexity (simple messages vs narrative text)
- Testing thoroughness (basic smoke test vs full regression)
- Familiarity with YAML and localization workflow

---

## Success Criteria Checklist

From spec.md, verify all criteria met:

- ✅ **SC-001**: Zero "Missing localization key" warnings in server logs during testing
- ✅ **SC-002**: 100% of chest search interactions display translated messages in both languages
- ✅ **SC-003**: 100% of custom item names display translated text in both languages
- ✅ **SC-004**: Players complete all plugin features without encountering untranslated text
- ✅ **SC-005**: All identified missing keys from server logs are added to localization files

---

## Next Steps

After this feature is complete:

1. **Merge to main branch**: Create pull request from `008-missing-localization-keys` to `main`
2. **Deploy to production**: Update production server with new plugin version
3. **Monitor logs**: Watch for any new missing key warnings (should be zero)
4. **Update documentation**: Note completion in project README
5. **Plan next feature**: Consider automated prevention strategy for future keys

---

## Resources

- **Spec**: `specs/008-missing-localization-keys/spec.md`
- **Research**: `specs/008-missing-localization-keys/research.md`
- **Data Model**: `specs/008-missing-localization-keys/data-model.md`
- **API Contract**: `specs/008-missing-localization-keys/contracts/MessageManager-API.md`
- **Translation Glossary**: `specs/008-missing-localization-keys/glossary.md`
- **Test Checklist**: `TRANSLATION_TEST_CHECKLIST.md`
- **Constitution**: `.specify/memory/constitution.md` (Principle V)
