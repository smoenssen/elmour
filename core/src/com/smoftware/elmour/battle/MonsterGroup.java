package com.smoftware.elmour.battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by steve on 5/6/18.
 */

public class MonsterGroup {
    private String groupID;
    private Array<MonsterFactory.MonsterEntityType> monsters;

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public Array<MonsterFactory.MonsterEntityType> getMonsters() {
        return monsters;
    }

    public void setMonsters(Array<MonsterFactory.MonsterEntityType> monsters) {
        this.monsters = monsters;
    }

    static public Hashtable<String, Array<MonsterFactory.MonsterEntityType>> getMonsterGroups(String configFilePath){
        Json json = new Json();
        Hashtable<String, Array<MonsterFactory.MonsterEntityType>> monsterGroups = new Hashtable<String, Array<MonsterFactory.MonsterEntityType>>();

        ArrayList<JsonValue> list = json.fromJson(ArrayList.class, Gdx.files.internal(configFilePath));

        for (JsonValue jsonVal : list) {
            MonsterGroup group = json.readValue(MonsterGroup.class, jsonVal);
            monsterGroups.put(group.getGroupID(), group.getMonsters());
        }

        return monsterGroups;
    }
}
