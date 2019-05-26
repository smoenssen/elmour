package com.smoftware.elmour.screens.quests;

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
import com.smoftware.elmour.screens.CutSceneBase;
import com.smoftware.elmour.screens.CutSceneObserver;
import com.smoftware.elmour.sfx.ScreenTransitionAction;

import java.util.ArrayList;

public class CloningQuest extends CutSceneBase implements ConversationGraphObserver {
    private static final String TAG = CloningQuest.class.getSimpleName();

    CloningQuest thisScreen;
    String questID;
    String currentPartNumber;

    private Action cloningMaterialsOpeningScene;
    private Action setupDogsQuestOpeningScene;

    private AnimatedImage character1;
    private AnimatedImage rick;
    private AnimatedImage justin;
    private AnimatedImage jaxon;

    private AnimatedImage camactor;
    private AnimatedImage misc;
    private AnimatedImage misc2;

    public CloningQuest(ElmourGame game, PlayerHUD playerHUD) {
        super(game, playerHUD);
        thisScreen = this;
        currentPartNumber = "";

        character1 = getAnimatedImage(EntityFactory.EntityName.CHARACTER_1);
        rick = getAnimatedImage(EntityFactory.EntityName.RICK);
        justin = getAnimatedImage(EntityFactory.EntityName.CARMEN);
        jaxon = getAnimatedImage(EntityFactory.EntityName.CARMEN);

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
        _stage.addActor(rick);
        _stage.addActor(justin);
        _stage.addActor(jaxon);

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

        cloningMaterialsOpeningScene = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.T1DOOR4);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(5, 4);

                rick.setVisible(true);
                rick.setPosition(6.5f, 2);
                rick.setCurrentAnimationType(Entity.AnimationType.IDLE);
                rick.setCurrentDirection(Entity.Direction.UP);

                character1.setVisible(false);
                character1.setPosition(3, 1);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.UP);

                misc.setVisible(true);
                misc.setPosition(1, 7);
                misc.setCurrentAnimationType(Entity.AnimationType.CLONING_COMP);

                misc2.setVisible(false);
                misc2.setPosition(8, 6);
                misc2.setCurrentAnimationType(Entity.AnimationType.IDLE);
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
            case RICK_WALK_TO_COMP:
                _playerHUD.acceptQuest(questID);
                fadeToMainScreen();
                break;




            case ACCEPT_QUEST:
                _playerHUD.acceptQuest(questID);
                fadeToMainScreen();
                break;
            case DECLINE_QUEST:
                String cutsceneName = ElmourGame.ScreenType.CloningQuestScreen.toString() + "_" + currentPartNumber;
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

    private Action getCloningMaterialsOpeningScene() {
        cloningMaterialsOpeningScene.reset();
        return Actions.sequence(
                Actions.addAction(cloningMaterialsOpeningScene),
                new setFading(true),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 2), _transitionActor),
                Actions.delay(2),
                Actions.run(
                        new Runnable() {
                            @Override
                            public void run() {
                                _playerHUD.loadConversationForCutScene("RPGGame/maps/Game/Text/Dialog/CloningMaterials/CloningMaterialsDialog.json", thisScreen);
                                _playerHUD.doConversation();
                                // NOTE: This just kicks off the conversation. The actions in the conversation are handled in the onNotify() function.
                            }
                        }),
                Actions.delay(2)
        );
    }

    @Override
    public void show() {
        baseShow();

        currentPartNumber = ProfileManager.getInstance().getProperty(ElmourGame.ScreenType.CloningQuestScreen.toString(), String.class);

        if (currentPartNumber.equals("CloningQuestOpen")) {
            questID = "CloningMaterials";
            _stage.addAction(getCloningMaterialsOpeningScene());
        }
        else if (currentPartNumber.equals("DogsQuestOpen")) {
            questID = "DogsQuest";
            _stage.addAction(getCloningMaterialsOpeningScene());
        }

        ProfileManager.getInstance().addObserver(_mapMgr);
        _playerHUD.setCutScene(true);

        if( _mapRenderer == null ){
            ProfileManager.getInstance().setProperty("currentMapType", MapFactory.MapType.MAP1.toString());
            _mapRenderer = new OrthogonalTiledMapRenderer(_mapMgr.getCurrentTiledMap(), Map.UNIT_SCALE);
        }
    }

    @Override
    public void hide() {
        ProfileManager.getInstance().setProperty(ElmourGame.ScreenType.CloningQuestScreen.toString(), "");
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

