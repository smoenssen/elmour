package com.smoftware.elmour;

import com.badlogic.gdx.utils.Array;

/**
 * Created by moenssr on 4/2/2018.
 */

public class SpellsPowerElement {
    public enum SpellPowerCategory {
        Black,
        White,
        Power
    }

    public enum Effect {
        Armor_Pierce,
        Burn,
        Confused,
        Crippled,
        Heal_Max_HP,
        Stun
    }

    public SpellPowerCategory category;
    public String name;
    public String summary;
    public int MP;
    public int DMG;
    public int ACC;
    public int turns;
    public boolean Revive;
    public boolean Kill;

    public static class EffectItem {
        public Effect effect;
        public Integer value;
        public int turns;
    }

    public Array<EffectItem> effectList;

}
