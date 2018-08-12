package com.smoftware.elmour.maps;

import com.badlogic.gdx.utils.Array;

/**
 * Created by steve on 8/8/18.
 */

public class MapSubject {
    private Array<MapObserver> _observers;

    public MapSubject(){
        _observers = new Array<MapObserver>();
    }

    public void addObserver(MapObserver mapObserver){
        _observers.add(mapObserver);
    }

    public void removeObserver(MapObserver mapObserver){
        _observers.removeValue(mapObserver, true);
    }

    protected void notify(MapObserver.MapEvent event){
        for(MapObserver observer: _observers){
            observer.onNotify(event);
        }
    }
}
