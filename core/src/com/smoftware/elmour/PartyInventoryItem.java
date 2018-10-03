package com.smoftware.elmour;

/**
 * Created by steve on 10/3/18.
 */

public class PartyInventoryItem {
    private InventoryElement item;
    private int quantity;

    public PartyInventoryItem() {

    }

    public PartyInventoryItem(InventoryElement item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public InventoryElement getItem() { return item; }

    public void setItem(InventoryElement item) { this.item = item; }

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
