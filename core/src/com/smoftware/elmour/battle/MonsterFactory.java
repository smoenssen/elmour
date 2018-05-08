package com.smoftware.elmour.battle;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.EntityConfig;

import java.util.Hashtable;

public class MonsterFactory {
    public static enum MonsterEntityType{
        Goblin, Octomaniac, Ogre,
        NONE
    }

    public static enum MonsterGroupType {
        GROUP001, GROUP002, GROUP003,
        NONE
    }

    private static MonsterFactory _instance = null;
    private Hashtable<String, Entity> _entities;
    private Hashtable<String, Array<MonsterEntityType>> monsterGroups;
    private Hashtable<String, Array<MonsterGroupType>> _monsterZones;

    private MonsterFactory(){
        Array<EntityConfig> configs = Entity.getEntityConfigs("scripts/monsters.json");
        _entities =  Entity.initEntities(configs);
        monsterGroups = MonsterGroup.getMonsterGroups("scripts/monster_groups.json");
        _monsterZones = MonsterZone.getMonsterZones("scripts/monster_zones.json");
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
        monsters.setMonsters(group);
        return monsters;
    }

    public MonsterGroup getRandomMonsterGroup(int monsterZoneID){
        Array<MonsterGroupType> groups = _monsterZones.get(String.valueOf(monsterZoneID));
        int size = groups.size;
        if( size == 0 ){
            return null;
        }
        int randomIndex = MathUtils.random(size - 1);

        return getMonsterGroup(groups.get(randomIndex));
    }

}
