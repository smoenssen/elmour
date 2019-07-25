package com.smoftware.elmour.screens.chapters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
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
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.screens.CutSceneBase;
import com.smoftware.elmour.sfx.ScreenTransitionAction;

import java.util.ArrayList;

public class Chapter3 extends CutSceneBase implements ConversationGraphObserver {
    private static final String TAG = Chapter3.class.getSimpleName();

    Chapter3 thisScreen;

    private Action setupScene01;

    private AnimatedImage character1;
    private AnimatedImage character2;
    private AnimatedImage rat;
    private AnimatedImage jaxon;
    private AnimatedImage diane;
    private AnimatedImage ophion;

    private AnimatedImage camactor;
    private AnimatedImage misc;
    private AnimatedImage misc2;

    public Chapter3(ElmourGame game, PlayerHUD playerHUD) {
        super(game, playerHUD);
        thisScreen = this;
        currentPartNumber = "";

        character1 = getAnimatedImage(EntityFactory.EntityName.CHARACTER_1);
        character2 = getAnimatedImage(EntityFactory.EntityName.CHARACTER_2);
        rat = getAnimatedImage(EntityFactory.EntityName.RAT);
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
        _stage.addActor(rat);
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
                _mapMgr.loadMap(MapFactory.MapType.MAP1);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(25.5f, 10);

                camactor.setPosition(25.5f, 10);
                camactor.setCurrentAnimationType(Entity.AnimationType.IDLE);
                camactor.setCurrentDirection(Entity.Direction.DOWN);

                character1.setPosition(25, 10);
                character1.setCurrentAnimationType(Entity.AnimationType.WALK_UP);
                character1.setCurrentDirection(Entity.Direction.UP);

                character2.setPosition(26, 10);
                character2.setCurrentAnimationType(Entity.AnimationType.WALK_UP);
                character2.setCurrentDirection(Entity.Direction.DOWN);

                rat.setPosition(32.5f, 13);
                rat.setCurrentAnimationType(Entity.AnimationType.WALK_UP);
                rat.setCurrentDirection(Entity.Direction.UP);

                misc.setPosition(29, 13);
                misc.setCurrentAnimationType(Entity.AnimationType.SHOCK_OFF);

                misc.setVisible(false);
                rat.setVisible(false);

                followActor(camactor);
            }
        };
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
            case CHAR1_BOOK:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(character1, Entity.AnimationType.BOOK),
                        Actions.delay(oneBlockTime),

                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveBy(0, -1.5f, oneBlockTime * 3), character2),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setIdleDirection(character2, Entity.Direction.LEFT),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case CHAR2_STAND_BACK:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(character2, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveBy(0, 1.5f, oneBlockTime * 3), character2),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.BOOK_CLOSE),

                        Actions.delay(oneBlockTime * 3),
                        myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.SPELL_RIGHT),

                        Actions.delay(oneBlockTime),

                        myActions.new setIdleDirection(character2, Entity.Direction.DOWN),
                        myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case THUNDER:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(misc, Entity.AnimationType.THUNDER),
                        myActions.new setIdleDirection(character2, Entity.Direction.RIGHT),

                        myActions.new setCharacterVisible(misc, true),
                        myActions.new setCharacterVisible(rat, true),

                        myActions.new setWalkDirection(rat, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveTo(30, 13, oneBlockTime * 2), rat),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),

                        myActions.new setWalkDirection(rat, Entity.AnimationType.FALL_LEFT),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setCharacterVisible(misc, false),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case RAT_GETS_UP:
                _stage.addAction(Actions.sequence(
                        myActions.new setWalkDirection(rat, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),

                        Actions.addAction(Actions.moveTo(rat.getX() + emoteX - 0.1f, rat.getY() + emoteY), misc),
                        myActions.new setCharacterVisible(misc, true),

                        myActions.new setWalkDirection(misc, Entity.AnimationType.ANGER_ON),
                        Actions.delay(emoteOn),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.ANGER_OFF),
                        Actions.delay(emoteOff),
                        myActions.new setCharacterVisible(misc, false),


                        myActions.new setWalkDirection(rat, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveTo(28, 13, oneBlockTime * 2), rat),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new setWalkDirection(rat, Entity.AnimationType.IDLE),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case CHAR2_LOOK_DOWN:
                _stage.addAction(Actions.sequence(
                        myActions.new setIdleDirection(character2, Entity.Direction.DOWN),
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
            case RAT_ATTACK:
                _stage.addAction(Actions.sequence(

                        myActions.new setCharacterVisible(rat, true),

                        myActions.new setWalkDirection(rat, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveTo(18, 13, oneBlockTime * 4), rat),
                        Actions.delay(oneBlockTime * 4),

                        myActions.new setIdleDirection(character2, Entity.Direction.RIGHT),

                        myActions.new setWalkDirection(rat, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case CHAR2_LOOK_LEFT:
                _stage.addAction(Actions.sequence(
                        myActions.new setIdleDirection(character2, Entity.Direction.LEFT),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
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
        oneBlockTime = 0.3f;
        setupScene01.reset();
        return Actions.sequence(
                Actions.addAction(setupScene01),
                new setFading(true),
                Actions.addAction(Actions.moveBy(0, 3, oneBlockTime * 5), character1),
                Actions.addAction(Actions.moveBy(0, 3, oneBlockTime * 5), character2),
                Actions.addAction(Actions.moveBy(0, 3, oneBlockTime * 5), camactor),

                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 1.5f), _transitionActor),
                Actions.delay(oneBlockTime * 5),
                myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),

                Actions.addAction(Actions.moveBy(0, 1.5f, oneBlockTime * 7.5f / 3), character2),
                Actions.delay(oneBlockTime * 7.5f / 3),

                myActions.new setWalkDirection(character2, Entity.AnimationType.IDLE),
                new setFading(false),

                Actions.run(
                        new Runnable() {
                            @Override
                            public void run() {
                                _playerHUD.loadConversationForCutScene("RPGGame/maps/Game/Text/Dialog/Chapter_3_P1.json", thisScreen);
                                _playerHUD.doConversation();
                                // NOTE: This just kicks off the conversation. The actions in the conversation are handled in the onNotify() function.
                            }
                        })
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

        currentPartNumber = ProfileManager.getInstance().getProperty(ElmourGame.ScreenType.Chapter3Screen.toString(), String.class);

        if (currentPartNumber.equals("P1")) {
            _stage.addAction(getOpeningCutSceneAction());
        }
        else if (currentPartNumber.equals("P2")) {
            //_stage.addAction(getWoodshopScene());
        }
        else if (currentPartNumber.equals("P3")) {
            //_stage.addAction(getOutsideInnScene());
        }
        else if (currentPartNumber.equals("P4")) {
            //_stage.addAction(getLeave());
        }
        else if (currentPartNumber.equals("P5")) {
            //_stage.addAction(getSwordScene());
        }
        else if (currentPartNumber.equals("P6")) {
            //_stage.addAction(getMaceScene());
        }

        // This will be a goal
        //P5
        //_stage.addAction(getSetupScene05());

        baseShow();

        /*
        if( _mapRenderer == null ){
            ProfileManager.getInstance().setProperty("currentMapType", MapFactory.MapType.ELMOUR.toString());   //todo: is this necessary?
            _mapRenderer = new OrthogonalTiledMapRenderer(_mapMgr.getCurrentTiledMap(), Map.UNIT_SCALE);
        }
        */
    }

    @Override
    public void hide() {
        ProfileManager.getInstance().setProperty(ElmourGame.ScreenType.Chapter3Screen.toString(), "");
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
