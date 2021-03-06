package com.smoftware.elmour.maps;

import com.badlogic.gdx.utils.Json;
import com.smoftware.elmour.entities.Entity;
import com.smoftware.elmour.audio.AudioObserver;

/**
 * Created by steve on 9/16/17.
 */

public class Map3 extends Map {

    private static final String TAG = Map3.class.getSimpleName();

    public static String mapPath = "RPGGame/maps/Map_3.tmx";
    private Json json;

    Map3(){
        super(MapFactory.MapType.MAP3, mapPath);

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
        switch(interaction) {
            case M3DOOR1:
                mapMgr.loadMap(MapFactory.MapType.M3DOOR1);
                mapMgr.setStartPostionByNameExtension("1");
                interaction = Entity.Interaction.NONE;
                break;
            case M3DOOR2:
                mapMgr.loadMap(MapFactory.MapType.M3DOOR1);
                mapMgr.setStartPostionByNameExtension("2");
                interaction = Entity.Interaction.NONE;
                break;
        }
    }

    @Override
    public void handleInteractionFinished() {
        interaction = Entity.Interaction.NONE;
    }

}
