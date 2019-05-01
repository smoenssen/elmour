package com.smoftware.elmour.screens;

import com.badlogic.gdx.utils.Timer;
import com.smoftware.elmour.ComponentObserver;
import com.smoftware.elmour.ElmourGame;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.EntityConfig;
import com.smoftware.elmour.UI.PlayerHUD;
import com.smoftware.elmour.profile.ProfileManager;

/**
 * Created by steve on 4/14/19.
 */

public class CutSceneManager extends CutSceneSubject implements ComponentObserver {
    private static final String TAG = CutSceneManager.class.getSimpleName();

    public final static float FADE_OUT_TIME = 0.5f;
    public final static String CUTSCENE_PREFIX = "CUTSCENE_";

    private Entity player;
    private ElmourGame game;
    private PlayerHUD playerHUD;

    public CutSceneManager(final ElmourGame game, final Entity player, final PlayerHUD playerHUD) {
        this.game = game;
        this.player = player;
        this.player.registerObserver(this);
        this.playerHUD = playerHUD;
    }

    @Override
    public void onNotify(String value, ComponentEvent event) {
        switch (event) {
            case CUTSCENE_ACTIVATED:
                startCutScene(value);
                break;
        }
    }

    @Override
    public void onNotify(Entity entity, String value, ComponentEvent event) {
        switch (event) {
            case LOAD_CONVERSATION:
                // check to see if cut scene should be kicked off
                EntityConfig.ConversationConfig conversationConfig = playerHUD.getConversationConfigForNPC(entity, value);

                if (conversationConfig != null) {
                    switch (conversationConfig.type) {
                        case PRE_QUEST_CUTSCENE:
                        case ACTIVE_QUEST_CUTSCENE:
                        case QUEST_TASK_CUTSCENE:
                            // config contains cut scene string
                            startCutScene(conversationConfig.config);
                            ProfileManager.getInstance().setProperty(
                                    entity.getEntityConfig().getEntityID() + EntityConfig.ConversationConfig.class.getSimpleName(), conversationConfig);
                            break;
                    }
                }
        }
    }

    private void startCutScene(String value) {
        // check to see if this cut scene should be kicked off
        CutSceneObserver.CutSceneStatus status = ProfileManager.getInstance().getProperty(value, CutSceneObserver.CutSceneStatus.class);

        if (status == null || status.equals(CutSceneObserver.CutSceneStatus.NOT_STARTED)) {
            ProfileManager.getInstance().setProperty(value, CutSceneObserver.CutSceneStatus.STARTED);

            if (!getSetScreenTimer(value).isScheduled()) {
                // delay here so game screen has a chance to fade out
                Timer.schedule(getSetScreenTimer(value), FADE_OUT_TIME);
            }

            notify(value, CutSceneObserver.CutSceneStatus.STARTED);
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
