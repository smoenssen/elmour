package com.smoftware.elmour.maps;

/**
 * Created by steve on 9/14/19.
 */

public interface MapManagerObserver {
    enum MapManagerEvent {
        PLAYER_START_CHANGED,
        NONE
    }

    void onNotify(MapManagerEvent event, String value);
}
