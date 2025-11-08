# NPC Despawn Visual Effects Fix

## Version 1.4.1 - NPC Despawn Improvements

### Fixed Issues
- **Fixed residual particles** remaining at NPC location after despawn
- **Fixed unintended player-centered visual effects** triggering during NPC despawn
- **Added idempotency** to prevent duplicate despawn operations
- **Enhanced particle cleanup** with configurable methods

### New Configuration Options
Added comprehensive despawn configuration under `npc.despawn:`:

```yaml
npc:
  despawn:
    visualEffects:
      enabled: true                    # Enable/disable despawn visual effects
      showParticles: true              # Show particle effects during despawn
      showPlayerEffects: false         # Show any player-centered effects (default: false)
      cleanupRadius: 15.0              # Radius to clean up particles after despawn
      animationDuration: 100           # Despawn animation duration in ticks (5 seconds)

    particleCleanup:
      enabled: true                    # Enable automatic particle cleanup
      immediateCleanup: true           # Clean particles immediately when despawn starts
      finalCleanup: true               # Final cleanup after despawn completes
      cleanupMethod: "override"        # Method: "override" (use barrier particles) or "wait"

    soundEffects:
      enabled: true                    # Enable despawn sound effects
      fadeOut: true                    # Fade sound volume over despawn duration
```

### Technical Improvements
- **Enhanced particle cleanup** with barrier particle override method
- **Idempotent despawn operations** to prevent duplicate effects
- **Configuration-driven behavior** for all despawn visual effects
- **Improved error handling** with graceful fallbacks
- **Better logging** for debugging despawn operations

### Behavioral Changes
- Default configuration now **prevents player-centered effects** during NPC despawn
- Despawn operations are now **idempotent** - multiple calls won't cause duplicate effects
- Particle cleanup happens **immediately** and again **after despawn completion**
- All visual effects can be **completely disabled** via configuration if needed

### Backwards Compatibility
- Existing `despawnMessenger()` method behavior remains the same
- New configuration uses sensible defaults that maintain current visual quality
- No breaking changes to existing API or dialog timing

### Testing
- Added unit tests for despawn idempotency and configuration handling
- Tests cover particle cleanup methods and error scenarios
- Validation tests for configuration parameter handling

## Usage Examples

### Disable All Despawn Effects
```yaml
npc:
  despawn:
    visualEffects:
      enabled: false
```

### Minimal Particle Cleanup
```yaml
npc:
  despawn:
    particleCleanup:
      cleanupMethod: "wait"
      immediateCleanup: false
      finalCleanup: false
```

### Larger Cleanup Radius
```yaml
npc:
  despawn:
    visualEffects:
      cleanupRadius: 25.0
```