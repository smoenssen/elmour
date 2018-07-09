package com.smoftware.elmour.battle;

import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.Entity;

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

    protected void notify(final Entity sourceEntity, final Entity destinationEntity, BattleObserver.BattleEvent event, String message){
        for(BattleObserver observer: _observers){
            observer.onNotify(sourceEntity, destinationEntity, event, message);
        }
    }
}
