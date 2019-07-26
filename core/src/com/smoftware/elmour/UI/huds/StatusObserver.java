package com.smoftware.elmour.UI.huds;


import com.smoftware.elmour.Entity;

public interface StatusObserver {
    public static enum StatusEvent {
        UPDATED_DIBS,
        UPDATED_LEVEL,
        LEVELED_UP,
        UPDATED_XP,
        UPDATED_PARTY_XP,
        UPDATED_HP,
        UPDATED_HP_MAX,
        UPDATED_MP,
        UPDATED_MP_MAX,
        UPDATED_ATK,
        UPDATED_MATK,
        UPDATED_DEF,
        UPDATED_MDEF,
        UPDATED_SPD,
        UPDATED_ACC,
        UPDATED_LCK,
        UPDATED_AVO,
        IS_REVIVED,
        SHAKE_CAM
    }

    void onNotify(final int value, StatusEvent event);
    void onNotify(final Entity entity, final int value, StatusEvent event);
}
