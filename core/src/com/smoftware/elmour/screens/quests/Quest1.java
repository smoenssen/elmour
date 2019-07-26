package com.smoftware.elmour.screens.quests;

/**
 * Created by steve on 4/23/19.
 */


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.smoftware.elmour.main.ElmourGame;
import com.smoftware.elmour.main.Entity;
import com.smoftware.elmour.main.EntityFactory;
import com.smoftware.elmour.UI.graphics.AnimatedImage;
import com.smoftware.elmour.UI.huds.PlayerHUD;
import com.smoftware.elmour.UI.dialog.ConversationChoice;
import com.smoftware.elmour.UI.dialog.ConversationGraph;
import com.smoftware.elmour.UI.dialog.ConversationGraphObserver;
import com.smoftware.elmour.maps.MapFactory;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.screens.CutSceneBase;
import com.smoftware.elmour.screens.CutSceneObserver;
import com.smoftware.elmour.sfx.ScreenTransitionAction;

import java.util.ArrayList;

public class Quest1 extends CutSceneBase implements ConversationGraphObserver {
    private static final String TAG = Quest1.class.getSimpleName();

    Quest1 thisScreen;
    String questID;

    private Action setupQuestOpeningScene;
    private Action setupDogsQuestOpeningScene;

    private AnimatedImage character1;
    private AnimatedImage ophion;
    private AnimatedImage carmen;

    private AnimatedImage camactor;
    private AnimatedImage misc;
    private AnimatedImage misc2;

    public Quest1(ElmourGame game, PlayerHUD playerHUD) {
        super(game, playerHUD);
        thisScreen = this;
        currentPartNumber = "";

        character1 = getAnimatedImage(EntityFactory.EntityName.CHARACTER_1);
        ophion = getAnimatedImage(EntityFactory.EntityName.OPHION);
        carmen = getAnimatedImage(EntityFactory.EntityName.CARMEN);

        camactor = getAnimatedImage(EntityFactory.EntityName.STEVE);
        misc = getAnimatedImage(EntityFactory.EntityName.MISC_ANIMATIONS);
        misc2 = getAnimatedImage(EntityFactory.EntityName.MISC_ANIMATIONS);

        camactor.setVisible(false);
        misc.setVisible(false);
        misc.setCurrentAnimationType(Entity.AnimationType.FORCEFIELD);
        misc2.setVisible(false);
        misc2.setCurrentAnimationType(Entity.AnimationType.FORCEFIELD);

        _followingActor.setPosition(0, 0);

        _stage.addActor(character1);
        _stage.addActor(ophion);
        _stage.addActor(carmen);

        _stage.addActor(camactor);
        _stage.addActor(misc);
        _stage.addActor(misc2);
        _stage.addActor(_transitionActor);

        //Actions
        _switchScreenToMainAction = new RunnableAction() {
            @Override
            public void run() {
                _game.setScreen(_game.getScreenType(ElmourGame.ScreenType.MainGame));
            }
        };

        setupQuestOpeningScene = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.MAP1);
                _mapMgr.disableCurrentmapMusic();
                float yPos = 8.5f;
                setCameraPosition(10, yPos);
                keepCamInMap = true;

                ophion.setVisible(true);
                ophion.setPosition(10, yPos);
                ophion.setCurrentAnimationType(Entity.AnimationType.IDLE);
                ophion.setCurrentDirection(Entity.Direction.RIGHT);

                character1.setVisible(true);
                character1.setPosition(11, yPos);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.LEFT);

                followActor(character1);
            }
        };

        setupDogsQuestOpeningScene = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.COMPASS);
                _mapMgr.disableCurrentmapMusic();
                float yPos = 3.5f;
                setCameraPosition(10, yPos);
                keepCamInMap = true;

                ophion.setVisible(false);

                carmen.setVisible(true);
                carmen.setPosition(15.5f, yPos);
                carmen.setCurrentAnimationType(Entity.AnimationType.IDLE);
                carmen.setCurrentDirection(Entity.Direction.LEFT);

                character1.setVisible(true);
                character1.setPosition(14.5f, yPos);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.RIGHT);

                followActor(character1);
            }
        };
    }

    private void fadeToMainScreen() {
        _stage.addAction(Actions.sequence(
                new setFading(true),
                Actions.addAction(Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 1), _transitionActor)),
                Actions.delay(1),
                Actions.addAction(_switchScreenToMainAction)
                )
        );
    }

    @Override
    public void onNotify(ConversationGraph graph, ConversationCommandEvent action, String data) {
        Gdx.app.log(TAG, "Got notification " + action.toString());
        String conversationId = data;
        oneBlockTime = 0.3f;
        emoteOn = 0.7f;
        emoteOff = 0.05f;

        switch (action) {
            case WAIT_1000:
                _playerHUD.doConversation(graph.getNextConversationIDFromChoice(conversationId, 0), 1000);
                break;
            case ACCEPT_QUEST:
                _playerHUD.acceptQuest(questID);
                fadeToMainScreen();
                break;
            case DECLINE_QUEST:
                String cutsceneName = ElmourGame.ScreenType.Quest1Screen.toString() + "_" + currentPartNumber;
                ProfileManager.getInstance().setProperty(cutsceneName, CutSceneObserver.CutSceneStatus.NOT_STARTED);
                fadeToMainScreen();
                break;
        }
    }

    @Override
    public void onNotify(ConversationGraph graph, ConversationCommandEvent action) {
    }

    @Override
    public void onNotify(ConversationGraph graph, ArrayList<ConversationChoice> choices) {
    }

    @Override
    public void onNotify(String value, ConversationCommandEvent event) {
    }

    private Action getTeddyBearQuestOpeningScene() {
        setupQuestOpeningScene.reset();
        return Actions.sequence(
                Actions.addAction(setupQuestOpeningScene),
                new setFading(true),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 3), _transitionActor),
                Actions.delay(2),
                Actions.run(
                        new Runnable() {
                            @Override
                            public void run() {
                                _playerHUD.loadConversationForCutScene("RPGGame/maps/Game/Text/Quest_Dialog/TeddyBear/OphionQuestDialog.json", thisScreen);
                                _playerHUD.doConversation();
                                // NOTE: This just kicks off the conversation. The actions in the conversation are handled in the onNotify() function.
                            }
                        }),
                Actions.delay(3)
        );
    }

    private Action getDogsQuestOpeningScene() {
        setupDogsQuestOpeningScene.reset();
        return Actions.sequence(
                Actions.addAction(setupDogsQuestOpeningScene),
                new setFading(true),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 3), _transitionActor),
                Actions.delay(2),
                Actions.run(
                        new Runnable() {
                            @Override
                            public void run() {
                                _playerHUD.loadConversationForCutScene("RPGGame/maps/Game/Text/Quest_Dialog/DogsQuest/DogsQuestDialog.json", thisScreen);
                                _playerHUD.doConversation();
                                // NOTE: This just kicks off the conversation. The actions in the conversation are handled in the onNotify() function.
                            }
                        }),
                Actions.delay(3)
        );
    }

    @Override
    public void show() {
        baseShow();

        currentPartNumber = ProfileManager.getInstance().getProperty(ElmourGame.ScreenType.Quest1Screen.toString(), String.class);

        if (currentPartNumber.equals("TeddyBearQuestOpen")) {
            questID = "TeddyBear";
            _stage.addAction(getTeddyBearQuestOpeningScene());
        }
        else if (currentPartNumber.equals("DogsQuestOpen")) {
            questID = "DogsQuest";
            _stage.addAction(getDogsQuestOpeningScene());
        }

        baseShow();

        /*
        if( _mapRenderer == null ){
            ProfileManager.getInstance().setProperty("currentMapType", MapFactory.MapType.MAP1.toString()); //todo: is this necessary?
            _mapRenderer = new OrthogonalTiledMapRenderer(_mapMgr.getCurrentTiledMap(), Map.UNIT_SCALE);
        }
        */
    }

    @Override
    public void hide() {
        ProfileManager.getInstance().setProperty(ElmourGame.ScreenType.Quest1Screen.toString(), "");
        baseHide();
    }

    @Override
    public void render(float delta) {
        baseRender(delta);
    }

    @Override
    public void resize(int width, int height) {
        baseResize(width, height);
    }

    @Override
    public void pause() {
        basePause();
    }

    @Override
    public void resume() {
        baseResume();
    }

    @Override
    public void dispose() {
        baseDispose();
    }
}

