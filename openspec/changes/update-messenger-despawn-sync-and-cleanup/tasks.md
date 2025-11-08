## 1. Implementation
- [ ] 1.1 Align dialog trigger: call `NPCManager.despawnMessenger()` on the exact tick of the “*Посланник исчезает в тумане*” line (respecting speed multipliers).
- [ ] 1.2 Ensure `NPCManager.despawnMessenger()` cancels aura/idle and scheduled emitters immediately and prevents duplicates.
- [ ] 1.3 Perform enhanced despawn animation and then clear particles in a small radius at the last location.
- [ ] 1.4 Add logging to confirm synchronization and cleanup path executed.

## 2. Validation
- [ ] 2.1 Manual: Spawn Messenger, play dialog; verify vanish begins during the line, not before/after.
- [ ] 2.2 Manual: Observe that no particles remain at the Messenger spot 1s after disappearance completes.
- [ ] 2.3 Regression: Verify no particles appear at player location when NPC despawns.
- [ ] 2.4 Configs: Test with different dialog speeds/localizations where the text matches the trigger.
