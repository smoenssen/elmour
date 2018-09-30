package com.smoftware.elmour;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by steve on 9/29/18.
 */

public class InventoryElementFactory {
    private Json _json = new Json();
    private final String INVENTORY_JSON = "scripts/inventory.json";
    private final String EQUIPMENT_JSON = "scripts/equipment.json";
    private static InventoryElementFactory _instance = null;
    private Hashtable<InventoryElement.ElementID, InventoryElement> inventoryList;
    private Hashtable<InventoryElement.ElementID, InventoryElement> equipmentList;

    public static InventoryElementFactory getInstance() {
        if (_instance == null) {
            _instance = new InventoryElementFactory();
        }

        return _instance;
    }

    private InventoryElementFactory(){
        ArrayList<JsonValue> jsonInventoryList = _json.fromJson(ArrayList.class, Gdx.files.internal(INVENTORY_JSON));
        inventoryList = new Hashtable<>();

        for (JsonValue jsonVal : jsonInventoryList) {
            InventoryElement inventoryElement = _json.readValue(InventoryElement.class, jsonVal);
            inventoryList.put(inventoryElement.id, inventoryElement);
        }

        ArrayList<JsonValue> jsonEquipmentList = _json.fromJson(ArrayList.class, Gdx.files.internal(EQUIPMENT_JSON));
        equipmentList = new Hashtable<>();

        for (JsonValue jsonVal : jsonEquipmentList) {
            InventoryElement inventoryElement = _json.readValue(InventoryElement.class, jsonVal);
            equipmentList.put(inventoryElement.id, inventoryElement);
        }
    }
}
