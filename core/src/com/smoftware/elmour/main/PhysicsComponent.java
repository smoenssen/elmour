package com.smoftware.elmour.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.smoftware.elmour.maps.MapFactory;
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
    protected Vector2 actualVelocityVector;
    protected float actualVelocity;
    protected boolean lastCollisionWasPolyline;
    private float polyLineCollisionAngle;
    private float polyLineCollisionSlope;
    private float polyLineVertices [];
    protected boolean isRunning;
    protected boolean isNPC;
    protected boolean isConversationInProgress;
    private boolean noClipping;

    protected Array<Entity> _tempEntities;

    public Rectangle _boundingBox;
    public Rectangle boundingBoxNextPosition;
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

        actualVelocityVector = new Vector2(0, 0);
        actualVelocity = 0;

        isRunning = false;
        isNPC = false;
        isConversationInProgress = false;
        noClipping = false;

        this._boundingBox = new Rectangle();
        this.boundingBoxNextPosition = new Rectangle();
        this._json = new Json();
        this._tempEntities = new Array<Entity>();
        _boundingBoxLocation = BoundingBoxLocation.BOTTOM_LEFT;
        _selectionRay = new Ray(new Vector3(), new Vector3());
        selectionAngle = 0;
    }

    public float getSelectionAngle() { return selectionAngle; }

    public float getActualVelocity() { return actualVelocity; }

    public void toggleNoClipping() { noClipping = !noClipping; }

    protected boolean isCollisionWithMapEntities(Entity entity, com.smoftware.elmour.maps.MapManager mapMgr){
        if (noClipping) {
            return false;
        }

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

    // Check if Polygon intersects Rectangle
    private boolean polygonOverlapsRectangle(Polygon p, Rectangle r) {
        Polygon rPoly = new Polygon(new float[] { 0, 0, r.width, 0, r.width,
                r.height, 0, r.height });
        rPoly.setPosition(r.x, r.y);
        if (Intersector.overlapConvexPolygons(rPoly, p))
            return true;
        return false;
    }

    // Check if PolyLine intersects Rectangle
    private boolean polyLineOverlapsRectangle(Polyline p, Rectangle r) {
        polyLineVertices  = p.getTransformedVertices();

        if (polyLineVertices.length != 4) {
            throw new IllegalArgumentException("Currently can't have more than 2 points in polyline!!");
        }

        // calculate angle and slope
        polyLineCollisionAngle = (float) Math.toDegrees(Math.atan2(polyLineVertices[3] - polyLineVertices[1], polyLineVertices[2] - polyLineVertices[0]));
        polyLineCollisionSlope = (polyLineVertices[3] - polyLineVertices[1]) / (polyLineVertices[2] - polyLineVertices[0]);
        /*
        if(angle < 0){
            angle += 360;
        }
        */

        Polygon rPoly = new Polygon(new float[] { 0, 0, r.width, 0, r.width,
                r.height, 0, r.height });
        Polygon plPoly = new Polygon(new float[] { polyLineVertices[0], polyLineVertices[1], polyLineVertices[2], polyLineVertices[3],
                polyLineVertices[2] + .01f, polyLineVertices[3] + .01f});   // hack to create a real skinny triangular polygon

        rPoly.setPosition(r.x, r.y);
        if (Intersector.overlapConvexPolygons(rPoly, plPoly))
            return true;
        return false;
    }

    protected boolean isCollisionWithMapLayer(Entity entity, com.smoftware.elmour.maps.MapManager mapMgr){
        if (noClipping) {
            return false;
        }

        MapLayer mapCollisionLayer =  mapMgr.getCollisionLayer();

        if( mapCollisionLayer == null ){
            return false;
        }

        lastCollisionWasPolyline = false;

        //Gdx.app.log(TAG, String.format("boundingBox: x = %3.2f, %3.2f, %3.2f, %3.2f", _boundingBox.x, _boundingBox.y,
        //        _boundingBox.x + _boundingBox.width, _boundingBox.y + _boundingBox.height));

        for( MapObject object: mapCollisionLayer.getObjects()){
            if(object instanceof RectangleMapObject) {
                Rectangle rectangle = ((RectangleMapObject)object).getRectangle();
                if( _boundingBox.overlaps(rectangle) ){
                    //Collision
                    entity.sendMessage(MESSAGE.COLLISION_WITH_MAP);
                    return true;
                }
            }
            else if (object instanceof  PolygonMapObject) {
                Polygon polygon = ((PolygonMapObject)object).getPolygon();
                if (polygonOverlapsRectangle(polygon, _boundingBox)) {
                    //Collision
                    entity.sendMessage(MESSAGE.COLLISION_WITH_MAP);
                    return true;
                }
            }
            else if (object instanceof PolylineMapObject) {
                Polyline polyLine = ((PolylineMapObject)object).getPolyline();
                if (polyLineOverlapsRectangle(polyLine, _boundingBox)) {
                    //Collision
                    //Gdx.app.log(TAG, "---------------COLLISION--------------");
                    entity.sendMessage(MESSAGE.COLLISION_WITH_MAP);
                    lastCollisionWasPolyline = true;
                    return true;
                }
            }
        }

        // if player is on ZDOWN layer then apply under bridge obstacles, otherwise apply regular bridge obstacles
        //Gdx.app.log(TAG, "Player Z layer = " + MapFactory.getMap(mapMgr.getCurrentMapType()).getPlayerZLayer());
        if (MapFactory.getMap(mapMgr.getCurrentMapType()).getPlayerZLayer().equals("ZDOWN")) {
            MapLayer underBridgeLayer = mapMgr.getUnderBridgeObstacleLayer();

            if( underBridgeLayer != null ) {
                for (MapObject object : underBridgeLayer.getObjects()) {
                    if (object instanceof RectangleMapObject) {
                        Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
                        if (_boundingBox.overlaps(rectangle)) {
                            //Collision
                            entity.sendMessage(MESSAGE.COLLISION_WITH_MAP);
                            return true;
                        }
                    }
                }
            }
        }
        else {
            MapLayer bridgeLayer = mapMgr.getBridgeObstacleLayer();

            if( bridgeLayer != null ) {
                for (MapObject object : bridgeLayer.getObjects()) {
                    if (object instanceof RectangleMapObject) {
                        Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
                        if (_boundingBox.overlaps(rectangle)) {
                            //Collision
                            entity.sendMessage(MESSAGE.COLLISION_WITH_MAP);
                            return true;
                        }
                    }
                }
            }
        }

        // need to also check 0_OPACITY_LAYER if it is present or active
        MapLayer mapZeroOpacityLayer = mapMgr.getZeroOpacityLayer();

        if( mapZeroOpacityLayer != null ) {
            for (MapObject object : mapZeroOpacityLayer.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
                    if (_boundingBox.overlaps(rectangle)) {
                        //Collision
                        entity.sendMessage(MESSAGE.COLLISION_WITH_MAP);
                        return true;
                    }
                }
            }
        }


        return false;
    }

    // NOTE: This public function is primarily to decide if the entity's shadow should be placed at the next position or not.
    //       No member variables are modified in this function.
    public boolean isNextPositionCollision(com.smoftware.elmour.maps.MapManager mapMgr) {
        MapLayer mapCollisionLayer =  mapMgr.getCollisionLayer();

        if( mapCollisionLayer == null ){
            return false;
        }

        updateBoundingBoxNextPosition();

        for( MapObject object: mapCollisionLayer.getObjects()){
            if(object instanceof RectangleMapObject) {
                if( boundingBoxNextPosition.overlaps(((RectangleMapObject)object).getRectangle()) ){
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
                if( boundingBoxNextPosition.overlaps(((RectangleMapObject)object).getRectangle()) ){
                    return true;
                }
            }
        }

        return false;
    }

    protected MapObject checkCollisionWithSpawnsLayerObject(MapManager mapMgr){
        MapLayer mapSpawnsLayer =  mapMgr.getSpawnsLayer();

        if( mapSpawnsLayer == null ){
            return null;
        }

        Rectangle rectangle = null;

        for( MapObject object: mapSpawnsLayer.getObjects()){
            if(object instanceof RectangleMapObject) {
                rectangle = ((RectangleMapObject)object).getRectangle();

                // Check distance from center points of rectangles
                float objectCenterX = rectangle.x + (rectangle.getWidth() / 2);
                float objectCenterY = rectangle.y + (rectangle.getHeight() / 2);
                float playerCenterX = _boundingBox.x + (_boundingBox.getWidth() / 2);
                float playerCenterY = _boundingBox.y + (_boundingBox.getHeight() / 2);
                _selectionRay.set(playerCenterX, playerCenterY, 0.0f, objectCenterX, objectCenterY, 0.0f);
                selectionAngle = (new Vector2(objectCenterX, objectCenterY)).sub(new Vector2(playerCenterX, playerCenterY)).angle();
                float distance =  _selectionRay.origin.dst(_selectionRay.direction);

                String name = object.getName();
                if (name != null && !name.contains("PLAYER_START") && !name.contains("NPC_START")) {
                    if (distance <= _selectRayMaximumDistance) {
                        // We have a valid selection
                        return object;
                    }
                }
            }
        }

        return null;
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

    protected float getAngleBetweenPlayerAndInteractionObject(MapManager mapMgr, MapObject interactionObject){
        MapLayer mapInteractionLayer =  mapMgr.getInteractionLayer();
        float angle = 0;

        if( mapInteractionLayer != null ) {
            for (MapObject object : mapInteractionLayer.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    if (object.equals(interactionObject)) {
                        Rectangle rectangle = ((RectangleMapObject) object).getRectangle();

                        // get angle from center points of player and object
                        float objectCenterX = rectangle.x + (rectangle.getWidth() / 2);
                        float objectCenterY = rectangle.y + (rectangle.getHeight() / 2);
                        float playerCenterX = _boundingBox.x + (_boundingBox.getWidth() / 2);
                        float playerCenterY = _boundingBox.y + (_boundingBox.getHeight() / 2);
                        angle = (new Vector2(objectCenterX, objectCenterY)).sub(new Vector2(playerCenterX, playerCenterY)).angle();
                        break;
                    }
                }
            }
        }

        return angle;
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

    protected Array<MapObject> checkCollisionWithZGatesLayers(MapManager mapMgr){
        Array<MapObject> objects = new Array<>();
        MapLayer zGatesLayer =  mapMgr.getZGatesLayer();

        if( zGatesLayer == null ){
            return null;
        }

        Rectangle rectangle = null;

        for (MapObject object: zGatesLayer.getObjects()){
            if(object instanceof RectangleMapObject) {
                rectangle = ((RectangleMapObject)object).getRectangle();
                if( _boundingBox.overlaps(rectangle) ){
                    objects.add(object);
                }
            }
        }

        return objects;
    }

    protected void setNextPositionToCurrent(Entity entity, float delta){
        // calculate actual velocity
        actualVelocityVector.x = (_nextEntityPosition.x - _currentEntityPosition.x)/delta;
        actualVelocityVector.y = (_nextEntityPosition.y - _currentEntityPosition.y)/delta;

        this._currentEntityPosition.x = _nextEntityPosition.x;
        this._currentEntityPosition.y = _nextEntityPosition.y;

        //Gdx.app.debug(TAG, "SETTING Current Position " + entity.getEntityConfig().getEntityID() + ": (" + _currentEntityPosition.x + "," + _currentEntityPosition.y + ")");
        entity.sendMessage(MESSAGE.CURRENT_POSITION, _json.toJson(_currentEntityPosition));
    }

    private float getVelocityFactor() {
        float velocityFactor;

        if (ElmourGame.isAndroid()) {
            // for Android, the velocityFactor is tiles per frame times the joystick position
            velocityFactor = 0.075f;
            if (isRunning)
                velocityFactor = 0.125f;
        }
        else {
            velocityFactor = 2.0f;
            if (isRunning)
                velocityFactor = 8.0f; // super fast for desktop!
            else if (isNPC)
                velocityFactor = 1.0f;
        }

        return velocityFactor;
    }

    protected void calculateNextPosition(float deltaTime){
        if( _currentDirection == null ) return;

        //Gdx.app.log(TAG, String.format("deltaTime = %3.3f", deltaTime));

        if( deltaTime > .7) return;

        float testX = _currentEntityPosition.x;
        float testY = _currentEntityPosition.y;

        if (ElmourGame.isAndroid()) {
            float fps = 1/deltaTime;
            float frameRateCompensation = 1;

            if (fps < 40) {
                // todo: compensate for slower frame rate?
                frameRateCompensation = 40/fps;
            }

            float velocityFactor = getVelocityFactor() * frameRateCompensation;

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
            float velocityFactor = getVelocityFactor();

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

    enum CollisionLineH { BOTTOM, TOP, NONE }
    enum CollisionLineV { LEFT, RIGHT, NONE }

    protected void calculateNextPositionParallelToLine(float deltaTime) {
        if( deltaTime > .7) return;

        CollisionLineH collisionLineH = CollisionLineH.NONE;
        CollisionLineV collisionLineV = CollisionLineV.NONE;
        Vector2 collisionPtH = new Vector2();
        Vector2 collisionPtV = new Vector2();

        // First, get the 2 intersecting points of character's bounding box with the polyline

        // Figure out if bottom or top of character is colliding.
        // slope of line is zero, so just find point along polyline based on y position
        // find equation of line: y - y1 = m(x - x1)
        //                    (y - y1)/m = x - x1
        //                             x = ((y - y1)/m) + x1

        float y = _boundingBox.y;
        float x = ((y - polyLineVertices[1])/polyLineCollisionSlope) + polyLineVertices[0];

        if (x >= _boundingBox.x && x <= _boundingBox.x + _boundingBox.width) {
            // bottom line intersects
            collisionLineH = CollisionLineH.BOTTOM;
        }
        else {
            y = _boundingBox.y + _boundingBox.height;
            x = ((y - polyLineVertices[1])/polyLineCollisionSlope) + polyLineVertices[0];
            if (x >= _boundingBox.x && x <= _boundingBox.x + _boundingBox.width) {
                // top line intersects
                collisionLineH = CollisionLineH.TOP;
            }
        }

        if (collisionLineH == CollisionLineH.NONE) {
            return;
            //throw new RuntimeException("Error calculating horizontal bounding box intersection with polyline");
        }
        else {
            // save horizontal intersection point
            collisionPtH.set(x, y);
        }

        // Now need to figure out if left or right of character is colliding.
        // slope of line is 1, so just find point along polyline based on x position
        // find equation of line: y = m(x - x1) + y1
        x = _boundingBox.x;
        y = (polyLineCollisionSlope * (x - polyLineVertices[0])) + polyLineVertices[1];

        if (y >= _boundingBox.y && y <= _boundingBox.y + _boundingBox.height) {
            // left line intersects
            collisionLineV = CollisionLineV.LEFT;
        }
        else {
            x = _boundingBox.x + _boundingBox.width;
            y = (polyLineCollisionSlope * (x - polyLineVertices[0])) + polyLineVertices[1];
            if (y >= _boundingBox.y && y <= _boundingBox.y + _boundingBox.height) {
                // right line intersects
                collisionLineV = CollisionLineV.RIGHT;
            }
        }

        if (collisionLineV == CollisionLineV.NONE) {
            //throw new RuntimeException("Error calculating vertical bounding box intersection with polyline");
            return;
        }
        else {
            // save vertical intersection point
            collisionPtV.set(x, y);
        }

        // Calculate 3rd point based on vertical and horizontal points
        Vector2 point3 = new Vector2(collisionPtV.x, collisionPtH.y);

        // Find equation of line through 3rd point and negative slope of polyline (perpendicular)
        // y = m(x - x1) + y1
        // Perpendicular Line:
        //      y = -1/polyLineCollisionSlope * (x - point3.x) + point3.y
        //      y = (-1/polyLineCollisionSlope * x) - (-1/polyLineCollisionSlope * point3.x) + point3.y
        // Polyline:
        //      y = polyLineCollisionSlope * (x - collisionPtH.x)) + collisionPtH.y
        //      y = (polyLineCollisionSlope * x) - (polyLineCollisionSlope * collisionPtH.x) + collisionPtH.y

        // Find intersecting point of polyline and perpendicular line
        // Set two equations equal to each other and solve for x coordinate of intersecting point:
        //      m = polyLineCollisionSlope
        //      (m * x) - (m * collisionPtH.x) + collisionPtH.y = (-1/m * x) - (-1/m * point3.x) + point3.y
        //      (m * x) - (-1/m * x) = - (-1/m * point3.x) + point3.y + (m * collisionPtH.x) - collisionPtH.y;
        //      x * (m -(-1/m)) = -(-1/m * point3.x) + point3.y + (m * collisionPtH.x) - collisionPtH.y
        //      x = (-(-1/m * point3.x) + point3.y + (m * collisionPtH.x) - collisionPtH.y) / (m -(-1/m))
        //
        float m = polyLineCollisionSlope;
        x = (-(-1/m * point3.x) + point3.y + (m * collisionPtH.x) - collisionPtH.y) / (m -(-1/m));

        // Solve for y coordinate of intersecting point (use either equation, they should be equal except for rounding)
        y = (m * x) - (m * collisionPtH.x) + collisionPtH.y;
       //y = (-1/m * x) - (-1/m * point3.x) + point3.y;

        //Gdx.app.log(TAG, String.format("intersection point: %3.2f, %3.2f", x, y));
        // Set next entity position
        float newX = 0;
        float newY = 0;
        float clearanceFactor = 0.15f;

        switch(_boundingBoxLocation){
            case BOTTOM_LEFT:
                break;
            case BOTTOM_CENTER:
                break;
            case CENTER:
                if (polyLineCollisionSlope > 0) {
                    // positive slope
                    switch (_currentDirection) {
                        case RIGHT:
                        case DOWN:
                            if (ElmourGame.isAndroid()) {
                                //****************
                                // ANDROID
                                //
                                newX = x - _boundingBox.width - clearanceFactor;
                                newY = y + clearanceFactor;
                            }
                            else {
                                //****************
                                // DESKTOP
                                //
                                // Bottom Right
                                if (collisionLineH == CollisionLineH.BOTTOM && collisionLineV == CollisionLineV.RIGHT) {
                                    newX = x - _boundingBox.width - clearanceFactor;
                                    newY = y + clearanceFactor;
                                }
                                // Top Left
                                else if (collisionLineH == CollisionLineH.TOP && collisionLineV == CollisionLineV.LEFT) {
                                    newX = x;
                                    newY = y;
                                }
                            }
                            break;
                        case LEFT:
                        case UP:
                            if (ElmourGame.isAndroid()) {
                                //****************
                                // ANDROID
                                //
                                newX = x + clearanceFactor;
                                newY = y - _boundingBox.height - clearanceFactor;
                            }
                            else {
                                //****************
                                // DESKTOP
                                //
                                // Bottom Right
                                if (collisionLineH == CollisionLineH.BOTTOM && collisionLineV == CollisionLineV.RIGHT) {
                                    newX = x;
                                    newY = y;
                                }
                                // Top Left
                                else if (collisionLineH == CollisionLineH.TOP && collisionLineV == CollisionLineV.LEFT) {
                                    newX = x + clearanceFactor;
                                    newY = y - _boundingBox.height - clearanceFactor;
                                }
                            }
                            break;
                    }
                }
                else {
                    // negative slope
                    switch (_currentDirection) {
                        case LEFT:
                        case DOWN:
                            if (ElmourGame.isAndroid()) {
                                //****************
                                // ANDROID
                                //
                                newX = x + clearanceFactor;
                                newY = y + clearanceFactor;
                            }
                            else {
                                //****************
                                // DESKTOP
                                //
                                // Bottom Left
                                if (collisionLineH == CollisionLineH.BOTTOM && collisionLineV == CollisionLineV.LEFT) {
                                    newX = x + clearanceFactor;
                                    newY = y + clearanceFactor;
                                }
                                // Top Right
                                else if (collisionLineH == CollisionLineH.TOP && collisionLineV == CollisionLineV.RIGHT) {
                                    newX = x;
                                    newY = y;
                                }
                            }
                            break;
                        case RIGHT:
                        case UP:
                            if (ElmourGame.isAndroid()) {
                                //****************
                                // ANDROID
                                //
                                newX = x - _boundingBox.width - clearanceFactor;
                                newY = y - _boundingBox.height - clearanceFactor;
                            }
                            else {
                                //****************
                                // DESKTOP
                                //
                                // Bottom Left
                                if (collisionLineH == CollisionLineH.BOTTOM && collisionLineV == CollisionLineV.LEFT) {
                                    newX = x;
                                    newY = y;
                                }
                                // Top Right
                                else if (collisionLineH == CollisionLineH.TOP && collisionLineV == CollisionLineV.RIGHT) {
                                    newX = x - _boundingBox.width - clearanceFactor;
                                    newY = y - _boundingBox.height - clearanceFactor;

                                }
                            }
                            break;
                    }
                }

                //Gdx.app.log(TAG, String.format("newX = %3.2f, newY = %3.2f", newX, newY));
                _nextEntityPosition.x = newX * com.smoftware.elmour.maps.Map.UNIT_SCALE;
                _nextEntityPosition.y = newY * com.smoftware.elmour.maps.Map.UNIT_SCALE;
                break;
        }
    }

    protected void calculateNextVerticalPosition(float deltaTime){
        if( deltaTime > .7) return;

        float testX = _currentEntityPosition.x;
        float testY = _currentEntityPosition.y;

        // NOTE: this function is currently for Android only
        float velocityFactor = getVelocityFactor();

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
        float velocityFactor = getVelocityFactor();

        // velocity is directly proportional to joystick position
        _velocity = currentJoystickPosition;

        // move horizontally
        testX += _velocity.x * velocityFactor;

        _nextEntityPosition.x = testX;
        _nextEntityPosition.y = testY;
    }

    protected void initBoundingBoxes(){
        //Update the current bounding box
        float width = 14;
        float height = 5;

        float origWidth =  Entity.FRAME_WIDTH;
        float origHeight = Entity.FRAME_HEIGHT;

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
        boundingBoxNextPosition.setWidth(width);
        boundingBoxNextPosition.setHeight(height);


        switch(_boundingBoxLocation){
            case BOTTOM_LEFT:
                _boundingBox.set(minX, minY, width, height);
                boundingBoxNextPosition.set(minX, minY, width, height);
                break;
            case BOTTOM_CENTER:
                _boundingBox.setCenter(minX + origWidth/2, minY + origHeight/4);
                boundingBoxNextPosition.setCenter(minX + origWidth/2, minY + origHeight/4);
                break;
            case CENTER:
                _boundingBox.setCenter(minX + origWidth/2, minY + origHeight/2);
                boundingBoxNextPosition.setCenter(minX + origWidth/2, minY + origHeight/2);
                break;
        }

        //Gdx.app.debug(TAG, "SETTING Bounding Box for " + entity.getEntityConfig().getEntityID() + ": (" + minX + "," + minY + ")  width: " + width + " height: " + height);
    }

    protected void initBoundingBoxes(float percentageWidthReduced, float percentageHeightReduced){
        //Update the current bounding box
        float width;
        float height;

        float origWidth =  Entity.FRAME_WIDTH;
        float origHeight = Entity.FRAME_HEIGHT;

        float widthReductionAmount = 0;//1.0f - percentageWidthReduced; //.8f for 20% (1 - .20)
        float heightReductionAmount = 0;//1.0f - percentageHeightReduced; //.8f for 20% (1 - .20)

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

        width = 14;
        height = 7;
        _boundingBox.setWidth(width);
        _boundingBox.setHeight(height);
        boundingBoxNextPosition.setWidth(width);
        boundingBoxNextPosition.setHeight(height);


        switch(_boundingBoxLocation){
            case BOTTOM_LEFT:
                _boundingBox.set(minX, minY, width, height);
                boundingBoxNextPosition.set(minX, minY, width, height);
                break;
            case BOTTOM_CENTER:
                _boundingBox.setCenter(minX + origWidth/2, minY + origHeight/4);
                boundingBoxNextPosition.setCenter(minX + origWidth/2, minY + origHeight/4);
                break;
            case CENTER:
                _boundingBox.setCenter(minX + origWidth/2, minY + origHeight/2);
                boundingBoxNextPosition.setCenter(minX + origWidth/2, minY + origHeight/2);
                break;
        }

        //Gdx.app.debug(TAG, "SETTING Bounding Box for " + entity.getEntityConfig().getEntityID() + ": (" + minX + "," + minY + ")  width: " + width + " height: " + height);
    }

    protected void updateBoundingBoxPosition(Vector2 position){
        //Need to account for the unitscale, since the map coordinates will be in pixels
        float minX;
        float minY;

        //Gdx.app.log(TAG, String.format("boundingBox before update: x = %3.2f, %3.2f, %3.2f, %3.2f", _boundingBox.x, _boundingBox.y,
        //        _boundingBox.x + _boundingBox.width, _boundingBox.y + _boundingBox.height));

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
                //_boundingBox.setCenter(minX + Entity.FRAME_WIDTH/2, minY + Entity.FRAME_HEIGHT/2);
                _boundingBox.setCenter(minX + _boundingBox.getWidth()/2, minY + _boundingBox.getHeight()/2);
                break;
        }

        //Gdx.app.log(TAG, String.format("minX =  %3.2f, minY = %3.2f", minX, minY));
        //Gdx.app.log(TAG, String.format("boundingBox after update:  x = %3.2f, %3.2f, %3.2f, %3.2f", _boundingBox.x, _boundingBox.y,
        //        _boundingBox.x + _boundingBox.width, _boundingBox.y + _boundingBox.height));

        //Gdx.app.debug(TAG, "SETTING Bounding Box for " + entity.getEntityConfig().getEntityID() + ": (" + minX + "," + minY + ")  width: " + width + " height: " + height);
    }

    protected void updateBoundingBoxNextPosition(){
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

        switch(_boundingBoxLocation){
            case BOTTOM_LEFT:
                boundingBoxNextPosition.set(minX, minY, _boundingBox.getWidth(), _boundingBox.getHeight());
                break;
            case BOTTOM_CENTER:
                boundingBoxNextPosition.setCenter(minX + Entity.FRAME_WIDTH/2, minY + Entity.FRAME_HEIGHT/4);
                break;
            case CENTER:
                boundingBoxNextPosition.setCenter(minX + Entity.FRAME_WIDTH/2, minY + Entity.FRAME_HEIGHT/2);
                break;
        }

        //Gdx.app.debug(TAG, "SETTING Bounding Box for " + entity.getEntityConfig().getEntityID() + ": (" + minX + "," + minY + ")  width: " + width + " height: " + height);
    }
}
