package com.smoftware.elmour.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Json;
import com.smoftware.elmour.Component;
import com.smoftware.elmour.ElmourGame;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.EntityFactory;
import com.smoftware.elmour.UI.MobileControls;
import com.smoftware.elmour.UI.PlayerHUD;
import com.smoftware.elmour.audio.AudioManager;
import com.smoftware.elmour.maps.Map;
import com.smoftware.elmour.maps.MapFactory;
import com.smoftware.elmour.maps.MapManager;
import com.smoftware.elmour.profile.ProfileManager;

public class MainGameScreen extends GameScreen {
	private static final String TAG = MainGameScreen.class.getSimpleName();

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

	protected OrthogonalTiledMapRenderer _mapRenderer = null;
	protected MapManager _mapMgr;
	protected OrthographicCamera _camera = null;
	protected OrthographicCamera _hudCamera = null;
	protected OrthographicCamera controllersCam = null;

	private Json _json;
	private ElmourGame _game;
	private InputMultiplexer _multiplexer;

	private Entity _player;
	private PlayerHUD _playerHUD;
	private MobileControls mobileControls;

	public MainGameScreen(ElmourGame game){
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

		if (ElmourGame.isAndroid()) {
			// capture Android back key so it is not passed on to the OS
			Gdx.input.setCatchBackKey(true);

            //NOTE!!! Need to create mobileControls before player because player
            //is an observer of mobileControls
			controllersCam = new OrthographicCamera();
			controllersCam.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);
			mobileControls = new MobileControls(controllersCam);

            _player = EntityFactory.getInstance().getEntity(EntityFactory.EntityType.PLAYER);
			_hudCamera = new OrthographicCamera();
			_hudCamera.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);

			_playerHUD = new PlayerHUD(_hudCamera, _player, _mapMgr);

			_multiplexer = new InputMultiplexer();
			_multiplexer.addProcessor(mobileControls.getStage());
			_multiplexer.addProcessor(_playerHUD.getStage());
			Gdx.input.setInputProcessor(_multiplexer);
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

		_mapMgr.setPlayer(_player);
		_mapMgr.setCamera(_camera);

		//Gdx.app.debug(TAG, "UnitScale value is: " + _mapRenderer.getUnitScale());
	}

	@Override
	public void show() {
		ProfileManager.getInstance().addObserver(_mapMgr);
		if (_playerHUD != null)
			ProfileManager.getInstance().addObserver(_playerHUD);

		setGameState(GameState.LOADING);

		if (ElmourGame.isAndroid()) {
			//Gdx.input.setInputProcessor(mobileControls.getStage());
			Gdx.input.setInputProcessor(_multiplexer);
		}
		else
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
		if( _gameState == GameState.GAME_OVER ){
			_game.setScreen(_game.getScreenType(ElmourGame.ScreenType.GameOver));
		}

		if( _gameState == GameState.PAUSED ){
			_player.updateInput(delta);
			_playerHUD.render(delta);
			return;
		}
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		_mapRenderer.setView(_camera);

		_mapRenderer.getBatch().enableBlending();
		_mapRenderer.getBatch().setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		if( _mapMgr.hasMapChanged() ){
			_mapRenderer.setMap(_mapMgr.getCurrentTiledMap());
			_player.sendMessage(Component.MESSAGE.INIT_START_POSITION, _json.toJson(_mapMgr.getPlayerStartUnitScaled()));

			_camera.position.set(_mapMgr.getPlayerStartUnitScaled().x, _mapMgr.getPlayerStartUnitScaled().y, 0f);
			_camera.update();

			if (_playerHUD != null)
				_playerHUD.updateEntityObservers();

			_mapMgr.setMapChanged(false);

			if (_playerHUD != null)
				_playerHUD.addTransitionToScreen();
		}

		if (_playerHUD != null)
			_mapMgr.updateLightMaps(_playerHUD.getCurrentTimeOfDay());
		TiledMapImageLayer lightMap = (TiledMapImageLayer)_mapMgr.getCurrentLightMapLayer();
		TiledMapImageLayer previousLightMap = (TiledMapImageLayer)_mapMgr.getPreviousLightMapLayer();

		if( lightMap != null) {
			_mapRenderer.getBatch().begin();
			TiledMapTileLayer backgroundMapLayer = (TiledMapTileLayer)_mapMgr.getCurrentTiledMap().getLayers().get(Map.BACKGROUND_LAYER);
			if( backgroundMapLayer != null ){
				_mapRenderer.renderTileLayer(backgroundMapLayer);
			}

			TiledMapTileLayer groundMapLayer = (TiledMapTileLayer)_mapMgr.getCurrentTiledMap().getLayers().get(Map.GROUND_LAYER);
			if( groundMapLayer != null ){
				_mapRenderer.renderTileLayer(groundMapLayer);
			}

			//TiledMapTileLayer decorationMapLayer = (TiledMapTileLayer)_mapMgr.getCurrentTiledMap().getLayers().get(Map.DECORATION_LAYER);
			TiledMapTileLayer decorationMapLayer = (TiledMapTileLayer)_mapMgr.getCurrentTiledMap().getLayers().get("Tree");
			if( decorationMapLayer != null ){
				_mapRenderer.renderTileLayer(decorationMapLayer);
			}

			_mapRenderer.getBatch().end();

			_mapMgr.updateCurrentMapEntities(_mapMgr, _mapRenderer.getBatch(), delta);
			_player.update(_mapMgr, _mapRenderer.getBatch(), delta);
			_mapMgr.updateCurrentMapEffects(_mapMgr, _mapRenderer.getBatch(), delta);

			_mapRenderer.getBatch().begin();
			_mapRenderer.getBatch().setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ONE_MINUS_SRC_ALPHA);

			_mapRenderer.renderImageLayer(lightMap);
			_mapRenderer.getBatch().setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			_mapRenderer.getBatch().end();

			if( previousLightMap != null ){
				_mapRenderer.getBatch().begin();
				_mapRenderer.getBatch().setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ONE_MINUS_SRC_COLOR);
				_mapRenderer.renderImageLayer(previousLightMap);
				_mapRenderer.getBatch().setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
				_mapRenderer.getBatch().end();
			}
		}else{
			_mapRenderer.render();
			_mapMgr.updateCurrentMapEntities(_mapMgr, _mapRenderer.getBatch(), delta);
			if (_player != null)
				_player.update(_mapMgr, _mapRenderer.getBatch(), delta);
			_mapMgr.updateCurrentMapEffects(_mapMgr, _mapRenderer.getBatch(), delta);

			/*////////////////srm - todo - testing character behind objects
			_mapRenderer.getBatch().begin();
			TiledMapTileLayer decorationMapLayer = (TiledMapTileLayer)_mapMgr.getCurrentTiledMap().getLayers().get("Highest");
			if( decorationMapLayer != null ){
				_mapRenderer.renderTileLayer(decorationMapLayer);
			}
			_mapRenderer.getBatch().end();
			*//////////////////////////////
		}

		if (_playerHUD != null)
			_playerHUD.render(delta);

		if (ElmourGame.isAndroid())
			mobileControls.render(delta);
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
			VIEWPORT.viewportWidth = VIEWPORT.viewportHeight * (VIEWPORT.physicalWidth/VIEWPORT.physicalHeight);
			VIEWPORT.viewportHeight = VIEWPORT.virtualHeight;
		}else{
			//letterbox above and below
			VIEWPORT.viewportWidth = VIEWPORT.virtualWidth;
			VIEWPORT.viewportHeight = VIEWPORT.viewportWidth * (VIEWPORT.physicalHeight/VIEWPORT.physicalWidth);
		}

		Gdx.app.debug(TAG, "WorldRenderer: virtual: (" + VIEWPORT.virtualWidth + "," + VIEWPORT.virtualHeight + ")" );
		Gdx.app.debug(TAG, "WorldRenderer: viewport: (" + VIEWPORT.viewportWidth + "," + VIEWPORT.viewportHeight + ")" );
		Gdx.app.debug(TAG, "WorldRenderer: physical: (" + VIEWPORT.physicalWidth + "," + VIEWPORT.physicalHeight + ")" );
	}
}
