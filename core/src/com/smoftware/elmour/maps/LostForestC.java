package com.smoftware.elmour.maps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.smoftware.elmour.Component;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.EntityConfig;
import com.smoftware.elmour.audio.AudioObserver;
import com.smoftware.elmour.profile.ProfileManager;

/**
 * Created by steve on 9/16/17.
 */

public class LostForestC extends Map {
    private static final String TAG = LostForestC.class.getSimpleName();

    private static String mapPath = "RPGGame/maps/Caves/Lost_Forest_C.tmx";

    LostForestC(){
        super(MapFactory.MapType.LOST_FOREST_C, mapPath);

    }

    @Override
    public void unloadMusic() {
        notify(AudioObserver.AudioCommand.MUSIC_STOP, AudioObserver.AudioTypeEvent.MUSIC_TOWN);
    }

    @Override
    public void loadMusic() {
        notify(AudioObserver.AudioCommand.MUSIC_LOAD, AudioObserver.AudioTypeEvent.MUSIC_TOWN);
        notify(AudioObserver.AudioCommand.MUSIC_PLAY_LOOP, AudioObserver.AudioTypeEvent.MUSIC_TOWN);
    }

    @Override
    public void
    handleInteractionInit(Entity.Interaction interaction) {
        this.interaction = interaction;
    }

    @Override
    public void handleInteraction() {

    }

    @Override
    public void handleInteraction(MapManager mapMgr) {
        if (interaction == Entity.Interaction.Portal_Room) {
            Gdx.app.log(TAG, "GOT Portal_Room INTERACTION!");
            mapMgr.loadMap(MapFactory.MapType.PORTAL_ROOM);
        }
    }

    @Override
    public void handleInteractionFinished() {
        interaction = Entity.Interaction.NONE;
    }

}
