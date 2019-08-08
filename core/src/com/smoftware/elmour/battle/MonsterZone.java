package com.smoftware.elmour.battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import java.util.ArrayList;
import java.util.Hashtable;

public class MonsterZone {
    private String zoneID;
    private int minTime;
    private int maxTime;
    private int minLevel;
    private int maxLevel;
    private int deviance;

    public String getZoneID() {
        return zoneID;
    }
    public int getMinTime() { return minTime; }
    public int getMaxTime() { return maxTime; }
    public int getminLevel() { return minLevel; }
    public int getmaxLevel() { return maxLevel; }
    public int getDeviance() { return deviance; }

    private Array<MonsterFactory.MonsterGroupType> groups;

    public void setZoneID(String zoneID) {
        this.zoneID = zoneID;
    }

    public Array<MonsterFactory.MonsterGroupType> getGroups() {
        return groups;
    }

    public void setGroups(Array<MonsterFactory.MonsterGroupType> groups) {
        this.groups = groups;
    }

    static public Hashtable<String, Array<MonsterFactory.MonsterGroupType>> getMonsterZoneGroups(String configFilePath){
        Json json = new Json();
        Hashtable<String, Array<MonsterFactory.MonsterGroupType>> monsterZoneGroups = new Hashtable<String, Array<MonsterFactory.MonsterGroupType>>();

        ArrayList<JsonValue> list = json.fromJson(ArrayList.class, Gdx.files.internal(configFilePath));

        for (JsonValue jsonVal : list) {
            MonsterZone zone = json.readValue(MonsterZone.class, jsonVal);
            monsterZoneGroups.put(zone.getZoneID(), zone.getGroups());
        }

        return monsterZoneGroups;
    }

    static public Hashtable<String, MonsterZone> getMonsterZones(String configFilePath){
        Json json = new Json();
        Hashtable<String, MonsterZone> monsterZones = new Hashtable<String, MonsterZone>();

        ArrayList<JsonValue> list = json.fromJson(ArrayList.class, Gdx.files.internal(configFilePath));

        for (JsonValue jsonVal : list) {
            MonsterZone zone = json.readValue(MonsterZone.class, jsonVal);
            monsterZones.put(zone.getZoneID(), zone);
        }

        return monsterZones;
    }
}
