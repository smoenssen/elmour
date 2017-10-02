package com.smoftware.elmour;

public interface ComponentObserver {
    public static enum ComponentEvent {
        LOAD_CONVERSATION,
        SHOW_CONVERSATION,
        HIDE_CONVERSATION,
        SIGN_POPUP_INITITIALIZE,
        SIGN_POPUP_INTERACT,
        SIGN_POPUP_HIDE,
        QUEST_LOCATION_DISCOVERED,
        ENEMY_SPAWN_LOCATION_CHANGED,
        PLAYER_HAS_MOVED
    }

    void onNotify(final String value, ComponentEvent event);
}
