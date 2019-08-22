package com.smoftware.elmour.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
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
    private ProgressBar bar;
    private float progressBarWidth;
    private boolean fontsLoaded = false;
    private boolean screensLoaded = false;
    private boolean doneWithSplashScreen = false;
    private float numPreLoadedAssets;

    public SplashScreen(ElmourGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(ElmourGame.V_WIDTH, ElmourGame.V_HEIGHT, camera);
        stage = new Stage(viewport);

        Skin skin = new Skin();
        Pixmap pixmap = new Pixmap(10, 10, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));

        float margin = 16;
        progressBarWidth = stage.getWidth() - (2 * margin);

        TextureRegionDrawable textureBar = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("graphics/progress_bar.png"))));
        ProgressBar.ProgressBarStyle barStyle = new ProgressBar.ProgressBarStyle(skin.newDrawable("white", Color.DARK_GRAY), textureBar);
        barStyle.knobBefore = barStyle.knob;
        bar = new ProgressBar(0, 100, 10, false, barStyle);
        bar.setPosition(margin, margin);
        bar.setSize(progressBarWidth, bar.getPrefHeight());

        Utility.preLoadFonts();
        Utility.preLoadMaps();
        //Utility.preLoadSounds();  //todo
        numPreLoadedAssets = (float)Utility.numberAssetsQueued() + ElmourGame.NUM_PRELOAD_SCREENS;
        Gdx.app.log("TAG", "numPreLoadedAssets = " + numPreLoadedAssets);

        Image title = new Image(new Texture("graphics/Elmour.png"));
        title.setPosition((stage.getWidth() - title.getWidth()) / 2, stage.getHeight() / 2);

        Image smoftware = new Image(new Texture("graphics/smoftware.png"));
        smoftware.setPosition((stage.getWidth() - smoftware.getWidth()) / 2, 50);

        stage.addActor(title);
        stage.addActor(smoftware);
        stage.addActor(bar);
    }

    float progress = 0;

    @Override
    public void render(float delta) {
        if (delta == 0 || doneWithSplashScreen) {
            return;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // delay used for amount of time to show splash screen after screens are loaded
        if (screensLoaded) {
            delayTime += delta;
        }

        if (screensLoaded && delayTime > 0.750f) {
            // Uncomment the following lines to go right to cut scenes. This is needed for previous save profile info.
            //ProfileManager.getInstance().setCurrentProfile(ProfileManager.SAVED_GAME_PROFILE);
            //game.setScreen(game.getScreenType(ElmourGame.ScreenType.Chapter1Screen));

            game.setScreen(game.getScreenType(ElmourGame.ScreenType.StartScreen));
            doneWithSplashScreen = true;
        }

        if (Utility.numberAssetsQueued() > 0) {
            float totalNumAssetsLeftToLoad = (float)Utility.numberAssetsQueued() + ElmourGame.NUM_PRELOAD_SCREENS;
            Utility.updateAssetLoading();
            //progress = MathUtils.clamp(100 - (totalNumAssetsLeftToLoad/numPreLoadedAssets * 100), progress, 100);
            progress = 100 - (totalNumAssetsLeftToLoad/numPreLoadedAssets * 100);
            bar.setValue(progress);
            Gdx.app.log("Loading Screen", "progress = " + progress);
        }
        else if (!fontsLoaded) {
            Utility.setFonts();
            Utility.initializeElmourUISkin();
            fontsLoaded = true;
        }
        else if (!screensLoaded) {
            int numScreensLeftToLoad = game.preLoadNextScreen();
            if (numScreensLeftToLoad > 0) {
                bar.setValue(100 - (numScreensLeftToLoad/numPreLoadedAssets * 100));
            }
            else {
                bar.setValue(100);
                screensLoaded = true;
            }
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
