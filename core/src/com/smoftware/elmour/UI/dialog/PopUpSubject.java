package com.smoftware.elmour.UI.dialog;

public interface PopUpSubject {
    void addObserver(PopUpObserver popUpObserver);
    void removeObserver(PopUpObserver popUpObserver);
    void removeAllObservers();
    void notify(final int value, PopUpObserver.PopUpEvent event);
}
