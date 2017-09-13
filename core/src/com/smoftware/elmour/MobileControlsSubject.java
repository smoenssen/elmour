package com.smoftware.elmour;

/**
 * Created by moenssr on 9/13/2017.
 */

public class MobileControlsSubject {
    public void addObserver(MobileControlsObserver observer);
    public void removeObserver(MobileControlsObserver observer);
    public void removeAllObservers();
    public void notify(final String value, MobileControlsObserver.MobileControlEvent event);
}
