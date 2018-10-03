package com.smoftware.elmour;

import com.badlogic.gdx.utils.Array;

/**
 * Created by steve on 9/30/18.
 */

public class PartyInventory {
    private static final String TAG = PartyInventory.class.getSimpleName();

    private static PartyInventory partyInventory;
    private Array<PartyInventoryItem> list;
    public final String PROPERTY_NAME = "partyInventory";
    public final String ITEM_DELIMITER = ";";
    public final String VALUE_DELIMITER = ",";

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
        String [] saItems = profileString.split(ITEM_DELIMITER);

        for (String item : saItems) {
            String [] saValues = item.split(VALUE_DELIMITER);

            InventoryElement.ElementID elementID = InventoryElement.ElementID.valueOf(saValues[0]);
            addItem(InventoryElementFactory.getInstance().getInventoryElement(elementID), Integer.parseInt(saValues[1]));
        }
    }

    public String getInventoryProfileString() {
        // return delimited string of inventory item ids and quantities
        String profileString = "";

        for (PartyInventoryItem item : list) {
            if (!profileString.equals("")) {
                profileString += ITEM_DELIMITER;
            }
            String newItem = item.getItem().id.toString() + VALUE_DELIMITER + Integer.toString(item.getQuantity());
            profileString += newItem;
        }
        return profileString;
    }

    public Array<PartyInventoryItem> getFullList() {
        return list;
    }

    public void addItem(InventoryElement item, int quantity) {
        // add item to list if it doesn't exist, otherwise update the quantity
        boolean itemInList = false;
        for (PartyInventoryItem listItem : list) {
            if (listItem.getItem().id == item.id) {
                itemInList = true;
                listItem.increaseQuantity(quantity);
                break;
            }
        }

        if (!itemInList) {
            PartyInventoryItem itemToAdd = new PartyInventoryItem(item, quantity);
            list.add(itemToAdd);
        }
    }

    public void removeItem(InventoryElement item, int quantity) {
        // decrement number of items, remove totally from list if quantity reaches zero
        int index = 0;
        for (PartyInventoryItem listItem : list) {
            if (listItem.getItem().id == item.id) {
                listItem.reduceQuantity(quantity);

                if (listItem.getQuantity() <= 0) {
                    list.removeIndex(index);
                }
                break;
            }

            index++;
        }
    }
}
