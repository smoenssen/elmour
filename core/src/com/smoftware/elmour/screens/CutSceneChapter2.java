package com.smoftware.elmour.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.smoftware.elmour.ElmourGame;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.EntityFactory;
import com.smoftware.elmour.UI.AnimatedImage;
import com.smoftware.elmour.UI.MyActions;
import com.smoftware.elmour.UI.PlayerHUD;
import com.smoftware.elmour.audio.AudioManager;
import com.smoftware.elmour.dialog.ConversationChoice;
import com.smoftware.elmour.dialog.ConversationGraph;
import com.smoftware.elmour.dialog.ConversationGraphObserver;
import com.smoftware.elmour.maps.Map;
import com.smoftware.elmour.maps.MapFactory;
import com.smoftware.elmour.maps.MapManager;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.sfx.ScreenTransitionAction;
import com.smoftware.elmour.sfx.ScreenTransitionActor;

import java.util.ArrayList;

public class CutSceneChapter2 extends CutSceneBase implements ConversationGraphObserver {
    private static final String TAG = CutSceneChapter2.class.getSimpleName();

    CutSceneChapter2 thisScreen;

    private Action openingCutScene;
    private Action armoryCutSceneAction;
    private Action setupScene01;
    private Action setupSceneArmory;

    private AnimatedImage character1;
    private AnimatedImage character2;
    private AnimatedImage justin;
    private AnimatedImage jaxon;

    public CutSceneChapter2(ElmourGame game){
        super(game);
        thisScreen = this;

        character1 = getAnimatedImage(EntityFactory.EntityName.CHARACTER_1);
        character2 = getAnimatedImage(EntityFactory.EntityName.CHARACTER_2);
        justin = getAnimatedImage(EntityFactory.EntityName.JUSTIN);
        jaxon = getAnimatedImage(EntityFactory.EntityName.JAXON_1);

        _followingActor.setPosition(0, 0);

        followActor(character2);

        _stage.addActor(character1);
        _stage.addActor(character2);
        _stage.addActor(justin);
        _stage.addActor(jaxon);
        _stage.addActor(_transitionActor);

        //Actions
        _switchScreenAction = new RunnableAction(){
            @Override
            public void run() {
                _game.setScreen(_game.getScreenType(ElmourGame.ScreenType.MainGame));
            }
        };

        setupScene01 = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.ELMOUR);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(36, 20);

                character1.setVisible(true);
                character1.setPosition(36, 20);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.RIGHT);

                character2.setVisible(true);
                character2.setPosition(37, 20);
                character2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character2.setCurrentDirection(Entity.Direction.LEFT);

                followActor(character2);
            }
        };

        setupSceneArmory = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.ARMORY);
                _mapMgr.disableCurrentmapMusic();

                float f = _stage.getWidth();
                float centerX = (4.5f);
                //setCameraPosition(centerX, 3f);

                character1.setVisible(false);
                character1.setPosition(centerX - character1.getWidth()/2, 0);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.UP);

                character2.setVisible(true);
                character2.setPosition(centerX - character2.getWidth()/2, 1);
                character2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character2.setCurrentDirection(Entity.Direction.UP);

                justin.setVisible(true);
                justin.setPosition(centerX - 1f, 7.1f);
                justin.setCurrentAnimationType(Entity.AnimationType.IDLE);
                justin.setCurrentDirection(Entity.Direction.DOWN);

                jaxon.setVisible(true);
                jaxon.setPosition(centerX, 7.1f);
                jaxon.setCurrentAnimationType(Entity.AnimationType.IDLE);
                jaxon.setCurrentDirection(Entity.Direction.DOWN);

                followActor(character2);
            }
        };
    }

    @Override
    public void onNotify(ConversationGraph graph, ConversationCommandEvent action, String conversationId) {
        Gdx.app.log(TAG, "Got notification " + action.toString());

        switch (action) {
            case WAIT_1000:
                _playerHUD.doConversation(graph.getNextConversationIDFromChoice(conversationId, 0), 1000 * 1.25f);
                break;
            case WAIT_10000:
                _playerHUD.doConversation(graph.getNextConversationIDFromChoice(conversationId, 0), 10000);
                break;
            case WALK_TO_ARMORY:
                float oneBlockTime = 0.4f;
                Rectangle rect = getObjectRectangle(_mapMgr.getInteractionLayer(), "ARMORY");
                float char2BlocksToArmoryX = character2.getX() - (rect.getX() * Map.UNIT_SCALE);
                float char1BlocksToArmoryX = character1.getX() - (rect.getX() * Map.UNIT_SCALE);
                float char2BlocksToArmoryY = (rect.getY() * Map.UNIT_SCALE) - character2.getY();
                float char1BlocksToArmoryY = (rect.getY() * Map.UNIT_SCALE) - character1.getY();

                character2.setCurrentAnimationType(Entity.AnimationType.WALK_UP);

                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(character2.getX(), character2.getY() + 1, oneBlockTime, Interpolation.linear), character2),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveTo(character2.getX() - char2BlocksToArmoryX, character2.getY() + 1, oneBlockTime * char2BlocksToArmoryX, Interpolation.linear), character2),
                        Actions.delay(oneBlockTime * 2f),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(character1.getX(), character1.getY() + 1, oneBlockTime, Interpolation.linear), character1),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveTo(character1.getX() - char1BlocksToArmoryX, character1.getY() + 1, oneBlockTime * char1BlocksToArmoryX, Interpolation.linear), character1),
                        Actions.delay(oneBlockTime * 7.25f),

                        Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 2f), _transitionActor),

                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(rect.getX() * Map.UNIT_SCALE, character2.getY() + char2BlocksToArmoryY, oneBlockTime * char2BlocksToArmoryY, Interpolation.linear), character2),
                        Actions.delay(oneBlockTime * 2.25f),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(rect.getX() * Map.UNIT_SCALE, character1.getY() + char1BlocksToArmoryY, oneBlockTime * char1BlocksToArmoryY, Interpolation.linear), character1),

                        Actions.delay(2.5f),
                        Actions.addAction(armoryCutSceneAction))
                );

                break;
            case EXIT_CONVERSATION:
                _stage.addAction(Actions.addAction(Actions.moveTo(15, 76, 10, Interpolation.linear), character2));
                _stage.addAction(Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 3), _transitionActor));
        }

    }

    @Override
    public void onNotify(ConversationGraph graph, ConversationCommandEvent action) {
        Gdx.app.log(TAG, "onNotify 1");
    }

    @Override
    public void onNotify(ConversationGraph graph, ArrayList<ConversationChoice> choices) {
        Gdx.app.log(TAG, "onNotify 2");
    }

    @Override
    public void onNotify(String value, ConversationCommandEvent event) {
        Gdx.app.log(TAG, "onNotify 3");
    }

    private Action getOpeningCutSceneAction() {
        setupScene01.reset();
        return Actions.sequence(
                Actions.addAction(setupScene01),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 3), _transitionActor),
                Actions.delay(2),
                Actions.run(
                        new Runnable() {
                            @Override
                            public void run() {
                                _playerHUD.loadConversationForCutScene("conversations/Chapter_2.json", thisScreen);
                                _playerHUD.doConversation();
                                // NOTE: This just kicks off the conversation. The actions in the conversation are handled in the onNotify() function.
                            }
                        }),
                Actions.delay(3)
        );
    }

    private Action getArmoryCutScreenAction() {
        setupSceneArmory.reset();
        return Actions.sequence(
                Actions.addAction(setupSceneArmory),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 3), _transitionActor),
                myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_UP),
                myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_UP),
                Actions.addAction(Actions.moveTo(3.5f, 5.5f, 2.25f, Interpolation.linear), character2),
                Actions.delay(1.0f),
                myActions.new setCharacterVisible(character1, true),
                Actions.addAction(Actions.moveTo(4.5f, 5.5f, 2f, Interpolation.linear), character1),
                Actions.delay(1.0f),
                Actions.run(
                        new Runnable() {
                            @Override
                            public void run() {
                                // uncomment to start right from armory screen
                                // also need to change current conversation in the json file to n12
                                //_playerHUD.loadConversationForCutScene("conversations/Chapter_2.json", thisScreen);
                                _playerHUD.doConversation();
                                // NOTE: This resumes conversation
                            }
                        }),
                Actions.delay(1.0f),
                myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE)
                //Actions.delay(3f),
                //Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 2), _transitionActor)

                );
    }


    public void followActor(Actor actor){
        _followingActor = actor;
        _isCameraFixed = false;
    }

    public void setCameraPosition(float x, float y){
        _camera.position.set(x, y, 0f);
        _isCameraFixed = true;
    }

    @Override
    public void show() {
        openingCutScene = getOpeningCutSceneAction();
        armoryCutSceneAction = getArmoryCutScreenAction();
        _stage.addAction(openingCutScene);

        ProfileManager.getInstance().addObserver(_mapMgr);
        if (_playerHUD != null)
            ProfileManager.getInstance().addObserver(_playerHUD);

        setGameState(GameState.LOADING);

        Gdx.input.setInputProcessor(_multiplexer);

        if( _mapRenderer == null ){
            _mapRenderer = new OrthogonalTiledMapRenderer(_mapMgr.getCurrentTiledMap(), Map.UNIT_SCALE);
        }
    }

    @Override
    public void hide() {
        //if( _gameState != GameState.GAME_OVER ){
        //    setGameState(GameState.SAVING);
        //}

        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        _mapRenderer.setView(_camera);

        _mapRenderer.getBatch().enableBlending();
        _mapRenderer.getBatch().setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if( _mapMgr.hasMapChanged() ){
            _mapRenderer.setMap(_mapMgr.getCurrentTiledMap());
            _mapMgr.setMapChanged(false);
        }

        _mapRenderer.render();

        if( !_isCameraFixed ){
        	_camera.position.set(_followingActor.getX() + _followingActor.getWidth()/2, _followingActor.getY(), 0f);
        }
        _camera.update();

        _stage.act(delta);
        _stage.draw();

        _playerHUD.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        setupViewport(V_WIDTH, V_HEIGHT);
        _camera.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);

        if (_playerHUD != null)
            _playerHUD.resize((int) VIEWPORT.physicalWidth, (int) VIEWPORT.physicalHeight);
    }

    @Override
    public void pause() {
        //setGameState(GameState.SAVING);
        if (_playerHUD != null)
            _playerHUD.pause();
    }

    @Override
    public void resume() {
        setGameState(GameState.RUNNING);
        if (_playerHUD != null)
            _playerHUD.resume();
    }

    @Override
    public void dispose() {
        if( _player != null ){
            _player.unregisterObservers();
            _player.dispose();
        }

        if( _mapRenderer != null ){
            _mapRenderer.dispose();
        }

        AudioManager.getInstance().dispose();
        MapFactory.clearCache();
    }
}
