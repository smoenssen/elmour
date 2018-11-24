package com.smoftware.elmour;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

import java.util.Hashtable;

public abstract class GraphicsComponent extends ComponentSubject implements Component {
    protected TextureRegion _currentFrame = null;
    protected TextureRegion currentShadowFrame = null;
    protected float _frameTime = 0f;
    public Entity.State currentState;
    protected Entity.Direction _currentDirection;
    protected Json json;
    protected Vector2 _currentPosition;
    protected Hashtable<Entity.AnimationType, Animation<TextureRegion>> animations;
    protected Hashtable<Entity.AnimationType, Animation<TextureRegion>> shadowAnimations;
    protected ShapeRenderer _shapeRenderer;
    protected Entity.Interaction currentInteraction;
    protected Entity.Interaction previousInteraction;
    protected boolean isConversationInProgress;

    protected GraphicsComponent(){
        _currentPosition = new Vector2(0,0);
        currentState = Entity.State.WALKING;
        _currentDirection = Entity.Direction.DOWN;
        currentInteraction = Entity.Interaction.NONE;
        previousInteraction = Entity.Interaction.NONE;
        json = new Json();
        animations = new Hashtable<>();
        shadowAnimations = new Hashtable<>();
        _shapeRenderer = new ShapeRenderer();
        isConversationInProgress = false;
    }

    public abstract void update(Entity entity, com.smoftware.elmour.maps.MapManager mapManager, Batch batch, float delta);

    public abstract void updateShadow(Entity entity, com.smoftware.elmour.maps.MapManager mapManager, Batch batch, float delta, Vector2 entityPosition);

    protected void updateAnimations(float delta){

        //Gdx.app.log("TAG", String.format("delta = %3.2f", delta));
        _frameTime = (_frameTime + delta)%5;

        if (_currentDirection == null)
            return;

        //Gdx.app.log("tag", String.format("Graphics: State = %s, Direction = %s", currentState.toString(), _currentDirection.toString()));

        if (isConversationInProgress) {
            // force idle state
            currentState = Entity.State.IDLE;
        }

        Animation<TextureRegion> shadowAnimation = null;

        //Gdx.app.log("TAG", "current direction = " + _currentDirection.toString());
        //Look into the appropriate variable when changing position
        switch (_currentDirection) {
            case DOWN:
                if (currentState == Entity.State.WALKING) {
                    Animation<TextureRegion> animation = animations.get(Entity.AnimationType.WALK_DOWN);
                    if (animation == null) return;
                    _currentFrame = animation.getKeyFrame(_frameTime);
                } else if (currentState == Entity.State.RUNNING) {
                    Animation<TextureRegion> animation = animations.get(Entity.AnimationType.RUN_DOWN);
                    if (animation == null) return;
                    _currentFrame = animation.getKeyFrame(_frameTime);
                } else if (currentState == Entity.State.IDLE) {
                    Animation<TextureRegion> animation = animations.get(Entity.AnimationType.WALK_DOWN);
                    if (animation == null) return;
                    _currentFrame = animation.getKeyFrames()[0];
                } else if (currentState == Entity.State.IMMOBILE) {
                    Animation<TextureRegion> animation = animations.get(Entity.AnimationType.IMMOBILE);
                    if (animation == null) return;
                    _currentFrame = animation.getKeyFrame(_frameTime);
                }

                if (shadowAnimations == null) return;
                shadowAnimation = shadowAnimations.get(Entity.AnimationType.WALK_DOWN);
                if (shadowAnimation == null) return;
                currentShadowFrame = shadowAnimation.getKeyFrame(0);
                break;
            case LEFT:
                if (currentState == Entity.State.WALKING) {
                    Animation<TextureRegion> animation = animations.get(Entity.AnimationType.WALK_LEFT);
                    if (animation == null) return;
                    _currentFrame = animation.getKeyFrame(_frameTime);
                } else if (currentState == Entity.State.RUNNING) {
                    Animation<TextureRegion> animation = animations.get(Entity.AnimationType.RUN_LEFT);
                    if (animation == null) return;
                    _currentFrame = animation.getKeyFrame(_frameTime);
                } else if (currentState == Entity.State.IDLE) {
                    Animation<TextureRegion> animation = animations.get(Entity.AnimationType.WALK_LEFT);
                    if (animation == null) return;
                    _currentFrame = animation.getKeyFrames()[0];
                } else if (currentState == Entity.State.IMMOBILE) {
                    Animation<TextureRegion> animation = animations.get(Entity.AnimationType.IMMOBILE);
                    if (animation == null) return;
                    _currentFrame = animation.getKeyFrame(_frameTime);
                }

                if (shadowAnimations == null) return;
                shadowAnimation = shadowAnimations.get(Entity.AnimationType.WALK_LEFT);
                currentShadowFrame = shadowAnimation.getKeyFrame(_frameTime);
                break;
            case UP:
                if (currentState == Entity.State.WALKING) {
                    Animation<TextureRegion> animation = animations.get(Entity.AnimationType.WALK_UP);
                    if (animation == null) return;
                    _currentFrame = animation.getKeyFrame(_frameTime);
                } else if (currentState == Entity.State.RUNNING) {
                    Animation<TextureRegion> animation = animations.get(Entity.AnimationType.RUN_UP);
                    if (animation == null) return;
                    _currentFrame = animation.getKeyFrame(_frameTime);
                } else if (currentState == Entity.State.IDLE) {
                    Animation<TextureRegion> animation = animations.get(Entity.AnimationType.WALK_UP);
                    if (animation == null) return;
                    _currentFrame = animation.getKeyFrames()[0];
                } else if (currentState == Entity.State.IMMOBILE) {
                    Animation<TextureRegion> animation = animations.get(Entity.AnimationType.IMMOBILE);
                    if (animation == null) return;
                    _currentFrame = animation.getKeyFrame(_frameTime);
                }

                if (shadowAnimations == null) return;
                shadowAnimation = shadowAnimations.get(Entity.AnimationType.WALK_UP);
                currentShadowFrame = shadowAnimation.getKeyFrame(_frameTime);
                break;
            case RIGHT:
                if (currentState == Entity.State.WALKING) {
                    Animation<TextureRegion> animation = animations.get(Entity.AnimationType.WALK_RIGHT);
                    if (animation == null) return;
                    _currentFrame = animation.getKeyFrame(_frameTime);
                } else if (currentState == Entity.State.RUNNING) {
                    Animation<TextureRegion> animation = animations.get(Entity.AnimationType.RUN_RIGHT);
                    if (animation == null) return;
                    _currentFrame = animation.getKeyFrame(_frameTime);
                } else if (currentState == Entity.State.IDLE) {
                    Animation<TextureRegion> animation = animations.get(Entity.AnimationType.WALK_RIGHT);
                    if (animation == null) return;
                    _currentFrame = animation.getKeyFrames()[0];
                } else if (currentState == Entity.State.IMMOBILE) {
                    Animation<TextureRegion> animation = animations.get(Entity.AnimationType.IMMOBILE);
                    if (animation == null) return;
                    _currentFrame = animation.getKeyFrame(_frameTime);
                }

                if (shadowAnimations == null) return;
                shadowAnimation = shadowAnimations.get(Entity.AnimationType.WALK_RIGHT);
                currentShadowFrame = shadowAnimation.getKeyFrame(_frameTime);
                break;
            default:
                break;
        }
    }

    //Specific to two frame animations where each frame is stored in a separate texture
    protected Animation loadAnimation(String firstTexture, String secondTexture, Array<GridPoint2> points, float frameDuration){
        Utility.loadTextureAsset(firstTexture);
        Texture texture1 = Utility.getTextureAsset(firstTexture);

        Utility.loadTextureAsset(secondTexture);
        Texture texture2 = Utility.getTextureAsset(secondTexture);

        TextureRegion[][] texture1Frames = TextureRegion.split(texture1, Entity.FRAME_WIDTH, Entity.FRAME_HEIGHT);
        TextureRegion[][] texture2Frames = TextureRegion.split(texture2, Entity.FRAME_WIDTH, Entity.FRAME_HEIGHT);

        GridPoint2 point = points.first();

		Animation animation = new Animation(frameDuration, texture1Frames[point.x][point.y],texture2Frames[point.x][point.y]);
		animation.setPlayMode(Animation.PlayMode.LOOP);

        return animation;
    }

    protected Animation loadAnimation(String textureName, Array<GridPoint2> points, float frameDuration){
        Utility.loadTextureAsset(textureName);
        Texture texture = Utility.getTextureAsset(textureName);

        //Gdx.app.log("GP", "textureName = " + textureName);

        TextureRegion[][] textureFrames = TextureRegion.split(texture, Entity.FRAME_WIDTH, Entity.FRAME_HEIGHT);

        TextureRegion[] animationKeyFrames = new TextureRegion[points.size];

        for(int i=0; i < points.size; i++){
			animationKeyFrames[i] = textureFrames[points.get(i).x][points.get(i).y];
        }

        Animation animation = new Animation(frameDuration, (Object[])animationKeyFrames);
		animation.setPlayMode(Animation.PlayMode.LOOP);

        return animation;
    }

    protected static Animation loadAnimation(String textureName, Array<GridPoint2> points, float frameDuration, int frameWidth, int frameHeight){
        Utility.loadTextureAsset(textureName);
        Texture texture = Utility.getTextureAsset(textureName);

        TextureRegion[][] textureFrames = TextureRegion.split(texture, frameWidth, frameHeight);

        TextureRegion[] animationKeyFrames = new TextureRegion[points.size];

        for(int i=0; i < points.size; i++){
            animationKeyFrames[i] = textureFrames[points.get(i).x][points.get(i).y];
        }

        Animation animation = new Animation(frameDuration, (Object[])animationKeyFrames);
        animation.setPlayMode(Animation.PlayMode.NORMAL);

        return animation;
    }

    // not ready for use yet
    protected static Animation loadIrregularTileAnimation(String textureName, Array<GridPoint2> points, float frameDuration, int frameWidth, int frameHeight){
        Utility.loadTextureAsset(textureName);
        Texture texture = Utility.getTextureAsset(textureName);

        TextureRegion[] animationKeyFrames = new TextureRegion[points.size];

        for(int i=0; i < points.size; i++){
            // point values need work if this is going to be used
            // see https://stackoverflow.com/questions/47026187/how-to-take-a-section-of-an-image-png-and-store-it-as-a-texture-in-libgdx
            animationKeyFrames[i] = new TextureRegion(texture, points.get(i).x, points.get(i).y, frameWidth, frameHeight);
        }

        Animation animation = new Animation(frameDuration, (Object[])animationKeyFrames);
        animation.setPlayMode(Animation.PlayMode.NORMAL);

        return animation;
    }

    public static Hashtable<Entity.AnimationType, Animation<TextureRegion>> loadAnimationsByName(EntityFactory.EntityName entityName) {
        Hashtable<Entity.AnimationType, Animation<TextureRegion>> animations = new Hashtable<>();
        Entity entity = EntityFactory.getInstance().getEntityByNameNoLoadAnimations(entityName);
        Array<EntityConfig.AnimationConfig> animationConfigs = entity.getEntityConfig().getAnimationConfig();

        for( EntityConfig.AnimationConfig animationConfig : animationConfigs ){
            Array<String> textureNames = animationConfig.getTexturePaths();
            Array<GridPoint2> points = animationConfig.getGridPoints();
            Entity.AnimationType animationType = animationConfig.getAnimationType();
            float frameDuration = animationConfig.getFrameDuration();

            Animation<TextureRegion> animation = GraphicsComponent.loadAnimation(textureNames.get(0), points, frameDuration,
                                                animationConfig.getFrameWidth(), animationConfig.getFrameHeight());

            animations.put(animationType, animation);
        }

        return animations;
    }

    public Animation<TextureRegion> getAnimation(Entity.AnimationType type){
        return animations.get(type);
    }

    public Animation<TextureRegion> getShadowAnimation(Entity.AnimationType type){
        return shadowAnimations.get(type);
    }
}
