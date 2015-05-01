package com.noe.badger.event.handler;

import com.noe.badger.event.message.Achievement;

/**
 * Interface to handle unlocked achievement events.
 */
public interface IAchievementUnlockedHandler {

    /**
     * Callback method to receive notification about the unlocked achievements.
     *
     * @param achievement Unlocked achievement information as an {@link Achievement} instance.
     */
    void onUnlocked(Achievement achievement);
}
