package com.smoftware.elmour;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;

import java.util.ArrayList;
import java.util.Hashtable;

public class SpellPowerFactory {
    private Json _json = new Json();
    private final String SPELL_JSON = "RPGGame/maps/Game/Scripts/Spell.json";
    private static SpellPowerFactory _instance = null;
    private Hashtable<SpellPowerElement.ElementID, SpellPowerElement> spellPowerList;

    public static SpellPowerFactory getInstance() {
        if (_instance == null) {
            _instance = new SpellPowerFactory();
        }

        return _instance;
    }

    private SpellPowerFactory(){
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
