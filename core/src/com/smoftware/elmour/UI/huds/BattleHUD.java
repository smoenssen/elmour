package com.smoftware.elmour.UI.huds;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
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
import com.smoftware.elmour.main.ElmourGame;
import com.smoftware.elmour.entities.Entity;
import com.smoftware.elmour.entities.EntityConfig;
import com.smoftware.elmour.inventory.InventoryElement;
import com.smoftware.elmour.inventory.InventoryElementFactory;
import com.smoftware.elmour.inventory.PartyInventory;
import com.smoftware.elmour.inventory.PartyInventoryItem;
import com.smoftware.elmour.inventory.PartyInventoryObserver;
import com.smoftware.elmour.inventory.SpellPowerElementFactory;
import com.smoftware.elmour.inventory.SpellPowerElement;
import com.smoftware.elmour.actions.MyActions;
import com.smoftware.elmour.UI.components.BattleTextArea;
import com.smoftware.elmour.UI.components.MyTextArea;
import com.smoftware.elmour.UI.components.MyTextField;
import com.smoftware.elmour.UI.controls.BattleControlsObserver;
import com.smoftware.elmour.UI.controls.BattleControlsSubject;
import com.smoftware.elmour.main.Utility;
import com.smoftware.elmour.audio.AudioManager;
import com.smoftware.elmour.audio.AudioObserver;
import com.smoftware.elmour.audio.AudioSubject;
import com.smoftware.elmour.battle.BattleObserver;
import com.smoftware.elmour.maps.MapManager;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.screens.BattleScreen;
import com.smoftware.elmour.sfx.ScreenTransitionAction;
import com.smoftware.elmour.sfx.ScreenTransitionActor;
import com.smoftware.elmour.sfx.ShakeCamera;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.Semaphore;

import static com.smoftware.elmour.battle.BattleObserver.BattleEventWithMessage.MISS_HIT;
import static com.smoftware.elmour.battle.BattleObserver.BattleEventWithMessage.PLAYER_APPLIED_INVENTORY;
import static com.smoftware.elmour.battle.BattleObserver.BattleEventWithMessage.PLAYER_APPLIED_SPELL_POWER;

public class BattleHUD implements Screen, AudioSubject, BattleControlsObserver, StatusObserver, BattleObserver, PartyInventoryObserver {
    private static final String TAG = BattleHUD.class.getSimpleName();

    public enum ScreenState { FIGHT, FINAL, INVENTORY, MAIN, MAGIC, MENU, SPELLS_POWER, STATS, UNKNOWN }
    private Stack<ScreenState> screenStack;

    // for keeping track of node's expanded state
    // and .equals comparison for .contains function
    Array<rootNode> inventoryRootNodeArray = new Array<>();
    Array<rootNode> spellPowerRootNodeArray = new Array<>();

    class rootNode {
        String name = "";
        boolean isExpanded = false;
        rootNode(String name, boolean isExpanded) {
            this.name = name;
            this.isExpanded = isExpanded;
        }

        @Override
        public boolean equals(Object object) {
            if (object == null || !(object instanceof rootNode)) {
                return false;
            }

            rootNode other = (rootNode) object;

            return this.name.equals(other.name);
        }
    }

    class InventoryNode extends Tree.Node {
        InventoryElement.ElementID elementID;

        public InventoryNode(TextButton textButton, InventoryElement.ElementID elementID) {
            super(textButton);
            this.elementID = elementID;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InventoryNode that = (InventoryNode) o;
            return elementID.equals(that.elementID);
        }

        @Override
        public int hashCode() {

            return Objects.hash(elementID);
        }
    }

    class SpellPowerNode extends Tree.Node {
        SpellPowerElement.ElementID elementID;

        public SpellPowerNode(TextButton textButton, SpellPowerElement.ElementID elementID) {
            super(textButton);
            this.elementID = elementID;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SpellPowerNode that = (SpellPowerNode) o;
            return elementID.equals(that.elementID);
        }

        @Override
        public int hashCode() {

            return Objects.hash(elementID);
        }
    }

    final String POTIONS = "Potions";
    final String CONSUMABLES = "Consumables";
    final String THROWING = "Throwing Items";
    final String BLACK_MAGIC = "Black Magic";
    final String WHITE_MAGIC = "White Magic";
    final String POWERS = "Powers";
    final String BATTLE_WON = "Battle WON!";
    final String BATTLE_LOST = "Battle LOST!";

    // Inventory tree
    Tree.Node PotionsNode = new Tree.Node(new TextButton(POTIONS, Utility.ELMOUR_UI_SKIN, "tree_node"));
    Tree.Node ConsumablesNode = new Tree.Node(new TextButton(CONSUMABLES, Utility.ELMOUR_UI_SKIN, "tree_node"));
    Tree.Node ThrowingNode = new Tree.Node(new TextButton(THROWING, Utility.ELMOUR_UI_SKIN, "tree_node"));

    rootNode potionsRootNode = new rootNode(POTIONS, false);
    rootNode consumablesRootNode = new rootNode(CONSUMABLES, false);
    rootNode throwingRootNode = new rootNode(THROWING, false);

    // Spells/Power tree
    Tree.Node BlackMagicNode = new Tree.Node(new TextButton(BLACK_MAGIC, Utility.ELMOUR_UI_SKIN, "tree_node"));
    Tree.Node WhiteMagicNode = new Tree.Node(new TextButton(WHITE_MAGIC, Utility.ELMOUR_UI_SKIN, "tree_node"));
    Tree.Node PowersNode = new Tree.Node(new TextButton(POWERS, Utility.ELMOUR_UI_SKIN, "tree_node"));

    rootNode blackMagicRootNode = new rootNode(BLACK_MAGIC, false);
    rootNode whiteMagicRootNode = new rootNode(WHITE_MAGIC, false);
    rootNode powersRootNode = new rootNode(POWERS, false);

    private final String INVENTORY_EMPTY = "No inventory items";
    private final String ABILITIES_EMPTY = "No abilities";
    private final String SELECT_AN_ITEM = "Select an item";
    private final String SELECT_AN_ABILITY = "Select an ability";
    private final String CHOOSE_A_CHARACTER = "Choose a character";
    private final String CHOOSE_AN_ENEMY = "Choose an enemy";

    private final String BTN_NAME_INVENTORY = "Inventory";
    private final String BTN_NAME_FIGHT = "Fight";
    private final String BTN_NAME_RUN = "Run";
    private final String BTN_NAME_STATUS = "Status";
    private final String BTN_NAME_ABILITIES = "Abilities";
    private final String BTN_NAME_ATTACK = "Attack";
    private final String BTN_NAME_BACK = "Back";
    private final String BTN_NAME_OK = "OK";

    private ElmourGame game;
    private Stage _stage;
    private Viewport _viewport;
    private Camera _camera;

    private MyActions myActions;
    final public static float fadeTime = 0.35f;
    final public static float crossFadeInFactor = 0.75f;
    final public static float crossFadeOutFactor = 1.5f;
    private float currentDelta = 0;

    private float menuItemWidth;

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
    private Label enemy1Name;
    private Label enemy2Name;
    private Label enemy3Name;
    private Label enemy4Name;
    private Label enemy5Name;

    private Table leftSummaryTable;
    private ScrollPane leftScrollPanel;
    private Label leftSummaryText;

    private float middleAreaWidth;

    // scrolling spells/powers tree
    private Tree spellPowerTree;
    private ScrollPane spellPowerScrollPaneTree;

    //private List<String> spellPowerListView;
    //private ScrollPane middleScrollPaneList;
    private ArrayList<SpellPowerElement> spellPowerList;
    private SpellPowerElement selectedSpellPowerElement;

    // scrolling inventory tree
    private MyTextArea middleTreeTextArea;
    private Tree inventoryTree;
    private float middleTreeHeight;
    private ScrollPane inventoryScrollPaneTree;
    private InventoryElement selectedInventoryElement;

    // area under scrolling tree
    private float tablePadding = 15;
    private MyTextField middleStatsTextArea;    // using TextField because alignment doesn't work for TextAreas
    private Table middleTextAreaTable;
    private float backButtonHeight;
    private TextButton backButton;

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

    private BattleScreen battleScreen;
    private BattleTextArea battleTextArea;
    private TextButton dummyTextArea;

    private MyTextField battleWonStatsTextField;
    private ScrollPane battleWonStatsScrollPanel;
    private Table battleWonStatsTable;
    private float battleWonStatsRowHeight = 26;
    private Image dimmedScreen;

    public String selectedCharacter = null;

    private boolean turnInProgress = false;
    private boolean battleLost = false;
    private boolean battleWon = false;

    private int xpReward = 0;
    private int dibsReward = 0;
    private int rewardItemsShown = 0;

    public class RewardItem {
        public InventoryElement.ElementID itemID;
        public int quantity;
        public RewardItem() {
            quantity = 0;
        }
    }
    private Array<RewardItem> rewardItems;

    public BattleHUD(final ElmourGame game, final Camera camera, MapManager mapMgr, BattleScreen screen) {
        _camera = camera;
        _mapMgr = mapMgr;
        battleScreen = screen;
        this.game = game;

        game.battleState.addObserver(this);
        PartyInventory.getInstance().addObserver(this);

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

        myActions = new MyActions();

        rewardItems = new Array<>();

        _json = new Json();

        topLeftButton = new TextButton(BTN_NAME_INVENTORY, Utility.ELMOUR_UI_SKIN, "battle");
        topRightButton = new TextButton(BTN_NAME_FIGHT, Utility.ELMOUR_UI_SKIN, "battle");
        runButton = new TextButton(BTN_NAME_RUN, Utility.ELMOUR_UI_SKIN, "battle");
        statusButton = new TextButton(BTN_NAME_STATUS, Utility.ELMOUR_UI_SKIN, "battle");
        backButton = new TextButton("", Utility.ELMOUR_UI_SKIN, "battle");  // button text is set when resizing the button
        dummyButtonLeft = new TextButton("", Utility.ELMOUR_UI_SKIN, "battle");
        dummyButtonRight = new TextButton("", Utility.ELMOUR_UI_SKIN, "battle");

        menuItemWidth = _stage.getWidth()/5.5f;
        menuItemHeight = _stage.getHeight()/5.5f;
        float menuItemX = _stage.getWidth()/4.75f;
        float menuItemY = menuItemHeight;
        float leftTextAreaWidth = menuItemX;
        float selectedItemBannerWidth = 300;

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
        topLeftButton.setVisible(false);

        menuItemX += menuItemWidth - 2;
        topRightButton.setWidth(menuItemWidth);
        topRightButton.setHeight(menuItemHeight);
        topRightButton.setPosition(menuItemX, menuItemY - menuItemHeight);
        topRightButton.setVisible(false);

        runButton.setWidth(menuItemWidth);
        runButton.setHeight(menuItemHeight);
        runButton.setPosition(topLeftButton.getX(), 2 - menuItemHeight);
        runButton.setVisible(false);

        statusButton.setWidth(menuItemWidth);
        statusButton.setHeight(menuItemHeight);
        statusButton.setPosition(menuItemX, 2 - menuItemHeight);
        statusButton.setVisible(false);

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

        enemy1Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        enemy1Name.setText("");

        enemy2Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        enemy2Name.setText("");

        enemy3Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        enemy3Name.setText("");

        enemy4Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        enemy4Name.setText("");

        enemy5Name = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        enemy5Name.setText("");

        leftNameTable = new Table();

        // Desktop
        float nameWidth = menuItemWidth;
        float nameHeight = menuItemHeight / 8;

        // Android
        if (ElmourGame.isAndroid()) {
            nameWidth = menuItemWidth;
            nameHeight = menuItemHeight / 5;
        }

        // Common
        float leftMargin = 8;
        float rightMargin = 0;
        float topMargin = MathUtils.clamp((((menuItemHeight * 2) - (nameHeight * 6)) / 6) / 2, 2, 7.5f);
        float bottomMargin = topMargin * 1.5f;

        leftNameTable.row().pad(topMargin, leftMargin, bottomMargin, rightMargin);
        leftNameTable.add(enemy1Name).size(nameWidth, nameHeight);
        leftNameTable.row().pad(topMargin, leftMargin, bottomMargin, rightMargin);
        leftNameTable.add(enemy2Name).size(nameWidth, nameHeight);
        leftNameTable.row().pad(topMargin, leftMargin, bottomMargin, rightMargin);
        leftNameTable.add(enemy3Name).size(nameWidth, nameHeight);
        leftNameTable.row().pad(topMargin, leftMargin, bottomMargin, rightMargin);
        leftNameTable.add(enemy4Name).size(nameWidth, nameHeight);
        leftNameTable.row().pad(topMargin, leftMargin, bottomMargin, rightMargin);
        leftNameTable.add(enemy5Name).size(nameWidth, nameHeight);

        leftNameTable.pack();

        leftNameTable.setX(3);

        if (ElmourGame.isAndroid()) {
            leftNameTable.setY(2 - menuItemHeight);
        }
        else {
            leftNameTable.setY(4 - menuItemHeight);
        }


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

        float rightTextAreaWidth = _stage.getWidth() - (menuItemWidth * 2) - leftTextArea.getWidth() + 2;
        middleAreaWidth = menuItemWidth * 2 - 2;//(_stage.getWidth() - rightTextAreaWidth) / 2f;

        middleStatsTextArea = new MyTextField("", Utility.ELMOUR_UI_SKIN, "battle");
        middleStatsTextArea.disabled = true;
        middleStatsTextArea.setAlignment(Align.center);
        middleStatsTextArea.setWidth(middleAreaWidth);
        middleStatsTextArea.setHeight(menuItemHeight * 2 - 2);
        middleStatsTextArea.setPosition(leftTextAreaWidth, 2 - menuItemHeight);
        middleStatsTextArea.setVisible(false);

        battleTextArea = new BattleTextArea();
        battleTextArea.setWidth(middleAreaWidth);
        battleTextArea.setHeight((menuItemHeight * 2) - 2);
        battleTextArea.setPosition(_stage.getWidth() - rightTextAreaWidth - middleAreaWidth, 2);
        battleTextArea.setVisible(false);
        battleTextArea.setMovable(false);

        dummyTextArea = new TextButton("", Utility.ELMOUR_UI_SKIN, "battle");
        dummyTextArea.setWidth(middleAreaWidth);
        dummyTextArea.setHeight((menuItemHeight * 2) - 2);
        dummyTextArea.setPosition(_stage.getWidth() - rightTextAreaWidth - middleAreaWidth, 2 - menuItemHeight);

        middleTreeHeight = menuItemHeight * 2;

        middleTreeTextArea = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        middleTreeTextArea.disabled = true;
        middleTreeTextArea.setWidth(middleAreaWidth);
        middleTreeTextArea.setHeight(0);
        middleTreeTextArea.setPosition(_stage.getWidth() - rightTextAreaWidth - middleAreaWidth, menuItemHeight * 2 - 2);
        middleTreeTextArea.setVisible(false);

        inventoryTree = new Tree(Utility.ELMOUR_UI_SKIN) {
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

        inventoryTree.setIconSpacing(4, 4);

        spellPowerTree = new Tree(Utility.ELMOUR_UI_SKIN) {
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

        spellPowerTree.setIconSpacing(4, 4);

        spellPowerList = new ArrayList<>();
/*
        spellPowerListView = new List<>(Utility.ELMOUR_UI_SKIN);
        middleScrollPaneList = new ScrollPane(spellPowerListView);
        middleScrollPaneList.setWidth(middleTreeTextArea.getWidth() - 4);
        middleScrollPaneList.setHeight(0);
        middleScrollPaneList.setTouchable(Touchable.disabled);
        middleScrollPaneList.setPosition(middleTreeTextArea.getX() + 2, menuItemHeight * 2);
*/
        // set padding on left side of list elements
        Utility.ELMOUR_UI_SKIN.get(List.ListStyle.class).selection.setLeftWidth(15);

        inventoryScrollPaneTree = new ScrollPane(inventoryTree);
        inventoryScrollPaneTree.setWidth(middleTreeTextArea.getWidth() - 4);
        inventoryScrollPaneTree.setHeight(0);
        inventoryScrollPaneTree.setPosition(middleTreeTextArea.getX() + 2, menuItemHeight * 2);
        inventoryTree.setVisible(false);

        spellPowerScrollPaneTree = new ScrollPane(spellPowerTree);
        spellPowerScrollPaneTree.setWidth(middleTreeTextArea.getWidth() - 4);
        spellPowerScrollPaneTree.setHeight(0);
        spellPowerScrollPaneTree.setPosition(middleTreeTextArea.getX() + 2, menuItemHeight * 2);
        spellPowerTree.setVisible(false);

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
        rightTextArea.setPosition(statusButton.getX() + menuItemWidth - 2, 2 - menuItemHeight);
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

        // Android
        if (ElmourGame.isAndroid()) {
            nameWidth = 75;
            topMargin = topMargin / 2;
        }

        //topMargin = topMargin / 2;
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

        if (ElmourGame.isAndroid()) {
            rightTable.setY(2 - menuItemHeight);
        }
        else {
            rightTable.setY(4 - menuItemHeight);
        }

        // height and position of battleWonStatsTextField and battleWonStatsScrollPanel will be set dynamically
        // battleWonStatsTextField is only used as background for the battleWonStatsTable because
        // battleWonStatsTable background is transparent
        battleWonStatsTextField = new MyTextField("", Utility.ELMOUR_UI_SKIN, "battleLarge");
        battleWonStatsTextField.setAlignment(Align.center);
        battleWonStatsTextField.setWidth(middleAreaWidth);
        battleWonStatsTextField.setVisible(false);

        battleWonStatsTable = new Table();
        battleWonStatsTable.setWidth(battleWonStatsTextField.getWidth() - (2 * tablePadding));
        battleWonStatsTable.setHeight(battleWonStatsTextField.getHeight() - (2 * tablePadding));
        battleWonStatsTable.setPosition(battleWonStatsTextField.getX() + tablePadding, battleWonStatsTextField.getY() + battleWonStatsRowHeight/1.45f);
        battleWonStatsTable.align(Align.top);
        battleWonStatsTable.setVisible(false);

        battleWonStatsScrollPanel = new ScrollPane(battleWonStatsTable);
        battleWonStatsScrollPanel.setWidth(battleWonStatsTextField.getWidth() - (2 * tablePadding));
        battleWonStatsScrollPanel.setHeight(battleWonStatsTextField.getHeight() - (2 * tablePadding));
        battleWonStatsScrollPanel.setPosition(battleWonStatsTextField.getX() + tablePadding, battleWonStatsTextField.getY() + battleWonStatsRowHeight/1.45f);

        dimmedScreen = new Image(new Texture("graphics/black_rectangle_opacity66.png"));
        dimmedScreen.setWidth(_stage.getWidth());
        dimmedScreen.setHeight(_stage.getHeight());
        dimmedScreen.setPosition(0, 0);
        dimmedScreen.setVisible(false);

        _stage.addActor(selectedItemBanner);
        _stage.addActor(dummyTextArea);
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
        _stage.addActor(inventoryScrollPaneTree);
        _stage.addActor(spellPowerScrollPaneTree);
        //_stage.addActor(middleScrollPaneList);
        _stage.addActor(battleTextArea);
        _stage.addActor(rightTextArea);
        _stage.addActor(rightTable);
        _stage.addActor(dimmedScreen);
        _stage.addActor(battleWonStatsTextField);
        _stage.addActor(battleWonStatsScrollPanel);

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
                                                else if (topLeftButton.getText().toString().equals(BTN_NAME_ABILITIES) && currentScreenState == ScreenState.FIGHT) {
                                                    // load white spells
                                                    populateSpellPowerTree();

                                                    //spellPowerListView.setItems(strings);
                                                    //spellPowerListView.setSelectedIndex(-1);
                                                    setHUDNewState(ScreenState.SPELLS_POWER);
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

                                              FileHandle file = Gdx.files.local("RPGGame/maps/Game/Scripts/InventoryTest.json");
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
                                               turnInProgress = true;

                                               game.battleState.playerRuns();
                                               // see note below why this isn't being used
                                               //switchScreen(game, game.getScreenType(ElmourGame.ScreenType.MainGame));

                                               hideMainScreen();

                                               battleTextArea.show();
                                               battleTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));

                                               // delay for one frame here to fix issue with blip when battleTextArea is set to visible
                                               dummyTextArea.addAction(Actions.sequence(Actions.delay(currentDelta), Actions.fadeOut(currentDelta)));
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

                                                       if (previousScreenState != ScreenState.FIGHT) {
                                                           selectedItemBanner.addAction(Actions.sizeBy(0, -selectedItemBannerHeight, fadeTime / 2));
                                                           selectedItemBanner.addAction(Actions.moveBy(0, selectedItemBannerHeight, fadeTime / 2));
                                                       }

                                                       battleTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                                                       battleTextArea.show();

                                                       switch (previousScreenState) {
                                                           case FIGHT:
                                                               game.battleState.frontMeleeAttack();
                                                               break;
                                                           case INVENTORY:
                                                               game.battleState.applyInventoryItemToCharacter(selectedInventoryElement);
                                                               break;
                                                           case SPELLS_POWER:
                                                               game.battleState.applySpellPowerToCharacter(selectedSpellPowerElement);
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
                                             selectedCharacter = null;
                                         }
                                     }
                                 }
        );

        /*spellPowerTree.addListener(new ClickListener() {
                                    @Override
                                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                        return true;
                                    }

                                    @Override
                                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                        Gdx.app.log(TAG, "list clicked " + spellPowerListView.getSelected());

                                        // don't process touchUp if switched screens
                                        ScreenState currentScreenState = ScreenState.MAIN;

                                        if (screenStack.size() > 0) {
                                            currentScreenState = screenStack.peek();
                                        }

                                        // get associated element from spells/power list based on name
                                        SpellPowerElement element = null;
                                        for (SpellPowerElement item : spellPowerList) {
                                            if (item.name.equals(spellPowerListView.getSelected())) {
                                                element = item;
                                                break;
                                            }
                                        }

                                        if (element != null) {
                                            selectedSpellPowerElement = element;

                                            // replace asterisks in summary with a comma
                                            String summary = selectedSpellPowerElement.summary.replace('*', ',');
                                            leftSummaryText.setText(summary);

                                            middleTextAreaTable.clear();
                                            middleStatsTextArea.setText("", true);
                                            middleTextAreaTable.setVisible(true);

                                            if (selectedSpellPowerElement.MP != 0) {
                                                middleTextAreaTable.row().width(middleAreaWidth/2 - tablePadding);

                                                Label stat = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
                                                stat.setText("MP");
                                                stat.setAlignment(Align.left);
                                                Label value = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
                                                value.setText(String.format("%d", selectedSpellPowerElement.MP));
                                                value.setAlignment(Align.right);

                                                middleTextAreaTable.add(stat).align(Align.left);
                                                middleTextAreaTable.add(value).align(Align.right);//.padLeft(0);
                                            }
                                            if (selectedSpellPowerElement.DMG != 0) {
                                                middleTextAreaTable.row().width(middleAreaWidth/2 - tablePadding);

                                                Label stat = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
                                                stat.setText("DMG");
                                                stat.setAlignment(Align.left);
                                                Label value = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
                                                value.setText(String.format("%d", selectedSpellPowerElement.DMG));
                                                value.setAlignment(Align.right);

                                                middleTextAreaTable.add(stat).align(Align.left);
                                                middleTextAreaTable.add(value).align(Align.right);//.padLeft(0);
                                            }
                                            if (selectedSpellPowerElement.ACC != 0) {
                                                middleTextAreaTable.row().width(middleAreaWidth/2 - tablePadding);

                                                Label stat = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
                                                stat.setText("ACC");
                                                stat.setAlignment(Align.left);
                                                Label value = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
                                                value.setText(String.format("%d", selectedSpellPowerElement.ACC));
                                                value.setAlignment(Align.right);

                                                middleTextAreaTable.add(stat).align(Align.left);
                                                middleTextAreaTable.add(value).align(Align.right);//.padLeft(0);
                                            }
                                            if (selectedSpellPowerElement.effectList != null) {
                                                for (SpellPowerElement.EffectItem effect : selectedSpellPowerElement.effectList) {
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
        );*/

        spellPowerTree.addListener(new ClickListener() {
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

                                           // get associated element from spells/power list based on name
                                           /*SpellPowerElement element = null;
                                           for (SpellPowerElement item : spellPowerList) {
                                               if (item.name.equals(spellPowerListView.getSelected())) {
                                                   element = item;
                                                   break;
                                               }
                                           }*/

                                           Selection<Tree.Node> selection = spellPowerTree.getSelection();
                                           for (Tree.Node node : selection) {
                                               // get selected tree item
                                               SpellPowerElement element = (SpellPowerElement) node.getObject();

                                               if (element != null) {
                                                   selectedSpellPowerElement = element;

                                                   // replace asterisks in summary with a comma
                                                   String summary = selectedSpellPowerElement.summary.replace('*', ',');
                                                   leftSummaryText.setText(summary);

                                                   middleTextAreaTable.clear();
                                                   middleStatsTextArea.setText("", true);
                                                   middleTextAreaTable.setVisible(true);

                                                   if (selectedSpellPowerElement.MP != 0) {
                                                       middleTextAreaTable.row().width(middleAreaWidth / 2 - tablePadding);

                                                       Label stat = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
                                                       stat.setText("MP");
                                                       stat.setAlignment(Align.left);
                                                       Label value = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
                                                       value.setText(String.format("%d", selectedSpellPowerElement.MP));
                                                       value.setAlignment(Align.right);

                                                       middleTextAreaTable.add(stat).align(Align.left);
                                                       middleTextAreaTable.add(value).align(Align.right);//.padLeft(0);
                                                   }
                                                   if (selectedSpellPowerElement.DMG != 0) {
                                                       middleTextAreaTable.row().width(middleAreaWidth / 2 - tablePadding);

                                                       Label stat = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
                                                       stat.setText("DMG");
                                                       stat.setAlignment(Align.left);
                                                       Label value = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
                                                       value.setText(String.format("%d", selectedSpellPowerElement.DMG));
                                                       value.setAlignment(Align.right);

                                                       middleTextAreaTable.add(stat).align(Align.left);
                                                       middleTextAreaTable.add(value).align(Align.right);//.padLeft(0);
                                                   }
                                                   if (selectedSpellPowerElement.ACC != 0) {
                                                       middleTextAreaTable.row().width(middleAreaWidth / 2 - tablePadding);

                                                       Label stat = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
                                                       stat.setText("ACC");
                                                       stat.setAlignment(Align.left);
                                                       Label value = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
                                                       value.setText(String.format("%d", selectedSpellPowerElement.ACC));
                                                       value.setAlignment(Align.right);

                                                       middleTextAreaTable.add(stat).align(Align.left);
                                                       middleTextAreaTable.add(value).align(Align.right);//.padLeft(0);
                                                   }
                                                   if (selectedSpellPowerElement.effectList != null) {
                                                       for (SpellPowerElement.EffectItem effect : selectedSpellPowerElement.effectList) {
                                                           middleTextAreaTable.row().width(middleAreaWidth / 2 - tablePadding);

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
                                               else {
                                                   leftSummaryText.setText("");
                                                   middleStatsTextArea.setText("", true);
                                                   middleTextAreaTable.clear();
                                                   middleStatsTextArea.setText(SELECT_AN_ABILITY, true);

                                                   // expand or collapse if root node selected
                                                   TextButton btn = (TextButton)node.getActor();
                                                   if (btn != null) {
                                                       for (rootNode r : spellPowerRootNodeArray) {
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
                                                   spellPowerTree.getSelection().clear();
                                               }

                                               // should only be one node selected
                                               break;
                                           }
                                       }
                                   }
        );

        inventoryTree.addListener(new ClickListener() {
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

                                       Selection<Tree.Node> selection = inventoryTree.getSelection();
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
                                               middleTextAreaTable.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));

                                               String effectList = "";
                                               for (InventoryElement.EffectItem effect : selectedInventoryElement.effectList) {
                                                   middleTextAreaTable.row().width(middleAreaWidth/2 - tablePadding);

                                                   Label stat = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
                                                   stat.setText(getEffectText(effect));
                                                   stat.setAlignment(Align.left);
                                                   Label value = new Label("", Utility.ELMOUR_UI_SKIN, "battle");;
                                                   value.setText(getEffectValue(effect));
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
                                                   for (rootNode r : inventoryRootNodeArray) {
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
                                               inventoryTree.getSelection().clear();

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
                                           if (turnInProgress) return;

                                           if (battleTextArea.getText().equals(BATTLE_WON)) {
                                               battleTextArea.cleanupTextArea();

                                               int numRows = 2;
                                               setBattleWonStatsControlSize(numRows);

                                               showMainScreen(true);
                                               dimmedScreen.addAction(Actions.fadeOut(0));
                                               dimmedScreen.setVisible(true);
                                               dimmedScreen.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));

                                               battleWonStatsTextField.addAction(Actions.fadeOut(0));
                                               battleWonStatsTextField.setVisible(true);
                                               battleWonStatsTextField.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));

                                               battleWonStatsTable.clear();
                                               battleWonStatsTable.setVisible(true);

                                               battleWonStatsTable.row().width(battleWonStatsTable.getWidth() / 2).height(battleWonStatsRowHeight);
                                               Label stat = new Label("", Utility.ELMOUR_UI_SKIN, "battleLarge");
                                               stat.setText("EXP Gained");
                                               stat.setAlignment(Align.left);
                                               Label value = new Label("", Utility.ELMOUR_UI_SKIN, "battleLarge");
                                               value.setText(String.format("%d", xpReward));
                                               value.setAlignment(Align.right);
                                               battleWonStatsTable.add(stat).align(Align.left);
                                               battleWonStatsTable.add(value).align(Align.right);

                                               battleWonStatsTable.row().width(battleWonStatsTable.getWidth() / 2).height(battleWonStatsRowHeight);
                                               Label stat2 = new Label("", Utility.ELMOUR_UI_SKIN, "battleLarge");
                                               stat2.setText("Dibs Gained");
                                               stat2.setAlignment(Align.left);
                                               Label value2 = new Label("", Utility.ELMOUR_UI_SKIN, "battleLarge");
                                               value2.setText(String.format("%d", dibsReward));
                                               value2.setAlignment(Align.right);
                                               battleWonStatsTable.add(stat2).align(Align.left);
                                               battleWonStatsTable.add(value2).align(Align.right);

                                               // distribute rewards
                                               // XP is split among living party members
                                               game.statusUI.updatePartyXP(xpReward);

                                               // DIBS go to the party
                                               game.statusUI.updatePartyDibs(dibsReward);

                                               // enable scroll panel
                                               battleWonStatsScrollPanel.setTouchable(Touchable.enabled);

                                               // reset for next battle
                                               xpReward = 0;
                                               dibsReward = 0;
                                           } else if (battleTextArea.getText().equals(BATTLE_LOST)) {
                                               battleTextArea.setTouchable(Touchable.disabled);
                                               battleTextArea.cleanupTextArea();
                                               game.battleState.gameOver();
                                               if (!resetControlsTimer().isScheduled()) {
                                                   Timer.schedule(resetControlsTimer(), 1);
                                               }
                                           } else if (battleWon) {
                                               battleTextArea.cleanupTextArea();
                                               battleTextArea.populateText(BATTLE_WON);
                                           } else if (battleLost) {
                                               battleTextArea.cleanupTextArea();
                                               battleTextArea.populateText(BATTLE_LOST);
                                           } else {
                                               turnInProgress = true;

                                               if (game.battleState.getCurrentTurnCharacter().getBattleEntityType() == Entity.BattleEntityType.PARTY) {
                                                   if (game.battleState.peekNextTurnCharacter().getBattleEntityType() == Entity.BattleEntityType.ENEMY) {
                                                       battleTextArea.cleanupTextArea();

                                                       // show dummyTextArea briefly to fix blip before battleTextArea
                                                       dummyTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                                                   } else {
                                                       battleTextArea.hide();
                                                       showMainScreen(true);
                                                   }
                                               } else {
                                                   // enemy
                                                   if (game.battleState.peekNextTurnCharacter().getBattleEntityType() == Entity.BattleEntityType.ENEMY) {
                                                       battleTextArea.cleanupTextArea();

                                                       // show dummyTextArea briefly to fix blip before next
                                                       // battleTextArea when there are enemy turns back to back
                                                       dummyTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                                                   } else {
                                                       showMainScreen(true);
                                                   }
                                               }

                                               game.battleState.getNextTurnCharacter(0);
                                           }
                                       }
                                   }
        );

        battleWonStatsScrollPanel.addListener(new ClickListener() {
                                       @Override
                                       public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                           return true;
                                       }

                                       @Override
                                       public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                           int maxRowsToShow = 10;

                                           if (ElmourGame.isAndroid())
                                               maxRowsToShow = 6;

                                           if (battleWon && rewardItems.size > 0 && rewardItemsShown < rewardItems.size) {

                                               int numRowsToShow = MathUtils.clamp(rewardItems.size - rewardItemsShown, 0, maxRowsToShow);
                                               setBattleWonStatsControlSize(numRowsToShow + 1);

                                               battleWonStatsTable.clear();

                                               battleWonStatsTable.row().width(battleWonStatsTable.getWidth()/2).height(battleWonStatsRowHeight);
                                               Label stat = new Label("", Utility.ELMOUR_UI_SKIN, "battleLarge");
                                               stat.setText("Items Gained:");
                                               stat.setAlignment(Align.left);
                                               Label value = new Label("", Utility.ELMOUR_UI_SKIN, "battleLarge");
                                               value.setText("");
                                               value.setAlignment(Align.right);
                                               battleWonStatsTable.add(stat).align(Align.left);
                                               battleWonStatsTable.add(value).align(Align.right);

                                               for (int i = rewardItemsShown, j = 0; i < rewardItems.size && j < numRowsToShow; i++, j++) {
                                                   RewardItem item = rewardItems.get(i);
                                                   battleWonStatsTable.row().width(battleWonStatsTable.getWidth() / 2).height(battleWonStatsRowHeight);
                                                   Label stat2 = new Label("", Utility.ELMOUR_UI_SKIN, "battleLarge");

                                                   InventoryElement element = InventoryElementFactory.getInstance().getInventoryElement(item.itemID);
                                                   PartyInventory.getInstance().addItem(element, item.quantity, 0, true);

                                                   stat2.setText(element.name);
                                                   stat2.setAlignment(Align.left);
                                                   Label value2 = new Label("", Utility.ELMOUR_UI_SKIN, "battleLarge");
                                                   value2.setText(String.format("%d", item.quantity));
                                                   value2.setAlignment(Align.right);
                                                   battleWonStatsTable.add(stat2).align(Align.left);
                                                   battleWonStatsTable.add(value2).align(Align.right);

                                                   rewardItemsShown++;
                                               }
                                           }
                                           else {
                                               dimmedScreen.addAction(Actions.fadeOut(1));
                                               battleWonStatsTextField.setVisible(false);
                                               battleWonStatsTable.setVisible(false);

                                               game.battleState.battleOver();

                                               // reset for next battle
                                               rewardItems.clear();
                                               rewardItemsShown = 0;
                                               battleWon = false;
                                               battleWonStatsScrollPanel.setTouchable(Touchable.disabled);

                                               if( !resetControlsTimer().isScheduled() ){
                                                   Timer.schedule(resetControlsTimer(), 1);
                                               }
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

    private String getEffectText(InventoryElement.EffectItem effect) {
        String ret = effect.effect.toString();

        switch (effect.effect) {
            case HEAL_HP:
            case HEAL_HP_PERCENT:
            case HEAL_MP:
            case HEAL_MP_PERCENT:
                ret = "HEAL";
                break;
            default:
                ret = ret.replace("_DOWN", "");
                ret = ret.replace("_UP", "");
        }

        if (effect.turns > 0) {
            ret += " (for " + effect.turns + " turns)";
        }
        else if (selectedInventoryElement.turns > 0) {
            ret += " (for " + selectedInventoryElement.turns + " turns)";
        }

        return ret;
    }

    private String getEffectValue(InventoryElement.EffectItem effect) {
        String ret = effect.value.toString();

        switch (effect.effect) {
            case HEAL_HP:
                ret = effect.value.toString() + " HP";
                break;
            case HEAL_HP_PERCENT:
                ret = effect.value.toString() + "% HP";
                break;
            case HEAL_MP:
                ret = effect.value.toString() + " MP";
                break;
            case HEAL_MP_PERCENT:
                ret = effect.value.toString() + "% MP";
                break;
            default:
                if (effect.effect.toString().contains("_DOWN")) {
                   ret = "- " + effect.value.toString();
                }
                else if (effect.effect.toString().contains("_UP")) {
                    ret = "+ " + effect.value.toString();
                }
        }

        return ret;
    }

    private void setBattleWonStatsControlSize(int numRows) {
        // height should be number of rows plus a little to allow for top and bottom padding
        float rowHeight = numRows + 1.125f;
        battleWonStatsTextField.setHeight(battleWonStatsRowHeight * rowHeight);

        float x = _stage.getWidth()/2 - battleWonStatsTextField.getWidth()/2;
        float y = (menuItemHeight * 2) + (_stage.getHeight() - (menuItemHeight * 2))/2 - battleWonStatsTextField.getHeight()/2;

        battleWonStatsTextField.setPosition(x, y);

        battleWonStatsTable.setHeight(battleWonStatsTextField.getHeight() - (2 * tablePadding));
        battleWonStatsTable.setPosition(battleWonStatsTextField.getX() + tablePadding, battleWonStatsTextField.getY() + battleWonStatsRowHeight/1.45f);
        battleWonStatsScrollPanel.setHeight(battleWonStatsTable.getHeight());
        battleWonStatsScrollPanel.setPosition(battleWonStatsTable.getX(), battleWonStatsTable.getY());
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

        if (ElmourGame.isAndroid()) {
            stats.setPosition(1, -6);
        }
        else {
            stats.setPosition(1, -8);
        }
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
            dummyTextArea.act(delta);
            battleTextArea.act(delta);
            inventoryTree.act(delta);
            middleTreeTextArea.act(delta);
            inventoryScrollPaneTree.act(delta);
            spellPowerScrollPaneTree.act(delta);
            //middleScrollPaneList.act(delta);
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

        //todo can't do this here, but need to disable battleTextArea until text has completed
        //battleTextArea.setTouchable(Touchable.disabled);
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
                topLeftButton.setText(BTN_NAME_ABILITIES);
                topRightButton.setText(BTN_NAME_ATTACK);
                runButton.addAction(Actions.fadeOut(fadeTime * 0.25f));
                statusButton.addAction(Actions.fadeOut(fadeTime * 0.25f));

                backButton.setHeight(2);
                backButton.setVisible(true);
                backButton.addAction(Actions.sequence(Actions.delay(fadeTime * 0.25f), myActions.new setButtonText(backButton, BTN_NAME_BACK)));
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
                    enemy1Name.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));
                    enemy2Name.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));
                    enemy3Name.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));
                    enemy4Name.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));
                    enemy5Name.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));

                    leftSummaryText.addAction(Actions.fadeOut(fadeTime/2));
                    leftSummaryText.addAction(Actions.sequence(Actions.delay(fadeTime/2), myActions.new setLabelText(leftSummaryText, "")));

                    inventoryTree.setTouchable(Touchable.disabled);

                    middleTreeTextArea.addAction(Actions.sizeBy(0, -middleTreeHeight, fadeTime));
                    middleTreeTextArea.addAction(Actions.sequence(Actions.delay(fadeTime * 0.8f), Actions.fadeOut(fadeTime * 0.2f)));

                    inventoryScrollPaneTree.addAction(Actions.sizeBy(0, (middleTreeHeight - 4) * -1, fadeTime));

                    backButton.setHeight(2);
                    backButton.setVisible(true);
                    backButton.addAction(Actions.sequence(Actions.delay(fadeTime * 0.25f), myActions.new setButtonText(backButton, BTN_NAME_BACK)));
                    backButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                    backButton.addAction(Actions.sizeBy(0, backButtonHeight - 2, fadeTime));

                    middleTextAreaTable.addAction(Actions.fadeOut(fadeTime/2));
                    middleTextAreaTable.addAction(Actions.sequence(Actions.delay(fadeTime/2), myActions.new setTableVisible(middleTextAreaTable, false)));

                    middleStatsTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                    middleStatsTextArea.addAction(Actions.sizeBy(0, -backButtonHeight + 2, fadeTime));
                    middleStatsTextArea.addAction(Actions.moveBy(0, backButtonHeight - 2, fadeTime));
                    middleStatsTextArea.addAction(Actions.sequence(Actions.delay(fadeTime), myActions.new setTextAreaText(middleStatsTextArea, CHOOSE_A_CHARACTER)));

                    selectedItemBanner.setWidth(calculateBannerWidth(selectedInventoryElement.name));
                    selectedItemBanner.setPosition((_stage.getWidth() - selectedItemBanner.getWidth())/2 , _stage.getHeight() + 8);
                    selectedItemBanner.addAction(Actions.sizeBy(0, selectedItemBannerHeight, fadeTime));
                    selectedItemBanner.addAction(Actions.moveBy(0, -selectedItemBannerHeight, fadeTime));
                    selectedItemBanner.setText(selectedInventoryElement.name, true);
                }
                else if (currentScreenState == ScreenState.FIGHT) {
                    // no transition here, just set the size and position
                    middleStatsTextArea.setHeight(topLeftButton.getHeight());
                    middleStatsTextArea.setPosition(middleStatsTextArea.getX(), topLeftButton.getY());
                    middleStatsTextArea.setVisible(true);
                    middleStatsTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                    middleStatsTextArea.addAction(Actions.sequence(Actions.delay(fadeTime/4), myActions.new setTextAreaText(middleStatsTextArea, CHOOSE_AN_ENEMY)));

                    topLeftButton.addAction(Actions.fadeOut(0));
                    topRightButton.addAction(Actions.fadeOut(0));
                }
                else if (currentScreenState == ScreenState.SPELLS_POWER)
                {
                    enemy1Name.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));
                    enemy2Name.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));
                    enemy3Name.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));
                    enemy4Name.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));
                    enemy5Name.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));

                    leftSummaryText.addAction(Actions.fadeOut(fadeTime/2));
                    leftSummaryText.addAction(Actions.sequence(Actions.delay(fadeTime/2), myActions.new setLabelText(leftSummaryText, "")));

                    middleTreeTextArea.addAction(Actions.sizeBy(0, -middleTreeHeight, fadeTime));
                    middleTreeTextArea.addAction(Actions.sequence(Actions.delay(fadeTime * 0.8f), Actions.fadeOut(fadeTime * 0.2f)));

                    spellPowerTree.setTouchable(Touchable.disabled);
                    spellPowerScrollPaneTree.addAction(Actions.sizeBy(0, (middleTreeHeight - 4) * -1, fadeTime));

                    backButton.setHeight(2);
                    backButton.setVisible(true);
                    backButton.addAction(Actions.sequence(Actions.delay(fadeTime * 0.25f), myActions.new setButtonText(backButton, BTN_NAME_BACK)));
                    backButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                    backButton.addAction(Actions.sizeBy(0, backButtonHeight - 2, fadeTime));

                    middleTextAreaTable.setVisible(false);

                    middleStatsTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                    middleStatsTextArea.addAction(Actions.sizeBy(0, -backButtonHeight + 2, fadeTime));
                    middleStatsTextArea.addAction(Actions.moveBy(0, backButtonHeight - 2, fadeTime));

                    if (currentScreenState == ScreenState.SPELLS_POWER) {
                        middleStatsTextArea.addAction(Actions.sequence(Actions.delay(fadeTime), myActions.new setTextAreaText(middleStatsTextArea, CHOOSE_A_CHARACTER)));
                    }
                    else {
                        middleStatsTextArea.addAction(Actions.sequence(Actions.delay(fadeTime), myActions.new setTextAreaText(middleStatsTextArea, CHOOSE_AN_ENEMY)));
                    }

                    selectedItemBanner.setWidth(calculateBannerWidth(selectedSpellPowerElement.name));
                    selectedItemBanner.setPosition((_stage.getWidth() - selectedItemBanner.getWidth())/2 , _stage.getHeight() + 8);
                    selectedItemBanner.addAction(Actions.sizeBy(0, selectedItemBannerHeight, fadeTime));
                    selectedItemBanner.addAction(Actions.moveBy(0, -selectedItemBannerHeight, fadeTime));
                    selectedItemBanner.setText(selectedSpellPowerElement.name, true);
                }
                break;
            case INVENTORY:
                enemy1Name.addAction(Actions.fadeOut(fadeTime));
                enemy2Name.addAction(Actions.fadeOut(fadeTime));
                enemy3Name.addAction(Actions.fadeOut(fadeTime));
                enemy4Name.addAction(Actions.fadeOut(fadeTime));
                enemy5Name.addAction(Actions.fadeOut(fadeTime));

                topLeftButton.addAction(Actions.fadeOut(fadeTime * crossFadeOutFactor));
                runButton.addAction(Actions.fadeOut(fadeTime * crossFadeOutFactor));
                topRightButton.addAction(Actions.fadeOut(fadeTime * crossFadeOutFactor));
                statusButton.addAction(Actions.fadeOut(fadeTime * crossFadeOutFactor));

                inventoryTree.setTouchable(Touchable.enabled);

                middleStatsTextArea.setPosition(middleStatsTextArea.getX(), 2);
                middleStatsTextArea.setVisible(true);
                middleStatsTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime * crossFadeInFactor)));
                if (PartyInventory.getInstance().getSize() == 0)
                    middleStatsTextArea.setText(INVENTORY_EMPTY, true);
                else
                    middleStatsTextArea.setText(SELECT_AN_ITEM, true);

                middleTreeTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime * 0.2f)));
                middleTreeTextArea.addAction(Actions.sizeBy(0, middleTreeHeight, fadeTime));
                middleTreeTextArea.setVisible(true);

                // reset tree selection
                inventoryTree.setVisible(true);
                inventoryTree.collapseAll();
                Selection<Tree.Node> selection = inventoryTree.getSelection();
                if (selection != null) {
                    selection.clear();
                }

                inventoryScrollPaneTree.addAction(Actions.sizeBy(0, middleTreeHeight - 4, fadeTime));

                leftSummaryText.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));

                break;
            case MAIN:
                // the MAIN screen should not be set as a new state, only as a previous state
                break;
            case MAGIC:
                break;
            case MENU:
                break;
            case SPELLS_POWER:
                enemy1Name.addAction(Actions.fadeOut(fadeTime));
                enemy2Name.addAction(Actions.fadeOut(fadeTime));
                enemy3Name.addAction(Actions.fadeOut(fadeTime));
                enemy4Name.addAction(Actions.fadeOut(fadeTime));
                enemy5Name.addAction(Actions.fadeOut(fadeTime));

                topLeftButton.addAction(Actions.fadeOut(fadeTime/2));
                runButton.addAction(Actions.fadeOut(fadeTime/2));
                topRightButton.addAction(Actions.fadeOut(fadeTime/2));
                statusButton.addAction(Actions.fadeOut(0));

                spellPowerTree.setTouchable(Touchable.enabled);

                middleStatsTextArea.setVisible(true);

                if (spellPowerList.size() > 0)
                    middleStatsTextArea.addAction(Actions.sequence(Actions.delay(fadeTime), myActions.new setTextAreaText(middleStatsTextArea, SELECT_AN_ABILITY)));
                else
                    middleStatsTextArea.addAction(Actions.sequence(Actions.delay(fadeTime), myActions.new setTextAreaText(middleStatsTextArea, ABILITIES_EMPTY)));

                middleStatsTextArea.setHeight(topLeftButton.getHeight());
                middleStatsTextArea.setPosition(middleStatsTextArea.getX(), topLeftButton.getY());
                middleStatsTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                middleStatsTextArea.addAction(Actions.sizeBy(0, backButtonHeight - 2, fadeTime));
                middleStatsTextArea.addAction(Actions.moveBy(0, -backButtonHeight + 2, fadeTime));

                if (MathUtils.round(backButton.getWidth()) == MathUtils.round(topLeftButton.getWidth())) {
                    dummyButtonRight.setWidth(menuItemWidth);
                    dummyButtonRight.setHeight(menuItemHeight);
                    dummyButtonRight.setPosition(statusButton.getX(), statusButton.getY());
                    dummyButtonRight.setVisible(true);
                    dummyButtonRight.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                    dummyButtonRight.addAction(Actions.sizeBy(-menuItemWidth, -menuItemHeight, fadeTime));
                    dummyButtonRight.addAction(Actions.moveBy(menuItemWidth, 0, fadeTime));
                    dummyButtonRight.addAction(Actions.sequence(Actions.delay(fadeTime), Actions.fadeOut(0)));

                    backButton.addAction(Actions.sequence(Actions.sizeBy(menuItemWidth, -backButtonHeight + 2, fadeTime), Actions.fadeOut(0)));
                }
                else {
                    backButton.addAction(Actions.sequence(Actions.sizeBy(0, -backButtonHeight + 2, fadeTime), Actions.fadeOut(0)));
                }

                backButton.addAction(Actions.sequence(Actions.delay(fadeTime * 0.8f), myActions.new setButtonText(backButton, "")));

                middleTreeTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0.2f)));
                middleTreeTextArea.addAction(Actions.sizeBy(0, middleTreeHeight, fadeTime));
                middleTreeTextArea.setVisible(true);

                // reset tree selection
                spellPowerTree.setVisible(true);
                spellPowerTree.collapseAll();
                Selection<Tree.Node> spellPowerTreeSelection = spellPowerTree.getSelection();
                if (spellPowerTreeSelection != null) {
                    spellPowerTreeSelection.clear();
                }

                spellPowerScrollPaneTree.addAction(Actions.sizeBy(0, middleTreeHeight - 4, fadeTime));

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

                    } else if (currentScreenState == ScreenState.SPELLS_POWER) {
                        //todo spells or power
                        topLeftButton.setText(BTN_NAME_ABILITIES);
                        topRightButton.setText(BTN_NAME_ATTACK);

                        backButton.setHeight(backButtonHeight);
                        topLeftButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime / 2)));
                        topRightButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime / 2)));
                        backButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime / 2)));

                        leftSummaryText.setText("");

                        enemy1Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        enemy2Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        enemy3Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        enemy4Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        enemy5Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));

                        middleTreeTextArea.addAction(Actions.sizeBy(0, -middleTreeHeight, fadeTime));
                        middleTreeTextArea.addAction(Actions.sequence(Actions.delay(fadeTime * 0.8f), Actions.fadeOut(fadeTime * 0.2f)));

                        spellPowerTree.setTouchable(Touchable.disabled);
                        spellPowerScrollPaneTree.addAction(Actions.sizeBy(0, (middleTreeHeight - 4) * -1, fadeTime));

                        middleStatsTextArea.setText("", true);
                        middleStatsTextArea.addAction(Actions.fadeOut(fadeTime / 2));
                        middleTextAreaTable.setVisible(false);
                        leftSummaryText.setText("");

                        backButton.setText(BTN_NAME_BACK);
                    }
                    else if (currentScreenState == ScreenState.FINAL) {
                        selectedCharacter = null;

                        topLeftButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime/4)));
                        topRightButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime/4)));

                        middleStatsTextArea.setText("", true);
                        middleStatsTextArea.addAction(Actions.sizeBy(0, backButtonHeight - 2, 0));
                        middleStatsTextArea.addAction(Actions.moveBy(0, -backButtonHeight + 2, 0));
                        middleStatsTextArea.addAction(Actions.fadeOut(fadeTime/2));
                        middleStatsTextArea.addAction(Actions.sequence(Actions.delay(fadeTime/2), myActions.new setTextAreaVisible(middleStatsTextArea, false)));

                        Gdx.app.log(TAG,"backButton.getWidth() = " + MathUtils.round(backButton.getWidth()) + " topLeftButton.getWidth() = " + MathUtils.round(topLeftButton.getWidth()));
                        if (MathUtils.round(backButton.getWidth()) == MathUtils.round(topLeftButton.getWidth()))
                            backButton.addAction(Actions.sizeBy(menuItemWidth - 2, 0, fadeTime));

                        statusButton.addAction(Actions.sizeBy(-menuItemWidth, 0, fadeTime));
                        statusButton.addAction(Actions.moveBy(menuItemWidth, 0, fadeTime));
                        statusButton.addAction(Actions.sequence(Actions.delay(fadeTime * 0.8f), myActions.new setButtonText(statusButton, "")));
                        statusButton.addAction(Actions.sequence(Actions.delay(fadeTime), Actions.fadeOut(0)));
                    }
                    break;
                case FINAL:
                    if (currentScreenState == ScreenState.INVENTORY) {
                        middleTreeTextArea.addAction(Actions.sizeBy(0, -middleTreeHeight, fadeTime));
                        middleTreeTextArea.addAction(Actions.sequence(Actions.delay(fadeTime * 0.8f), Actions.fadeOut(fadeTime * 0.2f)));

                        inventoryScrollPaneTree.addAction(Actions.sizeBy(0, (middleTreeHeight - 4) * -1, fadeTime));

                        backButton.setVisible(true);
                        backButton.addAction(Actions.sequence(Actions.delay(fadeTime * 0.25f), myActions.new setButtonText(backButton, BTN_NAME_BACK)));
                        backButton.addAction(Actions.sizeBy(0, backButtonHeight + 3, fadeTime));

                        middleStatsTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                        middleStatsTextArea.setText(CHOOSE_A_CHARACTER, true);
                        middleStatsTextArea.addAction(Actions.sizeBy(0, -backButtonHeight, fadeTime));
                        middleStatsTextArea.addAction(Actions.moveBy(0, backButtonHeight, fadeTime));
                    }

                    break;
                case INVENTORY:
                    if (currentScreenState == ScreenState.FINAL) {
                        inventoryTree.setTouchable(Touchable.enabled);
                        inventoryTree.setVisible(true);

                        middleTextAreaTable.addAction(Actions.fadeOut(0));
                        middleTextAreaTable.setVisible(true);
                        middleTextAreaTable.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));

                        inventoryScrollPaneTree.addAction(Actions.sizeBy(0, middleTreeHeight - 4, fadeTime));

                        setCommonTransitionBackFromFinal(ScreenState.INVENTORY);
                    }

                    break;
                case MAIN:
                    if (currentScreenState == ScreenState.INVENTORY) {

                        topLeftButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime * crossFadeInFactor)));
                        runButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime * crossFadeInFactor)));
                        topRightButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime * crossFadeInFactor)));
                        statusButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime * crossFadeInFactor)));

                        enemy1Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        enemy2Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        enemy3Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        enemy4Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        enemy5Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));

                        inventoryTree.setTouchable(Touchable.disabled);

                        middleTreeTextArea.addAction(Actions.sizeBy(0, -middleTreeHeight, fadeTime));
                        middleTreeTextArea.addAction(Actions.sequence(Actions.delay(fadeTime * 0.8f), Actions.fadeOut(fadeTime * 0.2f)));

                        inventoryScrollPaneTree.addAction(Actions.sizeBy(0, (middleTreeHeight - 4) * -1, fadeTime));

                        middleStatsTextArea.setText("", true);
                        middleStatsTextArea.addAction(Actions.fadeOut(fadeTime * crossFadeOutFactor));
                        middleTextAreaTable.setVisible(false);
                        leftSummaryText.setText("");

                        // reset root node array
                        for (rootNode r : inventoryRootNodeArray) {
                            r.isExpanded = false;
                        }
                    }
                    else if (currentScreenState == ScreenState.FIGHT) {
                        topLeftButton.setText(BTN_NAME_INVENTORY);
                        topRightButton.setText(BTN_NAME_FIGHT);
                        runButton.addAction(Actions.sequence(Actions.delay(fadeTime / 2), Actions.alpha(0), Actions.fadeIn(fadeTime / 4)));

                        statusButton.setText(BTN_NAME_STATUS);
                        statusButton.setWidth(topRightButton.getWidth());
                        statusButton.setPosition(topRightButton.getX(), runButton.getY());
                        statusButton.addAction(Actions.sequence(Actions.delay(fadeTime / 2), Actions.alpha(0), Actions.fadeIn(fadeTime / 4)));

                        backButton.addAction(Actions.sequence(Actions.delay(fadeTime / 2), Actions.fadeOut(fadeTime / 2)));
                        backButton.addAction(Actions.sizeBy(0, -backButtonHeight, fadeTime));
                        backButton.addAction(Actions.sequence(Actions.delay(fadeTime * 0.8f), myActions.new setButtonText(backButton, "")));

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

                        enemy1Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        enemy2Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        enemy3Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        enemy4Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));
                        enemy5Name.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeTime)));

                        middleTreeTextArea.setHeight(0);
                        middleTreeTextArea.addAction(Actions.fadeOut(fadeTime));

                        inventoryTree.setVisible(false);

                        inventoryScrollPaneTree.setHeight(0);
                        spellPowerScrollPaneTree.setHeight(0);
                        //middleScrollPaneList.setHeight(0);

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
                case SPELLS_POWER:
                    if (currentScreenState == ScreenState.FINAL) {
                        spellPowerTree.setTouchable(Touchable.enabled);
                        spellPowerTree.setVisible(true);

                        middleTextAreaTable.addAction(Actions.fadeOut(0));
                        middleTextAreaTable.setVisible(true);
                        middleTextAreaTable.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));

                        spellPowerScrollPaneTree.addAction(Actions.sizeBy(0, middleTreeHeight - 4, fadeTime));

                        setCommonTransitionBackFromFinal(ScreenState.SPELLS_POWER);
                    }
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

    private void setCommonTransitionBackFromFinal(ScreenState state) {
        enemy1Name.addAction(Actions.fadeOut(fadeTime/2));
        enemy2Name.addAction(Actions.fadeOut(fadeTime/2));
        enemy3Name.addAction(Actions.fadeOut(fadeTime/2));
        enemy4Name.addAction(Actions.fadeOut(fadeTime/2));
        enemy5Name.addAction(Actions.fadeOut(fadeTime/2));

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

        if (state == ScreenState.INVENTORY)
            leftSummaryText.setText(selectedInventoryElement.summary);
        else if (state == ScreenState.SPELLS_POWER)
            leftSummaryText.setText(selectedSpellPowerElement.summary);

        leftSummaryText.addAction(Actions.sequence(Actions.delay(fadeTime/2), Actions.alpha(0), Actions.fadeIn(fadeTime/2)));

        backButton.addAction(Actions.sequence(Actions.sizeBy(0, -backButtonHeight + 2, fadeTime), Actions.fadeOut(0)));
        backButton.addAction(Actions.sequence(Actions.delay(fadeTime * 0.8f), myActions.new setButtonText(backButton, "")));

        selectedItemBanner.addAction(Actions.sizeBy(0, -selectedItemBannerHeight, fadeTime));
        selectedItemBanner.addAction(Actions.moveBy(0, selectedItemBannerHeight, fadeTime));

        if (statusButton.getText().toString().equals(BTN_NAME_OK)) {
            statusButton.addAction(Actions.sequence(Actions.sizeBy(0, -backButtonHeight + 2, fadeTime), Actions.fadeOut(0)));
            statusButton.addAction(Actions.sequence(Actions.delay(fadeTime * 0.8f),
                    myActions.new setButtonText(statusButton, ""),
                    myActions.new setButtonText(statusButton, BTN_NAME_STATUS),
                    Actions.delay(fadeTime * 0.8f),
                    Actions.sizeBy(0, backButtonHeight - 2, 0),
                    myActions.new setButtonWidth(backButton, middleAreaWidth)));
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
                    case SPELLS_POWER:
                        if (!middleStatsTextArea.getText().equals(INVENTORY_EMPTY) &&
                                !middleStatsTextArea.getText().equals(SELECT_AN_ITEM) &&
                                !middleStatsTextArea.getText().equals(SELECT_AN_ABILITY) &&
                                !middleStatsTextArea.getText().equals(ABILITIES_EMPTY)) {
                            setHUDNewState(ScreenState.FINAL);
                        }
                        break;
                }
                break;
            case B_BUTTON_RELEASED:
                switch(currentScreenState) {
                    case INVENTORY:
                    case SPELLS_POWER:
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

    public InventoryElement getSelectedInventoryElement() { return selectedInventoryElement; }

    public void addTransitionToScreen(){
        _transitionActor.setVisible(true);
        _stage.addAction(
                Actions.sequence(
                        Actions.addAction(ScreenTransitionAction.transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 1), _transitionActor)));
    }

    @Override
    public void onNotify(int value, StatusEvent event) {
    }

    @Override
    public void onNotify(Entity entity, int value, StatusEvent event) {
        switch (event) {
            case UPDATED_HP:
                UpdateStats(entity);
                break;
            case UPDATED_MP:
                UpdateStats(entity);
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

        if (battleTextArea.isReady())
            battleTextArea.update();

        _stage.act(delta);
        _stage.draw();
    }

    private void setLabelFontColor(Label label, Color color) {
        Label.LabelStyle ls;
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/9_px.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = Utility.myFontVerySmallSize;
        parameter.shadowColor = Color.LIGHT_GRAY;
        parameter.color = color;

        BitmapFont font = generator.generateFont(parameter);
        ls = new Label.LabelStyle(font, parameter.color);
        label.setStyle(ls);
    }

    Timer.Task resetControlsTimer() {
        return new Timer.Task() {
            @Override
            public void run() {
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
                dummyTextArea.addAction(Actions.moveBy(0, -menuItemHeight, fadeTime));
                // middleStatsTextArea is handled elsewhere
                //middleStatsTextArea.setHeight(menuItemHeight * 2 - 2);
                //middleStatsTextArea.setPosition(middleStatsTextArea.getX(),2 - menuItemHeight);

                // reset other variables
                party1Name.setText("");
                party2Name.setText("");
                party3Name.setText("");
                party4Name.setText("");
                party5Name.setText("");

                enemy1Name.setText("");
                enemy2Name.setText("");
                enemy3Name.setText("");
                enemy4Name.setText("");
                enemy5Name.setText("");

                // reset color of monster names
                setLabelFontColor(enemy1Name, Color.DARK_GRAY);
                setLabelFontColor(enemy2Name, Color.DARK_GRAY);
                setLabelFontColor(enemy3Name, Color.DARK_GRAY);
                setLabelFontColor(enemy4Name, Color.DARK_GRAY);
                setLabelFontColor(enemy5Name, Color.DARK_GRAY);

                //for (int i = 1; i <= numberOfPartyMembers; i++) {
                //    battleScreen.removePartyMemberByIndex(i);
                //}

                battleScreen.removeAllPartyMembers();

                //for (int i = 1; i <= numberOfOpponents; i++) {
                //    battleScreen.removeOpponentByIndex(i);
                //}

                battleScreen.removeAllOpponents();

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

                dimmedScreen.setVisible(false);

                battleTextArea.setTouchable(Touchable.enabled);
            }
        };
    }

    public void fadeOutHUD(float duration) {
        leftTextArea.addAction(Actions.fadeOut(duration));
        leftNameTable.addAction(Actions.fadeOut(duration));
        battleTextArea.addAction(Actions.fadeOut(duration));
        rightTextArea.addAction(Actions.fadeOut(duration));
        rightTable.addAction(Actions.fadeOut(duration));

        topLeftButton.addAction(Actions.fadeOut(duration));
        topRightButton.addAction(Actions.fadeOut(duration));
        runButton.addAction(Actions.fadeOut(duration));
        statusButton.addAction(Actions.fadeOut(duration));
    }

    public void fadeInHUD(float duration) {
        leftTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(duration)));
        leftNameTable.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(duration)));
        battleTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(duration)));
        rightTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(duration)));
        rightTable.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(duration)));
/*
        topLeftButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(duration)));
        topRightButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(duration)));
        runButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(duration)));
        statusButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(duration)));*/
    }

    @Override
    public void resize(int width, int height) {
        // This is the function that is called when the battle screen is opened.
        _stage.getViewport().update(width, height, true);

        if (game.battleState.peekNextTurnCharacter().getBattleEntityType() == Entity.BattleEntityType.PARTY) {
            showMainScreen(true);
        }
        else {
            // enemy
            hideMainScreen();
            dummyTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
            turnInProgress = true;
        }

        leftTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
        leftNameTable.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
        rightTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
        rightTable.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));

        // need to complete above actions here, otherwise they don't show up
        completeAllActions();

        // make controls rise from the bottom of the screen when it is first displayed
        leftTextArea.addAction(Actions.moveBy(0, menuItemHeight, fadeTime));
        leftNameTable.addAction(Actions.moveBy(0, menuItemHeight, fadeTime));
        topLeftButton.addAction(Actions.moveBy(0, menuItemHeight, fadeTime));
        topRightButton.addAction(Actions.moveBy(0, menuItemHeight, fadeTime));
        runButton.addAction(Actions.moveBy(0, menuItemHeight, fadeTime));
        statusButton.addAction(Actions.moveBy(0, menuItemHeight, fadeTime));
        rightTextArea.addAction(Actions.moveBy(0, menuItemHeight, fadeTime));
        rightTable.addAction(Actions.moveBy(0, menuItemHeight, fadeTime));
        middleStatsTextArea.addAction(Actions.moveBy(0, menuItemHeight, fadeTime));
        dummyTextArea.addAction(Actions.moveBy(0, menuItemHeight, fadeTime));

        // Set names visible in case they were set invisible during last battle.
        // It's okay to do for all since if they shouldn't be shown, they will be set to "".
        party1Name.setVisible(true);
        party2Name.setVisible(true);
        party3Name.setVisible(true);
        party4Name.setVisible(true);
        party5Name.setVisible(true);
        enemy1Name.setVisible(true);
        enemy2Name.setVisible(true);
        enemy3Name.setVisible(true);
        enemy4Name.setVisible(true);
        enemy5Name.setVisible(true);

        battleWon = false;
        battleLost = false;
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

    private void updateStatusBar(int power, int power_max, Image statusBar, Label stats) {
        stats.setText(power + "/" + power_max);
        statusBar.setWidth((float)power/(float)power_max * barWidth);
    }

    @Override
    public void onNotify(Entity entity, BattleEvent event) {
        Gdx.app.log(TAG, String.format("BattleEvent received: %s", event.toString()));
        switch(event){
            //todo: special audio for battle events
            case PARTY_MEMBER_ADDED:
                Gdx.app.log(TAG, "Party member added: " + entity.getEntityConfig().getDisplayName());
                numberOfPartyMembers++;
                battleScreen.addPartyMember(entity, entity.getBattlePosition());

                switch (entity.getBattlePosition()) {
                    case 1:
                        party1Name.setText(entity.getEntityConfig().getDisplayName());

                        updateStatusBar(game.statusUI.getHPValue(entity), game.statusUI.getHPMaxValue(entity), hpBar1, hp1Stats);
                        updateStatusBar(game.statusUI.getMPValue(entity), game.statusUI.getMPMaxValue(entity), mpBar1, mp1Stats);

                        groupHp1.setVisible(true);
                        groupMp1.setVisible(true);
                        break;
                    case 2:
                        party2Name.setText(entity.getEntityConfig().getDisplayName());

                        updateStatusBar(game.statusUI.getHPValue(entity), game.statusUI.getHPMaxValue(entity), hpBar2, hp2Stats);
                        updateStatusBar(game.statusUI.getMPValue(entity), game.statusUI.getMPMaxValue(entity), mpBar2, mp2Stats);

                        groupHp2.setVisible(true);
                        groupMp2.setVisible(true);
                        break;
                    case 3:
                        party3Name.setText(entity.getEntityConfig().getDisplayName());

                        updateStatusBar(game.statusUI.getHPValue(entity), game.statusUI.getHPMaxValue(entity), hpBar3, hp3Stats);
                        updateStatusBar(game.statusUI.getMPValue(entity), game.statusUI.getMPMaxValue(entity), mpBar3, mp3Stats);

                        groupHp3.setVisible(true);
                        groupMp3.setVisible(true);
                        break;
                    case 4:
                        party4Name.setText(entity.getEntityConfig().getDisplayName());

                        updateStatusBar(game.statusUI.getHPValue(entity), game.statusUI.getHPMaxValue(entity), hpBar4, hp4Stats);
                        updateStatusBar(game.statusUI.getMPValue(entity), game.statusUI.getMPMaxValue(entity), mpBar4, mp4Stats);

                        groupHp4.setVisible(true);
                        groupMp4.setVisible(true);
                        break;
                    case 5:
                        party5Name.setText(entity.getEntityConfig().getDisplayName());

                        updateStatusBar(game.statusUI.getHPValue(entity), game.statusUI.getHPMaxValue(entity), hpBar5, hp5Stats);
                        updateStatusBar(game.statusUI.getMPValue(entity), game.statusUI.getMPMaxValue(entity), mpBar5, mp5Stats);

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
                        enemy1Name.setText(entity.getEntityConfig().getDisplayName());
                        break;
                    case 2:
                        enemy2Name.setText(entity.getEntityConfig().getDisplayName());
                        break;
                    case 3:
                        enemy3Name.setText(entity.getEntityConfig().getDisplayName());
                        break;
                    case 4:
                        enemy4Name.setText(entity.getEntityConfig().getDisplayName());
                        break;
                    case 5:
                        enemy5Name.setText(entity.getEntityConfig().getDisplayName());
                        break;
                }

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
                        backButton.addAction(Actions.sizeBy(-menuItemWidth, 0, fadeTime));
                        backButton.setWidth(backButton.getWidth() + 2);

                        statusButton.setPosition(rightTextArea.getX() + 2, 2);
                        statusButton.setWidth(0);
                        statusButton.setText(BTN_NAME_OK);
                        statusButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
                        statusButton.addAction(Actions.sizeBy(menuItemWidth, 0, fadeTime));
                        statusButton.addAction(Actions.moveBy(-menuItemWidth, 0, fadeTime));
                    }

                    selectedCharacter = entity.getEntityConfig().getDisplayName();
                    Gdx.app.log(TAG, selectedCharacter + " selected");
                }

                break;
            case OPPONENT_DEFEATED:
                Gdx.app.log(TAG, "entity " + entity.getEntityConfig().getDisplayName() + " defeated!!");

                if (enemy1Name.getText().toString().equals(entity.getEntityConfig().getDisplayName())) {
                    enemy1Name.setVisible(false);
                }
                else if (enemy2Name.getText().toString().equals(entity.getEntityConfig().getDisplayName())) {
                    enemy2Name.setVisible(false);
                }
                else if (enemy3Name.getText().toString().equals(entity.getEntityConfig().getDisplayName())) {
                    enemy3Name.setVisible(false);
                }
                else if (enemy4Name.getText().toString().equals(entity.getEntityConfig().getDisplayName())) {
                    enemy4Name.setVisible(false);
                }
                else if (enemy5Name.getText().toString().equals(entity.getEntityConfig().getDisplayName())){
                    enemy5Name.setVisible(false);
                }

                xpReward += Integer.parseInt(entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.XP_REWARD.toString().toString()));
                dibsReward += Integer.parseInt(entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.DIBS_REWARD.toString().toString()));

                Array<EntityConfig.ItemReward> items = entity.getEntityConfig().getRewardItems();

                if (items != null) {
                    for (EntityConfig.ItemReward item : items) {
                        // check probability of getting reward item
                        int randomVal = MathUtils.random(1, 100);
                        if (item.probability > randomVal) {
                            boolean inList = false;
                            for (RewardItem rewardItem : rewardItems) {
                                if (rewardItem.itemID.equals(item.itemID)) {
                                    rewardItem.quantity++;
                                    inList = true;
                                    break;
                                }
                            }

                            if (!inList) {
                                RewardItem newItem = new RewardItem();
                                newItem.itemID = item.itemID;
                                newItem.quantity = 1;
                                rewardItems.add(newItem);
                            }
                        }
                    }
                }

                selectedCharacter = null;
                break;
            case CRITICAL_HIT:
                turnInProgress = false;
                break;
            case WEAK_HIT:
                turnInProgress = false;
                break;
            case BATTLE_WON:
                battleWon = true;
                break;
            case BATTLE_LOST:
                battleLost = true;
                xpReward = 0;
                dibsReward = 0;
                break;
            case PLAYER_USED_MAGIC:
                /*
                float x = _currentImagePosition.x + (_enemyWidth/2);
                float y = _currentImagePosition.y + (_enemyHeight/2);
                _effects.add(ParticleEffectFactory.getParticleEffect(ParticleEffectFactory.ParticleEffectType.WAND_ATTACK, x,y));
                */
                break;
            case ANNIMATION_COMPLETE:
                switch (BattleScreen.getAnimationState()) {
                    case BATTLE:
                        turnInProgress = false;
                        break;
                    case ESCAPED:
                        if( !resetControlsTimer().isScheduled() ){
                            Timer.schedule(resetControlsTimer(), 0);
                        }
                        turnInProgress = false;
                        xpReward = 0;
                        dibsReward = 0;
                        break;
                    case FAILED_ESCAPE:
                        battleTextArea.populateText("Failed to run!");
                        turnInProgress = false;
                        break;
                }

                break;
            case PLAYER_ESCAPED:
                selectedCharacter = null;
                break;
            case PLAYER_FAILED_TO_ESCAPE:
                turnInProgress = true;
                break;
            case CHARACTER_TURN_CHANGED:
                Entity.BattleEntityType type = entity.getBattleEntityType();
                if (type == Entity.BattleEntityType.ENEMY)  {
                    // disable player input and kick off enemy turn
                    disableButtons();
                    turnInProgress = true;

                    hideMainScreen();

                    battleTextArea.show();
                    battleTextArea.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));

                    // delay for one frame here to fix issue with blip when battleTextArea is set to visible
                    dummyTextArea.addAction(Actions.sequence(Actions.delay(currentDelta), Actions.fadeOut(currentDelta)));
                }
                else {
                    // load character's spells/powers
                    spellPowerList.clear();

                    String propertyKey = entity.getEntityConfig().getEntityID().toString().toUpperCase() + EntityConfig.EntityProperties.SPELL_LIST.toString();
                    Array<SpellPowerElement.ElementID> elementArray = ProfileManager.getInstance().getProperty(propertyKey, Array.class);

                    if (elementArray != null) {
                        for (SpellPowerElement.ElementID id : elementArray) {
                            SpellPowerElement element = SpellPowerElementFactory.getInstance().getSpellPowerElement(id);
                            spellPowerList.add(element);
                        }
                    }
                }
                selectedCharacter = null;
                break;
            default:
                break;
        }
    }

    @Override
    public void onNotify(Entity sourceEntity, Entity destinationEntity, BattleEventWithMessage event, String message) {
        Gdx.app.log(TAG, event.toString() + " BattleEventWithMessage received, message = " + message);

        switch(event){
            case OPPONENT_ATTACKS:
                middleStatsTextArea.addAction(Actions.fadeOut(fadeTime / 2));
                break;
            case OPPONENT_TURN_DONE:
                // go back to Main screen
                screenStack.clear();
                screenStack.push(ScreenState.MAIN);

                UpdateStats(destinationEntity);

                battleTextArea.populateText(message);
                dummyTextArea.addAction(Actions.sequence(Actions.delay(0), Actions.fadeOut(0)));
                selectedCharacter = null;

                if (game.battleState.peekNextTurnCharacter().getBattleEntityType() == Entity.BattleEntityType.PARTY) {
                    enableButtons();
                }

                //turnInProgress = false;
                break;
            case OPPONENT_HIT_DAMAGE:
                notify(AudioObserver.AudioCommand.SOUND_PLAY_ONCE, AudioObserver.AudioTypeEvent.SOUND_PLAYER_ATTACK);
                notify(AudioObserver.AudioCommand.SOUND_PLAY_ONCE, AudioObserver.AudioTypeEvent.SOUND_CREATURE_PAIN);

                int HP = game.statusUI.getHPValue(destinationEntity);
                int HP_MAX = game.statusUI.getHPMaxValue(destinationEntity);

                // set color of enemy name based on current HP
                Color color;
                if (HP < 0.3f * (float)HP_MAX) {
                    color = new Color (1, 0, 0, 1); // red
                }
                else if (HP < 0.6f * (float)HP_MAX) {
                    color = new Color(1, 0.7f, 0, 1); // orange
                }
                else {
                    color = Color.DARK_GRAY;
                }

                if (enemy1Name.getText().toString().equals(destinationEntity.getEntityConfig().getDisplayName()))
                    setLabelFontColor(enemy1Name, color);
                else if (enemy2Name.getText().toString().equals(destinationEntity.getEntityConfig().getDisplayName()))
                    setLabelFontColor(enemy2Name, color);
                else if (enemy3Name.getText().toString().equals(destinationEntity.getEntityConfig().getDisplayName()))
                    setLabelFontColor(enemy3Name, color);
                else if (enemy4Name.getText().toString().equals(destinationEntity.getEntityConfig().getDisplayName()))
                    setLabelFontColor(enemy4Name, color);
                else if (enemy5Name.getText().toString().equals(destinationEntity.getEntityConfig().getDisplayName()))
                    setLabelFontColor(enemy5Name, color);

                //_battleShakeCam.startShaking();
                selectedCharacter = null;
                break;
            case PLAYER_TURN_DONE:
            case PLAYER_APPLIED_INVENTORY:
            case PLAYER_APPLIED_SPELL_POWER:
            case MISS_HIT:
                // go back to Main screen and enable buttons
                screenStack.clear();
                screenStack.push(ScreenState.MAIN);

                UpdateStats(destinationEntity);

                battleTextArea.populateText(message);
                battleTextArea.show();

                selectedCharacter = null;

                if (event.equals(PLAYER_APPLIED_INVENTORY) ||
                        event.equals(PLAYER_APPLIED_SPELL_POWER) ||
                        event.equals(MISS_HIT)) {
                    turnInProgress = false;
                }

                break;
            case ATTACK_BLOCKED:
                // kick off the delayed results
                if (!getDisplayResultsTimer(message).isScheduled()) {
                    Timer.schedule(getDisplayResultsTimer(message), 1.75f);
                }
                break;
        }
    }

    private Timer.Task getDisplayResultsTimer(final String message) {
        return new Timer.Task() {
            @Override
            public void run() {
                battleTextArea.populateText(message);
                battleTextArea.show();

                // reset screen stack
                screenStack.clear();
                screenStack.push(ScreenState.MAIN);

                selectedCharacter = null;
            }
        };
    }

    @Override
    public void onNotify(Entity entity, InventoryElement.Effect effect) {

    }

    private void UpdateStats(Entity destinationEntity) {
        // update HUD graphic stats for destination entity
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
        else {
            // must be enemy
            return;
        }

        if (hpBar != null && hpStats != null && mpBar != null & mpStats != null) {
            updateStatusBar(game.statusUI.getHPValue(destinationEntity), game.statusUI.getHPMaxValue(destinationEntity), hpBar, hpStats);
            updateStatusBar(game.statusUI.getMPValue(destinationEntity), game.statusUI.getMPMaxValue(destinationEntity), mpBar, mpStats);
        }
    }

    private void hideMainScreen() {
        topLeftButton.addAction(Actions.fadeOut(0));
        topRightButton.addAction(Actions.fadeOut(0));
        runButton.addAction(Actions.fadeOut(0));
        statusButton.addAction(Actions.fadeOut(0));
    }

    private void showMainScreen(boolean immediate) {
        Gdx.app.log(TAG, "showMainScreen");
        float delay = fadeTime;

        if (immediate) {
            delay = 0;
        }

        topLeftButton.setVisible(true);
        topRightButton.setVisible(true);
        runButton.setVisible(true);
        statusButton.setVisible(true);
        dummyTextArea.setVisible(true);

        enableButtons();

        topLeftButton.setText(BTN_NAME_INVENTORY);
        topRightButton.setText(BTN_NAME_FIGHT);

        // Not sure why, but setting fadeIn for buttons here to zero delay is necessary,
        // otherwise transition into BattleHUD screen first shows buttons as invisible
        // and trying to cross-fade with middleStatsTextArea was ugly.
        // Fade-ins from other BattleHUD screens still work though!
        topLeftButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
        runButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
        topRightButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));
        statusButton.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0)));

        dummyTextArea.addAction(Actions.fadeOut(delay * crossFadeOutFactor));

        // closeBattleTextAction calls battleTextArea.hide()
        // Add a little extra delay here to help with flashing issue
        float extraDelay = 0.125f;
        battleTextArea.addAction(Actions.fadeOut(delay * crossFadeOutFactor + extraDelay));
        battleTextArea.addAction(Actions.sequence(Actions.delay(delay * crossFadeOutFactor + extraDelay),
                myActions.new closeBattleTextAction(battleTextArea)));

        dummyButtonLeft.setVisible(false);
        dummyButtonLeft.setHeight(menuItemHeight + 2);
        dummyButtonLeft.setPosition(topLeftButton.getX(), 0);

        dummyButtonRight.setVisible(false);
        dummyButtonRight.setHeight(menuItemHeight + 2);
        dummyButtonRight.setPosition(dummyButtonLeft.getX() + dummyButtonLeft.getWidth() - 2, 0);

        middleStatsTextArea.setText("", true);
        middleStatsTextArea.addAction(Actions.fadeOut(delay * crossFadeOutFactor));
        middleStatsTextArea.addAction(Actions.sequence(
                Actions.delay(delay),
                myActions.new setTextFieldPositionAndSize(middleStatsTextArea,
                        middleStatsTextArea.getX(),
                        2,
                        middleStatsTextArea.getWidth(),
                        menuItemHeight * 2 - 2),
                        myActions.new setTextAreaVisible(middleStatsTextArea, false)));

        backButton.addAction(Actions.fadeOut(delay * crossFadeInFactor));
        backButton.addAction(Actions.sizeBy(0, -backButtonHeight - 3, delay)); // need -3 or else back button height wrong on final screen

        leftSummaryText.setText("");
    }

    private void updateBanner(String text) {
        // this function raises current banner, changes text, and then lowers banner again
        float bannerWidth = calculateBannerWidth(text);

        selectedItemBanner.addAction(Actions.sizeBy(0, -selectedItemBannerHeight, fadeTime/2));
        selectedItemBanner.addAction(Actions.moveBy(0, selectedItemBannerHeight, fadeTime/2));

        selectedItemBanner.addAction(Actions.sequence(Actions.delay(fadeTime/2),
                myActions.new setTextAreaText(selectedItemBanner, text),
                myActions.new setTextFieldPositionAndSize(selectedItemBanner,
                        (_stage.getWidth() - bannerWidth)/2,
                        selectedItemBanner.getY(),
                        bannerWidth,
                        selectedItemBannerHeight),
                Actions.sizeBy(0, selectedItemBannerHeight, fadeTime),
                Actions.moveBy(0, -selectedItemBannerHeight, fadeTime),
                Actions.delay(2),
                Actions.parallel(Actions.sizeBy(0, -selectedItemBannerHeight, fadeTime/2),
                        Actions.moveBy(0, selectedItemBannerHeight, fadeTime/2))));
    }

    private void displayBanner(String text, float duration) {
        // this function sets text and position of banner, lowers banner, delays for specified duration, and raises banner again
        float bannerWidth = calculateBannerWidth(text);
        selectedItemBanner.setText(text, true);
        selectedItemBanner.setWidth(bannerWidth);
        selectedItemBanner.setX((_stage.getWidth() - bannerWidth)/2);

        selectedItemBanner.addAction(Actions.sizeBy(0, selectedItemBannerHeight, fadeTime/2));
        selectedItemBanner.addAction(Actions.moveBy(0, -selectedItemBannerHeight, fadeTime/2));

        selectedItemBanner.addAction(Actions.sequence(Actions.delay(duration),
                                     Actions.parallel(Actions.sizeBy(0, -selectedItemBannerHeight, fadeTime/2),
                                                      Actions.moveBy(0, selectedItemBannerHeight, fadeTime/2))));
    }

    private String getInventoryDescription(String name, int quantity) {
        return String.format("%s (%d)", name, quantity);
    }

    private void clearSpellPowerTreeCategory(Tree.Node categoryNode, rootNode rNode) {
        Array<Tree.Node> nodeArray = categoryNode.getChildren();

        if (nodeArray != null && nodeArray.size != 0) {
            for (int i = nodeArray.size - 1; i >= 0; i--) {
                Tree.Node node = nodeArray.get(i);
                categoryNode.remove(node);
            }
        }

        spellPowerTree.remove(categoryNode); // remove from tree
        spellPowerRootNodeArray.removeValue(rNode, false); // remove from rootNode array
    }

    private void populateSpellPowerTree() {
        clearSpellPowerTreeCategory(WhiteMagicNode, whiteMagicRootNode);
        clearSpellPowerTreeCategory(BlackMagicNode, blackMagicRootNode);
        clearSpellPowerTreeCategory(PowersNode, powersRootNode);

        for (SpellPowerElement element : spellPowerList) {
            Array<Tree.Node> nodeArray = null;
            Tree.Node categoryNode = null;
            String categoryName = "";

            switch (element.category) {
                case White:
                    categoryNode = WhiteMagicNode;
                    categoryName = WHITE_MAGIC;
                    break;
                case Black:
                    categoryNode = BlackMagicNode;
                    categoryName = BLACK_MAGIC;
                    break;
                case Power:
                    categoryNode = PowersNode;
                    categoryName = POWERS;
                    break;
            }

            if (categoryNode == null) {
                continue;
            }

            nodeArray = categoryNode.getChildren();

            if (nodeArray == null || nodeArray.size == 0) {
                // add root node
                spellPowerRootNodeArray.add(new rootNode(categoryName, false));
                spellPowerTree.add(categoryNode);
            }

            // add node
            SpellPowerNode node = new SpellPowerNode(new TextButton(element.name, Utility.ELMOUR_UI_SKIN, "tree_node"), element.id);
            node.setObject(element);
            categoryNode.add(node);
        }
    }

    @Override
    public void onNotify(PartyInventoryItem partyInventoryItem, PartyInventoryEvent event) {
        //Gdx.app.log(TAG, event.toString() + " " + partyInventoryItem.getElement().id.toString() + " (" + partyInventoryItem.getQuantity() + ")");

        InventoryElement element = partyInventoryItem.getElement();
        Array<Tree.Node> nodeArray;
        rootNode rNode = null;
        InventoryNode inventoryNode = null;
        Tree.Node categoryNode = null;
        String categoryName = "";

        switch(element.category) {
            case Potion:
                categoryNode = PotionsNode;
                categoryName = POTIONS;
                rNode = potionsRootNode;
                break;
            case Consumables:
                categoryNode = ConsumablesNode;
                categoryName = CONSUMABLES;
                rNode = consumablesRootNode;
                break;
            case Throwing:
                categoryNode = ThrowingNode;
                categoryName = THROWING;
                rNode = throwingRootNode;
                break;
        }

        if (categoryNode == null) {
            // Only show Potion, Consumables, and Throwing Items
            return;
        }

        nodeArray = categoryNode.getChildren();

        if (nodeArray != null && nodeArray.size != 0) {
            // find node in tree
            for (Tree.Node nodeIterator : nodeArray) {
                InventoryNode n = (InventoryNode) nodeIterator;
                if (partyInventoryItem.getElement().id.equals(n.elementID)) {
                    inventoryNode = n;
                    break;
                }
            }
        }
        else {
            // add root node
            inventoryRootNodeArray.add(new rootNode(categoryName, false));
            inventoryTree.add(categoryNode);
        }

        PartyInventoryItem item = PartyInventory.getInstance().getItem(element);
        String text = getInventoryDescription(element.name, item.getQuantity());

        switch (event) {
            case INVENTORY_ADDED:
                if (inventoryNode != null) {
                    // update node
                    TextButton label = (TextButton)inventoryNode.getActor();
                    label.setText(text);
                }
                else {
                    // add node
                    InventoryNode node = new InventoryNode(new TextButton(text, Utility.ELMOUR_UI_SKIN, "tree_node"), element.id);
                    node.setObject(element);
                    categoryNode.add(node);
                }
                break;
            case INVENTORY_REMOVED:
                if (inventoryNode != null) {
                    // update node
                    TextButton label = (TextButton)inventoryNode.getActor();
                    label.setText(text);

                    if (partyInventoryItem.getQuantity() == 0) {
                        categoryNode.remove(inventoryNode);
                    }

                    // if no nodes left in category, remove root node
                    nodeArray = categoryNode.getChildren();
                    if (nodeArray.size == 0) {
                        categoryNode.remove();  // remove from tree
                        inventoryRootNodeArray.removeValue(rNode, false); // remove from rootNode array
                    }
                }
                break;
        }
    }

    @Override
    public void onNotify(PartyInventoryItem item1, PartyInventoryItem item2, PartyInventoryEvent event) {
        switch (event) {
            case INVENTORY_SWAP:
                Tree.Node categoryNode = null;
                switch(item1.getElement().category) {
                    case Potion:
                        categoryNode = PotionsNode;
                        break;
                    case Consumables:
                        categoryNode = ConsumablesNode;
                        break;
                    case Throwing:
                        categoryNode = ThrowingNode;
                        break;
                }

                if (categoryNode != null) {
                    categoryNode.getChildren().swap(categoryNode.getChildren().indexOf(categoryNode.findNode(item1), true),
                                                    categoryNode.getChildren().indexOf(categoryNode.findNode(item2), true));
                }

                break;
        }
    }
}
