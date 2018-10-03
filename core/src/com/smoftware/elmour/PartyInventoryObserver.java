package com.smoftware.elmour;

public interface PartyInventoryObserver {
    public static enum PartyInventoryEvent {
        INVENTORY_ADDED,
        INVENTORY_REMOVED
    }

    void onNotify(final String partyInventoryItem, final PartyInventoryEvent event);
}
