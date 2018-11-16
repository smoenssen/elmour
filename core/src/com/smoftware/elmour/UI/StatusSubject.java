package com.smoftware.elmour.UI;


import com.smoftware.elmour.Entity;

public interface StatusSubject {
    public void addObserver(StatusObserver statusObserver);
    public void removeObserver(StatusObserver statusObserver);
    public void removeAllObservers();
    public void notify(final int value, StatusObserver.StatusEvent event);
    public void notify(final Entity entity, final String value, StatusObserver.StatusEvent event);
}
