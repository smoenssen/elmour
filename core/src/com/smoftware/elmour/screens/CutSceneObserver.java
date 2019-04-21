package com.smoftware.elmour.screens;

/**
 * Created by steve on 4/21/19.
 */

public interface CutSceneObserver {
    enum CutSceneStatus {
        NOT_STARTED,
        STARTED,
        DONE }

    void onNotify(final String value, CutSceneStatus event);
}
