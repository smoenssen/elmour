package com.smoftware.elmour.dialog;

/**
 * Created by steve on 2/2/19.
 */

public interface InputDialogObserver {
    enum InputDialogEvent {
        GET_CHAR1_NAME,
        GET_CHAR2_NAME
    }

    void onInputDialogNotify(final String value, InputDialogEvent event);
}
