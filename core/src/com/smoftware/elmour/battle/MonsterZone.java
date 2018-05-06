package com.smoftware.elmour.battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import java.util.ArrayList;
import java.util.Hashtable;

public class MonsterZone {
    private String zoneID;
    private Array<MonsterFactory.MonsterGroupType> groups;

    public String getZoneID() {
        return zoneID;
    }

    public void setZoneID(String zoneID) {
        this.zoneID = zoneID;
    }

    public Array<MonsterFactory.MonsterGroupType> getGroups() {
        return groups;
    }

    public void setGroups(Array<MonsterFactory.MonsterGroupType> groups) {
        this.groups = groups;
    }

    static public Hashtable<String, Array<MonsterFactory.MonsterGroupType>> getMonsterZones(String configFilePath){
        Json json = new Json();
        Hashtable<String, Array<MonsterFactory.MonsterGroupType>> monsterZones = new Hashtable<String, Array<MonsterFactory.MonsterGroupType>>();

        ArrayList<JsonValue> list = json.fromJson(ArrayList.class, Gdx.files.internal(configFilePath));

        for (JsonValue jsonVal : list) {
            MonsterZone zone = json.readValue(MonsterZone.class, jsonVal);
            monsterZones.put(zone.getZoneID(), zone.getGroups());
        }

        return monsterZones;
    }
}
