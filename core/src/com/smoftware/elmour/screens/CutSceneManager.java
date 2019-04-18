package com.smoftware.elmour.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Timer;
import com.smoftware.elmour.ComponentObserver;
import com.smoftware.elmour.ElmourGame;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.profile.ProfileManager;

/**
 * Created by steve on 4/14/19.
 */

public class CutSceneManager implements ComponentObserver {
    private static final String TAG = CutSceneManager.class.getSimpleName();

    public static float FADE_OUT_TIME = 0.5f;

    private Entity player;
    private ElmourGame game;

    public CutSceneManager(final ElmourGame game, final Entity player) {
        this.game = game;
        this.player = player;
        this.player.registerObserver(this);
    }

    boolean isFadingIn = false;//todo: when to set false? also in MainGameScreen. Also, could this be a static variable?

    @Override
    public void onNotify(String value, ComponentEvent event) {
        switch (event) {
            case CUTSCENE_ACTIVATED:
                if (!isFadingIn) {
                    if (!getSetScreenTimer(value).isScheduled()) {
                        // delay here so game screen has a chance to fade out
                        Timer.schedule(getSetScreenTimer(value), FADE_OUT_TIME);
                    }

                    isFadingIn = true;
                }
                break;
        }
    }

    private Timer.Task getSetScreenTimer(final String value){
        return new Timer.Task() {
            @Override
            public void run() {
                // value is in the form <screen type>_<optional part number>
                String[] sa = value.split("_");

                if (sa.length > 1) {
                    // Set the cut scene's property. The cut scene can get the value in its show() function.
                    ProfileManager.getInstance().setProperty(sa[0], sa[1]);
                }

                game.setScreen(game.getScreenType(ElmourGame.ScreenType.valueOf(sa[0])));
            }
        };
    }
}
