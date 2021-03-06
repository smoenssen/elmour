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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.smoftware.elmour.main.ElmourGame;
import com.smoftware.elmour.entities.Entity;
import com.smoftware.elmour.entities.EntityFactory;
import com.smoftware.elmour.components.GraphicsComponent;
import com.smoftware.elmour.inventory.InventoryElement;
import com.smoftware.elmour.UI.graphics.AnimatedImage;
import com.smoftware.elmour.UI.controls.BattleControls;
import com.smoftware.elmour.UI.huds.BattleHUD;
import com.smoftware.elmour.actions.MyActions;
import com.smoftware.elmour.UI.graphics.StatusArrows;
import com.smoftware.elmour.main.Utility;
import com.smoftware.elmour.audio.AudioManager;
import com.smoftware.elmour.battle.BattleObserver;
import com.smoftware.elmour.maps.Map;
import com.smoftware.elmour.maps.MapFactory;
import com.smoftware.elmour.maps.MapManager;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.sfx.ScreenTransitionAction;
import com.smoftware.elmour.sfx.ScreenTransitionActor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.smoftware.elmour.battle.BattleObserver.BattleEventWithMessage.PLAYER_THROWING_ITEM_BUT_MISSED;

/**
 * Created by moenssr on 3/1/2018.
 */

public class BattleScreen extends MainGameScreen implements BattleObserver {

    private static final String TAG = BattleScreen.class.getSimpleName();

    public enum AnimationState {BATTLE, ESCAPED, FAILED_ESCAPE, NONE}
/*
    public class CharacterLayerComparator implements Comparator<AnimatedImage> {
        @Override
        public int compare(AnimatedImage arg0, AnimatedImage arg1) {
            float y0 = arg0.getY();
            float y1 = arg1.getY();
            if (y0 > y1) {
                return -1;
            }
            else if (y0 == y1) {
                return 0;
            }
            else {
                return 1;
            }
        }
    }
*/
    public class CharacterLayerComparator implements Comparator<AnimatedImageWithShadow> {
        @Override
        public int compare(AnimatedImageWithShadow arg0, AnimatedImageWithShadow arg1) {
            float y0 = arg0.character.getY();
            float y1 = arg1.character.getY();
            if (y0 > y1) {
                return -1;
            }
            else if (y0 == y1) {
                return 0;
            }
            else {
                return 1;
            }
        }
    }

    class BattleBurst {
        Array<Image> imageArray;
        boolean show;
        boolean isDelayed;
        float positionX;
        float positionY;
        float velocityX;
        float velocityY;

        public BattleBurst() {
            show = false;
            isDelayed = false;
            imageArray = new Array<>();
        }
    }

    public enum ThrowingDirection { LEFT, RIGHT }

    class ThrowingItem {
        boolean show;
        float positionX;
        float positionY;
        float endPositionX;
        float endPositionY;
        float velocityX;
        float velocityY;
        ThrowingDirection throwingDirection;

        float frameTime;
        TextureRegion currentFrame;
        Animation<TextureRegion> animation;

        public ThrowingItem() {
            show = false;
            throwingDirection = ThrowingDirection.LEFT;
        }
    }

    public class throwItem extends Action {
        Entity.AnimationType animationType;
        boolean isMissHit;

        public throwItem(Entity.AnimationType animationType, boolean isMissHit) {
            this.animationType = animationType;
            this.isMissHit = isMissHit;
        }

        @Override
        public boolean act (float delta) {
            initThrowingItem(animationType, isMissHit);
            return true; // An action returns true when it's completed
        }
    }

    public class AnimatedImageWithShadow {
        AnimatedImage character;
        AnimatedImage shadow;

        AnimatedImageWithShadow(AnimatedImage character, AnimatedImage shadow) {
            this.character = character;
            this.shadow = shadow;
        }
    }

    private final float V_WIDTH = 11;
    private final float V_HEIGHT = 11;
    private final float CAMERA_POS_X = 40;
    private final float CAMERA_POS_Y = 6;
    private float cameraRunningOffset = 0;

    private final String CRIT_HIT = "CRIT";
    private final String MISS_HIT = "MISS";
    private final String WEAK_HIT = "WEAK";

    private Hashtable<Entity.AnimationType, Animation<TextureRegion>> carmenBattleAnimations;
    private Hashtable<Entity.AnimationType, Animation<TextureRegion>> char1BattleAnimations;
    private Hashtable<Entity.AnimationType, Animation<TextureRegion>> char2BattleAnimations;
    private Hashtable<Entity.AnimationType, Animation<TextureRegion>> justinBattleAnimations;
    private Hashtable<Entity.AnimationType, Animation<TextureRegion>> jaxonBattleAnimations;

    private Hashtable<Entity.AnimationType, Animation<TextureRegion>> douglasBattleAnimations;
    private Hashtable<Entity.AnimationType, Animation<TextureRegion>> royalGuardBattleAnimations;
    private Hashtable<Entity.AnimationType, Animation<TextureRegion>> steveBattleAnimations;
    private Hashtable<Entity.AnimationType, Animation<TextureRegion>> steve2BattleAnimations;
    private Hashtable<Entity.AnimationType, Animation<TextureRegion>> steve3BattleAnimations;

    private Hashtable<Entity.AnimationType, Animation<TextureRegion>> battleHitAnimations;
    private Hashtable<Entity.AnimationType, Animation<TextureRegion>> weaponAnimations;

    protected TextureRegion _currentFrame = null;
    protected TextureRegion currentHitFrame = null;
    protected TextureRegion currentDefenderFrame = null;
    protected TextureRegion currentCharacterWeaponFrame = null;
    protected TextureRegion currentDefenderWeaponFrame = null;
    private float _frameTime = 0;
    private float hitFrameTime = 0;
    private Animation<TextureRegion> currentCharacterAnimation;
    private Animation<TextureRegion> currentCharacterWeaponAnimation;
    private Animation<TextureRegion> currentHitAnimation;
    private Animation<TextureRegion> currentDefenderAnimation;
    private Animation<TextureRegion> currentDefenderWeaponAnimation;

    protected OrthogonalTiledMapRenderer _mapRenderer = null;
    protected MapManager _mapMgr;
    protected OrthographicCamera _camera = null;
    protected OrthographicCamera _hudCamera = null;

    private Json _json;
    private ElmourGame _game;
    private InputMultiplexer _multiplexer;

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
    private static MapFactory.MapType mapType;

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

    private AnimatedImage party1Shadow;
    private AnimatedImage party2Shadow;
    private AnimatedImage party3Shadow;
    private AnimatedImage party4Shadow;
    private AnimatedImage party5Shadow;
    private AnimatedImage enemy1Shadow;
    private AnimatedImage enemy2Shadow;
    private AnimatedImage enemy3Shadow;
    private AnimatedImage enemy4Shadow;
    private AnimatedImage enemy5Shadow;

    private Hashtable<AnimatedImage, AnimatedImage> shadowMap;

    float shadowYOffset = 0.15f;

    private ArrayList<AnimatedImageWithShadow> characterShadowSortList;

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

    Entity.AnimationType weaponCategoryAnimationType;
    Entity.AnimationType characterWeaponIdAnimationType;
    Entity.AnimationType defenderWeaponIdAnimationType;
    Entity.AnimationType defenderWeaponAnimationType;

    private float gravity;
    private float battleHUDHeight = 4.5f;
    private float bounceVelocityY;
    private CopyOnWriteArrayList<BattleBurst> battleBursts; // a thread-safe Arraylist.
    private boolean isMissHit = false;
    private boolean battleWon = false;

    private ThrowingItem throwingItem;
    private boolean itemIsBeingThrown = false;

    private Image blackScreen;

    private static AnimationState animationState = AnimationState.NONE;

    public BattleScreen(ElmourGame game) {
        super(game, false);

        _game = game;
        _mapMgr = new MapManager();
        mapType = MapFactory.MapType.GRASS_BATTLE;

        _json = new Json();

        setupViewport(V_WIDTH, V_HEIGHT);

        //get the current size
        _camera = new OrthographicCamera();
        _camera.setToOrtho(false, BattleScreen.VIEWPORT.viewportWidth, BattleScreen.VIEWPORT.viewportHeight);

        //_viewport = new ScreenViewport(_camera);
        _viewport = new FitViewport(ElmourGame.V_WIDTH, ElmourGame.V_HEIGHT, _camera);
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

            _hudCamera = new OrthographicCamera();
            _hudCamera.setToOrtho(false, BattleScreen.VIEWPORT.viewportWidth, BattleScreen.VIEWPORT.viewportHeight);

            battleHUD = new BattleHUD(game, _hudCamera, _mapMgr, this);

            _multiplexer = new InputMultiplexer();
            _multiplexer.addProcessor(battleControls.getStage());
            _multiplexer.addProcessor(battleHUD.getStage());
            _multiplexer.addProcessor(_stage);
            Gdx.input.setInputProcessor(_multiplexer);
        } else {
            _hudCamera = new OrthographicCamera();
            _hudCamera.setToOrtho(false, BattleScreen.VIEWPORT.viewportWidth, BattleScreen.VIEWPORT.viewportHeight);

            controllersCam = new OrthographicCamera();
            controllersCam.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);
            battleControls = new BattleControls(controllersCam);

            battleHUD = new BattleHUD(game, _hudCamera, _mapMgr, this);

            _multiplexer = new InputMultiplexer();
            _multiplexer.addProcessor(battleControls.getStage());
            _multiplexer.addProcessor(battleHUD.getStage());
            _multiplexer.addProcessor(_stage);
            Gdx.input.setInputProcessor(_multiplexer);
        }

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

        party1Shadow = new AnimatedImage();
        party2Shadow = new AnimatedImage();
        party3Shadow = new AnimatedImage();
        party4Shadow = new AnimatedImage();
        party5Shadow = new AnimatedImage();

        enemy1Shadow = new AnimatedImage();
        enemy2Shadow = new AnimatedImage();
        enemy3Shadow = new AnimatedImage();
        enemy4Shadow = new AnimatedImage();
        enemy5Shadow = new AnimatedImage();

        party1Shadow.setEntity(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.SHADOW1));
        party2Shadow.setEntity(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.SHADOW2));
        party3Shadow.setEntity(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.SHADOW3));
        party4Shadow.setEntity(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.SHADOW4));
        party5Shadow.setEntity(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.SHADOW5));
        enemy1Shadow.setEntity(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.SHADOW6));
        enemy2Shadow.setEntity(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.SHADOW7));
        enemy3Shadow.setEntity(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.SHADOW8));
        enemy4Shadow.setEntity(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.SHADOW9));
        enemy5Shadow.setEntity(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.SHADOW10));

        shadowMap = new Hashtable<>();
        shadowMap.put(party1, party1Shadow);
        shadowMap.put(party2, party2Shadow);
        shadowMap.put(party3, party3Shadow);
        shadowMap.put(party4, party4Shadow);
        shadowMap.put(party5, party5Shadow);
        shadowMap.put(enemy1, enemy1Shadow);
        shadowMap.put(enemy2, enemy2Shadow);
        shadowMap.put(enemy3, enemy3Shadow);
        shadowMap.put(enemy4, enemy4Shadow);
        shadowMap.put(enemy5, enemy5Shadow);

        characterShadowSortList= new ArrayList<>();
        characterShadowSortList.add(new AnimatedImageWithShadow(party1, party1Shadow));
        characterShadowSortList.add(new AnimatedImageWithShadow(party2, party2Shadow));
        characterShadowSortList.add(new AnimatedImageWithShadow(party3, party3Shadow));
        characterShadowSortList.add(new AnimatedImageWithShadow(party4, party4Shadow));
        characterShadowSortList.add(new AnimatedImageWithShadow(party5, party5Shadow));
        characterShadowSortList.add(new AnimatedImageWithShadow(enemy1, enemy1Shadow));
        characterShadowSortList.add(new AnimatedImageWithShadow(enemy2, enemy2Shadow));
        characterShadowSortList.add(new AnimatedImageWithShadow(enemy3, enemy3Shadow));
        characterShadowSortList.add(new AnimatedImageWithShadow(enemy4, enemy4Shadow));
        characterShadowSortList.add(new AnimatedImageWithShadow(enemy5, enemy5Shadow));

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
        steve2BattleAnimations = GraphicsComponent.loadAnimationsByName((EntityFactory.EntityName.STEVE2));
        steve3BattleAnimations = GraphicsComponent.loadAnimationsByName((EntityFactory.EntityName.STEVE3));

        battleHitAnimations = GraphicsComponent.loadAnimationsByName((EntityFactory.EntityName.HIT));
        weaponAnimations = GraphicsComponent.loadAnimationsByName((EntityFactory.EntityName.WEAPON_ANIMATIONS));

        selectedEntityIndicator = new Texture("graphics/down_arrow_red.png");
        currentTurnIndicator = new Texture("graphics/down_arrow_blue.png");

        blackScreen = new Image(new Texture("graphics/black_rectangle.png"));
        blackScreen.setWidth(_stage.getWidth());
        blackScreen.setHeight(_stage.getHeight());
        blackScreen.setPosition(0, 0);
        blackScreen.setVisible(true);

        _transitionActor = new ScreenTransitionActor();

        battleBursts = new CopyOnWriteArrayList<>();
        throwingItem = new ThrowingItem();

        _stage.addActor(_transitionActor);
        _stage.addActor(blackScreen);

        //Actions
        myActions = new MyActions();

        _switchScreenAction = new RunnableAction() {
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
                party1Shadow.addAction(Actions.fadeOut(0));
                party1Shadow.setTouchable(Touchable.disabled);
                party1Shadow.setVisible(true);
                party1Shadow.setSize(characterWidth, characterHeight);
                party1Shadow.setPosition(party1.getX(), party1.getY() - shadowYOffset);

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
                party2Shadow.addAction(Actions.fadeOut(0));
                party2Shadow.setTouchable(Touchable.disabled);
                party2Shadow.setVisible(true);
                party2Shadow.setSize(characterWidth, characterHeight);
                party2Shadow.setPosition(party2.getX(), party2.getY() - shadowYOffset);

                party3.setSize(characterWidth, characterHeight);
                party3.addAction(Actions.fadeOut(0));
                party3.setVisible(true);
                party3.setPosition(getStartPosition("P3").x, getStartPosition("P3").y);
                if (party3.getEntity() != null)
                    party3.getEntity().setCurrentPosition(new Vector2(party3.getX(), party3.getY()));
                party3StatArrows.setPosition(party3.getX() - 1, party3.getY());
                party3Shadow.addAction(Actions.fadeOut(0));
                party3Shadow.setTouchable(Touchable.disabled);
                party3Shadow.setVisible(true);
                party3Shadow.setSize(characterWidth, characterHeight);
                party3Shadow.setPosition(party3.getX(), party3.getY() - shadowYOffset);

                party4.setSize(characterWidth, characterHeight);
                party4.addAction(Actions.fadeOut(0));
                party4.setVisible(true);
                party4.setPosition(getStartPosition("P4").x, getStartPosition("P4").y);
                if (party4.getEntity() != null)
                    party4.getEntity().setCurrentPosition(new Vector2(party4.getX(), party4.getY()));
                party4StatArrows.setPosition(party4.getX() - 1, party4.getY());
                party4Shadow.addAction(Actions.fadeOut(0));
                party4Shadow.setTouchable(Touchable.disabled);
                party4Shadow.setVisible(true);
                party4Shadow.setSize(characterWidth, characterHeight);
                party4Shadow.setPosition(party4.getX(), party4.getY() - shadowYOffset);

                party5.setSize(characterWidth, characterHeight);
                party5.addAction(Actions.fadeOut(0));
                party5.setVisible(true);
                party5.setPosition(getStartPosition("P5").x, getStartPosition("P5").y);
                if (party5.getEntity() != null)
                    party5.getEntity().setCurrentPosition(new Vector2(party5.getX(), party5.getY()));
                party5StatArrows.setPosition(party5.getX() - 1, party5.getY());
                party5Shadow.addAction(Actions.fadeOut(0));
                party5Shadow.setTouchable(Touchable.disabled);
                party5Shadow.setVisible(true);
                party5Shadow.setSize(characterWidth, characterHeight);
                party5Shadow.setPosition(party5.getX(), party5.getY() - shadowYOffset);

                enemy1.setSize(characterWidth, characterHeight);
                enemy1.addAction(Actions.fadeOut(0));
                enemy1.setVisible(true);
                enemy1.setPosition(getStartPosition("E1").x, getStartPosition("E1").y);
                if (enemy1.getEntity() != null)
                    enemy1.getEntity().setCurrentPosition(new Vector2(enemy1.getX(), enemy1.getY()));
                enemy1StatArrows.setPosition(enemy1.getX() + 1, enemy1.getY());
                enemy1Shadow.addAction(Actions.fadeOut(0));
                enemy1Shadow.setTouchable(Touchable.disabled);
                enemy1Shadow.setVisible(true);
                enemy1Shadow.setSize(characterWidth, characterHeight);
                enemy1Shadow.setPosition(enemy1.getX(), enemy1.getY() - shadowYOffset);

                enemy2.setSize(characterWidth, characterHeight);
                enemy2.addAction(Actions.fadeOut(0));
                enemy2.setVisible(true);
                enemy2.setPosition(getStartPosition("E2").x, getStartPosition("E2").y);
                if (enemy2.getEntity() != null)
                    enemy2.getEntity().setCurrentPosition(new Vector2(enemy2.getX(), enemy2.getY()));
                enemy2StatArrows.setPosition(enemy2.getX() + 1, enemy2.getY());
                enemy2Shadow.addAction(Actions.fadeOut(0));
                enemy2Shadow.setTouchable(Touchable.disabled);
                enemy2Shadow.setVisible(true);
                enemy2Shadow.setSize(characterWidth, characterHeight);
                enemy2Shadow.setPosition(enemy2.getX(), enemy2.getY() - shadowYOffset);

                enemy3.setSize(characterWidth, characterHeight);
                enemy3.addAction(Actions.fadeOut(0));
                enemy3.setVisible(true);
                enemy3.setPosition(getStartPosition("E3").x, getStartPosition("E3").y);
                if (enemy3.getEntity() != null)
                    enemy3.getEntity().setCurrentPosition(new Vector2(enemy3.getX(), enemy3.getY()));
                enemy3StatArrows.setPosition(enemy3.getX() + 1, enemy3.getY());
                enemy3Shadow.addAction(Actions.fadeOut(0));
                enemy3Shadow.setTouchable(Touchable.disabled);
                enemy3Shadow.setVisible(true);
                enemy3Shadow.setSize(characterWidth, characterHeight);
                enemy3Shadow.setPosition(enemy3.getX(), enemy3.getY() - shadowYOffset);

                enemy4.setSize(characterWidth, characterHeight);
                enemy4.addAction(Actions.fadeOut(0));
                enemy4.setVisible(true);
                enemy4.setPosition(getStartPosition("E4").x, getStartPosition("E4").y);
                if (enemy4.getEntity() != null)
                    enemy4.getEntity().setCurrentPosition(new Vector2(enemy4.getX(), enemy4.getY()));
                enemy4StatArrows.setPosition(enemy4.getX() + 1, enemy4.getY());
                enemy4Shadow.addAction(Actions.fadeOut(0));
                enemy4Shadow.setTouchable(Touchable.disabled);
                enemy4Shadow.setVisible(true);
                enemy4Shadow.setSize(characterWidth, characterHeight);
                enemy4Shadow.setPosition(enemy4.getX(), enemy4.getY() - shadowYOffset);

                enemy5.setSize(characterWidth, characterHeight);
                enemy5.addAction(Actions.fadeOut(0));
                enemy5.setVisible(true);
                enemy5.setPosition(getStartPosition("E5").x, getStartPosition("E5").y);
                if (enemy5.getEntity() != null)
                    enemy5.getEntity().setCurrentPosition(new Vector2(enemy5.getX(), enemy5.getY()));
                enemy5StatArrows.setPosition(enemy5.getX() + 1, enemy5.getY());
                enemy5Shadow.addAction(Actions.fadeOut(0));
                enemy5Shadow.setTouchable(Touchable.disabled);
                enemy5Shadow.setVisible(true);
                enemy5Shadow.setSize(characterWidth, characterHeight);
                enemy5Shadow.setPosition(enemy5.getX(), enemy5.getY() - shadowYOffset);
            }
        };

        party1.addListener(new ClickListener() {
                               @Override
                               public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                   return true;
                               }

                               @Override
                               public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                   // make sure touch point is still on this image
                                   if (touchPointIsInImage(party1) && !currentTurnEntity.equals(party1.getEntity())) {
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
                               public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                   // make sure touch point is still on this image
                                   if (touchPointIsInImage(party2) && !currentTurnEntity.equals(party2.getEntity())) {
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
                               public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                   // make sure touch point is still on this image
                                   if (touchPointIsInImage(party3) && !currentTurnEntity.equals(party3.getEntity())) {
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
                               public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                   // make sure touch point is still on this image
                                   if (touchPointIsInImage(party4) && !currentTurnEntity.equals(party4.getEntity())) {
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
                               public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                   // make sure touch point is still on this image
                                   if (touchPointIsInImage(party5) && !currentTurnEntity.equals(party5.getEntity())) {
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
                               public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
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
                               public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
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
                               public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
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
                               public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
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
                               public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
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
        Animation<TextureRegion> characterWeaponAnimation;
        Animation<TextureRegion> defenderAnimation;
        Animation<TextureRegion> defenderWeaponAnimation;

        public setCurrentBattleAnimations(Animation<TextureRegion> characterAnimation,
                                          Animation<TextureRegion> characterWeaponAnimation,
                                          Animation<TextureRegion> defenderAnimation,
                                          Animation<TextureRegion> defenderWeaponAnimation) {
            this.characterAnimation = characterAnimation;
            this.characterWeaponAnimation = characterWeaponAnimation;
            this.defenderAnimation = defenderAnimation;
            this.defenderWeaponAnimation = defenderWeaponAnimation;
        }

        @Override
        public boolean act(float delta) {
            currentCharacterAnimation = this.characterAnimation;
            currentCharacterWeaponAnimation = this.characterWeaponAnimation;
            currentDefenderAnimation = this.defenderAnimation;
            currentDefenderWeaponAnimation = this.defenderWeaponAnimation;
            if (currentCharacterAnimation == null) {
                // reset for frame index
                _frameTime = 0;
            }
            return true;
        }
    }

    public class setCurrentHitAnimation extends Action {
        Animation<TextureRegion> hitAnimation;

        public setCurrentHitAnimation(Animation<TextureRegion> hitAnimation) {
            this.hitAnimation = hitAnimation;
        }

        @Override
        public boolean act(float delta) {
            currentHitAnimation = this.hitAnimation;
            if (currentHitAnimation == null) {
                hitFrameTime = 0;
            }
            return true;
        }
    }

    public class animationComplete extends Action {
        public animationComplete() {
        }

        @Override
        public boolean act(float delta) {
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
        public boolean act(float delta) {
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
        public boolean act(float delta) {
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
        public boolean act(float delta) {
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
        public boolean act(float delta) {
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
        public boolean act(float delta) {
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
        public boolean act(float delta) {
            showStatusArrows = show;
            return true; // An action returns true when it's completed
        }
    }

    private void completeAllActions() {
        float delta = 1;

        for (BattleBurst bb : battleBursts) {
            for (Image image : bb.imageArray) {
                image.addAction(Actions.fadeOut(0));
            }
        }

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

            if (party1Shadow != null) party1Shadow.act(delta);
            if (party2Shadow != null) party2Shadow.act(delta);
            if (party3Shadow != null) party3Shadow.act(delta);
            if (party4Shadow != null) party4Shadow.act(delta);
            if (party5Shadow != null) party5Shadow.act(delta);
            if (enemy1Shadow != null) enemy1Shadow.act(delta);
            if (enemy2Shadow != null) enemy2Shadow.act(delta);
            if (enemy3Shadow != null) enemy3Shadow.act(delta);
            if (enemy4Shadow != null) enemy4Shadow.act(delta);
            if (enemy5Shadow != null) enemy5Shadow.act(delta);
        }
    }

    public static AnimationState getAnimationState() {
        return animationState;
    }

    private Entity.AnimationType getWeaponIdAnimationType(Entity entity, boolean attackingEnemy) {
        String direction;
        String strWeaponID;

        if (entity.getBattleEntityType() == Entity.BattleEntityType.PARTY) {
            if (attackingEnemy)
                direction = "LEFT";
            else
                direction = "RIGHT";
        } else {
            if (attackingEnemy)
                direction = "RIGHT";
            else
                direction = "LEFT";
        }

        InventoryElement weapon = entity.getWeapon();
        if (weapon != null) {
            strWeaponID = entity.getWeapon().id.toString() + "_" + direction;
        }
        else {
            strWeaponID = InventoryElement.ElementID.KNUCKLES1 + "_" + direction;
        }

        return Entity.AnimationType.valueOf(strWeaponID);
    }

    private Entity.AnimationType getWeaponCategoryAnimationType(Entity entity, boolean attackingEnemy) {
        // if attackingEnemy is false, then the attack is against an ally
        String direction;
        String strAnimationType;

        if (entity.getBattleEntityType() == Entity.BattleEntityType.PARTY) {
            if (attackingEnemy)
                direction = "LEFT";
            else
                direction = "RIGHT";
        } else {
            if (attackingEnemy)
                direction = "RIGHT";
            else
                direction = "LEFT";
        }

        InventoryElement weapon = entity.getWeapon();
        if (weapon != null) {
            strAnimationType = entity.getWeapon().category.toString() + "_" + direction;
        }
        else {
            strAnimationType = InventoryElement.InventoryCategory.KNUCKLES + "_" + direction;
        }

        return Entity.AnimationType.valueOf(strAnimationType);
    }

    private Action getAttackOpponentAction(Entity entity) {
        animationState = AnimationState.BATTLE;
        Hashtable<Entity.AnimationType, Animation<TextureRegion>> currentCharacterBattleAnimation;
        Entity.AnimationType walkTowardsVictim;
        Entity.AnimationType walkAwayFromVictim;
        float destinationX;
        boolean isDagger = false;
        characterWeaponIdAnimationType = getWeaponIdAnimationType(entity, true);
        weaponCategoryAnimationType = getWeaponCategoryAnimationType(entity, true);

        InventoryElement weapon = entity.getWeapon();
        if (weapon != null) {
            if (weapon.id.toString().contains("DAGGER"))
            isDagger = true;
        }

        EntityFactory.EntityName entityName = EntityFactory.EntityName.valueOf(entity.getEntityConfig().getEntityID().toUpperCase());

        if (entity.getBattleEntityType() == Entity.BattleEntityType.PARTY) {
            if (selectedEntity.getBattleEntityType() == Entity.BattleEntityType.ENEMY) {
                walkTowardsVictim = Entity.AnimationType.WALK_LEFT;
                walkAwayFromVictim = Entity.AnimationType.WALK_RIGHT;
                destinationX = selectedEntity.getCurrentPosition().x + 1;
                if (isDagger)
                    destinationX += 0.5f;
            }
            else {
                return getAttackAllyAction(entity, isDagger);
            }
        } else {
            // entity == Entity.BattleEntityType.ENEMY
            if (selectedEntity.getBattleEntityType() == Entity.BattleEntityType.PARTY) {
                walkTowardsVictim = Entity.AnimationType.WALK_RIGHT;
                walkAwayFromVictim = Entity.AnimationType.WALK_LEFT;
                destinationX = selectedEntity.getCurrentPosition().x - 1;
                if (isDagger)
                    destinationX -= 0.5f;
            } else {
                return getAttackAllyAction(entity, isDagger);
            }
        }

        currentCharacterBattleAnimation = getBattleAnimations(entityName);

        return Actions.sequence(

                new showStatArrows(false),
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkTowardsVictim),
                Actions.parallel(Actions.addAction(Actions.moveTo(destinationX, selectedEntity.getCurrentPosition().y, 0.75f, Interpolation.linear), currentTurnCharacter),
                                 Actions.addAction(Actions.moveTo(destinationX, selectedEntity.getCurrentPosition().y - shadowYOffset, 0.75f, Interpolation.linear), shadowMap.get(currentTurnCharacter))),

                Actions.delay(0.75f),
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), Entity.AnimationType.IDLE),
                Actions.delay(0.25f),
                new setCurrentBattleAnimations(currentCharacterBattleAnimation.get(weaponCategoryAnimationType),
                        weaponAnimations.get(characterWeaponIdAnimationType),null, null),

                new showMainCharacterAnimation(currentTurnCharacter, false),
                // Framerate * # of Frames
                Actions.delay(0.2f),
                new setCurrentHitAnimation(battleHitAnimations.get(weaponCategoryAnimationType)),
                Actions.delay(0.15f),
                new setCurrentHitAnimation(null),

                new setCurrentBattleAnimations(null, null, null, null),
                new showMainCharacterAnimation(currentTurnCharacter, true),

                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkAwayFromVictim),
                Actions.parallel(Actions.addAction(Actions.moveTo(currentTurnCharacter.getX(), currentTurnCharacter.getY(), 0.75f, Interpolation.linear), currentTurnCharacter),
                                 Actions.addAction(Actions.moveTo(currentTurnCharacter.getX(), currentTurnCharacter.getY() - shadowYOffset, 0.75f, Interpolation.linear), shadowMap.get(currentTurnCharacter))),
                Actions.delay(0.75f),

                // turn to face victim
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkTowardsVictim),
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), Entity.AnimationType.IDLE),

                new animationComplete(),
                new showStatArrows(true)
        );
    }

    private Action getAttackAllyAction(Entity entity, boolean isDagger) {
        animationState = AnimationState.BATTLE;
        Hashtable<Entity.AnimationType, Animation<TextureRegion>> currentCharacterBattleAnimation;
        Entity.AnimationType walkOut;
        Entity.AnimationType walkBackIntoPlace;
        Entity.AnimationType walkDirectionToAttack;
        Entity.AnimationType walkDirectionFromAttack;
        Entity.AnimationType walkTowardsVictim;
        float walkOutDestinationX;
        float attackDestinationX;
        characterWeaponIdAnimationType = getWeaponIdAnimationType(entity, false);
        weaponCategoryAnimationType = getWeaponCategoryAnimationType(entity, false);

        EntityFactory.EntityName entityName = EntityFactory.EntityName.valueOf(entity.getEntityConfig().getEntityID().toUpperCase());

        if (entity.getBattleEntityType() == Entity.BattleEntityType.PARTY) {
            walkOut = Entity.AnimationType.WALK_LEFT;
            walkTowardsVictim = Entity.AnimationType.WALK_RIGHT;
            attackDestinationX = selectedEntity.getCurrentPosition().x - 1;

            if ((entity.getBattlePosition() == 1 || entity.getBattlePosition() == 3 || entity.getBattlePosition() == 5) &&
                    (selectedEntity.getBattlePosition() == 1 || selectedEntity.getBattlePosition() == 3 || selectedEntity.getBattlePosition() == 5)) {
                walkOutDestinationX = selectedEntity.getCurrentPosition().x - 2;
            } else if ((entity.getBattlePosition() == 2 || entity.getBattlePosition() == 4) &&
                    (Math.abs(entity.getBattlePosition() - selectedEntity.getBattlePosition()) > 2)) {
                walkOutDestinationX = selectedEntity.getCurrentPosition().x - 2;
            } else {
                walkOutDestinationX = selectedEntity.getCurrentPosition().x - 1.5f;
            }

            if (isDagger) {
                walkOutDestinationX -= 1;
                attackDestinationX -= 0.5f;
            }
        } else {
            walkOut = Entity.AnimationType.WALK_RIGHT;
            walkTowardsVictim = Entity.AnimationType.WALK_LEFT;
            attackDestinationX = selectedEntity.getCurrentPosition().x + 1;

            if ((entity.getBattlePosition() == 1 || entity.getBattlePosition() == 3 || entity.getBattlePosition() == 5) &&
                    (selectedEntity.getBattlePosition() == 1 || selectedEntity.getBattlePosition() == 3 || selectedEntity.getBattlePosition() == 5)) {
                walkOutDestinationX = selectedEntity.getCurrentPosition().x + 2;
            } else if ((entity.getBattlePosition() == 2 || entity.getBattlePosition() == 4) &&
                    (Math.abs(entity.getBattlePosition() - selectedEntity.getBattlePosition()) > 2)) {
                walkOutDestinationX = selectedEntity.getCurrentPosition().x + 2;
            } else {
                walkOutDestinationX = selectedEntity.getCurrentPosition().x + 1.5f;
            }

            if (isDagger) {
                walkOutDestinationX += 1;
                attackDestinationX += 0.5f;
            }
        }

        float walkInOutTime;
        float walkToVictimAndBackTime;
        float walkUpDownTime;

        if (entity.getBattlePosition() == 1 || entity.getBattlePosition() == 3 || entity.getBattlePosition() == 5) {
            walkInOutTime = 0.4f;
        }
        else {
            walkInOutTime = 0.25f;
        }

        if (selectedEntity.getBattlePosition() == 1 || selectedEntity.getBattlePosition() == 3 || selectedEntity.getBattlePosition() == 5) {
            walkToVictimAndBackTime = 0.4f;
        }
        else {
            walkToVictimAndBackTime = 0.25f;
        }

        int positionDiff = Math.abs(entity.getBattlePosition() - selectedEntity.getBattlePosition());
        switch (positionDiff) {
            case 1:
                walkUpDownTime = 0.4f;
                break;
            case 2:
                walkUpDownTime = 0.53f;
                break;
            case 3:
                walkUpDownTime = 0.64f;
                break;
            case 4:
                walkUpDownTime = 0.8f;
                break;
            default:
                walkUpDownTime = 0.4f;
        }

        walkBackIntoPlace = walkTowardsVictim;

        if (entity.getBattlePosition() < selectedEntity.getBattlePosition()) {
            walkDirectionToAttack = Entity.AnimationType.WALK_DOWN;
            walkDirectionFromAttack = Entity.AnimationType.WALK_UP;
        } else {
            walkDirectionToAttack = Entity.AnimationType.WALK_UP;
            walkDirectionFromAttack = Entity.AnimationType.WALK_DOWN;
        }

        currentCharacterBattleAnimation = getBattleAnimations(entityName);

        // if need to do something for the frame that z-order is a little off
        //setDefendingCharacter(selectedEntity);
        //int index = defendingCharacter.getZIndex();
        //currentTurnCharacter.setZIndex(defendingCharacter.getZIndex());

        return Actions.sequence(

                new showStatArrows(false),
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkOut),
                Actions.parallel(Actions.addAction(Actions.moveTo(walkOutDestinationX, entity.getCurrentPosition().y, walkInOutTime, Interpolation.linear), currentTurnCharacter),
                                 Actions.addAction(Actions.moveTo(walkOutDestinationX, entity.getCurrentPosition().y- shadowYOffset, walkInOutTime, Interpolation.linear), shadowMap.get(currentTurnCharacter))),

                Actions.delay(walkInOutTime),

                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkDirectionToAttack),
                Actions.parallel(Actions.addAction(Actions.moveTo(walkOutDestinationX, selectedEntity.getCurrentPosition().y, walkUpDownTime, Interpolation.linear), currentTurnCharacter),
                                 Actions.addAction(Actions.moveTo(walkOutDestinationX, selectedEntity.getCurrentPosition().y - shadowYOffset, walkUpDownTime, Interpolation.linear), shadowMap.get(currentTurnCharacter))),

                Actions.delay(walkUpDownTime),

                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkTowardsVictim),
                Actions.parallel(Actions.addAction(Actions.moveTo(attackDestinationX, selectedEntity.getCurrentPosition().y, walkToVictimAndBackTime, Interpolation.linear), currentTurnCharacter),
                                 Actions.addAction(Actions.moveTo(attackDestinationX, selectedEntity.getCurrentPosition().y - shadowYOffset, walkToVictimAndBackTime, Interpolation.linear), shadowMap.get(currentTurnCharacter))),

                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), Entity.AnimationType.IMMOBILE),
                Actions.delay(walkToVictimAndBackTime),
                new setCurrentBattleAnimations(currentCharacterBattleAnimation.get(weaponCategoryAnimationType),
                        weaponAnimations.get(characterWeaponIdAnimationType),null, null),

                new showMainCharacterAnimation(currentTurnCharacter, false),
                // Framerate * # of Frames
                Actions.delay(0.2f),
                new setCurrentHitAnimation(battleHitAnimations.get(weaponCategoryAnimationType)),
                Actions.delay(0.3f),
                new setCurrentHitAnimation(null),

                new setCurrentBattleAnimations(null, null, null, null),
                new showMainCharacterAnimation(currentTurnCharacter, true),

                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkOut),
                Actions.parallel(Actions.addAction(Actions.moveTo(walkOutDestinationX, selectedEntity.getCurrentPosition().y, walkToVictimAndBackTime, Interpolation.linear), currentTurnCharacter),
                                 Actions.addAction(Actions.moveTo(walkOutDestinationX, selectedEntity.getCurrentPosition().y - shadowYOffset, walkToVictimAndBackTime, Interpolation.linear), shadowMap.get(currentTurnCharacter))),
                Actions.delay(walkToVictimAndBackTime),

                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkDirectionFromAttack),
                Actions.parallel(Actions.addAction(Actions.moveTo(walkOutDestinationX, currentTurnCharacter.getY(), walkUpDownTime, Interpolation.linear), currentTurnCharacter),
                                 Actions.addAction(Actions.moveTo(walkOutDestinationX, currentTurnCharacter.getY() - shadowYOffset, walkUpDownTime, Interpolation.linear), shadowMap.get(currentTurnCharacter))),
                Actions.delay(walkUpDownTime),

                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkBackIntoPlace),
                Actions.parallel(Actions.addAction(Actions.moveTo(currentTurnCharacter.getX(), currentTurnCharacter.getY(), walkInOutTime, Interpolation.linear), currentTurnCharacter),
                                 Actions.addAction(Actions.moveTo(currentTurnCharacter.getX(), currentTurnCharacter.getY() - shadowYOffset, walkInOutTime, Interpolation.linear), shadowMap.get(currentTurnCharacter))),
                Actions.delay(walkInOutTime),

                // turn to face victim
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkTowardsVictim),
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), Entity.AnimationType.IDLE),

                new animationComplete(),
                new showStatArrows(true)
        );
    }

    private Action getThrowAction(Entity entity, boolean isMissHit) {
        animationState = AnimationState.BATTLE;
        Hashtable<Entity.AnimationType, Animation<TextureRegion>> currentCharacterBattleAnimation;
        Entity.AnimationType walkOut;
        Entity.AnimationType walkBack;
        Entity.AnimationType faceVictim;
        float destinationX;
        float walkOutDistance = 1.5f;
        Entity.AnimationType throwAnimationType;

        EntityFactory.EntityName entityName = EntityFactory.EntityName.valueOf(entity.getEntityConfig().getEntityID().toUpperCase());

        characterWeaponIdAnimationType = null;

        // only party members can throw
        if (selectedEntity.getBattleEntityType() == Entity.BattleEntityType.ENEMY) {
            throwAnimationType = Entity.AnimationType.THROW1_LEFT;
        }
        else {
            throwAnimationType = Entity.AnimationType.THROW1_RIGHT;
        }

        if (entity.getBattleEntityType() == Entity.BattleEntityType.PARTY) {
            walkOut = Entity.AnimationType.WALK_LEFT;
            walkBack = Entity.AnimationType.WALK_RIGHT;
            destinationX = entity.getCurrentPosition().x - walkOutDistance;

            if (selectedEntity.getBattleEntityType() == Entity.BattleEntityType.ENEMY) {
                weaponCategoryAnimationType = Entity.AnimationType.BLUNT_LEFT;
                faceVictim = Entity.AnimationType.WALK_LEFT;
            } else {
                weaponCategoryAnimationType = Entity.AnimationType.BLUNT_RIGHT;
                faceVictim = Entity.AnimationType.WALK_RIGHT;
            }
        } else {
            // entity == Entity.BattleEntityType.ENEMY
            walkOut = Entity.AnimationType.WALK_RIGHT;
            walkBack = Entity.AnimationType.WALK_LEFT;
            destinationX = entity.getCurrentPosition().x + walkOutDistance;
            if (selectedEntity.getBattleEntityType() == Entity.BattleEntityType.PARTY) {
                weaponCategoryAnimationType = Entity.AnimationType.BLUNT_RIGHT;
                faceVictim = Entity.AnimationType.WALK_RIGHT;
            } else {
                weaponCategoryAnimationType = Entity.AnimationType.BLUNT_LEFT;
                faceVictim = Entity.AnimationType.WALK_LEFT;
            }
        }

        currentCharacterBattleAnimation = getBattleAnimations(entityName);

        return Actions.sequence(
                new showStatArrows(false),
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkOut),
                Actions.parallel(Actions.addAction(Actions.moveTo(destinationX, entity.getCurrentPosition().y, 0.25f, Interpolation.linear), currentTurnCharacter),
                                 Actions.addAction(Actions.moveTo(destinationX, entity.getCurrentPosition().y - shadowYOffset, 0.25f, Interpolation.linear), shadowMap.get(currentTurnCharacter))),

                Actions.delay(0.25f),

                // turn to face victim
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), faceVictim),
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), Entity.AnimationType.IDLE),
                Actions.delay(0.25f),
                new setCurrentBattleAnimations(currentCharacterBattleAnimation.get(weaponCategoryAnimationType),
                        null, null, null),

                new throwItem(throwAnimationType, isMissHit),

                new showMainCharacterAnimation(currentTurnCharacter, false),
                // Framerate * # of Frames
                Actions.delay(0.5f),
                new setCurrentHitAnimation(null),
                new showMainCharacterAnimation(currentTurnCharacter, true),
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), Entity.AnimationType.IDLE),

                new setCurrentBattleAnimations(null, null, null, null),

                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkBack),
                Actions.parallel(Actions.addAction(Actions.moveTo(currentTurnCharacter.getX(), currentTurnCharacter.getY(), 0.4f, Interpolation.linear), currentTurnCharacter),
                                 Actions.addAction(Actions.moveTo(currentTurnCharacter.getX(), currentTurnCharacter.getY() - shadowYOffset, 0.4f, Interpolation.linear), shadowMap.get(currentTurnCharacter))),

                Actions.delay(0.4f),

                // turn to face back out
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkOut),
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), Entity.AnimationType.IDLE),

                new animationComplete(),
                new showStatArrows(true)
        );
    }

    private Action getApplyInventoryAction(Entity entity) {
        animationState = AnimationState.BATTLE;
        Hashtable<Entity.AnimationType, Animation<TextureRegion>> currentCharacterBattleAnimation;
        float destinationX;
        float walkOutDistance = 1.5f;

        // only party members can apply inventory
        //todo: animation

        EntityFactory.EntityName entityName = EntityFactory.EntityName.valueOf(entity.getEntityConfig().getEntityID().toUpperCase());

        characterWeaponIdAnimationType = null;

        destinationX = entity.getCurrentPosition().x - walkOutDistance;

        weaponCategoryAnimationType = Entity.AnimationType.BLUNT_RIGHT;

        currentCharacterBattleAnimation = getBattleAnimations(entityName);

        return Actions.sequence(

                new showStatArrows(false),
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), Entity.AnimationType.WALK_LEFT),
                Actions.parallel(Actions.addAction(Actions.moveTo(destinationX, entity.getCurrentPosition().y, 0.25f, Interpolation.linear), currentTurnCharacter),
                                 Actions.addAction(Actions.moveTo(destinationX, entity.getCurrentPosition().y - shadowYOffset, 0.25f, Interpolation.linear), shadowMap.get(currentTurnCharacter))),
                Actions.delay(0.25f),

                // turn to face victim
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), Entity.AnimationType.WALK_RIGHT),
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), Entity.AnimationType.IDLE),
                Actions.delay(0.25f),
                new setCurrentBattleAnimations(currentCharacterBattleAnimation.get(weaponCategoryAnimationType),
                        null, null, null),

                //new throwItem(throwAnimationType, isMissHit),

                new showMainCharacterAnimation(currentTurnCharacter, false),
                // Framerate * # of Frames
                Actions.delay(0.5f),
                new setCurrentHitAnimation(null),
                new showMainCharacterAnimation(currentTurnCharacter, true),
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), Entity.AnimationType.IDLE),

                new setCurrentBattleAnimations(null, null, null, null),

                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), Entity.AnimationType.WALK_RIGHT),
                 Actions.parallel(Actions.addAction(Actions.moveTo(currentTurnCharacter.getX(), currentTurnCharacter.getY(), 0.4f, Interpolation.linear), currentTurnCharacter),
                                 Actions.addAction(Actions.moveTo(currentTurnCharacter.getX(), currentTurnCharacter.getY() - shadowYOffset, 0.4f, Interpolation.linear), shadowMap.get(currentTurnCharacter))),
                Actions.delay(0.4f),

                // turn to face back out
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), Entity.AnimationType.WALK_LEFT),
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), Entity.AnimationType.IDLE),

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
        Entity.AnimationType blockType;
        float destinationX;

        characterWeaponIdAnimationType = getWeaponIdAnimationType(attacker, true);
        defenderWeaponIdAnimationType = getWeaponIdAnimationType(defender, true);
        weaponCategoryAnimationType = getWeaponCategoryAnimationType(attacker, true);
        defenderWeaponAnimationType = getWeaponCategoryAnimationType(defender, true);

        EntityFactory.EntityName attackerEntityName = EntityFactory.EntityName.valueOf(attacker.getEntityConfig().getEntityID().toUpperCase());
        EntityFactory.EntityName defenderEntityName = EntityFactory.EntityName.valueOf(defender.getEntityConfig().getEntityID().toUpperCase());

        if (attacker.getBattleEntityType() == Entity.BattleEntityType.PARTY) {
            walkTowardsVictim = Entity.AnimationType.WALK_LEFT;
            walkAwayFromVictim = Entity.AnimationType.WALK_RIGHT;
            destinationX = defender.getCurrentPosition().x + 1;
        } else {
            // Entity.BattleEntityType.ENEMY
            walkTowardsVictim = Entity.AnimationType.WALK_RIGHT;
            walkAwayFromVictim = Entity.AnimationType.WALK_LEFT;
            destinationX = defender.getCurrentPosition().x - 1;
        }

        if (defender.getBattleEntityType() == Entity.BattleEntityType.PARTY) {
            blockType = Entity.AnimationType.BLOCK_RIGHT;
        }
        else
        {
            blockType = Entity.AnimationType.BLOCK_LEFT;
        }

        if (defender.getCurrentPosition().y < selectedEntity.getCurrentPosition().y) {
            walkDirectionToBlock = Entity.AnimationType.WALK_UP;
            walkDirectionFromBlock = Entity.AnimationType.WALK_DOWN;
        } else {
            walkDirectionToBlock = Entity.AnimationType.WALK_DOWN;
            walkDirectionFromBlock = Entity.AnimationType.WALK_UP;
        }

        currentAttackerBattleAnimation = getBattleAnimations(attackerEntityName);
        currentDefenderBattleAnimation = getBattleAnimations(defenderEntityName);

        return Actions.sequence(
                new showStatArrows(false),
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkTowardsVictim),
                Actions.parallel(Actions.addAction(Actions.moveTo(destinationX, selectedEntity.getCurrentPosition().y, 0.75f, Interpolation.linear), currentTurnCharacter),
                                 Actions.addAction(Actions.moveTo(destinationX, selectedEntity.getCurrentPosition().y - shadowYOffset, 0.75f, Interpolation.linear), shadowMap.get(currentTurnCharacter))),

                Actions.delay(0.50f),

                // defender walk to block
                myActions.new setWalkDirectionWithShadow(defendingCharacter, shadowMap.get(defendingCharacter), walkDirectionToBlock),
                Actions.parallel(Actions.addAction(Actions.moveTo(defender.getCurrentPosition().x, selectedEntity.getCurrentPosition().y, 0.25f, Interpolation.linear), defendingCharacter),
                                 Actions.addAction(Actions.moveTo(defender.getCurrentPosition().x, selectedEntity.getCurrentPosition().y - shadowYOffset, 0.25f, Interpolation.linear), shadowMap.get(defendingCharacter))),

                Actions.delay(0.25f),

                // defender turn to face attacker
                myActions.new setWalkDirectionWithShadow(defendingCharacter, shadowMap.get(defendingCharacter), walkAwayFromVictim),
                myActions.new setWalkDirectionWithShadow(defendingCharacter, shadowMap.get(defendingCharacter), Entity.AnimationType.IDLE),

                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), Entity.AnimationType.IDLE),
                Actions.delay(0.25f),
                new setCurrentBattleAnimations(currentAttackerBattleAnimation.get(weaponCategoryAnimationType),
                        weaponAnimations.get(characterWeaponIdAnimationType),
                        currentDefenderBattleAnimation.get(defenderWeaponAnimationType),
                        weaponAnimations.get(defenderWeaponIdAnimationType)),

                new showMainCharacterAnimation(currentTurnCharacter, false),
                // Framerate * # of Frames
                Actions.delay(0.2f),
                new setCurrentHitAnimation(battleHitAnimations.get(blockType)),
                Actions.delay(0.3f),
                new setCurrentHitAnimation(null),

                new setCurrentBattleAnimations(null, null, null, null),
                new showMainCharacterAnimation(currentTurnCharacter, true),

                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkAwayFromVictim),
                Actions.parallel(Actions.addAction(Actions.moveTo(currentTurnCharacter.getX(), currentTurnCharacter.getY(), 0.75f, Interpolation.linear), currentTurnCharacter),
                                 Actions.addAction(Actions.moveTo(currentTurnCharacter.getX(), currentTurnCharacter.getY() - shadowYOffset, 0.75f, Interpolation.linear), shadowMap.get(currentTurnCharacter))),

                // defender walk back into place
                myActions.new setWalkDirectionWithShadow(defendingCharacter, shadowMap.get(defendingCharacter), walkDirectionFromBlock),
                Actions.parallel(Actions.addAction(Actions.moveTo(defender.getCurrentPosition().x, defender.getCurrentPosition().y, 0.75f, Interpolation.linear), defendingCharacter),
                                 Actions.addAction(Actions.moveTo(defender.getCurrentPosition().x, defender.getCurrentPosition().y - shadowYOffset, 0.75f, Interpolation.linear), shadowMap.get(defendingCharacter))),

                Actions.delay(0.75f),

                // defender turn to face attacker
                myActions.new setWalkDirectionWithShadow(defendingCharacter, shadowMap.get(defendingCharacter), walkAwayFromVictim),
                myActions.new setWalkDirectionWithShadow(defendingCharacter, shadowMap.get(defendingCharacter), Entity.AnimationType.IDLE),

                // attacker turn to face victim
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkTowardsVictim),
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), Entity.AnimationType.IDLE),

                new animationComplete(),
                new showStatArrows(true)
        );
    }

    private Action getMissedAttackAction(Entity attacker, Entity defender) {
        animationState = AnimationState.BATTLE;
        Hashtable<Entity.AnimationType, Animation<TextureRegion>> currentAttackerBattleAnimation;
        Entity.AnimationType walkTowardsVictim;
        Entity.AnimationType walkAwayFromVictim;
        Entity.AnimationType walkDirectionDefender;
        float destinationX;
        int evasionOffset = 1;
        boolean isDagger = false;

        characterWeaponIdAnimationType = getWeaponIdAnimationType(attacker, true);
        weaponCategoryAnimationType = getWeaponCategoryAnimationType(attacker, true);

        InventoryElement weapon = attacker.getWeapon();
        if (weapon != null) {
            if (weapon.id.toString().contains("DAGGER"))
                isDagger = true;
        }

        EntityFactory.EntityName attackerEntityName = EntityFactory.EntityName.valueOf(attacker.getEntityConfig().getEntityID().toUpperCase());
        EntityFactory.EntityName defenderEntityName = EntityFactory.EntityName.valueOf(defender.getEntityConfig().getEntityID().toUpperCase());

        if (attacker.getBattleEntityType() == Entity.BattleEntityType.PARTY) {
            if (defender.getBattleEntityType() == Entity.BattleEntityType.ENEMY) {
                walkTowardsVictim = Entity.AnimationType.WALK_LEFT;
                walkAwayFromVictim = Entity.AnimationType.WALK_RIGHT;
                if (isDagger)
                    destinationX = defender.getCurrentPosition().x + 1.5f;
                else
                    destinationX = defender.getCurrentPosition().x + 1;
                evasionOffset = -1;
            } else {
                evasionOffset = 1;
                walkAwayFromVictim = Entity.AnimationType.WALK_LEFT;
                return getMissedAttackAllyAction(attacker, defender, evasionOffset, walkAwayFromVictim, isDagger);
            }
        } else {
            if (defender.getBattleEntityType() == Entity.BattleEntityType.PARTY) {
                // Entity.BattleEntityType.ENEMY
                walkTowardsVictim = Entity.AnimationType.WALK_RIGHT;
                walkAwayFromVictim = Entity.AnimationType.WALK_LEFT;
                if (isDagger)
                    destinationX = defender.getCurrentPosition().x - 1.5f;
                else
                    destinationX = defender.getCurrentPosition().x - 1;
                evasionOffset = 1;
            } else {
                evasionOffset = -1;
                walkAwayFromVictim = Entity.AnimationType.WALK_RIGHT;
                return getMissedAttackAllyAction(attacker, defender, evasionOffset, walkAwayFromVictim, isDagger);
            }
        }

        walkDirectionDefender = walkAwayFromVictim;

        currentAttackerBattleAnimation = getBattleAnimations(attackerEntityName);

        return Actions.sequence(
                new showStatArrows(false),

                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkTowardsVictim),
                Actions.parallel(Actions.addAction(Actions.moveTo(destinationX, defender.getCurrentPosition().y, 0.75f, Interpolation.linear), currentTurnCharacter),
                                 Actions.addAction(Actions.moveTo(destinationX, defender.getCurrentPosition().y- shadowYOffset, 0.75f, Interpolation.linear), shadowMap.get(currentTurnCharacter))),

                Actions.delay(0.65f),

                // defender turn to face attacker
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkAwayFromVictim),
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), Entity.AnimationType.IDLE),

                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), Entity.AnimationType.IDLE),
                Actions.delay(0.25f),

                new setCurrentBattleAnimations(currentAttackerBattleAnimation.get(weaponCategoryAnimationType),
                        weaponAnimations.get(characterWeaponIdAnimationType),null, null),
                new setCurrentHitAnimation(battleHitAnimations.get(weaponCategoryAnimationType)),

                new showMainCharacterAnimation(currentTurnCharacter, false),

                // defender walk back to miss attack
                myActions.new setWalkDirectionWithShadow(defendingCharacter, shadowMap.get(defendingCharacter), walkDirectionDefender),
                Actions.parallel(Actions.addAction(Actions.moveTo(defender.getCurrentPosition().x + evasionOffset, defender.getCurrentPosition().y, 0.25f, Interpolation.linear), defendingCharacter),
                                 Actions.addAction(Actions.moveTo(defender.getCurrentPosition().x + evasionOffset, defender.getCurrentPosition().y - shadowYOffset, 0.25f, Interpolation.linear), shadowMap.get(defendingCharacter))),

                // Framerate * # of Frames
                Actions.delay(0.5f),
                new setCurrentBattleAnimations(null, null, null, null),
                new showMainCharacterAnimation(currentTurnCharacter, true),

                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkAwayFromVictim),
                Actions.parallel(Actions.addAction(Actions.moveTo(currentTurnCharacter.getX(), currentTurnCharacter.getY(), 0.75f, Interpolation.linear), currentTurnCharacter),
                                 Actions.addAction(Actions.moveTo(currentTurnCharacter.getX(), currentTurnCharacter.getY() - shadowYOffset, 0.75f, Interpolation.linear), shadowMap.get(currentTurnCharacter))),

                // defender walk back into place
                myActions.new setWalkDirectionWithShadow(defendingCharacter, shadowMap.get(defendingCharacter), walkDirectionDefender),
                Actions.parallel(Actions.addAction(Actions.moveTo(defender.getCurrentPosition().x, defender.getCurrentPosition().y, 0.75f, Interpolation.linear), defendingCharacter),
                                 Actions.addAction(Actions.moveTo(defender.getCurrentPosition().x, defender.getCurrentPosition().y - shadowYOffset, 0.75f, Interpolation.linear), shadowMap.get(defendingCharacter))),

                Actions.delay(0.75f),

                // defender turn to face attacker
                myActions.new setWalkDirectionWithShadow(defendingCharacter, shadowMap.get(defendingCharacter), walkAwayFromVictim),
                myActions.new setWalkDirectionWithShadow(defendingCharacter, shadowMap.get(defendingCharacter), Entity.AnimationType.IDLE),

                // attacker turn to face victim
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkTowardsVictim),
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), Entity.AnimationType.IDLE),

                new animationComplete(),
                new showStatArrows(true)
        );
    }

    private Action getMissedAttackAllyAction(Entity attacker, Entity defender, int evasionOffset, Entity.AnimationType walkDirectionDefender, boolean isDagger) {
        animationState = AnimationState.BATTLE;
        Hashtable<Entity.AnimationType, Animation<TextureRegion>> currentCharacterBattleAnimation;
        Entity.AnimationType walkOut;
        Entity.AnimationType walkBackIntoPlace;
        Entity.AnimationType walkDirectionToAttack;
        Entity.AnimationType walkDirectionFromAttack;
        Entity.AnimationType walkTowardsVictim;
        float walkOutDestinationX;
        float attackDestinationX;

        selectedEntity = defender;

        characterWeaponIdAnimationType = getWeaponIdAnimationType(attacker, false);
        weaponCategoryAnimationType = getWeaponCategoryAnimationType(attacker, false);

        EntityFactory.EntityName entityName = EntityFactory.EntityName.valueOf(attacker.getEntityConfig().getEntityID().toUpperCase());

        if (attacker.getBattleEntityType() == Entity.BattleEntityType.PARTY) {
            walkOut = Entity.AnimationType.WALK_LEFT;
            walkTowardsVictim = Entity.AnimationType.WALK_RIGHT;
            attackDestinationX = selectedEntity.getCurrentPosition().x - 1;

            if ((attacker.getBattlePosition() == 1 || attacker.getBattlePosition() == 3 || attacker.getBattlePosition() == 5) &&
                    (selectedEntity.getBattlePosition() == 1 || selectedEntity.getBattlePosition() == 3 || selectedEntity.getBattlePosition() == 5)) {
                walkOutDestinationX = selectedEntity.getCurrentPosition().x - 2;
            } else if ((attacker.getBattlePosition() == 2 || attacker.getBattlePosition() == 4) &&
                    (Math.abs(attacker.getBattlePosition() - selectedEntity.getBattlePosition()) > 2)) {
                walkOutDestinationX = selectedEntity.getCurrentPosition().x - 2;
            } else {
                walkOutDestinationX = selectedEntity.getCurrentPosition().x - 1;
            }

            if (isDagger) {
                walkOutDestinationX -= 1;
                attackDestinationX -= 0.5f;
            }
        } else {
            walkOut = Entity.AnimationType.WALK_RIGHT;
            walkTowardsVictim = Entity.AnimationType.WALK_LEFT;
            attackDestinationX = selectedEntity.getCurrentPosition().x + 1;

            if ((attacker.getBattlePosition() == 1 || attacker.getBattlePosition() == 3 || attacker.getBattlePosition() == 5) &&
                    (selectedEntity.getBattlePosition() == 1 || selectedEntity.getBattlePosition() == 3 || selectedEntity.getBattlePosition() == 5)) {
                walkOutDestinationX = selectedEntity.getCurrentPosition().x + 2;
            } else if ((attacker.getBattlePosition() == 2 || attacker.getBattlePosition() == 4) &&
                    (Math.abs(attacker.getBattlePosition() - selectedEntity.getBattlePosition()) > 2)) {
                walkOutDestinationX = selectedEntity.getCurrentPosition().x + 2;
            } else {
                walkOutDestinationX = selectedEntity.getCurrentPosition().x + 1;
            }

            if (isDagger) {
                walkOutDestinationX += 1;
                attackDestinationX += 0.5f;
            }
        }

        walkBackIntoPlace = walkTowardsVictim;

        if (attacker.getBattlePosition() < selectedEntity.getBattlePosition()) {
            walkDirectionToAttack = Entity.AnimationType.WALK_DOWN;
            walkDirectionFromAttack = Entity.AnimationType.WALK_UP;
        } else {
            walkDirectionToAttack = Entity.AnimationType.WALK_UP;
            walkDirectionFromAttack = Entity.AnimationType.WALK_DOWN;
        }

        currentCharacterBattleAnimation = getBattleAnimations(entityName);

        return Actions.sequence(

                new showStatArrows(false),
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkOut),
                Actions.parallel(Actions.addAction(Actions.moveTo(walkOutDestinationX, attacker.getCurrentPosition().y, 0.25f, Interpolation.linear), currentTurnCharacter),
                                 Actions.addAction(Actions.moveTo(walkOutDestinationX, attacker.getCurrentPosition().y - shadowYOffset, 0.25f, Interpolation.linear), shadowMap.get(currentTurnCharacter))),
                Actions.delay(0.25f),

                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkDirectionToAttack),
                Actions.parallel(Actions.addAction(Actions.moveTo(walkOutDestinationX, selectedEntity.getCurrentPosition().y, 0.25f, Interpolation.linear), currentTurnCharacter),
                                 Actions.addAction(Actions.moveTo(walkOutDestinationX, selectedEntity.getCurrentPosition().y- shadowYOffset, 0.25f, Interpolation.linear), shadowMap.get(currentTurnCharacter))),
                Actions.delay(0.25f),

                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkTowardsVictim),
                Actions.parallel(Actions.addAction(Actions.moveTo(attackDestinationX, selectedEntity.getCurrentPosition().y, 0.25f, Interpolation.linear), currentTurnCharacter),
                                 Actions.addAction(Actions.moveTo(attackDestinationX, selectedEntity.getCurrentPosition().y- shadowYOffset, 0.25f, Interpolation.linear), shadowMap.get(currentTurnCharacter))),

                myActions.new setIdleDirection(currentTurnCharacter, Entity.Direction.RIGHT),
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), Entity.AnimationType.IDLE),

                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), Entity.AnimationType.IMMOBILE),
                Actions.delay(0.25f),
                new setCurrentBattleAnimations(currentCharacterBattleAnimation.get(weaponCategoryAnimationType),
                        weaponAnimations.get(characterWeaponIdAnimationType), null, null),
                new setCurrentHitAnimation(battleHitAnimations.get(weaponCategoryAnimationType)),
                new showMainCharacterAnimation(currentTurnCharacter, false),

                // defender walk back to miss attack
                myActions.new setWalkDirectionWithShadow(defendingCharacter, shadowMap.get(defendingCharacter), walkDirectionDefender),
                Actions.parallel(Actions.addAction(Actions.moveTo(defender.getCurrentPosition().x + evasionOffset, defender.getCurrentPosition().y, 0.25f, Interpolation.linear), defendingCharacter),
                                 Actions.addAction(Actions.moveTo(defender.getCurrentPosition().x + evasionOffset, defender.getCurrentPosition().y - shadowYOffset, 0.25f, Interpolation.linear), shadowMap.get(defendingCharacter))),

                // Framerate * # of Frames
                Actions.delay(0.5f),
                new setCurrentBattleAnimations(null, null, null, null),
                new showMainCharacterAnimation(currentTurnCharacter, true),

                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkOut),
                Actions.parallel(Actions.addAction(Actions.moveTo(walkOutDestinationX, selectedEntity.getCurrentPosition().y, 0.25f, Interpolation.linear), currentTurnCharacter),
                                 Actions.addAction(Actions.moveTo(walkOutDestinationX, selectedEntity.getCurrentPosition().y - shadowYOffset, 0.25f, Interpolation.linear), shadowMap.get(currentTurnCharacter))),
                Actions.delay(0.25f),

                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), Entity.AnimationType.IDLE),

                // defender walk back into place
                myActions.new setIdleDirection(currentTurnCharacter, Entity.Direction.LEFT),
                myActions.new setWalkDirectionWithShadow(defendingCharacter, shadowMap.get(defendingCharacter), walkDirectionDefender),
                Actions.parallel(Actions.addAction(Actions.moveTo(defender.getCurrentPosition().x, defender.getCurrentPosition().y, 0.25f, Interpolation.linear), defendingCharacter),
                                 Actions.addAction(Actions.moveTo(defender.getCurrentPosition().x, defender.getCurrentPosition().y - shadowYOffset, 0.25f, Interpolation.linear), shadowMap.get(defendingCharacter))),
                Actions.delay(0.15f),
                myActions.new setWalkDirectionWithShadow(defendingCharacter, shadowMap.get(defendingCharacter), Entity.AnimationType.IDLE),

                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkDirectionFromAttack),
                Actions.parallel(Actions.addAction(Actions.moveTo(walkOutDestinationX, currentTurnCharacter.getY(), 0.15f, Interpolation.linear), currentTurnCharacter),
                                 Actions.addAction(Actions.moveTo(walkOutDestinationX, currentTurnCharacter.getY() - shadowYOffset, 0.15f, Interpolation.linear), shadowMap.get(currentTurnCharacter))),
                Actions.delay(0.1f),

                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkBackIntoPlace),
                Actions.parallel(Actions.addAction(Actions.moveTo(currentTurnCharacter.getX(), currentTurnCharacter.getY(), 0.45f, Interpolation.linear), currentTurnCharacter),
                                 Actions.addAction(Actions.moveTo(currentTurnCharacter.getX(), currentTurnCharacter.getY() - shadowYOffset, 0.45f, Interpolation.linear), shadowMap.get(currentTurnCharacter))),
                Actions.delay(0.45f),

                // turn to face victim
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), walkTowardsVictim),
                myActions.new setWalkDirectionWithShadow(currentTurnCharacter, shadowMap.get(currentTurnCharacter), Entity.AnimationType.IDLE),

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
        } else {
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
                        Actions.parallel(Actions.addAction(Actions.moveTo(partyDestinationX_1_3_5, party1.getY(), duration, Interpolation.linear), party1),
                                Actions.addAction(Actions.moveTo(partyDestinationX_1_3_5, party1.getY() - shadowYOffset, duration, Interpolation.linear), shadowMap.get(party1))),
                        Actions.parallel(Actions.addAction(Actions.moveTo(partyDestinationX__2_4_, party2.getY(), duration, Interpolation.linear), party2),
                                Actions.addAction(Actions.moveTo(partyDestinationX__2_4_, party2.getY() - shadowYOffset, duration, Interpolation.linear), shadowMap.get(party2))),
                        Actions.parallel(Actions.addAction(Actions.moveTo(partyDestinationX_1_3_5, party3.getY(), duration, Interpolation.linear), party3),
                                Actions.addAction(Actions.moveTo(partyDestinationX_1_3_5, party3.getY() - shadowYOffset, duration, Interpolation.linear), shadowMap.get(party3))),
                        Actions.parallel(Actions.addAction(Actions.moveTo(partyDestinationX__2_4_, party4.getY(), duration, Interpolation.linear), party4),
                                Actions.addAction(Actions.moveTo(partyDestinationX__2_4_, party4.getY() - shadowYOffset, duration, Interpolation.linear), shadowMap.get(party4))),
                        Actions.parallel(Actions.addAction(Actions.moveTo(partyDestinationX_1_3_5, party5.getY(), duration, Interpolation.linear), party5),
                                Actions.addAction(Actions.moveTo(partyDestinationX_1_3_5, party5.getY() - shadowYOffset, duration, Interpolation.linear), shadowMap.get(party5))),

                        Actions.parallel(Actions.addAction(Actions.moveTo(enemyDestinationX_1_3_5, enemy1.getY(), enemyDuration, Interpolation.linear), enemy1),
                                Actions.addAction(Actions.moveTo(enemyDestinationX_1_3_5, enemy1.getY() - shadowYOffset, enemyDuration, Interpolation.linear), shadowMap.get(enemy1))),
                        Actions.parallel(Actions.addAction(Actions.moveTo(enemyDestinationX__2_4_, enemy2.getY(), enemyDuration, Interpolation.linear), enemy2),
                                Actions.addAction(Actions.moveTo(enemyDestinationX__2_4_, enemy2.getY() - shadowYOffset, enemyDuration, Interpolation.linear), shadowMap.get(enemy2))),
                        Actions.parallel(Actions.addAction(Actions.moveTo(enemyDestinationX_1_3_5, enemy3.getY(), enemyDuration, Interpolation.linear), enemy3),
                                Actions.addAction(Actions.moveTo(enemyDestinationX_1_3_5, enemy3.getY() - shadowYOffset, enemyDuration, Interpolation.linear), shadowMap.get(enemy3))),
                        Actions.parallel(Actions.addAction(Actions.moveTo(enemyDestinationX__2_4_, enemy4.getY(), enemyDuration, Interpolation.linear), enemy4),
                                Actions.addAction(Actions.moveTo(enemyDestinationX__2_4_, enemy4.getY() - shadowYOffset, enemyDuration, Interpolation.linear), shadowMap.get(enemy4))),
                        Actions.parallel(Actions.addAction(Actions.moveTo(enemyDestinationX_1_3_5, enemy5.getY(), enemyDuration, Interpolation.linear), enemy5),
                                Actions.addAction(Actions.moveTo(enemyDestinationX_1_3_5, enemy5.getY() - shadowYOffset, enemyDuration, Interpolation.linear), shadowMap.get(enemy5)))
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
        Entity.AnimationType runDirection;
        Entity.AnimationType battlePositionParty = Entity.AnimationType.WALK_LEFT;
        Entity.AnimationType battlePositionEnemy = Entity.AnimationType.WALK_RIGHT;

        // direction and destinations change if it's a back battle
        if (_game.battleState.isBackBattle()) {
            runDirection = Entity.AnimationType.RUN_LEFT;
            tilesPerSec *= -1;
        } else {
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
                        Actions.parallel(Actions.addAction(Actions.moveTo(partyDestinationX_1_3_5, party1.getY(), duration, Interpolation.linear), party1),
                                Actions.addAction(Actions.moveTo(partyDestinationX_1_3_5, party1.getY() - shadowYOffset, duration, Interpolation.linear), shadowMap.get(party1))),
                        Actions.parallel(Actions.addAction(Actions.moveTo(partyDestinationX__2_4_, party2.getY(), duration, Interpolation.linear), party2),
                                Actions.addAction(Actions.moveTo(partyDestinationX__2_4_, party2.getY() - shadowYOffset, duration, Interpolation.linear), shadowMap.get(party2))),
                        Actions.parallel(Actions.addAction(Actions.moveTo(partyDestinationX_1_3_5, party3.getY(), duration, Interpolation.linear), party3),
                                Actions.addAction(Actions.moveTo(partyDestinationX_1_3_5, party3.getY() - shadowYOffset, duration, Interpolation.linear), shadowMap.get(party3))),
                        Actions.parallel(Actions.addAction(Actions.moveTo(partyDestinationX__2_4_, party4.getY(), duration, Interpolation.linear), party4),
                                Actions.addAction(Actions.moveTo(partyDestinationX__2_4_, party4.getY() - shadowYOffset, duration, Interpolation.linear), shadowMap.get(party4))),
                        Actions.parallel(Actions.addAction(Actions.moveTo(partyDestinationX_1_3_5, party5.getY(), duration, Interpolation.linear), party5),
                                Actions.addAction(Actions.moveTo(partyDestinationX_1_3_5, party5.getY() - shadowYOffset, duration, Interpolation.linear), shadowMap.get(party5))),

                        Actions.parallel(Actions.addAction(Actions.moveTo(enemyDestinationX_1_3_5, enemy1.getY(), duration, Interpolation.linear), enemy1),
                                Actions.addAction(Actions.moveTo(enemyDestinationX_1_3_5, enemy1.getY() - shadowYOffset, duration, Interpolation.linear), shadowMap.get(enemy1))),
                        Actions.parallel(Actions.addAction(Actions.moveTo(enemyDestinationX__2_4_, enemy2.getY(), duration, Interpolation.linear), enemy2),
                                Actions.addAction(Actions.moveTo(enemyDestinationX__2_4_, enemy2.getY() - shadowYOffset, duration, Interpolation.linear), shadowMap.get(enemy2))),
                        Actions.parallel(Actions.addAction(Actions.moveTo(enemyDestinationX_1_3_5, enemy3.getY(), duration, Interpolation.linear), enemy3),
                                Actions.addAction(Actions.moveTo(enemyDestinationX_1_3_5, enemy3.getY() - shadowYOffset, duration, Interpolation.linear), shadowMap.get(enemy3))),
                        Actions.parallel(Actions.addAction(Actions.moveTo(enemyDestinationX__2_4_, enemy4.getY(), duration, Interpolation.linear), enemy4),
                                Actions.addAction(Actions.moveTo(enemyDestinationX__2_4_, enemy4.getY() - shadowYOffset, duration, Interpolation.linear), shadowMap.get(enemy4))),
                        Actions.parallel(Actions.addAction(Actions.moveTo(enemyDestinationX_1_3_5, enemy5.getY(), duration, Interpolation.linear), enemy5),
                                Actions.addAction(Actions.moveTo(enemyDestinationX_1_3_5, enemy5.getY() - shadowYOffset, duration, Interpolation.linear), shadowMap.get(enemy5)))
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
        switch (entityName) {
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
                return steveBattleAnimations;
            case STEVE2:
                return steve2BattleAnimations;
            case STEVE3:
                return steve3BattleAnimations;
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

        if (party1.getEntity() != null)
            party1Shadow.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
        if (party2.getEntity() != null)
            party2Shadow.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
        if (party3.getEntity() != null)
            party3Shadow.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
        if (party4.getEntity() != null)
            party4Shadow.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
        if (party5.getEntity() != null)
            party5Shadow.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));

        if (enemy1.getEntity() != null)
            enemy1Shadow.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
        if (enemy2.getEntity() != null)
            enemy2Shadow.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
        if (enemy3.getEntity() != null)
            enemy3Shadow.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
        if (enemy4.getEntity() != null)
            enemy4Shadow.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
        if (enemy5.getEntity() != null)
            enemy5Shadow.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
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

        party1Shadow.addAction(Actions.fadeOut(fadeTime));
        party2Shadow.addAction(Actions.fadeOut(fadeTime));
        party3Shadow.addAction(Actions.fadeOut(fadeTime));
        party4Shadow.addAction(Actions.fadeOut(fadeTime));
        party5Shadow.addAction(Actions.fadeOut(fadeTime));
        enemy1Shadow.addAction(Actions.fadeOut(fadeTime));
        enemy2Shadow.addAction(Actions.fadeOut(fadeTime));
        enemy3Shadow.addAction(Actions.fadeOut(fadeTime));
        enemy4Shadow.addAction(Actions.fadeOut(fadeTime));
        enemy5Shadow.addAction(Actions.fadeOut(fadeTime));
    }

    public static void setMapType(MapFactory.MapType type) {
        mapType = type;
    }

    @Override
    public void show() {
        _mapMgr.loadMap(mapType);

        completeAllActions();

        _stage.addAction(getBattleSceneAction());

        ProfileManager.getInstance().addObserver(_mapMgr);

        Gdx.input.setInputProcessor(_multiplexer);

        if (_mapRenderer == null) {
            _mapRenderer = new OrthogonalTiledMapRenderer(_mapMgr.getCurrentTiledMap(), Map.UNIT_SCALE);
        }

        fadeInCharacters(0.5f);
        showStatusArrows = true;
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

        if (_mapMgr.hasMapChanged()) {
            _mapRenderer.setMap(_mapMgr.getCurrentTiledMap());
            _mapMgr.setMapChanged(false);
        }

        // this is to fix an issue with the wrong map being flashed for the first frame
        if (!isFirstTime) {
            _mapRenderer.render();
        } else {
            isFirstTime = false;
        }

        if (!_isCameraFixed) {
            _camera.position.set(party1.getX() - cameraRunningOffset, _camera.position.y, 0f);
        }

        _camera.update();

        _stage.act(delta);

        // correct characters' z layer based on selected character
        //(this is needed for walking animation)
        correctCharactersZLayer();

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

                if (selectedEntity != null && battleHUD.selectedCharacter != null) {
                    _mapRenderer.getBatch().draw(selectedEntityIndicator, selectedEntity.getCurrentPosition().x + characterWidth / 2 * 0.5f, selectedEntity.getCurrentPosition().y + characterHeight * 1.1f, 0.5f, 0.5f);
                }

                _mapRenderer.getBatch().end();
            } else if (currentTurnFlashTimer > 0.75f) {
                currentTurnFlashTimer = 0;
            }
        }

        //////////////////////////////////////////////////////
        // Battle animation
        if (currentCharacterAnimation != null && currentTurnCharacter != null) {
            _frameTime = (_frameTime + delta) % 5;
            _currentFrame = currentCharacterAnimation.getKeyFrame(_frameTime);

            currentCharacterWeaponFrame = null;
            currentDefenderFrame = null;
            currentDefenderWeaponFrame = null;

            if (currentCharacterWeaponAnimation != null)
                currentCharacterWeaponFrame = currentCharacterWeaponAnimation.getKeyFrame(_frameTime);

            if (currentDefenderAnimation != null)
                currentDefenderFrame = currentDefenderAnimation.getKeyFrame(_frameTime);

            if (currentDefenderWeaponAnimation != null)
                currentDefenderWeaponFrame = currentDefenderWeaponAnimation.getKeyFrame(_frameTime);

            _mapRenderer.getBatch().begin();
            if (_currentFrame != null) {
                float regionWidth;
                float regionHeight;

                //adjust for character width/height vs. animation region width/height
                float adjustWidth;
                float adjustHeight;

                if (currentCharacterWeaponFrame != null) {
                    regionWidth = currentCharacterWeaponFrame.getRegionWidth() * Map.UNIT_SCALE;
                    regionHeight = currentCharacterWeaponFrame.getRegionHeight() * Map.UNIT_SCALE;
                    adjustWidth = (regionWidth - characterWidth) / 2;
                    adjustHeight = (regionHeight - characterHeight) / 2;

                    // draw weapon first (after stage)
                    _mapRenderer.getBatch().draw(currentCharacterWeaponFrame, currentTurnCharacter.getX() - adjustWidth, currentTurnCharacter.getY() - adjustHeight,
                                                regionWidth, regionHeight);
                }

                // draw attacker animation
                regionWidth = _currentFrame.getRegionWidth() * Map.UNIT_SCALE;
                regionHeight = _currentFrame.getRegionHeight() * Map.UNIT_SCALE;
                adjustWidth = (regionWidth - characterWidth) / 2;
                adjustHeight = (regionHeight - characterHeight) / 2;
                _mapRenderer.getBatch().draw(_currentFrame, currentTurnCharacter.getX() - adjustWidth, currentTurnCharacter.getY() - adjustHeight, regionWidth, regionHeight);

                if (currentHitAnimation != null && selectedEntity != null && !isMissHit) {
                    //draw hit animation
                    hitFrameTime = (hitFrameTime + delta) % 5;
                    currentHitFrame = currentHitAnimation.getKeyFrame(hitFrameTime);

                    float hitRegionWidth = currentHitFrame.getRegionWidth() * Map.UNIT_SCALE;
                    float hitRegionHeight = currentHitFrame.getRegionHeight() * Map.UNIT_SCALE;

                    if (currentDefenderAnimation != null) {
                        // special hit animation for defender blocking
                        float offset;
                        if (selectedEntity.getBattleEntityType().equals(Entity.BattleEntityType.PARTY))
                            offset = (hitRegionWidth / 3) * -1;
                        else
                            offset = (hitRegionWidth / 3) - characterWidth;

                        _mapRenderer.getBatch().draw(currentHitFrame, defendingCharacter.getX() + offset, defendingCharacter.getY(), hitRegionWidth, hitRegionHeight);
                    }
                    else {
                        _mapRenderer.getBatch().draw(currentHitFrame, selectedEntity.getCurrentPosition().x, selectedEntity.getCurrentPosition().y, hitRegionWidth, hitRegionHeight);
                    }

                    //draw any character that should be in a z layer above the current turn character hit animation
                    //(this has to be done here because the hit animation is drawn after the stage draw)
                    redrawPostHitAnimation();
                }

                if (currentDefenderAnimation != null) {
                    //draw defender animation
                    currentDefenderFrame = currentDefenderAnimation.getKeyFrame(_frameTime);

                    if (currentDefenderFrame != null) {
                        if (currentDefenderWeaponFrame != null) {
                            regionWidth = currentDefenderWeaponFrame.getRegionWidth() * Map.UNIT_SCALE;
                            regionHeight = currentDefenderWeaponFrame.getRegionHeight() * Map.UNIT_SCALE;
                            adjustWidth = (regionWidth - characterWidth) / 2;
                            adjustHeight = (regionHeight - characterHeight) / 2;

                            // draw weapon
                            _mapRenderer.getBatch().draw(currentDefenderWeaponFrame, defendingCharacter.getX() - adjustWidth, defendingCharacter.getY() - adjustHeight,
                                    regionWidth, regionHeight);
                        }

                        // draw defender animation
                        regionWidth = currentDefenderFrame.getRegionWidth() * Map.UNIT_SCALE;
                        regionHeight = currentDefenderFrame.getRegionHeight() * Map.UNIT_SCALE;
                        adjustWidth = (regionWidth - characterWidth) / 2;
                        adjustHeight = (regionHeight - characterHeight) / 2;
                        _mapRenderer.getBatch().draw(currentDefenderFrame, defendingCharacter.getX() - adjustWidth, defendingCharacter.getY() - adjustHeight, regionWidth, regionHeight);
                    }

                }
            }
            _mapRenderer.getBatch().end();
        }

        for (int i = 0; i < battleBursts.size(); i++) {
            BattleBurst bb = battleBursts.get(i);
            if (bb.show) {
                updateBattleBurst(delta, bb);
            }
        }

        if (throwingItem.show)
            updateThrowingItem(delta);

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

    private void correctCharactersZLayer() {
        // sort list in descending order by Y position
        Collections.sort(characterShadowSortList, new CharacterLayerComparator());

        // now set the z order of each character and shadow based on layer
        // shadow should be on the layer right before the character
        for (int i = 0; i < characterShadowSortList.size(); i++) {
            AnimatedImageWithShadow characterWithShadow = characterShadowSortList.get(i);
            characterWithShadow.shadow.setZIndex(i);
            characterWithShadow.character.setZIndex(i + 1);
        }
    }

    private void redrawPostHitAnimation() {
        // only care about re-drawing character 2 or 4 if selected character is in position 1 or 3

        if (selectedEntity.getBattleEntityType().equals(Entity.BattleEntityType.PARTY)) {
            switch (selectedEntity.getBattlePosition()) {
                case 1:
                    if (party2.getStage() != null)
                        party2.draw(_mapRenderer.getBatch(),1);
                    break;
                case 3:
                    if (party4.getStage() != null)
                        party4.draw(_mapRenderer.getBatch(),1);
                    break;
            }
        }
        else {
            switch (selectedEntity.getBattlePosition()) {
                case 1:
                    if (enemy2.getStage() != null)
                        enemy2.draw(_mapRenderer.getBatch(),1);
                    break;
                case 3:
                    if (enemy4.getStage() != null)
                        enemy4.draw(_mapRenderer.getBatch(),1);
                    break;
            }
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
        //setGameState(Chapter2.GameState.RUNNING);
        if (battleHUD != null)
            battleHUD.resume();
    }

    @Override
    public void dispose() {
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
            case SPELLS_POWER:
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
                party1Shadow.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                _stage.addActor(party1Shadow);
                _stage.addActor(party1);
                break;
            case 2:
                party2.setEntity(partyEntity);
                party2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                party2.setCurrentDirection(Entity.Direction.LEFT);
                _stage.addActor(party2Shadow);
                _stage.addActor(party2);
                break;
            case 3:
                party3.setEntity(partyEntity);
                party3.setCurrentAnimationType(Entity.AnimationType.IDLE);
                party3.setCurrentDirection(Entity.Direction.LEFT);
                _stage.addActor(party3Shadow);
                _stage.addActor(party3);
                break;
            case 4:
                party4.setEntity(partyEntity);
                party4.setCurrentAnimationType(Entity.AnimationType.IDLE);
                party4.setCurrentDirection(Entity.Direction.LEFT);
                _stage.addActor(party4Shadow);
                _stage.addActor(party4);
                break;
            case 5:
                party5.setEntity(partyEntity);
                party5.setCurrentAnimationType(Entity.AnimationType.IDLE);
                party5.setCurrentDirection(Entity.Direction.LEFT);
                _stage.addActor(party5Shadow);
                _stage.addActor(party5);
                break;
        }

        _stage.addActor(blackScreen);
    }

    public void removeAllPartyMembers() {
        for (int i = 0; i < 5; i++)
            removePartyMemberByIndex(i);
    }

    public void removePartyMemberByIndex(int index) {

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

        Gdx.app.log(TAG, "Adding enemy " + enemyEntity.getEntityConfig().getDisplayName());

        // make sure fade in image is on top of z order
        blackScreen.remove();

        switch (index) {
            case 1:
                enemy1.setEntity(enemyEntity);
                enemy1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                enemy1.setCurrentDirection(Entity.Direction.RIGHT);
                _stage.addActor(enemy1Shadow);
                _stage.addActor(enemy1);
                break;
            case 2:
                enemy2.setEntity(enemyEntity);
                enemy2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                enemy2.setCurrentDirection(Entity.Direction.RIGHT);
                _stage.addActor(enemy2Shadow);
                _stage.addActor(enemy2);
                break;
            case 3:
                enemy3.setEntity(enemyEntity);
                enemy3.setCurrentAnimationType(Entity.AnimationType.IDLE);
                enemy3.setCurrentDirection(Entity.Direction.RIGHT);
                _stage.addActor(enemy3Shadow);
                _stage.addActor(enemy3);
                break;
            case 4:
                enemy4.setEntity(enemyEntity);
                enemy4.setCurrentAnimationType(Entity.AnimationType.IDLE);
                enemy4.setCurrentDirection(Entity.Direction.RIGHT);
                _stage.addActor(enemy4Shadow);
                _stage.addActor(enemy4);
                break;
            case 5:
                enemy5.setEntity(enemyEntity);
                enemy5.setCurrentAnimationType(Entity.AnimationType.IDLE);
                enemy5.setCurrentDirection(Entity.Direction.RIGHT);
                _stage.addActor(enemy5Shadow);
                _stage.addActor(enemy5);
                break;
        }

        _stage.addActor(blackScreen);
    }

    public void removeAllOpponents() {
        for (int i = 0; i < 5; i++)
            removeOpponentByIndex(i);
    }

    public void removeOpponentByIndex(int index) {

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

    private Timer.Task removeOpponent(final AnimatedImage enemy, final AnimatedImage enemyShadow) {
        return new Timer.Task() {
            @Override
            public void run() {
                enemy.remove();
                enemyShadow.remove();
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
        Hashtable<Entity.AnimationType, Animation<TextureRegion>> animations;

        if (c == '1')
            animations = GraphicsComponent.loadAnimationsByName(EntityFactory.EntityName.ONE);
        else if (c == '2')
            animations = GraphicsComponent.loadAnimationsByName(EntityFactory.EntityName.TWO);
        else if (c == '3')
            animations = GraphicsComponent.loadAnimationsByName(EntityFactory.EntityName.THREE);
        else if (c == '4')
            animations = GraphicsComponent.loadAnimationsByName(EntityFactory.EntityName.FOUR);
        else if (c == '5')
            animations = GraphicsComponent.loadAnimationsByName(EntityFactory.EntityName.FIVE);
        else if (c == '6')
            animations = GraphicsComponent.loadAnimationsByName(EntityFactory.EntityName.SIX);
        else if (c == '7')
            animations = GraphicsComponent.loadAnimationsByName(EntityFactory.EntityName.SEVEN);
        else if (c == '8')
            animations = GraphicsComponent.loadAnimationsByName(EntityFactory.EntityName.EIGHT);
        else if (c == '9')
            animations = GraphicsComponent.loadAnimationsByName(EntityFactory.EntityName.NINE);
        else if (c == '0')
            animations = GraphicsComponent.loadAnimationsByName(EntityFactory.EntityName.ZERO);
        else
            animations = GraphicsComponent.loadAnimationsByName(EntityFactory.EntityName.ZERO);

        Animation<TextureRegion> animation = animations.get(Entity.AnimationType.BATTLE_BURST);
        TextureRegion frame = animation.getKeyFrame(0);
        digitImage = new Image(new TextureRegion(frame));

        digitImage.setScale(0.5f);
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

    private void resetBattleStats() {
        currentTurnCharacter = null;
        currentTurnEntity = null;
        selectedEntity = null;
        party1StatArrows.clear();
        party2StatArrows.clear();
        party3StatArrows.clear();
        party4StatArrows.clear();
        party5StatArrows.clear();
        enemy1StatArrows.clear();
        enemy2StatArrows.clear();
        enemy3StatArrows.clear();
        enemy4StatArrows.clear();
        enemy5StatArrows.clear();
        battleWon = false;
    }

    private void endThrowingItem() {
        throwingItem.show = false;
        itemIsBeingThrown = false;
        for (BattleBurst bb : battleBursts) {
            if (bb.isDelayed) {
                for (Image image : bb.imageArray) {
                    image.setVisible(true);
                }
                bb.show = true;
            }
        }
    }

    private void updateThrowingItem(float delta) {
        throwingItem.positionX += throwingItem.velocityX * delta;   // Apply horizontal velocity to X position
        throwingItem.positionY += throwingItem.velocityY * delta;   // Apply vertical velocity to X position
        throwingItem.velocityY += gravity * delta;                  // Apply gravity to vertical velocity

        if (throwingItem.throwingDirection.equals(ThrowingDirection.RIGHT)) {
            if (throwingItem.positionX > throwingItem.endPositionX) {
                endThrowingItem();
                return;
            }
        }
        else {
            if (throwingItem.positionX < throwingItem.endPositionX) {
                endThrowingItem();
                return;
            }
        }

        throwingItem.frameTime = (throwingItem.frameTime + delta) % 5;
        throwingItem.currentFrame = throwingItem.animation.getKeyFrame(throwingItem.frameTime);

        _mapRenderer.getBatch().begin();
        if (_currentFrame != null) {
            _mapRenderer.getBatch().draw(throwingItem.currentFrame, throwingItem.positionX, throwingItem.positionY,
                    throwingItem.currentFrame.getRegionWidth() * Map.UNIT_SCALE, throwingItem.currentFrame.getRegionHeight() * Map.UNIT_SCALE);
        }
        _mapRenderer.getBatch().end();
    }

    private void initThrowingItem(Entity.AnimationType animationType, boolean isMissHit) {
        /*
        Parabolic trajectory function is defined as:

        Fx = Vox*t + Ox;
        Fy = -0.5 * g * t * t + Voy*t + Oy;

        Known values:
            P: the target point.
            O: the origin point.
            g: gravity.
            t: time needed to impact.

        Unknown values:
        Vo: Initial Velocity

        To calculate 'Vo', we can give values to F function:

        't' = flight time  'duration'
        'F' = target point 'P'

              (Px - Ox)       // if throwing left, then (Ox - Px)
        Vox = ---------
              duration

               Py + 0.5 * g * duration * duration - Oy
        Voy = ----------------------------------------
                           duration

        You can now get all the values to reach the target from the origin giving values to t into the F equation:

        When t = 0         => F == O (Origin)
        When t = duration  => F == P (Target)
        */

        Vector2 P = new Vector2(selectedEntity.getCurrentPosition().x,
                                selectedEntity.getCurrentPosition().y);
        Vector2 O = new Vector2(currentTurnCharacter.getX() + characterWidth / 2,
                                currentTurnCharacter.getY() + characterHeight);

        if (isMissHit) {
            // todo: throw off target
            P.x -= 2;
        }

        float duration = 0.75f;
        gravity = 25;

        Vector2 Vo = new Vector2();
        Vo.x = (O.x - P.x) / duration;
        Vo.y = (P.y + 0.5f * gravity * duration * duration - O.y) / duration;

        if (animationType.toString().contains("LEFT"))
            throwingItem.throwingDirection = ThrowingDirection.LEFT;
        else
            throwingItem.throwingDirection = ThrowingDirection.RIGHT;

        throwingItem.velocityX = Vo.x;
        throwingItem.velocityY = Vo.y;
        gravity *= -1;

        if (currentTurnEntity.getBattleEntityType().equals(Entity.BattleEntityType.PARTY)) {
            throwingItem.positionX = currentTurnCharacter.getX() - currentTurnCharacter.getWidth() / 2;// - (image.getWidth() * Map.UNIT_SCALE) / 2;
            throwingItem.velocityX *= -1;
        }
        else {
            throwingItem.positionX = currentTurnCharacter.getX() + currentTurnCharacter.getWidth() / 2;// - (image.getWidth() * Map.UNIT_SCALE) / 2;
        }

        throwingItem.animation = weaponAnimations.get(animationType);
        throwingItem.positionX = O.x;
        throwingItem.positionY = O.y;
        throwingItem.endPositionX = P.x;
        throwingItem.endPositionY = P.y;

        throwingItem.show = true;
    }

    private void updateBattleBurst(float delta, BattleBurst bb) {

        bb.positionX += bb.velocityX * delta;   // Apply horizontal velocity to X position
        bb.positionY += bb.velocityY * delta;   // Apply vertical velocity to X position
        bb.velocityY += gravity * delta;        // Apply gravity to vertical velocity

        if (bb.positionY < battleHUDHeight) {
            bb.positionY = battleHUDHeight;
            bb.velocityY = bounceVelocityY;
            bounceVelocityY *= 0.75f;
            bb.velocityX *= 0.85f;

            if (bounceVelocityY < 0.125f) {
                bb.velocityY = 0;
                for (Image image : bb.imageArray) {
                    image.addAction(Actions.fadeOut(0.5f));
                }
                bb.show = false;
                bb.isDelayed = false;

                for (BattleBurst burst : battleBursts) {
                    if (bb == burst) {
                        battleBursts.remove(bb);
                    }
                }
            }
        }

        float lastPositionX = bb.positionX;

        for (Image image : bb.imageArray) {
            image.setPosition(lastPositionX, bb.positionY);
            lastPositionX += image.getWidth() * Map.UNIT_SCALE;
        }
    }

    private Image getSpecialImage(String name) {
        Image image = null;
        Hashtable<Entity.AnimationType, Animation<TextureRegion>> animations = null;

        if (name.equals(CRIT_HIT))
            animations = GraphicsComponent.loadAnimationsByName(EntityFactory.EntityName.CRIT);
        else if (name.equals(MISS_HIT))
            animations = GraphicsComponent.loadAnimationsByName(EntityFactory.EntityName.MISS);
        else if (name.equals(WEAK_HIT))
            animations = GraphicsComponent.loadAnimationsByName(EntityFactory.EntityName.WEAK);

        if (animations != null) {
            Animation<TextureRegion> animation = animations.get(Entity.AnimationType.BATTLE_BURST);
            TextureRegion frame = animation.getKeyFrame(0);
            image = new Image(new TextureRegion(frame));
            image.setScale(0.5f);
        }
        return image;
    }

    private void initBattleBurst(BattleBurst bb, AnimatedImage character, String hitValue, Entity.BattleEntityType battleType) {
        // hide any lingering battle bursts
        for (int i = 0; i < 5; i++) {
            for (Image image : bb.imageArray) {
                image.addAction(Actions.fadeOut(0));
            }
        }

        bb.imageArray.clear();

        if (hitValue.equals(CRIT_HIT) || hitValue.equals(MISS_HIT) || hitValue.equals(WEAK_HIT)) {
            Image image = getSpecialImage(hitValue);
            image.setScale(Map.UNIT_SCALE);

            float positionX = character.getX() + character.getWidth() / 2 - (image.getWidth() * Map.UNIT_SCALE) / 2;
            float positionY = character.getY() + character.getHeight() * 1.2f;
            image.setPosition(positionX, positionY);

            bb.imageArray.add(image);
            _stage.addActor(image);
        }
        else {
            float hitPointsImageWidth = 0;

            // populate image array of numbers
            for (int i = 0; i < hitValue.length(); i++) {
                Image image = getDigitImage(hitValue.charAt(i));
                hitPointsImageWidth += image.getWidth() * Map.UNIT_SCALE;
                bb.imageArray.add(image);
            }

            // for each number image set its position based on previous number's position
            float lastPositionX;
            if (itemIsBeingThrown) {
                // make battle burst come more off front of character
                if (battleType.equals(Entity.BattleEntityType.ENEMY))
                    lastPositionX = character.getX() + character.getWidth();
                else
                    lastPositionX = character.getX();
            }
            else {
                lastPositionX = character.getX() + character.getWidth() / 2 - hitPointsImageWidth / 2;
            }

            float lastPositionY = character.getY() + character.getHeight() * 1.2f;
            for (Image image : bb.imageArray) {
                image.setScale(Map.UNIT_SCALE);
                image.setPosition(lastPositionX, lastPositionY);
                lastPositionX += image.getWidth() * Map.UNIT_SCALE;
                image.setVisible(false);
                _stage.addActor(image);
            }
        }

        float minVelocityX = 2f;
        float maxVelocityX = 5;
        float minVelocityY = 0.5f;
        float maxVelocityY = 5;

        if (ElmourGame.isAndroid()) {
            minVelocityX = 2f;
            maxVelocityX = 6.5f;
            minVelocityY = 0.5f;
            maxVelocityY = 6.5f;
        }

        float velocityX = MathUtils.random(minVelocityX, maxVelocityX);
        float velocityY = MathUtils.random(minVelocityY, maxVelocityY);

        bb.velocityY = velocityX;
        bb.velocityX = velocityY;
        gravity = -25f;
        bounceVelocityY = 8f;

        if (battleType.equals(Entity.BattleEntityType.PARTY))
            bb.velocityX *= -1;

        if (bb.imageArray.size > 0) {
            bb.positionX = bb.imageArray.get(0).getX();
            bb.positionY = bb.imageArray.get(0).getY();
        }

        if (itemIsBeingThrown) {
            // Don't show battle burst here if it is due to item is being thrown.
            // Battle burst will be shown elsewhere after item has reached target.
            bb.isDelayed = true;
        }
        else {
            for (Image image : bb.imageArray) {
                image.setVisible(true);
            }
            bb.show = true;
        }
    }

    private void hitPointAnimation(final Entity entity, final String hitValue) {

        AnimatedImage character = getAnimatedImageFromEntity(entity);
/*
        if (character != null) {
            if (hitValue.equals(CRIT_HIT) || hitValue.equals(MISS_HIT) || hitValue.equals(WEAK_HIT)) {
                BattleBurst bb = new BattleBurst();
                initBattleBurst(bb, character, hitValue, entity.getBattleEntityType());
                specialBattleBursts.add(bb);
            }
            else {
                initBattleBurst(hpBattleBurst, character, hitValue, entity.getBattleEntityType());
            }
        }
*/
        if (character != null) {
            BattleBurst bb = new BattleBurst();
            initBattleBurst(bb, character, hitValue, entity.getBattleEntityType());
            battleBursts.add(bb);
        }

        isMissHit = false;
    }

    private Timer.Task getHitPointAnimation(final Entity entity, final String hitValue) {
        return new Timer.Task() {
            @Override
            public void run() {
                hitPointAnimation(entity, hitValue);
            }
        };
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
                resetBattleStats();
                break;
            case PLAYER_FAILED_TO_ESCAPE:
                _stage.addAction(getPlayerFailedEscapeAction());
                selectedEntity = null;
                break;
            case OPPONENT_DEFEATED:
                float fadeOutTime = 1;
                selectedEntity = null;
                StatusArrows statusArrows;

                if (enemy1.getEntity() != null) {
                    if (enemy1.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                        statusArrows = getStatArrows(enemy1.getEntity());
                        statusArrows.clear();
                        enemy1.addAction(Actions.fadeOut(fadeOutTime));
                        enemy1Shadow.addAction(Actions.fadeOut(fadeOutTime));
                        if (!removeOpponent(enemy1, enemy1Shadow).isScheduled()) {
                            Timer.schedule(removeOpponent(enemy1, enemy1Shadow), fadeOutTime);
                        }
                    }
                }
                if (enemy2.getEntity() != null) {
                    if (enemy2.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                        statusArrows = getStatArrows(enemy2.getEntity());
                        statusArrows.clear();
                        enemy2.addAction(Actions.fadeOut(fadeOutTime));
                        enemy2Shadow.addAction(Actions.fadeOut(fadeOutTime));
                        if (!removeOpponent(enemy2, enemy2Shadow).isScheduled()) {
                            Timer.schedule(removeOpponent(enemy2, enemy2Shadow), fadeOutTime);
                        }
                    }
                }
                if (enemy3.getEntity() != null) {
                    if (enemy3.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                        statusArrows = getStatArrows(enemy3.getEntity());
                        statusArrows.clear();
                        enemy3.addAction(Actions.fadeOut(fadeOutTime));
                        enemy3Shadow.addAction(Actions.fadeOut(fadeOutTime));
                        if (!removeOpponent(enemy3, enemy3Shadow).isScheduled()) {
                            Timer.schedule(removeOpponent(enemy3, enemy3Shadow), fadeOutTime);
                        }
                    }
                }
                if (enemy4.getEntity() != null) {
                    if (enemy4.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                        statusArrows = getStatArrows(enemy4.getEntity());
                        statusArrows.clear();
                        enemy4.addAction(Actions.fadeOut(fadeOutTime));
                        enemy4Shadow.addAction(Actions.fadeOut(fadeOutTime));
                        if (!removeOpponent(enemy4, enemy4Shadow).isScheduled()) {
                            Timer.schedule(removeOpponent(enemy4, enemy4Shadow), fadeOutTime);
                        }
                    }
                }
                if (enemy5.getEntity() != null) {
                    if (enemy5.getEntity().getEntityConfig().getDisplayName().equals(entity.getEntityConfig().getDisplayName())) {
                        statusArrows = getStatArrows(enemy5.getEntity());
                        statusArrows.clear();
                        enemy5.addAction(Actions.fadeOut(fadeOutTime));
                        enemy5Shadow.addAction(Actions.fadeOut(fadeOutTime));
                        if (!removeOpponent(enemy5, enemy5Shadow).isScheduled()) {
                            Timer.schedule(removeOpponent(enemy5, enemy5Shadow), fadeOutTime);
                        }
                    }
                }
                break;
            case ANNIMATION_COMPLETE:
                switch (BattleScreen.getAnimationState()) {
                    case BATTLE:
                        if (battleWon)
                            resetBattleStats();
                        break;
                }
                break;
            case CRITICAL_HIT:
                hitPointAnimation(entity, CRIT_HIT);
                break;
            case WEAK_HIT:
                hitPointAnimation(entity, WEAK_HIT);
                break;
            case GAME_OVER:
            case BATTLE_OVER:
                blackScreen.setVisible(true);
                _stage.addAction(fadeOutAction());
                resetBattleStats();
                break;
            case BATTLE_WON:
                battleWon = true;
                break;
        }
    }

    private void setDefendingCharacter(Entity defender) {
        switch (defender.getBattlePosition()) {
            case 1:
                if (defender.getBattleEntityType().equals(Entity.BattleEntityType.PARTY))
                    defendingCharacter = party1;
                else
                    defendingCharacter = enemy1;
                break;
            case 2:
                if (defender.getBattleEntityType().equals(Entity.BattleEntityType.PARTY))
                    defendingCharacter = party2;
                else
                    defendingCharacter = enemy2;
                break;
            case 3:
                if (defender.getBattleEntityType().equals(Entity.BattleEntityType.PARTY))
                    defendingCharacter = party3;
                else
                    defendingCharacter = enemy3;
                break;
            case 4:
                if (defender.getBattleEntityType().equals(Entity.BattleEntityType.PARTY))
                    defendingCharacter = party4;
                else
                    defendingCharacter = enemy4;
                break;
            case 5:
                if (defender.getBattleEntityType().equals(Entity.BattleEntityType.PARTY))
                    defendingCharacter = party5;
                else
                    defendingCharacter = enemy5;
                break;
        }
    }

    @Override
    public void onNotify(Entity sourceEntity, Entity destinationEntity, BattleEventWithMessage event, String message) {
        switch (event) {
            case PLAYER_ATTACKS:
                _stage.addAction(getAttackOpponentAction(sourceEntity));
                break;
            case PLAYER_APPLYING_INVENTORY:
                _stage.addAction(getApplyInventoryAction(sourceEntity));
                break;
            case PLAYER_TURN_DONE:
            case PLAYER_APPLIED_INVENTORY:
                selectedEntity = null;
                break;
            case PLAYER_THROWING_ITEM:
            case PLAYER_THROWING_ITEM_BUT_MISSED:
                selectedEntity = destinationEntity;
                itemIsBeingThrown = true;
                isMissHit = (event == PLAYER_THROWING_ITEM_BUT_MISSED);
                if (isMissHit) {
                    if (!getHitPointAnimation(destinationEntity, MISS_HIT).isScheduled()) {
                        Timer.schedule(getHitPointAnimation(destinationEntity, MISS_HIT), 1.5f);
                    }
                }
                _stage.addAction(getThrowAction(sourceEntity, event == PLAYER_THROWING_ITEM_BUT_MISSED));
                break;
            case PLAYER_HIT_DAMAGE:
                ////////////////////////////////////////////////////////////////////////////////
                // This condition here is only to clear out the hit value in case there
                // are consecutive hits so that the hit value disappears. getHitPointAnimation
                // calls hitPointAnimation so pay attention to the delaySeconds value!
                if (!getHitPointAnimation(destinationEntity, "").isScheduled()) {
                   Timer.schedule(getHitPointAnimation(destinationEntity, ""), 4f);
                }
                ////////////////////////////////////////////////////////////////////////////////
                hitPointAnimation(destinationEntity, message);
                break;
            case OPPONENT_ATTACKS:
                selectedEntity = destinationEntity;
                _stage.addAction(getAttackOpponentAction(sourceEntity));
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
                setDefendingCharacter(destinationEntity);
                _stage.addAction(getBlockedAttackAction(sourceEntity, destinationEntity));
                break;
            case MISS_HIT:
                selectedEntity = destinationEntity;
                isMissHit = true;
                if (!getHitPointAnimation(destinationEntity, MISS_HIT).isScheduled()) {
                    Timer.schedule(getHitPointAnimation(destinationEntity, MISS_HIT), 1.5f);
                }
                setDefendingCharacter(destinationEntity);
                selectedEntity = null;
                _stage.addAction(getMissedAttackAction(sourceEntity, destinationEntity));

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
