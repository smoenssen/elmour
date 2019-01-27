package com.smoftware.elmour.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Scaling;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.EntityConfig;

public class AnimatedImage extends Image {
    private static final String TAG = AnimatedImage.class.getSimpleName();
    private float _frameTime = 0;
    private boolean isLooping = true;
    protected Entity _entity;
    private Entity.AnimationType _currentAnimationType = Entity.AnimationType.IDLE;
    Entity.Direction currentDirection = Entity.Direction.DOWN;
    protected Json json;

    public AnimatedImage(){
        super();
        json = new Json();
    }

    public void setEntity(Entity entity){
        this._entity = entity;

        if (entity != null) {
            //set default
            setCurrentAnimation(Entity.AnimationType.IDLE);
        }
    }

    public Entity getEntity() {
        return _entity;
    }

    public void setCurrentDirection(Entity.Direction direction) {
        currentDirection = direction;
        //_entity.sendMessage(Component.MESSAGE.CURRENT_DIRECTION, json.toJson(direction));
    }

    public void setCurrentAnimation(Entity.AnimationType animationType){
        Animation<TextureRegion> animation = _entity.getAnimation(animationType);
        if( animation == null ){
            Gdx.app.debug(TAG, "Animation type " + animationType.toString() + " does not exist!");
            return;
        }

        this._currentAnimationType = animationType;
        this.setDrawable(new TextureRegionDrawable(animation.getKeyFrame(0)));
        this.setScaling(Scaling.stretch);
        this.setAlign(Align.center);
        this.setSize(this.getPrefWidth(), this.getPrefHeight());
    }

    public void setCurrentAnimationType(Entity.AnimationType animationType){
        if (_entity != null) {
            Animation<TextureRegion> animation = _entity.getAnimation(animationType);
            if (animation == null) {
                Gdx.app.debug(TAG, "Animation type " + animationType.toString() + " does not exist!");
                return;
            }

            // reset frame time so animation starts from first frame
            _frameTime = 0;

            // check if this animation is looping or not
            Array<EntityConfig.AnimationConfig> animationConfigs = _entity.getEntityConfig().getAnimationConfig();

            Gdx.app.debug(TAG, "Animation type " + animationType.toString());
            if (animationType.equals(Entity.AnimationType.THINK)) {
                int x;
                x=0;
            }

            for( EntityConfig.AnimationConfig animationConfig : animationConfigs ){
                if (animationConfig.getAnimationType().equals(animationType)) {
                    isLooping = animationConfig.getLooping();
                    Gdx.app.log(TAG, "setting isLooping to " + isLooping + " for animation type " + animationType.toString());
                    break;
                }
            }

            this._currentAnimationType = animationType;
        }
    }

    @Override
    public void act(float delta){
        Drawable drawable = this.getDrawable();
        if( drawable == null ) {
            //Gdx.app.debug(TAG, "Drawable is NULL!");
            return;
        }

        if (!isLooping) {
            _frameTime = (_frameTime + delta);
        }
        else {
            _frameTime = (_frameTime + delta) % 5;
        }

        if (_entity == null || _currentAnimationType == null)
            return;

        _entity.getAnimation(_currentAnimationType).setPlayMode(Animation.PlayMode.NORMAL);

        if (_currentAnimationType != Entity.AnimationType.IDLE) {
            if (_entity != null) {
                TextureRegion region = _entity.getAnimation(_currentAnimationType).getKeyFrame(_frameTime, isLooping);
                ((TextureRegionDrawable) drawable).setRegion(region);
            }
        }
        else {
            if (_entity != null) {
                switch (currentDirection) {
                    case UP:
                        ((TextureRegionDrawable) drawable).setRegion(_entity.getAnimation(Entity.AnimationType.WALK_UP).getKeyFrame(0, false));
                        break;
                    case DOWN:
                        ((TextureRegionDrawable) drawable).setRegion(_entity.getAnimation(Entity.AnimationType.WALK_DOWN).getKeyFrame(0, false));
                        break;
                    case LEFT:
                        ((TextureRegionDrawable) drawable).setRegion(_entity.getAnimation(Entity.AnimationType.WALK_LEFT).getKeyFrame(0, false));
                        break;
                    case RIGHT:
                        ((TextureRegionDrawable) drawable).setRegion(_entity.getAnimation(Entity.AnimationType.WALK_RIGHT).getKeyFrame(0, false));
                        break;
                }
            }
        }

        super.act(delta);
    }




}
