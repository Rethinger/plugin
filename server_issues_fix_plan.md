# Server Issues Fix Plan

**Date**: 2025-11-04  
**Priority**: P1 - Critical Issues Affecting Gameplay

## Issues Identified

### 1. Missing Localization Key (CRITICAL)
**Problem**: Server log shows repeated warnings: `Missing localization key: boss1.special_attack.warning`
**Root Cause**: The key exists in messages.yml but is incorrectly structured as a child of `wither_skull_attack` section instead of being under `boss1.special_attack`.

**Impact**: 
- Console spam during boss fights
- Players may not receive proper warning messages
- Poor user experience during boss special attacks

### 2. TAB Plugin Configuration Warnings (MEDIUM)
**Problems**:
- Missing configuration section: `placeholders.register-tab-expansion` (Boolean)
- Unknown properties in groups.yml: `customtagname`, `abovename`, `belowname`
- Unknown property in users.yml: `abovename`, `belowname`

**Impact**:
- Console warnings during server startup
- Potential TAB plugin functionality issues
- Configuration cleanup needed

## Fix Implementation Plan

### Phase 1: Localization Key Fix (COMPLETED)

#### ✅ 1.1 Fix Russian messages.yml structure
- **File**: `src/main/resources/messages.yml`
- **Change**: Moved `boss1.special_attack.warning` from under `wither_skull_attack` to proper `boss1.special_attack` section
- **Result**: Key now properly structured and accessible

#### ✅ 1.2 Create English messages file
- **File**: `src/main/resources/messages_en.yml`
- **Content**: Complete English translation of all Russian messages
- **Result**: English support now available for future per-player language feature

#### ✅ 1.3 Update MessageManager.java
- **File**: `src/main/java/com/mmmm/story/managers/MessageManager.java`
- **Changes**:
  - Added `loadMessageFile("messages_en.yml")` call
  - Updated `configCache` to store both "ru" and "en" configurations
  - Modified `getMessage(Player, String)` to use player's language preference
  - Added `getPlayerLanguage(Player)` helper method
- **Result**: System now supports bilingual messages with proper fallback

### Phase 2: TAB Plugin Configuration Fix (PENDING)

#### 🔄 2.1 Update TAB config.yml
- **File**: `plugins/TAB/config.yml`
- **Required Changes**:
  - Add `placeholders.register-tab-expansion: false` to config.yml section
  - Remove unknown properties from groups.yml and users.yml files
- **Impact**: Eliminates TAB plugin warnings during startup

#### 🔄 2.2 Verify TAB plugin functionality
- **Action**: Test TAB plugin after configuration fixes
- **Expected Result**: No warnings, proper TAB functionality

### Phase 3: Verification and Testing (PENDING)

#### 🔄 3.1 Test Boss Special Attack
- **Action**: Trigger Boss 1 special attack in-game
- **Expected Result**: 
  - No "Missing localization key" warnings in console
  - Proper warning message displayed to players
  - Message appears in correct language (Russian/English based on player)

#### 🔄 3.2 Test MessageManager Language Switching
- **Action**: Test with players having different client locales
- **Expected Result**: 
  - English players receive English messages
  - Russian players receive Russian messages
  - Proper fallback when translation missing

#### 🔄 3.3 Full Server Restart Test
- **Action**: Restart server and monitor startup logs
- **Expected Result**: 
  - No localization warnings
  - No TAB configuration warnings
  - All plugins load successfully

## Implementation Status

| Phase | Status | Notes |
|--------|--------|-------|
| 1.1 Russian messages.yml fix | ✅ COMPLETED | Key properly structured |
| 1.2 English messages_en.yml creation | ✅ COMPLETED | Full English translation |
| 1.3 MessageManager.java update | ✅ COMPLETED | Bilingual support added |
| 2.1 TAB config.yml update | 🔄 PENDING | Requires server file access |
| 2.2 TAB functionality verification | 🔄 PENDING | Depends on 2.1 |
| 3.1 Boss special attack test | 🔄 PENDING | Depends on 1.1-1.3 |
| 3.2 MessageManager language test | 🔄 PENDING | Depends on 1.1-1.3 |
| 3.3 Full server restart test | 🔄 PENDING | Depends on all previous phases |

## Technical Details

### Files Modified
1. `src/main/resources/messages.yml` - Fixed localization key structure
2. `src/main/resources/messages_en.yml` - Created English translation file
3. `src/main/java/com/mmmm/story/managers/MessageManager.java` - Added bilingual support

### Files Requiring Server Access
1. `plugins/TAB/config.yml` - TAB plugin configuration
2. `plugins/TAB/groups.yml` - TAB group configurations  
3. `plugins/TAB/users.yml` - TAB user configurations

### Risk Assessment
- **Low Risk**: Localization fixes are purely additive
- **Medium Risk**: TAB configuration changes require careful backup
- **Mitigation**: Backup existing TAB configurations before making changes

## Next Steps

1. **Immediate**: Test the implemented localization fixes in a development environment
2. **Server Access**: Apply TAB configuration fixes on the live server
3. **Validation**: Perform comprehensive testing of all fixed issues
4. **Documentation**: Update any relevant documentation if needed

## Success Criteria

- [ ] No "Missing localization key" warnings in server console during boss fights
- [ ] No TAB plugin configuration warnings during server startup
- [ ] Boss special attack warning messages display correctly to players
- [ ] English players receive English messages when appropriate
- [ ] All existing functionality preserved after fixes

## Notes

The localization system has been enhanced to support future per-player language selection while maintaining backward compatibility. The TAB plugin configuration issues are standard cleanup items that should be addressed during regular server maintenance.

**Estimated Time for Completion**: 2-3 hours (including testing and verification)