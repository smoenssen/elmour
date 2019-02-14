package com.smoftware.elmour.UI;

public interface PlayerHudObserver {
    enum PlayerHudEvent {
        //not used currently
    }

    void onNotify(PlayerHudEvent event);
}
