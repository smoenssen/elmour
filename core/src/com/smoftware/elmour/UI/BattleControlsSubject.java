package com.smoftware.elmour.UI;

import com.badlogic.gdx.utils.Array;

/**
 * Created by steve on 3/10/18.
 */

public class BattleControlsSubject {
    public void initBattleControlsSubject() {
        observers = new Array<BattleControlsObserver>();
    }
    private static Array<BattleControlsObserver> observers;

    public static void addObserver(BattleControlsObserver observer) {
        observers.add(observer);
    }

    public static void removeObserver(BattleControlsObserver observer) {
        observers.removeValue(observer, true);
    }

    public static void removeAllObservers() {
        observers.removeAll(observers, true);
    }

    public static void notify(Object data, BattleControlsObserver.BattleControlEvent event) {
        for(BattleControlsObserver observer: observers){
            observer.onBattleControlsNotify(data, event);
        }
    }
}
