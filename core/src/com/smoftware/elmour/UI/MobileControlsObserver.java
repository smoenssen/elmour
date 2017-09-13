package com.smoftware.elmour.UI;

import com.smoftware.elmour.Entity;

/**
 * Created by steve on 9/12/17.
 */

public interface MobileControlsObserver {
    public static enum MobileControlEvent{
        A_BUTTON_PRESSED,
        A_BUTTON_RELEASED,
        B_BUTTON_PRESSED,
        B_BUTTON_RELEASED,
        TOUCHPAD_POSITION,
        NONE
    }

    Entity or new MobileControlsValues??
    void onNotify(final Entity enemyEntity, MobileControlEvent event);
}
