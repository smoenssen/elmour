package com.smoftware.elmour.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.smoftware.elmour.ElmourGame;
import com.smoftware.elmour.Utility;

/**
 * Created by moenssr on 1/4/2018.
 */

public class SplashScreen extends GameScreen {

    private Stage stage;
    private ElmourGame game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private float delayTime = 0;
    private boolean splashShowing = true;
    ProgressBar bar;

    public SplashScreen(ElmourGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(ElmourGame.V_WIDTH, ElmourGame.V_HEIGHT, camera);
        stage = new Stage(viewport);

        //creation and layout

        /* srm - trying to get a progress bar working
        Skin skin = new Skin();
        Pixmap pixmap = new Pixmap(10, 10, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));

        TextureRegionDrawable textureBar = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("controllers/menuButton.png"))));
        ProgressBarStyle barStyle = new ProgressBarStyle(skin.newDrawable("white", Color.DARK_GRAY), textureBar);
        barStyle.knobBefore = barStyle.knob;
        bar = new ProgressBar(0, 10, 0.5f, false, barStyle);
        bar.setPosition(10, 10);
        bar.setSize(290, bar.getPrefHeight());
        bar.setAnimateDuration(2);
        */

        //Image title = new Image(new Texture("graphics/Elmour.png"));
        //title.setPosition((stage.getWidth() - title.getWidth()) / 2, stage.getHeight() / 2);

        Image smoftware = new Image(new Texture("graphics/smoftware.png"));
        smoftware.setPosition((stage.getWidth() - smoftware.getWidth()) / 2, 50);

        //stage.addActor(title);
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
        Gdx.input.setInputProcessor(stage);
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
