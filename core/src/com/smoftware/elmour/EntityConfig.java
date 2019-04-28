package com.smoftware.elmour;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.smoftware.elmour.Entity.AnimationType;

public class EntityConfig {
    private Array<AnimationConfig> animationConfig;
    private Array<ItemReward> rewardItems;
    private Array<SpellPowerElement.ElementID> spellPowerElementIDs;
    private Entity.State state = Entity.State.IDLE;
    private Entity.Direction direction = Entity.Direction.DOWN;
    private String entityID;
    private String conversationConfigPath;
    private String questConfigPath;
    private Array<String> questConfigPaths;
    private Array<ConversationConfig> conversationConfigs;
    private String currentQuestID;
    private InventoryElement.WeaponType preferredWeaponType;
    private ObjectMap<String, String> entityProperties;
    private String entityBoundsName;
    private String displayName;
    private Array<EntityAbility> entityAbilities;
    private Array<InventoryElement.EffectItem> turnEffectList;

    public enum EntityProperties{
        HP,
        HP_MAX,
        MP,         //characters only
        MP_MAX,     //characters only
        ATK,
        MATK,
        DEF,
        MDEF,
        SPD,
        ACC,
        LCK,
        AVO,
        XP_REWARD,  //monsters only
        DIBS_REWARD,//monsters only
        /////////////
        // needed for status arrow handling
        DIBS,
        DROPS,
        EXP,
        /////////////
        SPELL_LIST,
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
        public InventoryElement.ElementID itemID;
        public int probability;
    }

    public enum ConversationType {
        NORMAL_DIALOG, PRE_QUEST_CUTSCENE, ACTIVE_QUEST_DIALOG, ACTIVE_QUEST_CUTSCENE, POST_QUEST_DIALOG
    }

    public static class ConversationConfig {
        public int chapter;
        public ConversationType type;
        public String config;          // Depending on type, config can be path to .json config file, cut scene name
        public String questConfigPath; // This is only used if type is PRE_QUEST_CUTSCENE
    }

    EntityConfig(){
        animationConfig = new Array<>();
        entityProperties = new ObjectMap<>();
        rewardItems = new Array<>();
        entityAbilities = new Array<>();
        spellPowerElementIDs = new Array<>();
        questConfigPaths = new Array<>();
        conversationConfigs = new Array<>();
        preferredWeaponType = InventoryElement.WeaponType.NONE;

        if (turnEffectList == null)
            turnEffectList = new Array<InventoryElement.EffectItem>();

        //Test code to write to Json file
        ConversationConfig config = new ConversationConfig();
        config.chapter = 1;
        config.type = ConversationType.PRE_QUEST_CUTSCENE;
        config.config = "config.json";
        config.questConfigPath = "questConfig.json";
        conversationConfigs.add(config);;

        Json _json = new Json();
        String fileData = _json.prettyPrint(_json.toJson(conversationConfigs));

        if( Gdx.files.isLocalStorageAvailable() ) {
            FileHandle file = Gdx.files.local("test.json");
            file.writeString(fileData, false);
        }

/*
        spellPowerElementIDs.add(SpellPowerElement.ElementID.EARTH);
        spellPowerElementIDs.add(SpellPowerElement.ElementID.WATER);
        spellPowerElementIDs.add(SpellPowerElement.ElementID.FIRE);
        Json _json = new Json();
        String fileData = _json.prettyPrint(_json.toJson(spellPowerElementIDs));

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
        preferredWeaponType = config.getPreferredWeaponType();

        animationConfig = new Array<>();
        animationConfig.addAll(config.getAnimationConfig());

        entityProperties = new ObjectMap<>();
        entityProperties.putAll(config.entityProperties);

        rewardItems = new Array<>();
        rewardItems.addAll(config.getRewardItems());

        spellPowerElementIDs = new Array<>();
        spellPowerElementIDs.addAll(config.getSpellPowerElementIDs());

        questConfigPaths = new Array<>();
        questConfigPaths.addAll(config.getQuestConfigPaths());

        conversationConfigs = new Array<>();
        conversationConfigs.addAll(config.getConversationConfigs());

        if (turnEffectList == null)
            turnEffectList = new Array<>();
    }

    public int getTurnEffectListSize() { return turnEffectList.size; }

    public InventoryElement.EffectItem getTurnEffectListItem(int index) { return turnEffectList.get(index); }

    public void setTurnEffectListItem(int index, InventoryElement.EffectItem item) { turnEffectList.set(index, item); }

    public void addTurnEffectItem(InventoryElement.EffectItem item) { turnEffectList.add(item); }

    public void removeTurnEffectItem(int index) { turnEffectList.removeIndex(index); }

    public void clearTurnEffectList() { turnEffectList.clear(); }

    public ObjectMap<String, String> getEntityProperties() {
        return entityProperties;
    }

    public void setEntityProperties(ObjectMap<String, String> entityProperties) {
        this.entityProperties = entityProperties;
    }

    public void setPropertyValue(String key, String value){ entityProperties.put(key, value); }

    public String getPropertyValue(String key){
        Object propertyVal = entityProperties.get(key);
        if( propertyVal == null ) return new String();
        return propertyVal.toString();
    }

    public InventoryElement.WeaponType getPreferredWeaponType() { return preferredWeaponType; }

    public void setPreferredWeaponType(InventoryElement.WeaponType preferredWeaponType) { this.preferredWeaponType = preferredWeaponType; }

    public String getCurrentQuestID() { return currentQuestID; }

    public void setCurrentQuestID(String currentQuestID) {
        this.currentQuestID = currentQuestID;
    }

    public String getQuestConfigPath() {
        return questConfigPath;
    }

    public void setQuestConfigPath(String questConfigPath) { this.questConfigPath = questConfigPath; }

    public Array<String> getQuestConfigPaths() { return questConfigPaths; }

    public Array<ConversationConfig> getConversationConfigs() { return conversationConfigs; }

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

    public Array<ItemReward> getRewardItems() { return rewardItems; }

    public Array<SpellPowerElement.ElementID> getSpellPowerElementIDs() { return spellPowerElementIDs; }

    public void setSpellPowerElementIDs(Array<SpellPowerElement.ElementID> ids) { this.spellPowerElementIDs = ids; }

    public void addSpellPowerElementID(SpellPowerElement.ElementID id) { spellPowerElementIDs.add(id); }

    static public class AnimationConfig{
        private float frameDuration = 1.0f;
        private int frameWidth = 16;
        private int frameHeight = 16;
        private AnimationType animationType;
        private Array<String> texturePaths;
        private Array<GridPoint2> gridPoints;
        private boolean looping;

        public AnimationConfig(){
            animationType = AnimationType.IDLE;
            texturePaths = new Array<String>();
            gridPoints = new Array<GridPoint2>();
            looping = true;
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

        public void setTexturePaths(Array<String> texturePaths) { this.texturePaths = texturePaths; }

        public Array<GridPoint2> getGridPoints() {
            return gridPoints;
        }

        public void setGridPoints(Array<GridPoint2> gridPoints) {
            this.gridPoints = gridPoints;
        }

        public AnimationType getAnimationType() {
            return animationType;
        }

        public void setAnimationType(AnimationType animationType) { this.animationType = animationType; }

        public boolean getLooping() { return looping; }
    }

}
