package com.smoftware.elmour.UI;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.ElmourGame;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.EntityFactory;
import com.smoftware.elmour.InventoryElement;
import com.smoftware.elmour.KeyItem;
import com.smoftware.elmour.KeyItemFactory;
import com.smoftware.elmour.PartyInventory;
import com.smoftware.elmour.PartyInventoryItem;
import com.smoftware.elmour.PartyInventoryObserver;
import com.smoftware.elmour.Utility;
import com.smoftware.elmour.maps.Map;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.profile.ProfileObserver;

/**
 * Created by steve on 2/10/19.
 */

public class InventoryHUD implements Screen, InventoryHudSubject, PartyInventoryObserver, ProfileObserver {
    private static final String TAG = InventoryHUD.class.getSimpleName();

    enum ButtonState { EQUIPMENT, CONSUMABLES, KEY_ITEMS, EQUIP, NONE }
    enum ListType { WEAPON, ARMOR, POTION, CONSUMABLE, THROWING, QUEST, NON_QUEST }

    private ElmourGame game;
    private Stage stage;
    private Array<InventoryHudObserver> observers;
    private Array<Entity> partyEntityList;

    // message text
    private final String ITEMS_SWAPPED = "Items swapped!";
    private final String NOTHING_TO_SWAP = "Nothing to swap with!";

    // main button names
    private final String BTN_NAME_EQUIPMENT = "Equipment";
    private final String BTN_NAME_CONSUMABLES = "Consumables";
    private final String BTN_NAME_KEY_ITEMS = "Key Items";

    // action button names
    private final String BTN_NAME_USE = "Use";          // multi use
    private final String BTN_NAME_INSPECT = "Inspect";  // multi use
    private final String BTN_NAME_EQUIP = "Equip";      // multi use
    private final String BTN_NAME_OK = "OK";            // multi use
    private final String BTN_NAME_SWAP = "Swap";
    private final String BTN_NAME_BACK = "Back";
    private final String BTN_NAME_CLOSE = "Close";
    private final String BTN_NAME_CANCEL = "Cancel";

    // main buttons
    private TextButton equipmentButton;
    private TextButton consumablesButton;
    private TextButton keyItemsButton;

    // action buttons
    private TextButton actionButton;
    private TextButton swapButton;
    private TextButton backButton;

    // tables
    private Table equipmentListsTable;
    private Table weaponNameTable;
    private Table armorNameTable;
    private Table consumableListsTable;
    private Table keyItemsListsTable;
    private Table mainButtonTable;
    private Table actionButtonTable;

    // list groups
    private WidgetGroup groupWeapon;
    private WidgetGroup groupArmor;
    private WidgetGroup groupWeaponName;
    private WidgetGroup groupArmorName;
    private WidgetGroup groupNonQuest;
    private WidgetGroup groupQuest;
    private WidgetGroup groupPotions;
    private WidgetGroup groupConsumables;
    private WidgetGroup groupThrowing;
    private WidgetGroup groupEquipToName;

    // list components
    private TextButton labelWeapon;
    private MyTextButtonList<TextButton> weaponListView;
    private ScrollPane weaponScrollPaneList;
    private MyTextArea weaponBackground;

    private TextButton labelArmor;
    private MyTextButtonList<TextButton> armorListView;
    private ScrollPane armorScrollPaneList;
    private MyTextArea armorBackground;

    private MyTextArea weaponNameBackground;
    private MyTextArea armorNameBackground;

    private TextButton labelEquipTo;
    private MyTextButtonList<TextButton> equipToListView;
    private MyTextArea equipNameBackground;

    private TextButton labelNonQuest;
    private MyTextButtonList<TextButton> nonQuestListView;
    private ScrollPane nonQuestScrollPaneList;
    private MyTextArea nonQuestBackground;

    private TextButton labelQuest;
    private MyTextButtonList<TextButton> questListView;
    private ScrollPane questScrollPaneList;
    private MyTextArea questBackground;

    private TextButton labelPotions;
    private MyTextButtonList<TextButton> potionsListView;
    private ScrollPane potionsScrollPaneList;
    private MyTextArea potionsBackground;

    private TextButton labelConsumables;
    private MyTextButtonList<TextButton> consumablesListView;
    private ScrollPane consumablesScrollPaneList;
    private MyTextArea consumablesBackground;

    private TextButton labelThrowing;
    private MyTextButtonList<TextButton> throwingListView;
    private ScrollPane throwingScrollPaneList;
    private MyTextArea throwingBackground;

    // Description box
    private Table descTable;
    private ScrollPane descScrollPanel;
    private Label descText;
    private MyTextArea descBackground;

    // Graphic
    private WidgetGroup graphicGroup;
    private MyTextArea graphicBackground;
    private AnimatedImage backpack;

    // Persistence
    private TextButton lastSelectedConsumablesItem;
    private TextButton lastSelectedEquipmentItem;
    private TextButton lastSelectedKeyItem;
    private ButtonState buttonState;
    private ButtonState previousButtonState;
    private ListType lastSelectedEquipmentListType;
    private ListType lastSelectedConsumablesListType;
    private ListType lastSelectedKeyItemsListType;

    private boolean isSwapping = false;

    private float labelHeight = 30;
    private float bottomMargin = 6;
    private float nameTableHeight;

    public InventoryHUD(final ElmourGame game, Stage stage) {

        this.game = game;
        this.stage = stage;
        observers = new Array<>();
        partyEntityList = new Array<>();

        PartyInventory.getInstance().addObserver(this);
        ProfileManager.getInstance().addObserver(this);

        equipmentListsTable = new Table();
        consumableListsTable = new Table();
        keyItemsListsTable = new Table();
        mainButtonTable = new Table();
        actionButtonTable = new Table();
        weaponNameTable = new Table();
        armorNameTable = new Table();

        equipmentButton = new TextButton(BTN_NAME_EQUIPMENT, Utility.ELMOUR_UI_SKIN, "battle");
        consumablesButton = new TextButton(BTN_NAME_CONSUMABLES, Utility.ELMOUR_UI_SKIN, "battle");
        keyItemsButton = new TextButton(BTN_NAME_KEY_ITEMS, Utility.ELMOUR_UI_SKIN, "battle");

        actionButton = new TextButton(BTN_NAME_USE, Utility.ELMOUR_UI_SKIN, "battle");
        swapButton = new TextButton(BTN_NAME_SWAP, Utility.ELMOUR_UI_SKIN, "battle");
        backButton = new TextButton(BTN_NAME_CLOSE, Utility.ELMOUR_UI_SKIN, "battle");

        float buttonHeight = stage.getHeight() / 7.5f;
        float buttonWidth = (int)stage.getWidth() / 5;
        float leftMargin = (stage.getWidth() - (buttonWidth * 5) + 6)/2;

        mainButtonTable.row().width(stage.getWidth()).height(buttonHeight);
        mainButtonTable.add(equipmentButton).pad(-1).width(buttonWidth);
        mainButtonTable.add(consumablesButton).pad(-1).width(buttonWidth);
        mainButtonTable.add(keyItemsButton).pad(-1).width(buttonWidth);
        mainButtonTable.pack();
        mainButtonTable.setPosition(leftMargin, bottomMargin);

        float listWidth = mainButtonTable.getWidth()/2 + 2;
        float listHeight = (stage.getHeight() - mainButtonTable.getHeight() - bottomMargin);
        float listTopPadding = 6;

        //
        // EQUIPMENT
        //
        labelWeapon = new TextButton("Weapon", Utility.ELMOUR_UI_SKIN, "battle");
        labelWeapon.setTouchable(Touchable.disabled);
        weaponListView = new MyTextButtonList<>(Utility.ELMOUR_UI_SKIN);
        weaponScrollPaneList = new ScrollPane(weaponListView);
        weaponBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        weaponBackground.setTouchable(Touchable.disabled);

        labelArmor = new TextButton("Armor", Utility.ELMOUR_UI_SKIN, "battle");
        labelArmor.setTouchable(Touchable.disabled);
        armorListView = new MyTextButtonList<>(Utility.ELMOUR_UI_SKIN);
        armorScrollPaneList = new ScrollPane(armorListView);
        armorBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        armorBackground.setTouchable(Touchable.disabled);

        weaponNameBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        armorNameBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        equipNameBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        weaponNameBackground.setTouchable(Touchable.disabled);
        armorNameBackground.setTouchable(Touchable.disabled);
        equipNameBackground.setTouchable(Touchable.disabled);

        labelEquipTo = new TextButton("Equip To", Utility.ELMOUR_UI_SKIN, "battle");
        labelEquipTo.setTouchable(Touchable.disabled);
        equipToListView = new MyTextButtonList<>(Utility.ELMOUR_UI_SKIN);

        groupWeapon = new WidgetGroup();
        groupArmor = new WidgetGroup();
        groupWeaponName = new WidgetGroup();
        groupArmorName = new WidgetGroup();
        groupEquipToName = new WidgetGroup();

        nameTableHeight = 2 * buttonHeight - 2;

        // Note: the -2 prevents highlight of selected item from crossing over the borders
        // also need to set position then +2
        weaponBackground.setSize(listWidth, listHeight - labelHeight - nameTableHeight + 2);
        weaponScrollPaneList.setSize(listWidth - 2, listHeight - listTopPadding - labelHeight - nameTableHeight - 2);
        weaponScrollPaneList.setX(2);
        weaponScrollPaneList.setY(2);
        armorBackground.setSize(listWidth, listHeight - labelHeight - nameTableHeight + 2);
        armorScrollPaneList.setSize(listWidth - 4, listHeight - listTopPadding - labelHeight - nameTableHeight - 2);
        armorScrollPaneList.setX(2);
        armorScrollPaneList.setY(2);

        weaponNameBackground.setSize(listWidth, nameTableHeight);
        armorNameBackground.setSize(listWidth, nameTableHeight);

        groupWeapon.addActor(weaponBackground);
        groupWeapon.addActor(weaponScrollPaneList);
        groupArmor.addActor(armorBackground);
        groupArmor.addActor(armorScrollPaneList);
        groupWeaponName.addActor(weaponNameBackground);
        groupWeaponName.addActor(weaponNameTable);
        groupArmorName.addActor(armorNameBackground);
        groupArmorName.addActor(armorNameTable);
        groupEquipToName.addActor(equipNameBackground);
        groupEquipToName.addActor(equipToListView);

        // Note: padding -1 is so boxes can be overlapping to keep the border to 2 pixels
        equipmentListsTable.row().width(stage.getWidth()).height(labelHeight - 2);
        equipmentListsTable.add(labelWeapon).pad(-1).width(listWidth);
        equipmentListsTable.add(labelArmor).pad(-1).width(listWidth);

        equipmentListsTable.row().width(stage.getWidth()).height(stage.getHeight() - bottomMargin - labelHeight - nameTableHeight - mainButtonTable.getHeight() + 2);
        equipmentListsTable.add(groupWeapon).pad(-1).width(listWidth);
        equipmentListsTable.add(groupArmor).pad(-1).width(listWidth);

        equipmentListsTable.row().width(stage.getWidth()).height(nameTableHeight);
        equipmentListsTable.add(groupWeaponName).pad(-1).width(listWidth);
        equipmentListsTable.add(groupArmorName).pad(-1).width(listWidth);
        equipmentListsTable.pack();
        equipmentListsTable.setPosition(leftMargin, bottomMargin + mainButtonTable.getHeight());

        //
        // CONSUMABLES
        //
        labelPotions = new TextButton("Potions", Utility.ELMOUR_UI_SKIN, "battle");
        labelPotions.setTouchable(Touchable.disabled);
        potionsListView = new MyTextButtonList<>(Utility.ELMOUR_UI_SKIN);
        potionsScrollPaneList = new ScrollPane(potionsListView);
        potionsBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        potionsBackground.setTouchable(Touchable.disabled);

        labelConsumables = new TextButton("Consumables", Utility.ELMOUR_UI_SKIN, "battle");
        labelConsumables.setTouchable(Touchable.disabled);
        consumablesListView = new MyTextButtonList<>(Utility.ELMOUR_UI_SKIN);
        consumablesScrollPaneList = new ScrollPane(consumablesListView);
        consumablesBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        consumablesBackground.setTouchable(Touchable.disabled);

        labelThrowing = new TextButton("Throwing", Utility.ELMOUR_UI_SKIN, "battle");
        labelThrowing.setTouchable(Touchable.disabled);
        throwingListView = new MyTextButtonList<>(Utility.ELMOUR_UI_SKIN);
        throwingScrollPaneList = new ScrollPane(throwingListView);
        throwingBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        throwingBackground.setTouchable(Touchable.disabled);

        groupPotions = new WidgetGroup();
        groupConsumables = new WidgetGroup();
        groupThrowing = new WidgetGroup();

        listWidth = buttonWidth;

        potionsBackground.setSize(listWidth, listHeight - labelHeight);
        potionsScrollPaneList.setSize(listWidth - 2, listHeight - listTopPadding - labelHeight - 2);
        potionsScrollPaneList.setX(2);
        potionsScrollPaneList.setY(2);
        consumablesBackground.setSize(listWidth, listHeight - labelHeight);
        consumablesScrollPaneList.setSize(listWidth - 2, listHeight - listTopPadding - labelHeight - 2);
        consumablesScrollPaneList.setX(2);
        consumablesScrollPaneList.setY(2);
        throwingBackground.setSize(listWidth, listHeight - labelHeight);
        throwingScrollPaneList.setSize(listWidth - 4, listHeight - listTopPadding - labelHeight - 2);
        throwingScrollPaneList.setX(2);
        throwingScrollPaneList.setY(2);

        groupPotions.addActor(potionsBackground);
        groupPotions.addActor(potionsScrollPaneList);
        groupConsumables.addActor(consumablesBackground);
        groupConsumables.addActor(consumablesScrollPaneList);
        groupThrowing.addActor(throwingBackground);
        groupThrowing.addActor(throwingScrollPaneList);

        consumableListsTable.row().width(stage.getWidth()).height(labelHeight - 2);
        consumableListsTable.add(labelPotions).pad(-1).width(buttonWidth);
        consumableListsTable.add(labelConsumables).pad(-1).width(buttonWidth);
        consumableListsTable.add(labelThrowing).pad(-1).width(buttonWidth);

        consumableListsTable.row().width(stage.getWidth()).height(stage.getHeight() - bottomMargin - labelHeight - mainButtonTable.getHeight());
        consumableListsTable.add(groupPotions).pad(-1).width(buttonWidth);
        consumableListsTable.add(groupConsumables).pad(-1).width(buttonWidth);
        consumableListsTable.add(groupThrowing).pad(-1).width(buttonWidth);
        consumableListsTable.pack();
        consumableListsTable.setPosition(leftMargin, bottomMargin + mainButtonTable.getHeight());

        //
        // KEY ITEMS
        //
        labelNonQuest = new TextButton("Non-Quest", Utility.ELMOUR_UI_SKIN, "battle");
        labelNonQuest.setTouchable(Touchable.disabled);
        nonQuestListView = new MyTextButtonList<>(Utility.ELMOUR_UI_SKIN);
        nonQuestScrollPaneList = new ScrollPane(nonQuestListView);
        nonQuestBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        nonQuestBackground.setTouchable(Touchable.disabled);

        labelQuest = new TextButton("Quest", Utility.ELMOUR_UI_SKIN, "battle");
        labelQuest.setTouchable(Touchable.disabled);
        questListView = new MyTextButtonList<>(Utility.ELMOUR_UI_SKIN);
        questScrollPaneList = new ScrollPane(questListView);
        questBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        questBackground.setTouchable(Touchable.disabled);

        groupNonQuest = new WidgetGroup();
        groupQuest = new WidgetGroup();

        listWidth = mainButtonTable.getWidth()/2 + 2;

        nonQuestBackground.setSize(listWidth, listHeight - labelHeight);
        nonQuestScrollPaneList.setSize(listWidth - 2, listHeight - listTopPadding - labelHeight - 2);
        nonQuestScrollPaneList.setX(2);
        nonQuestScrollPaneList.setY(2);
        questBackground.setSize(listWidth, listHeight - labelHeight);
        questScrollPaneList.setSize(listWidth - 4, listHeight - listTopPadding - labelHeight - 2);
        questScrollPaneList.setX(2);
        questScrollPaneList.setX(4);


        groupNonQuest.addActor(nonQuestBackground);
        groupNonQuest.addActor(nonQuestScrollPaneList);
        groupQuest.addActor(questBackground);
        groupQuest.addActor(questScrollPaneList);

        keyItemsListsTable.row().width(stage.getWidth()).height(labelHeight - 2);
        keyItemsListsTable.add(labelNonQuest).pad(-1).width(listWidth);
        keyItemsListsTable.add(labelQuest).pad(-1).width(listWidth);

        keyItemsListsTable.row().width(stage.getWidth()).height(stage.getHeight() - bottomMargin - labelHeight - mainButtonTable.getHeight());
        keyItemsListsTable.add(groupNonQuest).pad(-1).width(listWidth);
        keyItemsListsTable.add(groupQuest).pad(-1).width(listWidth);
        keyItemsListsTable.pack();
        keyItemsListsTable.setPosition(leftMargin, bottomMargin + mainButtonTable.getHeight());

        //
        // RIGHT PANEL AREA
        final float descTablePadding = 10;
        float descAreaWidth = buttonWidth;
        float descAreaHeight = buttonHeight * 3 - 4;

        descText = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        descText.setWrap(true);

        descScrollPanel = new ScrollPane(descText);
        descScrollPanel.setHeight(10);

        descBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        descBackground.setSize(descAreaWidth, descAreaHeight);
        descBackground.setPosition(leftMargin + consumableListsTable.getWidth() - 1, bottomMargin - 1);
        descBackground.setTouchable(Touchable.disabled);

        descTable = new Table();
        descTable.setHeight(descAreaHeight - descTablePadding);
        descTable.setWidth(descAreaWidth);
        descTable.pad(descTablePadding).defaults().expandX().fillX().space(0);
        descTable.setPosition(leftMargin + consumableListsTable.getWidth(), bottomMargin + descTablePadding/2);
        descTable.row().height(descAreaHeight * 0.9f);
        descTable.add(descScrollPanel);

        //descTable.debugAll();

        actionButtonTable.row().width(stage.getWidth()).height(buttonHeight);
        actionButtonTable.add(actionButton).pad(-1).width(buttonWidth + 2);
        actionButtonTable.row().width(stage.getWidth()).height(buttonHeight);
        actionButtonTable.add(swapButton).pad(-1).width(buttonWidth + 2);
        actionButtonTable.row().width(stage.getWidth()).height(buttonHeight);
        actionButtonTable.add(backButton).pad(-1).width(buttonWidth + 2);
        actionButtonTable.pack();
        actionButtonTable.setPosition(descTable.getX() + descTable.getWidth() - 2, bottomMargin);

        graphicGroup = new WidgetGroup();
        graphicBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        graphicBackground.setSize(descTable.getWidth() + actionButtonTable.getWidth(), stage.getHeight() - actionButtonTable.getHeight() - bottomMargin - 4);
        graphicBackground.setPosition(descTable.getX() - 1, descTable.getY() + descTable.getHeight() + 2);
        graphicBackground.setTouchable(Touchable.disabled);

        backpack = getAnimatedImage(EntityFactory.EntityName.BACKPACK);
        backpack.setCurrentAnimationType(Entity.AnimationType.OPEN);
        backpack.setCurrentAnimation(Entity.AnimationType.OPEN);
        // Note: Size and AnimationType are also set in setBackpack
        backpack.setSize(graphicBackground.getWidth() * 2/3, graphicBackground.getWidth() * 2/3);
        backpack.setPosition(graphicBackground.getX() + (graphicBackground.getWidth() - backpack.getWidth())/2,
                             graphicBackground.getY() + (graphicBackground.getHeight() - backpack.getHeight())/2);

        graphicGroup.addActor(graphicBackground);
        graphicGroup.addActor(backpack);

        equipmentButton.addListener(new ClickListener() {
                                    @Override
                                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                        disableAllButtonsExceptSelectedButton(equipmentButton);
                                        setButtonState(ButtonState.EQUIPMENT);
                                        isSwapping = false;
                                        return true;
                                    }

                                    @Override
                                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                        if (backButton.getText().toString().equals(BTN_NAME_CANCEL)) {
                                            buttonCancel();
                                        }

                                        setLists(ButtonState.EQUIPMENT);
                                        enableAllButtons();
                                    }
                                }
        );

        consumablesButton.addListener(new ClickListener() {
                                        @Override
                                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                            disableAllButtonsExceptSelectedButton(consumablesButton);
                                            setButtonState(ButtonState.CONSUMABLES);
                                            isSwapping = false;
                                            return true;
                                        }

                                        @Override
                                        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                            if (backButton.getText().toString().equals(BTN_NAME_CANCEL)) {
                                                buttonCancel();
                                            }

                                            setLists(ButtonState.CONSUMABLES);
                                            enableAllButtons();
                                        }
                                    }
        );

        keyItemsButton.addListener(new ClickListener() {
                                        @Override
                                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                            disableAllButtonsExceptSelectedButton(keyItemsButton);
                                            setButtonState(ButtonState.KEY_ITEMS);
                                            isSwapping = false;
                                            return true;
                                        }

                                        @Override
                                        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                            if (backButton.getText().toString().equals(BTN_NAME_CANCEL)) {
                                                buttonCancel();
                                            }

                                            setLists(ButtonState.KEY_ITEMS);
                                            enableAllButtons();
                                        }
                                    }
        );

        swapButton.addListener(new ClickListener() {
                                   @Override
                                   public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                       disableAllButtonsExceptSelectedButton(swapButton);
                                       return true;
                                   }

                                   @Override
                                   public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                       isSwapping = true;

                                       switch (buttonState) {
                                           case EQUIPMENT:
                                               if (lastSelectedEquipmentListType == ListType.WEAPON) {
                                                   if (weaponListView.items.size == 1) {
                                                       descText.setText(NOTHING_TO_SWAP);
                                                       isSwapping = false;
                                                   }
                                                   else {
                                                       descText.setText("Select another weapon item");
                                                   }
                                               }
                                               else {
                                                   if (armorListView.items.size == 1) {
                                                       descText.setText(NOTHING_TO_SWAP);
                                                       isSwapping = false;
                                                   }
                                                   else {
                                                       descText.setText("Select another armor item");
                                                   }
                                               }
                                               break;
                                           case CONSUMABLES:
                                               if (lastSelectedConsumablesListType == ListType.POTION) {
                                                   if (potionsListView.items.size == 1) {
                                                       descText.setText(NOTHING_TO_SWAP);
                                                       isSwapping = false;
                                                   }
                                                   else {
                                                       descText.setText("Select another potion item");
                                                   }
                                               }
                                               else if (lastSelectedConsumablesListType == ListType.CONSUMABLE) {
                                                   if (consumablesListView.items.size == 1) {
                                                       descText.setText(NOTHING_TO_SWAP);
                                                       isSwapping = false;
                                                   }
                                                   else {
                                                       descText.setText("Select another consumable item");
                                                   }
                                               }
                                               else if (lastSelectedConsumablesListType == ListType.THROWING) {
                                                   if (throwingListView.items.size == 1) {
                                                       descText.setText(NOTHING_TO_SWAP);
                                                       isSwapping = false;
                                                   }
                                                   else {
                                                       descText.setText("Select another throwing item");
                                                   }
                                               }
                                               break;
                                           case KEY_ITEMS:
                                               descText.setText("Select another key item????? TODO!!! Or NOT!!");
                                               break;
                                       }

                                       enableAllButtons();
                                   }
                               }
        );

        backButton.addListener(new ClickListener() {
                                        @Override
                                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                            disableAllButtonsExceptSelectedButton(backButton);
                                            isSwapping = false;
                                            return true;
                                        }

                                        @Override
                                        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                            Gdx.app.log(TAG, "backButton up");
                                            if (backButton.getText().toString().equals(BTN_NAME_CANCEL)) {
                                                buttonCancel();
                                            }
                                            else if (backButton.getText().toString().equals(BTN_NAME_CLOSE)) {
                                                hide();
                                            }
                                            else {
                                                backButton.setText(BTN_NAME_CLOSE);
                                            }

                                            enableAllButtons();
                                        }
                                    }
        );

        actionButton.addListener(new ClickListener() {
                                   @Override
                                   public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                       disableAllButtonsExceptSelectedButton(actionButton);
                                       isSwapping = false;
                                       return true;
                                   }

                                   @Override
                                   public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                       Gdx.app.log(TAG, "actionButton up");
                                       String buttonName = actionButton.getText().toString();

                                       if (buttonName.equals(BTN_NAME_EQUIP)) {
                                           backButton.setText(BTN_NAME_CANCEL);
                                           actionButton.setText(BTN_NAME_OK);
                                           displayEquipToScreen();
                                           swapButton.setTouchable(Touchable.disabled); //todo?

                                           if (lastSelectedEquipmentListType == ListType.WEAPON) {
                                               handleEquipmentListSelection(weaponListView, false);
                                           }
                                           else if (lastSelectedEquipmentListType == ListType.ARMOR) {
                                               handleEquipmentListSelection(armorListView, false);
                                           }
                                       }
                                       else if (buttonName.equals(BTN_NAME_USE)) {

                                       }
                                       else if (buttonName.equals(BTN_NAME_INSPECT)) {

                                       }
                                       else if (buttonName.equals(BTN_NAME_OK)) {
                                           TextButton selectedCharacter = equipToListView.getSelected();
                                           if (selectedCharacter != null && lastSelectedEquipmentItem != null) {
                                               PartyInventoryItem selectedItem = (PartyInventoryItem)lastSelectedEquipmentItem.getUserObject();
                                               descText.setText(selectedCharacter.getText().toString() + " equipped with " + selectedItem.getElement().name);

                                               InventoryElement selectedElement = selectedItem.getElement();
                                               if (selectedElement.isWeapon()) {
                                                   Entity partyMember = (Entity)selectedCharacter.getUserObject();
                                                   InventoryElement currentWeapon = partyMember.getWeapon();

                                                   if (currentWeapon != null) {
                                                       PartyInventory.getInstance().stopUsingItem(currentWeapon);
                                                   }

                                                   partyMember.setWeapon(selectedElement);
                                                   PartyInventory.getInstance().useItem(selectedElement);
                                               }
                                               else if (selectedElement.isArmor()) {
                                                   Entity partyMember = (Entity)selectedCharacter.getUserObject();
                                                   InventoryElement currentArmorItem = partyMember.getArmor(selectedElement.category);

                                                   if (currentArmorItem != null) {
                                                       PartyInventory.getInstance().stopUsingItem(currentArmorItem);
                                                   }

                                                   partyMember.setArmor(selectedElement);
                                                   PartyInventory.getInstance().useItem(selectedElement);
                                               }

                                               displayEquipmentScreen();
                                           }
                                       }

                                       enableAllButtons();
                                   }
                               }
        );

        weaponListView.addListener(new ClickListener() {
                                        @Override
                                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                            armorListView.setSelectedIndex(-1);
                                            armorListView.setTouchable(Touchable.disabled);
                                            return true;
                                        }

                                        @Override
                                        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                            if (isSwapping) {
                                                if (lastSelectedEquipmentListType == ListType.WEAPON) {
                                                    lastSelectedEquipmentItem = swapEquipmentListItems(weaponListView, lastSelectedEquipmentItem);
                                                    handleEquipmentListSelection(weaponListView, true);
                                                }
                                                else {
                                                    reselectLastListItem(lastSelectedEquipmentListType);
                                                    weaponListView.setSelectedIndex(-1);
                                                }
                                            }
                                            else {
                                                handleEquipmentListSelection(weaponListView, false);
                                            }

                                            armorListView.setTouchable(Touchable.enabled);
                                        }
                                    }
        );

        armorListView.addListener(new ClickListener() {
                                       @Override
                                       public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                           weaponListView.setSelectedIndex(-1);
                                           weaponListView.setTouchable(Touchable.disabled);
                                           return true;
                                       }

                                       @Override
                                       public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                           if (isSwapping) {
                                               if (lastSelectedEquipmentListType == ListType.ARMOR) {
                                                   lastSelectedEquipmentItem = swapEquipmentListItems(armorListView, lastSelectedEquipmentItem);
                                                   handleEquipmentListSelection(armorListView, true);
                                               }
                                               else {
                                                   reselectLastListItem(lastSelectedEquipmentListType);
                                                   armorListView.setSelectedIndex(-1);
                                               }
                                           }
                                           else {
                                               handleEquipmentListSelection(armorListView, false);
                                           }

                                           weaponListView.setTouchable(Touchable.enabled);
                                       }
                                   }
        );

        potionsListView.addListener(new ClickListener() {
                                        @Override
                                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                            consumablesListView.setSelectedIndex(-1);
                                            throwingListView.setSelectedIndex(-1);
                                            consumablesListView.setTouchable(Touchable.disabled);
                                            throwingListView.setTouchable(Touchable.disabled);
                                            return true;
                                        }

                                        @Override
                                        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                            if (isSwapping) {
                                                if (lastSelectedConsumablesListType == ListType.POTION) {
                                                    lastSelectedConsumablesItem = swapEquipmentListItems(potionsListView, lastSelectedConsumablesItem);
                                                }
                                                else {
                                                    reselectLastListItem(lastSelectedConsumablesListType);
                                                    potionsListView.setSelectedIndex(-1);
                                                }
                                            }
                                            else {
                                                lastSelectedConsumablesItem = potionsListView.getSelected();
                                                if (lastSelectedConsumablesItem != null) {
                                                    PartyInventoryItem partyInventoryItem = (PartyInventoryItem) lastSelectedConsumablesItem.getUserObject();
                                                    InventoryElement element = partyInventoryItem.getElement();
                                                    descText.setText(element.summary);
                                                    lastSelectedConsumablesListType = ListType.POTION;
                                                }
                                            }

                                            consumablesListView.setTouchable(Touchable.enabled);
                                            throwingListView.setTouchable(Touchable.enabled);
                                        }
                                    }
        );

        consumablesListView.addListener(new ClickListener() {
                                        @Override
                                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                            potionsListView.setSelectedIndex(-1);
                                            throwingListView.setSelectedIndex(-1);
                                            potionsListView.setTouchable(Touchable.disabled);
                                            throwingListView.setTouchable(Touchable.disabled);
                                            return true;
                                        }

                                        @Override
                                        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                            if (isSwapping) {
                                                TextButton selectedItem  = consumablesListView.getSelected();
                                                if (lastSelectedConsumablesListType == ListType.CONSUMABLE) {
                                                    lastSelectedConsumablesItem = swapEquipmentListItems(consumablesListView, lastSelectedConsumablesItem);
                                                }
                                                else {
                                                    reselectLastListItem(lastSelectedConsumablesListType);
                                                    consumablesListView.setSelectedIndex(-1);
                                                }
                                            }
                                            else {
                                                lastSelectedConsumablesItem = consumablesListView.getSelected();
                                                if (lastSelectedConsumablesItem != null) {
                                                    PartyInventoryItem partyInventoryItem = (PartyInventoryItem) lastSelectedConsumablesItem.getUserObject();
                                                    InventoryElement element = partyInventoryItem.getElement();
                                                    descText.setText(element.summary);
                                                    lastSelectedConsumablesListType = ListType.CONSUMABLE;
                                                }
                                            }

                                            potionsListView.setTouchable(Touchable.enabled);
                                            throwingListView.setTouchable(Touchable.enabled);
                                        }
                                    }
        );

        throwingListView.addListener(new ClickListener() {
                                        @Override
                                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                            potionsListView.setSelectedIndex(-1);
                                            consumablesListView.setSelectedIndex(-1);
                                            potionsListView.setTouchable(Touchable.disabled);
                                            consumablesListView.setTouchable(Touchable.disabled);
                                            return true;
                                        }

                                        @Override
                                        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                            if (isSwapping) {
                                                if (lastSelectedConsumablesListType == ListType.THROWING) {
                                                    lastSelectedConsumablesItem = swapEquipmentListItems(throwingListView, lastSelectedConsumablesItem);
                                                }
                                                else {
                                                    reselectLastListItem(lastSelectedConsumablesListType);
                                                    throwingListView.setSelectedIndex(-1);
                                                }
                                            }
                                            else {
                                                lastSelectedConsumablesItem = throwingListView.getSelected();
                                                if (lastSelectedConsumablesItem != null) {
                                                    PartyInventoryItem partyInventoryItem = (PartyInventoryItem) lastSelectedConsumablesItem.getUserObject();
                                                    InventoryElement element = partyInventoryItem.getElement();
                                                    descText.setText(element.summary);
                                                    lastSelectedConsumablesListType = ListType.THROWING;
                                                }
                                            }

                                            potionsListView.setTouchable(Touchable.enabled);
                                            consumablesListView.setTouchable(Touchable.enabled);
                                        }
                                    }
        );

        questListView.addListener(new ClickListener() {
                                         @Override
                                         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                             nonQuestListView.setSelectedIndex(-1);
                                             nonQuestListView.setTouchable(Touchable.disabled);
                                             return true;
                                         }

                                         @Override
                                         public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                             if (isSwapping) {
                                                 if (lastSelectedKeyItemsListType == ListType.QUEST) {
                                                     TextButton selectedItem  = questListView.getSelected();
                                                     if (selectedItem != lastSelectedKeyItem) {
                                                         swapListItems(questListView, selectedItem, lastSelectedKeyItem);

                                                         PartyInventory.getInstance().swapItems((PartyInventoryItem) selectedItem.getUserObject(),
                                                                                                (PartyInventoryItem) lastSelectedKeyItem.getUserObject());

                                                         descText.setText(ITEMS_SWAPPED);
                                                         isSwapping = false;
                                                         lastSelectedKeyItem = selectedItem;
                                                     }
                                                 }
                                                 else {
                                                     reselectLastListItem(lastSelectedKeyItemsListType);
                                                     questListView.setSelectedIndex(-1);
                                                 }
                                             }
                                             else {
                                                 lastSelectedKeyItem = questListView.getSelected();
                                                 //todo
                                                 /*
                                                 PartyInventoryItem partyInventoryItem = (PartyInventoryItem) lastSelectedKeyItem.getUserObject();
                                                 InventoryElement element = partyInventoryItem.getElement();
                                                 descText.setText(element.summary);
                                                 */
                                                 lastSelectedKeyItemsListType = ListType.QUEST;
                                             }

                                             nonQuestListView.setTouchable(Touchable.enabled);
                                         }
                                     }
        );

        nonQuestListView.addListener(new ClickListener() {
                                      @Override
                                      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                          questListView.setSelectedIndex(-1);
                                          questListView.setTouchable(Touchable.disabled);
                                          return true;
                                      }

                                      @Override
                                      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                          if (isSwapping) {
                                              if (lastSelectedKeyItemsListType == ListType.NON_QUEST) {
                                                  TextButton selectedItem  = nonQuestListView.getSelected();
                                                  if (selectedItem != lastSelectedKeyItem) {
                                                      swapListItems(nonQuestListView, selectedItem, lastSelectedKeyItem);

                                                      PartyInventory.getInstance().swapItems((PartyInventoryItem) selectedItem.getUserObject(),
                                                                                            (PartyInventoryItem) lastSelectedKeyItem.getUserObject());

                                                      descText.setText(ITEMS_SWAPPED);
                                                      isSwapping = false;
                                                      lastSelectedKeyItem = selectedItem;
                                                  }
                                              }
                                              else {
                                                  reselectLastListItem(lastSelectedKeyItemsListType);
                                                  nonQuestListView.setSelectedIndex(-1);
                                              }
                                          }
                                          else {
                                              lastSelectedKeyItem = nonQuestListView.getSelected();
                                              //todo
                                              /*
                                              PartyInventoryItem partyInventoryItem = (PartyInventoryItem) lastSelectedKeyItem.getUserObject();
                                              InventoryElement element = partyInventoryItem.getElement();
                                              descText.setText(element.summary);
                                              */
                                              lastSelectedKeyItemsListType = ListType.NON_QUEST;
                                          }

                                          questListView.setTouchable(Touchable.enabled);
                                      }
                                  }
        );
    }

    private void populatePartyEntityList() {
        Array<EntityFactory.EntityName> partyList = game.getPartyList();

        for (EntityFactory.EntityName entityName : partyList) {
            Entity entity = EntityFactory.getInstance().getEntityByName(entityName);
            partyEntityList.add(entity);
        }
    }

    private AnimatedImage setEntityAnimation(Entity entity){
        final AnimatedImage animEntity = new AnimatedImage();
        animEntity.setEntity(entity);
        animEntity.setSize(animEntity.getWidth() * Map.UNIT_SCALE, animEntity.getHeight() * Map.UNIT_SCALE);
        return animEntity;
    }

    private AnimatedImage getAnimatedImage(EntityFactory.EntityName entityName){
        Entity entity = EntityFactory.getInstance().getEntityByName(entityName);
        return setEntityAnimation(entity);
    }

    private void buttonCancel() {
        backButton.setText(BTN_NAME_CLOSE);
        displayEquipmentScreen();
    }

    private void disableAllButtonsExceptSelectedButton(TextButton selectedButton) {
        if (!selectedButton.equals(equipmentButton))
            equipmentButton.setTouchable(Touchable.disabled);
        if (!selectedButton.equals(consumablesButton))
            consumablesButton.setTouchable(Touchable.disabled);
        if (!selectedButton.equals(keyItemsButton))
            keyItemsButton.setTouchable(Touchable.disabled);
        if (!selectedButton.equals(actionButton))
            actionButton.setTouchable(Touchable.disabled);
        if (!selectedButton.equals(swapButton))
            swapButton.setTouchable(Touchable.disabled);
        if (!selectedButton.equals(backButton))
            backButton.setTouchable(Touchable.disabled);
    }

    private void enableAllButtons() {
        equipmentButton.setTouchable(Touchable.enabled);
        consumablesButton.setTouchable(Touchable.enabled);
        keyItemsButton.setTouchable(Touchable.enabled);
        actionButton.setTouchable(Touchable.enabled);
        swapButton.setTouchable(Touchable.enabled);
        backButton.setTouchable(Touchable.enabled);
    }

    private void displayEquipToScreen() {
        equipmentListsTable.clear();
        descText.setText("Select an item and a character to equip and click OK.");

        float listWidth = (int)stage.getWidth() / 5;
        float listHeight = (stage.getHeight() - mainButtonTable.getHeight() - bottomMargin);
        float listTopPadding = 6;

        weaponBackground.setSize(listWidth, listHeight - labelHeight - nameTableHeight + 2);
        weaponScrollPaneList.setSize(listWidth - 2, listHeight - listTopPadding - labelHeight - nameTableHeight);
        weaponScrollPaneList.setX(2);   // this and the above -2 prevents highlight of selected item from crossing over the borders
        armorBackground.setSize(listWidth, listHeight - labelHeight - nameTableHeight + 2);
        armorScrollPaneList.setSize(listWidth - 4, listHeight - listTopPadding - labelHeight - nameTableHeight);
        armorScrollPaneList.setX(2);
        equipNameBackground.setSize(listWidth, listHeight - labelHeight - nameTableHeight + 2);
        equipToListView.setSize(listWidth - 4, listHeight - labelHeight - nameTableHeight + 2);
        equipToListView.setX(2);
        equipToListView.setY(-8);

        weaponNameBackground.setSize(listWidth, nameTableHeight);
        armorNameBackground.setSize(listWidth * 2 - 2, nameTableHeight); // needs to correspond with width of adding groupArmorName below

        equipmentListsTable.row().width(stage.getWidth()).height(labelHeight - 2);
        equipmentListsTable.add(labelWeapon).pad(-1).width(listWidth);
        equipmentListsTable.add(labelArmor).pad(-1).width(listWidth);
        equipmentListsTable.add(labelEquipTo).pad(-1).width(listWidth);

        equipmentListsTable.row().width(stage.getWidth()).height(stage.getHeight() - bottomMargin - labelHeight - nameTableHeight - mainButtonTable.getHeight() + 2);
        equipmentListsTable.add(groupWeapon).pad(-1).width(listWidth);
        equipmentListsTable.add(groupArmor).pad(-1).width(listWidth);
        equipmentListsTable.add(groupEquipToName).pad(-1).width(listWidth);

        equipmentListsTable.row().width(stage.getWidth()).height(nameTableHeight);
        equipmentListsTable.add(groupWeaponName).pad(-1).width(listWidth);
        // some trickery involved here... need to correspond with width of armorNameBackground above
        equipmentListsTable.add(groupArmorName).pad(-1).width(listWidth * 2 - 2).colspan(2);
        equipmentListsTable.pack();
    }

    private void displayEquipmentScreen() {
        // re-enable all buttons
        equipmentButton.setTouchable(Touchable.enabled);
        consumablesButton.setTouchable(Touchable.enabled);
        keyItemsButton.setTouchable(Touchable.enabled);
        swapButton.setTouchable(Touchable.enabled);

        actionButton.setText(BTN_NAME_EQUIP);
        backButton.setText(BTN_NAME_CLOSE);

        equipmentListsTable.clear();

        float listWidth = mainButtonTable.getWidth()/2 + 2;
        float listHeight = (stage.getHeight() - mainButtonTable.getHeight() - bottomMargin);
        float listTopPadding = 6;

        weaponBackground.setSize(listWidth, listHeight - labelHeight - nameTableHeight + 2);
        weaponScrollPaneList.setSize(listWidth - 2, listHeight - listTopPadding - labelHeight - nameTableHeight);
        weaponScrollPaneList.setX(2);   // this and the above -2 prevents highlight of selected item from crossing over the borders
        armorBackground.setSize(listWidth, listHeight - labelHeight - nameTableHeight + 2);
        armorScrollPaneList.setSize(listWidth - 4, listHeight - listTopPadding - labelHeight - nameTableHeight);
        armorScrollPaneList.setX(2);

        weaponNameBackground.setSize(listWidth, nameTableHeight);
        armorNameBackground.setSize(listWidth, nameTableHeight);

        equipmentListsTable.row().width(stage.getWidth()).height(labelHeight - 2);
        equipmentListsTable.add(labelWeapon).pad(-1).width(listWidth);
        equipmentListsTable.add(labelArmor).pad(-1).width(listWidth);

        equipmentListsTable.row().width(stage.getWidth()).height(stage.getHeight() - bottomMargin - labelHeight - nameTableHeight - mainButtonTable.getHeight() + 2);
        equipmentListsTable.add(groupWeapon).pad(-1).width(listWidth);
        equipmentListsTable.add(groupArmor).pad(-1).width(listWidth);

        equipmentListsTable.row().width(stage.getWidth()).height(nameTableHeight);
        equipmentListsTable.add(groupWeaponName).pad(-1).width(listWidth);
        equipmentListsTable.add(groupArmorName).pad(-1).width(listWidth);
        equipmentListsTable.pack();

        if (lastSelectedEquipmentListType == ListType.WEAPON) {
            // populate bottom character name list
            Array<Label> names = getEquipmentUserNames();
            createNameTable(weaponNameTable, names);
            clearNameTable(armorNameTable);
        }
        else if (lastSelectedEquipmentListType == ListType.ARMOR) {
            Array<Label> names = getEquipmentUserNames();
            createNameTable(armorNameTable, names);
            clearNameTable(weaponNameTable);
        }
        else {
            Gdx.app.error(TAG, "Unhandled ListType in displayEquipmentScreen");
        }
    }

    private Array<Label> getEquipmentUserNames()
    {
        Array<Label> names = new Array<>();
        PartyInventoryItem partyInventoryItem = (PartyInventoryItem) lastSelectedEquipmentItem.getUserObject();
        InventoryElement selectedElement = partyInventoryItem.getElement();

        for (Entity partyMember : partyEntityList) {
            InventoryElement partyMemberElement = null;

            if (selectedElement.isWeapon()) {
                partyMemberElement = partyMember.getWeapon();
            } else if (selectedElement.isArmor()) {
                partyMemberElement = partyMember.getArmor(selectedElement.category);
            }

            if (partyMemberElement != null && selectedElement != null) {
                if (selectedElement.id == partyMemberElement.id) {
                    // character has this weapon, so display their name in bottom list
                    names.add(new Label(partyMember.getEntityConfig().getDisplayName(), Utility.ELMOUR_UI_SKIN, "battle"));
                }
            }
        }

        if (!partyInventoryItem.isAvailable()) {
            // clear Equip To list
            equipToListView.clearItems();
        }

        return names;
    }

    private TextButton swapEquipmentListItems(MyTextButtonList<TextButton> listView, TextButton lastSelectedItem) {
        TextButton selectedItem = listView.getSelected();
        if (selectedItem != lastSelectedItem) {
            swapListItems(listView, selectedItem, lastSelectedItem);

            PartyInventory.getInstance().swapItems((PartyInventoryItem) selectedItem.getUserObject(),
                    (PartyInventoryItem) lastSelectedItem.getUserObject());

            descText.setText(ITEMS_SWAPPED);
            isSwapping = false;
            return selectedItem;
        }
        else {
            return lastSelectedItem;
        }
    }

    private void handleEquipmentListSelection(MyTextButtonList<TextButton> listView, boolean itemWasSwapped) {
        lastSelectedEquipmentItem = listView.getSelected();
        if (lastSelectedEquipmentItem != null) {

            if (partyEntityList.size == 0) {
                populatePartyEntityList();
            }
            PartyInventoryItem partyInventoryItem = (PartyInventoryItem) lastSelectedEquipmentItem.getUserObject();
            InventoryElement selectedElement = partyInventoryItem.getElement();

            if (!itemWasSwapped) {
                descText.setText(selectedElement.summary);
            }

            // populate Equip To and bottom character name lists
            Array<Label> names = new Array<>();
            equipToListView.clearItems();
            for (Entity partyMember : partyEntityList) {
                InventoryElement partyMemberElement = null;

                if (selectedElement.isWeapon()) {
                    partyMemberElement = partyMember.getWeapon();
                } else if (selectedElement.isArmor()) {
                    partyMemberElement = partyMember.getArmor(selectedElement.category);
                }

                if (partyMemberElement == null || (selectedElement.id != partyMemberElement.id)) {
                    if (partyInventoryItem.isAvailable()) {
                        // character does not have this item, so add their name to the Equip To list
                        TextButton equipToName = new TextButton(partyMember.getEntityConfig().getDisplayName(), Utility.ELMOUR_UI_SKIN, "tree_node");
                        equipToName.setUserObject(partyMember);
                        equipToListView.getItems().add(equipToName);
                    } else {
                        equipToListView.clearItems();
                    }
                } else {
                    // character already has this item, so display their name in bottom list
                    names.add(new Label(partyMember.getEntityConfig().getDisplayName(), Utility.ELMOUR_UI_SKIN, "battle"));
                }
            }

            if (selectedElement.isWeapon()) {
                createNameTable(weaponNameTable, names);
                clearNameTable(armorNameTable);
                lastSelectedEquipmentListType = ListType.WEAPON;
            } else if (selectedElement.isArmor()) {
                createNameTable(armorNameTable, names);
                clearNameTable(weaponNameTable);
                lastSelectedEquipmentListType = ListType.ARMOR;
            }
        }
    }
    private void swapListItems(MyTextButtonList<TextButton> list, TextButton a, TextButton b) {
        list.getItems().swap(list.getItems().indexOf(a, true), list.getItems().indexOf(b, true));
    }

    private void reselectLastListItem(ListType lastSelectedListType) {
        if (lastSelectedListType == null) {
            isSwapping = false;
            descText.setText(NOTHING_TO_SWAP);
        }
        else {
            switch (lastSelectedListType) {
                case WEAPON:
                    weaponListView.setSelected(lastSelectedEquipmentItem);
                    break;
                case ARMOR:
                    armorListView.setSelected(lastSelectedEquipmentItem);
                    break;
                case POTION:
                    potionsListView.setSelected(lastSelectedConsumablesItem);
                    break;
                case CONSUMABLE:
                    consumablesListView.setSelected(lastSelectedConsumablesItem);
                    break;
                case THROWING:
                    throwingListView.setSelected(lastSelectedConsumablesItem);
                    break;
                case QUEST:
                    questListView.setSelected(lastSelectedKeyItem);
                    break;
                case NON_QUEST:
                    nonQuestListView.setSelected(lastSelectedKeyItem);
                    break;
            }
        }
    }

    private void createNameTable(Table table, Array<Label> names) {
        table.clear();

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
            nameHeight = 10;
            topMargin = 0;
            bottomMargin = 4.5f;
            leftMargin = 8;
            rightMargin = 0;
        }

        if (names.size > 0) {
            table.row().pad(topMargin, leftMargin, bottomMargin, rightMargin);
            table.add(names.get(0)).size(nameWidth, nameHeight);
        }
        if (names.size > 1) {
            table.row().pad(topMargin, leftMargin, bottomMargin, rightMargin);
            table.add(names.get(1)).size(nameWidth, nameHeight);
        }
        if (names.size > 2) {
            table.row().pad(topMargin, leftMargin, bottomMargin, rightMargin);
            table.add(names.get(2)).size(nameWidth, nameHeight);
        }
        if (names.size > 3) {
            table.row().pad(topMargin, leftMargin, bottomMargin, rightMargin);
            table.add(names.get(3)).size(nameWidth, nameHeight);
        }
        if (names.size > 4) {
            table.row().pad(topMargin, leftMargin, bottomMargin, rightMargin);
            table.add(names.get(4)).size(nameWidth, nameHeight);
        }

        // add empty rows so that visible names are aligned at top
        for (int i = 0; i < 5 - names.size; i++) {
            table.row().pad(topMargin, leftMargin, bottomMargin, rightMargin);
            Label emptyLabel = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
            table.add(emptyLabel).size(nameWidth, nameHeight);
        }

        table.pack();
    }

    private void clearNameTable(Table table) {
        table.clear();
    }

    private boolean touchPointIsInButton(TextButton button) {
        // Get touch point
        Vector2 screenPos = new Vector2(Gdx.input.getX(), Gdx.input.getY());

        // Convert the touch point into local coordinates
        Vector2 localPos = new Vector2(screenPos);
        localPos = stage.screenToStageCoordinates(localPos);

        Rectangle buttonRect = new Rectangle(button.getX(), button.getY(), button.getWidth(), button.getHeight());

        return Utility.pointInRectangle(buttonRect, localPos.x, localPos.y);
    }

    private void setButtonState(ButtonState state) {
        buttonState = state;

        switch (state) {
            case EQUIPMENT:
                equipmentButton.setStyle(Utility.ELMOUR_UI_SKIN.get("force_down", TextButton.TextButtonStyle.class));
                consumablesButton.setStyle(Utility.ELMOUR_UI_SKIN.get("battle", TextButton.TextButtonStyle.class));
                keyItemsButton.setStyle(Utility.ELMOUR_UI_SKIN.get("battle", TextButton.TextButtonStyle.class));
                actionButton.setText(BTN_NAME_EQUIP);
                break;
            case CONSUMABLES:
                equipmentButton.setStyle(Utility.ELMOUR_UI_SKIN.get("battle", TextButton.TextButtonStyle.class));
                consumablesButton.setStyle(Utility.ELMOUR_UI_SKIN.get("force_down", TextButton.TextButtonStyle.class));
                keyItemsButton.setStyle(Utility.ELMOUR_UI_SKIN.get("battle", TextButton.TextButtonStyle.class));
                actionButton.setText(BTN_NAME_USE);
                break;
            case KEY_ITEMS:
                equipmentButton.setStyle(Utility.ELMOUR_UI_SKIN.get("battle", TextButton.TextButtonStyle.class));
                consumablesButton.setStyle(Utility.ELMOUR_UI_SKIN.get("battle", TextButton.TextButtonStyle.class));
                keyItemsButton.setStyle(Utility.ELMOUR_UI_SKIN.get("force_down", TextButton.TextButtonStyle.class));
                actionButton.setText(BTN_NAME_INSPECT);
                break;
        }
    }

    private void setLists(ButtonState state) {

        descText.setText("");

        setBackpack(state);

        switch (state) {
            case EQUIPMENT:
                stage.addActor(equipmentListsTable);
                consumableListsTable.remove();
                keyItemsListsTable.remove();
                if (lastSelectedEquipmentItem != null) {
                    PartyInventoryItem partyInventoryItem = (PartyInventoryItem) lastSelectedEquipmentItem.getUserObject();
                    InventoryElement equipmentElement = partyInventoryItem.getElement();
                    descText.setText(equipmentElement.summary);
                }
                break;
            case CONSUMABLES:
                equipmentListsTable.remove();
                stage.addActor(consumableListsTable);
                keyItemsListsTable.remove();
                if (lastSelectedConsumablesItem != null) {
                    PartyInventoryItem partyInventoryItem = (PartyInventoryItem) lastSelectedConsumablesItem.getUserObject();
                    InventoryElement consumableElement = partyInventoryItem.getElement();
                    descText.setText(consumableElement.summary);
                }
                break;
            case KEY_ITEMS:
                equipmentListsTable.remove();
                consumableListsTable.remove();
                stage.addActor(keyItemsListsTable);
                break;
        }
    }

    private void setBackpack(ButtonState buttonState) {
        Entity.AnimationType animationType = Entity.AnimationType.OPEN;

        switch(buttonState) {
            case EQUIPMENT:
                if (previousButtonState == ButtonState.CONSUMABLES)
                    animationType = Entity.AnimationType.BATTLE_EQUIP;
                else
                    animationType = Entity.AnimationType.KEY_EQUIP;

                previousButtonState = ButtonState.EQUIPMENT;
                break;
            case CONSUMABLES:
                if (previousButtonState == ButtonState.EQUIPMENT)
                    animationType = Entity.AnimationType.EQUIP_BATTLE;
                else
                    animationType = Entity.AnimationType.KEY_BATTLE;

                previousButtonState = ButtonState.CONSUMABLES;
                break;
            case KEY_ITEMS:
                if (previousButtonState == ButtonState.CONSUMABLES)
                    animationType = Entity.AnimationType.BATTLE_KEY;
                else
                    animationType = Entity.AnimationType.EQUIP_KEY;

                previousButtonState = ButtonState.KEY_ITEMS;
                break;
        }

        Gdx.app.log(TAG, "Setting AnimationType " + animationType.toString());
        backpack.setCurrentAnimationType(animationType);
        backpack.setCurrentAnimation(animationType);
        backpack.setSize(graphicBackground.getWidth() * 2/3, graphicBackground.getWidth() * 2/3);
    }

    @Override
    public void show() {
        // initial screen
        setLists(ButtonState.EQUIPMENT);
        setButtonState(ButtonState.EQUIPMENT);

        stage.addActor(mainButtonTable);
        stage.addActor(descBackground);
        stage.addActor(descTable);
        stage.addActor(actionButtonTable);
        stage.addActor(graphicGroup);
        notify(InventoryHudObserver.InventoryHudEvent.INVENTORY_HUD_SHOWN);
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        equipmentListsTable.remove();
        consumableListsTable.remove();
        keyItemsListsTable.remove();
        mainButtonTable.remove();
        descBackground.remove();
        descTable.remove();
        actionButtonTable.remove();
        graphicGroup.remove();
        notify(InventoryHudObserver.InventoryHudEvent.INVENTORY_HUD_HIDDEN);
    }

    @Override
    public void dispose() {

    }

    @Override
    public void addObserver(InventoryHudObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(InventoryHudObserver observer) {
        observers.removeValue(observer, true);
    }

    @Override
    public void notify(InventoryHudObserver.InventoryHudEvent event) {
        for(InventoryHudObserver observer: observers){
            observer.onNotify(event);
        }
    }

    @Override
    public void onNotify(PartyInventoryItem partyInventoryItem, PartyInventoryEvent event) {
        InventoryElement element = partyInventoryItem.getElement();

        //Gdx.app.log(TAG, event.toString() + " " + partyInventoryItem.getElement().id.toString() + " (" + partyInventoryItem.getQuantity() + ")");

        switch (event) {
            case INVENTORY_ADDED:
                switch(element.category) {
                    case Potion:
                        addListViewItem(potionsListView, potionsScrollPaneList, partyInventoryItem);
                        break;
                    case Consumables:
                        addListViewItem(consumablesListView, consumablesScrollPaneList, partyInventoryItem);
                        break;
                    case Throwing:
                        addListViewItem(throwingListView, throwingScrollPaneList, partyInventoryItem);
                        break;
                    case Leggings:
                    case Helmet:
                    case Breastplate:
                        addListViewItem(armorListView, armorScrollPaneList, partyInventoryItem);
                        break;
                    case BLUNT:
                    case STAB:
                    case KNUCKLES:
                        addListViewItem(weaponListView, weaponScrollPaneList, partyInventoryItem);
                        break;
                }
                break;
            case INVENTORY_REMOVED:
                break;
            case INVENTORY_ITEM_USE_CHANGED:
                if (element.isWeapon()) {
                    updateListViewItem(weaponListView, partyInventoryItem);
                }
                else if (element.isArmor()) {
                    updateListViewItem(armorListView, partyInventoryItem);
                }

                break;
        }
    }

    @Override
    public void onNotify(PartyInventoryItem item1, PartyInventoryItem item2, PartyInventoryEvent event) {
        switch (event) {
            case INVENTORY_SWAP:
                break;
        }
    }

    @Override
    public void onNotify(ProfileManager profileManager, ProfileEvent event) {
        Gdx.app.log(TAG, "onNotify event = " + event.toString());

        switch(event){
            case PROFILE_LOADED:
                boolean firstTime = profileManager.getIsNewProfile();

                if( firstTime ){
                    // no inventory
                }
                else {
                    // load inventory from profile manager
                    String partyInventoryString = ProfileManager.getInstance().getProperty(PartyInventory.getInstance().PROPERTY_NAME, String.class);
                    PartyInventory.getInstance().setInventoryList(partyInventoryString);
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

    private void addListViewItem(MyTextButtonList<TextButton> list, ScrollPane scrollPane, PartyInventoryItem partyInventoryItem) {
        InventoryElement element = partyInventoryItem.getElement();
        String description = String.format("%s (%d)", element.name, partyInventoryItem.getQuantityAvailable());
        TextButton button = new TextButton(description, Utility.ELMOUR_UI_SKIN, "tree_node");
        button.setUserObject(partyInventoryItem);

        if (list.getItems().size == 0) {
            // hack to get scrolling to work (need to add array if first item being added)
            TextButton[] buttons = new TextButton[1];
            buttons[0] = button;
            list.setItems(buttons);
            list.setSelectedIndex(-1);
        }
        else {
            TextButton inventoryItem = null;

            // see if there is already an existing inventory item in list
            for (TextButton iterator : list.getItems()) {
                PartyInventoryItem item = (PartyInventoryItem) iterator.getUserObject();
                if (partyInventoryItem.getElement().id.equals(item.getElement().id)) {
                    inventoryItem = iterator;
                    break;
                }
            }

            if (inventoryItem != null) {
                // update existing item
                inventoryItem.setText(description);
            }
            else {
                // add new item
                list.getItems().add(button);
                list.layout();
                scrollPane.layout();

            }
        }
    }

    private void addListViewItem(MyTextButtonList<TextButton> list, ScrollPane scrollPane, KeyItem keyItem) {
        /*
        InventoryElement element = partyInventoryItem.getElement();
        String description = String.format("%s (%d)", element.name, partyInventoryItem.getQuantityAvailable());
        TextButton button = new TextButton(description, Utility.ELMOUR_UI_SKIN, "tree_node");
        button.setUserObject(partyInventoryItem);

        if (list.getItems().size == 0) {
            // hack to get scrolling to work (need to add array if first item being added)
            TextButton[] buttons = new TextButton[1];
            buttons[0] = button;
            list.setItems(buttons);
            list.setSelectedIndex(-1);
        }
        else {
            TextButton inventoryItem = null;

            // see if there is already an existing inventory item in list
            for (TextButton iterator : list.getItems()) {
                PartyInventoryItem item = (PartyInventoryItem) iterator.getUserObject();
                if (partyInventoryItem.getElement().id.equals(item.getElement().id)) {
                    inventoryItem = iterator;
                    break;
                }
            }

            if (inventoryItem != null) {
                // update existing item
                inventoryItem.setText(description);
            }
            else {
                // add new item
                list.getItems().add(button);
                list.layout();
                scrollPane.layout();

            }
        }
        */
    }

    private void updateListViewItem(MyTextButtonList<TextButton> list, PartyInventoryItem partyInventoryItem) {
        InventoryElement element = partyInventoryItem.getElement();
        String description = String.format("%s (%d)", element.name, partyInventoryItem.getQuantityAvailable());

        TextButton inventoryItem = null;

        // find item in list
        for (TextButton iterator : list.getItems()) {
            PartyInventoryItem item = (PartyInventoryItem) iterator.getUserObject();
            if (partyInventoryItem.getElement().id.equals(item.getElement().id)) {
                inventoryItem = iterator;
                break;
            }
        }

        if (inventoryItem != null) {
            // update item
            inventoryItem.setText(description);
            inventoryItem.setUserObject(partyInventoryItem);
        }
    }

    public void addKeyItem(KeyItem item) {
        switch (item.category) {
            case QUEST:
                addListViewItem(questListView, questScrollPaneList, item);
                break;
            case NON_QUEST:
                break;
        }
    }

}
