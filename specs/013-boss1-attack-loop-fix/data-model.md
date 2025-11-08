# Boss 1 Attack State Machine

This diagram illustrates the flow of the Boss 1 special attack, including the proposed fixes.

```mermaid
graph TD
    subgraph Idle
        A[Idle]
    end

    subgraph Special Attack
        B[Rising Animation]
        C[Pause at Apex]
        D[Cast Skulls]
        E[Descend]
    end

    subgraph Cooldown
        F[Cooldown]
    end

    A -- Special Attack Trigger --> B;
    B -- Animation Complete --> C;
    C -- Pause Complete --> D;
    D -- Skulls Cast --> E;
    E -- Descend Complete --> F;
    F -- Cooldown Finished --> A;

    B -- Stuck/Timeout --> F;
    C -- Stuck/Timeout --> F;
    D -- Stuck/Timeout --> F;
```

## State Descriptions

*   **Idle**: The boss is in its normal combat state, performing standard attacks.
*   **Rising Animation**: The boss rises into the air. If the animation gets stuck or times out, it will transition directly to Cooldown.
*   **Pause at Apex**: The boss pauses at the highest point of the animation. This is a new state to ensure the skull attack happens at the correct time.
*   **Cast Skulls**: The boss launches its wither skull attack.
*   **Descend**: The boss descends, invulnerable to damage.
*   **Cooldown**: A brief period after the special attack before returning to Idle.

This new state machine introduces a dedicated "Pause at Apex" state to ensure the skull casting is decoupled from the rising animation, preventing the loop. It also includes timeout transitions from all special attack states to the cooldown state as a fail-safe.