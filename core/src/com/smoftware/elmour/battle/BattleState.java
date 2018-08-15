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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BattleState extends BattleSubject implements InventoryObserver {
    private static final String TAG = BattleState.class.getSimpleName();

    private Entity _currentOpponent = null;
    //private Entity currentPartyMember = null;
    private Entity currentTurnCharacter = null;
    private Entity currentSelectedCharacter = null;
    private Array<Entity> currentPartyList;
    private Array<Entity> currentEnemyList;
    private ArrayList<Entity> characterTurnList;
    private int _currentZoneLevel = 0;
    private int _currentPlayerAP;
    private int _currentPlayerDP;
    private int _currentPlayerWandAPPoints = 0;

    private Timer.Task _playerAttackCalculations;
    private Timer.Task _opponentAttackCalculations;
    private Timer.Task _checkPlayerMagicUse;
    private Timer.Task applyInventory;
    private Timer.Task applySpellPower;
    private Timer.Task chooseNextCharacterTurn;

    private MonsterZone currentMonsterZone;

    private boolean inBattle = false;

    private float battleCountDown = 0;

    public class EntitySpeedComparator implements Comparator<Entity> {
        @Override
        public int compare(Entity arg0, Entity arg1) {
            String SPD0 = arg0.getEntityConfig().getEntityProperties().get(String.valueOf(EntityConfig.EntityProperties.SPD));
            String SPD1 = arg1.getEntityConfig().getEntityProperties().get(String.valueOf(EntityConfig.EntityProperties.SPD));
            if (Integer.parseInt(SPD0) > Integer.parseInt(SPD1)) {
                return -1;
            } else if (Integer.parseInt(SPD0) == Integer.parseInt(SPD1)) {
                return 0;
            } else {
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

    public void setCurrentEnemytList(){
        currentEnemyList.clear();
        MonsterGroup monsterGroup = MonsterFactory.getInstance().getRandomMonsterGroup(_currentZoneLevel);
        Array<MonsterFactory.MonsterEntityType> monsterEntityTypes = monsterGroup.getMonsters();

        Gdx.app.log(TAG, "Setting current opponent list to: " + monsterGroup.getGroupID().toString());

        for (MonsterFactory.MonsterEntityType entityType : monsterEntityTypes) {
            Entity entity = MonsterFactory.getInstance().getMonster(entityType);
            if (entity != null) {
                currentEnemyList.add(entity);
                notify(entity, BattleObserver.BattleEvent.OPPONENT_ADDED);
            }
        }
    }

    public void setCurrentPartyList() {
        //todo: figure out how/when characters need to be added
        Entity entity1 = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.CARMEN);
        notify(entity1, BattleObserver.BattleEvent.PARTY_MEMBBER_ADDED);
        currentPartyList.add(entity1);

        Entity entity2 = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.CHARACTER_1);
        notify(entity2, BattleObserver.BattleEvent.PARTY_MEMBBER_ADDED);
        currentPartyList.add(entity2);

        Entity entity3 = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.CHARACTER_2);
        notify(entity3, BattleObserver.BattleEvent.PARTY_MEMBBER_ADDED);
        currentPartyList.add(entity3);

        Entity entity4 = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.JUSTIN);
        notify(entity4, BattleObserver.BattleEvent.PARTY_MEMBBER_ADDED);
        currentPartyList.add(entity4);

        Entity entity5 = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.JAXON);
        notify(entity5, BattleObserver.BattleEvent.PARTY_MEMBBER_ADDED);
        currentPartyList.add(entity5);
    }

    public void getNextTurnCharacter(){
        if( !chooseNextCharacterTurn.isScheduled() ){
            Timer.schedule(chooseNextCharacterTurn, 1);
        }
    }

    private void setCurrentTurnCharacter(Entity entity) {
        this.currentTurnCharacter = entity;
        notify(entity, BattleObserver.BattleEvent.CHARACTER_TURN_CHANGED);
    }

    public void setCurrentSelectedCharacter(Entity entity) {
        this.currentSelectedCharacter = entity;
        notify(entity, BattleObserver.BattleEvent.CHARACTER_SELECTED);
    }

    public void applyInventoryItemToCharacter(InventoryElement selectedElement) {
        if (!applyInventory.isScheduled()) {
            Gdx.app.log(TAG, "TODO: " + selectedElement.name + " used on " + currentSelectedCharacter.getEntityConfig().getEntityID());
            Timer.schedule(applyInventory, 1);
        }
    }

    public void applySpellPowerToCharacter(SpellsPowerElement selectedElement) {
        if (!applySpellPower.isScheduled()) {
            Gdx.app.log(TAG, "TODO: " + selectedElement.name + " used on " + currentSelectedCharacter.getEntityConfig().getEntityID());
            Timer.schedule(applySpellPower, 1);
        }
    }

    public void playerAttacks(){
        if( _currentOpponent == null ){
            return;
        }

        //Check for magic if used in attack; If we don't have enough MP, then return
        int mpVal = ProfileManager.getInstance().getProperty("currentPlayerMP", Integer.class);
        notify(_currentOpponent, BattleObserver.BattleEvent.PLAYER_TURN_START);

        if( _currentPlayerWandAPPoints == 0 ){
            if( !_playerAttackCalculations.isScheduled() ){
                Timer.schedule(_playerAttackCalculations, 1);
            }
        }
        else if(_currentPlayerWandAPPoints > mpVal ){
            BattleState.this.notify(_currentOpponent, BattleObserver.BattleEvent.PLAYER_TURN_DONE);
            return;
        }
        else {
            if( !_checkPlayerMagicUse.isScheduled() && !_playerAttackCalculations.isScheduled() ){
                Timer.schedule(_checkPlayerMagicUse, .5f);
                Timer.schedule(_playerAttackCalculations, 1);
            }
        }
    }

    public void opponentAttacks(){
        if( _currentOpponent == null ){
            return;
        }

        if( !_opponentAttackCalculations.isScheduled() ){
            Timer.schedule(_opponentAttackCalculations, 1);
        }
    }

    private Timer.Task getApplyInventoryTimer(){
        return new Timer.Task() {
            @Override
            public void run() {
                String message = ", healing 15 HP.";
                currentSelectedCharacter.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.HP.toString(), "25");
                currentSelectedCharacter.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.MP.toString(), "1");
                BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEvent.PLAYER_TURN_DONE, message);
            }
        };
    }

    private Timer.Task getApplySpellPowerTimer(){
        return new Timer.Task() {
            @Override
            public void run() {
                BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEvent.PLAYER_TURN_DONE, ".");
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
                BattleState.this.notify(_currentOpponent, BattleObserver.BattleEvent.PLAYER_USED_MAGIC);
            }
        };
    }

    private Timer.Task getPlayerAttackCalculationTimer() {
        return new Timer.Task() {
            @Override
            public void run() {
                int currentOpponentHP = Integer.parseInt(_currentOpponent.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP.toString()));
                int currentOpponentDP = Integer.parseInt(_currentOpponent.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.DEF.toString()));

                int damage = MathUtils.clamp(_currentPlayerAP - currentOpponentDP, 0, _currentPlayerAP);

                Gdx.app.log(TAG, "ENEMY HAS " + currentOpponentHP + " hit with damage: " + damage);

                currentOpponentHP = MathUtils.clamp(currentOpponentHP - damage, 0, currentOpponentHP);
                _currentOpponent.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.HP.toString(), String.valueOf(currentOpponentHP));

                Gdx.app.log(TAG, "Player attacks " + _currentOpponent.getEntityConfig().getEntityID() + " leaving it with HP: " + currentOpponentHP);

                _currentOpponent.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.HIT_DAMAGE_TOTAL.toString(), String.valueOf(damage));
                if( damage > 0 ){
                    BattleState.this.notify(_currentOpponent, BattleObserver.BattleEvent.OPPONENT_HIT_DAMAGE);
                }

                if (currentOpponentHP == 0) {
                    BattleState.this.notify(_currentOpponent, BattleObserver.BattleEvent.OPPONENT_DEFEATED);
                }

                BattleState.this.notify(_currentOpponent, BattleObserver.BattleEvent.PLAYER_TURN_DONE);
            }
        };
    }

    private Timer.Task getOpponentAttackCalculationTimer() {
        return new Timer.Task() {
            @Override
            public void run() {
                int currentOpponentHP = Integer.parseInt(_currentOpponent.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP.toString()));

                if (currentOpponentHP <= 0) {
                    BattleState.this.notify(_currentOpponent, BattleObserver.BattleEvent.OPPONENT_TURN_DONE);
                    return;
                }

                int currentOpponentAP = Integer.parseInt(_currentOpponent.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.ATK.toString()));
                int damage = MathUtils.clamp(currentOpponentAP - _currentPlayerDP, 0, currentOpponentAP);
                int hpVal = ProfileManager.getInstance().getProperty("currentPlayerHP", Integer.class);
                hpVal = MathUtils.clamp( hpVal - damage, 0, hpVal);
                ProfileManager.getInstance().setProperty("currentPlayerHP", hpVal);

                if( damage > 0 ) {
                    BattleState.this.notify(_currentOpponent, BattleObserver.BattleEvent.PLAYER_HIT_DAMAGE);
                }

                Gdx.app.log(TAG, "Player HIT for " + damage + " BY " + _currentOpponent.getEntityConfig().getEntityID() + " leaving player with HP: " + hpVal);

                BattleState.this.notify(_currentOpponent, BattleObserver.BattleEvent.OPPONENT_TURN_DONE);
            }
        };
    }

    private Timer.Task getChooseNextCharacterTurnTimer() {
        return new Timer.Task() {
            @Override
            public void run() {
                if (characterTurnList.size() == 0) {
                    // get list of all characters
                    for (Entity entity : currentPartyList) {
                        characterTurnList.add(entity);
                    }

                    for (Entity entity : currentEnemyList) {
                        characterTurnList.add(entity);
                    }

                    // sort list in descending order by SPD
                    Collections.sort(characterTurnList, new EntitySpeedComparator());
                }

                if (characterTurnList.size() > 0) {
                    // get character at top of list and remove it
                    setCurrentTurnCharacter(characterTurnList.get(0));
                    characterTurnList.remove(0);
                }

                Gdx.app.log(TAG, "Turn List:");
                for (Entity entity : characterTurnList) {
                    Gdx.app.log(TAG, entity.getEntityConfig().getDisplayName() + " " + entity.getEntityConfig().getEntityProperties().get(String.valueOf(EntityConfig.EntityProperties.SPD)));
                }
            }
        };
    }

    public void playerRuns(){
        // todo: randomize
        notify(_currentOpponent, BattleObserver.BattleEvent.PLAYER_RUNNING);
        /*
        int randomVal = MathUtils.random(1,100);
        if( _chanceOfEscape > randomVal  ) {
            notify(_currentOpponent, BattleObserver.BattleEvent.PLAYER_RUNNING);
        }else if (randomVal > _criticalChance){
            opponentAttacks();
        }else{
            return;
        }
        */
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
