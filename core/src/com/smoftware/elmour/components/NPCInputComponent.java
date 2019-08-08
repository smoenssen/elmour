package com.smoftware.elmour.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.smoftware.elmour.entities.Entity;
import com.smoftware.elmour.main.ElmourGame;

public class NPCInputComponent extends InputComponent {
    private static final String TAG = NPCInputComponent.class.getSimpleName();

    private float _frameTime = 0.0f;
    private float idleTime = 0.0f;
    private boolean inIdleState = false;

    long lastTickCount;

    public NPCInputComponent(){
        _currentDirection = Entity.Direction.getRandomNext();
        _currentState = Entity.State.WALKING;

        lastTickCount = System.currentTimeMillis();
    }

    @Override
    public void receiveMessage(String message) {
        String[] string = message.split(MESSAGE_TOKEN);

        if( string.length == 0 ) return;

        //Specifically for messages with 1 object payload
        if( string.length == 1 ) {
            if (string[0].equalsIgnoreCase(MESSAGE.COLLISION_WITH_MAP.toString())) {
                _currentDirection = Entity.Direction.getRandomNext();
            }else if (string[0].equalsIgnoreCase(MESSAGE.COLLISION_WITH_ENTITY.toString())) {
                _currentState = Entity.State.IDLE;
                //_currentDirection = _currentDirection.getOpposite();
            }
        }

        if( string.length == 2 ) {
            if (string[0].equalsIgnoreCase(MESSAGE.INIT_STATE.toString())) {
                _currentState = _json.fromJson(Entity.State.class, string[1]);
            }else if (string[0].equalsIgnoreCase(MESSAGE.INIT_DIRECTION.toString())) {
                _currentDirection = _json.fromJson(Entity.Direction.class, string[1]);
            }
        }

    }

    @Override
    public void dispose(){

    }

    @Override
    public void update(Entity entity, float delta){

        // NOTE: delta time is 0.020, but actual elapsed time ranges from 10 - 20 ms
        /*
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        long elapsedTime = System.currentTimeMillis() - lastTickCount;
        lastTickCount = System.currentTimeMillis();
        Gdx.app.debug(TAG, String.format("elapsedTime = %d", elapsedTime));
        */

        if (ElmourGame.isAndroid()) {
            if (actionButtons.get(ActionButtons.A_BUTTON_PRESSED)) {
                entity.sendMessage(MESSAGE.A_BUTTON_STATUS, _json.toJson(Entity.A_ButtonAction.PRESSED));
            }
            else if (actionButtons.get(ActionButtons.A_BUTTON_RELEASED)) {
                entity.sendMessage(MESSAGE.A_BUTTON_STATUS, _json.toJson(Entity.A_ButtonAction.RELEASED));
            }
        }
        else {
            if (keys.get(Keys.SPACE)) {
                entity.sendMessage(MESSAGE.A_BUTTON_STATUS, _json.toJson(Entity.A_ButtonAction.PRESSED));
            }

            if (!keys.get(Keys.SPACE)) {
                entity.sendMessage(MESSAGE.A_BUTTON_STATUS, _json.toJson(Entity.A_ButtonAction.RELEASED));
            }
        }

        if(keys.get(Keys.QUIT)) {
            Gdx.app.exit();
        }

        //If IMMOBILE, don't update anything
        if( _currentState == Entity.State.IMMOBILE ) {
            entity.sendMessage(MESSAGE.CURRENT_STATE, _json.toJson(Entity.State.IMMOBILE));
            return;
        }

        _frameTime += delta;

        // Change direction after so many seconds if not currently idle.
        // NOTE: This function is called  at least 50 times per second, so this increases
        // the likelihood that a number will be hit. For example, the chance for
        // a number to be hit between 1 - 1000 that is under 20 is 100% for the period of a second.
        // The outer random check is used so the inner check of the frame time being
        // between 1 - 5 seconds is more accurately random. Otherwise, the inner random
        // check would always pass shortly after frame time is just over 1 second.
        if (MathUtils.random(1, 1000) < 20) {
            if (_frameTime > MathUtils.random(1.0f, 5.0f) && !inIdleState) {
                _currentDirection = Entity.Direction.getRandomNext();
                _frameTime = 0.0f;
            }
        }

        // randomly set to IDLE
        if (MathUtils.random(1, 1000) < 5 && !inIdleState) {
            // start being idle
            inIdleState = true;
            _currentState = Entity.State.IDLE;

        }
        else if (inIdleState) {
            // stay in idle state for at least 1.5 seconds
            idleTime += delta;

            if (idleTime > MathUtils.random(1.5f, 5.0f)) {
                inIdleState = false;
                idleTime = 0;
            }
        }
        else {
            // start walking
            _currentState = Entity.State.WALKING;
        }

        switch( _currentDirection ) {
            case LEFT:
                entity.sendMessage(MESSAGE.CURRENT_STATE, _json.toJson(_currentState));
                entity.sendMessage(MESSAGE.CURRENT_DIRECTION, _json.toJson(Entity.Direction.LEFT));
                break;
            case RIGHT:
                entity.sendMessage(MESSAGE.CURRENT_STATE, _json.toJson(_currentState));
                entity.sendMessage(MESSAGE.CURRENT_DIRECTION, _json.toJson(Entity.Direction.RIGHT));
                break;
            case UP:
                entity.sendMessage(MESSAGE.CURRENT_STATE, _json.toJson(_currentState));
                entity.sendMessage(MESSAGE.CURRENT_DIRECTION, _json.toJson(Entity.Direction.UP));
                break;
            case DOWN:
                entity.sendMessage(MESSAGE.CURRENT_STATE, _json.toJson(_currentState));
                entity.sendMessage(MESSAGE.CURRENT_DIRECTION, _json.toJson(Entity.Direction.DOWN));
                break;
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if( keycode == Input.Keys.Q){
            keys.put(Keys.QUIT, true);
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
