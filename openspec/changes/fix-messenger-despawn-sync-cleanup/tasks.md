## 1. Dialog Trigger Fix
- [x] 1.1 Update DialogManager to use exact text matching for despawn trigger
- [x] 1.2 Ensure despawn happens immediately when the line appears, not after
- [x] 1.3 Add debug logging for despawn trigger timing

## 2. Particle Cleanup Enhancement
- [x] 2.1 Improve particle cleanup radius and method in NPCManager
- [x] 2.2 Add immediate particle clearing when despawn starts
- [x] 2.3 Ensure final cleanup happens after despawn completes

## 3. Despawn Animation Timing
- [x] 3.1 Review and adjust despawn animation duration
- [x] 3.2 Ensure NPC disappears smoothly while clearing particles
- [x] 3.3 Test despawn timing with dialog synchronization

## 4. Testing and Validation
- [x] 4.1 Test messenger despawn with different dialog speeds
- [x] 4.2 Verify no residual particles remain after despawn
- [x] 4.3 Validate timing matches dialog line exactly