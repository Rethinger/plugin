## 1. Implementation
- [x] 1.1 Update timing logic (remove peak delay or extend hover)
- [x] 1.2 Implement skull gather-to-boss choreography
- [x] 1.3 Implement downward hemisphere release and spherical formation flight
- [x] 1.4 Enforce warrior-summon window and minimal spacing between specials
- [x] 1.5 Add config toggles and defaults
- [x] 1.6 Tests and in-game validation checklist

## 2. Validation
- [x] Safe zones show, then launch starts with no extra delay (<=0.2s), or configured hover applies
- [x] Skulls visually gather from ground to boss, then emit downward hemisphere
- [x] Skulls approximate spherical distribution during flight; no block damage
- [x] At least one warrior-summon window occurs between specials; min spacing respected
- [x] Configs documented and defaulted; logs confirm state transitions
