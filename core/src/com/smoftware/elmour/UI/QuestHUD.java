package com.smoftware.elmour.UI;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.ElmourGame;
import com.smoftware.elmour.Utility;
import com.smoftware.elmour.quest.QuestGraph;
import com.smoftware.elmour.quest.QuestList;

/**
 * Created by steve on 5/14/19.
 */

public class QuestHUD implements Screen, QuestHudSubject {
    private static final String TAG = QuestHUD.class.getSimpleName();

    private ElmourGame game;
    private Stage stage;
    private Array<QuestHudObserver> observers;
    private QuestList questList;

    private TextButton closeButton;

    private TextButton labelQuests;
    private MyTextButtonList<TextButton> questListView;
    private ScrollPane questScrollPaneList;
    private MyTextArea questBackground;

    private TextButton labelTasks;
    private MyTextButtonList<TextButton> taskListView;
    private ScrollPane taskScrollPaneList;
    private MyTextArea taskBackground;

    private WidgetGroup groupQuests;
    private WidgetGroup groupTasks;

    private Table listsTable;

    private float labelHeight = 40;
    private float margin = 30;

    public QuestHUD(final ElmourGame game, Stage stage) {
        this.game = game;
        this.stage = stage;
        observers = new Array<>();
        questList = new QuestList();

        groupQuests = new WidgetGroup();
        groupTasks = new WidgetGroup();

        listsTable = new Table();

        labelQuests = new TextButton("Quests", Utility.ELMOUR_UI_SKIN, "battle");
        labelQuests.setTouchable(Touchable.disabled);
        questListView = new MyTextButtonList<>(Utility.ELMOUR_UI_SKIN);
        questScrollPaneList = new ScrollPane(questListView);
        questBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        questBackground.setTouchable(Touchable.disabled);

        labelTasks = new TextButton("Tasks", Utility.ELMOUR_UI_SKIN, "battle");
        labelTasks.setTouchable(Touchable.disabled);
        taskListView = new MyTextButtonList<>(Utility.ELMOUR_UI_SKIN);
        taskScrollPaneList = new ScrollPane(taskListView);
        taskBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        taskBackground.setTouchable(Touchable.disabled);

        float buttonHeight = stage.getHeight() / 7.5f;
        float buttonWidth = (int)stage.getWidth() / 5;
        float leftMargin = (stage.getWidth() - (buttonWidth * 5) + 6)/2;

        float questListWidth = stage.getWidth()/3 + 2;
        float taskListWidth = stage.getWidth() - questListWidth - (2 * margin);
        float listHeight = (stage.getHeight() - labelHeight - (2 * margin));
        float listTopPadding = 6;

        closeButton = new TextButton("Close", Utility.ELMOUR_UI_SKIN, "battle");
        closeButton.setWidth(questListWidth);
        closeButton.setHeight(labelHeight);

        // Note: the -2 prevents highlight of selected item from crossing over the borders
        // also need to set position then +2
        questBackground.setSize(questListWidth, listHeight - labelHeight + 4);
        questBackground.setY(labelHeight - 2);
        questScrollPaneList.setSize(questListWidth - 2, listHeight - labelHeight - listTopPadding - (2 * labelHeight) - 2);
        questScrollPaneList.setX(2);
        questScrollPaneList.setY(2 + labelHeight);

        taskBackground.setSize(taskListWidth, listHeight + 2);
        taskScrollPaneList.setSize(taskListWidth - 4, listHeight - listTopPadding - labelHeight - 2);
        taskScrollPaneList.setX(2);
        taskScrollPaneList.setY(2);

        groupQuests.addActor(questBackground);
        groupQuests.addActor(questScrollPaneList);
        groupQuests.addActor(closeButton);
        groupTasks.addActor(taskBackground);
        groupTasks.addActor(taskScrollPaneList);

        // Note: padding -1 is so boxes can be overlapping to keep the border to 2 pixels
        listsTable.row().width(stage.getWidth()).height(labelHeight - 2);
        listsTable.add(labelQuests).pad(-1).width(questListWidth);
        listsTable.add(labelTasks).pad(-1).width(taskListWidth);

        listsTable.row().width(stage.getWidth()).height(stage.getHeight() - (2 * margin) - labelHeight + 2);
        listsTable.add(groupQuests).pad(-1).width(questListWidth);
        listsTable.add(groupTasks).pad(-1).width(taskListWidth);

        listsTable.pack();
        listsTable.setPosition(margin, margin);
        //listsTable.debugAll();

        closeButton.addListener(new ClickListener() {
                                        @Override
                                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                            return true;
                                        }

                                        @Override
                                        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                            hide();
                                        }
                                    }
        );
    }
/*
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
    */

    public QuestGraph getQuestByID(String questID) {
        return questList.getQuestByID(questID);
    }

    public boolean isQuestReadyForReturn(String questID) {
        return questList.isQuestReadyForReturn(questID);
    }

    public void setQuestTaskStarted(String questID, String questTaskID) {
        questList.questTaskStarted(questID, questTaskID);
    }

    public void setQuestTaskComplete(String questID, String questTaskID) {
        questList.questTaskComplete(questID, questTaskID);
    }

    @Override
    public void show() {
        stage.addActor(listsTable);
        notify(QuestHudObserver.QuestHudEvent.QUEST_HUD_SHOWN);
    }

    @Override
    public void render(float delta) {

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
        notify(QuestHudObserver.QuestHudEvent.QUEST_HUD_HIDDEN);
    }

    @Override
    public void dispose() {

    }

    @Override
    public void addObserver(QuestHudObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(QuestHudObserver observer) {
        observers.removeValue(observer, true);
    }

    @Override
    public void notify(QuestHudObserver.QuestHudEvent event) {
        for(QuestHudObserver observer: observers){
            observer.onNotify(event);
        }
    }
}
