package com.smoftware.elmour.UI.huds;

public interface PlayerHudSubject {
    void addObserver(PlayerHudObserver observer);
    void removeObserver(PlayerHudObserver observer);
    void notify(final PlayerHudObserver.PlayerHudEvent event, String value);
}
