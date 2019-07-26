package com.smoftware.elmour.components;

import com.smoftware.elmour.entities.Entity;

public interface ComponentObserver {
    public static enum ComponentEvent {
        LOAD_CONVERSATION,
        SHOW_CONVERSATION,
        HIDE_CONVERSATION,
        DID_INITIAL_INTERACTION,
        DID_INTERACTION,
        FINISHED_INTERACTION,
        QUEST_LOCATION_DISCOVERED,
        CONVERSATION_CONFIG,
        ENEMY_SPAWN_LOCATION_CHANGED,
        PLAYER_HAS_MOVED,
        CUTSCENE_ACTIVATED
    }

    void onNotify(final String value, ComponentEvent event);

    void onNotify(final Entity entity, String value, ComponentEvent event);
}
