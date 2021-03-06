package com.smoftware.elmour.UI.huds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
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
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.smoftware.elmour.UI.devtools.AdjustKeyItemsInputListener;
import com.smoftware.elmour.UI.devtools.GotoMapListener;
import com.smoftware.elmour.UI.devtools.SaveFileListener;
import com.smoftware.elmour.UI.graphics.AnimatedImage;
import com.smoftware.elmour.actions.MyActions;
import com.smoftware.elmour.components.Component;
import com.smoftware.elmour.components.ComponentObserver;
import com.smoftware.elmour.components.PlayerPhysicsComponent;
import com.smoftware.elmour.inventory.PartyKeys;
import com.smoftware.elmour.main.ElmourGame;
import com.smoftware.elmour.entities.Entity;
import com.smoftware.elmour.entities.EntityConfig;
import com.smoftware.elmour.entities.EntityConfig.ConversationConfig;
import com.smoftware.elmour.entities.EntityFactory;
import com.smoftware.elmour.inventory.InventoryElement;
import com.smoftware.elmour.inventory.KeyItem;
import com.smoftware.elmour.inventory.KeyItemFactory;
import com.smoftware.elmour.UI.components.MyTextField;
import com.smoftware.elmour.UI.devtools.AdjustInventoryInputListener;
import com.smoftware.elmour.UI.devtools.AdjustSpellsPowersInputListener;
import com.smoftware.elmour.UI.devtools.AdjustStatsUI;
import com.smoftware.elmour.main.Utility;
import com.smoftware.elmour.audio.AudioManager;
import com.smoftware.elmour.audio.AudioObserver;
import com.smoftware.elmour.audio.AudioSubject;
import com.smoftware.elmour.battle.BattleObserver;
import com.smoftware.elmour.UI.dialog.ChoicePopUp;
import com.smoftware.elmour.UI.dialog.Conversation;
import com.smoftware.elmour.UI.dialog.ConversationChoice;
import com.smoftware.elmour.UI.dialog.ConversationGraph;
import com.smoftware.elmour.UI.dialog.ConversationGraphObserver;
import com.smoftware.elmour.UI.dialog.ConversationNode;
import com.smoftware.elmour.UI.dialog.InputDialogObserver;
import com.smoftware.elmour.UI.dialog.InputDialogSubject;
import com.smoftware.elmour.UI.dialog.PopUpLabel;
import com.smoftware.elmour.UI.dialog.PopUp;
import com.smoftware.elmour.UI.dialog.PopUpObserver;
import com.smoftware.elmour.maps.Map;
import com.smoftware.elmour.maps.MapFactory;
import com.smoftware.elmour.maps.MapManager;
import com.smoftware.elmour.maps.MapObserver;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.profile.ProfileObserver;
import com.smoftware.elmour.quest.QuestGraph;
import com.smoftware.elmour.quest.QuestList;
import com.smoftware.elmour.quest.QuestTask;
import com.smoftware.elmour.screens.BattleScreen;
import com.smoftware.elmour.screens.MainGameScreen;
import com.smoftware.elmour.sfx.ClockActor;
import com.smoftware.elmour.sfx.ScreenTransitionAction;
import com.smoftware.elmour.sfx.ScreenTransitionActor;
import com.smoftware.elmour.sfx.ShakeCamera;
import com.smoftware.elmour.tests.MyTextAreaTest;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Set;

public class PlayerHUD implements Screen, AudioSubject,
                                ProfileObserver,
                                ComponentObserver,
                                ConversationGraphObserver,
                                BattleObserver,
                                StatusObserver,
                                PopUpObserver,
                                InputDialogSubject,
                                PlayerHudSubject,
                                InventoryHudObserver,
                                QuestHudObserver,
                                MapObserver {
    private static final String TAG = PlayerHUD.class.getSimpleName();

    ElmourGame game;
    private Stage _stage;
    private Viewport _viewport;
    private Camera _camera;
    private Entity _player;

    private PlayerHUD thisPlayerHUD;

    private QuestUI _questUI;
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
    private boolean hiddenItemAlreadyShown;
    private boolean hiddenItemAlreadyHidden;

    private Image menuButton;
    private Image menuButtonDown;
    private boolean menuIsVisible;
    private boolean debugMenuIsVisible;
    private int numberOfMenuItems;
    private TextButton partyButton;
    private TextButton inventoryButton;
    private TextButton questsButton;
    private TextButton optionsButton;
    private TextButton saveButton;
    private TextButton debugButton;

    ////// for debugging
    private TextButton utilityButton;
    private TextButton noClipModeButton;
    private TextButton gotoMapButton;
    private TextButton saveAsButton;

    private TextButton adjustInventoryButton;
    private TextButton adjustSpellsPowersButton;
    private TextButton adjustKeyItemsButton;
    private TextButton adjustStatsButton;
    private TextButton adjustQuestsButton;

    private AdjustStatsUI adjustStatsUI;

    private Image testImage;
    /////////////////////

    private Dialog _messageBoxUI;
    private Label _label;
    private Json _json;
    private MapManager _mapMgr;

    private Array<AudioObserver> audioObservers;
    private Array<InputDialogObserver> inputDialogObservers;
    private Array<PlayerHudObserver> playerHudObservers;
    private ScreenTransitionActor _transitionActor;

    private ShakeCamera _shakeCam;
    private ClockActor _clock;

    private InventoryHUD inventoryHUD;
    private QuestHUD questHUD;

    private MyActions myActions;

    private WidgetGroup savingGroup;
    private AnimatedImage savingAnimation;
    private Label savingLabel;
    private MyTextField savingTextArea;
    private float savingGroupHeight;
    private float savingDropTime;

    private WidgetGroup hiddenItemGroup;
    private Image hiddenItemImage;
    private MyTextField hiddenItemTextArea;
    private float hiddenItemGroupHeight;
    private float hiddenItemGroupDropTime;

    private static final String INVENTORY_FULL = "Your inventory is full!";

    class ConversationConfigOverride {
        EntityConfig.ConversationType conversationType;
        String questID;

        ConversationConfigOverride(EntityConfig.ConversationType conversationType) {
            this.conversationType = conversationType;
            this.questID = null;
        }

        ConversationConfigOverride(EntityConfig.ConversationType conversationType, String questID) {
            this.conversationType = conversationType;
            this.questID = questID;
        }
    }

    private Hashtable<EntityFactory.EntityName, ConversationConfigOverride> entityConversationConfigOverrideMap;

    public PlayerHUD(final ElmourGame game, Camera camera, final Entity player, final MapManager mapMgr) {
        this.game = game;
        _camera = camera;
        _player = player;
        _mapMgr = mapMgr;
        _viewport = new FitViewport(ElmourGame.V_WIDTH, ElmourGame.V_HEIGHT, camera);
        _stage = new Stage(_viewport);
        //_stage.setDebugAll(true);

        thisPlayerHUD = this;

        entityConversationConfigOverrideMap = new Hashtable<>();

        adjustStatsUI = new AdjustStatsUI(this.game, this, _stage);

        testImage = new Image(new Texture("graphics/Untitled.png"));
        testImage.setWidth(Gdx.graphics.getWidth());
        testImage.setHeight(Gdx.graphics.getHeight());
        testImage.setPosition(0, 0);

        isCutScene = false;
        isEnabled = true;

        audioObservers = new Array<>();
        inputDialogObservers = new Array<>();
        playerHudObservers = new Array<>();
        _transitionActor = new ScreenTransitionActor();
        myActions = new MyActions();

        _shakeCam = new ShakeCamera(0, 0, 30.0f);

        inventoryHUD = new InventoryHUD(this.game, _stage);
        questHUD = new QuestHUD(this, _stage);

        game.battleState.addObserver(this);
        inventoryHUD.addObserver(this);
        questHUD.addObserver(this);
        ProfileManager.getInstance().addObserver(this);

        _json = new Json();

        _label = new Label("Test", Utility.STATUSUI_SKIN);
        _label.setWrap(true);
        _messageBoxUI = new Dialog("", Utility.STATUSUI_SKIN, "solidbackground");
        _messageBoxUI.setVisible(false);
        _messageBoxUI.getContentTable().add(_label).width(_stage.getWidth() / 2).pad(10, 10, 10, 0);
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
        _clock.setPosition(_stage.getWidth() - _clock.getWidth(), 0);
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

        signPopUp = new PopUp(PopUp.PopUpType.SIGN);
        if (ElmourGame.isAndroid()) {
            signPopUp.setWidth(_stage.getWidth() / 1.1f);
            signPopUp.setHeight(84);
        } else {
            signPopUp.setWidth(_stage.getWidth() / 1.1f);
            signPopUp.setHeight(84);
        }
        signPopUp.setPosition(_stage.getWidth() / 2 - signPopUp.getWidth() / 2, 25);

        signPopUp.setVisible(false);

        conversationPopUp = new PopUp(PopUp.PopUpType.CONVERSATION);
        if (ElmourGame.isAndroid()) {
            conversationPopUp.setWidth(_stage.getWidth() / 1.04f);
            conversationPopUp.setHeight(84);
        } else {
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
        } else {
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

        hiddenItemAlreadyShown = false;
        hiddenItemAlreadyHidden = false;

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
        questsButton = new TextButton("Quests", Utility.ELMOUR_UI_SKIN);
        optionsButton = new TextButton("Options", Utility.ELMOUR_UI_SKIN);
        saveButton = new TextButton("Save", Utility.ELMOUR_UI_SKIN);
        debugButton = new TextButton("Debug", Utility.ELMOUR_UI_SKIN);

        utilityButton = new TextButton("Utility", Utility.ELMOUR_UI_SKIN);
        noClipModeButton = new TextButton("No clip for you", Utility.ELMOUR_UI_SKIN);
        gotoMapButton = new TextButton("Go to map", Utility.ELMOUR_UI_SKIN);
        saveAsButton = new TextButton("Save as...", Utility.ELMOUR_UI_SKIN);

        adjustInventoryButton = new TextButton("Adjust Inventory", Utility.ELMOUR_UI_SKIN);
        adjustSpellsPowersButton = new TextButton("Adjust Spells", Utility.ELMOUR_UI_SKIN);
        adjustKeyItemsButton = new TextButton("Adjust Key Items", Utility.ELMOUR_UI_SKIN);
        adjustQuestsButton = new TextButton("Adjust Quests", Utility.ELMOUR_UI_SKIN);
        adjustStatsButton = new TextButton("Adjust Stats", Utility.ELMOUR_UI_SKIN);

        float menuPadding = 12;
        float menuItemWidth = _stage.getWidth() / 3f;
        float menuItemHeight = MathUtils.clamp(_stage.getHeight() / 7.5f, 0, 45);
        float menuItemX = _stage.getWidth() - menuItemWidth - menuPadding;
        float menuItemY = _stage.getHeight() - menuItemHeight - menuPadding;

        //////// Main menu
        partyButton.setWidth(menuItemWidth);
        partyButton.setHeight(menuItemHeight);
        partyButton.setPosition(menuItemX, menuItemY);
        partyButton.setVisible(false);

        menuItemY -= menuItemHeight - 2;
        inventoryButton.setWidth(menuItemWidth);
        inventoryButton.setHeight(menuItemHeight);
        inventoryButton.setPosition(menuItemX, menuItemY);
        inventoryButton.setVisible(false);

        menuItemY -= menuItemHeight - 2;
        questsButton.setWidth(menuItemWidth);
        questsButton.setHeight(menuItemHeight);
        questsButton.setPosition(menuItemX, menuItemY);
        questsButton.setVisible(false);

        menuItemY -= menuItemHeight - 2;
        optionsButton.setWidth(menuItemWidth);
        optionsButton.setHeight(menuItemHeight);
        optionsButton.setPosition(menuItemX, menuItemY);
        optionsButton.setVisible(false);

        menuItemY -= menuItemHeight - 2;
        saveButton.setWidth(menuItemWidth);
        saveButton.setHeight(menuItemHeight);
        saveButton.setPosition(menuItemX, menuItemY);
        saveButton.setVisible(false);

        menuItemY -= menuItemHeight - 2;
        debugButton.setWidth(menuItemWidth);
        debugButton.setHeight(menuItemHeight);
        debugButton.setPosition(menuItemX, menuItemY);
        debugButton.setVisible(false);

        //////// Right debug menu
        menuItemX = _stage.getWidth() - menuItemWidth - menuPadding;
        menuItemY = _stage.getHeight() - menuItemHeight - menuPadding;

        utilityButton.setWidth(menuItemWidth);
        utilityButton.setHeight(menuItemHeight);
        utilityButton.setPosition(menuItemX, menuItemY);
        utilityButton.setVisible(false);

        menuItemY -= menuItemHeight - 2;
        noClipModeButton.setWidth(menuItemWidth);
        noClipModeButton.setHeight(menuItemHeight);
        noClipModeButton.setPosition(menuItemX, menuItemY);
        noClipModeButton.setVisible(false);

        menuItemY -= menuItemHeight - 2;
        gotoMapButton.setWidth(menuItemWidth);
        gotoMapButton.setHeight(menuItemHeight);
        gotoMapButton.setPosition(menuItemX, menuItemY);
        gotoMapButton.setVisible(false);

        menuItemY -= menuItemHeight - 2;
        saveAsButton.setWidth(menuItemWidth);
        saveAsButton.setHeight(menuItemHeight);
        saveAsButton.setPosition(menuItemX, menuItemY);
        saveAsButton.setVisible(false);

        //////// Left debug menu
        menuItemX = menuPadding;
        menuItemY = _stage.getHeight() - menuItemHeight - menuPadding;

        adjustInventoryButton.setWidth(menuItemWidth);
        adjustInventoryButton.setHeight(menuItemHeight);
        adjustInventoryButton.setPosition(menuItemX, menuItemY);
        adjustInventoryButton.setVisible(false);

        menuItemY -= menuItemHeight - 2;
        adjustSpellsPowersButton.setWidth(menuItemWidth);
        adjustSpellsPowersButton.setHeight(menuItemHeight);
        adjustSpellsPowersButton.setPosition(menuItemX, menuItemY);
        adjustSpellsPowersButton.setVisible(false);

        menuItemY -= menuItemHeight - 2;
        adjustKeyItemsButton.setWidth(menuItemWidth);
        adjustKeyItemsButton.setHeight(menuItemHeight);
        adjustKeyItemsButton.setPosition(menuItemX, menuItemY);
        adjustKeyItemsButton.setVisible(false);

        menuItemY -= menuItemHeight - 2;
        adjustQuestsButton.setWidth(menuItemWidth);
        adjustQuestsButton.setHeight(menuItemHeight);
        adjustQuestsButton.setPosition(menuItemX, menuItemY);
        adjustQuestsButton.setVisible(false);

        menuItemY -= menuItemHeight - 2;
        adjustStatsButton.setWidth(menuItemWidth);
        adjustStatsButton.setHeight(menuItemHeight);
        adjustStatsButton.setPosition(menuItemX, menuItemY);
        adjustStatsButton.setVisible(false);

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

        savingGroupHeight = 100;
        savingDropTime = 0.5f;
        savingTextArea = new MyTextField("", Utility.ELMOUR_UI_SKIN, "battle");
        savingTextArea.disabled = true;
        savingTextArea.setWidth(100);
        savingTextArea.setHeight(savingGroupHeight);
        savingTextArea.setPosition((_stage.getWidth() - savingTextArea.getWidth()) / 2 , _stage.getHeight() + 12);
        savingTextArea.setVisible(true);

        savingLabel = new Label("Saving...", Utility.ELMOUR_UI_SKIN, "battle");
        savingLabel.setWidth(100);
        savingLabel.setHeight(40);
        savingLabel.setAlignment(Align.center);
        savingLabel.setPosition((_stage.getWidth() - savingTextArea.getWidth()) / 2 , _stage.getHeight() + savingTextArea.getHeight() - savingLabel.getHeight());
        savingLabel.setVisible(true);

        savingAnimation = getAnimatedImage(EntityFactory.EntityName.MISC_ANIMATIONS);
        savingAnimation.setCurrentAnimationType(Entity.AnimationType.SAVING_LOOP);
        savingAnimation.setWidth(64);
        savingAnimation.setHeight(64);
        savingAnimation.setPosition((_stage.getWidth() - savingAnimation.getWidth()) / 2, _stage.getHeight() + 12);
        savingAnimation.setVisible(true);

        savingGroup = new WidgetGroup();
        savingGroup.addActor(savingTextArea);
        savingGroup.addActor(savingLabel);
        savingGroup.addActor(savingAnimation);

        hiddenItemGroupHeight = 100;
        hiddenItemGroupDropTime = 0.5f;
        hiddenItemTextArea = new MyTextField("", Utility.ELMOUR_UI_SKIN, "battle");
        hiddenItemTextArea.disabled = true;
        hiddenItemTextArea.setWidth(100);
        hiddenItemTextArea.setHeight(hiddenItemGroupHeight);
        hiddenItemTextArea.setPosition((_stage.getWidth() - hiddenItemTextArea.getWidth()) / 2 , _stage.getHeight());
        hiddenItemTextArea.setVisible(true);

        // size and position of hiddenItemImage are set in setHiddenItemImage
        hiddenItemImage = new Image();

        hiddenItemGroup = new WidgetGroup();
        hiddenItemGroup.addActor(hiddenItemTextArea);
        hiddenItemGroup.addActor(hiddenItemImage);

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
        _stage.addActor(menuButtonDown);
        _stage.addActor(menuButton);
        _stage.addActor(signPopUp);
        _stage.addActor(conversationPopUp);
        _stage.addActor(popUpLabel);
        _stage.addActor(choicePopUp1);
        _stage.addActor(choicePopUp2);
        _stage.addActor(choicePopUp3);
        _stage.addActor(choicePopUp4);
        _stage.addActor(partyButton);
        _stage.addActor(inventoryButton);
        _stage.addActor(questsButton);
        _stage.addActor(optionsButton);
        _stage.addActor(saveButton);
        _stage.addActor(savingGroup);
        _stage.addActor(hiddenItemGroup);
        //_stage.addActor(testImage);

        if (ElmourGame.DEV_MODE) {
            _stage.addActor(debugButton);
            _stage.addActor(utilityButton);
            _stage.addActor(noClipModeButton);
            _stage.addActor(adjustInventoryButton);
            _stage.addActor(adjustSpellsPowersButton);
            _stage.addActor(adjustKeyItemsButton);
            _stage.addActor(adjustQuestsButton);
            _stage.addActor(adjustStatsButton);
            _stage.addActor(gotoMapButton);
            _stage.addActor(saveAsButton);
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
        conversationPopUp.addObserver(this);
        this.addObserver(AudioManager.getInstance());

        //Listeners
        conversationPopUp.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                doConversation();
            }
        });

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
                                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
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
                                        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                            Gdx.app.log(TAG, "inventory button up");
                                            if (touchPointIsInButton(inventoryButton)) {
                                                hideMenu(true);
                                                showInventoryHUD();
                                            }
                                        }
                                    }
        );

        questsButton.addListener(new ClickListener() {
                                    @Override
                                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                        return true;
                                    }

                                    @Override
                                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                        Gdx.app.log(TAG, "quests button up");
                                        if (touchPointIsInButton(questsButton)) {
                                            hideMenu(true);
                                            showQuestHUD(false);
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
                                      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
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
                                   public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                       // make sure touch point is still on this button
                                       if (touchPointIsInButton(saveButton)) {
                                           hideMenu(true);
                                           if (ProfileManager.getInstance().doesProfileExist(ProfileManager.SAVED_GAME_PROFILE)) {
                                               confirmOverwrite();
                                           } else {
                                               saveGame();
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
                                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                        hideMenu(true);
                                        showDebugMenu();
                                    }
                                }
        );

        utilityButton.addListener(new ClickListener() {
                                      @Override
                                      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                          return true;
                                      }

                                      @Override
                                      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                          // make sure touch point is still on this button
                                          if (touchPointIsInButton(utilityButton)) {
                                              hideDebugMenu();

                                              //MyTextAreaTest test = new MyTextAreaTest(thisPlayerHUD);
                                              //test.run();

                                              //notifySendShockwave(new Vector2(ElmourGame.V_WIDTH/2, ElmourGame.V_HEIGHT/2));

                                              notifySendShockwave(new Vector2(ElmourGame.V_WIDTH/2 + 16, ElmourGame.V_HEIGHT/2 + 16));


                                            /*
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
                                              */
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
                                         public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
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
                                              public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
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
                                                 public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
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

        adjustKeyItemsButton.addListener(new ClickListener() {
                                             @Override
                                             public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                                 return true;
                                             }

                                             @Override
                                             public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                                 // make sure touch point is still on this button
                                                 if (touchPointIsInButton(adjustKeyItemsButton)) {
                                                     hideDebugMenu();
                                                     Gdx.app.log(TAG, "adjustKeyItemsButton clicked");

                                                     AdjustKeyItemsInputListener listener = new AdjustKeyItemsInputListener(_stage);
                                                     Gdx.input.getTextInput(listener, "Enter Quantity", "", "");
                                                 }
                                             }
                                         }
        );

        adjustQuestsButton.addListener(new ClickListener() {

                                           @Override
                                           public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                               return true;
                                           }

                                           @Override
                                           public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                               if (touchPointIsInButton(adjustQuestsButton)) {
                                                   hideDebugMenu();
                                                   showQuestHUD(true);
                                               }
                                           }
                                       }
        );

        adjustStatsButton.addListener(new ClickListener() {

                                       @Override
                                       public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                           return true;
                                       }

                                       @Override
                                       public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                           if (touchPointIsInButton(adjustStatsButton)) {
                                               hideDebugMenu();
                                               notifyShowingStatsUI();
                                               adjustStatsUI.requestInput();
                                           }
                                       }
                                   }
        );

        gotoMapButton.addListener(new ClickListener() {

                                      @Override
                                      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                          return true;
                                      }

                                      @Override
                                      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                          if (touchPointIsInButton(gotoMapButton)) {
                                              hideDebugMenu();

                                              GotoMapListener listener = new GotoMapListener(_stage, mapMgr);
                                              Gdx.input.getTextInput(listener, "Enter Map Name", "", "");
                                          }
                                      }
                                  }
        );

        saveAsButton.addListener(new ClickListener() {

                                      @Override
                                      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                          return true;
                                      }

                                      @Override
                                      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                          if (touchPointIsInButton(saveAsButton)) {
                                              hideDebugMenu();

                                              SaveFileListener listener = new SaveFileListener(game, _stage);
                                              Gdx.input.getTextInput(listener, "Enter File Name", "", "");
                                          }
                                      }
                                  }
        );

        testImage.addListener(new ClickListener() {

                                     @Override
                                     public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                         return true;
                                     }

                                     @Override
                                     public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                         Gdx.app.log(TAG, "testImage clicked");
                                         notifySendShockwave(new Vector2(x, y));
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

    private AnimatedImage getAnimatedImage(EntityFactory.EntityName entityName){
        Entity entity = EntityFactory.getInstance().getEntityByName(entityName);
        return setEntityAnimation(entity);
    }

    private AnimatedImage setEntityAnimation(Entity entity){
        final AnimatedImage animEntity = new AnimatedImage();
        animEntity.setEntity(entity);
        animEntity.setSize(animEntity.getWidth() * Map.UNIT_SCALE, animEntity.getHeight() * Map.UNIT_SCALE);
        return animEntity;
    }

    public boolean isPlayerIsInBattle() {
        return playerIsInBattle;
    }

    public void showMessage(String message) {
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
        questsButton.setVisible(false);
        optionsButton.setVisible(false);
        saveButton.setVisible(false);
        debugButton.setVisible(false);
        notify(PlayerHudObserver.PlayerHudEvent.HIDING_MENU, "");

        if (setVisibleFlag)
            menuIsVisible = false;
    }

    private void hideDebugMenu() {
        utilityButton.setVisible(false);
        noClipModeButton.setVisible(false);
        adjustInventoryButton.setVisible(false);
        adjustSpellsPowersButton.setVisible(false);
        adjustKeyItemsButton.setVisible(false);
        adjustQuestsButton.setVisible(false);
        adjustStatsButton.setVisible(false);
        gotoMapButton.setVisible(false);
        saveAsButton.setVisible(false);
        debugMenuIsVisible = false;
        notify(PlayerHudObserver.PlayerHudEvent.HIDING_MENU, "");
    }

    private void showMenu() {
        partyButton.setVisible(true);
        inventoryButton.setVisible(true);
        questsButton.setVisible(true);
        optionsButton.setVisible(true);
        saveButton.setVisible(true);
        if (ElmourGame.DEV_MODE) {
            debugButton.setVisible(true);
        }
        notify(PlayerHudObserver.PlayerHudEvent.SHOWING_MENU, "");

        // don't set visible flag to true here, it's done in the touchUp handler of the menu button
    }

    private void showDebugMenu() {
        utilityButton.setVisible(true);
        noClipModeButton.setVisible(true);
        adjustInventoryButton.setVisible(true);
        adjustSpellsPowersButton.setVisible(true);
        adjustKeyItemsButton.setVisible(true);
        adjustQuestsButton.setVisible(true);
        adjustStatsButton.setVisible(true);
        gotoMapButton.setVisible(true);
        saveAsButton.setVisible(true);
        debugMenuIsVisible = true;
        notify(PlayerHudObserver.PlayerHudEvent.SHOWING_MENU, "");
    }

    public void notifyShowingStatsUI() {
        notify(PlayerHudObserver.PlayerHudEvent.SHOWING_STATS_UI, "");
    }

    public void notifyHidingStatsUI() {
        notify(PlayerHudObserver.PlayerHudEvent.HIDING_STATS_UI, "");
    }

    public void notifySendShockwave(Vector2 position) {
        notify(PlayerHudObserver.PlayerHudEvent.SEND_SHOCKWAVE, _json.toJson(position, Vector2.class));
    }

    private void showInventoryHUD() {
        inventoryHUD.show();
    }

    private void showQuestHUD(boolean debug) {
        if (debug) {
            questHUD.showDebug();
        }
        else {
            questHUD.show();
        }
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

    public InputDialogObserver.InputDialogEvent inputDialogEvent;

    public void requestInput(final String labelText, InputDialogObserver.InputDialogEvent event) {
        inputDialogEvent = event;
        TextButton btnOK = new TextButton("OK", Utility.ELMOUR_UI_SKIN, "message_box");

        // All of this is needed in order to get a blinking cursor
        MyTextField.TextFieldStyle tStyle = new MyTextField.TextFieldStyle();
        tStyle.font = Utility.ELMOUR_UI_SKIN.getFont("myFont");
        tStyle.fontColor = Color.BLACK;
        tStyle.background = Utility.ELMOUR_UI_SKIN.getDrawable("textbutton");
        tStyle.cursor = Utility.ELMOUR_UI_SKIN.newDrawable("cursor", Color.BLACK);
        tStyle.cursor.setMinWidth(3.5f);
        tStyle.selection = Utility.ELMOUR_UI_SKIN.newDrawable("textbutton", 0.5f, 0.5f, 0.5f, 0.5f);

        final MyTextField inputField = new MyTextField("", tStyle);

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
                String input = inputField.getText();
                Gdx.app.log(TAG, "Input: " + input);

                input = input.trim();

                if (input.isEmpty()) {
                    dialog.cancel();
                    dialog.hide();
                    String message = "Input cannot be empty";
                    invalidInputMessage(labelText, message);
                }
                else if (input.length() > 15) {
                    dialog.cancel();
                    dialog.hide();
                    String message = "A maximum of 15 characters is allowed";
                    invalidInputMessage(labelText, message);
                }
                else {
                    String invalidChars = validateInput(input);
                    if (invalidChars.isEmpty()) {
                        dialog.cancel();
                        dialog.hide();
                        Gdx.input.setOnscreenKeyboardVisible(false);
                        PlayerHUD.this.notify(input, inputDialogEvent);
                    }
                    else {
                        dialog.cancel();
                        dialog.hide();
                        String charsNotAllowed = removeDuplicateChars(invalidChars);
                        String message = "These character(s) are not allowed: \"" + charsNotAllowed + "\"";
                        invalidInputMessage(labelText, message);
                    }
                }
                return true;
            }
        });

        float btnHeight = 30f;
        float btnWidth = 100f;
        Table t = new Table();
        t.row().pad(5, 5, 0, 5);
        // t.debug();

        Label label1 = new Label(labelText, Utility.ELMOUR_UI_SKIN, "message_box");
        dialog.getContentTable().add(label1).padTop(5f);
        dialog.getContentTable().add(inputField).width(150).padTop(5f);

        t.add(btnOK).width(btnWidth).height(btnHeight);

        dialog.getButtonTable().add(t).center().padBottom(10f);
        dialog.show(_stage).setPosition(_stage.getWidth() / 2 - dialog.getWidth() / 2, _stage.getHeight() - dialog.getHeight() - 7);

        dialog.setName("inputDialog");
        _stage.addActor(dialog);
        _stage.setKeyboardFocus(inputField);

        if (Gdx.input.getInputProcessor() == null) {
            Gdx.app.log(TAG, "Setting input processor to PlayerHUD stage in requestInput()");
            Gdx.input.setInputProcessor(_stage);
        }
    }

    private void invalidInputMessage(final String labelText, String message) {
        TextButton btnOK = new TextButton("OK", Utility.ELMOUR_UI_SKIN, "message_box");

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
                dialog.cancel();
                dialog.hide();
                requestInput(labelText, inputDialogEvent);
                return true;
            }
        });

        float btnHeight = 30f;
        float btnWidth = 100f;
        Table t = new Table();
        t.row().pad(5, 5, 0, 5);
        // t.debug();

        Label label1 = new Label(message, Utility.ELMOUR_UI_SKIN, "message_box");
        dialog.getContentTable().add(label1).padTop(5f);

        t.add(btnOK).width(btnWidth).height(btnHeight);

        dialog.getButtonTable().add(t).center().padBottom(10f);
        dialog.show(_stage).setPosition(_stage.getWidth() / 2 - dialog.getWidth() / 2, _stage.getHeight() - dialog.getHeight() - 7);

        dialog.setName("invalidCharsDialog");
        _stage.addActor(dialog);
    }

    private String validateInput(String input) {
        String validCharacters = "\\sa-zA-Z0-9ÀÁÂÃÄÅÆÈÉÊËÌÍÎÏĐÑÒÓÔÕÖØÙÚÛÜÝÿþÞßŸàáâãäåæèéêëìíîïñòóôõöøùúûüý.,?!_*#$%+-/:;<=>\\^|£¥¿¡×÷€";
        String regEx = "^[" + validCharacters + "]*$";
        String invalidChars = "";

        if (input.matches(regEx)) {
            Gdx.app.log(TAG, "Input is valid");
            return invalidChars;
        } else {
            invalidChars = input.replaceAll("([" + validCharacters + "])", "");
            Gdx.app.log(TAG, "Input is not valid: " + invalidChars);
            return invalidChars;
        }
    }

    private String removeDuplicateChars(String string) {
        char[] chars = string.toCharArray();
        Set<Character> charSet = new LinkedHashSet<Character>();
        for (char c : chars) {
            charSet.add(c);
        }

        StringBuilder sb = new StringBuilder();
        for (Character character : charSet) {
            sb.append(character);
        }

        return sb.toString();
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
                dialog.remove();
                saveGame();
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

    private void saveGame() {
        //savingAnimation.setVisible(true);

        _stage.addAction(Actions.sequence(

                Actions.parallel(
                            myActions.new setWalkDirection(savingAnimation, Entity.AnimationType.SAVING),
                            Actions.run(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            savingAnimation.setWidth(64);
                                            savingAnimation.setHeight(64);
                                        }
                                    }
                            )
                        ),
                myActions.new setCharacterVisible(savingAnimation, true),

                Actions.delay(0.357f),
                Actions.parallel(
                            myActions.new setWalkDirection(savingAnimation, Entity.AnimationType.SAVING_LOOP),
                            Actions.run(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            savingAnimation.setWidth(64);
                                            savingAnimation.setHeight(64);
                                        }
                                    }
                            )
                        )

                )
        );

        savingGroup.addAction(Actions.sizeBy(0, savingGroupHeight, savingDropTime));
        savingGroup.addAction(Actions.moveBy(0, -savingGroupHeight, savingDropTime));
        ProfileManager.getInstance().setCurrentProfile(ProfileManager.SAVED_GAME_PROFILE);
        ProfileManager.getInstance().saveProfile();
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

    public void updateMapHiddenItemStatus() {
        _mapMgr.refreshMapHiddenItemEntities();
    }

    public void addTransitionToScreen(float duration){
        _transitionActor.setVisible(true);
        _stage.addAction(
                Actions.sequence(
                        Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, duration), _transitionActor)));
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

    public String getAvailableQuestTask(String npcName, String [] questAndTaskIDs) {
        for (String questInfo : questAndTaskIDs) {
            // questInfo is in the form <questID|<questTask1,questTask2...>
            String[] quest = questInfo.split(QuestList.QUEST_DELIMITER);
            String[] questTasks = quest[1].split(",");
            String questID = quest[0];
            QuestGraph questGraph = questHUD.getQuestByID(questID);

            // first deal with quests that are in progress
            if (questGraph.getQuestStatus() == QuestGraph.QuestStatus.IN_PROGRESS) {
                // this quest is in progress
                // see if there is an available task for this NPC (meaning no dependencies) and it is not complete
                for (String taskID : questTasks) {
                    if (questGraph.isQuestTaskAvailable(taskID) && !questGraph.isQuestTaskComplete(taskID)) {
                        return questID + QuestList.QUEST_DELIMITER + taskID;
                    }
                }

                if (npcName.equalsIgnoreCase(questGraph.getQuestGiver())) {
                    return questID + QuestList.QUEST_DELIMITER + QuestList.QUEST_GIVER;
                }

                return null;
            }
        }

        // if got here, then no quests in progress for this NPC
        // so see if this NPC is a quest giver
        for (String questInfo : questAndTaskIDs) {
            String[] quest = questInfo.split(QuestList.QUEST_DELIMITER);
            String questID = quest[0];
            QuestGraph questGraph = questHUD.getQuestByID(questID);

            Integer currentChapter = ProfileManager.getInstance().getProperty("currentChapter", Integer.class);
            if (currentChapter >= questGraph.getChapter()) {
                // this quest has not been started and is available
                if (npcName.equalsIgnoreCase(questGraph.getQuestGiver())) {
                    return questID + QuestList.QUEST_DELIMITER  + QuestList.QUEST_GIVER;
                }
            }
        }

        return null;
    }

    public ConversationConfig getConversationConfigForNPC(Entity npc, String value) {
        String [] questAndTaskIDs = value.split(QuestList.QUEST_TASK_DELIMITER);
        EntityConfig.ConversationConfig conversationConfig = null;
        EntityConfig config = npc.getEntityConfig();
        Array<ConversationConfig> npcConversationConfigs = config.getConversationConfigs();

        String questID = null;
        String taskID = null;
        String questIdTaskID = getAvailableQuestTask(config.getEntityID(), questAndTaskIDs);

        if (questIdTaskID != null) {
            String[] sa = questIdTaskID.split(QuestList.QUEST_DELIMITER);
            questID = sa[0];
            taskID = sa[1];
        }

        if (taskID == null) {
            // no available tasks
            // try to get latest configuration for this NPC from profile
            conversationConfig = getLatestEntityConversationConfig(npc);

            if (conversationConfig == null) {
                conversationConfig = getConversationConfig(npcConversationConfigs, questID, EntityConfig.ConversationType.NORMAL_DIALOG);
            }
        }
        else {
            QuestGraph questGraph = questHUD.getQuestByID(questID);
            QuestTask task = questGraph.getQuestTaskByID(taskID);

            if (questHUD.getQuestByID(questID).isQuestComplete()) {
                conversationConfig = getConversationConfig(npcConversationConfigs, questID, EntityConfig.ConversationType.POST_QUEST_DIALOG);
            }
            else if (questHUD.isQuestReadyForReturn(questID)) {
                // get return quest dialog or return quest cut scene
                conversationConfig = getConversationConfig(npcConversationConfigs, questID, task.getConversationType());
            }
            else if (taskID.equals(QuestList.QUEST_GIVER)) {
                if (questGraph.getQuestStatus() == QuestGraph.QuestStatus.IN_PROGRESS) {
                    // get active quest dialog or active quest cut scene
                    if (task != null) {
                        //todo: remove this if not needed
                        //conversationConfig = getConversationConfig(npcConversationConfigs, questID, task.getConversationType());
                        throw new RuntimeException("Unexpected situation!");
                    }

                    EntityFactory.EntityName npcName = EntityFactory.EntityName.valueOf(config.getEntityID().toUpperCase());
                    conversationConfig = getActiveConversationConfigForQuest(npcName, npcConversationConfigs, questID);
                }
                else {
                    // kick off pre-quest cut scene for this quest giver
                    conversationConfig = getConversationConfig(npcConversationConfigs, questID, EntityConfig.ConversationType.PRE_QUEST_CUTSCENE);
                }
            }
            else {
                // todo: handle post-task, think about renaming "ACTIVE_QUEST..." since if task is not started, but quest is active, incorrect case is entered below
                // handle available task
                if (task.getQuestTaskStatus() == QuestTask.QuestTaskStatus.NOT_STARTED) {
                    // get quest task dialog or quest task cut scene
                    conversationConfig = getConversationConfig(npcConversationConfigs, questID, EntityConfig.ConversationType.QUEST_TASK_DIALOG);
                    if (conversationConfig == null) {
                        conversationConfig = getConversationConfig(npcConversationConfigs, questID, EntityConfig.ConversationType.QUEST_TASK_CUTSCENE);
                    }
                    if (conversationConfig == null) {
                        conversationConfig = getConversationConfig(npcConversationConfigs, questID, task.getConversationType());
                    }
                } else {
                    // get active quest dialog or active quest cut scene
                    conversationConfig = getConversationConfig(npcConversationConfigs, questID, task.getConversationType());
                }
            }
        }

        return conversationConfig;
    }

    private ConversationConfig getConversationConfig(Array<ConversationConfig> npcConversationConfigs, String questID, EntityConfig.ConversationType type) {
        ConversationConfig conversationConfig = null;

        for (ConversationConfig npcQuestConfig : npcConversationConfigs) {
            if (type.toString().contains(EntityConfig.ConversationType.NORMAL_DIALOG.toString())) {
                if (ProfileManager.getInstance().currentChapterInRange(npcQuestConfig.chapters)) {
                    return npcQuestConfig;
                }
            }
            else if (questID == null && npcQuestConfig.type == type) {
                return npcQuestConfig;
            }
            else if (npcQuestConfig.questID != null && npcQuestConfig.questID.equals(questID) && npcQuestConfig.type == type) {
                return npcQuestConfig;
            }
        }

        return conversationConfig;
    }

    public static void saveLatestEntityConversationConfig(Entity entity, ConversationConfig conversationConfig) {
        ProfileManager.getInstance().setProperty(
                entity.getEntityConfig().getEntityID() + ConversationConfig.class.getSimpleName(), conversationConfig);
    }

    public static ConversationConfig getLatestEntityConversationConfig(Entity entity) {
        return ProfileManager.getInstance().getProperty(
                entity.getEntityConfig().getEntityID() + ConversationConfig.class.getSimpleName(), ConversationConfig.class);
    }

    private ConversationConfig getActiveConversationConfigForQuest(EntityFactory.EntityName npcName,
                                                                   Array<ConversationConfig> npcConversationConfigs,
                                                                   String questID) {
        ConversationConfig conversationConfig = null;

        ConversationConfigOverride override = entityConversationConfigOverrideMap.get(npcName);

        if (override == null) {
            throw new RuntimeException("Active quest config not set for NPC " + npcName.toString() + ", quest ID " + questID);
        }

        if (override.questID == null) {
            // use NORMAL_DIALOG
            if (!override.conversationType.toString().contains(EntityConfig.ConversationType.NORMAL_DIALOG.toString())) {
                throw new RuntimeException("Expected a NORMAL_DIALOG to be set for NPC " + npcName.toString() + ", quest ID " + questID);
            }

            for (ConversationConfig npcQuestConfig : npcConversationConfigs) {
                if (npcQuestConfig.type.toString().contains(EntityConfig.ConversationType.NORMAL_DIALOG.toString())) {
                    if (ProfileManager.getInstance().currentChapterInRange(npcQuestConfig.chapters)) {
                        conversationConfig = npcQuestConfig;
                    }
                }
            }
        }
        else {
            // use ACTIVE_DIALOG
            if (!override.questID.equals(questID) || !override.conversationType.toString().contains("ACTIVE_QUEST")) {
                throw new RuntimeException("Expected ACTIVE_QUEST to be set for NPC " + npcName.toString() + ", quest ID " + questID);
            }
        }

        //TODO

        for (ConversationConfig npcQuestConfig : npcConversationConfigs) {
            if (npcQuestConfig.type.toString().contains("ACTIVE_QUEST")) {
                if (npcQuestConfig.questID.equals(questID)) {
                    conversationConfig = npcQuestConfig;
                }
            }
        }

        return conversationConfig;
    }

    public void setActiveConversationConfigForQuest(EntityFactory.EntityName entityName, String questID, EntityConfig.ConversationType conversationType) {
        entityConversationConfigOverrideMap.put(entityName, new ConversationConfigOverride(conversationType, questID));
    }

    public void setNormalDialog(EntityFactory.EntityName entityName) {
        entityConversationConfigOverrideMap.put(entityName, new ConversationConfigOverride(EntityConfig.ConversationType.NORMAL_DIALOG));
    }

    public void setNormalDialog(EntityFactory.EntityName entityName, EntityConfig.ConversationType normalDialogType) {
        if (!normalDialogType.toString().contains(EntityConfig.ConversationType.NORMAL_DIALOG.toString())) {
            throw new IllegalArgumentException("Calling setNormalDialog with invalid type");
        }

        entityConversationConfigOverrideMap.put(entityName, new ConversationConfigOverride(normalDialogType));
    }

    public void clearConversationConfig(EntityFactory.EntityName entityName) {
        entityConversationConfigOverrideMap.remove(entityName);
    }

    public void setQuestTaskStarted(String questID, String questTaskID) {
        questHUD.setQuestTaskStarted(questID, questTaskID);
        updateEntityObservers();
        updateMapHiddenItemStatus();
    }

    public void setQuestTaskComplete(String questID, String questTaskID) {
        QuestGraph questGraph = questHUD.getQuestByID(questID);
        questGraph.setQuestTaskComplete(questTaskID);

        questHUD.setQuestTaskComplete(questID, questTaskID);
        updateEntityObservers();
        updateMapHiddenItemStatus();
    }

    @Override
    public void onNotify(ProfileManager profileManager, ProfileEvent event) {
        //Gdx.app.log(TAG, "onNotify event = " + event.toString());
        switch(event){
            case PROFILE_LOADED:
                boolean firstTime = profileManager.getIsNewProfile();
/*
                if( firstTime ){
                    _questUI.setQuests(new Array<QuestGraph>());

                }else{
                    Array<QuestGraph> quests = profileManager.getProperty("playerQuests", Array.class);
                    _questUI.setQuests(quests);
                }
*/
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
                //profileManager.setProperty("playerQuests", new Array<QuestGraph>());
                profileManager.setProperty("currentPlayerGP", 0 );
                profileManager.setProperty("currentPlayerLevel",0 );
                profileManager.setProperty("currentPlayerXP", 0 );
                profileManager.setProperty("currentPlayerXPMax", 0 );
                profileManager.setProperty("currentPlayerHP", 0 );
                profileManager.setProperty("currentPlayerHPMax", 0 );
                profileManager.setProperty("currentPlayerMP", 0 );
                profileManager.setProperty("currentPlayerMPMax", 0 );
                profileManager.setProperty("currentTime", 0);
                profileManager.setProperty("currentChapter", 1);
                profileManager.setProperty("CHARACTER_1", "Apollo");
                profileManager.setProperty("CHARACTER_2", "Isabell");
                break;
            case SAVED_PROFILE:
                savingGroup.addAction(Actions.sizeBy(0, -savingGroupHeight, savingDropTime));
                savingGroup.addAction(Actions.moveBy(0, savingGroupHeight, savingDropTime));
                break;
            default:
                break;
        }
    }

    @Override
    public void onNotify(Entity entity, String value, ComponentEvent event) {

    }

    @Override
    public void onNotify(String value, ComponentEvent event) {
        //Gdx.app.log(TAG, "onNotify event = " + event.toString());
        switch(event) {
            case LOAD_CONVERSATION:
                // this is only done at the beginning of a conversation graph
                if (isCurrentConversationDone) {
                    //todo: why is isExitingConversation necessary? It is causing issues with main game play with dialog and cut scenes
                    if (isExitingConversation) {
                        Gdx.app.log(TAG, "Exiting conversation");
                        isExitingConversation = false;
                    }
                    else {
                        if (value != null) {
                            Gdx.app.log(TAG, "Loading conversation");
                            Entity npc = _mapMgr.getCurrentSelectedMapEntity();

                            // check to see what type of conversation should be kicked off
                            ConversationConfig conversationConfig = getConversationConfigForNPC(npc, value);

                            if (conversationConfig != null) {
                                switch (conversationConfig.type) {
                                    case NORMAL_DIALOG:
                                    case NORMAL_DIALOG1:
                                    case NORMAL_DIALOG2:
                                    case NORMAL_DIALOG3:
                                    case ACTIVE_QUEST_DIALOG1:
                                    case ACTIVE_QUEST_DIALOG2:
                                    case ACTIVE_QUEST_DIALOG3:
                                    case RETURN_QUEST_DIALOG:
                                    case POST_QUEST_DIALOG:
                                    case QUEST_TASK_DIALOG:
                                        // config contains conversation .json file path
                                        loadEntityConversationFromJson(npc, conversationConfig.config);
                                        saveLatestEntityConversationConfig(npc, conversationConfig);
                                        break;
                                    case PRE_QUEST_CUTSCENE:
                                    case ACTIVE_QUEST_CUTSCENE1:
                                    case ACTIVE_QUEST_CUTSCENE2:
                                    case ACTIVE_QUEST_CUTSCENE3:
                                    case QUEST_TASK_CUTSCENE:
                                        // config contains cut scene string. CutSceneManager will handle it.
                                        break;
                                }
                            }
                            else {
                                Gdx.app.error(TAG, "ConversationConfig is null for " + npc.getEntityConfig().getEntityID());
                            }
                        }
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
                        notify(PlayerHudObserver.PlayerHudEvent.SHOWING_POPUP, "");
                    }
                }
                break;
            case HIDE_CONVERSATION:
                EntityConfig configHide = _json.fromJson(EntityConfig.class, value);
                if( configHide.getEntityID().equalsIgnoreCase(conversationPopUp.getCurrentEntityID())) {
                    popUpLabel.setVisible(false);
                    conversationPopUp.hide();
                    conversationPopUp.getCurrentConversationGraph().removeObserver(this);
                    notify(PlayerHudObserver.PlayerHudEvent.HIDING_POPUP, "");
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
            case HIDDEN_ITEM_DISCOVERED:
                KeyItem keyItem = _json.fromJson(KeyItem.class, value);
                Gdx.app.log(TAG, "HIDDEN_ITEM_DISCOVERED: " + keyItem.name + " : " + keyItem.text);

                signPopUp.setTextForSignInteraction(keyItem.text);
                signPopUp.interact(false);

                if (hiddenItemAlreadyShown) {
                    if (signPopUp.isDoneWithCurrentNode()) {
                        // now hidden item can be removed from map
                        updateMapHiddenItemStatus();
                    }

                    if (!hiddenItemAlreadyHidden) {
                        hiddenItemGroup.addAction(Actions.sizeBy(0, -hiddenItemGroupHeight, hiddenItemGroupDropTime));
                        hiddenItemGroup.addAction(Actions.moveBy(0, hiddenItemGroupHeight, hiddenItemGroupDropTime));
                        hiddenItemAlreadyHidden = true;
                    } else {
                        // now hidden item can be removed from map
                        updateMapHiddenItemStatus();
                    }
                }
                else {
                    if (keyItem.category == KeyItem.Category.QUEST) {
                        String[] quest = keyItem.taskID.split(QuestList.QUEST_DELIMITER);
                        QuestGraph questGraph = questHUD.getQuestByID(quest[0]);
                        questGraph.setQuestTaskComplete(quest[1]);
                    }

                    if (!hiddenItemAlreadyShown) {
                        setHiddenItemImage(keyItem.imagePath);
                        hiddenItemGroup.addAction(Actions.sizeBy(0, hiddenItemGroupHeight, hiddenItemGroupDropTime));
                        hiddenItemGroup.addAction(Actions.moveBy(0, -hiddenItemGroupHeight, hiddenItemGroupDropTime));
                        hiddenItemAlreadyShown = true;
                        PartyKeys.getInstance().addItem(keyItem, 1,true);
                    }
                }

                break;
            case QUEST_LOCATION_DISCOVERED:
                String[] string = value.split(Component.MESSAGE_TOKEN);
                String questID = string[0];
                String questTaskID = string[1];

                if (questTaskID != null) {
                    setQuestTaskComplete(questID, questTaskID);
                }
                else if (string.length > 2) {
                    String cutScene = string[2];
                }

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

    private void setHiddenItemImage(String imagePath) {
        hiddenItemImage.remove();
        hiddenItemImage = new Image(new Texture(imagePath));
        hiddenItemImage.setWidth(64);
        hiddenItemImage.setHeight(64);
        hiddenItemImage.setPosition((_stage.getWidth() - hiddenItemImage.getWidth()) / 2,
                _stage.getHeight() + (((hiddenItemTextArea.getHeight() - hiddenItemImage.getHeight()))/ 2));
        hiddenItemGroup.addActor(hiddenItemImage);
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
            // check for special handling of ACTION type - this allows two or more ACTION nodes to be strung together
            Conversation conversation = conversationPopUp.getCurrentConversationGraph().getConversationByID(nextConversationId);
            if (conversation != null) {
                if (conversation.getType().equals(ConversationNode.NodeType.ACTION.toString())) {
                    conversationPopUp.showChoices();
                    isThereAnActiveHiddenChoice = true;
                }
            }

            if (conversationPopUp.populateConversationDialogById(nextConversationId) == true) {
                // todo: is this still necessary if not a cut scene
                if (!isCutScene) {
                    conversationPopUp.interact(false);
                }
            }
            else {
                conversationPopUp.hide();
                popUpLabel.setVisible(false);
                notify(PlayerHudObserver.PlayerHudEvent.HIDING_POPUP, "");
            }
        }

        // don't interact here if there are choices visible
        if (numVisibleChoices == 0) {
            Gdx.app.log(TAG, "numVisibleChoices == 0");
            conversationPopUp.interact(false);
        }

        // this sets the popup up for the next destination id if NO_CHOICE is active
        // in this case, choicePopUp1 always holds the next destination id
        if (isThereAnActiveHiddenChoice) {
            nextConversationId = choicePopUp1.getChoice().getDestinationId();
            Gdx.app.log(TAG, String.format("isThereAnActiveHiddenChoice == true ------- next conversation id = %s", nextConversationId));
            if (conversationPopUp.populateConversationDialogById(nextConversationId) == false) {
                // if this is a delayed popup, the nextConversationId was already set
                if (!isDelayedPopUp) {
                    // the current conversation Id is non-interactive, so hide popup and move on to next Id that is in choicePopup1
                    nextConversationId = conversationPopUp.getCurrentConversationGraph().getNextConversationIDFromChoice(nextConversationId, 0);
                    Gdx.app.log(TAG, String.format("isDelayedPopUp == false ------- next conversation id = %s", nextConversationId));
                }
                conversationPopUp.hide();
                notify(PlayerHudObserver.PlayerHudEvent.HIDING_POPUP, "");
                popUpLabel.setVisible(false);
                isThereAnActiveHiddenChoice = false;
            }
        }
        else if (choicePopUp1.getChoice() != null) {
            Gdx.app.log(TAG, "choicePopUp1.getChoice() != null");
            if (choicePopUp1.getChoice().getConversationCommandEvent() != null) {
                if (choicePopUp1.getChoice().getConversationCommandEvent().equals(ConversationCommandEvent.EXIT_CHAT)) {
                    conversationPopUp.hide();
                    popUpLabel.setVisible(false);
                    notify(PlayerHudObserver.PlayerHudEvent.HIDING_POPUP, "");
                }
            }
        }
    }

    private void loadEntityConversationFromJson(Entity entity, String jsonFilePath) {
        isCurrentConversationDone = false;
        conversationPopUp.loadEntityConversationFromJson(entity, jsonFilePath);
        conversationPopUp.getCurrentConversationGraph().addObserver(this);
    }

    public void loadConversationForCutScene(String jsonFilePath, ConversationGraphObserver graphObserver) {
        isCurrentConversationDone = false;
        conversationPopUp.loadConversationFromJson(jsonFilePath);
        conversationPopUp.getCurrentConversationGraph().addObserver(this);
        conversationPopUp.getCurrentConversationGraph().addObserver(graphObserver);
    }

    private void handleExitConversation(ConversationCommandEvent event) {
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
        _mapMgr.clearCurrentSelectedMapEntity();

        if (event == ConversationCommandEvent.EXIT_CHAT) {
            //isExitingConversation = true;
        }

        notify(PlayerHudObserver.PlayerHudEvent.HIDING_POPUP, "");
    }

    public void acceptQuest(String questID) {
        QuestGraph questGraph = questHUD.getQuestByID(questID);

        if (questGraph != null) {
            // save full quest graph for quests that are in progress
            questGraph.setQuestStatus(QuestGraph.QuestStatus.IN_PROGRESS);
            questGraph.setTimestamp(TimeUtils.millis());
            updateEntityObservers();
            updateMapHiddenItemStatus();
        }
    }

    public void unAcceptQuest(String questID) {
        QuestGraph questGraph = questHUD.getQuestByID(questID);

        if (questGraph != null) {
            questGraph.setQuestStatus(QuestGraph.QuestStatus.NOT_STARTED);
            updateEntityObservers();
            updateMapHiddenItemStatus();
        }
    }

    @Override
    public void onNotify(ConversationGraph graph, ConversationCommandEvent event) {
        //Gdx.app.log(TAG, "onNotify event = " + event.toString());
        switch(event) {
            case EXIT_CHAT:
                handleExitConversation(event);
                break;
            case ACCEPT_QUEST:
                //todo: this should never get called unless we need to support accepting quests in a normal conversation
                /*
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
                    updateMapHiddenItemStatus();
                }

                _mapMgr.clearCurrentSelectedMapEntity();
                */
                break;
            case RETURN_QUEST:
                //todo: this should never get called unless we need to support return quests in a normal conversation
                /*
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
                */
                break;
            case NONE:
                break;
            default:
                break;
        }
    }

    @Override
    public void onNotify(ConversationGraph graph, ConversationCommandEvent event, String data) {
        switch(event) {
            case EXIT_CUTSCENE:
                handleExitConversation(event);

                // Load map and position player at correct start position.
                // data contains <PLAYER_START_ + extension>;<Map name>[;<Direction>[;<Character name>]]
                // Default Direction to DOWN
                // Character name is optional
                String [] sa = data.split(";");
                _mapMgr.loadMap(MapFactory.MapType.valueOf(sa[1]), true);
                _mapMgr.setStartPositionFromPreviousMap(sa[0]);

                Entity.Direction direction = Entity.Direction.DOWN;
                if (sa.length > 2) {
                    direction = Entity.Direction.valueOf(sa[2]);
                }

                _player.sendMessage(Component.MESSAGE.CURRENT_STATE, _json.toJson(Entity.State.IDLE));
                _player.sendMessage(Component.MESSAGE.CURRENT_DIRECTION, _json.toJson(direction));

                //todo: handle changing character if sa[3] parameter exists
                
                break;
            case TASK_COMPLETE:
            case TASK_COMPLETE_CUTSCENE:
                String conversationID = data;
                Conversation conversation = graph.getConversationByID(conversationID);

                if (conversation != null ) {
                    String questTaskID = conversation.getData();
                    // need to strip off parameters that are delimited by semi-colons
                    String [] temp = questTaskID.split(";");
                    String [] quest = temp[0].split(QuestList.QUEST_DELIMITER);
                    setQuestTaskComplete(quest[0], quest[1]);

                    QuestGraph questGraph = questHUD.getQuestByID(quest[0]);
                    QuestTask questTask = questGraph.getQuestTaskByID(quest[1]);

                    switch (questTask.getQuestTaskType()) {
                        case FETCH:
                            //	For FETCH "Target Entity" is the KeyItem ID
                            KeyItem.ID itemID = KeyItem.ID.valueOf(questTask.getTargetEntity());
                            KeyItem keyItem = KeyItemFactory.getInstance().getKeyItem(itemID);
                            PartyKeys.getInstance().addItem(keyItem, 1, true);
                            break;
                    }

                    // update the associated NPC conversation config based on current status
                    try {
                        Entity entity = EntityFactory.getInstance().getEntityByName(questTask.getTargetEntity());
                        if (entity != null) {
                            EntityConfig.ConversationConfig conversationConfig = null;
                            EntityConfig config = entity.getEntityConfig();
                            Array<ConversationConfig> npcConversationConfigs = config.getConversationConfigs();

                            if (questGraph.isQuestComplete()) {
                                conversationConfig = getConversationConfig(npcConversationConfigs, questGraph.getQuestID(), EntityConfig.ConversationType.POST_QUEST_DIALOG);
                            }
                            else {
                                if (questTask.getPostTaskConversationType() != null) {
                                    conversationConfig = getConversationConfig(npcConversationConfigs, questGraph.getQuestID(), questTask.getPostTaskConversationType());
                                }
                                else {
                                    conversationConfig = getConversationConfig(npcConversationConfigs, questGraph.getQuestID(), questTask.getConversationType());
                                }
                            }

                            saveLatestEntityConversationConfig(entity, conversationConfig);
                        }
                    }
                    catch (NullPointerException e) {
                    }
                }

                handleExitConversation(event);
                break;
        }
    }

    @Override
    public void onNotify(ConversationGraph graph, ArrayList<ConversationChoice> choices) {
        int choiceNum = 0;
        float choicePopupHeight;
        isThereAnActiveHiddenChoice = false;

        Gdx.app.log(TAG, "Displaying " + choices.size() + " choices");

        if (ElmourGame.isAndroid()) {
            choicePopupHeight = 110;
        } else {
            choicePopupHeight = 110;
        }

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
                choicePopUp1.setHeight(choicePopupHeight);
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
                choicePopUp1.setWidth(_stage.getWidth() / 1.04f / 2f);
                choicePopUp1.setHeight(choicePopupHeight);
                choicePopUp2.setWidth(_stage.getWidth() / 1.04f / 2f);
                choicePopUp2.setHeight(choicePopupHeight);

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
                choicePopUp1.setWidth(_stage.getWidth() / 1.04f / 3f);
                choicePopUp1.setHeight(choicePopupHeight);
                choicePopUp2.setWidth(_stage.getWidth() / 1.04f / 3f);
                choicePopUp2.setHeight(choicePopupHeight);
                choicePopUp3.setWidth(_stage.getWidth() / 1.04f / 3f);
                choicePopUp3.setHeight(choicePopupHeight);

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

                conversationPopUp.populateConversationDialogById(nextConversationId);

                break;
            case PLAYER_RESPONSE:
                // interact first so previous popup is cleared
                conversationPopUp.interact(false);

                // need a slight delay here, otherwise new popup isn't populated
                try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }

                conversationPopUp.populateConversationDialogById(nextConversationId);

                choicePopUp1.hide();
                choicePopUp2.hide();
                choicePopUp3.hide();
                choicePopUp4.hide();
                numVisibleChoices = 0;
                isThereAnActiveHiddenChoice = false;

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

        if (inventoryHUD != null)
            inventoryHUD.render(delta);

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
        if (inventoryHUD != null)
            inventoryHUD.resize((int) MainGameScreen.VIEWPORT.physicalWidth, (int) MainGameScreen.VIEWPORT.physicalHeight);
    }

    @Override
    public void pause() {
        /*_battleUI.resetDefaults();*/
        if (inventoryHUD != null)
            inventoryHUD.pause();
    }

    @Override
    public void resume() {
        if (inventoryHUD != null)
            inventoryHUD.resume();
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
        audioObservers.add(audioObserver);
    }

    @Override
    public void removeObserver(AudioObserver audioObserver) {
        audioObservers.removeValue(audioObserver, true);
    }

    @Override
    public void addObserver(PlayerHudObserver observer) {
        playerHudObservers.add(observer);
    }

    @Override
    public void removeObserver(PlayerHudObserver observer) {
        playerHudObservers.removeValue(observer, true);
    }

    public void addInventoryObserver(InventoryHudObserver observer) {
        inventoryHUD.addObserver(observer);
    }

    public void removeInventoryObserver(InventoryHudObserver observer) {
        inventoryHUD.removeObserver(observer);
    }

    public void addQuestObserver(QuestHudObserver observer) {
        questHUD.addObserver(observer);
    }

    public void removeQuestObserver(QuestHudObserver observer) {
        questHUD.removeObserver(observer);
    }

    @Override
    public void notify(PlayerHudObserver.PlayerHudEvent event, String value) {
        for(PlayerHudObserver observer: playerHudObservers){
            observer.onNotify(event, value);
        }
    }

    @Override
    public void removeAllObservers() {
        audioObservers.removeAll(audioObservers, true);
    }

    @Override
    public void notify(AudioObserver.AudioCommand command, AudioObserver.AudioTypeEvent event) {
        for(AudioObserver observer: audioObservers){
            observer.onNotify(command, event);
        }
    }

    @Override
    public void addObserver(InputDialogObserver observer) {
        inputDialogObservers.add(observer);
    }

    @Override
    public void removeObserver(InputDialogObserver observer) {
        inputDialogObservers.removeValue(observer, true);
    }

    @Override
    public void removeAllInputDialogObservers() {
        for(InputDialogObserver observer: inputDialogObservers){
            inputDialogObservers.removeValue(observer, true);
        }
    }

    @Override
    public void notify(final String value, InputDialogObserver.InputDialogEvent event) {
        for(InputDialogObserver observer: inputDialogObservers){
            observer.onInputDialogNotify(value, event);
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

                // reset for hidden item
                hiddenItemAlreadyShown = false;
                hiddenItemAlreadyHidden = false;
                PlayerPhysicsComponent.setHiddenItemBeingShown(false);
                break;
        }
    }

    @Override
    public void onNotify(InventoryHudEvent event) {
        switch (event) {
            case INVENTORY_HUD_SHOWN:
                menuButton.setVisible(false);
                break;
            case INVENTORY_HUD_HIDDEN:
                menuButton.setVisible(true);
                break;
        }
    }

    @Override
    public void onNotify(QuestHudEvent event) {
        switch (event) {
            case QUEST_HUD_SHOWN:
                menuButton.setVisible(false);
                break;
            case QUEST_HUD_HIDDEN:
                menuButton.setVisible(true);
                break;
        }
    }

    @Override
    public void onNotify(MapEvent event, String value) {
        switch (event) {
            case DISPLAY_CONVERSATION:
                loadConversationForCutScene(value, this);
                doConversation();
                break;
        }
    }
}
