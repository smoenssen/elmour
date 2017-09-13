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
import com.smoftware.elmour.*;

public class MobileControls implements Screen {
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
        //_player.registerObserver(this);
    }

    public Stage getStage() {
        return _stage;
    }


    public void updateEntityObservers(){

    }

    @Override
    public void onNotify(Entity entity, MobileControlEvent event, Object data) {

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
    public void addObserver(com.smoftware.elmour.MobileControlsObserver inventoryObserver) {

    }

    @Override
    public void removeObserver(com.smoftware.elmour.MobileControlsObserver inventoryObserver) {

    }

    @Override
    public void removeAllObservers(){
    }

    @Override
    public void notify(Object data, MobileControlEvent event) {

    }
}
