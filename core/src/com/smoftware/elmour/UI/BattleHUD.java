package com.smoftware.elmour.UI;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Selection;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.smoftware.elmour.ElmourGame;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.EntityConfig;
import com.smoftware.elmour.InventoryElement;
import com.smoftware.elmour.SpellsPowerElement;
import com.smoftware.elmour.Utility;
import com.smoftware.elmour.audio.AudioManager;
import com.smoftware.elmour.audio.AudioObserver;
import com.smoftware.elmour.audio.AudioSubject;
import com.smoftware.elmour.battle.BattleObserver;
import com.smoftware.elmour.maps.MapManager;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.profile.ProfileObserver;
import com.smoftware.elmour.screens.BattleScreen;
import com.smoftware.elmour.sfx.ScreenTransitionAction;
import com.smoftware.elmour.sfx.ScreenTransitionActor;
import com.smoftware.elmour.sfx.ShakeCamera;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.Semaphore;

public class BattleHUD implements Screen, AudioSubject, ProfileObserver, BattleControlsObserver, StatusObserver, BattleObserver {
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
    private final String CHOOSE_AN_ENEMY = "Choose an enemy";

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
    private final String BTN_NAME_OK = "OK";

    private ElmourGame game;
    private Stage _stage;
    private Viewport _viewport;
    private Camera _camera;
    private Entity _player;

    final private float fadeTime = 0.35f;
    final private float crossFadeInFactor = 0.75f;
    final private float crossFadeOutFactor = 1.5f;

    private TextButton topLeftButton;
    private TextButton topRightButton;
    private TextButton runButton;
    private TextButton statusButton;
    private TextButton dummyButtonLeft;
    private TextButton dummyButtonRight;

    private MyTextField selectedItemBanner;
    private float selectedItemBannerHeight;
    private float minBannerWidth;

    private Table leftNameTable;
    private MyTextArea leftTextArea;
    private int numberOfOpponents = 0;
    private int numberOfPartyMembers = 0;
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

    private WidgetGroup groupHp1;
    private WidgetGroup groupMp1;
    private WidgetGroup groupHp2;
    private WidgetGroup groupMp2;
    private WidgetGroup groupHp3;
    private WidgetGroup groupMp3;
    private WidgetGroup groupHp4;
    private WidgetGroup groupMp4;
    private WidgetGroup groupHp5;
    private WidgetGroup groupMp5;

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

    float menuItemHeight = 0;

    private Json _json;
    private MapManager _mapMgr;

    private Array<AudioObserver> _observers;
    private ScreenTransitionActor _transitionActor;

    private ShakeCamera _battleShakeCam = null;
    private Vector2 _currentImagePosition;

    BattleScreen battleScreen;
    BattleTextArea battleTextArea;
    private Timer.Task transitionToMainScreen;

    String selectedCharacter = null;

    private static final String INVENTORY_FULL = "Your inventory is full!";

    public BattleHUD(final ElmourGame game, final Camera camera, Entity player, MapManager mapMgr, BattleScreen screen) {
        _camera = camera;
        _player = player;
        _mapMgr = mapMgr;
        battleScreen = screen;
        this.game = game;

        game.battleState.addObserver(this);

        _viewport = new FitViewport(ElmourGame.V_WIDTH, ElmourGame.V_HEIGHT, camera);
        _stage = new Stage(_viewport);
        //_stage.setDebugAll(true);

        screenStack = new Stack();
        screenStack.push(ScreenState.MAIN);

        _observers = new Array<AudioObserver>();
        _transitionActor = new ScreenTransitionActor();

        BattleControlsSubject.addObserver(this);

        _battleShakeCam = new ShakeCamera(0,0, 30.0f);
        _currentImagePosition = new Vector2(0,0);

        transitionToMainScreen = getTransitionToMainScreenTimer();

        _json = new Json();

        topLeftButton = new TextButton(BTN_NAME_INVENTORY, Utility.ELMOUR_UI_SKIN, "battle");
        topRightButton = new TextButton(BTN_NAME_FIGHT, Utility.ELMOUR_UI_SKIN, "battle");
        runButton = new TextButton(BTN_NAME_RUN, Utility.ELMOUR_UI_SKIN, "battle");
        statusButton = new TextButton(BTN_NAME_STATUS, Utility.ELMOUR_UI_SKIN, "battle");
        backButton = new TextButton("", Utility.ELMOUR_UI_SKIN, "battle");  // button text is set when resizing the button
        dummyButtonLeft = new TextButton("", Utility.ELMOUR_UI_SKIN, "battle");
        dummyButtonRight = new TextButton("", Utility.ELMOUR_UI_SKIN, "battle");

        // Desktop
        float menuItemWidth = 115;
        menuItemHeight = 75;
        float menuItemX = _stage.getWidth()/4.75f;
        float menuItemY = menuItemHeight;
        float leftTextAreaWidth = menuItemX;
        float selectedItemBannerWidth = 300;

        // Android
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            menuItemWidth = 95;
            menuItemHeight = 57;
            menuItemX = _stage.getWidth()/4.75f;
            menuItemY = menuItemHeight;

        }

        minBannerWidth = 100;
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
        topLeftButton.setPosition(menuItemX, menuItemY - menuItemHeight);
        topLeftButton.setVisible(true);

        menuItemX += menuItemWidth - 2;
        topRightButton.setWidth(menuItemWidth);
        topRightButton.setHeight(menuItemHeight);
        topRightButton.setPosition(menuItemX, menuItemY - menuItemHeight);
        topRightButton.setVisible(true);

        runButton.setWidth(menuItemWidth);
        runButton.setHeight(menuItemHeight);
        runButton.setPosition(topLeftButton.getX(), 2 - menuItemHeight);
        runButton.setVisible(true);

        statusButton.setWidth(menuItemWidth);
        statusButton.setHeight(menuItemHeight);
        statusButton.setPosition(menuItemX, 2 - menuItemHeight);
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
        leftTextArea.setPosition(2, 2 - menuItemHeight);
        leftTextArea.setVisible(true);

        monster1Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        monster1Name.setText("");

        monster2Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        monster2Name.setText("");

        monster3Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        monster3Name.setText("");

        monster4Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        monster4Name.setText("");

        monster5Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        monster5Name.setText("");

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
        leftNameTable.setY(4 - menuItemHeight);


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

        battleTextArea = new BattleTextArea();
        battleTextArea.setWidth(middleAreaWidth);
        battleTextArea.setHeight((menuItemHeight * 2) - 2);
        battleTextArea.setPosition(_stage.getWidth() - rightTextAreaWidth - middleAreaWidth, 2);

        middleTreeHeight = menuItemHeight * 2;

        middleTreeTextArea = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        middleTreeTextArea.disabled = true;
        middleTreeTextArea.setWidth(middleAreaWidth);
        middleTreeTextArea.setHeight(0);
        middleTreeTextArea.setPosition(_stage.getWidth() - rightTextAreaWidth - middleAreaWidth, menuItemHeight * 2 - 2);
        middleTreeTextArea.setVisible(false);

        middleTree = new Tree(Utility.ELMOUR_UI_SKIN) {
            @Override
            public void setStyle(TreeStyle style) {
                super.setStyle(style);

                // After having called the base class's setStyle,
                // use reflection to find and alter the indentSpacing field.
                try {
                    Field field = Tree.class.getDeclaredField("indentSpacing");
                    field.setAccessible(true);
                    field.set(this, 8); // This is how much you want each plus and minus indented.
                    System.out.println(field);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        };

        middleTree.setIconSpacing(4, 4);

        // load tree from inventory.json, for battle only show Potion, Food, and Consumables
        Tree.Node Potions = new Tree.Node(new TextButton("Potions", Utility.ELMOUR_UI_SKIN, "tree_node"));
        Tree.Node Food = new Tree.Node(new TextButton("Food", Utility.ELMOUR_UI_SKIN, "tree_node"));
        Tree.Node Consumables = new Tree.Node(new TextButton("Consumables", Utility.ELMOUR_UI_SKIN, "tree_node"));
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

            Tree.Node node = new Tree.Node(new TextButton(element.name, Utility.ELMOUR_UI_SKIN, "tree_node"));
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
        middleScrollPaneList.setTouchable(Touchable.disabled);
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
        rightTextArea.setPosition(statusButton.getX() + statusButton.getWidth() - 2, 2 - menuItemHeight);
        rightTextArea.setVisible(true);

        party1Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        party1Name.setText("");

        party2Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        party2Name.setText("");

        party3Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        party3Name.setText("");

        party4Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        party4Name.setText("");

        party5Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        party5Name.setText("");

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
        groupHp1 = new WidgetGroup();
        groupMp1 = new WidgetGroup();
        groupHp2 = new WidgetGroup();
        groupMp2 = new WidgetGroup();
        groupHp3 = new WidgetGroup();
        groupMp3 = new WidgetGroup();
        groupHp4 = new WidgetGroup();
        groupMp4 = new WidgetGroup();
        groupHp5 = new WidgetGroup();
        groupMp5 = new WidgetGroup();

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

        hp1Stats.setText("");
        mp1Stats.setText("");
        hp2Stats.setText("");
        mp2Stats.setText("");
        hp3Stats.setText("");
        mp3Stats.setText("");
        hp4Stats.setText("");
        mp4Stats.setText("");
        hp5Stats.setText("");
        mp5Stats.setText("");

        //add to widget groups
        groupHp1.addActor(blackbar1hp);
        groupHp1.addActor(whitebar1hp);
        groupHp1.addActor(hpBar1);
        groupHp1.addActor(hp1Stats);
        groupHp1.setVisible(false);
        groupMp1.addActor(blackbar1mp);
        groupMp1.addActor(whitebar1mp);
        groupMp1.addActor(mpBar1);
        groupMp1.addActor(mp1Stats);
        groupMp1.setVisible(false);

        groupHp2.addActor(blackbar2hp);
        groupHp2.addActor(whitebar2hp);
        groupHp2.addActor(hpBar2);
        groupHp2.addActor(hp2Stats);
        groupHp2.setVisible(false);
        groupMp2.addActor(blackbar2mp);
        groupMp2.addActor(whitebar2mp);
        groupMp2.addActor(mpBar2);
        groupMp2.addActor(mp2Stats);
        groupMp2.setVisible(false);

        groupHp3.addActor(blackbar3hp);
        groupHp3.addActor(whitebar3hp);
        groupHp3.addActor(hpBar3);
        groupHp3.addActor(hp3Stats);
        groupHp3.setVisible(false);
        groupMp3.addActor(blackbar3mp);
        groupMp3.addActor(whitebar3mp);
        groupMp3.addActor(mpBar3);
        groupMp3.addActor(mp3Stats);
        groupMp3.setVisible(false);

        groupHp4.addActor(blackbar4hp);
        groupHp4.addActor(whitebar4hp);
        groupHp4.addActor(hpBar4);
        groupHp4.addActor(hp4Stats);
        groupHp4.setVisible(false);
        groupMp4.addActor(blackbar4mp);
        groupMp4.addActor(whitebar4mp);
        groupMp4.addActor(mpBar4);
        groupMp4.addActor(mp4Stats);
        groupMp4.setVisible(false);

        groupHp5.addActor(blackbar5hp);
        groupHp5.addActor(whitebar5hp);
        groupHp5.addActor(hpBar5);
        groupHp5.addActor(hp5Stats);
        groupHp5.setVisible(false);
        groupMp5.addActor(blackbar5mp);
        groupMp5.addActor(whitebar5mp);
        groupMp5.addActor(mpBar5);
        groupMp5.addActor(mp5Stats);
        groupMp5.setVisible(false);

        // Desktop
        nameWidth = 115;
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
        rightTable.setY(4 - menuItemHeight);

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
        _stage.addActor(battleTextArea);
        _stage.addActor(rightTextArea);
        _stage.addActor(rightTable);

        //if (ElmourGame.DEV_MODE)
        //  _stage.addActor(statusButton);

        _stage.addActor(_transitionActor);
        _transitionActor.setVisible(false);

        //Observers
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
                                              else if (topRightButton.getText().toString().equals(BTN_NAME_ATTACK) && currentScreenState == ScreenState.FIGHT) {
                                                  setHUDNewState(ScreenState.FINAL);
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

                                               game.battleState.playerRuns();
                                               // see note below why this isn't being used
                                               //switchScreen(game, game.getScreenType(ElmourGame.ScreenType.MainGame));
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

                                               if (currentScreenState == ScreenState.FINAL) {
                                                   if (statusButton.getText().toString().equals(BTN_NAME_OK)) {
                                                       disableButtons();

                                                       middleStatsTextArea.addAction(Actions.fadeOut(0));
                                                       middleStatsTextArea.setText("", true);

                                                       backButton.addAction(Actions.fadeOut(0));
                                                       backButton.setWidth(middleAreaWidth);

                                                       statusButton.addAction(Actions.fadeOut(0));
                                                       statusButton.setText(BTN_NAME_STATUS);

                                                       // look at second item in stack
                                                       currentScreenState = screenStack.pop();
                                                       ScreenState previousScreenState = screenStack.peek();
                                                       screenStack.push(currentScreenState);

                                                       selectedItemBanner.addAction(Actions.sizeBy(0, -selectedItemBannerHeight, fadeTime/2));
                                                       selectedItemBanner.addAction(Actions.moveBy(0, selectedItemBannerHeight, fadeTime/2));

                                                       battleTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                                                       battleTextArea.interact(); // first interact sets battleTextArea visible

                                                       switch (previousScreenState) {
                                                           case FIGHT:
                                                               game.battleState.playerMeleeAttack();
                                                               break;
                                                           case INVENTORY:
                                                               game.battleState.applyInventoryItemToCharacter(selectedInventoryElement);
                                                               break;
                                                           case SPELLS_BLACK:
                                                           case SPELLS_WHITE:
                                                           case POWER:
                                                               game.battleState.applySpellPowerToCharacter(selectedSpellsPowerElement);
                                                               break;
                                                       }
                                                   }
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

                                        // don't process touchUp if switched screens
                                        ScreenState currentScreenState = ScreenState.MAIN;

                                        if (screenStack.size() > 0) {
                                            currentScreenState = screenStack.peek();
                                        }

                                        if (currentScreenState != ScreenState.SPELLS_BLACK && currentScreenState != ScreenState.SPELLS_WHITE && currentScreenState != ScreenState.POWER) {
                                            return;
                                        }

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

                                       // don't process touchUp if switched screens
                                       ScreenState currentScreenState = ScreenState.MAIN;

                                       if (screenStack.size() > 0) {
                                           currentScreenState = screenStack.peek();
                                       }

                                       if (currentScreenState != ScreenState.INVENTORY) {
                                           return;
                                       }

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

        battleTextArea.addListener(new ClickListener() {
                                       @Override
                                       public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                           return true;
                                       }

                                       @Override
                                       public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                           if (battleTextArea.interact()) {
                                               showMainScreen(true);
                                           }
                                       }
                                   }
        );

        //Music/Sound loading
        notify(AudioObserver.AudioCommand.MUSIC_LOAD, AudioObserver.AudioTypeEvent.MUSIC_BATTLE);
        notify(AudioObserver.AudioCommand.MUSIC_LOAD, AudioObserver.AudioTypeEvent.MUSIC_LEVEL_UP_FANFARE);
        notify(AudioObserver.AudioCommand.SOUND_LOAD, AudioObserver.AudioTypeEvent.SOUND_COIN_RUSTLE);
        notify(AudioObserver.AudioCommand.SOUND_LOAD, AudioObserver.AudioTypeEvent.SOUND_CREATURE_PAIN);
        notify(AudioObserver.AudioCommand.SOUND_LOAD, AudioObserver.AudioTypeEvent.SOUND_PLAYER_ATTACK);
        notify(AudioObserver.AudioCommand.SOUND_LOAD, AudioObserver.AudioTypeEvent.SOUND_PLAYER_PAIN);
        notify(AudioObserver.AudioCommand.SOUND_LOAD, AudioObserver.AudioTypeEvent.SOUND_PLAYER_WAND_ATTACK);
        notify(AudioObserver.AudioCommand.SOUND_LOAD, AudioObserver.AudioTypeEvent.SOUND_EATING);
        notify(AudioObserver.AudioCommand.SOUND_LOAD, AudioObserver.AudioTypeEvent.SOUND_DRINKING);
    }

    // Actions
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

    public class setButtonHeight extends Action {
        TextButton btn = null;
        float height = 0;

        public setButtonHeight(TextButton btn, float height) {
            this.btn = btn;
            this.height = height;
        }

        @Override
        public boolean act (float delta) {
            btn.setHeight(height);
            return true; // An action returns true when it's completed
        }
    }

    public class setButtonWidth extends Action {
        TextButton btn = null;
        float width = 0;

        public setButtonWidth(TextButton btn, float width) {
            this.btn = btn;
            this.width = width;
        }

        @Override
        public boolean act (float delta) {
            btn.setWidth(width);
            return true; // An action returns true when it's completed
        }
    }

    public class setLabelText extends Action {
        Label label = null;
        String text = "";

        public setLabelText(Label label, String text) {
            this.label = label;
            this.text = text;
        }

        @Override
        public boolean act (float delta) {
            label.setText(text);
            return true; // An action returns true when it's completed
        }
    }

    public class setTextAreaVisible extends Action {
        MyTextField textArea = null;
        boolean visible = false;

        public setTextAreaVisible(MyTextField textArea, boolean visible) {
            this.textArea = textArea;
            this.visible = visible;
        }

        @Override
        public boolean act (float delta) {
            textArea.setVisible(visible);
            return true; // An action returns true when it's completed
        }
    }

    public class setTableVisible extends Action {
        Table table = null;
        boolean visible = false;

        public setTableVisible(Table table, boolean visible) {
            this.table = table;
            this.visible = visible;
        }

        @Override
        public boolean act (float delta) {
            table.setVisible(visible);
            return true; // An action returns true when it's completed
        }
    }

    public class enabledScrollPane extends Action {
        ScrollPane pane = null;
        boolean enable = false;

        public enabledScrollPane(ScrollPane pane, boolean enable) {
            this.pane = pane;
            this.enable = enable;
        }

        @Override
        public boolean act (float delta) {
            if (enable)
                pane.setTouchable(Touchable.enabled);
            else
                pane.setTouchable(Touchable.disabled);

            return true; // An action returns true when it's completed
        }
    }

    public class closeBattleTextAction extends Action {
        BattleTextArea battleTextArea = null;

        public closeBattleTextAction(BattleTextArea battleTextArea) {
            this.battleTextArea = battleTextArea;
        }

        @Override
        public boolean act (float delta) {
            battleTextArea.interact();
            return true; // An action returns true when it's completed
        }
    }

    public class setTextFieldWidthAndPosition extends Action {
        MyTextField textField = null;
        float width = 0;
        float positionX = 0;
        float positionY = 0;

        public setTextFieldWidthAndPosition(MyTextField textField, float width, float positionX) {
            this.textField = textField;
            this.width = width;
            this.positionX = positionX;
        }

        @Override
        public boolean act (float delta) {
            textField.setWidth(width);
            textField.setX(positionX);
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

        statusBar.setWidth(0);
        statusBar.setHeight(barHeight - 2);
        statusBar.setPosition(1, 1);

        stats.setAlignment(Align.center);
        stats.setWidth(barWidth - 2);
        stats.setPosition(1, -6);
    }

    private float calculateBannerWidth(String text) {
        int pixelLength = Utility.getPixelLengthOfString(text);
        float factor;

        if (Gdx.app.getType() == Application.ApplicationType.Android)
            factor = 2.25f;
        else
            factor = 2.5f;

        float bannerWidth = pixelLength * factor;
        if (bannerWidth  < minBannerWidth)
            bannerWidth = minBannerWidth;

        return bannerWidth;
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
            middleTree.act(delta);
            middleTreeTextArea.act(delta);
            middleScrollPaneTree.act(delta);
            middleScrollPaneList.act(delta);
            rightTextArea.act(delta);
            rightTable.act(delta);
        }
    }

    private void enableButtons() {
        topLeftButton.setDisabled(false);
        topRightButton.setDisabled(false);
        runButton.setDisabled(false);
        statusButton.setDisabled(false);
        backButton.setDisabled(false);

        topLeftButton.setTouchable(Touchable.enabled);
        topRightButton.setTouchable(Touchable.enabled);
        runButton.setTouchable(Touchable.enabled);
        statusButton.setTouchable(Touchable.enabled);
        backButton.setTouchable(Touchable.enabled);
    }

    private void disableButtons() {
        topLeftButton.setDisabled(true);
        topRightButton.setDisabled(true);
        runButton.setDisabled(true);
        statusButton.setDisabled(true);
        backButton.setDisabled(true);

        topLeftButton.setTouchable(Touchable.disabled);
        topRightButton.setTouchable(Touchable.disabled);
        runButton.setTouchable(Touchable.disabled);
        statusButton.setTouchable(Touchable.disabled);
        backButton.setTouchable(Touchable.disabled);
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
                runButton.addAction(Actions.fadeOut(fadeTime * 0.25f));
                statusButton.addAction(Actions.fadeOut(fadeTime * 0.25f));

                backButton.setHeight(2);
                backButton.setVisible(true);
                backButton.addAction(Actions.sequence(Actions.delay(fadeTime * 0.25f), new setButtonText(backButton, BTN_NAME_BACK)));
                backButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                backButton.addAction(Actions.sizeBy(0, backButtonHeight - 2, fadeTime));

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
                    monster1Name.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));
                    monster2Name.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));
                    monster3Name.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));
                    monster4Name.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));
                    monster5Name.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));

                    leftSummaryText.addAction(Actions.fadeOut(fadeTime/2));
                    leftSummaryText.addAction(Actions.sequence(Actions.delay(fadeTime/2), new setLabelText(leftSummaryText, "")));

                    middleTree.setTouchable(Touchable.disabled);

                    middleTreeTextArea.addAction(Actions.sizeBy(0, -middleTreeHeight, fadeTime));
                    middleTreeTextArea.addAction(Actions.sequence(Actions.delay(fadeTime * 0.8f), Actions.fadeOut(fadeTime * 0.2f)));

                    middleScrollPaneTree.addAction(Actions.sizeBy(0, (middleTreeHeight - 4) * -1, fadeTime));

                    backButton.setHeight(2);
                    backButton.setVisible(true);
                    backButton.addAction(Actions.sequence(Actions.delay(fadeTime * 0.25f), new setButtonText(backButton, BTN_NAME_BACK)));
                    backButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                    backButton.addAction(Actions.sizeBy(0, backButtonHeight - 2, fadeTime));

                    middleTextAreaTable.addAction(Actions.fadeOut(fadeTime/2));
                    middleTextAreaTable.addAction(Actions.sequence(Actions.delay(fadeTime/2), new setTableVisible(middleTextAreaTable, false)));

                    middleStatsTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                    middleStatsTextArea.addAction(Actions.sizeBy(0, -backButtonHeight + 2, fadeTime));
                    middleStatsTextArea.addAction(Actions.moveBy(0, backButtonHeight - 2, fadeTime));
                    middleStatsTextArea.addAction(Actions.sequence(Actions.delay(fadeTime), new setTextAreaText(middleStatsTextArea, CHOOSE_A_CHARACTER)));

                    selectedItemBanner.setWidth(calculateBannerWidth(selectedInventoryElement.name));
                    selectedItemBanner.setPosition((_stage.getWidth() - selectedItemBanner.getWidth())/2 , _stage.getHeight() + 8);
                    selectedItemBanner.addAction(Actions.sizeBy(0, selectedItemBannerHeight, fadeTime));
                    selectedItemBanner.addAction(Actions.moveBy(0, -selectedItemBannerHeight, fadeTime));
                    selectedItemBanner.setText(selectedInventoryElement.name, true);
                }
                else if (currentScreenState == ScreenState.FIGHT) {
                    middleStatsTextArea.addAction(Actions.sizeBy(0, -backButtonHeight + 2, 0));
                    middleStatsTextArea.addAction(Actions.moveBy(0, backButtonHeight - 2, 0));
                    middleStatsTextArea.setVisible(true);
                    middleStatsTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                    middleStatsTextArea.addAction(Actions.sequence(Actions.delay(fadeTime/4), new setTextAreaText(middleStatsTextArea, CHOOSE_AN_ENEMY)));

                    topLeftButton.addAction(Actions.fadeOut(0));
                    topRightButton.addAction(Actions.fadeOut(0));
                }
                else if (currentScreenState == ScreenState.SPELLS_BLACK ||
                         currentScreenState == ScreenState.SPELLS_WHITE ||
                         currentScreenState == ScreenState.POWER)
                {
                    monster1Name.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));
                    monster2Name.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));
                    monster3Name.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));
                    monster4Name.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));
                    monster5Name.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));

                    leftSummaryText.addAction(Actions.fadeOut(fadeTime/2));
                    leftSummaryText.addAction(Actions.sequence(Actions.delay(fadeTime/2), new setLabelText(leftSummaryText, "")));

                    middleTreeTextArea.addAction(Actions.sizeBy(0, -middleTreeHeight, fadeTime));
                    middleTreeTextArea.addAction(Actions.sequence(Actions.delay(fadeTime * 0.8f), Actions.fadeOut(fadeTime * 0.2f)));

                    middleScrollPaneList.setTouchable(Touchable.disabled);
                    middleScrollPaneList.addAction(Actions.sizeBy(0, (middleTreeHeight - 4) * -1, fadeTime));

                    backButton.setHeight(2);
                    backButton.setVisible(true);
                    backButton.addAction(Actions.sequence(Actions.delay(fadeTime * 0.25f), new setButtonText(backButton, BTN_NAME_BACK)));
                    backButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                    backButton.addAction(Actions.sizeBy(0, backButtonHeight - 2, fadeTime));

                    middleTextAreaTable.setVisible(false);

                    middleStatsTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                    middleStatsTextArea.addAction(Actions.sizeBy(0, -backButtonHeight + 2, fadeTime));
                    middleStatsTextArea.addAction(Actions.moveBy(0, backButtonHeight - 2, fadeTime));

                    if (currentScreenState == ScreenState.SPELLS_WHITE) {
                        middleStatsTextArea.addAction(Actions.sequence(Actions.delay(fadeTime), new setTextAreaText(middleStatsTextArea, CHOOSE_A_CHARACTER)));
                    }
                    else {
                        middleStatsTextArea.addAction(Actions.sequence(Actions.delay(fadeTime), new setTextAreaText(middleStatsTextArea, CHOOSE_AN_ENEMY)));
                    }

                    selectedItemBanner.setWidth(calculateBannerWidth(selectedSpellsPowerElement.name));
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

                topLeftButton.addAction(Actions.fadeOut(fadeTime * crossFadeOutFactor));
                runButton.addAction(Actions.fadeOut(fadeTime * crossFadeOutFactor));
                topRightButton.addAction(Actions.fadeOut(fadeTime * crossFadeOutFactor));
                statusButton.addAction(Actions.fadeOut(fadeTime * crossFadeOutFactor));

                middleTree.setTouchable(Touchable.enabled);

                middleStatsTextArea.setVisible(true);
                middleStatsTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime * crossFadeInFactor)));
                middleStatsTextArea.setText(SELECT_AN_ITEM, true);

                middleTreeTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime * 0.2f)));
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

                leftSummaryText.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));

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

                //middleTree.setVisible(false);

                middleScrollPaneList.addAction(Actions.sequence(Actions.delay(fadeTime), new enabledScrollPane(middleScrollPaneList, true)));

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

                middleTreeTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0.2f)));
                middleTreeTextArea.addAction(Actions.sizeBy(0, middleTreeHeight, fadeTime));
                middleTreeTextArea.setVisible(true);

                middleScrollPaneList.addAction(Actions.sizeBy(0, middleTreeHeight - 4, fadeTime));

                leftSummaryText.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));

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
                    else if (currentScreenState == ScreenState.FINAL) {
                        topLeftButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime/4)));
                        topRightButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime/4)));

                        middleStatsTextArea.setText("", true);
                        middleStatsTextArea.addAction(Actions.sizeBy(0, backButtonHeight - 2, 0));
                        middleStatsTextArea.addAction(Actions.moveBy(0, -backButtonHeight + 2, 0));
                        middleStatsTextArea.addAction(Actions.fadeOut(fadeTime/2));
                        middleStatsTextArea.addAction(Actions.sequence(Actions.delay(fadeTime/2), new setTextAreaVisible(middleStatsTextArea, false)));
                    }
                    break;
                case FINAL:
                    if (currentScreenState == ScreenState.INVENTORY) {
                        middleTreeTextArea.addAction(Actions.sizeBy(0, -middleTreeHeight, fadeTime));
                        middleTreeTextArea.addAction(Actions.sequence(Actions.delay(fadeTime * 0.8f), Actions.fadeOut(fadeTime * 0.2f)));

                        middleScrollPaneTree.addAction(Actions.sizeBy(0, (middleTreeHeight - 4) * -1, fadeTime));

                        backButton.setVisible(true);
                        backButton.addAction(Actions.sequence(Actions.delay(fadeTime * 0.25f), new setButtonText(backButton, BTN_NAME_BACK)));
                        backButton.addAction(Actions.sizeBy(0, backButtonHeight + 3, fadeTime));

                        middleStatsTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                        middleStatsTextArea.setText(CHOOSE_A_CHARACTER, true);
                        middleStatsTextArea.addAction(Actions.sizeBy(0, -backButtonHeight, fadeTime));
                        middleStatsTextArea.addAction(Actions.moveBy(0, backButtonHeight, fadeTime));
                    }

                    break;
                case INVENTORY:
                    if (currentScreenState == ScreenState.FINAL) {
                        middleTree.setTouchable(Touchable.enabled);
                        middleTree.setVisible(true);

                        middleTextAreaTable.addAction(Actions.fadeOut(0));
                        middleTextAreaTable.setVisible(true);
                        middleTextAreaTable.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));

                        middleScrollPaneTree.addAction(Actions.sizeBy(0, middleTreeHeight - 4, fadeTime));

                        setCommonTransitionBackFromFinal();
                    }

                    break;
                case MAIN:
                    if (currentScreenState == ScreenState.INVENTORY) {

                        topLeftButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime * crossFadeInFactor)));
                        runButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime * crossFadeInFactor)));
                        topRightButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime * crossFadeInFactor)));
                        statusButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime * crossFadeInFactor)));

                        monster1Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        monster2Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        monster3Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        monster4Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        monster5Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));

                        middleTree.setTouchable(Touchable.disabled);

                        middleTreeTextArea.addAction(Actions.sizeBy(0, -middleTreeHeight, fadeTime));
                        middleTreeTextArea.addAction(Actions.sequence(Actions.delay(fadeTime * 0.8f), Actions.fadeOut(fadeTime * 0.2f)));

                        middleScrollPaneTree.addAction(Actions.sizeBy(0, (middleTreeHeight - 4) * -1, fadeTime));

                        middleStatsTextArea.setText("", true);
                        middleStatsTextArea.addAction(Actions.fadeOut(fadeTime * crossFadeOutFactor));
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
                        backButton.addAction(Actions.sequence(Actions.delay(fadeTime * 0.8f), new setButtonText(backButton, "")));

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
                    middleTextAreaTable.setVisible(true);

                    middleScrollPaneList.addAction(Actions.sequence(Actions.delay(fadeTime), new enabledScrollPane(middleScrollPaneList, true)));
                    middleScrollPaneList.addAction(Actions.sizeBy(0, middleTreeHeight - 4, fadeTime));

                    setCommonTransitionBackFromFinal();

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
                    middleTreeTextArea.addAction(Actions.sequence(Actions.delay(fadeTime * 0.8f), Actions.fadeOut(fadeTime * 0.2f)));

                    middleScrollPaneList.setTouchable(Touchable.disabled);
                    middleScrollPaneList.addAction(Actions.sizeBy(0, (middleTreeHeight - 4) * -1, fadeTime));

                    middleStatsTextArea.setText("", true);
                    middleStatsTextArea.addAction(Actions.fadeOut(fadeTime / 2));
                    middleTextAreaTable.setVisible(false);
                    leftSummaryText.setText("");

                    backButton.setText(BTN_NAME_BACK);
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

    private void setCommonTransitionBackFromFinal() {
        monster1Name.addAction(Actions.fadeOut(fadeTime/2));
        monster2Name.addAction(Actions.fadeOut(fadeTime/2));
        monster3Name.addAction(Actions.fadeOut(fadeTime/2));
        monster4Name.addAction(Actions.fadeOut(fadeTime/2));
        monster5Name.addAction(Actions.fadeOut(fadeTime/2));

        middleStatsTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
        middleStatsTextArea.setText("", true);
        middleStatsTextArea.addAction(Actions.sizeBy(0, backButtonHeight - 2, fadeTime));
        middleStatsTextArea.addAction(Actions.moveBy(0, -backButtonHeight + 2, fadeTime));

        middleTreeTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0.2f)));
        middleTreeTextArea.addAction(Actions.sizeBy(0, middleTreeHeight, fadeTime));
        middleTreeTextArea.setVisible(true);

        middleStatsTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
        middleStatsTextArea.setVisible(true);

        leftSummaryText.addAction(Actions.fadeOut(0));
        leftSummaryText.setText(selectedInventoryElement.summary);
        leftSummaryText.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));

        backButton.addAction(Actions.sequence(Actions.sizeBy(0, -backButtonHeight + 2, fadeTime), Actions.fadeOut(0)));
        backButton.addAction(Actions.sequence(Actions.delay(fadeTime * 0.8f), new setButtonText(backButton, "")));

        selectedItemBanner.addAction(Actions.sizeBy(0, -selectedItemBannerHeight, fadeTime));
        selectedItemBanner.addAction(Actions.moveBy(0, selectedItemBannerHeight, fadeTime));

        if (statusButton.getText().toString().equals(BTN_NAME_OK)) {
            statusButton.addAction(Actions.sequence(Actions.sizeBy(0, -backButtonHeight + 2, fadeTime), Actions.fadeOut(0)));
            statusButton.addAction(Actions.sequence(Actions.delay(fadeTime * 0.8f),
                    new setButtonText(statusButton, ""),
                    new setButtonText(statusButton, BTN_NAME_STATUS),
                    Actions.delay(fadeTime * 0.8f),
                    Actions.sizeBy(0, backButtonHeight - 2, 0),
                    new setButtonWidth(backButton, middleAreaWidth)));
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

    public void addTransitionToScreen(){
        _transitionActor.setVisible(true);
        _stage.addAction(
                Actions.sequence(
                        Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 1), _transitionActor)));
    }

    @Override
    public void onNotify(ProfileManager profileManager, ProfileEvent event) {
        //Gdx.app.log(TAG, "onNotify event = " + event.toString());
        switch(event){
            case PROFILE_LOADED:
                boolean firstTime = profileManager.getIsNewProfile();

                if( firstTime ){

                }
                else {

                }

                break;
            case SAVING_PROFILE:
                break;
            case CLEAR_CURRENT_PROFILE:
                // set default profile
                break;
            default:
                break;
        }
    }


    @Override
    public void onNotify(int value, StatusEvent event) {
        switch(event) {
            /*
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
            */
            default:
                break;
        }
    }

    @Override
    public void show() {
        _battleShakeCam.reset();
    }

    @Override
    public void render(float delta) {
        /*
        if (_battleShakeCam.isCameraShaking()) {
            Vector2 shakeCoords = _battleShakeCam.getNewShakePosition();
            _camera.position.x = shakeCoords.x + _stage.getWidth() / 2;
            _camera.position.y = shakeCoords.y + _stage.getHeight() / 2;
        }
        */

        //_stage.setDebugAll(true);
        battleTextArea.update();
        _stage.act(delta);
        _stage.draw();
    }

    // NOTE: there is an issue using this to fade out. When coming back into this screen, the HUD is not displayed
    public void switchScreen(final ElmourGame game, final Screen newScreen){
        _stage.getRoot().getColor().a = 1;
        SequenceAction sequenceAction = new SequenceAction();
        sequenceAction.addAction(Actions.fadeOut(0.25f));
        sequenceAction.addAction(Actions.run(new Runnable() {
            @Override
            public void run() {
                game.setScreen(newScreen);
            }
        }));
        _stage.getRoot().addAction(sequenceAction);
    }

    public void resetControls() {

        float fadeTime = 0f;
        // reset controls so they rise from the correct location at bottom of the screen
        leftTextArea.addAction(Actions.moveBy(0, -menuItemHeight, fadeTime));
        leftNameTable.addAction(Actions.moveBy(0, -menuItemHeight, fadeTime));
        topLeftButton.addAction(Actions.moveBy(0, -menuItemHeight, fadeTime));
        topRightButton.addAction(Actions.moveBy(0, -menuItemHeight, fadeTime));
        runButton.addAction(Actions.moveBy(0, -menuItemHeight, fadeTime));
        statusButton.addAction(Actions.moveBy(0, -menuItemHeight, fadeTime));
        rightTextArea.addAction(Actions.moveBy(0, -menuItemHeight, fadeTime));
        rightTable.addAction(Actions.moveBy(0, -menuItemHeight, fadeTime));

        // reset other variables
        party1Name.setText("");
        party2Name.setText("");
        party3Name.setText("");
        party4Name.setText("");
        party5Name.setText("");

        monster1Name.setText("");
        monster2Name.setText("");
        monster3Name.setText("");
        monster4Name.setText("");
        monster5Name.setText("");

        for (int i = 1; i <= numberOfPartyMembers; i++) {
            battleScreen.removePartyMember(i);
        }

        for (int i = 1; i <= numberOfOpponents; i++) {
            battleScreen.removeOpponent(i);
        }

        numberOfPartyMembers = 0;
        numberOfOpponents = 0;

        groupHp1.setVisible(false);
        groupMp1.setVisible(false);
        groupHp2.setVisible(false);
        groupMp2.setVisible(false);
        groupHp3.setVisible(false);
        groupMp3.setVisible(false);
        groupHp4.setVisible(false);
        groupMp4.setVisible(false);
        groupHp5.setVisible(false);
        groupMp5.setVisible(false);
    }

    @Override
    public void resize(int width, int height) {
        _stage.getViewport().update(width, height, true);

        // make controls rise from the bottom of the screen when it is first displayed
        leftTextArea.addAction(Actions.moveBy(0, menuItemHeight, fadeTime));
        leftNameTable.addAction(Actions.moveBy(0, menuItemHeight, fadeTime));
        topLeftButton.addAction(Actions.moveBy(0, menuItemHeight, fadeTime));
        topRightButton.addAction(Actions.moveBy(0, menuItemHeight, fadeTime));
        runButton.addAction(Actions.moveBy(0, menuItemHeight, fadeTime));
        statusButton.addAction(Actions.moveBy(0, menuItemHeight, fadeTime));
        rightTextArea.addAction(Actions.moveBy(0, menuItemHeight, fadeTime));
        rightTable.addAction(Actions.moveBy(0, menuItemHeight, fadeTime));
    }

    @Override
    public void pause() {
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

    private void updateStatusBar(String power, String power_max, Image statusBar, Label stats) {
        stats.setText(power + "/" + power_max);
        statusBar.setWidth(Float.parseFloat(power)/Float.parseFloat(power_max) * barWidth);
    }

    @Override
    public void onNotify(Entity entity, BattleEvent event) {
        switch(event){
            case PLAYER_TURN_START:
                /*
                runButton.setDisabled(true);
                runButton.setTouchable(Touchable.disabled);
                attackButton.setDisabled(true);
                attackButton.setTouchable(Touchable.disabled);
                */
                break;
            case PARTY_MEMBBER_ADDED:
                Gdx.app.log(TAG, "Party member added: " + entity.getEntityConfig().getDisplayName());
                numberOfPartyMembers++;
                battleScreen.addPartyMember(entity, entity.getBattlePosition());

                switch (entity.getBattlePosition()) {
                    case 1:
                        String s = entity.getEntityConfig().getDisplayName();
                        party1Name.setText(entity.getEntityConfig().getDisplayName());
                        updateStatusBar(entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP.toString()),
                                entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP_MAX.toString()), hpBar1, hp1Stats);

                        updateStatusBar(entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MP.toString()),
                                entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MP_MAX.toString()), mpBar1, mp1Stats);

                        groupHp1.setVisible(true);
                        groupMp1.setVisible(true);
                        break;
                    case 2:
                        party2Name.setText(entity.getEntityConfig().getDisplayName());
                        updateStatusBar(entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP.toString()),
                                entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP_MAX.toString()), hpBar2, hp2Stats);

                        updateStatusBar(entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MP.toString()),
                                entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MP_MAX.toString()), mpBar2, mp2Stats);

                        groupHp2.setVisible(true);
                        groupMp2.setVisible(true);
                        break;
                    case 3:
                        party3Name.setText(entity.getEntityConfig().getDisplayName());
                        updateStatusBar(entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP.toString()),
                                entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP_MAX.toString()), hpBar3, hp3Stats);

                        updateStatusBar(entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MP.toString()),
                                entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MP_MAX.toString()), mpBar3, mp3Stats);

                        groupHp3.setVisible(true);
                        groupMp3.setVisible(true);
                        break;
                    case 4:
                        party4Name.setText(entity.getEntityConfig().getDisplayName());
                        updateStatusBar(entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP.toString()),
                                entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP_MAX.toString()), hpBar4, hp4Stats);

                        updateStatusBar(entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MP.toString()),
                                entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MP_MAX.toString()), mpBar4, mp4Stats);

                        groupHp4.setVisible(true);
                        groupMp4.setVisible(true);
                        break;
                    case 5:
                        party5Name.setText(entity.getEntityConfig().getDisplayName());
                        updateStatusBar(entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP.toString()),
                                entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP_MAX.toString()), hpBar5, hp5Stats);

                        updateStatusBar(entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MP.toString()),
                                entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MP_MAX.toString()), mpBar5, mp5Stats);

                        groupHp5.setVisible(true);
                        groupMp5.setVisible(true);
                        break;
                }

                break;
            case OPPONENT_ADDED:
                Gdx.app.log(TAG, "Opponent added: " + entity.getEntityConfig().getDisplayName());
                numberOfOpponents++;
                battleScreen.addOpponent(entity, numberOfOpponents);

                switch (numberOfOpponents) {
                    case 1:
                        monster1Name.setText(entity.getEntityConfig().getDisplayName());
                        break;
                    case 2:
                        monster2Name.setText(entity.getEntityConfig().getDisplayName());
                        break;
                    case 3:
                        monster3Name.setText(entity.getEntityConfig().getDisplayName());
                        break;
                    case 4:
                        monster4Name.setText(entity.getEntityConfig().getDisplayName());
                        break;
                    case 5:
                        monster5Name.setText(entity.getEntityConfig().getDisplayName());
                        break;
                }

                /*
                _image.setEntity(enemyEntity);
                _image.setCurrentAnimation(Entity.AnimationType.IMMOBILE);
                _image.setSize(_enemyWidth, _enemyHeight);
                _image.setPosition(this.getCell(_image).getActorX(), this.getCell(_image).getActorY());
*/
                _currentImagePosition.set(15, 15);

                if( _battleShakeCam == null ){
                    _battleShakeCam = new ShakeCamera(_currentImagePosition.x, _currentImagePosition.y, 30.0f);
                }

                //Gdx.app.debug(TAG, "Image position: " + _image.getX() + "," + _image.getY() );

                //this.getTitleLabel().setText("Level " + _battleState.getCurrentZoneLevel() + " " + entity.getEntityConfig().getDisplayName());
                break;
            case CHARACTER_SELECTED:
                if (middleStatsTextArea.getText().equals(CHOOSE_A_CHARACTER) || middleStatsTextArea.getText().equals(CHOOSE_AN_ENEMY)) {
                    if (selectedCharacter == null) {
                        float widthToMove = statusButton.getWidth();

                        backButton.addAction(Actions.sizeBy(-widthToMove, 0, fadeTime));
                        backButton.setWidth(backButton.getWidth() + 2);

                        statusButton.setPosition(rightTextArea.getX() + 2, 2);
                        statusButton.setWidth(0);
                        statusButton.setText(BTN_NAME_OK);
                        statusButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                        statusButton.addAction(Actions.sizeBy(widthToMove, 0, fadeTime));
                        statusButton.addAction(Actions.moveBy(-widthToMove, 0, fadeTime));
                    }

                    selectedCharacter = entity.getEntityConfig().getDisplayName();
                    Gdx.app.log(TAG, selectedCharacter + " selected");
                }

                break;
            case OPPONENT_BLOCKED:
                battleTextArea.populateText("Melee attack on " + entity.getEntityConfig().getDisplayName() + " has been blocked!");
                if (battleTextArea.interact()) {
                    showMainScreen(true);
                }
                break;
            case OPPONENT_HIT_DAMAGE:
                Label.LabelStyle ls;

                notify(AudioObserver.AudioCommand.SOUND_PLAY_ONCE, AudioObserver.AudioTypeEvent.SOUND_PLAYER_ATTACK);
                notify(AudioObserver.AudioCommand.SOUND_PLAY_ONCE, AudioObserver.AudioTypeEvent.SOUND_CREATURE_PAIN);

                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/9_px.ttf"));
                FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
                parameter.size = Utility.myFontVerySmallSize;
                parameter.shadowColor = Color.LIGHT_GRAY;

                int HP = Integer.parseInt(entity.getEntityConfig().getPropertyValue(String.valueOf(EntityConfig.EntityProperties.HP)));
                int HP_MAX = Integer.parseInt(entity.getEntityConfig().getPropertyValue(String.valueOf(EntityConfig.EntityProperties.HP_MAX)));

                Gdx.app.log(TAG, "HP = " + HP + ", HP_MAX = " + HP_MAX);

                if (HP < 0.3f * (float)HP_MAX) {
                    Color red = new Color (1, 0, 0, 1);
                    parameter.color = red;
                }
                else if (HP < 0.6f * (float)HP_MAX) {
                    Color orange = new Color(1, 0.7f, 0, 1);
                    parameter.color = orange;
                }
                else {
                    parameter.color = Color.DARK_GRAY;
                }

                // set color of entity name
                BitmapFont font = generator.generateFont(parameter);
                ls = new Label.LabelStyle(font, parameter.color);

                if (monster1Name.getText().toString().equals(entity.getEntityConfig().getDisplayName()))
                    monster1Name.setStyle(ls);
                else if (monster2Name.getText().toString().equals(entity.getEntityConfig().getDisplayName()))
                    monster2Name.setStyle(ls);
                else if (monster3Name.getText().toString().equals(entity.getEntityConfig().getDisplayName()))
                    monster3Name.setStyle(ls);
                else if (monster4Name.getText().toString().equals(entity.getEntityConfig().getDisplayName()))
                    monster4Name.setStyle(ls);
                else if (monster5Name.getText().toString().equals(entity.getEntityConfig().getDisplayName()))
                    monster5Name.setStyle(ls);
                /*
                int damage = Integer.parseInt(entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HIT_DAMAGE_TOTAL.toString()));
                _damageValLabel.setText(String.valueOf(damage));
                _damageValLabel.setY(_origDamageValLabelY);
                _battleShakeCam.startShaking();
                _damageValLabel.setVisible(true);
                */
                _battleShakeCam.startShaking();
                break;
            case OPPONENT_DEFEATED:
                Gdx.app.log(TAG, "entity " + entity.getEntityConfig().getDisplayName() + " defeated!!");

                if (monster1Name.getText().toString().equals(entity.getEntityConfig().getDisplayName())) {
                    monster1Name.setVisible(false);
                }
                else if (monster2Name.getText().toString().equals(entity.getEntityConfig().getDisplayName())) {
                    monster2Name.setVisible(false);
                }
                else if (monster3Name.getText().toString().equals(entity.getEntityConfig().getDisplayName())) {
                    monster3Name.setVisible(false);
                }
                else if (monster4Name.getText().toString().equals(entity.getEntityConfig().getDisplayName())) {
                    monster4Name.setVisible(false);
                }
                else if (monster5Name.getText().toString().equals(entity.getEntityConfig().getDisplayName())){
                    monster5Name.setVisible(false);
                }
                break;
            case OPPONENT_TURN_DONE:
                selectedCharacter = null;

                enableButtons();
                /*
                attackButton.setDisabled(false);
                attackButton.setTouchable(Touchable.enabled);
                runButton.setDisabled(false);
                runButton.setTouchable(Touchable.enabled);
                */
                break;
            case PLAYER_USED_MAGIC:
                /*
                float x = _currentImagePosition.x + (_enemyWidth/2);
                float y = _currentImagePosition.y + (_enemyHeight/2);
                _effects.add(ParticleEffectFactory.getParticleEffect(ParticleEffectFactory.ParticleEffectType.WAND_ATTACK, x,y));
                */
                break;
            case PLAYER_RUNNING:
                resetControls();
                break;
            case CHARACTER_TURN_CHANGED:
                Entity.BattleEntityType type = entity.getBattleEntityType();
                if (type == Entity.BattleEntityType.ENEMY)  {
                    // disable player input and kick off enemy turn
                    disableButtons();
                    /*////////////////
                    middleStatsTextArea.addAction(Actions.fadeOut(0));
                    middleStatsTextArea.setText("", true);

                    backButton.addAction(Actions.fadeOut(0));
                    backButton.setWidth(middleAreaWidth);

                    statusButton.addAction(Actions.fadeOut(0));
                    statusButton.setText(BTN_NAME_STATUS);
*/
                    // look at second item in stack
                    //currentScreenState = screenStack.pop();
                    //ScreenState previousScreenState = screenStack.peek();
                    //screenStack.push(currentScreenState);

                    topLeftButton.addAction(Actions.fadeOut(0));
                    topRightButton.addAction(Actions.fadeOut(0));
                    runButton.addAction(Actions.fadeOut(0));
                    statusButton.addAction(Actions.fadeOut(0));

                    battleTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                    battleTextArea.interact(); // first interact sets battleTextArea visible
                    battleTextArea.interact();
                    /////////////////////////
                    game.battleState.opponentAttacks();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onNotify(Entity sourceEntity, Entity destinationEntity, BattleEvent event, String message) {
        Gdx.app.log(TAG, event.toString() + " notification received");

        switch(event){
            case PLAYER_HIT_DAMAGE:
                battleTextArea.populateText(message);
                break;
            case OPPONENT_TURN_DONE:
                battleTextArea.populateText(message);
                break;
            case PLAYER_TURN_DONE:
                // go back to Main screen and enable buttons
                ScreenState currentScreenState = screenStack.pop();
                ScreenState previousScreenState = screenStack.peek();
                screenStack.clear();
                screenStack.push(ScreenState.MAIN);

                // todo: screen shouldn't necessarily be shown here. might have to do it after opponent turn is done
                if (currentScreenState == ScreenState.FINAL) {

                    if (!transitionToMainScreen.isScheduled()) {
                        Timer.schedule(transitionToMainScreen, 4);
                    }
                }

                //String fullMsg;

                // store any updated stats
                ProfileManager.getInstance().setStatProperties(destinationEntity, true);

                // update HUD graphic stats for destination entity
                // todo: handle if destination entity is enemy
                Image hpBar = null;
                Label hpStats = null;
                Image mpBar = null;
                Label mpStats = null;

                if (destinationEntity.getEntityConfig().getDisplayName().equals(party1Name.getText().toString())) {
                    hpBar = hpBar1;
                    hpStats = hp1Stats;
                    mpBar = mpBar1;
                    mpStats = mp1Stats;
                }
                else if (destinationEntity.getEntityConfig().getDisplayName().equals(party2Name.getText().toString())) {
                    hpBar = hpBar2;
                    hpStats = hp2Stats;
                    mpBar = mpBar2;
                    mpStats = mp2Stats;
                }
                else if (destinationEntity.getEntityConfig().getDisplayName().equals(party3Name.getText().toString())) {
                    hpBar = hpBar3;
                    hpStats = hp3Stats;
                    mpBar = mpBar3;
                    mpStats = mp3Stats;
                }
                else if (destinationEntity.getEntityConfig().getDisplayName().equals(party4Name.getText().toString())) {
                    hpBar = hpBar4;
                    hpStats = hp4Stats;
                    mpBar = mpBar4;
                    mpStats = mp4Stats;
                }
                else if (destinationEntity.getEntityConfig().getDisplayName().equals(party5Name.getText().toString())) {
                    hpBar = hpBar5;
                    hpStats = hp5Stats;
                    mpBar = mpBar5;
                    mpStats = mp5Stats;
                }

                if (hpBar != null && hpStats != null && mpBar != null & mpStats != null) {
                    updateStatusBar(destinationEntity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP.toString()),
                            destinationEntity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP_MAX.toString()), hpBar, hpStats);

                    updateStatusBar(destinationEntity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MP.toString()),
                            destinationEntity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MP_MAX.toString()), mpBar, mpStats);
                }

                switch (previousScreenState) {
                    case FIGHT:
                        //fullMsg = String.format("%s attacked %s%s", sourceEntity.getEntityConfig().getDisplayName(),
                        //        destinationEntity.getEntityConfig().getDisplayName(),
                        //        message);
                        Gdx.app.log(TAG, message);
                        battleTextArea.populateText(message);
                        if (battleTextArea.interact()) {
                            showMainScreen(true);
                        }
                        break;
                    case INVENTORY:
                        //fullMsg = String.format("%s used %s on %s%s", sourceEntity.getEntityConfig().getDisplayName(),
                        //                                                    selectedInventoryElement.name,
                        //                                                    destinationEntity.getEntityConfig().getDisplayName(),
                         //                                                    message);
                        Gdx.app.log(TAG, message);
                        battleTextArea.populateText(message);
                        if (battleTextArea.interact()) {
                            showMainScreen(true);
                        }
                        break;
                    case SPELLS_BLACK:
                    case SPELLS_WHITE:
                    case POWER:
                        //fullMsg = String.format("%s used %s on %s%s", sourceEntity.getEntityConfig().getDisplayName(),
                         //                                                   selectedSpellsPowerElement.name,
                         //                                                   destinationEntity.getEntityConfig().getDisplayName(),
                         //                                                   message);
                        Gdx.app.log(TAG, message);
                        battleTextArea.populateText(message);
                        if (battleTextArea.interact()) {
                            showMainScreen(true);
                        }
                        break;
                }

                selectedCharacter = null;
                break;
        }

        game.battleState.getNextTurnCharacter();
    }

    private void showMainScreen(boolean immediate) {
        // cancel any pending transitions
        transitionToMainScreen.cancel();

        float delay = fadeTime;

        if (immediate) {
            delay = 0;
        }

        enableButtons();

        topLeftButton.setText(BTN_NAME_INVENTORY);
        topRightButton.setText(BTN_NAME_FIGHT);

        topLeftButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(delay * crossFadeInFactor)));
        runButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(delay * crossFadeInFactor)));
        topRightButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(delay * crossFadeInFactor)));
        statusButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(delay * crossFadeInFactor)));

        battleTextArea.addAction(Actions.fadeOut(delay * crossFadeOutFactor));
        battleTextArea.addAction(Actions.sequence(Actions.delay(delay * crossFadeOutFactor),
                new closeBattleTextAction(battleTextArea)));

        dummyButtonLeft.setVisible(false);
        dummyButtonLeft.setHeight(menuItemHeight + 2);
        dummyButtonLeft.setPosition(topLeftButton.getX(), 0);

        dummyButtonRight.setVisible(false);
        dummyButtonRight.setHeight(menuItemHeight + 2);
        dummyButtonRight.setPosition(dummyButtonLeft.getX() + dummyButtonLeft.getWidth() - 2, 0);

        middleStatsTextArea.setText("", true);
        middleStatsTextArea.addAction(Actions.fadeOut(delay * crossFadeOutFactor));
        middleStatsTextArea.addAction(Actions.sizeBy(0, backButtonHeight - 2, delay));
        middleStatsTextArea.addAction(Actions.moveBy(0, -backButtonHeight + 2, delay));
        middleTextAreaTable.setVisible(false);

        backButton.addAction(Actions.fadeOut(delay * crossFadeInFactor));
        backButton.addAction(Actions.sizeBy(0, -backButtonHeight - 3, delay)); // need -3 or else back button height wrong on final screen

        leftSummaryText.setText("");
    }

    private Timer.Task getTransitionToMainScreenTimer(){
        return new Timer.Task() {
            @Override
            public void run() {
                if (battleTextArea.isVisible()) {
                    showMainScreen(false);
                }
            }
        };
    }

    private void updateBanner(String text) {
        // this function raises current banner, changes text, and then lowers banner again
        float bannerWidth = calculateBannerWidth(text);

        selectedItemBanner.addAction(Actions.sizeBy(0, -selectedItemBannerHeight, fadeTime/2));
        selectedItemBanner.addAction(Actions.moveBy(0, selectedItemBannerHeight, fadeTime/2));

        selectedItemBanner.addAction(Actions.sequence(Actions.delay(fadeTime/2),
                new setTextAreaText(selectedItemBanner, text),
                new setTextFieldWidthAndPosition(selectedItemBanner, bannerWidth, (_stage.getWidth() - bannerWidth)/2),
                Actions.sizeBy(0, selectedItemBannerHeight, fadeTime),
                Actions.moveBy(0, -selectedItemBannerHeight, fadeTime),
                Actions.delay(2),
                Actions.parallel(Actions.sizeBy(0, -selectedItemBannerHeight, fadeTime/2),
                        Actions.moveBy(0, selectedItemBannerHeight, fadeTime/2))));
    }
}
