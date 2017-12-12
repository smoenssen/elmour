package com.smoftware.elmour;

import com.badlogic.gdx.Gdx;
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
import com.smoftware.elmour.maps.MapManager;

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
                Gdx.app.log(TAG, "received INTERACTION_COLLISION " + currentInteraction.toString());
                if (currentInteraction != Entity.Interaction.NONE) {
                    //srm multiple signs todo check previous interaction?
                    receivedInteractionCollision = true;
                }
                else {
                    receivedInteractionCollision = false;
                    sentPopupInitializeMessage = false;
                    sentHidePopupMessage = false;
                }
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
            //Gdx.app.log(TAG, "received interactive collision");

            if (sentPopupInitializeMessage == false) {
                //this message is only sent the first time the interaction button is pressed.
                //subsequent button presses (and actually including the first press) will send DID_INTERACTION.
                //this is used for any initialization that has to be done just the first time, like setting the interaction type.
                Gdx.app.log(TAG, "sending DID_INITIAL_INTERACTION");
                notify(json.toJson(currentInteraction.toString()), ComponentObserver.ComponentEvent.DID_INITIAL_INTERACTION);
                sentPopupInitializeMessage = true;
                sentHidePopupMessage = false;
            }

            previousInteraction = currentInteraction;
        }
        else {
            if (sentHidePopupMessage == false ){
                notify("", ComponentObserver.ComponentEvent.FINISHED_INTERACTION);
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
        //Gdx.app.log(TAG, String.format("_currentPosition.x = %3.2f, _currentPosition.y = %3.2f", _currentPosition.x, _currentPosition.y));
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
