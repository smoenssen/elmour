package com.smoftware.elmour.inventory;

public interface PartyKeysObserver {
    public static enum PartyKeysEvent {
        KEY_ITEM_ADDED,
        KEY_ITEM_REMOVED,
        KEY_ITEM_SWAP,
    }

    void onNotify(final PartyKeyItem partyKeyItem, final PartyKeysEvent event);
    void onNotify(final PartyKeyItem item1, final PartyKeyItem item2, final PartyKeysEvent event);
}
