package com.smoftware.elmour;

public interface ComponentObserver {
    public static enum ComponentEvent {
        LOAD_CONVERSATION,
        SHOW_CONVERSATION,
        HIDE_CONVERSATION,
        DID_INITIAL_INTERACTION,
        DID_INTERACTION,
        FINISHED_INTERACTION,
        QUEST_LOCATION_DISCOVERED,
        ENEMY_SPAWN_LOCATION_CHANGED,
        PLAYER_HAS_MOVED,
        CUTSCENE_ACTIVATED
    }

    void onNotify(final String value, ComponentEvent event);
}
