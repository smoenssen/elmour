package com.smoftware.elmour.battle;

import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.entities.Entity;
import com.smoftware.elmour.inventory.InventoryElement;

public class BattleSubject {
    private Array<BattleObserver> _observers;

    public BattleSubject(){
        _observers = new Array<BattleObserver>();
    }

    public void addObserver(BattleObserver battleObserver){
        _observers.add(battleObserver);
    }

    public void removeObserver(BattleObserver battleObserver){
        _observers.removeValue(battleObserver, true);
    }

    protected void notify(final Entity entity, BattleObserver.BattleEvent event){
        for(BattleObserver observer: _observers){
            observer.onNotify(entity, event);
        }
    }

    protected void notify(final Entity sourceEntity, final Entity destinationEntity, final BattleObserver.BattleEventWithMessage event, final String message){
        for(BattleObserver observer: _observers){
            observer.onNotify(sourceEntity, destinationEntity, event, message);
        }
    }

    protected void notify(final Entity entity, final InventoryElement.Effect effect){
        for(BattleObserver observer: _observers){
            observer.onNotify(entity, effect);
        }
    }
}
