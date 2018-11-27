package com.smoftware.elmour;

import com.badlogic.gdx.utils.Array;

/**
 * Created by moenssr on 4/2/2018.
 */

public class SpellsPowerElement {
    public enum SpellPowerCategory {
        Black,
        White,
        Power,
        None
    }

    public enum Effect {
        Armor_Pierce,
        Burn,
        Confused,
        Crippled,
        Heal_Max_HP,
        Stun
    }

    public enum ElementID {
        FIRE,
        THUNDER,
        EARTH,
        WATER,
        POWER_WORD_KILL,
        LIFE,
        HEAL,
        POWER_WORD_HEAL,
        SLASH,
        KICK,
        NONE
    }

    public ElementID id;
    public SpellPowerCategory category;
    public String name;
    public String summary;
    public int MP = 0;
    public int DMG = 0;
    public int ACC = 0;
    public int turns = 0;
    public boolean Revive = false;
    public boolean Kill = false;

    public static class EffectItem {
        public Effect effect;
        public Integer value;
        public int turns;
    }

    public Array<EffectItem> effectList;

    public SpellsPowerElement() {
        // default values
        id = ElementID.NONE;
        turns = 0;//0x7FFFFFFF;
    }

    public SpellsPowerElement(SpellsPowerElement element) {
        // copy constructor
        this.category = element.category;
        this.name = element.name;
        this.id = element.id;
        this.summary = element.summary;
        this.MP = element.MP;
        this.DMG = element.DMG;
        this.ACC = element.ACC;
        this.turns = element.turns;
        this.Revive = element.Revive;
        this.turns = element.turns;
        this.Kill = element.Kill;

        if (element.effectList != null) {
            this.effectList = new Array<>();
            this.effectList.addAll(element.effectList);
        }
    }
}
