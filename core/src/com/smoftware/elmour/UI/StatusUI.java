package com.smoftware.elmour.UI;

import com.badlogic.gdx.Gdx;
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
    private static final String TAG = StatusUI.class.getSimpleName();

    private Array<StatusObserver> _observers;

    public StatusUI(){
        super("", Utility.STATUSUI_SKIN);

        _observers = new Array<StatusObserver>();
        ProfileManager.getInstance().addObserver(this);
    }

    public void setStat(Entity entity, EntityConfig.EntityProperties property, String value) {
        if (entity.getBattleEntityType() == Entity.BattleEntityType.PARTY) {
            ProfileManager.getInstance().setProperty(entity.getEntityConfig().getEntityID() + property.toString(), value);
        }
        else {
            entity.getEntityConfig().setPropertyValue(property.toString(), value);
        }
    }

    public String getStat(Entity entity, EntityConfig.EntityProperties property) {
        String value = "0";

        if (entity.getBattleEntityType() == Entity.BattleEntityType.PARTY) {
            String key = entity.getEntityConfig().getEntityID() + property.toString();
            if (!(ProfileManager.getInstance().getProperty(key, String.class) == null))
                value = ProfileManager.getInstance().getProperty(key, String.class);
        }
        else {
            value = entity.getEntityConfig().getPropertyValue(String.valueOf(property));
        }

        return value;
    }

    public int getHPValue(Entity entity) {
        int baseHP = Integer.parseInt(getStat(entity, EntityConfig.EntityProperties.HP));
        return baseHP;
    }

    public void setHPValue(Entity entity, int value) {
        int oldHP = Integer.parseInt(getStat(entity, EntityConfig.EntityProperties.HP));
        setStat(entity, EntityConfig.EntityProperties.HP, Integer.toString(value));

        if (oldHP == 0) {
            int newHP = Integer.parseInt(getStat(entity, EntityConfig.EntityProperties.HP));
            if (newHP > 0)
                notify(entity, value, StatusObserver.StatusEvent.IS_REVIVED);
        }
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_HP);
    }

    public int getHPMaxValue(Entity entity) {
        int baseHP_MAX = Integer.parseInt(getStat(entity, EntityConfig.EntityProperties.HP_MAX));
        return baseHP_MAX;
    }

    public void setHPMaxValue(Entity entity, int value) {
        setStat(entity, EntityConfig.EntityProperties.HP_MAX, Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_HP_MAX);
    }

    public int getMPValue(Entity entity) {
        int baseMP = Integer.parseInt(getStat(entity, EntityConfig.EntityProperties.MP));
        return baseMP;
    }

    public void setMPValue(Entity entity, int value) {
        setStat(entity, EntityConfig.EntityProperties.MP, Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_MP);
    }

    public int getMPMaxValue(Entity entity) {
        int baseMP_MAX = Integer.parseInt(getStat(entity, EntityConfig.EntityProperties.MP_MAX));
        return baseMP_MAX;
    }

    public void setMPMaxValue(Entity entity, int value) {
        setStat(entity, EntityConfig.EntityProperties.MP_MAX, Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_MP_MAX);
    }

    public int getATKValue(Entity entity) {
        int baseATK = Integer.parseInt(getStat(entity, EntityConfig.EntityProperties.ATK));
        return applyTurnEffects(entity, baseATK, InventoryElement.Effect.ATK_UP, InventoryElement.Effect.ATK_DOWN);
    }

    public void setATKValue(Entity entity, int value) {
        setStat(entity, EntityConfig.EntityProperties.ATK, Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_ATK);
    }

    public int getMATKValue(Entity entity) {
        int baseMATK = Integer.parseInt(getStat(entity, EntityConfig.EntityProperties.MATK));
        return applyTurnEffects(entity, baseMATK, InventoryElement.Effect.MATK_UP, InventoryElement.Effect.MATK_DOWN);
    }

    public void setMATKValue(Entity entity, int value) {
        setStat(entity, EntityConfig.EntityProperties.MATK, Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_MATK);
    }

    public int getDEFValue(Entity entity) {
        int baseDEF = Integer.parseInt(getStat(entity, EntityConfig.EntityProperties.DEF));
        return applyTurnEffects(entity, baseDEF, InventoryElement.Effect.DEF_UP, InventoryElement.Effect.DEF_DOWN);
    }

    public void setDEFValue(Entity entity, int value) {
        setStat(entity, EntityConfig.EntityProperties.DEF, Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_DEF);
    }

    public int getMDEFValue(Entity entity) {
        int baseMDEF = Integer.parseInt(getStat(entity, EntityConfig.EntityProperties.MDEF));
        return applyTurnEffects(entity, baseMDEF, InventoryElement.Effect.MDEF_UP, InventoryElement.Effect.MDEF_DOWN);
    }

    public void setMDEFValue(Entity entity, int value) {
        setStat(entity, EntityConfig.EntityProperties.MDEF, Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_MDEF);
    }

    public int getSPDValue(Entity entity) {
        int baseSPD = Integer.parseInt(getStat(entity, EntityConfig.EntityProperties.SPD));
        return applyTurnEffects(entity, baseSPD, InventoryElement.Effect.SPD_UP, InventoryElement.Effect.SPD_DOWN);
    }

    public void setSPDValue(Entity entity, int value) {
        setStat(entity, EntityConfig.EntityProperties.SPD, Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_SPD);
    }

    public int getACCValue(Entity entity) {
        int baseACC = Integer.parseInt(getStat(entity, EntityConfig.EntityProperties.ACC));
        return applyTurnEffects(entity, baseACC, InventoryElement.Effect.ACC_UP, InventoryElement.Effect.ACC_DOWN);
    }

    public void setACCValue(Entity entity, int value) {
        setStat(entity, EntityConfig.EntityProperties.ACC, Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_ACC);
    }

    public int getLCKValue(Entity entity) {
        int baseLCK = Integer.parseInt(getStat(entity, EntityConfig.EntityProperties.LCK));
        return applyTurnEffects(entity, baseLCK, InventoryElement.Effect.LCK_UP, InventoryElement.Effect.LCK_DOWN);
    }

    public void setLCKValue(Entity entity, int value) {
        setStat(entity, EntityConfig.EntityProperties.LCK, Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_LCK);
    }

    public int getAVOValue(Entity entity) {
        int baseAVO = Integer.parseInt(getStat(entity, EntityConfig.EntityProperties.AVO));
        return applyTurnEffects(entity, baseAVO, InventoryElement.Effect.AVO_UP, InventoryElement.Effect.AVO_DOWN);
    }

    public void setAVOValue(Entity entity, int value) {
        setStat(entity, EntityConfig.EntityProperties.AVO, Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_AVO);
    }

    public int getDibsValue() {
        // Dibs is only in profile properties
        return ProfileManager.getInstance().getProperty("Dibs", Integer.class);
    }

    public int getXPValue(Entity entity) {
        int baseMP_MAX = Integer.parseInt(getStat(entity, EntityConfig.EntityProperties.EXP));
        return baseMP_MAX;
    }

    public void setXPValue(Entity entity, int value) {
        setStat(entity, EntityConfig.EntityProperties.EXP, Integer.toString(value));
        notify(entity, value, StatusObserver.StatusEvent.UPDATED_XP);
    }

    public void updatePartyXP(int value) {
        notify(value, StatusObserver.StatusEvent.UPDATED_PARTY_XP);
    }

    public void setDibsValue(int value) {
        ProfileManager.getInstance().setProperty("Dibs", value);
        notify(value, StatusObserver.StatusEvent.UPDATED_DIBS);
    }

    public void updatePartyDibs(int value) {
        int dibs = getDibsValue();
        dibs += value;
        if (dibs < 0) dibs = 0;
        setDibsValue(dibs);
    }

    public int applyTurnEffects(Entity entity, int baseVal, InventoryElement.Effect effectUP, InventoryElement.Effect effectDOWN) {
        // apply any effect items to base value
        // effect changes are cumulative so need to loop through entire list
        int changePercent = 0;
        for (int i = 0; i < entity.getEntityConfig().getTurnEffectListSize(); i++) {
            InventoryElement.EffectItem effectItem = entity.getEntityConfig().getTurnEffectListItem(i);

            if (effectItem.effect.equals(effectUP))
                changePercent += effectItem.value;
            else if (effectItem.effect.equals(effectDOWN))
                changePercent -= effectItem.value;
        }

        int retVal = Utility.applyPercentageAndRoundUp(baseVal, changePercent);

        if (baseVal != retVal) {
            String units = effectUP.toString().substring(0, effectUP.toString().indexOf("_"));
            Gdx.app.log(TAG, entity.getEntityConfig().getDisplayName() + ": Applied change percent of " + changePercent + " for " + effectUP.toString() + "/" + effectDOWN.toString() +
                    " turn effects to base value of " + baseVal + " " + units + ", resulting in " + retVal + " " + units);
        }

        return retVal;
    }

    public Array<InventoryElement.Effect> getCurrentStatusArrayForEntity(Entity entity) {
        Array<InventoryElement.Effect> statusArray = new Array<>();

        statusArray.add(getEffectStatusByProperty(entity, EntityConfig.EntityProperties.ACC));
        statusArray.add(getEffectStatusByProperty(entity, EntityConfig.EntityProperties.ATK));
        statusArray.add(getEffectStatusByProperty(entity, EntityConfig.EntityProperties.AVO));
        statusArray.add(getEffectStatusByProperty(entity, EntityConfig.EntityProperties.DEF));
        statusArray.add(getEffectStatusByProperty(entity, EntityConfig.EntityProperties.MATK));
        statusArray.add(getEffectStatusByProperty(entity, EntityConfig.EntityProperties.MDEF));
        statusArray.add(getEffectStatusByProperty(entity, EntityConfig.EntityProperties.LCK));
        statusArray.add(getEffectStatusByProperty(entity, EntityConfig.EntityProperties.SPD));

        // todo: need to handle DIBS and XP and DROPS differently
        statusArray.add(getEffectStatusByProperty(entity, EntityConfig.EntityProperties.DIBS));
        statusArray.add(getEffectStatusByProperty(entity, EntityConfig.EntityProperties.DROPS));
        statusArray.add(getEffectStatusByProperty(entity, EntityConfig.EntityProperties.EXP));

        return statusArray;
    }

    public InventoryElement.Effect getEffectStatusByProperty(Entity entity, EntityConfig.EntityProperties property) {
        InventoryElement.Effect effect = InventoryElement.Effect.NONE;
        int currentValue = 0;
        InventoryElement.Effect effectUP = InventoryElement.Effect.valueOf(property.name() + "_UP");
        InventoryElement.Effect effectDOWN = InventoryElement.Effect.valueOf(property.name() + "_DOWN");

        for (int i = 0; i < entity.getEntityConfig().getTurnEffectListSize(); i++) {
            InventoryElement.EffectItem effectItem = entity.getEntityConfig().getTurnEffectListItem(i);

            if (effectItem.effect.equals(effectUP))
                currentValue += effectItem.value;
            else if (effectItem.effect.equals(effectDOWN))
                currentValue -= effectItem.value;
        }

        if (currentValue > 0)
            return InventoryElement.Effect.valueOf(property.name() + "_UP");
        else if (currentValue < 0)
            return InventoryElement.Effect.valueOf(property.name() + "_DOWN");
        else
            return InventoryElement.Effect.valueOf(property.name() + "_NORMAL");
    }

    public void printCurrentStatusForEntity(Entity entity) {
        Gdx.app.log(TAG, "-->");
        Gdx.app.log(TAG, "Current status for " + entity.getEntityConfig().getEntityID() + ":");

        for (int i = 0; i < entity.getEntityConfig().getTurnEffectListSize(); i++) {
            InventoryElement.EffectItem effectItem = entity.getEntityConfig().getTurnEffectListItem(i);
            Gdx.app.log(TAG, effectItem.effect.toString() + " : " + effectItem.value + " : (" + effectItem.turns + ") turns remaining");
        }
        Gdx.app.log(TAG, "---------------------------------");
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

            key = entityID + EntityConfig.EntityProperties.MATK.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MATK.toString().toString());
            ProfileManager.getInstance().setProperty(key, property);

            key = entityID + EntityConfig.EntityProperties.DEF.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.DEF.toString().toString());
            ProfileManager.getInstance().setProperty(key, property);

            key = entityID + EntityConfig.EntityProperties.MDEF.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MDEF.toString().toString());
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

            key = entityID + EntityConfig.EntityProperties.MATK.toString();
            property = ProfileManager.getInstance().getProperty(key, String.class);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.MATK.toString(), property);

            key = entityID + EntityConfig.EntityProperties.DEF.toString();
            property = ProfileManager.getInstance().getProperty(key, String.class);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.DEF.toString(), property);

            key = entityID + EntityConfig.EntityProperties.MDEF.toString();
            property = ProfileManager.getInstance().getProperty(key, String.class);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.MDEF.toString(), property);

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
