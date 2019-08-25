package com.smoftware.elmour.inventory;

/**
 * Created by steve on 5/27/19.
 */

public class KeyItem {

    public enum Category {
        QUEST, NON_QUEST, INVENTORY, NONE
    }

    public enum ID {
        //Non Quest
        EMERALD, GRASS_TEMPLE_KEY,
        //Quest
        TEDDY_BEAR, CTRL_KEY, ALT_KEY, DEL_KEY, MYSTERY_POUCH,

        NONE
    }

    public ID id;
    public Category category;
    public String name;
    public String summary;
    public String imagePath;

    // These items are not in .json file but set later when hidden item is found
    public String text;
    public String taskID;

    public KeyItem() {
        id = ID.NONE;
        category = Category.NONE;
        name = "";
        summary = "";
        imagePath = "";
    }

    public KeyItem(KeyItem keyItem) {
        id = keyItem.id;
        category = keyItem.category;
        name = keyItem.name;
        summary = keyItem.summary;
        imagePath = keyItem.imagePath;
    }
}
