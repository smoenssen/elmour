package com.smoftware.elmour.UI;

import com.badlogic.gdx.utils.Array;

/**
 * Created by moenssr on 9/13/2017.
 */

public class MobileControlsSubject {
    public void initMobileControlsSubject() {
        observers = new Array<MobileControlsObserver>();
    }
    private static Array<MobileControlsObserver> observers;

    public static void addObserver(MobileControlsObserver observer) {
        observers.add(observer);
    }

    public static void removeObserver(MobileControlsObserver observer) {
        observers.removeValue(observer, true);
    }

    public static void removeAllObservers() {
        observers.removeAll(observers, true);
    }

    public static void notify(Object data, MobileControlsObserver.MobileControlEvent event) {
        for(MobileControlsObserver observer: observers){
            observer.onMobileControlsNotify(data, event);
        }
    }
}
