package com.smoftware.elmour;

import com.badlogic.gdx.utils.Array;

/**
 * Created by steve on 9/30/18.
 */

public class PartyInventory {
    private static final String TAG = PartyInventory.class.getSimpleName();

    private class PartyInventoryItem {
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
        String [] saItems = profileString.split("|");

        for (String item : saItems) {
            String [] saValues = item.split("|");

            InventoryElement.ElementID elementID = InventoryElement.ElementID.valueOf(saValues[0]);
            addItem(InventoryElementFactory.getInstance().getInventoryElement(elementID), Integer.parseInt(saValues[1]));
        }
    }

    public String getInventoryProfileString() {
        // return delimited string of inventory item ids and quantities
        String profileString = "";

        for (PartyInventoryItem item : list) {
            if (!profileString.equals("")) {
                profileString += "|";
            }
            String newItem = item.item.toString() + "," + Integer.toString(item.quantity);
            profileString += newItem;
        }
        return profileString;
    }

    public Array<PartyInventoryItem> getList() {
        return list;
    }

    public void addItem(InventoryElement item, int quantity) {
        // add item to list if it doesn't exist, otherwise update the quantity
        if (quantity <= 0) throw new IllegalArgumentException("item quantity must be > 0");

        boolean itemInList = false;
        for (PartyInventoryItem listItem : list) {
            if (listItem.item.id == item.id) {
                itemInList = true;
                listItem.quantity += quantity;
                break;
            }
        }

        if (!itemInList) {
            PartyInventoryItem itemToAdd = new PartyInventoryItem();
            itemToAdd.item = item;
            itemToAdd.quantity = quantity;
            list.add(itemToAdd);
        }
    }

    public void removeItem(InventoryElement item, int quantity) {
        // decrement number of items, remove totally from list if quantity reaches zero
        if (quantity <= 0) throw new IllegalArgumentException("item quantity must be > 0");

        int index = 0;
        for (PartyInventoryItem listItem : list) {
            if (listItem.item.id == item.id) {
                listItem.quantity -= quantity;

                if (listItem.quantity <= 0) {
                    list.removeIndex(index);
                }
                break;
            }

            index++;
        }
    }
}
