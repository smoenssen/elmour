package com.smoftware.elmour;

import com.badlogic.gdx.utils.Array;

public class PartyInventorySubject {
    private Array<PartyInventoryObserver> _observers;

    public PartyInventorySubject(){
        _observers = new Array<PartyInventoryObserver>();
    }

    public void addObserver(PartyInventoryObserver partyInventoryObserver) {
        _observers.add(partyInventoryObserver);
    }

    public void removeObserver(PartyInventoryObserver partyInventoryObserver) {
        _observers.removeValue(partyInventoryObserver, true);
    }

    public void removeAllObservers() {
        for(PartyInventoryObserver observer: _observers){
            _observers.removeValue(observer, true);
        }
    }

    public void notify(final PartyInventoryItem partyInventoryItem, final PartyInventoryObserver.PartyInventoryEvent event) {
        for(PartyInventoryObserver observer: _observers){
            observer.onNotify(partyInventoryItem, event);
        }
    }
}
