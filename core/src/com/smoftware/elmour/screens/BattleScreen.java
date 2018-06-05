package com.smoftware.elmour.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
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
import com.smoftware.elmour.UI.BattleControls;
import com.smoftware.elmour.UI.BattleHUD;
import com.smoftware.elmour.audio.AudioManager;
import com.smoftware.elmour.battle.MonsterFactory;
import com.smoftware.elmour.maps.Map;
import com.smoftware.elmour.maps.MapFactory;
import com.smoftware.elmour.maps.MapManager;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.sfx.ScreenTransitionAction;
import com.smoftware.elmour.sfx.ScreenTransitionActor;

/**
 * Created by steve on 3/2/18.
=======
import com.smoftware.elmour.ElmourGame;

/**
 * Created by moenssr on 3/1/2018.
>>>>>>> Stashed changes
 */

public class BattleScreen extends MainGameScreen {

    private static final String TAG = BattleScreen.class.getSimpleName();

    private final float V_WIDTH = 11;
    private final float V_HEIGHT = 11;
    
    protected OrthogonalTiledMapRenderer _mapRenderer = null;
    protected MapManager _mapMgr;
    protected OrthographicCamera _camera = null;
    protected OrthographicCamera _hudCamera = null;
    //protected OrthographicCamera controllersCam = null;

    private Json _json;
    private ElmourGame _game;
    private InputMultiplexer _multiplexer;

    private Entity _player;
    private BattleHUD battleHUD;
    private BattleControls battleControls;

    private boolean isInConversation = false;
    private boolean isFirstTime = true;

    private Viewport _viewport;
    private Stage _stage;
    private boolean _isCameraFixed = true;
    private ScreenTransitionActor _transitionActor;
    private Action openBattleSceneAction;
    private Action _switchScreenAction;
    private Action setupBattleScene;

    private AnimatedImage party1;
    private AnimatedImage party2;
    private AnimatedImage party3;
    private AnimatedImage party4;
    private AnimatedImage party5;
    private AnimatedImage enemy1;
    private AnimatedImage enemy2;
    private AnimatedImage enemy3;
    private AnimatedImage enemy4;
    private AnimatedImage enemy5;
    
    public BattleScreen(ElmourGame game) {
        super(game);

        _game = game;
        _mapMgr = new MapManager();
        _mapMgr.loadMap(MapFactory.MapType.GRASS_BATTLE);

        _json = new Json();

        setupViewport(V_WIDTH, V_HEIGHT);

        //get the current size
        _camera = new OrthographicCamera();
        _camera.setToOrtho(false, BattleScreen.VIEWPORT.viewportWidth, BattleScreen.VIEWPORT.viewportHeight);

        _viewport = new ScreenViewport(_camera);
        _stage = new Stage(_viewport);

        if (ElmourGame.isAndroid()) {
            // capture Android back key so it is not passed on to the OS
            Gdx.input.setCatchBackKey(true);

            //NOTE!!! Need to create battleControls before player because player
            //is an observer of battleControls
            controllersCam = new OrthographicCamera();
            controllersCam.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);
            battleControls = new BattleControls(controllersCam);

            _player = EntityFactory.getInstance().getEntity(EntityFactory.EntityType.PLAYER);
            _hudCamera = new OrthographicCamera();
            _hudCamera.setToOrtho(false, BattleScreen.VIEWPORT.viewportWidth, BattleScreen.VIEWPORT.viewportHeight);

            battleHUD = new BattleHUD(game, _hudCamera, _player, _mapMgr, this);

            _multiplexer = new InputMultiplexer();
            _multiplexer.addProcessor(battleControls.getStage());
            _multiplexer.addProcessor(battleHUD.getStage());
            Gdx.input.setInputProcessor(_multiplexer);
            //Gdx.input.setInputProcessor(battleHUD.getStage());
        }
        else {
            _player = EntityFactory.getInstance().getEntity(EntityFactory.EntityType.PLAYER);
            _hudCamera = new OrthographicCamera();
            _hudCamera.setToOrtho(false, BattleScreen.VIEWPORT.viewportWidth, BattleScreen.VIEWPORT.viewportHeight);

            controllersCam = new OrthographicCamera();
            controllersCam.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);
            battleControls = new BattleControls(controllersCam);

            battleHUD = new BattleHUD(game, _hudCamera, _player, _mapMgr, this);

            _multiplexer = new InputMultiplexer();
            _multiplexer.addProcessor(battleControls.getStage());
            _multiplexer.addProcessor(battleHUD.getStage());
            //_multiplexer.addProcessor(_player.getInputProcessor());
            Gdx.input.setInputProcessor(_multiplexer);
        }

        _mapMgr.setPlayer(_player);
        _mapMgr.setCamera(_camera);

        party1 = new AnimatedImage();
        party2 = new AnimatedImage();
        party3 = new AnimatedImage();
        party4 = new AnimatedImage();
        party5 = new AnimatedImage();

        enemy1 = new AnimatedImage();
        enemy2 = new AnimatedImage();
        enemy3 = new AnimatedImage();
        enemy4 = new AnimatedImage();
        enemy5 = new AnimatedImage();

        _transitionActor = new ScreenTransitionActor();

        _stage.addActor(_transitionActor);

        //Actions
        _switchScreenAction = new RunnableAction(){
            @Override
            public void run() {
                _game.setScreen(_game.getScreenType(ElmourGame.ScreenType.MainGame));
            }
        };

        setupBattleScene = new RunnableAction() {
            @Override
            public void run() {
                _mapMgr.loadMap(MapFactory.MapType.GRASS_BATTLE);
                _mapMgr.disableCurrentmapMusic();
                _camera.position.set(10, 6, 0f);

                float characterWidth = 1.0f;
                float characterHeight = 1.0f;

                party1.setSize(characterWidth, characterHeight);
                party1.setVisible(true);
                party1.addAction(Actions.fadeOut(0));
                party1.setPosition(getStartPosition("P1").x, getStartPosition("P1").y);

                party2.setSize(characterWidth, characterHeight);
                party2.setVisible(true);
                party2.addAction(Actions.fadeOut(0));
                party2.setPosition(getStartPosition("P2").x, getStartPosition("P2").y);

                party3.setSize(characterWidth, characterHeight);
                party3.setVisible(true);
                party3.addAction(Actions.fadeOut(0));
                party3.setPosition(getStartPosition("P3").x, getStartPosition("P3").y);

                party4.setSize(characterWidth, characterHeight);
                party4.setVisible(true);
                party4.addAction(Actions.fadeOut(0));
                party4.setPosition(getStartPosition("P4").x, getStartPosition("P4").y);

                party5.setSize(characterWidth, characterHeight);
                party5.setVisible(true);
                party5.addAction(Actions.fadeOut(0));
                party5.setPosition(getStartPosition("P5").x, getStartPosition("P5").y);

                enemy1.setSize(characterWidth, characterHeight);
                enemy1.setVisible(true);
                enemy1.addAction(Actions.fadeOut(0));
                enemy1.setPosition(getStartPosition("E1").x, getStartPosition("E1").y);

                enemy2.setSize(characterWidth, characterHeight);
                enemy2.setVisible(true);
                enemy2.addAction(Actions.fadeOut(0));
                enemy2.setPosition(getStartPosition("E2").x, getStartPosition("E2").y);

                enemy3.setSize(characterWidth, characterHeight);
                enemy3.setVisible(true);
                enemy3.addAction(Actions.fadeOut(0));
                enemy3.setPosition(getStartPosition("E3").x, getStartPosition("E3").y);

                enemy4.setSize(characterWidth, characterHeight);
                enemy4.setVisible(true);
                enemy4.addAction(Actions.fadeOut(0));
                enemy4.setPosition(getStartPosition("E4").x, getStartPosition("E4").y);

                enemy5.setSize(characterWidth, characterHeight);
                enemy5.setVisible(true);
                enemy5.addAction(Actions.fadeOut(0));
                enemy5.setPosition(getStartPosition("E5").x, getStartPosition("E5").y);
            }
        };
    }

    @Override
    public void show() {
        openBattleSceneAction = getBattleSceneAction();
        _stage.addAction(openBattleSceneAction);

        ProfileManager.getInstance().addObserver(_mapMgr);
        if (battleHUD != null)
            ProfileManager.getInstance().addObserver(battleHUD);

        Gdx.input.setInputProcessor(_multiplexer);

        if( _mapRenderer == null ){
            _mapRenderer = new OrthogonalTiledMapRenderer(_mapMgr.getCurrentTiledMap(), Map.UNIT_SCALE);
        }

        float fadeTime = 0.5f;
        party1.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
        party2.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
        party3.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
        party4.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
        party5.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
        enemy1.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
        enemy2.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
        enemy3.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
        enemy4.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
        enemy5.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
    }

    @Override
    public void hide() {

        Gdx.input.setInputProcessor(null);

        party1.addAction(Actions.fadeOut(0));
        party2.addAction(Actions.fadeOut(0));
        party3.addAction(Actions.fadeOut(0));
        party4.addAction(Actions.fadeOut(0));
        party5.addAction(Actions.fadeOut(0));
        enemy1.addAction(Actions.fadeOut(0));
        enemy2.addAction(Actions.fadeOut(0));
        enemy3.addAction(Actions.fadeOut(0));
        enemy4.addAction(Actions.fadeOut(0));
        enemy5.addAction(Actions.fadeOut(0));

        isFirstTime = true;
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

        // this is to fix an issue with the wrong map being flashed for the first frame
        if (!isFirstTime) {
            _mapRenderer.render();
        }
        else {
            isFirstTime = false;
        }

        _camera.update();

        _stage.act(delta);
        _stage.draw();

        battleHUD.render(delta);

        battleControls.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        setupViewport(V_WIDTH, V_HEIGHT);
        _camera.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);

        if (battleHUD != null)
            battleHUD.resize((int) VIEWPORT.physicalWidth, (int) VIEWPORT.physicalHeight);
    }

    @Override
    public void pause() {
        //setGameState(GameState.SAVING);
        if (battleHUD != null)
            battleHUD.pause();
    }

    @Override
    public void resume() {
        //setGameState(CutSceneScreenChapter2.GameState.RUNNING);
        if (battleHUD != null)
            battleHUD.resume();
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

    public void setBattleControls(BattleHUD.ScreenState state) {
        switch (state) {
            case FIGHT:
                battleControls.hideABButtons();
                break;
            case FINAL:
                battleControls.hideABButtons();
                break;
            case INVENTORY:
                battleControls.showABButtons();
                break;
            case MAGIC:
                battleControls.hideABButtons();
                break;
            case MAIN:
                battleControls.hideABButtons();
                break;
            case MENU:
                battleControls.hideABButtons();
                break;
            case SPELL_TYPE:
                battleControls.hideABButtons();
                break;
            case SPELLS_BLACK:
                battleControls.showABButtons();
                break;
            case SPELLS_WHITE:
                battleControls.showABButtons();
                break;
            case POWER:
                battleControls.showABButtons();
                break;
            case STATS:
                battleControls.hideABButtons();
                break;
        }
    }

    private Action getBattleSceneAction() {
        setupBattleScene.reset();
        return Actions.sequence(
                Actions.addAction(setupBattleScene),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 2), _transitionActor)
        );
    }

    public void fadeOut() {
        Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 2), _transitionActor);
    }

    private AnimatedImage getAnimatedImage(EntityFactory.EntityName entityName){
        Entity entity = EntityFactory.getInstance().getEntityByName(entityName);
        return setEntityAnimation(entity);
    }

    private AnimatedImage getAnimatedImage(MonsterFactory.MonsterEntityType entityName){
        Entity entity = MonsterFactory.getInstance().getMonster(entityName);
        return setEntityAnimation(entity);
    }

    private AnimatedImage setEntityAnimation(Entity entity){
        final AnimatedImage animEntity = new AnimatedImage();
        animEntity.setEntity(entity);
        animEntity.setSize(animEntity.getWidth() * Map.UNIT_SCALE, animEntity.getHeight() * Map.UNIT_SCALE);
        return animEntity;
    }

    private void setupViewport(float width, float height){
        //Make the viewport a percentage of the total display area
        BattleScreen.VIEWPORT.virtualWidth = width;
        BattleScreen.VIEWPORT.virtualHeight = height;

        //Current viewport dimensions
        BattleScreen.VIEWPORT.viewportWidth = BattleScreen.VIEWPORT.virtualWidth;
        BattleScreen.VIEWPORT.viewportHeight = BattleScreen.VIEWPORT.virtualHeight;

        //pixel dimensions of display
        BattleScreen.VIEWPORT.physicalWidth = Gdx.graphics.getWidth();
        BattleScreen.VIEWPORT.physicalHeight = Gdx.graphics.getHeight();

        //aspect ratio for current viewport
        BattleScreen.VIEWPORT.aspectRatio = (BattleScreen.VIEWPORT.virtualWidth / BattleScreen.VIEWPORT.virtualHeight);

        //update viewport if there could be skewing
        if( BattleScreen.VIEWPORT.physicalWidth / BattleScreen.VIEWPORT.physicalHeight >= BattleScreen.VIEWPORT.aspectRatio){
            //Letterbox left and right
            BattleScreen.VIEWPORT.viewportWidth = BattleScreen.VIEWPORT.viewportHeight * (BattleScreen.VIEWPORT.physicalWidth/ BattleScreen.VIEWPORT.physicalHeight);
            BattleScreen.VIEWPORT.viewportHeight = BattleScreen.VIEWPORT.virtualHeight;
        }else{
            //letterbox above and below
            BattleScreen.VIEWPORT.viewportWidth = BattleScreen.VIEWPORT.virtualWidth;
            BattleScreen.VIEWPORT.viewportHeight = BattleScreen.VIEWPORT.viewportWidth * (BattleScreen.VIEWPORT.physicalHeight/ BattleScreen.VIEWPORT.physicalWidth);
        }

        Gdx.app.debug(TAG, "WorldRenderer: virtual: (" + BattleScreen.VIEWPORT.virtualWidth + "," + BattleScreen.VIEWPORT.virtualHeight + ")" );
        Gdx.app.debug(TAG, "WorldRenderer: viewport: (" + BattleScreen.VIEWPORT.viewportWidth + "," + BattleScreen.VIEWPORT.viewportHeight + ")" );
        Gdx.app.debug(TAG, "WorldRenderer: physical: (" + BattleScreen.VIEWPORT.physicalWidth + "," + BattleScreen.VIEWPORT.physicalHeight + ")" );
    }

    public Vector2 getStartPosition(String name){
        Vector2 position = null;

        for( MapObject object: _mapMgr.getSpawnsLayer().getObjects()){
            String objectName = object.getName();

            if( objectName == null || objectName.isEmpty() ){
                continue;
            }

            if( objectName.equalsIgnoreCase(name) ){
                //Get center of rectangle
                float x = ((RectangleMapObject)object).getRectangle().getX();
                float y = ((RectangleMapObject)object).getRectangle().getY();

                //scale by the unit to convert from map coordinates
                x *= Map.UNIT_SCALE;
                y *= Map.UNIT_SCALE;

                position = new Vector2(x,y);
            }
        }
        return position;
    }

    public void addPartyMember(Entity partyEntity, int index) {
        switch (index) {
            case 1:
                party1.setEntity(partyEntity);
                party1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                party1.setCurrentDirection(Entity.Direction.LEFT);
                _stage.addActor(party1);
                break;
            case 2:
                party2.setEntity(partyEntity);
                party2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                party2.setCurrentDirection(Entity.Direction.LEFT);
                _stage.addActor(party2);
                break;
            case 3:
                party3.setEntity(partyEntity);
                party3.setCurrentAnimationType(Entity.AnimationType.IDLE);
                party3.setCurrentDirection(Entity.Direction.LEFT);
                _stage.addActor(party3);
                break;
            case 4:
                party4.setEntity(partyEntity);
                party4.setCurrentAnimationType(Entity.AnimationType.IDLE);
                party4.setCurrentDirection(Entity.Direction.LEFT);
                _stage.addActor(party4);
                break;
            case 5:
                party5.setEntity(partyEntity);
                party5.setCurrentAnimationType(Entity.AnimationType.IDLE);
                party5.setCurrentDirection(Entity.Direction.LEFT);
                _stage.addActor(party5);
                break;
        }
    }

    public void removePartyMember(int index) {

        // remove actor from the stage
        switch (index) {
            case 1:
                party1.remove();
                break;
            case 2:
                party2.remove();
                break;
            case 3:
                party3.remove();
                break;
            case 4:
                party4.remove();
                break;
            case 5:
                party5.remove();
                break;
        }
    }

    public void addOpponent(Entity enemyEntity, int index) {

        switch (index) {
            case 1:
                enemy1.setEntity(enemyEntity);
                enemy1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                enemy1.setCurrentDirection(Entity.Direction.RIGHT);
                _stage.addActor(enemy1);
                break;
            case 2:
                enemy2.setEntity(enemyEntity);
                enemy2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                enemy2.setCurrentDirection(Entity.Direction.RIGHT);
                _stage.addActor(enemy2);
                break;
            case 3:
                enemy3.setEntity(enemyEntity);
                enemy3.setCurrentAnimationType(Entity.AnimationType.IDLE);
                enemy3.setCurrentDirection(Entity.Direction.RIGHT);
                _stage.addActor(enemy3);
                break;
            case 4:
                enemy4.setEntity(enemyEntity);
                enemy4.setCurrentAnimationType(Entity.AnimationType.IDLE);
                enemy4.setCurrentDirection(Entity.Direction.RIGHT);
                _stage.addActor(enemy4);
                break;
            case 5:
                enemy5.setEntity(enemyEntity);
                enemy5.setCurrentAnimationType(Entity.AnimationType.IDLE);
                enemy5.setCurrentDirection(Entity.Direction.RIGHT);
                _stage.addActor(enemy5);
                break;
        }
    }

    public void removeOpponent(int index) {

        // remove actor from the stage
        switch (index) {
            case 1:
                enemy1.remove();
                break;
            case 2:
                enemy2.remove();
                break;
            case 3:
                enemy3.remove();
                break;
            case 4:
                enemy4.remove();
                break;
            case 5:
                enemy5.remove();
                break;
        }
    }
}
