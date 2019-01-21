package com.smoftware.elmour.UI;

public interface SignPopUpObserver {
    public static enum SignPopUpEvent {
        INTERACTION_THREAD_EXIT
    }

    void onNotify(final int value, SignPopUpEvent event);
}
