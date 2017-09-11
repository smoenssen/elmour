package com.smoftware.elmour.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by moenssr on 8/30/2017.
 */

public class ActionButtons {
    private static final String TAG = ActionButtons.class.getSimpleName();

    //todo: these should be statics in another class
    private final float V_WIDTH = 12;//2.4f;//srm
    private final float V_HEIGHT = 8;//1.6f;

    private Viewport viewport;
    private Stage stage;
    private boolean leftPressed = false, rightPressed = false, leftJustPressed = false, rightJustPressed = false;
    private Camera camera;
    private Table buttonTable;

    public ActionButtons(Camera camera){
        this.camera = camera;
        viewport = new ScreenViewport(camera);
        stage = new Stage(viewport);

        buttonTable = new Table();

        Image rightImg = new Image(new Texture("controllers/touchpadKnob.png"));
        rightImg.setSize(25, 25);
        rightImg.addListener(new InputListener() {

            // Note: these functions are only called once
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Gdx.app.debug(TAG, "A pressed");
                rightPressed = true;
                //BensRPG.player.handleAButtonPressed();
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Gdx.app.debug(TAG, "A released");
                rightPressed = false;
                //BensRPG.player.handleAButtonReleased();
            }
        });

        Image leftImg = new Image(new Texture("controllers/touchpadKnob.png"));
        leftImg.setSize(25, 25);
        leftImg.addListener(new InputListener() {

            // Note: these functions are only called once
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Gdx.app.debug(TAG, "B pressed");
                leftPressed = true;
                //BensRPG.player.handleBButtonPressed();
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Gdx.app.debug(TAG, "B released");
                leftPressed = false;
                //BensRPG.player.handleBButtonReleased();
            }
        });

        //int xPad = 8;
        // top, left, bottom, right
        buttonTable.row().pad(0, 8, 8, 2);
        buttonTable.add(leftImg).size(leftImg.getWidth(), leftImg.getHeight());
        buttonTable.add(rightImg).size(rightImg.getWidth(), rightImg.getHeight());

        buttonTable.left().bottom();

        buttonTable.pack();
        Gdx.app.debug(TAG, String.format("table width = %3.2f", buttonTable.getWidth()));
        buttonTable.setX(V_WIDTH - buttonTable.getWidth());

        stage.addActor(buttonTable);
        setStage(stage);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() { return stage; }

    public boolean isLeftPressed() {
        return leftPressed;
    }

    public boolean isRightPressed() {
        return rightPressed;
    }

    public void resize(int width, int height){
        viewport.update(width, height);
    }
}
