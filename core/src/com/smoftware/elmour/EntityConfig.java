package com.smoftware.elmour;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.smoftware.elmour.Entity.AnimationType;
import com.smoftware.elmour.InventoryItem.ItemTypeID;

public class EntityConfig {
    private Array<AnimationConfig> animationConfig;
    private Array<ItemReward> rewardItems;
    private Array<ItemTypeID> inventory;
    private Entity.State state = Entity.State.IDLE;
    private Entity.Direction direction = Entity.Direction.DOWN;
    private String entityID;
    private String conversationConfigPath;
    private String questConfigPath;
    private String currentQuestID;
    private String itemTypeID;
    private ObjectMap<String, String> entityProperties;
    private String entityBoundsName;
    private String displayName;
    private Array<EntityAbility> entityAbilities;

    public enum EntityProperties{
        HIT_DAMAGE_TOTAL,//todo: remove
        HP,
        HP_MAX,
        MP,         //characters only
        MP_MAX,     //characters only
        ATK,
        MagicATK,
        DEF,
        MagicDEF,
        SPD,
        ACC,
        LCK,
        AVO,
        XP_REWARD,  //monsters only
        DIBS_REWARD,//monsters only
        NONE
    }

    public static class EntityAbility {
        String name;
        int numTurnsTilReset;
        int level;
        int maxHP;
        int priority;

        public EntityAbility() {
            numTurnsTilReset = 0;
            level = 0;
            maxHP = 0;
            priority = 0;
        }
    }

    public static class ItemReward {
        ItemTypeID item;
        int probability;
    }

    EntityConfig(){
        animationConfig = new Array<AnimationConfig>();
        inventory = new Array<ItemTypeID>();
        entityProperties = new ObjectMap<String, String>();
        rewardItems = new Array<ItemReward>();
        entityAbilities = new Array<EntityAbility>();

        /* Test code to write to Json file
        EntityAbility ability = new EntityAbility();
        ability.name = "Fire";
        ability.numTurnsTilReset = 5;
        ability.level = 3;
        ability.maxHP = 15;
        ability.priority = 1;
        entityAbilities.add(ability);

        EntityAbility ability2 = new EntityAbility();
        ability2.name = "Thunder";
        ability2.numTurnsTilReset = 43;
        ability2.level = 1;
        ability2.maxHP = 10;
        ability2.priority = 2;
        entityAbilities.add(ability2);

        Json _json = new Json();
        String fileData = _json.prettyPrint(_json.toJson(entityAbilities));

        if( Gdx.files.isLocalStorageAvailable() ) {
            FileHandle file = Gdx.files.local("test.json");
            String encodedString = fileData;//Base64Coder.encodeString(fileData);
            file.writeString(encodedString, false);
        }
        */
    }

    EntityConfig(EntityConfig config){
        state = config.getState();
        direction = config.getDirection();
        entityID = config.getEntityID();
        displayName = config.getDisplayName();
        entityBoundsName = config.getEntityBoundsName();
        conversationConfigPath = config.getConversationConfigPath();
        questConfigPath = config.getQuestConfigPath();
        currentQuestID = config.getCurrentQuestID();
        itemTypeID = config.getItemTypeID();

        animationConfig = new Array<AnimationConfig>();
        animationConfig.addAll(config.getAnimationConfig());

        inventory = new Array<ItemTypeID>();
        inventory.addAll(config.getInventory());

        entityProperties = new ObjectMap<String, String>();
        entityProperties.putAll(config.entityProperties);
    }

    public ObjectMap<String, String> getEntityProperties() {
        return entityProperties;
    }

    public void setEntityProperties(ObjectMap<String, String> entityProperties) {
        this.entityProperties = entityProperties;
    }

    public void setPropertyValue(String key, String value){
        entityProperties.put(key, value);
    }

    public String getPropertyValue(String key){
        Object propertyVal = entityProperties.get(key);
        if( propertyVal == null ) return new String();
        return propertyVal.toString();
    }

    public String getCurrentQuestID() {
        return currentQuestID;
    }

    public void setCurrentQuestID(String currentQuestID) {
        this.currentQuestID = currentQuestID;
    }

    public String getItemTypeID() {
        return itemTypeID;
    }

    public void setItemTypeID(String itemTypeID) {
        this.itemTypeID = itemTypeID;
    }

    public String getQuestConfigPath() {
        return questConfigPath;
    }

    public void setQuestConfigPath(String questConfigPath) { this.questConfigPath = questConfigPath; }

    public String getConversationConfigPath() {
        return conversationConfigPath;
    }

    public void setConversationConfigPath(String conversationConfigPath) { this.conversationConfigPath = conversationConfigPath; }

    public String getEntityID() {
        return entityID;
    }

    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    public String getEntityBoundsName() { return entityBoundsName; }

    public void setEntityBoundsName(String entityBoundsName) { this.entityBoundsName = entityBoundsName; }

    public String getDisplayName() { return displayName; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Entity.Direction getDirection() { return direction; }

    public void setDirection(Entity.Direction direction) { this.direction = direction; }

    public Entity.State getState() { return state; }

    public void setState(Entity.State state) { this.state = state; }

    public Array<AnimationConfig> getAnimationConfig() { return animationConfig; }

    public void addAnimationConfig(AnimationConfig animationConfig) { this.animationConfig.add(animationConfig); }

    public Array<ItemTypeID> getInventory() { return inventory; }

    public void setInventory(Array<ItemTypeID> inventory) { this.inventory = inventory; }

    static public class AnimationConfig{
        private float frameDuration = 1.0f;
        private int frameWidth = 16;
        private int frameHeight = 16;
        private AnimationType animationType;
        private Array<String> texturePaths;
        private Array<GridPoint2> gridPoints;

        public AnimationConfig(){
            animationType = AnimationType.IDLE;
            texturePaths = new Array<String>();
            gridPoints = new Array<GridPoint2>();
        }

        public float getFrameDuration() {
            return frameDuration;
        }

        public void setFrameDuration(float frameDuration) {
            this.frameDuration = frameDuration;
        }

        public int getFrameWidth() { return frameWidth; }

        public void setFrameWidth(int frameWidth ) { this.frameWidth = frameWidth; }

        public int getFrameHeight() { return frameHeight; }

        public void setFrameHeight(int frameHeight ) { this.frameHeight = frameHeight; }

        public Array<String> getTexturePaths() {
            return texturePaths;
        }

        public void setTexturePaths(Array<String> texturePaths) {
            this.texturePaths = texturePaths;
        }

        public Array<GridPoint2> getGridPoints() {
            return gridPoints;
        }

        public void setGridPoints(Array<GridPoint2> gridPoints) {
            this.gridPoints = gridPoints;
        }

        public AnimationType getAnimationType() {
            return animationType;
        }

        public void setAnimationType(AnimationType animationType) {
            this.animationType = animationType;
        }
    }

}
