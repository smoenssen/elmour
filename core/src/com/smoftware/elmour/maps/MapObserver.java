package com.smoftware.elmour.maps;

/**
 * Created by steve on 8/8/18.
 */

public interface MapObserver {
    public static enum MapEvent {
        SHAKE_CAM,
        DISPLAY_CONVERSATION,
        NONE
    }

    void onNotify(MapEvent event, String value);
}
