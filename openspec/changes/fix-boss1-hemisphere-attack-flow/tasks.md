## 1. Implementation
- [x] 1.1 Define phase timings (rise/formation/safe zones/launch)
- [x] 1.2 Enforce phase-aware timeouts and recovery
- [x] 1.3 Implement hemisphere geometry (radius, arc, count)
- [x] 1.4 Ensure safe zones per-player with shared anchors
- [x] 1.5 Update logs for each phase + errors
- [x] 1.6 Add config toggles and sane defaults

## 2. Validation
- [ ] Boss visibly rises; hover clearly noticeable (>= 0.8s)
- [ ] Skulls form a clear hemisphere; none go straight up
- [ ] Safe zones appear reliably; never zero for non-empty players
- [ ] Launch begins within expected window after zones appear
- [ ] No premature global timeout; phase recovery works
- [ ] Logs trace phases and include durations
