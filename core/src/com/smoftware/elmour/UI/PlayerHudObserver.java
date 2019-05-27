package com.smoftware.elmour.UI;

public interface PlayerHudObserver {
    enum PlayerHudEvent {
        SHOWING_POPUP,
        HIDING_POPUP,
        KEY_ITEM_FETCHED
    }

    void onNotify(PlayerHudEvent event, String value);
}
