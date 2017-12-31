package com.smoftware.elmour;

import com.badlogic.gdx.math.Vector2;

public class NPCPhysicsComponent extends PhysicsComponent {
    private static final String TAG = NPCPhysicsComponent.class.getSimpleName();

    private Entity.State _state;
    private Entity.A_ButtonAction a_BtnStatus;
    private Entity.B_ButtonAction b_BtnStatus;
    private Entity.ButtonState a_BtnState = Entity.ButtonState.IS_UP;
    private Entity.ButtonState b_BtnState = Entity.ButtonState.IS_UP;

    //private boolean is

    public NPCPhysicsComponent(){
        _boundingBoxLocation = BoundingBoxLocation.CENTER;
        initBoundingBox(0.4f, 0.15f);
    }

    @Override
    public void dispose(){
    }

    @Override
    public void receiveMessage(String message) {
        //Gdx.app.debug(TAG, "Got message " + message);
        String[] string = message.split(Component.MESSAGE_TOKEN);

        if( string.length == 0 ) return;

        //Specifically for messages with 1 object payload
        if( string.length == 2 ) {
           // if (ElmourGame.isAndroid()) {
                //todo
           // }
           // else {
                if (string[0].equalsIgnoreCase(MESSAGE.INIT_START_POSITION.toString())) {
                    _currentEntityPosition = _json.fromJson(Vector2.class, string[1]);
                    _nextEntityPosition.set(_currentEntityPosition.x, _currentEntityPosition.y);
                }
                else if (string[0].equalsIgnoreCase(MESSAGE.CURRENT_STATE.toString())) {
                    _state = _json.fromJson(Entity.State.class, string[1]);
                }
                else if (string[0].equalsIgnoreCase(MESSAGE.CURRENT_DIRECTION.toString())) {
                    _currentDirection = _json.fromJson(Entity.Direction.class, string[1]);
                }
                else if (string[0].equalsIgnoreCase(MESSAGE.A_BUTTON_STATUS.toString())) {
                    a_BtnStatus = _json.fromJson(Entity.A_ButtonAction.class, string[1]);

                    // only send message once per button press

                }
           // }
        }
    }

    @Override
    public void update(Entity entity, com.smoftware.elmour.maps.MapManager mapMgr, float delta) {
        updateBoundingBoxPosition(_nextEntityPosition);

        if( isEntityFarFromPlayer(mapMgr) ){
            entity.sendMessage(MESSAGE.ENTITY_DESELECTED);
        }

        if( _state == Entity.State.IMMOBILE ) return;

        if (    !isCollisionWithMapLayer(entity, mapMgr) &&
                !isCollisionWithMapEntities(entity, mapMgr) &&
                (_state == Entity.State.WALKING || _state == Entity.State.RUNNING)){
            setNextPositionToCurrent(entity);
        } else {
            updateBoundingBoxPosition(_currentEntityPosition);
        }
        calculateNextPosition(delta);
    }

    private boolean isEntityFarFromPlayer(com.smoftware.elmour.maps.MapManager mapMgr){
        //Check distance
        _selectionRay.set(mapMgr.getPlayer().getCurrentBoundingBox().x, mapMgr.getPlayer().getCurrentBoundingBox().y, 0.0f, _boundingBox.x, _boundingBox.y, 0.0f);
        float distance =  _selectionRay.origin.dst(_selectionRay.direction);

        if( distance <= _selectRayMaximumDistance ){
            return false;
        }else{
            return true;
        }
    }

    @Override
    protected boolean isCollisionWithMapEntities(Entity entity, com.smoftware.elmour.maps.MapManager mapMgr){
        //Test against player
        if( isCollision(entity, mapMgr.getPlayer()) ) {
            return true;
        }

        if( super.isCollisionWithMapEntities(entity, mapMgr) ){
            return true;
        }

        return false;
    }
}
