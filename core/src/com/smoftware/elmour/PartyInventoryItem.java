package com.smoftware.elmour;

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
    }
}
