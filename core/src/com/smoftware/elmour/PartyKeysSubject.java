package com.smoftware.elmour;

import com.badlogic.gdx.utils.Array;

public class PartyKeysSubject {
    private Array<PartyKeysObserver> _observers;

    public PartyKeysSubject(){
        _observers = new Array<PartyKeysObserver>();
    }

    public void addObserver(PartyKeysObserver partyKeysObserver) {
        _observers.add(partyKeysObserver);
    }

    public void removeObserver(PartyKeysObserver partyKeysObserver) {
        _observers.removeValue(partyKeysObserver, true);
    }

    public void removeAllObservers() {
        for(PartyKeysObserver observer: _observers){
            _observers.removeValue(observer, true);
        }
    }

    public void notify(final PartyKeyItem partyKeyItem, final PartyKeysObserver.PartyKeysEvent event) {
        for(PartyKeysObserver observer: _observers){
            observer.onNotify(partyKeyItem, event);
        }
    }

    public void notify(final PartyKeyItem item1, final PartyKeyItem item2, final PartyKeysObserver.PartyKeysEvent event) {
        for(PartyKeysObserver observer: _observers){
            observer.onNotify(item1, item2, event);
        }
    }
}
