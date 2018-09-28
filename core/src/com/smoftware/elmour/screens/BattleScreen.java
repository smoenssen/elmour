package com.smoftware.elmour.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.smoftware.elmour.ElmourGame;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.EntityConfig;
import com.smoftware.elmour.EntityFactory;
import com.smoftware.elmour.GraphicsComponent;
import com.smoftware.elmour.UI.AnimatedImage;
import com.smoftware.elmour.UI.BattleControls;
import com.smoftware.elmour.UI.BattleHUD;
import com.smoftware.elmour.Utility;
import com.smoftware.elmour.audio.AudioManager;
import com.smoftware.elmour.battle.BattleObserver;
import com.smoftware.elmour.battle.MonsterFactory;
import com.smoftware.elmour.maps.Map;
import com.smoftware.elmour.maps.MapFactory;
import com.smoftware.elmour.maps.MapManager;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.sfx.ScreenTransitionAction;
import com.smoftware.elmour.sfx.ScreenTransitionActor;

import java.util.Hashtable;

/**
 * Created by steve on 3/2/18.
=======
import com.smoftware.elmour.ElmourGame;

/**
 * Created by moenssr on 3/1/2018.
>>>>>>> Stashed changes
 */

public class BattleScreen extends MainGameScreen implements BattleObserver{

    private static final String TAG = BattleScreen.class.getSimpleName();

    private final float V_WIDTH = 11;
    private final float V_HEIGHT = 11;

    protected Hashtable<Entity.AnimationType, Animation<TextureRegion>> char1BattleAnimations;
    protected TextureRegion _currentFrame = null;
    private float _frameTime = 0;
    
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
    private Action playerAttackCutSceneAction;
    private Action opponentAttackCutSceneAction;

    private float characterWidth = 1.0f;
    private float characterHeight = 1.0f;

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

    private Texture turnIndicator;
    private Vector2 currentTurnCharPosition;
    private AnimatedImage currentTurnCharacter;

    private Texture selectedEntityIndicator;
    private Entity selectedEntity;

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

        _game.battleState.addObserver(this);

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
            _multiplexer.addProcessor(_stage);
            Gdx.input.setInputProcessor(_multiplexer);
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
            _multiplexer.addProcessor(_stage);
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

        char1BattleAnimations = GraphicsComponent.loadAnimationsByName((EntityFactory.EntityName.CHARACTER_1));
        selectedEntityIndicator = new Texture("graphics/down_arrow_red.png");
        turnIndicator = new Texture("graphics/down_arrow_blue.png");
        currentTurnCharPosition = new Vector2(0, 0);

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

                party1.setSize(characterWidth, characterHeight);
                party1.setVisible(true);
                party1.addAction(Actions.fadeOut(0));
                party1.setPosition(getStartPosition("P1").x, getStartPosition("P1").y);
                if (party1.getEntity() != null)
                    party1.getEntity().setCurrentPosition(new Vector2(party1.getX(), party1.getY()));

                party2.setSize(characterWidth, characterHeight);
                party2.setVisible(true);
                party2.addAction(Actions.fadeOut(0));
                party2.setPosition(getStartPosition("P2").x, getStartPosition("P2").y);
                if (party2.getEntity() != null)
                    party2.getEntity().setCurrentPosition(new Vector2(party2.getX(), party2.getY()));

                party3.setSize(characterWidth, characterHeight);
                party3.setVisible(true);
                party3.addAction(Actions.fadeOut(0));
                party3.setPosition(getStartPosition("P3").x, getStartPosition("P3").y);
                if (party3.getEntity() != null)
                    party3.getEntity().setCurrentPosition(new Vector2(party3.getX(), party3.getY()));

                party4.setSize(characterWidth, characterHeight);
                party4.setVisible(true);
                party4.addAction(Actions.fadeOut(0));
                party4.setPosition(getStartPosition("P4").x, getStartPosition("P4").y);
                if (party4.getEntity() != null)
                    party4.getEntity().setCurrentPosition(new Vector2(party4.getX(), party4.getY()));

                party5.setSize(characterWidth, characterHeight);
                party5.setVisible(true);
                party5.addAction(Actions.fadeOut(0));
                party5.setPosition(getStartPosition("P5").x, getStartPosition("P5").y);
                if (party5.getEntity() != null)
                    party5.getEntity().setCurrentPosition(new Vector2(party5.getX(), party5.getY()));

                enemy1.setSize(characterWidth, characterHeight);
                enemy1.setVisible(true);
                enemy1.addAction(Actions.fadeOut(0));
                enemy1.setPosition(getStartPosition("E1").x, getStartPosition("E1").y);
                if (enemy1.getEntity() != null)
                    enemy1.getEntity().setCurrentPosition(new Vector2(enemy1.getX(), enemy1.getY()));

                enemy2.setSize(characterWidth, characterHeight);
                enemy2.setVisible(true);
                enemy2.addAction(Actions.fadeOut(0));
                enemy2.setPosition(getStartPosition("E2").x, getStartPosition("E2").y);
                if (enemy2.getEntity() != null)
                    enemy2.getEntity().setCurrentPosition(new Vector2(enemy2.getX(), enemy2.getY()));

                enemy3.setSize(characterWidth, characterHeight);
                enemy3.setVisible(true);
                enemy3.addAction(Actions.fadeOut(0));
                enemy3.setPosition(getStartPosition("E3").x, getStartPosition("E3").y);
                if (enemy3.getEntity() != null)
                    enemy3.getEntity().setCurrentPosition(new Vector2(enemy3.getX(), enemy3.getY()));

                enemy4.setSize(characterWidth, characterHeight);
                enemy4.setVisible(true);
                enemy4.addAction(Actions.fadeOut(0));
                enemy4.setPosition(getStartPosition("E4").x, getStartPosition("E4").y);
                if (enemy4.getEntity() != null)
                    enemy4.getEntity().setCurrentPosition(new Vector2(enemy4.getX(), enemy4.getY()));

                enemy5.setSize(characterWidth, characterHeight);
                enemy5.setVisible(true);
                enemy5.addAction(Actions.fadeOut(0));
                enemy5.setPosition(getStartPosition("E5").x, getStartPosition("E5").y);
                if (enemy5.getEntity() != null)
                    enemy5.getEntity().setCurrentPosition(new Vector2(enemy5.getX(), enemy5.getY()));
            }
        };

        party1.addListener(new ClickListener() {
                              @Override
                              public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                  return true;
                              }

                              @Override
                              public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                  // make sure touch point is still on this image
                                  if (touchPointIsInImage(party1)) {
                                     _game.battleState.setCurrentSelectedCharacter(party1.getEntity());
                                     selectedEntity = party1.getEntity();
                                  }
                              }
                          }
        );

        party2.addListener(new ClickListener() {
                               @Override
                               public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                   return true;
                               }

                               @Override
                               public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                   // make sure touch point is still on this image
                                   if (touchPointIsInImage(party2)) {
                                       _game.battleState.setCurrentSelectedCharacter(party2.getEntity());
                                       selectedEntity = party2.getEntity();
                                   }
                               }
                           }
        );

        party3.addListener(new ClickListener() {
                               @Override
                               public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                   return true;
                               }

                               @Override
                               public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                   // make sure touch point is still on this image
                                   if (touchPointIsInImage(party3)) {
                                       _game.battleState.setCurrentSelectedCharacter(party3.getEntity());
                                       selectedEntity = party3.getEntity();
                                   }
                               }
                           }
        );

        party4.addListener(new ClickListener() {
                               @Override
                               public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                   return true;
                               }

                               @Override
                               public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                   // make sure touch point is still on this image
                                   if (touchPointIsInImage(party4)) {
                                       _game.battleState.setCurrentSelectedCharacter(party4.getEntity());
                                       selectedEntity = party4.getEntity();
                                   }
                               }
                           }
        );

        party5.addListener(new ClickListener() {
                               @Override
                               public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                   return true;
                               }

                               @Override
                               public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                   // make sure touch point is still on this image
                                   if (touchPointIsInImage(party5)) {
                                       _game.battleState.setCurrentSelectedCharacter(party5.getEntity());
                                       selectedEntity = party5.getEntity();
                                   }
                               }
                           }
        );

        enemy1.addListener(new ClickListener() {
                               @Override
                               public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                   return true;
                               }

                               @Override
                               public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                   // make sure touch point is still on this image
                                   if (touchPointIsInImage(enemy1)) {
                                       _game.battleState.setCurrentSelectedCharacter(enemy1.getEntity());
                                       selectedEntity = enemy1.getEntity();
                                   }
                               }
                           }
        );

        enemy2.addListener(new ClickListener() {
                               @Override
                               public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                   return true;
                               }

                               @Override
                               public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                   // make sure touch point is still on this image
                                   if (touchPointIsInImage(enemy2)) {
                                       _game.battleState.setCurrentSelectedCharacter(enemy2.getEntity());
                                       selectedEntity = enemy2.getEntity();
                                   }
                               }
                           }
        );

        enemy3.addListener(new ClickListener() {
                               @Override
                               public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                   return true;
                               }

                               @Override
                               public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                   // make sure touch point is still on this image
                                   if (touchPointIsInImage(enemy3)) {
                                       _game.battleState.setCurrentSelectedCharacter(enemy3.getEntity());
                                       selectedEntity = enemy3.getEntity();
                                   }
                               }
                           }
        );

        enemy4.addListener(new ClickListener() {
                               @Override
                               public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                   return true;
                               }

                               @Override
                               public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                   // make sure touch point is still on this image
                                   if (touchPointIsInImage(enemy4)) {
                                       _game.battleState.setCurrentSelectedCharacter(enemy4.getEntity());
                                       selectedEntity = enemy4.getEntity();
                                   }
                               }
                           }
        );

        enemy5.addListener(new ClickListener() {
                               @Override
                               public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                   return true;
                               }

                               @Override
                               public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                   // make sure touch point is still on this image
                                   if (touchPointIsInImage(enemy5)) {
                                       _game.battleState.setCurrentSelectedCharacter(enemy5.getEntity());
                                       selectedEntity = enemy5.getEntity();
                                   }
                               }
                           }
        );
    }

    boolean shouldShowWeaponAnimation = false;

    public class showWeaponAnimation extends Action {
        boolean visible = true;

        public showWeaponAnimation(boolean visible) {
            this.visible = visible;
        }

        @Override
        public boolean act(float delta) {
            if (this.visible) {
                shouldShowWeaponAnimation = true;
                _frameTime = 0;
            }
            else {
                shouldShowWeaponAnimation = false;
            }

            return true;
        }
    }

    public class setWalkDirection extends Action {
        AnimatedImage character = null;
        Entity.AnimationType direction = Entity.AnimationType.IDLE;

        public setWalkDirection(AnimatedImage character, Entity.AnimationType direction) {
            this.character = character;
            this.direction = direction;
        }

        @Override
        public boolean act (float delta) {
            character.setCurrentAnimationType(direction);
            return true; // An action returns true when it's completed
        }
    }

    public class showMainCharacterAnimation extends Action {
        AnimatedImage character = null;
        boolean show = false;

        public showMainCharacterAnimation(AnimatedImage character, boolean show) {
            this.character = character;
            this.show = show;
        }

        @Override
        public boolean act (float delta) {
            if (show)
                character.setVisible(true);
            else
                character.setVisible(false);
            return true; // An action returns true when it's completed
        }
    }

    private Action getPlayerAttackCutScreenAction() {
        return Actions.sequence(

                new setWalkDirection(currentTurnCharacter, Entity.AnimationType.WALK_LEFT),
                //Actions.addAction(Actions.moveTo(currentTurnCharacter.getX() - 2, currentTurnCharacter.getY(), 0.25f, Interpolation.linear), currentTurnCharacter),
                //Actions.delay(0.5f),
                Actions.addAction(Actions.moveTo(selectedEntity.getCurrentPosition().x + 1, selectedEntity.getCurrentPosition().y,  0.75f, Interpolation.linear), currentTurnCharacter),

                Actions.delay(0.75f),
                new setWalkDirection(currentTurnCharacter, Entity.AnimationType.IDLE),
                Actions.delay(0.75f),
                new showWeaponAnimation(true),
                new showMainCharacterAnimation(currentTurnCharacter, false),
                // Framerate * # of Frames
                Actions.delay(0.4f),
                new showWeaponAnimation(false),
                new showMainCharacterAnimation(currentTurnCharacter, true),

                new setWalkDirection(currentTurnCharacter, Entity.AnimationType.WALK_RIGHT),
                Actions.addAction(Actions.moveTo(currentTurnCharacter.getX(), currentTurnCharacter.getY(), 0.75f, Interpolation.linear), currentTurnCharacter),
                Actions.delay(0.75f),
                new setWalkDirection(currentTurnCharacter, Entity.AnimationType.WALK_LEFT),
                new setWalkDirection(currentTurnCharacter, Entity.AnimationType.IDLE)
        );
    }

    private Action getOpponentAttackCutScreenAction() {
        return Actions.sequence(

                new setWalkDirection(currentTurnCharacter, Entity.AnimationType.WALK_RIGHT),
                Actions.addAction(Actions.moveTo(selectedEntity.getCurrentPosition().x - 1, selectedEntity.getCurrentPosition().y,  0.75f, Interpolation.linear), currentTurnCharacter),

                Actions.delay(0.75f),
                new setWalkDirection(currentTurnCharacter, Entity.AnimationType.IDLE),
                Actions.delay(0.75f),
                new showWeaponAnimation(true),
                new showMainCharacterAnimation(currentTurnCharacter, false),
                // Framerate * # of Frames
                Actions.delay(0.4f),
                new showWeaponAnimation(false),
                new showMainCharacterAnimation(currentTurnCharacter, true),

                new setWalkDirection(currentTurnCharacter, Entity.AnimationType.WALK_LEFT),
                Actions.addAction(Actions.moveTo(currentTurnCharacter.getX(), currentTurnCharacter.getY(), 0.75f, Interpolation.linear), currentTurnCharacter),
                Actions.delay(0.75f),
                new setWalkDirection(currentTurnCharacter, Entity.AnimationType.WALK_RIGHT),
                new setWalkDirection(currentTurnCharacter, Entity.AnimationType.IDLE)
        );
    }

    private boolean touchPointIsInImage(AnimatedImage image) {
        // Get touch point
        Vector2 screenPos = new Vector2(Gdx.input.getX(), Gdx.input.getY());

        // Convert the touch point into local coordinates
        Vector2 localPos = new Vector2(screenPos);
        localPos = _stage.screenToStageCoordinates(localPos);

        Rectangle buttonRect = new Rectangle(image.getX(), image.getY(), image.getWidth(), image.getHeight());

        return Utility.pointInRectangle(buttonRect, localPos.x, localPos.y);
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

    float currentTurnFlashTimer = 0;

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //_stage.setDebugAll(true);

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

        currentTurnFlashTimer += delta;

        ////////////////////////////
        // Flashing turn indicator
        if (currentTurnFlashTimer < 0.5f) {
            _mapRenderer.getBatch().begin();
            _mapRenderer.getBatch().draw(turnIndicator, currentTurnCharPosition.x + characterWidth / 2 * 0.5f, currentTurnCharPosition.y + characterHeight * 1.1f, 0.5f, 0.5f);

            if (selectedEntity != null) {
                _mapRenderer.getBatch().draw(selectedEntityIndicator, selectedEntity.getCurrentPosition().x + characterWidth / 2 * 0.5f, selectedEntity.getCurrentPosition().y + characterHeight * 1.1f, 0.5f, 0.5f);
            }

            _mapRenderer.getBatch().end();
        }
        else if (currentTurnFlashTimer > 0.75f) {
            currentTurnFlashTimer = 0;
        }

        ////////////////////////////
        // Battle animation
        //todo: set correct animation elsewhere
        Animation<TextureRegion> animation = char1BattleAnimations.get(Entity.AnimationType.DAGGER_LEFT);
        //animation.getKeyFrames().length;
        if (animation != null) {
            _frameTime = (_frameTime + delta) % 5;
            _currentFrame = animation.getKeyFrame(_frameTime);
        }

        _mapRenderer.getBatch().begin();
        if (currentTurnCharacter != null && _currentFrame != null && shouldShowWeaponAnimation) {
            float regionWidth = _currentFrame.getRegionWidth() * Map.UNIT_SCALE;
            float regionHeight = _currentFrame.getRegionHeight() * Map.UNIT_SCALE;

            //adjust for character width/height vs. animation region width/height
            float adjustWidth = (regionWidth - characterWidth) / 2;
            float adjustHeight = (regionHeight - characterHeight) / 2;
            _mapRenderer.getBatch().draw(_currentFrame, currentTurnCharacter.getX() - adjustWidth, currentTurnCharacter.getY() - adjustHeight, regionWidth, regionHeight);
        }
        _mapRenderer.getBatch().end();
    }

    @Override
    public void resize(int width, int height) {
        setupViewport(V_WIDTH, V_HEIGHT);
        _camera.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);

        if (battleHUD != null && isFirstTime)
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
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 1), _transitionActor)
        );
    }

    public void fadeOut() {
        Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 1), _transitionActor);
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

    private Vector2 getStartPosition(String name){
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
        ProfileManager.getInstance().getStatProperties(partyEntity);

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
                ProfileManager.getInstance().getStatProperties(party2.getEntity());
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

    @Override
    public void onNotify(Entity entity, BattleEvent event) {
        switch (event) {
            case CHARACTER_TURN_CHANGED:
                currentTurnCharPosition = entity.getCurrentPosition();
                switch (entity.getBattlePosition()) {
                    case 1:
                        if (entity.getBattleEntityType().equals(Entity.BattleEntityType.PARTY))
                            currentTurnCharacter = party1;
                        else
                            currentTurnCharacter = enemy1;
                        break;
                    case 2:
                        if (entity.getBattleEntityType().equals(Entity.BattleEntityType.PARTY))
                            currentTurnCharacter = party2;
                        else
                            currentTurnCharacter = enemy2;
                        break;
                    case 3:
                        if (entity.getBattleEntityType().equals(Entity.BattleEntityType.PARTY))
                            currentTurnCharacter = party3;
                        else
                            currentTurnCharacter = enemy3;
                        break;
                    case 4:
                        if (entity.getBattleEntityType().equals(Entity.BattleEntityType.PARTY))
                            currentTurnCharacter = party4;
                        else
                            currentTurnCharacter = enemy4;
                        break;
                    case 5:
                        if (entity.getBattleEntityType().equals(Entity.BattleEntityType.PARTY))
                            currentTurnCharacter = party5;
                        else
                            currentTurnCharacter = enemy5;
                        break;
                }
                break;
            case CHARACTER_SELECTED:
                selectedEntity = entity;
                break;
            case OPPONENT_DEFEATED:
                float fadeOutTime = 1;

                if (enemy1.getEntity() != null) {
                    if (enemy1.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                        enemy1.addAction(Actions.fadeOut(fadeOutTime));
                    }
                }
                if (enemy2.getEntity() != null) {
                    if (enemy2.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                        enemy2.addAction(Actions.fadeOut(fadeOutTime));
                    }
                }
                if (enemy3.getEntity() != null) {
                    if (enemy3.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                        enemy3.addAction(Actions.fadeOut(fadeOutTime));
                    }
                }
                if (enemy4.getEntity() != null) {
                    if (enemy4.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                        enemy4.addAction(Actions.fadeOut(fadeOutTime));
                    }
                }
                if (enemy5.getEntity() != null) {
                    if (enemy5.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                        enemy5.addAction(Actions.fadeOut(fadeOutTime));
                    }
                }
        }
    }

    @Override
    public void onNotify(Entity sourceEntity, Entity destinationEntity, BattleEventWithMessage event, String message) {
        switch (event) {
            case PLAYER_ATTACKS:
                playerAttackCutSceneAction = getPlayerAttackCutScreenAction();
                _stage.addAction(playerAttackCutSceneAction);
                break;
            case PLAYER_TURN_DONE:
                selectedEntity = null;
                break;
            case OPPONENT_ATTACKS:
                selectedEntity = destinationEntity;
                opponentAttackCutSceneAction = getOpponentAttackCutScreenAction();
                _stage.addAction(opponentAttackCutSceneAction);
                break;
            case OPPONENT_TURN_DONE:
                selectedEntity = null;
                break;
        }
    }
}
