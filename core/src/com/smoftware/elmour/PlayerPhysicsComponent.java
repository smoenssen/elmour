package com.smoftware.elmour;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.smoftware.elmour.maps.MapFactory;
import com.smoftware.elmour.maps.MapManager;

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

    private boolean isInteractButtonPressed = false;
    private boolean isInteractionCollisionMsgSent = false;
    private boolean isDidInteractiontMsgSent = false;
    private boolean isInteractionColliding = false;

    private boolean isConversationButtonPressed = false;
    private boolean isLoadConversationMsgSent = false;
    private boolean isShowConversationMsgSent = false;
    private boolean isNPCColliding = false;

    public PlayerPhysicsComponent(){
        //_boundingBoxLocation = BoundingBoxLocation.CENTER;
        //initBoundingBox(0.3f, 0f);
        _currentDirection = _lasttDirection = Entity.Direction.DOWN;

        a_BtnStatus = Entity.A_ButtonAction.RELEASED;
        b_BtnStatus = Entity.B_ButtonAction.RELEASED;

        //reduce width and height of bounding box for better feel of collisions
        _boundingBoxLocation = BoundingBoxLocation.CENTER;
        initBoundingBox(0.4f, 0.05f);

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
            // put common stuff here...

            // check for conversation in progress
            if (string[0].equalsIgnoreCase(MESSAGE.CONVERSATION_STATUS.toString())) {
                isConversationInProgress = _json.fromJson(Entity.ConversationStatus.class, string[1]) == Entity.ConversationStatus.IN_CONVERSATION;
            }

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

                    if (a_BtnStatus == Entity.A_ButtonAction.PRESSED) {
                        // only send message once per button press
                        if (!isDidInteractiontMsgSent) {
                            isInteractButtonPressed = true;
                            // check for collision
                            if (isInteractionColliding) {
                                isDidInteractiontMsgSent = true;
                                notify(_json.toJson(a_BtnState.toString()), ComponentObserver.ComponentEvent.DID_INTERACTION);
                                Gdx.app.log(TAG, "sending DID_INTERACTION");
                            }
                            // collision detection is handled in update()
                            // and this is where isInteractionCollisionMsgSent is set
                        }

                        if (!isShowConversationMsgSent) {
                            isConversationButtonPressed = true;
                            // check for collision
                            if (isNPCColliding) {
                                isShowConversationMsgSent = true;
                                notify(_json.toJson(a_BtnState.toString()), ComponentObserver.ComponentEvent.SHOW_CONVERSATION);
                                Gdx.app.log(TAG, "sending SHOW_CONVERSATION");
                            } else {
                                isShowConversationMsgSent = false;
                                //notify(_json.toJson(a_BtnState.toString()), ComponentObserver.ComponentEvent.HIDE_CONVERSATION);
                                //Gdx.app.log(TAG, "sending HIDE_CONVERSATION");
                            }
                        }
                    }
                    else if (a_BtnStatus == Entity.A_ButtonAction.RELEASED) {
                        // button released so reset variables
                        isInteractButtonPressed = false;
                        isDidInteractiontMsgSent = false;
                        isConversationButtonPressed = false;
                        isShowConversationMsgSent = false;
                    }
                }

                if (string[0].equalsIgnoreCase(MESSAGE.B_BUTTON_STATUS.toString())) {
                    b_BtnStatus = _json.fromJson(Entity.B_ButtonAction.class, string[1]);

                    if (b_BtnStatus == Entity.B_ButtonAction.PRESSED) {
                        isRunning = true;
                    }
                    else if (b_BtnStatus == Entity.B_ButtonAction.RELEASED){
                        isRunning = false;
                    }
                }

                if (string[0].equalsIgnoreCase(MESSAGE.CURRENT_JOYSTICK_POSITION.toString())) {
                    currentJoystickPosition = _json.fromJson(Vector2.class, string[1]);

                    // need to figure out direction based on joystick coordinates for purposes of image to display
                    if (_velocity.y > 0 && currentJoystickPosition.angle() > 36 && currentJoystickPosition.angle() <= 144)
                        _currentDirection = Entity.Direction.UP;
                    else if (_velocity.x < 0 && currentJoystickPosition.angle() > 144 && currentJoystickPosition.angle() <= 216)
                        _currentDirection = Entity.Direction.LEFT;
                    else if (_velocity.y < 0 && currentJoystickPosition.angle() > 216 && currentJoystickPosition.angle() <= 324)
                        _currentDirection = Entity.Direction.DOWN;
                    else if (_velocity.x > 0 && (currentJoystickPosition.angle() > 324 || currentJoystickPosition.angle() <= 36) && currentJoystickPosition.angle() != 0)
                        _currentDirection = Entity.Direction.RIGHT;
                    else {
                        // idle frame direction should be last direction of movement
                        _currentDirection = _lasttDirection;
                    }

                    _lasttDirection = _currentDirection;

                    //Gdx.app.log(TAG, "_currentDirection set to " + _currentDirection.toString());
                    //Gdx.app.log(TAG, String.format("_velocity.x = %3.2f, _velocity.y = %3.2f, angle = %3.2f", _velocity.x, _velocity.x, currentJoystickPosition.angle()));

                    // figure out state based on velocity
                    if (_velocity.x != 0 || _velocity.y != 0) {

                        if (isRunning)
                            _state = Entity.State.RUNNING;
                        else
                            _state = Entity.State.WALKING;
                    }
                    else {
                        _state = Entity.State.IDLE;
                    }
                    //Gdx.app.log(TAG, String.format(currentJoystickPosition.toString()));
                    //Gdx.app.log(TAG, String.format(" Physics: State = %s, Direction = %s", _state.toString(), _currentDirection.toString()));
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

                    // check for collisions

                    // collision detection is handled in update()
                    // only send message once per button press
                    if (!isDidInteractiontMsgSent && a_BtnStatus == Entity.A_ButtonAction.PRESSED) {
                        isInteractButtonPressed = true;

                        if (isInteractionColliding) {
                            isDidInteractiontMsgSent = true;
                            notify(_json.toJson(a_BtnState.toString()), ComponentObserver.ComponentEvent.DID_INTERACTION);
                            Gdx.app.log(TAG, "sending DID_INTERACTION");
                        }
                    }

                    if (!isShowConversationMsgSent && a_BtnStatus == Entity.A_ButtonAction.PRESSED) {
                        isConversationButtonPressed = true;

                        if (isNPCColliding) {
                            isShowConversationMsgSent = true;
                            notify(_json.toJson(a_BtnState.toString()), ComponentObserver.ComponentEvent.SHOW_CONVERSATION);
                            Gdx.app.log(TAG, "sending SHOW_CONVERSATION");
                        }
                        else {
                            //isShowConversationMsgSent = false;
                            //notify(_json.toJson(a_BtnState.toString()), ComponentObserver.ComponentEvent.HIDE_CONVERSATION);
                            //Gdx.app.log(TAG, "sending HIDE_CONVERSATION");
                        }
                    }

                    if (a_BtnStatus == Entity.A_ButtonAction.RELEASED) {
                        // button released so reset variables
                        isInteractButtonPressed = false;
                        isDidInteractiontMsgSent = false;
                        isConversationButtonPressed = false;
                        isShowConversationMsgSent = false;
                    }
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

                if (string[0].equalsIgnoreCase(MESSAGE.B_BUTTON_STATUS.toString())) {
                    b_BtnStatus = _json.fromJson(Entity.B_ButtonAction.class, string[1]);

                    if (b_BtnStatus == Entity.B_ButtonAction.PRESSED) {
                        isRunning = true;
                        _state = Entity.State.RUNNING;
                    }
                    else if (b_BtnStatus == Entity.B_ButtonAction.RELEASED){
                        isRunning = false;
                    }
                }
            }
        }
    }

    @Override
    public void update(Entity entity, com.smoftware.elmour.maps.MapManager mapMgr, float delta) {
        MapObject object = null;
        updateBoundingBoxPosition(_nextEntityPosition);
        updatePortalLayerActivation(mapMgr);
        updateDiscoverLayerActivation(mapMgr);
        updateEnemySpawnLayerActivation(mapMgr);

        // pass current state to graphics entity
        entity.sendMessage(MESSAGE.CURRENT_STATE, _json.toJson(_state));

        ///////////////////////////////////////////////////
        //
        // CHECK IF PASSING THROUGH A PLAYER Z_GATE OR SHADOW Z_GATE
        //
        object = checkCollisionWithZGatesLayer(mapMgr);
        if (object != null) {
            if (object.getName().contains("SHADOW"))
                MapFactory.getMap(mapMgr.getCurrentMapType()).setShadowZLayer(object.getName());
            else
                MapFactory.getMap(mapMgr.getCurrentMapType()).setPlayerZLayer(object.getName());
        }

        /////////////////////////////////////////
        //
        // CONVERSATION HANDLING
        //
        if (isConversationButtonPressed && !isLoadConversationMsgSent) {
            // send message only once per button press
            Entity npc = checkCollisionWithNPC(mapMgr);
            if (npc != null) {
                npc.sendMessage(MESSAGE.ENTITY_SELECTED);
                mapMgr.setCurrentSelectedMapEntity(npc);
                Gdx.app.log(TAG, "sending LOAD_CONVERSATION");
                notify(_json.toJson(npc.getEntityConfig()), ComponentObserver.ComponentEvent.LOAD_CONVERSATION);
                isLoadConversationMsgSent = true;
                isNPCColliding = true;
            }
            else {
                isNPCColliding = false;
                isLoadConversationMsgSent = false;
            }
        }
        else if (isNPCColliding) {
            // send message once no longer colliding //todo?
            if (checkCollisionWithNPC(mapMgr) == null) {
                isNPCColliding = false;
                isLoadConversationMsgSent = false;
            }
        }
        else {
            isLoadConversationMsgSent = false;
        }

        /////////////////////////////////////////
        //
        // INTERACTION HANDLING
        //
        if (isInteractButtonPressed && !isInteractionCollisionMsgSent) {
            // send message only once per button press
            // check for interaction layer collision
            object = checkCollisionWithInteractionLayer(mapMgr);
            if (object != null) {
                Gdx.app.log(TAG, "sending INTERACTION_COLLISION for " + object.getName());
                entity.sendMessage(MESSAGE.INTERACTION_COLLISION, _json.toJson(Entity.Interaction.valueOf(object.getName())));
                isInteractionCollisionMsgSent = true;
                isInteractionColliding = true;
            } else {
                isInteractionColliding = false;
            }
        }
        else if (isInteractionColliding) {
            // send message once no longer colliding
            if (checkCollisionWithInteractionLayer(mapMgr) == null) {
                isInteractionColliding = false;
                isInteractionCollisionMsgSent = false;
                entity.sendMessage(MESSAGE.INTERACTION_COLLISION, _json.toJson(Entity.Interaction.NONE));
            }
        }

        if (!isConversationButtonPressed) {
            isLoadConversationMsgSent = false;
        }

        if (!isInteractButtonPressed) {
            isInteractionCollisionMsgSent = false;
        }

        ///////////////////////////////////////////////////
        //
        // OBSTACLE COLLISION HANDLING AND SETTING POSITION
        //
        if (!isCollisionWithMapLayer(entity, mapMgr) && !isCollisionWithMapEntities(entity, mapMgr) &&
            (_state == Entity.State.WALKING || _state == Entity.State.RUNNING)) {
            updatePosition(entity, mapMgr);
        }
        else if (ElmourGame.isAndroid() && (_state == Entity.State.WALKING || _state == Entity.State.RUNNING)) {
            // check if okay to move next vertical or horizontal position based on joystick position
            // (prevents "sticking" to obstacle)
            calculateNextVerticalPosition(delta);
            updateBoundingBoxPosition(_nextEntityPosition);
            if (!isCollisionWithMapLayer(entity, mapMgr) && !isCollisionWithMapEntities(entity, mapMgr)) {
                updatePosition(entity, mapMgr);
            }
            else {
                calculateNextHorizontalPosition(delta);
                updateBoundingBoxPosition(_nextEntityPosition);
                if (!isCollisionWithMapLayer(entity, mapMgr) && !isCollisionWithMapEntities(entity, mapMgr)) {
                    updatePosition(entity, mapMgr);
                }
                else {
                    updateBoundingBoxPosition(_currentEntityPosition);
                }
            }
        }
        else {

            if (isConversationInProgress) {
                // face the NPC based on selection angle between player and NPC
                if (selectionAngle > 36 && selectionAngle <= 144)
                    _currentDirection = Entity.Direction.UP;
                else if (selectionAngle > 144 && selectionAngle <= 216)
                    _currentDirection = Entity.Direction.LEFT;
                else if (selectionAngle > 216 && selectionAngle <= 324)
                    _currentDirection = Entity.Direction.DOWN;
                else
                    // selectionAngle > 324 || selectionAngle <= 36
                    _currentDirection = Entity.Direction.RIGHT;

                // NOTE: state is forced to IDLE in graphics component
                entity.sendMessage(MESSAGE.CURRENT_DIRECTION, _json.toJson(_currentDirection));
            }

            updateBoundingBoxPosition(_currentEntityPosition);
        }

        calculateNextPosition(delta);
    }

    private void updatePosition(Entity entity, com.smoftware.elmour.maps.MapManager mapMgr) {
        // Don't allow movement if conversation is in progress
        // NOTE: this is just a fail safe: this function should never get called during a conversation
        if (!isConversationInProgress) {
            setNextPositionToCurrent(entity);

            Camera camera = mapMgr.getCamera();
            camera.position.set(_currentEntityPosition.x, _currentEntityPosition.y, 0f);
            camera.update();

            if (_currentDirection != null) {
                entity.sendMessage(MESSAGE.CURRENT_DIRECTION, _json.toJson(_currentDirection));
                //Gdx.app.log(TAG, "sending _currentDirection = " + _currentDirection.toString());
            }
        }
    }

    private Entity checkCollisionWithNPC(com.smoftware.elmour.maps.MapManager mapMgr) {
        Entity npc = null;

        _tempEntities.clear();
        _tempEntities.addAll(mapMgr.getCurrentMapEntities());
        _tempEntities.addAll(mapMgr.getCurrentMapQuestEntities());

        for( Entity mapEntity : _tempEntities ) {
            Rectangle mapEntityBoundingBox = mapEntity.getCurrentBoundingBox();

            //Check distance from center points of entities
            float npcCenterX = mapEntityBoundingBox.x + (mapEntityBoundingBox.getWidth() / 2);
            float npcCenterY = mapEntityBoundingBox.y + (mapEntityBoundingBox.getHeight() / 2);
            float playerCenterX = _boundingBox.x + (_boundingBox.getWidth() / 2);
            float playerCenterY = _boundingBox.y + (_boundingBox.getHeight() / 2);
            _selectionRay.set(playerCenterX, playerCenterY, 0.0f, npcCenterX, npcCenterY, 0.0f);
            selectionAngle = (new Vector2(npcCenterX, npcCenterY)).sub(new Vector2(playerCenterX, playerCenterY)).angle();
            float distance =  _selectionRay.origin.dst(_selectionRay.direction);

            //Gdx.app.log(TAG, String.format("Distance = %3.2f", distance));

            if( distance <= _selectRayMaximumDistance ){
                //We have a valid entity selection
                npc = mapEntity;
                break;
            }
        }

        _tempEntities.clear();
        return npc;
    }

    /*
    private boolean selectMapEntityCandidate(com.smoftware.elmour.maps.MapManager mapMgr){
        boolean messageSent = false;

        _tempEntities.clear();
        _tempEntities.addAll(mapMgr.getCurrentMapEntities());
        _tempEntities.addAll(mapMgr.getCurrentMapQuestEntities());

        //Convert screen coordinates to world coordinates, then to unit scale coordinates
        //mapMgr.getCamera().unproject(_mouseSelectCoordinates);
        //_mouseSelectCoordinates.x /= com.smoftware.elmour.maps.Map.UNIT_SCALE;
        //_mouseSelectCoordinates.y /= com.smoftware.elmour.maps.Map.UNIT_SCALE;

        //Gdx.app.debug(TAG, "Mouse Coordinates " + "(" + _mouseSelectCoordinates.x + "," + _mouseSelectCoordinates.y + ")");

        for( Entity mapEntity : _tempEntities ) {
            //Don't break, reset all entities
            mapEntity.sendMessage(MESSAGE.ENTITY_DESELECTED);
            Rectangle mapEntityBoundingBox = mapEntity.getCurrentBoundingBox();
            //Gdx.app.debug(TAG, "Entity Candidate Location " + "(" + mapEntityBoundingBox.x + "," + mapEntityBoundingBox.y + ")");
            //if (mapEntity.getCurrentBoundingBox().contains(_mouseSelectCoordinates.x, _mouseSelectCoordinates.y)) {
                //Check distance
                _selectionRay.set(_boundingBox.x, _boundingBox.y, 0.0f, mapEntityBoundingBox.x, mapEntityBoundingBox.y, 0.0f);
                float distance =  _selectionRay.origin.dst(_selectionRay.direction);

                if( distance <= _selectRayMaximumDistance ){
                    //We have a valid entity selection
                    //Picked/Selected
                    Gdx.app.debug(TAG, "Selected Entity! " + mapEntity.getEntityConfig().getEntityID());
                    mapEntity.sendMessage(MESSAGE.ENTITY_SELECTED);
                    messageSent = true;
                    notify(_json.toJson(mapEntity.getEntityConfig()), ComponentObserver.ComponentEvent.LOAD_CONVERSATION);
                }
            //}
        }
        _tempEntities.clear();

        return messageSent;
    }
*/
    private boolean updateDiscoverLayerActivation(com.smoftware.elmour.maps.MapManager mapMgr){
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

    private boolean updateEnemySpawnLayerActivation(com.smoftware.elmour.maps.MapManager mapMgr){
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
                    mapName = mapName.toUpperCase();
                    if( mapName == null ) {
                        return false;
                    }

                    Gdx.app.debug(TAG, "loading map " + mapName);
                    //mapMgr.setClosestStartPositionFromScaledUnits(_currentEntityPosition);
                    mapMgr.loadMap(MapFactory.MapType.valueOf(mapName));
                    mapMgr.setStartPositionFromPreviousMap();

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
