package com.smoftware.elmour.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.smoftware.elmour.ElmourGame;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.EntityFactory;
import com.smoftware.elmour.UI.AnimatedImage;
import com.smoftware.elmour.audio.AudioManager;
import com.smoftware.elmour.dialog.ConversationChoice;
import com.smoftware.elmour.dialog.ConversationGraph;
import com.smoftware.elmour.dialog.ConversationGraphObserver;
import com.smoftware.elmour.dialog.InputDialogObserver;
import com.smoftware.elmour.maps.Map;
import com.smoftware.elmour.maps.MapFactory;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.sfx.ScreenTransitionAction;
import com.smoftware.elmour.sfx.ShakeCamera;

import java.util.ArrayList;

public class CutSceneChapter1 extends CutSceneBase implements ConversationGraphObserver, InputDialogObserver {
    private static final String TAG = CutSceneChapter1.class.getSimpleName();

    CutSceneChapter1 thisScreen;

    private Action setupScene01;
    private Action setupCastleChaseScene;
    private Action setupCourtyardChaseScene;
    private Action setupGuardsSurroundScene;
    private Action setupWakeUpScene;
    private Action setupPortalRoomScene;

    private AnimatedImage character1;
    private AnimatedImage character2;
    private AnimatedImage guard1;
    private AnimatedImage guard2;
    private AnimatedImage guard3;
    private AnimatedImage guard4;
    private AnimatedImage guard5;
    private AnimatedImage guard6;
    private AnimatedImage guard7;
    private AnimatedImage guard8;
    private AnimatedImage camactor;
    private AnimatedImage misc;

    private Image blackBarLeft;
    private Image blackBarRight;

    public CutSceneChapter1(ElmourGame game){
        super(game);
        thisScreen = this;

        _playerHUD.addObserver(this);

        character1 = getAnimatedImage(EntityFactory.EntityName.CHARACTER_1);
        character2 = getAnimatedImage(EntityFactory.EntityName.CHARACTER_2);
        guard1 = getAnimatedImage(EntityFactory.EntityName.ROYAL_GUARD);
        guard2 = getAnimatedImage(EntityFactory.EntityName.ROYAL_GUARD);
        guard3 = getAnimatedImage(EntityFactory.EntityName.ROYAL_GUARD);
        guard4 = getAnimatedImage(EntityFactory.EntityName.ROYAL_GUARD);
        guard5 = getAnimatedImage(EntityFactory.EntityName.ROYAL_GUARD);
        guard6 = getAnimatedImage(EntityFactory.EntityName.ROYAL_GUARD);
        guard7 = getAnimatedImage(EntityFactory.EntityName.ROYAL_GUARD);
        guard8 = getAnimatedImage(EntityFactory.EntityName.ROYAL_GUARD);
        camactor = getAnimatedImage(EntityFactory.EntityName.STEVE);
        misc = getAnimatedImage(EntityFactory.EntityName.MISC_ANIMATIONS);

        character2.setVisible(false);
        camactor.setVisible(false);
        guard1.setVisible(false);
        guard2.setVisible(false);
        guard3.setVisible(false);
        guard4.setVisible(false);
        guard5.setVisible(false);
        guard6.setVisible(false);
        guard7.setVisible(false);
        guard8.setVisible(false);
        misc.setVisible(false);
        misc.setCurrentAnimationType(Entity.AnimationType.FORCEFIELD);

        blackBarLeft = new Image(new Texture("graphics/black_rectangle.png"));
        blackBarLeft.setVisible(false);
        blackBarRight = new Image(new Texture("graphics/black_rectangle.png"));
        blackBarRight.setVisible(false);

        _followingActor.setPosition(0, 0);

        _stage.addActor(character1);
        _stage.addActor(character2);
        _stage.addActor(guard1);
        _stage.addActor(guard2);
        _stage.addActor(guard3);
        _stage.addActor(guard4);
        _stage.addActor(guard5);
        _stage.addActor(guard6);
        _stage.addActor(guard7);
        _stage.addActor(guard8);

        _stage.addActor(camactor);
        _stage.addActor(misc);
        _stage.addActor(blackBarLeft);
        _stage.addActor(blackBarRight);
        _stage.addActor(_transitionActor);

        setupScene01 = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.PORTAL_ROOM);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(5, 4.25f);

                character2.setPosition(7, 10);
                character2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character2.setCurrentDirection(Entity.Direction.LEFT);

                character1.setVisible(true);
                character1.setPosition(5.5f, 1.5f);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.DOWN);

                misc.setPosition(character1.getX() + emoteX, character1.getY() + emoteY);
                misc.setCurrentAnimationType(Entity.AnimationType.SHOCK_OFF);
            }
        };

        setupCastleChaseScene = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.CASTLE);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(10, 10.5f);

                character1.setVisible(true);
                character1.setPosition(-1, 10.5f);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.RIGHT);

                character2.setPosition(-1, 10.5f);
                character2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character2.setCurrentDirection(Entity.Direction.RIGHT);

                followActor(character1);
            }
        };

        setupCourtyardChaseScene = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.COURTYARD);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(16, 9);

                camactor.setPosition(16, 9);
                camactor.setCurrentAnimationType(Entity.AnimationType.IDLE);
                camactor.setCurrentDirection(Entity.Direction.DOWN);

                character1.setVisible(true);
                character1.setPosition(16, 17);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.DOWN);

                character2.setVisible(true);
                character2.setPosition(16, 17);
                character2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character2.setCurrentDirection(Entity.Direction.DOWN);

                followActor(camactor);
            }
        };

        setupGuardsSurroundScene = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.ELMOUR);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(16, 9);

                camactor.setPosition(37.5f, 29);
                camactor.setCurrentAnimationType(Entity.AnimationType.IDLE);
                camactor.setCurrentDirection(Entity.Direction.DOWN);

                character1.setVisible(true);
                character1.setPosition(37.5f, 43);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.DOWN);

                character2.setPosition(37.5f, 43);
                character2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character2.setCurrentDirection(Entity.Direction.DOWN);

                guard1.setVisible(true);
                guard1.setPosition(28, 22);
                guard1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                guard1.setCurrentDirection(Entity.Direction.UP);

                guard2.setVisible(true);
                guard2.setPosition(37.5f, 14);
                guard2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                guard2.setCurrentDirection(Entity.Direction.UP);

                guard3.setVisible(true);
                guard3.setPosition(47, 22);
                guard3.setCurrentAnimationType(Entity.AnimationType.IDLE);
                guard3.setCurrentDirection(Entity.Direction.UP);

                guard4.setPosition(35, 35);
                guard4.setCurrentAnimationType(Entity.AnimationType.IDLE);
                guard4.setCurrentDirection(Entity.Direction.DOWN);

                guard5.setPosition(37.5f, 35);
                guard5.setCurrentAnimationType(Entity.AnimationType.IDLE);
                guard5.setCurrentDirection(Entity.Direction.DOWN);

                guard6.setPosition(40, 35);
                guard6.setCurrentAnimationType(Entity.AnimationType.IDLE);
                guard6.setCurrentDirection(Entity.Direction.DOWN);

                guard7.setPosition(31.5f, 29);
                guard7.setCurrentAnimationType(Entity.AnimationType.IDLE);
                guard7.setCurrentDirection(Entity.Direction.RIGHT);

                guard8.setPosition(43.5f, 29);
                guard8.setCurrentAnimationType(Entity.AnimationType.IDLE);
                guard8.setCurrentDirection(Entity.Direction.LEFT);

                followActor(character1);
            }
        };

        setupWakeUpScene = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.BARREN_ROOM);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(4.5f, 5.5f);

                character1.setVisible(true);
                character1.setPosition(4, 5);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.DOWN);

                character2.setPosition(8, 5);
                character2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character2.setCurrentDirection(Entity.Direction.LEFT);
                character2.setVisible(false);

                guard1.setPosition(8, 5);
                guard1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                guard1.setCurrentDirection(Entity.Direction.LEFT);
                guard1.setVisible(false);

                guard2.setPosition(8, 5);
                guard2.setCurrentAnimationType(Entity.AnimationType.IDLE);
                guard2.setCurrentDirection(Entity.Direction.LEFT);
                guard2.setVisible(false);

                guard3.setPosition(8, 5);
                guard3.setCurrentAnimationType(Entity.AnimationType.IDLE);
                guard3.setCurrentDirection(Entity.Direction.LEFT);
                guard3.setVisible(false);

                guard4.setPosition(8, 5);
                guard4.setCurrentAnimationType(Entity.AnimationType.IDLE);
                guard4.setCurrentDirection(Entity.Direction.LEFT);
                guard4.setVisible(false);

                guard5.setPosition(8, 5);
                guard5.setCurrentAnimationType(Entity.AnimationType.IDLE);
                guard5.setCurrentDirection(Entity.Direction.LEFT);
                guard5.setVisible(false);

                guard6.setPosition(8, 5);
                guard6.setCurrentAnimationType(Entity.AnimationType.IDLE);
                guard6.setCurrentDirection(Entity.Direction.LEFT);
                guard6.setVisible(false);

                guard7.setPosition(8, 5);
                guard7.setCurrentAnimationType(Entity.AnimationType.IDLE);
                guard7.setCurrentDirection(Entity.Direction.LEFT);
                guard7.setVisible(false);

                misc.setPosition(4 + emoteX, 5 + emoteY);
                misc.setCurrentAnimationType(Entity.AnimationType.SHOCK_OFF);
                misc.setVisible(false);

                blackBarLeft.setWidth(2.5f);
                blackBarLeft.setHeight(5);
                blackBarLeft.setPosition(-1.5f, 2.5f);
                blackBarLeft.setVisible(true);

                blackBarRight.setWidth(2.5f);
                blackBarRight.setHeight(5);
                blackBarRight.setPosition(9, 4);
                blackBarRight.setVisible(true);
            }
        };

        setupPortalRoomScene = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.PORTAL_ROOM);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(5, 4.25f);

                character1.setPosition(3, 6);
                character1.setCurrentAnimationType(Entity.AnimationType.WALK_DOWN);
                character1.setCurrentDirection(Entity.Direction.UP);

                character2.setVisible(true);
                character2.setPosition(3, 4);
                character2.setCurrentAnimationType(Entity.AnimationType.WALK_DOWN);
                character2.setCurrentDirection(Entity.Direction.UP);

                guard1.setVisible(false);
                guard2.setVisible(false);
                guard3.setVisible(false);
                guard4.setVisible(false);
                guard5.setVisible(false);
                guard6.setVisible(false);
                guard7.setVisible(false);

                misc.setPosition(4.8f + emoteX, 3.5f + emoteY);
                misc.setCurrentAnimationType(Entity.AnimationType.SHOCK_OFF);
                misc.setVisible(false);

                blackBarLeft.setVisible(false);

                blackBarRight.setWidth(2.5f);
                blackBarRight.setHeight(5);
                blackBarRight.setPosition(8, 0);
                blackBarRight.setVisible(true);
            }
        };
    }

    @Override
    public void onInputDialogNotify(String value, InputDialogEvent event) {
        Gdx.app.log(TAG, "Got value " + value + " for event " + event.toString());

        switch(event) {
            case GET_CHAR1_NAME:
                ProfileManager.getInstance().setProperty("CHARACTER_1", value);
                _stage.addAction(
                        myActions.new continueConversation(_playerHUD)
                );
                break;
            case GET_CHAR2_NAME:
                ProfileManager.getInstance().setProperty("CHARACTER_2", value);
                _stage.addAction(
                        myActions.new continueConversation(_playerHUD)
                );
                break;
        }
    }

    @Override
    public void onNotify(ConversationGraph graph, ConversationCommandEvent action, String conversationId) {
        Gdx.app.log(TAG, "Got notification " + action.toString());
        oneBlockTime = 0.3f;
        emoteOn = 0.7f;
        emoteOff = 0.05f;

        switch (action) {
            case WAIT_500:
                _playerHUD.doConversation(graph.getNextConversationIDFromChoice(conversationId, 0), 500);
                break;
            case WAIT_1000:
                _playerHUD.doConversation(graph.getNextConversationIDFromChoice(conversationId, 0), 1000);
                break;
            case WAIT_10000:
                _playerHUD.doConversation(graph.getNextConversationIDFromChoice(conversationId, 0), 10000);
                break;
            case LOOK_AROUND:
                _stage.addAction(Actions.sequence(
                        myActions.new setIdleDirection(character1, Entity.Direction.LEFT),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setIdleDirection(character1, Entity.Direction.UP),
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setIdleDirection(character1, Entity.Direction.LEFT),
                        Actions.delay(oneBlockTime * 2),

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
            case WALK_TO_MIRROR:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveTo(3, character1.getY(), oneBlockTime * 2.5f), character1),
                        Actions.delay(oneBlockTime * 2.5f),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(3, 3, oneBlockTime * 1.5f), character1),
                        Actions.delay(oneBlockTime * 1.5f),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveTo(2, 3, oneBlockTime), character1),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime * 4),
                        //Look around
                        myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setIdleDirection(character1, Entity.Direction.UP),
                        Actions.delay(oneBlockTime * 4),
                        myActions.new setIdleDirection(character1, Entity.Direction.LEFT),
                        Actions.delay(oneBlockTime * 2),

                        Actions.addAction(Actions.moveTo(2 + emoteX, 3 + emoteY), misc),
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
            case JUMP_BACK:
                if( shakeCam == null ){
                    shakeCam = new ShakeCamera(_camera.position.x, _camera.position.y,
                            0.10f,
                            0.05f,
                            0.025f,
                            0.0125f,
                            0.2f,
                            0.2f,
                            0.2f);
                }
                _stage.addAction(Actions.sequence(
                        //Stutter
                        Actions.addAction(Actions.moveBy(0.05f, 0, 0.0025f), character1),
                        Actions.delay(0.0025f),
                        Actions.addAction(Actions.moveBy(-0.10f, 0, 0.005f), character1),
                        Actions.delay(0.005f),
                        Actions.addAction(Actions.moveBy(0.05f, 0, 0.0025f), character1),
                        Actions.delay(0.75f),
                        //Jump
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveTo(3.25f, 3.75f, 0.1f), character1),
                        Actions.delay(0.1f),

                        myActions.new shakeCam(shakeCam),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.FALL_LEFT),
                        Actions.addAction(Actions.moveBy(-0.5f, -0.25f, oneBlockTime), character1),
                        //Get up
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                        Actions.addAction(Actions.moveTo(3, 3.5f), character1),
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setIdleDirection(character1, Entity.Direction.LEFT),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setIdleDirection(character1, Entity.Direction.UP),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case CHASE_SEQUENCE:
                _stage.addAction(
                        Actions.parallel(
                                character1Chase(),
                                character2Chase()
                        )
                );
                break;
            case GUARDS_SURROUND:
                _stage.addAction(
                        Actions.parallel(
                                charactersGuardSurround(),
                                guardsSurround()
                        )
                );
                break;
            case GUARDS_STOP:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(character1, Entity.Direction.UP),
                        Actions.delay(oneBlockTime),

                        myActions.new setWalkDirection(guard5, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveBy(0, 1, oneBlockTime), guard5),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(guard5, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveBy(1, 0, oneBlockTime), guard5),
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(guard5, Entity.Direction.LEFT),
                        myActions.new setWalkDirection(guard5, Entity.AnimationType.IDLE),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveTo(37.5f, 30.25f , oneBlockTime * 3), character2),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case LOOK_AROUND_ELMOUR:
                oneBlockTime = 0.5f;
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),

                        Actions.run(
                            new Runnable() {
                                @Override
                                public void run() {
                                    followActor(camactor);
                                }
                            }
                        ),

                        new setZoomRate(0.01f),
                        Actions.delay(oneBlockTime * 2),
                        new setZoomRate(0),

                        myActions.new setIdleDirection(character1, Entity.Direction.LEFT),
                        Actions.addAction(Actions.moveTo(21, 29, oneBlockTime * 2), camactor),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                        Actions.addAction(Actions.moveTo(22, 13, oneBlockTime * 2), camactor),
                        Actions.delay(oneBlockTime * 3),
                        Actions.addAction(Actions.moveTo(53, 13, oneBlockTime * 3), camactor),
                        Actions.delay(oneBlockTime * 4),
                        myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),
                        Actions.addAction(Actions.moveTo(54, 29, oneBlockTime * 2), camactor),
                        Actions.delay(oneBlockTime * 3),
                        Actions.addAction(Actions.moveTo(37.5f, 29, oneBlockTime * 2), camactor),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setIdleDirection(character1, Entity.Direction.UP),

                        new setZoomRate(-0.01f),
                        Actions.delay(oneBlockTime * 2),
                        new setZoomRate(0),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case THINK:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime * 0.5f),
                        myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),
                        Actions.delay(oneBlockTime * 0.25f),
                        myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                        Actions.delay(oneBlockTime * 0.5f),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.THINK),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case STUTTER:
                _stage.addAction(Actions.sequence(
                        Actions.addAction(Actions.moveBy(0.05f, 0, 0.0025f), character1),
                        Actions.delay(0.0025f),
                        Actions.addAction(Actions.moveBy(-0.10f, 0, 0.005f), character1),
                        Actions.delay(0.005f),
                        Actions.addAction(Actions.moveBy(0.05f, 0, 0.0025f), character1),
                        Actions.delay(0.75f),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case FAINT:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.FALL_DOWN),
                        Actions.delay(oneBlockTime * 2),

                        Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 2), _transitionActor),
                        new setFading(true),
                        Actions.delay(2),

                        Actions.addAction(getWakeUpScene())
                        )
                );

                break;
            case LOOK_AROUND_AGAIN:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                        myActions.new setIdleDirection(character1, Entity.Direction.LEFT),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case ENTER_GUARDS:
                _stage.addAction(Actions.sequence(
                        //First
                        myActions.new setCharacterVisible(guard1, true),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(guard1, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveBy(-1, 0, oneBlockTime), guard1),
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),

                        Actions.addAction(Actions.moveTo(character1.getX() + emoteX, character1.getY() + emoteY), misc),
                        myActions.new setCharacterVisible(misc, true),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.SHOCK_ON),

                       //Second
                        myActions.new setCharacterVisible(guard2, true),
                        //guard 1
                        myActions.new setIdleDirection(guard1, Entity.Direction.UP),
                        myActions.new setWalkDirection(guard1, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),

                        myActions.new setWalkDirection(misc, Entity.AnimationType.SHOCK_OFF),
                        Actions.delay(emoteOff),
                        myActions.new setCharacterVisible(misc, false),

                        myActions.new setWalkDirection(guard1, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveBy(0, 1, oneBlockTime), guard1),
                        //guard 2
                        myActions.new setWalkDirection(guard2, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveBy(-1, 0, oneBlockTime), guard2),
                        Actions.delay(oneBlockTime),
                        //guard 1
                        myActions.new setIdleDirection(guard1, Entity.Direction.LEFT),
                        myActions.new setWalkDirection(guard1, Entity.AnimationType.IDLE),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.SLOW_WALK_RIGHT),
                        Actions.addAction(Actions.moveBy(-0.1f, 0, oneBlockTime * 2), character1),

                        //Third
                        myActions.new setCharacterVisible(guard3, true),
                        //guard 2
                        myActions.new setIdleDirection(guard2, Entity.Direction.DOWN),
                        myActions.new setWalkDirection(guard2, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),

                        myActions.new setWalkDirection(guard2, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveBy(0, -1, oneBlockTime), guard2),
                        //guard 3
                        myActions.new setWalkDirection(guard3, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveBy(-1, 0, oneBlockTime), guard3),
                        Actions.delay(oneBlockTime),
                        //guard 2
                        myActions.new setIdleDirection(guard2, Entity.Direction.LEFT),
                        myActions.new setWalkDirection(guard2, Entity.AnimationType.IDLE),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.SLOW_WALK_RIGHT),
                        Actions.addAction(Actions.moveBy(-0.1f, 0, oneBlockTime), character1),

                        //Fourth
                        myActions.new setCharacterVisible(guard4, true),
                        //guard 1 and 3
                        myActions.new setIdleDirection(guard1, Entity.Direction.UP),
                        myActions.new setWalkDirection(guard1, Entity.AnimationType.IDLE),
                        myActions.new setIdleDirection(guard3, Entity.Direction.UP),
                        myActions.new setWalkDirection(guard3, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),

                        myActions.new setWalkDirection(guard3, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveBy(0, 1, oneBlockTime), guard3),
                        myActions.new setWalkDirection(guard1, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveBy(0, 1, oneBlockTime), guard1),
                        //guard 4
                        myActions.new setWalkDirection(guard4, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveBy(-1, 0, oneBlockTime), guard4),
                        Actions.delay(oneBlockTime),
                        //guard 1 and 3
                        myActions.new setIdleDirection(guard1, Entity.Direction.LEFT),
                        myActions.new setWalkDirection(guard1, Entity.AnimationType.IDLE),
                        myActions.new setIdleDirection(guard3, Entity.Direction.LEFT),
                        myActions.new setWalkDirection(guard3, Entity.AnimationType.IDLE),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.SLOW_WALK_RIGHT),
                        Actions.addAction(Actions.moveBy(-0.1f, 0, oneBlockTime), character1),

                        //Fifth
                        myActions.new setCharacterVisible(guard5, true),
                        //guard 2 and 4
                        myActions.new setIdleDirection(guard2, Entity.Direction.DOWN),
                        myActions.new setWalkDirection(guard2, Entity.AnimationType.IDLE),
                        myActions.new setIdleDirection(guard4, Entity.Direction.DOWN),
                        myActions.new setWalkDirection(guard4, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),

                        myActions.new setWalkDirection(guard4, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveBy(0, -1, oneBlockTime), guard4),
                        myActions.new setWalkDirection(guard2, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveBy(0, -1, oneBlockTime), guard2),
                        //guard 5
                        myActions.new setWalkDirection(guard5, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveBy(-1, 0, oneBlockTime), guard5),
                        Actions.delay(oneBlockTime),
                        //guard 2 and 4
                        myActions.new setIdleDirection(guard2, Entity.Direction.LEFT),
                        myActions.new setWalkDirection(guard2, Entity.AnimationType.IDLE),
                        myActions.new setIdleDirection(guard4, Entity.Direction.LEFT),
                        myActions.new setWalkDirection(guard4, Entity.AnimationType.IDLE),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.SLOW_WALK_RIGHT),
                        Actions.addAction(Actions.moveBy(-0.1f, 0, oneBlockTime), character1),

                        //Sixth
                        myActions.new setCharacterVisible(guard6, true),
                        //guard 1 and 3 and 5
                        myActions.new setIdleDirection(guard1, Entity.Direction.UP),
                        myActions.new setWalkDirection(guard1, Entity.AnimationType.IDLE),
                        myActions.new setIdleDirection(guard3, Entity.Direction.UP),
                        myActions.new setWalkDirection(guard3, Entity.AnimationType.IDLE),
                        myActions.new setIdleDirection(guard5, Entity.Direction.UP),
                        myActions.new setWalkDirection(guard5, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),

                        myActions.new setWalkDirection(guard1, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveBy(0, 1, oneBlockTime), guard1),
                        myActions.new setWalkDirection(guard3, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveBy(0, 1, oneBlockTime), guard3),
                        myActions.new setWalkDirection(guard5, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveBy(0, 1, oneBlockTime), guard5),
                        //guard 6
                        myActions.new setWalkDirection(guard6, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveBy(-1, 0, oneBlockTime), guard6),
                        Actions.delay(oneBlockTime),
                        //guard 1 and 3 and 5
                        myActions.new setIdleDirection(guard1, Entity.Direction.LEFT),
                        myActions.new setWalkDirection(guard1, Entity.AnimationType.IDLE),
                        myActions.new setIdleDirection(guard3, Entity.Direction.LEFT),
                        myActions.new setWalkDirection(guard3, Entity.AnimationType.IDLE),
                        myActions.new setIdleDirection(guard5, Entity.Direction.LEFT),
                        myActions.new setWalkDirection(guard5, Entity.AnimationType.IDLE),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.SLOW_WALK_RIGHT),
                        Actions.addAction(Actions.moveBy(-0.1f, 0, oneBlockTime), character1),

                        //Seventh
                        myActions.new setCharacterVisible(guard7, true),
                        //guard 2 and 4 and 6
                        myActions.new setIdleDirection(guard2, Entity.Direction.DOWN),
                        myActions.new setWalkDirection(guard2, Entity.AnimationType.IDLE),
                        myActions.new setIdleDirection(guard4, Entity.Direction.DOWN),
                        myActions.new setWalkDirection(guard4, Entity.AnimationType.IDLE),
                        myActions.new setIdleDirection(guard6, Entity.Direction.DOWN),
                        myActions.new setWalkDirection(guard6, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),

                        myActions.new setWalkDirection(guard2, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveBy(0, -1, oneBlockTime), guard2),
                        myActions.new setWalkDirection(guard4, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveBy(0, -1, oneBlockTime), guard4),
                        myActions.new setWalkDirection(guard6, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveBy(0, -1, oneBlockTime), guard6),
                        //guard 7
                        myActions.new setWalkDirection(guard7, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveBy(-1, 0, oneBlockTime), guard7),
                        Actions.delay(oneBlockTime),
                        //guard 2 and 4 and 6
                        myActions.new setIdleDirection(guard2, Entity.Direction.LEFT),
                        myActions.new setWalkDirection(guard2, Entity.AnimationType.IDLE),
                        myActions.new setIdleDirection(guard4, Entity.Direction.LEFT),
                        myActions.new setWalkDirection(guard4, Entity.AnimationType.IDLE),
                        myActions.new setIdleDirection(guard6, Entity.Direction.LEFT),
                        myActions.new setWalkDirection(guard6, Entity.AnimationType.IDLE),
                        myActions.new setIdleDirection(guard7, Entity.Direction.LEFT),
                        myActions.new setWalkDirection(guard7, Entity.AnimationType.IDLE),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case GUARDS_MOVE_FORWARD:
                oneBlockTime = 0.5f;
                _stage.addAction(Actions.sequence(
                        Actions.parallel(
                                myActions.new setWalkDirection(guard1, Entity.AnimationType.WALK_LEFT),
                                Actions.addAction(Actions.moveTo(1, 8, oneBlockTime * 6), guard1),
                                myActions.new setWalkDirection(guard2, Entity.AnimationType.WALK_LEFT),
                                Actions.addAction(Actions.moveTo(1, 2, oneBlockTime * 6), guard2),
                                myActions.new setWalkDirection(guard3, Entity.AnimationType.WALK_LEFT),
                                Actions.addAction(Actions.moveTo(2, 7, oneBlockTime * 5), guard3),
                                myActions.new setWalkDirection(guard4, Entity.AnimationType.WALK_LEFT),
                                Actions.addAction(Actions.moveTo(2, 3, oneBlockTime * 5), guard4),
                                myActions.new setWalkDirection(guard5, Entity.AnimationType.WALK_LEFT),
                                Actions.addAction(Actions.moveTo(3, 6, oneBlockTime * 4), guard5),
                                myActions.new setWalkDirection(guard6, Entity.AnimationType.WALK_LEFT),
                                Actions.addAction(Actions.moveTo(3, 4, oneBlockTime * 4), guard6),
                                myActions.new setWalkDirection(guard7, Entity.AnimationType.WALK_LEFT),
                                Actions.addAction(Actions.moveTo(4, 5, oneBlockTime * 3), guard7)
                        ),

                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveBy(-2.5f, 0, oneBlockTime * 2), character1),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                        myActions.new setWalkDirection(guard7, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(character1, Entity.Direction.UP),
                        myActions.new setWalkDirection(guard5, Entity.AnimationType.IDLE),
                        myActions.new setWalkDirection(guard6, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                        myActions.new setIdleDirection(guard3, Entity.Direction.DOWN),
                        myActions.new setIdleDirection(guard4, Entity.Direction.UP),
                        myActions.new setWalkDirection(guard3, Entity.AnimationType.IDLE),
                        myActions.new setWalkDirection(guard4, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(guard1, Entity.Direction.DOWN),
                        myActions.new setIdleDirection(guard2, Entity.Direction.UP),
                        myActions.new setWalkDirection(guard1, Entity.AnimationType.IDLE),
                        myActions.new setWalkDirection(guard2, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case FORCE_FIELD:
                if( shakeCam == null ){
                    shakeCam = new ShakeCamera(_camera.position.x, _camera.position.y,
                            5.20f,
                            0.1f,
                            0.05f,
                            0.025f,
                            1f,
                           0.70f,
                            0.90f);
                }
                float percentToChar1 = 0.6f;
                Vector2 guard1Destination = new Vector2(guard1.getX() - ((guard1.getX() - character1.getX()) * percentToChar1),
                                                        guard1.getY() - ((guard1.getY() - character1.getY()) * percentToChar1));
                Vector2 guard2Destination = new Vector2(guard2.getX() - ((guard2.getX() - character1.getX()) * percentToChar1),
                                                        guard2.getY() - ((guard2.getY() - character1.getY()) * percentToChar1));
                Vector2 guard3Destination = new Vector2(guard3.getX() - ((guard3.getX() - character1.getX()) * percentToChar1),
                                                        guard3.getY() - ((guard3.getY() - character1.getY()) * percentToChar1));
                Vector2 guard4Destination = new Vector2(guard4.getX() - ((guard4.getX() - character1.getX()) * percentToChar1),
                                                        guard4.getY() - ((guard4.getY() - character1.getY()) * percentToChar1));
                Vector2 guard5Destination = new Vector2(guard5.getX() - ((guard5.getX() - character1.getX()) * percentToChar1),
                                                        guard5.getY() - ((guard5.getY() - character1.getY()) * percentToChar1));
                Vector2 guard6Destination = new Vector2(guard6.getX() - ((guard6.getX() - character1.getX()) * percentToChar1),
                                                        guard6.getY() - ((guard6.getY() - character1.getY()) * percentToChar1));
                Vector2 guard7Destination = new Vector2(guard7.getX() - ((guard7.getX() - character1.getX()) * percentToChar1),
                                                        guard7.getY() - ((guard7.getY() - character1.getY()) * percentToChar1));
                float guard3_4_X = (guard3.getX() - guard3Destination.x) * 3 * (float)Math.sqrt(5);
                float guard5_6_Y = (guard5.getY() - guard5Destination.y) * 3 * (float)Math.sqrt(1.25);

                float guard3_4_Time = (float)Math.sqrt((guard3_4_X * guard3_4_X) + (4.1f * 4.1f));
                float guard5_6_Time = (float)Math.sqrt((guard5_6_Y * guard5_6_Y) + (4.1f * 4.1f));

                _stage.addAction(Actions.sequence(
                        Actions.parallel(
                        myActions.new setWalkDirection(guard1, Entity.AnimationType.RUN_DOWN),
                        Actions.addAction(Actions.moveTo(guard1Destination.x, guard1Destination.y, oneBlockTime * 2), guard1),
                        myActions.new setWalkDirection(guard2, Entity.AnimationType.RUN_UP),
                        Actions.addAction(Actions.moveTo(guard2Destination.x, guard2Destination.y, oneBlockTime * 2), guard2),
                        myActions.new setWalkDirection(guard3, Entity.AnimationType.RUN_DOWN),
                        Actions.addAction(Actions.moveTo(guard3Destination.x, guard3Destination.y, oneBlockTime * 2), guard3),
                        myActions.new setWalkDirection(guard4, Entity.AnimationType.RUN_UP),
                        Actions.addAction(Actions.moveTo(guard4Destination.x, guard4Destination.y, oneBlockTime * 2), guard4),
                        myActions.new setWalkDirection(guard5, Entity.AnimationType.RUN_LEFT),
                        Actions.addAction(Actions.moveTo(guard5Destination.x, guard5Destination.y, oneBlockTime * 2), guard5),
                        myActions.new setWalkDirection(guard6, Entity.AnimationType.RUN_LEFT),
                        Actions.addAction(Actions.moveTo(guard6Destination.x, guard6Destination.y, oneBlockTime * 2), guard6),
                        myActions.new setWalkDirection(guard7, Entity.AnimationType.RUN_LEFT),
                        Actions.addAction(Actions.moveTo(guard7Destination.x, guard7Destination.y, oneBlockTime * 2), guard7)
                        ),
                        Actions.delay(oneBlockTime * 0.5f),

                        Actions.addAction(Actions.moveBy(0.05f, 0, 0.0025f), character1),
                        Actions.delay(oneBlockTime * 0.0025f),
                        Actions.addAction(Actions.moveBy(-0.10f, 0, 0.005f), character1),
                        Actions.delay(oneBlockTime * 0.005f),
                        Actions.addAction(Actions.moveBy(0.05f, 0, 0.0025f), character1),

                        Actions.delay(oneBlockTime * 0.2425f),

                        Actions.addAction(Actions.moveTo(-1, 3), misc),
                        myActions.new setCharacterVisible(misc, true),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.FORCEFIELD),
                        Actions.delay(oneBlockTime * 0.5f),
                        myActions.new shakeCam(shakeCam),

                        Actions.parallel(
                                myActions.new setWalkDirection(guard1, Entity.AnimationType.RUN_DOWN),
                                Actions.addAction(Actions.moveTo(guard1Destination.x, 9, oneBlockTime * 0.25f * 4.1f), guard1),
                                myActions.new setWalkDirection(guard2, Entity.AnimationType.RUN_UP),
                                Actions.addAction(Actions.moveTo(guard2Destination.x, 1, oneBlockTime * 0.25f * 4.1f), guard2),
                                myActions.new setWalkDirection(guard3, Entity.AnimationType.RUN_DOWN),
                                Actions.addAction(Actions.moveTo(guard3_4_X, 9, oneBlockTime * 0.25f * guard3_4_Time), guard3),
                                myActions.new setWalkDirection(guard4, Entity.AnimationType.RUN_UP),
                                Actions.addAction(Actions.moveTo(guard3_4_X, 1, oneBlockTime * 0.25f * guard3_4_Time), guard4),
                                myActions.new setWalkDirection(guard5, Entity.AnimationType.RUN_LEFT),
                                Actions.addAction(Actions.moveTo(7, character1.getY() + guard5_6_Y, oneBlockTime * 0.25f * guard5_6_Time), guard5),
                                myActions.new setWalkDirection(guard6, Entity.AnimationType.RUN_LEFT),
                                Actions.addAction(Actions.moveTo(7, guard5_6_Y, oneBlockTime * 0.25f * guard5_6_Time), guard6),
                                myActions.new setWalkDirection(guard7, Entity.AnimationType.RUN_LEFT),
                                Actions.addAction(Actions.moveTo(10, guard7Destination.y, oneBlockTime * 0.25f * 9), guard7)
                        ),
                        Actions.delay(oneBlockTime * 0.25f * 4.1f),
                        myActions.new setWalkDirection(guard1, Entity.AnimationType.FALL_DOWN),
                        myActions.new setWalkDirection(guard2, Entity.AnimationType.FALL_UP),
                        Actions.delay((oneBlockTime * 0.25f * guard3_4_Time) - (oneBlockTime * 0.25f * 4.1f)),
                        myActions.new setWalkDirection(guard3, Entity.AnimationType.FALL_DOWN),
                        myActions.new setWalkDirection(guard4, Entity.AnimationType.FALL_UP),
                        Actions.delay((oneBlockTime * 0.25f * guard5_6_Time) - (oneBlockTime * 0.25f * guard3_4_Time) - (oneBlockTime * 0.25f * 4.1f)),
                        myActions.new setWalkDirection(guard5, Entity.AnimationType.FALL_LEFT),
                        myActions.new setWalkDirection(guard6, Entity.AnimationType.FALL_LEFT),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(guard7, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),


                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                        myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setIdleDirection(character1, Entity.Direction.UP),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new setCharacterVisible(character2, true),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveTo(5, 5, oneBlockTime * 3), character2),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case GUARD_SHAKE:
                _stage.addAction(Actions.sequence(
                        Actions.addAction(Actions.moveBy(0.05f, 0, 0.0025f), guard5),
                        Actions.delay(0.025f),
                        Actions.addAction(Actions.moveBy(-0.10f, 0, 0.005f), guard5),
                        Actions.delay(0.05f),
                        Actions.addAction(Actions.moveBy(0.05f, 0, 0.0025f), guard5),
                        Actions.delay(0.05f),
                        Actions.delay(oneBlockTime * 3),

                        myActions.new setWalkDirection(guard5, Entity.AnimationType.REACH_LEFT),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case CHAR2_NEXT_TO_CHAR1:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveTo(2, 5, oneBlockTime * 6), character2),
                        Actions.delay(oneBlockTime * 6),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime * 3),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case DISMISSED:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(guard1, Entity.AnimationType.IDLE),
                        myActions.new setWalkDirection(guard2, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(guard1, Entity.AnimationType.WALK_DOWN),
                        myActions.new setWalkDirection(guard2, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(1, 7, oneBlockTime * 2), guard1),
                        Actions.addAction(Actions.moveTo(1, 3, oneBlockTime * 2), guard2),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setWalkDirection(guard1, Entity.AnimationType.IDLE),
                        myActions.new setWalkDirection(guard2, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(guard1, Entity.Direction.RIGHT),
                        myActions.new setIdleDirection(guard2, Entity.Direction.RIGHT),
                        Actions.delay(oneBlockTime),

                        myActions.new setWalkDirection(guard1, Entity.AnimationType.WALK_RIGHT),
                        myActions.new setWalkDirection(guard2, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveTo(5, 7, oneBlockTime * 5), guard1),
                        Actions.addAction(Actions.moveTo(4, 3, oneBlockTime * 4), guard2),
                        Actions.delay(oneBlockTime * 4),
                        myActions.new setWalkDirection(guard2, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(guard1, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(guard1, Entity.Direction.DOWN),
                        myActions.new setIdleDirection(guard2, Entity.Direction.UP),
                        myActions.new setWalkDirection(guard3, Entity.AnimationType.IDLE),
                        myActions.new setWalkDirection(guard4, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),

                        myActions.new setIdleDirection(guard3, Entity.Direction.RIGHT),
                        myActions.new setWalkDirection(guard1, Entity.AnimationType.WALK_DOWN),
                        myActions.new setWalkDirection(guard2, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(5, 5, oneBlockTime * 2), guard1),
                        Actions.addAction(Actions.moveTo(4, 5, oneBlockTime * 2), guard2),

                        Actions.delay(oneBlockTime),
                        Actions.addAction(Actions.moveTo(5, 9, oneBlockTime), guard3),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(guard1, Entity.AnimationType.IDLE),
                        myActions.new setWalkDirection(guard2, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(guard1, Entity.Direction.RIGHT),
                        myActions.new setIdleDirection(guard2, Entity.Direction.RIGHT),
                        myActions.new setIdleDirection(guard3, Entity.Direction.DOWN),
                        Actions.delay(oneBlockTime),

                        myActions.new setWalkDirection(guard3, Entity.AnimationType.WALK_DOWN),
                        myActions.new setWalkDirection(guard4, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(5, 5, oneBlockTime * 5), guard3),
                        Actions.addAction(Actions.moveTo(4, 5, oneBlockTime * 5), guard4),
                        myActions.new setWalkDirection(guard1, Entity.AnimationType.WALK_RIGHT),
                        myActions.new setWalkDirection(guard2, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveTo(10, 5, oneBlockTime * 5), guard1),
                        Actions.addAction(Actions.moveTo(9, 5, oneBlockTime * 5), guard2),
                        Actions.delay(oneBlockTime * 5),
                        myActions.new setWalkDirection(guard4, Entity.AnimationType.IDLE),
                        myActions.new setWalkDirection(guard3, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),

                        myActions.new setWalkDirection(guard3, Entity.AnimationType.IDLE),
                        myActions.new setWalkDirection(guard4, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(guard3, Entity.AnimationType.WALK_RIGHT),
                        myActions.new setWalkDirection(guard4, Entity.AnimationType.WALK_RIGHT),
                        Actions.delay(oneBlockTime),
                        Actions.addAction(Actions.moveTo(10, 5, oneBlockTime * 5), guard3),
                        Actions.addAction(Actions.moveTo(9, 5, oneBlockTime * 5), guard4),
                        Actions.delay(oneBlockTime * 3),

                        myActions.new setWalkDirection(guard5, Entity.AnimationType.IDLE),
                        myActions.new setWalkDirection(guard6, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(guard6, Entity.Direction.UP),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(guard5, Entity.AnimationType.WALK_LEFT),
                        myActions.new setWalkDirection(guard6, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(3, 7, oneBlockTime * 4), guard5),
                        Actions.addAction(Actions.moveTo(7, 5, oneBlockTime * 2), guard6),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setWalkDirection(guard6, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(guard6, Entity.Direction.RIGHT),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(guard6, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveTo(10, 5, oneBlockTime * 3), guard6),
                        myActions.new setWalkDirection(guard5, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(guard5, Entity.Direction.DOWN),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(guard5, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveTo(3, 5, oneBlockTime * 2), guard5),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setWalkDirection(guard5, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(guard5, Entity.Direction.LEFT),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case CHAR2_ANGER:
                if( shakeCam == null ){
                    shakeCam = new ShakeCamera(_camera.position.x, _camera.position.y,
                            0.10f,
                            0.05f,
                            0.025f,
                            0.0125f,
                            0.2f,
                            0.2f,
                            0.2f);
                }
                shakeCam.startShaking();

                _stage.addAction(Actions.sequence(
                        Actions.addAction(Actions.moveTo(character2.getX() + emoteX - 0.1f, character2.getY() + emoteY), misc),
                        myActions.new setCharacterVisible(misc, true),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.ANGER_ON),

                        myActions.new setEnabledHUD(_playerHUD, false),
                        myActions.new continueConversation(_playerHUD),

                        myActions.new setOrigin(guard5, guard5.getWidth()/3, guard5.getHeight()/3),
                        myActions.new setWalkDirection(guard5, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveTo(10, 5, oneBlockTime * 2), guard5),
                        Actions.addAction(Actions.rotateBy(-4500, oneBlockTime * 10), guard5),
                        Actions.delay(oneBlockTime * 5.5f),

                        myActions.new continueConversation(_playerHUD),

                        myActions.new setWalkDirection(misc, Entity.AnimationType.ANGER_OFF),
                        Actions.delay(emoteOff),
                        myActions.new setCharacterVisible(misc, false),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new setEnabledHUD(_playerHUD, true)
                        )
                );

                break;
            case GET_CHAR1_NAME:
                _playerHUD.requestInput("What is my name?", InputDialogEvent.GET_CHAR1_NAME);
                break;
            case GET_CHAR2_NAME:
                _playerHUD.requestInput("What was her name again?", InputDialogEvent.GET_CHAR2_NAME);
                break;
            case CHAR2_TURN_AROUND:
                _stage.addAction(Actions.sequence(
                        myActions.new setIdleDirection(character2, Entity.Direction.RIGHT),
                        myActions.new setCharacterVisible(misc, true),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.SAD_ON),
                        Actions.delay(emoteOn),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.SAD_OFF),
                        Actions.delay(emoteOff),
                        myActions.new setCharacterVisible(misc, false),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setIdleDirection(character2, Entity.Direction.DOWN),
                        Actions.delay(oneBlockTime),

                        myActions.new setWalkDirection(character2, Entity.AnimationType.THINK),
                        myActions.new setCharacterVisible(misc, true),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.THINK_ON),
                        Actions.delay(0.24f),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.THINK_LOOP),
                        Actions.delay(2.1f),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.THINK_OFF),
                        Actions.delay(0.075f),
                        myActions.new setCharacterVisible(misc, false),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setIdleDirection(character2, Entity.Direction.LEFT),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case CHAR2_WALK_RIGHT:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveTo(4, 5, oneBlockTime * 2), character2),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setIdleDirection(character2, Entity.Direction.RIGHT),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case GO_TO_PORTAL_ROOM:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_RIGHT),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveTo(10, 5, oneBlockTime * 9), character1),
                        Actions.addAction(Actions.moveTo(10, 5, oneBlockTime * 6), character2),
                        Actions.delay(oneBlockTime * 8),
                        Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 0.5f), _transitionActor),
                        new setFading(true),
                        Actions.delay(oneBlockTime * 2),

                        Actions.addAction(getPortalRoomScene())
                        )
                );

                break;
            case CHAR1_LOOK_CHAR2:
                _stage.addAction(Actions.sequence(
                        myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),
                        Actions.delay(oneBlockTime * 0.5f),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case CHAR1_SHOCK:
                _stage.addAction(Actions.sequence(
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
            case CHAR2_LOOK_CHAR1:
                _stage.addAction(Actions.sequence(
                        myActions.new setIdleDirection(character2, Entity.Direction.LEFT),
                        Actions.delay(oneBlockTime * 0.5f),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case CHAR1_TEAR:
                _stage.addAction(Actions.sequence(
                        myActions.new setCharacterVisible(misc, true),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.TEAR_ON),
                        Actions.delay(0.4f),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.TEAR_OFF),
                        Actions.delay(0.4f),
                        myActions.new setCharacterVisible(misc, false),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case START_LEAVING:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveTo(6, 2, oneBlockTime * 1.5f), character2),
                        Actions.delay(oneBlockTime * 1.5f),
                        myActions.new setIdleDirection(character2, Entity.Direction.DOWN),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),
                        myActions.new setIdleDirection(character1, Entity.Direction.DOWN),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case STOP_LEAVING:
                _stage.addAction(Actions.sequence(
                        myActions.new setIdleDirection(character2, Entity.Direction.UP),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case EXIT_CONVERSATION:
                _stage.addAction(Actions.sequence(
                        new setFading(true),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveTo(9, 2, oneBlockTime * 3), character2),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveTo(5, 2, oneBlockTime * 1.5f), character1),
                        Actions.delay(oneBlockTime * 1.5f),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveTo(9, 2, oneBlockTime * 4), character1),
                        Actions.delay(oneBlockTime * 2.5f)
                        )
                );
                _stage.addAction(Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 3), _transitionActor));

                _stage.addAction(_switchScreenToMainAction);
        }

    }

    private Action character1Chase() {
        return Actions.sequence(

                Actions.delay(oneBlockTime * 2),
                Actions.addAction(Actions.moveBy(0, 0.1f, oneBlockTime, Interpolation.exp10Out), character1),
                Actions.addAction(Actions.moveBy(0, -0.1f, oneBlockTime, Interpolation.exp10In), character1),
                Actions.delay(oneBlockTime),

                new setOneBlockTime(0.2f),
                myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_DOWN),
                Actions.addAction(Actions.moveTo(3, 1.5f, oneBlockTime * 2), character1),
                Actions.delay(oneBlockTime * 2),
                myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_RIGHT),
                Actions.addAction(Actions.moveTo(5, 1.5f, oneBlockTime * 2), character1),
                Actions.delay(oneBlockTime * 2),
                myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_UP),
                Actions.addAction(Actions.moveTo(5, 4, oneBlockTime * 2.5f), character1),
                Actions.delay(oneBlockTime * 2.5f),
                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                Actions.delay(oneBlockTime),
                myActions.new setWalkDirection(character1, Entity.AnimationType.LAY_DOWN),
                Actions.delay(oneBlockTime * 4),

                new setZoomRate(-0.002f),
                Actions.delay(oneBlockTime * 7.7f),

                //Look around
                myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),
                Actions.delay(oneBlockTime),
                myActions.new setIdleDirection(character1, Entity.Direction.LEFT),
                Actions.delay(oneBlockTime),

                new setZoomRate(0),

                myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                Actions.delay(oneBlockTime * 2.75f),
                new setZoomRate(0.0616f),

                Actions.delay(oneBlockTime * 0.25f),
                new setZoomRate(0),

                Actions.addAction(Actions.moveBy(0, 0.15f, oneBlockTime, Interpolation.exp10Out), character1),
                Actions.addAction(Actions.moveBy(0, -0.15f, oneBlockTime, Interpolation.exp10In), character1),
                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_RIGHT),
                //RUN
                Actions.addAction(Actions.moveTo(6, 4, oneBlockTime * 0.25f), character1),
                Actions.delay(oneBlockTime * 0.25f),
                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_DOWN),
                Actions.addAction(Actions.moveTo(6, 1.5f, oneBlockTime * 1.125f), character1),
                Actions.delay(oneBlockTime * 1.125f),
                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_LEFT),
                Actions.addAction(Actions.moveTo(3, 1.5f, oneBlockTime * 0.75f), character1),
                Actions.delay(oneBlockTime * 0.75f),
                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_UP),
                Actions.addAction(Actions.moveTo(3, 7, oneBlockTime * 1.375f), character1),
                Actions.delay(oneBlockTime * 1.375f),
                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_RIGHT),
                Actions.addAction(Actions.moveTo(4, 7, oneBlockTime * 0.25f), character1),
                Actions.delay(oneBlockTime * 0.25f),
                Actions.addAction(Actions.moveTo(7, 10, oneBlockTime * 1.05f), character1),
                Actions.delay(oneBlockTime * 1.05f),
                myActions.new setCharacterVisible(character1, false),
                Actions.delay(oneBlockTime)
                );
    }

    private Action character2Chase() {
        return Actions.sequence(
                myActions.new setCharacterVisible(character2, true),
                Actions.delay(oneBlockTime * 3),
                myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_LEFT),
                Actions.addAction(Actions.moveTo(4, 7, oneBlockTime * 4.2f), character2),
                Actions.delay(oneBlockTime * 4.2f),
                Actions.addAction(Actions.moveTo(3, 7, oneBlockTime), character2),
                Actions.delay(oneBlockTime),
                myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_DOWN),
                Actions.addAction(Actions.moveTo(3, 1.5f, oneBlockTime * 7), character2),
                Actions.delay(oneBlockTime * 7),
                myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_RIGHT),
                Actions.addAction(Actions.moveTo(5, 1.5f, oneBlockTime * 3), character2),
                Actions.delay(oneBlockTime * 3),
                myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_UP),
                Actions.addAction(Actions.moveTo(5, 3.5f, oneBlockTime * 10), character2),
                Actions.delay(oneBlockTime * 10),
                myActions.new setIdleDirection(character2, Entity.Direction.UP),
                myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),
                Actions.delay(oneBlockTime * 0.75f),
                myActions.new setIdleDirection(character2, Entity.Direction.DOWN),
                //RUNRUNRUN
                myActions.new setWalkDirection(character2, Entity.AnimationType.RUN_DOWN),
                Actions.addAction(Actions.moveTo(5, 1.5f, oneBlockTime * 1.125f), character2),
                Actions.delay(oneBlockTime * 1.125f),
                myActions.new setWalkDirection(character2, Entity.AnimationType.RUN_LEFT),
                Actions.addAction(Actions.moveTo(3, 1.5f, oneBlockTime * 0.75f), character2),
                Actions.delay(oneBlockTime * 0.75f),
                myActions.new setWalkDirection(character2, Entity.AnimationType.RUN_UP),
                Actions.addAction(Actions.moveTo(3, 7, oneBlockTime * 1.375f), character2),
                Actions.delay(oneBlockTime * 1.375f),
                myActions.new setWalkDirection(character2, Entity.AnimationType.RUN_RIGHT),
                Actions.addAction(Actions.moveTo(4, 7, oneBlockTime * 0.25f), character2),
                Actions.delay(oneBlockTime * 0.25f),

                myActions.new setWalkDirection(character2, Entity.AnimationType.RUN_RIGHT),
                Actions.addAction(Actions.moveTo(7, 10, oneBlockTime * 1.05f), character2),
                Actions.delay(oneBlockTime * 1.05f),

                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 0.25f), _transitionActor),
                new setFading(true),
                myActions.new setCharacterVisible(character2, false),
                Actions.delay(oneBlockTime),

                Actions.addAction(getCastleChaseScene())
        );
    }

    private Action character1Chase2() {
        oneBlockTime = 0.2f;
        return Actions.sequence(
                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_RIGHT),
                Actions.addAction(Actions.moveTo(3.5f, 10.5f, oneBlockTime * 2.25f), character1),
                Actions.delay(oneBlockTime * 2.25f),

                Actions.addAction(Actions.moveTo(10, 13.5f, oneBlockTime * 1.5f), camactor),

                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_UP),
                Actions.addAction(Actions.moveTo(3.5f, 13.5f, oneBlockTime * 1.5f), character1),
                Actions.delay(oneBlockTime * 1.5f),
                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_RIGHT),
                Actions.addAction(Actions.moveTo(15, 13.5f, oneBlockTime * 5.75f), character1),
                Actions.delay(oneBlockTime * 5.75f),

                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                Actions.delay(oneBlockTime),
                myActions.new setIdleDirection(character1, Entity.Direction.UP),
                Actions.delay(oneBlockTime),
                myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                Actions.delay(oneBlockTime * 2),
                myActions.new setIdleDirection(character1, Entity.Direction.LEFT),
                Actions.delay(oneBlockTime * 2),

                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_DOWN),
                Actions.addAction(Actions.moveTo(15, 4.5f, oneBlockTime * 4.5f), character1),
                Actions.delay(oneBlockTime * 4.5f),

                myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                Actions.delay(oneBlockTime * 2),
                myActions.new setIdleDirection(character1, Entity.Direction.UP),
                Actions.delay(oneBlockTime * 2),
                myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                Actions.delay(oneBlockTime * 2),

                //JUMP
                myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_DOWN),
                Actions.addAction(Actions.moveTo(15, 5.5f, oneBlockTime * 2, Interpolation.exp10Out), character1),
                Actions.delay(oneBlockTime * 2),

                Actions.addAction(Actions.moveTo(15, -1.5f, oneBlockTime * 2, Interpolation.exp10In), character1),
                Actions.delay(oneBlockTime * 2),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 0.25f), _transitionActor),
                new setFading(true),
                Actions.delay(0.5f),

                Actions.addAction(getCourtyardChaseScene())
        );
    }

    private Action character2Chase2() {
        oneBlockTime = 0.3f;
        return Actions.sequence(
                Actions.delay(oneBlockTime * 4),
                myActions.new setCharacterVisible(character2, true),
                myActions.new setWalkDirection(character2, Entity.AnimationType.RUN_RIGHT),
                Actions.addAction(Actions.moveTo(3.5f, 10.5f, oneBlockTime * 1.75f), character2),
                Actions.delay(oneBlockTime * 1.75f),

                myActions.new setWalkDirection(character2, Entity.AnimationType.RUN_UP),
                Actions.addAction(Actions.moveTo(3.5f, 13.5f, oneBlockTime * 1.5f), character2),
                Actions.delay(oneBlockTime * 1.5f),
                myActions.new setWalkDirection(character2, Entity.AnimationType.RUN_RIGHT),
                Actions.addAction(Actions.moveTo(15, 13.5f, oneBlockTime * 5.75f), character2),
                Actions.delay(oneBlockTime * 5.75f),
                myActions.new setWalkDirection(character2, Entity.AnimationType.RUN_DOWN),
                Actions.addAction(Actions.moveTo(15, 4.5f, oneBlockTime * 9.5f), character2),
                Actions.delay(oneBlockTime * 9.5f)
        );
    }

    private Action character1Chase3() {
        oneBlockTime = 0.2f;
        return Actions.sequence(
                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                Actions.addAction(Actions.moveTo(16, 10, oneBlockTime * 1.75f, Interpolation.exp10In), character1),
                Actions.delay(oneBlockTime * 1.75f),
                myActions.new setWalkDirection(character1, Entity.AnimationType.LAY_DOWN),
                Actions.delay(oneBlockTime * 2),
                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                Actions.delay(oneBlockTime),

                myActions.new setIdleDirection(character1, Entity.Direction.UP),
                Actions.delay(oneBlockTime * 2),
                Actions.addAction(Actions.moveBy(0, 0.15f, oneBlockTime, Interpolation.exp10Out), character1),
                Actions.addAction(Actions.moveBy(0, -0.15f, oneBlockTime, Interpolation.exp10In), character1),
                Actions.delay(oneBlockTime * 2),
                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_DOWN),
                Actions.addAction(Actions.moveTo(16, -2, oneBlockTime * 6), character1),
                Actions.delay(oneBlockTime * 6)
        );
    }

    private Action character2Chase3() {
        oneBlockTime = 0.2f;
        return Actions.sequence(
                Actions.delay(oneBlockTime * 11.75f),
                myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),
                Actions.addAction(Actions.moveTo(16, 10, oneBlockTime * 1.75f, Interpolation.exp10In), character2),
                Actions.delay(oneBlockTime * 2.25f),
                myActions.new setWalkDirection(character2, Entity.AnimationType.RUN_DOWN),
                Actions.addAction(Actions.moveTo(16, -2, oneBlockTime * 6), character2),

                Actions.addAction(Actions.moveTo(16, 8, oneBlockTime * 3), camactor),
                Actions.delay(oneBlockTime * 2),

                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 0.25f), _transitionActor),
                new setFading(true),
                Actions.delay(oneBlockTime),

                Actions.addAction(getGuardsSurroundScene())

        );
    }

    private Action charactersGuardSurround() {
        oneBlockTime = 0.4f;
        return Actions.sequence(
                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                Actions.delay(oneBlockTime),
                myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_DOWN),
                Actions.addAction(Actions.moveTo(37.5f, 25, oneBlockTime * 2), character1),
                Actions.delay(oneBlockTime * 2),
                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_UP),
                Actions.addAction(Actions.moveTo(37.5f, 30, oneBlockTime * 1.5f), character1),
                Actions.delay(oneBlockTime * 1.5f),

                myActions.new setIdleDirection(character1, Entity.Direction.UP),
                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                Actions.delay(oneBlockTime),
                myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_UP),
                Actions.addAction(Actions.moveTo(37.5f, 29, oneBlockTime * 2), character1),
                Actions.delay(oneBlockTime * 2),
                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_LEFT),
                Actions.addAction(Actions.moveTo(35, 29, oneBlockTime * 1.25f), character1),
                Actions.delay(oneBlockTime * 1.25f),

                myActions.new setIdleDirection(character1, Entity.Direction.LEFT),
                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                Actions.delay(oneBlockTime),
                myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_LEFT),
                Actions.addAction(Actions.moveTo(37.5f, 29, oneBlockTime * 6), character1),
                Actions.delay(oneBlockTime * 6),
                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                Actions.delay(oneBlockTime * 2),
                myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),
                Actions.delay(oneBlockTime * 2),

                myActions.new setCharacterVisible(character2, true),
                myActions.new setWalkDirection(character2, Entity.AnimationType.RUN_DOWN),
                Actions.addAction(Actions.moveTo(37.5f, 37), character2),
                Actions.addAction(Actions.moveTo(37.5f, 32, oneBlockTime * 3), character2),
                myActions.new setIdleDirection(character1, Entity.Direction.DOWN),
                Actions.delay(oneBlockTime * 3),
                myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),
                Actions.delay(oneBlockTime * 1.5f),

                myActions.new continueConversation(_playerHUD)
        );
    }

    private Action guardsSurround() {
        oneBlockTime = 0.4f;
        return Actions.sequence(
                myActions.new setWalkDirection(guard1, Entity.AnimationType.RUN_RIGHT),
                Actions.addAction(Actions.moveTo(35, 22, oneBlockTime * 3), guard1),
                myActions.new setWalkDirection(guard2, Entity.AnimationType.RUN_UP),
                Actions.addAction(Actions.moveTo(37.5f, 22, oneBlockTime * 3), guard2),
                myActions.new setWalkDirection(guard3, Entity.AnimationType.RUN_LEFT),
                Actions.addAction(Actions.moveTo(40, 22, oneBlockTime * 3), guard3),
                Actions.delay(oneBlockTime * 3),

                myActions.new setWalkDirection(guard1, Entity.AnimationType.WALK_UP),
                Actions.addAction(Actions.moveTo(35.5f, 26, oneBlockTime * 11), guard1),
                myActions.new setWalkDirection(guard2, Entity.AnimationType.WALK_UP),
                Actions.addAction(Actions.moveTo(37.5f, 26, oneBlockTime * 11), guard2),
                myActions.new setWalkDirection(guard3, Entity.AnimationType.WALK_UP),
                Actions.addAction(Actions.moveTo(39.5f, 26, oneBlockTime * 11), guard3),

                myActions.new setCharacterVisible(guard4, true),
                myActions.new setCharacterVisible(guard5, true),
                myActions.new setCharacterVisible(guard6, true),

                myActions.new setWalkDirection(guard4, Entity.AnimationType.WALK_DOWN),
                Actions.addAction(Actions.moveTo(35.5f, 32, oneBlockTime * 11), guard4),
                myActions.new setWalkDirection(guard5, Entity.AnimationType.WALK_DOWN),
                Actions.addAction(Actions.moveTo(37.5f, 32, oneBlockTime * 11), guard5),
                myActions.new setWalkDirection(guard6, Entity.AnimationType.WALK_DOWN),
                Actions.addAction(Actions.moveTo(39.5f, 32, oneBlockTime * 11), guard6),
                Actions.delay(oneBlockTime * 6),

                myActions.new setCharacterVisible(guard7, true),
                myActions.new setCharacterVisible(guard8, true),

                myActions.new setWalkDirection(guard7, Entity.AnimationType.WALK_RIGHT),
                Actions.addAction(Actions.moveTo(34.5f, 29, oneBlockTime * 5), guard7),
                myActions.new setWalkDirection(guard8, Entity.AnimationType.WALK_LEFT),
                Actions.addAction(Actions.moveTo(40.5f, 29, oneBlockTime * 5), guard8),
                Actions.delay(oneBlockTime * 5),

                myActions.new setWalkDirection(guard1, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard2, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard3, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard4, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard5, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard6, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard7, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard8, Entity.AnimationType.IDLE),

                Actions.addAction(guardInchForward()),
                Actions.delay(oneBlockTime * 3),
                Actions.addAction(guardInchForward()),
                Actions.delay(oneBlockTime * 3),
                Actions.addAction(guardInchForward()),
                Actions.delay(oneBlockTime)
                );
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

    private Action guardInchForward() {
        return Actions.sequence(
                myActions.new setWalkDirection(guard1, Entity.AnimationType.WALK_UP),
                Actions.addAction(Actions.moveBy(0.3f, 0.6f, oneBlockTime * 2), guard1),

                myActions.new setWalkDirection(guard2, Entity.AnimationType.WALK_UP),
                Actions.addAction(Actions.moveBy(0, 0.6f, oneBlockTime * 2), guard2),

                myActions.new setWalkDirection(guard3, Entity.AnimationType.WALK_UP),
                Actions.addAction(Actions.moveBy(-0.3f, 0.6f, oneBlockTime * 2), guard3),

                myActions.new setWalkDirection(guard4, Entity.AnimationType.WALK_DOWN),
                Actions.addAction(Actions.moveBy(0.3f, -0.6f, oneBlockTime * 2), guard4),

                myActions.new setWalkDirection(guard5, Entity.AnimationType.WALK_DOWN),
                Actions.addAction(Actions.moveBy(0, -0.6f, oneBlockTime * 2), guard5),

                myActions.new setWalkDirection(guard6, Entity.AnimationType.WALK_DOWN),
                Actions.addAction(Actions.moveBy(-0.3f, -0.6f, oneBlockTime * 2), guard6),

                myActions.new setWalkDirection(guard7, Entity.AnimationType.WALK_RIGHT),
                Actions.addAction(Actions.moveBy(0.6f, 0, oneBlockTime * 2), guard7),

                myActions.new setWalkDirection(guard8, Entity.AnimationType.WALK_LEFT),
                Actions.addAction(Actions.moveBy(-0.6f, 0, oneBlockTime * 2), guard8),
                Actions.delay(oneBlockTime * 2),

                myActions.new setWalkDirection(guard1, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard2, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard3, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard4, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard5, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard6, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard7, Entity.AnimationType.IDLE),
                myActions.new setWalkDirection(guard8, Entity.AnimationType.IDLE)
                );
    }

    private Action getOpeningCutSceneAction() {
        setupScene01.reset();
        return Actions.sequence(
                Actions.addAction(setupScene01),
                new setFading(true),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 3), _transitionActor),
                Actions.delay(3),
                new setFading(false),
                Actions.run(
                        new Runnable() {
                            @Override
                            public void run() {
                                _playerHUD.loadConversationForCutScene("RPGGame/maps/Game/Text/Dialog/Chapter_1.json", thisScreen);
                                _playerHUD.doConversation();
                                // NOTE: This just kicks off the conversation. The actions in the conversation are handled in the onNotify() function.
                            }
                        }),
                Actions.delay(3)
        );
    }

    private Action getCastleChaseScene(){
        setupCastleChaseScene.reset();
        return Actions.sequence(
                Actions.addAction(setupCastleChaseScene),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 0.5f), _transitionActor),
                Actions.delay(0.5f),
                new setFading(false),
                Actions.parallel(
                        character1Chase2(),
                        character2Chase2()
                )
        );
    }

    private Action getCourtyardChaseScene() {
        setupCourtyardChaseScene.reset();
        return Actions.sequence(
                Actions.addAction(setupCourtyardChaseScene),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 0.5f), _transitionActor),
                Actions.delay(0.5f),
                new setFading(false),
                Actions.parallel(
                        character1Chase3(),
                        character2Chase3()
                )
        );
    }

    private Action getGuardsSurroundScene() {
        setupGuardsSurroundScene.reset();
        return Actions.sequence(
                myActions.new setCharacterVisible(character2, false),
                Actions.addAction(setupGuardsSurroundScene),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 0.5f), _transitionActor),
                Actions.delay(0.5f),
                new setFading(false),
                myActions.new setWalkDirection(character1, Entity.AnimationType.RUN_DOWN),
                Actions.addAction(Actions.moveTo(37.5f, 24, oneBlockTime * 14.5f), character1),

                myActions.new setEnabledHUD(_playerHUD, false),

                // uncomment to start right from guard surround scene
                // also need to change currentConversationID in the json file to n4
                //myActions.new loadConversation(_playerHUD, "RPGGame/maps/Game/Text/Dialog/Chapter_1.json", thisScreen),

                myActions.new continueConversation(_playerHUD),
                Actions.delay(oneBlockTime * 14.5f),
                myActions.new continueConversation(_playerHUD),
                myActions.new setEnabledHUD(_playerHUD, true)
                );
    }

    private Action getWakeUpScene() {
        setupWakeUpScene.reset();
        return Actions.sequence(
                Actions.addAction(setupWakeUpScene),
                myActions.new setWalkDirection(character1, Entity.AnimationType.FALL_DOWN),
                Actions.delay(0.25f),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 2), _transitionActor),
                Actions.delay(2),
                new setFading(false),

                // uncomment to start right from guard surround scene
                // also need to change currentConversationID in the json file to n21
                //myActions.new loadConversation(_playerHUD, "RPGGame/maps/Game/Text/Dialog/Chapter_1.json", thisScreen),

                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),

                myActions.new setCharacterVisible(misc, true),
                myActions.new setWalkDirection(misc, Entity.AnimationType.SHOCK_ON),
                Actions.delay(emoteOn),
                myActions.new setWalkDirection(misc, Entity.AnimationType.SHOCK_OFF),
                Actions.delay(emoteOff),
                myActions.new setCharacterVisible(misc, false),
                Actions.delay(oneBlockTime),

                myActions.new continueConversation(_playerHUD)
        );
    }

    private Action getPortalRoomScene() {
        setupPortalRoomScene.reset();
        oneBlockTime = 0.3f;
        return Actions.sequence(
                Actions.addAction(setupPortalRoomScene),
                Actions.addAction(Actions.moveTo(3, 1.5f, oneBlockTime * 4.5f), character1),
                Actions.addAction(Actions.moveTo(3, 1.5f, oneBlockTime * 2.5f), character2),

                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, oneBlockTime * 2.5f), _transitionActor),
                Actions.delay(oneBlockTime * 2.5f),
                new setFading(false),

                // uncomment to start right from guard surround scene
                // also need to change currentConversationID in the json file to n114
                //myActions.new loadConversation(_playerHUD, "RPGGame/maps/Game/Text/Dialog/Chapter_1.json", thisScreen),

                myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_RIGHT),
                Actions.addAction(Actions.moveTo(6, 1.5f, oneBlockTime * 3), character2),
                Actions.delay(oneBlockTime * 2),
                myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_RIGHT),
                Actions.addAction(Actions.moveTo(5, 1.5f, oneBlockTime * 2), character1),
                Actions.delay(oneBlockTime * 1),
                myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_UP),
                Actions.addAction(Actions.moveTo(6, 3.5f, oneBlockTime * 2), character2),
                Actions.delay(oneBlockTime * 1),
                myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_UP),
                Actions.addAction(Actions.moveTo(5, 3.5f, oneBlockTime * 2), character1),
                Actions.delay(oneBlockTime * 1),
                myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),
                Actions.delay(oneBlockTime * 1),
                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),

                myActions.new continueConversation(_playerHUD)
        );
    }

    @Override
    public void show() {
        _stage.addAction(getOpeningCutSceneAction());
        //_stage.addAction(getWakeUpScene());

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
        //if( _gameState != GameState.GAME_OVER ){
        //    setGameState(GameState.SAVING);
        //}

        Gdx.input.setInputProcessor(null);
    }


    @Override
    public void render(float delta) {
        baseRender(delta);
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
