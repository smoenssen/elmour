package com.smoftware.elmour.screens;

import com.badlogic.gdx.Gdx;
import com.smoftware.elmour.ComponentObserver;
import com.smoftware.elmour.ElmourGame;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.profile.ProfileManager;

/**
 * Created by steve on 4/14/19.
 */

public class CutSceneManager implements ComponentObserver {
    private static final String TAG = CutSceneManager.class.getSimpleName();

    private Entity player;
    private ElmourGame game;

    public CutSceneManager(final ElmourGame game, final Entity player) {
        this.game = game;
        this.player = player;
        this.player.registerObserver(this);
    }

    boolean isFadingIn = false;

    @Override
    public void onNotify(String value, ComponentEvent event) {
        switch (event) {
            case CUTSCENE_ACTIVATED:
                if (!isFadingIn) {
                    // value is in the form <screen type>_<optional part number>
                    String[] sa = value.split("_");

                    if (sa.length > 1) {
                        ProfileManager.getInstance().setProperty(sa[0], sa[1]);
                    }

                    game.setScreen(game.getScreenType(ElmourGame.ScreenType.valueOf(sa[0])));

                    isFadingIn = true;
                }
                break;
        }
    }
}
