package com.smoftware.elmour;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;

import java.util.ArrayList;
import java.util.Hashtable;

public class SpellPowerFactory {
    private Json _json = new Json();
    private final String SPELL_JSON = "RPGGame/maps/Game/Scripts/Spell.json";
    private static SpellPowerFactory _instance = null;
    private Hashtable<SpellsPowerElement.ElementID, SpellsPowerElement> spellPowerList;

    public static SpellPowerFactory getInstance() {
        if (_instance == null) {
            _instance = new SpellPowerFactory();
        }

        return _instance;
    }

    private SpellPowerFactory(){
        ArrayList<SpellsPowerElement> jsonInventoryList = _json.fromJson(ArrayList.class, SpellsPowerElement.class, Gdx.files.internal(SPELL_JSON));
        spellPowerList = new Hashtable<>();

        for (SpellsPowerElement element : jsonInventoryList) {
            spellPowerList.put(element.id, element);
        }
    }

    public SpellsPowerElement getSpellPowerElement(SpellsPowerElement.ElementID elementID){
        SpellsPowerElement element = spellPowerList.get(elementID);

        if (element != null) {
            element = new SpellsPowerElement(element);
        }
        else {
            element = spellPowerList.get(elementID);

            if (element != null) {
                element = new SpellsPowerElement(element);
            }
        }

        return element;
    }

    public Hashtable<SpellsPowerElement.ElementID, SpellsPowerElement> getSpellPowerList() {
        return spellPowerList;
    }
}
