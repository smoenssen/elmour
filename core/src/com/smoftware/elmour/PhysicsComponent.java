package com.smoftware.elmour;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.smoftware.elmour.maps.MapManager;

public abstract class PhysicsComponent extends ComponentSubject implements Component{
    private static final String TAG = PhysicsComponent.class.getSimpleName();

    public abstract void update(Entity entity, com.smoftware.elmour.maps.MapManager mapMgr, float delta);

    public Vector2 _nextEntityPosition;
    protected Vector2 _currentEntityPosition;
    protected Entity.Direction _currentDirection;
    protected Entity.Direction _lasttDirection;
    protected Vector2 currentJoystickPosition;
    protected Json _json;
    protected Vector2 _velocity;
    protected boolean isRunning;
    protected boolean isNPC;
    protected boolean isConversationInProgress;

    protected Array<Entity> _tempEntities;

    public Rectangle _boundingBox;
    protected BoundingBoxLocation _boundingBoxLocation;
    protected Ray _selectionRay;
    protected float selectionAngle;
    protected final float _selectRayMaximumDistance = 20.0f;

    public static enum BoundingBoxLocation{
        BOTTOM_LEFT,
        BOTTOM_CENTER,
        CENTER,
    }

    PhysicsComponent(){
        this._nextEntityPosition = new Vector2(0,0);
        this._currentEntityPosition = new Vector2(0,0);
        this.currentJoystickPosition = new Vector2(0,0);

        if (ElmourGame.isAndroid())
            this._velocity = new Vector2(0,0);
        else
            this._velocity = new Vector2(2f,2f);

        isRunning = false;
        isNPC = false;
        isConversationInProgress = false;

        this._boundingBox = new Rectangle();
        this._json = new Json();
        this._tempEntities = new Array<Entity>();
        _boundingBoxLocation = BoundingBoxLocation.BOTTOM_LEFT;
        _selectionRay = new Ray(new Vector3(), new Vector3());
        selectionAngle = 0;
    }

    public float getSelectionAngle() { return selectionAngle; }

    protected boolean isCollisionWithMapEntities(Entity entity, com.smoftware.elmour.maps.MapManager mapMgr){
        _tempEntities.clear();
        _tempEntities.addAll(mapMgr.getCurrentMapEntities());
        _tempEntities.addAll(mapMgr.getCurrentMapQuestEntities());
        boolean isCollisionWithMapEntities = false;

        for(Entity mapEntity: _tempEntities){
            //Check for testing against self
            if( mapEntity.equals(entity) ){
                continue;
            }

            Rectangle targetRect = mapEntity.getCurrentBoundingBox();
            if (_boundingBox.overlaps(targetRect) ){
                //Collision
                entity.sendMessage(MESSAGE.COLLISION_WITH_ENTITY);
                isCollisionWithMapEntities = true;
                break;
            }
        }
        _tempEntities.clear();
        return isCollisionWithMapEntities;
    }

    protected boolean isCollision(Entity entity, MapObject object) {
        if (object != null) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            return entity.getCurrentBoundingBox().overlaps(rectangle);
        }
        else {
            return false;
        }
    }

    protected boolean isCollision(Entity entitySource, Entity entityTarget){
        boolean isCollisionWithMapEntities = false;

        if( entitySource.equals(entityTarget) ){
            return false;
        }

        if (entitySource.getCurrentBoundingBox().overlaps(entityTarget.getCurrentBoundingBox()) ){
            //Collision
            entitySource.sendMessage(MESSAGE.COLLISION_WITH_ENTITY);
            isCollisionWithMapEntities = true;
        }

        return isCollisionWithMapEntities;
    }

    protected boolean isCollisionWithMapLayer(Entity entity, com.smoftware.elmour.maps.MapManager mapMgr){
        MapLayer mapCollisionLayer =  mapMgr.getCollisionLayer();

        if( mapCollisionLayer == null ){
            return false;
        }

        Rectangle rectangle = null;

        for( MapObject object: mapCollisionLayer.getObjects()){
            if(object instanceof RectangleMapObject) {
                rectangle = ((RectangleMapObject)object).getRectangle();
                if( _boundingBox.overlaps(rectangle) ){
                    //Collision
                    entity.sendMessage(MESSAGE.COLLISION_WITH_MAP);
                    return true;
                }
            }
        }

        // need to also check 0_OPACITY_LAYER if it is present or active
        MapLayer mapZeroOpacityLayer = mapMgr.getZeroOpacityLayer();

        if( mapZeroOpacityLayer == null ){
            return false;
        }

        for( MapObject object: mapZeroOpacityLayer.getObjects()){
            if(object instanceof RectangleMapObject) {
                rectangle = ((RectangleMapObject)object).getRectangle();
                if( _boundingBox.overlaps(rectangle) ){
                    //Collision
                    entity.sendMessage(MESSAGE.COLLISION_WITH_MAP);
                    return true;
                }
            }
        }

        return false;
    }

    protected MapObject checkCollisionWithInteractionLayer(MapManager mapMgr){
        MapLayer mapInteractionLayer =  mapMgr.getInteractionLayer();

        if( mapInteractionLayer == null ){
            return null;
        }

        Rectangle rectangle = null;

        for( MapObject object: mapInteractionLayer.getObjects()){
            if(object instanceof RectangleMapObject) {
                rectangle = ((RectangleMapObject)object).getRectangle();
                if( _boundingBox.overlaps(rectangle) ){
                    //Collision
                    //Gdx.app.debug(TAG, "object.getName() = " + object.getName());
                    return object;
                }
            }
        }

        return null;
    }

    protected MapObject isCollisionWithWaterObstacleLayer(MapManager mapMgr){
        MapLayer waterObstacleLayer =  mapMgr.getWaterObstacleLayer();

        if( waterObstacleLayer == null ){
            return null;
        }

        Rectangle rectangle = null;

        for( MapObject object: waterObstacleLayer.getObjects()){
            if(object instanceof RectangleMapObject) {
                rectangle = ((RectangleMapObject)object).getRectangle();
                if( _boundingBox.overlaps(rectangle) ){
                    //Collision
                    //Gdx.app.debug(TAG, "object.getName() = " + object.getName());
                    return object;
                }
            }
        }

        return null;
    }

    protected MapObject checkCollisionWithZGatesLayer(MapManager mapMgr){
        MapLayer zGatesLayer =  mapMgr.getZGatesLayer();

        if( zGatesLayer == null ){
            return null;
        }

        Rectangle rectangle = null;

        for (MapObject object: zGatesLayer.getObjects()){
            if(object instanceof RectangleMapObject) {
                rectangle = ((RectangleMapObject)object).getRectangle();
                if( _boundingBox.overlaps(rectangle) ){
                    return object;
                }
            }
        }

        return null;
    }

    protected void setNextPositionToCurrent(Entity entity){
        this._currentEntityPosition.x = _nextEntityPosition.x;
        this._currentEntityPosition.y = _nextEntityPosition.y;

        //Gdx.app.debug(TAG, "SETTING Current Position " + entity.getEntityConfig().getEntityID() + ": (" + _currentEntityPosition.x + "," + _currentEntityPosition.y + ")");
        entity.sendMessage(MESSAGE.CURRENT_POSITION, _json.toJson(_currentEntityPosition));
    }

    protected void calculateNextPosition(float deltaTime){
        if( _currentDirection == null ) return;

        //Gdx.app.log(TAG, String.format("deltaTime = %3.2f", deltaTime));

        if( deltaTime > .7) return;

        float testX = _currentEntityPosition.x;
        float testY = _currentEntityPosition.y;

        if (ElmourGame.isAndroid()) {
            float velocityFactor = 0.075f;
            if (isRunning)
                velocityFactor = 0.125f;

            if (!isNPC) {
                // velocity is directly proportional to joystick position
                _velocity = currentJoystickPosition;
                testX += _velocity.x * velocityFactor;
                testY += _velocity.y * velocityFactor;
            }
            else {
                Vector2 npcVelocity = new Vector2(0.75f, 0.75f);

                npcVelocity.scl(deltaTime);

                switch (_currentDirection) {
                    case LEFT:
                        testX -= npcVelocity.x;
                        break;
                    case RIGHT:
                        testX += npcVelocity.x;
                        break;
                    case UP:
                        testY += npcVelocity.y;
                        break;
                    case DOWN:
                        testY -= npcVelocity.y;
                        break;
                    default:
                        break;
                }
            }

            //Gdx.app.log(TAG, String.format("velocity factor = %3.2f", velocityFactor));
        }
        else {
            float velocityFactor = 2.0f;
            if (isRunning)
                velocityFactor = 8.0f; // super fast for desktop!
            else if (isNPC)
                velocityFactor = 1.0f;

            _velocity.scl(deltaTime);

            switch (_currentDirection) {
                case LEFT:
                    testX -= _velocity.x * velocityFactor;
                    break;
                case RIGHT:
                    testX += _velocity.x * velocityFactor;
                    break;
                case UP:
                    testY += _velocity.y * velocityFactor;
                    break;
                case DOWN:
                    testY -= _velocity.y * velocityFactor;
                    break;
                default:
                    break;
            }

            //velocity
            _velocity.scl(1 / deltaTime);
        }

        _nextEntityPosition.x = testX;
        _nextEntityPosition.y = testY;
    }

    protected void calculateNextVerticalPosition(float deltaTime){
        if( deltaTime > .7) return;

        float testX = _currentEntityPosition.x;
        float testY = _currentEntityPosition.y;

        // NOTE: this function is currently for Android only
        float velocityFactor = 0.075f;
        if (isRunning)
            velocityFactor = 0.125f;

        // velocity is directly proportional to joystick position
        _velocity = currentJoystickPosition;

       // move vertically
        testY += _velocity.y * velocityFactor;

        _nextEntityPosition.x = testX;
        _nextEntityPosition.y = testY;
    }

    protected void calculateNextHorizontalPosition(float deltaTime){
        if( deltaTime > .7) return;

        float testX = _currentEntityPosition.x;
        float testY = _currentEntityPosition.y;

        // NOTE: this function is currently for Android only
        float velocityFactor = 0.075f;
        if (isRunning)
            velocityFactor = 0.125f;

        // velocity is directly proportional to joystick position
        _velocity = currentJoystickPosition;

        // move horizontally
        testX += _velocity.x * velocityFactor;

        _nextEntityPosition.x = testX;
        _nextEntityPosition.y = testY;
    }

    protected void initBoundingBox(float percentageWidthReduced, float percentageHeightReduced){
        //Update the current bounding box
        float width;
        float height;

        float origWidth =  Entity.FRAME_WIDTH;
        float origHeight = Entity.FRAME_HEIGHT;

        float widthReductionAmount = 1.0f - percentageWidthReduced; //.8f for 20% (1 - .20)
        float heightReductionAmount = 1.0f - percentageHeightReduced; //.8f for 20% (1 - .20)

        if( widthReductionAmount > 0 && widthReductionAmount < 1){
            width = Entity.FRAME_WIDTH * widthReductionAmount;
        }else{
            width = Entity.FRAME_WIDTH;
        }

        if( heightReductionAmount > 0 && heightReductionAmount < 1){
            height = Entity.FRAME_HEIGHT * heightReductionAmount;
        }else{
            height = Entity.FRAME_HEIGHT;
        }

        if( width == 0 || height == 0){
            Gdx.app.debug(TAG, "Width and Height are 0!! " + width + ":" + height);
        }

        //Need to account for the unitscale, since the map coordinates will be in pixels
        float minX;
        float minY;

        if( com.smoftware.elmour.maps.Map.UNIT_SCALE > 0 ) {
            minX = _nextEntityPosition.x / com.smoftware.elmour.maps.Map.UNIT_SCALE;
            minY = _nextEntityPosition.y / com.smoftware.elmour.maps.Map.UNIT_SCALE;
        }else{
            minX = _nextEntityPosition.x;
            minY = _nextEntityPosition.y;
        }

        _boundingBox.setWidth(width);
        _boundingBox.setHeight(height);

        switch(_boundingBoxLocation){
            case BOTTOM_LEFT:
                _boundingBox.set(minX, minY, width, height);
                break;
            case BOTTOM_CENTER:
                _boundingBox.setCenter(minX + origWidth/2, minY + origHeight/4);
                break;
            case CENTER:
                _boundingBox.setCenter(minX + origWidth/2, minY + origHeight/2);
                break;
        }

        //Gdx.app.debug(TAG, "SETTING Bounding Box for " + entity.getEntityConfig().getEntityID() + ": (" + minX + "," + minY + ")  width: " + width + " height: " + height);
    }

    protected void updateBoundingBoxPosition(Vector2 position){
        //Need to account for the unitscale, since the map coordinates will be in pixels
        float minX;
        float minY;

        if( com.smoftware.elmour.maps.Map.UNIT_SCALE > 0 ) {
            minX = position.x / com.smoftware.elmour.maps.Map.UNIT_SCALE;
            minY = position.y / com.smoftware.elmour.maps.Map.UNIT_SCALE;
        }else{
            minX = position.x;
            minY = position.y;
        }

        switch(_boundingBoxLocation){
            case BOTTOM_LEFT:
                _boundingBox.set(minX, minY, _boundingBox.getWidth(), _boundingBox.getHeight());
                break;
            case BOTTOM_CENTER:
                _boundingBox.setCenter(minX + Entity.FRAME_WIDTH/2, minY + Entity.FRAME_HEIGHT/4);
                break;
            case CENTER:
                _boundingBox.setCenter(minX + Entity.FRAME_WIDTH/2, minY + Entity.FRAME_HEIGHT/2);
                break;
        }

        //Gdx.app.debug(TAG, "SETTING Bounding Box for " + entity.getEntityConfig().getEntityID() + ": (" + minX + "," + minY + ")  width: " + width + " height: " + height);
    }
}
