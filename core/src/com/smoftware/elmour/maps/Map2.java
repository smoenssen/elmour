package com.smoftware.elmour.maps;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.utils.Json;
import com.smoftware.elmour.main.Entity;
import com.smoftware.elmour.audio.AudioObserver;
import com.smoftware.elmour.profile.ProfileManager;

/**
 * Created by steve on 9/14/17.
 */

public class Map2 extends Map {

    private static final String TAG = Map2.class.getSimpleName();

    private static String mapPath = "RPGGame/maps/Map_2.tmx";
    private boolean switchEnabled;

    Map2(){
        super(MapFactory.MapType.MAP2, mapPath);

        json = new Json();

        interaction = Entity.Interaction.NONE;
        switchEnabled = false; //todo: get this from profile
    }

    @Override
    public void unloadMusic() {
        notify(AudioObserver.AudioCommand.MUSIC_STOP, AudioObserver.AudioTypeEvent.MUSIC_TOWN);
    }

    @Override
    public void loadMusic() {
        notify(AudioObserver.AudioCommand.MUSIC_LOAD, AudioObserver.AudioTypeEvent.MUSIC_TOWN);
        notify(AudioObserver.AudioCommand.MUSIC_PLAY_LOOP, AudioObserver.AudioTypeEvent.MUSIC_TOWN);
        notify(AudioObserver.AudioCommand.MUSIC_LOAD, AudioObserver.AudioTypeEvent.SOUND_MOUNTAIN_AVALANCHE);
    }

    @Override
    public void handleInteractionInit(Entity.Interaction interaction) {
        this.interaction = interaction;
    }

    @Override
    public void handleInteraction() {
        if (interaction == Entity.Interaction.M2SWITCH) {
            toggleSwitch();
        }
    }

    @Override
    public void handleInteraction(MapManager mapMgr) {

    }

    @Override
    public void handleInteractionFinished() {
        interaction = Entity.Interaction.NONE;
    }


    public MapLayer getZeroOpacityLayer() {
        if (switchEnabled)
            return null;
        else
            return zeroOpacityLayer;
    }

    private void toggleSwitch() {
        // make sure switch hasn't been already set
        String value = ProfileManager.getInstance().getProperty(interaction.toString(), String.class);
        if (value == null) {
            if (switchEnabled) {
                switchEnabled = false;
                _currentMap.getLayers().get("Switch Press").setVisible(true);
            }
            else {
                switchEnabled = true;
                _currentMap.getLayers().get("Switch Press").setVisible(false);
                notify(MapObserver.MapEvent.SHAKE_CAM);
                notify(AudioObserver.AudioCommand.MUSIC_PLAY_ONCE, AudioObserver.AudioTypeEvent.SOUND_MOUNTAIN_AVALANCHE);
                ProfileManager.getInstance().setProperty(interaction.toString(), "");
            }
        }
    }
}
