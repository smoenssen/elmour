package com.smoftware.elmour;

public interface ComponentObserver {
    public static enum ComponentEvent {
        LOAD_CONVERSATION,
        SHOW_CONVERSATION,
        HIDE_CONVERSATION,
        SHOW_POPUP,
        HIDE_POPUP,
        UPDATE_POPUP,
        QUEST_LOCATION_DISCOVERED,
        ENEMY_SPAWN_LOCATION_CHANGED,
        PLAYER_HAS_MOVED
    }

    void onNotify(final String value, ComponentEvent event);
}
