# OpenCode Commands Guide

## Quick Switch Commands

This guide provides commands to quickly switch between different OpenCode modes with specific AI models.

## Available Commands

### 1. Building Mode (GLM-6)
**Command:** `building`

Switches to Building mode with the GLM-6 model for active development.

**Usage:**
```bash
opencode building
```

**Capabilities:**
- Full file editing and creation
- Bash command execution
- Read access to all files
- Search and glob capabilities
- Code implementation and testing

**When to use:**
- Writing new code
- Modifying existing files
- Running build commands
- Executing tests
- Making changes to the codebase

### 2. Plan Mode (Gemini 2.5 Pro)
**Command:** `plan`

Switches to Plan mode with the Gemini 2.5 Pro model for analysis and planning.

**Usage:**
```bash
opencode plan
```

**Capabilities:**
- Read-only access to files
- Search and glob capabilities
- Analysis and planning
- Code review and suggestions

**Restrictions:**
- No file writing or editing
- No bash command execution
- No direct code modifications

**When to use:**
- Analyzing codebase structure
- Planning implementation steps
- Reviewing code quality
- Designing architecture
- Creating documentation
- Investigating issues without making changes

## Configuration Details

The configuration is stored in `opencode.json` and includes:

### Mode Configurations
- **Building Mode**: Uses `glm-6` with full tool access
- **Plan Mode**: Uses `gemini-2.5-pro` with read-only tools

### Agent Configurations
- **Building Agent**: Primary mode with full development capabilities
- **Plan Agent**: Primary mode with analysis-only capabilities

## Additional OpenCode Commands

### List Available Models
```bash
opencode models
```

### Switch Modes Interactively
Use `TAB` in the OpenCode interface to switch between modes.

### Start OpenCode Server
```bash
opencode serve
```

### Start ACP Server for IDE Integration
```bash
opencode acp
```

## File Structure

```
.
├── opencode.json                    # Main configuration file
└── .opencode/
    └── command/
        ├── building.md              # Building mode command
        └── plan.md                  # Plan mode command
```

## Customization

You can modify the configurations in `opencode.json` to:
- Change model names
- Adjust tool permissions
- Modify temperature settings
- Add new modes and commands

## Notes

- Make sure the specified models (`glm-6` and `gemini-2.5-pro`) are available in your OpenCode setup
- You may need to configure authentication for the respective model providers
- The commands will appear in the OpenCode TUI for easy access