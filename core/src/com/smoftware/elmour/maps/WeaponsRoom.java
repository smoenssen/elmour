package com.smoftware.elmour.maps;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.smoftware.elmour.components.Component;
import com.smoftware.elmour.entities.Entity;
import com.smoftware.elmour.audio.AudioObserver;
import com.smoftware.elmour.entities.EntityFactory;
import com.smoftware.elmour.screens.chapters.Chapter2;

/**
 * Created by steve on 9/16/17.
 */

public class WeaponsRoom extends Map {
    private static final String TAG = WeaponsRoom.class.getSimpleName();

    public static String mapPath = "RPGGame/maps/Weapons_Room.tmx";
    private Json json;

    WeaponsRoom(){
        super(MapFactory.MapType.WEAPONS_ROOM, mapPath);

        json = new Json();

        //todo: spqwn justin and jaxon if weapon isn't selected
        //todo: also want to spawn in different spot if certain quests are active
        /*
        Entity justin = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.JUSTIN);
        initSpecialEntityPosition(justin);
        mapEntities.add(justin);
        */

        for( Vector2 position: _npcStartPositions){
            Entity entity = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.JUSTIN);
            entity.sendMessage(Component.MESSAGE.INIT_START_POSITION, json.toJson(position));
            mapEntities.add(entity);
        }

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
                // Allow exit only if weapon is selected
                if (Chapter2.isWeaponSelected()) {
                    mapMgr.loadMap(MapFactory.MapType.ARMORY);
                    mapMgr.setStartPositionFromPreviousMap(MapFactory.MapType.WEAPONS_ROOM.toString());
                }
                else {
                    notify(MapObserver.MapEvent.DISPLAY_CONVERSATION, "RPGGame/maps/Game/Text/Dialog/Chapter_2_SelectWeapon.json");
                }
                interaction = Entity.Interaction.NONE;
                break;
        }
    }

    @Override
    public void handleInteractionFinished() {
        interaction = Entity.Interaction.NONE;
    }
}
