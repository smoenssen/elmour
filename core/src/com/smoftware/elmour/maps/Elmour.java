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

public class Elmour extends Map {
    private static final String TAG = Elmour.class.getSimpleName();

    private static String mapPath = "RPGGame/maps/Elmour.tmx";

    Elmour(){
        super(MapFactory.MapType.ELMOUR, mapPath);

        json = new Json();

        for( Vector2 position: _npcStartPositions){
            Entity entity = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.TOWN_GUARD_WALKING);
            entity.sendMessage(Component.MESSAGE.INIT_START_POSITION, json.toJson(position));
            mapEntities.add(entity);
        }

        //Special cases
        //Entity ophion = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.OPHION);
        //initSpecialEntityPosition(ophion);
        //mapEntities.add(ophion);
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
            case ARMORY:
                mapMgr.loadMap(MapFactory.MapType.ARMORY);
                mapMgr.setStartPositionFromPreviousMap();
                interaction = Entity.Interaction.NONE;
                break;
            case T1DOOR4:
                mapMgr.loadMap(MapFactory.MapType.T1DOOR4);
                mapMgr.setStartPositionFromPreviousMap();
                interaction = Entity.Interaction.NONE;
                break;
        }

    }

    @Override
    public void handleInteractionFinished() {
        interaction = Entity.Interaction.NONE;
    }

}
