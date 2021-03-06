package com.smoftware.elmour.screens.quests;

/**
 * Created by steve on 4/23/19.
 */


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.smoftware.elmour.entities.EntityConfig;
import com.smoftware.elmour.main.ElmourGame;
import com.smoftware.elmour.entities.Entity;
import com.smoftware.elmour.entities.EntityFactory;
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

public class CloningQuest extends CutSceneBase implements ConversationGraphObserver {
    private static final String TAG = CloningQuest.class.getSimpleName();

    CloningQuest thisScreen;
    String questID;

    private Action cloningMaterialsOpeningScene;
    private Action cloningMaterialsTTJaxon;
    private Action cloningMaterialsGiveJaxonMaterials;
    private Action cloningMaterialsTTTony;

    private AnimatedImage character1;
    private AnimatedImage rick;
    private AnimatedImage justin;
    private AnimatedImage jaxon;
    private AnimatedImage tony;

    private AnimatedImage camactor;
    private AnimatedImage misc;
    private AnimatedImage misc2;
    private AnimatedImage misc3;

    public CloningQuest(ElmourGame game, PlayerHUD playerHUD) {
        super(game, playerHUD);
        thisScreen = this;
        currentPartNumber = "";

        character1 = getAnimatedImage(EntityFactory.EntityName.CHARACTER_1);
        rick = getAnimatedImage(EntityFactory.EntityName.RICK);
        justin = getAnimatedImage(EntityFactory.EntityName.JUSTIN);
        jaxon = getAnimatedImage(EntityFactory.EntityName.JAXON_1);
        tony = getAnimatedImage(EntityFactory.EntityName.TONY);

        camactor = getAnimatedImage(EntityFactory.EntityName.STEVE);
        misc = getAnimatedImage(EntityFactory.EntityName.MISC_ANIMATIONS);
        misc2 = getAnimatedImage(EntityFactory.EntityName.MISC_ANIMATIONS);
        misc3 = getAnimatedImage(EntityFactory.EntityName.MISC_ANIMATIONS);

        camactor.setVisible(false);
        misc.setVisible(false);
        misc.setCurrentAnimationType(Entity.AnimationType.FORCEFIELD);
        misc2.setVisible(false);
        misc2.setCurrentAnimationType(Entity.AnimationType.FORCEFIELD);
        misc3.setVisible(false);
        misc3.setCurrentAnimationType(Entity.AnimationType.FORCEFIELD);

        _followingActor.setPosition(0, 0);

        _stage.addActor(character1);
        _stage.addActor(rick);
        _stage.addActor(justin);
        _stage.addActor(jaxon);
        _stage.addActor(tony);

        _stage.addActor(camactor);
        _stage.addActor(misc);
        _stage.addActor(misc2);
        _stage.addActor(misc3);
        _stage.addActor(_transitionActor);

        //Actions
        _switchScreenToMainAction = new RunnableAction() {
            @Override
            public void run() {
                _game.setScreen(_game.getScreenType(ElmourGame.ScreenType.MainGame));
            }
        };

        cloningMaterialsOpeningScene = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.T1DOOR4, true);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(6, 5);
                keepCamInMap = false;

                rick.setVisible(true);
                rick.setPosition(6.5f, 2.5f);
                rick.setCurrentAnimationType(Entity.AnimationType.IDLE);
                rick.setCurrentDirection(Entity.Direction.UP);

                character1.setVisible(false);
                character1.setPosition(3, 1.33f);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.UP);

                misc.setVisible(true);
                misc.setPosition(1, 7);
                misc.setCurrentAnimationType(Entity.AnimationType.CLONING_COMP);

                misc2.setVisible(false);
                misc2.setPosition(8, 6);
                misc2.setCurrentAnimationType(Entity.AnimationType.SHOCK_OFF);

                misc3.setVisible(false);
                misc3.setCurrentAnimationType(Entity.AnimationType.SHOCK_OFF);

                justin.setVisible(false);

                jaxon.setVisible(false);

                tony.setVisible(false);
            }
        };

        cloningMaterialsTTJaxon = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.ARMORY, true);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(4.5f, 5);
                keepCamInMap = false;

                rick.setVisible(false);

                character1.setVisible(true);
                character1.setPosition(2.5f, 7.1f);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.RIGHT);

                misc.setVisible(false);

                misc2.setVisible(false);

                misc3.setVisible(false);

                justin.setVisible(true);
                justin.setPosition(4.5f, 7.1f);
                justin.setCurrentAnimationType(Entity.AnimationType.IDLE);
                justin.setCurrentDirection(Entity.Direction.DOWN);

                jaxon.setVisible(true);
                jaxon.setPosition(3.5f, 7.1f);
                jaxon.setCurrentAnimationType(Entity.AnimationType.IDLE);
                jaxon.setCurrentDirection(Entity.Direction.LEFT);

                tony.setVisible(false);

                camactor.setPosition(4.5f, 5);

                followActor(camactor);
            }
        };

        cloningMaterialsGiveJaxonMaterials = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.ARMORY, true);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(4.5f, 5);
                keepCamInMap = false;

                rick.setVisible(false);

                character1.setVisible(true);
                character1.setPosition(2.5f, 7.1f);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.RIGHT);

                misc.setVisible(false);

                misc2.setVisible(false);

                misc3.setVisible(false);

                justin.setVisible(false);
                justin.setPosition(7, 7.3f);
                justin.setCurrentAnimationType(Entity.AnimationType.IDLE);
                justin.setCurrentDirection(Entity.Direction.DOWN);

                jaxon.setVisible(true);
                jaxon.setPosition(3.5f, 7.1f);
                jaxon.setCurrentAnimationType(Entity.AnimationType.IDLE);
                jaxon.setCurrentDirection(Entity.Direction.LEFT);

                tony.setVisible(false);
            }
        };

        cloningMaterialsTTTony = new RunnableAction() {
            @Override
            public void run() {
                _playerHUD.hideMessage();
                _mapMgr.loadMap(MapFactory.MapType.M3DOOR1, true);
                _mapMgr.disableCurrentmapMusic();
                setCameraPosition(7, 6);
                keepCamInMap = false;

                rick.setVisible(false);

                character1.setVisible(false);
                character1.setPosition(12, 7.25f);
                character1.setCurrentAnimationType(Entity.AnimationType.IDLE);
                character1.setCurrentDirection(Entity.Direction.UP);

                misc.setVisible(false);

                misc2.setVisible(false);

                misc3.setVisible(false);

                justin.setVisible(false);

                jaxon.setVisible(false);

                tony.setVisible(false);
                tony.setPosition(11, 13.5f);
                tony.setCurrentAnimationType(Entity.AnimationType.IDLE);
                tony.setCurrentDirection(Entity.Direction.DOWN);

                camactor.setPosition(7.5f, 7.75f);

                followActor(camactor);
            }
        };
    }


    private void fadeToMainScreen() {
        _stage.addAction(Actions.sequence(
                new setFading(true),
                Actions.addAction(Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 1), _transitionActor)),
                Actions.delay(1),
                Actions.addAction(_switchScreenToMainAction)
                )
        );
    }

    @Override
    public void onNotify(ConversationGraph graph, ConversationCommandEvent action, String data) {
        Gdx.app.log(TAG, "Got notification " + action.toString());
        String conversationId = data;
        oneBlockTime = 0.3f;
        emoteOn = 0.7f;
        emoteOff = 0.05f;

        switch (action) {
            case WAIT_1000:
                _playerHUD.doConversation(graph.getNextConversationIDFromChoice(conversationId, 0), 1000);
                break;
            case RICK_FAILURE:
                if( shakeCam == null ){
                    shakeCam = new ShakeCamera(_camera.position.x, _camera.position.y,
                            0.02f,
                            0.3f,
                            0.05f,
                            0.0125f,
                            0.98f,
                            0.99f,
                            0.2f);
                }
                _stage.addAction(Actions.sequence(

                        Actions.delay(oneBlockTime * 1),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.PUSH_BUTTON),
                        Actions.delay(oneBlockTime * 4),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.IDLE),
                        myActions.new shakeCam(shakeCam),
                        Actions.delay(oneBlockTime * 4),

                        myActions.new setWalkDirection(misc, Entity.AnimationType.CLONING_COMP_BLINK),
                        myActions.new setCharacterVisible(misc2, true),
                        myActions.new setWalkDirection(misc2, Entity.AnimationType.CLONING_TANK_BLINK),

                        Actions.delay(oneBlockTime * 5),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case RICK_WALK_TO_COMP:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),

                        myActions.new setWalkDirection(rick, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveTo(3.5f, 2.5f, oneBlockTime * 3), rick),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveTo(1.5f, 6.5f, oneBlockTime * 4), rick),
                        Actions.delay(oneBlockTime * 4),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case RICK_THINK:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),
                        Actions.addAction(Actions.moveTo(rick.getX() + emoteX, rick.getY() + emoteY), misc3),

                        myActions.new setWalkDirection(rick, Entity.AnimationType.THINK),

                        myActions.new setCharacterVisible(misc3, true),
                        myActions.new setWalkDirection(misc3, Entity.AnimationType.THINK_ON),
                        Actions.delay(0.24f),
                        myActions.new setWalkDirection(misc3, Entity.AnimationType.THINK_LOOP),
                        Actions.delay(2.1f),
                        myActions.new setWalkDirection(misc3, Entity.AnimationType.THINK_OFF),
                        Actions.delay(0.075f),
                        myActions.new setCharacterVisible(misc3, false),

                        myActions.new setCharacterVisible(character1, true),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setIdleDirection(rick, Entity.Direction.DOWN),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.IDLE),

                        myActions.new setCharacterVisible(misc3, true),
                        myActions.new setWalkDirection(misc3, Entity.AnimationType.IDEA_ON),
                        Actions.delay(0.24f),
                        myActions.new setWalkDirection(misc3, Entity.AnimationType.IDEA_LOOP),
                        Actions.delay(1.4f),
                        myActions.new setWalkDirection(misc3, Entity.AnimationType.IDEA_OFF),
                        Actions.delay(0.075f),
                        myActions.new setCharacterVisible(misc3, false),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case RICK_WALK_AROUND:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),

                        myActions.new setWalkDirection(rick, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveBy(1.5f, 0, oneBlockTime * 3), rick),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setIdleDirection(rick, Entity.Direction.RIGHT),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.THINK),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case RICK_SPOT_CHAR:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),
                        Actions.addAction(Actions.moveTo(rick.getX() + emoteX, rick.getY() + emoteY), misc3),

                        myActions.new setIdleDirection(rick, Entity.Direction.DOWN),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.IDLE),
                        myActions.new setCharacterVisible(misc3, true),
                        myActions.new setWalkDirection(misc3, Entity.AnimationType.SHOCK_ON),
                        Actions.delay(emoteOn),
                        myActions.new setWalkDirection(misc3, Entity.AnimationType.SHOCK_OFF),
                        Actions.delay(emoteOff),
                        myActions.new setCharacterVisible(misc3, false),

                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case RICK_LOOK_AWAY:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(rick, Entity.Direction.RIGHT),
                        Actions.delay(oneBlockTime * 0.5f),
                        myActions.new setIdleDirection(rick, Entity.Direction.UP),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case RICK_LOOK_DOWN:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setIdleDirection(rick, Entity.Direction.RIGHT),
                        Actions.delay(oneBlockTime * 0.5f),
                        myActions.new setIdleDirection(rick, Entity.Direction.DOWN),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case RICK_WALK_DOWN:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),

                        myActions.new setWalkDirection(rick, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveBy(0, -2, oneBlockTime * 4), rick),
                        Actions.delay(oneBlockTime * 4),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case RICK_DESTROY_KEYBOARD:
                    shakeCam = new ShakeCamera(_camera.position.x, _camera.position.y,
                            0.02f,
                            0.4f,
                            0.4f,
                            0.0125f,
                            0.7f,
                            0.7f,
                            0.2f);
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),

                        myActions.new setWalkDirection(rick, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveTo(rick.getX() + 1, 2.5f, oneBlockTime * 3), rick),
                        Actions.delay(oneBlockTime * 3),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveTo(6.5f, 2.5f, oneBlockTime * 3), rick),
                        myActions.new setIdleDirection(character1, Entity.Direction.RIGHT),

                        Actions.delay(oneBlockTime * 3),

                        myActions.new setIdleDirection(rick, Entity.Direction.UP),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.PUSH_BUTTON),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new setCharacterVisible(misc, false),
                        myActions.new setCharacterVisible(misc2, false),
                        Actions.delay(oneBlockTime * 2),

                        Actions.addAction(Actions.moveTo(6.5f, 3), misc),
                        Actions.addAction(Actions.moveTo(6.5f, 3), misc2),
                        Actions.addAction(Actions.moveTo(6.5f, 3), misc3),
                        myActions.new setWalkDirection(misc, Entity.AnimationType.HIDDEN_ITEM_FULL),
                        myActions.new setWalkDirection(misc2, Entity.AnimationType.HIDDEN_ITEM_FULL),
                        myActions.new setWalkDirection(misc3, Entity.AnimationType.HIDDEN_ITEM_FULL),

                        myActions.new shakeCam(shakeCam),
                        myActions.new setCharacterVisible(misc, true),
                        myActions.new setCharacterVisible(misc2, true),
                        myActions.new setCharacterVisible(misc3, true),
                        Actions.addAction(Actions.moveBy(-5, 10, oneBlockTime * 3), misc),
                        Actions.addAction(Actions.moveBy(0, 15, oneBlockTime * 3), misc2),
                        Actions.addAction(Actions.moveBy(5, 10, oneBlockTime * 3), misc3),
                        myActions.new setWalkDirection(rick, Entity.AnimationType.IDLE),

                        Actions.delay(oneBlockTime * 5),
                        myActions.new setIdleDirection(rick, Entity.Direction.LEFT),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;

                //TTJAXON
            case ZOOM_IN:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_RIGHT),
                        myActions.new setWalkDirection(jaxon, Entity.AnimationType.WALK_LEFT),

                        Actions.addAction(Actions.moveBy(-0.5f, 0, oneBlockTime), character1),
                        Actions.addAction(Actions.moveBy(-0.5f, 0, oneBlockTime), jaxon),
                        Actions.addAction(Actions.moveTo(character1.getX() - 0.25f, character1.getY(), oneBlockTime * 0.5f), camactor),

                        new setZoomRate(-0.05f),
                        Actions.delay(oneBlockTime * 0.5f),
                        new setZoomRate(0),


                        Actions.delay(oneBlockTime * 0.5f),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                        myActions.new setWalkDirection(jaxon, Entity.AnimationType.IDLE),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case CHAR1_TEAR:
                _stage.addAction(Actions.sequence(
                        Actions.addAction(Actions.moveTo(character1.getX() + emoteX - 0.15f, character1.getY() + emoteY), misc),

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
            case JAXON_LOOK_DOWN:
                _stage.addAction(Actions.sequence(
                        myActions.new setIdleDirection(jaxon, Entity.Direction.DOWN),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case JAXON_LOOK_LEFT:
                _stage.addAction(Actions.sequence(
                        myActions.new setIdleDirection(jaxon, Entity.Direction.LEFT),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case ZOOM_OUT:
                _stage.addAction(Actions.sequence(
                        Actions.addAction(Actions.moveTo(4.5f, 5, oneBlockTime * 0.5f), camactor),

                        new setZoomRate(0.05f),
                        Actions.delay(oneBlockTime * 0.5f),
                        new setZoomRate(0),

                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;

            //GiveJaxonMaterials
            case JAXON_LOOK_AROUND:
                _stage.addAction(Actions.sequence(
                        myActions.new setIdleDirection(jaxon, Entity.Direction.DOWN),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new continueConversation(_playerHUD)
                        )
                );

                break;
            case JAXON_TAKE_BAG:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(jaxon, Entity.Direction.LEFT),
                        Actions.delay(oneBlockTime),

                        myActions.new setWalkDirection(jaxon, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveBy(-0.4f, 0, oneBlockTime), jaxon),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(jaxon, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),

                        myActions.new setWalkDirection(jaxon, Entity.AnimationType.KNUCKLES_LEFT),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new setWalkDirection(jaxon, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveBy(0.4f, 0, oneBlockTime), jaxon),
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(jaxon, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case JAXON_THINK:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),
                        Actions.addAction(Actions.moveTo(jaxon.getX() + emoteX, jaxon.getY() + emoteY), misc3),

                        myActions.new setWalkDirection(jaxon, Entity.AnimationType.THINK),

                        myActions.new setCharacterVisible(misc3, true),
                        myActions.new setWalkDirection(misc3, Entity.AnimationType.THINK_ON),
                        Actions.delay(0.24f),
                        myActions.new setWalkDirection(misc3, Entity.AnimationType.THINK_LOOP),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case JUSTIN_ENTER:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setWalkDirection(misc3, Entity.AnimationType.THINK_OFF),
                        Actions.delay(0.075f),
                        myActions.new setCharacterVisible(misc3, false),

                        Actions.delay(oneBlockTime),
                        myActions.new setCharacterVisible(justin, true),
                        Actions.delay(oneBlockTime * 2),
                        myActions.new setIdleDirection(justin, Entity.Direction.LEFT),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case JAXON_LOOK_AT_JUSTIN:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(jaxon, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime * 0.5f),
                        myActions.new setIdleDirection(jaxon, Entity.Direction.RIGHT),
                        Actions.delay(oneBlockTime),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case JUSTIN_WALK_DOWN:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(justin, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveBy(0, -1.8f, oneBlockTime * 2.7f), justin),
                        Actions.delay(oneBlockTime * 2.7f),

                        myActions.new setWalkDirection(justin, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveBy(-4, 0, oneBlockTime * 6), justin),
                        Actions.delay(oneBlockTime * 6),
                        myActions.new setIdleDirection(justin, Entity.Direction.UP),
                        myActions.new setWalkDirection(justin, Entity.AnimationType.IDLE),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case JUSTIN_LOOK_AWAY:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(justin, Entity.Direction.DOWN),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;

                //TTTony
            case TONY_WALK_AROUND:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(tony, Entity.AnimationType.WALK_DOWN),
                        Actions.addAction(Actions.moveBy(0, -1.5f, oneBlockTime * 2.5f), tony),
                        Actions.delay(oneBlockTime * 2.5f),
                        myActions.new setWalkDirection(tony, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveTo(9, 12, oneBlockTime * 3), tony),
                        Actions.delay(oneBlockTime * 2),

                        myActions.new setCharacterVisible(character1, true),
                        Actions.delay(oneBlockTime * 1),
                        myActions.new setIdleDirection(tony, Entity.Direction.LEFT),
                        myActions.new setWalkDirection(tony, Entity.AnimationType.IDLE),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case CHAR1_ENTER:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_UP),
                        Actions.addAction(Actions.moveBy(0, 4.75f, oneBlockTime * 5), character1),
                        Actions.delay(oneBlockTime * 4),
                        myActions.new setIdleDirection(tony, Entity.Direction.RIGHT),
                        Actions.delay(oneBlockTime * 1),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime * 0.5f),

                        myActions.new setWalkDirection(character1, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveBy(-1.5f, 0, oneBlockTime * 1.5f), character1),
                        Actions.delay(oneBlockTime * 1.5f),
                        myActions.new setIdleDirection(character1, Entity.Direction.LEFT),
                        myActions.new setWalkDirection(character1, Entity.AnimationType.IDLE),


                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case TONY_WALK_LEFT:
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(tony, Entity.AnimationType.WALK_LEFT),
                        Actions.addAction(Actions.moveBy(-1, 0, oneBlockTime * 1.5f), tony),
                        Actions.delay(oneBlockTime * 2f),
                        myActions.new setIdleDirection(tony, Entity.Direction.LEFT),
                        myActions.new setWalkDirection(tony, Entity.AnimationType.IDLE),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case CHAR1_SHOCK:
                _stage.addAction(Actions.sequence(
                        Actions.addAction(Actions.moveTo(character1.getX() + emoteX, character1.getY() + emoteY), misc),
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
            case TONY_LOOK_RIGHT:
                iterationNumber2 = 4;
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(tony, Entity.Direction.RIGHT),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case TONY_INCH_FORWARD:
                iterationNumber1 += 0.0025f;
                iterationNumber2 += -1;
                _stage.addAction(Actions.sequence(
                        Actions.addAction(Actions.moveBy((((character1.getX() - tony.getX()) / 2) + tony.getX() - camactor.getX()) / iterationNumber2, (tony.getY() - camactor.getY()) / iterationNumber2, oneBlockTime), camactor),
                        myActions.new setWalkDirection(tony, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveBy(0.35f, 0, oneBlockTime), tony),

                        new setZoomRate(-0.005f - iterationNumber1),
                        Actions.delay(oneBlockTime),
                        new setZoomRate(0),
                        myActions.new setWalkDirection(tony, Entity.AnimationType.IDLE),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case TONY_LEAP_FORWARD:
                iterationNumber1 = 0;
                iterationNumber2 = 0;
                _stage.addAction(Actions.sequence(
                        Actions.addAction(Actions.moveTo(character1.getX() - 0.325f, tony.getY() + 0.5f, oneBlockTime * 0.5f), camactor),
                        myActions.new setWalkDirection(tony, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveBy(0.75f, 0, oneBlockTime * 0.5f), tony),

                        new setZoomRate(-0.03125f),
                        Actions.delay(oneBlockTime * 0.5f),
                        new setZoomRate(0),
                        myActions.new setWalkDirection(tony, Entity.AnimationType.IDLE),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;
            case TONY_LOOK_DOWN:
                iterationNumber2 = 4;
                _stage.addAction(Actions.sequence(
                        Actions.delay(oneBlockTime),
                        myActions.new setWalkDirection(tony, Entity.AnimationType.WALK_RIGHT),
                        Actions.addAction(Actions.moveBy(-0.5f, 0, oneBlockTime * 0.5f), tony),

                        new setZoomRate(0.03125f),
                        Actions.delay(oneBlockTime * 0.5f),
                        new setZoomRate(0),

                        myActions.new setWalkDirection(tony, Entity.AnimationType.IDLE),
                        Actions.delay(oneBlockTime),
                        myActions.new setIdleDirection(tony, Entity.Direction.DOWN),

                        myActions.new continueConversation(_playerHUD)
                        )
                );
                break;








            case ACCEPT_QUEST:
                _playerHUD.acceptQuest(questID);

                if (questID.equals("CloningMaterials")) {
                    _playerHUD.setActiveConversationConfigForQuest(EntityFactory.EntityName.RICK, questID, EntityConfig.ConversationType.POST_TASK_DIALOG1);
                }

                fadeToMainScreen();
                break;
            case DECLINE_QUEST:
                String cutsceneName = ElmourGame.ScreenType.CloningQuestScreen.toString() + "_" + currentPartNumber;
                ProfileManager.getInstance().setProperty(cutsceneName, CutSceneObserver.CutSceneStatus.NOT_STARTED);
                fadeToMainScreen();
                break;
            case TASK_COMPLETE_CUTSCENE:
                if (currentPartNumber.equals("GiveJaxonMaterials")) {
                    _playerHUD.setActiveConversationConfigForQuest(EntityFactory.EntityName.JUSTIN, questID, EntityConfig.ConversationType.POST_TASK_DIALOG1);
                }
                else if (currentPartNumber.equals("TTTony")) {
                    _playerHUD.setActiveConversationConfigForQuest(EntityFactory.EntityName.JUSTIN, questID, EntityConfig.ConversationType.ACTIVE_QUEST_DIALOG1);
                }
                fadeToMainScreen();
                break;
        }
    }

    @Override
    public void onNotify(ConversationGraph graph, ConversationCommandEvent action) {
    }

    @Override
    public void onNotify(ConversationGraph graph, ArrayList<ConversationChoice> choices) {
    }

    @Override
    public void onNotify(String value, ConversationCommandEvent event) {
    }

    private Action getCloningMaterialsOpeningScene() {
        cloningMaterialsOpeningScene.reset();
        return Actions.sequence(
                Actions.addAction(cloningMaterialsOpeningScene),
                new setFading(true),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 2), _transitionActor),
                Actions.delay(2),
                Actions.run(
                        new Runnable() {
                            @Override
                            public void run() {
                                _playerHUD.loadConversationForCutScene("RPGGame/maps/Game/Text/Quest_Dialog/CloningMaterials/CloningMaterialsDialog.json", thisScreen);
                                _playerHUD.doConversation();
                                // NOTE: This just kicks off the conversation. The actions in the conversation are handled in the onNotify() function.
                            }
                        }),
                Actions.delay(2)
        );
    }

    private Action getTTJaxon() {
        cloningMaterialsTTJaxon.reset();
        return Actions.sequence(
                Actions.addAction(cloningMaterialsTTJaxon),
                new setFading(true),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 2), _transitionActor),
                Actions.delay(2),
                Actions.run(
                        new Runnable() {
                            @Override
                            public void run() {
                                _playerHUD.loadConversationForCutScene("RPGGame/maps/Game/Text/Quest_Dialog/CloningMaterials/CloningMaterialsTTJaxon.json", thisScreen);
                                _playerHUD.doConversation();
                                // NOTE: This just kicks off the conversation. The actions in the conversation are handled in the onNotify() function.
                            }
                        }),
                Actions.delay(2)
        );
    }

    private Action getGiveJaxonMaterials() {
        cloningMaterialsGiveJaxonMaterials.reset();
        return Actions.sequence(
                Actions.addAction(cloningMaterialsGiveJaxonMaterials),
                new setFading(true),
                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 2), _transitionActor),
                Actions.delay(2),
                Actions.run(
                        new Runnable() {
                            @Override
                            public void run() {
                                _playerHUD.loadConversationForCutScene("RPGGame/maps/Game/Text/Quest_Dialog/CloningMaterials/CloningMaterialsGiveJaxon.json", thisScreen);
                                _playerHUD.doConversation();
                                // NOTE: This just kicks off the conversation. The actions in the conversation are handled in the onNotify() function.
                            }
                        }),
                Actions.delay(2)
        );
    }

    private Action getTTTony() {
        cloningMaterialsTTTony.reset();
        return Actions.sequence(
                Actions.addAction(cloningMaterialsTTTony),
                new setFading(true),
                Actions.addAction(Actions.moveBy(0, 2.5f, 3, Interpolation.fade), camactor),

                Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 2), _transitionActor),
                Actions.delay(3),
                myActions.new setCharacterVisible(tony, true),
                Actions.delay(0.5f),
                Actions.run(
                        new Runnable() {
                            @Override
                            public void run() {
                                _playerHUD.loadConversationForCutScene("RPGGame/maps/Game/Text/Quest_Dialog/CloningMaterials/CloningMaterialsTTTony.json", thisScreen);
                                _playerHUD.doConversation();
                                // NOTE: This just kicks off the conversation. The actions in the conversation are handled in the onNotify() function.
                            }
                        }),
                Actions.delay(1)
        );
    }

    @Override
    public void show() {
        baseShow();

        currentPartNumber = ProfileManager.getInstance().getProperty(ElmourGame.ScreenType.CloningQuestScreen.toString(), String.class);

        if (currentPartNumber.equals("CloningMaterialsOpen")) {
            questID = "CloningMaterials";
            _stage.addAction(getCloningMaterialsOpeningScene());
        }
        else if (currentPartNumber.equals("TTJaxon")) {
            questID = "CloningMaterials";
            _stage.addAction(getTTJaxon());
        }
        else if (currentPartNumber.equals("GiveJaxonMaterials")) {
            questID = "CloningMaterials";
            _stage.addAction(getGiveJaxonMaterials());
        }
        else if (currentPartNumber.equals("TTTony")) {
            questID = "CloningMaterials";
            _stage.addAction(getTTTony());
        }

        baseShow();

        /*
        if( _mapRenderer == null ){
            ProfileManager.getInstance().setProperty("currentMapType", MapFactory.MapType.MAP1.toString()); //todo: is this necessary?
            _mapRenderer = new OrthogonalTiledMapRenderer(_mapMgr.getCurrentTiledMap(), Map.UNIT_SCALE);
        }
        */
    }

    @Override
    public void hide() {
        ProfileManager.getInstance().setProperty(ElmourGame.ScreenType.CloningQuestScreen.toString(), "");
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
        lastCameraPosition = _camera.position.cpy();

        if (_playerHUD != null)
            _playerHUD.pause();
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

