package com.smoftware.elmour.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.smoftware.elmour.ElmourGame;
import com.smoftware.elmour.UI.ChapterInputListener;
import com.smoftware.elmour.Utility;
import com.smoftware.elmour.profile.ProfileManager;


/**
 * Created by moenssr on 1/5/2018.
 */

public class StartScreen  extends GameScreen {

    private Stage stage;
    private ElmourGame game;
    private OrthographicCamera camera;
    private Viewport viewport;

    private TextButton continueButton;
    private TextButton newGameButton;
    private TextButton chapterButton;

    public StartScreen(final ElmourGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(ElmourGame.V_WIDTH, ElmourGame.V_HEIGHT, camera);
        stage = new Stage(viewport);

        //creation and layout
        continueButton = new TextButton("Continue", Utility.ELMOUR_UI_SKIN);
        newGameButton = new TextButton("New Game", Utility.ELMOUR_UI_SKIN);
        chapterButton = new TextButton("Chapter", Utility.ELMOUR_UI_SKIN);


        Image title = new Image(new Texture("graphics/Elmour.png"));
        title.setPosition((stage.getWidth() - title.getWidth()) / 2, stage.getHeight() / 2);

        float menuItemWidth = stage.getWidth() / 3f;
        float menuItemHeight = 45;
        float menuItemX = (stage.getWidth() - menuItemWidth) / 2 ;
        float menuItemY = title.getY() - 75;

        continueButton.setWidth(menuItemWidth);
        continueButton.setHeight(menuItemHeight);
        continueButton.setPosition(menuItemX, menuItemY);

        if (!ProfileManager.getInstance().doesProfileExist(ProfileManager.SAVED_GAME_PROFILE))
            continueButton.setVisible(false);

        menuItemY -= menuItemHeight + 10;
        newGameButton.setWidth(menuItemWidth);
        newGameButton.setHeight(menuItemHeight);
        newGameButton.setPosition(menuItemX, menuItemY);

        stage.addActor(title);
        stage.addActor(continueButton);
        stage.addActor(newGameButton);

        if (ElmourGame.DEV_MODE) {
            menuItemY -= menuItemHeight + 10;
            chapterButton.setWidth(menuItemWidth);
            chapterButton.setHeight(menuItemHeight);
            chapterButton.setPosition(menuItemX, menuItemY);
            stage.addActor(chapterButton);
        }

        //Listeners
        continueButton.addListener(new ClickListener() {
                  @Override
                  public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                      return true;
                  }

                  @Override
                  public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                      ProfileManager.getInstance().setCurrentProfile(ProfileManager.SAVED_GAME_PROFILE);
                      game.setScreen(game.getScreenType(ElmourGame.ScreenType.MainGame));
                  }
              }
        );

        newGameButton.addListener(new ClickListener() {
                   @Override
                   public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                       return true;
                   }

                   @Override
                   public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                       if (ProfileManager.getInstance().doesProfileExist(ProfileManager.SAVED_GAME_PROFILE)) {
                           confirmOverwrite();
                       }
                       else {
                           startNewGame();
                       }
                   }
               }
        );

        chapterButton.addListener(new ClickListener() {
                                       @Override
                                       public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                           return true;
                                       }

                                       @Override
                                       public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                           ChapterInputListener listener = new ChapterInputListener(game, stage);
                                           Gdx.input.getTextInput(listener, "Enter Chapter Number", "", "");
                                       }
                                   }
        );
    }

    private void startNewGame() {
        ProfileManager.getInstance().writeProfileToStorage(ProfileManager.NEW_GAME_PROFILE, "", false);
        ProfileManager.getInstance().setCurrentProfile(ProfileManager.NEW_GAME_PROFILE);
        ProfileManager.getInstance().setIsNewProfile(true);

        // To invoke libGDX routines like setScreen or others that need to be executed on the
        // libGDX render thread use postRunnable
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                // Do something on the main thread
                game.setScreen(game.getScreenType(ElmourGame.ScreenType.MainGame));
                // todo:
                //game.setChapterScreen(1);
            }
        });
    }

    private void confirmOverwrite() {
        Gdx.app.log("tag", "confirmOverwrite");
        TextButton btnYes = new TextButton("Yes", Utility.ELMOUR_UI_SKIN, "message_box");
        TextButton btnNo = new TextButton("No", Utility.ELMOUR_UI_SKIN, "message_box");

        final Dialog dialog = new Dialog("", Utility.ELMOUR_UI_SKIN, "message_box"){
            @Override
            public float getPrefWidth() {
                // force dialog width
                return stage.getWidth() / 1.1f;
            }

            @Override
            public float getPrefHeight() {
                // force dialog height
                return 125f;
            }
        };
        dialog.setModal(true);
        dialog.setMovable(false);
        dialog.setResizable(false);

        btnYes.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                startNewGame();
                dialog.cancel();
                dialog.hide();
                //dialog.remove();
                return true;
            }
        });

        btnNo.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                dialog.cancel();
                dialog.hide();
                return true;
            }
        });

        float btnHeight = 30f;
        float btnWidth = 100f;
        Table t = new Table();
        t.row().pad(5, 5, 0, 5);
        // t.debug();

        Label label1 = new Label("Start a new game?", Utility.ELMOUR_UI_SKIN, "message_box");
        dialog.getContentTable().add(label1).padTop(5f);

        t.add(btnYes).width(btnWidth).height(btnHeight);
        t.add(btnNo).width(btnWidth).height(btnHeight);

        dialog.getButtonTable().add(t).center().padBottom(10f);
        dialog.show(stage).setPosition(stage.getWidth() / 2 - dialog.getWidth() / 2, 25);

        dialog.setName("confirmDialog");
        stage.addActor(dialog);
    }

    @Override
    public void render(float delta) {
        if( delta == 0){
            return;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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
