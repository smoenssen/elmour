package com.smoftware.elmour.screens;

/**
 * Created by steve on 4/23/19.
 */


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.smoftware.elmour.ElmourGame;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.EntityFactory;
import com.smoftware.elmour.UI.AnimatedImage;
import com.smoftware.elmour.UI.PlayerHUD;
import com.smoftware.elmour.audio.AudioManager;
import com.smoftware.elmour.dialog.ConversationChoice;
import com.smoftware.elmour.dialog.ConversationGraph;
import com.smoftware.elmour.dialog.ConversationGraphObserver;
import com.smoftware.elmour.maps.Map;
import com.smoftware.elmour.maps.MapFactory;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.sfx.ScreenTransitionAction;

import java.util.ArrayList;

public class CutSceneQuest1 extends CutSceneBase implements ConversationGraphObserver {
    private static final String TAG = CutSceneQuest1.class.getSimpleName();

    CutSceneQuest1 thisScreen;
    String currentPartNumber;

    private Action setupQuestOpeningScene;

    private AnimatedImage character1;
    private AnimatedImage ophion;

    private AnimatedImage camactor;
    private AnimatedImage misc;
    private AnimatedImage misc2;

    public CutSceneQuest1(ElmourGame game, PlayerHUD playerHUD) {
        super(game, playerHUD);
        thisScreen = this;
        currentPartNumber = "";

        character1 = getAnimatedImage(EntityFactory.EntityName.CHARACTER_1);
        ophion = getAnimatedImage(EntityFactory.EntityName.OPHION);

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
            case WAIT_10000:
                _playerHUD.doConversation(graph.getNextConversationIDFromChoice(conversationId, 0), 10000);
                break;
            case EXIT_CONVERSATION_1:
                _stage.addAction(Actions.sequence(
                        new setFading(true),
                        Actions.addAction(Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 1), _transitionActor)),
                        Actions.delay(1),
                        Actions.addAction(_switchScreenToMainAction)
                        )
                );
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

    private Action getQuestOpeningScene() {
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
                                _playerHUD.loadConversationForCutScene("RPGGame/maps/Game/Text/Dialog/OphionQuestDialog.json", thisScreen);
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

        //if (currentPartNumber == null || currentPartNumber.equals("")) {
            _stage.addAction(getQuestOpeningScene());
        //}
        /*
        else if (currentPartNumber.equals("P2")) {
            _stage.addAction(getOutsideArmoryScene());
        }*/

        ProfileManager.getInstance().addObserver(_mapMgr);
        _playerHUD.setCutScene(true);

        if( _mapRenderer == null ){
            ProfileManager.getInstance().setProperty("currentMapType", MapFactory.MapType.MAP1.toString());
            _mapRenderer = new OrthogonalTiledMapRenderer(_mapMgr.getCurrentTiledMap(), Map.UNIT_SCALE);
        }
    }

    @Override
    public void hide() {
        ProfileManager.getInstance().setProperty(ElmourGame.ScreenType.Quest1Screen.toString(), "");
        Gdx.input.setInputProcessor(null);
        baseHide();
    }

    @Override
    public void render(float delta) {
        baseRender(delta);
    }

    @Override
    public void resize(int width, int height) {
        setupViewport(V_WIDTH, V_HEIGHT);
        _camera.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);
        _camera.position.set(lastCameraPosition);

        if (_playerHUD != null)
            _playerHUD.resize((int) VIEWPORT.physicalWidth, (int) VIEWPORT.physicalHeight);
    }

    @Override
    public void pause() {
        lastCameraPosition = _camera.position.cpy();

        if (_playerHUD != null)
            _playerHUD.pause();
    }

    @Override
    public void resume() {
        _camera.position.set(lastCameraPosition);

        setGameState(GameState.RUNNING);
        if (_playerHUD != null)
            _playerHUD.resume();
    }

    @Override
    public void dispose() {
        if( _mapRenderer != null ){
            _mapRenderer.dispose();
        }

        AudioManager.getInstance().dispose();
        MapFactory.clearCache();
    }
}

