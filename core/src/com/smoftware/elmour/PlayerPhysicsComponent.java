package com.smoftware.elmour;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class PlayerPhysicsComponent extends PhysicsComponent {
    private static final String TAG = PlayerPhysicsComponent.class.getSimpleName();

    private Entity.State _state;
    private Entity.A_ButtonAction a_BtnStatus;
    private Entity.B_ButtonAction b_BtnStatus;
    private Entity.ButtonState a_BtnState = Entity.ButtonState.IS_UP;
    private Entity.ButtonState b_BtnState = Entity.ButtonState.IS_UP;

    private Vector3 _mouseSelectCoordinates;
    private boolean _isMouseSelectEnabled = false;
    private String _previousDiscovery;
    private String _previousEnemySpawn;
    private boolean interactionMsgReceived = false;

    public PlayerPhysicsComponent(){
        //_boundingBoxLocation = BoundingBoxLocation.CENTER;
        //initBoundingBox(0.3f, 0f);

        //reduce width and height of bounding box for better feel of collisions
        _boundingBoxLocation = BoundingBoxLocation.CENTER;
        initBoundingBox(0.4f, 0.4f);

        _previousDiscovery = "";
        _previousEnemySpawn = "0";

        _mouseSelectCoordinates = new Vector3(0,0,0);
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
            if (ElmourGame.isAndroid()) {
                // mobile controls
                if (string[0].equalsIgnoreCase(MESSAGE.INIT_START_POSITION.toString())) {
                    _currentEntityPosition = _json.fromJson(Vector2.class, string[1]);
                    _nextEntityPosition.set(_currentEntityPosition.x, _currentEntityPosition.y);
                    _previousDiscovery = "";
                    _previousEnemySpawn = "0";
                    notify(_previousEnemySpawn, ComponentObserver.ComponentEvent.ENEMY_SPAWN_LOCATION_CHANGED);
                }
                else if (string[0].equalsIgnoreCase(MESSAGE.CURRENT_STATE.toString())) {
                    _state = _json.fromJson(Entity.State.class, string[1]);
                }
                else if (string[0].equalsIgnoreCase(MESSAGE.A_BUTTON_STATUS.toString())) {
                    a_BtnStatus = _json.fromJson(Entity.A_ButtonAction.class, string[1]);

                    // the following message should only be sent once
                    if (a_BtnStatus == Entity.A_ButtonAction.PRESSED && a_BtnState == Entity.ButtonState.IS_UP) {
                        a_BtnState = Entity.ButtonState.IS_DOWN;
                    }
                    else if (a_BtnStatus == Entity.A_ButtonAction.RELEASED && a_BtnState == Entity.ButtonState.IS_DOWN) {
                        a_BtnState = Entity.ButtonState.IS_UP;
                    }
                }
                else if (string[0].equalsIgnoreCase(MESSAGE.B_BUTTON_STATUS.toString())) {
                    b_BtnStatus = _json.fromJson(Entity.B_ButtonAction.class, string[1]);
                }
                else if (string[0].equalsIgnoreCase(MESSAGE.CURRENT_JOYSTICK_POSITION.toString())) {
                    currentJoystickPosition = _json.fromJson(Vector2.class, string[1]);

                    // need to figure out direction based on joystick coordinates for purposes of image to display
                    if (_velocity.y != 0 && currentJoystickPosition.angle() > 36 && currentJoystickPosition.angle() <= 144)
                        _currentDirection = Entity.Direction.UP;
                    else if (_velocity.x != 0 && currentJoystickPosition.angle() > 144 && currentJoystickPosition.angle() <= 216)
                        _currentDirection = Entity.Direction.LEFT;
                    else if (_velocity.y != 0 && currentJoystickPosition.angle() > 216 && currentJoystickPosition.angle() <= 324)
                        _currentDirection = Entity.Direction.DOWN;
                    else if (_velocity.x != 0 && (currentJoystickPosition.angle() > 324 || currentJoystickPosition.angle() <= 36))
                        _currentDirection = Entity.Direction.RIGHT;
                    else {
                        _currentDirection = Entity.Direction.DOWN;
                        _state = Entity.State.IDLE;
                    }

                    //Gdx.app.log("tag", String.format(" Physics: State = %s, Direction = %s", _state.toString(), _currentDirection.toString()));
                }
            }
            else {
                if (string[0].equalsIgnoreCase(MESSAGE.INIT_START_POSITION.toString())) {
                    _currentEntityPosition = _json.fromJson(Vector2.class, string[1]);
                    _nextEntityPosition.set(_currentEntityPosition.x, _currentEntityPosition.y);
                    _previousDiscovery = "";
                    _previousEnemySpawn = "0";
                    notify(_previousEnemySpawn, ComponentObserver.ComponentEvent.ENEMY_SPAWN_LOCATION_CHANGED);
                }
                else if (string[0].equalsIgnoreCase(MESSAGE.CURRENT_STATE.toString())) {
                    _state = _json.fromJson(Entity.State.class, string[1]);
                }
                else if (string[0].equalsIgnoreCase(MESSAGE.A_BUTTON_STATUS.toString())) {
                    a_BtnStatus = _json.fromJson(Entity.A_ButtonAction.class, string[1]);
                }
                else if (string[0].equalsIgnoreCase(MESSAGE.B_BUTTON_STATUS.toString())) {
                    b_BtnStatus = _json.fromJson(Entity.B_ButtonAction.class, string[1]);
                }
                else if (string[0].equalsIgnoreCase(MESSAGE.CURRENT_DIRECTION.toString())) {
                    _currentDirection = _json.fromJson(Entity.Direction.class, string[1]);
                }
                else if (string[0].equalsIgnoreCase(MESSAGE.INIT_SELECT_ENTITY.toString())) {
                    _mouseSelectCoordinates = _json.fromJson(Vector3.class, string[1]);
                    _isMouseSelectEnabled = true;
                }
            }
        }
    }

    @Override
    public void update(Entity entity, MapManager mapMgr, float delta) {
        updateBoundingBoxPosition(_nextEntityPosition);
        updatePortalLayerActivation(mapMgr);
        updateDiscoverLayerActivation(mapMgr);
        updateEnemySpawnLayerActivation(mapMgr);

        if( _isMouseSelectEnabled ){
            selectMapEntityCandidate(mapMgr);
            _isMouseSelectEnabled = false;
        }

        if (interactionMsgReceived) {
            interactionMsgReceived = false;
            MapObject object = checkCollisionWithInteractionLayer(mapMgr);
            if (object != null)
                entity.sendMessage(MESSAGE.INTERACTION_COLLISION, _json.toJson(Entity.Interaction.valueOf(object.getName())));
            else
                entity.sendMessage(MESSAGE.INTERACTION_COLLISION, _json.toJson(Entity.Interaction.NONE));
        }

        if (    !isCollisionWithMapLayer(entity, mapMgr) &&
                !isCollisionWithMapEntities(entity, mapMgr) &&
                (_state == Entity.State.WALKING || _state == Entity.State.RUNNING)){
            setNextPositionToCurrent(entity);

            Camera camera = mapMgr.getCamera();
            camera.position.set(_currentEntityPosition.x, _currentEntityPosition.y, 0f);
            camera.update();

            if (_currentDirection != null)
                entity.sendMessage(MESSAGE.CURRENT_DIRECTION, _json.toJson(_currentDirection));
        }else{
            updateBoundingBoxPosition(_currentEntityPosition);
        }

        calculateNextPosition(delta, _state == Entity.State.RUNNING);
    }

    private void selectMapEntityCandidate(MapManager mapMgr){
        _tempEntities.clear();
        _tempEntities.addAll(mapMgr.getCurrentMapEntities());
        _tempEntities.addAll(mapMgr.getCurrentMapQuestEntities());

        //Convert screen coordinates to world coordinates, then to unit scale coordinates
        mapMgr.getCamera().unproject(_mouseSelectCoordinates);
        _mouseSelectCoordinates.x /= Map.UNIT_SCALE;
        _mouseSelectCoordinates.y /= Map.UNIT_SCALE;

        //Gdx.app.debug(TAG, "Mouse Coordinates " + "(" + _mouseSelectCoordinates.x + "," + _mouseSelectCoordinates.y + ")");

        for( Entity mapEntity : _tempEntities ) {
            //Don't break, reset all entities
            mapEntity.sendMessage(MESSAGE.ENTITY_DESELECTED);
            Rectangle mapEntityBoundingBox = mapEntity.getCurrentBoundingBox();
            //Gdx.app.debug(TAG, "Entity Candidate Location " + "(" + mapEntityBoundingBox.x + "," + mapEntityBoundingBox.y + ")");
            if (mapEntity.getCurrentBoundingBox().contains(_mouseSelectCoordinates.x, _mouseSelectCoordinates.y)) {
                //Check distance
                _selectionRay.set(_boundingBox.x, _boundingBox.y, 0.0f, mapEntityBoundingBox.x, mapEntityBoundingBox.y, 0.0f);
                float distance =  _selectionRay.origin.dst(_selectionRay.direction);

                if( distance <= _selectRayMaximumDistance ){
                    //We have a valid entity selection
                    //Picked/Selected
                    Gdx.app.debug(TAG, "Selected Entity! " + mapEntity.getEntityConfig().getEntityID());
                    mapEntity.sendMessage(MESSAGE.ENTITY_SELECTED);
                    notify(_json.toJson(mapEntity.getEntityConfig()), ComponentObserver.ComponentEvent.LOAD_CONVERSATION);
                }
            }
        }
        _tempEntities.clear();
    }

    private boolean updateDiscoverLayerActivation(MapManager mapMgr){
        MapLayer mapDiscoverLayer =  mapMgr.getQuestDiscoverLayer();

        if( mapDiscoverLayer == null ){
            return false;
        }

        Rectangle rectangle = null;

        for( MapObject object: mapDiscoverLayer.getObjects()){
            if(object instanceof RectangleMapObject) {
                rectangle = ((RectangleMapObject)object).getRectangle();

                if (_boundingBox.overlaps(rectangle) ){
                    String questID = object.getName();
                    String questTaskID = (String)object.getProperties().get("taskID");
                    String val = questID + MESSAGE_TOKEN + questTaskID;

                    if( questID == null ) {
                        return false;
                    }

                    if( _previousDiscovery.equalsIgnoreCase(val) ){
                        return true;
                    }else{
                        _previousDiscovery = val;
                    }

                    notify(_json.toJson(val), ComponentObserver.ComponentEvent.QUEST_LOCATION_DISCOVERED);
                    Gdx.app.debug(TAG, "Discover Area Activated");
                    return true;
                }
            }
        }
        return false;
    }

    private boolean updateEnemySpawnLayerActivation(MapManager mapMgr){
        MapLayer mapEnemySpawnLayer =  mapMgr.getEnemySpawnLayer();

        if( mapEnemySpawnLayer == null ){
            return false;
        }

        Rectangle rectangle = null;

        for( MapObject object: mapEnemySpawnLayer.getObjects()){
            if(object instanceof RectangleMapObject) {
                rectangle = ((RectangleMapObject)object).getRectangle();

                if (_boundingBox.overlaps(rectangle) ){
                    String enemySpawnID = object.getName();

                    if( enemySpawnID == null ) {
                        return false;
                    }

                    if( _previousEnemySpawn.equalsIgnoreCase(enemySpawnID) ){
                        //Gdx.app.debug(TAG, "Enemy Spawn Area already activated " + enemySpawnID);
                        return true;
                    }else{
                        Gdx.app.debug(TAG, "Enemy Spawn Area " + enemySpawnID + " Activated with previous Spawn value: " + _previousEnemySpawn);
                        _previousEnemySpawn = enemySpawnID;
                    }

                    notify(enemySpawnID, ComponentObserver.ComponentEvent.ENEMY_SPAWN_LOCATION_CHANGED);
                    return true;
                }
            }
        }

        //If no collision, reset the value
        if( !_previousEnemySpawn.equalsIgnoreCase(String.valueOf(0)) ){
            Gdx.app.debug(TAG, "Enemy Spawn Area RESET with previous value " + _previousEnemySpawn);
            _previousEnemySpawn = String.valueOf(0);
            notify(_previousEnemySpawn, ComponentObserver.ComponentEvent.ENEMY_SPAWN_LOCATION_CHANGED);
        }

        return false;
    }

    private boolean updatePortalLayerActivation(MapManager mapMgr){
        MapLayer mapPortalLayer =  mapMgr.getPortalLayer();

        if( mapPortalLayer == null ){
            Gdx.app.debug(TAG, "Portal Layer doesn't exist!");
            return false;
        }

        Rectangle rectangle = null;

        for( MapObject object: mapPortalLayer.getObjects()){
            if(object instanceof RectangleMapObject) {
                rectangle = ((RectangleMapObject)object).getRectangle();

                if (_boundingBox.overlaps(rectangle) ){
                    String mapName = object.getName();
                    if( mapName == null ) {
                        return false;
                    }

                    Gdx.app.debug(TAG, "loading map " + mapName);
                    mapMgr.setClosestStartPositionFromScaledUnits(_currentEntityPosition);
                    mapMgr.loadMap(MapFactory.MapType.valueOf(mapName));

                    _currentEntityPosition.x = mapMgr.getPlayerStartUnitScaled().x;
                    _currentEntityPosition.y = mapMgr.getPlayerStartUnitScaled().y;
                    _nextEntityPosition.x = mapMgr.getPlayerStartUnitScaled().x;
                    _nextEntityPosition.y = mapMgr.getPlayerStartUnitScaled().y;

                    Gdx.app.debug(TAG, "Portal Activated");
                    return true;
                }
            }
        }
        return false;
    }


}
