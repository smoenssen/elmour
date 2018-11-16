package com.smoftware.elmour.UI;


import com.smoftware.elmour.Entity;

public interface StatusObserver {
    public static enum StatusEvent {
        UPDATED_GP,
        UPDATED_LEVEL,
        UPDATED_HP,
        UPDATED_MP,
        UPDATED_XP,
        LEVELED_UP,
        SHAKE_CAM
    }

    void onNotify(final int value, StatusEvent event);
    void onNotify(final Entity entity, final String value, StatusEvent event);
}
