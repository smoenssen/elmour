package com.smoftware.elmour.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.smoftware.elmour.ComponentObserver;
import com.smoftware.elmour.ElmourGame;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.MapManager;
import com.smoftware.elmour.audio.AudioObserver;
import com.smoftware.elmour.audio.AudioSubject;
import com.smoftware.elmour.battle.BattleObserver;
import com.smoftware.elmour.dialog.ConversationGraph;
import com.smoftware.elmour.dialog.ConversationGraphObserver;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.profile.ProfileObserver;

public class MobileControls implements Screen, AudioSubject, ProfileObserver,ComponentObserver,ConversationGraphObserver,StoreInventoryObserver, BattleObserver, InventoryObserver, StatusObserver {
    private static final String TAG = MobileControls.class.getSimpleName();

    private Stage _stage;
    private Viewport _viewport;
    private Camera _camera;
    private Entity _player;

    public Table buttonTable;
    private FixedThumbpadController touchpad;


    public MobileControls(Camera camera, Entity player, MapManager mapMgr) {
        _camera = camera;
        _player = player;

        //_viewport = new ScreenViewport(_camera);
        _viewport = new FitViewport(ElmourGame.V_WIDTH, ElmourGame.V_HEIGHT, camera);
        _stage = new Stage(_viewport);

        buttonTable = new Table();

        // todo: FloatingThumbpadController has an issue with being reset to 0 position if the A or B button are pressed
        touchpad = new FixedThumbpadController();//FloatingThumbpadController();
        _stage.addActor(touchpad.getTouchpad());

        Image rightImg = new Image(new Texture("controllers/touchpadKnob.png"));
        rightImg.setSize(50, 50);
        rightImg.addListener(new InputListener() {

            // Note: these functions are only called once
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Gdx.app.log("tag", "A pressed");
                //rightPressed = true;
                //BensRPG.player.handleAButtonPressed();
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Gdx.app.log("tag", "A released");
                //rightPressed = false;
                //BensRPG.player.handleAButtonReleased();
            }
        });

        Image leftImg = new Image(new Texture("controllers/touchpadKnob.png"));
        leftImg.setSize(50, 50);
        leftImg.addListener(new InputListener() {

            // Note: these functions are only called once
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Gdx.app.log("tag", "B pressed");
                //leftPressed = true;
                //BensRPG.player.handleBButtonPressed();
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Gdx.app.log("tag", "B released");
                //leftPressed = false;
               //BensRPG.player.handleBButtonReleased();
            }
        });

        //int xPad = 8;
        // top, left, bottom, right
        buttonTable.row().pad(0, 16, 16, 4);
        buttonTable.add(leftImg).size(leftImg.getWidth(), leftImg.getHeight());
        buttonTable.add(rightImg).size(rightImg.getWidth(), rightImg.getHeight());

        buttonTable.left().bottom();

        buttonTable.pack();
        Gdx.app.log("tag", String.format("table width = %3.2f", buttonTable.getWidth()));
        buttonTable.setX(ElmourGame.V_WIDTH - buttonTable.getWidth() - 10);

        _stage.addActor(buttonTable);

        //Observers
        _player.registerObserver(this);
    }

    public Stage getStage() {
        return _stage;
    }


    public void updateEntityObservers(){

    }


    @Override
    public void onNotify(ProfileManager profileManager, ProfileEvent event) {

    }

    @Override
    public void onNotify(String value, ComponentEvent event) {

    }

    @Override
    public void onNotify(ConversationGraph graph, ConversationCommandEvent event) {

    }

    @Override
    public void onNotify(String value, StoreInventoryEvent event) {

    }

    @Override
    public void onNotify(int value, StatusEvent event) {

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        _stage.act(delta);
        _stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        _stage.getViewport().update(width, height, true);

    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        _stage.dispose();
    }

    @Override
    public void onNotify(Entity enemyEntity, BattleEvent event) {

    }

    @Override
    public void onNotify(String value, InventoryEvent event) {

    }

    @Override
    public void addObserver(AudioObserver audioObserver) {

    }

    @Override
    public void removeObserver(AudioObserver audioObserver) {

    }

    @Override
    public void removeAllObservers(){
    }

    @Override
    public void notify(AudioObserver.AudioCommand command, AudioObserver.AudioTypeEvent event) {

    }




}
