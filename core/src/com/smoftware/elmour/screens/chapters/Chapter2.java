package com.smoftware.elmour.screens.chapters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.smoftware.elmour.ElmourGame;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.EntityFactory;
import com.smoftware.elmour.UI.graphics.AnimatedImage;
import com.smoftware.elmour.UI.huds.PlayerHUD;
import com.smoftware.elmour.UI.dialog.ConversationChoice;
import com.smoftware.elmour.UI.dialog.ConversationGraph;
import com.smoftware.elmour.UI.dialog.ConversationGraphObserver;
import com.smoftware.elmour.maps.MapFactory;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.screens.CutSceneBase;
import com.smoftware.elmour.screens.CutSceneObserver;
import com.smoftware.elmour.sfx.ScreenTransitionAction;
import com.smoftware.elmour.sfx.ShakeCamera;

import java.util.ArrayList;

public class Chapter2 extends CutSceneBase implements ConversationGraphObserver {
    private static final String TAG = Chapter2.class.getSimpleName();

    Chapter2 thisScreen;

    private Action setupScene01;
    private Action setupOutsideArmoryScene;
    private Action setupSceneArmory;
    private Action setupSceneWeaponsRoom;
    private Action setupGetWeapon;
    private Action setupOutsideInnScene;
    private Action setupInnScene;
    private Action setupWoodshopScene;
    private Action setupScene05;
    private Action setupLeave;

    private AnimatedImage character1;
    private AnimatedImage character2;
    private AnimatedImage justin;
    private AnimatedImage jaxon;
    private AnimatedImage diane;
    private AnimatedImage ophion;

    private AnimatedImage camactor;
    private AnimatedImage misc;
    private AnimatedImage misc2;

    public Chapter2(ElmourGame game, PlayerHUD playerHUD) {
        super(game, playerHUD);
        thisScreen = this;
        currentPartNumber = "";

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

                misc.setVisible(false);
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
                setCameraPosition(4.5f, 6);
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

                misc.setVisible(false);
                misc.setPosition(diane.getX() + emoteX, diane.getY() + emoteY);
                misc.setCurrentAnimationType(Entity.AnimationType.SHOCK_OFF);
            }
        };

        setupWoodshopScene = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.ELMOUR);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(60, 24);
                keepCamInMap = false;

                camactor.setPosition(60, 24);
                camactor.setCurrentAnimationType(Entity.AnimationType.IDLE);
                camactor.setCurrentDirection(Entity.Direction.DOWN);

                character1.setVisible(true);
                character1.setPosition(59.5f, 23.5f);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.RIGHT);

                character2.setVisible(true);
                character2.setPosition(60.5f, 23.5f);
                character2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character2.setCurrentDirection(Entity.Direction.LEFT);

                ophion.setVisible(false);
                ophion.setPosition(60, 28.8f);
                ophion.setCurrentAnimationType(Entity.AnimationType.IDLE);
                ophion.setCurrentDirection(Entity.Direction.DOWN);

                misc.setPosition(character1.getX() + emoteX, character1.getY() + emoteY);
                misc.setCurrentAnimationType(Entity.AnimationType.SHOCK_OFF);

                diane.setVisible(false);
                justin.setVisible(false);
                jaxon.setVisible(false);

                followActor(camactor);
            }
        };

        setupScene05 = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.ELMOUR);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(37.5f, 30);
                keepCamInMap = false;

                camactor.setPosition(37.5f, 30);
                camactor.setCurrentAnimationType(Entity.AnimationType.IDLE);
                camactor.setCurrentDirection(Entity.Direction.DOWN);

                character2.setVisible(true);
                character2.setPosition(38, 27);
                character2.setCurrentAnimationType(Entity.AnimationType.WALK_UP);
                character2.setCurrentDirection(Entity.Direction.UP);

                character1.setVisible(true);
                character1.setPosition(37, 27);
                character1.setCurrentAnimationType(Entity.AnimationType.WALK_UP);
                character1.setCurrentDirection(Entity.Direction.UP);

                misc.setCurrentAnimationType(Entity.AnimationType.SHOCK_OFF);

                ophion.setVisible(false);
                diane.setVisible(false);
                justin.setVisible(false);
                jaxon.setVisible(false);

                followActor(camactor);
            }
        };

        setupLeave = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.ELMOUR);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(10, 20.5f);
                keepCamInMap = true;

                character2.setVisible(true);
                character2.setPosition(3, 20.5f);
                character2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character2.setCurrentDirection(Entity.Direction.RIGHT);

                character1.setVisible(true);
                character1.setPosition(4, 20.5f);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.LEFT);

                ophion.setVisible(false);
                diane.setVisible(false);
                justin.setVisible(false);
                jaxon.setVisible(false);

                followActor(character1);
            }
        };
    }

    boolean isDoneWithExploring() {
        return ((ProfileManager.getInstance().getProperty(ElmourGame.ScreenType.Chapter2Screen.toString() + "_P2",
                                        CutSceneObserver.CutSceneStatus.class)) == CutSceneObserver.CutSceneStatus.DONE &&
                (ProfileManager.getInstance().getProperty(ElmourGame.ScreenType.Chapter2Screen.toString() + "_P3",
                                        CutSceneObserver.CutSceneStatus.class)) == CutSceneObserver.CutSceneStatus.DONE &&
                (ProfileManager.getInstance().getProperty(ElmourGame.ScreenType.Chapter2Screen.toString() + "_P4",
                                        CutSceneObserver.CutSceneStatus.class)) == CutSceneObserver.CutSceneStatus.DONE);
    }

    @Override
    public void onNotify(ConversationGraph graph, ConversationCommandEvent action, String data) {
        Gdx.app.log(TAG, "Got notification " + action.toString());
        String conversationId = data;
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
                        Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, oneBlockTime * 3), _transitionActor),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(26, 24, oneBlockTime), character2),
                        Actions.delay(oneBlockTime),
                        myActions.new setCharacterVisible(character2, false),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(26, 24, oneBlockTime), character1),
                        Actions.delay(oneBlockTime),
                        myActions.new setCharacterVisible(character1, false),
                        Actions.delay(oneBlockTime),

                        Actions.addAction(getArmoryCutScreenAction())
                        )
                );

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
                        Actions.addAction(Actions.moveTo(3.5f, 5.5f, oneBlockTime), character1),
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
                        Actions.addAction(Actions.moveTo(4, 5.5f, oneBlockTime * 0.5f), character1),
                        Actions.delay(oneBlockTime * 0.5f),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(6.5f, 7.5f, oneBlockTime * 2), character1),
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
                        Actions.addAction(Actions.moveTo(9.5f, 7.5f, oneBlockTime * 2), character1),
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
                        Actions.addAction(Actions.moveTo(12, 5.5f, oneBlockTime * 2), character1),
                        Actions.addAction(Actions.moveTo(12.5f, 6, oneBlockTime * 1.5f), camactor),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new setIdleDirection(jaxon, Entity.Direction.RIGHT),
                        myActions.new setIdleDirection(justin, Entity.Direction.RIGHT),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveTo(12.5f, 5.5f, oneBlockTime * 0.5f), character1),
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
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case WALK_TO_WOODSHOP:
                _stage.addAction(Actions.sequence(
                        myActions.new setCharacterVisible(ophion, true),
                        myActions.new setWalkDirection(ophion, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveBy(0, -1, oneBlockTime * 6), ophion),

                        Actions.addAction(Actions.moveTo(60, 26, oneBlockTime * 4), camactor),

                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(60.5f, 26.5f, oneBlockTime * 4), character2),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(59.5f, 26.5f, oneBlockTime * 4), character1),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new setIdleDirection(character2, Entity.Direction.UP),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setIdleDirection(character1, Entity.Direction.UP),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                        myActions.new setIdleDirection(ophion, Entity.Direction.DOWN),
                        myActions.new setWalkDirection(ophion, Entity.AnimationType.IDLE),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case GET_BACKPACKS:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(ophion, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveBy(1, 0, oneBlockTime * 6), ophion),
                        Actions.delay(oneBlockTime * 6),
                        myActions.new setIdleDirection(ophion, Entity.Direction.DOWN),
                        myActions.new setWalkDirection(ophion, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime * 3),

                        myActions.new setWalkDirection(ophion, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveBy(-1, 0, oneBlockTime * 6), ophion),
                        Actions.delay(oneBlockTime * 6),
                        myActions.new setIdleDirection(ophion, Entity.Direction.DOWN),
                        myActions.new setWalkDirection(ophion, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime * 3),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case GET_FIREWOOD:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(ophion, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveBy(0, 1, oneBlockTime * 6), ophion),
                        Actions.delay(oneBlockTime * 9),

                        myActions.new setWalkDirection(ophion, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveBy(0, -1, oneBlockTime * 6), ophion),
                        Actions.delay(oneBlockTime * 6),
                        myActions.new setIdleDirection(ophion, Entity.Direction.DOWN),
                        myActions.new setWalkDirection(ophion, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime * 3),

                        Actions.addAction(Actions.moveTo(character2.getX() + emoteX + 0.5f, character2.getY() + emoteY), misc),
                        myActions.new setCharacterVisible(misc, true),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.THINK_ON),
                        Actions.delay(0.24f),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.THINK_LOOP),
                        Actions.delay(1.1f),
                        myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),
                        Actions.delay(1),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.THINK_OFF),
                        Actions.delay(0.075f),
                        myActions.new setCharacterVisible(misc, false),
                        Actions.delay(oneBlockTime),

                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveBy(0, -5, oneBlockTime * 5), character2),
                        Actions.addAction(Actions.moveBy(0, -5, oneBlockTime * 9), camactor),

                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveBy(0, -5, oneBlockTime * 6), character1),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setIdleDirection(character2, Entity.Direction.DOWN),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setIdleDirection(character2, Entity.Direction.LEFT),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case CHAR2_LOOK_DOWN_2:
                _stage.addAction(Actions.sequence(
                        myActions.new setIdleDirection(character2, Entity.Direction.DOWN),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case CHAR2_TO_CASTLE:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveBy(-0.5f, 5, oneBlockTime * 5), character2),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveBy(0.5f, 0, oneBlockTime), character1),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),

                        Actions.delay(oneBlockTime * 2),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case CHAR1_LOOK_AT_BOOK:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime * 0.5f),
                        myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.BOOK),
                        Actions.delay(oneBlockTime * 3),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case CHAR2_RETURNS:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveBy(0, -5, oneBlockTime * 5), character2),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.BOOK_CLOSE),
                        Actions.delay(closeBook),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),

                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(character1, Entity.Direction.UP),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;








            case EXIT_CONVERSATION_6:
                if (currentPartNumber.equals("P4")) {
                    ProfileManager.getInstance().setProperty(ElmourGame.ScreenType.Chapter2Screen.toString() + "_P4", CutSceneObserver.CutSceneStatus.DONE);
                }

                if (isDoneWithExploring()) {
                    _stage.addAction(getSetupScene05());
                }
                else {
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

                            Actions.addAction(_switchScreenToMainAction)
                            )
                    );
                }

                break;
            case EXIT_CONVERSATION_5:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_DOWN),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveBy(0, -5, 2), character1),
                        Actions.addAction(Actions.moveBy(0, -5, 2), character2),
                        Actions.delay(oneBlockTime),

                        new setFading(true),
                        Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 1), _transitionActor),
                        Actions.delay(1),
                        Actions.addAction(_switchScreenToMainAction)
                        )
                );

                break;
            case EXIT_CONVERSATION_2:
                if (currentPartNumber.equals("P2")) {
                    ProfileManager.getInstance().setProperty(ElmourGame.ScreenType.Chapter2Screen.toString() + "_P2", CutSceneObserver.CutSceneStatus.DONE);
                }
                else if (currentPartNumber.equals("P3")) {
                    ProfileManager.getInstance().setProperty(ElmourGame.ScreenType.Chapter2Screen.toString() + "_P3", CutSceneObserver.CutSceneStatus.DONE);
                }

                if (isDoneWithExploring()) {
                    _stage.addAction(getSetupScene05());
                }
                else {
                    _stage.addAction(Actions.sequence(
                            new setFading(true),
                            Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 1), _transitionActor),
                            Actions.delay(1),
                            Actions.addAction(_switchScreenToMainAction)
                            )
                    );
                }

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
                new setMapRendering(false),
                Actions.addAction(setupSceneArmory),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 1), _transitionActor),
                new setMapRendering(true),
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

                new setZoomRate(-0.07f),
                Actions.delay(0.01f),
                new setZoomRate(0),

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

                new setZoomRate(-0.07f),
                Actions.delay(0.01f),
                new setZoomRate(0),

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

                new setZoomRate(-0.07f),
                Actions.delay(0.01f),
                new setZoomRate(0),

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

                new setZoomRate(-0.07f),
                Actions.delay(0.01f),
                new setZoomRate(0),

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

    private Action getWoodshopScene() {
        setupWoodshopScene.reset();
        oneBlockTime = 0.5f;
        return Actions.sequence(
                Actions.addAction(setupWoodshopScene),
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

    private Action getSetupScene05() {
        setupScene05.reset();
        oneBlockTime = 0.4f;
        return Actions.sequence(
                Actions.addAction(setupScene05),
                new setFading(true),
                Actions.addAction(Actions.moveBy(0, 7, oneBlockTime * 7), character2),
                Actions.addAction(Actions.moveBy(0, 5, oneBlockTime * 5), character1),
                Actions.addAction(Actions.moveBy(0, 2, oneBlockTime * 6), camactor),

                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 0.5f), _transitionActor),
                Actions.delay(oneBlockTime * 5),
                new setFading(false),

                // uncomment to start right from guard surround scene
                // also need to change currentConversationID in the json file to n0
                myActions.new loadConversation(_playerHUD, "RPGGame/maps/Game/Text/Dialog/Chapter_2_P5.json", thisScreen),

                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                Actions.delay(oneBlockTime * 2),
                myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),

                myActions.new continueConversation(_playerHUD)
        );
    }

    private Action getLeave() {
        setupLeave.reset();
        return Actions.sequence(
                Actions.addAction(setupLeave),
                new setFading(true),

                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 0.5f), _transitionActor),
                Actions.delay(0.5f),
                new setFading(false),

                // uncomment to start right from guard surround scene
                // also need to change currentConversationID in the json file to n0
                myActions.new loadConversation(_playerHUD, "RPGGame/maps/Game/Text/Dialog/Chapter_2_Leave.json", thisScreen),

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
        super.baseShow();

        currentPartNumber = ProfileManager.getInstance().getProperty(ElmourGame.ScreenType.Chapter2Screen.toString(), String.class);

        if (currentPartNumber == null || currentPartNumber.equals("")) {
            _stage.addAction(getOpeningCutSceneAction());
        }
        else if (currentPartNumber.equals("P2")) {
            _stage.addAction(getOutsideArmoryScene());
        }
        else if (currentPartNumber.equals("P3")) {
            _stage.addAction(getWoodshopScene());
        }
        else if (currentPartNumber.equals("P4")) {
            _stage.addAction(getOutsideInnScene());
        }
        else if (currentPartNumber.equals("Leave")) {
            _stage.addAction(getLeave());
        }
        else if (currentPartNumber.equals("Sword")) {
            _stage.addAction(getSwordScene());
        }
        else if (currentPartNumber.equals("Mace")) {
            _stage.addAction(getMaceScene());
        }
        else if (currentPartNumber.equals("Staff")) {
            _stage.addAction(getStaffScene());
        }
        else if (currentPartNumber.equals("Dagger")) {
            _stage.addAction(getDaggerScene());
        }
        else if (currentPartNumber.equals("P5")) {
            _stage.addAction(getSetupScene05());
        }

        // This will be a goal
        //P5
        //_stage.addAction(getSetupScene05());

        baseShow();

        /*
        if( _mapRenderer == null ){
            ProfileManager.getInstance().setProperty("currentMapType", MapFactory.MapType.ELMOUR.toString()); //todo: is this necessary?
            _mapRenderer = new OrthogonalTiledMapRenderer(_mapMgr.getCurrentTiledMap(), Map.UNIT_SCALE);
        }
        */
    }

    @Override
    public void hide() {
        ProfileManager.getInstance().setProperty(ElmourGame.ScreenType.Chapter2Screen.toString(), "");
        baseHide();
    }

    @Override
    public void render(float delta) {
        baseRender(delta);
    }

    @Override
    public void resize(int width, int height) {
        baseResize(width, height);
    }

    @Override
    public void pause() {
        basePause();
    }

    @Override
    public void resume() {
        baseResume();
    }

    @Override
    public void dispose() {
        baseDispose();
    }
}
