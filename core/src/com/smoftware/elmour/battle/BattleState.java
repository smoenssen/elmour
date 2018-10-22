package com.smoftware.elmour.battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.EntityConfig;
import com.smoftware.elmour.EntityFactory;
import com.smoftware.elmour.InventoryElement;
import com.smoftware.elmour.SpellsPowerElement;
import com.smoftware.elmour.UI.InventoryObserver;
import com.smoftware.elmour.profile.ProfileManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class BattleState extends BattleSubject implements InventoryObserver {
    private static final String TAG = BattleState.class.getSimpleName();

    //private Entity _currentOpponent = null;
    private Entity currentTurnCharacter = null;
    private Entity currentSelectedCharacter = null;
    private Array<Entity> currentPartyList;
    private Array<Entity> currentEnemyList;
    private ArrayList<Entity> characterTurnList;
    private int _currentZoneLevel = 0;
    private int _currentPlayerAP;
    private int _currentPlayerDP;
    private int _currentPlayerWandAPPoints = 0;
    private InventoryElement selectedInventoryElement = null;

    private Timer.Task _playerAttackCalculations;
    private Timer.Task _opponentAttackCalculations;
    private Timer.Task _checkPlayerMagicUse;
    private Timer.Task applyInventory;
    private Timer.Task applySpellPower;
    private Timer.Task chooseNextCharacterTurn;

    private MonsterZone currentMonsterZone;

    private boolean inBattle = false;
    private boolean isBackBattle = false; //todo

    private float battleCountDown = 0;

    public class EntitySpeedComparator implements Comparator<Entity> {
        @Override
        public int compare(Entity arg0, Entity arg1) {
            String SPD0 = arg0.getEntityConfig().getEntityProperties().get(String.valueOf(EntityConfig.EntityProperties.SPD));
            String SPD1 = arg1.getEntityConfig().getEntityProperties().get(String.valueOf(EntityConfig.EntityProperties.SPD));
            if (Integer.parseInt(SPD0) > Integer.parseInt(SPD1)) {
                return -1;
            }
            else if (Integer.parseInt(SPD0) == Integer.parseInt(SPD1)) {
                switch (arg0.getBattleEntityType()) {
                    case PARTY:
                        if (arg1.getBattleEntityType() == Entity.BattleEntityType.ENEMY)
                            return -1;
                        else if (arg0.getBattlePosition() < arg1.getBattlePosition())
                            return -1;
                        else
                            return 1;
                    case ENEMY:
                        if (arg1.getBattleEntityType() == Entity.BattleEntityType.PARTY)
                            return 1;
                        else if (arg0.getBattlePosition() < arg1.getBattlePosition())
                            return -1;
                        else
                            return 1;
                }
                return 0;
            }
            else {
                return 1;
            }
        }
    }

    public BattleState(){
        _playerAttackCalculations = getPlayerAttackCalculationTimer();
        _opponentAttackCalculations = getOpponentAttackCalculationTimer();
        _checkPlayerMagicUse = getPlayerMagicUseCheckTimer();
        applyInventory = getApplyInventoryTimer();
        applySpellPower = getApplySpellPowerTimer();
        chooseNextCharacterTurn = getChooseNextCharacterTurnTimer();

        currentPartyList = new Array<>();
        currentEnemyList = new Array<>();
        characterTurnList = new ArrayList<>();

        // for EntitySpeedComparator that was throwing an exception
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    }

    public void resetDefaults(){
        Gdx.app.log(TAG, "Resetting defaults...");
        _currentZoneLevel = 0;
        _currentPlayerAP = 0;
        _currentPlayerDP = 0;
        _currentPlayerWandAPPoints = 0;
        _playerAttackCalculations.cancel();
        _opponentAttackCalculations.cancel();
        _checkPlayerMagicUse.cancel();
        inBattle = false;
        isBackBattle = false;
    }

    public void setCurrentZone(int zoneLevel){
        _currentZoneLevel = zoneLevel;

        if (zoneLevel > 0) {
            currentMonsterZone = MonsterFactory.getInstance().getMonsterZone(Integer.toString(zoneLevel));
        }
    }

    public int getCurrentZoneLevel(){
        return _currentZoneLevel;
    }

    public boolean isBackBattle() { return isBackBattle; }

    public void battleZoneTriggered(int battleZoneValue){
        Gdx.app.debug(TAG, String.format("battleZoneTriggered: zone = %d", battleZoneValue));
        setCurrentZone(battleZoneValue);
        inBattle = false;
    }

    public boolean isBattleReady(float playerVelocity, float delta){

        if (currentMonsterZone == null || _currentZoneLevel == 0 || inBattle) { return false; }

        // for debugging
        return true;
/*
        if (battleCountDown <= 0) {
            // start new countdown
            battleCountDown = currentMonsterZone.getMaxTime();
        }
        else {
            // generate random number between min and max zone times
            int randomVal = MathUtils.random(currentMonsterZone.getMinTime(), currentMonsterZone.getMaxTime());

            // if lower than number previously generated, then override that number
            if (randomVal < battleCountDown) {
                battleCountDown = randomVal;
            }
        }

        // take generated value and subtract velocity/frame rate
        float frameRate = 1/delta;
        battleCountDown -= playerVelocity/frameRate;

        Gdx.app.log(TAG, String.format("BATTLE COUNTDOWN: %3.2f", battleCountDown));

        // when countdown is negative, then battle is ready
        if (battleCountDown < 0) {
            inBattle = true;
            return true;
        }
        else {
            return false;
        }*/
    }

    public void setCurrentEnemyList(){
        currentEnemyList.clear();
        MonsterGroup monsterGroup = MonsterFactory.getInstance().getRandomMonsterGroup(_currentZoneLevel);
        Array<MonsterFactory.MonsterEntityType> monsterEntityTypes = monsterGroup.getMonsters();

        Gdx.app.log(TAG, "Setting current opponent list to: " + monsterGroup.getGroupID().toString());

        int battlePosition = 1;
        for (MonsterFactory.MonsterEntityType entityType : monsterEntityTypes) {
            Entity entity = MonsterFactory.getInstance().getMonster(entityType);
            if (entity != null) {
                entity.setAlive(true);
                entity.setBattleEntityType(Entity.BattleEntityType.ENEMY);
                entity.setBattlePosition(battlePosition++);
                currentEnemyList.add(entity);
                notify(entity, BattleObserver.BattleEvent.OPPONENT_ADDED);
            }
        }
    }

    public void setCurrentPartyList() {
        //todo: this is temporary code: need to figure out how/when characters are added to party list
        currentPartyList.clear();

        int battlePosition = 1;
        Entity entity1 = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.CARMEN);
        entity1.setAlive(true);
        entity1.setBattleEntityType(Entity.BattleEntityType.PARTY);
        entity1.setBattlePosition(battlePosition++);
        notify(entity1, BattleObserver.BattleEvent.PARTY_MEMBER_ADDED);
        currentPartyList.add(entity1);

        Entity entity2 = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.CHARACTER_1);
        entity2.setAlive(true);
        entity2.setBattleEntityType(Entity.BattleEntityType.PARTY);
        entity2.setBattlePosition(battlePosition++);
        notify(entity2, BattleObserver.BattleEvent.PARTY_MEMBER_ADDED);
        currentPartyList.add(entity2);

        Entity entity3 = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.CHARACTER_2);
        entity3.setAlive(true);
        entity3.setBattleEntityType(Entity.BattleEntityType.PARTY);
        entity3.setBattlePosition(battlePosition++);
        notify(entity3, BattleObserver.BattleEvent.PARTY_MEMBER_ADDED);
        currentPartyList.add(entity3);

        Entity entity4 = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.JUSTIN);
        entity4.setAlive(true);
        entity4.setBattleEntityType(Entity.BattleEntityType.PARTY);
        entity4.setBattlePosition(battlePosition++);
        notify(entity4, BattleObserver.BattleEvent.PARTY_MEMBER_ADDED);
        currentPartyList.add(entity4);
/*
        Entity entity5 = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.JAXON_1);
        entity5.setAlive(true);
        entity5.setBattleEntityType(Entity.BattleEntityType.PARTY);
        entity5.setBattlePosition(battlePosition++);
        notify(entity5, BattleObserver.BattleEvent.PARTY_MEMBER_ADDED);
        currentPartyList.add(entity5);*/
    }

    public void getNextTurnCharacter(float delay){
        // NOTE: When the battle starts for the first time, this is called from PlayerHUD, not BattleHUD
        if (characterTurnList.size() == 0) {
            populateCharacterTurnList();
        }

        if( !chooseNextCharacterTurn.isScheduled() ){
            Timer.schedule(chooseNextCharacterTurn, delay);
        }
    }

    public Entity peekNextTurnCharacter() {
        if (characterTurnList.size() == 0) {
            populateCharacterTurnList();
        }

        // return character at top of list but don't remove it
        return characterTurnList.get(0);
    }

    public Entity getCurrentTurnCharacter() { return currentTurnCharacter; }

    private void setCurrentTurnCharacter(Entity entity) {
        // called from chooseNextCharacterTurn timer
        currentTurnCharacter = entity;
        notify(entity, BattleObserver.BattleEvent.CHARACTER_TURN_CHANGED);

        if (currentTurnCharacter.getBattleEntityType() == Entity.BattleEntityType.ENEMY) {
            // select character for battle, right now just totally random (easy mode)
            currentSelectedCharacter = currentPartyList.get(MathUtils.random(0, currentPartyList.size - 1));

            setCurrentSelectedCharacter(currentSelectedCharacter);
            opponentAttacks();
        }
    }

    public void setCurrentSelectedCharacter(Entity entity) {
        this.currentSelectedCharacter = entity;
        notify(entity, BattleObserver.BattleEvent.CHARACTER_SELECTED);
    }

    public void applyInventoryItemToCharacter(InventoryElement selectedElement) {
        if (!applyInventory.isScheduled()) {
            selectedInventoryElement = selectedElement;
            Gdx.app.log(TAG, selectedInventoryElement.name + " used on " + currentSelectedCharacter.getEntityConfig().getEntityID());
            Timer.schedule(applyInventory, 1);
        }
    }

    public void applySpellPowerToCharacter(SpellsPowerElement selectedElement) {
        if (!applySpellPower.isScheduled()) {
            Gdx.app.log(TAG, "TODO: " + selectedElement.name + " used on " + currentSelectedCharacter.getEntityConfig().getEntityID());
            Timer.schedule(applySpellPower, 1);
        }
    }

    public void playerMeleeAttack(){
        if( currentSelectedCharacter == null ){
            return;
        }

        if (currentSelectedCharacter.getBattleEntityType() == Entity.BattleEntityType.ENEMY) {
            // Melee attack against enemy 1, 3 or 5 can be blocked by enemies 2 or 4 if they are alive
            // i.e.,
            // 1
            //    2
            // 3
            //    4
            // 5
            // check for block
            Entity enemy;
            switch (currentSelectedCharacter.getBattlePosition()) {
                case 1:
                    if (currentEnemyList.size > 1) {
                        enemy = currentEnemyList.get(1);
                        if (enemy.isAlive()) {
                            BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.OPPONENT_BLOCKED);
                            return;
                        }
                    }
                    break;
                case 3:
                    enemy = currentEnemyList.get(1);
                    if (enemy.isAlive()) {
                        BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.OPPONENT_BLOCKED);
                        return;
                    } else if (currentEnemyList.size > 3) {
                        enemy = currentEnemyList.get(3);
                        if (enemy.isAlive()) {
                            BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.OPPONENT_BLOCKED);
                            return;
                        }
                    }
                    break;
                case 5:
                    enemy = currentEnemyList.get(3);
                    if (enemy.isAlive()) {
                        BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.OPPONENT_BLOCKED);
                        return;
                    }
                    break;
            }
        }
        // else Melee attack against party members can be made as long as they are alive; no blocking


        // if got this far, then kick off animation for player attacking
        BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEventWithMessage.PLAYER_ATTACKS, "");

        if( !_playerAttackCalculations.isScheduled() ){
            Timer.schedule(_playerAttackCalculations, 1.75f);
        }

        /*
        //Check for magic if used in attack; If we don't have enough MP, then return
        int mpVal = ProfileManager.getInstance().getProperty("currentPlayerMP", Integer.class);
        notify(currentSelectedCharacter, BattleObserver.BattleEvent.PLAYER_TURN_START);

        if( _currentPlayerWandAPPoints == 0 ){
            if( !_playerAttackCalculations.isScheduled() ){
                Timer.schedule(_playerAttackCalculations, 1);
            }
        }
        else if(_currentPlayerWandAPPoints > mpVal ){
            BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.PLAYER_TURN_DONE);
            return;
        }
        else {
            if( !_checkPlayerMagicUse.isScheduled() && !_playerAttackCalculations.isScheduled() ){
                Timer.schedule(_checkPlayerMagicUse, .5f);
                Timer.schedule(_playerAttackCalculations, 1);
            }
        }
        */
    }

    public void opponentAttacks(){
        // kick off animation for opponent attacking
        BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEventWithMessage.OPPONENT_ATTACKS, "");

        if( !_opponentAttackCalculations.isScheduled() ){
            Timer.schedule(_opponentAttackCalculations, 1.75f);
        }
    }

    private String getEffectPhrase(Integer value, String type, boolean useAnd) {
        String phrase = "";
        if (value >= 0) {
            if (useAnd) {
                phrase += " and healing " + value.toString() + " " + type;
            }
            else {
                phrase += ", healing " + value.toString() + " " + type;
            }
        }
        else {
            if (useAnd) {
                phrase += " and dealing " + value.toString() + " " + type;
            }
            else {
                phrase += ", dealing " + value.toString() + " " + type;
            }
        }

        return phrase;
    }

    private Timer.Task getApplyInventoryTimer(){
        return new Timer.Task() {
            @Override
            public void run() {
                String inventoryItem = selectedInventoryElement.name;
                String message = String.format("%s used %s", currentTurnCharacter.getEntityConfig().getDisplayName(), inventoryItem);

                if (!currentTurnCharacter.getEntityConfig().getEntityID().equals(currentSelectedCharacter)) {
                    // inventory used on another character
                    message += String.format(" on %s", currentSelectedCharacter.getEntityConfig().getDisplayName());
                }

                // Loop through effect list and apply effect value to character's profile.
                // Also modify message text for HP and MP
                boolean gotHPorMP = false;
                boolean addedPeriod = false;
                boolean addedEffectText = false;
                for (InventoryElement.EffectItem effectItem : selectedInventoryElement.effectList) {
                    String sVal;

                    Integer currVal = 0;
                    Integer newVal = 0;
                    if (effectItem.effect.equals(InventoryElement.Effect.HEAL_HP)) {
                        sVal = currentSelectedCharacter.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP.toString());
                        String hpMax = currentSelectedCharacter.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP_MAX.toString());
                        currVal = Integer.parseInt(sVal);
                        newVal = MathUtils.clamp(currVal + effectItem.value, 0, Integer.parseInt(hpMax));
                        message += getEffectPhrase(newVal - currVal, "HP", gotHPorMP);
                        gotHPorMP = true;
                    }
                    else if (effectItem.effect.equals(InventoryElement.Effect.HEAL_HP_PERCENT)) {
                        sVal = currentSelectedCharacter.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP.toString());
                        String hpMax = currentSelectedCharacter.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP_MAX.toString());
                        currVal = Integer.parseInt(sVal);
                        float fVal = (float)currVal * (float)effectItem.value * 0.01f;
                        newVal = MathUtils.clamp(currVal + (int)fVal, 0, Integer.parseInt(hpMax));
                        message += getEffectPhrase(newVal - currVal, "HP", gotHPorMP);
                        gotHPorMP = true;
                    }
                    else if (effectItem.effect.equals(InventoryElement.Effect.HEAL_MP)) {
                        sVal = currentSelectedCharacter.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MP.toString());
                        String mpMax = currentSelectedCharacter.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MP_MAX.toString());
                        currVal = Integer.parseInt(sVal);
                        newVal = MathUtils.clamp(currVal + effectItem.value, 0, Integer.parseInt(mpMax));
                        message += getEffectPhrase(newVal - currVal, "MP", gotHPorMP);
                        gotHPorMP = true;
                    }
                    else if (effectItem.effect.equals(InventoryElement.Effect.HEAL_MP_PERCENT)) {
                        sVal = currentSelectedCharacter.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MP.toString());
                        String mpMax = currentSelectedCharacter.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MP_MAX.toString());
                        currVal = Integer.parseInt(sVal);
                        float fVal = (float)currVal * (float)effectItem.value * 0.01f;
                        newVal = MathUtils.clamp(currVal + (int)fVal, 0, Integer.parseInt(mpMax));
                        message += getEffectPhrase(newVal - currVal, "MP", gotHPorMP);
                        gotHPorMP = true;
                    }
                    else {
                        if (gotHPorMP && !addedPeriod) {
                            String tmp = message.substring(message.length() - 1, message.length());
                            if (!tmp.equals(".")) {
                                message += ".";
                                addedPeriod = true;
                            }
                        }

                        // all other entity properties are the effect item's name left of the underscore
                        String entityProperty = effectItem.effect.toString();
                        entityProperty = entityProperty.substring(0, entityProperty.indexOf("_"));
                        sVal = currentSelectedCharacter.getEntityConfig().getPropertyValue(entityProperty);
                        if (!sVal.equals("")) {
                            currVal = Integer.parseInt(sVal);
                            newVal = currVal + effectItem.value;
                        }
                        else {
                            Gdx.app.log(TAG,">>>>>>>>>>>>>>>>> TODO: getApplyInventoryTimer needs to handle " + entityProperty + " <<<<<<<<<<<<<<<<<<<<<");
                        }
                    }

                    if (!selectedInventoryElement.effectText.equals("") && !addedEffectText) {
                        // set character name(s) if placeholders are in effect text
                        String tmp = selectedInventoryElement.effectText;
                        String finalEffectText = tmp;
                        int leftPercentIndex = tmp.indexOf('%', 0);
                        while (leftPercentIndex >= 0) {
                            int rightPercentIndex = tmp.indexOf('%', leftPercentIndex + 1);
                            String placeholder = tmp.substring(leftPercentIndex + 1, rightPercentIndex);
                            String characterName = currentSelectedCharacter.getEntityConfig().getDisplayName();
                            finalEffectText = finalEffectText.replace("%" + placeholder + "%", characterName);
                            leftPercentIndex = tmp.indexOf('%', rightPercentIndex + 1);
                        }

                        message += ". " + finalEffectText;
                        addedEffectText = true;
                    }

                    currentSelectedCharacter.getEntityConfig().setPropertyValue(effectItem.effect.toString(), newVal.toString());
                }

                BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEventWithMessage.PLAYER_TURN_DONE, message);
            }
        };
    }

    private Timer.Task getApplySpellPowerTimer(){
        return new Timer.Task() {
            @Override
            public void run() {
                String spell = "TODO";
                String message = String.format("%s used %s on %s.", currentTurnCharacter.getEntityConfig().getDisplayName(), spell,
                                        currentSelectedCharacter.getEntityConfig().getDisplayName());

                BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEventWithMessage.PLAYER_TURN_DONE, message);
            }
        };
    }

    private Timer.Task getPlayerMagicUseCheckTimer(){
        return new Timer.Task() {
            @Override
            public void run() {
                int mpVal = ProfileManager.getInstance().getProperty("currentPlayerMP", Integer.class);
                mpVal -= _currentPlayerWandAPPoints;
                ProfileManager.getInstance().setProperty("currentPlayerMP", mpVal);
                BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.PLAYER_USED_MAGIC);
            }
        };
    }

    private Timer.Task getPlayerAttackCalculationTimer() {
        return new Timer.Task() {
            @Override
            public void run() {
                String playerATK = currentTurnCharacter.getEntityConfig().getEntityProperties().get(String.valueOf(EntityConfig.EntityProperties.ATK));
                String enemyDEF = currentSelectedCharacter.getEntityConfig().getEntityProperties().get(String.valueOf(EntityConfig.EntityProperties.DEF));

                // If Character attacks enemy with a normal attack of 10 ATK, then there is 10 POW going at enemy.
                // Every 2 DEF blocks one ATK, meaning that because enemy has 15 DEF, it blocks 7 POW (rounding down).
                // This means that they take 3 HP
                // DEF blocks ATK 2:1
                Gdx.app.log(TAG, "playerATK = " + playerATK + ", enemyDEF = " + enemyDEF);
                int hitPoints = Integer.parseInt(playerATK) - Integer.parseInt(enemyDEF) / 2;
                if (hitPoints < 0)
                    hitPoints = 0;

                Gdx.app.log(TAG, "enemy takes " + hitPoints + " hit points");

                String message = String.format("%s attacked %s, dealing %s HP.", currentTurnCharacter.getEntityConfig().getDisplayName(),
                        currentSelectedCharacter.getEntityConfig().getDisplayName(), hitPoints);

                String enemyHP = currentSelectedCharacter.getEntityConfig().getEntityProperties().get(String.valueOf(EntityConfig.EntityProperties.HP));
                int newEnemyHP = Integer.parseInt(enemyHP) - hitPoints;
                currentSelectedCharacter.getEntityConfig().setPropertyValue(String.valueOf(EntityConfig.EntityProperties.HP), String.format("%d" , newEnemyHP));

                if (hitPoints > 0) {
                    BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.OPPONENT_HIT_DAMAGE);
                }

                Gdx.app.log(TAG, "new enemy HP = " + newEnemyHP);

                if (newEnemyHP <= 0) {
                    currentSelectedCharacter.setAlive(false);
                    BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.OPPONENT_DEFEATED);
                }

                BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEventWithMessage.PLAYER_TURN_DONE, message);
            }
        };
    }

    private Timer.Task getOpponentAttackCalculationTimer() {
        return new Timer.Task() {
            @Override
            public void run() {
                String enemyATK = currentTurnCharacter.getEntityConfig().getEntityProperties().get(String.valueOf(EntityConfig.EntityProperties.ATK));
                String playerDEF = currentSelectedCharacter.getEntityConfig().getEntityProperties().get(String.valueOf(EntityConfig.EntityProperties.DEF));

                Gdx.app.log(TAG, "enemyATK = " + enemyATK + ", playerDEF = " + playerDEF);
                int hitPoints = Integer.parseInt(enemyATK) - Integer.parseInt(playerDEF) / 2;
                if (hitPoints < 0)
                    hitPoints = 0;

                Gdx.app.log(TAG, "player takes " + hitPoints + " hit points");

                String message = String.format("%s attacked %s, dealing %s HP.", currentTurnCharacter.getEntityConfig().getDisplayName(),
                        currentSelectedCharacter.getEntityConfig().getDisplayName(), hitPoints);

                String playerHP = currentSelectedCharacter.getEntityConfig().getEntityProperties().get(String.valueOf(EntityConfig.EntityProperties.HP));
                int newPlayerHP = Integer.parseInt(playerHP) - hitPoints;
                currentSelectedCharacter.getEntityConfig().setPropertyValue(String.valueOf(EntityConfig.EntityProperties.HP), String.format("%d" , newPlayerHP));

                if (hitPoints > 0) {
                    BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.PLAYER_HIT_DAMAGE);
                }

                Gdx.app.log(TAG, "new player HP = " + newPlayerHP);

                if (newPlayerHP <= 0) {
                    currentSelectedCharacter.setAlive(false);
                    BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.PLAYER_DEFEATED);
                }

                BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEventWithMessage.OPPONENT_TURN_DONE, message);
                /*
                int currentOpponentHP = Integer.parseInt(currentSelectedCharacter.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP.toString()));

                if (currentOpponentHP <= 0) {
                    BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.OPPONENT_TURN_DONE);
                    return;
                }

                int currentOpponentAP = Integer.parseInt(currentSelectedCharacter.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.ATK.toString()));
                int damage = MathUtils.clamp(currentOpponentAP - _currentPlayerDP, 0, currentOpponentAP);
                int hpVal = ProfileManager.getInstance().getProperty("currentPlayerHP", Integer.class);
                hpVal = MathUtils.clamp( hpVal - damage, 0, hpVal);
                ProfileManager.getInstance().setProperty("currentPlayerHP", hpVal);

                if( damage > 0 ) {
                    String message = String.format("%s attacked %s, causing %s HP damage.", currentTurnCharacter.getEntityConfig().getDisplayName(),
                            currentSelectedCharacter.getEntityConfig().getDisplayName(), damage);

                    BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEvent.PLAYER_HIT_DAMAGE, message);
                }

                Gdx.app.log(TAG, "Player HIT for " + damage + " HP BY " + currentSelectedCharacter.getEntityConfig().getEntityID() + " leaving player with HP: " + hpVal);

                BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.OPPONENT_TURN_DONE);
                */
            }
        };
    }

    private Timer.Task getChooseNextCharacterTurnTimer() {
        return new Timer.Task() {
            @Override
            public void run() {
                if (characterTurnList.size() > 0) {
                    // get character at top of list and remove it
                    setCurrentTurnCharacter(characterTurnList.get(0));
                    characterTurnList.remove(0);
                }
            }
        };
    }

    private void populateCharacterTurnList() {
        characterTurnList.clear();

        // get list of all characters
        for (Entity entity : currentPartyList) {
            characterTurnList.add(entity);
        }

        for (Entity entity : currentEnemyList) {
            characterTurnList.add(entity);
        }

        // sort list in descending order by SPD
        Collections.sort(characterTurnList, new EntitySpeedComparator());

        Gdx.app.log(TAG, "Battle turn order:");
        for (Entity entity : characterTurnList) {
            Gdx.app.log(TAG, entity.getEntityConfig().getDisplayName() + " " + entity.getEntityConfig().getEntityProperties().get(String.valueOf(EntityConfig.EntityProperties.SPD)));
        }
    }

    public void playerRuns(){
        // Chance of escape is:
        // 0.5/(ENEMY AVG SPD / PLAYER AVG SPD) * (1 + (F! * 0.1)
        // where F is the number of characters that have fainted
        int totalSPD = 0;
        float avgPartySpd = 0;
        float avgEnemySpd = 0;
        int numCharactersFainted = 0;
        int factorial = 0;

        for (Entity entity : currentPartyList) {
            String SPD = entity.getEntityConfig().getEntityProperties().get(String.valueOf(EntityConfig.EntityProperties.SPD));
            totalSPD += Integer.parseInt(SPD);
            String HP = entity.getEntityConfig().getEntityProperties().get(String.valueOf(EntityConfig.EntityProperties.HP));
            if (Integer.parseInt(HP) <= 0)
                numCharactersFainted++;
        }
        avgPartySpd = totalSPD / (float)currentPartyList.size;

        totalSPD = 0;
        for (Entity entity : currentEnemyList) {
            String SPD = entity.getEntityConfig().getEntityProperties().get(String.valueOf(EntityConfig.EntityProperties.SPD));
            totalSPD += Integer.parseInt(SPD);
        }
        avgEnemySpd = totalSPD / (float)currentEnemyList.size;

        for (int i = numCharactersFainted; i > 0; i--) {
            if (factorial == 0) factorial = 1;
            factorial = factorial * i;
        }

        float chanceOfEscape = 100 * (0.5f / (avgEnemySpd / avgPartySpd) * (1 + (factorial * 0.1f)));
        int randomVal = MathUtils.random(1, 100);
        Gdx.app.log(TAG, "Chance of escape = " + chanceOfEscape + ", randVal = " + randomVal);

        if (chanceOfEscape > randomVal) {
            Gdx.app.log(TAG, "Player escaped!");
            resetBattleState();
            notify(currentSelectedCharacter, BattleObserver.BattleEvent.PLAYER_ESCAPED);
        }
        else {
            Gdx.app.log(TAG, "Player failed to escape!");
            notify(currentSelectedCharacter, BattleObserver.BattleEvent.PLAYER_FAILED_TO_ESCAPE);
        }
    }

    private void resetBattleState() {
        characterTurnList.clear();
        Gdx.app.log(TAG, "clearing characterTurnList");
    }

    public void animationComplete() {
        notify(currentSelectedCharacter, BattleObserver.BattleEvent.ANNIMATION_COMPLETE);
    }

    @Override
    public void onNotify(String value, InventoryEvent event) {
        switch(event) {
            case UPDATED_AP:
                int apVal = Integer.valueOf(value);
                _currentPlayerAP = apVal;
                Gdx.app.log(TAG, "APVAL: " + _currentPlayerAP);
                break;
            case UPDATED_DP:
                int dpVal = Integer.valueOf(value);
                _currentPlayerDP = dpVal;
                Gdx.app.log(TAG, "DPVAL: " + _currentPlayerDP);
                break;
            case ADD_WAND_AP:
                int wandAP = Integer.valueOf(value);
                _currentPlayerWandAPPoints += wandAP;
                Gdx.app.log(TAG, "WandAP: " + _currentPlayerWandAPPoints);
                break;
            case REMOVE_WAND_AP:
                int removeWandAP = Integer.valueOf(value);
                _currentPlayerWandAPPoints -= removeWandAP;
                Gdx.app.log(TAG, "WandAP: " + _currentPlayerWandAPPoints);
                break;
            default:
                break;
        }
    }
}
