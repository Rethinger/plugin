# Implementation Plan: Missing Localization Keys Throughout Plugin

**Branch**: `008-missing-localization-keys` | **Date**: October 29, 2025 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/008-missing-localization-keys/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

This feature addresses missing localization keys throughout the ServerStoryPlugin by:
1. Identifying all missing keys through automated source code scanning
2. Adding complete Russian and English translations for all missing keys
3. Implementing build-time and runtime validation to prevent future missing keys
4. Ensuring Russian as the primary language with technical key names displayed when translations are missing

Primary technical approach: Static code analysis to extract all localization key references, comparison with existing YAML files, and systematic addition of missing translations.

## Technical Context

**Language/Version**: Java 21 (Paper API 1.21.1-R0.1-SNAPSHOT)
**Primary Dependencies**: Paper API (provided), Bukkit Configuration API (for YAML), existing LanguageManager/MessageManager from feature 005
**Storage**: YAML files (messages_ru.yml, messages_en.yml, dialogs_ru.yml, dialogs_en.yml)
**Testing**: Manual gameplay testing + automated key validation script
**Target Platform**: Paper Minecraft Server 1.21.x
**Project Type**: Single Minecraft plugin project
**Performance Goals**: Zero runtime overhead for key validation (build-time checks), instant message retrieval from pre-loaded YAML
**Constraints**: Must not break existing translations, must maintain YAML format compatibility, zero downtime deployment
**Scale/Scope**: ~100-200 localization keys across all plugin features (chest search, items, dialogs, menus, commands)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| **I. Java 21 & Paper API Exclusivity** | ✅ PASS | No code changes to core plugin structure; only YAML file additions |
| **II. Singleton Plugin Pattern** | ✅ PASS | No changes to plugin instance management |
| **III. Manager-Based Service Layer** | ✅ PASS | Existing MessageManager/LanguageManager handle localization; no new managers needed |
| **IV. YAML-Only Data Storage** | ✅ PASS | All translations stored in YAML files (messages_ru.yml, messages_en.yml) |
| **V. Localization-First Message Design** | ✅ **CORE FEATURE** | This feature directly enforces this principle by completing all missing translations |
| **VI. Dialog System with Personalization** | ✅ PASS | Dialog translations added to dialogs_ru.yml, dialogs_en.yml |
| **VII. Protection of Story Items** | ✅ PASS | Item name translations added (items.*.name keys) |

**Gate Result**: ✅ **APPROVED** - This feature directly supports Constitution Principle V and requires no architectural changes.

## Project Structure

### Documentation (this feature)

```text
specs/008-missing-localization-keys/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   └── MessageManager-API.md  # API contract for localization methods
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/main/
├── java/com/mmmm/story/
│   └── managers/
│       ├── MessageManager.java    # Existing - handles message localization
│       └── LanguageManager.java   # Existing - handles per-player language
└── resources/
    ├── messages_ru.yml            # Russian translations (PRIMARY) - TO UPDATE
    ├── messages_en.yml            # English translations - TO UPDATE
    ├── dialogs_ru.yml             # Russian dialog translations - TO UPDATE
    └── dialogs_en.yml             # English dialog translations - TO UPDATE

target/classes/                     # Build output - contains compiled resources

.specify/scripts/                   # Build-time validation scripts
└── validate-localization-keys.ps1  # TO CREATE - scans code for missing keys
```

**Structure Decision**: This is a single Java plugin project following the existing Manager-Based Service Layer pattern. All changes are limited to YAML resource files and adding a build-time validation script. No new Java classes required - existing MessageManager and LanguageManager already handle localization.

## Phase 0: Research & Technical Decisions

See [research.md](./research.md) for detailed analysis of:
- Static code analysis approaches for Java localization key extraction
- YAML validation strategies (build-time vs runtime)
- Best practices for Minecraft plugin localization
- Translation workflow and quality assurance methods

## Phase 1: Design & Contracts

**Status**: ✅ Complete

### Artifacts Generated

- **[data-model.md](./data-model.md)**: Defines LocalizationKey, Translation, JavaSourceReference, LocalizationFile entities and validation metadata
- **[contracts/MessageManager-API.md](./contracts/MessageManager-API.md)**: Documents existing MessageManager API, YAML file structure, and integration points
- **[quickstart.md](./quickstart.md)**: Step-by-step implementation guide with time estimates and testing procedures

### Key Design Decisions

1. **No Code Changes Required**: Existing MessageManager and LanguageManager already support all needed functionality
2. **YAML-Only Changes**: Implementation focused entirely on adding missing key-value pairs to resource files
3. **Build-Time Validation**: PowerShell script runs in Maven validate phase to prevent deployment with missing keys
4. **Russian-Primary Strategy**: All keys MUST exist in messages_ru.yml (build fails if missing), English keys generate warnings only
5. **Technical Key Display**: When translation missing, system displays key name (not English fallback) to make issues immediately visible

### Re-Evaluation of Constitution Check

| Principle | Post-Design Status | Notes |
|-----------|-------------------|-------|
| **I. Java 21 & Paper API Exclusivity** | ✅ PASS | No Java code changes required |
| **II. Singleton Plugin Pattern** | ✅ PASS | No changes to plugin architecture |
| **III. Manager-Based Service Layer** | ✅ PASS | Leverages existing MessageManager/LanguageManager |
| **IV. YAML-Only Data Storage** | ✅ PASS | Pure YAML addition, no new storage mechanisms |
| **V. Localization-First Message Design** | ✅ **ENFORCED** | This feature completes localization coverage |
| **VI. Dialog System** | ✅ PASS | Dialog keys added to dialogs_ru.yml/dialogs_en.yml |
| **VII. Story Item Protection** | ✅ PASS | Item name translations preserve tagging system |

**Final Gate Result**: ✅ **APPROVED** - Design aligns perfectly with all constitution principles.

## Phase 2: Task Breakdown

Phase 2 is handled by the `/speckit.tasks` command (NOT created by `/speckit.plan`).

See `tasks.md` for detailed implementation tasks (generated after this plan is complete).

---

## Implementation Summary

### What This Feature Does

Completes the localization coverage for the ServerStoryPlugin by:
1. Identifying all missing translation keys through automated code scanning
2. Adding complete Russian (primary) and English (secondary) translations
3. Implementing build-time validation to prevent future missing keys
4. Ensuring proper fallback behavior (display technical key names when translations missing)

### Technical Approach

- **No Code Changes**: Leverage existing MessageManager and LanguageManager infrastructure
- **YAML-Only Updates**: Add missing key-value pairs to messages_ru.yml and messages_en.yml
- **Automated Discovery**: PowerShell script scans Java source for getMessage() calls
- **Build Integration**: Maven exec plugin runs validation during build lifecycle
- **Quality Assurance**: Translation glossary ensures terminology consistency

### Key Files Modified

```
src/main/resources/
├── messages_ru.yml       (PRIMARY - add ~20-50 missing keys)
├── messages_en.yml       (SECONDARY - add ~20-50 missing keys)
├── dialogs_ru.yml        (if dialog keys missing)
└── dialogs_en.yml        (if dialog keys missing)

.specify/scripts/
└── validate-localization-keys.ps1  (NEW - automated validation script)

pom.xml                    (MODIFIED - add Maven exec plugin for validation)
```

### Estimated Effort

- **Total Time**: 4-6 hours
- **Complexity**: Low (YAML edits only, no code changes)
- **Risk**: Very Low (backwards compatible, non-breaking changes)
- **Testing**: Manual gameplay walkthrough + automated validation

### Success Metrics

1. ✅ Zero "Missing localization key" warnings in server logs
2. ✅ 100% translation coverage for Russian (primary language)
3. ✅ 100% translation coverage for English (secondary language)
4. ✅ Build fails if new missing keys introduced in future
5. ✅ All user-facing text displays properly translated content

---

## Next Steps

1. **Run `/speckit.tasks`**: Generate detailed task breakdown with sub-tasks and dependencies
2. **Begin Implementation**: Follow quickstart.md for step-by-step execution
3. **Continuous Validation**: Run validation script frequently during development
4. **Testing**: Complete all test scenarios from quickstart.md before merging

---

## References

- **Specification**: [spec.md](./spec.md)
- **Research**: [research.md](./research.md)
- **Data Model**: [data-model.md](./data-model.md)
- **API Contract**: [contracts/MessageManager-API.md](./contracts/MessageManager-API.md)
- **Quick Start**: [quickstart.md](./quickstart.md)
- **Constitution**: `.specify/memory/constitution.md` - Principle V (Localization-First)
- **Feature 005**: Per-player i18n system (dependency)
- **Feature 006**: Russian-only unified menu (related context)
