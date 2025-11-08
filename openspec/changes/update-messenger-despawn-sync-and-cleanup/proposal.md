# Change: Synchronize Messenger despawn with dialog and clear residual particles

## Why
Players expect the Messenger to vanish exactly during the line “*Посланник исчезает в тумане*”, and to leave no lingering particles at the disappearance spot. Currently timing/cleanup can drift, leaving residual visuals.

## What Changes
- Synchronize despawn start with the specific dialog line (same tick) across speeds/localizations.
- Route all effects via `NPCManager.despawnMessenger()` for consistent behavior and logging.
- Ensure particle cleanup at the Messenger’s last location: cancel aura/idle tasks, stop scheduled particle emitters, and clear the area so no particles remain after the effect.
- Preserve a short, intentional “mist vanish” effect, but remove any lingering particles immediately after the animation completes.

## Impact
- Affected specs: `dialog-despawn-trigger`, `npc-despawn-cleanup`
- Affected code: `src/main/java/com/mmmm/story/managers/DialogManager.java`, `src/main/java/com/mmmm/story/managers/NPCManager.java`
- User-visible: Vanish begins exactly on the spoken line; no residual particles remain at the spot afterwards.
