package com.smoftware.elmour.UI;

public interface PlayerHudObserver {
    enum PlayerHudEvent {
        SHOWING_POPUP,
        HIDING_POPUP,
        SHOWING_STATS_UI,
        HIDING_STATS_UI,
        SHOWING_MENU,
        HIDING_MENU
    }

    void onNotify(PlayerHudEvent event, String value);
}
