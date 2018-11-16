package com.smoftware.elmour.UI;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.EntityConfig;
import com.smoftware.elmour.EntityFactory;
import com.smoftware.elmour.InventoryElement;
import com.smoftware.elmour.Utility;
import com.smoftware.elmour.battle.LevelTable;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.profile.ProfileObserver;

public class StatusUI extends Window implements StatusSubject, ProfileObserver {


    private Array<StatusObserver> _observers;

    public StatusUI(){
        super("", Utility.STATUSUI_SKIN);

        _observers = new Array<StatusObserver>();
    }

    public void setStat(String key, String value) {
        ProfileManager.getInstance().setProperty(key, value);
    }

    public String getStat(String key) {
        String value = "0";
        if (!(ProfileManager.getInstance().getProperty(key, String.class) == null))
            value = ProfileManager.getInstance().getProperty(key, String.class);

        return value;
    }

    public int getDibsValue() {
        return Integer.parseInt(getStat("Dibs"));
    }

    public void setDibsValue(int value) {
        setStat("Dibs", Integer.toString(value));
    }

    public int getHPValue(Entity entity) {
        return Integer.parseInt(getStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.HP.toString()));
    }

    public void setHPValue(Entity entity, int value) {
        setStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.HP.toString(), Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_HP);
    }

    public int getHPMaxValue(Entity entity) {
        return Integer.parseInt(getStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.HP_MAX.toString()));
    }

    public void setHPMaxValue(Entity entity, int value) {
        setStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.HP_MAX.toString(), Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_HP_MAX);
    }

    public int getMPValue(Entity entity) {
        return Integer.parseInt(getStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.MP.toString()));
    }

    public void setMPValue(Entity entity, int value) {
        setStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.MP.toString(), Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_MP);
    }

    public int getMPMaxValue(Entity entity) {
        return Integer.parseInt(getStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.MP_MAX.toString()));
    }

    public void setMPMaxValue(Entity entity, int value) {
        setStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.MP_MAX.toString(), Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_MP_MAX);
    }

    public int getATKValue(Entity entity) {
        return Integer.parseInt(getStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.ATK.toString()));
    }

    public void setATKValue(Entity entity, int value) {
        setStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.ATK.toString(), Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_ATK);
    }

    public int getMagicATKValue(Entity entity) {
        return Integer.parseInt(getStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.MagicATK.toString()));
    }

    public void setMagicATKValue(Entity entity, int value) {
        setStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.MagicATK.toString(), Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_MagicATK);
    }

    public int getDEFValue(Entity entity) {
        return Integer.parseInt(getStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.DEF.toString()));
    }

    public void setDEFValue(Entity entity, int value) {
        setStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.DEF.toString(), Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_DEF);
    }

    public int getMagicDEFValue(Entity entity) {
        return Integer.parseInt(getStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.MagicDEF.toString()));
    }

    public void setMagicDEFValue(Entity entity, int value) {
        setStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.MagicDEF.toString(), Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_MagicDEF);
    }

    public int getSPDValue(Entity entity) {
        int baseSPD = Integer.parseInt(getStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.SPD.toString()));
        return applyTurnEffects(entity, baseSPD, InventoryElement.Effect.SPD_UP, InventoryElement.Effect.SPD_DOWN);
    }

    public void setSPDValue(Entity entity, int value) {
        setStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.SPD.toString(), Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_SPD);
    }

    public int getACCValue(Entity entity) {
        return Integer.parseInt(getStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.ACC.toString()));
    }

    public void setACCValue(Entity entity, int value) {
        setStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.ACC.toString(), Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_ACC);
    }

    public int getLCKValue(Entity entity) {
        return Integer.parseInt(getStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.LCK.toString()));
    }

    public void setLCKValue(Entity entity, int value) {
        setStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.LCK.toString(), Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_LCK);
    }

    public int getAVOValue(Entity entity) {
        return Integer.parseInt(getStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.AVO.toString()));
    }

    public void setAVOValue(Entity entity, int value) {
        setStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.AVO.toString(), Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_AVO);
    }

    public int getXPRewardValue(Entity entity) {
        // XP_REWARD comes from entity properties, not profile
        return Integer.parseInt(entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.XP_REWARD.toString().toString()));
    }

    public int getDibsRewardValue(Entity entity) {
        // DIBS_REWARD comes from entity properties, not profile
        return Integer.parseInt(entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.DIBS_REWARD.toString().toString()));
    }

    private int applyTurnEffects(Entity entity, int baseVal, InventoryElement.Effect effectUP, InventoryElement.Effect effectDOWN) {
        // apply any effect items to base value
        int changePercent = 0;
        for (int i = 0; i < entity.getEntityConfig().getTurnEffectListSize(); i++) {
            InventoryElement.EffectItem effectItem = entity.getEntityConfig().getTurnEffectListItem(i);

            if (effectItem.effect.equals(effectUP))
                changePercent += effectItem.value;
            else if (effectItem.effect.equals(effectDOWN))
                changePercent -= effectItem.value;
        }
        return Utility.applyPercentageAndRoundUp(baseVal, changePercent);
    }

    @Override
    public void addObserver(StatusObserver statusObserver) {
        _observers.add(statusObserver);
    }

    @Override
    public void removeObserver(StatusObserver statusObserver) {
        _observers.removeValue(statusObserver, true);
    }

    @Override
    public void removeAllObservers() {
        for(StatusObserver observer: _observers){
            _observers.removeValue(observer, true);
        }
    }

    @Override
    public void notify(int value, StatusObserver.StatusEvent event) {
        for(StatusObserver observer: _observers){
            observer.onNotify(value, event);
        }
    }

    @Override
    public void notify(Entity entity, int value, StatusObserver.StatusEvent event) {
        for(StatusObserver observer: _observers){
            observer.onNotify(entity, value, event);
        }
    }

    @Override
    public void onNotify(ProfileManager profileManager, ProfileEvent event) {
        switch(event){
            case PROFILE_LOADED:
                boolean firstTime = profileManager.getIsNewProfile();

                if (firstTime) {
                    // load default stats
                    setDefaultStatProperties(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.CARMEN));
                    setDefaultStatProperties(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.CHARACTER_1));
                    setDefaultStatProperties(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.CHARACTER_2));
                    setDefaultStatProperties(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.DOUGLAS));
                    setDefaultStatProperties(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.JUSTIN));
                    setDefaultStatProperties(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.JAXON_1));

                    // start with some dibs
                    setDibsValue(20);
                }
                else {
                    /*
                    int goldVal = profileManager.getProperty("currentPlayerGP", Integer.class);

                    Array<InventoryItemLocation> inventory = profileManager.getProperty("playerInventory", Array.class);
                    InventoryUI.populateInventory(_inventoryUI.getInventorySlotTable(), inventory, _inventoryUI.getDragAndDrop(), InventoryUI.PLAYER_INVENTORY, false);

                    Array<InventoryItemLocation> equipInventory = profileManager.getProperty("playerEquipInventory", Array.class);
                    if( equipInventory != null && equipInventory.size > 0 ){
                        _inventoryUI.resetEquipSlots();
                        InventoryUI.populateInventory(_inventoryUI.getEquipSlotTable(), equipInventory, _inventoryUI.getDragAndDrop(), InventoryUI.PLAYER_INVENTORY, false);
                    }

                    Array<QuestGraph> quests = profileManager.getProperty("playerQuests", Array.class);
                    _questUI.setQuests(quests);

                    int xpMaxVal = profileManager.getProperty("currentPlayerXPMax", Integer.class);
                    int xpVal = profileManager.getProperty("currentPlayerXP", Integer.class);

                    int hpMaxVal = profileManager.getProperty("currentPlayerHPMax", Integer.class);
                    int hpVal = profileManager.getProperty("currentPlayerHP", Integer.class);

                    int mpMaxVal = profileManager.getProperty("currentPlayerMPMax", Integer.class);
                    int mpVal = profileManager.getProperty("currentPlayerMP", Integer.class);

                    int levelVal = profileManager.getProperty("currentPlayerLevel", Integer.class);

                    //set the current max values first
                    _statusUI.setXPValueMax(xpMaxVal);
                    _statusUI.setHPValueMax(hpMaxVal);
                    _statusUI.setMPValueMax(mpMaxVal);

                    _statusUI.setXPValue(xpVal);
                    _statusUI.setHPValue(hpVal);
                    _statusUI.setMPValue(mpVal);

                    //then add in current values
                    _statusUI.setGoldValue(goldVal);
                    _statusUI.setLevelValue(levelVal);

                    float totalTime = profileManager.getProperty("currentTime", Float.class);
                    _clock.setTotalTime(totalTime);
                    */
                }

                break;
            case SAVING_PROFILE:
                /*
                profileManager.setProperty("playerQuests", _questUI.getQuests());
                profileManager.setProperty("playerInventory", InventoryUI.getInventory(_inventoryUI.getInventorySlotTable()));
                profileManager.setProperty("playerEquipInventory", InventoryUI.getInventory(_inventoryUI.getEquipSlotTable()));
                profileManager.setProperty("currentPlayerGP", _statusUI.getGoldValue() );
                profileManager.setProperty("currentPlayerLevel", _statusUI.getLevelValue() );
                profileManager.setProperty("currentPlayerXP", _statusUI.getXPValue() );
                profileManager.setProperty("currentPlayerXPMax", _statusUI.getXPValueMax() );
                profileManager.setProperty("currentPlayerHP", _statusUI.getHPValue() );
                profileManager.setProperty("currentPlayerHPMax", _statusUI.getHPValueMax() );
                profileManager.setProperty("currentPlayerMP", _statusUI.getMPValue() );
                profileManager.setProperty("currentPlayerMPMax", _statusUI.getMPValueMax() );
                profileManager.setProperty("currentTime", _clock.getTotalTime());
                */
                break;
            case CLEAR_CURRENT_PROFILE:
                // set default profile
                /*
                profileManager.setProperty("playerQuests", new Array<QuestGraph>());
                profileManager.setProperty("playerInventory", new Array<InventoryItemLocation>());
                profileManager.setProperty("playerEquipInventory", new Array<InventoryItemLocation>());
                profileManager.setProperty("currentPlayerGP", 0 );
                profileManager.setProperty("currentPlayerLevel",0 );
                profileManager.setProperty("currentPlayerXP", 0 );
                profileManager.setProperty("currentPlayerXPMax", 0 );
                profileManager.setProperty("currentPlayerHP", 0 );
                profileManager.setProperty("currentPlayerHPMax", 0 );
                profileManager.setProperty("currentPlayerMP", 0 );
                profileManager.setProperty("currentPlayerMPMax", 0 );
                profileManager.setProperty("currentTime", 0);
                profileManager.setProperty("CHARACTER_1", "Purple Boy");
                profileManager.setProperty("CHARACTER_2", "Girl");*/
                break;
            default:
                break;
        }
    }

    private void setDefaultStatProperties(Entity entity) {
        // this function gets the stats from the entity properties and sets them in the profile
        // this should only be used to set default values
        String entityID = entity.getEntityConfig().getEntityID();
        String key;
        String property;

        // set stat properties only if they are not already set, or if an update is being made
        key = entityID + EntityConfig.EntityProperties.HP.toString();
        property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP.toString().toString());

        if (ProfileManager.getInstance().getProperty(key, String.class) == null) {
            ProfileManager.getInstance().setProperty(key, property);

            // we can assume all other status values need to be set since they are always done in a batch here
            key = entityID + EntityConfig.EntityProperties.HP_MAX.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP_MAX.toString().toString());
            ProfileManager.getInstance().setProperty(key, property);

            key = entityID + EntityConfig.EntityProperties.MP.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MP.toString().toString());
            ProfileManager.getInstance().setProperty(key, property);

            key = entityID + EntityConfig.EntityProperties.MP_MAX.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MP_MAX.toString().toString());
            ProfileManager.getInstance().setProperty(key, property);

            key = entityID + EntityConfig.EntityProperties.ATK.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.ATK.toString().toString());
            ProfileManager.getInstance().setProperty(key, property);

            key = entityID + EntityConfig.EntityProperties.MagicATK.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MagicATK.toString().toString());
            ProfileManager.getInstance().setProperty(key, property);

            key = entityID + EntityConfig.EntityProperties.DEF.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.DEF.toString().toString());
            ProfileManager.getInstance().setProperty(key, property);

            key = entityID + EntityConfig.EntityProperties.MagicDEF.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MagicDEF.toString().toString());
            ProfileManager.getInstance().setProperty(key, property);

            key = entityID + EntityConfig.EntityProperties.SPD.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.SPD.toString().toString());
            ProfileManager.getInstance().setProperty(key, property);

            key = entityID + EntityConfig.EntityProperties.ACC.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.ACC.toString().toString());
            ProfileManager.getInstance().setProperty(key, property);

            key = entityID + EntityConfig.EntityProperties.LCK.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.LCK.toString().toString());
            ProfileManager.getInstance().setProperty(key, property);

            key = entityID + EntityConfig.EntityProperties.AVO.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.AVO.toString().toString());
            ProfileManager.getInstance().setProperty(key, property);
        }
    }
/*
    public void getAllStatProperties(Entity entity) {
        String entityID = entity.getEntityConfig().getEntityID();
        String key;

        key = entityID + EntityConfig.EntityProperties.HP.toString();
        if (!ProfileManager.getInstance().getProperty(key, String.class).equals(null)) {
            String property = ProfileManager.getInstance().getProperty(key, String.class);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.HP.toString(), property);

            // we can assume all other status values exists since they are always set in a batch in setAllStatProperties
            key = entityID + EntityConfig.EntityProperties.HP_MAX.toString();
            property = ProfileManager.getInstance().getProperty(key, String.class);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.HP_MAX.toString(), property);

            key = entityID + EntityConfig.EntityProperties.MP.toString();
            property = ProfileManager.getInstance().getProperty(key, String.class);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.MP.toString(), property);

            key = entityID + EntityConfig.EntityProperties.MP_MAX.toString();
            property = ProfileManager.getInstance().getProperty(key, String.class);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.MP_MAX.toString(), property);

            key = entityID + EntityConfig.EntityProperties.ATK.toString();
            property = ProfileManager.getInstance().getProperty(key, String.class);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.ATK.toString(), property);

            key = entityID + EntityConfig.EntityProperties.MagicATK.toString();
            property = ProfileManager.getInstance().getProperty(key, String.class);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.MagicATK.toString(), property);

            key = entityID + EntityConfig.EntityProperties.DEF.toString();
            property = ProfileManager.getInstance().getProperty(key, String.class);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.DEF.toString(), property);

            key = entityID + EntityConfig.EntityProperties.MagicDEF.toString();
            property = ProfileManager.getInstance().getProperty(key, String.class);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.MagicDEF.toString(), property);

            key = entityID + EntityConfig.EntityProperties.SPD.toString();
            property = ProfileManager.getInstance().getProperty(key, String.class);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.SPD.toString(), property);

            key = entityID + EntityConfig.EntityProperties.ACC.toString();
            property = ProfileManager.getInstance().getProperty(key, String.class);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.ACC.toString(), property);

            key = entityID + EntityConfig.EntityProperties.LCK.toString();
            property = ProfileManager.getInstance().getProperty(key, String.class);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.LCK.toString(), property);

            key = entityID + EntityConfig.EntityProperties.AVO.toString();
            property = ProfileManager.getInstance().getProperty(key, String.class);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.AVO.toString(), property);
        }
    }*/
}
