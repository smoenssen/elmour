package com.smoftware.elmour.inventory;

import com.badlogic.gdx.utils.Array;


/**
 * Created by steve on 3/17/18.
 */

public class InventoryElement{

    public enum InventoryCategory {
        Helmet,
        Breastplate,
        Leggings,
        Weapon,
        Potion,
        Food,
        Consumables,
        Throwing,
        BLUNT,
        STAB,
        KNUCKLES,
        NONE
    }

    public enum WeaponType {
        DAGGER,
        KNUCKLES,
        SWORD,
        MACE,
        STAFF,
        NONE    // needs to always be last item
    }

    public enum Effect {
        HEAL_HP,
        HEAL_HP_PERCENT,
        HEAL_MP,
        HEAL_MP_PERCENT,
        ATK_UP,
        ATK_DOWN,
        ATK_NORMAL,
        DEF_UP,
        DEF_DOWN,
        DEF_NORMAL,
        MATK_UP,
        MATK_DOWN,
        MATK_NORMAL,
        MDEF_UP,
        MDEF_DOWN,
        MDEF_NORMAL,
        ACC_UP,
        ACC_DOWN,
        ACC_NORMAL,
        AVO_UP,
        AVO_DOWN,
        AVO_NORMAL,
        SPD_UP,
        SPD_DOWN,
        SPD_NORMAL,
        LCK_UP,
        LCK_DOWN,
        LCK_NORMAL,
        DIBS_UP,
        DIBS_DOWN,
        DIBS_NORMAL,
        EXP_UP,
        EXP_DOWN,
        EXP_NORMAL,
        DROPS_UP,
        DROPS_DOWN,
        DROPS_NORMAL,
        NONE
    }

    public enum ElementID {
        MEAT1, MEAT2, MEAT3, MEAT4, MEAT5, MEAT6, VEG1, VEG2, VEG3, VEG4, SUGAR1, SUGAR2, SUGAR3,
        FRUIT1, FRUIT2, FRUIT3, CHEESE1, CHEESE2, CHEESE3, CHEESE4, LCK1, LCK2, LCK3, HP1, HP2, HP3,
        HP4, HP5, HP6, MP1, MP2, MP3, MP4, MP5, MP6, BOTTLE1, BOTTLE2, BOTTLE3,
        THROW1, THROW2, THROW3, THROW4, THROW5, THROW6,
        BREASTPLATE1, BREASTPLATE2, BREASTPLATE3, LEGGINGS1, LEGGINGS2, LEGGINGS3, HELMET1, HELMET2, HELMET3,

        // Note: if any weapons are added, getNumberOfWeaponsOfType() below will need to be updated
        DAGGER1, DAGGER2, DAGGER3, DAGGER4, DAGGER5,
        KNUCKLES1, KNUCKLES2, KNUCKLES3, KNUCKLES4, KNUCKLES5,
        SWORD1, SWORD2, SWORD3, SWORD4, SWORD5,
        MACE1, MACE2, MACE3, MACE4, MACE5,
        STAFF1, STAFF2, STAFF3, STAFF4, STAFF5,

        NONE
    }

    public static int getNumberOfWeaponsOfType(WeaponType type) {
        int numOfWeapons = 0;

        switch (type) {
            case DAGGER:
                numOfWeapons = 5;
                break;
            case KNUCKLES:
                numOfWeapons = 5;
                break;
            case SWORD:
                numOfWeapons = 5;
                break;
            case MACE:
                numOfWeapons = 5;
                break;
            case STAFF:
                numOfWeapons = 5;
                break;
        }

        return numOfWeapons;
    }

    public InventoryCategory category;
    public String name;
    public ElementID id;
    public WeaponType type;
    public String summary;
    public String effectText;
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
            turns = 0;//0x7FFFFFFF;
        }
    }

    public Array<EffectItem> effectList;

    public InventoryElement() {
        // default values
        id = ElementID.NONE;
        type = WeaponType.NONE;
        effectText = "";
        location = "";
        chapter = 0;
        turns = 0;//0x7FFFFFFF;
        revive = false;
        category = InventoryCategory.NONE;
    }

    public InventoryElement(InventoryElement element) {
        // copy constructor
        this.category = element.category;
        this.name = element.name;
        this.id = element.id;
        this.type = element.type;
        this.summary = element.summary;
        this.effectText = element.effectText;
        this.buy = element.buy;
        this.sell = element.sell;
        this.location = element.location;
        this.chapter = element.chapter;
        this.turns = element.turns;
        this.revive = element.revive;

        if (element.effectList != null) {
            this.effectList = new Array<>();
            this.effectList.addAll(element.effectList);
        }
    }

    public EffectItem getEffectItem(InventoryElement.Effect effect) {
        EffectItem item = null;

        for (EffectItem effectItem : effectList) {
            if (effectItem.effect.equals(effect)) {
                item = effectItem;
                break;
            }
        }

        return item;
    }

    public boolean isWeapon() {
        return (category == InventoryCategory.BLUNT || category == InventoryCategory.STAB || category == InventoryCategory.KNUCKLES);
    }

    public boolean isArmor() {
        return (category == InventoryCategory.Helmet || category == InventoryCategory.Breastplate || category == InventoryCategory.Leggings);
    }
}
