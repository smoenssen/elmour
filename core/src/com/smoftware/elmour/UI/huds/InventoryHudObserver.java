package com.smoftware.elmour.UI.huds;

public interface InventoryHudObserver {
    enum InventoryHudEvent {
        INVENTORY_HUD_SHOWN,
        INVENTORY_HUD_HIDDEN
    }

    void onNotify(InventoryHudEvent event);
}
