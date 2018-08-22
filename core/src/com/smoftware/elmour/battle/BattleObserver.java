package com.smoftware.elmour.battle;

import com.smoftware.elmour.Entity;

public interface BattleObserver {
    public static enum BattleEvent{
        OPPONENT_ADDED,
        PARTY_MEMBBER_ADDED,
        OPPONENT_HIT_DAMAGE,
        OPPONENT_DEFEATED,
        OPPONENT_TURN_DONE,
        OPPONENT_BLOCKED,
        PLAYER_HIT_DAMAGE,
        PLAYER_RUNNING,
        PLAYER_TURN_DONE,
        PLAYER_TURN_START,
        PLAYER_USED_MAGIC,
        CHARACTER_SELECTED,
        CHARACTER_TURN_CHANGED,
        NONE
    }

    void onNotify(final Entity entity, BattleEvent event);
    void onNotify(final Entity sourceEntity, final Entity destinationEntity, BattleEvent event, String message);
}
