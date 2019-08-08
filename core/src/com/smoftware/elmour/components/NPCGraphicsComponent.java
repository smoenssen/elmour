package com.smoftware.elmour.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.entities.Entity;
import com.smoftware.elmour.entities.EntityConfig;
import com.smoftware.elmour.maps.MapManager;

public class NPCGraphicsComponent extends GraphicsComponent {
    private static final String TAG = NPCGraphicsComponent.class.getSimpleName();

    private boolean _isSelected = false;
    private boolean _wasSelected = false;

    private boolean _sentShowConversationMessage = false;
    private boolean _sentHideCoversationMessage = false;

    private Entity.A_ButtonAction a_BtnStatus;
    private Entity.B_ButtonAction b_BtnStatus;
    private Entity.ButtonState a_BtnState = Entity.ButtonState.IS_UP;
    private Entity.ButtonState b_BtnState = Entity.ButtonState.IS_UP;

    public NPCGraphicsComponent(){
    }

    @Override
    public void receiveMessage(String message) {
        //Gdx.app.debug(TAG, "Got message " + message);
        String[] string = message.split(MESSAGE_TOKEN);

        if( string.length == 0 ) return;

        if( string.length == 1 ) {
            if (string[0].equalsIgnoreCase(MESSAGE.ENTITY_SELECTED.toString())) {
                if( _wasSelected ){
                    _isSelected = false;
                }else{
                    _isSelected = true;
                }
            }else if (string[0].equalsIgnoreCase(MESSAGE.ENTITY_DESELECTED.toString())) {
                _wasSelected = _isSelected;
                _isSelected = false;
            }
        }

        if( string.length == 2 ) {
            if (string[0].equalsIgnoreCase(MESSAGE.CURRENT_POSITION.toString())) {
                _currentPosition = json.fromJson(Vector2.class, string[1]);
            } else if (string[0].equalsIgnoreCase(MESSAGE.INIT_START_POSITION.toString())) {
                _currentPosition = json.fromJson(Vector2.class, string[1]);
            } else if (string[0].equalsIgnoreCase(MESSAGE.CURRENT_STATE.toString())) {
                currentState = json.fromJson(Entity.State.class, string[1]);
            } else if (string[0].equalsIgnoreCase(MESSAGE.CURRENT_DIRECTION.toString())) {
                _currentDirection = json.fromJson(Entity.Direction.class, string[1]);
            }else if (string[0].equalsIgnoreCase(MESSAGE.LOAD_ANIMATIONS.toString())) {
                EntityConfig entityConfig = json.fromJson(EntityConfig.class, string[1]);
                Array<EntityConfig.AnimationConfig> animationConfigs = entityConfig.getAnimationConfig();

                for( EntityConfig.AnimationConfig animationConfig : animationConfigs ){
                    Array<String> textureNames = animationConfig.getTexturePaths();
                    Array<GridPoint2> points = animationConfig.getGridPoints();
                    Entity.AnimationType animationType = animationConfig.getAnimationType();
                    float frameDuration = animationConfig.getFrameDuration();
                    Animation<TextureRegion> animation = null;

                    int frameHeight = animationConfig.getFrameHeight();
                    int frameWidth = animationConfig.getFrameWidth();

                    if( textureNames.size == 1) {
                        animation = loadAnimation(textureNames.get(0), points, frameDuration, animationConfig.getFrameWidth(), animationConfig.getFrameHeight());
                    }else if( textureNames.size == 2){
                        animation = loadAnimation(textureNames.get(0), textureNames.get(1), points, frameDuration);
                    }

                    animations.put(animationType, animation);
                }
            }
        }
    }

    @Override
    public void update(Entity entity, com.smoftware.elmour.maps.MapManager mapMgr, Batch batch, float delta){
        updateAnimations(delta);
/*
        if( _isSelected ){
            drawSelected(entity, mapMgr);
            mapMgr.setCurrentSelectedMapEntity(entity);
            if( _sentShowConversationMessage == false){
                notify(json.toJson(entity.getEntityConfig()), ComponentObserver.ComponentEvent.SHOW_CONVERSATION);
                _sentShowConversationMessage = true;
                _sentHideCoversationMessage = false;
            }
        }else{
            if( _sentHideCoversationMessage == false ){
                notify(json.toJson(entity.getEntityConfig()), ComponentObserver.ComponentEvent.HIDE_CONVERSATION);
                _sentHideCoversationMessage = true;
                _sentShowConversationMessage = false;
            }
        }
*/
        batch.begin();
        batch.draw(_currentFrame, _currentPosition.x, _currentPosition.y, 1, 1);
        batch.end();

        //Used to graphically debug boundingboxes
        /*
        Rectangle rect = entity.getCurrentBoundingBox();
        Camera camera = mapMgr.getCamera();
        _shapeRenderer.setProjectionMatrix(camera.combined);
        _shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        _shapeRenderer.setColor(Color.BLACK);
        _shapeRenderer.rect(rect.getX() * Map.UNIT_SCALE, rect.getY() * Map.UNIT_SCALE, rect.getWidth() * Map.UNIT_SCALE, rect.getHeight() * Map.UNIT_SCALE);
        _shapeRenderer.end();
        */
    }

    @Override
    public void updateShadow(Entity entity, MapManager mapManager, Batch batch, float delta, Vector2 entityPosition) {

    }

    private void drawSelected(Entity entity, MapManager mapMgr){
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Camera camera = mapMgr.getCamera();
        Rectangle rect = entity.getCurrentBoundingBox();
        _shapeRenderer.setProjectionMatrix(camera.combined);
        _shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        _shapeRenderer.setColor(0.0f, 1.0f, 1.0f, 0.5f);

        float width =  rect.getWidth() * com.smoftware.elmour.maps.Map.UNIT_SCALE*2f;
        float height = rect.getHeight() * com.smoftware.elmour.maps.Map.UNIT_SCALE/2f;
        float x = rect.x * com.smoftware.elmour.maps.Map.UNIT_SCALE - width/4;
        float y = rect.y * com.smoftware.elmour.maps.Map.UNIT_SCALE - height/2;

        _shapeRenderer.ellipse(x,y,width,height);
        _shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }


    @Override
    public void dispose(){
    }
}
