package com.smoftware.elmour.UI;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.ElmourGame;
import com.smoftware.elmour.InventoryElement;
import com.smoftware.elmour.PartyInventory;
import com.smoftware.elmour.PartyInventoryItem;
import com.smoftware.elmour.PartyInventoryObserver;
import com.smoftware.elmour.Utility;

/**
 * Created by steve on 2/10/19.
 */

public class InventoryHUD implements Screen, InventoryHudSubject, PartyInventoryObserver {
    private static final String TAG = InventoryHUD.class.getSimpleName();

    public enum ButtonState { EQUIPMENT, CONSUMABLES, KEY_ITEMS }

    // main buttons
    private final String BTN_NAME_EQUIPMENT = "Equipment";
    private final String BTN_NAME_CONSUMABLES = "Consumables";
    private final String BTN_NAME_KEY_ITEMS = "Key Items";

    private Stage stage;
    private Array<InventoryHudObserver> observers;

    // action buttons
    private final String BTN_NAME_USE = "Use";
    private final String BTN_NAME_INSPECT = "Inspect";
    private final String BTN_NAME_EQUIP = "Equip";
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
    private Table consumableListsTable;
    private Table keyItemsListsTable;
    private Table mainButtonTable;
    private Table actionButtonTable;

    // lists
    private WidgetGroup groupWeapon;
    private WidgetGroup groupArmor;
    private WidgetGroup groupWeaponName;
    private WidgetGroup groupArmorName;
    private WidgetGroup groupNonQuest;
    private WidgetGroup groupQuest;
    private WidgetGroup groupPotions;
    private WidgetGroup groupConsumables;
    private WidgetGroup groupThrowing;

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

        equipmentButton = new TextButton(BTN_NAME_EQUIPMENT, Utility.ELMOUR_UI_SKIN, "battle");
        consumablesButton = new TextButton(BTN_NAME_CONSUMABLES, Utility.ELMOUR_UI_SKIN, "battle");
        keyItemsButton = new TextButton(BTN_NAME_KEY_ITEMS, Utility.ELMOUR_UI_SKIN, "battle");

        actionButton = new TextButton(BTN_NAME_USE, Utility.ELMOUR_UI_SKIN, "battle");
        swapButton = new TextButton(BTN_NAME_SWAP, Utility.ELMOUR_UI_SKIN, "battle");
        backButton = new TextButton(BTN_BACK, Utility.ELMOUR_UI_SKIN, "battle");

        float bottomMargin = 6;
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
        float labelHeight = 30;

        //
        // EQUIPMENT
        //
        labelWeapon = new TextButton("Weapon", Utility.ELMOUR_UI_SKIN, "battle");
        labelWeapon.setTouchable(Touchable.disabled);
        weaponListView = new MyTextButtonList<>(Utility.ELMOUR_UI_SKIN);
        weaponScrollPaneList = new ScrollPane(weaponListView);
        weaponBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");

        labelArmor = new TextButton("Armor", Utility.ELMOUR_UI_SKIN, "battle");
        labelArmor.setTouchable(Touchable.disabled);
        armorListView = new MyTextButtonList<>(Utility.ELMOUR_UI_SKIN);
        armorScrollPaneList = new ScrollPane(armorListView);
        armorBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");

        weaponNameBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        armorNameBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");

        groupWeapon = new WidgetGroup();
        groupArmor = new WidgetGroup();
        groupWeaponName = new WidgetGroup();
        groupArmorName = new WidgetGroup();

        float nameTableHeight = 2 * buttonHeight - 2;

        weaponBackground.setSize(listWidth, listHeight - labelHeight - nameTableHeight + 2);
        weaponScrollPaneList.setSize(listWidth, listHeight - listTopPadding - labelHeight - nameTableHeight + 2);
        armorBackground.setSize(listWidth, listHeight - labelHeight - nameTableHeight + 2);
        armorScrollPaneList.setSize(listWidth, listHeight - listTopPadding - labelHeight - nameTableHeight + 2);

        weaponNameBackground.setSize(listWidth, nameTableHeight);
        armorNameBackground.setSize(listWidth, nameTableHeight);

        //todo: get names from somewhere else. also, the list will change depending on what is selected.
        Array<Label> names = new Array<>();
        names.add(new Label("Carmen", Utility.ELMOUR_UI_SKIN, "battle"));
        names.add(new Label("Character", Utility.ELMOUR_UI_SKIN, "battle"));
        names.add(new Label("Character", Utility.ELMOUR_UI_SKIN, "battle"));
        names.add(new Label("Justin", Utility.ELMOUR_UI_SKIN, "battle"));
        names.add(new Label("Jaxon", Utility.ELMOUR_UI_SKIN, "battle"));

        Array<Label> names2 = new Array<>();
        names2.add(new Label("Carmen", Utility.ELMOUR_UI_SKIN, "battle"));
        names2.add(new Label("Character", Utility.ELMOUR_UI_SKIN, "battle"));
        names2.add(new Label("Character", Utility.ELMOUR_UI_SKIN, "battle"));
        names2.add(new Label("Justin", Utility.ELMOUR_UI_SKIN, "battle"));
        names2.add(new Label("Jaxon", Utility.ELMOUR_UI_SKIN, "battle"));

        createNameTable(weaponNameTable, names);
        createNameTable(armorNameTable, names2);

        groupWeapon.addActor(weaponBackground);
        groupWeapon.addActor(weaponScrollPaneList);
        groupArmor.addActor(armorBackground);
        groupArmor.addActor(armorScrollPaneList);
        groupWeaponName.addActor(weaponNameBackground);
        groupWeaponName.addActor(weaponNameTable);
        groupArmorName.addActor(armorNameBackground);
        groupArmorName.addActor(armorNameTable);

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

        labelConsumables = new TextButton("Consumables", Utility.ELMOUR_UI_SKIN, "battle");
        labelConsumables.setTouchable(Touchable.disabled);
        consumablesListView = new MyTextButtonList<>(Utility.ELMOUR_UI_SKIN);
        consumablesScrollPaneList = new ScrollPane(consumablesListView);
        consumablesBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");

        labelThrowing = new TextButton("Throwing", Utility.ELMOUR_UI_SKIN, "battle");
        labelThrowing.setTouchable(Touchable.disabled);
        throwingListView = new MyTextButtonList<>(Utility.ELMOUR_UI_SKIN);
        throwingScrollPaneList = new ScrollPane(throwingListView);
        throwingBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");

        groupPotions = new WidgetGroup();
        groupConsumables = new WidgetGroup();
        groupThrowing = new WidgetGroup();

        listWidth = buttonWidth;

        potionsBackground.setSize(listWidth, listHeight - labelHeight);
        potionsScrollPaneList.setSize(listWidth, listHeight - listTopPadding - labelHeight);
        consumablesBackground.setSize(listWidth, listHeight - labelHeight);
        consumablesScrollPaneList.setSize(listWidth, listHeight - listTopPadding - labelHeight);
        throwingBackground.setSize(listWidth, listHeight - labelHeight);
        throwingScrollPaneList.setSize(listWidth, listHeight - listTopPadding - labelHeight);

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

        labelQuest = new TextButton("Quest", Utility.ELMOUR_UI_SKIN, "battle");
        labelQuest.setTouchable(Touchable.disabled);
        questListView = new MyTextButtonList<>(Utility.ELMOUR_UI_SKIN);
        questScrollPaneList = new ScrollPane(questListView);
        questBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");

        groupNonQuest = new WidgetGroup();
        groupQuest = new WidgetGroup();

        listWidth = mainButtonTable.getWidth()/2 + 2;

        nonQuestBackground.setSize(listWidth, listHeight - labelHeight);
        nonQuestScrollPaneList.setSize(listWidth, listHeight - listTopPadding - labelHeight);
        questBackground.setSize(listWidth, listHeight - labelHeight);
        questScrollPaneList.setSize(listWidth, listHeight - listTopPadding - labelHeight);

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

        graphicGroup.addActor(graphicBackground);

        equipmentButton.addListener(new ClickListener() {
                                    @Override
                                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                        equipmentButton.setStyle(Utility.ELMOUR_UI_SKIN.get("force_down", TextButton.TextButtonStyle.class));
                                        consumablesButton.setStyle(Utility.ELMOUR_UI_SKIN.get("battle", TextButton.TextButtonStyle.class));
                                        keyItemsButton.setStyle(Utility.ELMOUR_UI_SKIN.get("battle", TextButton.TextButtonStyle.class));
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
                                            equipmentButton.setStyle(Utility.ELMOUR_UI_SKIN.get("battle", TextButton.TextButtonStyle.class));
                                            consumablesButton.setStyle(Utility.ELMOUR_UI_SKIN.get("force_down", TextButton.TextButtonStyle.class));
                                            keyItemsButton.setStyle(Utility.ELMOUR_UI_SKIN.get("battle", TextButton.TextButtonStyle.class));
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
                                            equipmentButton.setStyle(Utility.ELMOUR_UI_SKIN.get("battle", TextButton.TextButtonStyle.class));
                                            consumablesButton.setStyle(Utility.ELMOUR_UI_SKIN.get("battle", TextButton.TextButtonStyle.class));
                                            keyItemsButton.setStyle(Utility.ELMOUR_UI_SKIN.get("force_down", TextButton.TextButtonStyle.class));
                                            return true;
                                        }

                                        @Override
                                        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                            setLists(ButtonState.KEY_ITEMS);
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

        potionsListView.addListener(new ClickListener() {
                                        @Override
                                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                            consumablesListView.setSelectedIndex(-1);
                                            throwingListView.setSelectedIndex(-1);
                                            return true;
                                        }

                                        @Override
                                        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                            TextButton btn = potionsListView.getSelected();
                                            InventoryElement element = (InventoryElement)btn.getUserObject();
                                            descText.setText(element.summary);
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
                                            TextButton btn = consumablesListView.getSelected();
                                            InventoryElement element = (InventoryElement)btn.getUserObject();
                                            descText.setText(element.summary);
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
                                            TextButton btn = throwingListView.getSelected();
                                            InventoryElement element = (InventoryElement)btn.getUserObject();
                                            descText.setText(element.summary);
                                        }
                                    }
        );
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

        table.pack();
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

    public void setLists(ButtonState state) {

        //todo: set text based on last selection
        descText.setText("");


        switch (state) {
            case EQUIPMENT:
                stage.addActor(equipmentListsTable);
                consumableListsTable.remove();
                keyItemsListsTable.remove();
                break;
            case CONSUMABLES:
                equipmentListsTable.remove();
                stage.addActor(consumableListsTable);
                keyItemsListsTable.remove();
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
        stage.addActor(consumableListsTable);
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

    private void addListViewItem(MyTextButtonList<TextButton> list, PartyInventoryItem partyInventoryItem) {
        InventoryElement element = partyInventoryItem.getElement();
        String description = String.format("%s (%d)", element.name, partyInventoryItem.getQuantity());
        TextButton button = new TextButton(description, Utility.ELMOUR_UI_SKIN, "tree_node");
        button.setUserObject(element);

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
