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
import com.smoftware.elmour.UI.StatusObserver;
import com.smoftware.elmour.Utility;
import com.smoftware.elmour.profile.ProfileManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class BattleState extends BattleSubject implements StatusObserver {
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
    private SpellsPowerElement selectedSpellPowerElement = null;

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
            int SPD0 = game.statusUI.getSPDValue(arg0);
            int SPD1 = game.statusUI.getSPDValue(arg1);
            if (SPD0 > SPD1) {
                return -1;
            }
            else if (SPD0 == SPD1) {
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
        this.game.statusUI.addObserver(this);

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
        for (Entity entity : currentEnemyList) {
            entity.getEntityConfig().clearTurnEffectList();
        }

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
        for (Entity entity : currentPartyList) {
            entity.getEntityConfig().clearTurnEffectList();
        }

        currentPartyList.clear();

        int battlePosition = 1;
        int HPVal;
        Entity entity1 = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.CARMEN);
        HPVal = game.statusUI.getHPValue(entity1);
        entity1.setAlive(HPVal > 0);
        entity1.setBattleEntityType(Entity.BattleEntityType.PARTY);
        entity1.setBattlePosition(battlePosition++);
        notify(entity1, BattleObserver.BattleEvent.PARTY_MEMBER_ADDED);
        currentPartyList.add(entity1);

        Entity entity2 = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.CHARACTER_1);
        HPVal = game.statusUI.getHPValue(entity2);
        entity2.setAlive(HPVal > 0);
        entity2.setBattleEntityType(Entity.BattleEntityType.PARTY);
        entity2.setBattlePosition(battlePosition++);
        notify(entity2, BattleObserver.BattleEvent.PARTY_MEMBER_ADDED);
        currentPartyList.add(entity2);

        Entity entity3 = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.CHARACTER_2);
        HPVal = game.statusUI.getHPValue(entity3);
        entity3.setAlive(HPVal > 0);
        entity3.setBattleEntityType(Entity.BattleEntityType.PARTY);
        entity3.setBattlePosition(battlePosition++);
        notify(entity3, BattleObserver.BattleEvent.PARTY_MEMBER_ADDED);
        currentPartyList.add(entity3);

        Entity entity4 = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.JUSTIN);
        HPVal = game.statusUI.getHPValue(entity4);
        entity4.setAlive(HPVal > 0);
        entity4.setBattleEntityType(Entity.BattleEntityType.PARTY);
        entity4.setBattlePosition(battlePosition++);
        notify(entity4, BattleObserver.BattleEvent.PARTY_MEMBER_ADDED);
        currentPartyList.add(entity4);

        Entity entity5 = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.JAXON_1);
        HPVal = game.statusUI.getHPValue(entity5);
        entity5.setAlive(HPVal > 0);
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

        // Previous turn cycle is considered over here:
        // Remove any effect items from the turn effect list that are zero,
        // or decrement the ones that are not zero (loop backwards since removing by index)
        //todo: if need to debug empty battleTextArea put breakpoint here at beginning of for loop
        for (int i = currentTurnCharacter.getEntityConfig().getTurnEffectListSize() - 1; i >= 0; i--) {
            InventoryElement.EffectItem effectItem = entity.getEntityConfig().getTurnEffectListItem(i);
            if (effectItem.turns == 0) {
                entity.getEntityConfig().removeTurnEffectItem(i);
                Gdx.app.log(TAG, currentTurnCharacter.getEntityConfig().getEntityID() + ": Turns for " + effectItem.effect.toString() + " are complete.");
            }
            else {
                effectItem.turns--;
                entity.getEntityConfig().setTurnEffectListItem(i, effectItem);
                Gdx.app.log(TAG, currentTurnCharacter.getEntityConfig().getEntityID() + ": Decrementing turns for " +
                        effectItem.effect.toString() + " to " + effectItem.turns);
            }
        }

        // For debugging
        game.statusUI.printCurrentStatusForEntity(currentTurnCharacter);

        // Now see what statuses still have turns. Send appropriate notifications for ALL statuses of the current character whose turn it is.
        Array<InventoryElement.Effect> statusArray = game.statusUI.getCurrentStatusArrayForEntity(currentTurnCharacter);
        for (InventoryElement.Effect effectStatus : statusArray) {
            BattleState.this.notify(currentTurnCharacter, effectStatus);
        }

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
            selectedSpellPowerElement = selectedElement;
            Gdx.app.log(TAG, selectedSpellPowerElement.name + " used on " + currentSelectedCharacter.getEntityConfig().getEntityID());
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
            if (defender.getBattleEntityType().equals(Entity.BattleEntityType.PARTY) &&
                    attacker.getBattleEntityType().equals(Entity.BattleEntityType.PARTY)) {
                // don't let a party member block another party member
                return false;
            }

            if (defender.isAlive()) {
                int attackerSPD = game.statusUI.getSPDValue(attacker);
                int defenderSPD = game.statusUI.getSPDValue(defender);

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
            float chanceOfHit = getChanceOfHit(1);
            int randomVal = MathUtils.random(1, 100);

            Gdx.app.log(TAG, "Chance of hit with melee attack = " + chanceOfHit + ", randVal = " + randomVal);

            chanceOfHit = 0;
            if (chanceOfHit > randomVal) {
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
            else {
                // this is a MISS
                message = "Melee attack on " + currentSelectedCharacter.getEntityConfig().getDisplayName() + " missed!";

                if( !getAttackMissTimer(message).isScheduled() ){
                    Timer.schedule(getAttackMissTimer(message), 1.75f);
                }

                //BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEventWithMessage.PLAYER_TURN_DONE, message);
            }
        }
    }

    private Timer.Task getAttackMissTimer(final String message) {
        return new Timer.Task() {
            @Override
            public void run() {
                BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEventWithMessage.MISS_HIT, message);
            }
        };
    }

    public void opponentAttacks() {
        // kick off animation for opponent attacking
        frontMeleeAttack();
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

    private void checkForDamage (int oldValue, int newValue) {
        int hitPoints = oldValue - newValue;
        if (hitPoints > 0) {
            if (currentSelectedCharacter.getBattleEntityType().equals(Entity.BattleEntityType.ENEMY)) {
                BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEventWithMessage.OPPONENT_HIT_DAMAGE, String.format("%d", hitPoints));
            } else {
                BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEventWithMessage.PLAYER_HIT_DAMAGE, String.format("%d", hitPoints));
            }
        }

        if (newValue == 0) {
            setCurrentSelectedCharacterDead();
        }
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
                int hpMax = game.statusUI.getHPMaxValue(currentSelectedCharacter);
                for (InventoryElement.EffectItem effectItem : selectedInventoryElement.effectList) {

                    int currVal = 0;
                    int newVal = 0;
                    if (effectItem.effect.equals(InventoryElement.Effect.HEAL_HP)) {
                        currVal = game.statusUI.getHPValue(currentSelectedCharacter);
                        newVal = MathUtils.clamp(currVal + effectItem.value, 0, hpMax);
                        message += getEffectPhrase(newVal - currVal, "HP", gotHPorMP);
                        game.statusUI.setHPValue(currentSelectedCharacter, newVal);
                        gotHPorMP = true;
                        checkForDamage(currVal, newVal);
                    }
                    else if (effectItem.effect.equals(InventoryElement.Effect.HEAL_HP_PERCENT)) {
                        currVal = game.statusUI.getHPValue(currentSelectedCharacter);
                        newVal = MathUtils.clamp(Utility.applyPercentageAndRoundUp(currVal, effectItem.value), 0, hpMax);
                        message += getEffectPhrase(newVal - currVal, "HP", gotHPorMP);
                        game.statusUI.setHPValue(currentSelectedCharacter, newVal);
                        gotHPorMP = true;
                        checkForDamage(currVal, newVal);
                    }
                    else if (effectItem.effect.equals(InventoryElement.Effect.HEAL_MP) &&
                            currentSelectedCharacter.getBattleEntityType().equals(Entity.BattleEntityType.PARTY)) {
                        int mpMax = game.statusUI.getMPMaxValue(currentSelectedCharacter);
                        currVal = game.statusUI.getMPValue(currentSelectedCharacter);
                        newVal = MathUtils.clamp(currVal + effectItem.value, 0, mpMax);
                        message += getEffectPhrase(newVal - currVal, "MP", gotHPorMP);
                        game.statusUI.setMPValue(currentSelectedCharacter, newVal);
                        gotHPorMP = true;
                    }
                    else if (effectItem.effect.equals(InventoryElement.Effect.HEAL_MP_PERCENT) &&
                            currentSelectedCharacter.getBattleEntityType().equals(Entity.BattleEntityType.PARTY)) {
                        int mpMax = game.statusUI.getMPMaxValue(currentSelectedCharacter);
                        currVal = game.statusUI.getMPValue(currentSelectedCharacter);
                        newVal = MathUtils.clamp(Utility.applyPercentageAndRoundUp(currVal, effectItem.value), 0, mpMax);
                        message += getEffectPhrase(newVal - currVal, "MP", gotHPorMP);
                        game.statusUI.setMPValue(currentSelectedCharacter, newVal);
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

                        // all other entity effect items get added to the entity's turn list
                        InventoryElement.EffectItem itemToAdd = new InventoryElement.EffectItem();
                        itemToAdd.effect = effectItem.effect;
                        itemToAdd.value = effectItem.value;
                        itemToAdd.turns = effectItem.turns;

                        if (itemToAdd.turns == 0)
                            itemToAdd.turns = selectedInventoryElement.turns;

                        currentSelectedCharacter.getEntityConfig().addTurnEffectItem(itemToAdd);
                        Gdx.app.log(TAG, currentSelectedCharacter.getEntityConfig().getEntityID() + ": Adding " + itemToAdd.turns + " turns for " + itemToAdd.effect);
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

                    // I don't think I need this
                    //currentSelectedCharacter.getEntityConfig().setPropertyValue(effectItem.effect.toString(), Integer.toString(newVal));
                }

                // for debugging
                game.statusUI.printCurrentStatusForEntity(currentSelectedCharacter);

                // Send appropriate notifications for ALL statuses of the selected character.
                Array<InventoryElement.Effect> statusArray = game.statusUI.getCurrentStatusArrayForEntity(currentSelectedCharacter);
                for (InventoryElement.Effect effectStatus : statusArray) {
                    BattleState.this.notify(currentSelectedCharacter, effectStatus);
                }

                BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEventWithMessage.PLAYER_APPLIED_INVENTORY, message);
            }
        };
    }

    private float getChanceOfHit(int ACC) {
        /* % chance = ((A - E) / 2 + 90) * B
         Where A = ACC of turn character and E = AVO of the selected character.
         B = Spell / Power ACC, or 1 for standard attacks
        */

        int A = game.statusUI.getACCValue(currentTurnCharacter);
        int E = game.statusUI.getAVOValue(currentSelectedCharacter);
        float B = (float)ACC / 100;
        float chance = ((float)(A - E) / 2f + 90) * B;
        return chance * 100;
    }

    private Timer.Task getApplySpellPowerTimer(){
        return new Timer.Task() {
            @Override
            public void run() {
                float chanceOfHit = getChanceOfHit(selectedSpellPowerElement.ACC);
                int randomVal = MathUtils.random(1, 100);

                Gdx.app.log(TAG, "Chance of hit with spell/power = " + chanceOfHit + ", randVal = " + randomVal);
                String message;

                if (chanceOfHit > randomVal) {
                    //todo: effect list
                    message = String.format("%s used %s on %s and it did something....", currentTurnCharacter.getEntityConfig().getDisplayName(),
                            selectedSpellPowerElement.name,
                            currentSelectedCharacter.getEntityConfig().getDisplayName());
                }
                else {
                    message = String.format("%s tried to use %s on %s but it had no effect.", currentTurnCharacter.getEntityConfig().getDisplayName(),
                            selectedSpellPowerElement.name,
                            currentSelectedCharacter.getEntityConfig().getDisplayName());
                }

                BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEventWithMessage.PLAYER_APPLIED_SPELL_POWER, message);
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
        int A = game.statusUI.getATKValue(currentTurnCharacter);
        int D = game.statusUI.getDEFValue(currentSelectedCharacter);
        float V = MathUtils.random(0.9f, 1.1f);
        float M = 1.0f;
        float fHPD = (M * V * (float)Math.pow(A, 2)) / (5 * D);
        HPD = Utility.rountUpToNextInt(fHPD);

        Gdx.app.log(TAG, "attacker ATK = " + A + ", defender DEF = " + D + ", deviation = " + V);

        return HPD;
    }

    private boolean checkForCriticalHit() {
        int LCK = game.statusUI.getLCKValue(currentTurnCharacter);
        float chanceOfCrit = (float)LCK / 5f;
        float random = MathUtils.random(0f, 100f);
        return chanceOfCrit > random;
    }

    private Timer.Task getPlayerAttackCalculationTimer() {
        return new Timer.Task() {
            @Override
            public void run() {
                int hitPoints = calculateHitPointDamage();
                boolean criticalHit = checkForCriticalHit();
                if (criticalHit) {
                    hitPoints *= 2;
                }

                if (hitPoints < 0)
                    hitPoints = 0;

                Gdx.app.log(TAG, "enemy takes " + hitPoints + " hit points");

                int enemyHP = game.statusUI.getHPValue(currentSelectedCharacter);
                int newEnemyHP = MathUtils.clamp(enemyHP - hitPoints, 0, enemyHP);
                game.statusUI.setHPValue(currentSelectedCharacter, newEnemyHP);

                hitPoints = enemyHP - newEnemyHP;

                String message = String.format("%s attacked %s, dealing %s HP.", currentTurnCharacter.getEntityConfig().getDisplayName(),
                        currentSelectedCharacter.getEntityConfig().getDisplayName(), hitPoints);

                if (hitPoints > 0) {
                    if (criticalHit) {
                        BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.CRITICAL_HIT);
                    }

                    BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEventWithMessage.OPPONENT_HIT_DAMAGE, String.format("%d", hitPoints));
                }

                Gdx.app.log(TAG, "new enemy HP = " + newEnemyHP);

                if (newEnemyHP == 0) {
                    setCurrentSelectedCharacterDead();
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
                boolean criticalHit = checkForCriticalHit();
                if (criticalHit) {
                    hitPoints *= 2;
                }

                if (hitPoints < 0)
                    hitPoints = 0;

                Gdx.app.log(TAG, "player takes " + hitPoints + " hit points");

                int playerHP = game.statusUI.getHPValue(currentSelectedCharacter);
                int newPlayerHP = MathUtils.clamp(playerHP - hitPoints, 0, playerHP);
                game.statusUI.setHPValue(currentSelectedCharacter, newPlayerHP);

                hitPoints = playerHP - newPlayerHP;

                Gdx.app.log(TAG, "new player HP = " + newPlayerHP);

                String message = String.format("%s attacked %s, dealing %s HP.", currentTurnCharacter.getEntityConfig().getDisplayName(),
                        currentSelectedCharacter.getEntityConfig().getDisplayName(), playerHP - newPlayerHP);

                if (hitPoints > 0) {
                    // update current entity config in factory
                    EntityFactory.getInstance().setEntityByName(currentSelectedCharacter.getEntityConfig().getEntityID(),
                            currentSelectedCharacter.getEntityConfig());

                    if (criticalHit) {
                        BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.CRITICAL_HIT);
                    }

                    BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEventWithMessage.PLAYER_HIT_DAMAGE, String.format("%d", hitPoints));
                }

                if (newPlayerHP == 0) {
                    setCurrentSelectedCharacterDead();
                }

                BattleState.this.notify(currentTurnCharacter, currentSelectedCharacter, BattleObserver.BattleEventWithMessage.OPPONENT_TURN_DONE, message);
            }
        };
    }

    private boolean areAllDead(Array<Entity> list) {
        boolean allDead = true;
        for (Entity enemy : list) {
            if (enemy.isAlive()) {
                allDead = false;
                break;
            }
        }
        return allDead;
    }

    private void setCurrentSelectedCharacterDead() {
        currentSelectedCharacter.setAlive(false);

        // remove character from turn list if it's there
        removeSelectedCharacterFromTurnList();

        if (currentSelectedCharacter.getBattleEntityType().equals(Entity.BattleEntityType.ENEMY)) {
            BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.OPPONENT_DEFEATED);

            // see if all enemies are dead
            if (areAllDead(currentEnemyList)) {
                BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.BATTLE_WON);
                resetBattleState();
            }
        }
        else {
            BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.PLAYER_DEFEATED);

            // see if all party members are dead
            if (areAllDead(currentPartyList)) {
                BattleState.this.notify(currentSelectedCharacter, BattleObserver.BattleEvent.BATTLE_LOST);
                resetBattleState();
            }
        }
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
            int SPD = game.statusUI.getSPDValue(entity);
            totalSPD += SPD;
            int HP = game.statusUI.getHPValue(entity);
            if (HP <= 0)
                numCharactersFainted++;
        }
        avgPartySpd = totalSPD / (float)currentPartyList.size;

        totalSPD = 0;
        for (Entity entity : currentEnemyList) {
            int SPD = game.statusUI.getSPDValue(entity);
            totalSPD += SPD;
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

        for (Entity entity : currentPartyList) {
            entity.getEntityConfig().clearTurnEffectList();
        }

        for (Entity entity : currentEnemyList) {
            entity.getEntityConfig().clearTurnEffectList();
        }

        inBattle = false;
        isBackBattle = false;
    }

    public void animationComplete() {
        notify(currentSelectedCharacter, BattleObserver.BattleEvent.ANNIMATION_COMPLETE);
    }

    @Override
    public void onNotify(int value, StatusEvent event) {
        switch (event) {
            case UPDATED_XP:
                // distribute EXP among all living party members
                int numLivingMembers = 0;
                for (Entity entity : currentPartyList) {
                    if (entity.isAlive())
                        numLivingMembers++;
                }
                float fVal = (float)value / (float)numLivingMembers;
                int memberCut = Utility.rountUpToNextInt(fVal);

                for (Entity entity : currentPartyList) {
                    if (entity.isAlive()) {
                        game.statusUI.setXPValue(entity, game.statusUI.getXPValue(entity) + memberCut);
                    }
                }
                break;
        }
    }

    @Override
    public void onNotify(Entity entity, int value, StatusEvent event) {
        switch (event) {
            case IS_REVIVED:
                entity.setAlive(true);
                break;
        }
    }
}
