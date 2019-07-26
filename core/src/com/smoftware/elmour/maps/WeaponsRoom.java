package com.smoftware.elmour.maps;

import com.badlogic.gdx.utils.Json;
import com.smoftware.elmour.entities.Entity;
import com.smoftware.elmour.audio.AudioObserver;

/**
 * Created by steve on 9/16/17.
 */

public class WeaponsRoom extends Map {
    private static final String TAG = WeaponsRoom.class.getSimpleName();

    private static String mapPath = "RPGGame/maps/Weapons_Room.tmx";
    private Json json;

    WeaponsRoom(){
        super(MapFactory.MapType.WEAPONS_ROOM, mapPath);

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

    public void handleInteraction(MapManager mapMgr) {
        switch(interaction) {
            case EXIT:
                mapMgr.loadMap(MapFactory.MapType.ARMORY);
                mapMgr.setStartPositionFromPreviousMap(MapFactory.MapType.WEAPONS_ROOM.toString());
                interaction = Entity.Interaction.NONE;
                break;
        }
    }

    @Override
    public void handleInteractionFinished() {
        interaction = Entity.Interaction.NONE;
    }

}
