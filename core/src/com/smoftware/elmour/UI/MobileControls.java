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
import com.smoftware.elmour.ElmourGame;

public class MobileControls extends MobileControlsSubject implements Screen {
    private static final String TAG = MobileControls.class.getSimpleName();

    private Stage _stage;
    private Viewport _viewport;
    private Camera _camera;

    public Table buttonTable;
    private FixedThumbpadController touchpad;


    public MobileControls(Camera camera) {
        initMobileControlsSubject();
        _camera = camera;

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
                MobileControlsSubject.notify(null, MobileControlsObserver.MobileControlEvent.A_BUTTON_PRESSED);
                //rightPressed = true;
                //BensRPG.player.handleAButtonPressed();
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                MobileControlsSubject.notify(null, MobileControlsObserver.MobileControlEvent.A_BUTTON_RELEASED);
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
                MobileControlsSubject.notify(null, MobileControlsObserver.MobileControlEvent.B_BUTTON_PRESSED);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                MobileControlsSubject.notify(null, MobileControlsObserver.MobileControlEvent.B_BUTTON_RELEASED);
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
    }

    public Stage getStage() {
        return _stage;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        _stage.act(delta);
        _stage.draw();
        MobileControlsSubject.notify(touchpad.getDirection(), MobileControlsObserver.MobileControlEvent.JOYSTICK_POSITION);
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
        removeAllObservers();
    }
}
