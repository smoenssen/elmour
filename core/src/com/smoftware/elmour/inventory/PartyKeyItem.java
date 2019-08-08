package com.smoftware.elmour.inventory;

import java.util.Map;

/**
 * Created by steve on 6/9/2019.
 */

public class PartyKeyItem {
    private KeyItem keyItem;
    private int quantity;

    public PartyKeyItem() {

    }

    public PartyKeyItem(KeyItem keyItem, int quantity) {
        this.keyItem = keyItem;
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) return false;

        KeyItem.ID other;

        if (object instanceof KeyItem) {
            KeyItem keyItem = (KeyItem)object;
            other = keyItem.id;
        }
        else {
            Map.Entry entry = (Map.Entry) object;
            PartyKeyItem o = (PartyKeyItem) entry.getValue();
            other = o.keyItem.id;
        }
        return this.keyItem.id.equals(other);
    }

    public KeyItem getKeyItem() { return keyItem; }

    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity, Object o) {
        if (!(o instanceof PartyInventory)) return;
        this.quantity = quantity;
    }

    public void increaseQuantity(int quantity, Object o) {
        if (!(o instanceof PartyInventory)) return;
        if (quantity <= 0) throw new IllegalArgumentException("item quantity must be > 0");
        this.quantity += quantity;
    }

    public void reduceQuantity(int quantity, Object o) {
        if (!(o instanceof PartyInventory)) return;
        if (quantity <= 0) throw new IllegalArgumentException("item quantity must be > 0");
        this.quantity -= quantity;
        if (this.quantity < 0)
            this.quantity = 0;
    }
}
