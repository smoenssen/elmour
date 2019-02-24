package com.smoftware.elmour;

import com.badlogic.gdx.Gdx;
import com.smoftware.elmour.profile.ProfileManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by steve on 9/30/18.
 */

public class PartyInventory extends PartyInventorySubject {
    private static final String TAG = PartyInventory.class.getSimpleName();

    private static PartyInventory partyInventory;

    // Using a LinkedHashMap so that ordering of items is preserved which is needed for swapping
    private LinkedHashMap<InventoryElement.ElementID, PartyInventoryItem> _list = null;
    public final String PROPERTY_NAME = "partyInventory";
    public final String ITEM_DELIMITER = ";";
    public final String VALUE_DELIMITER = ",";

    private PartyInventory(){
        _list = new LinkedHashMap<>();
    }

    public static final PartyInventory getInstance(){
        if( partyInventory == null){
            partyInventory = new PartyInventory();
        }
        return partyInventory;
    }

    public int getSize() { return _list.size(); }

    public void setInventoryList(String profileString) {
        if (profileString == null) return;
        if (profileString.length() == 0) return;
        _list.clear();

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
            profileString += newItem + ITEM_DELIMITER;
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

            notify(listItem, PartyInventoryObserver.PartyInventoryEvent.INVENTORY_REMOVED);
        }
    }

    public void swapItems(PartyInventoryItem item1, PartyInventoryItem item2) {
        // Need to swap items in hash table...

        // Get Set of entries from HashMap
        Set<Map.Entry<InventoryElement.ElementID, PartyInventoryItem>> entrySet = _list.entrySet();

        // Create an ArrayList of Entry objects
        ArrayList<Map.Entry<InventoryElement.ElementID, PartyInventoryItem>> listOfEntries = new ArrayList<>(entrySet);

        // Swap items in ArrayList
        Collections.swap(listOfEntries, listOfEntries.indexOf(item1), listOfEntries.indexOf(item2));

        // Rewrite the Hash Table
        _list.clear();
        for (Map.Entry<InventoryElement.ElementID, PartyInventoryItem>  partyInventoryItem : listOfEntries) {
            Gdx.app.log(TAG, "putting " + partyInventoryItem.getKey());
            _list.put(partyInventoryItem.getKey(), partyInventoryItem.getValue());
        }

        // Save new list to profile and notify to reset inventory
        ProfileManager.getInstance().setProperty(PROPERTY_NAME, getInventoryProfileString());
        notify(item1, item2, PartyInventoryObserver.PartyInventoryEvent.INVENTORY_SWAP);
    }
}
