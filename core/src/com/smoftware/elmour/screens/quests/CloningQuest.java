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
import com.smoftware.elmour.sfx.ShakeCamera;

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
    private AnimatedImage misc3;

    public CloningQuest(ElmourGame game, PlayerHUD playerHUD) {
        super(game, playerHUD);
        thisScreen = this;
        currentPartNumber = "";

        character1 = getAnimatedImage(EntityFactory.EntityName.CHARACTER_1);
        rick = getAnimatedImage(EntityFactory.EntityName.RICK);
        justin = getAnimatedImage(EntityFactory.EntityName.JUSTIN);
        jaxon = getAnimatedImage(EntityFactory.EntityName.JAXON_1);

        camactor = getAnimatedImage(EntityFactory.EntityName.STEVE);
        misc = getAnimatedImage(EntityFactory.EntityName.MISC_ANIMATIONS);
        misc2 = getAnimatedImage(EntityFactory.EntityName.MISC_ANIMATIONS);
        misc3 = getAnimatedImage(EntityFactory.EntityName.MISC_ANIMATIONS);

        camactor.setVisible(false);
        misc.setVisible(false);
        misc.setCurrentAnimationType(Entity.AnimationType.FORCEFIELD);
        misc2.setVisible(false);
        misc2.setCurrentAnimationType(Entity.AnimationType.FORCEFIELD);
        misc3.setVisible(false);
        misc3.setCurrentAnimationType(Entity.AnimationType.FORCEFIELD);

        _followingActor.setPosition(0, 0);

        _stage.addActor(character1);
        _stage.addActor(rick);
        _stage.addActor(justin);
        _stage.addActor(jaxon);

        _stage.addActor(camactor);
        _stage.addActor(misc);
        _stage.addActor(misc2);
        _stage.addActor(misc3);
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
                setCameraPosition(6, 5);
                keepCamInMap = false;

                rick.setVisible(true);
                rick.setPosition(6.5f, 2.5f);
                rick.setCurrentAnimationType(Entity.AnimationType.IDLE);
                rick.setCurrentDirection(Entity.Direction.UP);

                character1.setVisible(false);
                character1.setPosition(3, 1.33f);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.UP);

                misc.setVisible(true);
                misc.setPosition(1, 7);
                misc.setCurrentAnimationType(Entity.AnimationType.CLONING_COMP);

                misc2.setVisible(false);
                misc2.setPosition(8, 6);
                misc2.setCurrentAnimationType(Entity.AnimationType.SHOCK_OFF);

                misc3.setVisible(false);
                misc3.setCurrentAnimationType(Entity.AnimationType.SHOCK_OFF);

                justin.setVisible(false);
                jaxon.setVisible(false);
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
            case RICK_FAILURE:
                if( shakeCam == null ){
                    shakeCam = new ShakeCamera(_camera.position.x, _camera.position.y,
                            0.02f,
                            0.3f,
                            0.05f,
                            0.0125f,
                            0.98f,
                            0.99f,
                            0.2f);
                }
                _stage.addAction(Actions.sequence(

                        Actions.delay(oneBlockTime * 1),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.PUSH_BUTTON),
                        Actions.delay(oneBlockTime * 4),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.IDLE),
                        myActions.new shakeCam(shakeCam),
                        Actions.delay(oneBlockTime * 4),

                        myActions.new setWalkDirection(misc, Entity.AnimationType.CLONING_COMP_BLINK),
                        myActions.new setCharacterVisible(misc2, true),
                        myActions.new setWalkDirection(misc2, Entity.AnimationType.CLONING_TANK_BLINK),

                        Actions.delay(oneBlockTime * 5),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case RICK_WALK_TO_COMP:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),

                        myActions.new setWalkDirection(rick, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveTo(3.5f, 2.5f, oneBlockTime * 3), rick),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(1.5f, 6.5f, oneBlockTime * 4), rick),
                        Actions.delay(oneBlockTime * 4),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case RICK_THINK:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),
                        Actions.addAction(Actions.moveTo(rick.getX() + emoteX, rick.getY() + emoteY), misc3),

                        myActions.new setWalkDirection(rick, Entity.AnimationType.THINK),

                        myActions.new setCharacterVisible(misc3, true),
                        myActions.new setWalkDirection(misc3, Entity.AnimationType.THINK_ON),
                        Actions.delay(0.24f),
                        myActions.new setWalkDirection(misc3, Entity.AnimationType.THINK_LOOP),
                        Actions.delay(2.1f),
                        myActions.new setWalkDirection(misc3, Entity.AnimationType.THINK_OFF),
                        Actions.delay(0.075f),
                        myActions.new setCharacterVisible(misc3, false),

                        myActions.new setCharacterVisible(character1, true),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setIdleDirection(rick, Entity.Direction.DOWN),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.IDLE),

                        myActions.new setCharacterVisible(misc3, true),
                        myActions.new setWalkDirection(misc3, Entity.AnimationType.IDEA_ON),
                        Actions.delay(0.24f),
                        myActions.new setWalkDirection(misc3, Entity.AnimationType.IDEA_LOOP),
                        Actions.delay(2.1f),
                        myActions.new setWalkDirection(misc3, Entity.AnimationType.IDEA_OFF),
                        Actions.delay(0.075f),
                        myActions.new setCharacterVisible(misc3, false),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case RICK_WALK_AROUND:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),

                        myActions.new setWalkDirection(rick, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveBy(1.5f, 0, oneBlockTime * 3), rick),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setIdleDirection(rick, Entity.Direction.RIGHT),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.THINK),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case RICK_SPOT_CHAR:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),
                        Actions.addAction(Actions.moveTo(rick.getX() + emoteX, rick.getY() + emoteY), misc3),

                        myActions.new setIdleDirection(rick, Entity.Direction.DOWN),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.IDLE),
                        myActions.new setCharacterVisible(misc3, true),
                        myActions.new setWalkDirection(misc3, Entity.AnimationType.SHOCK_ON),
                        Actions.delay(emoteOn),
                        myActions.new setWalkDirection(misc3, Entity.AnimationType.SHOCK_OFF),
                        Actions.delay(emoteOff),
                        myActions.new setCharacterVisible(misc3, false),

                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case RICK_LOOK_AWAY:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(rick, Entity.Direction.UP),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case RICK_LOOK_DOWN:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setIdleDirection(rick, Entity.Direction.DOWN),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case RICK_WALK_DOWN:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),

                        myActions.new setWalkDirection(rick, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveBy(0, -2, oneBlockTime * 4), rick),
                        Actions.delay(oneBlockTime * 4),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case RICK_DESTROY_KEYBOARD:
                    shakeCam = new ShakeCamera(_camera.position.x, _camera.position.y,
                            0.02f,
                            0.4f,
                            0.4f,
                            0.0125f,
                            0.7f,
                            0.7f,
                            0.2f);
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),

                        myActions.new setWalkDirection(rick, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveTo(rick.getX() + 1, 2.5f, oneBlockTime * 3), rick),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveTo(6.5f, 2.5f, oneBlockTime * 3), rick),
                        myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),

                        Actions.delay(oneBlockTime * 3),

                        myActions.new setIdleDirection(rick, Entity.Direction.UP),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.PUSH_BUTTON),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new setCharacterVisible(misc, false),
                        myActions.new setCharacterVisible(misc2, false),
                        Actions.delay(oneBlockTime * 2),

                        Actions.addAction(Actions.moveTo(6.5f, 3), misc),
                        Actions.addAction(Actions.moveTo(6.5f, 3), misc2),
                        Actions.addAction(Actions.moveTo(6.5f, 3), misc3),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.HIDDEN_ITEM_FULL),
                        myActions.new setWalkDirection(misc2, Entity.AnimationType.HIDDEN_ITEM_FULL),
                        myActions.new setWalkDirection(misc3, Entity.AnimationType.HIDDEN_ITEM_FULL),

                        myActions.new shakeCam(shakeCam),
                        myActions.new setCharacterVisible(misc, true),
                        myActions.new setCharacterVisible(misc2, true),
                        myActions.new setCharacterVisible(misc3, true),
                        Actions.addAction(Actions.moveBy(-5, 10, oneBlockTime * 3), misc),
                        Actions.addAction(Actions.moveBy(0, 15, oneBlockTime * 3), misc2),
                        Actions.addAction(Actions.moveBy(5, 10, oneBlockTime * 3), misc3),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.IDLE),

                        Actions.delay(oneBlockTime * 5),
                        myActions.new setIdleDirection(rick, Entity.Direction.LEFT),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
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
                                _playerHUD.loadConversationForCutScene("RPGGame/maps/Game/Text/Quest_Dialog/CloningMaterials/CloningMaterialsDialog.json", thisScreen);
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

        if (currentPartNumber.equals("CloningMaterialsOpen")) {
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

