package com.smoftware.elmour;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

import java.util.Hashtable;
import java.util.Set;

/**
 * Created by steve on 9/30/18.
 */

public class PartyInventory extends PartyInventorySubject {
    private static final String TAG = PartyInventory.class.getSimpleName();

    private static PartyInventory partyInventory;
    //private Array<PartyInventoryItem> list;
    private Hashtable<InventoryElement.ElementID, PartyInventoryItem> _list = null;
    public final String PROPERTY_NAME = "partyInventory";
    public final String ITEM_DELIMITER = ";";
    public final String VALUE_DELIMITER = ",";

    private PartyInventory(){
        _list = new Hashtable<>();
    }

    public static final PartyInventory getInstance(){
        if( partyInventory == null){
            partyInventory = new PartyInventory();
        }
        return partyInventory;
    }

    public void setInventoryList(String profileString) {
        // create list based on delimited string of inventory element ids and quantities
        String [] saItems = profileString.split(ITEM_DELIMITER);

        for (String item : saItems) {
            String [] saValues = item.split(VALUE_DELIMITER);

            InventoryElement.ElementID elementID = InventoryElement.ElementID.valueOf(saValues[0]);
            addItem(InventoryElementFactory.getInstance().getInventoryElement(elementID), Integer.parseInt(saValues[1]), true);
        }
    }

    public String getInventoryProfileString() {
        // return delimited string of inventory element ids and quantities
        String profileString = "";

        Set<InventoryElement.ElementID> setKeys = _list.keySet();
        for(InventoryElement.ElementID key: setKeys){
            PartyInventoryItem item = _list.get(key);
            String newItem = key.toString() + VALUE_DELIMITER + Integer.toString(item.getQuantity());
            profileString += newItem;
        }
        return profileString;
    }

    public PartyInventoryItem getItem(InventoryElement element) {
        return _list.get(element.id);
    }

    public void addItem(InventoryElement element, int quantity, boolean notify) {
        // add item to list if it doesn't exist, otherwise update the quantity
        PartyInventoryItem listItem = _list.get(element.id);
        if (listItem != null) {
            listItem.increaseQuantity(quantity);
            if (notify)
                notify(listItem, PartyInventoryObserver.PartyInventoryEvent.INVENTORY_ADDED);
        }
        else {
            PartyInventoryItem itemToAdd = new PartyInventoryItem(element, quantity);
            _list.put(element.id, itemToAdd);
            if (notify)
                notify(itemToAdd, PartyInventoryObserver.PartyInventoryEvent.INVENTORY_ADDED);
        }
    }

    public void removeItem(InventoryElement element, int quantity, boolean notify) {
        // decrement number of items, remove totally from list if quantity reaches zero
        PartyInventoryItem listItem = _list.get(element.id);

        if (listItem != null) {
            listItem.reduceQuantity(quantity);
            if (listItem.getQuantity() <= 0) {
                _list.remove(listItem);
            }
            else {
                _list.put(element.id, listItem);
            }

            if (notify)
                notify(listItem, PartyInventoryObserver.PartyInventoryEvent.INVENTORY_REMOVED);
        }
    }
}
