# Tasks: Fix NPC Despawn Visual Effects

- [x] 1. Cancel aura and idle tasks on despawn
  - NPCManager.startMessengerDespawn now cancels aura/idle and scheduled tasks at start (src/main/java/com/mmmm/story/managers/NPCManager.java:666)
  - removeNPC also cancels tracked tasks and timers (src/main/java/com/mmmm/story/managers/NPCManager.java:741)

- [x] 2. Remove player-location effects on despawn
  - DialogManager no longer spawns disappearance effects at player location; it only calls plugin.getNpcManager().despawnMessenger() (src/main/java/com/mmmm/story/managers/DialogManager.java:321)

- [x] 3. Centralize despawn trigger
  - DialogManager routes disappearance via NPCManager.despawnMessenger() (src/main/java/com/mmmm/story/managers/DialogManager.java:321)
  - NPCManager performs the 5s shrinking effect at the NPC’s location (src/main/java/com/mmmm/story/managers/NPCManager.java:684)

- [x] 4. Track one-off scheduled tasks
  - Added scheduledTasks map and trackTask/cancelScheduledTasks helpers (src/main/java/com/mmmm/story/managers/NPCManager.java:47)
  - Wrapped all runTaskLater calls in startMessengerAnimations with trackTask (multiple lines in file)

## Validation Steps

1) Spawn messenger and play dialog
- Use command or trigger that calls NPCManager.spawnMessenger and DialogManager.playDialogForAll("messenger.spawn").

2) Observe despawn at disappearance line
- Confirm effects occur only at NPC location; none at player location.
- Verify no lingering particles/sounds after NPC removal.

3) Early removal/regression
- Run removeNpcByName while messenger is active; ensure all animations/particles stop immediately and no residual tasks run.
- Reload/cleanup: call NPCManager.cleanup; ensure no lingering tasks or NPCs.

4) Compass regression
- Give direction marker via giveDirectionMarker; verify it resets after 5 minutes and is not tied to NPC tracking.
