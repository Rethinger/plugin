## 1. Implementation
- [x] 1.1 Audit NPC despawn flow to identify particle/effect sources (NPC location and player-centered).
- [x] 1.2 Add explicit cleanup to cancel/stop particle schedulers and remove emitters on NPC despawn.
- [x] 1.3 Guard player FX on NPC despawn behind a config flag (default: disabled) or remove if unintended.
- [x] 1.4 Ensure idempotency: multiple despawn calls do not re-trigger effects or leave residues.
- [x] 1.5 Add unit tests/mocks for despawn sequence verifying no particles remain and no player FX fire.
- [x] 1.6 Verify no regressions for boss/NPC scripted effects that intentionally outlive NPCs.
- [x] 1.7 Update docs/changelog if behavior becomes configurable.

## 2. Validation
- [x] 2.1 Reproduce: Spawn NPC, despawn; confirm no residual particles at NPC position.
- [x] 2.2 Reproduce: Despawn near player; confirm no unintended player-centered FX occur.
- [x] 2.3 Stress: Rapid spawn/despawn loops do not leak effects or produce flicker.
