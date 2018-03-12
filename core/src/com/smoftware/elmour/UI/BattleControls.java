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
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.smoftware.elmour.ElmourGame;

/**
 * Created by steve on 3/10/18.
 */

public class BattleControls extends BattleControlsSubject implements Screen {
    private static final String TAG = BattleControls.class.getSimpleName();

    private Stage _stage;
    private Viewport _viewport;
    private Camera _camera;

    public Table a_b_buttonTable;
    public Table dPad_buttonTable;

    private Image aBtnImage;
    private Image bBtnImage;
    private Image aBtnImageDown;
    private Image bBtnImageDown;

    public BattleControls(Camera camera) {
        initBattleControlsSubject();
        _camera = camera;

        _viewport = new FitViewport(ElmourGame.V_WIDTH, ElmourGame.V_HEIGHT, camera);
        _stage = new Stage(_viewport);

        a_b_buttonTable = new Table();
        dPad_buttonTable = new Table();

        aBtnImageDown = new Image(new Texture("controllers/A_Button_Down.png"));
        aBtnImageDown.setSize(50, 50);
        aBtnImageDown.setVisible(false);

        bBtnImageDown = new Image(new Texture("controllers/B_Button_Down.png"));
        bBtnImageDown.setSize(50, 50);
        bBtnImageDown.setVisible(false);

        aBtnImage = new Image(new Texture("controllers/A_Button.png"));
        aBtnImage.setSize(50, 50);
        aBtnImage.addListener(new InputListener() {

            // Note: these functions are only called once
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                BattleControlsSubject.notify(null, BattleControlsObserver.BattleControlEvent.A_BUTTON_PRESSED);
                aBtnImageDown.setVisible(true);
                aBtnImage.setVisible(false);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                BattleControlsSubject.notify(null, BattleControlsObserver.BattleControlEvent.A_BUTTON_RELEASED);
                aBtnImageDown.setVisible(false);
                aBtnImage.setVisible(true);
            }
        });

        aBtnImage.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                BattleControlsSubject.notify(null, BattleControlsObserver.BattleControlEvent.A_BUTTON_PRESSED);
                aBtnImageDown.setVisible(true);
                aBtnImage.setVisible(false);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                BattleControlsSubject.notify(null, BattleControlsObserver.BattleControlEvent.A_BUTTON_RELEASED);
                aBtnImageDown.setVisible(false);
                aBtnImage.setVisible(true);
            }
        });

        bBtnImage = new Image(new Texture("controllers/B_Button.png"));
        bBtnImage.setSize(50, 50);
        bBtnImage.addListener(new InputListener() {

            // Note: these functions are only called once
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                BattleControlsSubject.notify(null, BattleControlsObserver.BattleControlEvent.B_BUTTON_PRESSED);
                bBtnImageDown.setVisible(true);
                bBtnImage.setVisible(false);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                BattleControlsSubject.notify(null, BattleControlsObserver.BattleControlEvent.B_BUTTON_RELEASED);
                bBtnImageDown.setVisible(false);
                bBtnImage.setVisible(true);
            }
        });

        bBtnImage.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                BattleControlsSubject.notify(null, BattleControlsObserver.BattleControlEvent.B_BUTTON_PRESSED);
                bBtnImageDown.setVisible(true);
                bBtnImage.setVisible(false);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                BattleControlsSubject.notify(null, BattleControlsObserver.BattleControlEvent.B_BUTTON_RELEASED);
                bBtnImageDown.setVisible(false);
                bBtnImage.setVisible(true);
            }
        });

        WidgetGroup groupA = new WidgetGroup();
        groupA.addActor(aBtnImage);
        groupA.addActor(aBtnImageDown);

        WidgetGroup groupB = new WidgetGroup();
        groupB.addActor(bBtnImage);
        groupB.addActor(bBtnImageDown);

        if (ElmourGame.isAndroid()) {
            // top, left, bottom, right
            a_b_buttonTable.row().pad(0, 64, 0, 0);
            a_b_buttonTable.add(groupA).size(aBtnImage.getWidth(), aBtnImage.getHeight());
            a_b_buttonTable.row().pad(0, 0, 120, 80);
            a_b_buttonTable.add(groupB).size(bBtnImage.getWidth(), bBtnImage.getHeight());
        }
        else {
            // top, left, bottom, right
            a_b_buttonTable.row().pad(0, 64, 0, 0);
            a_b_buttonTable.add(groupA).size(aBtnImage.getWidth(), aBtnImage.getHeight());
            a_b_buttonTable.row().pad(0, 0, 160, 80);
            a_b_buttonTable.add(groupB).size(bBtnImage.getWidth(), bBtnImage.getHeight());
        }

        a_b_buttonTable.left().bottom();

        a_b_buttonTable.pack();
        a_b_buttonTable.setX(ElmourGame.V_WIDTH - a_b_buttonTable.getWidth() - 10);

        _stage.addActor(a_b_buttonTable);
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
