package com.smoftware.elmour;

import com.badlogic.gdx.utils.Array;

/**
 * Created by steve on 9/30/18.
 */

public class PartyInventory {
    private static final String TAG = PartyInventory.class.getSimpleName();

    public class PartyInventoryItem {
        public InventoryElement item;
        public int quantity;
    }

    private static PartyInventory partyInventory;
    private Array<PartyInventoryItem> list;

    private PartyInventory(){
        list = new Array<>();
    }

    public static final PartyInventory getInstance(){
        if( partyInventory == null){
            partyInventory = new PartyInventory();
        }
        return partyInventory;
    }

    public void setInventoryList(String profileString) {
        // create list based on delimited string of inventory item ids and quantities

    }

    public String getInventoryProfileString() {
        // return delimited string of inventory item ids and quantities
        String profileString = "";

        return profileString;
    }

    public Array<PartyInventoryItem> getList() {
        return list;
    }

    public void addItem(PartyInventoryItem itemToAdd) {
        // add item to list if it doesn't exist, otherwise update the quantity
        boolean itemInList = false;
        for (PartyInventoryItem listItem : list) {
            if (listItem.item.id == itemToAdd.item.id) {
                itemInList = true;
                listItem.quantity += itemToAdd.quantity;
                break;
            }
        }

        if (!itemInList) {
            list.add(itemToAdd);
        }
    }

    public void removeItem(PartyInventoryItem itemToRemove) {
        // decrement number of items, remove totally from list if quantity reaches zero
        int index = 0;
        for (PartyInventoryItem listItem : list) {
            if (listItem.item.id == itemToRemove.item.id) {
                listItem.quantity -= itemToRemove.quantity;

                if (listItem.quantity <= 0) {
                    list.removeIndex(index);
                }
                break;
            }

            index++;
        }
    }
}
