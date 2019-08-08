package com.smoftware.elmour.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.smoftware.elmour.main.ElmourGame;

/**
 * Created by moenssr on 11/5/2018.
 */

public class GameOverScreen extends GameScreen {

    private Stage stage;
    private ElmourGame game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private float delayTime = 0;
    private boolean splashShowing = true;
    ProgressBar bar;

    public GameOverScreen(ElmourGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(ElmourGame.V_WIDTH, ElmourGame.V_HEIGHT, camera);
        stage = new Stage(viewport);

        Image title = new Image(new Texture("graphics/GameOver.png"));
        title.setPosition((stage.getWidth() - title.getWidth()) / 2, stage.getHeight() / 2);

        Image smoftware = new Image(new Texture("graphics/smoftware.png"));
        smoftware.setPosition((stage.getWidth() - smoftware.getWidth()) / 2, 50);

        stage.addActor(title);
        stage.addActor(smoftware);
    }

    @Override
    public void render(float delta) {
        if( delta == 0){
            return;
        }

        // delay used for amount of time to show splash screen
        delayTime += delta;

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();

        if (splashShowing && (Gdx.input.justTouched() || delayTime > 4f)) {
            game.setScreen(game.getScreenType(ElmourGame.ScreenType.StartScreen));
            splashShowing = false;
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().setScreenSize(width, height);
    }

    @Override
    public void show() {
        splashShowing = true;
        delayTime = 0;
        Gdx.input.setInputProcessor(stage);
        stage.getRoot().getColor().a = 0;
        // delay here should match BattleScreen fadeOut time
        stage.getRoot().addAction(Actions.fadeIn(1.0f));
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        stage.clear();
        stage.dispose();
    }
}
