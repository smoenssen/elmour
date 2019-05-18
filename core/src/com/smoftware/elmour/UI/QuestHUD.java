package com.smoftware.elmour.UI;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.ElmourGame;
import com.smoftware.elmour.Utility;
import com.smoftware.elmour.quest.QuestGraph;
import com.smoftware.elmour.quest.QuestList;
import com.smoftware.elmour.quest.QuestTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by steve on 5/14/19.
 */

public class QuestHUD implements Screen, QuestHudSubject {
    private static final String TAG = QuestHUD.class.getSimpleName();

    private final String SORT_BY_QUEST_NAME = " Sort by quest name";
    private final String SORT_BY_ORDER_OBTAINED = " Sort by order obtained";
    private final String COMPLETED = " Show completed quests";
    private final String NOT_COMPLETED = " Show uncompleted quests";
    private final String ALL_QUESTS = " Show all quests";

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
    private Image completedSelectBtn;
    private Image sortingSelectBtn;

    private TextButton closeButton;

    private TextButton labelQuests;
    private Table questTableView;
    private MyTextButtonList<TextButton> questListView;
    private ScrollPane questScrollPaneList;
    private MyTextArea questBackground;

    private TextButton labelTasks;
    private Table taskTableView;
    private ScrollPane taskScrollPaneList;
    private MyTextArea taskBackground;
    private float taskListWidth;
    private float taskListHeight;

    private Table mainTable;

    private boolean isQuestListAscending = true;

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

        float sortPanelHeight = 40;
        float labelHeight = 35;
        float closeButtonHeight = 40;
        float selectBoxHeight = 30;
        float margin = 15;
        float btnWidth = selectBoxHeight/2;
        float btnHeight = selectBoxHeight/2;
        float questListWidth = stage.getWidth()/3 + 2;
        taskListWidth = stage.getWidth() - questListWidth - (2 * margin);
        float listHeight = (stage.getHeight() - sortPanelHeight - labelHeight - (2 * margin));
        taskListHeight = listHeight;
        float listWidth = questListWidth + taskListWidth;
        float listTopPadding = 6;
        float selectBoxPadding = (sortPanelHeight - selectBoxHeight) / 2;
        float selectBoxWidth = (listWidth - 3 * selectBoxPadding) / 2;

        /*
        **  SORT PANEL
        */
        WidgetGroup groupSortPanel = new WidgetGroup();

        completedSelectBox = new SelectBox<>(Utility.ELMOUR_UI_SKIN);
        completedSelectBox.setItems(NOT_COMPLETED, COMPLETED, ALL_QUESTS);
        completedSelectBtn = new Image(new Texture("RPGGame/maps/Game/Icons/Buttons/Down_Button_Down.png"));

        sortingSelectBox = new SelectBox<>(Utility.ELMOUR_UI_SKIN);
        sortingSelectBox.setItems(SORT_BY_QUEST_NAME, SORT_BY_ORDER_OBTAINED);
        sortingSelectBtn = new Image(new Texture("RPGGame/maps/Game/Icons/Buttons/Down_Button_Down.png"));

        WidgetGroup groupCompletedSelect = new WidgetGroup();
        groupCompletedSelect.addActor(completedSelectBox);
        groupCompletedSelect.addActor(completedSelectBtn);

        WidgetGroup groupSortingSelect = new WidgetGroup();
        groupSortingSelect.addActor(sortingSelectBox);
        groupSortingSelect.addActor(sortingSelectBtn);

        sortPanelBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        sortPanelBackground.setTouchable(Touchable.disabled);

        completedSelectBox.setSize(selectBoxWidth, selectBoxHeight);
        groupCompletedSelect.setPosition(selectBoxPadding, (sortPanelHeight - selectBoxHeight)/2);

        completedSelectBtn.setSize(btnWidth, btnHeight);
        completedSelectBtn.setPosition(selectBoxWidth - completedSelectBtn.getWidth() * 1.5f, (selectBoxHeight - completedSelectBtn.getHeight())/2);

        sortingSelectBox.setSize(selectBoxWidth, selectBoxHeight);
        groupSortingSelect.setPosition(groupCompletedSelect.getX() + completedSelectBox.getWidth() + selectBoxPadding, (sortPanelHeight - selectBoxHeight)/2);

        sortingSelectBtn.setSize(btnWidth, btnHeight);
        sortingSelectBtn.setPosition(selectBoxWidth - sortingSelectBtn.getWidth() * 1.5f, (selectBoxHeight - sortingSelectBtn.getHeight())/2);

        sortPanelBackground.setSize(questListWidth + taskListWidth, sortPanelHeight);

        groupSortPanel.addActor(sortPanelBackground);
        groupSortPanel.addActor(groupCompletedSelect);
        groupSortPanel.addActor(groupSortingSelect);

        /*
        **  QUEST LIST
        */
        WidgetGroup groupQuestLabel = new WidgetGroup();
        WidgetGroup groupQuests = new WidgetGroup();
        questTableView = new Table();

        labelQuests = new TextButton("Quests", Utility.ELMOUR_UI_SKIN, "battle");
        questListView = new MyTextButtonList<>(Utility.ELMOUR_UI_SKIN);
        questScrollPaneList = new ScrollPane(questListView);
        questBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        questBackground.setTouchable(Touchable.disabled);

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

        closeButton = new TextButton("Close", Utility.ELMOUR_UI_SKIN, "battle");

        labelQuests.setSize(questListWidth, labelHeight - 2);

        downButtonDown.setSize(btnWidth, btnHeight);
        downButtonUp.setSize(btnWidth, btnHeight);
        upButtonUp.setSize(btnWidth, btnHeight);
        upButtonDown.setSize(btnWidth, btnHeight);

        groupDownBtn.setPosition((labelHeight - btnHeight)/2, (labelHeight - btnHeight)/2);
        groupUpBtn.setPosition((labelHeight - btnHeight)/2, (labelHeight - btnHeight)/2);

        groupQuestLabel.addActor(labelQuests);
        groupQuestLabel.addActor(groupDownBtn);
        groupQuestLabel.addActor(groupUpBtn);

        closeButton.setWidth(questListWidth);
        closeButton.setHeight(closeButtonHeight);

        questBackground.setSize(questListWidth, listHeight - closeButtonHeight + 4);
        questBackground.setY(closeButtonHeight - 2);
        questScrollPaneList.setSize(questListWidth - 4, listHeight - sortPanelHeight - listTopPadding);
        questScrollPaneList.setX(2);
        questScrollPaneList.setY(2 + closeButtonHeight);

        groupQuests.addActor(questBackground);
        groupQuests.addActor(questScrollPaneList);
        groupQuests.addActor(closeButton);

        /*
        **  TASK LIST
        */
        WidgetGroup groupTasks = new WidgetGroup();
        taskTableView = new Table();
        //taskTableView.debugAll();
        labelTasks = new TextButton("Tasks", Utility.ELMOUR_UI_SKIN, "battle");
        labelTasks.setTouchable(Touchable.disabled);
        taskScrollPaneList = new ScrollPane(taskTableView);

        taskBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        taskBackground.setTouchable(Touchable.disabled);

        taskBackground.setSize(taskListWidth, listHeight + 2);
        taskScrollPaneList.setSize(taskListWidth - 4, listHeight - listTopPadding);
        taskScrollPaneList.setX(2);
        taskScrollPaneList.setY(2);

        groupTasks.addActor(taskBackground);
        groupTasks.addActor(taskScrollPaneList);

        /*
        **  MAIN TABLE
        */
        // Note: padding -1 is so boxes can be overlapping to keep the border to 2 pixels
        mainTable = new Table();
        mainTable.row().width(stage.getWidth()).height(sortPanelHeight - 2);
        mainTable.add(groupSortPanel).pad(-1).colspan(2).width(listWidth);

        mainTable.row().width(stage.getWidth()).height(labelHeight - 2);
        mainTable.add(groupQuestLabel).pad(-1).width(questListWidth);
        mainTable.add(labelTasks).pad(-1).width(taskListWidth);

        mainTable.row().width(stage.getWidth()).height(stage.getHeight() - sortPanelHeight - (2 * margin) - labelHeight + 2);
        mainTable.add(groupQuests).pad(-1).width(questListWidth);
        mainTable.add(groupTasks).pad(-1).width(taskListWidth);

        mainTable.pack();
        mainTable.setPosition(margin, margin);
        //mainTable.debugAll();

        show();

        labelQuests.addListener(new ClickListener() {
                                    @Override
                                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                        if (isQuestListAscending) {
                                            downButtonDown.setVisible(true);
                                            downButtonUp.setVisible(false);
                                        }
                                        else {
                                            upButtonDown.setVisible(true);
                                            upButtonUp.setVisible(false);
                                        }
                                        return true;
                                    }

                                    @Override
                                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                        if (isQuestListAscending) {
                                            sortQuestListDescending();
                                        }
                                        else {
                                            sortQuestListAscending();
                                        }
                                    }
                                }
        );

        downButtonUp.addListener(new ClickListener() {
                                     @Override
                                     public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                         downButtonDown.setVisible(true);
                                         downButtonUp.setVisible(false);
                                         return true;
                                     }

                                     @Override
                                     public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                         sortQuestListDescending();
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
                                         sortQuestListAscending();
                                     }
                                 }
        );

        completedSelectBtn.addListener(new ClickListener() {
                                    @Override
                                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                        return true;
                                    }

                                    @Override
                                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                        completedSelectBox.showList();
                                    }
                                }
        );

        sortingSelectBtn.addListener(new ClickListener() {
                                           @Override
                                           public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                               return true;
                                           }

                                           @Override
                                           public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                               sortingSelectBox.showList();
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

    private void sortQuestListAscending() {
        isQuestListAscending = true;

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

    private void sortQuestListDescending() {
        isQuestListAscending = false;

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

    boolean change = true;
    private void setTaskListViewItems(ArrayList<QuestTask> taskList) {
        taskTableView.clear();

        float usedSpace = 16;

        //todo: order tasks by dependencies

        for(int i=0;i<2;i++) {
            for (QuestTask questTask : taskList) {
                Image bullet;
                Label text;

                if (questTask.isTaskComplete()) {
                    bullet = new Image(new Texture("graphics/blackCheckmark.png"));
                    text = new Label(questTask.getTaskPhrase(), Utility.ELMOUR_UI_SKIN, "grayed_out");
                } else {
                    bullet = new Image(new Texture("graphics/bullet.png"));
                    text = new Label(questTask.getTaskPhrase() + questTask.getTaskPhrase() + questTask.getTaskPhrase() + questTask.getTaskPhrase() + questTask.getTaskPhrase() + questTask.getTaskPhrase(), Utility.ELMOUR_UI_SKIN, "battle");
                }

                text.setWrap(true);
                text.setWidth(taskListWidth - 30);
                text.setAlignment(Align.topLeft);
                text.pack();

                taskTableView.row().align(Align.top).width(taskListWidth).height(text.getHeight()).expandY().fillY();
                taskTableView.add(bullet).align(Align.top).pad(7, 9, 0, 2).width(16).height(16);
                taskTableView.add(text).pad(5).width(taskListWidth - 30);

                usedSpace += text.getHeight();
            }
        }

        // hack to fill in dummy rows to get first row at top of scroll panel, otherwise table is vertically centered
        Label dummyText = new Label("DUMMY", Utility.ELMOUR_UI_SKIN, "grayed_out");
        float remainingSpace = taskListHeight - usedSpace;

        if (remainingSpace > 0) {
            taskScrollPaneList.setScrollingDisabled(true, true);

            int numDummyRowsNeeded = (int) (remainingSpace / 23);//dummyText.getHeight());

            for (int i = 0; i < numDummyRowsNeeded; i++) {
                Image dummy = new Image();
                Label text = new Label("", Utility.ELMOUR_UI_SKIN, "grayed_out");

                taskTableView.row().align(Align.top).width(taskListWidth).height(text.getHeight()).expandY().fillY();
                taskTableView.add(dummy).align(Align.top).pad(7, 9, 0, 2).width(16).height(16);
                taskTableView.add(text).pad(5).width(taskListWidth - 30);
            }
        }
        else {
            taskScrollPaneList.setScrollingDisabled(false, false);
        }

        taskTableView.layout();
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
        stage.addActor(mainTable);
        notify(QuestHudObserver.QuestHudEvent.QUEST_HUD_SHOWN);

        questListView.getItems().clear();

        //todo: get sorting preference and sort
        // order completed will be the order written to the profile
        // also need to set isQuestListAscending
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
        mainTable.remove();
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
