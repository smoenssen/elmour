package com.smoftware.elmour.maps;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.smoftware.elmour.main.Component;
import com.smoftware.elmour.main.Entity;
import com.smoftware.elmour.main.EntityFactory;
import com.smoftware.elmour.audio.AudioObserver;

/**
 * Created by steve on 9/9/17.
 */

public class Compass extends Map {
    private static final String TAG = Compass.class.getSimpleName();

    private static String mapPath = "RPGGame/maps/Compass.tmx";
    private Json json;

    Compass(){
        super(MapFactory.MapType.COMPASS, mapPath);

        json = new Json();

        for( Vector2 position: _npcStartPositions){
            Entity entity = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.TOWN_GUARD_WALKING);
            entity.sendMessage(Component.MESSAGE.INIT_START_POSITION, json.toJson(position));
            mapEntities.add(entity);
        }

        //Special cases
        Entity justin = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.JUSTIN);
        initSpecialEntityPosition(justin);
        mapEntities.add(justin);

        Entity carmen = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.CARMEN);
        initSpecialEntityPosition(carmen);
        mapEntities.add(carmen);
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

    }

    @Override
    public void handleInteractionFinished() {
        interaction = Entity.Interaction.NONE;
    }

}
