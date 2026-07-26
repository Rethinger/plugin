package com.mmmm.story.data;

import com.mmmm.story.data.PlayerSettings.DialogSpeed;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerSettingsTest {

    @Test
    void defaultsToVisibleDialogsAtNormalSpeed() {
        PlayerSettings settings = new PlayerSettings();

        assertTrue(settings.isShowDialogs());
        assertEquals(DialogSpeed.NORMAL, settings.getDialogSpeed());
        assertEquals(1.0, settings.getSpeedMultiplier());
    }

    @Test
    void toggleDialogsFlipsBackAndForth() {
        PlayerSettings settings = new PlayerSettings();

        settings.toggleDialogs();
        assertFalse(settings.isShowDialogs());

        settings.toggleDialogs();
        assertTrue(settings.isShowDialogs());
    }

    @Test
    void cycleSpeedWrapsThroughEveryValue() {
        PlayerSettings settings = new PlayerSettings(true, DialogSpeed.SLOW);

        settings.cycleSpeed();
        assertEquals(DialogSpeed.NORMAL, settings.getDialogSpeed());

        settings.cycleSpeed();
        assertEquals(DialogSpeed.FAST, settings.getDialogSpeed());

        settings.cycleSpeed();
        assertEquals(DialogSpeed.SLOW, settings.getDialogSpeed());
    }

    @Test
    void speedMultiplierTracksTheSelectedSpeed() {
        PlayerSettings settings = new PlayerSettings();

        settings.setDialogSpeed(DialogSpeed.SLOW);
        assertEquals(1.5, settings.getSpeedMultiplier());

        settings.setDialogSpeed(DialogSpeed.FAST);
        assertEquals(0.75, settings.getSpeedMultiplier());
    }

    @Test
    void fromStringIsCaseInsensitiveAndFallsBackToNormal() {
        assertEquals(DialogSpeed.FAST, DialogSpeed.fromString("fast"));
        assertEquals(DialogSpeed.SLOW, DialogSpeed.fromString("SlOw"));
        assertEquals(DialogSpeed.NORMAL, DialogSpeed.fromString("not-a-speed"));
    }
}
