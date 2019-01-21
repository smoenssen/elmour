package com.smoftware.elmour.UI;

public interface SignPopUpSubject {
    public void addObserver(SignPopUpObserver popUpObserver);
    public void removeObserver(SignPopUpObserver popUpObserver);
    public void removeAllObservers();
    public void notify(final int value, SignPopUpObserver.SignPopUpEvent event);
}
