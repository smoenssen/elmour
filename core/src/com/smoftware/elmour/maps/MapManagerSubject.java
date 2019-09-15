package com.smoftware.elmour.maps;

import com.badlogic.gdx.utils.Array;

/**
 * Created by steve on 9/14/19.
 */

public class MapManagerSubject {
    private Array<MapManagerObserver> _observers;

    public MapManagerSubject(){
        _observers = new Array<>();
    }

    public void addObserver(MapManagerObserver observer){
        _observers.add(observer);
    }

    public void removeObserver(MapManagerObserver observer){
        _observers.removeValue(observer, true);
    }

    protected void notify(MapManagerObserver.MapManagerEvent event, String value){
        for(MapManagerObserver observer: _observers){
            observer.onNotify(event, value);
        }
    }
}
