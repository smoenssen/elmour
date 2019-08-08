package com.smoftware.elmour.screens;

import com.badlogic.gdx.utils.Array;

/**
 * Created by steve on 4/21/19.
 */

public class CutSceneSubject {
    private Array<CutSceneObserver> _observers;

    public CutSceneSubject(){
        _observers = new Array<>();
    }

    public void addObserver(CutSceneObserver observer){
        _observers.add(observer);
    }

    public void removeObserver(CutSceneObserver observer){
        _observers.removeValue(observer, true);
    }

    public void removeAllObservers(){
        for(CutSceneObserver observer: _observers){
            _observers.removeValue(observer, true);
        }
    }

    protected void notify(final String value, CutSceneObserver.CutSceneStatus event){
        for(CutSceneObserver observer: _observers){
            observer.onNotify(value, event);
        }
    }
}
