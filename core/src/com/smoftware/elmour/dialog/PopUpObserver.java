package com.smoftware.elmour.dialog;

public interface PopUpObserver {
    enum PopUpEvent {
        INTERACTION_THREAD_EXIT
    }

    void onNotify(final int value, PopUpEvent event);
}
