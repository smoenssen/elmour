package com.smoftware.elmour.inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;

import java.util.ArrayList;
import java.util.Hashtable;

public class SpellPowerElementFactory {
    private Json _json = new Json();
    private final String SPELL_JSON = "RPGGame/maps/Game/Scripts/Spell.json";
    private static SpellPowerElementFactory _instance = null;
    private Hashtable<SpellPowerElement.ElementID, SpellPowerElement> spellPowerList;

    public static SpellPowerElementFactory getInstance() {
        if (_instance == null) {
            _instance = new SpellPowerElementFactory();
        }

        return _instance;
    }

    private SpellPowerElementFactory(){
        ArrayList<SpellPowerElement> jsonInventoryList = _json.fromJson(ArrayList.class, SpellPowerElement.class, Gdx.files.internal(SPELL_JSON));
        spellPowerList = new Hashtable<>();

        for (SpellPowerElement element : jsonInventoryList) {
            spellPowerList.put(element.id, element);
        }
    }

    public SpellPowerElement getSpellPowerElement(SpellPowerElement.ElementID elementID){
        SpellPowerElement element = spellPowerList.get(elementID);

        if (element != null) {
            element = new SpellPowerElement(element);
        }
        else {
            element = spellPowerList.get(elementID);

            if (element != null) {
                element = new SpellPowerElement(element);
            }
        }

        return element;
    }

    public Hashtable<SpellPowerElement.ElementID, SpellPowerElement> getSpellPowerList() {
        return spellPowerList;
    }
}
