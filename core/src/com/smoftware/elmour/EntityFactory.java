package com.smoftware.elmour;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

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
        CHARACTER_2,
        DOUGLAS,
        JUSTIN,
        JAXON_1,
        ROYAL_GUARD,
        STEVE,
        STEVE2,
        STEVE3,
        SHADOW1, SHADOW2, SHADOW3, SHADOW4, SHADOW5, SHADOW6, SHADOW7, SHADOW8, SHADOW9, SHADOW10,
        HIT,
        WEAPON_ANIMATIONS,
        MISC_ANIMATIONS,
        PLAYER_PUPPET,
        TOWN_GUARD_WALKING,
        TOWN_BLACKSMITH,
        TOWN_MAGE,
        TOWN_INNKEEPER,
        TOWN_FOLK1, TOWN_FOLK2, TOWN_FOLK3, TOWN_FOLK4, TOWN_FOLK5,
        TOWN_FOLK6, TOWN_FOLK7, TOWN_FOLK8, TOWN_FOLK9, TOWN_FOLK10,
        TOWN_FOLK11, TOWN_FOLK12, TOWN_FOLK13, TOWN_FOLK14, TOWN_FOLK15,
        FIRE,
        ATK_UP_LEFT,    ATK_UP_RIGHT,   ATK_DOWN_LEFT,  ATK_DOWN_RIGHT,
        MATK_UP_LEFT,   MATK_UP_RIGHT,  MATK_DOWN_LEFT, MATK_DOWN_RIGHT,
        DEF_UP_LEFT,    DEF_UP_RIGHT,   DEF_DOWN_LEFT,  DEF_DOWN_RIGHT,
        MDEF_UP_LEFT,   MDEF_UP_RIGHT,  MDEF_DOWN_LEFT, MDEF_DOWN_RIGHT,
        SPD_UP_LEFT,    SPD_UP_RIGHT,   SPD_DOWN_LEFT,  SPD_DOWN_RIGHT,
        ACC_UP_LEFT,    ACC_UP_RIGHT,   ACC_DOWN_LEFT,  ACC_DOWN_RIGHT,
        AVO_UP_LEFT,    AVO_UP_RIGHT,   AVO_DOWN_LEFT,  AVO_DOWN_RIGHT,
        LCK_UP_LEFT,    LCK_UP_RIGHT,   LCK_DOWN_LEFT,  LCK_DOWN_RIGHT,
        DIBS_UP_LEFT,   DIBS_UP_RIGHT,  DIBS_DOWN_LEFT, DIBS_DOWN_RIGHT,
        EXP_UP_LEFT,    EXP_UP_RIGHT,   EXP_DOWN_LEFT,  EXP_DOWN_RIGHT,
        DROPS_UP_LEFT,  DROPS_UP_RIGHT, DROPS_DOWN_LEFT,DROPS_DOWN_RIGHT,

        ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, ZERO, MISS, CRIT, WEAK
    }

    public static String CARMEN_CONFIG = "RPGGame/maps/Game/Scripts/carmen.json";
    public static String CHARACTER_1_CONFIG = "RPGGame/maps/Game/Scripts/character_1.json";
    public static String CHARACTER_2_CONFIG = "RPGGame/maps/Game/Scripts/character_2.json";
    public static String JUSTIN_CONFIG = "RPGGame/maps/Game/Scripts/justin.json";
    public static String JAXON_1_CONFIG = "RPGGame/maps/Game/Scripts/jaxon_1.json";

    public static String DOUGLAS_CONFIG = "RPGGame/maps/Game/Scripts/douglas.json";
    public static String ROYAL_GUARD_CONFIG = "RPGGame/maps/Game/Scripts/royal_guard.json";
    public static String STEVE_CONFIG = "RPGGame/maps/Game/Scripts/steve.json";
    public static String STEVE2_CONFIG = "RPGGame/maps/Game/Scripts/steve2.json";
    public static String STEVE3_CONFIG = "RPGGame/maps/Game/Scripts/steve3.json";

    public static String HIT_CONFIG = "RPGGame/maps/Game/Scripts/hit.json";
    public static String WEAPON_ANIMATIONS_CONFIG = "RPGGame/maps/Game/Scripts/weapon_animations.json";
    public static String MISC_ANIMATIONS_CONFIG = "RPGGame/maps/Game/Scripts/misc_animations.json";

    public static String PLAYER_CONFIG = "scripts/player.json";

    /*
    public static String TOWN_GUARD_WALKING_CONFIG = "scripts/town_guard_walking.json";
    public static String TOWN_BLACKSMITH_CONFIG = "scripts/town_blacksmith.json";
    public static String TOWN_MAGE_CONFIG = "scripts/town_mage.json";
    public static String TOWN_INNKEEPER_CONFIG = "scripts/town_innkeeper.json";
    public static String TOWN_FOLK_CONFIGS = "scripts/town_folk.json";
    public static String ENVIRONMENTAL_ENTITY_CONFIGS = "scripts/environmental_entities.json";
    */

    // Config files with arrays
    public static String STATUS_ARROW_CONFIGS = "RPGGame/maps/Game/Scripts/stat_arrows.json";
    public static String BATTLE_BURST_CONFIGS = "RPGGame/maps/Game/Scripts/battle_burst.json";
    public static String SHADOW_CONFIGS = "RPGGame/maps/Game/Scripts/shadow.json";


    private EntityFactory(){
        entities = new Hashtable<String, EntityConfig>();
/*
        Array<EntityConfig> townFolkConfigs = Entity.getEntityConfigs(TOWN_FOLK_CONFIGS);
        for( EntityConfig config: townFolkConfigs){
            entities.put(config.getEntityID(), config);
        }

        Array<EntityConfig> environmentalEntityConfigs = Entity.getEntityConfigs(ENVIRONMENTAL_ENTITY_CONFIGS);
        for( EntityConfig config: environmentalEntityConfigs){
            entities.put(config.getEntityID(), config);
        }
*/

        // Process config files with arrays
        Array<EntityConfig> statArrowConfigs = Entity.getEntityConfigs(STATUS_ARROW_CONFIGS);
        for( EntityConfig config: statArrowConfigs){
            entities.put(config.getEntityID(), config);
        }

        Array<EntityConfig> battleBurstConfigs = Entity.getEntityConfigs(BATTLE_BURST_CONFIGS);
        for( EntityConfig config: battleBurstConfigs){
            entities.put(config.getEntityID(), config);
        }

        Array<EntityConfig> shadowConfigs = Entity.getEntityConfigs(SHADOW_CONFIGS);
        for( EntityConfig config: shadowConfigs){
            entities.put(config.getEntityID(), config);
        }

        // Individual config files
        entities.put(EntityName.CARMEN.toString(), Entity.loadEntityConfigByPath(CARMEN_CONFIG));
        entities.put(EntityName.CHARACTER_1.toString(), Entity.loadEntityConfigByPath(CHARACTER_1_CONFIG));
        entities.put(EntityName.CHARACTER_2.toString(), Entity.loadEntityConfigByPath(CHARACTER_2_CONFIG));
        entities.put(EntityName.JUSTIN.toString(), Entity.loadEntityConfigByPath(JUSTIN_CONFIG));
        entities.put(EntityName.JAXON_1.toString(), Entity.loadEntityConfigByPath(JAXON_1_CONFIG));

        entities.put(EntityName.DOUGLAS.toString(), Entity.loadEntityConfigByPath(DOUGLAS_CONFIG));
        entities.put(EntityName.ROYAL_GUARD.toString(), Entity.loadEntityConfigByPath(ROYAL_GUARD_CONFIG));
        entities.put(EntityName.STEVE.toString(), Entity.loadEntityConfigByPath(STEVE_CONFIG));
        entities.put(EntityName.STEVE2.toString(), Entity.loadEntityConfigByPath(STEVE2_CONFIG));
        entities.put(EntityName.STEVE3.toString(), Entity.loadEntityConfigByPath(STEVE3_CONFIG));

        entities.put(EntityName.HIT.toString(), Entity.loadEntityConfigByPath(HIT_CONFIG));
        entities.put(EntityName.WEAPON_ANIMATIONS.toString(), Entity.loadEntityConfigByPath(WEAPON_ANIMATIONS_CONFIG));
        entities.put(EntityName.MISC_ANIMATIONS.toString(), Entity.loadEntityConfigByPath(MISC_ANIMATIONS_CONFIG));

/*
        entities.put(EntityName.TOWN_GUARD_WALKING.toString(), Entity.loadEntityConfigByPath(TOWN_GUARD_WALKING_CONFIG));
        entities.put(EntityName.TOWN_BLACKSMITH.toString(), Entity.loadEntityConfigByPath(TOWN_BLACKSMITH_CONFIG));
        entities.put(EntityName.TOWN_MAGE.toString(), Entity.loadEntityConfigByPath(TOWN_MAGE_CONFIG));
        entities.put(EntityName.TOWN_INNKEEPER.toString(), Entity.loadEntityConfigByPath(TOWN_INNKEEPER_CONFIG));
        entities.put(EntityName.PLAYER_PUPPET.toString(), Entity.loadEntityConfigByPath(PLAYER_CONFIG));
 */
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
        Entity entity = Entity.initEntity(config, true);
        return entity;
    }

    public Entity getEntityByName(String entityName){
        EntityConfig config = new EntityConfig(entities.get(entityName));
        Entity entity = Entity.initEntity(config, true);
        return entity;
    }

    public Entity getEntityByNameNoLoadAnimations(EntityName entityName){
        EntityConfig config = new EntityConfig(entities.get(entityName.toString()));
        Entity entity = Entity.initEntity(config, false);
        return entity;
    }

    public void setEntityByName(String entityName, EntityConfig config) {
        entities.put(entityName.toUpperCase(), config);
    }
}
