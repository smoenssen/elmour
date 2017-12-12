package com.smoftware.elmour.dialog;

import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;

public class ConversationGraphSubject {
    private Array<ConversationGraphObserver> _observers;

    public ConversationGraphSubject(){
        _observers = new Array<ConversationGraphObserver>();
    }

    public void addObserver(ConversationGraphObserver graphObserver) {
        _observers.add(graphObserver);
    }

    public void removeObserver(ConversationGraphObserver graphObserver) {
        _observers.removeValue(graphObserver, true);
    }

    public void removeAllObservers() {
        for(ConversationGraphObserver observer: _observers){
            _observers.removeValue(observer, true);
        }
    }

    public void notify(final ConversationGraph graph, ConversationGraphObserver.ConversationCommandEvent event) {
        for(ConversationGraphObserver observer: _observers){
            observer.onNotify(graph, event);
        }
    }

    public void notify(final ConversationGraph graph, final ArrayList<ConversationChoice> choices) {
        for(ConversationGraphObserver observer: _observers){
            observer.onNotify(graph, choices);
        }
    }

    public void notify(final String value, ConversationGraphObserver.ConversationCommandEvent event) {
        // can't use an iterator here because it can be nested
        for (int i = 0; i < _observers.size; i++) {
            ConversationGraphObserver observer = _observers.get(i);
            observer.onNotify(value, event);
        }
    }
}
