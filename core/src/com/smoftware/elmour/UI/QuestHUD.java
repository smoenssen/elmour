package com.smoftware.elmour.UI;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.ElmourGame;
import com.smoftware.elmour.Utility;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.quest.Quest;
import com.smoftware.elmour.quest.QuestGraph;
import com.smoftware.elmour.quest.QuestList;
import com.smoftware.elmour.quest.QuestTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by steve on 5/14/19.
 */

public class QuestHUD implements Screen, QuestHudSubject {
    private static final String TAG = QuestHUD.class.getSimpleName();

    private final String SORT_BY_QUEST_NAME = " Sort by quest name";
    private final String SORT_BY_ORDER_OBTAINED = " Sort by order obtained";
    private final String SHOW_COMPLETED = " Show completed quests";
    private final String SHOW_NOT_COMPLETED = " Show uncompleted quests";
    private final String SHOW_ALL_QUESTS = " Show all quests";

    private final String SORT_QUEST_ORDER_PREFERENCE = "sortQuestOrderPreference";
    private final String SORT_ORDER_ASCENDING = "ascending";
    private final String SORT_ORDER_DESCENDING = "descending";
    private final String SORT_QUEST_BY_PREFERENCE = "sortQuestByPreference";
    private final String SHOW_QUEST_PREFERENCE = "showQuestPreference";

    private ElmourGame game;
    private Stage stage;
    private Array<QuestHudObserver> observers;
    private QuestList questList;

    private Image downButton;
    private Image upButton;
    private MyTextArea sortPanelBackground;

    SelectBox<String> sortingSelectBox;
    SelectBox<String> showQuestsSelectBox;
    private Image showQuestsSelectBtn;
    private Image sortingSelectBtn;

    private String sortByPreference;
    private String showQuestsPreference;
    private String sortOrderPreference;

    private TextButton closeButton;

    private TextButton labelQuests;
    private Table questListTableView;
    private ScrollPane questScrollPaneList;
    private MyTextArea questBackground;
    private float questListWidth;
    private float questListHeight;
    float questListRowPadding = 2;

    private TextButton labelTasks;
    private Table taskTableView;
    private ScrollPane taskScrollPaneList;
    private MyTextArea taskBackground;
    private float taskListWidth;
    private float taskListHeight;

    private Table mainTable;

    ImageTextButton selectedImageTextButton;

    public class QuestComparatorAscending implements Comparator<String> {
        @Override
        public int compare(String arg0, String arg1) {
            return arg0.compareTo(arg1);
        }
    }

    public class QuestComparatorDescending implements Comparator<String> {
        @Override
        public int compare(String arg0, String arg1) {
            return  arg0.compareTo(arg1) * -1;
        }
    }

    public QuestHUD(final ElmourGame game, Stage stage) {
        this.game = game;
        this.stage = stage;
        observers = new Array<>();

        questList = new QuestList(Quest.getAllMainQuestGraphs(), false);

        float sortPanelHeight = 40;
        float labelHeight = 35;
        float closeButtonHeight = 40;
        final float selectBoxHeight = 30;
        float margin = 15;
        float btnWidth = selectBoxHeight/2;
        float btnHeight = selectBoxHeight/2;
        questListWidth = stage.getWidth()/3 + 2;
        taskListWidth = stage.getWidth() - questListWidth - (2 * margin);
        float listHeight = (stage.getHeight() - sortPanelHeight - labelHeight - (2 * margin));
        taskListHeight = listHeight;
        questListHeight = listHeight - closeButtonHeight + 4;
        float listWidth = questListWidth + taskListWidth;
        float listTopPadding = 6;
        float selectBoxPadding = (sortPanelHeight - selectBoxHeight) / 2;
        float selectBoxWidth = (listWidth - 3 * selectBoxPadding) / 2;

        /*
        **  SORT PANEL
        */
        WidgetGroup groupSortPanel = new WidgetGroup();

        showQuestsSelectBox = new SelectBox<>(Utility.ELMOUR_UI_SKIN);
        showQuestsSelectBox.setItems(SHOW_NOT_COMPLETED, SHOW_COMPLETED, SHOW_ALL_QUESTS);
        showQuestsSelectBtn = new Image(new Texture("RPGGame/maps/Game/Icons/Buttons/Down_Button_Down.png"));

        sortingSelectBox = new SelectBox<>(Utility.ELMOUR_UI_SKIN);
        sortingSelectBox.setItems(SORT_BY_QUEST_NAME, SORT_BY_ORDER_OBTAINED);
        sortingSelectBtn = new Image(new Texture("RPGGame/maps/Game/Icons/Buttons/Down_Button_Down.png"));

        WidgetGroup groupCompletedSelect = new WidgetGroup();
        groupCompletedSelect.addActor(showQuestsSelectBox);
        groupCompletedSelect.addActor(showQuestsSelectBtn);

        WidgetGroup groupSortingSelect = new WidgetGroup();
        groupSortingSelect.addActor(sortingSelectBox);
        groupSortingSelect.addActor(sortingSelectBtn);

        sortPanelBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        sortPanelBackground.setTouchable(Touchable.disabled);

        showQuestsSelectBox.setSize(selectBoxWidth, selectBoxHeight);
        groupCompletedSelect.setPosition(selectBoxPadding, (sortPanelHeight - selectBoxHeight)/2);

        showQuestsSelectBtn.setSize(btnWidth, btnHeight);
        showQuestsSelectBtn.setPosition(selectBoxWidth - showQuestsSelectBtn.getWidth() * 1.5f, (selectBoxHeight - showQuestsSelectBtn.getHeight())/2);

        sortingSelectBox.setSize(selectBoxWidth, selectBoxHeight);
        groupSortingSelect.setPosition(groupCompletedSelect.getX() + showQuestsSelectBox.getWidth() + selectBoxPadding, (sortPanelHeight - selectBoxHeight)/2);

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
        questListTableView = new Table();

        labelQuests = new TextButton("Quests", Utility.ELMOUR_UI_SKIN, "battle");
        questScrollPaneList = new ScrollPane(questListTableView);
        questBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        questBackground.setTouchable(Touchable.disabled);

        downButton = new Image(new Texture("RPGGame/maps/Game/Icons/Buttons/Down_Button.png"));
        upButton = new Image(new Texture("RPGGame/maps/Game/Icons/Buttons/Up_Button.png"));
        upButton.setVisible(false);

        WidgetGroup groupDownBtn = new WidgetGroup();
        groupDownBtn.addActor(downButton);

        WidgetGroup groupUpBtn = new WidgetGroup();
        groupUpBtn.addActor(upButton);

        closeButton = new TextButton("Close", Utility.ELMOUR_UI_SKIN);

        labelQuests.setSize(questListWidth, labelHeight - 2);

        downButton.setSize(btnWidth, btnHeight);
        upButton.setSize(btnWidth, btnHeight);

        groupDownBtn.setPosition((labelHeight - btnHeight)/2, (labelHeight - btnHeight)/2);
        groupUpBtn.setPosition((labelHeight - btnHeight)/2, (labelHeight - btnHeight)/2);

        groupQuestLabel.addActor(labelQuests);
        groupQuestLabel.addActor(groupDownBtn);
        groupQuestLabel.addActor(groupUpBtn);

        closeButton.setWidth(questListWidth);
        closeButton.setHeight(closeButtonHeight);

        questBackground.setSize(questListWidth, questListHeight);
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
                                        return true;
                                    }

                                    @Override
                                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                        if (sortOrderPreference.equals(SORT_ORDER_ASCENDING)) {
                                            downButton.setVisible(false);
                                            upButton.setVisible(true);
                                            setSortingOrder(SORT_ORDER_DESCENDING);
                                            sortQuestList();
                                        }
                                        else {
                                            upButton.setVisible(false);
                                            downButton.setVisible(true);
                                            setSortingOrder(SORT_ORDER_ASCENDING);
                                            sortQuestList();
                                        }
                                    }
                                }
        );

        downButton.addListener(new ClickListener() {
                                     @Override
                                     public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                         return true;
                                     }

                                     @Override
                                     public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                         downButton.setVisible(false);
                                         upButton.setVisible(true);
                                         setSortingOrder(SORT_ORDER_DESCENDING);
                                         sortQuestList();
                                     }
                                 }
        );

        upButton.addListener(new ClickListener() {
                                     @Override
                                     public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                         return true;
                                     }

                                     @Override
                                     public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                         upButton.setVisible(false);
                                         downButton.setVisible(true);
                                         setSortingOrder(SORT_ORDER_ASCENDING);
                                         sortQuestList();
                                     }
                                 }
        );

        showQuestsSelectBtn.addListener(new ClickListener() {
                                    @Override
                                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                        return true;
                                    }

                                    @Override
                                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                        showQuestsSelectBox.showList();
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

        sortingSelectBox.addListener(new ChangeListener() {
                                        @Override
                                        public void changed(ChangeEvent event, Actor actor) {
                                            if (sortOrderPreference == null) return;

                                            String selection = sortingSelectBox.getSelected();
                                            if (selection != null) {
                                                sortByPreference = selection;
                                                sortQuestList();
                                            }
                                        }
                                     }
        );

        showQuestsSelectBox.addListener(new ChangeListener() {
                                        @Override
                                        public void changed(ChangeEvent event, Actor actor) {
                                            if (sortOrderPreference == null) return;

                                            String selection = showQuestsSelectBox.getSelected();
                                            if (selection != null) {
                                                showQuestsPreference = selection;
                                                sortQuestList();
                                            }
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

        questListTableView.addListener(new ClickListener() {
                                           @Override
                                           public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                               return true;
                                           }

                                           @Override
                                           public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                               if (selectedImageTextButton != null) {
                                                   // deselect previous button
                                                   selectedImageTextButton.setStyle(Utility.ELMOUR_UI_SKIN.get("default", ImageTextButton.ImageTextButtonStyle.class));
                                               }

                                               if (event.getTarget() instanceof Label) {
                                                   Label label = (Label) event.getTarget();
                                                   selectedImageTextButton = (ImageTextButton) label.getParent();
                                               } else if (event.getTarget() instanceof ImageTextButton) {
                                                   selectedImageTextButton = (ImageTextButton) event.getTarget();
                                               }

                                               selectedImageTextButton.setStyle(Utility.ELMOUR_UI_SKIN.get("force_down", ImageTextButton.ImageTextButtonStyle.class));

                                               QuestGraph questGraph = questList.getQuestByQuestTitle(selectedImageTextButton.getText().toString());
                                               setTaskListViewItems(questGraph.getAllQuestTasks());
                                           }
                                       }
        );
    }

    private void setSortingOrder(String sortingOrder) {
        sortOrderPreference = sortingOrder;
        ProfileManager.getInstance().setProperty(SORT_QUEST_ORDER_PREFERENCE, sortOrderPreference);
    }

    private void getSortingPreferences() {
        // SORT BY PREFERENCE
        sortByPreference = ProfileManager.getInstance().getProperty(SORT_QUEST_BY_PREFERENCE, String.class);
        if (sortByPreference == null) {
            sortByPreference = SORT_BY_QUEST_NAME;
        }

        sortingSelectBox.setSelected(sortByPreference);

        // SHOW QUESTS PREFERENCE
        showQuestsPreference = ProfileManager.getInstance().getProperty(SHOW_QUEST_PREFERENCE, String.class);
        if (showQuestsPreference == null) {
            showQuestsPreference = SHOW_ALL_QUESTS;
        }

        showQuestsSelectBox.setSelected(showQuestsPreference);

        // SORT ORDER PREFERENCE
        sortOrderPreference = ProfileManager.getInstance().getProperty(SORT_QUEST_ORDER_PREFERENCE, String.class);
        if (sortOrderPreference == null) {
            sortOrderPreference = SORT_ORDER_ASCENDING;
        }

        if (sortOrderPreference.equals(SORT_ORDER_ASCENDING)) {
            upButton.setVisible(false);
            downButton.setVisible(true);
        }
        else {
            upButton.setVisible(true);
            downButton.setVisible(false);
        }
    }

    private void sortQuestList() {
        ArrayList<String> strings = questList.getAllQuestIDs();

        if (sortByPreference.equals(SORT_BY_QUEST_NAME)) {
            if (sortOrderPreference.equals(SORT_ORDER_ASCENDING))
                Collections.sort(strings, new QuestComparatorAscending());
            else
                Collections.sort(strings, new QuestComparatorDescending());
        }
        else if (sortByPreference.equals(SORT_BY_ORDER_OBTAINED)) {

        }

        ArrayList<String> questIDs = new ArrayList<>();

        if (showQuestsPreference.equals(SHOW_NOT_COMPLETED)) {
            for (String string : strings) {
                if (string.trim().length() > 0) {
                    QuestGraph questGraph = questList.getQuestByID(string);
                    if (!questGraph.isQuestComplete()) {
                        questIDs.add(questGraph.getQuestID());
                    }
                }
            }
        }
        else if (showQuestsPreference.equals(SHOW_COMPLETED)) {
            for (String string : strings) {
                if (string.trim().length() > 0) {
                    QuestGraph questGraph = questList.getQuestByID(string);
                    if (questGraph.isQuestComplete()) {
                        questIDs.add(questGraph.getQuestID());
                    }
                }
            }
        }
        else if (showQuestsPreference.equals(SHOW_ALL_QUESTS)) {
            for (String string : strings) {
                if (string.trim().length() > 0) {
                    QuestGraph questGraph = questList.getQuestByID(string);
                    questIDs.add(questGraph.getQuestID());
                }
            }
        }

        setQuestListItems(questIDs);

        questScrollPaneList.layout();
/*
        ArrayList<String> strings = new ArrayList<>();

        Array<Cell> cells = questListTableView.getCells();
        for (Cell cell : cells) {
            String string = cell.toString();
            String [] sa = string.split(":");
            if (sa.length > 1 && sa[1].trim().length() > 0) {
                strings.add(sa[1].trim());
            }
        }

        if (cells.size == 0) {
            return;
        }


        if (sortByPreference.equals(SORT_BY_QUEST_NAME)) {
            if (sortOrderPreference.equals(SORT_ORDER_ASCENDING))
                Collections.sort(strings, new QuestComparatorAscending());
            else
                Collections.sort(strings, new QuestComparatorDescending());
        }
        else if (sortByPreference.equals(SORT_BY_ORDER_OBTAINED)) {

        }

        ArrayList<String> questIDs = new ArrayList<>();

        if (showQuestsPreference.equals(SHOW_NOT_COMPLETED)) {
            for (String string : strings) {
                if (!string.isEmpty()) {
                    QuestGraph questGraph = questList.getQuestByQuestTitle(string);
                    if (!questGraph.isQuestComplete()) {
                        questIDs.add(questGraph.getQuestID());
                    }
                }
            }
        }
        else if (showQuestsPreference.equals(SHOW_COMPLETED)) {
            for (String string : strings) {
                if (!string.isEmpty()) {
                    QuestGraph questGraph = questList.getQuestByQuestTitle(string);
                    if (questGraph.isQuestComplete()) {
                        questIDs.add(questGraph.getQuestID());
                    }
                }
            }
        }
        else if (showQuestsPreference.equals(SHOW_ALL_QUESTS)) {
            for (String string : strings) {
                if (!string.isEmpty()) {
                    QuestGraph questGraph = questList.getQuestByQuestTitle(string);
                    questIDs.add(questGraph.getQuestID());
                }
            }
        }

        setQuestListItems(questIDs);

        questScrollPaneList.layout();*/
    }

    private void addQuestListViewItem(QuestGraph questGraph) {
        Image noCheck = new Image(new Texture("graphics/noCheckmark.png"));
        Image check = new Image(new Texture("graphics/blackCheckmark.png"));
        check.setSize(32, 32);

        ImageTextButton button = new ImageTextButton(questGraph.getQuestTitle(), Utility.ELMOUR_UI_SKIN);
        button.setUserObject(questGraph);
        button.align(Align.left);

        button.clearChildren();

        if (questGraph.isQuestComplete()) {
            button.add(check).pad(5);
        }
        else {
            button.add(noCheck).pad(5);
        }

        button.add(button.getLabel());

        questListTableView.row().width(questListWidth);
        questListTableView.add(button).pad(questListRowPadding).width(questListWidth).align(Align.left);

        questListTableView.layout();
        questScrollPaneList.layout();
    }

    private void setQuestListItems(ArrayList<String> items) {
        questListTableView.clearChildren();

        for (int i=0; i<1; i++) {
            for (String questID : items) {
                QuestGraph questGraph = questList.getQuestByID(questID);
                addQuestListViewItem(questGraph);
            }
        }

        // hack to fill a dummy row to get first row at top of scroll panel, otherwise table is vertically centered
        float usedSpace = 0;
        float rowHeight = 0;

        if (questListTableView.getRows() > 0) {
            rowHeight = questListTableView.getRowHeight(0);
            usedSpace = (questListTableView.getRows()) * (rowHeight);
        }

        float remainingSpace = questListTableView.getHeight() - usedSpace;

        if (remainingSpace > 0) {
            questScrollPaneList.setScrollingDisabled(true, true);

            ImageTextButton dummy = new ImageTextButton("", Utility.ELMOUR_UI_SKIN);
            dummy.setTouchable(Touchable.disabled);
            questListTableView.row().width(questListWidth).height(remainingSpace);
            questListTableView.add(dummy).pad(questListRowPadding).width(questListWidth).align(Align.left);
        }
        else {
            questScrollPaneList.setScrollingDisabled(false, false);
        }
    }

    private void setTaskListViewItems(ArrayList<QuestTask> taskList) {
        taskTableView.clear();

        float usedSpace = 16;

        //todo: order tasks by dependencies

        for(int i=0;i<1;i++) {
            for (QuestTask questTask : taskList) {
                Image bullet;
                Image bullet2;
                Label text;

                if (questTask.isTaskComplete()) {
                    bullet = new Image(new Texture("graphics/blackCheckmark.png"));
                    text = new Label(questTask.getTaskPhrase(), Utility.ELMOUR_UI_SKIN, "grayed_out");
                } else {
                    bullet = new Image(new Texture("graphics/bullet.png"));
                    text = new Label(questTask.getTaskPhrase(), Utility.ELMOUR_UI_SKIN, "battle");
                }

                text.setWrap(true);
                text.setWidth(taskListWidth - 30);
                text.setAlignment(Align.topLeft);
                text.pack();

                taskTableView.row().align(Align.top).width(taskListWidth).height(text.getHeight()).expandY().fillY();
                taskTableView.add(bullet).align(Align.top).pad(7, 9, 0, 2).width(16).height(16);
                taskTableView.add(text).pad(5).width(taskListWidth - 30);

                usedSpace += text.getHeight();

                QuestList subQuestList = questTask.getSubQuestList();
                if (subQuestList != null) {
                    QuestGraph questGraph = subQuestList.getQuestByQuestTitle(questTask.getId());

                    for (QuestTask subQuestTask : questGraph.getAllQuestTasks()) {
                        if (subQuestTask.isTaskComplete()) {
                            bullet2 = new Image(new Texture("graphics/blackCheckmark.png"));
                            text = new Label(subQuestTask.getTaskPhrase(), Utility.ELMOUR_UI_SKIN, "grayed_out");
                        } else {
                            bullet2 = new Image(new Texture("graphics/bullet2.png"));
                            text = new Label(subQuestTask.getTaskPhrase(), Utility.ELMOUR_UI_SKIN, "battle");
                        }

                        text.setWrap(true);
                        text.setWidth(taskListWidth - 30);
                        text.setAlignment(Align.topLeft);
                        text.pack();

                        float leftIndent = 10;
                        taskTableView.row().align(Align.top).width(taskListWidth - leftIndent).height(text.getHeight()).expandY().fillY();
                        taskTableView.add(bullet2).align(Align.top).pad(7, 9 + leftIndent, 0, 2).width(16).height(16);
                        taskTableView.add(text).pad(5).width(taskListWidth - leftIndent - 30);

                        usedSpace += text.getHeight();
                    }
                }
            }
        }

        // hack to fill in dummy rows to get first row at top of scroll panel, otherwise table is vertically centered
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

        getSortingPreferences();
        setQuestListItems(questList.getAllQuestIDs());
        sortQuestList();
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
