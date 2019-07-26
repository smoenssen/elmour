package com.smoftware.elmour.main;

import com.badlogic.gdx.math.MathUtils;

import java.util.Map;

/**
 * Created by steve on 10/3/18.
 */

public class PartyInventoryItem {
    private InventoryElement element;
    private int quantity;
    private int quantityInUse;

    public PartyInventoryItem() {

    }

    public PartyInventoryItem(InventoryElement element, int quantity, int quantityInUse) {
        this.element = element;
        this.quantity = quantity;
        this.quantityInUse = quantityInUse;
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

    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity, Object o) {
        if (!(o instanceof PartyInventory)) return;
        this.quantity = quantity;
    }

    public int getQuantityInUse() { return quantityInUse; }

    public int getQuantityAvailable() { return quantity - quantityInUse; }

    public void setQuantityInUse(int quantityInUse, Object o) {
        if (!(o instanceof PartyInventory)) return;
        this.quantityInUse = quantityInUse;
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

    public void setItemInUse(boolean inUse, Object o) {
        if (!(o instanceof PartyInventory)) return;

        if (inUse) {
            MathUtils.clamp(quantityInUse++, 0, quantity);
        }
        else {
            MathUtils.clamp(quantityInUse--, 0, quantity);
        }
    }

    public boolean isAvailable() {
        return (quantity > quantityInUse);
    }
}
