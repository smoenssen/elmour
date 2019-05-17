package com.smoftware.elmour.UI;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.ElmourGame;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.Utility;
import com.smoftware.elmour.quest.QuestGraph;
import com.smoftware.elmour.quest.QuestList;
import com.smoftware.elmour.quest.QuestTask;
import com.smoftware.elmour.screens.BattleScreen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by steve on 5/14/19.
 */

public class QuestHUD implements Screen, QuestHudSubject {
    private static final String TAG = QuestHUD.class.getSimpleName();

    private final String SORT_BY_QUEST_NAME = "Sort by quest name";
    private final String SORT_BY_ORDER_COMPLETED = "Sort by order completed";
    private final String COMPLETED = "Completed";
    private final String NOT_COMPLETED = "Not completed";

    private ElmourGame game;
    private Stage stage;
    private Array<QuestHudObserver> observers;
    private QuestList questList;

    private Image downButtonUp;
    private Image downButtonDown;
    private Image upButtonUp;
    private Image upButtonDown;
    private MyTextArea sortPanelBackground;

    SelectBox<String> sortingSelectBox;
    SelectBox<String> completedSelectBox;

    private TextButton closeButton;

    private TextButton labelQuests;
    private MyTextButtonList<TextButton> questListView;
    private ScrollPane questScrollPaneList;
    private MyTextArea questBackground;

    private TextButton labelTasks;
    private MyTextButtonList<TextButton> taskListView;
    private ScrollPane taskScrollPaneList;
    private MyTextArea taskBackground;

    private WidgetGroup groupSortPanel;
    private WidgetGroup groupQuests;
    private WidgetGroup groupTasks;

    private Table listsTable;

    public class QuestComparatorAscending implements Comparator<TextButton> {
        @Override
        public int compare(TextButton arg0, TextButton arg1) {
            return arg0.getText().toString().compareTo(arg1.getText().toString());
        }
    }

    public class QuestComparatorDescending implements Comparator<TextButton> {
        @Override
        public int compare(TextButton arg0, TextButton arg1) {
            return  arg0.getText().toString().compareTo(arg1.getText().toString()) * -1;
        }
    }

    public QuestHUD(final ElmourGame game, Stage stage) {
        this.game = game;
        this.stage = stage;
        observers = new Array<>();
        questList = new QuestList();

        groupSortPanel = new WidgetGroup();
        groupQuests = new WidgetGroup();
        groupTasks = new WidgetGroup();

        listsTable = new Table();

        float btnWidth = 20;
        float btnHeight = 20;
        downButtonUp = new Image(new Texture("RPGGame/maps/Game/Icons/Buttons/Down_Button.png"));
        downButtonDown = new Image(new Texture("RPGGame/maps/Game/Icons/Buttons/Down_Button_Down.png"));
        downButtonDown.setVisible(false);

        upButtonUp = new Image(new Texture("RPGGame/maps/Game/Icons/Buttons/Up_Button.png"));
        upButtonUp.setVisible(false);
        upButtonDown = new Image(new Texture("RPGGame/maps/Game/Icons/Buttons/Up_Button_Down.png"));
        upButtonDown.setVisible(false);

        WidgetGroup groupDownBtn = new WidgetGroup();
        groupDownBtn.addActor(downButtonUp);
        groupDownBtn.addActor(downButtonDown);

        WidgetGroup groupUpBtn = new WidgetGroup();
        groupUpBtn.addActor(upButtonUp);
        groupUpBtn.addActor(upButtonDown);

        sortingSelectBox = new SelectBox<>(Utility.ELMOUR_UI_SKIN);
        sortingSelectBox.setItems(SORT_BY_QUEST_NAME, SORT_BY_ORDER_COMPLETED);

        sortPanelBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        sortPanelBackground.setTouchable(Touchable.disabled);

        labelQuests = new TextButton("Quests", Utility.ELMOUR_UI_SKIN, "battle");
        labelQuests.setTouchable(Touchable.disabled);
        questListView = new MyTextButtonList<>(Utility.ELMOUR_UI_SKIN);
        questScrollPaneList = new ScrollPane(questListView);
        questBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        questBackground.setTouchable(Touchable.disabled);

        labelTasks = new TextButton("Tasks", Utility.ELMOUR_UI_SKIN, "battle");
        labelTasks.setTouchable(Touchable.disabled);
        taskListView = new MyTextButtonList<>(Utility.ELMOUR_UI_SKIN);
        taskListView.setTouchable(Touchable.disabled);
        taskScrollPaneList = new ScrollPane(taskListView);
        taskBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        taskBackground.setTouchable(Touchable.disabled);

        completedSelectBox = new SelectBox<>(Utility.ELMOUR_UI_SKIN);
        completedSelectBox.setItems(NOT_COMPLETED, COMPLETED);

        closeButton = new TextButton("Close", Utility.ELMOUR_UI_SKIN, "battle");

        float sortPanelHeight = 40;
        float labelHeight = 40;
        float selectBoxHeight = 30;
        float margin = 15;

        float questListWidth = stage.getWidth()/3 + 2;
        float taskListWidth = stage.getWidth() - questListWidth - (2 * margin);
        float listHeight = (stage.getHeight() - sortPanelHeight - labelHeight - (2 * margin));
        float listWidth = questListWidth + taskListWidth;
        float listTopPadding = 6;
        float sortSelectBoxWidth = questListWidth * 1.25f;

        downButtonDown.setSize(btnWidth, btnHeight);
        downButtonUp.setSize(btnWidth, btnHeight);
        upButtonUp.setSize(btnWidth, btnHeight);
        upButtonDown.setSize(btnWidth, btnHeight);

        groupDownBtn.setPosition(btnWidth/2, (sortPanelHeight - btnHeight)/2);
        groupUpBtn.setPosition(btnWidth/2, (sortPanelHeight - btnHeight)/2);

        sortingSelectBox.setSize(sortSelectBoxWidth, selectBoxHeight);
        sortingSelectBox.setPosition(groupDownBtn.getX() + btnWidth * 1.5f, (sortPanelHeight - selectBoxHeight)/2);

        sortPanelBackground.setSize(questListWidth + taskListWidth, sortPanelHeight);

        completedSelectBox.setWidth(questListWidth);
        completedSelectBox.setHeight(selectBoxHeight);
        completedSelectBox.setY(2 + labelHeight);

        closeButton.setWidth(questListWidth);
        closeButton.setHeight(labelHeight);

        // Note: the -2 prevents highlight of selected item from crossing over the borders
        // also need to set position then +2
        questBackground.setSize(questListWidth, listHeight - labelHeight + 4);
        questBackground.setY(labelHeight - 2);
        questScrollPaneList.setSize(questListWidth - 2, listHeight - listTopPadding - sortPanelHeight - labelHeight - selectBoxHeight);
        questScrollPaneList.setX(2);
        questScrollPaneList.setY(2 + labelHeight + selectBoxHeight);

        taskBackground.setSize(taskListWidth, listHeight + 2);
        taskScrollPaneList.setSize(taskListWidth - 4, listHeight - listTopPadding - sortPanelHeight);
        taskScrollPaneList.setX(2);
        taskScrollPaneList.setY(2);

        groupSortPanel.addActor(sortPanelBackground);
        groupSortPanel.addActor(groupDownBtn);
        groupSortPanel.addActor(groupUpBtn);
        groupSortPanel.addActor(sortingSelectBox);

        groupQuests.addActor(questBackground);
        groupQuests.addActor(questScrollPaneList);
        groupQuests.addActor(completedSelectBox);
        groupQuests.addActor(closeButton);

        groupTasks.addActor(taskBackground);
        groupTasks.addActor(taskScrollPaneList);

        // Note: padding -1 is so boxes can be overlapping to keep the border to 2 pixels

        listsTable.row().width(stage.getWidth()).height(sortPanelHeight - 2);
        listsTable.add(groupSortPanel).colspan(2).width(listWidth);

        listsTable.row().width(stage.getWidth()).height(labelHeight - 2);
        listsTable.add(labelQuests).pad(-1).width(questListWidth);
        listsTable.add(labelTasks).pad(-1).width(taskListWidth);

        listsTable.row().width(stage.getWidth()).height(stage.getHeight() - sortPanelHeight - (2 * margin) - labelHeight + 2);
        listsTable.add(groupQuests).pad(-1).width(questListWidth);
        listsTable.add(groupTasks).pad(-1).width(taskListWidth);

        listsTable.pack();
        listsTable.setPosition(margin, margin);
        //listsTable.debugAll();

        show();

        downButtonUp.addListener(new ClickListener() {
                                     @Override
                                     public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                         downButtonDown.setVisible(true);
                                         downButtonUp.setVisible(false);
                                         return true;
                                     }

                                     @Override
                                     public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                         downButtonDown.setVisible(false);
                                         upButtonUp.setVisible(true);

                                         TextButton [] array = questListView.getItems().toArray(TextButton.class);
                                         ArrayList<TextButton> arrayList = new ArrayList<>(Arrays.asList(array));
                                         Collections.sort(arrayList, new QuestComparatorDescending());
                                         questListView.getItems().clear();
                                         for (TextButton textButton : arrayList) {
                                             TextButton btn = new TextButton(textButton.getText().toString(), Utility.ELMOUR_UI_SKIN, "tree_node");
                                             btn.setUserObject(textButton.getUserObject());
                                             questListView.getItems().add(btn);
                                         }
                                         questListView.layout();
                                         questScrollPaneList.layout();
                                     }
                                 }
        );

        upButtonUp.addListener(new ClickListener() {
                                     @Override
                                     public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                         upButtonDown.setVisible(true);
                                         upButtonUp.setVisible(false);
                                         return true;
                                     }

                                     @Override
                                     public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                         upButtonDown.setVisible(false);
                                         downButtonUp.setVisible(true);

                                         TextButton [] array = questListView.getItems().toArray(TextButton.class);
                                         ArrayList<TextButton> arrayList = new ArrayList<>(Arrays.asList(array));
                                         Collections.sort(arrayList, new QuestComparatorAscending());
                                         questListView.getItems().clear();
                                         for (TextButton textButton : arrayList) {
                                             TextButton btn = new TextButton(textButton.getText().toString(), Utility.ELMOUR_UI_SKIN, "tree_node");
                                             btn.setUserObject(textButton.getUserObject());
                                             questListView.getItems().add(btn);
                                         }
                                         questListView.layout();
                                         questScrollPaneList.layout();
                                     }
                                 }
        );

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

        questListView.addListener(new ClickListener() {
                                       @Override
                                       public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                           return true;
                                       }

                                       @Override
                                       public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                           TextButton selectedItem = questListView.getSelected();
                                           QuestGraph questGraph = (QuestGraph)selectedItem.getUserObject();
                                           setTaskListViewItems(questGraph.getAllQuestTasks());
                                       }
                                   }
        );
    }

    private void addQuestListViewItem(QuestGraph questGraph) {
        TextButton button = new TextButton("       " + questGraph.getQuestTitle(), Utility.ELMOUR_UI_SKIN, "tree_node");
        button.setUserObject(questGraph);

        if (questListView.getItems().size == 0) {
            // hack to get scrolling to work (need to add array if first item being added)
            TextButton[] buttons = new TextButton[1];
            buttons[0] = button;
            questListView.setItems(buttons);
            questListView.setSelectedIndex(-1);
        }
        else {
            // add new item
            questListView.getItems().add(button);
            questListView.layout();
            questScrollPaneList.layout();
        }
    }

    private void setTaskListViewItems(ArrayList<QuestTask> taskList) {
        taskListView.clearItems();

        for (QuestTask questTask : taskList) {
            TextButton button = new TextButton(questTask.getTaskPhrase(), Utility.ELMOUR_UI_SKIN, "tree_node");
            taskListView.getItems().add(button);
        }

        taskListView.layout();
        taskScrollPaneList.layout();
    }

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

        taskListView.getItems().clear();

        QuestGraph questGraph = questList.getQuestByID("TeddyBear");
        addQuestListViewItem(questGraph);

        QuestGraph questGraph2 = questList.getQuestByID("DogsQuest");
        addQuestListViewItem(questGraph2);
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
