package com.smoftware.elmour.maps;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.smoftware.elmour.components.Component;
import com.smoftware.elmour.entities.Entity;
import com.smoftware.elmour.entities.EntityFactory;
import com.smoftware.elmour.audio.AudioObserver;

/**
 * Created by steve on 9/9/17.
 */

public class M3DOOR1 extends Map {
    private static final String TAG = M3DOOR1.class.getSimpleName();

    public static String mapPath = "RPGGame/maps/Buildings/M3DOOR1.tmx";

    M3DOOR1(){
        super(MapFactory.MapType.M3DOOR1, mapPath);

        json = new Json();

        for( Vector2 position: _npcStartPositions){
            Entity entity = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.CHARACTER_1);
            entity.sendMessage(Component.MESSAGE.INIT_START_POSITION, json.toJson(position));
            mapEntities.add(entity);
        }

        //Special cases
        Entity tony = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.TONY);
        initSpecialEntityPosition(tony);
        mapEntities.add(tony);
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
        switch(interaction) {
            case EXIT:
                if (previousMapType != null) {
                    mapMgr.loadMap(MapFactory.MapType.MAP3);
                    mapMgr.setStartPostionByNameExtension("1");
                    interaction = Entity.Interaction.NONE;
                }
                break;
            case EXIT2:
                if (previousMapType != null) {
                    mapMgr.loadMap(MapFactory.MapType.MAP3);
                    mapMgr.setStartPostionByNameExtension("2");
                    interaction = Entity.Interaction.NONE;
                }
                break;
        }
    }

    @Override
    public void handleInteractionFinished() {
        interaction = Entity.Interaction.NONE;
    }

}
