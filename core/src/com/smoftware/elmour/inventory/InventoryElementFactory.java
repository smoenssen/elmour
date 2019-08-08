package com.smoftware.elmour.inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by steve on 9/29/18.
 */

public class InventoryElementFactory {
    private Json _json = new Json();
    private final String INVENTORY_JSON = "RPGGame/maps/Game/Scripts/Inventory.json";
    private final String EQUIPMENT_JSON = "RPGGame/maps/Game/Scripts/equipment.json";
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
        ArrayList<InventoryElement> jsonInventoryList = _json.fromJson(ArrayList.class, InventoryElement.class, Gdx.files.internal(INVENTORY_JSON));
        inventoryList = new Hashtable<>();

        for (InventoryElement inventoryElement : jsonInventoryList) {
            inventoryList.put(inventoryElement.id, inventoryElement);
        }

        ArrayList<InventoryElement> jsonEquipmentList = _json.fromJson(ArrayList.class, InventoryElement.class, Gdx.files.internal(EQUIPMENT_JSON));
        equipmentList = new Hashtable<>();

        for (InventoryElement inventoryElement : jsonEquipmentList) {
            equipmentList.put(inventoryElement.id, inventoryElement);
        }
    }

    public InventoryElement getInventoryElement(InventoryElement.ElementID elementID){
        InventoryElement element = inventoryList.get(elementID);

        if (element != null) {
            element = new InventoryElement(element);
        }
        else {
            element = equipmentList.get(elementID);

            if (element != null) {
                element = new InventoryElement(element);
            }
        }

        return element;
    }

    public Hashtable<InventoryElement.ElementID, InventoryElement> getInventoryList() {
        return inventoryList;
    }

    public Hashtable<InventoryElement.ElementID, InventoryElement> getEquipmentList() {
        return equipmentList;
    }
}
