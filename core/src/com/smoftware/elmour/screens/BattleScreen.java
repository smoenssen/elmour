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
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.smoftware.elmour.ElmourGame;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.EntityConfig;
import com.smoftware.elmour.EntityFactory;
import com.smoftware.elmour.GraphicsComponent;
import com.smoftware.elmour.InventoryElement;
import com.smoftware.elmour.UI.AnimatedImage;
import com.smoftware.elmour.UI.BattleControls;
import com.smoftware.elmour.UI.BattleHUD;
import com.smoftware.elmour.UI.MyActions;
import com.smoftware.elmour.UI.StatusArrows;
import com.smoftware.elmour.Utility;
import com.smoftware.elmour.audio.AudioManager;
import com.smoftware.elmour.battle.BattleObserver;
import com.smoftware.elmour.maps.Map;
import com.smoftware.elmour.maps.MapFactory;
import com.smoftware.elmour.maps.MapManager;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.sfx.ScreenTransitionAction;
import com.smoftware.elmour.sfx.ScreenTransitionActor;

import java.util.Hashtable;

/**
 * Created by moenssr on 3/1/2018.
 */

public class BattleScreen extends MainGameScreen implements BattleObserver{

    private static final String TAG = BattleScreen.class.getSimpleName();

    public enum AnimationState { BATTLE, ESCAPED, FAILED_ESCAPE, NONE }

    private final float V_WIDTH = 11;
    private final float V_HEIGHT = 11;
    private final float CAMERA_POS_X = 40;
    private final float CAMERA_POS_Y = 6;
    private float cameraRunningOffset = 0;

    private Hashtable<Entity.AnimationType, Animation<TextureRegion>> carmenBattleAnimations;
    private Hashtable<Entity.AnimationType, Animation<TextureRegion>> char1BattleAnimations;
    private Hashtable<Entity.AnimationType, Animation<TextureRegion>> char2BattleAnimations;
    private Hashtable<Entity.AnimationType, Animation<TextureRegion>> justinBattleAnimations;
    private Hashtable<Entity.AnimationType, Animation<TextureRegion>> jaxonBattleAnimations;

    private Hashtable<Entity.AnimationType, Animation<TextureRegion>> douglasBattleAnimations;
    private Hashtable<Entity.AnimationType, Animation<TextureRegion>> royalGuardBattleAnimations;
    private Hashtable<Entity.AnimationType, Animation<TextureRegion>> steveBattleAnimations;

    private Hashtable<Entity.AnimationType, Animation<TextureRegion>> battleHitAnimations;

    protected TextureRegion _currentFrame = null;
    protected TextureRegion currentHitFrame = null;
    protected TextureRegion currentDefenderFrame = null;
    private float _frameTime = 0;
    private Animation<TextureRegion> currentCharacterAnimation;
    private Animation<TextureRegion> currentHitAnimation;
    private Animation<TextureRegion> currentDefenderAnimation;
    
    protected OrthogonalTiledMapRenderer _mapRenderer = null;
    protected MapManager _mapMgr;
    protected OrthographicCamera _camera = null;
    protected OrthographicCamera _hudCamera = null;

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
    private MyActions myActions;
    private ScreenTransitionActor _transitionActor;
    private Action _switchScreenAction;
    private Action setupBattleScene;

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

    private boolean showStatusArrows = true;
    private StatusArrows party1StatArrows;
    private StatusArrows party2StatArrows;
    private StatusArrows party3StatArrows;
    private StatusArrows party4StatArrows;
    private StatusArrows party5StatArrows;
    private StatusArrows enemy1StatArrows;
    private StatusArrows enemy2StatArrows;
    private StatusArrows enemy3StatArrows;
    private StatusArrows enemy4StatArrows;
    private StatusArrows enemy5StatArrows;

    private float currentTurnFlashTimer = 0;
    private Texture currentTurnIndicator;
    private AnimatedImage currentTurnCharacter;
    private Entity currentTurnEntity;
    private AnimatedImage defendingCharacter;

    private Texture selectedEntityIndicator;
    private Entity selectedEntity;

    private Table hitPointFloaterTable;

    private Image blackScreen;

    private static AnimationState animationState = AnimationState.NONE;

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

        party1StatArrows = new StatusArrows(_stage);
        party2StatArrows = new StatusArrows(_stage);
        party3StatArrows = new StatusArrows(_stage);
        party4StatArrows = new StatusArrows(_stage);
        party5StatArrows = new StatusArrows(_stage);
        enemy1StatArrows = new StatusArrows(_stage);
        enemy2StatArrows = new StatusArrows(_stage);
        enemy3StatArrows = new StatusArrows(_stage);
        enemy4StatArrows = new StatusArrows(_stage);
        enemy5StatArrows = new StatusArrows(_stage);

        carmenBattleAnimations = GraphicsComponent.loadAnimationsByName((EntityFactory.EntityName.CARMEN));
        char1BattleAnimations = GraphicsComponent.loadAnimationsByName((EntityFactory.EntityName.CHARACTER_1));
        char2BattleAnimations = GraphicsComponent.loadAnimationsByName((EntityFactory.EntityName.CHARACTER_2));
        justinBattleAnimations = GraphicsComponent.loadAnimationsByName((EntityFactory.EntityName.JUSTIN));
        jaxonBattleAnimations = GraphicsComponent.loadAnimationsByName((EntityFactory.EntityName.JAXON_1));

        douglasBattleAnimations = GraphicsComponent.loadAnimationsByName((EntityFactory.EntityName.DOUGLAS));
        royalGuardBattleAnimations = GraphicsComponent.loadAnimationsByName((EntityFactory.EntityName.ROYAL_GUARD));
        steveBattleAnimations = GraphicsComponent.loadAnimationsByName((EntityFactory.EntityName.STEVE));

        battleHitAnimations = GraphicsComponent.loadAnimationsByName((EntityFactory.EntityName.HIT));

        selectedEntityIndicator = new Texture("graphics/down_arrow_red.png");
        currentTurnIndicator = new Texture("graphics/down_arrow_blue.png");

        hitPointFloaterTable = new Table();

        blackScreen = new Image(new Texture("graphics/black_rectangle.png"));
        blackScreen.setWidth(_stage.getWidth());
        blackScreen.setHeight(_stage.getHeight());
        blackScreen.setPosition(0, 0);
        blackScreen.setVisible(true);

        _transitionActor = new ScreenTransitionActor();

        _stage.addActor(_transitionActor);
        _stage.addActor(blackScreen);
        _stage.addActor(hitPointFloaterTable);

        //Actions
        myActions = new MyActions();

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
                _camera.position.set(CAMERA_POS_X, CAMERA_POS_Y, 0f);

                blackScreen.addAction(Actions.fadeOut(0));

                // Note: need to have characters started in faded out state here otherwise they might flash
                party1.setSize(characterWidth, characterHeight);
                party1.addAction(Actions.fadeOut(0));
                party1.setVisible(true);
                party1.setPosition(getStartPosition("P1").x, getStartPosition("P1").y);
                if (party1.getEntity() != null)
                    party1.getEntity().setCurrentPosition(new Vector2(party1.getX(), party1.getY()));
                party1StatArrows.setPosition(party1.getX() - 1, party1.getY());
                /*
                party1StatArrows.add(EntityFactory.EntityName.ATK_DOWN_LEFT);
                party1StatArrows.add(EntityFactory.EntityName.MATK_DOWN_LEFT);
                party1StatArrows.add(EntityFactory.EntityName.DEF_DOWN_LEFT);
                party1StatArrows.add(EntityFactory.EntityName.MDEF_DOWN_LEFT);
                party1StatArrows.add(EntityFactory.EntityName.SPD_DOWN_LEFT);
                party1StatArrows.add(EntityFactory.EntityName.ACC_DOWN_LEFT);
                party1StatArrows.add(EntityFactory.EntityName.DIBS_DOWN_LEFT);
                party1StatArrows.add(EntityFactory.EntityName.EXP_DOWN_LEFT);
                party1StatArrows.add(EntityFactory.EntityName.DROPS_DOWN_LEFT);
                */

                party2.setSize(characterWidth, characterHeight);
                party2.addAction(Actions.fadeOut(0));
                party2.setVisible(true);
                party2.setPosition(getStartPosition("P2").x, getStartPosition("P2").y);
                if (party2.getEntity() != null)
                    party2.getEntity().setCurrentPosition(new Vector2(party2.getX(), party2.getY()));
                party2StatArrows.setPosition(party2.getX() - 1, party2.getY());

                party3.setSize(characterWidth, characterHeight);
                party3.addAction(Actions.fadeOut(0));
                party3.setVisible(true);
                party3.setPosition(getStartPosition("P3").x, getStartPosition("P3").y);
                if (party3.getEntity() != null)
                    party3.getEntity().setCurrentPosition(new Vector2(party3.getX(), party3.getY()));
                party3StatArrows.setPosition(party3.getX() - 1, party3.getY());

                party4.setSize(characterWidth, characterHeight);
                party4.addAction(Actions.fadeOut(0));
                party4.setVisible(true);
                party4.setPosition(getStartPosition("P4").x, getStartPosition("P4").y);
                if (party4.getEntity() != null)
                    party4.getEntity().setCurrentPosition(new Vector2(party4.getX(), party4.getY()));
                party4StatArrows.setPosition(party4.getX() - 1, party4.getY());

                party5.setSize(characterWidth, characterHeight);
                party5.addAction(Actions.fadeOut(0));
                party5.setVisible(true);
                party5.setPosition(getStartPosition("P5").x, getStartPosition("P5").y);
                if (party5.getEntity() != null)
                    party5.getEntity().setCurrentPosition(new Vector2(party5.getX(), party5.getY()));
                party5StatArrows.setPosition(party5.getX() - 1, party5.getY());

                enemy1.setSize(characterWidth, characterHeight);
                enemy1.addAction(Actions.fadeOut(0));
                enemy1.setVisible(true);
                enemy1.setPosition(getStartPosition("E1").x, getStartPosition("E1").y);
                if (enemy1.getEntity() != null)
                    enemy1.getEntity().setCurrentPosition(new Vector2(enemy1.getX(), enemy1.getY()));
                enemy1StatArrows.setPosition(enemy1.getX() + 1, enemy1.getY());

                enemy2.setSize(characterWidth, characterHeight);
                enemy2.addAction(Actions.fadeOut(0));
                enemy2.setVisible(true);
                enemy2.setPosition(getStartPosition("E2").x, getStartPosition("E2").y);
                if (enemy2.getEntity() != null)
                    enemy2.getEntity().setCurrentPosition(new Vector2(enemy2.getX(), enemy2.getY()));
                enemy2StatArrows.setPosition(enemy2.getX() + 1, enemy2.getY());

                enemy3.setSize(characterWidth, characterHeight);
                enemy3.addAction(Actions.fadeOut(0));
                enemy3.setVisible(true);
                enemy3.setPosition(getStartPosition("E3").x, getStartPosition("E3").y);
                if (enemy3.getEntity() != null)
                    enemy3.getEntity().setCurrentPosition(new Vector2(enemy3.getX(), enemy3.getY()));
                enemy3StatArrows.setPosition(enemy3.getX() + 1, enemy3.getY());

                enemy4.setSize(characterWidth, characterHeight);
                enemy4.addAction(Actions.fadeOut(0));
                enemy4.setVisible(true);
                enemy4.setPosition(getStartPosition("E4").x, getStartPosition("E4").y);
                if (enemy4.getEntity() != null)
                    enemy4.getEntity().setCurrentPosition(new Vector2(enemy4.getX(), enemy4.getY()));
                enemy4StatArrows.setPosition(enemy4.getX() + 1, enemy4.getY());

                enemy5.setSize(characterWidth, characterHeight);
                enemy5.addAction(Actions.fadeOut(0));
                enemy5.setVisible(true);
                enemy5.setPosition(getStartPosition("E5").x, getStartPosition("E5").y);
                if (enemy5.getEntity() != null)
                    enemy5.getEntity().setCurrentPosition(new Vector2(enemy5.getX(), enemy5.getY()));
                enemy5StatArrows.setPosition(enemy5.getX() + 1, enemy5.getY());
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
                                      if (party1.getEntity().isAlive()) {
                                          _game.battleState.setCurrentSelectedCharacter(party1.getEntity());
                                          selectedEntity = party1.getEntity();
                                      }
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
                                       if (party2.getEntity().isAlive()) {
                                           _game.battleState.setCurrentSelectedCharacter(party2.getEntity());
                                           selectedEntity = party2.getEntity();
                                       }
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
                                       if (party3.getEntity().isAlive()) {
                                           _game.battleState.setCurrentSelectedCharacter(party3.getEntity());
                                           selectedEntity = party3.getEntity();
                                       }
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
                                       if (party4.getEntity().isAlive()) {
                                           _game.battleState.setCurrentSelectedCharacter(party4.getEntity());
                                           selectedEntity = party4.getEntity();
                                       }
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
                                       if (party5.getEntity().isAlive()) {
                                           _game.battleState.setCurrentSelectedCharacter(party5.getEntity());
                                           selectedEntity = party5.getEntity();
                                       }
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
                                       if (enemy1.getEntity().isAlive()) {
                                           _game.battleState.setCurrentSelectedCharacter(enemy1.getEntity());
                                           selectedEntity = enemy1.getEntity();
                                       }
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
                                       if (enemy2.getEntity().isAlive()) {
                                           _game.battleState.setCurrentSelectedCharacter(enemy2.getEntity());
                                           selectedEntity = enemy2.getEntity();
                                       }
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
                                       if (enemy3.getEntity().isAlive()) {
                                           _game.battleState.setCurrentSelectedCharacter(enemy3.getEntity());
                                           selectedEntity = enemy3.getEntity();
                                       }
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
                                       if (enemy4.getEntity().isAlive()) {
                                           _game.battleState.setCurrentSelectedCharacter(enemy4.getEntity());
                                           selectedEntity = enemy4.getEntity();
                                       }
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
                                       if (enemy5.getEntity().isAlive()) {
                                           _game.battleState.setCurrentSelectedCharacter(enemy5.getEntity());
                                           selectedEntity = enemy5.getEntity();
                                       }
                                   }
                               }
                           }
        );
    }

    public class setCurrentBattleAnimations extends Action {
        Animation<TextureRegion> characterAnimation;
        Animation<TextureRegion> hitAnimation;
        Animation<TextureRegion> defenderAnimation;

        public setCurrentBattleAnimations(Animation<TextureRegion> characterAnimation, Animation<TextureRegion> hitAnimation, Animation<TextureRegion> defenderAnimation) {
            this.characterAnimation = characterAnimation;
            this.hitAnimation = hitAnimation;
            this.defenderAnimation = defenderAnimation;
        }

        @Override
        public boolean act(float delta) {
            currentCharacterAnimation = this.characterAnimation;
            currentHitAnimation = this.hitAnimation;
            currentDefenderAnimation = this.defenderAnimation;
            if (currentCharacterAnimation == null) {
                // reset for frame index
                _frameTime = 0;
            }
            return true;
        }
    }

    public class animationComplete extends Action {
        public animationComplete() {
        }

        @Override
        public boolean act (float delta) {
            _game.battleState.animationComplete();
            _isCameraFixed = true;
            return true; // An action returns true when it's completed
        }
    }

    public class fadeInCharacters extends Action {
        float duration;
        public fadeInCharacters(float duration) {
            this.duration = duration;
        }

        @Override
        public boolean act (float delta) {
            fadeInCharacters(duration);
            return true; // An action returns true when it's completed
        }
    }

    public class fadeOutCharacters extends Action {
        float duration;
        public fadeOutCharacters(float duration) {
            this.duration = duration;
        }

        @Override
        public boolean act (float delta) {
            fadeOutCharacters(duration);
            return true; // An action returns true when it's completed
        }
    }

    public class fadeOutScreen extends Action {
        float duration;
        public fadeOutScreen(float duration) {
            this.duration = duration;
        }

        @Override
        public boolean act (float delta) {
            blackScreen.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(duration)));
            battleHUD.fadeOutHUD(duration);
            return true; // An action returns true when it's completed
        }
    }

    public class fadeInScreen extends Action {
        float duration;
        public fadeInScreen(float duration) {
            this.duration = duration;
        }

        @Override
        public boolean act (float delta) {
            _isCameraFixed = true;
            blackScreen.addAction(Actions.fadeOut(duration));
            battleHUD.fadeInHUD(duration);
            return true; // An action returns true when it's completed
        }
    }

    public class showMainCharacterAnimation extends Action {
        AnimatedImage character;
        boolean show;

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

    public class showStatArrows extends Action {
        boolean show;

        public showStatArrows(boolean show) {
            this.show = show;
        }

        @Override
        public boolean act (float delta) {
            showStatusArrows = show;
            return true; // An action returns true when it's completed
        }
    }

    private void completeAllActions() {
        float delta = 1;

        // need to loop multiple times in case there is an embedded sequence
        // shouldn't ever need anymore than 5
        for (int i = 0; i < 5; i++) {
            if (party1 != null) party1.act(delta);
            if (party2 != null) party2.act(delta);
            if (party3 != null) party3.act(delta);
            if (party4 != null) party4.act(delta);
            if (party5 != null) party5.act(delta);
            if (enemy1 != null) enemy1.act(delta);
            if (enemy2 != null) enemy2.act(delta);
            if (enemy3 != null) enemy3.act(delta);
            if (enemy4 != null) enemy4.act(delta);
            if (enemy5 != null) enemy5.act(delta);
        }
    }

    public static AnimationState getAnimationState() { return animationState; }

    private Entity.AnimationType getWeaponAnimationType(Entity entity) {
        Entity.AnimationType animationType;

        // todo: need to get correct weapon from somewhere
        if (entity.getBattleEntityType() == Entity.BattleEntityType.PARTY) {
            animationType = Entity.AnimationType.SWORD_LEFT;
        }
        else {
            animationType = Entity.AnimationType.SWORD_RIGHT;
        }

        return animationType;
    }

    private Entity.AnimationType getHitType(Entity.AnimationType weaponAnimationType) {
        Entity.AnimationType hitType = null;

        switch (weaponAnimationType) {
            case SWORD_LEFT:
            case MACE_LEFT:
            case STAFF_LEFT:
                hitType = Entity.AnimationType.BLUNT_LEFT;
                break;
            case SWORD_RIGHT:
            case MACE_RIGHT:
            case STAFF_RIGHT:
                hitType = Entity.AnimationType.BLUNT_RIGHT;
                break;
            case DAGGER_LEFT:
                hitType = Entity.AnimationType.STAB_LEFT;
                break;
            case DAGGER_RIGHT:
                hitType = Entity.AnimationType.STAB_RIGHT;
                break;
            case PUNCH_LEFT:
                hitType = Entity.AnimationType.KNUCKLE_LEFT;
                break;
            case PUNCH_RIGHT:
                hitType = Entity.AnimationType.KNUCKLE_RIGHT;
                break;
        }

        return hitType;
    }

    private Action getAttackAction(Entity entity) {
        animationState = AnimationState.BATTLE;
        Hashtable<Entity.AnimationType, Animation<TextureRegion>> currentCharacterBattleAnimation;
        Entity.AnimationType walkTowardsVictim;
        Entity.AnimationType walkAwayFromVictim;
        Entity.AnimationType weaponAnimationType;
        float destinationX;

        weaponAnimationType = getWeaponAnimationType(entity);

        EntityFactory.EntityName entityName = EntityFactory.EntityName.valueOf(entity.getEntityConfig().getEntityID().toUpperCase());

        if (entity.getBattleEntityType() == Entity.BattleEntityType.PARTY) {
            walkTowardsVictim = Entity.AnimationType.WALK_LEFT;
            walkAwayFromVictim = Entity.AnimationType.WALK_RIGHT;
            destinationX = selectedEntity.getCurrentPosition().x + 1;
        }
        else {
            // Entity.BattleEntityType.ENEMY
            walkTowardsVictim = Entity.AnimationType.WALK_RIGHT;
            walkAwayFromVictim = Entity.AnimationType.WALK_LEFT;
            destinationX = selectedEntity.getCurrentPosition().x - 1;
        }

        currentCharacterBattleAnimation = getBattleAnimations(entityName);

        return Actions.sequence(

                new showStatArrows(false),
                myActions.new setWalkDirection(currentTurnCharacter, walkTowardsVictim),
                Actions.addAction(Actions.moveTo(destinationX, selectedEntity.getCurrentPosition().y,  0.75f, Interpolation.linear), currentTurnCharacter),

                Actions.delay(0.75f),
                myActions.new setWalkDirection(currentTurnCharacter, Entity.AnimationType.IDLE),
                Actions.delay(0.25f),
                new setCurrentBattleAnimations(currentCharacterBattleAnimation.get(weaponAnimationType),
                                                battleHitAnimations.get(getHitType(weaponAnimationType)), null),
                new showMainCharacterAnimation(currentTurnCharacter, false),
                // Framerate * # of Frames
                Actions.delay(0.4f),
                new setCurrentBattleAnimations(null, null, null),
                new showMainCharacterAnimation(currentTurnCharacter, true),

                myActions.new setWalkDirection(currentTurnCharacter, walkAwayFromVictim),
                Actions.addAction(Actions.moveTo(currentTurnCharacter.getX(), currentTurnCharacter.getY(), 0.75f, Interpolation.linear), currentTurnCharacter),
                Actions.delay(0.75f),

                // turn to face victim
                myActions.new setWalkDirection(currentTurnCharacter, walkTowardsVictim),
                myActions.new setWalkDirection(currentTurnCharacter, Entity.AnimationType.IDLE),

                new animationComplete(),
                new showStatArrows(true)
        );
    }

    private Action getBlockedAttackAction(Entity attacker, Entity defender) {
        animationState = AnimationState.BATTLE;
        Hashtable<Entity.AnimationType, Animation<TextureRegion>> currentAttackerBattleAnimation;
        Hashtable<Entity.AnimationType, Animation<TextureRegion>> currentDefenderBattleAnimation;
        Entity.AnimationType walkTowardsVictim;
        Entity.AnimationType walkAwayFromVictim;
        Entity.AnimationType walkDirectionToBlock;
        Entity.AnimationType walkDirectionFromBlock;
        Entity.AnimationType attackerWeaponAnimationType;
        Entity.AnimationType defenderWeaponAnimationType;
        float destinationX;

        attackerWeaponAnimationType = getWeaponAnimationType(attacker);
        defenderWeaponAnimationType = getWeaponAnimationType(defender);

        EntityFactory.EntityName attackerEntityName = EntityFactory.EntityName.valueOf(attacker.getEntityConfig().getEntityID().toUpperCase());
        EntityFactory.EntityName defenderEntityName = EntityFactory.EntityName.valueOf(defender.getEntityConfig().getEntityID().toUpperCase());

        if (attacker.getBattleEntityType() == Entity.BattleEntityType.PARTY) {
            walkTowardsVictim = Entity.AnimationType.WALK_LEFT;
            walkAwayFromVictim = Entity.AnimationType.WALK_RIGHT;
            destinationX = defender.getCurrentPosition().x + 1;
        }
        else {
            // Entity.BattleEntityType.ENEMY
            walkTowardsVictim = Entity.AnimationType.WALK_RIGHT;
            walkAwayFromVictim = Entity.AnimationType.WALK_LEFT;
            destinationX = defender.getCurrentPosition().x - 1;
        }

        if (defender.getCurrentPosition().y < selectedEntity.getCurrentPosition().y) {
            walkDirectionToBlock = Entity.AnimationType.WALK_UP;
            walkDirectionFromBlock = Entity.AnimationType.WALK_DOWN;
        }
        else {
            walkDirectionToBlock = Entity.AnimationType.WALK_DOWN;
            walkDirectionFromBlock = Entity.AnimationType.WALK_UP;
        }

        currentAttackerBattleAnimation = getBattleAnimations(attackerEntityName);
        currentDefenderBattleAnimation = getBattleAnimations(defenderEntityName);

        return Actions.sequence(
                new showStatArrows(false),
                myActions.new setWalkDirection(currentTurnCharacter, walkTowardsVictim),
                Actions.addAction(Actions.moveTo(destinationX, selectedEntity.getCurrentPosition().y,  0.75f, Interpolation.linear), currentTurnCharacter),

                Actions.delay(0.50f),

                // defender walk to block
                myActions.new setWalkDirection(defendingCharacter, walkDirectionToBlock),
                Actions.addAction(Actions.moveTo(defender.getCurrentPosition().x, selectedEntity.getCurrentPosition().y,  0.25f, Interpolation.linear), defendingCharacter),

                Actions.delay(0.25f),

                // defender turn to face attacker
                myActions.new setWalkDirection(defendingCharacter, walkAwayFromVictim),
                myActions.new setWalkDirection(defendingCharacter, Entity.AnimationType.IDLE),

                myActions.new setWalkDirection(currentTurnCharacter, Entity.AnimationType.IDLE),
                Actions.delay(0.25f),
                new setCurrentBattleAnimations(currentAttackerBattleAnimation.get(attackerWeaponAnimationType),
                                                battleHitAnimations.get(Entity.AnimationType.BLOCK_LEFT),
                                                currentDefenderBattleAnimation.get(defenderWeaponAnimationType)),
                new showMainCharacterAnimation(currentTurnCharacter, false),
                // Framerate * # of Frames
                Actions.delay(0.4f),
                new setCurrentBattleAnimations(null, null, null),
                new showMainCharacterAnimation(currentTurnCharacter, true),

                myActions.new setWalkDirection(currentTurnCharacter, walkAwayFromVictim),
                Actions.addAction(Actions.moveTo(currentTurnCharacter.getX(), currentTurnCharacter.getY(),0.75f, Interpolation.linear), currentTurnCharacter),

                // defender walk back into place
                myActions.new setWalkDirection(defendingCharacter, walkDirectionFromBlock),
                Actions.addAction(Actions.moveTo(defender.getCurrentPosition().x, defender.getCurrentPosition().y,0.75f, Interpolation.linear), defendingCharacter),

                Actions.delay(0.75f),

                // defender turn to face attacker
                myActions.new setWalkDirection(defendingCharacter, walkAwayFromVictim),
                myActions.new setWalkDirection(defendingCharacter, Entity.AnimationType.IDLE),

                // attacker turn to face victim
                myActions.new setWalkDirection(currentTurnCharacter, walkTowardsVictim),
                myActions.new setWalkDirection(currentTurnCharacter, Entity.AnimationType.IDLE),

                new animationComplete(),
                new showStatArrows(true)
        );
    }

    private Action getPlayerEscapeAction() {
        animationState = AnimationState.ESCAPED;
        _isCameraFixed = false;

        float duration = 3;
        float enemyDuration = duration * 1.3f;
        float tilesPerSec = 7.5f;
        Entity.AnimationType runDirection;

        // direction and destinations change if it's a back battle
        if (_game.battleState.isBackBattle()) {
            runDirection = Entity.AnimationType.RUN_LEFT;
            tilesPerSec *= -1;
        }
        else {
            runDirection = Entity.AnimationType.RUN_RIGHT;
        }

        cameraRunningOffset = party1.getX() - CAMERA_POS_X;

        float partyDestinationX_1_3_5 = party1.getX() + (tilesPerSec * duration);
        float partyDestinationX__2_4_ = party2.getX() + (tilesPerSec * duration);
        float enemyDestinationX__2_4_ = enemy2.getX() + (tilesPerSec * duration);
        float enemyDestinationX_1_3_5 = enemy1.getX() + (tilesPerSec * duration);

        party1.setCurrentAnimationType(runDirection);
        party2.setCurrentAnimationType(runDirection);
        party3.setCurrentAnimationType(runDirection);
        party4.setCurrentAnimationType(runDirection);
        party5.setCurrentAnimationType(runDirection);

        enemy1.setCurrentAnimationType(runDirection);
        enemy2.setCurrentAnimationType(runDirection);
        enemy3.setCurrentAnimationType(runDirection);
        enemy4.setCurrentAnimationType(runDirection);
        enemy5.setCurrentAnimationType(runDirection);

        return Actions.sequence(
                new showStatArrows(false),
                Actions.parallel(
                    Actions.addAction(Actions.moveTo(partyDestinationX_1_3_5, party1.getY(),  duration, Interpolation.linear), party1),
                    Actions.addAction(Actions.moveTo(partyDestinationX__2_4_, party2.getY(),  duration, Interpolation.linear), party2),
                    Actions.addAction(Actions.moveTo(partyDestinationX_1_3_5, party3.getY(),  duration, Interpolation.linear), party3),
                    Actions.addAction(Actions.moveTo(partyDestinationX__2_4_, party4.getY(),  duration, Interpolation.linear), party4),
                    Actions.addAction(Actions.moveTo(partyDestinationX_1_3_5, party5.getY(),  duration, Interpolation.linear), party5),

                    Actions.addAction(Actions.moveTo(enemyDestinationX_1_3_5, enemy1.getY(),  enemyDuration, Interpolation.linear), enemy1),
                    Actions.addAction(Actions.moveTo(enemyDestinationX__2_4_, enemy2.getY(),  enemyDuration, Interpolation.linear), enemy2),
                    Actions.addAction(Actions.moveTo(enemyDestinationX_1_3_5, enemy3.getY(),  enemyDuration, Interpolation.linear), enemy3),
                    Actions.addAction(Actions.moveTo(enemyDestinationX__2_4_, enemy4.getY(),  enemyDuration, Interpolation.linear), enemy4),
                    Actions.addAction(Actions.moveTo(enemyDestinationX_1_3_5, enemy5.getY(),  enemyDuration, Interpolation.linear), enemy5)
                ),

                Actions.delay(duration * 0.5f),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, duration * 0.5f), _transitionActor),
                new fadeOutCharacters(duration * 0.5f),
                new fadeOutScreen(duration * 0.5f),

                Actions.delay(duration * 0.5f),
                myActions.new setImageVisible(blackScreen, false),
                new animationComplete()
        );
    }

    private Action fadeOutAction() {
        // delay here should match PlayerHUD getSetGameOverScreenTimer() and GameOverScreen fadeIn()
        float duration = 1.0f;
        return Actions.sequence(
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, duration), _transitionActor),
                new fadeOutScreen(duration),
                Actions.delay(duration),
                myActions.new setImageVisible(blackScreen, false)
        );
    }

    private Action getPlayerFailedEscapeAction() {
        animationState = AnimationState.FAILED_ESCAPE;
        _isCameraFixed = false;

        float duration = 3;
        float tilesPerSec = 7.5f;
        float enemyDurationFactor = 1f;
        Entity.AnimationType runDirection;
        Entity.AnimationType battlePositionParty = Entity.AnimationType.WALK_LEFT;
        Entity.AnimationType battlePositionEnemy = Entity.AnimationType.WALK_RIGHT;

        // direction and destinations change if it's a back battle
        if (_game.battleState.isBackBattle()) {
            runDirection = Entity.AnimationType.RUN_LEFT;
            tilesPerSec *= -1;
        }
        else {
            runDirection = Entity.AnimationType.RUN_RIGHT;
        }

        cameraRunningOffset = party1.getX() - CAMERA_POS_X;

        float partyDestinationX_1_3_5 = party1.getX() + (tilesPerSec * duration);
        float partyDestinationX__2_4_ = party2.getX() + (tilesPerSec * duration);
        float enemyDestinationX__2_4_ = party1.getX() + (tilesPerSec * duration) - party1.getWidth() / 2;
        float enemyDestinationX_1_3_5 = party2.getX() + (tilesPerSec * duration) - party2.getWidth() / 2;

        party1.setCurrentAnimationType(runDirection);
        party2.setCurrentAnimationType(runDirection);
        party3.setCurrentAnimationType(runDirection);
        party4.setCurrentAnimationType(runDirection);
        party5.setCurrentAnimationType(runDirection);

        enemy1.setCurrentAnimationType(runDirection);
        enemy2.setCurrentAnimationType(runDirection);
        enemy3.setCurrentAnimationType(runDirection);
        enemy4.setCurrentAnimationType(runDirection);
        enemy5.setCurrentAnimationType(runDirection);

        return Actions.sequence(
                new showStatArrows(false),
                Actions.parallel(
                    Actions.addAction(Actions.moveTo(partyDestinationX_1_3_5, party1.getY(),  duration, Interpolation.linear), party1),
                    Actions.addAction(Actions.moveTo(partyDestinationX__2_4_, party2.getY(),  duration, Interpolation.linear), party2),
                    Actions.addAction(Actions.moveTo(partyDestinationX_1_3_5, party3.getY(),  duration, Interpolation.linear), party3),
                    Actions.addAction(Actions.moveTo(partyDestinationX__2_4_, party4.getY(),  duration, Interpolation.linear), party4),
                    Actions.addAction(Actions.moveTo(partyDestinationX_1_3_5, party5.getY(),  duration, Interpolation.linear), party5),

                    Actions.addAction(Actions.moveTo(enemyDestinationX_1_3_5, enemy1.getY(),  duration * enemyDurationFactor, Interpolation.linear), enemy1),
                    Actions.addAction(Actions.moveTo(enemyDestinationX__2_4_, enemy2.getY(),  duration * enemyDurationFactor, Interpolation.linear), enemy2),
                    Actions.addAction(Actions.moveTo(enemyDestinationX_1_3_5, enemy3.getY(),  duration * enemyDurationFactor, Interpolation.linear), enemy3),
                    Actions.addAction(Actions.moveTo(enemyDestinationX__2_4_, enemy4.getY(),  duration * enemyDurationFactor, Interpolation.linear), enemy4),
                    Actions.addAction(Actions.moveTo(enemyDestinationX_1_3_5, enemy5.getY(),  duration * enemyDurationFactor, Interpolation.linear), enemy5)
                ),

                Actions.delay(duration * 0.5f),

                // fade out
                // Note: need ScreenTransitionActions here in addition to fadeOutScreen and fadeInScreen calls,
                // otherwise the screen flashes between fading out and fading back in
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, duration * 0.5f), _transitionActor),
                new fadeOutCharacters(duration * 0.5f),
                new fadeOutScreen(duration * 0.5f),
                Actions.delay(duration * 0.5f),
                myActions.new setImageVisible(blackScreen, false),

                // fade back in to battle
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, duration * 0.5f), _transitionActor),
                myActions.new setImageVisible(blackScreen, true),
                new fadeInScreen(duration * 0.5f),

                // reset characters
                setupBattleScene,
                new fadeInCharacters(0), // need to fade characters back in because setupBattleScene faded them out
                myActions.new setWalkDirection(party1, battlePositionParty),
                myActions.new setWalkDirection(party1, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(party2, battlePositionParty),
                myActions.new setWalkDirection(party2, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(party3, battlePositionParty),
                myActions.new setWalkDirection(party3, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(party4, battlePositionParty),
                myActions.new setWalkDirection(party4, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(party5, battlePositionParty),
                myActions.new setWalkDirection(party5, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(enemy1, battlePositionEnemy),
                myActions.new setWalkDirection(enemy1, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(enemy2, battlePositionEnemy),
                myActions.new setWalkDirection(enemy2, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(enemy3, battlePositionEnemy),
                myActions.new setWalkDirection(enemy3, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(enemy4, battlePositionEnemy),
                myActions.new setWalkDirection(enemy4, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(enemy5, battlePositionEnemy),
                myActions.new setWalkDirection(enemy5, Entity.AnimationType.IDLE),

                // reset camera
                myActions.new setCameraPosition(_camera, CAMERA_POS_X, CAMERA_POS_Y),

                Actions.delay(0.5f),
                myActions.new setImageVisible(blackScreen, false),
                new animationComplete(),
                new showStatArrows(true)
        );
    }

    private Hashtable<Entity.AnimationType, Animation<TextureRegion>> getBattleAnimations(EntityFactory.EntityName entityName) {
        switch(entityName) {
            // Party
            case CARMEN:
                return carmenBattleAnimations;
            case CHARACTER_1:
                return char1BattleAnimations;
            case CHARACTER_2:
                return char2BattleAnimations;
            case JUSTIN:
                return justinBattleAnimations;
            case JAXON_1:
                return jaxonBattleAnimations;

            //Enemies
            case DOUGLAS:
                return douglasBattleAnimations;
            case ROYAL_GUARD:
                return royalGuardBattleAnimations;
            case STEVE:
                return  steveBattleAnimations;
        }

        return null;
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

    private void fadeInCharacters(float fadeTime) {
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

    private void fadeOutCharacters(float fadeTime) {
        party1.addAction(Actions.fadeOut(fadeTime));
        party2.addAction(Actions.fadeOut(fadeTime));
        party3.addAction(Actions.fadeOut(fadeTime));
        party4.addAction(Actions.fadeOut(fadeTime));
        party5.addAction(Actions.fadeOut(fadeTime));
        enemy1.addAction(Actions.fadeOut(fadeTime));
        enemy2.addAction(Actions.fadeOut(fadeTime));
        enemy3.addAction(Actions.fadeOut(fadeTime));
        enemy4.addAction(Actions.fadeOut(fadeTime));
        enemy5.addAction(Actions.fadeOut(fadeTime));
    }

    @Override
    public void show() {
        completeAllActions();

        _stage.addAction(getBattleSceneAction());

        ProfileManager.getInstance().addObserver(_mapMgr);

        Gdx.input.setInputProcessor(_multiplexer);

        if( _mapRenderer == null ){
            _mapRenderer = new OrthogonalTiledMapRenderer(_mapMgr.getCurrentTiledMap(), Map.UNIT_SCALE);
        }

        fadeInCharacters(0.5f);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        ProfileManager.getInstance().removeObserver(_mapMgr);

        party1.setEntity(null);
        party2.setEntity(null);
        party3.setEntity(null);
        party4.setEntity(null);
        party5.setEntity(null);
        enemy1.setEntity(null);
        enemy2.setEntity(null);
        enemy3.setEntity(null);
        enemy4.setEntity(null);
        enemy5.setEntity(null);

        isFirstTime = true;
    }

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

        if( !_isCameraFixed ){
            _camera.position.set(party1.getX() - cameraRunningOffset, _camera.position.y, 0f);
        }

        _camera.update();

        _stage.act(delta);
        _stage.draw();

        battleHUD.render(delta);

        battleControls.render(delta);

        currentTurnFlashTimer += delta;

        //////////////////////////////////////////////////////
        // Flashing turn indicator (only when camera is fixed)
        if (_isCameraFixed) {
            if (currentTurnFlashTimer < 0.5f) {
                _mapRenderer.getBatch().begin();

                if (currentTurnEntity != null) {
                    _mapRenderer.getBatch().draw(currentTurnIndicator, currentTurnEntity.getCurrentPosition().x + characterWidth / 2 * 0.5f, currentTurnEntity.getCurrentPosition().y + characterHeight * 1.1f, 0.5f, 0.5f);
                }

                if (selectedEntity != null) {
                    _mapRenderer.getBatch().draw(selectedEntityIndicator, selectedEntity.getCurrentPosition().x + characterWidth / 2 * 0.5f, selectedEntity.getCurrentPosition().y + characterHeight * 1.1f, 0.5f, 0.5f);
                }

                _mapRenderer.getBatch().end();
            }
            else if (currentTurnFlashTimer > 0.75f) {
                currentTurnFlashTimer = 0;
            }
        }

        //////////////////////////////////////////////////////
        // Battle animation
        if (currentCharacterAnimation != null && currentTurnCharacter != null && currentHitAnimation != null) {
            _frameTime = (_frameTime + delta) % 5;
            _currentFrame = currentCharacterAnimation.getKeyFrame(_frameTime);
            currentHitFrame = currentHitAnimation.getKeyFrame(_frameTime);

            if (currentDefenderAnimation != null)
                currentDefenderFrame = currentDefenderAnimation.getKeyFrame(_frameTime);

            _mapRenderer.getBatch().begin();
            if (_currentFrame != null) {
                //draw attacker animation
                float regionWidth = _currentFrame.getRegionWidth() * Map.UNIT_SCALE;
                float regionHeight = _currentFrame.getRegionHeight() * Map.UNIT_SCALE;
                float hitRegionWidth = currentHitFrame.getRegionWidth() * Map.UNIT_SCALE;
                float hitRegionHeight = currentHitFrame.getRegionHeight() * Map.UNIT_SCALE;

                //adjust for character width/height vs. animation region width/height
                float adjustWidth = (regionWidth - characterWidth) / 2;
                float adjustHeight = (regionHeight - characterHeight) / 2;
                _mapRenderer.getBatch().draw(_currentFrame, currentTurnCharacter.getX() - adjustWidth, currentTurnCharacter.getY() - adjustHeight, regionWidth, regionHeight);

                if (currentHitFrame != null && selectedEntity != null) {
                    //draw hit animation
                    _mapRenderer.getBatch().draw(currentHitFrame, selectedEntity.getCurrentPosition().x, selectedEntity.getCurrentPosition().y, hitRegionWidth, hitRegionHeight);
                }

                if (currentDefenderAnimation != null) {
                    //draw defender animation
                    currentDefenderFrame = currentDefenderAnimation.getKeyFrame(_frameTime);

                    if (currentDefenderFrame != null) {
                        regionWidth = currentDefenderFrame.getRegionWidth() * Map.UNIT_SCALE;
                        regionHeight = currentDefenderFrame.getRegionHeight() * Map.UNIT_SCALE;

                        //adjust for character width/height vs. animation region width/height
                        adjustWidth = (regionWidth - characterWidth) / 2;
                        adjustHeight = (regionHeight - characterHeight) / 2;

                        _mapRenderer.getBatch().draw(currentDefenderFrame, defendingCharacter.getX() - adjustWidth, defendingCharacter.getY() - adjustHeight, regionWidth, regionHeight);
                    }

                }
            }
            _mapRenderer.getBatch().end();
        }

        if (showStatusArrows) {
            party1StatArrows.render(delta, _mapRenderer);
            party2StatArrows.render(delta, _mapRenderer);
            party3StatArrows.render(delta, _mapRenderer);
            party4StatArrows.render(delta, _mapRenderer);
            party5StatArrows.render(delta, _mapRenderer);
            enemy1StatArrows.render(delta, _mapRenderer);
            enemy2StatArrows.render(delta, _mapRenderer);
            enemy3StatArrows.render(delta, _mapRenderer);
            enemy4StatArrows.render(delta, _mapRenderer);
            enemy5StatArrows.render(delta, _mapRenderer);
        }
    }

    @Override
    public void resize(int width, int height) {
        if (isFirstTime) {
            setupViewport(V_WIDTH, V_HEIGHT);
            _camera.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);

            if (battleHUD != null)
                battleHUD.resize((int) VIEWPORT.physicalWidth, (int) VIEWPORT.physicalHeight);
        }
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
        float duration = 1;
        setupBattleScene.reset();
        return Actions.sequence(
                Actions.addAction(setupBattleScene),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, duration), _transitionActor),
                Actions.delay(duration),
                myActions.new setImageVisible(blackScreen, false)
                //new fadeInScreen(0) //was causing flash
        );
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
        // make sure fade in image is on top of z order
        blackScreen.remove();

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

        _stage.addActor(blackScreen);
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

        // make sure fade in image is on top of z order
        blackScreen.remove();

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

        _stage.addActor(blackScreen);
    }

    public void removeOpponentByIndex(int index) {

        // remove actor from the stage
        switch (index) {
            case 1:
                removeOpponent(enemy1);
                break;
            case 2:
                removeOpponent(enemy2);
                break;
            case 3:
                removeOpponent(enemy3);
                break;
            case 4:
                removeOpponent(enemy4);
                break;
            case 5:
                removeOpponent(enemy5);
                break;
        }
    }

    private Timer.Task removeOpponent(final AnimatedImage enemy) {
        return new Timer.Task() {
            @Override
            public void run() {
                enemy.remove();
            }
        };
    }

    public Vector2 getEntityCoordinates(Entity entity) {
        Vector2 coordinates = null;
        int position = entity.getBattlePosition();
        Entity.BattleEntityType entityType = entity.getBattleEntityType();

        if (entityType == Entity.BattleEntityType.ENEMY) {
            switch (position) {
                case 1:
                    coordinates = new Vector2(enemy1.getX(), enemy1.getY());
                    break;
                case 2:
                    coordinates = new Vector2(enemy2.getX(), enemy2.getY());
                    break;
                case 3:
                    coordinates = new Vector2(enemy3.getX(), enemy3.getY());
                    break;
                case 4:
                    coordinates = new Vector2(enemy4.getX(), enemy4.getY());
                    break;
                case 5:
                    coordinates = new Vector2(enemy5.getX(), enemy5.getY());
                    break;
            }
        }
        else {
            // PARTY
            switch (position) {
                case 1:
                    coordinates = new Vector2(party1.getX(), party1.getY());
                    break;
                case 2:
                    coordinates = new Vector2(party2.getX(), party2.getY());
                    break;
                case 3:
                    coordinates = new Vector2(party3.getX(), party3.getY());
                    break;
                case 4:
                    coordinates = new Vector2(party4.getX(), party4.getY());
                    break;
                case 5:
                    coordinates = new Vector2(party5.getX(), party5.getY());
                    break;
            }
        }
        return coordinates;
    }

    private Image getDigitImage(char c) {
        Image digitImage;

        if (c == '1')
            digitImage = new Image(new Texture("graphics/1.png"));
        else if (c == '2')
            digitImage = new Image(new Texture("graphics/2.png"));
        else if (c == '3')
            digitImage = new Image(new Texture("graphics/3.png"));
        else if (c == '4')
            digitImage = new Image(new Texture("graphics/4.png"));
        else if (c == '5')
            digitImage = new Image(new Texture("graphics/5.png"));
        else if (c == '6')
            digitImage = new Image(new Texture("graphics/6.png"));
        else if (c == '7')
            digitImage = new Image(new Texture("graphics/7.png"));
        else if (c == '8')
            digitImage = new Image(new Texture("graphics/8.png"));
        else if (c == '9')
            digitImage = new Image(new Texture("graphics/9.png"));
        else if (c == '0')
            digitImage = new Image(new Texture("graphics/0.png"));
        else
            digitImage = new Image(new Texture("graphics/0.png"));

        return digitImage;
    }

    private AnimatedImage getAnimatedImageFromEntity(Entity entity) {
        if (enemy1.getEntity() != null) {
            if (enemy1.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                return enemy1;
            }
        }
        if (enemy2.getEntity() != null) {
            if (enemy2.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                return enemy2;
            }
        }
        if (enemy3.getEntity() != null) {
            if (enemy3.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                return enemy3;
            }
        }
        if (enemy4.getEntity() != null) {
            if (enemy4.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                return enemy4;
            }
        }
        if (enemy5.getEntity() != null) {
            if (enemy5.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                return enemy5;
            }
        }
        if (party1.getEntity() != null) {
            if (party1.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                return party1;
            }
        }
        if (party2.getEntity() != null) {
            if (party2.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                return party2;
            }
        }
        if (party3.getEntity() != null) {
            if (party3.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                return party3;
            }
        }
        if (party4.getEntity() != null) {
            if (party4.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                return party4;
            }
        }
        if (party5.getEntity() != null) {
            if (party5.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                return party5;
            }
        }

        return null;
    }

    private void hitPointAnimation(Entity entity, String hitPoints) {
        AnimatedImage character = getAnimatedImageFromEntity(entity);

        if (character != null) {
            float xOffset1 = 2.0f;
            float xOffset2 = 3.75f;
            if (character.getEntity().getBattleEntityType() == Entity.BattleEntityType.PARTY) {
                xOffset1 *= -1;
                xOffset2 *= -1;
            }

            hitPointFloaterTable.clear();
            hitPointFloaterTable.setSize(hitPoints.length(), 1);

            for (int i = 0; i < hitPoints.length(); i++) {
                hitPointFloaterTable.add(getDigitImage(hitPoints.charAt(i)));
            }

            hitPointFloaterTable.setPosition(character.getX() + character.getWidth() / 2 - hitPointFloaterTable.getWidth() / 2,
                    character.getY() + character.getHeight() * 1.2f);

            hitPointFloaterTable.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));

            // Interpolation: https://github.com/libgdx/libgdx/wiki/Interpolation
            hitPointFloaterTable.addAction(Actions.sequence(
                    /*
                    Actions.moveTo(hitPointFloaterTable.getX() + xOffset1, hitPointFloaterTable.getY() + 0.75f, 0.5f, Interpolation.circle),
                    Actions.parallel(
                            Actions.moveTo(hitPointFloaterTable.getX() + xOffset2, 4.45f, 1f, Interpolation.bounceOut),
                            Actions.fadeOut(2.5f)
                    )
                    */
                    Actions.moveTo(hitPointFloaterTable.getX() + xOffset1, hitPointFloaterTable.getY() + 0.75f, 0.5f, Interpolation.circleOut),
                    Actions.parallel(
                            Actions.moveTo(hitPointFloaterTable.getX() + xOffset2, 4.45f, 1f, Interpolation.bounceOut),
                            Actions.fadeOut(2.5f)
                    )
            ));
        }
    }

    @Override
    public void onNotify(Entity entity, BattleEvent event) {
        switch (event) {
            case CHARACTER_TURN_CHANGED:
                currentTurnEntity = entity;
                selectedEntity = null;
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
            case PLAYER_ESCAPED:
                _stage.addAction(getPlayerEscapeAction());
                selectedEntity = null;
                break;
            case PLAYER_FAILED_TO_ESCAPE:
                _stage.addAction(getPlayerFailedEscapeAction());
                selectedEntity = null;
                break;
            case OPPONENT_DEFEATED:
                float fadeOutTime = 1;
                selectedEntity = null;

                if (enemy1.getEntity() != null) {
                    if (enemy1.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                        enemy1.addAction(Actions.fadeOut(fadeOutTime));
                        if (!removeOpponent(enemy1).isScheduled()) {
                            Timer.schedule(removeOpponent(enemy1), fadeOutTime);
                        }
                    }
                }
                if (enemy2.getEntity() != null) {
                    if (enemy2.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                        enemy2.addAction(Actions.fadeOut(fadeOutTime));
                        if (!removeOpponent(enemy2).isScheduled()) {
                            Timer.schedule(removeOpponent(enemy2), fadeOutTime);
                        }
                    }
                }
                if (enemy3.getEntity() != null) {
                    if (enemy3.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                        enemy3.addAction(Actions.fadeOut(fadeOutTime));
                        if (!removeOpponent(enemy3).isScheduled()) {
                            Timer.schedule(removeOpponent(enemy3), fadeOutTime);
                        }
                    }
                }
                if (enemy4.getEntity() != null) {
                    if (enemy4.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                        enemy4.addAction(Actions.fadeOut(fadeOutTime));
                        if (!removeOpponent(enemy4).isScheduled()) {
                            Timer.schedule(removeOpponent(enemy4), fadeOutTime);
                        }
                    }
                }
                if (enemy5.getEntity() != null) {
                    if (enemy5.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                        enemy5.addAction(Actions.fadeOut(fadeOutTime));
                        if (!removeOpponent(enemy5).isScheduled()) {
                            Timer.schedule(removeOpponent(enemy5), fadeOutTime);
                        }
                    }
                }
                break;
            case GAME_OVER:
            case BATTLE_OVER:
                currentTurnCharacter = null;
                currentTurnEntity = null;
                selectedEntity = null;
                blackScreen.setVisible(true);
                _stage.addAction(fadeOutAction());
                break;
            case BATTLE_WON:
                currentTurnEntity = null;
                break;
        }
    }

    @Override
    public void onNotify(Entity sourceEntity, Entity destinationEntity, BattleEventWithMessage event, String message) {
        switch (event) {
            case PLAYER_ATTACKS:
                _stage.addAction(getAttackAction(sourceEntity));
                break;
            case PLAYER_TURN_DONE:
                selectedEntity = null;
                break;
            case PLAYER_HIT_DAMAGE:
                hitPointAnimation(destinationEntity, message);
                break;
            case OPPONENT_ATTACKS:
                selectedEntity = destinationEntity;
                _stage.addAction(getAttackAction(sourceEntity));
                break;
            case OPPONENT_TURN_DONE:
                selectedEntity = null;
                break;
            case OPPONENT_HIT_DAMAGE:
                hitPointAnimation(destinationEntity, message);
                break;
            case ATTACK_BLOCKED:
                // sourceEntity is the attacker
                // destinationEntity is the defender
                switch (destinationEntity.getBattlePosition()) {
                    case 1:
                        if (destinationEntity.getBattleEntityType().equals(Entity.BattleEntityType.PARTY))
                            defendingCharacter = party1;
                        else
                            defendingCharacter = enemy1;
                        break;
                    case 2:
                        if (destinationEntity.getBattleEntityType().equals(Entity.BattleEntityType.PARTY))
                            defendingCharacter = party2;
                        else
                            defendingCharacter = enemy2;
                        break;
                    case 3:
                        if (destinationEntity.getBattleEntityType().equals(Entity.BattleEntityType.PARTY))
                            defendingCharacter = party3;
                        else
                            defendingCharacter = enemy3;
                        break;
                    case 4:
                        if (destinationEntity.getBattleEntityType().equals(Entity.BattleEntityType.PARTY))
                            defendingCharacter = party4;
                        else
                            defendingCharacter = enemy4;
                        break;
                    case 5:
                        if (destinationEntity.getBattleEntityType().equals(Entity.BattleEntityType.PARTY))
                            defendingCharacter = party5;
                        else
                            defendingCharacter = enemy5;
                        break;
                }
                _stage.addAction(getBlockedAttackAction(sourceEntity, destinationEntity));
                break;
        }
    }

    @Override
    public void onNotify(Entity entity, InventoryElement.Effect effect) {
        StatusArrows statusArrows = getStatArrows(entity);
        String effectString = effect.toString();
        String property = effectString.substring(0, effectString.indexOf("_"));
        String name;

        if (entity.getBattleEntityType().equals(Entity.BattleEntityType.PARTY)) {
            name = property + "_UP_LEFT";
            statusArrows.remove(EntityFactory.EntityName.valueOf(name));
            name = property + "_DOWN_LEFT";
            statusArrows.remove(EntityFactory.EntityName.valueOf(name));

            if (!effectString.contains("NORMAL")) {
                name = effectString + "_LEFT";
                statusArrows.add(EntityFactory.EntityName.valueOf(name));
            }
        }
        else {
            // ENEMY
            name = property + "_UP_RIGHT";
            statusArrows.remove(EntityFactory.EntityName.valueOf(name));
            name = property + "_DOWN_RIGHT";
            statusArrows.remove(EntityFactory.EntityName.valueOf(name));

            if (!effectString.contains("NORMAL")) {
                name = effectString + "_RIGHT";
                statusArrows.add(EntityFactory.EntityName.valueOf(name));
            }
        }
    }

    private StatusArrows getStatArrows(Entity entity) {
        if (enemy1.getEntity() != null) {
            if (enemy1.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                return enemy1StatArrows;
            }
        }
        if (enemy2.getEntity() != null) {
            if (enemy2.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                return enemy2StatArrows;
            }
        }
        if (enemy3.getEntity() != null) {
            if (enemy3.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                return enemy3StatArrows;
            }
        }
        if (enemy4.getEntity() != null) {
            if (enemy4.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                return enemy4StatArrows;
            }
        }
        if (enemy5.getEntity() != null) {
            if (enemy5.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                return enemy5StatArrows;
            }
        }
        if (party1.getEntity() != null) {
            if (party1.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                return party1StatArrows;
            }
        }
        if (party2.getEntity() != null) {
            if (party2.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                return party2StatArrows;
            }
        }
        if (party3.getEntity() != null) {
            if (party3.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                return party3StatArrows;
            }
        }
        if (party4.getEntity() != null) {
            if (party4.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                return party4StatArrows;
            }
        }
        if (party5.getEntity() != null) {
            if (party5.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                return party5StatArrows;
            }
        }

        return null;
    }
}
