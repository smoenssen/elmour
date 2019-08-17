package com.smoftware.elmour.sfx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ParticleEffectActor extends Actor {
    ParticleEffect effect;

    public ParticleEffectActor(ParticleEffect effect) {
        this.effect = effect;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        effect.draw(batch); //define behavior when stage calls Actor.draw()
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        //effect.setPosition(x, y); //set to whatever x/y you prefer
        effect.update(delta); //update it
        //effect.start(); //need to start the particle spawning
    }

    public ParticleEffect getEffect() {
        return effect;
    }
}
