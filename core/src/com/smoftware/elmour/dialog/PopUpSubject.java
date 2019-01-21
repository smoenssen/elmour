package com.smoftware.elmour.dialog;

public interface PopUpSubject {
    public void addObserver(PopUpObserver popUpObserver);
    public void removeObserver(PopUpObserver popUpObserver);
    public void removeAllObservers();
    public void notify(final int value, PopUpObserver.PopUpEvent event);
}
