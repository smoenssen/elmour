package com.smoftware.elmour.UI.huds;

/**
 * Created by steve on 5/14/19.
 */

public interface QuestHudObserver {
    enum QuestHudEvent {
        QUEST_HUD_SHOWN,
        QUEST_HUD_HIDDEN,
        QUEST_LIST_UPDATED
    }

    void onNotify(QuestHudEvent event);
}
