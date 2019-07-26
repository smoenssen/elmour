package com.smoftware.elmour.UI.dialog;

/**
 * Created by steve on 2/2/19.
 */

public interface InputDialogSubject {
    void addObserver(InputDialogObserver observer);
    void removeObserver(InputDialogObserver observer);
    void removeAllInputDialogObservers();
    void notify(final String value, InputDialogObserver.InputDialogEvent event);
}
