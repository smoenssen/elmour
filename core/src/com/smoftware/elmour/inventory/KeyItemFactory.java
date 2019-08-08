package com.smoftware.elmour.inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by steve on 5/27/19.
 */

public class KeyItemFactory {
    private Json _json = new Json();
    private final String KEY_ITEMS_JSON = "RPGGame/maps/Game/Scripts/keyItems.json";
    private static KeyItemFactory _instance = null;
    private Hashtable<KeyItem.ID, KeyItem> keyItemList;

    public static KeyItemFactory getInstance() {
        if (_instance == null) {
            _instance = new KeyItemFactory();
        }

        return _instance;
    }

    private KeyItemFactory() {
        ArrayList<KeyItem> jsonKeyItemList = _json.fromJson(ArrayList.class, InventoryElement.class, Gdx.files.internal(KEY_ITEMS_JSON));
        keyItemList = new Hashtable<>();

        for (KeyItem keyItem : jsonKeyItemList) {
            keyItemList.put(keyItem.id, keyItem);
        }
    }

    public KeyItem getKeyItem(KeyItem.ID keyItemID){
        KeyItem item = keyItemList.get(keyItemID);

        if (item != null) {
            item = new KeyItem(item);
        }

        return item;
    }
}
