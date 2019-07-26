package com.smoftware.elmour.UI.huds;


import com.smoftware.elmour.main.Entity;

public interface StatusSubject {
    public void addObserver(StatusObserver statusObserver);
    public void removeObserver(StatusObserver statusObserver);
    public void removeAllObservers();
    public void notify(final int value, StatusObserver.StatusEvent event);
    public void notify(final Entity entity, final int value, StatusObserver.StatusEvent event);
}
