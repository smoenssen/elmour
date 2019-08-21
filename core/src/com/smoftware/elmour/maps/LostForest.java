package com.smoftware.elmour.maps;

import com.badlogic.gdx.Gdx;
import com.smoftware.elmour.entities.Entity;
import com.smoftware.elmour.audio.AudioObserver;

/**
 * Created by steve on 9/16/17.
 */

public class LostForest extends Map {
    private static final String TAG = LostForest.class.getSimpleName();

    public static String mapPath = "RPGGame/maps/Lost_Forest.tmx";

    LostForest(){
        super(MapFactory.MapType.LOST_FOREST, mapPath);

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
