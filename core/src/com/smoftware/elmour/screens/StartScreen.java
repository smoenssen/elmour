package com.smoftware.elmour.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.smoftware.elmour.UI.devtools.LoadSavedFile;
import com.smoftware.elmour.UI.devtools.ManageSavedFiles;
import com.smoftware.elmour.UI.graphics.AnimatedImage;
import com.smoftware.elmour.entities.Entity;
import com.smoftware.elmour.entities.EntityFactory;
import com.smoftware.elmour.main.ElmourGame;
import com.smoftware.elmour.UI.devtools.ChapterInputListener;
import com.smoftware.elmour.main.Utility;
import com.smoftware.elmour.maps.Map;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.sfx.ParticleEffectActor;
import com.smoftware.elmour.sfx.ParticleEffectFactory;


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

    // Debug
    private Image menuButton;
    private Image menuButtonDown;
    private boolean debugMenuIsVisible;
    private TextButton chapterButton;
    private TextButton loadSaveFileButton;
    private LoadSavedFile loadSavedFile;
    private TextButton manageSavedFilesButton;
    private ManageSavedFiles manageSavedFilesUI;

    private Label message;

    private ParticleEffectActor effect;

    public StartScreen(final ElmourGame game) {
        this.game = game;
        final StartScreen startScreen = this;
        camera = new OrthographicCamera();
        viewport = new FitViewport(ElmourGame.V_WIDTH, ElmourGame.V_HEIGHT, camera);
        stage = new Stage(viewport);

        loadSavedFile = new LoadSavedFile(this.game, stage);

        //creation and layout
        continueButton = new TextButton("Continue", Utility.ELMOUR_UI_SKIN);
        newGameButton = new TextButton("New Game", Utility.ELMOUR_UI_SKIN);

        manageSavedFilesButton = new TextButton("Manage saved files", Utility.ELMOUR_UI_SKIN);
        loadSaveFileButton = new TextButton("Load saved file", Utility.ELMOUR_UI_SKIN);
        chapterButton = new TextButton("Chapter", Utility.ELMOUR_UI_SKIN);

        Image title = new Image(new Texture("graphics/Elmour.png"));
        title.setPosition((stage.getWidth() - title.getWidth()) / 2, stage.getHeight() / 2);

        message = new Label("", Utility.ELMOUR_UI_SKIN, "gray_small");
        message.setAlignment(Align.center);
        message.setPosition((stage.getWidth() - message.getWidth()) / 2, title.getY() - title.getHeight() / 1.5f);
        message.setVisible(false);

        // Effect pool example: https://github.com/libgdx/libgdx/wiki/2D-ParticleEffects
        effect = new ParticleEffectActor(ParticleEffectFactory.getParticleEffect(ParticleEffectFactory.ParticleEffectType.CANDLE_FIRE,
                                title.getX() + 75, title.getY() + title.getHeight() - 15));

        debugMenuIsVisible = false;
        float menuBtnWidth = 50;
        float menuBtnHeight = 20;
        menuButton = new Image(new Texture("controllers/menuButton.png"));
        menuButton.setSize(menuBtnWidth, menuBtnHeight);
        menuButton.setPosition(stage.getWidth() - menuBtnWidth - menuBtnHeight, menuBtnHeight);

        menuButtonDown = new Image(new Texture("controllers/menuButton_down.png"));
        menuButtonDown.setSize(menuBtnWidth, menuBtnHeight);
        menuButtonDown.setPosition(stage.getWidth() - menuBtnWidth - menuBtnHeight, menuBtnHeight);
        menuButtonDown.setVisible(false);

        float menuItemWidth = stage.getWidth() / 3f;
        float menuItemHeight = 45;
        float menuItemX = (stage.getWidth() - menuItemWidth) / 2 ;
        float menuItemY = title.getY() - 75;

        continueButton.setWidth(menuItemWidth);
        continueButton.setHeight(menuItemHeight);
        continueButton.setPosition(menuItemX, menuItemY);
        continueButton.setVisible(false);

        menuItemY -= menuItemHeight + 10;
        newGameButton.setWidth(menuItemWidth);
        newGameButton.setHeight(menuItemHeight);
        newGameButton.setPosition(menuItemX, menuItemY);

        if (!ElmourGame.QUIET_MODE) {
            stage.addActor(title);
            stage.addActor(effect);
        }
        stage.addActor(continueButton);
        stage.addActor(newGameButton);
        stage.addActor(message);

        if (ElmourGame.DEV_MODE) {
            // Debug buttons
            float menuPadding = 12;
            menuItemWidth = stage.getWidth() / 2.75f;
            menuItemHeight = MathUtils.clamp(stage.getHeight() / 7.5f, 0, 45);
            menuItemX = stage.getWidth() - menuItemWidth - menuPadding;
            menuItemY = stage.getHeight() - menuItemHeight - menuPadding;

            loadSaveFileButton.setWidth(menuItemWidth);
            loadSaveFileButton.setHeight(menuItemHeight);
            loadSaveFileButton.setPosition(menuItemX, menuItemY);
            loadSaveFileButton.setVisible(false);

            menuItemY -= menuItemHeight - 2;
            manageSavedFilesButton.setWidth(menuItemWidth);
            manageSavedFilesButton.setHeight(menuItemHeight);
            manageSavedFilesButton.setPosition(menuItemX, menuItemY);
            manageSavedFilesButton.setVisible(false);
            manageSavedFilesUI = new ManageSavedFiles(this.game, stage);

            menuItemY -= menuItemHeight - 2;
            chapterButton.setWidth(menuItemWidth);
            chapterButton.setHeight(menuItemHeight);
            chapterButton.setPosition(menuItemX, menuItemY);
            chapterButton.setVisible(false);

            stage.addActor(loadSaveFileButton);
            stage.addActor(manageSavedFilesButton);
            stage.addActor(chapterButton);
            stage.addActor(menuButtonDown);
            stage.addActor(menuButton);
        }

        //Listeners
        continueButton.addListener(new ClickListener() {
                  @Override
                  public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                      return true;
                  }

                  @Override
                  public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                      continueGame();
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

        manageSavedFilesButton.addListener(new ClickListener() {
                                           @Override
                                           public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                               return true;
                                           }

                                           @Override
                                           public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                               manageSavedFilesUI = new ManageSavedFiles(game, stage);
                                               manageSavedFilesUI.show();
                                               debugMenuIsVisible = false;
                                               hideDebugMenu();
                                           }
                                       }
        );

        loadSaveFileButton.addListener(new ClickListener() {
                                      @Override
                                      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                          return true;
                                      }

                                      @Override
                                      public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                          loadSavedFile = new LoadSavedFile(game, stage);
                                          loadSavedFile.requestInput(startScreen);
                                          debugMenuIsVisible = false;
                                          hideDebugMenu();
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
                                           debugMenuIsVisible = false;
                                           hideDebugMenu();
                                       }
                                   }
        );

        menuButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                menuButtonDown.setVisible(true);
                menuButton.setVisible(false);

                if (!debugMenuIsVisible)
                    showDebugMenu();
                else
                    hideDebugMenu();

                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                menuButtonDown.setVisible(false);
                menuButton.setVisible(true);
                debugMenuIsVisible = !debugMenuIsVisible;
            }
        });
    }

    private void showDebugMenu() {
        loadSaveFileButton.setVisible(true);
        manageSavedFilesButton.setVisible(true);
        chapterButton.setVisible(true);
    }

    private void hideDebugMenu() {
        loadSaveFileButton.setVisible(false);
        manageSavedFilesButton.setVisible(false);
        chapterButton.setVisible(false);
    }

    public void continueGameFromSaveFile(String loadSavedFile) {
        message.setText("Loading custom saved file...");
        message.setVisible(true);

        newGameButton.setVisible(false);
        continueButton.setVisible(false);

        hideDebugMenu();

        ProfileManager.getInstance().setCurrentProfile(loadSavedFile);

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                game.setScreen(game.getScreenType(ElmourGame.ScreenType.MainGame));
            }
        });
    }

    private void continueGame() {
        message.setText("Loading saved file...");
        message.setVisible(true);
        newGameButton.setVisible(false);
        continueButton.setVisible(false);
        hideDebugMenu();

        ProfileManager.getInstance().setCurrentProfile(ProfileManager.SAVED_GAME_PROFILE);

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                game.setScreen(game.getScreenType(ElmourGame.ScreenType.MainGame));
            }
        });
    }

    private void startNewGame() {
        message.setText("Creating new game...");
        message.setVisible(true);
        newGameButton.setVisible(false);
        continueButton.setVisible(false);
        hideDebugMenu();

        ProfileManager.getInstance().writeProfileToStorage(ProfileManager.NEW_GAME_PROFILE, "", false);
        ProfileManager.getInstance().setCurrentProfile(ProfileManager.NEW_GAME_PROFILE);
        ProfileManager.getInstance().setIsNewProfile(true);

        // To invoke libGDX routines like setScreen or others that need to be executed on the
        // libGDX render thread use postRunnable
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                // Do something on the main thread
                if (ElmourGame.DEV_MODE) {
                    game.setScreen(game.getScreenType(ElmourGame.ScreenType.MainGame));
                }
                else {
                    game.setChapterScreen(1);
                }
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
                dialog.remove();
                startNewGame();
                return true;
            }
        });

        btnNo.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                dialog.remove();
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

    protected AnimatedImage getAnimatedImage(EntityFactory.EntityName entityName){
        Entity entity = EntityFactory.getInstance().getEntityByName(entityName);
        return setEntityAnimation(entity);
    }

    protected AnimatedImage setEntityAnimation(Entity entity){
        final AnimatedImage animEntity = new AnimatedImage();
        animEntity.setEntity(entity);
        animEntity.setSize(animEntity.getWidth() * Map.UNIT_SCALE, animEntity.getHeight() * Map.UNIT_SCALE);
        return animEntity;
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
        newGameButton.setVisible(true);

        if (ProfileManager.getInstance().doesProfileExist(ProfileManager.SAVED_GAME_PROFILE)) {
            continueButton.setVisible(true);
        }

        Gdx.input.setInputProcessor(stage);

        effect.getEffect().start();
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
