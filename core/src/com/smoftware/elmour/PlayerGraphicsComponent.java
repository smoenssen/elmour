package com.smoftware.elmour;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.EntityConfig.AnimationConfig;

public class PlayerGraphicsComponent extends GraphicsComponent {

    private static final String TAG = PlayerGraphicsComponent.class.getSimpleName();

    protected Vector2 previousPosition;
    protected boolean receivedInteractionCollision = false;
    protected boolean sentPopupInitializeMessage = false;
    protected boolean sentHidePopupMessage = false;

    public PlayerGraphicsComponent(){
        previousPosition = new Vector2(0,0);
    }

    @Override
    public void receiveMessage(String message) {
        //Gdx.app.log(TAG, "Got message " + message);
        String[] string = message.split(MESSAGE_TOKEN);

        if( string.length == 0 ) return;

        //Specifically for messages with 1 object payload
        if( string.length == 2 ) {
            if (string[0].equalsIgnoreCase(MESSAGE.CURRENT_POSITION.toString())) {
                _currentPosition = json.fromJson(Vector2.class, string[1]);
            }
            else if (string[0].equalsIgnoreCase(MESSAGE.INIT_START_POSITION.toString())) {
                _currentPosition = json.fromJson(Vector2.class, string[1]);
            }
            else if (string[0].equalsIgnoreCase(MESSAGE.CURRENT_STATE.toString())) {
                currentState = json.fromJson(Entity.State.class, string[1]);
                if (currentState != Entity.State.IDLE)
                    notify("", ComponentObserver.ComponentEvent.POPUP_HIDE);
            }
            else if (string[0].equalsIgnoreCase(MESSAGE.CURRENT_DIRECTION.toString())) {
                _currentDirection = json.fromJson(Entity.Direction.class, string[1]);
            }
            else if (string[0].equalsIgnoreCase(MESSAGE.LOAD_ANIMATIONS.toString())) {
                EntityConfig entityConfig = json.fromJson(EntityConfig.class, string[1]);
                Array<AnimationConfig> animationConfigs = entityConfig.getAnimationConfig();

                for( AnimationConfig animationConfig : animationConfigs ){
                    Array<String> textureNames = animationConfig.getTexturePaths();
                    Array<GridPoint2> points = animationConfig.getGridPoints();
                    Entity.AnimationType animationType = animationConfig.getAnimationType();
                    float frameDuration = animationConfig.getFrameDuration();
                    Animation<TextureRegion> animation = null;

                    if( textureNames.size == 1) {
                        animation = loadAnimation(textureNames.get(0), points, frameDuration);
                    }else if( textureNames.size == 2){
                        animation = loadAnimation(textureNames.get(0), textureNames.get(1), points, frameDuration);
                    }

                    animations.put(animationType, animation);
                }
            }

            if (string[0].equalsIgnoreCase(MESSAGE.INTERACTION_COLLISION.toString())) {
                currentInteraction = json.fromJson(Entity.Interaction.class, string[1]);

                if (currentInteraction != Entity.Interaction.NONE)
                    receivedInteractionCollision = true;
            }
        }
    }

    @Override
    public void update(Entity entity, MapManager mapMgr, Batch batch, float delta){
        updateAnimations(delta);

        //Player has moved
        if( previousPosition.x != _currentPosition.x ||
                previousPosition.y != _currentPosition.y){
            notify("", ComponentObserver.ComponentEvent.PLAYER_HAS_MOVED);
            previousPosition = _currentPosition.cpy();
        }

        // make sure these notifications are only sent once
        if (receivedInteractionCollision) {
            if (sentPopupInitializeMessage == false) {
                notify(json.toJson(currentInteraction.toString()), ComponentObserver.ComponentEvent.POPUP_INITITIALIZE);
                sentPopupInitializeMessage = true;
                sentHidePopupMessage = false;
            }
        }
        else {
            if (sentHidePopupMessage == false ){
                notify("", ComponentObserver.ComponentEvent.POPUP_HIDE);
                sentHidePopupMessage = true;
                sentPopupInitializeMessage = false;
            }
        }

        TiledMap map = mapMgr.getCurrentTiledMap();
        MapProperties prop = map.getProperties();
        int mapWidthInTiles = prop.get("width", Integer.class);
        int mapHeightInTiles = prop.get("height", Integer.class);

        Camera camera = mapMgr.getCamera();

        //keep camera within map
        camera.position.set(MathUtils.clamp(_currentPosition.x, camera.viewportWidth / 2f, mapWidthInTiles - (camera.viewportWidth  / 2f)),
                            MathUtils.clamp(_currentPosition.y, camera.viewportHeight / 2f, mapHeightInTiles - (camera.viewportHeight / 2f)), 0f);

        camera.update();

        batch.begin();
        if (_currentFrame != null && _currentPosition != null)
            batch.draw(_currentFrame, _currentPosition.x, _currentPosition.y, 1, 1);//srm
        batch.end();

        //Used to graphically debug boundingboxes
/*
        Rectangle rect = entity.getCurrentBoundingBox();
        _shapeRenderer.setProjectionMatrix(camera.combined);
        _shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        _shapeRenderer.setColor(Color.RED);
        _shapeRenderer.rect(rect.getX() * Map.UNIT_SCALE , rect.getY() * Map.UNIT_SCALE, rect.getWidth() * Map.UNIT_SCALE, rect.getHeight()*Map.UNIT_SCALE);
        _shapeRenderer.end();
*/
    }

    @Override
    public void dispose(){
    }

}
