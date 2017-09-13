package com.smoftware.elmour;

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
        JOYSTICK_POSITION,
        NONE
    }

    void onNotify(final Entity entity, MobileControlEvent event, Object data);
}
