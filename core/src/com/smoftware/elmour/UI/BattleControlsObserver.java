package com.smoftware.elmour.UI;

/**
 * Created by steve on 3/10/18.
 */

public interface BattleControlsObserver {
    public static enum BattleControlEvent{
        A_BUTTON_PRESSED,
        A_BUTTON_RELEASED,
        B_BUTTON_PRESSED,
        B_BUTTON_RELEASED,
        D_PAD_UP_PRESSED,
        D_PAD_UP_RELEASED,
        D_PAD_DOWN_PRESSED,
        D_PAD_DOWN_RELEASED,
        NONE
    }

    void onBattleControlsNotify(Object data, BattleControlEvent event);
}
