package com.smoftware.elmour.UI.huds;

public interface InventoryHudSubject {
    void addObserver(InventoryHudObserver observer);
    void removeObserver(InventoryHudObserver observer);
    void notify(final InventoryHudObserver.InventoryHudEvent event);
}
