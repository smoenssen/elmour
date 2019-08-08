package com.smoftware.elmour.inventory;

import com.badlogic.gdx.Gdx;
import com.smoftware.elmour.profile.ProfileManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.smoftware.elmour.inventory.PartyKeysObserver.PartyKeysEvent.KEY_ITEM_ADDED;
import static com.smoftware.elmour.inventory.PartyKeysObserver.PartyKeysEvent.KEY_ITEM_REMOVED;
import static com.smoftware.elmour.inventory.PartyKeysObserver.PartyKeysEvent.KEY_ITEM_SWAP;

/**
 * Created by steve on 6/9/2019.
 */

public class PartyKeys extends PartyKeysSubject {
    private static final String TAG = PartyKeys.class.getSimpleName();

    private static PartyKeys partyKeys;

    // Using a LinkedHashMap so that ordering of items is preserved which is needed for swapping
    private LinkedHashMap<KeyItem.ID, PartyKeyItem> _list = null;
    public final String PROPERTY_NAME = "partyKeys";
    public final String ITEM_DELIMITER = ";";
    public final String VALUE_DELIMITER = ",";

    private PartyKeys(){
        _list = new LinkedHashMap<>();
    }

    public static final PartyKeys getInstance(){
        if( partyKeys == null){
            partyKeys = new PartyKeys();
        }
        return partyKeys;
    }

    public int getSize() { return _list.size(); }

    public void setKeysList(String profileString) {
        Gdx.app.log(TAG, "setKeysList");

        if (profileString == null) return;
        if (profileString.length() == 0) return;
        _list.clear();

        // create list based on delimited string of key item ids and quantities
        String [] saItems = profileString.split(ITEM_DELIMITER);

        for (String item : saItems) {
            String [] saValues = item.split(VALUE_DELIMITER);

            KeyItem.ID id = KeyItem.ID.valueOf(saValues[0]);
            addItem(KeyItemFactory.getInstance().getKeyItem(id),
                    Integer.parseInt(saValues[1]), true);
        }
    }

    public String getInventoryProfileString() {
        // return delimited string of inventory element ids and quantities
        String profileString = "";

        Set<KeyItem.ID> setKeys = _list.keySet();
        for(KeyItem.ID key: setKeys){
            PartyKeyItem item = _list.get(key);
            String newItem = key.toString() + VALUE_DELIMITER +
                    Integer.toString(item.getQuantity());

            profileString += newItem + ITEM_DELIMITER;
        }
        return profileString;
    }

    public PartyKeyItem getItem(KeyItem keyItem) {
        return _list.get(keyItem.id);
    }

    public void addItem(KeyItem keyItem, int quantity, boolean notify) {
        // add item to list if it doesn't exist, otherwise update the quantity
        PartyKeyItem listItem = _list.get(keyItem.id);

        if (listItem != null) {
            listItem.increaseQuantity(quantity, this);
        }
        else {
            listItem = new PartyKeyItem(keyItem, quantity);
            _list.put(keyItem.id, listItem);
        }

        // Save new list to profile
        ProfileManager.getInstance().setProperty(PROPERTY_NAME, getInventoryProfileString());

        if (notify)
            notify(listItem, KEY_ITEM_ADDED);
    }

    public void removeItem(KeyItem keyItem, int quantity, boolean notify) {
        // decrement number of items, remove totally from list if quantity reaches zero
        PartyKeyItem listItem = _list.get(keyItem.id);

        if (listItem != null) {
            listItem.reduceQuantity(quantity, this);
            if (listItem.getQuantity() <= 0) {
                _list.remove(listItem);
            }
            else {
                _list.put(keyItem.id, listItem);
            }

            // Save new list to profile and notify
            ProfileManager.getInstance().setProperty(PROPERTY_NAME, getInventoryProfileString());
            notify(listItem, KEY_ITEM_REMOVED);
        }
    }

    public void swapItems(PartyKeyItem item1, PartyKeyItem item2) {
        // Need to swap items in hash table...

        // Get Set of entries from HashMap
        Set<Map.Entry<KeyItem.ID, PartyKeyItem>> entrySet = _list.entrySet();

        // Create an ArrayList of Entry objects
        ArrayList<Map.Entry<KeyItem.ID, PartyKeyItem>> listOfEntries = new ArrayList<>(entrySet);

        // Swap items in ArrayList
        Collections.swap(listOfEntries, listOfEntries.indexOf(item1), listOfEntries.indexOf(item2));

        // Rewrite the Hash Table
        _list.clear();
        for (Map.Entry<KeyItem.ID, PartyKeyItem>  partyKeyItem : listOfEntries) {
            Gdx.app.log(TAG, "putting " + partyKeyItem.getKey());
            _list.put(partyKeyItem.getKey(), partyKeyItem.getValue());
        }

        // Save new list to profile and notify to reset inventory
        ProfileManager.getInstance().setProperty(PROPERTY_NAME, getInventoryProfileString());
        notify(item1, item2, KEY_ITEM_SWAP);
    }
}
