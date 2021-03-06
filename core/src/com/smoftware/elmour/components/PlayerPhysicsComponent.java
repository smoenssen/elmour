package com.smoftware.elmour.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.entities.Entity;
import com.smoftware.elmour.inventory.KeyItem;
import com.smoftware.elmour.inventory.KeyItemFactory;
import com.smoftware.elmour.main.ElmourGame;
import com.smoftware.elmour.main.Utility;
import com.smoftware.elmour.maps.MapFactory;
import com.smoftware.elmour.maps.MapManager;
import com.smoftware.elmour.maps.MapManagerObserver;
import com.smoftware.elmour.maps.MapObserver;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.profile.ProfileObserver;
import com.smoftware.elmour.screens.CutSceneManager;

public class PlayerPhysicsComponent extends PhysicsComponent implements MapManagerObserver {
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

    private boolean isHiddenItemButtonPressed = false;
    private boolean isLoadHiddenItemMsgSent = false;
    private boolean isShowHiddenItemMsgSent = false;
    private boolean isHiddenItemColliding = false;
    private KeyItem discoveredHiddenItem = null;

    private boolean isRegisteredWithMapManager = false;
    private boolean playerStartJustChanged = false;

    public PlayerPhysicsComponent(){
        //_boundingBoxLocation = BoundingBoxLocation.CENTER;
        //initBoundingBox(0.3f, 0f);
        _currentDirection = _lasttDirection = Entity.Direction.DOWN;

        a_BtnStatus = Entity.A_ButtonAction.RELEASED;
        b_BtnStatus = Entity.B_ButtonAction.RELEASED;

        //reduce width and height of bounding box for better feel of collisions
        _boundingBoxLocation = BoundingBoxLocation.CENTER;
        //initBoundingBoxes(0.4f, 0.05f);
        initBoundingBoxes();

        _previousDiscovery = "";
        _previousEnemySpawn = "0";

        _mouseSelectCoordinates = new Vector3(0,0,0);

        ProfileManager.getInstance().addObserver(this);
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

                        if (!isShowHiddenItemMsgSent) {
                            isHiddenItemButtonPressed = true;
                            // check for collision
                            if (isHiddenItemColliding) {
                                isShowHiddenItemMsgSent = true;
                                notify(_json.toJson(discoveredHiddenItem), ComponentObserver.ComponentEvent.HIDDEN_ITEM_DISCOVERED);
                                Gdx.app.log(TAG, "sending HIDDEN_ITEM_DISCOVERED");
                            }
                        }
                    }
                    else if (a_BtnStatus == Entity.A_ButtonAction.RELEASED) {
                        // button released so reset variables
                        isInteractButtonPressed = false;
                        isDidInteractiontMsgSent = false;
                        isConversationButtonPressed = false;
                        isShowConversationMsgSent = false;
                        isHiddenItemButtonPressed = false;
                        isShowHiddenItemMsgSent = false;
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
                    /*if (_velocity.y > 0 && currentJoystickPosition.angle() > 36 && currentJoystickPosition.angle() <= 144)
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
                    }*/
                    if (_velocity.y > 0 && currentJoystickPosition.angle() > 45 && currentJoystickPosition.angle() <= 135)
                        _currentDirection = Entity.Direction.UP;
                    else if (_velocity.x < 0 && currentJoystickPosition.angle() > 135 && currentJoystickPosition.angle() <= 225)
                        _currentDirection = Entity.Direction.LEFT;
                    else if (_velocity.y < 0 && currentJoystickPosition.angle() > 225 && currentJoystickPosition.angle() <= 315)
                        _currentDirection = Entity.Direction.DOWN;
                    else if (_velocity.x > 0 && (currentJoystickPosition.angle() > 315 || currentJoystickPosition.angle() <= 45) && currentJoystickPosition.angle() != 0)
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

                    if (!isShowHiddenItemMsgSent && a_BtnStatus == Entity.A_ButtonAction.PRESSED) {
                        isHiddenItemButtonPressed = true;

                        if (isHiddenItemColliding) {
                            isShowHiddenItemMsgSent = true;
                            notify(_json.toJson(discoveredHiddenItem), ComponentObserver.ComponentEvent.HIDDEN_ITEM_DISCOVERED);
                            Gdx.app.log(TAG, "sending HIDDEN_ITEM_DISCOVERED");
                        }
                    }

                    if (a_BtnStatus == Entity.A_ButtonAction.RELEASED) {
                        // button released so reset variables
                        isInteractButtonPressed = false;
                        isDidInteractiontMsgSent = false;
                        isConversationButtonPressed = false;
                        isShowConversationMsgSent = false;
                        isHiddenItemButtonPressed = false;
                        isShowHiddenItemMsgSent = false;
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

        if (!isRegisteredWithMapManager) {
            mapMgr.addObserver(this);
            isRegisteredWithMapManager = true;
        }

        if (playerStartJustChanged) {
            // don't update bounding box this frame otherwise player position from previous map will be used
            playerStartJustChanged = false;
        }
        else {
            updateBoundingBoxPosition(_nextEntityPosition);
        }

        updatePortalLayerActivation(mapMgr);
        updateQuestLayerActivation(mapMgr);
        updateEnemySpawnLayerActivation(mapMgr);
        updateCutsceneLayerActivation(mapMgr);

        // pass current state to graphics entity
        entity.sendMessage(MESSAGE.CURRENT_STATE, _json.toJson(_state));

        ///////////////////////////////////////////////////
        //
        // CHECK IF PASSING THROUGH A PLAYER Z_GATE OR SHADOW Z_GATE
        //
        Array<MapObject> objects = checkCollisionWithZGatesLayers(mapMgr);
        if (objects != null) {
            for (MapObject mapObject : objects) {
                if (mapObject.getName().contains("SHADOW"))
                    MapFactory.getMap(mapMgr.getCurrentMapType()).setShadowZLayer(mapObject.getName());
                else
                    MapFactory.getMap(mapMgr.getCurrentMapType()).setPlayerZLayer(mapObject.getName());
            }
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

                String taskIDs = "";
                object = checkCollisionWithSpawnsLayerObject(mapMgr);
                if (object != null) {
                    taskIDs = (String) object.getProperties().get("taskIDs");
                }

                Gdx.app.log(TAG, "sending LOAD_CONVERSATION");
                notify(taskIDs, ComponentObserver.ComponentEvent.LOAD_CONVERSATION);
                notify(npc, taskIDs, ComponentObserver.ComponentEvent.LOAD_CONVERSATION);

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
                if (!hasInteractionBeenDoneBefore(Entity.Interaction.valueOf(object.getName()))) {
                    Gdx.app.log(TAG, "sending INTERACTION_COLLISION for " + object.getName());
                    entity.sendMessage(MESSAGE.INTERACTION_COLLISION, _json.toJson(Entity.Interaction.valueOf(object.getName())));
                    isInteractionCollisionMsgSent = true;
                    isInteractionColliding = true;
                    selectionAngle = getAngleBetweenPlayerAndInteractionObject(mapMgr, object);
                }
                else {
                    isInteractionCollisionMsgSent = true;
                    Gdx.app.log(TAG, "INTERACTION_COLLISION for " + object.getName() + " has already been done once");
                }
            } else {
                isInteractionColliding = false;
            }
        }
        else if (isInteractionColliding) {
            String objectName = "";
            object = checkCollisionWithInteractionLayer(mapMgr);

            if (object != null) {
                objectName = object.getName();
            }

            // send message once no longer colliding or if map has changed
            if (object == null || objectName.startsWith(CutSceneManager.CUTSCENE_PREFIX) || mapMgr.hasMapChanged()) {
                isInteractionColliding = false;
                isInteractionCollisionMsgSent = false;
                entity.sendMessage(MESSAGE.INTERACTION_COLLISION, _json.toJson(Entity.Interaction.NONE));

                if (objectName.startsWith(CutSceneManager.CUTSCENE_PREFIX)) {
                    String cutsceneName = objectName.substring(CutSceneManager.CUTSCENE_PREFIX.length(), objectName.length());
                    notify(cutsceneName, ComponentObserver.ComponentEvent.CUTSCENE_ACTIVATED);
                }
            }
        }

        /////////////////////////////////////////
        //
        // HIDDEN ITEM HANDLING
        //
        if (isHiddenItemButtonPressed && !isLoadHiddenItemMsgSent) {
            // send message only once per button press
             object = checkCollisionWithHiddenItemsLayer(mapMgr);
            if (object != null) {
                KeyItem.ID id = KeyItem.ID.valueOf((String)object.getProperties().get("id"));
                discoveredHiddenItem = KeyItemFactory.getInstance().getKeyItem(id);
                discoveredHiddenItem.text = (String)object.getProperties().get("text");

                if (discoveredHiddenItem.category == KeyItem.Category.QUEST) {
                    discoveredHiddenItem.taskID = (String)object.getProperties().get("taskID");
                }

                isLoadHiddenItemMsgSent = true;
                isHiddenItemColliding = true;
                isHiddenItemBeingShown = true;
            }
            else {
                isHiddenItemColliding = false;
                isLoadHiddenItemMsgSent = false;
            }
        }
        else if (isHiddenItemColliding) {
            // send message once no longer colliding //todo?
            if (checkCollisionWithHiddenItemsLayer(mapMgr) == null) {
                isHiddenItemColliding = false;
                isLoadHiddenItemMsgSent = false;
            }
        }
        else {
            isLoadHiddenItemMsgSent = false;
        }

        /////////////////
        //
        // RESET
        //
        if (!isConversationButtonPressed) {
            isLoadConversationMsgSent = false;
        }

        if (!isInteractButtonPressed) {
            isInteractionCollisionMsgSent = false;
        }

        if (!isHiddenItemButtonPressed) {
            isLoadHiddenItemMsgSent = false;
        }

        ///////////////////////////////////////////////////
        //
        // OBSTACLE COLLISION HANDLING AND SETTING POSITION
        //
        if (!isCollisionWithMapLayer(entity, mapMgr) && !isCollisionWithMapEntities(entity, mapMgr) &&
            (_state == Entity.State.WALKING || _state == Entity.State.RUNNING)) {
            updatePosition(entity, mapMgr, delta);
        }
        else if (lastCollisionWasPolyline && (_state == Entity.State.WALKING || _state == Entity.State.RUNNING)) {
            // check if okay to move next position parallel to polyline
            // (prevents "sticking" to obstacle)
            calculateNextPositionParallelToLine(delta);
            updateBoundingBoxPosition(_nextEntityPosition);
            if (!isCollisionWithMapLayer(entity, mapMgr) && !isCollisionWithMapEntities(entity, mapMgr)) {
                updatePosition(entity, mapMgr, delta);
            }
        }
        else if (ElmourGame.isAndroid() && (_state == Entity.State.WALKING || _state == Entity.State.RUNNING)) {
            // check if okay to move next vertical or horizontal position based on joystick position
            // (prevents "sticking" to obstacle)
            if (lastCollisionWasPolyline) {
                calculateNextPositionParallelToLine(delta);
                updateBoundingBoxPosition(_nextEntityPosition);
                if (!isCollisionWithMapLayer(entity, mapMgr) && !isCollisionWithMapEntities(entity, mapMgr)) {
                    updatePosition(entity, mapMgr, delta);
                }
            }
            else {
                calculateNextVerticalPosition(delta);
                updateBoundingBoxPosition(_nextEntityPosition);
                if (!isCollisionWithMapLayer(entity, mapMgr) && !isCollisionWithMapEntities(entity, mapMgr)) {
                    updatePosition(entity, mapMgr, delta);
                } else {
                    calculateNextHorizontalPosition(delta);
                    updateBoundingBoxPosition(_nextEntityPosition);
                    if (!isCollisionWithMapLayer(entity, mapMgr) && !isCollisionWithMapEntities(entity, mapMgr)) {
                        updatePosition(entity, mapMgr, delta);
                    } else {
                        updateBoundingBoxPosition(_currentEntityPosition);
                    }
                }
            }
        }
        else {

            if (_state != Entity.State.WALKING && _state != Entity.State.RUNNING) {
                actualVelocityVector.scl(0);
            }

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

        actualVelocity = (float)Math.sqrt((actualVelocityVector.x * actualVelocityVector.x) + (actualVelocityVector.y * actualVelocityVector.y));

        //Gdx.app.log(TAG, String.format("velocity.x = %2.3f, velocity.y = %2.3f", actualVelocityVector.x, actualVelocityVector.y));
        //Gdx.app.log(TAG, String.format("actual velocity = %2.3f", actualVelocity));

        calculateNextPosition(delta);
    }

    private boolean hasInteractionBeenDoneBefore(Entity.Interaction interaction) {
        boolean isDone = false;

        // Check profile to see if this interaction has already been done.
        // This is for interactions that should only be done once.
        String value = ProfileManager.getInstance().getProperty(interaction.toString(), String.class);
        if (value != null) {
            isDone = true;
        }

        return isDone;
    }

    private void updatePosition(Entity entity, com.smoftware.elmour.maps.MapManager mapMgr, float delta) {
        // Don't allow movement if conversation is in progress
        // NOTE: this is just a fail safe: this function should never get called during a conversation
        // Also don't allow movement if a hidden item is being shown or if game is being saved
        if (!isConversationInProgress && !isHiddenItemBeingShown && !isGameSaving) {
            setNextPositionToCurrent(entity, delta);

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

    public static void setHiddenItemBeingShown(boolean beingShown) {
        isHiddenItemBeingShown = beingShown;
    }

    public static String getOverlappingQuestConversationConfig(MapManager mapMgr, Vector2 playerStart) {
        MapLayer mapQuestLayer =  mapMgr.getQuestDiscoverLayer();

        if( mapQuestLayer == null ){
            return null;
        }

        Rectangle boundingBox = new Rectangle(playerStart.x, playerStart.y, 14, 5);
        Rectangle rectangle = null;

        for( MapObject object: mapQuestLayer.getObjects()){
            if(object instanceof RectangleMapObject) {
                rectangle = ((RectangleMapObject)object).getRectangle();

                if (boundingBox.overlaps(rectangle) ){
                    String chapters = (String)object.getProperties().get("chapters");
                    if (!ProfileManager.getInstance().currentChapterInRange(chapters)) {
                        return null;
                    }

                    String questID = object.getName();
                    String questTaskID = (String)object.getProperties().get("taskID");
                    return (String)object.getProperties().get("conversationConfig");
                }
            }
        }
        return null;
    }

    private boolean updateQuestLayerActivation(MapManager mapMgr){
        MapLayer mapQuestLayer =  mapMgr.getQuestDiscoverLayer();

        if( mapQuestLayer == null ){
            return false;
        }

        Rectangle rectangle = null;

        for( MapObject object: mapQuestLayer.getObjects()){
            if(object instanceof RectangleMapObject) {
                rectangle = ((RectangleMapObject)object).getRectangle();

                if (_boundingBox.overlaps(rectangle) ){
                    String chapters = (String)object.getProperties().get("chapters");
                    if (!ProfileManager.getInstance().currentChapterInRange(chapters)) {
                        return false;
                    }

                    String questID = object.getName();
                    String questTaskID = (String)object.getProperties().get("taskID");
                    String conversationConfig = (String)object.getProperties().get("conversationConfig");
                    String val = questID + MESSAGE_TOKEN + questTaskID + MESSAGE_TOKEN;

                    if (conversationConfig != null) {
                        notify(conversationConfig, ComponentObserver.ComponentEvent.CONVERSATION_CONFIG);
                    }
                    else {
                        if (questID == null) {
                            return false;
                        }

                        if (_previousDiscovery.equalsIgnoreCase(val)) {
                            return true;
                        } else {
                            _previousDiscovery = val;
                        }

                        notify(_json.toJson(val), ComponentObserver.ComponentEvent.QUEST_LOCATION_DISCOVERED);
                    }

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
            //Gdx.app.debug(TAG, "Portal Layer doesn't exist!");
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

                    // check if there is a specific spawn position in the map name
                    String spawnPosition = null;
                    if (mapName.contains("+")) {
                        // get spawn position
                        spawnPosition = mapName.substring(mapName.indexOf("+") + 1);

                        // strip off spawn position for actual map name
                        mapName = mapName.substring(0, mapName.indexOf("+"));
                    }

                    Gdx.app.debug(TAG, "loading map " + mapName);
                    //mapMgr.setClosestStartPositionFromScaledUnits(_currentEntityPosition);

                    //todo: loading map calls setClosestStartPosition, so this could cause confusion
                    mapMgr.loadMap(MapFactory.MapType.valueOf(mapName));

                    if (spawnPosition != null) {
                        mapMgr.setStartPostionByNameExtension(spawnPosition);
                    }
                    else {
                        mapMgr.setStartPositionFromPreviousMap();
                    }

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

    // The cutscene layer is used to kick off cut scenes
    private boolean updateCutsceneLayerActivation(MapManager mapMgr){
        MapLayer mapCutsceneLayer =  mapMgr.getCutsceneLayer();

        if( mapCutsceneLayer == null ){
            //Gdx.app.debug(TAG, "Cutscene Layer doesn't exist!");
            return false;
        }

        Rectangle rectangle = null;

        for( MapObject object: mapCutsceneLayer.getObjects()){
            if(object instanceof RectangleMapObject) {
                rectangle = ((RectangleMapObject)object).getRectangle();

                if (_boundingBox.overlaps(rectangle) ){
                    String cutsceneName = object.getName();
                    if( cutsceneName == null ) {
                        return false;
                    }

                    notify(cutsceneName, ComponentObserver.ComponentEvent.CUTSCENE_ACTIVATED);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onNotify(ProfileManager profileManager, ProfileEvent event) {
        switch (event) {
            case SAVING_PROFILE:
                isGameSaving = true;
                break;
            case SAVED_PROFILE:
                isGameSaving = false;
                break;
        }
    }

    @Override
    public void onNotify(MapManagerEvent event, String value) {
        switch (event) {
            case PLAYER_START_CHANGED:
                Vector2 position = _json.fromJson(Vector2.class, value);
                updateBoundingBoxPosition(position);
                playerStartJustChanged = true;
                break;
        }
    }
}
