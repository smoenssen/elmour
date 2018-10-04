package com.smoftware.elmour;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

/**
 * Created by steve on 9/30/18.
 */

public class PartyInventory extends PartyInventorySubject {
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
            addItem(InventoryElementFactory.getInstance().getInventoryElement(elementID), Integer.parseInt(saValues[1]), false);
        }
    }

    public String getInventoryProfileString() {
        // return delimited string of inventory item ids and quantities
        String profileString = "";

        for (PartyInventoryItem item : list) {
            if (!profileString.equals("")) {
                profileString += ITEM_DELIMITER;
            }
            String newItem = item.getElement().id.toString() + VALUE_DELIMITER + Integer.toString(item.getQuantity());
            profileString += newItem;
        }
        return profileString;
    }

    public Array<PartyInventoryItem> getFullList() {
        return list;
    }

    public PartyInventoryItem getItem(InventoryElement element) {
        PartyInventoryItem item = null;

        for (PartyInventoryItem itemInList : list) {
            if (itemInList.getElement().id.toString().equals(element.id .toString())) {
                return item;
            }
        }
        return item;
    }

    public void addItem(InventoryElement element, int quantity, boolean notify) {
        // add item to list if it doesn't exist, otherwise update the quantity
        boolean itemInList = false;
        for (PartyInventoryItem listItem : list) {
            if (listItem.getElement().id == element.id) {
                itemInList = true;
                listItem.increaseQuantity(quantity);
                if (notify)
                    notify(listItem, PartyInventoryObserver.PartyInventoryEvent.INVENTORY_ADDED);
                break;
            }
        }

        if (!itemInList) {
            PartyInventoryItem itemToAdd = new PartyInventoryItem(element, quantity);
            list.add(itemToAdd);
            if (notify)
                notify(itemToAdd, PartyInventoryObserver.PartyInventoryEvent.INVENTORY_ADDED);
        }
    }

    public void removeItem(InventoryElement element, int quantity) {
        // decrement number of items, remove totally from list if quantity reaches zero
        int index = 0;
        for (PartyInventoryItem listItem : list) {
            if (listItem.getElement().id == element.id) {
                listItem.reduceQuantity(quantity);

                if (listItem.getQuantity() <= 0) {
                    list.removeIndex(index);
                }

                notify(listItem, PartyInventoryObserver.PartyInventoryEvent.INVENTORY_REMOVED);
                break;
            }

            index++;
        }
    }
}
