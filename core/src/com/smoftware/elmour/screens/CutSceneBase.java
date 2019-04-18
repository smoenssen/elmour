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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
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
import com.smoftware.elmour.maps.Map;
import com.smoftware.elmour.maps.MapManager;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.sfx.ScreenTransitionActor;
import com.smoftware.elmour.sfx.ShakeCamera;

/**
 * Created by steve on 3/2/19.
 */

public class CutSceneBase extends GameScreen {
    private static final String TAG = CutSceneBase.class.getSimpleName();

    /////////////////////////////////////////////
    // Put common cut scene actions here
    public class setOneBlockTime extends Action {
        float time = 0;

        public setOneBlockTime(float time) {
            this.time = time;
        }

        @Override
        public boolean act(float delta) {
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
        public boolean act(float delta) {
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
        public boolean act(float delta) {
            isFading = this.fading;
            return true; // An action returns true when it's completed
        }
    }

    public class setMapRendering extends Action {
        boolean on;

        public setMapRendering(boolean on) {
            this.on = on;
        }

        @Override
        public boolean act (float delta) {
            isMapRendering = this.on;
            return true; // An action returns true when it's completed
        }
    }

    // End common cut scene actions
    /////////////////////////////////////////////////////////

    protected final float V_WIDTH = 18;
    protected final float V_HEIGHT = 12;

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

    protected static GameState _gameState;

    protected OrthogonalTiledMapRenderer _mapRenderer = null;
    protected MapManager _mapMgr;
    protected OrthographicCamera _camera = null;
    protected OrthographicCamera _hudCamera = null;
    protected Vector3 lastCameraPosition;

    protected Json _json;
    protected ElmourGame _game;
    protected InputMultiplexer _multiplexer;
    protected ShakeCamera shakeCam;
    protected boolean keepCamInMap;
    protected boolean isMapRendering;

    protected Entity _player;
    protected PlayerHUD _playerHUD;

    protected Actor _followingActor;
    protected MyActions myActions;

    protected Viewport _viewport;
    protected Stage _stage;
    protected boolean _isCameraFixed = true;
    protected ScreenTransitionActor _transitionActor;
    protected Action _switchScreenToMainAction;
    protected Action switchScreenToElmourAction;

    protected float oneBlockTime = 0;
    protected float closeBook = 0;
    protected float emoteOn = 0;
    protected float emoteOff = 0;
    protected float emoteX = 0.8f;
    protected int emoteY = 1;
    protected float zoomRate = 0;
    protected boolean isFading = false;
    private boolean isFirstTime = true;

    public CutSceneBase(ElmourGame game, PlayerHUD playerHUD) {

        _game = game;
        _playerHUD = playerHUD;
        _mapMgr = new MapManager();
        _json = new Json();

        //_camera setup
        setupViewport(V_WIDTH, V_HEIGHT);
        shakeCam = null;
        lastCameraPosition = new Vector3(0, 0, 0);
        keepCamInMap = true;
        isMapRendering = true;

        setGameState(GameState.RUNNING);

        //get the current size
        _camera = new OrthographicCamera();
        _camera.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);

        _viewport = new FitViewport(ElmourGame.V_WIDTH, ElmourGame.V_HEIGHT, _camera);
        _stage = new Stage(_viewport);

        /*
        if (ElmourGame.isAndroid()) {
            // capture Android back key so it is not passed on to the OS
            Gdx.input.setCatchBackKey(true);

            //NOTE!!! Need to create mobileControls before player because player
            //is an observer of mobileControls
            _player = EntityFactory.getInstance().getEntity(EntityFactory.EntityType.PLAYER);
            _hudCamera = new OrthographicCamera();
            _hudCamera.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);

            _playerHUD = new PlayerHUD(game, _hudCamera, _player, _mapMgr);
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
        */

        //_mapMgr.setPlayer(_player);
        _mapMgr.setCamera(_camera);
        myActions = new MyActions();

        _transitionActor = new ScreenTransitionActor();
        _followingActor = new Actor();

        _switchScreenToMainAction = new
                RunnableAction() {
                    @Override
                    public void run() {
                        _game.setScreen(_game.getScreenType(ElmourGame.ScreenType.MainGame));
                    }
                };
    }

    protected AnimatedImage setEntityAnimation(Entity entity){
        final AnimatedImage animEntity = new AnimatedImage();
        animEntity.setEntity(entity);
        animEntity.setSize(animEntity.getWidth() * Map.UNIT_SCALE, animEntity.getHeight() * Map.UNIT_SCALE);
        return animEntity;
    }

    protected AnimatedImage getAnimatedImage(EntityFactory.EntityName entityName){
        Entity entity = EntityFactory.getInstance().getEntityByName(entityName);
        return setEntityAnimation(entity);
    }

    public void followActor(Actor actor){
        _followingActor = actor;
        _isCameraFixed = false;
    }

    public void setCameraPosition(float x, float y){
        _camera.position.set(x, y, 0f);
        lastCameraPosition = _camera.position.cpy();
        _isCameraFixed = true;
    }

    protected Rectangle getObjectRectangle(MapLayer layer, String objectName) {
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

    public static void setGameState(GameState gameState){
        Gdx.app.log(TAG, "setGameState " + gameState.toString());

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

    protected void setupViewport(float width, float height){
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

    protected void baseShow() {
        isFading = true;
    }

    protected void baseHide() {
        isFirstTime = true;
    }

    protected void baseRender(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        _mapRenderer.setView(_camera);

        _mapRenderer.getBatch().enableBlending();
        _mapRenderer.getBatch().setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if( _mapMgr.hasMapChanged() ){
            _mapRenderer.setMap(_mapMgr.getCurrentTiledMap());
            _mapMgr.setMapChanged(false);
        }

        // isFirstTime and isFading are used to fix an issue with the wrong map being flashed for the first frame
        // todo: is isMapRendering really necessary anymore? (called from cut scenes using setMapRendering)
        // todo: also need to fade in characters
        if (!isFirstTime && isMapRendering) {
            _mapRenderer.render();
        }

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

            if (keepCamInMap) {
                _camera.position.set(MathUtils.clamp(_followingActor.getX() + _followingActor.getWidth() / 2, _camera.viewportWidth / 2f, mapWidthInTiles - (_camera.viewportWidth / 2f)),
                        MathUtils.clamp(_followingActor.getY(), _camera.viewportHeight / 2f, mapHeightInTiles - (_camera.viewportHeight / 2f)), 0f);
            }
            else {
                _camera.position.set(_followingActor.getX() + _followingActor.getWidth() / 2, _followingActor.getY(), _camera.viewportHeight / 2f);
            }
        }

        _camera.zoom += zoomRate;
        _camera.update();

        if (shakeCam != null) {
            if (shakeCam.isCameraShaking()) {

                Vector2 shakeCoords = shakeCam.getNewShakePosition();
                _camera.position.x = shakeCoords.x;
                _camera.position.y = shakeCoords.y;
                _camera.update();
            } else {
                shakeCam.reset();
            }
        }

        _playerHUD.render(delta);

        isFirstTime = false;
    }
}
