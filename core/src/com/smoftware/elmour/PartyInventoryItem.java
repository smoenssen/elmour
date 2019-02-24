package com.smoftware.elmour;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by steve on 10/3/18.
 */

public class PartyInventoryItem {
    private InventoryElement element;
    private int quantity;

    public PartyInventoryItem() {

    }

    public PartyInventoryItem(InventoryElement element, int quantity) {
        this.element = element;
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) return false;

        InventoryElement.ElementID other;

        if (object instanceof InventoryElement) {
            InventoryElement element = (InventoryElement)object;
            other = element.id;
        }
        else {
            Map.Entry entry = (Map.Entry) object;
            PartyInventoryItem o = (PartyInventoryItem) entry.getValue();
            other = o.element.id;
        }
        return this.element.id.equals(other);
    }

    public InventoryElement getElement() { return element; }

    public void setElement(InventoryElement element) { this.element = element; }

    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }

    public void increaseQuantity(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("item quantity must be > 0");
        this.quantity += quantity;
    }

    public void reduceQuantity(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("item quantity must be > 0");
        this.quantity -= quantity;
        if (this.quantity < 0)
            this.quantity = 0;
    }
}
