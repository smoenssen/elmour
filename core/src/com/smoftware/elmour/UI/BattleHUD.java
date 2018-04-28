package com.smoftware.elmour.UI;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Selection;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.smoftware.elmour.Component;
import com.smoftware.elmour.ComponentObserver;
import com.smoftware.elmour.ElmourGame;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.EntityConfig;
import com.smoftware.elmour.InventoryElement;
import com.smoftware.elmour.InventoryItem;
import com.smoftware.elmour.InventoryItem.ItemTypeID;
import com.smoftware.elmour.SpellsPowerElement;
import com.smoftware.elmour.Utility;
import com.smoftware.elmour.audio.AudioManager;
import com.smoftware.elmour.audio.AudioObserver;
import com.smoftware.elmour.audio.AudioSubject;
import com.smoftware.elmour.battle.BattleObserver;
import com.smoftware.elmour.dialog.ConversationChoice;
import com.smoftware.elmour.dialog.ConversationGraph;
import com.smoftware.elmour.dialog.ConversationGraphObserver;
import com.smoftware.elmour.maps.MapManager;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.profile.ProfileObserver;
import com.smoftware.elmour.quest.QuestGraph;
import com.smoftware.elmour.screens.BattleScreen;
import com.smoftware.elmour.screens.MainGameScreen;
import com.smoftware.elmour.sfx.ScreenTransitionAction;
import com.smoftware.elmour.sfx.ScreenTransitionActor;
import com.smoftware.elmour.sfx.ShakeCamera;

import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.Semaphore;

public class BattleHUD implements Screen, AudioSubject, ProfileObserver,ComponentObserver,ConversationGraphObserver,StoreInventoryObserver, BattleObserver, BattleControlsObserver, InventoryObserver, StatusObserver {
    private static final String TAG = BattleHUD.class.getSimpleName();

    public enum ScreenState { FIGHT, FINAL, INVENTORY, MAIN, MAGIC, MENU, POWER, SPELL_TYPE, SPELLS_WHITE, SPELLS_BLACK, STATS, UNKNOWN }
    private Stack<ScreenState> screenStack;

    // for keeping track of node's expanded state
    Array<rootNode> rootNodes = new Array<>();
    class rootNode {
        String name = "";
        boolean isExpanded = false;
        rootNode(String name, boolean isExpanded) {
            this.name = name;
            this.isExpanded = isExpanded;
        }
    }

    private final String SELECT_AN_ITEM = "Select an item";
    private final String SELECT_A_SPELL = "Select a spell";
    private final String SELECT_A_POWER = "Select a power";
    private final String CHOOSE_A_CHARACTER = "Choose a character";

    private final String BTN_NAME_INVENTORY = "Inventory";
    private final String BTN_NAME_FIGHT = "Fight";
    private final String BTN_NAME_RUN = "Run";
    private final String BTN_NAME_STATUS = "Status";
    private final String BTN_NAME_SPELLS = "Spells";
    private final String BTN_NAME_POWER = "Power";
    private final String BTN_NAME_ATTACK = "Attack";
    private final String BTN_NAME_WHITE = "White";
    private final String BTN_NAME_BLACK = "Black";
    private final String BTN_NAME_BACK = "Back";
;
    private Stage _stage;
    private Viewport _viewport;
    private Camera _camera;
    private Entity _player;

    //private ScreenState currentScreenState = ScreenState.MAIN;

    private StatusUI _statusUI;
    private InventoryUI _inventoryUI;
    private StoreInventoryUI _storeInventoryUI;
    private QuestUI _questUI;
    private BattleUI _battleUI;
    private SignPopUp signPopUp;

    private ConversationPopUp conversationPopUp;
    private ConversationLabel conversationLabel;
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

    final private float fadeTime = 0.35f;

    private TextButton topLeftButton;
    private TextButton topRightButton;
    private TextButton runButton;
    private TextButton statusButton;
    private TextButton dummyButtonLeft;
    private TextButton dummyButtonRight;

    private MyTextField selectedItemBanner;
    private float selectedItemBannerHeight;

    private Table leftNameTable;
    private MyTextArea leftTextArea;
    private Label monster1Name;
    private Label monster2Name;
    private Label monster3Name;
    private Label monster4Name;
    private Label monster5Name;

    private Table leftSummaryTable;
    private ScrollPane leftScrollPanel;
    private Label leftSummaryText;

    private float middleAreaWidth;

    // scrolling list
    private List<String> spellsPowerListView;
    private ScrollPane middleScrollPaneList;
    private ArrayList<SpellsPowerElement> spellsPowerList;
    private SpellsPowerElement selectedSpellsPowerElement;

    // scrolling tree
    private MyTextArea middleTreeTextArea;
    private Tree middleTree;
    private float middleTreeHeight;
    private ScrollPane middleScrollPaneTree;
    private ArrayList<InventoryElement> inventoryList;
    private InventoryElement selectedInventoryElement;

    // area under scrolling tree
    private float tablePadding = 15;
    private MyTextField middleStatsTextArea;    // using TextField because alignment doesn't work for TextAreas
    private Table middleTextAreaTable;
    private float backButtonHeight;
    private TextButton backButton;
    private TextButton spells_powerButton;
    private TextButton attackButton;

    private Table rightTable;
    private MyTextArea rightTextArea;
    private Label party1Name;
    private Label party2Name;
    private Label party3Name;
    private Label party4Name;
    private Label party5Name;

    private Image hpBar1;
    private Image mpBar1;
    private Image hpBar2;
    private Image mpBar2;
    private Image hpBar3;
    private Image mpBar3;
    private Image hpBar4;
    private Image mpBar4;
    private Image hpBar5;
    private Image mpBar5;

    private Label hp1Stats;
    private Label mp1Stats;
    private Label hp2Stats;
    private Label mp2Stats;
    private Label hp3Stats;
    private Label mp3Stats;
    private Label hp4Stats;
    private Label mp4Stats;
    private Label hp5Stats;
    private Label mp5Stats;

    private float barWidth = 0;
    private float barHeight = 0;

    private Dialog _messageBoxUI;
    private Label _label;
    private Json _json;
    private MapManager _mapMgr;

    private Array<AudioObserver> _observers;
    private ScreenTransitionActor _transitionActor;

    private ShakeCamera _shakeCam;

    BattleScreen battleScreen;

    private static final String INVENTORY_FULL = "Your inventory is full!";

    public BattleHUD(Camera camera, Entity player, MapManager mapMgr, BattleScreen screen) {
        _camera = camera;
        _player = player;
        _mapMgr = mapMgr;
        battleScreen = screen;

        _viewport = new FitViewport(ElmourGame.V_WIDTH, ElmourGame.V_HEIGHT, camera);
        _stage = new Stage(_viewport);
        //_stage.setDebugAll(true);

        screenStack = new Stack();
        screenStack.push(ScreenState.MAIN);

        isCutScene = false;

        _observers = new Array<AudioObserver>();
        _transitionActor = new ScreenTransitionActor();

        BattleControlsSubject.addObserver(this);

        _shakeCam = new ShakeCamera(0,0, 30.0f);

        _json = new Json();

        _label = new Label("Test", Utility.STATUSUI_SKIN);
        _label.setWrap(true);
        _messageBoxUI = new Dialog("", Utility.STATUSUI_SKIN, "solidbackground");
        _messageBoxUI.setVisible(false);
        _messageBoxUI.getContentTable().add(_label).width(_stage.getWidth()/2).pad(10, 10, 10, 0);
        _messageBoxUI.pack();
        _messageBoxUI.setPosition(_stage.getWidth() / 2 - _messageBoxUI.getWidth() / 2, _stage.getHeight() - _messageBoxUI.getHeight());

        _messageBoxUI.setVisible(false);
        _messageBoxUI.pack();
        _messageBoxUI.setPosition(_stage.getWidth() / 2 - _messageBoxUI.getWidth() / 2, _stage.getHeight() / 2 - _messageBoxUI.getHeight() / 2);

        _statusUI = new StatusUI();
        _statusUI.setVisible(true);
        _statusUI.setPosition(0, 0);
        _statusUI.setKeepWithinStage(false);
        _statusUI.setMovable(false);
        _statusUI.setVisible(false);

        _inventoryUI = new InventoryUI();
        _inventoryUI.setKeepWithinStage(false);
        _inventoryUI.setMovable(false);
        _inventoryUI.setVisible(false);
        _inventoryUI.setPosition(_statusUI.getWidth(), 0);

        _storeInventoryUI = new StoreInventoryUI();
        _storeInventoryUI.setMovable(false);
        _storeInventoryUI.setVisible(false);
        _storeInventoryUI.setPosition(0, 0);

        _questUI = new QuestUI();
        _questUI.setMovable(false);
        _questUI.setVisible(false);
        _questUI.setKeepWithinStage(false);
        _questUI.setPosition(0, _stage.getHeight() / 2);
        _questUI.setWidth(_stage.getWidth());
        _questUI.setHeight(_stage.getHeight() / 2);

        _battleUI = new BattleUI();
        _battleUI.setMovable(false);
        //removes all listeners including ones that handle focus
        _battleUI.clearListeners();
        _battleUI.setVisible(false);

        signPopUp = new SignPopUp();
        if (ElmourGame.isAndroid()) {
            signPopUp.setWidth(_stage.getWidth() / 1.1f);
            signPopUp.setHeight(_stage.getHeight() / 3.1f);
        }
        else {
            signPopUp.setWidth(_stage.getWidth() / 1.1f);
            signPopUp.setHeight(_stage.getHeight() / 4f);
        }
        signPopUp.setPosition(_stage.getWidth() / 2 - signPopUp.getWidth() / 2, 25);

        signPopUp.setVisible(false);

        conversationPopUp = new ConversationPopUp();
        if (ElmourGame.isAndroid()) {
            conversationPopUp.setWidth(_stage.getWidth() / 1.04f);
            conversationPopUp.setHeight(80);
        }
        else {
            conversationPopUp.setWidth(_stage.getWidth() / 1.04f);
            conversationPopUp.setHeight(80);
        }
        conversationPopUp.setPosition(_stage.getWidth() / 2 - conversationPopUp.getWidth() / 2, 12);

        conversationPopUp.setVisible(false);

        conversationLabel = new ConversationLabel();
        if (ElmourGame.isAndroid()) {
            conversationLabel.setWidth(140);
            conversationLabel.setHeight(24);
        }
        else {
            conversationLabel.setWidth(140);
            conversationLabel.setHeight(24);
        }
        conversationLabel.setPosition(conversationPopUp.getX() + 10, conversationPopUp.getY() + conversationPopUp.getHeight());

        conversationLabel.setVisible(false);

        choicePopUp1 = new ChoicePopUp();
        choicePopUp2 = new ChoicePopUp();
        choicePopUp3 = new ChoicePopUp();
        choicePopUp4 = new ChoicePopUp();

        choicePopUp1.hide();
        choicePopUp2.hide();
        choicePopUp3.hide();
        choicePopUp4.hide();

        isDelayedPopUp = false;
        conversationPopUpDelay = 0;
        numVisibleChoices = 0;
        isThereAnActiveHiddenChoice = false;
        isCurrentConversationDone = true;
        isExitingConversation = false;
        didSendConversationBeginMsg = false;
        didSendConversationDoneMsg = false;

        topLeftButton = new TextButton(BTN_NAME_INVENTORY, Utility.ELMOUR_UI_SKIN, "battle");
        topRightButton = new TextButton(BTN_NAME_FIGHT, Utility.ELMOUR_UI_SKIN, "battle");
        runButton = new TextButton(BTN_NAME_RUN, Utility.ELMOUR_UI_SKIN, "battle");
        statusButton = new TextButton(BTN_NAME_STATUS, Utility.ELMOUR_UI_SKIN, "battle");
        backButton = new TextButton(BTN_NAME_BACK, Utility.ELMOUR_UI_SKIN, "battle");
        dummyButtonLeft = new TextButton("", Utility.ELMOUR_UI_SKIN, "battle");
        dummyButtonRight = new TextButton("", Utility.ELMOUR_UI_SKIN, "battle");

        // Desktop
        float menuItemWidth = 100;
        float menuItemHeight = 75;
        float menuItemX = _stage.getWidth()/3.75f;
        float menuItemY = menuItemHeight;
        float leftTextAreaWidth = menuItemX;
        float selectedItemBannerWidth = 300;

        // Android
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            menuItemWidth = 85;
            menuItemHeight = 57;
            menuItemX = _stage.getWidth()/3.75f;
            menuItemY = menuItemHeight;
        }

        selectedItemBannerHeight = 40;
        selectedItemBanner = new MyTextField("", Utility.ELMOUR_UI_SKIN, "battle");
        selectedItemBanner.disabled = true;
        selectedItemBanner.setWidth(selectedItemBannerWidth);
        selectedItemBanner.setHeight(0);
        selectedItemBanner.setPosition((_stage.getWidth() - selectedItemBanner.getWidth())/2 , _stage.getHeight() + 8);
        selectedItemBanner.setAlignment(Align.center);
        selectedItemBanner.setVisible(true);

        topLeftButton.setWidth(menuItemWidth);
        topLeftButton.setHeight(menuItemHeight);
        topLeftButton.setPosition(menuItemX, menuItemY);
        topLeftButton.setVisible(true);

        menuItemX += menuItemWidth - 2;
        topRightButton.setWidth(menuItemWidth);
        topRightButton.setHeight(menuItemHeight);
        topRightButton.setPosition(menuItemX, menuItemY);
        topRightButton.setVisible(true);

        runButton.setWidth(menuItemWidth);
        runButton.setHeight(menuItemHeight);
        runButton.setPosition(topLeftButton.getX(), 2);
        runButton.setVisible(true);

        statusButton.setWidth(menuItemWidth);
        statusButton.setHeight(menuItemHeight);
        statusButton.setPosition(menuItemX, 2);
        statusButton.setVisible(true);

        dummyButtonLeft.setWidth(menuItemWidth);
        dummyButtonLeft.setHeight(menuItemHeight + 2);
        dummyButtonLeft.setPosition(topLeftButton.getX(), 0);
        dummyButtonLeft.setVisible(false);

        dummyButtonRight.setWidth(menuItemWidth);
        dummyButtonRight.setHeight(menuItemHeight + 2);
        dummyButtonRight.setPosition(menuItemX, 0);
        dummyButtonRight.setVisible(false);

        leftTextArea = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        leftTextArea.disabled = true;
        leftTextArea.setWidth(leftTextAreaWidth);
        leftTextArea.setHeight(menuItemHeight * 2 - 2);
        leftTextArea.setPosition(2, 2);
        leftTextArea.setVisible(true);

        monster1Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        monster1Name.setText("Royal Guard");

        monster2Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        monster2Name.setText("Scooby Doo");

        monster3Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        monster3Name.setText("Octomaniac");

        monster4Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        monster4Name.setText("Burping Crow");

        monster5Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        monster5Name.setText("Giant Blob");

        leftNameTable = new Table();

        // Desktop
        float nameWidth = 100;
        float nameHeight = 15;
        float topMargin = 0;
        float bottomMargin = 6.5f;
        float leftMargin = 8;
        float rightMargin = 0;

        // Android
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            nameWidth = 75;
            nameHeight = 12;
            topMargin = 0;
            bottomMargin = 4.5f;
            leftMargin = 8;
            rightMargin = 0;
        }

        leftNameTable.row().pad(topMargin, leftMargin, bottomMargin, rightMargin);
        leftNameTable.add(monster1Name).size(nameWidth, nameHeight);
        leftNameTable.row().pad(topMargin, leftMargin, bottomMargin, rightMargin);
        leftNameTable.add(monster2Name).size(nameWidth, nameHeight);
        leftNameTable.row().pad(topMargin, leftMargin, bottomMargin, rightMargin);
        leftNameTable.add(monster3Name).size(nameWidth, nameHeight);
        leftNameTable.row().pad(topMargin, leftMargin, bottomMargin, rightMargin);
        leftNameTable.add(monster4Name).size(nameWidth, nameHeight);
        leftNameTable.row().pad(topMargin, leftMargin, bottomMargin, rightMargin);
        leftNameTable.add(monster5Name).size(nameWidth, nameHeight);

        leftNameTable.pack();

        leftNameTable.setX(3);
        leftNameTable.setY(4);


        float leftSummaryAreaWidth = leftTextAreaWidth;
        float leftSummaryAreaHeight = menuItemHeight * 2 - 6;
        leftSummaryText = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        leftSummaryText.setWidth(leftSummaryAreaWidth);
        leftSummaryText.setHeight(leftSummaryAreaHeight);
        leftSummaryText.setWrap(true);

        leftScrollPanel = new ScrollPane(leftSummaryText);

        leftSummaryTable = new Table();
        leftSummaryTable.setHeight(leftSummaryAreaHeight);
        leftSummaryTable.setWidth(leftSummaryAreaWidth);
        leftSummaryTable.pad(10).defaults().expandX().fillX().space(4);
        leftSummaryTable.setPosition(2, 4);
        leftSummaryTable.row().height(leftSummaryAreaHeight);
        leftSummaryTable.add(leftScrollPanel);

        float rightTextAreaWidth = _stage.getWidth() - (statusButton.getWidth() * 2) - leftTextArea.getWidth() + 2;
        middleAreaWidth = menuItemWidth * 2 - 2;//(_stage.getWidth() - rightTextAreaWidth) / 2f;

        middleStatsTextArea = new MyTextField("", Utility.ELMOUR_UI_SKIN, "battle");
        middleStatsTextArea.disabled = true;
        middleStatsTextArea.setAlignment(Align.center);
        middleStatsTextArea.setWidth(middleAreaWidth);
        middleStatsTextArea.setHeight(menuItemHeight * 2 - 2);
        middleStatsTextArea.setPosition(leftTextAreaWidth, 2);
        middleStatsTextArea.setVisible(false);

        middleTreeHeight = menuItemHeight * 2;

        middleTreeTextArea = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        middleTreeTextArea.disabled = true;
        middleTreeTextArea.setWidth(middleAreaWidth);
        middleTreeTextArea.setHeight(0);
        middleTreeTextArea.setPosition(_stage.getWidth() - rightTextAreaWidth - middleAreaWidth, menuItemHeight * 2 - 2);
        middleTreeTextArea.setVisible(false);

        middleTree = new Tree(Utility.ELMOUR_UI_SKIN);

        // load tree from inventory.json, for battle only show Potion, Food, and Consumables

        Tree.Node Potions = new Tree.Node(new TextButton("Potions", Utility.ELMOUR_UI_SKIN, "no_background"));
        Tree.Node Food = new Tree.Node(new TextButton("Food", Utility.ELMOUR_UI_SKIN, "no_background"));
        Tree.Node Consumables = new Tree.Node(new TextButton("Consumables", Utility.ELMOUR_UI_SKIN, "no_background"));
        rootNodes.add(new rootNode("Potions", false));
        rootNodes.add(new rootNode("Food", false));
        rootNodes.add(new rootNode("Consumables", false));

        middleTree.add(Potions);
        middleTree.add(Food);
        middleTree.add(Consumables);

        // load inventory and spells/powers from json files
        Json json = new Json();
        inventoryList = json.fromJson(ArrayList.class, InventoryElement.class, Gdx.files.internal("RPGGame/maps/Game/Scripts/Inventory.json"));
        spellsPowerList = json.fromJson(ArrayList.class, SpellsPowerElement.class, Gdx.files.internal("RPGGame/maps/Game/Scripts/Spell.json"));

        for (InventoryElement element : inventoryList) {

            Tree.Node node = new Tree.Node(new TextButton(element.name, Utility.ELMOUR_UI_SKIN, "no_background"));
            node.setObject(element);
            switch(element.category) {
                case Potion:
                    Potions.add(node);
                    break;
                case Food:
                    Food.add(node);
                    break;
                case Consumables:
                    Consumables.add(node);
                    break;
            }
        }

        spellsPowerListView = new List<>(Utility.ELMOUR_UI_SKIN);
        middleScrollPaneList = new ScrollPane(spellsPowerListView);
        middleScrollPaneList.setWidth(middleTreeTextArea.getWidth() - 4);
        middleScrollPaneList.setHeight(0);
        middleScrollPaneList.setPosition(middleTreeTextArea.getX() + 2, menuItemHeight * 2);

        // set padding on left side of list elements
        Utility.ELMOUR_UI_SKIN.get(List.ListStyle.class).selection.setLeftWidth(15);

        middleScrollPaneTree = new ScrollPane(middleTree);
        middleScrollPaneTree.setWidth(middleTreeTextArea.getWidth() - 4);
        middleScrollPaneTree.setHeight(0);
        middleScrollPaneTree.setPosition(middleTreeTextArea.getX() + 2, menuItemHeight * 2);
        middleTree.setVisible(false);

        middleTextAreaTable = new Table();
        middleTextAreaTable.setWidth(middleAreaWidth - (2 * tablePadding));
        middleTextAreaTable.setHeight(menuItemHeight * 2 - 5);
        middleTextAreaTable.setPosition(middleTreeTextArea.getX() + tablePadding, 0);
        middleTextAreaTable.align(Align.top);

        backButtonHeight = menuItemHeight;
        backButton.setWidth(middleAreaWidth);
        backButton.setHeight(0);
        backButton.setPosition(middleTreeTextArea.getX(), 2);
        backButton.setVisible(false);

        rightTextArea = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        rightTextArea.disabled = true;
        rightTextArea.setWidth(rightTextAreaWidth);
        rightTextArea.setHeight(menuItemHeight * 2 - 2);
        rightTextArea.setPosition(statusButton.getX() + statusButton.getWidth() - 2, 2);
        rightTextArea.setVisible(true);

        party1Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        party1Name.setText("Carmen");

        party2Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        party2Name.setText("Character One");

        party3Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        party3Name.setText("Character Two");

        party4Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        party4Name.setText("Justin");

        party5Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        party5Name.setText("Jaxon");

        // stats
        hp1Stats = new Label("", Utility.ELMOUR_UI_SKIN, "stats");
        mp1Stats = new Label("", Utility.ELMOUR_UI_SKIN, "stats");
        hp2Stats = new Label("", Utility.ELMOUR_UI_SKIN, "stats");
        mp2Stats = new Label("", Utility.ELMOUR_UI_SKIN, "stats");
        hp3Stats = new Label("", Utility.ELMOUR_UI_SKIN, "stats");
        mp3Stats = new Label("", Utility.ELMOUR_UI_SKIN, "stats");
        hp4Stats = new Label("", Utility.ELMOUR_UI_SKIN, "stats");
        mp4Stats = new Label("", Utility.ELMOUR_UI_SKIN, "stats");
        hp5Stats = new Label("", Utility.ELMOUR_UI_SKIN, "stats");
        mp5Stats = new Label("", Utility.ELMOUR_UI_SKIN, "stats");

        // status bar groups
        WidgetGroup groupHp1 = new WidgetGroup();
        WidgetGroup groupMp1 = new WidgetGroup();
        WidgetGroup groupHp2 = new WidgetGroup();
        WidgetGroup groupMp2 = new WidgetGroup();
        WidgetGroup groupHp3 = new WidgetGroup();
        WidgetGroup groupMp3 = new WidgetGroup();
        WidgetGroup groupHp4 = new WidgetGroup();
        WidgetGroup groupMp4 = new WidgetGroup();
        WidgetGroup groupHp5 = new WidgetGroup();
        WidgetGroup groupMp5 = new WidgetGroup();

        Image blackbar1hp = new Image(new Texture("graphics/black_bar.png"));
        Image whitebar1hp = new Image(new Texture("graphics/white_bar.png"));
        Image blackbar1mp = new Image(new Texture("graphics/black_bar.png"));
        Image whitebar1mp = new Image(new Texture("graphics/white_bar.png"));
        Image blackbar2hp = new Image(new Texture("graphics/black_bar.png"));
        Image whitebar2hp = new Image(new Texture("graphics/white_bar.png"));
        Image blackbar2mp = new Image(new Texture("graphics/black_bar.png"));
        Image whitebar2mp = new Image(new Texture("graphics/white_bar.png"));
        Image blackbar3hp = new Image(new Texture("graphics/black_bar.png"));
        Image whitebar3hp = new Image(new Texture("graphics/white_bar.png"));
        Image blackbar3mp = new Image(new Texture("graphics/black_bar.png"));
        Image whitebar3mp = new Image(new Texture("graphics/white_bar.png"));
        Image blackbar4hp = new Image(new Texture("graphics/black_bar.png"));
        Image whitebar4hp = new Image(new Texture("graphics/white_bar.png"));
        Image blackbar4mp = new Image(new Texture("graphics/black_bar.png"));
        Image whitebar4mp = new Image(new Texture("graphics/white_bar.png"));
        Image blackbar5hp = new Image(new Texture("graphics/black_bar.png"));
        Image whitebar5hp = new Image(new Texture("graphics/white_bar.png"));
        Image blackbar5mp = new Image(new Texture("graphics/black_bar.png"));
        Image whitebar5mp = new Image(new Texture("graphics/white_bar.png"));

        hpBar1 = new Image(new Texture("graphics/hp_bar.png"));
        mpBar1 = new Image(new Texture("graphics/mp_bar.png"));
        hpBar2 = new Image(new Texture("graphics/hp_bar.png"));
        mpBar2 = new Image(new Texture("graphics/mp_bar.png"));
        hpBar3 = new Image(new Texture("graphics/hp_bar.png"));
        mpBar3 = new Image(new Texture("graphics/mp_bar.png"));
        hpBar4 = new Image(new Texture("graphics/hp_bar.png"));
        mpBar4 = new Image(new Texture("graphics/mp_bar.png"));
        hpBar5 = new Image(new Texture("graphics/hp_bar.png"));
        mpBar5 = new Image(new Texture("graphics/mp_bar.png"));

        barWidth = 45;
        barHeight = 6;

        //Align images
        initStatusBars(blackbar1hp, whitebar1hp, hpBar1, hp1Stats);
        initStatusBars(blackbar1mp, whitebar1mp, mpBar1, mp1Stats);
        initStatusBars(blackbar2hp, whitebar2hp, hpBar2, hp2Stats);
        initStatusBars(blackbar2mp, whitebar2mp, mpBar2, mp2Stats);
        initStatusBars(blackbar3hp, whitebar3hp, hpBar3, hp3Stats);
        initStatusBars(blackbar3mp, whitebar3mp, mpBar3, mp3Stats);
        initStatusBars(blackbar4hp, whitebar4hp, hpBar4, hp4Stats);
        initStatusBars(blackbar4mp, whitebar4mp, mpBar4, mp4Stats);
        initStatusBars(blackbar5hp, whitebar5hp, hpBar5, hp5Stats);
        initStatusBars(blackbar5mp, whitebar5mp, mpBar5, mp5Stats);

        hp1Stats.setText("15/30");
        mp1Stats.setText("25/35");
        hp2Stats.setText("35/900");
        mp2Stats.setText("1500/3000");
        hp3Stats.setText("15/30");
        mp3Stats.setText("25/35");
        hp4Stats.setText("15/30");
        mp4Stats.setText("25/35");
        hp5Stats.setText("15/30");
        mp5Stats.setText("25/35");

        //add to widget groups
        groupHp1.addActor(blackbar1hp);
        groupHp1.addActor(whitebar1hp);
        groupHp1.addActor(hpBar1);
        groupHp1.addActor(hp1Stats);
        groupMp1.addActor(blackbar1mp);
        groupMp1.addActor(whitebar1mp);
        groupMp1.addActor(mpBar1);
        groupMp1.addActor(mp1Stats);

        groupHp2.addActor(blackbar2hp);
        groupHp2.addActor(whitebar2hp);
        groupHp2.addActor(hpBar2);
        groupHp2.addActor(hp2Stats);
        groupMp2.addActor(blackbar2mp);
        groupMp2.addActor(whitebar2mp);
        groupMp2.addActor(mpBar2);
        groupMp2.addActor(mp2Stats);

        groupHp3.addActor(blackbar3hp);
        groupHp3.addActor(whitebar3hp);
        groupHp3.addActor(hpBar3);
        groupHp3.addActor(hp3Stats);
        groupMp3.addActor(blackbar3mp);
        groupMp3.addActor(whitebar3mp);
        groupMp3.addActor(mpBar3);
        groupMp3.addActor(mp3Stats);

        groupHp4.addActor(blackbar4hp);
        groupHp4.addActor(whitebar4hp);
        groupHp4.addActor(hpBar4);
        groupHp4.addActor(hp4Stats);
        groupMp4.addActor(blackbar4mp);
        groupMp4.addActor(whitebar4mp);
        groupMp4.addActor(mpBar4);
        groupMp4.addActor(mp4Stats);

        groupHp5.addActor(blackbar5hp);
        groupHp5.addActor(whitebar5hp);
        groupHp5.addActor(hpBar5);
        groupHp5.addActor(hp5Stats);
        groupMp5.addActor(blackbar5mp);
        groupMp5.addActor(whitebar5mp);
        groupMp5.addActor(mpBar5);
        groupMp5.addActor(mp5Stats);

        // Desktop
        nameWidth = 100;
        nameHeight = 15;
        topMargin = 2;
        bottomMargin = 6.5f;
        leftMargin = 8;
        rightMargin = 0;

        // Android
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            nameWidth = 75;
            nameHeight = 12;
            topMargin = 2;
            bottomMargin = 4.5f;
            leftMargin = 8;
            rightMargin = 0;
        }

        float spaceBetweenBars = 20;

        // layout table
        rightTable = new Table();

        Label dummy = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        Label HP = new Label("", Utility.ELMOUR_UI_SKIN, "stats");
        HP.setText("HP");
        Label MP = new Label("", Utility.ELMOUR_UI_SKIN, "stats");
        MP.setText("MP");

        HP.setWidth(barWidth);
        HP.setHeight(barHeight);
        HP.setPosition(2, -5f);
        HP.setAlignment(Align.center);

        MP.setWidth(barWidth);
        MP.setHeight(barHeight);
        MP.setPosition(barWidth/2 + 7, -5f);
        MP.setAlignment(Align.center);

        WidgetGroup groupHP = new WidgetGroup();
        groupHP.addActor(HP);
        WidgetGroup groupMP = new WidgetGroup();
        groupMP.addActor(MP);

        //rightTable.row().pad(topMargin, leftMargin, bottomMargin, rightMargin);
        rightTable.add(dummy).size(nameWidth, nameHeight);
        rightTable.add(groupHP);//.align(Align.center);//.size(barWidth, barHeight);
        rightTable.add(groupMP);//.align(Align.center);//.size(nameWidth, nameHeight);

        rightTable.row().pad(topMargin, leftMargin, bottomMargin, rightMargin);
        rightTable.add(party1Name).size(nameWidth, nameHeight);
        rightTable.add(groupHp1);
        rightTable.add(groupMp1).padLeft(barWidth + spaceBetweenBars);

        rightTable.row().pad(topMargin, leftMargin, bottomMargin, rightMargin);
        rightTable.add(party2Name).size(nameWidth, nameHeight);
        rightTable.add(groupHp2);
        rightTable.add(groupMp2).padLeft(barWidth + spaceBetweenBars);

        rightTable.row().pad(topMargin, leftMargin, bottomMargin, rightMargin);
        rightTable.add(party3Name).size(nameWidth, nameHeight);
        rightTable.add(groupHp3);
        rightTable.add(groupMp3).padLeft(barWidth + spaceBetweenBars);

        rightTable.row().pad(topMargin, leftMargin, bottomMargin, rightMargin);
        rightTable.add(party4Name).size(nameWidth, nameHeight);
        rightTable.add(groupHp4);
        rightTable.add(groupMp4).padLeft(barWidth + spaceBetweenBars);

        rightTable.row().pad(topMargin, leftMargin, bottomMargin + 3, rightMargin);
        rightTable.add(party5Name).size(nameWidth, nameHeight);
        rightTable.add(groupHp5);
        rightTable.add(groupMp5).padLeft(barWidth + spaceBetweenBars);

        rightTable.pack();

        rightTable.setX(rightTextArea.getX());
        rightTable.setY(4);


        //_stage.addActor(_battleUI);
        //_stage.addActor(_questUI);
        //_stage.addActor(_storeInventoryUI);
        //_stage.addActor(_messageBoxUI);
        //_stage.addActor(_statusUI);
        //_stage.addActor(_inventoryUI);

        /*_stage.addActor(signPopUp);
        _stage.addActor(conversationPopUp);
        _stage.addActor(conversationLabel);
        _stage.addActor(choicePopUp1);
        _stage.addActor(choicePopUp2);
        _stage.addActor(choicePopUp3);
        _stage.addActor(choicePopUp4);*/
        _stage.addActor(selectedItemBanner);
        _stage.addActor(middleStatsTextArea);
        _stage.addActor(middleTextAreaTable);

        _stage.addActor(topLeftButton);
        _stage.addActor(topRightButton);
        _stage.addActor(runButton);
        _stage.addActor(statusButton);
        _stage.addActor(leftTextArea);
        _stage.addActor(leftNameTable);
        _stage.addActor(leftSummaryTable);
        _stage.addActor(backButton);
        _stage.addActor(dummyButtonLeft);
        _stage.addActor(dummyButtonRight);
        _stage.addActor(middleTreeTextArea);
        _stage.addActor(middleScrollPaneTree);
        _stage.addActor(middleScrollPaneList);
        _stage.addActor(rightTextArea);
        _stage.addActor(rightTable);

        //if (ElmourGame.DEV_MODE)
        //  _stage.addActor(statusButton);

        _battleUI.validate();
        _questUI.validate();
        _storeInventoryUI.validate();
        _messageBoxUI.validate();
        _statusUI.validate();
        _inventoryUI.validate();

        //add tooltips to the stage
        Array<Actor> actors = _inventoryUI.getInventoryActors();
        for(Actor actor : actors){
            _stage.addActor(actor);
        }

        Array<Actor> storeActors = _storeInventoryUI.getInventoryActors();
        for(Actor actor : storeActors ){
            _stage.addActor(actor);
        }

        _stage.addActor(_transitionActor);
        _transitionActor.setVisible(false);

        //Observers
        _player.registerObserver(this);
        _statusUI.addObserver(this);
        _storeInventoryUI.addObserver(this);
        _inventoryUI.addObserver(_battleUI.getCurrentState());
        _inventoryUI.addObserver(this);
        _battleUI.getCurrentState().addObserver(this);
        this.addObserver(AudioManager.getInstance());

        //Listeners
        topLeftButton.addListener(new ClickListener() {
                                        @Override
                                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                            return true;
                                        }

                                        @Override
                                        public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                            // make sure touch point is still on this button
                                            if (touchPointIsInButton(topLeftButton)) {
                                                ScreenState currentScreenState = ScreenState.MAIN;

                                                if (screenStack.size() > 0) {
                                                    currentScreenState = screenStack.peek();
                                                }

                                                if (topLeftButton.getText().toString().equals(BTN_NAME_INVENTORY) && currentScreenState == ScreenState.MAIN) {
                                                    setHUDNewState(ScreenState.INVENTORY);
                                                }
                                                else if (topLeftButton.getText().toString().equals(BTN_NAME_POWER) && currentScreenState == ScreenState.FIGHT) {
                                                    // load powers
                                                    int numElements = 0;
                                                    for (SpellsPowerElement element : spellsPowerList) {
                                                        if (element.category == SpellsPowerElement.SpellPowerCategory.Power) {
                                                            numElements++;
                                                        }
                                                    }

                                                    String[] strings = new String[numElements];
                                                    int index = 0;
                                                    for (SpellsPowerElement element : spellsPowerList) {
                                                        if (element.category == SpellsPowerElement.SpellPowerCategory.Power) {
                                                            strings[index++] = element.name;
                                                        }
                                                    }

                                                    spellsPowerListView.setItems(strings);
                                                    spellsPowerListView.setSelectedIndex(-1);
                                                    setHUDNewState(ScreenState.POWER);
                                                }
                                                else if (topLeftButton.getText().toString().equals(BTN_NAME_SPELLS) && currentScreenState == ScreenState.FIGHT) {
                                                    setHUDNewState(ScreenState.SPELL_TYPE);
                                                }
                                                else if (topLeftButton.getText().toString().equals(BTN_NAME_WHITE) && currentScreenState == ScreenState.SPELL_TYPE) {
                                                    // load white spells
                                                    int numElements = 0;
                                                    for (SpellsPowerElement element : spellsPowerList) {
                                                        if (element.category == SpellsPowerElement.SpellPowerCategory.White) {
                                                            numElements++;
                                                        }
                                                    }

                                                    String[] strings = new String[numElements];
                                                    int index = 0;
                                                    for (SpellsPowerElement element : spellsPowerList) {
                                                        if (element.category == SpellsPowerElement.SpellPowerCategory.White) {
                                                            strings[index++] = element.name;
                                                        }
                                                    }

                                                    spellsPowerListView.setItems(strings);
                                                    spellsPowerListView.setSelectedIndex(-1);
                                                    setHUDNewState(ScreenState.SPELLS_WHITE);
                                                }
                                            }
                                        }
                                    }
        );

        topRightButton.addListener(new ClickListener() {
                                      @Override
                                      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                          return true;
                                      }

                                      @Override
                                      public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                          // make sure touch point is still on this button
                                          if (touchPointIsInButton(topRightButton)) {
                                              ScreenState currentScreenState = ScreenState.MAIN;

                                              if (screenStack.size() > 0) {
                                                  currentScreenState = screenStack.peek();
                                              }

                                              if (topRightButton.getText().toString().equals(BTN_NAME_FIGHT) && currentScreenState == ScreenState.MAIN) {
                                                  setHUDNewState(ScreenState.FIGHT);
                                              }
                                              else if (topRightButton.getText().toString().equals(BTN_NAME_BLACK) && currentScreenState == ScreenState.SPELL_TYPE) {
                                                  // load black spells
                                                  int numElements = 0;
                                                  for (SpellsPowerElement element : spellsPowerList) {
                                                      if (element.category == SpellsPowerElement.SpellPowerCategory.Black) {
                                                          numElements++;
                                                      }
                                                  }

                                                  String[] strings = new String[numElements];
                                                  int index = 0;
                                                  for (SpellsPowerElement element : spellsPowerList) {
                                                      if (element.category == SpellsPowerElement.SpellPowerCategory.Black) {
                                                          strings[index++] = element.name;
                                                      }
                                                  }

                                                  spellsPowerListView.setItems(strings);
                                                  spellsPowerListView.setSelectedIndex(-1);
                                                  setHUDNewState(ScreenState.SPELLS_BLACK);
                                              }
/* testing only for writing to .json file
                                              ArrayList elements = new ArrayList<>();

                                              InventoryElement element = new InventoryElement();
                                              element.category = InventoryElement.InventoryCategory.Food;
                                              element.name = "Broccoli";
                                              element.summary = "Strengthens the person's bones.";
                                              element.buy = 123;
                                              element.sell = 75;
                                              element.turns = 3;
                                              element.revive = true;
                                              element.effectList = new Array<>();
                                              InventoryElement.EffectItem item = new InventoryElement.EffectItem();
                                              item.effect = InventoryElement.Effect.HEAL_HP;
                                              item.value = 10;
                                              InventoryElement.EffectItem item2 = new InventoryElement.EffectItem();
                                              item2.effect = InventoryElement.Effect.DEF_UP;
                                              item2.value = 10;
                                              element.effectList.add(item);
                                              element.effectList.add(item2);

                                              elements.add(element);

                                              InventoryElement element2 = new InventoryElement();
                                              element2.category = InventoryElement.InventoryCategory.Food;
                                              element2.name = "Steak";
                                              element2.summary = "Gives a large burst of energy to whoever consumes it.";
                                              element2.buy = 100;
                                              element2.sell = 50;
                                              element2.turns = 3;
                                              element2.effectList = new Array<>();
                                              InventoryElement.EffectItem item3 = new InventoryElement.EffectItem();
                                              item3.effect = InventoryElement.Effect.HEAL_HP;
                                              item3.value = 30;
                                              InventoryElement.EffectItem item4 = new InventoryElement.EffectItem();
                                              item4.effect = InventoryElement.Effect.DEF_UP;
                                              item4.value = 15;
                                              element2.effectList.add(item3);
                                              element2.effectList.add(item4);

                                              elements.add(element2);

                                              Json json = new Json();

                                              ArrayList<InventoryElement> list = new ArrayList<>();
                                              list.add(element);
                                              list.add(element2);
                                              json.toJson(list);

                                              FileHandle file = Gdx.files.local("scripts/InventoryTest.json");
                                              file.writeString(json.prettyPrint(list), false);
 test .json file*/
                                          }
                                      }
                                  }
        );

        runButton.addListener(new ClickListener() {
                                   @Override
                                   public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                       return true;
                                   }

                                   @Override
                                   public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                       // make sure touch point is still on this button
                                       if (touchPointIsInButton(runButton)) {
                                           ScreenState currentScreenState = ScreenState.MAIN;

                                           if (screenStack.size() > 0) {
                                               currentScreenState = screenStack.peek();
                                           }

                                           if (currentScreenState == ScreenState.MAIN) {
                                               Gdx.app.log(TAG, "run button up");
                                           }
                                       }
                                   }
                               }
        );

        statusButton.addListener(new ClickListener() {
                                       @Override
                                       public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                           return true;
                                       }

                                       @Override
                                       public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                           // make sure touch point is still on this button
                                           if (touchPointIsInButton(statusButton)) {
                                               ScreenState currentScreenState = ScreenState.MAIN;

                                               if (screenStack.size() > 0) {
                                                   currentScreenState = screenStack.peek();
                                               }

                                               if (currentScreenState == ScreenState.MAIN) {
                                                   Gdx.app.log(TAG, "status button up");
                                               }
                                           }
                                       }
                                   }
        );

        backButton.addListener(new ClickListener() {
                                     @Override
                                     public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                         return true;
                                     }

                                     @Override
                                     public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                                         // make sure touch point is still on this button
                                         if (touchPointIsInButton(backButton)) {
                                             setHUDPreviousState();
                                         }
                                     }
                                 }
        );

        /*
        _storeInventoryUI.getCloseButton().addListener(new ClickListener() {
                                                           @Override
                                                           public void clicked(InputEvent event, float x, float y) {
                                                               _storeInventoryUI.savePlayerInventory();
                                                               _storeInventoryUI.cleanupStoreInventory();
                                                               _storeInventoryUI.setVisible(false);
                                                               _mapMgr.clearCurrentSelectedMapEntity();
                                                           }
                                                       }
        );
        */

        spellsPowerListView.addListener(new ClickListener() {
                                    @Override
                                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                        return true;
                                    }

                                    @Override
                                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                        Gdx.app.log(TAG, "list clicked " + spellsPowerListView.getSelected());

                                        // get associated element from spells/power list based on name
                                        SpellsPowerElement element = null;
                                        for (SpellsPowerElement item : spellsPowerList) {
                                            if (item.name.equals(spellsPowerListView.getSelected())) {
                                                element = item;
                                                break;
                                            }
                                        }

                                        if (element != null) {
                                            selectedSpellsPowerElement = element;

                                            // replace asterisks in summary with a comma
                                            String summary = selectedSpellsPowerElement.summary.replace('*', ',');
                                            leftSummaryText.setText(summary);

                                            middleTextAreaTable.clear();
                                            middleStatsTextArea.setText("", true);
                                            middleTextAreaTable.setVisible(true);

                                            if (selectedSpellsPowerElement.MP != 0) {
                                                middleTextAreaTable.row().width(middleAreaWidth/2 - tablePadding);

                                                Label stat = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
                                                stat.setText("MP");
                                                stat.setAlignment(Align.left);
                                                Label value = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
                                                value.setText(String.format("%d", selectedSpellsPowerElement.MP));
                                                value.setAlignment(Align.right);

                                                middleTextAreaTable.add(stat).align(Align.left);
                                                middleTextAreaTable.add(value).align(Align.right);//.padLeft(0);
                                            }
                                            if (selectedSpellsPowerElement.DMG != 0) {
                                                middleTextAreaTable.row().width(middleAreaWidth/2 - tablePadding);

                                                Label stat = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
                                                stat.setText("DMG");
                                                stat.setAlignment(Align.left);
                                                Label value = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
                                                value.setText(String.format("%d", selectedSpellsPowerElement.DMG));
                                                value.setAlignment(Align.right);

                                                middleTextAreaTable.add(stat).align(Align.left);
                                                middleTextAreaTable.add(value).align(Align.right);//.padLeft(0);
                                            }
                                            if (selectedSpellsPowerElement.ACC != 0) {
                                                middleTextAreaTable.row().width(middleAreaWidth/2 - tablePadding);

                                                Label stat = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
                                                stat.setText("ACC");
                                                stat.setAlignment(Align.left);
                                                Label value = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
                                                value.setText(String.format("%d", selectedSpellsPowerElement.ACC));
                                                value.setAlignment(Align.right);

                                                middleTextAreaTable.add(stat).align(Align.left);
                                                middleTextAreaTable.add(value).align(Align.right);//.padLeft(0);
                                            }
                                            if (selectedSpellsPowerElement.effectList != null) {
                                                for (SpellsPowerElement.EffectItem effect : selectedSpellsPowerElement.effectList) {
                                                    middleTextAreaTable.row().width(middleAreaWidth/2 - tablePadding);

                                                    Label stat = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
                                                    stat.setText(effect.effect.toString());
                                                    stat.setAlignment(Align.left);
                                                    Label value = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
                                                    value.setText(effect.value.toString());
                                                    value.setAlignment(Align.right);

                                                    middleTextAreaTable.add(stat).align(Align.left);
                                                    middleTextAreaTable.add(value).align(Align.right);
                                                }
                                            }
                                        }
                                    }
                                }
        );

        middleTree.addListener(new ClickListener() {
                                   @Override
                                   public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                       return true;
                                   }

                                   @Override
                                   public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                       Selection<Tree.Node> selection = middleTree.getSelection();
                                       for (Tree.Node node : selection) {
                                           // get selected tree item
                                           InventoryElement element = (InventoryElement) node.getObject();
                                           if (element != null) {
                                               selectedInventoryElement = element;
                                               Gdx.app.log(TAG, selectedInventoryElement.name);

                                               // replace asterisks in summary with a comma
                                               String summary = selectedInventoryElement.summary.replace('*', ',');
                                               leftSummaryText.setText(summary);

                                               middleTextAreaTable.clear();
                                               middleStatsTextArea.setText("", true);
                                               middleTextAreaTable.setVisible(true);

                                               String effectList = "";
                                               for (InventoryElement.EffectItem effect : selectedInventoryElement.effectList) {
                                                   middleTextAreaTable.row().width(middleAreaWidth/2 - tablePadding);

                                                   Label stat = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
                                                   stat.setText(effect.effect.toString());
                                                   stat.setAlignment(Align.left);
                                                   Label value = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
                                                   value.setText(effect.value.toString());
                                                   value.setAlignment(Align.right);

                                                   middleTextAreaTable.add(stat).align(Align.left);
                                                   middleTextAreaTable.add(value).align(Align.right);
                                               }
                                           }
                                           else {
                                               leftSummaryText.setText("");
                                               middleStatsTextArea.setText("", true);
                                               middleTextAreaTable.clear();
                                               middleStatsTextArea.setText(SELECT_AN_ITEM, true);

                                               // expand or collapse if root node selected
                                               TextButton btn = (TextButton)node.getActor();
                                               if (btn != null) {
                                                   for (rootNode r : rootNodes) {
                                                       if (r.name.equals(btn.getLabel().getText().toString())) {
                                                           if (r.isExpanded) {
                                                               r.isExpanded = false;
                                                               node.collapseAll();
                                                           }
                                                           else {
                                                               r.isExpanded = true;
                                                               node.expandAll();
                                                           }
                                                       }
                                                   }
                                               }

                                               // clear selection so next root node selection is not unselected
                                               middleTree.getSelection().clear();

                                               // DON"T DO THIS!! It actually clears out the element's info
                                               //selectedInventoryElement.summary = "";
                                               //selectedInventoryElement.effectList.clear();
                                           }

                                           // should only be one node selected
                                           break;
                                       }
                                   }
                               }
        );

        //Music/Sound loading
        notify(AudioObserver.AudioCommand.MUSIC_LOAD, AudioObserver.AudioTypeEvent.MUSIC_BATTLE);
        notify(AudioObserver.AudioCommand.MUSIC_LOAD, AudioObserver.AudioTypeEvent.MUSIC_LEVEL_UP_FANFARE);
        notify(AudioObserver.AudioCommand.SOUND_LOAD, AudioObserver.AudioTypeEvent.SOUND_COIN_RUSTLE);
        notify(AudioObserver.AudioCommand.SOUND_LOAD, AudioObserver.AudioTypeEvent.SOUND_CREATURE_PAIN);
        notify(AudioObserver.AudioCommand.SOUND_LOAD, AudioObserver.AudioTypeEvent.SOUND_PLAYER_PAIN);
        notify(AudioObserver.AudioCommand.SOUND_LOAD, AudioObserver.AudioTypeEvent.SOUND_PLAYER_WAND_ATTACK);
        notify(AudioObserver.AudioCommand.SOUND_LOAD, AudioObserver.AudioTypeEvent.SOUND_EATING);
        notify(AudioObserver.AudioCommand.SOUND_LOAD, AudioObserver.AudioTypeEvent.SOUND_DRINKING);
    }

    public class setTextAreaText extends Action {
        MyTextField textArea = null;
        String text = "";

        public setTextAreaText(MyTextField textArea, String text) {
            this.textArea = textArea;
            this.text = text;
        }

        @Override
        public boolean act (float delta) {
            textArea.setText(text, true);
            return true; // An action returns true when it's completed
        }
    }

    public class setButtonText extends Action {
        TextButton btn = null;
        String text = "";

        public setButtonText(TextButton btn, String text) {
            this.btn = btn;
            this.text = text;
        }

        @Override
        public boolean act (float delta) {
            btn.setText(text);
            return true; // An action returns true when it's completed
        }
    }

    private void initStatusBars(Image blackbar, Image whitebar, Image statusBar, Label stats) {
        blackbar.setWidth(barWidth);
        blackbar.setHeight(barHeight);
        blackbar.setPosition(0, 0);

        whitebar.setWidth(barWidth - 2);
        whitebar.setHeight(barHeight - 2);
        whitebar.setPosition(1, 1);

        statusBar.setWidth(barWidth - 16);
        statusBar.setHeight(barHeight - 2);
        statusBar.setPosition(1, 1);

        stats.setAlignment(Align.center);
        stats.setWidth(barWidth - 2);
        stats.setPosition(1, -6);
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

    private void hideMainMenu() {
        topLeftButton.setVisible(false);
        topRightButton.setVisible(false);
        runButton.setVisible(false);
        statusButton.setVisible(false);
    }

    private void completeAllActions() {
        float delta = 3 * fadeTime;

        // need to loop multiple times in case there is an embedded sequence
        // shouldn't ever need anymore than 5
        for (int i = 0; i < 5; i++) {
            middleStatsTextArea.act(delta);
            middleTextAreaTable.act(delta);
            topLeftButton.act(delta);
            topRightButton.act(delta);
            runButton.act(delta);
            statusButton.act(delta);
            leftTextArea.act(delta);
            leftNameTable.act(delta);
            backButton.act(delta);
            backButton.act(delta);
            dummyButtonLeft.act(delta);
            dummyButtonRight.act(delta);
            middleTreeTextArea.act(delta);
            middleScrollPaneTree.act(delta);
        }
    }
    private void setHUDNewState(ScreenState newState) {

        Gdx.app.log(TAG, "setting new HUD state " + newState.toString());

        battleScreen.setBattleControls(newState);
        ScreenState currentScreenState = ScreenState.MAIN;

        if (screenStack.size() > 0) {
            currentScreenState = screenStack.peek();
        }

        screenStack.push(newState);

        // complete any actions in progress
        completeAllActions();

        switch(newState) {
            case FIGHT:
                //todo spells or power based on character being a mage or not
                topLeftButton.setText(BTN_NAME_SPELLS);
                topRightButton.setText(BTN_NAME_ATTACK);
                runButton.addAction(Actions.fadeOut(0));
                statusButton.addAction(Actions.fadeOut(0));

                backButton.setVisible(true);
                backButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                backButton.addAction(Actions.sizeBy(0, backButtonHeight, fadeTime));

                dummyButtonLeft.setVisible(true);
                dummyButtonLeft.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                dummyButtonLeft.addAction(Actions.sizeBy(0, -backButtonHeight, fadeTime));
                dummyButtonLeft.addAction(Actions.moveBy(0, backButtonHeight, fadeTime));
                dummyButtonLeft.addAction(Actions.sequence(Actions.delay(fadeTime), Actions.fadeOut(0)));

                dummyButtonRight.setVisible(true);
                dummyButtonRight.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                dummyButtonRight.addAction(Actions.sizeBy(0, -backButtonHeight, fadeTime));
                dummyButtonRight.addAction(Actions.moveBy(0, backButtonHeight, fadeTime));
                dummyButtonRight.addAction(Actions.sequence(Actions.delay(fadeTime), Actions.fadeOut(0)));

                break;
            case FINAL:
                if (currentScreenState == ScreenState.INVENTORY) {
                    monster1Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                    monster2Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                    monster3Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                    monster4Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                    monster5Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));

                    leftSummaryText.setText("");

                    middleTreeTextArea.addAction(Actions.sizeBy(0, -middleTreeHeight, fadeTime));
                    middleTreeTextArea.addAction(Actions.fadeOut(fadeTime));

                    middleScrollPaneTree.addAction(Actions.sizeBy(0, (middleTreeHeight - 4) * -1, fadeTime));

                    backButton.setVisible(true);
                    backButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                    backButton.addAction(Actions.sizeBy(0, backButtonHeight, fadeTime));

                    middleTextAreaTable.setVisible(false);

                    middleStatsTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                    middleStatsTextArea.addAction(Actions.sizeBy(0, -backButtonHeight + 3, fadeTime));
                    middleStatsTextArea.addAction(Actions.moveBy(0, backButtonHeight - 3, fadeTime));
                    middleStatsTextArea.addAction(Actions.sequence(Actions.delay(fadeTime), new setTextAreaText(middleStatsTextArea, CHOOSE_A_CHARACTER)));

                    // calculate banner width
                    int pixelLength = Utility.getPixelLengthOfString(selectedInventoryElement.name);
                    float bannerWidth = pixelLength * 2.25f;
                    float minWidth = 160;
                    if (bannerWidth  < minWidth)
                        bannerWidth = minWidth;

                    selectedItemBanner.setWidth(bannerWidth);
                    selectedItemBanner.setPosition((_stage.getWidth() - selectedItemBanner.getWidth())/2 , _stage.getHeight() + 8);
                    selectedItemBanner.addAction(Actions.sizeBy(0, selectedItemBannerHeight, fadeTime));
                    selectedItemBanner.addAction(Actions.moveBy(0, -selectedItemBannerHeight, fadeTime));
                    selectedItemBanner.setText(selectedInventoryElement.name, true);
                }
                else if (currentScreenState == ScreenState.SPELLS_BLACK ||
                         currentScreenState == ScreenState.SPELLS_WHITE ||
                         currentScreenState == ScreenState.POWER)
                {
                    monster1Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                    monster2Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                    monster3Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                    monster4Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                    monster5Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));

                    leftSummaryText.setText("");

                    middleTreeTextArea.addAction(Actions.sizeBy(0, -middleTreeHeight, fadeTime));
                    middleTreeTextArea.addAction(Actions.fadeOut(fadeTime));

                    middleScrollPaneList.addAction(Actions.sizeBy(0, (middleTreeHeight - 4) * -1, fadeTime));

                    backButton.setHeight(2);
                    backButton.setVisible(true);
                    backButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                    backButton.addAction(Actions.sizeBy(0, backButtonHeight - 3, fadeTime));

                    middleTextAreaTable.setVisible(false);

                    middleStatsTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                    middleStatsTextArea.addAction(Actions.sizeBy(0, -backButtonHeight + 3, fadeTime));
                    middleStatsTextArea.addAction(Actions.moveBy(0, backButtonHeight - 3, fadeTime));
                    middleStatsTextArea.addAction(Actions.sequence(Actions.delay(fadeTime), new setTextAreaText(middleStatsTextArea, CHOOSE_A_CHARACTER)));

                    // calculate banner width
                    int pixelLength = Utility.getPixelLengthOfString(selectedSpellsPowerElement.name);
                    float bannerWidth = pixelLength * 2.25f;
                    float minWidth = 160;
                    if (bannerWidth  < minWidth)
                        bannerWidth = minWidth;

                    selectedItemBanner.setWidth(bannerWidth);
                    selectedItemBanner.setPosition((_stage.getWidth() - selectedItemBanner.getWidth())/2 , _stage.getHeight() + 8);
                    selectedItemBanner.addAction(Actions.sizeBy(0, selectedItemBannerHeight, fadeTime));
                    selectedItemBanner.addAction(Actions.moveBy(0, -selectedItemBannerHeight, fadeTime));
                    selectedItemBanner.setText(selectedSpellsPowerElement.name, true);
                }
                break;
            case INVENTORY:
                monster1Name.addAction(Actions.fadeOut(fadeTime));
                monster2Name.addAction(Actions.fadeOut(fadeTime));
                monster3Name.addAction(Actions.fadeOut(fadeTime));
                monster4Name.addAction(Actions.fadeOut(fadeTime));
                monster5Name.addAction(Actions.fadeOut(fadeTime));

                topLeftButton.addAction(Actions.fadeOut(fadeTime/2));
                runButton.addAction(Actions.fadeOut(fadeTime/2));
                topRightButton.addAction(Actions.fadeOut(fadeTime/2));
                statusButton.addAction(Actions.fadeOut(fadeTime/2));

                middleStatsTextArea.setVisible(true);
                middleStatsTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                middleStatsTextArea.setText(SELECT_AN_ITEM, true);

                middleTreeTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                middleTreeTextArea.addAction(Actions.sizeBy(0, middleTreeHeight, fadeTime));
                middleTreeTextArea.setVisible(true);

                // reset tree selection
                middleTree.setVisible(true);
                middleTree.collapseAll();
                Selection<Tree.Node> selection = middleTree.getSelection();
                if (selection != null) {
                    selection.clear();
                }

                middleScrollPaneTree.addAction(Actions.sizeBy(0, middleTreeHeight - 4, fadeTime));

                break;
            case MAIN:
                // the MAIN screen should not be set as a new state, only as a previous state
                break;
            case SPELL_TYPE:
                if (currentScreenState == ScreenState.FIGHT) {
                    topLeftButton.setText(BTN_NAME_WHITE);
                    topRightButton.setText(BTN_NAME_BLACK);
                }
                break;
            case MAGIC:
                break;
            case MENU:
                break;
            case POWER:
            case SPELLS_BLACK:
            case SPELLS_WHITE:
                monster1Name.addAction(Actions.fadeOut(fadeTime));
                monster2Name.addAction(Actions.fadeOut(fadeTime));
                monster3Name.addAction(Actions.fadeOut(fadeTime));
                monster4Name.addAction(Actions.fadeOut(fadeTime));
                monster5Name.addAction(Actions.fadeOut(fadeTime));

                topLeftButton.addAction(Actions.fadeOut(fadeTime/2));
                runButton.addAction(Actions.fadeOut(fadeTime/2));
                topRightButton.addAction(Actions.fadeOut(fadeTime/2));
                statusButton.addAction(Actions.fadeOut(fadeTime/2));

                middleTree.setVisible(false);

                middleStatsTextArea.setVisible(true);
                if (newState == ScreenState.POWER) {
                    middleStatsTextArea.addAction(Actions.sequence(Actions.delay(fadeTime), new setTextAreaText(middleStatsTextArea, SELECT_A_POWER)));
                }
                else if (newState == ScreenState.SPELLS_BLACK || newState == ScreenState.SPELLS_WHITE) {
                    middleStatsTextArea.addAction(Actions.sequence(Actions.delay(fadeTime), new setTextAreaText(middleStatsTextArea, SELECT_A_SPELL)));
                }

                float startingHeight = topLeftButton.getHeight() + runButton.getHeight();
                middleStatsTextArea.setHeight(topLeftButton.getHeight());
                middleStatsTextArea.setPosition(topLeftButton.getX(), topLeftButton.getY());
                middleStatsTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                middleStatsTextArea.addAction(Actions.sizeBy(0, backButtonHeight - 2, fadeTime));
                middleStatsTextArea.addAction(Actions.moveBy(0, -backButtonHeight + 2, fadeTime));

                backButton.addAction(Actions.sequence(Actions.sizeBy(0, -backButtonHeight + 2, fadeTime), Actions.fadeOut(0)));
                backButton.addAction(Actions.sequence(Actions.delay(fadeTime * 0.8f), new setButtonText(backButton, "")));
                backButton.addAction(Actions.sequence(Actions.delay(fadeTime * 1.1f), new setButtonText(backButton, "Back")));

                middleTreeTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                middleTreeTextArea.addAction(Actions.sizeBy(0, middleTreeHeight, fadeTime));
                middleTreeTextArea.setVisible(true);

                // reset selection todo


                middleScrollPaneList.addAction(Actions.sizeBy(0, middleTreeHeight - 4, fadeTime));
                break;
            case STATS:
                break;
        }
    }

    // Semaphore with an access count of one is a Mutex (binary Semaphore)
    Semaphore previousScreenStateMutex = new Semaphore(1, true);

    private void setHUDPreviousState() {

        try {
            previousScreenStateMutex.acquire(1);

            if (screenStack.size() == 0) {
                Gdx.app.log(TAG, "Trying to set previous state on empty stack!!");
                screenStack.push(ScreenState.MAIN);
                screenStack.push(ScreenState.UNKNOWN);
            }

            ScreenState currentScreenState = screenStack.pop();
            ScreenState previousScreenState = screenStack.peek();
            battleScreen.setBattleControls(previousScreenState);

            Gdx.app.log(TAG, "setting previous HUD state " + previousScreenState.toString() + " from current state " + currentScreenState.toString());

            // complete any actions in progress
            completeAllActions();

            switch (previousScreenState) {
                case FIGHT:
                    if (currentScreenState == ScreenState.MAIN) {

                    } else if (currentScreenState == ScreenState.SPELL_TYPE) {
                        //todo spells or power
                        topLeftButton.setText(BTN_NAME_SPELLS);
                        topRightButton.setText(BTN_NAME_ATTACK);
                    }
                    break;
                case FINAL:
                    if (currentScreenState == ScreenState.INVENTORY) {
                        middleTreeTextArea.addAction(Actions.sizeBy(0, -middleTreeHeight, fadeTime));
                        middleTreeTextArea.addAction(Actions.fadeOut(fadeTime));

                        middleScrollPaneTree.addAction(Actions.sizeBy(0, (middleTreeHeight - 4) * -1, fadeTime));

                        backButton.setVisible(true);
                        backButton.addAction(Actions.sizeBy(0, backButtonHeight + 3, fadeTime));

                        middleStatsTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                        middleStatsTextArea.setText(CHOOSE_A_CHARACTER, true);
                        middleStatsTextArea.addAction(Actions.sizeBy(0, -backButtonHeight, fadeTime));
                        middleStatsTextArea.addAction(Actions.moveBy(0, backButtonHeight, fadeTime));
                    }

                    break;
                case INVENTORY:
                    if (currentScreenState == ScreenState.FINAL) {
                        monster1Name.addAction(Actions.fadeOut(fadeTime));
                        monster2Name.addAction(Actions.fadeOut(fadeTime));
                        monster3Name.addAction(Actions.fadeOut(fadeTime));
                        monster4Name.addAction(Actions.fadeOut(fadeTime));
                        monster5Name.addAction(Actions.fadeOut(fadeTime));

                        middleTextAreaTable.setVisible(true);
                        middleStatsTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                        middleStatsTextArea.setVisible(true);

                        middleTreeTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                        middleTreeTextArea.addAction(Actions.sizeBy(0, middleTreeHeight, fadeTime));
                        middleTreeTextArea.setVisible(true);

                        middleTree.setVisible(true);

                        leftSummaryText.setText(selectedInventoryElement.summary);

                        middleScrollPaneTree.addAction(Actions.sizeBy(0, middleTreeHeight - 4, fadeTime));

                        middleStatsTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                        middleStatsTextArea.setText("", true);
                        middleStatsTextArea.addAction(Actions.sizeBy(0, backButtonHeight - 3, fadeTime));
                        middleStatsTextArea.addAction(Actions.moveBy(0, -backButtonHeight + 3, fadeTime));

                        backButton.addAction(Actions.sequence(Actions.sizeBy(0, -backButtonHeight, fadeTime), Actions.fadeOut(0)));

                        selectedItemBanner.addAction(Actions.sizeBy(0, -selectedItemBannerHeight, fadeTime));
                        selectedItemBanner.addAction(Actions.moveBy(0, selectedItemBannerHeight, fadeTime));
                    }

                    break;
                case MAIN:
                    if (currentScreenState == ScreenState.INVENTORY) {

                        topLeftButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime / 2)));
                        runButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime / 2)));
                        topRightButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime / 2)));
                        statusButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime / 2)));

                        monster1Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        monster2Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        monster3Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        monster4Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        monster5Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));

                        middleTreeTextArea.addAction(Actions.sizeBy(0, -middleTreeHeight, fadeTime));
                        middleTreeTextArea.addAction(Actions.fadeOut(fadeTime));

                        //middleTree.setVisible(true);

                        middleScrollPaneTree.addAction(Actions.sizeBy(0, (middleTreeHeight - 4) * -1, fadeTime));

                        middleStatsTextArea.setText("", true);
                        middleStatsTextArea.addAction(Actions.fadeOut(fadeTime / 2));
                        middleTextAreaTable.setVisible(false);
                        leftSummaryText.setText("");

                        // reset root node array
                        for (rootNode r : rootNodes) {
                            r.isExpanded = false;
                        }
                    }
                    else if (currentScreenState == ScreenState.FIGHT) {
                        topLeftButton.setText(BTN_NAME_INVENTORY);
                        topRightButton.setText(BTN_NAME_FIGHT);
                        runButton.addAction(Actions.sequence(Actions.delay(fadeTime / 2), Actions.alpha(0), Actions.fadeIn(fadeTime / 4)));
                        statusButton.addAction(Actions.sequence(Actions.delay(fadeTime / 2), Actions.alpha(0), Actions.fadeIn(fadeTime / 4)));

                        backButton.addAction(Actions.sequence(Actions.delay(fadeTime / 2), Actions.fadeOut(fadeTime / 2)));
                        backButton.addAction(Actions.sizeBy(0, -backButtonHeight, fadeTime));

                        dummyButtonLeft.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                        dummyButtonLeft.addAction(Actions.sizeBy(0, backButtonHeight, fadeTime));
                        dummyButtonLeft.addAction(Actions.moveBy(0, -backButtonHeight, fadeTime));
                        dummyButtonLeft.addAction(Actions.sequence(Actions.delay(fadeTime / 2), Actions.fadeOut(fadeTime / 2), Actions.hide()));

                        dummyButtonRight.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                        dummyButtonRight.addAction(Actions.sizeBy(0, backButtonHeight, fadeTime));
                        dummyButtonRight.addAction(Actions.moveBy(0, -backButtonHeight, fadeTime));
                        dummyButtonRight.addAction(Actions.sequence(Actions.delay(fadeTime / 2), Actions.fadeOut(fadeTime / 2), Actions.hide()));
                    }
                    else if (currentScreenState == ScreenState.UNKNOWN) {
                        // need to recover to MAIN screen by reinitializing everything
                        topLeftButton.setText(BTN_NAME_INVENTORY);
                        topRightButton.setText(BTN_NAME_FIGHT);
                        topLeftButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime / 2)));
                        runButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime / 2)));
                        topRightButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime / 2)));
                        statusButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime / 2)));

                        backButton.setVisible(false);
                        backButton.setHeight(0);
                        backButton.setPosition(middleTreeTextArea.getX(), 2);

                        dummyButtonLeft.setVisible(false);
                        dummyButtonLeft.setHeight(backButtonHeight + 2);
                        dummyButtonLeft.setPosition(topLeftButton.getX(), 0);

                        dummyButtonRight.setVisible(false);
                        dummyButtonRight.setHeight(backButtonHeight + 2);
                        dummyButtonRight.setPosition(topLeftButton.getX() + dummyButtonLeft.getWidth() - 2, 0);

                        monster1Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        monster2Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        monster3Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        monster4Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        monster5Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));

                        middleTreeTextArea.setHeight(0);
                        middleTreeTextArea.addAction(Actions.fadeOut(fadeTime));

                        middleTree.setVisible(false);

                        middleScrollPaneTree.setHeight(0);
                        middleScrollPaneList.setHeight(0);

                        middleStatsTextArea.setText("", true);
                        middleStatsTextArea.addAction(Actions.fadeOut(fadeTime / 2));
                        middleTextAreaTable.setVisible(false);
                        leftSummaryText.setText("");
                    }
                    break;
                case MAGIC:
                    break;
                case MENU:
                    break;
                case POWER:
                case SPELLS_BLACK:
                case SPELLS_WHITE:
                    monster1Name.addAction(Actions.fadeOut(fadeTime));
                    monster2Name.addAction(Actions.fadeOut(fadeTime));
                    monster3Name.addAction(Actions.fadeOut(fadeTime));
                    monster4Name.addAction(Actions.fadeOut(fadeTime));
                    monster5Name.addAction(Actions.fadeOut(fadeTime));

                    middleTextAreaTable.setVisible(true);
                    middleStatsTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                    middleStatsTextArea.setVisible(true);

                    middleTreeTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                    middleTreeTextArea.addAction(Actions.sizeBy(0, middleTreeHeight, fadeTime));
                    middleTreeTextArea.setVisible(true);

                    middleTree.setVisible(true);

                    leftSummaryText.setText(selectedSpellsPowerElement.summary);

                    middleScrollPaneList.addAction(Actions.sizeBy(0, middleTreeHeight - 4, fadeTime));

                    middleStatsTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                    middleStatsTextArea.setText("", true);
                    middleStatsTextArea.addAction(Actions.sizeBy(0, backButtonHeight - 3, fadeTime));
                    middleStatsTextArea.addAction(Actions.moveBy(0, -backButtonHeight + 3, fadeTime));

                    backButton.addAction(Actions.sequence(Actions.sizeBy(0, -backButtonHeight + 3, fadeTime), Actions.fadeOut(0)));

                    selectedItemBanner.addAction(Actions.sizeBy(0, -selectedItemBannerHeight, fadeTime));
                    selectedItemBanner.addAction(Actions.moveBy(0, selectedItemBannerHeight, fadeTime));
                    break;
                case SPELL_TYPE:
                    backButton.setHeight(backButtonHeight);
                    topLeftButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime / 2)));
                    topRightButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime / 2)));
                    backButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime / 2)));

                    leftSummaryText.setText("");

                    monster1Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                    monster2Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                    monster3Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                    monster4Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                    monster5Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));

                    middleTreeTextArea.addAction(Actions.sizeBy(0, -middleTreeHeight, fadeTime));
                    middleTreeTextArea.addAction(Actions.fadeOut(fadeTime));

                    middleScrollPaneList.addAction(Actions.sizeBy(0, (middleTreeHeight - 4) * -1, fadeTime));

                    middleStatsTextArea.setText("", true);
                    middleStatsTextArea.addAction(Actions.fadeOut(fadeTime / 2));
                    middleTextAreaTable.setVisible(false);
                    leftSummaryText.setText("");
                    break;
                case STATS:
                    break;
            }
        }
        catch (Exception e) {
            Gdx.app.log(TAG, "Exception caught in setHUDPreviousState: " + e.toString());

            if (screenStack.size() == 0) {
                // MAIN screen needs to be recovered
                screenStack.push(ScreenState.MAIN);
            }
        }
        finally {
            previousScreenStateMutex.release(1);
        }
    }

    @Override
    public void onBattleControlsNotify(Object data, BattleControlEvent event) {
        Gdx.app.log(TAG, event.toString());

        ScreenState currentScreenState = ScreenState.MAIN;

        if (screenStack.size() > 0) {
            currentScreenState = screenStack.peek();
        }

        switch (event) {
            case A_BUTTON_RELEASED:
                switch(currentScreenState) {
                    case INVENTORY:
                    case POWER:
                    case SPELLS_BLACK:
                    case SPELLS_WHITE:
                        if (!middleStatsTextArea.getText().equals(SELECT_AN_ITEM) &&
                                !middleStatsTextArea.getText().equals(SELECT_A_POWER) &&
                                !middleStatsTextArea.getText().equals(SELECT_A_SPELL)) {
                            setHUDNewState(ScreenState.FINAL);
                        }
                        break;
                }
                break;
            case B_BUTTON_RELEASED:
                switch(currentScreenState) {
                    case INVENTORY:
                    case POWER:
                    case SPELLS_BLACK:
                    case SPELLS_WHITE:
                        setHUDPreviousState();
                        break;
                }
                break;
            case D_PAD_UP_PRESSED:
                break;
            case D_PAD_UP_RELEASED:
                break;
            case D_PAD_DOWN_PRESSED:
                break;
            case D_PAD_DOWN_RELEASED:
                break;
        }
    }

    private void showMainMenu() {
        topLeftButton.setVisible(true);
        topRightButton.setVisible(true);
        runButton.setVisible(true);
        statusButton.setVisible(true);

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

    public Stage getStage() {
        return _stage;
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

    private boolean isSignPostInteraction(Entity.Interaction interaction) {
        String signName = interaction.toString();
        return (signName.contains("SIGN"));
    }

    @Override
    public void onNotify(ProfileManager profileManager, ProfileEvent event) {
        //Gdx.app.log(TAG, "onNotify event = " + event.toString());
        switch(event){
            case PROFILE_LOADED:
                boolean firstTime = profileManager.getIsNewProfile();

                if( firstTime ){
                    InventoryUI.clearInventoryItems(_inventoryUI.getInventorySlotTable());
                    InventoryUI.clearInventoryItems(_inventoryUI.getEquipSlotTable());
                    _inventoryUI.resetEquipSlots();

                    _questUI.setQuests(new Array<QuestGraph>());

                    //add default items if first timenumVisibleChoices
                    Array<ItemTypeID> items = _player.getEntityConfig().getInventory();
                    Array<InventoryItemLocation> itemLocations = new Array<InventoryItemLocation>();
                    for( int i = 0; i < items.size; i++){
                        itemLocations.add(new InventoryItemLocation(i, items.get(i).toString(), 1, InventoryUI.PLAYER_INVENTORY));
                    }
                    InventoryUI.populateInventory(_inventoryUI.getInventorySlotTable(), itemLocations, _inventoryUI.getDragAndDrop(), InventoryUI.PLAYER_INVENTORY, false);
                    profileManager.setProperty("playerInventory", InventoryUI.getInventory(_inventoryUI.getInventorySlotTable()));

                    //start the player with some money
                    _statusUI.setGoldValue(20);
                    _statusUI.setStatusForLevel(1);

                }else{
                    int goldVal = profileManager.getProperty("currentPlayerGP", Integer.class);

                    Array<InventoryItemLocation> inventory = profileManager.getProperty("playerInventory", Array.class);
                    InventoryUI.populateInventory(_inventoryUI.getInventorySlotTable(), inventory, _inventoryUI.getDragAndDrop(), InventoryUI.PLAYER_INVENTORY, false);

                    Array<InventoryItemLocation> equipInventory = profileManager.getProperty("playerEquipInventory", Array.class);
                    if( equipInventory != null && equipInventory.size > 0 ){
                        _inventoryUI.resetEquipSlots();
                        InventoryUI.populateInventory(_inventoryUI.getEquipSlotTable(), equipInventory, _inventoryUI.getDragAndDrop(), InventoryUI.PLAYER_INVENTORY, false);
                    }

                    Array<QuestGraph> quests = profileManager.getProperty("playerQuests", Array.class);
                    _questUI.setQuests(quests);

                    int xpMaxVal = profileManager.getProperty("currentPlayerXPMax", Integer.class);
                    int xpVal = profileManager.getProperty("currentPlayerXP", Integer.class);

                    int hpMaxVal = profileManager.getProperty("currentPlayerHPMax", Integer.class);
                    int hpVal = profileManager.getProperty("currentPlayerHP", Integer.class);

                    int mpMaxVal = profileManager.getProperty("currentPlayerMPMax", Integer.class);
                    int mpVal = profileManager.getProperty("currentPlayerMP", Integer.class);

                    int levelVal = profileManager.getProperty("currentPlayerLevel", Integer.class);

                    //set the current max values first
                    _statusUI.setXPValueMax(xpMaxVal);
                    _statusUI.setHPValueMax(hpMaxVal);
                    _statusUI.setMPValueMax(mpMaxVal);

                    _statusUI.setXPValue(xpVal);
                    _statusUI.setHPValue(hpVal);
                    _statusUI.setMPValue(mpVal);

                    //then add in current values
                    _statusUI.setGoldValue(goldVal);
                    _statusUI.setLevelValue(levelVal);

                    float totalTime = profileManager.getProperty("currentTime", Float.class);
                }

                break;
            case SAVING_PROFILE:
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
                break;
            case CLEAR_CURRENT_PROFILE:
                // set default profile
                profileManager.setProperty("playerQuests", new Array<QuestGraph>());
                profileManager.setProperty("playerInventory", new Array<InventoryItemLocation>());
                profileManager.setProperty("playerEquipInventory", new Array<InventoryItemLocation>());
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

                        //Check to see if there is a version loading into properties
                        if (config.getItemTypeID().equalsIgnoreCase(InventoryItem.ItemTypeID.NONE.toString())) {
                            EntityConfig configReturnProperty = ProfileManager.getInstance().getProperty(config.getEntityID(), EntityConfig.class);
                            if (configReturnProperty != null) {
                                config = configReturnProperty;
                            }
                        }

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
                    conversationLabel.setVisible(false);
                    conversationPopUp.hide();
                    conversationPopUp.getCurrentConversationGraph().removeObserver(this);
                }
                break;
            case DID_INITIAL_INTERACTION:
                Entity.Interaction interaction = _json.fromJson(Entity.Interaction.class, value);
                if (isSignPostInteraction(interaction))
                    signPopUp.setTextForInteraction(interaction);
                break;
            case DID_INTERACTION:
                signPopUp.interact();
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
                String enemyZoneID = value;
                _battleUI.battleZoneTriggered(Integer.parseInt(enemyZoneID));
                break;
            case PLAYER_HAS_MOVED:
                if( _battleUI.isBattleReady() ){
                    addTransitionToScreen();
                    MainGameScreen.setGameState(MainGameScreen.GameState.SAVING);
                    _mapMgr.disableCurrentmapMusic();
                    notify(AudioObserver.AudioCommand.MUSIC_PLAY_LOOP, AudioObserver.AudioTypeEvent.MUSIC_BATTLE);
                    _battleUI.toBack();
                    _battleUI.setVisible(true);
                }
                break;
            default:
                break;
        }
    }

    public void doConversation(String nextConversationId, float delay) {
        isDelayedPopUp = true;
        conversationPopUpDelay = delay / 1000;
        this.nextConversationId = nextConversationId;
    }

    public void doConversation() {
        conversationLabel.setVisible(true);

        // this is where all the magic happens
        if (nextConversationId != null) {
            if (conversationPopUp.populateConversationDialogById(nextConversationId) == true) {
                // todo: is this still necessary if not a cut scene
                if (!isCutScene)
                    conversationPopUp.interact(false);
            }
            else {
                conversationPopUp.hide();
                conversationLabel.setVisible(false);
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
                conversationLabel.setVisible(false);
                isThereAnActiveHiddenChoice = false;
            }
        }
        else if (choicePopUp1.getChoice() != null) {
            if (choicePopUp1.getChoice().getConversationCommandEvent() != null) {
                if (choicePopUp1.getChoice().getConversationCommandEvent().equals(ConversationCommandEvent.EXIT_CONVERSATION)) {
                    conversationPopUp.hide();
                    conversationLabel.setVisible(false);
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
            case LOAD_STORE_INVENTORY:
                Entity selectedEntity = _mapMgr.getCurrentSelectedMapEntity();
                if( selectedEntity == null ){
                    break;
                }

                Array<InventoryItemLocation> inventory =  InventoryUI.getInventory(_inventoryUI.getInventorySlotTable());
                _storeInventoryUI.loadPlayerInventory(inventory);

                Array<InventoryItem.ItemTypeID> items  = selectedEntity.getEntityConfig().getInventory();
                Array<InventoryItemLocation> itemLocations = new Array<InventoryItemLocation>();
                for( int i = 0; i < items.size; i++){
                    itemLocations.add(new InventoryItemLocation(i, items.get(i).toString(), 1, InventoryUI.STORE_INVENTORY));
                }

                _storeInventoryUI.loadStoreInventory(itemLocations);

                _storeInventoryUI.toFront();
                _storeInventoryUI.setVisible(true);
                break;
            case EXIT_CONVERSATION:
                //_conversationUI.setVisible(false);
                nextConversationId = null;
                isCurrentConversationDone = true;
                conversationPopUp.endConversation();
                conversationLabel.setVisible(false);
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
                    _statusUI.addXPValue(quest.getXpReward());
                    _statusUI.addGoldValue(quest.getGoldReward());
                    notify(AudioObserver.AudioCommand.SOUND_PLAY_ONCE, AudioObserver.AudioTypeEvent.SOUND_COIN_RUSTLE);
                    _inventoryUI.removeQuestItemFromInventory(questID);
                    configReturnProperty.setConversationConfigPath(QuestUI.FINISHED_QUEST);
                    ProfileManager.getInstance().setProperty(configReturnProperty.getEntityID(), configReturnProperty);
                }

                _mapMgr.clearCurrentSelectedMapEntity();

                break;
            case ADD_ENTITY_TO_INVENTORY:
                Entity entity = _mapMgr.getCurrentSelectedMapEntity();
                if( entity == null ){
                    break;
                }

                if( _inventoryUI.doesInventoryHaveSpace() ){
                    _inventoryUI.addEntityToInventory(entity, entity.getEntityConfig().getCurrentQuestID());
                    _mapMgr.clearCurrentSelectedMapEntity();
                    entity.unregisterObservers();
                    _mapMgr.removeMapQuestEntity(entity);
                    _questUI.updateQuests(_mapMgr);
                }else{
                    _mapMgr.clearCurrentSelectedMapEntity();
                    _messageBoxUI.setVisible(true);
                }

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
                Gdx.app.log(TAG, String.format("-------nNEXT_CONVERSATION_ID = %s", nextConversationId));
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
                conversationLabel.setText(value);
                break;
        }
    }

    @Override
    public void onNotify(String value, StoreInventoryEvent event) {
        switch (event) {
            case PLAYER_GP_TOTAL_UPDATED:
                int val = Integer.valueOf(value);
                _statusUI.setGoldValue(val);
                notify(AudioObserver.AudioCommand.SOUND_PLAY_ONCE, AudioObserver.AudioTypeEvent.SOUND_COIN_RUSTLE);
                break;
            case PLAYER_INVENTORY_UPDATED:
                Array<InventoryItemLocation> items = _json.fromJson(Array.class, value);
                InventoryUI.populateInventory(_inventoryUI.getInventorySlotTable(), items, _inventoryUI.getDragAndDrop(), InventoryUI.PLAYER_INVENTORY, false);
                break;
            default:
                break;
        }
    }

    @Override
    public void onNotify(int value, StatusEvent event) {
        switch(event) {
            case UPDATED_GP:
                _storeInventoryUI.setPlayerGP(value);
                ProfileManager.getInstance().setProperty("currentPlayerGP", _statusUI.getGoldValue());
                break;
            case UPDATED_HP:
                ProfileManager.getInstance().setProperty("currentPlayerHP", _statusUI.getHPValue());
                break;
            case UPDATED_LEVEL:
                ProfileManager.getInstance().setProperty("currentPlayerLevel", _statusUI.getLevelValue());
                break;
            case UPDATED_MP:
                ProfileManager.getInstance().setProperty("currentPlayerMP", _statusUI.getMPValue());
                break;
            case UPDATED_XP:
                ProfileManager.getInstance().setProperty("currentPlayerXP", _statusUI.getXPValue());
                break;
            case LEVELED_UP:
                notify(AudioObserver.AudioCommand.MUSIC_PLAY_ONCE, AudioObserver.AudioTypeEvent.MUSIC_LEVEL_UP_FANFARE);
                break;
            default:
                break;
        }
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

        _stage.act(delta);
        _stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        _stage.getViewport().update(width, height, true);
        _battleUI.validate();
        _battleUI.resize();
    }

    @Override
    public void pause() {
        _battleUI.resetDefaults();
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
    }

    @Override
    public void onNotify(Entity enemyEntity, BattleEvent event) {
        switch (event) {
            case OPPONENT_HIT_DAMAGE:
                notify(AudioObserver.AudioCommand.SOUND_PLAY_ONCE, AudioObserver.AudioTypeEvent.SOUND_CREATURE_PAIN);
                break;
            case OPPONENT_DEFEATED:
                MainGameScreen.setGameState(MainGameScreen.GameState.RUNNING);
                int goldReward = Integer.parseInt(enemyEntity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.ENTITY_GP_REWARD.toString()));
                _statusUI.addGoldValue(goldReward);
                int xpReward = Integer.parseInt(enemyEntity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.ENTITY_XP_REWARD.toString()));
                _statusUI.addXPValue(xpReward);
                notify(AudioObserver.AudioCommand.MUSIC_STOP, AudioObserver.AudioTypeEvent.MUSIC_BATTLE);
                _mapMgr.enableCurrentmapMusic();
                addTransitionToScreen();
                _battleUI.setVisible(false);
                break;
            case PLAYER_RUNNING:
                MainGameScreen.setGameState(MainGameScreen.GameState.RUNNING);
                notify(AudioObserver.AudioCommand.MUSIC_STOP, AudioObserver.AudioTypeEvent.MUSIC_BATTLE);
                _mapMgr.enableCurrentmapMusic();
                addTransitionToScreen();
                _battleUI.setVisible(false);
                break;
            case PLAYER_HIT_DAMAGE:
                notify(AudioObserver.AudioCommand.SOUND_PLAY_ONCE, AudioObserver.AudioTypeEvent.SOUND_PLAYER_PAIN);
                int hpVal = ProfileManager.getInstance().getProperty("currentPlayerHP", Integer.class);
                _statusUI.setHPValue(hpVal);
                _shakeCam.startShaking();

                if( hpVal <= 0 ){
                    _shakeCam.reset();
                    notify(AudioObserver.AudioCommand.MUSIC_STOP, AudioObserver.AudioTypeEvent.MUSIC_BATTLE);
                    addTransitionToScreen();
                    _battleUI.setVisible(false);
                    MainGameScreen.setGameState(MainGameScreen.GameState.GAME_OVER);
                }
                break;
            case PLAYER_USED_MAGIC:
                notify(AudioObserver.AudioCommand.SOUND_PLAY_ONCE, AudioObserver.AudioTypeEvent.SOUND_PLAYER_WAND_ATTACK);
                int mpVal = ProfileManager.getInstance().getProperty("currentPlayerMP", Integer.class);
                _statusUI.setMPValue(mpVal);
                break;
            default:
                break;
        }
    }

    @Override
    public void onNotify(String value, InventoryEvent event) {
        switch(event){
            case ITEM_CONSUMED:
                String[] strings = value.split(Component.MESSAGE_TOKEN);
                if( strings.length != 2) return;

                int type = Integer.parseInt(strings[0]);
                int typeValue = Integer.parseInt(strings[1]);

                if( InventoryItem.doesRestoreHP(type) ){
                    notify(AudioObserver.AudioCommand.SOUND_PLAY_ONCE, AudioObserver.AudioTypeEvent.SOUND_EATING);
                    _statusUI.addHPValue(typeValue);
                }else if( InventoryItem.doesRestoreMP(type) ){
                    notify(AudioObserver.AudioCommand.SOUND_PLAY_ONCE, AudioObserver.AudioTypeEvent.SOUND_DRINKING);
                    _statusUI.addMPValue(typeValue);
                }
                break;
            default:
                break;
        }
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

    public void setCutScene(boolean cutScene) {
        isCutScene = cutScene;
    }
}
