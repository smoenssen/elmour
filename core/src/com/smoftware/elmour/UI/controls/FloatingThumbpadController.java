package com.smoftware.elmour.UI.controls;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.smoftware.elmour.main.ElmourGame;

/**
 * Created by moenssr on 8/30/2017.
 */

public class FloatingThumbpadController extends FixedThumbpadController {

    private Vector2 screenPos;
    private Vector2 localPos;
    private InputEvent fakeTouchDownEvent;
    private int currentNumTouchPointsOnLeft = 0;
    private boolean isHidden = false;

    public FloatingThumbpadController() {
        screenPos = new Vector2();
        localPos = new Vector2();
        fakeTouchDownEvent = new InputEvent();
        fakeTouchDownEvent.setType(InputEvent.Type.touchDown);
    }

    @Override
    public Vector2 getDirection() {

        if (isHidden) {
            return new Vector2(0, 0);
        }

        // get first touchpoint on the left side of the screen
        // assuming there should only be one, but need to differentiate from
        // touchpoints on the right side of the screen used for the A/B buttons
        int newNumTouchPointsOnLeft = 0;
        boolean isFirstTouchIndexOnLeftSide = false;
        for (int i = 0; i < 20; i++) {
            if (Gdx.input.isTouched(i)) {
                screenPos.set(Gdx.input.getX(i), Gdx.input.getY(i));

                // Convert the touch point into local coordinates
                localPos.set(screenPos);
                localPos = touchpad.getParent().screenToLocalCoordinates(localPos);

                // only care about the left side of screen
                if (localPos.x < ElmourGame.V_WIDTH / 2) {
                    if (i == 0)
                        isFirstTouchIndexOnLeftSide = true;  // this matters because if first touch index is not on LH side, the fake touchdown event below should not be fired

                    newNumTouchPointsOnLeft++;
                    break;
                }
            }
        }

        if (newNumTouchPointsOnLeft > currentNumTouchPointsOnLeft) {
            // place the touchpad and show it
            touchpad.setPosition(localPos.x - touchpad.getWidth() / 2, localPos.y - touchpad.getHeight() / 2);
            touchpad.setVisible(true);

            if (isFirstTouchIndexOnLeftSide) {
                // Fire a touch down event to get the touchpad working.
                screenPos.set(Gdx.input.getX(0), Gdx.input.getY(0));
                Vector2 stagePos = touchpad.getStage().screenToStageCoordinates(screenPos);
                fakeTouchDownEvent.setStageX(stagePos.x);
                fakeTouchDownEvent.setStageY(stagePos.y);
                touchpad.fire(fakeTouchDownEvent);
            }
        }
        else if (newNumTouchPointsOnLeft == 0) {
            // No touch points on left side of screen, so hide the touchpad
            touchpad.setVisible(false);
        }

        currentNumTouchPointsOnLeft = newNumTouchPointsOnLeft;

        return super.getDirection();
    }

    @Override
    public void hide() {
        touchpad.setVisible(false);
        isHidden = true;
    }

    @Override
    public void show() {
        isHidden = false;
    }
}
