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
import com.smoftware.elmour.Utility;
import com.smoftware.elmour.battle.LevelTable;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.profile.ProfileObserver;

public class StatusUI extends Window implements StatusSubject, ProfileObserver {
    private Image _hpBar;
    private Image _mpBar;
    private Image _xpBar;

    private ImageButton _inventoryButton;
    private ImageButton _questButton;
    private Array<StatusObserver> _observers;

    private Array<LevelTable> _levelTables;
    private static final String LEVEL_TABLE_CONFIG = "scripts/level_tables.json";

    //Attributes
    private int _levelVal = -1;
    private int _goldVal = -1;
    private int _hpVal = -1;
    private int _mpVal = -1;
    private int _xpVal = 0;

    private int _xpCurrentMax = -1;
    private int _hpCurrentMax = -1;
    private int _mpCurrentMax = -1;

    private Label _hpValLabel;
    private Label _mpValLabel;
    private Label _xpValLabel;
    private Label _levelValLabel;
    private Label _goldValLabel;

    private float _barWidth = 0;
    private float _barHeight = 0;

    public StatusUI(){
        super("", Utility.STATUSUI_SKIN);

        _levelTables = LevelTable.getLevelTables(LEVEL_TABLE_CONFIG);

        _observers = new Array<StatusObserver>();

        //groups
        WidgetGroup group = new WidgetGroup();
        WidgetGroup group2 = new WidgetGroup();
        WidgetGroup group3 = new WidgetGroup();

        //images
        _hpBar = new Image(Utility.STATUSUI_TEXTUREATLAS.findRegion("HP_Bar"));
        Image bar = new Image(Utility.STATUSUI_TEXTUREATLAS.findRegion("Bar"));
        _mpBar = new Image(Utility.STATUSUI_TEXTUREATLAS.findRegion("MP_Bar"));
        Image bar2 = new Image(Utility.STATUSUI_TEXTUREATLAS.findRegion("Bar"));
        _xpBar = new Image(Utility.STATUSUI_TEXTUREATLAS.findRegion("XP_Bar"));
        Image bar3 = new Image(Utility.STATUSUI_TEXTUREATLAS.findRegion("Bar"));

        _barWidth = _hpBar.getWidth();
        _barHeight = _hpBar.getHeight();


        //labels
        Label hpLabel = new Label(" hp: ", Utility.STATUSUI_SKIN);
        _hpValLabel = new Label(String.valueOf(_hpVal), Utility.STATUSUI_SKIN);
        Label mpLabel = new Label(" mp: ", Utility.STATUSUI_SKIN);
        _mpValLabel = new Label(String.valueOf(_mpVal), Utility.STATUSUI_SKIN);
        Label xpLabel = new Label(" xp: ", Utility.STATUSUI_SKIN);
        _xpValLabel = new Label(String.valueOf(_xpVal), Utility.STATUSUI_SKIN);
        Label levelLabel = new Label(" lv: ", Utility.STATUSUI_SKIN);
        _levelValLabel = new Label(String.valueOf(_levelVal), Utility.STATUSUI_SKIN);
        Label goldLabel = new Label(" gp: ", Utility.STATUSUI_SKIN);
        _goldValLabel = new Label(String.valueOf(_goldVal), Utility.STATUSUI_SKIN);

        //buttons
        _inventoryButton= new ImageButton(Utility.STATUSUI_SKIN, "inventory-button");
        _inventoryButton.getImageCell().size(32, 32);

        _questButton = new ImageButton(Utility.STATUSUI_SKIN, "quest-button");
        _questButton.getImageCell().size(32,32);

        //Align images
        _hpBar.setPosition(3, 6);
        _mpBar.setPosition(3, 6);
        _xpBar.setPosition(3, 6);

        //add to widget groups
        group.addActor(bar);
        group.addActor(_hpBar);
        group2.addActor(bar2);
        group2.addActor(_mpBar);
        group3.addActor(bar3);
        group3.addActor(_xpBar);

        //Add to layout
        defaults().expand().fill();

        //account for the title padding
        //this.pad(this.getPadTop() + 10, 10, 10, 10);

        //this.add();
        //this.add(_questButton).align(Align.center);
        //this.add(_inventoryButton).align(Align.right);
        //this.row();

        this.add(group).size(bar.getWidth(), bar.getHeight()).padRight(10);
        this.add(hpLabel);
        this.add(_hpValLabel).align(Align.left);
        this.row();

        this.add(group2).size(bar2.getWidth(), bar2.getHeight()).padRight(10);
        this.add(mpLabel);
        this.add(_mpValLabel).align(Align.left);
        this.row();

        this.add(group3).size(bar3.getWidth(), bar3.getHeight()).padRight(10);
        this.add(xpLabel);
        this.add(_xpValLabel).align(Align.left).padRight(20);
        this.row();

        this.add(levelLabel).align(Align.left);
        this.add(_levelValLabel).align(Align.left);
        this.row();
        this.add(goldLabel);
        this.add(_goldValLabel).align(Align.left);

        //this.debug();
        this.pack();
    }

    public ImageButton getInventoryButton() {
        return _inventoryButton;
    }

    public ImageButton getQuestButton() {
        return _questButton;
    }

    public int getLevelValue(){
        return _levelVal;
    }
    public void setLevelValue(int levelValue){
        this._levelVal = levelValue;
        _levelValLabel.setText(String.valueOf(_levelVal));
        notify(_levelVal, StatusObserver.StatusEvent.UPDATED_LEVEL);
    }

    public int getGoldValue(){
        return _goldVal;
    }
    public void setGoldValue(int goldValue){
        this._goldVal = goldValue;
        _goldValLabel.setText(String.valueOf(_goldVal));
        notify(_goldVal, StatusObserver.StatusEvent.UPDATED_GP);
    }

    public void addGoldValue(int goldValue){
        this._goldVal += goldValue;
        _goldValLabel.setText(String.valueOf(_goldVal));
        notify(_goldVal, StatusObserver.StatusEvent.UPDATED_GP);
    }

    public int getXPValue(){
        return _xpVal;
    }

    public void addXPValue(int xpValue){
        this._xpVal += xpValue;

        if( _xpVal > _xpCurrentMax ){
            updateToNewLevel();
        }

        _xpValLabel.setText(String.valueOf(_xpVal));

        updateBar(_xpBar, _xpVal, _xpCurrentMax);

        notify(_xpVal, StatusObserver.StatusEvent.UPDATED_XP);
    }

    public void setXPValue(int xpValue){
        this._xpVal = xpValue;

        if( _xpVal > _xpCurrentMax ){
            updateToNewLevel();
        }

        _xpValLabel.setText(String.valueOf(_xpVal));

        updateBar(_xpBar, _xpVal, _xpCurrentMax);

        notify(_xpVal, StatusObserver.StatusEvent.UPDATED_XP);
    }

    public void setXPValueMax(int maxXPValue){
        this._xpCurrentMax = maxXPValue;
    }

    public void setStatusForLevel(int level){
        for( LevelTable table: _levelTables ){
            if( Integer.parseInt(table.getLevelID()) == level ){
                setXPValueMax(table.getXpMax());
                setXPValue(0);

                setHPValueMax(table.getHpMax());
                setHPValue(table.getHpMax());

                setMPValueMax(table.getMpMax());
                setMPValue(table.getMpMax());

                setLevelValue(Integer.parseInt(table.getLevelID()));
                return;
            }
        }
    }

    public void updateToNewLevel(){
        for( LevelTable table: _levelTables ){
            //System.out.println("XPVAL " + _xpVal + " table XPMAX " + table.getXpMax() );
            if( _xpVal > table.getXpMax() ){
                continue;
            }else{
                setXPValueMax(table.getXpMax());

                setHPValueMax(table.getHpMax());
                setHPValue(table.getHpMax());

                setMPValueMax(table.getMpMax());
                setMPValue(table.getMpMax());

                setLevelValue(Integer.parseInt(table.getLevelID()));
                notify(_levelVal, StatusObserver.StatusEvent.LEVELED_UP);
                return;
            }
        }
    }

    public int getXPValueMax(){
        return _xpCurrentMax;
    }

    //HP
    public int getHPValue(){
        return _hpVal;
    }

    public String getStat(String key) {
        String value = "0";
        if (!ProfileManager.getInstance().getProperty(key, String.class).equals(null))
            value = ProfileManager.getInstance().getProperty(key, String.class);

        return value;
    }

    public String getHPValue(Entity entity) {
        return getStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.HP.toString());
    }

    public String getHPMaxValue(Entity entity) {
        return getStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.HP_MAX.toString());
    }

    public String getMPValue(Entity entity) {
        return getStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.MP.toString());
    }

    public String getMPMaxValue(Entity entity) {
        return getStat(entity.getEntityConfig().getEntityID() + EntityConfig.EntityProperties.MP_MAX.toString());
    }

    public void removeHPValue(int hpValue){
        _hpVal = MathUtils.clamp(_hpVal - hpValue, 0, _hpCurrentMax);
        _hpValLabel.setText(String.valueOf(_hpVal));

        updateBar(_hpBar, _hpVal, _hpCurrentMax);

        notify(_hpVal, StatusObserver.StatusEvent.UPDATED_HP);
    }

    public void addHPValue(int hpValue){
        _hpVal = MathUtils.clamp(_hpVal + hpValue, 0, _hpCurrentMax);
        _hpValLabel.setText(String.valueOf(_hpVal));

        updateBar(_hpBar, _hpVal, _hpCurrentMax);

        notify(_hpVal, StatusObserver.StatusEvent.UPDATED_HP);
    }

    public void setHPValue(int hpValue){
        this._hpVal = hpValue;
        _hpValLabel.setText(String.valueOf(_hpVal));

        updateBar(_hpBar, _hpVal, _hpCurrentMax);

        notify(_hpVal, StatusObserver.StatusEvent.UPDATED_HP);
    }

    public void setHPValueMax(int maxHPValue){
        this._hpCurrentMax = maxHPValue;
    }

    public int getHPValueMax(){
        return _hpCurrentMax;
    }

    //MP
    public int getMPValue(){
        return _mpVal;
    }

    public void removeMPValue(int mpValue){
        _mpVal = MathUtils.clamp(_mpVal - mpValue, 0, _mpCurrentMax);
        _mpValLabel.setText(String.valueOf(_mpVal));

        updateBar(_mpBar, _mpVal, _mpCurrentMax);

        notify(_mpVal, StatusObserver.StatusEvent.UPDATED_MP);
    }

    public void addMPValue(int mpValue){
        _mpVal = MathUtils.clamp(_mpVal + mpValue, 0, _mpCurrentMax);
        _mpValLabel.setText(String.valueOf(_mpVal));

        updateBar(_mpBar, _mpVal, _mpCurrentMax);

        notify(_mpVal, StatusObserver.StatusEvent.UPDATED_MP);
    }

    public void setMPValue(int mpValue){
        this._mpVal = mpValue;
        _mpValLabel.setText(String.valueOf(_mpVal));

        updateBar(_mpBar, _mpVal, _mpCurrentMax);

        notify(_mpVal, StatusObserver.StatusEvent.UPDATED_MP);
    }

    public void setMPValueMax(int maxMPValue){
        this._mpCurrentMax = maxMPValue;
    }

    public int getMPValueMax(){
        return _mpCurrentMax;
    }

    public void updateBar(Image bar, int currentVal, int maxVal){
        int val = MathUtils.clamp(currentVal, 0, maxVal);
        float tempPercent = (float) val / (float) maxVal;
        float percentage = MathUtils.clamp(tempPercent, 0, 100);
        bar.setSize(_barWidth*percentage, _barHeight);
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
    public void onNotify(ProfileManager profileManager, ProfileEvent event) {
        switch(event){
            case PROFILE_LOADED:
                boolean firstTime = profileManager.getIsNewProfile();

                if (firstTime) {
                    // load default stats
                    setAllStatProperties(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.CARMEN), false);
                    setAllStatProperties(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.CHARACTER_1), false);
                    setAllStatProperties(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.CHARACTER_2), false);
                    setAllStatProperties(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.DOUGLAS), false);
                    setAllStatProperties(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.JUSTIN), false);
                    setAllStatProperties(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.JAXON_1), false);
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

    public void setAllStatProperties(Entity entity, boolean update) {
        String entityID = entity.getEntityConfig().getEntityID();
        String key;
        String property;

        // set stat properties only if they are not already set, or if an update is being made
        key = entityID + EntityConfig.EntityProperties.HP.toString();
        property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP.toString().toString());

        if (ProfileManager.getInstance().getProperty(key, String.class).equals(null) || update) {
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

    public void getAllStatProperties(Entity entity) {
        String entityID = entity.getEntityConfig().getEntityID();
        String key;

        key = entityID + EntityConfig.EntityProperties.HP.toString();
        if (!ProfileManager.getInstance().getProperty(key, String.class).equals(null)) {
            String property = ProfileManager.getInstance().getProperty(key, String.class);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.HP.toString(), property);

            // we can assume all other status values exists since they are always set in a batch in setStatProperties
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
    }
}
