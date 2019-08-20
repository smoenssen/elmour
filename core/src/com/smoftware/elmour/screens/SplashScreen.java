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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.smoftware.elmour.main.ElmourGame;
import com.smoftware.elmour.main.Utility;

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
    private ProgressBar bar;
    private float progressBarWidth;

    public SplashScreen(ElmourGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(ElmourGame.V_WIDTH, ElmourGame.V_HEIGHT, camera);
        stage = new Stage(viewport);

        //creation and layout

        // srm - trying to get a progress bar working

        Skin skin = new Skin();
        Pixmap pixmap = new Pixmap(10, 10, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));

        float margin = 10;
        progressBarWidth = stage.getWidth() - (2 * margin);

        TextureRegionDrawable textureBar = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("controllers/menuButton.png"))));
        ProgressBar.ProgressBarStyle barStyle = new ProgressBar.ProgressBarStyle(skin.newDrawable("white", Color.DARK_GRAY), textureBar);
        barStyle.knobBefore = barStyle.knob;
        bar = new ProgressBar(0, 100, 10, false, barStyle);
        bar.setPosition(margin, margin);
        bar.setSize(progressBarWidth, bar.getPrefHeight());
        //bar.setAnimateDuration(2);
        //bar.setAnimateDuration(1.6f);
        //bar.setBounds(10, 10, progressBarWidth, 20);

        start = Utility.getStartTime();

        Utility.loadFonts();
        numAssets = (float)Utility.numberAssetsQueued();

        Gdx.app.log("TAG", "Loaded fonts in " + Utility.getElapsedTime(start) + " ms");

        start = Utility.getStartTime();

        Image title = new Image(new Texture("graphics/Elmour.png"));
        title.setPosition((stage.getWidth() - title.getWidth()) / 2, stage.getHeight() / 2);

        Image smoftware = new Image(new Texture("graphics/smoftware.png"));
        smoftware.setPosition((stage.getWidth() - smoftware.getWidth()) / 2, 50);

        stage.addActor(title);
        stage.addActor(smoftware);
        stage.addActor(bar);
    }

    long start = 0;
    boolean fontsLoaded = false;
    float numAssets = 0;

    @Override
    public void render(float delta) {
        if( delta == 0){
            return;
        }

        // delay used for amount of time to show splash screen
        delayTime += delta;

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (splashShowing && (Gdx.input.justTouched() || delayTime > 4f)) {
            game.setScreen(game.getScreenType(ElmourGame.ScreenType.StartScreen));
            splashShowing = false;
        }

        float numAssetsInQueue = (float)Utility.numberAssetsQueued();
        if (Utility.numberAssetsQueued() > 0) {
            Utility.updateAssetLoading();
            float progress = 100 - (numAssetsInQueue/numAssets * 100);
            Gdx.app.log("TAG", "progress = " + progress);
            bar.setValue(progress);
        }
        else if (!fontsLoaded) {
            bar.setValue(100);

            Gdx.app.log("TAG", "Finished loading fonts in " + Utility.getElapsedTime(start) + " ms");

            start = Utility.getStartTime();
            Utility.setFonts();
            Gdx.app.log("TAG", "Set fonts in " + Utility.getElapsedTime(start) + " ms");

            start = Utility.getStartTime();
            Utility.initializeElmourUISkin();
            Gdx.app.log("TAG", "Loaded skin in " + Utility.getElapsedTime(start) + " ms");

            start = Utility.getStartTime();
            game.loadScreens();
            Gdx.app.log("TAG", "Loaded screens in " + Utility.getElapsedTime(start) + " ms");

            //game.setScreen(game.getScreenType(ElmourGame.ScreenType.StartScreen));

            fontsLoaded = true;
        }

        stage.act(delta);
        stage.draw();
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
