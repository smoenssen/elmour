package com.smoftware.elmour.inventory;

public interface PartyInventoryObserver {
    public static enum PartyInventoryEvent {
        INVENTORY_ADDED,
        INVENTORY_REMOVED,
        INVENTORY_SWAP,
        INVENTORY_ITEM_USE_CHANGED
    }

    void onNotify(final PartyInventoryItem partyInventoryItem, final PartyInventoryEvent event);
    void onNotify(final PartyInventoryItem item1, final PartyInventoryItem item2, final PartyInventoryEvent event);
}
