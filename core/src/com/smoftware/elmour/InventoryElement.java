package com.smoftware.elmour;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.utils.Array;


/**
 * Created by steve on 3/17/18.
 */

public class InventoryElement{

    public enum InventoryCategory {
        Helmet,
        Breastplate,
        Legging,
        Weapon,
        Potion,
        Food,
        Consumables
    }

    public enum WeaponType {
        Dagger,
        Sword,
        Mace,
        Staff,
        None
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
        LCK_DOWN,
        DIBS_UP,
        EXP_UP,
        NONE
    }

    public enum ElementID {
        MEAT1, MEAT2, MEAT3, MEAT4, MEAT5, MEAT6, VEG1, VEG2, VEG3, VEG4, SUGAR1, SUGAR2, SUGAR3,
        FRUIT1, FRUIT2, FRUIT3, CHEESE1, CHEESE2, CHEESE3, CHEESE4, LCK1, LCK2, LCK3, HP1, HP2, HP3,
        HP4, HP5, HP6, MP1, MP2, MP3, MP4, MP5, MP6, BOTTLE1, BOTTLE2, BOTTLE3,

        DAGGER1, DAGGER2, DAGGER3, KNUCKLES1, KNUCKLES2, KNUCKLES3, HELMET1, HELMET2, HELMET3,
        BREASTPLATE1, BREASTPLATE2, BREASTPLATE3, LEGGINGS1, LEGGINGS2, LEGGINGS3,

        NONE
    }

    public InventoryCategory category;
    public String name;
    public ElementID id;
    public WeaponType type;
    public String summary;
    public int buy;
    public int sell;
    public String location;
    public int chapter;
    public int turns;
    public boolean revive;

    public static class EffectItem {
        public Effect effect;
        public Integer value;
        public int turns;

        public EffectItem() {
            // default values
            effect = Effect.NONE;
            turns = 0x7FFFFFFF;
        }
    }

    public Array<EffectItem> effectList;

    public InventoryElement() {
        // default values
        id = ElementID.NONE;
        type = WeaponType.None;
        location = "";
        chapter = 0;
        turns = 0x7FFFFFFF;
        revive = false;
    }
}
