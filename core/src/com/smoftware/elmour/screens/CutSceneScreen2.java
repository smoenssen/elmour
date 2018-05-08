package com.smoftware.elmour.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
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

public class CutSceneScreen2 extends GameScreen implements ConversationGraphObserver {
 private static final String TAG = CutSceneScreen2.class.getSimpleName();

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

 CutSceneScreen2 thisScreen;
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
 //private MobileControls mobileControls;

 private boolean isInConversation = false;

 private Viewport _viewport;
 private Stage _stage;
 private boolean _isCameraFixed = true;
 private ScreenTransitionActor _transitionActor;
 private Action _introCutSceneAction;
 private Action _switchScreenAction;
 private Action _setupScene01;
 private Action _setupScene02;
 private Action waitForConversationExit;

 private AnimatedImage _animBlackSmith;
 private AnimatedImage animPlayer;

 public CutSceneScreen2(ElmourGame game){
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

  _animBlackSmith = getAnimatedImage(EntityFactory.EntityName.TOWN_BLACKSMITH);
  animPlayer = getAnimatedImage(EntityFactory.EntityName.TOWN_GUARD_WALKING);

  _transitionActor = new ScreenTransitionActor();

  _stage.addActor(animPlayer);
  _stage.addActor(_animBlackSmith);
  _stage.addActor(_transitionActor);

  //Actions
  _switchScreenAction = new RunnableAction(){
   @Override
   public void run() {
    _game.setScreen(_game.getScreenType(ElmourGame.ScreenType.MainGame));
   }
  };

  _setupScene01 = new RunnableAction() {
   @Override
   public void run() {
    _playerHUD.hideMessage();
    _mapMgr.loadMap(MapFactory.MapType.MAP1);
    _mapMgr.disableCurrentmapMusic();
    setCameraPosition(10, 16);

    animPlayer.setVisible(true);
    animPlayer.setPosition(9, 16);

    _animBlackSmith.setVisible(true);
    _animBlackSmith.setPosition(10, 16);
   }
  };

  _setupScene02 = new RunnableAction() {
   @Override
   public void run() {
    _playerHUD.hideMessage();
    _mapMgr.loadMap(MapFactory.MapType.TOP_WORLD);
    _mapMgr.disableCurrentmapMusic();
    setCameraPosition(50, 30);
    _animBlackSmith.setPosition(50, 30);
   }
  };
 }

 private Action getConversationCutscreenAction() {
  _setupScene01.reset();
  return Actions.sequence(
          Actions.addAction(_setupScene01),
          Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 3), _transitionActor),
          Actions.delay(3),
          Actions.run(
                  new Runnable() {
                   @Override
                   public void run() {
                    isInConversation = true;
                    _playerHUD.loadConversationForCutScene("conversations/Chapter_2.json", thisScreen);
                    _playerHUD.doConversation();
                    //_playerHUD.showMessage("BLACKSMITH: We have planned this long enough. The time is now! I have had enough talk...");
                   }
                  }),
          Actions.delay(3)
  );
 }
 private Action getCutsceneAction(){
  _setupScene01.reset();
  _setupScene02.reset();
  _switchScreenAction.reset();

  return Actions.sequence(
          Actions.addAction(_setupScene01),
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
          Actions.addAction(_setupScene02),
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

 public void setCameraPosition(float x, float y){
  _camera.position.set(x, y, 0f);
  _isCameraFixed = true;
 }

 @Override
 public void onNotify(ConversationGraph graph, ConversationCommandEvent event) {
  switch  (event) {
   case EXIT_CONVERSATION:
    _stage.addAction(Actions.addAction(Actions.moveTo(15, 76, 10, Interpolation.linear), _animBlackSmith));
    _stage.addAction(Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 3), _transitionActor));
  }

 }

 @Override
 public void onNotify(ConversationGraph graph, ConversationCommandEvent event, String conversationId) {

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

  //if( !_isCameraFixed ){
  //	_camera.position.set(_followingActor.getX(), _followingActor.getY(), 0f);
  //}
  _camera.update();

  _playerHUD.render(delta);

  _stage.act(delta);
  _stage.draw();
		/*
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
		}
		else {
			_mapRenderer.render();
			_mapRenderer.getBatch().begin();

			for (int i = 0; i < _mapMgr.getCurrentTiledMap().getLayers().getCount(); i++) {
				// Break out if map has changed in the middle of this loop so layer
				// isn't rendered at incorrect camera position. This fixed issue with
				// a quick flash at map position of previous map being shown.
				if( _mapMgr.hasMapChanged() ){
					break;
				}

				MapLayer mapLayer = _mapMgr.getCurrentTiledMap().getLayers().get(i);

				if (mapLayer != null && mapLayer instanceof TiledMapTileLayer) {
					TiledMapTileLayer layer = (TiledMapTileLayer)mapLayer;

					_mapRenderer.renderTileLayer(layer);

					// render the player on the Z tile layer that matches the player's current Z layer
					if (_player != null) {
						if (layer.getName().equals(MapFactory.getMap(_mapMgr.getCurrentMapType()).getPlayerZLayer())) {
							_mapRenderer.getBatch().end();
							_player.update(_mapMgr, _mapRenderer.getBatch(), delta);
							_mapRenderer.getBatch().begin();
						}
					}
				}
			}

			_mapRenderer.getBatch().end();
			_mapMgr.updateCurrentMapEntities(_mapMgr, _mapRenderer.getBatch(), delta);
			_mapMgr.updateCurrentMapEffects(_mapMgr, _mapRenderer.getBatch(), delta);
		}

		if (_playerHUD != null)
			_playerHUD.render(delta);

		_stage.act(delta);
		_stage.draw();

		if (ElmourGame.isAndroid())
			mobileControls.render(delta);
			*/
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
