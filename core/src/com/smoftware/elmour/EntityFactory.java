package com.smoftware.elmour;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.smoftware.elmour.profile.ProfileManager;

import java.util.Hashtable;

public class EntityFactory {
    private static final String TAG = EntityFactory.class.getSimpleName();

    private static Json json = new Json();
    private static EntityFactory instance = null;
    private Hashtable<String, EntityConfig> entities;

    public static enum EntityType{
        PLAYER,
        PLAYER_DEMO,
        NPC
    }

    public static enum EntityName{
        CARMEN,
        CHARACTER_1,
        CHARACTER_1_BATTLE,
        CHARACTER_2,
        DOUGLAS,
        JUSTIN,
        JAXON,
        PLAYER_PUPPET,
        TOWN_GUARD_WALKING,
        TOWN_BLACKSMITH,
        TOWN_MAGE,
        TOWN_INNKEEPER,
        TOWN_FOLK1, TOWN_FOLK2, TOWN_FOLK3, TOWN_FOLK4, TOWN_FOLK5,
        TOWN_FOLK6, TOWN_FOLK7, TOWN_FOLK8, TOWN_FOLK9, TOWN_FOLK10,
        TOWN_FOLK11, TOWN_FOLK12, TOWN_FOLK13, TOWN_FOLK14, TOWN_FOLK15,
        FIRE,
        SWORD_SWIPE
    }

    public static String CARMEN_CONFIG = "scripts/carmen.json";
    public static String CHARACTER_1_CONFIG = "scripts/character_1.json";
    public static String CHARACTER_1_BATTLE_CONFIG = "RPGGame/maps/Game/Scripts/character_1_battle.json";
    public static String CHARACTER_2_CONFIG = "scripts/character_2.json";
    public static String DOUGLAS_CONFIG = "scripts/douglas.json";
    public static String JUSTIN_CONFIG = "scripts/justin.json";
    public static String JAXON_CONFIG = "scripts/jaxon_1.json";
    public static String PLAYER_CONFIG = "scripts/player.json";
    public static String TOWN_GUARD_WALKING_CONFIG = "scripts/town_guard_walking.json";
    public static String TOWN_BLACKSMITH_CONFIG = "scripts/town_blacksmith.json";
    public static String TOWN_MAGE_CONFIG = "scripts/town_mage.json";
    public static String TOWN_INNKEEPER_CONFIG = "scripts/town_innkeeper.json";
    public static String TOWN_FOLK_CONFIGS = "scripts/town_folk.json";
    public static String ENVIRONMENTAL_ENTITY_CONFIGS = "scripts/environmental_entities.json";
    public static String SWORD_SWIPE_CONFIG = "scripts/sword_swipe.json";


    private EntityFactory(){
        entities = new Hashtable<String, EntityConfig>();

        Array<EntityConfig> townFolkConfigs = Entity.getEntityConfigs(TOWN_FOLK_CONFIGS);
        for( EntityConfig config: townFolkConfigs){
            entities.put(config.getEntityID(), config);
        }

        Array<EntityConfig> environmentalEntityConfigs = Entity.getEntityConfigs(ENVIRONMENTAL_ENTITY_CONFIGS);
        for( EntityConfig config: environmentalEntityConfigs){
            entities.put(config.getEntityID(), config);
        }

        entities.put(EntityName.CARMEN.toString(), Entity.loadEntityConfigByPath(CARMEN_CONFIG));
        entities.put(EntityName.CHARACTER_1.toString(), Entity.loadEntityConfigByPath(CHARACTER_1_CONFIG));
        entities.put(EntityName.CHARACTER_1_BATTLE.toString(), Entity.loadEntityConfigByPath(CHARACTER_1_BATTLE_CONFIG));
        entities.put(EntityName.CHARACTER_2.toString(), Entity.loadEntityConfigByPath(CHARACTER_2_CONFIG));
        entities.put(EntityName.DOUGLAS.toString(), Entity.loadEntityConfigByPath(DOUGLAS_CONFIG));
        entities.put(EntityName.JUSTIN.toString(), Entity.loadEntityConfigByPath(JUSTIN_CONFIG));
        entities.put(EntityName.JAXON.toString(), Entity.loadEntityConfigByPath(JAXON_CONFIG));
        entities.put(EntityName.TOWN_GUARD_WALKING.toString(), Entity.loadEntityConfigByPath(TOWN_GUARD_WALKING_CONFIG));
        entities.put(EntityName.TOWN_BLACKSMITH.toString(), Entity.loadEntityConfigByPath(TOWN_BLACKSMITH_CONFIG));
        entities.put(EntityName.TOWN_MAGE.toString(), Entity.loadEntityConfigByPath(TOWN_MAGE_CONFIG));
        entities.put(EntityName.TOWN_INNKEEPER.toString(), Entity.loadEntityConfigByPath(TOWN_INNKEEPER_CONFIG));
        entities.put(EntityName.PLAYER_PUPPET.toString(), Entity.loadEntityConfigByPath(PLAYER_CONFIG));
        entities.put(EntityName.SWORD_SWIPE.toString(), Entity.loadEntityConfigByPath(SWORD_SWIPE_CONFIG));
    }

    public static EntityFactory getInstance() {
        if (instance == null) {
            instance = new EntityFactory();
        }

        return instance;
    }

    public static Entity getEntity(EntityType entityType){
        Entity entity = null;
        switch(entityType){
            case PLAYER:
                entity = new Entity(new PlayerInputComponent(), new PlayerPhysicsComponent(), new PlayerGraphicsComponent());
                entity.setEntityConfig(Entity.getEntityConfig(EntityFactory.PLAYER_CONFIG));
                entity.sendMessage(Component.MESSAGE.LOAD_ANIMATIONS, json.toJson(entity.getEntityConfig()));
                return entity;
            case PLAYER_DEMO:
                entity = new Entity(new NPCInputComponent(), new PlayerPhysicsComponent(), new PlayerGraphicsComponent());
                return entity;
            case NPC:
                entity = new Entity(new NPCInputComponent(), new NPCPhysicsComponent(), new NPCGraphicsComponent());
                return entity;
            default:
                return null;
        }
    }

    public Entity getEntityByName(EntityName entityName){
        EntityConfig config = new EntityConfig(entities.get(entityName.toString()));
        Entity entity = Entity.initEntity(config);
        return entity;
    }

    public Entity getEntityByName(String entityName){
        EntityConfig config = new EntityConfig(entities.get(entityName));
        Entity entity = Entity.initEntity(config);
        return entity;
    }

}
