package com.smoftware.elmour.battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.EntityConfig;

import java.util.ArrayList;
import java.util.Hashtable;

public class MonsterFactory {
    public static enum MonsterEntityType{
        Octomaniac, Ogre, Douglas, Steve, Royal_Guard, Steve2, Steve3,
        NONE
    }

    public static enum MonsterGroupType {
        GROUP001, GROUP002, GROUP003,
        NONE
    }

    private static MonsterFactory _instance = null;
    private Hashtable<String, Entity> _entities;
    private Hashtable<String, Array<MonsterEntityType>> monsterGroups;
    private Hashtable<String, Array<MonsterGroupType>> _monsterZoneGroups;
    private Hashtable<String, MonsterZone> monsterZones;

    private MonsterFactory(){
        //Array<EntityConfig> configs = Entity.getEntityConfigs("RPGGame/maps/Game/Scripts/monsters.json");

        Json json = new Json();
        Array<EntityConfig> configs = new Array<>();
        ArrayList<String> list = json.fromJson(ArrayList.class, Gdx.files.internal("RPGGame/maps/Game/Scripts/monsters.json"));

        for (String config : list) {
            configs.add(Entity.getEntityConfig(config));
        }

        _entities =  Entity.initEntities(configs);
        monsterGroups = MonsterGroup.getMonsterGroups("RPGGame/maps/Game/Scripts/monster_groups.json");
        _monsterZoneGroups = MonsterZone.getMonsterZoneGroups("RPGGame/maps/Game/Scripts/monster_zones.json");
        monsterZones = MonsterZone.getMonsterZones("RPGGame/maps/Game/Scripts/monster_zones.json");
    }

    public static MonsterFactory getInstance() {
        if (_instance == null) {
            _instance = new MonsterFactory();
        }

        return _instance;
    }

    public Entity getMonster(MonsterEntityType monsterEntityType){
        Entity entity = _entities.get(monsterEntityType.toString());
        return new Entity(entity);
    }

    public MonsterGroup getMonsterGroup(MonsterGroupType monsterGroupType){
        Array<MonsterEntityType> group = monsterGroups.get(monsterGroupType.toString());
        MonsterGroup monsters = new MonsterGroup();
        monsters.setGroupID(monsterGroupType.toString());
        monsters.setMonsters(group);
        return monsters;
    }

    public MonsterGroup getRandomMonsterGroup(int monsterZoneID){
        Array<MonsterGroupType> groups = _monsterZoneGroups.get(String.valueOf(monsterZoneID));
        int size = groups.size;
        if( size == 0 ){
            return null;
        }
        int randomIndex = MathUtils.random(size - 1);

        return getMonsterGroup(groups.get(randomIndex));
    }

    public MonsterZone getMonsterZone(String zoneID) {
        return monsterZones.get(zoneID);
    }
}
