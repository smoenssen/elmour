package com.smoftware.elmour.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.smoftware.elmour.main.ElmourGame;
import com.smoftware.elmour.main.Utility;

/**
 * Created by steve on 4/6/19.
 */

public class ChapterSplashScreen extends GameScreen {

    private Stage stage;
    private ElmourGame game;
    private ElmourGame.ScreenType screenType;
    private OrthographicCamera camera;
    private Viewport viewport;
    private float delayTime = 0;

    public ChapterSplashScreen(ElmourGame game, ElmourGame.ScreenType screenType, int chapterNumber, String subtext) {
        this.game = game;
        this.screenType = screenType;
        camera = new OrthographicCamera();
        viewport = new FitViewport(ElmourGame.V_WIDTH, ElmourGame.V_HEIGHT, camera);
        stage = new Stage(viewport);

        //Image title = new Image(new Texture("graphics/Elmour.png"));
        //title.setPosition((stage.getWidth() - title.getWidth()) / 2, stage.getHeight() / 2);

        Label title = new Label("Chapter " + chapterNumber, Utility.ELMOUR_UI_SKIN, "chapter_title");
        title.setAlignment(Align.center);
        title.setPosition((stage.getWidth() - title.getWidth()) / 2, stage.getHeight() / 2);

        Label titleSubtext = new Label(subtext, Utility.ELMOUR_UI_SKIN, "chapter_title_subtext");
        titleSubtext.setAlignment(Align.center);
        titleSubtext.setPosition((stage.getWidth() - titleSubtext.getWidth()) / 2, title.getY() - title.getHeight() / 1.5f);

        stage.addActor(title);
        stage.addActor(titleSubtext);
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

        if (delayTime > 3f) {
            game.setScreen(game.getScreenType(screenType));
        }
    }
}
