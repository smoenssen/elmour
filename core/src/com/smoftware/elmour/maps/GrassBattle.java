package com.smoftware.elmour.maps;

import com.badlogic.gdx.utils.Json;
import com.smoftware.elmour.entities.Entity;
import com.smoftware.elmour.audio.AudioObserver;

/**
 * Created by steve on 9/16/17.
 */

public class GrassBattle extends Map {
    private static final String TAG = GrassBattle.class.getSimpleName();

    public static String mapPath = "RPGGame/maps/Battle/Grass_Battle.tmx";
    private Json json;

    GrassBattle(){
        super(MapFactory.MapType.GRASS_BATTLE, mapPath);

        json = new Json();

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
    public void handleInteractionInit(Entity.Interaction interaction) {
        this.interaction = interaction;
    }

    @Override
    public void handleInteraction() {

    }

    @Override
    public void handleInteraction(MapManager mapMgr) {

    }

    @Override
    public void handleInteractionFinished() {
        interaction = Entity.Interaction.NONE;
    }

}
