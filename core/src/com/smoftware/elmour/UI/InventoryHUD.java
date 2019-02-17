package com.smoftware.elmour.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.Utility;

/**
 * Created by steve on 2/10/19.
 */

public class InventoryHUD implements Screen, InventoryHudSubject {
    private static final String TAG = InventoryHUD.class.getSimpleName();

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
    private Table listsTable;
    private Table mainButtonTable;
    private Table actionButtonTable;

    // lists
    private WidgetGroup groupPotions;
    private WidgetGroup groupConsumables;
    private WidgetGroup groupThrowing;

    private TextButton labelPotions;
    private List<String> potionsListView;
    private ScrollPane potionsScrollPaneList;
    private MyTextArea potionsBackground;

    private TextButton labelConsumables;
    private List<String> consumablesListView;
    private ScrollPane consumablesScrollPaneList;
    private MyTextArea consumablesBackground;

    private TextButton labelThrowing;
    private List<String> throwingListView;
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

        listsTable = new Table();
        mainButtonTable = new Table();
        actionButtonTable = new Table();

        equipmentButton = new TextButton(BTN_NAME_EQUIPMENT, Utility.ELMOUR_UI_SKIN, "battle");
        consumablesButton = new TextButton(BTN_NAME_CONSUMABLES, Utility.ELMOUR_UI_SKIN, "battle");
        keyItemsButton = new TextButton(BTN_NAME_KEY_ITEMS, Utility.ELMOUR_UI_SKIN, "battle");

        actionButton = new TextButton(BTN_NAME_USE, Utility.ELMOUR_UI_SKIN, "battle");
        swapButton = new TextButton(BTN_NAME_SWAP, Utility.ELMOUR_UI_SKIN, "battle");
        backButton = new TextButton(BTN_BACK, Utility.ELMOUR_UI_SKIN, "battle");

        labelPotions = new TextButton("Potions", Utility.ELMOUR_UI_SKIN, "battle");
        labelPotions.setTouchable(Touchable.disabled);
        potionsListView = new List<>(Utility.ELMOUR_UI_SKIN);
        potionsScrollPaneList = new ScrollPane(potionsListView);
        potionsBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");

        labelConsumables = new TextButton("Consumables", Utility.ELMOUR_UI_SKIN, "battle");
        labelConsumables.setTouchable(Touchable.disabled);
        consumablesListView = new List<>(Utility.ELMOUR_UI_SKIN);
        consumablesScrollPaneList = new ScrollPane(consumablesListView);
        consumablesBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");

        labelThrowing = new TextButton("Throwing", Utility.ELMOUR_UI_SKIN, "battle");
        labelThrowing.setTouchable(Touchable.disabled);
        throwingListView = new List<>(Utility.ELMOUR_UI_SKIN);
        throwingScrollPaneList = new ScrollPane(throwingListView);
        throwingBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");

        ////////////// todo: temporary
        int size = 40;
        String[] strings = new String[size];
        for (int i = 0, k = 0; i < size; i++) {
            strings[k++] = "Item: " + i;

        }
        potionsListView.setItems(strings);
        consumablesListView.setItems(strings);
        throwingListView.setItems(strings);
        /////////////////////////////////

        groupPotions = new WidgetGroup();
        groupConsumables = new WidgetGroup();
        groupThrowing = new WidgetGroup();

        float leftMargin = 6;
        float bottomMargin = 6;
        float topMargin = 6;
        float buttonHeight = 65;
        float buttonWidth = stage.getWidth() / 5 - 1;

        mainButtonTable.row().width(stage.getWidth()).height(buttonHeight);
        mainButtonTable.add(equipmentButton).pad(-1).width(buttonWidth);
        mainButtonTable.add(consumablesButton).pad(-1).width(buttonWidth);
        mainButtonTable.add(keyItemsButton).pad(-1).width(buttonWidth);
        mainButtonTable.pack();
        mainButtonTable.setPosition(leftMargin, bottomMargin);

        float listWidth = (buttonWidth);
        float listHeight = (stage.getHeight() - mainButtonTable.getHeight() - bottomMargin);
        float listTopPadding = 6;
        float labelHeight = 30;

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

        listsTable.row().width(stage.getWidth()).height(labelHeight - 2);
        listsTable.add(labelPotions).pad(-1).width(buttonWidth);
        listsTable.add(labelConsumables).pad(-1).width(buttonWidth);
        listsTable.add(labelThrowing).pad(-1).width(buttonWidth);

        listsTable.row().width(stage.getWidth()).height(stage.getHeight() - bottomMargin - labelHeight - mainButtonTable.getHeight());
        listsTable.add(groupPotions).pad(-1).width(buttonWidth);
        listsTable.add(groupConsumables).pad(-1).width(buttonWidth);
        listsTable.add(groupThrowing).pad(-1).width(buttonWidth);
        listsTable.pack();
        listsTable.setPosition(leftMargin, bottomMargin + mainButtonTable.getHeight());

        //listsTable.debugAll();
        float descTablePadding = 10;
        float descAreaWidth = buttonWidth;
        float descAreaHeight = buttonHeight * 3 - 4;

        descText = new Label("", Utility.ELMOUR_UI_SKIN, "battle");
        descText.setWrap(true);
        descText.setText("This is the place where the summary will be shown. This is the place where the summary will be shown. This is the place where the summary will be shown. This is the place where the summary will be shown. ");

        descScrollPanel = new ScrollPane(descText);
        descScrollPanel.setHeight(10);

        descBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        descBackground.setSize(descAreaWidth, descAreaHeight);
        descBackground.setPosition(leftMargin + listsTable.getWidth() - 1, bottomMargin - 1);

        descTable = new Table();
        descTable.setHeight(descAreaHeight - descTablePadding);
        descTable.setWidth(descAreaWidth);
        descTable.pad(descTablePadding).defaults().expandX().fillX().space(0);
        descTable.setPosition(leftMargin + listsTable.getWidth(), bottomMargin + descTablePadding/2);
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
                                        return true;
                                    }

                                    @Override
                                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                        Gdx.app.log(TAG, "equipmentButton up");
                                        if (touchPointIsInButton(equipmentButton)) {

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

    @Override
    public void show() {
        stage.addActor(listsTable);
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
        listsTable.remove();
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
}
