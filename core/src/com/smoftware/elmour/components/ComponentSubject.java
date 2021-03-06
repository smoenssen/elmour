package com.smoftware.elmour.components;

import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.entities.Entity;

public class ComponentSubject {
    private Array<ComponentObserver> _observers;

    public ComponentSubject(){
        _observers = new Array<ComponentObserver>();
    }

    public void addObserver(ComponentObserver conversationObserver){
        _observers.add(conversationObserver);
    }

    public void removeObserver(ComponentObserver conversationObserver){
        _observers.removeValue(conversationObserver, true);
    }

    public void removeAllObservers(){
        for(ComponentObserver observer: _observers){
            _observers.removeValue(observer, true);
        }
    }

    protected void notify(final String value, ComponentObserver.ComponentEvent event){
        for(ComponentObserver observer: _observers){
            observer.onNotify(value, event);
        }
    }

    protected void notify(final Entity entity, final String value, ComponentObserver.ComponentEvent event){
        for(ComponentObserver observer: _observers){
            observer.onNotify(entity, value, event);
        }
    }
}
