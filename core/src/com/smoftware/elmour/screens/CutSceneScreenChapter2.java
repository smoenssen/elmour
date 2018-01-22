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

public class CutSceneScreenChapter2 extends GameScreen implements ConversationGraphObserver {
    private static final String TAG = CutSceneScreenChapter2.class.getSimpleName();

    private final float V_WIDTH = 12;//2.4f;//srm
    private final float V_HEIGHT = 8;//1.6f;

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

    CutSceneScreenChapter2 thisScreen;
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

    private boolean isInConversation = false;

    private Viewport _viewport;
    private Stage _stage;
    private boolean _isCameraFixed = true;
    private ScreenTransitionActor _transitionActor;
    private Action _introCutSceneAction;
    private Action _switchScreenAction;
    private Action setupScene01;
    private Action setupSceneArmory;
    private Action waitForConversationExit;

    private AnimatedImage character1;
    private AnimatedImage character2;

    public CutSceneScreenChapter2(ElmourGame game){
        thisScreen = this;
        _game = game;
        _mapMgr = new MapManager();
        _json = new Json();

        setGameState(GameState.RUNNING);

        //_camera setup
        //setupViewport(V_WIDTH, V_HEIGHT);//srm
        setupViewport(V_WIDTH, V_HEIGHT);

        //get the current size
        _camera = new OrthographicCamera();
        _camera.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);

        _viewport = new ScreenViewport(_camera);
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

            _playerHUD = new PlayerHUD(_hudCamera, _player, _mapMgr);

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

            _playerHUD = new PlayerHUD(_hudCamera, _player, _mapMgr);

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

        _transitionActor = new ScreenTransitionActor();

        _followingActor = new Actor();
        _followingActor.setPosition(0, 0);

        followActor(character2);

        _stage.addActor(character1);
        _stage.addActor(character2);
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
                _mapMgr.loadMap(MapFactory.MapType.Elmour);
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
                _mapMgr.loadMap(MapFactory.MapType.Armory);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(0, 0);

                character1.setVisible(true);
                character1.setPosition(0, 0);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.RIGHT);

                character2.setVisible(true);
                character2.setPosition(1, 1);
                character2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character2.setCurrentDirection(Entity.Direction.LEFT);

                followActor(character2);
            }
        };
    }

    private Action getConversationCutscreenAction() {
        setupScene01.reset();
        return Actions.sequence(
                Actions.addAction(setupScene01),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 3), _transitionActor),
                Actions.delay(2),
                Actions.run(
                        new Runnable() {
                            @Override
                            public void run() {
                                isInConversation = true;
                                _playerHUD.loadConversationForCutScene("conversations/Chapter_2.json", thisScreen);
                                _playerHUD.doConversation();
                                // NOTE: This just kicks off the conversation. The actions in the conversation are handled in the onNotify() function.
                            }
                        }),
                Actions.delay(3)
        );
    }
    private Action getCutsceneAction(){
        setupScene01.reset();
        setupSceneArmory.reset();
        _switchScreenAction.reset();

        return Actions.sequence(
                Actions.addAction(setupScene01),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 3), _transitionActor),
                Actions.delay(3),
                Actions.run(
                        new Runnable() {
                            @Override
                            public void run() {
                                isInConversation = true;
                                _playerHUD.loadConversationForCutScene("conversations/testing.json", thisScreen);
                                _playerHUD.doConversation();
                                //_playerHUD.showMessage("BLACKSMITH: We have planned this long enough. The time is now! I have had enough talk...");
                            }
                        }),
                Actions.delay(3),
                Actions.run(
                        new Runnable() {
                            @Override
                            public void run() {
                                _playerHUD.showMessage("MAGE: This is dark magic you fool. We must proceed with caution, or this could end badly for all of us");
                            }
                        }),
                Actions.delay(3),
                Actions.run(
                        new Runnable() {
                            @Override
                            public void run() {
                                _playerHUD.showMessage("INNKEEPER: Both of you need to keep it down. If we get caught using black magic, we will all be hanged!");
                            }
                        }),
                Actions.delay(5),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 3), _transitionActor),
                Actions.delay(3),
                Actions.addAction(setupSceneArmory),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 3), _transitionActor),
                Actions.delay(3),
                Actions.run(
                        new Runnable() {
                            @Override
                            public void run() {
                                _playerHUD.showMessage("BLACKSMITH: Now, let's get on with this. I don't like the cemeteries very much...");
                            }
                        }
                ),
                Actions.delay(3),
                Actions.run(
                        new Runnable() {
                            @Override
                            public void run() {
                                _playerHUD.showMessage("MAGE: I told you, we can't rush the spell. Bringing someone back to life isn't simple!");
                            }
                        }
                ),
                Actions.delay(3),
                Actions.run(
                        new Runnable() {
                            @Override
                            public void run() {
                                _playerHUD.showMessage("INNKEEPER: I know you loved your daughter, but this just isn't right...");
                            }
                        }
                ),
                Actions.delay(3),
                Actions.run(
                        new Runnable() {
                            @Override
                            public void run() {
                                _playerHUD.showMessage("BLACKSMITH: You have never had a child of your own. You just don't understand!");
                            }
                        }
                ),
                Actions.delay(3),
                Actions.run(
                        new Runnable() {
                            @Override
                            public void run() {
                                _playerHUD.showMessage("MAGE: You both need to concentrate, wait...Oh no, something is wrong!!");
                            }
                        }
                ),

                Actions.delay(2),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 3), _transitionActor),
                Actions.delay(2),
                Actions.after(_switchScreenAction)
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

    public class setWalkDirection extends Action {
        AnimatedImage character = null;
        Entity.AnimationType direction = Entity.AnimationType.IDLE;

        public setWalkDirection(AnimatedImage character, Entity.AnimationType direction) {
            this.character = character;
            this.direction = direction;
        }
        public boolean act (float delta) {
            character.setCurrentAnimationType(direction);
            return true; // An action returns true when it's completed
        }

    }

    @Override
    public void onNotify(ConversationGraph graph, ConversationCommandEvent action) {
        Gdx.app.log(TAG, "Got notification " + action.toString());

        switch (action) {
            case WALK_TO_ARMORY:
                Rectangle rect = getObjectRectangle(_mapMgr.getInteractionLayer(), "ARMORY");

                character2.setCurrentAnimationType(Entity.AnimationType.WALK_UP);

                Gdx.app.log(TAG, String.format("character1 y = %3.2f", character1.getY()));

                _stage.addAction(Actions.sequence(
                        new setWalkDirection(character2, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(character2.getX(), character2.getY() + 1, 0.5f, Interpolation.linear), character2),
                        Actions.delay(0.5f),
                        new setWalkDirection(character2, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveTo(rect.getX() * Map.UNIT_SCALE, character2.getY() + 1, 3, Interpolation.linear), character2),
                        Actions.delay(0.25f),
                        new setWalkDirection(character1, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveTo(rect.getX() * Map.UNIT_SCALE, character1.getY(), 2.75f, Interpolation.linear), character1),
                        Actions.delay(2.75f),
                        new setWalkDirection(character1, Entity.AnimationType.WALK_UP),
                        new setWalkDirection(character2, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(rect.getX() * Map.UNIT_SCALE, rect.getY() * Map.UNIT_SCALE - 1, 2, Interpolation.linear), character1),
                        Actions.addAction(Actions.moveTo(rect.getX() * Map.UNIT_SCALE, rect.getY() * Map.UNIT_SCALE, 2, Interpolation.linear), character2),
                        Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 2), _transitionActor),

                        Actions.delay(2),
                        Actions.addAction(setupSceneArmory),
                        Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 3), _transitionActor))
                );

                break;
            case EXIT_CONVERSATION:
                _stage.addAction(Actions.addAction(Actions.moveTo(15, 76, 10, Interpolation.linear), character2));
                _stage.addAction(Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 3), _transitionActor));
        }

    }

    @Override
    public void onNotify(ConversationGraph graph, ArrayList<ConversationChoice> choices) {
        Gdx.app.log(TAG, "onNotify 2");
    }

    @Override
    public void onNotify(String value, ConversationCommandEvent event) {
        Gdx.app.log(TAG, "onNotify 3");
    }

    @Override
    public void show() {
        _introCutSceneAction = getConversationCutscreenAction();
        _stage.addAction(_introCutSceneAction);

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
        if( _gameState != GameState.GAME_OVER ){
            setGameState(GameState.SAVING);
        }

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
        	_camera.position.set(_followingActor.getX(), _followingActor.getY(), 0f);
        }
        _camera.update();

        _playerHUD.render(delta);

        _stage.act(delta);
        _stage.draw();
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
