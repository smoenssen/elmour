package com.smoftware.elmour.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.FitViewport;
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

public class CutSceneChapter1 extends GameScreen implements ConversationGraphObserver {
    private static final String TAG = CutSceneChapter1.class.getSimpleName();

    private final float V_WIDTH = 18;//2.4f;//srm
    private final float V_HEIGHT = 12;//1.6f;

    public static class VIEWPORT {
        public static float viewportWidth;
        public static float viewportHeight;
        public static float virtualWidth;
        public static float virtualHeight;
        public static float physicalWidth;
        public static float physicalHeight;
        public static float aspectRatio;
    }

    public static enum GameState {
        SAVING,
        LOADING,
        RUNNING,
        PAUSED,
        GAME_OVER
    }
    private static GameState _gameState;

    CutSceneChapter1 thisScreen;
    protected OrthogonalTiledMapRenderer _mapRenderer = null;
    protected MapManager _mapMgr;
    protected OrthographicCamera _camera = null;
    protected OrthographicCamera _hudCamera = null;
    //protected OrthographicCamera controllersCam = null;

    private Json _json;
    private ElmourGame _game;
    private InputMultiplexer _multiplexer;

    private Entity _player;
    private PlayerHUD _playerHUD;

    private Actor _followingActor;
    private MyActions myActions;

    private boolean isInConversation = false;

    private Viewport _viewport;
    private Stage _stage;
    private boolean _isCameraFixed = true;
    private ScreenTransitionActor _transitionActor;
    private Action _switchScreenAction;
    private Action setupScene01;
    private Action setupCastleChaseScene;
    private Action setupCourtyardChaseScene;
    private Action setupGuardsSurroundScene;
    private Action waitForConversationExit;

    private AnimatedImage character1;
    private AnimatedImage character2;
    private AnimatedImage guard1;
    private AnimatedImage guard2;
    private AnimatedImage guard3;
    private AnimatedImage guard4;
    private AnimatedImage guard5;
    private AnimatedImage guard6;
    private AnimatedImage guard7;
    private AnimatedImage guard8;
    private AnimatedImage camactor;

    float oneBlockTime = 0;
    float zoomRate = 0;
    private boolean isFading = false;

    public class setOneBlockTime extends Action {
        float time = 0;

        public setOneBlockTime(float time) {
            this.time = time;
        }

        @Override
        public boolean act (float delta) {
            oneBlockTime = this.time;
            return true; // An action returns true when it's completed
        }
    }

    public class setZoomRate extends Action {
        float rate = 0;

        public setZoomRate(float rate) {
            this.rate = rate;
        }

        @Override
        public boolean act (float delta) {
            zoomRate = this.rate;
            return true; // An action returns true when it's completed
        }
    }

    public class setFading extends Action {
        boolean fading;

        public setFading(boolean fading) {
            this.fading = fading;
        }

        @Override
        public boolean act (float delta) {
            isFading = this.fading;
            return true; // An action returns true when it's completed
        }
    }

    public CutSceneChapter1(ElmourGame game){
        thisScreen = this;
        _game = game;
        _mapMgr = new MapManager();
        _json = new Json();

        setGameState(GameState.RUNNING);

        //_camera setup
        setupViewport(V_WIDTH, V_HEIGHT);

        //get the current size
        _camera = new OrthographicCamera();
        _camera.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);

        _viewport = new FitViewport(ElmourGame.V_WIDTH, ElmourGame.V_HEIGHT, _camera);
        _stage = new Stage(_viewport);

        if (ElmourGame.isAndroid()) {
            // capture Android back key so it is not passed on to the OS
            Gdx.input.setCatchBackKey(true);

            //NOTE!!! Need to create mobileControls before player because player
            //is an observer of mobileControls
            //controllersCam = new OrthographicCamera();
            //controllersCam.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);
            //mobileControls = new MobileControls(controllersCam);

            _player = EntityFactory.getInstance().getEntity(EntityFactory.EntityType.PLAYER);
            _hudCamera = new OrthographicCamera();
            _hudCamera.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);

            _playerHUD = new PlayerHUD(game, _hudCamera, _player, _mapMgr);

            //_multiplexer = new InputMultiplexer();
            //_multiplexer.addProcessor(mobileControls.getStage());
            //_multiplexer.addProcessor(_playerHUD.getStage());
            //Gdx.input.setInputProcessor(_multiplexer);
            Gdx.input.setInputProcessor(_playerHUD.getStage());
        }
        else {
            _player = EntityFactory.getInstance().getEntity(EntityFactory.EntityType.PLAYER);
            _hudCamera = new OrthographicCamera();
            _hudCamera.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);

            _playerHUD = new PlayerHUD(game, _hudCamera, _player, _mapMgr);

            _multiplexer = new InputMultiplexer();
            _multiplexer.addProcessor(_playerHUD.getStage());
            _multiplexer.addProcessor(_player.getInputProcessor());
            Gdx.input.setInputProcessor(_multiplexer);
        }

        _playerHUD.setCutScene(true);

        _mapMgr.setPlayer(_player);
        _mapMgr.setCamera(_camera);

        character1 = getAnimatedImage(EntityFactory.EntityName.CHARACTER_1);
        character2 = getAnimatedImage(EntityFactory.EntityName.CHARACTER_2);
        guard1 = getAnimatedImage(EntityFactory.EntityName.ROYAL_GUARD);
        guard2 = getAnimatedImage(EntityFactory.EntityName.ROYAL_GUARD);
        guard3 = getAnimatedImage(EntityFactory.EntityName.ROYAL_GUARD);
        guard4 = getAnimatedImage(EntityFactory.EntityName.ROYAL_GUARD);
        guard5 = getAnimatedImage(EntityFactory.EntityName.ROYAL_GUARD);
        guard6 = getAnimatedImage(EntityFactory.EntityName.ROYAL_GUARD);
        guard7 = getAnimatedImage(EntityFactory.EntityName.ROYAL_GUARD);
        guard8 = getAnimatedImage(EntityFactory.EntityName.ROYAL_GUARD);
        camactor = getAnimatedImage(EntityFactory.EntityName.STEVE);

        _transitionActor = new ScreenTransitionActor();

        _followingActor = new Actor();
        _followingActor.setPosition(0, 0);

        myActions = new MyActions();

        _stage.addActor(character1);
        _stage.addActor(character2);
        _stage.addActor(guard1);
        _stage.addActor(guard2);
        _stage.addActor(guard3);
        _stage.addActor(guard4);
        _stage.addActor(guard5);
        _stage.addActor(guard6);
        _stage.addActor(guard7);
        _stage.addActor(guard8);

        _stage.addActor(camactor);
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
                _mapMgr.loadMap(MapFactory.MapType.PORTAL_ROOM);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(5, 4.25f);

                camactor.setVisible(false);
                guard1.setVisible(false);
                guard2.setVisible(false);
                guard3.setVisible(false);
                guard4.setVisible(false);
                guard5.setVisible(false);
                guard6.setVisible(false);
                guard7.setVisible(false);
                guard8.setVisible(false);


                character1.setVisible(true);
                character1.setPosition(5.5f, 1.5f);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.DOWN);

                character2.setVisible(false);
                character2.setPosition(7, 10);
                character2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character2.setCurrentDirection(Entity.Direction.LEFT);
            }
        };

        setupCastleChaseScene = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.CASTLE);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(10, 10.5f);

                character1.setVisible(true);
                character1.setPosition(0, 10.5f);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.RIGHT);

                character2.setVisible(false);
                character2.setPosition(0, 10.5f);
                character2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character2.setCurrentDirection(Entity.Direction.RIGHT);

                followActor(character1);
            }
        };

        setupCourtyardChaseScene = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.COURTYARD);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(16, 9);

                camactor.setVisible(false);
                camactor.setPosition(16, 9);
                camactor.setCurrentAnimationType(Entity.AnimationType.IDLE);
                camactor.setCurrentDirection(Entity.Direction.DOWN);

                character1.setVisible(true);
                character1.setPosition(16, 17);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.DOWN);

                character2.setVisible(true);
                character2.setPosition(16, 17);
                character2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character2.setCurrentDirection(Entity.Direction.DOWN);

                followActor(camactor);
            }
        };

        setupGuardsSurroundScene = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.ELMOUR);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(16, 9);

                character1.setVisible(true);
                character1.setPosition(37.5f, 43);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.DOWN);

                character2.setVisible(false);
                character2.setPosition(37.5f, 43);
                character2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character2.setCurrentDirection(Entity.Direction.DOWN);

                guard1.setVisible(true);
                guard1.setPosition(28, 22);
                guard1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                guard1.setCurrentDirection(Entity.Direction.UP);

                guard2.setVisible(true);
                guard2.setPosition(37.5f, 14);
                guard2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                guard2.setCurrentDirection(Entity.Direction.UP);

                guard3.setVisible(true);
                guard3.setPosition(47, 22);
                guard3.setCurrentAnimationType(Entity.AnimationType.IDLE);
                guard3.setCurrentDirection(Entity.Direction.UP);

                guard4.setVisible(false);
                guard4.setPosition(35, 35);
                guard4.setCurrentAnimationType(Entity.AnimationType.IDLE);
                guard4.setCurrentDirection(Entity.Direction.DOWN);

                guard5.setVisible(false);
                guard5.setPosition(37.5f, 35);
                guard5.setCurrentAnimationType(Entity.AnimationType.IDLE);
                guard5.setCurrentDirection(Entity.Direction.DOWN);

                guard6.setVisible(false);
                guard6.setPosition(40, 35);
                guard6.setCurrentAnimationType(Entity.AnimationType.IDLE);
                guard6.setCurrentDirection(Entity.Direction.DOWN);

                guard7.setVisible(false);
                guard7.setPosition(31.5f, 29);
                guard7.setCurrentAnimationType(Entity.AnimationType.IDLE);
                guard7.setCurrentDirection(Entity.Direction.RIGHT);

                guard8.setVisible(false);
                guard8.setPosition(43.5f, 29);
                guard8.setCurrentAnimationType(Entity.AnimationType.IDLE);
                guard8.setCurrentDirection(Entity.Direction.LEFT);

                followActor(character1);
            }
        };

    }

    @Override
    public void onNotify(ConversationGraph graph, ConversationCommandEvent action, String conversationId) {
        Gdx.app.log(TAG, "Got notification " + action.toString());
        oneBlockTime = 0.3f;

        switch (action) {
            case WAIT_1000:
                _playerHUD.doConversation(graph.getNextConversationIDFromChoice(conversationId, 0), 1000 * 1.25f);
                break;
            case WAIT_10000:
                _playerHUD.doConversation(graph.getNextConversationIDFromChoice(conversationId, 0), 10000);
                break;
            case LOOK_AROUND:
                _stage.addAction(Actions.sequence(
                        myActions.new setIdleDirection(character1, Entity.Direction.LEFT),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setIdleDirection(character1, Entity.Direction.UP),
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setIdleDirection(character1, Entity.Direction.LEFT),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case WALK_TO_MIRROR:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveTo(3, character1.getY(), oneBlockTime * 2.5f), character1),
                        Actions.delay(oneBlockTime * 2.5f),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(3, 3, oneBlockTime * 1.5f), character1),
                        Actions.delay(oneBlockTime * 1.5f),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveTo(2, 3, oneBlockTime), character1),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime * 4),
                        //Look around
                        myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setIdleDirection(character1, Entity.Direction.UP),
                        Actions.delay(oneBlockTime * 4),
                        myActions.new setIdleDirection(character1, Entity.Direction.LEFT),
                        Actions.delay(oneBlockTime * 6),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case JUMP_BACK:
                _stage.addAction(Actions.sequence(
                        //Stutter
                        Actions.addAction(Actions.moveBy(0.05f, 0, 0.0025f), character1),
                        Actions.delay(0.0025f),
                        Actions.addAction(Actions.moveBy(-0.10f, 0, 0.005f), character1),
                        Actions.delay(0.005f),
                        Actions.addAction(Actions.moveBy(0.05f, 0, 0.0025f), character1),
                        Actions.delay(0.75f),
                        //Jump
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveTo(3.5f, 4, 0.1f), character1),
                        Actions.delay(0.1f),



                        Actions.addAction(Actions.rotateBy(90, oneBlockTime), character1),
                        Actions.addAction(Actions.moveBy(0, -0.5f, oneBlockTime), character1),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                        //Get up
                        Actions.delay(1),
                        Actions.addAction(Actions.rotateBy(-90, oneBlockTime), character1),
                        Actions.addAction(Actions.moveTo(3, 3.5f, oneBlockTime), character1),
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setIdleDirection(character1, Entity.Direction.LEFT),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setIdleDirection(character1, Entity.Direction.UP),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case CHASE_SEQUENCE:
                oneBlockTime = 0.3f;
                _stage.addAction(
                        Actions.parallel(
                                character1Chase(),
                                character2Chase()
                        )
                );
                break;
            case GUARDS_SURROUND:
                _stage.addAction(
                        Actions.parallel(
                                charactersGuardSurround(),
                                guardsSurround()
                        )
                );
                break;
            case GUARDS_STOP:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setIdleDirection(character1, Entity.Direction.UP),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new setWalkDirection(guard5, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveBy(0, 1, oneBlockTime), guard5),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(guard5, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveBy(1, 0, oneBlockTime), guard5),
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(guard5, Entity.Direction.LEFT),
                        myActions.new setWalkDirection(guard5, Entity.AnimationType.IDLE),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveTo(37.5f, 30, oneBlockTime * 3), character2),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case EXIT_CONVERSATION:
                _stage.addAction(Actions.addAction(Actions.moveTo(15, 76, 10, Interpolation.linear), character2));
                _stage.addAction(Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 3), _transitionActor));
        }

    }

    private Action character1Chase() {
        return Actions.sequence(

                Actions.delay(oneBlockTime * 2),
                Actions.addAction(Actions.moveBy(0, 0.15f, oneBlockTime, Interpolation.exp10Out), character1),
                Actions.addAction(Actions.moveBy(0, -0.15f, oneBlockTime, Interpolation.exp10In), character1),
                Actions.delay(oneBlockTime),


                new setOneBlockTime(0.2f),
                myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_DOWN),
                Actions.addAction(Actions.moveTo(3, 1.5f, oneBlockTime * 2), character1),
                Actions.delay(oneBlockTime * 2),
                myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_RIGHT),
                Actions.addAction(Actions.moveTo(5, 1.5f, oneBlockTime * 2), character1),
                Actions.delay(oneBlockTime * 2),
                myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_UP),
                Actions.addAction(Actions.moveTo(5, 4, oneBlockTime * 2.5f), character1),
                Actions.delay(oneBlockTime * 2.5f),
                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                Actions.delay(oneBlockTime * 5),

                new setZoomRate(-0.002f),
                Actions.delay(oneBlockTime * 7.7f),

                //Look around
                myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),
                Actions.delay(oneBlockTime),
                myActions.new setIdleDirection(character1, Entity.Direction.LEFT),
                Actions.delay(oneBlockTime),

                new setZoomRate(0),

                myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                Actions.delay(oneBlockTime * 2.75f),
                new setZoomRate(0.0616f),

                Actions.delay(oneBlockTime * 0.25f),
                new setZoomRate(0),

                Actions.addAction(Actions.moveBy(0, 0.15f, oneBlockTime, Interpolation.exp10Out), character1),
                Actions.addAction(Actions.moveBy(0, -0.15f, oneBlockTime, Interpolation.exp10In), character1),
                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_RIGHT),
                //RUN
                            // todo dstfgeriushntvwoeishoahgeiohgaeoriuheoudivhfoiuqenpitjeriug
                Actions.addAction(Actions.rotateBy(9000, oneBlockTime * 100), character1),
                            // todo dstfgeriushntvwoeishoahgeiohgaeoriuheoudivhfoiuqenpitjeriug


                Actions.addAction(Actions.moveTo(6, 4, oneBlockTime * 0.25f), character1),
                Actions.delay(oneBlockTime * 0.25f),
                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_DOWN),
                Actions.addAction(Actions.moveTo(6, 1.5f, oneBlockTime * 1.125f), character1),
                Actions.delay(oneBlockTime * 1.125f),
                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_LEFT),
                Actions.addAction(Actions.moveTo(3, 1.5f, oneBlockTime * 0.75f), character1),
                Actions.delay(oneBlockTime * 0.75f),
                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_UP),
                Actions.addAction(Actions.moveTo(3, 7, oneBlockTime * 1.375f), character1),
                Actions.delay(oneBlockTime * 1.375f),
                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_RIGHT),
                Actions.addAction(Actions.moveTo(4, 7, oneBlockTime * 0.25f), character1),
                Actions.delay(oneBlockTime * 0.25f),
                Actions.addAction(Actions.moveTo(7, 10, oneBlockTime * 1.05f), character1),
                Actions.delay(oneBlockTime * 1.05f),
                myActions.new setCharacterVisible(character1, false),
                Actions.delay(oneBlockTime)
                );
    }

    private Action character2Chase() {
        return Actions.sequence(
                myActions.new setCharacterVisible(character2, true),
                Actions.delay(oneBlockTime * 3),
                myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_LEFT),
                Actions.addAction(Actions.moveTo(4, 7, oneBlockTime * 4.2f), character2),
                Actions.delay(oneBlockTime * 4.2f),
                Actions.addAction(Actions.moveTo(3, 7, oneBlockTime), character2),
                Actions.delay(oneBlockTime),
                myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_DOWN),
                Actions.addAction(Actions.moveTo(3, 1.5f, oneBlockTime * 7), character2),
                Actions.delay(oneBlockTime * 7),
                myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_RIGHT),
                Actions.addAction(Actions.moveTo(5, 1.5f, oneBlockTime * 3), character2),
                Actions.delay(oneBlockTime * 3),
                myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_UP),
                Actions.addAction(Actions.moveTo(5, 3.5f, oneBlockTime * 10), character2),
                Actions.delay(oneBlockTime * 10),
                myActions.new setIdleDirection(character2, Entity.Direction.UP),
                myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),
                Actions.delay(oneBlockTime * 0.75f),
                myActions.new setIdleDirection(character2, Entity.Direction.DOWN),
                //RUNRUNRUN
                myActions.new setWalkDirection(character2, Entity.AnimationType.RUN_DOWN),
                Actions.addAction(Actions.moveTo(5, 1.5f, oneBlockTime * 1.125f), character2),
                Actions.delay(oneBlockTime * 1.125f),
                myActions.new setWalkDirection(character2, Entity.AnimationType.RUN_LEFT),
                Actions.addAction(Actions.moveTo(3, 1.5f, oneBlockTime * 0.75f), character2),
                Actions.delay(oneBlockTime * 0.75f),
                myActions.new setWalkDirection(character2, Entity.AnimationType.RUN_UP),
                Actions.addAction(Actions.moveTo(3, 7, oneBlockTime * 1.375f), character2),
                Actions.delay(oneBlockTime * 1.375f),
                myActions.new setWalkDirection(character2, Entity.AnimationType.RUN_RIGHT),
                Actions.addAction(Actions.moveTo(4, 7, oneBlockTime * 0.25f), character2),
                Actions.delay(oneBlockTime * 0.25f),

                myActions.new setWalkDirection(character2, Entity.AnimationType.RUN_RIGHT),
                Actions.addAction(Actions.moveTo(7, 10, oneBlockTime * 1.05f), character2),
                Actions.delay(oneBlockTime * 1.05f),

                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 0.25f), _transitionActor),
                new setFading(true),
                myActions.new setCharacterVisible(character2, false),
                Actions.delay(oneBlockTime),

                Actions.addAction(getCastleChaseScene())
        );
    }

    private Action character1Chase2() {
        oneBlockTime = 0.2f;
        return Actions.sequence(
                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_RIGHT),
                Actions.addAction(Actions.moveTo(3.5f, 10.5f, oneBlockTime * 1.75f), character1),
                Actions.delay(oneBlockTime * 1.75f),




                                //todo tueahfyvsdfhgoysenrg8s8ghergiurnaehrGEAiug
                //Actions.addAction(Actions.rotateBy(90000, oneBlockTime * 100), character1),
                                //todo weurosjfndzcvuiweodrcuerhnvouienteioshusei




                Actions.addAction(Actions.moveTo(10, 13.5f, oneBlockTime * 1.5f), camactor),

                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_UP),
                Actions.addAction(Actions.moveTo(3.5f, 13.5f, oneBlockTime * 1.5f), character1),
                Actions.delay(oneBlockTime * 1.5f),
                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_RIGHT),
                Actions.addAction(Actions.moveTo(15, 13.5f, oneBlockTime * 5.75f), character1),
                Actions.delay(oneBlockTime * 5.75f),

                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                Actions.delay(oneBlockTime),
                myActions.new setIdleDirection(character1, Entity.Direction.UP),
                Actions.delay(oneBlockTime),
                myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                Actions.delay(oneBlockTime * 2),
                myActions.new setIdleDirection(character1, Entity.Direction.LEFT),
                Actions.delay(oneBlockTime * 2),

                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_DOWN),
                Actions.addAction(Actions.moveTo(15, 4.5f, oneBlockTime * 4.5f), character1),
                Actions.delay(oneBlockTime * 4.5f),

                myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                Actions.delay(oneBlockTime * 2),
                myActions.new setIdleDirection(character1, Entity.Direction.UP),
                Actions.delay(oneBlockTime * 2),
                myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                Actions.delay(oneBlockTime * 2),

                //JUMP
                myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_DOWN),
                Actions.addAction(Actions.moveTo(15, 5.5f, oneBlockTime * 2, Interpolation.exp10Out), character1),
                Actions.delay(oneBlockTime * 2),

                Actions.addAction(Actions.moveTo(15, -1.5f, oneBlockTime * 2, Interpolation.exp10In), character1),
                Actions.delay(oneBlockTime * 2),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 0.25f), _transitionActor),
                new setFading(true),
                Actions.delay(0.5f),

                Actions.addAction(getCourtyardChaseScene())
        );
    }

    private Action character2Chase2() {
        oneBlockTime = 0.3f;
        return Actions.sequence(
                Actions.delay(oneBlockTime * 3),
                myActions.new setCharacterVisible(character2, true),
                myActions.new setWalkDirection(character2, Entity.AnimationType.RUN_RIGHT),
                Actions.addAction(Actions.moveTo(3.5f, 10.5f, oneBlockTime * 1.75f), character2),
                Actions.delay(oneBlockTime * 1.75f),

                myActions.new setWalkDirection(character2, Entity.AnimationType.RUN_UP),
                Actions.addAction(Actions.moveTo(3.5f, 13.5f, oneBlockTime * 1.5f), character2),
                Actions.delay(oneBlockTime * 1.5f),
                myActions.new setWalkDirection(character2, Entity.AnimationType.RUN_RIGHT),
                Actions.addAction(Actions.moveTo(15, 13.5f, oneBlockTime * 5.75f), character2),
                Actions.delay(oneBlockTime * 5.75f),
                myActions.new setWalkDirection(character2, Entity.AnimationType.RUN_DOWN),
                Actions.addAction(Actions.moveTo(15, 4.5f, oneBlockTime * 9.5f), character2),
                Actions.delay(oneBlockTime * 9.5f)
        );
    }

    private Action character1Chase3() {
        oneBlockTime = 0.2f;
        return Actions.sequence(
                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                Actions.addAction(Actions.moveTo(16, 10, oneBlockTime * 1.75f, Interpolation.exp10In), character1),
                Actions.delay(oneBlockTime * 1.75f),
                Actions.addAction(Actions.rotateBy(360, oneBlockTime), character1),
                Actions.delay(oneBlockTime * 3),
                Actions.addAction(Actions.rotateBy(-360, oneBlockTime), character1),
                Actions.delay(oneBlockTime),

                myActions.new setIdleDirection(character1, Entity.Direction.UP),
                Actions.delay(oneBlockTime * 2),
                Actions.addAction(Actions.moveBy(0, 0.15f, oneBlockTime, Interpolation.exp10Out), character1),
                Actions.addAction(Actions.moveBy(0, -0.15f, oneBlockTime, Interpolation.exp10In), character1),
                Actions.delay(oneBlockTime * 2),
                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_DOWN),
                Actions.addAction(Actions.moveTo(16, -2, oneBlockTime * 6), character1),
                Actions.delay(oneBlockTime * 6)
        );
    }

    private Action character2Chase3() {
        oneBlockTime = 0.2f;
        return Actions.sequence(
                Actions.delay(oneBlockTime * 11.75f),
                myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),
                Actions.addAction(Actions.moveTo(16, 10, oneBlockTime * 1.75f, Interpolation.exp10In), character2),
                Actions.delay(oneBlockTime * 2.75f),
                myActions.new setWalkDirection(character2, Entity.AnimationType.RUN_DOWN),
                Actions.addAction(Actions.moveTo(16, -2, oneBlockTime * 6), character2),

                Actions.addAction(Actions.moveTo(16, 8, oneBlockTime * 3), camactor),
                Actions.delay(oneBlockTime * 2),

                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 0.25f), _transitionActor),
                new setFading(true),
                Actions.delay(oneBlockTime),

                Actions.addAction(getGuardsSurroundScene())

        );
    }

    private Action charactersGuardSurround() {
        oneBlockTime = 0.4f;
        return Actions.sequence(
                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_DOWN),
                Actions.addAction(Actions.moveTo(37.5f, 24, oneBlockTime * 1.5f), character1),
                Actions.delay(oneBlockTime * 1.5f),

                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                Actions.delay(oneBlockTime),
                myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_DOWN),
                Actions.addAction(Actions.moveTo(37.5f, 25, oneBlockTime * 2), character1),
                Actions.delay(oneBlockTime * 2),
                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_UP),
                Actions.addAction(Actions.moveTo(37.5f, 30, oneBlockTime * 1.5f), character1),
                Actions.delay(oneBlockTime * 1.5f),

                myActions.new setIdleDirection(character1, Entity.Direction.UP),
                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                Actions.delay(oneBlockTime),
                myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_UP),
                Actions.addAction(Actions.moveTo(37.5f, 29, oneBlockTime * 2), character1),
                Actions.delay(oneBlockTime * 2),
                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_LEFT),
                Actions.addAction(Actions.moveTo(35, 29, oneBlockTime * 1.25f), character1),
                Actions.delay(oneBlockTime * 1.25f),

                myActions.new setIdleDirection(character1, Entity.Direction.LEFT),
                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                Actions.delay(oneBlockTime),
                myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_LEFT),
                Actions.addAction(Actions.moveTo(37.5f, 29, oneBlockTime * 6), character1),
                Actions.delay(oneBlockTime * 6),
                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                Actions.delay(oneBlockTime * 2),
                myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),
                Actions.delay(oneBlockTime * 2),

                myActions.new setCharacterVisible(character2, true),
                myActions.new setWalkDirection(character2, Entity.AnimationType.RUN_DOWN),
                Actions.addAction(Actions.moveTo(37.5f, 37), character2),
                Actions.addAction(Actions.moveTo(37.5f, 32, oneBlockTime * 3), character2),
                myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                Actions.delay(oneBlockTime * 3),
                myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),

                myActions.new continueConversation(_playerHUD)
        );
    }

    private Action guardsSurround() {
        oneBlockTime = 0.4f;
        return Actions.sequence(
                myActions.new setWalkDirection(guard1, Entity.AnimationType.RUN_RIGHT),
                Actions.addAction(Actions.moveTo(35, 22, oneBlockTime * 3), guard1),
                myActions.new setWalkDirection(guard2, Entity.AnimationType.RUN_UP),
                Actions.addAction(Actions.moveTo(37.5f, 22, oneBlockTime * 3), guard2),
                myActions.new setWalkDirection(guard3, Entity.AnimationType.RUN_LEFT),
                Actions.addAction(Actions.moveTo(40, 22, oneBlockTime * 3), guard3),
                Actions.delay(oneBlockTime * 3),

                myActions.new setWalkDirection(guard1, Entity.AnimationType.WALK_UP),
                Actions.addAction(Actions.moveTo(35.5f, 26, oneBlockTime * 11), guard1),
                myActions.new setWalkDirection(guard2, Entity.AnimationType.WALK_UP),
                Actions.addAction(Actions.moveTo(37.5f, 26, oneBlockTime * 11), guard2),
                myActions.new setWalkDirection(guard3, Entity.AnimationType.WALK_UP),
                Actions.addAction(Actions.moveTo(39.5f, 26, oneBlockTime * 11), guard3),

                myActions.new setCharacterVisible(guard4, true),
                myActions.new setCharacterVisible(guard5, true),
                myActions.new setCharacterVisible(guard6, true),

                myActions.new setWalkDirection(guard4, Entity.AnimationType.WALK_DOWN),
                Actions.addAction(Actions.moveTo(35.5f, 32, oneBlockTime * 11), guard4),
                myActions.new setWalkDirection(guard5, Entity.AnimationType.WALK_DOWN),
                Actions.addAction(Actions.moveTo(37.5f, 32, oneBlockTime * 11), guard5),
                myActions.new setWalkDirection(guard6, Entity.AnimationType.WALK_DOWN),
                Actions.addAction(Actions.moveTo(39.5f, 32, oneBlockTime * 11), guard6),
                Actions.delay(oneBlockTime * 6),

                myActions.new setCharacterVisible(guard7, true),
                myActions.new setCharacterVisible(guard8, true),

                myActions.new setWalkDirection(guard7, Entity.AnimationType.WALK_RIGHT),
                Actions.addAction(Actions.moveTo(34.5f, 29, oneBlockTime * 5), guard7),
                myActions.new setWalkDirection(guard8, Entity.AnimationType.WALK_RIGHT),
                Actions.addAction(Actions.moveTo(40.5f, 29, oneBlockTime * 5), guard8),
                Actions.delay(oneBlockTime * 5),

                myActions.new setWalkDirection(guard1, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard2, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard3, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard4, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard5, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard6, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard7, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard8, Entity.AnimationType.IDLE),

                Actions.addAction(guardInchForward()),
                Actions.delay(oneBlockTime * 3),
                Actions.addAction(guardInchForward()),
                Actions.delay(oneBlockTime * 3),
                Actions.addAction(guardInchForward())
                );
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

    private Action guardInchForward() {
        return Actions.sequence(
                myActions.new setWalkDirection(guard1, Entity.AnimationType.WALK_UP),
                Actions.addAction(Actions.moveBy(0.3f, 0.6f, oneBlockTime * 2), guard1),

                myActions.new setWalkDirection(guard2, Entity.AnimationType.WALK_UP),
                Actions.addAction(Actions.moveBy(0, 0.6f, oneBlockTime * 2), guard2),

                myActions.new setWalkDirection(guard3, Entity.AnimationType.WALK_UP),
                Actions.addAction(Actions.moveBy(-0.3f, 0.6f, oneBlockTime * 2), guard3),

                myActions.new setWalkDirection(guard4, Entity.AnimationType.WALK_DOWN),
                Actions.addAction(Actions.moveBy(0.3f, -0.6f, oneBlockTime * 2), guard4),

                myActions.new setWalkDirection(guard5, Entity.AnimationType.WALK_DOWN),
                Actions.addAction(Actions.moveBy(0, -0.6f, oneBlockTime * 2), guard5),

                myActions.new setWalkDirection(guard6, Entity.AnimationType.WALK_DOWN),
                Actions.addAction(Actions.moveBy(-0.3f, -0.6f, oneBlockTime * 2), guard6),

                myActions.new setWalkDirection(guard7, Entity.AnimationType.WALK_RIGHT),
                Actions.addAction(Actions.moveBy(0.6f, 0, oneBlockTime * 2), guard7),

                myActions.new setWalkDirection(guard8, Entity.AnimationType.WALK_LEFT),
                Actions.addAction(Actions.moveBy(-0.6f, 0, oneBlockTime * 2), guard8),
                Actions.delay(oneBlockTime * 2),

                myActions.new setWalkDirection(guard1, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard2, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard3, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard4, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard5, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard6, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard7, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard8, Entity.AnimationType.IDLE)
                );
    }

    private Action getOpeningCutSceneAction() {
        setupScene01.reset();
        return Actions.sequence(
                Actions.addAction(setupScene01),
                new setFading(true),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 3), _transitionActor),
                Actions.delay(3),
                new setFading(false),
                Actions.run(
                        new Runnable() {
                            @Override
                            public void run() {
                                isInConversation = true;
                                _playerHUD.loadConversationForCutScene("RPGGame/maps/Game/Text/Dialog/Chapter_1.json", thisScreen);
                                _playerHUD.doConversation();
                                // NOTE: This just kicks off the conversation. The actions in the conversation are handled in the onNotify() function.
                            }
                        }),
                Actions.delay(3)
        );
    }

    private Action getCastleChaseScene(){
        setupCastleChaseScene.reset();
        return Actions.sequence(
                Actions.addAction(setupCastleChaseScene),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 0.5f), _transitionActor),
                Actions.delay(0.5f),
                new setFading(false),
                Actions.parallel(
                        character1Chase2(),
                        character2Chase2()
                )


                /*myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_UP),
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
                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE)*/
                //Actions.delay(3f),
                //Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 2), _transitionActor)

        );
    }

    private Action getCourtyardChaseScene() {
        setupCourtyardChaseScene.reset();
        return Actions.sequence(
                Actions.addAction(setupCourtyardChaseScene),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 0.5f), _transitionActor),
                Actions.delay(0.5f),
                new setFading(false),
                Actions.parallel(
                        character1Chase3(),
                        character2Chase3()
                )
        );
    }

    private Action getGuardsSurroundScene() {
        setupGuardsSurroundScene.reset();
        return Actions.sequence(
                Actions.addAction(setupGuardsSurroundScene),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 0.5f), _transitionActor),
                Actions.delay(0.5f),
                new setFading(false),
                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_DOWN),
                Actions.addAction(Actions.moveTo(37.5f, 27, oneBlockTime * 13), character1),

                myActions.new setEnabledHUD(_playerHUD, false),

                // uncomment to start right from guard surround scene
                // also need to change currentConversationID in the json file to n4
                myActions.new loadConversation(_playerHUD, "RPGGame/maps/Game/Text/Dialog/Chapter_1.json", thisScreen),

                myActions.new continueConversation(_playerHUD),
                Actions.delay(oneBlockTime * 13),
                myActions.new continueConversation(_playerHUD),
                myActions.new setEnabledHUD(_playerHUD, true)
                );
    }

    private AnimatedImage setEntityAnimation(Entity entity){
        final AnimatedImage animEntity = new AnimatedImage();
        animEntity.setEntity(entity);
        animEntity.setSize(animEntity.getWidth() * Map.UNIT_SCALE, animEntity.getHeight() * Map.UNIT_SCALE);
        return animEntity;
    }

    private AnimatedImage getAnimatedImage(EntityFactory.EntityName entityName){
        Entity entity = EntityFactory.getInstance().getEntityByName(entityName);
        return setEntityAnimation(entity);
    }

    public void followActor(Actor actor){
        _followingActor = actor;
        _isCameraFixed = false;
    }

    public void setCameraPosition(float x, float y){
        _camera.position.set(x, y, 0f);
        _isCameraFixed = true;
    }

    private Rectangle getObjectRectangle(MapLayer layer, String objectName) {
        if (layer == null) {
            return null;
        }

        Rectangle rectangle = null;

        for( MapObject object: layer.getObjects()){
            if(object instanceof RectangleMapObject) {
                if (object.getName().equals(objectName)) {
                    rectangle = ((RectangleMapObject)object).getRectangle();
                }
            }
        }

        return rectangle;
    }

    @Override
    public void show() {
        _stage.addAction(getCourtyardChaseScene());

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

        for (int i = 0; i < _mapMgr.getCurrentTiledMap().getLayers().getCount(); i++) {
            MapLayer mapLayer = _mapMgr.getCurrentTiledMap().getLayers().get(i);

            if (mapLayer != null && mapLayer instanceof TiledMapTileLayer) {
                TiledMapTileLayer layer = (TiledMapTileLayer) mapLayer;

                // render the stage on the ZDOWN tile layer
                if (layer.getName().equals("ZDOWN")) {
                    _stage.act(delta);
                    _stage.draw();
                } else if (layer.isVisible() && !isFading) { // don't render the layer if it's not visible
                    _mapRenderer.getBatch().begin();
                    _mapRenderer.renderTileLayer(layer);
                    _mapRenderer.getBatch().end();
                }
            }
        }

        if( !_isCameraFixed ){
            TiledMap map = _mapMgr.getCurrentTiledMap();
            MapProperties prop = map.getProperties();
            int mapWidthInTiles = prop.get("width", Integer.class);
            int mapHeightInTiles = prop.get("height", Integer.class);

            _camera.position.set(MathUtils.clamp(_followingActor.getX() + _followingActor.getWidth()/2, _camera.viewportWidth / 2f, mapWidthInTiles - (_camera.viewportWidth  / 2f)),
                    MathUtils.clamp(_followingActor.getY(), _camera.viewportHeight / 2f, mapHeightInTiles - (_camera.viewportHeight / 2f)), 0f);

        }

        _camera.zoom += zoomRate;
        _camera.update();

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

    public static void setGameState(GameState gameState){
        switch(gameState){
            case RUNNING:
                _gameState = GameState.RUNNING;
                break;
            case LOADING:
                ProfileManager.getInstance().loadProfile();
                _gameState = GameState.RUNNING;
                break;
            case SAVING:
                ProfileManager.getInstance().saveProfile();
                _gameState = GameState.PAUSED;
                break;
            case PAUSED:
                if( _gameState == GameState.PAUSED ){
                    _gameState = GameState.RUNNING;
                }else if( _gameState == GameState.RUNNING ){
                    _gameState = GameState.PAUSED;
                }
                break;
            case GAME_OVER:
                _gameState = GameState.GAME_OVER;
                break;
            default:
                _gameState = GameState.RUNNING;
                break;
        }
    }

    private void setupViewport(float width, float height){
        //Make the viewport a percentage of the total display area
        VIEWPORT.virtualWidth = width;
        VIEWPORT.virtualHeight = height;

        //Current viewport dimensions
        VIEWPORT.viewportWidth = VIEWPORT.virtualWidth;
        VIEWPORT.viewportHeight = VIEWPORT.virtualHeight;

        //pixel dimensions of display
        VIEWPORT.physicalWidth = Gdx.graphics.getWidth();
        VIEWPORT.physicalHeight = Gdx.graphics.getHeight();

        //aspect ratio for current viewport
        VIEWPORT.aspectRatio = (VIEWPORT.virtualWidth / VIEWPORT.virtualHeight);

        //update viewport if there could be skewing
        if( VIEWPORT.physicalWidth / VIEWPORT.physicalHeight >= VIEWPORT.aspectRatio){
            //Letterbox left and right
            VIEWPORT.viewportWidth = VIEWPORT.viewportHeight * (VIEWPORT.physicalWidth/ VIEWPORT.physicalHeight);
            VIEWPORT.viewportHeight = VIEWPORT.virtualHeight;
        }else{
            //letterbox above and below
            VIEWPORT.viewportWidth = VIEWPORT.virtualWidth;
            VIEWPORT.viewportHeight = VIEWPORT.viewportWidth * (VIEWPORT.physicalHeight/ VIEWPORT.physicalWidth);
        }

        Gdx.app.debug(TAG, "WorldRenderer: virtual: (" + VIEWPORT.virtualWidth + "," + VIEWPORT.virtualHeight + ")" );
        Gdx.app.debug(TAG, "WorldRenderer: viewport: (" + VIEWPORT.viewportWidth + "," + VIEWPORT.viewportHeight + ")" );
        Gdx.app.debug(TAG, "WorldRenderer: physical: (" + VIEWPORT.physicalWidth + "," + VIEWPORT.physicalHeight + ")" );
    }
}
