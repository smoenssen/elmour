package com.smoftware.elmour.UI.huds;

/**
 * Created by steve on 5/14/19.
 */

public interface QuestHudSubject {
    void addObserver(QuestHudObserver observer);
    void removeObserver(QuestHudObserver observer);
    void notify(final QuestHudObserver.QuestHudEvent event);
}
