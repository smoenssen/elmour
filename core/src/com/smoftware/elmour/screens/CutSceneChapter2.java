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
import com.smoftware.elmour.sfx.ShakeCamera;

import java.util.ArrayList;

public class CutSceneChapter2 extends CutSceneBase implements ConversationGraphObserver {
    private static final String TAG = CutSceneChapter2.class.getSimpleName();

    CutSceneChapter2 thisScreen;

    private Action setupScene01;
    private Action setupOutsideArmoryScene;
    private Action setupSceneArmory;
    private Action setupSceneWeaponsRoom;
    private Action setupGetWeapon;
    private Action setupOutsideInnScene;
    private Action setupInnScene;
    private Action setupOutsideWoodshopScene;
    private Action setupWoodshopScene;

    private AnimatedImage character1;
    private AnimatedImage character2;
    private AnimatedImage justin;
    private AnimatedImage jaxon;
    private AnimatedImage diane;
    private AnimatedImage ophion;

    private AnimatedImage camactor;
    private AnimatedImage misc;
    private AnimatedImage misc2;

    public CutSceneChapter2(ElmourGame game, PlayerHUD playerHUD) {
        super(game, playerHUD);
        thisScreen = this;

        character1 = getAnimatedImage(EntityFactory.EntityName.CHARACTER_1);
        character2 = getAnimatedImage(EntityFactory.EntityName.CHARACTER_2);
        justin = getAnimatedImage(EntityFactory.EntityName.JUSTIN);
        jaxon = getAnimatedImage(EntityFactory.EntityName.JAXON_1);
        diane = getAnimatedImage(EntityFactory.EntityName.DIANE);
        ophion = getAnimatedImage(EntityFactory.EntityName.OPHION);

        camactor = getAnimatedImage(EntityFactory.EntityName.STEVE);
        misc = getAnimatedImage(EntityFactory.EntityName.MISC_ANIMATIONS);
        misc2 = getAnimatedImage(EntityFactory.EntityName.MISC_ANIMATIONS);

        camactor.setVisible(false);
        misc.setVisible(false);
        misc.setCurrentAnimationType(Entity.AnimationType.FORCEFIELD);
        misc2.setVisible(false);
        misc2.setCurrentAnimationType(Entity.AnimationType.FORCEFIELD);

        _followingActor.setPosition(0, 0);

        _stage.addActor(character1);
        _stage.addActor(character2);
        _stage.addActor(justin);
        _stage.addActor(jaxon);
        _stage.addActor(diane);
        _stage.addActor(ophion);

        _stage.addActor(camactor);
        _stage.addActor(misc);
        _stage.addActor(misc2);
        _stage.addActor(_transitionActor);

        //Actions
        _switchScreenToMainAction = new RunnableAction() {
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
                setCameraPosition(37.5f, 20.5f);

                camactor.setPosition(37.5f, 20.5f);
                camactor.setCurrentAnimationType(Entity.AnimationType.IDLE);
                camactor.setCurrentDirection(Entity.Direction.DOWN);

                character1.setPosition(37, 20.5f);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.RIGHT);

                character2.setPosition(38, 20.5f);
                character2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character2.setCurrentDirection(Entity.Direction.LEFT);

                followActor(camactor);
            }
        };

        setupOutsideArmoryScene = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.ELMOUR);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(26, 24);

                character1.setPosition(25.5f, 23.5f);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.UP);

                character2.setPosition(26.5f, 23.5f);
                character2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character2.setCurrentDirection(Entity.Direction.UP);
            }
        };

        setupSceneArmory = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.ARMORY);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(4.5f, 4);
                keepCamInMap = false;

                camactor.setPosition(4.5f, 4);
                camactor.setCurrentAnimationType(Entity.AnimationType.IDLE);
                camactor.setCurrentDirection(Entity.Direction.DOWN);

                float f = _stage.getWidth();
                float centerX = (4.5f);
                //setCameraPosition(centerX, 3f);

                character1.setVisible(false);
                character1.setPosition(centerX - character1.getWidth() / 2, 1);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.UP);

                character2.setVisible(true);
                character2.setPosition(centerX - character2.getWidth() / 2, 1);
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

                diane.setVisible(false);
                ophion.setVisible(false);

                misc.setPosition(justin.getX() + emoteX, justin.getY() + emoteY);
                misc.setCurrentAnimationType(Entity.AnimationType.SHOCK_OFF);

                followActor(camactor);
            }
        };

        setupSceneWeaponsRoom = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.WEAPONS_ROOM);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(8, -6);
                keepCamInMap = false;

                camactor.setPosition(8, -6);
                camactor.setCurrentAnimationType(Entity.AnimationType.IDLE);
                camactor.setCurrentDirection(Entity.Direction.DOWN);

                character1.setVisible(false);
                character1.setPosition(8, 1);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.LEFT);

                character2.setVisible(false);
                character2.setPosition(8, 1);
                character2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character2.setCurrentDirection(Entity.Direction.UP);

                justin.setVisible(true);
                justin.setPosition(9, 5);
                justin.setCurrentAnimationType(Entity.AnimationType.IDLE);
                justin.setCurrentDirection(Entity.Direction.DOWN);

                jaxon.setVisible(true);
                jaxon.setPosition(7, 5);
                jaxon.setCurrentAnimationType(Entity.AnimationType.IDLE);
                jaxon.setCurrentDirection(Entity.Direction.DOWN);

                misc.setPosition(justin.getX() + emoteX, justin.getY() + emoteY);
                misc.setCurrentAnimationType(Entity.AnimationType.SHOCK_OFF);

                misc2.setVisible(true);
                misc2.setPosition(13, 3);
                misc2.setCurrentAnimationType(Entity.AnimationType.BOOK_STAND);

                followActor(camactor);
            }
        };

        setupGetWeapon = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.WEAPONS_ROOM);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(8, 6);
                keepCamInMap = false;

                camactor.setPosition(8, 6);
                camactor.setCurrentAnimationType(Entity.AnimationType.IDLE);
                camactor.setCurrentDirection(Entity.Direction.DOWN);

                character1.setVisible(true);
                character1.setPosition(7.5f, 6.5f);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.RIGHT);

                character2.setVisible(true);
                character2.setPosition(8.5f, 6.5f);
                character2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character2.setCurrentDirection(Entity.Direction.LEFT);

                justin.setVisible(true);
                justin.setPosition(9, 5);
                justin.setCurrentAnimationType(Entity.AnimationType.IDLE);
                justin.setCurrentDirection(Entity.Direction.UP);

                jaxon.setVisible(true);
                jaxon.setPosition(7, 5);
                jaxon.setCurrentAnimationType(Entity.AnimationType.IDLE);
                jaxon.setCurrentDirection(Entity.Direction.UP);

                followActor(camactor);
            }
        };

        setupOutsideInnScene = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.ELMOUR);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(49, 24);
                keepCamInMap = false;

                character1.setVisible(true);
                character1.setPosition(48.5f, 23.5f);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.RIGHT);

                character2.setVisible(true);
                character2.setPosition(49.5f, 23.5f);
                character2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character2.setCurrentDirection(Entity.Direction.LEFT);

                justin.setVisible(false);
                jaxon.setVisible(false);
                ophion.setVisible(false);
            }
        };

        setupInnScene = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.INN);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(4.5f, 5);
                keepCamInMap = false;

                float f = _stage.getWidth();
                float centerX = (4.5f);
                //setCameraPosition(centerX, 3f);

                character1.setVisible(false);
                character1.setPosition(centerX - character1.getWidth() / 2, 1);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.UP);

                character2.setVisible(true);
                character2.setPosition(centerX - character2.getWidth() / 2, 1);
                character2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character2.setCurrentDirection(Entity.Direction.UP);

                diane.setVisible(true);
                diane.setPosition(centerX- 0.5f, 7.1f);
                diane.setCurrentAnimationType(Entity.AnimationType.IDLE);
                diane.setCurrentDirection(Entity.Direction.DOWN);

                misc.setPosition(diane.getX() + emoteX, diane.getY() + emoteY);
                misc.setCurrentAnimationType(Entity.AnimationType.SHOCK_OFF);
            }
        };
        setupOutsideWoodshopScene = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.ELMOUR);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(60, 24);
                keepCamInMap = false;

                character1.setVisible(true);
                character1.setPosition(59.5f, 23.5f);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.RIGHT);

                character2.setVisible(true);
                character2.setPosition(60.5f, 23.5f);
                character2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character2.setCurrentDirection(Entity.Direction.LEFT);

                misc.setPosition(character1.getX() + emoteX, character1.getY() + emoteY);
                misc.setCurrentAnimationType(Entity.AnimationType.SHOCK_OFF);

                diane.setVisible(false);
                justin.setVisible(false);
                jaxon.setVisible(false);
            }
        };

        setupWoodshopScene = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.INN);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(4.5f, 5);
                keepCamInMap = false;

                float f = _stage.getWidth();
                float centerX = (4.5f);
                //setCameraPosition(centerX, 3f);

                character1.setVisible(false);
                character1.setPosition(centerX - character1.getWidth() / 2, 1);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.UP);

                character2.setVisible(true);
                character2.setPosition(centerX - character2.getWidth() / 2, 1);
                character2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character2.setCurrentDirection(Entity.Direction.UP);

                ophion.setVisible(true);
                ophion.setPosition(centerX- 0.5f, 7.1f);
                ophion.setCurrentAnimationType(Entity.AnimationType.IDLE);
                ophion.setCurrentDirection(Entity.Direction.DOWN);
            }
        };
    }

    @Override
    public void onNotify(ConversationGraph graph, ConversationCommandEvent action, String conversationId) {
        Gdx.app.log(TAG, "Got notification " + action.toString());
        oneBlockTime = 0.3f;
        emoteOn = 0.7f;
        emoteOff = 0.05f;
        closeBook = 1.2f;

        switch (action) {
            case WAIT_1000:
                _playerHUD.doConversation(graph.getNextConversationIDFromChoice(conversationId, 0), 1000);
                break;
            case WAIT_10000:
                _playerHUD.doConversation(graph.getNextConversationIDFromChoice(conversationId, 0), 10000);
                break;
            case PAN_TO_ARMORY:
                _stage.addAction(Actions.sequence(
                        Actions.addAction(Actions.moveTo(26, 27, oneBlockTime * 3), camactor),
                        Actions.delay(oneBlockTime * 0.5f),
                        myActions.new setIdleDirection(character1, Entity.Direction.LEFT),
                        Actions.delay(oneBlockTime * 3),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case PAN_TO_INN:
                _stage.addAction(Actions.sequence(
                        Actions.addAction(Actions.moveTo(49, 27, oneBlockTime * 3), camactor),
                        Actions.delay(oneBlockTime * 0.5f),
                        myActions.new setIdleDirection(character1, Entity.Direction.UP),
                        myActions.new setIdleDirection(character2, Entity.Direction.UP),
                        Actions.delay(oneBlockTime * 1),
                        myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),
                        myActions.new setIdleDirection(character2, Entity.Direction.RIGHT),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case PAN_TO_WOODSHOP:
                _stage.addAction(Actions.sequence(
                        Actions.addAction(Actions.moveTo(60, 27, oneBlockTime * 3), camactor),
                        Actions.delay(oneBlockTime * 3.5f),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case PAN_TO_CHARS:
                _stage.addAction(Actions.sequence(
                        Actions.addAction(Actions.moveTo(37.5f, 20.5f, oneBlockTime * 3), camactor),
                        Actions.delay(oneBlockTime * 0.5f),
                        myActions.new setIdleDirection(character2, Entity.Direction.LEFT),
                        Actions.delay(oneBlockTime * 3),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case CHAR1_LOOK_RIGHT:
                _stage.addAction(Actions.sequence(
                        myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case CHAR2_LOOK_LEFT:
                _stage.addAction(Actions.sequence(
                        myActions.new setIdleDirection(character2, Entity.Direction.LEFT),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case WALK_INTO_ARMORY:
                _stage.addAction(Actions.sequence(
                        new setFading(true),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(26, 24, oneBlockTime), character2),
                        Actions.delay(oneBlockTime),
                        myActions.new setCharacterVisible(character2, false),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(26, 24, oneBlockTime), character1),
                        Actions.delay(oneBlockTime),
                        myActions.new setCharacterVisible(character1, false),

                        Actions.addAction(getArmoryCutScreenAction())
                        )
                );
                _stage.addAction(Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, oneBlockTime * 3), _transitionActor));

                break;
            case JUSTIN_QUESTION:
                _stage.addAction(Actions.sequence(
                        myActions.new setCharacterVisible(misc, true),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.QUESTION_ON),
                        Actions.delay(emoteOn),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.QUESTION_OFF),
                        Actions.delay(emoteOff),
                        myActions.new setCharacterVisible(misc, false),
                        myActions.new setIdleDirection(jaxon, Entity.Direction.LEFT),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case CHAR2_LOOK_DOWN:
                if( shakeCam == null ){
                    shakeCam = new ShakeCamera(_camera.position.x, _camera.position.y,
                            0.20f,
                            0.1f,
                            0.05f,
                            0.025f,
                            0.2f,
                            0.2f,
                            0.2f);
                }

                _stage.addAction(Actions.sequence(
                        Actions.addAction(Actions.moveTo(character2.getX() + emoteX - 0.1f, character2.getY() + emoteY), misc),
                        myActions.new setIdleDirection(character2, Entity.Direction.DOWN),
                        Actions.delay(oneBlockTime),

                        myActions.new setCharacterVisible(misc, true),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.ANGER_ON),
                        myActions.new shakeCam(shakeCam),
                        Actions.delay(emoteOn),
                        myActions.new setIdleDirection(jaxon, Entity.Direction.RIGHT),
                        myActions.new setWalkDirection(jaxon, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveBy(1, 0, emoteOff + oneBlockTime), jaxon),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.ANGER_OFF),
                        Actions.delay(emoteOff),
                        myActions.new setCharacterVisible(misc, false),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(jaxon, Entity.AnimationType.IDLE),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case CHAR2_LOOK_UP:
                _stage.addAction(Actions.sequence(
                        myActions.new setIdleDirection(character2, Entity.Direction.UP),
                        myActions.new setIdleDirection(jaxon, Entity.Direction.RIGHT),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case JAXON_LOOK_DOWN:
                _stage.addAction(Actions.sequence(
                        Actions.addAction(Actions.moveTo(jaxon.getX() + emoteX - 0.1f, jaxon.getY() + emoteY), misc),
                        myActions.new setIdleDirection(jaxon, Entity.Direction.DOWN),
                        Actions.delay(oneBlockTime),

                        myActions.new setCharacterVisible(misc, true),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.SHOCK_ON),
                        Actions.delay(emoteOn),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.SHOCK_OFF),
                        Actions.delay(emoteOff),
                        myActions.new setCharacterVisible(misc, false),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case CHAR2_LOOK_RIGHT:
                _stage.addAction(Actions.sequence(
                        myActions.new setIdleDirection(character2, Entity.Direction.RIGHT),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case CHAR2_LOOK_UP_WAIT:
                _stage.addAction(Actions.sequence(
                        Actions.delay(1),
                        myActions.new setIdleDirection(character2, Entity.Direction.UP),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case JUSTIN_QUESTION2:
                _stage.addAction(Actions.sequence(
                        Actions.addAction(Actions.moveTo(justin.getX() + emoteX - 0.1f, justin.getY() + emoteY), misc),
                        myActions.new setCharacterVisible(misc, true),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.QUESTION_ON),
                        Actions.delay(emoteOn),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.QUESTION_OFF),
                        Actions.delay(emoteOff),
                        myActions.new setCharacterVisible(misc, false),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case START_WALKING:
                _stage.addAction(Actions.sequence(
                        myActions.new setIdleDirection(justin, Entity.Direction.RIGHT),
                        myActions.new setWalkDirection(justin, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveBy(1, 0, oneBlockTime), justin),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(justin, Entity.AnimationType.IDLE),
                        myActions.new setIdleDirection(jaxon, Entity.Direction.LEFT),
                        Actions.delay(oneBlockTime * 0.5f),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case OPEN_ARMORY_DOOR:
                _stage.addAction(Actions.sequence(
                        myActions.new setIdleDirection(jaxon, Entity.Direction.UP),
                        myActions.new setWalkDirection(jaxon, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveTo(7, 7, oneBlockTime * 1.5f), jaxon),
                        Actions.delay(oneBlockTime * 1.5f),
                        myActions.new setWalkDirection(jaxon, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),
                        Actions.addAction(Actions.moveTo(7, 8), misc),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.STEEL_DOOR_OPEN),
                        myActions.new setCharacterVisible(misc, true),
                        Actions.delay((0.25f * 9) + oneBlockTime),
                        myActions.new setIdleDirection(jaxon, Entity.Direction.LEFT),
                        Actions.delay(oneBlockTime),


                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case WALK_TO_DOOR:
                oneBlockTime = 0.5f;
                _stage.addAction(Actions.sequence(
                        myActions.new setIdleDirection(jaxon, Entity.Direction.UP),
                        myActions.new setIdleDirection(justin, Entity.Direction.UP),
                        myActions.new setIdleDirection(character1, Entity.Direction.UP),
                        myActions.new setIdleDirection(character2, Entity.Direction.UP),

                        myActions.new setWalkDirection(justin, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveTo(7, 7, oneBlockTime * 2), justin),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveTo(7, 5.5f, oneBlockTime * 2), character1),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveTo(7, 5.5f, oneBlockTime * 3), character2),

                        Actions.delay(oneBlockTime * 0.25f),
                        myActions.new setCharacterVisible(jaxon, false),
                        Actions.delay(oneBlockTime * 1.75f),

                        Actions.addAction(Actions.moveBy(0, 13, oneBlockTime * 4), camactor),

                        myActions.new setWalkDirection(justin, Entity.AnimationType.IDLE),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(7, 7, oneBlockTime), character1),
                        Actions.delay(oneBlockTime * 0.25f),
                        myActions.new setCharacterVisible(justin, false),
                        Actions.delay(oneBlockTime * 0.75f),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(7, 7, oneBlockTime), character2),
                        Actions.delay(oneBlockTime * 0.25f),
                        myActions.new setCharacterVisible(character1, false),
                        Actions.delay(oneBlockTime * 0.75f),

                        myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),

                        Actions.delay(oneBlockTime * 0.25f),
                        myActions.new setCharacterVisible(character2, false),

                        Actions.delay(oneBlockTime * 2),
                        Actions.addAction(Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 0.1f), _transitionActor)),

                        Actions.addAction(getWeaponsRoom())
                        )
                );

                break;
            case WALK_SWORD:
                shakeCam = null;
                if( shakeCam == null ){
                    shakeCam = new ShakeCamera(3.5f, 6,
                            0.20f,
                            0.1f,
                            0.05f,
                            0.025f,
                            0.2f,
                            0.2f,
                            0.2f);
                }
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(3.5f, 5, oneBlockTime), character1),
                        Actions.addAction(Actions.moveTo(3.5f, 6, oneBlockTime * 0.5f), camactor),
                        new setZoomRate(-0.05f),
                        Actions.delay(oneBlockTime * 0.5f),
                        new setZoomRate(0),
                        Actions.delay(oneBlockTime * 0.25f),
                        myActions.new shakeCam(shakeCam),
                        Actions.delay(oneBlockTime * 0.25f),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),

                        myActions.new setIdleDirection(jaxon, Entity.Direction.LEFT),
                        myActions.new setIdleDirection(justin, Entity.Direction.LEFT),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case WALK_MACE:
                shakeCam = null;
                if( shakeCam == null ){
                    shakeCam = new ShakeCamera(6.5f, 8,
                            0.20f,
                            0.1f,
                            0.05f,
                            0.025f,
                            0.2f,
                            0.2f,
                            0.2f);
                }
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveTo(4, 5, oneBlockTime * 0.5f), character1),
                        Actions.delay(oneBlockTime * 0.5f),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(6.5f, 7, oneBlockTime * 2), character1),
                        Actions.addAction(Actions.moveTo(6.5f, 8, oneBlockTime * 1.5f), camactor),
                        Actions.delay(oneBlockTime * 1.75f),
                        myActions.new shakeCam(shakeCam),
                        Actions.delay(oneBlockTime * 0.25f),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),

                        myActions.new setIdleDirection(jaxon, Entity.Direction.UP),
                        myActions.new setIdleDirection(justin, Entity.Direction.UP),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case WALK_STAFF:
                shakeCam = null;
                if( shakeCam == null ){
                    shakeCam = new ShakeCamera(9.5f, 8,
                            0.20f,
                            0.1f,
                            0.05f,
                            0.025f,
                            0.2f,
                            0.2f,
                            0.2f);
                }
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveTo(9.5f, 7, oneBlockTime * 2), character1),
                        Actions.addAction(Actions.moveTo(9.5f, 8, oneBlockTime * 1.5f), camactor),
                        Actions.delay(oneBlockTime * 1.75f),
                        myActions.new shakeCam(shakeCam),
                        Actions.delay(oneBlockTime * 0.25f),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),

                        Actions.addAction(Actions.moveTo(9, 3, oneBlockTime * 2), character2),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case WALK_DAGGER:
                shakeCam = null;
                if( shakeCam == null ){
                    shakeCam = new ShakeCamera(12.5f, 6,
                            0.20f,
                            0.1f,
                            0.05f,
                            0.025f,
                            0.2f,
                            0.2f,
                            0.2f);
                }
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveTo(12, 5, oneBlockTime * 2), character1),
                        Actions.addAction(Actions.moveTo(12.5f, 6, oneBlockTime * 1.5f), camactor),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new setIdleDirection(jaxon, Entity.Direction.RIGHT),
                        myActions.new setIdleDirection(justin, Entity.Direction.RIGHT),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveTo(12.5f, 5, oneBlockTime * 0.5f), character1),
                        Actions.delay(oneBlockTime * 0.25f),
                        myActions.new shakeCam(shakeCam),
                        Actions.delay(oneBlockTime * 0.25f),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case CHAR1_LOOK_DOWN:
                _stage.addAction(Actions.sequence(
                        myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveTo(12.5f, 4.5f, oneBlockTime * 0.5f), character1),
                        Actions.addAction(Actions.moveTo(12.5f, 4, oneBlockTime), camactor),
                        Actions.delay(oneBlockTime * 0.5f),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime * 0.5f),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case CHAR1_QUESTION:
                _stage.addAction(Actions.sequence(
                        Actions.addAction(Actions.moveTo(character1.getX() + emoteX - 0.1f, character1.getY() + emoteY), misc),
                        myActions.new setCharacterVisible(misc, true),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.QUESTION_ON),
                        Actions.delay(emoteOn),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.QUESTION_OFF),
                        Actions.delay(emoteOff),
                        myActions.new setCharacterVisible(misc, false),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case WALK_TO_BOOK:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_DOWN),
                        myActions.new setWalkDirection(justin, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveTo(12, 3, oneBlockTime * 2), character1),
                        Actions.addAction(Actions.moveTo(12.5f, 4.25f, oneBlockTime * 4), justin),
                        Actions.addAction(Actions.moveTo(11, 5, oneBlockTime * 2), camactor),
                        new setZoomRate(0.003125f),

                        Actions.delay(oneBlockTime * 2),
                        new setZoomRate(0),

                        myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setIdleDirection(justin, Entity.Direction.DOWN),
                        myActions.new setWalkDirection(justin, Entity.AnimationType.IDLE),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case TAKE_BOOK:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),

                        myActions.new setCharacterVisible(misc2, false),
                        Actions.delay(oneBlockTime * 0.5f),
                        myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                        Actions.delay(oneBlockTime * 0.5f),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.BOOK),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case WALK_TO_CHAR2:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.BOOK_CLOSE),
                        Actions.delay(closeBook),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveTo(10, 3, oneBlockTime * 2), character1),
                        Actions.addAction(Actions.moveBy(-1, -1.5f, oneBlockTime * 2), camactor),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new setIdleDirection(character1, Entity.Direction.LEFT),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case ZOOM_IN:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(character2, Entity.AnimationType.RUN_RIGHT),
                        Actions.addAction(Actions.moveBy(0.5f, 0, oneBlockTime * 0.5f), character2),
                        new setZoomRate(-0.05f),
                        Actions.delay(oneBlockTime * 0.25f),
                        new setZoomRate(0),
                        Actions.delay(oneBlockTime * 0.25f),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case ZOOM_OUT:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveBy(-0.5f, 0, oneBlockTime), character2),
                        new setZoomRate(0.025f),
                        Actions.delay(oneBlockTime * 0.5f),
                        new setZoomRate(0),
                        Actions.delay(oneBlockTime * 0.5f),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case WALK_OUT_OF_WAY:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(jaxon, Entity.AnimationType.WALK_LEFT),
                        myActions.new setWalkDirection(justin, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveBy(-1, 0, oneBlockTime), jaxon),
                        Actions.addAction(Actions.moveBy(1, 0, oneBlockTime), justin),
                        Actions.delay(oneBlockTime),

                        myActions.new setIdleDirection(jaxon, Entity.Direction.LEFT),
                        myActions.new setIdleDirection(justin, Entity.Direction.RIGHT),
                        myActions.new setWalkDirection(jaxon, Entity.AnimationType.IDLE),
                        myActions.new setWalkDirection(justin, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(jaxon, Entity.Direction.RIGHT),
                        myActions.new setIdleDirection(justin, Entity.Direction.LEFT),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case WALK_INTO_INN:
                _stage.addAction(Actions.sequence(
                        new setFading(true),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(49, 24, oneBlockTime), character2),
                        Actions.delay(oneBlockTime),
                        myActions.new setCharacterVisible(character2, false),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(49, 24, oneBlockTime), character1),
                        Actions.delay(oneBlockTime),
                        myActions.new setCharacterVisible(character1, false),

                        Actions.addAction(getInnScene())
                        )
                );
                _stage.addAction(Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, oneBlockTime * 2), _transitionActor));

                break;
            case DIANE_WALK:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(diane, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(4, 8.5f, oneBlockTime * 2), diane),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setIdleDirection(diane, Entity.Direction.UP),
                        myActions.new setWalkDirection(diane, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setWalkDirection(diane, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveTo(4, 8, oneBlockTime), diane),
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(diane, Entity.Direction.RIGHT),
                        myActions.new setWalkDirection(diane, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setIdleDirection(diane, Entity.Direction.DOWN),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case DIANE_WALK_BACK:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(diane, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveTo(3, 8, oneBlockTime), diane),
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(diane, Entity.Direction.LEFT),
                        myActions.new setWalkDirection(diane, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setWalkDirection(diane, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveTo(4, 7.1f, oneBlockTime * 1.5f), diane),
                        Actions.delay(oneBlockTime * 1.5f),
                        myActions.new setIdleDirection(diane, Entity.Direction.DOWN),
                        myActions.new setWalkDirection(diane, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case START_LEAVE_INN:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveTo(4, 3, oneBlockTime * 3), character2),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                        Actions.delay(oneBlockTime),

                        myActions.new setIdleDirection(character2, Entity.Direction.DOWN),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case CHAR2_LOOK_UP_2:
                _stage.addAction(Actions.sequence(
                        myActions.new setIdleDirection(character2, Entity.Direction.UP),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case CHAR1_STUTTER:
                _stage.addAction(Actions.sequence(
                        myActions.new setCharacterVisible(misc, true),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.SHOCK_ON),

                        Actions.addAction(Actions.moveBy(0.05f, 0, 0.0025f), character1),
                        Actions.delay(0.0025f),
                        Actions.addAction(Actions.moveBy(-0.10f, 0, 0.005f), character1),
                        Actions.delay(0.005f),
                        Actions.addAction(Actions.moveBy(0.05f, 0, 0.0025f), character1),
                        Actions.delay(0.5f),

                        myActions.new setWalkDirection(misc, Entity.AnimationType.SHOCK_OFF),
                        Actions.delay(emoteOff),
                        myActions.new setCharacterVisible(misc, false),


                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case CHAR1_THINK:
                _stage.addAction(Actions.sequence(
                        myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                        Actions.delay(oneBlockTime),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.THINK),

                        myActions.new setCharacterVisible(misc, true),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.THINK_ON),
                        Actions.delay(0.24f),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.THINK_LOOP),
                        Actions.delay(2.1f),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.THINK_OFF),
                        Actions.delay(0.075f),
                        myActions.new setCharacterVisible(misc, false),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;







            case WALK_OUT_OF_INN:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveTo(4, 1, oneBlockTime * 2), character2),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveTo(4, 1, oneBlockTime * 5), character1),
                        Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, oneBlockTime * 5), _transitionActor),
                        Actions.delay(oneBlockTime),
                        myActions.new setCharacterVisible(character2, false),
                        Actions.delay(oneBlockTime * 4),
                        //todo qweqerwutiotyretwqrykuthrarhsmdshagassysdsyaesjdywardgmtysrtunertu6usr6ks

                        Actions.addAction(_switchScreenToMainAction)
                        )
                );

                break;
            case WALK_OUT_OF_ARMORY:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_DOWN),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveBy(0, -5, 2), character1),
                        Actions.addAction(Actions.moveBy(0, -5, 2), character2),
                        Actions.delay(oneBlockTime),
                        new setFading(true),
                        Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 1), _transitionActor),
                        //todo qweqerwutiotyretwqrykuthrarhsmdshagassysdsyaesjdywardgmtysrtunertu6usr6ks
                        Actions.addAction(_switchScreenToMainAction)
                        )
                );

                break;
            case EXIT_CONVERSATION_2:
                _stage.addAction(Actions.sequence(
                        new setFading(true),
                        Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 1), _transitionActor),
                        //todo qweqerwutiotyretwqrykuthrarhsmdshagassysdsyaesjdywardgmtysrtunertu6usr6ks
                        Actions.addAction(_switchScreenToMainAction)
                        )
                );

                break;
            case EXIT_CONVERSATION_1:
                _stage.addAction(Actions.sequence(
                        new setFading(true),
                        Actions.addAction(Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 1), _transitionActor)),
                        Actions.delay(1),
                        Actions.addAction(_switchScreenToMainAction)
                        )
                );        }

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
                new setFading(true),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 3), _transitionActor),
                Actions.delay(2),
                Actions.run(
                        new Runnable() {
                            @Override
                            public void run() {
                                _playerHUD.loadConversationForCutScene("RPGGame/maps/Game/Text/Dialog/Chapter_2_P1.json", thisScreen);
                                _playerHUD.doConversation();
                                // NOTE: This just kicks off the conversation. The actions in the conversation are handled in the onNotify() function.
                            }
                        }),
                Actions.delay(3)
        );
    }

    private Action getOutsideArmoryScene() {
        setupOutsideArmoryScene.reset();
        return Actions.sequence(
                Actions.addAction(setupOutsideArmoryScene),
                new setFading(true),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 0.5f), _transitionActor),
                Actions.delay(0.5f),
                new setFading(false),

                // uncomment to start right from guard surround scene
                // also need to change currentConversationID in the json file to n0
                myActions.new loadConversation(_playerHUD, "RPGGame/maps/Game/Text/Dialog/Chapter_2_P2.json", thisScreen),

                myActions.new continueConversation(_playerHUD)
        );
    }

    private Action getArmoryCutScreenAction() {
        setupSceneArmory.reset();
        return Actions.sequence(
                Actions.addAction(setupSceneArmory),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 1), _transitionActor),
                myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_UP),
                myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_UP),
                Actions.addAction(Actions.moveTo(3.5f, 5.5f, oneBlockTime * 5), character2),
                Actions.delay(oneBlockTime * 2.5f),
                myActions.new setIdleDirection(jaxon, Entity.Direction.RIGHT),

                myActions.new setCharacterVisible(character1, true),
                Actions.addAction(Actions.moveTo(4.5f, 5.5f, oneBlockTime * 5), character1),
                Actions.delay(oneBlockTime * 2.5f),
                myActions.new setIdleDirection(jaxon, Entity.Direction.LEFT),

                // uncomment to start right from guard surround scene
                // also need to change currentConversationID in the json file to n18
                //myActions.new loadConversation(_playerHUD, "RPGGame/maps/Game/Text/Dialog/Chapter_2_P2.json", thisScreen),

                myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),
                myActions.new continueConversation(_playerHUD),

                Actions.delay(oneBlockTime * 2.5f),
                myActions.new setIdleDirection(jaxon, Entity.Direction.UP),

                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE)
                //Actions.delay(3f),
                //Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 2), _transitionActor)

        );
    }

    private Action getWeaponsRoom() {
        setupSceneWeaponsRoom.reset();
        return Actions.sequence(
                Actions.addAction(setupSceneWeaponsRoom),
                Actions.addAction(Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 0.1f), _transitionActor)),
                Actions.addAction(Actions.moveBy(0, 12, oneBlockTime * 48 / 9.5f), camactor),

                Actions.delay(oneBlockTime * 3),

                myActions.new setCharacterVisible(character1, true),
                Actions.delay(oneBlockTime * 0.5f),
                myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_LEFT),
                Actions.addAction(Actions.moveTo(3.5f, 4, oneBlockTime * 3), character1),
                Actions.delay(oneBlockTime * 2),

                myActions.new setCharacterVisible(character2, true),
                Actions.delay(oneBlockTime * 0.5f),
                myActions.new setIdleDirection(character2, Entity.Direction.LEFT),
                Actions.delay(oneBlockTime * 0.5f),

                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                Actions.delay(oneBlockTime * 0.5f),
                myActions.new setIdleDirection(character1, Entity.Direction.UP),

                Actions.delay(oneBlockTime * 1.5f),
                myActions.new setIdleDirection(character2, Entity.Direction.RIGHT),

                myActions.new continueConversation(_playerHUD)
        );
    }

    private Action getSwordScene() {
        setupGetWeapon.reset();
        return Actions.sequence(
                Actions.addAction(setupGetWeapon),
                new setFading(true),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 0.5f), _transitionActor),
                Actions.delay(0.5f),
                new setFading(false),

                // uncomment to start right from guard surround scene
                // also need to change currentConversationID in the json file to n0
                myActions.new loadConversation(_playerHUD, "RPGGame/maps/Game/Text/Dialog/Chapter_2_Sword.json", thisScreen),

                myActions.new continueConversation(_playerHUD)
        );
    }

    private Action getMaceScene() {
        setupGetWeapon.reset();
        return Actions.sequence(
                Actions.addAction(setupGetWeapon),
                new setFading(true),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 0.5f), _transitionActor),
                Actions.delay(0.5f),
                new setFading(false),

                // uncomment to start right from guard surround scene
                // also need to change currentConversationID in the json file to n0
                myActions.new loadConversation(_playerHUD, "RPGGame/maps/Game/Text/Dialog/Chapter_2_Mace.json", thisScreen),

                myActions.new continueConversation(_playerHUD)
        );
    }

    private Action getStaffScene() {
        setupGetWeapon.reset();
        return Actions.sequence(
                Actions.addAction(setupGetWeapon),
                new setFading(true),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 0.5f), _transitionActor),
                Actions.delay(0.5f),
                new setFading(false),

                // uncomment to start right from guard surround scene
                // also need to change currentConversationID in the json file to n0
                myActions.new loadConversation(_playerHUD, "RPGGame/maps/Game/Text/Dialog/Chapter_2_Staff.json", thisScreen),

                myActions.new continueConversation(_playerHUD)
        );
    }

    private Action getDaggerScene() {
        setupGetWeapon.reset();
        return Actions.sequence(
                Actions.addAction(setupGetWeapon),
                new setFading(true),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 0.5f), _transitionActor),
                Actions.delay(0.5f),
                new setFading(false),

                // uncomment to start right from guard surround scene
                // also need to change currentConversationID in the json file to n0
                myActions.new loadConversation(_playerHUD, "RPGGame/maps/Game/Text/Dialog/Chapter_2_Dagger.json", thisScreen),

                myActions.new continueConversation(_playerHUD)
        );
    }

    private Action getOutsideInnScene() {
        setupOutsideInnScene.reset();
        oneBlockTime = 0.3f;
        return Actions.sequence(
                Actions.addAction(setupOutsideInnScene),
                new setFading(true),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 0.5f), _transitionActor),
                Actions.delay(0.5f),
                new setFading(false),

                // uncomment to start right from guard surround scene
                // also need to change currentConversationID in the json file to n0
                myActions.new loadConversation(_playerHUD, "RPGGame/maps/Game/Text/Dialog/Chapter_2_P4.json", thisScreen),

                myActions.new continueConversation(_playerHUD)
        );
    }

    private Action getInnScene() {
        setupInnScene.reset();
        return Actions.sequence(
                new setMapRendering(false),
                Actions.addAction(setupInnScene),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 1), _transitionActor),
                new setMapRendering(true),

                myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_UP),
                myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_UP),
                Actions.addAction(Actions.moveTo(3.5f, 5.5f, oneBlockTime * 5), character2),
                Actions.delay(oneBlockTime * 2.5f),

                myActions.new setCharacterVisible(character1, true),
                Actions.addAction(Actions.moveTo(4.5f, 5.5f, oneBlockTime * 5), character1),
                Actions.delay(oneBlockTime * 2.5f),

                myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),

                Actions.delay((oneBlockTime * 2.5f) - 0.75f),

                myActions.new setCharacterVisible(misc, true),
                myActions.new setWalkDirection(misc, Entity.AnimationType.HAPPY_ON),
                Actions.delay(emoteOn),
                myActions.new setWalkDirection(misc, Entity.AnimationType.HAPPY_OFF),
                Actions.delay(emoteOff),
                myActions.new setCharacterVisible(misc, false),

                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),

                myActions.new continueConversation(_playerHUD)
        );
    }

    private Action getOutsideWoodshopScene() {
        setupOutsideWoodshopScene.reset();
        oneBlockTime = 0.3f;
        return Actions.sequence(
                Actions.addAction(setupOutsideWoodshopScene),
                new setFading(true),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 0.5f), _transitionActor),
                Actions.delay(0.5f),
                new setFading(false),

                // uncomment to start right from guard surround scene
                // also need to change currentConversationID in the json file to n0
                myActions.new loadConversation(_playerHUD, "RPGGame/maps/Game/Text/Dialog/Chapter_2_P3.json", thisScreen),

                myActions.new continueConversation(_playerHUD)
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
        _stage.addAction(getOpeningCutSceneAction());
        //P2
        //_stage.addAction(getOutsideArmoryScene());
        //_stage.addAction(getArmoryCutScreenAction());
        //_stage.addAction(getSwordScene());
        //P3
        //_stage.addAction(getOutsideWoodshopScene());
        //P4
        //_stage.addAction(getOutsideInnScene());

        ProfileManager.getInstance().addObserver(_mapMgr);
        _playerHUD.setCutScene(true);
        //if (_playerHUD != null)
        //    ProfileManager.getInstance().addObserver(_playerHUD);

        setGameState(GameState.LOADING);

        //Gdx.input.setInputProcessor(_multiplexer);

        if( _mapRenderer == null ){
            ProfileManager.getInstance().setProperty("currentMapType", MapFactory.MapType.ELMOUR.toString());
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
        baseRender(delta);
        //todo: did he fix it?
        /*Gdx.gl.glClearColor(0, 0, 0, 1);
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

        _playerHUD.render(delta);*/

    }

    @Override
    public void resize(int width, int height) {
        setupViewport(V_WIDTH, V_HEIGHT);
        _camera.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);
        _camera.position.set(lastCameraPosition);

        if (_playerHUD != null)
            _playerHUD.resize((int) VIEWPORT.physicalWidth, (int) VIEWPORT.physicalHeight);
    }

    @Override
    public void pause() {
        lastCameraPosition = _camera.position.cpy();

        if (_playerHUD != null)
            _playerHUD.pause();
    }

    @Override
    public void resume() {
        _camera.position.set(lastCameraPosition);

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
