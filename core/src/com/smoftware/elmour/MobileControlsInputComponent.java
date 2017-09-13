package com.smoftware.elmour;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Json;

import java.util.*;

/**
 * Created by moenssr on 9/13/2017.
 */

public abstract class MobileControlsInputComponent extends MobileControlsSubject implements Component, InputProcessor {

    protected Entity.Direction _currentDirection = null;
    protected Entity.State _currentState = null;
    protected Json _json;

    protected enum ActionButtons {
        A_BUTTON_PRESSED, A_BUTTON_RELEASED, B_BUTTON_PRESSED, B_BUTTON_RELEASED
    }

    protected enum JoystickPosition {
        X, Y
    }

    protected static java.util.Map<InputComponent.ActionButtons, Boolean> actionButtons = new HashMap<InputComponent.ActionButtons, Boolean>();
    protected static java.util.Map<InputComponent.JoystickPosition, Float> joystickPosition = new HashMap<InputComponent.JoystickPosition, Float>();

    //initialize the hashmap for inputs
    static {
        actionButtons.put(InputComponent.ActionButtons.A_BUTTON_PRESSED, false);
        actionButtons.put(InputComponent.ActionButtons.A_BUTTON_RELEASED, false);
        actionButtons.put(InputComponent.ActionButtons.B_BUTTON_PRESSED, false);
        actionButtons.put(InputComponent.ActionButtons.B_BUTTON_RELEASED, false);
    };

    static {
        joystickPosition.put(InputComponent.JoystickPosition.X, new Float(0));
        joystickPosition.put(InputComponent.JoystickPosition.Y, new Float(0));
    }

    MobileControlsInputComponent(){
        _json = new Json();
    }

    public abstract void update(Entity entity, float delta);
}
