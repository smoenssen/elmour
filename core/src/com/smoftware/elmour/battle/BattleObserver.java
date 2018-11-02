package com.smoftware.elmour.battle;

import com.smoftware.elmour.Entity;

public interface BattleObserver {
    public static enum BattleEvent{
        ANNIMATION_COMPLETE,
        OPPONENT_ADDED,
        PARTY_MEMBER_ADDED,
        //OPPONENT_ATTACKS,
        //OPPONENT_BLOCKED,
        OPPONENT_HIT_DAMAGE,
        OPPONENT_DEFEATED,
        //OPPONENT_TURN_DONE,
        //PLAYER_ATTACKS,
        PLAYER_HIT_DAMAGE,
        PLAYER_DEFEATED,
        PLAYER_ESCAPED,
        PLAYER_FAILED_TO_ESCAPE,
        //PLAYER_TURN_DONE,
        PLAYER_TURN_START,
        PLAYER_USED_MAGIC,
        CHARACTER_SELECTED,
        CHARACTER_TURN_CHANGED,
        BATTLE_WON,
        BATTLE_LOST,
        NONE
    }

    public static enum BattleEventWithMessage{
        OPPONENT_ADDED,
        PARTY_MEMBER_ADDED,
        OPPONENT_ATTACKS,
        OPPONENT_HIT_DAMAGE,
        OPPONENT_DEFEATED,
        OPPONENT_TURN_DONE,
        PLAYER_ATTACKS,
        PLAYER_HIT_DAMAGE,
        PLAYER_DEFEATED,
        //PLAYER_RUNNING,
        PLAYER_TURN_DONE,
        PLAYER_TURN_START,
        PLAYER_USED_MAGIC,
        CHARACTER_SELECTED,
        CHARACTER_TURN_CHANGED,
        ATTACK_BLOCKED,
        NONE
    }

    void onNotify(final Entity entity, BattleEvent event);
    void onNotify(final Entity sourceEntity, final Entity destinationEntity, BattleEventWithMessage event, String message);
}
