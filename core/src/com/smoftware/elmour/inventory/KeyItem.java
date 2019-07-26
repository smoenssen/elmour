package com.smoftware.elmour.inventory;

/**
 * Created by steve on 5/27/19.
 */

public class KeyItem {

    public enum Category {
        QUEST, NON_QUEST, INVENTORY, NONE
    }

    public enum ID {
        EMERALD,
        GRASS_TEMPLE_KEY,
        TEDDY_BEAR,
        NONE
    }

    public ID id;
    public Category category;
    public String name;
    public String summary;
    public String imagePath;

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
