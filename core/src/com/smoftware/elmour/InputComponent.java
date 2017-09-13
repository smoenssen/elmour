package com.smoftware.elmour;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Json;

import java.util.HashMap;
import java.util.Map;

public abstract class InputComponent extends ComponentSubject implements Component, InputProcessor {

    protected Entity.Direction _currentDirection = null;
    protected Entity.State _currentState = null;
    protected Json _json;

    protected enum Keys {
        LEFT, RIGHT, UP, DOWN, QUIT, PAUSE, SPACE
    }

    protected enum Mouse {
        SELECT, DOACTION
    }

    protected enum ActionButtons {
        A_BUTTON, B_BUTTON
    }

    protected class TogglePosition {
        float x = 0;
        float y = 0;
    }

    protected static Map<Keys, Boolean> keys = new HashMap<Keys, Boolean>();
    protected static Map<Mouse, Boolean> mouseButtons = new HashMap<Mouse, Boolean>();
    protected static Map<ActionButtons, Boolean> actionButtons = new HashMap<ActionButtons, Boolean>();
    protected static TogglePosition togglePosition;

    //initialize the hashmap for inputs
    static {
        keys.put(Keys.LEFT, false);
        keys.put(Keys.RIGHT, false);
        keys.put(Keys.UP, false);
        keys.put(Keys.DOWN, false);
        keys.put(Keys.QUIT, false);
        keys.put(Keys.PAUSE, false);
        keys.put(Keys.SPACE, false);
    };

    static {
        mouseButtons.put(Mouse.SELECT, false);
        mouseButtons.put(Mouse.DOACTION, false);
    };

    static {
        actionButtons.put(ActionButtons.A_BUTTON, false);
        actionButtons.put(ActionButtons.B_BUTTON, false);
    };

    InputComponent(){
        _json = new Json();
    }

    public abstract void update(Entity entity, float delta);

}
