package com.mmmm.story;

/**
 * Implemented by listeners and managers that own boss bars, spawned entities or other
 * runtime state which must be released when the plugin shuts down.
 *
 * <p>Bukkit cancels a plugin's scheduler tasks on disable by itself, but nothing else:
 * boss bars stay pinned to the screens of players who survive a {@code /reload}, and
 * summoned bosses, warrior skeletons and VFX armour stands stay in the world with no
 * listener tracking them. After the reload a fresh listener is constructed that knows
 * nothing about them.
 *
 * <p>Everything registered through {@link MmmmStoryPlugin#registerListeners()} is
 * collected into a single list, which {@link MmmmStoryPlugin#onDisable()} walks in
 * reverse registration order.
 */
public interface Cleanable {

    /**
     * Cancel tasks, hide boss bars and drop any other runtime state.
     *
     * <p>Must be safe to call more than once - {@code cleanup()} also runs when a boss
     * dies normally. The shutdown loop logs and continues on failure so that one broken
     * component cannot block the rest.
     */
    void cleanup();
}
