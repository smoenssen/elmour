package com.smoftware.elmour.battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.smoftware.elmour.ElmourGame;
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

    ElmourGame game;

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

    public BattleState(ElmourGame game){
        this.game = game;
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
        String sHPVal;
        Entity entity1 = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.CARMEN);
        game.statusUI.getAllStatProperties(entity1);
        sHPVal = entity1.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP.toString());
        entity1.setAlive(Integer.parseInt(sHPVal) > 0);
        entity1.setBattleEntityType(Entity.BattleEntityType.PARTY);
        entity1.setBattlePosition(battlePosition++);
        notify(entity1, BattleObserver.BattleEvent.PARTY_MEMBER_ADDED);
        currentPartyList.add(entity1);

        Entity entity2 = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.CHARACTER_1);
        game.statusUI.getAllStatProperties(entity2);
        sHPVal = entity2.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP.toString());
        entity2.setAlive(Integer.parseInt(sHPVal) > 0);
        entity2.setBattleEntityType(Entity.BattleEntityType.PARTY);
        entity2.setBattlePosition(battlePosition++);
        notify(entity2, BattleObserver.BattleEvent.PARTY_MEMBER_ADDED);
        currentPartyList.add(entity2);

        Entity entity3 = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.CHARACTER_2);
        game.statusUI.getAllStatProperties(entity3);
        sHPVal = entity3.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP.toString());
        entity3.setAlive(Integer.parseInt(sHPVal) > 0);
        entity3.setBattleEntityType(Entity.BattleEntityType.PARTY);
        entity3.setBattlePosition(battlePosition++);
        notify(entity3, BattleObserver.BattleEvent.PARTY_MEMBER_ADDED);
        currentPartyList.add(entity3);

        Entity entity4 = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.JUSTIN);
        game.statusUI.getAllStatProperties(entity4);
        sHPVal = entity4.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP.toString());
        entity4.setAlive(Integer.parseInt(sHPVal) > 0);
        entity4.setBattleEntityType(Entity.BattleEntityType.PARTY);
        entity4.setBattlePosition(battlePosition++);
        notify(entity4, BattleObserver.BattleEvent.PARTY_MEMBER_ADDED);
        currentPartyList.add(entity4);

        Entity entity5 = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.JAXON_1);
        game.statusUI.getAllStatProperties(entity5);
        sHPVal = entity5.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP.toString());
        entity5.setAlive(Integer.parseInt(sHPVal) > 0);
        entity5.setBattleEntityType(Entity.BattleEntityType.PARTY);
        entity5.setBattlePosition(battlePosition++);
        notify(entity5, BattleObserver.BattleEvent.PARTY_MEMBER_ADDED);
        currentPartyList.add(entity5);
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

        // see if current turn character is an enemy
        if (currentTurnCharacter.getBattleEntityType() == Entity.BattleEntityType.ENEMY) {
            // select an alive party character for battle, right now just totally random (easy mode)
            boolean isAnAliveMember = false;
            for (Entity partyMember : currentPartyList) {
                if (partyMember.isAlive()) {
                    isAnAliveMember = true;
                    break;
                }
            }

            if (isAnAliveMember) {
                do {
                    currentSelectedCharacter = currentPartyList.get(MathUtils.random(0, currentPartyList.size - 1));
                }
                while (!currentSelectedCharacter.isAlive());

                setCurrentSelectedCharacter(currentSelectedCharacter);
                opponentAttacks();
            }
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

    private Entity getCharacterAtBattlePosition(Array<Entity> list, int position) {
        Entity character = null;

        for (Entity entity : list) {
            if (entity.getBattlePosition() == position) {
                character = entity;
                break;
            }
        }
        return character;
    }

    private boolean isBlocked(Entity attacker, Entity defender) {
        // There is a % chance based on speed whether or not the attack will be blocked. This follows this equation:
        //      % = 50A / D
        // Where A is the Attacker’s SPD and D is the Defender’s SPD
        if (defender != null) {
            if (defender.isAlive()) {
                String SPD = attacker.getEntityConfig().getEntityProperties().get(String.valueOf(EntityConfig.EntityProperties.SPD));
                int attackerSPD = Integer.parseInt(SPD);

                SPD = defender.getEntityConfig().getEntityProperties().get(String.valueOf(EntityConfig.EntityProperties.SPD));
                int defenderSPD = Integer.parseInt(SPD);

                if (defenderSPD != 0) {
                    float chanceOfBlock = (50 * (float) attackerSPD) / (float) defenderSPD;
                    int randomVal = MathUtils.random(1, 100);

                    Gdx.app.log(TAG, "Chance of block = " + chanceOfBlock + ", randVal = " + randomVal);

                    if (chanceOfBlock > randomVal) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void frontMeleeAttack(){
        if (currentSelectedCharacter == null || currentTurnCharacter == null) { return; }

        Entity attacker = currentTurnCharacter;
        Array<Entity> defenderList = null;
        String message;
        boolean isEnemyBeingAttacked = false;

        if (currentSelectedCharacter.getBattleEntityType() == Entity.BattleEntityType.ENEMY) {
            defenderList = currentEnemyList;
            isEnemyBeingAttacked = true;
        }
        else {
            defenderList = currentPartyList;
        }

        // Melee attack against defender 1, 3 or 5 can be blocked by defender 2 or 4 if they are alive
        // i.e.,
        // 1
        //    2
        // 3
        //    4
        // 5
        // check for block
        Entity defender2 = getCharacterAtBattlePosition(defenderList, 2);
        Entity defender4 = getCharacterAtBattlePosition(defenderList, 4);
        boolean blockedBy2 = false;
        boolean blockedBy4 = false;

        switch (currentSelectedCharacter.getBattlePosition()) {
            case 1:
                blockedBy2 = isBlocked(attacker, defender2);
                break;
            case 3:
                blockedBy2 = isBlocked(attacker, defender2);
                blockedBy4 = isBlocked(attacker, defender4);
                break;
            case 5:
                blockedBy4 = isBlocked(attacker, defender4);
                break;
        }

        if (blockedBy2 && !blockedBy4) {
            message = "Melee attack on " + currentSelectedCharacter.getEntityConfig().getDisplayName() + " has been blocked by " + defender2.getEntityConfig().getDisplayName() + "!";
            BattleState.this.notify(attacker, defender2, BattleObserver.BattleEventWithMessage.ATTACK_BLOCKED, message);
        }
        else if (!blockedBy2 && blockedBy4) {
            message = "Melee attack on " + currentSelectedCharacter.getEntityConfig().getDisplayName() + " has been blocked by " + defender4.getEntityConfig().getDisplayName() + "!";
            BattleState.this.notify(attacker, defender4, BattleObserver.BattleEventWithMessage.ATTACK_BLOCKED, message);
        }
        else if (blockedBy2 && blockedBy4) {
            // blocked by both, so randomly pick one as the blocker
            int randomNum = MathUtils.random(1, 2);
            if (randomNum == 1) {
                message = "Melee attack on " + currentSelectedCharacter.getEntityConfig().getDisplayName() + " has been blocked by " + defender2.getEntityConfig().getDisplayName() + "!";
                BattleState.this.notify(attacker, defender2, BattleObserver.BattleEventWithMessage.ATTACK_BLOCKED, message);
            }
            else {
                message = "Melee attack on " + currentSelectedCharacter.getEntityConfig().getDisplayName() + " has been blocked by " + defender4.getEntityConfig().getDisplayName() + "!";
                BattleState.this.notify(attacker, defender4, BattleObserver.BattleEventWithMessage.ATTACK_BLOCKED, message);
            }
        }
        else {
            // if got this far, then kick off animation for successful attack
            if (isEnemyBeingAttacked) {
                BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEventWithMessage.PLAYER_ATTACKS, "");

                if (!_playerAttackCalculations.isScheduled()) {
                    Timer.schedule(_playerAttackCalculations, 1.75f);
                }
            }
            else {
                BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEventWithMessage.OPPONENT_ATTACKS, "");

                if( !_opponentAttackCalculations.isScheduled() ){
                    Timer.schedule(_opponentAttackCalculations, 1.75f);
                }
            }
        }
    }

    public void opponentAttacks() {
        // kick off animation for opponent attacking
        frontMeleeAttack();
/*
        BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEventWithMessage.OPPONENT_ATTACKS, "");

        if( !_opponentAttackCalculations.isScheduled() ){
            Timer.schedule(_opponentAttackCalculations, 1.75f);
        }*/
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

    private void removeSelectedCharacterFromTurnList() {
        for (int i = 0; i < characterTurnList.size(); i++) {
            Entity character = characterTurnList.get(i);
            if (character.getEntityConfig().getEntityID().equals(currentSelectedCharacter.getEntityConfig().getEntityID())) {
                characterTurnList.remove(i);
                break;
            }
        }
    }

    private int calculateHitPointDamage() {
        /*
        HPD = MV(A^2) / 5D
        POW = M(A^2) / 5F

        Where M = 10% of DMG when casting spell, else = 1
        Where A = Attacker's ATK or MATK
        Where D = Defender's DEF or MDEF
        Where F = Attacker's DEF
        Where V = Deviation of 0.9 and 1.1

        Where HPD = amount of damage the defendant takes.
                Always round up.
        Where POW = expected HPD, used for Hard Mode, when deciding who to attack
        */

        int HPD = 0;

        String attackerATK = currentTurnCharacter.getEntityConfig().getEntityProperties().get(String.valueOf(EntityConfig.EntityProperties.ATK));
        String defenderDEF = currentSelectedCharacter.getEntityConfig().getEntityProperties().get(String.valueOf(EntityConfig.EntityProperties.DEF));

        int A = Integer.parseInt(attackerATK);
        int D = Integer.parseInt(defenderDEF);
        float V = MathUtils.random(0.9f, 1.1f);
        float M = 1.0f;
        float fHPD = (M * V * (float)Math.pow(A, 2)) / (5 * D);
        HPD = (int)fHPD + 1;    //round up

        Gdx.app.log(TAG, "attackerATK = " + attackerATK + ", defenderDEF = " + defenderDEF + ", deviation = " + V);

        return HPD;
    }

    private Timer.Task getPlayerAttackCalculationTimer() {
        return new Timer.Task() {
            @Override
            public void run() {
                int hitPoints = calculateHitPointDamage();

                if (hitPoints < 0)
                    hitPoints = 0;

                Gdx.app.log(TAG, "enemy takes " + hitPoints + " hit points");

                String enemyHP = currentSelectedCharacter.getEntityConfig().getEntityProperties().get(String.valueOf(EntityConfig.EntityProperties.HP));
                int newEnemyHP = MathUtils.clamp(Integer.parseInt(enemyHP) - hitPoints, 0, Integer.parseInt(enemyHP));
                currentSelectedCharacter.getEntityConfig().setPropertyValue(String.valueOf(EntityConfig.EntityProperties.HP), String.format("%d" , newEnemyHP));

                String message = String.format("%s attacked %s, dealing %s HP.", currentTurnCharacter.getEntityConfig().getDisplayName(),
                        currentSelectedCharacter.getEntityConfig().getDisplayName(), hitPoints);

                if (hitPoints > 0) {
                    BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEventWithMessage.OPPONENT_HIT_DAMAGE, String.format("%d", hitPoints));
                }

                Gdx.app.log(TAG, "new enemy HP = " + newEnemyHP);

                if (newEnemyHP == 0) {
                    currentSelectedCharacter.setAlive(false);

                    // remove enemy from turn list if it's there
                    removeSelectedCharacterFromTurnList();

                    BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.OPPONENT_DEFEATED);

                    // see if all enemies are dead
                    boolean allDead = true;
                    for (Entity enemy : currentEnemyList) {
                        if (enemy.isAlive()) {
                            allDead = false;
                            break;
                        }
                    }

                    if (allDead) {
                        BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.BATTLE_WON);
                    }
                }

                BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEventWithMessage.PLAYER_TURN_DONE, message);
            }
        };
    }

    private Timer.Task getOpponentAttackCalculationTimer() {
        return new Timer.Task() {
            @Override
            public void run() {
                int hitPoints = calculateHitPointDamage();

                if (hitPoints < 0)
                    hitPoints = 0;

                Gdx.app.log(TAG, "player takes " + hitPoints + " hit points");

                String playerHP = currentSelectedCharacter.getEntityConfig().getEntityProperties().get(String.valueOf(EntityConfig.EntityProperties.HP));
                int newPlayerHP = MathUtils.clamp(Integer.parseInt(playerHP) - hitPoints, 0, Integer.parseInt(playerHP));
                currentSelectedCharacter.getEntityConfig().setPropertyValue(String.valueOf(EntityConfig.EntityProperties.HP), String.format("%d" , newPlayerHP));

                Gdx.app.log(TAG, "new player HP = " + newPlayerHP);

                String message = String.format("%s attacked %s, dealing %s HP.", currentTurnCharacter.getEntityConfig().getDisplayName(),
                        currentSelectedCharacter.getEntityConfig().getDisplayName(), Integer.parseInt(playerHP) - newPlayerHP);

                if (hitPoints > 0) {
                    // update current entity config in factory
                    EntityFactory.getInstance().setEntityByName(currentSelectedCharacter.getEntityConfig().getEntityID(),
                            currentSelectedCharacter.getEntityConfig());

                    BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEventWithMessage.PLAYER_HIT_DAMAGE, String.format("%d", hitPoints));
                }

                if (newPlayerHP == 0) {
                    currentSelectedCharacter.setAlive(false);

                    // remove player from turn list if it's there
                    removeSelectedCharacterFromTurnList();

                    BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.PLAYER_DEFEATED);

                    // see if all party members are dead
                    boolean allDead = true;
                    for (Entity partyMember : currentPartyList) {
                        if (partyMember.isAlive()) {
                            allDead = false;
                            break;
                        }
                    }

                    if (allDead) {
                        BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.BATTLE_LOST);
                    }
                }

                BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEventWithMessage.OPPONENT_TURN_DONE, message);
            }
        };
    }

    public void gameOver() {
        BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.GAME_OVER);
        characterTurnList.clear();
    }

    public void battleOver() {
        BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.BATTLE_OVER);
        characterTurnList.clear();
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
            if (entity.isAlive())
                characterTurnList.add(entity);
        }

        for (Entity entity : currentEnemyList) {
            if (entity.isAlive())
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
