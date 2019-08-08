package com.smoftware.elmour.battle;

import com.smoftware.elmour.entities.Entity;
import com.smoftware.elmour.inventory.InventoryElement;

public interface BattleObserver {
    public static enum BattleEvent{
        ANNIMATION_COMPLETE,
        OPPONENT_ADDED,
        PARTY_MEMBER_ADDED,
        OPPONENT_DEFEATED,
        PLAYER_DEFEATED,
        PLAYER_ESCAPED,
        PLAYER_FAILED_TO_ESCAPE,
        PLAYER_USED_MAGIC,
        CRITICAL_HIT,
        WEAK_HIT,
        CHARACTER_SELECTED,
        CHARACTER_TURN_CHANGED,
        BATTLE_WON,
        BATTLE_LOST,
        GAME_OVER,
        BATTLE_OVER,
        NONE
    }

    public static enum BattleEventWithMessage{
        OPPONENT_ATTACKS,
        OPPONENT_TURN_DONE,
        OPPONENT_HIT_DAMAGE,
        PLAYER_ATTACKS,
        PLAYER_TURN_DONE,
        PLAYER_HIT_DAMAGE,
        PLAYER_APPLYING_INVENTORY,
        PLAYER_APPLIED_INVENTORY,
        PLAYER_APPLIED_SPELL_POWER,
        ATTACK_BLOCKED,
        MISS_HIT,
        PLAYER_THROWING_ITEM,
        PLAYER_THROWING_ITEM_BUT_MISSED,
        NONE
    }

    void onNotify(final Entity entity, BattleEvent event);
    void onNotify(final Entity sourceEntity, final Entity destinationEntity, final BattleEventWithMessage event, final String message);
    void onNotify(final Entity entity, final InventoryElement.Effect effect);
}
