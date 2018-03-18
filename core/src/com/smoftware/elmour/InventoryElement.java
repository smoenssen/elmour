package com.smoftware.elmour;

import com.badlogic.gdx.utils.Array;


/**
 * Created by steve on 3/17/18.
 */

public class InventoryElement {

    public enum InventoryCategory {
        ARMOR,
        WEAPON,
        EQUIPMENT,
        POTION,
        FOOD,
        OTHER
    }

    public enum Effect {
        HEAL_HP,
        HEAL_MP,
        ATK_UP,
        ATK_DOWN,
        DEF_UP,
        DEF_DOWN,
        MATK_UP,
        MATK_DOWN,
        MDEF_UP,
        MDEF_DOWN,
        ACC_UP,
        ACC_DOWN,
        EVO_UP,
        EVO_DOWN,
        SPD_UP,
        SPD_DOWN,
        LCK_UP,
        LCK_DOWN
    }

    public InventoryCategory category;
    public String name;
    public String summary;

    public static class EffectItem {
        public Effect effect;
        public int value;
    }

    public Array<EffectItem> effectList;

}
