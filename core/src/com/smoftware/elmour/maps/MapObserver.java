package com.smoftware.elmour.maps;

/**
 * Created by steve on 8/8/18.
 */

public interface MapObserver {
    public static enum MapEvent {
        SHAKE_CAM,
        NONE
    }

    void onNotify(MapEvent event);
}
