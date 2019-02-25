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
import com.smoftware.elmour.InventoryElement;
import com.smoftware.elmour.InventoryElementFactory;
import com.smoftware.elmour.PartyInventory;
import com.smoftware.elmour.PartyInventoryItem;
import com.smoftware.elmour.PartyInventoryObserver;
import com.smoftware.elmour.Utility;

/**
 * Created by steve on 2/10/19.
 */

public class InventoryHUD implements Screen, InventoryHudSubject, PartyInventoryObserver {
    private static final String TAG = InventoryHUD.class.getSimpleName();

    enum ButtonState { EQUIPMENT, CONSUMABLES, KEY_ITEMS, EQUIP }
    enum ListType { WEAPON, ARMOR, POTION, CONSUMABLE, THROWING, QUEST, NON_QUEST }

    private Stage stage;
    private Array<InventoryHudObserver> observers;

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
    private final String BTN_NAME_SWAP = "Swap";
    private final String BTN_BACK = "Back";

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
    private Table equipNameTable;
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
    private WidgetGroup groupEquipName;

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
    private MyTextArea equipNameBackground;
    private TextButton labelEquipTo;

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

    // Persistence
    private TextButton lastSelectedConsumablesItem;
    private TextButton lastSelectedEquipmentItem;
    private TextButton lastSelectedKeyItem;
    private ButtonState buttonState;
    private ListType lastSelectedEquipmentListType;
    private ListType lastSelectedConsumablesListType;
    private ListType lastSelectedKeyItemsListType;

    private boolean isSwapping = false;

    float labelHeight = 30;
    float bottomMargin = 6;
    float nameTableHeight;

    public InventoryHUD(Stage stage) {

        this.stage = stage;
        observers = new Array<>();

        PartyInventory.getInstance().addObserver(this);

        equipmentListsTable = new Table();
        consumableListsTable = new Table();
        keyItemsListsTable = new Table();
        mainButtonTable = new Table();
        actionButtonTable = new Table();
        weaponNameTable = new Table();
        armorNameTable = new Table();
        equipNameTable = new Table();

        equipmentButton = new TextButton(BTN_NAME_EQUIPMENT, Utility.ELMOUR_UI_SKIN, "battle");
        consumablesButton = new TextButton(BTN_NAME_CONSUMABLES, Utility.ELMOUR_UI_SKIN, "battle");
        keyItemsButton = new TextButton(BTN_NAME_KEY_ITEMS, Utility.ELMOUR_UI_SKIN, "battle");

        actionButton = new TextButton(BTN_NAME_USE, Utility.ELMOUR_UI_SKIN, "battle");
        swapButton = new TextButton(BTN_NAME_SWAP, Utility.ELMOUR_UI_SKIN, "battle");
        backButton = new TextButton(BTN_BACK, Utility.ELMOUR_UI_SKIN, "battle");

        float topMargin = 6;
        float buttonHeight = 65;
        float buttonWidth = (int)stage.getWidth() / 5;
        float leftMargin = (stage.getWidth() - (buttonWidth * 5) + 6)/2;

        if (ElmourGame.isAndroid()) {
            buttonHeight = 45;
        }

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

        groupWeapon = new WidgetGroup();
        groupArmor = new WidgetGroup();
        groupWeaponName = new WidgetGroup();
        groupArmorName = new WidgetGroup();
        groupEquipName = new WidgetGroup();

        nameTableHeight = 2 * buttonHeight - 2;

        weaponBackground.setSize(listWidth, listHeight - labelHeight - nameTableHeight + 2);
        weaponScrollPaneList.setSize(listWidth - 2, listHeight - listTopPadding - labelHeight - nameTableHeight);
        weaponScrollPaneList.setX(2);   // this and the above -2 prevents highlight of selected item from crossing over the borders
        armorBackground.setSize(listWidth, listHeight - labelHeight - nameTableHeight + 2);
        armorScrollPaneList.setSize(listWidth - 4, listHeight - listTopPadding - labelHeight - nameTableHeight);
        armorScrollPaneList.setX(2);

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
        groupEquipName.addActor(equipNameBackground);
        groupEquipName.addActor(equipNameTable);

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

        /////////////////////////
        //todo:
        InventoryElement weaponElement = InventoryElementFactory.getInstance().getInventoryElement(InventoryElement.ElementID.DAGGER1);
        PartyInventoryItem weaponPartyInventoryItem = new PartyInventoryItem(weaponElement, 5);
        addListViewItem(weaponListView, weaponPartyInventoryItem);

        InventoryElement armorElement = InventoryElementFactory.getInstance().getInventoryElement(InventoryElement.ElementID.BREASTPLATE1);
        PartyInventoryItem armorPartyInventoryItem = new PartyInventoryItem(armorElement, 5);
        addListViewItem(armorListView, armorPartyInventoryItem);
        //////////////////////////

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
        potionsScrollPaneList.setSize(listWidth - 2, listHeight - listTopPadding - labelHeight);
        potionsScrollPaneList.setX(2);
        consumablesBackground.setSize(listWidth, listHeight - labelHeight);
        consumablesScrollPaneList.setSize(listWidth - 2, listHeight - listTopPadding - labelHeight);
        consumablesScrollPaneList.setX(2);
        throwingBackground.setSize(listWidth, listHeight - labelHeight);
        throwingScrollPaneList.setSize(listWidth - 4, listHeight - listTopPadding - labelHeight);
        throwingScrollPaneList.setX(2);

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
        nonQuestScrollPaneList.setSize(listWidth - 2, listHeight - listTopPadding - labelHeight);
        nonQuestScrollPaneList.setX(2);
        questBackground.setSize(listWidth, listHeight - labelHeight);
        questScrollPaneList.setSize(listWidth - 4, listHeight - listTopPadding - labelHeight);
        questScrollPaneList.setX(2);

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
        float descTablePadding = 10;
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

        graphicGroup.addActor(graphicBackground);

        equipmentButton.addListener(new ClickListener() {
                                    @Override
                                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                        setButtonState(ButtonState.EQUIPMENT);
                                        return true;
                                    }

                                    @Override
                                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                        setLists(ButtonState.EQUIPMENT);
                                    }
                                }
        );

        consumablesButton.addListener(new ClickListener() {
                                        @Override
                                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                            setButtonState(ButtonState.CONSUMABLES);
                                            return true;
                                        }

                                        @Override
                                        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                            setLists(ButtonState.CONSUMABLES);
                                        }
                                    }
        );

        keyItemsButton.addListener(new ClickListener() {
                                        @Override
                                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                            setButtonState(ButtonState.KEY_ITEMS);
                                            return true;
                                        }

                                        @Override
                                        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                            setLists(ButtonState.KEY_ITEMS);
                                        }
                                    }
        );

        swapButton.addListener(new ClickListener() {
                                   @Override
                                   public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
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
                                               descText.setText("Select another key item?????");
                                               break;
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
                                        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                            Gdx.app.log(TAG, "backButton up");
                                            // there is an issue with the button x, y coordinates
                                            //if (touchPointIsInButton(backButton)) {
                                                hide();
                                            //}
                                        }
                                    }
        );

        actionButton.addListener(new ClickListener() {
                                   @Override
                                   public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                       return true;
                                   }

                                   @Override
                                   public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                       Gdx.app.log(TAG, "actionButton up");
                                       String buttonName = actionButton.getText().toString();

                                       if (buttonName.equals(BTN_NAME_EQUIP)) {
                                            displayEquipScreen();
                                       }
                                       else if (buttonName.equals(BTN_NAME_USE)) {

                                       }
                                       else if (buttonName.equals(BTN_NAME_INSPECT)) {

                                       }
                                   }
                               }
        );

        weaponListView.addListener(new ClickListener() {
                                        @Override
                                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                            armorListView.setSelectedIndex(-1);
                                            return true;
                                        }

                                        @Override
                                        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                            if (isSwapping) {
                                                if (lastSelectedEquipmentListType == ListType.WEAPON) {
                                                    TextButton selectedItem  = weaponListView.getSelected();
                                                    if (selectedItem != lastSelectedEquipmentItem) {
                                                        swapListItems(weaponListView, selectedItem, lastSelectedEquipmentItem);

                                                        PartyInventory.getInstance().swapItems((PartyInventoryItem) selectedItem.getUserObject(),
                                                                                                (PartyInventoryItem) lastSelectedEquipmentItem.getUserObject());

                                                        descText.setText(ITEMS_SWAPPED);
                                                        isSwapping = false;
                                                        lastSelectedEquipmentItem = selectedItem;
                                                    }
                                                }
                                                else {
                                                    reselectLastListItem(lastSelectedEquipmentListType);
                                                    weaponListView.setSelectedIndex(-1);
                                                }
                                            }
                                            else {
                                                lastSelectedEquipmentItem = weaponListView.getSelected();
                                                PartyInventoryItem partyInventoryItem = (PartyInventoryItem) lastSelectedEquipmentItem.getUserObject();
                                                InventoryElement element = partyInventoryItem.getElement();
                                                descText.setText(element.summary);

                                                //todo: get names from somewhere else. also, the list will change depending on what is selected.
                                                Array<Label> names = new Array<>();
                                                names.add(new Label("Character 2", Utility.ELMOUR_UI_SKIN, "battle"));
                                                names.add(new Label("Justin", Utility.ELMOUR_UI_SKIN, "battle"));
                                                names.add(new Label("Jaxon", Utility.ELMOUR_UI_SKIN, "battle"));

                                                createNameTable(weaponNameTable, names);
                                                clearNameTable(armorNameTable);
                                                lastSelectedEquipmentListType = ListType.WEAPON;
                                            }
                                        }
                                    }
        );

        armorListView.addListener(new ClickListener() {
                                       @Override
                                       public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                           weaponListView.setSelectedIndex(-1);
                                           return true;
                                       }

                                       @Override
                                       public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                           if (isSwapping) {
                                               if (lastSelectedEquipmentListType == ListType.ARMOR) {
                                                   TextButton selectedItem  = armorListView.getSelected();
                                                   if (selectedItem != lastSelectedEquipmentItem) {
                                                       swapListItems(armorListView, selectedItem, lastSelectedEquipmentItem);

                                                       PartyInventory.getInstance().swapItems((PartyInventoryItem) selectedItem.getUserObject(),
                                                                                                (PartyInventoryItem) lastSelectedEquipmentItem.getUserObject());

                                                       descText.setText(ITEMS_SWAPPED);
                                                       isSwapping = false;
                                                       lastSelectedEquipmentItem = selectedItem;
                                                   }
                                               }
                                               else {
                                                   reselectLastListItem(lastSelectedEquipmentListType);
                                                   armorListView.setSelectedIndex(-1);
                                               }
                                           }
                                           else {
                                               lastSelectedEquipmentItem = armorListView.getSelected();
                                               PartyInventoryItem partyInventoryItem = (PartyInventoryItem) lastSelectedEquipmentItem.getUserObject();
                                               InventoryElement element = partyInventoryItem.getElement();
                                               descText.setText(element.summary);

                                               //todo: get names from somewhere else. also, the list will change depending on what is selected.
                                               Array<Label> names = new Array<>();
                                               names.add(new Label("Carmen", Utility.ELMOUR_UI_SKIN, "battle"));
                                               names.add(new Label("Character 1", Utility.ELMOUR_UI_SKIN, "battle"));
                                               names.add(new Label("Character 2", Utility.ELMOUR_UI_SKIN, "battle"));

                                               createNameTable(armorNameTable, names);
                                               clearNameTable(weaponNameTable);
                                               lastSelectedEquipmentListType = ListType.ARMOR;
                                           }
                                       }
                                   }
        );

        potionsListView.addListener(new ClickListener() {
                                        @Override
                                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                            consumablesListView.setSelectedIndex(-1);
                                            throwingListView.setSelectedIndex(-1);
                                            return true;
                                        }

                                        @Override
                                        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                            if (isSwapping) {
                                                if (lastSelectedConsumablesListType == ListType.POTION) {
                                                    TextButton selectedItem  = potionsListView.getSelected();
                                                    if (selectedItem != lastSelectedConsumablesItem) {
                                                        swapListItems(potionsListView, selectedItem, lastSelectedConsumablesItem);

                                                        PartyInventory.getInstance().swapItems((PartyInventoryItem) selectedItem.getUserObject(),
                                                                                                (PartyInventoryItem) lastSelectedConsumablesItem.getUserObject());

                                                        descText.setText(ITEMS_SWAPPED);
                                                        isSwapping = false;
                                                        lastSelectedConsumablesItem = selectedItem;
                                                    }
                                                }
                                                else {
                                                    reselectLastListItem(lastSelectedConsumablesListType);
                                                    potionsListView.setSelectedIndex(-1);
                                                }
                                            }
                                            else {
                                                lastSelectedConsumablesItem = potionsListView.getSelected();
                                                PartyInventoryItem partyInventoryItem = (PartyInventoryItem) lastSelectedConsumablesItem.getUserObject();
                                                InventoryElement element = partyInventoryItem.getElement();
                                                descText.setText(element.summary);
                                                lastSelectedConsumablesListType = ListType.POTION;
                                            }
                                        }
                                    }
        );

        consumablesListView.addListener(new ClickListener() {
                                        @Override
                                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                            potionsListView.setSelectedIndex(-1);
                                            throwingListView.setSelectedIndex(-1);
                                            return true;
                                        }

                                        @Override
                                        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                            if (isSwapping) {
                                                TextButton selectedItem  = consumablesListView.getSelected();
                                                if (lastSelectedConsumablesListType == ListType.CONSUMABLE) {
                                                    if (selectedItem != lastSelectedConsumablesItem) {
                                                        swapListItems(consumablesListView, selectedItem, lastSelectedConsumablesItem);

                                                        PartyInventory.getInstance().swapItems((PartyInventoryItem) selectedItem.getUserObject(),
                                                                                                (PartyInventoryItem) lastSelectedConsumablesItem.getUserObject());

                                                        descText.setText(ITEMS_SWAPPED);
                                                        isSwapping = false;
                                                        lastSelectedConsumablesItem = selectedItem;
                                                    }
                                                }
                                                else {
                                                    reselectLastListItem(lastSelectedConsumablesListType);
                                                    consumablesListView.setSelectedIndex(-1);
                                                }
                                            }
                                            else {
                                                lastSelectedConsumablesItem = consumablesListView.getSelected();
                                                PartyInventoryItem partyInventoryItem = (PartyInventoryItem) lastSelectedConsumablesItem.getUserObject();
                                                InventoryElement element = partyInventoryItem.getElement();
                                                descText.setText(element.summary);
                                                lastSelectedConsumablesListType = ListType.CONSUMABLE;
                                            }
                                        }
                                    }
        );

        throwingListView.addListener(new ClickListener() {
                                        @Override
                                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                            potionsListView.setSelectedIndex(-1);
                                            consumablesListView.setSelectedIndex(-1);
                                            return true;
                                        }

                                        @Override
                                        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                            if (isSwapping) {
                                                if (lastSelectedConsumablesListType == ListType.THROWING) {
                                                    TextButton selectedItem  = throwingListView.getSelected();
                                                    if (selectedItem != lastSelectedConsumablesItem) {
                                                        swapListItems(throwingListView, selectedItem, lastSelectedConsumablesItem);

                                                        PartyInventory.getInstance().swapItems((PartyInventoryItem) selectedItem.getUserObject(),
                                                                                                (PartyInventoryItem) lastSelectedConsumablesItem.getUserObject());

                                                        descText.setText(ITEMS_SWAPPED);
                                                        isSwapping = false;
                                                        lastSelectedConsumablesItem = selectedItem;
                                                    }
                                                }
                                                else {
                                                    reselectLastListItem(lastSelectedConsumablesListType);
                                                    throwingListView.setSelectedIndex(-1);
                                                }
                                            }
                                            else {
                                                lastSelectedConsumablesItem = throwingListView.getSelected();
                                                PartyInventoryItem partyInventoryItem = (PartyInventoryItem) lastSelectedConsumablesItem.getUserObject();
                                                InventoryElement element = partyInventoryItem.getElement();
                                                descText.setText(element.summary);
                                                lastSelectedConsumablesListType = ListType.THROWING;
                                            }
                                        }
                                    }
        );

        questListView.addListener(new ClickListener() {
                                         @Override
                                         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                             nonQuestListView.setSelectedIndex(-1);
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
                                         }
                                     }
        );

        nonQuestListView.addListener(new ClickListener() {
                                      @Override
                                      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                          questListView.setSelectedIndex(-1);
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
                                      }
                                  }
        );
    }

    private void displayEquipScreen() {
        equipmentListsTable.clear();

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

        weaponNameBackground.setSize(listWidth, nameTableHeight);
        armorNameBackground.setSize(listWidth, nameTableHeight);
        //equipNameBackground.setSize(listWidth, nameTableHeight);

        equipmentListsTable.row().width(stage.getWidth()).height(labelHeight - 2);
        equipmentListsTable.add(labelWeapon).pad(-1).width(listWidth);
        equipmentListsTable.add(labelArmor).pad(-1).width(listWidth);
        equipmentListsTable.add(labelEquipTo).pad(-1).width(listWidth);

        equipmentListsTable.row().width(stage.getWidth()).height(stage.getHeight() - bottomMargin - labelHeight - nameTableHeight - mainButtonTable.getHeight() + 2);
        equipmentListsTable.add(groupWeapon).pad(-1).width(listWidth);
        equipmentListsTable.add(groupArmor).pad(-1).width(listWidth);
        equipmentListsTable.add(groupEquipName).pad(-1).width(listWidth);

        equipmentListsTable.row().width(stage.getWidth()).height(nameTableHeight);
        equipmentListsTable.add(groupWeaponName).pad(-1).width(listWidth);
        equipmentListsTable.add(groupArmorName).pad(-1).width(listWidth * 2).colspan(2);
        equipmentListsTable.pack();
    }

    private void displayEquipmentScreen() {
        equipmentListsTable.clear();

        float listWidth = mainButtonTable.getWidth()/2 + 2;

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
            nameHeight = 12;
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

        switch (event) {
            case INVENTORY_ADDED:
                switch(element.category) {
                    case Potion:
                        addListViewItem(potionsListView, partyInventoryItem);
                        break;
                    case Consumables:
                        addListViewItem(consumablesListView, partyInventoryItem);
                        break;
                    case Throwing:
                        addListViewItem(throwingListView, partyInventoryItem);
                        break;
                }
                break;
            case INVENTORY_REMOVED:
                break;
        }
    }

    @Override
    public void onNotify(PartyInventoryItem item1, PartyInventoryItem item2, PartyInventoryEvent event) {

    }

    private void addListViewItem(MyTextButtonList<TextButton> list, PartyInventoryItem partyInventoryItem) {
        InventoryElement element = partyInventoryItem.getElement();
        String description = String.format("%s (%d)", element.name, partyInventoryItem.getQuantity());
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
            list.getItems().add(button);
        }
    }
}
