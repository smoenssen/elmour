package com.smoftware.elmour.battle;

import com.smoftware.elmour.Entity;

public interface BattleObserver {
    public static enum BattleEvent{
        OPPONENT_ADDED,
        OPPONENT_HIT_DAMAGE,
        OPPONENT_DEFEATED,
        OPPONENT_TURN_DONE,
        PLAYER_HIT_DAMAGE,
        PLAYER_RUNNING,
        PLAYER_TURN_DONE,
        PLAYER_TURN_START,
        PLAYER_USED_MAGIC,
        NONE
    }

    void onNotify(final Entity enemyEntity, BattleEvent event);
}
