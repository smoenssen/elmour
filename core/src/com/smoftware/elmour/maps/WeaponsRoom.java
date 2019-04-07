package com.smoftware.elmour.maps;

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

public class WeaponsRoom extends Map {
    private static final String TAG = WeaponsRoom.class.getSimpleName();

    private static String mapPath = "RPGGame/maps/Weapons_Room.tmx";
    private Json json;

    WeaponsRoom(){
        super(MapFactory.MapType.WEAPONS_ROOM, mapPath);

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

    }

    @Override
    public void handleInteraction() {

    }

    public void handleInteraction(MapManager mapMgr) {
        switch(interaction) {
            case EXIT:
                if (previousMapType != null) {
                    mapMgr.loadMap(previousMapType);
                    mapMgr.setStartPositionFromPreviousMap();
                    interaction = Entity.Interaction.NONE;
                }
                break;
        }
    }

    @Override
    public void handleInteractionFinished() {

    }

    private void initSpecialEntityPosition(Entity entity){
        Vector2 position = new Vector2(0,0);

        if( _specialNPCStartPositions.containsKey(entity.getEntityConfig().getEntityID()) ) {
            position = _specialNPCStartPositions.get(entity.getEntityConfig().getEntityID());
        }
        entity.sendMessage(Component.MESSAGE.INIT_START_POSITION, json.toJson(position));

        //Overwrite default if special config is found
        EntityConfig entityConfig = ProfileManager.getInstance().getProperty(entity.getEntityConfig().getEntityID(), EntityConfig.class);
        if( entityConfig != null ){
            entity.setEntityConfig(entityConfig);
        }
    }
}
