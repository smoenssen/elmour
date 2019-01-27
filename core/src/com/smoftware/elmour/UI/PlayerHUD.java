package com.smoftware.elmour.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.smoftware.elmour.Component;
import com.smoftware.elmour.ComponentObserver;
import com.smoftware.elmour.ElmourGame;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.EntityConfig;
import com.smoftware.elmour.InventoryElement;
import com.smoftware.elmour.Utility;
import com.smoftware.elmour.audio.AudioManager;
import com.smoftware.elmour.audio.AudioObserver;
import com.smoftware.elmour.audio.AudioSubject;
import com.smoftware.elmour.battle.BattleObserver;
import com.smoftware.elmour.dialog.ChoicePopUp;
import com.smoftware.elmour.dialog.ConversationChoice;
import com.smoftware.elmour.dialog.ConversationGraph;
import com.smoftware.elmour.dialog.ConversationGraphObserver;
import com.smoftware.elmour.dialog.PopUpLabel;
import com.smoftware.elmour.dialog.PopUp;
import com.smoftware.elmour.dialog.PopUpObserver;
import com.smoftware.elmour.maps.MapManager;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.profile.ProfileObserver;
import com.smoftware.elmour.quest.QuestGraph;
import com.smoftware.elmour.screens.BattleScreen;
import com.smoftware.elmour.screens.MainGameScreen;
import com.smoftware.elmour.sfx.ClockActor;
import com.smoftware.elmour.sfx.ScreenTransitionAction;
import com.smoftware.elmour.sfx.ScreenTransitionActor;
import com.smoftware.elmour.sfx.ShakeCamera;

import java.util.ArrayList;

public class PlayerHUD implements Screen, AudioSubject,
                                ProfileObserver,
                                ComponentObserver,
                                ConversationGraphObserver,
                                BattleObserver,
                                StatusObserver,
        PopUpObserver {
    private static final String TAG = PlayerHUD.class.getSimpleName();

    ElmourGame game;
    private Stage _stage;
    private Viewport _viewport;
    private Camera _camera;
    private Entity _player;

    private QuestUI _questUI;
    //private BattleUI _battleUI;
    //private SignPopUp signPopUp;
    private PopUp signPopUp;

    private boolean battleScreenTransitionTriggered = false;
    private boolean playerComingFromBattle = false;
    private boolean playerIsInBattle = false;
    private float elapsedTransitionTime = 0;
    Image screenSwipe1;
    Image screenSwipe2;
    Image screenSwipe3;
    Image screenSwipe4;
    Image screenSwipe5;
    Image screenSwipe6;
    Image screenSwipe7;
    Image screenSwipe8;
    Image screenSwipe9;
    Image screenSwipe10;

    private PopUp conversationPopUp;
    private PopUpLabel popUpLabel;
    private ChoicePopUp choicePopUp1;
    private ChoicePopUp choicePopUp2;
    private ChoicePopUp choicePopUp3;
    private ChoicePopUp choicePopUp4;
    private int numVisibleChoices;
    private boolean isThereAnActiveHiddenChoice;
    private String nextConversationId;
    private boolean isCurrentConversationDone;
    private boolean isDelayedPopUp;
    private float conversationPopUpDelay;
    private boolean isExitingConversation;
    private boolean didSendConversationBeginMsg;
    private boolean didSendConversationDoneMsg;
    private boolean isCutScene;
    private boolean isEnabled;

    private Image menuButton;
    private Image menuButtonDown;
    private boolean menuIsVisible;
    private boolean debugMenuIsVisible;
    private int numberOfMenuItems;
    private TextButton partyButton;
    private TextButton inventoryButton;
    private TextButton optionsButton;
    private TextButton saveButton;
    private TextButton debugButton;

    // for debugging
    private TextButton utilityButton;
    private TextButton noClipModeButton;
    private TextButton adjustInventoryButton;
    private TextButton adjustSpellsPowersButton;
    private TextButton parseXMLButton;

    private Dialog _messageBoxUI;
    private Label _label;
    private Json _json;
    private MapManager _mapMgr;

    private Array<AudioObserver> _observers;
    private ScreenTransitionActor _transitionActor;

    private ShakeCamera _shakeCam;
    private ClockActor _clock;

    private static final String INVENTORY_FULL = "Your inventory is full!";

    public PlayerHUD(final ElmourGame game, Camera camera, final Entity player, MapManager mapMgr) {
        this.game = game;
        _camera = camera;
        _player = player;
        _mapMgr = mapMgr;
        //_viewport = new ScreenViewport(_camera);
        _viewport = new FitViewport(ElmourGame.V_WIDTH, ElmourGame.V_HEIGHT, camera);
        _stage = new Stage(_viewport);
        //_stage.setDebugAll(true);

        //todo: should this be removed????
        game.battleState.addObserver(this);

        isCutScene = false;
        isEnabled = true;

        _observers = new Array<AudioObserver>();
        _transitionActor = new ScreenTransitionActor();

        _shakeCam = new ShakeCamera(0,0, 30.0f);

        _json = new Json();

        _label = new Label("Test", Utility.STATUSUI_SKIN);
        _label.setWrap(true);
        _messageBoxUI = new Dialog("", Utility.STATUSUI_SKIN, "solidbackground");
        _messageBoxUI.setVisible(false);
        _messageBoxUI.getContentTable().add(_label).width(_stage.getWidth()/2).pad(10, 10, 10, 0);
        _messageBoxUI.pack();
        _messageBoxUI.setPosition(_stage.getWidth() / 2 - _messageBoxUI.getWidth() / 2, _stage.getHeight() - _messageBoxUI.getHeight());
        /*
        _messageBoxUI = new Dialog("Message", Utility.STATUSUI_SKIN, "solidbackground"){
            {
                button("OK");
                text(INVENTORY_FULL);
            }
            @Override
            protected void result(final Object object){
                cancel();
                setVisible(false);
            }

        };
        */
        _clock = new ClockActor("0", Utility.STATUSUI_SKIN);
        _clock.setPosition(_stage.getWidth()-_clock.getWidth(),0);
        _clock.setRateOfTime(60);
        _clock.setVisible(true);

        _messageBoxUI.setVisible(false);
        _messageBoxUI.pack();
        _messageBoxUI.setPosition(_stage.getWidth() / 2 - _messageBoxUI.getWidth() / 2, _stage.getHeight() / 2 - _messageBoxUI.getHeight() / 2);

        _questUI = new QuestUI();
        _questUI.setMovable(false);
        _questUI.setVisible(false);
        _questUI.setKeepWithinStage(false);
        _questUI.setPosition(0, _stage.getHeight() / 2);
        _questUI.setWidth(_stage.getWidth());
        _questUI.setHeight(_stage.getHeight() / 2);

        //_battleUI = new BattleUI();
        //_battleUI.setMovable(false);
        //removes all listeners including ones that handle focus
        //_battleUI.clearListeners();
        //_battleUI.setVisible(false);

        signPopUp = new PopUp(PopUp.PopUpType.SIGN);
        if (ElmourGame.isAndroid()) {
            signPopUp.setWidth(_stage.getWidth() / 1.1f);
            signPopUp.setHeight(84);
        }
        else {
            signPopUp.setWidth(_stage.getWidth() / 1.1f);
            signPopUp.setHeight(84);
        }
        signPopUp.setPosition(_stage.getWidth() / 2 - signPopUp.getWidth() / 2, 25);

        signPopUp.setVisible(false);

        conversationPopUp = new PopUp(PopUp.PopUpType.CONVERSATION);
        if (ElmourGame.isAndroid()) {
            conversationPopUp.setWidth(_stage.getWidth() / 1.04f);
            conversationPopUp.setHeight(84);
        }
        else {
            conversationPopUp.setWidth(_stage.getWidth() / 1.04f);
            conversationPopUp.setHeight(84);
        }
        conversationPopUp.setPosition(_stage.getWidth() / 2 - conversationPopUp.getWidth() / 2, 12);
        conversationPopUp.setVisible(false);
        conversationPopUp.setMovable(false);

        popUpLabel = new PopUpLabel();
        if (ElmourGame.isAndroid()) {
            popUpLabel.setWidth(140);
            popUpLabel.setHeight(24);
        }
        else {
            popUpLabel.setWidth(140);
            popUpLabel.setHeight(24);
        }
        popUpLabel.setPosition(conversationPopUp.getX() + 10, conversationPopUp.getY() + conversationPopUp.getHeight());
        popUpLabel.setVisible(false);
        popUpLabel.setMovable(false);

        choicePopUp1 = new ChoicePopUp();
        choicePopUp2 = new ChoicePopUp();
        choicePopUp3 = new ChoicePopUp();
        choicePopUp4 = new ChoicePopUp();

        choicePopUp1.hide();
        choicePopUp2.hide();
        choicePopUp3.hide();
        choicePopUp4.hide();

        choicePopUp1.setMovable(false);
        choicePopUp2.setMovable(false);
        choicePopUp3.setMovable(false);
        choicePopUp4.setMovable(false);

        isDelayedPopUp = false;
        conversationPopUpDelay = 0;
        numVisibleChoices = 0;
        isThereAnActiveHiddenChoice = false;
        isCurrentConversationDone = true;
        isExitingConversation = false;
        didSendConversationBeginMsg = false;
        didSendConversationDoneMsg = false;

        menuIsVisible = false;
        debugMenuIsVisible = false;

        numberOfMenuItems = 4; // update this if number of menu items changes
        float menuBtnWidth = 50;
        float menuBtnHeight = 20;
        menuButton = new Image(new Texture("controllers/menuButton.png"));
        menuButton.setSize(menuBtnWidth, menuBtnHeight);
        menuButton.setPosition((_stage.getWidth() - menuBtnWidth) / 2, menuBtnHeight);

        menuButtonDown = new Image(new Texture("controllers/menuButton_down.png"));
        menuButtonDown.setSize(menuBtnWidth, menuBtnHeight);
        menuButtonDown.setPosition((_stage.getWidth() - menuBtnWidth) / 2, menuBtnHeight);
        menuButtonDown.setVisible(false);

        partyButton = new TextButton("Party", Utility.ELMOUR_UI_SKIN);
        inventoryButton = new TextButton("Inventory", Utility.ELMOUR_UI_SKIN);
        optionsButton = new TextButton("Options", Utility.ELMOUR_UI_SKIN);
        saveButton = new TextButton("Save", Utility.ELMOUR_UI_SKIN);
        debugButton = new TextButton("Debug", Utility.ELMOUR_UI_SKIN);

        utilityButton = new TextButton("Utility", Utility.ELMOUR_UI_SKIN);
        noClipModeButton = new TextButton("No clip for you", Utility.ELMOUR_UI_SKIN);
        adjustInventoryButton = new TextButton("Adjust Inventory", Utility.ELMOUR_UI_SKIN);
        adjustSpellsPowersButton = new TextButton("Adjust Spells", Utility.ELMOUR_UI_SKIN);
        parseXMLButton = new TextButton("Parse XML", Utility.ELMOUR_UI_SKIN);

        float menuPadding = 12;
        float menuItemWidth = _stage.getWidth() / 3f;
        float menuItemHeight = 45;
        float menuItemX = _stage.getWidth() - menuItemWidth - menuPadding;
        float menuItemY = _stage.getHeight() - menuItemHeight - menuPadding;

        partyButton.setWidth(menuItemWidth);
        partyButton.setHeight(menuItemHeight);
        partyButton.setPosition(menuItemX, menuItemY);
        partyButton.setVisible(false);

        utilityButton.setWidth(menuItemWidth);
        utilityButton.setHeight(menuItemHeight);
        utilityButton.setPosition(menuItemX, menuItemY);
        utilityButton.setVisible(false);

        menuItemY -= menuItemHeight - 2;
        inventoryButton.setWidth(menuItemWidth);
        inventoryButton.setHeight(menuItemHeight);
        inventoryButton.setPosition(menuItemX, menuItemY);
        inventoryButton.setVisible(false);

        noClipModeButton.setWidth(menuItemWidth);
        noClipModeButton.setHeight(menuItemHeight);
        noClipModeButton.setPosition(menuItemX, menuItemY);
        noClipModeButton.setVisible(false);

        menuItemY -= menuItemHeight - 2;
        optionsButton.setWidth(menuItemWidth);
        optionsButton.setHeight(menuItemHeight);
        optionsButton.setPosition(menuItemX, menuItemY);
        optionsButton.setVisible(false);

        adjustInventoryButton.setWidth(menuItemWidth);
        adjustInventoryButton.setHeight(menuItemHeight);
        adjustInventoryButton.setPosition(menuItemX, menuItemY);
        adjustInventoryButton.setVisible(false);

        menuItemY -= menuItemHeight - 2;
        saveButton.setWidth(menuItemWidth);
        saveButton.setHeight(menuItemHeight);
        saveButton.setPosition(menuItemX, menuItemY);
        saveButton.setVisible(false);

        adjustSpellsPowersButton.setWidth(menuItemWidth);
        adjustSpellsPowersButton.setHeight(menuItemHeight);
        adjustSpellsPowersButton.setPosition(menuItemX, menuItemY);
        adjustSpellsPowersButton.setVisible(false);

        menuItemY -= menuItemHeight - 2;
        debugButton.setWidth(menuItemWidth);
        debugButton.setHeight(menuItemHeight);
        debugButton.setPosition(menuItemX, menuItemY);
        debugButton.setVisible(false);

        parseXMLButton.setWidth(menuItemWidth);
        parseXMLButton.setHeight(menuItemHeight);
        parseXMLButton.setPosition(menuItemX, menuItemY);
        parseXMLButton.setVisible(false);

        float swipeBarHeight = _stage.getHeight() / 10;
        float swipeBarWidth = 1000;
        screenSwipe1 = new Image(new Texture("graphics/screenSwipe.png"));
        screenSwipe1.setWidth(swipeBarWidth);
        screenSwipe1.setHeight(swipeBarHeight);
        screenSwipe1.setPosition(_stage.getWidth(), _stage.getHeight() - swipeBarHeight);
        screenSwipe1.setVisible(false);

        screenSwipe2 = new Image(new Texture("graphics/screenSwipe.png"));
        screenSwipe2.setWidth(swipeBarWidth);
        screenSwipe2.setHeight(swipeBarHeight);
        screenSwipe2.setPosition(-screenSwipe2.getWidth(), _stage.getHeight() - swipeBarHeight * 2);
        screenSwipe2.setVisible(false);

        screenSwipe3 = new Image(new Texture("graphics/screenSwipe.png"));
        screenSwipe3.setWidth(swipeBarWidth);
        screenSwipe3.setHeight(swipeBarHeight);
        screenSwipe3.setPosition(_stage.getWidth(), _stage.getHeight() - swipeBarHeight * 3);
        screenSwipe3.setVisible(false);

        screenSwipe4 = new Image(new Texture("graphics/screenSwipe.png"));
        screenSwipe4.setWidth(swipeBarWidth);
        screenSwipe4.setHeight(swipeBarHeight);
        screenSwipe4.setPosition(-screenSwipe4.getWidth(), _stage.getHeight() - swipeBarHeight * 4);
        screenSwipe4.setVisible(false);

        screenSwipe5 = new Image(new Texture("graphics/screenSwipe.png"));
        screenSwipe5.setWidth(swipeBarWidth);
        screenSwipe5.setHeight(swipeBarHeight);
        screenSwipe5.setPosition(_stage.getWidth(), _stage.getHeight() - swipeBarHeight * 5);
        screenSwipe5.setVisible(false);

        screenSwipe6 = new Image(new Texture("graphics/screenSwipe.png"));
        screenSwipe6.setWidth(swipeBarWidth);
        screenSwipe6.setHeight(swipeBarHeight);
        screenSwipe6.setPosition(-screenSwipe6.getWidth(), _stage.getHeight() - swipeBarHeight * 6);
        screenSwipe6.setVisible(false);

        screenSwipe7 = new Image(new Texture("graphics/screenSwipe.png"));
        screenSwipe7.setWidth(swipeBarWidth);
        screenSwipe7.setHeight(swipeBarHeight);
        screenSwipe7.setPosition(_stage.getWidth(), _stage.getHeight() - swipeBarHeight * 7);
        screenSwipe7.setVisible(false);

        screenSwipe8 = new Image(new Texture("graphics/screenSwipe.png"));
        screenSwipe8.setWidth(swipeBarWidth);
        screenSwipe8.setHeight(swipeBarHeight);
        screenSwipe8.setPosition(-screenSwipe6.getWidth(), _stage.getHeight() - swipeBarHeight * 8);
        screenSwipe8.setVisible(false);

        screenSwipe9 = new Image(new Texture("graphics/screenSwipe.png"));
        screenSwipe9.setWidth(swipeBarWidth);
        screenSwipe9.setHeight(swipeBarHeight);
        screenSwipe9.setPosition(_stage.getWidth(), _stage.getHeight() - swipeBarHeight * 9);
        screenSwipe9.setVisible(false);

        screenSwipe10 = new Image(new Texture("graphics/screenSwipe.png"));
        screenSwipe10.setWidth(swipeBarWidth);
        screenSwipe10.setHeight(swipeBarHeight);
        screenSwipe10.setPosition(-screenSwipe6.getWidth(), _stage.getHeight() - swipeBarHeight * 10);
        screenSwipe10.setVisible(false);

        //_stage.addActor(_battleUI);
        _stage.addActor(screenSwipe1);
        _stage.addActor(screenSwipe2);
        _stage.addActor(screenSwipe3);
        _stage.addActor(screenSwipe4);
        _stage.addActor(screenSwipe5);
        _stage.addActor(screenSwipe6);
        _stage.addActor(screenSwipe7);
        _stage.addActor(screenSwipe8);
        _stage.addActor(screenSwipe9);
        _stage.addActor(screenSwipe10);
        _stage.addActor(_questUI);
        _stage.addActor(_messageBoxUI);
        _stage.addActor(signPopUp);
        _stage.addActor(conversationPopUp);
        _stage.addActor(popUpLabel);
        _stage.addActor(choicePopUp1);
        _stage.addActor(choicePopUp2);
        _stage.addActor(choicePopUp3);
        _stage.addActor(choicePopUp4);
        _stage.addActor(menuButtonDown);
        _stage.addActor(menuButton);
        _stage.addActor(partyButton);
        _stage.addActor(inventoryButton);
        _stage.addActor(optionsButton);
        _stage.addActor(saveButton);

        if (ElmourGame.DEV_MODE) {
            _stage.addActor(debugButton);
            _stage.addActor(utilityButton);
            _stage.addActor(noClipModeButton);
            _stage.addActor(adjustInventoryButton);
            _stage.addActor(adjustSpellsPowersButton);
            _stage.addActor(parseXMLButton);
        }

        //_battleUI.validate();
        _questUI.validate();
        _messageBoxUI.validate();
        _clock.validate();

        _stage.addActor(_transitionActor);
        _transitionActor.setVisible(false);

        //Observers
        _player.registerObserver(this);
        //_battleUI.getCurrentState().addObserver(this);
        signPopUp.addObserver(this);
        this.addObserver(AudioManager.getInstance());

        //Listeners
        menuButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                menuButtonDown.setVisible(true);
                menuButton.setVisible(false);

                if (!menuIsVisible)
                    showMenu();
                else
                    hideMenu(false);

                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                menuButtonDown.setVisible(false);
                menuButton.setVisible(true);
                menuIsVisible = !menuIsVisible;
            }
        });

        partyButton.addListener(new ClickListener() {
                                    @Override
                                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                        return true;
                                    }

                                    @Override
                                    public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                        Gdx.app.log(TAG, "party button up");
                                        if (touchPointIsInButton(partyButton)) {
                                            hideMenu(true);
                                        }
                                    }
                                }
        );

        inventoryButton.addListener(new ClickListener() {
                                        @Override
                                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                            return true;
                                        }

                                        @Override
                                        public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                            Gdx.app.log(TAG, "inventory button up");
                                            if (touchPointIsInButton(inventoryButton)) {
                                                hideMenu(true);
                                            }
                                        }
                                    }
        );

        optionsButton.addListener(new ClickListener() {
                                      @Override
                                      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                          return true;
                                      }

                                      @Override
                                      public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                          Gdx.app.log(TAG, "options button up");
                                          if (touchPointIsInButton(optionsButton)) {
                                              hideMenu(true);
                                          }
                                      }
                                  }
        );

        saveButton.addListener(new ClickListener() {
                                   @Override
                                   public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                       return true;
                                   }

                                   @Override
                                   public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                       // make sure touch point is still on this button
                                       if (touchPointIsInButton(saveButton)) {
                                           hideMenu(true);
                                           if (ProfileManager.getInstance().doesProfileExist(ProfileManager.SAVED_GAME_PROFILE)) {
                                               //confirmOverwrite();
                                               testInput();
                                           }
                                           else {
                                               ProfileManager.getInstance().setCurrentProfile(ProfileManager.SAVED_GAME_PROFILE);
                                               ProfileManager.getInstance().saveProfile();
                                           }
                                       }
                                   }
                               }
        );

        debugButton.addListener(new ClickListener() {
                                   @Override
                                   public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                       return true;
                                   }

                                   @Override
                                   public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                       hideMenu(true);
                                       utilityButton.setVisible(true);
                                       noClipModeButton.setVisible(true);
                                       adjustInventoryButton.setVisible(true);
                                       adjustSpellsPowersButton.setVisible(true);
                                       parseXMLButton.setVisible(true);
                                       debugMenuIsVisible = true;
                                   }
                               }
        );

        utilityButton.addListener(new ClickListener() {
                                      @Override
                                      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                          return true;
                                      }

                                      @Override
                                      public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                          // make sure touch point is still on this button
                                          if (touchPointIsInButton(utilityButton)) {
                                              hideDebugMenu();

                                              FileHandle file = Gdx.files.local("Ben.csv");

                                              for (int i = 0; i < 1000; i++) {
                                                  int sum = 0;
                                                  double B = 1.05f;

                                                  for (int X = 0; X < 100; X++) {
                                                      int R = MathUtils.random(1, 4);
                                                      sum += (int) (R * Math.pow(B, X));
                                                  }

                                                  String output = String.format("%d\n", sum);
                                                  Gdx.app.log(TAG, output);
                                                  file.writeString(output, true);
                                              }
                                          }
                                      }
                                  }
        );

        noClipModeButton.addListener(new ClickListener() {
                                      @Override
                                      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                          return true;
                                      }

                                      @Override
                                      public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                          // make sure touch point is still on this button
                                          if (touchPointIsInButton(noClipModeButton)) {
                                              hideDebugMenu();
                                              _player.toggleNoClipping();
                                          }
                                      }
                                  }
        );

        adjustInventoryButton.addListener(new ClickListener() {
                                              @Override
                                              public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                                  return true;
                                              }

                                              @Override
                                              public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                                  // make sure touch point is still on this button
                                                  if (touchPointIsInButton(adjustInventoryButton)) {
                                                      hideDebugMenu();
                                                      Gdx.app.log(TAG, "adjustInventoryButton clicked");

                                                      AdjustInventoryInputListener listener = new AdjustInventoryInputListener(_stage);
                                                      Gdx.input.getTextInput(listener, "Enter Quantity", "", "");
                                                  }
                                              }
                                          }
        );

        adjustSpellsPowersButton.addListener(new ClickListener() {
                                              @Override
                                              public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                                  return true;
                                              }

                                              @Override
                                              public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                                  // make sure touch point is still on this button
                                                  if (touchPointIsInButton(adjustSpellsPowersButton)) {
                                                      hideDebugMenu();
                                                      Gdx.app.log(TAG, "adjustSpellsPowersButton clicked");

                                                      AdjustSpellsPowersInputListener listener = new AdjustSpellsPowersInputListener(_stage);
                                                      Gdx.input.getTextInput(listener, "Enter Character", "", "");
                                                  }
                                              }
                                          }
        );

        parseXMLButton.addListener(new ClickListener() {

                                       @Override
                                       public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                           return true;
                                       }

                                       @Override
                                       public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                           Utility.parseConversationXMLFiles("");
                                       }
                                   }
        );

        //Music/Sound loading
        notify(AudioObserver.AudioCommand.MUSIC_LOAD, AudioObserver.AudioTypeEvent.MUSIC_BATTLE);
        notify(AudioObserver.AudioCommand.MUSIC_LOAD, AudioObserver.AudioTypeEvent.MUSIC_LEVEL_UP_FANFARE);
        notify(AudioObserver.AudioCommand.SOUND_LOAD, AudioObserver.AudioTypeEvent.SOUND_COIN_RUSTLE);
        notify(AudioObserver.AudioCommand.SOUND_LOAD, AudioObserver.AudioTypeEvent.SOUND_PLAYER_ATTACK);
        notify(AudioObserver.AudioCommand.SOUND_LOAD, AudioObserver.AudioTypeEvent.SOUND_CREATURE_PAIN);
        notify(AudioObserver.AudioCommand.SOUND_LOAD, AudioObserver.AudioTypeEvent.SOUND_PLAYER_PAIN);
        notify(AudioObserver.AudioCommand.SOUND_LOAD, AudioObserver.AudioTypeEvent.SOUND_PLAYER_WAND_ATTACK);
        notify(AudioObserver.AudioCommand.SOUND_LOAD, AudioObserver.AudioTypeEvent.SOUND_EATING);
        notify(AudioObserver.AudioCommand.SOUND_LOAD, AudioObserver.AudioTypeEvent.SOUND_DRINKING);
    }

    public boolean isPlayerIsInBattle() {
        return playerIsInBattle;
    }

    public void showMessage(String message){
        _label.setText(message);
        Gdx.app.debug(TAG, message);
        _messageBoxUI.pack();
        _messageBoxUI.setVisible(true);
    }

    public void hideMessage() {
        _messageBoxUI.setVisible(false);
    }

    private void hideMenu(boolean setVisibleFlag) {
        partyButton.setVisible(false);
        inventoryButton.setVisible(false);
        optionsButton.setVisible(false);
        saveButton.setVisible(false);
        debugButton.setVisible(false);

        if (setVisibleFlag)
            menuIsVisible = false;
    }

    private void hideDebugMenu() {
        utilityButton.setVisible(false);
        noClipModeButton.setVisible(false);
        adjustInventoryButton.setVisible(false);
        adjustSpellsPowersButton.setVisible(false);
        parseXMLButton.setVisible(false);
        debugMenuIsVisible = false;
    }

    private void showMenu() {
        partyButton.setVisible(true);
        inventoryButton.setVisible(true);
        optionsButton.setVisible(true);
        saveButton.setVisible(true);
        debugButton.setVisible(true);

        // don't set visible flag to true here, it's done in the touchUp handler of the menu button
    }

    private boolean touchPointIsInButton(TextButton button) {
        // Get touch point
        Vector2 screenPos = new Vector2(Gdx.input.getX(), Gdx.input.getY());

        // Convert the touch point into local coordinates
        Vector2 localPos = new Vector2(screenPos);
        localPos = _stage.screenToStageCoordinates(localPos);

        Rectangle buttonRect = new Rectangle(button.getX(), button.getY(), button.getWidth(), button.getHeight());

        return Utility.pointInRectangle(buttonRect, localPos.x, localPos.y);
    }

    private void testInput() {
        TextButton btnOK = new TextButton("OK", Utility.ELMOUR_UI_SKIN, "message_box");
        TextButton btnCancel = new TextButton("Cancel", Utility.ELMOUR_UI_SKIN, "message_box");
        final MyTextField inputField = new MyTextField("", Utility.ELMOUR_UI_SKIN, "battleLarge");

        //_stage.add(usernameTextField);            // <-- Actor now on stage
        //Gdx.input.setInputProcessor(stage);

        final Dialog dialog = new Dialog("", Utility.ELMOUR_UI_SKIN, "message_box"){
            @Override
            public float getPrefWidth() {
                // force dialog width
                return _stage.getWidth() / 1.1f;
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

        btnOK.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                // save profile
                String input = inputField.getText();
                Gdx.app.log(TAG, "Input: " + input);
                dialog.cancel();
                dialog.hide();
                Gdx.input.setOnscreenKeyboardVisible(false);
                return true;
            }
        });

        btnCancel.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                dialog.cancel();
                dialog.hide();
                Gdx.input.setOnscreenKeyboardVisible(false);
                return true;
            }
        });

        float btnHeight = 30f;
        float btnWidth = 100f;
        Table t = new Table();
        t.row().pad(5, 5, 0, 5);
        // t.debug();

        Label label1 = new Label("Name:", Utility.ELMOUR_UI_SKIN, "message_box");
        //label1.setAlignment(Align.center);
        dialog.getContentTable().add(label1).padTop(5f);
        dialog.getContentTable().add(inputField).width(200).padTop(5f);

        t.add(btnOK).width(btnWidth).height(btnHeight);
        t.add(btnCancel).width(btnWidth).height(btnHeight);

        dialog.getButtonTable().add(t).center().padBottom(10f);
        dialog.show(_stage).setPosition(_stage.getWidth() / 2 - dialog.getWidth() / 2, _stage.getHeight() - dialog.getHeight() - 7);

        dialog.setName("inputDialog");
        _stage.addActor(dialog);
    }

    private void confirmOverwrite() {
        TextButton btnYes = new TextButton("Yes", Utility.ELMOUR_UI_SKIN, "message_box");
        TextButton btnNo = new TextButton("No", Utility.ELMOUR_UI_SKIN, "message_box");

        final Dialog dialog = new Dialog("", Utility.ELMOUR_UI_SKIN, "message_box"){
            @Override
            public float getPrefWidth() {
                // force dialog width
                return _stage.getWidth() / 1.1f;
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
                // save profile
                ProfileManager.getInstance().setCurrentProfile(ProfileManager.SAVED_GAME_PROFILE);
                ProfileManager.getInstance().saveProfile();
                dialog.cancel();
                dialog.hide();
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

        Label label1 = new Label("Existing saved data will be\noverwritten! Is that okay?", Utility.ELMOUR_UI_SKIN, "message_box");
        label1.setAlignment(Align.center);
        dialog.getContentTable().add(label1).padTop(5f);

        t.add(btnYes).width(btnWidth).height(btnHeight);
        t.add(btnNo).width(btnWidth).height(btnHeight);

        dialog.getButtonTable().add(t).center().padBottom(10f);
        dialog.show(_stage).setPosition(_stage.getWidth() / 2 - dialog.getWidth() / 2, 25);

        dialog.setName("confirmDialog");
        _stage.addActor(dialog);

    }

    public Stage getStage() {
        return _stage;
    }

    public ClockActor.TimeOfDay getCurrentTimeOfDay(){
        return _clock.getCurrentTimeOfDay();
    }

    public void updateEntityObservers(){
        _mapMgr.unregisterCurrentMapEntityObservers();
        _questUI.initQuests(_mapMgr);
        _mapMgr.registerCurrentMapEntityObservers(this);
    }

    public void addTransitionToScreen(){
        _transitionActor.setVisible(true);
        _stage.addAction(
                Actions.sequence(
                        Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 1), _transitionActor)));
    }

    public boolean isPlayerComingFromBattle() {
        return playerComingFromBattle;
    }

    public void resetPlayerComingFromBattle() {
        playerComingFromBattle = false;
        playerIsInBattle = false;
    }

    private boolean isSignPostInteraction(Entity.Interaction interaction) {
        String name = interaction.toString();
        return (name.contains("SIGN"));
    }

    private boolean isSwitchInteraction(Entity.Interaction interaction) {
        String name = interaction.toString();
        return (name.contains("SWITCH"));
    }

    @Override
    public void onNotify(ProfileManager profileManager, ProfileEvent event) {
        //Gdx.app.log(TAG, "onNotify event = " + event.toString());
        switch(event){
            case PROFILE_LOADED:
                boolean firstTime = profileManager.getIsNewProfile();

                if( firstTime ){
                    _questUI.setQuests(new Array<QuestGraph>());

                }else{
                    Array<QuestGraph> quests = profileManager.getProperty("playerQuests", Array.class);
                    _questUI.setQuests(quests);
                }

                break;
            case SAVING_PROFILE:
                /*
                profileManager.setProperty("playerQuests", _questUI.getQuests());
                profileManager.setProperty("playerInventory", InventoryUI.getInventory(_inventoryUI.getInventorySlotTable()));
                profileManager.setProperty("playerEquipInventory", InventoryUI.getInventory(_inventoryUI.getEquipSlotTable()));
                profileManager.setProperty("currentPlayerGP", _statusUI.getGoldValue() );
                profileManager.setProperty("currentPlayerLevel", _statusUI.getLevelValue() );
                profileManager.setProperty("currentPlayerXP", _statusUI.getXPValue() );
                profileManager.setProperty("currentPlayerXPMax", _statusUI.getXPValueMax() );
                profileManager.setProperty("currentPlayerHP", _statusUI.getHPValue() );
                profileManager.setProperty("currentPlayerHPMax", _statusUI.getHPValueMax() );
                profileManager.setProperty("currentPlayerMP", _statusUI.getMPValue() );
                profileManager.setProperty("currentPlayerMPMax", _statusUI.getMPValueMax() );
                profileManager.setProperty("currentTime", _clock.getTotalTime());*/
                break;
            case CLEAR_CURRENT_PROFILE:
                // set default profile
                profileManager.setProperty("playerQuests", new Array<QuestGraph>());
                profileManager.setProperty("currentPlayerGP", 0 );
                profileManager.setProperty("currentPlayerLevel",0 );
                profileManager.setProperty("currentPlayerXP", 0 );
                profileManager.setProperty("currentPlayerXPMax", 0 );
                profileManager.setProperty("currentPlayerHP", 0 );
                profileManager.setProperty("currentPlayerHPMax", 0 );
                profileManager.setProperty("currentPlayerMP", 0 );
                profileManager.setProperty("currentPlayerMPMax", 0 );
                profileManager.setProperty("currentTime", 0);
                profileManager.setProperty("CHARACTER_1", "Purple Boy");
                profileManager.setProperty("CHARACTER_2", "Girl");
                break;
            default:
                break;
        }
    }

    @Override
    public void onNotify(String value, ComponentEvent event) {
        //Gdx.app.log(TAG, "onNotify event = " + event.toString());
        switch(event) {
            case LOAD_CONVERSATION:
                // this is only done at the beginning of a conversation graph
                if (isCurrentConversationDone) {
                    if (isExitingConversation) {
                        Gdx.app.log(TAG, "Exiting conversation");
                        isExitingConversation = false;
                    }
                    else {
                        Gdx.app.log(TAG, "Loading conversation");
                        Entity npc = _mapMgr.getCurrentSelectedMapEntity();
                        EntityConfig config = npc.getEntityConfig();
/*
                        //Check to see if there is a version loading into properties
                        if (config.getItemTypeID().equalsIgnoreCase(InventoryItem.ItemTypeID.NONE.toString())) {
                            EntityConfig configReturnProperty = ProfileManager.getInstance().getProperty(config.getEntityID(), EntityConfig.class);
                            if (configReturnProperty != null) {
                                config = configReturnProperty;
                            }
                        }
*/
                        loadConversationFromConfig(config);
                    }
                }
                break;
            case SHOW_CONVERSATION:
                // show or continue current conversation
                Entity npcShow = _mapMgr.getCurrentSelectedMapEntity();

                if (npcShow != null) {
                    EntityConfig configShow = npcShow.getEntityConfig();

                    Gdx.app.log(TAG, "SHOW_CONVERSATION");
                    if (configShow.getEntityID().equalsIgnoreCase(conversationPopUp.getCurrentEntityID())) {
                        doConversation();
                    }
                }
                break;
            case HIDE_CONVERSATION:
                EntityConfig configHide = _json.fromJson(EntityConfig.class, value);
                if( configHide.getEntityID().equalsIgnoreCase(conversationPopUp.getCurrentEntityID())) {
                    popUpLabel.setVisible(false);
                    conversationPopUp.hide();
                    conversationPopUp.getCurrentConversationGraph().removeObserver(this);
                }
                break;
            case DID_INITIAL_INTERACTION:
                Entity.Interaction interaction = _json.fromJson(Entity.Interaction.class, value);
                if (isSignPostInteraction(interaction)) {
                    signPopUp.setTextForSignInteraction(interaction);
                }

                if (isSwitchInteraction(interaction)) {
                    signPopUp.setTextForSignInteraction(interaction);
                    isCurrentConversationDone = false;  // this starts the "conversation" and is necessary to prevent player from moving
                }

                break;
            case DID_INTERACTION:
                signPopUp.interact(false);
                break;
            case FINISHED_INTERACTION:
                signPopUp.hide();
                break;
            case QUEST_LOCATION_DISCOVERED:
                String[] string = value.split(Component.MESSAGE_TOKEN);
                String questID = string[0];
                String questTaskID = string[1];

                _questUI.questTaskComplete(questID, questTaskID);
                updateEntityObservers();
                break;
            case ENEMY_SPAWN_LOCATION_CHANGED:
                Gdx.app.debug(TAG, "ENEMY_SPAWN_LOCATION_CHANGED");
                String enemyZoneID = value;
                game.battleState.battleZoneTriggered(Integer.parseInt(enemyZoneID));
                break;
            case PLAYER_HAS_MOVED:
                if( game.battleState.isBattleReady(_player.getActualVelocity(), Float.parseFloat(value)) && !battleScreenTransitionTriggered){
                    battleScreenTransitionTriggered = true;
                    playerIsInBattle = true;
                    game.battleState.setCurrentEnemyList();
                    game.battleState.setCurrentPartyList();
                    game.battleState.getNextTurnCharacter(1.5f); // slight delay so BattleScreen has a chance to set up
                    _mapMgr.disableCurrentmapMusic();
                    notify(AudioObserver.AudioCommand.MUSIC_PLAY_LOOP, AudioObserver.AudioTypeEvent.MUSIC_BATTLE);

                    // transition animation - note: screen transition occurs in render() function.
                    // timing may need to be adjusted in render function
                    screenSwipe1.setPosition(_stage.getWidth(), _stage.getHeight() - screenSwipe1.getHeight());
                    screenSwipe2.setPosition(-screenSwipe2.getWidth(), _stage.getHeight() - screenSwipe1.getHeight() * 2);
                    screenSwipe3.setPosition(_stage.getWidth(), _stage.getHeight() - screenSwipe1.getHeight() * 3);
                    screenSwipe4.setPosition(-screenSwipe4.getWidth(), _stage.getHeight() - screenSwipe1.getHeight() * 4);
                    screenSwipe5.setPosition(_stage.getWidth(), _stage.getHeight() - screenSwipe1.getHeight() * 5);
                    screenSwipe6.setPosition(-screenSwipe6.getWidth(), _stage.getHeight() - screenSwipe1.getHeight() * 6);
                    screenSwipe7.setPosition(_stage.getWidth(), _stage.getHeight() - screenSwipe1.getHeight() * 7);
                    screenSwipe8.setPosition(-screenSwipe6.getWidth(), _stage.getHeight() - screenSwipe1.getHeight() * 8);
                    screenSwipe9.setPosition(_stage.getWidth(), _stage.getHeight() - screenSwipe1.getHeight() * 9);
                    screenSwipe10.setPosition(-screenSwipe6.getWidth(), _stage.getHeight() - screenSwipe1.getHeight() * 10);

                    float transitionTime = 1;
                    screenSwipe1.setVisible(true);
                    screenSwipe2.setVisible(true);
                    screenSwipe3.setVisible(true);
                    screenSwipe4.setVisible(true);
                    screenSwipe5.setVisible(true);
                    screenSwipe6.setVisible(true);
                    screenSwipe7.setVisible(true);
                    screenSwipe8.setVisible(true);
                    screenSwipe9.setVisible(true);
                    screenSwipe10.setVisible(true);

                    screenSwipe1.addAction(Actions.moveBy(-1000, 0, transitionTime));
                    screenSwipe2.addAction(Actions.moveBy(1000, 0, transitionTime));
                    screenSwipe3.addAction(Actions.moveBy(-1000, 0, transitionTime));
                    screenSwipe4.addAction(Actions.moveBy(1000, 0, transitionTime));
                    screenSwipe5.addAction(Actions.moveBy(-1000, 0, transitionTime));
                    screenSwipe6.addAction(Actions.moveBy(1000, 0, transitionTime));
                    screenSwipe7.addAction(Actions.moveBy(-1000, 0, transitionTime));
                    screenSwipe8.addAction(Actions.moveBy(1000, 0, transitionTime));
                    screenSwipe9.addAction(Actions.moveBy(-1000, 0, transitionTime));
                    screenSwipe10.addAction(Actions.moveBy(1000, 0, transitionTime));

                }
                break;
            default:
                break;
        }
    }

    public void switchScreen(final Screen newScreen){
        _stage.getRoot().getColor().a = 1;
        SequenceAction sequenceAction = new SequenceAction();
        sequenceAction.addAction(Actions.fadeOut(3));
        sequenceAction.addAction(Actions.run(new Runnable() {
            @Override
            public void run() {
                game.setScreen(newScreen);
            }
        }));
        _stage.getRoot().addAction(sequenceAction);
    }

    public void doConversation(String nextConversationId, float delay) {
        isDelayedPopUp = true;
        conversationPopUpDelay = delay / 1000;
        this.nextConversationId = nextConversationId;
    }

    public void doConversation() {
        popUpLabel.setVisible(true);

        // this is where all the magic happens
        if (nextConversationId != null) {
            if (conversationPopUp.populateConversationDialogById(nextConversationId) == true) {
                // todo: is this still necessary if not a cut scene
                if (!isCutScene)
                    conversationPopUp.interact(false);
            }
            else {
                conversationPopUp.hide();
                popUpLabel.setVisible(false);
            }
        }

        // don't interact here if there are choices visible
        if (numVisibleChoices == 0)
            conversationPopUp.interact(false);

        // this sets the popup up for the next destination id if NO_CHOICE is active
        // in this case, choicePopUp1 always holds the next destination id
        if (isThereAnActiveHiddenChoice) {
            nextConversationId = choicePopUp1.getChoice().getDestinationId();
            Gdx.app.log(TAG, String.format("1-------next conversation id = %s", nextConversationId));
            if (conversationPopUp.populateConversationDialogById(nextConversationId) == false) {
                // if this is a delayed popup, the nextConversationId was already set
                if (!isDelayedPopUp) {
                    // the current conversation Id is non-interactive, so hide popup and move on to next Id that is in choicePopup1
                    nextConversationId = conversationPopUp.getCurrentConversationGraph().getNextConversationIDFromChoice(nextConversationId, 0);
                    Gdx.app.log(TAG, String.format("2-------next conversation id = %s", nextConversationId));
                }
                conversationPopUp.hide();
                popUpLabel.setVisible(false);
                isThereAnActiveHiddenChoice = false;
            }
        }
        else if (choicePopUp1.getChoice() != null) {
            if (choicePopUp1.getChoice().getConversationCommandEvent() != null) {
                if (choicePopUp1.getChoice().getConversationCommandEvent().equals(ConversationCommandEvent.EXIT_CONVERSATION)) {
                    conversationPopUp.hide();
                    popUpLabel.setVisible(false);
                }
            }
        }
    }

    private void loadConversationFromConfig(EntityConfig config) {
        isCurrentConversationDone = false;
        conversationPopUp.loadConversationFromConfig(config);
        conversationPopUp.getCurrentConversationGraph().addObserver(this);
    }

    public void loadConversationForCutScene(String jsonFilePath, ConversationGraphObserver graphObserver) {
        isCurrentConversationDone = false;
        conversationPopUp.loadConversationFromJson(jsonFilePath);
        conversationPopUp.getCurrentConversationGraph().addObserver(this);
        conversationPopUp.getCurrentConversationGraph().addObserver(graphObserver);
    }

    @Override
    public void onNotify(ConversationGraph graph, ConversationCommandEvent event) {
        //Gdx.app.log(TAG, "onNotify event = " + event.toString());
        switch(event) {
            case EXIT_CONVERSATION:
                //_conversationUI.setVisible(false);
                nextConversationId = null;
                isCurrentConversationDone = true;
                conversationPopUp.endConversation();
                popUpLabel.setVisible(false);
                conversationPopUp.hide();
                isThereAnActiveHiddenChoice = false;
                choicePopUp1.clear();
                choicePopUp2.clear();
                choicePopUp3.clear();
                choicePopUp4.clear();
                isExitingConversation = true;
                _mapMgr.clearCurrentSelectedMapEntity();
                break;
            case ACCEPT_QUEST:
                Entity currentlySelectedEntity = _mapMgr.getCurrentSelectedMapEntity();
                if( currentlySelectedEntity == null ){
                    break;
                }
                EntityConfig config = currentlySelectedEntity.getEntityConfig();

                QuestGraph questGraph = _questUI.loadQuest(config.getQuestConfigPath());

                if( questGraph != null ){
                    //Update conversation dialog
                    config.setConversationConfigPath(QuestUI.RETURN_QUEST);
                    config.setCurrentQuestID(questGraph.getQuestID());
                    ProfileManager.getInstance().setProperty(config.getEntityID(), config);
                    updateEntityObservers();
                }

                _mapMgr.clearCurrentSelectedMapEntity();
                break;
            case RETURN_QUEST:
                Entity returnEntity = _mapMgr.getCurrentSelectedMapEntity();
                if( returnEntity == null ){
                    break;
                }
                EntityConfig configReturn = returnEntity.getEntityConfig();

                EntityConfig configReturnProperty = ProfileManager.getInstance().getProperty(configReturn.getEntityID(), EntityConfig.class);
                if( configReturnProperty == null ) return;

                String questID = configReturnProperty.getCurrentQuestID();

                if( _questUI.isQuestReadyForReturn(questID) ){
                    notify(AudioObserver.AudioCommand.MUSIC_PLAY_ONCE, AudioObserver.AudioTypeEvent.MUSIC_LEVEL_UP_FANFARE);
                    QuestGraph quest = _questUI.getQuestByID(questID);
                    //_statusUI.addXPValue(quest.getXpReward());
                    //_statusUI.addGoldValue(quest.getGoldReward());
                    notify(AudioObserver.AudioCommand.SOUND_PLAY_ONCE, AudioObserver.AudioTypeEvent.SOUND_COIN_RUSTLE);
                    //_inventoryUI.removeQuestItemFromInventory(questID);
                    configReturnProperty.setConversationConfigPath(QuestUI.FINISHED_QUEST);
                    ProfileManager.getInstance().setProperty(configReturnProperty.getEntityID(), configReturnProperty);
                }

                _mapMgr.clearCurrentSelectedMapEntity();

                break;
            case NONE:
                break;
            default:
                break;
        }
    }

    @Override
    public void onNotify(ConversationGraph graph, ConversationCommandEvent event, String conversationId) {

    }

    @Override
    public void onNotify(ConversationGraph graph, ArrayList<ConversationChoice> choices) {
        int choiceNum = 0;
        isThereAnActiveHiddenChoice = false;

        for (ConversationChoice choice : choices) {
            switch(choiceNum++) {
                case 0:
                    choicePopUp1.setChoice(choice);
                    choicePopUp1.setConversationGraph(graph);
                    break;
                case 1:
                    choicePopUp2.setChoice(choice);
                    choicePopUp2.setConversationGraph(graph);
                    break;
                case 2:
                    choicePopUp3.setChoice(choice);
                    choicePopUp3.setConversationGraph(graph);
                    break;
                case 3:
                    choicePopUp4.setChoice(choice);
                    choicePopUp4.setConversationGraph(graph);
                    break;
            }
        }

        switch (choices.size()) {
            case 1:
                choicePopUp1.setWidth(_stage.getWidth() / 2f);
                choicePopUp1.setHeight(80);
                choicePopUp1.setPosition(_stage.getWidth() / 2 - conversationPopUp.getWidth() / 4, _stage.getHeight() - choicePopUp1.getHeight() - 12);

                if (!choicePopUp1.getChoice().getChoicePhrase().equals(ConversationChoice.NO_CHOICE)) {
                    choicePopUp1.show();
                    numVisibleChoices++;
                }
                else {
                    isThereAnActiveHiddenChoice = true;
                }

                break;
            case 2:
                if (ElmourGame.isAndroid()) {
                    choicePopUp1.setWidth(_stage.getWidth() / 1.04f / 2f);
                    choicePopUp1.setHeight(80);
                    choicePopUp2.setWidth(_stage.getWidth() / 1.04f / 2f);
                    choicePopUp2.setHeight(80);
                }
                else {
                    choicePopUp1.setWidth(_stage.getWidth() / 1.04f / 2f);
                    choicePopUp1.setHeight(80);
                    choicePopUp2.setWidth(_stage.getWidth() / 1.04f / 2f);
                    choicePopUp2.setHeight(80);
                }
                choicePopUp1.setPosition(_stage.getWidth() / 2 - conversationPopUp.getWidth() / 2, _stage.getHeight() - choicePopUp2.getHeight() - 12);
                choicePopUp2.setPosition(_stage.getWidth() / 2, _stage.getHeight() - choicePopUp2.getHeight() - 12);
                if (!choicePopUp1.getChoice().getChoicePhrase().equals(ConversationChoice.NO_CHOICE)) {
                    //choicePopUp1.setVisible(true);
                    choicePopUp1.show();
                    numVisibleChoices++;
                }

                if (!choicePopUp2.getChoice().getChoicePhrase().equals(ConversationChoice.NO_CHOICE)) {
                    choicePopUp2.show();
                    numVisibleChoices++;
                }

                break;
            case 3:
                if (ElmourGame.isAndroid()) {
                    choicePopUp1.setWidth(_stage.getWidth() / 1.04f / 3f);
                    choicePopUp1.setHeight(90);
                    choicePopUp2.setWidth(_stage.getWidth() / 1.04f / 3f);
                    choicePopUp2.setHeight(90);
                    choicePopUp3.setWidth(_stage.getWidth() / 1.04f / 3f);
                    choicePopUp3.setHeight(90);
                }
                else {
                    choicePopUp1.setWidth(_stage.getWidth() / 1.04f / 3f);
                    choicePopUp1.setHeight(100);
                    choicePopUp2.setWidth(_stage.getWidth() / 1.04f / 3f);
                    choicePopUp2.setHeight(100);
                    choicePopUp3.setWidth(_stage.getWidth() / 1.04f / 3f);
                    choicePopUp3.setHeight(100);
                }
                choicePopUp1.setPosition(_stage.getWidth() /  -  (1.5f * choicePopUp1.getWidth()) + 13, _stage.getHeight() - choicePopUp1.getHeight() - 12);
                choicePopUp2.setPosition(_stage.getWidth() / 2 - choicePopUp2.getWidth() / 2, _stage.getHeight() - choicePopUp2.getHeight() - 12);
                choicePopUp3.setPosition(_stage.getWidth() / 2 + choicePopUp3.getWidth() / 2, _stage.getHeight() - choicePopUp3.getHeight() - 12);
                if (!choicePopUp1.getChoice().getChoicePhrase().equals(ConversationChoice.NO_CHOICE)) {
                    choicePopUp1.show();
                    numVisibleChoices++;
                }

                if (!choicePopUp2.getChoice().getChoicePhrase().equals(ConversationChoice.NO_CHOICE)) {
                    choicePopUp2.show();
                    numVisibleChoices++;
                }

                if (!choicePopUp3.getChoice().getChoicePhrase().equals(ConversationChoice.NO_CHOICE)) {
                    choicePopUp3.show();
                    numVisibleChoices++;
                }

                break;
            case 4:
                break;
        }
    }

    @Override
    public void onNotify(String value, ConversationCommandEvent event) {
        Gdx.app.log(TAG, "onNotify: ConversationCommandEvent = " + event.toString() + ", value = " + value);

        switch (event) {
            case NEXT_CONVERSATION_ID:
                nextConversationId = value;
                Gdx.app.log(TAG, String.format("------- NEXT_CONVERSATION_ID = %s", nextConversationId));
                break;
            case PLAYER_RESPONSE:
                //if (numVisibleChoices > 0) {
                // interact first so previous popup is cleared
                conversationPopUp.interact(false);
                //}

                // need a slight delay here, otherwise new popup isn't populated
                try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }

                conversationPopUp.populateConversationDialogByText(value, "Me"); //todo: get player name

                choicePopUp1.hide();
                choicePopUp2.hide();
                choicePopUp3.hide();
                choicePopUp4.hide();
                numVisibleChoices = 0;
                isThereAnActiveHiddenChoice = false;

                // //now interact again to show new popup
                conversationPopUp.interact(true);

                break;
            case CHARACTER_NAME:
                popUpLabel.setText(value);
                break;
        }
    }

    @Override
    public void onNotify(int value, StatusEvent event) {
        switch(event) {
            //case UPDATED_GP:
            //    _storeInventoryUI.setPlayerGP(value);
                //ProfileManager.getInstance().setProperty("currentPlayerGP", _statusUI.getGoldValue());
            //    break;
            case UPDATED_HP:
                //ProfileManager.getInstance().setProperty("currentPlayerHP", _statusUI.getHPValue());
                break;
            case UPDATED_LEVEL:
                //ProfileManager.getInstance().setProperty("currentPlayerLevel", _statusUI.getLevelValue());
                break;
            case UPDATED_MP:
                //ProfileManager.getInstance().setProperty("currentPlayerMP", _statusUI.getMPValue());
                break;
            case UPDATED_XP:
                //ProfileManager.getInstance().setProperty("currentPlayerXP", _statusUI.getXPValue());
                break;
            case LEVELED_UP:
                notify(AudioObserver.AudioCommand.MUSIC_PLAY_ONCE, AudioObserver.AudioTypeEvent.MUSIC_LEVEL_UP_FANFARE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onNotify(Entity entity, int value, StatusEvent event) {

    }

    @Override
    public void show() {
        _shakeCam.reset();
    }

    @Override
    public void render(float delta) {
        if (_shakeCam.isCameraShaking()) {
            Vector2 shakeCoords = _shakeCam.getNewShakePosition();
            _camera.position.x = shakeCoords.x + _stage.getWidth() / 2;
            _camera.position.y = shakeCoords.y + _stage.getHeight() / 2;
        }

        if (battleScreenTransitionTriggered) {
            // transition into battle screen after a certain amount of time
            elapsedTransitionTime += delta;

            // note: timing may need to be adjusted in PLAYER_HAS_MOVED notify
            float transitionTime = .75f;
            if (elapsedTransitionTime > transitionTime) {
                game.setScreen(game.getScreenType(ElmourGame.ScreenType.BattleScreen));
                battleScreenTransitionTriggered = false;
            }
        }
        else {
            elapsedTransitionTime = 0;
        }

        if (isCutScene) {
            menuButtonDown.setVisible(false);
            menuButton.setVisible(false);
        }

        if (signPopUp.isReady())
            signPopUp.update();

        if (conversationPopUp.isReady())
            conversationPopUp.update();

        choicePopUp1.update();
        choicePopUp2.update();
        choicePopUp3.update();
        choicePopUp4.update();

        if (_player != null) {
            // send conversation status message if necessary
            if (!isCurrentConversationDone && !didSendConversationBeginMsg) {
                didSendConversationBeginMsg = true;
                didSendConversationDoneMsg = false;
                _player.sendMessage(Component.MESSAGE.CONVERSATION_STATUS, _json.toJson(Entity.ConversationStatus.IN_CONVERSATION));
                Entity entity = _mapMgr.getCurrentSelectedMapEntity();
                if (entity != null) {
                    Float angle = _player.getSelectionAngle();
                    entity.sendMessage(Component.MESSAGE.CONVERSATION_ANGLE, _json.toJson(angle));
                    entity.sendMessage(Component.MESSAGE.CONVERSATION_STATUS, _json.toJson(Entity.ConversationStatus.IN_CONVERSATION));
                }
            }
            else if (isCurrentConversationDone && !didSendConversationDoneMsg) {
                didSendConversationDoneMsg = true;
                didSendConversationBeginMsg = false;
                _player.sendMessage(Component.MESSAGE.CONVERSATION_STATUS, _json.toJson(Entity.ConversationStatus.NOT_IN_CONVERSATION));
                Entity entity = _mapMgr.getCurrentSelectedMapEntity();
                if (entity != null)
                    entity.sendMessage(Component.MESSAGE.CONVERSATION_STATUS, _json.toJson(Entity.ConversationStatus.NOT_IN_CONVERSATION));
            }
        }

        // hide menu if screen is touched anywhere but the menu area or menu button
        if(Gdx.input.justTouched() && isEnabled && (menuIsVisible || debugMenuIsVisible)) {
            // Get the touch point in screen coordinates.
            Vector2 screenPos = new Vector2(Gdx.input.getX(), Gdx.input.getY());

            // Convert the touch point into local coordinates
            Vector2 localPos = new Vector2(screenPos);
            localPos = _stage.screenToStageCoordinates(localPos);

            Rectangle menuButtonRect = new Rectangle(menuButton.getX(), menuButton.getY(), menuButton.getWidth(), menuButton.getHeight());

            // make sure the menu button wasn't touched
            // NOTE: if buttons are added, need to change first rectangle to bottom button
            if (!Utility.pointInRectangle(menuButtonRect, localPos.x, localPos.y)) {
                Vector2 bottomLeftHandCorner;
                if (ElmourGame.DEV_MODE)
                    bottomLeftHandCorner = new Vector2(debugButton.getX(), debugButton.getY());
                else
                    bottomLeftHandCorner = new Vector2(saveButton.getX(), saveButton.getY());

                Rectangle menuAreaRect = new Rectangle(bottomLeftHandCorner.x, bottomLeftHandCorner.y, saveButton.getWidth(), saveButton.getHeight() * numberOfMenuItems);

                // Make sure the menu area wasn't touched
                if (!Utility.pointInRectangle(menuAreaRect, localPos.x, localPos.y)) {
                    hideMenu(true);
                    hideDebugMenu();
                    Gdx.app.log(TAG, "render set menuIsVisible = false");
                }
            }
        }
        else if (Gdx.input.justTouched() && isEnabled && isCutScene && !isDelayedPopUp) {
            Vector3 touchPoint = new Vector3();

            _camera.unproject(touchPoint.set(Gdx.input.getX(),Gdx.input.getY(), 0));
            Rectangle popupRect = new Rectangle(conversationPopUp.getX(), conversationPopUp.getY(), conversationPopUp.getWidth(), conversationPopUp.getHeight());

            // don't interact if popup is hidden (i.e., not listening) or choice(s) are visible
            if (numVisibleChoices == 0 && conversationPopUp.isListening() && Utility.pointInRectangle(popupRect, touchPoint.x, touchPoint.y))
            {
                conversationPopUp.interact(false);

                if (conversationPopUp.isDoneWithCurrentNode())
                    doConversation();
            }
        }
        else if (isDelayedPopUp) {
            conversationPopUpDelay -= delta;
            if (conversationPopUpDelay <= 0) {
                isDelayedPopUp = false;
                doConversation();
            }
        }

        _stage.act(delta);
        _stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        _stage.getViewport().update(width, height, true);
        //_battleUI.validate();
       // _battleUI.resize();
    }

    @Override
    public void pause() {
        /*_battleUI.resetDefaults();*/
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 3), _transitionActor);
    }

    @Override
    public void dispose() {
        _stage.dispose();
    }

    private Timer.Task getSetGameOverScreenTimer(){
        return new Timer.Task() {
            @Override
            public void run() {
                game.setScreen(game.getScreenType(ElmourGame.ScreenType.GameOver));
            }
        };
    }

    private Timer.Task getSetMainGameScreenTimer(){
        return new Timer.Task() {
            @Override
            public void run() {
                game.setScreen(game.getScreenType(ElmourGame.ScreenType.MainGame));
            }
        };
    }

    private void swipeBackToMainGameScreen(float transitionTime) {
        screenSwipe1.setVisible(true);
        screenSwipe2.setVisible(true);
        screenSwipe3.setVisible(true);
        screenSwipe4.setVisible(true);
        screenSwipe5.setVisible(true);
        screenSwipe6.setVisible(true);
        screenSwipe7.setVisible(true);
        screenSwipe8.setVisible(true);
        screenSwipe9.setVisible(true);
        screenSwipe10.setVisible(true);

        screenSwipe1.addAction(Actions.moveBy(-1000, 0, transitionTime));
        screenSwipe2.addAction(Actions.moveBy(1000, 0, transitionTime));
        screenSwipe3.addAction(Actions.moveBy(-1000, 0, transitionTime));
        screenSwipe4.addAction(Actions.moveBy(1000, 0, transitionTime));
        screenSwipe5.addAction(Actions.moveBy(-1000, 0, transitionTime));
        screenSwipe6.addAction(Actions.moveBy(1000, 0, transitionTime));
        screenSwipe7.addAction(Actions.moveBy(-1000, 0, transitionTime));
        screenSwipe8.addAction(Actions.moveBy(1000, 0, transitionTime));
        screenSwipe9.addAction(Actions.moveBy(-1000, 0, transitionTime));
        screenSwipe10.addAction(Actions.moveBy(1000, 0, transitionTime));
    }

    @Override
    public void onNotify(Entity entity, BattleEvent event) {
        switch (event) {
            /*
          case OPPONENT_HIT_DAMAGE:
                notify(AudioObserver.AudioCommand.SOUND_PLAY_ONCE, AudioObserver.AudioTypeEvent.SOUND_PLAYER_ATTACK);
                notify(AudioObserver.AudioCommand.SOUND_PLAY_ONCE, AudioObserver.AudioTypeEvent.SOUND_CREATURE_PAIN);
                break;
            case OPPONENT_DEFEATED:
                MainGameScreen.setGameState(MainGameScreen.GameState.RUNNING);
                int goldReward = Integer.parseInt(entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.DIBS_REWARD.toString()));
                _statusUI.addGoldValue(goldReward);
                int xpReward = Integer.parseInt(entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.XP_REWARD.toString()));
                _statusUI.addXPValue(xpReward);
                notify(AudioObserver.AudioCommand.MUSIC_STOP, AudioObserver.AudioTypeEvent.MUSIC_BATTLE);
                _mapMgr.enableCurrentmapMusic();
                addTransitionToScreen();
                //todo
                //_battleUI.setVisible(false);
                break;
                */
            case ANNIMATION_COMPLETE:
                switch (BattleScreen.getAnimationState()) {
                    case BATTLE:
                        break;
                    case ESCAPED:
                        playerComingFromBattle = true;
                        MainGameScreen.setGameState(MainGameScreen.GameState.RUNNING);
                        notify(AudioObserver.AudioCommand.MUSIC_STOP, AudioObserver.AudioTypeEvent.MUSIC_BATTLE);

                        // transition animation - note: screen transition to main screen cannot occur in render() because render is called from main screen
                        // battle screen is faded out from battle HUD
                        swipeBackToMainGameScreen(1.0f);

                        game.setScreen(game.getScreenType(ElmourGame.ScreenType.MainGame));
                        break;
                    case FAILED_ESCAPE:
                        break;
                }
                break;
            case GAME_OVER:
                // hide screen swipes for when game play goes back to main screen
                screenSwipe1.setVisible(false);
                screenSwipe2.setVisible(false);
                screenSwipe3.setVisible(false);
                screenSwipe4.setVisible(false);
                screenSwipe5.setVisible(false);
                screenSwipe6.setVisible(false);
                screenSwipe7.setVisible(false);
                screenSwipe8.setVisible(false);
                screenSwipe9.setVisible(false);
                screenSwipe10.setVisible(false);

                resetPlayerComingFromBattle();

                ProfileManager.getInstance().setIsLoaded(false);

                if (!getSetGameOverScreenTimer().isScheduled()) {
                    // delay here should match BattleScreen fadeOut time
                    Timer.schedule(getSetGameOverScreenTimer(), 1.0f);
                }

                break;
            case BATTLE_OVER:
                playerComingFromBattle = true;
                swipeBackToMainGameScreen(1.0f);

                if (!getSetGameOverScreenTimer().isScheduled()) {
                    // delay here should match BattleScreen fadeOut time
                    Timer.schedule(getSetMainGameScreenTimer(), 1.0f);
                }

                break;
        }
    }

    @Override
    public void onNotify(Entity sourceEntity, Entity destinationEntity, BattleEventWithMessage event, String message) {

    }

    @Override
    public void onNotify(Entity entity, InventoryElement.Effect effect) {

    }

    @Override
    public void addObserver(AudioObserver audioObserver) {
        _observers.add(audioObserver);
    }

    @Override
    public void removeObserver(AudioObserver audioObserver) {
        _observers.removeValue(audioObserver, true);
    }

    @Override
    public void removeAllObservers() {
        _observers.removeAll(_observers, true);
    }

    @Override
    public void notify(AudioObserver.AudioCommand command, AudioObserver.AudioTypeEvent event) {
        for(AudioObserver observer: _observers){
            observer.onNotify(command, event);
        }
    }

    public boolean isCutScene() {
        return isCutScene;
    }

    public void setEnabled(boolean enable) { isEnabled = enable; }

    public void setCutScene(boolean cutScene) {
        isCutScene = cutScene;
        if (!isCutScene)
            menuButton.setVisible(true);
    }

    @Override
    public void onNotify(int value, PopUpEvent event) {
        switch(event) {
            case INTERACTION_THREAD_EXIT:
                // this is necessary to allow player to move again
                isCurrentConversationDone = true;
                break;
        }
    }
}
